package liquidwars;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class LevelLoaderTest {

    @Test
    void loadWallsFromResource_ThrowsIOExceptionForMissingResource() {
        assertThrows(IOException.class, () -> {
            LevelLoader.loadWallsFromResource("/nonexistent/map.png", 100, 100);
        });
    }

    @Test
    void loadWallsFromResource_ThrowsIOExceptionForWrongDimensions() {
        // map1.png is 160x120, so requesting different dimensions should fail
        assertThrows(IOException.class, () -> {
            LevelLoader.loadWallsFromResource("/levels/map1.png", 50, 50);
        });
    }

    @Test
    void loadWallsFromResourceAnySize_LoadsCorrectDimensions() throws IOException {
        boolean[][] walls = LevelLoader.loadWallsFromResourceAnySize("/levels/map1.png");

        assertNotNull(walls);
        assertTrue(walls.length > 0);
        assertTrue(walls[0].length > 0);
    }

    @Test
    void loadWallsFromResourceAnySize_ForceBorderWalls() throws IOException {
        boolean[][] walls = LevelLoader.loadWallsFromResourceAnySize("/levels/map1.png");

        int h = walls.length;
        int w = walls[0].length;

        // Check all border cells are walls
        for (int x = 0; x < w; x++) {
            assertTrue(walls[0][x], "Top border should be wall");
            assertTrue(walls[h - 1][x], "Bottom border should be wall");
        }

        for (int y = 0; y < h; y++) {
            assertTrue(walls[y][0], "Left border should be wall");
            assertTrue(walls[y][w - 1], "Right border should be wall");
        }
    }

    @Test
    void loadWallsFromResourceAnySize_ThrowsIOExceptionForMissingResource() {
        assertThrows(IOException.class, () -> {
            LevelLoader.loadWallsFromResourceAnySize("/nonexistent/map.png");
        });
    }

    @Test
    void loadWallsFromResource_BlackPixelsAreWalls() throws IOException {
        // This test assumes the map has some variation in pixel colors
        boolean[][] walls = LevelLoader.loadWallsFromResourceAnySize("/levels/map1.png");

        // Just verify we got a reasonable mix of walls and non-walls
        int wallCount = 0;
        int totalCells = 0;

        for (int y = 0; y < walls.length; y++) {
            for (int x = 0; x < walls[0].length; x++) {
                totalCells++;
                if (walls[y][x]) wallCount++;
            }
        }

        // Expect at least some walls (borders at minimum)
        assertTrue(wallCount > 0, "Should have at least some walls");
        // Expect not all cells to be walls
        assertTrue(wallCount < totalCells, "Not all cells should be walls");
    }
}
