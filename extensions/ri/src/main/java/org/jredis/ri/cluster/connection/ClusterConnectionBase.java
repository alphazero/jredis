/*
 *   Copyright 2009-2010 Joubin Houshyar
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

package org.jredis.ri.cluster.connection;

import java.util.concurrent.Future;
import org.jredis.ClientRuntimeException;
import org.jredis.ProviderException;
import org.jredis.RedisException;
import org.jredis.connector.Connection;
import org.jredis.protocol.Command;
import org.jredis.protocol.Response;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 29, 2010
 * 
 */

public class ClusterConnectionBase implements Connection {

	/* (non-Javadoc) @see org.jredis.connector.Connection#getModality() */
	public Modality getModality () {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc) @see org.jredis.connector.Connection#queueRequest(org.jredis.protocol.Command, byte[][]) */
	public Future<Response> queueRequest (Command cmd, byte[]... args) throws ClientRuntimeException, ProviderException {
		return null;
	}

	/* (non-Javadoc) @see org.jredis.connector.Connection#serviceRequest(org.jredis.protocol.Command, byte[][]) */
	public Response serviceRequest (Command cmd, byte[]... args) throws RedisException, ClientRuntimeException, ProviderException {
		return null;
	}
}
