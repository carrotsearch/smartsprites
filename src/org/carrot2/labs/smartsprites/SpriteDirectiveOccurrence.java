package org.carrot2.labs.smartsprites;

import java.io.File;

/**
 * A base class for SmartSprites directives/
 */
public class SpriteDirectiveOccurrence
{
    /** CSS file in which this directive was found */
    public final File cssFile;

    /** Line number on which the directive occurred */
    public final int line;

    public SpriteDirectiveOccurrence(File cssFile, int line)
    {
        this.cssFile = cssFile;
        this.line = line;
    }
}
