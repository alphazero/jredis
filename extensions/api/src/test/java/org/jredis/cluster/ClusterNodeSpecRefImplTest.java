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

package org.jredis.cluster;

import java.util.HashSet;
import java.util.Set;
import org.jredis.ri.alphazero.support.Log;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 24, 2010
 */
@Test(suiteName="api-spec-refimpl-tests")
public class ClusterNodeSpecRefImplTest extends RefImplTestSuiteBase {
	
	/**
	 * We have the full port range of an IPv4 address as our clusterNodes.  Here
	 * we make sure the ids are unique.
	 */
	@Test
	public void testIdGeneration () {
		Log.log("Testing ClusterNodeSpec.getId() ...");
		Set<String> generatedIdSet = new HashSet<String>(this.clusterNodeSpecs.size());
		Log.log("... cluster list member cnt: " + this.clusterNodeSpecs.size());
		for(ClusterNodeSpec nodeSpec : clusterNodeSpecs){
			String nodeId = nodeSpec.getId();
			assertTrue(generatedIdSet.add(nodeId), "generated ID should be unique but was not: " + nodeId);
		}
	}
	
	/**
	 * We test the Consitent Hash Key for uniqueness.  Testing the full port range * reasonable_inst_cnt will
	 * exhaust the memory so we'll limit to a subset of ports.
	 */
	@Test
	public void testGetKeyForCHRangeInstance () {
		int instanceCnt = 100;
		int nodeCnt = 100;
		Log.log("Testing CHRange key uqniueness for "+instanceCnt+" instances in the ring... this will take a while! (TODO: cnt should be a parameter!)");
		Set<String> chRangeKeys = new HashSet<String>(nodeCnt*instanceCnt);
		Log.log("... cluster list member cnt: " + this.clusterNodeSpecs.size());
		for(int n = 0; n<100; n++){
			ClusterNodeSpec nodeSpec = clusterNodeSpecs.get(n);
			for(int i=0; i<instanceCnt; i++) {
				String nodeInstanceCHKey = nodeSpec.getKeyForCHRangeInstance(i);
				assertTrue(chRangeKeys.add(nodeInstanceCHKey), "generated ConsistentHash Key for node "+nodeSpec.getId()+" for instance "+i+" should be unique but was not: " + nodeInstanceCHKey);
			}
		}
	}
}
