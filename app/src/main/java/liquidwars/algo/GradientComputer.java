package liquidwars.algo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Milestone A: Gradient computation
 *
 * Convention:
 * - x = column index (left/right)
 * - y = row index (up/down on the screen; y increases downward)
 * - 2D arrays are accessed as grid[y][x]
 *
 * Output:
 * - dist[y][x] = shortest number of steps to the nearest target cell
 * - targets have distance 0
 * - obstacles are unreachable (INF)
 *
 * This is BFS (Breadth-First Search) from the targets outward.
 */

public class GradientComputer {
    public static final int INF = 1_000_000_000;

    // A grid coordinate: x = colun, y = row
    public record  Pos(int x, int y) {}

    /**
     * @param obstacles obstacles[y][x] == true means the cell is blocked
     * @param targets list of target cells (for one team)
     * @return dist[y][x] distance-to-target, or INF if blocked/unreachable
     */

    public int[][] compute(boolean[][] obstacles, List<Pos> targets)
    {
        int h = obstacles.length;       // rows
        int w = obstacles[0].length;    // columns

        // Initialise all distances to INF
        int[][] dist = new int[h][w];
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                dist[y][x] = INF;
            }
        }

        //BFS queue
        Deque<Pos> q = new ArrayDeque<>();

        // Start BFS from all targets (multi-source BFS)
        for (Pos t : targets)
        {
            if (inBounds(t.x, t.y, w, h) && !obstacles[t.y][t.x])
            {
                dist[t.y][t.x] = 0;
                q.addLast(t);
            }
        }

        // 4-neighbourhood (right, left, up, down)
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        while (!q.isEmpty())
        {
            Pos cur = q.removeFirst();
            int base = dist[cur.y][cur.x];

            for (int i = 0; i < 4; i++)
            {
                int nx = cur.x + dx[i];
                int ny = cur.y + dy[i];

                if (!inBounds(nx, ny, w, h)) continue;  // outside map
                if (obstacles[ny][nx]) continue;        // blocked cell

                int nd = base + 1;
                if (nd < dist[ny][nx])                  // found a shorter path
                {
                    dist[ny][nx] = nd;
                    q.addLast(new Pos(nx, ny));
                }
            }
        }

        // Ensure obstacles stay INF (clarity)
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                if (obstacles[y][x]) dist[y][x] = INF;
            }
        }

        return dist;
    }

    private boolean inBounds(int x, int y, int w, int h)
    {
        return x >= 0 && x < w && y >= 0 && y < h;
    }
}
