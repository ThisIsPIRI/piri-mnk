package com.thisispiri.mnk;

import org.junit.Test;

import com.thisispiri.common.Point;

import static org.junit.Assert.*;

public class PiriValueAiTest {
	@Test public void test1() {
		PiriValueAi ai = new PiriValueAi();
		MnkGame game = new BaseMnkGame();
		game.setSize(11, 11);
		game.setWinStreak(5);
		//Test if it values cells with more open spaces more highly
		Point played = ai.playTurn(game);
		assertEquals(5, played.x);
		assertEquals(5, played.y);
		//Test if it values vertical lines
		for(int i = 2;i <= 5;i++) {
			game.place(9, i);
			game.changeShape(i % 2 == 0 ? -1 : 1); //With shapes.length == 2, -1 and 1 passed to changeShape() yields same results
		}
		played = ai.playTurn(game);
		assertEquals(9, played.x);
		assertTrue(played.y == 6 || played.y == 1);
		//Test if it values diagonal connections and if it treats lines blocked all the same regardless of the cause(boundary or a Shape)
		game.initialize();
		for(int i = 2;i <= 5;i++) {
			game.place(9, i);
		}
		played = ai.playTurn(game);
		assertEquals(8, played.x);
		assertTrue(played.y == 3 || played.y == 5);
	}
}
