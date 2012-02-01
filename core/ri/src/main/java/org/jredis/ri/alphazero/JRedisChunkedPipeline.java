/*
 *   Copyright 2009-2012 Joubin Houshyar
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
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.protocol.Command;
import org.jredis.protocol.Response;
import org.jredis.ri.alphazero.connection.ChunkedPipelineConnection;

/**
 * An asynchronous pipeline supporting {@link JRedisFuture} api,
 * utilizing the {@link ChunkedPipelineConnection}, providing 
 * maximal throughput as a forwarding pipe to Redis server.
 * 
 * Thread safe.  Use only one instance per application.
 * 
 *   
 * @author Joubin <alphazero@sensesay.net>
 *
 */
public class JRedisChunkedPipeline extends JRedisFutureSupport {

	final private Connection	connection;
	public JRedisChunkedPipeline(ConnectionSpec spec){
		assert spec != null : "spec is null";
		connection = new ChunkedPipelineConnection(spec);
	}
	@Override
	protected Future<Response> queueRequest(Command cmd, byte[]... args)
			throws ClientRuntimeException, ProviderException 
	{
		return connection.queueRequest(cmd, args);
	}
}
