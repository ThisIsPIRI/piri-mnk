package com.thisispiri.mnk;

import com.thisispiri.common.Point;

/**A representation of a move that can be made in an MNK game.*/
public class Move {
	public final Point coord;
	public final Shape placed, prev;
	Move(Point p, Shape placed, Shape previous) {
		coord = new Point(p);
		this.placed = placed;
		prev = previous;
	}
}