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

import org.jredis.protocol.Command;
import org.jredis.protocol.Response;

/**
 * This basically extends {@link SyncProtocol} so that the response buffers
 * are not shared, so that it can be used in multi-threaded environments. 
 * @author  Joubin (alphazero@sensesay.net)
 * @version alpha.0, Apr 23, 2009
 * @since   alpha.0
 * 
 */

public class ConcurrentSyncProtocol extends SyncProtocol {
	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	
	// ------------------------------------------------------------------------
	// Super Extensions
	// ------------------------------------------------------------------------

//	@Override
//	protected ByteArrayOutputStream createRequestBufffer(Command cmd) {
//		return new ByteArrayOutputStream (PREFERRED_REQUEST_BUFFER_SIZE);
//	}
//	
//	protected Request createRequest (ByteArrayOutputStream buffer) {
////		sharedRequestObject.reset(buffer);
//		return new StreamBufferRequest (buffer);	
//	}

	@Override
	protected Response createStatusResponse(Command cmd) {
		return new SyncLineResponse (new byte[PREFERRED_LINE_BUFFER_SIZE], cmd, ValueType.STATUS);
	}
	
	@Override
	protected Response createBooleanResponse(Command cmd) {
		return new SyncLineResponse (new byte[PREFERRED_LINE_BUFFER_SIZE], cmd, ValueType.BOOLEAN);
	}
	@Override
	protected Response createStringResponse(Command cmd) {
		return new SyncLineResponse (new byte[PREFERRED_LINE_BUFFER_SIZE], cmd, ValueType.STRING);
	}
	
	@Override
	protected Response createNumberResponse(Command cmd /*, boolean isBigNum*/) {
		ValueType flavor = ValueType.NUMBER64;
		return new SyncLineResponse (new byte[PREFERRED_LINE_BUFFER_SIZE], cmd, flavor);
	}
	
	@Override
	protected Response createBulkResponse(Command cmd) {
		return new SyncBulkResponse (new byte[PREFERRED_LINE_BUFFER_SIZE], cmd);
	}
	
	@Override
	protected Response createMultiBulkResponse(Command cmd) {
		return new SyncMultiBulkResponse (new byte[PREFERRED_LINE_BUFFER_SIZE], cmd);
	}
}
