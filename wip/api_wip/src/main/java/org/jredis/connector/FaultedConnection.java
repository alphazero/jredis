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

/**
 * [TODO: document me!][NOTE: disabling @Override annotations for 1.5 compliance.]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 11, 2009
 * @since   alpha.0
 * 
 */
public class FaultedConnection implements Connection {

	private String	errorMsg;

	public FaultedConnection (String errMsg) {
		this.errorMsg = errMsg;
	}
//	@Override
	public Modality getModality() 
	{
		throw new ClientRuntimeException (errorMsg);
	}

//	@Override
	public Response serviceRequest(Command cmd, byte[]... args) throws RedisException, ClientRuntimeException,
			ProviderException 
	{
		throw new ClientRuntimeException (errorMsg);
	}

//	@Override
	public Response serviceRequest(RequestListener requestListener, Command cmd, byte[]... args)
			throws RedisException, ClientRuntimeException, ProviderException 
	{
		throw new ClientRuntimeException (errorMsg);
	}

}
