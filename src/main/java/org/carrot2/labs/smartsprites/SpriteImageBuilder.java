package org.carrot2.labs.smartsprites;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.imageio.ImageIO;

import org.carrot2.labs.smartsprites.SpriteImageDirective.SpriteImageFormat;
import org.carrot2.labs.smartsprites.layout.SpriteImageLayout;
import org.carrot2.labs.smartsprites.layout.VerticalLayout;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.resource.ResourceHandler;
import org.carrot2.util.BufferedImageUtils;
import org.carrot2.util.FileUtils;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.io.Closeables;

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
    private SpriteImageRenderer spriteImageRenderer;

    /** The resource handler */
    private ResourceHandler resourceHandler;

    /**
     * A timestamp to use for timestamp-based sprite image UIDs. We need this time stamp
     * as a field to make sure the timestamp is the same for all sprite image
     * replacements.
     */
    private String timestamp;

    /**
     * Creates a {@link SpriteImageBuilder} with the provided parameters and log.
     */
    SpriteImageBuilder(SmartSpritesParameters parameters, MessageLog messageLog,
        ResourceHandler resourceHandler)
    {
        this.messageLog = messageLog;
        this.parameters = parameters;
        this.resourceHandler = resourceHandler;
        spriteImageRenderer = new SpriteImageRenderer(parameters, messageLog);
    }

    /**
     * Builds all sprite images based on the collected directives.
     */
    Multimap<String, SpriteReferenceReplacement> buildSpriteImages(
        Map<String, SpriteImageOccurrence> spriteImageOccurrencesBySpriteId,
        Multimap<String, SpriteReferenceOccurrence> spriteReferenceOccurrencesBySpriteId)
    {
        timestamp = Long.toString(new Date().getTime());

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
                    final BufferedImage image = ImageIO.read(is);
                    if (image != null)
                    {
                        images.put(spriteReferenceOccurrence, image);
                    }
                    else
                    {
                        messageLog.warning(
                            MessageType.UNSUPPORTED_INDIVIDUAL_IMAGE_FORMAT,
                            realImagePath);
                    }
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
                messageLog.warning(MessageType.CANNOT_NOT_LOAD_IMAGE, realImagePath,
                    "Can't read input file!");
                continue;
            }
            finally
            {
                Closeables.closeQuietly(is);
            }

            messageLog.setCssFile(null);
        }

        // Build the sprite image bitmap
        final SpriteImage spriteImage = SpriteImageBuilder.buildSpriteImage(
            spriteImageOccurrence, images, messageLog);
        if (spriteImage == null)
        {
            return Collections
                .<SpriteReferenceOccurrence, SpriteReferenceReplacement> emptyMap();
        }

        // Render the sprite into the required formats, perform quantization if needed
        final BufferedImage [] mergedImages = spriteImageRenderer.render(spriteImage);

        writeSprite(spriteImage, mergedImages[0], false);
        if (mergedImages[1] != null)
        {
            // Write IE6 version if generated
            writeSprite(spriteImage, mergedImages[1], true);
        }

        return spriteImage.spriteReferenceReplacements;
    }

    /**
     * Writes sprite image to the disk.
     */
    private void writeSprite(SpriteImage spriteImage, final BufferedImage mergedImage,
        boolean ie6Reduced)
    {
        final SpriteImageOccurrence spriteImageOccurrence = spriteImage.spriteImageOccurrence;
        final SpriteImageDirective spriteImageDirective = spriteImageOccurrence.spriteImageDirective;

        // Write the image to a byte array first. We need the data to compute an md5 hash.
        final ByteArrayOutputStream spriteImageByteArrayOuputStream = new ByteArrayOutputStream();

        // If writing to a JPEG, we need to make a 3-byte-encoded image
        final BufferedImage imageToWrite;
        if (SpriteImageFormat.JPG.equals(spriteImageDirective.format))
        {
            imageToWrite = new BufferedImage(mergedImage.getWidth(),
                mergedImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            BufferedImageUtils.drawImage(mergedImage, imageToWrite, 0, 0);
        }
        else
        {
            imageToWrite = mergedImage;
        }

        try
        {
            ImageIO.write(imageToWrite, spriteImageDirective.format.toString(),
                spriteImageByteArrayOuputStream);
        }
        catch (IOException e)
        {
            // Unlikely to happen.
            messageLog.warning(MessageType.CANNOT_WRITE_SPRITE_IMAGE,
                spriteImageDirective.imagePath, e.getMessage());
        }

        // Build file name
        byte [] spriteImageBytes = spriteImageByteArrayOuputStream.toByteArray();
        String resolvedImagePath = spriteImage.resolveImagePath(spriteImageBytes,
            timestamp, ie6Reduced);
        if (resolvedImagePath.indexOf('?') >= 0)
        {
            resolvedImagePath = resolvedImagePath.substring(0,
                resolvedImagePath.indexOf('?'));
        }

        // Save the image to the disk
        final String mergedImageFile = getImageFile(spriteImageOccurrence.cssFile,
            resolvedImagePath);

        OutputStream spriteImageOuputStream = null;
        try
        {
            messageLog.info(MessageType.WRITING_SPRITE_IMAGE, mergedImage.getWidth(),
                mergedImage.getHeight(), spriteImageDirective.spriteId, mergedImageFile);
            spriteImageOuputStream = resourceHandler
                .getResourceAsOutputStream(mergedImageFile);

            spriteImageOuputStream.write(spriteImageBytes);
        }
        catch (final IOException e)
        {
            messageLog.warning(MessageType.CANNOT_WRITE_SPRITE_IMAGE, mergedImageFile,
                e.getMessage());
        }
        finally
        {
            Closeables.closeQuietly(spriteImageOuputStream);
        }
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
            return FileUtils.changeRoot(path, parameters.getRootDir(),
                parameters.getOutputDir());
        }
        else
        {
            return path;
        }
    }

    /**
     * Calculates total dimensions and lays out a single sprite image.
     */
    static SpriteImage buildSpriteImage(SpriteImageOccurrence spriteImageOccurrence,
        Map<SpriteReferenceOccurrence, BufferedImage> images, MessageLog messageLog)
    {
        // First find the least common multiple of the images with 'repeat' alignment
        final SpriteImageLayout layout = spriteImageOccurrence.spriteImageDirective.layout;
        final float spriteScale = spriteImageOccurrence.spriteImageDirective.scaleRatio;
        final int leastCommonMultiple = SpriteImageBuilder.calculateLeastCommonMultiple(
            images, layout);

        // Compute sprite dimension (width for vertical, height for horizontal sprites)
        final boolean vertical = VerticalLayout.class.isAssignableFrom(layout.getClass());
        int dimension = leastCommonMultiple;
        for (final Map.Entry<SpriteReferenceOccurrence, BufferedImage> entry : images
            .entrySet())
        {
            final BufferedImage image = entry.getValue();
            final SpriteReferenceOccurrence spriteReferenceOcurrence = entry.getKey();

            // Compute dimensions
            dimension = Math.max(dimension,
                vertical ? spriteReferenceOcurrence.getRequiredWidth(image, layout)
                    : spriteReferenceOcurrence.getRequiredHeight(image, layout));
        }

        // Correct for least common multiple
        if (dimension % leastCommonMultiple != 0)
        {
            dimension += leastCommonMultiple - (dimension % leastCommonMultiple);
        }

        // Compute the other sprite dimension.
        int currentOffset = 0;
        final Map<SpriteReferenceOccurrence, SpriteReferenceReplacement> spriteReplacements = Maps
            .newLinkedHashMap();
        final Map<BufferedImageEqualsWrapper, Integer> renderedImageToOffset = Maps
            .newLinkedHashMap();
        for (final Map.Entry<SpriteReferenceOccurrence, BufferedImage> entry : images
            .entrySet())
        {
            final SpriteReferenceOccurrence spriteReferenceOccurrence = entry.getKey();
            final BufferedImage image = entry.getValue();

            final BufferedImage rendered = spriteReferenceOccurrence.render(image,
                layout, dimension);
            final BufferedImageEqualsWrapper imageWrapper = new BufferedImageEqualsWrapper(
                rendered);
            Integer imageOffset = renderedImageToOffset.get(imageWrapper);
            if (imageOffset == null)
            {
                // Draw a new image
                imageOffset = currentOffset;
                renderedImageToOffset.put(imageWrapper, imageOffset);
                currentOffset += vertical ? rendered.getHeight() : rendered.getWidth();
            }

            final float scaledImageWidth = spriteReferenceOccurrence.getRequiredWidth(image, layout) / spriteScale;
            final float scaledImageHeight = spriteReferenceOccurrence.getRequiredHeight(image, layout) / spriteScale;
            if (Math.round(scaledImageWidth) != scaledImageWidth ||
                Math.round(scaledImageHeight) != scaledImageHeight)
            {
                messageLog.warning(MessageType.IMAGE_FRACTIONAL_SCALE_VALUE,
                    spriteReferenceOccurrence.imagePath, scaledImageWidth, scaledImageHeight);
            }

            final int adjustedImageOffset = Math.round(imageOffset / spriteScale);
            spriteReplacements.put(spriteReferenceOccurrence,
                spriteReferenceOccurrence.buildReplacement(layout, adjustedImageOffset));
        }

        // Render the sprite image and build sprite reference replacements
        final int spriteWidth = vertical ? dimension : currentOffset;
        final int spriteHeight = vertical ? currentOffset : dimension;
        if (spriteWidth == 0 || spriteHeight == 0)
        {
            return null;
        }

        final float scaledWidth = spriteWidth / spriteScale;
        final float scaledHeight = spriteHeight / spriteScale;
        if (Math.round(scaledWidth) != scaledWidth ||
            Math.round(scaledHeight) != scaledHeight)
        {
            messageLog.warning(MessageType.FRACTIONAL_SCALE_VALUE,
                spriteImageOccurrence.spriteImageDirective.spriteId, scaledWidth, scaledHeight);
        }

        final BufferedImage sprite = new BufferedImage(spriteWidth, spriteHeight,
            BufferedImage.TYPE_4BYTE_ABGR);

        for (final Map.Entry<BufferedImageEqualsWrapper, Integer> entry : renderedImageToOffset
            .entrySet())
        {

            BufferedImageUtils.drawImage(entry.getKey().image, sprite, vertical ? 0
                : entry.getValue(), vertical ? entry.getValue() : 0);
        }

        return new SpriteImage(sprite, spriteImageOccurrence, spriteReplacements, spriteWidth, spriteHeight, spriteScale);
    }

    /**
     * Calculates the width/ height of "repeated" sprites.
     */
    static int calculateLeastCommonMultiple(
        Map<SpriteReferenceOccurrence, BufferedImage> images, SpriteImageLayout layout)
    {
        return layout.calculateRepeatAlignmentDimension(images);
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

    /**
     * A wrapper that implements content-aware {@link Object#equals(Object)} and
     * {@link Object#hashCode()} on {@link BufferedImage}s.
     */
    static final class BufferedImageEqualsWrapper
    {
        BufferedImage image;

        BufferedImageEqualsWrapper(BufferedImage image)
        {
            this.image = image;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof BufferedImageEqualsWrapper))
            {
                return false;
            }

            if (obj == this)
            {
                return true;
            }

            final BufferedImage other = ((BufferedImageEqualsWrapper) obj).image;

            boolean equal = other.getWidth() == image.getWidth()
                && other.getHeight() == other.getHeight()
                && other.getType() == image.getType();

            if (equal)
            {
                for (int y = 0; y < image.getHeight(); y++)
                {
                    for (int x = 0; x < image.getWidth(); x++)
                    {
                        if (ignoreFullTransparency(image.getRGB(x, y)) != ignoreFullTransparency(other
                            .getRGB(x, y)))
                        {
                            return false;
                        }
                    }
                }
            }

            return equal;
        }

        @Override
        public int hashCode()
        {
            if (image == null)
            {
                return 0;
            }

            int hash = image.getWidth() ^ (image.getHeight() << 16);

            // Computes the hashCode based on an 4 x 4 to 7 x 7 grid of image's pixels
            final int xIncrement = image.getWidth() > 7 ? image.getWidth() >> 2 : 1;
            final int yIncrement = image.getHeight() > 7 ? image.getHeight() >> 2 : 1;

            for (int y = 0; y < image.getHeight(); y += yIncrement)
            {
                for (int x = 0; x < image.getWidth(); x += xIncrement)
                {
                    hash ^= ignoreFullTransparency(image.getRGB(x, y));
                }
            }

            return hash;
        }

        /**
         * If the pixel is fully transparent, returns 0. Otherwise, returns the pixel.
         * This is useful in {@link #equals(Object)} and {@link #hashCode()} to ignore
         * pixels that have different colors but are invisible anyway because of full
         * transparency.
         */
        private static int ignoreFullTransparency(int pixel)
        {
            if ((pixel & 0xff000000) == 0x00000000)
            {
                return 0;
            }
            else
            {
                return pixel;
            }
        }
    }
}
