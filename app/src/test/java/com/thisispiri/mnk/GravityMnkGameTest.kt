package com.thisispiri.mnk

import org.junit.Test
import org.junit.Assert.*

class GravityMnkGameTest {
	@Test fun testPlaying() {
		val game = GravityMnkGame(BaseMnkGame())
		game.setSize(10, 50)
		game.place(1, 2)
		assertTrue(game.isEmpty(1, 2))
		assertEquals(Shape.X, game.array[49][1])
		game.place(2, 49)
		game.array[20][5] = Shape.X
		game.place(5, 10)
		assertEquals(Shape.X, game.array[19][5])
		game.place(5, 20)
		assertEquals(Shape.O, game.array[49][5])
	}
	@Test fun testCopiedBase() {
		val normalGame = LegalMnkGame(BaseMnkGame())
		normalGame.setSize(4, 4)
		normalGame.place(0, 0); normalGame.place(1, 0); normalGame.place(2, 0); normalGame.place(3, 0)
		normalGame.changeShape(1)
		val gravityGame = GravityMnkGame(BaseMnkGame(normalGame))
		val expected = arrayOf(Shape.X, Shape.O, Shape.X, Shape.O)
		assertArrayEquals(expected, gravityGame.array[0])
		gravityGame.place(2, 1)
		assertTrue(gravityGame.isEmpty(2, 1))
		assertEquals(Shape.O, gravityGame.array[3][2])
	}
}