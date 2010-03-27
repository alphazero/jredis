/*
 *   Copyright 2009-2010 Joubin Houshyar
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

package org.jredis.ri.cluster.support;

import org.jredis.ClientRuntimeException;
import org.jredis.cluster.RefImplTestSuiteBase;
import org.jredis.ri.alphazero.support.Log;
import org.jredis.ri.cluster.support.CryptoHashUtils;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 27, 2010
 * 
 */

@Test(suiteName="extensions-ri-cluster-tests2")
public class CryptoHashUtilTest extends RefImplTestSuiteBase<Object> {

	// ------------------------------------------------------------------------
	// Specification Interface tested
	// ------------------------------------------------------------------------
	
	/* (non-Javadoc) @see org.jredis.ri.ProviderTestBase#newProviderInstance() */
    @Override
    protected Object newProviderInstance () {
	    return new CryptoHashUtils();
    }
	protected final Class<?> getSpecificationClass () {
		return CryptoHashUtils.class;
	}
	
	// ------------------------------------------------------------------------
	// Tests
	// ------------------------------------------------------------------------
	
	/**
	 * Test computeMd5 using byte[]
	 */
	@SuppressWarnings("static-access")
    @Test
	public void testComputeMd5 () {
		Log.log("Testing Crptographic function computeMd5(byte[])");
		
		try {
			byte[] data1 = data.getRandomBytes(255);
	        byte[] data1_md5 = CryptoHashUtils.computeMd5(data1);
	        assertNotNull(data1_md5, "md5 digest should not be null");
	        assertNotSame(data1_md5, data1, "md5 digest result shoud not be the same as the input array");
	        assertTrue(data1_md5.length > 0, "md5 digest length should be non-zero");
	        
	        // edge case - NULL input not allowed
	        boolean didRaiseError = false;
	        try {
				byte[] nullref = null;
	        	CryptoHashUtils.computeMd5(nullref);
	        }
	        catch (IllegalArgumentException e){ didRaiseError = true; }
	        catch (RuntimeException what) { fail("Unexpected runtime exception raised",what); }
        	assertTrue(didRaiseError, "Expecting a raised exception for null input");
	        
	        // edge case - zero length input not allowed
	        didRaiseError = false;
	        try {
				byte[] zerobytes = data.getRandomBytes(0);
	        	CryptoHashUtils.computeMd5(zerobytes);
	        }
	        catch (IllegalArgumentException e){ didRaiseError = true; }
	        catch (RuntimeException what) { fail("Unexpected runtime exception raised",what); }
        	assertTrue(didRaiseError, "Expecting a raised exception for zero length input");
        }
        catch (ClientRuntimeException e) {
	        fail("Required cryptographic algorithm (MD5) is not available", e);
        }
        catch (RuntimeException whatsthis){
        	fail("Unexpected exception class thrown", whatsthis);
        }
	}
}
