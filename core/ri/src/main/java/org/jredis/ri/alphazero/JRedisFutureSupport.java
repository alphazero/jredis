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

package org.jredis.ri.alphazero;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
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
import org.jredis.ri.alphazero.support.Convert;
import org.jredis.ri.alphazero.support.DefaultCodec;
import org.jredis.ri.alphazero.support.SortSupport;

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


	@Override
	public <K extends Object> FutureStatus bgsave() {
		return new FutureStatus(this.queueRequest(Command.BGSAVE));
	}
	
	@Override
	public <K extends Object> FutureString bgrewriteaof() {
		Future<Response> futureResponse = this.queueRequest(Command.BGREWRITEAOF);
		return new FutureString(futureResponse);
	}

	@Override
	public <K extends Object> FutureStatus ping() {
		return new FutureStatus(this.queueRequest(Command.PING));
	}

	@Override
	public <K extends Object> FutureStatus flushall() {
		return new FutureStatus(this.queueRequest(Command.FLUSHALL));
	}
	@Override
	public <K extends Object> FutureStatus flushdb() {
		return new FutureStatus(this.queueRequest(Command.FLUSHDB));
	}

//	public <K extends Object> FutureStatus select(int index) {
//		this.queueRequest(Command.SELECT, Convert.toBytes(index));
//		return this;
//	}
	@Override
	public <K extends Object> Future<ResponseStatus>  slaveof(String host, int port) {
		byte[] hostbytes = null;
		if((hostbytes = JRedisSupport.getKeyBytes(host)) == null)
			throw new IllegalArgumentException ("invalid host => ["+host+"]");

		byte[] portbytes = null;
		if((portbytes = Convert.toBytes(port)) == null)
			throw new IllegalArgumentException ("invalid port => ["+port+"]");

		return new FutureStatus(this.queueRequest(Command.SLAVEOF, hostbytes, portbytes));
	}
	public <K extends Object> Future<ResponseStatus>  slaveofnone() {
		return new FutureStatus(this.queueRequest(Command.SLAVEOF, "no".getBytes(), "one".getBytes()));
	}
	
	@Override
	public <K extends Object> FutureStatus rename(K oldkey, K newkey) {
		byte[] oldkeydata = null;
		if((oldkeydata = JRedisSupport.getKeyBytes(oldkey)) == null)
			throw new IllegalArgumentException ("invalid key => ["+oldkey+"]");

		byte[] newkeydata = null;
		if((newkeydata = JRedisSupport.getKeyBytes(newkey)) == null)
			throw new IllegalArgumentException ("invalid key => ["+newkey+"]");

		return new FutureStatus(this.queueRequest(Command.RENAME, oldkeydata, newkeydata));
	}
	
	@Override
	public <K extends Object> Future<Boolean> renamenx(K oldkey, K newkey){
		byte[] oldkeydata = null;
		if((oldkeydata = JRedisSupport.getKeyBytes(oldkey)) == null)
			throw new IllegalArgumentException ("invalid key => ["+oldkey+"]");

		byte[] newkeydata = null;
		if((newkeydata = JRedisSupport.getKeyBytes(newkey)) == null)
			throw new IllegalArgumentException ("invalid key => ["+newkey+"]");

		Future<Response> futureResponse = this.queueRequest(Command.RENAMENX, oldkeydata, newkeydata);
		return new FutureBoolean(futureResponse);
	}
	public <K extends Object> FutureLong rpush(K key, byte[] value)  {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
		
		if(value == null)
			throw new IllegalArgumentException ("null value");
		
		return new FutureLong(this.queueRequest(Command.RPUSH, keybytes, value));
	}

	public <K extends Object> FutureLong rpushx(K key, byte[] value) {
		byte[] keybytes = null;
		if ((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		if (value == null)
			throw new IllegalArgumentException ("null value");

		return new FutureLong(this.queueRequest(Command.RPUSHX, keybytes, value));
	}

	public <K extends Object> FutureLong lpushx(K key, byte[] value) {
		byte[] keybytes = null;
		if ((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		if (value == null)
			throw new IllegalArgumentException ("null value");

		return new FutureLong(this.queueRequest(Command.LPUSHX, keybytes, value));
	}

	public <K extends Object> FutureLong linsert(K key, boolean after, byte[] oldvalue, byte[] newvalue) {
		byte[] keybytes = null;
		if ((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
    byte[][] bulk = new byte[4][];
    bulk[0] = keybytes;
    bulk[1] = (after ? "AFTER" : "BEFORE").getBytes();
    bulk[2] = oldvalue;
    bulk[3] = newvalue;
		return new FutureLong(this.queueRequest(Command.LINSERT, bulk));
	}

	public <K extends Object> FutureLong linsertAfter(K key, byte[] oldvalue, byte[] newvalue) {
    return linsert(key, true, oldvalue, newvalue);
  }

	public <K extends Object> FutureLong linsertBefore(K key, byte[] oldvalue, byte[] newvalue) {
    return linsert(key, false, oldvalue, newvalue);
  }

	@Override
	public <K extends Object> FutureByteArray rpoplpush (String srcList, String destList)  {
		byte[] srckeybytes = null;
		if((srckeybytes = JRedisSupport.getKeyBytes(srcList)) == null)
			throw new IllegalArgumentException ("invalid src key => ["+srcList+"]");
		byte[] destkeybytes = null;
		if((destkeybytes = JRedisSupport.getKeyBytes(destList)) == null)
			throw new IllegalArgumentException ("invalid dest key => ["+destList+"]");
		
		Future<Response> futureResponse = this.queueRequest(Command.RPOPLPUSH, srckeybytes, destkeybytes);
		return new FutureByteArray(futureResponse);
	}
	@Override
	public <K extends Object> FutureLong rpush(K key, String value) {
//		rpush(key, DefaultCodec.encode(value));
		return rpush(key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> FutureLong rpush(K key, Number value) {
		return rpush(key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> FutureLong rpush (K key, T value)
	{
		return rpush(key, DefaultCodec.encode(value));
	}

	@Override
	public <K extends Object> Future<Boolean> sadd(K key, byte[] member)
	{
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.SADD, keybytes, member);
		return new FutureBoolean(futureResponse);
	}
	@Override
	public <K extends Object> Future<Boolean> sadd (K key, String value) {
		return sadd (key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> Future<Boolean> sadd (K key, Number value) {
		return sadd (key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> Future<Boolean> sadd (K key, T value)
	{
		return sadd (key, DefaultCodec.encode(value));
	}

	@Override
	public <K extends Object> Future<Boolean> zadd(K key, double score, byte[] member)
	{
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.ZADD, keybytes,  Convert.toBytes(score), member);
		return new FutureBoolean(futureResponse);
	}
	@Override
	public <K extends Object> Future<Boolean> zadd (K key, double score, String value) {
		return zadd (key, score, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> Future<Boolean> zadd (K key, double score, Number value) {
		return zadd (key, score, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> Future<Boolean> zadd (K key, double score, T value)
	{
		return zadd (key, score, DefaultCodec.encode(value));
	}

	@Override
	public <K extends Object> Future<Double> zincrby(K key, double score, byte[] member)
	{
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.ZINCRBY, keybytes,  Convert.toBytes(score), member);
		return new FutureDouble(futureResponse);
	}
	@Override
	public <K extends Object> Future<Double> zincrby (K key, double score, String value) {
		return zincrby (key, score, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> Future<Double> zincrby (K key, double score, Number value) {
		return zincrby (key, score, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> Future<Double> zincrby (K key, double score, T value)
	{
		return zincrby (key, score, DefaultCodec.encode(value));
	}

	@Override
	public <K extends Object> FutureStatus save()
	{
		return new FutureStatus(this.queueRequest(Command.SAVE));
	}
	
	// -------- set

	@Override
	public <K> Future<Boolean> setbit(K key, int offset, boolean value) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.SETBIT, keybytes,  
				Convert.toBytes(offset), Convert.toBytes(value ? 1 : 0));

		return new FutureBit(futureResponse);
		
	}


	@Override
	public <K> Future<Boolean> getbit(K key, int offset)  {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.GETBIT, keybytes,  
				Convert.toBytes(offset));

		return new FutureBit(futureResponse);
	}
	
	@Override
	public <K extends Object> FutureStatus set(K key, byte[] value) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		return new FutureStatus(this.queueRequest(Command.SET, keybytes, value));
	}
	@Override
	public <K extends Object> FutureStatus set(K key, String value) {
		return set(key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> FutureStatus set(K key, Number value) {
		return set(key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> FutureStatus set (K key, T value)
	{
		return set(key, DefaultCodec.encode(value));
	}
	
	@Override
	public <K extends Object> Future<byte[]> getset(K key, byte[] value) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.GETSET, keybytes, value);
		return new FutureByteArray(futureResponse);
	}
	@Override
	public <K extends Object> Future<byte[]> getset(K key, String value) {
		return getset(key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> Future<byte[]> getset(K key, Number value) {
		return getset(key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable>
	Future<byte[]> getset (K key, T value)
	{
		return getset(key, DefaultCodec.encode(value));
	}
	
	@Override
	public <K extends Object> Future<Boolean> setnx(K key, byte[] value){
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.SETNX, keybytes, value);
		return new FutureBoolean(futureResponse);
	}
	@Override
	public <K extends Object> Future<Boolean> setnx(K key, String value) {
		return setnx(key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> Future<Boolean> setnx(K key, Number value) {
		return setnx(key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> Future<Boolean> setnx (K key, T value) {
		return setnx(key, DefaultCodec.encode(value));
	}

	
	@Override
	public <K extends Object> Future<Long> append (K key, byte[] value){
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.APPEND, keybytes, value);
		return new FutureLong(futureResponse);
	}
	@Override
	public <K extends Object> Future<Long> append(K key, String value) {
		return append(key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> Future<Long> append(K key, Number value) {
		return append(key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> Future<Long> append (K key, T value) {
		return append(key, DefaultCodec.encode(value));
	}

	@Override
	public <K extends Object> Future<Boolean> sismember(K key, byte[] member) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.SISMEMBER, keybytes, member);
		return new FutureBoolean(futureResponse);
	}

	@Override
	public <K extends Object> Future<Boolean> sismember(K key, String value) {
		return sismember(key, DefaultCodec.encode(value));
	}

	@Override
	public <K extends Object> Future<Boolean> sismember(K key, Number numberValue) {
		return sismember (key, String.valueOf(numberValue).getBytes());
	}

	@Override
	public <K extends Object, T extends Serializable> Future<Boolean> sismember(K key, T object) {
		return sismember(key, DefaultCodec.encode(object));
	}

	public <K extends Object> Future<Boolean> smove (K srcKey, K destKey, byte[] member) {
		byte[] srcKeyBytes = null;
		if((srcKeyBytes = JRedisSupport.getKeyBytes(srcKey)) == null)
			throw new IllegalArgumentException ("invalid key => ["+srcKey+"]");

		byte[] destKeyBytes = null;
		if((destKeyBytes = JRedisSupport.getKeyBytes(destKey)) == null)
			throw new IllegalArgumentException ("invalid key => ["+destKey+"]");

		Future<Response> futureResponse = this.queueRequest(Command.SMOVE, srcKeyBytes, destKeyBytes, member);
		return new FutureBoolean(futureResponse);
	}
	public <K extends Object> Future<Boolean> smove (K srcKey, K destKey, String stringValue) {
		return smove (srcKey, destKey, DefaultCodec.encode(stringValue));
	}
	public <K extends Object> Future<Boolean> smove (K srcKey, K destKey, Number numberValue) {
		return smove (srcKey, destKey, String.valueOf(numberValue).getBytes());
	}
	public <K extends Object, T extends Serializable>
		   Future<Boolean> smove (K srcKey, K destKey, T object) {
		return smove (srcKey, destKey, DefaultCodec.encode(object));
	}
		
	// ------------------------------------------------------------------------
	// Commands operating on hashes
	// ------------------------------------------------------------------------
	
	public <K extends Object> Future<Boolean> hset(K key, K field, byte[] value) {
		byte[] keyBytes = null;
		if((keyBytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] entryBytes = null;
		if((entryBytes = JRedisSupport.getKeyBytes(field)) == null)
			throw new IllegalArgumentException ("invalid field => ["+field+"]");

		Future<Response> futureResponse = this.queueRequest(Command.HSET, keyBytes, entryBytes, value);
		return new FutureBoolean(futureResponse);
	}
	public <K extends Object> Future<Boolean> hset(K key, K field, String stringValue) {
		return hset (key, field, DefaultCodec.encode(stringValue));
	}
	
	public <K extends Object> Future<Long> hincrby(K key, K field, long increment) {
		byte[] keyBytes = null;
		if((keyBytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] entryBytes = null;
		if((entryBytes = JRedisSupport.getKeyBytes(field)) == null)
			throw new IllegalArgumentException ("invalid field => ["+field+"]");

		Future<Response> futureResponse = this.queueRequest(Command.HINCRBY, keyBytes, entryBytes, Convert.toBytes(increment));
		return new FutureLong(futureResponse);
	}
	
	public <K extends Object> Future<Boolean> hset(K key, K field, Number numberValue) {
		return hset (key, field, String.valueOf(numberValue).getBytes());
	}
	public <K extends Object, T extends Serializable>
		Future<Boolean> hset(K key, K field, T object) {
		return hset (key, field, DefaultCodec.encode(object));
	}
	
	public <K extends Object> Future<byte[]> hget(K key, K entry) {
		byte[] keyBytes = null;
		if((keyBytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] entryBytes = null;
		if((entryBytes = JRedisSupport.getKeyBytes(entry)) == null)
			throw new IllegalArgumentException ("invalid field => ["+entry+"]");
		
		Future<Response> futureResponse = this.queueRequest(Command.HGET, keyBytes, entryBytes);
		return new FutureByteArray(futureResponse);
	}
	
	
	public <K extends Object> Future<Boolean> hexists(K key, K entry) {
		byte[] keyBytes = null;
		if((keyBytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] entryBytes = null;
		if((entryBytes = JRedisSupport.getKeyBytes(entry)) == null)
			throw new IllegalArgumentException ("invalid field => ["+entry+"]");
		
		Future<Response> futureResponse = this.queueRequest(Command.HEXISTS, keyBytes, entryBytes);
		return new FutureBoolean(futureResponse);
	}
	
	public <K extends Object> Future<Boolean> hdel(K key, K entry) {
		byte[] keyBytes = null;
		if((keyBytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] entryBytes = null;
		if((entryBytes = JRedisSupport.getKeyBytes(entry)) == null)
			throw new IllegalArgumentException ("invalid field => ["+entry+"]");
		
		Future<Response> futureResponse = this.queueRequest(Command.HDEL, keyBytes, entryBytes);
		return new FutureBoolean(futureResponse);
	}
	
	public <K extends Object> Future<Long> hlen(K key) {
		byte[] keyBytes = null;
		if((keyBytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		
		Future<Response> futureResponse = this.queueRequest(Command.HLEN, keyBytes);
		return new FutureLong(futureResponse);
	}
	
	public <K extends Object> Future<List<byte[]>> hkeys(K key) {
		byte[] keyBytes = null;
		if((keyBytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.HKEYS, keyBytes);
		return new FutureKeyList (futureResponse);
	}
	
	@Override
	public <K extends Object> Future<List<byte[]>> hvals(K key) {
		byte[] keyBytes = null;
		if((keyBytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.HKEYS, keyBytes);
		return new FutureByteArrayList (futureResponse);
	}
	@Override
	public <K extends Object> Future<Map<byte[], byte[]>> hgetall(K key) {
		byte[] keyBytes = null;
		if((keyBytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.HGETALL, keyBytes);
		return new FutureDataDictionary (futureResponse);
	}
	
	
	/* ------------------------------- commands returning int value --------- */

	@Override
	public <K extends Object> Future<Long> incr(K key) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.INCR, keybytes);
		return new FutureLong (futureResponse);
	}

	@Override
	public <K extends Object> Future<Long> incrby(K key, int delta) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.INCRBY, keybytes, Convert.toBytes(delta));
		return new FutureLong (futureResponse);
	}

	@Override
	public <K extends Object> Future<Long> decr(K key) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.DECR, keybytes);
		return new FutureLong (futureResponse);
	}

	@Override
	public <K extends Object> Future<Long> decrby(K key, int delta) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.DECRBY, keybytes, Convert.toBytes(delta));
		return new FutureLong (futureResponse);
	}

	@Override
	public <K extends Object> Future<Long> llen(K key) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.LLEN, keybytes);
		return new FutureLong (futureResponse);
	}

	@Override
	public <K extends Object> Future<Long> scard(K key) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.SCARD, keybytes);
		return new FutureLong (futureResponse);
	}
	
	@Override
	public <K extends Object> Future<Long> zcard(K key) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.ZCARD, keybytes);
		return new FutureLong (futureResponse);
	}
	
	public <K extends Object> Future<byte[]> srandmember (K key) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.SRANDMEMBER, keybytes);
		return new FutureByteArray (futureResponse);
	}

	public <K extends Object> Future<byte[]> spop (K key) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.SPOP, keybytes);
		return new FutureByteArray (futureResponse);
	}

	/* ------------------------------- commands returning long value --------- */

	@Override
	public <K extends Object> Future<Long> dbsize() {
		Future<Response> futureResponse = this.queueRequest(Command.DBSIZE);
		return new FutureLong (futureResponse);
	}
	@Override
	public <K extends Object> Future<Long> lastsave() {
		Future<Response> futureResponse = this.queueRequest(Command.LASTSAVE);
		return new FutureLong (futureResponse);
	}

	/* ------------------------------- commands returning byte[] --------- */

	@Override
	public <K extends Object> Future<byte[]> get(K key) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.GET, keybytes);
		return new FutureByteArray(futureResponse);
	}

	@Override
	public <K extends Object> Future<byte[]> lindex(K key, long index) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.LINDEX, keybytes, Convert.toBytes(index));
		return new FutureByteArray(futureResponse);
	}
	@Override
	public <K extends Object> Future<byte[]> lpop(K key) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.LPOP, keybytes);
		return new FutureByteArray(futureResponse);
	}

	@Override
	public <K extends Object> Future<byte[]> rpop(K key) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.RPOP, keybytes);
		return new FutureByteArray(futureResponse);
	}


	/* ------------------------------- commands returning String--------- */

	@Override
	public <K extends Object> Future<byte[]> randomkey() {
		Future<Response> futureResponse = this.queueRequest(Command.RANDOMKEY);
		return new FutureByteArray(futureResponse);
	}
	@Override
	public <K extends Object> Future<RedisType> type(K key) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		return new FutureRedisType(this.queueRequest(Command.TYPE, keybytes));
	}

	/* ------------------------------- commands returning Maps --------- */

	@Override
	public <K extends Object> Future<Map<String, String>> info() {
		return new FutureInfo(this.queueRequest(Command.INFO));
	}

	@Override
	public <K extends Object> Future<ObjectInfo> debug (K key) {
		byte[] keybytes = JRedisSupport.getKeyBytes(key);
//		if(key.length() == 0)
//			throw new IllegalArgumentException ("invalid zero length key => ["+key+"]");

		return new FutureObjectInfo (this.queueRequest(Command.DEBUG, "OBJECT".getBytes(), keybytes));
	}
	/* ------------------------------- commands returning Lists --------- */

	@Override
	public <K extends Object> Future<List<byte[]>> mget(String ... keys) {

		if(null == keys || keys.length == 0) throw new IllegalArgumentException("no keys specified");
		byte[] keydata = null;
		byte[][] keybytes = new byte[keys.length][];
		int i=0;
		for(String k : keys) {
			if((keydata = JRedisSupport.getKeyBytes(k)) == null)
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
	public <K extends Object> FutureStatus mset(Map<K, byte[]> keyValueMap){
//		KeyCodec codec = DefaultKeyCodec.provider();
		byte[][] mappings = new byte[keyValueMap.size()*2][];
		int i = 0;
		for (Entry<K, byte[]> e : keyValueMap.entrySet()){
			mappings[i++] = JRedisSupport.getKeyBytes(e.getKey());
			mappings[i++] = e.getValue();
		}
		return mset(mappings);
	}
	
	public <K extends Object> FutureStatus mset(KeyValueSet.ByteArrays<K> keyValueMap){
		return mset(keyValueMap.getMappings());
	}
	public <K extends Object> FutureStatus mset(KeyValueSet.Strings<K> keyValueMap){
		return mset(keyValueMap.getMappings());
	}

	public <K extends Object> FutureStatus mset(KeyValueSet.Numbers<K> keyValueMap){
		return mset(keyValueMap.getMappings());
	}

	public <K extends Object, T extends Serializable> FutureStatus mset(KeyValueSet.Objects<K, T> keyValueMap){
		return mset(keyValueMap.getMappings());
	}

	/* MSETNXs */
	private Future<Boolean> msetnx(byte[][] mappings){
		Future<Response> futureResponse = this.queueRequest(Command.MSETNX, mappings);
		return new FutureBoolean(futureResponse);
	}
	public <K extends Object> Future<Boolean> msetnx(Map<K, byte[]> keyValueMap){
//		KeyCodec codec = DefaultKeyCodec.provider();
		byte[][] mappings = new byte[keyValueMap.size()*2][];
		int i = 0;
		for (Entry<K, byte[]> e : keyValueMap.entrySet()){
			mappings[i++] = JRedisSupport.getKeyBytes(e.getKey());
			mappings[i++] = e.getValue();
		}
		return msetnx(mappings);
	}
	
	public <K extends Object> Future<Boolean> msetnx(KeyValueSet.ByteArrays<K> keyValueMap){
		return msetnx(keyValueMap.getMappings());
	}
	public <K extends Object> Future<Boolean> msetnx(KeyValueSet.Strings<K> keyValueMap){
		return msetnx(keyValueMap.getMappings());
	}

	public <K extends Object> Future<Boolean> msetnx(KeyValueSet.Numbers<K> keyValueMap){
		return msetnx(keyValueMap.getMappings());
	}

	public <K extends Object,T extends Serializable> Future<Boolean> msetnx(KeyValueSet.Objects<K, T> keyValueMap){
		return msetnx(keyValueMap.getMappings());
	}


	@Override
	public <K extends Object> Future<List<byte[]>> smembers(K key) {
		byte[] keydata = null;
		if((keydata = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("null key.");

		return new FutureByteArrayList(this.queueRequest(Command.SMEMBERS, keydata));
	}
	@Override
	public <K extends Object> Future<List<byte[]>> keys() {
		return this.keys("*");
	}

	@Override
	public <K extends Object> Future<List<byte[]>> keys(K pattern) {
		byte[] keydata = null;
		if((keydata = JRedisSupport.getKeyBytes(pattern)) == null)
			throw new IllegalArgumentException ("null key.");

		Future<Response> futureResponse = this.queueRequest(Command.KEYS, keydata);
		return new FutureKeyList(futureResponse);
	}

	public <K extends Object> Future<Long> keystolist(String pattern, String listname) {
		byte[] keydata = null;
		if((keydata = JRedisSupport.getKeyBytes(pattern)) == null)
			throw new IllegalArgumentException ("null key.");
		byte[] listnamedata = null;
		if((listnamedata = JRedisSupport.getKeyBytes(listname)) == null)
			throw new IllegalArgumentException ("null list name.");

		return new FutureLong(this.queueRequest(Command.KEYSTOLIST, keydata, listnamedata));
	}

	@Override
	public <K extends Object> Future<List<byte[]>> lrange(K key, long from, long to) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(from);
		byte[] toBytes = Convert.toBytes(to);

		return new FutureByteArrayList(this.queueRequest(Command.LRANGE, keybytes, fromBytes, toBytes));
	}

	@Override
	public <K extends Object> Future<byte[]> substr(K key, long from, long to) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(from);
		byte[] toBytes = Convert.toBytes(to);

		return new FutureByteArray(this.queueRequest(Command.SUBSTR, keybytes, fromBytes, toBytes));
	}

	@Override
	public <K extends Object> Future<List<byte[]>> zrange(K key, long from, long to) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(from);
		byte[] toBytes = Convert.toBytes(to);

		return new FutureByteArrayList(this.queueRequest(Command.ZRANGE, keybytes, fromBytes, toBytes));
	}

	@Override
	public <K extends Object> Future<List<byte[]>> zrangebyscore(K key, double minScore, double maxScore) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] minScoreBytes = Convert.toBytes(minScore);
		byte[] maxScoreBytes = Convert.toBytes(maxScore);

		return new FutureByteArrayList(this.queueRequest(Command.ZRANGEBYSCORE, keybytes, minScoreBytes, maxScoreBytes));
	}
	
	@Override
	public <K extends Object> Future<List<ZSetEntry>> zrangebyscoreSubset(K key, double minScore, double maxScore) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] minScoreBytes = Convert.toBytes(minScore);
		byte[] maxScoreBytes = Convert.toBytes(maxScore);

		return new FutureZSetList(this.queueRequest(Command.ZRANGEBYSCORE$OPTS, keybytes, minScoreBytes, maxScoreBytes, Command.Option.WITHSCORES.bytes));
	}

	@Override
	public <K extends Object> Future<Long> zremrangebyscore(K key, double minScore, double maxScore) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] minScoreBytes = Convert.toBytes(minScore);
		byte[] maxScoreBytes = Convert.toBytes(maxScore);

		return new FutureLong(this.queueRequest(Command.ZREMRANGEBYSCORE, keybytes, minScoreBytes, maxScoreBytes));
	}
	
	@Override
	public <K extends Object> Future<Long> zcount(K key, double minScore, double maxScore) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] minScoreBytes = Convert.toBytes(minScore);
		byte[] maxScoreBytes = Convert.toBytes(maxScore);

		return new FutureLong(this.queueRequest(Command.ZCOUNT, keybytes, minScoreBytes, maxScoreBytes));
	}
	
	@Override
	public <K extends Object> Future<Long> zremrangebyrank(K key, long minRank, long maxRank) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] minScoreBytes = Convert.toBytes(minRank);
		byte[] maxScoreBytes = Convert.toBytes(maxRank);

		return new FutureLong(this.queueRequest(Command.ZREMRANGEBYRANK, keybytes, minScoreBytes, maxScoreBytes));
	}


	@Override
	public <K extends Object> Future<List<byte[]>> zrevrange(K key, long from, long to) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(from);
		byte[] toBytes = Convert.toBytes(to);

		return new FutureByteArrayList(this.queueRequest(Command.ZREVRANGE, keybytes, fromBytes, toBytes));
	}

	@Override
	public <K extends Object> Future<List<ZSetEntry>> zrangeSubset(K key, long from, long to) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(from);
		byte[] toBytes = Convert.toBytes(to);

		return new FutureZSetList(this.queueRequest(Command.ZRANGE$OPTS, keybytes, fromBytes, toBytes, Command.Option.WITHSCORES.bytes));
	}

	@Override
	public <K extends Object> Future<List<ZSetEntry>> zrevrangeSubset(K key, long from, long to) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(from);
		byte[] toBytes = Convert.toBytes(to);

		return new FutureZSetList(this.queueRequest(Command.ZREVRANGE$OPTS, keybytes, fromBytes, toBytes, Command.Option.WITHSCORES.bytes));
	}
	
	@Override
	public <K extends Object> Sort sort(final K key) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
		
		final JRedisFutureSupport client = this;
		Sort sortQuery = new SortSupport (keybytes) {

				@Override 
			protected Future<List<byte[]>> execAsyncSort(byte[]... fullSortCmd) {
				return new FutureByteArrayList(client.queueRequest(Command.SORT, fullSortCmd));
			}
			protected Future<List<byte[]>> execAsyncSortStore(byte[]... fullSortCmd) {
				Future<Response> fResp = client.queueRequest(Command.SORT$STORE, fullSortCmd);
//				new FutureLong(fResp); // hah?
				return new FutureSortStoreResp(fResp);
			}
			protected List<byte[]> execSort(byte[]... fullSortCmd) {
				throw new IllegalStateException("JRedisFuture does not support synchronous sort.");
			}
			protected List<byte[]> execSortStore(byte[]... fullSortCmd) {
				throw new IllegalStateException("JRedisFuture does not support synchronous sort.");
			}
		};
		return sortQuery;
	}

	/* ------------------------------- commands that don't get a response --------- */

	@Override
	public <K extends Object> FutureStatus quit()  {
		return new FutureStatus(this.queueRequest(Command.QUIT));
	}
	@Override
	public FutureStatus flush()  {
		return new FutureStatus(this.queueRequest(Command.CONN_FLUSH));
	}
	/* ------------------------------- commands that don't get a response END --------- */
	@Override
	public <K extends Object> Future<List<byte[]>> sinter(K set1, K... sets) {
		byte[] keydata = null;
		if((keydata = JRedisSupport.getKeyBytes(set1)) == null)
			throw new IllegalArgumentException ("invalid key => ["+set1+"]");

		byte[][] keybytes = new byte[1+sets.length][];
		int i=0; keybytes[i++] = keydata;
		for(K k : sets) {
			if((keydata = JRedisSupport.getKeyBytes(k)) == null)
				throw new IllegalArgumentException ("invalid key => ["+k+"]");
			keybytes[i++] = keydata;
		}
		
		return new FutureByteArrayList(this.queueRequest(Command.SINTER, keybytes));
	}

	@Override
	public <K extends Object> Future<List<byte[]>> sunion(K set1, K... sets) {
		byte[] keydata = null;
		if((keydata = JRedisSupport.getKeyBytes(set1)) == null)
			throw new IllegalArgumentException ("invalid key => ["+set1+"]");

		byte[][] keybytes = new byte[1+sets.length][];
		int i=0; keybytes[i++] = keydata;
		for(K k : sets) {
			if((keydata = JRedisSupport.getKeyBytes(k)) == null)
				throw new IllegalArgumentException ("invalid key => ["+k+"]");
			keybytes[i++] = keydata;
		}
		
		return new FutureByteArrayList(this.queueRequest(Command.SUNION, keybytes));
	}

	@Override
	public <K extends Object> Future<List<byte[]>> sdiff(K set1, K... sets) {
		byte[] keydata = null;
		if((keydata = JRedisSupport.getKeyBytes(set1)) == null)
			throw new IllegalArgumentException ("invalid key => ["+set1+"]");

		byte[][] keybytes = new byte[sets.length+1][];
		int i=0; keybytes[i++] = keydata;
		for(K k : sets) {
			if((keydata = JRedisSupport.getKeyBytes(k)) == null)
				throw new IllegalArgumentException ("invalid key => ["+k+"]");
			keybytes[i++] = keydata;
		}
		return new FutureByteArrayList(this.queueRequest(Command.SDIFF, keybytes));
	}

	@Override
	public <K extends Object> FutureStatus sinterstore(K dest, K... sets) {
		byte[] keydata = null;
		if((keydata = JRedisSupport.getKeyBytes(dest)) == null)
			throw new IllegalArgumentException ("invalid key => ["+dest+"]");

		byte[][] setbytes = new byte[1+sets.length][];
		int i=0;
		setbytes[i++] = keydata;
		byte[] setdata =null;
		for(K k : sets) {
			if((setdata = JRedisSupport.getKeyBytes(k)) == null)
				throw new IllegalArgumentException ("invalid key => ["+k+"]");
			setbytes[i++] = setdata;
		}
		
		return new FutureStatus(this.queueRequest(Command.SINTERSTORE, setbytes));
	}

	@Override
	public <K extends Object> FutureStatus sunionstore(K dest, K... sets) {
		byte[] keydata = null;
		if((keydata = JRedisSupport.getKeyBytes(dest)) == null)
			throw new IllegalArgumentException ("invalid key => ["+dest+"]");

		byte[][] setbytes = new byte[1+sets.length][];
		int i=0;
		setbytes[i++] = keydata;
		byte[] setdata =null;
		for(K k : sets) {
			if((setdata = JRedisSupport.getKeyBytes(k)) == null)
				throw new IllegalArgumentException ("invalid key => ["+k+"]");
			setbytes[i++] = setdata;
		}
		
		return new FutureStatus(this.queueRequest(Command.SUNIONSTORE, setbytes));
	}

	@Override
	public <K extends Object> FutureStatus sdiffstore(K dest, K... sets) {
		byte[] keydata = null;
		if((keydata = JRedisSupport.getKeyBytes(dest)) == null)
			throw new IllegalArgumentException ("invalid key => ["+dest+"]");

		byte[][] setbytes = new byte[1+sets.length][];
		int i=0;
		setbytes[i++] = keydata;
		byte[] setdata =null;
		for(K k : sets) {
			if((setdata = JRedisSupport.getKeyBytes(k)) == null)
				throw new IllegalArgumentException ("invalid key => ["+k+"]");
			setbytes[i++] = setdata;
		}
		
		return new FutureStatus(this.queueRequest(Command.SDIFFSTORE, setbytes));
	}

	@Override
	public <K extends Object> Future<Long> del(K ... keys) {
		if(null == keys || keys.length == 0) throw new IllegalArgumentException("no keys specified");
		byte[] keydata = null;
		byte[][] keybytes = new byte[keys.length][];
		int i=0;
		for(K k : keys) {
			if((keydata = JRedisSupport.getKeyBytes(k)) == null)
				throw new IllegalArgumentException ("invalid key => ["+k+"] @ index: " + i);
			
			keybytes[i++] = keydata;
		}

		Future<Response> futureResponse = this.queueRequest(Command.DEL, keybytes);
		return new FutureLong(futureResponse);
	}


	@Override
	public <K extends Object> Future<Boolean> exists(K key) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.EXISTS, keybytes);
		return new FutureBoolean(futureResponse);
	}


	@Override
	public <K extends Object> FutureLong lpush(K key, byte[] value) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
		
		if(value == null)
			throw new IllegalArgumentException ("null value");
		
		
		return new FutureLong(this.queueRequest(Command.LPUSH, keybytes, value));
	}
	@Override
	public <K extends Object> FutureLong lpush(K key, String value) {
		return lpush(key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> FutureLong lpush(K key, Number value) {
		return lpush(key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> FutureLong lpush (K key, T value)
	{
		return lpush(key, DefaultCodec.encode(value));
	}
	


	@Override
	public <K extends Object> Future<Long> lrem(K key, byte[] value, int count) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] countBytes = Convert.toBytes(count);

		Future<Response> futureResponse = this.queueRequest(Command.LREM, keybytes, countBytes, value);
		return new FutureLong(futureResponse);
	}
	@Override
	public <K extends Object> Future<Long> lrem (K listKey, String value, int count){
		return lrem (listKey, DefaultCodec.encode(value), count);
	}
	@Override
	public <K extends Object> Future<Long> lrem (K listKey, Number numberValue, int count) {
		return lrem (listKey, String.valueOf(numberValue).getBytes(), count);
	}
	@Override
	public <K extends Object, T extends Serializable>
	Future<Long> lrem (K listKey, T object, int count){
		return lrem (listKey, DefaultCodec.encode(object), count);
	}


	@Override
	public <K extends Object> FutureStatus lset(K key, long index, byte[] value) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] indexBytes = Convert.toBytes(index);
		return new FutureStatus(this.queueRequest(Command.LSET, keybytes, indexBytes, value));
	}
	@Override
	public <K extends Object> FutureStatus lset (K key, long index, String value) {
		return lset (key, index, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> FutureStatus lset (K key, long index, Number numberValue){
		return lset (key, index, String.valueOf(numberValue).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> FutureStatus lset (K key, long index, T object){
		return lset (key, index, DefaultCodec.encode(object));
	}

	@Override
	public <K extends Object> Future<Boolean> move(K key, int dbIndex) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
		
		Future<Response> futureResponse = this.queueRequest(Command.MOVE, keybytes, Convert.toBytes(dbIndex));
		return new FutureBoolean(futureResponse);
	}


	@Override
	public <K extends Object> Future<Boolean> srem(K key, byte[] member) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.SREM, keybytes, member);
		return new FutureBoolean(futureResponse);
	}
	@Override
	public <K extends Object> Future<Boolean> srem (K key, String value) {
		return srem (key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> Future<Boolean> srem (K key, Number value) {
		return srem (key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> Future<Boolean> srem (K key, T value)
	{
		return srem (key, DefaultCodec.encode(value));
	}

	@Override
	public <K extends Object> Future<Boolean> zrem(K key, byte[] member) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.ZREM, keybytes, member);
		return new FutureBoolean(futureResponse);
	}
	@Override
	public <K extends Object> Future<Boolean> zrem (K key, String value) {
		return zrem (key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> Future<Boolean> zrem (K key, Number value) {
		return zrem (key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> Future<Boolean> zrem (K key, T value)
	{
		return zrem (key, DefaultCodec.encode(value));
	}


	@Override
	public <K extends Object> Future<Double> zscore(K key, byte[] member) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.ZSCORE, keybytes, member);
		return new FutureDouble(futureResponse);
	}
	@Override
	public <K extends Object> Future<Double> zscore (K key, String value) {
		return zscore (key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> Future<Double> zscore (K key, Number value) {
		return zscore (key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> Future<Double> zscore (K key, T value)
	{
		return zscore (key, DefaultCodec.encode(value));
	}

	@Override
	public <K extends Object> Future<Long> zrank(K key, byte[] member) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.ZRANK, keybytes, member);
		return new FutureLong(futureResponse);
	}
	@Override
	public <K extends Object> Future<Long> zrank (K key, String value) {
		return zrank (key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> Future<Long> zrank (K key, Number value) {
		return zrank (key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> Future<Long> zrank (K key, T value)
	{
		return zrank (key, DefaultCodec.encode(value));
	}

	@Override
	public <K extends Object> Future<Long> zrevrank(K key, byte[] member) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.ZREVRANK, keybytes, member);
		return new FutureLong(futureResponse);
	}
	@Override
	public <K extends Object> Future<Long> zrevrank (K key, String value) {
		return zrevrank (key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> Future<Long> zrevrank (K key, Number value) {
		return zrevrank (key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> Future<Long> zrevrank (K key, T value)
	{
		return zrevrank (key, DefaultCodec.encode(value));
	}


	@Override
	public <K extends Object> FutureStatus ltrim(K key, long keepFrom, long keepTo) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(keepFrom);
		byte[] toBytes = Convert.toBytes(keepTo);
		return new FutureStatus(this.queueRequest(Command.LTRIM, keybytes, fromBytes, toBytes));
	}

	@Override
	public <K extends Object> Future<Boolean> expire(K key, int ttlseconds) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] ttlbytes = Convert.toBytes(ttlseconds);
		
		Future<Response> futureResponse = this.queueRequest(Command.EXPIRE, keybytes, ttlbytes);
		return new FutureBoolean(futureResponse);
	}

	@Override
	public <K extends Object> Future<Boolean> expireat(K key, long epochtime) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		long expiretime = TimeUnit.SECONDS.convert(epochtime, TimeUnit.MILLISECONDS);
		byte[] expiretimeBytes = Convert.toBytes(expiretime);
		
		Future<Response> futureResponse = this.queueRequest(Command.EXPIREAT, keybytes, expiretimeBytes);
		return new FutureBoolean(futureResponse);
	}

	@Override
	public <K extends Object> Future<Long> ttl (K key) {
		byte[] keybytes = null;
		if((keybytes = JRedisSupport.getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Future<Response> futureResponse = this.queueRequest(Command.TTL, keybytes);
		return new FutureLong(futureResponse);
	}

	// TODO: integrate using KeyCodec and a CodecManager at client spec and init time.
	// TODO: (implied) ClientSpec (impls. ConnectionSpec)
	// this isn't cooked yet -- lets think more about the implications...
	//
//	static final private Map<String, byte[]>	keyByteCache = new ConcurrentHashMap<String, byte[]>();
	public static final boolean	CacheKeys	= false;
	
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

        @SuppressWarnings("boxing")
		public Boolean get () throws InterruptedException, ExecutionException {
        	ValueResponse valResp = (ValueResponse) pendingRequest.get();
        	return valResp.getBooleanValue();
        }

        @SuppressWarnings("boxing")
		public Boolean get (long timeout, TimeUnit unit)
        	throws InterruptedException, ExecutionException, TimeoutException
        {
        	ValueResponse valResp = (ValueResponse) pendingRequest.get(timeout, unit);
        	return valResp.getBooleanValue();
        }
	}
	
	public static class FutureBit extends FutureResultBase implements Future<Boolean> {

        protected FutureBit (Future<Response> pendingRequest) { super(pendingRequest); }

        @SuppressWarnings("boxing")
		public Boolean get () throws InterruptedException, ExecutionException {
        	ValueResponse valResp = (ValueResponse) pendingRequest.get();
        	return valResp.getLongValue() == 1;
        }

        @SuppressWarnings("boxing")
		public Boolean get (long timeout, TimeUnit unit)
        	throws InterruptedException, ExecutionException, TimeoutException
        {
        	ValueResponse valResp = (ValueResponse) pendingRequest.get(timeout, unit);
        	return valResp.getLongValue() == 1;
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

        @SuppressWarnings("boxing")
		public Long get () throws InterruptedException, ExecutionException {
        	ValueResponse valResp = (ValueResponse) pendingRequest.get();
        	return valResp.getLongValue();
        }

        @SuppressWarnings("boxing")
		public Long get (long timeout, TimeUnit unit)
        	throws InterruptedException, ExecutionException, TimeoutException
        {
        	ValueResponse valResp = (ValueResponse) pendingRequest.get(timeout, unit);
        	return valResp.getLongValue();
        }
	}
	public static class FutureDouble extends FutureResultBase implements Future<Double>{

        protected FutureDouble (Future<Response> pendingRequest) { super(pendingRequest); }

        @SuppressWarnings("boxing")
		public Double get () throws InterruptedException, ExecutionException {
        	BulkResponse bulkResp = (BulkResponse) pendingRequest.get();
        	if(bulkResp.getBulkData() != null)
        		return Convert.toDouble(bulkResp.getBulkData());
        	return null;
        }

        @SuppressWarnings("boxing")
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
	public static class FutureSortStoreResp extends FutureResultBase implements Future<List<byte[]>>{
		
        protected FutureSortStoreResp (Future<Response> pendingRequest) { super(pendingRequest); }
		
        public List<byte[]> get () throws InterruptedException, ExecutionException {
        	ValueResponse resp = (ValueResponse) pendingRequest.get();
        	return packValueResult(resp.getLongValue());
        }
		
        public List<byte[]> get (long timeout, TimeUnit unit)
		throws InterruptedException, ExecutionException, TimeoutException
        {
        	ValueResponse resp = (ValueResponse) pendingRequest.get(timeout, unit);
        	return packValueResult(resp.getLongValue());
        }
        private static List<byte[]> packValueResult(long number) {
        	List<byte[]> list = new ArrayList<byte[]>(1);
			list.add(Convert.toBytes(number));
			
        	return list;
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

	public static class FutureDataDictionary extends FutureResultBase implements Future<Map<byte[], byte[]>>{

        protected FutureDataDictionary (Future<Response> pendingRequest) { super(pendingRequest); }

        public Map<byte[], byte[]> get () throws InterruptedException, ExecutionException {
        	MultiBulkResponse resp = (MultiBulkResponse) pendingRequest.get();
        	return convert(resp.getMultiBulkData());
        }

        public Map<byte[], byte[]> get (long timeout, TimeUnit unit)
        	throws InterruptedException, ExecutionException, TimeoutException
        {
        	MultiBulkResponse resp = (MultiBulkResponse) pendingRequest.get(timeout, unit);
        	return convert(resp.getMultiBulkData());
        }
        private static final Map<byte[], byte[]> convert (List<byte[]> bulkdata) {
        	Map<byte[], byte[]> map = null;
        	if(null != bulkdata) {
        		map = new HashMap<byte[], byte[]>(bulkdata.size()/2);
        		for(int i=0; i<bulkdata.size(); i+=2){
//        			map.put(DefaultCodec.toStr(bulkdata.get(i)), bulkdata.get(i+1));
        			map.put(bulkdata.get(i), bulkdata.get(i+1));
        		}
        	}
        	return map;
        }
	}

	public static class FutureKeyList extends FutureResultBase implements Future<List<byte[]>>{

        protected FutureKeyList (Future<Response> pendingRequest) { super(pendingRequest); }

//        private List<String>  getResultList (BulkResponse resp) {
//    		StringTokenizer tokenizer = new StringTokenizer(new String(resp.getBulkData()), " ");
//    		List<String>  list = new ArrayList <String>(12);
//    		while (tokenizer.hasMoreTokens()){
//    			list.add(tokenizer.nextToken());
//    		}
//    		return list;
//        }
        public List<byte[]> get () throws InterruptedException, ExecutionException {

        	MultiBulkResponse resp = (MultiBulkResponse) pendingRequest.get();
        	List<byte[]> multibulkdata = resp.getMultiBulkData();
//        	List<String> list = null;
//        	if(null != multibulkdata)
//        		list = DefaultCodec.toStr(multibulkdata);
        	return multibulkdata;
        }

        public List<byte[]> get (long timeout, TimeUnit unit)
        	throws InterruptedException, ExecutionException, TimeoutException
        {
            	MultiBulkResponse resp = (MultiBulkResponse) pendingRequest.get(timeout, unit);
            	List<byte[]> multibulkdata = resp.getMultiBulkData();
//            	List<String> list = null;
//            	if(null != multibulkdata)
//            		list = DefaultCodec.toStr(multibulkdata);
            	return multibulkdata;
        }
	}
	public static class FutureInfo extends FutureResultBase implements Future<Map<String, String>>{

        protected FutureInfo (Future<Response> pendingRequest) { super(pendingRequest); }

        private Map<String, String>  getResultMap (BulkResponse resp) {
    		StringTokenizer tokenizer = new StringTokenizer(new String(resp.getBulkData()), "\r\n");
    		Map<String, String>  infomap = new HashMap<String, String>(48);
    		while (tokenizer.hasMoreTokens()){
    			final String info = tokenizer.nextToken();
    			// ignore comments "# heading"
    			if(info.startsWith("#"))
    				continue;
    			int c = info.indexOf(':');
    			String _key =info.substring(0, c);
    			String _value = info.substring(c+1);
    			infomap.put(_key, _value);
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
	public <K extends Object> Future<byte[]> echo (byte[] msg) {
		if(msg == null)
			throw new IllegalArgumentException ("invalid value for echo => [null]");

		Future<Response> futureResponse = this.queueRequest(Command.ECHO, msg);
		return new FutureByteArray(futureResponse);
		
	}
	public <K extends Object> Future<byte[]> echo (String msg) {
		return echo(DefaultCodec.encode(msg));
	}
	public <K extends Object> Future<byte[]> echo (Number msg) {
		return echo(String.valueOf(msg).getBytes());
	}
	public <K extends Object, T extends Serializable>
		Future<byte[]> echo (T msg) {
			return echo (DefaultCodec.encode(msg));
	}
	

}
