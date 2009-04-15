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

package org.jredis.connector;

import org.jredis.ClientRuntimeException;

public interface ConnectionFactory {
	
	/**
	 * Gets a connection to a redis server, per default connection specifications.  This may
	 * or may not entail the creation of a new connection.  The details of the behavior is
	 * implementation specific. 
	 * 
	 * <p>Simple implementations may simply return the same (singleton) instance.  This is just
	 * to decouple the details of establishing connections from the consumer of connection's services.
	 * 
	 * @return an active connection.
	 * 
	 * @see ConnectionSpec
	 */
	public Connection getConnection () throws ClientRuntimeException;
	
	/**
	 * Gets a connection to a redis server using the specified connection attributes.  
	 * 
	 * @param connectionSpecification
	 * @return
	 * @throws JRedisException
	 * 
	 * @see {@link ConnectionFactory#getConnection()}
	 */
	public Connection getConnection (ConnectionSpec connectionSpecification) throws ClientRuntimeException;
}
