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

import java.net.InetAddress;

import org.jredis.ClientRuntimeException;
import org.jredis.ProviderException;
import org.jredis.RedisException;
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.connector.NotConnectedException;
import org.jredis.protocol.Command;
import org.jredis.protocol.Protocol;
import org.jredis.protocol.Request;
import org.jredis.protocol.Response;
import org.jredis.protocol.ResponseStatus;
import org.jredis.ri.alphazero.RedisVersion;
import org.jredis.ri.alphazero.protocol.SynchProtocol;
import org.jredis.ri.alphazero.support.Assert;
import org.jredis.ri.alphazero.support.Convert;
import org.jredis.ri.alphazero.support.Log;


/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 10, 2009
 * @since   alpha.0
 * 
 */
public class SynchConnection extends ConnectionBase implements Connection {

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	
	
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------
	
	/**
	 * This constructor will pass the connection spec to the super class constructor and
	 * create and install the {@link Protocol} handler delegate instance for this {@link SynchConnection}
	 * for the {@link RedisVersion#current_revision}.  
	 * 
	 * @param connectionSpec
	 * @throws ClientRuntimeException
	 * @throws ProviderException
	 */
	public SynchConnection (
			ConnectionSpec connectionSpec
		)
		throws ClientRuntimeException, ProviderException 
	{
		this(connectionSpec, RedisVersion.current_revision);
	}
	
	/**
	 * All the constructors in this class delegate to this constructor and are
	 * effectively provided for convenience.
	 * <p>
	 * This constructor will pass the connection spec to the super class constructor and
	 * create and install the {@link Protocol} handler delegate instance for this {@link SynchConnection}.
	 * If you definitely need to specify the redis server version, and the protocol implementation for that
	 * version exists, you should use this constructor.  Otherwise, it is recommended that the 
	 * {@link SynchConnection#SynchConnection(ConnectionSpec)}be used.
	 * <p>
	 * This constructor will open the socket connection immediately. 
	 *  
	 * @param connectionSpec
	 * @param redisversion
	 * @throws ClientRuntimeException due to either dns (host connectivity) or any IO issues related to establishing 
	 * the socket connection to the specified server.
	 * @throws ProviderException if the version specified is not supported.
	 */
	public SynchConnection (
			ConnectionSpec connectionSpec,
			RedisVersion 	redisversion
		)
		throws ClientRuntimeException, ProviderException 
	{
		super (connectionSpec);
		// get new and set Protocol handler delegate
		//
		Protocol protocolHdlr = new SynchProtocol();	// TODO: rewire it to get it from the ProtocolManager
//		Protocol protocolHdlr = new SharableSynchProtocol();	// TODO: rewire it to get it from the ProtocolManager
		Assert.notNull (protocolHdlr, "the delegate protocol handler", ClientRuntimeException.class);
		Assert.isTrue(protocolHdlr.isCompatibleWithVersion(redisversion.id), "handler delegate supports redis version " + redisversion, ProviderException.class);
		
		setProtocolHandler(protocolHdlr);
	}
	
	/**
	 * Creates a connection instance to the redis server at the specified address and port
	 * using the specified {@link RedisVersion} protocol handler (if available) and a
	 * default {@link ConnectionSpec}.
	 * 
	 * @param address
	 * @param port
	 * @param credentials 
	 * @param database 
	 * @param redisversion
	 * @throws ClientRuntimeException
	 * @throws ProviderException if the redisVersion specified is not supported.
	 */
	public SynchConnection (
			InetAddress 	address, 
			int 			port, 
			int database, byte[] credentials, RedisVersion 	redisversion
		) 
		throws ClientRuntimeException, ProviderException 
	{
//		this(new DefaultConnectionSpec(address, port, database, credentials), redisversion);
		this(getDefaultConnectionSpec(address, port, database, credentials), redisversion);
	}

	// ------------------------------------------------------------------------
	// Static methods
	// ------------------------------------------------------------------------
	/**
	 * Returns an instance of the {@link ConnectionSpec} used by this {@link Connection}
	 * as default spec, for the provided params.
	 * @param address
	 * @param port
	 * @param database
	 * @param credentials
	 * @param redisversion
	 * @return
	 * @throws ClientRuntimeException
	 */
	public static final ConnectionSpec getDefaultConnectionSpec (
			InetAddress 	address, 
			int 			port, 
			int 			database, 
			byte[] 			credentials
		) 
		throws ClientRuntimeException 
	{
		return new DefaultConnectionSpec(address, port, database, credentials);
	}
	// ------------------------------------------------------------------------
	// Interface
	// ======================================================= ProtocolHandler
	// ------------------------------------------------------------------------
	
	@Override
	public final Modality getModality() {
		return Connection.Modality.Synchronous;
	}

	public Response serviceRequest (Command cmd, byte[]... args) 
		throws RedisException
	{
		
		if(!isConnected()) throw new NotConnectedException ("Not connected!");
		
		Request  		request = null;
		Response		response = null;
		ResponseStatus  status = null;
		
		try {
			// 1 - Request
			//				Log.log("RedisConnection - requesting ..." + cmd.code);
			request = Assert.notNull(protocol.createRequest (cmd, args), "request object from handler", ProviderException.class);
			request.write(super.getOutputStream());

			// 2 - response
			//				Log.log("RedisConnection - read response ..." + cmd.code);
			response = Assert.notNull(protocol.createResponse(cmd), "response object from handler", ProviderException.class);
			response.read(super.getInputStream());

			//				break;
		}
		catch (ProviderException bug){
			Log.bug ("serviceRequest() -- ProviderException: " + bug.getLocalizedMessage());
			Log.log ("serviceRequest() -- closing connection ...");
			disconnect();
			throw bug;
		}
		catch (ClientRuntimeException cre) {
			Log.problem ("serviceRequest() -- ClientRuntimeException  => " + cre.getLocalizedMessage());
			reconnect();
			//							break;
			if(null!=getCredentials()) {
				this.serviceRequest(Command.AUTH, getCredentials());
			}
			if(getDatabase() != 0) {
				this.serviceRequest(Command.SELECT, Convert.toBytes(getDatabase()));
			}
		}
		catch (RuntimeException e){
			e.printStackTrace();
			Log.bug ("serviceRequest() -- *unexpected* RuntimeException: " + e.getLocalizedMessage());

			Log.log ("serviceRequest() -- closing connection ...");
			disconnect();

			throw new ClientRuntimeException("unexpected runtime exeption: " + e.getLocalizedMessage(), e);
		}
		
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
}
