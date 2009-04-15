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



/**
 * Redis commands, verbatim. Each member of the Command enum maps to a 
 * corresponding (protocol level) command in Redis.  
 * 
 * <p><b>specification</b> <code>Redis 0.0.8</code>
 * 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 * 
 */
public enum Command {
	
	// security
	AUTH,
	
	// connection handling
	PING, QUIT, 

	// String values operations
	SET, GET, MGET, SETNX, INCR, INCRBY,  DECR , DECRBY, EXISTS , DEL, TYPE ,

	// "Commands operating on the key space"
	KEYS, RANDOMKEY, RENAME, RENAMENX, DBSIZE, EXPIRE,
	
	// keys operating on lists
	RPUSH, LPUSH, LLEN, LRANGE, LTRIM, LINDEX, LSET, LREM, LPOP, RPOP,
	
	// keys operating on sets
	SADD, SREM, SCARD, SISMEMBER, SINTER, SINTERSTORE, SMEMBERS,
	
	// "Multiple databases handling commands"
	SELECT, FLUSHDB, FLUSHALL, MOVE,
	
	// Sorting
	SORT, 
	
	// Persistence control commands
	SAVE, BGSAVE, LASTSAVE, SHUTDOWN,
	
	// Remote server control commands
	INFO;
	
	/** semantic sugar */
	public final String code;
	public final byte[] bytes;
	public final int length;
	public final int arg_cnt;
	Command () { 
		this.code = this.name(); 
		this.bytes = code.getBytes();
		this.length = code.length();
		this.arg_cnt = -1; // to raise exception -- make sure we don't miss any
	}
}