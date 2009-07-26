package org.carrot2.labs.smartsprites;

import java.io.InputStream;
import java.io.Reader;

/**
 * The interface for the resource handler. It allows to have an abstraction level about
 * the resource (CSS or images) management.
 * 
 * @author Ibrahim Chaehoi
 */
public interface ResourceHandler
{
    /**
     * Returns the resource stream for the file path
     * 
     * @param baseFile the base file from which the path is defined.
     * @param filePath the file path
     * @return the resource stream
     */
    public InputStream getResourceAsStream(String baseFile, String filePath);

    /**
     * Returns the reader for the path
     * 
     * @param path the resource path
     * @return the reader
     */
    public Reader getReader(String path);

    /**
     * Returns the resource path
     * 
     * @param baseFile the base file
     * @param filePath the file path
     * @return the resource path
     */
    public String getResourcePath(String baseFile, String filePath);
}
