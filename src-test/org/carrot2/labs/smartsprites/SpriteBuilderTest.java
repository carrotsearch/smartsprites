package org.carrot2.labs.smartsprites;

import static org.carrot2.labs.test.Assertions.assertThat;
import static org.fest.assertions.Assertions.assertThat;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.carrot2.labs.smartsprites.SmartSpritesParameters.PngDepth;
import org.carrot2.labs.smartsprites.message.Message;
import org.carrot2.labs.smartsprites.message.Message.MessageLevel;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.junit.*;

import com.google.common.collect.Lists;

/**
 * Test cases for {@link SpriteBuilder}. The test cases read/ write files to the
 * directories contained in the test/ directory.
 */
public class SpriteBuilderTest extends TestWithMemoryMessageSink
{
    /** Builder under tests, initialized in {@link #buildSprites(SmartSpritesParameters)} */
    private SpriteBuilder spriteBuilder;

    @Before
    public void setUpHeadlessMode()
    {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    public void testNoSpriteDeclarations() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("no-sprite-declarations");

        buildSprites(testDir);

        assertThat(processedCss()).doesNotExist();
        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);
    }

    @Test
    public void testNoSpriteReferences() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("no-sprite-references");
        buildSprites(testDir);

        assertThat(processedCss()).hasSameContentAs(expectedCss());
        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);
    }

    @Test
    public void testTargetSpriteImageDirNotExists() throws FileNotFoundException,
        IOException
    {
        final File testDir = testDir("target-sprite-image-dir-not-exists");
        buildSprites(testDir);

        assertThat(processedCss()).hasSameContentAs(expectedCss());
        assertThat(new File(testDir, "img-sprite/sprite.png")).exists();
        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);

        FileUtils.deleteDirectory(new File(testDir, "img-sprite"));
    }

    @Test
    public void testSimpleHorizontalSprite() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("simple-horizontal-sprite");
        buildSprites(testDir);

        assertThat(processedCss()).hasSameContentAs(expectedCss());
        assertThat(new File(testDir, "img/sprite.png")).exists();
        org.fest.assertions.Assertions.assertThat(sprite(testDir)).hasSize(
            new Dimension(17 + 15 + 48, 47));
        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);
    }

    @Test
    public void testSimpleHorizontalSpriteImportant() throws FileNotFoundException,
        IOException
    {
        final File testDir = testDir("simple-horizontal-sprite-important");
        buildSprites(testDir, true);

        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);
        assertThat(processedCss()).hasSameContentAs(expectedCss());
        assertThat(new File(testDir, "img/sprite.png")).exists();
        org.fest.assertions.Assertions.assertThat(sprite(testDir)).hasSize(
            new Dimension(17 + 15 + 48 + 20, 47));
    }

    @Test
    public void testMultipleCssFiles() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("multiple-css-files");
        buildSprites(testDir);

        assertThat(processedCss()).hasSameContentAs(expectedCss());
        assertThat(css("css/style2-sprite.css")).hasSameContentAs(
            css("css/style2-expected.css"));
        assertThat(new File(testDir, "img/sprite.png")).exists();
        org.fest.assertions.Assertions.assertThat(sprite(testDir)).hasSize(
            new Dimension(17 + 15 + 48, 47));
        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);
    }

    @Test
    public void testLargeRepeat() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("large-repeat");
        buildSprites(testDir);

        final String spriteHorizontalPath = "img/sprite-horizontal.png";
        final String spriteVerticalPath = "img/sprite-vertical.png";

        assertThat(processedCss()).hasSameContentAs(expectedCss());
        assertThat(new File(testDir, spriteHorizontalPath)).exists();
        assertThat(new File(testDir, spriteVerticalPath)).exists();
        org.fest.assertions.Assertions.assertThat(sprite(testDir, spriteHorizontalPath))
            .hasSize(new Dimension(17 + 15, 16 * 17 /* lcm(16, 17) */));
        org.fest.assertions.Assertions.assertThat(sprite(testDir, spriteVerticalPath))
            .hasSize(new Dimension(15 * 17 /* lcm(15, 17) */, 17 + 16));
        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);
    }

    @Test
    public void testMissingImages() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("missing-images");
        buildSprites(testDir);

        assertThat(processedCss()).hasSameContentAs(expectedCss());
        assertThat(new File(testDir, "img/sprite.png")).exists();
        org.fest.assertions.Assertions.assertThat(sprite(testDir)).hasSize(
            new Dimension(18, 17 + 6 + 5));

        // The unsatisfied sprite references are not removed from the output
        // file, hence we have two warnings
        assertThat(messages).contains(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.CANNOT_NOT_LOAD_IMAGE, new File(testDir,
                    "css/style.css").getPath(), 15, new File(testDir, "img/logo.png")
                    .getPath(), "Can't read input file!"),
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.CANNOT_NOT_LOAD_IMAGE, new File(testDir,
                    "css/style-expected.css").getPath(), 15, new File(testDir,
                    "img/logo.png").getPath(), "Can't read input file!"));
    }

    @Test
    public void testUnsupportedIndividualImageFormat() throws FileNotFoundException,
        IOException
    {
        final File testDir = testDir("unsupported-image-format");
        buildSprites(testDir);

        assertThat(processedCss()).hasSameContentAs(expectedCss());
        assertThat(new File(testDir, "img/sprite.png")).doesNotExist();

        assertThat(messages).contains(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_INDIVIDUAL_IMAGE_FORMAT, new File(
                    testDir, "css/style.css").getPath(), 8, new File(testDir,
                    "img/web.iff").getPath()));
    }

    @Test
    public void testUnsupportedSpriteProperties() throws FileNotFoundException,
        IOException
    {
        final File testDir = testDir("unsupported-sprite-properties");
        buildSprites(testDir);

        assertThat(processedCss()).hasSameContentAs(expectedCss());
        assertThat(new File(testDir, "img/sprite.png")).exists();
        org.fest.assertions.Assertions.assertThat(sprite(testDir)).hasSize(
            new Dimension(48, 16 + 17 + 47));

        final String styleCssPath = new File(testDir, "css/style.css").getPath();
        assertThat(messages).isEquivalentTo(
            Message.MessageLevel.WARN,
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_PROPERTIES_FOUND, styleCssPath, 4,
                "sprites-layout"),
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_PROPERTIES_FOUND, styleCssPath, 14,
                "sprites-margin-top"),
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_PROPERTIES_FOUND, styleCssPath, 18,
                "sprites-alignment"));
    }

    @Test
    public void testOverridingCssProperties() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("overriding-css-properties");
        buildSprites(testDir);

        assertThat(processedCss()).hasSameContentAs(expectedCss());
        assertThat(new File(testDir, "img/sprite.png")).exists();
        org.fest.assertions.Assertions.assertThat(sprite(testDir)).hasSize(
            new Dimension(17 + 15 + 48, 47));

        final String styleCssPath = new File(testDir, "css/style.css").getPath();
        assertThat(messages).isEquivalentTo(
            Message.MessageLevel.WARN,
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.OVERRIDING_PROPERTY_FOUND, styleCssPath, 10,
                "background-image", 9),
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.OVERRIDING_PROPERTY_FOUND, styleCssPath, 21,
                "background-position", 20));
    }

    @Test
    public void testAbsoluteImageUrl() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("absolute-image-url");
        final File documentRootDir = testDir("absolute-image-url/absolute-path");
        buildSprites(filesystemSmartSpritesParameters(testDir, null, documentRootDir,
            MessageLevel.INFO, SmartSpritesParameters.DEFAULT_CSS_FILE_SUFFIX,
            SmartSpritesParameters.DEFAULT_SPRITE_PNG_DEPTH,
            SmartSpritesParameters.DEFAULT_SPRITE_PNG_IE6,
            SmartSpritesParameters.DEFAULT_CSS_FILE_ENCODING));

        assertThat(processedCss()).hasSameContentAs(expectedCss());
        final File spriteFile = new File(documentRootDir, "img/sprite.png");
        assertThat(spriteFile).exists();
        org.fest.assertions.Assertions.assertThat(ImageIO.read(spriteFile)).hasSize(
            new Dimension(17, 17));
        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);

        org.carrot2.util.FileUtils.deleteThrowingExceptions(spriteFile);
    }

    @Test
    public void testNonDefaultOutputDir() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("non-default-output-dir");
        final File documentRootDir = testDir("non-default-output-dir/absolute-path");
        final File outputDir = testDir("non-default-output-dir/output-dir");
        org.carrot2.util.FileUtils.mkdirsThrowingExceptions(outputDir);
        buildSprites(filesystemSmartSpritesParameters(testDir, outputDir,
            documentRootDir, MessageLevel.INFO,
            SmartSpritesParameters.DEFAULT_CSS_FILE_SUFFIX,
            SmartSpritesParameters.DEFAULT_SPRITE_PNG_DEPTH,
            SmartSpritesParameters.DEFAULT_SPRITE_PNG_IE6,
            SmartSpritesParameters.DEFAULT_CSS_FILE_ENCODING));

        assertThat(processedCss()).hasSameContentAs(expectedCss());

        final File absoluteSpriteFile = new File(documentRootDir, "img/absolute.png");
        assertThat(absoluteSpriteFile).exists();
        org.fest.assertions.Assertions.assertThat(ImageIO.read(absoluteSpriteFile))
            .hasSize(new Dimension(17, 17));

        final File relativeSpriteFile = new File(outputDir, "img/relative.png");
        assertThat(relativeSpriteFile).exists();
        org.fest.assertions.Assertions.assertThat(ImageIO.read(relativeSpriteFile))
            .hasSize(new Dimension(15, 16));
        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);

        FileUtils.deleteDirectory(outputDir);
        FileUtils.deleteDirectory(absoluteSpriteFile.getParentFile());
    }

    @Test
    public void testCssOutputDir() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("css-output-dir");
        final File rootDir = new File(testDir, "css/sprite").getCanonicalFile();
        final File outputDir = testDir("css-output-dir/output-dir/css");
        org.carrot2.util.FileUtils.mkdirsThrowingExceptions(outputDir);
        buildSprites(filesystemSmartSpritesParameters(rootDir, outputDir, null,
            MessageLevel.INFO, SmartSpritesParameters.DEFAULT_CSS_FILE_SUFFIX,
            SmartSpritesParameters.DEFAULT_SPRITE_PNG_DEPTH,
            SmartSpritesParameters.DEFAULT_SPRITE_PNG_IE6,
            SmartSpritesParameters.DEFAULT_CSS_FILE_ENCODING));

        assertThat(processedCss(new File(rootDir, "style.css"))).hasSameContentAs(
            new File(rootDir, "style-expected.css"));

        final File relativeSpriteFile = new File(outputDir, "../img/relative.png");
        assertThat(relativeSpriteFile).exists();
        org.fest.assertions.Assertions.assertThat(ImageIO.read(relativeSpriteFile))
            .hasSize(new Dimension(17 + 15, 17));
        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);

        FileUtils.deleteDirectory(outputDir.getParentFile());
    }

    @Test
    public void testRepeatedImageReferences() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("repeated-image-references");
        buildSprites(testDir);

        assertThat(processedCss()).hasSameContentAs(expectedCss());
        assertThat(new File(testDir, "img/sprite.png")).exists();
        org.fest.assertions.Assertions.assertThat(sprite(testDir)).hasSize(
            new Dimension(17 + 19, 19));
        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);
    }

    @Test
    public void testIndexedColor() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("indexed-color");
        buildSprites(testDir);

        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-bit-alpha.gif")).isIndexedColor().hasBitAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-bit-alpha.png")).isIndexedColor().hasBitAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-full-alpha.png")).isDirectColor().hasTrueAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-many-colors.png")).isDirectColor()
            .doesNotHaveAlpha();

        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);
    }

    @Test
    public void testIndexedForcedDirectColor() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("indexed-color");
        buildSprites(filesystemSmartSpritesParameters(testDir, null, null,
            MessageLevel.INFO, SmartSpritesParameters.DEFAULT_CSS_FILE_SUFFIX,
            PngDepth.DIRECT, SmartSpritesParameters.DEFAULT_SPRITE_PNG_IE6,
            SmartSpritesParameters.DEFAULT_CSS_FILE_ENCODING));

        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-bit-alpha.gif")).isIndexedColor().hasBitAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-bit-alpha.png")).isDirectColor().hasBitAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-full-alpha.png")).isDirectColor().hasTrueAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-many-colors.png")).isDirectColor()
            .doesNotHaveAlpha();

        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);
    }

    @Test
    public void testIndexedForcedIndexedColor() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("indexed-color");
        buildSprites(filesystemSmartSpritesParameters(testDir, null, null,
            MessageLevel.INFO, SmartSpritesParameters.DEFAULT_CSS_FILE_SUFFIX,
            PngDepth.INDEXED, SmartSpritesParameters.DEFAULT_SPRITE_PNG_IE6,
            SmartSpritesParameters.DEFAULT_CSS_FILE_ENCODING));

        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-bit-alpha.gif")).isIndexedColor().hasBitAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-bit-alpha.png")).isIndexedColor().hasBitAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-full-alpha.png")).isIndexedColor().hasBitAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-many-colors.png")).isIndexedColor()
            .doesNotHaveAlpha();

        assertThat(messages).isEquivalentTo(
            Message.MessageLevel.WARN,
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.ALPHA_CHANNEL_LOSS_IN_INDEXED_COLOR, null, 25,
                "full-alpha"),
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.USING_WHITE_MATTE_COLOR_AS_DEFAULT, null, 25,
                "full-alpha"),
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.TOO_MANY_COLORS_FOR_INDEXED_COLOR, null, 32,
                "many-colors", 293, 255));
    }

    @Test
    public void testMatteColor() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("matte-color");
        buildSprites(testDir);

        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-bit-alpha.png")).isIndexedColor().hasBitAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-full-alpha-m1.png")).isDirectColor()
            .hasTrueAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-full-alpha-m2.png")).isDirectColor()
            .hasTrueAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-full-alpha-m3.png")).isDirectColor()
            .hasTrueAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-many-colors.png")).isDirectColor()
            .doesNotHaveAlpha();

        assertThat(messages).isEquivalentTo(
            Message.MessageLevel.WARN,
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.IGNORING_MATTE_COLOR_NO_SUPPORT, null, 12,
                "full-alpha-m1"),
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.IGNORING_MATTE_COLOR_NO_SUPPORT, null, 19,
                "full-alpha-m2"),
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.IGNORING_MATTE_COLOR_NO_SUPPORT, null, 26,
                "full-alpha-m3"),
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.IGNORING_MATTE_COLOR_NO_PARTIAL_TRANSPARENCY, null,
                33, "bit-alpha"),
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.IGNORING_MATTE_COLOR_NO_SUPPORT, null, 40,
                "many-colors"));
    }

    @Test
    public void testMatteColorForcedIndex() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("matte-color");
        buildSprites(filesystemSmartSpritesParameters(testDir, null, null,
            MessageLevel.INFO, SmartSpritesParameters.DEFAULT_CSS_FILE_SUFFIX,
            PngDepth.INDEXED, SmartSpritesParameters.DEFAULT_SPRITE_PNG_IE6,
            SmartSpritesParameters.DEFAULT_CSS_FILE_ENCODING));

        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-bit-alpha.png")).isIndexedColor().hasBitAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-full-alpha-m1.png")).isIndexedColor()
            .hasBitAlpha().isEqualTo(sprite(testDir, "img/sprite-full-alpha-m2.png"))
            .isNotEqualTo(sprite(testDir, "img/sprite-full-alpha-m3.png"));
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-full-alpha-m2.png")).isIndexedColor()
            .hasBitAlpha().isEqualTo(sprite(testDir, "img/sprite-full-alpha-m1.png"))
            .isNotEqualTo(sprite(testDir, "img/sprite-full-alpha-m3.png"));
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-full-alpha-m3.png")).isIndexedColor()
            .hasBitAlpha().isNotEqualTo(sprite(testDir, "img/sprite-full-alpha-m1.png"));
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-many-colors.png")).isIndexedColor()
            .doesNotHaveAlpha();

        assertThat(messages).isEquivalentTo(
            Message.MessageLevel.WARN,
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.ALPHA_CHANNEL_LOSS_IN_INDEXED_COLOR, null, 12,
                "full-alpha-m1"),
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.ALPHA_CHANNEL_LOSS_IN_INDEXED_COLOR, null, 19,
                "full-alpha-m2"),
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.ALPHA_CHANNEL_LOSS_IN_INDEXED_COLOR, null, 26,
                "full-alpha-m3"),
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.IGNORING_MATTE_COLOR_NO_PARTIAL_TRANSPARENCY, null,
                33, "bit-alpha"),
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.TOO_MANY_COLORS_FOR_INDEXED_COLOR, null, 40,
                "many-colors", 293, 255));
    }

    @Test
    public void testIe6IndexedColor() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("indexed-color-ie6");
        buildSprites(filesystemSmartSpritesParameters(testDir, null, null,
            MessageLevel.INFO, SmartSpritesParameters.DEFAULT_CSS_FILE_SUFFIX,
            PngDepth.AUTO, true, SmartSpritesParameters.DEFAULT_CSS_FILE_ENCODING));

        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-bit-alpha.gif")).isIndexedColor().hasBitAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-bit-alpha.png")).isIndexedColor().hasBitAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-full-alpha.png")).isDirectColor().hasTrueAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-full-alpha-ie6.png")).isIndexedColor()
            .hasBitAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-many-colors.png")).isDirectColor()
            .doesNotHaveAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-many-colors-bit-alpha-ie6.png")).isIndexedColor()
            .hasBitAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-many-colors-bit-alpha-no-ie6.png"))
            .isDirectColor().hasBitAlpha();
        org.carrot2.labs.test.Assertions.assertThat(
            sprite(testDir, "img/sprite-many-colors-bit-alpha.png")).isDirectColor()
            .hasBitAlpha();

        assertThat(processedCss()).hasSameContentAs(expectedCss());

        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);
        assertThat(messages).isEquivalentTo(
            Message.MessageLevel.IE6NOTICE,
            new Message(Message.MessageLevel.IE6NOTICE,
                Message.MessageType.ALPHA_CHANNEL_LOSS_IN_INDEXED_COLOR, null, 27,
                "full-alpha"),
            new Message(Message.MessageLevel.IE6NOTICE,
                Message.MessageType.USING_WHITE_MATTE_COLOR_AS_DEFAULT, null, 27,
                "full-alpha"),
            new Message(Message.MessageLevel.IE6NOTICE,
                Message.MessageType.TOO_MANY_COLORS_FOR_INDEXED_COLOR, null, 41,
                "many-colors-bit-alpha", 293, 255));
    }

    @Test
    public void testSpriteImageUidMd5() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("sprite-image-uid-md5");
        buildSprites(testDir);

        assertThat(processedCss()).hasSameContentAs(expectedCss());
        assertThat(new File(testDir, "img/sprite.png")).exists();
        org.fest.assertions.Assertions.assertThat(sprite(testDir)).hasSize(
            new Dimension(17 + 15, 17));
        org.fest.assertions.Assertions.assertThat(sprite(testDir, "img/sprite2.png"))
            .hasSize(new Dimension(48, 47));
        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);
    }

    @Test
    public void testSpriteImageUidMd5Ie6() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("sprite-image-uid-md5-ie6");
        buildSprites(filesystemSmartSpritesParameters(testDir, null, null,
            MessageLevel.INFO, SmartSpritesParameters.DEFAULT_CSS_FILE_SUFFIX,
            PngDepth.AUTO, true, SmartSpritesParameters.DEFAULT_CSS_FILE_ENCODING));

        assertThat(processedCss()).hasSameContentAs(expectedCss());
        assertThat(new File(testDir, "img/sprite.png")).exists();
        assertThat(new File(testDir, "img/sprite-ie6.png")).exists();
        org.fest.assertions.Assertions.assertThat(sprite(testDir)).hasSize(
            new Dimension(20, 20));
        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);
    }

    @Test
    public void testIndividualCssFileDoesNotExist() throws IOException
    {
        final String path = testDir("does-not-exist").getPath();
        buildSprites(Lists.newArrayList(path));
        assertThat(messages).contains(
            new Message(MessageLevel.WARN, MessageType.CSS_FILE_DOES_NOT_EXIST, path));
    }

    @Test
    public void testDirectoryProvidedAsIndividualCssFile() throws IOException
    {
        final String path = testDir(".").getPath();
        buildSprites(Lists.newArrayList(path));
        assertThat(messages).contains(
            new Message(MessageLevel.WARN, MessageType.CSS_PATH_IS_NOT_A_FILE, path));
    }

    @Test
    public void testIndividualCssFilesWithoutOutputDir() throws IOException
    {
        final File testDir = testDir("individual-css-files-without-output-dir");
        final File css = new File(testDir, "css/style-sprite.css");
        final File customCss = new File(testDir, "css/custom/style-sprite.css");
        final File otherCss = new File(testDir, "css-other/style-sprite.css");
        final File sprite = new File(testDir, "img/sprite.png");
        try
        {
            buildSprites(Lists.newArrayList(
                new File(testDir, "/css/style.css").getPath(), new File(testDir,
                    "css/custom/style.css").getPath(), new File(testDir,
                    "css-other/style.css").getPath()));
            assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);
            assertThat(css).hasSameContentAs(new File(testDir, "css/style-expected.css"));
            assertThat(customCss).hasSameContentAs(
                new File(testDir, "css/custom/style-expected.css"));
            assertThat(otherCss).hasSameContentAs(
                new File(testDir, "css-other/style-expected.css"));
            assertThat(sprite).exists();
            org.fest.assertions.Assertions.assertThat(sprite(testDir)).hasSize(
                new Dimension(17, 17));
        }
        finally
        {
            org.carrot2.util.FileUtils.deleteThrowingExceptions(css, otherCss, customCss,
                sprite);
        }
    }

    @Test
    public void testIndividualCssFilesWithOutputDir() throws IOException
    {
        final File testDir = testDir("individual-css-files-with-output-dir");
        final File outputDir = new File(testDir, "output");
        final File css = new File(outputDir, "style-sprite.css");
        final File customCss = new File(outputDir, "custom/style-sprite.css");
        final File otherCss = new File(testDir, "css-other/style-sprite.css");
        final File sprite = new File(testDir, "img/sprite.png");
        try
        {
            final String otherCssPath = new File(testDir, "css-other/style.css")
                .getPath();
            buildSprites(Lists.newArrayList(new File(testDir, "css/style.css").getPath(),
                new File(testDir, "css/custom/style.css").getPath(), otherCssPath),
                new File(testDir, "css").getPath(), outputDir.getPath());
            assertThat(css).hasSameContentAs(new File(testDir, "css/style-expected.css"));
            assertThat(customCss).hasSameContentAs(
                new File(testDir, "css/custom/style-expected.css"));
            assertThat(otherCss).doesNotExist();
            assertThat(sprite).exists();
            org.fest.assertions.Assertions.assertThat(sprite(testDir)).hasSize(
                new Dimension(17, 17));
            assertThat(messages).contains(
                new Message(MessageLevel.WARN,
                    MessageType.IGNORING_CSS_FILE_OUTSIDE_OF_ROOT_DIR, otherCssPath));
        }
        finally
        {
            FileUtils.deleteDirectory(outputDir);
            org.carrot2.util.FileUtils.deleteThrowingExceptions(sprite);
        }
    }

    @Test
    public void testSpriteMargins() throws FileNotFoundException, IOException
    {
        final File testDir = testDir("sprite-margins");
        buildSprites(testDir);

        assertThat(processedCss()).hasSameContentAs(expectedCss());
        final String horizontalSpritePath = "img/sprite-horizontal.png";
        assertThat(new File(testDir, horizontalSpritePath)).exists();
        org.fest.assertions.Assertions.assertThat(sprite(testDir, horizontalSpritePath))
            .hasSize(new Dimension(48 + 100 + 100 + 48 + 48, 47 * 6));

        final String verticalSpritePath = "img/sprite-vertical.png";
        org.fest.assertions.Assertions.assertThat(sprite(testDir, verticalSpritePath))
            .hasSize(new Dimension(48 * 6, 47 + 100 + 100 + 47 + 47));

        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);
    }

    @After
    public void cleanUp() throws IOException
    {
        // Delete sprite CSS
        final String rootDir = spriteBuilder.parameters.getRootDir();
        if (rootDir == null)
        {
            return;
        }

        org.carrot2.util.FileUtils.deleteThrowingExceptions(new File(rootDir, "css")
            .listFiles(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.contains("-sprite");
                }
            }));

        // Delete sprites
        org.carrot2.util.FileUtils.deleteThrowingExceptions(new File(rootDir, "img")
            .listFiles(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.startsWith("sprite");
                }
            }));
    }

    private File testDir(String test)
    {
        return new File("test/" + test);
    }

    private BufferedImage sprite(final File testDir) throws IOException
    {
        return sprite(testDir, "img/sprite.png");
    }

    private BufferedImage sprite(final File testDir, String imagePath) throws IOException
    {
        return ImageIO.read(new File(testDir, imagePath));
    }

    private File expectedCss()
    {
        return css("css/style-expected.css");
    }

    private File sourceCss()
    {
        return css("css/style.css");
    }

    private File processedCss()
    {
        return processedCss(sourceCss());
    }

    private File css(String cssPath)
    {
        return new File(spriteBuilder.parameters.getRootDir(), cssPath);
    }

    private File processedCss(File sourceCss)
    {
        return new File(spriteBuilder.getProcessedCssFile(sourceCss.getPath()));
    }

    private void buildSprites(File dir) throws IOException
    {
        buildSprites(dir, false);
    }

    private void buildSprites(File dir, boolean ie6) throws IOException
    {
        buildSprites(new SmartSpritesParameters(dir.getPath(), null, null, null,
            SmartSpritesParameters.DEFAULT_LOGGING_LEVEL,
            SmartSpritesParameters.DEFAULT_CSS_FILE_SUFFIX,
            SmartSpritesParameters.DEFAULT_SPRITE_PNG_DEPTH, ie6,
            SmartSpritesParameters.DEFAULT_CSS_FILE_ENCODING));
    }

    private void buildSprites(List<String> cssFiles) throws IOException
    {
        buildSprites(cssFiles, null, null);
    }

    private void buildSprites(List<String> cssFiles, String rootDir, String outputDir)
        throws IOException
    {
        buildSprites(new SmartSpritesParameters(rootDir, cssFiles, outputDir, null,
            SmartSpritesParameters.DEFAULT_LOGGING_LEVEL,
            SmartSpritesParameters.DEFAULT_CSS_FILE_SUFFIX,
            SmartSpritesParameters.DEFAULT_SPRITE_PNG_DEPTH,
            SmartSpritesParameters.DEFAULT_SPRITE_PNG_IE6,
            SmartSpritesParameters.DEFAULT_CSS_FILE_ENCODING));
    }

    private void buildSprites(SmartSpritesParameters parameters) throws IOException
    {
        spriteBuilder = new SpriteBuilder(parameters, messageLog);
        spriteBuilder.buildSprites();
    }

    private static SmartSpritesParameters filesystemSmartSpritesParameters(File rootDir,
        File outputDir, File documentRootDir, MessageLevel logLevel,
        String cssFileSuffix, PngDepth spritePngDepth, boolean spritePngIe6,
        String cssEncoding)
    {
        return new SmartSpritesParameters(rootDir.getPath(), null,
            outputDir != null ? outputDir.getPath() : null,
            documentRootDir != null ? documentRootDir.getPath() : null, logLevel,
            cssFileSuffix, spritePngDepth, spritePngIe6, cssEncoding);
    }

}
