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

package org.jredis.ri.alphazero.connection;

import static org.jredis.connector.Connection.Socket.Property.SO_PREF_BANDWIDTH;
import static org.jredis.connector.Connection.Socket.Property.SO_PREF_CONN_TIME;
import static org.jredis.connector.Connection.Socket.Property.SO_PREF_LATENCY;
import static org.jredis.connector.Connection.Socket.Property.SO_RCVBUF;
import static org.jredis.connector.Connection.Socket.Property.SO_SNDBUF;
import static org.jredis.connector.Connection.Socket.Property.SO_TIMEOUT;
import static org.jredis.ri.alphazero.support.Assert.notNull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.jredis.ClientRuntimeException;
import org.jredis.NotSupportedException;
import org.jredis.ProviderException;
import org.jredis.RedisException;
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.connector.Connection.Event.Type;
import org.jredis.protocol.Command;
import org.jredis.protocol.Protocol;
import org.jredis.protocol.Response;
import org.jredis.ri.alphazero.protocol.DefaultProtocolFactory;
import org.jredis.ri.alphazero.support.Assert;
import org.jredis.ri.alphazero.support.Convert;
import org.jredis.ri.alphazero.support.FastBufferedInputStream;
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
 * @version alpha.0, Apr 20, 2009-2011
 * @since   alpha.0
 * 
 */

public abstract class ConnectionBase implements Connection{

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	
	/** 
	 * Thread specific protocol handler -- optimize fencing 
	 * Protocol specific matters are delegated to an instance of {@link Protocol} 
	 */
	ThreadLocal<Protocol> thrdProtocol = new ThreadLocal<Protocol>();
	
	/** Connection specs used to create this {@link Connection} */
	final protected ConnectionSpec  	spec;
	
	private boolean 			isConnected = false;
	/** socket reference -- a new instance obtained in {@link ConnectionBase#newSocketConnect()} */
	private java.net.Socket		socket;
	private InputStream		    instream;
	private OutputStream	    outstream;
	
	/** Connector Listeners */
	final private Set<Connection.Listener> listeners = new HashSet<Connection.Listener>();

//	/** Keep-alive heartbeat daemon thread  */
//	private HeartbeatJinn			heartbeat;

	/** address of the socket connection */
	private final InetSocketAddress  	socketAddress;
	
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------
	
	/**
	 * Will create and initialize a socket per the connection spec.
	 * @See {@link ConnectionSpec}
	 * @param spec
	 * @param connectImmediately will connect the socket immediately if true
	 * @throws ClientRuntimeException if connection attempt to specified host is not possible and
	 * connect immediate was requested.
	 */
	protected ConnectionBase (ConnectionSpec spec) 
		throws ClientRuntimeException
	{
		try {
			this.spec = notNull(spec, "ConnectionSpec init parameter", ClientRuntimeException.class);
			socketAddress = new InetSocketAddress(spec.getAddress(), spec.getPort());
			initializeComponents();
		}
		catch (IllegalArgumentException e) { 
			throw new ClientRuntimeException 
				("invalid connection spec parameters: " + e.getLocalizedMessage(), e);
		}
		catch (Exception e) {
			throw new ProviderException("Unexpected error on initialize -- BUG", e);
		} 

		if (spec.getConnectionFlag(Flag.CONNECT_IMMEDIATELY)) {
			connect ();
		}
	}

	// ------------------------------------------------------------------------
	// Interface
	// ============================================================ Connection
	/*
	 * These are the extension points for the concrete connection classes.
	 */
	// ------------------------------------------------------------------------

	@Override
	public ConnectionSpec getSpec () {
		return this.spec;
	}
	
	@Override
	public Response serviceRequest(Command cmd, byte[]... args)
			throws RedisException, ClientRuntimeException, ProviderException 
	{
		throw new NotSupportedException (
				"Response.serviceRequest(Command cmd, " +
				"byte[]...) is not supported.");
	}

	@Override
	public Future<Response> queueRequest(Command cmd, byte[]... args) 
		throws ClientRuntimeException, ProviderException 
	{
		throw new NotSupportedException (
				"Response.serviceRequest(RequestListener requestListener, " +
				"Object , Command, byte[]...) is not supported.");
	}
	
	// ------------------------------------------------------------------------
	// Event management

	/**
	 * Optional
	 * @param connListener
	 * @return
	 */
	final public boolean addListener(Listener connListener){
		return listeners.add(connListener);
	}

	/**
	 * Optional
	 * @param connListener
	 * @return
	 */
	final public boolean removeListener(Listener connListener){
		return listeners.remove(connListener);
	}
	
	// ------------------------------------------------------------------------
	// Internal ops : Extension points
	// ------------------------------------------------------------------------
	/**
     * Extension point: child classes may override for additional components:
     * <pre>
     * In the extended class:
     * <code>
     * protected void initializeComponents() {
     *    super.initializeComponents();
     *    // my components here ...
     *    //
     * }
     * </code>
     * </pre>
     */
    protected void initializeComponents () {
//		setProtocolHandler (Assert.notNull (newProtocolHandler(), "the delegate protocol handler", ClientRuntimeException.class));
		if(spec.getConnectionFlag(Connection.Flag.RELIABLE)){
			Log.log("WARNING: heartbeat is disabled.");
//	    	heartbeat = new HeartbeatJinn(this, this.spec.getHeartbeat(), " [" + this + "] heartbeat");
//	    	heartbeat.start();
		}
    }

    /**
     * Extension point -- callback on this method when {@link ConnectionBase} has connected to server.
     * <b>It is important to note that the extension must call super.notifyConnected</b> if reliable service (using
     * heartbeats) is required!.
     */
    protected void notifyConnected () {
    	notifyListeners(new Event(this, Type.CONNECTED));
    }
    /**
     * Extension point -- callback on this method when {@link ConnectionBase} has disconnected from server.
     * <b>It is important to note that the extension must call super.notifyDisconnected</b> if reliable service (using
     * heartbeats) is required!.
     */
    protected void notifyDisconnected () {
    	notifyListeners(new Event(this, Type.DISCONNECTED));
    }
    
    protected void notifyFaulted (String info) {
    	notifyListeners(new Event(this, Type.FAULTED, info));
    }
    protected void notifyShuttingDown () {
    	notifyListeners(new Event(this, Type.SHUTDOWN));
    }
    /**
     * Extension point:  child classes may override to return specific {@link Protocol} implementations per their requirements.
     * @return
     */
    protected Protocol newProtocolHandler () {
    	Protocol.Factory protfac = (Protocol.Factory) spec.getConnectionProperty(Property.PROTOCOL_FACTORY); 
    	if(protfac == null) protfac = new DefaultProtocolFactory();
    	return protfac.newProtocol(spec);
//
//    	return (new DefaultProtocolFactory()).newProtocol(spec);
    }
    
    /**
     * Extension point: override to return stream per requirement.  Base implementation uses {@link FastBufferedInputStream} by default,
     * with buffer size matching the SO_RCVBUF property of the {@link Connection}'s {@link ConnectionSpec}
     * @param socketInputStream
     * @return
     */
    @SuppressWarnings("boxing")
	protected InputStream newInputStream(InputStream socketInputStream) { 
    	return  new FastBufferedInputStream(socketInputStream, spec.getSocketProperty(SO_RCVBUF));
    }
    
    /**
     * Extension point: override to return stream per requirement.  Base implementation simply returns the input param
     * @param socketOutputStream
     * @return
     */
    protected OutputStream newOutputStream(OutputStream socketOutputStream) { return socketOutputStream; }
    
	// ------------------------------------------------------------------------
	// Inner ops: event management
	// ------------------------------------------------------------------------
	final protected void notifyListeners(Connection.Event e) {
		for(Connection.Listener l : listeners)
			l.onEvent(e);
	}

	// ------------------------------------------------------------------------
	// Inner ops: socket and connection management
	// ------------------------------------------------------------------------
	/** @return connected status*/
	protected final boolean isConnected () { return isConnected; }
	
	
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
					
					String faultmsg = String.format("Reconnect retry limit exceeded.  Failed to reconnect to the server after %d reconnect attempts", attempts);
					onConnectionFault(faultmsg, true);
				}
			}
		}
	}

	/**
	 * Will throw a {@link ClientRuntimeException} if raiseEx is true
	 * @throws IllegalStateException
	 */
	protected final void onConnectionFault (String fault, boolean raiseEx) throws ClientRuntimeException {
		notifyFaulted(fault);
		Log.problem("Shutting down due to connection FAULT: %s - %s", fault, this);
//		notifyShuttingDown();
		shutdown(); // REVU: best to add a pending shutdown flag ..
 		if(raiseEx) 
 			throw new ClientRuntimeException(fault);
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
			String faultmsg = String.format("Socket connect failed [cause: %s] -- make sure the server is running at %s:%d", e, spec.getAddress().getHostName(), spec.getPort());
			onConnectionFault(faultmsg, true);
//			throw new ClientRuntimeException(
//				"Socket connect failed -- make sure the server is running at " + spec.getAddress().getHostName(), e);
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
		
		try {
	        initializeOnConnect();
        }
        catch (RedisException e) {
        	// either authorize or db select is using invalid parameters
        	// which is user error
        	throw new IllegalArgumentException("Failed to connect -- check credentials and/or database settings for the connection spec", e);
        }
		
		Log.debug ("CONNECTED | conn: %s", toString());
		notifyConnected();
	}

	/**
	 * @throws IllegalStateException
	 */
	protected final void disconnect () throws IllegalStateException {
		Assert.isTrue (isConnected(), IllegalStateException.class); // REVU: client threads will keep banging on faulted pipelines ...
		
		socketClose();
		isConnected = false;

		notifyDisconnected();
		Log.debug ("DISCONNECTED | conn: %s", toString());
	}
	
	/**
	 * @throws IllegalStateException
	 */
	protected final void shutdown () throws IllegalStateException {
		notifyShuttingDown();
		/* client threads can be banging on a connection and queued up
		 * on a lock, so isConnected() may have been true
		 * before they acquired it.  
		 */
		if(isConnected)
			disconnect();
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
	@SuppressWarnings("boxing")
	private final void newSocketConnect () 
		throws IOException 
	{
		socket = new java.net.Socket ();
		
		socket.setKeepAlive (
				spec.getSocketFlag (Connection.Socket.Flag.SO_KEEP_ALIVE));
		
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
			instream = null;
			outstream = null;
		}
	}
	
	/**
	 * @throws IllegalStateException if socket is null
	 * @throws IOException thrown by socket instance stream accessors
	 */
	protected final void initializeSocketStreams() throws IllegalArgumentException, IOException {
		instream = newInputStream (Assert.notNull(socket.getInputStream(), "socket input stream", IllegalArgumentException.class));
		Assert.notNull(instream, "input stream provided by extended class", IllegalArgumentException.class);
		outstream = newOutputStream (Assert.notNull(socket.getOutputStream(), "socket output stream", IllegalArgumentException.class));
	}
	
	/**
	 * @throws RedisException 
	 * @throws ClientRuntimeException 
	 * @throws ProviderException 
     * 
     */
    protected final void initializeOnConnect () throws ProviderException, ClientRuntimeException, RedisException{
    	switch (spec.getModality()){
			case Asynchronous:
				initializeAsyncConnection();
				break;
			case Synchronous:
				initializeSyncConnection();
				break;
			default:
				throw new ProviderException("Modality " + spec.getModality().name() + " is not supported.");
    	}
    }

    /**
     * @throws ProviderException
     * @throws ClientRuntimeException
     * @throws RedisException
     */
    protected final void initializeSyncConnection () throws ProviderException, ClientRuntimeException, RedisException{
		if(null!=spec.getCredentials()) {
			this.serviceRequest(Command.AUTH, spec.getCredentials());
		}
		if(spec.getDatabase() != 0) {
			this.serviceRequest(Command.SELECT, Convert.toBytes(spec.getDatabase()));
		}
    }
    /**
     * @throws ProviderException
     * @throws ClientRuntimeException
     * @throws RedisException
     */
    protected final void initializeAsyncConnection () throws ProviderException, ClientRuntimeException, RedisException{
    	try {
    		if(null!=spec.getCredentials()) {
    			this.queueRequest(Command.AUTH, spec.getCredentials()).get();
    		}
    		if(spec.getDatabase() != 0) {
    			this.queueRequest(Command.SELECT, Convert.toBytes(spec.getDatabase())).get();
    		}
        }
        catch (InterruptedException e) {
	        e.printStackTrace();
	        throw new ClientRuntimeException("Interrupted while initializing asynchronous connection", e);
        }
        catch (ExecutionException e) {
	        e.printStackTrace();
	        if(e.getCause() != null){
	        	if(e.getCause() instanceof RedisException)
	        		throw (RedisException) e.getCause();
	        	else if(e.getCause() instanceof ProviderException)
	        		throw (ProviderException) e.getCause();
	        	else if(e.getCause() instanceof ClientRuntimeException)
	        		throw (ClientRuntimeException) e.getCause();
	        }
	        throw new ProviderException("Exception while initializing asynchronous connection", e);
        }
    }
	
    @SuppressWarnings("boxing")
	@Override
    public String toString() {
//    	String selfdesc = String.format("%s@%d", getClass().getSimpleName(), hashCode());
    	return String.format("Connection: %-12s %s:%d db:%d | %s@%d", spec.getModality().name().toUpperCase(), spec.getAddress(), spec.getPort(), spec.getDatabase(), getClass().getSimpleName(), hashCode());
    }
	// ------------------------------------------------------------------------
	// Property accessors
	// ------------------------------------------------------------------------
	
//	final protected void setProtocolHandler(Protocol protocolHandler) {
//		new Exception().printStackTrace();
//		System.out.format(">>>>>>>>>>> PROTOCOL HANDLER: %s\n", protocolHandler);
//		if(protocolHandler == null) throw new ProviderException("protocol handler is null - BUG");
//    	thrdProtocol.set(protocolHandler);
//		this.protocol = notNull(protocolHandler, "protocolHandler for ConnectionBase", ClientRuntimeException.class);
//	}
	
	final protected Protocol getProtocolHandler() {
		if(thrdProtocol.get() == null) {
			final Protocol protocol = Assert.notNull (newProtocolHandler(), "the delegate protocol handler", ClientRuntimeException.class);
	    	thrdProtocol.set(protocol);
		}
//		System.out.format("<<<<<<<<<<< PROTOCOL HANDLER: %s\n", thrdProtocol.get());
		return notNull(thrdProtocol.get(), "protocolHandler for ConnectionBase", ClientRuntimeException.class);
	}

	final protected OutputStream getOutputStream() {
		return outstream;
	}

	final protected InputStream getInputStream() {
		return instream;
	}
}
