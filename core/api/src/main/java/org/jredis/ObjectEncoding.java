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
 * Internal Redis object encoding schemes.
 *   
 * @see ObjectInfo#getEncoding()
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Mar 17, 2010
 * @since   alpha.0 | Redis 1.3.5
 *
 */
public enum ObjectEncoding {
	/** Redis 'raw'  */
	RAW (),
	/** Redis 'int'  */
	INT (),
	/** Redis 'zipmap'  */
	ZIPMAP(),
	/** Redis 'hashtable'  */
	HASHTABLE,
	/** Redis 'embstr'  */
	EMBSTR,
}