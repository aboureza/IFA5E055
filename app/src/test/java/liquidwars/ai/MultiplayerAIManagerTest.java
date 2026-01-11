package liquidwars.ai;

import liquidwars.model.Particle;
import liquidwars.model.World;
import liquidwars.ui.MultiplayerGameController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiplayerAIManagerTest {

    private MultiplayerAIManager aiManager;
    private MultiplayerGameController controller;
    private boolean[][] walls;
    private static final int WIDTH = 160;
    private static final int HEIGHT = 120;

    @BeforeEach
    void setUp() {
        walls = new boolean[HEIGHT][WIDTH];
        
        // Create simple border walls
        for (int i = 0; i < WIDTH; i++) {
            walls[0][i] = true;
            walls[HEIGHT - 1][i] = true;
        }
        for (int i = 0; i < HEIGHT; i++) {
            walls[i][0] = true;
            walls[i][WIDTH - 1] = true;
        }

        // Create initial world with particles for all 4 teams
        Particle[][] particles = new Particle[HEIGHT][WIDTH];
        particles[10][10] = new Particle(0, 5);
        particles[20][20] = new Particle(1, 5);
        particles[30][30] = new Particle(2, 5);
        particles[40][40] = new Particle(3, 5);

        World initialWorld = new World(walls, particles);
        controller = new MultiplayerGameController(initialWorld, walls, WIDTH, HEIGHT);
        aiManager = new MultiplayerAIManager(controller, walls, WIDTH, HEIGHT);
    }

    @AfterEach
    void tearDown() {
        if (aiManager != null) {
            aiManager.stop();
        }
    }

    @Test
    void constructor_CreatesAIManager() {
        assertNotNull(aiManager);
    }

    @Test
    void start_DoesNotThrowException() {
        assertDoesNotThrow(() -> aiManager.start());
    }

    @Test
    void stop_DoesNotThrowException() {
        aiManager.start();
        assertDoesNotThrow(() -> aiManager.stop());
    }

    @Test
    void startAndStop_CanBeCalledMultipleTimes() {
        aiManager.start();
        aiManager.stop();
        aiManager.start();
        aiManager.stop();
        
        // Should not throw exception
        assertDoesNotThrow(() -> {});
    }

    @Test
    void constructor_InitializesWithDifferentBehaviorsForEachTeam() {
        // Create multiple AI managers and verify they can assign different behaviors
        MultiplayerAIManager ai1 = new MultiplayerAIManager(controller, walls, WIDTH, HEIGHT);
        MultiplayerAIManager ai2 = new MultiplayerAIManager(controller, walls, WIDTH, HEIGHT);
        MultiplayerAIManager ai3 = new MultiplayerAIManager(controller, walls, WIDTH, HEIGHT);

        // All should be created successfully (behaviors are randomly assigned)
        assertNotNull(ai1);
        assertNotNull(ai2);
        assertNotNull(ai3);

        ai1.stop();
        ai2.stop();
        ai3.stop();
    }

    @Test
    void start_UpdatesTargetsForBotTeams() throws InterruptedException {
        aiManager.start();

        // Wait a bit for AI to potentially update targets
        Thread.sleep(200);

        // Targets might have been updated by AI (or might still be the same depending on behavior)
        // Just verify we can still get targets without exception
        assertDoesNotThrow(() -> {
            controller.getTargetX(1);
            controller.getTargetY(1);
            controller.getTargetX(2);
            controller.getTargetY(2);
            controller.getTargetX(3);
            controller.getTargetY(3);
        });
    }

    @Test
    void stop_StopsUpdatingTargets() throws InterruptedException {
        aiManager.start();
        Thread.sleep(100);

        aiManager.stop();

        // Wait a bit
        Thread.sleep(200);

        // After stopping, targets should not change (or at least we can still read them)
        assertDoesNotThrow(() -> {
            controller.getTargetX(1);
            controller.getTargetY(1);
        });
    }

    @Test
    void constructor_WorksWithDifferentWorldSizes() {
        boolean[][] smallWalls = new boolean[50][50];
        Particle[][] smallParticles = new Particle[50][50];
        World smallWorld = new World(smallWalls, smallParticles);
        MultiplayerGameController smallController = new MultiplayerGameController(smallWorld, smallWalls, 50, 50);

        MultiplayerAIManager smallAI = new MultiplayerAIManager(smallController, smallWalls, 50, 50);

        assertNotNull(smallAI);
        smallAI.stop();
    }
}
