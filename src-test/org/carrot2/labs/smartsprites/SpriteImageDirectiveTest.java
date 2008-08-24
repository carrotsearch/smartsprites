package org.carrot2.labs.smartsprites;

import static junit.framework.Assert.*;
import static org.carrot2.labs.test.Assertions.assertThat;

import java.awt.Color;

import org.carrot2.labs.smartsprites.message.Message;
import org.junit.Test;

/**
 * Test cases for {@link SpriteImageDirective}.
 */
public class SpriteImageDirectiveTest extends TestWithMemoryMessageSink
{
    @Test
    public void testEmpty()
    {
        final SpriteImageDirective directive = SpriteImageDirective.parse("", messageLog);
        assertNull(directive);
    }

    @Test
    public void testIdUrlLayoutProvided()
    {
        final SpriteImageDirective directive = SpriteImageDirective
            .parse(
                "sprite: sprite; sprite-image: url('../sprite.png'); sprite-layout: horizontal",
                messageLog);

        assertNotNull(directive);
        assertEquals(directive.spriteId, "sprite");
        assertEquals(directive.imagePath, "../sprite.png");
        assertEquals(directive.format, SpriteImageDirective.SpriteImageFormat.PNG);
        assertEquals(directive.layout, SpriteImageDirective.SpriteImageLayout.HORIZONTAL);
    }

    @Test
    public void testIdUrlProvided()
    {
        final SpriteImageDirective directive = SpriteImageDirective.parse(
            "sprite: sprite; sprite-image: url('../sprite.png')", messageLog);

        assertNotNull(directive);
        assertEquals(directive.spriteId, "sprite");
        assertEquals(directive.imagePath, "../sprite.png");
        assertEquals(directive.format, SpriteImageDirective.SpriteImageFormat.PNG);
        assertEquals(directive.layout, SpriteImageDirective.SpriteImageLayout.VERTICAL);
    }

    @Test
    public void testMatteColor()
    {
        final SpriteImageDirective directive = SpriteImageDirective
            .parse(
                "sprite: sprite; sprite-image: url('../sprite.png'); sprite-matte-color: #f08231",
                messageLog);

        assertNotNull(directive);
        assertEquals(directive.spriteId, "sprite");
        assertEquals(directive.imagePath, "../sprite.png");
        assertEquals(directive.format, SpriteImageDirective.SpriteImageFormat.PNG);
        assertEquals(directive.layout, SpriteImageDirective.SpriteImageLayout.VERTICAL);
        assertEquals(directive.matteColor, new Color(0x00f08231));
    }

    @Test
    public void testNoId()
    {
        final SpriteImageDirective directive = SpriteImageDirective.parse(
            "sprite-image: url('../sprite.png')", messageLog);

        assertNull(directive);

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.SPRITE_ID_NOT_FOUND, null, 0));
    }

    @Test
    public void testNoUrl()
    {
        final SpriteImageDirective directive = SpriteImageDirective.parse(
            "sprite: sprite;", messageLog);

        assertNull(directive);

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.SPRITE_IMAGE_URL_NOT_FOUND, null, 0));
    }

    @Test
    public void testUnrecognizedFormat()
    {
        final SpriteImageDirective directive = SpriteImageDirective.parse(
            "sprite: sprite; sprite-image: url('../sprite.')", messageLog);

        assertNotNull(directive);
        assertEquals(directive.spriteId, "sprite");
        assertEquals(directive.imagePath, "../sprite.");
        assertEquals(directive.format, SpriteImageDirective.SpriteImageFormat.PNG);
        assertEquals(directive.layout, SpriteImageDirective.SpriteImageLayout.VERTICAL);

        assertThat(messages)
            .isEquivalentTo(
                new Message(Message.MessageLevel.WARN,
                    Message.MessageType.CANNOT_DETERMINE_IMAGE_FORMAT, null, 0,
                    "../sprite."));
    }

    @Test
    public void testUnsupportedFormat()
    {
        final SpriteImageDirective directive = SpriteImageDirective.parse(
            "sprite: sprite; sprite-image: url('../sprite.jpgx')", messageLog);

        assertNotNull(directive);
        assertEquals(directive.spriteId, "sprite");
        assertEquals(directive.imagePath, "../sprite.jpgx");
        assertEquals(directive.format, SpriteImageDirective.SpriteImageFormat.PNG);
        assertEquals(directive.layout, SpriteImageDirective.SpriteImageLayout.VERTICAL);

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_FORMAT, null, 0, "jpgx"));
    }

    @Test
    public void testUnsupportedLayout()
    {
        final SpriteImageDirective directive = SpriteImageDirective.parse(
            "sprite: sprite; sprite-image: url('../sprite.jpg'); sprite-layout: other",
            messageLog);

        assertNotNull(directive);
        assertEquals(directive.spriteId, "sprite");
        assertEquals(directive.imagePath, "../sprite.jpg");
        assertEquals(directive.format, SpriteImageDirective.SpriteImageFormat.JPG);
        assertEquals(directive.layout, SpriteImageDirective.SpriteImageLayout.VERTICAL);

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_LAYOUT, null, 0, "other"));
    }

    @Test
    public void testUnsupportedProperties()
    {
        final SpriteImageDirective directive = SpriteImageDirective
            .parse(
                "sprite: sprite; sprites-image: url('../sprite.png'); sprites-layout: horizontal",
                messageLog);

        assertNull(directive);
        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_PROPERTIES_FOUND, null, 0,
                "sprites-image, sprites-layout"),
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.SPRITE_IMAGE_URL_NOT_FOUND, null, 0));
    }
}
