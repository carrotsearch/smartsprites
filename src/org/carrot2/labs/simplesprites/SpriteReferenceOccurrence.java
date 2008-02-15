package org.carrot2.labs.simplesprites;

import java.io.File;

/**
 * @author Stanislaw Osinski
 */
public class SpriteReferenceOccurrence extends SpriteDirectiveOccurrence
{
    public final SpriteReferenceDirective spriteReferenceDirective;
    public final String imagePath;

    public SpriteReferenceOccurrence(SpriteReferenceDirective spriteReferenceDirective,
        String imageFile, File cssFile, int line)
    {
        super(cssFile, line);
        this.spriteReferenceDirective = spriteReferenceDirective;
        this.imagePath = imageFile;
    }
}
