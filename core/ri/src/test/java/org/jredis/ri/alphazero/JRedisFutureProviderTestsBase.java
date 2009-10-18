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
import static org.testng.Assert.assertTrue;
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
	/**
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
