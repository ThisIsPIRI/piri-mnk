package com.thisispiri.mnk;

import android.graphics.Point;
import java.util.LinkedList;

/**An implementation of {@link MnkAi} that evaluates values of every cell on the board by rudimentary means.*/
@SuppressWarnings("WeakerAccess") public class PiriMnkAi extends MnkAi {
	private class CellValue {
		final int[] enemyLines, ownLines; //enemyLines(that the cell can block with the cell) and ownLines(that can be extended to the cell)
		CellValue(int length) {
			enemyLines = new int[length];
			ownLines = new int[length];
		}
	}
	private final static Shape EMPTY = Shape.N;
	private final static int STREAK_SCALE = 4, OPEN_BONUS = 2, DOUBLE_OPEN_BONUS = 3, FULL_OPEN_BONUS_THRESHOLD = 4, OPPOSITE_OPEN_BONUS = 1;
	private int valueLength;
	private CellValue value;
	/**Has AI play a turn.
	 * Determines every cell's importance and returns a Point that contains the cell which the AI deemed has highest importance.
	 * Importance is represented by a {@link CellValue} which contains enemyLines and ownLines.
	 * Cells that can block a line of a higher number almost always have higher importance than cells that can block many lines of a lower number. It means values in latter indexes of the arrays are always considered first.
	 * If two cells can block lines of a same number, the cell that can block more lines has higher importance.
	 * If two or more cells seem to have same importance, put a stone on the cell with most blank spaces(that it can use to create lines). Due to this, the first move in a large board costs some time.
	 * @param game the game for the AI to play
	 * @return the {@code Point} to play on*/
	@Override Point playTurn(final MnkGame game) {
		this.game = game;
		valueLength = (game.winStreak + 1) * STREAK_SCALE;
		CellValue bestValue = new CellValue(valueLength); //the list of lines that a cell can block and has highest value
		LinkedList<Point> list = new LinkedList<>(); //list of cells to consider.
		//evaluation loop. checks all cells and evaluates them.
		for (int i = 0; i < game.getVerSize(); i++) {
			for (int j = 0; j < game.getHorSize(); j++) {
				evaluate(j, i);
				int k;
				for(k = valueLength - 1;k > 0;k--) {
					if(value.ownLines[k] > bestValue.ownLines[k]/* || (value.ownLines[k] == bestValue.ownLines[k] && value.enemyLines[k] > bestValue.enemyLines[k])*/) {
						bestValue = value;
						list.clear();
						list.addLast(new Point(j, i));
						break;
					}
					else if(value.ownLines[k] < bestValue.ownLines[k]) {
						break;
					}
					if(value.enemyLines[k] > bestValue.enemyLines[k]) {
						bestValue = value;
						list.clear();
						list.addLast(new Point(j, i));
						break;
					}
					else if(value.enemyLines[k] < bestValue.enemyLines[k]) {
						break;
					}
				}
				if(k == 0) {
					list.addLast(new Point(j, i));
				}
			}
		}
		//if more than one cell appears to have a same value, place a stone on the cell with most blank spaces to fill a line containing the cell with
		Point good = new Point();
		int max = -1, maxHolder;
		for (Point p : list) {
			maxHolder = cellSpaces(p.x, p.y);
			if (maxHolder > max) {
				good = p;
				max = maxHolder;
			}
		}
		return good;
	}

	private int cellSpaces(final int x, final int y) {
		int spaces = 0;
		int[] xP = {-1, 0, -1, -1}, yP = {0, -1, 1, -1};
		Shape original = game.array[y][x];
		boolean pastTheLine;
		for (int k = 0; k < 4; k++) {
			pastTheLine = false;
			if (inBoundary(y + yP[k], x + xP[k])) {
				int i, j;
				for (i = y + yP[k], j = x + xP[k]; inBoundary(i, j); i += yP[k], j += xP[k]) { //follow the line until it hits the other symbol or blank space.
					if (game.array[i][j] == EMPTY) {
						spaces++;
						pastTheLine = true;
					} else if (game.array[i][j] != original || pastTheLine) break;
				}
			}
			//right, down, right up, right down
			pastTheLine = false;
			if (inBoundary(y - yP[k], x - xP[k])) {
				int i, j;
				for (i = y - yP[k], j = x - xP[k]; inBoundary(i, j); i -= yP[k], j -= xP[k]) { //follow the line until it hits the other symbol or blank space.
					if (game.array[i][j] == EMPTY) {
						spaces++;
						pastTheLine = true;
					} else if (game.array[i][j] != original || pastTheLine) break;
				}
			}
		}
		return spaces;
	}

	private void evaluate(final int x, final int y) {
		value = new CellValue(valueLength);
		if (game.array[y][x] != Shape.N) { //if the cell is already filled, it has no importance.
			value.ownLines[valueLength - 1] = -1;
			return;
		}
		examineLine(x, y, -1, 0); // - shape
		examineLine(x, y, 0, -1); // | shape
		examineLine(x, y, -1, 1); // / shape
		examineLine(x, y, -1, -1); // \ shape
	}
	/**Examine a line(two if the two shapes on (x + xP, y + yP), (x - xP, y - yP) are different) and pass it to update()
	 * A cell being examined can block blockableLines[i] lines of i. e.g. if blockableLines[3] is 2, the cell can block 2 lines of 3 cells.
	 * If a line can't be a line of game.winStreak cells(because it's blocked by wall or another symbol), ignore it(represented by setting streak to 0. enemy/ownLines[0] is not considered in playTurn()).
	 * If a line is "open" (i.e. two ends are both empty), it receives bonus of OPEN_BONUS. if a cell can block two open lines of same length, they will be treated as one (the length) + DOUBLE_OPEN_BONUS line.
	 * If a cell's neighbor at the opposite of line is empty or a cell is in the middle of a line, the line receives bonus of OPPOSITE_OPEN_BONUS.
	 * @param xP the direction the function will add or subtract to x while examining
	 * @param yP the direction the function will add or subtract to y while examining*/
	private void examineLine(final int x, final int y, final int xP, final int yP) {
		int previousStreak, blank = 0, streak = 0;
		boolean isOpen = false, isOppositeOpen = false;
		Shape lineShape = EMPTY, firstShape;
		//left, up, left up, left down
		if (inBoundary(y + yP, x + xP) && game.array[y + yP][x + xP] != EMPTY) {
			streak = 1;
			lineShape = game.array[y + yP][x + xP];
			int i, j;
			for (i = y + yP * 2, j = x + xP * 2; inBoundary(i, j); i += yP, j += xP) { //follow the line until it hits the other symbol or blank space.
				if (game.array[i][j] == EMPTY) {
					isOpen = true;
					break;
				} else if (game.array[i][j] != lineShape) break;
				streak++;
			}
			blank = lineSpaces(j, i, x, y, xP, yP);
		}
		previousStreak = streak;
		if (streak + blank < game.winStreak)
			streak = 0; //if the line is impossible to complete, set streak to 0 so the cell won't receive any importance because of it.
		if(inBoundary(y - yP, x - xP) && game.array[y - yP][x - xP] == game.empty && lineShape != game.empty)
			isOppositeOpen = true;
		update(streak, blank, lineShape, isOpen, isOppositeOpen);
		firstShape = lineShape;
		//right, down, right up, right down
		streak = 0;
		blank = 0;
		isOppositeOpen = false;
		boolean isConnected = false;
		if (inBoundary(y - yP, x - xP) && game.array[y - yP][x - xP] != EMPTY) {
			if (lineShape == game.array[y - yP][x - xP]) {
				streak = previousStreak + 1;
				//blockableLines[previousStreak]--; //since we added 1 to blockableLines[previousStreak] before but we found that the line is continuous.
				isConnected = true;
			}
			else {
				streak = 1;
				lineShape = game.array[y - yP][x - xP];
			}
			int i, j;
			for (i = y - yP * 2, j = x - xP * 2; inBoundary(i, j); i -= yP, j -= xP) { //follow the line until it hits the other symbol or blank space.
				/*four cases:
				* 1. first line is connected to second one and open : since isOpen is not initialized after first scan, the line receives open bonus.
				* 2. first line is connected to second one but not open : second loop only sets isOpen to true if first line isn't connected to second one, so isOpen stays false.
				* 3. first line is not connected to second one and open : second loop sets isOpen to false at the end if the second line isn't open.
				* 4. first line is not connected to second one and not open : second loop behaves in the same way first loop does.*/
				if (game.array[i][j] == EMPTY) {
					if (!isConnected) isOpen = true;
					break;
				} else if (game.array[i][j] != lineShape) {
					isOpen = false;
					break;
				}
				streak++;
			}
			if (!inBoundary(i, j))
				isOpen = false; //loop is ended because of a wall == the line is blocked
			if(isConnected) blank = lineSpaces(x + (xP * previousStreak) + xP, y + (yP * previousStreak) + yP, j, i, xP, yP);
			else blank = lineSpaces(x, y, j, i, xP, yP);
		}
		else isOpen = false;
		if (streak + blank + (isConnected ? 1 : 0) < game.winStreak) //if the line is separated only by the cell, we need to add 1 to possible blank spaces(since the cell is blank, too)
			streak = 0; //if the line is impossible to complete, set streak to 0 so the cell won't receive any importance because of it.
		if(firstShape != lineShape && firstShape == game.empty) {
			isOppositeOpen = true;
		}
		update(streak, blank, lineShape, isOpen, isOppositeOpen || isConnected);
	}

	/**Returns how many blank cells a line can use to complete itself.*/
	private int lineSpaces(int x1, int y1, int x2, int y2, final int xP, final int yP) {
		int blank = 0;
		if (inBoundary(y1, x1)) {
			for (; inBoundary(y1, x1); y1 += yP, x1 += xP) {
				if (game.array[y1][x1] != game.empty) break;
				blank++;
			}
		}
		if (inBoundary(y2, x2)) {
			for (; inBoundary(y2, x2); y2 -= yP, x2 -= xP) {
				if (game.array[y2][x2] != game.empty) break;
				blank++;
			}
		}
		return blank;
	}
	private void update(int streak, final int blank, final Shape lineShape, final boolean isOpen, final boolean isConnectedOrOppositeOpen) {
		streak *= STREAK_SCALE;
		int[] target;
		if(game.shapes[game.getNextIndex()] == lineShape) target = value.ownLines;
		else target = value.enemyLines;
		if(isConnectedOrOppositeOpen) streak += OPPOSITE_OPEN_BONUS;
		if (isOpen) {
			streak += OPEN_BONUS / (blank >= FULL_OPEN_BONUS_THRESHOLD ? 1 : 2);
			if(target[streak] == 1) { //if the cell can block two lines of x cells, mark it as an open line of x + DOUBLE_OPEN_BONUS cells.
				target[streak] = 0;
				if(streak + DOUBLE_OPEN_BONUS >= target.length) streak = target.length - DOUBLE_OPEN_BONUS - 1;
				target[streak + DOUBLE_OPEN_BONUS]++;
				return;
			}
		}
		if(streak >= target.length) streak = target.length - 1;
		target[streak]++;
	}
}