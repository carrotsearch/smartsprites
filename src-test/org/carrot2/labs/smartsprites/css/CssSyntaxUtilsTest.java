package org.carrot2.labs.smartsprites.css;

import static org.carrot2.labs.test.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.util.List;

import org.carrot2.labs.smartsprites.TestWithMemoryMessageSink;
import org.carrot2.labs.smartsprites.message.Message;
import org.carrot2.labs.smartsprites.message.Message.MessageLevel;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.junit.Test;

/**
 * Test cases for {@link CssSyntaxUtils}.
 */
public class CssSyntaxUtilsTest extends TestWithMemoryMessageSink
{
    @Test
    public void testEmpty()
    {
        final List<CssProperty> actualRules = CssSyntaxUtils.extractProperties("   ");
        assertThat(actualRules).isEmpty();
    }

    @Test
    public void testSingleRule()
    {
        final List<CssProperty> actualRules = CssSyntaxUtils
            .extractProperties("test-rule: test-value");

        assertThat(actualRules)
            .isEquivalentTo(new CssProperty("test-rule", "test-value"));
    }

    @Test
    public void testImportantRules()
    {
        final List<CssProperty> actualRules = CssSyntaxUtils
            .extractProperties("background-image: url(test.png) !important; color: red!   important");

        assertThat(actualRules).isEquivalentTo(
            new CssProperty("background-image", "url(test.png)", true),
            new CssProperty("color", "red", true));
    }

    @Test
    public void testUpperCaseRule()
    {
        final List<CssProperty> actualRules = CssSyntaxUtils
            .extractProperties("TEST-rule: test-value");

        assertThat(actualRules)
            .isEquivalentTo(new CssProperty("test-rule", "test-value"));
    }

    @Test
    public void testMoreRules()
    {
        final List<CssProperty> actualRules = CssSyntaxUtils
            .extractProperties("rule-1: value1; rule-2: value2;");

        assertThat(actualRules).isEquivalentTo(new CssProperty("rule-1", "value1"),
            new CssProperty("rule-2", "value2"));
    }

    @Test
    public void testWhiteSpace()
    {
        final List<CssProperty> actualRules = CssSyntaxUtils
            .extractProperties("\trule-1  : value1  ; \trule-2  : value2\t;");

        assertThat(actualRules).isEquivalentTo(new CssProperty("rule-1", "value1"),
            new CssProperty("rule-2", "value2"));
    }

    @Test
    public void testTooManyColons()
    {
        final List<CssProperty> actualRules = CssSyntaxUtils.extractRules(
            "rule-1: value1 : v2; rule-2: value2;", messageLog);

        assertThat(actualRules).isEquivalentTo(new CssProperty("rule-2", "value2"));

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.MALFORMED_CSS_RULE, null, 0, "rule-1: value1 : v2"));
    }

    @Test
    public void testUnpackUrlNoQuotes()
    {
        assertEquals("test/img/t.png", CssSyntaxUtils.unpackUrl("url(test/img/t.png)"));
    }

    @Test
    public void testUnpackUrlSingleQuotes()
    {
        assertEquals("test/img/t.png", CssSyntaxUtils.unpackUrl("url('test/img/t.png')"));
    }

    @Test
    public void testUnpackUrlDoubleQuotes()
    {
        assertEquals("test/img/t.png", CssSyntaxUtils
            .unpackUrl("url(\"test/img/t.png\")"));
    }

    @Test
    public void testUnbalancedQuotes()
    {
        assertEquals(null, CssSyntaxUtils.unpackUrl("url('test/img/t.png\")", messageLog));

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN, Message.MessageType.MALFORMED_URL,
                null, 0, "url('test/img/t.png\")"));
    }

    @Test
    public void testMalformedPrefix()
    {
        assertEquals(null, CssSyntaxUtils.unpackUrl("urlx('test/img/t.png')", messageLog));

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN, Message.MessageType.MALFORMED_URL,
                null, 0, "urlx('test/img/t.png')"));
    }

    @Test
    public void testLongCssColor()
    {
        assertEquals("cafe01", parseCssColor("#cafe01"));
        assertThat(messages).doesNotHaveMessagesOfLevel(MessageLevel.WARN);
    }

    @Test
    public void testInvalidCssColor()
    {
        assertEquals(null, parseCssColor("cafe01"));
        assertThat(messages)
            .isEquivalentTo(
                new Message(MessageLevel.WARN, MessageType.MALFORMED_COLOR, null, 0,
                    "cafe01"));
    }

    @Test
    public void testShortCssColor()
    {
        // Currently unsupported
        assertEquals(null, parseCssColor("#fff"));
        assertThat(messages).isEquivalentTo(
            new Message(MessageLevel.WARN, MessageType.MALFORMED_COLOR, null, 0, "#fff"));
    }

    private String parseCssColor(String cssColor)
    {
        final Color color = CssSyntaxUtils.parseColor(cssColor, messageLog, null);
        if (color != null)
        {
            return Integer.toString(color.getRGB() & 0x00ffffff, 16);
        }
        else
        {
            return null;
        }
    }
}
