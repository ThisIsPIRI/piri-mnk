package com.thisispiri.util

import android.os.CountDownTimer

/**The class for implementing time limits in Android games.
 * @property manager The [TimedGameManager] for the timer to notify.
 * @param millisInFuture The amount of time given to the user.
 * @param countDownInterval The interval between timer updates in milliseconds. Defaults to 60.
 * @property latencyOffset The length of time after `millisInFuture` runs out in which the user is disallowed to play to ensure two or more communicating devices have the same side playing, in milliseconds.
 * Set this to a negative value to disable it. Defaults to 600.*/
class GameTimer @JvmOverloads constructor(private var manager: TimedGameManager, millisInFuture: Long, countDownInterval: Long = 60, var latencyOffset: Long = 600)
		: CountDownTimer(millisInFuture + latencyOffset, countDownInterval) {
	/**Updates the manager with the remaining time. */
	override fun onTick(millisUntilFinished: Long) {
		if (millisUntilFinished < latencyOffset)
			manager.togglePlaying(false)
		else
			manager.updateRemaining(millisUntilFinished - latencyOffset)
	}
	/**Notifies the manager that the time has run out.*/
	override fun onFinish() {
		manager.timerFinished()
	}
}