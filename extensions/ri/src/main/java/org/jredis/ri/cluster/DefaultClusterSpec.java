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

import org.jredis.cluster.ClusterSpec;
import org.jredis.cluster.HashAlgorithm;
import org.jredis.cluster.NodeMappingAlgorithm;
import org.jredis.cluster.ClusterSpec.Support;
import org.jredis.ri.cluster.ketama.KetamaHashProvider;
import org.jredis.ri.cluster.ketama.KetamaNodeMappingAlgorithm;

/**
 * The default ClusterSpec uses the {@link KetamaHashProvider} as its {@link HashAlgorithm}. 
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 25, 2010
 * 
 */

public class DefaultClusterSpec extends Support implements ClusterSpec {
	
	public DefaultClusterSpec () {
		super();
	}

	// ------------------------------------------------------------------------
	// public interface
	// ------------------------------------------------------------------------
	
	// ------------------------------------------------------------------------
	// super overrides
	// ------------------------------------------------------------------------
	/* (non-Javadoc) @see org.jredis.cluster.ClusterSpec.Support#newHashProvider() */
    @Override
    protected HashAlgorithm newHashAlgorithm () {
		return new KetamaHashProvider();
    }

	/* (non-Javadoc) @see org.jredis.cluster.ClusterSpec.Support#newNodeMappingAlgorithm() */
    @Override
    protected NodeMappingAlgorithm newNodeMappingAlgorithm () {
	    return new KetamaNodeMappingAlgorithm();
    }
}
