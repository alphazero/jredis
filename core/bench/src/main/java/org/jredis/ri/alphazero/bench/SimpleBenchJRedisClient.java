package org.jredis.ri.alphazero.bench;

import java.util.concurrent.TimeUnit;

import org.jredis.JRedis;
import org.jredis.RedisException;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.JRedisClient;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;

public class SimpleBenchJRedisClient {
	public static void main(String[] args) {
		try {
			new SimpleBenchJRedisClient().run();
		} catch (RedisException e) {
			e.printStackTrace();
		}
	}

	private void run() throws RedisException {
		int database = 11;
		ConnectionSpec connSpec = DefaultConnectionSpec.newSpec("localhost", 6379, database, "jredis".getBytes());
		JRedis jredis = new JRedisClient(connSpec);
		
		byte[] key = "bench-jredis-pipeline-key".getBytes();
		int iters = 100 * 1000;
		
		try {
			cleandb(jredis);
		} catch (Throwable e) {
			e.printStackTrace();
			return;
		}
		
		for(;;){
			Long counter = null;
			long start = System.nanoTime();
			for(int i=0;i<iters; i++){
				counter = jredis.incr(key);
			}
			long delta_ns = System.nanoTime() - start;
			long delta_ms = TimeUnit.MILLISECONDS.convert(delta_ns, TimeUnit.NANOSECONDS);
			float opsrate = iters/delta_ms;
			System.out.format("counter: %d  msec:%d ops/msecs:%f  [delta:%d]\n", counter, delta_ms, opsrate, delta_ns);
		}
	}

	private void cleandb(JRedis jredis) throws RedisException {
		jredis.flushdb();
	}
}
