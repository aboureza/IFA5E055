package liquidwars.ui;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class ColourUtilTest {

    private static int r(int rgb) { return (rgb >> 16) & 0xFF; }
    private static int g(int rgb) { return (rgb >> 8) & 0xFF; }
    private static int b(int rgb) { return (rgb) & 0xFF; }

    @Test
    void particleRGB_ReturnsPackedRgbWithinRange() {
        int rgb = ColourUtil.particleRGB(0, 5);

        assertTrue(r(rgb) >= 0 && r(rgb) <= 255);
        assertTrue(g(rgb) >= 0 && g(rgb) <= 255);
        assertTrue(b(rgb) >= 0 && b(rgb) <= 255);

        // Ensure no alpha bits are used (should only be 0xRRGGBB)
        assertEquals(0, (rgb >> 24) & 0xFF);
    }

    @Test
    void particleRGB_EnergyBelowMin_ClampsToMinBrightness() {
        // energy < 0 => t = 0 => factor = 0.25
        int rgb = ColourUtil.particleRGB(0, -100);

        // TEAM0 base = (255,60,60)
        // factor = 0.25 => (64,15,15) after rounding
        assertEquals(64, r(rgb));
        assertEquals(15, g(rgb));
        assertEquals(15, b(rgb));
    }

    @Test
    void particleRGB_EnergyAboveMax_ClampsToFullBrightness() {
        // energy > 10 => t = 1 => factor = 1.0 (base colour)
        int rgb0 = ColourUtil.particleRGB(0, 999);
        int rgb1 = ColourUtil.particleRGB(1, 999);

        // TEAM0 base = (255,60,60)
        assertEquals(255, r(rgb0));
        assertEquals(60,  g(rgb0));
        assertEquals(60,  b(rgb0));

        // TEAM1 base = (60,120,255)
        assertEquals(60,  r(rgb1));
        assertEquals(120, g(rgb1));
        assertEquals(255, b(rgb1));
    }

    @Test
    void particleRGB_Energy0_EqualsMinBrightnessFactor() {
        // energy = 0 => t=0 => factor=0.25
        int rgb = ColourUtil.particleRGB(1, 0);

        // TEAM1 base = (60,120,255)
        // factor = 0.25 => (15,30,64) after rounding
        assertEquals(15, r(rgb));
        assertEquals(30, g(rgb));
        assertEquals(64, b(rgb));
    }

    @Test
    void particleRGB_Energy10_EqualsBaseColour() {
        // energy = 10 => t=1 => factor=1.0
        int rgb = ColourUtil.particleRGB(1, 10);

        assertEquals(60,  r(rgb));
        assertEquals(120, g(rgb));
        assertEquals(255, b(rgb));
    }

    @Test
    void particleRGB_BrightnessIncreasesWithEnergy_SameTeam() {
        int low = ColourUtil.particleRGB(0, 0);
        int mid = ColourUtil.particleRGB(0, 5);
        int high = ColourUtil.particleRGB(0, 10);

        // Since factor increases with energy, each channel should be non-decreasing
        assertTrue(r(low) <= r(mid) && r(mid) <= r(high));
        assertTrue(g(low) <= g(mid) && g(mid) <= g(high));
        assertTrue(b(low) <= b(mid) && b(mid) <= b(high));
    }

    @Test
    void particleRGB_TeamsProduceDifferentColours() {
        int rgbTeam0 = ColourUtil.particleRGB(0, 10);
        int rgbTeam1 = ColourUtil.particleRGB(1, 10);

        assertNotEquals(rgbTeam0, rgbTeam1);
    }

    @Test
    void clamp255_WorksViaReflection() throws Exception {
        Method m = ColourUtil.class.getDeclaredMethod("clamp255", int.class);
        m.setAccessible(true);

        assertEquals(0,   (int) m.invoke(null, -50));
        assertEquals(255, (int) m.invoke(null, 999));
        assertEquals(123, (int) m.invoke(null, 123));
    }
}
