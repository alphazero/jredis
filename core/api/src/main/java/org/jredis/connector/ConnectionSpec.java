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
import java.util.HashMap;
import java.util.Map;
import org.jredis.connector.Connection.Modality;
import org.jredis.connector.Connection.Property;


/**
 * ConnectionSpec specifies the parameters used in the creation and
 * runtime operation of JRedis connections.
 * 
 * @author joubin (alphazero@sensesay.net)
 *
 * @see Factory#newConnection(ConnectionSpec)
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
	 * Set the Connection's credentials, presented to Redis on (re)connects.
	 * @param credentials
	 * @see ConnectionSpec#setCredentials(String)
     * @return the {@link ConnectionSpec}
	 */
	public ConnectionSpec setCredentials(byte[] credentials);
	/**
	 * Convenience method 
	 * @param credentials
	 * @see ConnectionSpec#setCredentials(byte[])
     * @return the {@link ConnectionSpec}
	 */
	public ConnectionSpec setCredentials(String credentials);
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
	 * @return the {@link Modality} of this protocol handler.
	 */
	public Modality getModality ();
	/**
	 * @param modality
	 * @return the the {@link ConnectionSpec}
	 */
	public ConnectionSpec setModality (Modality modality);
	/**
	 * Get the {@link SocketFlag} for the {@link ConnectionSpec}
	 * @param flag
	 * @return the specified TCP socket flag used for the connection.
	 * @see SocketFlag
	 */
	public boolean getSocketFlag (Connection.Socket.Flag flag);
	/**
	 * Set the {@link Connection.Socket.Flag} for the {@link ConnectionSpec}
	 * @param flag
	 * @param value
	 * @return {@link ConnectionSpec} this
	 */
	public ConnectionSpec setSocketFlag(Connection.Socket.Flag flag, Boolean value);
	/**
	 * Get the {@link Connection.Socket.Property} for the {@link ConnectionSpec}
	 * @param property
	 * @return the specified socket property used for the connection.
	 * @see SocketFlag
	 */
	public Integer getSocketProperty (Connection.Socket.Property property);
	/**
	 * Set the {@link SocketProperty} for the {@link ConnectionSpec}
	 * @param property
	 * @param value
	 * @return the previous value (if any).  Null if none existed, per {@link Map#put(Object, Object)} semantics.
	 */
	public ConnectionSpec setSocketProperty(Connection.Socket.Property property, Integer value);
 	/**
	 * @param flag
	 * @return the {@link Connection.Flag}
	 * @see SocketFlag
	 */
	public boolean getConnectionFlag (Connection.Flag flag);
	/**
	 * Sets the specified {@link Connection.Flag}
	 * @param flag
	 * @param value
	 * @return the referenced {@link ConnectionSpec}
	 */
	public ConnectionSpec setConnectionFlag(Connection.Flag flag, Boolean value);
	/**
	 * @param <T>
	 * @param proptype
	 * @return
	 */
	public Object getConnectionProperty(Property prop);
	/**
	 * @param <T>
	 * @param property
	 * @param value
	 * @return
	 */
	public ConnectionSpec setConnectionProperty(Property prop, Object value);
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
	 * @return
	 */
	public int	getMaxConnectWait ();
	/**
	 * @param cnt
     * @return the {@link ConnectionSpec}
	 */
	public ConnectionSpec setMaxConnectWait(int cnt);
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
		// Attributes
		// ------------------------------------------------------------------------
		
		/** {@link Map} of the {@link SocketFlag}s of the {@link ConnectionSpec} */
		Map<Connection.Socket.Flag, Boolean> socketFlags = new HashMap<Connection.Socket.Flag, Boolean>();
		
		/** {@link Map} of the {@link SocketProperty}s of the {@link ConnectionSpec} */
		Map<Connection.Socket.Property, Integer> socketProperties = new HashMap<Connection.Socket.Property, Integer>();
		
		/** Connection.Flag bitmask */
		int		connectionFlagBitmask;
		
		/** {@link Map} of the {@link Connection.Flag}s of the {@link ConnectionSpec} */
		Map<Connection.Property, Object> connectionProperties = new HashMap<Connection.Property, Object>();
		
		/** heartbeat period in milliseconds */
		private int heartbeat;
		
		// ------------------------------------------------------------------------
		// Constructor(s)
		// ------------------------------------------------------------------------
		
		// ------------------------------------------------------------------------
		// Interface
		// ------------------------------------------------------------------------
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getAddress() */
		@Override
		final public InetAddress getAddress () {
			return (InetAddress) getConnectionProperty(Connection.Property.HOST);
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getCredentials() */
		@Override
		final public byte[] getCredentials () {
			return (byte[]) getConnectionProperty(Connection.Property.CREDENTIAL);
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getDatabase() */
		@Override
		final public int getDatabase () {
			return (Integer) getConnectionProperty(Connection.Property.DB);
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getPort() */
		@Override
		final public int getPort () {
			return (Integer) getConnectionProperty(Connection.Property.PORT);
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getModality() */
		final public Modality getModality () { 
			return (Modality) getConnectionProperty(Connection.Property.MODALITY);
		}
		
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getMaxConnectWait() */
		final public int getMaxConnectWait () {
			return (Integer) getConnectionProperty(Connection.Property.MAX_CONNECT_WAIT);
		}
		
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getReconnectCnt() */
		@Override
		final public int getReconnectCnt () {
			return (Integer) getConnectionProperty(Connection.Property.MAX_CONNECT_ATTEMPT);
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getSocketFlag(org.jredis.connector.ConnectionSpec.SocketFlag) */
		@Override
		final public boolean getSocketFlag (Connection.Socket.Flag flag) {
			return socketFlags.get(flag);
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getSocketProperty(org.jredis.connector.ConnectionSpec.SocketProperty) */
		@Override
		final public Integer getSocketProperty (Connection.Socket.Property property) {
			return socketProperties.get(property);
		}
        /* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getConnectionProperty(org.jredis.connector.Connection.Property) */
		@Override
        final public Object getConnectionProperty(Property prop){
			return connectionProperties.get(prop);			
		}

		// ------------------------------------------------------------------------
		// Property Setters
		// ------------------------------------------------------------------------
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setAddress(java.net.InetAddress) */
		final public ConnectionSpec setAddress (InetAddress address) {
			setConnectionProperty(Connection.Property.HOST, address);
        	return this;
        }
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setPort(int) */
		@Override
		final public ConnectionSpec setPort (int port) {
			setConnectionProperty(Connection.Property.PORT, port);
//        	this.port = port;
        	return this;
        }
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setCredentials(byte[]) */
		@Override
		final public ConnectionSpec setCredentials (byte[] credentials) {
			setConnectionProperty(Connection.Property.CREDENTIAL, credentials);
        	return this;
        }
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setCredentials(java.lang.String) */
		@Override
		final public ConnectionSpec setCredentials (String credentials) {
			byte[] bytes;
			if(credentials == null || credentials.length() == 0)
				bytes = null;
			else 
				bytes = credentials.getBytes();
        	return setCredentials(bytes);
        }
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setDatabase(int) */
		@Override
		final public ConnectionSpec setDatabase (int database) {
			setConnectionProperty(Connection.Property.DB, database);
        	return this;
        }
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setReconnectCnt(int) */
		@Override
		final public ConnectionSpec setReconnectCnt (int reconnectCnt) {
			setConnectionProperty(Connection.Property.MAX_CONNECT_ATTEMPT, reconnectCnt);
        	return this;
        }
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setConnectionProperty(org.jredis.connector.Connection.Property, java.lang.Object) */
		@Override
		final public ConnectionSpec setConnectionProperty(Property prop, Object value){
			try {  connectionProperties.put(prop, value); }
			catch (ClassCastException e){ throw new IllegalArgumentException("value type", e);}
			return this;
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setModality(org.jredis.connector.Connection.Modality) */
		@Override
		final public ConnectionSpec setModality (Modality modality) {
			setConnectionProperty(Connection.Property.MODALITY, modality);
			return this;
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getMaxConnectWait(int) */
		final public ConnectionSpec setMaxConnectWait(int cnt) {
			setConnectionProperty(Connection.Property.MAX_CONNECT_WAIT, cnt);
			return this;
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setSocketFlag(org.jredis.connector.ConnectionSpec.SocketFlag, java.lang.Boolean) */
		@Override
		final public ConnectionSpec setSocketFlag(Connection.Socket.Flag flag, Boolean value){
			socketFlags.put(flag, value);
			return this;
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setSocketProperty(org.jredis.connector.ConnectionSpec.SocketProperty, java.lang.Integer) */
		@Override
		final public ConnectionSpec setSocketProperty(Connection.Socket.Property property, Integer value){
			socketProperties.put(property, value);
			return this;
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getConnectionFlag(org.jredis.connector.ConnectionSpec.ConnectionFlag) */
		@Override
		final public boolean getConnectionFlag (Connection.Flag flag){
			return Connection.Flag.isSet(connectionFlagBitmask, flag);
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setConnectionFlag(org.jredis.connector.ConnectionSpec.ConnectionFlag, java.lang.Boolean) */
		@SuppressWarnings("boxing")
		@Override
		final public ConnectionSpec setConnectionFlag(Connection.Flag flag, Boolean value){
			connectionFlagBitmask = 
				value ? 
						Connection.Flag.bitset(connectionFlagBitmask, flag) : 
						Connection.Flag.bitclear(connectionFlagBitmask, flag);
			return this;
		}
		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#getHeartbeat() */
		@Override
		final public int	getHeartbeat() {
        	return heartbeat/1000;
        }
 		/* (non-Javadoc) @see org.jredis.connector.ConnectionSpec#setHeartbeat(int) */
		@Override
		final public ConnectionSpec setHeartbeat(int seconds) {
        	this.heartbeat = seconds * 1000;
        	return this;
        }
	}
}