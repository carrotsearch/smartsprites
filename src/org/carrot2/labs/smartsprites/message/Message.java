package org.carrot2.labs.smartsprites.message;

import java.io.Serializable;
import java.util.Comparator;

import org.carrot2.labs.smartsprites.SpriteImageDirective;
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
    public static enum MessageLevel {
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
    public enum MessageType {
        CANNOT_DETERMINE_IMAGE_FORMAT("Cannot determine image format from file name: %s"),
        CANNOT_NOT_LOAD_IMAGE("Cannot load image: %s due to: %s"),
        CANNOT_PARSE_MARGIN_VALUE("Cannot parse margin value: %s"),
        CANNOT_WRITE_SPRITE_IMAGE("Cannot write sprite image: %s due to %s"),
        CREATING_CSS_STYLE_SHEET("Creating CSS style sheet: %s"),
        WRITING_SPRITE_IMAGE(
            "Writing sprite image of size %s x %s for sprite '%s' to %s"),
        IGNORING_SPRITE_IMAGE_REDEFINITION("Ignoring sprite image redefinition"),
        MALFORMED_CSS_RULE("Malformed CSS rule: %s"),
        MALFORMED_COLOR("Malformed color: %s"),
        MALFORMED_URL("Malformed URL: %s"),
        MORE_THAN_ONE_RULE_NEXT_TO_SPRITE_REFERENCE_DIRECTIVE(
            "Found more than one CSS rule next to sprite reference comment: %s"),
        NO_BACKGROUND_IMAGE_RULE_NEXT_TO_SPRITE_REFERENCE_DIRECTIVE(
            "No 'background-image' CSS rule next to sprite reference comment: %s"),
        ROOT_DIR_DOES_NOT_EXIST_OR_IS_NOT_DIRECTORY(
            "Root directory must exist and be a directory: %s"),
        OUTPUT_DIR_IS_NOT_DIRECTORY("Output directory must be a directory: %s"),
        DOCUMENT_ROOT_DIR_DOES_NOT_EXIST_OR_IS_NOT_DIRECTORY(
            "Document root directory must exist and be a directory: %s"),
        ONLY_LEFT_OR_RIGHT_ALIGNMENT_ALLOWED(
            "Only 'left' or 'right' alignment allowed on vertical sprites, found: %s. Using 'left'."),
        ONLY_TOP_OR_BOTTOM_ALIGNMENT_ALLOWED(
            "Only 'top' or 'bottom' alignment allowed on horizontal sprites, found: %s. Using 'top'."),
        READING_IMAGE("Reading image from: %s"),
        REFERENCED_SPRITE_NOT_FOUND("Referenced sprite: %s not found"),
        SPRITE_ID_NOT_FOUND("'" + SpriteImageDirective.PROPERTY_SPRITE_ID
            + "' rule is required"),
        SPRITE_IMAGE_URL_NOT_FOUND("'" + SpriteImageDirective.PROPERTY_SPRITE_IMAGE_URL
            + "' rule is required"),
        SPRITE_REF_NOT_FOUND("'" + SpriteReferenceDirective.PROPERTY_SPRITE_REF
            + "' rule is required"),
        UNSUPPORTED_ALIGNMENT("Unsupported alignment: %s"),
        UNSUPPORTED_FORMAT("Unsupported format: %s"),
        UNSUPPORTED_LAYOUT("Unsupported layout: %s"),
        UNSUPPORTED_IE6_MODE("Unsupported ie6 mode: %s"),
        UNSUPPORTED_UID_TYPE("Unsupported uid type: %s"),
        IGNORING_IE6_MODE("The sprite-ie6-mode applies only to PNG sprites. Ignoring for a %s sprite."),
        JPG_DOES_NOT_SUPPORT_INDEXED_COLOR("JPG format does not support indexed color"),
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
        PROCESSING_COMPLETED("SmartSprites processing completed in %d ms"),
        PROCESSING_COMPLETED_WITH_WARNINGS(
            "SmartSprites processing completed in %d ms with %d warning(s)"),
        UNSUPPORTED_PROPERTIES_FOUND("Unsupported properties found: %s"),
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
     * Creates a new message, see field descriptions for details.
     */
    public Message(MessageLevel level, MessageType type, String cssPath, int line,
        Object... arguments)
    {
        this.level = level;
        this.type = type;
        this.cssPath = cssPath;
        this.line = line;
        this.arguments = arguments;
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