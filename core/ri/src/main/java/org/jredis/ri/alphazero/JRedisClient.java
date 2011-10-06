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

package org.jredis.ri.alphazero;

import java.net.UnknownHostException;
import org.jredis.ClientRuntimeException;
import org.jredis.JRedis;
import org.jredis.ProviderException;
import org.jredis.Redis;
import org.jredis.RedisException;
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.protocol.Command;
import org.jredis.protocol.Response;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import org.jredis.ri.alphazero.support.Assert;


/**
 * [TODO: check documentation and make necessary changes made during refactoring]
 * 
 * A basic client, using {@link SocketConnection} and handler delegate 
 * <p>
 * This class is simply an assembly of various other components that address distinct
 * concerns of a JRedis connection, and effectively defines the connection policy
 * and use-case patterns by selecting this set of cooperating elements.
 * <p>
 * This is a <i>simple client</i> object suitable for long-held open connections
 * to a redis server by single threaded applications.  
 * <p>
 * <b>The components that are used ARE NOT thread-safe</b> and assume 
 * synchronised/sequential access to the api defined by {@link JRedis}.
 * Both the connection and protocol handler delegates of this class are intended for use
 * by a <b>single</b> thread, or strictly sequential access by a pool of threads.  You can
 * create multiple instances of this class and use dedicated threads for each to create a
 * service (if you wish).
 * <p>
 * Redis protocol is handled by a {@link Protocol} instance obtained from the {@link ProtocolManager}.
 * This class will by default specify the {@link RedisVersion#current_revision}.  
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 * 
 */
/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Aug 13, 2009
 * @since   alpha.0
 * 
 */
/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Aug 13, 2009
 * @since   alpha.0
 * 
 */
@Redis(versions={"1.00"})
public class JRedisClient extends SyncJRedisBase  {
	
	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------

	private Connection	connection;

	// ------------------------------------------------------------------------
	// Construct and initialize
	// ------------------------------------------------------------------------

	public JRedisClient (ConnectionSpec connectionSpec){
		connectionSpec.setConnectionFlag(Connection.Flag.RELIABLE, Boolean.TRUE);
		connectionSpec.setConnectionFlag(Connection.Flag.SHARED, Boolean.FALSE);
		Connection syncConnection = createSyncConnection (connectionSpec);
		setConnection (syncConnection);
	}
	/**
	 * Connects to the localhost:6379 redis server using the password.
	 * Will select db 0.
     * @param password used for AUTH
	 * @throws UnknownHostException 
	 * @throws ClientRuntimeException 
     */
    public JRedisClient (String password) throws ClientRuntimeException {
		this ("localhost", 6379, password, 0);
    }

	/**
	 * and using localhost:6379 as its network addressing parameters. 
	 * Database will be selected to db 0
	 * Assumes no password required.
	 * @throws UnknownHostException 
	 * @throws ClientRuntimeException 
	 */
	public JRedisClient ( ) throws ClientRuntimeException {
		this ("localhost", 6379, null, 0);
	}
	/**
	 * 
	 * @param host
	 * @param port
	 * @throws UnknownHostException 
	 * @throws ClientRuntimeException 
	 */
	public JRedisClient(String host, int port) throws ClientRuntimeException {
//		this(host, port, null, 0, RedisVersion.current_revision);
		this(host, port, null, 0);
	}
	
	
	/**
	 * New JRedisClient for the default protocol version {@link RedisVersion}
	 * @param host redis server's host
	 * @param port redis server's port
	 * @param password to use for AUTHentication (can be null)
	 * @param database database to select on connect
	 * @throws ClientRuntimeException
	 * @throws UnknownHostException 
	 */
	public JRedisClient (String host, int port, String password, int database) 
	throws ClientRuntimeException
	{
		this(DefaultConnectionSpec.newSpec(host, port, database, getCredentialBytes(password)));
	}
	
	// ------------------------------------------------------------------------
	// Super overrides
	// ------------------------------------------------------------------------
	@Override
	protected Response serviceRequest(Command cmd, byte[]... args)
			throws RedisException, ClientRuntimeException, ProviderException 
	{
		return connection.serviceRequest(cmd, args);
	}
	

	// TODO: what's the use of this?
	@Override
	protected final void setConnection (Connection connection)  {
		this.connection = Assert.notNull(connection, "connection on setConnection()", ClientRuntimeException.class);
	}
	
	// ------------------------------------------------------------------------
	// Interface
	// =========================================================== Resource<T>
	/*
	 * Provides basic Resource support without any state management.  Extensions
	 * that use context in a simply manner can rely on these methods.  Others may
	 * wish to override.
	 */
	// ------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see org.jredis.resource.Resource#getInterface()
	 */
	@Override
	public JRedis getInterface() {
		return this;
	}
	// ------------------------------------------------------------------------
	// Static Utils
	// ------------------------------------------------------------------------
	/**
	 * Internal use only - 
	 * @param password
	 * @return the bytes of password or null if none spec'd
	 */
	private static byte[] getCredentialBytes (String password){
		return (null != password ? password.getBytes() : null);
	}
}
