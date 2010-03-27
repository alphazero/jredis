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

import org.jredis.ri.alphazero.support.Log;
import org.jredis.ri.cluster.ketama.KetamaHashProvider;
import org.jredis.ri.cluster.ketama.KetamaNodeMappingAlgorithm;

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
	// Specification Interface tested
	// ------------------------------------------------------------------------
	
	protected final Class<?> getSpecificationClass () {
		return ClusterSpec.class;
	}

	// ------------------------------------------------------------------------
	// Test general contract of SPECS for Cluster and its Nodes
	// ------------------------------------------------------------------------
	
	@Test
	public void testHashProviderAvailability() {
		Log.log("Testing ClusterSpec.getHashAlgorithm()");
		
		// new (empty) ClusterSpec
		ClusterSpec clusterSpec = newProviderInstance();
		
		// getHashProvider should return non-null results
		assertTrue (clusterSpec.getHashAlgorithm() != null, "ClusterSpec.getHashAlgorithm should not be null");
		
		// getHashProvider should return an instance of KetamaHashProvider for the DefaultClusterSpec
		assertTrue (clusterSpec.getHashAlgorithm() instanceof KetamaHashProvider, "Default ClusterSpec.getHashAlgorithm should be a Ketama algoritm");
	}
	
	@Test
	public void testNodeMappingAlgorithmAvailability() {
		Log.log("Testing ClusterSpec.getNodeMappingAlgorithm()");
		
		// new (empty) ClusterSpec
		ClusterSpec clusterSpec = newProviderInstance();
		
		// getHashProvider should return non-null results
		assertTrue (clusterSpec.getNodeMappingAlgorithm() != null, "ClusterSpec.getNodeMappingAlgorithm should not be null");
		
		// getHashProvider should return an instance of KetamaHashProvider for the DefaultClusterSpec
		assertTrue (clusterSpec.getNodeMappingAlgorithm() instanceof KetamaNodeMappingAlgorithm, "Default ClusterSpec.getNodeMappingAlgorithm should be a Ketama algoritm");
	}
	
	@Test
	public void testAddNodeSpec() {
		Log.log("Testing ClusterSpec.addNodeSpec()");
		ClusterSpec clusterSpec = newProviderInstance();
		
		Log.log("clusterNodeSpecsArray: " + data.clusterNodeSpecsArray);
		try {
			clusterSpec.addNodeSpec(data.clusterNodeSpecsArray[0]);
		}
		catch (Exception e){ fail("when adding a unique spec", e); }
		
		try {
			clusterSpec.addNodeSpec(data.clusterNodeSpecsArray[1]);
		}
		catch (Exception e){ fail("when adding a unique spec", e); }
		
		// now lets raise some errors
		boolean didRaiseError;
		
		// should not allow adding of duplicate ClusterNodeSpecs
		didRaiseError = false;
		
		assertTrue(clusterSpec.addNodeSpec(data.defaultRedisWithDb10ClusterNodeSpec) == clusterSpec, "add of unique spec should be possible and must return the clusterSpec instance");
		try {
			assertTrue(clusterSpec.addNodeSpec(data.defaultRedisWithDb10ClusterNodeSpec_dup) == clusterSpec, "add of duplicate spec is expected to raise a runtime exception");
		}
		catch (IllegalArgumentException e){
			didRaiseError = true;
		}
		if(!didRaiseError) fail("Expecting an IllegalArgumentException raised for duplicate ClusterNodeSpec to add()");
		
		// should not allow adding of null specs
		didRaiseError = false;
		ClusterNodeSpec nullRef = null;
		try {
			assertTrue(clusterSpec.addNodeSpec(nullRef) == clusterSpec, "add of null spec is expected to raise a runtime exception");
		}
		catch (IllegalArgumentException e){
			didRaiseError = true;
		}
		if(!didRaiseError) fail("Expecting an IllegalArgumentException raised for null input arg to add()");
	}
}
