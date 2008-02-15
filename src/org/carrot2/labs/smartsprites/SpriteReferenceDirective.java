package org.carrot2.labs.smartsprites;

import java.util.Map;

import org.carrot2.labs.smartsprites.SpriteImageDirective.SpriteImageLayout;
import org.carrot2.labs.smartsprites.css.CssRule;
import org.carrot2.labs.smartsprites.css.CssSyntaxUtils;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message.MessageType;

/**
 * @author Stanislaw Osinski
 */
public class SpriteReferenceDirective
{
    public static final String RULE_SPRITE_ALIGNMENT = "sprite-alignment";
    public static final String RULE_SPRITE_REF = "sprite-ref";

    public enum SpriteAlignment {
        LEFT, RIGHT, TOP, BOTTOM, REPEAT;

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

    public final String spriteRef;
    public final SpriteAlignment alignment;

    public final int marginLeft;
    public final int marginRight;
    public final int marginTop;
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

    public static SpriteReferenceDirective parse(String directiveString,
        Map<String, SpriteImageDirective> spriteImages, MessageLog messageCollector)
    {
        final Map<String, CssRule> rules = CssSyntaxUtils.rulesAsMap(CssSyntaxUtils
            .extractRules(directiveString, messageCollector));

        // Sprite-ref is required
        if (!CssSyntaxUtils.hasNonBlankValue(rules, RULE_SPRITE_REF))
        {
            messageCollector.logWarning(MessageType.SPRITE_REF_NOT_FOUND);
            return null;
        }

        final String spriteRef = rules.get(RULE_SPRITE_REF).value;

        // Check if referred sprite exists
        final SpriteImageDirective spriteImageDirective = spriteImages.get(spriteRef);

        // Referenced sprite not found
        if (spriteImageDirective == null)
        {
            messageCollector.logWarning(MessageType.REFERENCED_SPRITE_NOT_FOUND,
                spriteRef);
            return null;
        }

        // Alignment is optional
        SpriteAlignment alignment;
        if (CssSyntaxUtils.hasNonBlankValue(rules, RULE_SPRITE_ALIGNMENT))
        {
            final String alignmentValue = rules.get(RULE_SPRITE_ALIGNMENT).value;
            try
            {
                alignment = correctAlignment(spriteImageDirective, SpriteAlignment
                    .getValue(alignmentValue), messageCollector);
            }
            catch (final IllegalArgumentException e)
            {
                messageCollector.logWarning(MessageType.UNSUPPORTED_ALIGNMENT,
                    alignmentValue);
                alignment = getDefaultAlignment(spriteImageDirective);
            }
        }
        else
        {
            alignment = getDefaultAlignment(spriteImageDirective);
        }

        // Parse margins
        final int marginLeft = getMargin("sprite-margin-left", rules, messageCollector);
        final int marginRight = getMargin("sprite-margin-right", rules, messageCollector);
        final int marginTop = getMargin("sprite-margin-top", rules, messageCollector);
        final int marginBottom = getMargin("sprite-margin-bottom", rules,
            messageCollector);

        return new SpriteReferenceDirective(spriteRef, alignment, marginLeft,
            marginRight, marginTop, marginBottom);
    }

    private static SpriteAlignment correctAlignment(
        SpriteImageDirective spriteImageDirective, SpriteAlignment alignment,
        MessageLog messageCollector)
    {
        if (spriteImageDirective.layout.equals(SpriteImageLayout.HORIZONTAL))
        {
            if (alignment.equals(SpriteAlignment.LEFT)
                || alignment.equals(SpriteAlignment.RIGHT))
            {
                messageCollector.logWarning(
                    MessageType.ONLY_TOP_OR_BOTTOM_ALIGNMENT_ALLOWED, alignment.value);
                return SpriteAlignment.TOP;
            }
        }
        else
        {
            if (alignment.equals(SpriteAlignment.TOP)
                || alignment.equals(SpriteAlignment.LEFT))
            {
                messageCollector.logWarning(
                    MessageType.ONLY_LEFT_OR_RIGHT_ALIGNMENT_ALLOWED, alignment.value);
                return SpriteAlignment.LEFT;
            }
        }

        return alignment;
    }

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

    private static int getMargin(String marginRule, Map<String, CssRule> rules,
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
                messageLog.logWarning(MessageType.CANNOT_PARSE_MARGIN_VALUE,
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