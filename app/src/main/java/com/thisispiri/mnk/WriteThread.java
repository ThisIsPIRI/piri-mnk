package com.thisispiri.mnk;

import java.util.Deque;
import java.util.LinkedList;

public class WriteThread extends Thread {
	private final Deque<byte[]> buffer = new LinkedList<>();
	private final IoThread ioThread;
	public final Object syncObject = new Object();
	public WriteThread(IoThread ioThread) {
		this.ioThread = ioThread;
	}
	@Override public void run() {
		while(!interrupted()) {
			try { synchronized(syncObject) {
				while(buffer.isEmpty()) syncObject.wait();
			}}
			catch(InterruptedException e) {
				break;
			}
			ioThread.write(buffer.removeFirst());
		}
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
