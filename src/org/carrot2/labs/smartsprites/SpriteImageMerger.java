package org.carrot2.labs.smartsprites;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Map;

import org.carrot2.labs.smartsprites.SmartSpritesParameters.PngDepth;
import org.carrot2.labs.smartsprites.SpriteImageDirective.Ie6Mode;
import org.carrot2.labs.smartsprites.SpriteImageDirective.SpriteImageFormat;
import org.carrot2.labs.smartsprites.SpriteReferenceDirective.SpriteAlignment;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message.MessageLevel;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.util.BufferedImageUtils;
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
    BufferedImage [] buildMergedSpriteImage(
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
    private BufferedImage [] render(BufferedImage sprite,
        SpriteImageProperties spriteImageProperties)
    {
        final boolean isPng = spriteImageProperties.spriteImageDirective.format == SpriteImageFormat.PNG;
        final boolean isJpg = spriteImageProperties.spriteImageDirective.format == SpriteImageFormat.JPG;

        final boolean isPngAuto = isPng && parameters.getSpritePngDepth() == PngDepth.AUTO;
        final boolean isPngDirect = isPng && parameters.getSpritePngDepth() == PngDepth.DIRECT;

        final ColorReductionInfo colorReductionInfo = ColorQuantizer
            .getColorReductionInfo(sprite);
        final boolean canReduceWithoutQualityLoss = colorReductionInfo
            .canReduceWithoutQualityLoss();

        final BufferedImage [] result = new BufferedImage [2];

        if (isPngDirect || (isPngAuto && !canReduceWithoutQualityLoss) || isJpg)
        {
            // Can't or no need to handle indexed color
            if (spriteImageProperties.spriteImageDirective.matteColor != null)
            {
                messageLog.warning(MessageType.IGNORING_MATTE_COLOR_NO_SUPPORT,
                    spriteImageProperties.spriteImageDirective.spriteId);
            }

            result[0] = sprite;

            // If needed, generate a quantized version for IE6. If the image has >255
            // colors but doesn't have any transparency, we don't need an IE6 version,
            // because IE6 can handle PNG24 with no transparency correctly.
            if (parameters.isSpritePngIe6() && isPng
                && BufferedImageUtils.hasTransparency(sprite)
                && spriteImageProperties.spriteImageDirective.ie6Mode != Ie6Mode.NONE)
            {
                result[1] = quantize(sprite, spriteImageProperties, colorReductionInfo,
                    MessageLevel.IE6NOTICE);
                spriteImageProperties.hasReducedForIe6 = true;
            }

            return result;
        }
        else if (canReduceWithoutQualityLoss)
        {
            // Can perform reduction to indexed color without data loss
            if (spriteImageProperties.spriteImageDirective.matteColor != null)
            {
                messageLog.warning(
                    MessageType.IGNORING_MATTE_COLOR_NO_PARTIAL_TRANSPARENCY,
                    spriteImageProperties.spriteImageDirective.spriteId);
            }
            result[0] = ColorQuantizer.reduce(sprite);
            return result;
        }
        else
        {
            result[0] = quantize(sprite, spriteImageProperties, colorReductionInfo,
                MessageLevel.WARN);
            return result;
        }
    }

    /**
     * Performs quantization, logs the appropriate messages if needed.
     */
    private BufferedImage quantize(BufferedImage sprite,
        SpriteImageProperties spriteImageProperties,
        final ColorReductionInfo colorReductionInfo, MessageLevel logLevel)
    {
        // Need to quantize
        if (colorReductionInfo.hasPartialTransparency)
        {
            messageLog.log(logLevel, MessageType.ALPHA_CHANNEL_LOSS_IN_INDEXED_COLOR,
                spriteImageProperties.spriteImageDirective.spriteId);
        }
        else
        {
            messageLog.log(logLevel, MessageType.TOO_MANY_COLORS_FOR_INDEXED_COLOR,
                spriteImageProperties.spriteImageDirective.spriteId,
                colorReductionInfo.distictColors, ColorQuantizer.MAX_INDEXED_COLORS);
        }

        final Color matte;
        if (spriteImageProperties.spriteImageDirective.matteColor != null)
        {
            matte = spriteImageProperties.spriteImageDirective.matteColor;
        }
        else
        {
            if (colorReductionInfo.hasPartialTransparency)
            {
                messageLog.log(logLevel, MessageType.USING_WHITE_MATTE_COLOR_AS_DEFAULT,
                    spriteImageProperties.spriteImageDirective.spriteId);
            }
            matte = Color.WHITE;
        }

        final BufferedImage quantized = ColorQuantizer.quantize(sprite, matte);
        return quantized;
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
