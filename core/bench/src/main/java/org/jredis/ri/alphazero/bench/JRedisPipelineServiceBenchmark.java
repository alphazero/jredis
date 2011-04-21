///*
// *   Copyright 2009 Joubin Houshyar
// * 
// *   Licensed under the Apache License, Version 2.0 (the "License");
// *   you may not use this file except in compliance with the License.
// *   You may obtain a copy of the License at
// *    
// *   http://www.apache.org/licenses/LICENSE-2.0
// *    
// *   Unless required by applicable law or agreed to in writing, software
// *   distributed under the License is distributed on an "AS IS" BASIS,
// *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *   See the License for the specific language governing permissions and
// *   limitations under the License.
// */
//
//package org.jredis.ri.alphazero.bench;
//
//import org.jredis.ClientRuntimeException;
//import org.jredis.JRedis;
//import org.jredis.bench.JRedisBenchmark;
//import org.jredis.connector.ConnectionSpec;
//import org.jredis.ri.alphazero.JRedisPipelineService;
//import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
//
///**
// * [TODO: document me!]
// *
// * @author  Joubin Houshyar (alphazero@sensesay.net)
// * @version alpha.0, Nov 6, 2009
// * @since   alpha.0
// * 
// */
//
//public class JRedisPipelineServiceBenchmark extends JRedisBenchmark {
//	public static void main(String[] args) {
////		host = "192.168.1.222";
//		String host = "127.0.0.1";
//		String password = "jredis";
//		int    port = 6379;
//		int	   size = 3;
//		int workerCnt = 100;
//		int reqCnt = 1000;
//		int	db = 13;
//		if(args.length > 0) db = Integer.valueOf (args[0]);
//		if(args.length > 1) workerCnt = Integer.valueOf(args[1]);
//		if(args.length > 2) reqCnt = Integer.valueOf(args[2]);
//		if(args.length > 3) size = Integer.parseInt(args[3]);
//		if(args.length > 4) host = args[4];
//		
//		System.out.format("==> Usage: [db [conn [req [size [host]]]]\n");
//		
//		new JRedisPipelineServiceBenchmark(host, port, db, password).runBenchmarks (host, port, workerCnt, reqCnt, size, db);
//	}
//	final JRedis jredisService;
//    public JRedisPipelineServiceBenchmark (String host, int port, int db, String password) {
//		ConnectionSpec connectionSpec = DefaultConnectionSpec.newSpec("localhost", 6379, db, "jredis".getBytes());
//		jredisService = new JRedisPipelineService(connectionSpec);
//		super.quitOnRunEnd(false);
//    }
//	@Override
//	protected final JRedis newConnection (String host, int port, int db, String password) throws ClientRuntimeException {
//		return jredisService;
//	}
//	@Override
//	protected final Class<? extends JRedis> getImplementationClass() {
//		return JRedisPipelineService.class;
//	}
//}
