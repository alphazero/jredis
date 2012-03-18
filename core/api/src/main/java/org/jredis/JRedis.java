/*
 *   Copyright 2009-2011 Joubin Houshyar
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
 * <p>This is effectively a one to one mapping to Redis commands.  And that
 * is basically it.
 * <p>Beyond that , just be aware that an implementation may throw {@link ClientRuntimeException}
 * or an extension to report problems (typically connectivity) or features {@link NotSupportedException}
 * or bugs.  These are {@link RuntimeException}.
 * 
 * @author  joubin (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 * 
 */
@Redis(versions="1.07")
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
//	public <K extends Object, T> JavaSemantics<T>  semantic (Class<T>  type) throws ClientRuntimeException;
	
	// ------------------------------------------------------------------------
	// Security and User Management
	// NOTE: Moved to ConnectionSpec
	// ------------------------------------------------------------------------

	
	// ------------------------------------------------------------------------
	// "Connection Handling"
	// ------------------------------------------------------------------------

	/**
	 * Ping redis
	 * @return true (unless not authorized)
	 * @throws RedisException (as of ver. 0.09) in case of unauthorized access
	 */
	public <K extends Object> JRedis ping () throws RedisException;

	/**
	 * Disconnects the client.
	 * @Redis QUIT
	 */
	public <K extends Object> void quit ();
	
	// ------------------------------------------------------------------------
	// "Commands operating on string values"
	// ------------------------------------------------------------------------

	/**
	 * Bind the value to key.  
	 * @Redis SET
	 * @param key any UTF-8 {@link String}
	 * @param value any bytes.  For current data size limitations, refer to
	 * Redis documentation.
	 * @throws RedisException on user error.
	 * @throws ProviderException on un-documented features/bug
	 * @throws ClientRuntimeException on errors due to operating environment (Redis or network)
	 */
	public <K extends Object> void set (K key, byte[] value) throws RedisException;
	/**
	 * Convenient method for {@link String} data binding
	 * @Redis SET
	 * @param key
	 * @param stringValue
	 * @throws RedisException
	 * @see {@link JRedis#set(String, byte[])}
	 */
	public <K extends Object> void set (K key, String stringValue) throws RedisException;
	/**
	 * Convenient method for {@link String} numeric values binding
	 * @Redis SET
	 * @param key
	 * @param numberValue
	 * @throws RedisException
	 * @see {@link JRedis#set(String, byte[])}
	 */
	public <K extends Object> void set (K key, Number numberValue) throws RedisException;
	/**
	 * Binds the given java {@link Object} to the key.  Serialization format is
	 * implementation specific.  Simple implementations may apply the basic {@link Serializable}
	 * protocol.
	 * @Redis SET
	 * @param <T>
	 * @param key
	 * @param object
	 * @throws RedisException
	 * @see {@link JRedis#set(String, byte[])}
	 */
	public <K extends Object, T extends Serializable> 
		   void set (K key, T object) throws RedisException;

	/**
	 * @Redis SETNX
	 * @param key
	 * @param value
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> boolean setnx (K key, byte[] value) throws RedisException;
	public <K extends Object> boolean setnx (K key, String stringValue) throws RedisException;
	public <K extends Object> boolean setnx (K key, Number numberValue) throws RedisException;
	public <K extends Object, T extends Serializable> 
		   boolean setnx (K key, T object) throws RedisException;

	/**
	 * @Redis GET
	 * @param key
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> byte[] get (K key)  throws RedisException;

	public <K extends Object> byte[] getset (K key, byte[] value) throws RedisException;
	public <K extends Object> byte[] getset (K key, String stringValue) throws RedisException;
	public <K extends Object> byte[] getset (K key, Number numberValue) throws RedisException;
	public <K extends Object, T extends Serializable> 
		byte[] getset (K key, T object) throws RedisException;

	
	/**
	 * @Redis MGET
	 * @param key
	 * @param moreKeys
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> List<byte[]> mget(K...keys) throws RedisException;

	/**
	 * @Redis MSET
	 * @param keyValueMap a {@link Map}ping of {@link String} key names to byte[] values.
	 * @return 
	 * @throws RedisException
	 */
	public <K extends Object> void mset(Map<K, byte[]> keyValueMap) throws RedisException;
	
	public <K extends Object> void mset(KeyValueSet.ByteArrays<K> mappings) throws RedisException;
	public <K extends Object> void mset(KeyValueSet.Strings<K> mappings) throws RedisException;
	public <K extends Object> void mset(KeyValueSet.Numbers<K> mappings) throws RedisException;
	public <K extends Object, T extends Serializable> void mset(KeyValueSet.Objects<K, T> mappings) throws RedisException;
	
	/**
	 * @Redis MSETNX
	 * @param keyValueMap a {@link Map}ping of {@link String} key names to byte[] values.
	 * @return false if ANY of the keys in the map already existed, true if all were new and were set.
	 * @throws RedisException
	 */
	public <K extends Object> boolean msetnx(Map<K, byte[]> keyValueMap) throws RedisException;
	
	public <K extends Object> boolean msetnx(KeyValueSet.ByteArrays<K> mappings) throws RedisException;
	public <K extends Object> boolean msetnx(KeyValueSet.Strings<K> mappings) throws RedisException;
	public <K extends Object> boolean msetnx(KeyValueSet.Numbers<K> mappings) throws RedisException;
	public <K extends Object, T extends Serializable> boolean msetnx(KeyValueSet.Objects<K, T> mappings) throws RedisException;
	
	/**
	 * @Redis INCR
	 * @param key
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> long incr (K key) throws RedisException;

	/**
	 * @Redis INCRBY
	 * @param key
	 * @param delta
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> long incrby (K key, int delta) throws RedisException;

	/**
	 * @Redis DECR
	 * @param key
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> long decr (K key) throws RedisException;

	/**
	 * @Redis DECRBY
	 * @param key
	 * @param delta
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> long decrby (K key, int delta) throws RedisException;

	/**
	 * @Redis SUBSTR
	 * @param key
	 * @param from
	 * @param to
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> byte[] substr (K key, long from, long to) throws RedisException;
	
	
	/**
	 * @Redis APPEND
	 * @param key
	 * @param value
	 * @return length (byte count) of appended value
	 * @throws RedisException
	 */
	public <K extends Object> long append (K key, byte[] value) throws RedisException;
	public <K extends Object> long append (K key, String stringValue) throws RedisException;
	public <K extends Object> long append (K key, Number numberValue) throws RedisException;
	public <K extends Object, T extends Serializable> 
		   long append (K key, T object) throws RedisException;

	// ------------------------------------------------------------------------
	// "Commands operating on the key space"
	// ------------------------------------------------------------------------
	
	/**
	 * @Redis EXISTS
	 * @param key
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> boolean exists(K key) throws RedisException;

	/**
	 * @Redis DEL
	 * @param keys one or more non-null, non-zero-length, keys to be deleted
	 * @return number of keys actually deleted
	 * @throws RedisException
	 */
//	public <K extends Object> boolean del (K key) throws RedisException;
	public <K extends Object> long del (K ... keys) throws RedisException;

	/**
	 * @Redis TYPE
	 * @param key
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> RedisType type (K key) throws RedisException;
	
	
	/**
	 * @Redis KEYS
	 * @param pattern
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> List<byte[]> keys (K pattern) throws RedisException;
	
	/**
	 * Convenience method.  Equivalent to calling <code>jredis.keys("*");</code>
	 * @Redis KEYS
	 * @return
	 * @throws RedisException
	 * @see {@link JRedis#keys(String)}
	 */
	public <K extends Object> List<byte[]> keys () throws RedisException;

	/**
	 * @Redis RANDOMKEY
	 * @return
	 * @throws RedisException
	 */
	public byte[] randomkey() throws RedisException;
	
	/**
	 * @Redis RENAME
	 * @param oldkey
	 * @param newkey
	 * @throws RedisException
	 */
	public <K extends Object> void rename (K oldkey, K newkey) throws RedisException;
	
	/**
	 * @Redis RENAMENX
	 * @param oldkey
	 * @param brandnewkey
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> boolean renamenx (K oldkey, K brandnewkey) throws RedisException;
	
	/**
	 * @Redis DBSIZE
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> long dbsize () throws RedisException;
	
	/**
	 * @Redis EXPIRE
	 * @param key
	 * @param ttlseconds
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> boolean expire (K key, int ttlseconds) throws RedisException; 
	
	/**
	 * 
	 * @Redis EXPIREAT
	 * @param key
	 * @param UNIX epoch-time in <b>milliseconds</b>.  Note that Redis expects epochtime
	 * in seconds. Implementations are responsible for converting to seconds.
	 * method   
	 * @return
	 * @throws RedisException
	 * @see {@link System#currentTimeMillis()}
	 */
	public <K extends Object> boolean expireat (K key, long epochtimeMillisecs) throws RedisException; 
	
	/**
	 * @Redis TTL
	 * @param key
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> long ttl (K key) throws RedisException;
	
	// ------------------------------------------------------------------------
	// Commands operating on lists
	// ------------------------------------------------------------------------

	/**
	 * @Redis RPUSH
	 * @param listkey
	 * @param value
	 * @throws RedisException
	 */
	public <K extends Object> void rpush (K listkey, byte[] value) throws RedisException;
	public <K extends Object> void rpush (K listkey, String stringValue) throws RedisException;
	public <K extends Object> void rpush (K listkey, Number numberValue) throws RedisException;
	public <K extends Object, T extends Serializable> 
		   void rpush (K listkey, T object) throws RedisException;
	
	/**
	 * @Redis LPUSH
	 * @param listkey
	 * @param value
	 * @throws RedisException
	 */
	public <K extends Object> void lpush (K listkey, byte[] value) throws RedisException;
	public <K extends Object> void lpush (K listkey, String stringValue) throws RedisException;
	public <K extends Object> void lpush (K listkey, Number numberValue) throws RedisException;
	public <K extends Object, T extends Serializable> 
		   void lpush (K listkey, T object) throws RedisException;
	
	/**
	 * @Redis LSET
	 * @param key
	 * @param index
	 * @param value
	 * @throws RedisException
	 */
	public <K extends Object> void lset (K key, long index, byte[] value) throws RedisException;
	public <K extends Object> void lset (K key, long index, String stringValue) throws RedisException;
	public <K extends Object> void lset (K key, long index, Number numberValue) throws RedisException;
	public <K extends Object, T extends Serializable> 
		   void lset (K key, long index, T object) throws RedisException;
	

	/**
	 * @Redis LREM
	 * @param listKey
	 * @param value
	 * @param count
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> long lrem (K listkey, byte[] value,       int count) throws RedisException;
	public <K extends Object> long lrem (K listkey, String stringValue, int count) throws RedisException;
	public <K extends Object> long lrem (K listkey, Number numberValue, int count) throws RedisException;
	public <K extends Object, T extends Serializable> 
		   long lrem (K listkey, T object, int count) throws RedisException;
	
	/**
	 * Given a 'list' key, returns the number of items in the list.
	 * @Redis LLEN
	 * @param listkey
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> long llen (K listkey) throws RedisException;
	
	/**
	 * @Redis LRANGE
	 * @param listkey
	 * @param from
	 * @param to
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> List<byte[]> lrange (K listkey, long from, long to) throws RedisException; 

	/**
	 * @Redis LTRIM
	 * @param listkey
	 * @param keepFrom
	 * @param keepTo
	 * @throws RedisException
	 */
	public <K extends Object> void ltrim (K listkey, long keepFrom, long keepTo) throws RedisException;
	
	/**
	 * @Redis LINDEX
	 * @param listkey
	 * @param index
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> byte[] lindex (K listkey, long index) throws RedisException;
	
	/**
	 * @Redis LPOP
	 * @param listKey
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> byte[] lpop (K listkey) throws RedisException;
	
	/**
	 * @Redis RPOP
	 * @param listKey
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> byte[] rpop (K listkey) throws RedisException;

	/**
	 * @Redis RPOPLPUSH
	 * @param srcList
	 * @param destList
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> byte[] rpoplpush (K srcList, K destList) throws RedisException;

	// ------------------------------------------------------------------------
	// Commands operating on sets
	// ------------------------------------------------------------------------
	
	/**
	 * @Redis SADD
	 * @param setkey
	 * @param member
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> boolean sadd (K setkey, byte[] member) throws RedisException;
	public <K extends Object> boolean sadd (K setkey, String stringValue) throws RedisException;
	public <K extends Object> boolean sadd (K setkey, Number numberValue) throws RedisException;
	public <K extends Object, T extends Serializable> 
		   boolean sadd (K setkey, T object) throws RedisException;

	/**
	 * @Redis SREM
	 * @param setKey
	 * @param member
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> boolean srem (K setkey, byte[] member) throws RedisException;
	public <K extends Object> boolean srem (K setkey, String stringValue) throws RedisException;
	public <K extends Object> boolean srem (K setkey, Number numberValue) throws RedisException;
	public <K extends Object, T extends Serializable> 
		   boolean srem (K setkey, T object) throws RedisException;

	/**
	 * @Redis SISMEMBER
	 * @param setKey
	 * @param member
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> boolean sismember (K setkey, byte[] member) throws RedisException;
	public <K extends Object> boolean sismember (K setkey, String stringValue) throws RedisException;
	public <K extends Object> boolean sismember (K setkey, Number numberValue) throws RedisException;
	public <K extends Object, T extends Serializable> 
		   boolean sismember (K setkey, T object) throws RedisException;
	
	/**
	 * @Redis SMOVE
	 * @param srcKey
	 * @param destKey
	 * @param member
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> boolean smove (K srcKey, K destKey, byte[] member) throws RedisException;
	public <K extends Object> boolean smove (K srcKey, K destKey, String stringValue) throws RedisException;
	public <K extends Object> boolean smove (K srcKey, K destKey, Number numberValue) throws RedisException;
	public <K extends Object, T extends Serializable> 
		   boolean smove (K srcKey, K destKey, T object) throws RedisException;
	
	/**
	 * @Redis SCARD
	 * @param setKey
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> long scard (K setKey) throws RedisException;	
	
	/**
	 * @Redis SINTER
	 * @param set1
	 * @param sets
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> List<byte[]> sinter (K set1, K...sets) throws RedisException;
	/**
	 * @Redis SINTERSTORE
	 * @param destSetKey
	 * @param sets
	 * @throws RedisException
	 */
	public <K extends Object> void sinterstore (K destSetKey, K...sets) throws RedisException;

	/**
	 * @Redis SUNION
	 * @param set1
	 * @param sets
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> List<byte[]> sunion (K set1, K...sets) throws RedisException;
	
	/**
	 * @Redis SUNIONSTORE
	 * @param destSetKey
	 * @param sets
	 * @throws RedisException
	 */
	public <K extends Object> void sunionstore (K destSetKey, K...sets) throws RedisException;

	/**
	 * @Redis SDIFF
	 * @param set1
	 * @param sets
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> List<byte[]> sdiff (K set1, K...sets) throws RedisException;
	
	/**
	 * @Redis SDIFFSTORE
	 * @param destSetKey
	 * @param sets
	 * @throws RedisException
	 */
	public <K extends Object> void sdiffstore (K destSetKey, K...sets) throws RedisException;

	/**
	 * @Redis SMEMBERS
	 * @param setkey
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> List<byte[]> smembers (K setKey) throws RedisException;
	
	/**
	 * @Redis SRANDMEMBER
	 * @param setkey
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> byte[] srandmember (K setKey) throws RedisException;
	
	/**
	 * @Redis SPOP
	 * @param setkey
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> byte[] spop (K setKey) throws RedisException;
	// ------------------------------------------------------------------------
	// Commands operating on sets
	// ------------------------------------------------------------------------
	
	/**
	 * @Redis ZADD
	 * @param setkey
	 * @param score
	 * @param member
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> boolean zadd (K setkey, double score, byte[] member) throws RedisException;
	public <K extends Object> boolean zadd (K setkey, double score, String stringValue) throws RedisException;
	public <K extends Object> boolean zadd (K setkey, double score, Number numberValue) throws RedisException;
	public <K extends Object, T extends Serializable> 
		   boolean zadd (K setkey, double score, T object) throws RedisException;

	/**
	 * @Redis ZREM
	 * @param setKey
	 * @param member
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> boolean zrem (K setkey, byte[] member) throws RedisException;
	public <K extends Object> boolean zrem (K setkey, String stringValue) throws RedisException;
	public <K extends Object> boolean zrem (K setkey, Number numberValue) throws RedisException;
	public <K extends Object, T extends Serializable> 
		   boolean zrem (K setkey, T object) throws RedisException;
	
	/**
	 * @Redis ZCARD
	 * @param setKey
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> long zcard (K setKey) throws RedisException;	
	
	
	/**
	 * @Redis ZSCORE
	 * @param setkey
	 * @param member
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> Double zscore (K setkey, byte[] member) throws RedisException;
	public <K extends Object> Double zscore (K setkey, String stringValue) throws RedisException;
	public <K extends Object> Double zscore (K setkey, Number numberValue) throws RedisException;
	public <K extends Object, T extends Serializable> 
		Double zscore (K setkey, T object) throws RedisException;

	/**
	 * @Redis ZRANK
	 * @param setkey
	 * @param member
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> long zrank (K setkey, byte[] member) throws RedisException;
	public <K extends Object> long zrank (K setkey, String stringValue) throws RedisException;
	public <K extends Object> long zrank (K setkey, Number numberValue) throws RedisException;
	public <K extends Object, T extends Serializable> 
		long zrank (K setkey, T object) throws RedisException;

	/**
	 * @Redis ZREVRANK
	 * @param setkey
	 * @param member
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> long zrevrank (K setkey, byte[] member) throws RedisException;
	public <K extends Object> long zrevrank (K setkey, String stringValue) throws RedisException;
	public <K extends Object> long zrevrank (K setkey, Number numberValue) throws RedisException;
	public <K extends Object, T extends Serializable> 
		long zrevrank (K setkey, T object) throws RedisException;

	/**
	 * @Redis ZRANGE
	 * @param setkey
	 * @param from
	 * @param to
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> List<byte[]> zrange (K setkey, long from, long to) throws RedisException; 

	/**
	 * @Redis ZREVRANGE
	 * @param setkey
	 * @param from
	 * @param to
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> List<byte[]> zrevrange (K setkey, long from, long to) throws RedisException; 

	/**
	 * Equivalent to {@link JRedis#zrange(String, long, long)} with the {@link Option.Options#WITHSCORES}.
	 * Unlike the general ZRANGE command that only returns the values, this method returns both
	 * values and associated scores for the specified range.
	 *  
	 * @Redis ZRANGE ... WITHSCORES
	 * @param setkey
	 * @param from
	 * @param to
	 * @return the subset of the specified set 
	 * @throws RedisException
	 * @see JRedis#zrange(String, long, long)
	 * @see ZSetEntry
	 */
	public <K extends Object> List<ZSetEntry> zrangeSubset (K setkey, long from, long to) throws RedisException; 

	/**
	 * Equivalent to {@link JRedis#zrevrange(String, long, long)} with the {@link Option.Options#WITHSCORES}.
	 * Unlike the general ZREVRANGE command that only returns the values, this method returns both
	 * values and associated scores for the specified range.
	 *  
	 * @Redis ZREVRANGE ... WITHSCORES
	 * @param setkey
	 * @param from
	 * @param to
	 * @return the subset of the specified set 
	 * @throws RedisException
	 * @see JRedis#zrevrange(String, long, long)
	 * @see ZSetEntry
	 */
	public <K extends Object> List<ZSetEntry> zrevrangeSubset (K setkey, long from, long to) throws RedisException; 

	/**
	 * @Redis ZRANGE
	 * @param setkey
	 * @param minScore
	 * @param maxScore
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> List<byte[]> zrangebyscore (K setkey, double minScore, double maxScore) throws RedisException; 

	/**
	 * @Redis ZRANGEBYSCORE ... WITHSCORES
	 * @param setkey
	 * @param minScore
	 * @param maxScore
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> List<ZSetEntry> zrangebyscoreSubset (K setkey, double minScore, double maxScore) throws RedisException;

	/**
	 * @Redis ZREMRANGEBYSCORE
	 * @param setkey
	 * @param minScore
	 * @param maxScore
	 * @return number of removed elements
	 * @throws RedisException
	 */
	public <K extends Object> long zremrangebyscore (K setkey, double minScore, double maxScore) throws RedisException; 

	/**
	 * @Redis ZREMRANGEBYRANK
	 * @param setkey
	 * @param minRank
	 * @param maxRank
	 * @return number of removed elements
	 * @throws RedisException
	 */
	public <K extends Object> long zremrangebyrank (K setkey, long minRank, long maxRank) throws RedisException; 

	/**
	 * @Redis ZINCRBY
	 * @param setkey
	 * @param score
	 * @param member
	 * @return
	 * @throws RedisException
	 */
	@Redis(versions="1.07")
	public <K extends Object> Double zincrby (K setkey, double score, byte[] member) throws RedisException;
	public <K extends Object> Double zincrby (K setkey, double score, String stringValue) throws RedisException;
	public <K extends Object> Double zincrby (K setkey, double score, Number numberValue) throws RedisException;
	public <K extends Object, T extends Serializable> 
		Double zincrby (K setkey, double score, T object) throws RedisException;

	/**
	 * @Redis ZCOUNT
	 * @param setkey
	 * @param minScore
	 * @param maxScore
	 * @return count of set members with score in the given range.
	 * @throws RedisException
	 */
	public <K extends Object> long zcount (K setkey, double minScore, double maxScore) throws RedisException; 
	// ------------------------------------------------------------------------
	// Commands operating on hashes
	// ------------------------------------------------------------------------
	
	/**
	 * @Redis HSET
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 */
	@Redis(versions="1.3.n")
	public <K extends Object> boolean hset(K key, K field, byte[] value)  throws RedisException;
	
	/**
	 * @Redis HSET
	 * @param key
	 * @param field
	 * @param string
	 * @return
	 */
	@Redis(versions="1.3.n")
	public <K extends Object> boolean hset(K key, K field, String string)  throws RedisException;
	
	/**
	 * @Redis HSET
	 * @param key
	 * @param field
	 * @param number
	 * @return
	 */
	@Redis(versions="1.3.n")
	public <K extends Object> boolean hset(K key, K field, Number number)  throws RedisException;
	
	/**
	 * @Redis HSET
	 * @param <T>
	 * @param key
	 * @param field
	 * @param object
	 * @return
	 */
	@Redis(versions="1.3.4")
	public <K extends Object, T extends Serializable> 
		boolean hset(K key, K field, T object)  throws RedisException;
	
	/**
	 * @Redis HGET
	 * @param key
	 * @param field
	 * @return
	 */
	@Redis(versions="1.3.4")
	public <K extends Object> byte[] hget(K key, K field)  throws RedisException;
	
	
	/**
	 * 
	 * @Redis HEXISTS
	 * @param key
	 * @param field
	 * @return true if the spec'd field exists for the spec'd (hash type) key
	 * @throws RedisException
	 */
	@Redis(versions="1.3.n")
	public <K extends Object> boolean hexists(K key, K field)  throws RedisException;
	
	/**
	 * 
	 * @Redis HDEL
	 * @param key
	 * @param field
	 * @return true if the spec'd field exists for the spec'd (hash type) key
	 * @throws RedisException
	 */
	@Redis(versions="1.3.n")
	public <K extends Object> boolean hdel(K key, K field)  throws RedisException;
	
	/**
	 * 
	 * @Redis HLEN
	 * @param key
	 * @return # of fields/entries in the given hashtable.
	 * @throws RedisException
	 */
	@Redis(versions="1.3.n")
	public <K extends Object> long hlen(K key)  throws RedisException;
	
	/**
	 * 
	 * @Redis HKEYS
	 * @param key
	 * @return list of keys in the given hashtable.
	 * @throws RedisException
	 */
	@Redis(versions="1.3.n")
	public <K extends Object> List<byte[]> hkeys(K key)  throws RedisException;
	
	/**
	 * 
	 * @Redis HVALS
	 * @param key
	 * @return list of values in the given hashtable.
	 * @throws RedisException
	 */
	@Redis(versions="1.3.n")
	public <K extends Object> List<byte[]> hvals(K key)  throws RedisException;
	
	/**
	 * 
	 * @Redis HGETALL
	 * @param key
	 * @return the given hash as a Map<String, byte[]>
	 * @throws RedisException
	 */
	@Redis(versions="1.3.n")
	public <K extends Object> Map<byte[], byte[]> hgetall(K key)  throws RedisException;
	
	// ------------------------------------------------------------------------
	// Transactional commands
	// ------------------------------------------------------------------------
	/**
	 * one option is to return a subclass of JRedis (e.g. JRedisCommandSequence)
	 * and have that interface declare discard and multi.  Benefit is being able
	 * to associate state with the transaction.
	 * @throws RedisException
	 */
	@Redis(versions="2.0")
	public <K extends Object> JRedis multi() throws RedisException;
	/**
	 * @throws RedisException
	 */
	@Redis(versions="2.0")
	public <K extends Object> JRedis discard () throws RedisException;
	
	// ------------------------------------------------------------------------
	// Multiple databases handling commands
	// ------------------------------------------------------------------------
	
//	@Deprecated
//	public <K extends Object> JRedis select (int index) throws RedisException;

	/**
	 * Flushes the db you selected when connecting to Redis server.  Typically,
	 * implementations will select db 0 on connecting if non was specified.  Remember
	 * that there is no roll-back.
	 * @Redis FLUSHDB
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> JRedis flushdb () throws RedisException;

	/**
	 * Flushes all dbs in the connect Redis server, regardless of which db was selected
	 * on connect time.  Remember that there is no rollback.
	 * @Redis FLUSHALL
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> JRedis flushall () throws RedisException;

	/**
	 * Moves the given key from the currently selected db to the one indicated
	 * by <code>dbIndex</code>.
	 * @Redis MOVE
	 * @param key
	 * @param dbIndex
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> boolean move (K key, int dbIndex) throws RedisException;
	
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
	 * 
	 * @Redis SORT
	 */
	public <K extends Object> Sort sort(K key);
	
	// ------------------------------------------------------------------------
	// Persistence control commands
	// ------------------------------------------------------------------------

	/**
	 * @Redis SAVE
	 * @throws RedisException
	 */
	public <K extends Object> void save() throws RedisException;

	/**
	 * @Redis BGSAVE
	 * @throws RedisException
	 */
	public <K extends Object> void bgsave () throws RedisException;

	/**
	 * @Redis BGREWRITEAOF
	 * @return ack message.  
	 * @throws RedisException
	 */
	public <K extends Object> String bgrewriteaof () throws RedisException;

	/**
	 * @Redis LASTSAVE
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> long lastsave () throws RedisException;

//	@Deprecated
//	public <K extends Object> void shutdown () throws RedisException;

// ------------------------------------------------------------------------
// Remote server control commands
// ------------------------------------------------------------------------

	/**
	 * @Redis INFO
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> Map<String, String>	info ()  throws RedisException;
	
	/**
	 * @Redis SLAVEOF
	 * @param host ip address 
	 * @param port
	 */
	public <K extends Object> void slaveof(String host, int port) throws RedisException;
	
	/**
	 * Convenience method.  Turns off replication.
	 * @Redis SLAVEOF "no one"
	 */
	public <K extends Object> void slaveofnone() throws RedisException;
	
	// ------------------------------------------------------------------------
	// Diagnostics commands
	// ------------------------------------------------------------------------
	
	/**
	 * @Redis ECHO
	 * @param msg
	 * @return
	 * @throws RedisException
	 */
	public <K extends Object> byte[] echo (byte[] msg) throws RedisException;
	public <K extends Object> byte[] echo (String msg) throws RedisException;
	public <K extends Object> byte[] echo (Number msg) throws RedisException;
	public <K extends Object, T extends Serializable> 
		byte[] echo (T msg) throws RedisException;
	
	/**
	 * @Redis DEBUG OBJECT <key>
	 * @param key
	 * @return
	 * @throws RedisException
	 * @see {@link ObjectInfo}
	 */
	public <K extends Object> ObjectInfo debug (K key) throws RedisException;
	
	public <K extends Object> boolean setbit(K key, int offset, boolean value) throws RedisException;

	public <K extends Object> boolean getbit(K key, int offset) throws RedisException;
	
}
