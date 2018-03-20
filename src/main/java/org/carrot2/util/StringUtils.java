package org.carrot2.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

public class StringUtils {

    private StringUtils()
    {
        // Prevent Instantiation
    }

    public static boolean isBlank(final String string)
    {
        return Strings.isNullOrEmpty(string) ? true : CharMatcher.whitespace().matchesAllOf(string);
    }

    public static boolean isNotBlank(final String string)
    {
        return !StringUtils.isBlank(string);
    }

}
