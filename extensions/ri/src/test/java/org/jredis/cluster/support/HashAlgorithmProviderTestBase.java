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

package org.jredis.cluster.support;



import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import java.util.HashSet;
import java.util.Set;
import org.jredis.ClientRuntimeException;
import org.jredis.cluster.RefImplTestSuiteBase;
import org.jredis.cluster.support.HashAlgorithm;
import org.jredis.ri.alphazero.support.Log;
import org.testng.annotations.Test;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 26, 2010
 * 
 */

@Test(suiteName="extensions-cluster-specs-algorithms-1")
abstract
public class HashAlgorithmProviderTestBase extends RefImplTestSuiteBase<HashAlgorithm>{

	// ------------------------------------------------------------------------
	// Specification Interface tested
	// ------------------------------------------------------------------------
	
	protected final Class<?> getSpecificationClass () {
		return HashAlgorithm.class;
	}
	
	// ------------------------------------------------------------------------
    // Tests
	// ------------------------------------------------------------------------
	@SuppressWarnings("static-access")
	@Test
	public void testHashByteArray() {
		Log.log("Testing HashAlgorithm hash(byte[])");
		HashAlgorithm hashAlgo = newProviderInstance();
		try {
	        int c = 2000;
	        int cnt = (int) (10 * Math.log(c)) * c;
        	Log.log("Test hashing %d keys", cnt);
	        Set<Long> hashSet = new HashSet<Long>(cnt);
	        for(int i=0; i<cnt; i++){
	        	long hash = hashAlgo.hash(data.getRandomBytes(255));
	        	boolean didAdd = hashSet.add(hash);
	        	if(!didAdd){
	        		Log.log("[NOTE] got a collision (hash: %d) at idx: %d!", hash, i);
	        	}
	        }
//	        assertEquals(hashSet.size(), cnt);
	        if(hashSet.size() == cnt)
	        	Log.log("Hashed %d keys with no collisions", cnt);
	        else
	        	Log.log("Hashed %d keys with %d collisions", cnt, cnt - hashSet.size());
	        
	        // edge case - NULL input not allowed
	        boolean didRaiseError = false;
	        try {
				byte[] nullref = null;
	        	hashAlgo.hash(nullref);
	        }
	        catch (IllegalArgumentException e){ didRaiseError = true; }
	        catch (RuntimeException what) { fail("Unexpected runtime exception raised",what); }
        	assertTrue(didRaiseError, "Expecting a raised exception for null input");
	        
	        // edge case - zero length input not allowed
	        didRaiseError = false;
	        try {
				byte[] zerobytes = data.getRandomBytes(0);
				hashAlgo.hash(zerobytes);
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
