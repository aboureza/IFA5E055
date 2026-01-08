package liquidwars.ui;

import liquidwars.model.Particle;
import liquidwars.model.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {

    @Test
    void initialTargetsAreSetFromWidthHeight() {
        int w = 8, h = 6;
        boolean[][] walls = new boolean[h][w];
        Particle[][] parts = new Particle[h][w];
        World world = new World(walls, parts);

        GameController c = new GameController(world, walls, w, h);

        assertEquals(w / 4, c.getTargetX(0));
        assertEquals(h / 2, c.getTargetY(0));
        assertEquals(3 * w / 4, c.getTargetX(1));
        assertEquals(h / 2, c.getTargetY(1));
    }

    @Test
    void setTargetClampsAndIgnoresWalls() {
        int w = 4, h = 3;
        boolean[][] walls = new boolean[h][w];
        Particle[][] parts = new Particle[h][w];
        World world = new World(walls, parts);

        GameController c = new GameController(world, walls, w, h);

        // clamp
        c.setTarget(0, -999, 999);
        assertEquals(0, c.getTargetX(0));
        assertEquals(h - 1, c.getTargetY(0));

        // ignore walls
        walls[1][1] = true;
        int beforeX = c.getTargetX(0);
        int beforeY = c.getTargetY(0);

        c.setTarget(0, 1, 1); // on a wall
        assertEquals(beforeX, c.getTargetX(0));
        assertEquals(beforeY, c.getTargetY(0));
    }

    @Test
    void tickMovesTeam0ParticleTowardItsTargetInSimpleCase() {
        // 1 row, 3 cols, particle at (0,0), target at (2,0) => should move to (1,0)
        int w = 3, h = 1;
        boolean[][] walls = new boolean[h][w];
        Particle[][] parts = new Particle[h][w];

        parts[0][0] = new Particle(0, 5);

        World world = new World(walls, parts);
        GameController c = new GameController(world, walls, w, h);

        // set team 0 target at far right
        c.setTarget(0, 2, 0);

        c.tick();

        World w2 = c.getWorld();
        assertNull(w2.get(0, 0));
        assertNotNull(w2.get(1, 0));
        assertEquals(0, w2.get(1, 0).teamId());
        assertEquals(5, w2.get(1, 0).energy());
    }
}
