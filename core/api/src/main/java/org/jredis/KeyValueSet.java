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
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Nov 25, 2009
 * @since   alpha.0
 * 
 */

public interface KeyValueSet <K extends Object, T> {
	public KeyValueSet<K, T> add(K key, T value);
	byte[][] getMappings ();
	
	public interface ByteArrays<K extends Object> extends KeyValueSet<K, byte[]>{/* nop */}
	public interface Numbers<K extends Object>  extends KeyValueSet<K, Number>{/* nop */}
	public interface Strings<K extends Object>  extends KeyValueSet<K, String>{/* nop */}
	public interface Objects<K extends Object, T extends Serializable> extends KeyValueSet<K, T>{/* nop */}
	
//	abstract static class  GenImpl<T> implements  BulkSetMappings<T> {
//		protected final Map<String, T> map = new HashMap<String, T>();
//        public BulkSetMappings<T> add (String key, T value) {
//        	map.put(key, value);
//	        return this;
//        }
//        public Map<String, T> getMappings () { return map; }
//        abstract protected byte[] toBytes(T value) ;
//	}
//	public final static class BytesValueMappings extends GenImpl<byte[]> implements  BulkSetMappings<byte[]> {
//        abstract protected byte[] toBytes(T value) {
//        	
//        }
//	}
//	public final static class StringValueMappings extends GenImpl<String> implements  BulkSetMappings<String> {
//        abstract protected byte[] toBytes(T value) {
//        	
//        }
//	}
//	public final static class NumberValueMappings extends GenImpl<Number> implements  BulkSetMappings<Number> {
//        abstract protected byte[] toBytes(T value) {
//        	
//        }
//	}
//	public final static class ObjectValueMappings <T extends Serializable> extends GenImpl<T> implements  BulkSetMappings<T> {
//        abstract protected byte[] toBytes(T value) {
//        	
//        }
//	}
//	
//	public static final class TestMe {
//		@SuppressWarnings("null")
//        public static void main (String[] args) {
//	        BulkSetMappings<byte[]> byteMappings = new BytesValueMappings();
//	        byteMappings
//	        	.add("foo", "woof".getBytes())
//	        	.add("bar", "meow".getBytes())
//	        	.add("paz", "the salt".getBytes())
//	        	.add("x?", "yz!".getBytes());
//	        
//	        try {
//		        JRedis jredis = null;
//	            if(jredis.mset(byteMappings)) {
//	            	System.out.println("ok!");
//	            }
//            }
//            catch (RedisException e) {
//	            // TODO Auto-generated catch block
//	            e.printStackTrace();
//            }
//        }
//	}
}
