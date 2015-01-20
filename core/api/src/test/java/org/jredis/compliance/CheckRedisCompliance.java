/*
 *   Copyright 2009-2012 Joubin Houshyar
 * 
 *   This file is part of JRedis.
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

package org.jredis.compliance;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jredis.JRedis;
import org.jredis.JRedisFuture;

/**
 * Informal.  A munge of redis.h is used to produce the canonical
 * Redis commands for a given Redis release.  This program reflects
 * over JRedis to determine missing commands.
 * <p>
 * TODOS: 
 * <li>need to address minor variations introduced for Java readablility.</li>
 * <li>beef up annotations to tighten up the compliance check</li>
 * <li>apt cycle on release would be best</li>
 * 
 * 
 * @author Joubin Houshyar <alphazero@sensesay.net>
 * @date:  Feb 1, 2012
 */
public class CheckRedisCompliance {
	// TODO: drive this via annotation on the JRedis interface 
	static final String spec_file_path = "META-INF";
	static final String spec_file_prefix = "redis-commands";
	static final String spec_file_ext = "txt";
	final int major;
	final int minor;
	final InputStream specfilein;
	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------
	/** 
	 * @throws Exception  
	 */
	public CheckRedisCompliance(int major, int minor) throws Exception {
		this.major = major;
		this.minor = minor;
		specfilein = getSpecFileInputStream(major, minor);
	}
	private void run() throws Exception {
		List<String> cmdlist = getSpecCommandList();
		checkAndReportCompliance(cmdlist, JRedis.class);
		checkAndReportCompliance(cmdlist, JRedisFuture.class);
	}
	private List<String> checkAndReportCompliance(List<String> cmdlist, Class<?> jredis_class) {
		Method[] pubmethods = jredis_class.getMethods();
		Set<String> uniqueMethodNames = new HashSet<String>();
		for(Method m : pubmethods)
			uniqueMethodNames.add(m.getName());
			
		List<String> notsupported = new ArrayList<String>();
		for(String cmd : cmdlist) {
			if(!uniqueMethodNames.contains(cmd))
				notsupported.add(cmd);
		}
		if(!notsupported.isEmpty()) {
			reportNonCompliance(notsupported, jredis_class.getSimpleName());
		}
			
		return notsupported;
	}
	private void reportNonCompliance(List<String> notsupported, String simpleName) {
		System.out.format("\n /// non-compliance report for %-12s //////////////\n", simpleName);
		int i = 0;
		for(String cmd : notsupported){
			System.out.format("[%2d] %s\n",i++, cmd);
		}
		System.out.format("\n /////////////////////////////////////////////////////////\n", simpleName);
	}
	private List<String> getSpecCommandList() throws IOException {
		List<String> cmdlist = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(specfilein));
		String cmd = null;
		while((cmd=reader.readLine()) != null){
			cmdlist.add(cmd);
		}
		return cmdlist;
	}
	private static InputStream getSpecFileInputStream(int major, int minor) {
		String fname = 
			String.format("%s%s%s-%d.%d.n.%s",
				spec_file_path, File.separator,spec_file_prefix,  major, minor, spec_file_ext)
			      .toString();
		InputStream in = JRedis.class.getClassLoader().getResourceAsStream(fname);
		if(in == null){
			String errmsg = String.format("No spec file found: <%s>", fname).toString();
			throw new IllegalArgumentException(errmsg);
		}
		return in;
	}
	public static void main(String[] args) {
		try {
			new CheckRedisCompliance(2, 4).run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
