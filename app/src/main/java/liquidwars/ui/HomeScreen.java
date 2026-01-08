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
    private final JButton toggleAIButton;
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
        playButton.setBackground(new java.awt.Color(0x202020));
        playButton.setForeground(java.awt.Color.WHITE);
        add(playButton);
        
        // Exit button
        exitButton = new JButton("Exit");
        exitButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        exitButton.setBackground(new java.awt.Color(0x202020));
        exitButton.setForeground(java.awt.Color.WHITE);
        exitButton.addActionListener(e -> System.exit(0));
        add(exitButton);
        
        // Toggle AI button in corner
        toggleAIButton = new JButton("Toggle AI Opponent");
        toggleAIButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        toggleAIButton.setBackground(new java.awt.Color(0x202020));
        toggleAIButton.setForeground(java.awt.Color.WHITE);
        toggleAIButton.addActionListener(e -> {
            aiEnabled = !aiEnabled;
            toggleAIButton.setText(aiEnabled ? "AI: ON" : "AI: OFF");
        });
        add(toggleAIButton);
    }
    
    @Override
    public void doLayout() {
        super.doLayout();
        
        int width = getWidth();
        int height = getHeight();
        
        // Position title in upper portion
        getComponent(0).setBounds(0, height / 4, width, 60);
        
        // Position buttons below title (vertically stacked)
        int buttonY = height / 4 + 100;
        int buttonWidth = 120;
        int buttonHeight = 40;
        int buttonSpacing = 10;
        
        playButton.setBounds(width / 2 - buttonWidth / 2, buttonY, buttonWidth, buttonHeight);
        exitButton.setBounds(width / 2 - buttonWidth / 2, buttonY + buttonHeight + buttonSpacing, buttonWidth, buttonHeight);
        
        // Position toggle AI button in top-left corner
        toggleAIButton.setBounds(10, 10, 160, 30);
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
    
    public boolean isAIEnabled() {
        return aiEnabled;
    }
}