package liquidwars.ui;

import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JLabel;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class AboutScreenTest {

    @Test
    void constructor_CreatesComponentsCorrectly() {
        AboutScreen screen = new AboutScreen();

        assertNotNull(screen);
        assertEquals(3, screen.getComponentCount(), "Should have title, text, and back button");
    }

    @Test
    void constructor_CreatesTitleLabel() {
        AboutScreen screen = new AboutScreen();

        assertTrue(screen.getComponent(0) instanceof JLabel);
        JLabel title = (JLabel) screen.getComponent(0);
        assertEquals("About", title.getText());
    }

    @Test
    void constructor_CreatesAboutTextLabel() {
        AboutScreen screen = new AboutScreen();

        assertTrue(screen.getComponent(1) instanceof JLabel);
        JLabel text = (JLabel) screen.getComponent(1);
        assertTrue(text.getText().contains("Habiba El Bastawisy"));
        assertTrue(text.getText().contains("Ahmed Abourezk"));
    }

    @Test
    void setBackAction_AddsActionListener() {
        AboutScreen screen = new AboutScreen();
        AtomicBoolean actionFired = new AtomicBoolean(false);

        screen.setBackAction(e -> actionFired.set(true));

        // Find the back button and trigger it
        JButton backButton = findBackButton(screen);
        assertNotNull(backButton);

        backButton.doClick();
        assertTrue(actionFired.get(), "Back action should be triggered");
    }

    @Test
    void doLayout_PositionsComponentsWithinBounds() {
        AboutScreen screen = new AboutScreen();
        screen.setSize(800, 600);
        screen.doLayout();

        // Verify all components are within screen bounds
        for (int i = 0; i < screen.getComponentCount(); i++) {
            var component = screen.getComponent(i);
            assertTrue(component.getX() >= 0);
            assertTrue(component.getY() >= 0);
            assertTrue(component.getX() + component.getWidth() <= screen.getWidth());
            assertTrue(component.getY() + component.getHeight() <= screen.getHeight());
        }
    }

    @Test
    void backButton_HasCorrectText() {
        AboutScreen screen = new AboutScreen();
        JButton backButton = findBackButton(screen);

        assertNotNull(backButton);
        assertEquals("Back", backButton.getText());
    }

    private JButton findBackButton(AboutScreen screen) {
        for (int i = 0; i < screen.getComponentCount(); i++) {
            if (screen.getComponent(i) instanceof JButton button) {
                if ("Back".equals(button.getText())) {
                    return button;
                }
            }
        }
        return null;
    }
}
