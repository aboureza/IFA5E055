package liquidwars.sim;

import liquidwars.model.Particle;
import liquidwars.model.World;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class StepSimulatorTest {

    @Test
    void movesIntoFreeMainDirection() {
        // 1 row, 3 cols: particle should move right (toward smaller gradient)
        boolean[][] walls = new boolean[1][3];
        Particle[][] parts = new Particle[1][3];
        parts[0][0] = new Particle(0, 5);

        World w = new World(walls, parts);

        int[][] g0 = new int[][] { { 2, 1, 0 } }; // dist[y][x]
        StepSimulator sim = new StepSimulator();

        World w2 = sim.step(w, Map.of(0, g0));

        assertNull(w2.get(0, 0));
        assertNotNull(w2.get(1, 0));
        assertEquals(0, w2.get(1, 0).teamId());
        assertEquals(5, w2.get(1, 0).energy());

        assertEquals(w.particleCount(), w2.particleCount());
        assertEquals(w.totalEnergy(), w2.totalEnergy());
    }

    @Test
    void ifMainBlockedButOtherGoodFree_thenMoveGoodNotTransfer() {
        // 2 rows, 3 cols
        boolean[][] walls = new boolean[2][3];
        Particle[][] parts = new Particle[2][3];

        // Actor at (1,1)
        parts[1][1] = new Particle(0, 5);

        // Friend blocks the MAIN direction at (2,1)
        parts[1][2] = new Particle(0, 0);

        World w = new World(walls, parts);

        // Gradient for team 0:
        // Actor at (1,1) has g=2
        // Right neighbour (2,1) has g=0  -> MAIN but occupied by friend (not free)
        // Up neighbour   (1,0) has g=1  -> GOOD and free (should move here, per rule #2)
        int[][] g0 = new int[][] {
                { 9, 1, 9 },
                { 9, 2, 0 }
        };

        StepSimulator sim = new StepSimulator();
        World w2 = sim.step(w, Map.of(0, g0));

        // Actor moved up to (1,0)
        assertNull(w2.get(1, 1));
        assertNotNull(w2.get(1, 0));

        // Friend still at (2,1)
        assertNotNull(w2.get(2, 1));

        assertEquals(w.particleCount(), w2.particleCount());
        assertEquals(w.totalEnergy(), w2.totalEnergy());
    }

    @Test
    void attacksEnemyInMainDirection_andCanConvertAtMinEnergy() {
        boolean[][] walls = new boolean[1][3];
        Particle[][] parts = new Particle[1][3];

        // Attacker team 0 at x=0
        parts[0][0] = new Particle(0, 5);

        // Enemy team 1 in MAIN direction at x=1 with low energy
        parts[0][1] = new Particle(1, 1);

        World w = new World(walls, parts);

        // Make x=1 the main direction (lower gradient)
        int[][] g0 = new int[][] { { 1, 0, 9 } };

        StepSimulator sim = new StepSimulator();
        World w2 = sim.step(w, Map.of(0, g0, 1, g0)); // team 1 gradient unused in this test

        // Positions unchanged
        assertNotNull(w2.get(0, 0));
        assertNotNull(w2.get(1, 0));

        // Enemy was attacked: energy 1 -> 0, converted to team 0
        assertEquals(0, w2.get(1, 0).teamId());
        assertEquals(0, w2.get(1, 0).energy());

        // Attacker gained 1 energy
        assertEquals(6, w2.get(0, 0).energy());

        assertEquals(w.particleCount(), w2.particleCount());
        assertEquals(w.totalEnergy(), w2.totalEnergy());
    }

    @Test
    void transfersEnergyToFriendInMainDirection() {
        boolean[][] walls = new boolean[1][3];
        Particle[][] parts = new Particle[1][3];

        // Donor at x=0
        parts[0][0] = new Particle(0, 5);

        // Friend in MAIN direction at x=1 with low energy
        parts[0][1] = new Particle(0, 0);

        World w = new World(walls, parts);

        int[][] g0 = new int[][] { { 1, 0, 9 } }; // x=1 is main

        StepSimulator sim = new StepSimulator();
        World w2 = sim.step(w, Map.of(0, g0));

        // No movement, just energy transfer
        assertEquals(4, w2.get(0, 0).energy());
        assertEquals(1, w2.get(1, 0).energy());

        assertEquals(w.particleCount(), w2.particleCount());
        assertEquals(w.totalEnergy(), w2.totalEnergy());
    }
}
