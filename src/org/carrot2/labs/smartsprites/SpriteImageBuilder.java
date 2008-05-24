package org.carrot2.labs.smartsprites;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.math.util.MathUtils;
import org.carrot2.labs.smartsprites.SpriteImageDirective.SpriteImageFormat;
import org.carrot2.labs.smartsprites.SpriteImageDirective.SpriteImageLayout;
import org.carrot2.labs.smartsprites.SpriteReferenceDirective.SpriteAlignment;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.util.FileUtils;

import com.google.common.collect.*;

/**
 * Lays out and build sprite images based on the collected SmartSprites directives.
 */
public class SpriteImageBuilder
{
    /**
     * Builds all sprite images based on the collected directives.
     */
    static Multimap<File, SpriteReferenceReplacement> buildSpriteImages(
        Map<String, SpriteImageDirective> spriteImageDirectivesBySpriteId,
        Multimap<String, SpriteReferenceOccurrence> spriteReferenceOccurrencesBySpriteId,
        MessageLog messageLog)
    {
        final Multimap<File, SpriteReferenceReplacement> spriteReplacementsByFile = Multimaps
            .newArrayListMultimap();
        for (final Map.Entry<String, Collection<SpriteReferenceOccurrence>> spriteReferenceOccurrences : spriteReferenceOccurrencesBySpriteId
            .asMap().entrySet())
        {
            final Map<SpriteReferenceOccurrence, SpriteReferenceReplacement> spriteReferenceReplacements = SpriteImageBuilder
                .buildSpriteReplacements(spriteImageDirectivesBySpriteId
                    .get(spriteReferenceOccurrences.getKey()), spriteReferenceOccurrences
                    .getValue(), messageLog);

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
    static Map<SpriteReferenceOccurrence, SpriteReferenceReplacement> buildSpriteReplacements(
        SpriteImageDirective spriteImageDirective,
        Collection<SpriteReferenceOccurrence> spriteReferenceOccurrences,
        MessageLog messageLog)
    {
        // Take SpriteImageDescriptor from the first entry, they should be the same
        final SpriteReferenceOccurrence firstSpriteEntry = spriteReferenceOccurrences
            .iterator().next();

        // Load images into memory. TODO: impose some limit here?
        final Map<SpriteReferenceOccurrence, BufferedImage> images = Maps
            .newLinkedHashMap();
        for (final SpriteReferenceOccurrence spriteReferenceOccurrence : spriteReferenceOccurrences)
        {
            final File imageFile = new File(spriteReferenceOccurrence.cssFile
                .getParentFile(), spriteReferenceOccurrence.imagePath);

            messageLog.setCssPath(FileUtils
                .getCanonicalOrAbsolutePath(spriteReferenceOccurrence.cssFile));
            messageLog.setLine(spriteReferenceOccurrence.line);

            // Load image
            BufferedImage image;
            try
            {
                messageLog.logInfo(MessageType.READING_IMAGE, FileUtils
                    .getCanonicalOrAbsolutePath(imageFile));
                image = ImageIO.read(imageFile);
            }
            catch (final IOException e)
            {
                messageLog.logWarning(MessageType.CANNOT_NOT_LOAD_IMAGE, FileUtils
                    .getCanonicalOrAbsolutePath(imageFile), e.getMessage());
                continue;
            }

            messageLog.setCssPath(null);

            images.put(spriteReferenceOccurrence, image);
        }

        final SpriteImageProperties spriteImageProperties = SpriteImageBuilder
            .buildSpriteImageProperties(spriteImageDirective, images);

        // Finally, build the sprite image
        // Create buffer for merged image
        final BufferedImage mergedImage = SpriteImageBuilder.buildMergedSpriteImage(
            images, spriteImageProperties);

        // Save the image to the disk
        final File mergedImageFile = new File(firstSpriteEntry.cssFile.getParentFile(),
            spriteImageDirective.imagePath);
        if (!mergedImageFile.getParentFile().exists())
        {
            mergedImageFile.getParentFile().mkdirs();
        }

        try
        {
            messageLog.logInfo(MessageType.CREATING_SPRITE_IMAGE, mergedImage.getWidth(),
                mergedImage.getHeight(), spriteImageDirective.spriteId);
            ImageIO.write(mergedImage, spriteImageDirective.format.toString(),
                mergedImageFile);
        }
        catch (final IOException e)
        {
            messageLog.logInfo(MessageType.CANNOT_WRITE_SPRITE_IMAGE, FileUtils
                .getCanonicalOrAbsolutePath(mergedImageFile), e.getMessage());
        }

        return spriteImageProperties.spriteReferenceReplacements;
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
                spriteReferenceReplacement = new SpriteReferenceReplacement(
                    entry.getKey(),
                    spriteImageDirective.imagePath,
                    spriteHeight,
                    (spriteReferenceDirective.alignment.equals(SpriteAlignment.RIGHT) ? "right"
                        : "left"));

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
                spriteReferenceReplacement = new SpriteReferenceReplacement(
                    entry.getKey(),
                    spriteImageDirective.imagePath,
                    (spriteReferenceDirective.alignment.equals(SpriteAlignment.BOTTOM) ? "bottom"
                        : "top"), spriteWidth);

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
     * Builds the actual sprite image.
     */
    static BufferedImage buildMergedSpriteImage(
        Map<SpriteReferenceOccurrence, BufferedImage> images,
        SpriteImageProperties spriteImageProperties)
    {
        final int spriteHeight = spriteImageProperties.height;
        final int spriteWidth = spriteImageProperties.width;
        final boolean verticalSprite = spriteImageProperties.vertical;
        final Map<SpriteReferenceOccurrence, SpriteReferenceReplacement> spriteReferenceReplacements = spriteImageProperties.spriteReferenceReplacements;

        final BufferedImage mergedImage;
        if (!SpriteImageFormat.GIF
            .equals(spriteImageProperties.spriteImageDirective.format))
        {
            mergedImage = new BufferedImage(spriteWidth, spriteHeight,
                BufferedImage.TYPE_4BYTE_ABGR);
        }
        else
        {
            mergedImage = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration()
                .createCompatibleImage(spriteWidth, spriteHeight, Transparency.BITMASK);
        }

        final Graphics mergedGraphics = mergedImage.getGraphics();

        // Draw individual images into the merged one
        for (final Map.Entry<SpriteReferenceOccurrence, BufferedImage> entry : images
            .entrySet())
        {
            final BufferedImage image = entry.getValue();
            final SpriteReferenceDirective spriteReferenceDirective = entry.getKey().spriteReferenceDirective;

            final SpriteReferenceReplacement spriteReferenceReplacement = spriteReferenceReplacements
                .get(entry.getKey());

            if (verticalSprite)
            {
                if (spriteReferenceDirective.alignment.equals(SpriteAlignment.RIGHT))
                {
                    mergedGraphics.drawImage(image, spriteWidth - image.getWidth()
                        - spriteReferenceDirective.marginRight,
                        spriteReferenceReplacement.verticalPosition
                            + spriteReferenceDirective.marginTop, null);
                }
                else if (spriteReferenceDirective.alignment.equals(SpriteAlignment.LEFT))
                {
                    mergedGraphics.drawImage(image, spriteReferenceDirective.marginLeft,
                        spriteReferenceReplacement.verticalPosition
                            + spriteReferenceDirective.marginTop, null);
                }
                else
                {
                    // Repeat
                    for (int x = 0; x < spriteWidth; x += image.getWidth())
                    {
                        mergedGraphics.drawImage(image, x,
                            spriteReferenceReplacement.verticalPosition
                                + spriteReferenceDirective.marginTop, null);
                    }
                }
            }
            else
            {
                if (spriteReferenceDirective.alignment.equals(SpriteAlignment.BOTTOM))
                {
                    mergedGraphics.drawImage(image,
                        spriteReferenceReplacement.horizontalPosition
                            + spriteReferenceDirective.marginLeft, spriteHeight
                            - image.getHeight() - spriteReferenceDirective.marginBottom,
                        null);
                }
                else if (spriteReferenceDirective.alignment.equals(SpriteAlignment.TOP))
                {
                    mergedGraphics.drawImage(image,
                        spriteReferenceReplacement.horizontalPosition
                            + spriteReferenceDirective.marginLeft,
                        spriteReferenceDirective.marginTop, null);
                }
                else
                {
                    // Repeat
                    for (int y = 0; y < spriteHeight; y += image.getHeight())
                    {
                        mergedGraphics.drawImage(image,
                            spriteReferenceReplacement.horizontalPosition
                                + spriteReferenceDirective.marginLeft, y, null);
                    }
                }
            }
        }
        return mergedImage;
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
