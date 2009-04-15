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

import org.jredis.ClientRuntimeException;
import org.jredis.Command;
import org.jredis.RedisException;

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
	 * A <b>non-blocking call</b> to service the specified request.  This method will return immediately.
	 * 
	 * <p>
	 * Upon the completion of the request response protocol with the connected redis server, the
	 * {@link RequestListener} registered in this call will receive a notification.  Support for this
	 * method is required for all {@link Modality#Asynchronous}
	 * 
	 * <p>{@link Modality#Synchronous} handlers must always throw a {@link ClientRuntimeException}
	 * for <b>this method which violates the contract for</b> {@link Modality#Synchronous}  <b>handlers</b>.
	 * 
	 * @param cmd
	 * @param args
	 * @return
	 * @throws RedisException
	 * @throws ClientRuntimeException
	 * @throws ProviderException
	 */
	public Response serviceRequest (RequestListener requestListener,  Command cmd, byte[]...args) throws RedisException, ClientRuntimeException, ProviderException; 
}
