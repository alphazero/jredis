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

package org.jredis.ri.alphazero.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import org.jredis.ClientRuntimeException;
import org.jredis.NotSupportedException;
import org.jredis.ProviderException;
import org.jredis.protocol.Command;
import org.jredis.protocol.Protocol;
import org.jredis.protocol.Request;
import org.jredis.protocol.Response;
import org.jredis.protocol.ResponseStatus;
import org.jredis.ri.alphazero.support.Assert;
import org.jredis.ri.alphazero.support.Convert;
import org.jredis.ri.alphazero.support.Log;


/**
 * [TODO: document me!]
 *
 * @author  Joubin (alphazero@sensesay.net)
 * @version alpha.0, Apr 10, 2009
 * @since   alpha.0
 * 
 */
public abstract class ProtocolBase implements Protocol {

	// ------------------------------------------------------------------------
	// Protocol Revision specific 
	// ------------------------------------------------------------------------

	public static final byte[] 	CRLF = {(byte) 13, (byte)10};
	public static final byte[] 	SPACE = {(byte) 32};
	public static final int 	CRLF_LEN = CRLF.length;
	public static final int 	DELIMETER_LEN = SPACE.length;
	public static final byte	ERR_BYTE 	= (byte) 45; // -
	public static final byte	OK_BYTE 	= (byte) 43; // +
	public static final byte	COUNT_BYTE 	= (byte) 42; // *
	public static final byte	SIZE_BYTE 	= (byte) 36; // $
	public static final byte	NUM_BYTE 	= (byte) 58; // :
	public static final byte	ASCII_ZERO	= (byte) 48; // 0
	
	// ------------------------------------------------------------------------
	// Protocol Revision specific consts
	// ------------------------------------------------------------------------
	protected ProtocolBase () {}
	
	// ------------------------------------------------------------------------
	// Interface: Protocol
	// ------------------------------------------------------------------------
	
	/* TODO: lets just forget about this multi-version nonsense. */  
	@Override
	public boolean isCompatibleWithVersion(String version) {
		return version.equals("0.09");
	}
	
	public byte[] createRequestBuffer(Command cmd, byte[]...args) throws ProviderException, IllegalArgumentException {
		class Buffer {
			byte[] b = null;
			private int off = 0;
			Buffer(int size){
				b = new byte[size];
			}
			void write (byte[] d){
				final int dlen = d.length;
				System.arraycopy(d, 0, b, off, dlen);
				off+=dlen;
			}
			void write (byte d){
				b[off] = d;
				off++;
			}
			byte[] getBytes() {
				return b;
			}
		}
		Buffer buffer = null;
		byte[] cmdLenBytes = Convert.toBytes(cmd.bytes.length);
		byte[] lineCntBytes = Convert.toBytes(args.length+1);

		// calculate the buffer size
		int bsize = 1 + lineCntBytes.length + CRLF_LEN + 1 + cmdLenBytes.length + CRLF_LEN + cmd.bytes.length + CRLF_LEN;
		for(int i=0;i<args.length; i++){
			byte[] argLenBytes = Convert.toBytes(Assert.notNull(args[i], i, ProviderException.class).length);
			int _bsize = 1 + argLenBytes.length + CRLF_LEN + args[i].length + CRLF_LEN;
			bsize += _bsize;
		}
		
		buffer = new Buffer(bsize);
        buffer.write(COUNT_BYTE);  		// 1
        buffer.write(lineCntBytes);		// lineCntBytes.length()
        buffer.write(CRLF);				// CRLF.lengt
        buffer.write(SIZE_BYTE);		// 1
        buffer.write(cmdLenBytes);		// length
        buffer.write(CRLF);				// CRLF_LEN
        buffer.write(cmd.bytes);		//
        buffer.write(CRLF);
        
		switch (cmd.requestType) {

		case NO_ARG:
		    break;
		    
		// TODO: check w/ antirez if in fact nulls are now generally accepted
		// that is the only diff here.
		case BULK_SET:
			String errmsg = "Only MSET, MSETNX, LINSERT bulk commands are supported";
			Assert.isTrue(cmd == Command.MSET || cmd == Command.MSETNX || cmd == Command.LINSERT, errmsg, NotSupportedException.class);

			// THIS IS CLEARLY BROKEN ... SO MUCH FOR COMPREHSIVE TESTS ...
			buffer.write(COUNT_BYTE);
			buffer.write(lineCntBytes);
			buffer.write(CRLF);
			buffer.write(SIZE_BYTE);
			buffer.write(cmdLenBytes);
			buffer.write(CRLF);
			buffer.write(cmd.bytes);
			buffer.write(CRLF);
			
			for(int s=0; s<args.length; s++){
				buffer.write(SIZE_BYTE);
				if (args[s] != null) {
  					buffer.write(Convert.toBytes(args[s].length));
  					buffer.write(CRLF);
  					buffer.write(args[s]);
  					buffer.write(CRLF);
  				} else {
					buffer.write(ASCII_ZERO);
					buffer.write(CRLF);
					buffer.write(CRLF);
  				}
			}
			break;
		
		default:
			for(int i=0;i<args.length; i++){
				buffer.write(SIZE_BYTE);
				buffer.write(Convert.toBytes(Assert.notNull(args[i], i, ProviderException.class).length));
				buffer.write(CRLF);
				buffer.write(args[i]);
				buffer.write(CRLF);
			}
			break;
		
		}
		return buffer.getBytes();
	}

	/* (non-Javadoc)
	 * @see org.jredis.connector.Protocol#createRequest(org.jredis.Command, byte[][])
	 */
	@Override
	public Request createRequest(Command cmd, byte[]... args) throws ProviderException, IllegalArgumentException {
		
		ByteArrayOutputStream buffer = createRequestBufffer (cmd);

		try {
			byte[] cmdLenBytes = Convert.toBytes(cmd.bytes.length);
			byte[] lineCntBytes = Convert.toBytes(args.length+1);

            buffer.write(COUNT_BYTE);
            buffer.write(lineCntBytes);
            buffer.write(CRLF);
            buffer.write(SIZE_BYTE);
            buffer.write(cmdLenBytes);
            buffer.write(CRLF);
            buffer.write(cmd.bytes);
            buffer.write(CRLF);
            
			switch (cmd.requestType) {

			case NO_ARG:
			    break;
			    
			// TODO: check w/ antirez if in fact nulls are now generally accepted
			// that is the only diff here.
			case BULK_SET:
				String errmsg = "Only MSET, MSETNX, LINSERT bulk commands are supported";
				Assert.isTrue(cmd == Command.MSET || cmd == Command.MSETNX || cmd == Command.LINSERT, errmsg, NotSupportedException.class);

				// THIS IS CLEARLY BROKEN ... SO MUCH FOR COMPREHSIVE TESTS ...
				buffer.write(COUNT_BYTE);
				buffer.write(lineCntBytes);
				buffer.write(CRLF);
				buffer.write(SIZE_BYTE);
				buffer.write(cmdLenBytes);
				buffer.write(CRLF);
				buffer.write(cmd.bytes);
				buffer.write(CRLF);
				
				for(int s=0; s<args.length; s++){
					buffer.write(SIZE_BYTE);
					if (args[s] != null) {
	  					buffer.write(Convert.toBytes(args[s].length));
	  					buffer.write(CRLF);
	  					buffer.write(args[s]);
	  					buffer.write(CRLF);
	  				} else {
						buffer.write(ASCII_ZERO);
						buffer.write(CRLF);
						buffer.write(CRLF);
	  				}
				}
				break;
			
			default:
				for(int i=0;i<args.length; i++){
					buffer.write(SIZE_BYTE);
					buffer.write(Convert.toBytes(Assert.notNull(args[i], i, ProviderException.class).length));
					buffer.write(CRLF);
					buffer.write(args[i]);
					buffer.write(CRLF);
				}
				break;
			
			}
		}
		catch (IOException e) {
			throw new ProviderException("Problem writing to the buffer" + e.getLocalizedMessage(), e);
		}
		return createRequest(buffer);
	}
	
	/* (non-Javadoc)
	 * @see org.jredis.connector.Protocol#createResponse(org.jredis.Command)
	 */
	@Override
	public Response createResponse(Command cmd) throws ProviderException, ClientRuntimeException {

		Response response = null;
		switch (cmd.responseType){
		case BOOLEAN:
			response = createBooleanResponse(cmd);
			break;
		case BULK:
			response = createBulkResponse (cmd);
			break;
		case MULTI_BULK:
			response = createMultiBulkResponse (cmd);
			break;
		case NUMBER:
			response = createNumberResponse (cmd);
			break;
		case STATUS:
			response = createStatusResponse (cmd);
			break;
		case STRING:
			response = createStringResponse (cmd);
			break;
		case VIRTUAL:
			response = new VirtualResponse(ResponseStatus.STATUS_CIAO);
			break;
		case NOP:
			response = new VirtualResponse(ResponseStatus.STATUS_OK); // TODO: needs more thinking ..
			break;
		case QUEUED:
		case RESULT_SET:
			throw new NotSupportedException(String.format("ResponseType %s not yet supported", cmd.requestType.name()));
		
		}

		return response;
	
	}


	// ------------------------------------------------------------------------
	// Extension Points
	// ------------------------------------------------------------------------
	
	protected abstract ByteArrayOutputStream createRequestBufffer(Command cmd);
	protected abstract Request createRequest (ByteArrayOutputStream buffer);
	protected abstract Response createMultiBulkResponse(Command cmd) ;
	protected abstract Response createBulkResponse(Command cmd) ;
	protected abstract Response createNumberResponse(Command cmd /*, boolean bigNum*/) ;
	protected abstract Response createBooleanResponse(Command cmd) ;
	protected abstract Response createStringResponse(Command cmd) ;
	protected abstract Response createStatusResponse(Command cmd);

	
	// ========================================================================
	// Inner Types
	// ========================================================================
	
	/**
	 * SimpleRequest implements the required {@link Request#read(InputStream)}
	 * using a (likely) shared data buffer.  It is not thread safe and can only
	 * be used by connections that serialized request processing through a single
	 * requesting thread.
	 * <p>
	 * Specifically, if an instance of this class is obtained and not immediately
	 * used (by calling read) before the user has obtained another instance of the
	 * same, data corruption is guaranteed.
	 *
	 * @author  Joubin Houshyar (alphazero@sensesay.net)
	 * @version alpha.0, 04/02/09
	 * @since   alpha.0
	 * 
	 */
	public static class StreamBufferRequest implements Request {

		/**  */
		ByteArrayOutputStream buffer;
		/**
		 * @param buffer
		 */
		public StreamBufferRequest (ByteArrayOutputStream	buffer) {
			this.buffer = buffer;
		}
		public void reset (ByteArrayOutputStream	buffer) {
			this.buffer = buffer;
		}
		/* (non-Javadoc)
		 * @see com.alphazero.jredis.connector.Message#read(java.io.InputStream)
		 */
		@Override
		public void read(InputStream in) throws ClientRuntimeException, ProviderException {
			throw new ProviderException("Request.read is not supported by this class! [Apr 2, 2009]");
		}

		/**
		 * Writes the entire content of the message to the output stream and flushes it.
		 * 
		 * @param out the stream to write the Request message to.
		 */
		@Override
		public void write(OutputStream out) throws ClientRuntimeException, ProviderException {
			try {
				// you would expect these to throw exceptions if the socket has been reset
				// but they don't.  
				buffer.writeTo(out);
				out.flush();
			}
			catch (SocketException e){
				Log.error("StreamBufferRequest.write(): SocketException on write: " + e.getLocalizedMessage());
				throw new ClientRuntimeException ("socket exception", e);
			}
			catch (IOException e) { 
				Log.error("StreamBufferRequest.write(): IOException on write: " + e.getLocalizedMessage());
				throw new ClientRuntimeException ("stream io exception", e);
			}
		}
	}
}
