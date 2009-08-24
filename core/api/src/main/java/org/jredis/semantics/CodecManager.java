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

package org.jredis.semantics;

import org.jredis.Codec;
import org.jredis.JRedis;

/**
 * A CodecManager maintains a mapping of Java types to {@link Codec} instances
 * and can be used by a {@link JRedis} implementation to support the optional semantic
 * methods.
 * 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 14, 2009
 * @since   alpha.0
 * 
 */

public interface CodecManager {
	public <T> Codec<T>		getCodec(Class<T> type);
	public <T>	boolean 	register (Codec<T> code, Class<T> type);
}
