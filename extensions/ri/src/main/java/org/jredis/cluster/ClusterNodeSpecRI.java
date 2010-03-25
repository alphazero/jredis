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
 * @date    Mar 25, 2010
 * 
 */

public class ClusterNodeSpecRI extends ClusterNodeSpec.Support implements ClusterNodeSpec {
	// ------------------------------------------------------------------------
	// Attrs
	// ------------------------------------------------------------------------
	
	// ------------------------------------------------------------------------
	// Constructor(s)
	// ------------------------------------------------------------------------
	
	/**
	 * @param connSpec
	 * @throws IllegalArgumentException 
	 */
	public ClusterNodeSpecRI(ConnectionSpec connSpec){
		super(connSpec);
	}
	// ------------------------------------------------------------------------
	// Interface
	// ------------------------------------------------------------------------
	
	// ------------------------------------------------------------------------
	// Identity
	// ------------------------------------------------------------------------
    
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
    protected String generateId () {
    	Formatter fmt = new Formatter();
    	fmt.format("%s:%05d:%02d", 
    			this.connSpec.getAddress().getHostAddress(),
    			this.connSpec.getPort(),
    			this.connSpec.getDatabase()
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
