package org.carrot2.labs.smartsprites;

/**
 * Represents the replacement that will be made for an individual
 * {@link SpriteReferenceOccurrence}.
 */
public class SpriteReferenceReplacement
{
    /** The {@link SpriteReferenceOccurrence} this instance refers to */
    public final SpriteReferenceOccurrence spriteReferenceOccurrence;

    /** CSS file relative path of the sprite image replacing the individual image */
    public final String spriteImageUrl;

    /** String representation of the horizontal position of this sprite replacement */
    public final String horizontalPositionString;

    /** Numeric representation of the horizontal position of this sprite replacement */
    public final int horizontalPosition;

    /** String representation of the vertical position of this sprite replacement */
    public final String verticalPositionString;

    /** Numeric representation of the vertical position of this sprite replacement */
    public final int verticalPosition;

    public SpriteReferenceReplacement(
        SpriteReferenceOccurrence spriteReferenceOccurrence, String imageUrl,
        int verticalPosition, String horizontalPosition)
    {
        this.spriteReferenceOccurrence = spriteReferenceOccurrence;
        this.spriteImageUrl = imageUrl;
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
        this.spriteImageUrl = imageUrl;
        this.horizontalPosition = horizontalPosition;
        this.horizontalPositionString = "-" + horizontalPosition + "px";
        this.verticalPosition = -1;
        this.verticalPositionString = verticalPosition;
    }
}
