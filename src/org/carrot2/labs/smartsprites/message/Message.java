package org.carrot2.labs.smartsprites.message;

import java.util.Comparator;

import org.carrot2.labs.smartsprites.SpriteImageDirective;
import org.carrot2.labs.smartsprites.SpriteReferenceDirective;

/**
 * Represents a processing message, can be an information message or a warning.
 */
public class Message
{
    /**
     * The importance of the message.
     */
    public static enum MessageLevel {
        /**
         * Information message, can be ignored.
         */
        INFO,

        /**
         * Warning messages, ignoring can lead to the converted designs looking broken.
         */
        WARN;

        public final static Comparator<MessageLevel> COMPARATOR = new Comparator<MessageLevel>()
        {
            @Override
            public int compare(MessageLevel levelA, MessageLevel levelB)
            {
                if (levelA.equals(levelB))
                {
                    return 0;
                }
                
                if (levelA.equals(INFO))
                {
                    return -1;
                }
                else
                {
                    return 1;
                }
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
        CREATING_SPRITE_IMAGE("Creating sprite image of size %s x %s for %s"),
        IGNORING_SPRITE_IMAGE_REDEFINITION("Ignoring sprite image redefinition"),
        MALFORMED_CSS_RULE("Malformed CSS rule: %s"),
        MALFORMED_URL("Malformed URL: %s"),
        MORE_THAN_ONE_RULE_NEXT_TO_SPRITE_REFERENCE_DIRECTIVE(
            "Found more than one CSS rule next to sprite reference comment: %s"),
        NO_BACKGROUND_IMAGE_RULE_NEXT_TO_SPRITE_REFERENCE_DIRECTIVE(
            "No 'background-image' CSS rule next to sprite reference comment: %s"),
        NOT_A_DIRECTORY_ON_INPUT("Not a directory: %s"),
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
        PROCESSING_COMPLETED("Processing completed in %d ms"),
        UNSUPPORTED_PROPERTIES_FOUND("Unsupported properties found: %s"),
        OVERRIDING_PROPERTY_FOUND(
            "Found a '%s' property that overrides the generated one. Move it before the sprite reference directive on line %d.");

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
        stringBuilder.append(String.format(type.getText(), arguments));

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
}