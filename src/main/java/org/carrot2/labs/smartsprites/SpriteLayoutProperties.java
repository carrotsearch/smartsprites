package org.carrot2.labs.smartsprites;

import java.util.Map;
import java.util.Set;

import org.carrot2.labs.smartsprites.css.CssProperty;
import org.carrot2.labs.smartsprites.css.CssSyntaxUtils;
import org.carrot2.labs.smartsprites.layout.SpriteImageLayout;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.labs.smartsprites.message.MessageLog;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * Represents common sprite layout properties that can be used both in
 * {@link SpriteImageDirective} and {@link SpriteReferenceDirective}.
 */
public class SpriteLayoutProperties
{
    public static final String PROPERTY_SPRITE_ALIGNMENT = "sprite-alignment";
    public static final String PROPERTY_SPRITE_MARGIN_BOTTOM = "sprite-margin-bottom";
    public static final String PROPERTY_SPRITE_MARGIN_TOP = "sprite-margin-top";
    public static final String PROPERTY_SPRITE_MARGIN_RIGHT = "sprite-margin-right";
    public static final String PROPERTY_SPRITE_MARGIN_LEFT = "sprite-margin-left";

    /** Allowed properties of this directive */
    static final Set<String> ALLOWED_PROPERTIES = ImmutableSet.of(
        PROPERTY_SPRITE_ALIGNMENT, PROPERTY_SPRITE_MARGIN_LEFT,
        PROPERTY_SPRITE_MARGIN_RIGHT, PROPERTY_SPRITE_MARGIN_TOP,
        PROPERTY_SPRITE_MARGIN_BOTTOM);

    /**
     * Alignment of the individual image within the sprite image.
     */
    public static enum SpriteAlignment
    {
        /**
         * To the left edge of a vertical sprite.
         */
        LEFT,

        /**
         * To the right edge of a vertical sprite.
         */
        RIGHT,

        /**
         * To the top edge of a horizontal sprite.
         */
        TOP,

        /**
         * To the bottom edge of a horizontal sprite.
         */
        BOTTOM,

        /**
         * Repeated across the full width/ height of the sprite image.
         */
        REPEAT,
        
        /**
         * To the center of a vertical or horizontal sprite
         */
        CENTER;

        private String value;

        private SpriteAlignment()
        {
            this.value = name().toLowerCase();
        }

        @Override
        public String toString()
        {
            return value;
        }

        public static SpriteAlignment getValue(String value)
        {
            return valueOf(value.toUpperCase());
        }

        public static String valuesAsString()
        {
            final String list = Lists.newArrayList(values()).toString();
            return list.substring(1, list.length() - 1);
        }
    }

    /** Alignment of this individual image */
    public final SpriteAlignment alignment;

    /** Left margin of the individual image */
    public final int marginLeft;

    /** Right margin of the individual image */
    public final int marginRight;

    /** Top margin of the individual image */
    public final int marginTop;

    /** Bottom margin of the individual image */
    public final int marginBottom;

    public SpriteLayoutProperties(SpriteAlignment alignment, int marginLeft,
        int marginRight, int marginTop, int marginBottom)
    {
        this.alignment = alignment;
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
        this.marginTop = marginTop;
        this.marginBottom = marginBottom;
    }

    /**
     * Creates an instance with default values.
     */
    SpriteLayoutProperties(SpriteImageLayout layout)
    {
        this(getDefaultAlignment(layout), 0, 0, 0, 0);
    }

    /**
     * Parses a {@link SpriteLayoutProperties} from the provided {@link String} logging
     * messages to the provided {@link MessageLog}.
     */
    public static SpriteLayoutProperties parse(String directiveString,
        SpriteImageLayout spriteImageLayout, MessageLog messageCollector)
    {
        return parse(directiveString, spriteImageLayout, new SpriteLayoutProperties(
            spriteImageLayout), messageCollector);
    }

    /**
     * Parses a {@link SpriteLayoutProperties} from the provided {@link String}, using the
     * provided defaults and logging messages to the provided {@link MessageLog}.
     */
    public static SpriteLayoutProperties parse(String directiveString,
        SpriteImageLayout spriteImageLayout, SpriteLayoutProperties defaults,
        MessageLog messageCollector)
    {
        final Map<String, CssProperty> rules = CssSyntaxUtils
            .propertiesAsMap(CssSyntaxUtils.extractRules(directiveString,
                messageCollector));

        // We don't check for allowed properties here. The check, including
        // sprite layout properties will be done when parsing the directive
        // that embeds sprite layout properties.

        // Alignment is optional
        SpriteAlignment alignment;
        if (CssSyntaxUtils.hasNonBlankValue(rules, PROPERTY_SPRITE_ALIGNMENT))
        {
            final String alignmentValue = rules.get(PROPERTY_SPRITE_ALIGNMENT).value;
            try
            {
                alignment = correctAlignment(spriteImageLayout,
                    SpriteAlignment.getValue(alignmentValue), messageCollector);
            }
            catch (final IllegalArgumentException e)
            {
                messageCollector.warning(MessageType.UNSUPPORTED_ALIGNMENT,
                    alignmentValue);
                alignment = getDefaultAlignment(spriteImageLayout);
            }
        }
        else
        {
            alignment = defaults.alignment;
        }

        // Parse margins
        final int marginLeft = getMargin(PROPERTY_SPRITE_MARGIN_LEFT, rules,
            defaults.marginLeft, messageCollector);
        final int marginRight = getMargin(PROPERTY_SPRITE_MARGIN_RIGHT, rules,
            defaults.marginRight, messageCollector);
        final int marginTop = getMargin(PROPERTY_SPRITE_MARGIN_TOP, rules,
            defaults.marginTop, messageCollector);
        final int marginBottom = getMargin(PROPERTY_SPRITE_MARGIN_BOTTOM, rules,
            defaults.marginBottom, messageCollector);

        return new SpriteLayoutProperties(alignment, marginLeft, marginRight, marginTop,
            marginBottom);
    }

    /**
     * Corrects sprite alignment if necessary based on the layout of the enclosing sprite
     * image.
     */
    private static SpriteAlignment correctAlignment(SpriteImageLayout spriteImageLayout,
        SpriteAlignment alignment, MessageLog messageCollector)
    {
        return spriteImageLayout.correctAlignment(alignment, messageCollector);
    }

    /**
     * Returns default alignment for given sprite image directive.
     */
    private static SpriteAlignment getDefaultAlignment(SpriteImageLayout spriteImageLayout)
    {
        return spriteImageLayout.getDefaultAlignment();
    }

    /**
     * Parses margin value.
     */
    private static int getMargin(String marginRule, Map<String, CssProperty> rules,
        int defaultMargin, MessageLog messageLog)
    {
        if (CssSyntaxUtils.hasNonBlankValue(rules, marginRule))
        {
            final String rawMarginValue = rules.get(marginRule).value;
            String marginValue = rawMarginValue;
            if (marginValue.toLowerCase().endsWith("px"))
            {
                marginValue = marginValue.substring(0, marginValue.length() - 2);
            }
            try
            {
                int marginIntValue = Integer.parseInt(marginValue);
                if (marginIntValue < 0)
                {
                    messageLog.warning(MessageType.IGNORING_NEGATIVE_MARGIN_VALUE,
                        marginRule);
                    marginIntValue = 0;
                }

                return marginIntValue;
            }
            catch (final NumberFormatException e)
            {
                messageLog.warning(MessageType.CANNOT_PARSE_MARGIN_VALUE, rawMarginValue);
                return 0;
            }
        }
        else
        {
            return defaultMargin;
        }
    }
}