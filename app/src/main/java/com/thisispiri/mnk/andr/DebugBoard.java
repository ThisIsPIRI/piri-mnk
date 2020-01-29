package com.thisispiri.mnk.andr;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.thisispiri.mnk.MnkGame;
import com.thisispiri.mnk.Move;

/**Board, but with support for printing the order of moves and values of each cell.*/
public class DebugBoard extends Board {
	private final Paint aiPaint = new Paint();
	private final Paint[] orderPaints = {new Paint(), new Paint()};
	private String[][] aiInternals;
	/**Set to true to enable printing the turn in which each stone was placed.*/
	public boolean showOrder = false;
	private Move[] dummyArray = new Move[0];
	public DebugBoard(android.content.Context context, android.util.AttributeSet attr)  {
		super(context, attr);
		aiPaint.setTextSize(getResources().getDisplayMetrics().scaledDensity * 9);
		for(Paint p : orderPaints) {
			p.setTextAlign(Paint.Align.CENTER);
		}
	}
	@Override protected void onDraw(Canvas canvas) {
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
	/**The Strings will be drawn on lower left corner of their respective cells if {@code internals}' size matches that of the game's array.
	 * Pass null to disable it.*/
	public void setAiInternals(String[][] internals) {
		this.aiInternals = internals;
	}
	/**Change the turn numbers' colors. Only 2 ints should be passed for now.*/
	public void setOrderColors(int... colors) {
		for(int i = 0; i < orderPaints.length; i++) {
			orderPaints[i].setColor(colors[i]);
		}
	}
	@Override public void setGame(MnkGame game) {
		super.setGame(game);
		for(Paint p : orderPaints) {
			p.setTextSize(sideLength / horSize / 2);
		}
	}
	@Override public void setSideLength(int length) {
		super.setSideLength(length);
		for(Paint p : orderPaints) {
			p.setTextSize(sideLength / horSize / 2);
		}
	}
}
