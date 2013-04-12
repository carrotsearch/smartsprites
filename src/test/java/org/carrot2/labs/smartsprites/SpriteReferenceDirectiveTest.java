package org.carrot2.labs.smartsprites;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.carrot2.labs.test.Assertions.assertThat;

import java.awt.Color;
import java.util.Map;

import org.carrot2.labs.smartsprites.SpriteImageDirective.Ie6Mode;
import org.carrot2.labs.smartsprites.SpriteImageDirective.SpriteImageFormat;
import org.carrot2.labs.smartsprites.SpriteImageDirective.SpriteImageLayout;
import org.carrot2.labs.smartsprites.SpriteImageDirective.SpriteUidType;
import org.carrot2.labs.smartsprites.SpriteLayoutProperties.SpriteAlignment;
import org.carrot2.labs.smartsprites.message.Message;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

/**
 * Test cases for {@link SpriteReferenceDirective}
 */
public class SpriteReferenceDirectiveTest extends TestWithMemoryMessageSink
{
    private static final SpriteImageDirective VERTICAL_SPRITE_IMAGE_DIRECTIVE = new SpriteImageDirective(
        "vsprite", "sprite.png", SpriteImageLayout.VERTICAL, SpriteImageFormat.PNG,
        Ie6Mode.AUTO, Color.WHITE, SpriteUidType.NONE, 1);

    private static final SpriteImageDirective HORIZONTAL_SPRITE_IMAGE_DIRECTIVE = new SpriteImageDirective(
        "hsprite", "hsprite.png", SpriteImageLayout.HORIZONTAL, SpriteImageFormat.PNG,
        Ie6Mode.AUTO, Color.WHITE, SpriteUidType.NONE, 1);

    private static final SpriteImageDirective VERTICAL_SPRITE_IMAGE_DIRECTIVE_WITH_LAYOUT = new SpriteImageDirective(
        "vsprite", "sprite.png", SpriteImageLayout.VERTICAL, SpriteImageFormat.PNG,
        Ie6Mode.AUTO, Color.WHITE, SpriteUidType.NONE, 1, new SpriteLayoutProperties(
            SpriteAlignment.REPEAT, 1, 2, 3, 4));

    private static final Map<String, SpriteImageDirective> SPRITE_IMAGE_DIRECTIVES = ImmutableMap
        .of("vsprite", VERTICAL_SPRITE_IMAGE_DIRECTIVE, "hsprite",
            HORIZONTAL_SPRITE_IMAGE_DIRECTIVE, "vlsprite",
            VERTICAL_SPRITE_IMAGE_DIRECTIVE_WITH_LAYOUT);

    @Test
    public void testEmpty()
    {
        final MessageLog messageLog = new MessageLog();
        final SpriteReferenceDirective directive = SpriteReferenceDirective.parse("",
            SPRITE_IMAGE_DIRECTIVES, messageLog);
        assertNull(directive);
    }

    @Test
    public void testSpriteRefOnly()
    {
        final MessageLog messageLog = new MessageLog();
        final SpriteReferenceDirective directive = SpriteReferenceDirective.parse(
            "sprite-ref: vsprite", SPRITE_IMAGE_DIRECTIVES, messageLog);

        assertNotNull(directive);
        assertEquals("vsprite", directive.spriteRef);
        assertEquals(SpriteAlignment.LEFT, directive.spriteLayoutProperties.alignment);
        assertEquals(0, directive.spriteLayoutProperties.marginLeft);
        assertEquals(0, directive.spriteLayoutProperties.marginRight);
        assertEquals(0, directive.spriteLayoutProperties.marginTop);
        assertEquals(0, directive.spriteLayoutProperties.marginBottom);

        assertThat(messages).isEmpty();
    }

    @Test
    public void testSpriteRefOnlyHorizontalImage()
    {
        final MessageLog messageLog = new MessageLog();
        final SpriteReferenceDirective directive = SpriteReferenceDirective.parse(
            "sprite-ref: hsprite", SPRITE_IMAGE_DIRECTIVES, messageLog);

        assertNotNull(directive);
        assertEquals("hsprite", directive.spriteRef);
        assertEquals(SpriteAlignment.TOP, directive.spriteLayoutProperties.alignment);
        assertEquals(0, directive.spriteLayoutProperties.marginLeft);
        assertEquals(0, directive.spriteLayoutProperties.marginRight);
        assertEquals(0, directive.spriteLayoutProperties.marginTop);
        assertEquals(0, directive.spriteLayoutProperties.marginBottom);

        assertThat(messages).isEmpty();
    }

    @Test
    public void testSpriteRefNotFound()
    {
        final SpriteReferenceDirective directive = SpriteReferenceDirective.parse(
            "sprite-ref: spritex", SPRITE_IMAGE_DIRECTIVES, messageLog);

        assertNull(directive);

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.REFERENCED_SPRITE_NOT_FOUND, null, 0, "spritex"));
    }

    @Test
    public void testSpriteRefAlignment()
    {
        final MessageLog messageLog = new MessageLog();
        final SpriteReferenceDirective directive = SpriteReferenceDirective.parse(
            "sprite-ref: vsprite; sprite-alignment: repeat", SPRITE_IMAGE_DIRECTIVES,
            messageLog);

        assertNotNull(directive);
        assertEquals("vsprite", directive.spriteRef);
        assertEquals(SpriteAlignment.REPEAT, directive.spriteLayoutProperties.alignment);
        assertEquals(0, directive.spriteLayoutProperties.marginLeft);
        assertEquals(0, directive.spriteLayoutProperties.marginRight);
        assertEquals(0, directive.spriteLayoutProperties.marginTop);
        assertEquals(0, directive.spriteLayoutProperties.marginBottom);

        assertThat(messages).isEmpty();
    }

    @Test
    public void testUnsupportedAlignment()
    {
        final SpriteReferenceDirective directive = SpriteReferenceDirective.parse(
            "sprite-ref: vsprite; sprite-alignment: repeat-x", SPRITE_IMAGE_DIRECTIVES,
            messageLog);

        assertNotNull(directive);
        assertEquals("vsprite", directive.spriteRef);
        assertEquals(SpriteAlignment.LEFT, directive.spriteLayoutProperties.alignment);
        assertEquals(0, directive.spriteLayoutProperties.marginLeft);
        assertEquals(0, directive.spriteLayoutProperties.marginRight);
        assertEquals(0, directive.spriteLayoutProperties.marginTop);
        assertEquals(0, directive.spriteLayoutProperties.marginBottom);

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_ALIGNMENT, null, 0, "repeat-x"));
    }

    @Test
    public void testMismatchedTopAlignment()
    {
        checkMismatchedAlignment("vsprite", "top", SpriteAlignment.LEFT,
            Message.MessageType.ONLY_LEFT_OR_RIGHT_ALIGNMENT_ALLOWED);
    }

    @Test
    public void testMismatchedBottomAlignment()
    {
        checkMismatchedAlignment("vsprite", "bottom", SpriteAlignment.LEFT,
            Message.MessageType.ONLY_LEFT_OR_RIGHT_ALIGNMENT_ALLOWED);
    }

    @Test
    public void testMismatchedLeftAlignment()
    {
        checkMismatchedAlignment("hsprite", "left", SpriteAlignment.TOP,
            Message.MessageType.ONLY_TOP_OR_BOTTOM_ALIGNMENT_ALLOWED);
    }

    @Test
    public void testMismatchedRightAlignment()
    {
        checkMismatchedAlignment("hsprite", "right", SpriteAlignment.TOP,
            Message.MessageType.ONLY_TOP_OR_BOTTOM_ALIGNMENT_ALLOWED);
    }

    private void checkMismatchedAlignment(String sprite, final String alignment,
        SpriteAlignment correctedAlignment, MessageType message)
    {
        final SpriteReferenceDirective directive = SpriteReferenceDirective.parse(
            "sprite-ref: " + sprite + "; sprite-alignment: " + alignment,
            SPRITE_IMAGE_DIRECTIVES, messageLog);

        assertNotNull(directive);
        assertEquals(sprite, directive.spriteRef);
        assertEquals(correctedAlignment, directive.spriteLayoutProperties.alignment);
        assertEquals(0, directive.spriteLayoutProperties.marginLeft);
        assertEquals(0, directive.spriteLayoutProperties.marginRight);
        assertEquals(0, directive.spriteLayoutProperties.marginTop);
        assertEquals(0, directive.spriteLayoutProperties.marginBottom);

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN, message, null, 0, alignment));
    }

    @Test
    public void testSpriteMargins()
    {
        final SpriteReferenceDirective directive = SpriteReferenceDirective
            .parse(
                "sprite-ref: vsprite; sprite-margin-left: 10px; sprite-margin-right: 20; sprite-margin-top: 30px; sprite-margin-bottom: 40;",
                SPRITE_IMAGE_DIRECTIVES, messageLog);

        assertNotNull(directive);
        assertEquals("vsprite", directive.spriteRef);
        assertEquals(SpriteAlignment.LEFT, directive.spriteLayoutProperties.alignment);
        assertEquals(10, directive.spriteLayoutProperties.marginLeft);
        assertEquals(20, directive.spriteLayoutProperties.marginRight);
        assertEquals(30, directive.spriteLayoutProperties.marginTop);
        assertEquals(40, directive.spriteLayoutProperties.marginBottom);

        assertThat(messages).isEmpty();
    }

    @Test
    public void testCannotParseMargin()
    {
        final SpriteReferenceDirective directive = SpriteReferenceDirective
            .parse(
                "sprite-ref: vsprite; sprite-margin-left: 10zpx; sprite-margin-right: 20; sprite-margin-top: 30; sprite-margin-bottom: 40;",
                SPRITE_IMAGE_DIRECTIVES, messageLog);

        assertNotNull(directive);
        assertEquals("vsprite", directive.spriteRef);
        assertEquals(SpriteAlignment.LEFT, directive.spriteLayoutProperties.alignment);
        assertEquals(0, directive.spriteLayoutProperties.marginLeft);
        assertEquals(20, directive.spriteLayoutProperties.marginRight);
        assertEquals(30, directive.spriteLayoutProperties.marginTop);
        assertEquals(40, directive.spriteLayoutProperties.marginBottom);

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.CANNOT_PARSE_MARGIN_VALUE, null, 0, "10zpx"));
    }

    @Test
    public void testUnsupportedProperties()
    {
        final SpriteReferenceDirective directive = SpriteReferenceDirective.parse(
            "sprite-ref: vsprite; sprites-alignment: repeat; sprites-margin-left: 10px",
            SPRITE_IMAGE_DIRECTIVES, messageLog);

        assertNotNull(directive);
        assertEquals("vsprite", directive.spriteRef);

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_PROPERTIES_FOUND, null, 0,
                "sprites-alignment, sprites-margin-left"));
    }

    @Test
    public void testSpriteLayoutFromSpriteImageDirective()
    {
        final SpriteReferenceDirective directive = SpriteReferenceDirective.parse(
            "sprite-ref: vlsprite", SPRITE_IMAGE_DIRECTIVES, messageLog);

        assertNotNull(directive);
        assertEquals("vlsprite", directive.spriteRef);
        assertEquals(
            VERTICAL_SPRITE_IMAGE_DIRECTIVE_WITH_LAYOUT.spriteLayoutProperties.alignment,
            directive.spriteLayoutProperties.alignment);
        assertEquals(
            VERTICAL_SPRITE_IMAGE_DIRECTIVE_WITH_LAYOUT.spriteLayoutProperties.marginLeft,
            directive.spriteLayoutProperties.marginLeft);
        assertEquals(
            VERTICAL_SPRITE_IMAGE_DIRECTIVE_WITH_LAYOUT.spriteLayoutProperties.marginRight,
            directive.spriteLayoutProperties.marginRight);
        assertEquals(
            VERTICAL_SPRITE_IMAGE_DIRECTIVE_WITH_LAYOUT.spriteLayoutProperties.marginTop,
            directive.spriteLayoutProperties.marginTop);
        assertEquals(
            VERTICAL_SPRITE_IMAGE_DIRECTIVE_WITH_LAYOUT.spriteLayoutProperties.marginBottom,
            directive.spriteLayoutProperties.marginBottom);

        assertThat(messages).isEmpty();
    }

    @Test
    public void testOverriddenSpriteLayoutFromSpriteImageDirective()
    {
        final SpriteReferenceDirective directive = SpriteReferenceDirective
            .parse(
                "sprite-ref: vlsprite; sprite-alignment: right; sprite-margin-left: 10px; sprite-margin-right: 20; sprite-margin-top: 30px; sprite-margin-bottom: 40;",
                SPRITE_IMAGE_DIRECTIVES, messageLog);

        assertNotNull(directive);
        assertEquals("vlsprite", directive.spriteRef);
        assertEquals(SpriteAlignment.RIGHT, directive.spriteLayoutProperties.alignment);
        assertEquals(10, directive.spriteLayoutProperties.marginLeft);
        assertEquals(20, directive.spriteLayoutProperties.marginRight);
        assertEquals(30, directive.spriteLayoutProperties.marginTop);
        assertEquals(40, directive.spriteLayoutProperties.marginBottom);

        assertThat(messages).isEmpty();
    }

    @Test
    public void testNegativeMarginValues()
    {
        final SpriteReferenceDirective directive = SpriteReferenceDirective
        .parse(
            "sprite-ref: vlsprite; sprite-alignment: right; sprite-margin-left: -10px; sprite-margin-right: 20; sprite-margin-top: 30px; sprite-margin-bottom: -40;",
            SPRITE_IMAGE_DIRECTIVES, messageLog);

        assertNotNull(directive);
        assertEquals("vlsprite", directive.spriteRef);
        assertEquals(SpriteAlignment.RIGHT, directive.spriteLayoutProperties.alignment);
        assertEquals(0, directive.spriteLayoutProperties.marginLeft);
        assertEquals(20, directive.spriteLayoutProperties.marginRight);
        assertEquals(30, directive.spriteLayoutProperties.marginTop);
        assertEquals(0, directive.spriteLayoutProperties.marginBottom);

        assertThat(messages).contains(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.IGNORING_NEGATIVE_MARGIN_VALUE, null, 0,
                "sprite-margin-left"),
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.IGNORING_NEGATIVE_MARGIN_VALUE, null, 0,
                "sprite-margin-bottom"));
    }
}
