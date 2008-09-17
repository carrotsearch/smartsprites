package org.carrot2.labs.test;

import java.util.*;

import org.carrot2.labs.smartsprites.message.Message;
import org.carrot2.labs.smartsprites.message.Message.MessageLevel;
import org.fest.assertions.Assertions;
import org.fest.assertions.Fail;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Assertions on lists of {@link Message}s.
 */
public class MessageListAssertion
{
    /** The actual message list */
    private List<Message> actual;

    /**
     * Creates a {@link Message} list assertion object.
     */
    public MessageListAssertion(List<Message> actual)
    {
        this.actual = actual;
    }

    /**
     * Asserts that the current message list contains (at least) the specified messages.
     */
    public MessageListAssertion contains(Message... messages)
    {
        final Set<Message> toCheck = Sets.newHashSet(messages);
        for (int i = 0; i < actual.size(); i++)
        {
            for (Iterator<Message> it = toCheck.iterator(); it.hasNext();)
            {
                final Message message = it.next();
                try
                {
                    org.carrot2.labs.test.Assertions.assertThat(actual.get(i)).as(
                        "message[" + i + "]").isEquivalentTo(message);
                    it.remove();

                }
                catch (AssertionError e)
                {
                    // This means the message wan't equivalen, ignore
                }
            }
        }

        if (!toCheck.isEmpty())
        {
            Fail.fail("Message list did not contain " + toCheck.size()
                + " of the required messages");
        }

        return this;
    }

    /**
     * Asserts that the current message list is equivalent to the provided expected
     * message list.
     */
    public MessageListAssertion isEquivalentTo(List<Message> expected)
    {
        Assertions.assertThat(actual).hasSize(expected.size());
        for (int i = 0; i < actual.size(); i++)
        {
            org.carrot2.labs.test.Assertions.assertThat(actual.get(i)).as(
                "message[" + i + "]").isEquivalentTo(expected.get(i));
        }
        return this;
    }

    /**
     * Asserts that the current message list is equivalent to the provided expected
     * message list.
     */
    public MessageListAssertion isEquivalentTo(MessageLevel onlyLevel,
        List<Message> expected)
    {
        final List<Message> filtered = Lists.newArrayList();
        for (Message message : actual)
        {
            if (message.level == onlyLevel)
            {
                filtered.add(message);
            }
        }

        final List<Message> actualBackup = actual;
        actual = filtered;
        isEquivalentTo(expected);
        actual = actualBackup;

        return this;
    }

    /**
     * Asserts that the current message list is equivalent to the provided expected
     * message list.
     */
    public MessageListAssertion isEquivalentTo(Message... messages)
    {
        return isEquivalentTo(Lists.newArrayList(messages));
    }

    /**
     * Asserts that the current message list is equivalent to the provided expected
     * message list.
     */
    public MessageListAssertion isEquivalentTo(MessageLevel onlyLevel,
        Message... messages)
    {
        return isEquivalentTo(onlyLevel, Lists.newArrayList(messages));
    }

    public MessageListAssertion doesNotHaveMessagesOfLevel(MessageLevel level)
    {
        int levelCount = 0;
        final StringBuilder messages = new StringBuilder();
        for (Message message : actual)
        {
            if (message.level == level)
            {
                levelCount++;
                messages.append(message.toString());
                messages.append(", ");
            }
        }

        if (levelCount > 0)
        {
            Fail.fail("Found " + levelCount + " " + level.name() + " messages: "
                + messages.substring(0, messages.length() - 2));
        }

        return this;
    }

    public MessageListAssertion isEmpty()
    {
        Assertions.assertThat(actual).isEmpty();
        return this;
    }

}
