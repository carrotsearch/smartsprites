package org.carrot2.labs.smartsprites.message;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Collects messages in a {@link List}, see {@link #messages}.
 */
public class MemoryMessageSink implements MessageSink
{
    /**
     * Contains collected messages.
     */
    public final List<Message> messages = Lists.newArrayList();

    public void add(Message message)
    {
        messages.add(message);
    }
}
