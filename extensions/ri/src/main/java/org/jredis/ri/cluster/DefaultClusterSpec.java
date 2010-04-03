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

package org.jredis.ri.cluster;

import java.util.Collection;
import org.jredis.cluster.ClusterNodeSpec;
import org.jredis.cluster.ClusterSpec;
import org.jredis.cluster.ClusterType;
import org.jredis.cluster.ClusterSpec.Support;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;

/**
 * The default ClusterSpec uses the {@link KetamaConsitentHashCluster_reture} as its {@link ClusterModel_deprecated}. 
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 25, 2010
 * 
 */

public class DefaultClusterSpec extends Support implements ClusterSpec {
	
	/** Default ClusterSpec.Type is {@link ClusterType#CONSISTENT_HASH} */
	public static final ClusterType DEFAULT_CLUSTER_TYPE = ClusterType.CONSISTENT_HASH;
	
	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------
	
	public DefaultClusterSpec () {
		this(null);
	}

	public DefaultClusterSpec (Collection<ClusterNodeSpec> nodeSpecs) {
		super();
		this.setType(DEFAULT_CLUSTER_TYPE);
		if(null != nodeSpecs){
			addAll(nodeSpecs);
		}
	}

	// ------------------------------------------------------------------------
	// public interface
	// ------------------------------------------------------------------------
	
	/**
	 * @param host
	 * @param firstPort
	 * @param lastPort
	 * @param templateConnSpec
	 * @return
	 */
	public static ClusterSpec newSpecForRange (ConnectionSpec templateConnSpec, int firstPort, int lastPort) {
		ClusterSpec spec = new DefaultClusterSpec();
		for(int i=firstPort; i<=lastPort; i++){
			ConnectionSpec connSpec = DefaultConnectionSpec.newSpec()
				.setAddress(templateConnSpec.getAddress())
				.setPort(i)
				.setDatabase(templateConnSpec.getDatabase())
				.setCredentials(templateConnSpec.getCredentials());
			ClusterNodeSpec nodeSpec = new DefaultClusterNodeSpec(connSpec);
			spec.addNode(nodeSpec);
		}
		return spec;
	}
	// ------------------------------------------------------------------------
	// super overrides
	// ------------------------------------------------------------------------
}
