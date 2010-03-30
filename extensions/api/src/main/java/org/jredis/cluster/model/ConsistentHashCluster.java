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

public interface ConsistentHashCluster extends ClusterModel {
	/**
	 * @return
	 */
	ClusterNodeMap getNodeMap ();
	
	// ========================================================================
	// Inner Types
	// ========================================================================
	
	// ------------------------------------------------------------------------
	// Support
	// ------------------------------------------------------------------------
	
	public abstract static class Support extends ClusterModel.Support implements ConsistentHashCluster {

		// ------------------------------------------------------------------------
		// Properties
		// ------------------------------------------------------------------------
		
		/** what is a sensible value here? */
		protected static final double DEFAULT_REPLICATION_CONST = 10;
		/**  */
		protected ClusterNodeMap	nodeMap;
		/**  */
		protected int nodeReplicationCnt;
		
		// ------------------------------------------------------------------------
		// Properties
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
        abstract protected ClusterNodeMap newClusterNodeMap();
        
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
        protected void initializeComponents () {
        	nodeMap = newClusterNodeMap();
        }
        
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
        	nodeReplicationCnt = replicationCount();
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

//		/* (non-Javadoc) @see org.jredis.cluster.model.ConsistentHashCluster#getNodeMap() */
//        public ClusterNodeMap getNodeMap () {
//        	// THINK: not sure if returning the actual node map is a hot idea.
//        	// in fact, not sure if this is really a necessary method.
//        	// TODO: think.
//        	throw new RuntimeException("not implemented");        
//        }
        
		/* (non-Javadoc) @see org.jredis.cluster.ClusterModel#supports(org.jredis.cluster.ClusterType) */
        final public boolean supports (ClusterType type) {
        	return type == ClusterType.CONSISTENT_HASH;
        }

        // TODO: SHOULD IT BE YES?
//		/* (non-Javadoc) @see org.jredis.cluster.ClusterModel#supportsReconfiguration() */
//        public boolean supportsReconfiguration () {
//	        return false;
//        }
	}
}
