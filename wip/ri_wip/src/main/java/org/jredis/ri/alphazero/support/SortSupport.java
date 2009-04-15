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

import java.util.List;

import org.jredis.ClientRuntimeException;
import org.jredis.RedisException;
import org.jredis.Sort;


public abstract class SortSupport implements Sort {
	protected final String key;
	protected final byte[] keyBytes;
	protected SortSupport (String key, byte[] validatedKeyBytes){
		Assert.notNull(key, "key for sort", ClientRuntimeException.class);
		this.key = key;
		this.keyBytes = validatedKeyBytes;
	}
	static final String ALPHA = " ALPHA ";
	static final String DESC = " DESC ";
	static final String ASC = " ASC ";
	static final String LIMIT = " LIMIT ";
	static final String GET = " GET ";
	static final String BY = " BY ";
	
	String alphaSpec = "";
	String sortSpec = "";
	String  getSpec = "";
	String  bySpec = "";
	String  limitSpec = "";
	public Sort ALPHA() { alphaSpec = ALPHA; return this;}
	public Sort DESC() { sortSpec = DESC; return this;}
	public Sort BY(String pattern) { bySpec = BY + pattern; return this; }
	public Sort GET(String pattern) { getSpec = GET + pattern + " "; return this; }
	public Sort LIMIT(long from, long to) {
		// TODO: validate here
		Assert.inRange(to, 0, Long.MAX_VALUE, "from in LIMIT clause", ClientRuntimeException.class);
		Assert.inRange(to, from, Long.MAX_VALUE, "to in LIMIT clause (when from=" + from + ")", ClientRuntimeException.class);
		limitSpec = LIMIT + from + " " + to;
		return this;
	}
	public List<byte[]> exec() throws IllegalStateException, RedisException {
		// SORT key by pattern limit from to get pattern desc alpha 
		StringBuilder spec = new StringBuilder()
			.append(bySpec)
			.append(limitSpec)
			.append(getSpec)
			.append(sortSpec)
			.append(alphaSpec);
		byte[] sortSpec = spec.toString().getBytes();
		return execSort (keyBytes, sortSpec);
	}
	protected abstract List<byte[]> execSort (byte[] keyBytes, byte[] sortSpecBytes) throws IllegalStateException, RedisException;
}