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

package org.jredis.cluster.model;

import java.util.SortedMap;
import org.jredis.cluster.ClusterModel;
import org.jredis.cluster.ClusterNodeSpec;
import org.jredis.cluster.ClusterSpec;
import org.jredis.cluster.ClusterType;

/**
 * Generalized interface for a {@link ClusterModel} using a Consistent Hashing strategy,
 * per the original paper by Karger, et al [1].
 * <p>
 * Requirements:
 * <li>Implementors must return {@link ClusterType#CONSISTENT_HASH} for {@link ClusterSpec#getType()}
 * 
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 30, 2010
 * @see [1]: <a href="http://www.akamai.com/dl/technical_publications/ConsistenHashingandRandomTreesDistributedCachingprotocolsforrelievingHotSpotsontheworldwideweb.pdf">Consistent Hashing and Random Trees</a>
 */

// TODO: extend from a new super DynamicClusterModel and fold all NodeMap event/mod apis to that interface

public interface ConsistentHashCluster extends ClusterModel {
	
	/**
	 * Return the consistent hash node map.  Semantics of the this map are per the consistent hashing strategy paper and Ketama's implementation.
	 * @return
	 * @see ConsistentHashCluster.NodeMap
	 */
	NodeMap getNodeMap ();
	
	// ========================================================================
	// Inner Types
	// ========================================================================
	
	public interface NodeMap extends SortedMap<Long, ClusterNodeSpec> {

	}
	
	// ------------------------------------------------------------------------
	// Support
	// ------------------------------------------------------------------------
	
	public abstract static class Support extends ClusterModel.Support implements ConsistentHashCluster {

		// ------------------------------------------------------------------------
		// Properties
		// ------------------------------------------------------------------------
		
		/** what is a sensible value here? */
		protected static final double DEFAULT_REPLICATION_CONST = 100;
		/**  */
		protected NodeMap	nodeMap;
		/**  */
		protected int nodeReplicationCnt;
		
		// ------------------------------------------------------------------------
		// Constructor
		// ------------------------------------------------------------------------
		/**
         * @param clusterSpec
         */
        protected Support (ClusterSpec clusterSpec) {
	        super(clusterSpec);
        }

		// ------------------------------------------------------------------------
		// Extension points
		// ------------------------------------------------------------------------
        /**
         * @return a new (un-initialized) instance of {@link ClusterNodeMap}.  This instance
         * will be installed as the class's nodeMap attribute.
         */
        abstract protected NodeMap newClusterNodeMap();
        
        /**
         * The meats and potatoes of this type of model. Invocation of this method
         * means a complete computation of the node map per the current {@link ClusterSpec}. 
         */
        abstract protected void mapNodes();
        
        /**
         * TODO: need to change this to getReplicationConstant and 
         * TODO: extend ClusterSpec to allow setting of arbitrary parameters so it can be user configured.
         * @return
         */
        abstract protected int replicationCount();

        /**
         * Can be overriden, but extending classes MUST call super.initializeComponents() as the
         * first statement in the overriding method.
         */
        abstract protected void initializeComponents () ;
//        protected void initializeComponents () {
//        	nodeReplicationCnt = replicationCount();
//        	nodeMap = newClusterNodeMap();
//        }
        
		// ------------------------------------------------------------------------
		// Inner ops
		// ------------------------------------------------------------------------
        /**
         * This method will first invoke {@link ConsistentHashCluster.Support#initializeComponents()},
         * and then will invoke the abstract {@link ConsistentHashCluster.Support#mapNodes()}.
         * <p>
         * When this method returns, the cluster model is expected to be ready to service request to 
         * map keys to nodes.
         * 
         * @see org.jredis.cluster.ClusterModel.Support#initializeModel()
         */
        @Override
        final protected void initializeModel () {
        	initializeComponents();
//        	hashAlgo = new KetamaHashProvider();
        	nodeReplicationCnt = replicationCount();
        	nodeMap = newClusterNodeMap();
        	mapNodes();
        }

		/* (non-Javadoc) @see org.jredis.cluster.ClusterModel.Support#onNodeAddition(org.jredis.cluster.ClusterNodeSpec) */
        @Override
        protected boolean onNodeAddition (ClusterNodeSpec newNode) {
        	throw new RuntimeException("not implemented");        
        }

		/* (non-Javadoc) @see org.jredis.cluster.ClusterModel.Support#onNodeRemoval(org.jredis.cluster.ClusterNodeSpec) */
        @Override
        protected boolean onNodeRemoval (ClusterNodeSpec newNode) {
        	throw new RuntimeException("not implemented");        
        }

    	/**
    	 * TODO: return the map or clone it?  WHY IS METHOD EVEN NECESSARY?
    	 * @see org.jredis.cluster.model.ConsistentHashCluster#getNodeMap()
    	 * @return ???
    	 */
        public NodeMap getNodeMap () {
    	    return nodeMap;
        }
        
		/* (non-Javadoc) @see org.jredis.cluster.ClusterModel#supports(org.jredis.cluster.ClusterType) */
        final public boolean supports (ClusterType type) {
        	return type == ClusterType.CONSISTENT_HASH;
        }
	}
}
