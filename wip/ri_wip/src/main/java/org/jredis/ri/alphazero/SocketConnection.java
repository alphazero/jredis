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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.jredis.ClientRuntimeException;
import org.jredis.Redis;
import org.jredis.connector.Protocol;
import org.jredis.connector.ProviderException;
import org.jredis.ri.alphazero.support.Assert;
import org.jredis.ri.alphazero.support.Log;


/**
 * [TODO: reconnect, timeoout, etc. ]
 * 
 * A delegating {@link Protocol} that is mainly responsible for managing the 
 * network connection to the redis server, and in support of the 
 * {@link ProtocolHandler#serviceRequest(Command, byte[]...)} method uses another,
 * redis version specific, protool handler to provide support for createRequest
 * and createResponse methods.  This class will provide the IO streams required by
 * Request and Response objects to be send and received.  
 * 
 * <p>This client will open and maintain a single socket connection to the server
 * identified and [TODO] will handle reconnects and timeouts (which is why it is
 * in the loop as a {@link Protocol}.
 * 
 * <p>This class only supports the {@link ProtocolHandler#serviceRequest(Command, byte[]...)}
 * method of the communication aspects of the protocol handler to insure control
 * over the sequence and modality of message processing.
 * 
 * <p><b>This class is not (as of now 04/02/09) thread-safe</b>.
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 *
 */
@Redis(versions={"*"})
public class SocketConnection  {
	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------

	private final InetAddress	address;
	private InetSocketAddress   socketAddress;
	private final int 			port;
	private InputStream		    input_stream;
	private OutputStream	    output_stream;
	private Socket				socket;
	private boolean isConnected = false;

	// ------------------------------------------------------------------------
	// Construct and initialize
	// ------------------------------------------------------------------------


	/**
	 * Creates the connection and initializes it.  Will connect immediately.
	 * 
	 * @param address
	 * @param port
	 * @param protocolHandler
	 * @param redisVersion
	 * @throws ClientRuntimeException if invalid or illegal arguments are provided (including
	 * the inet info.
	 * @throws ProviderException if protocol handler can not support the version.
	 */
	public SocketConnection(InetAddress address, int port) 
		throws 
			ClientRuntimeException, ProviderException
	{
		// get set
		
		this.address = Assert.notNull (address, "redis server address", ClientRuntimeException.class);
		this.port = Assert.inRange (port, 1, 65534, "redis server port", ClientRuntimeException.class);
		
		// ready to go 
		//
		initialize ();
	}
	// ------------------------------------------------------------------------
	// Extension point
	// ------------------------------------------------------------------------

	// well?
	
	// ------------------------------------------------------------------------
	// Inner ops
	// ------------------------------------------------------------------------
	
	/**
	 * Create, connect and get socket's streams.  Set this objects properties accordingly.
	 */
	private final void initialize() throws ClientRuntimeException, ProviderException {
		try {
			socketAddress = new InetSocketAddress(address, port);
			connect ();
		}
		catch (IOException e) { 
			throw new ClientRuntimeException 
			("Failed to open connection due to io error => " + e.getLocalizedMessage(), e);
		}
	}

	/**
	 * @return
	 */
	protected final boolean isConnected () {
		return isConnected;
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
	 * @throws IOException
	 * @throws IllegalStateException
	 */
	protected final void connect () throws IOException, IllegalStateException {
		Assert.isTrue (!isConnected(), IllegalStateException.class);
		socketConnect(true, 0, 1, 2);
		initializeSocketStreams ();
		isConnected = true;

//		Log.log("RedisConnection - connected.");
	}

	/**
	 * @param keepalive
	 * @param connectiontime
	 * @param latency
	 * @param bandwidth
	 * @throws IOException
	 */
	private void socketConnect (boolean keepalive, int connectiontime, int latency, int bandwidth) 
	throws IOException 
	{
		socket = new Socket ();
		socket.setKeepAlive (keepalive);
		socket.setPerformancePreferences(connectiontime, latency, bandwidth); 
		
		socket.connect(socketAddress);
		
//		Log.log("RedisConnection - socket connected to %s:%d", socketAddress.getHostName(), port);
	}

	/**
	 * 
	 */
	private void socketClose () {
		try {
			if(null != socket) socket.close();
		}
		catch (IOException e) {
			Log.error("[IO] on closeSocketConnect" + e.getLocalizedMessage());
		}
		catch (Exception e) {
			Log.error ("on closeSocketConnect" + e.getLocalizedMessage());
		}
		finally {
			socket = null;
			try {
				if(null != input_stream) input_stream.close();
				if(null != output_stream) output_stream.close();
			}
			catch (Exception e) {/* matter none */ }
			finally {
				input_stream = null;
				output_stream = null;
//				Log.log("RedisConnection - i/o streams closed.");
			}
//			Log.log("RedisConnection - socket closed");
		}
	}
	
	/**
	 * @throws ProviderException
	 * @throws IOException
	 */
	private final void initializeSocketStreams() throws ProviderException, IOException {
		input_stream = Assert.notNull(socket.getInputStream(), "input_stream", ProviderException.class);
		output_stream = Assert.notNull(socket.getOutputStream(), "output_stream", ProviderException.class);

//		Log.log("RedisConnection - initialized i/o streams ");
	}

	/**
	 * TODO: think about getting rid of this ...
	 * @return
	 */
	protected final OutputStream getOutputStream() {
		return output_stream;
	}

	/**
	 * TODO: ... likewise
	 * @return
	 */
	protected final InputStream getInputStream() {
		return input_stream;
	}
}
