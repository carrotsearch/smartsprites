package org.carrot2.labs.smartsprites;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.util.*;

import org.carrot2.labs.smartsprites.message.LevelCounterMessageSink;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.util.CloseableUtils;
import org.carrot2.util.FileUtils;

import com.google.common.collect.*;

/**
 * Performs all stages of sprite building. This class is not thread-safe.
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
     * A cache of sprite image UIDs by sprite image file. This cache will help us to avoid
     * generating the MD5 each time we replace sprite image ocurrence in the CSS.
     */
    private final Map<File, String> spriteImageUidBySpriteImageFile;

    /**
     * A timestamp to use for timestamp-based sprite image UIDs. We need this time stamp
     * as a field to make sure the timestamp is the same for all sprite image
     * replacements.
     */
    private final String timestamp;

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
        spriteImageUidBySpriteImageFile = Maps.newHashMap();
        timestamp = Long.toString(new Date().getTime());
    }

    /**
     * Performs processing for this builder's parameters.
     */
    @SuppressWarnings("unchecked")
    public void buildSprites() throws FileNotFoundException, IOException
    {
        if (!parameters.validate(messageLog))
        {
            return;
        }

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

        final BufferedReader originalCssReader = new BufferedReader(
            new InputStreamReader(new FileInputStream(originalCssFile), parameters
                .getCssFileEncoding()));
        final BufferedWriter processedCssWriter = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(processedCssFile), parameters
                .getCssFileEncoding()));

        String originalCssLine;
        int originalCssLineNumber = -1;
        int lastReferenceReplacementLine = -1;

        // Generate UID for sprite file
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
                    processedCssWriter
                        .write("  background-image: url('"
                            + spriteReferenceReplacement.spriteImageProperties.spriteImageDirective.imagePath
                            + generateUidSuffix(
                                originalCssFile,
                                spriteReferenceReplacement.spriteImageProperties.spriteImageDirective,
                                false) + "')" + (important ? " !important" : "") + ";\n");
                    if (spriteReferenceReplacement.spriteImageProperties.hasReducedForIe6)
                    {
                        processedCssWriter
                            .write("  -background-image: url('"
                                + SpriteImageBuilder
                                    .addIe6Suffix(
                                        spriteReferenceReplacement.spriteImageProperties.spriteImageDirective,
                                        true)
                                + generateUidSuffix(
                                    originalCssFile,
                                    spriteReferenceReplacement.spriteImageProperties.spriteImageDirective,
                                    true) + "')" + (important ? " !important" : "")
                                + ";\n");
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
     * @param ie6 of true, the ie6 version of the sprite will be loaded
     * @return the value to be extered after '?' in the CSS
     */
    private String generateUidSuffix(File cssFile, SpriteImageDirective directive,
        boolean ie6) throws IOException
    {

        if (directive.uidType == SpriteImageDirective.SpriteUidType.NONE)
        {
            return "";
        }
        else if (directive.uidType == SpriteImageDirective.SpriteUidType.DATE)
        {
            return "?" + timestamp;
        }
        else if (directive.uidType == SpriteImageDirective.SpriteUidType.MD5)
        {
            final File imageFile = spriteImageBuilder.getImageFile(cssFile,
                (ie6 ? SpriteImageBuilder.addIe6Suffix(directive, true)
                    : directive.imagePath), false);
            if (spriteImageUidBySpriteImageFile.containsKey(directive))
            {
                return spriteImageUidBySpriteImageFile.get(imageFile);
            }

            try
            {
                final byte [] buffer = new byte [4069];
                final MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
                final InputStream is = new FileInputStream(imageFile);
                try
                {
                    final InputStream digestInputStream = new DigestInputStream(is,
                        digest);
                    while (digestInputStream.read(buffer) >= 0)
                    {
                    }

                    final String md5 = new BigInteger(1, digest.digest()).toString(16);
                    spriteImageUidBySpriteImageFile.put(imageFile, md5);
                    return "?" + md5;
                }
                finally
                {
                    CloseableUtils.closeIgnoringException(is);
                    digest.reset();
                }
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
