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

package org.jredis.ri.cluster;

import java.net.Socket;
import java.util.Formatter;
import org.jredis.cluster.ClusterNodeSpec;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;

/**
 * A basic {@link ClusterNodeSpec} that uses the basic configuration information
 * (address, port) to generate unique ids and replication instance keys.  Note that
 * while {@link ConnectionSpec} as of JRedis 1.0 distinguishes between DBs, given
 * that the the Redis 1.3.n compliant library will have stateful connections that would
 * allow use of SELELCT to change the DB, this class also does NOT distinguish between
 * two {@link ConnectionSpec}s that are identical but have different DB specification.
 * 
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 25, 2010
 * 
 */

public class DefaultClusterNodeSpec extends ClusterNodeSpec.Support implements ClusterNodeSpec {
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
	public DefaultClusterNodeSpec(ConnectionSpec connSpec){
		super(connSpec);
	}
	// ------------------------------------------------------------------------
	// Interface
	// ------------------------------------------------------------------------
	
	public static ClusterNodeSpec getSpecFor(Socket conn){
		if(null == conn) throw new IllegalArgumentException("null arg [conn]");
		ConnectionSpec connSpec = DefaultConnectionSpec.newSpec(conn.getInetAddress(), conn.getPort(), 0, null);
		return new DefaultClusterNodeSpec(connSpec);
	}
	// ------------------------------------------------------------------------
	// Identity
	// ------------------------------------------------------------------------
    
	// ------------------------------------------------------------------------
	// Extension points
	// ------------------------------------------------------------------------
    /**
     * Method is called once (and only once) by the constructor to set the
     * final {@link Support#id} field.  This (default) implementation simply
     * creates a string of form <ip-address-string-rep>:<0-padded-5-digit-port-number>.
     * <p>ex: <code>"127.0.0.1:06379" </code>
     * <p>
     * <b>Method is not finalized to allow further specializations</b>
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
     * @see org.jredis.cluster.ClusterNodeSpec#getKeyForReplicationInstance(int)
     */
//    @Override
    public String getKeyForReplicationInstance (int rangeReplicationIndex) {
    	Formatter fmt = new Formatter();
    	fmt.format("%s[%d]",  this.id, rangeReplicationIndex);
    	return fmt.toString();
    }
}
