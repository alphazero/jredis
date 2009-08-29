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

package org.jredis.protocol;

/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 * 
 */
public final class ResponseStatus {

	/** Status code enum -- error or ok */
	public enum Code { OK, ERROR, CIAO }
	
	/** 
	 * Hopefully we have many none error response statuses 
	 * and we don't want to keep instantiating them.  
	 * Use this singleton instance for non-error status. 
	 */
	public static final ResponseStatus STATUS_OK = new ResponseStatus (ResponseStatus.Code.OK);

	public static final ResponseStatus	STATUS_CIAO	= new ResponseStatus (ResponseStatus.Code.CIAO);

	/**  */
	private final Code	code;
	/**  */
	private		  String msg;

	/**
	 * @param code
	 */
	public ResponseStatus(ResponseStatus.Code code) {
		this.code = code;
		this.msg = "";
	}

	/**
	 * @param code
	 * @param msg
	 */
	public ResponseStatus(ResponseStatus.Code code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public ResponseStatus.Code code() { return code; }
	public String message() {return msg; }
	public boolean isError () { return this.code==Code.ERROR; }
}