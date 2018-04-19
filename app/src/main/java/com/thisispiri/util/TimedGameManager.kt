package com.thisispiri.util

interface TimedGameManager {
	/**Shows the remaining time to the user if he set the manager to do so. Will not be called inside the latency offset.
	 * @param time The remaining time in milliseconds.*/
	fun updateRemaining(time: Long)
	/**Notifies the manager that a timer has finished.*/
	fun timerFinished()
	/**Blocks or enables users to play.*/
	fun togglePlaying(allow: Boolean)
}
