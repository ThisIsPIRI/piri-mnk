package com.thisispiri.mnk;

import com.thisispiri.common.Point;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**A base implementation of {@link MnkGame}.*/
public class BaseMnkGame implements MnkGame { //TODO: Support renju
	public Shape[][] array;
	public final Shape[] shapes = {Shape.X, Shape.O};
	/**The list of shapes for searching*/
	private final List<Shape> shapesList = Arrays.asList(shapes);
	/**The {@link Shape} representing an empty cell.*/
	private final Shape empty = Shape.N;
	public Stack<Move> history = new Stack<>(); //Give initialize() something to clear when the copy constructor is used
	private int horSize, verSize;
	@Override public int getHorSize() {return horSize;}
	@Override public int getVerSize() {return verSize;}
	private int winStreak; //Making this public causes overload resolution ambiguity when accessing from Kotlin
	private int nextIndex = 0;
	private final static int[] xP = {1, 0, 1, 1}, yP = {0, 1, -1, 1};

	//Kotlin interop
	@Override public int getWinStreak() { return winStreak; }
	@Override public void setWinStreak(int value) { winStreak = value; }
	@Override public Shape[][] getArray() { return array; }
	@Override public Shape[] getShapes() { return shapes; }
	@Override public Stack<Move> getHistory() { return history; }

	/**Returns the index of the next {@link Shape} to be placed in {@link MnkGame#shapes}.
	 * @return The index of the next {@link Shape} to be placed.*/
	@Override public int getNextIndex() {return nextIndex;}

	/**Initializes the game with the default size of 15 * 15 and winning length of 5.*/
	public BaseMnkGame() {
		this(15, 15, 5);
	}
	/**Initializes the game with the supplied arguments.*/
	public BaseMnkGame(int horSize, int verSize, int winStreak) {
		setSize(horSize, verSize); //Will initialize the game, assuming the sizes aren't 0
		this.winStreak = winStreak;
	}
	/**Shallow copies the supplied {@link MnkGame}.*/
	public BaseMnkGame(final MnkGame original) {
		setSize(original.getHorSize(), original.getVerSize());
		array = original.getArray();
		//shapes = original.shapes;
		//shapesList = Arrays.asList(shapes);
		//empty = original.empty;
		history = original.getHistory();
		winStreak = original.getWinStreak();
		nextIndex = original.getNextIndex();
	}

	@Override public boolean place(final int x, final int y) {
		if(place(x, y, shapes[nextIndex])) {
			changeShape(1);
			return true;
		}
		else return false;
	}
	@Override public boolean place(final int x, final int y, final Shape toPlace) {
		if(inBoundary(y, x)) {
			history.push(new Move(new Point(x, y), toPlace, array[y][x]));
			array[y][x] = toPlace;
			return true;
		}
		else return false;
	}

	@Override public void initialize() {
		for(int i = 0;i < verSize;i++)
			Arrays.fill(array[i], empty);
		nextIndex = 0;
		history.clear();
	}

	@Override public boolean revertLast() {
		if(history.size() > 0) {
			Move m = history.pop();
			array[m.coord.y][m.coord.x] = m.prev;
			nextIndex = shapesList.indexOf(m.placed);
			return true;
		}
		else return false;
	}

	@Override public void changeShape(final int steps) {
		nextIndex = getNextIndexAt(steps);
	}

	@Override public int getNextIndexAt(final int steps) {
		return Math.abs((nextIndex + steps) % shapes.length);
	}

	@Override public boolean setSize(final int hor, final int ver) {
		if(horSize != hor || verSize != ver) {
			horSize = hor; verSize = ver;
			array = new Shape[verSize][horSize];
			initialize();
			return true;
		}
		else return false;
	}

	@Override public Point[] checkWin(final int x, final int y) {return checkWin(x, y, false);}
	/**{@inheritDoc}
	 * The array will only contain {@link MnkGame#getWinStreak} Points even if the line is longer than that.
	 * The Points are stored from bottom to top if the line is vertical, right to left if horizontal,
	 * top right to bottom left if a /-shaped diagonal and bottom right to top left if a \-shaped diagonal.*/
	@Override public Point[] checkWin(final int x, final int y, final boolean exact) {
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

	@Override public boolean isEmpty(final Point p) { return isEmpty(p.x, p.y); }
	@Override public boolean isEmpty(final int x, final int y) { return inBoundary(y, x) && array[y][x] == empty; }
	@Override public boolean isEmpty(final Shape s) { return s == empty; }
	@Override public boolean inBoundary(final Point p) { return inBoundary(p.y, p.x); }
	@Override public boolean inBoundary(final int y, final int x) { return y >= 0 && y < verSize && x >= 0 && x < horSize; }
}
