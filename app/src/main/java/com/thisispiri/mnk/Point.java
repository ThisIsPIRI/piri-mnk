package com.thisispiri.mnk;

import java.util.Locale;

public class Point {
	public int x, y;
	public Point(Point p) {
		this.x = p.x;
		this.y = p.y;
	}
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	@Override public boolean equals(Object obj) {
		return obj instanceof Point && this.x == ((Point) obj).x && this.y == ((Point) obj).y;
	}
	@Override public String toString() {
		return String.format(Locale.ENGLISH, "Point (%d, %d)", x, y);
	}
}
