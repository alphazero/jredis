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

package org.jredis.ri;

import org.jredis.ClientRuntimeException;
//import org.jredis.JRedis;
import org.jredis.ri.alphazero.support.Log;
import org.testng.annotations.BeforeTest;

/**
 * Support for tests of interface <code>T</code> implementation providers.
 * 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Oct 10, 2009
 * @since   alpha.0
 * 
 */

public abstract class ProviderTestBase <T> {
	
	// ========================================================================
	// Test Properties
	// ========================================================================
	
	/** the JRedis implementation being tested */
	protected T provider = null;
	
	// ------------------------------------------------------------------------
	// JRedisFuture Provider initialize methods
	// ------------------------------------------------------------------------
	/**
	 * Sets the {@link JRedis} implementation provider for the test suite
	 */
	@BeforeTest
	public void setProvider () {
		try {
			T provider = newProviderInstance();

			setProviderInstance (provider);
			Log.log("%s.setProvider - done", this.getClass().getSimpleName());
        }
        catch (ClientRuntimeException e) {
        	Log.error(e.getLocalizedMessage());
        }
	}
	
	/**
	 * Extension point:  Tests for specific implementations of <code>T</code> 
	 * implement this method to create the provider instance.
	 * @return <code>T</code> implementation instance
	 */
	protected abstract T newProviderInstance () ;
	
	/**
	 * Must be called by a BeforeTest method to set the jredis parameter.
	 * @param provider that is being tested.
	 */
	private final void setProviderInstance (T provider) {
		this.provider = provider;
		Log.log("\n\nTEST: " +
				"\n\t-----------------------------------------------\n" +
				"\tProvider Class: %s" +
				"\n\t-----------------------------------------------\n", 
				provider.getClass().getCanonicalName());
	}
	/**
	 * @return the <code>T</code> instance used for the provider tests
	 */
	protected final T getProviderInstance() {
		return provider;
	}
}
