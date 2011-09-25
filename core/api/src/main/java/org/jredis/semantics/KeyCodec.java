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

// TODO: this is the wrong package -- we'll need something either under
// 

package org.jredis.semantics;

import org.jredis.Codec;

/**
 * Encoding and decoding keys is a significant (but not major) performance bottleneck.   
 * <p>
 * A KeyCodec is a {@link String} {@link Codec} that is tasked
 * with the optimal performance of encoding and decoding String keys
 * for use by connector implementations that support such extended
 * mechanisms.
 * <p>
 * This interface is projected to be implemented by a key caching
 * mechanism suitable for employment in usage scenarios where a finite
 * set of keys is repeatedly employed to accomplish domain specific tasks.
 * <p>
 * For example, a common Redis pattern is the use of atomic counters to
 * increment IDs.  Whenever this pattern is used, the key for the ID(s)
 * will be <i>repeatedly</i> encoded and decoded by a non-optimized client
 * which can not make indiscriminate decisions regarding such optimizations.
 * <p>
 * A key cache, implementing {@link KeyCodec}, for example, can be used to indicate to
 * the client that for keys, the default codec (which is basically delegating to
 * {@link String}'s {@link String#getBytes()} and {@link String#String(byte[], int, int)})
 * should not be used and that the client should use the object implementing this
 * to accomplish the same task.   
 * <p>
 * This is just a marker interface beyond that.
 * 
 *  
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 15, 2009
 * @since   alpha.0
 * 
 */

public interface KeyCodec<K extends Object> extends Codec<K> {/* nop */}
