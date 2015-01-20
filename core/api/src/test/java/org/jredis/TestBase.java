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

package org.jredis;

import static org.testng.Assert.fail;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Sep 10, 2010
 * 
 */

public class TestBase {

	// ------------------------------------------------------------------------
	// Shared resources
	// ------------------------------------------------------------------------
	/**  */
	final protected Log log = LogFactory.getLog(TestBase.class);
	
	// ------------------------------------------------------------------------
	// Test support 
	// ------------------------------------------------------------------------
	/**
	 * @param <T>
	 * @param test
	 * @param errtype
	 */
	protected final <T extends RuntimeException> void assertDidRaiseRuntimeError (Runnable test, Class<T> errtype){
		boolean didRaiseError = false;
		try { test.run(); }
		catch (RuntimeException t){
			if(errtype.isAssignableFrom(t.getClass()))
				didRaiseError = true; 
		}
		catch (Exception e){ fail("Unexpected exception", e); }
		finally {
			if(!didRaiseError) { fail("Failed to raise expected RuntimeError " + errtype.getCanonicalName()); }
		}
	}
	
	// ------------------------------------------------------------------------
	// Test support - mildly enhanced TESTNG Assert semantics
	// ------------------------------------------------------------------------
	// notNull
	public static void assertNotNull(Object object, String msgfmt, Object...optionalFmtArgs){
		String message = String.format(msgfmt, optionalFmtArgs);
		Assert.assertNotNull (object, message);
	}
	// null
	public static void assertNull(Object object, String msgfmt, Object...optionalFmtArgs){
		String message = String.format(msgfmt, optionalFmtArgs);
		Assert.assertNull (object, message);		// << has bug.  reports a boolean comp result -- TODO: fix and patch.
	}
	
	// equals
	public static void assertEquals(Object actual, Object expected, String msgfmt, Object...optionalFmtArgs){
		String message = String.format(msgfmt, optionalFmtArgs);
		Assert.assertEquals (actual, expected, message);		
	}
	public static void assertEquals(byte[] actual, byte[] expected, String msgfmt, Object...optionalFmtArgs){
		String message = String.format(msgfmt, optionalFmtArgs);
		Assert.assertEquals (actual, expected, message);		
	}
	
	// true/false
	public static void assertTrue(boolean condition, String msgfmt, Object...optionalFmtArgs){
		String message = String.format(msgfmt, optionalFmtArgs);
		Assert.assertTrue (condition, message);
	}
	public static void assertFalse(boolean condition, String msgfmt, Object...optionalFmtArgs){
		String message = String.format(msgfmt, optionalFmtArgs);
		Assert.assertFalse (condition, message);
	}
}
