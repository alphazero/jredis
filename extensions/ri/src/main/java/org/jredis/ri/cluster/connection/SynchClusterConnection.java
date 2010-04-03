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

package org.jredis.ri.cluster.connection;

import java.util.concurrent.Future;
import org.jredis.ClientRuntimeException;
import org.jredis.NotSupportedException;
import org.jredis.ProviderException;
import org.jredis.cluster.ClusterModel;
import org.jredis.cluster.ClusterNodeSpec;
import org.jredis.connector.Connection;
import org.jredis.protocol.Command;
import org.jredis.protocol.Response;
import org.jredis.ri.alphazero.connection.SynchConnection;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Apr 3, 2010
 * 
 */

public class SynchClusterConnection extends ClusterConnectionBase {

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	
	/**
     * @param model
     * @throws ClientRuntimeException
     */
    protected SynchClusterConnection (ClusterModel model, boolean connectImmediately) throws ClientRuntimeException {
	    super(model, connectImmediately);
    }

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------
	/**
     * @param model
     * @throws ClientRuntimeException
     */
    protected SynchClusterConnection (ClusterModel model) throws ClientRuntimeException {
	    super(model);
    }

	/* (non-Javadoc) @see org.jredis.ri.cluster.connection.ClusterConnectionBase#initializeComponents() */
	@Override
	protected void initializeComponents () {
		// TODO Auto-generated method stub
		throw new ProviderException("[LAZY] implement me!");
	}

	// ------------------------------------------------------------------------
	// Interface
	// ===================================================== ClusterConnection
	/*
	 * General methods of the interface are supported in this base class and the
	 * rest left for the specialized extensions.
	 */
	// ------------------------------------------------------------------------
	
	/* (non-Javadoc) @see org.jredis.connector.Connection#getModality() */
	final public Modality getModality () { return Connection.Modality.Synchronous; }

	/* (non-Javadoc) @see org.jredis.connector.Connection#queueRequest(org.jredis.protocol.Command, byte[][]) */
	final public Future<Response> queueRequest (Command cmd, byte[]... args)
	        throws ClientRuntimeException, ProviderException 
    {
		throw new NotSupportedException("Not supported by abstract base class");
	}
	
	// ------------------------------------------------------------------------
	// Super overrides
	// ------------------------------------------------------------------------
	/**
     * @param nodeSpec
     * @return
     */
    protected Connection createSynchConnection (ClusterNodeSpec nodeSpec) {
    	Connection conn = null;
    	conn = new SynchConnection(nodeSpec.getConnectionSpec(), true);
    	return conn;
    }
}
