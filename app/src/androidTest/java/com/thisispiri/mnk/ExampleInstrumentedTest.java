package com.thisispiri.mnk;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.thisispiri.mnk", appContext.getPackageName());
        MnkGame game;
        game = new MnkGame();
        game.setSize(11, 19);
        game.winStreak = 4;
        assertTrue(game.checkWin(5, 14) == null);
        game.place(3, 3);
        assertEquals(Shape.X, game.array[3][3]);
        game.place(3, 3);
        assertEquals(Shape.O, game.array[3][3]);
        game.revertLast();
        assertEquals(Shape.X, game.array[3][3]);
        game.place(3, 4, Shape.X);
        game.place(3, 5, Shape.X);
        assertTrue(game.checkWin(3, 5) == null);
        game.place(3, 6, Shape.X);
        assertFalse(game.checkWin(3, 6) == null);
        game.changeShape(1);
        assertEquals(Shape.X, game.shapes[game.getNextIndex()]);
        game.place(3, 5, Shape.N);
        assertTrue(game.checkWin(3, 6) == null);
    }
}
