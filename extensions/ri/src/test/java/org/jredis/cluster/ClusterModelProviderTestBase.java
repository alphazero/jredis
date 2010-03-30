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
import org.testng.annotations.Test;
import static org.testng.Assert.*;


/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 29, 2010
 * 
 */
@Test(suiteName="extensions-cluster-model")
public abstract class ClusterModelProviderTestBase extends RefImplTestSuiteBase<ClusterModel> {

	// ------------------------------------------------------------------------
	// Extension point
	// ------------------------------------------------------------------------
	
    /**
     * @param clusterSpec
     * @return
     */
    protected abstract ClusterModel newClusterModel (ClusterSpec clusterSpec) ;
    
    /**
     * @return a {@link ClusterSpec} suitable for instantiating a provider instance
     */
    protected abstract ClusterSpec newClusterSpec () ;

	/**
	 * @return the ClusterSpec.Type that the provider is supposed to support
	 */
	protected abstract ClusterType getSupportedClusterType ();
	// ------------------------------------------------------------------------
	// Specification Interface tested
	// ------------------------------------------------------------------------
	/* (non-Javadoc) @see org.jredis.cluster.ProviderTestBase#getSpecificationClass() */
	@Override
	protected Class<?> getSpecificationClass () {
		return ClusterModel.class;
	}

	/* (non-Javadoc) @see org.jredis.cluster.ProviderTestBase#newProviderInstance() */
	@Override
	protected ClusterModel newProviderInstance () {
		return newClusterModel(newClusterSpec());
	}
	
	
	// ------------------------------------------------------------------------
	// Test general contract of SPECS for Cluster and its Nodes
	// ------------------------------------------------------------------------
	@Test
	public void metaTest (){
		Log.log("[META] test the test suite assumptions!");

		ClusterType clusterType = getSupportedClusterType();
		assertNotNull(clusterType, "getSupportedClusterType");
		
		ClusterSpec testSpec = newClusterSpec();
		assertNotNull(testSpec, "newClusterSpec should not return null");
		
		assertEquals(testSpec.getType(), clusterType, "asserted supported cluster type and type from the newClusterSpec should be the same");
		
		ClusterModel model = newProviderInstance();
		assertNotNull(model, "newProviderInstance should not return null");
	}
	
	@Test
	public void testClusterSpecPropertyOps () {
		Log.log("test ClusterSpec accessors");

		// null spec arg on construct must raise an error
		//
		boolean didRaiseEx;
		didRaiseEx = false;
		try {
			@SuppressWarnings("unused")
            ClusterModel m = newClusterModel(null);
		}
		catch (IllegalArgumentException e) { didRaiseEx = true; }
		catch (RuntimeException whatsthis) { fail("unexpected exception raised during op", whatsthis); }
		assertTrue(didRaiseEx, "IllegalArgumentException raise is expected");
		
		// spec arg with no nodes on construct is not an error if 
		// model supports reconfiguration
		//
		boolean supportsReconfig = newProviderInstance().supportsReconfiguration();
		didRaiseEx = false;
		try {
			@SuppressWarnings("unused")
			ClusterSpec s = newClusterSpec();
			s.removeAll(s.getNodeSpecs());
			assertTrue(s.getNodeSpecs().size() == 0, "cluster spec should have no node specs now");
            ClusterModel m = newClusterModel(s);
		}
		catch (IllegalArgumentException e) { didRaiseEx = true; }
		catch (RuntimeException whatsthis) { fail("unexpected exception raised during op", whatsthis); }
		assertTrue(didRaiseEx && !supportsReconfig , "expected only if non reconfigurable");

		ClusterSpec spec = newClusterSpec();
		ClusterModel model = newClusterModel(spec);
		assertNotNull(newProviderInstance().getSpec(), "clusterSpec property should be non-null");
		assertEquals(model.getSpec(), spec, "expecting returned property to be the spec used in constructor");
	}
}
