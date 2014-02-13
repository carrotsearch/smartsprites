package org.carrot2.labs.smartsprites.layout;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.carrot2.labs.smartsprites.SpriteImage;
import org.carrot2.labs.smartsprites.SpriteImageOccurrence;
import org.carrot2.labs.smartsprites.SpriteLayoutProperties.SpriteAlignment;
import org.carrot2.labs.smartsprites.SpriteReferenceDirective;
import org.carrot2.labs.smartsprites.SpriteReferenceOccurrence;
import org.carrot2.labs.smartsprites.SpriteReferenceReplacement;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.util.BufferedImageUtils;

import com.google.common.collect.Maps;

public class HorizontalLayout extends AbstractLayout {

    public SpriteAlignment correctAlignment(SpriteAlignment alignment, MessageLog messageCollector)
    {
        if (alignment.equals(SpriteAlignment.LEFT) || alignment.equals(SpriteAlignment.RIGHT))
        {
            messageCollector.warning(MessageType.ONLY_TOP_OR_BOTTOM_ALIGNMENT_ALLOWED, alignment.toString());
            return SpriteAlignment.TOP;
        }
        return alignment;
    }

    public SpriteAlignment getDefaultAlignment() {
        return SpriteAlignment.TOP;
    }

    public SpriteReferenceReplacement buildReplacement(SpriteReferenceOccurrence spriteReferenceOccurrence, SpriteReferenceDirective spriteReferenceDirective, int offset) {
        String verticalPosition;
        if (SpriteAlignment.BOTTOM
                .equals(spriteReferenceDirective.spriteLayoutProperties.alignment)) 
        {
            verticalPosition = "bottom";
        }
        else if (SpriteAlignment.CENTER
                .equals(spriteReferenceDirective.spriteLayoutProperties.alignment))
        {
            verticalPosition = "center";
        }
        else
        {
            verticalPosition = "top";
        }
        return new SpriteReferenceReplacement(
                spriteReferenceOccurrence,
                verticalPosition,
                offset);
    }

    public BufferedImage render(BufferedImage image, SpriteReferenceOccurrence spriteReferenceOccurrence, SpriteReferenceDirective spriteReferenceDirective, int dimension) {
        final BufferedImage rendered = new BufferedImage(spriteReferenceOccurrence.getRequiredWidth(image, this), dimension,
                BufferedImage.TYPE_4BYTE_ABGR);

        if (SpriteAlignment.TOP
                .equals(spriteReferenceDirective.spriteLayoutProperties.alignment))
        {
            BufferedImageUtils.drawImage(image, rendered,
                    spriteReferenceDirective.spriteLayoutProperties.marginLeft,
                    spriteReferenceDirective.spriteLayoutProperties.marginTop);
        }
        else if (SpriteAlignment.BOTTOM
                .equals(spriteReferenceDirective.spriteLayoutProperties.alignment))
        {
            BufferedImageUtils.drawImage(image, rendered,
                    spriteReferenceDirective.spriteLayoutProperties.marginLeft, dimension
                    - spriteReferenceDirective.spriteLayoutProperties.marginBottom
                    - image.getHeight());
        }
        else if (SpriteAlignment.CENTER
                .equals(spriteReferenceDirective.spriteLayoutProperties.alignment))
        {
            BufferedImageUtils.drawImage(image, rendered,
                    spriteReferenceDirective.spriteLayoutProperties.marginLeft, 
                    (rendered.getHeight() - image.getHeight()) / 2);
        }
        else
        {
            // Repeat, ignoring margin-top and margin-bottom
            for (int y = 0; y < dimension; y += image.getHeight())
            {
                BufferedImageUtils.drawImage(image, rendered,
                        spriteReferenceDirective.spriteLayoutProperties.marginLeft, y);
            }
        }
        return rendered;
    }

    public int getRequiredHeight(BufferedImage image, SpriteReferenceDirective spriteReferenceDirective) {
        if (SpriteAlignment.REPEAT
                .equals(spriteReferenceDirective.spriteLayoutProperties.alignment))
        {
            // Ignoring top/bottom margins on repeated
            // images in horizontally lined sprites
            return image.getHeight();
        }
        else
        {
            return image.getHeight()
                    + spriteReferenceDirective.spriteLayoutProperties.marginTop
                    + spriteReferenceDirective.spriteLayoutProperties.marginBottom;
        }
    }

    public int getRequiredWidth(BufferedImage image,
            SpriteReferenceDirective spriteReferenceDirective) {
        return image.getWidth()
                + spriteReferenceDirective.spriteLayoutProperties.marginLeft
                + spriteReferenceDirective.spriteLayoutProperties.marginRight;
    }

    protected List<Integer> getDimensions(Map<SpriteReferenceOccurrence, BufferedImage> images) {
        List<Integer> dimensions = new ArrayList<Integer>();
        for (final Map.Entry<SpriteReferenceOccurrence, BufferedImage> entry : images
                .entrySet())
        {
            final BufferedImage image = entry.getValue();
            final SpriteReferenceOccurrence spriteReferenceOccurrence = entry.getKey();
            if (image != null
                    && SpriteAlignment.REPEAT
                    .equals(spriteReferenceOccurrence.spriteReferenceDirective.spriteLayoutProperties.alignment))
            {
                dimensions.add(this.getRequiredHeight(image, spriteReferenceOccurrence.spriteReferenceDirective));
            }
        }
        return dimensions;
    }

    public SpriteImage buildSpriteImage(SpriteImageOccurrence spriteImageOccurrence, Map<SpriteReferenceOccurrence, BufferedImage> images, MessageLog messageLog)
    {
        // First find the least common multiple of the images with 'repeat' alignment
        final SpriteImageLayout layout = spriteImageOccurrence.spriteImageDirective.layout;
        final float spriteScale = spriteImageOccurrence.spriteImageDirective.scaleRatio;
        final int leastCommonMultiple = this.calculateRepeatAlignmentDimension(images);

        // Compute sprite dimension (width for vertical, height for horizontal sprites)
        int dimension = leastCommonMultiple;
        for (final Map.Entry<SpriteReferenceOccurrence, BufferedImage> entry : images
            .entrySet())
        {
            final BufferedImage image = entry.getValue();
            final SpriteReferenceOccurrence spriteReferenceOcurrence = entry.getKey();

            // Compute dimensions
            dimension = Math.max(dimension, spriteReferenceOcurrence.getRequiredHeight(image, layout));
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
                currentOffset += rendered.getWidth();
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
        final int spriteWidth = currentOffset;
        final int spriteHeight = dimension;
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

            BufferedImageUtils.drawImage(entry.getKey().image, sprite, entry.getValue(), 0);
        }

        return new SpriteImage(sprite, spriteImageOccurrence, spriteReplacements, spriteWidth, spriteHeight, spriteScale);
    }
}