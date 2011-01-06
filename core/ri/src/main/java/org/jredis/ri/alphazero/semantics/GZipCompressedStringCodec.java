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
        return super.decode(decompress(bytes));
    }

	/* (non-Javadoc)
     * @see org.jredis.Codec.DefaultStringCodec#encode(java.lang.String)
     */
    @Override
    public byte[] encode (String value) {
        return compress(super.encode(value));
    }
}