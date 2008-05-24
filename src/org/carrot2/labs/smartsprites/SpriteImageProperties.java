package org.carrot2.labs.smartsprites;

import java.util.Map;

import org.carrot2.labs.smartsprites.SpriteImageDirective.SpriteImageLayout;

/**
 * Properties of a single sprite image.
 */
public class SpriteImageProperties
{
    /** Total width of the sprite image */
    public final int width;

    /** Total height of the sprite image */
    public final int height;

    /** <code>true</code> if the layout is vertical, <code>false</code> otherwise. */
    public final boolean vertical;

    /**
     * All {@link SpriteReferenceReplacement}s corresponding to the individual images
     * this sprite image consists of.
     */
    public final Map<SpriteReferenceOccurrence, SpriteReferenceReplacement> spriteReferenceReplacements;

    /**
     * {@link SpriteImageDirective} for which this {@link SpriteImageProperties} has been
     * built. We need this reference here to properly save GIF transparency (sic!), see
     * {@link SpriteImageBuilder#buildMergedSpriteImage(Map, SpriteImageProperties)}.
     */
    public final SpriteImageDirective spriteImageDirective;

    public SpriteImageProperties(int width, int height,
        SpriteImageDirective spriteImageDirective,
        Map<SpriteReferenceOccurrence, SpriteReferenceReplacement> spriteReplacements)
    {
        this.width = width;
        this.height = height;
        this.spriteReferenceReplacements = spriteReplacements;
        this.spriteImageDirective = spriteImageDirective;
        
        this.vertical = SpriteImageLayout.VERTICAL.equals(spriteImageDirective.layout);
    }
}
