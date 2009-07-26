package org.carrot2.labs.smartsprites;

/**
 * A base class for SmartSprites directives.
 */
public class SpriteDirectiveOccurrence
{
    /** CSS file in which this directive was found */
    public final String cssFile;

    /** Line number on which the directive occurred */
    public final int line;

    public SpriteDirectiveOccurrence(String cssFile, int line)
    {
        this.cssFile = cssFile;
        this.line = line;
    }
}
