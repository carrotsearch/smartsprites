package org.carrot2.labs.smartsprites.layout;

import java.awt.image.BufferedImage;

import org.carrot2.labs.smartsprites.SpriteLayoutProperties.SpriteAlignment;
import org.carrot2.labs.smartsprites.SpriteReferenceDirective;
import org.carrot2.labs.smartsprites.SpriteReferenceOccurrence;
import org.carrot2.labs.smartsprites.SpriteReferenceReplacement;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.util.BufferedImageUtils;

public class VerticalLayout implements SpriteImageLayout {

    public SpriteAlignment correctAlignment(SpriteAlignment alignment, MessageLog messageCollector)
    {
        if (alignment.equals(SpriteAlignment.TOP) || alignment.equals(SpriteAlignment.BOTTOM))
        {
            messageCollector.warning(
                    MessageType.ONLY_LEFT_OR_RIGHT_ALIGNMENT_ALLOWED, alignment.toString());
            return SpriteAlignment.LEFT;
        }
        return alignment;
    }

    public SpriteAlignment getDefaultAlignment() {
        return SpriteAlignment.LEFT;
    }

    public SpriteReferenceReplacement buildReplacement(SpriteReferenceOccurrence spriteReferenceOccurrence, SpriteReferenceDirective spriteReferenceDirective, int offset) {
        String horizontalPosition;
        if (SpriteAlignment.RIGHT.equals(spriteReferenceDirective.spriteLayoutProperties.alignment)) 
        {
            horizontalPosition = "right";
        }
        else if (SpriteAlignment.CENTER
                .equals(spriteReferenceDirective.spriteLayoutProperties.alignment)) 
        {
            horizontalPosition = "center";
        }
        else
        {
            horizontalPosition = "left";
        }

        return new SpriteReferenceReplacement(
                spriteReferenceOccurrence,
                offset,
                horizontalPosition);
    }

    public BufferedImage render(BufferedImage image, SpriteReferenceOccurrence spriteReferenceOccurrence, SpriteReferenceDirective spriteReferenceDirective, int dimension) {
        final BufferedImage rendered = new BufferedImage(dimension, spriteReferenceOccurrence.getRequiredHeight(image, this),
                BufferedImage.TYPE_4BYTE_ABGR);

        if (SpriteAlignment.LEFT
                .equals(spriteReferenceDirective.spriteLayoutProperties.alignment))
        {
            BufferedImageUtils.drawImage(image, rendered,
                    spriteReferenceDirective.spriteLayoutProperties.marginLeft,
                    spriteReferenceDirective.spriteLayoutProperties.marginTop);
        }
        else if (SpriteAlignment.RIGHT
                .equals(spriteReferenceDirective.spriteLayoutProperties.alignment))
        {
            BufferedImageUtils.drawImage(image, rendered,
                    dimension
                    - spriteReferenceDirective.spriteLayoutProperties.marginRight
                    - image.getWidth(),
                    spriteReferenceDirective.spriteLayoutProperties.marginTop);
        }
        else if (SpriteAlignment.CENTER
                .equals(spriteReferenceDirective.spriteLayoutProperties.alignment))
        {
            BufferedImageUtils.drawImage(image, rendered,
                    (rendered.getWidth() - image.getWidth()) / 2,
                    spriteReferenceDirective.spriteLayoutProperties.marginTop);
        }
        else
        {
            // Repeat, ignoring margin-left and margin-right
            for (int x = 0; x < dimension; x += image.getWidth())
            {
                BufferedImageUtils.drawImage(image, rendered, x,
                        spriteReferenceDirective.spriteLayoutProperties.marginTop);
            }
        }
        return rendered;
    }

    public int getRequiredHeight(BufferedImage image, SpriteReferenceDirective spriteReferenceDirective) {
        return image.getHeight()
                + spriteReferenceDirective.spriteLayoutProperties.marginTop
                + spriteReferenceDirective.spriteLayoutProperties.marginBottom;
    }

    public int getRequiredWidth(BufferedImage image,
            SpriteReferenceDirective spriteReferenceDirective) {
        if (SpriteAlignment.REPEAT
                .equals(spriteReferenceDirective.spriteLayoutProperties.alignment))
        {
            // Ignoring left/right margins on repeated
            // images in vertically stacked sprites
            return image.getWidth();
        }
        else
        {
            return image.getWidth()
                    + spriteReferenceDirective.spriteLayoutProperties.marginLeft
                    + spriteReferenceDirective.spriteLayoutProperties.marginRight;
        }
    }
}