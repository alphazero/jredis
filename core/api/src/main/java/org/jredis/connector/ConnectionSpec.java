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
import org.jredis.Command;


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
	 * @return the port number for the connection
	 */
	public int getPort ();
	/**
	 * @return the password (if any) for the connection.  Used on (re-)connect to authenticate 
	 * the client after connectivity has been established
	 * @Redis AUTH  
	 * @see Command#AUTH
	 */
	public byte[] getCredentials();
	/**
	 * @return the database selected for the connection.  Used on (re-)connect to select the db 
	 * after network connectivity has been established.
	 * @Redis SELECT
	 * @see Command#SELECT
	 */
	public int getDatabase ();
	/**
	 * @param flag
	 * @return the specified TCP socket flag used for the connection.
	 * @see SocketFlag
	 */
	public boolean getSocketFlag (ConnectionSpec.SocketFlag flag);
	/**
	 * @param property
	 * @return the specified socket property used for the connection.
	 * @see SocketFlag
	 */
	public Integer getSocketProperty (ConnectionSpec.SocketProperty property);

	/**
	 * @return
	 */
	public int	getReconnectCnt ();

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
		
		// ------------------------------------------------------------------------
		// Constructor(s)
		// ------------------------------------------------------------------------
		
		// ------------------------------------------------------------------------
		// Interface
		// ------------------------------------------------------------------------
		@Override
		public InetAddress getAddress () {
			// TODO Auto-generated method stub
			return null;
		}

		/* (non-Javadoc)
		 * @see org.jredis.connector.ConnectionSpec#getCredentials()
		 */
		@Override
		public byte[] getCredentials () {
			// TODO Auto-generated method stub
			return credentials;
		}

		/* (non-Javadoc)
		 * @see org.jredis.connector.ConnectionSpec#getDatabase()
		 */
		@Override
		public int getDatabase () {
			// TODO Auto-generated method stub
			return database;
		}

		/* (non-Javadoc)
		 * @see org.jredis.connector.ConnectionSpec#getPort()
		 */
		@Override
		public int getPort () {
			// TODO Auto-generated method stub
			return port;
		}

		/* (non-Javadoc)
		 * @see org.jredis.connector.ConnectionSpec#getReconnectCnt()
		 */
		@Override
		public int getReconnectCnt () {
			// TODO Auto-generated method stub
			return this.reconnectCnt;
		}

		/* (non-Javadoc)
		 * @see org.jredis.connector.ConnectionSpec#getSocketFlag(org.jredis.connector.ConnectionSpec.SocketFlag)
		 */
		@Override
		public boolean getSocketFlag (SocketFlag flag) {
			return socketFlags.get(flag);
		}

		/* (non-Javadoc)
		 * @see org.jredis.connector.ConnectionSpec#getSocketProperty(org.jredis.connector.ConnectionSpec.SocketProperty)
		 */
		@Override
		public Integer getSocketProperty (SocketProperty property) {
			return socketProperties.get(property);
		}
		
		// ------------------------------------------------------------------------
		// Property Setters
		// ------------------------------------------------------------------------
		/**  @param address the address to set */
        public void setAddress (InetAddress address) {
        	this.address = address;
        }

		/**  @param port the port to set */
        public void setPort (int port) {
        	this.port = port;
        }

		/**  @param credentials the credentials to set */
        public void setCredentials (byte[] credentials) {
        	this.credentials = credentials;
        }

		/**  @param database the database to set */
        public void setDatabase (int database) {
        	this.database = database;
        }

		/**  @param reconnectCnt the reconnectCnt to set */
        public void setReconnectCnt (int reconnectCnt) {
        	this.reconnectCnt = reconnectCnt;
        }

		/**
		 * Set the {@link SocketFlag} for the {@link ConnectionSpec}
		 * @param flag
		 * @param value
		 * @return the previous value (if any).  Null if none existed, per {@link Map#put(Object, Object)} semantics.
		 */
		public Boolean setSocketFlag(SocketFlag flag, Boolean value){
			return socketFlags.put(flag, value);
		}
		/**
		 * Set the {@link SocketProperty} for the {@link ConnectionSpec}.
		 * @param property
		 * @param value
		 * @return the previous value (if any).  Null if none existed, per {@link Map#put(Object, Object)} semantics.
		 */
		public Integer setSocketProperty(SocketProperty property, Integer value){
			return socketProperties.put(property, value);
		}
	}
}