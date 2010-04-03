/*
 *   Copyright 2009-2010 Joubin Houshyar
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

package org.jredis.test.util;

public class RunningAverage {
	private float  avg;
	private long	max;
	private long	min = Long.MAX_VALUE;
	private long  n;
	public RunningAverage() {
		avg = 0;
		n = 0;
	}
	public long onMeasure (long delta){
		avg =((avg*n)+delta)/(++n);
		if(delta > max) max = delta;
		if(delta < min) min = delta;
		return (long) avg;
	}
    public long get () { return (long) avg; }
	/**
     * @return
     */
    public long getMin () { return min; }
    public long getMax () { return max; }
	/**
     * 
     */
    public long getCount () { return n; }
}
