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

package org.jredis.cluster.connector;

import java.util.Collection;
import org.jredis.NotSupportedException;
import org.jredis.cluster.ClusterModel;
import org.jredis.cluster.ClusterNodeSpec;
import org.jredis.cluster.ClusterSpec;
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.protocol.Command;

/**
 * An extension of {@link Connection}, as the interface to a cluster of Redis
 * servers (nodes).  
 * <p>
 * Given that this interface effectively represents a group of {@link Connection}s,
 * the implementations must provide a modified semantics regarding the
 * {@link ConnectionSpec} related methods and properties.  
 * <p>
 * <li>In general, we can not expect all the connections in a given cluster to have 
 * identical {@link ConnectionSpec}s.  Calls to {@link Connection#getSpec()} must
 * throw a {@link NotSupportedException}.
 * <li>All the underlying {@link Connection}s for the cluster must have the
 * same {@link Connection.Modality} as that which is returned by the cluster's 
 * {@link Connection#getModality()} method.
 * <p>
 * Further, some implementations may elect to not support Redis {@link Command}s 
 * which are problematic in context of a cluster, and must accurately indicate
 * provision of support for {@link Command}s using the 
 * {@link ClusterConnection#supports(Command)} method and 
 * {@link ClusterConnection#getSupportedCommands()}.
 * 
 * 
 * @author  joubin (alphazero@sensesay.net)
 * @date    Apr 3, 2010
 * @see Connection
 * @see ClusterModel
 * @see ClusterSpec
 * @see ClusterNodeSpec
 * @see Command
 */

public interface ClusterConnection extends Connection{

	/**
	 * 
	 * @return the {@link ClusterSpec} instance associated with this {@link ClusterConnection}.
	 * The returned results must be identical to that which is returned by the 
	 * {@link ClusterModel#getSpec()} of the associated {@link ClusterModel} of this
	 * connection.
	 */
	public ClusterSpec getClusterSpec();
	
	/**
	 * @return the {@link ClusterModel} instance associated with this {@link ClusterConnection}
	 */
	public ClusterModel getClusterModel();
	
	/**
	 * Indicates whether the specific {@link Command} is supported by this {@link ClusterConnection}.
	 * @param cmd
	 * @return
	 */
	public boolean supports (Command cmd);
	
	/**
	 * @return the set of {@link Command}s supported by this {@link ClusterConnection}
	 */
	public Collection<Command> getSupportedCommands ();
}
