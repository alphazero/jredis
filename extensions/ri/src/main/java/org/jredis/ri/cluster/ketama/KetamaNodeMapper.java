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
import org.jredis.cluster.deprecated.ClusterListener;
import org.jredis.cluster.deprecated.ClusterNodeMapper;
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

public class KetamaNodeMapper implements ClusterNodeMapper, ClusterListener {

	// ------------------------------------------------------------------------
	// Props
	// ------------------------------------------------------------------------
	
	/** what is a sensible value here? */
	private static final double REPLICATION_CONST = 10;

	/**  */
	private final ClusterSpec clusterSpec;
	
	/**  */
	private final NodeMap	nodeMap;
	
	private final KetamaHashProvider ketamaHashAlgo;
	
	private int nodeReplicationCnt;
	

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------
	/**
     * Initializes the {@link NodeMap} by calling {@link KetamaNodeMapper#mapNodes(ClusterSpec)}.
     * 
     */
    KetamaNodeMapper (ClusterSpec clusterSpec) {
    	this.clusterSpec = clusterSpec;
    	this.ketamaHashAlgo = new KetamaHashProvider();
		this.nodeMap = new NodeMap();
		
		initialize();
    }
	// ------------------------------------------------------------------------
	// package access scoped accessors - for use by other Ketama related classes
	// ------------------------------------------------------------------------
    
	/**  @return the nodeMap */
    NodeMap getNodeMap () { return nodeMap; }

	/**  @return the clusterSpec */
    ClusterSpec getClusterSpec () { return clusterSpec; }
    
	// ------------------------------------------------------------------------
	// Interface
    /* ======================================================== ClusterListener
     * 
     * NodeMap is modified in response to events.
     * 
     */
	// ------------------------------------------------------------------------
	/* (non-Javadoc) @see org.jredis.cluster.ClusterListener#notifyNodeAdded(org.jredis.cluster.ClusterSpec, org.jredis.cluster.ClusterNodeSpec) */
    public void notifyNodeAdded (ClusterSpec src, ClusterNodeSpec clusterNodeSpec) {
    	if(this.clusterSpec != src )
    		throw new ProviderException("BUG: cluster event src");
    	
    	if(mapNode(clusterNodeSpec)){
    		throw new ProviderException("BUG/TODO: not sure what needs to happen in case of collisions.");
    	}
    	
    }
	/* (non-Javadoc) @see org.jredis.cluster.ClusterListener#notifyNodeRemoved(org.jredis.cluster.ClusterSpec, org.jredis.cluster.ClusterNodeSpec) */
    public void notifyNodeRemoved (ClusterSpec src, ClusterNodeSpec clusterNodeSpec) {
    	if(this.clusterSpec != src )
    		throw new ProviderException("BUG: cluster event src");
    	
    	
    }

	// ------------------------------------------------------------------------
	// Interface
    /* ============================================================== NodeMaper
     * 
     */
	// ------------------------------------------------------------------------
	/* (non-Javadoc) @see org.jredis.cluster.ClusterNodeMapper#getNodeForKey(byte[]) */
    public ClusterNodeSpec getNodeForKey (long hash) {
		final ClusterNodeSpec rv;
		if(!nodeMap.containsKey(hash)) {
			// Java 1.6 adds a ceilingKey method, but I'm still stuck in 1.5
			// in a lot of places, so I'm doing this myself.
			SortedMap<Long, ClusterNodeSpec> tailMap=nodeMap.tailMap(hash);
			if(tailMap.isEmpty()) {
				hash = nodeMap.firstKey();
			} 
			else {
				hash=tailMap.firstKey();
			}
		}
		rv = nodeMap.get(hash);
		return rv;
    }

	/**
	 * Per original paper on consistent hashing, the replication count of any given bucket is
	 * k*log(C), where C is the number of buckets (i.e. nodes).  We're using {@link KetamaNodeMapper#REPLICATION_CONST}
	 * as k.
	 * 
     * @param size
     * @return
     */
    int getNodeReplicationCount (int nodeCnt) {
	    return (int) (Math.log(nodeCnt) * REPLICATION_CONST);
    }
    
    // ------------------------------------------------------------------------
    // Inner Ops
    // ------------------------------------------------------------------------
    
    private void initialize () {
    	nodeReplicationCnt = (int) (Math.log(clusterSpec.getNodeSpecs().size()) * REPLICATION_CONST);
    	mapNodes();
    }
	/**
	 * This method is a slightly modified version of net.spy.memcached.KetamaNodeLocator's constructor.
	 * @see <a href="http://github.com/????????/">GIT HUB LINK HERE ...</a>
	 */

	private void mapNodes () 
	{		
		try {
//			KetamaHashProvider 		ketamaHashAlgo = (KetamaHashProvider) clusterSpec.getStrategy().getHashAlgorithm();
			Set<ClusterNodeSpec> 	nodes = clusterSpec.getNodeSpecs();
			for(ClusterNodeSpec node : nodes) {
				mapNode(node);
//				// Dustin says: "Ketama does some special work with md5 where it reuses chunks."
//				for(int i=0; i<nodeReplicationCnt / 4; i++) {
//					byte[] digest;
//					digest = CryptoHashUtils.computeMd5(node.getKeyForReplicationInstance(i));
//					for(int h=0;h<4;h++) {
//						// Joubin says: here's we're calling a KetamaHashProvider specific method that does the 
//						// Ketama chunking per above.  
//						nodeMap.put(ketamaHashAlgo.hash(digest, h), node);
//					}
//				}
			}
			if(nodeMap.size() != nodeReplicationCnt * nodes.size())
				throw new ProviderException ("[BUG]: expecting node map size to be multiple of replication count * cluster node count");
		}
		catch (ClassCastException e) {
			throw new ProviderException ("[BUG] KetamaNodeMappingAlgorithm requires a KetamaHashAlgorithm");
		}
	}

	private boolean mapNode(ClusterNodeSpec node){
		// Dustin says: "Ketama does some special work with md5 where it reuses chunks."
		for(int i=0; i<nodeReplicationCnt / 4; i++) {
			byte[] digest;
			digest = CryptoHashUtils.computeMd5(node.getKeyForReplicationInstance(i));
			for(int h=0;h<4;h++) {
				// Joubin says: here's we're calling a KetamaHashProvider specific method that does the 
				// Ketama chunking per above.  
				nodeMap.put(ketamaHashAlgo.hash(digest, h), node);
			}
		}
		return false;
	}

    // ========================================================================
    //	INNER TYPES
    // ========================================================================

    @SuppressWarnings("serial")
    public static class NodeMap extends TreeMap<Long, ClusterNodeSpec> implements SortedMap<Long, ClusterNodeSpec> {
    	// TODO: add the required iterators, nextKey, etc.
    }

}
