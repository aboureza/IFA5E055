package liquidwars.ui;

import liquidwars.LevelLoader;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
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
    private final BufferedImage mapBackground;
    
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
        
        setLayout(new BorderLayout());
        
        // Title
        JLabel title = new JLabel("Liquid Wars", JLabel.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
        title.setForeground(java.awt.Color.WHITE);
        add(title, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        
        playButton = new JButton("Play");
        playButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        buttonPanel.add(playButton);
        
        exitButton = new JButton("Exit");
        exitButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        exitButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(exitButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
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
}