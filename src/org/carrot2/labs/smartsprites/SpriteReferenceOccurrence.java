package org.carrot2.labs.smartsprites;

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
}
