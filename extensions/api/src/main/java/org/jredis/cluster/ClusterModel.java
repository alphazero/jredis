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
import org.jredis.NotSupportedException;

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
	
	/**
	 * @return the {@link ClusterSpec} that accurately represents the current state
	 * of the model.
	 */
	public ClusterSpec getSpec();
	
	// ------------------------------------------------------------------------
	// Cluster semantics
	// ------------------------------------------------------------------------
	
	/**
	 * Maps the given key to a specified node of the cluster, by returning its
	 * {@link ClusterNodeSpec}.
	 * @param key
	 * @return
	 */
	public ClusterNodeSpec getNodeForKey (byte[] key);
	
	
	// ------------------------------------------------------------------------
	// Event management
	// ------------------------------------------------------------------------
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
		/**
		 * @param src
		 * @param type
		 */
		public Event (ClusterModel src, Type type) {
			this(src, type, null);
		}
		
		/**
		 * @return event approximate event generation time.
		 */
		public long getTimestamp () { return timestamp; }
		
		/**
		 * [TODO: document me!]
		 */
		public enum Type {
			NodeAdded,
			NodeRemoved
		}
	}
	// ------------------------------------------------------------------------
	// Support base
	// ------------------------------------------------------------------------
	abstract public static class Support implements ClusterModel {

		protected ClusterSpec clusterSpec;
		private final Set<Listener> listeners = new HashSet<Listener>();
		protected Object configLock = new Object();	
		
		public Support(ClusterSpec clusterSpec) {
			this.clusterSpec = clusterSpec;
		}
		

		/* (non-Javadoc) @see org.jredis.cluster.ClusterModel#addNode(org.jredis.cluster.ClusterNodeSpec) */
        final public void addNode (ClusterNodeSpec nodeSpec)
                throws IllegalArgumentException 
        {
        	synchronized (configLock) {
            	clusterSpec.addNode(nodeSpec);
            	onNodeAddition (nodeSpec);
            }
        	notifyListeners(new ClusterModel.Event(this, ClusterModel.Event.Type.NodeAdded, nodeSpec));
        }
		/* (non-Javadoc) @see org.jredis.cluster.ClusterModel#removeNode(org.jredis.cluster.ClusterNodeSpec) */
        final public void removeNode (ClusterNodeSpec nodeSpec) throws IllegalArgumentException {
        	if(!clusterSpec.getNodeSpecs().contains(nodeSpec)) throw new IllegalArgumentException("NodeSpec not part of cluster spec!");
        	synchronized (configLock) {
            	clusterSpec.removeNode(nodeSpec);
            	onNodeAddition (nodeSpec);
            }
        	notifyListeners(new ClusterModel.Event(this, ClusterModel.Event.Type.NodeRemoved, nodeSpec));
        }
        private final void notifyListeners(ClusterModel.Event e) {
        	for(ClusterModel.Listener l : listeners)
        		l.onEvent(e);
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
        abstract protected boolean onNodeAddition(ClusterNodeSpec newNode);
        abstract protected boolean onNodeRemoval(ClusterNodeSpec newNode);
	}
}
