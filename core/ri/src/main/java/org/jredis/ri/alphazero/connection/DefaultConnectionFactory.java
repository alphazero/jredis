/*
 *   Copyright 2010 Joubin Houshyar
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

package org.jredis.ri.alphazero.connection;

import java.security.ProviderException;
import org.jredis.ClientRuntimeException;
import org.jredis.NotSupportedException;
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Sep 15, 2010
 * 
 */

public class DefaultConnectionFactory implements Connection.Factory {

	/* (non-Javadoc) @see org.jredis.connector.Connection.Factory#newConnection(org.jredis.connector.ConnectionSpec) */
    public Connection newConnection (ConnectionSpec spec)
            throws ClientRuntimeException, NotSupportedException 
    {
    	Connection conn = null;
    	switch (spec.getModality()){
			case Asynchronous:
				throw new ProviderException("NOT IMPLEMENTED!");
			case Monitor:
				throw new ProviderException("NOT IMPLEMENTED!");
			case PubSub:
				throw new ProviderException("NOT IMPLEMENTED!");
			case Synchronous:
				conn = new SynchConnection(spec);
				break;
    	
    	}
	    return conn;
    }
}
