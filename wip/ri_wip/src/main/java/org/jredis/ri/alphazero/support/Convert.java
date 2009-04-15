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


/**
 * Perhaps a silly hack, but proven to speed things up.
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 02, 2009
 * @since   alpha.0
 * 
 */
public class Convert {
	
	// ------------------------------------------------------------------------
	// Constants
	// ------------------------------------------------------------------------
	public static final int 		INT_P_65535 = 65535;
	public static final int 		INT_N_65535 = 0 - INT_P_65535;
	private static final byte[][]   i2b_65535 = new byte[INT_P_65535+1][];
	private static final byte       BYTE_MINUS = (byte) '-';
	private static final byte       BYTE_PLUS = (byte) '+';
	private static final byte	    BYTE_ZERO = (byte) '0';
	private static final byte	    BYTE_NINE = (byte) '9';

	private static final int	MAX_POSITIVE_32_BIT_DIGITS	= 10;
	private static final int	MAX_POSITIVE_64_BIT_DIGITS	= 19;
//	private static final int	MAX_NEGATIVE_INT_REP_BUFFER_SIZE	= MAX_POSITIVE_INT_REP_BUFFER_SIZE + 1;
	private static final long [] TEN_TO_POWER = {
		1L,				
		10L,				
		100L,			
		1000L,
		10000L,
		100000L,
		1000000L,
		10000000L,
		100000000L,    
		1000000000L,			// 10^9 for Integer.MAX_VALUE @ 214748364
		10000000000L,
		100000000000L,
		1000000000000L,
		10000000000000L,
		100000000000000L,
		1000000000000000L,
		10000000000000000L,
		100000000000000000L,
		1000000000000000000L 	// 10^18 for Long.MAX_VALUE @ 9223372036854775807
	};
	/**
	 * A few hundred Ks/classloader  Speed things up considerably in the long run as far
	 * as int to byte[] conversions are concerned.  
	 * Reduce the constant to adjust memory consumption as required.
	 */
	static {
		for(int i=0; i<INT_P_65535; i++) getBytes(i);
	}
	
	// ------------------------------------------------------------------------
	// public Interface
	// ------------------------------------------------------------------------
	
	/**
	 * return the bytes of the string representation of the integer.  Perhaps
	 * should be called getNumberBytes, or perhaps getHumanReadableBytes()  
	 * Ex:  444 => "444".getBytes() => new byte[3]={52, 52, 52}
	 * 
	 * If representation is not in our cache or too high (larger than Convert#INT_P_65535 )
	 * then it will return whatever we would get from {@link Convert#toBytes(int)} which is
	 * using JDK libs for the same.  
	 * 
	 * The whole point here is to be faster since we need to convert numbers to the byte array 
	 * of their string representation a lot in JRedis for the protocol and to cut out the 
	 * unnecessary step of creating a new string simply because there apparently isn't any other
	 * way in JDK to go from a number to the bytes of its textual representation. (!)  
	 * 
	 * @param i
	 * @param signed
	 * @return
	 */ 
	public static final byte[] toBytes(int i){
		byte[] data = null;
		boolean negative=false;
		if(i >= INT_N_65535 && i <= INT_P_65535) {
			if(i < 0) {
				negative = true;
				i = 0 - i;
			}
			if(null == i2b_65535[i]){
				i2b_65535[i] = getBytes(i);
			}
			data = i2b_65535[i];
			if(negative) data = getSignedNumberBytes(data, negative);
		}
		else {
			data = getBytes(i);
		}
		if(null == data) throw new RuntimeException("null for i=" + i + " and cache is: " + i2b_65535[i]);
		return data;
	}
	/**
	 * Converts the byte[]s of the ASCII representation of a decimal number to an int.  
	 * 
	 * <p>Expects a byte array of no more than {@link Convert#MAX_POSITIVE_32_BIT_DIGITS} bytes in length
	 * after accounting for potential leading byte indicating the sign of the number representation.
	 * 9 bytes (for a positive integer).  
	 *
	 * @param potentiallySignedAsciiBytes, for example {49, 49, 52} ("114")
	 * @return
	 * @throw IllegalArgumentException if buffer contains anything other than values 48 to 57
	 */
	public static final int getInt(byte[] potentiallySignedAsciiBytes, int offset, int len) throws IllegalArgumentException
	{
		byte[] buff = potentiallySignedAsciiBytes; // lets use a sensible name ;)
		if(null == buff) throw new IllegalArgumentException ("Null input");
		if(len > buff.length) throw new IllegalArgumentException ("buffer length of " + buff.length + " less than the spec'd len " + len);

		boolean negative = false;
		int digitCnt = len;
		if(buff[offset]==BYTE_MINUS || buff[offset]==BYTE_PLUS){ 
			if(buff[offset]==BYTE_MINUS) negative = true; 
			offset++;
			digitCnt--;
		}
		if(digitCnt > MAX_POSITIVE_32_BIT_DIGITS) throw new IllegalArgumentException ("This \"int\" has more digits than a 32 bit signed number:" + digitCnt);
		
		// lets do it
		int value = 0;
		for(int p = 0; p < digitCnt; p++){
			byte b = buff[offset+p];
			if(b < BYTE_ZERO || b > BYTE_NINE) throw new IllegalArgumentException("That's not a number!  byte value: " + b);
			value += (b-BYTE_ZERO) * Convert.TEN_TO_POWER[digitCnt-p-1];
		}
		if(negative) value = 0 - value;
		
		// done.
		return value;
	}
	
	/**
	 * Its just like (really! :) {@link Convert#getInt(byte[], int, int)} but for {@link Long} values.  Max number of digits 
	 * is now {@link Convert#MAX_POSITIVE_64_BIT_DIGITS}.  
	 * 
	 * @param potentiallySignedAsciiBytes
	 * @param offset
	 * @param len
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static final long getLong(byte[] potentiallySignedAsciiBytes, int offset, int len) throws IllegalArgumentException
	{
		byte[] buff = potentiallySignedAsciiBytes; // lets use a sensible name ;)
		if(null == buff) throw new IllegalArgumentException ("Null input");
		if(len > buff.length) throw new IllegalArgumentException ("buffer length of " + buff.length + " less than the spec'd len " + len);

		boolean negative = false;
		int digitCnt = len;
		if(buff[offset]==BYTE_MINUS || buff[offset]==BYTE_PLUS){ 
			if(buff[offset]==BYTE_MINUS) negative = true; 
			offset++;
			digitCnt--;
		}
		if(digitCnt > MAX_POSITIVE_64_BIT_DIGITS) throw new IllegalArgumentException ("This \"int\" has more digits than a 32 bit signed number:" + digitCnt);
		
		// lets do it
		long value = 0;
		for(int p = 0; p < digitCnt; p++){
			byte b = buff[offset+p];
			if(b < BYTE_ZERO || b > BYTE_NINE) throw new IllegalArgumentException("That's not a number!  byte value: " + b);
			value += (b-BYTE_ZERO) * Convert.TEN_TO_POWER[digitCnt-p-1];
		}
		if(negative) value = 0 - value;
		
		// done.
		return value;
	}
	
	/**
	 * @param potentiallySignedBytes
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static final int getInt(byte[] potentiallySignedBytes) throws IllegalArgumentException
	{
		if(null == potentiallySignedBytes) throw new IllegalArgumentException ("null input");
		return getInt(potentiallySignedBytes, 0, potentiallySignedBytes.length);
	}
	
	/**
	 * @param potentiallySignedBytes
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static final long getLong(byte[] potentiallySignedBytes) throws IllegalArgumentException
	{
		if(null == potentiallySignedBytes) throw new IllegalArgumentException ("null input");
		return getLong(potentiallySignedBytes, 0, potentiallySignedBytes.length);
	}
	
	// ------------------------------------------------------------------------
	// Inner ops
	// ------------------------------------------------------------------------
	/**
	 * @param unsigned
	 * @param negative
	 * @return
	 */
	private static final byte[] getSignedNumberBytes(byte[] unsigned, boolean negative){
		int unsigned_length = unsigned.length;
		byte[] data = new byte[unsigned_length+1];
		data [0] = negative ? BYTE_MINUS : BYTE_PLUS;
		System.arraycopy(unsigned, 0, data, 1, unsigned_length);
		return data;
	}
	/**
	 * return the bytes of the string representation of the integer.  Perhaps
	 * should be called getNumberBytes, or perhaps getHumanReadableBytes()  
	 * Ex:  444 => "444".getBytes() => new byte[3]={52, 52, 52}
	 * @param i
	 * @return
	 */
	private static final byte[] getBytes(int i){
		return Integer.toString(i).getBytes();
	}
}
