package org.carrot2.labs.test;

import static org.fest.assertions.Assertions.assertThat;

import org.carrot2.labs.smartsprites.message.Message;

/**
 * Assertions on {@link Message}s.
 */
public class MessageAssertion
{
    /** The actual message */
    private Message actual;

    /** Assertion description */
    private String description = "message";

    /**
     * Creates a {@link Message} assertion object.
     */
    public MessageAssertion(Message actual)
    {
        this.actual = actual;
    }

    /**
     * Asserts that the current message is equivalent to the provided expected message.
     */
    public MessageAssertion isEquivalentTo(Message expected)
    {
        assertThat(actual).isNotNull();
        assertThat(actual.cssPath).as(description + ".cssPath").isEqualTo(
            expected.cssPath);
        assertThat(actual.line).as(description + ".line").isEqualTo(expected.line);
        assertThat(actual.level).as(description + ".level").isEqualTo(expected.level);
        assertThat(actual.type).as(description + ".type").isEqualTo(expected.type);
        assertThat(actual.arguments).as(description + ".arguments").isEqualTo(
            expected.arguments);

        return this;
    }

    public MessageAssertion as(String description)
    {
        this.description = description;
        return this;
    }
}
