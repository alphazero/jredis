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

package org.jredis;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Provides for specifying and chaining query predicates.  The results of the query directive are returned
 * as an ordered {@link List} of <code>byte[]</code> values.
 * 
 * <p>Specific {@link Query} types are obtained from the {@link JRedis} api.  Extensions of this interface
 * allow for the definition of natural expression of the query clauses.  Such interfaces should <b>only</b> 
 * define methods for the optional elements of the specific query type.  Redis interface itself will also
 * only allow for obtaining a reference to such an interface by specifying the required arguments in the
 * associated method signature.
 * 
 * <p>For example, to get a {@link Sort} instance, {@link JRedis} api specifies the required argument,  
 * (namely the <i>the key</i>) in the method signature for {@link JRedis#sort(String)}, and all the optional
 * elements of the SORT command are provided for in the {@link Sort} interface:
 * <p>
 * <pre><code>
 * Sort    sort = redis.sort (key)
 * List<byte[]>  results = BY("foo*").LIMIT(1, 11).GET("*woof*").DESC().<b>exec()</b>;
 * for(byte[] item : results) {
 *     // do something with item ..
 *  }
 * 
 * </code></pre>
 * 
 * @see Sort
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 * 
 */
public interface Query {
	
	/**
	 * Executes the query.
	 * 
	 * @return the resultant value list from redis.
	 * @throws IllegalStateException
	 * @throws RedisException
	 */
	// TODO: why illegal state?
	public List<byte[]> exec () throws IllegalStateException, RedisException;	
	public Future<List<byte[]>> execAsync ();	
	
	public static class Support {
		public static long unpackValue (List<byte[]> queryResult){
			if(null == queryResult) throw new ClientRuntimeException("queryResult is null");
			if(queryResult.size() < 1) throw new ClientRuntimeException("queryResult must have at least 1 entry");
			return Long.parseLong(new String(queryResult.get(0)));
		}
	}
}
