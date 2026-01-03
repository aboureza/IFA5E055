package liquidwars.ui;

import liquidwars.algo.GradientComputer;
import liquidwars.model.World;
import liquidwars.sim.StepSimulator;

import java.util.List;
import java.util.Map;

public final class GameController {
    private final int width;
    private final int height;
    private final boolean[][] wallsStable;  // [y][x], never changes

    private final GradientComputer gradientComputer = new GradientComputer();
    private final StepSimulator stepSimulator = new StepSimulator();

    // Targets (mouse-controlled)
    private volatile int target0x;
    private volatile int target0y;
    private volatile int target1x;
    private volatile int target1y;

    private volatile World world;

    public GameController (World initialWorld, boolean[][] wallsStable, int width, int height)
    {
        this.world = initialWorld;
        this.wallsStable = wallsStable;
        this.width = width;
        this.height = height;

        // Default targets
        this.target0x = width / 4;
        this.target0y = height / 2;

        this.target1x = 3 * width / 4;
        this.target1y = height / 2;
    }

    public World getWorld()
    {
        return world;
    }

    public int getTargetX (int teamId)
    {
        return (teamId == 0) ? target0x : target1x;
    }

    public int getTargetY (int teamId)
    {
        return (teamId == 0) ? target0y : target1y;
    }

    public void setTarget(int teamId, int x, int y)
    {
        x = clamp(x, 0, width - 1);
        y = clamp(y, 0, height - 1);

        // if you click on a wall, ignore (keeps target valid)
        if (wallsStable[y][x]) return;

        if (teamId == 0)
        {
            target0x = x;
            target0y = y;
        }
        else 
        {
            target1x = x;
            target1y = y;
        }
    }

    /**
     * One frame step:
     * 1) compute gradients for each team
     * 2) apply StepSimulator.step(...)
     */
    public void tick()
    {
        World cur = world;

        int[][] g0 = gradientComputer.compute(wallsStable, 
            List.of(new GradientComputer.Pos(target0x, target0y))
        );

        int [][] g1 = gradientComputer.compute(wallsStable, 
            List.of(new GradientComputer.Pos(target1x, target1y))
        );

        world = stepSimulator.step(cur, Map.of(0, g0, 1, g1));
    }

    private int clamp(int v, int lo, int hi) {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }
}
