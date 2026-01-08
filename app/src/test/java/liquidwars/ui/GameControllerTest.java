package liquidwars.ui;

import liquidwars.model.Particle;
import liquidwars.model.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {

    @Test
    void constructorSetsDefaultTargets() {
        int w = 10, h = 8;

        boolean[][] walls = new boolean[h][w];
        Particle[][] parts = new Particle[h][w];
        World world = new World(walls, parts);

        GameController gc = new GameController(world, walls, w, h);

        assertEquals(w / 4, gc.getTargetX(0));
        assertEquals(h / 2, gc.getTargetY(0));

        assertEquals(3 * w / 4, gc.getTargetX(1));
        assertEquals(h / 2, gc.getTargetY(1));
    }

    @Test
    void setTarget_clampsToBounds() {
        int w = 6, h = 5;

        boolean[][] walls = new boolean[h][w];
        Particle[][] parts = new Particle[h][w];
        World world = new World(walls, parts);

        GameController gc = new GameController(world, walls, w, h);

        // request out of bounds -> should clamp to (0, h-1)
        gc.setTarget(0, -999, 999);

        assertEquals(0, gc.getTargetX(0));
        assertEquals(h - 1, gc.getTargetY(0));
    }

    @Test
    void setTarget_ignoresWallCells() {
        int w = 6, h = 5;

        boolean[][] walls = new boolean[h][w];
        walls[1][1] = true; // wall at (1,1)

        Particle[][] parts = new Particle[h][w];
        World world = new World(walls, parts);

        GameController gc = new GameController(world, walls, w, h);

        int beforeX = gc.getTargetX(0);
        int beforeY = gc.getTargetY(0);

        gc.setTarget(0, 1, 1); // click wall => should be ignored

        assertEquals(beforeX, gc.getTargetX(0));
        assertEquals(beforeY, gc.getTargetY(0));
    }

    @Test
    void tick_doesNotThrow_andWorldRemainsNonNull() {
        int w = 10, h = 10;

        boolean[][] walls = new boolean[h][w];
        Particle[][] parts = new Particle[h][w];
        // put a couple particles
        parts[2][2] = new Particle(0, 5);
        parts[7][7] = new Particle(1, 5);

        World world = new World(walls, parts);
        GameController gc = new GameController(world, walls, w, h);

        assertDoesNotThrow(gc::tick);
        assertNotNull(gc.getWorld());
        assertEquals(w, gc.getWorld().width());
        assertEquals(h, gc.getWorld().height());
    }
}
