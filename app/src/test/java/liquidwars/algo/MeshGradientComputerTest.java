package liquidwars.algo;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class MeshGradientComputerTest {

    @Test
    void diagonalShouldBeCheaperThanTwoOrthogonalSteps() {
        boolean[][] obstacles = new boolean[3][3];
        MeshGradientComputer mg = new MeshGradientComputer();

        int[][] dist = mg.compute(obstacles, List.of(new MeshGradientComputer.Pos(1, 1)));

        // Diagonal (0,0) from (1,1) costs 14
        assertEquals(14, dist[0][0]);

        // Orthogonal neighbor costs 10
        assertEquals(10, dist[1][0]);
    }
}
