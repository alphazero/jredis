/*
 *   Copyright 2009-2011 Joubin Houshyar
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

package org.jredis.ri.alphazero.connection;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jredis.ClientRuntimeException;
import org.jredis.ProviderException;
import org.jredis.RedisException;
import org.jredis.protocol.Command;
import org.jredis.protocol.Request;
import org.jredis.protocol.Response;
import org.jredis.ri.alphazero.support.Signal;

/**
 * An implementation of {@link Future} for parameteric <code>T</code> type {@link Response}
 * used for processing of pipelined responses from the server.
 * <p>
 * Note that this implementation does NOT support canceling of {@link Request}s.
 * 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Sep 7, 2009
 * @since   alpha.0
 * 
 */
public class PendingRequest implements Future<Response> {

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	/** Used to signal completion of the request */
//	final private BooleanLatch completion = new BooleanLatch();
	final Signal completion = new Signal();

	/** awaited response */
	Response response;

	/** Pending command */
	final Command cmd;

	/** true if response processor encountered exceptions */
	private boolean excepted = false;
	
	/** if {@link PendingRequest#excepted} is true, this will be set to the cause. */
	private ClientRuntimeException cre = null;
	
	final byte[][] args;
	// ------------------------------------------------------------------------
	// constructor(s)
	// ------------------------------------------------------------------------
	public PendingRequest(Command cmd){
		this.cmd = cmd;
		this.args = null;
	}
	
	public PendingRequest(Command cmd, byte[]... args){
		this.cmd = cmd;
		this.args = args;
	}
	
	// ------------------------------------------------------------------------
	// package scoped methods used by request processors
	// ------------------------------------------------------------------------
	final Command getCommand () {
		return cmd;
	}

	/**
	 * Signals completion without error.
	 * <p>
	 * Sets the response, which also signals the completion of this {@link Future} 
	 * object.  When this method is invoked, a call to {@link PendingRequest#get()}
	 * will immediately return with the response.
	 * @param response
	 */
	final void setResponse(Response response){
		this.response = response;
		this.completion.signal();
	}

	/**
	 * Signals completion with error -- response will be null
	 * @param cre
	 */
	final void setCRE (ClientRuntimeException cre){
		this.cre = cre;
		excepted = true;
		setResponse(null);
//		this.completion.signal();
	}
	
	
	/**
	 * Determines if a completed request encountered errors and will throw an {@link ExecutionException} wrapping 
	 * the original cause.  Called by the {@link PendingRequest#get} methods.
	 * @throws ExecutionException
	 */
	private final void checkStatus () throws ExecutionException 
	{
		// check for runtime or provider exceptions
		if(excepted) {
			if(cre != null) {
				if(cre instanceof ProviderException)
					throw new ExecutionException ("Provider Exception", cre);
				else
					throw new ExecutionException ("Client Runtime Exception", cre);
			}
			else {
				throw new ExecutionException ("Bug -- Request processing encountered exceptions but CRE is null", new ProviderException("unknown cause"));
			}
		}
		// check for Redis Errors
		if(response.isError())
			throw new ExecutionException("Redis Exception on ["+cmd.name()+"] " + response.getStatus().message(), new RedisException(cmd, response.getStatus().message()));
	}

	// ------------------------------------------------------------------------
	// Interface: Future<Response>
	// ------------------------------------------------------------------------
	
	/* (non-Javadoc) @see java.util.concurrent.Future#get() */
	@Override
	public Response get () throws InterruptedException, ExecutionException {
		completion.await();

		checkStatus();
		return response;
	}

	/* (non-Javadoc) @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit) */
	@Override
	public Response get (long timeout, TimeUnit unit)
	throws InterruptedException, ExecutionException, TimeoutException 
	{
		if(!completion.await(timeout, unit))
			throw new TimeoutException();
		
		checkStatus();
		return response;
	}

	/**  
	 * Pipeline does not support canceling of requests -- will always return false.
	 * @see java.util.concurrent.Future#cancel(boolean) 
	 */
	@Override
	public boolean cancel (boolean mayInterruptIfRunning) { return false; }

	/**  
	 * Pipeline does not support canceling of requests -- will always return false.
	 * @see PendingRequest#cancel(boolean)
	 * @see java.util.concurrent.Future#isCancelled()
	 */
	@Override
	public boolean isCancelled () { return false; }


	/* (non-Javadoc) @see java.util.concurrent.Future#isDone() */
	@Override
	public boolean isDone () { 
		return completion.isSignalled(); 
	}
}