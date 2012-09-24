package org.carrot2.labs.smartsprites.message;

import org.carrot2.labs.smartsprites.message.Message.MessageLevel;

/**
 * Counts the number of messages logged with different levels.
 */
public class LevelCounterMessageSink implements MessageSink
{
    /** Number of info messages */
    private int infoCount = 0;

    /** Number of warning messages */
    private int warnCount = 0;

    public void add(Message message)
    {
        if (MessageLevel.INFO.equals(message.level))
        {
            infoCount++;
        }

        if (MessageLevel.WARN.equals(message.level))
        {
            warnCount++;
        }
    }

    public int getInfoCount()
    {
        return infoCount;
    }

    public int getWarnCount()
    {
        return warnCount;
    }
}
