package com.thisispiri.mnk;

import android.graphics.Canvas;
import android.graphics.Paint;

public class DebugBoard extends Board {
	private Paint textPaint = new Paint();
	private String[][] values;
	public DebugBoard(android.content.Context context, android.util.AttributeSet attr)  {
		super(context, attr);
		textPaint.setTextSize(9);
	}
	@Override protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(values == null || values.length != verSize || values[0].length != horSize) return;
		for (int i = 0; i < verSize; i++) {
			for (int j = 0; j < horSize; j++) {
				canvas.drawText(values[i][j], j * horUnit, i * verUnit, textPaint);
			}
		}
	}
	public void setValues(String[][] values) {
		this.values = values;
	}
}
