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

package org.jredis.ri.alphazero.semantics;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jredis.NotSupportedException;
import org.jredis.ri.alphazero.JRedisSupport;
import org.jredis.ri.alphazero.support.DefaultCodec;
import org.jredis.semantics.KeyCodec;

/**
 * Default {@link KeyCodec} provider for JRedis RI.
 * 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Nov 25, 2009
 * @since   alpha.0
 * 
 */

public class DefaultKeyCodec implements KeyCodec {
	private static final KeyCodec instance;
	static {
		instance = new DefaultKeyCodec();
	}
	/**  */
	static final private Map<String, byte[]>	keyByteCache = new ConcurrentHashMap<String, byte[]>();
	
	/**
	 * @return
	 */
	public static final KeyCodec provider() { return instance; }
	
	/** TODO: jvm arg me ... defaults to FALSE now */
	public static final boolean	CacheKeys	= false;

	/* (non-Javadoc)
	 * @see org.jredis.Codec#decode(byte[])
	 */
	public String decode (byte[] bytes) { throw new NotSupportedException("key decode not supported."); }

	/* (non-Javadoc)
	 * @see org.jredis.Codec#encode(java.lang.Object)
	 */
	public byte[] encode (String key) {
		if(null == key) throw new IllegalArgumentException("key is null");
		byte[] bytes = null;
		
		if(JRedisSupport.CacheKeys == true)
			bytes = keyByteCache.get(key);
		
		if(null == bytes) {
			try {
	            bytes = key.getBytes(DefaultCodec.SUPPORTED_CHARSET_NAME);
            }
            catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
            }
			for(byte b : bytes) {
				if (b == (byte)32 || b == (byte)10 || b == (byte)13)
					throw new IllegalArgumentException ("Key includes invalid byte value: " + (int)b);
			}
			if(CacheKeys == true) keyByteCache.put(key, bytes);
		}
		return bytes;
	}

	/* (non-Javadoc)
	 * @see org.jredis.Codec#supports(java.lang.Class)
	 */
	public boolean supports (Class<?> type) { return type == String.class ? true : false; }
}
