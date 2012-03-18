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

package org.jredis.protocol;

import org.jredis.Redis;

/**
 * Redis commands, (~) verbatim. Each member of the Command enum maps to a 
 * corresponding (protocol level) command in Redis.  Commands with optional
 * semantics are further distinguished: optional variants include 
 * embedded '$').
 * 
 * <p><b>specification</b> <code>Redis 1.00</code>
 * 
 * @author  Joubin (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09 (Redis 1.n)
 * @version alpha.0, 09/17/10 (Redis 2.n)
 * @since   alpha.0
 * 
 */
@Redis(versions={"1.n", "2.0"})
public enum Command {
	// security
	AUTH 		(RequestType.KEY, 			ResponseType.STATUS),
	
	// connection handling
	PING 		(RequestType.NO_ARG, 		ResponseType.STATUS), 
	QUIT 		(RequestType.NO_ARG, 		ResponseType.VIRTUAL), 
	CONN_FLUSH 	(RequestType.NO_ARG, 		ResponseType.NOP), 

	// String values operations
	SET 		(RequestType.KEY_VALUE, 	ResponseType.STATUS), 
	GET 		(RequestType.KEY, 			ResponseType.BULK), 
	GETSET		(RequestType.KEY_VALUE, 	ResponseType.BULK), 
	MGET		(RequestType.MULTI_KEY, 	ResponseType.MULTI_BULK), 
	SETNX		(RequestType.KEY_VALUE, 	ResponseType.BOOLEAN),
	MSET		(RequestType.BULK_SET, 		ResponseType.STATUS), 
	MSETNX		(RequestType.BULK_SET, 		ResponseType.BOOLEAN), 
	INCR		(RequestType.KEY, 			ResponseType.NUMBER), 
	INCRBY		(RequestType.KEY_NUM,		ResponseType.NUMBER),  
	DECR		(RequestType.KEY, 			ResponseType.NUMBER), 
	DECRBY		(RequestType.KEY_NUM,		ResponseType.NUMBER),  
	EXISTS		(RequestType.KEY, 			ResponseType.BOOLEAN), 
	DEL			(RequestType.MULTI_KEY, 	ResponseType.NUMBER), 
	TYPE		(RequestType.KEY, 			ResponseType.STRING),
	SUBSTR		(RequestType.KEY_NUM_NUM,	ResponseType.BULK),
	APPEND		(RequestType.KEY_VALUE, 	ResponseType.NUMBER),

	// "Commands operating on the key space"
	KEYS		(RequestType.KEY, 			ResponseType.MULTI_BULK), 
	KEYSTOLIST	(RequestType.KEY_KEY, 		ResponseType.NUMBER), 
	RANDOMKEY	(RequestType.NO_ARG,		ResponseType.BULK),
	RENAME		(RequestType.KEY_KEY, 		ResponseType.STATUS), 
	RENAMENX	(RequestType.KEY_KEY, 		ResponseType.BOOLEAN), 
	DBSIZE		(RequestType.NO_ARG,		ResponseType.NUMBER),
	EXPIRE		(RequestType.KEY_NUM,		ResponseType.BOOLEAN), 
	EXPIREAT	(RequestType.KEY_NUM,		ResponseType.BOOLEAN), 
	TTL			(RequestType.KEY,			ResponseType.NUMBER),
	
	// Commands operating on lists
	RPUSH		(RequestType.KEY_VALUE,		ResponseType.NUMBER), 
	RPUSHX	(RequestType.KEY_VALUE,		ResponseType.NUMBER), 
	LPUSH		(RequestType.KEY_VALUE,		ResponseType.NUMBER),
	LPUSHX		(RequestType.KEY_VALUE,		ResponseType.NUMBER),
	LINSERT	(RequestType.BULK_SET,		ResponseType.NUMBER),
	LLEN		(RequestType.KEY,			ResponseType.NUMBER), 
	LRANGE		(RequestType.KEY_NUM_NUM,	ResponseType.MULTI_BULK), 
	LTRIM		(RequestType.KEY_NUM_NUM,	ResponseType.STATUS),
	LINDEX		(RequestType.KEY_NUM,		ResponseType.BULK), 
	LSET		(RequestType.KEY_IDX_VALUE,	ResponseType.STATUS), 
	LREM		(RequestType.KEY_CNT_VALUE,	ResponseType.NUMBER),
	LPOP		(RequestType.KEY,			ResponseType.BULK), 
	RPOP		(RequestType.KEY,			ResponseType.BULK),
	RPOPLPUSH	(RequestType.KEY_KEY,		ResponseType.BULK),
	
	// Commands operating on sets
	SADD		(RequestType.KEY_VALUE,		ResponseType.BOOLEAN), 
	SREM		(RequestType.KEY_VALUE,		ResponseType.BOOLEAN), 
	SCARD		(RequestType.KEY,			ResponseType.NUMBER), 
	SISMEMBER	(RequestType.KEY_VALUE,		ResponseType.BOOLEAN), 
	SINTER		(RequestType.MULTI_KEY,		ResponseType.MULTI_BULK), 
	SINTERSTORE (RequestType.MULTI_KEY,		ResponseType.STATUS),
	SUNION		(RequestType.MULTI_KEY,		ResponseType.MULTI_BULK), 
	SUNIONSTORE (RequestType.MULTI_KEY,		ResponseType.STATUS), 
	SDIFF		(RequestType.MULTI_KEY,		ResponseType.MULTI_BULK), 
	SDIFFSTORE  (RequestType.MULTI_KEY,		ResponseType.STATUS),
	SMEMBERS	(RequestType.KEY,			ResponseType.MULTI_BULK), 
	SMOVE		(RequestType.KEY_KEY_VALUE,	ResponseType.BOOLEAN),
	SRANDMEMBER (RequestType.KEY,  			ResponseType.BULK),
	SPOP     	(RequestType.KEY,        	ResponseType.BULK),
	// Commands operating on sorted sets
	ZADD		(RequestType.KEY_IDX_VALUE,	ResponseType.BOOLEAN), 
	ZREM		(RequestType.KEY_VALUE,		ResponseType.BOOLEAN),
	ZCARD		(RequestType.KEY,			ResponseType.NUMBER), 
	ZSCORE		(RequestType.KEY_VALUE,		ResponseType.BULK),
	ZRANK		(RequestType.KEY_VALUE,		ResponseType.NUMBER),
	ZREVRANK	(RequestType.KEY_VALUE,		ResponseType.NUMBER),
	ZRANGE			(RequestType.KEY_NUM_NUM,	ResponseType.MULTI_BULK),
	/** ZRANGE with OPTIONS  */
	ZRANGE$OPTS		(RequestType.KEY_NUM_NUM_OPTS,	ResponseType.MULTI_BULK),
	ZREVRANGE		(RequestType.KEY_NUM_NUM,		ResponseType.MULTI_BULK),
	/** ZREVRANGE with OPTIONS  */
	ZREVRANGE$OPTS	(RequestType.KEY_NUM_NUM_OPTS,	ResponseType.MULTI_BULK),
	ZINCRBY		(RequestType.KEY_IDX_VALUE, ResponseType.BULK),
	ZRANGEBYSCORE		(RequestType.KEY_NUM_NUM,	ResponseType.MULTI_BULK),
	ZRANGEBYSCORE$OPTS		(RequestType.KEY_NUM_NUM_OPTS,	ResponseType.MULTI_BULK),
	ZREMRANGEBYSCORE	(RequestType.KEY_NUM_NUM,	ResponseType.NUMBER),
	ZREMRANGEBYRANK	(RequestType.KEY_NUM_NUM,	ResponseType.NUMBER),
	ZCOUNT		(RequestType.KEY_NUM_NUM, ResponseType.NUMBER),
		
	// Commands operating on bit sets
	SETBIT		(RequestType.KEY_IDX_VALUE, ResponseType.NUMBER),
	GETBIT		(RequestType.KEY_NUM, ResponseType.NUMBER),
	
	// Commands operating on hashes
	HSET 		(RequestType.KEY_KEY_VALUE, ResponseType.BOOLEAN),
	HGET 		(RequestType.KEY_VALUE, 	ResponseType.BULK),
	HEXISTS 	(RequestType.KEY_VALUE, 	ResponseType.BOOLEAN),
	HDEL 		(RequestType.KEY_VALUE, 	ResponseType.BOOLEAN),
	HLEN 		(RequestType.KEY, 			ResponseType.NUMBER),
	HKEYS 		(RequestType.KEY, 			ResponseType.MULTI_BULK),
	HVALS 		(RequestType.KEY, 			ResponseType.MULTI_BULK),
	HGETALL 	(RequestType.KEY, 			ResponseType.MULTI_BULK),
	HINCRBY		(RequestType.KEY_KEY_NUM, ResponseType.NUMBER),
	
	// transactional commands
	MULTI		(RequestType.NO_ARG, 		ResponseType.STATUS),
	EXEC		(RequestType.NO_ARG, 		ResponseType.RESULT_SET), // NEED NEW RESPONSE TYPE
	DISCARD		(RequestType.NO_ARG, 		ResponseType.STATUS),
	
	// "Multiple databases handling commands"
	SELECT		(RequestType.KEY,			ResponseType.STATUS),
	FLUSHDB		(RequestType.NO_ARG,		ResponseType.STATUS), 
	FLUSHALL	(RequestType.NO_ARG,		ResponseType.STATUS),
	MOVE		(RequestType.KEY_NUM,		ResponseType.BOOLEAN),
	
	// Sorting
	SORT		(RequestType.MULTI_KEY,		ResponseType.MULTI_BULK),
	/** SORT...STORE */
	SORT$STORE	(RequestType.MULTI_KEY,		ResponseType.NUMBER),
	
	// Persistence control commands
	SAVE		(RequestType.NO_ARG,		ResponseType.STATUS), 
	BGSAVE		(RequestType.NO_ARG,		ResponseType.STATUS), 
	BGREWRITEAOF(RequestType.NO_ARG,		ResponseType.STRING), 
	LASTSAVE	(RequestType.NO_ARG,		ResponseType.NUMBER),
	SHUTDOWN	(RequestType.NO_ARG, 		ResponseType.VIRTUAL),
	
	// diagnostics commands
	ECHO    	(RequestType.VALUE,     	ResponseType.BULK),
	DEBUG		(RequestType.KEY_KEY, 		ResponseType.STRING), 
	
	// Remote server control commands
	INFO		(RequestType.NO_ARG, 		ResponseType.BULK), 
	MONITOR	    (RequestType.NO_ARG, 		ResponseType.VIRTUAL), // BUG: NOTE: TODO: not virtual ..
	SLAVEOF		(RequestType.KEY_KEY, 		ResponseType.STATUS),
	
	;// -- end --
	
	/** semantic sugar */
	public final String code;
	public final byte[] bytes;
	public final RequestType requestType;
	public final ResponseType responseType;
	private final int flags_bitset;
	
	/** internal */
	static final public String OPTCODE = "$";
	/**
	 * Each enum member directly corresponds to a Redis command, per
	 * specification.  Command semantics is specified by the element
	 * constructor params.
	 * @param reqType the {@link RequestType} of the Command
	 * @param respType the {@link ResponseType} of the Command
	 */
	Command (RequestType reqType, ResponseType respType, Flag... flags) { 
		this.code = this.name();
		
		if(code.indexOf(OPTCODE) > 0) 
			this.bytes = code.substring(0, code.indexOf(OPTCODE)).getBytes();
		else
			this.bytes = code.getBytes();
		
		this.requestType = reqType;
		this.responseType = respType;
		
		if(flags != null && flags.length > 0)
			this.flags_bitset = Flag.bitset(flags);
		else
			this.flags_bitset = Flag.OPAQUE_BITMASK_32;
	}

	/**
	 * Tests if the specified flag is set for the command.
	 * @param flag the flag
	 * @return true if flag is set.
	 * @see Command.Flag
	 */
	final public boolean isSet(Flag flag) {
		return (flags_bitset & flag.bitmask) != Flag.OPAQUE_BITMASK_32;
	}

	// ------------------------------------------------------------------------
	// Inner Types
	// ------------------------------------------------------------------------

	/**
	 * Redis Command Options and modifiers
	 * 
	 * @author  Joubin Houshyar (alphazero@sensesay.net)
	 * @version alpha.0, Mar 20, 2010
	 * @since   alpha.0
	 * 
	 */
	public enum Option {
		WITHSCORES,
		BY,
		LIMIT,
		GET,
		ASC,
		DESC,
		ALPHA,
		STORE;
		/** semantic sugar */
		public final byte[] bytes;
		Option () {
			this.bytes = name().getBytes();
		}
	}
	// TODO: wtf is this doing here?
	/**
	 * Defines (32 bit) flags for {@link Command}
	 *
	 * @author  joubin (alphazero@sensesay.net)
	 * @date    Sep 10, 2010
	 * 
	 */
	public enum Flag {
		TEST,
		FOO,
		BAR,
		;// -- end --
		public final int bitmask;
		private static final int OPAQUE_BITMASK_32 = 0x0000;
		Flag (){
			this.bitmask = (int)Math.pow(2, ordinal());
		}
		static final public int bitset(Flag...flags){
			return bitset(OPAQUE_BITMASK_32, flags);
		}
		static final public int bitset(final int bitsetin, Flag...flags){
			int bitset = bitsetin;
			for(Flag f : flags) bitset = bitset | f.bitmask;
			return bitset;
		}
		public static boolean isSet(int bitset, Flag flag) {
			return (bitset & flag.bitmask) > OPAQUE_BITMASK_32;
		}
		static final public int bitclear(final int bitsetin, Flag...flags){
			int bitset = bitsetin;
			for(Flag f : flags) bitset = bitset ^ f.bitmask;
			return bitset;
		}
	}
    /**
     * Broad Request Type categorization of the Redis Command per the request's
     * argument signature.  These categories are a more differentiated than the
     * Redis specification itself to impart further information about the argument
     * semantics.
	 *
     * @author  Joubin (alphazero@sensesay.net)
     * @version alpha.0, Aug 29, 2009
     * 
     */
    public enum RequestType {
    	/**  */
    	NO_ARG,
    	/**  */
    	KEY,
    	/**  */
    	VALUE,
    	/**  */
    	KEY_KEY,
    	/**  */
    	KEY_NUM,
//    	/**  */
//    	KEY_SPEC,
    	/**  */
    	KEY_NUM_NUM,
    	/** */
    	KEY_NUM_NUM_OPTS,
    	/**  */
    	KEY_VALUE,
    	/**  */
    	KEY_KEY_VALUE,
    	/**  */
    	KEY_IDX_VALUE,
    	/**  */
    	KEY_CNT_VALUE,  // TODO: this should be key value cnt ...
    	/**  */
    	MULTI_KEY,
    	/**  */
    	BULK_SET,
    	
    	KEY_KEY_NUM
    }

    /**
     * Broad Response Type categorization of the Redis Command responses.
     * <p>
     * As with {@link RequestType}, there is further differentiation of the
     * Redis response types to further inform the semantics.
     * <p>
     * Beyond that, there is also linkage between the {@link ResponseType} and
     * its associated {@link Response} interface extension.
     * 
     * @author  Joubin (alphazero@sensesay.net)
     * @version alpha.0, Aug 29, 2009
     * @see Response
     */
    public enum ResponseType {
    	/**  */
    	NOP (StatusResponse.class),
    	/**  */
    	VIRTUAL (StatusResponse.class),
    	/**  */
    	STATUS (StatusResponse.class),
    	/**  */
    	QUEUED (StatusResponse.class),
    	/**  */
    	STRING (ValueResponse.class),
    	/**  */
    	BOOLEAN (ValueResponse.class),
    	/**  */
    	NUMBER (ValueResponse.class),
    	/**  */
    	BULK (BulkResponse.class),
    	/**  */
    	MULTI_BULK (MultiBulkResponse.class),
    	/** */
    	RESULT_SET (Response.class),
    	;
    	public Class<? extends Response> respClass;
    	
    	/**
    	 * For each {@link ResponseType} member, we specify the 
    	 * corresponding {@link Response} extension interface.
    	 * @param respClass
    	 */
    	ResponseType (Class<? extends Response> respClass){
    		this.respClass = respClass;
    	}
    }
}
