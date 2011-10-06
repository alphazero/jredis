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

package org.jredis.examples;

import static org.jredis.ri.alphazero.support.DefaultCodec.toLong;
import static org.jredis.ri.alphazero.support.DefaultCodec.toStr;
import java.util.Random;
import org.jredis.JRedisFuture;
import org.jredis.RedisException;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.JRedisPipeline;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import org.jredis.ri.alphazero.support.Log;

/**
 * Extension of {@link UsingJRedisFuture} with a {@link JRedisPipeline} as the 
 * provider.  
 * <p>See {@link UsingJRedisFuture} for demonstration of generic asynchronous
 * semantics.
 * <p>See {@link UsingJRedisPipeline#exampleUseofSyncInPipeline(ConnectionSpec)} for {@link JRedisPipeline} specific
 * mix mode usage incorporating asynchronous and synchronous calls.
 *  
 * 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Oct 11, 2009
 * @since   alpha.0
 * @see UsingJRedisFuture
 * 
 */

public class UsingJRedisPipeline extends UsingJRedisFuture {

	public static void main (String[] args) {
		final int database = 13;
		ConnectionSpec connectionSpec = DefaultConnectionSpec.newSpec("localhost", 6379, database, "jredis".getBytes());
		
	    exampleUseofSyncInPipeline(connectionSpec);
    }
	/**
	 * Example highlights usage pattern of pipeline with both the natural asynchronous
	 * semantics and synchronous semantics provided by {@link JRedisPipeline#sync()}
	 * 
     * @param connectionSpec
     */
    @SuppressWarnings("boxing")
	private static void exampleUseofSyncInPipeline (ConnectionSpec connectionSpec) {
    	
    	// Note that we are using a JRedisPipeline reference and not a generic
    	// JRedisFuture here -- this exposes the additional 
    	
    	JRedisPipeline pipeline = new JRedisPipeline(connectionSpec);
    	
    	/*
    	 * Alright, so a hokey example of situations where we would like to
    	 * asynchronously pipeline a bunch of commands, and then at various point in
    	 * the process need to sync up with redis and get values before continuing.
    	 * 
    	 * Obviously we can do this using JRedisFuture as well by calling get() on the
    	 * returned Future object, but the sync() method may be enhanced in future to
    	 * provide additional features.  Regardless, it does some of the boiler plate
    	 * ExecutionException handling code so its a bit prettier.
    	 */
    	try {
    		long start = System.currentTimeMillis();
    		
    		/* a sequence of asynchronous calls */
    		
	        pipeline.ping();
	        pipeline.flushdb();

	        Random rand = new Random();
	        byte[] data = new byte[8];
	        for(int i=0; i<1000000; i++){
	        	rand.nextBytes(data);
	        	pipeline.lpush("my-list", data);
	        }
	        /* 
	         * switch to synchronous semantics
	         * the following call will block until
	         * all the responses for above + the llen() 
	         * itself have been received.
	         */
	        
	        long llen = pipeline.sync().llen("my-list");
	        
	        String cntrKey = "my-cntr";
	        for(int i=0; i<100000; i++) {
	        	pipeline.incr(cntrKey);
	        }
	        /* sync call */
	        long cntr = toLong (pipeline.sync().get(cntrKey));
	        
	        for(int i=0; i<100000; i++){
	        	pipeline.set("random:"+i, "value:" + rand.nextInt());
	        }
	        /* sync call */
	        String randomVal = toStr (pipeline.sync().get("random:"+999));
	        
	        pipeline.flushdb();
    		System.out.format ("end using sync() = %d msec\n", System.currentTimeMillis() - start);
	        
	        System.out.format("%s => %d\n", cntrKey, cntr);
	        System.out.format("%s => %s\n", "random:"+999, randomVal);
	        System.out.format("%s has %s items\n", "my-list", llen);
	        
        }
        catch (RedisException e) {
        	Log.problem("RedisException: " + e);
        }
        finally{
        	pipeline.sync().quit();
        	Log.log("shutting down.");
        }
    }
	/**
     * @param connectionSpec
     */
    public UsingJRedisPipeline (ConnectionSpec connectionSpec) {
	    super(connectionSpec);
    }

	/* (non-Javadoc)
	 * @see org.jredis.examples.UsingJRedisFuture#getProviderInstance(org.jredis.connector.ConnectionSpec)
	 */
	@Override
	protected JRedisFuture getProviderInstance (ConnectionSpec connectionSpec) {
		return new JRedisPipeline(connectionSpec);
	}
}
