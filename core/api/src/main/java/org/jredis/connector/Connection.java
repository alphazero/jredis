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

package org.jredis.connector;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.jredis.ClientRuntimeException;
import org.jredis.ProviderException;
import org.jredis.RedisException;
import org.jredis.protocol.Command;
import org.jredis.protocol.Protocol;
import org.jredis.protocol.Response;

public interface Connection {

	/**
	 * Enum for defining the operational modality of the protocol handlers.
	 *   
	 * @author  Joubin Houshyar (alphazero@sensesay.net)
	 * @version alpha.0, 04/02/09
	 * @since   alpha.0
	 * 
	 */
	public enum Modality {
		Synchronous,
		Asynchronous
	}
	
	/**
	 * The {@link ConnectionSpec} of a Connection must be invariant during its life-cycle.
	 * @return the associated {@link ConnectionSpec} for this Connection. 
	 */
	public ConnectionSpec getSpec();
	
	/**
	 * @return the {@link Modality} of this protocol handler.
	 */
	public Modality getModality ();
	
	/**
	 * A <b>blocking call</b> to service the specified request.  This method will return upon 
	 * the completion of the request response protocol with the connected redis server.  Timeouts
	 * and related matters are not addressed by this method or the {@link Protocol} interface
	 * and can (and should) be addressed at the implementation level (for example when creating 
	 * handler instances using a specification set, including max wait for synchronous response.)
	 * 
	 * <p>{@link Modality#Asynchronous} handlers must always throw a {@link ClientRuntimeException}
	 * for <b>this method which violates the contract for</b> {@link Modality#Asynchronous}  <b>handlers</b>.
	 * 
	 * @param cmd
	 * @param args
	 * @return
	 * @throws RedisException
	 * @throws ClientRuntimeException
	 * @throws ProviderException
	 */
	public Response serviceRequest (Command cmd, byte[]...args) throws RedisException, ClientRuntimeException, ProviderException; 
	
	
	/**
	 * A <b>non-blocking call</b> to service the specified request at some point in the future.  
	 * This method will return immediately with a {@link Future} object of parametric type {@link Response}
	 * <p>
	 * When the request is serviced, call to {@link Future#get()} will return the request response.
	 * <p>{@link Modality#Synchronous} handlers must always throw a {@link ClientRuntimeException}
	 * for <b>this method which violates the contract for</b> {@link Modality#Synchronous}  <b>handlers</b>.
	 * <p>
	 * If request resulted in a redis error ({@link RedisException}), the exception will be set as the cause of
	 * the corresponding {@link ExecutionException} of the {@link Future} object returned.
	 * @param cmd
	 * @param args
	 * @return the {@link Future} {@link Response}.
	 * @throws ClientRuntimeException
	 * @throws ProviderException
	 * @see Future
	 * @see ExecutionException
	 */

	public Future<Response> queueRequest (Command cmd,  byte[]...args) throws ClientRuntimeException, ProviderException;
	
	// ========================================================================
	// Innner Types
	// ========================================================================
	
	/**
	 * [TODO: document me!]
	 *
	 * @author  Joubin Houshyar (alphazero@sensesay.net)
	 * @version alpha.0, Mar 29, 2010
	 * @since   alpha.0
	 * 
	 */
	public interface Listener {
		public void onEvent(Connection.Event event);
	}
	
	/**
	 * [TODO: document me!]
	 *
	 * @author  Joubin Houshyar (alphazero@sensesay.net)
	 * @version alpha.0, Mar 29, 2010
	 * @since   alpha.0
	 * 
	 */
	@SuppressWarnings("serial")
    final public static class Event extends org.jredis.Event<Connection, Event.Type, Object>{
		/**
         * @param src
         * @param type
         */
        public Event (Connection src, Type type) {
	        super(src, type);
        }
        public Event (Connection src, Type type, Object eventInfo) {
	        super(src, type, eventInfo);
        }

		public enum Type {
			Established,
			Dropped,
			Faulted
		}
	}
}
