package org.carrot2.labs.smartsprites.layout;

import java.awt.image.BufferedImage;

import org.carrot2.labs.smartsprites.SpriteLayoutProperties.SpriteAlignment;
import org.carrot2.labs.smartsprites.SpriteReferenceDirective;
import org.carrot2.labs.smartsprites.SpriteReferenceOccurrence;
import org.carrot2.labs.smartsprites.SpriteReferenceReplacement;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.util.BufferedImageUtils;

public class HorizontalLayout implements SpriteImageLayout {

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
}