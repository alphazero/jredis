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


package org.jredis.ri.cluster.model;

import org.jredis.ClientRuntimeException;
import org.jredis.cluster.support.HashAlgorithm;
import org.jredis.ri.cluster.support.CryptoHashUtils;

/**
 * The Ketama consistent hash algorithm as implemented by Dustin Sallings,
 * with some minor (non-algorithmic) modifications.
 * <p>
 * <b>Note</b> that if certain expected cryptographic algorithms expected to be 
 * present in your JRE are not available, {@link ClientRuntimeException}s 
 * will be thrown.
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 25, 2010
 * 
 */

public class KetamaHashProvider implements HashAlgorithm {

	// ------------------------------------------------------------------------
	// Interface
	// ------------------------------------------------------------------------

	/**
	 * Uses MD5 digest.  
	 * <p>
	 * Contains code from net.spy.memecached.
	 * @ Copyright (c) 2006-2009  Dustin Sallings <dustin@spy.net>
	 * 
	 * @param b bytes to be hashed
	 */
//	@Override
	public long hash (byte[] b) {
		if(null == b || b.length ==0) throw new IllegalArgumentException();
		
		/* Copyright (c) 2006-2009  Dustin Sallings <dustin@spy.net> */
		/* -- BEGIN code segment */
		byte[] kb;
		long rv = 0;
        kb = CryptoHashUtils.computeMd5(b);
		rv = ((long) (kb[3] & 0xFF) << 24)
		| ((long) (kb[2] & 0xFF) << 16)
		| ((long) (kb[1] & 0xFF) << 8)
		| (kb[0] & 0xFF);
		/* -- END code segment */
		
		return rv;
	}
	// ------------------------------------------------------------------------
	// Ketama specific and for Ketama package only
	// ------------------------------------------------------------------------
	
	/**
	 * Rip of the inner loop of the KetamaNodeLocator's constructor, in full.
	 * <p>
	 * @ Copyright (c) 2006-2009  Dustin Sallings <dustin@spy.net>
	 * 
	 * @param digest
	 * @param h
	 * @return
	 */
	long hash (byte[] digest, int h) {
		return 
			((long)(digest[3+h*4]&0xFF) << 24)
			| ((long)(digest[2+h*4]&0xFF) << 16)
			| ((long)(digest[1+h*4]&0xFF) << 8)
			| (digest[h*4]&0xFF);
	}
}