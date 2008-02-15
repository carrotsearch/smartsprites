package org.carrot2.labs.simplesprites.message;

import java.io.PrintStream;

/**
 * @author Stanislaw Osinski
 */
public class PrintStreamMessageSink implements MessageSink
{
    private final PrintStream printStream;

    public PrintStreamMessageSink(PrintStream printStream)
    {
        this.printStream = printStream;
    }

    @Override
    public void add(Message message)
    {
        printStream.print(message.level + ": ");
        printStream.printf(message.type.getText(), message.arguments);
        if (message.cssPath != null)
        {
            printStream.print(" (" + message.cssPath + ", line: " + message.line + ")");
        }
        printStream.println();
    }
}
