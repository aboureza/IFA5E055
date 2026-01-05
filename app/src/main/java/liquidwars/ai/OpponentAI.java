package liquidwars.ai;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.LongSupplier;

/**
 * Simple opponent AI that mirrors the player's target, and every
 * `randomIntervalMillis` picks a random free cell as a temporary target.
 *
 * Design notes:
 * - Keeps logic encapsulated for easy testing and reuse
 * - Uses dependency injection for Random and clock for deterministic tests
 */
public final class OpponentAI {

    public static final long DEFAULT_RANDOM_INTERVAL_MS = 5000L;

    private final boolean[][] walls; // walls[y][x]
    private final int width;
    private final int height;
    private final long randomIntervalMillis;
    private final Random rnd;
    private final LongSupplier clock;

    // Last time a random target was chosen
    private long lastRandomizeMillis;

    public record Target(int x, int y) {}

    public OpponentAI(boolean[][] walls) {
        this(walls, DEFAULT_RANDOM_INTERVAL_MS, new Random(), System::currentTimeMillis);
    }

    public OpponentAI(boolean[][] walls, long randomIntervalMillis, Random rnd, LongSupplier clock) {
        Objects.requireNonNull(walls, "walls");
        Objects.requireNonNull(rnd, "rnd");
        Objects.requireNonNull(clock, "clock");

        if (walls.length == 0 || walls[0].length == 0) throw new IllegalArgumentException("walls must be non-empty");

        this.height = walls.length;
        this.width = walls[0].length;

        for (int y = 0; y < height; y++) {
            if (walls[y] == null || walls[y].length != width) throw new IllegalArgumentException("walls must be rectangular");
        }

        this.walls = walls;
        this.randomIntervalMillis = randomIntervalMillis;
        this.rnd = rnd;
        this.clock = clock;

        // Start counting from now so the first random event occurs after the interval
        this.lastRandomizeMillis = clock.getAsLong();
    }

    /**
     * Compute the next target for the opponent given the player's target.
     * Behavior:
     * - If the random interval passed since last random pick, choose a random free cell and return it.
     * - Otherwise, return the mirrored cell (x -> width-1-x, same y). If mirrored is a wall, return the nearest free cell.
     */
    public Target nextTarget(int playerTx, int playerTy) {
        long now = clock.getAsLong();

        if (now - lastRandomizeMillis >= randomIntervalMillis) {
            Target r = chooseRandomFreeCell();
            // If no free cell exists, fall back to mirrored behaviour
            if (r != null) {
                lastRandomizeMillis = now;
                return r;
            }
        }

        // Mirror horizontally
        int mx = width - 1 - playerTx;
        int my = clamp(playerTy, 0, height - 1);

        if (!isWall(mx, my)) return new Target(mx, my);

        Target n = findNearestFree(mx, my);
        return n != null ? n : new Target(mx, my);
    }

    private boolean isWall(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return true;
        return walls[y][x];
    }

    private int clamp(int v, int lo, int hi) {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }

    // Gather all free cells and pick one at random
    private Target chooseRandomFreeCell() {
        List<Target> free = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!walls[y][x]) free.add(new Target(x, y));
            }
        }
        if (free.isEmpty()) return null;
        int idx = rnd.nextInt(free.size());
        return free.get(idx);
    }

    // BFS to find nearest free (non-wall) cell. Returns null if none found
    private Target findNearestFree(int sx, int sy) {
        boolean[][] seen = new boolean[height][width];
        Deque<Target> q = new ArrayDeque<>();
        if (sx >= 0 && sx < width && sy >= 0 && sy < height) {
            q.addLast(new Target(sx, sy));
            seen[sy][sx] = true;
        }

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        while (!q.isEmpty()) {
            Target cur = q.removeFirst();
            if (!isWall(cur.x, cur.y)) return cur;

            for (int i = 0; i < 4; i++) {
                int nx = cur.x + dx[i];
                int ny = cur.y + dy[i];
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue;
                if (seen[ny][nx]) continue;
                seen[ny][nx] = true;
                q.addLast(new Target(nx, ny));
            }
        }

        return null;
    }
}
