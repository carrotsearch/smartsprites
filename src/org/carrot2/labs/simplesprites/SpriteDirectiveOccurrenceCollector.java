package org.carrot2.labs.simplesprites;

import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.carrot2.labs.simplesprites.css.CssRule;
import org.carrot2.labs.simplesprites.css.CssSyntaxUtils;
import org.carrot2.labs.simplesprites.message.MessageLog;
import org.carrot2.labs.simplesprites.message.Message.MessageType;
import org.carrot2.util.CloseableUtils;

import com.google.common.collect.Lists;

/**
 * @author Stanislaw Osinski
 */
public class SpriteDirectiveOccurrenceCollector
{
    private final static Pattern SPRITE_IMAGE_DIRECTIVE = Pattern
        .compile("/\\*+\\s+(sprite:[^*]*)\\*+/");

    private final static Pattern SPRITE_REFERENCE_DIRECTIVE = Pattern
        .compile("/\\*+\\s+(sprite-ref:[^*]*)\\*+/");

    public static Collection<SpriteImageOccurrence> collectSpriteImageOccurrences(
        File cssFile, MessageLog messageLog) throws FileNotFoundException, IOException
    {
        final Collection<SpriteImageOccurrence> occurrences = Lists.newArrayList();
        final BufferedReader reader = new BufferedReader(new FileReader(cssFile));

        int lineNumber = -1;
        String line;

        try
        {
            while ((line = reader.readLine()) != null)
            {
                messageLog.setLine(++lineNumber);

                final String spriteImageDirectiveString = extractSpriteImageDirectiveString(line);
                if (spriteImageDirectiveString == null)
                {
                    continue;
                }

                final SpriteImageDirective directive = SpriteImageDirective.parse(
                    spriteImageDirectiveString, messageLog);
                if (directive == null)
                {
                    continue;
                }

                occurrences
                    .add(new SpriteImageOccurrence(directive, cssFile, lineNumber));
            }
        }
        finally
        {
            CloseableUtils.closeIgnoringException(reader);
        }

        return occurrences;
    }

    public static Collection<SpriteReferenceOccurrence> collectSpriteReferenceOccurrences(
        File cssFile, Map<String, SpriteImageDirective> spriteImageDirectives,
        MessageLog messageLog) throws FileNotFoundException, IOException
    {
        final Collection<SpriteReferenceOccurrence> directives = Lists.newArrayList();

        final BufferedReader reader = new BufferedReader(new FileReader(cssFile));
        int lineNumber = -1;
        String line;

        try
        {
            while ((line = reader.readLine()) != null)
            {
                messageLog.setLine(++lineNumber);
                
                final String directiveString = extractSpriteReferenceDirectiveString(line);
                if (directiveString == null)
                {
                    continue;
                }

                final String imageUrl = extractSpriteReferenceImageUrl(line, messageLog);
                if (imageUrl == null)
                {
                    continue;
                }

                final SpriteReferenceDirective directive = SpriteReferenceDirective
                    .parse(directiveString, spriteImageDirectives, messageLog);
                if (directive == null)
                {
                    continue;
                }

                directives.add(new SpriteReferenceOccurrence(directive, imageUrl,
                    cssFile, lineNumber));
            }
        }
        finally
        {
            CloseableUtils.closeIgnoringException(reader);
        }

        return directives;
    }

    static String extractSpriteImageDirectiveString(String cssLine)
    {
        final Matcher matcher = SPRITE_IMAGE_DIRECTIVE.matcher(cssLine);

        if (matcher.find())
        {
            return matcher.group(1).trim();
        }
        else
        {
            return null;
        }
    }

    static String extractSpriteReferenceDirectiveString(String css)
    {
        final Matcher matcher = SPRITE_REFERENCE_DIRECTIVE.matcher(css);

        if (matcher.find())
        {
            return matcher.group(1).trim();
        }
        else
        {
            return null;
        }
    }

    static String extractSpriteReferenceImageUrl(String css, MessageLog messageLog)
    {
        final Matcher matcher = SPRITE_REFERENCE_DIRECTIVE.matcher(css);

        // Remove the directive
        final String noDirective = matcher.replaceAll("").trim();

        final Collection<CssRule> rules = CssSyntaxUtils.extractRules(noDirective);
        if (rules.size() == 0)
        {
            messageLog.logWarning(
                MessageType.NO_BACKGROUND_IMAGE_RULE_NEXT_TO_SPRITE_REFERENCE_DIRECTIVE,
                css);
            return null;
        }

        if (rules.size() > 1)
        {
            messageLog.logWarning(
                MessageType.MORE_THAN_ONE_RULE_NEXT_TO_SPRITE_REFERENCE_DIRECTIVE, css);
            return null;
        }

        final CssRule backgroundImageRule = rules.iterator().next();
        if (!backgroundImageRule.rule.equals("background-image"))
        {
            messageLog.logWarning(
                MessageType.NO_BACKGROUND_IMAGE_RULE_NEXT_TO_SPRITE_REFERENCE_DIRECTIVE,
                css);
            return null;
        }

        return CssSyntaxUtils.unpackUrl(backgroundImageRule.value, messageLog);
    }
}
