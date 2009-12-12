package org.carrot2.labs.smartsprites;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.carrot2.labs.smartsprites.message.*;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.labs.smartsprites.resource.FileSystemResourceHandler;
import org.carrot2.labs.smartsprites.resource.ResourceHandler;
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
    private final Map<String, String> spriteImageUidBySpriteImageFile;

    /**
     * A timestamp to use for timestamp-based sprite image UIDs. We need this time stamp
     * as a field to make sure the timestamp is the same for all sprite image
     * replacements.
     */
    private final String timestamp;

    /** Resource handler */
    private ResourceHandler resourceHandler;

    /**
     * Creates a {@link SpriteBuilder} with the provided parameters and log.
     */
    public SpriteBuilder(SmartSpritesParameters parameters, MessageLog messageLog)
    {
        this(parameters, messageLog, new FileSystemResourceHandler(parameters
            .getDocumentRootDir(), parameters.getCssFileEncoding(), messageLog));
    }

    /**
     * Creates a {@link SpriteBuilder} with the provided parameters and log.
     */
    public SpriteBuilder(SmartSpritesParameters parameters, MessageLog messageLog,
        ResourceHandler resourceHandler)
    {
        this.messageLog = messageLog;
        this.parameters = parameters;
        this.resourceHandler = resourceHandler;
        spriteDirectiveOccurrenceCollector = new SpriteDirectiveOccurrenceCollector(
            messageLog, resourceHandler);
        spriteImageBuilder = new SpriteImageBuilder(parameters, messageLog,
            resourceHandler);
        spriteImageUidBySpriteImageFile = Maps.newHashMap();
        timestamp = Long.toString(new Date().getTime());
    }

    /**
     * Performs processing for this builder's parameters. This method resolves all paths
     * against the local file system.
     */
    @SuppressWarnings("unchecked")
    public void buildSprites() throws FileNotFoundException, IOException
    {
        if (!parameters.validate(messageLog))
        {
            return;
        }

        final Collection<String> filePaths;
        if (parameters.getCssFiles() != null && !parameters.getCssFiles().isEmpty())
        {
            // Take directly provided css fle paths
            filePaths = parameters.getCssFiles();

            // If root dir is provided, filter out those files that are outside root dir
            if (StringUtils.isNotBlank(parameters.getOutputDir()))
            {
                filterFilesOutsideRootDir(filePaths);
            }

            // Make sure the files exist and are really files
            for (Iterator<String> it = filePaths.iterator(); it.hasNext();)
            {
                final String path = it.next();
                final File file = new File(path);
                if (file.exists())
                {
                    if (!file.isFile())
                    {
                        messageLog.warning(MessageType.CSS_PATH_IS_NOT_A_FILE, path);
                        it.remove();
                    }
                }
                else
                {
                    messageLog.warning(MessageType.CSS_FILE_DOES_NOT_EXIST, path);
                    it.remove();
                }
            }
        }
        else
        {
            // Take all css files from the root dir
            filePaths = Lists.newArrayList();
            final Collection<File> files = org.apache.commons.io.FileUtils.listFiles(
                parameters.getRootDirFile(), new String []
                {
                    "css"
                }, true);

            for (File file : files)
            {
                filePaths.add(file.getPath());
            }
        }

        buildSprites(filePaths);
    }

    private void filterFilesOutsideRootDir(Collection<String> filePaths)
        throws IOException
    {
        for (Iterator<String> it = filePaths.iterator(); it.hasNext();)
        {
            final String filePath = it.next();
            if (!FileUtils.isFileInParent(new File(filePath), parameters.getRootDirFile()))
            {
                it.remove();
                messageLog.warning(MessageType.IGNORING_CSS_FILE_OUTSIDE_OF_ROOT_DIR,
                    filePath);
            }
        }
    }

    /**
     * Performs processing from the list of file paths for this builder's parameters.
     */
    public void buildSprites(Collection<String> filePaths) throws FileNotFoundException,
        IOException
    {
        final long start = System.currentTimeMillis();

        final LevelCounterMessageSink levelCounter = new LevelCounterMessageSink();
        messageLog.addMessageSink(levelCounter);

        // Collect sprite declarations from all css files
        final Multimap<String, SpriteImageOccurrence> spriteImageOccurrencesByFile = spriteDirectiveOccurrenceCollector
            .collectSpriteImageOccurrences(filePaths);

        // Merge them, checking for duplicates
        final Map<String, SpriteImageOccurrence> spriteImageOccurrencesBySpriteId = spriteDirectiveOccurrenceCollector
            .mergeSpriteImageOccurrences(spriteImageOccurrencesByFile);
        final Map<String, SpriteImageDirective> spriteImageDirectivesBySpriteId = Maps
            .newLinkedHashMap();
        for (Map.Entry<String, SpriteImageOccurrence> entry : spriteImageOccurrencesBySpriteId
            .entrySet())
        {
            spriteImageDirectivesBySpriteId.put(entry.getKey(),
                entry.getValue().spriteImageDirective);
        }

        // Collect sprite references from all css files
        final Multimap<String, SpriteReferenceOccurrence> spriteEntriesByFile = spriteDirectiveOccurrenceCollector
            .collectSpriteReferenceOccurrences(filePaths, spriteImageDirectivesBySpriteId);

        // Now merge and regroup all files by sprite-id
        final Multimap<String, SpriteReferenceOccurrence> spriteReferenceOccurrencesBySpriteId = SpriteDirectiveOccurrenceCollector
            .mergeSpriteReferenceOccurrences(spriteEntriesByFile);

        // Build the sprite images
        messageLog.setCssFile(null);
        final Multimap<String, SpriteReferenceReplacement> spriteReplacementsByFile = spriteImageBuilder
            .buildSpriteImages(spriteImageOccurrencesBySpriteId,
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
        final Multimap<String, SpriteImageOccurrence> spriteImageOccurrencesByFile,
        final Multimap<String, SpriteReferenceReplacement> spriteReplacementsByFile)
        throws IOException
    {
        if (spriteReplacementsByFile.isEmpty())
        {
            // If nothing to replace, still, copy the original file, so that there
            // is some output file.
            for (final Map.Entry<String, Collection<SpriteImageOccurrence>> entry : spriteImageOccurrencesByFile
                .asMap().entrySet())
            {
                final String cssFile = entry.getKey();

                createProcessedCss(cssFile, SpriteImageBuilder
                    .getSpriteImageOccurrencesByLineNumber(spriteImageOccurrencesByFile
                        .get(cssFile)),
                    new HashMap<Integer, SpriteReferenceReplacement>());
            }
        }
        else
        {
            for (final Map.Entry<String, Collection<SpriteReferenceReplacement>> entry : spriteReplacementsByFile
                .asMap().entrySet())
            {
                final String cssFile = entry.getKey();
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
    private void createProcessedCss(String originalCssFile,
        Map<Integer, SpriteImageOccurrence> spriteImageOccurrencesByLineNumber,
        Map<Integer, SpriteReferenceReplacement> spriteReplacementsByLineNumber)
        throws IOException
    {
        final String processedCssFile = getProcessedCssFile(originalCssFile);
        final BufferedReader originalCssReader = new BufferedReader(resourceHandler
            .getResourceAsReader(originalCssFile));
        messageLog.setCssFile(null);
        messageLog.info(MessageType.CREATING_CSS_STYLE_SHEET, processedCssFile);
        messageLog.info(MessageType.READING_CSS, originalCssFile);
        final BufferedWriter processedCssWriter = new BufferedWriter(resourceHandler
            .getResourceAsWriter(processedCssFile));
        messageLog.info(MessageType.WRITING_CSS, processedCssFile);

        String originalCssLine;
        int originalCssLineNumber = -1;
        int lastReferenceReplacementLine = -1;

        // Generate UID for sprite file
        try
        {
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
                            + spriteReferenceReplacement.spriteImage.spriteImageDirective.imagePath
                            + generateUidSuffix(
                                originalCssFile,
                                spriteReferenceReplacement.spriteImage.spriteImageDirective,
                                false) + "')" + (important ? " !important" : "") + ";\n");
                    if (spriteReferenceReplacement.spriteImage.hasReducedForIe6)
                    {
                        processedCssWriter
                            .write("  -background-image: url('"
                                + SpriteImageBuilder
                                    .addIe6Suffix(
                                        spriteReferenceReplacement.spriteImage.spriteImageDirective,
                                        true)
                                + generateUidSuffix(
                                    originalCssFile,
                                    spriteReferenceReplacement.spriteImage.spriteImageDirective,
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
    String getProcessedCssFile(String originalCssFile)
    {
        final int lastDotIndex = originalCssFile.lastIndexOf('.');
        final String processedCssFile;
        if (lastDotIndex >= 0)
        {
            processedCssFile = originalCssFile.substring(0, lastDotIndex)
                + parameters.getCssFileSuffix() + originalCssFile.substring(lastDotIndex);
        }
        else
        {
            processedCssFile = originalCssFile + parameters.getCssFileSuffix();
        }

        if (parameters.hasOutputDir())
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
    private String generateUidSuffix(String cssFile, SpriteImageDirective directive,
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
            final String imageFile = spriteImageBuilder.getImageFile(cssFile,
                (ie6 ? SpriteImageBuilder.addIe6Suffix(directive, true)
                    : directive.imagePath));
            if (spriteImageUidBySpriteImageFile.containsKey(imageFile))
            {
                return "?" + spriteImageUidBySpriteImageFile.get(imageFile);
            }

            try
            {
                final byte [] buffer = new byte [4069];
                final MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
                InputStream is = null, digestInputStream = null;
                try
                {
                    is = resourceHandler.getResourceAsInputStream(imageFile);
                    digestInputStream = new DigestInputStream(is, digest);
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
                    CloseableUtils.closeIgnoringException(digestInputStream);
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
