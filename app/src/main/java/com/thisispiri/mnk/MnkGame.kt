package com.thisispiri.mnk

import com.thisispiri.common.Point
import java.util.Stack

/**A representation of an MNK game.*/
interface MnkGame {
	/**The number of shapes in a line it takes to win.*/
	var winStreak: Int
	/**The state of the board, on which lie the [Shape]s of the game. */
	val array: Array<Array<Shape>>
	/**The Array of Shapes to be placed rotationally in a normal game. */
	val shapes: Array<Shape>
	/**The list of all [Move]s made in the game. Earlier Moves are stored first.*/
	val history: Stack<Move>
	/**The index of the [Shape] to be placed next in [MnkGame.shapes].*/
	val nextIndex: Int
	val verSize: Int
	val horSize: Int

	/**Places a [Shape] at the supplied coordinate. Does not check if the move is illegal(the Shape may be placed on top of another, already placed, one).
	 * Changes [MnkGame.nextIndex].
	 * @return `true` if a [Shape] was successfully placed. `false` if it failed to place it(for example, because the coordinate was out of boundaries).*/
	fun place(x: Int, y: Int): Boolean
	/**Places the shape at the location.
	 * Does not change [MnkGame.nextIndex].
	 * @param toPlace The shape to place at the location.
	 * @return `true` if a [Shape] was successfully placed. `false` if it failed to place it.*/
	fun place(x: Int, y: Int, toPlace: Shape): Boolean

	/**Restores the game to the initial state before any move was made.*/
	fun initialize()

	/**Reverts the last [Move], restoring the state of the game before the Move was made including [MnkGame.nextIndex].
	 * @return Whether a Move was actually removed. False if the board was empty.*/
	fun revertLast(): Boolean

	/**Changes [MnkGame.nextIndex] to ((current turn) + steps))th turn's, assuming the game went "normally" and no player gave up any turns.
	 * To put simply, nextIndex = (nextIndex + steps) % [MnkGame.shapes].length
	 * @param steps The number to add to the turn.*/
	fun changeShape(steps: Int)

	/**Returns [MnkGame.nextIndex] at ((current turn) + steps)th turn, without actually changing [MnkGame.nextIndex]}.
	 * @param steps The number to add to the turn.
	 * @return The next shape at the turn.*/
	fun getNextIndexAt(steps: Int): Int

	/**Sets the size of the board.
	 * @param hor The horizontal length of the board in the number of tiles.
	 * @param ver The vertical length of the board in the number of tiles.
	 * @return Whether the game was initialized(because of a change in the board size) or not.*/
	fun setSize(hor: Int, ver: Int): Boolean

	/**Checks if someone won, counting lines with more than [MnkGame.winStreak] lines as winning lines also.*/
	fun checkWin(x: Int, y: Int): Array<Point>?
	/**Checks if someone won by placing a [Shape] on the supplied coordinate.
	 * To know who(what [Shape]) won, use [MnkGame.shapes] and [MnkGame.nextIndex].
	 * Note that **only lines that contain the supplied cell will be checked.**
	 * @param exact Whether to match only lines with exactly [MnkGame.winStreak] [Shape]s.
	 * @return An array of `Point`s responsible for the win. `null` if no one won.*/
	fun checkWin(x: Int, y: Int, exact: Boolean): Array<Point>?

	/**Returns `true` if (x, y) is within the boundaries and is empty.*/
	fun isEmpty(p: Point): Boolean
	fun isEmpty(x: Int, y: Int): Boolean
	fun isEmpty(s: Shape): Boolean
	fun inBoundary(p: Point): Boolean
	fun inBoundary(y: Int, x: Int): Boolean
}
