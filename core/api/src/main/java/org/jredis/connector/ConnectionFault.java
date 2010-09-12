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

package org.jredis.connector;

import java.net.SocketException;
import org.jredis._specification;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Sep 12, 2010
 * 
 */

public class ConnectionFault extends ConnectionException {
	/**  */
	private static final long serialVersionUID = _specification.Version.major;

	/**
	 * @param string
	 * @param e
	 */
	public ConnectionFault(String msg, SocketException e) {
		super(msg, e);
	}
	public ConnectionFault(String msg) {
		super(msg);
	}
}
