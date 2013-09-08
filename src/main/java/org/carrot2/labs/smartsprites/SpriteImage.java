package org.carrot2.labs.smartsprites;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.carrot2.labs.smartsprites.SpriteImageDirective.SpriteUidType;

import com.google.common.io.Closeables;

/**
 * A merged sprite image consisting of a number of individual images.
 */
public class SpriteImage
{
    /** The rendered sprite image bitmap */
    public final BufferedImage sprite;

    /**
     * All {@link SpriteReferenceReplacement}s corresponding to the individual images this
     * sprite image consists of.
     */
    public final Map<SpriteReferenceOccurrence, SpriteReferenceReplacement> spriteReferenceReplacements;

    /**
     * {@link SpriteImageOccurrence} for which this {@link SpriteImage} has been built.
     */
    public final SpriteImageOccurrence spriteImageOccurrence;

    /**
     * Indicates whether this sprite has been also generated in an alpha/color degraded
     * version for IE6;
     */
    public boolean hasReducedForIe6 = false;

    /**
     * The {@link SpriteImageDirective#imagePath} with variables resolved.
     */
    public String resolvedPath;

    /**
     * The {@link SpriteImageDirective#imagePath} with variables resolved and the
     * IE6-specific suffix, <code>null</code> if {@link #hasReducedForIe6} is
     * <code>false</code>.
     */
    public String resolvedPathIe6;

    /**
     * The width of the final sprite.
     */
    public int spriteWidth;

    /**
     * The height of the final sprite.
     */
    public int spriteHeight;

    /**
     * The scale to apply to the final sprite's background-size.
     */
    public float scaleRatio;

    private static final Pattern SPRITE_VARIABLE = Pattern.compile("${sprite}",
        Pattern.LITERAL);

    public SpriteImage(BufferedImage sprite, SpriteImageOccurrence spriteImageOccurrence,
        Map<SpriteReferenceOccurrence, SpriteReferenceReplacement> spriteReplacements,
        int width, int height, float scale)
    {
        this.sprite = sprite;
        this.spriteReferenceReplacements = spriteReplacements;
        this.spriteImageOccurrence = spriteImageOccurrence;
        this.spriteWidth = width;
        this.spriteHeight = height;
        this.scaleRatio = scale;

        for (SpriteReferenceReplacement replacement : spriteReplacements.values())
        {
            replacement.spriteImage = this;
        }
    }

    String resolveImagePath(byte [] image, String timestamp, boolean reducedForIe6)
    {
        String imagePath = spriteImageOccurrence.spriteImageDirective.imagePath;

        // Backwards compatibility: if there are no place holders in the path
        // and the UID type is defined, append the UID as a query string just like
        // the previous versions did. To be removed in 0.4.0.
        if (spriteImageOccurrence.spriteImageDirective.uidType != SpriteUidType.NONE
            && !SpriteUidType.MD5.pattern.matcher(imagePath).find()
            && !SpriteUidType.DATE.pattern.matcher(imagePath).find())
        {
            imagePath += "?${"
                + spriteImageOccurrence.spriteImageDirective.uidType.toString() + "}";
        }

        // Resolve MD5 hash
        Matcher md5Matcher = SpriteUidType.MD5.pattern.matcher(imagePath);
        if (md5Matcher.find())
        {
            // Compute MD5 only when necessary
            imagePath = md5Matcher.replaceAll(computeMd5(image));
        }

        // Resolve timestamp
        imagePath = SpriteUidType.DATE.pattern.matcher(imagePath).replaceAll(timestamp);

        // Resolve sprite name
        imagePath = SPRITE_VARIABLE.matcher(imagePath).replaceAll(
            spriteImageOccurrence.spriteImageDirective.spriteId);

        if (reducedForIe6)
        {
            this.resolvedPathIe6 = addIe6Suffix(imagePath, reducedForIe6);
            return this.resolvedPathIe6;
        }
        else
        {
            this.resolvedPath = addIe6Suffix(imagePath, reducedForIe6);
            return this.resolvedPath;
        }
    }

    /**
     * Adds IE6 suffix to the sprite image path for IE6 reduced images. We make sure we
     * don't add the suffix to the directory names or after the '?' character.
     */
    static String addIe6Suffix(String spritePath, boolean ie6Reduced)
    {
        if (ie6Reduced)
        {
            final StringBuilder ie6Path = new StringBuilder();

            int lastFoundIndex = 0;

            final int lastSlashIndex = spritePath.lastIndexOf("/");
            if (lastSlashIndex >= 0)
            {
                ie6Path.append(spritePath, lastFoundIndex, lastSlashIndex + 1);
                lastFoundIndex = lastSlashIndex + 1;
            }

            int lastDotIndex = spritePath.lastIndexOf(".");
            if (lastDotIndex < lastFoundIndex)
            {
                lastDotIndex = -1;
            }
            final int firstQuestionMarkIndex = spritePath.indexOf("?", lastFoundIndex);

            if (lastDotIndex >= 0
                && (lastDotIndex < firstQuestionMarkIndex || firstQuestionMarkIndex < 0))
            {
                ie6Path.append(spritePath, lastFoundIndex, lastDotIndex);
                ie6Path.append("-ie6");
                ie6Path.append(spritePath, lastDotIndex, spritePath.length());
            }
            else if (firstQuestionMarkIndex >= 0)
            {
                ie6Path.append(spritePath, lastFoundIndex, firstQuestionMarkIndex);
                ie6Path.append("-ie6");
                ie6Path.append(spritePath, firstQuestionMarkIndex, spritePath.length());
            }
            else
            {
                ie6Path.append(spritePath, lastFoundIndex, spritePath.length());
                ie6Path.append("-ie6");
            }

            return ie6Path.toString();
        }
        return spritePath;
    }

    /**
     * Computes
     */
    private static String computeMd5(byte [] image)
    {
        try
        {
            final byte [] buffer = new byte [4069];
            final MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            InputStream is = null, digestInputStream = null;
            try
            {
                is = new ByteArrayInputStream(image);
                digestInputStream = new DigestInputStream(is, digest);
                while (digestInputStream.read(buffer) >= 0)
                {
                }

                return new BigInteger(1, digest.digest()).toString(16);
            }
            catch (IOException e)
            {
                // Should not happen because we're reading from memory
                throw new RuntimeException(e);
            }
            finally
            {
                Closeables.closeQuietly(is);
                Closeables.closeQuietly(digestInputStream);
                digest.reset();
            }
        }
        catch (NoSuchAlgorithmException nsaex)
        {
            throw new RuntimeException(nsaex);
        }
    }
}
