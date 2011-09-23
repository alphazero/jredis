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

import org.jredis.ClientRuntimeException;
import org.jredis.JRedis;
import org.jredis.ProviderException;
import org.jredis.RedisException;
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.protocol.Command;
import org.jredis.protocol.Response;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import org.jredis.ri.alphazero.connection.SyncPipelineConnection;

/**
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Nov 6, 2009
 * @since   alpha.0
 * 
 */

public class JRedisPipelineService extends SyncJRedisBase {

	// ------------------------------------------------------------------------
	// Consts
	// ------------------------------------------------------------------------

	
	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------

	/** connection spec shared by all connections in pool */
	@SuppressWarnings("unused")
    private final ConnectionSpec 	connectionSpec;
	
	private final Connection connection;

	// ------------------------------------------------------------------------
	// Construct and initialize
	// ------------------------------------------------------------------------
	/**
	 * @param host
	 * @param port
	 * @param password
	 * @param database
	 * @param connectionCount
	 */
	public JRedisPipelineService (String host, int port, String password, int database) {
		this(DefaultConnectionSpec.newSpec(host, port, database, (password != null ? password.getBytes() : null)));
	}
	
	/**
	 * @param connectionSpec
	 * @param connectionCount
	 */
	public JRedisPipelineService (ConnectionSpec connectionSpec) {
		this.connectionSpec = connectionSpec;
		connectionSpec.setConnectionFlag(Connection.Flag.SHARED, Boolean.TRUE);
		connection = new SyncPipelineConnection(connectionSpec);
	}
	
	// ------------------------------------------------------------------------
	// super overrides.
	// ------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see org.jredis.ri.alphazero.SynchJRedisBase#setConnection(org.jredis.connector.Connection)
	 */
	@Override
	protected final void setConnection (Connection connection) {
		throw new RuntimeException("who called me?");
	}

	/* (non-Javadoc)
	 * @see org.jredis.ri.alphazero.JRedisSupport#serviceRequest(org.jredis.protocol.Command, byte[][])
	 */
	@Override
	protected Response serviceRequest (Command cmd, byte[]... args) throws RedisException, ClientRuntimeException, ProviderException {
		return connection.serviceRequest(cmd, args);
	}

	/* (non-Javadoc)
	 * @see org.jredis.resource.Resource#getInterface()
	 */
	public JRedis getInterface () {
		return this;
	}
}
