package org.carrot2.labs.smartsprites.resource;

import java.io.*;

import org.apache.commons.io.FilenameUtils;
import org.carrot2.labs.smartsprites.SmartSpritesParameters;
import org.carrot2.labs.smartsprites.message.Message;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.util.FileUtils;

/**
 * This class defines the resource handler which manage resources from the file system.
 * 
 * @author Ibrahim Chaehoi
 * @author Stanislaw Osinski
 */
public class FileSystemResourceHandler implements ResourceHandler
{
    /** The message log */
    private final MessageLog messageLog;

    /** The root directory */
    private final File rootDir;

    /** The charset to assume in the {@link #getReader(String)} method. */
    private final String charset;

    /**
     * Creates a new {@link FileSystemResourceHandler}.
     * 
     * @param rootDir the root directory
     * @param charset the charset to assume in the {@link #getReader(String)} method
     * @param messageLog the message log
     */
    public FileSystemResourceHandler(File rootDir, String charset, MessageLog messageLog)
    {
        this.rootDir = rootDir;
        this.messageLog = messageLog;
        this.charset = charset;
    }

    public InputStream getResourceAsStream(String path)
    {
        InputStream is = null;
        try
        {
            is = new FileInputStream(path);
        }
        catch (FileNotFoundException e)
        {
            messageLog.info(Message.MessageType.GENERIC, e.getMessage());
        }

        return is;
    }

    public Reader getReader(String path)
    {
        Reader rd = null;
        try
        {
            rd = new InputStreamReader(getResourceAsStream(path), charset);
        }
        catch (UnsupportedEncodingException e)
        {
            messageLog.info(Message.MessageType.GENERIC, e.getMessage());
        }

        return rd;
    }

    /**
     * This implementation detects if the resource path starts with a "/" and resolves
     * such resources against the provided
     * {@link SmartSpritesParameters#getDocumentRootDir()} directory.
     */
    public String getResourcePath(String baseFile, String filePath)
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

        return file != null ? FileUtils.getCanonicalOrAbsolutePath(file) : "";
    }
}
