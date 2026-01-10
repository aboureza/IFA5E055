package liquidwars;

import liquidwars.model.Particle;
import liquidwars.model.World;
import liquidwars.ui.GameController;
import liquidwars.ui.GamePanel;
import liquidwars.ui.HomeScreen;
import liquidwars.ui.MapSelectScreen;
import liquidwars.ui.AboutScreen;
import liquidwars.ui.MultiplayerGameController;
import liquidwars.ui.MultiplayerGamePanel;
import liquidwars.ai.OpponentAI;
import liquidwars.ai.OpponentManager; 

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * The full app together (for now)
 * 
 * What this file does:
 * 1) Creates a demo map (walls + initial particles)
 * 2) Creates a GameController (the "game logic" runner)
 * 3) Creates a GamePanel (the Swingg component that draws + handles mouse)
 * 4) Starts the loop (Timer) to animate the simulatio
 */

public class App {
    
    // Store currently selected map (persists across screen navigation)
    private static int selectedMapNumber = 1;
    
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> showHomeScreen());
    }
    
    private static void showHomeScreen()
    {
        JFrame frame = new JFrame("Liquid Wars");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        showHomeScreen(frame);
        frame.setVisible(true);
    }
    
    private static void startGame(JFrame frame, boolean aiEnabled, int mapNumber)
    {
        // Load the chosen map (try .png then .PNG)
        boolean [][] walls;
        try
        {
            try {
                walls = LevelLoader.loadWallsFromResourceAnySize("/levels/map" + mapNumber + ".png");
            } catch (IOException e) {
                walls = LevelLoader.loadWallsFromResourceAnySize("/levels/map" + mapNumber + ".PNG");
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        int h = walls.length;
        int w = walls[0].length;

        // Starting configuration of particles
        Particle[][] parts = makeInitialParticles(w, h, walls);

        // World is the current state (walls + particles)
        World world = new World(walls, parts);

        // Controller manages:
        // - current world state
        // - mouse targets per team
        // - per-frame tick: gradient computation + step simulation
        GameController controller = new GameController(world, walls, w, h);

        // Panel does:
        // - rendering
        // - mouse input
        // - calls controller.tick() on a timer
        GamePanel panel = new GamePanel(controller, w, h, 6, aiEnabled);
        panel.setLeaveAction(e -> returnToHome(frame, panel));
        panel.setPlayAgainAction(e -> restartGame(frame));
        panel.setExitToHomeAction(e -> returnToHome(frame, panel));

        frame.setContentPane(panel);

        // pack() uses panel.getPreferredSize() => correct window size for grid
        frame.pack();

        // Center the window screen
        frame.setLocationRelativeTo(null);

        // Start a simple opponent manager that mirrors the player and randomizes occasionally
        if (aiEnabled) {
            OpponentAI opponentAI = new OpponentAI(walls);
            OpponentManager opponentManager = new OpponentManager(controller, opponentAI);
            opponentManager.start();
        }

        // Start the simulation loop (timer)
        panel.startLoop();
    }
    
    private static void returnToHome(JFrame frame, GamePanel gamePanel) {
        gamePanel.stopGame();
        showHomeScreen(frame);
    }
    
    private static void restartGame(JFrame frame) {
        startGame(frame, true, selectedMapNumber); // Use stored map for restart
    }
    
    private static void showHomeScreen(JFrame frame) {
        HomeScreen homeScreen = new HomeScreen();
        homeScreen.setPlayAction(e -> startGame(frame, homeScreen.isAIEnabled(), selectedMapNumber));
        homeScreen.setMapSelectAction(e -> {
            MapSelectScreen ms = new MapSelectScreen();
            ms.setBackAction(a -> {
                selectedMapNumber = ms.getSelectedMap(); // Store selected map before returning
                showHomeScreen(frame);
            });
            ms.setPlayAction(a -> {
                selectedMapNumber = ms.getSelectedMap(); // Store selected map before playing
                startGame(frame, homeScreen.isAIEnabled(), selectedMapNumber);
            });
            frame.setContentPane(ms);
            frame.revalidate();
            frame.repaint();
        });

        homeScreen.setAboutAction(e -> {
            AboutScreen about = new AboutScreen();
            about.setBackAction(a -> showHomeScreen(frame));
            frame.setContentPane(about);
            frame.revalidate();
            frame.repaint();
        });

        homeScreen.setMultiplayerAction(e -> startMultiplayerGame(frame));
        
        frame.setContentPane(homeScreen);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Start multiplayer game with 4 teams.
     * Team 0 is controlled by player (mouse).
     * Teams 1-3 are bots (movement placeholder for now).
     */
    private static void startMultiplayerGame(JFrame frame) {
        // Load map (use stored selected map)
        boolean[][] walls;
        try {
            try {
                walls = LevelLoader.loadWallsFromResourceAnySize("/levels/map" + selectedMapNumber + ".png");
            } catch (IOException e) {
                walls = LevelLoader.loadWallsFromResourceAnySize("/levels/map" + selectedMapNumber + ".PNG");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int h = walls.length;
        int w = walls[0].length;

        // Create 4-team particles
        Particle[][] parts = makeMultiplayerParticles(w, h, walls);

        World world = new World(walls, parts);

        MultiplayerGameController controller = new MultiplayerGameController(world, walls, w, h);

        MultiplayerGamePanel panel = new MultiplayerGamePanel(controller, w, h, 6);
        panel.setLeaveAction(e -> returnToHomeMultiplayer(frame, panel));
        panel.setPlayAgainAction(e -> startMultiplayerGame(frame));
        panel.setExitToHomeAction(e -> returnToHomeMultiplayer(frame, panel));

        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);

        // Start simulation
        panel.startLoop();
    }

    private static void returnToHomeMultiplayer(JFrame frame, MultiplayerGamePanel panel) {
        panel.stopGame();
        showHomeScreen(frame);
    }

    /**
     * Following method creates the particles[y][x]
     * 
     * Demo (for now):
     * - Team 0 mostly left
     * - Team 1 mostly right
     * - Equal particle counts for both teams
     */
    private static Particle[][] makeInitialParticles(int w, int h, boolean[][] walls)
    {
        Particle[][] parts = new Particle[h][w];
        
        // Collect valid positions for each team
        java.util.List<int[]> team0Positions = new java.util.ArrayList<>();
        java.util.List<int[]> team1Positions = new java.util.ArrayList<>();

        for (int y = 1; y < h - 1; y++)
        {
            for (int x = 1; x < w - 1; x++)
            {
                if (walls[y][x]) continue;

                // Left zone for team 0
                if (x < w / 3 && (x + y) % 3 == 0)
                {
                    team0Positions.add(new int[]{x, y});
                }

                // Right zone for team 1
                if (x > 2 * w / 3 && (x + y) % 3 == 0)
                {
                    team1Positions.add(new int[]{x, y});
                }
            }
        }
        
        // Use the smaller count to ensure equal teams
        int particleCount = Math.min(team0Positions.size(), team1Positions.size());
        
        // Place equal number of particles for both teams
        for (int i = 0; i < particleCount; i++)
        {
            int[] pos0 = team0Positions.get(i);
            parts[pos0[1]][pos0[0]] = new Particle(0, 6);
            
            int[] pos1 = team1Positions.get(i);
            parts[pos1[1]][pos1[0]] = new Particle(1, 6);
        }

        return parts;
    }

    /**
     * Create initial particles for 4-team multiplayer.
     * Teams are placed in 4 corners/zones:
     * - Team 0 (red): top-left
     * - Team 1 (blue): top-right
     * - Team 2 (green): bottom-left
     * - Team 3 (yellow): bottom-right
     */
    private static Particle[][] makeMultiplayerParticles(int w, int h, boolean[][] walls) {
        Particle[][] parts = new Particle[h][w];
        
        java.util.List<int[]> team0Positions = new java.util.ArrayList<>();
        java.util.List<int[]> team1Positions = new java.util.ArrayList<>();
        java.util.List<int[]> team2Positions = new java.util.ArrayList<>();
        java.util.List<int[]> team3Positions = new java.util.ArrayList<>();

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                if (walls[y][x]) continue;

                // Top-left zone for team 0
                if (x < w / 2 && y < h / 2 && (x + y) % 3 == 0) {
                    team0Positions.add(new int[]{x, y});
                }

                // Top-right zone for team 1
                if (x >= w / 2 && y < h / 2 && (x + y) % 3 == 0) {
                    team1Positions.add(new int[]{x, y});
                }

                // Bottom-left zone for team 2
                if (x < w / 2 && y >= h / 2 && (x + y) % 3 == 0) {
                    team2Positions.add(new int[]{x, y});
                }

                // Bottom-right zone for team 3
                if (x >= w / 2 && y >= h / 2 && (x + y) % 3 == 0) {
                    team3Positions.add(new int[]{x, y});
                }
            }
        }
        
        // Use minimum count to ensure equal teams
        int particleCount = Math.min(
            Math.min(team0Positions.size(), team1Positions.size()),
            Math.min(team2Positions.size(), team3Positions.size())
        );
        
        // Place equal particles for all 4 teams
        for (int i = 0; i < particleCount; i++) {
            int[] pos0 = team0Positions.get(i);
            parts[pos0[1]][pos0[0]] = new Particle(0, 6);
            
            int[] pos1 = team1Positions.get(i);
            parts[pos1[1]][pos1[0]] = new Particle(1, 6);
            
            int[] pos2 = team2Positions.get(i);
            parts[pos2[1]][pos2[0]] = new Particle(2, 6);
            
            int[] pos3 = team3Positions.get(i);
            parts[pos3[1]][pos3[0]] = new Particle(3, 6);
        }

        return parts;
    }
}