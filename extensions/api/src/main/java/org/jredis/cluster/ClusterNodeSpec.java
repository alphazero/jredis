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

import java.util.Set;
import org.jredis.connector.ConnectionSpec;

/**
 * Contract:
 * <br>
 * Any given pair of {@link ClusterNodeSpec}s with identical <i>id</i>s (per {@link ClusterNodeSpec#getId()}) must
 * also return identical results for {@link ClusterNodeSpec#getKeyForReplicationInstance(int)} and will be considered equivalent. 
 * Implementations are required to appropriately override {@link Object#equals(Object)} to enforce this contract for use in 
 * Collections (such as {@link Set}.
 * 
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 24, 2010
 * 
 */

// this is just a data structure with a few methods - make it a class
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
	 * This is an optional method, but is required for all implementations supporting Consistent Hashing for sharded clusters.
	 * <p>
	 * See Consistent Hashing and Random Trees: Distributed Caching Protocols for Relieving Hot Spots on the World Wide Web
	 * (sec 4).
	 * @param rangeReplicationIndex 
	 * @return a globally unique {@link String} key. 
	 */
	public String getKeyForReplicationInstance (int rangeReplicationIndex);
	
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
	public abstract static class Support implements ClusterNodeSpec {

		// ------------------------------------------------------------------------
		// Attrs
		// ------------------------------------------------------------------------
		/** {@link ConnectionSpec} of this node */
		final protected ConnectionSpec connSpec;
		
		/** Cluster wide unique identifier */
		final protected String id;
		
		// ------------------------------------------------------------------------
		// Constructor(s)
		// ------------------------------------------------------------------------
		
		/**
		 * @param connSpec
		 * @throws IllegalArgumentException 
		 */
		public Support(ConnectionSpec connSpec){
			if(null == connSpec)
				throw new IllegalArgumentException("ConnectionSpec is null");
			
			this.connSpec = connSpec;
			this.id = generateId();
		}
		// ------------------------------------------------------------------------
		// Interface
		// ------------------------------------------------------------------------
		
		/* (non-Javadoc) @see org.jredis.cluster.ClusterNodeSpec#getConnectionSpec() */
//        @Override
        final public ConnectionSpec getConnectionSpec () { return this.connSpec; }

		/* (non-Javadoc) @see org.jredis.cluster.ClusterNodeSpec#getId() */
//        @Override
        final public String getId () { return this.id;}
        
		// ------------------------------------------------------------------------
		// Identity
		// ------------------------------------------------------------------------
        /**
         * Test for equality to enforce {@link ClusterNodeSpec} identity contract spec,
         * will compare the ids of the two objects.
         * @throws IllegalArgumentException if arg is null or not a {@link ClusterNodeSpec}
         */
        @Override
        final public boolean equals(Object o) {
        	if(null == o) throw new IllegalArgumentException("null argument");
        	ClusterNodeSpec n = null;
        	try { n = (ClusterNodeSpec) o; }
        	catch (ClassCastException e) { throw new IllegalArgumentException ("object is not a ClusterNodeSpec");}
        	
        	return this.getId().equals(n.getId());
        }
        
        /**
         * Enforece {@link ClusterNodeSpec} identity contract by returning the hashcode of the
         * id.  Delegates to {@link String#hashCode()}
         */
        @Override
        final public int hashCode() {
        	return this.getId().hashCode();
        }
        
        @Override
        public String toString() { return this.getId(); }
        
		// ------------------------------------------------------------------------
		// Extension points
		// ------------------------------------------------------------------------
        /**
         * Method is called once (and only once) by the constructor to set the
         * final {@link Support#id} field.  This (default) implementation simply
         * creates a string of form <ip-address-string-rep>:<0-padded-5-digit-port-number>:<0 padded 2-digit-db-number.
         * <p>
         * ex:
         * <code>
         * "127.0.0.1:06379:02" 
         * </code>
         * <p>
         * Optional extension point.
         * @return
         */
        protected abstract String generateId () ;
	}
}
