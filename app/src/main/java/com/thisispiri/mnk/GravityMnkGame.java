package com.thisispiri.mnk;

/**A variant of {@link MnkGame} with gravity.*/
public class GravityMnkGame extends MnkGame {
	public GravityMnkGame() {super();}
	public GravityMnkGame(int horSize, int verSize, int winStreak) {super(horSize, verSize, winStreak);}
	public GravityMnkGame(final MnkGame original){super(original);}
	/**Returns the largest y that keeps (x, y) within boundary and empty.*/
	public int getFallenY(final int x, int y) {
		for(;isEmpty(x, y + 1);y++);
		return y;
	}
	@Override public boolean place(final int x, final int y, final Shape toPlace) {
		return place(x, y, toPlace, true);
	}
	public boolean place(final int x, int y, final Shape toPlace, final boolean gravity) {
		if(gravity)
			y = getFallenY(x, y);
		return super.place(x, y, toPlace);
	}
}
