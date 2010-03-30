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

import java.util.Collection;
import java.util.HashSet;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.support.Log;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 24, 2010
 */
//TODO: look into the data provider attrib of the annotation ...
@Test(suiteName="extensions-cluster-specs-2")
abstract 
public class ClusterSpecProviderTestBase extends RefImplTestSuiteBase<ClusterSpec> {

	// ------------------------------------------------------------------------
	// Extension point
	// ------------------------------------------------------------------------
	
    protected abstract ClusterNodeSpec newNodeSpec (ConnectionSpec connectionSpec) ;

	// ------------------------------------------------------------------------
	// Specification Interface tested
	// ------------------------------------------------------------------------
	
	protected final Class<?> getSpecificationClass () {
		return ClusterSpec.class;
	}

	// ------------------------------------------------------------------------
	// Test general contract of SPECS for Cluster and its Nodes
	// ------------------------------------------------------------------------
	
	@Test
	public void testGetType() {
		Log.log("Testing ClusterSpec.getType()");
		assertNotNull(provider.getType(), "getType() must never return null");
	}
	
	@Test
	public void testSetType() {
		Log.log("Testing ClusterSpec.setType()");
		
		// Note: don't use provider instance as we are changing various settings here
		// and don't want to break assumptions down the hierarchy chain.

		ClusterType clusterType = null;
		ClusterSpec clusterSpec = newProviderInstance();
		ClusterType prevType = clusterSpec.getType();
		assertNotNull(prevType, "getType() must never return null");
		// just pick something else
		for(ClusterType type : ClusterType.values()){
			if(type != prevType){
				clusterType = type;
				break;
			}
		}
		assertNotNull(clusterType, "[BUG] why couldn't we find another different type?");
		
		// use the setter and test various requirements.
		ClusterSpec chainedRes = clusterSpec.setType(clusterType);
		testChainedResult(chainedRes, clusterSpec);
		
		assertEquals(clusterSpec.getType(), clusterType, "getType() result must match the ref used for setType()");
	}
	
	@Test
	public void testAddAndRemoveAll() {
		Log.log("Testing ClusterSpec addAll() | removeAll()");
		ClusterSpec clusterSpec = newProviderInstance();
		
		Log.log("Test with cluster spec with %d node specs ..", clusterSpec.getNodeSpecs().size());
		Collection<ClusterNodeSpec> nodes = new HashSet<ClusterNodeSpec>();
		for(int i=0; i<10; i++){
			nodes.add(newNodeSpec(data.connSpecs[i]));
		}
		assertTrue(clusterSpec.addAll(nodes), "addAll should return true");
		assertFalse(clusterSpec.addAll(nodes), "dup addAll should return false");
		
		assertTrue(clusterSpec.removeAll(nodes), "removeAll should return true");
		assertFalse(clusterSpec.removeAll(nodes), "second removeAll should return false");
		
		// test constraint against collections with a null member
		nodes.clear();
		assertTrue(nodes.add(null), "collection w null input");
		for(int i=0; i<10; i++) {
			nodes.add(newNodeSpec(data.connSpecs[i]));
		}
		assertTrue(nodes.contains(null), "collection has a null input");
		boolean didRaiseEx;
		didRaiseEx = false;
		try {
			clusterSpec.addAll(nodes);
		}
		catch (IllegalArgumentException e) { didRaiseEx = true; }
		catch (RuntimeException whatsthis) { fail("unexpected exception raised during op", whatsthis); }
		assertTrue(didRaiseEx, "IllegalArgumentException raise is expected");
		
		didRaiseEx = false;
		try {
			clusterSpec.removeAll(nodes);
		}
		catch (IllegalArgumentException e) { didRaiseEx = true; }
		catch (RuntimeException whatsthis) { fail("unexpected exception raised during op", whatsthis); }
		assertTrue(didRaiseEx, "IllegalArgumentException raise is expected");
		
		
	}
	
	
	@Test
	public void testRemoveNodeSpec() {
		Log.log("Testing ClusterSpec.addNodeSpec()");
		ClusterSpec clusterSpec = newProviderInstance();
		
		for(int i=0; i<10; i++){
			ClusterNodeSpec nodeSpec = newNodeSpec(data.connSpecs[i]);
			assertTrue(clusterSpec.addNode(nodeSpec));
		}
		
		for(int i=0; i<10; i++){
			ClusterNodeSpec nodeSpec = newNodeSpec(data.connSpecs[i]);
			assertTrue(clusterSpec.removeNode(nodeSpec));
		}
	}
	
	@Test
	public void testAddNodeSpec() {
		Log.log("Testing ClusterSpec.addNodeSpec()");
		ClusterSpec clusterSpec = newProviderInstance();
		
		ClusterNodeSpec nodeSpec = null;
		
		nodeSpec = newNodeSpec(data.connSpecs[0]);
		assertTrue(clusterSpec.addNode(nodeSpec));
		
		nodeSpec = newNodeSpec(data.connSpecs[1]);
		assertTrue(clusterSpec.addNode(nodeSpec));
		
		
		// now lets raise some errors
		boolean didRaiseError;
		
		// should not allow adding of duplicate ClusterNodeSpecs
		didRaiseError = false;
		
		assertTrue(clusterSpec.addNode(newNodeSpec(data.defRedisDb10Port7777ConnSpec)), "add of unique spec should be possible and must return the clusterSpec instance");
		assertFalse(clusterSpec.addNode(newNodeSpec(data.defRedisDb10Port7777ConnSpec_dup)), "add of duplicate spec is expected to raise a runtime exception");
		
		// should not allow adding of null specs
		didRaiseError = false;
		ClusterNodeSpec nullRef = null;
		try {
			clusterSpec.addNode(nullRef);
		}
		catch (IllegalArgumentException e){
			didRaiseError = true;
		}
		if(!didRaiseError) fail("Expecting an IllegalArgumentException raised for null input arg to add()");
	}
	
	// ------------------------------------------------------------------------
	// helper methods
	// ------------------------------------------------------------------------
	
	private final void testChainedResult (ClusterSpec res, ClusterSpec expected) {
		assertNotNull(res, "fluent interface setters must return non null values");
		assertEquals(res, expected, "setter result must be the same reference as the original");
	}
}
