package org.carrot2.labs.smartsprites.resource;

import java.io.*;

import org.carrot2.labs.smartsprites.SmartSpritesParameters;

/**
 * Defines an abstraction layer for resource (CSS, images) management. Resources are
 * defined by means of abstract implementation-dependent paths represented by plain
 * {@link String}s. Responsibility for closing of all resources acquired from this
 * interface rests with the caller.
 * 
 * @author Ibrahim Chaehoi
 * @author Stanislaw Osinski
 */
public interface ResourceHandler
{
    /**
     * Returns the resource input stream for the provided path.
     * 
     * @param path the resource path
     * @return the resource stream or <code>null</code> if the resource could not be
     *         opened.
     */
    public InputStream getResourceAsInputStream(String path) throws IOException;

    /**
     * Returns the reader for the provided path. Implementations are responsible for
     * creating the reader with the right charset.
     * 
     * @param path the resource path
     * @return the reader or <code>null</code> if the resource could not be opened.
     */
    public Reader getResourceAsReader(String path) throws IOException;

    /**
     * Returns the resource output stream for the provided path. If the resource already
     * exists, its content should be overwritten.
     * 
     * @param path the resource path
     * @return the resource stream or <code>null</code> if the resource could not be
     *         opened.
     */
    public OutputStream getResourceAsOutputStream(String path) throws IOException;

    /**
     * Returns the writer for the provided path. If the resource already exists, its
     * content should be overwritten. Implementations are responsible for creating the
     * writer with the right charset.
     * 
     * @param path the resource path
     * @return the writer or <code>null</code> if the resource could not be opened.
     */
    public Writer getResourceAsWriter(String path) throws IOException;

    /**
     * Builds a resource path relative to a CSS file resource path.
     * 
     * @param cssFilePath the CSS file path (base)
     * @param cssRelativePath the relative path to be resolved against the cssFilePath. If
     *            the resource path starts with the '/' character, it is an absolute
     *            resource and should be resolved against the
     *            {@link SmartSpritesParameters#getDocumentRootDir()} path instead. All
     *            other resource paths should be assumed to be relative to the
     *            cssFilePath.
     * @return the combined resource path
     */
    public String getResourcePath(String cssFilePath, String cssRelativePath);
}
