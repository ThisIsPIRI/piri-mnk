package com.thisispiri.mnk.andr;

import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.ColorInt;

import com.thisispiri.mnk.MnkGame;
import com.thisispiri.mnk.Move;

import static android.graphics.Color.alpha;
import static android.graphics.Color.argb;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

/**Board, but with support for printing the order of moves and values of each cell.*/
public class DebugBoard extends Board {
	private final Paint aiPaint = new Paint();
	private final Paint[] orderPaints = {new Paint(), new Paint()};
	private String[][] aiInternals;
	/**Set to true to enable printing the turn in which each stone was placed.
	 * By default, the color of the text is complementary to the stone's color.
	 * This shouldn't be used with {@link Symbol#XS_AND_OS}.*/
	public boolean showOrder = false;
	/**Only here for calling Stack.toArray(T[]).*/
	private final static Move[] dummyArray = new Move[0];
	public DebugBoard(final android.content.Context context, final android.util.AttributeSet attr)  {
		super(context, attr);
		aiPaint.setTextSize(getResources().getDisplayMetrics().scaledDensity * 9);
		for(Paint p : orderPaints) {
			p.setTextAlign(Paint.Align.CENTER);
		}
	}
	@Override protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		if(showOrder) {
			Move[] moves = game.history.toArray(dummyArray);
			for(int i = 0;i < moves.length;i++) {
				canvas.drawText(Integer.toString(i + 1),moves[i].coord.x * horUnit + horUnit / 2,
						moves[i].coord.y * verUnit + verUnit / 1.5f, orderPaints[moves[i].placed.value]);
			}
		}
		if(aiInternals == null || aiInternals.length != verSize || aiInternals[0].length != horSize) return;
		for (int i = 1; i <= verSize; i++) {
			for (int j = 0; j < horSize; j++) {
				canvas.drawText(aiInternals[i - 1][j], j * horUnit, i * verUnit, aiPaint);
			}
		}
	}
	/**Manually change the turn numbers' colors. This will persist until the next {@link DebugBoard#updateValues}.
	 * @param colors ARGB ColorInt vararg. First one for the first player and second one for the second. Do not pass more than two.*/
	public void setOrderColors(final int... colors) {
		for(int i = 0; i < orderPaints.length; i++) {
			orderPaints[i].setColor(colors[i]);
		}
	}
	/**The Strings will be drawn on lower left corner of their respective cells if {@code internals}' size matches that of the game's array.
	 * Pass null to disable it.*/
	public void setAiInternals(final String[][] internals) {
		this.aiInternals = internals;
	}
	@Override public void updateValues(final int bgColor, final int lineColor, final int[] playerColors, final Symbol[] symbols, final Line line) {
		super.updateValues(bgColor, lineColor, playerColors, symbols, line);
		for(int i = 0;i < playerPaints.length;i++) {
			orderPaints[i].setColor(invertColor(playerPaints[i].getColor()));
		}
	}
	@Override public void setGame(final MnkGame game) {
		super.setGame(game);
		for(Paint p : orderPaints) {
			p.setTextSize(sideLength / Math.max(horSize, verSize) / 2);
		}
	}
	@Override public void setSideLength(final int length) {
		super.setSideLength(length);
		for(Paint p : orderPaints) {
			p.setTextSize(sideLength / Math.max(horSize, verSize) / 2);
		}
	}
	private int invertColor(final @ColorInt int color) {
		return argb(alpha(color), 0xFF - red(color), 0xFF - green(color), 0xFF - blue(color));
	}
}
