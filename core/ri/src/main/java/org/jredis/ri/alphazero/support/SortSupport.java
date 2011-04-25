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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.jredis.ClientRuntimeException;
import org.jredis.RedisException;
import org.jredis.Sort;
import org.jredis.protocol.Command;
import org.jredis.ri.alphazero.semantics.DefaultKeyCodec;


public abstract class SortSupport implements Sort {
	protected volatile boolean stores = false;
//	protected final String key;
	protected final byte[] keyBytes;
//	protected SortSupport (String key, byte[] validatedKeyBytes){
	protected SortSupport (byte[] validatedKeyBytes){
//		Assert.notNull(key, "key for sort", ClientRuntimeException.class);
//		this.key = key;
		this.keyBytes = validatedKeyBytes;
	}
	
    public static final byte[]  CRLF = {(byte) 13, (byte)10};
    public static final byte[]  SPACE = {(byte) 32};
    public static final int     CRLF_LEN = CRLF.length;
    public static final int     DELIMETER_LEN = SPACE.length;
    public static final byte    ERR_BYTE    = (byte) 45; // -
    public static final byte    OK_BYTE     = (byte) 43; // +
    public static final byte    COUNT_BYTE  = (byte) 42; // *
    public static final byte    SIZE_BYTE   = (byte) 36; // $
    public static final byte    NUM_BYTE    = (byte) 58; // :
    public static final byte    ASCII_ZERO  = (byte) 48; // 0
	    
	private List<byte[]> alphaSpec = new ArrayList<byte[]>();
	private List<byte[]> descSpec = new ArrayList<byte[]>();
	private List<byte[]> getSpec = new ArrayList<byte[]>();
	private List<byte[]> bySpec = new ArrayList<byte[]>();
	private List<byte[]> limitSpec = new ArrayList<byte[]>();
	private List<byte[]> storeSpec = new ArrayList<byte[]>();
	
	public Sort ALPHA() {
//	  String alphaSpecName = Command.Option.ALPHA.name();
	  alphaSpec.add(Command.Option.ALPHA.bytes);
	  return this; 
	}
	
	public Sort DESC() {
//      String sortSpecName = Command.Option.DESC.name();
      descSpec.add(Command.Option.DESC.bytes);

	  return this;
	}
	
	public <K extends Object> Sort BY(K pattern) {
//	   String bySpecName = Command.Option.BY.name();
	   bySpec.add(Command.Option.BY.bytes);
	   bySpec.add(DefaultKeyCodec.provider().encode(pattern));
	   
	   return this; 
	 }
	
	public <K extends Object> Sort GET(K pattern) {
//      String getSpecName = Command.Option.GET.name();
      getSpec.add(Command.Option.GET.bytes);
      getSpec.add(DefaultKeyCodec.provider().encode(pattern));
      
	  return this;
	}
	
	public Sort LIMIT(long from, long count) {
		if(from < 0) {
		  throw new ClientRuntimeException("from in LIMIT clause: " + from);
		}
		
		if(count <= 0) {
		  throw new ClientRuntimeException("count in LIMIT clause: " + from);
		}
		
//		String limitSpecName = Command.Option.LIMIT.name();
	    String fromString = new Long(from).toString();
	    String countString = new Long(count).toString();
	    
	    limitSpec.add(Command.Option.LIMIT.bytes);
	    limitSpec.add(fromString.getBytes());
	    limitSpec.add(countString.getBytes());
		
		return this;
	}
	
	/** Store the sort results in another key */
	public <K extends Object> Sort STORE (K destKey) {
		Assert.notNull(destKey, "deskKey is null", ClientRuntimeException.class);
		
//		String storeSpecName = Command.Option.STORE.name();
		storeSpec.add(Command.Option.STORE.bytes);
		storeSpec.add(DefaultKeyCodec.provider().encode(destKey));
		
		stores = true;
		
		return this;
	}
	
	private final byte[][] buildSortCmd() {
	  ArrayList<byte[]> sortSpecs = new ArrayList<byte[]>();
	  
	  sortSpecs.addAll(bySpec);
	  sortSpecs.addAll(limitSpec);
	  sortSpecs.addAll(getSpec);
	  sortSpecs.addAll(descSpec);
	  sortSpecs.addAll(alphaSpec);
	  sortSpecs.addAll(storeSpec);

	  byte[][] sortCmd = new byte[sortSpecs.size() + 1][];
	  sortCmd[0] = keyBytes;
	  for (int i = 0; i < sortSpecs.size(); i++) {
	    sortCmd[i+1] = sortSpecs.get(i);//.getBytes();
	  }
  	  return sortCmd;
	}

	
	public List<byte[]> exec() throws IllegalStateException, RedisException {
//	    System.out.format("sort spec: [%S %S %S %S %S %S]\n", bySpec, limitSpec, getSpec, descSpec, alphaSpec, storeSpec);
		List<byte[]> res = null;
		if(!stores)
			res = execSort(buildSortCmd());
		else 
			res = execSortStore(buildSortCmd());
		return res;
	}
	public Future<List<byte[]>> execAsync() {
//		System.out.format("sort spec: [%S %S %S %S %S %S]\n", bySpec, limitSpec, getSpec, descSpec, alphaSpec, storeSpec);
		Future<List<byte[]>>  res = null;
		if(!stores)
			res = execAsyncSort (buildSortCmd());
		else 
			res = execAsyncSortStore(buildSortCmd());
		return res;
	}
	protected abstract List<byte[]> execSort (byte[]... fullSortCmd) throws IllegalStateException, RedisException;
	protected abstract List<byte[]> execSortStore (byte[]... fullSortCmd) throws IllegalStateException, RedisException;
	protected abstract Future<List<byte[]>> execAsyncSort (byte[]... fullSortCmd);
	protected abstract Future<List<byte[]>> execAsyncSortStore (byte[]... fullSortCmd);
}