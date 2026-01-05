package liquidwars.ui;

import liquidwars.model.Particle;
import liquidwars.model.World;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * GamePanel = the Swing visual component.
 *
 * Responsibilities:
 * 1) Run a Swing Timer loop:
 *      controller.tick();
 *      repaint();
 *
 * 2) Render the world grid:
 *      - walls drawn dark gray
 *      - empty cells black
 *      - particles colored by team + energy (brightness)
 *
 * 3) Handle mouse input:
 *      - Left click/drag sets target for team 0
 *      - Right click/drag sets target for team 1
 *      - Mouse move updates team 0 target live (optional behavior)
 *
 * Rendering performance:
 * - We draw into a small BufferedImage buffer of size (gridW x gridH)
 * - Then scale it up on screen (gridW*cellSize x gridH*cellSize)
 * This is faster and simpler than drawing thousands of rectangles every frame.
 */

public final class GamePanel extends JPanel {
    
    private final GameController controller;

    private final int gridW;    // number of columns
    private final int gridH;    // number of rows
    private final int cellSize; // pixel size of one cell when displayed

    // Offscreen image we draw the world into each frame
    private final BufferedImage buffer;

    // Swing timer = our "game loop"
    private final Timer timer;
    
    // Small leave button in corner
    private final JButton leaveButton;
    
    // Game area and progress bar
    private final JPanel gameArea;
    private final JPanel progressPanel;
    
    // Game over state
    private boolean gameOver = false;
    private boolean redPlayerWon = false;
    private boolean bluePlayerWon = false;
    private final JButton playAgainButton;
    private final JButton exitToHomeButton;

    public GamePanel (GameController controller, int gridW, int gridH, int cellSize)
    {
        this.controller = controller;
        this.gridW = gridW;
        this.gridH = gridH;
        this.cellSize = cellSize;

        // Buffer is 1 pixel per cell
        this.buffer = new BufferedImage(gridW, gridH, BufferedImage.TYPE_INT_RGB);

        setLayout(new BorderLayout());
        
        // Game area for the actual game rendering
        gameArea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                renderGame(g);
            }
        };
        gameArea.setPreferredSize(new Dimension(gridW * cellSize, gridH * cellSize));
        gameArea.setLayout(null); // For absolute positioning of leave button
        
        // Small leave button in top-right corner of game area
        leaveButton = new JButton("Leave");
        leaveButton.setSize(70, 25);
        gameArea.add(leaveButton);
        
        // Victory buttons (initially hidden)
        playAgainButton = new JButton("Play Again");
        playAgainButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        playAgainButton.setBackground(new java.awt.Color(0x202020));
        playAgainButton.setForeground(java.awt.Color.WHITE);
        playAgainButton.setVisible(false);
        gameArea.add(playAgainButton);
        
        exitToHomeButton = new JButton("Exit to Home");
        exitToHomeButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        exitToHomeButton.setBackground(new java.awt.Color(0x202020));
        exitToHomeButton.setForeground(java.awt.Color.WHITE);
        exitToHomeButton.setVisible(false);
        gameArea.add(exitToHomeButton);
        
        // Progress bar panel
        progressPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawProgressBar((Graphics2D) g);
            }
        };
        progressPanel.setPreferredSize(new Dimension(gridW * cellSize, 30));
        progressPanel.setBackground(java.awt.Color.BLACK);
        
        add(gameArea, BorderLayout.CENTER);
        add(progressPanel, BorderLayout.SOUTH);

        // FPS: 33 ms~ 30 fps
        this.timer = new Timer(33, e -> {
            if (!gameOver) {
                controller.tick();  // update simulation only if not game over
            }
            checkGameOver();
            repaint();          // redraw
        });

        // Mouse handlers on game area only
        MouseHandler mh = new MouseHandler();
        gameArea.addMouseListener(mh);
        gameArea.addMouseMotionListener(mh);
    }
    
    @Override
    public void doLayout() {
        super.doLayout();
        // Position leave button in top-right corner of game area
        if (gameArea != null && leaveButton != null) {
            leaveButton.setLocation(gameArea.getWidth() - leaveButton.getWidth() - 10, 10);
        }
        
        // Position victory buttons
        if (gameArea != null && playAgainButton != null && exitToHomeButton != null) {
            int width = gameArea.getWidth();
            int height = gameArea.getHeight();
            int buttonWidth = 180;
            int buttonHeight = 40;
            int buttonSpacing = 10;
            int startY = height / 2 + 50;
            
            playAgainButton.setBounds(width / 2 - buttonWidth / 2, startY, buttonWidth, buttonHeight);
            exitToHomeButton.setBounds(width / 2 - buttonWidth / 2, startY + buttonHeight + buttonSpacing, buttonWidth, buttonHeight);
        }
    }
    
    public void setLeaveAction(ActionListener action) {
        leaveButton.addActionListener(action);
    }
    
    public void setPlayAgainAction(ActionListener action) {
        playAgainButton.addActionListener(action);
    }
    
    public void setExitToHomeAction(ActionListener action) {
        exitToHomeButton.addActionListener(action);
    }
    
    private void checkGameOver() {
        if (gameOver) return;
        
        World world = controller.getWorld();
        int team0Count = 0;
        int team1Count = 0;
        
        // Count particles for each team
        for (int y = 0; y < gridH; y++) {
            for (int x = 0; x < gridW; x++) {
                Particle p = world.get(x, y);
                if (p != null) {
                    if (p.teamId() == 0) team0Count++;
                    else if (p.teamId() == 1) team1Count++;
                }
            }
        }
        
        // Check if one team has 100% of particles
        if (team0Count > 0 && team1Count == 0) {
            gameOver = true;
            redPlayerWon = true;
            showVictoryScreen();
        } else if (team1Count > 0 && team0Count == 0) {
            gameOver = true;
            bluePlayerWon = true;
            showVictoryScreen();
        }
    }
    
    private void showVictoryScreen() {
        leaveButton.setVisible(false);
        playAgainButton.setVisible(true);
        exitToHomeButton.setVisible(true);
    }

    
    //This method starts the simulation loop.
    public void startLoop()
    {
        timer.start();
    }

    /**
     * Renders the game in the game area
     */
    private void renderGame(Graphics g)
    {
        World w = controller.getWorld();

        // Convert world state into pixels in 'buffer'
        renderToBuffer(w);

        Graphics2D g2 = (Graphics2D) g;

        // Draw scale up to fit the game area
        g2.drawImage(buffer, 0, 0, gridW * cellSize, gridH * cellSize, null);

        // Draw targets on top (so they are visible)
        drawTargets(g2, controller.getTargetX(0), controller.getTargetY(0), 0);
        drawTargets(g2, controller.getTargetX(1), controller.getTargetY(1), 1);
        
        // Draw victory overlay if either player won
        if (gameOver && (redPlayerWon || bluePlayerWon)) {
            drawVictoryOverlay(g2);
        }
    }
    
    private void drawVictoryOverlay(Graphics2D g2) {
        // Semi-transparent dark overlay
        g2.setColor(new java.awt.Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gridW * cellSize, gridH * cellSize);
        
        // Victory text based on winner
        if (redPlayerWon) {
            g2.setColor(java.awt.Color.RED);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 72));
            String victoryText = "BOOYAH";
            int textWidth = g2.getFontMetrics().stringWidth(victoryText);
            int textX = (gridW * cellSize - textWidth) / 2;
            int textY = gridH * cellSize / 2 - 50;
            g2.drawString(victoryText, textX, textY);
        } else if (bluePlayerWon) {
            g2.setColor(java.awt.Color.BLUE);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
            String victoryText = "RUN BACK TO YOUR MOMMY LOSER";
            int textWidth = g2.getFontMetrics().stringWidth(victoryText);
            int textX = (gridW * cellSize - textWidth) / 2;
            int textY = gridH * cellSize / 2 - 50;
            g2.drawString(victoryText, textX, textY);
        }
    }
    
    private void drawProgressBar(Graphics2D g2) {
        World world = controller.getWorld();
        int team0Count = 0;
        int team1Count = 0;
        
        // Count particles for each team
        for (int y = 0; y < gridH; y++) {
            for (int x = 0; x < gridW; x++) {
                Particle p = world.get(x, y);
                if (p != null) {
                    if (p.teamId() == 0) team0Count++;
                    else if (p.teamId() == 1) team1Count++;
                }
            }
        }
        
        int totalCount = team0Count + team1Count;
        if (totalCount == 0) return;
        
        // Progress bar dimensions (use full progress panel)
        int barHeight = 20;
        int barY = 5;
        int barWidth = progressPanel.getWidth() - 20;
        int barX = 10;
        
        // Calculate widths
        int team0Width = (team0Count * barWidth) / totalCount;
        int team1Width = barWidth - team0Width;
        
        // Draw team 0 (red) portion
        g2.setColor(java.awt.Color.RED);
        g2.fillRect(barX, barY, team0Width, barHeight);
        
        // Draw team 1 (blue) portion
        g2.setColor(java.awt.Color.BLUE);
        g2.fillRect(barX + team0Width, barY, team1Width, barHeight);
        
        // Draw border
        g2.setColor(java.awt.Color.WHITE);
        g2.drawRect(barX, barY, barWidth, barHeight);
    }

    /**
     * The following writes RBG values into the buffer:
     * - walls = gray
     * - empty = black
     * - particle = team colour + brightness by energy
     */
    private void renderToBuffer (World w)
    {
        for (int y = 0; y < gridH; y++)
        {
            for(int x = 0; x < gridW; x++)
            {
                int rgb;

                if (w.isWall(x, y))
                {
                    rgb = 0x202020; // dark gray
                }
                else
                {
                    Particle p = w.get(x, y);
                    if (p == null)
                    {
                        rgb = 0x00000;  // empty = black
                    }
                    else
                    {
                        // team coulor + brightness based on energy
                        rgb = ColourUtil.particleRGB(p.teamId(), p.energy());
                    }
                }

                buffer.setRGB(x, y, rgb);
            }
        }
    }

    /**
     * Draws a small square marker at the target location
     * Drawn in screen coordinates = (grid cell coord * cellSize)
     */
    private void drawTargets(Graphics2D g2, int tx, int ty, int teamId)
    {
        int px = tx * cellSize;
        int py = ty * cellSize;

        // Marker size should be visible even if cellSize is small
        int size = Math.max(2, cellSize / 2);

        // Team target colours
        int rgb = (teamId == 0) ? 0x00FF00 : 0xFFFF00;  // green or yellow
        g2.setColor(new java.awt.Color(rgb));

        g2.fillRect(
            px + (cellSize - size) / 2, 
            py + (cellSize - size) / 2,
            size,
            size
        );
    }

    // Mouse mapping:
    // Mouse coordinates are pixels
    private final class MouseHandler extends MouseAdapter 
    {
        @Override
        public void mousePressed(MouseEvent e)
        {
            handleMouse(e);
        }

        @Override
        public void mouseMoved(MouseEvent e)
        {
            int gx = e.getX() / cellSize;
            int gy = e.getY() / cellSize;
            controller.setTarget(0, gx, gy);
        }

        private void handleMouse (MouseEvent e)
        {
            int gx = e.getX() / cellSize;
            int gy = e.getY() / cellSize;

            // Left click/drag => team 0 target
            if (SwingUtilities.isLeftMouseButton(e))
            {
                controller.setTarget(0, gx, gy);
            }

            // Right click/drag => team 1 target
            else if (SwingUtilities.isRightMouseButton(e))
            {
                controller.setTarget(1, gx, gy);
            }
        }
    }
    
    public void stopGame() {
        timer.stop();
    }
}
