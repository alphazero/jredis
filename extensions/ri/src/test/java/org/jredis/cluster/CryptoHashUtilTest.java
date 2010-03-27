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

package org.jredis.cluster;

import java.security.NoSuchAlgorithmException;
import org.jredis.ri.alphazero.support.Log;
import org.jredis.ri.cluster.CryptoHashUtils;
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

	@SuppressWarnings("static-access")
    @Test
	public void testComputeMd5 () {
		Log.log("Testing Crptographic function computeMd5(byte[])");
		byte[] nullref = null;
		byte[] zerobytes = data.getRandomBytes(0);
		byte[] data1 = data.getRandomBytes(255);
		
		try {
//	        byte[] nullref_md5 = CryptoHashUtils.computeMd5(nullref);
	        byte[] zerobytes_md5 = CryptoHashUtils.computeMd5(zerobytes);
	        byte[] data1_md5 = CryptoHashUtils.computeMd5(data1);
	        assertNotNull(data1_md5, "md5 digest should not be null");
	        assertNotSame(data1_md5, data1, "md5 digest result shoud not be the same as the input array");
	        assertTrue(data1_md5.length > 0, "md5 digest length should be non-zero");
        }
        catch (NoSuchAlgorithmException e) {
	        fail("Required cryptographic algorithm (MD5) is not available", e);
        }
	}

	/* (non-Javadoc) @see org.jredis.ri.ProviderTestBase#newProviderInstance() */
    @Override
    protected Object newProviderInstance () {
	    return new CryptoHashUtils();
    }
	protected final Class<?> getSpecificationClass () {
		return CryptoHashUtils.class;
	}
}
