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

import java.util.Formatter;
import org.jredis.connector.ConnectionSpec;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 24, 2010
 * 
 */

public interface ClusterNodeSpec {
	/**
	 * @return
	 */
	public ConnectionSpec getConnectionSpec();
	
	/**
	 * All implementations must provide a cluster wide unique {@link String} identifier for a node.
	 * @return
	 */
	public String getId ();
	
	/**
	 * Each node in a consistent hashing scheme is represented a number of times to satisfy the requirements of the
	 * Consistent Hashing.  The implementation must satisfy the global uniqueness of the returned key within the associated
	 * cluster.
	 * <p>
	 * This is an optional method, but is required for all implementations supproting Consistent Hashing for sharded clusters.
	 * <p>
	 * See Consistent Hashing and Random Trees: Distributed Caching Protocols for Relieving Hot Spots on the World Wide Web
	 * (sec 4).
	 * @param rangeReplicationIndex 
	 * @return a globally unique {@link String} key. 
	 */
	public String getKeyForCHRangeInstance (int rangeReplicationIndex);
	
	// ------------------------------------------------------------------------
	// Reference Implementation 
	// ------------------------------------------------------------------------
	
	/**
	 * [TODO: document me!]
	 *
	 * @author  joubin (alphazero@sensesay.net)
	 * @date    Mar 24, 2010
	 * 
	 */
	public static class RefImpl implements ClusterNodeSpec {

		// ------------------------------------------------------------------------
		// Attrs
		// ------------------------------------------------------------------------
		/** {@link ConnectionSpec} of this node */
		final ConnectionSpec connSpec;
		
		/** Cluster wide unique identifier */
		final String id;
		
		// ------------------------------------------------------------------------
		// Constructor(s)
		// ------------------------------------------------------------------------
		
		/**
		 * @param connSpec
		 * @throws IllegalArgumentException 
		 */
		public RefImpl(ConnectionSpec connSpec){
			if(null == connSpec)
				throw new IllegalArgumentException("ConnectionSpec is null");
			
			this.connSpec = connSpec;
			this.id = generateId();
		}
		// ------------------------------------------------------------------------
		// Interface
		// ------------------------------------------------------------------------
		
		/* (non-Javadoc) @see org.jredis.cluster.ClusterNodeSpec#getConnectionSpec() */
        @Override
        final public ConnectionSpec getConnectionSpec () { return this.connSpec; }

		/* (non-Javadoc) @see org.jredis.cluster.ClusterNodeSpec#getId() */
        @Override
        final public String getId () { return this.id;}
		// ------------------------------------------------------------------------
		// Extension points
		// ------------------------------------------------------------------------
        /**
         * Method is called once (and only once) by the constructor to set the
         * final {@link RefImpl#id} field.  This (default) implementation simply
         * creates a string of form <ip-address-string-rep>:<0-padded-5-digit-port-number>.
         * <p>
         * Optional extension point.
         * @return
         */
        protected String generateId () {
        	Formatter fmt = new Formatter();
        	fmt.format("%s:%05d", 
        			this.connSpec.getAddress().getHostAddress(),
        			this.connSpec.getPort()
        		);
        	return fmt.toString();
        }

        /**
         * Default implementation will simply return a string of form
         * <node-id>[<@see org.jredis.cluster.ClusterNodeSpec#getKeyForCHRangeInstance(int)>]
         * <p>
         * Optional extension point. 
         * @param rangeReplicationIndex
         * @see org.jredis.cluster.ClusterNodeSpec#getKeyForCHRangeInstance(int)
         */
        @Override
        public String getKeyForCHRangeInstance (int rangeReplicationIndex) {
        	Formatter fmt = new Formatter();
        	fmt.format("%s[%d]",  this.id, rangeReplicationIndex);
        	return fmt.toString();
        }
	}
}
