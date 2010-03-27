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

import static org.testng.Assert.fail;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import org.jredis.ri.alphazero.support.Log;
import org.jredis.ri.cluster.ClusterNodeSpecRI;

/**
 * We'll use a singleton for our test data to minimize jvm heap impact
 * of the potentially very large test data.
 * 
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 27, 2010
 */

public class ClusterSuiteTestData {
	
	/** singleton instance - otherwise multiple test runs kill the jvm heap */
	static private final ClusterSuiteTestData instance = new ClusterSuiteTestData();
	// ------------------------------------------------------------------------
	// General RI Test Suite Parameters with default values to avoid XML
	// ------------------------------------------------------------------------
	
	protected int		NODE_CNT =  1000;
	protected String 	CLUSTER_NODES_ADDRESS_BASE = "127.0.0.1";
	protected int 		CLUSTER_NODES_PORT_BASE = 6379;
	protected int		db = 10;
	
	// ------------------------------------------------------------------------
	// General RI Test Suite test data
	// ------------------------------------------------------------------------
	public final Set<ClusterNodeSpec> clusterNodeSpecs = new HashSet<ClusterNodeSpec>();
	public final ClusterNodeSpec[] clusterNodeSpecsArray;
	
	// ------------------------------------------------------------------------
	// Access
	// ------------------------------------------------------------------------
	/**
	 * @return the singleton instance of  {@link ClusterSuiteTestData}
	 */
	public static ClusterSuiteTestData getInstance () {
		return instance;
	}
	// ------------------------------------------------------------------------
	// Const
	// ------------------------------------------------------------------------
	private ClusterSuiteTestData () {
		ClusterNodeSpec nodeSpec = null;
		for(int i=0;i<NODE_CNT; i++) {
			InetAddress address = getInetAddressFor(CLUSTER_NODES_ADDRESS_BASE);
			ConnectionSpec connSpec = getConnectionSpecFor(address, CLUSTER_NODES_PORT_BASE+i, db);
			nodeSpec = new ClusterNodeSpecRI (connSpec);
			clusterNodeSpecs.add(nodeSpec);
		}

		// sets are a pain if you just want a member
		clusterNodeSpecsArray = new ClusterNodeSpec[clusterNodeSpecs.size()];
		clusterNodeSpecs.toArray(clusterNodeSpecsArray);

		Log.log("clusterNodeSpecsArray: " + clusterNodeSpecsArray);
		for(ClusterNodeSpec s : clusterNodeSpecsArray)
			if (null == s) Log.log("NULL clusterNodeSpec: " + s);

		Log.log("[ClusterSuiteTestData] Suite test data initialized");
	}	
	
	// ------------------------------------------------------------------------
	// Helper methods
	// ------------------------------------------------------------------------

	static public final Random random = new Random(System.currentTimeMillis());

	/**
	 * Creates a random ascii string
	 * @param length
	 * @return
	 */
	static public String getRandomAsciiString (int length) {
		StringBuilder builder = new  StringBuilder(length);
		for(int i = 0; i<length; i++){
			char c = (char) (random.nextInt(126-33) + 33);
			builder.append(c);
		}
		return builder.toString();
	}

	/**
	 * Creates a buffer of given size filled with random byte values
	 * @param size
	 * @return
	 */
	static public byte[] getRandomBytes(int size) {
		int len = size;
		byte[]	buff = new byte[len];
		random.nextBytes(buff);
		return buff;
	}

	/**
	 * @return
	 */
	static public String getRandomIPv$HostName () {
		Formatter fmt = new Formatter();
		fmt.format("%d.%d.%d.%d", 
			random.nextInt(255),
			random.nextInt(255),
			random.nextInt(255),
			random.nextInt(255)
		);
		return fmt.toString();
	}
	
	static public InetAddress getInetAddressFor (String hostName) {
		InetAddress address = null;
		try {
			address = InetAddress.getByName(hostName);
		}
		catch (UnknownHostException e) {
			fail("In suite setup for random address <"+hostName+">", e);
		}
		return address;
	}
	static public ConnectionSpec getConnectionSpecFor (InetAddress address, int port, int db) {
		ConnectionSpec connSpec = DefaultConnectionSpec.newSpec().setAddress(address).setPort(port).setDatabase(db);
		return connSpec;
	}
}
