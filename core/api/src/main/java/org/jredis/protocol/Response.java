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

package org.jredis.protocol;

import java.io.InputStream;
import java.util.List;

import org.jredis.ProviderException;
import org.jredis.connector.Message;



/**
 * [TODO: detail the requirements - this is wip.]
 * 
 * <p>Response is a {@link Message} object that will read itself from a 
 * {@link InputStream} upon demand.  It is provided by {@link Protocol}s
 * as the result of a call to {@link Protocol#createResponse(Command)}.
 * 
 * A Response is a generalize contract and does not provide the necessary semantics 
 * corresponding to the data for the various response possibilities (such as bulk data
 * to a collection, value to bytes, etc.)  
 * 
 *<p>This specification also does not specify whether a response object can be
 * reused (to read the same command type response).  This is left to the provider
 * of the implementation.  If a provider does NOT wish to re-use responses,
 * they should raise a {@link ProviderException} in any subsequent calls to
 * {@link Response#read(InputStream)}.  This class, however, does not and will
 * not provide a 'reset' means.
 *  
/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 * 
 */
public interface Response extends Message {

	// -------------------------------------------------------------------------
	// Properties
	// -------------------------------------------------------------------------
	/** 
	 * @return <b>true</b> if response was an error response.  
	 */
	public boolean isError ();
	
	/** 
	 * @return {@link Response.Type} of this response. 
	 */
	public Type getType ();


	/**
	 * @return
	 */
	public ResponseStatus getStatus();
	
	/** 
	 * @return if response has been read.
	 * @see Response#read(java.io.InputStream)
	 */
	public boolean didRead ();

	
	// -------------------------------------------------------------------------
	// Associated type
	// ======================================================== Response.Type
	// -------------------------------------------------------------------------
	/**
	 * A redis server responds with: 
	 * <ul>
	 * <li>Status response - such as {@link Command#SET}
	 * <li>Value Data ("String") - such as {@link Command#GET}
	 * <li>Bulk Data - such as {@link Command#KEYS}
	 * <li>Multi Bulk Data - such as {@link Command#LRANGE}
	 * </ul>
	 * <p>
	 * This enum reflects these types and provides additional information regarding
	 * the expected flavor of the response data.
	 *
	 * @author  Joubin Houshyar (alphazero@sensesay.net)
	 * @version alpha.0, 04/02/09
	 * @since   alpha.0
	 * 
	 */
	public enum Type{
		/**  */
		Status 	  (ResponseStatus.class),
		/**  */
		Value 	  (Integer.class),
		/**  */
		Bulk  	  (byte[].class),
		/**  */
		MultiBulk (List.class);
		
		/**  */
		public final Class<?>	dataClass;
		/** @return the data flavor. */
		public Class<?>   getDataClass() { return dataClass; }
		/**
		 * @param clazz
		 */
		Type (Class<?> clazz){
			this.dataClass = clazz;
		}
	}
}
