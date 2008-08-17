package org.carrot2.labs.smartsprites;

import java.io.*;
import java.util.*;

import org.carrot2.labs.smartsprites.message.*;
import org.carrot2.labs.smartsprites.message.Message.MessageLevel;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.util.CloseableUtils;
import org.carrot2.util.FileUtils;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Performs all stages of sprite building.
 */
public class SpriteBuilder
{
    /** Default indent for the generated CSS properties. */
    public static final String DEFAULT_CSS_INDENT = "  ";

    /** The default suffix to be added to the generated CSS files. */
    public static final String DEFAULT_CSS_FILE_SUFFIX = "-sprite";

    /** The default logging level. */
    public static final String DEFAULT_LOGGING_LEVEL = "INFO";

    /** Properties we need to watch for in terms of overriding the generated ones. */
    private static final HashSet<String> OVERRIDING_PROPERTIES = Sets.newHashSet(
        "background-position", "background-image");

    /**
     * Performs processing for CSS/images contained in the provided <code>dir</code>,
     * saving messages to the provided {@link MessageLog}. Default parameters will be
     * used.
     */
    public static void buildSprites(File dir, MessageLog messageLog)
        throws FileNotFoundException, IOException
    {
        buildSprites(new SmartSpritesParameters(dir, null, null, MessageLevel.INFO,
            DEFAULT_CSS_FILE_SUFFIX, DEFAULT_CSS_INDENT), messageLog);
    }

    /**
     * Performs processing for CSS/images contained in the provided <code>dir</code>,
     * saving messages to the provided {@link MessageLog}. The provided values of
     * parameters will be used.
     */
    @SuppressWarnings("unchecked")
    public static void buildSprites(SmartSpritesParameters parameters,
        MessageLog messageLog) throws FileNotFoundException, IOException
    {
        parameters.validate(messageLog);
        
        final long start = System.currentTimeMillis();
        final LevelCounterMessageSink levelCounter = new LevelCounterMessageSink();
        messageLog.addMessageSink(levelCounter);

        if (parameters.outputDir != null && !parameters.outputDir.exists())
        {
            parameters.outputDir.mkdirs();
        }

        // Identify css files.
        final Collection<File> files = org.apache.commons.io.FileUtils.listFiles(
            parameters.rootDir, new String []
            {
                "css"
            }, true);

        // Collect sprite declaration from all css files
        final Multimap<File, SpriteImageOccurrence> spriteImageOccurrencesByFile = SpriteDirectiveOccurrenceCollector
            .collectSpriteImageOccurrences(files, messageLog);

        // Merge them, checking for duplicates
        final Map<String, SpriteImageDirective> spriteImageDirectivesBySpriteId = SpriteDirectiveOccurrenceCollector
            .mergeSpriteImageOccurrences(spriteImageOccurrencesByFile, messageLog);

        // Collect sprite references from all css files
        final Multimap<File, SpriteReferenceOccurrence> spriteEntriesByFile = SpriteDirectiveOccurrenceCollector
            .collectSpriteReferenceOccurrences(files, spriteImageDirectivesBySpriteId,
                messageLog);

        // Now merge and regroup all files by sprite-id
        final Multimap<String, SpriteReferenceOccurrence> spriteReferenceOccurrencesBySpriteId = SpriteDirectiveOccurrenceCollector
            .mergeSpriteReferenceOccurrences(spriteEntriesByFile);

        // Build the sprite images
        messageLog.setCssPath(null);
        final Multimap<File, SpriteReferenceReplacement> spriteReplacementsByFile = SpriteImageBuilder
            .buildSpriteImages(spriteImageDirectivesBySpriteId,
                spriteReferenceOccurrencesBySpriteId, parameters.rootDir,
                parameters.outputDir, parameters.documentRootDir, messageLog);

        // Rewrite the CSS
        rewriteCssFiles(spriteImageOccurrencesByFile, spriteReplacementsByFile,
            parameters.cssFileSuffix, parameters.cssPropertyIndent, parameters.rootDir,
            parameters.outputDir, messageLog);

        final long stop = System.currentTimeMillis();

        if (levelCounter.getWarnCount() > 0)
        {
            messageLog.status(MessageType.PROCESSING_COMPLETED_WITH_WARNINGS,
                (stop - start), levelCounter.getWarnCount());
        }
        else
        {
            messageLog.status(MessageType.PROCESSING_COMPLETED, (stop - start));
        }
    }

    /**
     * Rewrites the original files to refer to the generated sprite images.
     */
    private static void rewriteCssFiles(
        final Multimap<File, SpriteImageOccurrence> spriteImageOccurrencesByFile,
        final Multimap<File, SpriteReferenceReplacement> spriteReplacementsByFile,
        String cssFileSuffix, String cssRuleIndent, File rootDir, File outputDir,
        MessageLog messageLog) throws IOException
    {
        if (spriteReplacementsByFile.isEmpty())
        {
            // If nothing to replace, still, copy the original file, so that there
            // is some output file.
            for (final Map.Entry<File, Collection<SpriteImageOccurrence>> entry : spriteImageOccurrencesByFile
                .asMap().entrySet())
            {
                final File cssFile = entry.getKey();

                createProcessedCss(cssFile, SpriteImageBuilder
                    .getSpriteImageOccurrencesByLineNumber(spriteImageOccurrencesByFile
                        .get(cssFile)),
                    new HashMap<Integer, SpriteReferenceReplacement>(), cssFileSuffix,
                    cssRuleIndent, rootDir, outputDir, messageLog);
            }
        }
        else
        {
            for (final Map.Entry<File, Collection<SpriteReferenceReplacement>> entry : spriteReplacementsByFile
                .asMap().entrySet())
            {
                final File cssFile = entry.getKey();
                final Map<Integer, SpriteReferenceReplacement> spriteReplacementsByLineNumber = SpriteImageBuilder
                    .getSpriteReplacementsByLineNumber(entry.getValue());

                createProcessedCss(cssFile, SpriteImageBuilder
                    .getSpriteImageOccurrencesByLineNumber(spriteImageOccurrencesByFile
                        .get(cssFile)), spriteReplacementsByLineNumber, cssFileSuffix,
                    cssRuleIndent, rootDir, outputDir, messageLog);
            }
        }
    }

    /**
     * Rewrites one CSS file to refer to the generated sprite images.
     */
    private static void createProcessedCss(File originalCssFile,
        Map<Integer, SpriteImageOccurrence> spriteImageOccurrencesByLineNumber,
        Map<Integer, SpriteReferenceReplacement> spriteReplacementsByLineNumber,
        String cssFileSuffix, String cssRuleIndent, File rootDir, File outputDir,
        MessageLog messageLog) throws IOException
    {
        final File processedCssFile = getProcessedCssFile(originalCssFile, cssFileSuffix,
            rootDir, outputDir);
        if (!processedCssFile.getParentFile().exists())
        {
            processedCssFile.getParentFile().mkdirs();
        }

        final BufferedReader originalCssReader = new BufferedReader(new FileReader(
            originalCssFile));
        final BufferedWriter processedCssWriter = new BufferedWriter(new FileWriter(
            processedCssFile));

        String originalCssLine;
        int originalCssLineNumber = -1;
        int lastReferenceReplacementLine = -1;

        try
        {
            messageLog.info(MessageType.CREATING_CSS_STYLE_SHEET, FileUtils
                .getCanonicalOrAbsolutePath(processedCssFile));
            messageLog.setCssPath(FileUtils.getCanonicalOrAbsolutePath(originalCssFile));

            while ((originalCssLine = originalCssReader.readLine()) != null)
            {
                originalCssLineNumber++;
                messageLog.setLine(originalCssLineNumber);

                if (originalCssLine.contains("}"))
                {
                    lastReferenceReplacementLine = -1;
                }

                final SpriteImageOccurrence spriteImageOccurrence = spriteImageOccurrencesByLineNumber
                    .get(originalCssLineNumber);
                final SpriteReferenceReplacement spriteReferenceReplacement = spriteReplacementsByLineNumber
                    .get(originalCssLineNumber);

                if (spriteImageOccurrence != null)
                {
                    // Ignore line with directive
                    continue;
                }

                if (spriteReferenceReplacement != null)
                {
                    lastReferenceReplacementLine = originalCssLineNumber;

                    // Write some extra css as a replacement and ignore the directive
                    processedCssWriter.write("  background-image: url('"
                        + spriteReferenceReplacement.spriteImageUrl + "');\n");
                    processedCssWriter.write("  background-position: "
                        + spriteReferenceReplacement.horizontalPositionString + " "
                        + spriteReferenceReplacement.verticalPositionString + ";\n");
                    continue;
                }

                if (lastReferenceReplacementLine >= 0)
                {
                    for (final String property : OVERRIDING_PROPERTIES)
                    {
                        if (originalCssLine.contains(property))
                        {
                            messageLog.warning(MessageType.OVERRIDING_PROPERTY_FOUND,
                                property, lastReferenceReplacementLine);
                        }
                    }
                }

                // Just write the original line
                processedCssWriter.write(originalCssLine + "\n");
            }

            messageLog.setCssPath(null);
        }
        finally
        {
            CloseableUtils.closeIgnoringException(originalCssReader);
            processedCssWriter.close();
        }
    }

    /**
     * Gets the name of the processed CSS file.
     */
    static File getProcessedCssFile(File originalCssFile)
    {
        return getProcessedCssFile(originalCssFile, DEFAULT_CSS_FILE_SUFFIX, null, null);
    }

    /**
     * Gets the name of the processed CSS file.
     */
    static File getProcessedCssFile(File originalCssFile, String cssFileSuffix,
        File rootDir, File outputDir)
    {
        final String originalCssFileName = originalCssFile.getName();
        final String processedCssFileName = originalCssFileName.substring(0,
            originalCssFileName.length() - 4)
            + cssFileSuffix + ".css";

        final File processedCssFile = new File(originalCssFile.getParentFile(),
            processedCssFileName);

        if (outputDir != null)
        {
            return FileUtils.changeRoot(processedCssFile, rootDir, outputDir);
        }
        else
        {
            return processedCssFile;
        }
    }
}
