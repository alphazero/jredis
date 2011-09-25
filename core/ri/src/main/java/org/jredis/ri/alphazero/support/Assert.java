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

import java.lang.reflect.InvocationTargetException;
import java.util.Formatter;

/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 02, 2009
 * @since   alpha.0
 * 
 */
public class Assert {

	/**
	 * assert identical based on content.
	 * @param <E>
	 * @param a
	 * @param b
	 * @param clazz
	 */
	public static final <E extends RuntimeException>
	void isEquivalent (byte[] a, byte[] b) {
		isEquivalent(a, b, RuntimeException.class);
	}
	/**
	 * assert identical based on content.
	 * @param <E>
	 * @param a
	 * @param b
	 * @param clazz
	 */
	public static final <E extends RuntimeException>
	void isEquivalent (byte[] a, byte[] b, Class<E> clazz) {
		notNull(a, "arg a", clazz);
		notNull(b, "arg b", clazz);
		if(a.length != b.length) { 
			throwIt("byte arrays being compared are of unequal length", clazz);
		}
		for(int i=0; i<a.length; i++){
			if(a[i]!=b[i]){
				throwIt("byte arrays being compared differ beginning at index " + i, clazz);
			}
		}
	}
	/**
	 * <b>Usage</b>: <pre><code>
	 * // ... somewhere within the bowels of your code ..
	 * //
	 * Assert.notNull (aReference, YourRuntimeException.class);
	 * </code> </pre>
	 * @param <T>
	 * @param <E>
	 * @param obj
	 * @param clazz
	 * @return
	 * @throws E
	 */
	public static final <T, E extends RuntimeException> 
	T notNull
		(T obj, Class<E> clazz)
	throws E
	{
		if(null == obj){ throwIt("null reference", clazz);}
		return obj;
	}
	
	/**
	 * <b>Usage</b>: <pre><code>
	 * // ... somewhere within the bowels of your code ..
	 * //
	 * Assert.notNull (inputRecord, "inputRecord", YourRuntimeException.class);
	 * </code> </pre>
	 * @param <T>
	 * @param <E>
	 * @param obj
	 * @param info
	 * @param clazz
	 * @return
	 * @throws E
	 */
	public static final <T, E extends RuntimeException> 
	T notNull
		(T obj, String info, Class<E> clazz)
	throws E
	{
		if(null == obj){ throwIt("null reference:{"+info+"}", clazz);}
		return obj;
	}
	
	@SuppressWarnings("boxing")
	public static final <T, E extends RuntimeException> 
	T notNull
		(T obj, int idx, Class<E> clazz)
	throws E
	{
		if(null == obj){ throwIt(String.format("null reference: arg {%d}", idx), clazz);}
		return obj;
	}
	
	/**
	 * <b>Usage</b>: <pre><code>
	 * // ... somewhere within the bowels of your code ..
	 * //
	 * Assert.isTrue (conn.isReady(), "connection is ready", YourRuntimeException.class);
	 * </code> </pre>
	 * @param <E>
	 * @param fact
	 * @param claim
	 * @param clazz
	 * @throws E
	 */
	public static final <E extends RuntimeException> 
	void isTrue
		(boolean fact, String claim, Class<E> clazz) 
	throws E 
	{
		if(!fact) { throwIt("its not true that \"" + claim + "\"", clazz);}
	}
	
	/**
	 * <b>Usage</b>: <pre><code>
	 * // ... somewhere within the bowels of your code ..
	 * //
	 * Assert.isTrue (conn.isReady(), YourRuntimeException.class);
	 * </code> </pre>
	 * @param <E> is what you wish thrown - must be related to {@link RuntimeException}
	 * @param fact
	 * @param clazz
	 * @throws E
	 */
	public static final <E extends RuntimeException> 
	void isTrue
		(boolean fact, Class<E> clazz) 
	throws E 
	{
		if(!fact) { throwIt("Factual error ", clazz);}
	}
	
	/**
	 * @param <E>
	 * @param n
	 * @param from
	 * @param to
	 * @param info
	 * @param clazz
	 * @return
	 * @throws E
	 */
	public static final <E extends RuntimeException> 
	long inRange
		(long n, long from, long to, String info, Class<E> clazz)
	throws E
	{
		if(n > to || n < from ){ throwIt("exceeds valid range :{"+info+"}", clazz);}
		return n;
	}
	/**
	 * Note: asserts n is in the range from->to, inclusive.
	 * @param <E>
	 * @param n
	 * @param from
	 * @param to
	 * @param info
	 * @param clazz
	 * @return
	 * @throws E
	 */
	public static final <E extends RuntimeException> 
	int inRange
		(int n, int from, int to, String info, Class<E> clazz)
	throws E
	{
		if(n > to || n < from ){ throwIt("exceeds valid range :{"+info+"}", clazz);}
		return n;
	}
//	Object obj = Assert.isType(T obj, Class<T> clazz, "", RuntimeException.class);

	/**
	 * Tests to see if the object can be cast to the specified type.
	 * Will not test to see arguments are not null.
	 * @param <T> the type
	 * @param <E> the thrown exception type
	 * @param obj the object to be cast
	 * @param clazz of the the target type
	 * @param info the message for the exception thrown
	 * @param throwable specified if not
	 * @return the cast object if successful
	 * @throws E
	 */
	public static final <T, E extends RuntimeException> 
	T cast
		(Object obj, Class<T> clazz, String info, Class<E> throwable)
	throws E
	{
		T t = null;
		try {
			t = clazz.cast(obj);
		}
		catch (ClassCastException e) {
			String actual = obj.getClass().getCanonicalName();
			throwIt(info + " [object type: "+ actual +" target: " + clazz.getCanonicalName() +"]", throwable);
		}
		return t;
	}
	
	/**
	 * Not really intended for external use since you could simply throw new E (msg). 
	 * <p>
	 * <b>Usage</b> (if you must): <pre><code>
	 * // ... somewhere within the bowels of your code ..
	 * //
	 * Assert.throwIt ("why am i calling Assert.throwIt() when I could be throwing myself?", YourRuntimeException.class);
	 * </code> </pre>
	 * @param <E> {@link RuntimeException} subclass to to throw
	 * @param msg message to set in the exception
	 * @param clazz the Class of E
	 * @throws E your requested exception (unless reflection instantiate failed, 
	 *        in which case it will be a plain old {@link RuntimeException})
	 */
	private static final <E extends RuntimeException> 
	void throwIt 
		(String msg, Class<E> clazz) 
	throws E 
	{
		// TODO: isolate parts of this for our logger
		// TODO: hey - a simple logger ...
		// 1 - get the current stack trace and filter it
		//
		int fidx = 0;
		StackTraceElement ste[] =Thread.currentThread().getStackTrace();
		int j=0;
		for(StackTraceElement e : ste){
			j++;
			if(e.getClassName().equals(Assert.class.getName())) fidx = j;
		}
		StackTraceElement[] filtered = new StackTraceElement[ste.length-fidx];
		for(int i=fidx; i<ste.length; i++) filtered[i-fidx]=ste[i];

		// 2- get info about the source of the call (aka where the assertion failed)
		//
		StackTraceElement src = filtered[0];
		String simpleClassName = src.getClassName().substring(src.getClassName().lastIndexOf('.')+1);
		int scni = src.getClassName().indexOf(simpleClassName) > 0 ? src.getClassName().indexOf(simpleClassName)-1 : 0;
		String packageName = src.getClassName().substring(0, scni);
		
		@SuppressWarnings("boxing")
		String info = new Formatter().format("%s in method %s.%s() [file: %s line:%d - package: %s]", msg,
				simpleClassName, src.getMethodName(), src.getFileName(), src.getLineNumber(), packageName).toString();

		// 3- throw the exception
		//
		RuntimeException rte = new IllegalArgumentException(info); // in case we can't instantiate the E class
		try {
			rte = clazz.getDeclaredConstructor(String.class, Throwable.class).newInstance(info, rte);
		}
		catch (InstantiationException e) { e.printStackTrace(); }
		catch (IllegalAccessException e) { e.printStackTrace(); }
		catch (IllegalArgumentException e) { e.printStackTrace(); }
		catch (SecurityException e) { e.printStackTrace(); }
		catch (InvocationTargetException e) { e.printStackTrace(); }
		catch (NoSuchMethodException e) { e.printStackTrace(); }
		
		// 4- use the filtered stacktrace from (1)
		rte.setStackTrace(filtered);
		
		// 5- its coming your way ... catch!
		throw rte;
	}
}
