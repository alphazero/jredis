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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.jredis.ClientRuntimeException;
import org.jredis.Command;
import org.jredis.JRedis;
import org.jredis.RedisException;
import org.jredis.RedisType;
import org.jredis.Sort;
import org.jredis.connector.BulkResponse;
import org.jredis.connector.Connection;
import org.jredis.connector.MultiBulkResponse;
import org.jredis.connector.ProviderException;
import org.jredis.connector.ValueResponse;
import org.jredis.ri.alphazero.semantics.DefaultCodec;
import org.jredis.ri.alphazero.support.Assert;
import org.jredis.ri.alphazero.support.Convert;
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
	/** No setter or getters for this property - it is initialized at construct time. */
	private Connection	connection;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------
	/**
	 * @param connection
	 */
	protected final void setConnection (Connection connection) {
		this.connection = Assert.notNull(connection, "connection on setConnection()", ClientRuntimeException.class);
	}
	/**
	 * @return
	 */
	protected final Connection getConnection () {
		return this.connection;
	}
	// ------------------------------------------------------------------------
	// INTERFACE
	// ================================================================ Redis
	// ------------------------------------------------------------------------


	@Override
	public JRedis auth(String key) throws RedisException {
		byte[] keydata = null;
		if((keydata = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		connection.serviceRequest(Command.AUTH, keydata);
		
		return this;
	}
	@Override
	public void bgsave() throws RedisException {
		connection.serviceRequest(Command.BGSAVE);
	}
	@Override
	public JRedis ping() throws RedisException {
		connection.serviceRequest(Command.PING);
		return this;
	}

	@Override
	public JRedis flushall() throws RedisException {
		connection.serviceRequest(Command.FLUSHALL).getStatus();
		return this;
	}
	@Override
	public JRedis flushdb() throws RedisException {
		connection.serviceRequest(Command.FLUSHDB).getStatus();
		return this;
	}
	@Override
	public JRedis select(int index) throws RedisException {
		connection.serviceRequest(Command.SELECT, Convert.toBytes(index));
		return this;
	}
	@Override
	public String rename(String oldkey, String newkey) throws RedisException {
		byte[] oldkeydata = null;
		if((oldkeydata = getKeyBytes(oldkey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+oldkey+"]");

		byte[] newkeydata = null;
		if((newkeydata = getKeyBytes(newkey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+newkey+"]");

		connection.serviceRequest(Command.RENAME, oldkeydata, newkeydata);
		return newkey;
	}
	
	@Override
	public boolean renamenx(String oldkey, String newkey) throws RedisException{
		byte[] oldkeydata = null;
		if((oldkeydata = getKeyBytes(oldkey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+oldkey+"]");

		byte[] newkeydata = null;
		if((newkeydata = getKeyBytes(newkey)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+newkey+"]");

		/* boolean ValueRespose */
		boolean value = false;
		try {
			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.RENAMENX, oldkeydata, newkeydata);
			value = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}
	
	@Override
	public void rpush(String key, byte[] value) 
	throws RedisException 
	{
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
		
		connection.serviceRequest(Command.RPUSH, keybytes, value);
	}
	
//	@Override
//	public void rpush(String key, String value) throws RedisException {
//		rpush(key, value.getBytes());
//	}
//	@Override
//	public void rpush(String key, Number value) throws RedisException {
//		rpush(key, String.valueOf(value).getBytes());
//	}
//	@Override
//	public <T extends Serializable> void rpush (String key, T value) throws RedisException
//	{
//		rpush(key, Encode.encode(value));
//	}

	@Override
	public boolean sadd(String key, byte[] member) 
	throws RedisException 
	{
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
		/* boolean ValueRespose */
		boolean res = false;
		try {
			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.SADD, keybytes, member);
			res = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return res;
	}
//	@Override
//	public boolean sadd (String key, String value) throws RedisException {
//		return sadd (key, value.getBytes());
//	}
//	@Override
//	public boolean sadd (String key, Number value) throws RedisException {
//		return sadd (key, String.valueOf(value).getBytes());
//	}
//	@Override
//	public <T extends Serializable> boolean sadd (String key, T value) throws RedisException
//	{
//		return sadd (key, Encode.encode(value));
//	}

	@Override
	public void save() 
	throws RedisException 
	{
		connection.serviceRequest(Command.SAVE);
//		/* boolean ValueRespose */
//		boolean res = false;
//		try {
//			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.SAVE);
//			res = valResponse.getBooleanValue();
//		}
//		catch (ClassCastException e){
//			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
//		}
//		return res;
	}
	
	// -------- set 

	@Override
	public void set(String key, byte[] value) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		connection.serviceRequest(Command.SET, keybytes, value);
	}
//	@Override
//	public void set(String key, String value) throws RedisException {
//		set(key, value.getBytes());
//	}
//	@Override
//	public void set(String key, Number value) throws RedisException {
//		set(key, String.valueOf(value).getBytes());
//	}
//	@Override
//	public <T extends Serializable> void set (String key, T value) throws RedisException
//	{
//		set(key, Encode.encode(value));
//	}
	
	@Override
	public boolean setnx(String key, byte[] value) throws RedisException{
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		boolean resvalue = false;
		try {
			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.SETNX, keybytes, value);
			resvalue = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resvalue;
	}
//	@Override
//	public boolean setnx(String key, String value) throws RedisException {
//		return setnx(key, value.getBytes());
//	}
//	@Override
//	public boolean setnx(String key, Number value) throws RedisException {
//		return setnx(key, String.valueOf(value).getBytes());
//	}
//	@Override
//	public <T extends Serializable> boolean setnx (String key, T value) throws RedisException {
//		return setnx(key, Encode.encode(value));
//	}

	@Override
	public boolean sismember(String key, byte[] member) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		/* boolean ValueRespose */
		boolean value = false;
		try {
			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.SISMEMBER, keybytes, member);
			value = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}

//	@Override
//	public boolean sismember(String key, String stringValue) throws RedisException {
//		return sismember(key, stringValue.getBytes());
//	}
//
//	@Override
//	public boolean sismember(String key, Number numberValue) throws RedisException {
//		return sismember (key, String.valueOf(numberValue).getBytes());
//	}
//
//	@Override
//	public <T extends Serializable> boolean sismember(String key, T object) throws RedisException {
//		return sismember(key, Encode.encode(object));
//	}

	/* ------------------------------- commands returning int value --------- */

	@Override
	public long incr(String key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		/* ValueRespose */
		long value = Long.MIN_VALUE;
		try {
			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.INCR, keybytes);
			value = valResponse.getLongValue();
		}
//		int value = Integer.MIN_VALUE;
//		try {
//			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.INCR, keybytes);
//			value = valResponse.getIntValue();
//		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}

	@Override
	public long incrby(String key, int delta) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		/* ValueRespose */
		long value = Long.MIN_VALUE;
		try {
			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.INCRBY, keybytes, Convert.toBytes(delta));
			value = valResponse.getLongValue();
		}
//		int value = Integer.MIN_VALUE;
//		try {
//			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.INCRBY, keybytes, Convert.toBytes(delta));
//			value = valResponse.getIntValue();
//		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}

	@Override
	public long decr(String key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		/* ValueRespose */
		long value = Long.MAX_VALUE;
		try {
			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.DECR, keybytes);
			value = valResponse.getLongValue();
		}
//		int value = Integer.MIN_VALUE;
//		try {
//			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.DECR, keybytes);
//			value = valResponse.getIntValue();
//		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}

	@Override
	public long decrby(String key, int delta) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		/* ValueRespose */
		long value = Long.MAX_VALUE;
		try {
			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.DECRBY, keybytes, Convert.toBytes(delta));
			value = valResponse.getLongValue();
		}
//		int value = Integer.MIN_VALUE;
//		try {
//			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.DECRBY, keybytes, Convert.toBytes(delta));
//			value = valResponse.getIntValue();
//		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}

	@Override
	public long llen(String key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		/* ValueRespose */ /* int since max size is 1GB, an integer 1,073,741,824 */
		long value = Integer.MIN_VALUE;
		try {
			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.LLEN, keybytes);
			value = valResponse.getLongValue();
		}
//		int value = Integer.MIN_VALUE;
//		try {
//			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.LLEN, keybytes);
//			value = valResponse.getIntValue();
//		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}

	@Override
	public long scard(String key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
		long value = Long.MIN_VALUE;
		try {
			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.SCARD, keybytes);
			value = valResponse.getLongValue();
		}
//		int value = Integer.MIN_VALUE;
//		try {
//			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.SCARD, keybytes);
//			value = valResponse.getIntValue();
//		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}

	/* ------------------------------- commands returning long value --------- */

	@Override
	public long dbsize() throws RedisException {
		/* value response */ /* who knows?  but we'll treat it as a VERY big number .. */
		long value = Long.MIN_VALUE;
		try {
			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.DBSIZE);
			value = valResponse.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}
	
	@Override
	public long lastsave() throws RedisException {
		long value = Long.MIN_VALUE;
		try {
			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.LASTSAVE);
			value = valResponse.getLongValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return value;
	}

	/* ------------------------------- commands returning byte[] --------- */

	@Override
	public byte[] get(String key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] bulkData= null;
		try {
			BulkResponse response = (BulkResponse) connection.serviceRequest(Command.GET, keybytes);
			bulkData = response.getBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return bulkData;
	}

	@Override
	public byte[] lindex(String key, int index) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] bulkData= null;
		try {
			BulkResponse response = (BulkResponse) connection.serviceRequest(Command.LINDEX, keybytes, Convert.toBytes(index));
			bulkData = response.getBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return bulkData;
	}
	@Override
	public byte[] lpop(String key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] bulkData= null;
		try {
			BulkResponse response = (BulkResponse) connection.serviceRequest(Command.LPOP, keybytes);
			bulkData = response.getBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return bulkData;
	}

	@Override
	public byte[] rpop(String key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] bulkData= null;
		try {
			BulkResponse response = (BulkResponse) connection.serviceRequest(Command.RPOP, keybytes);
			bulkData = response.getBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return bulkData;
	}


	/* ------------------------------- commands returning String--------- */

	@Override
	public String randomkey() throws RedisException {
		/* ValueRespose */
		String stringValue = null;
		try {
			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.RANDOMKEY);
			stringValue = valResponse.getStringValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return stringValue;
	}
	@Override
	public RedisType type(String key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		RedisType	type = null;
		/* ValueRespose */
		try {
			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.TYPE, keybytes);
			String stringValue = valResponse.getStringValue();
			type = RedisType.valueOf(stringValue);
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return type;
	}

	/* ------------------------------- commands returning Maps --------- */

	@Override
	public Map<String, String> info() throws RedisException {

		byte[] bulkData= null;
		try {
			BulkResponse response = (BulkResponse) connection.serviceRequest(Command.INFO);
			bulkData = response.getBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a BulkResponse here => " + e.getLocalizedMessage(), e);
		}

		StringTokenizer tokenizer = new StringTokenizer(new String(bulkData), "\r\n");
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

	/* ------------------------------- commands returning Lists --------- */

	@Override
	public List<byte[]> mget(String key, String... moreKeys) throws RedisException {

		byte[] keydata = null;
		if((keydata = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[][] keybytes = new byte[1+moreKeys.length][];
		int i=0; keybytes[i++] = keydata;
		for(String k : moreKeys) {
			if((keydata = getKeyBytes(k)) == null) 
				throw new IllegalArgumentException ("invalid key => ["+k+"]");
			keybytes[i++] = keydata;
		}
		
		List<byte[]> multiBulkData= null;
		try {
			MultiBulkResponse MultiBulkResponse = (MultiBulkResponse) connection.serviceRequest(Command.MGET, keybytes);
			multiBulkData = MultiBulkResponse.getMultiBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return multiBulkData;
	}

	@Override
	public List<byte[]> smembers(String key) throws RedisException {
		byte[] keydata = null;
		if((keydata = getKeyBytes(key)) == null) 
			throw new RedisException (Command.KEYS, "ERR Invalid key.");

		List<byte[]> multiBulkData= null;
		try {
			MultiBulkResponse MultiBulkResponse = (MultiBulkResponse) connection.serviceRequest(Command.SMEMBERS, keydata);
			multiBulkData = MultiBulkResponse.getMultiBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return multiBulkData;
	}
	@Override
	public List<String> keys() throws RedisException {
		return this.keys("*");
	}

	@Override
	public List<String> keys(String pattern) throws RedisException {
		byte[] keydata = null;
		if((keydata = getKeyBytes(pattern)) == null) 
			throw new RedisException (Command.KEYS, "ERR Invalid key.");


		byte[] bulkData= null;
		try {
			BulkResponse response = (BulkResponse) connection.serviceRequest(Command.KEYS, keydata);
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
	}

	@Override
	public List<byte[]> lrange(String key, int from, int to) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(from);
		byte[] toBytes = Convert.toBytes(to);

		List<byte[]> multiBulkData= null;
		try {
			MultiBulkResponse MultiBulkResponse = (MultiBulkResponse) connection.serviceRequest(Command.LRANGE, keybytes, fromBytes, toBytes);
			multiBulkData = MultiBulkResponse.getMultiBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return multiBulkData;
	}

	// TODO: NOTIMPLEMENTED:
	@Override
	public Sort sort(final String key) {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		final JRedisSupport client = this;
		Sort sortQuery = new SortSupport (key, keybytes) {
			@Override 
			protected List<byte[]> execSort(byte[] keyBytes, byte[] sortSpecBytes) 
			throws IllegalStateException, RedisException {
				
				List<byte[]> multiBulkData= null;
				try {
					MultiBulkResponse MultiBulkResponse = (MultiBulkResponse) client.connection.serviceRequest(Command.SORT, keyBytes, sortSpecBytes);
					multiBulkData = MultiBulkResponse.getMultiBulkData();
				}
				catch (ClassCastException e){
					throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
				}
				return multiBulkData;
			}
		};
		return sortQuery;
	}

	/* ------------------------------- commands that don't get a response --------- */

	@Override
	public void quit()  {
		try {
			connection.serviceRequest(Command.QUIT);
		}
		catch (RedisException e) { /* NotConnectedException is OK */
			e.printStackTrace();
			throw new ProviderException ("Quit raised an unexpected RedisException -- Bug");
		}
//		return true;
	}
	@Override
	public void shutdown() {
		try {
			connection.serviceRequest(Command.SHUTDOWN);
		}
		catch (RedisException e) { /* NotConnectedException is OK */
			e.printStackTrace();
			throw new ProviderException ("Shutdown raised an unexpected RedisException -- Bug");
		}
//		return true;
	}
	@Override
	public List<byte[]> sinter(String set1, String... sets) throws RedisException {
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
		
		List<byte[]> multiBulkData= null;
		try {
			MultiBulkResponse MultiBulkResponse = (MultiBulkResponse) connection.serviceRequest(Command.SINTER, keybytes);
			multiBulkData = MultiBulkResponse.getMultiBulkData();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a MultiBulkResponse here => " + e.getLocalizedMessage(), e);
		}
		return multiBulkData;
	}

	@Override
	public void sinterstore(String dest, String... sets) throws RedisException {
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
		
		connection.serviceRequest(Command.SINTERSTORE, setbytes);
	}

//	@Override
//	public void sinterstore(String key, String set1, String... sets) throws RedisException {
//		byte[] keydata = null;
//		if((keydata = getKeyBytes(key)) == null) 
//			throw new IllegalArgumentException ("invalid key => ["+key+"]");
//
//		byte[] set1data = null;
//		if((set1data = getKeyBytes(set1)) == null) 
//			throw new IllegalArgumentException ("invalid key => ["+set1+"]");
//
//		byte[][] setbytes = new byte[2+sets.length][];
//		int i=0; 
//		setbytes[i++] = keydata;
//		setbytes[i++] = set1data;
//		byte[] setdata =null;
//		for(String k : sets) {
//			if((setdata = getKeyBytes(k)) == null) 
//				throw new IllegalArgumentException ("invalid key => ["+k+"]");
//			setbytes[i++] = setdata;
//		}
//		
//		connection.serviceRequest(Command.SINTERSTORE, setbytes);
////		boolean resvalue = false;
////		try {
////			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.SINTERSTORE, setbytes);
////			resvalue = valResponse.getBooleanValue();
////		}
////		catch (ClassCastException e){
////			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
////		}
////		return resvalue;
//	}


	@Override
	public boolean del(String key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		boolean resvalue = false;
		try {
			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.DEL, keybytes);
			resvalue = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resvalue;
	}


	@Override
	public boolean exists(String key) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		boolean resvalue = false;
		try {
			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.EXISTS, keybytes);
			resvalue = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resvalue;
	}


	@Override
	public void lpush(String key, byte[] value) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
		
		connection.serviceRequest(Command.LPUSH, keybytes, value);
	}
//	@Override
//	public void lpush(String key, String value) throws RedisException {
//		lpush(key, value.getBytes());
//	}
//	@Override
//	public void lpush(String key, Number value) throws RedisException {
//		lpush(key, String.valueOf(value).getBytes());
//	}
//	@Override
//	public <T extends Serializable> void lpush (String key, T value) throws RedisException
//	{
//		lpush(key, Encode.encode(value));
//	}
	


	@Override
	public long lrem(String key, byte[] value, int count) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] countBytes = Convert.toBytes(count);

		long remcnt = 0;
		try {
			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.LREM, keybytes, value, countBytes);
			remcnt = valResponse.getLongValue();
		}
//		int remcnt = 0;
//		try {
//			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.LREM, keybytes, value, countBytes);
//			remcnt = valResponse.getIntValue();
//		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return remcnt;
	}
//	@Override
//	public long lrem (String listKey, String stringValue, int count) throws RedisException{
//		return lrem (listKey, stringValue.getBytes(), count);
//	}
//	@Override
//	public long lrem (String listKey, Number numberValue, int count) throws RedisException {
//		return lrem (listKey, String.valueOf(numberValue).getBytes(), count);
//	}
//	@Override
//	public <T extends Serializable> 
//	long lrem (String listKey, T object, int count) throws RedisException{
//		return lrem (listKey, Encode.encode(object), count);
//	}


	@Override
	public void lset(String key, int index, byte[] value) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] indexBytes = Convert.toBytes(index);
		connection.serviceRequest(Command.LSET, keybytes, indexBytes, value);
	}
//	@Override
//	public void lset (String key, int index, String stringValue) throws RedisException {
//		lset (key, index, stringValue.getBytes());
//	}
//	@Override
//	public void lset (String key, int index, Number numberValue) throws RedisException{
//		lset (key, index, String.valueOf(numberValue).getBytes());
//	}
//	@Override
//	public <T extends Serializable> void lset (String key, int index, T object) throws RedisException{
//		lset (key, index, Encode.encode(object));
//	}

	@Override
	public boolean move(String key, int dbIndex) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");
		
		byte[] toBytes = Convert.toBytes(dbIndex);

		boolean resvalue = false;
		try {
			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.MOVE, keybytes, toBytes);
			resvalue = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resvalue;
	}


	@Override
	public boolean srem(String key, byte[] member) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		boolean resvalue = false;
		try {
			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.SREM, keybytes, member);
			resvalue = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resvalue;
	}
//	@Override
//	public boolean srem (String key, String value) throws RedisException {
//		return srem (key, value.getBytes());
//	}
//	@Override
//	public boolean srem (String key, Number value) throws RedisException {
//		return srem (key, String.valueOf(value).getBytes());
//	}
//	@Override
//	public <T extends Serializable> boolean srem (String key, T value) throws RedisException
//	{
//		return srem (key, Encode.encode(value));
//	}


	@Override
	public void ltrim(String key, int keepFrom, int keepTo) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] fromBytes = Convert.toBytes(keepFrom);
		byte[] toBytes = Convert.toBytes(keepTo);
		connection.serviceRequest(Command.LTRIM, keybytes, fromBytes, toBytes);
	}

	@Override
	public boolean expire(String key, int ttlseconds) throws RedisException {
		byte[] keybytes = null;
		if((keybytes = getKeyBytes(key)) == null) 
			throw new IllegalArgumentException ("invalid key => ["+key+"]");

		byte[] ttlbytes = Convert.toBytes(ttlseconds);
		
		boolean resvalue = false;
		try {
			ValueResponse valResponse = (ValueResponse) connection.serviceRequest(Command.EXPIRE, keybytes, ttlbytes);
			resvalue = valResponse.getBooleanValue();
		}
		catch (ClassCastException e){
			throw new ProviderException("Expecting a ValueResponse here => " + e.getLocalizedMessage(), e);
		}
		return resvalue;
	}
	
	// this isn't cooked yet -- lets think more about the implications...
	// 
	@SuppressWarnings("unused")
	static final private Map<String, byte[]>	keyByteCache = new HashMap<String, byte[]>();
	public static final boolean	CacheKeys	= false;
	
	private byte[] getKeyBytes(String key) throws IllegalArgumentException {
		if(null == key) throw new IllegalArgumentException("key is null");
		byte[] bytes = null;
		if(JRedisSupport.CacheKeys == true)
			bytes = keyByteCache.get(key);
		if(null == bytes) {
			bytes = key.getBytes(DefaultCodec.SUPPORTED_CHARSET);
			for(byte b : bytes) {
				if (b == (byte)32 || b == (byte)10 || b == (byte)13)
					throw new IllegalArgumentException ("Key includes invalid byte value: " + (int)b);
			}
			
			if(JRedisSupport.CacheKeys == true)
				keyByteCache.put(key, bytes);
		}

		return bytes;
	}
}
