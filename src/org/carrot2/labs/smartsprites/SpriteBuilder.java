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
 * The entry class for SmartSprites.
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
        buildSprites(dir, DEFAULT_CSS_FILE_SUFFIX, DEFAULT_CSS_INDENT, messageLog);
    }

    /**
     * Performs processing for CSS/images contained in the provided <code>dir</code>,
     * saving messages to the provided {@link MessageLog}. The provided values of
     * parameters will be used.
     */
    @SuppressWarnings("unchecked")
    public static void buildSprites(File dir, String cssFileSuffix, String cssIndent,
        MessageLog messageLog) throws FileNotFoundException, IOException
    {
        if (!dir.isDirectory())
        {
            messageLog.logWarning(MessageType.NOT_A_DIRECTORY_ON_INPUT, FileUtils
                .getCanonicalOrAbsolutePath(dir));
            return;
        }

        // Identify css files.
        final Collection<File> files = org.apache.commons.io.FileUtils.listFiles(dir,
            new String []
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
                spriteReferenceOccurrencesBySpriteId, messageLog);

        // Rewrite the CSS
        rewriteCssFiles(spriteImageOccurrencesByFile, spriteReplacementsByFile,
            cssFileSuffix, cssIndent, messageLog);

    }

    /**
     * Rewrites the original files to refer to the generated sprite images.
     */
    private static void rewriteCssFiles(
        final Multimap<File, SpriteImageOccurrence> spriteImageOccurrencesByFile,
        final Multimap<File, SpriteReferenceReplacement> spriteReplacementsByFile,
        String cssFileSuffix, String cssRuleIndent, MessageLog messageLog)
        throws IOException
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
                    cssRuleIndent, messageLog);
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
                    cssRuleIndent, messageLog);
            }
        }
    }

    /**
     * Rewrites one CSS file to refer to the generated sprite images.
     */
    private static void createProcessedCss(File originalCssFile,
        Map<Integer, SpriteImageOccurrence> spriteImageOccurrencesByLineNumber,
        Map<Integer, SpriteReferenceReplacement> spriteReplacementsByLineNumber,
        String cssFileSuffix, String cssRuleIndent, MessageLog messageLog)
        throws IOException
    {
        final File processedCssFile = getProcessedCssFile(originalCssFile, cssFileSuffix);

        final BufferedReader originalCssReader = new BufferedReader(new FileReader(
            originalCssFile));
        final BufferedWriter processedCssWriter = new BufferedWriter(new FileWriter(
            processedCssFile));

        String originalCssLine;
        int originalCssLineNumber = -1;
        int lastReferenceReplacementLine = -1;

        try
        {
            messageLog.logInfo(MessageType.CREATING_CSS_STYLE_SHEET, FileUtils
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
                            messageLog.logWarning(MessageType.OVERRIDING_PROPERTY_FOUND,
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
        return getProcessedCssFile(originalCssFile, DEFAULT_CSS_FILE_SUFFIX);
    }

    /**
     * Gets the name of the processed CSS file.
     */
    static File getProcessedCssFile(File originalCssFile, String cssFileSuffix)
    {
        final String originalCssFileName = originalCssFile.getName();
        final String processedCssFileName = originalCssFileName.substring(0,
            originalCssFileName.length() - 4)
            + cssFileSuffix + ".css";

        final File processedCssFile = new File(originalCssFile.getParentFile(),
            processedCssFileName);
        return processedCssFile;
    }

    /**
     * Entry point to SmartSprites. All parameters are passed as JVM properties.
     */
    public static void main(String [] args) throws FileNotFoundException, IOException
    {
        String cssFileSuffix = System.getProperty("css.file.suffix");
        if (cssFileSuffix == null)
        {
            cssFileSuffix = DEFAULT_CSS_FILE_SUFFIX;
        }

        String cssIndent = System.getProperty("css.properti.indent");
        if (cssIndent == null)
        {
            cssIndent = DEFAULT_CSS_INDENT;
        }

        String logLevel = System.getProperty("log.level");
        if (logLevel == null)
        {
            logLevel = DEFAULT_LOGGING_LEVEL;
        }
        
        MessageLevel level;
        try
        {
            level = MessageLevel.valueOf(logLevel);
        }
        catch (Exception e)
        {
            level = MessageLevel.INFO;
        }

        final String rootDir = System.getProperty("root.dir.path");
        if (rootDir == null)
        {
            System.out
                .println("Please privide root dir in root.dir.path system property.");
            return;
        }

        final LevelCounterMessageSink levelCounter = new LevelCounterMessageSink();
        final MessageLog messageLog = new MessageLog(new PrintStreamMessageSink(
            System.out, level), levelCounter);
        
        final long start = System.currentTimeMillis();
        buildSprites(new File(rootDir), cssFileSuffix, cssIndent, messageLog);
        final long stop = System.currentTimeMillis();

        System.out.print("SmartSprites processing completed in " + (stop - start) + " ms");
        if (levelCounter.getWarnCount() > 0)
        {
            System.out.println(" with " + levelCounter.getWarnCount() + " warning(s)");
        }
        else
        {
            System.out.println();
        }
    }
}
