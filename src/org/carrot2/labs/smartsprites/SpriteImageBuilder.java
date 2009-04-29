package org.carrot2.labs.smartsprites;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.math.util.MathUtils;
import org.carrot2.labs.smartsprites.SpriteImageDirective.SpriteImageLayout;
import org.carrot2.labs.smartsprites.SpriteReferenceDirective.SpriteAlignment;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.util.FileUtils;

import com.google.common.collect.*;

/**
 * Lays out and builds sprite images based on the collected SmartSprites directives.
 */
public class SpriteImageBuilder
{
    /** This builder's configuration */
    public final SmartSpritesParameters parameters;

    /** This builder's message log */
    private final MessageLog messageLog;

    /** Image merger for this builder */
    private SpriteImageMerger spriteImageMerger;

    /**
     * Creates a {@link SpriteImageBuilder} with the provided parameters and log.
     */
    SpriteImageBuilder(SmartSpritesParameters parameters, MessageLog messageLog)
    {
        this.messageLog = messageLog;
        this.parameters = parameters;

        spriteImageMerger = new SpriteImageMerger(parameters, messageLog);
    }

    /**
     * Builds all sprite images based on the collected directives.
     */
    Multimap<File, SpriteReferenceReplacement> buildSpriteImages(
        Map<String, SpriteImageDirective> spriteImageDirectivesBySpriteId,
        Multimap<String, SpriteReferenceOccurrence> spriteReferenceOccurrencesBySpriteId)
    {
        final Multimap<File, SpriteReferenceReplacement> spriteReplacementsByFile = Multimaps
            .newArrayListMultimap();
        for (final Map.Entry<String, Collection<SpriteReferenceOccurrence>> spriteReferenceOccurrences : spriteReferenceOccurrencesBySpriteId
            .asMap().entrySet())
        {
            final Map<SpriteReferenceOccurrence, SpriteReferenceReplacement> spriteReferenceReplacements = buildSpriteReplacements(
                spriteImageDirectivesBySpriteId.get(spriteReferenceOccurrences.getKey()),
                spriteReferenceOccurrences.getValue());

            for (final SpriteReferenceReplacement spriteReferenceReplacement : spriteReferenceReplacements
                .values())
            {
                spriteReplacementsByFile.put(
                    spriteReferenceReplacement.spriteReferenceOccurrence.cssFile,
                    spriteReferenceReplacement);
            }
        }

        return spriteReplacementsByFile;
    }

    /**
     * Builds sprite image for a single sprite image directive.
     */
    Map<SpriteReferenceOccurrence, SpriteReferenceReplacement> buildSpriteReplacements(
        SpriteImageDirective spriteImageDirective,
        Collection<SpriteReferenceOccurrence> spriteReferenceOccurrences)
    {
        // Take SpriteImageDescriptor from the first entry, they should be the same
        final SpriteReferenceOccurrence firstSpriteEntry = spriteReferenceOccurrences
            .iterator().next();

        // Load images into memory. TODO: impose some limit here?
        final Map<SpriteReferenceOccurrence, BufferedImage> images = Maps
            .newLinkedHashMap();
        for (final SpriteReferenceOccurrence spriteReferenceOccurrence : spriteReferenceOccurrences)
        {
            messageLog.setCssFile(spriteReferenceOccurrence.cssFile);
            messageLog.setLine(spriteReferenceOccurrence.line);

            final File imageFile = getImageFile(spriteReferenceOccurrence.cssFile,
                spriteReferenceOccurrence.imagePath, false);

            // Load image
            BufferedImage image;
            try
            {
                messageLog.info(MessageType.READING_IMAGE, imageFile.getName());
                image = ImageIO.read(imageFile);
            }
            catch (final IOException e)
            {
                messageLog.warning(MessageType.CANNOT_NOT_LOAD_IMAGE, FileUtils
                    .getCanonicalOrAbsolutePath(imageFile), e.getMessage());
                continue;
            }

            messageLog.setCssFile(null);

            images.put(spriteReferenceOccurrence, image);
        }

        final SpriteImageProperties spriteImageProperties = SpriteImageBuilder
            .buildSpriteImageProperties(spriteImageDirective, images);

        // Finally, build the sprite image
        // Create buffer for merged image
        final BufferedImage [] mergedImages = spriteImageMerger.buildMergedSpriteImage(
            images, spriteImageProperties);

        writeSprite(spriteImageDirective, firstSpriteEntry, mergedImages[0], false);
        if (mergedImages[1] != null)
        {
            // Write IE6 version if generated
            writeSprite(spriteImageDirective, firstSpriteEntry, mergedImages[1], true);
        }

        return spriteImageProperties.spriteReferenceReplacements;
    }

    /**
     * Writes sprite image to the disk.
     */
    private void writeSprite(SpriteImageDirective spriteImageDirective,
        final SpriteReferenceOccurrence firstSpriteEntry,
        final BufferedImage mergedImage, boolean ie6Reduced)
    {
        // Add IE6 suffix if needed
        String spritePath = addIe6Suffix(spriteImageDirective, ie6Reduced);

        // Save the image to the disk
        final File mergedImageFile = getImageFile(firstSpriteEntry.cssFile, spritePath,
            true);

        if (!mergedImageFile.getParentFile().exists())
        {
            mergedImageFile.getParentFile().mkdirs();
        }

        try
        {
            messageLog.info(MessageType.WRITING_SPRITE_IMAGE, mergedImage.getWidth(),
                mergedImage.getHeight(), spriteImageDirective.spriteId, mergedImageFile
                    .getName());
            ImageIO.write(mergedImage, spriteImageDirective.format.toString(),
                mergedImageFile);
        }
        catch (final IOException e)
        {
            messageLog.info(MessageType.CANNOT_WRITE_SPRITE_IMAGE, FileUtils
                .getCanonicalOrAbsolutePath(mergedImageFile), e.getMessage());
        }
    }

    /**
     * Adds IE6 suffix to the sprite image path for IE6 reduced images.
     */
    static String addIe6Suffix(SpriteImageDirective spriteImageDirective,
        boolean ie6Reduced)
    {
        String spritePath = spriteImageDirective.imagePath;
        if (ie6Reduced)
        {
            final int dotIndex = spritePath.lastIndexOf('.');
            if (dotIndex >= 0)
            {
                StringBuilder ie6Path = new StringBuilder(spritePath);
                ie6Path.insert(dotIndex, "-ie6");
                spritePath = ie6Path.toString();
            }
            else
            {
                spritePath = spritePath + "-ie6";
            }
        }
        return spritePath;
    }

    /**
     * Canonicalize the path returned from {@link #getImageFile0(File, String, boolean)}
     * for Linux and Unix systems. Paths that contain non-existing components, followed by
     * <code>/../</code> throw exceptions on such systems.
     */
    File getImageFile(File cssFile, String imagePath, boolean changeRoot)
    {
        return FileUtils.getCanonicalOrAbsoluteFile(getImageFile0(cssFile, imagePath,
            changeRoot));
    }

    /**
     * Returns the {@link File} for an imagePath. If the imagePath is relative, it's taken
     * relative to the cssFile. If imagePath is absolute (starts with '/') and
     * documentRootDir is not null, it's taken relative to documentRootDir.
     */
    File getImageFile0(File cssFile, String imagePath, boolean changeRoot)
    {
        if (imagePath.startsWith("/"))
        {
            if (parameters.getDocumentRootDir() != null)
            {
                return new File(parameters.getDocumentRootDir(), imagePath.substring(1));
            }
            else
            {
                messageLog.warning(MessageType.ABSOLUTE_PATH_AND_NO_DOCUMENT_ROOT,
                    imagePath);
            }
        }

        final File file = new File(cssFile.getParentFile(), imagePath);

        if (changeRoot && !imagePath.startsWith("/") && parameters.getOutputDir() != null)
        {
            return FileUtils.changeRoot(file, parameters.getRootDir(), parameters
                .getOutputDir());
        }
        else
        {
            return file;
        }
    }

    /**
     * Calculates total dimensions and lays out a single sprite image.
     */
    static SpriteImageProperties buildSpriteImageProperties(
        SpriteImageDirective spriteImageDirective,
        Map<SpriteReferenceOccurrence, BufferedImage> images)
    {
        // First find the least common multiple of the images with 'repeat' alignment
        final boolean verticalSprite = spriteImageDirective.layout
            .equals(SpriteImageLayout.VERTICAL);

        final int leastCommonMultiple = SpriteImageBuilder.calculateLeastCommonMultiple(
            images, verticalSprite);

        // Now compute sprite width and height and build sprite replacements
        int spriteWidth = (verticalSprite ? leastCommonMultiple : 0);
        int spriteHeight = (verticalSprite ? 0 : leastCommonMultiple);
        final Map<SpriteReferenceOccurrence, SpriteReferenceReplacement> spriteReplacements = Maps
            .newHashMap();

        for (final Map.Entry<SpriteReferenceOccurrence, BufferedImage> entry : images
            .entrySet())
        {
            // Compute dimensions
            final BufferedImage image = entry.getValue();
            final SpriteReferenceDirective spriteReferenceDirective = entry.getKey().spriteReferenceDirective;

            final int requiredWidth = image.getWidth()
                + spriteReferenceDirective.marginRight
                + spriteReferenceDirective.marginLeft;
            final int requiredHeight = image.getHeight()
                + spriteReferenceDirective.marginTop
                + spriteReferenceDirective.marginBottom;

            SpriteReferenceReplacement spriteReferenceReplacement;
            if (verticalSprite)
            {
                spriteReferenceReplacement = new SpriteReferenceReplacement(entry
                    .getKey(), spriteHeight, (spriteReferenceDirective.alignment
                    .equals(SpriteAlignment.RIGHT) ? "right" : "left"));

                spriteWidth = Math.max(spriteWidth, requiredWidth);

                // Correct for least common multiple
                if (spriteWidth % leastCommonMultiple != 0)
                {
                    spriteWidth += leastCommonMultiple
                        - (spriteWidth % leastCommonMultiple);
                }

                spriteHeight += requiredHeight;
            }
            else
            {
                spriteReferenceReplacement = new SpriteReferenceReplacement(entry
                    .getKey(), (spriteReferenceDirective.alignment
                    .equals(SpriteAlignment.BOTTOM) ? "bottom" : "top"), spriteWidth);

                spriteHeight = Math.max(spriteHeight, requiredHeight);

                // Correct for least common multiple
                if (spriteHeight % leastCommonMultiple != 0)
                {
                    spriteHeight += leastCommonMultiple
                        - (spriteHeight % leastCommonMultiple);
                }

                spriteWidth += requiredWidth;
            }

            spriteReplacements.put(entry.getKey(), spriteReferenceReplacement);
        }

        return new SpriteImageProperties(spriteWidth, spriteHeight, spriteImageDirective,
            spriteReplacements);
    }

    /**
     * Calculates the width/ height of "repeated" sprites.
     */
    static int calculateLeastCommonMultiple(
        Map<SpriteReferenceOccurrence, BufferedImage> images, final boolean verticalSprite)
    {
        int leastCommonMultiple = 1;
        for (final Map.Entry<SpriteReferenceOccurrence, BufferedImage> entry : images
            .entrySet())
        {
            if ((entry.getValue() != null)
                && entry.getKey().spriteReferenceDirective.alignment
                    .equals(SpriteAlignment.REPEAT))
            {
                if (verticalSprite)
                {
                    leastCommonMultiple = MathUtils.lcm(leastCommonMultiple, entry
                        .getValue().getWidth());
                }
                else
                {
                    leastCommonMultiple = MathUtils.lcm(leastCommonMultiple, entry
                        .getValue().getHeight());
                }
            }
        }
        return leastCommonMultiple;
    }

    /**
     * Groups {@link SpriteReferenceReplacement}s by the line number of their
     * corresponding directives.
     */
    static Map<Integer, SpriteReferenceReplacement> getSpriteReplacementsByLineNumber(
        Collection<SpriteReferenceReplacement> spriteReferenceReplacements)
    {
        final Map<Integer, SpriteReferenceReplacement> result = Maps.newHashMap();

        for (final SpriteReferenceReplacement spriteReferenceReplacement : spriteReferenceReplacements)
        {
            result.put(spriteReferenceReplacement.spriteReferenceOccurrence.line,
                spriteReferenceReplacement);
        }

        return result;
    }

    /**
     * Groups {@link SpriteImageOccurrence}s by the line number of their corresponding
     * directives.
     */
    static Map<Integer, SpriteImageOccurrence> getSpriteImageOccurrencesByLineNumber(
        Collection<SpriteImageOccurrence> spriteImageOccurrences)
    {
        final Map<Integer, SpriteImageOccurrence> result = Maps.newHashMap();

        for (final SpriteImageOccurrence spriteImageOccurrence : spriteImageOccurrences)
        {
            result.put(spriteImageOccurrence.line, spriteImageOccurrence);
        }

        return result;
    }

}
