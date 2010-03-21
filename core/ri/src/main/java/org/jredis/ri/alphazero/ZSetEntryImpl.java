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

import org.jredis.ZSetEntry;
import org.jredis.ri.alphazero.support.DefaultCodec;

/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Mar 20, 2010
 * @since   alpha.0
 * 
 */

class ZSetEntryImpl extends Pair<byte[], byte[]> implements ZSetEntry {

    public ZSetEntryImpl (byte[] valueBytes, byte[] scoreBytes) {
    	super(valueBytes, scoreBytes);
    }
	/* (non-Javadoc) @see org.jredis.ZSetEntry#getScore() */
	public double getScore () { return DefaultCodec.toDouble(t2); }

	/* (non-Javadoc) @see org.jredis.ZSetEntry#getValue() */
	public byte[] getValue () { return t1;}
}
