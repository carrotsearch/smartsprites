package org.carrot2.labs.smartsprites;

import static org.fest.assertions.Assertions.assertThat;

import java.awt.Dimension;
import java.io.*;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * @author Stanislaw Osinski
 */
public class SpriteBuilderTest extends TestWithMemoryMessageSink
{

    @Test
    public void testNoSpriteDeclarations() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("no-sprite-declarations");

        SpriteBuilder.buildSprites(testDir, messageLog);

        assertThat(SpriteBuilder.getProcessedCssFile(sourceCss(testDir))).doesNotExist();

        cleanUp(testDir);
    }

    @Test
    public void testNoSpriteReferences() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("no-sprite-references");
        SpriteBuilder.buildSprites(testDir, messageLog);

        assertThat(expectedCss(testDir)).hasSameContentAs(
            SpriteBuilder.getProcessedCssFile(sourceCss(testDir)));

        cleanUp(testDir);
    }

    @Test
    public void testTargetSpriteImageDirNotExists() throws FileNotFoundException,
        IOException
    {
        final File testDir = testDir("target-sprite-image-dir-not-exists");
        SpriteBuilder.buildSprites(testDir, messageLog);

        assertThat(expectedCss(testDir)).hasSameContentAs(
            SpriteBuilder.getProcessedCssFile(sourceCss(testDir)));
        assertThat(new File(testDir, "img-sprite/sprite.png")).exists();

        cleanUp(testDir);
        FileUtils.deleteDirectory(new File(testDir, "img-sprite"));
    }

    @Test
    public void testSimpleHorizontalSprite() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("simple-horizontal-sprite");
        SpriteBuilder.buildSprites(testDir, messageLog);

        assertThat(expectedCss(testDir)).hasSameContentAs(
            SpriteBuilder.getProcessedCssFile(sourceCss(testDir)));
        assertThat(new File(testDir, "img/sprite.png")).exists();
        assertThat(ImageIO.read(new File(testDir, "img/sprite.png"))).hasSize(
            new Dimension(17 + 15 + 48, 47));

        cleanUp(testDir);
    }

    @Test
    public void testLargeVerticalRepeat() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("large-vertical-repeat");
        SpriteBuilder.buildSprites(testDir, messageLog);

        assertThat(expectedCss(testDir)).hasSameContentAs(
            SpriteBuilder.getProcessedCssFile(sourceCss(testDir)));
        assertThat(new File(testDir, "img/sprite.png")).exists();
        assertThat(ImageIO.read(new File(testDir, "img/sprite.png"))).hasSize(
            new Dimension(17 + 15, 16 * 17 /* lcm(16, 17) */));

        cleanUp(testDir);
    }

    private File testDir(String test)
    {
        final File testDir = new File("test/" + test);
        return testDir;
    }

    private void cleanUp(File testDir)
    {
        SpriteBuilder.getProcessedCssFile(sourceCss(testDir)).delete();
        new File(testDir, "img/sprite.png").delete();
    }

    private File expectedCss(final File testDir)
    {
        return new File(testDir, "css/style-expected.css");
    }

    private File sourceCss(final File testDir)
    {
        return new File(testDir, "css/style.css");
    }
}
