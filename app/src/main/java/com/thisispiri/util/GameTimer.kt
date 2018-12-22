package com.thisispiri.util

import android.os.CountDownTimer

/**The class for implementing time limits in Android games.
 * @property manager The [TimedGameManager] for the timer to notify.
 * @param millisInFuture The amount of time given to the user.
 * @param countDownInterval The interval between timer updates in milliseconds. Defaults to 60.
 * @property latencyOffset The length of time, in milliseconds, after `millisInFuture` runs out in which the user(but not the other users communicating with him)
 * should be disallowed to play to ensure two or more communicating devices have the same side playing.
 * For example, if player A plays just before his time runs out, and player B's device receives it after B's timer finishes,
 * B's device could reject it or interpret is as a move made by B, creating a discrepancy between the two games.
 * With the latency offset, the manager could disallow the user using its device - and that user only - to play, while accepting moves from the others,
 * thus eliminating the problem. Note that the currently-playing side's timer will typically be ahead of the others, making the problem even likelier.
 * Set this to a negative value to disable it. Defaults to 600.*/
open class GameTimer @JvmOverloads constructor(private var manager: TimedGameManager, millisInFuture: Long, countDownInterval: Long = 60, var latencyOffset: Long = 600)
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