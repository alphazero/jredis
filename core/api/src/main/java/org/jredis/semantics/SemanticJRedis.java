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

package org.jredis.semantics;

import java.util.List;
import java.util.Map;

import org.jredis.JRedis;
import org.jredis.RedisException;
import org.jredis.RedisType;

/**
 * This interface is certainly optional, and as of now simply an idea
 * that needed to be put in place for review and feedback.
 * 
 * [TODO: think about me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 14, 2009
 * @since   alpha.0
 * 
 */

public interface SemanticJRedis <T> /* extends JRedis */
extends CodecManager {

	// ------------------------------------------------------------------------
	// Semantic context methods
	// ------------------------------------------------------------------------


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

	public void set (String key, T value) throws RedisException;

	public boolean setnx (String key, T value) throws RedisException;

	public T get (String key)  throws RedisException;

	public List<T> mget(String key, String...moreKeys) throws RedisException;

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
	public String rename (String oldkey, String newkey) throws RedisException;
	public boolean renamenx (String oldkey, String brandnewkey) throws RedisException;
	public long dbsize () throws RedisException;
	public boolean expire (String key, int ttlseconds) throws RedisException; 
	// ------------------------------------------------------------------------
	// Commands operating on lists
	// ------------------------------------------------------------------------

	public void rpush (String listkey, T value) throws RedisException;
	public void lpush (String listkey, T value) throws RedisException;
	public void lset (String key, int index, T value) throws RedisException;
	public long lrem (String listKey, T value,       int count) throws RedisException;
	public long llen (String listkey) throws RedisException;
	public List<T> lrange (String listkey, int from, int to) throws RedisException; 
	public void ltrim (String listkey, int keepFrom, int keepTo) throws RedisException;
	public T lindex (String listkey, int index) throws RedisException;
	public T lpop (String listKey) throws RedisException;
	public T rpop (String listKey) throws RedisException;

	// ------------------------------------------------------------------------
	// Commands operating on sets
	// ------------------------------------------------------------------------

	public boolean sadd (String setkey, T member) throws RedisException;
	public boolean srem (String setKey, T member) throws RedisException;
	public boolean sismember (String setKey, T member) throws RedisException;
	public long scard (String setKey) throws RedisException;	
	public List<T> sinter (String set1, String...sets) throws RedisException;
	public void sinterstore (String destSetKey, String...sets) throws RedisException;
	public List<T> smembers (String setkey) throws RedisException;

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

	public SemanticSort<T> sort(String key);

	// ------------------------------------------------------------------------
	// Persistence control commands
	// ------------------------------------------------------------------------

	public void save() throws RedisException;
	public void bgsave () throws RedisException;
	public long lastsave () throws RedisException;
	public void shutdown () throws RedisException;

	//------------------------------------------------------------------------
	//Remote server control commands
	//------------------------------------------------------------------------

	public Map<String, String>	info ()  throws RedisException;
}