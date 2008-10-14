package org.carrot2.labs.smartsprites.css;

import java.awt.Color;
import java.util.*;
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

    private static final Pattern COLOR_PATTERN = Pattern.compile("#([0-9a-f]{6})");

    private static final Pattern IMPORTANT_PATTERN = Pattern.compile("!\\s*important");

    /**
     * Extracts CSS properties from the provided {@link String}.
     */
    public static List<CssProperty> extractProperties(String text)
    {
        return extractRules(text, null);
    }

    /**
     * Extracts CSS properties from the provided {@link String} and logs warnings to the
     * provided {@link MessageLog}.
     */
    public static List<CssProperty> extractRules(String text, MessageLog messageLog)
    {
        final List<CssProperty> rules = Lists.newArrayList();

        final String [] chunks = text.split(";");
        for (final String chunk : chunks)
        {
            final String [] parts = chunk.split(":");

            if (parts.length == 2)
            {
                String value = parts[1].trim();
                final Matcher matcher = IMPORTANT_PATTERN.matcher(value);
                boolean important = false;
                if (matcher.find())
                {
                    important = true;
                    value = matcher.replaceAll("");
                }

                rules.add(new CssProperty(parts[0].trim().toLowerCase(), value.trim(),
                    important));
            }
            else
            {
                if (messageLog != null)
                {
                    messageLog.warning(Message.MessageType.MALFORMED_CSS_RULE, chunk
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
        final Map<String, CssProperty> result = Maps.newLinkedHashMap();
        for (final CssProperty cssProperty : rules)
        {
            result.put(cssProperty.rule, cssProperty);
        }
        return result;
    }

    /**
     * Returns the value of a CSS property if it exists, <code>null</code> otherwise.
     */
    public static String getValue(Map<String, CssProperty> rules, String property)
    {
        final CssProperty cssProperty = rules.get(property);
        if (cssProperty != null)
        {
            return cssProperty.value;
        }
        else
        {
            return null;
        }
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
     * Extracts the actual url from the CSS url expression like <code>url('actua_url')</code>.
     */
    public static String unpackUrl(String urlValue)
    {
        return unpackUrl(urlValue, null);
    }

    /**
     * Extracts the actual url from the CSS url expression like <code>url('actua_url')</code> and logs
     * warnings to the provided {@link MessageLog}.
     */
    public static String unpackUrl(String urlValue, MessageLog messageLog)
    {
        final Matcher matcher = URL_PATTERN.matcher(urlValue);
        if (!matcher.matches())
        {
            if (messageLog != null)
            {
                messageLog.warning(MessageType.MALFORMED_URL, urlValue);
            }
            return null;
        }
        return matcher.group(2);
    }

    /**
     * Parses a hexadecimal format (#fff or #ffffff) of CSS color into a {@link Color}
     * object. The RGB format (rgb(100, 0, 0)) is currently not supported. In case of
     * parse errors, the default is returned
     */
    public static Color parseColor(String colorValue, MessageLog messageLog,
        Color defaultColor)
    {
        final Matcher matcher = COLOR_PATTERN.matcher(colorValue);
        if (!matcher.matches())
        {
            if (messageLog != null)
            {
                messageLog.warning(MessageType.MALFORMED_COLOR, colorValue);
            }
            return defaultColor;
        }

        return new Color(Integer.parseInt(matcher.group(1), 16));
    }
}
