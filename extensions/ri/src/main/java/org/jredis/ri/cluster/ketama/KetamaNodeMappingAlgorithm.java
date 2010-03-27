/* -- BEGIN NOTICE --
 * 
 * This class uses in parts extant and/or modified code from net.spy.memcached.HashAlgorithm
 * by Dustin Sallings.  See this module's 3rd party license folder for license details.
 * 
 * -- END NOTICE -- 
 * 
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

package org.jredis.ri.cluster.ketama;

import java.util.SortedMap;
import java.util.Set;
import java.util.TreeMap;
import org.jredis.ClientRuntimeException;
import org.jredis.ProviderException;
import org.jredis.cluster.ClusterNodeSpec;
import org.jredis.cluster.ClusterSpec;
import org.jredis.cluster.NodeMappingAlgorithm;
import org.jredis.ri.cluster.support.CryptoHashUtils;

/**
 * <p>
 * <b>Note</b> that if certain expected cryptographic algorithms expected to be 
 * present in your JRE are not available, {@link ClientRuntimeException}s 
 * will be thrown.
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 25, 2010
 * 
 */

public class KetamaNodeMappingAlgorithm implements NodeMappingAlgorithm {

	/** what is a sensible value here? */
	private static final double REPLICATION_CONST = 10;

	/**
	 * This method is a slightly modified version of net.spy.memcached.KetamaNodeLocator's constructor.
	 * @see <a href="http://github.com/????????/">GIT HUB LINK HERE ...</a>
	 */
//	@Override
	public SortedMap<Long, ClusterNodeSpec> mapNodes (ClusterSpec clusterSpec) 
	{		
		SortedMap<Long, ClusterNodeSpec> ketamaNodes=new TreeMap<Long, ClusterNodeSpec>();
		
		try {
			KetamaHashProvider 		ketamaHashAlgo = (KetamaHashProvider) clusterSpec.getHashAlgorithm();
			Set<ClusterNodeSpec> 	nodes = clusterSpec.getNodeSpecs();
			int numReps= getNodeReplicationCount (nodes.size());
			for(ClusterNodeSpec node : nodes) {
				// Dustin says: "Ketama does some special work with md5 where it reuses chunks."
				for(int i=0; i<numReps / 4; i++) {
					byte[] digest;
					digest = CryptoHashUtils.computeMd5(node.getKeyForReplicationInstance(i));
					for(int h=0;h<4;h++) {
						// Joubin says: here's we're calling a KetamaHashProvider specific method that does the 
						// Ketama chunking per above.  
						ketamaNodes.put(ketamaHashAlgo.hash(digest, h), node);
					}
				}
			}
			if(ketamaNodes.size() != numReps * nodes.size())
				throw new ProviderException ("[BUG]: expecting node map size to be multiple of replication count * cluster node count");
		}
//		catch (NoSuchAlgorithmException e) {
//			throw new ClientRuntimeException("Ketama algorithm requires JRE support for MD5 message digest algorithm.", e);
//		}
		catch (ClassCastException e) {
			throw new ProviderException ("[BUG] KetamaNodeMappingAlgorithm requires a KetamaHashAlgorithm");
		}

		return ketamaNodes;
	}

	/**
	 * Per original paper on consistent hashing, the replication count of any given bucket is
	 * k*log(C), where C is the number of buckets (i.e. nodes).  We're using {@link KetamaNodeMappingAlgorithm#REPLICATION_CONST}
	 * as k.
	 * 
     * @param size
     * @return
     */
    int getNodeReplicationCount (int nodeCnt) {
	    return (int) (Math.log(nodeCnt) * REPLICATION_CONST);
    }
}
