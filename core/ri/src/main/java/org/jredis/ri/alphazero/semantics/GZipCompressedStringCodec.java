package org.jredis.ri.alphazero.semantics;

import static org.jredis.ri.alphazero.support.GZip.compress;
import static org.jredis.ri.alphazero.support.GZip.decompress;

/**
 * 
 * @author  Joubin (alphazero@sensesay.net)
 * @version alpha.0, Aug 23, 2009
 * @since   alpha.0
 * 
 */
public class GZipCompressedStringCodec extends DefaultStringCodec {

	/* (non-Javadoc)
     * @see org.jredis.Codec.DefaultStringCodec#decode(byte[])
     */
    @Override
    public String decode (byte[] bytes) {
        return decode(decompress(bytes));
    }

	/* (non-Javadoc)
     * @see org.jredis.Codec.DefaultStringCodec#encode(java.lang.String)
     */
    @Override
    public byte[] encode (String value) {
        return compress(super.encode(value));
    }
    
//    // ------------------------------------------------------------------------
//    // Test nazis be damned -- here's the test of the GZip methods.
//    // ------------------------------------------------------------------------
//    
//    /**
//     * TODO: TestNG this thing.
//     * Test it.
//     * @param args
//     */
//    public static final void main (String [] args){
//    	int cnt = 1000;
//    	int size = 1024 * 24;
//    	for(int i=0; i<cnt; i++){
//    		String randomString = Util.getRandomString(size);
//    		byte[] stringBytes = randomString.getBytes(DEFAULT_CHARSET);
//    		byte[] compressed = compress(stringBytes);
//    		byte[] decompressed = decompress(compressed);
//    		Assert.assertEquals(decompressed, stringBytes);
//    	}
//    	Log.log("GZIP Compression tests a OK!");
//    }
}