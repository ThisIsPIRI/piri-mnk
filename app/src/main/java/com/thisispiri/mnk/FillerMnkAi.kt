package com.thisispiri.mnk

import com.thisispiri.common.Point

open class FillerMnkAi: MnkAi {
	/**Returns a Point for the first empty cell it finds while traversing the board in row-major order.
	 * Returns `null` if no cell is empty.*/
	override fun playTurn(game: MnkGame): Point? {
		for((i, r) in game.array.withIndex()) {
			for((j, c) in r.withIndex()) {
				if(game.isEmpty(c))
					return Point(j, i)
			}
		}
		return null
	}
	override fun playTurn(game: MnkGame, justify: Boolean): MnkAiDecision {
		return if(justify) playTurnJustify(game) else MnkAiDecision(playTurn(game), null)
	}
	override fun playTurnJustify(game: MnkGame): MnkAiDecision {
		return MnkAiDecision(playTurn(game), Array(game.verSize) {Array(game.horSize) {"N/A"}})
	}
}