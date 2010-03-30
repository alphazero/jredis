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
	protected abstract ClusterSpec.Type getSupportedClusterType ();
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

		ClusterSpec.Type clusterType = getSupportedClusterType();
		assertNotNull(clusterType, "getSupportedClusterType");
		
		ClusterSpec testSpec = newClusterSpec();
		assertNotNull(testSpec, "newClusterSpec should not return null");
		
		assertEquals(testSpec.getType(), clusterType, "asserted supported cluster type and type from the newClusterSpec should be the same");
		
		ClusterModel model = newProviderInstance();
		assertNotNull(model, "newProviderInstance should not return null");
		
	}
}
