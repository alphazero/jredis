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

package org.jredis.ri.alphazero.protocol;

import org.jredis.NotSupportedException;
import org.jredis.connector.ConnectionSpec;
import org.jredis.protocol.Protocol;
import org.jredis.connector.Connection;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Sep 13, 2010
 * 
 */

public class DefaultProtocolFactory implements Protocol.Factory {

	/* (non-Javadoc) @see org.jredis.protocol.Protocol.Factory#newProtocol(org.jredis.connector.ConnectionSpec) */
    public Protocol newProtocol (ConnectionSpec connSpec) throws NotSupportedException {
    	/* 
    	 * TODO:
    	 * check various Connection.Property/Spec keys.  
    	 * 
    	 */
		return connSpec.getConnectionFlag(Connection.Flag.SHARED) ? new ConcurrentSyncProtocol() : new SyncProtocol();
    }

}
