package liquidwars.ui;

import liquidwars.model.Particle;
import liquidwars.model.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiplayerGameControllerTest {

    private MultiplayerGameController controller;
    private boolean[][] walls;
    private World initialWorld;
    private static final int WIDTH = 160;
    private static final int HEIGHT = 120;

    @BeforeEach
    void setUp() {
        walls = new boolean[HEIGHT][WIDTH];
        // Create some simple walls
        for (int i = 0; i < WIDTH; i++) {
            walls[0][i] = true;
            walls[HEIGHT - 1][i] = true;
        }
        for (int i = 0; i < HEIGHT; i++) {
            walls[i][0] = true;
            walls[i][WIDTH - 1] = true;
        }

        // Create initial world with some particles
        Particle[][] particles = new Particle[HEIGHT][WIDTH];
        particles[10][10] = new Particle(0, 5);
        particles[20][20] = new Particle(1, 5);
        particles[30][30] = new Particle(2, 5);
        particles[40][40] = new Particle(3, 5);

        initialWorld = new World(walls, particles);
        controller = new MultiplayerGameController(initialWorld, walls, WIDTH, HEIGHT);
    }

    @Test
    void constructor_InitializesWorldCorrectly() {
        assertNotNull(controller.getWorld());
        assertEquals(initialWorld, controller.getWorld());
    }

    @Test
    void constructor_InitializesDefaultTargets() {
        // Default targets should be spread across the map
        assertTrue(controller.getTargetX(0) > 0);
        assertTrue(controller.getTargetY(0) > 0);
        assertTrue(controller.getTargetX(1) > 0);
        assertTrue(controller.getTargetY(1) > 0);
        assertTrue(controller.getTargetX(2) > 0);
        assertTrue(controller.getTargetY(2) > 0);
        assertTrue(controller.getTargetX(3) > 0);
        assertTrue(controller.getTargetY(3) > 0);
    }

    @Test
    void setTarget_UpdatesTargetForTeam() {
        controller.setTarget(0, 50, 50);

        assertEquals(50, controller.getTargetX(0));
        assertEquals(50, controller.getTargetY(0));
    }

    @Test
    void setTarget_IgnoresWallTargets() {
        int oldX = controller.getTargetX(0);
        int oldY = controller.getTargetY(0);

        // Try to set target on a wall (border)
        controller.setTarget(0, 0, 0);

        // Target should remain unchanged
        assertEquals(oldX, controller.getTargetX(0));
        assertEquals(oldY, controller.getTargetY(0));
    }

    @Test
    void getTargetX_ThrowsForInvalidTeamId() {
        assertThrows(IllegalArgumentException.class, () -> {
            controller.getTargetX(4);
        });
    }

    @Test
    void getTargetY_ThrowsForInvalidTeamId() {
        assertThrows(IllegalArgumentException.class, () -> {
            controller.getTargetY(4);
        });
    }

    @Test
    void setTarget_ThrowsForInvalidTeamId() {
        assertThrows(IllegalArgumentException.class, () -> {
            controller.setTarget(4, 10, 10);
        });
    }

    @Test
    void tick_UpdatesWorld() {
        World worldBefore = controller.getWorld();

        controller.tick();

        World worldAfter = controller.getWorld();

        // World should be updated (new instance)
        assertNotSame(worldBefore, worldAfter);
    }

    @Test
    void tick_HandlesAllFourTeams() {
        // Set different targets for all teams
        controller.setTarget(0, 10, 10);
        controller.setTarget(1, 20, 20);
        controller.setTarget(2, 30, 30);
        controller.setTarget(3, 40, 40);

        // Should not throw exception
        assertDoesNotThrow(() -> controller.tick());
    }

    @Test
    void setTarget_WorksForAllTeams() {
        controller.setTarget(0, 15, 15);
        controller.setTarget(1, 25, 25);
        controller.setTarget(2, 35, 35);
        controller.setTarget(3, 45, 45);

        assertEquals(15, controller.getTargetX(0));
        assertEquals(15, controller.getTargetY(0));
        assertEquals(25, controller.getTargetX(1));
        assertEquals(25, controller.getTargetY(1));
        assertEquals(35, controller.getTargetX(2));
        assertEquals(35, controller.getTargetY(2));
        assertEquals(45, controller.getTargetX(3));
        assertEquals(45, controller.getTargetY(3));
    }
}
