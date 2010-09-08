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

import java.util.Collection;
import org.jredis.cluster.ClusterModelProviderTestBase;
import org.jredis.cluster.ClusterNodeSpec;
import org.jredis.cluster.ClusterSpec;
import org.jredis.cluster.ClusterType;
import org.jredis.cluster.model.ConsistentHashCluster;
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

	/* (non-Javadoc) @see org.jredis.cluster.ClusterModelProviderTestBase#getSupportedClusterType() */
    @Override
    protected ClusterType getSupportedClusterType () {
	    return ClusterType.CONSISTENT_HASH;
    }
	// ------------------------------------------------------------------------
	// ConsistentHashCluster generic tests
	// ------------------------------------------------------------------------
    @Test
    public void compatibilityTest() {
    	
    	Log.log("Test provider support for Consistent Hashing");
    	assertNotNull(provider, "provider is null!");
    	assertTrue(provider.supports(ClusterType.CONSISTENT_HASH), "A ConsistentHashCluster model must support Type.CONSISTENT_HASH");
    }
    
    @Test
    public void basicNodeMapTest() {
    	Log.log("Basic nodemap test of Consistent Hashing cluster model");
    	
    	ConsistentHashCluster model = (ConsistentHashCluster) newProviderInstance();
    	assertNotNull(model, "model should not be null");
    	
    	ClusterSpec spec = model.getSpec();
    	assertNotNull(spec, "spec should not be null");
    	
    	ConsistentHashCluster.NodeMap nodeMap = model.getNodeMap();
    	assertNotNull(nodeMap, "node map should not be null");
    	
    	Collection<ClusterNodeSpec> nodes = nodeMap.values();
    	assertNotNull(nodes, "value set of node map should not be null");
    	
    	// regardless of what hash (key) they are mapped to,
    	// we expected the value set of NodeMap to contain each and every
    	// ClusterNodeSpec in the ClusterSpec of the model
    	//
		Log.log("NOTE: %d nodes", spec.getNodeSpecs().size());
		Log.log("NOTE: size of value set is %d ", nodes.size());
		int missCnt = 0;
    	for(ClusterNodeSpec node : spec.getNodeSpecs()){
    		if(!nodes.contains(node)){
    			missCnt ++;
    		}
//    		assertTrue(nodes.contains(node), "nodeSpec should be in the value set of NodeMap: " + node);
    	}
		Log.log("BUG: missing %d nodes!", missCnt);
		assertEquals(missCnt, 0, "There should be no nodes missing from the node map");
    }
}
