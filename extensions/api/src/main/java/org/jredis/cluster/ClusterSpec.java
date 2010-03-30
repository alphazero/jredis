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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 24, 2010
 * 
 */

// this is just a datastructure with a few methods -- make it a class
public interface ClusterSpec {

	// ------------------------------------------------------------------------
	// Node/Key space management and distribution 
	// ------------------------------------------------------------------------
	
	// TODO: may wish to use a simple enum here to spec the cluster type (i.e. ConsistentHashing)
	// and let the RIs supply a model to suite it.
//	
//	/**
//	 * @return the stretegy used to distribute keys across the node space.
//	 */
//	public ClusterStrategy  getStrategy();
//	
	public ClusterType getType ();
	public ClusterSpec setType (ClusterType clusterType);
	
	// ------------------------------------------------------------------------
	// Membership
	// ------------------------------------------------------------------------
	/**
	 * @return
	 */
	public Set<ClusterNodeSpec> getNodeSpecs();
	
	/**
	 * @param nodeSpec
	 * @return
	 * @throws IllegalArgumentException if nodeSpec provided is already present.
	 */
	public boolean addNode(ClusterNodeSpec nodeSpec);
	
	/**
     * @param nodeSpec
	 * @throws IllegalArgumentException if nodeSpec provided is already present.
     */
    public boolean removeNode (ClusterNodeSpec nodeSpec);
    
	/**
	 * Note that the collection must not include a null member.  
	 * 
	 * @param nodeSpecs is a {@link Collection} instead of a {@link Set} to 
	 * relax the type requirements, but the required semantics remains set like,
	 * and duplicate (per {@link ClusterNodeSpec#equals(Object)} elements will
	 * be rejected.
	 * @throws IllegalArgumentException if collection includes a null member
	 * @return per contract of {@link Set#addAll(Collection)}
	 */
	public boolean addAll(Collection<ClusterNodeSpec> nodeSpecs);
	
	/**
	 * Note that the collection must not include a null member.  
	 * 
	 * @param nodeSpecs
	 * @throws IllegalArgumentException if collection includes a null member
	 * @return per contract of {@link Set#removeAll(Collection)}
	 */
	public boolean removeAll(Collection<ClusterNodeSpec> nodeSpecs);
	
	// ========================================================================
	// INNER CLASSES
	// ========================================================================
	
	public abstract static class Support implements ClusterSpec {

//		/**  */
//		final protected ClusterModel distributionStrategy;
		private ClusterType type;
		
		/**  */
		final protected Set<ClusterNodeSpec> nodeSpecs = new HashSet<ClusterNodeSpec>();
		
		// ------------------------------------------------------------------------
		// constructor (template) 
		// ------------------------------------------------------------------------
		
		protected Support () { }
		
		// ------------------------------------------------------------------------
		// interface
		// ------------------------------------------------------------------------
		
		public ClusterType getType() { return type; }
		public ClusterSpec setType(ClusterType type) { this.type = type; return this; }
		
		/* (non-Javadoc) @see org.jredis.cluster.ClusterSpec#addAll(java.util.List) */
//      @Override
        public boolean addAll (Collection<ClusterNodeSpec> nodes) {
        	if(nodes.contains(null)) throw new IllegalArgumentException("collection includes a null member");
        	return  nodeSpecs.addAll(nodes);
        }

        /* (non-Javadoc) @see org.jredis.cluster.ClusterSpec#removeAll(java.util.Collection) */
//      @Override
        public boolean removeAll (Collection<ClusterNodeSpec> nodes) {
        	if(nodes.contains(null)) throw new IllegalArgumentException("collection includes a null member");
        	return nodeSpecs.removeAll(nodes);
        }

		/* (non-Javadoc) @see org.jredis.cluster.ClusterSpec#addNode(org.jredis.cluster.ClusterNodeSpec) */
//        @Override
        public boolean addNode (ClusterNodeSpec nodeSpec) {
        	if(null == nodeSpec)
    			throw new IllegalArgumentException("null nodeSpec");
        	
    		return this.nodeSpecs.add(nodeSpec);
        }

        /* (non-Javadoc) @see org.jredis.cluster.ClusterSpec#removeNode(org.jredis.cluster.ClusterNodeSpec) */
        public boolean removeNode (ClusterNodeSpec nodeSpec) {
        	if(null == nodeSpec)
    			throw new IllegalArgumentException("null nodeSpec");
        	
    		return nodeSpecs.remove(nodeSpec);
        }

		// ------------------------------------------------------------------------
		// accessors
		// ------------------------------------------------------------------------
        
//		
//    	/* (non-Javadoc) @see org.jredis.cluster.ClusterSpec#getDistributionStrategy() */
//    	final public ClusterStrategy  getStrategy() {
//    		return this.distributionStrategy;
//    	}
    	
		/* (non-Javadoc) @see org.jredis.cluster.ClusterSpec#getNodes() */
//        @Override
        final public Set<ClusterNodeSpec> getNodeSpecs () {
        	Set<ClusterNodeSpec> set = new HashSet<ClusterNodeSpec>(nodeSpecs.size());
        	for(ClusterNodeSpec spec : nodeSpecs)
        		set.add(spec);
	        return set;
        }

//		// ------------------------------------------------------------------------
//		// Extension points
//		// ------------------------------------------------------------------------
//        protected abstract ClusterModel newStrategy();
	}

}
