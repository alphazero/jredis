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

import java.util.Set;
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
 * 
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 30, 2010
 * @see ConsistentHashCluster
 * @see ClusterType
 */

public interface StaticHashCluster extends ClusterModel {

	public abstract static class Support extends ClusterModel.Support implements StaticHashCluster {

		// ------------------------------------------------------------------------
		// Props
		// ------------------------------------------------------------------------
		/** */
		protected HashAlgorithm hashAlgo;
		/**  */
		protected int nodeCnt;
		/**  */
		protected ClusterNodeSpec[] nodes;
		
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
         * Extensions are expected to plugin their specific {@link HashAlgorithm} here.
         * @return
         */
        abstract protected HashAlgorithm newHashAlgorithm();
        
		// ------------------------------------------------------------------------
		// finalized super overrides
		// ------------------------------------------------------------------------
        
		/* (non-Javadoc) @see org.jredis.cluster.ClusterModel.Support#initializeModel() */
        @Override
        final protected void initializeModel () {
        	hashAlgo = newHashAlgorithm ();
        	Set<ClusterNodeSpec> nodeSpecs = clusterSpec.getNodeSpecs();
        	nodeCnt = nodeSpecs.size();
        	nodes = new ClusterNodeSpec[nodeCnt];
        	nodes = nodeSpecs.toArray(nodes);
        }

		/* (non-Javadoc) @see org.jredis.cluster.ClusterModel.Support#onNodeAddition(org.jredis.cluster.ClusterNodeSpec) */
        @Override
        final protected boolean onNodeAddition (ClusterNodeSpec newNode) {
        	throw new NotSupportedException ("node addition");
        }

		/* (non-Javadoc) @see org.jredis.cluster.ClusterModel.Support#onNodeRemoval(org.jredis.cluster.ClusterNodeSpec) */
        @Override
        final protected boolean onNodeRemoval (ClusterNodeSpec newNode) {
        	throw new NotSupportedException ("nodeRemoval");
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
