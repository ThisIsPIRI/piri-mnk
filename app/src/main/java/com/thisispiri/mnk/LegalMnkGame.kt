package com.thisispiri.mnk

/**An [MnkGame] decorator through which you can only play valid moves.
 * This should be the innermost decorator.*/
open class LegalMnkGame(private val game: MnkGame): MnkGame by game {
	/**Places a [Shape] on the position if and only if the tile is empty.
	 * @return Whether it succeeded in placing a stone.*/
	override fun place(x: Int, y: Int): Boolean {
		return isEmpty(x, y) && game.place(x, y)
	}
	/**This method is disabled in `LegalMnkGame`. Use place(Int, Int) instead.
	 * @return false.*/
	override fun place(x: Int, y: Int, toPlace: Shape): Boolean {
		return false
	}
}
