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
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
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
import org.jredis.ri.alphazero.support.Assert;
import org.jredis.ri.alphazero.support.FastBufferedInputStream;

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
	private ResponseHandler	    	respHandler;
	private Thread 					respHandlerThread;
	private BlockingQueue<PendingRequest>	pendingResponseQueue;

	private Object					serviceLock = new Object();
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
    protected void initializeComponents () {
    	super.initializeComponents();
    	
    	serviceLock = new Object();
    	
    	pendingResponseQueue = new LinkedBlockingQueue<PendingRequest>();
    	respHandler = new ResponseHandler();
    	respHandlerThread = new Thread(respHandler, "response-handler");
    	respHandlerThread.start();
    }
    
   
    /**
     * Pipeline must use a concurrent protocol handler.
     *  
     * @see org.jredis.ri.alphazero.connection.ConnectionBase#newProtocolHandler()
     */
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
    @Override
    public final Future<Response> queueRequest (Command cmd, byte[]... args) 
    	throws ClientRuntimeException, ProviderException 
    {
		if(!isConnected()) throw new NotConnectedException ("Not connected!");
   
		PendingRequest pendingResponse = null;
		synchronized (serviceLock) {
			Request request = Assert.notNull(protocol.createRequest (cmd, args), "request object from handler", ProviderException.class);
			request.write(getOutputStream());
			pendingResponse = new PendingRequest(request, cmd);
			pendingResponseQueue.add(pendingResponse);
		}
		return pendingResponse;
    }

	// ------------------------------------------------------------------------
	// Inner Class
	// ------------------------------------------------------------------------
    
    public final class ResponseHandler implements Runnable {
    	private boolean keepWorking = true;
    	void stop () {
    		// TODO: 
    	}
    	/**
    	 * Keeps processing the {@link PendingRequest}s in the pending {@link Queue}
    	 * while {@link ResponseHandler#keepWorking} is <code>true</code>.
    	 * <p>
    	 * TODO: not entirely clear what is the best way to handle exceptions.
    	 * <p>
    	 * TODO: socket Reconnect in the context of pipelining is non-trivial, and maybe
    	 * not even practically possible.  (e.g. request n is sent but pipe breaks on
    	 * some m (m!=n) response.  non trivial.
    	 */
//        @Override
        public void run () {
        	PendingRequest pending = null;
        	while(keepWorking){
        		Response response = null;
				try {
	                pending = pendingResponseQueue.take();
					try {
						response = protocol.createResponse(pending.cmd);
						response.read(getInputStream());
						pending.response = response;
						pending.completion.signal();
					}
					catch (ProviderException bug){
						bug.printStackTrace();
						pending.setCRE(bug);
					}
					catch (ClientRuntimeException cre) {
						cre.printStackTrace();
						pending.setCRE(cre);
					}
					catch (RuntimeException e){
						e.printStackTrace();
						System.err.format("BUG -- unexpected RuntimeException '%s' (not handled) -- response handler will stop!", e.getLocalizedMessage());
						pending.setCRE(new ProviderException("Unexpected runtime exception in response handler"));
						pending.setResponse(null);
						break;
					}
                }
                catch (InterruptedException e1) {
	                e1.printStackTrace();
                }
        	}
        }
    }
}
