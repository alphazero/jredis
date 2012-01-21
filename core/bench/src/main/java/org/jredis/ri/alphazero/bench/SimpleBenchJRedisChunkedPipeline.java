package org.jredis.ri.alphazero.bench;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jredis.JRedisFuture;
import org.jredis.connector.ConnectionSpec;
import org.jredis.protocol.ResponseStatus;
import org.jredis.ri.alphazero.JRedisChunkedPipeline;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import org.jredis.ri.alphazero.support.Log;

public class SimpleBenchJRedisChunkedPipeline implements Runnable {
	public static void main(String[] args) {
		Runnable bench = new SimpleBenchJRedisChunkedPipeline();
		Thread bt = new Thread(bench, "");
		bt.start();
		try {
			bt.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void run() {
		int database = 11;
		ConnectionSpec connSpec = DefaultConnectionSpec.newSpec("localhost", 6379, database, "jredis".getBytes());
		JRedisFuture jredis = new JRedisChunkedPipeline(connSpec);
		
		byte[] key = "cpct".getBytes();
		int iters = 100000;
		
		for(;;){
			Future<Long> frCounter = null;
			long start = System.nanoTime();
			for(int i=0;i<iters; i++){
				frCounter = jredis.incr(key);
			}
			long queued = System.nanoTime() - start;
			try {
				jredis.flush();
				long counter = frCounter.get();  // NOTE: excellent place to put implicit flush()
				long delta_ns = System.nanoTime() - start;
				long delta_ms = TimeUnit.MILLISECONDS.convert(delta_ns, TimeUnit.NANOSECONDS);
				float opsrate = iters/delta_ms;
				Log.log("counter: %d  msec:%d ops/msecs:%f  [q-delta:%d] [delta:%d]", counter, delta_ms, opsrate, queued, delta_ns);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			} catch (ExecutionException e) {
				e.printStackTrace();
				break;
			}
		}
	}

	@SuppressWarnings("unused")
	private void cleandb(JRedisFuture jredis) throws InterruptedException, ExecutionException {
		Future<ResponseStatus> fr = jredis.flushdb();
		jredis.flush();
		fr.get();
	}
}
