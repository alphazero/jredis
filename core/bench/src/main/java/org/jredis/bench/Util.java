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

package org.jredis.bench;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author  joubin 
 */
public class Util {
	
	// ------------------------------------------------------------------------
	// Static properties
	// ------------------------------------------------------------------------
	/** 
	 * Random generator used for Util random functions, 
	 * instantiated using {@link System#currentTimeMillis()} as seed
	 */
	final static Random 		random = new Random(System.currentTimeMillis());
	
	// ------------------------------------------------------------------------
	// Static methods
	// ------------------------------------------------------------------------
	/**
	 * Generates a random {@link String} of length <code>size</code> using the default
	 * encoding scheme.  String content is ASCII characters.
	 * @param size of the string (character count)
	 * @return the generated {@link String}
	 */
	static 
	public String getRandomString (int size) {
		StringBuilder builder = new  StringBuilder(size);
		for(int i = 0; i<size; i++){
			char c = (char) (random.nextInt(126-33) + 33);
			builder.append(c);
		}
		return builder.toString();
	}
	/**
	 * Generates a random byte[] of given <code>size</code>.
	 * @param size of the generated array.
	 * @return the generated array
	 */
	static 
	public byte[] getRandomBytes(int size) {
		int len = size;
		byte[]	bigstuff = new byte[len];
		random.nextBytes(bigstuff);
		return bigstuff;
	}
	// ------------------------------------------------------------------------
	// Inner types
	// ------------------------------------------------------------------------
	/**
	 * A timer utility for benchmarking.  Internally uses {@link TimeUnit#MILLISECONDS}.
	 */
	public static final class Timer {
		final static public TimeUnit UNIT = TimeUnit.MILLISECONDS;
		final long startTime = System.currentTimeMillis();
		private long markTime = startTime;
		private long delta = 0;
		private Timer () {}
		
		public static final Timer startNewTimer() {
			return new Timer ();
		}
		public long mark () {
			markTime = now();
			delta = markTime - startTime;
			return delta;
		}
		public static final long now() { return System.currentTimeMillis(); }
		/**
         * @param keyCount
         * @return
         */
        public float opsPerSecAtDelta (long opCount, long delta) {
	        return (UNIT.convert(1, TimeUnit.SECONDS)*opCount)/(float)delta;
        }
        
        public float opsPerSecAtMark (long opCount) {
        	return opsPerSecAtDelta(opCount, deltaAtMark());
        }
        
		/**
         * @return the elapsed time since call to {@link Timer#mark} in the timer's TimeUnit
         */
        public long deltaAtMark () { return delta;}
        
        /**
         * @param unit
         * @return the elapsed time since call to {@link Timer#mark} in the specified unit.
         * If the unit provided is finer than the timer's internal time unit, finer precision will
         * naturally be irrelevant.
         */
        public long deltaAtMark (TimeUnit unit){
        	return unit.convert(deltaAtMark(), UNIT);
        }
	}
}
