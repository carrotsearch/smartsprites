package org.carrot2.labs.smartsprites;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message.MessageLevel;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.util.FileUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 * Contains invocation parameters for SmartSprites, provides methods for validating the
 * parameters.
 */
public final class SmartSpritesParameters
{
    /**
     * Path to the directory that contains the css files to be processed. Directories
     * containing CSS and image files must be writable, unless output.dir.path is
     * provided. The root.dir.path can be either absolute, e.g. c:/myproject/web or
     * relative to the directory in which this script is run.
     */
    @Option(name = "--root-dir-path", required = false, metaVar = "DIR")
    private String rootDir;

    /**
     * Paths to individual CSS files to process. Either {@link #rootDir} or
     * {@link #cssFiles} must be not empty. If only {@link #cssFiles} is not empty,
     * {@link #outputDir} must be blank. If both {@link #rootDir} and {@link #cssFiles}
     * are not empty, {@link #outputDir} is supported but only {@link #cssFiles} from
     * {@link #rootDir} are processed.
     */
    @Argument(metaVar = "CSS-FILES")
    @Option(name = "--css-files", required = false, metaVar = "FILES")
    private List<String> cssFiles;

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

    /**
     * If <code>true</code>, SmartSprites will generate the sprite directive indicating
     * that the image is a sprite image.
     */
    @Option(name = "--mark-sprite-images")
    private boolean markSpriteImages;

    /** The default suffix to be added to the generated CSS files. */
    public static final String DEFAULT_CSS_FILE_SUFFIX = "-sprite";

    /** By default, we use full color only when necessary */
    public static final PngDepth DEFAULT_SPRITE_PNG_DEPTH = PngDepth.AUTO;

    /** By default, we don't generate separate sprites for IE6 */
    public static final boolean DEFAULT_SPRITE_PNG_IE6 = false;

    /** By default, we'll assume CSS files are UTF-8 encoded */
    public static final String DEFAULT_CSS_FILE_ENCODING = "UTF-8";

    /** The default logging level. */
    public static final MessageLevel DEFAULT_LOGGING_LEVEL = MessageLevel.INFO;

    /** By default, we don't generate sprite directive in output css */
    public static final boolean DEFAULT_MARK_SPRITE_IMAGES = false;

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
     * Creates the parameters with most default values.
     */
    public SmartSpritesParameters(String rootDir)
    {
        this(rootDir, null, null, null, MessageLevel.INFO, DEFAULT_CSS_FILE_SUFFIX,
            DEFAULT_SPRITE_PNG_DEPTH, DEFAULT_SPRITE_PNG_IE6, DEFAULT_CSS_FILE_ENCODING,
            DEFAULT_MARK_SPRITE_IMAGES);
    }

    /**
     * Creates the parameters.
     */
    public SmartSpritesParameters(String rootDir, List<String> cssFiles,
        String outputDir, String documentRootDir, MessageLevel logLevel,
        String cssFileSuffix, PngDepth spritePngDepth, boolean spritePngIe6,
        String cssEncoding)
    {
        this(rootDir, cssFiles, outputDir, documentRootDir, logLevel, cssFileSuffix,
            spritePngDepth, spritePngIe6, cssEncoding, DEFAULT_MARK_SPRITE_IMAGES);
    }

    /**
     * Creates the parameters.
     */
    public SmartSpritesParameters(String rootDir, List<String> cssFiles,
        String outputDir, String documentRootDir, MessageLevel logLevel,
        String cssFileSuffix, PngDepth spritePngDepth, boolean spritePngIe6,
        String cssEncoding, boolean markSpriteImages)
    {
        this.rootDir = rootDir;
        this.cssFiles = cssFiles;
        this.outputDir = outputDir;
        this.documentRootDir = documentRootDir;
        this.logLevel = logLevel;
        this.cssFileEncoding = cssEncoding;
        this.cssFileSuffix = getCssFileSuffix(cssFileSuffix);
        this.spritePngDepth = spritePngDepth;
        this.spritePngIe6 = spritePngIe6;
        this.markSpriteImages = markSpriteImages;
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

        // Either root dir or css files are required
        if (!hasRootDir() && !hasCssFiles())
        {
            log.error(MessageType.EITHER_ROOT_DIR_OR_CSS_FILES_IS_REQIRED);
            return false;
        }

        // If there is no output dir, we can't have both root dir or css files
        if (!hasOutputDir() && hasRootDir() && hasCssFiles())
        {
            log.error(MessageType.ROOT_DIR_AND_CSS_FILES_CANNOT_BE_BOTH_SPECIFIED_UNLESS_WITH_OUTPUT_DIR);
            return false;
        }

        // Check root dir if provided
        if (hasRootDir())
        {
            final File rootDir = FileUtils.getCanonicalOrAbsoluteFile(this.rootDir);
            if ((!rootDir.exists() || !rootDir.isDirectory()))
            {
                log.error(MessageType.ROOT_DIR_DOES_NOT_EXIST_OR_IS_NOT_DIRECTORY,
                    this.rootDir);
                valid = false;
            }
        }

        // Check output dir if provided
        if (hasOutputDir())
        {
            // For output dir, we need root dir
            if (!hasRootDir())
            {
                log.error(MessageType.ROOT_DIR_IS_REQIRED_FOR_OUTPUT_DIR);
                return false;
            }

            final File outputDir = FileUtils.getCanonicalOrAbsoluteFile(this.outputDir);
            if (outputDir.exists() && !outputDir.isDirectory())
            {
                log.error(MessageType.OUTPUT_DIR_IS_NOT_DIRECTORY, this.outputDir);
                valid = false;
            }
        }

        if (!hasOutputDir() && StringUtils.isBlank(cssFileSuffix))
        {
            log.error(MessageType.CSS_FILE_SUFFIX_IS_REQUIRED_IF_NO_OUTPUT_DIR);
            valid = false;
        }

        if (hasDocumentRootDir())
        {
            final File documentRootDir = FileUtils
                .getCanonicalOrAbsoluteFile(this.documentRootDir);
            if (!documentRootDir.exists() || !documentRootDir.isDirectory())
            {
                log.error(
                    MessageType.DOCUMENT_ROOT_DIR_DOES_NOT_EXIST_OR_IS_NOT_DIRECTORY,
                    this.documentRootDir);
                valid = false;
            }
        }

        return valid;
    }

    private String getCssFileSuffix(String suffix)
    {
        if (suffix == null)
        {
            if (!hasOutputDir())
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

    public File getRootDirFile() throws IOException
    {
        return rootDir.startsWith("..") ? new File(rootDir).getCanonicalFile()
            : new File(rootDir);
    }

    public boolean hasRootDir()
    {
        return StringUtils.isNotBlank(rootDir);
    }

    public List<String> getCssFiles()
    {
        return cssFiles;
    }

    public boolean hasCssFiles()
    {
        return cssFiles != null && !cssFiles.isEmpty();
    }

    public String getOutputDir()
    {
        return outputDir;
    }

    public boolean hasOutputDir()
    {
        return StringUtils.isNotBlank(outputDir);
    }

    public String getDocumentRootDir()
    {
        return documentRootDir;
    }

    public boolean hasDocumentRootDir()
    {
        return StringUtils.isNotBlank(documentRootDir);
    }

    public MessageLevel getLogLevel()
    {
        return logLevel;
    }

    public String getCssFileSuffix()
    {
        return cssFileSuffix;
    }

    public PngDepth getSpritePngDepth()
    {
        return spritePngDepth;
    }

    public boolean isSpritePngIe6()
    {
        return spritePngIe6;
    }

    public boolean isMarkSpriteImages()
    {
        return markSpriteImages;
    }

    public String getCssFileEncoding()
    {
        return cssFileEncoding;
    }
}