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

package org.jredis.protocol;

import org.jredis.ClientRuntimeException;
import org.jredis.NotSupportedException;
import org.jredis.ProviderException;
import org.jredis.connector.ConnectionSpec;
import org.jredis.connector.Message;


/**
 * <p>
 * Protocol is effectively a {@link Message} factory.  Implementations of this interface
 * provides Message objects that read and write according to a specific Redis protocol
 * specification.
 * 
 * <p>Implementations may use this interface to address issues regarding buffer management.
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 * 
 */
public interface Protocol {
	
	/**
	 * 
	 * @param version
	 * @return
	 */
	public boolean isCompatibleWithVersion (String version);
	
	/**
	 * 
	 * @param cmd
	 * @param args
	 * @return
	 * @throws ProviderException
	 * @throws IllegalArgumentException
	 */
	public Request createRequest (Command cmd, byte[]...args) throws ProviderException, IllegalArgumentException;

	/**
	 * Creates a response object for the {@link Command} specified.  
	 * <p><b>Note</b> that this {@link Response} object has not yet been read.
	 * @param cmd the {@link Command} that will be responded to.
	 * 
	 * @return the response object that is ready to be read from the network connection.
	 * 
	 * @throws ClientRuntimeException if the command is invalid for this version of the protocol
	 * @throws ProviderException if the command is not implemented
	 * 
	 * @see Response
	 * @See {@link Response#read(java.io.InputStream)}
	 */
	public Response createResponse (Command cmd) throws ProviderException, ClientRuntimeException ;

	/**
	 * EXPERIMENTAL 
	 * @param cmd
	 * @param args
	 * @return
	 * @throws ProviderException
	 * @throws IllegalArgumentException
	 */
	public byte[] createRequestBuffer(Command cmd, byte[]...args) throws ProviderException, IllegalArgumentException;
	
	public interface Factory {
		/**
		 * Creates a {@link Protocol} instance for a connection per the specified
		 * {@link ConnectionSpec}
		 * @param connSpec
		 * @return the new {@link Protocol} instance.
		 * @throws NotSupportedException
		 */
		public Protocol newProtocol(ConnectionSpec connSpec) throws NotSupportedException;
	}

}
