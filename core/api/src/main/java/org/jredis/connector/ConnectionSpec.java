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

package org.jredis.connector;

import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;


/**
 * Connection specification for getting (creating) {@link Connection} objects.
 * @author joubin (alphazero@sensesay.net)
 *
 * @see ConnectionFactory#getConnection(ConnectionSpec)
 * @see java.resource.cci.ConnectionSpec
 * 
 */
public interface ConnectionSpec {
	
	public InetAddress getAddress();
	public int getPort ();
	public boolean getSocketFlag (ConnectionSpec.SocketFlag flag);
	public Integer getSocketProperty (ConnectionSpec.SocketProperty property);

	/**
	 * Flag keys for SocketFlag settings of the connection specification.
	 * @see ConnectionSpec#getSocketFlag(SocketFlag)
	 * @author Joubin Houshyar (alphazero@sensesay.net)
	 *
	 */
	public enum SocketFlag {
		SO_KEEP_ALIVE
	}
	
	/**
	 * Property keys for SocketProperty settings of the connection specification.
	 * @see ConnectionSpec#getSocketProperty(SocketProperty)
	 * @author Joubin Houshyar (alphazero@sensesay.net)
	 *
	 */
	public enum SocketProperty {
		/** 
		 * Corresponds to <code><b>SO_SNDBUF</b></code> flag. see {@link Socket#setSendBufferSize(int)} 
		 * <p>expected value is an <b><code>int</code></b> or an {@link Integer}.
		 */
		send_buffer_size,
		
		/** 
		 * corresponds to SO_RCVBUF flag. see {@link Socket#setReceiveBufferSize(int)} 
		 */
		receive_buffer_size,
		
		/** 
		 * corresponds to SO_TIMEOUT flag.  see {@link Socket#setSoTimeout(int)} 
		 */
		timeout,
		
		/**
		 * Socket performance preferences.
		 * <p> This property will be used in conjunction with other associated properties.
		 * @See {@link SocketProperty#latency}
		 * @See {@link SocketProperty#bandwidth}
		 * @See {@link Socket#setPerformancePreferences(int, int, int)} for details.
		 */
		connection_time,

		/**
		 * Socket performance preferences.
		 * <p> This property will be used in conjunction with other associated properties.
		 * @See {@link SocketProperty#bandwidth}
		 * @See {@link SocketProperty#connection_time}
		 * @See {@link Socket#setPerformancePreferences(int, int, int)} for details.
		 */
		latency,

		/**
		 * Socket performance preferences.
		 * <p> This property will be used in conjunction with other associated properties.
		 * @See {@link SocketProperty#latency}
		 * @See {@link SocketProperty#connection_time}
		 * @See {@link Socket#setPerformancePreferences(int, int, int)} for details.
		 */
		bandwidth,
	}

	/**
	 * @see SocketFlag
	 * @see ConnectionSpec#getSocketFlag(SocketFlag)
	 * 
	 * @return the socket flag map.  Whether this is a copy or the reference to underlying references
	 * is un-Specified.  What is specified is that changes to this set after connection has been established are
	 * of no effect.
	 */
	public Map<SocketFlag, Boolean> getSocketFlags();
	
	/**
	 * @see SocketProperty
	 * @see ConnectionSpec#getSocketProperty(SocketProperty)
	 * 
	 * @return the socket property map.  Whether this is a copy or the reference to underlying references
	 * is un-Specified.  What is specified is that changes to this set after connection has been established are
	 * of no effect.
	 */
	public Map<SocketProperty, Integer> getSocketProperties();
	
}