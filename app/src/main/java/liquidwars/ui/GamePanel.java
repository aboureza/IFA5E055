package liquidwars.ui;

import liquidwars.model.Particle;
import liquidwars.model.World;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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

    public GamePanel (GameController controller, int gridW, int gridH, int cellSize)
    {
        this.controller = controller;
        this.gridW = gridW;
        this.gridH = gridH;
        this.cellSize = cellSize;

        // Buffer is 1 pixel per cell
        // Going to scale it to cellSize in paintComponent
        this.buffer = new BufferedImage(gridW, gridH, BufferedImage.TYPE_INT_RGB);

        // Window size = grid size times cell size
        setPreferredSize(new Dimension(gridW * cellSize, gridH * cellSize));
        setFocusable(true);

        // FPS:
        // 33 ms~ 30 fps
        this.timer = new Timer(33, e -> {
            controller.tick();  // update simulation
            repaint();          // redraw
        });

        // Mouse handlers:
        MouseHandler mh = new MouseHandler();
        addMouseListener(mh);
        addMouseMotionListener(mh);
    }

    
    //This method starts the simulation loop.
    public void startLoop()
    {
        timer.start();
    }

    /**
     * Swing calls whenever the panel needs repainting
     * We draw the buffer (scale up) then draw target markers on top
     */
    @Override
    protected void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        World w = controller.getWorld();

        // Convert world state into pixels in 'buffer'
        renderToBuffer(w);

        Graphics2D g2 = (Graphics2D) g;

        // Draw scale up to fit the window
        g2.drawImage(buffer, 0, 0, gridW * cellSize, gridH * cellSize, null);

        // Draw targets on top (so they are visible)
        drawTargets(g2, controller.getTargetX(0), controller.getTargetY(0), 0);
        drawTargets(g2, controller.getTargetX(1), controller.getTargetY(1), 1);
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
}
