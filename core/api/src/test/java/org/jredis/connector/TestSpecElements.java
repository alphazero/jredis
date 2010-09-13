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

package org.jredis.connector;

import org.jredis.TestBase;
import org.jredis.connector.Connection.Flag;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * [TODO: document me!]
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Sep 12, 2010
 * 
 */

public class TestSpecElements extends TestBase{
	
	@Test
	public void testConnectionFlags() {
		log.info("TEST:CONNECTOR spec sematics - ConnectionFlags");
		
		Flag flags[] = {Flag.CONNECT_IMMEDIATELY, Flag.SHARED, Flag.RELIABLE};
        int bitset = Flag.bitset(flags);
        for(Flag f : flags)
        	Assert.assertTrue(Flag.isSet(bitset, f), String.format("%s should have been set!\n", f.name()));
        int oldbitset = bitset;
        
        bitset = Flag.bitclear(bitset, flags[1]);
        Assert.assertFalse(bitset == oldbitset, "clearing flag should have changed bitset");
        Assert.assertFalse(Flag.isSet(bitset, flags[1]), String.format("%s should have been cleared!\n", flags[1].name()));
        
        int bitset2 = 0x0000;
    	bitset2 = Flag.bitset(bitset2, flags);
        for(Flag f : flags)
        	Assert.assertTrue(Flag.isSet(bitset2, f), String.format("%s should have been set!\n", f.name()));
	}
}
