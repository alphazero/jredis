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

package org.jredis.bench;

import org.jredis.JRedis;
import org.jredis.RedisException;
import org.jredis.ri.alphazero.support.Log;

/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 11, 2009
 * @since   alpha.0
 * 
 */
public class JRedisJProfileSubject {
	private final JRedis jredis;

	public JRedisJProfileSubject (JRedis jredis){
		this.jredis = jredis;
	}
	protected JRedisJProfileSubject () {
		jredis = null;
	}
	/**
	 * In a tight loop, we execute a few select
	 * commands that touch the various permutations of
	 * request complexity, and response type, so that
	 * we can pinpoint the bottlenecks and the general
	 * runtime characteristics of the JRedic provider.
	 * @throws RedisException
	 */
	public void run () throws RedisException {
		Log.log("***** JProfileTestCase ****");
//		jredis.auth("jredis").ping().flushall();
		
		int iter = 100000;
		String key = "foostring";
		String cntrkey = "foocntr";
		String listkey = "foolist";
		String setkey = "fooset";
		byte[] data = "meow".getBytes();
		long start = System.currentTimeMillis();
		for(Long j=0L; j<iter; j++) {
			jredis.incr(cntrkey);
			jredis.set(key, data);
			jredis.sadd(setkey, data);
			jredis.rpush(listkey, data);
		}
		long delta = System.currentTimeMillis() - start;
		float rate = ((float)iter * 1000) / delta;
		System.out.format("%d iterations | %d msec | %8.2f /sec \n", iter, delta, rate);
	}
}
