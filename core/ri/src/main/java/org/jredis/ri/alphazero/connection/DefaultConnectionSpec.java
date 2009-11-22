package org.jredis.ri.alphazero.connection;

import static org.jredis.connector.ConnectionSpec.SocketFlag.SO_KEEP_ALIVE;
import static org.jredis.connector.ConnectionSpec.SocketProperty.SO_PREF_BANDWIDTH;
import static org.jredis.connector.ConnectionSpec.SocketProperty.SO_PREF_CONN_TIME;
import static org.jredis.connector.ConnectionSpec.SocketProperty.SO_PREF_LATENCY;
import static org.jredis.connector.ConnectionSpec.SocketProperty.SO_RCVBUF;
import static org.jredis.connector.ConnectionSpec.SocketProperty.SO_SNDBUF;
import static org.jredis.connector.ConnectionSpec.SocketProperty.SO_TIMEOUT;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.jredis.ClientRuntimeException;
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.support.Assert;

/**
 * Default connection spec provides the following default values for a connection.  See
 * {@link ConnectionSpec} for details of these properties and flags.
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
public class DefaultConnectionSpec extends ConnectionSpec.RefImpl {
	// ------------------------------------------------------------------------
	// Consts
	// ------------------------------------------------------------------------
	/** defaults to 3 */
	static final int DEFAULT_RECONNECT_CNT = 3;
	/** defautls to 48KB */
	private static final int DEFAULT_RCV_BUFF_SIZE = 1024 * 48;
	/** defaults to 48KB */
	private static final int DEFAULT_SND_BUFF_SIZE = 1024 * 48;
	/** defaults to 5000 msecs */
	static final int DEFAULT_READ_TIMEOUT_MSEC = 5000;
	
	/** defaults to 1 second (the min on Redis) */
	static final int DEFAULT_HEARTBEAT_SEC = 1;
	
	/** higher priority pref is bandwidth */
	private static final int DEFAULT_SO_PREF_BANDWIDTH = 0;
	/** second priority pref is latency */
	private static final int DEFAULT_SO_PREF_LATENCY = 1;
	/** thrid priority pref is connection time */
	private static final int DEFAULT_SO_PREF_CONN_TIME = 2;
	
	private static final boolean DEFAULT_IS_SHARED = true;
	private static final boolean DEFAULT_IS_RELIABLE = false;
	private static final boolean DEFAULT_IS_PIPELINE = false;
	
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------
	/**
	 * Instantiates a default connection spec for the given host and port.
	 * @param address
	 * @param port
	 * @throws ClientRuntimeException for invalid port, or null address values
	 */
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
	public DefaultConnectionSpec (InetAddress address, int port, int database, byte[] credentials) throws ClientRuntimeException {
		setPort(Assert.inRange (port, 1, 65534, "port init parameter for DefaultConnectionSpec", ClientRuntimeException.class));
		setAddress(Assert.notNull(address, "address init parameter for DefaultConnectionSpec", ClientRuntimeException.class));
		setDatabase(database);
		setCredentials(credentials);
		
		setDefaultProperties();
	}
	/**
     * Set the default values for the {@link SocketFlag}s and {@link SocketProperty}s and various
     * other properties.
     * @See {@link ConnectionSpec}
     */
    private void setDefaultProperties () {
    	// reconnect try count
    	setReconnectCnt(DEFAULT_RECONNECT_CNT);
    	
    	//  tcp socket flags
    	setSocketFlag(SO_KEEP_ALIVE, true);
    	
    	// tcp socket flags
    	setSocketProperty(SO_TIMEOUT, DEFAULT_READ_TIMEOUT_MSEC);
    	setSocketProperty(SO_RCVBUF, DEFAULT_RCV_BUFF_SIZE);
    	setSocketProperty(SO_SNDBUF, DEFAULT_SND_BUFF_SIZE);
    	setSocketProperty(SO_PREF_BANDWIDTH, DEFAULT_SO_PREF_BANDWIDTH);
    	setSocketProperty(SO_PREF_CONN_TIME, DEFAULT_SO_PREF_CONN_TIME);
    	setSocketProperty(SO_PREF_LATENCY, DEFAULT_SO_PREF_LATENCY);
    	
    	isReliable(DEFAULT_IS_RELIABLE);
    	isShared(DEFAULT_IS_SHARED);
    	isPipeline(DEFAULT_IS_PIPELINE);
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
		return newSpec ("localhost", 6379, 0, null);
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
		return new DefaultConnectionSpec(address, port, database, credentials);
	}
}