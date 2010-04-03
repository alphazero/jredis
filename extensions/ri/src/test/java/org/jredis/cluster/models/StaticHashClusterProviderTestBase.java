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

import java.util.Set;
import org.jredis.NotSupportedException;
import org.jredis.cluster.ClusterModelProviderTestBase;
import org.jredis.cluster.ClusterNodeSpec;
import org.jredis.cluster.ClusterSpec;
import org.jredis.cluster.ClusterType;
import org.jredis.cluster.model.StaticHashCluster;
import org.jredis.ri.alphazero.support.Log;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

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
    	
    	Log.log("Test provider conformance to static hashing requirements");
    	assertNotNull(provider, "provider is null!");
    	
    	// 1 - Must support the STATIC HASH and nothing else
    	assertTrue(provider.supports(ClusterType.STATIC_HASH), "A StaticHashCluster model must support Type.CONSISTENT_HASH");
    	assertFalse(provider.supports(ClusterType.CONSISTENT_HASH), "A StaticHashCluster model can not support other cluster types beyond Type.CONSISTENT_HASH");
    	
    	// 2 - Must not be reconfigurable
    	assertFalse(provider.supportsReconfiguration(), "Static hash clusters can not be reconfigured.");
    	
    	// 3 - Must not support any of the NodeMap modification ops, indicated by raising the specified ProviderException class
    	boolean didRaiseEx;    	
		ClusterSpec clusterSpec = this.newClusterSpec();
		Set<ClusterNodeSpec> nodeSpecs =  clusterSpec.getNodeSpecs();
		assertTrue(nodeSpecs.size() > 0, "node specs set size must be greater than zero.");
		ClusterNodeSpec aNodeSpec =  (ClusterNodeSpec) nodeSpecs.toArray()[0];
		
    	didRaiseEx = false;
    	try {
    		provider.addNode(aNodeSpec);
    	}
    	catch (NotSupportedException expected) {didRaiseEx = true;}
    	catch (Throwable whatsthis) {fail ("Unexpected exception raised by provider method", whatsthis);}
    	if(!didRaiseEx) { fail("Provider failed to raise exception for addNode()");}
		
    	didRaiseEx = false;
    	try {
    		provider.removeNode(aNodeSpec);
    	}
    	catch (NotSupportedException expected) {didRaiseEx = true;}
    	catch (Throwable whatsthis) {fail ("Unexpected exception raised by provider method", whatsthis);}
    	if(!didRaiseEx) { fail("Provider failed to raise exception for removeNode()");}

    }
    
    @Test
    public void testStaticHash() {
    	Log.log("Basic nodemap test of Consistent Hashing cluster model");
    	
    	StaticHashCluster model = (StaticHashCluster) newProviderInstance();
    	assertNotNull(model, "model should not be null");
    	
    	ClusterSpec spec = model.getSpec();
    	assertNotNull(spec, "spec should not be null");
    }    
}
