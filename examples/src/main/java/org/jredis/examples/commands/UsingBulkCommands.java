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

package org.jredis.examples.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.jredis.JRedis;
import org.jredis.JRedisFuture;
import org.jredis.KeyValueSet;
import org.jredis.RedisException;
import org.jredis.connector.ConnectionSpec;
import org.jredis.protocol.ResponseStatus;
import org.jredis.ri.alphazero.BulkSetMapping;
import org.jredis.ri.alphazero.JRedisAsyncClient;
import org.jredis.ri.alphazero.JRedisClient;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;

/**
 * Demonstrates using the bulk commands and helper classes to marshall data.
 * <b>Note that the example flushes DB 10.</b>
 * 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Nov 25, 2009
 * @since   alpha.0 | Redis 1.07
 */

public class UsingBulkCommands {
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

		System.out.println ("\nusing the SyncClient: \n\n");
		
		useMSet (jredis);
		useMSetNX (jredis);
		jredis.quit();
	}

	private static void useMSet (JRedis jredis) {
		// BulkSetMapping provides a set of static methods to create new specific
		// flavors of KeyValueSet<T> for the JRedis.mset(..) method.
		// use this when your values are all of the same type.
		// Here we are using String values

		KeyValueSet.Strings 	kvSet = BulkSetMapping.newStringKVSet();
		kvSet
		.add("foo", "woof")
		.add("bar", "meow")
		.add("paz", "the salt")
		.add("x?", "yz!");
		
		try {
			jredis.flushdb();
	        jredis.mset(kvSet);
        }
        catch (RedisException e) { e.printStackTrace(); }
	}
	
	private static void useMSetNX (JRedis jredis) {
		// Here we are using a mixed set of value types

		Map<String, byte[]> kvMap = new HashMap<String, byte[]>();
		kvMap.put("foo", "bar".getBytes());
		kvMap.put("cat", "meow".getBytes());
		kvMap.put("dog", "woof".getBytes());
		kvMap.put("bird", "whale fail".getBytes());
		kvMap.put("pi", String.valueOf(3.141592653589793).getBytes());
		
		try {
			jredis.flushdb();
			
			jredis.set("bird", "tweet");	// <= force an error on msetnx
	        boolean stat = jredis.msetnx(kvMap);
	        if(!stat) {
	        	System.out.format("Couldn't msetnx - one of these already exists: %s\n", kvMap.keySet());
	        }
	        
	        // and now we can find out which one existed:
	        for(String key : kvMap.keySet()){
	        	if(jredis.exists(key)){
	        		System.out.format("key '%s' [value: %s] already existed!\n", key, new String(jredis.get(key)));
	        	}
	        }
        }
        catch (RedisException e) { e.printStackTrace(); }
	}
	
	
	/**
	 * Using the asynchronous interface 
	 */
	public static void usingAsyncClient () {
		ConnectionSpec spec = DefaultConnectionSpec.newSpec()
		.setCredentials("jredis".getBytes())
		.setDatabase(10);

		JRedisFuture jredis = new JRedisAsyncClient(spec);

		System.out.println ("\nusing the AsyncClient: \n\n");
		useMSet(jredis);
		useMSetNX (jredis);
		
        jredis.quit();

	}
	private static void useMSetNX (JRedisFuture jredis) {
		
		Map<String, byte[]> kvMap = new HashMap<String, byte[]>();
		kvMap.put("foo", "bar".getBytes());
		kvMap.put("cat", "meow".getBytes());
		kvMap.put("dog", "woof".getBytes());
		kvMap.put("bird", "whale fail".getBytes());
		kvMap.put("pi", String.valueOf(3.141592653589793).getBytes());

		try {
			jredis.flushdb();
	        jredis.set("bird", "tweet");
	        Future<Boolean> future = jredis.msetnx(kvMap);
	        if(!future.get()) {
	        	System.out.format("Couldn't msetnx - one of these already exists: %s\n", kvMap.keySet());
	        }
	        // and now we can find out which one existed:
	        for(String key : kvMap.keySet()){
	        	if(jredis.exists(key).get()){
	        		System.out.format("key '%s' [value: %s] already existed!\n", key, new String(jredis.get(key).get()));
	        	}
	        }
        }
        catch (ExecutionException e) {
	        e.printStackTrace();
        }
        catch (InterruptedException e) {
	        e.printStackTrace();
        }
	}
	private static void useMSet (JRedisFuture jredis) {
		// BulkSetMapping provides a set of static methods to create new specific
		// flavors of KeyValueSet<T> for the JRedis.mset(..) method.
		// use this when your values are all of the same type.
		// Here we are using Number values
		
		KeyValueSet.Numbers 	kvSet = BulkSetMapping.newNumberKVSet();
		kvSet
		.add("integer", 10)
		.add("real", 10.55)
		.add("scientific", 1.5e4)
		.add("sepher", 0);

		try {
			jredis.flushdb();
	        Future<ResponseStatus> future = jredis.mset(kvSet);
	        if(future.get().isError()){
	        	throw new RuntimeException("MSET failed! (How could that be?)");
	        }
	        jredis.exists("foo");
        }
        catch (ExecutionException e) {
	        e.printStackTrace();
        }
        catch (InterruptedException e) {
	        e.printStackTrace();
        }
	}
}
