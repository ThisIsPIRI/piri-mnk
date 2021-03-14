package com.thisispiri.mnk;

import java.util.Deque;
import java.util.LinkedList;

/**A helper Thread for asynchronous writing with an {@link IoThread}.*/
public class WriteThread extends Thread {
	private final Deque<byte[]> buffer = new LinkedList<>();
	private final IoThread ioThread;
	public final Object syncObject = new Object();
	private Runnable runAfterInterrupt = null;
	public WriteThread(IoThread ioThread) {
		this.ioThread = ioThread;
	}
	@Override public void run() {
		while(!interrupted()) {
			try { synchronized(syncObject) {
				while(buffer.isEmpty()) syncObject.wait();
			}}
			catch(InterruptedException e) {
				afterInterrupt();
				return;
			}
			ioThread.write(buffer.removeFirst());
		}
		afterInterrupt();
	}
	private void afterInterrupt() {
		//Write all remaining data before exiting.
		for(byte[] ba : buffer)
			ioThread.write(ba);
		if(runAfterInterrupt != null)
			runAfterInterrupt.run();
	}
	/**{@code runAfterInterrupt} will be run after the Thread is interrupted and all remaining data are written.
	 * If you're writing to a socket's stream and have to write some data right before interrupting this Thread,
	 * it is recommended to not close the socket yourself and instead pass a method that closes the socket as {@code runAfterInterrupt}.
	 * Otherwise, it's likely that the socket will close before this Thread can write the last data.*/
	public void interrupt(Runnable runAfterInterrupt) {
		this.runAfterInterrupt = runAfterInterrupt;
		super.interrupt();
	}
	public void write(int size, Object... data) {
		write(IoThread.makeBuffer(size, data));
	}
	public void write(byte[] data) {
		buffer.add(data);
		synchronized(syncObject) {
			syncObject.notify();
		}
	}
}
