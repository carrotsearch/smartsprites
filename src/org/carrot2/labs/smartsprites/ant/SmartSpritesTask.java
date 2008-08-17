package org.carrot2.labs.smartsprites.ant;

import static junit.framework.Assert.fail;

import java.io.*;

import org.apache.tools.ant.*;
import org.carrot2.labs.smartsprites.SmartSpritesParameters;
import org.carrot2.labs.smartsprites.SpriteBuilder;
import org.carrot2.labs.smartsprites.message.*;
import org.carrot2.labs.smartsprites.message.Message.MessageLevel;

/**
 * Ant task for calling SmartSprites processing.
 */
public class SmartSpritesTask extends Task
{
    private File rootDir;
    private File outputDir;
    private File documentRootDir;
    private MessageLevel logLevel;
    private String cssFileSuffix;
    private String cssPropertyIndent;

    public void setRootDir(File dir)
    {
        this.rootDir = dir;
    }

    public void setOutputDir(File outputDir)
    {
        this.outputDir = outputDir;
    }

    public void setDocumentRootDir(File documentRootDir)
    {
        this.documentRootDir = documentRootDir;
    }

    public void setLogLevel(String logLevel)
    {
        try
        {
            this.logLevel = MessageLevel.valueOf(logLevel);
        }
        catch (Exception e)
        {
            this.logLevel = MessageLevel.INFO;
        }
    }

    public void setCssFileSuffix(String cssFileSuffix)
    {
        this.cssFileSuffix = cssFileSuffix;
    }

    public void setCssPropertyIndent(String cssPropertyIndent)
    {
        this.cssPropertyIndent = cssPropertyIndent;
    }

    @Override
    public void execute() throws BuildException
    {
        final SmartSpritesParameters parameters = new SmartSpritesParameters(rootDir,
            outputDir, documentRootDir, logLevel, cssFileSuffix, cssPropertyIndent);

        MessageLog log = new MessageLog(new AntLogMessageSink());

        
        
        try
        {
            parameters.validate(log);
        }
        catch (IllegalArgumentException e)
        {
            fail();
        }

        try
        {
            SpriteBuilder.buildSprites(parameters, log);
        }
        catch (FileNotFoundException e)
        {
            throw new BuildException(e);
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
    }

    private class AntLogMessageSink implements MessageSink
    {
        public void add(Message message)
        {
            if (MessageLevel.COMPARATOR.compare(message.level, logLevel) >= 0)
            {
                getProject().log(message.toString());
            }
        }
    }
}
