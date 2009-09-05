package org.carrot2.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.Lists;

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

    /**
     * Attempts to delete the provided filesand throws an {@link IOException} in case
     * {@link File#delete()} returns <code>false</code> for any of them.
     */
    public static void deleteThrowingExceptions(File... files) throws IOException
    {
        if (files == null)
        {
            return;
        }

        final ArrayList<String> undeletedFiles = Lists.newArrayList();
        for (File file : files)
        {
            if (file == null)
            {
                continue;
            }

            if (!file.delete())
            {
                undeletedFiles.add(file.getPath());
            }
        }

        if (!undeletedFiles.isEmpty())
        {
            throw new IOException("Unable to delete files: " + undeletedFiles.toString());
        }
    }

    /**
     * Calls {@link File#mkdirs()} on the provided argument and throws an
     * {@link IOException} if the call returns <code>false</code>.
     */
    public static void mkdirsThrowingExceptions(File dirs) throws IOException
    {
        if (dirs.exists())
        {
            return;
        }
        
        if (!dirs.mkdirs())
        {
            throw new IOException("Unable to create directories: " + dirs.getPath());
        }
    }

    /**
     * Returns <code>true</code> if file is contained in the parent directory or any
     * parent of the parent directory.
     */
    public static boolean isFileInParent(File file, File parent)
    {
        final File fileParent = file.getParentFile();
        if (fileParent == null)
        {
            return false;
        }

        if (fileParent.equals(parent))
        {
            return true;
        }

        return isFileInParent(fileParent, parent);
    }
}
