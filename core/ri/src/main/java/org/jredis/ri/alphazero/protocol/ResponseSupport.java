package org.jredis.ri.alphazero.protocol;

import java.io.OutputStream;
import org.jredis.ClientRuntimeException;
import org.jredis.ProviderException;
import org.jredis.protocol.Command;
import org.jredis.protocol.Response;
import org.jredis.protocol.ResponseStatus;

/**
	 * Base for all responses.  Responsible for reading and determining status.
	 *
	 * @author  Joubin Houshyar (alphazero@sensesay.net)
	 * @version alpha.0, 04/02/09
	 * @since   alpha.0
	 * 
	 */
	public abstract class ResponseSupport implements Response {

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
		
		@Override
		public boolean didRead() { return didRead;  }

		@Override
		public ResponseStatus getStatus() {return status; }
		
		@Override
		public Type getType() { return type;}

		@Override
		public boolean isError() {
			assertResponseRead(); 
			return isError; 
		}

		@Override
		public void write(OutputStream out) throws ClientRuntimeException, ProviderException {
			throw new RuntimeException ("Message.write not implemented! [Apr 10, 2009]");
		}
	}