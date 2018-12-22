package com.thisispiri.mnk;

import android.graphics.Point;

public class MnkAiDecision {
	public final Point coord;
	public final String[][] values;
	public MnkAiDecision(Point c, String[][] v) {
		this.coord = c;
		this.values = v;
	}
}
