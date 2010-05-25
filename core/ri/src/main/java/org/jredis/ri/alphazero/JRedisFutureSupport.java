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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jredis.ClientRuntimeException;
import org.jredis.JRedisFuture;
import org.jredis.KeyValueSet;
import org.jredis.ObjectInfo;
import org.jredis.ProviderException;
import org.jredis.RedisType;
import org.jredis.Sort;
import org.jredis.ZSetEntry;
import org.jredis.connector.Connection;
import org.jredis.protocol.BulkResponse;
import org.jredis.protocol.Command;
import org.jredis.protocol.MultiBulkResponse;
import org.jredis.protocol.Response;
import org.jredis.protocol.ResponseStatus;
import org.jredis.protocol.ValueResponse;
import org.jredis.ri.alphazero.semantics.DefaultKeyCodec;
import org.jredis.ri.alphazero.support.Convert;
import org.jredis.ri.alphazero.support.DefaultCodec;
import org.jredis.ri.alphazero.support.SortSupport;
import org.jredis.semantics.KeyCodec;

/**
 * 
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 10, 2009
 * @since   alpha.0
 *
 */
public abstract class JRedisFutureSupport implements JRedisFuture {
	
	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------
	
	// ------------------------------------------------------------------------
	// Extension point(s)
	/*
	 * This class provides the convenience of a uniform implementation wide mapping
	 * of JRedis api semantics to the native protocol level semantics of byte[]s.
	 * 
	 * Extensions can use the provided extension points to provide or delegate the
	 * servicing of request calls.  
	 */
	// ------------------------------------------------------------------------
	
	/**
	 * This method mimics the eponymous {@link Connection#queueRequest(Command, byte[]...)}
	 * which defines the blocking api semantics of Synchronous connections.  The extending class
	 * can either directly (a) implement the protocol requirements, or, (b) delegate to a
	 * {@link Connection} instance, or, (c) utilize a pool of {@link Connection}s.  
	 * 
	 * @param cmd
	 * @param args
	 * @return
	 * @throws ClientRuntimeException
	 * @throws ProviderException
	 */
	abstract protected  Future<Response> queueRequest (Command cmd, byte[]...args) throws ClientRuntimeException, ProviderException; 
	// ------------------------------------------------------------------------
	// INTERFACE
	// ================================================================ Redis
	/*
	 * Support of all the JRedisFuture interface methods.
	 * 
	 * This class uses the UTF-8 character set for all conversions due to its
	 * use of the Convert and Codec support classes.
	 * 
	 * All calls are forwarded to an abstract queueRequest method that the
	 * extending classes are expected to implement.  
	 */
	// ------------------------------------------------------------------------


//	@Override
	public FutureStatus bgsave() {
		return new FutureStatus(this.queueRequest(Command.BGSAVE));
	}
	
//	@Override
	public FutureString bgrewriteaof() {
		Future<Response> futureResponse = this.queueRequest(Command.BGREWRITEAOF);
		return new FutureString(futureResponse);
	}

//	@Override
	public FutureStatus ping() {
		return new FutureStatus(this.queueRequest(Command.PING));
	}

//	@Override
	public FutureStatus flushall() {
		return new FutureStatus(this.queueRequest(Command.FLUSHALL));
	}
//	@Override
	public FutureStatus flushdb() {
		return new FutureStatus(this.queueRequest(Command.FLUSHDB));
	}
////	@Override
//	public FutureStatus select(int index) {
//		this.queueRequest(Command.SELECT, Convert.toBytes(index));
//		return this;
//	}

	public Future<ResponseStatus>  slaveof(String host, int port) {
		byte[] hostbytes = null;
		if((hostbytes = getKeyBytes(host)) == null) 
			throw new IllegalArgumentException ("invalid host => ["+host+"]");

		byte[] portbytes = null;
		if((portbytes = Convert.toBytes(port)) == null) 
			throw new IllegalArgumentException ("invalid port => ["+port+"]");

		return new FutureStatus(this.queueRequest(Command.SLAVEOF, hostbytes, portbytes));
	}
	public Future<ResponseStatus>  slaveofnone() {
		return new FutureStatus(this.queueRequest(Command.SLAVEOF, "no".getBytes(), "one".getBytes()));
	}
	
//	@Override
	public FutureStatus rename(String oldkey, String newkey) {
		byte[] oldkeydata = null;
		if((oldkeydata = getKeyBytes(oldkey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+oldkey+"]");

		byte[] newkeydata = null;
		if((newkeydata = getKeyBytes(newkey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+newkey+"]");

		return new FutureStatus(this.queueRequest(Command.RENAME, oldkeydata, newkeydata));
	}
	
//	@Override
	public Future<Boolean> renamenx(String oldkey, String newkey){
		byte[] oldkeydata = null;
		if((oldkeydata = getKeyBytes(oldkey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+oldkey+"]");

		byte[] newkeydata = null;
		if((newkeydata = getKeyBytes(newkey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+newkey+"]");

		Future<Response> futureResponse = this.queueRequest(Command.RENAMENX, oldkeydata, newkeydata);
		return new FutureBoolean(futureResponse);
	}
	public FutureStatus rpush(String key, byte[] value)  {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
		
		if(value == null) 
			throw new IllegalArgumentException ("null value");
		
		return new FutureStatus(this.queueRequest(Command.RPUSH, keybytes, value));
	}

	public FutureStatus rpushx(String key, byte[] value) {
		byte[] keybytes = null;
		if ((keybytes = getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		if (value == null)
			throw new IllegalArgumentException ("null value");

		return new FutureStatus(this.queueRequest(Command.RPUSHX, keybytes, value));
	}

	public FutureStatus rpushxafter(String key, byte[] oldvalue, byte[] newvalue) {
		byte[] keybytes = null;
		if ((keybytes = getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
    byte[][] bulk = new byte[3][];
    bulk[0] = keybytes;
    bulk[1] = oldvalue;
    bulk[2] = newvalue;
		return new FutureStatus(this.queueRequest(Command.RPUSHXAFTER, bulk));
	}

	public FutureStatus ldelete(String key, byte[] value) {
		byte[] keybytes = null;
		if ((keybytes = getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		if (value == null)
			throw new IllegalArgumentException ("null value");

		return new FutureStatus(this.queueRequest(Command.LDELETE, keybytes, value));
	}

//	@Override
	public FutureByteArray rpoplpush (String srcList, String destList)  {
		byte[] srckeybytes = null;
		if((srckeybytes = getKeyBytes(srcList)) == null) 
			throw new IllegalArgumentException ("invalid src key => ["+srcList+"]");
		byte[] destkeybytes = null;
		if((destkeybytes = getKeyBytes(destList)) == null) 
			throw new IllegalArgumentException ("invalid dest key => ["+destList+"]");
		
		Future<Response> futureResponse = this.queueRequest(Command.RPOPLPUSH, srckeybytes, destkeybytes);
		return new FutureByteArray(futureResponse);
	}
//	@Override
	public FutureStatus rpush(String key, String value) {
//		rpush(key, DefaultCodec.encode(value));
		return rpush(key, DefaultCodec.encode(value));
	}
//	@Override
	public FutureStatus rpush(String key, Number value) {
		return rpush(key, String.valueOf(value).getBytes());
	}
//	@Override
	public <T extends Serializable> FutureStatus rpush (String key, T value)
	{
		return rpush(key, DefaultCodec.encode(value));
	}

//	@Override
	public Future<Boolean> sadd(String key, byte[] member) 
	{
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.SADD, keybytes, member);
		return new FutureBoolean(futureResponse);
	}
//	@Override
	public Future<Boolean> sadd (String key, String value) {
		return sadd (key, DefaultCodec.encode(value));
	}
//	@Override
	public Future<Boolean> sadd (String key, Number value) {
		return sadd (key, String.valueOf(value).getBytes());
	}
//	@Override
	public <T extends Serializable> Future<Boolean> sadd (String key, T value)
	{
		return sadd (key, DefaultCodec.encode(value));
	}

//	@Override
	public Future<Boolean> zadd(String key, double score, byte[] member) 
	{
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.ZADD, keybytes,  Convert.toBytes(score), member);
		return new FutureBoolean(futureResponse);
	}
//	@Override
	public Future<Boolean> zadd (String key, double score, String value) {
		return zadd (key, score, DefaultCodec.encode(value));
	}
//	@Override
	public Future<Boolean> zadd (String key, double score, Number value) {
		return zadd (key, score, String.valueOf(value).getBytes());
	}
//	@Override
	public <T extends Serializable> Future<Boolean> zadd (String key, double score, T value)
	{
		return zadd (key, score, DefaultCodec.encode(value));
	}

//	@Override
	public Future<Double> zincrby(String key, double score, byte[] member) 
	{
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.ZINCRBY, keybytes,  Convert.toBytes(score), member);
		return new FutureDouble(futureResponse);
	}
//	@Override
	public Future<Double> zincrby (String key, double score, String value) {
		return zincrby (key, score, DefaultCodec.encode(value));
	}
//	@Override
	public Future<Double> zincrby (String key, double score, Number value) {
		return zincrby (key, score, String.valueOf(value).getBytes());
	}
//	@Override
	public <T extends Serializable> Future<Double> zincrby (String key, double score, T value)
	{
		return zincrby (key, score, DefaultCodec.encode(value));
	}

//	@Override
	public FutureStatus save() 
	{
		return new FutureStatus(this.queueRequest(Command.SAVE));
	}
	
	// -------- set 

//	@Override
	public FutureStatus set(String key, byte[] value) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		return new FutureStatus(this.queueRequest(Command.SET, keybytes, value));
	}
//	@Override
	public FutureStatus set(String key, String value) {
		return set(key, DefaultCodec.encode(value));
	}
//	@Override
	public FutureStatus set(String key, Number value) {
		return set(key, String.valueOf(value).getBytes());
	}
//	@Override
	public <T extends Serializable> FutureStatus set (String key, T value)
	{
		return set(key, DefaultCodec.encode(value));
	}
	
//	@Override
	public Future<byte[]> getset(String key, byte[] value) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.GETSET, keybytes, value);
		return new FutureByteArray(futureResponse);
	}
//	@Override
	public Future<byte[]> getset(String key, String value) {
		return getset(key, DefaultCodec.encode(value));
	}
//	@Override
	public Future<byte[]> getset(String key, Number value) {
		return getset(key, String.valueOf(value).getBytes());
	}
//	@Override
	public <T extends Serializable> 
	Future<byte[]> getset (String key, T value)
	{
		return getset(key, DefaultCodec.encode(value));
	}
	
//	@Override
	public Future<Boolean> setnx(String key, byte[] value){
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.SETNX, keybytes, value);
		return new FutureBoolean(futureResponse);
	}
//	@Override
	public Future<Boolean> setnx(String key, String value) {
		return setnx(key, DefaultCodec.encode(value));
	}
//	@Override
	public Future<Boolean> setnx(String key, Number value) {
		return setnx(key, String.valueOf(value).getBytes());
	}
//	@Override
	public <T extends Serializable> Future<Boolean> setnx (String key, T value) {
		return setnx(key, DefaultCodec.encode(value));
	}

	
//	@Override
	public Future<Long> append (String key, byte[] value){
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.APPEND, keybytes, value);
		return new FutureLong(futureResponse);
	}
//	@Override
	public Future<Long> append(String key, String value) {
		return append(key, DefaultCodec.encode(value));
	}
//	@Override
	public Future<Long> append(String key, Number value) {
		return append(key, String.valueOf(value).getBytes());
	}
//	@Override
	public <T extends Serializable> Future<Long> append (String key, T value) {
		return append(key, DefaultCodec.encode(value));
	}

//	@Override
	public Future<Boolean> sismember(String key, byte[] member) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.SISMEMBER, keybytes, member);
		return new FutureBoolean(futureResponse);
	}

//	@Override
	public Future<Boolean> sismember(String key, String value) {
		return sismember(key, DefaultCodec.encode(value));
	}

//	@Override
	public Future<Boolean> sismember(String key, Number numberValue) {
		return sismember (key, String.valueOf(numberValue).getBytes());
	}

//	@Override
	public <T extends Serializable> Future<Boolean> sismember(String key, T object) {
		return sismember(key, DefaultCodec.encode(object));
	}

	public Future<Boolean> smove (String srcKey, String destKey, byte[] member) {
		byte[] srcKeyBytes = null;
		if((srcKeyBytes = getKeyBytes(srcKey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+srcKey+"]");

		byte[] destKeyBytes = null;
		if((destKeyBytes = getKeyBytes(destKey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+destKey+"]");

		Future<Response> futureResponse = this.queueRequest(Command.SMOVE, srcKeyBytes, destKeyBytes, member);
		return new FutureBoolean(futureResponse);
	}
	public Future<Boolean> smove (String srcKey, String destKey, String stringValue) {
		return smove (srcKey, destKey, DefaultCodec.encode(stringValue));
	}
	public Future<Boolean> smove (String srcKey, String destKey, Number numberValue) {
		return smove (srcKey, destKey, String.valueOf(numberValue).getBytes());
	}
	public <T extends Serializable> 
		   Future<Boolean> smove (String srcKey, String destKey, T object) {
		return smove (srcKey, destKey, DefaultCodec.encode(object));
	}
		   
	// ------------------------------------------------------------------------
	// Commands operating on hashes
	// ------------------------------------------------------------------------
	
	public Future<Boolean> hset(String key, String field, byte[] value) {
		byte[] hashKeyBytes = null;
		if((hashKeyBytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] hashFieldBytes = null;
		if((hashFieldBytes = getKeyBytes(field)) == null) 
			throw new IllegalArgumentException ("invalid field => ["+field+"]");

		Future<Response> futureResponse = this.queueRequest(Command.HSET, hashKeyBytes, hashFieldBytes, value);
		return new FutureBoolean(futureResponse);
	}
	public Future<Boolean> hset(String key, String field, String stringValue) {
		return hset (key, field, DefaultCodec.encode(stringValue));
	}
	public Future<Boolean> hset(String key, String field, Number numberValue) {
		return hset (key, field, String.valueOf(numberValue).getBytes());
	}
	public <T extends Serializable> 
		Future<Boolean> hset(String key, String field, T object) {
		return hset (key, field, DefaultCodec.encode(object));
	}
	
	public Future<byte[]> hget(String hashKey, String hashField) {
		byte[] hashKeyBytes = null;
		if((hashKeyBytes = getKeyBytes(hashKey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+hashKey+"]");

		byte[] hashFieldBytes = null;
		if((hashFieldBytes = getKeyBytes(hashField)) == null) 
			throw new IllegalArgumentException ("invalid field => ["+hashField+"]");
		
		Future<Response> futureResponse = this.queueRequest(Command.HGET, hashKeyBytes, hashFieldBytes);
		return new FutureByteArray(futureResponse);
	}
	
	
	public Future<Boolean> hexists(String hashKey, String hashField) {
		byte[] hashKeyBytes = null;
		if((hashKeyBytes = getKeyBytes(hashKey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+hashKey+"]");

		byte[] hashFieldBytes = null;
		if((hashFieldBytes = getKeyBytes(hashField)) == null) 
			throw new IllegalArgumentException ("invalid field => ["+hashField+"]");
		
		Future<Response> futureResponse = this.queueRequest(Command.HEXISTS, hashKeyBytes, hashFieldBytes);
		return new FutureBoolean(futureResponse);
	}
	
	public Future<Boolean> hdel(String hashKey, String hashField) {
		byte[] hashKeyBytes = null;
		if((hashKeyBytes = getKeyBytes(hashKey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+hashKey+"]");

		byte[] hashFieldBytes = null;
		if((hashFieldBytes = getKeyBytes(hashField)) == null) 
			throw new IllegalArgumentException ("invalid field => ["+hashField+"]");
		
		Future<Response> futureResponse = this.queueRequest(Command.HDEL, hashKeyBytes, hashFieldBytes);
		return new FutureBoolean(futureResponse);
	}
	
	public Future<Long> hlen(String hashKey) {
		byte[] hashKeyBytes = null;
		if((hashKeyBytes = getKeyBytes(hashKey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+hashKey+"]");

		
		Future<Response> futureResponse = this.queueRequest(Command.HLEN, hashKeyBytes);
		return new FutureLong(futureResponse);
	}
	
	public Future<List<String>> hkeys(String hashKey) {
		byte[] hashKeyBytes = null;
		if((hashKeyBytes = getKeyBytes(hashKey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+hashKey+"]");

		Future<Response> futureResponse = this.queueRequest(Command.HKEYS, hashKeyBytes);
		return new FutureKeyList (futureResponse);
	}
	
	public Future<List<byte[]>> hvals(String hashKey) {
		byte[] hashKeyBytes = null;
		if((hashKeyBytes = getKeyBytes(hashKey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+hashKey+"]");

		Future<Response> futureResponse = this.queueRequest(Command.HKEYS, hashKeyBytes);
		return new FutureByteArrayList (futureResponse);
	}
	
	public Future<Map<String, byte[]>> hgetall(String hashKey) {
		byte[] hashKeyBytes = null;
		if((hashKeyBytes = getKeyBytes(hashKey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+hashKey+"]");

		Future<Response> futureResponse = this.queueRequest(Command.HGETALL, hashKeyBytes);
		return new FutureDataDictionary (futureResponse);
	}
	
	
	/* ------------------------------- commands returning int value --------- */

//	@Override
	public Future<Long> incr(String key) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.INCR, keybytes);
		return new FutureLong (futureResponse);
	}

//	@Override
	public Future<Long> incrby(String key, int delta) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.INCRBY, keybytes, Convert.toBytes(delta));
		return new FutureLong (futureResponse);
	}

//	@Override
	public Future<Long> decr(String key) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.DECR, keybytes);
		return new FutureLong (futureResponse);
	}

//	@Override
	public Future<Long> decrby(String key, int delta) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.DECRBY, keybytes, Convert.toBytes(delta));
		return new FutureLong (futureResponse);
	}

//	@Override
	public Future<Long> llen(String key) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.LLEN, keybytes);
		return new FutureLong (futureResponse);
	}

//	@Override
	public Future<Long> scard(String key) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.SCARD, keybytes);
		return new FutureLong (futureResponse);
	}
	
//	@Override
	public Future<Long> zcard(String key) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.ZCARD, keybytes);
		return new FutureLong (futureResponse);
	}
	
	public Future<byte[]> srandmember (String key) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.SRANDMEMBER, keybytes);
		return new FutureByteArray (futureResponse);
	}

	public Future<byte[]> spop (String key) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.SPOP, keybytes);
		return new FutureByteArray (futureResponse);
	}

	/* ------------------------------- commands returning long value --------- */

//	@Override
	public Future<Long> dbsize() {
		Future<Response> futureResponse = this.queueRequest(Command.DBSIZE);
		return new FutureLong (futureResponse);
	}
//	@Override
	public Future<Long> lastsave() {
		Future<Response> futureResponse = this.queueRequest(Command.LASTSAVE);
		return new FutureLong (futureResponse);
	}

	/* ------------------------------- commands returning byte[] --------- */

//	@Override
	public Future<byte[]> get(String key) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.GET, keybytes);
		return new FutureByteArray(futureResponse);
	}

//	@Override
	public Future<byte[]> lindex(String key, long index) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.LINDEX, keybytes, Convert.toBytes(index));
		return new FutureByteArray(futureResponse);
	}
//	@Override
	public Future<byte[]> lpop(String key) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.LPOP, keybytes);
		return new FutureByteArray(futureResponse);
	}

//	@Override
	public Future<byte[]> rpop(String key) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.RPOP, keybytes);
		return new FutureByteArray(futureResponse);
	}


	/* ------------------------------- commands returning String--------- */

//	@Override
	public Future<String> randomkey() {
		Future<Response> futureResponse = this.queueRequest(Command.RANDOMKEY);
		return new FutureString(futureResponse);
	}
//	@Override
	public Future<RedisType> type(String key) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		return new FutureRedisType(this.queueRequest(Command.TYPE, keybytes));
	}

	/* ------------------------------- commands returning Maps --------- */

//	@Override
	public Future<Map<String, String>> info() {
		return new FutureInfo(this.queueRequest(Command.INFO));
	}

//	@Override
	public Future<ObjectInfo> debug (String key) {
		byte[] keybytes = getKeyBytes(key);
		if(key.length() == 0)
			throw new IllegalArgumentException ("invalid zero length key => ["+key+"]");

		return new FutureObjectInfo (this.queueRequest(Command.DEBUG, "OBJECT".getBytes(), keybytes));
	}
	/* ------------------------------- commands returning Lists --------- */

//	@Override
	public Future<List<byte[]>> mget(String ... keys) {

		if(null == keys || keys.length == 0) throw new IllegalArgumentException("no keys specified");
		byte[] keydata = null;
		byte[][] keybytes = new byte[keys.length][];
		int i=0;
		for(String k : keys) {
			if((keydata = getKeyBytes(k)) == null) 
				throw new IllegalArgumentException ("invalid key => ["+k+"] @ index: " + i);
			
			keybytes[i++] = keydata;
		}
		return new FutureByteArrayList(this.queueRequest(Command.MGET, keybytes));
	}
	
	/* MSETs */
	private FutureStatus mset(byte[][] mappings){
		Future<Response> futureResponse = this.queueRequest(Command.MSET, mappings);
		return new FutureStatus(futureResponse);
	}
	public FutureStatus mset(Map<String, byte[]> keyValueMap){
		KeyCodec codec = DefaultKeyCodec.provider();
		byte[][] mappings = new byte[keyValueMap.size()*2][];
		int i = 0;
		for (Entry<String, byte[]> e : keyValueMap.entrySet()){
			mappings[i++] = codec.encode(e.getKey());
			mappings[i++] = e.getValue();
		}
		return mset(mappings);
	}
	
	public FutureStatus mset(KeyValueSet.ByteArrays keyValueMap){
		return mset(keyValueMap.getMappings());
	}
	public FutureStatus mset(KeyValueSet.Strings keyValueMap){
		return mset(keyValueMap.getMappings());
	}

	public FutureStatus mset(KeyValueSet.Numbers keyValueMap){
		return mset(keyValueMap.getMappings());
	}

	public <T extends Serializable> FutureStatus mset(KeyValueSet.Objects<T> keyValueMap){
		return mset(keyValueMap.getMappings());
	}

	/* MSETNXs */
	private Future<Boolean> msetnx(byte[][] mappings){
		Future<Response> futureResponse = this.queueRequest(Command.MSETNX, mappings);
		return new FutureBoolean(futureResponse);
	}
	public Future<Boolean> msetnx(Map<String, byte[]> keyValueMap){
		KeyCodec codec = DefaultKeyCodec.provider();
		byte[][] mappings = new byte[keyValueMap.size()*2][];
		int i = 0;
		for (Entry<String, byte[]> e : keyValueMap.entrySet()){
			mappings[i++] = codec.encode(e.getKey());
			mappings[i++] = e.getValue();
		}
		return msetnx(mappings);
	}
	
	public Future<Boolean> msetnx(KeyValueSet.ByteArrays keyValueMap){
		return msetnx(keyValueMap.getMappings());
	}
	public Future<Boolean> msetnx(KeyValueSet.Strings keyValueMap){
		return msetnx(keyValueMap.getMappings());
	}

	public Future<Boolean> msetnx(KeyValueSet.Numbers keyValueMap){
		return msetnx(keyValueMap.getMappings());
	}

	public <T extends Serializable> Future<Boolean> msetnx(KeyValueSet.Objects<T> keyValueMap){
		return msetnx(keyValueMap.getMappings());
	}


//	@Override
	public Future<List<byte[]>> smembers(String key) {
		byte[] keydata = null;
		if((keydata = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("null key.");

		return new FutureByteArrayList(this.queueRequest(Command.SMEMBERS, keydata));
	}
//	@Override
	public Future<List<String>> keys() {
		return this.keys("*");
	}

//	@Override
	public Future<List<String>> keys(String pattern) {
		byte[] keydata = null;
		if((keydata = getKeyBytes(pattern)) == null) 
			throw new IllegalArgumentException ("null key.");

		Future<Response> futureResponse = this.queueRequest(Command.KEYS, keydata);
		return new FutureKeyList(futureResponse);
	}

//	@Override
	public Future<List<byte[]>> lrange(String key, long from, long to) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(from);
		byte[] toBytes = Convert.toBytes(to);

		return new FutureByteArrayList(this.queueRequest(Command.LRANGE, keybytes, fromBytes, toBytes));
	}

//	@Override
	public Future<byte[]> substr(String key, long from, long to) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(from);
		byte[] toBytes = Convert.toBytes(to);

		return new FutureByteArray(this.queueRequest(Command.SUBSTR, keybytes, fromBytes, toBytes));
	}

//	@Override
	public Future<List<byte[]>> zrange(String key, long from, long to) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(from);
		byte[] toBytes = Convert.toBytes(to);

		return new FutureByteArrayList(this.queueRequest(Command.ZRANGE, keybytes, fromBytes, toBytes));
	}

//	@Override
	public Future<List<byte[]>> zrangebyscore(String key, double minScore, double maxScore) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] minScoreBytes = Convert.toBytes(minScore);
		byte[] maxScoreBytes = Convert.toBytes(maxScore);

		return new FutureByteArrayList(this.queueRequest(Command.ZRANGEBYSCORE, keybytes, minScoreBytes, maxScoreBytes));
	}
	
//	@Override
	public Future<Long> zremrangebyscore(String key, double minScore, double maxScore) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] minScoreBytes = Convert.toBytes(minScore);
		byte[] maxScoreBytes = Convert.toBytes(maxScore);

		return new FutureLong(this.queueRequest(Command.ZREMRANGEBYSCORE, keybytes, minScoreBytes, maxScoreBytes));
	}
	
//	@Override
	public Future<Long> zcount(String key, double minScore, double maxScore) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] minScoreBytes = Convert.toBytes(minScore);
		byte[] maxScoreBytes = Convert.toBytes(maxScore);

		return new FutureLong(this.queueRequest(Command.ZCOUNT, keybytes, minScoreBytes, maxScoreBytes));
	}
	
//	@Override
	public Future<Long> zremrangebyrank(String key, double minRank, double maxRank) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] minScoreBytes = Convert.toBytes(minRank);
		byte[] maxScoreBytes = Convert.toBytes(maxRank);

		return new FutureLong(this.queueRequest(Command.ZREMRANGEBYRANK, keybytes, minScoreBytes, maxScoreBytes));
	}


//	@Override
	public Future<List<byte[]>> zrevrange(String key, long from, long to) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(from);
		byte[] toBytes = Convert.toBytes(to);

		return new FutureByteArrayList(this.queueRequest(Command.ZREVRANGE, keybytes, fromBytes, toBytes));
	}

//	@Override
	public Future<List<ZSetEntry>> zrangeSubset(String key, long from, long to) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(from);
		byte[] toBytes = Convert.toBytes(to);

		return new FutureZSetList(this.queueRequest(Command.ZRANGE$OPTS, keybytes, fromBytes, toBytes, Command.Options.WITHSCORES.bytes));
	}

//	@Override
	public Future<List<ZSetEntry>> zrevrangeSubset(String key, long from, long to) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(from);
		byte[] toBytes = Convert.toBytes(to);

		return new FutureZSetList(this.queueRequest(Command.ZREVRANGE$OPTS, keybytes, fromBytes, toBytes, Command.Options.WITHSCORES.bytes));
	}
	
	// TODO: NOTIMPLEMENTED:
//	@Override
	public Sort sort(final String key) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		final JRedisFutureSupport client = this;
		Sort sortQuery = new SortSupport (key, keybytes) {
		//	@Override 
			protected Future<List<byte[]>> execAsynchSort(byte[] keyBytes, byte[] sortSpecBytes) {
				return new FutureByteArrayList(client.queueRequest(Command.SORT, keyBytes, sortSpecBytes));
			}
			protected List<byte[]> execSort(byte[] keyBytes, byte[] sortSpecBytes) {
				throw new IllegalStateException("JRedisFuture does not support synchronous sort.");
			}
		};
		return sortQuery;
	}

	/* ------------------------------- commands that don't get a response --------- */

//	@Override
	public FutureStatus quit()  {
		return new FutureStatus(this.queueRequest(Command.QUIT));
	}
//	@Override
	public Future<List<byte[]>> sinter(String set1, String... sets) {
		byte[] keydata = null;
		if((keydata = getKeyBytes(set1)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+set1+"]");

		byte[][] keybytes = new byte[1+sets.length][];
		int i=0; keybytes[i++] = keydata;
		for(String k : sets) {
			if((keydata = getKeyBytes(k)) == null) 
				throw new IllegalArgumentException ("invalid key => ["+k+"]");
			keybytes[i++] = keydata;
		}
		
		return new FutureByteArrayList(this.queueRequest(Command.SINTER, keybytes));
	}

//	@Override
	public Future<List<byte[]>> sunion(String set1, String... sets) {
		byte[] keydata = null;
		if((keydata = getKeyBytes(set1)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+set1+"]");

		byte[][] keybytes = new byte[1+sets.length][];
		int i=0; keybytes[i++] = keydata;
		for(String k : sets) {
			if((keydata = getKeyBytes(k)) == null) 
				throw new IllegalArgumentException ("invalid key => ["+k+"]");
			keybytes[i++] = keydata;
		}
		
		return new FutureByteArrayList(this.queueRequest(Command.SUNION, keybytes));
	}

//	@Override
	public Future<List<byte[]>> sdiff(String set1, String... sets) {
		byte[] keydata = null;
		if((keydata = getKeyBytes(set1)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+set1+"]");

		byte[][] keybytes = new byte[1+sets.length][];
		int i=0; keybytes[i++] = keydata;
		for(String k : sets) {
			if((keydata = getKeyBytes(k)) == null) 
				throw new IllegalArgumentException ("invalid key => ["+k+"]");
			keybytes[i++] = keydata;
		}
		return new FutureByteArrayList(this.queueRequest(Command.SDIFF, keybytes));
	}

//	@Override
	public FutureStatus sinterstore(String dest, String... sets) {
		byte[] keydata = null;
		if((keydata = getKeyBytes(dest)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+dest+"]");

		byte[][] setbytes = new byte[1+sets.length][];
		int i=0; 
		setbytes[i++] = keydata;
		byte[] setdata =null;
		for(String k : sets) {
			if((setdata = getKeyBytes(k)) == null) 
				throw new IllegalArgumentException ("invalid key => ["+k+"]");
			setbytes[i++] = setdata;
		}
		
		return new FutureStatus(this.queueRequest(Command.SINTERSTORE, setbytes));
	}

//	@Override
	public FutureStatus sunionstore(String dest, String... sets) {
		byte[] keydata = null;
		if((keydata = getKeyBytes(dest)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+dest+"]");

		byte[][] setbytes = new byte[1+sets.length][];
		int i=0; 
		setbytes[i++] = keydata;
		byte[] setdata =null;
		for(String k : sets) {
			if((setdata = getKeyBytes(k)) == null) 
				throw new IllegalArgumentException ("invalid key => ["+k+"]");
			setbytes[i++] = setdata;
		}
		
		return new FutureStatus(this.queueRequest(Command.SUNIONSTORE, setbytes));
	}

//	@Override
	public FutureStatus sdiffstore(String dest, String... sets) {
		byte[] keydata = null;
		if((keydata = getKeyBytes(dest)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+dest+"]");

		byte[][] setbytes = new byte[1+sets.length][];
		int i=0; 
		setbytes[i++] = keydata;
		byte[] setdata =null;
		for(String k : sets) {
			if((setdata = getKeyBytes(k)) == null) 
				throw new IllegalArgumentException ("invalid key => ["+k+"]");
			setbytes[i++] = setdata;
		}
		
		return new FutureStatus(this.queueRequest(Command.SDIFFSTORE, setbytes));
	}

//	@Override
	public Future<Long> del(String ... keys) {
		if(null == keys || keys.length == 0) throw new IllegalArgumentException("no keys specified");
		byte[] keydata = null;
		byte[][] keybytes = new byte[keys.length][];
		int i=0;
		for(String k : keys) {
			if((keydata = getKeyBytes(k)) == null) 
				throw new IllegalArgumentException ("invalid key => ["+k+"] @ index: " + i);
			
			keybytes[i++] = keydata;
		}

		Future<Response> futureResponse = this.queueRequest(Command.DEL, keybytes);
		return new FutureLong(futureResponse);
	}


//	@Override
	public Future<Boolean> exists(String key) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.EXISTS, keybytes);
		return new FutureBoolean(futureResponse);
	}


//	@Override
	public FutureStatus lpush(String key, byte[] value) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
		
		if(value == null) 
			throw new IllegalArgumentException ("null value");
		
		
		return new FutureStatus(this.queueRequest(Command.LPUSH, keybytes, value));
	}
//	@Override
	public FutureStatus lpush(String key, String value) {
		return lpush(key, DefaultCodec.encode(value));
	}
//	@Override
	public FutureStatus lpush(String key, Number value) {
		return lpush(key, String.valueOf(value).getBytes());
	}
//	@Override
	public <T extends Serializable> FutureStatus lpush (String key, T value)
	{
		return lpush(key, DefaultCodec.encode(value));
	}
	


//	@Override
	public Future<Long> lrem(String key, byte[] value, int count) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] countBytes = Convert.toBytes(count);

		Future<Response> futureResponse = this.queueRequest(Command.LREM, keybytes, value, countBytes);
		return new FutureLong(futureResponse);
	}
//	@Override
	public Future<Long> lrem (String listKey, String value, int count){
		return lrem (listKey, DefaultCodec.encode(value), count);
	}
//	@Override
	public Future<Long> lrem (String listKey, Number numberValue, int count) {
		return lrem (listKey, String.valueOf(numberValue).getBytes(), count);
	}
//	@Override
	public <T extends Serializable> 
	Future<Long> lrem (String listKey, T object, int count){
		return lrem (listKey, DefaultCodec.encode(object), count);
	}


//	@Override
	public FutureStatus lset(String key, long index, byte[] value) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] indexBytes = Convert.toBytes(index);
		return new FutureStatus(this.queueRequest(Command.LSET, keybytes, indexBytes, value));
	}
//	@Override
	public FutureStatus lset (String key, long index, String value) {
		return lset (key, index, DefaultCodec.encode(value));
	}
//	@Override
	public FutureStatus lset (String key, long index, Number numberValue){
		return lset (key, index, String.valueOf(numberValue).getBytes());
	}
//	@Override
	public <T extends Serializable> FutureStatus lset (String key, long index, T object){
		return lset (key, index, DefaultCodec.encode(object));
	}

//	@Override
	public Future<Boolean> move(String key, int dbIndex) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
		
		Future<Response> futureResponse = this.queueRequest(Command.MOVE, keybytes, Convert.toBytes(dbIndex));
		return new FutureBoolean(futureResponse);
	}


//	@Override
	public Future<Boolean> srem(String key, byte[] member) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.SREM, keybytes, member);
		return new FutureBoolean(futureResponse);
	}
//	@Override
	public Future<Boolean> srem (String key, String value) {
		return srem (key, DefaultCodec.encode(value));
	}
//	@Override
	public Future<Boolean> srem (String key, Number value) {
		return srem (key, String.valueOf(value).getBytes());
	}
//	@Override
	public <T extends Serializable> Future<Boolean> srem (String key, T value)
	{
		return srem (key, DefaultCodec.encode(value));
	}

//	@Override
	public Future<Boolean> zrem(String key, byte[] member) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.ZREM, keybytes, member);
		return new FutureBoolean(futureResponse);
	}
//	@Override
	public Future<Boolean> zrem (String key, String value) {
		return zrem (key, DefaultCodec.encode(value));
	}
//	@Override
	public Future<Boolean> zrem (String key, Number value) {
		return zrem (key, String.valueOf(value).getBytes());
	}
//	@Override
	public <T extends Serializable> Future<Boolean> zrem (String key, T value)
	{
		return zrem (key, DefaultCodec.encode(value));
	}


//	@Override
	public Future<Double> zscore(String key, byte[] member) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.ZSCORE, keybytes, member);
		return new FutureDouble(futureResponse);
	}
//	@Override
	public Future<Double> zscore (String key, String value) {
		return zscore (key, DefaultCodec.encode(value));
	}
//	@Override
	public Future<Double> zscore (String key, Number value) {
		return zscore (key, String.valueOf(value).getBytes());
	}
//	@Override
	public <T extends Serializable> Future<Double> zscore (String key, T value)
	{
		return zscore (key, DefaultCodec.encode(value));
	}

//	@Override
	public Future<Long> zrank(String key, byte[] member) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.ZRANK, keybytes, member);
		return new FutureLong(futureResponse);
	}
//	@Override
	public Future<Long> zrank (String key, String value) {
		return zrank (key, DefaultCodec.encode(value));
	}
//	@Override
	public Future<Long> zrank (String key, Number value) {
		return zrank (key, String.valueOf(value).getBytes());
	}
//	@Override
	public <T extends Serializable> Future<Long> zrank (String key, T value)
	{
		return zrank (key, DefaultCodec.encode(value));
	}

//	@Override
	public Future<Long> zrevrank(String key, byte[] member) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.ZREVRANK, keybytes, member);
		return new FutureLong(futureResponse);
	}
//	@Override
	public Future<Long> zrevrank (String key, String value) {
		return zrevrank (key, DefaultCodec.encode(value));
	}
//	@Override
	public Future<Long> zrevrank (String key, Number value) {
		return zrevrank (key, String.valueOf(value).getBytes());
	}
//	@Override
	public <T extends Serializable> Future<Long> zrevrank (String key, T value)
	{
		return zrevrank (key, DefaultCodec.encode(value));
	}


//	@Override
	public FutureStatus ltrim(String key, long keepFrom, long keepTo) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(keepFrom);
		byte[] toBytes = Convert.toBytes(keepTo);
		return new FutureStatus(this.queueRequest(Command.LTRIM, keybytes, fromBytes, toBytes));
	}

//	@Override
	public Future<Boolean> expire(String key, int ttlseconds) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] ttlbytes = Convert.toBytes(ttlseconds);
		
		Future<Response> futureResponse = this.queueRequest(Command.EXPIRE, keybytes, ttlbytes);
		return new FutureBoolean(futureResponse);
	}

//	@Override
	public Future<Boolean> expireat(String key, long epochtime) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		long expiretime = TimeUnit.SECONDS.convert(epochtime, TimeUnit.MILLISECONDS);
		byte[] expiretimeBytes = Convert.toBytes(expiretime);
		
		Future<Response> futureResponse = this.queueRequest(Command.EXPIREAT, keybytes, expiretimeBytes);
		return new FutureBoolean(futureResponse);
	}

//	@Override
	public Future<Long> ttl (String key) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.TTL, keybytes);
		return new FutureLong(futureResponse);
	}

	// TODO: integrate using KeyCodec and a CodecManager at client spec and init time.
	// TODO: (implied) ClientSpec (impls. ConnectionSpec)
	// this isn't cooked yet -- lets think more about the implications...
	// 
	static final private Map<String, byte[]>	keyByteCache = new ConcurrentHashMap<String, byte[]>();
	public static final boolean	CacheKeys	= false;
	
	private byte[] getKeyBytes(String key) throws IllegalArgumentException {
		if(null == key) throw new IllegalArgumentException("key is null");
		byte[] bytes = null;
		if(JRedisSupport.CacheKeys == true)
			bytes = keyByteCache.get(key);
		if(null == bytes) {
//			bytes = key.getBytes(DefaultCodec.SUPPORTED_CHARSET); // java 1.6
			try {
	            bytes = key.getBytes(DefaultCodec.SUPPORTED_CHARSET_NAME);
            }
            catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
            }
			for(byte b : bytes) {
				if (b == (byte)32 || b == (byte)10 || b == (byte)13)
					throw new IllegalArgumentException ("Key includes invalid byte value: " + (int)b);
			}
			
			if(JRedisSupport.CacheKeys == true)
				keyByteCache.put(key, bytes);
		}
		return bytes;
	}

	// ------------------------------------------------------------------------
	// Inner classes : support for Future<x> return types
	// ------------------------------------------------------------------------
	
	public static class FutureResultBase {
		final protected Future<Response> pendingRequest;
		protected FutureResultBase(Future<Response> pendingRequest){ this.pendingRequest = pendingRequest;}
		public boolean cancel (boolean mayInterruptIfRunning) {
	        return pendingRequest.cancel(mayInterruptIfRunning);
        }
        public boolean isCancelled () {
	        return pendingRequest.isCancelled();
        }
        public boolean isDone () {
	        return pendingRequest.isDone();
        }
	}
	public static class FutureStatus extends FutureResultBase implements Future<ResponseStatus>  {

        protected FutureStatus (Future<Response> pendingRequest) { super(pendingRequest); }
        public ResponseStatus get () throws InterruptedException, ExecutionException {
//        	StatusResponse statusResponse = (StatusResponse) pendingRequest.get();  // HANDLE VIRTUALS HERE
//        	return statusResponse.getStatus();
        	return pendingRequest.get().getStatus();
        }

        public ResponseStatus get (long timeout, TimeUnit unit)
        	throws InterruptedException, ExecutionException, TimeoutException 
        {
//        	StatusResponse statusResponse = (StatusResponse) pendingRequest.get(timeout, unit);
//        	return statusResponse.getStatus();
        	return pendingRequest.get(timeout, unit).getStatus();
        }
        
	}
	public static class FutureBoolean extends FutureResultBase implements Future<Boolean>{

        protected FutureBoolean (Future<Response> pendingRequest) { super(pendingRequest); }

        public Boolean get () throws InterruptedException, ExecutionException {
        	ValueResponse valResp = (ValueResponse) pendingRequest.get();
        	return valResp.getBooleanValue();
        }

        public Boolean get (long timeout, TimeUnit unit)
        	throws InterruptedException, ExecutionException, TimeoutException 
        {
        	ValueResponse valResp = (ValueResponse) pendingRequest.get(timeout, unit);
        	return valResp.getBooleanValue();
        }
	}
	public static class FutureString extends FutureResultBase implements Future<String>{

        protected FutureString (Future<Response> pendingRequest) { super(pendingRequest); }

        public String get () throws InterruptedException, ExecutionException {
        	ValueResponse valResp = (ValueResponse) pendingRequest.get();
        	return valResp.getStringValue();
        }

        public String get (long timeout, TimeUnit unit)
        	throws InterruptedException, ExecutionException, TimeoutException 
        {
        	ValueResponse valResp = (ValueResponse) pendingRequest.get(timeout, unit);
        	return valResp.getStringValue();
        }
	}
	public static class FutureRedisType extends FutureResultBase implements Future<RedisType>{

        protected FutureRedisType (Future<Response> pendingRequest) { super(pendingRequest); }

        private final RedisType getRedisType(ValueResponse resp){
			String stringValue = resp.getStringValue();
			return RedisType.valueOf(stringValue);
        }
        public RedisType get () throws InterruptedException, ExecutionException {
        	ValueResponse valResp = (ValueResponse) pendingRequest.get();
        	return getRedisType(valResp);
        }

        public RedisType get (long timeout, TimeUnit unit)
        	throws InterruptedException, ExecutionException, TimeoutException 
        {
        	ValueResponse valResp = (ValueResponse) pendingRequest.get(timeout, unit);
        	return getRedisType(valResp);
        }
	}
	public static class FutureLong extends FutureResultBase implements Future<Long>{

        protected FutureLong (Future<Response> pendingRequest) { super(pendingRequest); }

        public Long get () throws InterruptedException, ExecutionException {
        	ValueResponse valResp = (ValueResponse) pendingRequest.get();
        	return valResp.getLongValue();
        }

        public Long get (long timeout, TimeUnit unit)
        	throws InterruptedException, ExecutionException, TimeoutException 
        {
        	ValueResponse valResp = (ValueResponse) pendingRequest.get(timeout, unit);
        	return valResp.getLongValue();
        }
	}
	public static class FutureDouble extends FutureResultBase implements Future<Double>{

        protected FutureDouble (Future<Response> pendingRequest) { super(pendingRequest); }

        public Double get () throws InterruptedException, ExecutionException {
        	BulkResponse bulkResp = (BulkResponse) pendingRequest.get();
        	if(bulkResp.getBulkData() != null)
        		return Convert.toDouble(bulkResp.getBulkData());
        	return null;
        }

        public Double get (long timeout, TimeUnit unit)
        	throws InterruptedException, ExecutionException, TimeoutException 
        {
        	BulkResponse bulkResp = (BulkResponse) pendingRequest.get(timeout, unit);
        	if(bulkResp.getBulkData() != null)
        		return Convert.toDouble(bulkResp.getBulkData());
        	return null;
        }
	}
	public static class FutureByteArray extends FutureResultBase implements Future<byte[]>{

        protected FutureByteArray (Future<Response> pendingRequest) { super(pendingRequest); }

        public byte[] get () throws InterruptedException, ExecutionException {
        	BulkResponse resp = (BulkResponse) pendingRequest.get();
        	return resp.getBulkData();
        }

        public byte[] get (long timeout, TimeUnit unit)
        	throws InterruptedException, ExecutionException, TimeoutException 
        {
        	BulkResponse resp = (BulkResponse) pendingRequest.get(timeout, unit);
        	return resp.getBulkData();
        }
	}
	public static class FutureByteArrayList extends FutureResultBase implements Future<List<byte[]>>{

        protected FutureByteArrayList (Future<Response> pendingRequest) { super(pendingRequest); }

        public List<byte[]> get () throws InterruptedException, ExecutionException {
        	MultiBulkResponse resp = (MultiBulkResponse) pendingRequest.get();
        	return resp.getMultiBulkData();
        }

        public List<byte[]> get (long timeout, TimeUnit unit)
        	throws InterruptedException, ExecutionException, TimeoutException 
        {
        	MultiBulkResponse resp = (MultiBulkResponse) pendingRequest.get(timeout, unit);
        	return resp.getMultiBulkData();
        }
	}

	public static class FutureDataDictionary extends FutureResultBase implements Future<Map<String, byte[]>>{

        protected FutureDataDictionary (Future<Response> pendingRequest) { super(pendingRequest); }

        public Map<String, byte[]> get () throws InterruptedException, ExecutionException {
        	MultiBulkResponse resp = (MultiBulkResponse) pendingRequest.get();
        	return convert(resp.getMultiBulkData());
        }

        public Map<String, byte[]> get (long timeout, TimeUnit unit)
        	throws InterruptedException, ExecutionException, TimeoutException 
        {
        	MultiBulkResponse resp = (MultiBulkResponse) pendingRequest.get(timeout, unit);
        	return convert(resp.getMultiBulkData());
        }
        private static final Map<String, byte[]> convert (List<byte[]> bulkdata) {
        	Map<String, byte[]> map = null;
        	if(null != bulkdata) {
        		map = new HashMap<String, byte[]>(bulkdata.size()/2);
        		for(int i=0; i<bulkdata.size(); i+=2){
        			map.put(DefaultCodec.toStr(bulkdata.get(i)), bulkdata.get(i+1));
        		}
        	}
        	return map;
        }
	}

	public static class FutureKeyList extends FutureResultBase implements Future<List<String>>{

        protected FutureKeyList (Future<Response> pendingRequest) { super(pendingRequest); }

//        private List<String>  getResultList (BulkResponse resp) {
//    		StringTokenizer tokenizer = new StringTokenizer(new String(resp.getBulkData()), " ");
//    		List<String>  list = new ArrayList <String>(12);
//    		while (tokenizer.hasMoreTokens()){
//    			list.add(tokenizer.nextToken());
//    		}
//    		return list;
//        }
        public List<String> get () throws InterruptedException, ExecutionException {

        	MultiBulkResponse resp = (MultiBulkResponse) pendingRequest.get();
        	List<byte[]> multibulkdata = resp.getMultiBulkData();
        	List<String> list = null;
        	if(null != multibulkdata)
        		list = DefaultCodec.toStr(multibulkdata);
        	return list;
        }

        public List<String> get (long timeout, TimeUnit unit)
        	throws InterruptedException, ExecutionException, TimeoutException 
        {
            	MultiBulkResponse resp = (MultiBulkResponse) pendingRequest.get(timeout, unit);
            	List<byte[]> multibulkdata = resp.getMultiBulkData();
            	List<String> list = null;
            	if(null != multibulkdata)
            		list = DefaultCodec.toStr(multibulkdata);
            	return list;
        }
	}
	public static class FutureInfo extends FutureResultBase implements Future<Map<String, String>>{

        protected FutureInfo (Future<Response> pendingRequest) { super(pendingRequest); }

        private Map<String, String>  getResultMap (BulkResponse resp) {
    		StringTokenizer tokenizer = new StringTokenizer(new String(resp.getBulkData()), "\r\n");
    		Map<String, String>  infomap = new HashMap<String, String>(12);
    		while (tokenizer.hasMoreTokens()){
    			String info = tokenizer.nextToken();
    			int c = info.indexOf(':');
    			String key =info.substring(0, c);
    			String value = info.substring(c+1);
    			infomap.put(key, value);
    		}
    		return infomap;
        }
        public Map<String, String> get () throws InterruptedException, ExecutionException {
        	BulkResponse resp = (BulkResponse) pendingRequest.get();
        	return getResultMap(resp);
        }

        public Map<String, String> get (long timeout, TimeUnit unit)
        	throws InterruptedException, ExecutionException, TimeoutException 
        {
        	BulkResponse resp = (BulkResponse) pendingRequest.get(timeout, unit);
        	return getResultMap(resp);
        }
	}
	public static class FutureObjectInfo extends FutureResultBase implements Future<ObjectInfo>{

        protected FutureObjectInfo (Future<Response> pendingRequest) { super(pendingRequest); }

        private final ObjectInfo getObjectInfo(ValueResponse resp){
			String stringValue = resp.getStringValue();
			return ObjectInfo.valueOf(stringValue);
        }
        public ObjectInfo get () throws InterruptedException, ExecutionException {
        	ValueResponse valResp = (ValueResponse) pendingRequest.get();
        	return getObjectInfo(valResp);
        }

        public ObjectInfo get (long timeout, TimeUnit unit)
        	throws InterruptedException, ExecutionException, TimeoutException 
        {
        	ValueResponse valResp = (ValueResponse) pendingRequest.get(timeout, unit);
        	return getObjectInfo(valResp);
        }
	}
	public static class FutureZSetList extends FutureResultBase implements Future<List<ZSetEntry>>{

        protected FutureZSetList (Future<Response> pendingRequest) { super(pendingRequest); }

        public List<ZSetEntry> get () throws InterruptedException, ExecutionException {
        	MultiBulkResponse resp = (MultiBulkResponse) pendingRequest.get();
        	return convert(resp.getMultiBulkData());
        }

        public List<ZSetEntry> get (long timeout, TimeUnit unit)
        	throws InterruptedException, ExecutionException, TimeoutException 
        {
        	MultiBulkResponse resp = (MultiBulkResponse) pendingRequest.get(timeout, unit);
        	return convert(resp.getMultiBulkData());
        }
        private static final List<ZSetEntry> convert (List<byte[]> mbulkdata) {
        	List<ZSetEntry> zset = null;
        	if(mbulkdata.size() > 0){
        		zset = new ArrayList<ZSetEntry>(mbulkdata.size()/2);
        		for(int i=0; i<mbulkdata.size(); i+=2){
        			zset.add(new ZSetEntryImpl(mbulkdata.get(i),  mbulkdata.get(i+1)));
        		}
        	}
        	return zset;
        }
	}
	// ------------------------------------------------------------------------
	// Diagnostics commands
	// ------------------------------------------------------------------------
	
	/**
	 * @Redis ECHO
	 * @param msg
	 * @return
	 */
	public Future<byte[]> echo (byte[] msg) {
		if(msg == null) 
			throw new IllegalArgumentException ("invalid value for echo => ["+msg+"]");

		Future<Response> futureResponse = this.queueRequest(Command.ECHO, msg);
		return new FutureByteArray(futureResponse);
		
	}
	public Future<byte[]> echo (String msg) {
		return echo(DefaultCodec.encode(msg));
	}
	public Future<byte[]> echo (Number msg) {
		return echo(String.valueOf(msg).getBytes());
	}
	public <T extends Serializable> 
		Future<byte[]> echo (T msg) {
			return echo (DefaultCodec.encode(msg));
	}
}
