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

package org.jredis.ri.alphazero.support;

import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 02, 2009
 * @since   alpha.0
 * 
 */
public class Log {
	public static org.apache.commons.logging.Log logger = LogFactory.getLog("JREDIS");
	public enum Category { INFO, DEBUG, ERROR, PROBLEM, BUG }

	// the various 'just FYI's ...
	public static final void log (String msg)   { log (msg, (Object[])null); }
	public static final void log (String format, Object...args)   { 
		logger.info(String.format(format, args)); 
	}
	public static final void debug (String msg) { debug(msg, (Object[])null); }
	public static final void debug (String format, Object...args) { 
		logger.debug(String.format(format, args)); 
	}
	
	// the various 'error! run for covers', ... 
	public static final void error (String msg)   { _error (Category.ERROR, msg); }
	public static final void error (String msg, Object...args) { _error (Category.ERROR, msg, args); }
	
	public static final void problem (String msg) { _error (Category.PROBLEM, msg); }
	public static final void problem (String msg, Object...args) { _error (Category.PROBLEM, msg, args); }
	
	public static final void bug (String msg)     { _error (Category.BUG, msg); }
	public static final void bug (String msg, Object...args) { _error (Category.BUG, msg, args); }
	
	private static final void _error (Category cat, String msg, Object...args) {
		msg = String.format(msg, args);
		if(cat.equals(Category.ERROR))
			logger.error(String.format("%s", msg));
		else
			logger.error(String.format("%s: %s", cat, msg));
	}
}