package com.thisispiri.mnk;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.thisispiri.util.AndroidUtilsKt;

/**Writes on and reads from .sgf and .pirimnk files an {@link MnkGame}.*/
public class MnkSaveLoader {
	/**The maximum horizontal/vertical size of the board SGF supports.*/
	final static int SGF_MAX = 52;
	/**Saves the {@code MnkGame}. If its horizontal/vertical size do not exceed {@link MnkSaveLoader#SGF_MAX}, uses SGF. Otherwise, uses a simple format called .pirimnk.*/
	void save(final MnkGame game, final String directoryName, final String fileName) throws IOException {
		if(game.getHorSize() > SGF_MAX || game.getVerSize() > SGF_MAX) piriSave(game, directoryName, fileName);
		else sgfSave(game, directoryName, fileName);
	}
	private void sgfSave(final MnkGame game, final String directoryName, final String fileName) throws IOException {
		OutputStreamWriter outputWriter = new OutputStreamWriter(new FileOutputStream(AndroidUtilsKt.getFile(directoryName, fileName, true)));
		outputWriter.write("(;FF[4]GM[4]SZ["); //FF[4] : use SGF version 4. GM[4] : game type is gomoku+renju(although PIRI MNK doesn't support renju yet). SZ[game.boardSize] : use board of horSize:verSize(rectangle) or horSize(square)
		if(game.getHorSize() == game.getVerSize()) outputWriter.write(String.valueOf(game.getHorSize())); //writing square boards' size with two numbers is illegal
		else outputWriter.write(game.getHorSize() + ":" + game.getVerSize());
		outputWriter.write(']');
		for(Move move : game.history) { //Foreach loops on Stacks are FIFO in Java
			if(move.placed == Shape.X) outputWriter.write(";B["); //first player(black)
			else outputWriter.write(";W["); //second player(white)
			char xChar = (char)(move.coord.x + 'a'), yChar = (char)(move.coord.y + 'a'); //start at lowercase a
			//after using all 26 lowercase letters, move to uppercase alphabets
			if(move.coord.x > 'z') xChar += 'A' - 'z' - 1; //equals subtracting 58.
			if(move.coord.y > 'z') yChar += 'A' - 'z' - 1;
			outputWriter.write(new String(new char[]{xChar, yChar, ']'}));
		}
		outputWriter.write(')'); //mark end of the tree
		outputWriter.close();
	}
	private void piriSave(final MnkGame game, final String directoryName, final String fileName) throws IOException {
		//TODO: implement
		throw new IOException("incomplete method called");
	}
	MnkGame load(final String directoryName, final String fileName, final int winStreak) throws IOException {
		MnkGame game = new MnkGame();
		InputStreamReader inputReader = new InputStreamReader(new FileInputStream(AndroidUtilsKt.getFile(directoryName, fileName, false)));
		int skipper;
		do {skipper = inputReader.read();}
		while(skipper != '(' && skipper != -1);
		if(inputReader.read() == ';') { //SGF format
			char previous = 0, now;
			while(inputReader.ready()) {
				now = (char)inputReader.read();
				if(previous == 'S' && now == 'Z') { //board size
					if(inputReader.skip(1) != 1) throw new IOException(); //process a [
					int size = 0;
					now = (char)inputReader.read();
					while(now != ']' && now != ':') {
						size = (size * 10) + Character.getNumericValue(now);
						now = (char)inputReader.read();
					}
					if(now == ':') {
						int verSize = 0;
						now = (char)inputReader.read();
						while(now != ']') {
							verSize = (verSize * 10) + Character.getNumericValue(now);
							now = (char)inputReader.read();
						}
						game.setSize(size, verSize);
					}
					else game.setSize(size, size);
					game.winStreak = winStreak;
				}
				else if(previous == ';' && (now == 'B' || now == 'W')) { //moves
					if(inputReader.skip(1) != 1) throw new IOException(); //process a [
					int x = inputReader.read() - 'a', y = inputReader.read() - 'a'; //recover indexes from lowercase alphabets
					//recover uppercase alphabets to indexes
					if(x < 0) x -= 'A' - 'z' - 1; //equals adding 58.
					if(y < 0) y -= 'A' - 'z' - 1;
					if(now == 'B') game.place(x, y, Shape.X);
					else game.place(x, y, Shape.O);
				}
				previous = now;
			}
		}
		else { //PIRIMNK format
			//TODO: implement
			throw new IOException();
		}
		inputReader.close();
		return game;
	}
}
