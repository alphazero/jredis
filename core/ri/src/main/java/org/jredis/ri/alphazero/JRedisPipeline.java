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
import org.jredis.JRedisFuture;
import org.jredis.ProviderException;
import org.jredis.Redis;
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.protocol.Command;
import org.jredis.protocol.Response;
import org.jredis.ri.alphazero.connection.AsynchPipelineConnection;

/**
 * Asynchronous Redis client implementing {@link JRedisFuture} and using 
 * an {@link AsynchPipelineConnection} for command processing.
 * <p>
 * TODO: details the usage and characteristics.
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Sep 21, 2009
 * @since   alpha.0
 * 
 */

@Redis(versions={"1.00"})
public class JRedisPipeline extends JRedisFutureSupport {

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------

	/**  */
	final private Connection	connection;

	// ------------------------------------------------------------------------
	// Construct and initialize
	// ------------------------------------------------------------------------

	/**
	 * @param coonectionSpec
	 */
	public JRedisPipeline (ConnectionSpec coonectionSpec) {
		// note: using a non shared connection mod
		connection = new AsynchPipelineConnection(coonectionSpec);
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
