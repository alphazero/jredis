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
 * Interface to specify the semantics of the actual key/values stored in the Redis server.
 *
 * @author  Joubin (alphazero@sensesay.net)
 * @version alpha.0, Aug 23, 2009
 * @since   alpha.0
 * 
 */

public interface Semantics {
	/**
	 * @param <T>
	 * @param keyClass
	 * @return the Codec used to 
	 */
	public <T> Codec<T> getKeyCodec(Class<T> keyClass);
	/**
	 * @param <T>
	 * @param keyClass
	 * @param keyCodec
	 * @return
	 */
	public <T> Semantics setKeyCodec(Class<T> keyClass, Codec<T> keyCodec);
	/**
	 * @param <T>
	 * @param valueClass
	 * @return
	 */
	public <T> Codec<T> getValueCodec(Class<T> valueClass);
	/**
	 * @param <T>
	 * @param valueClass
	 * @param valueCdec
	 * @return
	 */
	public <T> Semantics setValueCodec(Class<T> valueClass, Codec<T> valueCdec);
}
