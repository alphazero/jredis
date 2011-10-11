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

package org.jredis.connector;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.jredis.ClientRuntimeException;
import org.jredis.NotSupportedException;
import org.jredis.ProviderException;
import org.jredis.RedisException;
import org.jredis.protocol.Command;
import org.jredis.protocol.Protocol;
import org.jredis.protocol.Response;

/**
 * {@link Connection} defines the general (required) and optional 
 * contract of a JRedis connection.
 * <p>
 * Redis protocol does not provide for request sequencing (for a
 * variety of good reasons) and guaranteed delivery semantics are
 * supported only by multi-exec (redis transaction) protocol.  Accordingly,
 * {@link Connection} is not required (given that it can not) to support
 * fault-tolerant semantics.  
 * <p>
 * If a connection <i>faults</i> during an interaction with the server 
 * (e.g. anytime during send and receive) it must raise one of the following:
 * <ul>
 * <li>{@link ConnectionReset} - connection faulted but connection re-established
 * <li>{@link ConnectionFault} - connection faulted and reconnect not possible.
 * </ul>  
 * In the former case, the specific request that was being processed will not
 * be transparently re-issued by the connector.  The application layer must
 * determine what course of action to take.  
 * <p>
 * In either case, it should be noted that there exists a tiny (but possible)
 * window where even with append logging on the server, where the server received
 * and processed the request but never could reply (e.g. the server crashes for
 * whatever reason).  These considerations are irrelevant to read only commands,
 * but are significant in context of write ops (e.g. INCR).  If you require
 * guarantees on writes, you must use redis transactions (e.g. multi-bulk).
 * 
 * <p>
 * That said, the {@link Connection}'s optional event and state management
 * do provide sufficient support for a softer set of guarantees for 
 * transparent connection management (on fault detection, connect on demand,
 * etc.).
 * 
 * <br> 
 * @author  joubin (alphazero@sensesay.net)
 * @date    Sep 12, 2010
 * 
 */
public interface Connection {

	/**
	 * The {@link ConnectionSpec} of a Connection must be invariant during its life-cycle.
	 * @return the associated {@link ConnectionSpec} for this Connection. 
	 */
	public ConnectionSpec getSpec();
	
	/**
	 * A <b>blocking call</b> to service the specified request.  This method will return upon 
	 * the completion of the request response protocol with the connected redis server.  Timeouts
	 * and related matters are not addressed by this method or the {@link Protocol} interface
	 * and can (and should) be addressed at the implementation level (for example when creating 
	 * handler instances using a specification set, including max wait for synchronous response.)
	 * 
	 * <p>{@link Modality#Asynchronous} handlers must always throw a {@link ClientRuntimeException}
	 * for <b>this method which violates the contract for</b> {@link Modality#Asynchronous}  <b>handlers</b>.
	 * 
	 * @param cmd
	 * @param args
	 * @return
	 * @throws RedisException
	 * @throws ClientRuntimeException
	 * @throws ProviderException
	 */
	public Response serviceRequest (Command cmd, byte[]...args) throws RedisException, ClientRuntimeException, ProviderException; 
	
	
	/**
	 * A <b>non-blocking call</b> to service the specified request at some point in the future.  
	 * This method will return immediately with a {@link Future} object of parametric type {@link Response}
	 * <p>
	 * When the request is serviced, call to {@link Future#get()} will return the request response.
	 * <p>{@link Modality#Synchronous} handlers must always throw a {@link ClientRuntimeException}
	 * for <b>this method which violates the contract for</b> {@link Modality#Synchronous}  <b>handlers</b>.
	 * <p>
	 * If request resulted in a redis error ({@link RedisException}), the exception will be set as the cause of
	 * the corresponding {@link ExecutionException} of the {@link Future} object returned.
	 * @param cmd
	 * @param args
	 * @return the {@link Future} {@link Response}.
	 * @throws ClientRuntimeException
	 * @throws ProviderException
	 * @see Future
	 * @see ExecutionException
	 */

	public Future<Response> queueRequest (Command cmd,  byte[]...args) throws ClientRuntimeException, ProviderException;
	
	// ------------------------------------------------------------------------
	// State management -- optional
	/**
	 */
	public enum State {
		/** Connection is initialized and ready. Will connect on demand */
		INITIALIZED,
		/** Connection is established. */
		CONNECTED,
		/** Not connected to remote server.  Can connect on demand. */
		DISCONNECTED,
		/** Connection is shutdown and can be disposed. */
		TERMINATED
	}
	
	// ------------------------------------------------------------------------
	// Event management -- optional
	 
	 	/**
		 * Connection.Event propagation.
		 * @optional  
	 	 * @param connListener
		 * @return true if listener was successfully added.
	 	 */
	 	public boolean addListener(Listener connListener);
	 
	/**
	 * <b>Optional</b> event propagation method.  Removes the specified listenr
	 * from the list of {@link Connection.Listener}s.
	 * @param connListener
	 * @return true if the listener was actually present and was removed.
	 */
	public boolean removeListener(Listener connListener);

	// ========================================================================
	// Innner Types
	// ========================================================================
	
	/**
	 * Enumeration of the top-level properties of the {@link Connection} that can be
	 * specified by the User.  
	 *
	 * @author  joubin (alphazero@sensesay.net)
	 * @date    Sep 13, 2010
	 * 
	 */
	public enum Property {
		/** the redis server host name or ip */
		HOST,
		/** the Redis server port */
		PORT,
		/** the password used   */
		CREDENTIAL,
		/** The db selected on (re-)connect */
		DB,
		/** Defines the {@link Connection}'s {@link Connection.Modality} */
		MODALITY,
		/** On Connect (or reconnect after faults or timeouts) the maximum duration that you are willing to wait, milliseconds */
		MAX_CONNECT_WAIT,
		/** number of reconnect attempts after timeouts or faults. */
		MAX_CONNECT_ATTEMPT,
		/** if specified, is used to create the new protocol */
		PROTOCOL_CLASS,
		/** if specified, is used to create the new protocol */
		PROTOCOL_FACTORY,
		/** if specified, is used to create the new connection */
		CONNECTION_CLASS,
		/** if specified, is used to create the new connection. */
		CONNECTION_FACTORY,
		;// -- fini
	}
	/**
	 * Enum for defining the operational modality of the protocol handlers.
	 *   
	 * @author  Joubin Houshyar (alphazero@sensesay.net)
	 * @version alpha.0, 04/02/09
	 * @since   alpha.0
	 * 
	 */
	public enum Modality {
		/** blocking request/reply semantics */
		Synchronous,
		/** non-blocking request/future-response semantics */
		Asynchronous,
		/**  */
		PubSub,
		/**  */
		Monitor,
		;
		// -- end
	}
	
	/**
	 * {@link Connection} listeners' callback API.
	 * @optional
	 * @author  Joubin Houshyar (alphazero@sensesay.net)
	 * @version alpha.0, Mar 29, 2010
	 * @since   alpha.0
	 * 
	 */
	public interface Listener {
		public void onEvent(Connection.Event event);
	}
	
	/**
	 * Events raised by the {@link Connection}.
	 * <br>Events are typed and may have optional event info (Objects).
	 * @optional
	 * @author  Joubin Houshyar (alphazero@sensesay.net)
	 * @version alpha.0, Mar 29, 2010
	 * @since   alpha.0
	 * 
	 */
	@SuppressWarnings("serial")
    final public static class Event extends org.jredis.Event<Connection, Event.Type, Object>{
		/**
         * @param src
         * @param type
         */
        public Event (Connection src, Type type) {
	        super(src, type);
        }
        public Event (Connection src, Type type, Object eventInfo) {
	        super(src, type, eventInfo);
        }
		/** Connector.Event types. */
		public enum Type {
//			INITIALIZED,
			/** Raised when Connector is about to initiate the connect protocol */
			CONNECTING,
			/** Raised when Connector has established connectivity to the remote server */
			CONNECTED,
			/** Raised when Connector is about to initiate the disconnect protocol */
			DISCONNECTING,
			/** Raised when Connector has disconnected from the remote server */
			DISCONNECTED,
			/** Raised when the Connector encounters a {@link ClientRuntimeException} or {@link ProviderException}.  */
			FAULTED,
			/** 
			 * Raised to signal the beginning of the shutdown sequence (commences after listerners are notified.  
			 * Cease all activity on receipt 
			 * */
//			STOPPING,
			/** Raised when Connector is terminated.  Dispose of your references on receipt. */
			SHUTDOWN
		}
	}

	// ------------------------------------------------------------------------
    // Connection.Flag
    // ------------------------------------------------------------------------
    /**
     * Connection flags - not necessarily mutually exclusive.  Uses a 32 bit
     * mask for 32 (max) disctinct flags for Connnections.
     *
     * @author  joubin (alphazero@sensesay.net)
     * @date    Sep 18, 2010
     * 
     */
    public enum Flag {  	
    	/** if true will connect immediately on initialization.  otherwise on first use. */
    	CONNECT_IMMEDIATELY,
//    	/**  */
//    	TRANSPARENT_RECONNECT,
//    	/**  */
//    	RETRY_AFTER_RESET, 
    	/** if true uses pipelining */
    	PIPELINE,
    	/** Connection can be used by more than 1 client concurrently */
    	SHARED,
    	/** if true attempts to maintain connection. drops are detected. Better fault tolerance guarantees, w/ some performance impact */
    	RELIABLE,
    	/** if true connection maintains conversational state - for use with multi-exec */
    	STATEFUL,
    	/** if true service requests are logged (verbose/slower due to io)  */
    	TRACE,
    	;
		public final int bitmask;
		static final int OPAQUE_BITMASK = 0x0000;
		Flag (){
			this.bitmask = (int)Math.pow(2, ordinal());
		}
		static final public int bitset(Flag...flags){
			int bitset = OPAQUE_BITMASK;
			return bitset(bitset, flags);
		}
		static final public int bitset(final int bitset, Flag...flags){
			int _bitset = bitset;
			for(Flag f : flags) _bitset = _bitset | f.bitmask;
			return _bitset;
		}
		static final public int bitclear(final int bitset, Flag...flags){
			int _bitset = bitset;
			for(Flag f : flags) _bitset = _bitset ^ f.bitmask;
			return _bitset;
		}
		public static boolean isSet(int bitset, Flag flag) {
			return (bitset & flag.bitmask) > OPAQUE_BITMASK;
		}
    }
    
	// ------------------------------------------------------------------------
    // Connection.Socket
    // ------------------------------------------------------------------------
    public interface Socket {
    	/**
    	 * Flag keys for SocketFlag settings of the connection specification.
    	 * @see ConnectionSpec#getSocketFlag(SocketFlag)
    	 * @author Joubin Houshyar (alphazero@sensesay.net)
    	 *
    	 */
    	public enum Flag {
    		/** Corresponds to SO_KEEP_ALIVE flag of {@link Socket}.  @see {@link Socket#setSoTimeout(int)} */
    		SO_KEEP_ALIVE,
    		;
    	}
    	/**
    	 * Property keys for SocketProperty settings of the connection specification.
    	 * @see ConnectionSpec#getSocketProperty(SocketProperty)
    	 * @author Joubin Houshyar (alphazero@sensesay.net)
    	 *
    	 */
    	public enum Property {
    		/** 
    		 * Corresponds to <code><b>SO_SNDBUF</b></code> flag. see {@link Socket#setSendBufferSize(int)} 
    		 * <p>expected value is an <b><code>int</code></b> or an {@link Integer}.
    		 */
    		SO_SNDBUF,
    		
    		/** 
    		 * corresponds to SO_RCVBUF flag. see {@link Socket#setReceiveBufferSize(int)} 
    		 */
    		SO_RCVBUF,
    		
    		/** 
    		 * corresponds to SO_TIMEOUT flag.  see {@link Socket#setSoTimeout(int)} 
    		 */
    		SO_TIMEOUT,
    		
    		/**
    		 * Socket performance preferences.
    		 * <p> This property will be used in conjunction with other associated properties.
    		 * @See {@link SocketProperty#latency}
    		 * @See {@link SocketProperty#bandwidth}
    		 * @See {@link Socket#setPerformancePreferences(int, int, int)} for details.
    		 */
    		SO_PREF_CONN_TIME,

    		/**
    		 * Socket performance preferences.
    		 * <p> This property will be used in conjunction with other associated properties.
    		 * @See {@link SocketProperty#bandwidth}
    		 * @See {@link SocketProperty#connection_time}
    		 * @See {@link Socket#setPerformancePreferences(int, int, int)} for details.
    		 */
    		SO_PREF_LATENCY,

    		/**
    		 * Socket performance preferences.
    		 * <p> This property will be used in conjunction with other associated properties.
    		 * @See {@link SocketProperty#latency}
    		 * @See {@link SocketProperty#connection_time}
    		 * @See {@link Socket#setPerformancePreferences(int, int, int)} for details.
    		 */
    		SO_PREF_BANDWIDTH,
    	}
    }
	// ------------------------------------------------------------------------
    // Connection.Factor
    // ------------------------------------------------------------------------
    public interface Factory {
    	/**
    	 * Creates a connection to a redis server using the specified connection attributes.
    	 * @param spec of the new connection
    	 * @return a new {@link Connection} initialized per <code>spec</code>.
    	 * @throws ClientRuntimeException if the requirements exceed system resources/limits.
    	 * @throws NotSupportedException if the {@link ConnectionSpec} provided 
    	 * can not be supported by the provider.
    	 */
    	public Connection newConnection (ConnectionSpec spec) throws ClientRuntimeException, NotSupportedException;
    }
}
