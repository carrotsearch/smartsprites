package org.carrot2.labs.smartsprites;

import java.util.List;

import org.carrot2.labs.smartsprites.message.*;
import org.junit.Before;

/**
 * A base class for tests checking logged messages.
 */
public abstract class TestWithMemoryMessageSink
{
    protected MessageLog messageLog;
    protected List<Message> messages;

    @Before
    public void setUpMessageLogWithMemoryMessageSink()
    {
        final MemoryMessageSink messageSink = new MemoryMessageSink();

        messageLog = new MessageLog(messageSink);
        messages = messageSink.messages;
    }
}
