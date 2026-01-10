package liquidwars.ui;

import liquidwars.LevelLoader;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.ImageIcon;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;


/**
 * Simple home screen with title and play button over game map background
 */
public final class HomeScreen extends JPanel {
    
    private final JButton playButton;
    private final JButton exitButton;
    private final JButton toggleAIButton;
    
    // GIF background
    private final ImageIcon bgGif;
    private final Timer repaintTimer;
    
    private boolean aiEnabled = true;
    
    public HomeScreen() {
        setLayout(null); // Use absolute positioning

        // Load GIF from resources
        java.net.URL url = getClass().getResource("/ui/Animation.gif");
        if (url == null) {
            throw new IllegalArgumentException("Missing resource: /ui/Animation.gif");
        }
        bgGif = new ImageIcon(url);

        // Repaint regularly so the GIF animates smoothly
        repaintTimer = new Timer(33, e -> repaint()); // 33 FPS
        repaintTimer.start();
        
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
        getComponent(0).setBounds(0, height / 6, width, 200);
        
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
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw scaled GIF background
        Graphics2D g2 = (Graphics2D) g;
        g.drawImage(bgGif.getImage(), 0, 0, getWidth(), getHeight(), this);
    }
    
    public void setPlayAction(ActionListener action) {
        playButton.addActionListener(action);
    }
    
    public boolean isAIEnabled() {
        return aiEnabled;
    }

    // Stop animation when leaving the home screen
    public void stopAnimation() {
        repaintTimer.stop();
    }
}