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
		MnkGame game0 = new MnkGame(20, 20, 5);
		for(int i = 0;i < 20; i++)
			game0.place(0, i, Shape.O);
		Point[] expected = {new Point(0, 4), new Point(0, 3), new Point(0, 2), new Point(0, 1), new Point(0,0)};
		assertArrayEquals(expected, game0.checkWin(0, 19));
		assertNull(game0.checkWin(1, 19));
		MnkGame game1 = new MnkGame(game0);
		game1.winStreak = 21;
		assertNull(game1.checkWin(0, 19));
	}
	private void forAllCellOn(MnkGame game, Consumer<Shape> action) {
		for(int i = 0;i < game.getVerSize();i++) {
			for(int j = 0;j < game.getHorSize();j++) {
				action.accept(game.array[i][j]);
			}
		}
	}
}