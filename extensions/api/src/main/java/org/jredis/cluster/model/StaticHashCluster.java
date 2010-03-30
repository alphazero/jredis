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

import org.jredis.NotSupportedException;
import org.jredis.cluster.ClusterModel;
import org.jredis.cluster.ClusterNodeSpec;
import org.jredis.cluster.ClusterSpec;
import org.jredis.cluster.ClusterType;
import org.jredis.cluster.support.HashAlgorithm;

/**
 * A basic {@link ClusterModel} that uses hashing of keys to distribute keys 
 * on a static set of nodes.  This type of cluster model obviously does not 
 * support reconfiguration and brittle in face of server node failures.
 * <p>
 * Given that, the implementors of interface:
 * <li> must return <b>false</b> for {@link ClusterModel#supportsReconfiguration()}
 * <li> must return <b>true</b> for {@link ClusterModel#supports(org.jredis.cluster.ClusterType)} for {@link ClusterType#STATIC_HASH}
 * <li> must return <b>false</b> for {@link ClusterModel#supports(org.jredis.cluster.ClusterType)} for all other {@link ClusterType}s.
 * <li> must throw {@link NotSupportedException} for {@link ClusterModel#addNode(org.jredis.cluster.ClusterNodeSpec)}
 * <li> must throw {@link NotSupportedException} for {@link ClusterModel#removeNode(org.jredis.cluster.ClusterNodeSpec)}
 * <li> must throw {@link NotSupportedException} for {@link ClusterModel#addListener(org.jredis.cluster.ClusterModel.Listener)}
 * <li> must throw {@link NotSupportedException} for {@link ClusterModel#removeListener(org.jredis.cluster.ClusterModel.Listener)}
 * 
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 30, 2010
 * @see ConsistentHashCluster
 * @see ClusterType
 */

public interface StaticHashCluster extends ClusterModel {

	public abstract static class Support extends ClusterModel.Support implements StaticHashCluster {

		/** */
		protected HashAlgorithm hashAlgo;
		
		/**  */
		protected ClusterNodeMap	nodeMap;
		
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
         * Extensions are expected to plugin their specific {@link HashAlgorithm} here.
         * @return
         */
        abstract protected HashAlgorithm newHashAlgorithm();
        
        /**
         * The meats and potatoes of this type of model. Invocation of this method
         * means a complete computation of the node map per the current {@link ClusterSpec}. 
         */
        abstract protected void mapNodes();
        
        /**
         * Can be overriden, but extending classes MUST call super.initializeComponents() as the
         * first statement in the overriding method.
         */
        protected void initializeComponents () {
        	nodeMap = newClusterNodeMap();
        	hashAlgo = newHashAlgorithm ();
        }

		/* (non-Javadoc) @see org.jredis.cluster.ClusterModel.Support#initializeModel() */
        @Override
        protected void initializeModel () {
        	initializeComponents();
        	mapNodes();
        }

		// ------------------------------------------------------------------------
		// finalized
		// ------------------------------------------------------------------------
        
		/* (non-Javadoc) @see org.jredis.cluster.ClusterModel.Support#onNodeAddition(org.jredis.cluster.ClusterNodeSpec) */
        @Override
        final protected boolean onNodeAddition (ClusterNodeSpec newNode) {
        	throw new NotSupportedException ("n/a");
        }

		/* (non-Javadoc) @see org.jredis.cluster.ClusterModel.Support#onNodeRemoval(org.jredis.cluster.ClusterNodeSpec) */
        @Override
        final protected boolean onNodeRemoval (ClusterNodeSpec newNode) {
        	throw new NotSupportedException ("n/a");
        }

		/* (non-Javadoc) @see org.jredis.cluster.ClusterModel#supports(org.jredis.cluster.ClusterType) */
        final public boolean supports (ClusterType type) {
	        return type == ClusterType.STATIC_HASH;
        }

		/* (non-Javadoc) @see org.jredis.cluster.ClusterModel#supportsReconfiguration() */
        final public boolean supportsReconfiguration () {
	        return false;
        }
	}
}
