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

package org.jredis.resource;



/**
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 16, 2009
 * @since   alpha.0
 * 
 */

public interface Resource <T> {
	
	/**
	 * @return the resource type specific interface.
	 */
	public T getInterface();
	
	/**
	 * Sets the resource context.
	 * 
	 * @see Context<T>
	 * @param context the context for this resource.
	 * @throws ResourceException if the resource context provided is either insufficient, or, 
	 * if the new context (regardless of its utility) can not be used.  (For example, it already
	 * has a context and re-setting of contexts is not supported or permissible.)
	 */
	public void setContext (Context context) throws ResourceException;
	
	/**
	 * @see Context<T>
	 * @return the context for this resource. 
	 * @throws ResourceException if the resource for whatever reason can not (or will not)
	 * return a reference to its context.  (Security considerations, for example.)
	 */
	public Context getContext () throws ResourceException;
}
