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

package org.jredis.ri.alphazero.bench;

import org.jredis.JRedis;
import org.jredis.RedisException;
import org.jredis.ri.alphazero.JRedisClient;

/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 11, 2009
 * @since   alpha.0
 * 
 */
public class JProfileTestCase {
	public static void main(String[] args) throws RedisException {
		new JProfileTestCase().run();
	}

	private void run() throws RedisException {
		JRedis	redis = new JRedisClient();
		redis.auth("jredis").ping().flushall();
		
		int iter = 500000;
		String key = "foo";
//		redis.sadd(key, "member 1");
//		redis.sadd(key, "member 2");
//		redis.sadd(key, "member 3");
//		redis.sadd(key, "member 4");
		redis.incrby(key, Integer.MAX_VALUE*2);
		long start = System.currentTimeMillis();
		for(Long j=0L; j<iter; j++) {
			redis.incr(key);
		}
		long delta = System.currentTimeMillis() - start;
		float rate = (iter * 1000) / delta;
		System.out.format("%d iterations | %d msec | %8.2f /sec \n", iter, delta, rate);
	}
}
