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

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Encapsulates the debug information returned by Redis in response to DEBUG
 * OBJECT <key>.
 * 
 * @author Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Mar 16, 2010
 * @since alpha.0
 * @see JRedis#debug(String)
 */

public class ObjectInfo {
	private static final long numTokens = 7;
	private final String keyAddress;
	private final long keyRefCount;
	private final ObjectEncoding encoding;
	private final long serializedLength;
	private final long lru;
	private final long lruSecondsIdle;

	public ObjectInfo(String keyAddress, long keyRefCount,
			ObjectEncoding encoding, long serializedLength, long lru,
			long lruSecondsIdle) {
		this.keyAddress = keyAddress;
		this.keyRefCount = keyRefCount;
		this.encoding = encoding;
		this.serializedLength = serializedLength;
		this.lru = lru;
		this.lruSecondsIdle = lruSecondsIdle;
	}

	/**
	 * @return the keyAddress as {@link String} representation of hex address.
	 *         Ex: "0x100d60"
	 */
	public String getKeyAddress() {
		return keyAddress;
	}

	/** @return the keyRefCount */
	public long getKeyRefCount() {
		return keyRefCount;
	}

	/**
	 * @return the valueAddress as {@link String} representation of hex address.
	 *         Ex: "0x100d60"
	 */

	/** @return the encoding */
	public ObjectEncoding getEncoding() {
		return encoding;
	}

	/**
	 * Convenience method to convert the address info to long. Ex: "0x101860" =>
	 * 1054816. (Note that the returned number is (obviously) base 10.)
	 * 
	 * @param addressStrRep
	 * @return
	 */
	public static long toLong(String addressStrRep) {
		return Long.parseLong(addressStrRep.substring(2), 16);
	}

	@SuppressWarnings("boxing")
	@Override
	public String toString() {
		Formatter formatter = new Formatter();
		formatter
				.format("ObjectInfo: key [addr:%s  refCnt: %d] encoding:%s serializedLength: %d lru %d lruSecondsIdle %d",
						keyAddress, keyRefCount, encoding, serializedLength,
						lru, lruSecondsIdle);
		return formatter.toString();
	}

	/**
	 * Strictly speaking, this doesn't belong here but cuts down on redundant
	 * code. Parses the DEBUG OBJECT <key> response to return an instance of
	 * {@link ObjectInfo}.
	 * 
	 * @param strRep
	 * @return {@link ObjectInfo}
	 */
	static public final ObjectInfo valueOf(String strRep) {
		StringTokenizer tokenizer = new StringTokenizer(strRep);
		int tokenCnt = tokenizer.countTokens();

		if (tokenCnt != numTokens)
			throw new ProviderException(
					"DEBUG OBJECT <key> response does not conform to expected format.  Got: ["
							+ strRep + "]");

		List<String> tokens = new ArrayList<String>(tokenCnt);

		while (tokenizer.hasMoreElements())
			tokens.add(tokenizer.nextToken());

		String keyAddr = tokens.get(1).substring(3);
		String keyCnt = tokens.get(2).substring("refcount:".length());
		String encodingRep = tokens.get(3).substring("encoding:".length());
		String serlen = tokens.get(4).substring("serializedlength:".length());
		String lru = tokens.get(5).substring("lru:".length());
		String lruSecondsIdle = tokens.get(6).substring(
				"lru_seconds_idle:".length());

		ObjectInfo info = new ObjectInfo(keyAddr, Integer.parseInt(keyCnt),
				ObjectEncoding.valueOf(encodingRep.toUpperCase()),
				Integer.parseInt(serlen), Integer.parseInt(lru),
				Integer.parseInt(lruSecondsIdle));

		return info;
	}
}
