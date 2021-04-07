package com.thisispiri.mnk

import com.thisispiri.common.Point
import org.junit.Test
import org.junit.Assert.*
import java.util.function.Consumer

class MnkGameTest {
	@Test fun test1() {
		val game: MnkGame = BaseMnkGame()
		game.setSize(11, 19)
		//Test if it returns its size correctly
		assertEquals(11, game.horSize)
		assertEquals(19, game.verSize)

		game.winStreak = 4
		assertNull(game.checkWin(5, 14))
		game.place(3, 3)
		assertEquals(Shape.X, game.array[3][3])
		game.place(3, 3)
		assertEquals(Shape.O, game.array[3][3])
		game.revertLast()
		assertEquals(Shape.X, game.array[3][3])
		game.place(3, 4, Shape.X)
		game.place(3, 5, Shape.X)
		assertNull(game.checkWin(3, 5))
		game.place(3, 6, Shape.X)
		assertNotNull(game.checkWin(3, 6))
		game.changeShape(1)
		assertEquals(Shape.X, game.shapes[game.nextIndex])
		game.place(3, 5, Shape.N)
		assertNull(game.checkWin(3, 6))
		assertFalse(game.place(5, 19))

		//Test if it initializes the game correctly
		game.initialize()
		forAllCellOn(game) { shape: Shape? -> assertEquals(Shape.N, shape) }

		//Test nextIndex
		assertEquals(0, game.nextIndex)
		game.place(1, 1)
		assertEquals(1, game.nextIndex)
		//Test if game.place(int, int, Shape) changes nextIndex
		game.place(5, 7, Shape.X)
		assertEquals(1, game.nextIndex)

		//Test getNextIndexAt
		assertEquals(0, game.getNextIndexAt(5))

		//Test changeShape
		game.changeShape(1)
		assertEquals(0, game.nextIndex)
		game.changeShape(2)
		assertEquals(0, game.nextIndex)
	}

	@Test fun testCheckWinArray() {
		//TODO: Make getLinePoints public/move it to a utility class?
		var game: MnkGame = BaseMnkGame(20, 20, 5)
		// vertical
		for(i in 0 until 20) game.place(0, i, Shape.O)
		var expected = arrayOf<Point?>(Point(0, 4), Point(0, 3), Point(0, 2), Point(0, 1), Point(0, 0))
		assertArrayEquals(expected, game.checkWin(0, 19))
		assertNull(game.checkWin(1, 19))
		game = BaseMnkGame(game)
		game.winStreak = 21
		assertNull(game.checkWin(0, 19))
		game.winStreak = 20
		expected = arrayOfNulls(20)
		for(i in 0 until 20) expected[i] = Point(0, 19 - i)
		assertArrayEquals(expected, game.checkWin(0, 0))
		// \ diagonal
		game.winStreak = 5
		expected = arrayOfNulls(5)
		for(i in 7 downTo 7 - 5 + 1) {
			expected[7 - i] = Point(i, i + 4)
			game.place(i, i + 4, Shape.X)
		}
		assertArrayEquals(expected, game.checkWin(6, 10))
		// / diagonal
		expected = arrayOfNulls(3)
		expected[0] = Point(2, 2); expected[1] = Point(1, 3); expected[2] = Point(0, 4)
		game.setSize(4, 5)
		game.winStreak = 3
		game.place(0, 4, Shape.X); game.place(1, 3, Shape.X); game.place(2, 2, Shape.X)
		assertArrayEquals(expected, game.checkWin(3, 1))
	}

	@Test fun testCheckWinExact() {
		val game: MnkGame = BaseMnkGame(5, 5, 3)
		// / diagonal
		var expected = arrayOf<Point?>(Point(4, 0), Point(3, 1), Point(2, 2))
		game.place(4, 0, Shape.X); game.place(3, 1, Shape.X); game.place(2, 2, Shape.X); game.place(1, 3, Shape.O)
		assertArrayEquals(expected, game.checkWin(4, 0, true))
		game.place(1, 3, Shape.X)
		assertNull(game.checkWin(4, 0, true))
		assertNotNull(game.checkWin(4, 0, false))
		// horizontal
		game.setSize(541, 433)
		game.winStreak = 150
		expected = arrayOfNulls(150)
		for(i in 0 until 150) {
			game.place(i + 100, 150, Shape.O)
			expected[i] = Point(249 - i, 150)
		}
		assertArrayEquals(expected, game.checkWin(6, 150, true))
		game.place(250, 150, Shape.X)
		assertNotNull(game.checkWin(6, 150, true))
		game.place(250, 150, Shape.O)
		assertNull(game.checkWin(6, 150, true))
		assertNotNull(game.checkWin(6, 150, false))
		for(i in 0 until 541) game.place(i, 150, Shape.O)
		assertNull(game.checkWin(6, 150, true))
		assertNotNull(game.checkWin(6, 150, false))
	}

	private fun forAllCellOn(game: MnkGame, action: Consumer<Shape>) {
		for(i in 0 until game.verSize) {
			for(j in 0 until game.horSize) {
				action.accept(game.array[i][j])
			}
		}
	}
}