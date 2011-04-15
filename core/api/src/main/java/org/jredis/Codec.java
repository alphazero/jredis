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

package org.jredis;



/**
 * Defines the necessary methods for a Java type encoder-decoder.  Implementations of this
 * interface can be registered with a {@link CodecManager} used by a {@link JRedis} implementation
 * (whether per instance or by other relations per JRedis provider implementation), and used
 * during the encoding of an semantic java type into {@link byte[]} and back.  
 *
 * @author  Joubin (alphazero@sensesay.net)
 * @version alpha.0, Apr 14, 2009
 * @since   alpha.0
 * 
 */

public interface Codec <T extends Object>  {
	/**
	 * @param bytes
	 * @return an instance of type <code>T</code> corresponding to the value of decoded <code>bytes</code>
	 */
	public T decode (byte[] bytes);
	/**
	 * @param object
	 * @return
	 */
	public byte[] encode (T object);
	/**
	 * @param type
	 * @return whether this codec supports the (en/de)coding of the type <code>T</code>
	 */
	public boolean supports (Class<?> type);
}
