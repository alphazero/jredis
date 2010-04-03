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

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;

/**
 * As base for all org.jredis.cluster[...] tests, this class is mainly responsible
 * for 
 * <li> [-TODO] initialize the test suite parameters through testNG injection from module's pom
 * <li> [-TODO] get and initialize the test data singleton   
 * <li> providing any required helper methods
 * <b>
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 24, 2010
 */

public abstract class RefImplTestSuiteBase <T> extends ProviderTestBase <T>{

	// ------------------------------------------------------------------------
	// General RI Test Suite Parameters with default values to avoid XML
	// ------------------------------------------------------------------------
	
	protected int		NODE_CNT =  10;
	protected String 	CLUSTER_NODES_ADDRESS_BASE = "127.0.0.1";
	protected int 		CLUSTER_NODES_PORT_BASE = 6379;
	
	
	/** data holds the ref to a singleton class with all the test data */
	protected ClusterSuiteTestData data;

	@Parameters({ 
//		"jredis.test.password", 
//		"jredis.test.host", 
//		"jredis.test.port",
//		"jredis.test.db.1",
//		"jredis.test.db.2",
//
//		"jredis.test.datasize.small",
//		"jredis.test.datasize.medium",
//		"jredis.test.datasize.large",
//		"jredis.test.cnt.small",
//		"jredis.test.cnt.medium",
//		"jredis.test.cnt.large",
//		"jredis.test.expire.secs",
//		"jredis.test.expire.wait.millisecs"
		"jredis.cluster.node.cnt",
		"jredis.cluster.node.address.base",
		"jredis.cluster.node.port.base"
	})
	@BeforeSuite
	public void suiteParametersInit(
//		String password, 
//		String host, 
//		int port,
//		int db1,
//		int db2,

		int 	nodecnt,
		String 	nodesAddressBase,
		int		nodesPortBase
	) 
	{
		NODE_CNT = nodecnt;
		CLUSTER_NODES_ADDRESS_BASE = nodesAddressBase;
		CLUSTER_NODES_PORT_BASE = nodesPortBase;
		
//		Log.log("nodecnt: %d", NODE_CNT);
//		Log.log("cluster nodes address base: %s", CLUSTER_NODES_ADDRESS_BASE);
//		Log.log("cluster nodes port base:    %d", CLUSTER_NODES_PORT_BASE);
//		Log.log("nodecnt: %d", NODE_CNT);
//		Log.log("[extensions.cluster] Suite parameters initialized <suiteParametersInit>");
		
		data = setupTestSuiteData();
		Assert.assertNotNull(data, "ClusterSuiteTestData instance obtained is null");
	}
	/**
	 * a do nothing method - keeping it around in case I change my mind about using the
	 * singleton test data class.
	 * @return
	 */
	private ClusterSuiteTestData setupTestSuiteData () {
		return ClusterSuiteTestData.getInstance();
	}	
}
