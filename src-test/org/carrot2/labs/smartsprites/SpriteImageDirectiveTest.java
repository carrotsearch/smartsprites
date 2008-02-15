package org.carrot2.labs.smartsprites;

import static junit.framework.Assert.*;
import static org.carrot2.labs.test.TestEqualsHelper.wrap;
import static org.fest.assertions.Assertions.assertThat;

import org.carrot2.labs.smartsprites.message.Message;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.junit.Test;

/**
 * @author Stanislaw Osinski
 */
public class SpriteImageDirectiveTest extends TestWithMemoryMessageSink
{
    @Test
    public void testEmpty()
    {
        final MessageLog messageLog = new MessageLog();
        final SpriteImageDirective directive = SpriteImageDirective.parse("", messageLog);
        assertNull(directive);
    }

    @Test
    public void testIdUrlLayoutProvided()
    {
        final MessageLog messageLog = new MessageLog();
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
        final MessageLog messageLog = new MessageLog();
        final SpriteImageDirective directive = SpriteImageDirective.parse(
            "sprite: sprite; sprite-image: url('../sprite.png')", messageLog);

        assertNotNull(directive);
        assertEquals(directive.spriteId, "sprite");
        assertEquals(directive.imagePath, "../sprite.png");
        assertEquals(directive.format, SpriteImageDirective.SpriteImageFormat.PNG);
        assertEquals(directive.layout, SpriteImageDirective.SpriteImageLayout.VERTICAL);
    }

    @Test
    public void testNoId()
    {
        final SpriteImageDirective directive = SpriteImageDirective.parse(
            "sprite-image: url('../sprite.png')", messageLog);

        assertNull(directive);

        assertThat(wrap(messages)).contains(
            wrap(new Message(Message.MessageLevel.WARN,
                Message.MessageType.SPRITE_ID_NOT_FOUND, null, 0)));
    }

    @Test
    public void testNoUrl()
    {
        final SpriteImageDirective directive = SpriteImageDirective.parse(
            "sprite: sprite;", messageLog);

        assertNull(directive);

        assertThat(wrap(messages)).contains(
            wrap(new Message(Message.MessageLevel.WARN,
                Message.MessageType.SPRITE_IMAGE_URL_NOT_FOUND, null, 0)));
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

        assertThat(wrap(messages))
            .contains(
                wrap(new Message(Message.MessageLevel.WARN,
                    Message.MessageType.CANNOT_DETERMINE_IMAGE_FORMAT, null, 0,
                    "../sprite.")));
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

        assertThat(wrap(messages)).contains(
            wrap(new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_FORMAT, null, 0, "jpgx")));
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

        assertThat(wrap(messages)).contains(
            wrap(new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_LAYOUT, null, 0, "other")));
    }
}
