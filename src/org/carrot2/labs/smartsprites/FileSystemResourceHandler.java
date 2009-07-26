package org.carrot2.labs.smartsprites;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;

import org.apache.commons.io.FilenameUtils;
import org.carrot2.labs.smartsprites.message.Message;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.util.FileUtils;

/**
 * This class defines the resource handler which manage resources from the file system.
 * 
 * @author Ibrahim Chaehoi
 */
public class FileSystemResourceHandler implements ResourceHandler
{
    /** The message log */
    private final MessageLog messageLog;

    /** The root directory */
    private final File rootDir;

    /**
     * Constructor.
     * 
     * @param rootDir the root directory
     * @param messageLog the message log
     */
    public FileSystemResourceHandler(File rootDir, MessageLog messageLog)
    {
        this.rootDir = rootDir;
        this.messageLog = messageLog;
    }

    public InputStream getResourceAsStream(String cssFile, String imagePath)
    {
        File file = getResourceFile(cssFile, imagePath);
        InputStream is = null;
        try
        {
            is = new FileInputStream(file);
        }
        catch (FileNotFoundException e)
        {
            messageLog.info(Message.MessageType.GENERIC, e.getMessage());
        }

        return is;
    }

    /**
     * Retrieves the resource file from a base file.
     * 
     * @param baseFile the base file.
     * @param filePath the file path, which can be relative to the base file or relative
     *            to the root directory if it starts with a "/"
     * @return the resource file.
     */
    private File getResourceFile(String baseFile, String filePath)
    {
        File file = null;
        if (filePath.startsWith("/"))
        {
            if (rootDir != null)
            {
                file = new File(FileUtils.getCanonicalOrAbsoluteFile(rootDir), filePath
                    .substring(1));
            }
            else
            {
                messageLog.warning(MessageType.ABSOLUTE_PATH_AND_NO_DOCUMENT_ROOT,
                    filePath);
            }
        }
        else
        {
            String parentPath = "";
            int idx = FilenameUtils.indexOfLastSeparator(baseFile);
            if (idx != -1)
            {
                parentPath = baseFile.substring(0, idx);
            }
            file = new File(FilenameUtils.concat(parentPath, filePath));
        }
        return file;
    }

    public Reader getReader(String path)
    {
        Reader rd = null;
        try
        {
            rd = new FileReader(new File(path));
        }
        catch (FileNotFoundException e)
        {
            messageLog.info(Message.MessageType.GENERIC, e.getMessage());
        }

        return rd;
    }

    public String getResourcePath(String cssFile, String imagePath)
    {
        return FileUtils.getCanonicalOrAbsolutePath(getResourceFile(cssFile, imagePath));
    }
}
