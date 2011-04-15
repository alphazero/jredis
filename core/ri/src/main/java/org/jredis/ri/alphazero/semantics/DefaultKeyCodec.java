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
import java.lang.reflect.UndeclaredThrowableException;
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

public class DefaultKeyCodec<K extends Object> implements KeyCodec<K> {
	private static final KeyCodec<Object> instance;
	static {
		instance = new DefaultKeyCodec<Object>();
	}
	/**  */
	static final private Map<String, byte[]>	keyByteCache = new ConcurrentHashMap<String, byte[]>();
	
	public static final KeyCodec<Object> provider() { return instance; }
	
	/** TODO: jvm arg me ... defaults to FALSE now */
	public static final boolean	CacheKeys	= false;

	/* (non-Javadoc)
	 * @see org.jredis.Codec#decode(byte[])
	 */
	public K decode (byte[] bytes) { throw new NotSupportedException("key decode not supported."); }

	/* (non-Javadoc)
	 * @see org.jredis.Codec#encode(java.lang.Object)
	 */
	public byte[] encode (K key) {
		if(null == key) throw new IllegalArgumentException("key is null");
		if(key instanceof String) {
			return encodeString((String) key);
		}
		else if (key instanceof byte[]){
			byte[] bkey = (byte[]) key ;
			if(bkey.length == 0) throw new IllegalArgumentException("key is zerolewn");
			return bkey;
		}
		else {
			String msg = String.format("only String and byte[] keys are supported", key.getClass().getCanonicalName());
			throw new IllegalArgumentException(msg);
		}
	}
	public static byte[] encodeString(String key) throws IllegalArgumentException {
		if(null == key) throw new IllegalArgumentException("key is null");
		byte[] bytes = null;
		if(JRedisSupport.CacheKeys == true)
			bytes = keyByteCache.get(key);
		if(null == bytes) {
//			bytes = key.getBytes(DefaultCodec.SUPPORTED_CHARSET); // java 1.6
			try {
	            bytes = key.getBytes(DefaultCodec.SUPPORTED_CHARSET_NAME);
            }
            catch (UnsupportedEncodingException e) {
	            throw new UndeclaredThrowableException(e);
            }
			for(byte b : bytes) {
				if (b == (byte)32 || b == (byte)10 || b == (byte)13)
					throw new IllegalArgumentException ("Key includes invalid byte value: " + (int)b);
			}
			
			if(JRedisSupport.CacheKeys == true)
				keyByteCache.put(key, bytes);
		}
		return bytes;
	}

	/* (non-Javadoc)
	 * @see org.jredis.Codec#supports(java.lang.Class)
	 */
	static byte[] ba = new byte[0];
	static Class<?> BAClass = ba.getClass();
	public boolean supports (Class<?> type) { 
		return type == String.class || type.equals(BAClass); 
	}
}
