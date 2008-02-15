package org.carrot2.labs.smartsprites;

import java.util.Map;

import org.carrot2.labs.smartsprites.css.CssRule;
import org.carrot2.labs.smartsprites.css.CssSyntaxUtils;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message.MessageType;

/**
 * @author Stanislaw Osinski
 */
public class SpriteImageDirective
{
    public static final String RULE_SPRITE_ID = "sprite";
    public static final String RULE_SPRITE_IMAGE_LAYOUT = "sprite-layout";
    public static final String RULE_SPRITE_IMAGE_URL = "sprite-image";

    public enum SpriteImageLayout {
        VERTICAL, HORIZONTAL;

        private String value;

        private SpriteImageLayout()
        {
            this.value = name().toLowerCase();
        }

        @Override
        public String toString()
        {
            return value;
        }

        public static SpriteImageLayout getValue(String value)
        {
            return valueOf(value.toUpperCase());
        }
    }

    public enum SpriteImageFormat {
        PNG, GIF, JPG;

        private String value;

        private SpriteImageFormat()
        {
            this.value = name().toLowerCase();
        }

        @Override
        public String toString()
        {
            return value;
        }

        public static SpriteImageFormat getValue(String value)
        {
            return valueOf(value.toUpperCase());
        }
    }

    public final String spriteId;
    public final String imagePath;
    public final SpriteImageLayout layout;
    public final SpriteImageFormat format;

    public SpriteImageDirective(String id, String imageUrl, SpriteImageLayout layout,
        SpriteImageFormat format)
    {
        this.spriteId = id;
        this.imagePath = imageUrl;
        this.layout = layout;
        this.format = format;
    }

    public static SpriteImageDirective parse(String directiveString,
        MessageLog messageCollector)
    {
        final Map<String, CssRule> rules = CssSyntaxUtils.rulesAsMap(CssSyntaxUtils
            .extractRules(directiveString, messageCollector));

        if (!CssSyntaxUtils.hasNonBlankValue(rules, RULE_SPRITE_ID))
        {
            messageCollector.logWarning(MessageType.SPRITE_ID_NOT_FOUND);
            return null;
        }

        if (!CssSyntaxUtils.hasNonBlankValue(rules, RULE_SPRITE_IMAGE_URL))
        {
            messageCollector.logWarning(MessageType.SPRITE_IMAGE_URL_NOT_FOUND);
            return null;
        }

        final String id = rules.get(RULE_SPRITE_ID).value;
        final String imagePath = CssSyntaxUtils.unpackUrl(rules
            .get(RULE_SPRITE_IMAGE_URL).value);
        SpriteImageLayout layout;

        // Layout is optional
        if (CssSyntaxUtils.hasNonBlankValue(rules, RULE_SPRITE_IMAGE_LAYOUT))
        {
            final String layoutValue = rules.get(RULE_SPRITE_IMAGE_LAYOUT).value;
            try
            {
                layout = SpriteImageLayout.getValue(layoutValue);
            }
            catch (final IllegalArgumentException e)
            {
                messageCollector.logWarning(MessageType.UNSUPPORTED_LAYOUT, layoutValue);
                layout = SpriteImageLayout.VERTICAL;
            }
        }
        else
        {
            layout = SpriteImageLayout.VERTICAL;
        }

        // Infer format from image path
        SpriteImageFormat format;
        final int lastDotIndex = imagePath.lastIndexOf('.');
        if ((lastDotIndex < 0) || (lastDotIndex == imagePath.length() - 1))
        {
            messageCollector.logWarning(MessageType.CANNOT_DETERMINE_IMAGE_FORMAT,
                imagePath);
            format = SpriteImageFormat.PNG;
        }
        else
        {
            final String formatValue = imagePath.substring(lastDotIndex + 1);
            try
            {
                format = SpriteImageFormat.getValue(formatValue);
            }
            catch (final IllegalArgumentException e)
            {
                messageCollector.logWarning(MessageType.UNSUPPORTED_FORMAT, formatValue);
                format = SpriteImageFormat.PNG;
            }
        }

        return new SpriteImageDirective(id, imagePath, layout, format);
    }
}
