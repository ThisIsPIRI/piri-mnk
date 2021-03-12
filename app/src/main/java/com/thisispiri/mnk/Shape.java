package com.thisispiri.mnk;

/**A representation of shapes(stones) that can be placed on the board of MNK or similar games(go, reversi, ...)*/
public enum Shape{
	X(0), O(1), N(999);
	public final int value;
	Shape(int v) { value = v; }
}
