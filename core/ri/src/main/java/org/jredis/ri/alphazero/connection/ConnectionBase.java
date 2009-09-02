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

import static org.jredis.connector.ConnectionSpec.SocketFlag.SO_KEEP_ALIVE;
import static org.jredis.connector.ConnectionSpec.SocketProperty.SO_PREF_BANDWIDTH;
import static org.jredis.connector.ConnectionSpec.SocketProperty.SO_PREF_CONN_TIME;
import static org.jredis.connector.ConnectionSpec.SocketProperty.SO_PREF_LATENCY;
import static org.jredis.connector.ConnectionSpec.SocketProperty.SO_RCVBUF;
import static org.jredis.connector.ConnectionSpec.SocketProperty.SO_SNDBUF;
import static org.jredis.connector.ConnectionSpec.SocketProperty.SO_TIMEOUT;
import static org.jredis.ri.alphazero.support.Assert.notNull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.jredis.ClientRuntimeException;
import org.jredis.NotSupportedException;
import org.jredis.ProviderException;
import org.jredis.RedisException;
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.connector.RequestListener;
import org.jredis.protocol.Command;
import org.jredis.protocol.Protocol;
import org.jredis.protocol.Response;
import org.jredis.ri.alphazero.support.Assert;
import org.jredis.ri.alphazero.support.Log;

/**
 * This abstract class is responsible for managing the socket connection, and, defining
 * the template of the Connection for concrete extensions.  Given that, it basically
 * manages all the details of dealing with {@link Socket}, and maintains the reference to
 * the handler.  
 * <p>
 * Further, it provides the default {@link NotSupportedException} response for the 
 * {@link Connection}'s methods that the extending classes of various {@link Connection.Modality}
 * are expected to support.  (They would simply implement the method that they support.)
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 20, 2009
 * @since   alpha.0
 * 
 */

public abstract class ConnectionBase implements Connection {

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	
	/** Protocol specific matters are delegated to an instance of {@link Protocol} */
	protected Protocol 			protocol;
	
	/** Connection specs used to create this {@link Connection} */
	protected final ConnectionSpec  spec;
	
	private InputStream		    input_stream;
	private OutputStream	    output_stream;

	private boolean 			isConnected = false;
	
	// TODO: not quite right ... in connection spec perhaps?
//	private byte[] 				credentials = null;

	// ------------------------------------------------------------------------
	// Internal use fields
	// ------------------------------------------------------------------------
	
	
	/** address of the socket connection */
	private final InetSocketAddress  	socketAddress;
	
	/** socket reference -- a new instance obtained in {@link ConnectionBase#newSocketConnect()} */
	private Socket	socket;
	
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------
	
	/**
	 * Will create a {@link DefaultConnectionSpec} and use that to instantiate.
	 * @see DefaultConnectionSpec
	 * @See {@link ConnectionSpec}
	 * 
	 * @param address
	 * @param port
	 * @param redisversion
	 * @throws ClientRuntimeException for null address or invalid port input, or, if connection
	 * attempt to specified host is not possible.
	 */
	public ConnectionBase (
			InetAddress 	address, 
			int 			port
		) 
		throws ClientRuntimeException
	{
		this (new DefaultConnectionSpec(address, port));
	}
	
	/**
	 * Will create and initialize a socket per the connection spec. Will connect immediately.
	 * 
	 * @See {@link ConnectionSpec}
	 * @param spec
	 * @throws ClientRuntimeException if connection attempt to specified host is not possible.
	 */
	public ConnectionBase (ConnectionSpec spec) 
		throws ClientRuntimeException
	{
		this(spec, true);
	}
	
	/**
	 * Will create and initialize a socket per the connection spec.
	 * @See {@link ConnectionSpec}
	 * @param spec
	 * @param connectImmediately will connect the socket immediately if true
	 * @throws ClientRuntimeException if connection attempt to specified host is not possible and
	 * connect immediate was requested.
	 */
	public ConnectionBase (ConnectionSpec spec, boolean connectImmediately) 
		throws ClientRuntimeException
	{
		try {
			this.spec = notNull(spec, "ConnectionSpec init parameter", ClientRuntimeException.class);
			socketAddress = new InetSocketAddress(spec.getAddress(), spec.getPort());
		}
		catch (IllegalArgumentException e) { 
			throw new ClientRuntimeException 
				("invalid connection spec parameters" + e.getLocalizedMessage(), e);
		}
		
		if(connectImmediately) {
			try {
				connect (); // throws CRE ..
			} 
			catch (IllegalStateException e) {
				throw new ProviderException("Unexpected error on initialize -- BUG", e);
			} 
		}
	}
	
	protected byte[]  getCredentials () {
		return this.spec.getCredentials();
	}
	
	protected int	getDatabase () {
		return this.spec.getDatabase();
	}

	// ------------------------------------------------------------------------
	// Inner ops
	/*
	 * Main responsibilities handled here are managing the socket instance, 
	 * including the reconnect facility.
	 */
	// ====================================================== socket management
	// ------------------------------------------------------------------------

	/**
	 * @return
	 */
	protected final boolean isConnected () {
		return isConnected;
	}
	
	
	/**
	 * Attempt reconnect.  Must be in a (previously) connected state when called.
	 * @throws IllegalStateException if not (logically) connected.
	 */
	protected final void reconnect () {
		Log.log("RedisConnection - reconnecting");
		int attempts = 0;

		while(true){
			try {
				disconnect();
				connect ();
				break;
			}
			catch (RuntimeException e){
				Log.error("while attempting reconnect: " + e.getMessage());
				if(++attempts == spec.getReconnectCnt()) {
					Log.problem("Retry limit exceeded attempting reconnect.");
					throw new ClientRuntimeException ("Failed to reconnect to the server.");
				}
			}
		}
	}
	/**
	 * @throws IOException
	 * @throws IllegalStateException
	 */
	protected final void connect () throws IllegalStateException, ClientRuntimeException {
		// we're not connected
		Assert.isTrue (!isConnected(), IllegalStateException.class);

		// create new socket and connect
		//
		try {
			newSocketConnect();
		} 
		catch (IOException e) {
			throw new ClientRuntimeException(
				"Socket connect failed -- make sure the server is running at " + spec.getAddress().getHostName(), e);
		}
		
		// get the streams
		//
		try {
			initializeSocketStreams ();
		} 
		catch (IOException e) {
			throw new ClientRuntimeException("Error obtaining connected socket's streams ", e);
		}
		
		isConnected = true;
//		Log.log("RedisConnection - connected");

	}

	/**
	 * @throws IllegalStateException
	 */
	protected final void disconnect () throws IllegalStateException {
		Assert.isTrue (isConnected(), IllegalStateException.class);
		
		socketClose();
		isConnected = false;

//		Log.log("RedisConnection - disconnected");
	}
	
	/**
	 * Instantiates a new {@link Socket}, sets its properties and flags using the {@link ConnectionBase#spec}
	 * and finally connects to the {@link ConnectionBase#socketAddress}.
	 * <p>
	 * Note that if the platform default send and receive buffers are larger than that specified, this method
	 * will <b>not</b> use the (smaller) values defined in the spec.
	 * <p>
	 * Further note that method will not check connection state. 
	 * 
	 * @throws IOException thrown by the socket object.
	 */
	private final void newSocketConnect () 
		throws IOException 
	{
		socket = new Socket ();
		
		socket.setKeepAlive (
				spec.getSocketFlag (SO_KEEP_ALIVE));
		
		socket.setPerformancePreferences(
				spec.getSocketProperty (SO_PREF_CONN_TIME),
				spec.getSocketProperty (SO_PREF_LATENCY),
				spec.getSocketProperty (SO_PREF_BANDWIDTH)); 

		socket.setSoTimeout(
				spec.getSocketProperty(SO_TIMEOUT));

		if(socket.getSendBufferSize() < spec.getSocketProperty(SO_SNDBUF))
			socket.setSendBufferSize(spec.getSocketProperty(SO_SNDBUF));
		
		if(socket.getReceiveBufferSize() < spec.getSocketProperty(SO_RCVBUF))
			socket.setReceiveBufferSize(spec.getSocketProperty(SO_RCVBUF));
		
		socket.connect(socketAddress);
		
//		Log.log("RedisConnection - socket connected to %s:%d", socketAddress.getHostName(), port);
	}

	/**
	 * 
	 */
	private final void socketClose () {
		try {
			if(null != socket) socket.close();
		}
		catch (IOException e) {
			Log.error("[IO] on closeSocketConnect -- socketClose() continues ..." + e.getLocalizedMessage());
		}
		finally {
			socket = null;
			input_stream = null;
			output_stream = null;
		}
	}
	
	/**
	 * @throws IllegalStateException if socket is null
	 * @throws IOException thrown by socket instance stream accessors
	 */
	private final void initializeSocketStreams() throws IllegalStateException, IOException {
		input_stream = Assert.notNull(socket.getInputStream(), "input_stream", IllegalStateException.class);
		output_stream = Assert.notNull(socket.getOutputStream(), "output_stream", IllegalStateException.class);

//		Log.log("RedisConnection - initialized i/o streams ");
	}
	// ------------------------------------------------------------------------
	// Interface
	// ============================================================ Connection
	/*
	 * These are the extension points for the concrete connection classes.
	 */
	// ------------------------------------------------------------------------

	@Override
	public Response serviceRequest(Command cmd, byte[]... args)
			throws RedisException, ClientRuntimeException, ProviderException 
	{
		throw new NotSupportedException (
				"Response.serviceRequest(Command cmd, " +
				"byte[]... args) is not supported.");
	}

	@Override
	public Response serviceRequest(RequestListener requestListener, Command cmd, byte[]... args) 
		throws RedisException, ClientRuntimeException, ProviderException 
	{
		throw new NotSupportedException (
				"Response.serviceRequest(RequestListener requestListener, " +
				"Command cmd, byte[]... args) is not supported.");
	}
	
	// ------------------------------------------------------------------------
	// Property accessors
	// ------------------------------------------------------------------------
	
	final protected void setProtocolHandler(Protocol protocolHandler) {
		this.protocol = notNull(protocolHandler, "protocolHandler for ConnectionBase", ClientRuntimeException.class);
	}
	
	final protected Protocol getProtocolHandler() {
		return notNull(protocol, "protocolHandler for ConnectionBase", ClientRuntimeException.class);
	}

	protected final OutputStream getOutputStream() {
		return output_stream;
	}

	protected final InputStream getInputStream() {
		return input_stream;
	}
}
