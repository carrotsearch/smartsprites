package org.carrot2.labs.smartsprites;

import java.io.File;

/**
 * Describes an occurrence of a {@link SpriteImageDirective} in a specific CSS file.
 */
public class SpriteImageOccurrence extends SpriteDirectiveOccurrence
{
    public final SpriteImageDirective spriteImageDirective;

    public SpriteImageOccurrence(SpriteImageDirective spriteImageDirective, File cssFile,
        int line)
    {
        super(cssFile, line);
        this.spriteImageDirective = spriteImageDirective;
    }
}
