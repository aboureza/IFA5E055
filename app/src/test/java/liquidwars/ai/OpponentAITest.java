package liquidwars.ai;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

public class OpponentAITest {

    @Test
    void mirroredPositionIsReturnedWhenFree() {
        boolean[][] walls = new boolean[3][5]; // all false
        OpponentAI ai = new OpponentAI(walls, 5000, new Random(1), () -> 0L);

        // player at x=1,y=2 -> mirrored x = 5-1-1=3
        OpponentAI.Target t = ai.nextTarget(1, 2);
        assertEquals(3, t.x());
        assertEquals(2, t.y());
    }

    @Test
    void randomizesAfterIntervalAndChoosesNonWall() {
        boolean[][] walls = new boolean[4][4];
        // make some walls but leave some space
        walls[0][0] = true; walls[1][1] = true;

        AtomicLong clock = new AtomicLong(0);
        // Use a short duration for tests to make assertions easier
        OpponentAI ai = new OpponentAI(walls, 5000, 2000, new Random(42), clock::get);

        // Initially not time yet -> mirrored
        OpponentAI.Target t1 = ai.nextTarget(1, 1);
        assertEquals(4 - 1 - 1, t1.x());

        // advance time by interval
        clock.addAndGet(5000);
        OpponentAI.Target t2 = ai.nextTarget(1, 1);

        // random target must not be a wall
        assertFalse(walls[t2.y()][t2.x()]);

        // Immediately after random pick (within duration), should keep random target
        OpponentAI.Target t3 = ai.nextTarget(1, 1);
        assertEquals(t2.x(), t3.x());
        assertEquals(t2.y(), t3.y());

        // After duration expiry it should revert to mirrored
        clock.addAndGet(2001);
        OpponentAI.Target t4 = ai.nextTarget(1, 1);
        assertEquals(4 - 1 - 1, t4.x());
    }

    @Test
    void returnsNearestFreeWhenMirroredIsBlocked() {
        // 3x3 map, mirrored cell (1,1) blocked, but neighbors free
        boolean[][] walls = new boolean[3][3];
        // block center
        walls[1][1] = true;

        OpponentAI ai = new OpponentAI(walls, 5000, new Random(1), () -> 0L);

        // player at x=1 -> mirrored x=1 (center) is a wall, expect nearest free like (2,1) or (0,1) or (1,0) etc
        OpponentAI.Target t = ai.nextTarget(1, 1);
        assertFalse(walls[t.y()][t.x()]);
        // Distance should be 1 from center
        int dx = Math.abs(t.x() - 1);
        int dy = Math.abs(t.y() - 1);
        assertTrue(dx + dy == 1);
    }

    @Test
    void doesNotRandomizeBeforeInterval() {
        boolean[][] walls = new boolean[2][2];
        AtomicLong clock = new AtomicLong(0);
        OpponentAI ai = new OpponentAI(walls, 5000, new Random(1), clock::get);

        OpponentAI.Target t1 = ai.nextTarget(0, 0);     //not used, why?
        // simulate time progressing less than interval
        clock.addAndGet(4999);
        OpponentAI.Target t2 = ai.nextTarget(0, 0);

        // should still be mirrored (no randomization yet)
        assertEquals(2 - 1 - 0, t2.x());
        assertEquals(0, t2.y());
    }
}
