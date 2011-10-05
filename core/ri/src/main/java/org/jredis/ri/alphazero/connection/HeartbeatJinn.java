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

import java.security.ProviderException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jredis.ClientRuntimeException;
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.connector.Connection.Event;
import org.jredis.connector.Connection.Modality;
import org.jredis.protocol.Command;
import org.jredis.protocol.Response;
import org.jredis.ri.alphazero.support.Log;

/**
 * A demon thread tasked with PINGing the associated connection
 * per its {@link ConnectionSpec#getHeartbeat()} heartbeat interval.
 * <p>
 * {@link HeartbeatJinn}s are {@link Connection.Listener}s, and rely
 * on {@link Connection} event propagation to synchronize their activity
 * with the associated connection.
 * <p>
 * The connection must only call {@link Thread#start()} when it has
 * established its connectivity.
 * 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Nov 22, 2009
 * @since   alpha.0
 * 
 */
public class HeartbeatJinn extends Thread implements Connection.Listener{

	/**  */
	AtomicBoolean connected;
	/**  */
	AtomicBoolean mustBeat;
	/**  */
	private final Modality modality;
	/**  */
	private final Connection conn;
	/**  */
	private final int period;
	
	/**
	 * Instantiate and initialize the HeartbeatJinn.  On return, this instance
	 * is:
	 * <li> sets flag to work
	 * <li> added to the listeners for the connection
	 * <li> assumes connection is not yet established
	 * 
	 * @param conn associated with this instnace
	 * @param periodInSecs a reasonable value is 1.  Internally converted to millisecs.
	 * @param name associated with this (heartbeat) thread.
	 */
	public HeartbeatJinn (Connection conn, int periodInSecs, String name) {
		super (name);
		setDaemon(true);
		this.conn = conn;
		conn.addListener(this);
		this.modality = conn.getSpec().getModality();
		this.period = periodInSecs * 1000;
		this.connected = new AtomicBoolean(false);
		this.mustBeat = new AtomicBoolean(true);
	}

	/**
	 * 
	 */
	public void shutdown() {
		mustBeat.set(false);
		this.interrupt();
	}
	
	// ------------------------------------------------------------------------
	// INTERFACE
	/* ====================================================== Thread (Runnable)
	 * 
	 */
	// ------------------------------------------------------------------------
	
	/**
	 * Your basic infinite loop with branchings on connection state and modality
	 * <p>
	 * TODO: run loop should be a proper state machine.
	 * TODO: delouse this baby ..
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run () {
//		if (conn.getSpec().getLogLevel()==ConnectionSpec.LogLevel.DEBUG)
		Log.debug("HeartbeatJinn thread <%s> started.", Thread.currentThread().getName());
		
		while (mustBeat.get()) {
			try {
				if(connected.get()){  // << buggy: quit needs to propagate down here.
					Response response = null;
					try {
						switch (modality){
						case Asynchronous:
							Future<Response> fResponse = conn.queueRequest(Command.PING);
							response = fResponse.get();
							break;
						case Synchronous:
							response = conn.serviceRequest(Command.PING);
							break;
						case Monitor:
						case PubSub:
							throw new ProviderException(String.format("%s connector not supported", modality.name()));
						}
						if(!response.isError()){ 
//							if(conn.getSpec().getLogLevel().equals(LogLevel.DEBUG))
							Log.debug (String.format("<%s> is alive", conn)); 
						}
						else {
							String errmsg = String.format("Error response on PING: %s", response.getStatus().toString());
							Log.error(errmsg);
							throw new ClientRuntimeException(errmsg);  // NOTE: can't be sure this is a protocol BUG .. so CRE instead
						}
					}
					catch (Exception e) {
						// addressing buggy above.  notifyDisconnected gets called after we have checked it but before we
						// made the call - it is disconnected by the time the call is made and we end up here
						// checking the flag again and if it is indeed not the above scenario then there is something wrong,
						// otherwise ignore it and basically loop on sleep until we get notify on connect again (if ever).
						if(connected.get()){
							// how now brown cow?  we'll log it for now and assume reconnect try in progress and wait for the flag change.
							Log.problem("HeartbeatJinn thread <" + Thread.currentThread().getName() + "> encountered exception on PING: " + e.getMessage() );
//							connected.set(false);
						}
					}

				}
//				Log.debug("Looping : <%s>", conn);
				sleep (period);	// sleep regardless - 
			}
			catch (InterruptedException e) { 
//				if (conn.getSpec().getLogLevel()==ConnectionSpec.LogLevel.DEBUG)
				Log.debug ("HeartbeatJinn thread <%s> interrupted.", Thread.currentThread().getName());
				break; 
			}
		}
		Log.log("HeartbeatJinn thread <%s> stopped.", Thread.currentThread().getName());
	}

	// ------------------------------------------------------------------------
	// INTERFACE
	/* =================================================== Connection.Listener
	 * 
	 * hooks for integrating the heartbeat thread's state with the associated
	 * connection's state through event callbacks. 
	 */
	// ------------------------------------------------------------------------
	/**
	 * 
     * @see org.jredis.connector.Connection.Listener#onEvent(org.jredis.connector.Connection.Event)
     */
	// TODO: let's hook this up.
    public void onEvent (Event event) {
//		if (conn.getSpec().getLogLevel()==ConnectionSpec.LogLevel.DEBUG)
		Log.debug("onEvent %s : <%s>", event.getType().name(), this);
		
    	switch (event.getType()){
			case CONNECTING:
				break;
			case CONNECTED:
				connected.set(true);
				break;
			case DISCONNECTING:
			case DISCONNECTED:
				connected.set(false);
				break;
			case FAULTED:
//				shutdown();  // REVU: this is wrong.
				break;
			case SHUTDOWN:
				shutdown();
				break;
//			case STOPPING:
//				break;
    	}
    }
}
