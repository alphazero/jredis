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

package org.jredis.connector;

import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


/**
 * ConnectionSpec specifies the parameters used in the creation and
 * runtime operation of JRedis connections.
 * 
 * @author joubin (alphazero@sensesay.net)
 *
 * @see ConnectionFactory#getConnection(ConnectionSpec)
 * @see java.resource.cci.ConnectionSpec
 * 
 */
public interface ConnectionSpec {
	/**
	 * @return
	 */
	public InetAddress getAddress();
	/**
	 * @param address
     * @return the {@link ConnectionSpec}
	 */
	public ConnectionSpec setAddress(InetAddress address);
	/**
	 * @return the port number for the connection
	 */
	public int getPort ();
	/**
	 * @param port
     * @return the {@link ConnectionSpec}
	 */
	public ConnectionSpec setPort(int port);
	/**
	 * @return the password (if any) for the connection.  Used on (re-)connect to authenticate 
	 * the client after connectivity has been established
	 * @Redis AUTH  
	 * @see Command#AUTH
	 */
	public byte[] getCredentials();
	/**
	 * @param credentials
     * @return the {@link ConnectionSpec}
	 */
	public ConnectionSpec setCredentials(byte[] credentials);
	/**
	 * @return the database selected for the connection.  Used on (re-)connect to select the db 
	 * after network connectivity has been established.
	 * @Redis SELECT
	 * @see Command#SELECT
	 */
	public int getDatabase ();
	/**
	 * @param database
     * @return the {@link ConnectionSpec}
	 */
	public ConnectionSpec setDatabase(int database);
	/**
	 * Get the {@link SocketFlag} for the {@link ConnectionSpec}
	 * @param flag
	 * @return the specified TCP socket flag used for the connection.
	 * @see SocketFlag
	 */
	public boolean getSocketFlag (ConnectionSpec.SocketFlag flag);
	/**
	 * Set the {@link SocketFlag} for the {@link ConnectionSpec}
	 * @param flag
	 * @param value
	 * @return {@link ConnectionSpec} this
	 */
	public ConnectionSpec setSocketFlag(ConnectionSpec.SocketFlag flag, Boolean value);
	/**
	 * Get the {@link SocketProperty} for the {@link ConnectionSpec}
	 * @param property
	 * @return the specified socket property used for the connection.
	 * @see SocketFlag
	 */
	public Integer getSocketProperty (ConnectionSpec.SocketProperty property);
	/**
	 * Set the {@link SocketProperty} for the {@link ConnectionSpec}
	 * @param property
	 * @param value
	 * @return the previous value (if any).  Null if none existed, per {@link Map#put(Object, Object)} semantics.
	 */
	public ConnectionSpec setSocketProperty(ConnectionSpec.SocketProperty property, Integer value);
 	/**
	 * @param flag
	 * @return the {@link ConnectionFlag}
	 * @see SocketFlag
	 */
	public boolean getConnectionFlag (ConnectionSpec.ConnectionFlag flag);
	/**
	 * Sets the specified {@link ConnectionFlag}
	 * @param flag
	 * @param value
	 * @return the referenced {@link ConnectionSpec}
	 */
	public ConnectionSpec setConnectionFlag(ConnectionSpec.ConnectionFlag flag, Boolean value);
	/**
	 * @return
	 */
	public int	getReconnectCnt ();
	/**
	 * @param cnt
     * @return the {@link ConnectionSpec}
	 */
	public ConnectionSpec setReconnectCnt(int cnt);
    /**
     * @return the heartbeat period in seconds
     */
    public int	getHeartbeat();
    /**
     * @param seconds heartbeat period
     * @return the {@link ConnectionSpec}
     */
    public ConnectionSpec setHeartbeat(int seconds);

	// ------------------------------------------------------------------------
	// Associated (inner) types
	// ------------------------------------------------------------------------
    public enum ConnectionFlag {
    	/**  */
    	CONNECT_IMMEDIATELY,
    	/**  */
    	TRANSPARENT_RECONNECT,
    	/**  */
    	RETRY_AFTER_RESET, 
    	/**  */
    	PIPELINE,
    	/**  */
    	SHARED,
    	/**  */
    	RELIABLE,
    	/**  */
    	TRACE
    }

	/**
	 * Flag keys for SocketFlag settings of the connection specification.
	 * @see ConnectionSpec#getSocketFlag(SocketFlag)
	 * @author Joubin Houshyar (alphazero@sensesay.net)
	 *
	 */
	public enum SocketFlag {
		/** Corresponds to SO_KEEP_ALIVE flag of {@link Socket}.  @see {@link Socket#setSoTimeout(int)} */
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
		SO_SNDBUF,
		
		/** 
		 * corresponds to SO_RCVBUF flag. see {@link Socket#setReceiveBufferSize(int)} 
		 */
		SO_RCVBUF,
		
		/** 
		 * corresponds to SO_TIMEOUT flag.  see {@link Socket#setSoTimeout(int)} 
		 */
		SO_TIMEOUT,
		
		/**
		 * Socket performance preferences.
		 * <p> This property will be used in conjunction with other associated properties.
		 * @See {@link SocketProperty#latency}
		 * @See {@link SocketProperty#bandwidth}
		 * @See {@link Socket#setPerformancePreferences(int, int, int)} for details.
		 */
		SO_PREF_CONN_TIME,

		/**
		 * Socket performance preferences.
		 * <p> This property will be used in conjunction with other associated properties.
		 * @See {@link SocketProperty#bandwidth}
		 * @See {@link SocketProperty#connection_time}
		 * @See {@link Socket#setPerformancePreferences(int, int, int)} for details.
		 */
		SO_PREF_LATENCY,

		/**
		 * Socket performance preferences.
		 * <p> This property will be used in conjunction with other associated properties.
		 * @See {@link SocketProperty#latency}
		 * @See {@link SocketProperty#connection_time}
		 * @See {@link Socket#setPerformancePreferences(int, int, int)} for details.
		 */
		SO_PREF_BANDWIDTH,
	}

	// ------------------------------------------------------------------------
	// Reference Implementation 
	// ------------------------------------------------------------------------

	/**
	 * Reference implementation of {@link ConnectionSpec}.
	 * <p>
	 * This implementation is a read/write implementation providing no default
	 * values of any kind.  It can be used as is and initialized as required,
	 * or (as it is intended) it will provide support for various connection
	 * profiles (e.g. server connectors), etc.
	 *
	 * @author  joubin (alphazero@sensesay.net)
	 * @version alpha.0, Aug 23, 2009
	 * @since   alpha.0
	 * 
	 */
	public static class RefImpl implements ConnectionSpec {

		// ------------------------------------------------------------------------
		// Attrs
		// ------------------------------------------------------------------------
		
		/** redis server address */
		InetAddress  	address;
		
        /** redis server port */
		int			port;

		/** authorization password */
		byte[]		credentials;

		/** selected database */
		int			database;
		
		/** retry count for reconnects */
		int 	reconnectCnt;
		
		/** {@link Map} of the {@link SocketFlag}s of the {@link ConnectionSpec} */
		Map<SocketFlag, Boolean> socketFlags = new HashMap<SocketFlag, Boolean>();
		
		/** {@link Map} of the {@link SocketProperty}s of the {@link ConnectionSpec} */
		Map<SocketProperty, Integer> socketProperties = new HashMap<SocketProperty, Integer>();
		
		/** {@link Map} of the {@link ConnectionFlag}s of the {@link ConnectionSpec} */
		Map<ConnectionFlag, Boolean> connectionFlags = new HashMap<ConnectionFlag, Boolean>();
		
		/** heartbeat period in milliseconds */
		private int heartbeat;
		
		// ------------------------------------------------------------------------
		// Constructor(s)
		// ------------------------------------------------------------------------
		
		// ------------------------------------------------------------------------
		// Interface
		// ------------------------------------------------------------------------
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getAddress() */
//		@Override
		final public InetAddress getAddress () {
			return address;
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getCredentials() */
//		@Override
		final public byte[] getCredentials () {
			return credentials;
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getDatabase() */
//		@Override
		final public int getDatabase () {
			return database;
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getPort() */
//		@Override
		final public int getPort () {
			return port;
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getReconnectCnt() */
//		@Override
		final public int getReconnectCnt () {
			return this.reconnectCnt;
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getSocketFlag(org.jredis.connector.ConnectionSpec.SocketFlag) */
//		@Override
		final public boolean getSocketFlag (SocketFlag flag) {
			return socketFlags.get(flag);
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getSocketProperty(org.jredis.connector.ConnectionSpec.SocketProperty) */
//		@Override
		final public Integer getSocketProperty (SocketProperty property) {
			return socketProperties.get(property);
		}
		// ------------------------------------------------------------------------
		// Property Setters
		// ------------------------------------------------------------------------
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setAddress(java.net.InetAddress) */
		final public ConnectionSpec setAddress (InetAddress address) {
        	this.address = address;
        	return this;
        }
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setPort(int) */
//		@Override
		final public ConnectionSpec setPort (int port) {
        	this.port = port;
        	return this;
        }
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setCredentials(byte[]) */
//      @Override
		final public ConnectionSpec setCredentials (byte[] credentials) {
        	this.credentials = credentials;
        	return this;
        }
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setDatabase(int) */
//      @Override
		final public ConnectionSpec setDatabase (int database) {
        	this.database = database;
        	return this;
        }
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setReconnectCnt(int) */
//      @Override
		final public ConnectionSpec setReconnectCnt (int reconnectCnt) {
        	this.reconnectCnt = reconnectCnt;
        	return this;
        }
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setSocketFlag(org.jredis.connector.ConnectionSpec.SocketFlag, java.lang.Boolean) */
//      @Override
		final public ConnectionSpec setSocketFlag(SocketFlag flag, Boolean value){
			socketFlags.put(flag, value);
			return this;
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setSocketProperty(org.jredis.connector.ConnectionSpec.SocketProperty, java.lang.Integer) */
//      @Override
		final public ConnectionSpec setSocketProperty(SocketProperty property, Integer value){
			socketProperties.put(property, value);
			return this;
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getConnectionFlag(org.jredis.connector.ConnectionSpec.ConnectionFlag) */
//      @Override
		final public boolean getConnectionFlag (ConnectionSpec.ConnectionFlag flag){
			return connectionFlags.get(flag);
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setConnectionFlag(org.jredis.connector.ConnectionSpec.ConnectionFlag, java.lang.Boolean) */
//      @Override
		final public ConnectionSpec setConnectionFlag(ConnectionSpec.ConnectionFlag flag, Boolean value){
			connectionFlags.put(flag, value);
			return this;
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getHeartbeat() */
//      @Override
		final public int	getHeartbeat() {
        	return heartbeat/1000;
        }
 		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setHeartbeat(int) */
//      @Override
		final public ConnectionSpec setHeartbeat(int seconds) {
        	this.heartbeat = seconds * 1000;
        	return this;
        }
	}
}