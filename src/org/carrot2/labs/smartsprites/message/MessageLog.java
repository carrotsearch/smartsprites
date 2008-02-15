package org.carrot2.labs.smartsprites.message;

/**
 * @author Stanislaw Osinski
 */
public class MessageLog
{
    private int line = 0;
    private String cssPath = null;

    private final MessageSink [] sinks;

    public MessageLog(MessageSink... sinks)
    {
        this.sinks = sinks;
    }

    public void log(Message.MessageLevel level, Message.MessageType type,
        Object... arguments)
    {
        for (final MessageSink sink : sinks)
        {
            sink.add(new Message(level, type, cssPath, line, arguments));
        }
    }

    public void logInfo(Message.MessageType type, Object... arguments)
    {
        log(Message.MessageLevel.INFO, type, arguments);
    }

    public void logWarning(Message.MessageType type, Object... arguments)
    {
        log(Message.MessageLevel.WARN, type, arguments);
    }

    public void setLine(int line)
    {
        this.line = line;
    }

    public void setCssPath(String cssPath)
    {
        this.cssPath = cssPath;
    }
}
