package liquidwars.algo;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Improved gradient from initial 4-neighbours to 8 neighbours now, (BFS replaced with Dijktra algorithm)
 * - orthogonal cost = 10
 * - diagonal cost   = 14  (~ 10*sqrt(2))
 *
 * This is Dijkstra on a grid.
 *
 * Arrays are indexed as [y][x]:
 * - x = column
 * - y = row
 */
public final class MeshGradientComputer {

    public static final int INF = 1_000_000_000;

    private static final int COST_ORTHO = 10;
    private static final int COST_DIAG  = 14;

    // 8-neighbourhood: (dx, dy, cost)
    private static final int[] DX = { 1,-1, 0, 0,  1, 1,-1,-1 };
    private static final int[] DY = { 0, 0, 1,-1,  1,-1, 1,-1 };
    private static final int[] DC = { COST_ORTHO, COST_ORTHO, COST_ORTHO, COST_ORTHO,
                                      COST_DIAG,  COST_DIAG,  COST_DIAG,  COST_DIAG };

    // Position: x=col, y=row
    public record Pos(int x, int y) {}

    // Priority queue node for Dijkstra
    private record Node(int d, int x, int y) {}

    /**
     * @param obstacles obstacles[y][x] == true means cell blocked
     * @param targets list of target cells
     * @return dist[y][x] = minimum cost-to-target (10/14 units) or INF if unreachable
     */
    public int[][] compute(boolean[][] obstacles, List<Pos> targets) {
        int h = obstacles.length;
        int w = obstacles[0].length;

        int[][] dist = new int[h][w];

        // Init all cells to INF
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                dist[y][x] = INF;
            }
        }

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(Node::d));

        // Multi-source init: push all targets with distance 0
        for (Pos t : targets) {
            if (inBounds(t.x(), t.y(), w, h) && !obstacles[t.y()][t.x()]) {
                dist[t.y()][t.x()] = 0;
                pq.add(new Node(0, t.x(), t.y()));
            }
        }

        // Dijkstra loop
        while (!pq.isEmpty()) {
            Node cur = pq.poll();
            int d = cur.d();
            int x = cur.x();
            int y = cur.y();

            // Skip stale entries
            if (d != dist[y][x]) continue;

            for (int i = 0; i < 8; i++) {
                int nx = x + DX[i];
                int ny = y + DY[i];

                if (!inBounds(nx, ny, w, h)) continue;
                if (obstacles[ny][nx]) continue;

                int nd = d + DC[i];
                if (nd < dist[ny][nx]) {
                    dist[ny][nx] = nd;
                    pq.add(new Node(nd, nx, ny));
                }
            }
        }

        // Keep obstacles as INF (clarity)
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (obstacles[y][x]) dist[y][x] = INF;
            }
        }

        return dist;
    }

    private boolean inBounds(int x, int y, int w, int h) {
        return x >= 0 && x < w && y >= 0 && y < h;
    }
}
