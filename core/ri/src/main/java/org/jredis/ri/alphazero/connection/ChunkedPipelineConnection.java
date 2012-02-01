/*
 *   Copyright 2009-2012 Joubin Houshyar
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

import static org.jredis.ri.alphazero.protocol.ProtocolBase.ASCII_ZERO;
import static org.jredis.ri.alphazero.protocol.ProtocolBase.COUNT_BYTE;
import static org.jredis.ri.alphazero.protocol.ProtocolBase.CRLF;
import static org.jredis.ri.alphazero.protocol.ProtocolBase.CRLF_LEN;
import static org.jredis.ri.alphazero.protocol.ProtocolBase.SIZE_BYTE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import org.jredis.ClientRuntimeException;
import org.jredis.NotSupportedException;
import org.jredis.ProviderException;
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.connector.NotConnectedException;
import org.jredis.protocol.Command;
import org.jredis.protocol.Protocol;
import org.jredis.protocol.Response;
import org.jredis.protocol.Command.ResponseType;
import org.jredis.ri.alphazero.protocol.ConcurrentSyncProtocol;
import org.jredis.ri.alphazero.protocol.ProtocolBase;
import org.jredis.ri.alphazero.protocol.VirtualResponse;
import org.jredis.ri.alphazero.support.Assert;
import org.jredis.ri.alphazero.support.Convert;
import org.jredis.ri.alphazero.support.FastBufferedInputStream;
import org.jredis.ri.alphazero.support.Log;
//import org.jredis.ri.alphazero.support.Concurrent2LockQueue.Node;

/**
 * WIP NOTES: 
 * <p>
 * intended to replace existing pipeline per further tests.  This
 * pipeline: 
 * 
 * <li>- provides maximal throughput for asynchronous feedsto Redis server.</li>
 * <li>- it is thread-safe.</li>
 *  
 * <p>
 * Design:
 * <p>
 * We're basically delegating output throttling concerns to the OS
 * and the TCP/IP layer using blocking write semantics. The TCP layer
 * will write MTU sized packets, regardless of actual user data, so
 * clearly the more we pack per packet, the higher will be the throughput 
 * of the connector.
 * 
 * @author Joubin <alphazero@sensesay.net>
 *
 */
public class ChunkedPipelineConnection 
	extends ConnectionBase // TODO: doesn't need the ThreadLocals -- re-think super via ConnectionSpec settings.
{

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	/**  */
	private ResponseHandler	    	respHandler;

	/**  */
	private Thread 					respHandlerThread;

	/**  */
	BlockingQueue<PendingCPRequest[]>	pendingResponseQueue;
	

	/** synchronization object used to serialize request queuing  */
	private Lock requestlock;

	/** 
	 * flag (default false) indicates if a pending QUIT command is being processed.  
	 * If true, any calls to queueRequests will result in a raise runtime exception
	 */
	private boolean					pendingQuit = false;

	/** used by the Pipeline to indicate its state.  Set to true on connect and false on Quit/Close */
	private AtomicBoolean			isActive;

	/** counted down on notifyConnect */
	private CountDownLatch		    connectionEstablished;

	/** MTU multiples to use as upper bound of the size of the chunk buffer */
	private static final int MTU_FACTOR = 2; // TODO: ConnectionSpec me.
	
	/** Assuming TCP MTU of 1500 - ~tcp header overhead rounded to nearest power of 8  */
	static final int MTU_SIZE = 1488;
	
	/** chunk buffer size */
	static final int CHUNK_BUFF_SIZE = Math.min(MTU_SIZE * MTU_FACTOR, 0xFFFF);
	
	/** minimum request size in bytes -- using PING e.g. 14 b */
	static final int MIN_REQ_SIZE = 14; 
	
	/** Chunk Queue size (slots) */
	static final int CHUNK_Q_SIZE = CHUNK_BUFF_SIZE / MIN_REQ_SIZE;
	
	/** chunk buffer */
	private byte[] chunkbuff;
	
	/**  chunk [hi:idx | lo:off] control long word */
	private int ctl_word;
	
	/** Chunk Queue of requests in Chunk buffer */
	private PendingCPRequest[] chunkqueue;
	
	// ------------------------------------------------------------------------
	// Constructor(s)
	// ------------------------------------------------------------------------
	/**
	 * @param spec
	 * @throws ClientRuntimeException
	 */
	//	protected
	public ChunkedPipelineConnection (ConnectionSpec spec) throws ClientRuntimeException {
		super(spec.setModality(Modality.Asynchronous));
	}

	// ------------------------------------------------------------------------
	// Extension
	// ------------------------------------------------------------------------
	/**
	 * 
	 */
	@SuppressWarnings("boxing")
	@Override
	protected void initializeComponents () {

		spec.setConnectionFlag(Flag.PIPELINE, true);
		spec.setConnectionFlag(Flag.RELIABLE, true);
		spec.setConnectionFlag(Flag.SHARED, true);

		chunkbuff = new byte[CHUNK_BUFF_SIZE];
		chunkqueue = new PendingCPRequest[CHUNK_Q_SIZE];

		ctl_word = 0;

		requestlock = new ReentrantLock(false);
		
		super.initializeComponents(); // REVU: this is a bit oddly placed .. 


		isActive = new AtomicBoolean(false);
		connectionEstablished = new CountDownLatch(1);

		pendingResponseQueue = new Concurrent2LockQueue<PendingCPRequest[]>();
		
		respHandler = new ResponseHandler();
		respHandlerThread = new Thread(respHandler, "response-handler");
		respHandlerThread.start();

		isActive.set(false); // REVU: ? superstitious ?
	}

	@Override
	protected void notifyConnected () {
		super.notifyConnected();
		Log.log("Pipeline <%s> connected", this);
		isActive.set(true);
		connectionEstablished.countDown();
	}
	@Override
	protected void notifyDisconnected () {
		super.notifyDisconnected();
		Log.log("Pipeline <%s> disconnected", this);
		isActive.set(true);
		connectionEstablished.countDown();
	}

	/**
	 * Pipeline must use a concurrent protocol handler.
	 *  
	 * @see org.jredis.ri.alphazero.connection.ConnectionBase#newProtocolHandler()
	 */
	@Override
	protected Protocol newProtocolHandler () {
		return new ConcurrentSyncProtocol();
		//		return new SynchProtocol();
	}

//	    @Override
//	    protected OutputStream newOutputStream(OutputStream socketOutputStream) {
//	    	return new BufferedOutputStream(socketOutputStream, MTU_SIZE);
//	    }

	/**
	 * Just make sure its a {@link FastBufferedInputStream}.
	 */
	@SuppressWarnings("boxing")
	@Override
	protected final InputStream newInputStream (InputStream socketInputStream) throws IllegalArgumentException {

		InputStream in = super.newInputStream(socketInputStream);
		if(!(in instanceof FastBufferedInputStream)){
			System.out.format("WARN: input was: %s\n", in.getClass().getCanonicalName());
			in = new FastBufferedInputStream (in, spec.getSocketProperty(Connection.Socket.Property.SO_RCVBUF));
		}
		return in;
	}

	// ------------------------------------------------------------------------
	// Interface: Connection
	// ------------------------------------------------------------------------


	/**
	 * This is a true asynchronous method.  The actual request write to server 
	 * possibly does occur in this method if the connection determines it is 
	 * optimal to flush the pipeline buffer, or, if you explicitly had requested
	 * flush via {@link Command#CONN_FLUSH}. 
	 * <p>
	 * Other item of note is that once a QUIT request has been queued, no further
	 * requests are accepted and a ClientRuntimeException is thrown.
	 * 
	 * @see org.jredis.ri.alphazero.connection.ConnectionBase#queueRequest(org.jredis.protocol.Command, byte[][])
	 */

	@Override
	public final Future<Response> queueRequest (Command cmd, byte[]... args) 
	throws ClientRuntimeException, ProviderException 
	{
		if(!isConnected()) 
			throw new NotConnectedException ("Not connected!");

		if(pendingQuit) 
			throw new ClientRuntimeException("Pipeline shutting down: Quit in progess; no further requests are accepted.");

		Protocol		protocol = Assert.notNull(getProtocolHandler(), "thread protocol handler", ProviderException.class);
		//		Log.log("protocol %d@%s", protocol.hashCode(), protocol.getClass());

		final boolean sendreq = 
			cmd.responseType != ResponseType.VIRTUAL && 
			cmd.responseType != ResponseType.NOP;

		/* setup send buffer if necessary */
		int		reqbyteslen = 0;
		if(sendreq) {
			reqbyteslen = ProtocolHelper.calcReqBuffSize(cmd, args);
		}

		/* PendingCPRequest provides transparent hook to force flush on future get(..) */
		final PendingCPRequest 	queuedRequest = new PendingCPRequest(this, cmd);

		/* possibly silly optimization, pulled out of sync block */
		final OutputStream out = getOutputStream();
		final boolean isflush = cmd == Command.CONN_FLUSH;
		final boolean exceeds = reqbyteslen > CHUNK_BUFF_SIZE;
		final boolean isquit = cmd == Command.QUIT;

		/* auth is used on connector initialization and must be sent asap */ 
		final boolean doflush = 
			cmd == Command.AUTH 	|| 
			cmd == Command.SELECT 	|| 
			isquit					||
			isflush;

		try {
			requestlock.lock();
			
			/* ======== CRITICAL BLOCK ====== */
			
			/* 4 byte control word is [ idx | off ] */
			/* chunk_ctl_word is contended and must be accessed only inside of critical block */
			final int __ctl_word = ctl_word;	// REVU: opportunity ..
			int idx = __ctl_word >> 16;
			int off = __ctl_word & 0x0000FFFF;

			boolean overflows = exceeds || off + reqbyteslen > CHUNK_BUFF_SIZE ? true : false;
			
			if(overflows) {
				out.write(chunkbuff, 0, off);
				out.flush();
				off = 0;
				
				pendingResponseQueue.add(chunkqueue);
				chunkqueue = new PendingCPRequest[CHUNK_Q_SIZE];
				idx = 0;
			}

			if(sendreq){
				if(exceeds) {
					/* can optimize and dispense with new byte[] -- only for large payloads */
					/* chunkqueue should be empty and idx 0 : assert for now */
					out.write(protocol.createRequestBuffer(cmd, args));
					out.flush();
					chunkqueue[0] = queuedRequest;
					final PendingCPRequest[] oneoffitem =  new PendingCPRequest[1];
					oneoffitem[0] = queuedRequest;
					pendingResponseQueue.add(oneoffitem);
				}
				else {
					// REVU: next optimization step !
					// NOTE: this 'new' here is not necessary and is only because of copy/paste from
					// ProtocolBase (see ProtocolHelper.writeReq..().
					ProtocolHelper.writeRequestToBuffer(new ProtocolHelper.Buffer(chunkbuff, off), cmd, args);
					off+=reqbyteslen;
					chunkqueue[idx] = queuedRequest;
					idx++;
				}
			}

			if(doflush) {
				if(!isquit){
					if(off>0){
						out.write(chunkbuff, 0, off);
						out.flush();
						off = 0;
						pendingResponseQueue.add(chunkqueue);
						chunkqueue = new PendingCPRequest[CHUNK_Q_SIZE];
						idx = 0;
					}
				}
				else {
					pendingQuit = true;
					isActive.set(false);
					final PendingCPRequest[] oneoffitem =  new PendingCPRequest[1];
					oneoffitem[0] = queuedRequest;
					pendingResponseQueue.add(oneoffitem);
				}
			}
			
			/* update 4 byte control word [ idx | off ] */
			ctl_word = (idx << 16) | (off | 0x0); 
			
			/* ==END=== CRITICAL BLOCK ====== */
			/* REVU: both of these exceptions require setting fault on the cached pendings */
		} catch (IOException e) {
			Log.error("IOException cmd:%s isConnected:%b", cmd.code, isConnected());
			this.onConnectionFault(String.format("IOFault (cmd: %s)", cmd.code), true);
		} catch (ArrayIndexOutOfBoundsException e){
			Log.error("on %s", cmd.code);
			throw new ProviderException("BUG - recheck assumptions ..", e);
		} finally {
			requestlock.unlock();
		}
		
		return queuedRequest;
	}

	void onResponseHandlerError (ClientRuntimeException cre, PendingRequest request) {
		Log.error("Pipeline response handler encountered an error: " + cre.getMessage());

		// signal fault
		onConnectionFault(cre.getMessage(), false);
		
		// set execution error for future object
		request.setCRE(cre);

//		PendingCPRequest pending = null;
		while(true){
			try {
//				pending = pendingResponseQueue.remove();
				PendingCPRequest[] items = null;
				items = pendingResponseQueue.remove();
				for(PendingCPRequest item : items){
					if(item == null) { break; }
					item.setCRE(cre);
					Log.error("set pending %s response to error with CRE", item.cmd);
				}
//				pending.setCRE(cre);
//				Log.error("set pending %s response to error with CRE", pending.cmd);
			}
			catch (NoSuchElementException empty){ break; }
		}
	}
	
	// ========================================================================
	// Inner Class
	// ========================================================================
	
	/**
	 * This was pretty much based on Maged M. Michael, and Michael L. Scott's
	 * 2 Lock concurrent queue as described in their paper, "Simple, Fast, 
	 * and Practical Non-Blocking and Blocking Concurrent Algorithms" 
	 * ({michael, scott}@cs.rochester.edu).  
	 * 
	 * For the specific use case of {@link ChunkedPipelineConnection}, 
	 * we will have 1:1 producer-consumer concurrency and can do
	 * away with the 2 locks of their algorithm and get better performance.
	 * 
	 * This class partially supports the {@link BlockingQueue} for the
	 * purposes of the pipeline.  It is not intended for general use.
	 *
	 * @author  joubin (alphazero@sensesay.net)
	 * @date    Dec 9, 2009 (original)
	 * @date    Jan 23, 2012 (this version)
	 * 
	 */
	final static class Concurrent2LockQueue<E> implements BlockingQueue<E>{

		/**
		 * @param <E>
		 */
		private static final class Node<E> {
			private volatile E 			item;
			private volatile Node<E>	next;

			@SuppressWarnings("unchecked")
	        private static final
			AtomicReferenceFieldUpdater<Node, Node>
			nextUpdater = 
				AtomicReferenceFieldUpdater.newUpdater
				(Node.class, Node.class, "next");

			@SuppressWarnings("unchecked")
	        private static final
			AtomicReferenceFieldUpdater<Node, Object>
			itemUpdater = 
				AtomicReferenceFieldUpdater.newUpdater
				(Node.class, Object.class, "item");

			private Node(E x, Node<E> n) { item = x; next = n; }
			
			private final E getItem() { return item; }
			private final void setItem(E update) { itemUpdater.set(this, update); }
			private final void setNext(Node<E> update) { nextUpdater.set(this, update); }
			private final Node<E> getNext() { return next; }
		}

		// --------------------------------------------------------------
		// Properties
		// --------------------------------------------------------------
		private transient volatile Node<E> head = new Node<E>(null, null);
		private transient volatile Node<E> tail = head;

	    public Concurrent2LockQueue () {}

		// --------------------------------------------------------------
		// INTERFACE: BlockingQueue<E>
		// --------------------------------------------------------------
	    
	    /* (non-Javadoc) @see java.util.concurrent.BlockingQueue#offer(java.lang.Object) */
	    public final boolean offer(E item) {
	    	if(null == item) throw new NullPointerException("item");
	    	final Node<E> n = new Node<E>(item, null);

	    	Node<E> t = tail;
			t.setNext(n);
			tail = n;

	    	return true;
	    }
	    
		/* (non-Javadoc) @see java.util.concurrent.BlockingQueue#poll(long, java.util.concurrent.TimeUnit) */
		@Override final
		public E poll(long timeout, TimeUnit unit) throws InterruptedException {
			throw new RuntimeException("not supported");
		}
		
	    /* (non-Javadoc) @see java.util.Queue#poll() */
	    public final E poll () {
	    	E item = null;

			final Node<E> h = head;
			final Node<E> newhead = h.getNext();
			if(newhead != null) {
				item = newhead.getItem();
				head = newhead;
				newhead.setItem(null);
				h.setNext(null);
			} 

	    	return item;
	    }
	    
	    /* (non-Javadoc) @see java.util.Queue#peek() */
	    @Override final
	    public E peek () {
	    	E item = null;

			final Node<E> h = head;
			final Node<E> f = h.getNext();
			if(f != null) {
				item = f.getItem();
			}

	    	return item;
	    }
	    
		/* (non-Javadoc) @see java.util.concurrent.BlockingQueue#take() */
		@Override final
		public E take() throws InterruptedException {
			E	item = null;
			while(true){
				item = poll();
				if(item != null) 
					break;
				LockSupport.parkNanos(1L); // magic number -- let's configure this.
				if(Thread.interrupted()){
					Log.error("%s interrupted!", Thread.currentThread().getName());
					throw new InterruptedException();
				}
			}
			return item;
		}
		
		/* (non-Javadoc) @see java.util.Queue#remove() */
		@Override final
		public E remove() {
			final E item = poll();
			if(item == null) 
				throw new NoSuchElementException();
			return item;
		}

		/* (non-Javadoc) @see java.util.concurrent.BlockingQueue#add(java.lang.Object) */
		@Override final
		public boolean add(E e) {
			return this.offer(e);
		}

		/* -- NOT SUPPORTED -- BEGIN */
		
		@Override final
		public E element() {
			throw new RuntimeException("not supported");
		}
		@Override final
		public boolean addAll(Collection<? extends E> c) {
			throw new RuntimeException("not supported");
		}
		@Override final
		public void clear() {
			throw new RuntimeException("not supported");
		}
		@Override final
		public boolean contains(Object o) {
			throw new RuntimeException("not supported");
		}
		@Override final
		public boolean containsAll(Collection<?> c) {
			throw new RuntimeException("not supported");
		}
		@Override final public boolean isEmpty() {
			throw new RuntimeException("not supported");
		}
		@Override final
		public Iterator<E> iterator() {
			throw new RuntimeException("not supported");
		}
		@Override final
		public boolean remove(Object o) {
			throw new RuntimeException("not supported");
		}
		@Override final
		public boolean removeAll(Collection<?> c) {
			throw new RuntimeException("not supported");
		}
		@Override final
		public boolean retainAll(Collection<?> c) {
			throw new RuntimeException("not supported");
		}
		@Override final
		public int size() {
			throw new RuntimeException("not supported");
		}
		@Override final
		public Object[] toArray() {
			throw new RuntimeException("not supported");
		}
		@Override final
		public <T> T[] toArray(T[] a) {
			throw new RuntimeException("not supported");
		}
		@Override final
		public int drainTo(Collection<? super E> c) {
			throw new RuntimeException("not supported");
		}
		@Override final
		public int drainTo(Collection<? super E> c, int maxElements) {
			throw new RuntimeException("not supported");
		}
		@Override final
		public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
			throw new RuntimeException("not supported");
		}
		@Override final
		public void put(E e) throws InterruptedException {
			throw new RuntimeException("not supported");
		}
		@Override final
		public int remainingCapacity() {
			throw new RuntimeException("not supported");
		}
		/* -- NOT SUPPORTED -- END */
	}
	
	// ========================================================================
	// Inner Class
	// ========================================================================
	
	/**
	 * A not so KISSy reproduction of necessary {@link Protocol} support
	 * from {@link ProtocolBase}.  Stays here until update of the interface
	 * to allow for sharing of the codebase.
	 * @author Joubin <alphazero@sensesay.net>
	 *
	 */
	final static class ProtocolHelper {
		/* hackedy hack -- to go bye bye soon  */
		static class Buffer {
			byte[] b = null;
			private int off = 0;
			Buffer(int size){
				b = new byte[size];
			}
			Buffer(byte[] b, int off){
				this.b = b;
				this.off = off;
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
		public static int calcReqBuffSize (Command cmd, byte[] ... args) throws IllegalArgumentException {
			byte[] cmdLenBytes = Convert.toBytes(cmd.bytes.length);
			byte[] lineCntBytes = Convert.toBytes(args.length+1);

			int bsize = 1 + lineCntBytes.length + CRLF_LEN + 1 + cmdLenBytes.length + CRLF_LEN + cmd.bytes.length + CRLF_LEN;
			for(int i=0;i<args.length; i++){
				byte[] argLenBytes = Convert.toBytes(Assert.notNull(args[i], i, ProviderException.class).length);
				int _bsize = 1 + argLenBytes.length + CRLF_LEN + args[i].length + CRLF_LEN;
				bsize += _bsize;
			}
			return bsize;
		}
		static public byte[] writeRequestToBuffer(final Buffer buffer, final Command cmd, final byte[]...args) throws ProviderException, IllegalArgumentException {
			//    		Buffer buffer = null;
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
			return buffer.getBytes();
		}

	}
	
	// ========================================================================
	// Inner Class
	// ========================================================================
	
	/**
	 * ChunkedPipeline specific of {@link PendingRequest}.  This class
	 * maintains a reference to the pipeline to allow for transparent 
	 * invokation of {@value Command#CONN_FLUSH}.
	 * 
	 * @author Joubin <alphazero@sensesay.net>
	 */
	final static class PendingCPRequest extends PendingRequest {

		private final ChunkedPipelineConnection pipeline;
		
		PendingCPRequest(ChunkedPipelineConnection pipeline, Command cmd) {
			super(cmd);
			this.pipeline = pipeline;
		}
		@Override final
		public Response get() 
		throws InterruptedException, ExecutionException {
			requestFlush();
			return super.get();
		}
		@Override final
		public Response get(long timeout, TimeUnit unit)
		throws InterruptedException, ExecutionException, TimeoutException 
		{
			requestFlush();
			return super.get(timeout, unit);
		}
		final private void requestFlush() {
			if(cmd != Command.QUIT) {
				pipeline.queueRequest(Command.CONN_FLUSH);
			}
			else {
				new Exception().printStackTrace();
			}
		}
	}
	
	// ========================================================================
	// Inner Class
	// ========================================================================
	
	/**
	 * Provides the response processing logic as a {@link Runnable}.
	 * <p>
	 * TODD: Needs to have a more regulated operating cycle.  Right now its just
	 * infinite loop until something goes boom.  Not good.
	 * 
	 * @author  Joubin Houshyar (alphazero@sensesay.net)
	 * @version alpha.0, Oct 18, 2009
	 * @since   alpha.0
	 * 
	 */
	public final class ResponseHandler implements Runnable, Connection.Listener {

//		private final AtomicBoolean work_flag;
		private volatile boolean work_flag; // REVU: checked in every loop - no need for atomic.
		private final AtomicBoolean alive_flag;
		private final AtomicReference<Thread> thread;

		// ------------------------------------------------------------------------
		// Constructor
		// ------------------------------------------------------------------------

		// REVU: this is a complete mess -- TODO: 
		/**
		 * Adds self to the listeners of the enclosing {@link Connection} instance.
		 */
		public ResponseHandler () {
			ChunkedPipelineConnection.this.addListener(this);
//			this.work_flag = new AtomicBoolean(true); 
			this.work_flag = true;
			this.alive_flag = new AtomicBoolean(false);
			this.thread = new AtomicReference<Thread>(null);
		} 

		// ------------------------------------------------------------------------
		// INTERFACE: Runnable
		// ------------------------------------------------------------------------
		/**
		 * Keeps processing the {@link PendingRequest}s in the pending {@link Queue}
		 * until a QUIT is encountered in the pending queue.  Thread will stop after
		 * processing the QUIT response (which is expected to be a {@link VirtualResponse}.
		 * <p>
		 * TODO: not entirely clear what is the best way to handle exceptions.
		 * <p>
		 * TODO: socket Reconnect in the context of pipelining is non-trivial, and maybe
		 * not even practically possible.  (e.g. request n is sent but pipe breaks on
		 * some m (m!=n) response.  non trivial.  Perhaps its best to assume broken connection
		 * means faulted server, specially given the fact that a pipeline has a heartbeat
		 * so the issue can not be timeout.
		 */
		@Override
		public void run () {
			thread.compareAndSet(null, Thread.currentThread());
			alive_flag.compareAndSet(false, true);
			/** Response handler thread specific protocol handler -- optimize fencing */
			Protocol protocol = Assert.notNull (newProtocolHandler(), "the delegate protocol handler", ClientRuntimeException.class);

			Log.log("Pipeline <%s> thread for <%s> started.", Thread.currentThread().getName(), ChunkedPipelineConnection.this.toString());
			PendingCPRequest pending = null;
			//			while(work_flag.get()){
			while(work_flag){
				Response response = null;
				try {
					PendingCPRequest[] items = null;
					items = pendingResponseQueue.take();
					for(PendingCPRequest item : items) {
						if(item == null) { break; }
						pending = item;
						try {
							// TODO: here -- simplify REVU: ?
							response = protocol.createResponse(pending.cmd);
							response.read(getInputStream());
							pending.response = response;
							pending.completion.signal();
							if(response.getStatus().isError()) {
								Log.error ("(Asynch) Error response for " + pending.cmd.code + " => " + response.getStatus().message());
							}

						}

						// TODO: REVU: this in context of both connection and (general) errors. 
						catch (ProviderException bug){
							Log.bug ("ProviderException: " + bug.getMessage());
							onResponseHandlerError(bug, pending);
							break;
						}
						catch (ClientRuntimeException cre) {
							Log.problem ("ClientRuntimeException: " + cre.getMessage());
							onResponseHandlerError(cre, pending);
							break;
						}
						catch (RuntimeException e){
							Log.problem ("Unexpected (and not handled) RuntimeException: " + e.getMessage());
							onResponseHandlerError(new ClientRuntimeException("Unexpected (and not handled) RuntimeException", e), pending);
							break;
						}

						// REVU: this should be noted on the API spec : TODO:
						/* QUITs are not sent in pipelines to Redis as it immediately will 
						 * drop the connection and there goes the pending stuff. */
						if(pending.cmd == Command.QUIT) {
							ChunkedPipelineConnection.this.disconnect();
							break;
						}
					}
				}
				// REVU: why the general catch -- related to coherence issues of the volatile flags
				// TODO: review and clean this shit up
				//				catch (InterruptedException e1) {
				catch (Throwable e1) {
					Log.log("Pipeline thread interrupted.");
					break;
				}
			}
			Log.log("Pipeline <%s> thread for <%s> stopped.", Thread.currentThread().getName(), ChunkedPipelineConnection.this);
			alive_flag.compareAndSet(true, false);
		}

		// REVU: TODO: 
		final private void stopHandler() {
			Log.log("%s stopping handler thread", this);
//			work_flag.set(false);
			work_flag = false;
			thread.get().interrupt();
			//        	PipelineConnectionBase.this.respHandlerThread.interrupt();
		}
		final private void shutdownHandler() {
			/*
			 * It is not expected that shutdown would get called before
			 * stop, but if it has, this makes sure we first go through
			 * the stop sequence.
			 */
//			if(work_flag.get() != false || alive_flag.get() != false)
			if(work_flag != false || alive_flag.get() != false)
				stopHandler();
			alive_flag.set(false);
			ChunkedPipelineConnection.this.removeListener(this);
			Log.log("%s response handler has shutdown", this);
		}
		// ------------------------------------------------------------------------
		// INTERFACE: Connection.Listener
		/* 
		 * hooks for integrating the response handler thread's state with the 
		 * wrapping connection's state through event callbacks. 
		 */
		// ------------------------------------------------------------------------

		// REVU: clean this shit up. TODO:
		/**
		 * @see org.jredis.connector.Connection.Listener#onEvent(org.jredis.connector.Connection.Event)
		 */
		@Override
		public void onEvent (Event event) {
			if(event.getSource() != ChunkedPipelineConnection.this) {
				String msg = String.format("event source [%s] is not this pipeline [%s]", event.getSource(), ChunkedPipelineConnection.this);
				Log.bug(msg);
				throw new ProviderException(msg);
			}
			//        	(new Exception()).printStackTrace();
			Log.log("Pipeline.ResponseHandler: onEvent %s source: %s", event.getType().name(), event.getSource());
			switch (event.getType()){
			case CONNECTED:
				// (re)start
				break;
			case DISCONNECTED:
				// should be stopped now
				break;
			case CONNECTING:
				// no op
				break;
			case FAULTED:
			case DISCONNECTING:
				stopHandler();
				break;
			case SHUTDOWN:
				shutdownHandler();
				break;
			}
		}
	}
}
