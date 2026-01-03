package liquidwars;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public final class LevelLoader {

    public static boolean[][] loadWallsFromResource(String resourcePath, int w, int h) throws IOException
    {
        BufferedImage img;

        try (InputStream in = LevelLoader.class.getResourceAsStream(resourcePath))
        {
            if (in == null) throw new IOException("Missing resource: " + resourcePath);
            img = ImageIO.read(in);
        }

        if (img == null) throw new IOException("Could not decode image: " + resourcePath);
        if (img.getWidth() != w || img.getHeight() != h)
        {
            throw new IOException("Level image must be " + w + "x" + h +
                " but was " + img.getWidth() + "x" + img.getHeight());
        }

        boolean[][] walls = new boolean[h][w];

        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                int rgb = img.getRGB(x, y);

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb) & 0xFF;

                // black-ish pixel => wall
                walls[y][x] = (r + g + b) / 3 < 128;
            }
        }

        // force border walls so nobody hugs the edge
        for (int x = 0; x < w; x++) { walls[0][x] = true; walls[h - 1][x] = true; }
        for (int y = 0; y < h; y++) { walls[y][0] = true; walls[y][w - 1] = true; }

        return walls;
    }
}
