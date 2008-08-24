package org.carrot2.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Various utility methods for working with {@link BufferedImage}s.
 */
public class BufferedImageUtils
{
    /**
     * Returns <code>true</code> if the provided image has partially transparent areas
     * (alpha channel).
     */
    public static boolean hasPartialTransparency(BufferedImage image)
    {
        final Raster alphaRaster = image.getAlphaRaster();
        if (image.getTransparency() != Transparency.TRANSLUCENT || alphaRaster == null)
        {
            return false;
        }

        int [] pixels = alphaRaster.getPixels(0, 0, alphaRaster.getWidth(), alphaRaster
            .getHeight(), (int []) null);
        for (int i : pixels)
        {
            if (i != 0 && i != 255)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the number of distinct colors (excluding transparency) in the
     * <code>image</code>.
     */
    public static int countDistictColors(BufferedImage image)
    {
        return getDistictColors(image).length;
    }

    /**
     * Returns the <code>image</code>'s distinct colors in an RGB format, discarding
     * transparency information.
     */
    public static int [] getDistictColors(BufferedImage image)
    {
        return getDistictColors(image, 0);
    }

    /**
     * Returns the <code>image</code>'s distinct colors in an RGB format, discarding
     * transparency information. Adds <code>padding</code> empty slots at the beginning of
     * the returned array.
     */
    public static int [] getDistictColors(BufferedImage image, int padding)
    {
        final int width = image.getWidth();
        final int height = image.getHeight();

        final Set<Integer> colors = Sets.newHashSet();

        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                final int pixel = image.getRGB(x, y);

                // Count only colors for which alpha is not fully transparent
                if ((pixel & 0xff000000) != 0x00000000)
                {
                    colors.add(Integer.valueOf(pixel & 0x00ffffff));
                }
            }
        }

        final int [] colorMap = new int [colors.size() + padding];
        int index = padding;
        for (Integer color : colors)
        {
            colorMap[index++] = color;
        }

        return colorMap;
    }

    /**
     * Returns a two dimensional array of the <code>image</code>'s RGB values, including
     * transparency.
     */
    public static int [][] getRgb(BufferedImage image)
    {
        final int width = image.getWidth();
        final int height = image.getHeight();

        final int [][] rgb = new int [width] [height];

        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                rgb[x][y] = image.getRGB(x, y);
            }
        }

        return rgb;
    }

    /**
     * Performs matting of the <code>cource</code> image using <code>matteColor</code>.
     * Matting is rendering partial transparencies using solid color as if the original
     * image was put on top of a bitmap filled with <code>matteColor</code>.
     */
    public static BufferedImage matte(BufferedImage source, Color matteColor)
    {
        final int width = source.getWidth();
        final int height = source.getHeight();

        // A workaround for possibly different custom image types we can get:
        // draw a copy of the image
        final BufferedImage sourceConverted = new BufferedImage(width, height,
            BufferedImage.TYPE_4BYTE_ABGR);
        sourceConverted.getGraphics().drawImage(source, 0, 0, null);

        final BufferedImage matted = new BufferedImage(width, height,
            BufferedImage.TYPE_4BYTE_ABGR);

        final BufferedImage matte = new BufferedImage(width, height,
            BufferedImage.TYPE_4BYTE_ABGR);
        final int matteRgb = matteColor.getRGB();
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                matte.setRGB(x, y, matteRgb);
            }
        }

        CompositeContext context = AlphaComposite.DstOver.createContext(matte
            .getColorModel(), sourceConverted.getColorModel(), null);
        context.compose(matte.getRaster(), sourceConverted.getRaster(), matted
            .getRaster());

        return matted;
    }

    private BufferedImageUtils()
    {
    }
}
