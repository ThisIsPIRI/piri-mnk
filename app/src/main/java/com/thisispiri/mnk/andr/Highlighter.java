package com.thisispiri.mnk.andr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import com.thisispiri.common.Point;

/**Highlights rectangular cells within a grid by overlaying a color over them. It is assumed that the {@code Highlighter} itself is a square.*/
public class Highlighter extends View {
	private float horUnit, verUnit;
	private int depth = 0, duration, howManyTimes;
	private boolean restart = false;
	private Point[] toHighlight;
	/**{@code Paint} object used for highlighting.*/
	private final Paint paint = new Paint();
	/**The only constructor.*/
	public Highlighter(final Context context, final AttributeSet attrs) { super(context, attrs); }
	/**Works recursively to make an overlay flick over cells for some time, thus highlighting them.*/
	@Override public void onDraw(final Canvas canvas) {
		if(toHighlight == null) return;
		if(restart) {
			restart = false;
			depth = 0;
			highlight(toHighlight);
			return;
		}
		if(depth >= howManyTimes * 2) {
			depth = 0;
			toHighlight = null; //Some Android versions redraw other Views when one is invalidated. Disable highlighting after one is finished to solve the problem.
			return;
		}
		if(depth % 2 == 0) { //Show the overlay. It will disappear after the next onDraw() call.
			for(Point p : toHighlight) {
				canvas.drawRect(p.x * horUnit, p.y * verUnit, (p.x + 1) * horUnit, (p.y + 1) * verUnit, paint);
			}
		}
		depth++;
		postInvalidateDelayed(duration);
	}
	/**Highlights the cell specified by the coordinate.*/
	void highlight(final int x, final int y) {
		highlight(new Point[]{new Point(x, y)});
	}
	/**Highlights the cells specified by the {@code Point}s in the array.
	 * @param toHighlight The array of coordinates of the cells to highlight.*/
	void highlight(final Point[] toHighlight) {
		this.toHighlight = toHighlight;
		if(depth == 0) {
			if(Looper.myLooper() != Looper.getMainLooper())
				postInvalidate();
			else
				invalidate();
		}
		else restart = true;
	}
	void updateValues(final int horSize, final int verSize, final int sideLength, final int color, final int duration, final int howManyTimes) {
		updateValues(horSize, verSize, sideLength);
		paint.setColor(color);
		this.duration = duration;
		this.howManyTimes = howManyTimes;
	}
	void updateValues(final int horSize, final int verSize, final int sideLength) {
		horUnit = (float) sideLength / horSize;
		verUnit = (float) sideLength / verSize;
	}
}
