package org.carrot2.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

/**
 * Various utility methods for working with {@link File}s.
 */
public class FileUtils
{
    /**
     * Creates a new {@link File} from the provided path and executes
     * {@link #getCanonicalOrAbsoluteFile(File)}.
     */
    public static File getCanonicalOrAbsoluteFile(String path)
    {
        File file = new File(path);
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
    public static String changeRoot(String file, String oldRoot, String newRoot)
    {
        // File is assumed to be a subpath of oldRoot, so PathUtils.getRelativeFilePath()
        // shouldn't return null here.
        final String relativePath = PathUtils.getRelativeFilePath(oldRoot, file);
        return FilenameUtils.concat(newRoot, relativePath);
    }
}
