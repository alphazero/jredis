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

public class JRedisTestSuiteNGBase {
	// ------------------------------------------------------------------------
	// General RI Test Suite Parameters with default values
	// ------------------------------------------------------------------------
	protected String password = "jredis";
	protected String host = "localhost";
	protected int port = 6379;
	protected int db1 = 13;
	protected int db2 = 10;
	
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
		"jredis.password", 
		"jredis.host", 
		"jredis.port",
		"jredis.db.1",
		"jredis.db.2"
	})
	@BeforeSuite
	public void suiteParametersInit(
			String password, 
			String host, 
			int port,
			int db1,
			int db2
		) 
	{
		this.password = password;
		this.host = host;
		this.port = port;
		this.db1 = db1;
		this.db2 = db2;
		Log.log("Suite parameters initialized <suiteParametersInit>");
	}	
}
