/*
 *   Copyright 2010 Joubin Houshyar
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

package org.jredis.examples.commands;

import java.util.Random;
import org.jredis.ClientRuntimeException;
import org.jredis.JRedis;
import org.jredis.JRedisFuture;
import org.jredis.ProviderException;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.JRedisClient;
import org.jredis.ri.alphazero.JRedisPipeline;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import org.jredis.ri.alphazero.support.Log;

/**
 * Abstract base for command usage example classes.  Basic
 * template pattern with extension points for extending classes.
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Aug 21, 2010
 * 
 */

abstract public class UsingCommandBase implements Runnable{
	static final Random rand = new Random(System.currentTimeMillis());
	enum Semantics { Sync (JRedis.class), Async(JRedisFuture.class); Class<?> iface; Semantics(Class<?> iface){this.iface = iface;} };
	protected ConnectionSpec spec = null;
	protected UsingCommandBase() {
		initialize();
//		run();
	}
	private final void usingSemantics(Semantics s) {
		try {
			Log.log("\n --- Using %s semantics (%s interface)", s.name(), s.iface.getSimpleName());
			if(s == Semantics.Sync){
				JRedis jredis = newSyncClient();
				usingSyncSemantics(jredis);
				jredis.quit();
			}
			else {
				JRedisFuture jredis = newAsyncClient();
				usingAsyncSemantics(jredis);
				jredis.quit();
			}
		}
		catch (Throwable t) {
			Log.error(String.format("When using %s semantics.  %s: %s", s.name(), t.getClass().getSimpleName(), t.getMessage()));
		}
	}
	public final void run() {
		Log.log("Running %s", this.getClass().getSimpleName());
		
		usingSemantics(Semantics.Sync);
		usingSemantics(Semantics.Async);
	}
	/** creates a {@link ConnectionSpec} and sets the AUTH password and the db for SELECT */
	protected final void initialize() {
		spec = DefaultConnectionSpec.newSpec()
			.setCredentials("jredis".getBytes())
			.setDatabase(10);
		setConnectionSpec();
	}
	/** called to update spec beyond baseline settings. See {@link UsingCommandBase#initialize()} for default settings. */
	protected void setConnectionSpec() { /* no op */ }
	protected  JRedis newSyncClient() {
		return new JRedisClient(spec);
	}
	protected JRedisFuture newAsyncClient() {
		return new JRedisPipeline(spec);
	}
	abstract public void usingSyncSemantics(JRedis jredis) throws ClientRuntimeException, ProviderException;
	abstract public void usingAsyncSemantics(JRedisFuture jredis) throws ClientRuntimeException, ProviderException;
}
