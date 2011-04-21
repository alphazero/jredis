///*
// *   Copyright 2009 Joubin Houshyar
// * 
// *   Licensed under the Apache License, Version 2.0 (the "License");
// *   you may not use this file except in compliance with the License.
// *   You may obtain a copy of the License at
// *    
// *   http://www.apache.org/licenses/LICENSE-2.0
// *    
// *   Unless required by applicable law or agreed to in writing, software
// *   distributed under the License is distributed on an "AS IS" BASIS,
// *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *   See the License for the specific language governing permissions and
// *   limitations under the License.
// */
//
//package org.jredis.ri.alphazero;
//
//import java.util.concurrent.Semaphore;
//import org.jredis.ClientRuntimeException;
//import org.jredis.JRedis;
//import org.jredis.ProviderException;
//import org.jredis.RedisException;
//import org.jredis.connector.Connection;
//import org.jredis.connector.ConnectionSpec;
//import org.jredis.protocol.Command;
//import org.jredis.protocol.Response;
//import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
//import org.jredis.ri.alphazero.support.Assert;
//import org.jredis.ri.alphazero.support.Log;
//
///**
// * This class utilizes a (configurable) number of {@link Connection}s in a pool
// * and can be utilized in multi-threaded usage contexts, such as web containers,
// * etc.  
// * <p>
// * The service uses an internal pool of connections (see {@link JRedisService#default_connection_count}.  If
// * the number of service requests being processed reaches that limit, then any further calls will block until
// * a connection becomes available.  
// * <p>
// *
// * @author  Joubin Houshyar (alphazero@sensesay.net)
// * @version alpha.0, Apr 21, 2009
// * @since   alpha.0
// * 
// */
//
//public class JRedisService extends SynchJRedisBase {
//
//	// ------------------------------------------------------------------------
//	// Consts
//	// ------------------------------------------------------------------------
//	/** Default value: 5 */
//	public static final int 	default_connection_count = 5;
//	
//	// ------------------------------------------------------------------------
//	// Properties
//	// ------------------------------------------------------------------------
//
//	/** counting semaphore for limiting concurrent access to pool to the count */
//	private Semaphore   			connPoolAccess;
//	/** in use flag for the connections */
//	private boolean 				connInUse[];
//	/** the connections */
//	private Connection 				conns[];
//	
//	/** connection spec shared by all connections in pool */
//	private final ConnectionSpec 	connectionSpec;
//	/** number of connections in pool */
//	private final int				connCount;
//
//	// ------------------------------------------------------------------------
//	// Construct and initialize
//	// ------------------------------------------------------------------------
//	
//	/**
//	 * @param host
//	 * @param port
//	 */
//	public JRedisService (String host, int port) {
//		this(host, port, null, 0, default_connection_count);
//	}
//	
//	/**
//	 * @param host
//	 * @param port
//	 * @param password
//	 * @param database
//	 * @param connectionCount
//	 */
//	public JRedisService (String host, int port, String password, int database, int connectionCount) {
//		byte[] credentials = password != null ? password.getBytes() : null;
//		connectionSpec = DefaultConnectionSpec.newSpec(host, port, database, credentials);
//		connCount = connectionCount;
//		initialize();
//	}
//	
//	/**
//	 * @param connectionSpec
//	 * @param connectionCount
//	 */
//	public JRedisService (ConnectionSpec connectionSpec, int connectionCount) {
//		this.connectionSpec = connectionSpec;
//		// regardless of user spec, service has to assume shared connections
//		connectionSpec.setConnectionFlag(Connection.Flag.SHARED, true);
//		connCount = connectionCount;
//		
//		initialize();
//	}
//	
//	/**
//	 * Initialize the connection pool using the connection spec.
//	 */
//	private final void initialize () {
//		connPoolAccess = new Semaphore(connCount);
//		conns = new Connection[connCount];
//		connInUse = new boolean [connCount];
//		Connection conn = null;
//		connectionSpec.setConnectionFlag(Connection.Flag.RELIABLE, true);
//		connectionSpec.setConnectionFlag(Connection.Flag.SHARED, true);
//		for(int i=0; i< connCount;i++) {
//			try {
//				conn = Assert.notNull(createSynchConnection(connectionSpec), "Connection " + i, ClientRuntimeException.class);
//				conns[i] = conn;
//				connInUse[i] = false;
//			} 
//			catch (Exception e) {
//				// TODO: remove this stacktrace
//				e.printStackTrace();
//				throw new ClientRuntimeException("Could not create connection for service", e);
//			}		
//		}
//	}
//
//	// ------------------------------------------------------------------------
//	// super overrides.
//	// ------------------------------------------------------------------------
//	/* (non-Javadoc)
//	 * @see org.jredis.ri.alphazero.SynchJRedisBase#setConnection(org.jredis.connector.Connection)
//	 */
//	@Override
//	protected void setConnection(Connection connection) {
//		throw new RuntimeException("who called me?");
//	}
//
//	
//	/* (non-Javadoc)
//	 * @see org.jredis.ri.alphazero.JRedisSupport#serviceRequest(org.jredis.protocol.Command, byte[][])
//	 */
//	@Override
//	protected Response serviceRequest(Command cmd, byte[]... args)
//			throws RedisException, ClientRuntimeException, ProviderException 
//	{
//		Response response = null;
//		
//		try {
//			// we're using a counting semaphore to remain within
//			// bounds of the connection count
//			// if more than cnt requests are being serviced, we block here
//			connPoolAccess.acquire();
//			
//			int i = 0;
//			try {
//				// bare bones connection pool - mark the first available
//				// connection as inUse and then use it
//				// this is faster than using the synch collections of JDK and sufficient
//				synchronized(connInUse){	// Note: could use finer grained sync on conns[i] in loop instead ..
//					for(; i<connInUse.length;i++){
//						if(connInUse[i]==false) {
//							connInUse[i] = true;
//							break;
//						}
//					}
//				}
//				
//				// This shouldn't happen -- but lets make sure, hey?
//				if(i>=connCount) 
//					throw new ProviderException("BUG: JRedisService connection pool manager - index exceeds bounds!");
//				
//				response = conns[i].serviceRequest(cmd, args);
//			}
//			finally {
//				// return the connection to the pool
//				synchronized(connInUse){
//					if(connInUse[i] != true) 
//						throw new ProviderException("BUG: JRedisService connection pool manager - connection should have been marked in use!");
//					connInUse[i] = false;
//				}
//				
//				// release the bounds sem.
//				connPoolAccess.release();
//			}
//		} 
//		catch (InterruptedException e) {
//			// TODO: need meaningful action here
//			// its not clear what can be done since the interrupt is occurring on the
//			// user (calling) thread and is not connection specific, and the situation
//			// is ambiguous.  We can note it for now.
//			e.printStackTrace();
//			Log.log("Thread <%s> was interrupted in JRedisService.serviceRequest", Thread.currentThread().getName());
//		}
//		return response;
//	}
//	// ------------------------------------------------------------------------
//	// Interface
//	// =========================================================== Resource<T>
//	/*
//	 * Provides basic Resource support without any state management.  Extensions
//	 * that use context in a simply manner can rely on these methods.  Others may
//	 * wish to override.
//	 */
//	// ------------------------------------------------------------------------
//	
//	/* (non-Javadoc)
//	 * @see org.jredis.resource.Resource#getInterface()
//	 */
////	@Override
//	public JRedis getInterface() {
//		return this;
//	}
//}
