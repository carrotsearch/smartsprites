package org.carrot2.labs.smartsprites;

import static junit.framework.Assert.assertEquals;
import static org.carrot2.labs.test.Assertions.assertThat;

import org.carrot2.labs.smartsprites.css.CssSyntaxUtils;
import org.carrot2.labs.smartsprites.message.Message;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for {@link SpriteDirectiveOccurrenceCollector}.
 */
public class SpriteDirectiveOccurrenceCollectorTest extends TestWithMemoryMessageSink
{
    SpriteDirectiveOccurrenceCollector spriteDirectiveOccurrenceCollector;

    @Before
    public void prepare()
    {
        spriteDirectiveOccurrenceCollector = new SpriteDirectiveOccurrenceCollector(
            messageLog);
    }

    @Test
    public void testSpriteImageDirectiveExtractionOneDirectiveComplex()
    {
        final String spriteDirective = "sprite: sprite; sprite-image-url: url('../sprite.png'); sprite-image-layout: vertical";
        final String css = ".test { margin-top: 10px }\n/* some comment */\n" + "/* "
            + spriteDirective + " */";

        assertEquals(spriteDirective, SpriteDirectiveOccurrenceCollector
            .extractSpriteImageDirectiveString(css));
    }

    @Test
    public void testSpriteImageDirectiveExtractionOneDirectiveSimple()
    {
        final String spriteDirective = "sprite: sprite";
        final String css = "/* " + spriteDirective + " */";

        assertEquals(spriteDirective, SpriteDirectiveOccurrenceCollector
            .extractSpriteImageDirectiveString(css));
    }

    @Test
    public void testSpriteImageDirectiveExtractionMoreDirectives()
    {
        final String spriteDirective1 = "sprite: sprite; sprite-image-url: url('../sprite.png'); sprite-image-layout: vertical";
        final String spriteDirective2 = "sprite: sprite2; sprite-image-url: url('../sprite2.png'); sprite-image-layout: horizontal";
        final String css = ".test { margin-top: 10px }\n/* some comment */\n" + "/* "
            + spriteDirective1 + " */\n" + ".rule { float: left }\n" + "/*** \t"
            + spriteDirective2 + " \t **/";

        assertEquals(spriteDirective1, SpriteDirectiveOccurrenceCollector
            .extractSpriteImageDirectiveString(css));
    }

    @Test
    public void testSpriteReferenceDirectiveExtraction()
    {
        final String spriteDirective = "sprite-ref: sprite; sprite-alignment: repeat";
        final String css = "background-image: url('../img/img.png'); /** "
            + spriteDirective + " */";

        assertEquals(spriteDirective, SpriteDirectiveOccurrenceCollector
            .extractSpriteReferenceDirectiveString(css));
    }

    @Test
    public void testSpriteReferenceImageUrlExtraction()
    {
        final String spriteDirective = "sprite-ref: sprite; sprite-alignment: repeat";
        final String css = "background-image: url('../img/img.png'); /** "
            + spriteDirective + " */";

        assertEquals("../img/img.png", CssSyntaxUtils.unpackUrl(spriteDirectiveOccurrenceCollector
            .extractSpriteReferenceCssProperty(css).value, null));
    }

    @Test
    public void testSpriteReferenceImageUrlExtractionNoBackgroundImage()
    {
        final String spriteDirective = "sprite-ref: sprite; sprite-alignment: repeat";
        final String css = "background-imagez: url('../img/img.png'); /** "
            + spriteDirective + " */";

        assertEquals(null, spriteDirectiveOccurrenceCollector
            .extractSpriteReferenceCssProperty(css));

        assertThat(messages)
            .isEquivalentTo(
                new Message(
                    Message.MessageLevel.WARN,
                    Message.MessageType.NO_BACKGROUND_IMAGE_RULE_NEXT_TO_SPRITE_REFERENCE_DIRECTIVE,
                    null, 0, css));
    }

    @Test
    public void testSpriteReferenceImageUrlExtractionMoreRules()
    {
        final String spriteDirective = "sprite-ref: sprite; sprite-alignment: repeat";
        final String css = "color: red; background-image: url('../img/img.png'); /** "
            + spriteDirective + " */";

        assertEquals(null, spriteDirectiveOccurrenceCollector
            .extractSpriteReferenceCssProperty(css));

        assertThat(messages)
            .isEquivalentTo(
                new Message(
                    Message.MessageLevel.WARN,
                    Message.MessageType.MORE_THAN_ONE_RULE_NEXT_TO_SPRITE_REFERENCE_DIRECTIVE,
                    null, 0, css));
    }
}
