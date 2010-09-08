/* -- BEGIN NOTICE --
 * 
 * This class uses in parts extant and/or modified code from net.spy.memcached.HashAlgorithm
 * by Dustin Sallings.  See this module's 3rd party license folder for license details.
 * 
 * -- END NOTICE -- 
 * 
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.jredis.ClientRuntimeException;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 26, 2010
 * 
 */

public class CryptoHashUtils {
	/**
	 * Get the md5 of the given key. 
	 * @throws ClientRuntimeException if MD5 algorithm is not supported.
	 * @throws IllegalArgumentException if input is null or zero length
	 * 
	 * @Copyright (c) 2006-2009  Dustin Sallings <dustin@spy.net> 
	 */
	public static byte[] computeMd5(byte[] b) throws ClientRuntimeException{
		if(null == b) throw new IllegalArgumentException ("null input");
		if(b.length == 0) throw new IllegalArgumentException ("zero length input");
		MessageDigest md5 = null;
		try {
	        md5 = MessageDigest.getInstance("MD5");
			md5.reset();
			md5.update(b);
        }
        catch (NoSuchAlgorithmException e) {
        	throw new ClientRuntimeException("MD5 Message Digest algorithm is not present in this JRE", e);
        }
		return md5.digest();
	}
	
	/**
	 * @param s
	 * @return
	 * @throws ClientRuntimeException
	 * @throws IllegalArgumentException if input is null or zero length
	 */
	public static byte[] computeMd5(String s) throws ClientRuntimeException{
		if(null == s) throw new IllegalArgumentException ("null input");
		return computeMd5(s.getBytes());
	}
}
