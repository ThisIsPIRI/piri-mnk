package com.thisispiri.mnk;
import android.graphics.Point;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;
/**A representation of an MNK game.*/
public class MnkGame {
	Shape[][] array; //TODO: make this private
	/**The array of shapes to be placed rotationally in a normal game.*/
	public final Shape[] shapes = {Shape.X, Shape.O};
	/**The list of shapes for searching*/
	private final List<Shape> shapesList = Arrays.asList(shapes);
	/**The {@link Shape} representing an empty cell.*/
	public final Shape empty = Shape.N;
	/**The list of all {@link Move}s made in the game. Earlier moves are stored first.*/
	final Stack<Move> history = new Stack<>();
	private int horSize = 15, verSize = 15;
	public int getHorSize() {return horSize;}
	public int getVerSize() {return verSize;}
	/**The number of shapes in a line it takes to win.*/
	public int winStreak = 5;
	/**The index of the {@link Shape} to be placed next in {@link MnkGame#shapes}.*/
	private int nextIndex = 0;
	/**Returns the index of the next {@link Shape} to be placed in {@link MnkGame#shapes}.
	 * @return The index of the next {@link Shape} to be placed.*/
	public int getNextIndex() {return nextIndex;}
	/**Initializes the game with the default size of 15 * 15 and winning length of 5.*/
	MnkGame() {
		array = new Shape[verSize][horSize];
		initialize();
	}
	/**Places a {@link Shape} at the supplied coordinate. Does not check if the move is illegal(the Shape may be placed on another, already placed, one).
	 * Changes the result of {@link MnkGame#getNextIndex()}.
	 * @return {@code true} if a {@link Shape} was successfully placed. {@code false} if it failed to place it(because the coordinate was out of the boundaries).*/
	public boolean place(final int x, final int y) {
		if(place(x, y, shapes[nextIndex])) {
			changeShape(1);
			return true;
		}
		else return false;
	}
	/**Places the shape at the location. This method does not change the next {@link Shape} to be placed.
	 * Does not change the result of {@link MnkGame#getNextIndex()}.
	 * @param toPlace The shape to place at the location.
	 * @return {@code true} if a {@link Shape} was successfully placed. {@code false} if it failed to place it.*/
	public boolean place(final int x, final int y, final Shape toPlace) {
		if(inBoundary(y, x)) {
			history.push(new Move(new Point(x, y), toPlace, array[y][x]));
			array[y][x] = toPlace;
			return true;
		}
		return false;
	}
	/**Restores the game to the initial state before any move was made.*/
	public void initialize() {
		for(int i = 0;i < verSize;i++)
			Arrays.fill(array[i], empty);
		nextIndex = 0;
		history.clear();
	}
	/**Reverts the last {@link Move}, restoring the state of the game before the Move was made including {@link MnkGame#nextIndex}.
	 * @return If a Move was actually removed. False if the board was empty.*/
	public boolean revertLast() {
		if(history.size() > 0) {
			Move m = history.pop();
			array[m.coord.y][m.coord.x] = m.prev;
			nextIndex = shapesList.indexOf(m.placed);
			return true;
		}
		return false;
	}
	/**Changes {@link MnkGame#nextIndex} to ((current turn) + steps))th turn's, assuming the game went "normally" and the change side button hasn't been used.
	 * To put simply, nextIndex = (nextIndex + steps) % {@link MnkGame#shapes}.length
	 * @param steps The number to add to the turn.*/
	public void changeShape(final int steps) {
		nextIndex = Math.abs((nextIndex + steps) % shapes.length);
	}
	/**Sets the size of the board.
	 * @param hor The horizontal length of the board in the number of tiles.
	 * @param ver The vertical length of the board in the number of tiles.
	 * @return Whether the game was initialized(because of the change in the board size) or not.*/
	public boolean setSize(final int hor, final int ver) {
		int previousHor = horSize, previousVer = verSize;
		horSize = hor; verSize = ver;
		if(previousHor != horSize || previousVer != verSize) {
			array = new Shape[verSize][horSize];
			initialize();
			return true;
		}
		else return false;
	}
	/**Checks if someone won by placing a {@link Shape} on the supplied coordinate. To know who(what {@link Shape}) won, use {@link MnkGame#shapes}[{@link MnkGame#getNextIndex()}].
	 * @return An array of {@code Point} responsible for the win. {@code null} if no one won. If the length of the line exceeds {@link MnkGame#winStreak}, the array will not contain all of them.*/
	public Point[] checkWin(final int x, final int y) {
		int i, j, streak = 0;
		//vertical check
		for(i = 0;i + 1 < verSize;i++) {
			streak++;
			if (array[i][x] != array[i + 1][x] || array[i][x] == Shape.N) streak = 0;
			if(streak == winStreak - 1) return getLinePoints(winStreak, x, i + 1, 0, -1);
		}
		//horizontal check
		streak = 0;
		for(i = 0;i + 1 < horSize;i++) {
			streak++;
			if(array[y][i] != array[y][i + 1] || array[y][i] == Shape.N) streak = 0;
			if(streak == winStreak - 1) return getLinePoints(winStreak, i + 1, y, -1, 0);
		}
		//diagonal check / shape
		streak = 0;
		i = x + y >= verSize ? verSize - 1 : x + y;
		j = (x + y) - i;
		while(inBoundary(i - 1, j + 1)) {
			streak++;
			if(array[i][j] != array[i - 1][j + 1] || array[i][j] == Shape.N) streak = 0;
			if(streak == winStreak - 1) return getLinePoints(winStreak, j + 1, i - 1, -1, 1);
			i--; j++;
		}
		//diagonal check \ shape
		streak = 0;
		i = y - x < 0 ? 0 : y - x;
		j = x - y < 0 ? 0 : x - y;
		while(i + 1 < verSize && j + 1 < horSize) {
			streak++;
			if(array[i][j] != array[i + 1][j + 1] || array[i][j] == Shape.N) streak = 0;
			if(streak == winStreak - 1) return getLinePoints(winStreak, j + 1, i + 1, -1, -1);
			i++; j++;
		}
		return null;
	}
	/**Returns the {@code Point}s consisting a line.
	 * @param length The length of the line.
	 * @param xP The direction the X coordinate progresses toward from startX on the line.
	 * @param yP The direction the Y coordinate progresses toward from startY on the line.
	 * @return The {@code Point}s consisting the line.*/
	private Point[] getLinePoints(final int length, int startX, int startY, final int xP, final int yP) {
		Point[] arr = new Point[length];
		for(int i = 0;i < length;startX += xP, startY += yP, i++) {
			arr[i] = new Point(startX, startY);
		}
		return arr;
	}
	public boolean isEmpty(final int x, final int y) {return array[y][x] == empty;}
	public boolean inBoundary(final int y, final int x) {return y >= 0 && y < verSize && x >= 0 && x < horSize;}
	//TODO: make a method to dump training data
}
