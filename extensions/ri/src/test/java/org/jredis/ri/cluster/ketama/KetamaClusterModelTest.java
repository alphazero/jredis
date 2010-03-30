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
import org.jredis.cluster.ClusterSpec;
import org.jredis.cluster.ClusterType;
import org.jredis.cluster.models.ConsistentHashClusterProviderTestBase;
import org.jredis.ri.alphazero.support.Log;
import org.jredis.ri.cluster.DefaultClusterSpec;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * {@link KetamaClusterModel} specific tests.  All generic and C.H. specific tests are
 * defined higher in the hierarchy chain.
 * 
 * <p>Uses the {@link DefaultClusterSpec} for tests.
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 29, 2010
 * 
 */

public class KetamaClusterModelTest extends ConsistentHashClusterProviderTestBase {

	// ------------------------------------------------------------------------
	// super overrides
	// ------------------------------------------------------------------------
	
	/* (non-Javadoc) @see org.jredis.cluster.ClusterModelProviderTestBase#newClusterModel(org.jredis.cluster.ClusterSpec) */
	@Override
	protected ClusterModel newClusterModel (ClusterSpec clusterSpec) {
		return new KetamaClusterModel(clusterSpec);
	}

	/* (non-Javadoc) @see org.jredis.cluster.ClusterModelProviderTestBase#newClusterSpec() */
	@Override
	protected ClusterSpec newClusterSpec () {
		ClusterSpec spec = new DefaultClusterSpec();
		return spec;
	}

	/* (non-Javadoc) @see org.jredis.cluster.ClusterModelProviderTestBase#getSupportedClusterType() */
    @Override
    protected ClusterType getSupportedClusterType () {
	    return ClusterType.CONSISTENT_HASH;
    }
    
	// ------------------------------------------------------------------------
	// Ketama specific tests
	// ------------------------------------------------------------------------
    @Test
    public void fooTest() {
    	Log.log("Foo test for KetamaClusterModel");
    }
}
