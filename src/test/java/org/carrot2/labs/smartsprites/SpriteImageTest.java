package org.carrot2.labs.smartsprites;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

/**
 * Test cases for static methods in {@link SpriteImage}.
 */
public class SpriteImageTest
{
    @Test
    public void ie6SuffixNoSuffix()
    {
        assertThat(SpriteImage.addIe6Suffix("path", false)).isEqualTo("path");
    }

    @Test
    public void ie6SuffixPlainFileNameWithDot()
    {
        assertThat(SpriteImage.addIe6Suffix("image.png", true))
            .isEqualTo("image-ie6.png");
    }

    @Test
    public void ie6SuffixPlainFileNameWithoutDoc()
    {
        assertThat(SpriteImage.addIe6Suffix("image", true)).isEqualTo("image-ie6");
    }

    @Test
    public void ie6SuffixPathWithoutDot()
    {
        assertThat(SpriteImage.addIe6Suffix("assets/img/image.png", true)).isEqualTo(
            "assets/img/image-ie6.png");
    }

    @Test
    public void ie6SuffixPathWithDot()
    {
        assertThat(SpriteImage.addIe6Suffix("../assets.img/image.png", true)).isEqualTo(
            "../assets.img/image-ie6.png");
    }

    @Test
    public void ie6SuffixPlainFileNameWithDotAndQuery()
    {
        assertThat(SpriteImage.addIe6Suffix("image.png?query", true)).isEqualTo(
            "image-ie6.png?query");
    }

    @Test
    public void ie6SuffixPlainFileNameWithQuery()
    {
        assertThat(SpriteImage.addIe6Suffix("image?query", true)).isEqualTo(
            "image-ie6?query");
    }

    @Test
    public void ie6SuffixPathWithDotAndQuery()
    {
        assertThat(SpriteImage.addIe6Suffix("assets.img/image.png?query", true)).isEqualTo(
            "assets.img/image-ie6.png?query");
    }
    
    @Test
    public void ie6SuffixPathWithQuery()
    {
        assertThat(SpriteImage.addIe6Suffix("assets.img/image?query", true)).isEqualTo(
        "assets.img/image-ie6?query");
    }
}
