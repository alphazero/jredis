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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jredis.ClientRuntimeException;
import org.jredis.ProviderException;


/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 * 
 */
public interface Message {
	
	/**
	 * Reads itself from the provided {@link InputStream}
	 * 
	 * @param in the steam to read from.
	 * 
	 * @throws ClientRuntimeException to indicate a system error, potentially network related
	 * and hopefully recoverable.  Typically used to wrap and propagate the IO stream's thrown
	 * {@link IOException}s.
	 * 
	 * @throws ProviderException to indicate operational error not directly related to the stream,
	 * and should be treated as a bug.
	 */
	public void	read (InputStream    in) throws ClientRuntimeException, ProviderException; 
	
	/**
	 * Writes itself to the provided {@link OutputStream}.
	 * 
	 * @param out the stream to write to.
	 * 
	 * @throws ClientRuntimeException to indicate a system error, potentially network related
	 * and hopefully recoverable.  Typically used to wrap and propagate the IO stream's thrown
	 * {@link IOException}s.
	 * 
	 * @throws ProviderException to indicate operational error not directly related to the stream,
	 * and should be treated as a bug.
	 */
	public void	write (OutputStream  out)throws ClientRuntimeException, ProviderException; 
}
