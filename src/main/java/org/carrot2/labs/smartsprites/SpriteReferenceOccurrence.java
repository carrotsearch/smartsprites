package org.carrot2.labs.smartsprites;

import java.awt.image.BufferedImage;

import org.carrot2.labs.smartsprites.layout.SpriteImageLayout;

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
        return layout.getRequiredWidth(image, spriteReferenceDirective);
    }

    /**
     * Computes the minimum height the individual image will need when rendering.
     */
    public int getRequiredHeight(BufferedImage image, SpriteImageLayout layout)
    {
        return layout.getRequiredHeight(image, spriteReferenceDirective);
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
        return layout.render(image, this, spriteReferenceDirective, dimension);
    }

    /**
     * Returns the {@link SpriteReferenceReplacement} corresponding to the occurrence,
     * taking into account the layout the the enclosing sprite and the offset at which the
     * individual image was rendered.
     */
    public SpriteReferenceReplacement buildReplacement(SpriteImageLayout layout,
        int offset)
    {
        return layout.buildReplacement(this, spriteReferenceDirective, offset);
    }
}
