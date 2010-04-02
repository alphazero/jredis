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



/**
 * Used by JRedis implementation providers to indicate an exception related to the
 * implementation of the specification.  Effectively, this exception and its extensions
 * should only be thrown when a fault has occurred that is neither a {@link SystemException},
 * nor a Redis prompted server side error.
 * 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 * 
 */
public class ProviderException extends ClientRuntimeException {

	/** */
	public static final long version = _specification.Version.major;

	/**  */
	private static final long	serialVersionUID	= _specification.Version.major;
	

	/**
	 * @param message
	 */
	public ProviderException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ProviderException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
