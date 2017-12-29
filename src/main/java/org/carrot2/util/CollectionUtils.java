package org.carrot2.util;

import java.util.Collection;

/**
 * Various utility methods for working with {@link Collection}s.
 */
public class CollectionUtils
{

    private CollectionUtils()
    {
        // Prevent Instantiation
    }

    /**
     * Converts a {@link Collection} to a {@link String} separating entries by
     * <code>, </code>.
     */
    public static String toString(Collection<?> collection)
    {
        final StringBuilder string = new StringBuilder();
        for (final Object object : collection)
        {
            string.append(object.toString());
            string.append(", ");
        }

        string.delete(string.length() - 2, string.length());

        return string.toString();
    }
}
