package org.carrot2.labs.smartsprites;

import java.util.Map;

/**
 * @author Stanislaw Osinski
 */
public class SpriteImageProperties
{
    public final int width;
    public final int height;
    public final boolean vertical;
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
