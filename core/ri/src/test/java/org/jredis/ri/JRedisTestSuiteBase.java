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

package org.jredis.ri;

import static org.testng.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jredis.ri.alphazero.support.Log;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;

/**
 * The grand daddy of all TestNG test classes for the RI test suites and classes,
 * this class will get loaded with all the <b>general</b> parameters we use for 
 * testing, namely host, port, password, and the DBs we will use to test (which 
 * will be flushed!)
 * <p>
 * Defaults for values are defined in this class so no testng.xml is required. Change
 * values in master pom as required.
 * <p>
 * [Note: as of now, these are defined in the master pom.]
 * 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 17, 2009
 * @since   alpha.0
 * 
 */

//TODO: get rid of NG in class name

public abstract class JRedisTestSuiteBase<T> extends ProviderTestBase<T>{
	// ------------------------------------------------------------------------
	// General RI Test Suite Parameters with default values
	// ------------------------------------------------------------------------
	protected String password = "jredis";
	protected String host = "localhost";
	protected int port = 6379;
	protected int db1 = 13;
	protected int db2 = 10;

	// ------------------------------------------------------------------------
	// General RI Test Suite Parameters with default values to avoid XML
	// ------------------------------------------------------------------------
	protected int	SMALL_DATA =  128;
	protected int	MEDIUM_DATA = 1024 * 2;
	protected int	LARGE_DATA =  1024 * 512;
	protected int	SMALL_CNT = 10;
	protected int	MEDIUM_CNT = 1000;
	protected int	LARGE_CNT = 100000;

	protected int	expire_secs = 1;
	protected long	expire_wait_millisecs = 1;

	protected final Random random = new Random(System.currentTimeMillis());

	// we'll uses these for values 
	protected final byte[]			emptyBytes = new byte[0];
	protected final String			emptyString = "";
	protected final List<byte[]>	dataList = new ArrayList<byte[]>();
	protected final List<byte[]>	sparseList = new ArrayList<byte[]>();
	protected final List<TestBean>	objectList = new ArrayList<TestBean>();
	protected final List<String>	stringList = new ArrayList<String>();
	protected final List<String>	patternList = new ArrayList<String>();
	protected final List<Integer>	intList = new ArrayList<Integer>();
	protected final List<Long>		longList= new ArrayList<Long>();
	protected final List<Double>	doubleList= new ArrayList<Double>();

	protected final Set<String>		uniqueSet = new HashSet<String> ();
	protected final Set<String>		commonSet = new HashSet<String> ();
	protected final Set<String>		set1 = new HashSet<String> ();
	protected final Set<String>		set2 = new HashSet<String> ();


	protected final String			patternA = "_AAA_";

	protected final byte[]			smallData = getRandomBytes(SMALL_DATA);
	protected final byte[]			mediumData = getRandomBytes(MEDIUM_DATA);
	protected final byte[]			largeData = getRandomBytes(LARGE_DATA);

	protected final List<String>	keys = new ArrayList<String>();

	protected int	 		cnt;
	protected String 		key = null;

	@SuppressWarnings("unused")
	private byte   		bytevalue;

	@SuppressWarnings("unused")
	private String		stringvalue;

	@SuppressWarnings("unused")
	private int			intValue;

	@SuppressWarnings("unused")
	private long		longValue;

	@SuppressWarnings("unused")
	private TestBean 	objectvalue;



	// ------------------------------------------------------------------------
	// General RI Test Suite Parameters init
	// ------------------------------------------------------------------------
	/**
	 * This method sets up all the general test parameters for all
	 * classes that inherit from it.
	 * @param password password we'll use to authenticate
	 * @param host host name
	 * @param port port number
	 * @param db1 db index for testing - will be flushed
	 * @param db2 db index for testing - will be flushed
	 */
	@Parameters({ 
		"jredis.test.password", 
		"jredis.test.host", 
		"jredis.test.port",
		"jredis.test.db.1",
		"jredis.test.db.2",

		"jredis.test.datasize.small",
		"jredis.test.datasize.medium",
		"jredis.test.datasize.large",
		"jredis.test.cnt.small",
		"jredis.test.cnt.medium",
		"jredis.test.cnt.large",
		"jredis.test.expire.secs",
		"jredis.test.expire.wait.millisecs"

	})
	@BeforeSuite
	public void suiteParametersInit(
			String password, 
			String host, 
			int port,
			int db1,
			int db2,

			int small_data,
			int medium_data,
			int large_data,
			int small_cnt,
			int medium_cnt,
			int large_cnt,
			int expire_secs,
			int expire_wait_millisecs
	) 
	{
		this.password = password;
		this.host = host;
		this.port = port;
		this.db1 = db1;
		this.db2 = db2;

		this.SMALL_DATA = small_data;
		this.MEDIUM_DATA = medium_data;
		this.LARGE_DATA = large_data;
		this.SMALL_CNT = small_cnt;
		this.MEDIUM_CNT = medium_cnt;
		this.LARGE_CNT = large_cnt;
		this.expire_secs = expire_secs;
		this.expire_wait_millisecs = expire_wait_millisecs;

		Log.log("Suite parameters initialized <suiteParametersInit>");

		setupTestSuiteData();
	}	

	// ------------------------------------------------------------------------
	// Test data setup methods
	// ------------------------------------------------------------------------

	/**
	 * All providers to be tested with the same degree of test data.
	 * We're using random data and can't guarantee exact teset data.
	 * TODO: flip switch to use random or deterministic data.
	 */
	@SuppressWarnings("boxing")
	private final void setupTestSuiteData () {
		/** setup data */
		cnt = MEDIUM_CNT;
		byte[] zerobytes = new byte[0];
		for(int i=0; i<cnt; i++){
			keys.add(getRandomAsciiString (48));
			patternList.add(getRandomAsciiString(random.nextInt(10)+2) + patternA + getRandomAsciiString(random.nextInt(10)+2));
			uniqueSet.add(getRandomAsciiString(48));
			commonSet.add(getRandomAsciiString(48));
			set1.add("set_1" + getRandomAsciiString(20));
			set2.add("set_2" + getRandomAsciiString(20));
			dataList.add(getRandomBytes (128));
			if(random.nextBoolean())
				sparseList.add(zerobytes);
			else
				sparseList.add(getRandomBytes (128));
			
			stringList.add(getRandomAsciiString (128));
			objectList.add(new TestBean("testbean." + i));
			intList.add(random.nextInt());
			longList.add(random.nextLong());
			doubleList.add(random.nextDouble());
		}
		for(String m : commonSet) {
			set1.add(m);
			set2.add(m);
		}
		Log.log("TEST-SUITE-INIT: JRedis Provider Test Suite random test data created");

	}

	protected final void prepTestDBs() {
		//	try {
		//		jredis.auth(password);
		//		Log.log("TEST-PREP: AUTH with password %s" + password);
		//	} 
		//	catch (RedisException e) {
		//		Log.error("AUTH with password " + password + " => " + e.getLocalizedMessage());
		//		fail("AUTH with password: " + password, e);
		//	}
		//	try {
		//		jredis.select(db1).flushdb().select(db2).flushdb().select(db1);
		//		Log.log("TEST-PREP: %s:%d Redis server DB %d & %d flushed", host, port, db1, db2);
		//	} 
		//	catch (RedisException e) {
		//		Log.error("SELECT/FLUSHDB for test prep" + password);
		//		fail("SELECT/FLUSHDB for test prep", e);
		//	}
	}
	// ------------------------------------------------------------------------
	// Helper methods
	// ------------------------------------------------------------------------

	/**
	 * Creates a random ascii string
	 * @param length
	 * @return
	 */
	protected String getRandomAsciiString (int length) {
		StringBuilder builder = new  StringBuilder(length);
		for(int i = 0; i<length; i++){
			char c = (char) (random.nextInt(126-33) + 33);
			builder.append(c);
		}
		return builder.toString();
	}
	/**
	 * Creates a buffer of given size filled with random byte values
	 * @param size
	 * @return
	 */
	protected byte[] getRandomBytes(int size) {
		int len = size;
		byte[]	buff = new byte[len];
		random.nextBytes(buff);
		return buff;
	}

	protected final <FAULT extends RuntimeException> void assertDidRaiseRuntimeError (Runnable test, Class<FAULT> errtype){
		boolean didRaiseError = false;
		try { test.run(); }
		catch (RuntimeException t){
			if(errtype.isAssignableFrom(t.getClass()))
				didRaiseError = true; 
		}
		catch (Exception e){ fail("Unexpected exception", e); }
		finally {
			if(!didRaiseError) { fail("Failed to raise expected RuntimeError " + errtype.getCanonicalName()); }
		}
	}
	// ------------------------------------------------------------------------
	// INNER TYPES USED FOR TESTING
	// ============================================================== TestBean
	// ------------------------------------------------------------------------
	/**
	 * This is a simple {@link Serializable} class that we use to test object
	 * values.  
	 *
	 * @author  Joubin Houshyar (alphazero@sensesay.net)
	 * @version alpha.0, Apr 18, 2009
	 * @since   alpha.0
	 * 
	 */
	public static class TestBean implements Serializable {
		/**  */
		private static final long	serialVersionUID	= 4457509786469904810L;
		public final long getCreated_on() {return named_on;}
		public final void setCreated_on(long created_on) {this.named_on = created_on;}
		public final String getName() {return name;}
		public final void setName(String name) {this.name = name;}
		public final byte[] getData() { return data;}
		public final void setData(byte[] data) { this.data = data;}
		private long   named_on;
		private String name;
		private byte[] data;
		public TestBean() {
			//			named_on = System.currentTimeMillis();
		}
		public TestBean(String string) { 
			this(); name = string;
			named_on = System.currentTimeMillis();
		}
		@Override public String toString() { return "[" + getClass().getSimpleName() + " | name: " + getName() + " created on: " + getCreated_on() + "]"; }
		@Override public boolean equals (Object o) {
			if(o instanceof TestBean) {
				TestBean isItMe = (TestBean) o;
				return isItMe.getName().equals(name) && isItMe.getCreated_on()==this.named_on;
			}
			return false;
		}

		@Override public int hashCode() {
			return name.hashCode() ^ (int)named_on;
		}
	}
	// ------------------------------------------------------------------------
	// Test support - mildly enhanced TESTNG Assert semantics
	// ------------------------------------------------------------------------
// TODO: check latest version of testng	
//	// notNull
//	public static final void assertNotNull(Object object, String msgfmt, Object...optionalFmtArgs){
//		String message = String.format(msgfmt, optionalFmtArgs);
//		Assert.assertNotNull (object, message);
//	}
//	// null
//	public static final void assertNull(Object object, String msgfmt, Object...optionalFmtArgs){
//		String message = String.format(msgfmt, optionalFmtArgs);
//		Assert.assertNull (object, message);		// << has bug.  reports a boolean comp result -- TODO: fix and patch.
//	}
//	
//	// equals
//	public static final void assertEquals(Object actual, Object expected, String msgfmt, Object...optionalFmtArgs){
//		String message = String.format(msgfmt, optionalFmtArgs);
//		Assert.assertEquals (actual, expected, message);		
//	}
//	public static final void assertEquals(byte[] actual, byte[] expected, String msgfmt, Object...optionalFmtArgs){
//		String message = String.format(msgfmt, optionalFmtArgs);
//		Assert.assertEquals (actual, expected, message);		
//	}
//	
//	// true/false
//	public static final void assertTrue(boolean condition, String msgfmt, Object...optionalFmtArgs){
//		String message = String.format(msgfmt, optionalFmtArgs);
//		Assert.assertTrue (condition, message);
//	}
//	public static final void assertFalse(boolean condition, String msgfmt, Object...optionalFmtArgs){
//		String message = String.format(msgfmt, optionalFmtArgs);
//		Assert.assertFalse (condition, message);
//	}
}
