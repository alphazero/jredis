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

package org.jredis.ri.alphazero.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jredis.JRedis;
import org.jredis.semantics.KeyCodec;

/**
 * Note that this is the one element of this package that is most likely to change
 * drastically.  It is provided, for now, as a helper for using {@link JRedis}.
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 * 
 */
public class DefaultCodec {
	public static class Keys<K extends Object> implements KeyCodec<K>{
		public enum SupportedType {
			STRING (),
			BYTES ();
		}

		/* (non-Javadoc)  @see org.jredis.Codec#decode(byte[]) */
		@Override
		public K decode(byte[] bytes) {
			throw new RuntimeException("TODO Auto-generated Codec<K>#decode stub -- NOT IMPLEMENTED");
		}

		/* (non-Javadoc)  @see org.jredis.Codec#encode(java.lang.Object) */
		@Override
		public byte[] encode(K object) {
			throw new RuntimeException("TODO Auto-generated Codec<K>#encode stub -- NOT IMPLEMENTED");
		}

		/* (non-Javadoc)  @see org.jredis.Codec#supports(java.lang.Class) */
		@Override
		public boolean supports(Class<?> type) {
			throw new RuntimeException("TODO Auto-generated Codec<K>#supports stub -- NOT IMPLEMENTED");
		}
	}

	public final static String SUPPORTED_CHARSET_NAME = "UTF-8";	// this is for jdk 1.5 compliance
	public final static Charset SUPPORTED_CHARSET = Charset.forName ("UTF-8");
	
	/**
	 * This helper method is mainly intended for use with a list of
	 * keys returned from Redis, given that it will use the UTF-8
	 * {@link Charset} in decoding the byte array.  Typical use would
	 * be to convert from the List<byte[]> output of {@link JRedis#keys()}
	 * 
	 * @param bytearray
	 * @return
	 */
	public static final List<String> toStr (List<byte[]> bytearray) {
		if(null == bytearray) return null;
		List<String> list = new ArrayList<String>(bytearray.size());
		for(byte[] b : bytearray) 
			if(null!= b) 
				list.add(toStr(b)); 
			else 
				list.add(null);
		return list;
	}
	public static final Map<String, byte[]> toDataDictionary (Map<byte[], byte[]> binaryMap) {
		if(null == binaryMap) return null;
		Map<String, byte[]> dict = new HashMap<String, byte[]>(binaryMap.size());
		for(byte[] bkey : binaryMap.keySet()) 
			if(null!= bkey) 
				dict.put(toStr(bkey), binaryMap.get(bkey)); 
		return dict;
	}
	/**
	 * @param bytes
	 * @return new {@link String#String(byte[])} or null if bytes is null. 
	 */
	public static final String toStr (byte[] bytes) {
        String str = null;
        if(null != bytes) {
			try {
				str = new String(bytes, SUPPORTED_CHARSET_NAME);
	        }
	        catch (UnsupportedEncodingException e) {
		        e.printStackTrace();
	        }
        }
        return str;
//		return new String(bytes, SUPPORTED_CHARSET); // Java 1.6 only
	}
	
	public static final byte[] encode(String value) {
		byte[] bytes = null;
		try {
	        bytes = value.getBytes(SUPPORTED_CHARSET_NAME);
        }
        catch (UnsupportedEncodingException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
        return bytes;
//		return value.getBytes(SUPPORTED_CHARSET);
	}
	
//	/**
//	 * @param bytes
//	 * @return
//	 */
//	@Deprecated
//	public static final Integer toInt (byte[]  bytes) {
//		return new Integer(toStr (bytes));
//	}

	/**
	 * NOTE: Isn't this already in {@link Convert#toLong(byte[])}?
	 * TODO: get rid of this method
	 * This helper method will convert the byte[] to a {@link Long}.
	 * @param bytes
	 * @return
	 */
//	@Deprecated
	@SuppressWarnings("boxing")
	public static final Long toLong (byte[]  bytes) {
//		return new Long (toStr (bytes));
		return Convert.toLong(bytes);
	}
	
	@SuppressWarnings("boxing")
	public static final List<Long> toLong(List<byte[]> bytearray){
		if(null == bytearray) return null;
		List<Long> list = new ArrayList<Long>(bytearray.size());
		for(byte[] b : bytearray) list.add(Convert.toLong(b));
		return list;
	}

	/**
     * @param bs bytes of the ascii string representation of a double number. E.g. "2.002".getBytes()
     * @return
     */
    public static double toDouble (byte[] bs) {
	    return Convert.toDouble(bs);
    }
	@SuppressWarnings("boxing")
	public static final List<Double> toDouble(List<byte[]> bytearray){
		if(null == bytearray) return null;
		List<Double> list = new ArrayList<Double>(bytearray.size());
		for(byte[] b : bytearray) list.add(Convert.toDouble(b));
		return list;
	}

	/**
	 * This helper method will assume the List<byte[]> being presented is the list returned
	 * from a {@link JRedis} method such as {@link JRedis#smembers(String)}, and that this
	 * list contains the {@link DefaultCodec#encode(Serializable)}ed bytes of the parametric type <code>T</code>.
	 * <p>
	 * Specifically, this method will instantiate an {@link ArrayList} for type T, of equal 
	 * size to the size of bytelist {@link List}.  Then it will iterate over the byte list 
	 * and for each byte[] list item call {@link DefaultCodec#decode(byte[])}.
	 * <p>
	 * <b>Usage example:</b>
	 * <pre><code>
	 * List<byte[]>  memberBytes = redis.smembers("my-object-set");
	 * List<MySerializableClass>  members = decode (memberBytes);
	 * </code></pre>
	 * @param <T>
	 * @param byteList
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <T extends Serializable>  
	List<T> decode (List<byte[]> byteList) {
		if(null == byteList) return null;
		List<T>		objectList = new ArrayList<T>(byteList.size());
		for (byte[] bytes : byteList) {
			if(null != bytes){
				T object = (T) decode(bytes);
				objectList.add (object);
			}
			else{
				objectList.add(null);
			}
		}
		return objectList;
	}
	/**
	 * This helper method will assume that the byte[] provided are the serialized
	 * bytes obtainable for an instance of type T obtained from {@link ObjectOutputStream}
	 * and subsequently stored as a value for a Redis key (regardless of key type). 
	 * <p>Specifically, this method will simply do:
	 * <pre><code>
	 * ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(bytes));
	 * t = (T) oin.readObject();
	 * </code></pre>
	 * and returning the reference <i>t</i>, and throwing any exceptions encountered along
	 * the way.
	 * <p>
	 * This method is the decoding peer of {@link DefaultCodec#encode(Serializable)}, and it is
	 * assumed (and certainly recommended) that you use these two methods in tandem.
	 * <p>
	 * Naturally, all caveats, rules, and considerations that generally apply to {@link Serializable}
	 * and the Object Serialization specification apply.
	 * @param <T>
	 * @param bytes
	 * @return the instance for <code><b>T</b></code>
	 */
	@SuppressWarnings("unchecked")
	public static final <T extends Serializable>  
	T  decode(byte [] bytes) 
	{
		T t = null;
		Exception thrown = null;
		try {
			ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(bytes));
			t = (T) oin.readObject();
		}
		catch (IOException e) {
			e.printStackTrace();
			thrown = e;
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			thrown = e;
		}
		catch (ClassCastException e) {
			e.printStackTrace();
			thrown = e;
		}
		finally {
			if(null != thrown)
				throw new RuntimeException(
						"Error decoding byte[] data to instantiate java object - " +
						"data at key may not have been of this type or even an object", thrown
				);
		}
		return t;
	}
	
	/**
	 * This helper method will serialize the given serializable object of type T
	 * to a byte[], suitable for use as a value for a redis key, regardless of the key
	 * type.
	 * 
	 * @param <T>
	 * @param obj
	 * @return
	 */
	public static final <T extends Serializable>  byte[]  encode(T obj) {
		byte[] bytes = null;
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bout);
			out.writeObject(obj);
			bytes = bout.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error serializing object"+obj+" => " + e);
		}
		return bytes;
	}
}
