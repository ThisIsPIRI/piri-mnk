package com.thisispiri.mnk

import android.graphics.Point
import android.util.Log
import java.util.ArrayDeque
import java.util.Queue

class EmacsGomokuAi: MnkAi {
	enum class Mode(val value: Int) {
		HOR(0), VER(1), SLASH(2), RESLASH(3);
		fun opposite(): Mode {
			return when(this) {
				HOR -> VER
				VER -> HOR
				SLASH -> RESLASH
				RESLASH -> SLASH
			}
		}
	}

	companion object {
		val ownValues = arrayOf(7, 35, 800, 15000, 800000)
		val enemValues = arrayOf(7, 15, 400, 1800, 100000)
		val xP = arrayOf(1, 0, -1, 1); val yP = arrayOf(0, 1, 1, 1)
	}
	lateinit var game: MnkGame
	lateinit var myShape: Shape; lateinit var enemShape: Shape

	override fun playTurn(game: MnkGame): Point? {
		this.game = game
		myShape = game.shapes[game.nextIndex]
		enemShape = if(myShape == Shape.X) Shape.O else Shape.X
		val values: Array<Array<Int>> = Array(game.verSize) {Array(game.horSize) {0}}
		checkTuples(Mode.HOR, values)
		checkTuples(Mode.VER, values)
		checkTuples(Mode.SLASH, values)
		checkTuples(Mode.RESLASH, values)
		return findMax(game, values)
	}
	private fun checkTuples(mode: Mode, values: Array<Array<Int>>) {
		val po = Point(if(mode == Mode.RESLASH) game.horSize - 1 else 0, 0)
		while(game.inBoundary(po.y, po.x)) {
			val count: Array<Int> = arrayOf(0, 0)
			val tuple: Queue<Shape> = ArrayDeque()
			val pi = Point(po)
			while(game.inBoundary(pi.y, pi.x)) {
				if(game.array[pi.y][pi.x] != Shape.N)
					count[game.array[pi.y][pi.x].value]++
				tuple.add(game.array[pi.y][pi.x])
				if(bigEnough(po, pi, mode)) {
					forBackward(pi, mode, 5) { x, y ->
						if (count[enemShape.value] == 0)
							values[y][x] += ownValues[count[myShape.value]]
						else if (count[myShape.value] == 0)
							values[y][x] += enemValues[count[enemShape.value]]
					}
					val removed: Shape = tuple.remove()
					if(removed != Shape.N)
						count[removed.value]--
				}
				forward(pi, mode)
			}
			if(mode == Mode.HOR || mode == Mode.VER)
				forward(po, mode.opposite())
			else if(mode == Mode.SLASH && po.x < game.horSize) po.x++
			else if(mode == Mode.RESLASH && po.x > 0) po.x--
			else po.y++
		}
	}
	private fun bigEnough(po: Point, pi: Point, mode: Mode): Boolean {
		when(mode) {
			Mode.HOR -> return pi.x >= 4
			Mode.VER -> return pi.y >= 4
			else -> return pi.y - po.y >= 4
		}
	}
	private fun forward(p: Point, mode: Mode) {
		p.y += yP[mode.value]
		p.x += xP[mode.value]
	}
	private fun backward(p: Point, mode: Mode) {
		p.y -= yP[mode.value]
		p.x -= xP[mode.value]
	}
	private fun forBackward(from: Point, mode: Mode, n: Int, action: (Int, Int) -> Unit) {
		val p = Point(from)
		for(i in 1..n) {
			action(p.x, p.y)
			backward(p, mode)
		}
	}
	private fun findMax(game: MnkGame, values: Array<Array<Int>>): Point? {
		var max: Int = -1
		var maxI: Int = -1; var maxJ: Int = -1
		for(i in 0..(game.verSize - 1)) {
			for(j in 0..(game.horSize - 1)) {
				if(game.array[i][j] == Shape.N && values[i][j] > max) {
					max = values[i][j]
					maxI = i
					maxJ = j
				}
			}
		}
		for(i in 0..(game.verSize - 1))
			Log.d("EMACS", values[i].joinToString())
		Log.d("EMACS", "----------------------------")
		return if(max == -1) null else Point(maxJ, maxI)
	}
}