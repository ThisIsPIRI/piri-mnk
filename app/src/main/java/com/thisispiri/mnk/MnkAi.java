package com.thisispiri.mnk;

/**A representation of an abstract AI for MNK games that can return the {@code Point} on which it wants to play, given an {@link MnkGame}.*/
public interface MnkAi {
	/**Decides where to play.
	 * @return The {@code Point} to play at. It can be {@code null} if no valid move is found(i.e. the board is full).*/
	Point playTurn(MnkGame game);
	/**Decides where to play and also gives the perceived values for each cell.
	 * @return An {@code MnkAiDecision} object, containing the {@code Point} to play at and the values for each cell. While it won't be {@code null}, its coord can be null.*/
	MnkAiDecision playTurnJustify(MnkGame game);
}