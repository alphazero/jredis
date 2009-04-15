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

package org.jredis.ri.alphazero.semantics;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.jredis.JRedis;
import org.jredis.RedisException;
import org.jredis.RedisType;
import org.jredis.semantics.Codec;
import org.jredis.semantics.SemanticJRedis;
import org.jredis.semantics.SemanticSort;

/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 13, 2009
 * @since   alpha.0
 * 
 */

public class SemanticJRedisSupport <T> implements SemanticJRedis <T> {

	final JRedis 		jredis;
	final Codec<T>		codec;
	public SemanticJRedisSupport(JRedis jredis, Codec<T> codec) {
		this.jredis = jredis;
		this.codec = codec;
	}
	@Override
	public JRedis auth(String authorization) throws RedisException { return jredis.auth(authorization);}

	@Override
	public void bgsave() throws RedisException { jredis.bgsave(); }

	@Override
	public long dbsize() throws RedisException { return jredis.dbsize(); }

	@Override
	public long decr(String key) throws RedisException { return jredis.decr(key);}

	@Override
	public long decrby(String key, int delta) throws RedisException { return jredis.decrby(key, delta);}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#del(java.lang.String)
	 */
	@Override
	public boolean del(String key) throws RedisException {
		return false;
	}

	@Override
	public boolean exists(String key) throws RedisException {

		return false;
	}

	@Override
	public boolean expire(String key, int ttlseconds) throws RedisException {

		return false;
	}

	@Override
	public JRedis flushall() throws RedisException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JRedis flushdb() throws RedisException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T get(String key) throws RedisException {
		return codec.decode(jredis.get(key));
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#incr(java.lang.String)
	 */
	@Override
	public long incr(String key) throws RedisException {
		// TODO Auto-generated method stub
		return 0;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#incrby(java.lang.String, int)
	 */
	@Override
	public long incrby(String key, int delta) throws RedisException {
		// TODO Auto-generated method stub
		return 0;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#info()
	 */
	@Override
	public Map<String, String> info() throws RedisException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#keys()
	 */
	@Override
	public List<String> keys() throws RedisException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#keys(java.lang.String)
	 */
	@Override
	public List<String> keys(String pattern) throws RedisException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#lastsave()
	 */
	@Override
	public long lastsave() throws RedisException {
		// TODO Auto-generated method stub
		return 0;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#lindex(java.lang.String, int)
	 */
	@Override
	public T lindex(String listkey, int index) throws RedisException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#llen(java.lang.String)
	 */
	@Override
	public long llen(String listkey) throws RedisException {
		// TODO Auto-generated method stub
		return 0;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#lpop(java.lang.String)
	 */
	@Override
	public T lpop(String listKey) throws RedisException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#lpush(java.lang.String, java.lang.Object)
	 */
	@Override
	public void lpush(String listkey, T value) throws RedisException {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#lrange(java.lang.String, int, int)
	 */
	@Override
	public List<T> lrange(String listkey, int from, int to)
			throws RedisException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#lrem(java.lang.String, java.lang.Object, int)
	 */
	@Override
	public long lrem(String listKey, T value, int count) throws RedisException {
		// TODO Auto-generated method stub
		return 0;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#lset(java.lang.String, int, java.lang.Object)
	 */
	@Override
	public void lset(String key, int index, T value) throws RedisException {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#ltrim(java.lang.String, int, int)
	 */
	@Override
	public void ltrim(String listkey, int keepFrom, int keepTo)
			throws RedisException {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#mget(java.lang.String, java.lang.String[])
	 */
	@Override
	public List<T> mget(String key, String... moreKeys) throws RedisException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#move(java.lang.String, int)
	 */
	@Override
	public boolean move(String key, int dbIndex) throws RedisException {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#ping()
	 */
	@Override
	public JRedis ping() throws RedisException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#quit()
	 */
	@Override
	public void quit() {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#randomkey()
	 */
	@Override
	public String randomkey() throws RedisException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#rename(java.lang.String, java.lang.String)
	 */
	@Override
	public String rename(String oldkey, String newkey) throws RedisException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#renamenx(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean renamenx(String oldkey, String brandnewkey)
			throws RedisException {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#rpop(java.lang.String)
	 */
	@Override
	public T rpop(String listKey) throws RedisException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#rpush(java.lang.String, java.lang.Object)
	 */
	@Override
	public void rpush(String listkey, T value) throws RedisException {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#sadd(java.lang.String, java.lang.Object)
	 */
	@Override
	public boolean sadd(String setkey, T member) throws RedisException {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#save()
	 */
	@Override
	public void save() throws RedisException {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#scard(java.lang.String)
	 */
	@Override
	public long scard(String setKey) throws RedisException {
		// TODO Auto-generated method stub
		return 0;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#select(int)
	 */
	@Override
	public JRedis select(int index) throws RedisException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#set(java.lang.String, java.lang.Object)
	 */
	@Override
	public void set(String key, T value) throws RedisException {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#setnx(java.lang.String, java.lang.Object)
	 */
	@Override
	public boolean setnx(String key, T value) throws RedisException {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#shutdown()
	 */
	@Override
	public void shutdown() throws RedisException {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#sinter(java.lang.String, java.lang.String[])
	 */
	@Override
	public List<T> sinter(String set1, String... sets) throws RedisException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#sinterstore(java.lang.String, java.lang.String[])
	 */
	@Override
	public void sinterstore(String destSetKey, String... sets)
			throws RedisException {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#sismember(java.lang.String, java.lang.Object)
	 */
	@Override
	public boolean sismember(String setKey, T member) throws RedisException {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#smembers(java.lang.String)
	 */
	@Override
	public List<T> smembers(String setkey) throws RedisException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#sort(java.lang.String)
	 */
	@Override
	public SemanticSort<T> sort(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#srem(java.lang.String, java.lang.Object)
	 */
	@Override
	public boolean srem(String setKey, T member) throws RedisException {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.SemanticJRedis#type(java.lang.String)
	 */
	@Override
	public RedisType type(String key) throws RedisException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.CodecManager#getCodec(java.lang.Class)
	 */
	@Override
	public <T> Codec<T> getCodec(Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.jredis.semantics.CodecManager#register(org.jredis.semantics.Codec, java.lang.Class)
	 */
	@Override
	public <T> boolean register(Codec<T> code, Class<T> type) {
		// TODO Auto-generated method stub
		return false;
	}

}
