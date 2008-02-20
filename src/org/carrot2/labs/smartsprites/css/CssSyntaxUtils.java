package org.carrot2.labs.smartsprites.css;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.carrot2.labs.smartsprites.message.Message;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message.MessageType;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A few utility methods for processing CSS syntax.
 */
public class CssSyntaxUtils
{
    private static final Pattern URL_PATTERN = Pattern
        .compile("[uU][rR][lL]\\((['\"]?)([^'\"]*)\\1\\)");

    /**
     * Extracts CSS properties from the provided {@link String}.
     */
    public static Collection<CssProperty> extractProperties(String text)
    {
        return extractRules(text, null);
    }

    /**
     * Extracts CSS properties from the provided {@link String} and logs warnings to the
     * provided {@link MessageLog}.
     */
    public static Collection<CssProperty> extractRules(String text, MessageLog messageLog)
    {
        final Collection<CssProperty> rules = Lists.newArrayList();

        final String [] chunks = text.split(";");
        for (final String chunk : chunks)
        {
            final String [] parts = chunk.split(":");

            if (parts.length == 2)
            {
                rules
                    .add(new CssProperty(parts[0].trim().toLowerCase(), parts[1].trim()));
            }
            else
            {
                if (messageLog != null)
                {
                    messageLog.logWarning(Message.MessageType.MALFORMED_CSS_RULE, chunk
                        .trim());
                }
            }
        }

        return rules;
    }

    /**
     * Converts the provided collection of CSS properties to a {@link Map} with keys being
     * property names and values being {@link CssProperty} objects.
     */
    public static Map<String, CssProperty> propertiesAsMap(Collection<CssProperty> rules)
    {
        final Map<String, CssProperty> result = Maps.newHashMap();
        for (final CssProperty cssProperty : rules)
        {
            result.put(cssProperty.rule, cssProperty);
        }
        return result;
    }

    /**
     * Returns <code>true</code> if the the provided map contains a property with the
     * specified name that has a non-blank value.
     */
    public static boolean hasNonBlankValue(Map<String, CssProperty> properties,
        String propertyName)
    {
        return properties.containsKey(propertyName)
            && !StringUtils.isBlank(properties.get(propertyName).value);
    }

    /**
     * Extracts the actual url from the CSS url expression like
     * <code>url('actua_url')</code>.
     */
    public static String unpackUrl(String urlValue)
    {
        return unpackUrl(urlValue, null);
    }

    /**
     * Extracts the actual url from the CSS url expression like
     * <code>url('actua_url')</code> and logs warnings to the provided
     * {@link MessageLog}.
     */
    public static String unpackUrl(String urlValue, MessageLog messageLog)
    {
        final Matcher matcher = URL_PATTERN.matcher(urlValue);
        if (!matcher.matches())
        {
            if (messageLog != null)
            {
                messageLog.logWarning(MessageType.MALFORMED_URL, urlValue);
            }
            return null;
        }
        return matcher.group(2);
    }
}
