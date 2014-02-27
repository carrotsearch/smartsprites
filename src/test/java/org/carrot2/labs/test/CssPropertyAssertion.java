package org.carrot2.labs.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import org.carrot2.labs.smartsprites.css.CssProperty;

/**
 * Assertions on instances of {@link CssProperty}.
 */
public class CssPropertyAssertion
{
    /** The actual property */
    private CssProperty actual;

    /** Assertion description */
    private String description = "cssProperty";

    /**
     * Creates a {@link CssProperty} assertion object.
     */
    public CssPropertyAssertion(CssProperty actual)
    {
        this.actual = actual;
    }

    /**
     * Asserts that the current property is equivalent to the provided expected property.
     */
    public CssPropertyAssertion isEquivalentTo(CssProperty expected)
    {
        assertNotNull(actual);
        assertThat(actual.rule).as(description + ".rule").isEqualTo(actual.rule);
        assertThat(actual.value).as(description + ".value").isEqualTo(actual.value);

        return this;
    }

    public CssPropertyAssertion as(String description)
    {
        this.description = description;
        return this;
    }
}
