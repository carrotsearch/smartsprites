package org.carrot2.labs.test;

import static org.fest.assertions.Assertions.assertThat;

import java.awt.image.BufferedImage;

import org.carrot2.labs.smartsprites.css.CssProperty;
import org.carrot2.util.BufferedImageUtils;

/**
 * Assertions on instances of {@link CssProperty}.
 */
public class BufferedImageAssertion
{
    /** The actual property */
    private final BufferedImage actual;

    /** Assertion description */
    private String description = "image";

    /**
     * Creates a {@link BufferedImage} assertion.
     */
    public BufferedImageAssertion(BufferedImage actual)
    {
        this.actual = actual;
    }

    /**
     * Asserts that the image is an indexed color image.
     */
    public BufferedImageAssertion isIndexedColor()
    {
        assertThat(isIndexed()).as(description + ".indexed").isTrue();
        return this;
    }

    /**
     * Asserts that the image is a direct color image.
     */
    public BufferedImageAssertion isDirectColor()
    {
        assertThat(!isIndexed()).as(description + ".direct").isTrue();
        return this;
    }

    private boolean isIndexed()
    {
        return actual.getType() == BufferedImage.TYPE_BYTE_INDEXED
            || actual.getType() == BufferedImage.TYPE_BYTE_BINARY;
    }

    /**
     * Asserts that the image has bit (0/1) alpha areas.
     */
    public BufferedImageAssertion hasBitAlpha()
    {
        final int [][] rgb = BufferedImageUtils.getRgb(actual);
        int width = actual.getWidth();
        int height = actual.getHeight();
        boolean hasBitAlpha = false;

        exit: for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                final int alpha = (rgb[x][y] & 0xff000000) >> 24;
                if (alpha == 0)
                {
                    hasBitAlpha = true;
                }

                if (alpha > 0 && alpha != 255)
                {
                    hasBitAlpha = false;
                    break exit;
                }
            }
        }

        assertThat(hasBitAlpha).as(description + ".hasBitAlpha").isTrue();
        return this;
    }

    /**
     * Asserts that the image has true (0..1) alpha areas.
     */
    public BufferedImageAssertion hasTrueAlpha()
    {
        final int [][] rgb = BufferedImageUtils.getRgb(actual);
        int width = actual.getWidth();
        int height = actual.getHeight();
        boolean hasTrueAlpha = false;

        exit: for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                final int alpha = (rgb[x][y] & 0xff000000) >> 24;
                if (alpha > 0 && alpha < 255)
                {
                    hasTrueAlpha = true;
                    break exit;
                }
            }
        }

        assertThat(hasTrueAlpha).as(description + ".hasTrueAlpha").isTrue();
        return this;
    }

    /**
     * Asserts that the image has or doesn't have any transparent areas.
     */
    public BufferedImageAssertion doesNotHaveAlpha()
    {
        final int [][] rgb = BufferedImageUtils.getRgb(actual);
        int width = actual.getWidth();
        int height = actual.getHeight();
        boolean hasAlpha = false;

        exit: for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                if ((rgb[x][y] & 0xff000000) != 0xff000000)
                {
                    hasAlpha = true;
                    break exit;
                }
            }
        }

        assertThat(hasAlpha).as(description + ".hasAlpha").isFalse();
        return this;
    }

    /**
     * Asserts that the image has the specified number of colors, fully transparent pixels
     * are not counted.
     */
    public BufferedImageAssertion hasNumberOfColorsEqualTo(int colors)
    {
        assertThat(BufferedImageUtils.countDistictColors(actual)).as(
            description + ".colors").isEqualTo(colors);
        return this;
    }

    public BufferedImageAssertion isEqualTo(BufferedImage expected)
    {
        assertThat(actual).isEqualTo(expected);
        return this;
    }

    public BufferedImageAssertion isNotEqualTo(BufferedImage expected)
    {
        assertThat(actual).isNotEqualTo(expected);
        return this;
    }

    public BufferedImageAssertion as(String description)
    {
        this.description = description;
        return this;
    }
}
