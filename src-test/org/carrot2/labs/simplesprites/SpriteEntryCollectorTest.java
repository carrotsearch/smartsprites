package org.carrot2.labs.simplesprites;

import static junit.framework.Assert.assertEquals;
import static org.carrot2.labs.test.TestEqualsHelper.wrap;
import static org.fest.assertions.Assertions.assertThat;

import org.carrot2.labs.simplesprites.message.Message;
import org.carrot2.labs.simplesprites.message.MessageLog;
import org.junit.Test;

/**
 * @author Stanislaw Osinski
 */
public class SpriteEntryCollectorTest extends TestWithMemoryMessageSink
{
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

        final MessageLog messageLog = new MessageLog();
        assertEquals("../img/img.png", SpriteDirectiveOccurrenceCollector
            .extractSpriteReferenceImageUrl(css, messageLog));
    }

    @Test
    public void testSpriteReferenceImageUrlExtractionNoBackgroundImage()
    {
        final String spriteDirective = "sprite-ref: sprite; sprite-alignment: repeat";
        final String css = "background-imagez: url('../img/img.png'); /** "
            + spriteDirective + " */";

        assertEquals(null, SpriteDirectiveOccurrenceCollector
            .extractSpriteReferenceImageUrl(css, messageLog));

        assertThat(wrap(messages))
            .contains(
                wrap(new Message(
                    Message.MessageLevel.WARN,
                    Message.MessageType.NO_BACKGROUND_IMAGE_RULE_NEXT_TO_SPRITE_REFERENCE_DIRECTIVE,
                    null, 0, css)));
    }

    @Test
    public void testSpriteReferenceImageUrlExtractionMoreRules()
    {
        final String spriteDirective = "sprite-ref: sprite; sprite-alignment: repeat";
        final String css = "color: red; background-image: url('../img/img.png'); /** "
            + spriteDirective + " */";

        assertEquals(null, SpriteDirectiveOccurrenceCollector
            .extractSpriteReferenceImageUrl(css, messageLog));

        assertThat(wrap(messages))
            .contains(
                wrap(new Message(
                    Message.MessageLevel.WARN,
                    Message.MessageType.MORE_THAN_ONE_RULE_NEXT_TO_SPRITE_REFERENCE_DIRECTIVE,
                    null, 0, css)));
    }
}
