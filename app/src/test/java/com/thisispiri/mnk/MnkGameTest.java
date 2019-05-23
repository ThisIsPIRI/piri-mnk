package com.thisispiri.mnk;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.function.Consumer;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class MnkGameTest {
	@Test public void test1() throws Exception {
		MnkGame game = new MnkGame();
		game.setSize(11, 19);
		//Test if it returns its size correctly
		assertEquals(11, game.getHorSize());
		assertEquals(19, game.getVerSize());

		game.winStreak = 4;
		assertNull(game.checkWin(5, 14));
		game.place(3, 3);
		assertEquals(Shape.X, game.array[3][3]);
		game.place(3, 3);
		assertEquals(Shape.O, game.array[3][3]);
		game.revertLast();
		assertEquals(Shape.X, game.array[3][3]);
		game.place(3, 4, Shape.X);
		game.place(3, 5, Shape.X);
		assertNull(game.checkWin(3, 5));
		game.place(3, 6, Shape.X);
		assertNotNull(game.checkWin(3, 6));
		game.changeShape(1);
		assertEquals(Shape.X, game.shapes[game.getNextIndex()]);
		game.place(3, 5, Shape.N);
		assertNull(game.checkWin(3, 6));
		assertFalse(game.place(5, 19));

		//Test if it initializes the game correctly
		game.initialize();
		forAllCellOn(game, (shape -> assertEquals(Shape.N, shape)));

		//Test nextIndex
		assertEquals(0, game.getNextIndex());
		game.place(1, 1);
		assertEquals(1, game.getNextIndex());
		//Test if game.place(int, int, Shape) changes nextIndex
		game.place(5, 7, Shape.X);
		assertEquals(1, game.getNextIndex());

		//Test getNextIndexAt
		assertEquals(0, game.getNextIndexAt(5));

		//Test changeShape
		game.changeShape(1);
		assertEquals(0, game.getNextIndex());
		game.changeShape(2);
		assertEquals(0, game.getNextIndex());
	}
	@Test public void testCheckWinArray() {
		//TODO: Make getLinePoints public/move it to a utility class?
		MnkGame game = new MnkGame(20, 20, 5);
		// vertical
		for(int i = 0;i < 20;i++)
			game.place(0, i, Shape.O);
		Point[] expected = {new Point(0, 4), new Point(0, 3), new Point(0, 2), new Point(0, 1), new Point(0,0)};
		assertArrayEquals(expected, game.checkWin(0, 19));
		assertNull(game.checkWin(1, 19));
		game = new MnkGame(game);
		game.winStreak = 21;
		assertNull(game.checkWin(0, 19));
		game.winStreak = 20;
		expected = new Point[20];
		for(int i = 0;i < 20;i++) expected[i] = new Point(0, 19 - i);
		assertArrayEquals(expected, game.checkWin(0, 0));
		// \ diagonal
		game.winStreak = 5;
		expected = new Point[5];
		for(int i = 7;i > 7 - 5;i--) {
			expected[7 - i] = new Point(i, i + 4);
			game.place(i, i + 4, Shape.X);
		}
		assertArrayEquals(expected, game.checkWin(6, 10));
		// / diagonal
		expected = new Point[3];
		expected[0] = new Point(2, 2); expected[1] = new Point(1, 3); expected[2] = new Point(0, 4);
		game.setSize(4, 5);
		game.winStreak = 3;
		game.place(0, 4, Shape.X); game.place(1, 3, Shape.X); game.place(2, 2, Shape.X);
		assertArrayEquals(expected, game.checkWin(3, 1));
	}
	@Test public void testCheckWinExact() {
		MnkGame game = new MnkGame(5, 5, 3);
		// / diagonal
		Point[] expected = {new Point(4, 0), new Point(3, 1), new Point(2, 2)};
		game.place(4, 0, Shape.X); game.place(3, 1, Shape.X); game.place(2, 2, Shape.X); game.place(1, 3, Shape.O);
		assertArrayEquals(expected, game.checkWin(4, 0, true));
		game.place(1, 3, Shape.X);
		assertNull(game.checkWin(4, 0, true));
		assertNotNull(game.checkWin(4, 0, false));
		// horizontal
		game.setSize(541, 433);
		game.winStreak = 150;
		expected = new Point[150];
		for(int i = 0;i < 150;i++) {
			game.place(i + 100, 150, Shape.O);
			expected[i] = new Point(249 - i, 150);
		}
		assertArrayEquals(expected, game.checkWin(6, 150, true));
		game.place(250, 150, Shape.X);
		assertNotNull(game.checkWin(6, 150, true));
		game.place(250, 150, Shape.O);
		assertNull(game.checkWin(6, 150, true));
		assertNotNull(game.checkWin(6, 150, false));
		for(int i = 0;i < 541; i++)
			game.place(i, 150, Shape.O);
		assertNull(game.checkWin(6, 150, true));
		assertNotNull(game.checkWin(6, 150, false));
	}
	private void forAllCellOn(MnkGame game, Consumer<Shape> action) {
		for(int i = 0;i < game.getVerSize();i++) {
			for(int j = 0;j < game.getHorSize();j++) {
				action.accept(game.array[i][j]);
			}
		}
	}
}