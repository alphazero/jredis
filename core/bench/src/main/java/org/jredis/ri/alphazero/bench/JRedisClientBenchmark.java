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

package org.jredis.ri.alphazero.bench;

import org.jredis.ClientRuntimeException;
import org.jredis.JRedis;
import org.jredis.bench.JRedisBenchmark;
import org.jredis.ri.alphazero.JRedisClient;


/**
 * Not fully baked ... just a hack to get things going.
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, 04/10/09
 * @since   alpha.0
 * 
 */
public class JRedisClientBenchmark extends JRedisBenchmark {

	/**
	 * Runs the benchmark tests.
	 * [TODO: lets do proper usage here and clean this up.]
	 * 
	 * Currently, uses 50 concurrent connections and 5000 requests / each connection.
	 * TODO: munch on some commandline args ...
	 * @param args
	 */
	public static void main(String[] args) {
		String host = "127.0.0.1";
		int    port = 6379;
		int	   size = 3;
		int workerCnt = 10;
		int reqCnt = 20000;
		int	db = 13;
		if(args.length > 0) db = Integer.valueOf (args[0]);
		if(args.length > 1) workerCnt = Integer.valueOf(args[1]);
		if(args.length > 2) reqCnt = Integer.valueOf(args[2]);
		if(args.length > 3) size = Integer.parseInt(args[3]);
		if(args.length > 4) host = args[4];
		
		System.out.format("==> Usage: [db [conn [req [size [host]]]]\n");
		
		new JRedisClientBenchmark().runBenchmarks (host, port, workerCnt, reqCnt, size, db);
	}
	
	@Override
	protected final JRedis newConnection(String host, int port, int db, String password) throws ClientRuntimeException {
		return new JRedisClient(host, port, password, db);
	}
	@Override
	protected final Class<? extends JRedis> getImplementationClass() {
		return JRedisClient.class;
	}
}
