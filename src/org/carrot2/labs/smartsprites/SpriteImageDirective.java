package org.carrot2.labs.smartsprites;

import java.awt.Color;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.carrot2.labs.smartsprites.css.CssProperty;
import org.carrot2.labs.smartsprites.css.CssSyntaxUtils;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.util.CollectionUtils;

import com.google.common.collect.Sets;

/**
 * Represents a directive that declares an individual sprite image.
 */
public class SpriteImageDirective
{
    public static final String PROPERTY_SPRITE_ID = "sprite";
    public static final String PROPERTY_SPRITE_IMAGE_LAYOUT = "sprite-layout";
    public static final String PROPERTY_SPRITE_IMAGE_URL = "sprite-image";
    public static final String PROPERTY_SPRITE_MATTE_COLOR = "sprite-matte-color";
    public static final String PROPERTY_SPRITE_IE6_MODE = "sprite-ie6-mode";

    /** A set of allowed properties */
    private static final HashSet<String> ALLOWED_PROPERTIES = Sets.newLinkedHashSet(
        PROPERTY_SPRITE_ID, PROPERTY_SPRITE_IMAGE_LAYOUT, PROPERTY_SPRITE_IMAGE_URL,
        PROPERTY_SPRITE_MATTE_COLOR, PROPERTY_SPRITE_IE6_MODE);

    /**
     * Defines the layout of this sprite.
     */
    public static enum SpriteImageLayout
    {
        /**
         * Vertical layout, images stacked on each other.
         */
        VERTICAL,

        /**
         * Horizontal layout, images next to each other.
         */
        HORIZONTAL;

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
    }

    /**
     * Defines supported image file formats.
     */
    public static enum SpriteImageFormat
    {
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

    /**
     * Defines supported IE6 support options.
     */
    public static enum Ie6Mode
    {
        /** No IE6-friendly image will be created for this sprite, even if needed */
        NONE,

        /** IE6-friendly image will be generated for this sprite if needed */
        AUTO;

        private String value;

        private Ie6Mode()
        {
            this.value = name().toLowerCase();
        }

        @Override
        public String toString()
        {
            return value;
        }
    }

    /**
     * Unique identified of this sprite.
     */
    public final String spriteId;

    /**
     * CSS file relative path for this sprite image.
     */
    public final String imagePath;

    /**
     * Layout of this sprite image.
     */
    public final SpriteImageLayout layout;

    /**
     * Format of this sprite image.
     */
    public final SpriteImageFormat format;

    /**
     * How IE6 sprites should be handled.
     */
    public final Ie6Mode ie6Mode;

    /**
     * Matte color to be used when reducing true alpha channel.
     */
    public final Color matteColor;

    public SpriteImageDirective(String id, String imageUrl, SpriteImageLayout layout,
        SpriteImageFormat format, Ie6Mode ie6Mode, Color matteColor)
    {
        this.spriteId = id;
        this.imagePath = imageUrl;
        this.layout = layout;
        this.format = format;
        this.ie6Mode = ie6Mode;
        this.matteColor = matteColor;
    }

    /**
     * Parses a string into a {@link SpriteImageDirective}, logging messages to the
     * provided {@link MessageLog}s.
     */
    public static SpriteImageDirective parse(String directiveString,
        MessageLog messageCollector)
    {
        final Map<String, CssProperty> rules = CssSyntaxUtils
            .propertiesAsMap(CssSyntaxUtils.extractRules(directiveString,
                messageCollector));

        final Set<String> properties = Sets.newLinkedHashSet(rules.keySet());
        properties.removeAll(ALLOWED_PROPERTIES);
        if (!properties.isEmpty())
        {
            messageCollector.warning(MessageType.UNSUPPORTED_PROPERTIES_FOUND,
                CollectionUtils.toString(properties));
        }

        if (!CssSyntaxUtils.hasNonBlankValue(rules, PROPERTY_SPRITE_ID))
        {
            messageCollector.warning(MessageType.SPRITE_ID_NOT_FOUND);
            return null;
        }

        if (!CssSyntaxUtils.hasNonBlankValue(rules, PROPERTY_SPRITE_IMAGE_URL))
        {
            messageCollector.warning(MessageType.SPRITE_IMAGE_URL_NOT_FOUND);
            return null;
        }

        final String id = rules.get(PROPERTY_SPRITE_ID).value;
        final String imagePath = CssSyntaxUtils.unpackUrl(rules
            .get(PROPERTY_SPRITE_IMAGE_URL).value);

        // Layout is optional
        final SpriteImageLayout layout = valueOf(CssSyntaxUtils.getValue(rules,
            PROPERTY_SPRITE_IMAGE_LAYOUT), SpriteImageLayout.class,
            SpriteImageLayout.VERTICAL, messageCollector, MessageType.UNSUPPORTED_LAYOUT);

        // Infer format from image path
        SpriteImageFormat format;
        final int lastDotIndex = imagePath.lastIndexOf('.');
        if ((lastDotIndex < 0) || (lastDotIndex == imagePath.length() - 1))
        {
            messageCollector
                .warning(MessageType.CANNOT_DETERMINE_IMAGE_FORMAT, imagePath);
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
                messageCollector.warning(MessageType.UNSUPPORTED_FORMAT, formatValue);
                format = SpriteImageFormat.PNG;
            }
        }

        // Layout is optional
        final String ie6ModeString = CssSyntaxUtils.getValue(rules,
            PROPERTY_SPRITE_IE6_MODE);
        final Ie6Mode ie6Mode = valueOf(ie6ModeString, Ie6Mode.class, Ie6Mode.AUTO,
            messageCollector, MessageType.UNSUPPORTED_IE6_MODE);
        if (StringUtils.isNotBlank(ie6ModeString) && format != SpriteImageFormat.PNG)
        {
            messageCollector.notice(MessageType.IGNORING_IE6_MODE, format.name());
        }

        // Matte color
        final Color matteColor;
        if (CssSyntaxUtils.hasNonBlankValue(rules, PROPERTY_SPRITE_MATTE_COLOR))
        {
            matteColor = CssSyntaxUtils.parseColor(
                rules.get(PROPERTY_SPRITE_MATTE_COLOR).value, messageCollector, null);
        }
        else
        {
            matteColor = null;
        }

        return new SpriteImageDirective(id, imagePath, layout, format, ie6Mode,
            matteColor);
    }

    private static <T extends Enum<T>> T valueOf(String stringValue, Class<T> enumClass,
        T defaultValue, MessageLog messageCollector, MessageType messageType)
    {
        if (StringUtils.isNotBlank(stringValue))
        {
            try
            {
                return Enum.valueOf(enumClass, stringValue.toUpperCase());
            }
            catch (IllegalArgumentException e)
            {
                messageCollector.warning(messageType, stringValue);
                return defaultValue;
            }
        }
        else
        {
            return defaultValue;
        }
    }
}
