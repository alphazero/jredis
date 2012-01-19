package org.jredis.ri.alphazero.bench;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jredis.JRedisFuture;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.JRedisPipeline;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import org.jredis.ri.alphazero.support.Log;

public class SimpleBenchJRedisPipeline {
	public static void main(String[] args) {
		new SimpleBenchJRedisPipeline().run();
	}

	private void run() {
		int database = 11;
		ConnectionSpec connSpec = DefaultConnectionSpec.newSpec("localhost", 6379, database, "jredis".getBytes());
		JRedisFuture jredis = new JRedisPipeline(connSpec);
		
		byte[] key = "pipe".getBytes();
		int iters = 100 * 1000;
		
		try {
			cleandb(jredis);
		} catch (Throwable e) {
			e.printStackTrace();
			return;
		}
		
		for(;;){
			Future<Long> frCounter = null;
			long start = System.nanoTime();
			for(int i=0;i<iters; i++){
				frCounter = jredis.incr(key);
			}
			long queued = System.nanoTime() - start;
			try {
				long counter = frCounter.get();
				long delta_ns = System.nanoTime() - start;
				long delta_ms = TimeUnit.MILLISECONDS.convert(delta_ns, TimeUnit.NANOSECONDS);
				float opsrate = iters/delta_ms;
				Log.log ("counter: %d  msec:%d ops/msecs:%f  [q-delta:%d] [delta:%d]", counter, delta_ms, opsrate, queued, delta_ns);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			} catch (ExecutionException e) {
				e.printStackTrace();
				break;
			}
		}
	}

	private void cleandb(JRedisFuture jredis) throws InterruptedException, ExecutionException {
		jredis.flushdb().get();
	}
}
