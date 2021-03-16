package com.thisispiri.mnk;

import com.thisispiri.common.Point;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**A representation of an MNK game.*/
public class MnkGame { //TODO: Support renju
	/**The state of the board, on which lie the {@link Shape}s of the game.*/
	public Shape[][] array;
	/**The array of shapes to be placed rotationally in a normal game.*/
	public final Shape[] shapes = {Shape.X, Shape.O};
	/**The list of shapes for searching*/
	private final List<Shape> shapesList = Arrays.asList(shapes);
	/**The {@link Shape} representing an empty cell.*/
	public final Shape empty = Shape.N;
	/**The list of all {@link Move}s made in the game. Earlier moves are stored first.*/
	public Stack<Move> history = new Stack<>(); //Give initialize() something to clear when the copy constructor is used
	private int horSize, verSize;
	public int getHorSize() {return horSize;}
	public int getVerSize() {return verSize;}
	/**The number of shapes in a line it takes to win.*/
	public int winStreak;
	/**The index of the {@link Shape} to be placed next in {@link MnkGame#shapes}.*/
	private int nextIndex = 0;
	private final static int[] xP = {1, 0, 1, 1}, yP = {0, 1, -1, 1};
	/**Returns the index of the next {@link Shape} to be placed in {@link MnkGame#shapes}.
	 * @return The index of the next {@link Shape} to be placed.*/
	public int getNextIndex() {return nextIndex;}
	/**Initializes the game with the default size of 15 * 15 and winning length of 5.*/
	public MnkGame() {
		this(15, 15, 5);
	}
	/**Initializes the game with the supplied arguments.*/
	public MnkGame(int horSize, int verSize, int winStreak) {
		setSize(horSize, verSize); //Will initialize the game, assuming the sizes aren't 0
		this.winStreak = winStreak;
	}
	/**Shallow copies the supplied {@link MnkGame}.*/
	public MnkGame(final MnkGame original) {
		setSize(original.getHorSize(), original.getVerSize());
		array = original.array;
		//shapes = original.shapes;
		//shapesList = Arrays.asList(shapes);
		//empty = original.empty;
		history = original.history;
		winStreak = original.winStreak;
		nextIndex = original.nextIndex;
	}
	/**Places a {@link Shape} at the supplied coordinate. Does not check if the move is illegal(the Shape may be placed on another, already placed, one).
	 * Changes the result of {@link MnkGame#getNextIndex()}.
	 * @return {@code true} if a {@link Shape} was successfully placed. {@code false} if it failed to place it(because the coordinate was out of boundaries).*/
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
	 * @return Whether a Move was actually removed. False if the board was empty.*/
	public boolean revertLast() {
		if(history.size() > 0) {
			Move m = history.pop();
			array[m.coord.y][m.coord.x] = m.prev;
			nextIndex = shapesList.indexOf(m.placed);
			return true;
		}
		else return false;
	}
	/**Changes {@link MnkGame#nextIndex} to ((current turn) + steps))th turn's, assuming the game went "normally" and no player gave up any turns.
	 * To put simply, nextIndex = (nextIndex + steps) % {@link MnkGame#shapes}.length
	 * @param steps The number to add to the turn.*/
	public void changeShape(final int steps) {
		nextIndex = getNextIndexAt(steps);
	}
	/**Returns {@link MnkGame#nextIndex} at ((current turn) + steps)th turn, without actually changing the result of {@link MnkGame#getNextIndex()}.
	 * @param steps The number to add to the turn.
	 * @return The next shape at the turn.*/
	public int getNextIndexAt(final int steps) {
		return Math.abs((nextIndex + steps) % shapes.length);
	}
	/**Sets the size of the board.
	 * @param hor The horizontal length of the board in the number of tiles.
	 * @param ver The vertical length of the board in the number of tiles.
	 * @return Whether the game was initialized(because of a change in the board size) or not.*/
	public boolean setSize(final int hor, final int ver) {
		if(horSize != hor || verSize != ver) {
			horSize = hor; verSize = ver;
			array = new Shape[verSize][horSize];
			initialize();
			return true;
		}
		else return false;
	}
	/**Checks if someone won, counting lines with more than {@link MnkGame#winStreak} lines as winning lines also.
	 * @see MnkGame#checkWin(int, int, boolean)*/
	public Point[] checkWin(final int x, final int y) {return checkWin(x, y, false);}
	/**Checks if someone won by placing a {@link Shape} on the supplied coordinate.
	 * To know who(what {@link Shape}) won, use {@link MnkGame#shapes}[{@link MnkGame#getNextIndex()}].
	 * Note that <b>only lines that contain the supplied cell will be checked.</b>
	 * @param exact Whether to match only lines with exactly {@link MnkGame#winStreak} {@link Shape}s.
	 * @return An array of {@code Point} responsible for the win. {@code null} if no one won.
	 * The array will only contain {@link MnkGame#winStreak} Points even if the line is longer than that.
	 * The Points are stored from bottom to top if the line is vertical, right to left if horizontal,
	 * top right to bottom left if a /-shaped diagonal and bottom right to top left if a \-shaped diagonal.*/
	public Point[] checkWin(final int x, final int y, final boolean exact) {
		//TODO: Merge with EmacsGomokuAi's pointer system?
		final Point[] starting = {new Point(0, y), new Point(x, 0),
				new Point((x + y) - Math.min(x + y, verSize - 1), Math.min(x + y, verSize - 1)),
				new Point(Math.max(x - y, 0), Math.max(y - x, 0))};
		for(int i = 0;i < xP.length;i++) {
			int streak = 0;
			Point p = starting[i];
			while(inBoundary(getNextPoint(p, xP[i], yP[i]))) {
				streak++;
				Point n = getNextPoint(p, xP[i], yP[i]);
				if(array[p.y][p.x] != array[n.y][n.x] || isEmpty(p.x, p.y)) {
					if(streak == winStreak) //Exact matches not including the last cell
						return getLinePoints(winStreak, p.x, p.y, -xP[i], -yP[i]);
					streak = 0;
				}
				//All non-exact matches and exact matches including the last cell
				if(streak == winStreak - 1 && (!exact || !inBoundary(getNextPoint(n, xP[i], yP[i]))))
					return getLinePoints(winStreak, n.x, n.y, -xP[i], -yP[i]);
				p = n;
			}
		}
		return null;
	}
	private Point getNextPoint(final Point p, final int xPlus, final int yPlus) {
		return new Point(p.x + xPlus, p.y + yPlus);
	}
	/**Returns the {@code Point}s consisting a line.
	 * @param length The length of the line.
	 * @param xPlus The direction the X coordinate progresses in from startX on the line.
	 * @param yPlus The direction the Y coordinate progresses in from startY on the line.
	 * @return The {@code Point}s consisting the line.*/
	private static Point[] getLinePoints(final int length, int startX, int startY, final int xPlus, final int yPlus) {
		Point[] arr = new Point[length];
		for(int i = 0;i < length;startX += xPlus, startY += yPlus, i++) {
			arr[i] = new Point(startX, startY);
		}
		return arr;
	}
	public boolean isEmpty(final Point p) {return isEmpty(p.x, p.y);}
	/**Returns {@code true} if (x, y) is within the boundary and is empty.*/
	public boolean isEmpty(final int x, final int y) {return inBoundary(y, x) && array[y][x] == empty;}
	public boolean inBoundary(final Point p) {return inBoundary(p.y, p.x);}
	public boolean inBoundary(final int y, final int x) {return y >= 0 && y < verSize && x >= 0 && x < horSize;}
}
