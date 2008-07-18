package org.carrot2.labs.smartsprites;

import static org.carrot2.labs.test.TestEqualsHelper.wrap;
import static org.fest.assertions.Assertions.assertThat;

import java.awt.Dimension;
import java.io.*;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.carrot2.labs.smartsprites.message.Message;
import org.junit.Test;

/**
 * Test cases for {@link SpriteBuilder}. The test cases read/ write files to the
 * directories contained in the test/ directory.
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

    @Test
    public void testMissingImages() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("missing-images");
        SpriteBuilder.buildSprites(testDir, messageLog);

        assertThat(expectedCss(testDir)).hasSameContentAs(
            SpriteBuilder.getProcessedCssFile(sourceCss(testDir)));
        assertThat(new File(testDir, "img/sprite.png")).exists();
        assertThat(ImageIO.read(new File(testDir, "img/sprite.png"))).hasSize(
            new Dimension(18, 17 + 6 + 5));

        assertThat(wrap(messages)).contains(
            wrap(new Message(Message.MessageLevel.WARN,
                Message.MessageType.CANNOT_NOT_LOAD_IMAGE, new File(testDir,
                    "css/style.css").getCanonicalPath(), 15, new File(testDir,
                    "img/logo.png").getCanonicalPath(), "Can't read input file!")));

        cleanUp(testDir);
    }

    @Test
    public void testUnsupportedSpriteProperties() throws FileNotFoundException,
        IOException
    {
        final File testDir = testDir("unsupported-sprite-properties");
        SpriteBuilder.buildSprites(testDir, messageLog);

        assertThat(expectedCss(testDir)).hasSameContentAs(
            SpriteBuilder.getProcessedCssFile(sourceCss(testDir)));
        assertThat(new File(testDir, "img/sprite.png")).exists();
        assertThat(ImageIO.read(new File(testDir, "img/sprite.png"))).hasSize(
            new Dimension(48, 16 + 17 + 47));

        final String styleCssPath = new File(testDir, "css/style.css").getCanonicalPath();
        assertThat(wrap(messages)).contains(
            wrap(new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_PROPERTIES_FOUND, styleCssPath, 4,
                "sprites-layout")),
            wrap(new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_PROPERTIES_FOUND, styleCssPath, 14,
                "sprites-margin-top")),
            wrap(new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_PROPERTIES_FOUND, styleCssPath, 18,
                "sprites-alignment")));

        cleanUp(testDir);
    }

    @Test
    public void testOverridingCssProperties() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("overriding-css-properties");
        SpriteBuilder.buildSprites(testDir, messageLog);

        assertThat(expectedCss(testDir)).hasSameContentAs(
            SpriteBuilder.getProcessedCssFile(sourceCss(testDir)));
        assertThat(new File(testDir, "img/sprite.png")).exists();
        assertThat(ImageIO.read(new File(testDir, "img/sprite.png"))).hasSize(
            new Dimension(17 + 15 + 48, 47));

        final String styleCssPath = new File(testDir, "css/style.css").getCanonicalPath();
        assertThat(wrap(messages)).contains(
            wrap(new Message(Message.MessageLevel.WARN,
                Message.MessageType.OVERRIDING_PROPERTY_FOUND, styleCssPath, 10,
                "background-image", 9)),
            wrap(new Message(Message.MessageLevel.WARN,
                Message.MessageType.OVERRIDING_PROPERTY_FOUND, styleCssPath, 21,
                "background-position", 20)));

        cleanUp(testDir);
    }

    @Test
    public void testAbsoluteImageUrl() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("absolute-image-url");
        final File documentRootDir = testDir("absolute-image-url/absolute-path");
        SpriteBuilder.buildSprites(testDir, messageLog,
            SpriteBuilder.DEFAULT_CSS_FILE_SUFFIX, SpriteBuilder.DEFAULT_CSS_INDENT,
            null, documentRootDir);

        assertThat(expectedCss(testDir)).hasSameContentAs(
            SpriteBuilder.getProcessedCssFile(sourceCss(testDir)));
        final File spriteFile = new File(documentRootDir, "img/sprite.png");
        assertThat(spriteFile).exists();
        assertThat(ImageIO.read(spriteFile)).hasSize(new Dimension(17, 17));

        cleanUp(testDir);
        spriteFile.delete();
    }

    @Test
    public void testNonDefaultOutputDir() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("non-default-output-dir");
        final File documentRootDir = testDir("non-default-output-dir/absolute-path");
        final File outputDir = testDir("non-default-output-dir/output-dir");
        outputDir.mkdirs();
        SpriteBuilder.buildSprites(testDir, messageLog,
            SpriteBuilder.DEFAULT_CSS_FILE_SUFFIX, SpriteBuilder.DEFAULT_CSS_INDENT,
            outputDir, documentRootDir);

        assertThat(expectedCss(testDir)).hasSameContentAs(
            SpriteBuilder.getProcessedCssFile(sourceCss(testDir),
                SpriteBuilder.DEFAULT_CSS_FILE_SUFFIX, testDir, outputDir));

        final File absoluteSpriteFile = new File(documentRootDir, "img/absolute.png");
        assertThat(absoluteSpriteFile).exists();
        assertThat(ImageIO.read(absoluteSpriteFile)).hasSize(new Dimension(17, 17));

        final File relativeSpriteFile = new File(outputDir, "img/relative.png");
        assertThat(relativeSpriteFile).exists();
        assertThat(ImageIO.read(relativeSpriteFile)).hasSize(new Dimension(15, 16));

        FileUtils.deleteDirectory(outputDir);
        FileUtils.deleteDirectory(absoluteSpriteFile.getParentFile());
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
