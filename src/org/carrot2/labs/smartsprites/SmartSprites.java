package org.carrot2.labs.smartsprites;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.PrintStreamMessageSink;

/**
 * The entry class for SmartSprites.
 */
public class SmartSprites
{
    /**
     * Entry point to SmartSprites. All parameters are passed as JVM properties.
     */
    public static void main(String [] args) throws FileNotFoundException, IOException
    {
        // Get parameters form system properties
        final SmartSpritesParameters parameters = new SmartSpritesParameters();
        final MessageLog messageLog = new MessageLog(new PrintStreamMessageSink(
            System.out, parameters.logLevel));
        new SpriteBuilder(parameters, messageLog).buildSprites();
    }
}
