package org.carrot2.labs.smartsprites;

import java.io.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.DigestInputStream;
import java.math.BigInteger;

import org.carrot2.labs.smartsprites.message.LevelCounterMessageSink;
import org.carrot2.labs.smartsprites.message.MessageLog;
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
    /** Properties we need to watch for in terms of overriding the generated ones. */
    private static final HashSet<String> OVERRIDING_PROPERTIES = Sets.newHashSet(
        "background-position", "background-image");

    /** This builder's configuration */
    public final SmartSpritesParameters parameters;

    /** This builder's message log */
    private final MessageLog messageLog;

    /** Directive occurrence collector for this builder */
    private final SpriteDirectiveOccurrenceCollector spriteDirectiveOccurrenceCollector;

    /** SpriteImageBuilder for this builder */
    private final SpriteImageBuilder spriteImageBuilder;

    /**
     * Creates a {@link SpriteBuilder} with the provided parameters and log.
     */
    public SpriteBuilder(SmartSpritesParameters parameters, MessageLog messageLog)
    {
        this.messageLog = messageLog;
        this.parameters = parameters;

        spriteDirectiveOccurrenceCollector = new SpriteDirectiveOccurrenceCollector(
            messageLog);
        spriteImageBuilder = new SpriteImageBuilder(parameters, messageLog);
    }

    /**
     * Performs processing for this builder's parameters.
     */
    @SuppressWarnings("unchecked")
    public void buildSprites() throws FileNotFoundException, IOException
    {
        parameters.validate(messageLog);

        final long start = System.currentTimeMillis();
        final LevelCounterMessageSink levelCounter = new LevelCounterMessageSink();
        messageLog.addMessageSink(levelCounter);

        if (parameters.getOutputDir() != null && !parameters.getOutputDir().exists())
        {
            parameters.getOutputDir().mkdirs();
        }

        // Identify css files.
        final Collection<File> files = org.apache.commons.io.FileUtils.listFiles(
            parameters.getRootDir(), new String []
            {
                "css"
            }, true);

        // Collect sprite declaration from all css files
        final Multimap<File, SpriteImageOccurrence> spriteImageOccurrencesByFile = spriteDirectiveOccurrenceCollector
            .collectSpriteImageOccurrences(files);

        // Merge them, checking for duplicates
        final Map<String, SpriteImageDirective> spriteImageDirectivesBySpriteId = spriteDirectiveOccurrenceCollector
            .mergeSpriteImageOccurrences(spriteImageOccurrencesByFile);

        // Collect sprite references from all css files
        final Multimap<File, SpriteReferenceOccurrence> spriteEntriesByFile = spriteDirectiveOccurrenceCollector
            .collectSpriteReferenceOccurrences(files, spriteImageDirectivesBySpriteId);

        // Now merge and regroup all files by sprite-id
        final Multimap<String, SpriteReferenceOccurrence> spriteReferenceOccurrencesBySpriteId = SpriteDirectiveOccurrenceCollector
            .mergeSpriteReferenceOccurrences(spriteEntriesByFile);

        // Build the sprite images
        messageLog.setCssFile(null);
        final Multimap<File, SpriteReferenceReplacement> spriteReplacementsByFile = spriteImageBuilder
            .buildSpriteImages(spriteImageDirectivesBySpriteId,
                spriteReferenceOccurrencesBySpriteId);

        // Rewrite the CSS
        rewriteCssFiles(spriteImageOccurrencesByFile, spriteReplacementsByFile);

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
    private void rewriteCssFiles(
        final Multimap<File, SpriteImageOccurrence> spriteImageOccurrencesByFile,
        final Multimap<File, SpriteReferenceReplacement> spriteReplacementsByFile)
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
                    new HashMap<Integer, SpriteReferenceReplacement>());
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
                        .get(cssFile)), spriteReplacementsByLineNumber);
            }
        }
    }

    /**
     * Rewrites one CSS file to refer to the generated sprite images.
     */
    private void createProcessedCss(File originalCssFile,
        Map<Integer, SpriteImageOccurrence> spriteImageOccurrencesByLineNumber,
        Map<Integer, SpriteReferenceReplacement> spriteReplacementsByLineNumber)
        throws IOException
    {
        final File processedCssFile = getProcessedCssFile(originalCssFile);
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

        // Generate UID for sprite file
        final String uidSuffix = generateUidSuffix(
            originalCssFile,
            spriteImageOccurrencesByLineNumber.values().iterator().next().spriteImageDirective);

        try
        {
            messageLog.info(MessageType.CREATING_CSS_STYLE_SHEET, processedCssFile
                .getName());
            messageLog.setCssFile(originalCssFile);

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
                    final boolean important = spriteReferenceReplacement.spriteReferenceOccurrence.important;
                    lastReferenceReplacementLine = originalCssLineNumber;

                    // Write some extra css as a replacement and ignore the directive
                    processedCssWriter.write("  background-image: url('"
                        + spriteReferenceReplacement.spriteImageUrl + uidSuffix + "')"
                        + (important ? " !important" : "") + ";\n");
                    if (spriteReferenceReplacement.spriteImageProperties.hasReducedForIe6)
                    {
                        processedCssWriter
                            .write("  -background-image: url('"
                                + SpriteImageBuilder
                                    .addIe6Suffix(
                                        spriteReferenceReplacement.spriteImageProperties.spriteImageDirective,
                                        true) + uidSuffix + "')"
                                + (important ? " !important" : "") + ";\n");
                    }

                    processedCssWriter.write("  background-position: "
                        + spriteReferenceReplacement.horizontalPositionString + " "
                        + spriteReferenceReplacement.verticalPositionString
                        + (important ? " !important" : "") + ";\n");
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

            messageLog.setCssFile(null);
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
    File getProcessedCssFile(File originalCssFile)
    {
        final String originalCssFileName = originalCssFile.getName();
        final String processedCssFileName = originalCssFileName.substring(0,
            originalCssFileName.length() - 4)
            + parameters.getCssFileSuffix() + ".css";

        final File processedCssFile = new File(originalCssFile.getParentFile(),
            processedCssFileName);

        if (parameters.getOutputDir() != null)
        {
            return FileUtils.changeRoot(processedCssFile, parameters.getRootDir(),
                parameters.getOutputDir());
        }
        else
        {
            return processedCssFile;
        }
    }

    /**
     * Gets the appropriate extension based on the sprite UID generation mode.
     * 
     * @param cssFile The CSS File
     * @param directive The Image Directive for the generated CSS file
     * @return the value to be extered after '?' in the CSS
     */
    private String generateUidSuffix(File cssFile, SpriteImageDirective directive)
        throws IOException
    {
        if (directive.uidType == SpriteImageDirective.SpriteUidType.NONE)
        {
            return "";
        }
        else if (directive.uidType == SpriteImageDirective.SpriteUidType.DATE)
        {
            return "?" + Long.toString(new Date().getTime());
        }
        else if (directive.uidType == SpriteImageDirective.SpriteUidType.MD5)
        {
            try
            {
                MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
                InputStream is = new FileInputStream(spriteImageBuilder.getImageFile(
                    cssFile, directive.imagePath, false));
                try
                {
                    is = new DigestInputStream(is, digest);
                }
                finally
                {
                    is.close();
                }
                return "?" + new BigInteger(1, digest.digest()).toString(16);
            }
            catch (NoSuchAlgorithmException nsaex)
            {
                throw new RuntimeException(nsaex);
            }
        }

        // No valid value set
        return "";
    }
}
