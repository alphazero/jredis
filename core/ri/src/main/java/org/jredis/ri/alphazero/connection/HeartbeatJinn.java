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

import java.util.concurrent.atomic.AtomicBoolean;
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.connector.Connection.Modality;
import org.jredis.protocol.Command;
import org.jredis.ri.alphazero.support.Log;

/**
 * A demon thread tasked with PINGing the associated connection
 * per its {@link ConnectionSpec#getHeartbeat()} heartbeat interval.
 * 
 * The connection must only call {@link Thread#start()} when it has
 * established its connectivity.
 * 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Nov 22, 2009
 * @since   alpha.0
 * 
 */
public class HeartbeatJinn extends Thread{

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
	 * @param conn
	 * @param periodInSecs
	 * @param name
	 */
	public HeartbeatJinn (Connection conn, int periodInSecs, String name) {
		super (name);
		setDaemon(true);
		this.conn = conn;
		this.modality = conn.getModality();
		this.period = periodInSecs * 1000;
		this.connected = new AtomicBoolean(false);
		this.mustBeat = new AtomicBoolean(true);
	}
	public void notifyDisconnected () {
		connected.set(false);
	}
	public void notifyConnected () {
		connected.set(true);
	}
	// TODO: no one is calling this method (yet) and we're relying on daemon status to
	// avoid the jvm hanging around but this ain't right.
	public void exit() {
		mustBeat.set(false);
	}
	public void run () {
//		Log.log("HeartbeatJinn thread <%s> started.", getName());
		while (mustBeat.get()) {
			try {
				if(connected.get()){  // << buggy.
					try {
						switch (modality){
						case Asynchronous:
							conn.queueRequest(Command.PING);
							break;
						case Synchronous:
							conn.serviceRequest(Command.PING);
							break;
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
							connected.set(false);
						}
					}
				}
				sleep (period);	// sleep regardless - 
			}
			catch (InterruptedException e) { break; }
		}
		Log.log("HeartbeatJinn thread <%s> stopped.", Thread.currentThread().getName());
	}
	@Override
	public void interrupt () {
		super.interrupt();
	}
}
