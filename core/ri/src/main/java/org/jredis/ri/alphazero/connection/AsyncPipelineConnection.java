/*
 *   Copyright 2009 Joubin Houshyar
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

package org.jredis.ri.alphazero.connection;

import org.jredis.ClientRuntimeException;
import org.jredis.connector.ConnectionSpec;

/**
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Sep 7, 2009
 * @since   alpha.0
 * 
 */

public class AsyncPipelineConnection extends PipelineConnectionBase{

    // ------------------------------------------------------------------------
    // Properties
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Constructor(s)
    // ------------------------------------------------------------------------
	/**
     * @param spec
     * @throws ClientRuntimeException
     */
    public AsyncPipelineConnection (ConnectionSpec spec) throws ClientRuntimeException {
	    super(spec.setModality(Modality.Asynchronous));
    }
}
