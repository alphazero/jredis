package org.jredis.ri.adhoc;

import java.util.Map;

import org.jredis.JRedisFuture;
import org.jredis.RedisInfo;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.JRedisPipeline;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import org.jredis.ri.alphazero.support.Log;

public class AdHocTestInfo {
	public static void main(String[] args) throws Throwable {
		try {
			new AdHocTestInfo().run();
		} catch (Exception e) {
			Log.error(e.getMessage());
		}
	}
	final ConnectionSpec spec;
	JRedisFuture jredis = null;
	public AdHocTestInfo() throws Throwable{
		spec = DefaultConnectionSpec.newSpec().setCredentials("jredis".getBytes());
		jredis = new JRedisPipeline(spec);
	}
	/** this is not supposed to get called unless you actually run redis on port 9999 :P */
	public void run() {
		try {
			Map<String, String> infomap = jredis.info().get();
			for(String k : infomap.keySet()){
				System.out.format("%s => %s\n", k, infomap.get(k));
			}
			
			System.out.format("\n\n### InfoMap needs to be updated to include ###\n");
			for(String k : infomap.keySet()){
				try {
					RedisInfo.valueOf(k);
				}
				catch (IllegalArgumentException e){
					System.out.format("%s,\n", k);
				}
			}
			System.out.format("### END ###\n");
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
		finally {
			jredis.quit();
		}
	}
}
