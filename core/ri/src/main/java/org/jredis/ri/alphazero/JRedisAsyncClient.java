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

import java.util.concurrent.Future;
import org.jredis.ClientRuntimeException;
import org.jredis.ProviderException;
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.connector.FaultedConnection;
import org.jredis.connector.Connection.Modality;
import org.jredis.connector.Connection.Property;
import org.jredis.protocol.Command;
import org.jredis.protocol.Response;
import org.jredis.ri.alphazero.support.Assert;
import org.jredis.ri.alphazero.support.Log;

/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Nov 6, 2009
 * @since   alpha.0
 * 
 */

public class JRedisAsyncClient extends JRedisFutureSupport {
	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------

	/**  */
	final private Connection	connection;
	/**  */
	final protected ConnectionSpec connSpec;

	// ------------------------------------------------------------------------
	// Construct and initialize
	// ------------------------------------------------------------------------

	/**
	 * @param connectionSpec
	 */
	public JRedisAsyncClient (ConnectionSpec connectionSpec) {
		// note: using a shared connection mod
		connSpec = Assert.notNull(connectionSpec, "ConnectionSpec 'connectionSpec'", ClientRuntimeException.class);
		connectionSpec.setConnectionFlag(Connection.Flag.RELIABLE, Boolean.TRUE);  // REVU: TODO: review all these spot mods.
		connectionSpec.setConnectionFlag(Connection.Flag.SHARED, Boolean.FALSE);  // REVU: TODO: review all these spot mods.
		connectionSpec.setConnectionFlag(Connection.Flag.PIPELINE, Boolean.FALSE);  // REVU: TODO: review all these spot mods.
		connectionSpec.setModality(Modality.Asynchronous);
//		connection = new AsyncConnection(connectionSpec, true);
		connection = createAsyncConnection();
	}
	
	// ------------------------------------------------------------------------
	// Internal ops
	// ------------------------------------------------------------------------
	final private Connection createAsyncConnection() {
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
			conn = new FaultedConnection(connSpec, msg);
		}
		Log.debug ("%s: Using %s", this.getClass().getSimpleName(), conn);
		return conn;
	}
	// ------------------------------------------------------------------------
	// Super overrides
	// ------------------------------------------------------------------------
	/**
	 * Requests to server are queued at this point.  Any requests after a {@link Command#QUIT} will
	 * raise an exception indicating the pipeline is shutting down.  
	 * @see org.jredis.ri.alphazero.JRedisFutureSupport#queueRequest(org.jredis.protocol.Command, byte[][])
	 */
	protected  Future<Response> queueRequest (Command cmd, byte[]...args) throws ClientRuntimeException, ProviderException {
		return connection.queueRequest(cmd, args);
	}
}
