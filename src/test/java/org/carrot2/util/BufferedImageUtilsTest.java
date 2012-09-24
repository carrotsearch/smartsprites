package org.carrot2.util;

import static org.fest.assertions.Assertions.assertThat;

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
        assertThat(
            BufferedImageUtils.hasTransparency(image("full-alpha.png")))
            .isTrue();
    }

    @Test
    public void testHasAlphaTransparencyBitmask() throws IOException
    {
        assertThat(
            BufferedImageUtils.hasTransparency(image("bit-alpha.png")))
            .isTrue();
    }

    @Test
    public void testHasAlphaTransparencyNoTransparency() throws IOException
    {
        assertThat(
            BufferedImageUtils.hasTransparency(image("no-alpha.png")))
            .isFalse();
    }

    @Test
    public void testHasPartialAlphaTransparencyPartial() throws IOException
    {
        assertThat(
            BufferedImageUtils
                .hasPartialTransparency(image("full-alpha.png")))
            .isTrue();
    }

    @Test
    public void testHasPartialAlphaTransparencyBitmask() throws IOException
    {
        assertThat(
            BufferedImageUtils
                .hasPartialTransparency(image("bit-alpha.png")))
            .isFalse();
    }

    @Test
    public void testHasPartialAlphaTransparencyNoTransparency() throws IOException
    {
        assertThat(
            BufferedImageUtils
                .hasPartialTransparency(image("no-alpha.png"))).isFalse();
    }

    @Test
    public void testCountDistinctColorsTransparency() throws IOException
    {
        assertThat(
            BufferedImageUtils
                .countDistictColors(image("full-alpha.png")))
            .isEqualTo(1);
    }

    @Test
    public void testCountDistinctColorsTransparencyMatted() throws IOException
    {
        assertThat(
            BufferedImageUtils.countDistictColors(BufferedImageUtils.matte(
                image("full-alpha.png"), Color.WHITE))).isEqualTo(4);
    }

    @Test
    public void testCountDistinctColorsNoTransparency() throws IOException
    {
        assertThat(
            BufferedImageUtils.countDistictColors(image("no-alpha.png")))
            .isEqualTo(4);
    }

    @Test
    public void testCountDistinctColorsGradient() throws IOException
    {
        assertThat(
            BufferedImageUtils
                .countDistictColors(image("many-colors.png"))).isEqualTo(
            1021); // black is the same in all bands
    }
}
