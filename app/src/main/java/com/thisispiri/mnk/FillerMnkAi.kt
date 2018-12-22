package com.thisispiri.mnk

import android.graphics.Point

class FillerMnkAi: MnkAi {
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