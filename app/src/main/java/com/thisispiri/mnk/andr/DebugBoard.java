package com.thisispiri.mnk.andr;

import android.graphics.Canvas;
import android.graphics.Paint;

public class DebugBoard extends Board { //TODO support stone numbering
	private final Paint textPaint = new Paint();
	private String[][] aiInternals;
	public DebugBoard(android.content.Context context, android.util.AttributeSet attr)  {
		super(context, attr);
		textPaint.setTextSize(getResources().getDisplayMetrics().scaledDensity * 9);
	}
	@Override protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(aiInternals == null || aiInternals.length != verSize || aiInternals[0].length != horSize) return;
		for (int i = 1; i <= verSize; i++) {
			for (int j = 0; j < horSize; j++) {
				canvas.drawText(aiInternals[i - 1][j], j * horUnit, i * verUnit, textPaint);
			}
		}
	}
	public void setAiInternals(String[][] internals) {
		this.aiInternals = internals;
	}
}
