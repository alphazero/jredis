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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jredis.ClientRuntimeException;
import org.jredis.JRedis;
import org.jredis.JRedisFuture;
import org.jredis.ProviderException;
import org.jredis.RedisException;
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.protocol.Command;
import org.jredis.protocol.Response;
import org.jredis.ri.alphazero.connection.AsyncPipelineConnection;

/**
 * Asynchronous Redis client implementing {@link JRedisFuture} and using 
 * an {@link AsyncPipelineConnection} for command processing.
 * <p>
 * TODO: details the usage and characteristics.
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Sep 21, 2009
 * @since   alpha.0
 * 
 */

//@Redis(versions={"1.00"})
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
	 * @param connectionSpec
	 */
	public JRedisPipeline (ConnectionSpec connectionSpec) {
		// note: using a non shared connection mod
		connection = new AsyncPipelineConnection(connectionSpec);
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
	
	// ------------------------------------------------------------------------
	// public interface
	// ------------------------------------------------------------------------
	/**
	 * Provides a synchronous semantics interface ({@link JRedis}) to this pipeline.
	 * Note that this is <b>not a thread-safe mechanism</b>.  If you need a pipeline
	 * connection with support for concurrent synchronous semantics, use a {@link JRedisPipelineService}.
	 *  
	 * @return the {@link JRedis} interface for this pipeline.
	 */
	public JRedis sync () {
		return new JRedisSupport() {
			@Override
            protected Response serviceRequest (Command cmd, byte[]... args) throws RedisException, ClientRuntimeException, ProviderException {
				Response response = null;
				try {
	                response = JRedisPipeline.this.queueRequest(cmd, args).get();
                }
                catch (InterruptedException e) {
	                throw new ClientRuntimeException("Interrupted!", e);
                }
                catch (ExecutionException e) {
                	Throwable cause = e.getCause();
                	if(cause instanceof RedisException)
                		throw (RedisException) cause;
                	else if(cause instanceof ProviderException)
                		throw (ProviderException) cause;
                	else if(cause instanceof ClientRuntimeException) 
                		throw (ClientRuntimeException)cause;
                	else throw new ClientRuntimeException("Exception in pipeline exec of requested command", cause);
                }
                return response;
            }
		};
	}
	/**
	 * 
	 * @param timeout
	 * @param unit
	 * @return
	 */
	public JRedis sync (final long timeout, final TimeUnit unit) {
		return new JRedisSupport() {
			@Override
            protected Response serviceRequest (Command cmd, byte[]... args) throws RedisException, ClientRuntimeException, ProviderException {
				Response response = null;
				try {
	                response = JRedisPipeline.this.queueRequest(cmd, args).get(timeout, unit);
                }
                catch (InterruptedException e) {
	                throw new ClientRuntimeException("Interrupted!", e);
                }
                catch (TimeoutException e) {
	                throw new ClientRuntimeException("timedout waiting for response");
                }
                catch (ExecutionException e) {
                	Throwable cause = e.getCause();
                	if(cause instanceof RedisException)
                		throw (RedisException) cause;
                	else if(cause instanceof ProviderException)
                		throw (ProviderException) cause;
                	else if(cause instanceof ClientRuntimeException) 
                		throw (ClientRuntimeException)cause;
                	else throw new ClientRuntimeException("Exception in pipeline exec of requested command", cause);
                }
                return response;
            }
		};
	}
}
