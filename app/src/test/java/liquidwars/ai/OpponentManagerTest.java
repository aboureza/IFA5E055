package liquidwars.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpponentManagerTest {



    @Test
    void updateOnceUsesAiToSetTarget() {
        boolean[][] walls = new boolean[3][4]; // all free
        OpponentAI ai = new OpponentAI(walls, 5000, new java.util.Random(1), () -> 0L);

        int height = walls.length;
        int width = walls[0].length;
        liquidwars.model.Particle[][] parts = new liquidwars.model.Particle[height][width];
        liquidwars.model.World w = new liquidwars.model.World(walls, parts);

        liquidwars.ui.GameController c = new liquidwars.ui.GameController(w, walls, width, height);
        // set player (team 0) target
        c.setTarget(0, 1, 2);

        OpponentManager m = new OpponentManager(c, ai);
        m.updateOnce();

        // mirrored x = width-1-playerX = 4-1-1 = 2
        assertEquals(2, c.getTargetX(1));
        assertEquals(2, c.getTargetY(1));
    }

    @Test
    void updateOnceUsesRandomWhenAiDecides() {
        // make a small map and force AI to randomize by giving clock >= interval
        boolean[][] walls = new boolean[2][2];
        java.util.concurrent.atomic.AtomicLong clock = new java.util.concurrent.atomic.AtomicLong(6000L);
        OpponentAI ai = new OpponentAI(walls, 5000, new java.util.Random(42), clock::get);

        int height = walls.length;
        int width = walls[0].length;
        liquidwars.model.Particle[][] parts = new liquidwars.model.Particle[height][width];
        liquidwars.model.World w = new liquidwars.model.World(walls, parts);

        liquidwars.ui.GameController c = new liquidwars.ui.GameController(w, walls, width, height);
        c.setTarget(0, 0, 0);

        OpponentManager m = new OpponentManager(c, ai);
        m.updateOnce();

        // should have been set to some free cell
        assertTrue(c.getTargetX(1) >= 0 && c.getTargetY(1) >= 0);
        assertFalse(walls[c.getTargetY(1)][c.getTargetX(1)]);
    }
}