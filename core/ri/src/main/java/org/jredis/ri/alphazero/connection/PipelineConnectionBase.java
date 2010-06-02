/*
 *   Copyright 2009 Joubin Houshyar
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *    
 *   http://www.apache.org/licenses/LICENSE-2.0
 *    
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.jredis.ri.alphazero.connection;

import java.io.InputStream;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jredis.ClientRuntimeException;
import org.jredis.ProviderException;
import org.jredis.connector.ConnectionSpec;
import org.jredis.connector.NotConnectedException;
import org.jredis.connector.ConnectionSpec.SocketProperty;
import org.jredis.protocol.Command;
import org.jredis.protocol.Protocol;
import org.jredis.protocol.Request;
import org.jredis.protocol.Response;
import org.jredis.ri.alphazero.protocol.ConcurrentSynchProtocol;
import org.jredis.ri.alphazero.protocol.VirtualResponse;
import org.jredis.ri.alphazero.support.Assert;
import org.jredis.ri.alphazero.support.FastBufferedInputStream;
import org.jredis.ri.alphazero.support.Log;

/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Sep 7, 2009
 * @since   alpha.0
 * 
 */

public abstract class PipelineConnectionBase extends ConnectionBase {

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	/**  */
	private ResponseHandler	    	respHandler;

	/**  */
	private Thread 					respHandlerThread;

	/**  */
	private BlockingQueue<PendingRequest>	pendingResponseQueue;

	/** synchronization object used to serialize request queuing  */
	private Object					serviceLock = new Object();
	
	/** 
	 * flag (default false) indicates if a pending QUIT command is being processed.  
	 * If true, any calls to queueRequests will result in a raise runtime exception
	 */
	private boolean					pendingQuit = false;
	
	/** used by the Pipeline to indicate its state.  Set to true on connect and false on Quit/Close */
	private AtomicBoolean			isActive;
	
	/** counted down on notifyConnect */
	private CountDownLatch		    connectionEstablished;

	// ------------------------------------------------------------------------
	// Constructor(s)
	// ------------------------------------------------------------------------
	/**
	 * @param spec
	 * @throws ClientRuntimeException
	 */
	protected PipelineConnectionBase (ConnectionSpec spec) throws ClientRuntimeException {
		super(spec, true);
	}
	// ------------------------------------------------------------------------
	// Extension
	// ------------------------------------------------------------------------
	/**
     * 
     */
    @Override
    protected void initializeComponents () {
    	
    	spec.isReliable(true);
    	spec.isPipeline(true);
    	spec.isShared(true);
    	
    	super.initializeComponents();
    	
    	serviceLock = new Object();
    	isActive = new AtomicBoolean(false);
    	connectionEstablished = new CountDownLatch(1);
    	
    	pendingResponseQueue = new LinkedBlockingQueue<PendingRequest>();
    	respHandler = new ResponseHandler();
    	respHandlerThread = new Thread(respHandler, "response-handler");
    	respHandlerThread.start();
    	
    	isActive.set(false);
    }
    
    @Override
    protected void notifyConnected () {
    	super.notifyConnected();
		Log.log("Pipeline <%s> connected", this);
    	isActive.set(true);
    	connectionEstablished.countDown();
    }
    @Override
    protected void notifyDisconnected () {
    	super.notifyDisconnected();
		Log.log("Pipeline <%s> disconnected", this);
    	isActive.set(true);
    	connectionEstablished.countDown();
    }
   
    /**
     * Pipeline must use a concurrent protocol handler.
     *  
     * @see org.jredis.ri.alphazero.connection.ConnectionBase#newProtocolHandler()
     */
    @Override
    protected Protocol newProtocolHandler () {
		return new ConcurrentSynchProtocol();
    }
    
    /**
     * Just make sure its a {@link FastBufferedInputStream}.
     */
    @Override
	protected final InputStream newInputStream (InputStream socketInputStream) throws IllegalArgumentException {
    	
    	InputStream in = super.newInputStream(socketInputStream);
    	if(!(in instanceof FastBufferedInputStream)){
    		System.out.format("WARN: input was: %s\n", in.getClass().getCanonicalName());
    		in = new FastBufferedInputStream (in, spec.getSocketProperty(SocketProperty.SO_RCVBUF));
    	}
    	return in;
    }

    // ------------------------------------------------------------------------
	// Interface: Connection
	// ------------------------------------------------------------------------
    /**
     * This is a pseudo asynchronous method.  The actual write to server does 
     * occur in this method, so when this method returns, your request has been
     * sent.  This simply defers the response read to the response handler.
     * <p>
     * Other item of note is that once a QUIT request has been queued, no further
     * requests are accepted and a ClientRuntimeException is thrown.
     * 
     * @see org.jredis.ri.alphazero.connection.ConnectionBase#queueRequest(org.jredis.protocol.Command, byte[][])
     */
    @Override
    public final Future<Response> queueRequest (Command cmd, byte[]... args) 
    	throws ClientRuntimeException, ProviderException 
    {
		if(!isConnected()) 
			throw new NotConnectedException ("Not connected!");
		
		PendingRequest pendingResponse = null;
		synchronized (serviceLock) {
			if(pendingQuit) 
				throw new ClientRuntimeException("Pipeline shutting down: Quit in progess; no further requests are accepted.");
			
			Request request = Assert.notNull(protocol.createRequest (cmd, args), "request object from handler", ProviderException.class);
			
			if(cmd != Command.QUIT)
				request.write(getOutputStream());
			else {
				pendingQuit = true;
				isActive.set(false);
//				heartbeat.exit();
			}
				
			pendingResponse = new PendingRequest(request, cmd);
			pendingResponseQueue.add(pendingResponse);
		}
		return pendingResponse;
    }

	// ------------------------------------------------------------------------
	// Inner Class
	// ------------------------------------------------------------------------
    /**
     * Provides the response processing logic as a {@link Runnable}.
     * 
     * @author  Joubin Houshyar (alphazero@sensesay.net)
     * @version alpha.0, Oct 18, 2009
     * @since   alpha.0
     * 
     */
    public final class ResponseHandler implements Runnable {

    	/**
    	 * Keeps processing the {@link PendingRequest}s in the pending {@link Queue}
		 * until a QUIT is encountered in the pending queue.  Thread will stop after
		 * processing the QUIT response (which is expected to be a {@link VirtualResponse}.
    	 * <p>
    	 * TODO: not entirely clear what is the best way to handle exceptions.
    	 * <p>
    	 * TODO: socket Reconnect in the context of pipelining is non-trivial, and maybe
    	 * not even practically possible.  (e.g. request n is sent but pipe breaks on
    	 * some m (m!=n) response.  non trivial.
    	 */
//        @Override
        public void run () {
			Log.log("Pipeline thread <%s> started.", Thread.currentThread().getName());
        	PendingRequest pending = null;
        	while(true){
        		Response response = null;
				try {
	                pending = pendingResponseQueue.take();
					try {
						response = protocol.createResponse(pending.cmd);
						response.read(getInputStream());
						pending.response = response;
						pending.completion.signal();
						if(response.getStatus().isError()) {
							Log.error ("(Asynch) Error response for " + pending.cmd.code + " => " + response.getStatus().message());
						}

					}
					catch (ProviderException bug){
						Log.error ("ProviderException: " + bug.getLocalizedMessage());
						bug.printStackTrace();
						pending.setCRE(bug);
					}
					catch (ClientRuntimeException cre) {
						Log.error ("ClientRuntimeException: " + cre.getLocalizedMessage());
//						cre.printStackTrace();
						pending.setCRE(cre);
					}
					catch (RuntimeException e){
						Log.error ("Unexpected (and not handled) RuntimeException: " + e.getLocalizedMessage());
						e.printStackTrace();
						pending.setCRE(new ProviderException("Unexpected runtime exception in response handler"));
						pending.setResponse(null);
						break;
					}
					
					// redis (1.00) simply shutsdown connection even if pending responses
					// are expected, so quit is NOT sent.  we simply close connection on this
					// end. 
					if(pending.cmd == Command.QUIT) {
						PipelineConnectionBase.this.disconnect();
						break;
					}
                }
                catch (InterruptedException e1) {
	                e1.printStackTrace();
                }
        	}
			Log.log("Pipeline thread <%s> stopped.", Thread.currentThread().getName());
        }
    }
}
