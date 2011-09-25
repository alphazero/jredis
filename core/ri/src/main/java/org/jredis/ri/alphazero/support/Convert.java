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
		if(i < INT_N_65535 || i > INT_P_65535) {
			return Integer.toString(i).getBytes();
		}
		final int absi = Math.abs(i);
		final byte[] cachedData = i2b_65535[absi];
		final byte[] data;
		if(cachedData == null) {
			data = Integer.toString(absi).getBytes();
			i2b_65535[absi] = data;
		}
		else {
			data = cachedData;
		}
		return i >= 0 ? data : getNegativeNumberBytes(data);
	}

	/**
	 * Will delegate to {@link Convert#getBytes(int)} if the 'long' number is actually
	 * within the range of our int cache, otherwise it will return the bytes using std
	 * JDK mechanisms.
	 * @param lnum
	 * @return
	 */
	public static final byte[] toBytes(long lnum){
		if(lnum >= INT_N_65535 && lnum <= INT_P_65535) 
			return toBytes((int)lnum);
		
		return Long.toString(lnum).getBytes();
	}
	
	public static final byte[] toBytes(double dnum){
		return Double.toString(dnum).getBytes();
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
	public static final int toInt(final byte[] potentiallySignedAsciiBytes, final int offsetin, final int len) throws IllegalArgumentException
	{
		int offset = offsetin;
		final byte[] buff = potentiallySignedAsciiBytes; // lets use a sensible name ;)
		if(null == buff) throw new IllegalArgumentException ("Null input");
		if(len > buff.length) throw new IllegalArgumentException ("buffer length of " + buff.length + " less than the spec'd len " + len);

		boolean negative = false;
		int digitCnt = len;
		final byte bs = buff[offset];
		if(bs ==BYTE_MINUS || bs == BYTE_PLUS){
			if(bs == BYTE_MINUS) negative = true;
			offset++;
			digitCnt--;
		}
		if(digitCnt > MAX_POSITIVE_32_BIT_DIGITS) throw new IllegalArgumentException ("This \"int\" has more digits than a 32 bit signed number:" + digitCnt);
		
		// lets do it
		int value = 0;
		for(int p = 0; p < digitCnt; p++){
			final byte b = buff[offset+p];
			if(b < BYTE_ZERO || b > BYTE_NINE) throw new IllegalArgumentException("That's not a number!  byte value: " + b);
			value = value*10 + b - BYTE_ZERO;
		}
		if(negative) value = 0 - value;
		
		return value;
	}
	
	/**
	 * Its just like (really! :) {@link Convert#toInt(byte[], int, int)} but for {@link Long} values.  Max number of digits 
	 * is now {@link Convert#MAX_POSITIVE_64_BIT_DIGITS}.  
	 * 
	 * @param potentiallySignedAsciiBytes
	 * @param offset
	 * @param len
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static final long toLong(byte[] potentiallySignedAsciiBytes, final int offsetin, int len) throws IllegalArgumentException
	{
		int offset = offsetin;
		final byte[] buff = potentiallySignedAsciiBytes; // lets use a sensible name ;)
		if(null == buff) throw new IllegalArgumentException ("Null input");
		if(len > buff.length) throw new IllegalArgumentException ("buffer length of " + buff.length + " less than the spec'd len " + len);

		boolean negative = false;
		int digitCnt = len;
		final byte bs = buff[offset];
		if(bs ==BYTE_MINUS || bs == BYTE_PLUS){
			if(buff[offset]==BYTE_MINUS) negative = true; 
			offset++;
			digitCnt--;
		}
		if(digitCnt > MAX_POSITIVE_64_BIT_DIGITS) throw new IllegalArgumentException ("This \"int\" has more digits than a 32 bit signed number:" + digitCnt);
		
		// lets do it
		long value = 0;
		for(int p = 0; p < digitCnt; p++){
			final byte b = buff[offset+p];
			if(b < BYTE_ZERO || b > BYTE_NINE) throw new IllegalArgumentException("That's not a number!  byte value: " + b);
			value = value*10 + b - BYTE_ZERO;
		}
		if(negative) value = 0 - value;
		
		return value;
	}
	
	/**
	 * @param potentiallySignedBytes
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static final int toInt(byte[] potentiallySignedBytes) throws IllegalArgumentException
	{
		if(null == potentiallySignedBytes) throw new IllegalArgumentException ("null input");
		return toInt(potentiallySignedBytes, 0, potentiallySignedBytes.length);
	}
	
	/**
	 * @param potentiallySignedBytes
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static final long toLong(byte[] potentiallySignedBytes) throws IllegalArgumentException
	{
		if(null == potentiallySignedBytes) throw new IllegalArgumentException ("null input");
		return toLong(potentiallySignedBytes, 0, potentiallySignedBytes.length);
	}
	
	/**
	 * TODO: optimize.
	 * @param potentiallySignedBytes
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static final double toDouble (byte[] stringRepOfDoublePrecisionBytes) throws IllegalArgumentException
	{
		double dnum = 0;
		if(null == stringRepOfDoublePrecisionBytes) throw new IllegalArgumentException ("null input");
		try {
			dnum = Double.parseDouble(new String(stringRepOfDoublePrecisionBytes));
		}
		catch (Exception e){
			throw new IllegalArgumentException("", e);
		}
		return dnum;
	}
	
	// ------------------------------------------------------------------------
	// Inner ops
	// ------------------------------------------------------------------------
	/**
	 * @param unsigned
	 * @return
	 */
	private static final byte[] getNegativeNumberBytes(byte[] unsigned){
		int unsigned_length = unsigned.length;
		byte[] data = new byte[unsigned_length+1];
		data [0] = BYTE_MINUS;
		System.arraycopy(unsigned, 0, data, 1, unsigned_length);
		return data;
	}
}