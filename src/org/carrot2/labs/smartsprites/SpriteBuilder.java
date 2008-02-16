package org.carrot2.labs.smartsprites;

import java.io.*;
import java.util.*;

import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.PrintStreamMessageSink;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.util.CloseableUtils;
import org.carrot2.util.FileUtils;

import com.google.common.collect.*;

/**
 * @author Stanislaw Osinski
 */
public class SpriteBuilder
{
    public static final String DEFAULT_CSS_INDENT = "  ";
    public static final String DEFAULT_CSS_FILE_SUFFIX = "-sprite";

    public static void buildSprites(File dir, MessageLog messageLog)
        throws FileNotFoundException, IOException
    {
        buildSprites(dir, DEFAULT_CSS_FILE_SUFFIX, DEFAULT_CSS_INDENT, messageLog);
    }

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

        final long start = System.currentTimeMillis();

        // Identify css files.
        final Collection<File> files = org.apache.commons.io.FileUtils.listFiles(dir,
            new String []
            {
                "css"
            }, true);

        // Collect sprite declaration from all css files
        final Multimap<File, SpriteImageOccurrence> spriteImageOccurrencesByFile = collectSpriteImageOccurrences(
            files, messageLog);

        // Merge them, checking for duplicates
        final Map<String, SpriteImageDirective> spriteImageDirectivesBySpriteId = mergeSpriteImageOccurrences(
            spriteImageOccurrencesByFile, messageLog);

        // Collect sprite references from all css files
        final Multimap<File, SpriteReferenceOccurrence> spriteEntriesByFile = collectSpriteReferenceOccurrences(
            files, spriteImageDirectivesBySpriteId, messageLog);

        // Now merge and regroup all files by sprite-id
        final Multimap<String, SpriteReferenceOccurrence> spriteReferenceOccurrencesBySpriteId = mergeSpriteReferenceOccurrences(spriteEntriesByFile);

        // Build the sprite images
        messageLog.setCssPath(null);
        final Multimap<File, SpriteReferenceReplacement> spriteReplacementsByFile = SpriteImageBuilder
            .buildSpriteImages(spriteImageDirectivesBySpriteId,
                spriteReferenceOccurrencesBySpriteId, messageLog);

        // Rewrite the CSS
        rewriteCssFiles(spriteImageOccurrencesByFile, spriteReplacementsByFile,
            cssFileSuffix, cssIndent, messageLog);

        final long stop = System.currentTimeMillis();
        messageLog.logInfo(MessageType.PROCESSING_COMPLETED, (stop - start));
    }

    private static Multimap<File, SpriteImageOccurrence> collectSpriteImageOccurrences(
        Collection<File> files, MessageLog messageLog) throws FileNotFoundException,
        IOException
    {
        final Multimap<File, SpriteImageOccurrence> spriteImageOccurrencesByFile = Multimaps
            .newArrayListMultimap();
        for (final File cssFile : files)
        {
            messageLog.setCssPath(FileUtils.getCanonicalOrAbsolutePath(cssFile));

            final Collection<SpriteImageOccurrence> spriteImageOccurrences = SpriteDirectiveOccurrenceCollector
                .collectSpriteImageOccurrences(cssFile, messageLog);

            spriteImageOccurrencesByFile.putAll(cssFile, spriteImageOccurrences);
        }
        return spriteImageOccurrencesByFile;
    }

    private static Map<String, SpriteImageDirective> mergeSpriteImageOccurrences(
        final Multimap<File, SpriteImageOccurrence> spriteImageOccurrencesByFile,
        MessageLog messageLog)
    {
        final Map<String, SpriteImageDirective> spriteImageDirectivesBySpriteId = Maps
            .newHashMap();
        for (final Map.Entry<File, SpriteImageOccurrence> entry : spriteImageOccurrencesByFile
            .entries())
        {
            final File cssFile = entry.getKey();
            final SpriteImageOccurrence spriteImageOccurrence = entry.getValue();

            messageLog.setCssPath(FileUtils.getCanonicalOrAbsolutePath(cssFile));

            // Add to the global map, checking for duplicates
            if (spriteImageDirectivesBySpriteId
                .containsKey(spriteImageOccurrence.spriteImageDirective.spriteId))
            {
                messageLog.logWarning(MessageType.IGNORING_SPRITE_IMAGE_REDEFINITION);
            }
            else
            {
                spriteImageDirectivesBySpriteId.put(
                    spriteImageOccurrence.spriteImageDirective.spriteId,
                    spriteImageOccurrence.spriteImageDirective);
            }
        }
        return spriteImageDirectivesBySpriteId;
    }

    private static Multimap<File, SpriteReferenceOccurrence> collectSpriteReferenceOccurrences(
        Collection<File> files,
        final Map<String, SpriteImageDirective> spriteImageDirectivesBySpriteId,
        MessageLog messageLog) throws FileNotFoundException, IOException
    {
        final Multimap<File, SpriteReferenceOccurrence> spriteEntriesByFile = Multimaps
            .newArrayListMultimap();
        for (final File cssFile : files)
        {
            messageLog.setCssPath(FileUtils.getCanonicalOrAbsolutePath(cssFile));

            final Collection<SpriteReferenceOccurrence> spriteReferenceOccurrences = SpriteDirectiveOccurrenceCollector
                .collectSpriteReferenceOccurrences(cssFile,
                    spriteImageDirectivesBySpriteId, messageLog);

            spriteEntriesByFile.putAll(cssFile, spriteReferenceOccurrences);
        }
        return spriteEntriesByFile;
    }

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

    private static Multimap<String, SpriteReferenceOccurrence> mergeSpriteReferenceOccurrences(
        final Multimap<File, SpriteReferenceOccurrence> spriteEntriesByFile)
    {
        final Multimap<String, SpriteReferenceOccurrence> spriteReferenceOccurrencesBySpriteId = Multimaps
            .newLinkedHashMultimap();
        for (final SpriteReferenceOccurrence spriteReferenceOccurrence : spriteEntriesByFile
            .values())
        {
            spriteReferenceOccurrencesBySpriteId.put(
                spriteReferenceOccurrence.spriteReferenceDirective.spriteRef,
                spriteReferenceOccurrence);
        }
        return spriteReferenceOccurrencesBySpriteId;
    }

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

        try
        {
            messageLog.logInfo(MessageType.CREATING_CSS_STYLE_SHEET, FileUtils
                .getCanonicalOrAbsolutePath(processedCssFile));
            while ((originalCssLine = originalCssReader.readLine()) != null)
            {
                originalCssLineNumber++;

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
                    // Write some extra css as a replacement and ignore the directive
                    processedCssWriter.write("  background-image: url('"
                        + spriteReferenceReplacement.imageUrl + "');\n");
                    processedCssWriter.write("  background-position: "
                        + spriteReferenceReplacement.horizontalPositionString + " "
                        + spriteReferenceReplacement.verticalPositionString + ";\n");
                    continue;
                }

                // Just write the original line
                processedCssWriter.write(originalCssLine + "\n");
            }
        }
        finally
        {
            CloseableUtils.closeIgnoringException(originalCssReader);
            processedCssWriter.close();
        }
    }

    static File getProcessedCssFile(File originalCssFile)
    {
        return getProcessedCssFile(originalCssFile, DEFAULT_CSS_FILE_SUFFIX);
    }

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

        final String rootDir = System.getProperty("root.dir.path");
        if (rootDir == null)
        {
            System.out
                .println("Please privide root dir in root.dir.path system property.");
            return;
        }

        final MessageLog messageLog = new MessageLog(new PrintStreamMessageSink(
            System.out));

        buildSprites(new File(rootDir), cssFileSuffix, cssIndent, messageLog);
    }
}
