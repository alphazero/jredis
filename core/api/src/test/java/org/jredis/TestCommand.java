/*
 *   Copyright 2010 Joubin Houshyar
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

package org.jredis;

import org.jredis.protocol.*;
import org.jredis.protocol.Command.*;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Sep 11, 2010
 * 
 */

public class TestCommand extends TestBase {
	/*
	public final String code;
	public final byte[] bytes;
	public final RequestType requestType;
	public final ResponseType responseType;
	private final int flags_bitset;
	 */
	@Test
	public void testCommandSemanticsRequestType () {
		log.info("TEST: Command sematics - RequestType");
		for(Command c : Command.values()){
			assertTrue(new String(c.bytes).indexOf(Command.OPTCODE) == -1, "Command bytes must not include control characters.");
		}

	}
//	@Test
//	public void testCommandFlag () {
//        for(Flag f : Flag.values())
//        	System.out.format("Flag: %s  bitset: %d ord: %d\n", f.name(), f.bitmask, f.ordinal());
//        
//        Command ping = Command.PING;
//        Command test = Command.TEST;
//        Command test2 = Command.TEST2;
//        
//    	int bitset = Flag.bitset(Flag.TEST, Flag.WOOF);
//    	System.out.format("%s isSet: %b\n",Flag.TEST.name(), Flag.isSet(bitset, Flag.TEST));
//    	System.out.format("%s isSet: %b\n",Flag.WOOF.name(), Flag.isSet(bitset, Flag.WOOF));
//    	System.out.format("%s isSet: %b\n",Flag.MEOW.name(), Flag.isSet(0x0003, Flag.MEOW));
//    	
//        for(Flag f : Flag.values())
//        	System.out.format("cmd: %s Flag: %s  isSet: %b\n", test2.name(), f.name(), test2.isSet(f));
//    }
}
