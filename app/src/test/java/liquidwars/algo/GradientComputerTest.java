package liquidwars.algo;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

public class GradientComputerTest {

    @Test
    void targetCellIsZero() {
        boolean[][] obstacles = new boolean[3][3]; // all false
        GradientComputer gc = new GradientComputer();

        int[][] dist = gc.compute(obstacles, List.of(new GradientComputer.Pos(1, 1)));

        // dist[y][x]
        assertEquals(0, dist[1][1]);
    }

    @Test
    void distancesGrowInLayersFromTarget() {
        boolean[][] obstacles = new boolean[3][3]; // all false
        GradientComputer gc = new GradientComputer();

        int[][] dist = gc.compute(obstacles, List.of(new GradientComputer.Pos(1, 1)));

        // Direct neighbors should be 1 step away
        assertEquals(1, dist[1][0]); // left
        assertEquals(1, dist[1][2]); // right
        assertEquals(1, dist[0][1]); // up
        assertEquals(1, dist[2][1]); // down

        // Corner is 2 steps away from center
        assertEquals(2, dist[0][0]);
        assertEquals(2, dist[0][2]);
        assertEquals(2, dist[2][0]);
        assertEquals(2, dist[2][2]);
    }

    @Test
    void obstaclesStayInfiniteAndPathsGoAround() {
        boolean[][] obstacles = new boolean[5][5];

        // Wall at x=2 except a gap at (2,2)
        obstacles[0][2] = true;
        obstacles[1][2] = true;
        obstacles[3][2] = true;
        obstacles[4][2] = true;
        // obstacles[2][2] is false (gap)

        GradientComputer gc = new GradientComputer();
        int[][] dist = gc.compute(obstacles, List.of(new GradientComputer.Pos(0, 0)));

        // Obstacles are INF
        assertEquals(GradientComputer.INF, dist[0][2]);
        assertEquals(GradientComputer.INF, dist[1][2]);
        assertEquals(GradientComputer.INF, dist[3][2]);
        assertEquals(GradientComputer.INF, dist[4][2]);

        // Cell on far side should still be reachable via the gap
        assertTrue(dist[4][4] < GradientComputer.INF);
    }

    @Test
    void unreachableCellsRemainInfinite() {
        boolean[][] obstacles = new boolean[3][3];

        // Block off the target completely
        // Target at (1,1), block its 4 neighbors
        obstacles[1][0] = true;
        obstacles[1][2] = true;
        obstacles[0][1] = true;
        obstacles[2][1] = true;

        GradientComputer gc = new GradientComputer();
        int[][] dist = gc.compute(obstacles, List.of(new GradientComputer.Pos(1, 1)));

        // Target itself is still 0
        assertEquals(0, dist[1][1]);

        // Corners cannot be reached because all exits are blocked
        assertEquals(GradientComputer.INF, dist[0][0]);
        assertEquals(GradientComputer.INF, dist[0][2]);
        assertEquals(GradientComputer.INF, dist[2][0]);
        assertEquals(GradientComputer.INF, dist[2][2]);
    }

    @Test
    void multipleTargetsUsesNearestOne() {
        boolean[][] obstacles = new boolean[5][5]; // all false
        GradientComputer gc = new GradientComputer();

        // Two targets: one at left, one at right
        int[][] dist = gc.compute(obstacles, List.of(
                new GradientComputer.Pos(0, 2),
                new GradientComputer.Pos(4, 2)
        ));

        // Middle cell should be distance 2 from nearest target
        assertEquals(2, dist[2][2]);

        // A cell near the left should be closer to left target
        assertEquals(1, dist[2][1]);
        assertEquals(0, dist[2][0]);

        // A cell near the right should be closer to right target
        assertEquals(1, dist[2][3]);
        assertEquals(0, dist[2][4]);
    }
}
