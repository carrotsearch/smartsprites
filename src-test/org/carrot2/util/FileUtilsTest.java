package org.carrot2.util;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

/**
 * Test cases for {@link FileUtils}.
 */
public class FileUtilsTest
{
    @Test
    public void canonicalizeEmpty()
    {
        assertThat(FileUtils.canonicalize("", "/")).isEqualTo("");
    }

    @Test
    public void canonicalizeOneSegment()
    {
        assertThat(FileUtils.canonicalize("file", "/")).isEqualTo("file");
    }

    @Test
    public void canonicalizeTwoSegmentsCanonical()
    {
        assertThat(FileUtils.canonicalize("path/file", "/")).isEqualTo("path/file");
    }

    @Test
    public void canonicalizeTwoSegmentsNonCanonical()
    {
        assertThat(FileUtils.canonicalize("path/..", "/")).isEqualTo("");
    }

    @Test
    public void canonicalizeTwoSegmentsNonCanonicalTrailingSeparator()
    {
        assertThat(FileUtils.canonicalize("path/../", "/")).isEqualTo("");
    }

    @Test
    public void canonicalizeMoreSegmentsNonCanonical()
    {
        assertThat(FileUtils.canonicalize("/longer/file/path/../actual/file", "/"))
            .isEqualTo("/longer/file/actual/file");
    }

    @Test
    public void canonicalizeMoreParents()
    {
        assertThat(FileUtils.canonicalize("/longer/file/path/../../actual/file", "/"))
            .isEqualTo("/longer/actual/file");
    }

    @Test
    public void canonicalizeCanonicalStartingWithParent()
    {
        assertThat(FileUtils.canonicalize("../../img/sprite.png", "/")).isEqualTo("../../img/sprite.png");
    }
}
