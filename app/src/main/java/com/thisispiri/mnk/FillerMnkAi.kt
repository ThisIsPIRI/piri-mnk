package com.thisispiri.mnk

import android.graphics.Point

class FillerMnkAi: MnkAi {
	/**Returns a Point for the first empty cell it finds while traversing the board in row-major order.
	 * Returns `null` if no cell is empty.*/
	override fun playTurn(game: MnkGame): Point? {
		for((i, r) in game.array.withIndex()) {
			for((j, c) in r.withIndex()) {
				if(c == Shape.N)
					return Point(j, i)
			}
		}
		return null
	}
}