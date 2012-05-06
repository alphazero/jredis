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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jredis.ClientRuntimeException;
import org.jredis.JRedis;
import org.jredis.KeyValueSet;
import org.jredis.ObjectInfo;
import org.jredis.ProviderException;
import org.jredis.Redis;
import org.jredis.RedisException;
import org.jredis.RedisType;
import org.jredis.Sort;
import org.jredis.ZSetEntry;
import org.jredis.connector.Connection;
import org.jredis.protocol.BulkResponse;
import org.jredis.protocol.Command;
import org.jredis.protocol.MultiBulkResponse;
import org.jredis.protocol.Response;
import org.jredis.protocol.ValueResponse;
import org.jredis.ri.RI.Release;
import org.jredis.ri.RI.Version;
import org.jredis.ri.alphazero.semantics.DefaultKeyCodec;
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
public abstract class JRedisSupport implements JRedis {
	
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
	 * This method mimics the eponymous {@link Connection#serviceRequest(Command, byte[]...)}
	 * which defines the blocking api semantics of Synchronous connections.  The extending class
	 * can either directly (a) implement the protocol requirements, or, (b) delegate to a
	 * {@link Connection} instance, or, (c) utilize a pool of {@link Connection}s.  
	 * 
	 * @param cmd
	 * @param args
	 * @return
	 * @throws RedisException
	 * @throws ClientRuntimeException
	 * @throws ProviderException
	 */
	protected abstract Response serviceRequest (Command cmd, byte[]...args) throws RedisException, ClientRuntimeException, ProviderException; 
	// ------------------------------------------------------------------------
	// INTERFACE
	// ================================================================ Redis
	/*
	 * Support of all the JRedis interface methods.
	 * 
	 * This class uses the UTF-8 character set for all conversions due to its
	 * use of the Convert and Codec support classes.
	 * 
	 * All calls are forwarded to an abstract serviceRequest method that the
	 * extending classes are expected to implement.  
	 * 
	 * Implementation note:
	 * The methods in this class use redundant code in marshalling request params
	 * and in unmarshalling the response data.  We certainly can use a few helper
	 * functions to reduce the redundancy, but given that such methods would be
	 * repeatedly called, it was decided to effectively inline these statements in 
	 * each method body.  
	 */
	// ------------------------------------------------------------------------


	@Override
	public <K> boolean setbit(K key, int offset, boolean value) throws ProviderException, ClientRuntimeException, RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		/* ValueRespose */
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.SETBIT, keybytes, Convert.toBytes(offset), Convert.toBytes(value ? 1 : 0));
			return valResponse.getLongValue() == 1;
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
	}


	@Override
	public <K> boolean getbit(K key, int offset) throws ProviderException, ClientRuntimeException, RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		/* ValueRespose */
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.GETBIT, keybytes, Convert.toBytes(offset));
			return valResponse.getLongValue() == 1;
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
	}


	//	@Override
//	public <K extends Object> JRedis auth(K key) throws RedisException {
//		byte[] keydata = null;
//		if((keydata = getKeyBytes(key)) == null) 
//			throw new IllegalArgumentException ("invalid key => ["+key+"]");
//
//		this.serviceRequest(Command.AUTH, keydata);
//		return this;
//	}
	@Override
	public <K extends Object> void bgsave() throws RedisException {
		this.serviceRequest(Command.BGSAVE);
	}
	
	@Override
	public <K extends Object> String bgrewriteaof() throws RedisException {
		/* boolean ValueRespose */
		String value = null;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.BGREWRITEAOF);
			value = valResponse.getStringValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}
	
	@Override
	public <K extends Object> JRedis ping() throws RedisException {
		this.serviceRequest(Command.PING);
		return this;
	}

	@Override
	public <K extends Object> JRedis flushall() throws RedisException {
		this.serviceRequest(Command.FLUSHALL).getStatus();
		return this;
	}
	@Override
	public <K extends Object> JRedis flushdb() throws RedisException {
		this.serviceRequest(Command.FLUSHDB).getStatus();
		return this;
	}
//	@Override
//	public <K extends Object> JRedis select(int index) throws RedisException {
//		this.serviceRequest(Command.SELECT, Convert.toBytes(index));
//		return this;
//	}
	
	public <K extends Object> void slaveof(String host, int port) throws RedisException{
		byte[] hostbytes = null;
		if((hostbytes = getKeyBytes(host)) == null) 
			throw new IllegalArgumentException ("invalid host => ["+host+"]");

		byte[] portbytes = null;
		if((portbytes = Convert.toBytes(port)) == null) 
			throw new IllegalArgumentException ("invalid port => ["+port+"]");

		this.serviceRequest(Command.SLAVEOF, hostbytes, portbytes);
	}
	
	public <K extends Object> void slaveofnone() throws RedisException{
		this.serviceRequest(Command.SLAVEOF, "no".getBytes(), "one".getBytes());
	}
	
	@Override
	public <K extends Object> void rename(K oldkey, K newkey) throws RedisException {
		byte[] oldkeydata = null;
		if((oldkeydata = getKeyBytes(oldkey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+oldkey+"]");

		byte[] newkeydata = null;
		if((newkeydata = getKeyBytes(newkey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+newkey+"]");

		this.serviceRequest(Command.RENAME, oldkeydata, newkeydata);
	}
	
	@Override
	public <K extends Object> boolean renamenx(K oldkey, K newkey) throws RedisException{
		byte[] oldkeydata = null;
		if((oldkeydata = getKeyBytes(oldkey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+oldkey+"]");

		byte[] newkeydata = null;
		if((newkeydata = getKeyBytes(newkey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+newkey+"]");

		/* boolean ValueRespose */
		boolean value = false;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.RENAMENX, oldkeydata, newkeydata);
			value = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}
	@Override
	public <K extends Object> byte[] rpoplpush(K srcList, K destList) 
	throws RedisException 
	{
		byte[] srckeybytes = null;
		if((srckeybytes = getKeyBytes(srcList)) == null) 
			throw new IllegalArgumentException ("invalid src key => ["+srcList+"]");
		byte[] destkeybytes = null;
		if((destkeybytes = getKeyBytes(destList)) == null) 
			throw new IllegalArgumentException ("invalid dest key => ["+destList+"]");
		byte[] bulkData= null;
		try {
			BulkResponse response = (BulkResponse) this.serviceRequest(Command.RPOPLPUSH, srckeybytes, destkeybytes);
			bulkData = response.getBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return bulkData;
		
	}
	
	@Override
	public <K extends Object> void rpush(K key, byte[] value) 
	throws RedisException 
	{
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
		
		if(value == null) 
			throw new IllegalArgumentException ("null value for list op");
		
		this.serviceRequest(Command.RPUSH, keybytes, value);
	}
	@Override
	public <K extends Object> void rpush(K key, String value) throws RedisException {
//		rpush(key, DefaultCodec.encode(value));
		rpush(key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> void rpush(K key, Number value) throws RedisException {
		rpush(key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> void rpush (K key, T value) throws RedisException
	{
		rpush(key, DefaultCodec.encode(value));
	}

	@Override
	public <K extends Object> boolean sadd(K key, byte[] member) 
	throws RedisException 
	{
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
		/* boolean ValueRespose */
		boolean res = false;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.SADD, keybytes, member);
			res = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return res;
	}
	@Override
	public <K extends Object> boolean sadd (K key, String value) throws RedisException {
		return sadd (key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> boolean sadd (K key, Number value) throws RedisException {
		return sadd (key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> boolean sadd (K key, T value) throws RedisException
	{
		return sadd (key, DefaultCodec.encode(value));
	}

	@Override
	public <K extends Object> boolean zadd(K key, double score, byte[] member) 
	throws RedisException 
	{
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
		/* boolean ValueRespose */
		boolean res = false;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.ZADD, keybytes, Convert.toBytes(score), member);
			res = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return res;
	}
	@Override
	public <K extends Object> boolean zadd (K key, double score, String value) throws RedisException {
		return zadd (key, score, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> boolean zadd (K key, double score, Number value) throws RedisException {
		return zadd (key, score, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> boolean zadd (K key, double score, T value) throws RedisException
	{
		return zadd (key, score, DefaultCodec.encode(value));
	}

	@SuppressWarnings("boxing")
	@Override
	public <K extends Object> Double zincrby(K key, double score, byte[] member) 
	throws RedisException 
	{
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
		/* Double BulkResponse */
		Double resvalue = null;
		try {
			BulkResponse bulkResponse = (BulkResponse) this.serviceRequest(Command.ZINCRBY, keybytes, Convert.toBytes(score), member);
			if (bulkResponse.getBulkData() != null)
				resvalue = Convert.toDouble(bulkResponse.getBulkData());
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resvalue;
	}
	@Override
	public <K extends Object> Double zincrby (K key, double score, String value) throws RedisException {
		return zincrby (key, score, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> Double zincrby (K key, double score, Number value) throws RedisException {
		return zincrby (key, score, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> Double zincrby (K key, double score, T value) throws RedisException
	{
		return zincrby (key, score, DefaultCodec.encode(value));
	}

	
	@Override
	public <K extends Object> void save() 
	throws RedisException 
	{
		this.serviceRequest(Command.SAVE);
	}
	
	// -------- set 

	@Override
	public <K extends Object> void set(K key, byte[] value) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		this.serviceRequest(Command.SET, keybytes, value);
	}
	@Override
	public <K extends Object> void set(K key, String value) throws RedisException {
		set(key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> void set(K key, Number value) throws RedisException {
		set(key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> void set (K key, T value) throws RedisException
	{
		set(key, DefaultCodec.encode(value));
	}
	
	@Override
	public <K extends Object> byte[] getset(K key, byte[] value) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] bulkData= null;
		try {
			BulkResponse response = (BulkResponse) this.serviceRequest(Command.GETSET, keybytes, value);
			bulkData = response.getBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return bulkData;
	}
	@Override
	public <K extends Object> byte[] getset(K key, String value) throws RedisException {
		return getset(key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> byte[] getset(K key, Number value) throws RedisException {
		return getset(key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> 
	byte[] getset (K key, T value) throws RedisException
	{
		return getset(key, DefaultCodec.encode(value));
	}
	
	@Override
	public <K extends Object> boolean setnx(K key, byte[] value) throws RedisException{
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		boolean resvalue = false;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.SETNX, keybytes, value);
			resvalue = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resvalue;
	}
	@Override
	public <K extends Object> boolean setnx(K key, String value) throws RedisException {
		return setnx(key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> boolean setnx(K key, Number value) throws RedisException {
		return setnx(key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> boolean setnx (K key, T value) throws RedisException {
		return setnx(key, DefaultCodec.encode(value));
	}

	
	@Override
	public <K extends Object> long append(K key, byte[] value) throws RedisException{
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		long resvalue = -1;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.APPEND, keybytes, value);
			resvalue = valResponse.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resvalue;
	}
	@Override
	public <K extends Object> long append(K key, String value) throws RedisException {
		return append(key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> long append(K key, Number value) throws RedisException {
		return append(key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> long append (K key, T value) throws RedisException {
		return append(key, DefaultCodec.encode(value));
	}

	private boolean msetnx(byte[][] mappings) throws RedisException {
		boolean resvalue = false;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.MSETNX, mappings);
			resvalue = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resvalue;
	}
	@Override
	public <K extends Object> boolean msetnx(Map<K, byte[]> keyValueMap) throws RedisException {
//		KeyCodec<Object> codec = DefaultKeyCodec.provider();
		byte[][] mappings = new byte[keyValueMap.size()*2][];
		int i = 0;
		for (Entry<K, byte[]> e : keyValueMap.entrySet()){
			mappings[i++] = getKeyBytes(e.getKey());
			mappings[i++] = e.getValue();
		}
		return msetnx(mappings);
	}
	@Override
	public <K extends Object> boolean msetnx(KeyValueSet.ByteArrays<K> keyValueMap) throws RedisException {
		return msetnx(keyValueMap.getMappings());
	}
	@Override
	public <K extends Object> boolean msetnx(KeyValueSet.Strings<K> keyValueMap) throws RedisException{
		return msetnx(keyValueMap.getMappings());
	}
	@Override
	public <K extends Object> boolean msetnx(KeyValueSet.Numbers<K> keyValueMap) throws RedisException{
		return msetnx(keyValueMap.getMappings());
	}
	@Override
	public <K extends Object, T extends Serializable> boolean msetnx(KeyValueSet.Objects<K, T> keyValueMap) throws RedisException{
		return msetnx(keyValueMap.getMappings());
	}

	
	@Override
	public <K extends Object> boolean sismember(K key, byte[] member) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		/* boolean ValueRespose */
		boolean value = false;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.SISMEMBER, keybytes, member);
			value = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}

	@Override
	public <K extends Object> boolean sismember(K key, String value) throws RedisException {
		return sismember(key, DefaultCodec.encode(value));
	}

	@Override
	public <K extends Object> boolean sismember(K key, Number numberValue) throws RedisException {
		return sismember (key, String.valueOf(numberValue).getBytes());
	}

	@Override
	public <K extends Object, T extends Serializable> boolean sismember(K key, T object) throws RedisException {
		return sismember(key, DefaultCodec.encode(object));
	}

	public <K extends Object> boolean smove (K srcKey, K destKey, byte[] member) throws RedisException {
		byte[] srcKeyBytes = null;
		if((srcKeyBytes = getKeyBytes(srcKey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+srcKey+"]");

		byte[] destKeyBytes = null;
		if((destKeyBytes = getKeyBytes(destKey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+destKey+"]");

		/* boolean ValueRespose */
		boolean value = false;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.SMOVE, srcKeyBytes, destKeyBytes, member);
			value = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}
	public <K extends Object> boolean smove (K srcKey, K destKey, String stringValue) throws RedisException {
		return smove (srcKey, destKey, DefaultCodec.encode(stringValue));
	}
	public <K extends Object> boolean smove (K srcKey, K destKey, Number numberValue) throws RedisException {
		return smove (srcKey, destKey, String.valueOf(numberValue).getBytes());
	}
	public <K extends Object, T extends Serializable> 
	boolean smove (K srcKey, K destKey, T object) throws RedisException {
		return smove (srcKey, destKey, DefaultCodec.encode(object));
	}
		   
	// ------------------------------------------------------------------------
	// Commands operating on hashes
	// ------------------------------------------------------------------------
	
	public <K extends Object> boolean hset(K hashKey, K hashField, byte[] value)  throws RedisException {
		byte[] hashKeyBytes = null;
		if((hashKeyBytes = getKeyBytes(hashKey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+hashKey+"]");

		byte[] hashFieldBytes = null;
		if((hashFieldBytes = getKeyBytes(hashField)) == null) 
			throw new IllegalArgumentException ("invalid field => ["+hashField+"]");

		/* boolean ValueRespose */
		boolean response = false;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.HSET, hashKeyBytes, hashFieldBytes, value);
			response = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return response;
	}
	public <K extends Object> boolean hset(K key, K field, String stringValue)  throws RedisException {
		return hset (key, field, DefaultCodec.encode(stringValue));
	}
	public <K extends Object> boolean hset(K key, K field, Number numberValue)  throws RedisException {
		return hset (key, field, String.valueOf(numberValue).getBytes());
	}
	public <K extends Object, T extends Serializable> 
	boolean hset(K key, K field, T object)  throws RedisException {
		return hset (key, field, DefaultCodec.encode(object));
	}
	
	public <K extends Object> byte[] hget(K hashKey, K hashField)  throws RedisException {
		byte[] hashKeyBytes = null;
		if((hashKeyBytes = getKeyBytes(hashKey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+hashKey+"]");

		byte[] hashFieldBytes = null;
		if((hashFieldBytes = getKeyBytes(hashField)) == null) 
			throw new IllegalArgumentException ("invalid field => ["+hashField+"]");

		byte[] bulkData= null;
		try {
			BulkResponse response = (BulkResponse) this.serviceRequest(Command.HGET, hashKeyBytes, hashFieldBytes);
			bulkData = response.getBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return bulkData;
	}
	
	public <K extends Object> boolean hexists(K hashKey, K hashField)  throws RedisException {
		byte[] hashKeyBytes = null;
		if((hashKeyBytes = getKeyBytes(hashKey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+hashKey+"]");

		byte[] hashFieldBytes = null;
		if((hashFieldBytes = getKeyBytes(hashField)) == null) 
			throw new IllegalArgumentException ("invalid field => ["+hashField+"]");

		boolean resp = false;
		try {
			ValueResponse response = (ValueResponse) this.serviceRequest(Command.HEXISTS, hashKeyBytes, hashFieldBytes);
			resp = response.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resp;
	}
	@Override
	public <K extends Object> boolean hdel(K hashKey, K hashField)  throws RedisException {
		byte[] hashKeyBytes = null;
		if((hashKeyBytes = getKeyBytes(hashKey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+hashKey+"]");

		byte[] hashFieldBytes = null;
		if((hashFieldBytes = getKeyBytes(hashField)) == null) 
			throw new IllegalArgumentException ("invalid field => ["+hashField+"]");

		boolean resp = false;
		try {
			ValueResponse response = (ValueResponse) this.serviceRequest(Command.HDEL, hashKeyBytes, hashFieldBytes);
			resp = response.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resp;
	}
	@Override
	public <K extends Object> long hlen(K hashKey)  throws RedisException {
		byte[] hashKeyBytes = null;
		if((hashKeyBytes = getKeyBytes(hashKey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+hashKey+"]");

		long resp = 0;
		try {
			ValueResponse response = (ValueResponse) this.serviceRequest(Command.HLEN, hashKeyBytes);
			resp = response.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resp;
	}
	@Redis(versions="1.3.n")
	@Override
	public <K extends Object> List<byte[]> hkeys(K hashKey)  throws RedisException {
		byte[] hashKeyBytes = null;
		if((hashKeyBytes = getKeyBytes(hashKey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+hashKey+"]");

		List<byte[]> multibulkData = null;
		try {
			MultiBulkResponse response = (MultiBulkResponse) this.serviceRequest(Command.HKEYS, hashKeyBytes);
//			if(null != response.getMultiBulkData()) resp = DefaultCodec.toStr(response.getMultiBulkData());
			multibulkData = response.getMultiBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return multibulkData;
	}

	@Redis(versions="1.3.n")
	@Override
	public <K extends Object> List<byte[]> hvals(K hashKey)  throws RedisException {
		byte[] hashKeyBytes = null;
		if((hashKeyBytes = getKeyBytes(hashKey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+hashKey+"]");

		List<byte[]> resp = null;
		try {
			MultiBulkResponse response = (MultiBulkResponse) this.serviceRequest(Command.HVALS, hashKeyBytes);
			resp = response.getMultiBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return resp;
	}

	@Redis(versions="1.3.n")
	@Override
	public <K extends Object> Map<byte[], byte[]> hgetall(K hashKey)  throws RedisException {
		byte[] hashKeyBytes = null;
		if((hashKeyBytes = getKeyBytes(hashKey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+hashKey+"]");

		Map<byte[], byte[]> resp = null;
		try {
			MultiBulkResponse response = (MultiBulkResponse) this.serviceRequest(Command.HGETALL, hashKeyBytes);
			List<byte[]> bulkdata = response.getMultiBulkData();
			if(null != bulkdata) {
				resp = new HashMap<byte[], byte[]>(bulkdata.size()/2);
				for(int i=0; i<bulkdata.size(); i+=2){
					resp.put(bulkdata.get(i), bulkdata.get(i+1));
				}
			}
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return resp;
	}

	
	/* ------------------------------- commands returning int value --------- */

	@Override
	public <K extends Object> long incr(K key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		/* ValueRespose */
		long value = Long.MIN_VALUE;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.INCR, keybytes);
			value = valResponse.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}

	@Override
	public <K extends Object> long incrby(K key, int delta) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		/* ValueRespose */
		long value = Long.MIN_VALUE;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.INCRBY, keybytes, Convert.toBytes(delta));
			value = valResponse.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}

	@Override
	public <K extends Object> long decr(K key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		/* ValueRespose */
		long value = Long.MAX_VALUE;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.DECR, keybytes);
			value = valResponse.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}

	@Override
	public <K extends Object> long decrby(K key, int delta) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		/* ValueRespose */
		long value = Long.MAX_VALUE;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.DECRBY, keybytes, Convert.toBytes(delta));
			value = valResponse.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}

	@Override
	public <K extends Object> long llen(K key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		/* ValueRespose */ /* int since max size is 1GB, an integer 1,073,741,824 */
		long value = Integer.MIN_VALUE;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.LLEN, keybytes);
			value = valResponse.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}

	@Override
	public <K extends Object> long scard(K key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
		long value = Long.MIN_VALUE;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.SCARD, keybytes);
			value = valResponse.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}
	
	@Override
	public <K extends Object> long zcard(K key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
		long value = Long.MIN_VALUE;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.ZCARD, keybytes);
			value = valResponse.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}
	
	public <K extends Object> byte[] srandmember (K setkey) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(setkey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+setkey+"]");

		byte[] bulkData= null;
		try {
			BulkResponse response = (BulkResponse) this.serviceRequest(Command.SRANDMEMBER, keybytes);
			bulkData = response.getBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return bulkData;
	}

	public <K extends Object> byte[] spop (K setkey) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(setkey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+setkey+"]");

		byte[] bulkData= null;
		try {
			BulkResponse response = (BulkResponse) this.serviceRequest(Command.SPOP, keybytes);
			bulkData = response.getBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return bulkData;
	}

	/* ------------------------------- commands returning long value --------- */

	@Override
	public <K extends Object> long dbsize() throws RedisException {
		long value = Long.MIN_VALUE;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.DBSIZE);
			value = valResponse.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}
	@Override
	public <K extends Object> long lastsave() throws RedisException {
		long value = Long.MIN_VALUE;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.LASTSAVE);
			value = valResponse.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}

	/* ------------------------------- commands returning byte[] --------- */

	@Override
	public <K extends Object> byte[] get(K key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] bulkData= null;
		try {
			BulkResponse response = (BulkResponse) this.serviceRequest(Command.GET, keybytes);
			bulkData = response.getBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return bulkData;
	}

	@Override
	public <K extends Object> byte[] lindex(K key, long index) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] bulkData= null;
		try {
			BulkResponse response = (BulkResponse) this.serviceRequest(Command.LINDEX, keybytes, Convert.toBytes(index));
			bulkData = response.getBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return bulkData;
	}
	@Override
	public <K extends Object> byte[] lpop(K key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] bulkData= null;
		try {
			BulkResponse response = (BulkResponse) this.serviceRequest(Command.LPOP, keybytes);
			bulkData = response.getBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return bulkData;
	}

	@Override
	public <K extends Object> byte[] rpop(K key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] bulkData= null;
		try {
			BulkResponse response = (BulkResponse) this.serviceRequest(Command.RPOP, keybytes);
			bulkData = response.getBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return bulkData;
	}


	/* ------------------------------- commands returning String--------- */

	@Override
	public byte[] randomkey() throws RedisException {
		/* ValueRespose */
		byte[] bulkData = null;
//		String stringValue = null;
		try {
			BulkResponse valResponse = (BulkResponse) this.serviceRequest(Command.RANDOMKEY);
			bulkData = valResponse.getBulkData();
//			if (null != bulkData) {
//			  stringValue = new String(bulkData);
//			}
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}
//		return stringValue;
		return bulkData;
	}
	@Override
	public <K extends Object> RedisType type(K key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		RedisType	type = null;
		/* ValueRespose */
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.TYPE, keybytes);
			String stringValue = valResponse.getStringValue();
			type = RedisType.valueOf(stringValue);
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return type;
	}

	@Override
	public <K extends Object> ObjectInfo debug (K key) throws RedisException {
		
		byte[] keybytes = getKeyBytes(key);
//		if(key.length() == 0)
//			throw new IllegalArgumentException ("invalid zero length key => ["+key+"]");

		ObjectInfo	objectInfo = null;
		/* ValueRespose */
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.DEBUG, "OBJECT".getBytes(), keybytes);
			String stringValue = valResponse.getStringValue();
			objectInfo = ObjectInfo.valueOf(stringValue);
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return objectInfo;
	}
	/* ------------------------------- commands returning Maps --------- */

	@Override
	public <K extends Object> Map<String, String> info() throws RedisException {

		byte[] bulkData= null;
		try {
			BulkResponse response = (BulkResponse) this.serviceRequest(Command.INFO);
			bulkData = response.getBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}

		StringTokenizer tokenizer = new StringTokenizer(new String(bulkData), "\r\n");
		Map<String, String>  infomap = new HashMap<String, String>(48);
		while (tokenizer.hasMoreTokens()){
			String info = tokenizer.nextToken();
			System.out.format("line:<%s>\n", info);
			// ignore comments "# heading"
			if(info.startsWith("#"))
				continue;
			int c = info.indexOf(':');
			String key =info.substring(0, c);
			String value = info.substring(c+1);
			infomap.put(key, value);
		}
		return infomap;
	}

	private void mset(byte[][] mappings) throws RedisException {
		this.serviceRequest(Command.MSET, mappings);
	}
	public <K extends Object> void mset(Map<K, byte[]> keyValueMap) throws RedisException {
//		KeyCodec<Object> codec = DefaultKeyCodec.provider();
		byte[][] mappings = new byte[keyValueMap.size()*2][];
		int i = 0;
		for (Entry<K, byte[]> e : keyValueMap.entrySet()){
			mappings[i++] = getKeyBytes(e.getKey());
			mappings[i++] = e.getValue();
		}
		mset(mappings);
	}
	public <K extends Object> void mset(KeyValueSet.ByteArrays<K> keyValueMap) throws RedisException {
		mset(keyValueMap.getMappings());
	}
	public <K extends Object> void mset(KeyValueSet.Strings<K> keyValueMap) throws RedisException{
		mset(keyValueMap.getMappings());
	}
	public <K extends Object> void mset(KeyValueSet.Numbers<K> keyValueMap) throws RedisException{
		mset(keyValueMap.getMappings());
	}
	public <K extends Object, T extends Serializable> void mset(KeyValueSet.Objects<K, T> keyValueMap) throws RedisException{
		mset(keyValueMap.getMappings());
	}

	@Override
	public <K extends Object> List<byte[]> mget(K...keys) throws RedisException {

		if(null == keys || keys.length == 0) throw new IllegalArgumentException("no keys specified");
		byte[] keydata = null;
		byte[][] keybytes = new byte[keys.length][];
		int i=0;
		for(K k : keys) {
			if((keydata = getKeyBytes(k)) == null) 
				throw new IllegalArgumentException ("invalid key => ["+k+"] @ index: " + i);
			
			keybytes[i++] = keydata;
		}
		
		List<byte[]> multiBulkData= null;
		try {
			MultiBulkResponse MultiBulkResponse = (MultiBulkResponse) this.serviceRequest(Command.MGET, keybytes);
			multiBulkData = MultiBulkResponse.getMultiBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return multiBulkData;
	}

	@Override
	public <K extends Object> List<byte[]> smembers(K key) throws RedisException {
		byte[] keydata = null;
		if((keydata = getKeyBytes(key)) == null) 
			throw new RedisException (Command.KEYS, "ERR Invalid key.");

		List<byte[]> multiBulkData= null;
		try {
			MultiBulkResponse MultiBulkResponse = (MultiBulkResponse) this.serviceRequest(Command.SMEMBERS, keydata);
			multiBulkData = MultiBulkResponse.getMultiBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return multiBulkData;
	}
	@Override
	public <K extends Object> List<byte[]> keys() throws RedisException {
		return this.keys("*");
	}

	@Override
	public <K extends Object> List<byte[]> keys(K pattern) throws RedisException {
		byte[] keydata = null;
		if((keydata = getKeyBytes(pattern)) == null) 
			throw new RedisException (Command.KEYS, "ERR Invalid key.");

		List<byte[]> multiBulkData= null;
		try {
			MultiBulkResponse MultiBulkResponse = (MultiBulkResponse) this.serviceRequest(Command.KEYS, keydata);
			multiBulkData = MultiBulkResponse.getMultiBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
		}
//		return DefaultCodec.toStr(multiBulkData);
		return multiBulkData;
		/*
		byte[] bulkData= null;
		try {
			BulkResponse response = (BulkResponse) this.serviceRequest(Command.KEYS, keydata);
			bulkData = response.getBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}

		StringTokenizer tokenizer = new StringTokenizer(new String(bulkData), " ");
		List<String>  keyList = new ArrayList <String>(12);
		while (tokenizer.hasMoreTokens()){
			keyList.add(tokenizer.nextToken());
		}
		return keyList;
		*/
	}

	@Override
	public <K extends Object> List<byte[]> lrange(K key, long from, long to) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(from);
		byte[] toBytes = Convert.toBytes(to);

		List<byte[]> multiBulkData= null;
		try {
			MultiBulkResponse multiBulkResponse = (MultiBulkResponse) this.serviceRequest(Command.LRANGE, keybytes, fromBytes, toBytes);
			multiBulkData = multiBulkResponse.getMultiBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return multiBulkData;
	}

	@Override
	public <K extends Object> byte[] substr(K key, long from, long to) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(from);
		byte[] toBytes = Convert.toBytes(to);

		byte[] bulkData= null;
		try {
			BulkResponse bulkResponse = (BulkResponse) this.serviceRequest(Command.SUBSTR, keybytes, fromBytes, toBytes);
			bulkData = bulkResponse.getBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return bulkData;
	}

	@Override
	public <K extends Object> List<byte[]> zrangebyscore (K key, double minScore, double maxScore) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(minScore);
		byte[] toBytes = Convert.toBytes(maxScore);

		List<byte[]> multiBulkData= null;
		try {
			MultiBulkResponse MultiBulkResponse = (MultiBulkResponse) this.serviceRequest(Command.ZRANGEBYSCORE, keybytes, fromBytes, toBytes);
			multiBulkData = MultiBulkResponse.getMultiBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return multiBulkData;
	}

	@Override
	public <K extends Object> List<ZSetEntry> zrangebyscoreSubset (K key, double minScore, double maxScore) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null)
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(minScore);
		byte[] toBytes = Convert.toBytes(maxScore);

		List<ZSetEntry> list= null;
		try {
			MultiBulkResponse multiBulkResponse = (MultiBulkResponse) this.serviceRequest(Command.ZRANGEBYSCORE$OPTS, keybytes, fromBytes, toBytes, Command.Option.WITHSCORES.bytes);
			List<byte[]> bulkData = multiBulkResponse.getMultiBulkData();
			if(null != bulkData){
				list = new ArrayList<ZSetEntry>(bulkData.size()/2);
				for(int i=0; i<bulkData.size(); i+=2){
					list.add(new ZSetEntryImpl(bulkData.get(i), bulkData.get(i+1)));
				}
			}
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return list;
	}

	@Override
	public <K extends Object> long zremrangebyscore (K key, double minScore, double maxScore) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(minScore);
		byte[] toBytes = Convert.toBytes(maxScore);

		long resp = Long.MIN_VALUE;
		try {
			ValueResponse valueResponse = (ValueResponse) this.serviceRequest(Command.ZREMRANGEBYSCORE, keybytes, fromBytes, toBytes);
			resp = valueResponse.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a numeric ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resp;
	}

	@Override
	public <K extends Object> long zcount (K key, double minScore, double maxScore) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(minScore);
		byte[] toBytes = Convert.toBytes(maxScore);

		long resp = Long.MIN_VALUE;
		try {
			ValueResponse valueResponse = (ValueResponse) this.serviceRequest(Command.ZCOUNT, keybytes, fromBytes, toBytes);
			resp = valueResponse.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a numeric ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resp;
	}

	@Override
	public <K extends Object> long zremrangebyrank (K key, long minRank, long maxRank) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(minRank);
		byte[] toBytes = Convert.toBytes(maxRank);

		long resp = Long.MIN_VALUE;
		try {
			ValueResponse valueResponse = (ValueResponse) this.serviceRequest(Command.ZREMRANGEBYRANK, keybytes, fromBytes, toBytes);
			resp = valueResponse.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a numeric ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resp;
	}

	@Override
	public <K extends Object> List<byte[]> zrange(K key, long from, long to) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(from);
		byte[] toBytes = Convert.toBytes(to);

		List<byte[]> multiBulkData= null;
		try {
			MultiBulkResponse MultiBulkResponse = (MultiBulkResponse) this.serviceRequest(Command.ZRANGE, keybytes, fromBytes, toBytes);
			multiBulkData = MultiBulkResponse.getMultiBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return multiBulkData;
	}

	@Override
	public <K extends Object> List<byte[]> zrevrange(K key, long from, long to) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(from);
		byte[] toBytes = Convert.toBytes(to);

		List<byte[]> multiBulkData= null;
		try {
			MultiBulkResponse MultiBulkResponse = (MultiBulkResponse) this.serviceRequest(Command.ZREVRANGE, keybytes, fromBytes, toBytes);
			multiBulkData = MultiBulkResponse.getMultiBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return multiBulkData;
	}
	@Override
	public <K extends Object> List<ZSetEntry> zrangeSubset(K key, long from, long to) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(from);
		byte[] toBytes = Convert.toBytes(to);

		List<ZSetEntry> list= null;
		try {
			MultiBulkResponse multiBulkResponse = (MultiBulkResponse) this.serviceRequest(Command.ZRANGE$OPTS, keybytes, fromBytes, toBytes, Command.Option.WITHSCORES.bytes);
			List<byte[]> bulkData = multiBulkResponse.getMultiBulkData();
			if(null != bulkData){
				list = new ArrayList<ZSetEntry>(bulkData.size()/2);
				for(int i=0; i<bulkData.size(); i+=2){
					list.add(new ZSetEntryImpl(bulkData.get(i), bulkData.get(i+1)));
				}
			}
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return list;
	}

	@Override
	public <K extends Object> List<ZSetEntry> zrevrangeSubset(K key, long from, long to) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(from);
		byte[] toBytes = Convert.toBytes(to);

		List<ZSetEntry> list= null;
		try {
			MultiBulkResponse multiBulkResponse = (MultiBulkResponse) this.serviceRequest(Command.ZREVRANGE$OPTS, keybytes, fromBytes, toBytes, Command.Option.WITHSCORES.bytes);
			List<byte[]> bulkData = multiBulkResponse.getMultiBulkData();
			if(null != bulkData){
				list = new ArrayList<ZSetEntry>(bulkData.size()/2);
				for(int i=0; i<bulkData.size(); i+=2){
					list.add(new ZSetEntryImpl(bulkData.get(i), bulkData.get(i+1)));
				}
			}
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return list;
	}


	@Override
	public <K extends Object> Sort sort(final K key) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		final JRedisSupport client = this;
//		Sort sortQuery = new SortSupport (key, keybytes) {
		Sort sortQuery = new SortSupport (keybytes) {
			@Override 
			protected List<byte[]> execSort(byte[]... fullSortCmd) 
			throws IllegalStateException, RedisException {
				
				List<byte[]> multiBulkData= null;
				try {
					MultiBulkResponse multiBulkResponse = (MultiBulkResponse) client.serviceRequest(Command.SORT, fullSortCmd);
					multiBulkData = multiBulkResponse.getMultiBulkData();
				}
				catch (ClassCastException e){
					throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
				}
				return multiBulkData;
			}

			protected List<byte[]> execSortStore(byte[]... fullSortCmd) 
			throws IllegalStateException, RedisException {
				
				List<byte[]> multiBulkData= new ArrayList<byte[]>(1);
				try {
					ValueResponse valueResp = (ValueResponse) client.serviceRequest(Command.SORT$STORE, fullSortCmd);
					long resSize = valueResp.getLongValue();
					multiBulkData.add(Convert.toBytes(resSize));
				}
				catch (ClassCastException e){
					throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
				}
				return multiBulkData;
			}

			@Override
	        protected Future<List<byte[]>> execAsyncSort (byte[]... fullSortCmd) {
				throw new IllegalStateException("JRedis does not support asynchronous sort.");
	        }
			@Override
	        protected Future<List<byte[]>> execAsyncSortStore (byte[]... fullSortCmd) {
				throw new IllegalStateException("JRedis does not support asynchronous sort.");
	        }
		};
		return sortQuery;
	}

	/* ------------------------------- commands that don't get a response --------- */

	@Override
	public <K extends Object> void quit()  {
		try {
			this.serviceRequest(Command.QUIT);
		}
		catch (RedisException e) { /* NotConnectedException is OK */
			e.printStackTrace();
			throw new ProviderException ("Quit raised an unexpected RedisException -- Bug");
		}
//		return true;
	}
//	@Override
//	public <K extends Object> void shutdown() {
//		try {
//			this.serviceRequest(Command.SHUTDOWN);
//		}
//		catch (RedisException e) { /* NotConnectedException is OK */
//			e.printStackTrace();
//			throw new ProviderException ("Shutdown raised an unexpected RedisException -- Bug");
//		}
////		return true;
//	}
	@Override
	public <K extends Object> List<byte[]> sinter(K set1, K... sets) throws RedisException {
		byte[] keydata = null;
		if((keydata = getKeyBytes(set1)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+set1+"]");

		byte[][] keybytes = new byte[1+sets.length][];
		int i=0; keybytes[i++] = keydata;
		for(K k : sets) {
			if((keydata = getKeyBytes(k)) == null) 
				throw new IllegalArgumentException ("invalid key => ["+k+"]");
			keybytes[i++] = keydata;
		}
		
		List<byte[]> multiBulkData= null;
		try {
			MultiBulkResponse MultiBulkResponse = (MultiBulkResponse) this.serviceRequest(Command.SINTER, keybytes);
			multiBulkData = MultiBulkResponse.getMultiBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return multiBulkData;
	}

	@Override
	public <K extends Object> List<byte[]> sunion(K set1, K... sets) throws RedisException {
		byte[] keydata = null;
		if((keydata = getKeyBytes(set1)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+set1+"]");

		byte[][] keybytes = new byte[1+sets.length][];
		int i=0; keybytes[i++] = keydata;
		for(K k : sets) {
			if((keydata = getKeyBytes(k)) == null) 
				throw new IllegalArgumentException ("invalid key => ["+k+"]");
			keybytes[i++] = keydata;
		}
		
		List<byte[]> multiBulkData= null;
		try {
			MultiBulkResponse MultiBulkResponse = (MultiBulkResponse) this.serviceRequest(Command.SUNION, keybytes);
			multiBulkData = MultiBulkResponse.getMultiBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return multiBulkData;
	}

	@Override
	public <K extends Object> List<byte[]> sdiff(K set1, K... sets) throws RedisException {
		byte[] keydata = null;
		if((keydata = getKeyBytes(set1)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+set1+"]");

		byte[][] keybytes = new byte[1+sets.length][];
		int i=0; keybytes[i++] = keydata;
		for(K k : sets) {
			if((keydata = getKeyBytes(k)) == null) 
				throw new IllegalArgumentException ("invalid key => ["+k+"]");
			keybytes[i++] = keydata;
		}
		
		List<byte[]> multiBulkData= null;
		try {
			MultiBulkResponse MultiBulkResponse = (MultiBulkResponse) this.serviceRequest(Command.SDIFF, keybytes);
			multiBulkData = MultiBulkResponse.getMultiBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return multiBulkData;
	}

	@Override
	public <K extends Object> void sinterstore(K dest, K... sets) throws RedisException {
		byte[] keydata = null;
		if((keydata = getKeyBytes(dest)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+dest+"]");

		byte[][] setbytes = new byte[1+sets.length][];
		int i=0; 
		setbytes[i++] = keydata;
		byte[] setdata =null;
		for(K k : sets) {
			if((setdata = getKeyBytes(k)) == null) 
				throw new IllegalArgumentException ("invalid key => ["+k+"]");
			setbytes[i++] = setdata;
		}
		
		this.serviceRequest(Command.SINTERSTORE, setbytes);
	}

	@Override
	public <K extends Object> void sunionstore(K dest, K... sets) throws RedisException {
		byte[] keydata = null;
		if((keydata = getKeyBytes(dest)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+dest+"]");

		byte[][] setbytes = new byte[1+sets.length][];
		int i=0; 
		setbytes[i++] = keydata;
		byte[] setdata =null;
		for(K k : sets) {
			if((setdata = getKeyBytes(k)) == null) 
				throw new IllegalArgumentException ("invalid key => ["+k+"]");
			setbytes[i++] = setdata;
		}
		
		this.serviceRequest(Command.SUNIONSTORE, setbytes);
	}

	@Override
	public <K extends Object> void sdiffstore(K dest, K... sets) throws RedisException {
		byte[] keydata = null;
		if((keydata = getKeyBytes(dest)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+dest+"]");

		byte[][] setbytes = new byte[1+sets.length][];
		int i=0; 
		setbytes[i++] = keydata;
		byte[] setdata =null;
		for(K k : sets) {
			if((setdata = getKeyBytes(k)) == null) 
				throw new IllegalArgumentException ("invalid key => ["+k+"]");
			setbytes[i++] = setdata;
		}
		
		this.serviceRequest(Command.SDIFFSTORE, setbytes);
	}

	@Override
	public <K extends Object> long del(K ...keys ) throws RedisException {
		
		if(null == keys || keys.length == 0) throw new IllegalArgumentException("no keys specified");
		byte[] keydata = null;
		byte[][] keybytes = new byte[keys.length][];
		int i=0;
		for(K k : keys) {
			if((keydata = getKeyBytes(k)) == null) 
				throw new IllegalArgumentException ("invalid key => ["+k+"] @ index: " + i);
			
			keybytes[i++] = keydata;
		}

		long resvalue = -1;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.DEL, keybytes);
			resvalue = valResponse.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resvalue;
	}


	@Override
	public <K extends Object> boolean exists(K key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		boolean resvalue = false;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.EXISTS, keybytes);
			resvalue = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resvalue;
	}


	@Override
	public <K extends Object> void lpush(K key, byte[] value) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
		
		if(value == null) 
			throw new IllegalArgumentException ("null value for list op");
		
		
		this.serviceRequest(Command.LPUSH, keybytes, value);
	}
	@Override
	public <K extends Object> void lpush(K key, String value) throws RedisException {
		lpush(key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> void lpush(K key, Number value) throws RedisException {
		lpush(key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> void lpush (K key, T value) throws RedisException
	{
		lpush(key, DefaultCodec.encode(value));
	}
	


	@Override
	public <K extends Object> long lrem(K key, byte[] value, int count) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] countBytes = Convert.toBytes(count);

		long remcnt = 0;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.LREM, keybytes, countBytes, value);
			remcnt = valResponse.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return remcnt;
	}
	@Override
	public <K extends Object> long lrem (K listKey, String value, int count) throws RedisException{
		return lrem (listKey, DefaultCodec.encode(value), count);
	}
	@Override
	public <K extends Object> long lrem (K listKey, Number numberValue, int count) throws RedisException {
		return lrem (listKey, String.valueOf(numberValue).getBytes(), count);
	}
	@Override
	public <K extends Object, T extends Serializable> 
	long lrem (K listKey, T object, int count) throws RedisException{
		return lrem (listKey, DefaultCodec.encode(object), count);
	}


	@Override
	public <K extends Object> void lset(K key, long index, byte[] value) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] indexBytes = Convert.toBytes(index);
		this.serviceRequest(Command.LSET, keybytes, indexBytes, value);
	}
	@Override
	public <K extends Object> void lset (K key, long index, String value) throws RedisException {
		lset (key, index, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> void lset (K key, long index, Number numberValue) throws RedisException{
		lset (key, index, String.valueOf(numberValue).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> void lset (K key, long index, T object) throws RedisException{
		lset (key, index, DefaultCodec.encode(object));
	}

	@Override
	public <K extends Object> boolean move(K key, int dbIndex) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
		
		byte[] toBytes = Convert.toBytes(dbIndex);

		boolean resvalue = false;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.MOVE, keybytes, toBytes);
			resvalue = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resvalue;
	}


	@Override
	public <K extends Object> boolean srem(K key, byte[] member) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		boolean resvalue = false;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.SREM, keybytes, member);
			resvalue = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resvalue;
	}
	@Override
	public <K extends Object> boolean srem (K key, String value) throws RedisException {
		return srem (key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> boolean srem (K key, Number value) throws RedisException {
		return srem (key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> boolean srem (K key, T value) throws RedisException
	{
		return srem (key, DefaultCodec.encode(value));
	}

	@Override
	public <K extends Object> boolean zrem(K key, byte[] member) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		boolean resvalue = false;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.ZREM, keybytes, member);
			resvalue = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resvalue;
	}
	@Override
	public <K extends Object> boolean zrem (K key, String value) throws RedisException {
		return zrem (key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> boolean zrem (K key, Number value) throws RedisException {
		return zrem (key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> boolean zrem (K key, T value) throws RedisException
	{
		return zrem (key, DefaultCodec.encode(value));
	}

	@SuppressWarnings("boxing")
	@Override
	public <K extends Object> Double zscore(K key, byte[] member) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		Double resvalue = null;
		try {
			BulkResponse bulkResponse = (BulkResponse) this.serviceRequest(Command.ZSCORE, keybytes, member);
			if (bulkResponse.getBulkData() != null)
				resvalue = Convert.toDouble(bulkResponse.getBulkData());
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return resvalue;
	}
	@Override
	public <K extends Object> Double zscore (K key, String value) throws RedisException {
		return zscore (key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> Double zscore (K key, Number value) throws RedisException {
		return zscore (key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> Double zscore (K key, T value) throws RedisException
	{
		return zscore (key, DefaultCodec.encode(value));
	}

	@Override
	public <K extends Object> long zrank(K key, byte[] member) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		long resvalue = -1;
		try {
			ValueResponse bulkResponse = (ValueResponse) this.serviceRequest(Command.ZRANK, keybytes, member);
			resvalue = bulkResponse.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return resvalue;
	}
	@Override
	public <K extends Object> long zrank (K key, String value) throws RedisException {
		return zrank (key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> long zrank (K key, Number value) throws RedisException {
		return zrank (key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> long zrank (K key, T value) throws RedisException
	{
		return zrank (key, DefaultCodec.encode(value));
	}

	@Override
	public <K extends Object> long zrevrank(K key, byte[] member) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		long resvalue = -1;
		try {
			ValueResponse bulkResponse = (ValueResponse) this.serviceRequest(Command.ZREVRANK, keybytes, member);
			resvalue = bulkResponse.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return resvalue;
	}
	@Override
	public <K extends Object> long zrevrank (K key, String value) throws RedisException {
		return zrevrank (key, DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> long zrevrank (K key, Number value) throws RedisException {
		return zrevrank (key, String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> long zrevrank (K key, T value) throws RedisException
	{
		return zrevrank (key, DefaultCodec.encode(value));
	}


	@Override
	public <K extends Object> void ltrim(K key, long keepFrom, long keepTo) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(keepFrom);
		byte[] toBytes = Convert.toBytes(keepTo);
		this.serviceRequest(Command.LTRIM, keybytes, fromBytes, toBytes);
	}

	@Override
	public <K extends Object> boolean expire(K key, int ttlseconds) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] ttlbytes = Convert.toBytes(ttlseconds);
		
		boolean resvalue = false;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.EXPIRE, keybytes, ttlbytes);
			resvalue = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resvalue;
	}

	@Override
	public <K extends Object> boolean expireat(K key, long epochtime) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		long expiretime = TimeUnit.SECONDS.convert(epochtime, TimeUnit.MILLISECONDS);
		byte[] expiretimeBytes = Convert.toBytes(expiretime);
		
		boolean resvalue = false;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.EXPIREAT, keybytes, expiretimeBytes);
			resvalue = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resvalue;
	}
	
	@Override
	public <K extends Object> long ttl (K key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		
		long value = Long.MIN_VALUE;
		try {
			ValueResponse valResponse = (ValueResponse) this.serviceRequest(Command.TTL, keybytes);
			value = valResponse.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}
	@Override
	public <K extends Object> byte[] echo (byte[] value) throws RedisException {
		if(value ==null) 
			throw new IllegalArgumentException ("invalid echo value => [null]");

		byte[] bulkData= null;
		try {
			BulkResponse response = (BulkResponse) this.serviceRequest(Command.ECHO, value);
			bulkData = response.getBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return bulkData;
	}
	@Override
	public <K extends Object> byte[] echo(String value) throws RedisException {
		return echo(DefaultCodec.encode(value));
	}
	@Override
	public <K extends Object> byte[] echo(Number value) throws RedisException {
		return echo(String.valueOf(value).getBytes());
	}
	@Override
	public <K extends Object, T extends Serializable> 
	byte[] echo (T value) throws RedisException
	{
		return echo(DefaultCodec.encode(value));
	}
	// ------------------------------------------------------------------------
	// Transactional commands
	// ------------------------------------------------------------------------
	/**
	 * one option is to return a subclass of JRedis (e.g. JRedisCommandSequence)
	 * and have that interface declare discard and multi.  Benefit is being able
	 * to associate state with the transaction.
	 * @throws RedisException
	 */
	@Version(major=2, minor=0, release=Release.ALPHA)
	public <K extends Object> JRedis multi() throws RedisException {
		throw new ProviderException("NOT IMPLEMENTED");
//		// works
//		this.serviceRequest(Command.MULTI);
//		return this;
	}
	/**
	 * @throws RedisException
	 */
	@Version(major=2, minor=0, release=Release.ALPHA)
	public <K extends Object> JRedis discard () throws RedisException {
		throw new ProviderException("NOT IMPLEMENTED");
//		// works
//		this.serviceRequest(Command.DISCARD);
//		return this;
	}
	// ------------------------------------------------------------------------
	// utility
	// ------------------------------------------------------------------------
	
	// TODO: integrate using KeyCodec and a CodecManager at client spec and init time.
	// TODO: (implied) ClientSpec (impls. ConnectionSpec)
	// this isn't cooked yet -- lets think more about the implications...
	// 
//	static final private Map<String, byte[]>	keyByteCache = new ConcurrentHashMap<String, byte[]>();
	public static boolean	CacheKeys	= false;
	
	public static <K extends Object> byte[] getKeyBytes(K key) throws IllegalArgumentException {
		return DefaultKeyCodec.provider().encode(key);
	}
}
