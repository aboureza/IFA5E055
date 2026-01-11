package liquidwars.ui;

import liquidwars.model.Particle;
import liquidwars.model.World;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * Multiplayer game panel for 4 teams.
 * Player controls team 0 with mouse, teams 1-3 are bots (movement to be implemented).
 * Isolated from 2-team GamePanel to maintain strict OOP separation.
 */
public final class MultiplayerGamePanel extends JPanel {
    
    private final MultiplayerGameController controller;

    private final int gridW;
    private final int gridH;
    private final int cellSize;

    private final BufferedImage buffer;
    private final Timer timer;
    private final JButton leaveButton;
    private final JPanel gameArea;
    private final JPanel progressPanel;
    
    private boolean gameOver = false;
    private int winningTeam = -1;
    
    private final long gameStartTime;
    private static final long GAME_DURATION_MS = 5 * 60 * 1000; // 5 minutes
    
    private final JButton playAgainButton;
    private final JButton exitToHomeButton;

    public MultiplayerGamePanel(MultiplayerGameController controller, int gridW, int gridH, int cellSize) {
        this.controller = controller;
        this.gridW = gridW;
        this.gridH = gridH;
        this.cellSize = cellSize;
        this.gameStartTime = System.currentTimeMillis();

        this.buffer = new BufferedImage(gridW, gridH, BufferedImage.TYPE_INT_RGB);

        setLayout(new BorderLayout());
        
        // Game area for rendering
        gameArea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                renderGame(g);
            }
        };
        gameArea.setPreferredSize(new Dimension(gridW * cellSize, gridH * cellSize));
        gameArea.setLayout(null);
        
        // Leave button in top-right corner
        leaveButton = new JButton("Leave");
        leaveButton.setSize(70, 25);
        gameArea.add(leaveButton);
        
        // Victory buttons (initially hidden)
        playAgainButton = new JButton("Play Again");
        playAgainButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        playAgainButton.setBackground(new Color(0x202020));
        playAgainButton.setForeground(Color.WHITE);
        playAgainButton.setVisible(false);
        gameArea.add(playAgainButton);
        
        exitToHomeButton = new JButton("Exit to Home");
        exitToHomeButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        exitToHomeButton.setBackground(new Color(0x202020));
        exitToHomeButton.setForeground(Color.WHITE);
        exitToHomeButton.setVisible(false);
        gameArea.add(exitToHomeButton);
        
        // Progress bar panel (4 bars for 4 teams)
        progressPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawProgressBars((Graphics2D) g);
            }
        };
        progressPanel.setPreferredSize(new Dimension(gridW * cellSize, 70)); // taller for 4 bars
        progressPanel.setBackground(Color.BLACK);
        
        add(gameArea, BorderLayout.CENTER);
        add(progressPanel, BorderLayout.SOUTH);

        // Timer: 30 fps
        this.timer = new Timer(33, e -> {
            if (!gameOver) {
                controller.tick();
            }
            checkGameOver();
            repaint();
        });

        // Mouse handler - player controls team 0
        MouseHandler mh = new MouseHandler();
        gameArea.addMouseListener(mh);
        gameArea.addMouseMotionListener(mh);
    }
    
    @Override
    public void doLayout() {
        super.doLayout();
        if (gameArea != null && leaveButton != null) {
            leaveButton.setLocation(gameArea.getWidth() - leaveButton.getWidth() - 10, 10);
        }
        
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
        Map<Integer, Integer> teamCounts = countParticlesMultithreaded(world);
        
        // Remove teams with 0 particles
        teamCounts.entrySet().removeIf(entry -> entry.getValue() == 0);
        
        // Check if only one team remains
        if (teamCounts.size() == 1) {
            gameOver = true;
            winningTeam = teamCounts.keySet().iterator().next();
            showVictoryScreen();
            return;
        }
        
        // Check if time is up
        long elapsed = System.currentTimeMillis() - gameStartTime;
        if (elapsed >= GAME_DURATION_MS) {
            gameOver = true;
            // Winner is team with most particles
            winningTeam = teamCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);
            showVictoryScreen();
        }
    }
    
    private void showVictoryScreen() {
        leaveButton.setVisible(false);
        playAgainButton.setVisible(true);
        exitToHomeButton.setVisible(true);
    }
    
    public void startLoop() {
        timer.start();
    }
    
    private void renderGame(Graphics g) {
        World w = controller.getWorld();

        renderToBuffer(w);

        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(buffer, 0, 0, gridW * cellSize, gridH * cellSize, null);

        // Draw targets for all 4 teams
        for (int teamId = 0; teamId < 4; teamId++) {
            drawTarget(g2, controller.getTargetX(teamId), controller.getTargetY(teamId), teamId);
        }
        
        drawTimer(g2);
        
        if (gameOver && winningTeam >= 0) {
            drawVictoryOverlay(g2);
        }
    }
    
    private void drawVictoryOverlay(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gridW * cellSize, gridH * cellSize);
        
        Color winColor = getTeamColor(winningTeam);
        String teamName = getTeamName(winningTeam);
        
        // Large team color and wins text
        g2.setColor(winColor);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 72));
        String victoryText = teamName.toUpperCase() + " WINS";
        int textWidth = g2.getFontMetrics().stringWidth(victoryText);
        int textX = (gridW * cellSize - textWidth) / 2;
        int textY = gridH * cellSize / 2 - 50;
        g2.drawString(victoryText, textX, textY);
        
        // Slightly smaller message below
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
        String message = "This town isn't big enough for the four of us";
        int messageWidth = g2.getFontMetrics().stringWidth(message);
        int messageX = (gridW * cellSize - messageWidth) / 2;
        g2.drawString(message, messageX, textY + 80);
    }
    
    private void drawTimer(Graphics2D g2) {
        long elapsed = System.currentTimeMillis() - gameStartTime;
        long remaining = Math.max(0, GAME_DURATION_MS - elapsed);
        
        int minutes = (int) (remaining / 60000);
        int seconds = (int) ((remaining % 60000) / 1000);
        
        String timeText = String.format("%d:%02d", minutes, seconds);
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        g2.drawString(timeText, 10, 30);
    }
    
    private void drawProgressBars(Graphics2D g2) {
        World world = controller.getWorld();
        Map<Integer, Integer> teamCounts = countParticlesMultithreaded(world);
        
        int totalCount = teamCounts.values().stream().mapToInt(Integer::intValue).sum();
        if (totalCount == 0) return;
        
        int barHeight = 12;
        int barSpacing = 2;
        int barWidth = progressPanel.getWidth() - 20;
        int barX = 10;
        
        int currentY = 2;
        for (int teamId = 0; teamId < 4; teamId++) {
            int count = teamCounts.getOrDefault(teamId, 0);
            
            int teamBarWidth = (count * barWidth) / totalCount;
            
            Color teamColor = getTeamColor(teamId);
            
            // Draw filled portion
            g2.setColor(teamColor);
            g2.fillRect(barX, currentY, teamBarWidth, barHeight);
            
            // Draw empty portion
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(barX + teamBarWidth, currentY, barWidth - teamBarWidth, barHeight);
            
            // Draw border
            g2.setColor(Color.WHITE);
            g2.drawRect(barX, currentY, barWidth, barHeight);
            
            // Draw team label
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            String label = getTeamName(teamId);
            g2.drawString(label, barX + barWidth + 5, currentY + barHeight);
            
            currentY += barHeight + barSpacing;
        }
    }
    
    private Color getTeamColor(int teamId) {
        return switch (teamId) {
            case 0 -> Color.RED;
            case 1 -> Color.BLUE;
            case 2 -> Color.GREEN;
            case 3 -> Color.YELLOW;
            default -> Color.WHITE;
        };
    }
    
    private String getTeamName(int teamId) {
        return switch (teamId) {
            case 0 -> "Red";
            case 1 -> "Blue";
            case 2 -> "Green";
            case 3 -> "Yellow";
            default -> "Unknown";
        };
    }
    
    private Map<Integer, Integer> countParticlesMultithreaded(World world) {
        Map<Integer, Integer> teamCounts = new ConcurrentHashMap<>();
        
        IntStream.range(0, gridH).parallel().forEach(y -> {
            for (int x = 0; x < gridW; x++) {
                Particle p = world.get(x, y);
                if (p != null) {
                    teamCounts.merge(p.teamId(), 1, (a, b) -> a + b);
                }
            }
        });
        
        return teamCounts;
    }

    private void renderToBuffer(World w) {
        for (int y = 0; y < gridH; y++) {
            for (int x = 0; x < gridW; x++) {
                int rgb;

                if (w.isWall(x, y)) {
                    rgb = 0x202020;
                } else {
                    Particle p = w.get(x, y);
                    if (p == null) {
                        rgb = 0x000000;
                    } else {
                        rgb = ColourUtil.particleRGB(p.teamId(), p.energy());
                    }
                }

                buffer.setRGB(x, y, rgb);
            }
        }
    }

    private void drawTarget(Graphics2D g2, int tx, int ty, int teamId) {
        int px = tx * cellSize;
        int py = ty * cellSize;

        int size = Math.max(2, cellSize / 2);

        // Different marker colors for visibility
        Color markerColor = switch (teamId) {
            case 0 -> Color.WHITE;      // player
            case 1 -> Color.CYAN;       // bot 1
            case 2 -> Color.MAGENTA;    // bot 2
            case 3 -> Color.ORANGE;     // bot 3
            default -> Color.WHITE;
        };
        
        g2.setColor(markerColor);
        g2.fillRect(
            px + (cellSize - size) / 2, 
            py + (cellSize - size) / 2,
            size,
            size
        );
    }

    private final class MouseHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            handleMouse(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int gx = e.getX() / cellSize;
            int gy = e.getY() / cellSize;
            controller.setTarget(0, gx, gy);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            int gx = e.getX() / cellSize;
            int gy = e.getY() / cellSize;
            controller.setTarget(0, gx, gy);
        }

        private void handleMouse(MouseEvent e) {
            int gx = e.getX() / cellSize;
            int gy = e.getY() / cellSize;
            controller.setTarget(0, gx, gy);
        }
    }
    
    public void stopGame() {
        timer.stop();
    }
}
