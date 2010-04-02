package org.jredis.resource;

import org.jredis.ClientRuntimeException;

/**
 * The overall superclass for resource related exceptions, itself an 
 * extension of ClientRuntimeException.
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 16, 2009
 * @since   alpha.0
 * 
 */
public class ResourceException extends ClientRuntimeException {


	/**
	 * @param message
	 * @param cause
	 */
	public ResourceException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public ResourceException(String message) {
		super(message);
	}

	/**  */
	private static final long serialVersionUID = _specification.Version.major;

}