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
import java.util.HashMap;
import java.util.Map;
import org.jredis.protocol.Command;


/**
 * Connection specification for getting (creating) {@link Connection} objects.
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
	 * @return
	 */
	public ConnectionSpec setAddress(InetAddress address);
	/**
	 * @return the port number for the connection
	 */
	public int getPort ();
	/**
	 * @param port
	 * @return
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
	 * @return
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
	 * @return
	 */
	public ConnectionSpec setDatabase(int database);
	/**
	 * @param flag
	 * @return the specified TCP socket flag used for the connection.
	 * @see SocketFlag
	 */
	public boolean getSocketFlag (ConnectionSpec.SocketFlag flag);
	/**
	 * @param flag
	 * @param value
	 * @return
	 */
	public ConnectionSpec setSocketFlag(ConnectionSpec.SocketFlag flag, Boolean value);
	/**
	 * @param property
	 * @return the specified socket property used for the connection.
	 * @see SocketFlag
	 */
	public Integer getSocketProperty (ConnectionSpec.SocketProperty property);
	/**
	 * @param property
	 * @param value
	 * @return
	 */
	public ConnectionSpec setSocketProperty(ConnectionSpec.SocketProperty property, Integer value);

	/**
	 * @return
	 */
	public int	getReconnectCnt ();
	/**
	 * @param cnt
	 * @return
	 */
	public ConnectionSpec setReconnectCnt(int cnt);

	/**
     * @return
     */
    public boolean isReliable ();

	/**
     * @return
     */
    public void isReliable (boolean flag);
    
    /**
     * @return
     */
    public boolean isShared ();
    
    /**
     * @return the heartbeat period in seconds
     */
    public int	getHeartbeat();
    
    /**
     * @param seconds heartbeat period
     */
    public void setHeartbeat(int seconds);
    /**
     * @param flag
     */
    public void isShared(boolean flag);

    public boolean isPipeline();
    
    public void isPipeline(boolean flag);
    
	// ------------------------------------------------------------------------
	// Associated (inner) types
	// ------------------------------------------------------------------------
	
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
// 2much - keep it simple: the getters above are sufficient - this serves not additional purpose.
//	/**
//	 * @see SocketFlag
//	 * @see ConnectionSpec#getSocketFlag(SocketFlag)
//	 * 
//	 * @return the socket flag map.  Whether this is a copy or the reference to underlying references
//	 * is un-Specified.  What is specified is that changes to this set after connection has been established are
//	 * of no effect.
//	 */
//	public Map<SocketFlag, Boolean> getSocketFlags();
//	
//	/**
//	 * @see SocketProperty
//	 * @see ConnectionSpec#getSocketProperty(SocketProperty)
//	 * 
//	 * @return the socket property map.  Whether this is a copy or the reference to underlying references
//	 * is un-Specified.  What is specified is that changes to this set after connection has been established are
//	 * of no effect.
//	 */
//	public Map<SocketProperty, Integer> getSocketProperties();
	// ------------------------------------------------------------------------
	// Reference Implementation 
	// ------------------------------------------------------------------------

	/**
	 * [TODO: document me!]
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
		
		/**  */
		private boolean isReliable;
		
		/** */
		private boolean isShared;
		
		/**  */
		private boolean isPipeline;
		
		/** heartbeat period in milliseconds */
		private int heartbeat;
		// ------------------------------------------------------------------------
		// Constructor(s)
		// ------------------------------------------------------------------------
		
		// ------------------------------------------------------------------------
		// Interface
		// ------------------------------------------------------------------------
//		@Override
		public InetAddress getAddress () {
			return address;
		}

		/* (non-Javadoc)
		 * @see org.jredis.connector.ConnectionSpec#getCredentials()
		 */
//		@Override
		public byte[] getCredentials () {
			return credentials;
		}

		/* (non-Javadoc)
		 * @see org.jredis.connector.ConnectionSpec#getDatabase()
		 */
//		@Override
		public int getDatabase () {
			return database;
		}

		/* (non-Javadoc)
		 * @see org.jredis.connector.ConnectionSpec#getPort()
		 */
//		@Override
		public int getPort () {
			return port;
		}

		/* (non-Javadoc)
		 * @see org.jredis.connector.ConnectionSpec#getReconnectCnt()
		 */
//		@Override
		public int getReconnectCnt () {
			return this.reconnectCnt;
		}

		/* (non-Javadoc)
		 * @see org.jredis.connector.ConnectionSpec#getSocketFlag(org.jredis.connector.ConnectionSpec.SocketFlag)
		 */
//		@Override
		public boolean getSocketFlag (SocketFlag flag) {
			return socketFlags.get(flag);
		}

		/* (non-Javadoc)
		 * @see org.jredis.connector.ConnectionSpec#getSocketProperty(org.jredis.connector.ConnectionSpec.SocketProperty)
		 */
//		@Override
		public Integer getSocketProperty (SocketProperty property) {
			return socketProperties.get(property);
		}
		
		// ------------------------------------------------------------------------
		// Property Setters
		// ------------------------------------------------------------------------
		/**  @param address the address to set */
//		@Override
        public ConnectionSpec setAddress (InetAddress address) {
        	this.address = address;
        	return this;
        }

		/**  @param port the port to set */
//        @Override
        public ConnectionSpec setPort (int port) {
        	this.port = port;
        	return this;
        }

		/**  @param credentials the credentials to set */
//        @Override
        public ConnectionSpec setCredentials (byte[] credentials) {
        	this.credentials = credentials;
        	return this;
        }

		/**  @param database the database to set */
//        @Override
        public ConnectionSpec setDatabase (int database) {
        	this.database = database;
        	return this;
        }

		/**  @param reconnectCnt the reconnectCnt to set */
//        @Override
        public ConnectionSpec setReconnectCnt (int reconnectCnt) {
        	this.reconnectCnt = reconnectCnt;
        	return this;
        }

		/**
		 * Set the {@link SocketFlag} for the {@link ConnectionSpec}
		 * @param flag
		 * @param value
		 * @return {@link ConnectionSpec} this
		 */
//        @Override
		public ConnectionSpec setSocketFlag(SocketFlag flag, Boolean value){
			socketFlags.put(flag, value);
			return this;
		}
		/**
		 * Set the {@link SocketProperty} for the {@link ConnectionSpec}.
		 * @param property
		 * @param value
		 * @return the previous value (if any).  Null if none existed, per {@link Map#put(Object, Object)} semantics.
		 */
//        @Override
		public ConnectionSpec setSocketProperty(SocketProperty property, Integer value){
			socketProperties.put(property, value);
			return this;
		}

		/* (non-Javadoc)
         * @see org.jredis.connector.ConnectionSpec#isReliable()
         */
        public boolean isReliable () {
	        return isReliable;
        }

		/* (non-Javadoc)
         * @see org.jredis.connector.ConnectionSpec#isReliable(boolean)
         */
        public void isReliable (boolean flag) {
        	isReliable = flag;
        }
        
        public boolean isShared () {
        	return isShared;
        }
        
        public void isShared(boolean flag){
        	this.isShared = flag;
        }
        public boolean isPipeline() {
        	return isPipeline;
        }
        
        public void isPipeline(boolean flag) {
        	isPipeline = flag;
        }
        
        /**
         * @return the heartbeat period in seconds
         */
        public int	getHeartbeat() {
        	return heartbeat/1000;
        }
        
        /**
         * @param seconds heartbeat period
         */
        public void setHeartbeat(int seconds) {
        	this.heartbeat = seconds * 1000;
        }

	}
}