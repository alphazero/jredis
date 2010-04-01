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

package org.jredis.cluster.models;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import org.jredis.cluster.ClusterModelProviderTestBase;
import org.jredis.cluster.ClusterSpec;
import org.jredis.cluster.ClusterType;
import org.jredis.cluster.model.StaticHashCluster;
import org.jredis.ri.alphazero.support.Log;
import org.testng.annotations.Test;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 31, 2010
 * 
 */

public abstract class StaticHashClusterProviderTestBase extends ClusterModelProviderTestBase 
{
	/* (non-Javadoc) @see org.jredis.cluster.ClusterModelProviderTestBase#getSupportedClusterType() */
	@Override
	protected final ClusterType getSupportedClusterType () {
		return ClusterType.CONSISTENT_HASH;
	}

	// ------------------------------------------------------------------------
	// StaticHashCluster generic tests
	// ------------------------------------------------------------------------
    @Test
    public void compatibilityTest() {
    	
    	Log.log("Test provider support for Static Hashing");
    	assertNotNull(provider, "provider is null!");
    	assertTrue(provider.supports(ClusterType.STATIC_HASH), "A StaticHashCluster model must support Type.CONSISTENT_HASH");
    }
    @Test
    public void basicNodeMapTest() {
    	Log.log("Basic nodemap test of Consistent Hashing cluster model");
    	
    	StaticHashCluster model = (StaticHashCluster) newProviderInstance();
    	assertNotNull(model, "model should not be null");
    	
    	ClusterSpec spec = model.getSpec();
    	assertNotNull(spec, "spec should not be null");
    	
    	assertFalse(model.supportsReconfiguration(), "Static hash clusters can not be reconfigured.");
    }
}
