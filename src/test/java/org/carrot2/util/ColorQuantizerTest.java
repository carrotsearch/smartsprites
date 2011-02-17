package org.carrot2.util;

import static org.fest.assertions.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.junit.Test;

import amd.Quantize;

/**
 * Test cases for {@link Quantize}.
 */
public class ColorQuantizerTest extends BufferedImageTestBase
{
    @Test
    public void testOneColor() throws IOException
    {
        final String fileName = "one-color.png";
        final BufferedImage quantized = ColorQuantizer.quantize(image(fileName));
        org.carrot2.labs.test.Assertions.assertThat(quantized).doesNotHaveAlpha()
            .hasNumberOfColorsEqualTo(1).isIndexedColor();
    }

    @Test
    public void testNoAlpha() throws IOException
    {
        final String fileName = "no-alpha.png";
        final BufferedImage quantized = ColorQuantizer.quantize(image(fileName));
        org.carrot2.labs.test.Assertions.assertThat(quantized).doesNotHaveAlpha()
            .hasNumberOfColorsEqualTo(4).isIndexedColor();
    }

    @Test
    public void testBitAlpha() throws IOException
    {
        final String fileName = "bit-alpha.png";
        final BufferedImage quantized = ColorQuantizer.quantize(image(fileName));
        org.carrot2.labs.test.Assertions.assertThat(quantized).hasBitAlpha()
            .hasNumberOfColorsEqualTo(1).isIndexedColor();
    }

    @Test
    public void testFullAlpha() throws IOException
    {
        final String fileName = "full-alpha.png";
        final BufferedImage quantized = ColorQuantizer.quantize(image(fileName));
        org.carrot2.labs.test.Assertions.assertThat(quantized).hasBitAlpha()
            .hasNumberOfColorsEqualTo(3).isIndexedColor();
    }

    @Test
    public void testExactColorsQuantize() throws IOException
    {
        final String fileName = "exact-colors.png";
        final BufferedImage quantized = ColorQuantizer.quantize(image(fileName));
        org.carrot2.labs.test.Assertions.assertThat(quantized).hasBitAlpha()
            .hasNumberOfColorsEqualTo(16).isIndexedColor();
    }

    @Test
    public void testExactColorsReduce() throws IOException
    {
        final String fileName = "exact-colors.png";
        final BufferedImage quantized = ColorQuantizer.reduce(image(fileName));
        org.carrot2.labs.test.Assertions.assertThat(quantized).hasBitAlpha()
            .hasNumberOfColorsEqualTo(255).isIndexedColor();
    }

    @Test
    public void testManyColorsQuantize() throws IOException
    {
        final String fileName = "many-colors.png";
        final BufferedImage quantized = ColorQuantizer.quantize(image(fileName));
        org.carrot2.labs.test.Assertions.assertThat(quantized).doesNotHaveAlpha()
            .hasNumberOfColorsEqualTo(61).isIndexedColor();
        // Current quantizer is far from perfect
    }

    @Test(expected = IllegalArgumentException.class)
    public void testManyColorsReduce() throws IOException
    {
        final String fileName = "many-colors.png";
        ColorQuantizer.reduce(image(fileName));
    }

    @Test
    public void testCanReduceWithoutDataLoss() throws IOException
    {
        checkDataLoss("bit-alpha.png", true);
        checkDataLoss("exact-colors.png", true);
        checkDataLoss("full-alpha.png", false);
        checkDataLoss("many-colors.png", false);
        checkDataLoss("no-alpha.png", true);
        checkDataLoss("one-color.png", true);
    }

    private void checkDataLoss(String path, boolean expectedCanReduce) throws IOException
    {
        assertThat(
            ColorQuantizer.getColorReductionInfo(image(path))
                .canReduceWithoutQualityLoss()).isEqualTo(expectedCanReduce);
    }
}
