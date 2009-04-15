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
import java.io.OutputStream;
import java.net.SocketException;

import org.jredis.ClientRuntimeException;
import org.jredis.Command;
import org.jredis.connector.Protocol;
import org.jredis.connector.ProviderException;
import org.jredis.connector.Request;
import org.jredis.connector.Response;
import org.jredis.connector.ResponseStatus;
import org.jredis.ri.alphazero.support.Assert;
import org.jredis.ri.alphazero.support.Convert;


/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
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
	
	// ------------------------------------------------------------------------
	// Protocol Revision specific consts
	// ------------------------------------------------------------------------
	protected ProtocolBase () {}
	
	// ------------------------------------------------------------------------
	// Interface
	// ============================================================== Protocol
	// ------------------------------------------------------------------------
	
//	@Override
	public boolean isCompatibleWithVersion(String version) {
		return version.equals("0.09");
	}
	
//	@Override
	public Request createRequest(Command cmd, byte[]... args) throws ProviderException, IllegalArgumentException {
		
		ByteArrayOutputStream buffer = createRequestBufffer (cmd);

		try {
			switch (cmd){
			/* -------------------- no arg commands <CMD> */
			/* -------------------- 1 CRLF line */
			case PING:
			case QUIT:
			case FLUSHALL:
			case FLUSHDB:
			case INFO:
			case RANDOMKEY:
			case SHUTDOWN:
			case DBSIZE:
			case BGSAVE:
			case SAVE:
			case LASTSAVE:
				// -------------------
				buffer.write(cmd.bytes);
				buffer.write(SPACE);
				buffer.write(CRLF);
				// -------------------
				break;

			case AUTH:
			case SELECT:
			case KEYS:
			case TYPE:
			case INCR:
			case DECR:
			case GET:
			case DEL:
			case EXISTS:
			case SMEMBERS:
			case SCARD:
			case LLEN:
			case LPOP:
			case RPOP:
				// -------------------
				buffer.write(cmd.bytes);
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[0], "key arg", ProviderException.class));
				buffer.write(CRLF);
				// -------------------
				break;

			case RENAME:	
			case RENAMENX:
			case INCRBY:  // 2nd key is actually a number o/c
			case DECRBY:  // 2nd key is actually a number o/c
			case LINDEX:  // 2nd key is actually a number o/c
			case MOVE:
			case EXPIRE:
			case SORT:
				// -------------------
				buffer.write(cmd.bytes);
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[0], "key arg", ProviderException.class));
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[1], "key2 arg", ProviderException.class));
				buffer.write(CRLF);
				// -------------------
				break;

			case LTRIM:
			case LRANGE:
				// -------------------
				buffer.write(cmd.bytes);
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[0], "key arg", ProviderException.class));
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[1], "from arg", ProviderException.class));
				buffer.write(SPACE);
				buffer.write(Assert.notNull(args[2], "to arg", ProviderException.class));
				buffer.write(CRLF);
				// -------------------
				break;

			case SET:
			case SETNX:
			case SADD:
			case SREM:
			case RPUSH:
			case LPUSH:
			case SISMEMBER:
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

			case LSET:
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
			
			case LREM:
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

			case SINTER: /* sinter key [key1 [key2 [...]]] */
			case SINTERSTORE:
			case MGET:
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
			}
		}
		catch (Exception e) {
			throw new ProviderException("Problem writing to the buffer" + e.getLocalizedMessage());
		}
//		return new SimpleRequest (buffer.toByteArray(), buffer.size());
		return new StreamBufferRequest (buffer);
	}

//	@Override
	public Response createResponse(Command cmd) throws ProviderException, ClientRuntimeException {

		Response response = null;
		switch (cmd) {
		/* ---------------------------------- Commands that close the connection without response */
		case QUIT:     		// closes connection
		case SHUTDOWN:		// closes connection
			response = new VirtualResponse(ResponseStatus.STATUS_CIAO);
			break;

			/* ------------------------------ Status   Responses ----------------------------------- */
		case PING:          
		case AUTH:			
		case RENAME:
		case SET:			
		case BGSAVE:
		case SAVE:
		case LPUSH:
		case RPUSH:
		case LSET:
		case LTRIM:
		case SINTERSTORE:
		case FLUSHALL: 		
		case FLUSHDB:		
		case SELECT:		
			response = createStatusResponse (cmd);
			break;
			/* ------------------------------ Value String    Responses ----------------------------------- */
		case RANDOMKEY:	    
		case TYPE:
			response = createStringResponse (cmd);
			break;

			/* ------------------------------ Value Boolean    Responses ----------------------------------- */
		case SETNX:			
		case EXISTS:
		case DEL:			
		case RENAMENX:
		case SADD:			
		case SISMEMBER:
		case SREM:
		case EXPIRE:
		case MOVE:			
			response = createBooleanResponse(cmd);
			break;

			/* ----------- TODO: LONG'EM-------- Value number    Responses ----------------------------------- */
//		case INCR:	    	
//		case INCRBY:	     
//		case DECR:	    	 
//		case DECRBY:	     
//		case SCARD:	    	 
//		case LLEN:	    	 
//		case LREM:

//			response = createNumberResponse (cmd, false);
//			break;

			/* ------------------------------ Value  BIG number    Responses ----------------------------------- */
			// -- moving all to long ..
		case INCR:	    	
		case INCRBY:	     
		case DECR:	    	 
		case DECRBY:	     
		case SCARD:	    	 
		case LLEN:	    	 
		case LREM:
			// -- moving all to long ..
		case DBSIZE:
		case LASTSAVE:
			response = createNumberResponse (cmd, true);
			break;

			/* ------------------------------ Bulk      Responses ----------------------------------- */
		case GET:
		case KEYS:
		case INFO:
		case LPOP:
		case RPOP:
		case LINDEX:
			response = createBulkResponse (cmd);
			break;
			/* ------------------------------ MultiBulk Responses ----------------------------------- */
		case MGET:
		case LRANGE:
		case SINTER:
		case SMEMBERS:
		case SORT:
			response = createMultiBulkResponse (cmd);
			break;

		default:
			throw new ProviderException ("createResponse() for Command " + cmd.code + " not yet implemented!");
		}

		return response;
	
	}


	// ------------------------------------------------------------------------
	// Extension Points
	// ------------------------------------------------------------------------
	
	protected abstract ByteArrayOutputStream createRequestBufffer(Command cmd);
	protected abstract Response createMultiBulkResponse(Command cmd) ;
	protected abstract Response createBulkResponse(Command cmd) ;
	protected abstract Response createNumberResponse(Command cmd, boolean bigNum) ;
	protected abstract Response createBooleanResponse(Command cmd) ;
	protected abstract Response createStringResponse(Command cmd) ;
	protected abstract Response createStatusResponse(Command cmd);

	// ------------------------------------------------------------------------
	// Inner Type
	// ============================================================ Response(s)
	// ------------------------------------------------------------------------
	/**
	 * Base for all responses.  Responsible for reading and determining status.
	 *
	 * @author  Joubin Houshyar (alphazero@sensesay.net)
	 * @version alpha.0, 04/02/09
	 * @since   alpha.0
	 * 
	 */
	public abstract static class ResponseSupport implements Response {

		// ------------------------------------------------------------------------
		// Properties and fields
		// ------------------------------------------------------------------------
		protected Type				type;
		protected ResponseStatus	status;
		protected Command 			cmd;
		protected boolean 			didRead = false;
		protected boolean 			isError = false;
		
		// ------------------------------------------------------------------------
		// Constructor
		// ------------------------------------------------------------------------
		public ResponseSupport (Command cmd, Type type) {
			this.type = type;
			this.cmd = cmd;
		}
		// ------------------------------------------------------------------------
		// Internal ops
		// ------------------------------------------------------------------------
		/** called by child classes to indicate if & when their read operation has completed */
		protected final boolean didRead (boolean value) { return didRead = value;}
		
		/** a bit aggressive but to force out the little bugs .. */
		protected final void assertResponseRead () {
			if(!didRead) throw new ProviderException ("Response has not been read yet! -- whose bad?");
		}
		
		// ------------------------------------------------------------------------
		// Interface
		// ------------------------------------------------------------------------
		
//		@Override
		public boolean didRead() { return didRead;  }

//		@Override
		public ResponseStatus getStatus() {return status; }
		
//		@Override
		public Type getType() { return type;}

//		@Override
		public boolean isError() {
			assertResponseRead(); 
			return isError; 
		}

//		@Override
		public void write(OutputStream out) throws ClientRuntimeException, ProviderException {
			throw new RuntimeException ("Message.write not implemented! [Apr 10, 2009]");
		}
//		protected void reset(Command cmd2, Type type2) {
//			cmd = cmd2;
//			type = type2;
//			didRead = false;
//			status = null;
//			isError = false;
//		}
	}
	
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
		final ByteArrayOutputStream buffer;
		/**
		 * @param buffer
		 */
		public StreamBufferRequest (ByteArrayOutputStream	buffer) {
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
				buffer.writeTo (out);
				out.flush();
			}
			catch (SocketException e){ throw new ClientRuntimeException ("socket exception", e);}
			catch (IOException e) { throw new ClientRuntimeException ("stream io exception", e);}
		}
	}
}
