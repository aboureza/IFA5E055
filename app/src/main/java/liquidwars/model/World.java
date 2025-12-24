package liquidwars.model;

/**
 * Represents the whole grid at a given instant.
 *
 * Conventions:
 * - x = column
 * - y = row
 * - arrays are indexed [y][x]
 *
 * A cell can contain:
 * - a wall (obstacle) OR
 * - a particle OR
 * - be empty
 */

public final class World {

    private final int width;
    private final int height;

    private final boolean[][] walls;        // walls[y][x]
    private final Particle[][] particles;   // particles[y][x] (null = empty)

    public World(boolean[][] walls, Particle[][] particles) 
    {
        if (walls == null || walls.length == 0 || walls[0].length == 0) 
        {
            throw new IllegalArgumentException("walls must be a non-empty 2D array");
        }

        this.height = walls.length;
        this.width = walls[0].length; // FIX: width is number of columns

        // Validate rectangle
        for (int y = 0; y < height; y++) 
        {
            if (walls[y] == null || walls[y].length != width) 
            {
                throw new IllegalArgumentException("walls must be rectangular");
            }
        }

        if (particles == null || particles.length != height) 
        {
            throw new IllegalArgumentException("particles must match walls height");
        }
        for (int y = 0; y < height; y++) 
        {
            if (particles[y] == null || particles[y].length != width) 
            {
                throw new IllegalArgumentException("particles must match walls width");
            }
        }

        // Defensive copy
        this.walls = new boolean[height][width];
        this.particles = new Particle[height][width];

        for (int y = 0; y < height; y++) 
        {
            System.arraycopy(walls[y], 0, this.walls[y], 0, width);
            System.arraycopy(particles[y], 0, this.particles[y], 0, width);
        }
    }

    public int width() { return width; }
    public int height() { return height; }

    public boolean inBounds(int x, int y) 
    {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public boolean isWall(int x, int y) 
    {
        return walls[y][x];
    }

    public Particle get(int x, int y) 
    {
        return particles[y][x];
    }

    public void set(int x, int y, Particle p) 
    {
        particles[y][x] = p;
    }

    /** Deep copy of the grid state. */
    public World copy() 
    {
        return new World(this.walls, this.particles);
    }

    public int particleCount() 
    {
        int count = 0;
        for (int y = 0; y < height; y++) 
        {
            for (int x = 0; x < width; x++) 
            {
                if (particles[y][x] != null) count++;
            }
        }
        return count;
    }

    public int totalEnergy() 
    {
        int sum = 0;
        for (int y = 0; y < height; y++) 
        {
            for (int x = 0; x < width; x++) 
            {
                Particle p = particles[y][x];
                if (p != null) sum += p.energy();
            }
        }
        return sum;
    }
}
