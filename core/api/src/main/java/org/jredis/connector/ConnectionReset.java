/*
 *   Copyright 2009-2011 Joubin Houshyar
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

package org.jredis.connector;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import org.jredis._specification;
import org.jredis.protocol.Command;

/**
 * This exception is thrown by a {@link Connection} to indicate that the connection to redis was
 * reset (typically due to timeout on Redis side).  
 * <p>
 * If the {@link Connection} is specified to use
 * auto-reconnect, the connection has been re-established, but, the status of the request that
 * gave rise to this exception is indeterminate, in the sense that while this exception certainly indicates
 * that the response was not received from Redis, it is not clear whether the request itself was
 * received by the server.  This is due to the fact that writing to the {@link OutputStream} of a {@link Socket}
 * that has been closed by the remote peer does NOT raise an exception.  The exception is raised (reliably) only
 * on the receive when reading from the {@link Socket}'s {@link InputStream}.
 * <p>
 * Depending on the application domain and the {@link Command} of the request, the user may retry 
 * the request. 
 * 
 * @author  Joubin (alphazero@sensesay.net)
 * @version alpha.0, Apr 15, 2009
 * @since   alpha.0
 * 
 */

public class ConnectionReset extends ConnectionException {

	/**  */
	private static final long serialVersionUID = _specification.Version.major;
	
	private static final String info = "potential redis timeout";
	/**
	 * @param string
	 * @param e
	 */
	public ConnectionReset(String msg, SocketException e) {
		super(String.format("(%s) %s", info, msg), e);
	}
	public ConnectionReset(String msg) {
		super(String.format("(%s) %s", info, msg));
	}
}
