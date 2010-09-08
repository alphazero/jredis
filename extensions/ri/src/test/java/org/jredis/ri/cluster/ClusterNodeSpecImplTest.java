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
import org.jredis.cluster.ClusterNodeSpecProviderTestBase;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 27, 2010
 * 
 */

public class ClusterNodeSpecImplTest extends ClusterNodeSpecProviderTestBase {
	/* (non-Javadoc) @see org.jredis.ri.ProviderTestBase#newProviderInstance() */
    @Override
    final protected ClusterNodeSpec newProviderInstance () {
	    return new DefaultClusterNodeSpec (DefaultConnectionSpec.newSpec());
    }
    /* (non-Javadoc) @see org.jredis.cluster.ClusterNodeSpecProviderTestBase#newProviderInstance(org.jredis.connector.ConnectionSpec) */
    @Override
    final protected ClusterNodeSpec newProviderInstance (ConnectionSpec connectionSpec) {
	    return new DefaultClusterNodeSpec (connectionSpec);
    }
}
