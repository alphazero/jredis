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

import org.jredis.cluster.ClusterNodeSpec;
import org.jredis.cluster.ClusterSpec;
import org.jredis.cluster.ClusterSpecProviderTestBase;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.cluster.DefaultClusterSpec;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 27, 2010
 * 
 */

public class DefaultClusterSpecTest extends ClusterSpecProviderTestBase {
	/* (non-Javadoc) @see org.jredis.ri.ProviderTestBase#newProviderInstance() */
    @Override
    protected ClusterSpec newProviderInstance () {
	    return new DefaultClusterSpec ();
    }

	/* (non-Javadoc) @see org.jredis.cluster.ClusterSpecProviderTestBase#newNodeSpec(org.jredis.connector.ConnectionSpec) */
    @Override
    protected ClusterNodeSpec newNodeSpec (ConnectionSpec connectionSpec) {
	    return new DefaultClusterNodeSpec(connectionSpec);
    }
}
