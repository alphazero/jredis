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

package org.jredis.ri.cluster.ketama;

import org.jredis.cluster.ClusterModel;
import org.jredis.cluster.ClusterNodeSpec;
import org.jredis.cluster.ClusterSpec;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 28, 2010
 * 
 */

public class KetamaCluster extends ClusterModel.Support implements ClusterModel {

	/**
     * @param clusterSpec
     */
    public KetamaCluster (ClusterSpec clusterSpec) {
	    super(clusterSpec);
    }

    @Override
    protected boolean onNodeAddition (ClusterNodeSpec newNode) {
	    return false;
    }

    @Override
    protected boolean onNodeRemoval (ClusterNodeSpec newNode) {
	    return false;
    }

    public ClusterNodeSpec getNodeForKey (byte[] key) {
	    return null;
    }
}
