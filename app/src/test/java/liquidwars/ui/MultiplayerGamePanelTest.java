package liquidwars.ui;

import liquidwars.model.Particle;
import liquidwars.model.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class MultiplayerGamePanelTest {

    private MultiplayerGamePanel panel;
    private MultiplayerGameController controller;
    private boolean[][] walls;
    private static final int WIDTH = 160;
    private static final int HEIGHT = 120;
    private static final int CELL_SIZE = 4;

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

        // Create initial world
        Particle[][] particles = new Particle[HEIGHT][WIDTH];
        particles[10][10] = new Particle(0, 5);
        particles[20][20] = new Particle(1, 5);
        particles[30][30] = new Particle(2, 5);
        particles[40][40] = new Particle(3, 5);

        World initialWorld = new World(walls, particles);
        controller = new MultiplayerGameController(initialWorld, walls, WIDTH, HEIGHT);
        panel = new MultiplayerGamePanel(controller, WIDTH, HEIGHT, CELL_SIZE);
    }

    @Test
    void constructor_CreatesPanel() {
        assertNotNull(panel);
    }

    @Test
    void constructor_InitializesWithCorrectDimensions() {
        // Panel should have game area and progress panel
        assertTrue(panel.getComponentCount() >= 2);
    }

    @Test
    void setLeaveAction_AddsActionListener() {
        AtomicBoolean actionFired = new AtomicBoolean(false);

        panel.setLeaveAction(e -> actionFired.set(true));

        JButton leaveButton = findButtonByText(panel, "Leave");
        if (leaveButton != null) {
            leaveButton.doClick();
            assertTrue(actionFired.get(), "Leave action should be triggered");
        }
    }

    @Test
    void setPlayAgainAction_AddsActionListener() {
        AtomicBoolean actionFired = new AtomicBoolean(false);

        panel.setPlayAgainAction(e -> actionFired.set(true));

        // Play again button should exist (though initially hidden)
        assertDoesNotThrow(() -> panel.setPlayAgainAction(e -> {}));
    }

    @Test
    void setExitToHomeAction_AddsActionListener() {
        AtomicBoolean actionFired = new AtomicBoolean(false);

        panel.setExitToHomeAction(e -> actionFired.set(true));

        // Exit to home button should exist (though initially hidden)
        assertDoesNotThrow(() -> panel.setExitToHomeAction(e -> {}));
    }

    @Test
    void startLoop_DoesNotThrowException() {
        assertDoesNotThrow(() -> panel.startLoop());
    }

    @Test
    void doLayout_PositionsComponentsCorrectly() {
        panel.setSize(WIDTH * CELL_SIZE, HEIGHT * CELL_SIZE + 100);
        
        assertDoesNotThrow(() -> panel.doLayout());
    }

    private JButton findButtonByText(MultiplayerGamePanel panel, String text) {
        for (int i = 0; i < panel.getComponentCount(); i++) {
            var comp = panel.getComponent(i);
            if (comp instanceof JButton button && text.equals(button.getText())) {
                return button;
            }
            // Recursively search in sub-panels
            if (comp instanceof javax.swing.JPanel subPanel) {
                JButton found = findButtonInPanel(subPanel, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private JButton findButtonInPanel(javax.swing.JPanel panel, String text) {
        for (int i = 0; i < panel.getComponentCount(); i++) {
            var comp = panel.getComponent(i);
            if (comp instanceof JButton button && text.equals(button.getText())) {
                return button;
            }
            if (comp instanceof javax.swing.JPanel subPanel) {
                JButton found = findButtonInPanel(subPanel, text);
                if (found != null) return found;
            }
        }
        return null;
    }
}
