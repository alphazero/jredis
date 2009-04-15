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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jredis.JRedis;
import org.jredis.NotSupportedException;
import org.jredis.ri.alphazero.support.Convert;
import org.jredis.semantics.Codec;
import org.jredis.semantics.SemanticJRedis;
import org.jredis.semantics.Semantics;

/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 13, 2009
 * @since   alpha.0
 * 
 */

public class SemanticManager implements Semantics {

	/** String.class codec */
	static final Codec<String>  java_lang_String_codec = new Codec<String>() {
		@Override
		public String decode(byte[] bytes) { return new String(bytes); }

		@Override
		public byte[] encode(String object) { return object.getBytes(); }
	};

	/** String.class codec */
	static final Codec<Integer>  java_lang_Integer_codec = new Codec<Integer>() {
		@Override
		public Integer decode(byte[] bytes) { return Convert.getInt(bytes);}

		@Override
		public byte[] encode(Integer object) { return Convert.toBytes(object); }
	};

	/** String.class codec */
	static final Codec<Long>  java_lang_Long_codec = new Codec<Long>() {
		@Override
		public Long decode(byte[] bytes) { return Convert.getLong(bytes); }

		@Override
		public byte[] encode(Long object) { return String.valueOf(object).getBytes(); }
	};
	
	/* (non-Javadoc)
	 * @see org.jredis.semantics.Semantics#semantic(java.lang.Class, org.jredis.JRedis)
	 */
	@Override
	public <T> SemanticJRedis<T> forType(Class<T> type)
			throws NotSupportedException 
	{
		SemanticJRedis<?> semantics = null;
		if(type.isAssignableFrom(String.class)) {
			semantics = new SemanticJRedisSupport<String> (jredis, (Codec<String>)map.get(String.class));
		}
		else
		if(type.isAssignableFrom(Serializable.class)) {
			semantics = new SemanticJRedisSupport<String> (jredis, (Codec<String>)map.get(String.class));
		}
		return (SemanticJRedis<T>) semantics;
	}


//	private final static SemanticManager _instance = new SemanticManager();
	private final static Map<Class<?>, Codec<?>> map = new HashMap<Class<?>, Codec<?>>();
	static {
		map.put(String.class, DefaultCodec.java_lang_String_codec);
		map.put(Integer.class, DefaultCodec.java_lang_Integer_codec);
		map.put(Long.class, DefaultCodec.java_lang_Long_codec);
	}
	private final JRedis jredis;
	private SemanticManager (JRedis jredis){
		this.jredis = jredis;
	}
	public static Semantics getSemantics(JRedis binaryJRedis) {
		return new SemanticManager(binaryJRedis);
	}

	/* (non-Javadoc)
	 * @see org.jredis.semantics.CodecManager#getCodec(java.lang.Class)
	 */
	@Override
	public <T> Codec<T> getCodec(Class<T> type) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jredis.semantics.CodecManager#register(org.jredis.semantics.Codec, java.lang.Class)
	 */
	@Override
	public <T> boolean register(Codec<T> code, Class<T> type) {
		return false;
	}
}
