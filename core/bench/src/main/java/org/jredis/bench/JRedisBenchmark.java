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

import static org.jredis.bench.Util.getRandomString;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
//import org.jredis.ClientRuntimeException;
//import org.jredis.JRedis;
//import org.jredis.RedisException;
//import org.jredis.bench.Util.Timer;
//import org.jredis.protocol.Command;
//
import org.jredis.ClientRuntimeException;
import org.jredis.JRedis;
import org.jredis.RedisException;
import org.jredis.bench.Util.Timer;
import org.jredis.protocol.Command;

/**
 * Runs a few benchmarks for the {@link SocketConnection}, using a concurrent set of workers, each with its own
 * unique connection to the Redis server.  
 *  
 * @author Joubin Houshyar (alphazero@sensesay.net)
 */
public abstract class JRedisBenchmark {
	
	/** password used to AUTH with redis select -- password is: jredis */
	public static final String password = "jredis";

	// ------------------------------------------------------------------------
	// Helper methods
	// ------------------------------------------------------------------------

	// ------------------------------------------------------------------------
	// For test data 
	// ------------------------------------------------------------------------
	final static Random 		random = new Random(System.currentTimeMillis());
	
	/**  */
	byte[] 			fixedbytes;

	/**  */
	static List<String>		stringList = new ArrayList<String>();
		
	protected boolean quitOnRunEnd = true;
	/**
     * @param b
     */
    protected void quitOnRunEnd (boolean flag) { this.quitOnRunEnd = flag; }
	// ------------------------------------------------------------------------
	// Extension Points
	// ------------------------------------------------------------------------
	protected abstract Class<? extends JRedis> getImplementationClass();
	/**
	 * Define this method to test a specific JRedis implementation.
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	protected abstract JRedis newConnection(String host, int port, int db, String password) throws ClientRuntimeException;

	/**
	 * Runs a set of command primitives test runs using concurrent clients. [TODO: add all commands - this is a sampling of a few.]
	 * <p><b>Be advised that this will FLUSH the db specified.</b>  Defaults to database at index 13:  i.e. <code>jredis.select(13).flushdb()</code>. 
	 * This bench will use <code>AUTH</code> to make sure you do not accidentally destroy important data
	 * when runnign the benchmark.  (If you don't have a password on your db, it can't be that important, right?)
	 * 
	 * <p>Further note that this will use the password <code><b>jredis</b></code> so either
	 * make sure the redis.conf <code>requirepass</code> is set appropriately or simply comment it out.
	 * 
	 * @param host
	 * @param port
	 * @param threadCnt
	 * @param reqCnt
	 * @param size
	 * @param db
	 */
	
	protected final void  runBenchmarks(String host, int port, int threadCnt, int reqCnt, int size, int db)
	{
//		random = new Random(System.currentTimeMillis());
		
		fixedbytes = new byte[size];
		random.nextBytes(fixedbytes);
		/** setup data */
		for(int i=0; i<reqCnt; i++){
//			keys.add(getRandomString (48));
//			patternList.add(getRandomString(random.nextInt(10)+2) + patternA + getRandomString(random.nextInt(10)+2));
//			uniqueSet.add(getRandomString(48));
//			commonSet.add(getRandomString(48));
//			set1.add("set_1" + getRandomString(20));
//			set2.add("set_2" + getRandomString(20));
//			dataList.add(getRandomBytes (128));
			stringList.add(getRandomString (128));
//			objectList.add(new TestBean("testbean." + i));
//			intList.add(random.nextInt());
//			longList.add(random.nextLong());
		}
		BenchmarkWorker[] workers = new BenchmarkWorker[threadCnt];

		System.out.println ();
		System.out.println("-------------------------------------------------------------------- JREDIS ----");
		System.out.println("---");
		System.out.format ("--- Benchmarking JRedis provider: %s\n", getImplementationClass().getName());
		System.out.format ("--- host:%s:%d (db:%d) | bytes:%d | threads:%d | reqs/conn:%d \n", host, port, db, size ,threadCnt, reqCnt);
		System.out.println("---");
		System.out.println("--------------------------------------------------------------------------------\n\n");

		for(int i=0;i<threadCnt;i++) workers[i] = newPingWorker (host, port, db);
		Benchmarker.runBenchmark (Command.PING, reqCnt, threadCnt, workers);

		for(int i=0;i<threadCnt;i++) workers[i] = newSetWorker (host, port, db);
		Benchmarker.runBenchmark (Command.SET, reqCnt, threadCnt, workers);

		for(int i=0;i<threadCnt;i++) workers[i] = newSetWorker (host, port, db);
		Benchmarker.runBenchmark (Command.GET, reqCnt, threadCnt, workers);

		for(int i=0;i<threadCnt;i++) workers[i] = newSetnxWorker (host, port, db);
		Benchmarker.runBenchmark (Command.SETNX, reqCnt, threadCnt, workers);

		for(int i=0;i<threadCnt;i++) workers[i] = newIncrWorker (host, port, db);
		Benchmarker.runBenchmark (Command.INCR, reqCnt, threadCnt, workers);

		for(int i=0;i<threadCnt;i++) workers[i] = newDecrWorker (host, port, db);
		Benchmarker.runBenchmark (Command.DECR, reqCnt, threadCnt, workers);

		for(int i=0;i<threadCnt;i++) workers[i] = newIncrbyWorker (host, port, db);
		Benchmarker.runBenchmark (Command.INCRBY, reqCnt, threadCnt, workers);

		for(int i=0;i<threadCnt;i++) workers[i] = newDecrbyWorker (host, port, db);
		Benchmarker.runBenchmark (Command.DECRBY, reqCnt, threadCnt, workers);

		for(int i=0;i<threadCnt;i++) workers[i] = newDbsizeWorker (host, port, db);
		Benchmarker.runBenchmark (Command.DBSIZE, reqCnt, threadCnt, workers);

		for(int i=0;i<threadCnt;i++) workers[i] = newLPushWorker (host, port, db);
		Benchmarker.runBenchmark (Command.LPUSH, reqCnt, threadCnt, workers);

		for(int i=0;i<threadCnt;i++) workers[i] = newRPushWorker (host, port, db);
		Benchmarker.runBenchmark (Command.RPUSH, reqCnt, threadCnt, workers);

		for(int i=0;i<threadCnt;i++) workers[i] = newLPopWorker (host, port, db);
		Benchmarker.runBenchmark (Command.LPOP, reqCnt, threadCnt, workers);

		for(int i=0;i<threadCnt;i++) workers[i] = newRPopWorker (host, port, db);
		Benchmarker.runBenchmark (Command.RPOP, reqCnt, threadCnt, workers);

		for(int i=0;i<threadCnt;i++) workers[i] = newLLenWorker (host, port, db);
		Benchmarker.runBenchmark (Command.LLEN, reqCnt, threadCnt, workers);

		for(int i=0;i<threadCnt;i++) workers[i] = newScardWorker (host, port, db);
		Benchmarker.runBenchmark (Command.SCARD, reqCnt, threadCnt, workers);

		
		for(int i=0;i<threadCnt;i++) workers[i] = newSaddWorker (host, port, db);
		Benchmarker.runBenchmark (Command.SADD, reqCnt, threadCnt, workers);

		for(int i=0;i<threadCnt;i++) workers[i] = newSremWorker (host, port, db);
		Benchmarker.runBenchmark (Command.SREM, reqCnt, threadCnt, workers);

		//		for(int i=0;i<connectionCnt;i++) workers[i] = newSmembersWorker();
//		Benchmarker.runBenchmark (Command.SMEMBERS, reqCnt, connectionCnt, workers);
		
	}
	// ------------------------------------------------------------------------
	// The workers
	// ------------------------------------------------------------------------
	
	/** does the PING */
	public  final BenchmarkWorker newPingWorker (String host, int port, int db) {
		return new BenchmarkWorker (host, port, db){
			@Override
			protected void prep() {}
			@Override
			protected void work() {
				for(int i=0; i<reqCnt; i++){ 
					try {
						jredis.ping();
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
		};
	}
	
	/** does the DBSIZE */
	public  final BenchmarkWorker newDbsizeWorker (String host, int port, int db) {
		return new BenchmarkWorker (host, port, db){
			@Override
			protected void prep() {}
			@Override
			protected void work() {
				for(int i=0; i<reqCnt; i++){ 
					try {
						jredis.dbsize();
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
		};
	}

	/** does the SADD */
	public final BenchmarkWorker newSaddWorker (String host, int port, int db) {
		return new BenchmarkWorker (host, port, db){
			String key = "num_" + id;
			@Override
			protected void prep() { }
			@Override
			protected void work() {
				for(int i=0; i<reqCnt; i++) { 
					try {
						jredis.sadd (key, i);
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
		};
	}
	
	/** does the SREM */
	public final BenchmarkWorker newSremWorker (String host, int port, int db) {
		return new BenchmarkWorker (host, port, db){
			String key = "num_" + id;
			@Override
			protected void prep() { 
				for(int i=0; i<reqCnt; i++) { 
					try {
						jredis.sadd (key, i);
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
			@Override
			protected void work() {
				for(int i=0; i<reqCnt; i++) { 
					try {
						jredis.srem (key, i);
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
		};
	}
	
	/** does the SCARD */
	public final BenchmarkWorker newScardWorker (String host, int port, int db) {
		return new BenchmarkWorker (host, port, db){
			String key = "num_" + id;
			@Override
			protected void prep() { 
				for(int i=0; i<reqCnt; i++) { 
					try {
						jredis.sadd (key, i);
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
			@Override
			protected void work() {
				for(int i=0; i<reqCnt; i++) { 
					try {
						jredis.scard (key);
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
		};
	}
	
	/** does the LLEN */
	public final BenchmarkWorker newLLenWorker (String host, int port, int db) {
		return new BenchmarkWorker (host, port, db){
			String key = "num_" + id;
			@Override
			protected void prep() { 
				for(int i=0; i<reqCnt; i++) { 
					try {
						jredis.lpush (key, 1);
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
			@Override
			protected void work() {
				for(int i=0; i<reqCnt; i++) { 
					try {
						jredis.llen (key);
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
		};
	}
	
	/** does the LPOP */
	public final BenchmarkWorker newLPopWorker (String host, int port, int db) {
		return new BenchmarkWorker (host, port, db){
			String key = "num_" + id;
			@Override
			protected void prep() { 
				for(int i=0; i<reqCnt; i++) { 
					try {
						jredis.lpush (key, 1);
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
			@Override
			protected void work() {
				for(int i=0; i<reqCnt; i++) { 
					try {
						jredis.lpop (key);
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
		};
	}

	/** does the RPOP */
	public final BenchmarkWorker newRPopWorker (String host, int port, int db) {
		return new BenchmarkWorker (host, port, db){
			String key = "num_" + id;
			@Override
			protected void prep() { 
				for(int i=0; i<reqCnt; i++) { 
					try {
						jredis.lpush (key, 1);
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
			@Override
			protected void work() {
				for(int i=0; i<reqCnt; i++) { 
					try {
						jredis.rpop (key);
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
		};
	}

	/** does the LPUSH */
	public final BenchmarkWorker newLPushWorker (String host, int port, int db) {
		return new BenchmarkWorker (host, port, db){
			String key = "num_" + id;
			@Override
			protected void prep() { }
			@Override
			protected void work() {
				for(int i=0; i<reqCnt; i++) { 
					try {
						jredis.lpush (key, i);
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
		};
	}

	/** does the RPUSH */
	public final BenchmarkWorker newRPushWorker (String host, int port, int db) {
		return new BenchmarkWorker (host, port, db){
			String key = "num_" + id;
			@Override
			protected void prep() { }
			@Override
			protected void work() {
				for(int i=0; i<reqCnt; i++) { 
					try {
						jredis.rpush (key, i);
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
		};
	}

	/** does the INCR */
	public final BenchmarkWorker newIncrWorker (String host, int port, int db) {
		return new BenchmarkWorker (host, port, db){
			String key = "num_" + id;
			@Override
			protected void prep() { }
			@Override
			protected void work() {
				for(int i=0; i<reqCnt; i++) { 
					try {
						jredis.incr (key);
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
		};
	}

	/** does the DECR */
	public final BenchmarkWorker newDecrWorker (String host, int port, int db) {
		return new BenchmarkWorker (host, port, db){
			String key = "num_" + id;
			@Override
			protected void prep() { }
			@Override
			protected void work() {
				for(int i=0; i<reqCnt; i++) { 
					try {
						jredis.decr (key);
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
		};
	}
	/** does the INCRBY */
	public final BenchmarkWorker newIncrbyWorker (String host, int port, int db) {
		return new BenchmarkWorker (host, port, db){
			String key = "num_" + id;
			@Override
			protected void prep() { }
			@Override
			protected void work() {
				for(int i=0; i<reqCnt; i++) { 
					try {
						jredis.incrby (key, 10);
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
		};
	}

	/** does the DECR */
	public final BenchmarkWorker newDecrbyWorker (String host, int port, int db) {
		return new BenchmarkWorker (host, port, db){
			String key = "num_" + id;
			@Override
			protected void prep() { }
			@Override
			protected void work() {
				for(int i=0; i<reqCnt; i++) { 
					try {
						jredis.decrby (key, 10);
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
		};
	}

	/** does the SET */
	public  final BenchmarkWorker newSetWorker (String host, int port, int db) {
		return new BenchmarkWorker (host, port, db){
			byte[] data = fixedbytes;
			String key = id + ":fixedbytes:string";
			@Override
			protected void prep() {  }
			@Override
			protected void work() {
				for(int i=0; i<reqCnt; i++){ 
					try {
						jredis.set(key, data);
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
		};
	}

	/** does the SETNX */
	public  final BenchmarkWorker newSetnxWorker (String host, int port, int db) {
		return new BenchmarkWorker (host, port, db){
			byte[] data = fixedbytes;
			String key = id + ":fixedbytes:string";
			@Override
			protected void prep() {  }
			@Override
			protected void work() {
				for(int i=0; i<reqCnt; i++){ 
					try {
						jredis.setnx (key, data);
					}
					catch (RedisException e) { e.printStackTrace(); }
				}
			}
		};
	}
	/** does the GET */
	public  final BenchmarkWorker newGetWorker (String host, int port, int db) {
		return new BenchmarkWorker (host, port, db){
			byte[] data = fixedbytes;
			String key = id + ":fixedbytes:string";
			@Override
			protected void prep() {
				try {
					for(int i=0; i<reqCnt; i++){ 
						jredis.set(key, data);
					}
				}
				catch (RedisException e) { e.printStackTrace(); }
			}
			@Override
			protected void work() {
				try {
					for(int i=0; i<reqCnt; i++){ 
						jredis.get("fixedbytes_"+id);
					}
				}
				catch (RedisException e) { e.printStackTrace(); }
			}
		};
	}
	/** does the SMEMBERS */
	public  final BenchmarkWorker newSmembersWorker (String host, int port, int db) {
		return new BenchmarkWorker (host, port, db){
			List<String>  values = JRedisBenchmark.stringList;
			String key = id + ":stringList:set";
			@Override
			protected void prep() {
				try {
					for(int i=0; i<values.size(); i++){ 
						jredis.sadd (key, values.get(i));
					}
				}
				catch (RedisException e) { e.printStackTrace(); }
			}
			@Override
			protected void work() {
				try {
					for(int i=0; i<reqCnt; i++){ 
						jredis.smembers(key);
					}
				}
				catch (RedisException e) { e.printStackTrace(); }
			}
		};
	}
	
	// ------------------------------------------------------------------------
	// The Benchmarker
	// ------------------------------------------------------------------------
	
	public static class Benchmarker {
		public static final void runBenchmark(final Command cmd, final int reqCnt, final int threadCnt, final BenchmarkWorker[] workers) {
			new Benchmarker(cmd, reqCnt, threadCnt, workers).runBenchmark();
		}
		private Command cmd;
		private BenchmarkWorker[] workers;
		private int threadCnt;
		private int reqCnt;
		private Benchmarker (final Command cmd, final int reqCnt, final int threadCnt, final BenchmarkWorker[] workers) {
			this.cmd = cmd;
			this.reqCnt = reqCnt;
			this.threadCnt = threadCnt;
			this.workers = workers;
		}
		
		private void runBenchmark () {
			
			String host = workers[0].host;
			
			final long[]   			deltas = new long[threadCnt];
			final CountDownLatch 	completion = new CountDownLatch(threadCnt);
			final CountDownLatch 	ready = new CountDownLatch(threadCnt);
			final CountDownLatch 	mark = new CountDownLatch(1);

			for(int i=0; i<threadCnt; i++){
				workers[i].id = i;
				workers[i].completion = completion;
				workers[i].mark = mark;
				workers[i].ready = ready;
				workers[i].reqCnt = reqCnt;
				workers[i].deltas = deltas;
				new Thread (workers[i]).start();
			}
			
			try {
				/* run benchmark workers */ 
				ready.await();										// ready
				Timer timer = Timer.startNewTimer();				// set
				mark.countDown();   								// go
				
				System.out.print(" ...\n");
				completion.await(); 								// ... done

				timer.mark();
				
				/* report */ 
					// overall - this is throughput
				System.out.format("===== %s =====\n",cmd.code);
				System.out.format("%d concurrent clients (%d %ss each) [host: %s]\n", threadCnt, reqCnt, cmd.code, host);
				System.out.format("  ==> %d total requests @ %f seconds\n", threadCnt*reqCnt, (float)timer.deltaAtMark(TimeUnit.SECONDS));
				System.out.format("  ==> %f/second\n", (float)timer.opsPerSecAtMark((long)threadCnt*reqCnt));
				System.out.println();
				
					// report for each - this is response time
				long max = Long.MIN_VALUE;
				long min = Long.MAX_VALUE;
				for(int i=0; i<threadCnt; i++){
					min = deltas[i]<min ? deltas[i] : min;
					max = deltas[i]>max ? deltas[i] : max;
				}
				System.out.format("\t\t\tmin: %s msecs\n\t\t\tmax: %s msecs\n\n", min, max);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	// ------------------------------------------------------------------------
	// Base for all workers
	// ------------------------------------------------------------------------
	
	/**
	 * Abstract base class for all benchmark workers.  This is a runnable that 
	 * uses the template pattern in the run method and takes care of common
	 * time keeping tasks for the specific (child class) workers.
	 */
	public abstract class BenchmarkWorker implements Runnable {
		final String host;
		final int port;
		
		long[] deltas;
		CountDownLatch ready;
		CountDownLatch completion;
		CountDownLatch mark;
		int reqCnt;
		int id; 
		int db;

		JRedis jredis = null;
		
		public BenchmarkWorker (String host, int port, int db) {
			this.host = host;
			this.port = port;
			this.db = db;
		}
		
		/** 
		 * <p>1 - connects to the redis server
		 * <p>2 - calls the {@link BenchmarkWorker#prep()} -- subclasses will implement this
		 * <p>3 - waits for the mark signal to start the calls
		 * <p>4 - gets the sys time, and ..
		 * <p>5 - calls the {@link BenchmarkWorker#work()} method -- subclasses will implement this
		 * <p>6 - gets the sys time -- this is the time delta for this worker
		 * <p>7 - closes the connection, and signals completion to the benchmarker.
		 */
		public void run() {
			try {

				jredis = newConnection (host, 6379, db, password);
				try {
//					jredis.auth (password).select(db).flushdb();
					jredis.flushdb();
				}
				catch (RedisException e) {				
					System.err.format("BENCHMARK::REDIS %s ERROR => %s\nWorker will stop.\n", e.getCommand(), e.getLocalizedMessage());
					System.exit (1);
				}
				
				prep();
				ready.countDown();
				mark.await();		// wait sig
				long start=0;
				start = System.currentTimeMillis();
				work();
				deltas[id] = System.currentTimeMillis() - start;
				
				if(quitOnRunEnd)
					jredis.quit();
				completion.countDown();  // sig done
			} 
			catch (Exception e){
				System.err.format("BENCHMARK::Exception => %s\nWill stop.\n", e.getLocalizedMessage());
			}
		}
		/** anything that needs to be done to setup the calls -- typically all time consuming data setup tasks go here ...*/
		protected abstract void prep();
		/** Typically just a tight loop calling the redis server for the given test and using the data prep'd in {@link BenchmarkWorker#prep()} */
		protected abstract void work();
	}
}
