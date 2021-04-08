package com.thisispiri.mnk;

import com.thisispiri.common.Point;

/**A representation of a move that can be made in an MNK game.*/
public class Move {
	public final Point coord;
	public final Shape placed, prev;
	/**@param p The coordinate. Will be assigned directly to {@link Move#coord}, and not copied.*/
	public Move(Point p, Shape placed, Shape previous) {
		coord = p;
		this.placed = placed;
		prev = previous;
	}
}
