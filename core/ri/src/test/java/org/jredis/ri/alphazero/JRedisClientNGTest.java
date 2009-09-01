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
import org.jredis.JRedis;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.support.Log;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

/**
 * For testing the JRedis test suite for {@link JRedisClient} implementation.
 * TODO: should also do a minimal test using {@link ConnectionSpec}.
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 17, 2009
 * @since   alpha.0
 * 
 */

public class JRedisClientNGTest extends JRedisProviderTestNGBase {

	// ------------------------------------------------------------------------
	// TEST SETUP 
	// ------------------------------------------------------------------------
	/**
	 * We're testing {@link JRedisClient} in this test class.  We'll use the 
	 * {@link JRedisProviderTestNGBase#setJRedisProviderInstance(org.jredis.JRedis)}
	 * here since we don't (yet) have the resource manager interface ironed out.  
	 */
	@BeforeTest
	public void setJRedisProvider () {
		try {
			JRedis jredis = new JRedisClient (this.host, this.port, this.password, this.db1);

			setJRedisProviderInstance(jredis);
			prepTestDBs();
			
			Log.log("JRedisClientNGTest.setJRedisProvider - done");
        }
        catch (ClientRuntimeException e) {
        	Log.error(e.getLocalizedMessage());
        }
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
	 * Test method for {@link org.jredis.ri.alphazero.JRedisSupport#auth(java.lang.String)}.
	 */
	@AfterTest
	public void testQuit() {
		Log.log("TEST: QUIT command");
		try {
			jredis.quit ();
		} 
		catch (Exception e) {
			fail("QUIT" + e);
		}
	}
}
