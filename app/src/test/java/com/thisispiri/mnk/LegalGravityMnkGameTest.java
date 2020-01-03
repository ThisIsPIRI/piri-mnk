package com.thisispiri.mnk;

import org.junit.Test;

import static org.junit.Assert.*;

public class LegalGravityMnkGameTest {
	@Test public void testPlaying() {
		GravityMnkGame game = new LegalGravityMnkGame();
		game.setSize(10, 50);
		game.place(1, 2);
		assertEquals(game.empty, game.array[2][1]);
		assertEquals(Shape.X, game.array[49][1]);
		game.place(2, 49);
		game.place(5, 20, Shape.X);
		assertEquals(Shape.X, game.array[49][5]);
		game.place(5, 10);
		assertEquals(Shape.O, game.array[48][5]);
		game.place(5, 20);
		assertEquals(Shape.X, game.array[47][5]);
	}
	@Test public void testRejecting() {
		GravityMnkGame game = new LegalGravityMnkGame();
		game.setSize(5, 5);
		assertFalse(game.place(2, 2, Shape.O));
		assertFalse(game.place(7, 7));
		assertFalse(game.place(2, 2, Shape.X, false));
		assertTrue(game.place(2, 2, Shape.X, true));
		assertTrue(game.place(2, 2));
		assertEquals(Shape.X, game.array[4][2]);
	}
}
