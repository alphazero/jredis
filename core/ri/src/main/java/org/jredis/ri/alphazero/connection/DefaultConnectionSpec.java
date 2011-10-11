package org.jredis.ri.alphazero.connection;

import static org.jredis.connector.Connection.Flag.CONNECT_IMMEDIATELY;
import static org.jredis.connector.Connection.Flag.PIPELINE;
import static org.jredis.connector.Connection.Flag.RELIABLE;
import static org.jredis.connector.Connection.Flag.SHARED;
import static org.jredis.connector.Connection.Socket.Property.SO_PREF_BANDWIDTH;
import static org.jredis.connector.Connection.Socket.Property.SO_PREF_CONN_TIME;
import static org.jredis.connector.Connection.Socket.Property.SO_PREF_LATENCY;
import static org.jredis.connector.Connection.Socket.Property.SO_RCVBUF;
import static org.jredis.connector.Connection.Socket.Property.SO_SNDBUF;
import static org.jredis.connector.Connection.Socket.Property.SO_TIMEOUT;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.jredis.ClientRuntimeException;
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.connector.Connection.Flag;
import org.jredis.connector.Connection.Modality;
import org.jredis.ri.alphazero.protocol.DefaultProtocolFactory;
import org.jredis.ri.alphazero.support.Assert;

/**
 * Default connection spec provides the following default values for a connection.  See
 * {@link ConnectionSpec} for details of these properties and flags.
 * <p>
 * The default {@link Modality} for the {@link Connection} is {@link Modality#Synchronous}.
 * <p>
 * This {@link ConnectionSpec} is configured to prefer bandwidth and relatively large (48K)
 * buffers (which is probably less than your OS's default buffer sizes anyway, but you never know).
 * <p>
 * Connection Retry limit is {@link DefaultConnectionSpec#DEFAULT_RECONNECT_CNT}.
 * <p>
 * Connection is spec'd for Keep Alive.
 * <p>
 * Socket timeout is {@link DefaultConnectionSpec#DEFAULT_READ_TIMEOUT_MSEC}
 * <p>
 * No codecs and/or compression classes are defined.
 *   
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 20, 2009
 * @since   alpha.0
 * 
 */
final public class DefaultConnectionSpec extends ConnectionSpec.RefImpl {
	// ------------------------------------------------------------------------
	// Consts
	// ------------------------------------------------------------------------
	
	static final int DEFAULT_REDIS_PORT = 6379;
	static final String DEFAULT_REDIS_HOST_NAME = "localhost";
	static final int DEFAULT_REDIS_DB = 0;
	static final byte[] DEFAULT_REDIS_PASSWORD = null;
	
	/** def value: <code>48KB</code> */
	private static final int DEFAULT_RCV_BUFF_SIZE = 1024 * 48;
	/** def value: <code>48KB</code> */
	private static final int DEFAULT_SND_BUFF_SIZE = 1024 * 48;
	/** def value: <code>5000 msecs</code> */
	static final int DEFAULT_READ_TIMEOUT_MSEC = 5000;
	
	/** def value: <code>1</code> sec. (min timeout in redis conf */
	static final int DEFAULT_HEARTBEAT_SEC = 1;
	
	/** def value: <code>0</code> e.g. higher priority pref is bandwidth */
	private static final int DEFAULT_SO_PREF_BANDWIDTH = 0;
	/** def value: <code>1</code> e.g. second priority pref is latency */
	private static final int DEFAULT_SO_PREF_LATENCY = 1;
	/** def value: <code>3</code> e.g. third priority pref is connection time */
	private static final int DEFAULT_SO_PREF_CONN_TIME = 2;
	
	/** def value: <code>true</code> */
	private static final boolean DEFAULT_CF_SHARED = true;
	/** def value: <code>false</code> */
	private static final boolean DEFAULT_CF_RELIABLE = false;
	/** def value: <code>false</code> */
	private static final boolean DEFAULT_CF_PIPELINE = false;
	/** def value: <code>true</code> */
	private static final boolean DEFAULT_CF_CONNECT_IMMEDIATELY = true;
	/** def value: <code>true</code> */
	private static final boolean DEFAULT_CF_STATEFUL = false;
	
	/** def value: <code>Modality.Synchronous</code> */
	private static final Modality DEFAULT_CP_CONN_MODALITY = Modality.Synchronous;
	/** def value: <code>3</code> */
	private static final int DEFAULT_CP_MAX_CONNECT_ATTEMPT = 3;
	
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------
	/**
	 * Instantiates a default connection spec for the given host and port.
	 * @param address
	 * @param port
	 * @throws ClientRuntimeException for invalid port, or null address values
	 */
	public DefaultConnectionSpec () throws ClientRuntimeException {
//		Log.debug("Yo!");
		setDefaultValues();
	}
	/**
	 * Instantiates a default connection spec for the given host and port.
	 * @param address
	 * @param port
	 * @throws ClientRuntimeException for invalid port, or null address values
	 */
	@Deprecated
	public DefaultConnectionSpec (InetAddress address, int port) throws ClientRuntimeException {
		this (address, port, 0, null);
	}
	/**
	 * Instantiates a default connection spec for the given host and port.
	 * @param address
	 * @param port
	 * @param database
	 * @param credentials
	 * @throws ClientRuntimeException for invalid port, or null address values
	 */
	@Deprecated
	public DefaultConnectionSpec (InetAddress address, int port, int database, byte[] credentials) throws ClientRuntimeException {
		this();
		setPort(Assert.inRange (port, 1, 65534, "port init parameter for DefaultConnectionSpec", ClientRuntimeException.class));
		setAddress(Assert.notNull(address, "address init parameter for DefaultConnectionSpec", ClientRuntimeException.class));
		setDatabase(database);
		setCredentials(credentials);
	}
	/**
     * Set the default values for the {@link ConnectionSpec}
     * other properties.
     * @see ConnectionSpec
     * @see Connection.Property
     * @see Connection.Property
     * @see Connection.Flag
     * @see Connection.Socket.Flag
     * @see Connection.Socket.Property
     * @see DefaultConnectionFactory
     * @see DefaultProtocolFactory
     */
    private void setDefaultValues () {
//    	// reconnect try count
//    	setReconnectCnt(DEFAULT_CP_MAX_CONNECT_ATTEMPT);
    	
    	//  tcp socket flags
    	setSocketFlag(Connection.Socket.Flag.SO_KEEP_ALIVE, true);
    	
    	// tcp socket flags
    	setSocketProperty(SO_TIMEOUT, DEFAULT_READ_TIMEOUT_MSEC);
    	setSocketProperty(SO_RCVBUF, DEFAULT_RCV_BUFF_SIZE);
    	setSocketProperty(SO_SNDBUF, DEFAULT_SND_BUFF_SIZE);
    	setSocketProperty(SO_PREF_BANDWIDTH, DEFAULT_SO_PREF_BANDWIDTH);
    	setSocketProperty(SO_PREF_CONN_TIME, DEFAULT_SO_PREF_CONN_TIME);
    	setSocketProperty(SO_PREF_LATENCY, DEFAULT_SO_PREF_LATENCY);
    	
    	setConnectionFlag(RELIABLE, DEFAULT_CF_RELIABLE);
    	setConnectionFlag(SHARED, DEFAULT_CF_SHARED);
    	setConnectionFlag(PIPELINE, DEFAULT_CF_PIPELINE);
    	setConnectionFlag(CONNECT_IMMEDIATELY, DEFAULT_CF_CONNECT_IMMEDIATELY);
    	setConnectionFlag(Flag.STATEFUL, DEFAULT_CF_STATEFUL);
    	
    	setConnectionProperty(Connection.Property.MODALITY, DEFAULT_CP_CONN_MODALITY);
    	setConnectionProperty(Connection.Property.MAX_CONNECT_ATTEMPT, DEFAULT_CP_MAX_CONNECT_ATTEMPT);
    	setConnectionProperty(Connection.Property.PROTOCOL_FACTORY, new DefaultProtocolFactory());
    	setConnectionProperty(Connection.Property.CONNECTION_FACTORY, new DefaultConnectionFactory());
    	
    	setHeartbeat(DEFAULT_HEARTBEAT_SEC);
    }
	// ------------------------------------------------------------------------
	// Static methods
	// ------------------------------------------------------------------------
	/**
	 * @return
	 * @throws ClientRuntimeException
	 */
	public static final ConnectionSpec newSpec () 
		throws ClientRuntimeException 
	{
		return newSpec (DEFAULT_REDIS_HOST_NAME, DEFAULT_REDIS_PORT, DEFAULT_REDIS_DB, DEFAULT_REDIS_PASSWORD);
	}

	/**
	 * Returns an instance of the {@link ConnectionSpec} used by this {@link Connection}
	 * as default spec, for the provided params.
	 * @param host
	 * @param port
	 * @param database
	 * @param credentials
	 * @param redisversion
	 * @return
	 * @throws ClientRuntimeException
	 */
	public static final ConnectionSpec newSpec (
			String 			host, 
			int 			port, 
			int 			database, 
			byte[] 			credentials
		) 
		throws ClientRuntimeException 
	{
    	InetAddress address;
        try {
	        address = InetAddress.getByName(host);
        }
        catch (UnknownHostException e) {
        	throw new ClientRuntimeException("unknown host: " + host, e);
        }
		
		return newSpec(address, port, database, credentials);
	}
	/**
	 * Returns an instance of the {@link ConnectionSpec} used by this {@link Connection}
	 * as default spec, for the provided params.
	 * @param address
	 * @param port
	 * @param database
	 * @param credentials
	 * @param redisversion
	 * @return
	 * @throws ClientRuntimeException
	 */
	public static final ConnectionSpec newSpec (
			InetAddress 	address, 
			int 			port, 
			int 			database, 
			byte[] 			credentials
		) 
		throws ClientRuntimeException 
	{
//		return new DefaultConnectionSpec(address, port, database, credentials);
		ConnectionSpec spec = new DefaultConnectionSpec();
		return spec.setAddress(address).setPort(port).setDatabase(database).setCredentials(credentials);
		
	}
}