package liquidwars.ui;

import liquidwars.model.Particle;
import liquidwars.model.World;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class GamePanelTest {

    @Test
    void startLoopAndStopGame_toggleTimerRunning() throws Exception {
        GamePanel panel = makePanelWithSimpleController(false);

        Timer timer = getField(panel, "timer", Timer.class);
        assertFalse(timer.isRunning());

        panel.startLoop();
        assertTrue(timer.isRunning());

        panel.stopGame();
        assertFalse(timer.isRunning());
    }

    @Test
    void doLayout_positionsLeaveButtonTopRight() throws Exception {
        GamePanel panel = makePanelWithSimpleController(false);

        // give panel a size so BorderLayout can lay out children
        panel.setSize(400, 300);
        panel.doLayout();

        JPanel gameArea = getField(panel, "gameArea", JPanel.class);
        JButton leave = getField(panel, "leaveButton", JButton.class);

        int expectedX = gameArea.getWidth() - leave.getWidth() - 10;
        int expectedY = 10;

        assertEquals(expectedX, leave.getX());
        assertEquals(expectedY, leave.getY());
    }

    @Test
    void renderToBuffer_wallsEmptyAndParticleHaveExpectedRGB() throws Exception {
        int gridW = 3, gridH = 1, cell = 10;

        boolean[][] walls = new boolean[gridH][gridW];
        walls[0][0] = true; // wall at (0,0)

        Particle[][] parts = new Particle[gridH][gridW];
        parts[0][2] = new Particle(0, 10); // particle at (2,0)

        World world = new World(walls, parts);
        GameController controller = new GameController(world, walls, gridW, gridH);

        GamePanel panel = new GamePanel(controller, gridW, gridH, cell, true);

        // invoke private renderToBuffer(World)
        Method m = GamePanel.class.getDeclaredMethod("renderToBuffer", World.class);
        m.setAccessible(true);
        m.invoke(panel, world);

        BufferedImage buffer = getField(panel, "buffer", BufferedImage.class);

        int wallRgb = buffer.getRGB(0, 0) & 0xFFFFFF;
        int emptyRgb = buffer.getRGB(1, 0) & 0xFFFFFF;
        int particleRgb = buffer.getRGB(2, 0) & 0xFFFFFF;

        assertEquals(0x202020, wallRgb);
        assertEquals(0x000000, emptyRgb);
        assertEquals(ColourUtil.particleRGB(0, 10), particleRgb);
    }

    @Test
    void mouseLeftClick_setsTeam0Target() throws Exception {
        int gridW = 10, gridH = 10, cell = 10;
        GameController controller = makeController(gridW, gridH);
        GamePanel panel = new GamePanel(controller, gridW, gridH, cell, true);

        JPanel gameArea = getField(panel, "gameArea", JPanel.class);

        // click at pixel (35, 25) -> grid (3,2)
        MouseEvent e = new MouseEvent(
                gameArea,
                MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(),
                0,
                35, 25,
                1,
                false,
                MouseEvent.BUTTON1
        );

        for (var ml : gameArea.getMouseListeners()) {
            ml.mousePressed(e);
        }

        assertEquals(3, controller.getTargetX(0));
        assertEquals(2, controller.getTargetY(0));
    }

    @Test
    void mouseRightClick_setsTeam1Target() throws Exception {
        int gridW = 10, gridH = 10, cell = 10;
        GameController controller = makeController(gridW, gridH);
        GamePanel panel = new GamePanel(controller, gridW, gridH, cell, true);

        JPanel gameArea = getField(panel, "gameArea", JPanel.class);

        // click at pixel (95, 15) -> grid (9,1)
        MouseEvent e = new MouseEvent(
                gameArea,
                MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(),
                0,
                95, 15,
                1,
                false,
                MouseEvent.BUTTON3
        );

        for (var ml : gameArea.getMouseListeners()) {
            ml.mousePressed(e);
        }

        assertEquals(9, controller.getTargetX(1));
        assertEquals(1, controller.getTargetY(1));
    }

    @Test
    void keyboardW_movesTeam1TargetUpByOneWhenAiDisabled() throws Exception {
        int gridW = 10, gridH = 10, cell = 10;

        boolean[][] walls = new boolean[gridH][gridW];
        Particle[][] parts = new Particle[gridH][gridW];
        World world = new World(walls, parts);
        GameController controller = new GameController(world, walls, gridW, gridH);

        GamePanel panel = new GamePanel(controller, gridW, gridH, cell, false);

        int beforeX = controller.getTargetX(1);
        int beforeY = controller.getTargetY(1);

        // simulate key press 'W'
        KeyEvent pressW = new KeyEvent(panel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_W, 'W');
        for (var kl : panel.getKeyListeners()) {
            kl.keyPressed(pressW);
        }

        // invoke private updateKeyboardMovement()
        Method m = GamePanel.class.getDeclaredMethod("updateKeyboardMovement");
        m.setAccessible(true);
        m.invoke(panel);

        assertEquals(beforeX, controller.getTargetX(1));
        assertEquals(Math.max(0, beforeY - 1), controller.getTargetY(1));
    }

    @Test
    void checkGameOver_team0Only_showsVictoryButtons() throws Exception {
        int gridW = 3, gridH = 3, cell = 10;

        boolean[][] walls = new boolean[gridH][gridW];
        Particle[][] parts = new Particle[gridH][gridW];
        // only team 0 particles
        parts[0][0] = new Particle(0, 1);
        parts[1][1] = new Particle(0, 1);

        World world = new World(walls, parts);
        GameController controller = new GameController(world, walls, gridW, gridH);
        GamePanel panel = new GamePanel(controller, gridW, gridH, cell, true);

        // invoke private checkGameOver()
        Method m = GamePanel.class.getDeclaredMethod("checkGameOver");
        m.setAccessible(true);
        m.invoke(panel);

        boolean gameOver = getField(panel, "gameOver", Boolean.class);
        boolean redWon = getField(panel, "redPlayerWon", Boolean.class);

        JButton leave = getField(panel, "leaveButton", JButton.class);
        JButton playAgain = getField(panel, "playAgainButton", JButton.class);
        JButton exitHome = getField(panel, "exitToHomeButton", JButton.class);

        assertTrue(gameOver);
        assertTrue(redWon);

        assertFalse(leave.isVisible());
        assertTrue(playAgain.isVisible());
        assertTrue(exitHome.isVisible());
    }

    // The following are helpers

    private static GamePanel makePanelWithSimpleController(boolean aiEnabled) {
        int gridW = 10, gridH = 10, cell = 10;
        GameController controller = makeController(gridW, gridH);
        return new GamePanel(controller, gridW, gridH, cell, aiEnabled);
    }

    private static GameController makeController(int w, int h) {
        boolean[][] walls = new boolean[h][w];
        Particle[][] parts = new Particle[h][w];
        World world = new World(walls, parts);
        return new GameController(world, walls, w, h);
    }

    private static <T> T getField(Object obj, String name, Class<T> type) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object v = f.get(obj);
        return type.cast(v);
    }
}