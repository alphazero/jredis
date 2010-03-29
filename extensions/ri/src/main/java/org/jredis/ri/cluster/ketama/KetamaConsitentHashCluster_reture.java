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

import org.jredis.cluster.ClusterSpec;
import org.jredis.cluster.deprecated.ClusterKeyMapper;
import org.jredis.cluster.deprecated.ClusterModel_deprecated;
import org.jredis.cluster.deprecated.ClusterNodeMapper;
import org.jredis.cluster.support.HashAlgorithm;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 28, 2010
 * 
 */

public class KetamaConsitentHashCluster_reture extends ClusterModel_deprecated.Support implements ClusterModel_deprecated {

	public KetamaConsitentHashCluster_reture (ClusterSpec clusterSpec) {
		super (clusterSpec);
	}
	/* (non-Javadoc) @see org.jredis.cluster.ClusterStrategy.Support#newHashAlgorithm() */
    @Override
    protected HashAlgorithm newHashAlgorithm () {
	    return new KetamaHashProvider();
    }

	/* (non-Javadoc) @see org.jredis.cluster.ClusterStrategy.Support#newKeyMapper() */
    @Override
    protected ClusterKeyMapper newKeyMapper () {
	    return new KetamaKeyMapper(this);
    }

	/* (non-Javadoc) @see org.jredis.cluster.ClusterStrategy.Support#newNodeMapper() */
    @Override
    protected ClusterNodeMapper newNodeMapper () {
	    return new KetamaNodeMapper(this.clusterSpec);
    }
}
