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
	// Interface
	// ============================================================== Protocol
	// ------------------------------------------------------------------------
	
	// TODO: lets just forget about this multi-version nonsense.  
	// and get rid of version all together.
//	@Override
	public boolean isCompatibleWithVersion(String version) {
		return version.equals("0.09");
	}
	
//	@Override
	/* (non-Javadoc)
	 * @see org.jredis.connector.Protocol#createRequest(org.jredis.Command, byte[][])
	 */
	public Request createRequest(Command cmd, byte[]... args) throws ProviderException, IllegalArgumentException {
		
		ByteArrayOutputStream buffer = createRequestBufffer (cmd);

		try {
			switch (cmd.requestType) {
			case NO_ARG:
				// -------------------
				buffer.write(cmd.bytes);
				buffer.write(SPACE);
				buffer.write(CRLF);
				// -------------------
				break;

			case KEY:
				// -------------------
				buffer.write(cmd.bytes);
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[0], "key arg", ProviderException.class));
				buffer.write(CRLF);
				// -------------------
				break;

			case VALUE:
			{
				byte[] value = Assert.notNull(args[0], "value arg", ProviderException.class);				
				// -------------------
				buffer.write(cmd.bytes);
				buffer.write(SPACE);
				buffer.write(Convert.toBytes(value.length));
				buffer.write(CRLF);
				buffer.write(value);
				buffer.write(CRLF);
				// -------------------
			}
				break;

			case KEY_KEY:
			case KEY_NUM:
			case KEY_SPEC:
				// -------------------
				buffer.write(cmd.bytes);
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[0], "key arg", ProviderException.class));
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[1], "key2 arg", ProviderException.class));
				buffer.write(CRLF);
				// -------------------
				break;

			case KEY_NUM_NUM:
				// -------------------
				buffer.write(cmd.bytes);
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[0], "key arg", ProviderException.class));
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[1], "num_1 arg", ProviderException.class));
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[2], "num_2 arg", ProviderException.class));
				buffer.write(CRLF);
				// -------------------
				break;

			case KEY_NUM_NUM_OPTS:
				// -------------------
				buffer.write(cmd.bytes);
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[0], "key arg", ProviderException.class));
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[1], "num_1 arg", ProviderException.class));
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[2], "num_2 arg", ProviderException.class));
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[3], "opt args", ProviderException.class));
				buffer.write(CRLF);
				// -------------------
				break;
				
			case KEY_VALUE:
			{
				byte[] value = Assert.notNull(args[1], "value arg", ProviderException.class);
				// -------------------
				buffer.write(cmd.bytes);
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[0], "key arg", ProviderException.class));
				buffer.write(SPACE);
				buffer.write(Convert.toBytes(value.length));
				buffer.write(CRLF);
				buffer.write(value);
				buffer.write(CRLF);
				// -------------------
			}
			break;

			case KEY_IDX_VALUE:
			case KEY_KEY_VALUE:
			{
				byte[] value = Assert.notNull(args[2], "value arg", ProviderException.class);
				// -------------------
				buffer.write(cmd.bytes);
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[0], "key arg", ProviderException.class));
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[1], "index arg", ProviderException.class));
				buffer.write(SPACE);
				buffer.write(Convert.toBytes(value.length));
				buffer.write(CRLF);
				buffer.write(value);
				buffer.write(CRLF);
				// -------------------
			}
			break;
			
			case KEY_CNT_VALUE:
			{
				byte[] value = Assert.notNull(args[1], "value arg", ProviderException.class);
				// -------------------
				buffer.write(cmd.bytes);
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[0], "key arg", ProviderException.class));
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[2], "count arg", ProviderException.class));
				buffer.write(SPACE);
				buffer.write(Convert.toBytes(value.length));
				buffer.write(CRLF);
				buffer.write(value);
				buffer.write(CRLF);
				// -------------------
			}
			break;

			case MULTI_KEY:
			{
				int keycnt = args.length;
				// -------------------
				buffer.write(cmd.bytes);
				buffer.write(SPACE);
				for(int i=0;i<keycnt; i++){
					buffer.write(Assert.notNull(args[i], "key arg", ProviderException.class));
					buffer.write(SPACE);
				}
				buffer.write(CRLF);
				// -------------------
			}	
			break;
			
			case BULK_SET:
				Assert.isTrue(cmd == Command.MSET || cmd == Command.MSETNX || cmd == Command.RPUSHXAFTER, "Only MSET/NX/RPUSHXAFTER bulk commands are supported", NotSupportedException.class);

				byte[] setCmdLenBytes = Convert.toBytes(cmd.bytes.length);
				byte[] bulkSetLineCntBytes = Convert.toBytes(args.length+1);

				buffer.write(COUNT_BYTE);
				buffer.write(bulkSetLineCntBytes);
				buffer.write(CRLF);

				buffer.write(SIZE_BYTE);
				buffer.write(setCmdLenBytes);
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
			
			}
		}
		catch (Exception e) {
			throw new ProviderException("Problem writing to the buffer" + e.getLocalizedMessage());
		}
		return createRequest(buffer);
	}
	
//	@Override
	/* (non-Javadoc)
	 * @see org.jredis.connector.Protocol#createResponse(org.jredis.Command)
	 */
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

	// ------------------------------------------------------------------------
	// Inner Type
	// =============================================================== Request
	// ------------------------------------------------------------------------
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
//		@Override
		public void read(InputStream in) throws ClientRuntimeException, ProviderException {
			throw new ProviderException("Request.read is not supported by this class! [Apr 2, 2009]");
		}

		/**
		 * Writes the entire content of the message to the output stream and flushes it.
		 * 
		 * @param out the stream to write the Request message to.
		 */
//		@Override
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
