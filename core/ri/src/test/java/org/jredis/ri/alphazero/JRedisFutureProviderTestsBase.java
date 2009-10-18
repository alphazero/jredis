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

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
	 * Asynchronous responses must provide reference to {@link RedisException}
	 * through the {@link ExecutionException#getCause()} per {@link Future} semantics.
	 */
	@Test
	public void testExecutionExceptionCauseType() {
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
            catch (InterruptedException e) {
	            e.printStackTrace();
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
	 */
	@Test
	public void testPing () {
		Future<ResponseStatus> frStatus = null;
		cmd = Command.PING.code;
		Log.log("TEST: %s command", cmd);
		try {
			frStatus = provider.ping();
			ResponseStatus status = frStatus.get();
			assertTrue(!status.isError(), "ping return status");
		}
        catch (InterruptedException e) {
	        e.printStackTrace();
        }
        catch (ExecutionException e) {
	        e.printStackTrace();
	        fail(cmd + " FAULT: " + e.getCause().getLocalizedMessage(), e);
        }
	}
	/**
	 * Test {@link JRedisFuture#flushdb()}
	 */
	@Test
	public void testFlushDb () {
		Future<ResponseStatus> frStatus = null;
		cmd = Command.FLUSHDB.code;
		Log.log("TEST: %s command", cmd);
		try {
			frStatus = provider.flushdb();
			ResponseStatus status = frStatus.get();
			assertTrue(!status.isError(), "flushdb return status");
		}
        catch (InterruptedException e) {
	        e.printStackTrace();
        }
        catch (ExecutionException e) {
	        e.printStackTrace();
	        fail(cmd + " FAULT: " + e.getCause().getLocalizedMessage(), e);
        }
	}
}
