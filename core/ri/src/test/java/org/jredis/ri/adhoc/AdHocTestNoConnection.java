package org.jredis.ri.adhoc;

import java.util.concurrent.Future;

import org.jredis.JRedisFuture;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.JRedisPipeline;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import org.jredis.ri.alphazero.support.Log;

public class AdHocTestNoConnection {
	private static final int NOT_A_USUAL_REDIS_PORT = 9999;
	public static void main(String[] args) throws Throwable {
		try {
			new AdHocTestNoConnection().run();
		} catch (Exception e) {
			Log.error(e.getMessage());
		}
	}
	final ConnectionSpec spec;
	JRedisFuture jredis = null;
	public AdHocTestNoConnection() throws Throwable{
		spec = DefaultConnectionSpec.newSpec("localhost", NOT_A_USUAL_REDIS_PORT, 11, "jredis".getBytes());
		jredis = new JRedisPipeline(spec);
	}
	/** this is not supposed to get called unless you actually run redis on port 9999 :P */
	public void run() {
		final byte[] key = "foo".getBytes();
		for(;;){
			try {
				@SuppressWarnings("unused")
				Future<Long> fcntr = jredis.incr(key);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
}
