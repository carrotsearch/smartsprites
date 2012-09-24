package org.carrot2.labs.smartsprites;

import java.awt.image.BufferedImage;

import org.carrot2.labs.smartsprites.SpriteImageDirective.SpriteImageLayout;
import org.carrot2.labs.smartsprites.SpriteLayoutProperties.SpriteAlignment;
import org.carrot2.util.BufferedImageUtils;

/**
 * Represents an occurrence of a {@link SpriteReferenceDirective} in a specific CSS file.
 */
public class SpriteReferenceOccurrence extends SpriteDirectiveOccurrence
{
    /** The directive */
    public final SpriteReferenceDirective spriteReferenceDirective;

    /** CSS file relative path to the individual image to be added to a sprite. */
    public final String imagePath;

    /** Indicates whether the original css property has been marked as important */
    public final boolean important;

    public SpriteReferenceOccurrence(SpriteReferenceDirective spriteReferenceDirective,
        String imageFile, String cssFile, int line, boolean important)
    {
        super(cssFile, line);
        this.spriteReferenceDirective = spriteReferenceDirective;
        this.imagePath = imageFile;
        this.important = important;
    }

    /**
     * Computes the minimum width the individual image will need when rendering.
     */
    public int getRequiredWidth(BufferedImage image, SpriteImageLayout layout)
    {
        if (SpriteAlignment.REPEAT
            .equals(spriteReferenceDirective.spriteLayoutProperties.alignment)
            && SpriteImageLayout.VERTICAL.equals(layout))
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

    /**
     * Computes the minimum height the individual image will need when rendering.
     */
    public int getRequiredHeight(BufferedImage image, SpriteImageLayout layout)
    {
        if (SpriteAlignment.REPEAT
            .equals(spriteReferenceDirective.spriteLayoutProperties.alignment)
            && SpriteImageLayout.HORIZONTAL.equals(layout))
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

    /**
     * Renders the individual image, including margins and repeats if any.
     * 
     * @param image the individual image as read from the file
     * @param layout the layout the enclosing sprite
     * @param dimension height/width of a horizontal/vertical sprite
     * @return the rendered individual image
     */
    public BufferedImage render(BufferedImage image, SpriteImageLayout layout,
        int dimension)
    {
        final BufferedImage rendered;
        if (SpriteImageLayout.VERTICAL.equals(layout))
        {
            rendered = new BufferedImage(dimension, getRequiredHeight(image, layout),
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
            else
            {
                // Repeat, ignoring margin-left and margin-right
                for (int x = 0; x < dimension; x += image.getWidth())
                {
                    BufferedImageUtils.drawImage(image, rendered, x,
                        spriteReferenceDirective.spriteLayoutProperties.marginTop);
                }
            }
        }
        else
        {
            rendered = new BufferedImage(getRequiredWidth(image, layout), dimension,
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
            else
            {
                // Repeat, ignoring margin-top and margin-bottom
                for (int y = 0; y < dimension; y += image.getHeight())
                {
                    BufferedImageUtils.drawImage(image, rendered,
                        spriteReferenceDirective.spriteLayoutProperties.marginLeft, y);
                }
            }
        }
        return rendered;
    }

    /**
     * Returns the {@link SpriteReferenceReplacement} corresponding to the occurrence,
     * taking into account the layout the the enclosing sprite and the offset at which the
     * individual image was rendered.
     */
    public SpriteReferenceReplacement buildReplacement(SpriteImageLayout layout,
        int offset)
    {
        if (SpriteImageLayout.VERTICAL.equals(layout))
        {
            return new SpriteReferenceReplacement(
                this,
                offset,
                SpriteAlignment.RIGHT
                    .equals(spriteReferenceDirective.spriteLayoutProperties.alignment) ? "right"
                    : "left");
        }
        else
        {
            return new SpriteReferenceReplacement(
                this,
                SpriteAlignment.BOTTOM
                    .equals(spriteReferenceDirective.spriteLayoutProperties.alignment) ? "bottom"
                    : "top", offset);
        }
    }
}
