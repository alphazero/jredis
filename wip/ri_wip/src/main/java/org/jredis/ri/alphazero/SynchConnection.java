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
import org.jredis.RedisException;
import org.jredis.connector.Connection;
import org.jredis.connector.NotConnectedException;
import org.jredis.connector.Protocol;
import org.jredis.connector.ProviderException;
import org.jredis.connector.Request;
import org.jredis.connector.RequestListener;
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
public class SynchConnection extends SocketConnection implements Connection {

	private Protocol 	protocolHandler;
	
	public SynchConnection (
			InetAddress 	address, 
			int 			port, 
//			ProtocolHandler protocolHandler,
			RedisVersion 	redisversion
		) 
		throws ClientRuntimeException, ProviderException 
	{
		super(address, port);
//		protocolHandler = ProtocolManager.getFactory().createProtocolHandler (Connection.Modality.Synchronous, redisversion.id);
		protocolHandler = new SynchProtocol();
		Assert.notNull(protocolHandler, "handlerDelegate from ProtocolManager", ProviderException.class);
		
		this.protocolHandler = Assert.notNull (protocolHandler, "the delegate protocol handler", ClientRuntimeException.class);
		Assert.isTrue(protocolHandler.isCompatibleWithVersion(redisversion.id), "handler delegate supports redis version " + redisversion, ClientRuntimeException.class);
	}

	// ------------------------------------------------------------------------
	// Interface
	// ======================================================= ProtocolHandler
	// ------------------------------------------------------------------------
	
//	@Override
	public final Modality getModality() {
		return Connection.Modality.Synchronous;
	}

	public Response serviceRequest(Command cmd, byte[]... args) 
		throws RedisException
	{
/*		
		if(start ==-1) start = System.currentTimeMillis();
 */		
		if(!isConnected()) throw new NotConnectedException ("Not connected!");
		Request  		request = null;
		Response		response = null;
		ResponseStatus  status = null;

		
		try {
			// 1 - Request
			//
			try {
				request = Assert.notNull(protocolHandler.createRequest (cmd, args), "request object from handler", ProviderException.class);
				request.write(super.getOutputStream());
			}
			catch (ProviderException bug) {throw bug; }
			catch (ClientRuntimeException problem) {
				Throwable rootProblem = problem.getCause();
				if (null != rootProblem && rootProblem instanceof SocketException) {
					Log.log ("[TODO -- attempt reconnect] serviceRequest() -- " +
							"unrecovered %s in Request phase <Cmd: %s>", rootProblem, cmd.code);
				}
				throw problem; // TODO: reconnect here ...
			}
			catch (RuntimeException everythingelse) {
				String msg = "For <Cmd: " +cmd.code+"> Possible bug in provider code: " +
					everythingelse.getClass().getSimpleName() + " => " + everythingelse.getLocalizedMessage();
				Log.error("serviceRequest() -- Request phase -- " + msg);
				throw new ProviderException(msg, everythingelse);
			}
			
			// 2 - Response
			//
			try {
				response = Assert.notNull(protocolHandler.createResponse(cmd), "response object from handler", ProviderException.class);
				response.read(super.getInputStream());
			}
			catch (ProviderException bug) { throw bug; }
			catch (ClientRuntimeException problem) {
				Throwable rootProblem = problem.getCause();
				if (null != rootProblem) {
					if (rootProblem instanceof SocketException) {
						Log.log ("[NET] [TODO (attempt reconnect?)] serviceRequest(%s) -- " +
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
		
/* - temp benchmarking request service throughput
 
		serviceCount ++;
		if(serviceCount > gate) {
			delta = System.currentTimeMillis() - start;
			start = System.currentTimeMillis();
			throughput = (serviceCount * 1000) / (float) delta;
			System.out.format ("<%s> - %8.2f /sec | serviced %d requests at %d msecs\n", Thread.currentThread().getName(), throughput, serviceCount, delta );
			serviceCount = 0;
		}
*/ 
		return response; 
	}
/*
	long serviceCount = 0;
	long start = -1;
	long delta = 0;
	float throughput = 0;
	int	 gate = 10000;
*/
// TODO: fixed the exception thrown below -- its a ProviderException
	/* ----------------------------------- NOT SUPPORTED  ----------------*/
//	@Override
	public Response serviceRequest(RequestListener requestListener, Command cmd, byte[]... args)
			throws RedisException, ClientRuntimeException, ProviderException 
	{
		throw new RuntimeException ("ProtocolHandler.serviceRequest not implemented! [Apr 10, 2009]");
	}
	/* ----------------------------------- NOT SUPPORTED  ----------------*/

}
