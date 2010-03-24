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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import org.jredis.ri.alphazero.support.Log;
import org.testng.annotations.BeforeSuite;
import static org.testng.Assert.*;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 24, 2010
 * 
 */

public class RefImplTestSuiteBase {

	protected final List<ClusterNodeSpec> clusterNodeSpecs = new ArrayList<ClusterNodeSpec>();

	@BeforeSuite
	public void suiteParametersInit(
	) 
	{
		Log.log("Suite parameters initialized <suiteParametersInit>");

		setupTestSuiteData();
	}

	/**
	 * 
	 */
	private void setupTestSuiteData () {
		ClusterNodeSpec nodeSpec = null;
		for(int i=0;i<65536; i++) {
			InetAddress address = getInetAddressFor(getRandomIPv$HostName());
			ConnectionSpec connSpec = getConnectionSpecFor(address, i);
			nodeSpec = new ClusterNodeSpec.RefImpl(connSpec);
			clusterNodeSpecs.add(nodeSpec);
		}
	}	
	// ------------------------------------------------------------------------
	// Helper methods
	// ------------------------------------------------------------------------

	protected final Random random = new Random(System.currentTimeMillis());

	/**
	 * Creates a random ascii string
	 * @param length
	 * @return
	 */
	protected String getRandomAsciiString (int length) {
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
	protected byte[] getRandomBytes(int size) {
		int len = size;
		byte[]	buff = new byte[len];
		random.nextBytes(buff);
		return buff;
	}

	/**
	 * @return
	 */
	protected String getRandomIPv$HostName () {
		// {255.255.255.255}
		return "127.0.0.1";
	}
	protected InetAddress getInetAddressFor (String hostName) {
		InetAddress address = null;
		try {
			address = InetAddress.getByName(hostName);
//			ConnectionSpec connSpec = DefaultConnectionSpec.newSpec().setAddress(address);
		}
		catch (UnknownHostException e) {
			fail("In suite setup for random address <"+hostName+">", e);
		}
		return address;
	}
	protected ConnectionSpec getConnectionSpecFor (InetAddress address, int port) {
		ConnectionSpec connSpec = DefaultConnectionSpec.newSpec().setAddress(address).setPort(port);
		return connSpec;
	}
}
