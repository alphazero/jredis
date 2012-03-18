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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.jredis.protocol.ResponseStatus;


/**
 * The asynchronous interface to Redis.
 * <p>
 * This is effectively a one to one mapping to Redis commands.  Depending on the implementation
 * either the redis response and/or redis write are asynchronous.  Regardless, each method returns
 * an extension of {@link Future} and the returned results conforms to the contract of that interface (which
 * you should review).
 * <p>
 * If your request results in a {@link RedisException}, the call to {@link Future#get()} (of either flavor)
 * will raise a {@link ExecutionException} with {@link ExecutionException#getCause()} returning the underlying
 * {@link RedisException}.
 * <p>
 * Similarly, if the request results in either {@link ClientRuntimeException} or {@link ProviderException}, the
 * {@link Future}'s {@link ExecutionException} will wrap these as the cause.
 * <p>
 * Beyond that , just be aware that an implementation may throw {@link ClientRuntimeException}
 * or an extension to report problems (typically connectivity) or {@link ProviderException}
 * (to highlight implementation features/bugs).  
 * These are {@link RuntimeException} that have been encountered while trying to queue your request.
 * <p>
 * <b>Note</b> that this interface provides no guarantees whatsoever regarding the execution of your requests beyond
 * the strict ordering of the requests per your invocations.  Specifically, in the event of connection issues, this
 * interface's contract does not place any requirements on the implementation beyond to notify the user of such issues
 * either during a call to this interface, or, on the attempt to get the result of a pending response on {@link Future#get()}.
 * Refer to the documentation of the implementation of {@link JRedisFuture} for the specifics of behavior in context of
 * errors. 
 * 
 * @author  Joubin (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 * 
 */
@Redis(versions="1.07")
public interface JRedisFuture {
	
	// ------------------------------------------------------------------------
	// "Connection Handling"
	// ------------------------------------------------------------------------

	/**
	 * Ping redis
	 */
	public <K extends Object> Future<ResponseStatus> ping ();

	/**
	 * Disconnects the client.
	 * @Redis QUIT
	 */
	public <K extends Object> Future<ResponseStatus> quit ();
	
	/**
	 * Optional connection control command. 
	 * @param <K>
	 * @return
	 */
	public Future<ResponseStatus> flush ();
	
	// ------------------------------------------------------------------------
	// "Commands operating on string values"
	// ------------------------------------------------------------------------

	/**
	 * Bind the value to key.  
	 * @Redis SET
	 * @param key any UTF-8 {@link String}
	 * @param value any bytes.  For current data size limitations, refer to
	 * Redis documentation.
	 * @throws ProviderException on un-documented features/bug
	 * @throws ClientRuntimeException on errors due to operating environment (Redis or network)
	 */
	public <K extends Object> Future<ResponseStatus> set (K key, byte[] value);
	/**
	 * Convenient method for {@link String} data binding
	 * @Redis SET
	 * @param key
	 * @param stringValue
	 * @see {@link JRedis#set(String, byte[])}
	 */
	public <K extends Object> Future<ResponseStatus> set (K key, String stringValue);
	/**
	 * Convenient method for {@link String} numeric values binding
	 * @Redis SET
	 * @param key
	 * @param numberValue
	 * @see {@link JRedis#set(String, byte[])}
	 */
	public <K extends Object> Future<ResponseStatus> set (K key, Number numberValue);
	/**
	 * Binds the given java {@link Object} to the key.  Serialization format is
	 * implementation specific.  Simple implementations may apply the basic {@link Serializable}
	 * protocol.
	 * @Redis SET
	 * @param <T>
	 * @param key
	 * @param object
	 * @see {@link JRedis#set(String, byte[])}
	 */
	public <K extends Object, T extends Serializable> 
		   Future<ResponseStatus> set (K key, T object);

	/**
	 * @Redis SETNX
	 * @param key
	 * @param value
	 * @return
	 */
	public <K extends Object> Future<Boolean> setnx (K key, byte[] value);
	public <K extends Object> Future<Boolean> setnx (K key, String stringValue);
	public <K extends Object> Future<Boolean> setnx (K key, Number numberValue);
	public <K extends Object, T extends Serializable> 
		   Future<Boolean> setnx (K key, T object);

	public <K extends Object> Future<Boolean> setbit(K key, int offset, boolean value);


	
	/**
	 * @Redis GET
	 * @param key
	 * @return
	 */
	public <K extends Object> Future<byte[]> get (K key) ;

	public <K extends Object> Future<byte[]> getset (K key, byte[] value);
	public <K extends Object> Future<byte[]> getset (K key, String stringValue);
	public <K extends Object> Future<byte[]> getset (K key, Number numberValue);
	public <K extends Object, T extends Serializable> 
		Future<byte[]> getset (K key, T object);


	public <K extends Object> Future<Boolean> getbit(K key, int offset);

		
	/**
	 * @Redis MGET
	 * @param key
	 * @param moreKeys
	 * @return
	 */
	public <K extends Object> Future<List<byte[]>> mget(String ... keys);

	/**
	 * @Redis MSET
	 * @param keyValueMap a {@link Map}ping of {@link String} key names to byte[] values.
	 * @return Future<Boolean> indicating if all of sets were OK or not
	 * @throws RedisException
	 */
	public <K extends Object> Future<ResponseStatus> mset(Map<K, byte[]> keyValueMap);
	
	public <K extends Object> Future<ResponseStatus> mset(KeyValueSet.ByteArrays<K> mappings);
	public <K extends Object> Future<ResponseStatus> mset(KeyValueSet.Strings<K> mappings);
	public <K extends Object> Future<ResponseStatus> mset(KeyValueSet.Numbers<K> mappings);
	public <K extends Object, T extends Serializable> Future<ResponseStatus> mset(KeyValueSet.Objects<K, T> mappings);
	
	/**
	 * @Redis MSETNX
	 * @param keyValueMap a {@link Map}ping of {@link String} key names to byte[] values.
	 * @return Future<Boolean> indicating if all of sets were OK or not
	 * @throws RedisException
	 */
	public <K extends Object> Future<Boolean> msetnx(Map<K, byte[]> keyValueMap);
	
	public <K extends Object> Future<Boolean> msetnx(KeyValueSet.ByteArrays<K> mappings);
	public <K extends Object> Future<Boolean> msetnx(KeyValueSet.Strings<K> mappings);
	public <K extends Object> Future<Boolean> msetnx(KeyValueSet.Numbers<K> mappings);
	public <K extends Object, T extends Serializable> Future<Boolean> msetnx(KeyValueSet.Objects<K, T> mappings);
	
	/**
	 * @Redis INCR
	 * @param key
	 * @return
	 */
	public <K extends Object> Future<Long> incr (K key);

	/**
	 * @Redis INCRBY
	 * @param key
	 * @param delta
	 * @return
	 */
	public <K extends Object> Future<Long> incrby (K key, int delta);

	/**
	 * @Redis DECR
	 * @param key
	 * @return
	 */
	public <K extends Object> Future<Long> decr (K key);

	/**
	 * @Redis DECRBY
	 * @param key
	 * @param delta
	 * @return
	 */
	public <K extends Object> Future<Long> decrby (K key, int delta);

	/**
	 * @Redis SUBSTR
	 * @param listkey
	 * @param from
	 * @param to
	 * @return
	 */
	public <K extends Object> Future<byte[]> substr (K listkey, long from, long to); 
	
	/**
	 * @Redis APPEND
	 * @param key
	 * @param value
	 * @return the length (byte count) of appended key.
	 */
	public <K extends Object> Future<Long> append (K key, byte[] value);
	public <K extends Object> Future<Long> append (K key, String stringValue);
	public <K extends Object> Future<Long> append (K key, Number numberValue);
	public <K extends Object, T extends Serializable> 
		   Future<Long> append (K key, T object);

	/**
	 * @Redis EXISTS
	 * @param key
	 * @return
	 */
	public <K extends Object> Future<Boolean> exists(K key);

	/**
	 * @Redis DEL
	 * @param keys one or more, non-null, non-zero-length, keys to be deleted
	 * @return Future<Long> of number keys actually deleted.
	 */
	public <K extends Object> Future<Long> del (K ... keys);

	/**
	 * @Redis TYPE
	 * @param key
	 * @return
	 */
	public <K extends Object> Future<RedisType> type (K key);
	
	
	// ------------------------------------------------------------------------
	// "Commands operating on the key space"
	// ------------------------------------------------------------------------
	
	/**
	 * @Redis KEYS
	 * @param pattern
	 * @return
	 */
	public <K extends Object> Future<List<byte[]>> keys (K pattern);
	
	/**
	 * Convenience method.  Equivalent to calling <code>jredis.keys("*");</code>
	 * @Redis KEYS
	 * @return
	 * @see {@link JRedis#keys(String)}
	 */
	public <K extends Object> Future<List<byte[]>> keys ();

	/**
	 * @Redis RANDOMKEY
	 * @return
	 */
	public <K extends Object> Future<byte[]> randomkey();
	
	/**
	 * @Redis RENAME
	 * @param oldkey
	 * @param newkey
	 */
	public <K extends Object> Future<ResponseStatus> rename (K oldkey, K newkey);
	
	/**
	 * @Redis RENAMENX
	 * @param oldkey
	 * @param brandnewkey
	 * @return
	 */
	public <K extends Object> Future<Boolean> renamenx (K oldkey, K brandnewkey);
	
	/**
	 * @Redis DBSIZE
	 * @return
	 */
	public <K extends Object> Future<Long> dbsize ();
	
	/**
	 * @Redis EXPIRE
	 * @param key
	 * @param ttlseconds
	 * @return
	 */
	public <K extends Object> Future<Boolean> expire (K key, int ttlseconds); 
	
	/**
	 * @Redis EXPIREAT
	 * @param key
	 * @param UNIX epoch-time in <b>milliseconds</b>.  Note that Redis expects epochtime
	 * in seconds. Implementations are responsible for converting to seconds.
	 * method   
	 * @return
	 * @see {@link System#currentTimeMillis()}
	 */
	public <K extends Object> Future<Boolean> expireat (K key, long epochtimeMillisecs); 
	
	/**
	 * @Redis TTL
	 * @param key
	 * @return
	 */
	public <K extends Object> Future<Long> ttl (K key);
	
	// ------------------------------------------------------------------------
	// Commands operating on lists
	// ------------------------------------------------------------------------

	/**
	 * @Redis RPUSH
	 * @param listkey
	 * @param value
	 */
	public <K extends Object> Future<Long> rpush (K listkey, byte[] value);
	public <K extends Object> Future<Long> rpush (K listkey, String stringValue);
	public <K extends Object> Future<Long> rpush (K listkey, Number numberValue);
	public <K extends Object, T extends Serializable> 
		Future<Long> rpush (K listkey, T object);
	
	/**
	 * @Redis LPUSH
	 * @param listkey
	 * @param value
	 */
	public <K extends Object> Future<Long> lpush (K listkey, byte[] value);
	public <K extends Object> Future<Long> lpush (K listkey, String stringValue);
	public <K extends Object> Future<Long> lpush (K listkey, Number numberValue);
	public <K extends Object, T extends Serializable> 
		Future<Long> lpush (K listkey, T object);
	
	/**
	 * @Redis LSET
	 * @param key
	 * @param index
	 * @param value
	 */
	public <K extends Object> Future<ResponseStatus> lset (K key, long index, byte[] value);
	public <K extends Object> Future<ResponseStatus> lset (K key, long index, String stringValue);
	public <K extends Object> Future<ResponseStatus> lset (K key, long index, Number numberValue);
	public <K extends Object, T extends Serializable> 
		Future<ResponseStatus> lset (K key, long index, T object);
	

	/**
	 * @Redis LREM
	 * @param listKey
	 * @param value
	 * @param count
	 * @return
	 */
	public <K extends Object> Future<Long> lrem (K listkey, byte[] value,       int count);
	public <K extends Object> Future<Long> lrem (K listkey, String stringValue, int count);
	public <K extends Object> Future<Long> lrem (K listkey, Number numberValue, int count);
	public <K extends Object, T extends Serializable> 
		Future<Long> lrem (K listkey, T object, int count);
	
	/**
	 * Given a 'list' key, returns the number of items in the list.
	 * @Redis LLEN
	 * @param listkey
	 * @return
	 */
	public <K extends Object> Future<Long> llen (K listkey);
	
	/**
	 * @Redis LRANGE
	 * @param listkey
	 * @param from
	 * @param to
	 * @return
	 */
	public <K extends Object> Future<List<byte[]>> lrange (K listkey, long from, long to); 

	/**
	 * @Redis LTRIM
	 * @param listkey
	 * @param keepFrom
	 * @param keepTo
	 */
	public <K extends Object> Future<ResponseStatus> ltrim (K listkey, long keepFrom, long keepTo);
	
	/**
	 * @Redis LINDEX
	 * @param listkey
	 * @param index
	 * @return
	 */
	public <K extends Object> Future<byte[]> lindex (K listkey, long index);
	
	/**
	 * @Redis LPOP
	 * @param listKey
	 * @return
	 */
	public <K extends Object> Future<byte[]> lpop (K listkey);
	
	/**
	 * @Redis RPOP
	 * @param listKey
	 * @return
	 */
	public <K extends Object> Future<byte[]> rpop (K listkey);

	/**
	 * @Redis RPOPLPUSH
	 * @param srcList
	 * @param destList
	 * @return
	 */
	public <K extends Object> Future<byte[]> rpoplpush (String srcList, String destList);
	// ------------------------------------------------------------------------
	// Commands operating on sets
	// ------------------------------------------------------------------------
	
	/**
	 * @Redis SADD
	 * @param setkey
	 * @param member
	 * @return
	 */
	public <K extends Object> Future<Boolean> sadd (K setkey, byte[] member);
	public <K extends Object> Future<Boolean> sadd (K setkey, String stringValue);
	public <K extends Object> Future<Boolean> sadd (K setkey, Number numberValue);
	public <K extends Object, T extends Serializable> 
		Future<Boolean> sadd (K setkey, T object);

	/**
	 * @Redis SREM
	 * @param setKey
	 * @param member
	 * @return
	 */
	public <K extends Object> Future<Boolean> srem (K setkey, byte[] member);
	public <K extends Object> Future<Boolean> srem (K setkey, String stringValue);
	public <K extends Object> Future<Boolean> srem (K setkey, Number numberValue);
	public <K extends Object, T extends Serializable> 
		Future<Boolean> srem (K setkey, T object);

	/**
	 * @Redis SISMEMBER
	 * @param setKey
	 * @param member
	 * @return
	 */
	public <K extends Object> Future<Boolean> sismember (K setkey, byte[] member);
	public <K extends Object> Future<Boolean> sismember (K setkey, String stringValue);
	public <K extends Object> Future<Boolean> sismember (K setkey, Number numberValue);
	public <K extends Object, T extends Serializable> 
		Future<Boolean> sismember (K setkey, T object);
	
	/**
	 * @Redis SMOVE
	 * @param srcKey
	 * @param destKey
	 * @param member
	 * @return
	 */
	public <K extends Object> Future<Boolean> smove (K srcKey, K destKey, byte[] member);
	public <K extends Object> Future<Boolean> smove (K srcKey, K destKey, String stringValue);
	public <K extends Object> Future<Boolean> smove (K srcKey, K destKey, Number numberValue);
	public <K extends Object, T extends Serializable> 
		Future<Boolean> smove (K srcKey, K destKey, T object);
	
	/**
	 * @Redis SCARD
	 * @param setKey
	 * @return
	 */
	public <K extends Object> Future<Long> scard (K setKey);	
	
	/**
	 * @Redis SINTER
	 * @param set1
	 * @param sets
	 * @return
	 */
	public <K extends Object> Future<List<byte[]>> sinter (K set1, K...sets);
	/**
	 * @Redis SINTERSTORE
	 * @param destSetKey
	 * @param sets
	 */
	public <K extends Object> Future<ResponseStatus> sinterstore (K destSetKey, K...sets);

	/**
	 * @Redis SUNION
	 * @param set1
	 * @param sets
	 * @return
	 */
	public <K extends Object> Future<List<byte[]>> sunion (K set1, K...sets);
	
	/**
	 * @Redis SUNIONSTORE
	 * @param destSetKey
	 * @param sets
	 */
	public <K extends Object> Future<ResponseStatus> sunionstore (K destSetKey, K...sets);

	/**
	 * @Redis SDIFF
	 * @param set1
	 * @param sets
	 * @return
	 */
	public <K extends Object> Future<List<byte[]>> sdiff (K set1, K...sets);
	
	/**
	 * @Redis SDIFFSTORE
	 * @param destSetKey
	 * @param sets
	 */
	public <K extends Object> Future<ResponseStatus> sdiffstore (K destSetKey, K...sets);

	/**
	 * @Redis SMEMBERS
	 * @param setkey
	 * @return
	 */
	public <K extends Object> Future<List<byte[]>> smembers (K setkey);
	
	/**
	 * @Redis SRANDMEMBER
	 * @param setkey
	 * @return
	 */
	public <K extends Object> Future<byte[]> srandmember (K setkey);

	/**
	 * @Redis SPOP
	 * @param setkey
	 * @return
	 */
	public <K extends Object> Future<byte[]> spop (K setkey);
	// ------------------------------------------------------------------------
	// Commands operating on sorted sets
	// ------------------------------------------------------------------------
	
	/**
	 * @Redis ZADD
	 * @param setkey
	 * @param score
	 * @param member
	 * @return
	 */
	public <K extends Object> Future<Boolean> zadd (K setkey, double score, byte[] member);
	public <K extends Object> Future<Boolean> zadd (K setkey, double score, String stringValue);
	public <K extends Object> Future<Boolean> zadd (K setkey, double score, Number numberValue);
	public <K extends Object, T extends Serializable> 
		Future<Boolean> zadd (K setkey, double score, T object);

	/**
	 * @Redis ZREM
	 * @param setKey
	 * @param member
	 * @return
	 */
	public <K extends Object> Future<Boolean> zrem (K setkey, byte[] member);
	public <K extends Object> Future<Boolean> zrem (K setkey, String stringValue);
	public <K extends Object> Future<Boolean> zrem (K setkey, Number numberValue);
	public <K extends Object, T extends Serializable> 
		Future<Boolean> zrem (K setkey, T object);

	/**
	 * @Redis ZCARD
	 * @param setKey
	 * @return
	 */
	public <K extends Object> Future<Long> zcard (K setKey);	
	
	/**
	 * @Redis ZSCORE
	 * @param setkey
	 * @param member
	 * @return
	 */
	public <K extends Object> Future<Double> zscore (K setkey, byte[] member);
	public <K extends Object> Future<Double> zscore (K setkey, String stringValue);
	public <K extends Object> Future<Double> zscore (K setkey, Number numberValue);
	public <K extends Object, T extends Serializable> 
		Future<Double> zscore (K setkey, T object);

	/**
	 * @Redis ZRANK
	 * @param setkey
	 * @param member
	 * @return
	 */
	public <K extends Object> Future<Long> zrank (K setkey, byte[] member);
	public <K extends Object> Future<Long> zrank (K setkey, String stringValue);
	public <K extends Object> Future<Long> zrank (K setkey, Number numberValue);
	public <K extends Object, T extends Serializable> 
		Future<Long> zrank (K setkey, T object);

	/**
	 * @Redis ZREVRANK
	 * @param setkey
	 * @param member
	 * @return
	 */
	public <K extends Object> Future<Long> zrevrank (K setkey, byte[] member);
	public <K extends Object> Future<Long> zrevrank (K setkey, String stringValue);
	public <K extends Object> Future<Long> zrevrank (K setkey, Number numberValue);
	public <K extends Object, T extends Serializable> 
		Future<Long> zrevrank (K setkey, T object);

	/**
	 * @Redis ZRANGE
	 * @param setkey
	 * @param from
	 * @param to
	 * @return
	 */
	public <K extends Object> Future<List<byte[]>> zrange (K setkey, long from, long to); 

	/**
	 * @Redis ZREVRANGE
	 * @param setkey
	 * @param from
	 * @param to
	 * @return
	 */
	public <K extends Object> Future<List<byte[]>> zrevrange (K setkey, long from, long to); 

	/**
	 * Equivalent to {@link JRedis#zrange(String, long, long)} with the {@link Command.Option#WITHSCORES}.
	 * Unlike the general ZRANGE command that only returns the values, this method returns both
	 * values and associated scores for the specified range.
	 * 
	 * @Redis ZRANGE ... WITHSCORES
	 * @param setkey
	 * @param from
	 * @param to
	 * @return
	 * @see JRedis#zrange(String, long, long)
	 * @see ZSetEntry
	 */
	public <K extends Object> Future<List<ZSetEntry>> zrangeSubset (K setkey, long from, long to); 

	/**
	 * Equivalent to {@link JRedis#zrange(String, long, long)} with the {@link Command.Option#WITHSCORES}.
	 * Unlike the general ZRANGE command that only returns the values, this method returns both
	 * values and associated scores for the specified range.
	 * 
	 * @Redis ZREVRANGE ... WITHSCORES
	 * @param setkey
	 * @param from
	 * @param to
	 * @return
	 * @see JRedis#zrevrange(String, long, long)
	 * @see ZSetEntry
	 */
	public <K extends Object> Future<List<ZSetEntry>> zrevrangeSubset (K setkey, long from, long to); 

	/**
	 * @Redis ZINCRBY
	 * @param setkey
	 * @param score
	 * @param member
	 * @return
	 */
	@Redis(versions="1.07")
	public <K extends Object> Future<Double> zincrby (K setkey, double score, byte[] member);
	public <K extends Object> Future<Double> zincrby (K setkey, double score, String stringValue);
	public <K extends Object> Future<Double> zincrby (K setkey, double score, Number numberValue);
	public <K extends Object, T extends Serializable> 
		Future<Double> zincrby (K setkey, double score, T object);

	/**
	 * @Redis ZRANGEBYSCORE
	 * @param setkey
	 * @param from
	 * @param to
	 * @return
	 */
	public <K extends Object> Future<List<byte[]>> zrangebyscore (K setkey, double minScore, double maxScore); 

	/**
	 * @Redis ZRANGEBYSCORE ... WITHSCORES
	 * @param setkey
	 * @param minScore
	 * @param maxScore
	 * @return
	 */
	public <K extends Object> Future<List<ZSetEntry>> zrangebyscoreSubset (K setkey, double minScore, double maxScore);

	/**
	 * @Redis ZREMRANGEBYSCORE
	 * @param setkey
	 * @param from
	 * @param to
	 * @return number of removed elements
	 */
	public <K extends Object> Future<Long> zremrangebyscore (K setkey, double minScore, double maxScore); 

	/**
	 * @Redis ZCOUNT
	 * @param setkey
	 * @param minScore
	 * @param maxScore
	 * @return number of removed elements
	 */
	public <K extends Object> Future<Long> zcount (K setkey, double minScore, double maxScore); 

	/**
	 * @Redis ZREMRANGEBYRANK
	 * @param setkey
	 * @param from
	 * @param to
	 * @return number of removed elements
	 */
	public <K extends Object> Future<Long> zremrangebyrank (K setkey, long minRank, long maxRank); 
	
	
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
	public <K extends Object> Future<Boolean> hset(K key, K entry, byte[] value);
	
	/**
	 * @Redis HSET
	 * @param key
	 * @param field
	 * @param string
	 * @return
	 */
	@Redis(versions="1.3.n")
	public <K extends Object> Future<Boolean> hset(K key, K entry, String string);
	
	/**
	 * @Redis HSET
	 * @param key
	 * @param field
	 * @param number
	 * @return
	 */
	@Redis(versions="1.3.n")
	public <K extends Object> Future<Boolean> hset(K key, K entry, Number number);
	
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
		Future<Boolean> hset(K key, K entry, T object);
	
	/**
	 * @Redis HGET
	 * @param key
	 * @param field
	 * @return
	 */
	@Redis(versions="1.3.4")
	public <K extends Object> Future<byte[]> hget(K key, K entry);
	

	/**
	 * @Redis HINCRBY
	 * @param key
	 * @param entry
	 * @param increment
	 * @return
	 */
	public <K extends Object> Future<Long> hincrby(K key, K entry, long increment);
	
	
	/**
	 * 
	 * @Redis HEXISTS
	 * @param key
	 * @param field
	 * @return true if the spec'd field exists for the spec'd (hash type) key
	 */
	@Redis(versions="1.3.5")
	public <K extends Object> Future<Boolean> hexists(K key, K entry);
	
	/**
	 * 
	 * @Redis HDEL
	 * @param key
	 * @param field
	 * @return true if the spec'd field exists for the spec'd (hash type) key
	 */
	@Redis(versions="1.3.5")
	public <K extends Object> Future<Boolean> hdel(K key, K entry);
	
	/**
	 * 
	 * @Redis HLEN
	 * @param key
	 * @param field
	 * @return # of fields/entries for the given hash type key
	 */
	@Redis(versions="1.3.5")
	public <K extends Object> Future<Long> hlen(K key);
	
	/**
	 * 
	 * @Redis HKEYS
	 * @param key
	 * @return list of keys in the given hashtable.
	 * @throws RedisException
	 */
	@Redis(versions="1.3.n")
	public <K extends Object> Future<List<byte[]>> hkeys(K key);
	
	/**
	 * 
	 * @Redis HVALS
	 * @param key
	 * @return list of values in the given hashtable.
	 * @throws RedisException
	 */
	@Redis(versions="1.3.n")
	public <K extends Object> Future<List<byte[]>> hvals(K key);
	
	/**
	 * 
	 * @Redis HGETALL
	 * @param key
	 * @return the given hash as a Map<String, byte[]>
	 * @throws RedisException
	 */
	@Redis(versions="1.3.n")
	public <K extends Object> Future<Map<byte[], byte[]>> hgetall(K key);
	
	// ------------------------------------------------------------------------
	// Multiple databases handling commands
	// ------------------------------------------------------------------------
	
//	@Deprecated
//	public <K extends Object> Future<ResponseStatus> select (int index);

	/**
	 * Flushes the db you selected when connecting to Redis server.  Typically,
	 * implementations will select db 0 on connecting if non was specified.  Remember
	 * that there is no roll-back.
	 * @Redis FLUSHDB
	 * @return
	 */
	public <K extends Object> Future<ResponseStatus> flushdb ();

	/**
	 * Flushes all dbs in the connect Redis server, regardless of which db was selected
	 * on connect time.  Remember that there is no rollback.
	 * @Redis FLUSHALL
	 * @return
	 */
	public <K extends Object> Future<ResponseStatus> flushall ();

	/**
	 * Moves the given key from the currently selected db to the one indicated
	 * by <code>dbIndex</code>.
	 * @Redis MOVE
	 * @param key
	 * @param dbIndex
	 * @return
	 */
	public <K extends Object> Future<Boolean> move (K key, int dbIndex);
	
	// ------------------------------------------------------------------------
	// Sorting
	// ------------------------------------------------------------------------
	
	/**
	 * <p>For Usage details regarding sort semantics, see {@link JRedis#sort}.  The
	 * only difference in usage is that you must use the {@link Sort#execAsync()} method
	 * which returns a {@link Future} instances.
	 * <p>Usage:
	 * <p><code><pre>
	 * Future<List<byte[]>>  futureResults = redis.sort("my-list-or-set-key").BY("weight*").LIMIT(1, 11).GET("object*").DESC().ALPHA().execAsync();
	 * List<byte[]> results = futureResult.get();  // wait for the asynchronous response to be processed
	 * for(byte[] item : results) {
	 *     // do something with item ..
	 *  }
	 * </pre></code>
	 * 
	 * @Redis SORT
	 * @see Redis
	 * @see Future
	 * 
	 */
	public <K extends Object> Sort sort(K key);
	
	// ------------------------------------------------------------------------
	// Persistence control commands
	// ------------------------------------------------------------------------

	/**
	 * @Redis SAVE
	 */
	public <K extends Object> Future<ResponseStatus> save();

	/**
	 * @Redis BGSAVE
	 */
	public <K extends Object> Future<ResponseStatus> bgsave ();

	/**
	 * @Redis BGREWRITEAOF
	 * @return ack message.  
	 */
	public <K extends Object> Future<String> bgrewriteaof ();

	/**
	 * @Redis LASTSAVE
	 * @return
	 */
	public <K extends Object> Future<Long> lastsave ();


// ------------------------------------------------------------------------
// Remote server control commands
// ------------------------------------------------------------------------

	/**
	 * @Redis INFO
	 * @return
	 */
	public <K extends Object> Future<Map<String, String>>	info () ;

	/**
	 * @Redis SLAVEOF
	 * @param host ip address 
	 * @param port
	 */
	public <K extends Object> Future<ResponseStatus>  slaveof(String host, int port);
	
	/**
	 * Convenience method.  Turns off replication.
	 * @Redis SLAVEOF "no one"
	 */
	public <K extends Object> Future<ResponseStatus>  slaveofnone();
	// ------------------------------------------------------------------------
	// Diagnostics commands
	// ------------------------------------------------------------------------
	
	/**
	 * @Redis ECHO
	 * @param msg
	 * @return
	 */
	public <K extends Object> Future<byte[]> echo (byte[] msg);
	public <K extends Object> Future<byte[]> echo (String msg);
	public <K extends Object> Future<byte[]> echo (Number msg);
	public <K extends Object, T extends Serializable> 
		Future<byte[]> echo (T msg);
		
	/**
	 * @Redis DEBUG OBJECT <key>
	 * @param key
	 * @return
	 */
	public <K extends Object> Future<ObjectInfo> debug (K key);
	

}
