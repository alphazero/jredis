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

package org.jredis.cluster.model;

import org.jredis.cluster.ClusterModel;
import org.jredis.cluster.ClusterSpec;
import org.jredis.cluster.ClusterType;

/**
 * Generalized interface for a {@link ClusterModel} using a Consistent Hashing strategy,
 * per the original paper by Karger, et al [1].
 * <p>
 * Requirements:
 * <li>Implementors must return {@link ClusterType#CONSISTENT_HASH} for {@link ClusterSpec#getType()}
 * 
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 30, 2010
 * @see [1]: <a href="http://www.akamai.com/dl/technical_publications/ConsistenHashingandRandomTreesDistributedCachingprotocolsforrelievingHotSpotsontheworldwideweb.pdf">Consistent Hashing and Random Trees</a>
 */

public interface ConsistentHashCluster extends ClusterModel {
	ClusterNodeMap getNodeMap ();
}
