package com.thisispiri.mnk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**Writes on and reads from .sgf and .pirimnk files an {@link MnkGame}.*/
public class MnkSaveLoader {
	/**The maximum horizontal/vertical size of the board SGF supports.*/
	public final static int SGF_MAX = 52;
	/**Saves the {@code MnkGame}. If its horizontal/vertical size do not exceed {@link MnkSaveLoader#SGF_MAX}, uses SGF. Otherwise, uses a simple format called .pirimnk.*/
	public static void save(final MnkGame game, final File file) throws IOException {
		if(game.getHorSize() > SGF_MAX || game.getVerSize() > SGF_MAX) piriSave(game, file);
		else sgfSave(game, file);
	}
	private static void sgfSave(final MnkGame game, final File file) throws IOException {
		OutputStreamWriter outputWriter = new OutputStreamWriter(new FileOutputStream(file));
		outputWriter.write("(;FF[4]GM[4]SZ["); //FF[4] : use SGF version 4. GM[4] : game type is gomoku+renju(although PIRI MNK doesn't support renju yet). SZ[game.boardSize] : use board of horSize:verSize(rectangle) or horSize(square)
		if(game.getHorSize() == game.getVerSize()) outputWriter.write(String.valueOf(game.getHorSize())); //writing square boards' size with two numbers is illegal
		else outputWriter.write(game.getHorSize() + ":" + game.getVerSize());
		outputWriter.write(']');
		for(Move move : game.history) { //Foreach loops on Stacks are FIFO in Java
			if(move.placed == Shape.X) outputWriter.write(";B["); //first player(black)
			else outputWriter.write(";W["); //second player(white)
			char xChar = (char)(move.coord.x + 'a'), yChar = (char)(move.coord.y + 'a'); //start at lowercase a
			//after using all 26 lowercase letters, move to uppercase letters
			if(move.coord.x > 'z') xChar += 'A' - 'z' - 1; //equals subtracting 58.
			if(move.coord.y > 'z') yChar += 'A' - 'z' - 1;
			outputWriter.write(new String(new char[]{xChar, yChar, ']'}));
		}
		outputWriter.write(')'); //mark end of the tree
		outputWriter.close();
	}
	private static void piriSave(final MnkGame game, final File file) throws IOException {
		//TODO: implement
		throw new IOException("incomplete method called");
	}
	public static MnkGame load(final File file, final int winStreak) throws IOException {
		MnkGame game = new MnkGame();
		InputStreamReader inputReader = new InputStreamReader(new FileInputStream(file));
		int skipper;
		Shape last = Shape.O;
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
				}
				else if(previous == ';' && (now == 'B' || now == 'W')) { //moves
					if(inputReader.skip(1) != 1) throw new IOException(); //process a [
					int x = inputReader.read() - 'a', y = inputReader.read() - 'a'; //recover indices from lowercase characters.
					//Recover indices from uppercase characters.
					if(x < 0) x -= 'A' - 'z' - 1; //equals adding 58.
					if(y < 0) y -= 'A' - 'z' - 1;
					last = now == 'B' ? Shape.X : Shape.O;
					game.place(x, y, last);
				}
				previous = now;
			}
			//Change the game's next Shape to the one next to the last Shape.
			//Of course, saving it in the file would be more efficient, but SGF probably doesn't support it.
			int i;
			for(i = 0;i < game.shapes.length;i++) if(game.shapes[i] == last) break;
			game.changeShape(i + 1); //nextIndex is initialized to 0, so after adding ((index of last) + 1), it will be pointing to the Shape next of last.
		}
		else { //PIRIMNK format
			//TODO: implement
			throw new IOException();
		}
		inputReader.close();
		game.winStreak = winStreak;
		return game;
	}
}
