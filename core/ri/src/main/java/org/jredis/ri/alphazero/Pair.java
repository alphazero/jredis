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

package org.jredis.ri.alphazero;

/**
 * Generic immutable 2-tuple data struct.
 *  
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Mar 20, 2010
 * @since   alpha.0
 * 
 */

class Pair<T1, T2> {
	public final T1 t1;
	public final T2 t2;
	public Pair(T1 t1, T2 t2){
		this.t1 = t1;
		this.t2 = t2;
	}
}
