package org.carrot2.labs.smartsprites;

import java.awt.Color;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.carrot2.labs.smartsprites.css.CssProperty;
import org.carrot2.labs.smartsprites.css.CssSyntaxUtils;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.util.CollectionUtils;

import com.google.common.collect.*;

/**
 * Represents a directive that declares an individual sprite image.
 */
public class SpriteImageDirective
{
    public static final String PROPERTY_SPRITE_ID = "sprite";
    public static final String PROPERTY_SPRITE_IMAGE_LAYOUT = "sprite-layout";
    public static final String PROPERTY_SPRITE_IMAGE_URL = "sprite-image";
    public static final String PROPERTY_SPRITE_IMAGE_UID_SUFFIX = "sprite-image-uid";
    public static final String PROPERTY_SPRITE_MATTE_COLOR = "sprite-matte-color";
    public static final String PROPERTY_SPRITE_IE6_MODE = "sprite-ie6-mode";

    /** A set of allowed properties */
    private static final Set<String> ALLOWED_PROPERTIES = ImmutableSet.of(
        PROPERTY_SPRITE_ID, PROPERTY_SPRITE_IMAGE_LAYOUT, PROPERTY_SPRITE_IMAGE_URL,
        PROPERTY_SPRITE_MATTE_COLOR, PROPERTY_SPRITE_IE6_MODE,
        PROPERTY_SPRITE_IMAGE_UID_SUFFIX);

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

        public static String valuesAsString()
        {
            final String list = Lists.newArrayList(values()).toString();
            return list.substring(1, list.length() - 1);
        }
    }

    /**
     * Defines the UID Generation Mode of this sprite.
     */
    public static enum SpriteUidType
    {
        /**
         * No UID extension.
         */
        NONE,

        /**
         * Append current timestamp as long.
         */
        DATE,

        /**
         * Append MD5 of the sprites file.
         */
        MD5;

        private String value;

        private SpriteUidType()
        {
            this.value = name().toLowerCase();
        }

        @Override
        public String toString()
        {
            return value;
        }

        public static String valuesAsString()
        {
            final String list = Lists.newArrayList(values()).toString();
            return list.substring(1, list.length() - 1);
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

        public static String valuesAsString()
        {
            final String list = Lists.newArrayList(values()).toString();
            return list.substring(1, list.length() - 1);
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

        public static String valuesAsString()
        {
            final String list = Lists.newArrayList(values()).toString();
            return list.substring(1, list.length() - 1);
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
     * Non-file-name extension after the sprite image path to force a cache update on
     * change, prefixed by '?'.
     */
    public final SpriteUidType uidType;

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

    /**
     * Sprite layout properties defined at the sprite image directive level. The defaults
     * provided here can be overridden at the sprite reference directive level.
     */
    public final SpriteLayoutProperties spriteLayoutProperties;

    public SpriteImageDirective(String id, String imageUrl, SpriteImageLayout layout,
        SpriteImageFormat format, Ie6Mode ie6Mode, Color matteColor, SpriteUidType uidType)
    {
        this(id, imageUrl, layout, format, ie6Mode, matteColor, uidType,
            new SpriteLayoutProperties(layout));
    }

    public SpriteImageDirective(String id, String imageUrl, SpriteImageLayout layout,
        SpriteImageFormat format, Ie6Mode ie6Mode, Color matteColor,
        SpriteUidType uidType, SpriteLayoutProperties spriteLayoutProperties)
    {
        this.spriteId = id;
        this.imagePath = imageUrl;
        this.layout = layout;
        this.format = format;
        this.ie6Mode = ie6Mode;
        this.matteColor = matteColor;
        this.uidType = uidType;
        this.spriteLayoutProperties = spriteLayoutProperties;
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
        properties.removeAll(SpriteLayoutProperties.ALLOWED_PROPERTIES);
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

        final SpriteUidType uidGenerator = valueOf(
            CssSyntaxUtils.getValue(rules, PROPERTY_SPRITE_IMAGE_UID_SUFFIX),
            SpriteUidType.class, SpriteUidType.NONE, messageCollector,
            MessageType.UNSUPPORTED_UID_TYPE);

        final String imagePath = CssSyntaxUtils.unpackUrl(rules
            .get(PROPERTY_SPRITE_IMAGE_URL).value);

        // Layout is optional
        final SpriteImageLayout layout = valueOf(
            CssSyntaxUtils.getValue(rules, PROPERTY_SPRITE_IMAGE_LAYOUT),
            SpriteImageLayout.class, SpriteImageLayout.VERTICAL, messageCollector,
            MessageType.UNSUPPORTED_LAYOUT);

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
                messageCollector.warning(MessageType.UNSUPPORTED_SPRITE_IMAGE_FORMAT,
                    formatValue);
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
            matteColor, uidGenerator, SpriteLayoutProperties.parse(directiveString,
                layout, messageCollector));
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
