package org.carrot2.labs.smartsprites;

import java.util.Map;

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

    public SpriteImageProperties(int width, int height, boolean vertical,
        Map<SpriteReferenceOccurrence, SpriteReferenceReplacement> spriteReplacements)
    {
        this.width = width;
        this.height = height;
        this.vertical = vertical;
        this.spriteReferenceReplacements = spriteReplacements;
    }
}
