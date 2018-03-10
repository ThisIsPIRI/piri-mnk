package com.thisispiri.mnk;
import android.graphics.Point;
/**A representation of a move that can be made in an MNK game.*/
public class Move {
	final Point coord;
	final Shape placed, prev;
	Move(Point p, Shape placed, Shape previous) {
		coord = new Point(p);
		this.placed = placed;
		prev = previous;
	}
}