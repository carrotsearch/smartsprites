package org.carrot2.labs.smartsprites;

import java.io.File;

/**
 * Represents an occurrence of a {@link SpriteReferenceDirective} in a specific CSS file.
 */
public class SpriteReferenceOccurrence extends SpriteDirectiveOccurrence
{
    /** The directive */
    public final SpriteReferenceDirective spriteReferenceDirective;

    /** CSS file relative path to the individual image to be added to a sprite. */
    public final String imagePath;

    public SpriteReferenceOccurrence(SpriteReferenceDirective spriteReferenceDirective,
        String imageFile, File cssFile, int line)
    {
        super(cssFile, line);
        this.spriteReferenceDirective = spriteReferenceDirective;
        this.imagePath = imageFile;
    }
}
