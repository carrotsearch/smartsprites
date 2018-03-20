package org.carrot2.util;

/**
 * Various utility methods for working with Java 5 enum types.
 */
public class EnumUtils
{

    private EnumUtils()
    {
        // Prevent Instantiation
    }

    /**
     * Returns the enum instance corresponding to the provided <code>name</code> or
     * <code>defaultValue</code> if no enum value corresponds to <code>name</code>.
     */
    public static <T extends Enum<T>> T valueOf(String name, Class<T> enumClass,
        T defaultValue)
    {
        if (StringUtils.isNotBlank(name))
        {
            try
            {
                return Enum.valueOf(enumClass, name);
            }
            catch (IllegalArgumentException e)
            {
                return defaultValue;
            }
        }
        else
        {
            return defaultValue;
        }
    }

}
