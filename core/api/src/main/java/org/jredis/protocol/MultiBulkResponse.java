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

import java.io.InputStream;
import java.util.List;

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
public interface MultiBulkResponse extends Response {
	/**
	 * @return the List of values returned from the server.  List may contain null elements,
	 * reflecting '<b>nil</b>' values per redis specification.  The operation {@link List#size()} will return
	 * the same number that is received from the server, with the exception
	 *  
	 * @throws ClientRuntimeException if data access is attempted before the response has been read, or,
	 * if the provided {@link InputStream} presents any problems.
	 *   
	 * @throws ProviderException for any other errors beyond system level (stream, network, etc.) or
	 * user errors (such as attempting getData before the response has been read.  
	 */
	public List<byte[]>		getMultiBulkData () throws ClientRuntimeException, ProviderException;
}
