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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.jredis.ClientRuntimeException;
import org.jredis.NotSupportedException;
import org.jredis.ProviderException;
import org.jredis.RedisException;
import org.jredis.cluster.ClusterModel;
import org.jredis.cluster.ClusterNodeSpec;
import org.jredis.cluster.ClusterSpec;
import org.jredis.cluster.connector.ClusterConnection;
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.protocol.Command;
import org.jredis.protocol.Response;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 29, 2010
 * 
 */

abstract public class ClusterConnectionBase implements ClusterConnection, Connection.Listener {

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	/**  */
	final protected ClusterModel model;
	/**  */
	final private Set<Command> supportedCmds = new HashSet<Command>();
	/**  */
	final private Map<String, Connection> connections = new HashMap<String, Connection>();
	/** Connector Listeners */
	final private Set<Connection.Listener> listeners = new HashSet<Connection.Listener>();

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
	
	/* (non-Javadoc) @see org.jredis.cluster.connector.ClusterConnection#getSupportedCommands() */
	final public Collection<Command> getSupportedCommands () {
		return Collections.unmodifiableSet(supportedCmds);
	}

	/* (non-Javadoc) @see org.jredis.cluster.connector.ClusterConnection#supports(org.jredis.protocol.Command) */
	final public boolean supports (Command cmd) {
		return supportedCmds.contains(cmd);
	}
	
	/* (non-Javadoc) @see org.jredis.connector.Connection#queueRequest(org.jredis.protocol.Command, byte[][]) */
	public Future<Response> queueRequest (Command cmd, byte[]... args)
	        throws ClientRuntimeException, ProviderException 
    {
		byte[] key = verifyAndGetKeyForRequest(cmd, args);
		return getConnectionForKey(key).queueRequest(cmd, args);
	}

	/* (non-Javadoc) @see org.jredis.connector.Connection#serviceRequest(org.jredis.protocol.Command, byte[][]) */
	public Response serviceRequest (Command cmd, byte[]... args)
	        throws RedisException, ClientRuntimeException, ProviderException
    {
		byte[] key = verifyAndGetKeyForRequest(cmd, args);
		return getConnectionForKey(key).serviceRequest(cmd, args);
	}
	// ------------------------------------------------------------------------
	// Event management

	/* (non-Javadoc) @see org.jredis.connector.Connection#addListener(org.jredis.connector.Connection.Listener) */
	final public boolean addListener(Listener connListener){
		return listeners.add(connListener);
	}

	/* (non-Javadoc) @see org.jredis.connector.Connection#removeListener(org.jredis.connector.Connection.Listener) */
	final public boolean removeListener(Listener connListener){
		return listeners.remove(connListener);
	}

	// ------------------------------------------------------------------------
	// Interface
	// =================================================== Connection.Listener
	/*
	 * TODO: monitor all connections and deal with faults
	 */
	// ------------------------------------------------------------------------

	public void onEvent(Connection.Event event) {
		Connection conn = event.getSource();
		if(!connections.containsKey(conn)){
			throw new ProviderException("ClusterConnection receiving events for unrelated connection!");
		}
		
		// TODO: deal with it!
		Connection.Event.Type type = event.getType();
		switch (type) {
		case DISCONNECTED:
			break;
		case CONNECTED:
			break;
		case FAULTED:
			break;
		}
		throw new ProviderException("[BUG] lazy programmer -- TODOs here!");
	}

	// ------------------------------------------------------------------------
	// Internal ops
	// ------------------------------------------------------------------------

	final private byte[] verifyAndGetKeyForRequest(Command cmd, byte[]...args) 
	{
		notNull(args, "[BUG]: args for request is null!", ProviderException.class);
		isTrue(args.length > 0, "[BUG]: expecting at least 1 arg for the request (and a key at that)", ProviderException.class);
		isTrue(supports(cmd), cmd.name() + " is not supported", NotSupportedException.class);
		return args[0];
		
	}
	final protected void initialize () throws ClientRuntimeException, ProviderException {
		mapSupportedCommands();
		initializeConnections();
		initializeComponents();
	}

	final private void initializeConnections () throws ClientRuntimeException, ProviderException {
		for(ClusterNodeSpec nodeSpec : model.getSpec().getNodeSpecs()){
			Connection conn = null;
			if(getModality() == Connection.Modality.Synchronous){
				conn = notNull(createSynchConnection(nodeSpec), "", ProviderException.class);
			}
			else {
				conn = notNull(createAsynchConnection(nodeSpec), "", ProviderException.class);
			}
			connections.put(nodeSpec.getId(), conn);
			// TODO: add set as listener to connection
		}
	}
	/**
	 * Default implementation simply includes all {@link Command}s with {@link Command.RequestType}s
	 * that include key params in the request.
	 */
	final private void mapSupportedCommands () {
		// filter out the unsupported commands
		//
		for(Command cmd : Command.values()){
			switch (cmd.requestType){
			
			// -- NOT SUPPORTED --
			case BULK_SET:
			case NO_ARG:
			case VALUE:
				if(!affirmLackOfSupportFor(cmd))
					supportedCmds.add(cmd);
				break;

				// -- SUPPORTED --
			case KEY:
			case KEY_CNT_VALUE:
			case KEY_IDX_VALUE:
			case KEY_KEY:
			case KEY_KEY_VALUE:
			case KEY_NUM:
			case KEY_NUM_NUM:
			case KEY_NUM_NUM_OPTS:
			case KEY_SPEC:
			case KEY_VALUE:
			case MULTI_KEY:
				if(affirmSupportFor(cmd))
					supportedCmds.add(cmd);
				break;
			}
		}
	}

	final protected Connection getConnectionForKey(byte[] key){
		ClusterNodeSpec nodeSpec = model.getNodeForKey(key);
		String nodeId = nodeSpec.getId();
		
		// TEMP TEMP TEMP.
		return connections.get(nodeId);
	}
	// ------------------------------------------------------------------------
	// Internal ops : Extension points
	// ------------------------------------------------------------------------

	/**
	 * Extension point for subclasses.  This method is guaranteed to be called
	 * exactly once during the instantiation process. 
	 */
	abstract protected void initializeComponents () ;

	/**
	 * By default returns true.  Override to veto default command mappings.
     * @param cmd
     * @return
     */
    protected boolean affirmSupportFor (Command cmd) { return true; }

	/**
	 * By default returns true.  Override to veto default command mappings.
     * @param cmd
     * @return
     */
    protected boolean affirmLackOfSupportFor (Command cmd) {return true; }

	/**
     * @param nodeSpec
     * @return
     */
    protected Connection createAsynchConnection (ClusterNodeSpec nodeSpec) {
    	throw new ProviderException("Not implemented in the abstract base!");
    }

	/**
     * @param nodeSpec
     * @return
     */
    protected Connection createSynchConnection (ClusterNodeSpec nodeSpec) {
    	throw new ProviderException("Not implemented in the abstract base!");
    }


}
