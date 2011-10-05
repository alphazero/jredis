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

package org.jredis.semantics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jredis.Codec;

// REFACTOR: this doesn't belong here
//
public final class BasicCodecManager implements CodecManager {
	private final Map<Class<?>, Codec<?>> map = new ConcurrentHashMap<Class<?>, Codec<?>>();
	public void foo () {
		Codec<String>  stringCodec = null;
		map.put(String.class, stringCodec);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Codec<T> getCodec(Class<T> type) {
		return (Codec<T>) map.get(type);
	}

	@Override
	public <T> boolean register(Codec<T> code, Class<T> type) {
		Codec<?> existing = map.get(type);
		if(null == existing){
			if (code.supports(type)){
				map.put(type, code);
				return true;
			}
		}
		return false;
	}
}