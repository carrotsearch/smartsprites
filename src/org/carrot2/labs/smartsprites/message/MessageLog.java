package org.carrot2.labs.smartsprites.message;

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
    private final MessageSink [] sinks;

    /**
     * Creates a {@link MessageLog} with the provided {@link MessageSink}s.
     */
    public MessageLog(MessageSink... sinks)
    {
        this.sinks = sinks;
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
    public void logInfo(Message.MessageType type, Object... arguments)
    {
        log(Message.MessageLevel.INFO, type, arguments);
    }

    /**
     * Logs a warning message to this log.
     */
    public void logWarning(Message.MessageType type, Object... arguments)
    {
        log(Message.MessageLevel.WARN, type, arguments);
    }

    /**
     * Sets current CSS line for this log.
     */
    public void setLine(int line)
    {
        this.line = line;
    }

    /**
     * Sets current CSS file path for this log.
     */
    public void setCssPath(String cssPath)
    {
        this.cssPath = cssPath;
    }
}
