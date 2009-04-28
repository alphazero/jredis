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

import static org.jredis.ri.alphazero.support.DefaultCodec.decode;
import static org.jredis.ri.alphazero.support.DefaultCodec.toLong;
import static org.jredis.ri.alphazero.support.DefaultCodec.toStr;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.jredis.Command;
import org.jredis.JRedis;
import org.jredis.RedisException;
import org.jredis.RedisInfo;
import org.jredis.RedisType;
import org.jredis.ri.JRedisTestSuiteNGBase;
import org.jredis.ri.alphazero.support.Log;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * This class is abstract and it is to remain abstract.
 */
public abstract class JRedisProviderTestNGBase extends JRedisTestSuiteNGBase{

	// ------------------------------------------------------------------------
	// The Tests
	// ========================================================= JRedisClient
	/**
	 * We define and run provider agnostic tests here.  This means we run a set
	 * of JRedis interface method tests that every connected JRedis implementation
	 * should be able to support. 
	 * 
	 * The following commands are omitted:
	 * 1 - QUIT: since we may be testing a multi-connection provier
	 * 2 - SHUTDOWN: for the same reason as QUIT 
	 */
	// ------------------------------------------------------------------------

	/** JRedis Command being tested -- for log info */
	private String test;
	
	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#auth(java.lang.String)}.
	 */
	@Test
	public void testElicitErrors() {
		test = Command.AUTH.code;
		Log.log("TEST: Elicit errors", test);
		try {
			jredis.select(db1).flushdb();
			
			String key = keys.get(0);
			jredis.set(key, smallData);
			boolean expectedError;
			
			// -- commands returning status response 
			expectedError = false;
			try {
				Log.log("Expecting an operation against key holding the wrong kind of value ERROR..");
				jredis.sadd(key, dataList.get(0)); 
			}
			catch (RedisException e) { expectedError = true; }
			assertTrue(expectedError, "should have raised an exception but did not");
			
			// -- commands returning value response 
			expectedError = false;
			try {
				Log.log("Expecting an operation against key holding the wrong kind of value ERROR..");
				jredis.scard(key); 
			}
			catch (RedisException e) { expectedError = true; }
			assertTrue(expectedError, "should have raised an exception but did not");
			
			// -- commands returning bulk response
			expectedError = false;
			try {
				Log.log("Expecting an operation against key holding the wrong kind of value ERROR..");
				jredis.lpop(key); 
			}
			catch (RedisException e) { expectedError = true; }
			assertTrue(expectedError, "should have raised an exception but did not");
			
			
			// -- commands returning multi-bulk response 
			expectedError = false;
			try {
				Log.log("Expecting an operation against key holding the wrong kind of value ERROR..");
				jredis.smembers(key); 
			}
			catch (RedisException e) { expectedError = true; }
			assertTrue(expectedError, "should have raised an exception but did not");
			
			
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}
	
	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#auth(java.lang.String)}.
	 */
	@Test
	public void testAuth() {
		test = Command.AUTH.code;
		Log.log("TEST: %s command", test);
		try {
			jredis.auth(password);
		} 
		catch (RedisException e) {
			fail(test + " with password: " + password, e);
		}
	}
	
	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#ping()}.
	 */
	@Test
	public void testPing() {
		test = Command.PING.code;
		Log.log("TEST: %s command", test);
		try {
			jredis.ping();
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}
	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#expire(java.lang.String, int)}.
	 */
	@Test
	public void testExistsAndExpire() {
		test = Command.EXISTS.code + " | " + Command.EXPIRE.code;
		Log.log("TEST: %s command(s)", test);
		try {
			jredis.select (db1).flushdb();
			assertTrue(jredis.dbsize() == 0);
			
			key = "expire-me";
			
			jredis.set(key, System.currentTimeMillis());
			assertTrue (jredis.exists(key));
			
			jredis.expire(key, expire_secs);
			assertTrue (jredis.exists(key));
			
			// IT SIMPLY WON'T WORK WITHOUT GIVING REDIS A CHANCE
			// could be network latency, or whatever, but the expire command is NOT
			// that precise
			
			Thread.sleep(500);
			assertTrue (jredis.exists(key));
			
			Thread.sleep(this.expire_wait_millisecs);
			assertFalse (jredis.exists(key));
		} 
		catch (RedisException e) {
			fail(test + " with password: " + password, e);
		}
		catch (InterruptedException e) {
			fail (test + "thread was interrupted and test did not conclude" + e.getLocalizedMessage());
		}
	}

// CANT test this without risking hosing the user's DBs
// TODO: use a flag
//	/**
//	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#flushall()}.
//	 */
//	@Test
//	public void testFlushall() {
//		fail("Not yet implemented");
//	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#flushdb()}.
	 */
	@Test
	public void testSelectAndFlushdb() {
		test = 
			Command.SELECT.code + " | " + 
			Command.FLUSHDB.code + " | " +
			Command.SET.code + " | " +
			Command.EXISTS.code + " | " +
			Command.FLUSHDB.code + " | " +
			Command.KEYS.code;
			
		Log.log("TEST: %s commands", test);
		try {
			key = "woof";
			jredis.select(db1).flushdb();
			jredis.set(key, "meow");
			assertTrue (jredis.exists(key));
			jredis.flushdb();
			assertTrue(jredis.keys().size()==0);
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#rename(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testRename() {
		test = Command.RENAME.code;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String newkey = null;
			byte[] value = dataList.get(0);
			key = getRandomAsciiString (random.nextInt(24)+2);
			newkey = getRandomAsciiString (random.nextInt(24)+2);
			
			jredis.set (key, value);
			assertEquals(value, jredis.get(key));
			jredis.rename (key, newkey);
			assertEquals(value, jredis.get(newkey));
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#renamenx(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testRenamenx() {
		test = Command.RENAMENX.code;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();

			jredis.set (keys.get(0), dataList.get(0));
			assertEquals(dataList.get(0), jredis.get(keys.get(0)));

			// should work
			assertTrue(jredis.renamenx (keys.get(0), keys.get(2)));
			assertEquals(dataList.get(0), jredis.get(keys.get(2)));
			
			jredis.flushdb();
			
			// set key1
			jredis.set (keys.get(1), dataList.get(1));
			assertEquals(dataList.get(1), jredis.get(keys.get(1)));
			
			// set key2
			jredis.set (keys.get(2), dataList.get(2));
			assertEquals(dataList.get(2), jredis.get(keys.get(2)));
			
			// rename key1 to key 2 
			// should not
			assertFalse(jredis.renamenx (keys.get(1), keys.get(2)));
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	
	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#save()}.
	 */
	@Test
	public void testSaveAndLastSave() {
		test = Command.SAVE.code + " | " + Command.LASTSAVE;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			jredis.save();
			long when = jredis.lastsave();
			Thread.sleep (this.expire_wait_millisecs);
			jredis.save();
			long when2 = jredis.lastsave();
			assertTrue(when != when2);
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
		catch (InterruptedException e) {
			fail ("thread was interrupted and test did not conclude" + e.getLocalizedMessage());
		}
	}
	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#bgsave()}.
	 */
	@Test
	public void testBgsave() {
		test = Command.BGSAVE.code;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			// TODO: what's a meaningful test for this besides asserting command works?
			jredis.bgsave();
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#set(java.lang.String, byte[])}.
	 */
	@Test
	public void testSetStringByteArray() {
		test = Command.SET.code + " | " + Command.SETNX.code + " byte[] | " + Command.GET;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			jredis.set(keys.get(0), dataList.get(0));
			assertEquals(dataList.get(0), jredis.get(keys.get(0)), "data and get results");
			
			assertTrue(jredis.setnx(keys.get(1), dataList.get(1)), "set key");
			assertNotNull(jredis.get(keys.get(1)));
			assertFalse(jredis.setnx(keys.get(1), dataList.get(2)), "key was already set");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#set(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testSetStringString() {
		test = Command.SET.code + " | " + Command.SETNX.code + " String | " + Command.GET;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			jredis.set(keys.get(0), stringList.get(0));
			assertEquals(stringList.get(0), toStr(jredis.get(keys.get(0))), "string and get results");
			
			assertTrue(jredis.setnx(keys.get(1), stringList.get(1)), "set key");
			assertNotNull(jredis.get(keys.get(1)));
			assertFalse(jredis.setnx(keys.get(1), stringList.get(2)), "key was already set");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#set(java.lang.String, java.lang.Number)}.
	 */
	@Test
	public void testSetStringNumber() {
		test = Command.SET.code + " | " + Command.SETNX.code + " Long | " + Command.GET;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			jredis.set(keys.get(0), longList.get(0));
			assertTrue(longList.get(0).equals(toLong(jredis.get(keys.get(0)))), "long and get results");
			
			assertTrue(jredis.setnx(keys.get(1), longList.get(1)), "set key");
			assertNotNull(jredis.get(keys.get(1)));
			assertFalse(jredis.setnx(keys.get(1), longList.get(2)), "key was already set");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#set(java.lang.String, java.io.Serializable)}.
	 */
	@Test
	public void testSetStringT() {
		test = Command.SET.code + " | " + Command.SETNX.code + " Java Object | " + Command.GET;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			jredis.set(keys.get(0), objectList.get(0));
			assertTrue(objectList.get(0).equals(decode(jredis.get(keys.get(0)))), "object and get results");
			
			assertTrue(jredis.setnx(keys.get(1), objectList.get(1)), "set key");
			assertNotNull(jredis.get(keys.get(1)));
			assertFalse(jredis.setnx(keys.get(1), objectList.get(2)), "key was already set");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	
	
	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#set(java.lang.String, byte[])}.
	 */
	@Test
	public void testGetSetStringByteArray() {
		test = Command.SET.code + " | " + Command.GETSET.code + " byte[] ";
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			jredis.set(keys.get(0), dataList.get(0));
			assertEquals(dataList.get(0), jredis.get(keys.get(0)), "data and get results");
			
			assertEquals (jredis.getset(keys.get(0), dataList.get(1)), dataList.get(0), "getset key");
			
			assertEquals (jredis.get(keys.get(1)), null, "non existent key should be null");
			assertEquals (jredis.getset(keys.get(1), dataList.get(1)), null, "getset on null key should be null");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#set(java.lang.String, java.lang.String)}.
	 */
//	@Test
//	public void testGetSetStringString() {
//		test = Command.SET.code + " | " + Command.GETSET.code + " String ";
//		Log.log("TEST: %s command", test);
//		try {
//			jredis.select(db1).flushdb();
//			
//			jredis.set(keys.get(0), stringList.get(0));
//			assertEquals(stringList.get(0), toStr(jredis.get(keys.get(0))), "string and get results");
//			
//			assertTrue(jredis.setnx(keys.get(1), stringList.get(1)), "set key");
//			assertNotNull(jredis.get(keys.get(1)));
//			assertFalse(jredis.setnx(keys.get(1), stringList.get(2)), "key was already set");
//		} 
//		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
//	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#set(java.lang.String, java.lang.Number)}.
	 */
//	@Test
//	public void testGetSetStringNumber() {
//		test = Command.SET.code + " | " + Command.GETSET.code + " Number ";
//		Log.log("TEST: %s command", test);
//		try {
//			jredis.select(db1).flushdb();
//			
//			jredis.set(keys.get(0), longList.get(0));
//			assertTrue(longList.get(0).equals(toLong(jredis.get(keys.get(0)))), "long and get results");
//			
//			assertTrue(jredis.setnx(keys.get(1), longList.get(1)), "set key");
//			assertNotNull(jredis.get(keys.get(1)));
//			assertFalse(jredis.setnx(keys.get(1), longList.get(2)), "key was already set");
//		} 
//		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
//	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#set(java.lang.String, java.io.Serializable)}.
	 */
//	@Test
//	public void testGetSetStringT() {
//		test = Command.SET.code + " | " + Command.GETSET.code + " Java Object ";
//		Log.log("TEST: %s command", test);
//		try {
//			jredis.select(db1).flushdb();
//			
//			jredis.set(keys.get(0), objectList.get(0));
//			assertTrue(objectList.get(0).equals(decode(jredis.get(keys.get(0)))), "object and get results");
//			
//			assertTrue(jredis.setnx(keys.get(1), objectList.get(1)), "set key");
//			assertNotNull(jredis.get(keys.get(1)));
//			assertFalse(jredis.setnx(keys.get(1), objectList.get(2)), "key was already set");
//		} 
//		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
//	}

	
	

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#incr(java.lang.String)}.
	 */
	@Test
	public void testIncrAndDecr() {
		test = Command.INCR.code + " | " + Command.DECR.code;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			long cntr = 0;
			String cntr_key = keys.get(0);
			
			for(int i = 1; i<MEDIUM_CNT; i++){
				cntr = jredis.incr(cntr_key);
				assertEquals(i, cntr);
			}
			
			for(long i=cntr-1; i>=0; i--){
				cntr = jredis.decr(cntr_key);
				assertEquals(i, cntr);
			}
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#incrby(java.lang.String, int)}.
	 */
	@Test
	public void testIncrbyAndDecrby() {
		test = Command.INCRBY.code + " |" + Command.DECRBY.code;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			long cntr = 0;
			String cntr_key = keys.get(0);

			for(long i = 1; i<MEDIUM_CNT; i++){
				cntr = jredis.incrby(cntr_key, 10);
				assertEquals(i*10, cntr);
			}
			
			jredis.set(cntr_key, 0);
			assertTrue(0 == toLong(jredis.get(cntr_key)));
			for(long i = 1; i<MEDIUM_CNT; i++){
				cntr = jredis.decrby(cntr_key, 10);
				assertEquals(i*-10, cntr);
			}
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#del(java.lang.String)}.
	 */
	@Test
	public void testDel() {
		test = Command.DEL.code;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();

			String key = this.keys.get(0);
			jredis.set (key, dataList.get(0));
			assertTrue (jredis.exists(key));
			
			jredis.del(key);
			assertFalse (jredis.exists(key));
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}
	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#mget(java.lang.String, java.lang.String[])}.
	 */
	@Test
	public void testMget() {
		test = Command.MGET.code ;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();

			for(int i=0; i<SMALL_CNT; i++){
				jredis.set (keys.get(i), dataList.get(i));
			}
			
			List<byte[]>  values = null;
			values = jredis.mget(keys.get(0)); 
			assertEquals(values.size(), 1, "one value expected");
			for(int i=0; i<1; i++)
				assertEquals(values.get(i), dataList.get(i));
			
			values = jredis.mget(keys.get(0), keys.get(1)); 
			assertEquals(values.size(), 2, "2 values expected");
			for(int i=0; i<2; i++)
				assertEquals(values.get(i), dataList.get(i));
			
			values = jredis.mget(keys.get(0), keys.get(1), keys.get(2)); 
			assertEquals(values.size(), 3, "3 values expected");
			for(int i=0; i<3; i++)
				assertEquals(values.get(i), dataList.get(i));
			
			values = jredis.mget("foo", "bar", "paz"); 
			assertEquals(values.size(), 3, "3 values expected");
			for(int i=0; i<3; i++)
				assertEquals(values.get(i), null, "nonexistent key value in list should be null");
			
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	
	/**************** LIST COMMANDS ******************************/

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#rpush(java.lang.String, byte[])}.
	 */
	@Test
	public void testRpushStringByteArray() {
		test = Command.RPUSH.code + " byte[] | " + Command.LLEN + " | " + Command.LRANGE;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();

			String listkey = this.keys.get(0);
			for(int i=0; i<SMALL_CNT; i++){
				jredis.rpush(listkey, dataList.get(i));
			}
			// use LLEN: size should be small count
			assertTrue(jredis.llen(listkey)==SMALL_CNT, "LLEN after RPUSH is wrong");
			
			// use LRANGE 0 cnt: equal size and data should be same in order
			List<byte[]>  range = jredis.lrange(listkey, 0, SMALL_CNT);
			assertTrue(range.size()==SMALL_CNT, "range size after RPUSH is wrong");
			for(int i=0; i<SMALL_CNT; i++){
				assertEquals (dataList.get(i), range.get(i), "range and reference list differ at i: " + i);
			}
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}
	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#lpush(java.lang.String, byte[])}.
	 */
	@Test
	public void testLpushStringByteArray() {
		test = Command.LPUSH.code + " byte[] | " + Command.LLEN + " | " + Command.LRANGE;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();

			String listkey = this.keys.get(0);
			for(int i=0; i<SMALL_CNT; i++){
				jredis.lpush(listkey, dataList.get(i));
			}
			// use LLEN: size should be small count
			assertTrue(jredis.llen(listkey)==SMALL_CNT, "LLEN after RPUSH is wrong");
			
			// use LRANGE 0 cnt: equal size and data should be same in order
			List<byte[]>  range = jredis.lrange(listkey, 0, SMALL_CNT);
			assertTrue(range.size()==SMALL_CNT, "range size after RPUSH is wrong");
			for(int i=0; i<SMALL_CNT; i++){
				int r = SMALL_CNT - i - 1;
				assertEquals (dataList.get(i), range.get(r), "range and reference list differ at i: " + i);
			}
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}


	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#rpush(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testRpushStringString() {
		test = Command.RPUSH.code + " String | " + Command.LLEN + " | " + Command.LRANGE;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();

			String listkey = this.keys.get(0);
			for(int i=0; i<SMALL_CNT; i++){
				jredis.rpush(listkey, stringList.get(i));
			}
			// use LLEN: size should be small count
			assertTrue(jredis.llen(listkey)==SMALL_CNT, "LLEN after RPUSH is wrong");
			
			// use LRANGE 0 cnt: equal size and data should be same in order
			List<byte[]>  range = jredis.lrange(listkey, 0, SMALL_CNT);
			assertTrue(range.size()==SMALL_CNT, "range size after RPUSH is wrong");
			for(int i=0; i<SMALL_CNT; i++){
				assertEquals (stringList.get(i), toStr(range.get(i)), "range and reference list differ at i: " + i);
			}
			List<String>  strRange = toStr(range);
			for(int i=0; i<SMALL_CNT; i++){
				assertEquals (stringList.get(i), strRange.get(i), "range and reference list differ at i: " + i);
			}
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}
	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#lpush(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testLpushStringString() {
		test = Command.LPUSH.code + " String | " + Command.LLEN + " | " + Command.LRANGE;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();

			String listkey = this.keys.get(0);
			for(int i=0; i<SMALL_CNT; i++){
				jredis.lpush(listkey, stringList.get(i));
			}
			// use LLEN: size should be small count
			assertTrue(jredis.llen(listkey)==SMALL_CNT, "LLEN after RPUSH is wrong");
			
			// use LRANGE 0 cnt: equal size and data should be same in order
			List<byte[]>  range = jredis.lrange(listkey, 0, SMALL_CNT);
			assertTrue(range.size()==SMALL_CNT, "range size after RPUSH is wrong");
			for(int i=0; i<SMALL_CNT; i++){
				int r = SMALL_CNT - i - 1;
				assertEquals (stringList.get(i), toStr(range.get(r)), "range and reference list differ at i: " + i);
			}
			List<String>  strRange = toStr(range);
			for(int i=0; i<SMALL_CNT; i++){
				int r = SMALL_CNT - i - 1;
				assertEquals (stringList.get(i), strRange.get(r), "range and reference list differ at i: " + i);
			}
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#rpush(java.lang.String, java.lang.Number)}.
	 */
	@Test
	public void testRpushStringNumber() {
		test = Command.RPUSH.code + " Number | " + Command.LLEN + " | " + Command.LRANGE;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();

			String listkey = this.keys.get(0);
			for(int i=0; i<SMALL_CNT; i++){
				jredis.rpush(listkey, this.longList.get(i));
			}
			// use LLEN: size should be small count
			assertTrue(jredis.llen(listkey)==SMALL_CNT, "LLEN after RPUSH is wrong");
			
			// use LRANGE 0 cnt: equal size and data should be same in order
			List<byte[]>  range = jredis.lrange(listkey, 0, SMALL_CNT);
			assertTrue(range.size()==SMALL_CNT, "range size after RPUSH is wrong");
			for(int i=0; i<SMALL_CNT; i++){
				assertTrue (longList.get(i).equals(toLong(range.get(i))), "range and reference list differ at i: " + i);
			}
			List<Long>  longRange = toLong(range);
			for(int i=0; i<SMALL_CNT; i++){
				assertEquals (longList.get(i), longRange.get(i), "range and reference list differ at i: " + i);
			}
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}
	
	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#lpush(java.lang.String, java.lang.Number)}.
	 */
	@Test
	public void testLpushStringNumber() {
		test = Command.LPUSH.code + " Number | " + Command.LLEN + " | " + Command.LRANGE;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();

			String listkey = this.keys.get(0);
			for(int i=0; i<SMALL_CNT; i++){
				jredis.lpush(listkey, this.longList.get(i));
			}
			// use LLEN: size should be small count
			assertTrue(jredis.llen(listkey)==SMALL_CNT, "LLEN after RPUSH is wrong");
			
			// use LRANGE 0 cnt: equal size and data should be same in order
			List<byte[]>  range = jredis.lrange(listkey, 0, SMALL_CNT);
			assertTrue(range.size()==SMALL_CNT, "range size after RPUSH is wrong");
			for(int i=0; i<SMALL_CNT; i++){
				int r = SMALL_CNT - i - 1;
				assertTrue (longList.get(i).equals(toLong(range.get(r))), "range and reference list differ at i: " + i);
			}
			List<Long>  longRange = toLong(range);
			for(int i=0; i<SMALL_CNT; i++){
				int r = SMALL_CNT - i - 1;
				assertEquals (longList.get(i), longRange.get(r), "range and reference list differ at i: " + i);
			}
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	
	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#rpush(java.lang.String, java.io.Serializable)}.
	 */
	@Test
	public void testRpushStringT() {
		test = Command.RPUSH.code + " Java Object | " + Command.LLEN + " | " + Command.LRANGE;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();

			String listkey = this.keys.get(0);
			for(int i=0; i<SMALL_CNT; i++){
				jredis.rpush(listkey, this.objectList.get(i));
			}
			// use LLEN: size should be small count
			assertTrue(jredis.llen(listkey)==SMALL_CNT, "LLEN after RPUSH is wrong");
			
			// use LRANGE 0 cnt: equal size and data should be same in order
			List<byte[]>  range = jredis.lrange(listkey, 0, SMALL_CNT);
			assertTrue(range.size()==SMALL_CNT, "range size after RPUSH is wrong");
			for(int i=0; i<SMALL_CNT; i++){
				assertTrue (objectList.get(i).equals(decode(range.get(i))), "range and reference list differ at i: " + i);
			}
			List<TestBean>  objRange = decode(range);
			for(int i=0; i<SMALL_CNT; i++){
				assertEquals (objectList.get(i), objRange.get(i), "range and reference list differ at i: " + i);
			}
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#lpush(java.lang.String, java.io.Serializable)}.
	 */
	@Test
	public void testLpushStringT() {
		test = Command.LPUSH.code + " Java Object | " + Command.LLEN + " | " + Command.LRANGE;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();

			String listkey = this.keys.get(0);
			for(int i=0; i<SMALL_CNT; i++){
				jredis.lpush(listkey, this.objectList.get(i));
			}
			// use LLEN: size should be small count
			assertTrue(jredis.llen(listkey)==SMALL_CNT, "LLEN after RPUSH is wrong");
			
			// use LRANGE 0 cnt: equal size and data should be same in order
			List<byte[]>  range = jredis.lrange(listkey, 0, SMALL_CNT);
			assertTrue(range.size()==SMALL_CNT, "range size after RPUSH is wrong");
			for(int i=0; i<SMALL_CNT; i++){
				int r = SMALL_CNT - i - 1;
				assertTrue (objectList.get(i).equals(decode(range.get(r))), "range and reference list differ at i: " + i);
			}
			List<TestBean>  objRange = decode(range);
			for(int i=0; i<SMALL_CNT; i++){
				int r = SMALL_CNT - i - 1;
				assertEquals (objectList.get(i), objRange.get(r), "range and reference list differ at i: " + i);
			}
		} 
		catch (RedisException e) {
			fail(test + " ERROR => " + e.getLocalizedMessage(), e);
		}
	}
	
	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#ltrim(java.lang.String, long, long)}.
	 */
	@Test
	public void testLtrim() {
		test = Command.LTRIM.code + " | " + Command.LLEN.code + " | " + Command.LRANGE.code ;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			// prep a small list
			String listkey = keys.get(0);
			for(int i=0; i<SMALL_CNT; i++)
				jredis.rpush(listkey, dataList.get(i)); // use rpush (append) so ref list sequence order is preserved
			
			// sanity check
			long listcnt = jredis.llen(listkey);
			assertEquals (listcnt, SMALL_CNT, "list length should be SMALL_CNT");
			
			jredis.ltrim(listkey, 0,listcnt-1);	// trim nothing
			assertEquals(jredis.llen(listkey), listcnt, "trim from end to end - no delta expected");
			
			jredis.ltrim(listkey, 1, listcnt-1); 	// remove the head
			assertEquals(jredis.llen(listkey), listcnt-1, "trim head - len should be --1 expected");
			
			listcnt = jredis.llen(listkey);
			assertEquals(listcnt, SMALL_CNT - 1, "list length should be SMALL_CNT - 1");
			for(int i=0; i<SMALL_CNT-1; i++)
				assertEquals(jredis.lindex(listkey, i), dataList.get(i+1), "list items should match ref data shifted by 1 after removing head");
			
			jredis.ltrim(listkey, -2, -1);
			assertEquals(jredis.llen(listkey), 2, "list length should be 2");
			
			jredis.ltrim(listkey, 0, 0);
			assertEquals(jredis.llen(listkey), 1, "list length should be 1");

			byte[] lastItem = jredis.lpop(listkey);
			assertNotNull(lastItem, "last item should not have been null");
			assertEquals(jredis.llen(listkey), 0, "expecting empty list after trims and pop");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#lindex(java.lang.String, long)}.
	 */
	@Test
	public void testLindex() {
		test = Command.LINDEX.code;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			// prep a small list
			String listkey = keys.get(0);
			for(int i=0; i<SMALL_CNT; i++)
				jredis.rpush(listkey, dataList.get(i)); // use rpush (append) so ref list sequence order is preserved
			
			for(int i=0; i<SMALL_CNT; i++)
				assertEquals(jredis.lindex(listkey, i), dataList.get(i), "list items should match ref data");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}
	
//	test = Command.PING.code;
//	Log.log("TEST: %s command", test);
//	try {
//		jredis.ping();
//	} 
//	catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#lpop(java.lang.String)}.
	 */
	@Test
	public void testLpop() {
		test = Command.LPOP.code ;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			// prep a small list
			String listkey = keys.get(0);
			for(int i=0; i<SMALL_CNT; i++)
				jredis.rpush(listkey, dataList.get(i)); // use rpush (append) so ref list sequence order is preserved
			
			// sanity check
			long listcnt = jredis.llen(listkey);
			assertEquals (listcnt, SMALL_CNT, "list length should be SMALL_CNT");
			
			for(int i=0; i<SMALL_CNT; i++)
				assertEquals(jredis.lpop(listkey), dataList.get(i), 
						"nth popped head should be the same as nth dataitem, where n is " + i);
			
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#rpop(java.lang.String)}.
	 */
	@Test
	public void testRpop() {
		test = Command.RPOP.code ;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			// prep a small list
			String listkey = keys.get(0);
			for(int i=0; i<SMALL_CNT; i++)
				jredis.lpush(listkey, dataList.get(i)); // use rpush (append) so ref list sequence order is preserved
			
			// sanity check
			long listcnt = jredis.llen(listkey);
			assertEquals (listcnt, SMALL_CNT, "list length should be SMALL_CNT");
			
			for(int i=0; i<SMALL_CNT; i++)
				assertEquals(jredis.rpop(listkey), dataList.get(i), 
						"nth popped tail should be the same as nth dataitem, where n is " + i);
			
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#lrange(java.lang.String, int, int)}.
	 */
	@Test
	public void testLrange() {
		test = Command.LRANGE.code ;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			// prep a MEDIUM list
			String listkey = keys.get(0);
			for(int i=0; i<MEDIUM_CNT; i++)
				jredis.rpush(listkey, dataList.get(i)); // use rpush (append) so ref list sequence order is preserved
			
			// sanity check
			long listcnt = jredis.llen(listkey);
			assertEquals (listcnt, MEDIUM_CNT, "list length should be MEDIUM_CNT");

			List<byte[]> items = jredis.lrange(listkey, 0, SMALL_CNT-1);
			assertEquals (items.size(), SMALL_CNT, "list range 0->SMALL_CNT length should be SMALL_CNT");
			for(int i=0; i<SMALL_CNT; i++)
				assertEquals(items.get(i), dataList.get(i), 
						"nth items of range 0->CNT should be the same as nth dataitem, where n is " + i);
			
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}
	

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#lrem(java.lang.String, byte[], int)}.
	 */
	@Test
	public void testLremStringByteArrayInt() {
		test = Command.LREM.code + " byte[] | " + Command.LLEN;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();

			String listkey = this.keys.get(0);
			// we'll make a list of unique items first
			for(int i=0; i<MEDIUM_CNT; i++)
				jredis.rpush(listkey, dataList.get(i));
			assertTrue(jredis.llen(listkey)==MEDIUM_CNT, "LLEN after RPUSH is wrong");
			
			// everysingle one of these should work and remove just 1 element
			assertEquals(1, jredis.lrem(listkey, dataList.get(0), 0));
			assertEquals(1, jredis.lrem(listkey, dataList.get(1), -1));
			assertEquals(1, jredis.lrem(listkey, dataList.get(2), 1));
			assertEquals(1, jredis.lrem(listkey, dataList.get(3), 2));
			assertEquals(1, jredis.lrem(listkey, dataList.get(4), -2));
			
			// everysingle one of these should work and remove NOTHING
			assertEquals(0, jredis.lrem(listkey, dataList.get(0), 0));
			assertEquals(0, jredis.lrem(listkey, dataList.get(1), -1));
			assertEquals(0, jredis.lrem(listkey, dataList.get(2), 1));
			assertEquals(0, jredis.lrem(listkey, dataList.get(3), 2));
			assertEquals(0, jredis.lrem(listkey, dataList.get(4), -2));
			
			// now we'll test to see how it handles empty lists
			jredis.flushdb();
			for(int i=0; i<SMALL_CNT; i++)
				jredis.rpush(listkey, dataList.get(i));
			assertTrue(jredis.llen(listkey)==SMALL_CNT, "LLEN after RPUSH is wrong");
			
			for(int i=0; i<SMALL_CNT; i++)
				jredis.lrem(listkey, dataList.get(i), 100);
			assertEquals(0, jredis.llen(listkey), "LLEN should be zero");
			
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#lrem(java.lang.String, java.lang.String, int)}.
	 */
	@Test
	public void testLremStringStringInt() {
		test = Command.LREM.code + " String | " + Command.LLEN;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();

			String listkey = this.keys.get(0);
			// we'll make a list of unique items first
			for(int i=0; i<MEDIUM_CNT; i++)
				jredis.rpush(listkey, stringList.get(i));
			assertTrue(jredis.llen(listkey)==MEDIUM_CNT, "LLEN after RPUSH is wrong");
			
			// everysingle one of these should work and remove just 1 element
			assertEquals(1, jredis.lrem(listkey, stringList.get(0), 0));
			assertEquals(1, jredis.lrem(listkey, stringList.get(1), -1));
			assertEquals(1, jredis.lrem(listkey, stringList.get(2), 1));
			assertEquals(1, jredis.lrem(listkey, stringList.get(3), 2));
			assertEquals(1, jredis.lrem(listkey, stringList.get(4), -2));
			
			// everysingle one of these should work and remove NOTHING
			assertEquals(0, jredis.lrem(listkey, stringList.get(0), 0));
			assertEquals(0, jredis.lrem(listkey, stringList.get(1), -1));
			assertEquals(0, jredis.lrem(listkey, stringList.get(2), 1));
			assertEquals(0, jredis.lrem(listkey, stringList.get(3), 2));
			assertEquals(0, jredis.lrem(listkey, stringList.get(4), -2));
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#lrem(java.lang.String, java.lang.Number, int)}.
	 */
	@Test
	public void testLremStringNumberInt() {
		test = Command.LREM.code + " Number | " + Command.LLEN;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();

			String listkey = this.keys.get(0);
			// we'll make a list of unique items first
			for(int i=0; i<MEDIUM_CNT; i++)
				jredis.rpush(listkey, longList.get(i));
			assertTrue(jredis.llen(listkey)==MEDIUM_CNT, "LLEN after RPUSH is wrong");
			
			// everysingle one of these should work and remove just 1 element
			assertEquals(1, jredis.lrem(listkey, longList.get(0), 0));
			assertEquals(1, jredis.lrem(listkey, longList.get(1), -1));
			assertEquals(1, jredis.lrem(listkey, longList.get(2), 1));
			assertEquals(1, jredis.lrem(listkey, longList.get(3), 2));
			assertEquals(1, jredis.lrem(listkey, longList.get(4), -2));
			
			// everysingle one of these should work and remove NOTHING
			assertEquals(0, jredis.lrem(listkey, longList.get(0), 0));
			assertEquals(0, jredis.lrem(listkey, longList.get(1), -1));
			assertEquals(0, jredis.lrem(listkey, longList.get(2), 1));
			assertEquals(0, jredis.lrem(listkey, longList.get(3), 2));
			assertEquals(0, jredis.lrem(listkey, longList.get(4), -2));
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#lrem(java.lang.String, java.io.Serializable, int)}.
	 */
	@Test
	public void testLremStringTInt() {
		test = Command.LREM.code + " Java Object | " + Command.LLEN;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();

			String listkey = this.keys.get(0);
			// we'll make a list of unique items first
			for(int i=0; i<MEDIUM_CNT; i++)
				jredis.rpush(listkey, objectList.get(i));
			assertTrue(jredis.llen(listkey)==MEDIUM_CNT, "LLEN after RPUSH is wrong");
			
			// everysingle one of these should work and remove just 1 element
			assertEquals(1, jredis.lrem(listkey, objectList.get(0), 0));
			assertEquals(1, jredis.lrem(listkey, objectList.get(1), -1));
			assertEquals(1, jredis.lrem(listkey, objectList.get(2), 1));
			assertEquals(1, jredis.lrem(listkey, objectList.get(3), 2));
			assertEquals(1, jredis.lrem(listkey, objectList.get(4), -2));
			
			// everysingle one of these should work and remove NOTHING
			assertEquals(0, jredis.lrem(listkey, objectList.get(0), 0));
			assertEquals(0, jredis.lrem(listkey, objectList.get(1), -1));
			assertEquals(0, jredis.lrem(listkey, objectList.get(2), 1));
			assertEquals(0, jredis.lrem(listkey, objectList.get(3), 2));
			assertEquals(0, jredis.lrem(listkey, objectList.get(4), -2));
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#lset(java.lang.String, int, byte[])}.
	 */
	@Test
	public void testLsetStringIntByteArray() {
		test = Command.LSET.code + " byte[] | " + Command.LLEN;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();

			String listkey = this.keys.get(0);
			// we'll make a list of unique items first
			for(int i=0; i<SMALL_CNT; i++)
				jredis.rpush(listkey, dataList.get(i));
			assertTrue(jredis.llen(listkey)==SMALL_CNT, "LLEN after RPUSH is wrong");
			
			// now we'll change their values
			for(int i=0; i<SMALL_CNT; i++)
				jredis.lset(listkey, i, dataList.get(SMALL_CNT+i));
			
			List<byte[]> range = null;
			
			range = jredis.lrange(listkey, 0, LARGE_CNT);
			assertEquals (SMALL_CNT, range.size(), "range length is wrong");
			for(int i=0; i<SMALL_CNT; i++)
				assertEquals (dataList.get(SMALL_CNT+i), range.get(i), "after LSET the expected and range item differ at idx: " + i);
			
			// now we'll change their values using the negative index mode
			int lim = SMALL_CNT*-1;
			for(int i=-1; i>lim; i--)
				jredis.lset(listkey, i, dataList.get(i*-1));

			range = jredis.lrange(listkey, 0, LARGE_CNT);
			assertEquals (SMALL_CNT, range.size(), "range length is wrong");
			for(int i=0; i<SMALL_CNT; i++)
				assertEquals (dataList.get(SMALL_CNT-i), range.get(i), "after LSET the expected and range item differ at idx: " + i);
			
			// test edge conditions
			// out of range
			boolean expectedError = false;
			try {
				Log.log("Expecting an out of range ERROR for LSET here ..");
				jredis.lset(listkey, SMALL_CNT, dataList.get(0)); 
			}
			catch (RedisException e) { expectedError = true; }
			assertTrue(expectedError, "out of range LSET index should have raised an exception but did not");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#lset(java.lang.String, int, java.lang.String)}.
	 */
	@Test
	public void testLsetStringIntString() {
		test = Command.LSET.code + " String | " + Command.LLEN;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();

			String listkey = this.keys.get(0);
			// we'll make a list of unique items first
			for(int i=0; i<SMALL_CNT; i++)
				jredis.rpush(listkey, stringList.get(i));
			assertTrue(jredis.llen(listkey)==SMALL_CNT, "LLEN after RPUSH is wrong");
			
			// now we'll change their values
			for(int i=0; i<SMALL_CNT; i++)
				jredis.lset(listkey, i, stringList.get(SMALL_CNT+i));
			
			List<String> range = null;
			
			range = toStr (jredis.lrange(listkey, 0, LARGE_CNT));
			assertEquals (SMALL_CNT, range.size(), "range length is wrong");
			for(int i=0; i<SMALL_CNT; i++)
				assertTrue (stringList.get(SMALL_CNT+i).equals(range.get(i)), "after LSET the expected and range item differ at idx: " + i);
			
			// now we'll change their values using the negative index mode
			int lim = SMALL_CNT*-1;
			for(int i=-1; i>lim; i--)
				jredis.lset(listkey, i, stringList.get(i*-1));

			range = toStr (jredis.lrange(listkey, 0, LARGE_CNT));
			assertEquals (SMALL_CNT, range.size(), "range length is wrong");
			for(int i=0; i<SMALL_CNT; i++)
				assertTrue (stringList.get(SMALL_CNT-i).equals(range.get(i)), "after LSET the expected and range item differ at idx: " + i);
			
			// test edge conditions
			// out of range is same as byte[] as value type makes no difference
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#lset(java.lang.String, int, java.lang.Number)}.
	 */
	@Test
	public void testLsetStringIntNumber() {
		test = Command.LSET.code + " Number | " + Command.LLEN;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();

			String listkey = this.keys.get(0);
			// we'll make a list of unique items first
			for(int i=0; i<SMALL_CNT; i++)
				jredis.rpush(listkey, longList.get(i));
			assertTrue(jredis.llen(listkey)==SMALL_CNT, "LLEN after RPUSH is wrong");
			
			// now we'll change their values
			for(int i=0; i<SMALL_CNT; i++)
				jredis.lset(listkey, i, longList.get(SMALL_CNT+i));
			
			List<Long> range = null;
			
			range = toLong (jredis.lrange(listkey, 0, LARGE_CNT));
			assertEquals (SMALL_CNT, range.size(), "range length is wrong");
			for(int i=0; i<SMALL_CNT; i++)
				assertTrue (longList.get(SMALL_CNT+i).equals(range.get(i)), "after LSET the expected and range item differ at idx: " + i);
			
			// now we'll change their values using the negative index mode
			int lim = SMALL_CNT*-1;
			for(int i=-1; i>lim; i--)
				jredis.lset(listkey, i, longList.get(i*-1));

			range = toLong (jredis.lrange(listkey, 0, LARGE_CNT));
			assertEquals (SMALL_CNT, range.size(), "range length is wrong");
			for(int i=0; i<SMALL_CNT; i++)
				assertTrue (longList.get(SMALL_CNT-i).equals(range.get(i)), "after LSET the expected and range item differ at idx: " + i);
			
			// test edge conditions
			// out of range is same as byte[] as value type makes no difference
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#lset(java.lang.String, int, java.io.Serializable)}.
	 */
	@Test
	public void testLsetStringIntT() {
		test = Command.LSET.code + " Java Object | " + Command.LLEN;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();

			String listkey = this.keys.get(0);
			// we'll make a list of unique items first
			for(int i=0; i<SMALL_CNT; i++)
				jredis.rpush(listkey, objectList.get(i));
			assertTrue(jredis.llen(listkey)==SMALL_CNT, "LLEN after RPUSH is wrong");
			
			// now we'll change their values
			for(int i=0; i<SMALL_CNT; i++)
				jredis.lset(listkey, i, objectList.get(SMALL_CNT+i));
			
			List<TestBean> range = null;
			
			range = decode (jredis.lrange(listkey, 0, LARGE_CNT));
			assertEquals (SMALL_CNT, range.size(), "range length is wrong");
			for(int i=0; i<SMALL_CNT; i++)
				assertTrue (objectList.get(SMALL_CNT+i).equals(range.get(i)), "after LSET the expected and range item differ at idx: " + i);
			
			// now we'll change their values using the negative index mode
			int lim = SMALL_CNT*-1;
			for(int i=-1; i>lim; i--)
				jredis.lset(listkey, i, objectList.get(i*-1));

			range = decode (jredis.lrange(listkey, 0, LARGE_CNT));
			assertEquals (SMALL_CNT, range.size(), "range length is wrong");
			for(int i=0; i<SMALL_CNT; i++)
				assertTrue (objectList.get(SMALL_CNT-i).equals(range.get(i)), "after LSET the expected and range item differ at idx: " + i);
			
			// test edge conditions
			// out of range is same as byte[] as value type makes no difference
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}


	/**************** QUERY COMMANDS ******************************/
	/**
	 * This command is still half-baked on the Redis side, so we just test to see if
	 * it blows up or not.  (cooking:  if you sort on a set/list of size N and your
	 * constrains (GET) limit the actual results to nothing, Redis (0.091) returns a 
	 * list of size N full of nulls.  That's not tasty ..)
	 * 
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#sort(java.lang.String)}.
	 */
	@Test
	public void testSort() {
		test = Command.SORT.code;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey = "set-key";
			String listkey = "list-key";
			for(int i=0; i<MEDIUM_CNT; i++){
				jredis.sadd(setkey, stringList.get(i));
				jredis.lpush(listkey, stringList.get(i));
			}

			List<String> sorted = null;
			
			Log.log("TEST: SORTED LIST ");
//			sorted = toStr(jredis.sort(listkey).ALPHA().LIMIT(0, 100).BY("*A*").exec());
			sorted = toStr(jredis.sort(listkey).ALPHA().LIMIT(0, 555).DESC().exec());
			for(String s : sorted)
				System.out.format("%s\n", s);
			
			Log.log("TEST: SORTED SET ");
//			sorted = toStr(jredis.sort(setkey).ALPHA().LIMIT(0, 100).BY("*BB*").exec());
			sorted = toStr(jredis.sort(setkey).ALPHA().LIMIT(0, 555).DESC().exec());
			for(String s : sorted)
				System.out.format("%s\n", s);
			
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	
	/**************** SET COMMANDS ******************************/
	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#sadd(java.lang.String, byte[])}.
	 */
	@Test
	public void testSaddStringByteArray() {
		test = Command.SADD.code + " byte[]";
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey = keys.get(0);
			for(int i=0;i<SMALL_CNT; i++)
				assertTrue(jredis.sadd(setkey, dataList.get(i)), "sadd of random element should be true");
			
			for(int i=0;i<SMALL_CNT; i++)
				assertFalse(jredis.sadd(setkey, dataList.get(i)), "sadd of existing element should be false");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#sadd(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testSaddStringString() {
		test = Command.SADD.code + " String";
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey = keys.get(0);
			for(int i=0;i<SMALL_CNT; i++)
				assertTrue(jredis.sadd(setkey, stringList.get(i)), "sadd of random element should be true");
			
			for(int i=0;i<SMALL_CNT; i++)
				assertFalse(jredis.sadd(setkey, stringList.get(i)), "sadd of existing element should be false");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#sadd(java.lang.String, java.lang.Number)}.
	 */
	@Test
	public void testSaddStringNumber() {
		test = Command.SADD.code + " Number";
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey = keys.get(0);
			for(int i=0;i<SMALL_CNT; i++)
				assertTrue(jredis.sadd(setkey, longList.get(i)), "sadd of random element should be true");
			
			for(int i=0;i<SMALL_CNT; i++)
				assertFalse(jredis.sadd(setkey, longList.get(i)), "sadd of existing element should be false");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#sadd(java.lang.String, java.io.Serializable)}.
	 */
	@Test
	public void testSaddStringT() {
		test = Command.SADD.code + " Java Object";
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey = keys.get(0);
			for(int i=0;i<SMALL_CNT; i++)
				assertTrue(jredis.sadd(setkey, objectList.get(i)), "sadd of random element should be true");
			
			for(int i=0;i<SMALL_CNT; i++)
				assertFalse(jredis.sadd(setkey, objectList.get(i)), "sadd of existing element should be false");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#smembers(java.lang.String)}.
	 */
	@Test
	public void testSmembers() {
		test = Command.SMEMBERS.code + " byte[] ";
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey = keys.get(0);
			for(int i=0;i<SMALL_CNT; i++)
				assertTrue(jredis.sadd(setkey, dataList.get(i)), "sadd of random element should be true");
			
			List<byte[]> members = null;
			members = jredis.smembers(setkey);
			assertTrue(members.size() == SMALL_CNT);
			// byte[] don't play nice with equals -- values are random so if size matches, its ok
//			for(int i=0;i<SMALL_CNT; i++)
//				assertTrue(members.contains(dataList.get(i)), "set members should include item at idx: " + i);

			// test edget conditions
			// empty set
			jredis.sadd(keys.get(2), dataList.get(0));
			jredis.srem(keys.get(2), dataList.get(0));
			assertTrue(jredis.scard(keys.get(2)) == 0, "set should be empty now");
			members = jredis.smembers(keys.get(2));
			assertNotNull(members, "smembers should return an empty set, not null");
			assertTrue(members.size() == 0, "smembers should have returned an empty list");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }

		test = Command.SMEMBERS.code + " String ";
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey = keys.get(0);
			for(int i=0;i<SMALL_CNT; i++)
				assertTrue(jredis.sadd(setkey, stringList.get(i)), "sadd of random element should be true");
			
			List<String> members = null;
			members = toStr(jredis.smembers(setkey));
			assertTrue(members.size() == SMALL_CNT);

			for(int i=0;i<SMALL_CNT; i++)
				assertTrue(members.contains(stringList.get(i)), "set members should include item at idx: " + i);

			// test edget conditions
			// empty set
			jredis.sadd(keys.get(2), stringList.get(0));
			jredis.srem(keys.get(2), stringList.get(0));
			assertTrue(jredis.scard(keys.get(2)) == 0, "set should be empty now");
			members = toStr(jredis.smembers(keys.get(2)));
			assertNotNull(members, "smembers should return an empty set, not null");
			assertTrue(members.size() == 0, "smembers should have returned an empty list");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }

		test = Command.SMEMBERS.code + " Number ";
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey = keys.get(0);
			for(int i=0;i<SMALL_CNT; i++)
				assertTrue(jredis.sadd(setkey, longList.get(i)), "sadd of random element should be true");
			
			List<Long> members = null;
			members = toLong (jredis.smembers(setkey));
			assertTrue(members.size() == SMALL_CNT);

			for(int i=0;i<SMALL_CNT; i++)
				assertTrue(members.contains(longList.get(i)), "set members should include item at idx: " + i);

			// test edget conditions
			// empty set
			jredis.sadd(keys.get(2), longList.get(0));
			jredis.srem(keys.get(2), longList.get(0));
			assertTrue(jredis.scard(keys.get(2)) == 0, "set should be empty now");
			members = toLong (jredis.smembers(keys.get(2)));
			assertNotNull(members, "smembers should return an empty set, not null");
			assertTrue(members.size() == 0, "smembers should have returned an empty list");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }

		test = Command.SMEMBERS.code + " Java Object ";
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey = keys.get(0);
			for(int i=0;i<SMALL_CNT; i++)
				assertTrue(jredis.sadd(setkey, objectList.get(i)), "sadd of random element should be true");
			
			List<TestBean> members = null;
			members = decode (jredis.smembers(setkey));
			assertTrue(members.size() == SMALL_CNT);

			for(int i=0;i<SMALL_CNT; i++)
				assertTrue(members.contains(objectList.get(i)), "set members should include item at idx: " + i);

			// test edget conditions
			// empty set
			jredis.sadd(keys.get(2), objectList.get(0));
			jredis.srem(keys.get(2), objectList.get(0));
			assertTrue(jredis.scard(keys.get(2)) == 0, "set should be empty now");
			members = decode (jredis.smembers(keys.get(2)));
			assertNotNull(members, "smembers should return an empty set, not null");
			assertTrue(members.size() == 0, "smembers should have returned an empty list");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#sismember(java.lang.String, byte[])}.
	 */
	@Test
	public void testSismemberStringByteArray() {
		test = Command.SISMEMBER.code + " byte[]";
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey = keys.get(0);
			for(int i=0;i<SMALL_CNT; i++)
				assertTrue(jredis.sadd(setkey, dataList.get(i)), "sadd of random element should be true");
			
			for(int i=0;i<SMALL_CNT; i++)
				assertTrue(jredis.sismember(setkey, dataList.get(i)), "should be a member of the set");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#sismember(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testSismemberStringString() {
		test = Command.SISMEMBER.code + " String";
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey = keys.get(0);
			for(int i=0;i<SMALL_CNT; i++)
				assertTrue(jredis.sadd(setkey, stringList.get(i)), "sadd of random element should be true");
			
			for(int i=0;i<SMALL_CNT; i++)
				assertTrue(jredis.sismember(setkey, stringList.get(i)), "should be a member of the set");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#sismember(java.lang.String, java.lang.Number)}.
	 */
	@Test
	public void testSismemberStringNumber() {
		test = Command.SISMEMBER.code + " Number";
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey = keys.get(0);
			for(int i=0;i<SMALL_CNT; i++)
				assertTrue(jredis.sadd(setkey, longList.get(i)), "sadd of random element should be true");
			
			for(int i=0;i<SMALL_CNT; i++)
				assertTrue(jredis.sismember(setkey, longList.get(i)), "should be a member of the set");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#sismember(java.lang.String, java.io.Serializable)}.
	 */
	@Test
	public void testSismemberStringT() {
		test = Command.SISMEMBER.code + " Java Object";
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey = keys.get(0);
			for(int i=0;i<SMALL_CNT; i++)
				assertTrue(jredis.sadd(setkey, objectList.get(i)), "sadd of random element should be true");
			
			for(int i=0;i<SMALL_CNT; i++)
				assertTrue(jredis.sismember(setkey, objectList.get(i)), "should be a member of the set");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}


	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#scard(java.lang.String)}.
	 */
	@Test
	public void testScard() {
		test = Command.SCARD.code + " Java Object";
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey = keys.get(0);
			for(int i=0;i<SMALL_CNT; i++)
				assertTrue(jredis.sadd(setkey, dataList.get(i)), "sadd of random element should be true");
			
			assertEquals (jredis.scard (setkey), SMALL_CNT, "scard should be SMALL_CNT");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#sinter(java.lang.String, java.lang.String[])}.
	 */
	@Test
	public void testSinter() {
		test = Command.SINTER.code;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey1 = keys.get(0);
			String setkey2 = keys.get(1);
			String setunique = keys.get(2);
			for(int i=0;i<SMALL_CNT; i++) {
				assertTrue(jredis.sadd(setkey1, dataList.get(i)), "sadd of random element should be true");
				assertTrue(jredis.sadd(setkey2, dataList.get(i+2)), "sadd of random element should be true");
				assertTrue(jredis.sadd(setunique, dataList.get(10+i+SMALL_CNT)), "sadd of random element should be true");
			}
			assertEquals (0, jredis.sinter(setkey1, setkey2, setunique).size(), "should be no common elements in all three");
			assertTrue (jredis.sinter(setkey1, setkey2).size() > 0, "should be common elements in set 1 and 2");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#sinterstore(java.lang.String, java.lang.String[])}.
	 */
	@Test
	public void testSinterstore() {
		test = Command.SINTERSTORE.code;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey1 = keys.get(0);
			String setkey2 = keys.get(1);
			String setunique = keys.get(2);
			String interset = keys.get(3);
			for(int i=0;i<SMALL_CNT; i++) {
				assertTrue(jredis.sadd(setkey1, dataList.get(i)), "sadd of random element should be true");
				assertTrue(jredis.sadd(setkey2, dataList.get(i+2)), "sadd of random element should be true");
				assertTrue(jredis.sadd(setunique, dataList.get(10+i+SMALL_CNT)), "sadd of random element should be true");
			}
			jredis.sinterstore (interset, setkey1, setkey2, setunique);
			assertEquals (0, jredis.scard(interset), "interset set should be empty");
			jredis.sinterstore (interset, setkey1, setkey2);
			assertTrue (jredis.scard(interset) > 0, "interset set should be non-empty");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}


	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#sunion(java.lang.String, java.lang.String[])}.
	 */
	@Test
	public void testSunion() {
		test = Command.SUNION.code;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey1 = keys.get(0);
			String setkey2 = keys.get(1);
			String setunique = keys.get(2);
			for(int i=0;i<SMALL_CNT; i++) {
				assertTrue(jredis.sadd(setkey1, dataList.get(i)), "sadd of random element should be true");
				assertTrue(jredis.sadd(setkey2, dataList.get(i)), "sadd of random element should be true");
				assertTrue(jredis.sadd(setunique, stringList.get(i)), "sadd of random element should be true");
			}
			assertEquals (SMALL_CNT, jredis.sunion (setkey1, setkey2).size(), "union of equiv sets should have same card as the two");
			assertEquals (SMALL_CNT*2, jredis.sunion (setkey1, setkey2, setunique).size(), "union of all 3 sets should have SMALL_CNT * 2 members");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#sunionstore(java.lang.String, java.lang.String[])}.
	 */
	@Test
	public void testSunionstore() {
		test = Command.SUNIONSTORE.code;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey1 = keys.get(0);
			String setkey2 = keys.get(1);
			String setunique = keys.get(2);
			String union = keys.get(3);
			for(int i=0;i<SMALL_CNT; i++) {
				assertTrue(jredis.sadd(setkey1, dataList.get(i)), "sadd of random element should be true");
				assertTrue(jredis.sadd(setkey2, dataList.get(i)), "sadd of random element should be true");
				assertTrue(jredis.sadd(setunique, stringList.get(i)), "sadd of random element should be true");
			}
			jredis.sunionstore (union, setkey1, setkey2);
			assertEquals (SMALL_CNT, jredis.scard(union), "union of equiv sets should have same card as the two");
			jredis.sunionstore (union, setkey1, setkey2, setunique);
			assertEquals (SMALL_CNT*2, jredis.scard(union), "union of all 3 sets should have SMALL_CNT * 2 members");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}


	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#srem(java.lang.String, byte[])}.
	 */
	@Test
	public void testSremStringByteArray() {
		test = Command.SISMEMBER.code + " byte[]";
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey = keys.get(0);
			for(int i=0;i<SMALL_CNT; i++) 
				assertTrue(jredis.sadd(setkey, dataList.get(i)), "sadd of random element should be true");
			
			for(int i=0;i<SMALL_CNT; i++) 
				assertTrue(jredis.srem(setkey, dataList.get(i)), "should be a removable member of the set");
			
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#srem(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testSremStringString() {
		test = Command.SISMEMBER.code + " String";
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey = keys.get(0);
			for(int i=0;i<SMALL_CNT; i++) 
				assertTrue(jredis.sadd(setkey, stringList.get(i)), "sadd of random element should be true");
			
			for(int i=0;i<SMALL_CNT; i++) 
				assertTrue(jredis.srem(setkey, stringList.get(i)), "should be a removable member of the set");
			
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#srem(java.lang.String, java.lang.Number)}.
	 */
	@Test
	public void testSremStringNumber() {
		test = Command.SISMEMBER.code + " Number";
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey = keys.get(0);
			for(int i=0;i<SMALL_CNT; i++) 
				assertTrue(jredis.sadd(setkey, longList.get(i)), "sadd of random element should be true");
			
			for(int i=0;i<SMALL_CNT; i++) 
				assertTrue(jredis.srem(setkey, longList.get(i)), "should be a removable member of the set");
			
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#srem(java.lang.String, java.io.Serializable)}.
	 */
	@Test
	public void testSremStringT() {
		test = Command.SISMEMBER.code + " Java Object";
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			String setkey = keys.get(0);
			for(int i=0;i<SMALL_CNT; i++) 
				assertTrue(jredis.sadd(setkey, objectList.get(i)), "sadd of random element should be true");
			
			for(int i=0;i<SMALL_CNT; i++) 
				assertTrue(jredis.srem(setkey, objectList.get(i)), "should be a removable member of the set");
			
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}
	
	/************************ DB COMMANDS ***********************/
	
	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#dbsize()}.
	 */
	@Test
	public void testDbsize() {
		test = Command.DBSIZE.code ;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			jredis.flushdb();
			assertTrue (jredis.dbsize() == 0);
			
			for (int i=0; i<SMALL_CNT; i++)
				jredis.set(keys.get(i), dataList.get(i));
			
			assertTrue (jredis.dbsize() == SMALL_CNT, "dbsize should be SMALL_CNT");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#randomkey()}.
	 */
	@Test
	public void testRandomkey() {
		test = Command.RANDOMKEY.code ;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			assertTrue (jredis.dbsize() == 0);
			
			String iamempty = jredis.randomkey();
			assertEquals(0, iamempty.length(), "randomkey of an empty db should be a zero length result");
			
			for (int i=0; i<MEDIUM_CNT; i++)
				jredis.set(keys.get(i), dataList.get(i));
			
			assertTrue (jredis.dbsize() == MEDIUM_CNT, "dbsize should be MEDIUM_CNT");
			for (int i=0; i<SMALL_CNT; i++) {
				assertTrue(keys.contains(jredis.randomkey()), "randomkey should be an item in our keys list");
			}
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}
	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#move(java.lang.String, int)}.
	 */
	@Test
	public void testMove() {
		test = Command.MOVE.code ;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			assertTrue (jredis.dbsize() == 0, "db1 should be empty");
			
			jredis.select(db2).flushdb();
			assertTrue (jredis.dbsize() == 0, "db2 should be empty");
			
			jredis.set(keys.get(0), dataList.get(0));
			assertTrue (jredis.dbsize() == 1, "db2 should have 1 key at this point");
			
			jredis.move(keys.get(0), db1);
			assertTrue (jredis.dbsize() == 0, "db2 should be empty again");
			jredis.select(db1);
			assertTrue (jredis.dbsize() == 1, "db1 should have 1 key at this point");
			
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#type(java.lang.String)}.
	 */
	@Test
	public void testType() {
		test = Command.TYPE.code ;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			jredis.set(keys.get(0), dataList.get(0));
			jredis.sadd(keys.get(1), dataList.get(1));
			jredis.rpush(keys.get(2), dataList.get(2));
			
			assertTrue(jredis.type(keys.get(0))==RedisType.string, "type should be string");
			assertTrue(jredis.type(keys.get(1))==RedisType.set, "type should be set");
			assertTrue(jredis.type(keys.get(2))==RedisType.list, "type should be list");
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#info()}.
	 */
	@Test
	public void testInfo() {
		test = Command.INFO.code ;
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();

			Map<String, String> infoMap =  jredis.info();
			for (RedisInfo info : RedisInfo.values()){
				assertNotNull(infoMap.get(info.name()));
				Log.log("%s => %s", info.name(), infoMap.get(info.name()));
			}
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#keys()}.
	 */
	@Test
	public void testKeys() {
		test = Command.KEYS.code + " (*)";
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			for (int i=0; i<SMALL_CNT; i++)
				jredis.set(keys.get(i), dataList.get(i));

			List<String> rediskeys = jredis.keys();
			assertEquals(SMALL_CNT, rediskeys.size(), "size of key list should be SMALL_CNT");
			for(int i=0; i<SMALL_CNT; i++) 
				assertTrue(rediskeys.contains(keys.get(i)), "should contain " + keys.get(i));
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#keys(java.lang.String)}.
	 */
	@Test
	public void testKeysString() {
		test = Command.KEYS.code + " (using patterns)";
		Log.log("TEST: %s command", test);
		try {
			jredis.select(db1).flushdb();
			
			for (int i=0; i<SMALL_CNT; i++)
				jredis.set(patternList.get(i), dataList.get(i));

			List<String> rediskeys = jredis.keys("*"+patternA+"*");
			assertEquals(SMALL_CNT, rediskeys.size(), "size of key list should be SMALL_CNT");
			for(int i=0; i<SMALL_CNT; i++) 
				assertTrue(rediskeys.contains(patternList.get(i)), "should contain " + patternList.get(i));
		} 
		catch (RedisException e) { fail(test + " ERROR => " + e.getLocalizedMessage(), e); }
	}
//	/**
//	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#shutdown()}.
//	 */
//	@Test
//	public void testShutdown() {
//		fail("Not yet implemented");
//	}
//



	// ========================================================================
	// Test Properties
	// ========================================================================
	
	/** the JRedis implementation being tested */
	protected JRedis jredis = null;
	
	// TODO: does it belong here?  not likely ..
	protected int svc_conn_cnt = 5;
	
	// ------------------------------------------------------------------------
	// General RI Test Suite Parameters with default values to avoid XML
	// ------------------------------------------------------------------------
	protected int	SMALL_DATA =  128;
	protected int	MEDIUM_DATA = 1024 * 2;
	protected int	LARGE_DATA =  1024 * 512;
	protected int	SMALL_CNT = 10;
	protected int	MEDIUM_CNT = 1000;
	protected int	LARGE_CNT = 100000;
	
	protected int	expire_secs = 1;
	protected long	expire_wait_millisecs = 1;
	
	protected final Random random = new Random(System.currentTimeMillis());
	
	// we'll uses these for values 
	protected final List<byte[]>	dataList = new ArrayList<byte[]>();
	protected final List<TestBean>	objectList = new ArrayList<TestBean>();
	protected final List<String>	stringList = new ArrayList<String>();
	protected final List<String>	patternList = new ArrayList<String>();
	protected final List<Integer>	intList = new ArrayList<Integer>();
	protected final List<Long>		longList= new ArrayList<Long>();
	
	protected final Set<String>		uniqueSet = new HashSet<String> ();
	protected final Set<String>		commonSet = new HashSet<String> ();
	protected final Set<String>		set1 = new HashSet<String> ();
	protected final Set<String>		set2 = new HashSet<String> ();
	
	
	protected final String			patternA = "_AAA_";
	
	protected final byte[]			smallData = getRandomBytes(SMALL_DATA);
	protected final byte[]			mediumData = getRandomBytes(MEDIUM_DATA);
	protected final byte[]			largeData = getRandomBytes(LARGE_DATA);
	
	protected final List<String>	keys = new ArrayList<String>();

	private int	 		cnt;
	private String 		key = null;
	
	@SuppressWarnings("unused")
	private byte   		bytevalue;
	
	@SuppressWarnings("unused")
	private String		stringvalue;
	
	@SuppressWarnings("unused")
	private int			intValue;
	
	@SuppressWarnings("unused")
	private long		longValue;
	
	@SuppressWarnings("unused")
	private TestBean 	objectvalue;
	
	
	
	// ========================================================================
	// PREP AND HELPER METHODS
	// ========================================================================
	
	// ------------------------------------------------------------------------
	// General RI Test Suite Parameters init
	// ------------------------------------------------------------------------
	@Parameters({ 
		"jredis.test.datasize.small",
		"jredis.test.datasize.medium",
		"jredis.test.datasize.large",
		"jredis.test.cnt.small",
		"jredis.test.cnt.medium",
		"jredis.test.cnt.large",
		"jredis.test.expire.secs",
		"jredis.test.expire.wait.millisecs",
		
		"jredis.service.connection.cnt"
	})
	@BeforeSuite
	public void providerTestSuiteParametersInit(
			int small_data,
			int medium_data,
			int large_data,
			int small_cnt,
			int medium_cnt,
			int large_cnt,
			int expire_secs,
			int expire_wait_millisecs,
			
			int svc_conn_cnt
		) 
	{
		this.SMALL_DATA = small_data;
		this.MEDIUM_DATA = medium_data;
		this.LARGE_DATA = large_data;
		this.SMALL_CNT = small_cnt;
		this.MEDIUM_CNT = medium_cnt;
		this.LARGE_CNT = large_cnt;
		this.expire_secs = expire_secs;
		this.expire_wait_millisecs = expire_wait_millisecs;
		
		this.svc_conn_cnt = svc_conn_cnt;
		Log.log("TEST-SUITE-PREP: JRedis Provider Test Suite parameters initialized");
		
		setupTestSuiteData();
	}
	

	/**
	 * All providers to be tested with the same degree of test data.
	 * We're using random data and can't guarantee exact teset data.
	 * TODO: flip switch to use random or deterministic data.
	 */
	private final void setupTestSuiteData () {
		/** setup data */
		cnt = MEDIUM_CNT;
		for(int i=0; i<cnt; i++){
			keys.add(getRandomAsciiString (48));
			patternList.add(getRandomAsciiString(random.nextInt(10)+2) + patternA + getRandomAsciiString(random.nextInt(10)+2));
			uniqueSet.add(getRandomAsciiString(48));
			commonSet.add(getRandomAsciiString(48));
			set1.add("set_1" + getRandomAsciiString(20));
			set2.add("set_2" + getRandomAsciiString(20));
			dataList.add(getRandomBytes (128));
			stringList.add(getRandomAsciiString (128));
			objectList.add(new TestBean("testbean." + i));
			intList.add(random.nextInt());
			longList.add(random.nextLong());
		}
		for(String m : commonSet) {
			set1.add(m);
			set2.add(m);
		}
		Log.log("TEST-SUITE-PREP: JRedis Provider Test Suite random test data created");
		
	}
	// ------------------------------------------------------------------------
	// JRedis Provider initialize methods
	// ------------------------------------------------------------------------
	/**
	 * Must be called by a BeforeTest method to set the jredis parameter.
	 * @param jredisProvider that is being tested.
	 */
	protected final void setJRedisProviderInstance (JRedis jredisProvider) {
		this.jredis = jredisProvider;
		Log.log( "TEST: " +
				"\n\t-----------------------------------------------\n" +
				"\tProvider Class: %s" +
				"\n\t-----------------------------------------------\n", 
				jredisProvider.getClass().getCanonicalName());
	}
	// ------------------------------------------------------------------------
	// Helper methods
	// ------------------------------------------------------------------------
	/**
	 * 
	 */
	protected final void prepTestDBs() {
		try {
			jredis.auth(password);
			Log.log("TEST-PREP: AUTH with password %s" + password);
		} 
		catch (RedisException e) {
			Log.error("AUTH with password " + password + " => " + e.getLocalizedMessage());
			fail("AUTH with password: " + password, e);
		}
		try {
			jredis.select(db1).flushdb().select(db2).flushdb().select(db1);
			Log.log("TEST-PREP: %s:%d Redis server DB %d & %d flushed", host, port, db1, db2);
		} 
		catch (RedisException e) {
			Log.error("SELECT/FLUSHDB for test prep" + password);
			fail("SELECT/FLUSHDB for test prep", e);
		}
	}
	/**
	 * Creates a random ascii string
	 * @param length
	 * @return
	 */
	protected String getRandomAsciiString (int length) {
		StringBuilder builder = new  StringBuilder(length);
		for(int i = 0; i<length; i++){
			char c = (char) (random.nextInt(126-33) + 33);
			builder.append(c);
		}
		return builder.toString();
	}
	/**
	 * Creates a buffer of given size filled with random byte values
	 * @param size
	 * @return
	 */
	protected byte[] getRandomBytes(int size) {
		int len = size;
		byte[]	buff = new byte[len];
		random.nextBytes(buff);
		return buff;
	}
	
	// TODO: fix this -- throws assert here
	@Deprecated
	public void compareLists (List<String> strL1, List<String> strL2) {
		assertTrue(strL1.size() == strL2.size(), "un-equal size of the two lists under consideration");
		for(int i=0; i<strL1.size(); i++){
			assertTrue(strL2.contains(strL1.get(i)), "set member equivelance at member " + i);
		}
	}
	
	
	// ------------------------------------------------------------------------
	// INNER TYPES USED FOR TESTING
	// ============================================================== TestBean
	// ------------------------------------------------------------------------
	/**
	 * This is a simple {@link Serializable} class that we use to test object
	 * values.  
	 *
	 * @author  Joubin Houshyar (alphazero@sensesay.net)
	 * @version alpha.0, Apr 18, 2009
	 * @since   alpha.0
	 * 
	 */
	public static class TestBean implements Serializable {
		/**  */
		private static final long	serialVersionUID	= 4457509786469904810L;
		protected final long getCreated_on() {return named_on;}
		protected final void setCreated_on(long created_on) {this.named_on = created_on;}
		protected final String getName() {return name;}
		protected final void setName(String name) {this.name = name;}
		protected final byte[] getData() { return data;}
		protected final void setData(byte[] data) { this.data = data;}
		private long   named_on;
		private String name;
		private byte[] data;
		public TestBean() {
//			named_on = System.currentTimeMillis();
		}
		public TestBean(String string) { 
			this(); name = string;
			named_on = System.currentTimeMillis();
		}
		@Override public String toString() { return "[" + getClass().getSimpleName() + " | name: " + getName() + " created on: " + getCreated_on() + "]"; }
		@Override public boolean equals (Object o) {
			boolean res = false;
			try {
				TestBean isItMe = (TestBean) o;
				res = isItMe.getName().equals(name) && isItMe.getCreated_on()==this.named_on;
			}
			catch (ClassCastException e) {
				return false;
			}
			return res;
		}
	}
}
