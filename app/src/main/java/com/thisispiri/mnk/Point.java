package com.thisispiri.mnk;

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
}
