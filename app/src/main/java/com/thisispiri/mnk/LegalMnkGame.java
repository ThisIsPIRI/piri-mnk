package com.thisispiri.mnk;

/**An {@link MnkGame} in which you can only play valid moves.*/
public class LegalMnkGame extends MnkGame {
	public LegalMnkGame() {super();}
	public LegalMnkGame(int horSize, int verSize, int winStreak) {super(horSize, verSize, winStreak);}
	public LegalMnkGame(final MnkGame original) {super(original);}
	/**Places a {@link Shape} on the position if and only if the tile is empty.
	 * Changes the result of {@link MnkGame#getNextIndex()}.
	 * @return if it succeeded in placing a stone.*/
	@Override public boolean place(final int x, final int y) {
		//Never call super.place(int, int) here; it calls place(int, int, Shape), which is overridden here to call this method(place(int, int)), recursing infinitely.
		if(isEmpty(x, y) && super.place(x, y, shapes[getNextIndex()])) {
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
}
