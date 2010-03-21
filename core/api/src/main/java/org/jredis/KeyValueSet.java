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

import java.io.Serializable;


/**
 * Generalized interface to support 'Multi SET' ops, {such as @link JRedis#mset(ByteArrays)} and {@link JRedis#msetnx(ByteArrays)}.
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Nov 25, 2009
 * @since   alpha.0
 * @see JRedis
 * @see JRedisFuture
 */

public interface KeyValueSet <T> {
	
	/**
	 * Adds a key/value pair to the set for the multi set op.
	 * @param key
	 * @param value
	 * @return
	 */
	public KeyValueSet<T> add(String key, T value);
	
	
	/**
	 * For internal use by the implementations.
	 * @return the 2-dim byte array
	 */
	byte[][] getMappings ();
	
	// ------------------------------------------------------------------------
	// Type specific extensions
	// ------------------------------------------------------------------------
	
	/** 
	 * Specialization of {@link KeyValueSet} for byte[] value payloads. You should
	 * use this data structure and corresponding {@link JRedis#mset(ByteArrays)} and
	 * {@link JRedis#msetnx(ByteArrays)} when you have a mix of various value types.
	 */
	public interface ByteArrays extends KeyValueSet<byte[]>{}
	
	/**
	 * Specialization of {@link KeyValueSet} for {@link Number} values.
	 */
	public interface Numbers extends KeyValueSet<Number>{}

	/**
	 * Specialization of {@link KeyValueSet} for {@link String} values.
	 */
	public interface Strings extends KeyValueSet<String>{}
	
	/**
	 * Specialization of {@link KeyValueSet} for {@link Serializable} values.
	 */
	public interface Objects <T extends Serializable> extends KeyValueSet<T>{}
}
