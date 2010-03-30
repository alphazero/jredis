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

import org.jredis.cluster.ClusterModelProviderTestBase;
import org.jredis.cluster.ClusterType;
import org.jredis.ri.alphazero.support.Log;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 30, 2010
 * 
 */

public abstract class ConsistentHashClusterProviderTestBase extends ClusterModelProviderTestBase {

	// ------------------------------------------------------------------------
	// ConsistentHashCluster specific tests
	// ------------------------------------------------------------------------
    @Test
    public void compatibilityTest() {
    	
    	Log.log("Test provider support for Consistent Hashing");
    	assertNotNull(provider, "provider is null!");
    	assertTrue(provider.supports(ClusterType.CONSISTENT_HASH), "A ConsistentHashCluster model must support Type.CONSISTENT_HASH");
    }
}
