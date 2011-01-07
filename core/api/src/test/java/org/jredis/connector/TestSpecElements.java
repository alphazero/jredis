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

package org.jredis.connector;

import org.jredis.TestBase;
import org.jredis.connector.Connection.Flag;
import org.testng.annotations.Test;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Sep 12, 2010
 * 
 */

public class TestSpecElements extends TestBase{
	
	/**
	 * [Doc this baby]
	 */
	@Test
	public void testConnectionFlags() {
		log.info("TEST:CONNECTOR spec sematics - ConnectionFlags");
		
		Flag flags[] = {Flag.CONNECT_IMMEDIATELY, Flag.SHARED, Flag.RELIABLE};
        int bitset = Flag.bitset(flags);
        for(Flag f : flags)
        	assertTrue(Flag.isSet(bitset, f), "%s should have been set!\n", f.name());
        int oldbitset = bitset;
        
        bitset = Flag.bitclear(bitset, flags[1]);
        assertFalse(bitset == oldbitset, "clearing flag should have changed bitset");
        assertFalse(Flag.isSet(bitset, flags[1]), "%s should have been cleared!\n", flags[1].name() );
        
        int bitset2 = 0x0000;
    	bitset2 = Flag.bitset(bitset2, flags);
        for(Flag f : flags)
        	assertTrue(Flag.isSet(bitset2, f), "%s should have been set!\n", f.name());
	}
	/**
	 * Test the equivalence of
	 * {@link ConnectionSpec#setCredentials(byte[])} and {@link ConnectionSpec#setCredentials(String)}
	 * using {@link ConnectionSpec#getCredentials()} 
	 */
	@Test
	public void testCredentialsOverloads () {
		String property = Connection.Property.CREDENTIAL.name();
		log.info(String.format("TEST:CONNECTOR spec sematics - Credentials %s", property));
		
		// check with actual passwords
		//
		String password = "jredis";
		byte[] credentials = password.getBytes();
		
		ConnectionSpec spec = new ConnectionSpec.RefImpl();
		assertNull(spec.getCredentials(), "RefImpl should not have defined: %s", property);
		
		spec.setCredentials(password.getBytes());
		byte[] credentials_1 = spec.getCredentials();
		assertNotNull(credentials_1, "RefImpl.setCredentials (byte[]) did not set: %s", property);
		
		// use the String variant
		//
		spec.setCredentials(password);
		byte[] credentials_2 = spec.getCredentials();
		assertNotNull(credentials_2, String.format("RefImpl.setCredentials(String) did not set: %s", property));

		// compare them
		assertEquals(credentials_2, credentials_1, String.format("Overloaded methods for %s setter are not equivalent.", property));
		
		// check for treatment of empty strings -- should be equivalent to null byte[]
		//
		spec = new ConnectionSpec.RefImpl();  // ASSUMPTION: tested above for null default value.
		password = "";
		spec.setCredentials(password);
		assertNull(spec.getCredentials(), "RefImpl should not have set \"\" (empty) credential to non-null value: %s", property);
	}
}
