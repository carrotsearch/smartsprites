package org.carrot2.util;

import java.io.File;
import java.io.IOException;

/**
 * @author Stanislaw Osinski
 */
public class FileUtils
{
    public static String getCanonicalOrAbsolutePath(File file)
    {
        try
        {
            return file.getCanonicalPath();
        }
        catch (final IOException e)
        {
            return file.getAbsolutePath();
        }
    }
}
