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

package org.jredis.cluster;

import org.jredis.JRedis;
import org.jredis.JRedisFuture;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 26, 2010
 * 
 */

public class JRedisClusterSupport implements JRedisCluster {

	/* (non-Javadoc) @see org.jredis.cluster.JRedisCluster#getAsychInterface() */
	@Override
	public JRedisFuture getAsychInterface () {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc) @see org.jredis.cluster.JRedisCluster#getClusterSpec() */
	@Override
	public ClusterSpec getClusterSpec () {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc) @see org.jredis.cluster.JRedisCluster#getSynchInterface() */
	@Override
	public JRedis getSynchInterface () {
		// TODO Auto-generated method stub
		return null;
	}

}
