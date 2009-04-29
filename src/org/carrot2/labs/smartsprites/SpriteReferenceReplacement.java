package org.carrot2.labs.smartsprites;

/**
 * Represents the replacement that will be made for an individual
 * {@link SpriteReferenceOccurrence}.
 */
public class SpriteReferenceReplacement
{
    /** Properties of the sprite image this replacement refers to */
    public SpriteImageProperties spriteImageProperties;

    /** The {@link SpriteReferenceOccurrence} this instance refers to */
    public final SpriteReferenceOccurrence spriteReferenceOccurrence;

    /** String representation of the horizontal position of this sprite replacement */
    public final String horizontalPositionString;

    /** Numeric representation of the horizontal position of this sprite replacement */
    public final int horizontalPosition;

    /** String representation of the vertical position of this sprite replacement */
    public final String verticalPositionString;

    /** Numeric representation of the vertical position of this sprite replacement */
    public final int verticalPosition;

    public SpriteReferenceReplacement(
        SpriteReferenceOccurrence spriteReferenceOccurrence, int verticalPosition,
        String horizontalPosition)
    {
        this.spriteReferenceOccurrence = spriteReferenceOccurrence;
        this.horizontalPosition = -1;
        this.horizontalPositionString = horizontalPosition;
        this.verticalPosition = verticalPosition;
        this.verticalPositionString = "-" + verticalPosition + "px";
    }

    public SpriteReferenceReplacement(
        SpriteReferenceOccurrence spriteReferenceOccurrence, String verticalPosition,
        int horizontalPosition)
    {
        this.spriteReferenceOccurrence = spriteReferenceOccurrence;
        this.horizontalPosition = horizontalPosition;
        this.horizontalPositionString = "-" + horizontalPosition + "px";
        this.verticalPosition = -1;
        this.verticalPositionString = verticalPosition;
    }
}
