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
        game.setValues(11, 19, 4);
        assertFalse(game.checkWin(5, 14));
        game.place(3, 3);
        assertEquals(Shape.X, game.array[3][3]);
        game.place(3, 3);
        assertEquals(Shape.O, game.array[3][3]);
        game.revertLast();
        assertEquals(Shape.X, game.array[3][3]);
        game.place(3, 4, Shape.X);
        game.place(3, 5, Shape.X);
        assertFalse(game.checkWin(3, 5));
        game.place(3, 6, Shape.X);
        assertTrue(game.checkWin(3, 6));
        game.toNextShape();
        assertEquals(Shape.X, game.shapes[game.nextShape]);
        game.place(3, 5, Shape.N);
        assertFalse(game.checkWin(3, 6));
    }
}
