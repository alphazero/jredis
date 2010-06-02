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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO: deprecate this and use a standard logger (jdk or log4j ...)
 * 
 * yes -- yet another logger.  This is to keep down the dependencies
 * you can add your own later ... 
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 02, 2009
 * @since   alpha.0
 * 
 */
public class Log {
	public enum Category { INFO, ERROR, PROBLEM, BUG }

	public static final void log (String msg)   { _loginfo (msg); }
	public static final void error (String msg)   { _log (Category.ERROR, msg); }
	public static final void problem (String msg) { _log (Category.PROBLEM, msg); }
	public static final void bug (String msg)     { _log (Category.BUG, msg); }

  public static Logger logger = Logger.getLogger("org.jredis.JRedis");

	public static final void log (String format, Object...args) {
		_loginfo(format, args);
	}
	private static final void _log (Category cat, String msg) {
	  logger.log(Level.WARNING, msg);
	}
	private static final void _loginfo (String format, Object...args) {
	  logger.log(Level.INFO, String.format(format, args));
	}
//	private static final String format(String format, Object...args){
//		Formatter formatter = new Formatter();
//		formatter.format(format, args);
//		return formatter.toString();
//	}
}
