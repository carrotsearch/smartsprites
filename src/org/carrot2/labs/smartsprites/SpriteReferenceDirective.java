package org.carrot2.labs.smartsprites;

import java.util.*;

import org.carrot2.labs.smartsprites.SpriteImageDirective.SpriteImageLayout;
import org.carrot2.labs.smartsprites.css.CssProperty;
import org.carrot2.labs.smartsprites.css.CssSyntaxUtils;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.util.CollectionUtils;

import com.google.common.collect.Sets;

/**
 * Represents a directive that adds an individual image to a sprite image.
 */
public class SpriteReferenceDirective
{
    public static final String PROPERTY_SPRITE_ALIGNMENT = "sprite-alignment";
    public static final String PROPERTY_SPRITE_REF = "sprite-ref";
    public static final String PROPERTY_SPRITE_MARGIN_BOTTOM = "sprite-margin-bottom";
    public static final String PROPERTY_SPRITE_MARGIN_TOP = "sprite-margin-top";
    public static final String PROPERTY_SPRITE_MARGIN_RIGHT = "sprite-margin-right";
    public static final String PROPERTY_SPRITE_MARGIN_LEFT = "sprite-margin-left";

    /** Allowed properties of this directive */
    private static final HashSet<String> ALLOWED_PROPERTIES = Sets.newHashSet(
        PROPERTY_SPRITE_ALIGNMENT, PROPERTY_SPRITE_REF, PROPERTY_SPRITE_MARGIN_LEFT,
        PROPERTY_SPRITE_MARGIN_RIGHT, PROPERTY_SPRITE_MARGIN_TOP,
        PROPERTY_SPRITE_MARGIN_BOTTOM);

    /**
     * Alignment of the individual image within the sprite image.
     */
    public static enum SpriteAlignment {
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
        REPEAT;

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
    }

    /** Sprite id for this individual image */
    public final String spriteRef;

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

    public SpriteReferenceDirective(String spriteImageId, SpriteAlignment alignment,
        int marginLeft, int marginRight, int marginTop, int marginBottom)
    {
        this.spriteRef = spriteImageId;
        this.alignment = alignment;
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
        this.marginTop = marginTop;
        this.marginBottom = marginBottom;
    }

    /**
     * Parses a {@link SpriteReferenceDirective} from the provided {@link String},
     * logging messages to the provided {@link MessageLog}.
     */
    public static SpriteReferenceDirective parse(String directiveString,
        Map<String, SpriteImageDirective> spriteImages, MessageLog messageCollector)
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

        // Sprite-ref is required
        if (!CssSyntaxUtils.hasNonBlankValue(rules, PROPERTY_SPRITE_REF))
        {
            messageCollector.warning(MessageType.SPRITE_REF_NOT_FOUND);
            return null;
        }

        final String spriteRef = rules.get(PROPERTY_SPRITE_REF).value;

        // Check if referred sprite exists
        final SpriteImageDirective spriteImageDirective = spriteImages.get(spriteRef);

        // Referenced sprite not found
        if (spriteImageDirective == null)
        {
            messageCollector.warning(MessageType.REFERENCED_SPRITE_NOT_FOUND,
                spriteRef);
            return null;
        }

        // Alignment is optional
        SpriteAlignment alignment;
        if (CssSyntaxUtils.hasNonBlankValue(rules, PROPERTY_SPRITE_ALIGNMENT))
        {
            final String alignmentValue = rules.get(PROPERTY_SPRITE_ALIGNMENT).value;
            try
            {
                alignment = correctAlignment(spriteImageDirective, SpriteAlignment
                    .getValue(alignmentValue), messageCollector);
            }
            catch (final IllegalArgumentException e)
            {
                messageCollector.warning(MessageType.UNSUPPORTED_ALIGNMENT,
                    alignmentValue);
                alignment = getDefaultAlignment(spriteImageDirective);
            }
        }
        else
        {
            alignment = getDefaultAlignment(spriteImageDirective);
        }

        // Parse margins
        final int marginLeft = getMargin(PROPERTY_SPRITE_MARGIN_LEFT, rules,
            messageCollector);
        final int marginRight = getMargin(PROPERTY_SPRITE_MARGIN_RIGHT, rules,
            messageCollector);
        final int marginTop = getMargin(PROPERTY_SPRITE_MARGIN_TOP, rules,
            messageCollector);
        final int marginBottom = getMargin(PROPERTY_SPRITE_MARGIN_BOTTOM, rules,
            messageCollector);

        return new SpriteReferenceDirective(spriteRef, alignment, marginLeft,
            marginRight, marginTop, marginBottom);
    }

    /**
     * Corrects sprite alignment if necessary based on the layout of the enclosing sprite
     * image.
     */
    private static SpriteAlignment correctAlignment(
        SpriteImageDirective spriteImageDirective, SpriteAlignment alignment,
        MessageLog messageCollector)
    {
        if (spriteImageDirective.layout.equals(SpriteImageLayout.HORIZONTAL))
        {
            if (alignment.equals(SpriteAlignment.LEFT)
                || alignment.equals(SpriteAlignment.RIGHT))
            {
                messageCollector.warning(
                    MessageType.ONLY_TOP_OR_BOTTOM_ALIGNMENT_ALLOWED, alignment.value);
                return SpriteAlignment.TOP;
            }
        }
        else
        {
            if (alignment.equals(SpriteAlignment.TOP)
                || alignment.equals(SpriteAlignment.LEFT))
            {
                messageCollector.warning(
                    MessageType.ONLY_LEFT_OR_RIGHT_ALIGNMENT_ALLOWED, alignment.value);
                return SpriteAlignment.LEFT;
            }
        }

        return alignment;
    }

    /**
     * Returns default alignment for given sprite image directive.
     */
    private static SpriteAlignment getDefaultAlignment(
        SpriteImageDirective spriteImageDirective)
    {
        if (spriteImageDirective.layout.equals(SpriteImageLayout.HORIZONTAL))
        {
            return SpriteAlignment.TOP;
        }
        else
        {
            return SpriteAlignment.LEFT;
        }
    }

    /**
     * Parses margin value.
     */
    private static int getMargin(String marginRule, Map<String, CssProperty> rules,
        MessageLog messageLog)
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
                return Integer.parseInt(marginValue);
            }
            catch (final NumberFormatException e)
            {
                messageLog.warning(MessageType.CANNOT_PARSE_MARGIN_VALUE,
                    rawMarginValue);
                return 0;
            }
        }
        else
        {
            return 0;
        }
    }
}