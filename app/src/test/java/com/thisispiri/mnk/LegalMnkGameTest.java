package com.thisispiri.mnk;

import org.junit.Test;

import static org.junit.Assert.*;

public class LegalMnkGameTest {
	@Test public void test1() {
		LegalMnkGame game = new LegalMnkGame(new BaseMnkGame());
		game.setSize(16, 16);
		//Test if it rejects place(int, int, Shape) calls
		assertFalse(game.place(5, 5, Shape.O));
		assertFalse(game.place(5, 5, Shape.N));
		assertFalse(game.place(5, 5, Shape.X));
		//Test if it places properly without an explicit Shape given
		game.place(5, 5);
		game.place(5, 6);
		assertTrue(game.place(5, 7));
		//Test if it refuses to place a stone on a non-empty cell
		assertFalse(game.place(5, 5));
		//Test if it refuses to do so(and doesn't throw an Exception doing so) out of boundary
		assertFalse(game.place(16, 5));
		//Test the board is reset after resizing
		game.setSize(17, 9);
		assertEquals(Shape.N, game.getArray()[5][5]);
		//Test if the board has actually been resized
		assertTrue(game.place(16, 5));
	}
}