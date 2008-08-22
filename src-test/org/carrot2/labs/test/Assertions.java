package org.carrot2.labs.test;

import java.awt.image.BufferedImage;
import java.util.List;

import org.carrot2.labs.smartsprites.css.CssProperty;
import org.carrot2.labs.smartsprites.message.Message;

/**
 * FEST-style assertions for SmartSprites-specific data types.
 */
public class Assertions
{
    /**
     * Creates a {@link MessageAssertion}.
     */
    public static MessageAssertion assertThat(Message actual)
    {
        return new MessageAssertion(actual);
    }

    /**
     * Creates a {@link MessageListAssertion}.
     */
    public static MessageListAssertion assertThat(List<Message> actual)
    {
        return new MessageListAssertion(actual);
    }

    /**
     * Creates a {@link CssPropertyAssertion}.
     */
    public static CssPropertyAssertion assertThat(CssProperty actual)
    {
        return new CssPropertyAssertion(actual);
    }

    /**
     * Creates a {@link CssPropertyListAssertion}.
     */
    public static CssPropertyListAssertion assertThat(List<CssProperty> actual)
    {
        return new CssPropertyListAssertion(actual);
    }

    /**
     * Creates a {@link BufferedImageAssertion}.
     */
    public static BufferedImageAssertion assertThat(BufferedImage actual)
    {
        return new BufferedImageAssertion(actual);
    }
}
