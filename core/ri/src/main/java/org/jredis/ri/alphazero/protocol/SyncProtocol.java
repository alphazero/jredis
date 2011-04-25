/*
 *   Copyright 2009 - 2011 Joubin Houshyar
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

//import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import org.jredis.ClientRuntimeException;
import org.jredis.ProviderException;
import org.jredis.connector.ConnectionReset;
import org.jredis.protocol.BulkResponse;
import org.jredis.protocol.Command;
import org.jredis.protocol.MultiBulkResponse;
import org.jredis.protocol.Protocol;
import org.jredis.protocol.Request;
import org.jredis.protocol.Response;
import org.jredis.protocol.ResponseStatus;
import org.jredis.protocol.StatusResponse;
import org.jredis.protocol.ValueResponse;
import org.jredis.ri.alphazero.connection.UnexpectedEOFException;
import org.jredis.ri.alphazero.support.Convert;
import org.jredis.ri.alphazero.support.Log;


/**
 * [TODO: document me!]
 *
 * @version alpha.0, Apr 10, 2009
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @since   alpha.0
 * 
 */
public class SyncProtocol extends ProtocolBase {
	
	// ------------------------------------------------------------------------
	// Protocol Handler's Data Buffer (pseudo-Registers) specific attributes
	// ------------------------------------------------------------------------

	/** Preferred size of request data buffer */
	protected static final int			PREFERRED_REQUEST_BUFFER_SIZE	= 1024 * 48;
	
	/** Initial size of the shared line buffer */
	protected static final int			PREFERRED_LINE_BUFFER_SIZE = 128;
	
	/**  */
	protected static final int			INPUT_STREAM_BUFFER_SIZE = 1024 * 128;
	
	// ------------------------------------------------------------------------
	// SyncConnection's can use the same buffers again and again and ...
	// ------------------------------------------------------------------------
	
	/** Shared by <b>all</b> {@link Request} instances of this <b>non-thread-safe</b> {@link Protocol} implementation. */
	private final ByteArrayOutputStream sharedRequestBuffer;

	/** Shared {@link Request} instance of this <b>non-thread-safe</b> {@link Protocol} implementation. */
	private final StreamBufferRequest   sharedRequestObject;

	/** Shared by <b>all</b> {@link Response} instances of this <b>non-thread-safe</b> {@link Protocol} implementation. */
	private final byte[]	sharedResponseBuffer;

	// ------------------------------------------------------------------------
	// Constructor(s)
	// ------------------------------------------------------------------------
	
	public SyncProtocol() {
		sharedRequestBuffer = new ByteArrayOutputStream (PREFERRED_REQUEST_BUFFER_SIZE);
		sharedRequestObject = new StreamBufferRequest (sharedRequestBuffer);
		sharedResponseBuffer = new byte [PREFERRED_LINE_BUFFER_SIZE];

	}
	
	// ------------------------------------------------------------------------
	// Super Extensions
	// ------------------------------------------------------------------------
	/**
	 * @param cmd {@link Command} for this request - potentially useful for 
	 * optimizing buffers.
	 * 
	 * @return the shared instance of {@link ByteArrayOutputStream} that
	 * is used <b>by all requests</b> created by this {@link Protocol} implementation.
	 */
	@Override
	protected ByteArrayOutputStream createRequestBufffer(Command cmd) {
		sharedRequestBuffer.reset();
		return sharedRequestBuffer;
	}
	
	protected Request createRequest (ByteArrayOutputStream buffer) {
//		sharedRequestObject.reset(buffer);
		return sharedRequestObject;
	}

	SyncLineResponse cache_syncLineResponse = null;
	@Override
	protected Response createStatusResponse(Command cmd) {
		if(null == cache_syncLineResponse)
			cache_syncLineResponse = new SyncLineResponse(cmd, ValueType.STATUS);
		else {
			cache_syncLineResponse.reset(cmd);
		}
		return cache_syncLineResponse;
	}
	@Override
	protected Response createBooleanResponse(Command cmd) {
		if(null == cache_syncLineResponse)
			cache_syncLineResponse = new SyncLineResponse(cmd, ValueType.BOOLEAN);
		else {
			cache_syncLineResponse.reset(cmd, ValueType.BOOLEAN);
		}
		return cache_syncLineResponse;
	}
	@Override
	protected Response createStringResponse(Command cmd) {
		if(null == cache_syncLineResponse)
			cache_syncLineResponse = new SyncLineResponse(cmd, ValueType.STRING);
		else {
			cache_syncLineResponse.reset(cmd, ValueType.STRING);
		}
		return cache_syncLineResponse;
	}
	@Override
	protected Response createNumberResponse(Command cmd /*, boolean isBigNum*/) {
		ValueType flavor = ValueType.NUMBER64;
		if(null == cache_syncLineResponse)
			cache_syncLineResponse = new SyncLineResponse(cmd, flavor);
		else {
			cache_syncLineResponse.reset(cmd, flavor);
		}
		return cache_syncLineResponse;
	}
	
	SyncBulkResponse  cache_syncBulkResponse = null;
	@Override
	protected Response createBulkResponse(Command cmd) {
		if(null == cache_syncBulkResponse)
			cache_syncBulkResponse = new SyncBulkResponse(cmd);
		else {
			cache_syncBulkResponse.reset(cmd);
		}
		return cache_syncBulkResponse;
	}

	
	SyncMultiBulkResponse  cache_syncMultiBulkResponse = null;
	@Override
	protected Response createMultiBulkResponse(Command cmd) {
		if(null == cache_syncMultiBulkResponse)
			cache_syncMultiBulkResponse = new SyncMultiBulkResponse(cmd);
		else {
			cache_syncMultiBulkResponse.reset(cmd);
		}
		return cache_syncMultiBulkResponse;
	}
	
	// ------------------------------------------------------------------------
	// Inner Type
	// ========================================================================
	// ------------------------------------------------------------------------
	protected enum ValueType {
		STATUS,
		BOOLEAN,
		NUMBER64,
		STRING
	}

	// ------------------------------------------------------------------------
	// Inner Type
	// ============================================================ Response(s)
	// ------------------------------------------------------------------------
	/**
	 * Synchronous responses are guaranteed to be contiguous chunks (if the
	 * client of this class is respecting its contract) -- meaning, it can go
	 * ahead and read as much as it can in its first read without busy waiting
	 * or reading one byte at a time.  After that initial read, specialized
	 * response types can go head and read/parse as they please.  
	 *
	 * @author  Joubin Houshyar (alphazero@sensesay.net)
	 * @version alpha.0, 04/02/09
	 * @since   alpha.0
	 * 
	 */
	public abstract class SyncResponseBase extends ResponseSupport {

		byte[]		buffer;
		int			offset;
		
		protected SyncResponseBase(byte[] buffer, Command cmd, Type type) {
			super(cmd, type);
			this.buffer = buffer;
			offset = 0;
		}

		protected void reset (Command cmd, Type type) {
			this.cmd = cmd;
			this.type = type;
			offset = 0;
			didRead = false;
			status = null;
			isError = false;
		}
		/**
		 * Makes blocking calls to input stream until it gets crlf. Should not be
		 * used for size/count lines.
		 * @param in
		 */
		void readSingleLineResponse (InputStream in) {
			offset = 0;
			int c = -1;
			int available = buffer.length;
			try {
				while ((c = in.read(buffer, offset, 1)) != -1) {
					offset += c; 
					available -= c;
					if(offset > 2 && buffer[offset-2]==(byte)13 && buffer[offset-1]==(byte)10){
						break;  // we're done
					}
					if(available == 0) {
						byte[] newbuff = new byte[buffer.length * 2];
						System.arraycopy(buffer, 0, newbuff, 0, buffer.length);
						buffer = newbuff;
						available = buffer.length - offset;
					}
				}
				if(c == -1) {
					Log.error("-1 read count in readLine() while reading response line.");
					throw new UnexpectedEOFException ("Unexpected EOF (read -1) in readLine.  Command: " + cmd.code);
				}
				if((this.isError = buffer[0] == ProtocolBase.ERR_BYTE) == true) 
					status = new ResponseStatus(ResponseStatus.Code.ERROR, new String(buffer, 1, offset-3));
				else 
					status = ResponseStatus.STATUS_OK;
			}
			catch (SocketException e) {
				// on connection reset
				throw new ConnectionReset("SocketException in readLine.  Command: " + cmd.code, e);
			}
			catch (IOException e) {
				e.printStackTrace();
				throw new ClientRuntimeException ("IOException in readLine.  Command: " + cmd.code, e);
			}
		}
	}
	// ------------------------------------------------------------------------
	// Inner Type
	// ============================================================ Response(s)
	// ------------------------------------------------------------------------
	/**
	 *
	 * @author  Joubin Houshyar (alphazero@sensesay.net)
	 * @version alpha.0, 04/02/09
	 * @since   alpha.0
	 *
	 */
	public class SyncLineResponse extends SyncResponseBase implements StatusResponse, ValueResponse {
		private ValueType 	flavor;
		private String		stringValue;
		private long		longValue;
		private boolean		booleanValue;
		
		private SyncLineResponse(Command cmd, ValueType flavor) {
			this (sharedResponseBuffer, cmd, flavor);
		}
		/**
		 * @param bs
		 * @param cmd
		 * @param status
		 */
		public  SyncLineResponse(byte[] buff, Command cmd, ValueType flavor) {
			super(buff, cmd, Type.Value);
			this.flavor = flavor;
		}
		protected void reset (Command cmd){
			super.reset(cmd, Type.Status);
			this.flavor = ValueType.STATUS;
		}
		protected void reset (Command cmd, ValueType flavor){
			super.reset(cmd, Type.Value);
			this.flavor = flavor;
		}

		@Override
		public boolean getBooleanValue() throws IllegalStateException {
			if(flavor != ValueType.BOOLEAN) throw new IllegalStateException ("Response value type is " + flavor.name() + " not " + ValueType.BOOLEAN.name());
			return booleanValue;
		}
		
		@Override
		public long getLongValue() throws IllegalStateException {
			if(flavor != ValueType.NUMBER64) throw new IllegalStateException ("Response value type is " + flavor.name() + " not " + ValueType.NUMBER64.name());
			return longValue;
		}
		@Override
		public String getStringValue() throws IllegalStateException {
			if(flavor != ValueType.STRING) throw new IllegalStateException ("Response value type is " + flavor.name() + " not " + ValueType.STRING.name());
			return stringValue;
		}
		
		/**
		 * Delegates the io handling to the base class and parses the value reponse
		 * based on the data flavor.
		 */
		@Override
		public void read(InputStream in) throws ClientRuntimeException, ProviderException {
			if(didRead) return;
			
//			BufferedInputStream bin = new BufferedInputStream(in, 1024 * 48);
//			super.readSingleLineResponse (bin);

			super.readSingleLineResponse (in);
			didRead = true;

			// TODO: not quite happy with the access to raw buffer here -- a method call would
			// slow things done but this is fragile in light of possible future code re-factoring.
			if(!status.isError() && flavor != ValueType.STATUS){
				switch (flavor){
				case BOOLEAN:
					booleanValue = buffer[1]==49?true:false;
					break;
				case NUMBER64:
					longValue = Convert.toLong (buffer, 1, offset-3);
					break;
				case STATUS:
					break;
				case STRING:
					stringValue = new String (buffer, 1, offset-3);
					break;
				}
			}
		}
	}	
	// ------------------------------------------------------------------------
	// Inner Type
	// ============================================================ Response(s)
	// ------------------------------------------------------------------------
	/**
	 * Abstract base for all multiline responses (as of now, Bulk and MultBulk).
	 *
	 * @author  Joubin (alphazero@sensesay.net)
	 * @version alpha.0, Sep 2, 2009
	 * @since   alpha.0
	 * TODO: Use overflow buffers to increase read efficiency.
	 */
	public abstract class SyncMultiLineResponseBase extends SyncResponseBase {

        protected SyncMultiLineResponseBase (byte[] buffer, Command cmd, Type type) {
	        super(buffer, cmd, type);
        }
		
		/**
		 * @param in
		 */
		void seekToCRLF (InputStream in){
			offset = 0;
			int c = -1;
			int available = buffer.length;
			try {
				while ((c = in.read(buffer, offset, 1)) != -1) {
					offset += c;
					available -= c;
					if(offset > 2 && buffer[offset-2]==(byte)13 && buffer[offset-1]==(byte)10){
						break;  // we're done
					}
					if(available == 0) {
						byte[] newbuff = new byte[buffer.length * 2];
						System.arraycopy(buffer, 0, newbuff, 0, buffer.length);
						buffer = newbuff;
						available = buffer.length - offset;
					}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
				throw new ClientRuntimeException ("IOEx while reading line for command " + cmd.code, e);
			}
			
			if(c==-1) throw new ClientRuntimeException ("in.read returned -1");
		}
		/**
		 * @param in
		 * @param checkForError
		 * @param ctlByte
		 * @return
		 */
		int readControlLine (InputStream in, boolean checkForError, byte ctlByte){
			seekToCRLF(in);
			if(checkForError && (this.isError = buffer[0] == ProtocolBase.ERR_BYTE) == true) {
				status = new ResponseStatus(ResponseStatus.Code.ERROR, new String(buffer, 1, offset-3));
				didRead = true;  // we're done - error's are only one line
				return -2;
			}
			if(buffer[0] != ctlByte) {
				throw new ProviderException ("Bug?  Expecting status code for size/count");
			}
			status = ResponseStatus.STATUS_OK;
			return Convert.toInt (buffer, 1, offset-3);
		}

		/**
		 * Will read up expected bulkdata bytes from the input stream.  Routine will
		 * also read in the last two bytes and will check that they are indeed CRLF.
		 *  
		 * @param in the stream to read from.
		 * @param length expected bulk data length (NOT including the trailing CRLF).  
		 * @return a byte[] of length.
		 * @throws IOException 
		 * @throws IllegalArgumentException if could not read the bulk data
		 */
		public final byte[] readBulkData (InputStream in, int length)
			throws IOException, RuntimeException
		{
			byte[] data = new byte[length]; // TODO: optimize me
//			byte[] term = new byte[CRLF.length]; // FIX: http://github.com/alphazero/jredis/issues#issue/5 -- N/A
			
			int readcnt = -1;
			int offset = 0;

			while(offset < length){
				if((readcnt = in.read (data, offset, length-offset)) ==-1 ) throw new ClientRuntimeException("IO - read returned -1 -- problem");
				offset += readcnt;
			}
			// FIX: http://github.com/alphazero/jredis/issues#issue/5 -- BEGIN
			for(int i=0; i<CRLF_LEN; i++){
				if (in.read() == -1){
					throw new RuntimeException ("read got EOF (-1) while consuming the " +(i+1)+ "-th byte of CRLF bytes!");
				}
			}
//			if((readcnt = in.read (term, 0, CRLF.length)) != CRLF.length) { 
//				throw new RuntimeException ("Only read " + readcnt + " bytes for CRLF!");
//			}
			// http://github.com/alphazero/jredis/issues#issue/5 -- END
			return data;
		}
	}
	// ------------------------------------------------------------------------
	// Inner Type
	// ============================================================ Response(s)
	// ------------------------------------------------------------------------
	public class SyncBulkResponse extends SyncMultiLineResponseBase implements BulkResponse {
		/**  */
		byte[] data = null;

		/**
		 * Uses the sharedResponseBuffer for reading of the response control line.
		 * @param cmd
		 */
		private SyncBulkResponse(Command cmd) {
			this (sharedResponseBuffer, cmd);
		}

		public SyncBulkResponse(byte[] buff, Command cmd) {
			super (buff, cmd, Type.Bulk);
		}
		
		protected void reset (Command cmd){
			super.reset(cmd, Type.Bulk);
			this.data = null;
		}

		@Override
		public byte[] getBulkData() {
			assertResponseRead();
			return data;
		}

		@Override
		public void read(InputStream in) throws ClientRuntimeException, ProviderException {
			if(didRead) return;

//			BufferedInputStream bin = new BufferedInputStream(in, INPUT_STREAM_BUFFER_SIZE);
			int size = readControlLine (in, true, SIZE_BYTE);

			if(!status.isError() && size >= 0){
				try {
					data = super.readBulkData(in, size);
				}
				catch (IllegalArgumentException bug){ 
					throw new ProviderException ("Bug: in converting the bulk data length bytes", bug);
				}
				catch (IOException problem) {
					throw new ClientRuntimeException ("Problem: reading the bulk data bytes", problem);
				}
				catch (RuntimeException bug) {
					throw new ProviderException ("Bug: reading the bulk data bytes.  expecting " + size + " bytes.", bug);
				}
			}
			didRead = true;
			return;
		}
	}
	// ------------------------------------------------------------------------
	// Inner Type
	// ============================================================ Response(s)
	// ------------------------------------------------------------------------
	public class SyncMultiBulkResponse extends SyncMultiLineResponseBase implements MultiBulkResponse {

		/**  */
		List<byte[]>   datalist;
		
		/**
		 * @param cmd
		 */
		private SyncMultiBulkResponse(Command cmd) {
			this (sharedResponseBuffer, cmd);
		}

		public SyncMultiBulkResponse(byte[] buff, Command cmd) {
			super (buff, cmd, Type.MultiBulk);
		}

		protected void reset (Command cmd){
			super.reset(cmd, Type.Bulk);
			this.datalist = null;
		}

		@Override
		public List<byte[]> getMultiBulkData() throws ClientRuntimeException, ProviderException {
			assertResponseRead();
			return datalist;
		}

		@Override
		public void read(InputStream in) throws ClientRuntimeException, ProviderException {
			if(didRead) return;
			
//			BufferedInputStream bin = new BufferedInputStream(in, 1024);
			int count = super.readControlLine (in, true, COUNT_BYTE);
			
			if(!status.isError() && count >= 0){
				datalist = new ArrayList<byte[]>(count);
				try {
					int size = -1;
					for(int i=0;i<count; i++){
						size = readControlLine(in, false, SIZE_BYTE);

						if(size >= 0)
							datalist.add (super.readBulkData(in, size));
						else
							datalist.add(null);
					}
				}
				catch (IllegalArgumentException bug){ 
					throw new ProviderException ("Bug: in converting the bulk data length bytes", bug);
				}
				catch (IOException problem) {
					throw new ClientRuntimeException ("Problem: reading the bulk data bytes", problem);
				}
				catch (RuntimeException bug) {
					throw new ProviderException ("Bug: reading the multibulk data bytes.", bug);
				}
			}
			didRead = true;
			return;
		}
	}
}
