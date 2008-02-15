package org.carrot2.labs.simplesprites;

import java.util.Collection;

import com.google.common.collect.Lists;

/**
 * @author Stanislaw Osinski
 */
public class SpriteReferenceReplacement
{
    public final SpriteReferenceOccurrence spriteReferenceOccurrence;

    public final String imageUrl;
    public final String horizontalPositionString;
    public final int horizontalPosition;
    public final String verticalPositionString;
    public final int verticalPosition;

    public SpriteReferenceReplacement(
        SpriteReferenceOccurrence spriteReferenceOccurrence, String imageUrl,
        int verticalPosition, String horizontalPosition)
    {
        this.spriteReferenceOccurrence = spriteReferenceOccurrence;
        this.imageUrl = imageUrl;
        this.horizontalPosition = -1;
        this.horizontalPositionString = horizontalPosition;
        this.verticalPosition = verticalPosition;
        this.verticalPositionString = "-" + verticalPosition + "px";
    }

    public SpriteReferenceReplacement(
        SpriteReferenceOccurrence spriteReferenceOccurrence, String imageUrl,
        String verticalPosition, int horizontalPosition)
    {
        this.spriteReferenceOccurrence = spriteReferenceOccurrence;
        this.imageUrl = imageUrl;
        this.horizontalPosition = horizontalPosition;
        this.horizontalPositionString = "-" + horizontalPosition + "px";
        this.verticalPosition = -1;
        this.verticalPositionString = verticalPosition;
    }

    static Collection<SpriteReferenceOccurrence> asSpriteReferenceOccurrences(
        Collection<SpriteReferenceReplacement> spriteReferenceReplacements)
    {
        final Collection<SpriteReferenceOccurrence> result = Lists.newArrayList();

        for (final SpriteReferenceReplacement spriteReferenceReplacement : spriteReferenceReplacements)
        {
            result.add(spriteReferenceReplacement.spriteReferenceOccurrence);
        }

        return result;
    }
}
