package com.thisispiri.mnk;

import com.thisispiri.common.Point;

import java.util.LinkedList;
import java.util.Locale;

/**This is the oldest surviving version of PIRI Value AI, with some refactoring to make it work on the current version of PIRI MNK.*/
public class PiriValue01Ai implements MnkAi {
	private static class Pair<LEFT, RIGHT> {
		final LEFT first;
		final RIGHT second;
		Pair(LEFT l, RIGHT r) {
			this.first = l;
			this.second = r;
		}
	}
	private MnkGame game;
	/** has AI play a turn.
	  * determine every cell's importance and place a stone on the cell with highest importance.
	  * cells that can block a line of a higher number always have higher importance than cells that can block many lines of a lower number.
	  * if two cells can block lines of a same number, the cell that can block more lines has higher importance.
	  * if a line can't be a line of maxStreak cells(because it's blocked by wall or the other symbol), ignore it.
	  * cells that can end game(by granting the player win) have infinite importance(represented as 9999999)
	*/
	@Override public MnkAiDecision playTurnJustify(final MnkGame game) {
		return play(game, true);
	}
	@Override public Point playTurn(final MnkGame game) {
		return play(game, false).coord;
	}
	private MnkAiDecision play(final MnkGame game, final boolean justify) { //the parameter will be true if executed inside a thread.
		this.game = game;
		int maxStreak = 0, maxLine = 0; //maximum streak appeared until now in the evaluation loop, maximum number of lines of maxStreak cells that can be blocked by a cell
		Pair<Integer, Integer> temp;
		final LinkedList<Point> list = new LinkedList<>(); //list of cells to consider.
		final String[][] justification = new String[game.getVerSize()][game.getHorSize()];
		//evaluation loop. checks all cells and evaluates them.
		for(int i = 0;i < game.getVerSize();i++) {
			for(int j = 0;j < game.getHorSize();j++) {
				temp = evaluate(j, i);
				streak = this.maxStreak = this.maxLine = 0; //after using evaluate(), initialize fields it uses.
				if(temp.first > maxStreak) { //if a cell that can block a line of more cells than maxStreak or two, update maxStreak and clear list.
					maxStreak = temp.first;
					maxLine = temp.second;
					list.clear();
					list.addLast(new Point(j, i));
				}
				else if(temp.first == maxStreak) {
					if(temp.second > maxLine) { //if a cell that can block more lines of maxStreak than maxLine, update maxLine and clear list.
						maxLine = temp.second;
						list.clear();
						list.addLast(new Point(j, i));
					}
					else if(temp.second == maxLine) //if a cell that can block maxLine lines of maxStreak, add it to list.
						list.addLast(new Point(j, i));
				}
				if(justify)
					justification[i][j] = String.format(Locale.US, "%d,%d", temp.first, temp.second);
			}
		}
		return new MnkAiDecision(new Point(list.get(0).x, list.get(0).y), justification);
	}

	//first : maximum length of lines the cell can block. second : the value(how many lines of [first] cells it can block) of the cell.
	private int maxStreak = 0, maxLine = 0, streak = 0;

	private Pair<Integer, Integer> evaluate(final int x, final int y) {
		if(game.array[y][x] == Shape.O || game.array[y][x] == Shape.X)
			return new Pair<>(-1, -1); //if the cell is already filled, it has no importance.
		examineLine(x, y, -1, 0); // - shape
		examineLine(x, y, 0, -1); // | shape
		examineLine(x, y, -1, 1); // / shape
		examineLine(x, y, -1, -1); // \ shape
		return new Pair<>(maxStreak, maxLine);
	}
	//the core of this class. examine a line(two if we think the line is separated by this cell) and set maxStreak(if the line is longer than any other lines adjacent to the cell) and maxLine(if the line's length is maxStreak)
	private void examineLine(final int x, final int y, final int xP, final int yP) {
		int previousStreak, blank = 0;
		Shape previous = Shape.N;
		//left, up, left up, left down
		streak = 0; //in case !game.inBoundary(y + yP, x + xP)
		if(game.inBoundary(y + yP, x + xP)) if (game.array[y + yP][x + xP] != Shape.N) {
			streak = 1;
			previous = game.array[y + yP][x + xP];
			int i, j;
			for (i = y + yP * 2, j = x + xP * 2; game.inBoundary(i, j); i += yP, j += xP) { //follow the line until it hits the other symbol or blank space.
				if (game.array[i][j] == Shape.N || game.array[i][j] != previous) break;
				streak++;
			}
			blank = spaces(j, i, x, y, xP, yP);
		}
		previousStreak = streak;
		if(streak + blank < game.winStreak) streak = 0; //if the line is impossible to complete, set streak to 0 so the cell won't receive any importance because of it.
		checkUpdate();
		if(game.shapes[game.getNextIndex()] == previous && streak + 1 == game.winStreak) {
			maxStreak = 9999999;
			return;
		}
		//right, down, right up, right down
		streak = 0; //in case !game.inBoundary(y - xP, x - xP)
		blank = 0;
		if(game.inBoundary(y - yP, x - xP)) if (game.array[y - yP][x - xP] != Shape.N) {
			if (previous == game.array[y - yP][x - xP])
				streak = previousStreak + 1;
			else {
				streak = 1;
				previous = game.array[y - yP][x - xP];
			}
			int i, j;
			for(i = y - yP * 2, j = x - xP * 2;game.inBoundary(i, j);i -= yP, j -= xP) { //follow the line until it hits the other symbol or blank space.
				if (game.array[i][j] == Shape.N || game.array[i][j] != previous) break;
				streak++;
			}
			blank = spaces(x, y, j, i, xP, yP);
		}
		if(streak + blank < game.winStreak) streak = 0; //if the line is impossible to complete, set streak to 0 so the cell won't receive any importance because of it.
		checkUpdate();
		if(game.shapes[game.getNextIndex()] == previous && streak + 1 == game.winStreak) {
			maxStreak = 9999999;
		}
	}
	//returns how many blank spaces a line can use to complete itself.
	private int spaces(int x1, int y1, int x2, int y2, final int xP, final int yP) {
		int blank = 0;
		if(game.inBoundary(y1, x1)) {
			for(;game.inBoundary(y1, x1);y1 += yP, x1 += xP) {
				if(game.array[y1][x1] != Shape.N) break;
				blank++;
			}
		}
		if(game.inBoundary(y2, x2)) {
			for(;game.inBoundary(y2, x2);y2 -= yP, x2 -= xP) {
				if(game.array[y2][x2] != Shape.N) break;
				blank++;
			}
		}
		return blank;
	}

	private void checkUpdate() {
		//check if we should update something
		if(streak > maxStreak) { //streak is higher than maxStreak. initialize maxLine too, since maxLine is the maximum number of lines of "maxStreak".
			maxStreak = streak;
			maxLine = 1;
		}
		else if(streak == maxStreak) //streak is as same as maxStreak.
			maxLine++;
	}
}