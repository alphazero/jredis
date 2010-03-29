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

package org.jredis.ri.cluster;

import java.util.Map;
import java.util.concurrent.Future;
import org.jredis.ClientRuntimeException;
import org.jredis.JRedis;
import org.jredis.JRedisFuture;
import org.jredis.NotSupportedException;
import org.jredis.ProviderException;
import org.jredis.RedisException;
import org.jredis.cluster.ClusterModel;
import org.jredis.cluster.ClusterNodeSpec;
import org.jredis.cluster.ClusterSpec;
import org.jredis.cluster.deprecated.ClusterModel_deprecated;
import org.jredis.cluster.deprecated.JRedisCluster;
import org.jredis.connector.Connection;
import org.jredis.protocol.Command;
import org.jredis.protocol.Response;
import org.jredis.ri.alphazero.JRedisFutureSupport;
import org.jredis.ri.alphazero.JRedisSupport;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 26, 2010
 * 
 */

public class JRedisClusterSupport extends JRedisSupport
//	implements JRedisCluster 
{
	// constructor should take cluster spec, and cluster model

	/* (non-Javadoc) @see org.jredis.ri.alphazero.JRedisSupport#serviceRequest(org.jredis.protocol.Command, byte[][]) */
    @Override
    protected Response serviceRequest (Command cmd, byte[]... args)
            throws RedisException, ClientRuntimeException, ProviderException 
    {
    	// filter out the unsupported commands
    	//
    	switch (cmd.requestType){
			case BULK_SET:
			case NO_ARG:
			case VALUE:
				// alternatively, a subset of the above can just be
				// issued to a random server ?
				throw new NotSupportedException(cmd.name());

			case KEY:
			case KEY_CNT_VALUE:
			case KEY_IDX_VALUE:
			case KEY_KEY:
			case KEY_KEY_VALUE:
			case KEY_NUM:
			case KEY_NUM_NUM:
			case KEY_NUM_NUM_OPTS:
			case KEY_SPEC:
			case KEY_VALUE:
			case MULTI_KEY:
				break;
    	}
    	// get key
    	//
		byte[] key = args[0];
    	
    	// find node and delegate request to associated connection
    	//
		ClusterModel cluster = null;
		ClusterNodeSpec nodeSpec = cluster.getNodeForKey(key);
		String nodeId = nodeSpec.getId();
		Map<String, Connection> connections = null;
		Connection conn = connections.get(nodeId);
		
		return conn.serviceRequest(cmd, args);
    }
}
