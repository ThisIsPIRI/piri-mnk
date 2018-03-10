package com.thisispiri.mnk;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
/**Reads and writes data from and to {@code InputStream}s and {@code OutputStream}s for playing {@link MnkGame} with another person. <br>
 * When sending moves, the format is {MOVE_HEADER, (x int), (y int)}. <br>
 * When sending requests, the format is {REQUEST_HEADER, (action constant)}. <br>
 * When sending responses(to requests), the format is {RESPONSE_HEADER, RESPONSE_REJECT/PERMIT, (action constant)}. <br>
 * When sending orders, the format is {ORDER_HEADER, (order constant), in case of initialization : (board size int), (winning streak int)}.*/
public class IoThread extends Thread {
	public final static byte MOVE_HEADER = 0, REQUEST_HEADER = 1, RESPONSE_HEADER = 2, ORDER_HEADER = 3; //headers
	public final static byte REQUEST_RESTART = 4, REQUEST_REVERT = 5; //requests
	public final static byte RESPONSE_PERMIT = 6, RESPONSE_REJECT = 7; //responses
	public final static byte ORDER_INITIALIZE = 8, ORDER_CANCEL_CONNECTION = 9; //orders
	private final InputStream inputStream;
	private final OutputStream outputStream;
	private final MnkManager manager;
	/**All three arguments must not be null.*/
	public IoThread(MnkManager manager, InputStream input, OutputStream output) {
		this.manager = manager;inputStream = input; outputStream = output;
	}
	/**Constantly reads data from the {@code InputStream}.*/
	@Override public void run() { //main loop is dedicated to reading
		byte[] buffer = new byte[32];
		try {
			readLoop: while (!interrupted()) {
				if(inputStream.read(buffer) == -1) continue; //If the end of stream was reached.
				switch(buffer[0]) {
					case MOVE_HEADER:
						if(!manager.endTurn(ByteBuffer.wrap(Arrays.copyOfRange(buffer, 1, 5)).getInt(), ByteBuffer.wrap(Arrays.copyOfRange(buffer, 5, 9)).getInt()))
							manager.informUser("The opponent's move was invalid. He might be using a modified version of the game.");
						break;
					case REQUEST_HEADER:
						manager.requestToUser(buffer[1]);
						break;
					case RESPONSE_HEADER:
						if(buffer[1] == RESPONSE_PERMIT) {
							switch(buffer[2]) {
								case REQUEST_RESTART: manager.initialize(); break;
								case REQUEST_REVERT: manager.revertLast(); break;
							}
						}
						else if(buffer[1] == RESPONSE_REJECT) manager.informRejection();
						break;
					case ORDER_HEADER:
						if(buffer[1] == ORDER_INITIALIZE) {
							manager.getGame().setSize(ByteBuffer.wrap(Arrays.copyOfRange(buffer, 2, 6)).getInt(), ByteBuffer.wrap(Arrays.copyOfRange(buffer, 6, 10)).getInt());
							manager.getGame().winStreak = ByteBuffer.wrap(Arrays.copyOfRange(buffer, 10, 14)).getInt();
							manager.initialize();
							manager.setTimeLimit(ByteBuffer.wrap(Arrays.copyOfRange(buffer, 14, 18)).getInt());
						}
						else if(buffer[1] == ORDER_CANCEL_CONNECTION) {
							manager.cancelConnection();
							break readLoop;
						}
						break;
				}
			}
			inputStream.close();
			outputStream.close();
		}
		catch (IOException e) {
			/*If the Thread is interrupted, we don't have to inform the user of the Exception; its purpose will have expired.
			In this specific case, the only case an IOException can be caught here AND the Thread remain interrupted is when the Exception was thrown in the blocking read() call due to the socket having been closed.
			If the cause of the Exception is something else, the Thread won't stay interrupted in this catch block.*/
			if(!interrupted()) manager.informIoError();
		}
	}
	/**Writes the data to the {@code OutputStream}.*/
	public void write(final byte[] data) {
		try {
			outputStream.write(data);
		}
		catch(IOException e) {
			manager.informIoError();
		}
	}
	/**Writes Bytes, Integers, Longs, Shorts, Floats or Doubles in data varargs to the {@code OutputStream}.
	 * @param size The total size of data in bytes.
	 * @param data The primitive data to write to the {@code OutputStream}.
	 * @throws BufferOverflowException If the {@code size} is smaller than the sum of the size of {@code data} in bytes.*/
	public void write(final int size, final Object... data) throws BufferOverflowException {
		ByteBuffer buffer = ByteBuffer.allocate(size);
		for(Object d : data) {
			if (d instanceof Integer) buffer.putInt((Integer) d);
			else if (d instanceof Byte) buffer.put((Byte) d);
			else if (d instanceof Float) buffer.putFloat((Float) d);
			else if (d instanceof Double) buffer.putDouble((Double) d);
			else if (d instanceof Long) buffer.putLong((Long) d);
			else if (d instanceof Short) buffer.putShort((Short) d);
		}
		write(buffer.array());
	}
}