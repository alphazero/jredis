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
import org.jredis.ClientRuntimeException;
import org.jredis.NotSupportedException;
import org.jredis.ProviderException;

/**
 * Represent (a potentially active) model of a cluster.  
 * <p>
 * Implementors may throw {@link NotSupportedException} for the optional methods.
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 28, 2010
 * 
 */

public interface ClusterModel {

	/**
	 * Indicates if the implementation supports the cluster type.  
	 * @param type
	 * @return
	 */
	public boolean supports(ClusterType type);
	
	/**
	 * Indicates if the {@link ClusterModel} implementation supports dynamic cluster configurations,
	 * @return true if yes, otherwise false.
	 */
	public boolean supportsReconfiguration();

	/**
	 * @return the {@link ClusterSpec} that accurately represents the current state
	 * of the model.
	 */
	public ClusterSpec getSpec();

	/**
	 * Optional.
	 * <p>
	 * Must raise a {@link ClusterModel.Event.Type#NodeAdded} event with the deleted node as
	 * the event's info.  Event must be raised after the model has transitioned to the new state.
	 * @param nodeSpec
	 * @throws IllegalArgumentException
	 */
	public void addNode(ClusterNodeSpec nodeSpec) throws IllegalArgumentException; 

	/**
	 * Optional.
	 * <p>
	 * Must raise a {@link ClusterModel.Event.Type#NodeAdded} event with the new node as
	 * the event's info.  Event must be raised after the model has transitioned to the new state.
	 * @param nodeSpec
	 * @throws IllegalArgumentException
	 */
	public void removeNode(ClusterNodeSpec nodeSpec) throws IllegalArgumentException;

	// ------------------------------------------------------------------------
	// Cluster semantics

	/**
	 * Maps the given key to a specified node of the cluster, by returning its
	 * {@link ClusterNodeSpec}.
	 * @param key
	 * @return
	 */
	public ClusterNodeSpec getNodeForKey (byte[] key);


	// ------------------------------------------------------------------------
	// Event management

	/**
	 * Optinal
	 * @param modelListener
	 * @return
	 */
	public boolean addListener(Listener modelListener);

	/**
	 * Optinal
	 * @param modelListener
	 * @return
	 */
	public boolean removeListener(Listener modelListener);

	// ========================================================================
	// Inner Types
	// ========================================================================


	// ------------------------------------------------------------------------
	// EventListener
	// ------------------------------------------------------------------------
	/**
	 * Your basic ClusterModel.Event Listener.
	 *
	 * @author  joubin (alphazero@sensesay.net)
	 * @date    Mar 29, 2010
	 * 
	 */
	public interface Listener {
		public void onEvent(ClusterModel.Event event);
	}

	// ------------------------------------------------------------------------
	// Model Events
	// ------------------------------------------------------------------------
	/**
	 * Optional event interface for dynamic models which support event
	 * generation.
	 * 
	 * @author  joubin (alphazero@sensesay.net)
	 * @date    Mar 29, 2010
	 * 
	 */
	public static class Event extends org.jredis.Event<ClusterModel, ClusterModel.Event.Type, ClusterNodeSpec> {

		/**  */
		private static final long serialVersionUID = 1L;
		/** generated at Event construction time */
		private final long	 timestamp;
		/**
		 * @param src
		 * @param type
		 * @param info
		 */
		public Event (ClusterModel src, Type type, ClusterNodeSpec info) {
			super(src, type, info);
			timestamp = System.currentTimeMillis();
		}
		
		/** @return event approximate event generation time. */
		public long getTimestamp () { return timestamp; }

		/** ClusterModel.Event.Types */
		public enum Type {
			Initialized,
			NodeAdded,
			NodeRemoved
		}
	}
	
	// ------------------------------------------------------------------------
	// Support base
	// ------------------------------------------------------------------------
	/**
	 * Provides basic support for the {@link ClusterModel} interface for the
	 * general semantics and features, and, extension points for specialized
	 * {@link ClusterModel}s.
	 * <p>
	 * Support for optional event management methods are provided.
	 *
	 * @author  joubin (alphazero@sensesay.net)
	 * @date    Mar 29, 2010
	 * 
	 */
	abstract public static class Support implements ClusterModel {

		// --------------------------------------------------------------------
		// properties
		// --------------------------------------------------------------------
		/**  */
		final protected ClusterSpec clusterSpec;
		/**  */
		final private Set<Listener> listeners = new HashSet<Listener>();
		/**  */
		final protected Object configLock = new Object();	


		// --------------------------------------------------------------------
		// Constructor
		// --------------------------------------------------------------------
		/**
		 * Instantiates and initializes the model.  This constructor will
		 * invoke {@link Support#initializeModel()} immediately after setting
		 * all parameters, including {@link ClusterSpec}.
		 * <p>
		 * Due to initialization cycle, this constructor may throw {@link ClientRuntimeException}
		 * or {@link ProviderException}s.  Refer to the implementation for details.
		 * <p>
		 * Methods that modify the cluster configuration, {@link ClusterModel#addNode(ClusterNodeSpec)} and
		 * {@link ClusterModel#removeNode(ClusterNodeSpec)}, will first use the 
		 * {@link ClusterModel#supportsReconfiguration()} to check if such operations 
		 * are supported.  If not, a {@link NotSupportedException} is thrown.
		 * <br>
		 * Otherwise, this implementaiton wwill first obtain the {@link Support#configLock}, 
		 * and then modify the associated {@link ClusterSpec}, and will invoke the 
		 * extension point {@link Support#onNodeAddition(ClusterNodeSpec)} or 
		 * {@link Support#onNodeRemoval(ClusterNodeSpec)} to signal the configuration 
		 * change to the specialized subclass. Finally, after releasing the model's 
		 * config lock, all listerners are notified of the configuration change with
		 * an appropriate {@link ClusterModel.Event}.
		 * 
		 * @param clusterSpec
		 */
		protected Support(ClusterSpec clusterSpec){
			if(null == clusterSpec) 
				throw new IllegalArgumentException("clusterSpec param is null");
			if(!supportsReconfiguration() && clusterSpec.getNodeSpecs().size() == 0)
				throw new IllegalArgumentException("clusterSpec has no ClusterNodeSpecs and this model can not be reconfigured.");
			
			this.clusterSpec = clusterSpec;
			initialize();
		}

		// --------------------------------------------------------------------
		// Interface
		// --------------------------------------------------------------------

		/* (non-Javadoc) @see org.jredis.cluster.ClusterModel#addNode(org.jredis.cluster.ClusterNodeSpec) */
		final public void addNode (ClusterNodeSpec nodeSpec)
		throws IllegalArgumentException 
		{
			if(supportsReconfiguration()){
				synchronized (configLock) {
					clusterSpec.addNode(nodeSpec);
					onNodeAddition (nodeSpec);
				}
				notifyListeners(new ClusterModel.Event(this, ClusterModel.Event.Type.NodeAdded, nodeSpec));
			}
			else {
				throw new NotSupportedException("Cluster reconfiguration not supported.");
			}
		}

		/* (non-Javadoc) @see org.jredis.cluster.ClusterModel#removeNode(org.jredis.cluster.ClusterNodeSpec) */
		final public void removeNode (ClusterNodeSpec nodeSpec) throws IllegalArgumentException {
			if(supportsReconfiguration()){
				if(!clusterSpec.getNodeSpecs().contains(nodeSpec)) throw new IllegalArgumentException("NodeSpec not part of cluster spec!");
				synchronized (configLock) {
					clusterSpec.removeNode(nodeSpec);
					onNodeAddition (nodeSpec);
				}
				notifyListeners(new ClusterModel.Event(this, ClusterModel.Event.Type.NodeRemoved, nodeSpec));
			}
			else {
				throw new NotSupportedException("Cluster reconfiguration not supported.");
			}
		}

		/* (non-Javadoc) @see org.jredis.cluster.ClusterModel#getSpec() */
		final public ClusterSpec getSpec () {
			return clusterSpec;
		}
		/* (non-Javadoc) @see org.jredis.cluster.ClusterModel#addListener(org.jredis.cluster.ClusterModelListener) */
		final public boolean addListener (Listener modelListener) {
			return listeners.add(modelListener);
		}
		/* (non-Javadoc) @see org.jredis.cluster.ClusterModel#removeListener(org.jredis.cluster.ClusterModelListener) */
		final public boolean removeListener (Listener modelListener) {
			return listeners.remove(modelListener);
		}

		// --------------------------------------------------------------------
		// Internal ops
		// --------------------------------------------------------------------
		private final void initialize () {
			initializeModel();
		}
		
		private final void notifyListeners(ClusterModel.Event e) {
			for(ClusterModel.Listener l : listeners)
				l.onEvent(e);
		}
		// --------------------------------------------------------------------
		// Extension Points
		// --------------------------------------------------------------------
		/**
		 * @param newNode
		 * @return
		 */
		abstract protected boolean onNodeAddition(ClusterNodeSpec newNode);
		/**
		 * @param newNode
		 * @return
		 */
		abstract protected boolean onNodeRemoval(ClusterNodeSpec newNode);
		/**
		 * 
		 */
		abstract protected void initializeModel();
	}
}
