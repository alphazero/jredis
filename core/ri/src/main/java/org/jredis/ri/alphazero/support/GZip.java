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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * GZip (de)compression utility methods.
 * @author  Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Aug 24, 2009
 * @since   alpha.0
 * 
 */

public class GZip {
    /**
     * @param data
     * @return
     */
    public static final byte[] compress(byte[] data){
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	try {
            GZIPOutputStream gzipOutputtStream = new GZIPOutputStream(out);
            gzipOutputtStream.write(data);
            gzipOutputtStream.close();
        }
        catch (IOException e) {
			throw new RuntimeException("Failed to GZip compress data", e);
        }
        return out.toByteArray();
    }
    
    /**
     * @param data
     * @return
     */
    public static final byte[] decompress(byte[] data){
		ByteArrayOutputStream buffer = null;
		GZIPInputStream gizpInputStream= null;
		try {
			buffer = new ByteArrayOutputStream();
			gizpInputStream = new GZIPInputStream(new ByteArrayInputStream(data));
			int n=-1;
			@SuppressWarnings("unused")
			int tot = 0;
			byte[] _buffer = new byte[1024 * 12];
			while(-1 != (n = gizpInputStream.read(_buffer))){
				buffer.write(_buffer, 0, n);
				tot += n;
			}
			gizpInputStream.close();
			buffer.close();
		} 
		catch (IOException e) {
			throw new RuntimeException("Failed to GZip decompress data", e);
		} 
		return buffer.toByteArray();
    }
}
