package org.carrot2.util;

import java.io.File;
import java.io.IOException;

/**
 * Various utility methods for working with {@link File}s.
 */
public class FileUtils
{
    /**
     * Tries to execute {@link File#getCanonicalPath()} on the provided <code>file</code>,
     * if the code fails, returns the result of {@link File#getAbsolutePath()}.
     */
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
