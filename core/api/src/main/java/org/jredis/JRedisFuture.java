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
	public Future<ResponseStatus> ping ();

	/**
	 * Disconnects the client.
	 * @Redis QUIT
	 */
	public Future<ResponseStatus> quit ();
	
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
	public Future<ResponseStatus> set (String key, byte[] value);
	/**
	 * Convenient method for {@link String} data binding
	 * @Redis SET
	 * @param key
	 * @param stringValue
	 * @see {@link JRedis#set(String, byte[])}
	 */
	public Future<ResponseStatus> set (String key, String stringValue);
	/**
	 * Convenient method for {@link String} numeric values binding
	 * @Redis SET
	 * @param key
	 * @param numberValue
	 * @see {@link JRedis#set(String, byte[])}
	 */
	public Future<ResponseStatus> set (String key, Number numberValue);
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
	public <T extends Serializable> 
		   Future<ResponseStatus> set (String key, T object);

	/**
	 * @Redis SETNX
	 * @param key
	 * @param value
	 * @return
	 */
	public Future<Boolean> setnx (String key, byte[] value);
	public Future<Boolean> setnx (String key, String stringValue);
	public Future<Boolean> setnx (String key, Number numberValue);
	public <T extends Serializable> 
		   Future<Boolean> setnx (String key, T object);

	/**
	 * @Redis GET
	 * @param key
	 * @return
	 */
	public Future<byte[]> get (String key) ;

	public Future<byte[]> getset (String key, byte[] value);
	public Future<byte[]> getset (String key, String stringValue);
	public Future<byte[]> getset (String key, Number numberValue);
	public <T extends Serializable> 
		Future<byte[]> getset (String key, T object);

	
	/**
	 * @Redis MGET
	 * @param key
	 * @param moreKeys
	 * @return
	 */
	public Future<List<byte[]>> mget(String ... keys);

	/**
	 * @Redis MSET
	 * @param keyValueMap a {@link Map}ping of {@link String} key names to byte[] values.
	 * @return Future<Boolean> indicating if all of sets were OK or not
	 * @throws RedisException
	 */
	public Future<ResponseStatus> mset(Map<String, byte[]> keyValueMap);
	
	public Future<ResponseStatus> mset(KeyValueSet.ByteArrays mappings);
	public Future<ResponseStatus> mset(KeyValueSet.Strings mappings);
	public Future<ResponseStatus> mset(KeyValueSet.Numbers mappings);
	public <T extends Serializable> Future<ResponseStatus> mset(KeyValueSet.Objects<T> mappings);
	
	/**
	 * @Redis MSETNX
	 * @param keyValueMap a {@link Map}ping of {@link String} key names to byte[] values.
	 * @return Future<Boolean> indicating if all of sets were OK or not
	 * @throws RedisException
	 */
	public Future<Boolean> msetnx(Map<String, byte[]> keyValueMap);
	
	public Future<Boolean> msetnx(KeyValueSet.ByteArrays mappings);
	public Future<Boolean> msetnx(KeyValueSet.Strings mappings);
	public Future<Boolean> msetnx(KeyValueSet.Numbers mappings);
	public <T extends Serializable> Future<Boolean> msetnx(KeyValueSet.Objects<T> mappings);
	
	/**
	 * @Redis INCR
	 * @param key
	 * @return
	 */
	public Future<Long> incr (String key);

	/**
	 * @Redis INCRBY
	 * @param key
	 * @param delta
	 * @return
	 */
	public Future<Long> incrby (String key, int delta);

	/**
	 * @Redis DECR
	 * @param key
	 * @return
	 */
	public Future<Long> decr (String key);

	/**
	 * @Redis DECRBY
	 * @param key
	 * @param delta
	 * @return
	 */
	public Future<Long> decrby (String key, int delta);

	/**
	 * @Redis SUBSTR
	 * @param listkey
	 * @param from
	 * @param to
	 * @return
	 */
	public Future<byte[]> substr (String listkey, long from, long to); 
	
	/**
	 * @Redis APPEND
	 * @param key
	 * @param value
	 * @return the length (byte count) of appended key.
	 */
	public Future<Long> append (String key, byte[] value);
	public Future<Long> append (String key, String stringValue);
	public Future<Long> append (String key, Number numberValue);
	public <T extends Serializable> 
		   Future<Long> append (String key, T object);

	/**
	 * @Redis EXISTS
	 * @param key
	 * @return
	 */
	public Future<Boolean> exists(String key);

	/**
	 * @Redis DEL
	 * @param keys one or more, non-null, non-zero-length, keys to be deleted
	 * @return Future<Long> of number keys actually deleted.
	 */
	public Future<Long> del (String ... keys);

	/**
	 * @Redis TYPE
	 * @param key
	 * @return
	 */
	public Future<RedisType> type (String key);
	
	
	// ------------------------------------------------------------------------
	// "Commands operating on the key space"
	// ------------------------------------------------------------------------
	
	/**
	 * @Redis KEYS
	 * @param pattern
	 * @return
	 */
	public Future<List<String>> keys (String pattern);
	
	/**
	 * Convenience method.  Equivalent to calling <code>jredis.keys("*");</code>
	 * @Redis KEYS
	 * @return
	 * @see {@link JRedis#keys(String)}
	 */
	public Future<List<String>> keys ();

	/**
	 * @Redis RANDOMKEY
	 * @return
	 */
	public Future<String> randomkey();
	
	/**
	 * @Redis RENAME
	 * @param oldkey
	 * @param newkey
	 */
	public Future<ResponseStatus> rename (String oldkey, String newkey);
	
	/**
	 * @Redis RENAMENX
	 * @param oldkey
	 * @param brandnewkey
	 * @return
	 */
	public Future<Boolean> renamenx (String oldkey, String brandnewkey);
	
	/**
	 * @Redis DBSIZE
	 * @return
	 */
	public Future<Long> dbsize ();
	
	/**
	 * @Redis EXPIRE
	 * @param key
	 * @param ttlseconds
	 * @return
	 */
	public Future<Boolean> expire (String key, int ttlseconds); 
	
	/**
	 * @Redis EXPIREAT
	 * @param key
	 * @param UNIX epoch-time in <b>milliseconds</b>.  Note that Redis expects epochtime
	 * in seconds. Implementations are responsible for converting to seconds.
	 * method   
	 * @return
	 * @see {@link System#currentTimeMillis()}
	 */
	public Future<Boolean> expireat (String key, long epochtimeMillisecs); 
	
	/**
	 * @Redis TTL
	 * @param key
	 * @return
	 */
	public Future<Long> ttl (String key);
	
	// ------------------------------------------------------------------------
	// Commands operating on lists
	// ------------------------------------------------------------------------

	/**
	 * @Redis RPUSH
	 * @param listkey
	 * @param value
	 */
	public Future<ResponseStatus> rpush (String listkey, byte[] value);
	public Future<ResponseStatus> rpush (String listkey, String stringValue);
	public Future<ResponseStatus> rpush (String listkey, Number numberValue);
	public <T extends Serializable> 
		Future<ResponseStatus> rpush (String listkey, T object);
	
	/**
	 * @Redis LPUSH
	 * @param listkey
	 * @param value
	 */
	public Future<ResponseStatus> lpush (String listkey, byte[] value);
	public Future<ResponseStatus> lpush (String listkey, String stringValue);
	public Future<ResponseStatus> lpush (String listkey, Number numberValue);
	public <T extends Serializable> 
		Future<ResponseStatus> lpush (String listkey, T object);
	
	/**
	 * @Redis LSET
	 * @param key
	 * @param index
	 * @param value
	 */
	public Future<ResponseStatus> lset (String key, long index, byte[] value);
	public Future<ResponseStatus> lset (String key, long index, String stringValue);
	public Future<ResponseStatus> lset (String key, long index, Number numberValue);
	public <T extends Serializable> 
		Future<ResponseStatus> lset (String key, long index, T object);
	

	/**
	 * @Redis LREM
	 * @param listKey
	 * @param value
	 * @param count
	 * @return
	 */
	public Future<Long> lrem (String listKey, byte[] value,       int count);
	public Future<Long> lrem (String listKey, String stringValue, int count);
	public Future<Long> lrem (String listKey, Number numberValue, int count);
	public <T extends Serializable> 
		Future<Long> lrem (String listKey, T object, int count);
	
	/**
	 * Given a 'list' key, returns the number of items in the list.
	 * @Redis LLEN
	 * @param listkey
	 * @return
	 */
	public Future<Long> llen (String listkey);
	
	/**
	 * @Redis LRANGE
	 * @param listkey
	 * @param from
	 * @param to
	 * @return
	 */
	public Future<List<byte[]>> lrange (String listkey, long from, long to); 

	/**
	 * @Redis LTRIM
	 * @param listkey
	 * @param keepFrom
	 * @param keepTo
	 */
	public Future<ResponseStatus> ltrim (String listkey, long keepFrom, long keepTo);
	
	/**
	 * @Redis LINDEX
	 * @param listkey
	 * @param index
	 * @return
	 */
	public Future<byte[]> lindex (String listkey, long index);
	
	/**
	 * @Redis LPOP
	 * @param listKey
	 * @return
	 */
	public Future<byte[]> lpop (String listKey);
	
	/**
	 * @Redis RPOP
	 * @param listKey
	 * @return
	 */
	public Future<byte[]> rpop (String listKey);

	/**
	 * @Redis RPOPLPUSH
	 * @param srcList
	 * @param destList
	 * @return
	 */
	public Future<byte[]> rpoplpush (String srcList, String destList);
	// ------------------------------------------------------------------------
	// Commands operating on sets
	// ------------------------------------------------------------------------
	
	/**
	 * @Redis SADD
	 * @param setkey
	 * @param member
	 * @return
	 */
	public Future<Boolean> sadd (String setkey, byte[] member);
	public Future<Boolean> sadd (String setkey, String stringValue);
	public Future<Boolean> sadd (String setkey, Number numberValue);
	public <T extends Serializable> 
		Future<Boolean> sadd (String setkey, T object);

	/**
	 * @Redis SREM
	 * @param setKey
	 * @param member
	 * @return
	 */
	public Future<Boolean> srem (String setKey, byte[] member);
	public Future<Boolean> srem (String setKey, String stringValue);
	public Future<Boolean> srem (String setKey, Number numberValue);
	public <T extends Serializable> 
		Future<Boolean> srem (String setKey, T object);

	/**
	 * @Redis SISMEMBER
	 * @param setKey
	 * @param member
	 * @return
	 */
	public Future<Boolean> sismember (String setKey, byte[] member);
	public Future<Boolean> sismember (String setKey, String stringValue);
	public Future<Boolean> sismember (String setKey, Number numberValue);
	public <T extends Serializable> 
		Future<Boolean> sismember (String setKey, T object);
	
	/**
	 * @Redis SMOVE
	 * @param srcKey
	 * @param destKey
	 * @param member
	 * @return
	 */
	public Future<Boolean> smove (String srcKey, String destKey, byte[] member);
	public Future<Boolean> smove (String srcKey, String destKey, String stringValue);
	public Future<Boolean> smove (String srcKey, String destKey, Number numberValue);
	public <T extends Serializable> 
		Future<Boolean> smove (String srcKey, String destKey, T object);
	
	/**
	 * @Redis SCARD
	 * @param setKey
	 * @return
	 */
	public Future<Long> scard (String setKey);	
	
	/**
	 * @Redis SINTER
	 * @param set1
	 * @param sets
	 * @return
	 */
	public Future<List<byte[]>> sinter (String set1, String...sets);
	/**
	 * @Redis SINTERSTORE
	 * @param destSetKey
	 * @param sets
	 */
	public Future<ResponseStatus> sinterstore (String destSetKey, String...sets);

	/**
	 * @Redis SUNION
	 * @param set1
	 * @param sets
	 * @return
	 */
	public Future<List<byte[]>> sunion (String set1, String...sets);
	
	/**
	 * @Redis SUNIONSTORE
	 * @param destSetKey
	 * @param sets
	 */
	public Future<ResponseStatus> sunionstore (String destSetKey, String...sets);

	/**
	 * @Redis SDIFF
	 * @param set1
	 * @param sets
	 * @return
	 */
	public Future<List<byte[]>> sdiff (String set1, String...sets);
	
	/**
	 * @Redis SDIFFSTORE
	 * @param destSetKey
	 * @param sets
	 */
	public Future<ResponseStatus> sdiffstore (String destSetKey, String...sets);

	/**
	 * @Redis SMEMBERS
	 * @param setkey
	 * @return
	 */
	public Future<List<byte[]>> smembers (String setkey);
	
	/**
	 * @Redis SRANDMEMBER
	 * @param setkey
	 * @return
	 */
	public Future<byte[]> srandmember (String setkey);

	/**
	 * @Redis SPOP
	 * @param setkey
	 * @return
	 */
	public Future<byte[]> spop (String setkey);
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
	public Future<Boolean> zadd (String setkey, double score, byte[] member);
	public Future<Boolean> zadd (String setkey, double score, String stringValue);
	public Future<Boolean> zadd (String setkey, double score, Number numberValue);
	public <T extends Serializable> 
		Future<Boolean> zadd (String setkey, double score, T object);

	/**
	 * @Redis ZREM
	 * @param setKey
	 * @param member
	 * @return
	 */
	public Future<Boolean> zrem (String setKey, byte[] member);
	public Future<Boolean> zrem (String setKey, String stringValue);
	public Future<Boolean> zrem (String setKey, Number numberValue);
	public <T extends Serializable> 
		Future<Boolean> zrem (String setKey, T object);

	/**
	 * @Redis ZCARD
	 * @param setKey
	 * @return
	 */
	public Future<Long> zcard (String setKey);	
	
	/**
	 * @Redis ZSCORE
	 * @param setkey
	 * @param member
	 * @return
	 */
	public Future<Double> zscore (String setkey, byte[] member);
	public Future<Double> zscore (String setkey, String stringValue);
	public Future<Double> zscore (String setkey, Number numberValue);
	public <T extends Serializable> 
		Future<Double> zscore (String setkey, T object);

	/**
	 * @Redis ZRANK
	 * @param setkey
	 * @param member
	 * @return
	 */
	public Future<Long> zrank (String setkey, byte[] member);
	public Future<Long> zrank (String setkey, String stringValue);
	public Future<Long> zrank (String setkey, Number numberValue);
	public <T extends Serializable> 
		Future<Long> zrank (String setkey, T object);

	/**
	 * @Redis ZREVRANK
	 * @param setkey
	 * @param member
	 * @return
	 */
	public Future<Long> zrevrank (String setkey, byte[] member);
	public Future<Long> zrevrank (String setkey, String stringValue);
	public Future<Long> zrevrank (String setkey, Number numberValue);
	public <T extends Serializable> 
		Future<Long> zrevrank (String setkey, T object);

	/**
	 * @Redis ZRANGE
	 * @param setkey
	 * @param from
	 * @param to
	 * @return
	 */
	public Future<List<byte[]>> zrange (String setkey, long from, long to); 

	/**
	 * @Redis ZREVRANGE
	 * @param setkey
	 * @param from
	 * @param to
	 * @return
	 */
	public Future<List<byte[]>> zrevrange (String setkey, long from, long to); 

	/**
	 * Equivalent to {@link JRedis#zrange(String, long, long)} with the {@link Command.Options#WITHSCORES}.
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
	public Future<List<ZSetEntry>> zrangeSubset (String setkey, long from, long to); 

	/**
	 * Equivalent to {@link JRedis#zrange(String, long, long)} with the {@link Command.Options#WITHSCORES}.
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
	public Future<List<ZSetEntry>> zrevrangeSubset (String setkey, long from, long to); 

	/**
	 * @Redis ZINCRBY
	 * @param setkey
	 * @param score
	 * @param member
	 * @return
	 */
	@Redis(versions="1.07")
	public Future<Double> zincrby (String setkey, double score, byte[] member);
	public Future<Double> zincrby (String setkey, double score, String stringValue);
	public Future<Double> zincrby (String setkey, double score, Number numberValue);
	public <T extends Serializable> 
		Future<Double> zincrby (String setkey, double score, T object);

	/**
	 * @Redis ZRANGEBYSCORE
	 * @param setkey
	 * @param from
	 * @param to
	 * @return
	 */
	public Future<List<byte[]>> zrangebyscore (String setkey, double minScore, double maxScore); 

	/**
	 * @Redis ZREMRANGEBYSCORE
	 * @param setkey
	 * @param from
	 * @param to
	 * @return number of removed elements
	 */
	public Future<Long> zremrangebyscore (String setkey, double minScore, double maxScore); 

	/**
	 * @Redis ZCOUNT
	 * @param setkey
	 * @param minScore
	 * @param maxScore
	 * @return number of removed elements
	 */
	public Future<Long> zcount (String setkey, double minScore, double maxScore); 

	/**
	 * @Redis ZREMRANGEBYRANK
	 * @param setkey
	 * @param from
	 * @param to
	 * @return number of removed elements
	 */
	public Future<Long> zremrangebyrank (String setkey, double minRank, double maxRank); 
	
	
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
	public Future<Boolean> hset(String key, String field, byte[] value);
	
	/**
	 * @Redis HSET
	 * @param key
	 * @param field
	 * @param string
	 * @return
	 */
	@Redis(versions="1.3.n")
	public Future<Boolean> hset(String key, String field, String string);
	
	/**
	 * @Redis HSET
	 * @param key
	 * @param field
	 * @param number
	 * @return
	 */
	@Redis(versions="1.3.n")
	public Future<Boolean> hset(String key, String field, Number number);
	
	/**
	 * @Redis HSET
	 * @param <T>
	 * @param key
	 * @param field
	 * @param object
	 * @return
	 */
	@Redis(versions="1.3.4")
	public <T extends Serializable> 
		Future<Boolean> hset(String key, String field, T object);
	
	/**
	 * @Redis HGET
	 * @param key
	 * @param field
	 * @return
	 */
	@Redis(versions="1.3.4")
	public Future<byte[]> hget(String key, String field);
	
	/**
	 * 
	 * @Redis HEXISTS
	 * @param key
	 * @param field
	 * @return true if the spec'd field exists for the spec'd (hash type) key
	 */
	@Redis(versions="1.3.5")
	public Future<Boolean> hexists(String key, String field);
	
	/**
	 * 
	 * @Redis HDEL
	 * @param key
	 * @param field
	 * @return true if the spec'd field exists for the spec'd (hash type) key
	 */
	@Redis(versions="1.3.5")
	public Future<Boolean> hdel(String key, String field);
	
	/**
	 * 
	 * @Redis HLEN
	 * @param key
	 * @param field
	 * @return # of fields/entries for the given hash type key
	 */
	@Redis(versions="1.3.5")
	public Future<Long> hlen(String key);
	
	/**
	 * 
	 * @Redis HKEYS
	 * @param key
	 * @return list of keys in the given hashtable.
	 * @throws RedisException
	 */
	@Redis(versions="1.3.n")
	public Future<List<String>> hkeys(String key);
	
	/**
	 * 
	 * @Redis HVALS
	 * @param key
	 * @return list of values in the given hashtable.
	 * @throws RedisException
	 */
	@Redis(versions="1.3.n")
	public Future<List<byte[]>> hvals(String key);
	
	/**
	 * 
	 * @Redis HGETALL
	 * @param key
	 * @return the given hash as a Map<String, byte[]>
	 * @throws RedisException
	 */
	@Redis(versions="1.3.n")
	public Future<Map<String, byte[]>> hgetall(String key);
	
	// ------------------------------------------------------------------------
	// Multiple databases handling commands
	// ------------------------------------------------------------------------
	
//	@Deprecated
//	public Future<ResponseStatus> select (int index);

	/**
	 * Flushes the db you selected when connecting to Redis server.  Typically,
	 * implementations will select db 0 on connecting if non was specified.  Remember
	 * that there is no roll-back.
	 * @Redis FLUSHDB
	 * @return
	 */
	public Future<ResponseStatus> flushdb ();

	/**
	 * Flushes all dbs in the connect Redis server, regardless of which db was selected
	 * on connect time.  Remember that there is no rollback.
	 * @Redis FLUSHALL
	 * @return
	 */
	public Future<ResponseStatus> flushall ();

	/**
	 * Moves the given key from the currently selected db to the one indicated
	 * by <code>dbIndex</code>.
	 * @Redis MOVE
	 * @param key
	 * @param dbIndex
	 * @return
	 */
	public Future<Boolean> move (String key, int dbIndex);
	
	// ------------------------------------------------------------------------
	// Sorting
	// ------------------------------------------------------------------------
	
	/**
	 * <p>For Usage details regarding sort semantics, see {@link JRedis#sort}.  The
	 * only difference in usage is that you must use the {@link Sort#execAsynch()} method
	 * which returns a {@link Future} instances.
	 * <p>Usage:
	 * <p><code><pre>
	 * Future<List<byte[]>>  futureResults = redis.sort("my-list-or-set-key").BY("weight*").LIMIT(1, 11).GET("object*").DESC().ALPHA().execAsynch();
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
	public Sort sort(String key);
	
	// ------------------------------------------------------------------------
	// Persistence control commands
	// ------------------------------------------------------------------------

	/**
	 * @Redis SAVE
	 */
	public Future<ResponseStatus> save();

	/**
	 * @Redis BGSAVE
	 */
	public Future<ResponseStatus> bgsave ();

	/**
	 * @Redis BGREWRITEAOF
	 * @return ack message.  
	 */
	public Future<String> bgrewriteaof ();

	/**
	 * @Redis LASTSAVE
	 * @return
	 */
	public Future<Long> lastsave ();


// ------------------------------------------------------------------------
// Remote server control commands
// ------------------------------------------------------------------------

	/**
	 * @Redis INFO
	 * @return
	 */
	public Future<Map<String, String>>	info () ;

	/**
	 * @Redis SLAVEOF
	 * @param host ip address 
	 * @param port
	 */
	public Future<ResponseStatus>  slaveof(String host, int port);
	
	/**
	 * Convenience method.  Turns off replication.
	 * @Redis SLAVEOF "no one"
	 */
	public Future<ResponseStatus>  slaveofnone();
	// ------------------------------------------------------------------------
	// Diagnostics commands
	// ------------------------------------------------------------------------
	
	/**
	 * @Redis ECHO
	 * @param msg
	 * @return
	 */
	public Future<byte[]> echo (byte[] msg);
	public Future<byte[]> echo (String msg);
	public Future<byte[]> echo (Number msg);
	public <T extends Serializable> 
		Future<byte[]> echo (T msg);
		
	/**
	 * @Redis DEBUG OBJECT <key>
	 * @param key
	 * @return
	 */
	public Future<ObjectInfo> debug (String key);
}
