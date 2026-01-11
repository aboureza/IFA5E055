package liquidwars.ui;

import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JLabel;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class MapSelectScreenTest {

    @Test
    void constructor_CreatesComponentsCorrectly() {
        MapSelectScreen screen = new MapSelectScreen();

        assertNotNull(screen);
        assertTrue(screen.getComponentCount() >= 4, "Should have title, map label, next map button, and back button");
    }

    @Test
    void constructor_CreatesTitleLabel() {
        MapSelectScreen screen = new MapSelectScreen();

        assertTrue(screen.getComponent(0) instanceof JLabel);
        JLabel title = (JLabel) screen.getComponent(0);
        assertEquals("Map Select", title.getText());
    }

    @Test
    void constructor_CreatesMapLabelWithInitialMap() {
        MapSelectScreen screen = new MapSelectScreen();

        assertTrue(screen.getComponent(1) instanceof JLabel);
        JLabel mapLabel = (JLabel) screen.getComponent(1);
        assertTrue(mapLabel.getText().contains("Map: 1"));
    }

    @Test
    void nextMapButton_CyclesThroughMaps() {
        MapSelectScreen screen = new MapSelectScreen();

        JButton nextMapButton = findButtonByText(screen, "Next Map");
        assertNotNull(nextMapButton);

        JLabel mapLabel = (JLabel) screen.getComponent(1);

        // Initial map should be 1
        assertTrue(mapLabel.getText().contains("1"));

        // Click next map
        nextMapButton.doClick();
        assertTrue(mapLabel.getText().contains("2"));

        // Click again
        nextMapButton.doClick();
        assertTrue(mapLabel.getText().contains("3"));
    }

    @Test
    void nextMapButton_CyclesBackToOne() {
        MapSelectScreen screen = new MapSelectScreen();

        JButton nextMapButton = findButtonByText(screen, "Next Map");
        assertNotNull(nextMapButton);

        JLabel mapLabel = (JLabel) screen.getComponent(1);

        // Cycle through all maps (1-5)
        for (int i = 0; i < 5; i++) {
            nextMapButton.doClick();
        }

        // Should be back at map 1
        assertTrue(mapLabel.getText().contains("1"));
    }

    @Test
    void setBackAction_AddsActionListener() {
        MapSelectScreen screen = new MapSelectScreen();
        AtomicBoolean actionFired = new AtomicBoolean(false);

        screen.setBackAction(e -> actionFired.set(true));

        JButton backButton = findButtonByText(screen, "Back");
        assertNotNull(backButton);

        backButton.doClick();
        assertTrue(actionFired.get(), "Back action should be triggered");
    }

    @Test
    void getSelectedMap_ReturnsCorrectMapNumber() {
        MapSelectScreen screen = new MapSelectScreen();

        assertEquals(1, screen.getSelectedMap(), "Initial map should be 1");

        JButton nextMapButton = findButtonByText(screen, "Next Map");
        nextMapButton.doClick();

        assertEquals(2, screen.getSelectedMap(), "Map should be 2 after one click");
    }

    @Test
    void doLayout_PositionsComponentsWithinBounds() {
        MapSelectScreen screen = new MapSelectScreen();
        screen.setSize(800, 600);
        screen.doLayout();

        // Verify all components are within screen bounds
        for (int i = 0; i < screen.getComponentCount(); i++) {
            var component = screen.getComponent(i);
            assertTrue(component.getX() >= 0);
            assertTrue(component.getY() >= 0);
        }
    }



    private JButton findButtonByText(MapSelectScreen screen, String text) {
        for (int i = 0; i < screen.getComponentCount(); i++) {
            if (screen.getComponent(i) instanceof JButton button) {
                if (text.equals(button.getText())) {
                    return button;
                }
            }
        }
        return null;
    }
}
