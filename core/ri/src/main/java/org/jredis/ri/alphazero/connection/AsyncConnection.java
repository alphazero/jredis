/*
 *   Copyright 2009-2010 Joubin Houshyar
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
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.connector.NotConnectedException;
import org.jredis.protocol.Command;
import org.jredis.protocol.Protocol;
import org.jredis.protocol.Request;
import org.jredis.protocol.Response;
import org.jredis.ri.alphazero.protocol.ConcurrentSyncProtocol;
import org.jredis.ri.alphazero.protocol.VirtualResponse;
import org.jredis.ri.alphazero.support.Assert;
import org.jredis.ri.alphazero.support.FastBufferedInputStream;
import org.jredis.ri.alphazero.support.Log;

/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Nov 6, 2009
 * @since   alpha.0
 * 
 */

public class AsyncConnection extends ConnectionBase implements Connection {
	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	
	private RequestProcessor	   processor;
	
	/**  */
	private Thread 					processerThread;

	/**  */
	private BlockingQueue<PendingRequest>	pendingQueue;
	BlockingQueue<PendingRequest> getPendingQueue() { return pendingQueue; }
	
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------
	public AsyncConnection (
			ConnectionSpec connectionSpec
		)
		throws ClientRuntimeException, ProviderException 
	{
		super (connectionSpec.setModality(Modality.Asynchronous));
//		super (connectionSpec); // should be - really need an assert
	}

	// ------------------------------------------------------------------------
	// Extension
	// ------------------------------------------------------------------------
	/**
     * 
     */
    protected void initializeComponents () {
    	super.initializeComponents();
    	
//    	serviceLock = new Object();
    	
    	pendingQueue = new LinkedBlockingQueue<PendingRequest>();
    	processor = new RequestProcessor();
    	processerThread = new Thread(processor, "request-processor");
    	processerThread.start();
    }
    /**
     * Pipeline must use a concurrent protocol handler.
     *  
     * @see org.jredis.ri.alphazero.connection.ConnectionBase#newProtocolHandler()
     */
    @Override
    protected Protocol newProtocolHandler () {
		return new ConcurrentSyncProtocol();
    }
    
    /**
     * Just make sure its a {@link FastBufferedInputStream}.
     */
    @SuppressWarnings("boxing")
	@Override
	protected final InputStream newInputStream (InputStream socketInputStream) throws IllegalArgumentException {
    	
    	InputStream in = super.newInputStream(socketInputStream);
    	if(!(in instanceof FastBufferedInputStream)){
    		Log.log(String.format("WARN: input was: %s\n", in.getClass().getCanonicalName()));
    		in = new FastBufferedInputStream (in, spec.getSocketProperty(Connection.Socket.Property.SO_RCVBUF));
    	}
    	return in;
    }
    
	// ------------------------------------------------------------------------
	// Interface
	// ======================================================= ProtocolHandler
	// ------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see org.jredis.connector.Connection#getModality()
	 */
	public final Modality getModality() {
		return Connection.Modality.Asynchronous;
	}
	
	/* (non-Javadoc)
     * @see org.jredis.ri.alphazero.connection.ConnectionBase#queueRequest(org.jredis.protocol.Command, byte[][])
     */
    @Override
    public Future<Response> queueRequest (Command cmd, byte[]... args)
    	throws ClientRuntimeException, ProviderException 
    {
		if(!isConnected()) 
			throw new NotConnectedException ("Not connected!");
		
		PendingRequest pending = new PendingRequest(cmd, args);
		pendingQueue.add(pending);
		return pending;
    }
    
	// ------------------------------------------------------------------------
	// Inner Class
	// ------------------------------------------------------------------------
    public final class RequestProcessor implements Runnable {

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

        public void run () {
			Log.log("AsyncConnection processor thread <%s> started.", Thread.currentThread().getName());
        	/** Response handler thread specific protocol handler -- optimize fencing */
        	Protocol protocol = Assert.notNull (newProtocolHandler(), "the delegate protocol handler", ClientRuntimeException.class);
        	PendingRequest pending = null;
        	final BlockingQueue<PendingRequest>	_pendingQueue = getPendingQueue();
        	while(true){
				try {
	                pending = _pendingQueue.take();
					try {
						Request request = Assert.notNull(protocol.createRequest (pending.cmd, pending.args), "request object from handler", ProviderException.class);
						request.write(getOutputStream());
						
						pending.response = protocol.createResponse(pending.cmd);
						pending.response.read(getInputStream());
						
						pending.completion.signal();
						if(pending.response.getStatus().isError()) {
							Log.error ("(Asynch) Error response for " + pending.cmd.code + " => " + pending.response.getStatus().message());
						}

					}
					catch (ProviderException bug){
						Log.error ("ProviderException: " + bug.getLocalizedMessage());
						bug.printStackTrace();
						pending.setCRE(bug);
					}
					catch (ClientRuntimeException cre) {
						Log.error ("ClientRuntimeException: " + cre.getLocalizedMessage());
						cre.printStackTrace();
						pending.setCRE(cre);
					}
					catch (RuntimeException e){
						Log.error("Unexpected RuntimeException ", e);
						e.printStackTrace();
						pending.setCRE(new ProviderException("Unexpected runtime exception in response handler"));
						pending.setResponse(null);
						break;
					}
					
					// redis (1.00) simply shutsdown connection even if pending responses
					// are expected, so quit is NOT sent.  we simply close connection on this
					// end. 
					if(pending.cmd == Command.QUIT) {
						AsyncConnection.this.disconnect();
						break;
					}
                }
                catch (InterruptedException e1) {
	                e1.printStackTrace();
                }
        	}
			Log.log("AsyncConnection processor thread <%s> stopped.", Thread.currentThread().getName());
        }
    }
}
