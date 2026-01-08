package liquidwars.ui;

import liquidwars.model.Particle;
import liquidwars.model.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class GamePanelTest {

    private World world;
    private boolean[][] walls;
    private GameController controller;
    private GamePanel panel;

    private final int W = 8;
    private final int H = 6;
    private final int CELL = 2;

    @BeforeEach
    void setup() {
        walls = new boolean[H][W];                 // all false (no walls)
        Particle[][] parts = new Particle[H][W];   // all null
        world = new World(walls, parts);

        controller = new GameController(world, walls, W, H);

        // aiEnabled=true so the panel doesn't rely on key focus/listeners.
        // We still test keyboard movement by calling updateKeyboardMovement via reflection.
        panel = new GamePanel(controller, W, H, CELL, true);
    }

    @Test
    void checkGameOver_AllTeam0Wins() throws Exception {
        fillWorldWithTeam(0);

        invokePrivate(panel, "checkGameOver");

        assertTrue((boolean) getPrivateField(panel, "gameOver"));
    }

    @Test
    void checkGameOver_AllTeam1Wins() throws Exception {
        fillWorldWithTeam(1);

        invokePrivate(panel, "checkGameOver");

        assertTrue((boolean) getPrivateField(panel, "gameOver"));
    }

    @Test
    void updateKeyboardMovement_WMovesTeam1Up() throws Exception {
        // Put team1 target at (4,3)
        controller.setTarget(1, 4, 3);

        // Force "W" key pressed in keysPressed[]
        boolean[] pressed = (boolean[]) getPrivateField(panel, "keysPressed");
        pressed[KeyEvent.VK_W] = true;

        invokePrivate(panel, "updateKeyboardMovement");

        assertEquals(4, controller.getTargetX(1));
        assertEquals(2, controller.getTargetY(1)); // moved up by 1
    }

    // helpers

    private void fillWorldWithTeam(int teamId) {
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                world.set(x, y, new Particle(teamId, 5));
            }
        }
    }

    private static void invokePrivate(Object obj, String methodName) throws Exception {
        Method m = obj.getClass().getDeclaredMethod(methodName);
        m.setAccessible(true);
        m.invoke(obj);
    }

    private static Object getPrivateField(Object obj, String fieldName) throws Exception {
        Field f = obj.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(obj);
    }
}
