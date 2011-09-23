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

import static org.jredis.connector.Connection.Socket.Property.SO_RCVBUF;
import static org.jredis.connector.Connection.Socket.Property.SO_SNDBUF;
import static org.jredis.ri.alphazero.support.DefaultCodec.toLong;
import static org.jredis.ri.alphazero.support.DefaultCodec.toStr;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.jredis.JRedisFuture;
import org.jredis.ProviderException;
import org.jredis.RedisException;
import org.jredis.bench.Util;
import org.jredis.bench.Util.Timer;
import org.jredis.connector.ConnectionSpec;
import org.jredis.protocol.ResponseStatus;
import org.jredis.ri.alphazero.JRedisPipeline;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import org.jredis.ri.alphazero.support.Log;

/**
 * Pipelines are an order of magnitude faster than the request/reply connectors.
 * Get a sense of how much faster it is.
 * 
 * <p>use JVM flags -server -Xms512m -Xmx2560m for better results (adjust your
 * mem settings per your box's limits.)
 *  
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Nov 5, 2009
 * @since   alpha.0
 * 
 */

public class PipelineInAction {
	@SuppressWarnings("boxing")
	public static void main (String[] args) {
    	final ConnectionSpec spec = DefaultConnectionSpec.newSpec();
    	spec.setCredentials("jredis".getBytes());
    	spec.setDatabase(13);
    	spec.setSocketProperty(SO_RCVBUF, 1024 * 24);
    	spec.setSocketProperty(SO_SNDBUF, 1024 * 24);
    	
    	usingSynchSemantics(spec);
    	final boolean forever = true;
    	
    	runJRedisPipelineSET (spec, 100000, 3, forever);
	}
    /**
     * @param spec
     */
    @SuppressWarnings("boxing")
	private static void usingSynchSemantics (ConnectionSpec spec) {
    	JRedisPipeline pipeline = new JRedisPipeline(spec);
    	try {
    		long start = System.currentTimeMillis();
	        pipeline.ping();
	        pipeline.flushall();
	        String cntrKey = "my-cntr";
	        
	        Random rand = new Random();
	        byte[] data = new byte[8];
	        for(int i=0; i<100000; i++)
	        	pipeline.incr(cntrKey);
	        
	        long cntr = toLong (pipeline.sync().get(cntrKey));
	        
	        for(int i=0; i<100000; i++){
	        	rand.nextBytes(data);
	        	pipeline.set("random:"+i, "value:" + rand.nextInt());
	        }
	        String randomVal = toStr (pipeline.sync().get("random:"+999));
    		System.out.format ("end using sync() = %d msec\n", System.currentTimeMillis() - start);
	        
	        System.out.format("%s => %d\n", cntrKey, cntr);
	        System.out.format("%s => %s\n", "random:"+999, randomVal);
	        
        }
        catch (RedisException e) {
        	Log.problem("RedisException: " + e);
        }
        finally{
        	pipeline.sync().quit();
        }

    }
	/**
     * pipelines SET reqCnt times and then waits on response of the last INCR.
     * If foverever flag is true, will do so forever.  Each cycle prints out timing stats.
     * @param spec
     * @param reqCnt
     * @param forever
     */
    @SuppressWarnings({ "unused", "boxing" })
    private static void runJRedisPipelineGET (ConnectionSpec spec, int reqCnt, int size, boolean forever) {
    	long totTime = 0;
    	long avgRespTime = 0;
    	float avgThroughput = 0;
    	long iters = 0;
    	JRedisFuture pipeline = new JRedisPipeline(spec);
    	try {
    		String key = "pipeKey";
    		byte[] data = new byte[size];
    		(new Random()).nextBytes(data);
			pipeline.del(key);
			pipeline.set(key, data);
			
    		do {
	    		int cnt = 0;
	    		Util.Timer timer = Timer.startNewTimer();
	    		Future<byte[]> futureBytes = null;
	    		while(cnt < reqCnt){
	    			futureBytes = pipeline.get(key);
	    			cnt++;
	    		}
	    		long reqDoneTime = timer.mark();
	    		assert futureBytes != null;
				@SuppressWarnings("null")
				byte[] value = futureBytes.get();
	    		long respDoneTime = timer.mark();
//				System.out.format("JRedisPipeline: %d GETs invoked   @ %5d  (%.2f ops/s)\n", cnt, reqDoneTime, timer.opsPerSecAtDelta(cnt, reqDoneTime));
				float throughput = timer.opsPerSecAtMark(cnt);
//				System.out.format("JRedisPipeline: %d GETs completed @ %5d  (%.2f ops/s) [%d msecs to comp] \n", cnt, timer.deltaAtMark(), throughput, respDoneTime-reqDoneTime);
				if(iters > 0){
					totTime += respDoneTime;
					avgRespTime = (totTime) / iters;
					avgThroughput =(float)( reqCnt * 1000) / (float) avgRespTime;
					System.out.format("JRedisPipeline: %d GETs [%d bytes/GET] average response time @ %dms (%.2f ops/s) last: %dms\n", cnt, data.length, avgRespTime, avgThroughput, respDoneTime);
//					Assert.isEquivalent(data, value);
				}
				iters ++;
//				System.out.println ();
    		} while(forever);

    		pipeline.quit();
        }
        catch (ProviderException e) {
	        e.printStackTrace();
        }
        catch (InterruptedException e) {
	        e.printStackTrace();
        }
        catch (ExecutionException e) {
	        e.printStackTrace();
        }
    }
    @SuppressWarnings({ "unused", "boxing" })
    private static void runJRedisPipelinePING (ConnectionSpec spec, int reqCnt, int size, boolean forever) {
    	long totTime = 0;
    	long avgRespTime = 0;
    	float avgThroughput = 0;
    	long iters = 0;
    	JRedisFuture pipeline = new JRedisPipeline(spec);
    	try {
    		do {
	    		int cnt = 0;
	    		Util.Timer timer = Timer.startNewTimer();
	    		Future<ResponseStatus> futureStat = null;
	    		while(cnt < reqCnt){
	    			futureStat = pipeline.ping();
	    			cnt++;
	    		}
	    		long reqDoneTime = timer.mark();
	    		assert futureStat != null;
				@SuppressWarnings("null")
				ResponseStatus rstat = futureStat.get();
	    		long respDoneTime = timer.mark();
//				System.out.format("JRedisPipeline: %d PINGs invoked   @ %5d  (%.2f ops/s)\n", cnt, reqDoneTime, timer.opsPerSecAtDelta(cnt, reqDoneTime));
				float throughput = timer.opsPerSecAtMark(cnt);
//				System.out.format("JRedisPipeline: %d PINGs completed @ %5d  (%.2f ops/s) [%d msecs to comp] \n", cnt, timer.deltaAtMark(), throughput, respDoneTime-reqDoneTime);
				if(iters > 0){
					totTime += reqDoneTime;
					avgRespTime = (totTime) / iters;
					avgThroughput =(float)( reqCnt * 1000) / (float) avgRespTime;
					System.out.print("\r");
					System.out.format("JRedisPipeline: %d PINGs average response time @ %dms (%.2f ops/s)", cnt, avgRespTime, avgThroughput);
				}
				iters ++;
//				System.out.println ();
    		} while(forever);

    		pipeline.quit();
        }
        catch (ProviderException e) {
	        e.printStackTrace();
        }
        catch (InterruptedException e) {
	        e.printStackTrace();
        }
        catch (ExecutionException e) {
	        e.printStackTrace();
        }
    }
    @SuppressWarnings("boxing")
	private static void runJRedisPipelineLPUSH (ConnectionSpec spec, int reqCnt, int size, boolean forever) {
    	JRedisFuture pipeline = new JRedisPipeline(spec);
    	long totTime = 0;
    	long avgRespTime = 0;
    	float avgThroughput = 0;
    	long iters = 0;
    	try {
    		String key = "pipeKey";
    		byte[] data = new byte[size];
    		(new Random()).nextBytes(data);
			Future<Long> futureLong = pipeline.del(key);
			futureLong.get();
    		do {
	    		int cnt = 0;
	    		Util.Timer timer = Timer.startNewTimer();
	    		Future<ResponseStatus> futureStat = null;
	    		while(cnt < reqCnt){
	    			futureLong = pipeline.lpush(key, data);
	    			cnt++;
	    		}
	    		long reqDoneTime = timer.mark();
	    		assert futureStat != null;
	    		@SuppressWarnings({ "null", "unused" })
				ResponseStatus rstat = futureStat.get();
	    		long respDoneTime = timer.mark();
				System.out.format("JRedisPipeline: %d LPUSHs invoked   @ %5d  (%.2f ops/s)\n", cnt, reqDoneTime, timer.opsPerSecAtDelta(cnt, reqDoneTime));
				System.out.format("JRedisPipeline: %d LPUSHs completed @ %5d  (%.2f ops/s) [%d msecs to comp] \n", cnt, timer.deltaAtMark(), timer.opsPerSecAtMark(cnt), respDoneTime-reqDoneTime);
				if(iters > 0){
					totTime += reqDoneTime;
					avgRespTime = (totTime) / iters;
					avgThroughput =(float)( reqCnt * 1000) / (float) avgRespTime;
					System.out.format("JRedisPipeline: %d LPUSHs average response time @ %dms (%.2f ops/s) \n", cnt, avgRespTime, avgThroughput);
				}
				iters ++;
				System.out.println ();
    		} while(forever);

    		pipeline.quit();
        }
        catch (ProviderException e) {
	        e.printStackTrace();
        }
        catch (InterruptedException e) {
	        e.printStackTrace();
        }
        catch (ExecutionException e) {
	        e.printStackTrace();
        }
    }
   
    /**
     * pipelines SET reqCnt times and then waits on response of the last INCR.
     * If foverever flag is true, will do so forever.  Each cycle prints out timing stats.
     * @param spec
     * @param reqCnt
     * @param forever
     */
    @SuppressWarnings("boxing")
	private static void runJRedisPipelineSET (ConnectionSpec spec, int reqCnt, int size, boolean forever) {
    	JRedisFuture pipeline = new JRedisPipeline(spec);
    	long totTime = 0;
    	long avgRespTime = 0;
    	float avgThroughput = 0;
    	long iters = 0;
    	try {
    		String key = "pipeKey";
    		byte[] data = new byte[size];
    		(new Random()).nextBytes(data);
			Future<Long> futureLong = pipeline.del(key);
			futureLong.get();
    		do {
	    		int cnt = 0;
	    		Util.Timer timer = Timer.startNewTimer();
	    		Future<ResponseStatus> futureStat = null;
	    		while(cnt < reqCnt){
	    			futureStat = pipeline.set(key, data);
	    			cnt++;
	    		}
	    		assert futureStat != null;
	    		@SuppressWarnings({ "null", "unused" })
				ResponseStatus rstat = futureStat.get();
	    		long respDoneTime = timer.mark();
				if(iters > 0){
					totTime += respDoneTime;
					avgRespTime = (totTime) / iters;
					avgThroughput =(float)( reqCnt * 1000) / (float) avgRespTime;
					System.out.format("JRedisPipeline: %d SETs [%d bytes/GET] average response time @ %dms (%.2f ops/s) \n", cnt, data.length, avgRespTime, avgThroughput);
				}
				iters ++;
    		} while(forever);

    		pipeline.quit();
        }
        catch (ProviderException e) {
	        e.printStackTrace();
        }
        catch (InterruptedException e) {
	        e.printStackTrace();
        }
        catch (ExecutionException e) {
	        e.printStackTrace();
        }
    }
    /**
     * pipelines INCRs reqCnt times and then waits on response of the last INCR.
     * If foverever flag is true, will do so forever.  Each cycle prints out timing stats.
     * @param spec
     * @param reqCnt
     * @param forever
     */
    @SuppressWarnings("boxing")
	private static void runJRedisPipelineINCR (ConnectionSpec spec, int reqCnt, int size, boolean forever) {
    	JRedisFuture pipeline = new JRedisPipeline(spec);
    	long totTime = 0;
    	long avgRespTime = 0;
    	float avgThroughput = 0;
    	long iters = 0;
    	try {
    		String key = "pipeCounter";
			Future<Long> futureDelCnt = pipeline.del(key);
			futureDelCnt.get();
    		do {
	    		int cnt = 0;
	    		Util.Timer timer = Timer.startNewTimer();
	    		Future<Long> futureLong = null;
	    		while(cnt < reqCnt){
	    			futureLong = pipeline.incr(key);
	    			cnt++;
	    		}
	    		long reqDoneTime = timer.mark();
	    		assert futureLong != null;
				@SuppressWarnings("null")
				long counter = futureLong.get();
	    		long respDoneTime = timer.mark();
				System.out.format("JRedisPipeline: %d INCRs invoked   @ %5d  (%.2f ops/s)\n", cnt, reqDoneTime, timer.opsPerSecAtDelta(cnt, reqDoneTime));
				System.out.format("JRedisPipeline: %d INCRs completed @ %5d  (%.2f ops/s) [%d msecs to comp] \n", cnt, timer.deltaAtMark(), timer.opsPerSecAtMark(cnt), respDoneTime-reqDoneTime);
				System.out.format ("counter is now: %d\n\n", counter);
				if(iters > 0){
					totTime += reqDoneTime;
					avgRespTime = (totTime) / iters;
					avgThroughput =(float)( reqCnt * 1000) / (float) avgRespTime;
					System.out.format("JRedisPipeline: %d INCRs average response time @ %dms (%.2f ops/s) \n", cnt, avgRespTime, avgThroughput);
				}
				iters ++;
				System.out.println ();
    		} while(forever);

    		pipeline.quit();
        }
        catch (ProviderException e) {
	        e.printStackTrace();
        }
        catch (InterruptedException e) {
	        e.printStackTrace();
        }
        catch (ExecutionException e) {
	        e.printStackTrace();
        }
    }
}
