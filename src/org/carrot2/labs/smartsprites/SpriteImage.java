package org.carrot2.labs.smartsprites;

import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * A merged sprite image consisting of a number of individual images.
 */
public class SpriteImage
{
    /** The rendered sprite image bitmap */
    public final BufferedImage sprite;

    /**
     * All {@link SpriteReferenceReplacement}s corresponding to the individual images this
     * sprite image consists of.
     */
    public final Map<SpriteReferenceOccurrence, SpriteReferenceReplacement> spriteReferenceReplacements;

    /**
     * {@link SpriteImageDirective} for which this {@link SpriteImage} has been
     * built.
     */
    public final SpriteImageDirective spriteImageDirective;

    /**
     * Indicates whether this sprite has been also generated in an alpha/color degraded
     * version for IE6;
     */
    public boolean hasReducedForIe6 = false;

    public SpriteImage(BufferedImage sprite,
        SpriteImageDirective spriteImageDirective,
        Map<SpriteReferenceOccurrence, SpriteReferenceReplacement> spriteReplacements)
    {
        this.sprite = sprite;
        this.spriteReferenceReplacements = spriteReplacements;
        this.spriteImageDirective = spriteImageDirective;

        for (SpriteReferenceReplacement replacement : spriteReplacements.values())
        {
            replacement.spriteImage = this;
        }
    }
}
