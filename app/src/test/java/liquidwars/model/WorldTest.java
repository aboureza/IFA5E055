package liquidwars.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {

    @Test
    void constructorSetsWidthAndHeight() {
        boolean[][] walls = {
                { false, true,  false },
                { false, false, false }
        };
        Particle[][] particles = {
                { null, null, null },
                { null, null, null }
        };

        World w = new World(walls, particles);
        assertEquals(3, w.width());
        assertEquals(2, w.height());
    }

    @Test
    void inBoundsWorks() {
        World w = new World(
                new boolean[][] { { false, false }, { false, false } },
                new Particle[][] { { null, null }, { null, null } }
        );

        assertTrue(w.inBounds(0, 0));
        assertTrue(w.inBounds(1, 1));
        assertFalse(w.inBounds(-1, 0));
        assertFalse(w.inBounds(0, -1));
        assertFalse(w.inBounds(2, 0));
        assertFalse(w.inBounds(0, 2));
    }

    @Test
    void isWallAndGetAndSetWork() {
        boolean[][] walls = {
                { false, true },
                { false, false }
        };
        Particle[][] particles = {
                { null, null },
                { null, null }
        };

        World w = new World(walls, particles);

        assertFalse(w.isWall(0, 0));
        assertTrue(w.isWall(1, 0));

        assertNull(w.get(0, 0));
        Particle p = new Particle(0, 5);
        w.set(0, 0, p);
        assertEquals(p, w.get(0, 0));
    }

    @Test
    void particleCountAndTotalEnergyAreCorrect() {
        boolean[][] walls = {
                { false, false, false },
                { false, false, false }
        };
        Particle[][] particles = {
                { new Particle(0, 5), null, new Particle(1, 7) },
                { null, null, null }
        };

        World w = new World(walls, particles);
        assertEquals(2, w.particleCount());
        assertEquals(12, w.totalEnergy());
    }

    @Test
    void constructorMakesDefensiveCopiesOfInputArrays() {
        boolean[][] walls = {
                { false, false },
                { false, false }
        };
        Particle[][] particles = {
                { null, null },
                { null, null }
        };

        World w = new World(walls, particles);

        // mutate the arrays we passed in AFTER construction
        walls[0][0] = true;
        particles[0][0] = new Particle(9, 9);

        // world should NOT change because it copied defensively
        assertFalse(w.isWall(0, 0));
        assertNull(w.get(0, 0));
    }

    @Test
    void copyProducesIndependentWorldState() {
        boolean[][] walls = {
                { false, false },
                { false, false }
        };
        Particle[][] particles = {
                { new Particle(0, 1), null },
                { null, new Particle(1, 2) }
        };

        World original = new World(walls, particles);
        World copy = original.copy();

        // sanity: same visible contents initially
        assertEquals(original.width(), copy.width());
        assertEquals(original.height(), copy.height());
        assertEquals(original.particleCount(), copy.particleCount());
        assertEquals(original.totalEnergy(), copy.totalEnergy());
        assertEquals(original.get(0, 0), copy.get(0, 0));
        assertEquals(original.get(1, 1), copy.get(1, 1));

        // mutate original; copy should not change
        original.set(0, 0, null);
        original.set(1, 0, new Particle(0, 100));

        assertEquals(new Particle(0, 1), copy.get(0, 0));   // unchanged
        assertNull(copy.get(1, 0));                         // unchanged

        // mutate copy; original should not change
        copy.set(1, 1, null);
        assertEquals(new Particle(1, 2), original.get(1, 1));
    }

    @Test
    void constructorRejectsNullOrEmptyWalls() {
        assertThrows(IllegalArgumentException.class, () -> new World(null, null));

        boolean[][] emptyWalls = new boolean[0][];
        assertThrows(IllegalArgumentException.class, () -> new World(emptyWalls, null));

        boolean[][] badWalls = new boolean[][] { new boolean[0] };
        assertThrows(IllegalArgumentException.class, () -> new World(badWalls, null));
    }

    @Test
    void constructorRejectsNonRectangularWalls() {
        boolean[][] walls = new boolean[][] {
                { false, false },
                { false } // different length
        };
        Particle[][] particles = new Particle[][] {
                { null, null },
                { null, null }
        };

        assertThrows(IllegalArgumentException.class, () -> new World(walls, particles));
    }

    @Test
    void constructorRejectsParticlesWithMismatchedDimensions() {
        boolean[][] walls = new boolean[][] {
                { false, false },
                { false, false }
        };

        // wrong height
        Particle[][] wrongHeight = new Particle[][] {
                { null, null }
        };
        assertThrows(IllegalArgumentException.class, () -> new World(walls, wrongHeight));

        // right height but wrong width
        Particle[][] wrongWidth = new Particle[][] {
                { null, null },
                { null }
        };
        assertThrows(IllegalArgumentException.class, () -> new World(walls, wrongWidth));
    }
}
