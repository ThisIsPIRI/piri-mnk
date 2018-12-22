package com.thisispiri.mnk.andr;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import com.thisispiri.mnk.MnkGame;
import com.thisispiri.mnk.Shape;

/**Draws a rectangular board of {@link Shape}s on its area.*/
public class Board extends View {
	public enum Symbol {
		XS_AND_OS, GO_STONES
	}
	public enum Line {
		LINES_ENCLOSING_SYMBOLS, LINES_UNDER_SYMBOLS, DIAGONAL_ENCLOSING_SYMBOLS
	}
	private final Paint background, line, o, x;
	protected int horUnit, verUnit, horSize, verSize;
	private int sideLength;
	protected Symbol symbolType;
	protected Line lineType;
	protected MnkGame game;
	private final RectF ovalData = new RectF();
	public Board(android.content.Context context, android.util.AttributeSet attr)  {
		super(context, attr);
		background = new Paint(); line = new Paint(); o = new Paint(); x = new Paint();
	}
	@Override protected void onDraw(Canvas canvas) {
		//draw background
		canvas.drawRect(0, 0, sideLength, sideLength, background);
		//draw lines
		final int halfHor = horUnit / 2, halfVer = verUnit / 2; //manual common subexpression elimination
		if(lineType == Line.LINES_ENCLOSING_SYMBOLS) {
			for (int i = 1; i < horSize; i++) canvas.drawLine(horUnit * i, 0, horUnit * i, sideLength, line); //vertical lines
			for (int i = 1; i < verSize; i++) canvas.drawLine(0, verUnit * i, sideLength, verUnit * i, line); //horizontal lines
		}
		else if(lineType == Line.LINES_UNDER_SYMBOLS) {
			for (int i = 0; i < horSize; i++) canvas.drawLine(horUnit * i + halfHor, 0, horUnit * i + halfHor , sideLength, line); //vertical lines
			for (int i = 0; i < verSize; i++) canvas.drawLine(0, verUnit * i + halfVer, sideLength, verUnit * i + halfVer, line); //horizontal lines
		}
		else if(lineType == Line.DIAGONAL_ENCLOSING_SYMBOLS) {
			for(int i = 0;i < horSize + verSize;i++) {
				float startX1, startY1, endX1, endY1; // / shape
				float startX2, startY2, endX2, endY2; // \ shape
				if(i < verSize) {
					startX1 = 0;
					startY1 = verUnit * (i + 1) - halfVer;
					startX2 = 0;
					startY2 = verUnit * (verSize - i) - halfVer;
				}
				else {
					startX1 = horUnit * (i + 1 - verSize) - halfHor;
					startY1 = sideLength;
					startX2 = horUnit * (i + 1 - verSize) - halfHor;
					startY2 = 0;
				}
				if(i < horSize) {
					endX1 = horUnit * (i + 1) - halfHor;
					endY1 = 0;
					endX2 = horUnit * (i + 1) - halfHor;
					endY2 = sideLength;
				}
				else {
					endX1 = sideLength;
					endY1 = verUnit * (i + 1 - horSize) - halfVer;
					endX2 = sideLength;
					endY2 = verUnit * (verSize - (i - horSize)) - halfVer;
				}
				canvas.drawLine(startX1, startY1, endX1, endY1, line);// / shape
				canvas.drawLine(startX2, startY2, endX2, endY2, line);// \ shape
				//line.setColor(line.getColor() + 6000); //interesting, especially with thicker lines
			}
		}
		//draw Os and Xs
		o.setStrokeWidth(5);
		x.setStrokeWidth(5);
		if(symbolType == Symbol.XS_AND_OS) {
			o.setStyle(Paint.Style.STROKE);
			x.setStyle(Paint.Style.STROKE);
		}
		else if(symbolType == Symbol.GO_STONES) {
			o.setStyle(Paint.Style.FILL);
			x.setStyle(Paint.Style.FILL);
		}
		//loop through the array
		for (int i = 0; i < verSize; i++) {
			for (int j = 0; j < horSize; j++) {
				if(game.array[i][j] == Shape.O) { //draw O
					ovalData.set(j * horUnit, i * verUnit, (j + 1) * horUnit, (i + 1) * verUnit);
					canvas.drawOval(ovalData, o);
				}
				else if(game.array[i][j] == Shape.X) { //draw X
					if(symbolType == Symbol.XS_AND_OS) {
						canvas.drawLine(j * horUnit, i * verUnit, (j + 1) * horUnit, (i + 1) * verUnit, x);
						canvas.drawLine((j + 1) * horUnit, i * verUnit, j * horUnit, (i + 1) * verUnit, x);
					}
					else {
						ovalData.set(j * horUnit, i * verUnit, (j + 1) * horUnit, (i + 1) * verUnit);
						canvas.drawOval(ovalData, x);
					}
				}
			}
		}
	}
	public void updateValues(int bgColor, int lineColor, int oColor, int xColor, Symbol ox, Line line) {
		background.setColor(bgColor);
		this.line.setColor(lineColor);
		//this.line.setStrokeWidth(lineWidth);
		o.setColor(oColor);
		x.setColor(xColor);
		symbolType = ox;
		lineType = line;
	}
	/**Assigns an {@link MnkGame} to be used in the object. Call this every time {@link MnkGame#horSize} or {@link MnkGame#verSize} is changed since it caches the values.*/
	public void setGame(MnkGame g) {
		game = g;
		horSize = game.getHorSize();
		verSize = game.getVerSize();
		horUnit = sideLength / horSize;
		verUnit = sideLength / verSize;
	}
	public void setSideLength(int length) {
		sideLength = length;
		horUnit = length / horSize;
		verUnit = length / verSize;
	}
}
