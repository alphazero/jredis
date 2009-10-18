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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Non-reusable (disposable) signal class.
 * 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Oct 15, 2009
 * @since   alpha.0
 * 
 */
public class Signal {
	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	/** underlying lock used for implementation */
	final Lock lock = new ReentrantLock();
	/** condition based on lock */
	final Condition signalled = lock.newCondition();
	/** Signal state */
	boolean isSignalled = false;
	
	// ------------------------------------------------------------------------
	// Public interface 
	// ------------------------------------------------------------------------
	
	/**
	 * Blocking call awaits the signal for the specified duration.  
	 * Multiple threads may call this method to wait signal. Ordering is not maintained.   
	 * @param time duration in time units
	 * @param unit time unit
	 * @return true if timedout.  
	 * @throws InterruptedException
	 * @see Signal#signal()
	 */
	public boolean await (long time, TimeUnit unit) throws InterruptedException {
		boolean timedout = false;
		long nanosTimeout = unit.toNanos(time);
		long timecheck = System.nanoTime();
		lock.lock();
		try {
			while(!isSignalled && nanosTimeout > 0L) {
				timedout = signalled.await(time, unit);
				long now = System.nanoTime();
				nanosTimeout -= now - timecheck;
				timecheck = now;
			}
		}
		finally { lock.unlock(); }
		return timedout | isSignalled;
	}
	
	/**
	 * Blocking call awaits the signal.  Multiple threads may call this method to wait signal.
	 * Ordering is not maintained.   
	 * @throws InterruptedException if interrupted while waiting
	 * @see Signal#signal()
	 */
	public void await () throws InterruptedException {
		lock.lock();
		try {
			while(!isSignalled) 
				signalled.await();
		}
		finally { lock.unlock(); }
	}
	
	/**
	 * Signals.  All waiters are signaled.
	 */
	public void signal () {
		lock.lock();
		try {
			isSignalled = true;
			signalled.signalAll();
		}
		finally { lock.unlock(); }
	}
	
	/**
	 * Non-blocking call immediately returns with the current status of the signal.
	 * @return true if signaled. 
	 */
	public boolean isSignalled() {
		boolean state = false;
		lock.lock();
		try {
			state = isSignalled;
		}
		finally { lock.unlock(); }
		return state;
	}
}