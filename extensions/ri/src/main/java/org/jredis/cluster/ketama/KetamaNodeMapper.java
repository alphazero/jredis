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

package org.jredis.cluster.ketama;

import org.jredis.cluster.ClusterNodeMapper;
import org.jredis.cluster.ClusterNodeSpec;
import org.jredis.cluster.ClusterSpec;
import org.jredis.cluster.NodeMappingAlgorithm;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 25, 2010
 * 
 */

public class KetamaNodeMapper extends ClusterNodeMapper.Support implements ClusterNodeMapper {

	
	/**
     * @param clusterSpec
     */
    public KetamaNodeMapper (ClusterSpec clusterSpec) {
	    super(clusterSpec);
    }

	/* (non-Javadoc) @see org.jredis.cluster.ClusterNodeMapper#getPrimary(byte[]) */
	@Override
	public ClusterNodeSpec getPrimary (byte[] key) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc) @see org.jredis.cluster.ClusterNodeMapper#getSecondary(byte[]) */
	@Override
	public ClusterNodeSpec getSecondary (byte[] key) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc) @see org.jredis.cluster.ClusterNodeMapper.Support#newMappingAlgorithm() */
    @Override
    protected NodeMappingAlgorithm newMappingAlgorithm () {
	    return new KetamaNodeMappingAlgorithm();
    }
}
