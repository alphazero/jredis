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

package org.jredis.ri.alphazero;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jredis.ClientRuntimeException;
import org.jredis.Command;
import org.jredis.connector.BulkResponse;
import org.jredis.connector.MultiBulkResponse;
import org.jredis.connector.Protocol;
import org.jredis.ProviderException;
import org.jredis.connector.Request;
import org.jredis.connector.Response;
import org.jredis.connector.ResponseStatus;
import org.jredis.connector.StatusResponse;
import org.jredis.connector.ValueResponse;
import org.jredis.ri.alphazero.support.Convert;


/**
 * [TODO: document me!]
 *
 * @version alpha.0, Apr 10, 2009
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @since   alpha.0
 * 
 */
public class SynchProtocol extends ProtocolBase {
	
	// ------------------------------------------------------------------------
	// Protocol Handler's Data Buffer (pseudo-Registers) specific attributes
	// ------------------------------------------------------------------------

	/** Preferred size of request data buffer */
	private static final int			PREFERRED_SIZE	= 1024;
	
	// ------------------------------------------------------------------------
	// SynchConnection's can use the same buffers again and again and ...
	// ------------------------------------------------------------------------
	
	/** Shared by <b>all</b> {@link Request} instances of this <b>non-thread-safe</b> {@link Protocol} implementation. */
	private final ByteArrayOutputStream sharedRequestBuffer;

	/** Shared by <b>all</b> {@link Response} instances of this <b>non-thread-safe</b> {@link Protocol} implementation. */
//	private final ExtByteArrayOutputStream sharedResponseBuffer;
	private final byte[]	sharedResponseBuffer;

	// ------------------------------------------------------------------------
	// Constructor(s)
	// ------------------------------------------------------------------------
	
	public SynchProtocol() {
		sharedRequestBuffer = new ByteArrayOutputStream (PREFERRED_SIZE);
		sharedResponseBuffer = new byte [1024];
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
	SynchLineResponse cache_synchLineResponse = null;
	@Override
	protected Response createStatusResponse(Command cmd) {
		if(null == cache_synchLineResponse)
			cache_synchLineResponse = new SynchLineResponse(cmd, ValueType.STATUS);
		else {
			cache_synchLineResponse.reset(cmd);
		}
		return cache_synchLineResponse;
//		return new SynchLineResponse(cmd);
	}
	@Override
	protected Response createBooleanResponse(Command cmd) {
		if(null == cache_synchLineResponse)
			cache_synchLineResponse = new SynchLineResponse(cmd, ValueType.BOOLEAN);
		else {
			cache_synchLineResponse.reset(cmd, ValueType.BOOLEAN);
		}
		return cache_synchLineResponse;
//		return new SynchLineResponse(cmd, ValueType.BOOLEAN);
	}
	@Override
	protected Response createStringResponse(Command cmd) {
		if(null == cache_synchLineResponse)
			cache_synchLineResponse = new SynchLineResponse(cmd, ValueType.STRING);
		else {
			cache_synchLineResponse.reset(cmd, ValueType.STRING);
		}
		return cache_synchLineResponse;
//		return new SynchLineResponse(cmd, ValueType.STRING);
	}
	@Override
	protected Response createNumberResponse(Command cmd /*, boolean isBigNum*/) {
		ValueType flavor = ValueType.NUMBER64;
//		if(isBigNum) flavor = ValueType.NUMBER64;
		if(null == cache_synchLineResponse)
			cache_synchLineResponse = new SynchLineResponse(cmd, ValueType.NUMBER64);
		else {
			cache_synchLineResponse.reset(cmd, flavor);
		}
		return cache_synchLineResponse;
//		return new SynchLineResponse(cmd, flavor);
	}
	
	SynchBulkResponse  cache_synchBulkResponse = null;
	@Override
	protected Response createBulkResponse(Command cmd) {
		if(null == cache_synchBulkResponse)
			cache_synchBulkResponse = new SynchBulkResponse(cmd);
		else {
			cache_synchBulkResponse.reset(cmd);
		}
		return cache_synchBulkResponse;
	}

	
	SynchMultiBulkResponse  cache_synchMultiBulkResponse = null;
	@Override
	protected Response createMultiBulkResponse(Command cmd) {
		if(null == cache_synchMultiBulkResponse)
			cache_synchMultiBulkResponse = new SynchMultiBulkResponse(cmd);
		else {
			cache_synchMultiBulkResponse.reset(cmd);
		}
		return cache_synchMultiBulkResponse;
	}
	
	// ------------------------------------------------------------------------
	// Inner Type
	// ========================================================================
	// ------------------------------------------------------------------------
	private enum ValueType {
		STATUS,
		BOOLEAN,
//		@Deprecated
//		NUMBER32,
		NUMBER64,
		STRING
	}

	// ------------------------------------------------------------------------
	// Inner Type
	// ============================================================ Response(s)
	// ------------------------------------------------------------------------
	/**
	 * Synchronous responses are guaranteed to be continigous chuncks (if the
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
	public abstract class SynchResponseBase extends ResponseSupport {

		final byte[]	buffer;
		int				offset;
		public SynchResponseBase(Command cmd, Type type) {
			super(cmd, type);
			buffer = sharedResponseBuffer;
			offset = 0;
		}
//		@Override
		protected void reset (Command cmd, Type type) {
//			super.reset(cmd, type);
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
		void readLine (InputStream in) {
			offset = 0;
			int readcnt = 0;
			int c = -1;
			int available = buffer.length - offset;  // offset=0 now
			try {
				while (available > 0 && (c = in.read(buffer, offset, available)) != -1) {
					offset += c; 
					available -= c;
					readcnt +=c;
					if(offset > 3 && buffer[offset-2]==(byte)13 && buffer[offset-1]==(byte)10){
						break;  // we're done
					}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
				throw new ClientRuntimeException ("IOEx while reading line for command " + cmd.code, e);
			}
		}
		/**
		 * Resets offset and reads bytes until CRLF is found.  Offset on find is the offset the subsequent
		 * element after \n.
		 * @param in
		 */
		void seekToCRLF (InputStream in){
			offset = 0;
			int c = -1;
			int available = buffer.length - offset;
			try {
				while (available > 0 && (c = in.read(buffer, offset, 1)) != -1) {
					offset += c; 
					if(offset > 2 && buffer[offset-2]==(byte)13 && buffer[offset-1]==(byte)10){
						break;  // we're done
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
		 * resets offset and reads bytes until it has a size line.
		 * @param in
		 * @return
		 */
		int readSize (InputStream in, boolean checkForError) {

			return readControlLine(in, checkForError, SIZE_BYTE);
//			seekToCRLF(in);
//			if(checkForError && (this.isError = buffer[0] == ProtocolBase.ERR_BYTE) == true) {
//				status = new ResponseStatus(ResponseStatus.Code.ERROR, new String(buffer, 1, offset-3));
//				didRead = true;
//				return -2;
//			}
//			if(buffer[0] != SIZE_BYTE) {
//				throw new ProviderException ("Bug?  Expecting status code for size");
//			}
//			status = ResponseStatus.STATUS_OK;
//			return Convert.getInt (buffer, 1, offset-3);
		}
		
		/**
		 * resets offset and reads bytes until it has a size line.
		 * @param in
		 * @return the count or -2 if control line was an error status
		 */
		int readCount (InputStream in, boolean checkForError) {
			return readControlLine(in, checkForError, COUNT_BYTE);
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
				didRead = true;
				return -2;
			}
			if(buffer[0] != ctlByte) {
				throw new ProviderException ("Bug?  Expecting status code for size");
			}
			status = ResponseStatus.STATUS_OK;
			return Convert.getInt (buffer, 1, offset-3);
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
			byte[] data = new byte[length];
			byte[] term = new byte[CRLF.length];
			
			int readcnt = -1;
			int offset = 0;

			while(offset < length){
				if((readcnt = in.read (data, offset, length-offset)) ==-1 ) throw new ClientRuntimeException("IO - read returned -1 -- problem");
				offset += readcnt;
			}
			if((readcnt = in.read (term, 0, CRLF.length)) != CRLF.length) { 
				throw new RuntimeException ("Only read " + readcnt + " bytes for CRLF!");
			}
			return data;
		}
	}
	
	// ------------------------------------------------------------------------
	// Inner Type
	// ============================================================ Response(s)
	// ------------------------------------------------------------------------
	public class SynchMultiBulkResponse extends SynchResponseBase implements MultiBulkResponse {

		/**  */
		List<byte[]>   datalist;
		
		public SynchMultiBulkResponse(Command cmd) {
			super(cmd, Type.MultiBulk);
		}
		protected void reset (Command cmd){
			super.reset(cmd, Type.Bulk);
//			this.cmd = cmd;
//			this.type = Type.Value;
			this.datalist = null;
		}

//		@Override
		public List<byte[]> getMultiBulkData() throws ClientRuntimeException, ProviderException {
			assertResponseRead();
			return datalist;
		}

//		@Override
		public void read(InputStream in) throws ClientRuntimeException, ProviderException {
			if(didRead) return;
			int count = super.readCount(in, true);
			if(status.isError()) {
				didRead = true;
				return;
			}
//			if(count != -2){
			if(count >= 0){
				datalist = new ArrayList<byte[]>(count);
				try {
					int size = -1;
					for(int i=0;i<count; i++){
						size = readSize(in, false);
						if(size > 0)
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
	public class SynchBulkResponse extends SynchResponseBase implements BulkResponse {
		/**  */
		byte[] data = null;

		public SynchBulkResponse(Command cmd) {
			super(cmd, Type.Bulk);
		}
		protected void reset (Command cmd){
			super.reset(cmd, Type.Bulk);
//			this.cmd = cmd;
//			this.type = Type.Value;
			this.data = null;
		}

//		@Override
		public byte[] getBulkData() {
			assertResponseRead();
			return data;
		}
//		@Override
		public void read(InputStream in) throws ClientRuntimeException, ProviderException {
			if(didRead) return;
			
			int size = super.readSize(in, true);
			if(size > 0){
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
	/**
	 *
	 * @author  Joubin Houshyar (alphazero@sensesay.net)
	 * @version alpha.0, 04/02/09
	 * @since   alpha.0
	 *
	 */
	public class SynchLineResponse extends SynchResponseBase implements StatusResponse, ValueResponse {
		private ValueType 	flavor;
//		private int				intValue;
		private String			stringValue;
		private long	longValue;
		private boolean	booleanValue;
		
//		/** used for status responses */
//		public SynchLineResponse(Command cmd) {
//			super(cmd, Type.Status);
//			flavor = ValueType.STATUS;
//		}
		public SynchLineResponse(Command cmd, ValueType flavor) {
			super(cmd, Type.Value);
			this.flavor = flavor;
		}
		protected void reset (Command cmd){
			super.reset(cmd, Type.Status);
//			this.cmd = cmd;
//			this.type = Type.Status;
			this.flavor = ValueType.STATUS;
		}
		protected void reset (Command cmd, ValueType flavor){
			super.reset(cmd, Type.Value);
//			this.cmd = cmd;
//			this.type = Type.Value;
			this.flavor = flavor;
		}
//		@Override
		public boolean getBooleanValue() throws IllegalStateException {
			if(flavor != ValueType.BOOLEAN) throw new IllegalStateException ("Response value type is " + flavor.name() + " not " + ValueType.BOOLEAN.name());
			return booleanValue;
		}
////		@Override
//		public int getIntValue() throws IllegalStateException {
//			if(flavor != ValueType.NUMBER32) throw new IllegalStateException ("Response value type is " + flavor.name() + " not " + ValueType.NUMBER32.name());
//			return intValue;
//		}
//		@Override
		public long getLongValue() throws IllegalStateException {
			if(flavor != ValueType.NUMBER64) throw new IllegalStateException ("Response value type is " + flavor.name() + " not " + ValueType.NUMBER64.name());
			return longValue;
		}
//		@Override
		public String getStringValue() throws IllegalStateException {
			if(flavor != ValueType.STRING) throw new IllegalStateException ("Response value type is " + flavor.name() + " not " + ValueType.STRING.name());
			return stringValue;
		}
		/**
		 * Its a synchronous connection (right?) so we'll never get anything more
		 * than a response to a command returning a bunch of bytes ending in CRLF.
		 * And that' what's we're looking for here.
		 */
//		@Override
		public void read(InputStream in) throws ClientRuntimeException, ProviderException {
			if(didRead) return;
			super.readLine (in);
			
			if((this.isError = buffer[0] == ProtocolBase.ERR_BYTE) == true) {
				status = new ResponseStatus(ResponseStatus.Code.ERROR, new String(buffer, 1, offset-3));
			}
			else {
				status = ResponseStatus.STATUS_OK;
				if(flavor != ValueType.STATUS){
					// TODO: verify for these!
					switch (flavor){
						case BOOLEAN:
							booleanValue = buffer[1]==49?true:false;
							break;
//						case NUMBER32:
//							intValue = Convert.getInt (buffer, 1, offset-3);
//							break;
						case NUMBER64:
//							longValue = Long.parseLong(new String(buffer, 1, offset-3));
							longValue = Convert.getLong (buffer, 1, offset-3);
							break;
						case STATUS:
							break;
						case STRING:
							stringValue = new String (buffer, 1, offset-3);
							break;
					}
				}
				else {
					status = ResponseStatus.STATUS_OK;
				}
			}
			didRead = true;
		}
	}
}
