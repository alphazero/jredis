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
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import org.jredis.ri.alphazero.support.Log;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 24, 2010
 */
@Test(suiteName="api-spec-refimpl-tests")
public class ClusterNodeSpecRefImplTest extends RefImplTestSuiteBase {
	
	@Test
	public void testIdentityContract () {
		Log.log("Testing ClusterNodeSpec identity contract enforcement: [Object.equals() | Object.hashCode()]");
		int db = 10;
		int anotherDb = 2;
		ConnectionSpec node1Spec = DefaultConnectionSpec.newSpec("127.0.0.1", 6379, db, null);
		ConnectionSpec node2Spec = DefaultConnectionSpec.newSpec("127.0.0.1", 6379, db, null);
		ConnectionSpec node3Spec = DefaultConnectionSpec.newSpec("127.0.0.1", 6379, anotherDb, null);
		
		ClusterNodeSpec node1 = new ClusterNodeSpecRI(node1Spec);
		ClusterNodeSpec node2 = new ClusterNodeSpecRI(node2Spec);
		ClusterNodeSpec node3 = new ClusterNodeSpecRI(node3Spec);
		
		assertTrue(node1.getId().equals(node2.getId()), "ids should be identical");
		assertTrue(node1.hashCode() == node2.hashCode(), "hashCodes should be equal");
		assertTrue(node1.equals(node2), "nodes must be considered equivalent");
		assertTrue(node2.equals(node1), "nodes must be considered equivalent [transitive test]");
		
		
		// test enforcement of DB# of node's ConnectionSpec in identity tests
		// these should all fail
		// this will likely change in future when we open up Select but for now (as of 1.2.n) selected DB
		// must be enforced.
		// TODO: spell this out in the ClusterNodeSpec docs.
		
		assertFalse(node1.getId().equals(node3.getId()), "ids should NOT be identical");
		assertFalse(node1.hashCode() == node3.hashCode(), "hashCodes should NOT be equal");
		assertFalse(node1.equals(node3), "nodes must NOT be considered equivalent");
		assertFalse(node3.equals(node1), "nodes must NOT be considered equivalent [transitive test]");

		// now lets raise some errors
		boolean didRaiseError;
		
		// test arg constraint checking
		//
		didRaiseError = false;
		ClusterNodeSpec nullRef = null;
		try {
			node1.equals(nullRef);
		}
		catch (IllegalArgumentException e){
			didRaiseError = true;
		}
		if(!didRaiseError) fail("Expecting an IllegalArgumentException raised for null input arg to equals()");
		
		didRaiseError = false;
		Object foo = new Object();
		try {
			node1.equals(foo);
		}
		catch (IllegalArgumentException e){
			didRaiseError = true;
		}
		if(!didRaiseError) fail("Expecting an IllegalArgumentException raised for invalid object type arg to equals()");
	}
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
		int n = 0;
		for(ClusterNodeSpec nodeSpec : clusterNodeSpecs){
			for(int i=0; i<instanceCnt; i++) {
				String nodeInstanceCHKey = nodeSpec.getKeyForCHRangeInstance(i);
				assertTrue(chRangeKeys.add(nodeInstanceCHKey), "generated ConsistentHash Key for node "+nodeSpec.getId()+" for instance "+i+" should be unique but was not: " + nodeInstanceCHKey);
			}
			if(++n > 100) break;
		}
	}
}
