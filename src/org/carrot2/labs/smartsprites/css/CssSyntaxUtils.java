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
 * @author Stanislaw Osinski
 */
public class CssSyntaxUtils
{
    private static final Pattern URL_PATTERN = Pattern
        .compile("[uU][rR][lL]\\((['\"]?)([^'\"]*)\\1\\)");

    public static Collection<CssRule> extractRules(String text)
    {
        return extractRules(text, null);
    }

    public static Collection<CssRule> extractRules(String text,
        MessageLog messageCollector)
    {
        final Collection<CssRule> rules = Lists.newArrayList();

        final String [] chunks = text.split(";");
        for (final String chunk : chunks)
        {
            final String [] parts = chunk.split(":");

            if (parts.length == 2)
            {
                rules.add(new CssRule(parts[0].trim().toLowerCase(), parts[1].trim()));
            }
            else
            {
                if (messageCollector != null)
                {
                    messageCollector.logWarning(Message.MessageType.MALFORMED_CSS_RULE,
                        chunk.trim());
                }
            }
        }

        return rules;
    }

    public static Map<String, CssRule> rulesAsMap(Collection<CssRule> rules)
    {
        final Map<String, CssRule> result = Maps.newHashMap();
        for (final CssRule cssRule : rules)
        {
            result.put(cssRule.rule, cssRule);
        }
        return result;
    }

    public static boolean hasNonBlankValue(Map<String, CssRule> rules, String rule)
    {
        return rules.containsKey(rule) && !StringUtils.isBlank(rules.get(rule).value);
    }

    public static String unpackUrl(String urlValue)
    {
        return unpackUrl(urlValue, null);
    }

    public static String unpackUrl(String urlValue, MessageLog messageCollector)
    {
        final Matcher matcher = URL_PATTERN.matcher(urlValue);
        if (!matcher.matches())
        {
            if (messageCollector != null)
            {
                messageCollector.logWarning(MessageType.MALFORMED_URL, urlValue);
            }
            return null;
        }
        return matcher.group(2);
    }
}
