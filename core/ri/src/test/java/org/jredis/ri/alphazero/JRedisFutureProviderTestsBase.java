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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.jredis.ClientRuntimeException;
import org.jredis.JRedisFuture;
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
//		Future<ResponseStatus> reqResp = provider.ping();
//		try {
//			try {
//				reqResp.get();
//			}
//			catch(ExecutionException e){
//				Throwable cause = e.getCause();
//				fail(cmd + " ERROR => " + cause.getLocalizedMessage(), e); 
//			}
//		} 
//		catch (ClientRuntimeException e) {  fail(cmd + " Runtime ERROR => " + e.getLocalizedMessage(), e);  }
//	}
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
}
