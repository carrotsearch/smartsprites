package org.carrot2.labs.simplesprites.message;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * @author Stanislaw Osinski
 */
public class MemoryMessageSink implements MessageSink
{
    public final List<Message> messages = Lists.newArrayList();

    @Override
    public void add(Message message)
    {
        messages.add(message);
    }
}
