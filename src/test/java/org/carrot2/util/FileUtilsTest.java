package org.carrot2.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test cases for {@link FileUtils}.
 */
public class FileUtilsTest
{
    @Test
    public void canonicalizeEmpty()
    {
        assertEquals("", FileUtils.canonicalize("", "/"));
    }

    @Test
    public void canonicalizeOneSegment()
    {
        assertEquals("file", FileUtils.canonicalize("file", "/"));
    }

    @Test
    public void canonicalizeTwoSegmentsCanonical()
    {
        assertEquals("path/file", FileUtils.canonicalize("path/file", "/"));
    }

    @Test
    public void canonicalizeTwoSegmentsNonCanonical()
    {
        assertEquals("", FileUtils.canonicalize("path/..", "/"));
    }

    @Test
    public void canonicalizeTwoSegmentsNonCanonicalTrailingSeparator()
    {
        assertEquals("", FileUtils.canonicalize("path/../", "/"));
    }

    @Test
    public void canonicalizeMoreSegmentsNonCanonical()
    {
        assertEquals("/longer/file/actual/file", FileUtils.canonicalize("/longer/file/path/../actual/file", "/"));
    }

    @Test
    public void canonicalizeMoreParents()
    {
        assertEquals("/longer/actual/file", FileUtils.canonicalize("/longer/file/path/../../actual/file", "/"));
    }

    @Test
    public void canonicalizeCanonicalStartingWithParent()
    {
        assertEquals("../../img/sprite.png", FileUtils.canonicalize("../../img/sprite.png", "/"));
    }
}
