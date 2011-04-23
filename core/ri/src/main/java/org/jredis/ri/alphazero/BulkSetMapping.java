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

package org.jredis.ri.alphazero;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jredis.KeyValueSet;
import org.jredis.ri.alphazero.semantics.DefaultKeyCodec;
import org.jredis.ri.alphazero.support.DefaultCodec;
import org.jredis.semantics.KeyCodec;

public abstract class BulkSetMapping<K extends Object, T> implements KeyValueSet<K, T>{
	private final Map<Object, T> map = new HashMap<Object, T>();
	abstract byte[] toBytes(T value) ;
	public byte[][] getMappings () {
		KeyCodec<Object> codec = DefaultKeyCodec.provider();
		byte[][] mappings = new byte[map.size()*2][];
		int i = 0;
		for (Entry<Object, T> e : map.entrySet()){
			mappings[i++] = codec.encode(e.getKey());
			mappings[i++] = toBytes(e.getValue());
		}
		return mappings;
	}
    public KeyValueSet<K, T> add (K key, T value) {
    	map.put(key, value);
        return this;
    }
// 
    public static <K extends Object> KeyValueSet.ByteArrays<K> newByteArrayKVSet() { return new BulkSetMapping.Bytes<K>(); }
	final static class Bytes<K extends Object> extends BulkSetMapping<K, byte[]> implements  KeyValueSet.ByteArrays<K> {
	    byte[] toBytes(byte[] value) { return value;}
	}
    public static <K extends Object> KeyValueSet.Strings<K> newStringKVSet() { return new BulkSetMapping.Strings<K>(); }
	final static class Strings<K extends Object>  extends BulkSetMapping<K, String> implements  KeyValueSet.Strings<K> {
	    byte[] toBytes(String value) { return DefaultKeyCodec.provider().encode(value);}
	}
    public static <K extends Object> KeyValueSet.Numbers<K> newNumberKVSet() { return new BulkSetMapping.Numbers<K>(); }
	final static class Numbers<K extends Object>  extends BulkSetMapping<K, Number> implements  KeyValueSet.Numbers<K> {
	    byte[] toBytes(Number value) { return String.valueOf(value).getBytes();}
	}
    public static <K extends Object, T extends Serializable> KeyValueSet.Objects<K, T> newObjectKVSet() { return new BulkSetMapping.Objects<K, T>(); }
	final static class  Objects <K extends Object, T extends Serializable> extends BulkSetMapping<K, T> implements  KeyValueSet.Objects<K, T> {
	    byte[] toBytes(T value) { return DefaultCodec.encode(value);}
	}
}
