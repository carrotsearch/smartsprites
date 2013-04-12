package org.carrot2.labs.smartsprites;

import static org.carrot2.labs.test.Assertions.assertThat;
import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.carrot2.labs.smartsprites.message.Message;
import org.carrot2.labs.smartsprites.message.Message.MessageLevel;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.util.FileUtils;
import org.junit.*;

import com.google.common.collect.Lists;

/**
 * Test cases for {@link SmartSpritesParameters}.
 */
public class SmartSpritesParametersTest extends TestWithMemoryMessageSink
{
    private File existingRootDir;
    private String existingRootDirPath;
    private File existingOutputDir;
    private String existingOutputDirPath;
    private File existingFile;
    private String existingFilePath;

    @Before
    public void prepareFiles() throws IOException
    {
        existingFile = File.createTempFile("smartsprites", null);
        existingFilePath = existingFile.getPath();
        existingRootDir = mkdirInTemp("rootdir");
        existingRootDirPath = existingRootDir.getPath();
        existingOutputDir = mkdirInTemp("outputdir");
        existingOutputDirPath = existingOutputDir.getPath();
    }

    @After
    public void cleanUpFiles() throws IOException
    {
        FileUtils.deleteThrowingExceptions(existingFile, existingOutputDir,
            existingRootDir);
    }

    @Test
    public void testValidateNoRootDirNoCssFiles()
    {
        checkInvalid(parameters(null, null),
            Message.error(MessageType.EITHER_ROOT_DIR_OR_CSS_FILES_IS_REQIRED));
    }

    @Test
    public void testValidateRootDirDoesNotExist()
    {
        final String dir = "nonexisting-dir";
        checkInvalid(parameters(dir, null), Message.error(
            MessageType.ROOT_DIR_DOES_NOT_EXIST_OR_IS_NOT_DIRECTORY, dir));
    }

    @Test
    public void testValidateOutputDirNoRootDir()
    {
        checkInvalid(
            parameters(null, Lists.newArrayList("css/file.css"), existingOutputDirPath),
            Message.error(
                MessageType.ROOT_DIR_IS_REQIRED_FOR_OUTPUT_DIR));
    }

    @Test
    public void testValidateOutputDirIsNotADirectory()
    {
        checkInvalid(
            parameters(existingRootDirPath, Lists.newArrayList("css/file.css"),
                existingFilePath), Message.error(
                MessageType.OUTPUT_DIR_IS_NOT_DIRECTORY, existingFilePath));
    }

    @Test
    public void testValidateDocumentRootDirDoesNotExist()
    {
        final String nonexistingDir = "nonexisting-dir";
        checkInvalid(parameters(existingRootDirPath, (String) null, nonexistingDir),
            Message.error(
                MessageType.DOCUMENT_ROOT_DIR_DOES_NOT_EXIST_OR_IS_NOT_DIRECTORY,
                nonexistingDir));
    }

    @Test
    public void testValidateDocumentRootDirIsNotADirectory()
    {
        checkInvalid(parameters(existingRootDirPath, (String) null, existingFilePath),
            Message.error(
                MessageType.DOCUMENT_ROOT_DIR_DOES_NOT_EXIST_OR_IS_NOT_DIRECTORY,
                existingFilePath));
    }

    @Test
    public void testValidateNoOutputDirAndEmptyCssFileSuffix()
    {
        checkInvalid(new SmartSpritesParameters(null, Lists.newArrayList("css/file.css"),
            null, null, null, "", null, false, null), Message.error(
            MessageType.CSS_FILE_SUFFIX_IS_REQUIRED_IF_NO_OUTPUT_DIR));
    }

    @Test
    public void testValidateRootDirAndCssFilesWithoutOutputDir()
    {
        checkInvalid(
            parameters(existingRootDirPath, Lists.newArrayList("css/file.css"), null),
            Message.error(
                MessageType.ROOT_DIR_AND_CSS_FILES_CANNOT_BE_BOTH_SPECIFIED_UNLESS_WITH_OUTPUT_DIR));
    }

    @Test
    public void testValidateValidAllDirs()
    {
        checkValid(parameters(existingRootDirPath, existingOutputDirPath,
            existingOutputDirPath));

    }

    @Test
    public void testValidateValidOnlyRootDir()
    {
        checkValid(parameters(".", null));
    }

    private void checkValid(final SmartSpritesParameters parameters)
    {
        assertThat(parameters.validate(messageLog)).isTrue();
    }

    private void checkInvalid(SmartSpritesParameters parameters, Message... messages)
    {
        assertThat(parameters.validate(messageLog)).isFalse();
        assertThat(this.messages).contains(messages);
    }

    private static SmartSpritesParameters parameters(String rootDir, String outputDir)
    {
        return parameters(rootDir, outputDir, null);
    }

    private static SmartSpritesParameters parameters(String rootDir, String outputDir,
        String documentRootDir)
    {
        return new SmartSpritesParameters(rootDir, null, outputDir, documentRootDir,
            null, null, null, false, null);
    }

    private static SmartSpritesParameters parameters(String rootDir,
        List<String> cssFiles, String outputDir)
    {
        return new SmartSpritesParameters(rootDir, cssFiles, outputDir, null, null, null,
            null, false, null);
    }

    private File mkdirInTemp(final String name) throws IOException
    {
        final File file = new File(existingFile.getParent(), existingFile.getName()
            + name);
        FileUtils.mkdirsThrowingExceptions(file);
        return file;
    }
}
