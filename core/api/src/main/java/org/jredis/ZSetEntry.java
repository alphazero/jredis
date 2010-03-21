/*
 *   Copyright 2010 Joubin Houshyar
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
 * An entry in a Redis "sorted set" and returned by a subset Z* commands.  
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Mar 20, 2010
 * @since   alpha.0
 * 
 */

public interface ZSetEntry{
	/**  @return the value of this entry in a Redis sorted set */
	byte[] getValue();
	
	/** @return the score associated with the value */
	double getScore();
}
