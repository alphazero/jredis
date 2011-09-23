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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jredis.JRedis;
import org.jredis.JRedisFuture;
import org.jredis.connector.ConnectionSpec;
import org.jredis.protocol.ResponseStatus;
import org.jredis.ri.alphazero.support.DefaultCodec;
import org.jredis.ri.alphazero.support.Log;

/**
 * Example usage of {@link JRedisFuture}, demonstrating the asynchronous semantics.
 * <p>
 * Note that this is an abstract class and concrete extensions will use specific
 * implementation of {@link JRedisFuture}.  Refer to the extension class for the
 * specifics of the associated {@link JRedisFuture} provider class.
 * 
 * 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Oct 11, 2009
 * @since   alpha.0
 * 
 */

public abstract class UsingJRedisFuture {
	
	// ------------------------------------------------------------------------
	// EXAMPLES:
	// ------------------------------------------------------------------------

	/**
	 * The basics of using {@link Future} objects and {@link JRedisFuture}.
	 * <p>Note the key differences between using {@link JRedis} and {@link JRedisFuture}:
	 * <ul>
	 * <li>Methods have identical parameters, but all asynchronous methods return a flavor of {@link Future}
	 * <li>Exception handling is significantly different.  
	 * </ul>
	 * @throws InterruptedException 
	 */
	@SuppressWarnings("boxing")
	public void theBasics () throws InterruptedException {
		Log.log("theBasics()");
		
		/*******************************************/
		// fire and forget -- no exception handling and not even bothering about the response
		/*******************************************/
		// this may throw exceptions on the invoke
		jredis.ping();
		
		/*******************************************/
		// invoke and wait for response -- was it OK?
		/*******************************************/
		Future<ResponseStatus> fResp = null;
		try {
			/*
			 * Alright, asynch call is just like the synchronous flavor, but the return type is
			 * a Future<?> object.  Here we are doing a PING which simply returns a STATUS response
			 * from redis, so the return type is Future<ResponseStatus>
			 * 
			 */
			fResp = jredis.ping();
			
			/*
			 * So we have a Future<?> object, but is that the actual response?  No.  You need to wait
			 * for it to actually complete.
			 * 
			 * Here we're waiting using blocking semantics:  we wait until its done.
			 */
	        ResponseStatus response = fResp.get();
	        
	        /**
	         * So we have our response at this point.  We can check it, etc.
	         */
	        if(response.isError()){
	        	Log.error("PING returned an ERR response -- is it an authorization issue?");
	        }
        }
        catch (InterruptedException e) {
        	/*
        	 * This can happen if the calling thread (which is under your control, not JRedis) is
        	 * interrupted.  Given that, its entirely up to you to determine what you should do here.
        	 * If this is all greek to you, then simply log and re-throw it back up, or add InterruptedException
        	 * to your method signatures.    
        	 */
        	Log.problem("thread was interrupted while waiting for the response to be processed.");
        	throw e;
        }
        catch (ExecutionException e) {
        	/**
        	 * Remember the exceptions that a synchronous JRedis method can throw (ClientRuntime, ProviderException, 
        	 * and of course RedisException)?  Well, ExecutionException is the overall wrapper exception that is thrown
        	 * by (all) Future instances to permit the propagation of those exceptions to the caller in an asynch manner.
        	 * So, basically, if any problems arose after your call successfully returned (from the JRedisFuture method),
        	 * here/now is where/when you find out about it.
        	 * 
        	 * So what need to happen is for you to get the underlying 'cause' of the generalized ExecutionException.
        	 */
        	Log.problem("An execution exception occurred");
        }
		
		/*******************************************/
		// invoke and wait for response using timeouts 
		/*******************************************/
        byte[] setval = "bar".getBytes();
		try {
			for(int i=0; i<100000; i++){
				jredis.ping();
				jredis.incr("cntr");
			}
			jredis.set("foo", setval);
			
			/*
			 * Here we're waiting using blocking semantics with timeout
			 */
			Future<byte[]> fVal = jredis.get("foo");
			long t1 = System.nanoTime();
			int toCnt = 0;
	        while(true){
	        	try {
					byte[] val = fVal.get(100L, TimeUnit.MICROSECONDS);
					String value = DefaultCodec.toStr(val);
					System.out.format("=> %s\n", value);
					break;
	        	}
	            catch (TimeoutException e) {
	            	toCnt++;
//	            	System.out.println('.');
	            }
	        }
			t1 = System.nanoTime() - t1;
        	Log.log("done after %d timeouts (%d nanos)", toCnt, t1);
	        
        }
        catch (InterruptedException e) {
        	/*
        	 * This can happen if the calling thread (which is under your control, not JRedis) is
        	 * interrupted.  Given that, its entirely up to you to determine what you should do here.
        	 * If this is all greek to you, then simply log and re-throw it back up, or add InterruptedException
        	 * to your method signatures.    
        	 */
        	Log.problem("thread was interrupted while waiting for the response to be processed.");
        	throw e;
        }
        catch (ExecutionException e) {
        	/**
        	 * Remember the exceptions that a synchronous JRedis method can throw (ClientRuntime, ProviderException, 
        	 * and of course RedisException)?  Well, ExecutionException is the overall wrapper exception that is thrown
        	 * by (all) Future instances to permit the propagation of those exceptions to the caller in an asynch manner.
        	 * So, basically, if any problems arose after your call successfully returned (from the JRedisFuture method),
        	 * here/now is where/when you find out about it.
        	 * 
        	 * So what need to happen is for you to get the underlying 'cause' of the generalized ExecutionException.
        	 */
        	Log.problem("An execution exception occurred");
        }
		
	}
	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	/** {@link JRedisFuture} provider instance */
	protected final JRedisFuture jredis;
	
	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------
	/**
	 * Creates a new instances and runs the example methods.
	 * @param connectionSpec used for creating the connection
	 */
	public UsingJRedisFuture (ConnectionSpec connectionSpec){
		this.jredis = getProviderInstance(connectionSpec);
		try {
	        runExamples();
        }
        catch (InterruptedException e) {
        	Log.problem("Interrupted while running the examples.");
	        e.printStackTrace();
        }
        jredis.quit();
	}
	
	/**
	 * @throws InterruptedException 
     * 
     */
    private void runExamples () throws InterruptedException {
		Log.log("running the JRedisFuture usage examples with %s as the provider implementation.", jredis.getClass().getSimpleName());
		
    	theBasics();
    }

	// ------------------------------------------------------------------------
	// Extension point
	// ------------------------------------------------------------------------
	/**
	 * Extension point.
	 * @param connectionSpec used for creating the connection.
	 * @return the {@link JRedisFuture} implementation instance
	 */
	abstract
	protected JRedisFuture getProviderInstance (ConnectionSpec connectionSpec);
}
