/*
 *   Copyright 2009-2010 Joubin Houshyar
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
	protected volatile boolean stores = false;
	protected final String key;
	protected final byte[] keyBytes;
	protected SortSupport (String key, byte[] validatedKeyBytes){
		Assert.notNull(key, "key for sort", ClientRuntimeException.class);
		this.key = key;
		this.keyBytes = validatedKeyBytes;
	}
	private static final String NO_OP_SPEC = "";
	private static final String PAD = " ";
	private String alphaSpec = NO_OP_SPEC;
	private String sortSpec = NO_OP_SPEC;
	private String getSpec = NO_OP_SPEC;
	private String bySpec = NO_OP_SPEC;
	private String limitSpec = NO_OP_SPEC;
	private String storeSpec = NO_OP_SPEC;
	
	public Sort ALPHA() {  alphaSpec = String.format("%s%s", Command.Option.ALPHA.name(), PAD); return this; }
	public Sort DESC() { sortSpec = String.format("%s%s", Command.Option.DESC.name(), PAD); return this;}
	public Sort BY(String pattern) { bySpec = String.format("%s %s%s", Command.Option.BY.name(), pattern, PAD); return this; }
	public Sort GET(String pattern) { getSpec = String.format("%s %s%s", Command.Option.GET.name(), pattern, PAD); return this; }
	public Sort LIMIT(long from, long count) {
		if(from < 0) throw new ClientRuntimeException("from in LIMIT clause: " + from);
		if(count <= 0) throw new ClientRuntimeException("count in LIMIT clause: " + from);
		limitSpec = String.format("%s %d %d%s", Command.Option.LIMIT.name(), from, count, PAD);
		return this;
	}
	/** Store the sort results in another key */
	public Sort STORE (String destKey) {
		Assert.notNull(destKey, "deskKey is null", ClientRuntimeException.class);
		// TODO: check for whitespaces
		storeSpec = String.format("%s %s%s", Command.Option.STORE, destKey, PAD);
		stores = true;
		return this;
	}
	private final byte[] getSortSpec() {
		StringBuilder spec = new StringBuilder()
			.append(bySpec)
			.append(limitSpec)
			.append(getSpec)
			.append(sortSpec)
			.append(alphaSpec)
			.append(storeSpec);
		return spec.toString().trim().getBytes();
	}
	public List<byte[]> exec() throws IllegalStateException, RedisException {
		System.out.format("sort spec: [%S]\n", new String(getSortSpec()));
		List<byte[]> res = null;
		if(!stores)
			res = execSort (keyBytes, getSortSpec());
		else 
			res = execSortStore(keyBytes, getSortSpec());
		return res;
	}
	public Future<List<byte[]>> execAsynch() {
		System.out.format("sort spec: [%S]\n", new String(getSortSpec()));
		Future<List<byte[]>>  res = null;
		if(!stores)
			res = execAsynchSort (keyBytes, getSortSpec());
		else 
			res = execAsynchSortStore(keyBytes, getSortSpec());
		return res;
	}
	protected abstract List<byte[]> execSort (byte[] keyBytes, byte[] sortSpecBytes) throws IllegalStateException, RedisException;
	protected abstract List<byte[]> execSortStore (byte[] keyBytes, byte[] sortSpecBytes) throws IllegalStateException, RedisException;
	protected abstract Future<List<byte[]>> execAsynchSort (byte[] keyBytes, byte[] sortSpecBytes);
	protected abstract Future<List<byte[]>> execAsynchSortStore (byte[] keyBytes, byte[] sortSpecBytes);
}