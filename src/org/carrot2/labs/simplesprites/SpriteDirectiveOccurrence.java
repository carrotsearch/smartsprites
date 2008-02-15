package org.carrot2.labs.simplesprites;

import java.io.File;

/**
 * @author Stanislaw Osinski
 */
public class SpriteDirectiveOccurrence
{
    public final File cssFile;
    public final int line;

    public SpriteDirectiveOccurrence(File cssFile, int line)
    {
        this.cssFile = cssFile;
        this.line = line;
    }
}
