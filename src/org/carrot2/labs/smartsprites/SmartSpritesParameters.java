package org.carrot2.labs.smartsprites;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message.MessageLevel;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.util.FileUtils;

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
    public final File rootDir;

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
    public final File outputDir;

    /**
     * Document root path for document-root-relative (starting with '/') image urls in
     * CSS. All such image URLs will be taken relative to document.root.dir.path. Also
     * document-root-relative sprite URLs will be written relative to
     * document.root.dir.path. You can leave this property empty if your CSS uses only
     * CSS-relative image URLs. *
     */
    public final File documentRootDir;

    /**
     * Message logging level. If you're getting lots of INFO messages and want to see only
     * warnings, set this option to WARN.
     */
    public final MessageLevel logLevel;

    /**
     * Suffix to be appended to the processed CSS file name.
     */
    public final String cssFileSuffix;

    /**
     * How generated CSS properties should be indented.
     */
    public final String cssPropertyIndent;

    /** Default indent for the generated CSS properties. */
    public static final String DEFAULT_CSS_INDENT = "  ";

    /** The default suffix to be added to the generated CSS files. */
    public static final String DEFAULT_CSS_FILE_SUFFIX = "-sprite";

    /** The default logging level. */
    public static final MessageLevel DEFAULT_LOGGING_LEVEL = MessageLevel.INFO;

    private static final String ROOT_DIR_PATH_PROPERTY = "root.dir.path";

    /**
     * Creates the parameter.
     */
    public SmartSpritesParameters(File rootDir, File outputDir, File documentRootDir,
        MessageLevel logLevel, String cssFileSuffix, String cssPropertyIndent)
    {
        this.rootDir = rootDir;
        this.outputDir = outputDir;
        this.documentRootDir = documentRootDir;
        this.logLevel = logLevel;
        this.cssPropertyIndent = cssPropertyIndent;
        this.cssFileSuffix = getCssFileSuffix(cssFileSuffix);
    }

    /**
     * Initializes the parameter object from system properties.
     */
    public SmartSpritesParameters()
    {
        // CSS indent
        String cssIndent = System.getProperty("css.property.indent");
        if (StringUtils.isBlank(cssIndent))
        {
            cssPropertyIndent = DEFAULT_CSS_INDENT;
        }
        else
        {
            cssPropertyIndent = cssIndent;
        }

        // Loging level
        String logLevelString = System.getProperty("log.level");
        if (StringUtils.isBlank(logLevelString))
        {
            logLevel = DEFAULT_LOGGING_LEVEL;
        }
        else
        {
            MessageLevel level;
            try
            {
                level = MessageLevel.valueOf(logLevelString);
            }
            catch (Exception e)
            {
                level = MessageLevel.INFO;
            }
            logLevel = level;
        }

        // Root dir
        final String rootDirString = System.getProperty(ROOT_DIR_PATH_PROPERTY);
        if (StringUtils.isBlank(rootDirString))
        {
            throw new IllegalArgumentException(
                "Please provide root directory in 'root.dir' system property.");
        }
        else
        {
            rootDir = new File(rootDirString);
        }

        // Output dir
        final String outputDirString = System.getProperty("output.dir.path");
        if (StringUtils.isNotBlank(outputDirString))
        {
            outputDir = new File(outputDirString);
        }
        else
        {
            outputDir = null;
        }

        // Document root
        final String documentRootDirString = System.getProperty("document.root.dir.path");
        if (StringUtils.isNotBlank(documentRootDirString))
        {
            documentRootDir = new File(documentRootDirString);
        }
        else
        {
            documentRootDir = null;
        }

        // Css file suffix
        cssFileSuffix = getCssFileSuffix(System.getProperty("css.file.suffix"));
    }

    /**
     * Validates the provided parameters.
     * 
     * @throws IllegalArgumentException in case of validation errors. Detailed messages
     *             will be stored in the <code>log</code>.
     */
    public void validate(MessageLog log)
    {
        if (!rootDir.exists() || !rootDir.isDirectory())
        {
            log.error(MessageType.ROOT_DIR_DOES_NOT_EXIST_OR_IS_NOT_DIRECTORY, FileUtils
                .getCanonicalOrAbsolutePath(rootDir));
            throw new IllegalArgumentException();
        }

        if (outputDir != null)
        {
            if (outputDir.exists() && !outputDir.isDirectory())
            {
                log.error(MessageType.OUTPUT_DIR_IS_NOT_DIRECTORY, FileUtils
                    .getCanonicalOrAbsolutePath(documentRootDir));
                throw new IllegalArgumentException();
            }
        }

        if (documentRootDir != null
            && (!documentRootDir.exists() || !documentRootDir.isDirectory()))
        {
            log.error(MessageType.DOCUMENT_ROOT_DIR_DOES_NOT_EXIST_OR_IS_NOT_DIRECTORY,
                FileUtils.getCanonicalOrAbsolutePath(documentRootDir));
            throw new IllegalArgumentException();
        }
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

}