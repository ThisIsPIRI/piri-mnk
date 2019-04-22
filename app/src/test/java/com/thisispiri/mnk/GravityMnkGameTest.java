package com.thisispiri.mnk;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class GravityMnkGameTest {
	@Test public void testPlaying() {
		GravityMnkGame game = new GravityMnkGame();
		game.setSize(10, 50);
		game.place(1, 2);
		assertEquals(game.empty, game.array[2][1]);
		assertEquals(Shape.X, game.array[49][1]);
		game.place(2, 49);
		game.place(5, 20, Shape.X, false);
		assertEquals(Shape.X, game.array[20][5]);
		game.place(5, 10);
		assertEquals(Shape.X, game.array[19][5]);
		game.place(5, 20);
		assertEquals(Shape.O, game.array[49][5]);
	}
	@Test public void testCopying() {
		LegalMnkGame normalGame = new LegalMnkGame();
		normalGame.setSize(4, 4);
		normalGame.place(0, 0); normalGame.place(1, 0); normalGame.place(2, 0); normalGame.place(3, 0);
		normalGame.changeShape(1);
		GravityMnkGame gravityGame = new GravityMnkGame(normalGame);
		Shape[] expected = {Shape.X, Shape.O, Shape.X, Shape.O};
		assertArrayEquals(expected, gravityGame.array[0]);
		gravityGame.place(2, 1);
		assertEquals(gravityGame.empty, gravityGame.array[1][2]);
		assertEquals(Shape.O, gravityGame.array[3][2]);
	}
}
