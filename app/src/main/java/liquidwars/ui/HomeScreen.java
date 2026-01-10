package liquidwars.ui;

import liquidwars.LevelLoader;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Simple home screen with title and play button over game map background
 */
public final class HomeScreen extends JPanel {
    
    private final JButton playButton;
    private final JButton exitButton;
    private final JButton mapSelectButton;
    private final JButton vsAIButton;
    private final JButton localPlayButton;
    private final JButton multiplayerButton;
    private final JButton aboutButton;
    private final BufferedImage mapBackground; 
    
    private boolean aiEnabled = true;
    
    public HomeScreen() {
        // Load map for background
        boolean[][] walls;
        try {
            walls = LevelLoader.loadWallsFromResource("/levels/map1.png", 160, 100);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        // Create background image
        mapBackground = createMapBackground(walls, 160, 100);
        
        setLayout(null); // Use absolute positioning
        
        // Title
        JLabel title = new JLabel("Liquid Wars", JLabel.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
        title.setForeground(java.awt.Color.WHITE);
        add(title);
        
        // Play button
        playButton = new JButton("Play");
        playButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        playButton.setBackground(new java.awt.Color(0x006600));
        playButton.setForeground(java.awt.Color.WHITE);
        add(playButton);
        
        // Exit button
        exitButton = new JButton("Exit");
        exitButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        exitButton.setBackground(new java.awt.Color(0x202020));
        exitButton.setForeground(java.awt.Color.WHITE);
        exitButton.addActionListener(e -> System.exit(0));
        add(exitButton);
        
        // Map Select button
        mapSelectButton = new JButton("Map Select");
        mapSelectButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        mapSelectButton.setBackground(new java.awt.Color(0x202020));
        mapSelectButton.setForeground(java.awt.Color.WHITE);
        add(mapSelectButton);
        
        // AI mode buttons in corner
        vsAIButton = new JButton("vs AI");
        vsAIButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        vsAIButton.setBackground(new java.awt.Color(0x202020));
        vsAIButton.setForeground(java.awt.Color.WHITE);
        vsAIButton.addActionListener(e -> {
            aiEnabled = true;
            updateAIButtons();
        });
        add(vsAIButton);

        localPlayButton = new JButton("Local Play");
        localPlayButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        localPlayButton.setBackground(new java.awt.Color(0x202020));
        localPlayButton.setForeground(java.awt.Color.WHITE);
        localPlayButton.addActionListener(e -> {
            aiEnabled = false;
            updateAIButtons();
        });
        add(localPlayButton);

        // Multiplayer button (placeholder action can be attached by caller)
        multiplayerButton = new JButton("Multiplayer");
        multiplayerButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        multiplayerButton.setBackground(new java.awt.Color(0x202020));
        multiplayerButton.setForeground(java.awt.Color.WHITE);
        add(multiplayerButton);

        // About button in corner
        aboutButton = new JButton("About");
        aboutButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        aboutButton.setBackground(new java.awt.Color(0x202020));
        aboutButton.setForeground(java.awt.Color.WHITE);
        add(aboutButton);

        // Initialize buttons to reflect default AI state
        updateAIButtons();
    }
    
    @Override
    public void doLayout() {
        super.doLayout();
        
        int width = getWidth();
        int height = getHeight();
        
        // Position title in upper portion (moved up slightly to avoid crowding buttons)
        getComponent(0).setBounds(0, height / 6, width, 60);
        
        // Position About button in top-right corner
        aboutButton.setBounds(width - 110, 10, 100, 30);
        
        // Position buttons below title (vertically stacked)
        // moved up slightly to reduce crowding
        int buttonY = height / 4 + 60;
        int buttonWidth = 160; // widen so labels like "Local Play" are fully visible
        int buttonHeight = 40;
        int buttonSpacing = 10;
        
        // Exit button sits at the bottom
        int exitY = height - buttonHeight - 20;
        // Compute stack for the other buttons (play, map select, vs AI, local play, multiplayer)
        int stackCount = 5;
        int stackHeight = stackCount * buttonHeight + (stackCount - 1) * buttonSpacing;
        int stackTop = buttonY;
        int maxStackBottom = exitY - buttonSpacing;
        int stackBottom = stackTop + stackHeight;
        if (stackBottom > maxStackBottom) {
            // Shift stack upward so it doesn't overlap the exit button
            stackTop = Math.max(10, maxStackBottom - stackHeight);
        }
        
        playButton.setBounds(width / 2 - buttonWidth / 2, stackTop, buttonWidth, buttonHeight);
        mapSelectButton.setBounds(width / 2 - buttonWidth / 2, stackTop + (buttonHeight + buttonSpacing), buttonWidth, buttonHeight);
        vsAIButton.setBounds(width / 2 - buttonWidth / 2, stackTop + 2 * (buttonHeight + buttonSpacing), buttonWidth, buttonHeight);
        localPlayButton.setBounds(width / 2 - buttonWidth / 2, stackTop + 3 * (buttonHeight + buttonSpacing), buttonWidth, buttonHeight);
        multiplayerButton.setBounds(width / 2 - buttonWidth / 2, stackTop + 4 * (buttonHeight + buttonSpacing), buttonWidth, buttonHeight);
        
        // Place Exit at very bottom
        exitButton.setBounds(width / 2 - buttonWidth / 2, exitY, buttonWidth, buttonHeight);
    }
    
    private BufferedImage createMapBackground(boolean[][] walls, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = walls[y][x] ? 0x202020 : 0x000000; // gray walls, black empty
                img.setRGB(x, y, rgb);
            }
        }
        
        return img;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw scaled map background
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(mapBackground, 0, 0, getWidth(), getHeight(), null);
    }
    
    public void setPlayAction(ActionListener action) {
        playButton.addActionListener(action);
    }

    public void setMapSelectAction(ActionListener action) {
        mapSelectButton.addActionListener(action);
    }

    public void setMultiplayerAction(ActionListener action) {
        multiplayerButton.addActionListener(action);
    }

    public void setAboutAction(ActionListener action) {
        aboutButton.addActionListener(action);
    }
    
    private void updateAIButtons() {
        // disable the currently selected mode button so user can see the active mode,
        // and enable the other so they can switch modes
        vsAIButton.setEnabled(!aiEnabled);
        localPlayButton.setEnabled(aiEnabled);
    }
    
    public boolean isAIEnabled() {
        return aiEnabled;
    }
}