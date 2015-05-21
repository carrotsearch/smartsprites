package org.carrot2.labs.test;

import java.util.List;

import org.carrot2.labs.smartsprites.css.CssProperty;
import org.carrot2.labs.smartsprites.message.Message;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;

/**
 * Assertions on lists of {@link CssProperty} instances.
 */
public class CssPropertyListAssertion
{
    /** The actual message list */
    private List<CssProperty> actual;

    /**
     * Creates a {@link Message} list assertion object.
     */
    public CssPropertyListAssertion(List<CssProperty> actual)
    {
        this.actual = actual;
    }

    /**
     * Asserts that the current message list is equivalent to the provided expected
     * message list.
     */
    public CssPropertyListAssertion isEquivalentTo(List<CssProperty> properties)
    {
        assertThat(actual).hasSize(properties.size());
        for (int i = 0; i < actual.size(); i++)
        {
            org.carrot2.labs.test.Assertions.assertThat(actual.get(i)).as(
                "property[" + i + "]").isEquivalentTo(properties.get(i));
        }
        return this;
    }

    /**
     * Asserts that the current message list is equivalent to the provided expected
     * message list.
     */
    public CssPropertyListAssertion isEquivalentTo(CssProperty... properties)
    {
        return isEquivalentTo(Lists.newArrayList(properties));
    }

    public CssPropertyListAssertion isEmpty()
    {
        assertTrue(actual.isEmpty());
        return this;
    }

}
