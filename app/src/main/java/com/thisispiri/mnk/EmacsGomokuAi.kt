package com.thisispiri.mnk

import java.util.ArrayDeque
import java.util.Queue

open class EmacsGomokuAi: MnkAi {
	enum class Mode(val value: Int) {
		HOR(0), VER(1), SLASH(2), RESLASH(3);
		fun perpendicular(): Mode {
			return when(this) {
				HOR -> VER
				VER -> HOR
				SLASH -> RESLASH
				RESLASH -> SLASH
			}
		}
	}

	companion object {
		val ownValues = arrayOf(7, 35, 800, 15000, 800000, 4000000, 20000000, 2100000000)
		val enemValues = arrayOf(7, 15, 400, 1800, 100000, 600000, 1500000, 80000000)
		val xP = arrayOf(1, 0, -1, 1); val yP = arrayOf(0, 1, 1, 1)
	}
	protected lateinit var game: MnkGame
	protected lateinit var myShape: Shape

	override fun playTurn(game: MnkGame): Point? {
		return play(game, false).coord
	}
	override fun playTurnJustify(game: MnkGame): MnkAiDecision {
		return play(game, true)
	}
	private fun play(game: MnkGame, justify: Boolean): MnkAiDecision {
		this.game = game
		myShape = game.shapes[game.nextIndex]
		val values: Array<Array<Int>> = Array(game.verSize) {Array(game.horSize) {0}}
		checkTuples(Mode.HOR, values)
		checkTuples(Mode.VER, values)
		checkTuples(Mode.SLASH, values)
		checkTuples(Mode.RESLASH, values)
		return if(justify) MnkAiDecision(findMax(game, values), values.map {i -> i.map {j -> j.toString()}.toTypedArray()}.toTypedArray())
		else MnkAiDecision(findMax(game, values), null)
	}
	private fun checkTuples(mode: Mode, values: Array<Array<Int>>) {
		val po = Point(if(mode == Mode.RESLASH) game.horSize - 1 else 0, 0)
		while(game.inBoundary(po.y, po.x)) {
			val count: Array<Int> = arrayOf(0, 0) //count[0] == ownCount, count[1] == enemCount. TODO: use separate variables?
			val tuple: Queue<Shape> = ArrayDeque()
			val pi = Point(po)
			while(game.inBoundary(pi.y, pi.x)) {
				if(game.array[pi.y][pi.x] != game.empty)
					count[if(game.array[pi.y][pi.x] == myShape) 0 else 1]++
				tuple.add(game.array[pi.y][pi.x])
				if(bigEnough(po, pi, mode)) {
					forBackward(pi, mode, game.winStreak) { x, y ->
						if(count[1] == 0)
							values[y][x] += ownValues[count[0]]
						else if (count[0] == 0)
							values[y][x] += enemValues[count[1]]
					}
					val removed: Shape = tuple.remove()
					if(removed != game.empty)
						count[if(removed == myShape) 0 else 1]--
				}
				forward(pi, mode)
			}
			if(mode == Mode.HOR || mode == Mode.VER)
				forward(po, mode.perpendicular())
			else if(mode == Mode.SLASH && po.x < game.horSize - 1) po.x++
			else if(mode == Mode.RESLASH && po.x > 0) po.x--
			else po.y++
		}
	}
	private fun bigEnough(po: Point, pi: Point, mode: Mode): Boolean {
		return when(mode) {
			Mode.HOR -> pi.x >= game.winStreak - 1
			Mode.VER -> pi.y >= game.winStreak - 1
			else -> pi.y - po.y >= game.winStreak - 1
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
		for(i in 0 until game.verSize) {
			for(j in 0 until game.horSize) {
				if(game.array[i][j] == game.empty && values[i][j] > max) {
					max = values[i][j]
					maxI = i
					maxJ = j
				}
			}
		}
		return if(max == -1) null else Point(maxJ, maxI)
	}
}