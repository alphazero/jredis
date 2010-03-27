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

import java.util.SortedMap;
import org.jredis.JRedis;
import org.jredis.JRedisFuture;
import org.jredis.cluster.ClusterNodeSpec;
import org.jredis.cluster.ClusterSpec;
import org.jredis.cluster.JRedisCluster;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 26, 2010
 * 
 */

public class JRedisClusterSupport implements JRedisCluster {

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------

	/** {@link ClusterSpec} */
	protected final ClusterSpec clusterSpec;
	
	/** Initialized at instantiation using the info/algos of associated ClusterSpec */
	protected final SortedMap<Long, ClusterNodeSpec> clusterMap;
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------
	
	protected JRedisClusterSupport (ClusterSpec clusterSpec) {
		this.clusterSpec = clusterSpec;
		this.clusterMap = clusterSpec.getNodeMappingAlgorithm().mapNodes(clusterSpec);
	}
	
	// ------------------------------------------------------------------------
	// Extension point(s)
	/*
	 * [TODO]
	 */
	// ------------------------------------------------------------------------
	
	// ???
	
	// ------------------------------------------------------------------------
	// INTERFACE
	// ========================================================== JRedisCluster
	/*
	 * TODO: doc this
	 */
	// ------------------------------------------------------------------------
	
	/* (non-Javadoc) @see org.jredis.cluster.JRedisCluster#getAsychInterface() */
//	@Override
	public JRedisFuture getAsychInterface () {
		throw new RuntimeException("not implemented!");
	}

	/* (non-Javadoc) @see org.jredis.cluster.JRedisCluster#getClusterSpec() */
//	@Override
	final public ClusterSpec getClusterSpec () {
		return this.clusterSpec;
	}

	/* (non-Javadoc) @see org.jredis.cluster.JRedisCluster#getSynchInterface() */
//	@Override
	public JRedis getSynchInterface () {
		throw new RuntimeException("not implemented!");
	}
}
