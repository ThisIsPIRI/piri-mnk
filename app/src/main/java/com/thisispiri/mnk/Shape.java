package com.thisispiri.mnk;

/**A representation of shapes(stones) that can be placed on the board of MNK or similar games(go, reversi, ...)*/
public enum Shape{O(1), X(0), N(999);
	final int value;
	Shape(int v) { value = v; }
}