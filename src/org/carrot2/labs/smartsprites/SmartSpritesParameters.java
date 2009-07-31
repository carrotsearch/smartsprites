package org.carrot2.labs.smartsprites;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message.MessageLevel;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.util.FileUtils;
import org.kohsuke.args4j.Option;

/**
 * Contains invocation parameters for SmartSprites, provides methods for validating the
 * parameters.
 */
public class SmartSpritesParameters
{
    /**
     * Path to the directory that contains the css files to be processed. Directories
     * containing CSS and image files must be writable, unless output.dir.path is
     * provided. The root.dir.path can be either absolute, e.g. c:/myproject/web or
     * relative to the directory in which this script is run.
     */
    @Option(name = "--root-dir-path", required = true, metaVar = "DIR")
    private String rootDir;

    /**
     * Output directory for processed CSS files and CSS-relative sprite images. The
     * directory structure relative to root.dir.path will be preserved in the output
     * directory. E.g. if CSS files are contained in the css/base directory of
     * root.dir.path, the processed results will be written to output.dir.path/css/base.
     * Also, CSS-relative sprite images will be written to the output directory. Sprite
     * images with document-root-relative URLs will be written relative to the
     * document.root.dir.path.
     * <p>
     * If the output.dir.path directory does not exist, it will be created.
     * <p>
     * You can leave this property empty, in which case the CSS files will be written next
     * to the original CSS files with css.file.suffix, and sprite images will be written
     * relative to CSS files.
     * <p>
     * If you are using a non-empty output.dir.path, you might want to use an empty
     * css.file.suffix.
     */
    @Option(name = "--output-dir-path", metaVar = "DIR")
    private String outputDir;

    /**
     * Document root path for document-root-relative (starting with '/') image urls in
     * CSS. All such image URLs will be taken relative to document.root.dir.path. Also
     * document-root-relative sprite URLs will be written relative to
     * document.root.dir.path. You can leave this property empty if your CSS uses only
     * CSS-relative image URLs. *
     */
    @Option(name = "--document-root-dir-path", metaVar = "DIR")
    private String documentRootDir;

    /**
     * Message logging level. If you're getting lots of INFO messages and want to see only
     * warnings, set this option to WARN.
     */
    @Option(name = "--log-level")
    private MessageLevel logLevel;

    /**
     * The encoding to assume for input and output CSS files.
     */
    @Option(name = "--css-file-encoding")
    private String cssFileEncoding;

    /**
     * Suffix to be appended to the processed CSS file name.
     */
    @Option(name = "--css-file-suffix")
    private String cssFileSuffix;

    /**
     * How generated CSS properties should be indented.
     */
    private String cssPropertyIndent;

    /**
     * The required depth of the generated PNG sprites.
     */
    @Option(name = "--sprite-png-depth")
    private PngDepth spritePngDepth;

    /**
     * If <code>true</code>, SmartSprites will generate IE6-friendly PNG sprites if
     * needed.
     */
    @Option(name = "--sprite-png-ie6")
    private boolean spritePngIe6;

    /** Default indent for the generated CSS properties. */
    public static final String DEFAULT_CSS_INDENT = "  ";

    /** The default suffix to be added to the generated CSS files. */
    public static final String DEFAULT_CSS_FILE_SUFFIX = "-sprite";

    /** By default, we use full color only when necessary */
    public static final PngDepth DEFAULT_SPRITE_PNG_DEPTH = PngDepth.AUTO;

    /** By default, we'll generate separate sprites for IE6 if needed */
    public static final boolean DEFAULT_SPRITE_PNG_IE6 = false;

    /** By default, we'll assume CSS files are UTF-8 encoded */
    public static final String DEFAULT_CSS_FILE_ENCODING = "UTF-8";

    /** The default logging level. */
    public static final MessageLevel DEFAULT_LOGGING_LEVEL = MessageLevel.INFO;

    public enum PngDepth
    {
        AUTO, INDEXED, DIRECT;
    }

    /**
     * Creates the parameters with default options and null root dir, before root dir is
     * set, the parameters are invalid.
     */
    public SmartSpritesParameters()
    {
        this(null);
    }

    /**
     * Creates the parameter with most default values.
     */
    public SmartSpritesParameters(String rootDir)
    {
        this(rootDir, null, null, MessageLevel.INFO, DEFAULT_CSS_FILE_SUFFIX,
            DEFAULT_CSS_INDENT, DEFAULT_SPRITE_PNG_DEPTH, DEFAULT_SPRITE_PNG_IE6,
            DEFAULT_CSS_FILE_ENCODING);
    }

    /**
     * Creates the parameter with most default values.
     */
    public SmartSpritesParameters(String rootDir, MessageLevel messageLevel,
        String cssFileSuffix, PngDepth spritePngDepth, boolean spritePngIe6)
    {
        this(rootDir, null, null, messageLevel, cssFileSuffix, DEFAULT_CSS_INDENT,
            spritePngDepth, spritePngIe6, DEFAULT_CSS_FILE_ENCODING);
    }

    /**
     * Creates the parameter.
     */
    public SmartSpritesParameters(String rootDir, String outputDir,
        String documentRootDir, MessageLevel logLevel, String cssFileSuffix,
        String cssPropertyIndent, PngDepth spritePngDepth, boolean spritePngIe6,
        String cssEncoding)
    {
        this.rootDir = rootDir;
        this.outputDir = outputDir;
        this.documentRootDir = documentRootDir;
        this.logLevel = logLevel;
        this.cssFileEncoding = cssEncoding;
        this.cssPropertyIndent = cssPropertyIndent;
        this.cssFileSuffix = getCssFileSuffix(cssFileSuffix);
        this.spritePngDepth = spritePngDepth;
        this.spritePngIe6 = spritePngIe6;
    }

    /**
     * Validates the provided parameters. All resource paths are resolved agains the local
     * file system.
     * 
     * @return <code>true</code> if the parameters are valid
     */
    public boolean validate(MessageLog log)
    {
        boolean valid = true;

        File rootDir = FileUtils.getCanonicalOrAbsoluteFile(this.rootDir);
        if (!rootDir.exists() || !rootDir.isDirectory())
        {
            log.error(MessageType.ROOT_DIR_DOES_NOT_EXIST_OR_IS_NOT_DIRECTORY, rootDir);
            valid = false;
        }

        if (outputDir != null)
        {
            File outputDir = FileUtils.getCanonicalOrAbsoluteFile(this.outputDir);
            if (outputDir.exists() && !outputDir.isDirectory())
            {
                log.error(MessageType.OUTPUT_DIR_IS_NOT_DIRECTORY, outputDir);
                valid = false;
            }
        }

        if (documentRootDir != null)
        {
            File documentRootDir = FileUtils
                .getCanonicalOrAbsoluteFile(this.documentRootDir);
            if (!documentRootDir.exists() || !documentRootDir.isDirectory())
            {
                log.error(
                    MessageType.DOCUMENT_ROOT_DIR_DOES_NOT_EXIST_OR_IS_NOT_DIRECTORY,
                    documentRootDir);
                valid = false;
            }
        }

        return valid;
    }

    private String getCssFileSuffix(String suffix)
    {
        if (StringUtils.isBlank(suffix))
        {
            if (outputDir == null)
            {
                // If there is no output dir, we must have some suffix
                return DEFAULT_CSS_FILE_SUFFIX;
            }
            else
            {
                // If we have an output dir, we can have an empty suffix
                return "";
            }
        }
        else
        {
            return suffix;
        }
    }

    public String getRootDir()
    {
        return rootDir;
    }

    public String getOutputDir()
    {
        return outputDir;
    }

    public String getDocumentRootDir()
    {
        return documentRootDir;
    }

    public MessageLevel getLogLevel()
    {
        return logLevel;
    }

    public String getCssFileSuffix()
    {
        return cssFileSuffix;
    }

    public String getCssPropertyIndent()
    {
        return cssPropertyIndent;
    }

    public PngDepth getSpritePngDepth()
    {
        return spritePngDepth;
    }

    public boolean isSpritePngIe6()
    {
        return spritePngIe6;
    }

    public String getCssFileEncoding()
    {
        return cssFileEncoding;
    }
}