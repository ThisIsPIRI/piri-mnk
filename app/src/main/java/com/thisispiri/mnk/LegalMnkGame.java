package com.thisispiri.mnk;
/**An {@link MnkGame} in which you can only play valid moves.*/
public class LegalMnkGame extends MnkGame {
	LegalMnkGame() {
		super();
	}
	/**Shallow copies the supplied {@link MnkGame}.*/
	LegalMnkGame(final MnkGame original) {
		setSize(original.getHorSize(), original.getVerSize());
		winStreak = original.winStreak;
		array = original.array;
	}
	/**Places a {@link Shape} on the position if and only if the tile is empty.*/
	@Override public boolean place(int x, int y) {
		return isEmpty(x, y) && super.place(x, y);
	}
	/**Places a {@link Shape} on the position if and only if the tile is empty and {@code toPlace} is the {@link Shape} planned to be placed next.*/
	@Override public boolean place(int x, int y, Shape toPlace) {
		return toPlace == shapes[getNextIndex()] && place(x, y);
	}
}
