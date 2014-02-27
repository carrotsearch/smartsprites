package org.carrot2.labs.smartsprites;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test cases for static methods in {@link SpriteImage}.
 */
public class SpriteImageTest
{
    @Test
    public void ie6SuffixNoSuffix()
    {
        assertEquals("path", SpriteImage.addIe6Suffix("path", false));
    }

    @Test
    public void ie6SuffixPlainFileNameWithDot()
    {
        assertEquals("image-ie6.png", SpriteImage.addIe6Suffix("image.png", true));
    }

    @Test
    public void ie6SuffixPlainFileNameWithoutDoc()
    {
        assertEquals("image-ie6", SpriteImage.addIe6Suffix("image", true));
    }

    @Test
    public void ie6SuffixPathWithoutDot()
    {
        assertEquals("assets/img/image-ie6.png", SpriteImage.addIe6Suffix("assets/img/image.png", true));
    }

    @Test
    public void ie6SuffixPathWithDot()
    {
        assertEquals("../assets.img/image-ie6.png", SpriteImage.addIe6Suffix("../assets.img/image.png", true));
    }

    @Test
    public void ie6SuffixPlainFileNameWithDotAndQuery()
    {
        assertEquals("image-ie6.png?query", SpriteImage.addIe6Suffix("image.png?query", true));
    }

    @Test
    public void ie6SuffixPlainFileNameWithQuery()
    {
        assertEquals("image-ie6?query", SpriteImage.addIe6Suffix("image?query", true));
    }

    @Test
    public void ie6SuffixPathWithDotAndQuery()
    {
        assertEquals("assets.img/image-ie6.png?query", SpriteImage.addIe6Suffix("assets.img/image.png?query", true));
    }
    
    @Test
    public void ie6SuffixPathWithQuery()
    {
        assertEquals("assets.img/image-ie6?query", SpriteImage.addIe6Suffix("assets.img/image?query", true));
    }
}
