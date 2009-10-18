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

package org.jredis.ri.alphazero.support;

import java.nio.charset.Charset;
import java.util.Random;
import org.testng.annotations.*;
import org.testng.Assert;
import static org.jredis.ri.alphazero.support.GZip.*;
/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Aug 24, 2009
 * @since   alpha.0
 * 
 */

@Test(suiteName="support-tests")
public class GZipTest {
	public final static Charset SUPPORTED_CHARSET = Charset.forName ("UTF-8");

	@Test
	public void testCompression() {
		Log.log("Testing compress/decompress of 1000 random 24KB strings ...");
    	int cnt = 1000;
    	int size = 1024 * 24;
    	for(int i=0; i<cnt; i++){
    		String randomString = getRandomString(size);
    		byte[] stringBytes = randomString.getBytes();
    		byte[] compressed = compress(stringBytes);
    		byte[] decompressed = decompress(compressed);
    		Assert.assertEquals(decompressed, stringBytes);
    	}
	}
	/*
	 * OK, so this is a reduandant hack -- the Bench pom states that it is dependent
	 * on RI and API, but here we are testing a RI class so Bench isn't installed
	 * yet so we are duplicating the methods and violating DRY. (Sue me).
	 */
	// ------------------------------------------------------------------------
	// Static properties
	// ------------------------------------------------------------------------
	/** 
	 * Random generator used for Util random functions, 
	 * instantiated using {@link System#currentTimeMillis()} as seed
	 */
	final static Random 		random = new Random(System.currentTimeMillis());
	
	// ------------------------------------------------------------------------
	// Static methods
	// ------------------------------------------------------------------------
	static 
	public String getRandomString (int size) {
		StringBuilder builder = new  StringBuilder(size);
		for(int i = 0; i<size; i++){
			char c = (char) (random.nextInt(126-33) + 33);
			builder.append(c);
		}
		return builder.toString();
	}
}
