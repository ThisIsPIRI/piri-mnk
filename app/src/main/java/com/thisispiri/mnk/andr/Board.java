package com.thisispiri.mnk.andr;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import com.thisispiri.mnk.MnkGame;
import com.thisispiri.mnk.Shape;

/**Draws a board of {@link Shape}s on its area.
 * While the board may be rectangular in cells, the {@code View} itself must be square in pixels.*/
public class Board extends View {
	public enum Symbol {
		XS_AND_OS, GO_STONES, RECTANGLES, DIAMONDS;
		public final static Symbol[] VALUES = Symbol.values();
	}
	public enum Line {
		LINES_ENCLOSING_SYMBOLS, LINES_UNDER_SYMBOLS, DIAGONAL_ENCLOSING_SYMBOLS;
		public final static Line[] VALUES = Line.values();
	}
	protected final Paint background, line, oPaint, xPaint;
	protected final Path xPath = new Path(), oPath = new Path();
	protected int horUnit, verUnit, horSize, verSize;
	protected int sideLength;
	protected Symbol symbolType;
	protected Line lineType;
	protected MnkGame game;
	private final RectF ovalData = new RectF();
	public Board(final android.content.Context context, final android.util.AttributeSet attr)  {
		super(context, attr);
		background = new Paint(); line = new Paint(); oPaint = new Paint(); xPaint = new Paint();
	}
	@Override protected void onDraw(final Canvas canvas) {
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
		oPaint.setStrokeWidth(5);
		xPaint.setStrokeWidth(5);
		if(symbolType == Symbol.XS_AND_OS) {
			oPaint.setStyle(Paint.Style.STROKE);
			xPaint.setStyle(Paint.Style.STROKE);
		}
		else if(symbolType == Symbol.GO_STONES || symbolType == Symbol.RECTANGLES || symbolType == Symbol.DIAMONDS) {
			oPaint.setStyle(Paint.Style.FILL);
			xPaint.setStyle(Paint.Style.FILL);
		}
		xPath.reset();
		oPath.reset();
		//loop through the array
		for (int i = 0; i < verSize; i++) {
			for (int j = 0; j < horSize; j++) {
				if(game.array[i][j] == Shape.O) { //draw O
					if(symbolType == Symbol.RECTANGLES) {
						canvas.drawRect(j * horUnit, i * verUnit, (j + 1) * horUnit, (i + 1) * verUnit, oPaint);
					}
					else if(symbolType == Symbol.DIAMONDS) {
						oPath.moveTo((j + 0.5f) * horUnit, i * verUnit);
						oPath.lineTo((j + 1) * horUnit, (i + 0.5f) * verUnit);
						oPath.lineTo((j + 0.5f) * horUnit, (i + 1) * verUnit);
						oPath.lineTo(j * horUnit, (i + 0.5f) * verUnit);
						oPath.lineTo((j + 0.5f) * horUnit, i * verUnit);
						oPath.close();
					}
					else {
						ovalData.set(j * horUnit, i * verUnit, (j + 1) * horUnit, (i + 1) * verUnit);
						canvas.drawOval(ovalData, oPaint);
					}
				}
				else if(game.array[i][j] == Shape.X) { //draw X
					if(symbolType == Symbol.XS_AND_OS) {
						canvas.drawLine(j * horUnit, i * verUnit, (j + 1) * horUnit, (i + 1) * verUnit, xPaint);
						canvas.drawLine((j + 1) * horUnit, i * verUnit, j * horUnit, (i + 1) * verUnit, xPaint);
					}
					else if(symbolType == Symbol.GO_STONES) {
						ovalData.set(j * horUnit, i * verUnit, (j + 1) * horUnit, (i + 1) * verUnit);
						canvas.drawOval(ovalData, xPaint);
					}
					else if(symbolType == Symbol.RECTANGLES) {
						canvas.drawRect(j * horUnit, i * verUnit, (j + 1) * horUnit, (i + 1) * verUnit, xPaint);
					}
					else if(symbolType == Symbol.DIAMONDS) {
						xPath.moveTo((j + 0.5f) * horUnit, i * verUnit);
						xPath.lineTo((j + 1) * horUnit, (i + 0.5f) * verUnit);
						xPath.lineTo((j + 0.5f) * horUnit, (i + 1) * verUnit);
						xPath.lineTo(j * horUnit, (i + 0.5f) * verUnit);
						xPath.lineTo((j + 0.5f) * horUnit, i * verUnit);
						xPath.close();
					}
				}
			}
		}
		if(symbolType == Symbol.DIAMONDS) {
			canvas.drawPath(xPath, xPaint);
			canvas.drawPath(oPath, oPaint);
		}
	}
	public void updateValues(final int bgColor, final int lineColor, final int oColor, final int xColor, final Symbol ox, final Line line) {
		background.setColor(bgColor);
		this.line.setColor(lineColor);
		//this.line.setStrokeWidth(lineWidth);
		oPaint.setColor(oColor);
		xPaint.setColor(xColor);
		symbolType = ox;
		lineType = line;
	}
	/**Assigns an {@link MnkGame} to be used in the object. Call this every time the board size is changed since it caches the values.*/
	public void setGame(final MnkGame g) {
		game = g;
		horSize = game.getHorSize();
		verSize = game.getVerSize();
		horUnit = sideLength / horSize;
		verUnit = sideLength / verSize;
	}
	public void setSideLength(final int length) {
		sideLength = length;
		horUnit = length / horSize;
		verUnit = length / verSize;
	}
}
