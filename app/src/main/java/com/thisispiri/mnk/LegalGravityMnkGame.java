package com.thisispiri.mnk;

/**A {@link GravityMnkGame} in which you can only play valid moves.*/
public class LegalGravityMnkGame extends GravityMnkGame {
	public LegalGravityMnkGame() {super();}
	public LegalGravityMnkGame(int horSize, int verSize, int winStreak) {super(horSize, verSize, winStreak);}
	public LegalGravityMnkGame(final MnkGame original) {super(original);}
	/**Places a {@link Shape} on the position if and only if the tile is empty.
	 * Changes the result of {@link MnkGame#getNextIndex()}.
	 * @return if it succeeded in placing a stone.*/
	@Override public boolean place(final int x, int y) {
		//Never call super.place(int, int, Shape) here; it calls place(int, int, Shape, boolean), which is overridden here to call this method(place(int, int)) indirectly, recursing infinitely.
		y = getFallenY(x, y);
		if(isEmpty(x, y) && super.place(x, y, shapes[getNextIndex()], true)) {
			changeShape(1);
			return true;
		}
		return false;
	}
	/**Places a {@link Shape} on the position if and only if the tile is empty and {@code toPlace} is the {@link Shape} planned to be placed next(i.e. {@link MnkGame#shapes}[{@link MnkGame#getNextIndex()}]).
	 * Changes the result of {@link MnkGame#getNextIndex()}.
	 * @return if it succeeded in placing the {@link Shape}.*/
	@Override public boolean place(final int x, final int y, final Shape toPlace) {
		return toPlace == shapes[getNextIndex()] && place(x, y);
	}
	//TODO: Allow toggling gravity in LegalGravityMnkGame to avoid switching between LegalMnkGame and LegalGravityMnkGame?
	/**Places if and only if the tile is empty, {@code toPlace} is the next {@link Shape} and {@code gravity == true}.*/
	@Override public boolean place(final int x, final int y, final Shape toPlace, final boolean gravity) {
		return gravity && place(x, y, toPlace);
	}
}
