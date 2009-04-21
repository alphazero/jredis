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

package org.jredis.ri.alphazero;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

import org.jredis.ClientRuntimeException;
import org.jredis.Command;
import org.jredis.ProviderException;
import org.jredis.RedisException;
import org.jredis.connector.Connection;
import org.jredis.connector.NotConnectedException;
import org.jredis.connector.Protocol;
import org.jredis.connector.Request;
import org.jredis.connector.Response;
import org.jredis.connector.ResponseStatus;
import org.jredis.ri.alphazero.support.Assert;
import org.jredis.ri.alphazero.support.Log;


/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 10, 2009
 * @since   alpha.0
 * 
 */
//public class SynchConnection extends SocketConnection implements Connection {
public class SynchConnection extends ConnectionBase implements Connection {

	// TODO: move to ConnectionSpec properties
	private static final int MAX_RECONNECTS = 3;

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	
	
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------
	/**
	 * 
	 * @param address
	 * @param port
	 * @param redisversion
	 * @throws ClientRuntimeException
	 * @throws ProviderException
	 */
	public SynchConnection (
			InetAddress 	address, 
			int 			port
		) 
		throws ClientRuntimeException, ProviderException 
	{
		this(address, port, RedisVersion.current_revision);
	}
	/**
	 * 
	 * @param address
	 * @param port
	 * @param redisversion
	 * @throws ClientRuntimeException
	 * @throws ProviderException
	 */
	public SynchConnection (
			InetAddress 	address, 
			int 			port, 
			RedisVersion 	redisversion
		) 
		throws ClientRuntimeException, ProviderException 
	{
		super(address, port);
		
		// get new and set Protocol handler delegate
		//
		Protocol protocolHdlr = new SynchProtocol();	// TODO: rewire it to get it from the ProtocolManager
		Assert.notNull (protocolHdlr, "the delegate protocol handler", ClientRuntimeException.class);
		Assert.isTrue(protocolHdlr.isCompatibleWithVersion(redisversion.id), "handler delegate supports redis version " + redisversion, ProviderException.class);
		
		setProtocolHandler(protocolHdlr);
	}

	// ------------------------------------------------------------------------
	// Interface
	// ======================================================= ProtocolHandler
	// ------------------------------------------------------------------------
	
//	@Override
	public final Modality getModality() {
		return Connection.Modality.Synchronous;
	}

	public Response serviceRequest (Command cmd, byte[]... args) 
		throws RedisException
	{
		if(!isConnected()) throw new NotConnectedException ("Not connected!");
		if(cmd == Command.AUTH) setCredentials (args[0]);

		
		Request  		request = null;
		Response		response = null;
		ResponseStatus  status = null;
		
//		boolean didServiceRequest = false;
		int		reconnectTries = 0;
		while(reconnectTries < spec.getReconnectCnt() ){
			try {
				// 1 - Request
//				Log.log("RedisConnection - requesting ..." + cmd.code);
				request = Assert.notNull(protocol.createRequest (cmd, args), "request object from handler", ProviderException.class);
				request.write(super.getOutputStream());
				
				// 2 - response
//				Log.log("RedisConnection - read response ..." + cmd.code);
				response = Assert.notNull(protocol.createResponse(cmd), "response object from handler", ProviderException.class);
				response.read(super.getInputStream());
				
				break;
//				didServiceRequest = true;
			}
			catch (ProviderException bug){
				Log.bug ("serviceRequest() -- ProviderException: " + bug.getLocalizedMessage());
				Log.log ("serviceRequest() -- closing connection ...");
				disconnect();
				throw bug;
			}
			catch (ClientRuntimeException cre) {
				Log.error("serviceRequest() -- ClientRuntimeException  => " + cre.getLocalizedMessage());
				Log.log ("serviceRequest() -- Attempting reconnect.  Tries: " + reconnectTries);
				reconnect();
				if(null!=getCredentials()) {
					this.serviceRequest(Command.AUTH, getCredentials());
				}
				reconnectTries++;
			}
			catch (RuntimeException e){
				e.printStackTrace();
				Log.bug ("serviceRequest() -- *unexpected* RuntimeException: " + e.getLocalizedMessage());

				Log.log ("serviceRequest() -- closing connection ...");
				disconnect();
				
				throw new ClientRuntimeException("unexpected runtime exeption: " + e.getLocalizedMessage(), e);
			}
		}
		
//		if(!didServiceRequest) {
//			String msg = "Failed to send request. Reconnect tries: " + reconnectTries + ".  Disconnecting";
//			disconnect ();
//			throw new ClientRuntimeException (msg);
//		}
		
		// 3 - Status
		//
		status = Assert.notNull (response.getStatus(), "status from response object", ProviderException.class);
		if(status.isError()) {
			Log.error ("Error response for " + cmd.code + " => " + status.message());
			throw new RedisException(cmd, status.message());
		}
		else if(status.code() == ResponseStatus.Code.CIAO) {
			// normal for quit and shutdown commands.  we disconnect too.
			disconnect();
		}

		return response;
	}
	public Response serviceRequest2(Command cmd, byte[]... args) 
		throws RedisException
	{
		if(!isConnected()) throw new NotConnectedException ("Not connected!");
		
		Request  		request = null;
		Response		response = null;
		ResponseStatus  status = null;

		try {
			// 1 - Request
			//
			request = Assert.notNull(protocol.createRequest (cmd, args), "request object from handler", ProviderException.class);
			
			boolean didRequest = false;
			int		reconnectTries = 0;
			while(!didRequest && reconnectTries < MAX_RECONNECTS){
				try {
					request.write(super.getOutputStream());
					didRequest = true;
				}
				catch (ClientRuntimeException problem) {
					Log.log ("[ClientRuntimeException in Request phase -- attempting reconnect] -- ");
					reconnect();
					reconnectTries++;
				}
				/* provider exceptions are bugs -- the outer try should catch it and log it */
			}
			if(!didRequest) 
				throw new ClientRuntimeException ("Failed to write request. Reconnect tries: " + reconnectTries);

			// 2 - Response
			//
			try {
				response = Assert.notNull(protocol.createResponse(cmd), "response object from handler", ProviderException.class);
				response.read(super.getInputStream());
			}
			catch (ProviderException bug) { throw bug; }
			catch (UnexpectedEOFException networkProblem) {
				// can't really handle this -- could still try reconnect just to be certain
				// TODO: *** reconnect ***!
				Log.error ("Unexpected EOF which servicing request -- network or possibly server problem");
				throw networkProblem;
			}
			catch (ConnectionResetException connectionProblem) {
				// this would happen if we had been idle for too long, but then we should have it this issue
				// on the send side, not reading the response --
				Log.error ("Unexpected connection reset by server while servicing request -- possible server problem");
				throw connectionProblem;
			}
			catch (ClientRuntimeException problem) {
				// TODO: check code in synchProtocol to see where a general CRE would be thrown 
				Throwable rootProblem = problem.getCause();
				if (null != rootProblem) {
					if (rootProblem instanceof SocketException) {
						// TODO: we shouldn't be throwing general CRE for socket related issues
						// synchProtocol is certainly aware that it is reading an socket input stream
						// we shouldn't be reading tea leaves here -- we should be getting explicit exceptions
						// such as above.
						Log.log ("[NET] [c.f. TODO in synchConnection] serviceRequest(%s) -- " +
								"problem in Response phase <rootProblem: %s>", cmd.code, rootProblem);
					}
					else if (rootProblem instanceof IOException) {
						Log.log ("[IO] [TODO (attempt reconnect?)]  serviceRequest(%s) -- " +
								"problem in Response phase <rootProblem: %s>", cmd.code, rootProblem);
					}
				}
				throw problem; // this is caught below under ClientR..
			}
			
			// 3 - Status
			//
			// sending response didn't cause any problems
			// check for redis errors
			//
			status = Assert.notNull (response.getStatus(), "status from response object", ProviderException.class);
			if(status.isError()) {
//				Log.error ("Request resulted in error: cmd: " + cmd.code + " " + status.message());
				Log.error (cmd.code + " => " + status.message());
				throw new RedisException(cmd, status.message());
			}
			else if(status.code() == ResponseStatus.Code.CIAO) {
//				Log.log ("serviceRequest() -- Response status is CIAO.");
//				Log.log ("serviceRequest() -- closing connection ...");
				disconnect();
			}
		}
		catch (ProviderException bug){
			Log.bug ("serviceRequest() -- ProviderException: " + bug.getLocalizedMessage());
			Log.log ("serviceRequest() -- closing connection ...");

			// now we must close the connection if we can
			disconnect();

			throw bug;
		}
		catch (ClientRuntimeException problem)
		{
			Log.problem ("serviceRequest() -- Unrecovered ClientRuntimeException: " + problem.getLocalizedMessage());
			Log.log ("serviceRequest() -- closing connection ...");

			// now we must close the connection if we can
			disconnect();
			
			throw problem;
		}
		catch (RuntimeException surprise){
			surprise.printStackTrace();
			// TODO: Log.log it ...
			Log.bug ("serviceRequest() -- *unexpected* RuntimeException: " + surprise.getLocalizedMessage());
			Log.log ("serviceRequest() -- closing connection ...");

			// now we must close the connection if we can
			disconnect();
			
			throw new ClientRuntimeException("unexpected runtime exeption: " + surprise.getLocalizedMessage(), surprise);
		}
		catch (Error disaster){
			disaster.printStackTrace();
			// TODO: Log.log it ...
			Log.bug ("serviceRequest() -- *unforseen* Java System Error: " + disaster.getLocalizedMessage());
			Log.log ("serviceRequest() -- closing connection ...");

			// now we must close the connection if we can
			disconnect();
			
			throw new ClientRuntimeException("unexpected system error: " + disaster.getLocalizedMessage(), disaster);
		}
		finally { }
		return response; 
	}
}
