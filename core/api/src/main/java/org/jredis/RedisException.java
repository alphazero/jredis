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

package org.jredis;

import org.jredis.protocol.Command;

//import org.jredis.connector.ProviderException;

/**
 * [TODO: update doc regarding "extensions"]
 * 
 * RedisExceptions are only created/raised subsequent to an error result from the redis server.
 * These exceptions are <b>_not_</b> intended as a general purpose exception mechanism for
 * implementations of this specification.  Accordingly, this exception (and extensions if any)
 * do not (and must not) provide default no arg constructors.  (Minimal information required is
 * an associated {@link Command} object.)  
 * 
 * <p>Further, this class (and extension if any) should never expose a constructor accepting 
 * an underlying 'cause' (as in {@link Exception#Exception(Throwable)}), since this excpetion,
 * to repeat, has very specific semantics:  <b>Redis has responded with an error 
 * status to an issued command and the content of that message (e.g. "-ERR operation on wrong type"</b>
 * is what is required for instantiating a {@link RedisException}.
 * 
 * @see ClientRuntimeException
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 * 
 */
public final class RedisException extends Exception {

	/**  */
	public static final long version = _specification.Version.major;

	/**  */
	private static final long	serialVersionUID = version;

	/**  */
	private final Command command;
	
//	public RedisException (String message) {
//		super (message);
//	}
	
	public RedisException (Command command, String message){
		super (message);
		this.command = command;
	}

	/**  @return the associated {@link Command} if any */
	public Command getCommand () { return command; }
	
	public String toString () {
		return "Exception on [" + command.code + "] => " + getMessage(); 
	}
}
