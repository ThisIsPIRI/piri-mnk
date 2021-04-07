package com.thisispiri.mnk

import org.junit.Test
import org.junit.Assert.*

class LegalGravityMnkGameTest {
	@Test fun testPlaying() {
		val game: MnkGame = GravityMnkGame(LegalMnkGame(BaseMnkGame()))
		game.setSize(10, 50)
		game.place(1, 2)
		assertTrue(game.isEmpty(1, 2))
		assertEquals(Shape.X, game.array[49][1])
		game.place(2, 49)
		assertFalse(game.place(5, 20, Shape.X))
		game.place(5, 20)
		assertEquals(Shape.X, game.array[49][5])
		game.place(5, 10)
		assertEquals(Shape.O, game.array[48][5])
		game.place(5, 20)
		assertEquals(Shape.X, game.array[47][5])
	}
	@Test fun testRejecting() {
		val game: MnkGame = GravityMnkGame(LegalMnkGame(BaseMnkGame()))
		game.setSize(5, 5)
		assertFalse(game.place(2, 2, Shape.O))
		assertFalse(game.place(7, 7))
		assertTrue(game.place(2, 2))
		assertTrue(game.place(2, 2))
		assertEquals(Shape.X, game.array[4][2])
		assertEquals(Shape.O, game.array[3][2])
	}
}