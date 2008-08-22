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
        final String fileName = "src-test/images/one-color.png";
        final BufferedImage quantized = ColorQuantizer.quantize(image(fileName));
        org.carrot2.labs.test.Assertions.assertThat(quantized).doesNotHaveAlpha()
            .hasNumberOfColorsEqualTo(1).isIndexedColor();
    }

    @Test
    public void testNoAlpha() throws IOException
    {
        final String fileName = "src-test/images/no-alpha.png";
        final BufferedImage quantized = ColorQuantizer.quantize(image(fileName));
        org.carrot2.labs.test.Assertions.assertThat(quantized).doesNotHaveAlpha()
            .hasNumberOfColorsEqualTo(4).isIndexedColor();
    }

    @Test
    public void testBitAlpha() throws IOException
    {
        final String fileName = "src-test/images/bit-alpha.png";
        final BufferedImage quantized = ColorQuantizer.quantize(image(fileName));
        org.carrot2.labs.test.Assertions.assertThat(quantized).hasBitAlpha()
            .hasNumberOfColorsEqualTo(1).isIndexedColor();
    }

    @Test
    public void testFullAlpha() throws IOException
    {
        final String fileName = "src-test/images/full-alpha.png";
        final BufferedImage quantized = ColorQuantizer.quantize(image(fileName));
        org.carrot2.labs.test.Assertions.assertThat(quantized).hasBitAlpha()
            .hasNumberOfColorsEqualTo(3).isIndexedColor();
    }

    @Test
    public void testExactColorsQuantize() throws IOException
    {
        final String fileName = "src-test/images/exact-colors.png";
        final BufferedImage quantized = ColorQuantizer.quantize(image(fileName));
        org.carrot2.labs.test.Assertions.assertThat(quantized).hasBitAlpha()
            .hasNumberOfColorsEqualTo(16).isIndexedColor();
    }

    @Test
    public void testExactColorsReduce() throws IOException
    {
        final String fileName = "src-test/images/exact-colors.png";
        final BufferedImage quantized = ColorQuantizer.reduce(image(fileName));
        org.carrot2.labs.test.Assertions.assertThat(quantized).hasBitAlpha()
            .hasNumberOfColorsEqualTo(255).isIndexedColor();
    }

    @Test
    public void testManyColorsQuantize() throws IOException
    {
        final String fileName = "src-test/images/many-colors.png";
        final BufferedImage quantized = ColorQuantizer.quantize(image(fileName));
        org.carrot2.labs.test.Assertions.assertThat(quantized).doesNotHaveAlpha()
            .hasNumberOfColorsEqualTo(61).isIndexedColor();
        // Current quantizer is far from perfect
    }

    @Test(expected = IllegalArgumentException.class)
    public void testManyColorsReduce() throws IOException
    {
        final String fileName = "src-test/images/many-colors.png";
        ColorQuantizer.reduce(image(fileName));
    }

    @Test
    public void testCanReduceWithoutDataLoss() throws IOException
    {
        checkDataLoss("src-test/images/bit-alpha.png", true);
        checkDataLoss("src-test/images/exact-colors.png", true);
        checkDataLoss("src-test/images/full-alpha.png", false);
        checkDataLoss("src-test/images/many-colors.png", false);
        checkDataLoss("src-test/images/no-alpha.png", true);
        checkDataLoss("src-test/images/one-color.png", true);
    }

    private void checkDataLoss(String path, boolean expectedCanReduce) throws IOException
    {
        assertThat(
            ColorQuantizer.getColorReductionInfo(image(path))
                .canReduceWithoutQualityLoss()).isEqualTo(expectedCanReduce);
    }
}
