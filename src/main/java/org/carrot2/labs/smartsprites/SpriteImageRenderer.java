package org.carrot2.labs.smartsprites;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.carrot2.labs.smartsprites.SmartSpritesParameters.PngDepth;
import org.carrot2.labs.smartsprites.SpriteImageDirective.Ie6Mode;
import org.carrot2.labs.smartsprites.SpriteImageDirective.SpriteImageFormat;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message.MessageLevel;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.util.BufferedImageUtils;
import org.carrot2.util.ColorQuantizer;
import org.carrot2.util.ColorQuantizer.ColorReductionInfo;

/**
 * Applies color quantization to the merged sprite image if required.
 */
public class SpriteImageRenderer
{
    /** This builder's configuration */
    public final SmartSpritesParameters parameters;

    /** This builder's message log */
    private final MessageLog messageLog;

    SpriteImageRenderer(SmartSpritesParameters parameters, MessageLog messageLog)
    {
        this.parameters = parameters;
        this.messageLog = messageLog;
    }

    /**
     * If needed, quantizes the image.
     */
    BufferedImage [] render(SpriteImage spriteImage)
    {
        final BufferedImage sprite = spriteImage.sprite;
        final SpriteImageDirective spriteImageDirective = spriteImage.spriteImageOccurrence.spriteImageDirective;
        final boolean isPng = spriteImageDirective.format == SpriteImageFormat.PNG;
        final boolean isJpg = spriteImageDirective.format == SpriteImageFormat.JPG;

        final boolean isPngAuto = isPng
            && parameters.getSpritePngDepth() == PngDepth.AUTO;
        final boolean isPngDirect = isPng
            && parameters.getSpritePngDepth() == PngDepth.DIRECT;

        final ColorReductionInfo colorReductionInfo = ColorQuantizer
            .getColorReductionInfo(sprite);
        final boolean canReduceWithoutQualityLoss = colorReductionInfo
            .canReduceWithoutQualityLoss();

        final BufferedImage [] result = new BufferedImage [2];

        if (isPngDirect || (isPngAuto && !canReduceWithoutQualityLoss) || isJpg)
        {
            result[0] = sprite;

            // If needed, generate a quantized version for IE6. If the image has >255
            // colors but doesn't have any transparency, we don't need an IE6 version,
            // because IE6 can handle PNG24 with no transparency correctly.
            if (parameters.isSpritePngIe6() && isPng
                && BufferedImageUtils.hasTransparency(sprite)
                && spriteImageDirective.ie6Mode != Ie6Mode.NONE)
            {
                result[1] = quantize(sprite, spriteImage, colorReductionInfo,
                    MessageLevel.IE6NOTICE);
                spriteImage.hasReducedForIe6 = true;
            }
            else if (spriteImageDirective.matteColor != null)
            {
                // Can't or no need to handle indexed color
                messageLog.warning(MessageType.IGNORING_MATTE_COLOR_NO_SUPPORT,
                    spriteImageDirective.spriteId);
            }

            return result;
        }
        else if (canReduceWithoutQualityLoss)
        {
            // Can perform reduction to indexed color without data loss
            if (spriteImageDirective.matteColor != null)
            {
                messageLog.warning(
                    MessageType.IGNORING_MATTE_COLOR_NO_PARTIAL_TRANSPARENCY,
                    spriteImageDirective.spriteId);
            }
            result[0] = ColorQuantizer.reduce(sprite);
            return result;
        }
        else
        {
            result[0] = quantize(sprite, spriteImage, colorReductionInfo,
                MessageLevel.WARN);
            return result;
        }
    }

    /**
     * Performs quantization, logs the appropriate messages if needed.
     */
    private BufferedImage quantize(BufferedImage sprite, SpriteImage spriteImage,
        final ColorReductionInfo colorReductionInfo, MessageLevel logLevel)
    {
        final SpriteImageDirective spriteImageDirective = spriteImage.spriteImageOccurrence.spriteImageDirective;

        // Need to quantize
        if (colorReductionInfo.hasPartialTransparency)
        {
            messageLog.log(logLevel, MessageType.ALPHA_CHANNEL_LOSS_IN_INDEXED_COLOR,
                spriteImageDirective.spriteId);
        }
        else
        {
            messageLog.log(logLevel, MessageType.TOO_MANY_COLORS_FOR_INDEXED_COLOR,
                spriteImageDirective.spriteId,
                colorReductionInfo.distictColors, ColorQuantizer.MAX_INDEXED_COLORS);
        }

        final Color matte;
        if (spriteImageDirective.matteColor != null)
        {
            matte = spriteImageDirective.matteColor;
        }
        else
        {
            if (colorReductionInfo.hasPartialTransparency)
            {
                messageLog.log(logLevel, MessageType.USING_WHITE_MATTE_COLOR_AS_DEFAULT,
                    spriteImageDirective.spriteId);
            }
            matte = Color.WHITE;
        }

        return ColorQuantizer.quantize(sprite, matte);
    }
}
