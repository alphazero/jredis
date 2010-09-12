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

import java.net.SocketException;

import org.jredis.ClientRuntimeException;
import org.jredis._specification;

/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 15, 2009
 * @since   alpha.0
 * 
 */

abstract public class ConnectionException extends ClientRuntimeException{

	/**  */
	private static final long serialVersionUID = _specification.Version.major;
	
	/**
	 * TODO: not sure if specifying {@link SocketException} is a good idea.
	 * @param msg
	 * @param e
	 */
	public ConnectionException(String msg, SocketException e) {
		super(msg, e);
	}

	/**
	 * @param msg
	 */
	public ConnectionException(String msg) {
		super(msg);
	}
}
