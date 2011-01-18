package org.carrot2.labs.smartsprites.message;

import java.io.Serializable;
import java.util.Comparator;

import org.carrot2.labs.smartsprites.SpriteImageDirective;
import org.carrot2.labs.smartsprites.SpriteImageDirective.Ie6Mode;
import org.carrot2.labs.smartsprites.SpriteImageDirective.SpriteImageFormat;
import org.carrot2.labs.smartsprites.SpriteImageDirective.SpriteImageLayout;
import org.carrot2.labs.smartsprites.SpriteImageDirective.SpriteUidType;
import org.carrot2.labs.smartsprites.SpriteLayoutProperties.SpriteAlignment;
import org.carrot2.labs.smartsprites.SpriteReferenceDirective;

/**
 * Represents a processing message, can be an information message or a warning.
 */
public class Message implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * The importance of the message.
     */
    public static enum MessageLevel
    {
        /**
         * Information message, can be ignored.
         */
        INFO(1),

        /**
         * Notice messages related to IE6 problems.
         */
        IE6NOTICE(2),

        /**
         * Warning messages, ignoring can lead to the converted designs looking broken.
         */
        WARN(3),

        /**
         * Error messages, SmartSpricess cannot perform processing.
         */
        ERROR(4),

        /**
         * Status messages displayed at the end of processing.
         */
        STATUS(5);

        /** Numeric level for comparisons */
        final private int level;

        private MessageLevel(int level)
        {
            this.level = level;
        }

        public final static Comparator<MessageLevel> COMPARATOR = new Comparator<MessageLevel>()
        {
            public int compare(MessageLevel levelA, MessageLevel levelB)
            {
                return levelA.level - levelB.level;
            }
        };

    }

    /**
     * Defines all the possible information and warning messages.
     */
    public enum MessageType
    {
        CANNOT_DETERMINE_IMAGE_FORMAT("Cannot determine image format from file name: %s."),

        CANNOT_NOT_LOAD_IMAGE("Cannot load image: %s due to: %s."),

        CANNOT_PARSE_MARGIN_VALUE("Cannot parse margin value: %s."),

        CANNOT_WRITE_SPRITE_IMAGE("Cannot write sprite image: %s due to %s."),

        CANNOT_CREATE_DIRECTORIES("Cannot create directories: %s."),

        CREATING_CSS_STYLE_SHEET("Creating CSS style sheet: %s."),

        WRITING_CSS("Writing CSS to %s."),

        WRITING_SPRITE_IMAGE(
            "Writing sprite image of size %s x %s for sprite '%s' to %s."),

        IGNORING_SPRITE_IMAGE_REDEFINITION("Ignoring sprite image redefinition."),

        MALFORMED_CSS_RULE("Malformed CSS rule: %s."),

        MALFORMED_COLOR("Malformed color: %s."),

        MALFORMED_URL("Malformed URL: %s."),

        MORE_THAN_ONE_RULE_NEXT_TO_SPRITE_REFERENCE_DIRECTIVE(
            "Found more than one CSS rule next to sprite reference comment: %s."),

        NO_BACKGROUND_IMAGE_RULE_NEXT_TO_SPRITE_REFERENCE_DIRECTIVE(
            "No 'background-image' CSS rule next to sprite reference comment: %s."),

        EITHER_ROOT_DIR_OR_CSS_FILES_IS_REQIRED(
            "Either root directory or non-empty list of individual CSS files is required."),

        ROOT_DIR_AND_CSS_FILES_CANNOT_BE_BOTH_SPECIFIED_UNLESS_WITH_OUTPUT_DIR(
            "Root directory and individual CSS files cannot be both specified, unless output dir is also specified."),

        ROOT_DIR_IS_REQIRED_FOR_OUTPUT_DIR(
            "If output directory is specified, root directory must also be provided."),

        ROOT_DIR_DOES_NOT_EXIST_OR_IS_NOT_DIRECTORY(
            "Root directory must exist and be a directory: %s."),

        CSS_FILE_DOES_NOT_EXIST("Ignoring CSS file %s, it does not exist."),

        CSS_PATH_IS_NOT_A_FILE("Ignoring CSS path %s, it is not a file."),

        OUTPUT_DIR_IS_NOT_DIRECTORY("Output directory must be a directory: %s."),

        DOCUMENT_ROOT_DIR_DOES_NOT_EXIST_OR_IS_NOT_DIRECTORY(
            "Document root directory must exist and be a directory: %s."),

        IGNORING_CSS_FILE_OUTSIDE_OF_ROOT_DIR(
            "Ignoring a CSS file outside of root directory: %s."),

        CSS_FILE_SUFFIX_IS_REQUIRED_IF_NO_OUTPUT_DIR(
            "A non-empty CSS file suffix is required when no output directory is specified."),

        ONLY_LEFT_OR_RIGHT_ALIGNMENT_ALLOWED(
            "Only 'left' or 'right' alignment allowed on vertical sprites, found: %s. Using 'left'."),

        ONLY_TOP_OR_BOTTOM_ALIGNMENT_ALLOWED(
            "Only 'top' or 'bottom' alignment allowed on horizontal sprites, found: %s. Using 'top'."),

        READING_SPRITE_IMAGE_DIRECTIVES("Reading sprite image directives from %s."),

        READING_SPRITE_REFERENCE_DIRECTIVES(
            "Reading sprite reference directives from %s."),

        READING_CSS("Reading CSS from %s."),

        READING_IMAGE("Reading image from %s."),

        REFERENCED_SPRITE_NOT_FOUND("Referenced sprite: %s not found."),

        SPRITE_ID_NOT_FOUND("'" + SpriteImageDirective.PROPERTY_SPRITE_ID
            + "' rule is required."),

        SPRITE_IMAGE_URL_NOT_FOUND("'" + SpriteImageDirective.PROPERTY_SPRITE_IMAGE_URL
            + "' rule is required."),

        SPRITE_REF_NOT_FOUND("'" + SpriteReferenceDirective.PROPERTY_SPRITE_REF
            + "' rule is required."),

        UNSUPPORTED_ALIGNMENT("Unsupported alignment: %s. Supported alignments are: "
            + SpriteAlignment.valuesAsString() + "."),

        UNSUPPORTED_INDIVIDUAL_IMAGE_FORMAT(
            "Unsupported format of image loaded from: %s."),

        UNSUPPORTED_SPRITE_IMAGE_FORMAT(
            "Format of image: %s is not supported. Supported formats are: "
                + SpriteImageFormat.valuesAsString() + "."),

        UNSUPPORTED_LAYOUT("Unsupported layout: %s. Supported layouts are: "
            + SpriteImageLayout.valuesAsString() + "."),

        UNSUPPORTED_IE6_MODE("Unsupported ie6 mode: %s. Supported ie6 modes are: "
            + Ie6Mode.valuesAsString() + "."),

        UNSUPPORTED_UID_TYPE("Unsupported uid type: %s. Supported uid types are: "
            + SpriteUidType.valuesAsString() + "."),

        IGNORING_IE6_MODE(
            "The sprite-ie6-mode applies only to PNG sprites. Ignoring for a %s sprite."),

        JPG_DOES_NOT_SUPPORT_INDEXED_COLOR("JPG format does not support indexed color."),

        TOO_MANY_COLORS_FOR_INDEXED_COLOR(
            "Sprite '%s' requires %d colors, but the maximum for indexed color mode is %d. Image quality will be degraded."),

        ALPHA_CHANNEL_LOSS_IN_INDEXED_COLOR(
            "Alpha channel of sprite '%s' cannot be encoded in indexed color mode. Image quality will be degraded."),

        USING_WHITE_MATTE_COLOR_AS_DEFAULT(
            "Defaulting to white matte color to render partial transparencies of sprite '%s'."),

        IGNORING_MATTE_COLOR_NO_PARTIAL_TRANSPARENCY(
            "Ignoring sprite-mate-color on sprite '%s' because the sprite image does not contain partially transparent areas."),

        IGNORING_MATTE_COLOR_NO_SUPPORT(
            "Ignoring sprite-mate-color on sprite '%s' because its output format does not require matting or does not support transparency."),

        IGNORING_NEGATIVE_MARGIN_VALUE("Values of %s must not be negative, using 0."),

        PROCESSING_COMPLETED("SmartSprites processing completed in %d ms."),

        PROCESSING_COMPLETED_WITH_WARNINGS(
            "SmartSprites processing completed in %d ms with %d warning(s)."),

        UNSUPPORTED_PROPERTIES_FOUND("Unsupported properties found: %s."),

        OVERRIDING_PROPERTY_FOUND(
            "Found a '%s' property that overrides the generated one. Move it before the sprite reference directive on line %d."),

        ABSOLUTE_PATH_AND_NO_DOCUMENT_ROOT(
            "Found an absolute image path '%s' and no document.root.dir.path was defined. Taking relative to the CSS file."),

        GENERIC("%s");

        /**
         * Human readable text of the message.
         */
        private String text;

        private MessageType(String text)
        {
            this.text = text;
        }

        /**
         * Returns a human readable version of this message.
         */
        public String getText()
        {
            return text;
        }
    }

    /**
     * Importance of this message.
     */
    public final MessageLevel level;

    /**
     * Semantics of the message.
     */
    public final MessageType type;

    /**
     * CSS file to which this message refers or <code>null</code>.
     */
    public final String cssPath;

    /**
     * Line number to which this message refers, meaningful only if {@link #cssPath} is
     * not <code>null</code>.
     */
    public final int line;

    /**
     * Additional arguments to this message, used to format the human-readable string.
     */
    public final Object [] arguments;

    /**
     * Creates a new message without CSS file path and line number.
     */
    public Message(MessageLevel level, MessageType type, Object... arguments)
    {
        this(level, type, null, 0, arguments);
    }

    /**
     * Creates a new message, see field descriptions for details.
     */
    public Message(MessageLevel level, MessageType type, String cssPath, int line,
        Object... arguments)
    {
        this.level = level;
        this.type = type;
        this.cssPath = cssPath;
        this.line = line;
        this.arguments = new Object [arguments.length];
        System.arraycopy(arguments, 0, this.arguments, 0, arguments.length);
    }

    @Override
    public String toString()
    {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(level);
        stringBuilder.append(": ");
        stringBuilder.append(getFormattedMessage());

        if (cssPath != null)
        {
            stringBuilder.append(" (");
            stringBuilder.append(cssPath);
            stringBuilder.append(", line: ");
            stringBuilder.append(line + 1);
            stringBuilder.append(")");
        }

        return stringBuilder.toString();
    }

    public String getFormattedMessage()
    {
        return String.format(type.getText(), arguments);
    }
}