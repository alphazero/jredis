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

package org.jredis.ri.alphazero;

import static org.testng.Assert.fail;
import org.jredis.ClientRuntimeException;
import org.jredis.JRedisFuture;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import org.jredis.ri.alphazero.support.Log;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Nov 6, 2009
 * @since   alpha.0
 * 
 */
@Test(sequential = true, suiteName="JRedisAsyncClient-tests")
public class JRedisAsyncClientTest extends JRedisFutureProviderTestsBase {

	// ------------------------------------------------------------------------
	// TEST SETUP 
	// ------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see org.jredis.ri.ProviderTestBase#newProviderInstance()
	 */
	@Override
	protected JRedisFuture newProviderInstance () {
		JRedisFuture provider = null;
		try {
			ConnectionSpec connectionSpec = DefaultConnectionSpec.newSpec(this.host, this.port, this.db2, this.password.getBytes());
			provider = new JRedisAsyncClient(connectionSpec);
        }
        catch (ClientRuntimeException e) {
        	Log.error(e.getLocalizedMessage());
        }
        return provider;
	}
	// ------------------------------------------------------------------------
	// The Tests
	// ========================================================= JRedisClient
	/**
	 * We define and run any additional, provider specific tests here.  The
	 * basic generally applicable JRedis interface method test are defined 
	 * in the super class.
	 * 
	 * Here we test Quit in a post test method to insure all tests have been
	 * completed.
	 */
	// ------------------------------------------------------------------------

	/**
	 * Pipeline quit.  
	 * We first ping and await the response to insure pipeline has processed
	 * all pending responses, and then issue the quit command.
	 */
	@AfterTest
	public void testQuit() {
		try {
			JRedisFuture pipeline = getProviderInstance();
			pipeline.ping().get();
			pipeline.quit().get();
		} 
		catch (Exception e) {
			fail("QUIT" + e);
		}
	}
}
