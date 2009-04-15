/*
 *   Copyright 2009 Joubin Mohammad Houshyar
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.jredis.JRedis;
import org.jredis.RedisException;
import org.jredis.RedisInfo;
import org.jredis.RedisType;
import org.jredis.ri.alphazero.JRedisClient;
import org.jredis.ri.alphazero.support.Encode;
import org.jredis.ri.alphazero.support.Log;

import junit.framework.TestCase;


/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 14, 2009
 * @since   alpha.0
 * 
 */
public class JRedisClientTest extends TestCase {

	// ------------------------------------------------------------------------
	// TEST SETUP Consts
	// ------------------------------------------------------------------------
	// we'll use some unlikely numbers just in case someone runs this and 
	// their data gets hosed! ..
	
	// TODO: note this in the docs!
	
	/** One of the databases we'll use -- this is #10 */
	static int					DB1 = 10;
	/** One of the databases we'll use -- this is #13 */
	static int					DB2 = 13;

	static int					SMALL_DATA =  128;
	static int					MEDIUM_DATA = 1024 * 2;
	static int					LARGE_DATA =  1024 * 512;
	
	// ------------------------------------------------------------------------
	// TEST KEYS AND DATA
	// ------------------------------------------------------------------------
	private JRedis		redis;
	private String		password;

	final Random random = new Random(System.currentTimeMillis());
	
	// we'll uses these for values 
	final List<byte[]>		dataList = new ArrayList<byte[]>();
	final List<TestBean>	objectList = new ArrayList<TestBean>();
	final List<String>		stringList = new ArrayList<String>();
	final List<String>		patternList = new ArrayList<String>();
	final List<Integer>		intList = new ArrayList<Integer>();
	final List<Long>		longList= new ArrayList<Long>();
	
	final Set<String>		uniqueSet = new HashSet<String> ();
	final Set<String>		commonSet = new HashSet<String> ();
	final Set<String>		set1 = new HashSet<String> ();
	final Set<String>		set2 = new HashSet<String> ();
	
	final int				smallcnt = 10;
	final int				mediumcnt = 1000;
	final int				largecnt = 100000;
	
	final String			patternA = "_AAA_";
	int	 cnt;
	
	final byte[]			smallData = getRandomBytes(SMALL_DATA);
	final byte[]			mediumData = getRandomBytes(MEDIUM_DATA);
	final byte[]			largeData = getRandomBytes(LARGE_DATA);
	
	final List<String>		keys = new ArrayList<String>();

	String 		key = null;
	byte   		bytevalue;
	String		stringvalue;
	int			intValue;
	long		longValue;
	TestBean 	objectvalue;
	
	// ------------------------------------------------------------------------
	// JUNIT 
	// ------------------------------------------------------------------------
	protected void setUp() throws Exception {
		super.setUp();
		
		Log.log("TEST: generating random test data ...");

		/** setup data */
		cnt = mediumcnt;
		for(int i=0; i<cnt; i++){
			keys.add(getRandomString (48));
			patternList.add(getRandomString(random.nextInt(10)+2) + patternA + getRandomString(random.nextInt(10)+2));
			uniqueSet.add(getRandomString(48));
			commonSet.add(getRandomString(48));
			set1.add("set_1" + getRandomString(20));
			set2.add("set_2" + getRandomString(20));
			dataList.add(getRandomBytes (128));
			stringList.add(getRandomString (128));
			objectList.add(new TestBean("testbean." + i));
			intList.add(random.nextInt());
			longList.add(random.nextLong());
		}
		for(String m : commonSet) {
			set1.add(m);
			set2.add(m);
		}
		
		password = "jredis";
		
		/** create client, connect and authorize with Redis + a ping */
		Log.log("TEST: ** NOTE ** creating and authorizing with localhost server (using password %s) ...", password);
		try {
			redis = new JRedisClient().auth(password).ping();
		}
		catch (RedisException e) {
			Log.error("Test setup create - connect - authorize: " + e.getLocalizedMessage());
			throw e;
		}
		
		/** flush our two dbs */
		Log.log("TEST: Flushing the test dbs (using %d and %d) ...", DB1, DB2);
		try {
			redis.select(DB1).flushdb();
			redis.select(DB2).flushdb();
		}
		catch (RedisException e) {
			Log.error("Test setup create - flushing DBs: " + e.getLocalizedMessage());
			throw e;
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		/** just need to quit so connection is closed - a test on its own*/
		try {
			redis.quit();
		}
		catch (Exception e) {
			Log.error("Test tearDown failure: " + e.getLocalizedMessage());
			throw e;
		}
	}
	
	// ------------------------------------------------------------------------
	// *** TESTS *** 
	// EXPIRE
	// ------------------------------------------------------------------------
	
	public void testInit() {
		Log.log("TEST: ing new and basic initial doX() for various permutations ...");

		try {
			JRedis r = new JRedisClient().ping();
			r.quit();
		}
		catch (RedisException e) {
			Log.error("Test setup create - connect - ping: " + e.getLocalizedMessage());
			fail("init failed: " + e.getLocalizedMessage());
		}
		Log.log("TEST: PING");
		
		try {
			JRedis r = new JRedisClient();
			r.smembers("no-such-set");
			r.quit();
		}
		catch (RedisException e) {
			Log.error("Test setup create - connect - smembers: " + e.getLocalizedMessage());
			fail("init failed: " + e.getLocalizedMessage());
		}
		Log.log("TEST: new() -> SMEMBERS on non-exist set ->quit()");
		
		boolean expectedError = false;
		try {
			String not_a_set = "not-a-set";
			JRedis r = new JRedisClient();
			r.set(not_a_set, "a value");
			r.smembers(not_a_set);
		}
		catch (RedisException e) { expectedError = true;}
		finally {
			if(!expectedError) {
				Log.error("Test ");
				fail("Did not raise expected ERR on operating on key of wrong type ");
			}
		}
		Log.log("TEST: new() -> set(key) -> SMEMBERS (key) ->quit()");
		
		try {
			redis = new JRedisClient().auth(password).ping();
		}
		catch (RedisException e) {
			Log.error("Test setup create - connect - authorize: " + e.getLocalizedMessage());
			fail("init failed: " + e.getLocalizedMessage());
		}
		Log.log("TEST: AUTH -> PING");
		
		try {
			redis = new JRedisClient();
			redis.incr("__jredisclienttestkey__");  // sorry, but can't do a select if this test is to be meaningful
		}
		catch (RedisException e) {
			Log.error("Test setup create - connect - authorize: " + e.getLocalizedMessage());
			fail("init failed: " + e.getLocalizedMessage());
		}
	}
	// ------------------------------------------------------------------------
	// *** TESTS *** Commands operating on DBs
	// EXPIRE EXISTS SAVE SAVEBG LASTSAVE
	// ------------------------------------------------------------------------
	
	public void testDBCommands () {
		Log.log("TEST:  expire... this will Thread.sleep for a few secs ...");

		try { redis.select(DB1).flushdb(); }
		catch (RedisException e1) {
			e1.printStackTrace();
			fail("error SELECTing database => " + e1.getLocalizedMessage());
		}
		
		try {
			redis.select (DB1).flushdb();
			assertTrue(redis.dbsize() == 0);
			
			key = "expire-me";
			
			redis.set(key, System.currentTimeMillis());
			assertTrue (redis.exists(key));
			
			redis.expire(key, 1);
			assertTrue (redis.exists(key));
			
			Thread.sleep(500);
			assertTrue (redis.exists(key));
			
			Thread.sleep(2000);
			assertFalse (redis.exists(key));
			
			long when = redis.lastsave();
			redis.save();
			long when2 = redis.lastsave();
			assertTrue(when != when2);
		}
		catch (RedisException e1) { 
			e1.printStackTrace();
			fail("error => " + e1.getLocalizedMessage());
		}
		catch (InterruptedException e) {
			fail ("thread was interrupted and test did not conclude" + e.getLocalizedMessage());
		}
	}
		
	// ------------------------------------------------------------------------
	// *** TESTS *** Commands operating on Lists
	// RPUSH LPUSH LLEN LRANGE TRIM LINDEX LSET LREM LPOP RPOP
	// ------------------------------------------------------------------------
	public void testListCommands () {
		Log.log("TEST:  Commands Operating On Lists...");

		try { redis.select(DB1).flushdb(); }
		catch (RedisException e1) {
			e1.printStackTrace();
			fail("error SELECTing database => " + e1.getLocalizedMessage());
		}
		String key1 = "list1";
		String key2 = "list2";
		try {
			for(String item : stringList){
				redis.lpush (key1, item); 
				redis.rpush (key2, item); 
			}
			Log.log("TEST: RPUSH");
			Log.log("TEST: LPUSH");
			
			List<String>  range1 = Encode.toStr(redis.lrange(key1, 0, Integer.MAX_VALUE));
			List<String>  range2 = Encode.toStr(redis.lrange(key2, 0, Integer.MAX_VALUE));
			assertTrue(range1.size() == stringList.size());
			assertTrue(range2.size() == stringList.size());
			for (String s : stringList) {
				assertTrue(range1.contains(s));
				assertTrue(range2.contains(s));
			}
			for(int i=0; i<stringList.size(); i++) {
				assertTrue (stringList.get(i).equals(range1.get(stringList.size()-i-1)));
				assertTrue (stringList.get(i).equals(range2.get(i)));
			}
			Log.log("TEST: LRANGE");
			
			key = "num-list";
			for(int i=0; i<10; i++) {
				redis.lpush (key, i);
			}
			assertTrue ("LLEN", redis.llen(key)==10);
			assertTrue ("LREM", redis.lrem (key, 2, 0) == 1);
			assertTrue ("LLEN", redis.llen(key)==9);
			
			redis.ltrim(key, 2, 4);
			assertTrue("Testing LTRIM", redis.llen(key)==3);
			Log.log("TEST: LLEN");
			Log.log("TEST: LREM");
			Log.log("TEST: LTRIM");

			redis.lpush(key, "aaaa");
			redis.lpush(key, "bbbb");
			redis.lpush(key, "cccc");
			
			redis.lset(key, 0, "number zero");
			assertTrue("Testint LIndex", Encode.toStr(redis.lindex(key, 0)).equals("number zero"));

			redis.lset(key, 1, "number nine");
			assertTrue("Testint LIndex", Encode.toStr(redis.lindex(key, 1)).equals("number nine"));
			Log.log("TEST: LSET");
			Log.log("TEST: LINDEX");
			
			redis.flushdb();
			
			for(String item : stringList)
				redis.lpush (key1, item); // LPUSH is list.add, RPUSH is list.push 
			
			for(@SuppressWarnings("unused") String foo : stringList)
				redis.lpop(key1);
			
			assertTrue("Testing LPOP", redis.llen(key1)==0);
			
			redis.flushdb();
			for(int i=0; i<100; i++)
				redis.rpush(key1, i);
			
			for(int i=0; i<100; i++)
				assertTrue("Testing RPOP and encode as well", Encode.toInt(redis.rpop(key1)) == 99-i);
			Log.log("TEST: RPOP");
			
			redis.flushdb();
			for(int i=0; i<100; i++)
				redis.lpush(key1, i);
			
			for(int i=0; i<100; i++)
				assertTrue("Testing LPOP and encode as well", Encode.toInt(redis.lpop(key1)) == 99-i);
			Log.log("TEST: LPOP");
			
			Log.log("TEST:  Commands Operating On Lists... PASSED");
		}
		catch (RedisException e1) { 
			e1.printStackTrace();
			fail("error => " + e1.getLocalizedMessage());
		}
		
	}
	// ------------------------------------------------------------------------
	// *** TESTS *** Commands operating on Sets
	// SADD SREM SCARD SISMEMBER SINTER SINTERSTORE SMEMBERS
	// ------------------------------------------------------------------------
	public void testSetCommands () {
		Log.log("TEST:  Commands Operating On Sets...");

		try { redis.select(DB1).flushdb(); }
		catch (RedisException e1) {
			e1.printStackTrace();
			fail("error SELECTing database => " + e1.getLocalizedMessage());
		}
		try {
			String key1 = "set1";
			String key2 = "set2";
			
			String keyunique = "unique";
			for (String member : set1){
				assertTrue(redis.sadd(key1, member));
			}
			assertTrue(redis.scard(key1) == set1.size());
			for (String member : set1){
				assertTrue(redis.sismember(key1, member));
			}
			List<String> redisSet1Members = Encode.toStr(redis.smembers(key1));
			assertTrue(redisSet1Members.size() == set1.size());
			
			for (String member : set2){
				assertTrue(redis.sadd(key2, member));
			}
			assertTrue(redis.scard(key2) == set2.size());
			for (String member : set2){
				assertTrue(redis.sismember(key2, member));
			}
			List<String> redisSet2Members = Encode.toStr(redis.smembers(key2));
			assertTrue(redisSet2Members.size() == set2.size());

			
			for (String member : uniqueSet){
				assertTrue(redis.sadd(keyunique, member));
			}
			
			assertTrue(redis.sinter(keyunique, key1).size() == 0);
			assertTrue(redis.sinter(keyunique, key2).size() == 0);
			assertTrue(redis.sinter(keyunique, key1, key2).size() == 0);
			assertFalse(redis.sinter(keyunique, key1, key2).size() == commonSet.size());
			assertTrue(redis.sinter(key1, key2).size() == commonSet.size());
			
			String storeKey = "common-set-stored";
			redis.sinterstore(storeKey, key1, key2);
			for(String m : commonSet) {
				assertTrue (redis.sismember(storeKey, m));
				assertTrue (redis.srem(key1, m));
			}
			assertTrue(redis.sinter(key1, key2).size() == 0);
			
			redis.flushdb();
			
		}
		catch (RedisException e1) { 
			e1.printStackTrace();
			fail("error => " + e1.getLocalizedMessage());
		}
		
	}
	// ------------------------------------------------------------------------
	// *** TESTS *** Using JAVA OBJECTS -- not exhaustive -- just samply some
	// key redis actions SET GET SADD RPUSH, etc.
	// ------------------------------------------------------------------------
	public void testJavaObjects () {
		Log.log("TEST:  Commands Operating On String Values and Key Space...");

		try { redis.select(DB1).flushdb(); }
		catch (RedisException e1) {
			e1.printStackTrace();
			fail("error SELECTing database => " + e1.getLocalizedMessage());
		}

		try {
			/* as STRING values */
			TestBean redisBean = null;
			for (int i=0; i<objectList.size(); i++){
				redis.set(keys.get(i), objectList.get(i));
				redisBean = Encode.decode(redis.get(keys.get(i)));
				assertEquals(objectList.get(i), redisBean);
			}

			/* as SET values */
			redis.flushdb();
			String objectSet = "test-beans";
			redisBean = null;
			for (int i=0; i<smallcnt; i++){
				redis.sadd(objectSet, objectList.get(i));
			}
			// TODO: redo using semantic interface
//			List<TestBean>  redisBeanList = Encode.decode(redis.smembers(objectSet));
//			for (int i=0; i<smallcnt; i++){
//				assertTrue(redisBeanList.contains(objectList.get(i)));
//			}
			
			/* as LIST values */
			redis.flushdb();
			String listkey = "test-beans";
			redisBean = null;
			for (int i=0; i<smallcnt; i++){
				redis.rpush(listkey, objectList.get(i));
			}
			
			// TODO: redo using semantic interface
//			List<TestBean>  redisRange = Encode.decode(redis.lrange(listkey, 0, smallcnt));
//			for (int i=0; i<smallcnt; i++){
//				assertTrue(redisRange.contains(objectList.get(i)));
//			}
			
		}
		catch (RedisException e1) { 
			e1.printStackTrace();
			fail("error => " + e1.getLocalizedMessage());
		}
	}
	// ------------------------------------------------------------------------
	// *** TESTS *** "STRING" VALUES
	// GET, SET, SETNX, MGET, EXISTS, DEL, TYPE
	// INCR, INCRBY, DECR, DECRBY
	// *** TESTS *** KEY SPACE
	// KEYS, RANDOMKEY, RENAME, RENAMENX, DBSIZE [checked]
	// ------------------------------------------------------------------------
	public void testCommandsOperatingOnStringValues ()  {
		Log.log("TEST:  Commands Operating On String Values and Key Space...");

		try { redis.select(DB1); }
		catch (RedisException e1) {
			e1.printStackTrace();
			fail("error SELECTing database => " + e1.getLocalizedMessage());
		}
		/* SET GET SETNX MGET DEL EXISTS TYPE */
		try {
			byte[]	value;
			byte[]  redisvalue;
			for (int i=0; i<cnt; i++){
				key = keys.get(i);
				value = dataList.get(i);
				
				redis.set(key, value);
				assertTrue(redis.exists(key));
				redisvalue = redis.get(key);
				
				assertTrue(value.length == redisvalue.length);
				for(int j=0; j<value.length; j++)
					assertTrue(value[j]==redisvalue[j]);
			}
			
			assertNotNull(redis.get(key));
			assertFalse(redis.setnx(key, "the key is already set!"));

			assertTrue(redis.del(key));
			assertFalse(redis.del(key));
			assertTrue(redis.setnx(key, "the key is ready to be set again!"));
			
			List<byte[]> mgetBytes = redis.mget(keys.get(random.nextInt(keys.size())), keys.get(random.nextInt(keys.size())), keys.get(random.nextInt(keys.size())));
			for(int i=0; i<mgetBytes.size(); i++)
				assertNotNull(mgetBytes.get(i));
			
			mgetBytes = redis.mget("not_there", keys.get(random.nextInt(keys.size())), keys.get(random.nextInt(keys.size())), keys.get(random.nextInt(keys.size())));
			assertNull(mgetBytes.get(0));
			for(int i=1; i<mgetBytes.size(); i++)
				assertNotNull(mgetBytes.get(i));
			
			mgetBytes = redis.mget("not_there", "not_there", "not_there", "not_there", keys.get(random.nextInt(keys.size())));
			assertNotNull(mgetBytes.get(mgetBytes.size()-1));
			for(int i=0; i<mgetBytes.size()-1; i++)
				assertNull(mgetBytes.get(i));

			redis.flushdb();
			
			redis.set  ("string_key", "bar");
			redis.sadd ("set_key", "member");
			redis.rpush ("list_key", "item");
			assertTrue(redis.type("string_key")==RedisType.string);
			assertTrue(redis.type("set_key")==RedisType.set);
			assertTrue(redis.type("list_key")==RedisType.list);
			
			redis.flushdb();
			
		}
		catch (RedisException e1) { 
			e1.printStackTrace();
			fail("error => " + e1.getLocalizedMessage());
		}
		
		/* INCR INCRBY DECR DECRBY */
		try {
			redis.flushdb();
//			int cntr = 0;
//			String cntr_key = keys.get(0);
//			for(int i = 1; i<mediumcnt; i++){
//				cntr = redis.incr(cntr_key);
//				assertEquals(i, cntr);
//			}
//			for(int i=cntr-1; i>=0; i--){
//				cntr = redis.decr(cntr_key);
//				assertEquals(i, cntr);
//			}
//			assertEquals(0, cntr);
//			for(int i = 1; i<mediumcnt; i++){
//				cntr = redis.incrby(cntr_key, 10);
//				assertEquals(i*10, cntr);
//			}
//			redis.set(cntr_key, 0);
//			assertTrue(0 == Encode.toInt(redis.get(cntr_key)));
//			for(int i = 1; i<mediumcnt; i++){
//				cntr = redis.decrby(cntr_key, 10);
//				assertEquals(i*-10, cntr);
//			}
			long cntr = 0;
			String cntr_key = keys.get(0);
			for(int i = 1; i<mediumcnt; i++){
				cntr = redis.incr(cntr_key);
				assertEquals(i, cntr);
			}
			for(long i=cntr-1; i>=0; i--){
				cntr = redis.decr(cntr_key);
				assertEquals(i, cntr);
			}
			assertEquals(0, cntr);
			for(long i = 1; i<mediumcnt; i++){
				cntr = redis.incrby(cntr_key, 10);
				assertEquals(i*10, cntr);
			}
			redis.set(cntr_key, 0);
			assertTrue(0 == Encode.toInt(redis.get(cntr_key)));
			for(long i = 1; i<mediumcnt; i++){
				cntr = redis.decrby(cntr_key, 10);
				assertEquals(i*-10, cntr);
			}
			
			redis.flushdb();
			
			long value = redis.incrby(cntr_key, Integer.MAX_VALUE - 1);
			for(long i = 0; i < 200; i++)
				value = redis.incr(cntr_key);

			Log.log("using the new long signature -- %d is long", value);
			Log.log("              that's max int -- %d ", Integer.MAX_VALUE);

			
			for(long i = 0; i < 200; i++)
				value = redis.incrby (cntr_key, Integer.MAX_VALUE);
			
			Log.log("using the new long signature -- %d is long", value);
			Log.log("              that's max int -- %d ", Integer.MAX_VALUE);
		}
		catch (RedisException e1) { 
			e1.printStackTrace();
			fail("error => " + e1.getLocalizedMessage());
		}
		/* KEYS RANDOMKEY KEY(PATTERN) RENAME RENAMENX DBSIZE MOVE*/
		String 		key = null;
		try {
			Log.log("TEST:  Commands Operating On Key Space in database %d ...", DB1);
			
			redis.flushdb();

			byte[]	value;
			List<String>	redisKeys = null;
			for (int i=0; i<keys.size(); i++){
				key = keys.get(i);
				value = dataList.get(i);
				redis.set(key, value);
			}
			redisKeys = redis.keys();
			for(int i=0; i<keys.size(); i++) assertTrue(redisKeys.contains(keys.get(i)));

			String newkey = null;
			byte[]  redisvalue = null;
			for (int i=0; i<cnt; i++)
			{
				key = keys.get(i);
				value = dataList.get(i);
				redisvalue = redis.get(key);
				assertTrue(value.length == redisvalue.length);
				for(int j=0; j<value.length; j++)
					assertTrue(value[j]==redisvalue[j]);
				
				newkey = getRandomString (random.nextInt(24)+2);
				redis.rename(key, newkey);
				redisvalue = redis.get(newkey);
				assertTrue(value.length == redisvalue.length);
				for(int j=0; j<value.length; j++)
					assertTrue(value[j]==redisvalue[j]);
			}
			
			assertTrue(redis.renamenx(newkey, key));
			boolean expectedException = false;
			try {
				redis.renamenx(key, key);
			}
			catch (RedisException expected) { expectedException = true; }
			finally {
				assertTrue(expectedException);
			}

			redis.flushdb();
			assertTrue (redis.dbsize() == 0);
			
			for (int i=0; i<patternList.size(); i++){
				key = patternList.get(i);
				value = dataList.get(i);
				redis.set(key, value);
			}
			assertTrue (redis.dbsize() == patternList.size());
			
			redisKeys = redis.keys("*"+patternA+"*");
			for(int i=0; i<keys.size(); i++) 
				assertTrue("missing: " + patternList.get(i) , redisKeys.contains(patternList.get(i)));

			redis.flushdb();
			assertTrue (redis.dbsize() == 0);
			
			/* MOVE */
			try { 
				redis.select(DB1).flushdb();
				assertTrue(redis.dbsize() == 0);
				
				redis.select(DB2).flushdb();
				assertTrue(redis.dbsize() == 0);
			
				// DB2 now ..
				for (int i=0; i<keys.size(); i++){
					key = keys.get(i);
					value = dataList.get(i);
					redis.set(key, value);
				}
				assertTrue(redis.dbsize() == keys.size());
				for (int i=0; i<keys.size(); i++){
					key = keys.get(i);
					redis.move (key, DB1);
				}
				// DB2 should be empty again
				assertTrue(redis.dbsize() == 0);
				
				redis.select(DB1);
				assertTrue(redis.dbsize() == keys.size());
				for (int i=0; i<keys.size(); i++){
					key = keys.get(i);
					assertTrue (redis.exists (key));
				}
				// move them back
				for (int i=0; i<keys.size(); i++){
					key = keys.get(i);
					redis.move (key, DB2);
				}
				assertTrue(redis.dbsize() == 0);
				
				redis.flushall();
				for(int i=0; i<Math.max(DB2, DB1); i++) {
					assertTrue(redis.select(i).dbsize() == 0);
				}
				
				// finally
				Map<String, String> infoMap =  redis.info();
				for (RedisInfo info : RedisInfo.values()){
					assertNotNull(infoMap.get(info.name()));
					Log.log("%s => %s", info.name(), infoMap.get(info.name()));
				}

			}
			catch (RedisException e1) {
				e1.printStackTrace();
				fail("error SELECTing database => " + e1.getLocalizedMessage());
			}
		}
		catch (RedisException e) { 
			e.printStackTrace();
			fail("error testing KEYS  => " + e.getLocalizedMessage());
		}
	}
	
	// ------------------------------------------------------------------------
	// Helper methods
	// ------------------------------------------------------------------------
	protected String getRandomString (int size) {
		StringBuilder builder = new  StringBuilder(size);
		for(int i = 0; i<size; i++){
			char c = (char) (random.nextInt(126-33) + 33);
			builder.append(c);
		}
		return builder.toString();
	}
	protected byte[] getRandomBytes(int size) {
		int len = size;
		byte[]	bigstuff = new byte[len];
		random.nextBytes(bigstuff);
		return bigstuff;
	}
	public void compareLists (List<String> strL1, List<String> strL2) {
		for(int i=0; i<strL1.size(); i++){
			assertTrue(strL2.contains(strL1.get(i)));
		}
	}
	// ------------------------------------------------------------------------
	// INNER TYPES USED FOR TESTING
	// ------------------------------------------------------------------------
	public static class TestBean implements Serializable {
		/**  */
		private static final long	serialVersionUID	= 4457509786469904810L;
		protected final long getCreated_on() {return created_on;}
		protected final void setCreated_on(long created_on) {this.created_on = created_on;}
		protected final String getName() {return name;}
		protected final void setName(String name) {this.name = name;}
		protected final byte[] getData() { return data;}
		protected final void setData(byte[] data) { this.data = data;}
		private long   created_on;
		private String name;
		private byte[] data;
		public TestBean() {created_on = System.currentTimeMillis();}
		public TestBean(String string) { this(); name = string;}
		@Override public String toString() { return "[" + getClass().getSimpleName() + " | name: " + getName() + " created on: " + getCreated_on() + "]"; }
		@Override public boolean equals (Object o) {
			boolean res = false;
			try {
				TestBean isItMe = (TestBean) o;
				res = isItMe.getName().equals(name) && isItMe.getCreated_on()==this.created_on;
			}
			catch (ClassCastException e) {
				return false;
			}
			return res;
		}
	}

}
