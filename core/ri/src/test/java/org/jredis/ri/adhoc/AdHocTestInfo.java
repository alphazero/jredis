package org.jredis.ri.adhoc;

import java.util.Map;

import org.jredis.JRedis;
import org.jredis.JRedisFuture;
import org.jredis.RedisInfo;
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.JRedisClient;
import org.jredis.ri.alphazero.JRedisPipeline;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import org.jredis.ri.alphazero.support.Assert;
import org.jredis.ri.alphazero.support.Log;

public class AdHocTestInfo {
	public static void main(String[] args) throws Throwable {
		try {
			new AdHocTestInfo(Connection.Modality.Asynchronous).run();
		} catch (Exception e) {
			Log.error(e.getMessage());
		}
	}
	
	final ConnectionSpec spec;
	final Connection.Modality connmode;
	JRedisFuture jredis_async;
	JRedis jredis;
	public AdHocTestInfo(final Connection.Modality connmode) throws Throwable{
		this.connmode = Assert.notNull(connmode, "connmode", IllegalArgumentException.class);
		this.spec = DefaultConnectionSpec.newSpec().setCredentials("jredis".getBytes());
		switch (connmode) {
		case Asynchronous:
			jredis_async = new JRedisPipeline(spec);
			jredis = null;
			break;
		case Synchronous:
			jredis_async = null;
			jredis = new JRedisClient(spec);
			break;
		default:
			throw new IllegalArgumentException("only sync and async modes are supported");
		}
	}
	final public void run() {
		try {
			switch (connmode) {
			case Asynchronous:
				runasync();
				break;
			case Synchronous:
				runsync();
				break;
			default:
				throw new RuntimeException("BUG: should not be reachable");
			}
		} catch (RuntimeException rte) {
			Log.error("oops: fault: %s - bye!", rte.getMessage());
		}
	}
	final public void runsync() {
		try {
			Map<String, String> infomap = jredis.info();
			for (String k : infomap.keySet()) {
				System.out.format("%s\n", k);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Log.error("synch run error", e);
			throw new RuntimeException(e);
		} finally {
			jredis.quit();
		}
		
	}
	public void runasync() {
		try {
			Map<String, String> infomap = jredis_async.info().get();
			for(String k : infomap.keySet()){
				System.out.format("%s => %s\n", k, infomap.get(k));
			}
			
			System.out.format("\n\n### InfoMap needs to be updated to include ###\n");
			for (RedisInfo info : RedisInfo.values()){
				if(infomap.get(info.name()) == null)
					Log.log("%s IS MISSING", info.name());
			}

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
			jredis_async.quit();
		}
	}
}
