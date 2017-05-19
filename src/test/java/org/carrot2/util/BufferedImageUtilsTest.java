package org.carrot2.util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.io.IOException;

import org.junit.Test;

/**
 * Test cases for {@link BufferedImageUtils}.
 */
public class BufferedImageUtilsTest extends BufferedImageTestBase
{
    @Test
    public void testHasAlphaTransparencyPartial() throws IOException
    {
        assertTrue(
            BufferedImageUtils.hasTransparency(image("full-alpha.png")));
    }

    @Test
    public void testHasAlphaTransparencyBitmask() throws IOException
    {
        assertTrue(
            BufferedImageUtils.hasTransparency(image("bit-alpha.png")));
    }

    @Test
    public void testHasAlphaTransparencyNoTransparency() throws IOException
    {
        assertFalse(
            BufferedImageUtils.hasTransparency(image("no-alpha.png")));
    }

    @Test
    public void testHasPartialAlphaTransparencyPartial() throws IOException
    {
        assertTrue(
            BufferedImageUtils
                .hasPartialTransparency(image("full-alpha.png")));
    }

    @Test
    public void testHasPartialAlphaTransparencyBitmask() throws IOException
    {
        assertFalse(
            BufferedImageUtils
                .hasPartialTransparency(image("bit-alpha.png")));
    }

    @Test
    public void testHasPartialAlphaTransparencyNoTransparency() throws IOException
    {
        assertFalse(
            BufferedImageUtils
                .hasPartialTransparency(image("no-alpha.png")));
    }

    @Test
    public void testCountDistinctColorsTransparency() throws IOException
    {
        assertEquals(1, 
            BufferedImageUtils
                .countDistinctColors(image("full-alpha.png")));
    }

    @Test
    public void testCountDistinctColorsTransparencyMatted() throws IOException
    {
        assertEquals(4,
            BufferedImageUtils.countDistinctColors(BufferedImageUtils.matte(
                image("full-alpha.png"), Color.WHITE)));
    }

    @Test
    public void testCountDistinctColorsNoTransparency() throws IOException
    {
        assertEquals(4,
            BufferedImageUtils.countDistinctColors(image("no-alpha.png")));
    }

    @Test
    public void testCountDistinctColorsGradient() throws IOException
    {
        // black is the same in all bands
        assertEquals(1021,
            BufferedImageUtils
                .countDistinctColors(image("many-colors.png")));
    }
}
