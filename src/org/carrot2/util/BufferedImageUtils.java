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
    public static boolean hasFullAlphaTransparency(BufferedImage image)
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

    public static int countDistictColors(BufferedImage image)
    {
        return getDistictColors(image).length;
    }

    public static int [] getDistictColors(BufferedImage image)
    {
        return getDistictColors(image, 0);
    }
    
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
