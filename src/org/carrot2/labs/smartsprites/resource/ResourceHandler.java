package org.carrot2.labs.smartsprites.resource;

import java.io.InputStream;
import java.io.Reader;

/**
 * Defines an abstraction layer for resource (CSS, images) management. Resources are
 * defined by means of abstract implementation-dependent paths represented by plain
 * {@link String}s.
 * 
 * @author Ibrahim Chaehoi
 * @author Stanislaw Osinski
 */
public interface ResourceHandler
{
    /**
     * Returns the resource stream for the provided path.
     * 
     * @param path the resource path
     * @return the resource stream or <code>null</code> if the resource could not be
     *         opened.
     */
    public InputStream getResourceAsStream(String path);

    /**
     * Returns the reader for the provided path. Implementations are responsible for
     * creating the reader with the right charset.
     * 
     * @param path the resource path
     * @return the reader or <code>null</code> if the resource could not be opened.
     */
    public Reader getReader(String path);

    /**
     * Builds a resource path from a base resource or directory path and a relative
     * resource path.
     * 
     * @param basePath the base path, can be either a resource or directory path
     * @param resourcePath the base-path-relative resource path. Note that certain
     *            implementation may support a case where resource path is in fact
     *            absolute and ignore the base path as appropriate.
     * @return the combined resource path
     */
    public String getResourcePath(String basePath, String resourcePath);
}
