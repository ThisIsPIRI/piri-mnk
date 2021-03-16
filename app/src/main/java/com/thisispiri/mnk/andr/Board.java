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
		XS, OS, GO_STONES, RECTANGLES, DIAMONDS;
		public final static Symbol[] VALUES = Symbol.values();
	}
	public enum Line {
		LINES_ENCLOSING_SYMBOLS, LINES_UNDER_SYMBOLS, DIAGONAL_ENCLOSING_SYMBOLS;
		public final static Line[] VALUES = Line.values();
	}
	protected final Paint background = new Paint(), line = new Paint();
	protected final Paint[] playerPaints = {new Paint(), new Paint()};
	protected final Path[] playerPaths = {new Path(), new Path()};
	protected float horUnit, verUnit;
	protected int horSize, verSize;
	protected float sideLength;
	protected final Symbol[] symbols = new Symbol[2];
	protected Line lineType;
	protected MnkGame game;
	private final RectF ovalData = new RectF();
	public Board(final android.content.Context context, final android.util.AttributeSet attr)  {
		super(context, attr);
	}
	@Override protected void onDraw(final Canvas canvas) {
		//draw background
		canvas.drawRect(0, 0, sideLength, sideLength, background);
		//draw lines
		final float halfHor = horUnit / 2, halfVer = verUnit / 2;
		if(lineType == Line.LINES_ENCLOSING_SYMBOLS) {
			for(int i = 1; i < horSize; i++)
				canvas.drawLine(horUnit * i, 0, horUnit * i, sideLength, line); //vertical lines
			for(int i = 1; i < verSize; i++)
				canvas.drawLine(0, verUnit * i, sideLength, verUnit * i, line); //horizontal lines
		}
		else if(lineType == Line.LINES_UNDER_SYMBOLS) {
			for(int i = 0; i < horSize; i++)
				canvas.drawLine(horUnit * i + halfHor, 0, horUnit * i + halfHor , sideLength, line); //vertical lines
			for(int i = 0; i < verSize; i++)
				canvas.drawLine(0, verUnit * i + halfVer, sideLength, verUnit * i + halfVer, line); //horizontal lines
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
				canvas.drawLine(startX1, startY1, endX1, endY1, line); // / shape
				canvas.drawLine(startX2, startY2, endX2, endY2, line); // \ shape
				//line.setColor(line.getColor() + 6000); //interesting, especially with thicker lines
			}
		}
		//draw symbols
		for(int i = 0;i < playerPaints.length;i++) {
			playerPaints[i].setStrokeWidth(5);
			if(symbols[i] == Symbol.XS || symbols[i] == Symbol.OS)
				playerPaints[i].setStyle(Paint.Style.STROKE);
			else
				playerPaints[i].setStyle(Paint.Style.FILL);
			playerPaths[i].reset();
		}
		for(int i = 0; i < verSize; i++) {
			for(int j = 0; j < horSize; j++) {
				if(game.array[i][j] != game.empty) {
					final int shapeval = game.array[i][j].value;
					switch(symbols[shapeval]) {
					case XS:
						canvas.drawLine(j * horUnit, i * verUnit, (j + 1) * horUnit, (i + 1) * verUnit, playerPaints[shapeval]);
						canvas.drawLine((j + 1) * horUnit, i * verUnit, j * horUnit, (i + 1) * verUnit, playerPaints[shapeval]);
						break;
					case OS:
					case GO_STONES:
						ovalData.set(j * horUnit, i * verUnit, (j + 1) * horUnit, (i + 1) * verUnit);
						canvas.drawOval(ovalData, playerPaints[shapeval]);
						break;
					case RECTANGLES:
						canvas.drawRect(j * horUnit, i * verUnit, (j + 1) * horUnit, (i + 1) * verUnit, playerPaints[shapeval]);
						break;
					case DIAMONDS:
						playerPaths[shapeval].moveTo((j + 0.5f) * horUnit, i * verUnit);
						playerPaths[shapeval].lineTo((j + 1) * horUnit, (i + 0.5f) * verUnit);
						playerPaths[shapeval].lineTo((j + 0.5f) * horUnit, (i + 1) * verUnit);
						playerPaths[shapeval].lineTo(j * horUnit, (i + 0.5f) * verUnit);
						playerPaths[shapeval].lineTo((j + 0.5f) * horUnit, i * verUnit);
						playerPaths[shapeval].close();
						break;
					}
				}
			}
		}
		for(int i = 0;i < playerPaths.length;i++) {
			if(!playerPaths[i].isEmpty())
				canvas.drawPath(playerPaths[i], playerPaints[i]);
		}
	}
	public void updateValues(final int bgColor, final int lineColor, final int[] playerColors, final Symbol[] symbols, final Line line) {
		background.setColor(bgColor);
		this.line.setColor(lineColor);
		//this.line.setStrokeWidth(lineWidth);
		for(int i = 0;i < playerPaints.length;i++) {
			playerPaints[i].setColor(playerColors[i]);
			this.symbols[i] = symbols[i];
		}
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
		horUnit = sideLength / horSize;
		verUnit = sideLength / verSize;
	}
}
