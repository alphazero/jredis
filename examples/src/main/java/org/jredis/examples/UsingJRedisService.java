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
//package org.jredis.examples;
//
//import org.jredis.RedisException;
//import org.jredis.connector.ConnectionSpec;
//import org.jredis.ri.alphazero.JRedisService;
//import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
//
///**
// * [TODO: document me!]
// *
// * @author  Joubin Houshyar (alphazero@sensesay.net)
// * @version alpha.0, Sep 1, 2009
// * @since   alpha.0
// * 
// */
//
//public class UsingJRedisService {
//	public static JRedisService service = null;
//	/**
//	 * Demonstrated using the {@link JRedisService} class.  Its also a bench
//	 * that shows the performance of the service with lots of threads banging on it.
//	 * Don't forget to flush db#11 after running this as it adds a whole bunch of keys.
//	 * @param args
//	 */
//	public static void main (String[] args) {
//		int database = 11;
//		ConnectionSpec connectionSpec = DefaultConnectionSpec.newSpec("localhost", 6379, database, "jredis".getBytes());
//		int connCnt = 7;
//		int userCnt = 10;
//		int opsCnt = 100000;
//		
//		// create the service -- well this is it as far as usage goes:  set the number of connections for the service pool
//		// You can use this anywhere you would use JRedis instances and it is thread safe.
//		// 
//		service = new JRedisService(connectionSpec, connCnt);
//		
//		// create a bunch of dummy users for the service
//		Thread[] users = new Thread[userCnt];
//		for(int i=0; i<userCnt; i++){
//			users[i] = getDummyUser(i, opsCnt);
//		}
//		
//		// alright, lets run these dummy users
//		//
//		for(int i=0; i<userCnt; i++){
//			users[i].start();
//		}
//	}
//	
//    /**
//     * You can change the actual operation to test other Redis commands, if you feel like it.
//     * @param id
//     * @param opsCnt
//     * @return
//     */
//    private static Thread getDummyUser (final int id, final int opsCnt) {
//    	Thread user = new Thread(new Runnable() {
////			@Override
//            public void run () {
//				try {
//					String key = null;
//					byte[] value = null;
//					for(int i=0; i<opsCnt; i++){
//						key = "foo" + i+ "_" + id;
//						value = ("woof_" + i + "_" + id).getBytes();
//						service.set(key, value);
//						service.get(key);
//					}
//                }
//                catch (RedisException e) {
//	                e.printStackTrace();
//                }
//            }
//    	}, "user_" + id);
//    	return user;
//    }
//}
