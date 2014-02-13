package org.carrot2.labs.smartsprites;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.carrot2.labs.test.Assertions.assertThat;

import java.awt.Color;

import org.carrot2.labs.smartsprites.SpriteImageDirective.Ie6Mode;
import org.carrot2.labs.smartsprites.SpriteImageDirective.SpriteUidType;
import org.carrot2.labs.smartsprites.SpriteLayoutProperties.SpriteAlignment;
import org.carrot2.labs.smartsprites.layout.HorizontalLayout;
import org.carrot2.labs.smartsprites.layout.VerticalLayout;
import org.carrot2.labs.smartsprites.message.Message;
import org.carrot2.labs.smartsprites.message.Message.MessageLevel;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
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
    public void testIdUrlLayoutProvidedIe6Mode()
    {
        final SpriteImageDirective directive = SpriteImageDirective
            .parse(
                "sprite: sprite; sprite-image: url('../sprite.png'); sprite-layout: horizontal; sprite-ie6-mode: none",
                messageLog);

        assertNotNull(directive);
        assertEquals(directive.spriteId, "sprite");
        assertEquals(directive.imagePath, "../sprite.png");
        assertEquals(directive.format, SpriteImageDirective.SpriteImageFormat.PNG);
        assertEquals(directive.layout.getClass(), HorizontalLayout.class);
        assertEquals(Ie6Mode.NONE, directive.ie6Mode);
        assertThat(messages).isEmpty();
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
        assertEquals(directive.layout.getClass(), VerticalLayout.class);
        assertThat(messages).isEmpty();
    }

    @Test
    public void variablesCorrectSyntax()
    {
        checkImagePathVariableCorrect("../${date}/${sprite}-${md5}.png");
    }

    @Test
    public void variablesAndQueryStringCorrectSyntax()
    {
        checkImagePathVariableCorrect("../${sprite}-${md5}.png?${date}");
    }

    @Test
    public void variablesUnbalancedBrackets()
    {
        checkImagePathVariableIncorrect("../$sprite}-${md5}.png?${date}");
    }

    @Test
    public void variablesMissingDollar()
    {
        checkImagePathVariableIncorrect("../{sprite}-${md5}.png?${date}");
    }

    @Test
    public void variablesUnsupportedVariable()
    {
        checkImagePathUnsupportedVariable("abc");
    }

    @Test
    public void variablesEmptyVariable()
    {
        checkImagePathUnsupportedVariable("");
    }

    private void checkImagePathVariableCorrect(String path)
    {
        assertNotNull(SpriteImageDirective.parse("sprite: sprite; sprite-image: url('"
            + path + "')", messageLog));
        assertThat(messages).isEmpty();
    }

    private void checkImagePathVariableIncorrect(String path)
    {
        assertNotNull(SpriteImageDirective.parse("sprite: sprite; sprite-image: url('"
            + path + "')", messageLog));
        assertThat(messages).contains(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.MALFORMED_SPRITE_IMAGE_PATH, null, 0, path));
    }

    private void checkImagePathUnsupportedVariable(String variable)
    {
        assertNotNull(SpriteImageDirective.parse(
            "sprite: sprite; sprite-image: url('../img/${" + variable + "}.png')",
            messageLog));
        assertThat(messages).contains(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_VARIABLE_IN_SPRITE_IMAGE_PATH, null, 0,
                variable));
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
        assertEquals(directive.layout.getClass(), VerticalLayout.class);
        assertEquals(directive.matteColor, new Color(0x00f08231));
        assertThat(messages).isEmpty();
    }

    @Test
    public void testUidNone()
    {
        checkUidType("sprite-image-uid: none", SpriteUidType.NONE);
        assertThat(messages).isEmpty();
    }

    @Test
    public void testUidDate()
    {
        checkUidType("sprite-image-uid: date", SpriteUidType.DATE);
        assertThat(messages).contains(
            new Message(MessageLevel.DEPRECATION,
                MessageType.DEPRECATED_SPRITE_IMAGE_UID, null, 0, "date"));
    }

    @Test
    public void testUidMd5()
    {
        checkUidType("sprite-image-uid: md5", SpriteUidType.MD5);
        assertThat(messages).contains(
            new Message(MessageLevel.DEPRECATION,
                MessageType.DEPRECATED_SPRITE_IMAGE_UID, null, 0, "md5"));
    }

    @Test
    public void testUidUnknown()
    {
        checkUidType("sprite-image-uid: unknown", SpriteUidType.NONE);
        assertThat(messages).contains(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_UID_TYPE, null, 0, "unknown"));
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
        assertEquals(directive.layout.getClass(), VerticalLayout.class);

        assertThat(messages)
            .isEquivalentTo(
                new Message(Message.MessageLevel.WARN,
                    Message.MessageType.CANNOT_DETERMINE_IMAGE_FORMAT, null, 0,
                    "../sprite."));
    }

    @Test
    public void testUnsupportedSpriteImageFormat()
    {
        final SpriteImageDirective directive = SpriteImageDirective.parse(
            "sprite: sprite; sprite-image: url('../sprite.jpgx')", messageLog);

        assertNotNull(directive);
        assertEquals(directive.spriteId, "sprite");
        assertEquals(directive.imagePath, "../sprite.jpgx");
        assertEquals(directive.format, SpriteImageDirective.SpriteImageFormat.PNG);
        assertEquals(directive.layout.getClass(), VerticalLayout.class);

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_SPRITE_IMAGE_FORMAT, null, 0, "jpgx"));
    }

    @Test
    public void testLeadingSpaceInUrl()
    {
        final SpriteImageDirective directive = SpriteImageDirective.parse(
            "sprite: sprite; sprite-image: url(../sprite.png )", messageLog);

        assertNotNull(directive);
        assertEquals(directive.spriteId, "sprite");
        assertEquals(directive.imagePath, "../sprite.png");
        assertEquals(directive.format, SpriteImageDirective.SpriteImageFormat.PNG);
        assertEquals(directive.layout.getClass(), VerticalLayout.class);

        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);
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
        assertEquals(directive.layout.getClass(), VerticalLayout.class);

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_LAYOUT, null, 0, "other"));
    }

    @Test
    public void testUnsupportedIe6Mode()
    {
        final SpriteImageDirective directive = SpriteImageDirective.parse(
            "sprite: sprite; sprite-image: url('../sprite.png'); sprite-ie6-mode: other",
            messageLog);

        assertNotNull(directive);
        assertEquals(directive.spriteId, "sprite");
        assertEquals(directive.imagePath, "../sprite.png");

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_IE6_MODE, null, 0, "other"));
    }

    @Test
    public void testIgnoredIe6Mode()
    {
        final SpriteImageDirective directive = SpriteImageDirective.parse(
            "sprite: sprite; sprite-image: url('../sprite.gif'); sprite-ie6-mode: auto",
            messageLog);

        assertNotNull(directive);
        assertEquals(directive.spriteId, "sprite");
        assertEquals(directive.imagePath, "../sprite.gif");

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.IE6NOTICE,
                Message.MessageType.IGNORING_IE6_MODE, null, 0, "GIF"));
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

    @Test
    public void testSpriteLayoutProperties()
    {
        final SpriteImageDirective directive = SpriteImageDirective
            .parse(
                "sprite: sprite; sprite-image: url('../sprite.png'); sprite-layout: horizontal; "
                    + "sprite-alignment: bottom; sprite-margin-left: 10px; sprite-margin-right: 20; sprite-margin-top: 30px; sprite-margin-bottom: 40;",
                messageLog);

        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);
        assertNotNull(directive);
        assertEquals(directive.spriteId, "sprite");
        assertEquals(directive.imagePath, "../sprite.png");
        assertEquals(directive.format, SpriteImageDirective.SpriteImageFormat.PNG);
        assertEquals(directive.layout.getClass(), HorizontalLayout.class);

        assertEquals(directive.spriteLayoutProperties.alignment, SpriteAlignment.BOTTOM);
        assertEquals(directive.spriteLayoutProperties.marginLeft, 10);
        assertEquals(directive.spriteLayoutProperties.marginRight, 20);
        assertEquals(directive.spriteLayoutProperties.marginTop, 30);
        assertEquals(directive.spriteLayoutProperties.marginBottom, 40);
    }

    @Test
    public void testNegativeMarginValues()
    {
        final SpriteImageDirective directive = SpriteImageDirective
            .parse(
                "sprite: sprite; sprite-image: url('../sprite.png'); sprite-layout: horizontal; "
                    + "sprite-alignment: bottom; sprite-margin-left: -5px; sprite-margin-right: 20; sprite-margin-top: 30px; sprite-margin-bottom: -40;",
                messageLog);

        assertThat(messages).contains(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.IGNORING_NEGATIVE_MARGIN_VALUE, null, 0,
                "sprite-margin-left"),
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.IGNORING_NEGATIVE_MARGIN_VALUE, null, 0,
                "sprite-margin-bottom"));
        assertNotNull(directive);
        assertEquals(directive.spriteId, "sprite");
        assertEquals(directive.imagePath, "../sprite.png");
        assertEquals(directive.format, SpriteImageDirective.SpriteImageFormat.PNG);
        assertEquals(directive.layout.getClass(), HorizontalLayout.class);

        assertEquals(directive.spriteLayoutProperties.alignment, SpriteAlignment.BOTTOM);
        assertEquals(0, directive.spriteLayoutProperties.marginLeft);
        assertEquals(20, directive.spriteLayoutProperties.marginRight);
        assertEquals(30, directive.spriteLayoutProperties.marginTop);
        assertEquals(0, directive.spriteLayoutProperties.marginBottom);
    }

    @Test
    public void testSpriteScalingProperty()
    {
        final SpriteImageDirective directive = SpriteImageDirective
            .parse(
                "sprite: sprite; sprite-image: url('../sprite.png'); sprite-layout: horizontal; "
                    + "sprite-scale: 2;",
                messageLog);

        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);
        assertNotNull(directive);
        assertEquals(directive.spriteId, "sprite");
        assertEquals(directive.imagePath, "../sprite.png");
        assertEquals(directive.scaleRatio, 2f);
    }

    private void checkUidType(String uidDeclaration, SpriteUidType expectedUidType)
    {
        final SpriteImageDirective directive = SpriteImageDirective.parse(
            "sprite: sprite; sprite-image: url('../sprite.png'); " + uidDeclaration,
            messageLog);

        assertNotNull(directive);
        assertEquals(directive.spriteId, "sprite");
        assertEquals(directive.imagePath, "../sprite.png");
        assertEquals(directive.uidType, expectedUidType);
    }
}
