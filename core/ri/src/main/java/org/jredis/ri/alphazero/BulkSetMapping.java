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

public abstract class BulkSetMapping<T> implements KeyValueSet<T>{
	private final Map<String, T> map = new HashMap<String, T>();
	abstract byte[] toBytes(T value) ;
	public byte[][] getMappings () {
		KeyCodec codec = DefaultKeyCodec.provider();
		byte[][] mappings = new byte[map.size()*2][];
		int i = 0;
		for (Entry<String, T> e : map.entrySet()){
			mappings[i++] = codec.encode(e.getKey());
			mappings[i++] = toBytes(e.getValue());
		}
		return mappings;
	}
    public KeyValueSet<T> add (String key, T value) {
    	map.put(key, value);
        return this;
    }
// 
    public static KeyValueSet.ByteArrays newByteArrayKVSet() { return new BulkSetMapping.Bytes(); }
	final static class Bytes extends BulkSetMapping<byte[]> implements  KeyValueSet.ByteArrays {
	    byte[] toBytes(byte[] value) { return value;}
	}
    public static KeyValueSet.Strings newStringKVSet() { return new BulkSetMapping.Strings(); }
	final static class Strings extends BulkSetMapping<String> implements  KeyValueSet.Strings {
	    byte[] toBytes(String value) { return DefaultCodec.encode(value);}
	}
    public static KeyValueSet.Numbers newNumberKVSet() { return new BulkSetMapping.Numbers(); }
	final static class Numbers extends BulkSetMapping<Number> implements  KeyValueSet.Numbers {
	    byte[] toBytes(Number value) { return String.valueOf(value).getBytes();}
	}
    public static KeyValueSet.Numbers newObjectKVSet() { return new BulkSetMapping.Numbers(); }
	final static class  Objects <T extends Serializable> extends BulkSetMapping<T> implements  KeyValueSet.Objects<T> {
	    byte[] toBytes(T value) { return DefaultCodec.encode(value);}
	}
}
