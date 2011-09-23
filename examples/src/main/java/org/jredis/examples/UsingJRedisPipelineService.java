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

import org.jredis.JRedis;
import org.jredis.RedisException;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.JRedisClient;
import org.jredis.ri.alphazero.JRedisPipelineService;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import org.jredis.ri.alphazero.support.Log;

import static org.jredis.ri.alphazero.support.DefaultCodec.*;

/**
 * JRedisPipelineService provides blocking synchronous semantics backed by a pipeline and is suitable
 * for concurrent usages with a single (socket) connection to the server.  There is really nothing
 * different here than using a vanial {@link JRedisClient}.
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Nov 6, 2009
 * @since   alpha.0
 * 
 */

public class UsingJRedisPipelineService {

	final JRedis jredis;
	private UsingJRedisPipelineService() {
		// same as usual.
		ConnectionSpec spec = DefaultConnectionSpec.newSpec();
		spec.setDatabase(11).setCredentials("jredis".getBytes());
		
		// only need to use the specific class.
		jredis = new JRedisPipelineService(spec);
	}
	
    private  void run () {
    	try {
	        jredis.ping();
        	basicStuff();
	        elicitErrors();
        }
        catch (RedisException e) {
	        e.printStackTrace();
        }
    	
    	// Use the connection concurrently

    	final int wcnt = 10;
    	final int opcnt = 20000;
    	final Thread[] workers = new Thread[wcnt];
    	
    	final JRedis client = jredis;
    	for(int i=0; i<workers.length; i++) {
    		final int j = i;
    		workers[i] = new Thread(new Runnable() {
				@Override
				public void run() {
			        final String wkey = "foo" + j;
			        String wvalue = null;
					for(int i=0; i< opcnt; i++) {
				        try {
				        	client.incr(wkey);
					        wvalue = toStr(client.get(wkey));
						} catch (RedisException e) {
							e.printStackTrace();
							break;
						}
					}
			        System.out.format("%s => %s\n", wkey, wvalue);
				}
			});
    		workers[i].start();
    	}
    	for(Thread t : workers){
    		try {
				t.join();
			} 
    		catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    	
    	// 
		jredis.quit();
    }
    
    private void elicitErrors ()  {
        String key = "foo"	;
        try {
	        jredis.set(key, "bar");
	        jredis.sadd(key, "foobar");
        }
        catch (RedisException e) {
        	Log.log("Expected elicited error: %s", e.getMessage());
        }
    }

	private void basicStuff () throws RedisException {
        jredis.flushdb();
        String key = "foo"	;
        jredis.set(key, "bar");
        String value = toStr(jredis.get(key));
        System.out.format("%s => %s\n", key, value);
    }

	public static void main (String[] args) {
		(new UsingJRedisPipelineService()).run();
	}
}
