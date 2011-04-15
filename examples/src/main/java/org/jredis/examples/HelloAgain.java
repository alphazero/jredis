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
import org.jredis.JRedis;
import org.jredis.ProviderException;
import org.jredis.RedisException;
import org.jredis.connector.ConnectionSpec;
import org.jredis.protocol.Command;
import org.jredis.ri.alphazero.JRedisClient;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
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
	public static final byte[] bkey = key.getBytes();
	public static void main(String[] args) {
		String password = "jredis";
		if(args.length > 0) password  = args[0];
		new HelloAgain().run(password);
	}

	private void run(String password) {
		try {
			ConnectionSpec spec = DefaultConnectionSpec.newSpec().setCredentials(password);
			JRedis	jredis = new JRedisClient(spec);
			jredis.ping();
			
			if(!jredis.exists(bkey)) {
				jredis.set(bkey, "Hello Again!");
				System.out.format("Hello!  You should run me again!\n");
			}
			else {
				String msg = toStr ( jredis.get(bkey) );
				System.out.format("%s\n", msg);
			}
			jredis.quit();
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
