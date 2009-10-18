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

package org.jredis.examples;

import org.jredis.JRedisFuture;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.JRedisPipeline;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;

/**
 * Extension of {@link UsingJRedisFuture} with a {@link JRedisPipeline} as the 
 * provider.
 * 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Oct 11, 2009
 * @since   alpha.0
 * @see UsingJRedisFuture
 * 
 */

public class UsingJRedisPipeline extends UsingJRedisFuture {

	public static void main (String[] args) {
		int database = 11;
		ConnectionSpec connectionSpec = DefaultConnectionSpec.newSpec("localhost", 6379, database, "jredis".getBytes());
		
	    new UsingJRedisPipeline (connectionSpec);
    }
	/**
     * @param connectionSpec
     */
    public UsingJRedisPipeline (ConnectionSpec connectionSpec) {
	    super(connectionSpec);
    }

	/* (non-Javadoc)
	 * @see org.jredis.examples.UsingJRedisFuture#getProviderInstance(org.jredis.connector.ConnectionSpec)
	 */
	@Override
	protected JRedisFuture getProviderInstance (ConnectionSpec connectionSpec) {
		return new JRedisPipeline(connectionSpec);
	}
}
