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

package org.jredis.cluster.deprecated;

import java.util.HashMap;
import java.util.Map;
import org.jredis.cluster.ClusterSpec;
import org.jredis.cluster.support.HashAlgorithm;

/**
 * A relatively generalized notion of a distribution strategy for a cluster,
 * so we can swap various different favors.  
 * 
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 28, 2010
 * 
 */

@Deprecated
public interface ClusterModel_deprecated {
	// ------------------------------------------------------------------------
	// Distribution 
	// ------------------------------------------------------------------------
	/**
	 * @return
	 */
	public HashAlgorithm getHashAlgorithm();
	
	/**
	 * @return
	 */
	public ClusterNodeMapper getNodeMapper();
	
	/**
	 * @return
	 */
	public ClusterKeyMapper getKeyMapper();
	
	public Object getProperty(Property property);

	// ========================================================================
	// INNER CLASSES
	// ========================================================================
	
	/**
	 * TODO: place holder - will be sticking various attributes here for use w/
	 * the generalized property accessor.
	 *
	 * @author  joubin (alphazero@sensesay.net)
	 * @date    Mar 28, 2010
	 * 
	 */
	public enum Property {
	}
	
	// ------------------------------------------------------------------------
	// Implementation Support 
	// ------------------------------------------------------------------------
	/**
	 * provides an abstract extension point in support of the {@link ClusterModel_deprecated} 
	 * interface.  Actual construction of the various configurable elements (such as
	 * the node mapping algorithm, etc) are delegated to the child classes and this
	 * class takes care of the boiler plate accessors, etc.
	 *
	 * @author  joubin (alphazero@sensesay.net)
	 * @date    Mar 28, 2010
	 * 
	 */
	public abstract static class Support implements ClusterModel_deprecated {

		/**  */
		final protected ClusterSpec clusterSpec;
		
		/**  */
		final protected HashAlgorithm hashProvider;
		
		/**  */
		final protected ClusterNodeMapper nodeMapper;
		
		/**  */
		final protected ClusterKeyMapper keyMapper;
		
		final Map<Property, Object> properties = new HashMap<Property, Object>();
		
		// ------------------------------------------------------------------------
		// constructor (template) 
		// ------------------------------------------------------------------------
		
		/**
		 * A template pattern constructor, with guaranteed ordering of the extension
		 * point invocations:
		 * <ol>
		 * <li> {@link Support#newHashAlgorithm()}
		 * <li> {@link Support#newNodeMapper()}
		 * <li> {@link Support#newKeyMapper()}
		 * </ol>
		 * All extension point methods are invoked after the {@link ClusterSpec} property
		 * of this class has been set to a valid reference (for static clusters).  
		 * @param clusterSpec
		 */
		protected Support (ClusterSpec clusterSpec) {
			if(null == clusterSpec)
				throw new IllegalArgumentException("clusterSpec is null!");
			
			this.clusterSpec = clusterSpec;
			hashProvider = newHashAlgorithm ();
			nodeMapper = newNodeMapper ();
			keyMapper = newKeyMapper ();
		}
		// ------------------------------------------------------------------------
		// accessors
		// ------------------------------------------------------------------------
		
		/* (non-Javadoc) @see org.jredis.cluster.ClusterStrategy#getProperty(org.jredis.cluster.ClusterStrategy.Property) */
		final public Object getProperty(ClusterModel_deprecated.Property property) {
			return properties.get(property);
		}
		/* (non-Javadoc) @see org.jredis.cluster.DistributionStrategy#getHashAlgorithm() */
        final public HashAlgorithm getHashAlgorithm () {
	        return hashProvider;
        }

		/* (non-Javadoc) @see org.jredis.cluster.DistributionStrategy#getKeyMapper() */
        final public ClusterKeyMapper getKeyMapper () {
	        return keyMapper;
        }

		/* (non-Javadoc) @see org.jredis.cluster.DistributionStrategy#getNodeMapper() */
        final public ClusterNodeMapper getNodeMapper () {
	        return nodeMapper;
        }
		// ------------------------------------------------------------------------
		// Extension points
		// ------------------------------------------------------------------------
        /**
         * This method is guaranteed to be the called before all other extension points.
         * As with all extension points, method will only be invoked after the clusterSpec
         * property of the class has been set and is non-null
         * @param spec
         * @return
         */
        protected abstract HashAlgorithm newHashAlgorithm ();
        /**
         * As with all extension points, method will only be invoked after the clusterSpec
         * property of the class has been set and is non-null
         * @param spec
         * @return
         */
        protected abstract ClusterNodeMapper newNodeMapper ();
        /**
         * As with all extension points, method will only be invoked after the clusterSpec
         * property of the class has been set and is non-null
         * @param spec
         * @return
         */
        protected abstract ClusterKeyMapper newKeyMapper ();
	}
}
