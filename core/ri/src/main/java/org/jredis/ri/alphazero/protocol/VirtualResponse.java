/*
 *   Copyright 2009 Joubin Mohammad Houshyar
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

package org.jredis.ri.alphazero.protocol;

import java.io.InputStream;
import java.io.OutputStream;

import org.jredis.ClientRuntimeException;
import org.jredis.ProviderException;
import org.jredis.protocol.Command;
import org.jredis.protocol.Response;
import org.jredis.protocol.ResponseStatus;



/**
 * Certain requested commands do not have a corresponding response from
 * redis.  Quit and Shutdown are two examples as of now.  This response
 * provides a virtual response for this type of request.  You can set
 * the virtual response's status in the constructor.  The default constructor
 * provides for OK status.
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 * 
 */
public final class VirtualResponse implements Response {

	
	private final ResponseStatus	status;
	public VirtualResponse () { 
		this.status = ResponseStatus.STATUS_OK;
	}
	public VirtualResponse (ResponseStatus status) { 
		this.status = status;
	}

	@Override
	public boolean didRead() {return true;}

	@Override
	public ResponseStatus getStatus() { return status;}

	@Override
	public Type getType() { return Type.Status;}

	@Override
	public boolean isError() { return false;}

	@Override
	/**
	 * The purpose of this class is to provide responses that are not actually read from
	 * the server.  Typically this is for commands that closed the connection on the send, such
	 * as QUIT.
	 * @see Command#QUIT
	 * @see Command#SHUTDOWN
	 * @see org.jredis.connector.Message#read(java.io.InputStream)
	 */
	public void read(InputStream in) throws ClientRuntimeException, ProviderException { return;}

	@Override
	public void write(OutputStream out) throws ClientRuntimeException, ProviderException {
		throw new RuntimeException ("Streamable.write not implemented! [Apr 4, 2009]");
	}
}
