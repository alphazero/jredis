/*
 *   Copyright 2009 Joubin Houshyar
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

package org.jredis.examples;

import org.jredis.ClientRuntimeException;
import org.jredis.Command;
import org.jredis.JRedis;
import org.jredis.ProviderException;
import org.jredis.RedisException;
import org.jredis.ri.alphazero.JRedisClient;
import static org.jredis.ri.alphazero.support.DefaultCodec.*;

/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 15, 2009
 * @since   alpha.0
 * 
 */

public class HelloAgain {
	public static final String key = "jredis::examples::HelloAgain::message";
	public static void main(String[] args) {
		String password = "";
		if(args.length > 0) password  = args[0];
		new HelloAgain().run(password);
	}

	private void run(String password) {
		try {
			// use explicit constructor params for example
			// but localhost:6379 is the default setting
			//
			JRedis	jredis = new JRedisClient("localhost", 6379);
			jredis.ping();
			if(!password.equals("")) 
				jredis.auth(password);
			
			jredis.ping().select(13);
			
			if(!jredis.exists(key)) {
				jredis.set(key, "Hello Again!");
				System.out.format("Hello!  You should run me again!\n");
				return;
			}
			
			String msg = toStr ( jredis.get(key) );
			
			System.out.format("%s\n", msg);
		}
		catch (RedisException e){
			if (e.getCommand()==Command.PING){
				System.out.format("I'll need that password!  Try again with password as command line arg for this program.\n");
			}
		}
		catch (ProviderException e){
			System.out.format("Oh no, an 'un-documented feature':  %s\nKindly report it.", e.getMessage());
		}
		catch (ClientRuntimeException e){
			System.out.format("%s\n", e.getMessage());
		}
	}
}
