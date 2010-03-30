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
import org.jredis.ProviderException;
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.connector.FaultedConnection;
import org.jredis.resource.Context;
import org.jredis.resource.Resource;
import org.jredis.resource.ResourceException;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import org.jredis.ri.alphazero.connection.SynchConnection;
import org.jredis.ri.alphazero.support.Assert;
import org.jredis.ri.alphazero.support.Log;

/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 15, 2009
 * @since   alpha.0
 * 
 */

public abstract class SynchJRedisBase extends JRedisSupport implements Resource<JRedis> {

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// Inner ops
	// ------------------------------------------------------------------------

	/**
	 * This extension point is really only necessary to allow this class to
	 * set the {@link FaultedConnection} when necessary, in course of the
	 * {@link SynchJRedisBase#createSynchConnection(String, int, RedisVersion)}
	 * method operation.  
	 * 
	 * @param connection
	 */
	protected abstract void setConnection (Connection connection) ;

	/**
	 * Creates a {@link Connection} with {@link Connection.Modality#Synchronous} semantics
	 * suitable for use by synchronous (blocking) JRedis clients.
	 *  
	 * [TODO: this method should be using connection spec!]
	 * @param host
	 * @param port
	 * @param credentials 
	 * @param database 
	 * @param redisVersion
	 * @return
	 */
	protected Connection createSynchConnection (String host, int port, int database, byte[] credentials, boolean isShared, RedisVersion redisVersion) 
	{
		InetAddress 	address = null;
		Connection 		synchConnection = null;
		try {
			
			address = InetAddress.getByName(host);
			ConnectionSpec spec = DefaultConnectionSpec.newSpec(address, port, database, credentials);
			synchConnection = createSynchConnection(spec, isShared, redisVersion);
			Assert.notNull(synchConnection, "connection delegate", ClientRuntimeException.class);
		}
		catch (UnknownHostException e) {
			String msg = "Couldn't obtain InetAddress for "+host;
			Log.problem (msg+"  => " + e.getLocalizedMessage());
			throw new ClientRuntimeException(msg, e);
		}
		return synchConnection;
	}
	
	/**
	 * Creates a {@link Connection} with {@link Connection.Modality#Synchronous} semantics
	 * suitable for use by synchronous (blocking) JRedis clients.
	 *  
	 * @param connectionSpec connection's specification
	 * @param redisVersion redis protocol compliance
	 * @return
	 */
	protected Connection createSynchConnection(ConnectionSpec connectionSpec, boolean isShared, RedisVersion redisVersion){
		Connection 		synchConnection = null;
		try {
			synchConnection = new SynchConnection(connectionSpec, isShared, redisVersion);
			Assert.notNull(synchConnection, "connection delegate", ClientRuntimeException.class);
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
			setConnection(new FaultedConnection(connectionSpec, msg));
		}
		return synchConnection;
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
	
	private Context context;

//	@Override
	public final Context getContext() throws ResourceException {
		return context;
	}

//	@Override
	public final void setContext(Context context) throws ResourceException {
		this.context = context;
	}
}
