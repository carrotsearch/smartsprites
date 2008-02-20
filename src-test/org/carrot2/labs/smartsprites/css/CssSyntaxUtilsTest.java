package org.carrot2.labs.smartsprites.css;

import static org.carrot2.labs.test.TestEqualsHelper.wrap;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.carrot2.labs.smartsprites.TestWithMemoryMessageSink;
import org.carrot2.labs.smartsprites.message.Message;
import org.junit.Test;

/**
 * Test cases for {@link CssSyntaxUtils}.
 */
public class CssSyntaxUtilsTest extends TestWithMemoryMessageSink
{
    @Test
    public void testEmpty()
    {
        final Collection<CssProperty> actualRules = CssSyntaxUtils
            .extractProperties("   ");
        assertThat(actualRules).isEmpty();
    }

    @Test
    public void testSingleRule()
    {
        final Collection<CssProperty> actualRules = CssSyntaxUtils
            .extractProperties("test-rule: test-value");

        assertThat(wrap(actualRules)).containsOnly(
            wrap(new CssProperty("test-rule", "test-value")));
    }

    @Test
    public void testUpperCaseRule()
    {
        final Collection<CssProperty> actualRules = CssSyntaxUtils
            .extractProperties("TEST-rule: test-value");

        assertThat(wrap(actualRules)).containsOnly(
            wrap(new CssProperty("test-rule", "test-value")));
    }

    @Test
    public void testMoreRules()
    {
        final Collection<CssProperty> actualRules = CssSyntaxUtils
            .extractProperties("rule-1: value1; rule-2: value2;");

        assertThat(wrap(actualRules)).containsOnly(
            wrap(new CssProperty("rule-1", "value1")),
            wrap(new CssProperty("rule-2", "value2")));
    }

    @Test
    public void testWhiteSpace()
    {
        final Collection<CssProperty> actualRules = CssSyntaxUtils
            .extractProperties("\trule-1  : value1  ; \trule-2  : value2\t;");

        assertThat(wrap(actualRules)).containsOnly(
            wrap(new CssProperty("rule-1", "value1")),
            wrap(new CssProperty("rule-2", "value2")));
    }

    @Test
    public void testTooManyColons()
    {
        final Collection<CssProperty> actualRules = CssSyntaxUtils.extractRules(
            "rule-1: value1 : v2; rule-2: value2;", messageLog);

        assertThat(wrap(actualRules)).containsOnly(
            wrap(new CssProperty("rule-2", "value2")));

        assertThat(wrap(messages)).contains(
            wrap(new Message(Message.MessageLevel.WARN,
                Message.MessageType.MALFORMED_CSS_RULE, null, 0, "rule-1: value1 : v2")));
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

        assertThat(wrap(messages)).contains(
            wrap(new Message(Message.MessageLevel.WARN,
                Message.MessageType.MALFORMED_URL, null, 0, "url('test/img/t.png\")")));
    }

    @Test
    public void testMalformedPrefix()
    {
        assertEquals(null, CssSyntaxUtils.unpackUrl("urlx('test/img/t.png')", messageLog));

        assertThat(wrap(messages)).contains(
            wrap(new Message(Message.MessageLevel.WARN,
                Message.MessageType.MALFORMED_URL, null, 0, "urlx('test/img/t.png')")));
    }
}
