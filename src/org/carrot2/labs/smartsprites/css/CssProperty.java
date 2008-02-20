package org.carrot2.labs.smartsprites.css;

/**
 * Represents a single CSS property and its value, e.g.
 * <code>background-image: url(img.png)</code>.
 */
public class CssProperty
{
    public final String rule;
    public final String value;

    public CssProperty(String rule, String value)
    {
        this.rule = rule;
        this.value = value;
    }
}
