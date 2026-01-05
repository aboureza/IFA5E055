package liquidwars;

import liquidwars.model.Particle;
import liquidwars.model.World;
import liquidwars.ui.GameController;
import liquidwars.ui.GamePanel;
import liquidwars.ui.HomeScreen;
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
    
    private static void startGame(JFrame frame, boolean aiEnabled)
    {
        // Grid size (in cells)
        // w = number of columns
        // h = number of rows
        int w = 160;
        int h = 100;

        // Simple obstacle layout for a demo run
        boolean [][] walls;
        try
        {
            walls = LevelLoader.loadWallsFromResource("/levels/map1.png", w, h);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

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
        GamePanel panel = new GamePanel(controller, w, h, 6);
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
        startGame(frame, true); // Default to AI enabled for restart
    }
    
    private static void showHomeScreen(JFrame frame) {
        HomeScreen homeScreen = new HomeScreen();
        homeScreen.setPlayAction(e -> startGame(frame, homeScreen.isAIEnabled()));
        
        frame.setContentPane(homeScreen);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Following method creates walls[y][x]
     * True means: obstacle (cannot enter)
     * 
     * Demo (for now):
     * - Border walls
     * - One vertical wall in the middle with a gap
     */
    private static boolean[][] makeWalls(int w, int h)
    {
        boolean[][] walls = new boolean[h][w];

        // Border walls
        for (int x = 0; x < w; x++)
        {
            walls[0][x] = true;
            walls[h-1][x] = true;
        }
        for (int y = 0; y < h; y++)
        {
            walls[y][0] = true;
            walls[y][w-1] = true;
        }

        // Middle vertical wall
        for (int y = h / 2 - 6; y <= h / 2 +6; y++)
        {
            walls[y][w / 2] = false;
        }

        return walls;
    }

    /**
     * Following method creates the particles[y][x]
     * 
     * Demo (for now):
     * - Team 0 mostly left
     * - Team 1 mostly right
     * - Spare patter so movement is visible and not too crowded
     */
    private static Particle[][] makeInitialParticles(int w, int h, boolean[][] walls)
    {
        Particle[][] parts = new Particle[h][w];

        for (int y = 1; y < h - 1; y++)
        {
            for (int x = 1; x < w - 1; x++)
            {
                if (walls[y][x]) continue;  // makes sure particles are not inside walls

                // Left zone for team 0
                if (x < w / 3)
                {
                    if ((x + y) % 3 == 0)
                    {
                        parts[y][x] = new Particle(0, 6);
                    }
                }

                // Right zone for team 1
                if (x > 2 * w / 3)
                {
                    if ((x + y) % 3 == 0)
                    {
                        parts[y][x] = new Particle(1, 6);
                    }
                }
            }
        }

        return parts;
    }
}