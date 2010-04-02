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
 * Base class for all non-Redis exceptions relating to client runtime.  Implementations
 * must only throw this type of exception when the problem(s) encountered are neither Redis usage errors, nor 
 * unexpected code segment execution.
 * <p>
 * For example, failure to establish a connection, or losing the connection, should raise this type of exception.  But
 * encountering parse errors in Redis responses streams is a bug and should be noted by raising a {@link ProviderException}.
 * 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 * 
 */
public class ClientRuntimeException extends RuntimeException {

	/**  */
	private static final long	serialVersionUID	= _specification.Version.major;
	
	/**
	 * @param message
	 */
	public ClientRuntimeException(String message) {
		super (message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ClientRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
	
	// ------------------------------------------------------------------------
	//	Super overrides
	// ------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see java.lang.Throwable#getLocalizedMessage()
	 */
	@Override
    final public String getLocalizedMessage () {
		return this.getMessage();
    }

	/* (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
    final public String getMessage () {
		StringBuffer buff = new StringBuffer();
		
		String message = super.getMessage();
		if(null == message) buff.append("[BUG: null message]");
		else buff.append(message);
		
		Throwable cause = getCause();
		if(null != cause) buff.append(" cause: => [").append(cause.getClass().getSimpleName()).append(": ").append(cause.getMessage()).append("]");
		
		return buff.toString();
    }
}
