package com.thisispiri.mnk

import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer

class PiriPolicyAi(modelBuffer: MappedByteBuffer): MnkAi {
	val interpreter = Interpreter(modelBuffer)
	override fun playTurn(game: MnkGame): Point {
		return playTurnJustify(game).coord
	}
	override fun playTurnJustify(game: MnkGame): MnkAiDecision {
		val input = arrayOf(FloatArray(game.horSize * game.verSize * 2))
		for(i in 0 until game.verSize) {
			for(j in 0 until game.horSize) {
				if(game.array[i][j] == Shape.X)
					input[0][i * game.horSize + j] = 1f
				else if(game.array[i][j] == Shape.O)
					input[0][i * game.horSize + j + 9] = 1f
			}
		}
		val output = arrayOf(FloatArray(game.horSize * game.verSize))
		interpreter.run(input, output)
		for(i in 0 until output[0].size) {
			val p = toGameIndex(game, i)
			if(game.array[p.y][p.x] != game.empty)
				output[0][i] = 0f
		}
		val sum = output[0].sum()
		for(i in 0 until output[0].size) {
			output[0][i] /= sum
		}
		val maxIdx: Int = output[0].indices.maxBy {
			val p = toGameIndex(game, it)
			if(game.array[p.y][p.x] == game.empty) output[0][it] else -1f
			} ?: -1
		val justification = Array(game.verSize) {Array(game.horSize) {""}}
		for(i in 0 until game.verSize) {
			for(j in 0 until game.horSize) {
				justification[i][j] = "%.2f".format(output[0][i * game.horSize + j])
			}
		}
		return MnkAiDecision(toGameIndex(game, maxIdx), justification)
	}
	private fun toGameIndex(game: MnkGame, idx: Int): Point {
		return Point(idx % game.horSize, idx / game.verSize)
	}
}