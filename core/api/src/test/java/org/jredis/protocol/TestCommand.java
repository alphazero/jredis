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

package org.jredis.protocol;

import org.jredis.TestBase;
import static org.jredis.protocol.Command.Flag.*;

import org.jredis.protocol.Command.Flag;
import org.testng.Assert;
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
	@Test
	public void testCommandSemanticsRequestType () {
		log.info("TEST:PROTOCOL Command sematics - RequestType");
		for(Command c : Command.values()){
			assertTrue(new String(c.bytes).indexOf(Command.OPTCODE) == -1, "Command bytes must not include control characters.");
		}
	}
	@Test
	public void testCommandFlags() {
		log.info("TEST:PROTOCOL Command sematics - CommandFlags");
		Flag flags[] = {TEST, FOO, BAR};
        int bitset = Flag.bitset(flags);
        for(Flag f : flags)
        	Assert.assertTrue(Flag.isSet(bitset, f), String.format("%s should have been set!\n", f.name()));
        int oldbitset = bitset;
        
        bitset = Flag.bitclear(bitset, flags[1]);
        Assert.assertFalse(bitset == oldbitset, "clearing flag should have changed bitset");
        Assert.assertFalse(Flag.isSet(bitset, flags[1]), String.format("%s should have been cleared!\n", flags[1].name()));
	}
}
