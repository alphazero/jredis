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

package org.jredis;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;

/**
 * Generic Event class for JRedis and sub-modules.
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Mar 29, 2010
 * @since   alpha.0
 * 
 * @param <SRC>
 * @param <ETYPE>
 * @param <INFO>
 */
public class Event <SRC, ETYPE, INFO> implements Serializable{
	/**  */
    private static final long serialVersionUID = 1L;
    
	/**  */
	private final ETYPE type;
	/**  */
	private transient WeakReference<SRC> srcRef;
	/**  */
	private final INFO info;
	
	/**
	 * @param src
	 * @param eType
	 * @param info
	 */
	protected Event(SRC src, ETYPE eType, INFO info){
		this.type = eType;
		this.srcRef = new WeakReference<SRC>(src);
		this.info = info;
	}
	/**
	 * @param src
	 * @param eType
	 */
	protected Event(SRC src, ETYPE eType){
		this(src, eType, null);
	}
	/**
	 * @return
	 */
	public ETYPE getType () {
    	return type;
    }
	/**
	 * @return
	 */
	public SRC getSource () {
    	return srcRef.get();
    }
	/**
	 * @return
	 */
	public INFO getInfo () {
    	return info;
    }

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeObject(srcRef.get());
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		srcRef = new WeakReference<SRC>((SRC)in.readObject());
	}
}