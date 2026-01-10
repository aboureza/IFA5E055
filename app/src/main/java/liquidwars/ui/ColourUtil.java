package liquidwars.ui;

import java.awt.Color;

/**
 * This is a class used for converting (teamId, energy) into a display colour
 * 
 * Requirement:
 * - Each team has a base "saturated" colour
 * - the brightness will depend on energy
 * 
 * Implementation:
 * - Pick base colours for each team
 * - Convert energy into a brightness factor in [minBrightness..1.0]
 * - Scale RGB components by that factor
 */
public final class ColourUtil {
    
    private ColourUtil() {}

    // Match StepSimulator energy range
    private static final int ENERGY_MIN = 0;
    private static final int ENERGY_MAX = 10;

    // Base saturated colours per team
    private static final Color TEAM0 = new Color(255, 60, 60);   // red-ish
    private static final Color TEAM1 = new Color(60, 120, 255);  // blue-ish
    private static final Color TEAM2 = new Color(60, 255, 60);   // green-ish
    private static final Color TEAM3 = new Color(255, 255, 60);  // yellow-ish
    
    // Returns a packed RGB int (0xRRGGBB) for a particle
    public static int particleRGB(int teamId, int energy)
    {
        Color base = switch (teamId) {
            case 0 -> TEAM0;
            case 1 -> TEAM1;
            case 2 -> TEAM2;
            case 3 -> TEAM3;
            default -> TEAM0; // fallback to red
        };

        // Convert energy to 0..1
        double t = (energy - ENERGY_MIN) / (double) (ENERGY_MAX - ENERGY_MIN);
        if (t < 0) t = 0;
        if (t > 1) t = 1;

        //Following keeps particles visible at low energy
        double factor = 0.25 + 0.75 * t;

        int r = clamp255((int) Math.round(base.getRed() * factor));
        int g = clamp255((int) Math.round(base.getGreen() * factor));
        int b = clamp255((int) Math.round(base.getBlue() * factor));

        return (r << 16) | (g << 8) | b;
    }

    private static int clamp255(int v)
    {
        if (v < 0) return 0;
        if (v > 255) return 255;
        return v;
    }
}
