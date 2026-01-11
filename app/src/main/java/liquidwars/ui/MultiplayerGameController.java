package liquidwars.ui;

import liquidwars.algo.GradientComputer;
import liquidwars.model.World;
import liquidwars.sim.StepSimulator;

import java.util.List;
import java.util.Map;

/**
 * Game controller for 4-team multiplayer mode.
 * Manages world state, targets for 4 teams, and simulation ticks.
 * Isolated from 2-team GameController to maintain OOP separation.
 */
public final class MultiplayerGameController {
    private final int width;
    private final int height;
    private final boolean[][] wallsStable;  // [y][x], never changes

    private final GradientComputer gradientComputer = new GradientComputer();
    private final StepSimulator stepSimulator = new StepSimulator();

    // Targets for 4 teams (mouse/bot-controlled)
    private volatile int target0x, target0y;
    private volatile int target1x, target1y;
    private volatile int target2x, target2y;
    private volatile int target3x, target3y;

    private volatile World world;

    public MultiplayerGameController(World initialWorld, boolean[][] wallsStable, int width, int height) {
        this.world = initialWorld;
        this.wallsStable = wallsStable;
        this.width = width;
        this.height = height;

        // Default targets (spread across map)
        this.target0x = width / 4;
        this.target0y = height / 4;

        this.target1x = 3 * width / 4;
        this.target1y = height / 4;

        this.target2x = width / 4;
        this.target2y = 3 * height / 4;

        this.target3x = 3 * width / 4;
        this.target3y = 3 * height / 4;
    }

    public World getWorld() {
        return world;
    }

    public int getTargetX(int teamId) {
        return switch (teamId) {
            case 0 -> target0x;
            case 1 -> target1x;
            case 2 -> target2x;
            case 3 -> target3x;
            default -> throw new IllegalArgumentException("Invalid teamId: " + teamId);
        };
    }

    public int getTargetY(int teamId) {
        return switch (teamId) {
            case 0 -> target0y;
            case 1 -> target1y;
            case 2 -> target2y;
            case 3 -> target3y;
            default -> throw new IllegalArgumentException("Invalid teamId: " + teamId);
        };
    }

    public void setTarget(int teamId, int x, int y) {
        x = clamp(x, 0, width - 1);
        y = clamp(y, 0, height - 1);

        // if you click on a wall, ignore (keeps target valid)
        if (wallsStable[y][x]) return;

        switch (teamId) {
            case 0 -> { target0x = x; target0y = y; }
            case 1 -> { target1x = x; target1y = y; }
            case 2 -> { target2x = x; target2y = y; }
            case 3 -> { target3x = x; target3y = y; }
            default -> throw new IllegalArgumentException("Invalid teamId: " + teamId);
        }
    }

    /**
     * One frame step:
     * 1) compute gradients for each of the 4 teams
     * 2) apply StepSimulator.step(...)
     */
    public void tick() {
        World cur = world;

        int[][] g0 = gradientComputer.compute(wallsStable,
            List.of(new GradientComputer.Pos(target0x, target0y))
        );

        int[][] g1 = gradientComputer.compute(wallsStable,
            List.of(new GradientComputer.Pos(target1x, target1y))
        );

        int[][] g2 = gradientComputer.compute(wallsStable,
            List.of(new GradientComputer.Pos(target2x, target2y))
        );

        int[][] g3 = gradientComputer.compute(wallsStable,
            List.of(new GradientComputer.Pos(target3x, target3y))
        );

        world = stepSimulator.step(cur, Map.of(0, g0, 1, g1, 2, g2, 3, g3));
    }

    private int clamp(int v, int lo, int hi) {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }
}
