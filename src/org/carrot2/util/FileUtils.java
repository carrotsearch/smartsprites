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

    /**
     * Tries to execute {@link File#getCanonicalFile()} on the provided <code>file</code>,
     * if the code fails, returns the result of {@link File#getAbsoluteFile()}.
     */
    public static File getCanonicalOrAbsoluteFile(File file)
    {
        try
        {
            return file.getCanonicalFile();
        }
        catch (final IOException e)
        {
            return file.getAbsoluteFile();
        }
    }

    /**
     * Changes the root directory of a file. For example, file is /a/b/c/d/e and oldRoot
     * is /a/b/c, and newRoot is /x/y, the result will be /x/y/d/e.
     */
    public static File changeRoot(File file, File oldRoot, File newRoot)
    {
        if (!oldRoot.isDirectory())
        {
            throw new IllegalArgumentException("oldRoot must be a directory");
        }
        if (!newRoot.isDirectory())
        {
            throw new IllegalArgumentException("newRoot must be a directory");
        }
        if (file.equals(oldRoot))
        {
            throw new IllegalArgumentException("file must not be equal to oldRoot");
        }

        final File absoluteFile = getCanonicalOrAbsoluteFile(file);
        final File absoluteOldRoot = getCanonicalOrAbsoluteFile(oldRoot);

        final String filePath = absoluteFile.getPath();
        final String oldRootPath = absoluteOldRoot.getPath();

        if (!filePath.startsWith(oldRootPath))
        {
            throw new IllegalArgumentException("file must be contained in oldRoot");
        }

        return new File(newRoot, filePath.substring(oldRootPath.length() + 1));
    }
}
