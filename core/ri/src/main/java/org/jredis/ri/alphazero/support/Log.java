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

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 02, 2009-2011
 * @since   alpha.0
 * 
 */
public class Log {

	public final static Logger logger = Logger.getLogger("JRedis");
	static {
		logger.setUseParentHandlers(false);
		final Handler handler = new Log.Handler();
		final Formatter formatter = new Log.Formatter();
		handler.trySetFormatter(formatter);
		logger.addHandler(handler);
	}
	public enum Category { INFO, DEBUG, ERROR, PROBLEM, BUG }

	// the various 'just FYI's ...
	public static final void log (String msg)   { log (msg, (Object[])null); }
	public static final void log (String format, Object...args)   { 
		logger.info(String.format(format, args)); 
	}
	public static final void debug (String msg) { debug(msg, (Object[])null); }
	public static final void debug (String format, Object...args) { 
		logger.log(Level.FINE, String.format(format, args)); 
	}

	// the various 'error! run for covers', ... 
	public static final void error (String msg)   { _error (Category.ERROR, msg); }
	public static final void error (String msg, Throwable t)   { logger.log(Level.SEVERE, msg, t); }
	public static final void error (String msg, Object...args) { _error (Category.ERROR, msg, args); }

	public static final void problem (String msg) { _error (Category.PROBLEM, msg); }
	public static final void problem (String msg, Object...args) { _error (Category.PROBLEM, msg, args); }

	public static final void bug (String msg)     { _error (Category.BUG, msg); }
	public static final void bug (String msg, Object...args) { _error (Category.BUG, msg, args); }

	private static final void _error (final Category cat, final String msg, final Object...args) {
		final String _msg = String.format(msg, args);
		if(cat.equals(Category.ERROR))
			logger.severe(String.format("%s", _msg));
		else
			logger.log(Level.WARNING, String.format("%s: %s", cat, _msg));
	}
	
	// ------------------------------------------------------------------------
	// Inner Classes | formatter and handler
	// ------------------------------------------------------------------------
	/**
	 * Default handler for JRedis.Log
	 * @author alphazero
	 */
	public static class Handler extends java.util.logging.Handler {
		/**
		 * Try and set the formatter -- may not be possible if
		 * run in containers, etc. due to security checks.
		 * @param fmt 
		 */
		private java.util.logging.Formatter formatter;
		final void trySetFormatter(Formatter fmt){
			try {
				super.setFormatter(fmt);
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				formatter = getFormatter();
			}
		}
		@Override final
		public void publish(LogRecord record) {
			System.err.print(formatter.format(record));
			flush();
		}
		@Override final
		public void flush() {
			System.err.flush();
		}
		@Override final
		public void close() throws SecurityException {
			flush();
		}
	}
	/**
	 * simple formatter for a single line log out.
	 * @author alphazero
	 */
	public static class Formatter extends java.util.logging.Formatter {
		final String LINESEP = System.getProperty("line.separator");
		@SuppressWarnings("boxing")
		@Override final
		public String format(LogRecord record) {
			// TODO: clean up the mess above and fix this.
			final Level level = record.getLevel();
			final String logger = record.getLoggerName();
			final String msg = record.getMessage();
			final Object[] msgparams = record.getParameters();
			final int tid = record.getThreadID();
			final long time = record.getMillis();
			
			String _msg = null;
			if(msgparams != null && msgparams.length > 0){
				_msg = String.format(msg, msgparams);
			}
			else {
				_msg = msg;
			}
			
			final Date d = new Date(time);
			return String.format("%014d %s %s[tid:%d] <%s>: %s%s", time, d, logger, tid, level.getLocalizedName(), _msg, LINESEP);
		}
	}
}
