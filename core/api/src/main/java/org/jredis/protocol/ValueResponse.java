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

package org.jredis.protocol;


/**
 * Formally, redis only returns "integers" as values, but in fact, an operation such as
 * {@link Command#RANDOMKEY} will return a key in a single line reply, and not a bulk reply.
 * (As of 04/02/09.  Ex randomkey response => +woof )
 * 
 * <p>This class deals with all responses that are a single line responses distinct from 'status'
 * replies.  For now, this means the result is either a UTF-8 String <i>key</i> or <i>number</i>.
 * 
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 * 
 */
public interface ValueResponse extends Response {
	/**
	 * Redis number values are "64bit signed integers".  
	 * @return
	 * @throws IllegalStateException
	 */
	public long getLongValue () throws IllegalStateException;
	
	/**
	 * Its gone.  Everything is 64 bit signed integer now.
	 * This is deprecated but kept in the initial release just in case things change on the Redis side.
	 * @return
	 * @throws IllegalStateException
	 */
//	@Deprecated
//	public int getIntValue () throws IllegalStateException;
	
	/**
	 * @return
	 * @throws IllegalStateException
	 */
	public String getStringValue () throws IllegalStateException;
	/**
	 * @return
	 * @throws IllegalStateException
	 */
	public boolean getBooleanValue () throws IllegalStateException;
}
