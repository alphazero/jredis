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
import java.util.concurrent.Future;

import org.jredis.ClientRuntimeException;
import org.jredis.RedisException;
import org.jredis.Sort;
import org.jredis.protocol.Command;


public abstract class SortSupport implements Sort {
	protected final String key;
	protected final byte[] keyBytes;
	protected SortSupport (String key, byte[] validatedKeyBytes){
		Assert.notNull(key, "key for sort", ClientRuntimeException.class);
		this.key = key;
		this.keyBytes = validatedKeyBytes;
	}
	static final String WSPAD = " ";
	String alphaSpec = "";
	String sortSpec = "";
	String  getSpec = "";
	String  bySpec = "";
	String  limitSpec = "";
	
	public Sort ALPHA() { alphaSpec = Command.Options.ALPHA.name() + WSPAD; return this;}
	public Sort DESC() { sortSpec = Command.Options.DESC.name() + WSPAD; return this;}
	public Sort BY(String pattern) { bySpec = Command.Options.BY.name() + WSPAD + pattern; return this; }
	public Sort GET(String pattern) { getSpec = Command.Options.GET.name() + WSPAD + pattern + " "; return this; }
	public Sort LIMIT(long from, long count) {
		if(from < 0) throw new ClientRuntimeException("from in LIMIT clause: " + from);
		if(count <= 0) throw new ClientRuntimeException("count in LIMIT clause: " + from);
		limitSpec = Command.Options.LIMIT.name() + WSPAD + from + " " + count;
		return this;
	}
	private final byte[] getSortSpec() {
		StringBuilder spec = new StringBuilder()
			.append(bySpec)
			.append(limitSpec)
			.append(getSpec)
			.append(sortSpec)
			.append(alphaSpec);
		return spec.toString().getBytes();
	}
	public List<byte[]> exec() throws IllegalStateException, RedisException {
		return execSort (keyBytes, getSortSpec());
	}
	public Future<List<byte[]>> execAsynch() {
		return execAsynchSort (keyBytes, getSortSpec());
	}
	protected abstract List<byte[]> execSort (byte[] keyBytes, byte[] sortSpecBytes) throws IllegalStateException, RedisException;
	protected abstract Future<List<byte[]>> execAsynchSort (byte[] keyBytes, byte[] sortSpecBytes);
}