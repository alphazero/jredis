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

import java.io.Serializable;
import java.util.List;
import java.util.Map;


/**
 * Temp doc:  
 * <p>This is effectively a one to one mapping to Redis commands.  And that
 * is basically it.
 * <p>Beyond that , just be aware that an implementation may throw {@link ClientRuntimeException}
 * or an extension to report problems (typically connectivity) or features {@link NotSupportedException}
 * or bugs.  These are {@link RuntimeException}.
 * 
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 * 
 */
public interface JRedis {
	
	// ------------------------------------------------------------------------
	// Semantic context methods
	// ------------------------------------------------------------------------

	// TODO: reach a decision on whether to include this or not.
//	/**
//	 * Provides for access to an interface providing standard Java collections
//	 * semantics on the specified parametric type.  
//	 * <p>
//	 * The type <code>T</code> can be of 3 categories:
//	 * <ol>
//	 * <li>It is 
//	 * </ol>
//	 * @param <T> a Java class type that you wish to perform {@link Set}, 
//	 * {@link List}, or {@link Map}, operations. 
//	 * @return the {@link JavaSemantics} for type <code>T</code>, if the type specified meets
//	 * the required initialization characteristics.
//	 */
//	public <T> JavaSemantics<T>  semantic (Class<T>  type) throws ClientRuntimeException;
	
	// ------------------------------------------------------------------------
	// Security and User Management
	// ------------------------------------------------------------------------

	/**
	 * Required for authorizing access to the server.  This method implements
	 * the AUTH command.  It may be used with a non-secured server without
	 * any side effect.
	 * 
	 * @param authorization key as defined in the server.
	 * @throws RedisException if the server is in secure more and the 
	 * authorization provided 
	 */
	public JRedis auth (String authorization) throws RedisException;
	
	// ------------------------------------------------------------------------
	// "Connection Handling"
	// ------------------------------------------------------------------------

	/**
	 * Ping redis
	 * @return true (unless not authorized)
	 * @throws RedisException (as of ver. 0.09) in case of unauthorized access
	 */
	public JRedis ping () throws RedisException;

	public void quit ();
	
	// ------------------------------------------------------------------------
	// "Commands operating on string values"
	// ------------------------------------------------------------------------

	public void set (String key, byte[] value) throws RedisException;
	public void set (String key, String stringValue) throws RedisException;
	public void set (String key, Number numberValue) throws RedisException;
	public <T extends Serializable> 
		   void set (String key, T object) throws RedisException;

	public boolean setnx (String key, byte[] value) throws RedisException;
	public boolean setnx (String key, String stringValue) throws RedisException;
	public boolean setnx (String key, Number numberValue) throws RedisException;
	public <T extends Serializable> 
		   boolean setnx (String key, T object) throws RedisException;

	public byte[] get (String key)  throws RedisException;

	public List<byte[]> mget(String key, String...moreKeys) throws RedisException;

	public long incr (String key) throws RedisException;

	public long incrby (String key, int delta) throws RedisException;

	public long decr (String key) throws RedisException;

	public long decrby (String key, int delta) throws RedisException;

	public boolean exists(String key) throws RedisException;

	public boolean del (String key) throws RedisException;

	public RedisType type (String key) throws RedisException;
	
	
	// ------------------------------------------------------------------------
	// "Commands operating on the key space"
	// ------------------------------------------------------------------------
	
	public List<String> keys () throws RedisException;

	public List<String> keys (String pattern) throws RedisException;
	
	public String randomkey() throws RedisException;
	
	public void rename (String oldkey, String newkey) throws RedisException;
	
	public boolean renamenx (String oldkey, String brandnewkey) throws RedisException;
	
	public long dbsize () throws RedisException;
	
	public boolean expire (String key, int ttlseconds) throws RedisException; 
	
	// ------------------------------------------------------------------------
	// Commands operating on lists
	// ------------------------------------------------------------------------

	public void rpush (String listkey, byte[] value) throws RedisException;
	public void rpush (String listkey, String stringValue) throws RedisException;
	public void rpush (String listkey, Number numberValue) throws RedisException;
	public <T extends Serializable> 
		   void rpush (String listkey, T object) throws RedisException;
	
	public void lpush (String listkey, byte[] value) throws RedisException;
	public void lpush (String listkey, String stringValue) throws RedisException;
	public void lpush (String listkey, Number numberValue) throws RedisException;
	public <T extends Serializable> 
		   void lpush (String listkey, T object) throws RedisException;
	
	public void lset (String key, long index, byte[] value) throws RedisException;
	public void lset (String key, long index, String stringValue) throws RedisException;
	public void lset (String key, long index, Number numberValue) throws RedisException;
	public <T extends Serializable> 
		   void lset (String key, long index, T object) throws RedisException;
	

	public long lrem (String listKey, byte[] value,       int count) throws RedisException;
	public long lrem (String listKey, String stringValue, int count) throws RedisException;
	public long lrem (String listKey, Number numberValue, int count) throws RedisException;
	public <T extends Serializable> 
		   long lrem (String listKey, T object, int count) throws RedisException;
	
	public long llen (String listkey) throws RedisException;
	
	public List<byte[]> lrange (String listkey, long from, long to) throws RedisException; 

	public void ltrim (String listkey, long keepFrom, long keepTo) throws RedisException;
	
	public byte[] lindex (String listkey, long index) throws RedisException;
	
	public byte[] lpop (String listKey) throws RedisException;
	
	public byte[] rpop (String listKey) throws RedisException;

	// ------------------------------------------------------------------------
	// Commands operating on sets
	// ------------------------------------------------------------------------
	
	public boolean sadd (String setkey, byte[] member) throws RedisException;
	public boolean sadd (String setkey, String stringValue) throws RedisException;
	public boolean sadd (String setkey, Number numberValue) throws RedisException;
	public <T extends Serializable> 
		   boolean sadd (String setkey, T object) throws RedisException;

	public boolean srem (String setKey, byte[] member) throws RedisException;
	public boolean srem (String setKey, String stringValue) throws RedisException;
	public boolean srem (String setKey, Number numberValue) throws RedisException;
	public <T extends Serializable> 
		   boolean srem (String setKey, T object) throws RedisException;

	public boolean sismember (String setKey, byte[] member) throws RedisException;
	public boolean sismember (String setKey, String stringValue) throws RedisException;
	public boolean sismember (String setKey, Number numberValue) throws RedisException;
	public <T extends Serializable> 
		   boolean sismember (String setKey, T object) throws RedisException;
	
	public long scard (String setKey) throws RedisException;	
	
	public List<byte[]> sinter (String set1, String...sets) throws RedisException;

	public void sinterstore (String destSetKey, String...sets) throws RedisException;

	public List<byte[]> sunion (String set1, String...sets) throws RedisException;

	public void sunionstore (String destSetKey, String...sets) throws RedisException;

	public List<byte[]> smembers (String setkey) throws RedisException;
	
	// ------------------------------------------------------------------------
	// Multiple databases handling commands
	// ------------------------------------------------------------------------
	
	public JRedis select (int index) throws RedisException;

	public JRedis flushdb () throws RedisException;

	public JRedis flushall () throws RedisException;

	public boolean move (String key, int dbIndex) throws RedisException;
	
	// ------------------------------------------------------------------------
	// Sorting
	// ------------------------------------------------------------------------
	
	/**
	 * Usage:
	 * <p>Usage:
	 * <p><code><pre>
	 * List<byte[]>  results = redis.sort("my-list-or-set-key").BY("weight*").LIMIT(1, 11).GET("object*").DESC().ALPHA().exec();
	 * for(byte[] item : results) {
	 *     // do something with item ..
	 *  }
	 * </pre></code>
	 * <p>Sort specification elements are all options.  You could simply say:
	 * <p><code><pre>
	 * List<byte[]>  results = redis.sort("my-list-or-set-key").exec();
	 * for(byte[] item : results) {
	 *     // do something with item ..
	 *  }
	 * </pre></code>
	 * <p>Sort specification elements are also can appear in any order -- the client implementation will send them to the server
	 * in the order expected by the protocol, although it is good form to specify the predicates in natural order:
	 * <p><code><pre>
	 * List<byte[]>  results = redis.sort("my-list-or-set-key").GET("object*").DESC().ALPHA().BY("weight*").LIMIT(1, 11).exec();
	 * for(byte[] item : results) {
	 *     // do something with item ..
	 *  }
	 * </pre></code>
	 */
	public Sort sort(String key);
	
	// ------------------------------------------------------------------------
	// Persistence control commands
	// ------------------------------------------------------------------------

	public void save() throws RedisException;

	public void bgsave () throws RedisException;

	public long lastsave () throws RedisException;

	public void shutdown () throws RedisException;

// ------------------------------------------------------------------------
// Remote server control commands
// ------------------------------------------------------------------------

	public Map<String, String>	info ()  throws RedisException;
}
