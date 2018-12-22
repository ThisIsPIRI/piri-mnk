package com.thisispiri.mnk;

import android.graphics.Point;

/**A representation of an abstract AI for MNK games that can return the {@code Point} on which it wants to play, given an {@link MnkGame}.*/
public interface MnkAi {
	Point playTurn(MnkGame game);
}