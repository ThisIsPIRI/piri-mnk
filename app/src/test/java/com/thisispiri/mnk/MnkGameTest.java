package com.thisispiri.mnk;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class MnkGameTest {
	private MnkGame game;
	@Test public void test1() throws Exception {
		game = new MnkGame();
		//assertEquals(5, new Point(5, 1).x);
		game.setSize(11, 19);
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
	}
}