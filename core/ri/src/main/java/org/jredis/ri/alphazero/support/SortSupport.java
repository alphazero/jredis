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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
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
	    
	private List<String> alphaSpec = new ArrayList<String>();
	private List<String> descSpec = new ArrayList<String>();
	private List<String> getSpec = new ArrayList<String>();
	private List<String> bySpec = new ArrayList<String>();
	private List<String> limitSpec = new ArrayList<String>();
	private List<String> storeSpec = new ArrayList<String>();
	
	public Sort ALPHA() {
	  String alphaSpecName = Command.Option.ALPHA.name();
	  alphaSpec.add(alphaSpecName);
	  return this; 
	}
	
	public Sort DESC() {
      String sortSpecName = Command.Option.DESC.name();
      descSpec.add(sortSpecName);

	  return this;
	}
	
	public Sort BY(String pattern) {
	   String bySpecName = Command.Option.BY.name();
	   bySpec.add(bySpecName);
	   bySpec.add(pattern);
	   
	   return this; 
	 }
	
	public Sort GET(String pattern) {
      String getSpecName = Command.Option.GET.name();
      getSpec.add(getSpecName);
      getSpec.add(pattern);
      
	  return this;
	}
	
	public Sort LIMIT(long from, long count) {
		if(from < 0) {
		  throw new ClientRuntimeException("from in LIMIT clause: " + from);
		}
		
		if(count <= 0) {
		  throw new ClientRuntimeException("count in LIMIT clause: " + from);
		}
		
		String limitSpecName = Command.Option.LIMIT.name();
	    String fromString = new Long(from).toString();
	    String countString = new Long(count).toString();
	    
	    limitSpec.add(limitSpecName);
	    limitSpec.add(fromString);
	    limitSpec.add(countString);
		
		return this;
	}
	
	/** Store the sort results in another key */
	public Sort STORE (String destKey) {
		Assert.notNull(destKey, "deskKey is null", ClientRuntimeException.class);
		
		String storeSpecName = Command.Option.STORE.name();
		storeSpec.add(storeSpecName);
		storeSpec.add(destKey);
		
		stores = true;
		
		return this;
	}
	
	private final byte[][] buildSortCmd() {
	  ArrayList<String> sortSpecs = new ArrayList<String>();
	  
	  sortSpecs.addAll(bySpec);
	  sortSpecs.addAll(limitSpec);
	  sortSpecs.addAll(getSpec);
	  sortSpecs.addAll(descSpec);
	  sortSpecs.addAll(alphaSpec);
	  sortSpecs.addAll(storeSpec);

	  byte[][] sortCmd = new byte[sortSpecs.size() + 1][];
	  sortCmd[0] = keyBytes;
	  for (int i = 0; i < sortSpecs.size(); i++) {
	    sortCmd[i+1] = sortSpecs.get(i).getBytes();
	  }
  	  return sortCmd;
	}

	
	public List<byte[]> exec() throws IllegalStateException, RedisException {
	    System.out.format("sort spec: [%S %S %S %S %S %S]\n", bySpec, limitSpec, getSpec, descSpec, alphaSpec, storeSpec);
		List<byte[]> res = null;
		if(!stores)
			res = execSort(buildSortCmd());
		else 
			res = execSortStore(buildSortCmd());
		return res;
	}
	public Future<List<byte[]>> execAsynch() {
		System.out.format("sort spec: [%S %S %S %S %S %S]\n", bySpec, limitSpec, getSpec, descSpec, alphaSpec, storeSpec);
		Future<List<byte[]>>  res = null;
		if(!stores)
			res = execAsynchSort (buildSortCmd());
		else 
			res = execAsynchSortStore(buildSortCmd());
		return res;
	}
	protected abstract List<byte[]> execSort (byte[]... fullSortCmd) throws IllegalStateException, RedisException;
	protected abstract List<byte[]> execSortStore (byte[]... fullSortCmd) throws IllegalStateException, RedisException;
	protected abstract Future<List<byte[]>> execAsynchSort (byte[]... fullSortCmd);
	protected abstract Future<List<byte[]>> execAsynchSortStore (byte[]... fullSortCmd);
}