package com.thisispiri.mnk

/**An [MnkGame] decorator that adds gravity.
 * This, or any other decorator that changes (x,y), should be the outermost decorator.*/
open class GravityMnkGame(private val game: MnkGame): MnkGame by game {
	/**Returns the largest y that keeps (x, y) within boundary and empty. */
	protected fun getFallenY(x: Int, y: Int): Int {
		var result = y
		while(isEmpty(x, result + 1)) result++
		return result
	}
	override fun place(x: Int, y: Int): Boolean {
		return game.place(x, getFallenY(x, y))
	}
	override fun place(x: Int, y: Int, toPlace: Shape): Boolean {
		return game.place(x, getFallenY(x, y), toPlace)
	}
}
