package com.thisispiri.mnk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.thisispiri.mnk.MnkManager.Info;

/**Reads and writes data from and to {@code InputStream}s and {@code OutputStream}s for playing {@link MnkGame} with another person. <br>
 * When sending moves, the format is {MOVE_HEADER, (x int), (y int)}. <br>
 * When sending requests, the format is {REQUEST_HEADER, (action constant)}. <br>
 * When sending responses(to requests), the format is {RESPONSE_HEADER, RESPONSE_REJECT/PERMIT, (action constant)}. <br>
 * When sending orders, the format is {ORDER_HEADER, (order constant), in case of initialization : (board size ints), (winning streak int)}.
 * {@link IoThread#REQUEST_RESTART} can have the changed rules after it: in this case, the format is
 * {REQUEST/RESPONSE_HEADER, (RESPONSE_PERMIT if response), REQUEST_RESTART, 1, horSize, verSize, winStreak, timeLimit}, with the 1 indicating the rules have changed.*/
public class IoThread extends Thread {
	public final static byte MOVE_HEADER = 0, REQUEST_HEADER = 1, RESPONSE_HEADER = 2, ORDER_HEADER = 3; //headers
	public final static byte REQUEST_RESTART = 4, REQUEST_REVERT = 5; //requests
	public final static byte RESPONSE_PERMIT = 6, RESPONSE_REJECT = 7; //responses
	public final static byte ORDER_INITIALIZE = 8, ORDER_CANCEL_CONNECTION = 9; //orders
	public final static byte RULE_CHANGED = 10; //, PACKET_END = 11; TODO: Account for the possibility of data not arriving all at once
	private final InputStream inputStream;
	private final OutputStream outputStream;
	private final MnkManager manager;
	/**All three arguments must not be null.*/
	public IoThread(MnkManager manager, InputStream input, OutputStream output) {
		this.manager = manager;inputStream = input; outputStream = output;
	}
	private int[] getRulesFrom(byte[] array, int startingFrom) {
		int[] result = new int[MnkManager.RULE_SIZE];
		for(int i = 0;i < MnkManager.RULE_SIZE;i++)
			result[i] = ByteBuffer.wrap(Arrays.copyOfRange(array, i * 4 + startingFrom, i * 4 + startingFrom + 4)).getInt();
		result[MnkManager.RULE_SIZE - 1] ^= 1; //Flip myIndex.
		return result;
	}
	/**Constantly reads data from the {@code InputStream}.*/
	@Override public void run() { //main loop is dedicated to reading
		byte[] buffer = new byte[36];
		try {
			readLoop: while (!interrupted()) {
				if(inputStream.read(buffer) == -1) continue; //If the end of stream was reached.
				switch(buffer[0]) {
				case MOVE_HEADER:
					if(!manager.endTurn(ByteBuffer.wrap(Arrays.copyOfRange(buffer, 1, 5)).getInt(), ByteBuffer.wrap(Arrays.copyOfRange(buffer, 5, 9)).getInt()))
						manager.informUser(Info.INVALID_MOVE);
					break;
				case REQUEST_HEADER:
					if(buffer[1] == REQUEST_RESTART && buffer[2] == RULE_CHANGED)
						manager.requestToUser(buffer[1], getRulesFrom(buffer, 3));
					else
						manager.requestToUser(buffer[1]);
					break;
				case RESPONSE_HEADER:
					if(buffer[1] == RESPONSE_PERMIT) {
						switch(buffer[2]) {
							case REQUEST_RESTART:
								if(buffer[3] == RULE_CHANGED)
									manager.setRulesFrom(getRulesFrom(buffer, 4));
								manager.initialize();
								break;
							case REQUEST_REVERT: manager.revertLast(); break;
						}
					}
					else if(buffer[1] == RESPONSE_REJECT) manager.informUser(Info.REJECTION);
					break;
				case ORDER_HEADER:
					if(buffer[1] == ORDER_INITIALIZE) {
						manager.setRulesFrom(getRulesFrom(buffer, 2));
						manager.initialize();
					}
					else if(buffer[1] == ORDER_CANCEL_CONNECTION) {
						manager.cancelConnection();
						break readLoop;
					}
					break;
				}
				Arrays.fill(buffer, (byte) 0);
			}
			inputStream.close();
			outputStream.close();
		}
		catch (IOException e) {
			/*If the Thread is interrupted, we don't have to inform the user of the Exception; its purpose will have expired.
			In this specific case, the only case an IOException can be caught here AND the Thread remain interrupted is when the Exception was thrown in the blocking read() call due to the socket having been closed.
			If the cause of the Exception is something else, the Thread won't stay interrupted in this catch block.*/
			if(!interrupted()) manager.informUser(Info.READ_FAIL);
		}
	}
	/**Writes the data to the {@code OutputStream}.*/
	public void write(final byte[] data) {
		try {
			outputStream.write(data);
		}
		catch(IOException e) {
			manager.informUser(Info.WRITE_FAIL);
		}
	}
	/**Writes Bytes, Integers, Longs, Shorts, Floats, Doubles or int[]s in {@code data} varargs to the {@code OutputStream}.
	 * @param size The total size of data in bytes.
	 * @param data The data to write to the {@code OutputStream}.
	 * @throws BufferOverflowException If the {@code size} is smaller than the sum of the size of {@code data} in bytes.*/
	public void write(final int size, final Object... data) throws BufferOverflowException {
		write(makeBuffer(size, data));
	}
	public static byte[] makeBuffer(final int size, final Object... data) throws BufferOverflowException {
		//TODO: Remove the size argument?
		ByteBuffer buffer = ByteBuffer.allocate(size);
		for(Object d : data) {
			if(d instanceof Integer) buffer.putInt((Integer) d);
			else if(d instanceof Byte) buffer.put((Byte) d);
			else if(d instanceof Float) buffer.putFloat((Float) d);
			else if(d instanceof Double) buffer.putDouble((Double) d);
			else if(d instanceof Long) buffer.putLong((Long) d);
			else if(d instanceof Short) buffer.putShort((Short) d);
			else if(d instanceof int[])
				for(int i : (int[])d)
					buffer.putInt(i);
		}
		return buffer.array();
	}
}