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
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.connector.FaultedConnection;
import org.jredis.connector.Connection.Property;
import org.jredis.resource.Context;
import org.jredis.resource.Resource;
import org.jredis.resource.ResourceException;
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

public abstract class SyncJRedisBase extends JRedisSupport implements Resource<JRedis> {

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
	 * {@link SyncJRedisBase#createSyncConnection(String)}
	 * method operation.  
	 * 
	 * @param connection
	 */
	protected abstract void setConnection (Connection connection) ;

	/**
	 * Creates a {@link Connection} with {@link Connection.Modality#Synchronous} semantics
	 * suitable for use by synchronous (blocking) JRedis clients.
	 *  
	 * @param connSpec connection's specification
	 * @param redisVersion redis protocol compliance
	 * @return
	 */
	protected Connection createSyncConnection(ConnectionSpec connSpec){
		Connection.Factory cfact = (Connection.Factory) connSpec.getConnectionProperty(Property.CONNECTION_FACTORY);
		Connection 	conn = null;
		try {
			conn = Assert.notNull(cfact.newConnection(connSpec), "connection delegate", ClientRuntimeException.class);
		}
		catch (ProviderException e) {
			Log.bug("Couldn't create the handler delegate.  => " + e.getLocalizedMessage());
			throw e;
		}
		catch (ClientRuntimeException e) {
			String msg = String.format("%s\nMake sure your server is running.", e.getMessage());
			Log.error ("Error creating connection -> " + e.getLocalizedMessage());
			setConnection(new FaultedConnection(connSpec, msg));
		}
		Log.debug ("%s: Using %s", this.getClass().getSimpleName(), conn);
		return conn;
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

	@Override
	public final Context getContext() throws ResourceException {
		return context;
	}

	@Override
	public final void setContext(Context context) throws ResourceException {
		this.context = context;
	}
}
