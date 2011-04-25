/*
 *   Copyright 2010 Joubin Houshyar
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

package org.jredis.examples.commands;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.jredis.JRedis;
import org.jredis.JRedisFuture;
import org.jredis.RedisException;
import org.jredis.RedisType;
import org.jredis.ZSetEntry;
import org.jredis.connector.ConnectionSpec;
import org.jredis.protocol.Command;
import org.jredis.ri.alphazero.JRedisAsyncClient;
import org.jredis.ri.alphazero.JRedisClient;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import org.jredis.ri.alphazero.support.DefaultCodec;

/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Mar 20, 2010
 * @since   alpha.0
 * 
 */

public class UsingZrangeSubset {
	
	static final String zset = "example-sorted-set";
	static final Random rand = new Random(System.currentTimeMillis());
	
	public static void main (String[] args) {
		usingSyncClient();
		usingAsyncClient();
	}
	
	/**
	 * Using the synchronous interface 
	 */
	public static void usingSyncClient () {
		ConnectionSpec spec = DefaultConnectionSpec.newSpec()
		.setCredentials("jredis".getBytes())
		.setDatabase(10);

		JRedis jredis = new JRedisClient(spec);

		System.out.println ("** using JRedis **");
		
		useZRangeSubset (jredis);
		
		jredis.quit();
	}

	/**
	 * Using the asynchronous interface 
	 */
	public static void usingAsyncClient () {
		ConnectionSpec spec = DefaultConnectionSpec.newSpec()
		.setCredentials("jredis".getBytes())
		.setDatabase(10);

		JRedisFuture jredis = new JRedisAsyncClient(spec);

		System.out.println ("\n\n** using JRedisFuture **");
		useZRangeSubset(jredis);
		
        jredis.quit();

	}
	
	/**
	 * The z[rev]rangeSubset commands return non-standard results tpes (see {@link ZSetEntry}).  Nothing
	 * that unexpected as far as the semantics are concerned, but just in case it is not perfectly clear, this
	 * example illustrates its intended use.
	 * <b>
	 * So here we'll flush the db, {@link Command#ZADD} a few (scored) entries to a {@link RedisType#zset}, and
	 * then use {@link JRedis#zrangeSubset(String, long, long)} and {@link JRedis#zrevrangeSubset(String, long, long)}
	 * and dump the results to console. 
	 *  
	 * @param jredis
	 */
	private static void useZRangeSubset (JRedis jredis) {

		try {
			jredis.flushdb();
			
			// we'll add a few String values to our set with random scores
			//
			for(int i=0; i<100; i++){
				jredis.zadd(zset, rand.nextDouble(), getRandomAsciiString(rand, 8));
			}
			
			// let's get the subset in range {0, 10} in ascending order
			//
			List<ZSetEntry> subset = jredis.zrangeSubset(zset, 0, 10);
			
			System.out.format ("\n\n%s elements from 0 to 10 - natural order:\n\t---\n", zset);
			for(int j=0; j<subset.size(); j++) {
				// ZSetEntry uses byte[] as baseline for value
				// since we know we put Strings in the set, we can go ahead and decode the byte[] and get our String value back
				// value is just a double (primitive).  
				
				ZSetEntry e = subset.get(j);
				String value = DefaultCodec.toStr(e.getValue());
				
				System.out.format("\t[%03d]:  {value: %s score: %1.19f}\n", j, value, e.getScore());
			}
			
			// & now let's get the subset in range {0, 10} in ascending order
			//
			List<ZSetEntry> revsubset = jredis.zrevrangeSubset(zset, 0, 10);
			
			System.out.format ("\n\n%s elements from 0 to 10 - reverse order:\n\t---\n", zset);
			for(int j=0; j<revsubset.size(); j++) {
				ZSetEntry e = revsubset.get(j);
				String value = DefaultCodec.toStr(e.getValue());
				
				System.out.format("\t[%03d]:  {value: %s score: %1.19f}\n", 100-j, value, e.getScore());
			}			
        }
        catch (RedisException e) { e.printStackTrace(); }
	}
	
	/**
	 * Here we're doing the same thing as {@link UsingZrangeSubset#useZRangeSubset(JRedis)} but we're
	 * using the {@link JRedisFuture} and asynchronous semantics.
	 */
	private static void useZRangeSubset (JRedisFuture jredis) {
		
		jredis.flushdb();
		for(int i=0; i<100; i++){
			jredis.zadd(zset, rand.nextDouble(), getRandomAsciiString(rand, 8));
		}
		
		Future<List<ZSetEntry>> futureSubset = jredis.zrangeSubset(zset, 0, 10);
		Future<List<ZSetEntry>> futureRevSubset = jredis.zrevrangeSubset(zset, 0, 10);
		
		try {
			
			System.out.format ("\n\n%s elements from 0 to 10 - natural order:\n\t---\n", zset);
			List<ZSetEntry> subset = futureSubset.get();
			for(int j=0; j<subset.size(); j++) {
				// ZSetEntry uses byte[] as baseline for value
				// since we know we put Strings in the set, we can go ahead and decode the byte[] and get our String value back
				// value is just a double (primitive).  
				
				ZSetEntry e = subset.get(j);
				String value = DefaultCodec.toStr(e.getValue());
				
				System.out.format("\t[%03d]:  {value: %s score: %1.19f}\n", j, value, e.getScore());
			}
			
			// & now let's get the subset in range {0, 10} in ascending order
			//
			List<ZSetEntry> revsubset = futureRevSubset.get();
			
			System.out.format ("\n\n%s elements from 0 to 10 - reverse order:\n\t---\n", zset);
			for(int j=0; j<revsubset.size(); j++) {
				ZSetEntry e = revsubset.get(j);
				String value = DefaultCodec.toStr(e.getValue());
				
				System.out.format("\t[%03d]:  {value: %s score: %1.19f}\n", 100-j, value, e.getScore());
			}			
        }
        catch (ExecutionException e) {
	        e.printStackTrace();
        }
        catch (InterruptedException e) {
	        e.printStackTrace();
        }
	}
	// -- util
	/**
	 * Creates a random ascii string
	 * @param length
	 * @return
	 */
	static final String getRandomAsciiString (Random random, int length) {
		StringBuilder builder = new  StringBuilder(length);
		for(int i = 0; i<length; i++){
			char c = (char) (random.nextInt(126-33) + 33);
			builder.append(c);
		}
		return builder.toString();
	}

}
