package org.jredis.ri.alphazero.semantics;

import java.nio.charset.Charset;
import org.jredis.Codec;

/**
 * Nothing to see here, folks.  Your basic {@link String#getBytes(Charset)} and {@link String#String(byte[], Charset)}
 * wrapper, and of course you can get to set the {@link Charset} if the default <code>UTF-8</code> {@link Charset} is 
 * not to your liking.
 *
 * @author  Joubin (alphazero@sensesay.net)
 * @version alpha.0, Aug 23, 2009
 * @since   alpha.0
 * 
 */
public class DefaultStringCodec implements Codec<String> {
	/** Default supported character set is UTF-8  */
	public final static Charset DEFAULT_CHARSET = Charset.forName ("UTF-8");
	/**  */
	@SuppressWarnings("unused")
    private final Charset charSet; // java 1.6
	/**
	 * 
	 */
	public DefaultStringCodec() {
		this(DEFAULT_CHARSET);
	}
	/**
	 * @param charset
	 */
	public DefaultStringCodec(Charset charset){
		this.charSet = charset;
	}

	/* (non-Javadoc)
     * @see org.jredis.Codec#decode(byte[])
     */
    @Override
    public String decode (byte[] bytes) {
        return new String(bytes);
//        return new String(bytes, charSet);  // java 1.6
    }

	/* (non-Javadoc)
     * @see org.jredis.Codec#encode(java.lang.Object)
     */
    @Override
    public byte[] encode (String value) {
		return value.getBytes();
//		return value.getBytes(charSet);
    }

	/* (non-Javadoc)
     * @see org.jredis.Codec#supports(java.lang.Class)
     */
    @Override
    public boolean supports (Class<?> type) {
        return type.equals(String.class) ? true : false;
    }
}