package org.carrot2.labs.smartsprites.ant;

import static junit.framework.Assert.fail;

import java.io.*;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.carrot2.labs.smartsprites.SmartSpritesParameters;
import org.carrot2.labs.smartsprites.SpriteBuilder;
import org.carrot2.labs.smartsprites.SmartSpritesParameters.PngDepth;
import org.carrot2.labs.smartsprites.message.*;
import org.carrot2.labs.smartsprites.message.Message.MessageLevel;
import org.carrot2.util.EnumUtils;

/**
 * Ant task for calling SmartSprites processing.
 */
public class SmartSpritesTask extends Task
{
    private File rootDir;
    private File outputDir;
    private File documentRootDir;
    private MessageLevel logLevel;
    private MessageLevel failOnLevel;
    private String cssFileSuffix = SmartSpritesParameters.DEFAULT_CSS_FILE_SUFFIX;
    private String cssFileEncoding = SmartSpritesParameters.DEFAULT_CSS_FILE_ENCODING;
    private String cssPropertyIndent = SmartSpritesParameters.DEFAULT_CSS_INDENT;
    private PngDepth spritePngDepth = SmartSpritesParameters.DEFAULT_SPRITE_PNG_DEPTH;
    private boolean spritePngIe6 = SmartSpritesParameters.DEFAULT_SPRITE_PNG_IE6;

    public void setRootDir(File dir)
    {
        this.rootDir = dir;
    }

    public void setOutputDir(String outputDir)
    {
        this.outputDir = StringUtils.isNotBlank(outputDir) ? new File(outputDir) : null;
    }

    public void setDocumentRootDir(File documentRootDir)
    {
        this.documentRootDir = documentRootDir;
    }

    public void setLogLevel(String logLevel)
    {
        this.logLevel = getLogLevelFromString(logLevel, MessageLevel.INFO);
    }

    public void setFailOnLevel(String failOnLevel)
    {
        this.failOnLevel = getLogLevelFromString(failOnLevel, null);
    }

    private MessageLevel getLogLevelFromString(String logLevel, MessageLevel defaultLevel)
    {
        try
        {
            return MessageLevel.valueOf(logLevel);
        }
        catch (Exception e)
        {
            return defaultLevel;
        }
    }

    public void setCssFileEncoding(String cssFileEncoding)
    {
        this.cssFileEncoding = cssFileEncoding;
    }

    public void setCssFileSuffix(String cssFileSuffix)
    {
        this.cssFileSuffix = cssFileSuffix;
    }

    public void setCssPropertyIndent(String cssPropertyIndent)
    {
        this.cssPropertyIndent = cssPropertyIndent;
    }

    public void setSpritePngDepth(String spritePngDepthString)
    {
        this.spritePngDepth = EnumUtils.valueOf(spritePngDepthString, PngDepth.class,
            SmartSpritesParameters.DEFAULT_SPRITE_PNG_DEPTH);
    }

    public void setSpritePngIe6(boolean spritePngIe6)
    {
        this.spritePngIe6 = spritePngIe6;
    }

    @Override
    public void execute() throws BuildException
    {
        final SmartSpritesParameters parameters = new SmartSpritesParameters(rootDir,
            outputDir, documentRootDir, logLevel, cssFileSuffix, cssPropertyIndent,
            spritePngDepth, spritePngIe6, cssFileEncoding);

        final FailureDetectorMessageSink failureDetectorMessageSink = new FailureDetectorMessageSink();
        MessageLog log = new MessageLog(new AntLogMessageSink(),
            failureDetectorMessageSink);

        if (!parameters.validate(log))
        {
            fail();
        }

        try
        {
            new SpriteBuilder(parameters, log).buildSprites();
        }
        catch (FileNotFoundException e)
        {
            throw new BuildException(e);
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }

        if (failureDetectorMessageSink.shouldFail)
        {
            fail(failureDetectorMessageSink.failureLevel.name() + " messages found");
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

    private class FailureDetectorMessageSink implements MessageSink
    {
        boolean shouldFail = false;
        MessageLevel failureLevel = null;

        public void add(Message message)
        {
            if (failOnLevel != null
                && MessageLevel.COMPARATOR.compare(message.level, failOnLevel) >= 0
                && message.level != MessageLevel.STATUS)
            {
                failureLevel = message.level;
                shouldFail = true;
            }
        }
    }
}
