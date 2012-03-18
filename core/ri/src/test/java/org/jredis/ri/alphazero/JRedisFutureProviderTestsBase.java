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

import static org.jredis.ri.alphazero.support.DefaultCodec.toStr;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.jredis.ClientRuntimeException;
import org.jredis.JRedisFuture;
import org.jredis.ObjectInfo;
import org.jredis.Query;
import org.jredis.RedisException;
import org.jredis.RedisType;
import org.jredis.ZSetEntry;
import org.jredis.protocol.Command;
import org.jredis.protocol.ResponseStatus;
import org.jredis.ri.JRedisTestSuiteBase;
import org.jredis.ri.alphazero.support.Convert;
import org.jredis.ri.alphazero.support.DefaultCodec;
import org.jredis.ri.alphazero.support.Log;
import org.testng.annotations.Test;

/**
 * [TODO: cleanup the unit test comments]
 * 
 * Provides the comprehensive set of tests of all {@link JRedisFuture} methods.
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Oct 10, 2009
 * @since   alpha.0
 * 
 */

public abstract class JRedisFutureProviderTestsBase extends JRedisTestSuiteBase<JRedisFuture> {

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------

	/** JRedis Command being tested -- for log info */
	private String cmd;
	
	// ------------------------------------------------------------------------
	// The Tests
	// ======================================================= JRedisFuture ===
	/*
	 * We define and run provider agnostic tests here.  This means we run a set
	 * of JRedisFuture interface method tests that every connected JRedisFuture
	 * implementation should be able to support. 
	 * 
	 * The following commands are omitted:
	 * 1 - QUIT: since we may be testing a multi-connection provier
	 * 2 - SHUTDOWN: for the same reason as QUIT 
	 * 3 - MOVE and SELECT
	 */
	// ------------------------------------------------------------------------


//	@Test
//	public void testTemplate() throws InterruptedException {
//		cmd = Command.PING.code + " | " + Command.SETNX.code + " byte[] | " + Command.GET;
//		Log.log("TEST: %s command", cmd);
//		
//		try {
//			provider.flushdb();
//			try {
//			}
//			catch(ExecutionException e){
//				Throwable cause = e.getCause();
//				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
//			}
//		} 
//		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
//	}

	@Test
	public void testSrandmember () throws InterruptedException {
		cmd = Command.SRANDMEMBER.code + " String | " + Command.SMEMBERS;
		Log.log("TEST: %s command", cmd);

		try {
			provider.flushdb();
			String setkey = keys.get(0);
			
			List<Future<Boolean>> saddResponses = new ArrayList<Future<Boolean>>();
			for(int i=0;i<SMALL_CNT; i++)
				saddResponses.add (provider.sadd(setkey, stringList.get(i)));
			
			Future<List<byte[]>>  smembersResponse = provider.smembers(setkey);
			Future<byte[]>	      srandmemberResponse = provider.srandmember(setkey);
			
			try {
				
				for(Future<Boolean> resp : saddResponses)
					assertTrue (resp.get().booleanValue(), "sadd of random element should have been true");
				
				List<String>  members = toStr(smembersResponse.get());
				assertEquals(members.size(), SMALL_CNT, "set members count should be SMALL_CNT");
				
				String randmember = toStr(srandmemberResponse.get());
				
				boolean found = false;
				for(String m : members) {
					if(m.equals(randmember)) {
						found = true;
						break;
					}
				}
				assertTrue(found, "random member should have been part of the members list");

				// edge cases
				
				// empty set
				String emptyset = "empty-set";
				provider.sadd(emptyset, "delete-me");
				provider.srem(emptyset, "delete-me");
				assertEquals(provider.smembers(emptyset).get(), Collections.EMPTY_LIST, "smembers should return an empty list");
				assertEquals(provider.srandmember(emptyset).get(), null, "random member of empty set should be null");
				
				// non existent key
				String nonsuch = "no-such-key";
				assertEquals(provider.smembers(nonsuch).get(), Collections.EMPTY_LIST, "members of non existent key set should be empty");
				assertEquals (provider.srandmember(nonsuch).get(), null, "random member of non existent key set should be null");
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}


	@Test
	public void testSmoveStringByteArray() throws InterruptedException{
		cmd = Command.SMOVE.code + " byte[]";
		Log.log("TEST: %s command", cmd);

		provider.flushdb();
		
		String srckey = keys.get(0);
		String destkey = keys.get(1);
		
		List<Future<Boolean>> saddResponses = new ArrayList<Future<Boolean>>();
		for(int i=0;i<MEDIUM_CNT; i++)
			saddResponses.add (provider.sadd(srckey, dataList.get(i)));
		
		List<Future<Boolean>> smoveResponses = new ArrayList<Future<Boolean>>();
		for(int i=0;i<MEDIUM_CNT; i++)
			smoveResponses.add (provider.smove(srckey, destkey, dataList.get(i)));
		
		try {
			try {
				for(Future<Boolean> resp : saddResponses)
					assertTrue (resp.get().booleanValue(), "sadd of random element should have been true");
				
				for(Future<Boolean> resp : smoveResponses)
					assertTrue (resp.get().booleanValue(), "smove of element should have been true");
				
				for(int i=0;i<MEDIUM_CNT; i++){
					assertTrue  (provider.sismember(destkey, dataList.get(i)).get(), "@ [" + i +"] should be a member of the dest after move");
					assertFalse (provider.sismember(srckey, dataList.get(i)).get(), "@ [" + i +"] should NOT be a member of the src after move");
				}
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}

	@Test
	public void testScard() throws InterruptedException {
		cmd = Command.SCARD.code + " Java Object";
		Log.log("TEST: %s command", cmd);

		provider.flushdb();
		
		String setkey = keys.get(0);
	
		List<Future<Boolean>> saddResponses = new ArrayList<Future<Boolean>>();
		for(int i=0;i<MEDIUM_CNT; i++)
			saddResponses.add (provider.sadd(setkey, objectList.get(i)));
		
		Future<Long> scardResp = provider.scard(setkey);
		
		try {
			provider.flushdb();
			try {
				for(Future<Boolean> resp : saddResponses)
					assertTrue (resp.get().booleanValue(), "sadd of random object should have been true");
				
				assertEquals (scardResp.get().longValue(), MEDIUM_CNT, "scard value should have been MEDIUM_CNT");
				
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}

	@Test
	public void testZcard() throws InterruptedException {
		cmd = Command.ZCARD.code + " Java Object";
		Log.log("TEST: %s command", cmd);

		provider.flushdb();
		
		String setkey = keys.get(0);
	
		List<Future<Boolean>> zaddResponses = new ArrayList<Future<Boolean>>();
		for(int i=0;i<MEDIUM_CNT; i++)
			zaddResponses.add (provider.zadd(setkey, i, objectList.get(i)));
		
		Future<Long> zcardResp = provider.zcard(setkey);
		
		try {
			provider.flushdb();
			try {
				for(Future<Boolean> resp : zaddResponses)
					assertTrue (resp.get().booleanValue(), "zadd of random object should have been true");
				
				assertEquals (zcardResp.get().longValue(), MEDIUM_CNT, "zcard value should have been MEDIUM_CNT");
				
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}

	@Test
	public void testSismemberStringByteArray() throws InterruptedException {
		cmd = Command.SISMEMBER.code + " byte[]";
		Log.log("TEST: %s command", cmd);

		String setkey = keys.get(0);
		
		provider.flushdb();
		
		List<Future<Boolean>> saddResponses = new ArrayList<Future<Boolean>>();
		for(int i=0;i<SMALL_CNT; i++)
			saddResponses.add (provider.sadd(setkey, dataList.get(i)));
		
		List<Future<Boolean>> sismemberResponses = new ArrayList<Future<Boolean>>();
		for(int i=0;i<SMALL_CNT; i++)
			saddResponses.add (provider.sismember(setkey, dataList.get(i)));
		
		
		try {
			try {
				for(Future<Boolean> resp : saddResponses)
					assertTrue (resp.get().booleanValue(), "sadd of random element should have been true");
				
				for(Future<Boolean> resp : sismemberResponses)
					assertTrue (resp.get().booleanValue(), "set membership test should have been true");
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	
	@Test
	public void testSmembers() throws InterruptedException{
		cmd = Command.SMEMBERS.code + " byte[] | " + Command.SADD + "| " + Command.SCARD;
		Log.log("TEST: %s command", cmd);

		try {
			provider.flushdb();
			
			String setkey = keys.get(0);
			List<Future<Boolean>> saddResponses = new ArrayList<Future<Boolean>>();
			for(int i=0;i<SMALL_CNT; i++)
				saddResponses.add (provider.sadd(setkey, dataList.get(i)));
			
			Future<Long> scardResp = provider.scard(setkey);
			Future<List<byte[]>> smembersResp = provider.smembers(setkey);			

			String setkey2 = keys.get(2);
			provider.sadd(setkey2, dataList.get(0));
			provider.srem(setkey2, dataList.get(0));
			Future<Long> scardResp2 = provider.scard(setkey2);
			Future<List<byte[]>> smembersResp2 = provider.smembers(setkey2);			
			
			
			try {
				for(Future<Boolean> resp : saddResponses)
					assertTrue (resp.get().booleanValue(), "sadd of random element should have been true");
				
				List<byte[]> members = smembersResp.get();
				assertTrue(members.size() == SMALL_CNT, "smembers should have returned a list of SMALL_CNT size");
				assertTrue(members.size() == scardResp.get().longValue(), "smembers should have returned a list of scard size");
				
				List<byte[]> members2 = smembersResp2.get();
				assertEquals(scardResp2.get().longValue(), 0, "setkey2 should be zero");
				assertEquals(members2, Collections.EMPTY_LIST, "smembers should have returned empty");
//				assertTrue(members2.size() == scardResp2.get().longValue(), "smembers should have returned a list of scard size");
				
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	@Test
	public void testZremStringByteArray() throws InterruptedException{
		cmd = Command.ZADD.code + " byte[] | " + Command.ZREM.code + " byte[]";
		Log.log("TEST: %s command", cmd);

		try {
			provider.flushdb();
			String setkey = keys.get(0);
			List<Future<Boolean>> expectedOKResponses = new ArrayList<Future<Boolean>>();
			for(int i=0;i<SMALL_CNT; i++)
				expectedOKResponses.add (provider.zadd(setkey, i, dataList.get(i)));
			
			List<Future<Boolean>> expectedOKRemResponses = new ArrayList<Future<Boolean>>();
			for(int i=0;i<SMALL_CNT; i++)
				expectedOKRemResponses.add (provider.zrem(setkey, dataList.get(i)));
			

			try {
				for(Future<Boolean> resp : expectedOKResponses)
					assertTrue (resp.get().booleanValue(), "zadd of random element should have been true");
				
				for(Future<Boolean> resp : expectedOKRemResponses)
					assertTrue (resp.get().booleanValue(), "zadd of existing element should have been false");
				
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	@Test
	public void testZaddStringByteArray() throws InterruptedException{
		cmd = Command.ZADD.code + " byte[]";
		Log.log("TEST: %s command", cmd);

		try {
			provider.flushdb();
			String setkey = keys.get(0);
			List<Future<Boolean>> expectedOKResponses = new ArrayList<Future<Boolean>>();
			for(int i=0;i<SMALL_CNT; i++)
				expectedOKResponses.add (provider.zadd(setkey, random.nextDouble(), dataList.get(i)));
			
			List<Future<Boolean>> expectedErrorResponses = new ArrayList<Future<Boolean>>();
			for(int i=0;i<SMALL_CNT; i++)
				expectedErrorResponses.add (provider.zadd(setkey, random.nextDouble(), dataList.get(i)));
			

			try {
				for(Future<Boolean> resp : expectedOKResponses)
					assertTrue (resp.get().booleanValue(), "zadd of random element should have been true");
				
				for(Future<Boolean> resp : expectedErrorResponses)
					assertFalse (resp.get().booleanValue(), "zadd of existing element should have been false");
				
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	@Test
	public void testZscoreAndZincrbyStringByteArray() throws InterruptedException{
		cmd = Command.ZSCORE.code + " byte[] | " + Command.ZINCRBY.code + " byte[]";
		Log.log("TEST: %s command", cmd);

		try {
			provider.flushdb();
			String setkey = keys.get(0);
			List<Future<Boolean>> expectedOKResponses = new ArrayList<Future<Boolean>>();
			for(int i=0;i<SMALL_CNT; i++)
				expectedOKResponses.add (provider.zadd(setkey, doubleList.get(i), dataList.get(i)));
			
			List<Future<Double>> scores = new ArrayList<Future<Double>>();
			for(int i=0;i<SMALL_CNT; i++)
				scores.add (provider.zscore(setkey, dataList.get(i)));
			
			double increment = 0.05;
			List<Future<Double>> incrementedScores = new ArrayList<Future<Double>>();
			for(int i=0;i<SMALL_CNT; i++)
				incrementedScores.add (provider.zincrby(setkey, increment, dataList.get(i)));
			
			Future<Double>  noneSuchKeyScore = provider.zscore(setkey, "no such member");
			try {
				for(Future<Boolean> resp : expectedOKResponses)
					assertTrue (resp.get().booleanValue(), "zadd of random element should have been true");
				
				int i=0;
				for(Future<Double> score : scores){
					assertEquals (score.get(), doubleList.get(i), "zscore of element should have been " + doubleList.get(i));
					i++;
				}	
				
				i=0;
				for(Future<Double> score : incrementedScores){
					assertEquals (score.get(), doubleList.get(i) + increment, "zincr of element should be " + doubleList.get(i) + increment);
					i++;
				}	
				
				assertNull(noneSuchKeyScore.get(), "zscore of none existent member of sorted set should be null");
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	
	@Test
	public void testZrangebyscoreStringByteArray() throws InterruptedException{
		cmd = Command.ZRANGEBYSCORE.code + " byte[] | " + Command.ZSCORE.code + " byte[]";
		Log.log("TEST: %s command", cmd);

		try {
			provider.flushdb();
			String setkey = keys.get(0);
			List<Future<Boolean>> expectedOKResponses = new ArrayList<Future<Boolean>>();
			for(int i=0;i<MEDIUM_CNT; i++)
				expectedOKResponses.add (provider.zadd(setkey, i, dataList.get(i)));
			
			Future<List<byte[]>> frRange = provider.zrangebyscore(setkey, 0, SMALL_CNT); 
			try {
				for(Future<Boolean> resp : expectedOKResponses)
					assertTrue (resp.get().booleanValue(), "zadd of random element should have been true");
				
				List<byte[]> range = frRange.get();
				assertTrue(range.size() > 0, "should have non empty results for range by score here");
				
				for(int i=0;i<SMALL_CNT-1; i++){
					assertEquals(range.get(i), dataList.get(i), "expected value in the range by score missing");
				}
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}

	@Test
	public void testZremrangebyscoreStringByteArray() throws InterruptedException{
		cmd = Command.ZREMRANGEBYSCORE.code + " byte[] | " + Command.ZSCORE.code + " byte[]";
		Log.log("TEST: %s command", cmd);

		try {
			provider.flushdb();
			String setkey = keys.get(0);
			List<Future<Boolean>> expectedOKResponses = new ArrayList<Future<Boolean>>();
			for(int i=0;i<MEDIUM_CNT; i++)
				expectedOKResponses.add (provider.zadd(setkey, i, dataList.get(i)));
			
			Future<Long> frRemCnt = provider.zremrangebyscore(setkey, 0, SMALL_CNT); 
			try {
				for(Future<Boolean> resp : expectedOKResponses)
					assertTrue (resp.get().booleanValue(), "zadd of random element should have been true");
				
				long remCnt = frRemCnt.get().longValue();
				assertTrue(remCnt > 0, "should have non-zero number of rem cnt for zremrangebyscore");
				assertEquals(remCnt, SMALL_CNT+1, "should have specific number of rem cnt for zremrangebyscore");
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}

	@Test
	public void testZcountStringByteArray() throws InterruptedException{
		cmd = Command.ZCOUNT.code + " byte[] | " + Command.ZADD.code + " byte[]";
		Log.log("TEST: %s command", cmd);

		try {
			provider.flushdb();
			String setkey = keys.get(0);
			List<Future<Boolean>> expectedOKResponses = new ArrayList<Future<Boolean>>();
			for(int i=0;i<MEDIUM_CNT; i++)
				expectedOKResponses.add (provider.zadd(setkey, i, dataList.get(i)));
			
			Future<Long> frCount = provider.zcount(setkey, 0, SMALL_CNT); 
			try {
				for(Future<Boolean> resp : expectedOKResponses)
					assertTrue (resp.get().booleanValue(), "zadd of random element should have been true");
				
				long remCnt = frCount.get().longValue();
				assertTrue(remCnt > 0, "should have non-zero number of rem cnt for zremrangebyscore");
				assertEquals(remCnt, SMALL_CNT+1, "should have specific number for zcount");
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	
	@Test
	public void testZremrangebyrankStringByteArray() throws InterruptedException{
		cmd = Command.ZREMRANGEBYRANK.code + " byte[] | " + Command.ZSCORE.code + " byte[]";
		Log.log("TEST: %s command", cmd);

		try {
			provider.flushdb();
			String setkey = keys.get(0);
			List<Future<Boolean>> expectedOKResponses = new ArrayList<Future<Boolean>>();
			for(int i=0;i<MEDIUM_CNT; i++)
				expectedOKResponses.add (provider.zadd(setkey, i, dataList.get(i)));
			
			Future<Long> frRemCnt = provider.zremrangebyrank(setkey, 0, SMALL_CNT); 
			try {
				for(Future<Boolean> resp : expectedOKResponses)
					assertTrue (resp.get().booleanValue(), "zadd of random element should have been true");
				
				long remCnt = frRemCnt.get().longValue();
				assertTrue(remCnt > 0, "should have non-zero number of rem cnt for zremrangebyrank");
				assertEquals(remCnt, SMALL_CNT+1, "should have specific number of rem cnt for zremrangebyrank");
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	
	@Test
	public void testZrankStringByteArray() throws InterruptedException{
		cmd = Command.ZRANK.code;
		Log.log("TEST: %s command", cmd);

		try {
			provider.flushdb();
			String setkey = keys.get(0);
			
			List<Future<Boolean>> expectedOKResponses = new ArrayList<Future<Boolean>>();
			for(int i=0;i<SMALL_CNT; i++)
				expectedOKResponses.add (provider.zadd(setkey, i, dataList.get(i)));
			
			List<Future<Long>> rankingResps = new ArrayList<Future<Long>>();
			for(int i=0;i<SMALL_CNT; i++)
				rankingResps.add(provider.zrank(setkey, dataList.get(i)));

			Future<Long>  frRankForMissingElement = provider.zrank(setkey, dataList.get(SMALL_CNT+1));
			Future<Long>  frRankForNoSuchSet = provider.zrank("no-such-set", dataList.get(0));
			try {
				for(Future<Boolean> resp : expectedOKResponses)
					assertTrue (resp.get().booleanValue(), "zadd of random element should have been true");
				
				int i=0;
				for(Future<Long> resp : rankingResps)
					assertEquals (resp.get().longValue(), i++, "zrank of element");
				
				assertEquals (frRankForMissingElement.get().longValue(), -1, "zrank against non-existent member should be -1");
				assertEquals (frRankForNoSuchSet.get().longValue(), -1, "zrank against non-existent key should be -1");
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	
	
	@Test
	public void testZrevrankStringByteArray() throws InterruptedException{
		cmd = Command.ZREVRANK.code;
		Log.log("TEST: %s command", cmd);

		try {
			provider.flushdb();
			String setkey = keys.get(0);
			
			List<Future<Boolean>> expectedOKResponses = new ArrayList<Future<Boolean>>();
			for(int i=0;i<=SMALL_CNT; i++)
				expectedOKResponses.add (provider.zadd(setkey, i, dataList.get(i)));
			
			List<Future<Long>> rankingResps = new ArrayList<Future<Long>>();
			for(int i=0;i<=SMALL_CNT; i++)
				rankingResps.add(provider.zrevrank(setkey, dataList.get(i)));

			Future<Long>  frRankForMissingElement = provider.zrevrank(setkey, dataList.get(SMALL_CNT+1));
			Future<Long>  frRankForNoSuchSet = provider.zrevrank("no-such-set", dataList.get(0));
			try {
				for(Future<Boolean> resp : expectedOKResponses)
					assertTrue (resp.get().booleanValue(), "zadd of random element should have been true");
				
				int i=0;
				for(Future<Long> resp : rankingResps)
					assertEquals (resp.get().longValue(), SMALL_CNT - i++, "zrevrank of element");
				
				assertEquals (frRankForMissingElement.get().longValue(), -1, "zrevrank against non-existent member should be -1");
				assertEquals (frRankForNoSuchSet.get().longValue(), -1, "zrevrank against non-existent key should be -1");
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	@Test
	public void testZrangeWithscoresStringByteArray() throws InterruptedException{
		cmd = Command.ZRANGE$OPTS.code + " byte[] | " + Command.ZSCORE.code + " byte[]";
		Log.log("TEST: %s command", cmd);

		try {
			provider.flushdb();
			String setkey = keys.get(0);
			List<Future<Boolean>> expectedOKResponses = new ArrayList<Future<Boolean>>();
			for(int i=0;i<MEDIUM_CNT; i++)
				expectedOKResponses.add (provider.zadd(setkey, i, dataList.get(i)));
			
			Future<List<byte[]>> frZValues = provider.zrange(setkey, 0, SMALL_CNT); 
			Future<List<ZSetEntry>> frSubset = provider.zrangeSubset(setkey, 0, SMALL_CNT); 
			
			try {
				for(Future<Boolean> resp : expectedOKResponses)
					assertTrue (resp.get().booleanValue(), "zadd of random element should have been true");
				
				List<byte[]> zvalues = frZValues.get();
				List<ZSetEntry> zsubset = frSubset.get();
				for(int i=0;i<SMALL_CNT; i++){
					assertEquals(zsubset.get(i).getValue(), dataList.get(i), "value of element from zrange_withscore");
					assertEquals(zsubset.get(i).getValue(), zvalues.get(i), "value of element from zrange_withscore compared with zscore with same range query");
					assertEquals (zsubset.get(i).getScore(), (double)i, "score of element from zrange_withscore");
					assertTrue(zsubset.get(i).getScore() <= zsubset.get(i+1).getScore(), "range member score should be smaller or equal to previous range member.  idx: " + i);
					if(i>0) assertTrue(zsubset.get(i).getScore() >= zsubset.get(i-1).getScore(), "range member score should be bigger or equal to previous range member.  idx: " + i);
				}
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}

	@Test
	public void testZrevrangeWithscoresStringByteArray() throws InterruptedException{
		cmd = Command.ZREVRANGE$OPTS.code + " byte[] | " + Command.ZSCORE.code + " byte[]";
		Log.log("TEST: %s command", cmd);

		try {
			provider.flushdb();
			String setkey = keys.get(0);
			List<Future<Boolean>> expectedOKResponses = new ArrayList<Future<Boolean>>();
			for(int i=0;i<MEDIUM_CNT; i++)
				expectedOKResponses.add (provider.zadd(setkey, i, dataList.get(i)));
			
			Future<List<byte[]>> frZValues = provider.zrevrange(setkey, 0, SMALL_CNT); 
			Future<List<ZSetEntry>> frSubset = provider.zrevrangeSubset(setkey, 0, SMALL_CNT); 
			
			try {
				for(Future<Boolean> resp : expectedOKResponses)
					assertTrue (resp.get().booleanValue(), "zadd of random element should have been true");
				
				List<byte[]> zvalues = frZValues.get();
				List<ZSetEntry> zsubset = frSubset.get();
				for(int i=0;i<SMALL_CNT; i++){
					assertEquals(zsubset.get(i).getValue(), dataList.get(MEDIUM_CNT-i-1), "value of element from zrange_withscore");
					assertEquals(zsubset.get(i).getValue(), zvalues.get(i), "value of element from zrange_withscore compared with zscore with same range query");
					assertEquals (zsubset.get(i).getScore(), (double)MEDIUM_CNT-i-1, "score of element from zrange_withscore");
					assertTrue(zsubset.get(i).getScore() >= zsubset.get(i+1).getScore(), "range member score should be smaller or equal to previous range member.  idx: " + i);
					if(i>0) assertTrue(zsubset.get(i).getScore() <= zsubset.get(i-1).getScore(), "range member score should be bigger or equal to previous range member.  idx: " + i);
				}
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	
	@Test
	public void testSaddStringByteArray() throws InterruptedException{
		cmd = Command.SADD.code + " byte[]";
		Log.log("TEST: %s command", cmd);

		try {
			provider.flushdb();
			String setkey = keys.get(0);
			List<Future<Boolean>> expectedOKResponses = new ArrayList<Future<Boolean>>();
			for(int i=0;i<SMALL_CNT; i++)
				expectedOKResponses.add (provider.sadd(setkey, dataList.get(i)));
			
			List<Future<Boolean>> expectedErrorResponses = new ArrayList<Future<Boolean>>();
			for(int i=0;i<SMALL_CNT; i++)
				expectedErrorResponses.add (provider.sadd(setkey, dataList.get(i)));
			

			try {
				for(Future<Boolean> resp : expectedOKResponses)
					assertTrue (resp.get().booleanValue(), "sadd of random element should have been true");
				
				for(Future<Boolean> resp : expectedErrorResponses)
					assertFalse (resp.get().booleanValue(), "sadd of existing element should have been false");
				
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	
	@Test
	public void testSort() throws InterruptedException{
		cmd = Command.SORT.code;
		Log.log("TEST: %s command", cmd);

		final String setkey = "set-key";
		final String listkey = "list-key";
		try {
			provider.flushdb();
			
			for(int i=0; i<MEDIUM_CNT; i++){
				provider.sadd(setkey, stringList.get(i));
				provider.lpush(listkey, stringList.get(i));
			}
			
			int cnt1 = MEDIUM_CNT, cnt2 = 9, cnt3 = 1;
			Future<List<byte[]>> sortListResp1 = provider.sort(listkey).ALPHA().LIMIT(0, cnt1).DESC().execAsync();
			Future<List<byte[]>> sortListResp2 = provider.sort(listkey).ALPHA().LIMIT(10, cnt2).DESC().execAsync();
			Future<List<byte[]>> sortListResp3 = provider.sort(listkey).ALPHA().LIMIT(MEDIUM_CNT-1, cnt3).DESC().execAsync();
			
			Future<List<byte[]>> sortSetResp = provider.sort(setkey).ALPHA().LIMIT(0, 555).DESC().execAsync();
			
			try {
				assertEquals(sortListResp1.get().size(), cnt1, "expecting sort results of size MEDIUM_CNT");
				assertEquals(sortListResp2.get().size(), cnt2, "expecting sort results of size 9");
				assertEquals(sortListResp3.get().size(), cnt3, "expecting sort results of size 1");
				
				Log.log("TEST: SORTED LIST ");
				for(String s : toStr(sortListResp1.get()))
					System.out.format("[t.1] %s\n", s);
				
				Log.log("TEST: SORTED LIST ");
				for(String s : toStr(sortListResp2.get()))
					System.out.format("[t.1] %s\n", s);
				
				Log.log("TEST: SORTED LIST ");
				for(String s : toStr(sortListResp3.get()))
					System.out.format("[t.1] %s\n", s);
				
				Log.log("TEST: SORTED SET ");
				for(String s : toStr(sortSetResp.get()))
					System.out.format("%s\n", s);
				
				String destKey = String.format("%s_store", listkey);
				List<byte[]> ssres = provider.sort(listkey).ALPHA().LIMIT(0, MEDIUM_CNT).DESC().STORE(destKey).execAsync().get();
				assertNotNull(ssres, "result of srot with STORE should be non-null");
				assertEquals(ssres.size(), 1, "result of sort with STORE should be a list of single entry (the stored list's size)");
				long sortedListSize = Query.Support.unpackValue(ssres);
				assertEquals(sortedListSize, MEDIUM_CNT);
				RedisType type = provider.type(destKey).get();
				assertEquals(type, RedisType.list, "dest key of SORT .. STORE should be a LIST");
				long sslistSize = provider.llen(destKey).get();
				assertEquals(sslistSize, sortedListSize, "result of SORT ... STORE and LLEN of destkey list should be same");
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
		
		// force errors
		
		// count can't be zero
		Runnable invalidLimitSpec = new Runnable() {
			public void run() {
				try { provider.sort(listkey).ALPHA().LIMIT(0, 0).DESC().exec(); }
                catch (Throwable t) { throw new RuntimeException ("", t); }
			}
		};
		assertDidRaiseRuntimeError(invalidLimitSpec, RuntimeException.class);
		
		// LIMIT from must be positive, {0...n}
		Runnable invalidLimitSpec2 = new Runnable() {
			public void run() {
				try { provider.sort(listkey).ALPHA().LIMIT(-1, 1).DESC().exec(); }
                catch (Throwable t) { throw new RuntimeException ("", t); }
			}
		};
		assertDidRaiseRuntimeError(invalidLimitSpec2, RuntimeException.class);
		
	}
	
	@Test
	public void testLsetStringIntByteArray() throws InterruptedException {
		cmd = Command.LSET.code + " byte[] | " + Command.LLEN;
		Log.log("TEST: %s command", cmd);

		try {
			provider.flushdb();
			
			// prep a MEDIUM list
			String listkey = keys.get(0);
			for(int i=0; i<SMALL_CNT; i++)
				provider.rpush(listkey, dataList.get(i)); // use rpush (append) so ref list sequence order is preserved
			Future<Long> llenResp = provider.llen(listkey);
			
			// now we'll change their values
			for(int i=0; i<SMALL_CNT; i++)
				provider.lset(listkey, i, dataList.get(SMALL_CNT+i));
			Future<List<byte[]>>  lrangeResp1 = provider.lrange(listkey, 0, LARGE_CNT);
			
			// now we'll change their values using the negative index mode
			int lim = SMALL_CNT*-1;
			for(int i=-1; i>lim; i--)
				provider.lset(listkey, i, dataList.get(i*-1));
			Future<List<byte[]>>  lrangeResp2 = provider.lrange(listkey, 0, LARGE_CNT);

			// test edge conditions
			// out of range
			Future<ResponseStatus> expectedErrorResp =  provider.lset(listkey, SMALL_CNT, dataList.get(0)); 

			try {
				assertTrue (llenResp.get().longValue() == SMALL_CNT, "list length should be SMALL_CNT");
				
				for(int i=0; i<SMALL_CNT; i++)
					assertEquals (dataList.get(SMALL_CNT+i), lrangeResp1.get().get(i), "after LSET the expected and range item differ at idx: " + i);

				for(int i=0; i<SMALL_CNT; i++)
					assertEquals (dataList.get(SMALL_CNT-i), lrangeResp2.get().get(i), "after LSET the expected and range item differ at idx: " + i);
				
				boolean expectedError = false;
				try {
					Log.log("Expecting an out of range ERROR for LSET here..");
	                expectedErrorResp.get(); // wait for response
				}
	            catch (ExecutionException e) {
	            	expectedError = true;
	            	Throwable cause = e.getCause();
	            	if(cause instanceof RedisException)
	            		Log.log("%s (as excepted)", cause);
	            	else
	            		fail("FAULT: the cause of ExecutionException was expected to be a RedisException");
	            }
				assertTrue(expectedError, "was expecting a Expecting an out of range ERROR for LSET here");
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	
	@Test
	public void testLremStringByteArrayInt() throws InterruptedException {
		cmd = Command.LREM.code + " byte[] | " + Command.LLEN;
		Log.log("TEST: %s command", cmd);

		try {
			provider.flushdb();
			
			// prep a MEDIUM list
			String listkey = keys.get(0);
			for(int i=0; i<MEDIUM_CNT; i++)
				provider.rpush(listkey, dataList.get(i)); // use rpush (append) so ref list sequence order is preserved
			
			Future<Long> llenResp = provider.llen(listkey);
			
			// everysingle one of these should work and remove exactly 1 item
			Future<Long> lremResp1 = provider.lrem(listkey, dataList.get(0), 0);
			Future<Long> lremResp2 = provider.lrem(listkey, dataList.get(1), -1);
			Future<Long> lremResp3 = provider.lrem(listkey, dataList.get(2), 1);
			Future<Long> lremResp4 = provider.lrem(listkey, dataList.get(3), 2);
			Future<Long> lremResp5 = provider.lrem(listkey, dataList.get(4), -2);
			
			// everysingle one of these should work and remove NOTHING
			Future<Long> lremResp6 = provider.lrem(listkey, dataList.get(0), 0);
			Future<Long> lremResp7 = provider.lrem(listkey, dataList.get(1), -1);
			Future<Long> lremResp8 = provider.lrem(listkey, dataList.get(2), 1);
			Future<Long> lremResp9 = provider.lrem(listkey, dataList.get(3), 2);
			Future<Long> lremResp10 = provider.lrem(listkey, dataList.get(4), -2);
			
			try {
				long listcnt = llenResp.get();
				assertTrue (listcnt == MEDIUM_CNT, "list length should be MEDIUM_CNT");
				
				assertEquals(1, lremResp1.get().longValue());
				assertEquals(1, lremResp2.get().longValue());
				assertEquals(1, lremResp3.get().longValue());
				assertEquals(1, lremResp4.get().longValue());
				assertEquals(1, lremResp5.get().longValue());
				assertEquals(0, lremResp6.get().longValue());
				assertEquals(0, lremResp7.get().longValue());
				assertEquals(0, lremResp8.get().longValue());
				assertEquals(0, lremResp9.get().longValue());
				assertEquals(0, lremResp10.get().longValue());
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	
	@Test
	public void testLrange() throws InterruptedException {
		cmd = Command.LRANGE.code ;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();
			
			// prep a MEDIUM list
			String listkey = keys.get(0);
			for(int i=0; i<MEDIUM_CNT; i++)
				provider.rpush(listkey, dataList.get(i)); // use rpush (append) so ref list sequence order is preserved
			
			Future<Long> llenResp = provider.llen(listkey);
			Future<List<byte[]>>  lrangeResp = provider.lrange(listkey, 0, MEDIUM_CNT-1);

			try {
//				provider.ping().get();
				// sanity check
				long listcnt = llenResp.get();
				assertTrue (listcnt == MEDIUM_CNT, "list length should be MEDIUM_CNT");
				
				List<byte[]> items = lrangeResp.get();
				assertEquals (items.size(), MEDIUM_CNT, "list range 0->MEDIUM_CNT length should be MEDIUM_CNT");
				for(int i=0; i<MEDIUM_CNT; i++)
					assertEquals(items.get(i), dataList.get(i), 
							"nth items of range 0->CNT should be the same as nth dataitem, where n is " + i);
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	
	@Test
	public void testSubstr() throws InterruptedException {
		cmd = Command.SUBSTR.code ;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();
			
			String key = keys.get(0);
			byte[] value = dataList.get(0);
			provider.set(key, value);
			
			List<Future<byte[]>> frBytesRespList = new ArrayList<Future<byte[]>>();
			for(int i=0; i<value.length; i++){
				frBytesRespList.add(provider.substr(key, i, i));
			}
			
			Future<byte[]> frFullValue1 = provider.substr(key, 0, -1);
			Future<byte[]> frFullValue2 = provider.substr(key, 0, value.length);
			Future<byte[]> frExpectedNull = provider.substr(key, -1, 0);

			try {
				assertEquals(frFullValue1.get(), value, "full range substr should be equal to value");
				assertEquals(frFullValue2.get(), value, "full range substr should be equal to value");
				assertEquals(frExpectedNull.get(), new byte[0], "substr with -1 from idx should be zero-length array");
				for(int i=0; i<value.length; i++){
					assertTrue(frBytesRespList.get(i).get().length == 1, "checking size: using substr to iterate over value bytes @ idx " + i);
					assertEquals(frBytesRespList.get(i).get()[0], value[i], "checking value: using substr to iterate over value bytes @ idx " + i);
				}
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	
	@Test
	public void testRpop() throws InterruptedException {
		cmd = Command.RPOP.code ;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();

			// prep a small list
			String listkey = keys.get(0);
			for(int i=0; i<SMALL_CNT; i++)
				provider.lpush(listkey, dataList.get(i)); 
			
			Future<Long> llenResp = provider.llen(listkey);
			
			List<Future<byte[]>>  rpops = new ArrayList<Future<byte[]>>();
			for(int i=0; i<SMALL_CNT; i++)
				rpops.add(provider.rpop(listkey)); 
			
			try {
				long listcnt = llenResp.get();
				// sanity check
				assertTrue (listcnt == SMALL_CNT, "list length should be SMALL_CNT");
				
				for(int i=0; i<SMALL_CNT; i++)
					assertEquals(rpops.get(i).get(), dataList.get(i), 
							"nth popped tail should be the same as nth dataitem, where n is " + i);
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	
	@Test
	public void testLpop() throws InterruptedException {
		cmd = Command.LPOP.code ;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();

			// prep a small list
			String listkey = keys.get(0);
			for(int i=0; i<SMALL_CNT; i++)
				provider.rpush(listkey, dataList.get(i)); 
			
			Future<Long> llenResp = provider.llen(listkey);
			List<Future<byte[]>>  lpops = new ArrayList<Future<byte[]>>();
			for(int i=0; i<SMALL_CNT; i++)
				lpops.add(provider.lpop(listkey)); 
			
			try {
				long listcnt = llenResp.get();
				// sanity check
				assertTrue (listcnt == SMALL_CNT, "list length should be SMALL_CNT");
				
				for(int i=0; i<SMALL_CNT; i++)
					assertEquals(lpops.get(i).get(), dataList.get(i), 
							"nth popped head should be the same as nth dataitem, where n is " + i);
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	
	@Test
	public void testLindex() throws InterruptedException {
		cmd = Command.LINDEX.code;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();

			// prep a small list
			String listkey = keys.get(0);
			for(int i=0; i<SMALL_CNT; i++)
				provider.rpush(listkey, dataList.get(i)); 
			
			try {
				for(int i=0; i<SMALL_CNT; i++)
					assertEquals (provider.lindex(listkey, i).get(), dataList.get(i), "list items should match ref data");
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	
	@Test
	public void testLtrim() throws InterruptedException {
		cmd = Command.LTRIM.code + " | " + Command.LLEN.code + " | " + Command.LRANGE.code ;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();

			// prep a small list
			String listkey = keys.get(0);
			for(int i=0; i<SMALL_CNT; i++)
				provider.rpush(listkey, dataList.get(i)); 
			
			Future<Long> llenResp = provider.llen(listkey);
			try {
				long listcnt = llenResp.get();
				// sanity check
				assertTrue (listcnt == SMALL_CNT, "list length should be SMALL_CNT");
				
				provider.ltrim(listkey, 0,listcnt-1);	// trim nothing
				assertTrue (provider.llen(listkey).get() == listcnt, "trim from end to end - no delta expected");
				
				provider.ltrim(listkey, 1, listcnt-1); 	// remove the head
				assertTrue (provider.llen(listkey).get() == listcnt-1, "trim head - len should be --1 expected");
				
				listcnt = provider.llen(listkey).get();
				assertEquals(listcnt, SMALL_CNT - 1, "list length should be SMALL_CNT - 1");
				for(int i=0; i<SMALL_CNT-1; i++)
					assertEquals(provider.lindex(listkey, i).get(), dataList.get(i+1), "list items should match ref data shifted by 1 after removing head");
				
				provider.ltrim(listkey, -2, -1);
				assertTrue (provider.llen(listkey).get() == 2, "list length should be 2");
				
				provider.ltrim(listkey, 0, 0);
				assertTrue (provider.llen(listkey).get() == 1, "list length should be 1");

				byte[] lastItem = provider.lpop(listkey).get();
				assertNotNull(lastItem, "last item should not have been null");
				assertTrue (provider.llen(listkey).get() == 0, "expecting empty list after trims and pop");
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	
	@Test
	public void testLpushStringByteArray() throws InterruptedException {
		cmd = Command.LPUSH.code + " byte[] | " + Command.LLEN + " | " + Command.LRANGE;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();
			boolean expected = false;
			try {
				byte[] nil = null;
				provider.lpush("foo", nil);
			}
			catch(IllegalArgumentException e) { expected = true; }
			assertTrue(expected, "expecting exception for null value to RPUSH");
			
			String listkey = this.keys.get(0);
			for(int i=0; i<SMALL_CNT; i++){
				provider.lpush(listkey, dataList.get(i));
			}
			Future<Long> llenResp = provider.llen(listkey);
			Future<List<byte[]>> lrangeResp = provider.lrange(listkey, 0, SMALL_CNT);

			try {
				// use LLEN: size should be small count
				assertTrue(llenResp.get()==SMALL_CNT, "LLEN after LPUSH is wrong");
				
				// use LRANGE 0 cnt: equal size and data should be same in order
				List<byte[]> range = lrangeResp.get();
				assertTrue(range.size()==SMALL_CNT, "range size after LPUSH is wrong");
				for(int i=0; i<SMALL_CNT; i++){
					int r = SMALL_CNT - i - 1;
					assertEquals (dataList.get(i), range.get(r), "range and reference list differ at i: " + i);
				}
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	@Test
	public void testLpoppushStringByteArray() throws InterruptedException {
		cmd = Command.RPOPLPUSH.code;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();

			String listkey = this.keys.get(0);
			for(int i=0; i<SMALL_CNT; i++){
				provider.lpush(listkey, dataList.get(i));
			}
			
			Future<Long> llenRespBefore = provider.llen(listkey);
			List<Future<byte[]>> poppushResponses = new ArrayList<Future<byte[]>>();
			
			for(int i=0; i<SMALL_CNT; i++){
				poppushResponses.add(provider.rpoplpush (listkey, listkey));
			}
			
			Future<Long> llenRespAfter = provider.llen(listkey);
			try {
				// use LLEN: size should be small count
				assertTrue(llenRespBefore.get()==SMALL_CNT, "LLEN after LPUSH is wrong");
				
				for(int i=0; i<SMALL_CNT; i++){
					assertEquals (poppushResponses.get(i).get(), dataList.get(i), "RPOPLPUSH result and reference list differ at i: " + i);
				}
				
				// use LLEN: size should be small count
				assertTrue(llenRespAfter.get()==SMALL_CNT, "LLEN after RPOPLPUSH is wrong");
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}

	@Test
	public void testRpushStringByteArray() throws InterruptedException {
		cmd = Command.RPUSH.code + " byte[] | " + Command.LLEN + " | " + Command.LRANGE;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();

			boolean expected = false;
			try {
				byte[] nil = null;
				provider.rpush("foo", nil);
			}
			catch(IllegalArgumentException e) { expected = true; }
			assertTrue(expected, "expecting exception for null value to RPUSH");
			
			String listkey = this.keys.get(0);
			for(int i=0; i<SMALL_CNT; i++){
				provider.rpush(listkey, dataList.get(i));
			}
			
			Future<Long> llenResp = provider.llen(listkey);
			Future<List<byte[]>> lrangeResp = provider.lrange(listkey, 0, SMALL_CNT);
			
			try {
				// use LLEN: size should be small count
				assertTrue(llenResp.get()==SMALL_CNT, "LLEN after RPUSH is wrong");
				
				// use LRANGE 0 cnt: equal size and data should be same in order
				List<byte[]> range = lrangeResp.get();
				assertTrue(range.size()==SMALL_CNT, "range size after RPUSH is wrong");
				for(int i=0; i<SMALL_CNT; i++){
					assertEquals (dataList.get(i), range.get(i), "range and reference list differ at i: " + i);
				}
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	@Test
	public void testMget() throws InterruptedException {
		cmd = Command.MGET.code ;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();
			for(int i=0; i<SMALL_CNT; i++){
				provider.set (keys.get(i), dataList.get(i));
			}
			Future<List<byte[]>> mgetResp1 = provider.mget(keys.get(0));
			Future<List<byte[]>> mgetResp2 = provider.mget(keys.get(0), keys.get(1)); 
			Future<List<byte[]>> mgetResp3 = provider.mget(keys.get(0), keys.get(1), keys.get(2));
			Future<List<byte[]>> mgetResp4 = provider.mget("foo", "bar", "paz"); 
			
			try {
				List<byte[]>  values = null;
				
				values = mgetResp1.get();
				assertEquals(values.size(), 1, "one value expected");
				for(int i=0; i<1; i++)
					assertEquals(values.get(i), dataList.get(i));
				
				values = mgetResp2.get();
				assertEquals(values.size(), 2, "2 values expected");
				for(int i=0; i<2; i++)
					assertEquals(values.get(i), dataList.get(i));
				
				values = mgetResp3.get();
				assertEquals(values.size(), 3, "3 values expected");
				for(int i=0; i<3; i++)
					assertEquals(values.get(i), dataList.get(i));
				
				values = mgetResp4.get();
				assertEquals(values.size(), 3, "3 values expected");
				for(int i=0; i<3; i++)
					assertEquals(values.get(i), null, "nonexistent key value in list should be null");
				
				// edge cases
				// all should through exceptions
				boolean didRaiseEx;
				didRaiseEx = false;
				try {
					String[] keys = null;
					provider.mget(keys).get();
				}
				catch (IllegalArgumentException e) {didRaiseEx = true;}
				catch (Throwable whatsthis) { fail ("unexpected exception raised", whatsthis);}
				if(!didRaiseEx){ fail ("Expected exception not raised."); }

				didRaiseEx = false;
				try {
					String[] keys = new String[0];
					provider.mget(keys).get();
				}
				catch (IllegalArgumentException e) {didRaiseEx = true;}
				catch (Throwable whatsthis) { fail ("unexpected exception raised", whatsthis);}
				if(!didRaiseEx){ fail ("Expected exception not raised."); }

				didRaiseEx = false;
				try {
					String[] keys = new String[3];
					keys[0] = stringList.get(0);
					keys[1] = null;
					keys[2] = stringList.get(2);
					provider.mget(keys).get();
				}
				catch (IllegalArgumentException e) {didRaiseEx = true;}
				catch (Throwable whatsthis) { fail ("unexpected exception raised", whatsthis);}
				if(!didRaiseEx){ fail ("Expected exception not raised."); }
				
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}

	@Test
	public void testDel() throws InterruptedException {
		cmd = Command.DEL.code;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();
			
			String key = this.keys.get(0);
			provider.set (key, dataList.get(0));
			Future<Boolean> existsResp1 = provider.exists(key); 
			Future<Long> delResp = provider.del(key);
			Future<Boolean> existsResp2 = provider.exists(key); 

			try {
				assertEquals(delResp.get().longValue(), 1, "one key should been deleted");
				assertTrue (existsResp1.get(), "After set key should exist");
				assertFalse (existsResp2.get(), "After del key should not exist");
				
				// delete many keys
				provider.flushdb();
				for(int i=0; i<SMALL_CNT; i++) provider.set(stringList.get(i), dataList.get(i));

				String[] keysToDel = new String[SMALL_CNT];
				for(int i=0; i<SMALL_CNT; i++) keysToDel[i] = stringList.get(i);

				Future<Long> delCnt1 = provider.del(keysToDel);
				for(int i=0; i<SMALL_CNT; i++) assertFalse (provider.exists(stringList.get(i)).get(), "key should have been deleted");
				assertEquals(delCnt1.get().longValue(), SMALL_CNT, "SMALL_CNT keys were deleted");
				
				// delete many keys but also spec one non existent keys - delete result should be less than key cnt
				provider.flushdb();
				for(int i=0; i<SMALL_CNT-1; i++) provider.set(stringList.get(i), dataList.get(i));

				keysToDel = new String[SMALL_CNT];
				for(int i=0; i<SMALL_CNT; i++) keysToDel[i] = stringList.get(i);

				Future<Long> delCnt2 = provider.del(keysToDel);
				for(int i=0; i<SMALL_CNT; i++) assertFalse (provider.exists(stringList.get(i)).get(), "key should have been deleted");
				assertEquals(delCnt2.get().longValue(), SMALL_CNT-1, "SMALL_CNT-1 keys were actually deleted");

				// edge cases
				// all should through exceptions
				boolean didRaiseEx;
				didRaiseEx = false;
				try {
					String[] keys = null;
					provider.del(keys).get();
				}
				catch (IllegalArgumentException e) {didRaiseEx = true;}
				catch (Throwable whatsthis) { fail ("unexpected exception raised", whatsthis);}
				if(!didRaiseEx){ fail ("Expected exception not raised."); }

				didRaiseEx = false;
				try {
					String[] keys = new String[0];
					provider.del(keys).get();
				}
				catch (IllegalArgumentException e) {didRaiseEx = true;}
				catch (Throwable whatsthis) { fail ("unexpected exception raised", whatsthis);}
				if(!didRaiseEx){ fail ("Expected exception not raised."); }

				didRaiseEx = false;
				try {
					String[] keys = new String[3];
					keys[0] = stringList.get(0);
					keys[1] = null;
					keys[2] = stringList.get(2);
					provider.del(keys).get();
				}
				catch (IllegalArgumentException e) {didRaiseEx = true;}
				catch (Throwable whatsthis) { fail ("unexpected exception raised", whatsthis);}
				if(!didRaiseEx){ fail ("Expected exception not raised."); }

			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	
	@Test
	public void testIncrAndDecr() throws InterruptedException {
		cmd = Command.INCR.code + " | " + Command.DECR.code;
		Log.log("TEST: %s command", cmd);
		try {
			String cntr_key = keys.get(0);

			provider.flushdb();

			Future<Long> incrResp = null;
			for(int i = 0; i<MEDIUM_CNT; i++){
				incrResp = provider.incr(cntr_key);
			}
			Future<Long> decrResp = null;
			for(int i = 0; i<MEDIUM_CNT; i++){
				decrResp = provider.decr(cntr_key);
			}
			
			try {
				assertEquals(incrResp.get().longValue(), MEDIUM_CNT, "INCR should have counted counter to MEDIUM_CNT");
				assertEquals(decrResp.get().longValue(), 0, "DECR should have counted counter to zero");
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}

	@Test
	public void testIncrbyAndDecrby() throws InterruptedException {
		cmd = Command.INCRBY.code + " |" + Command.DECRBY.code;
		Log.log("TEST: %s command", cmd);
		try {
			String cntr_key = keys.get(0);

			provider.flushdb();

			Future<Long> incrbyResp = null;
			for(int i = 0; i<MEDIUM_CNT; i++){
				incrbyResp = provider.incrby(cntr_key, 10);
			}
			Future<Long> decrbyResp = null;
			for(int i = 0; i<MEDIUM_CNT; i++){
				decrbyResp = provider.decrby(cntr_key, 10);
			}
			
			try {
				assertEquals(incrbyResp.get().longValue(), MEDIUM_CNT*10, "INCRBY should have counted counter to MEDIUM_CNT*10");
				assertEquals(decrbyResp.get().longValue(), 0, "DECRBY should have counted counter to zero");
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}

	@Test
	public void testGetSetStringByteArray() throws InterruptedException {
		cmd = Command.SET.code + " | " + Command.GETSET.code + " byte[] ";
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();
			provider.set(keys.get(0), dataList.get(0));
			Future<byte[]> getResp = provider.get(keys.get(0));
			Future<byte[]> getsetResp1 = provider.getset(keys.get(0), dataList.get(1));
			Future<byte[]> getsetResp2 = provider.getset(keys.get(1), dataList.get(2));
			
			try {
				assertEquals(getResp.get(), dataList.get(0), "get results doesn't match the expected data");
				assertEquals(getsetResp1.get(), dataList.get(0), "getset results doesn't match the expected data");
				assertEquals(getsetResp2.get(), null, "getset result for new key should have been null");
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}

	@Test
	public void testHsetHget() throws InterruptedException {
		cmd = Command.HSET.code + " | " + Command.HGET + " | " + Command.HEXISTS;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();
			Future<Boolean> hsetResp1 = provider.hset(keys.get(0), keys.get(1), dataList.get(0));
			Future<Boolean> hexistsResp1 = provider.hexists(keys.get(0), keys.get(1));
			Future<Boolean> hexistsResp2 = provider.hexists(keys.get(0), keys.get(2));
			Future<Boolean> hsetResp1_1 = provider.hset(keys.get(0), keys.get(1), dataList.get(0));
			Future<Boolean> hsetResp2 = provider.hset(keys.get(0), keys.get(2), stringList.get(0));
			Future<Boolean> hsetResp3 = provider.hset(keys.get(0), keys.get(3), 222);
			objectList.get(0).setName("Hash Stash");
			Future<Boolean> hsetResp4 = provider.hset(keys.get(0), keys.get(4), objectList.get(0));
			
			Future<byte[]> hgetResp1 = provider.hget(keys.get(0), keys.get(1));
			Future<byte[]> hgetResp2 = provider.hget(keys.get(0), keys.get(2));
			Future<byte[]> hgetResp3 = provider.hget(keys.get(0), keys.get(3));
			Future<byte[]> hgetResp4 = provider.hget(keys.get(0), keys.get(4));
			
			Future<Long> hlenResp1 = provider.hlen(keys.get(0));

			Future<Boolean> hdelResp1 = provider.hdel(keys.get(0), keys.get(1));
			Future<Boolean> hdelResp2 = provider.hdel(keys.get(0), keys.get(1));
			
			Future<Long> hlenResp2 = provider.hlen(keys.get(0));
			Future<Long> hlenResp3 = provider.hlen("some-random-key");
			
			
			try {
				assertTrue (hsetResp1.get(), "hset using byte[] value");
				assertTrue (hexistsResp1.get(), "hexists of field should be true");
				assertTrue (!hexistsResp2.get(), "hexists of non existent field should be false");
				assertTrue (!hsetResp1_1.get(), "second hset using byte[] value should return false");
				assertTrue (hsetResp2.get(), "hset using String value");
				assertTrue (hsetResp3.get(), "hset using Number value");
				assertTrue (hsetResp4.get(), "hset using Object value");
				
				assertEquals (hgetResp1.get(), dataList.get(0), "hget of field with byte[] value");
				assertEquals (DefaultCodec.toStr(hgetResp2.get()), stringList.get(0), "hget of field with String value");
				assertEquals (DefaultCodec.toLong(hgetResp3.get()).longValue(), 222, "hget of field with Number value");
				TestBean objval = DefaultCodec.decode(hgetResp4.get());
				assertEquals (objval.getName(), objectList.get(0).getName(), "hget of field with Object value");
				
				assertTrue (hdelResp1.get(), "hdel of field should be true");
				assertTrue (!hdelResp2.get(), "hdel of non-existent field should be false");
				
				assertEquals (hlenResp1.get().longValue(), 4, "hlen of hash should be 4");
				assertEquals (hlenResp2.get().longValue(), 3, "hlen of hash should be 3");
				assertEquals (hlenResp3.get().longValue(), 0, "hlen of non-existant hash should be 0");

			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}


	@Test
	public void testHIncrBy() throws InterruptedException {
		cmd = Command.HSET.code + " | " + Command.HGET + " | " + Command.HINCRBY;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();
			Future<Boolean> hsetResp1 = provider.hset(keys.get(0), keys.get(1), 41);
			Future<Long> hincrBy1 = provider.hincrby(keys.get(0), keys.get(1), 1);
			Future<byte[]> hget = provider.hget(keys.get(0), keys.get(1));
						
			Future<Long> hincrBy2 = provider.hincrby(keys.get(0), keys.get(2), 4);
			
			try {
				assertTrue (hsetResp1.get(), "hset using byte[] value");
				assertEquals (hincrBy1.get(), (Long)42l, "hexists of field should be true");
				assertEquals(hget.get(), Convert.toBytes(42l));
				assertEquals(hincrBy2.get(), (Long)4l);

			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	
	@Test
	public void testHkeys() throws InterruptedException {
		cmd = Command.HSET.code + " | " + Command.HKEYS;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();
			Future<Boolean> hsetResp1 = provider.hset(keys.get(0), keys.get(1), dataList.get(0));
			Future<Boolean> hsetResp2 = provider.hset(keys.get(0), keys.get(2), stringList.get(0));
			Future<Boolean> hsetResp3 = provider.hset(keys.get(0), keys.get(3), 222);
			objectList.get(0).setName("Hash Stash");
			Future<Boolean> hsetResp4 = provider.hset(keys.get(0), keys.get(4), objectList.get(0));
			
			// get keys
			Future<List<byte[]>> hkeysResp1 = provider.hkeys(keys.get(0));
			// alright - empty the hash
			for(int i=1; i<5; i++)
	            try {
	                assertTrue(provider.hdel(keys.get(0), keys.get(i).getBytes()).get());
                }
                catch (ExecutionException e) {
    				Throwable cause = e.getCause();
    				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
                }
			// get keys again
			Future<List<byte[]>> hkeysResp2 = provider.hkeys(keys.get(0));
			// nil case
			Future<List<byte[]>> hkeysResp3 = provider.hkeys("no-such-hash");
			
			
			try {
				assertTrue (hsetResp1.get(), "hset using byte[] value");
				assertTrue (hsetResp2.get(), "hset using String value");
				assertTrue (hsetResp3.get(), "hset using Number value");
				assertTrue (hsetResp4.get(), "hset using Object value");
				
				List<byte[]> hkeys = hkeysResp1.get(); 
				assertEquals (hkeys.size(), 4, "keys list size should be 4");
				assertEquals(hkeysResp2.get(), Collections.EMPTY_LIST, "result should be empty");
				assertEquals(hkeysResp3.get(), Collections.EMPTY_LIST, "list of keys of non-existent hash should be empty");
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	
	@Test
	public void testSetBitGetBit() throws InterruptedException {
		cmd = Command.SETBIT.code + " | " + Command.GETBIT;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();
			Future<Boolean> setbit1 = provider.setbit(keys.get(0), 1, true);
			Future<Boolean> setbit32 = provider.setbit(keys.get(0), 32, true);

			Future<Boolean> getbit1 = provider.getbit(keys.get(0), 1);
			Future<Boolean> getbit2 = provider.getbit(keys.get(0), 2);
			Future<Boolean> getbit32 = provider.getbit(keys.get(0), 32);
			
			try {
				assertFalse (setbit1.get(), "original bit at 1");
				assertFalse (setbit32.get(), "original bit at 32");
				assertTrue (getbit1.get(), "getbit at 1");
				assertFalse (getbit2.get(), "getbit at 2");
				assertTrue (getbit32.get(), "getbit at 32");
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}


	@Test
	public void testHvals() throws InterruptedException {
		cmd = Command.HSET.code + " | " + Command.HVALS;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();
			Future<Boolean> hsetResp1 = provider.hset(keys.get(0), keys.get(1), dataList.get(0));
			Future<Boolean> hsetResp2 = provider.hset(keys.get(0), keys.get(2), stringList.get(0));
			Future<Boolean> hsetResp3 = provider.hset(keys.get(0), keys.get(3), 222);
			objectList.get(0).setName("Hash Stash");
			Future<Boolean> hsetResp4 = provider.hset(keys.get(0), keys.get(4), objectList.get(0));
			
			// get values
			Future<List<byte[]>> hvalsResp1 = provider.hvals(keys.get(0));
			// alright - empty the hash
			for(int i=1; i<5; i++)
	            try {
	                assertTrue(provider.hdel(keys.get(0), keys.get(i)).get());
                }
                catch (ExecutionException e) {
    				Throwable cause = e.getCause();
    				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
                }
			// get values again
    			// get values
			Future<List<byte[]>> hvalsResp2 = provider.hvals(keys.get(0));
			// nil case
			Future<List<byte[]>> hvalsResp3 = provider.hvals("no-such-hash");
			
			
			try {
				assertTrue (hsetResp1.get(), "hset using byte[] value");
				assertTrue (hsetResp2.get(), "hset using String value");
				assertTrue (hsetResp3.get(), "hset using Number value");
				assertTrue (hsetResp4.get(), "hset using Object value");
				
				List<byte[]> hvals = hvalsResp1.get(); 
				assertEquals(hvals.size(), 4, "values list size should be 4");
				assertEquals(hvalsResp2.get(), Collections.EMPTY_LIST, "values list size should be empty");
				assertEquals(hvalsResp3.get(), Collections.EMPTY_LIST, "list of values of non-existent hash should be empty");

			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}


	@Test
	public void testHgetall() throws InterruptedException {
		cmd = Command.HSET.code + " | " + Command.HGETALL;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();
			Future<Boolean> hsetResp1 = provider.hset(keys.get(0), keys.get(1), dataList.get(0));
			Future<Boolean> hsetResp2 = provider.hset(keys.get(0), keys.get(2), stringList.get(0));
			Future<Boolean> hsetResp3 = provider.hset(keys.get(0), keys.get(3), 222);
			objectList.get(0).setName("Hash Stash");
			Future<Boolean> hsetResp4 = provider.hset(keys.get(0), keys.get(4), objectList.get(0));
			
			// get keys
			Future<List<byte[]>> frHkeys = provider.hkeys(keys.get(0));
			
			// get all
			Future<Map<byte[], byte[]>> frHmap1 = provider.hgetall(keys.get(0));
			
			// delete all keys
			for(int i =1; i<5; i++) {
	            try {
	                provider.hdel(keys.get(0), keys.get(i)).get();
                }
                catch (ExecutionException e) {
    				Throwable cause = e.getCause();
    				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
                }
			}
			// get all again
			Future<Map<byte[], byte[]>> frHmap2 = provider.hgetall(keys.get(0));
    			
			// get all for non-existent hash
			Future<Map<byte[], byte[]>> frHmap3 = provider.hgetall("no-such-hash");
        			
			try {
				assertTrue (hsetResp1.get(), "hset using byte[] value");
				assertTrue (hsetResp2.get(), "hset using String value");
				assertTrue (hsetResp3.get(), "hset using Number value");
				assertTrue (hsetResp4.get(), "hset using Object value");
				
				assertEquals( frHmap1.get().size(), 4, "hash map length");
				assertEquals( frHkeys.get().size(), 4, "keys list length");

				Map<String, byte[]> hmap = DefaultCodec.toDataDictionary(frHmap1.get());
				assertEquals(hmap.get(keys.get(1)), dataList.get(0), "byte[] value mapping should correspond to prior HSET");
				assertEquals(DefaultCodec.toStr(hmap.get(keys.get(2))), stringList.get(0), "String value mapping should correspond to prior HSET");
				assertEquals(DefaultCodec.toLong(hmap.get(keys.get(3))).longValue(), 222, "Number value mapping should correspond to prior HSET");
				assertEquals(DefaultCodec.decode(hmap.get(keys.get(4))), objectList.get(0), "Object value mapping should correspond to prior HSET");
				
				Map<String, byte[]> hmap2 = DefaultCodec.toDataDictionary(frHmap2.get());
				assertEquals(hmap2, Collections.EMPTY_MAP, "result should be empty");
				
				Map<String, byte[]> hmap3 = DefaultCodec.toDataDictionary(frHmap3.get());
				assertEquals(hmap3, Collections.EMPTY_MAP, "hgetall for non existent hash should be empty");
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}

	
	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#set(java.lang.String, byte[])}.
	 * @throws InterruptedException 
	 */
	@Test
	public void testSetStringByteArray() throws InterruptedException {
		cmd = Command.SET.code + " | " + Command.SETNX.code + " byte[] | " + Command.GET;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();
			provider.set(keys.get(keys.size()-1), emptyBytes);
			Future<byte[]> getResp = provider.get(keys.get(keys.size()-1));
			Future<Boolean> setnxResp1 = provider.setnx(keys.get(1), dataList.get(1));
			Future<Boolean> setnxResp2 = provider.setnx(keys.get(1), dataList.get(2));
			
			try {
				assertEquals(getResp.get(), emptyBytes, "set and get results for empty byte[]");
				assertTrue(setnxResp1.get());
				assertFalse(setnxResp2.get());
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	
	/**
	 * Test method for {@link JRedisFuture#append(String, String)}
	 * @throws InterruptedException 
	 */
	@Test
	public void testAppendStringString() throws InterruptedException {
		cmd = Command.APPEND.code + " | " + Command.GET.code + " String";
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();
			
			// append to a non-existent key
			// as of Redis 1.3.7 it behaves just like set but returns value len instead of status
			String key1 = keys.get(0);
			
			Future<Long> frLen0 = provider.append(key1, emptyString);
			Future<byte[]> frGet0 = provider.get(key1);
			
			Future<Long> frLen1 = provider.append(key1, stringList.get(0));
			Future<byte[]> frGet1 = provider.get(key1);
			
			Future<Long> frLen2 = provider.append(key1, stringList.get(1));
			Future<byte[]> frGet2 = provider.get(key1);
			
			provider.sadd(keys.get(1), stringList.get(0));
			Future<Long>   frExpectedError = provider.append(keys.get(1), stringList.get(0));
			
			try {
				assertEquals(frLen0.get().longValue(), 0, "append of emtpy string to new key should be zero");
				assertEquals(DefaultCodec.toStr(frGet0.get()), emptyString, "get results after append to new key for empty string");
				
				assertEquals(frLen1.get().longValue(), stringList.get(0).length(), "append of emtpy string to new key should be zero");
				assertEquals(DefaultCodec.toStr(frGet1.get()), stringList.get(0), "get results after append to new key for empty string");
				
				assertEquals(frLen2.get().longValue(), stringList.get(0).length() + stringList.get(1).length(), "append of emtpy string to new key should be zero");
				StringBuffer appendedString = new StringBuffer();
				appendedString.append(stringList.get(0));
				appendedString.append(stringList.get(1));
				assertEquals(DefaultCodec.toStr(frGet2.get()), appendedString.toString(), "get results after append to new key for empty string");
				
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}

			
			// check see if we got the expected RedisException
			boolean expected = false;
			try {
				frExpectedError.get();
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				if(cause instanceof RedisException)
					expected = true;
				else
					fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
			assertTrue(expected, "expecting RedisException for append to a non-string key");
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}
	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#rename(java.lang.String, java.lang.String)}.
	 * @throws InterruptedException 
	 */
	@Test
	public void testRename() throws InterruptedException {
		cmd = Command.RENAME.code;
		Log.log("TEST: %s command", cmd);
		try {
			
			String newkey = null;
			byte[] value = dataList.get(0);
			key = getRandomAsciiString (random.nextInt(24)+2);
			newkey = getRandomAsciiString (random.nextInt(24)+2);
			
			Future<ResponseStatus> 	flushResp = provider.flushdb();
			Future<ResponseStatus> 	setResp = provider.set (key, value);
			Future<byte[]> 			getOldResp = provider.get(key);
			Future<ResponseStatus> 	reanmeResp = provider.rename (key, newkey);
			Future<byte[]> 			getNewResp = provider.get(newkey);
			
			try {
				flushResp.get();
				setResp.get();
				assertEquals(value, getOldResp.get());
				reanmeResp.get();
				assertEquals(value, getNewResp.get());
			}
			catch(ExecutionException e){
				// errors in response 
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}

	/**
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#rename(java.lang.String, java.lang.String)}.
	 * @throws InterruptedException 
	 */
	@Test
	public void testRenamenx() throws InterruptedException {
		cmd = Command.RENAMENX.code;
		Log.log("TEST: %s command", cmd);
		try {
			// flush db and set a key
			provider.flushdb();
			provider.set (keys.get(0), dataList.get(0));
			
			// this should return true in future
			Future<Boolean> 	reanmenxResp1 = provider.renamenx (keys.get(0), keys.get(2));

			// flush db again and set 2 keys
			provider.flushdb();
			provider.set (keys.get(1), dataList.get(1));
			provider.set (keys.get(2), dataList.get(2));
			
			// this should return false in future
			Future<Boolean> 	reanmenxResp2 = provider.renamenx (keys.get(1), keys.get(2));
			
			try {
				// note that we don't have to 'get' the future results for 
				// commands we are not interested in.
				assertTrue (reanmenxResp1.get(), "1st renamenx should have been true");
				assertFalse (reanmenxResp2.get(), "2nd renamenx should have been false");
			}
			catch(ExecutionException e){
				// errors in response 
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
	}

	/**
	 * Test settting a key in a flushed db, and then checking
	 * exists, flushing again, and finally keys().  
	 * Will invoke commands asynchronously, and after 
	 * @throws InterruptedException 
	 */
	@Test
	public void testSetAndFlushdbAndExistsAndKeys() throws InterruptedException {
		cmd = 
			Command.FLUSHDB.code + " | " +
			Command.SET.code + " | " +
			Command.EXISTS.code + " | " +
			Command.FLUSHDB.code + " | " +
			Command.KEYS.code;
			
		Log.log("TEST: %s commands", cmd);
		try {
			key = "woof";
			Future<ResponseStatus> flushResp = provider.flushdb();
			Future<ResponseStatus> setResp = provider.set(key, "meow");
			Future<Boolean> existsResp = provider.exists(key);
			Future<ResponseStatus> flush2Resp = provider.flushdb();
			Future<List<byte[]>>  keysResp = provider.keys();
			
			try {
				flushResp.get(); // no need to check status; if error exception is raised
				setResp.get();
				assertTrue(existsResp.get(), "key should exists at this point");
				flush2Resp.get();
				assertTrue(keysResp.get().size() == 0, "keys should have returned list of 0 items");
			}
			catch(ExecutionException e){
				// errors in response 
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) { 
			// errors in request time
			fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e); 
		}
	}
	
	/**
	 * Tests to force RedisExceptions against various response types.
	 * First we queue the requests and then we get the responses for all.
	 * This tests both the primary goal and the correct behaviour of asynch
	 * provider to queue the responses.
	 * @throws InterruptedException 
	 */
	@Test
	public void testElicitErrors() throws InterruptedException {
		Log.log("TEST: Elicit errors");
		try {
			provider.flushdb();
			
			String key = keys.get(0);
			try {
	            provider.set(key, smallData).get();
            }
            catch (ExecutionException e) {
            	fail(cmd + " ERROR => " + e.getLocalizedMessage(), e); 
            }
			
            // queue a few commands -- all are expected to result in ExecutionExcetion on Future.get()
            //
			Future<Boolean>	fSaddResp = provider.sadd(key, dataList.get(0)); 
			Future<Long>	fScardResp = provider.scard(key); 
			Future<byte[]>  fLpopResp = provider.lpop(key); 
			Future<List<byte[]>> fSmembersResp = provider.smembers(key); 
			
			boolean expectedError;
			
			expectedError = false;
			try {
				Log.log("1 - Expecting an operation against key holding the wrong kind of value ERROR for SADD..");
				fSaddResp.get();
			}
			catch (ExecutionException e) { expectedError = true; }
			assertTrue(expectedError, "should have raised an exception but did not");
			
			expectedError = false;
			try {
				Log.log("2 - Expecting an operation against key holding the wrong kind of value ERROR for SCARD..");
				fScardResp.get();
			}
			catch (ExecutionException e) { expectedError = true; }
			assertTrue(expectedError, "should have raised an exception but did not");
			
			expectedError = false;
			try {
				Log.log("3 - Expecting an operation against key holding the wrong kind of value ERROR for LPOP ..");
				fLpopResp.get();
			}
			catch (ExecutionException e) { expectedError = true; }
			assertTrue(expectedError, "should have raised an exception but did not");
			
			expectedError = false;
			try {
				Log.log("4 - Expecting an operation against key holding the wrong kind of value ERROR for SMEMBERS ..");
				fSmembersResp.get();
			}
			catch (ExecutionException e) { expectedError = true; }
			assertTrue(expectedError, "should have raised an exception but did not");
			
			
		} 
		catch (ClientRuntimeException e) { fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e); }
	}
	
	/**
	 * Asynchronous responses must provide reference to {@link RedisException}
	 * through the {@link ExecutionException#getCause()} per {@link Future} semantics.
	 * @throws InterruptedException 
	 */
	@Test
	public void testExecutionExceptionCauseType() throws InterruptedException {
		boolean expectedError;
		String key = keys.get(0);
		try {
			expectedError = false;
			try {
				Log.log("Expecting an operation against key holding the wrong kind of value ERROR..");
				
				provider.set(key, smallData);  // don't wait for response ..
				Future<Boolean> fBool = provider.sadd(key, dataList.get(0));
				@SuppressWarnings("unused")
                boolean response = fBool.get(); // wait for response
			}
            catch (ExecutionException e) {
            	expectedError = true;
            	Throwable cause = e.getCause();
            	if(cause instanceof RedisException)
            		Log.log("%s (as excepted)", cause);
            	else
            		fail("FAULT: the cause of ExecutionException was expected to be a RedisException");
            }
			assertTrue(expectedError, "should have raised an exception but did not");
		}
		finally {
			
		}
	}
	
	/**
	 * Test {@link JRedisFuture#ping()}
	 * @throws InterruptedException 
	 */
	@Test
	public void testPing () throws InterruptedException {
		Future<ResponseStatus> frStatus = null;
		cmd = Command.PING.code;
		Log.log("TEST: %s command", cmd);
		try {
			frStatus = provider.ping();
			ResponseStatus status = frStatus.get();
			assertTrue(!status.isError(), "ping return status");
		}
        catch (ExecutionException e) {
	        e.printStackTrace();
	        fail(cmd + " FAULT: " + e.getCause().getLocalizedMessage(), e);
        }
	}
	/**
	 * Test {@link JRedisFuture#flushdb()}
	 * @throws InterruptedException 
	 */
	@Test
	public void testFlushDb () throws InterruptedException {
		Future<ResponseStatus> frStatus = null;
		cmd = Command.FLUSHDB.code;
		Log.log("TEST: %s command", cmd);
		try {
			frStatus = provider.flushdb();
			ResponseStatus status = frStatus.get();
			assertTrue(!status.isError(), "flushdb return status");
		}
        catch (ExecutionException e) {
	        e.printStackTrace();
	        fail(cmd + " FAULT: " + e.getCause().getLocalizedMessage(), e);
        }
	}
	@Test
	public void testEcho() throws InterruptedException {
		Future<byte[]> echoResp = null;
		cmd = Command.ECHO.code;
		Log.log("TEST: %s command", cmd);
		try {
			echoResp = provider.echo(dataList.get(0));
			assertEquals(dataList.get(0), echoResp.get(), "data and echo results");

			byte[] zerolenData = new byte[0];
			assertEquals(zerolenData, provider.echo(zerolenData).get(), "zero len byte[] and echo results");
			boolean expected = false;
			try {
				provider.echo((byte[])null);
			}
			catch(IllegalArgumentException e) { expected = true; }
			assertTrue(expected, "expecting exception for null value to ECHO");

		}
        catch (ExecutionException e) {
	        e.printStackTrace();
	        fail(cmd + " FAULT: " + e.getCause().getLocalizedMessage(), e);
        }
	}
	
	@Test
	public void testBgrewriteaof() throws InterruptedException {
		Future<String> cmdRespMsg = null;
		cmd = Command.BGREWRITEAOF.code;
		Log.log("TEST: %s command", cmd);
		try {
			cmdRespMsg = provider.bgrewriteaof();
			assertTrue(cmdRespMsg.get() != null, "cmd response message should not be null");
		}
        catch (ExecutionException e) {
	        e.printStackTrace();
	        fail(cmd + " FAULT: " + e.getCause().getLocalizedMessage(), e);
        }
	}
	
	/**
	 * Test {@link JRedisFuture#debug()}
	 * @throws InterruptedException 
	 */
	@Test
	public void testDebug () throws InterruptedException {
		Future<ObjectInfo> frInfo = null;
		cmd = Command.DEBUG.code;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb();
			provider.set("foo", "bar");
			frInfo = provider.debug("foo");
			ObjectInfo info = frInfo.get();
			assertNotNull(info);
			Log.log("DEBUG of key => %s", info);
		}
        catch (ExecutionException e) {
	        e.printStackTrace();
	        fail(cmd + " FAULT: " + e.getCause().getLocalizedMessage(), e);
        }
	}
	
	@Test
	public void testExpireat() throws InterruptedException {
		cmd = Command.EXPIREAT.code;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb().get();
			String keyToExpire = "expire-me";
			provider.set(keyToExpire, dataList.get(0)).get();

			Log.log("TEST: %s with expire time 1000 msecs in future", Command.EXPIREAT);
			assertTrue(provider.expireat(keyToExpire, System.currentTimeMillis() + 2000).get(), "expireat for existing key should be true");
      assertTrue (provider.exists(keyToExpire).get());
			assertTrue(!provider.expireat("no-such-key", System.currentTimeMillis() + 500).get(), "expireat for non-existant key should be false");
			
			
			// NOTE: IT SIMPLY WON'T WORK WITHOUT GIVING REDIS A CHANCE
			// could be network latency, or whatever, but the expire command is NOT
			// that precise, so we need to wait a bit longer
			
			Thread.sleep(5000);
			assertTrue (!provider.exists(keyToExpire).get(), "key should have expired by now");
		}
        catch (ExecutionException e) {
	        e.printStackTrace();
	        fail(cmd + " FAULT: " + e.getCause().getLocalizedMessage(), e);
        }
	}

}
