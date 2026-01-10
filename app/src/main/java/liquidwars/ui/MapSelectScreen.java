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
 * Map selection screen with map preview background
 */
public final class MapSelectScreen extends JPanel {
    
    private final JButton nextMapButton;
    private final JButton backButton;
    private BufferedImage mapBackground;
    private int selectedMap = 1;
    
    public MapSelectScreen() {
        // Load initial map background
        mapBackground = loadMapBackground(selectedMap);
        
        setLayout(null); // Use absolute positioning
        
        // Title
        JLabel title = new JLabel("Map Select", JLabel.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
        title.setForeground(java.awt.Color.WHITE);
        add(title);
        
        // Map info label
        JLabel mapLabel = new JLabel("Map: " + selectedMap, JLabel.CENTER);
        mapLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        mapLabel.setForeground(java.awt.Color.WHITE);
        add(mapLabel);
        
        // Next Map button
        nextMapButton = new JButton("Next Map");
        nextMapButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        nextMapButton.setBackground(new java.awt.Color(0x404040));
        nextMapButton.setForeground(java.awt.Color.WHITE);
        nextMapButton.addActionListener(e -> cycleMap());
        add(nextMapButton);
        
        // Back button
        backButton = new JButton("Back");
        backButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        backButton.setBackground(new java.awt.Color(0x202020));
        backButton.setForeground(java.awt.Color.WHITE);
        add(backButton);
    }
    
    @Override
    public void doLayout() {
        super.doLayout();
        
        int width = getWidth();
        int height = getHeight();
        
        // Position title
        getComponent(0).setBounds(0, height / 6, width, 50);
        
        // Position map label
        getComponent(1).setBounds(0, height / 6 + 60, width, 30);
        
        // Position buttons
        int buttonWidth = 140;
        int buttonHeight = 40;
        int buttonY = height - 120;
        
        nextMapButton.setBounds(width / 2 - buttonWidth - 10, buttonY, buttonWidth, buttonHeight);
        backButton.setBounds(width / 2 + 10, buttonY, buttonWidth, buttonHeight);
        
        // Position play button if it exists
        if (getComponentCount() > 4) {
            getComponent(4).setBounds(width / 2 - buttonWidth / 2, buttonY - 60, buttonWidth, buttonHeight);
        }
    }
    
    private BufferedImage loadMapBackground(int mapNumber) {
        boolean[][] walls;
        try {
            String mapFile;
            if (mapNumber <= 3) {
                mapFile = "/levels/map" + mapNumber + ".png";
            } else {
                mapFile = "/levels/map" + mapNumber + ".PNG";
            }
            walls = LevelLoader.loadWallsFromResourceAnySize(mapFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return createMapBackground(walls, walls[0].length, walls.length);
    }
    
    private void cycleMap() {
        selectedMap = (selectedMap % 5) + 1; // Cycle through maps 1-5
        ((JLabel) getComponent(1)).setText("Map: " + selectedMap);
        mapBackground = loadMapBackground(selectedMap);
        repaint();
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
    
    public void setBackAction(ActionListener action) {
        backButton.addActionListener(action);
    }
    
    public void setPlayAction(ActionListener action) {
        // Add a Play button that uses the selected map
        JButton playButton = new JButton("Play");
        playButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        playButton.setBackground(new java.awt.Color(0x006600));
        playButton.setForeground(java.awt.Color.WHITE);
        playButton.addActionListener(action);
        add(playButton);
    }
    
    public int getSelectedMap() {
        return selectedMap;
    }
}