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

import static org.testng.Assert.*;
import static org.jredis.ri.alphazero.support.DefaultCodec.*;

import org.jredis.JRedis;
import org.jredis.RedisException;
import org.jredis.protocol.Command;
import org.jredis.ri.alphazero.support.Log;
import org.testng.annotations.Test;

/**
 * Tests {@link JRedis} implementations that support concurrent access.  Will
 * not run the full suite of tests (per {@link JRedisProviderTestsBase} concurrently. 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Nov 7, 2009
 * @since   alpha.0
 * 
 */

public abstract class ConcurrentJRedisProviderTestsBase extends JRedisProviderTestsBase {

	/*
		switch (command.responseType){
			case BULK:
			case MULTI_BULK:
			case STATUS:
			case STRING:
			case VIRTUAL:
				break;
	 */
	@Test(invocationCount=20, threadPoolSize=5, singleThreaded=false)
	public void testConcurrentBulkCommands() {
		String cmd = Command.GET.responseType.name();
		String threadName = Thread.currentThread().getName();
		String key = threadName + "::" + keys.get(0);
		Log.log("CONCURRENT TEST: %s resp type command | key: %s", cmd, key);
		try {
			provider.del(key);
			provider.set (key, threadName);
			assertTrue (provider.exists(key), "key should exist");
			String value = toStr (provider.get(key));
			assertTrue(value.endsWith(threadName), "value of key should be the thread name");
			provider.del(key);
		} 
		catch (RedisException e) { fail(cmd + " ERROR => " + e.getLocalizedMessage(), e); }
	}
	
	/**
	 * 
	 */
	@Test(invocationCount=20, threadPoolSize=5, singleThreaded=false)
	public void testConcurrentBooleanCommands() {
		String cmd = Command.EXISTS.responseType.name();
		String threadName = Thread.currentThread().getName();
		String key = threadName + "::" + keys.get(0);
		Log.log("CONCURRENT TEST: %s resp type command | key: %s", cmd, key);
		try {
			provider.set (key, threadName);
//			assertTrue (provider.del (key), "del response should be OK");
			assertEquals (provider.del(key), 1, "del response should be 1 for one key deleted");
			assertFalse (provider.exists(key), "deleted key response should not exist");
		} 
		catch (RedisException e) { fail(cmd + " ERROR => " + e.getLocalizedMessage(), e); }
	}
	
	/**
	 * 
	 */
	@Test(invocationCount=20, threadPoolSize=5, singleThreaded=false)
	public void testConcurrentNumberCommands() {
		String cmd = Command.INCR.responseType.name();
		String cntr_key = Thread.currentThread().getName() + "::" + keys.get(0);
		Log.log("CONCURRENT TEST: %s resp type command | key: %s", cmd, cntr_key);
		try {
			provider.del(cntr_key);
			for(int i = 1; i<50; i++)
				assertEquals(i, provider.incr(cntr_key));
			
			provider.del(cntr_key);
		} 
		catch (RedisException e) { fail(cmd + " ERROR => " + e.getLocalizedMessage(), e); }
	}
}
