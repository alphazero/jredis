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

import java.util.HashSet;
import java.util.Set;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 24, 2010
 * 
 */

public interface ClusterSpec {
	
	/**
	 * @return
	 */
	public HashAlgorithm getHashAlgorithm();
	
	/**
	 * @return
	 */
	public NodeMappingAlgorithm getNodeMappingAlgorithm();
	
//	/**
//	 * DOES THIS REALLY BELONG HERE?
//	 * @return
//	 */
//	public int  getNodeReplicationCount();
	
//	/**
//	 * @param hashProvider
//	 * @return
//	 */
//	public ClusterSpec setHashProvider(HashProvider hashProvider);
	
	
	/**
	 * @return
	 */
	public Set<ClusterNodeSpec> getNodeSpecs();
	
	/**
	 * @param nodeSpec
	 * @return
	 * @throws IllegalArgumentException if nodeSpec provided is already present.
	 */
	public ClusterSpec addNodeSpec(ClusterNodeSpec nodeSpec);
	
	/**
	 * @param nodeSpecs
	 * @return
	 */
	public ClusterSpec addAll(Set<ClusterNodeSpec> nodeSpecs);
	
	// ------------------------------------------------------------------------
	// Reference Implementation 
	// ------------------------------------------------------------------------
	
	public abstract static class Support implements ClusterSpec {

		/**  */
		final protected HashAlgorithm hashProvider;
		
		/**  */
		final protected NodeMappingAlgorithm nodeMappingAlgorithm;
		
//		/**  */
//		final protected int nodeReplicationCount;
		/**  */
		final protected Set<ClusterNodeSpec> nodeSpecs = new HashSet<ClusterNodeSpec>();
		
		public Support() {
			this.hashProvider = newHashAlgorithm();
			this.nodeMappingAlgorithm = newNodeMappingAlgorithm();
			// TODO: mapping algo should recommend a replication count
			// but wait: isn't that Ketama/CH specific?
		}
		/* (non-Javadoc) @see org.jredis.cluster.ClusterSpec#addAll(java.util.List) */
        @Override
        public ClusterSpec addAll (Set<ClusterNodeSpec> nodeSpecs) {
        	for(ClusterNodeSpec nodeSpec : nodeSpecs){
        		addNodeSpec(nodeSpec);
        	}
	        return this;
        }

		/* (non-Javadoc) @see org.jredis.cluster.ClusterSpec#addNode(org.jredis.cluster.ClusterNodeSpec) */
        @Override
        public ClusterSpec addNodeSpec (ClusterNodeSpec nodeSpec) {
        	if(null == nodeSpec)
    			throw new IllegalArgumentException("null nodeSpec");
        	
    		if(!this.nodeSpecs.add(nodeSpec))
    			throw new IllegalArgumentException("nodeSpec [id: <"+nodeSpec.getId()+">] is already present");
    		
    		return this;
        }

		/* (non-Javadoc) @see org.jredis.cluster.ClusterSpec#getNodes() */
        @Override
        public Set<ClusterNodeSpec> getNodeSpecs () {
        	Set<ClusterNodeSpec> set = new HashSet<ClusterNodeSpec>(nodeSpecs.size());
        	for(ClusterNodeSpec spec : nodeSpecs)
        		set.add(spec);
	        return set;
        }

		/* (non-Javadoc) @see org.jredis.cluster.ClusterSpec#getHashProvider() */
        @Override
        public HashAlgorithm getHashAlgorithm () {
	        return hashProvider;
        }

		/* (non-Javadoc) @see org.jredis.cluster.ClusterSpec#getHashProvider() */
        @Override
        public NodeMappingAlgorithm getNodeMappingAlgorithm () {
	        return nodeMappingAlgorithm;
        }
		// ------------------------------------------------------------------------
		// Extension points
		// ------------------------------------------------------------------------
        protected abstract HashAlgorithm newHashAlgorithm();
        protected abstract NodeMappingAlgorithm newNodeMappingAlgorithm();
	}
}
