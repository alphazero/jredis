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

import java.util.HashMap;
import java.util.Map;

import org.jredis.ClientRuntimeException;
import org.jredis.NotSupportedException;
import org.jredis.connector.Connection;
import org.jredis.connector.Protocol;
import org.jredis.connector.ProtocolFactory;
import org.jredis.connector.ProviderException;
import org.jredis.ri.alphazero.support.Assert;
import org.jredis.ri.alphazero.support.Log;

import com.sun.corba.se.pept.protocol.ProtocolHandler;


/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 10, 2009
 * @since   alpha.0
 * 
 */
public class ProtocolManager implements ProtocolFactory {

	/**  */
	private static final ProtocolManager _instance = new ProtocolManager ();
	/**  */
	private static final Map<RedisVersion, Class<?>>  synchHandlers = new HashMap<RedisVersion, Class<?>>();
	private static final Map<RedisVersion, Class<?>>  asynchHandlers = new HashMap<RedisVersion, Class<?>>();

	static {
		Class<?>	v_0_09_class = SynchProtocol.class;
		Class<?>	default_class = SynchProtocol.class;

		synchHandlers.put(RedisVersion.current_revision, default_class);
		synchHandlers.put(RedisVersion.beta_0_09, v_0_09_class);
	}
	/**
	 * 
	 */
	private ProtocolManager () {}

	public static final ProtocolFactory getFactory() {
		return _instance;
	}

	/* (non-Javadoc)
	 * @see com.alphazero.jredis.protocol.ProtocolHandlerFactory#createProtocolHandler(java.lang.String)
	 */
//	@Override
	@SuppressWarnings("unchecked")
	public Protocol createProtocolHandler (Connection.Modality modality, String redisVersionId) 
		throws NotSupportedException, ClientRuntimeException, IllegalArgumentException
	{
		// filter out garbage
		//
		Assert.notNull(redisVersionId, "redisVersion parameter", IllegalArgumentException.class);

		RedisVersion version = null;
		boolean foundit = false;
		for (RedisVersion ver : RedisVersion.values()){
			if(ver.id.equalsIgnoreCase(redisVersionId)) { 
				version = ver; 
				foundit = true; 
				break; 
			}
		}
		if(!foundit) 
			throw new NotSupportedException ("Version " + redisVersionId + " is not supported.");
		
		Assert.notNull(version, "version", ProviderException.class);

		// create the handler
		//
		Class<ProtocolHandler>  handlerClass = null;
//		Protocol handler = null;
		switch (modality){
		case Asynchronous:
//			handler = createAsynchProtocolHandler(version);
			handlerClass = (Class<ProtocolHandler>) Assert.notNull(
					synchHandlers.get(version), "registered protocol handler for version " + version.id, 
					NotSupportedException.class);
			break;
		case Synchronous:
//			handler = createSynchProtocolHandler(version);
			handlerClass = (Class<ProtocolHandler>) Assert.notNull(
					asynchHandlers.get(version), "registered protocol handler for version " + version.id, 
					NotSupportedException.class);
			break;
		}
		// instantiate protocol handler object
		//
		Protocol handler = null;
		try {
			handler = (Protocol) handlerClass.getConstructor().newInstance();
			Assert.isTrue(handler.isCompatibleWithVersion(version.id), "supports version " + version.id, ProviderException.class);
		}
		catch (SecurityException e) {
			Log.problem("SecurityException when attempting to instantiate a " + handlerClass.getCanonicalName());
			throw new ClientRuntimeException ("Check the security policy -- we have a problem => "+ e.getLocalizedMessage(), e);
		}
		catch (Exception e) {
			String bugMsg = 
				"Couldn't instantiate handler of class " + 
				handlerClass.getCanonicalName() + " to service protocal version " + 
				version.id + "\n thrown => " + e.getLocalizedMessage(); 

			Log.bug(bugMsg);
			throw new ProviderException(bugMsg, e);
		}

		// done.
		return Assert.notNull(handler, "how did this happen??", ProviderException.class);
	}

//	/**
//	 * @param redisVersionId
//	 * @return
//	 * @throws NotSupportedException
//	 * @throws ClientRuntimeException
//	 * @throws IllegalArgumentException
//	 */
//	private Protocol createSynchProtocolHandler (RedisVersion version) 
//		throws NotSupportedException, ClientRuntimeException, IllegalArgumentException
//	{
//		Class<?> handlerClass = Assert.notNull(
//				synchHandlers.get(version), "registered protocol handler for version " + version.id, 
//				NotSupportedException.class);
//
//		// instantiate protocol handler object
//		//
//		Protocol handler = null;
//		try {
//			handler = (Protocol) handlerClass.getConstructor().newInstance();
//			Assert.isTrue(handler.isCompatibleWithVersion(version.id), "supports version " + version.id, ProviderException.class);
//		}
//		catch (SecurityException e) {
//			Log.problem("SecurityException when attempting to instantiate a " + handlerClass.getCanonicalName());
//			throw new ClientRuntimeException ("Check the security policy -- we have a problem => "+ e.getLocalizedMessage(), e);
//		}
//		catch (Exception e) {
//			String bugMsg = 
//				"Couldn't instantiate handler of class " + 
//				handlerClass.getCanonicalName() + " to service protocal version " + 
//				version.id + "\n thrown => " + e.getLocalizedMessage(); 
//
//			Log.bug(bugMsg);
//			throw new ProviderException(bugMsg, e);
//		}
//
//		// done.
//		return Assert.notNull(handler, "how did this happen??", ProviderException.class);
//	}
//	
//	/**
//	 * @param redisVersionId
//	 * @return
//	 * @throws NotSupportedException
//	 * @throws ClientRuntimeException
//	 * @throws IllegalArgumentException
//	 */
//	private Protocol createAsynchProtocolHandler (RedisVersion version) 
//		throws NotSupportedException, ClientRuntimeException, IllegalArgumentException
//	{
//			Class<?> handlerClass = Assert.notNull(
//					asynchHandlers.get(version), "registered protocol handler for version " + version.id, 
//					NotSupportedException.class);
//
//			// instantiate protocol handler object
//			//
//			Protocol handler = null;
//			try {
//				handler = (Protocol) handlerClass.getConstructor().newInstance();
//				Assert.isTrue(handler.isCompatibleWithVersion(version.id), "supports version " + version.id, ProviderException.class);
//			}
//			catch (SecurityException e) {
//				Log.problem("SecurityException when attempting to instantiate a " + handlerClass.getCanonicalName());
//				throw new ClientRuntimeException ("Check the security policy -- we have a problem => "+ e.getLocalizedMessage(), e);
//			}
//			catch (Exception e) {
//				String bugMsg = 
//					"Couldn't instantiate handler of class " + 
//					handlerClass.getCanonicalName() + " to service protocal version " + 
//					version.id + "\n thrown => " + e.getLocalizedMessage(); 
//
//				Log.bug(bugMsg);
//				throw new ProviderException(bugMsg, e);
//			}
//
//			// done.
//			return Assert.notNull(handler, "how did this happen??", ProviderException.class);
//	}
}
