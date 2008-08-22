package org.carrot2.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Base class for tests involving {@link BufferedImage}s.
 */
public class BufferedImageTestBase
{

    protected BufferedImage image(String fileName) throws IOException
    {
        return ImageIO.read(new File(fileName));
    }

}
