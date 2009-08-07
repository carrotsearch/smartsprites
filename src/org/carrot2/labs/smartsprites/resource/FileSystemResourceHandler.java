package org.carrot2.labs.smartsprites.resource;

import java.io.*;
import java.nio.charset.Charset;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
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
    private final String documentRootDir;

    /** The charset to assume in the {@link #getResourceAsReader(String)} method. */
    private final String charset;

    /**
     * Creates a new {@link FileSystemResourceHandler}.
     * 
     * @param documentRootDir the document root directory path, can be <code>null</code>
     * @param charset the charset to assume in the {@link #getResourceAsReader(String)}
     *            method
     * @param messageLog the message log
     */
    public FileSystemResourceHandler(String documentRootDirPath, String charset,
        MessageLog messageLog)
    {
        this.documentRootDir = documentRootDirPath;
        this.messageLog = messageLog;
        this.charset = charset;
        if (!Charset.isSupported(charset))
        {
            messageLog.error(Message.MessageType.GENERIC, "Charset '" + charset
                + "' is not supported.");
        }
    }

    public InputStream getResourceAsInputStream(String path) throws IOException
    {
        return new FileInputStream(FileUtils.getCanonicalOrAbsoluteFile(path));
    }

    public Reader getResourceAsReader(String path) throws IOException
    {
        try
        {
            return new InputStreamReader(getResourceAsInputStream(path), charset);
        }
        catch (UnsupportedEncodingException e)
        {
            // Should not happen as we're checking the charset in constructor
            throw new RuntimeException(e);
        }
    }

    public OutputStream getResourceAsOutputStream(String path) throws IOException
    {
        // Create directories if needed
        final File parentFile = new File(path).getParentFile();
        if (!parentFile.exists())
        {
            if (!parentFile.mkdirs()) 
            {
                messageLog.warning(Message.MessageType.CANNOT_CREATE_DIRECTORIES, 
                    parentFile.getPath());
            }
        }
        return new FileOutputStream(FileUtils.getCanonicalOrAbsoluteFile(path));
    }

    public Writer getResourceAsWriter(String path) throws IOException
    {
        try
        {
            return new OutputStreamWriter(getResourceAsOutputStream(path), charset);
        }
        catch (UnsupportedEncodingException e)
        {
            // Should not happen as we're checking the charset in constructor
            throw new RuntimeException(e);
        }
    }

    /**
     * This implementation detects if the resource path starts with a "/" and resolves
     * such resources against the provided
     * {@link SmartSpritesParameters#getDocumentRootDir()} directory.
     */
    public String getResourcePath(String baseFile, String filePath)
    {
        if (filePath.startsWith("/"))
        {
            if (StringUtils.isNotBlank(documentRootDir))
            {
                return FilenameUtils.concat(documentRootDir, filePath.substring(1));
            }
            else
            {
                messageLog.warning(MessageType.ABSOLUTE_PATH_AND_NO_DOCUMENT_ROOT,
                    filePath);
                return "";
            }
        }
        else
        {
            return FilenameUtils.concat(FilenameUtils.getFullPath(baseFile), filePath);
        }
    }
}
