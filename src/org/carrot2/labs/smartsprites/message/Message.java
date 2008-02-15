package org.carrot2.labs.smartsprites.message;

import org.carrot2.labs.smartsprites.SpriteImageDirective;
import org.carrot2.labs.smartsprites.SpriteReferenceDirective;

/**
 * @author Stanislaw Osinski
 */
public class Message
{
    public enum MessageLevel {
        INFO, WARN
    }

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
        SPRITE_ID_NOT_FOUND("'" + SpriteImageDirective.RULE_SPRITE_ID
            + "' rule is required"),
        SPRITE_IMAGE_URL_NOT_FOUND("'" + SpriteImageDirective.RULE_SPRITE_IMAGE_URL
            + "' rule is required"),
        SPRITE_REF_NOT_FOUND("'" + SpriteReferenceDirective.RULE_SPRITE_REF
            + "' rule is required"),
        UNSUPPORTED_ALIGNMENT("Unsupported alignment: %s"),
        UNSUPPORTED_FORMAT("Unsupported format: %s"),
        UNSUPPORTED_LAYOUT("Unsupported layout: %s"),
        PROCESSING_COMPLETED("Processing completed in %d ms");

        private String text;

        private MessageType(String text)
        {
            this.text = text;
        }

        public String getText()
        {
            return text;
        }
    }

    public final MessageLevel level;
    public final MessageType type;
    public final String cssPath;
    public final int line;
    public final Object [] arguments;

    public Message(MessageLevel level, MessageType type, String cssPath, int line,
        Object... arguments)
    {
        this.level = level;
        this.type = type;
        this.cssPath = cssPath;
        this.line = line;
        this.arguments = arguments;
    }
}