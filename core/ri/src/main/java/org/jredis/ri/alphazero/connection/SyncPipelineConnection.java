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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.jredis.ClientRuntimeException;
import org.jredis.ProviderException;
import org.jredis.RedisException;
import org.jredis.connector.ConnectionSpec;
import org.jredis.protocol.Command;
import org.jredis.protocol.Response;
import org.jredis.protocol.ResponseStatus;
import org.jredis.ri.alphazero.support.Assert;
import org.jredis.ri.alphazero.support.Log;

/**
 * Synchronous {@link PipelineConnectionBase} extension.
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Sep 7, 2009
 * @since   alpha.0
 * 
 */

public class SyncPipelineConnection extends PipelineConnectionBase {

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
    public SyncPipelineConnection (ConnectionSpec spec) throws ClientRuntimeException {
	    super(spec.setModality(Modality.Synchronous));
    }

    // ------------------------------------------------------------------------
    // Interface : Connection
    // ------------------------------------------------------------------------
    
	/* (non-Javadoc)
     * @see org.jredis.ri.alphazero.connection.ConnectionBase#serviceRequest(org.jredis.protocol.Command, byte[][])
     */
    @Override
    public Response serviceRequest (Command cmd, byte[]... args)
            throws RedisException, ClientRuntimeException, ProviderException 
    {

    	// queue the request
    	//
    	Future<Response> pendingResponse = queueRequest(cmd, args);
    	
    	// wait for response
    	//
    	Response response;
        try {
        	// This will block.
	        response = pendingResponse.get();
        }
        catch (InterruptedException e) {
	        e.printStackTrace();
	        throw new ClientRuntimeException("on pendingResponse.get()", e);
        }
        catch (ExecutionException e) {
        	if(e.getCause() instanceof RedisException) {
        		throw (RedisException) e.getCause();
        	}
        	else {
		        e.printStackTrace();
		        throw new ProviderException("on pendingResponse.get()", e);
        	}
        }
    	
        // check response status
        //
		ResponseStatus status = Assert.notNull (response.getStatus(), "status from response object", ProviderException.class);
		if(status.isError()) {
			Log.error ("Error response for " + cmd.code + " => " + status.message());
			throw new RedisException(cmd, status.message());
		}
		/* this is handled by the super class */
//		else if(status.code() == ResponseStatus.Code.CIAO) {
//			// normal for quit and shutdown commands.  we disconnect too.
//			disconnect();
//		}

		return response;
    }
}
