package com.thisispiri.mnk;

import android.graphics.Point;

/**A representation of an abstract AI for MNK games that can return the {@code Point} on which it wants to play, given an {@link MnkGame}.*/
public abstract class MnkAi {
	MnkGame game;
	boolean inBoundary(final int y, final int x) {return game.inBoundary(y, x);}
	abstract Point playTurn(MnkGame game);
}