package com.thisispiri.mnk;

import com.thisispiri.common.Point;

/**A representation of an abstract AI for MNK games that can return the {@code Point} on which it wants to play, given an {@link MnkGame}.*/
public interface MnkAi {
	/**Decides where to play.
	 * @return The {@link Point} to play at. It can be {@code null} if no valid move is found(i.e. the board is full).*/
	Point playTurn(MnkGame game);
	/**Decides where to play and also gives the perceived values for each cell.
	 * @return An {@link MnkAiDecision} object, containing the {@code Point} to play at and the values for each cell. While it won't be {@code null}, its coord can be null.*/
	MnkAiDecision playTurnJustify(MnkGame game);
	/**Decides where to play and also gives the perceived values for each cell if {@code justify} is true.
	 * @return An {@link MnkAiDecision} object. While it won't be {@code null}, its coord can be null and its {@code values} will be null if {@code justify} is false.*/
	MnkAiDecision playTurn(MnkGame game, boolean justify);
}
