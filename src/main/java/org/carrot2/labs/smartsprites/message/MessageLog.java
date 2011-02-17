package org.carrot2.labs.smartsprites.message;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Gathers {@link Message}s during the runtime of the applications.
 */
public class MessageLog
{
    /** Current line in the CSS file */
    private int line = 0;

    /** CSS file currently processed */
    private String cssPath = null;

    /** Sinks for messages */
    private final List<MessageSink> sinks;

    /**
     * Creates a {@link MessageLog} with the provided {@link MessageSink}s.
     */
    public MessageLog(MessageSink... sinks)
    {
        this.sinks = Lists.newArrayList(sinks);
    }

    /**
     * Logs a message to this log.
     */
    public void log(Message.MessageLevel level, Message.MessageType type,
        Object... arguments)
    {
        for (final MessageSink sink : sinks)
        {
            sink.add(new Message(level, type, cssPath, line, arguments));
        }
    }

    /**
     * Logs an information message to this log.
     */
    public void info(Message.MessageType type, Object... arguments)
    {
        log(Message.MessageLevel.INFO, type, arguments);
    }

    /**
     * Logs a warning message to this log.
     */
    public void notice(Message.MessageType type, Object... arguments)
    {
        log(Message.MessageLevel.IE6NOTICE, type, arguments);
    }

    /**
     * Logs a warning message to this log.
     */
    public void warning(Message.MessageType type, Object... arguments)
    {
        log(Message.MessageLevel.WARN, type, arguments);
    }
    
    /**
     * Logs an error message to this log.
     */
    public void error(Message.MessageType type, Object... arguments)
    {
        log(Message.MessageLevel.ERROR, type, arguments);
    }

    /**
     * Logs a status message to this log.
     */
    public void status(Message.MessageType type, Object... arguments)
    {
        log(Message.MessageLevel.STATUS, type, arguments);
    }

    /**
     * Sets current CSS line for this log.
     */
    public void setLine(int line)
    {
        this.line = line;
    }
    
    public void setCssFile(String cssFilePath)
    {
        this.cssPath = cssFilePath;
    }
    
    /**
     * Adds a {@link MessageSink} to this log.
     */
    public void addMessageSink(MessageSink sink)
    {
        this.sinks.add(sink);
    }
}
