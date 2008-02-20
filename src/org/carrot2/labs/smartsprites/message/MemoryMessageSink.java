package org.carrot2.labs.smartsprites.message;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Colects messages in a {@list Link}, see {@link #messages}.
 */
public class MemoryMessageSink implements MessageSink
{
    /**
     * Contains collected messages.
     */
    public final List<Message> messages = Lists.newArrayList();

    @Override
    public void add(Message message)
    {
        messages.add(message);
    }
}
