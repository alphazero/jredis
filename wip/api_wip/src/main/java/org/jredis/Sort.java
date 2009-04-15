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


package org.jredis;

import java.util.List;


/**
 * Sort, a type of {@link Query}, provides for specifying and chaining the query predicates.  The results of the sort directive are returned
 * as an ordered {@link List} of <code>byte[]</code> values.
 *
 * <p>Sort instances are obtained directly from the {@link JRedis} client, by calling the eponymous method {@link JRedis#sort(String)}, which expects
 * a <i>key name</i> as its required arguments.  
 * 
 * <br><i>[Note: this provides the pattern for all future query types: required args are specified in the method that returns the {@link Query} form.]</i>
 *  
 * <p>As with all {@link Query} forms, 
 * <b>do not forget to call {@link Query#exec()} at the end</b>! 
 *
 * <p>Usage:
 * <p><code><pre>
 * List<byte[]>  results = redis.<b>sort</b>("my-list-or-set-key").BY("weight*").LIMIT(1, 11).GET("object*").DESC().ALPHA().<b>exec()</b>;
 * for(byte[] item : results) {
 *     // do something with item ..
 *  }
 * </pre></code>
 * <p>Sort specification elements are all optional.  You could simply say:
 * <p><code><pre>
 * List<byte[]>  results = redis.sort("my-list-or-set-key").<b>exec()</b>;
 * for(byte[] item : results) {
 *     // do something with item ..
 *  }
 * </pre></code>
 * <p>Sort specification elements are also can appear in any order -- the client implementation will send them to the server
 * in the order expected by the protocol, although it is good form to specify the predicates in natural order:
 * <p><code><pre>
 * List<byte[]>  results = redis.sort("my-list-or-set-key").GET("object*").DESC().ALPHA().BY("weight*").LIMIT(1, 11).<b>exec()</b>;
 * for(byte[] item : results) {
 *     // do something with item ..
 *  }
 * </pre></code>
 * 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 * 
 */
public interface Sort extends Query {
	/** species the BY clause */
	Sort BY    (String pattern);
	
	/** specifies the GET clause */
	Sort GET   (String pattern);
	
	/** specifies the LIMIT class: from is the intial index, count is the number of results */
	Sort LIMIT (long from, long count);
	
	/** default sort is ASCending -- use this in your sort to specify DESC sort */
	Sort DESC  ();
	
	/** sort is be default numeric -- use this to indicate lexiographic alphanumeric sort */
	Sort ALPHA ();
}
