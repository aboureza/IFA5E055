package liquidwars.sim;

import liquidwars.model.Particle;
import liquidwars.model.World;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Apply the movement rules for ONE tick on the whole population
 * 
 * Input:
 * - current World (Walls + particles)
 * - gradients per team: gradients.get(teamId[y][x] gives the distance to that team's target)
 * 
 * Output:
 * - a NEW World instance 
 * 
 * The rule priority list is taken from the project statement. :contentReference[oaicite:4]{index=4}
 */
public final class StepSimulator {

    public static final int ENERGY_MIN = 0;
    public static final int ENERGY_MAX = 10;
    private static final int ENERGY_DELTA = 1;

    // Deterministic tie-break order: right, left, down, up
    private static final int[] DX = { 1, -1, 0, 0 };
    private static final int[] DY = { 0,  0, 1, -1 };

    public World step(World current, Map<Integer, int[][]> gradientsByTeam) {
        World next = current.copy();

        // Decide all actions using CURRENT (so decisions aren't affected by earlier updates)
        List<PlannedAction> plan = new ArrayList<>();

        for (int y = 0; y < current.height(); y++) 
        {
            for (int x = 0; x < current.width(); x++) 
            {
                Particle p = current.get(x, y);
                if (p == null) continue;

                int[][] grad = gradientsByTeam.get(p.teamId());
                if (grad == null) 
                {
                    throw new IllegalArgumentException("Missing gradient for teamId=" + p.teamId());
                }

                Action a = decide(current, grad, x, y, p.teamId());
                plan.add(new PlannedAction(x, y, a));
            }
        }

        // Apply on NEXT
        for (PlannedAction pa : plan) 
        {
            apply(next, pa.x(), pa.y(), pa.action());
        }

        return next;
    }

    private enum ActionType { MOVE, ATTACK, TRANSFER, NONE }

    private record Action(ActionType type, int tx, int ty) 
    {
        static Action none() { return new Action(ActionType.NONE, -1, -1); }
    }

    private record PlannedAction(int x, int y, Action action) {}

    private Action decide(World world, int[][] grad, int x, int y, int teamId) 
    {
        int g0 = grad[y][x];

        // Find minimal gradient among valid neighbours
        int minG = Integer.MAX_VALUE;
        for (int i = 0; i < 4; i++) 
        {
            int nx = x + DX[i];
            int ny = y + DY[i];
            if (!validNeighbour(world, nx, ny)) continue;
            minG = Math.min(minG, grad[ny][nx]);
        }
        if (minG == Integer.MAX_VALUE) return Action.none();

        // 1) main free -> move
        for (int i = 0; i < 4; i++) 
        {
            int nx = x + DX[i];
            int ny = y + DY[i];
            if (!validNeighbour(world, nx, ny)) continue;
            if (grad[ny][nx] == minG && isFree(world, nx, ny)) return new Action(ActionType.MOVE, nx, ny);
        }

        // 2) good free -> move (strictly smaller than current)
        for (int i = 0; i < 4; i++) 
        {
            int nx = x + DX[i];
            int ny = y + DY[i];
            if (!validNeighbour(world, nx, ny)) continue;
            if (grad[ny][nx] < g0 && isFree(world, nx, ny)) return new Action(ActionType.MOVE, nx, ny);
        }

        // 3) acceptable free -> move (equal to current)
        for (int i = 0; i < 4; i++) 
        {
            int nx = x + DX[i];
            int ny = y + DY[i];
            if (!validNeighbour(world, nx, ny)) continue;
            if (grad[ny][nx] == g0 && isFree(world, nx, ny)) return new Action(ActionType.MOVE, nx, ny);
        }

        // 4) main enemy -> attack
        for (int i = 0; i < 4; i++) 
        {
            int nx = x + DX[i];
            int ny = y + DY[i];
            if (!validNeighbour(world, nx, ny)) continue;
            if (grad[ny][nx] == minG && isEnemy(world, nx, ny, teamId)) return new Action(ActionType.ATTACK, nx, ny);
        }

        // 5) good enemy -> attack
        for (int i = 0; i < 4; i++) 
        {
            int nx = x + DX[i];
            int ny = y + DY[i];
            if (!validNeighbour(world, nx, ny)) continue;
            if (grad[ny][nx] < g0 && isEnemy(world, nx, ny, teamId)) return new Action(ActionType.ATTACK, nx, ny);
        }

        // 6) main friend -> transfer
        if (minG < g0)
        {
            for (int i = 0; i < 4; i++) 
            {
                int nx = x + DX[i];
                int ny = y + DY[i];
                if (!validNeighbour(world, nx, ny)) continue;
                if (grad[ny][nx] == minG && isFriend(world, nx, ny, teamId)) return new Action(ActionType.TRANSFER, nx, ny);
            }
        }

        // 7) else nothing
        return Action.none();
    }

    private boolean validNeighbour(World world, int x, int y) 
    {
        return world.inBounds(x, y) && !world.isWall(x, y);
    }

    private boolean isFree(World world, int x, int y) 
    {
        return world.get(x, y) == null;
    }

    private boolean isEnemy(World world, int x, int y, int myTeam) 
    {
        Particle other = world.get(x, y);
        return other != null && other.teamId() != myTeam;
    }

    private boolean isFriend(World world, int x, int y, int myTeam) 
    {
        Particle other = world.get(x, y);
        return other != null && other.teamId() == myTeam;
    }

    private void apply(World next, int x, int y, Action action) 
    {
        Particle actor = next.get(x, y);
        if (actor == null) return; // might have moved away already

        if (action.type() == ActionType.MOVE) 
        {
            int nx = action.tx();
            int ny = action.ty();

            // Only move if destination still empty in NEXT (conflict resolution)
            if (next.inBounds(nx, ny) && !next.isWall(nx, ny) && next.get(nx, ny) == null) 
            {
                next.set(nx, ny, actor);
                next.set(x, y, null);
            }
            return;
        }

        if (action.type() == ActionType.ATTACK) 
        {
            int tx = action.tx();
            int ty = action.ty();

            Particle target = next.get(tx, ty);
            if (target == null) return;
            if (target.teamId() == actor.teamId()) return;

            // Steal 1 energy if possible
            if (target.energy() > ENERGY_MIN) 
            {
                target = target.withEnergy(target.energy() - ENERGY_DELTA);
                actor = actor.withEnergy(actor.energy() + ENERGY_DELTA);
                next.set(x, y, actor);
            }

            // Convert if target has no energy left
            if (target.energy() <= ENERGY_MIN) 
            {
                target = target.withTeam(actor.teamId()).withEnergy(ENERGY_MIN);
            }

            next.set(tx, ty, target);
            return;
        }

        if (action.type() == ActionType.TRANSFER) 
        {
            int tx = action.tx();
            int ty = action.ty();

            Particle friend = next.get(tx, ty);
            if (friend == null) return;
            if (friend.teamId() != actor.teamId()) return;

            // Transfer 1 energy if donor > min and friend < max
            if (actor.energy() > ENERGY_MIN && friend.energy() < ENERGY_MAX) 
            {
                actor = actor.withEnergy(actor.energy() - ENERGY_DELTA);
                friend = friend.withEnergy(friend.energy() + ENERGY_DELTA);
                next.set(x, y, actor);
                next.set(tx, ty, friend);
            }
        }
    }
}