package org.carrot2.labs.smartsprites.message;

/**
 * Collects {@link Message}s for further processing/ retrieval.
 */
public interface MessageSink
{
    /**
     * Adds a {@link Message} for further processing/ retrieval.
     */
    public void add(Message message);
}
