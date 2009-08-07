package org.carrot2.labs.smartsprites;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Collection;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.math.util.MathUtils;
import org.carrot2.labs.smartsprites.SpriteImageDirective.SpriteImageLayout;
import org.carrot2.labs.smartsprites.SpriteReferenceDirective.SpriteAlignment;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.labs.smartsprites.resource.ResourceHandler;
import org.carrot2.util.CloseableUtils;
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

    /** The resource handler */
    private ResourceHandler resourceHandler;

    /**
     * Creates a {@link SpriteImageBuilder} with the provided parameters and log.
     */
    SpriteImageBuilder(SmartSpritesParameters parameters, MessageLog messageLog,
        ResourceHandler resourceHandler)
    {
        this.messageLog = messageLog;
        this.parameters = parameters;
        this.resourceHandler = resourceHandler;
        spriteImageMerger = new SpriteImageMerger(parameters, messageLog);
    }

    /**
     * Builds all sprite images based on the collected directives.
     */
    Multimap<String, SpriteReferenceReplacement> buildSpriteImages(
        Map<String, SpriteImageOccurrence> spriteImageOccurrencesBySpriteId,
        Multimap<String, SpriteReferenceOccurrence> spriteReferenceOccurrencesBySpriteId)
    {
        final Multimap<String, SpriteReferenceReplacement> spriteReplacementsByFile = LinkedListMultimap
            .create();
        for (final Map.Entry<String, Collection<SpriteReferenceOccurrence>> spriteReferenceOccurrences : spriteReferenceOccurrencesBySpriteId
            .asMap().entrySet())
        {
            final Map<SpriteReferenceOccurrence, SpriteReferenceReplacement> spriteReferenceReplacements = buildSpriteReplacements(
                spriteImageOccurrencesBySpriteId.get(spriteReferenceOccurrences.getKey()),
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
        SpriteImageOccurrence spriteImageOccurrence,
        Collection<SpriteReferenceOccurrence> spriteReferenceOccurrences)
    {
        // Load images into memory. TODO: impose some limit here?
        final Map<SpriteReferenceOccurrence, BufferedImage> images = Maps
            .newLinkedHashMap();
        for (final SpriteReferenceOccurrence spriteReferenceOccurrence : spriteReferenceOccurrences)
        {
            messageLog.setCssFile(spriteReferenceOccurrence.cssFile);
            messageLog.setLine(spriteReferenceOccurrence.line);

            final String realImagePath = resourceHandler.getResourcePath(
                spriteReferenceOccurrence.cssFile, spriteReferenceOccurrence.imagePath);
            InputStream is = null;
            try
            {
                is = resourceHandler.getResourceAsInputStream(realImagePath);

                // Load image
                if (is != null)
                {
                    messageLog.info(MessageType.READING_IMAGE, realImagePath);
                    images.put(spriteReferenceOccurrence, ImageIO.read(is));
                }
                else
                {
                    messageLog.warning(MessageType.CANNOT_NOT_LOAD_IMAGE, realImagePath,
                        "Can't read input file!");
                    continue;
                }
            }
            catch (final IOException e)
            {
                CloseableUtils.closeIgnoringException(is);
                messageLog.warning(MessageType.CANNOT_NOT_LOAD_IMAGE, realImagePath,
                    "Can't read input file!");
                continue;
            }

            messageLog.setCssFile(null);
        }

        final SpriteImageProperties spriteImageProperties = SpriteImageBuilder
            .buildSpriteImageProperties(spriteImageOccurrence.spriteImageDirective,
                images);

        // Finally, build the sprite image
        // Create buffer for merged image
        final BufferedImage [] mergedImages = spriteImageMerger.buildMergedSpriteImage(
            images, spriteImageProperties);

        writeSprite(spriteImageOccurrence, mergedImages[0], false);
        if (mergedImages[1] != null)
        {
            // Write IE6 version if generated
            writeSprite(spriteImageOccurrence, mergedImages[1], true);
        }

        return spriteImageProperties.spriteReferenceReplacements;
    }

    /**
     * Writes sprite image to the disk.
     */
    private void writeSprite(SpriteImageOccurrence spriteImageOccurrence,
        final BufferedImage mergedImage, boolean ie6Reduced)
    {
        // Add IE6 suffix if needed
        final SpriteImageDirective spriteImageDirective = spriteImageOccurrence.spriteImageDirective;
        final String spritePath = addIe6Suffix(spriteImageDirective, ie6Reduced);

        // Save the image to the disk
        final String mergedImageFile = getImageFile(spriteImageOccurrence.cssFile,
            spritePath);

        OutputStream spriteImageOuputStream = null;
        try
        {
            messageLog.info(MessageType.WRITING_SPRITE_IMAGE, mergedImage.getWidth(),
                mergedImage.getHeight(), spriteImageDirective.spriteId, mergedImageFile);
            spriteImageOuputStream = resourceHandler
                .getResourceAsOutputStream(mergedImageFile);
            ImageIO.write(mergedImage, spriteImageDirective.format.toString(),
                spriteImageOuputStream);
        }
        catch (final IOException e)
        {
            messageLog.warning(MessageType.CANNOT_WRITE_SPRITE_IMAGE, mergedImageFile, e
                .getMessage());
        }
        finally
        {
            CloseableUtils.closeIgnoringException(spriteImageOuputStream);
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
     * Computes the image path. If the imagePath is relative, it's taken relative to the
     * cssFile. If imagePath is absolute (starts with '/') and documentRootDir is not
     * null, it's taken relative to documentRootDir.
     */
    String getImageFile(String cssFile, String imagePath)
    {
        // Absolute path resolution is done by resourceHandler
        final String path = resourceHandler.getResourcePath(cssFile, imagePath);

        // Just handle the root directory changing
        if (!imagePath.startsWith("/") && parameters.hasOutputDir())
        {
            return FileUtils.changeRoot(path, parameters.getRootDir(), parameters
                .getOutputDir());
        }
        else
        {
            return path;
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
            .newLinkedHashMap();

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
