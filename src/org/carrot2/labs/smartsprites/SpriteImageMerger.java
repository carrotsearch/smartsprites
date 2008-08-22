package org.carrot2.labs.smartsprites;

import java.awt.image.BufferedImage;
import java.util.Map;

import org.carrot2.labs.smartsprites.SmartSpritesParameters.PngDepth;
import org.carrot2.labs.smartsprites.SpriteImageDirective.SpriteImageFormat;
import org.carrot2.labs.smartsprites.SpriteReferenceDirective.SpriteAlignment;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.util.ColorQuantizer;
import org.carrot2.util.ColorQuantizer.ColorReductionInfo;

/**
 * Performs merging of individual images into sprites, applies color quantization if
 * required.
 */
public class SpriteImageMerger
{
    /** This builder's configuration */
    public final SmartSpritesParameters parameters;

    /** This builder's message log */
    private final MessageLog messageLog;

    SpriteImageMerger(SmartSpritesParameters parameters, MessageLog messageLog)
    {
        this.parameters = parameters;
        this.messageLog = messageLog;
    }

    /**
     * Builds the actual sprite image and color table if necessary
     */
    BufferedImage buildMergedSpriteImage(
        Map<SpriteReferenceOccurrence, BufferedImage> images,
        SpriteImageProperties spriteImageProperties)
    {
        final int spriteHeight = spriteImageProperties.height;
        final int spriteWidth = spriteImageProperties.width;
        final boolean verticalSprite = spriteImageProperties.vertical;
        final Map<SpriteReferenceOccurrence, SpriteReferenceReplacement> spriteReferenceReplacements = spriteImageProperties.spriteReferenceReplacements;

        final BufferedImage mergedImage = new BufferedImage(spriteWidth, spriteHeight,
            BufferedImage.TYPE_4BYTE_ABGR);

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
                    drawImage(image, mergedImage, spriteWidth - image.getWidth()
                        - spriteReferenceDirective.marginRight,
                        spriteReferenceReplacement.verticalPosition
                            + spriteReferenceDirective.marginTop);
                }
                else if (spriteReferenceDirective.alignment.equals(SpriteAlignment.LEFT))
                {

                    drawImage(image, mergedImage, spriteReferenceDirective.marginLeft,
                        spriteReferenceReplacement.verticalPosition
                            + spriteReferenceDirective.marginTop);
                }
                else
                {
                    // Repeat
                    for (int x = 0; x < spriteWidth; x += image.getWidth())
                    {
                        drawImage(image, mergedImage, x,
                            spriteReferenceReplacement.verticalPosition
                                + spriteReferenceDirective.marginTop);
                    }
                }
            }
            else
            {
                if (spriteReferenceDirective.alignment.equals(SpriteAlignment.BOTTOM))
                {
                    drawImage(image, mergedImage,
                        spriteReferenceReplacement.horizontalPosition
                            + spriteReferenceDirective.marginLeft, spriteHeight
                            - image.getHeight() - spriteReferenceDirective.marginBottom);
                }
                else if (spriteReferenceDirective.alignment.equals(SpriteAlignment.TOP))
                {
                    drawImage(image, mergedImage,
                        spriteReferenceReplacement.horizontalPosition
                            + spriteReferenceDirective.marginLeft,
                        spriteReferenceDirective.marginTop);
                }
                else
                {
                    // Repeat
                    for (int y = 0; y < spriteHeight; y += image.getHeight())
                    {
                        drawImage(image, mergedImage,
                            spriteReferenceReplacement.horizontalPosition
                                + spriteReferenceDirective.marginLeft, y);
                    }
                }
            }
        }

        return render(mergedImage, spriteImageProperties);
    }

    /**
     * If needed, quantizes the image.
     */
    private BufferedImage render(BufferedImage sprite,
        SpriteImageProperties spriteImageProperties)
    {
        final boolean isPng = spriteImageProperties.spriteImageDirective.format == SpriteImageFormat.PNG;
        final boolean isJpg = spriteImageProperties.spriteImageDirective.format == SpriteImageFormat.JPG;

        final boolean isPngAuto = isPng && parameters.spritePngDepth == PngDepth.AUTO;
        final boolean isPngDirect = isPng && parameters.spritePngDepth == PngDepth.DIRECT;

        final ColorReductionInfo colorReductionInfo = ColorQuantizer
            .getColorReductionInfo(sprite);
        final boolean canReduceWithoutQualityLoss = colorReductionInfo
            .canReduceWithoutQualityLoss();

        if (isPngDirect || (isPngAuto && !canReduceWithoutQualityLoss) || isJpg)
        {
            // Can't or no need to handle indexed color
            return sprite;
        }
        else if (canReduceWithoutQualityLoss)
        {
            // Can perform reduction to indexed color without data loss
            return ColorQuantizer.reduce(sprite);
        }
        else
        {
            // Need to quantize
            if (colorReductionInfo.hasFullAlphaTransparency)
            {
                messageLog.warning(MessageType.ALPHA_CHANNEL_LOSS_IN_INDEXED_COLOR,
                    spriteImageProperties.spriteImageDirective.spriteId);
            }
            else
            {
                messageLog.warning(MessageType.TOO_MANY_COLORS_FOR_INDEXED_COLOR,
                    spriteImageProperties.spriteImageDirective.spriteId,
                    colorReductionInfo.distictColors, ColorQuantizer.MAX_INDEXED_COLORS);
            }
            return ColorQuantizer.quantize(sprite);
        }
    }

    protected static void drawImage(BufferedImage image, BufferedImage sprite, int x,
        int y)
    {
        final int [] imgRGB = image.getRGB(0, 0, image.getWidth(), image.getHeight(),
            null, 0, image.getWidth());
        sprite.setRGB(x, y, image.getWidth(), image.getHeight(), imgRGB, 0, image
            .getWidth());
    }
}
