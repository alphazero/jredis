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
	public HashProvider getHashProvider();
	
	
	/**
	 * @return
	 */
	public Set<ClusterNodeSpec> getNodes();
	
	/**
	 * @param nodeSpec
	 * @return
	 * @throws IllegalArgumentException if nodeSpec provided is already present.
	 */
	public ClusterSpec addNode(ClusterNodeSpec nodeSpec);
	
	/**
	 * @param nodeSpecs
	 * @return
	 */
	public ClusterSpec addAll(Set<ClusterNodeSpec> nodeSpecs);
	
	// ------------------------------------------------------------------------
	// Reference Implementation 
	// ------------------------------------------------------------------------
	
	public static class RefImpl implements ClusterSpec {

		protected HashProvider hashProvider;
		
		final protected Set<ClusterNodeSpec> nodeSpecs = new HashSet<ClusterNodeSpec>();
		
		/* (non-Javadoc) @see org.jredis.cluster.ClusterSpec#addAll(java.util.List) */
        @Override
        public ClusterSpec addAll (Set<ClusterNodeSpec> nodeSpecs) {
	        // TODO Auto-generated method stub
	        return null;
        }

		/* (non-Javadoc) @see org.jredis.cluster.ClusterSpec#addNode(org.jredis.cluster.ClusterNodeSpec) */
        @Override
        public ClusterSpec addNode (ClusterNodeSpec nodeSpec) {
	        // TODO Auto-generated method stub
	        return null;
        }

		/* (non-Javadoc) @see org.jredis.cluster.ClusterSpec#getNodes() */
        @Override
        public Set<ClusterNodeSpec> getNodes () {
        	Set<ClusterNodeSpec> set = new HashSet<ClusterNodeSpec>(nodeSpecs.size());
        	for(ClusterNodeSpec spec : nodeSpecs)
        		set.add(spec);
	        return set;
        }

		/* (non-Javadoc) @see org.jredis.cluster.ClusterSpec#getHashProvider() */
        @Override
        public HashProvider getHashProvider () {
	        // TODO Auto-generated method stub
	        return null;
        }
		
	}

}
