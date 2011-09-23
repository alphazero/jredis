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

import static org.jredis.connector.Connection.Socket.Property.SO_PREF_BANDWIDTH;
import static org.jredis.connector.Connection.Socket.Property.SO_PREF_CONN_TIME;
import static org.jredis.connector.Connection.Socket.Property.SO_PREF_LATENCY;
import static org.jredis.connector.Connection.Socket.Property.SO_RCVBUF;
import static org.jredis.connector.Connection.Socket.Property.SO_SNDBUF;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.jredis.RedisException;
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.JRedisClient;
import org.jredis.ri.alphazero.connection.DefaultConnectionSpec;
import org.jredis.ri.alphazero.support.Log;
/**
 * Illustrates using the {@link ConnectionSpec} as a parameter when creating the {@link JRedisClient}.
 * @author  Joubin (alphazero@sensesay.net)
 * @see ConnectionSpec
 */

public class UsingConnectionSpec {
	public static void main (String[] args) {
	    exampleUsingDefaultConnectionSpec ();
	    exampleUsingCustomTCPSettings();
    }

	/**
     * Alright, so here we're going to fiddle with the various TCP settings that the JRedisClient
     * will use in its connection.  
     */
    @SuppressWarnings("boxing")
	private static void exampleUsingCustomTCPSettings () {
    	// try our own values for various flags and properties
    	//
    	{
    		// Note that if your localhost:6379 redis server expects a password
    		// this will fail
	    	try {
	    		String password = "jredis";
	    		InetAddress address = InetAddress.getLocalHost();
	    		
	    		// 1 - get the default connection spec
	    		//
		    	ConnectionSpec connectionSpec = DefaultConnectionSpec.newSpec();
		    	
		    	// 2 - customize it
		    	// here we're demonstrating the full set of parameters -- obviously you can just set what you need
		    	// but DO NOTE that the SO_PREF_XXX properties of TCP sockets must be defined as a set.  See
		    	// ConnectionSpec for javadoc details.
		    	//
		    	// Here we are spec'ing a connection that is NOT kept alive, and obviously is really keen on making sure
		    	// we connect as fast as possible.  This is a good connectionspec for disposable JRedisClients that are used
		    	// to issue a few commands and then discarded.  We're minimizing the connection overhead cost.
		    	
		    	connectionSpec
		    		// to be or not to be -- you decide
		    		//
		    		.setSocketFlag(Connection.Socket.Flag.SO_KEEP_ALIVE, Boolean.FALSE)				// DO NOT keep socket allive

		    		// connect retries on connection breaks
		    		//
		    		.setReconnectCnt(2) 										// reconnect attempts set to 2 retries

		    		// TCP algorithm preferences
		    		//
		    		.setSocketProperty(SO_PREF_CONN_TIME, 0) 	// connection time is highester pref
		    		.setSocketProperty(SO_PREF_LATENCY, 1)		// latency is 2nd pref
		    		.setSocketProperty(SO_PREF_BANDWIDTH, 2)	// bandwith is 3rd pref
		    	
		    		// TCP buffer sizes -- more than likely your platform's default settings are quite large
		    		// but if you are itching to try your own settings, please do.  Remember:  connections
		    		// will use whatever is the larger value: you OS's TCP buffer sizes or your ConnectionSpecs
		    		// so you can NOT use these settings to shrink the SND and RCV buffer sizes.
		    		//
		    		.setSocketProperty(SO_RCVBUF, 1024 * 128)  // set RCV buffer to 128KB
		    		.setSocketProperty(SO_SNDBUF, 1024 * 128) // set SND buffer to 128KB
		    		
		    		// obviously we can still set the basic props as well ..
		    		//
		    		.setAddress(address)
		    		.setCredentials(password.getBytes())
		    		.setDatabase(13);
		    	
		    	// finally - use it to create the JRedisClient instance
		    	//
		    	JRedisClient jredis = new JRedisClient(connectionSpec);
		        jredis.ping();
		        Log.log("Sweet success -- we're connected using custom TCP settings");
	        }
	        catch (RedisException e) {
		        Log.error("Failed to connect to Redis using JRedisClient custom ConnectionSpec -- password perhaps? => " + e.getLocalizedMessage());
	        }
            catch (UnknownHostException e) {
            	Log.error("Unknownhost: " + e.getLocalizedMessage());
            }
    	}
    }

	/**
     * On the first try, We're not really using the {@link ConnectionSpec} here, but it shows how to use
     * the {@link JRedisClient#JRedisClient(ConnectionSpec)} constructor.  Defaults are
     * localhost, 6379, and no password, and database 0.
     * <p>
     * Then we try setting the basic {@link ConnectionSpec} properties:  host, port, password, and database.
     */
    @SuppressWarnings("boxing")
	private static void exampleUsingDefaultConnectionSpec () {
    	
    	// 1st example: using defaults
    	// if your server expects a AUTH password, this will fail so see next block
    	{
    		// Note that if your localhost:6379 redis server expects a password
    		// this will fail
	    	ConnectionSpec connectionSpec = DefaultConnectionSpec.newSpec();
	    	JRedisClient jredis = new JRedisClient(connectionSpec);
	    	try {
		        jredis.ping();
		        Log.log("Sweet success -- we're connected using all default values.");
	        }
	        catch (RedisException e) {
		        Log.error("Failed to connect to Redis using JRedisClient default ConnectionSpec -- password perhaps? => " + e.getLocalizedMessage());
	        }
    	}
    	
    	// 2nd example: using defaults but setting the password and the database.  
    	// also demonstrated using the method chaining on the ConnectionSpec property setters.
    	{
    		// Note that if your localhost:6379 redis server expects a password
    		// this will fail
    		String password = "jredis";
    		int database = 11;
	    	ConnectionSpec connectionSpec = DefaultConnectionSpec.newSpec();
	    	connectionSpec
	    		.setCredentials(password.getBytes())
	    		.setDatabase(database);
	    	
	    	JRedisClient jredis = new JRedisClient(connectionSpec);
	    	try {
		        jredis.ping();
		        Log.log("Sweet success -- we're connected using %s as password to %d database.", password, database);
	        }
	        catch (RedisException e) {
		        Log.error("Failed to connect to Redis using JRedisClient default ConnectionSpec -- password perhaps? => " + e.getLocalizedMessage());
	        }
    	}
    	
    	// final example: using defaults but setting the full set of basic settings  
    	{
    		// Note that if your localhost:6379 redis server expects a password
    		// this will fail
	    	try {
	    		String password = "jredis";
	    		int	   port = 6379;
	    		int	   database = 11;
	    		InetAddress address = InetAddress.getLocalHost();
	    		
	    		// 1 - get the default connection spec for the basic settings
	    		//
		    	ConnectionSpec connectionSpec = DefaultConnectionSpec.newSpec(address, port, database, password.getBytes());
		    	
		    	// finally - use it to create the JRedisClient instance
		    	//
		    	JRedisClient jredis = new JRedisClient(connectionSpec);
		        jredis.ping();
		        Log.log("Sweet success -- we're connected using default specs and various server info settings.");
	        }
	        catch (RedisException e) {
		        Log.error("Failed to connect to Redis using JRedisClient default ConnectionSpec -- password perhaps? => " + e.getLocalizedMessage());
	        }
            catch (UnknownHostException e) {
            	Log.error("Unknownhost: " + e.getLocalizedMessage());
            }
    	}
    }
}
