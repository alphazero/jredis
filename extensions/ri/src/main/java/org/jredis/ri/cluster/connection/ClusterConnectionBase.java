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

import static org.jredis.ri.alphazero.support.Assert.*;
import java.util.Collection;

import org.jredis.ClientRuntimeException;
import org.jredis.NotSupportedException;
import org.jredis.ProviderException;
import org.jredis.cluster.ClusterModel;
import org.jredis.cluster.ClusterNodeSpec;
import org.jredis.cluster.ClusterSpec;
import org.jredis.cluster.connector.ClusterConnection;
import org.jredis.connector.ConnectionSpec;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 29, 2010
 * 
 */

abstract public class ClusterConnectionBase implements ClusterConnection {

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	final protected ClusterModel model;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------
	/**
	 * Convenience form for default immediate connection to the nodes.
	 * @param model
	 * @throws ClientRuntimeException
	 * @see {@link ClusterConnectionBase#ClusterConnectionBase(ClusterModel, boolean)}
	 */
	protected ClusterConnectionBase (ClusterModel model) 
	throws ClientRuntimeException
	{
		this(model, true);
	}

	/**
	 * @param model
	 * @param connectImmediately
	 * @throws ClientRuntimeException
	 */
	protected ClusterConnectionBase (ClusterModel model, boolean connectImmediately) 
	throws ClientRuntimeException
	{
		this.model = notNull(model, "ClusterModel param for constructor", ClientRuntimeException.class); 
		
		// model integrity checks
		//
		ClusterSpec spec = notNull(model.getSpec(), "ClusterModel's ClusterSpec property", ClientRuntimeException.class); 
		Collection<ClusterNodeSpec> nodeSpecs = notNull(spec.getNodeSpecs(), "ClusterSpec's nodes", ClientRuntimeException.class);
		isTrue(nodeSpecs.size() > 1, "ClusterSpec node count", ClientRuntimeException.class);
		
		// initialize cluster's connections
		initialize();
	}

	// ------------------------------------------------------------------------
	// Interface
	// ===================================================== ClusterConnection
	/*
	 * General methods of the interface are supported in this base class and the
	 * rest left for the specialized extensions.
	 */
	// ------------------------------------------------------------------------
	
	/* (non-Javadoc) @see org.jredis.cluster.connector.ClusterConnection#getClusterModel() */
	final public ClusterModel getClusterModel () { return model; }

	/* (non-Javadoc) @see org.jredis.cluster.connector.ClusterConnection#getClusterSpec() */
	final public ClusterSpec getClusterSpec () { return model.getSpec(); }

	/* (non-Javadoc) @see org.jredis.connector.Connection#getSpec() */
	final public ConnectionSpec getSpec () {
		throw new NotSupportedException ("Per specification -- see org.jredis.cluster.ClusterConnection's specification.");
	}
	
	// ------------------------------------------------------------------------
	// Internal ops
	// ------------------------------------------------------------------------
	
	final protected void initialize () throws ClientRuntimeException, ProviderException {
		initializeComponents();
	}

	// ------------------------------------------------------------------------
	// Internal ops : Extension points
	// ------------------------------------------------------------------------
	
	/**
     * Extension point for subclasses.  This method is guaranteed to be called
     * exactly once during the instantiation process. 
     */
    abstract protected void initializeComponents () ;
}
