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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.jredis.ClientRuntimeException;
import org.jredis.JRedisFuture;
import org.jredis.ObjectInfo;
import org.jredis.RedisException;
import org.jredis.protocol.Command;
import org.jredis.protocol.ResponseStatus;
import org.jredis.ri.JRedisTestSuiteBase;
import org.jredis.ri.alphazero.support.Log;
import org.testng.annotations.Test;

/**
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
				assertEquals (provider.smembers(emptyset).get().size(), 0, "size of empty set members should be zero");
				assertEquals (provider.srandmember(emptyset).get(), null, "random member of empty set should be null");
				
				// non existent key
				String nonsuch = "no-such-key";
				assertEquals (provider.smembers(nonsuch).get(), null, "members of non existent key set should be null");
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
	public void testSpop () throws InterruptedException {
		cmd = Command.SPOP.code + " String | " + Command.SMEMBERS;
		Log.log("TEST: %s command", cmd);

		try {
			provider.flushdb();
			String setkey = keys.get(0);
			
			List<Future<Boolean>> saddResponses = new ArrayList<Future<Boolean>>();
			for(int i=0;i<SMALL_CNT; i++)
				saddResponses.add (provider.sadd(setkey, stringList.get(i)));
			
			List<Future<byte[]>>  poppedList = new ArrayList<Future<byte[]>>(SMALL_CNT);
			for(int i=0;i<SMALL_CNT; i++)
				poppedList.add(provider.spop(setkey));
			
			try {
				for(Future<Boolean> resp : saddResponses)
					assertTrue (resp.get().booleanValue(), "sadd of random element should have been true");
				
				for(Future<byte[]> item : poppedList){
					assertTrue(item != null, "random popped set element should be non-null");
				}
				
				// edge cases
				
				// empty set
				String emptyset = "empty-set";
				provider.sadd(emptyset, "delete-me");
				provider.srem(emptyset, "delete-me");
				assertEquals (provider.scard(setkey).get().longValue(), 0, "set should be empty after all elements are popped");
				assertEquals (provider.spop("no-such-set").get(), null, "spop of non existent key should be null");
				
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
				assertTrue(scardResp2.get().longValue() == 0, "setkey2 should be an empty set");
				assertTrue(members2.size() == 0, "smembers should have returned an empty list");
				assertTrue(members2.size() == scardResp2.get().longValue(), "smembers should have returned a list of scard size");
				
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

		try {
			provider.flushdb();
			
			String setkey = "set-key";
			String listkey = "list-key";
			for(int i=0; i<MEDIUM_CNT; i++){
				provider.sadd(setkey, stringList.get(i));
				provider.lpush(listkey, stringList.get(i));
			}
			
			Future<List<byte[]>> sortListResp = provider.sort(listkey).ALPHA().LIMIT(0, 555).DESC().execAsynch();
			Future<List<byte[]>> sortSetResp = provider.sort(setkey).ALPHA().LIMIT(0, 555).DESC().execAsynch();
			
			try {
				Log.log("TEST: SORTED LIST ");
				for(String s : toStr(sortListResp.get()))
					System.out.format("%s\n", s);
				
				Log.log("TEST: SORTED SET ");
				for(String s : toStr(sortSetResp.get()))
					System.out.format("%s\n", s);
			}
			catch(ExecutionException e){
				Throwable cause = e.getCause();
				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
			}
		} 
		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
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
			provider.del(key);
			Future<Boolean> existsResp2 = provider.exists(key); 
			
			try {
				assertTrue (existsResp1.get(), "After set key should exist");
				assertFalse (existsResp2.get(), "After del key should not exist");
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
			Future<List<String>>  keysResp = provider.keys();
			
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
	public void testExpireat() throws InterruptedException {
		cmd = Command.EXPIREAT.code;
		Log.log("TEST: %s command", cmd);
		try {
			provider.flushdb().get();
			String keyToExpire = "expire-me";
			provider.set(keyToExpire, dataList.get(0)).get();

			long expireTime = System.currentTimeMillis() + 500;
			Log.log("TEST: %s with expire time 1000 msecs in future", Command.EXPIREAT);
			assertTrue(provider.expireat(keyToExpire, expireTime).get(), "expireat for existing key should be true");
			assertTrue(!provider.expireat("no-such-key", expireTime).get(), "expireat for non-existant key should be false");
			assertTrue (provider.exists(keyToExpire).get());
			
			
			// NOTE: IT SIMPLY WON'T WORK WITHOUT GIVING REDIS A CHANCE
			// could be network latency, or whatever, but the expire command is NOT
			// that precise, so we need to wait a bit longer
			
			Thread.sleep(2000);
			assertTrue (!provider.exists(keyToExpire).get(), "key should have expired by now");
		}
        catch (ExecutionException e) {
	        e.printStackTrace();
	        fail(cmd + " FAULT: " + e.getCause().getLocalizedMessage(), e);
        }
	}
}
