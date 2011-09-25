package org.jredis.ri.alphazero.support;

import java.io.IOException;
import java.io.InputStream;
import org.jredis.ProviderException;

/**
 * Extension of {@link java.io.InputStream} that uses the enclosing instance's
 * {@link InputStream} its data source. This is not supposed to be a general purpose
 * implementation.
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Sep 5, 2009
 * @since   alpha.0
 * 
 */
public final class FastBufferedInputStream extends java.io.InputStream {

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	/** data buffer (cache) */
	private byte[] buffer;

	/** current (read) offset of {@link FastBufferedInputStream#buffer} */
	private int   offset = 0;

	/** 
	 * underlying input stream read buffer -- the size of this buffer determines the
	 * maximum bytes read  
	 */
	final 
	private byte[] iobuffer;

	/** underying input stream */
	final
	private InputStream in;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------
	/**
	 * @param in the input source
	 * @param bufferSize size of the {@link FastBufferedInputStream#iobuffer}
	 * 
	 */
	public FastBufferedInputStream (InputStream in, int bufferSize) {
		this.in = in;
		buffer = new byte [0];
		iobuffer = new byte[bufferSize];
	}

	// ------------------------------------------------------------------------
	// Inner Ops
	// ------------------------------------------------------------------------
	/**
	 * Get more bytes from the underling {@link InputStream}.  
	 * Only reads from input source if len exceeds available data in 
	 * {@link FastBufferedInputStream#buffer}.
	 * <p>
	 * This call will block until (minimally) len bytes have been read.
	 * 
	 * @param len
	 * @throws IOException if a read on the underlying stream returns 0 length bytes.
	 * This (obviously) shouldn't happen but if it does, it would be treated as an exception.
	 */
	@SuppressWarnings("boxing")
	private final int getMoreBytes (int len) throws IOException {

		// hit the date source until we have enough bytes
		// compact (reset offset to 0) on first copy
		int rlen = 0;
		while (len > buffer.length - offset) {
			int c = in.read(iobuffer, 0, iobuffer.length);
			if(c==-1) {
				return -1;
			}
			else if(c > 0){
				int bufflen = buffer.length - offset;
				rlen+=c;
				byte[] newbuffer = new byte[bufflen + c];
				System.arraycopy(buffer, offset, newbuffer, 0, bufflen);
				System.arraycopy(iobuffer, 0, newbuffer, bufflen, c);
				offset = 0;
				buffer = newbuffer;
			}
			else {// should never happen per contract of inputstream ...
				Log.bug (String.format("ZERO! <= %d\n", c));
				throw new IOException ("input stream read return 0 bytes!");
			}
		}
		return rlen;
	}

	// ------------------------------------------------------------------------
	// Interface: InputStream
	// ------------------------------------------------------------------------

	/**
	 * <b>Note:</b><br>
	 * Breaks the contract of the {@link InputStream#read(byte[], int, int)} 
	 * to the extent that <b>call will block</b> on the underlying {@link InputStream} 
	 * until it gets all the <code>len</code> bytes specified.
	 * <p>
	 * Also, this call will return -1 if and only if it (a) needs to get more 
	 * data by calling {@link FastBufferedInputStream#getMoreBytes(int)},
	 * and (b) that call returns -1.  Note that it is possible, in a general 
	 * context, that -1 is returned but there is previously accumulated data in 
	 * {@link FastBufferedInputStream#buffer} and thus 
	 * {@link FastBufferedInputStream#available()} returns a non zero positive 
	 * integer which is less than specified <b>len</b>.  But that is not expected
	 * in the specific context of Redis protocol.
	 * 
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	int maxRead = 0;
	@Override
	public int read (byte[] b, int off, int len) throws IOException {
		if (off < 0 || off >= b.length || len < 0 || off + len > b.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		int available = buffer.length - offset;
		if(len > available) {
			int c = getMoreBytes (len);  // this is a potentially blocking call
			if(c==-1) return -1;
			else if(c < len - available) 
				throw new ProviderException ("Bug: getMoreBytes() returned less bytes than requested.  c="+c+" len=" + len + "available=" + available);
		}

		if(len == 1){
			b[off] = buffer[offset];
		}
		else {
			System.arraycopy(buffer, offset, b, off, len);
		}

		offset += len;
		return len;
	}
	
	/** 
	 * @return the length of data available without making call
	 * to the underlying stream.
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available () throws IOException {
		return buffer.length - offset;
	}

	/**
	 * {@link InputStream#mark(int)} is <b>not supported</b>.
	 * @see java.io.InputStream#markSupported()
	 */
	@Override
	public boolean markSupported () { return false; }

	/* (non-Javadoc)
	 * @see java.io.InputStream#read(byte[])
	 */
	@Override
	public int read (byte[] b) throws IOException { return read (b, 0, b.length); }

	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read () throws IOException { 
		byte[] b = new byte[1];
		int c = read(b, 0, 1);
		if(c == -1) return -1;
		return b[0];
	}
}