package com.thisispiri.mnk;

import android.graphics.Point;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class PiriMnkAiTest {
	private PiriMnkAi ai;
	@Test public void test1() throws Exception {
		ai = new PiriMnkAi();
		MnkGame game = new MnkGame();
		game.setSize(11, 11);
		game.winStreak = 5;
		//Test if it values cells with more open spaces more highly
		Point played = ai.playTurn(game);
		assertEquals(played.x, 5);
		assertEquals(played.y, 5);
		//Test if it values vertical lines
		for(int i = 2;i <= 5;i++) {
			game.place(9, i);
			game.changeShape(i % 2 == 0 ? -1 : 1); //With shapes.length == 2, -1 and 1 passed to changeShape() yields same results
		}
		played = ai.playTurn(game);
		assertEquals(played.x, 9);
		assertTrue(played.y == 6 || played.y == 1);
		//Test if it values diagonal connections and if it treats lines blocked all the same regardless of the cause(boundary or a Shape)
		game.initialize();
		for(int i = 2;i <= 5;i++) {
			game.place(9, i);
		}
		played = ai.playTurn(game);
		assertEquals(played.x, 8);
		assertTrue(played.y == 3 || played.y == 5);
	}
}
