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

package org.jredis.ri.alphazero;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jredis.ClientRuntimeException;
import org.jredis.JRedis;
import org.jredis.NotSupportedException;
import org.jredis.Redis;
import org.jredis.connector.Connection;
import org.jredis.connector.FaultedConnection;
import org.jredis.connector.Protocol;
import org.jredis.connector.ProviderException;
import org.jredis.ri.alphazero.support.Assert;
import org.jredis.ri.alphazero.support.Log;


/**
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
 * Connection management issues are handled by the {@link SocketConnection} delegate and
 * you should read the docs for that class for details of its operations.  But as far as
 * {@link JRedis} support is concerned, a call to {@link JRedis#quit()} or {@link JRedis#shutdown()}
 * will close the connections and subsequent calls to the interface will prompt the throwing of an
 * exception.
 * <p>
 * Redis protocol is handled by a {@link Protocol} instance obtained from the {@link ProtocolManager}.
 * This class will by default specify the {@link RedisVersion#current_revision}.  
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 * 
 */
@Redis(versions={"0.09"})
public class JRedisClient  extends JRedisSupport  {
	
	// ------------------------------------------------------------------------
	// Construct and initialize
	// ------------------------------------------------------------------------

	
	/**
	 * New RedisClient for the default protocol version {@link RedisVersion} 
	 * obtained from the {@link ProtocolManager}
	 * and using localhost:6379 as its network addressing parameters. 
	 * 
	 * @see JRedisClient#RedisClient(String, int, String)
	 */
	public JRedisClient ( ){
		this ("localhost", 6379, RedisVersion.current_revision);
	}
	/**
	 * New RedisClient for the default protocol version {@link RedisVersion} 
	 * obtained from the {@link ProtocolManager}
	 * 
	 * @see JRedisClient#RedisClient(String, int, String)
	 * @param host
	 * @param port
	 */
	public JRedisClient(String host, int port) {
		this(host, port, RedisVersion.current_revision);
	}

	/**
	 * Creates a new instance of RedisClient, using the information provided.  
	 * <p>
	 * This constructor will delegate all {@link Protocol} issues to a
	 * {@link SocketConnection} instance, for which it will obtain a {@link Protocol}
	 * handler delegate from {@link ProtocolManager} for the user specified redis version 
	 * <p>
	 * All specifics regarding the implementation of the {@link JRedis} contract are handled by
	 * the superclass  and you should consult that class's documentation for the detailss.
	 *   
	 * @param host
	 * @param port
	 * @param redisVersion
	 * @throws ClientRuntimeException
	 */
	public JRedisClient (String host, int port, RedisVersion redisVersion) 
	throws ClientRuntimeException
	{
		Assert.notNull(host, "host parameter", IllegalArgumentException.class);
		Assert.notNull(redisVersion, "redisVersion paramter", IllegalArgumentException.class);
		
		InetAddress 	address = null;
		Connection 		connection = null;
		try {
			address = InetAddress.getByName(host);
			connection = new SynchConnection(address, port, redisVersion);
			Assert.notNull(connection, "connection delegate", ClientRuntimeException.class);
			
			super.setConnection (connection);
		}
		catch (NotSupportedException e) {
			Log.log("Can not support redis protocol '%s'", redisVersion);
			throw e;
		}
		catch (ProviderException e) {
			Log.bug("Couldn't create the handler delegate.  => " + e.getLocalizedMessage());
			throw e;
		}
		catch (ClientRuntimeException e) {
			String msg = e.getMessage() + "\nMake sure your server is running.";
			Log.error ("Error creating connection -> " + e.getLocalizedMessage());
			super.setConnection(new FaultedConnection(msg));
//			throw e;
		}
		catch (UnknownHostException e) {
			String msg = "Couldn't obtain InetAddress for "+host;
			Log.problem (msg+"  => " + e.getLocalizedMessage());
			throw new ClientRuntimeException(msg, e);
		}
//		Log.log("RedisClient [ver: %s] created and connected", redisVersion);
	}
}
