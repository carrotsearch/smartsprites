package org.carrot2.labs.smartsprites.layout;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.ArithmeticUtils;
import org.carrot2.labs.smartsprites.SpriteImage;
import org.carrot2.labs.smartsprites.SpriteImageOccurrence;
import org.carrot2.labs.smartsprites.SpriteLayoutProperties.SpriteAlignment;
import org.carrot2.labs.smartsprites.SpriteReferenceDirective;
import org.carrot2.labs.smartsprites.SpriteReferenceOccurrence;
import org.carrot2.labs.smartsprites.SpriteReferenceReplacement;
import org.carrot2.labs.smartsprites.message.MessageLog;

public abstract class AbstractLayout implements SpriteImageLayout {

    /**
     * A wrapper that implements content-aware {@link Object#equals(Object)} and
     * {@link Object#hashCode()} on {@link BufferedImage}s.
     */
    static final class BufferedImageEqualsWrapper
    {
        BufferedImage image;
    
        BufferedImageEqualsWrapper(BufferedImage image)
        {
            this.image = image;
        }
    
        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof BufferedImageEqualsWrapper))
            {
                return false;
            }
    
            if (obj == this)
            {
                return true;
            }
    
            final BufferedImage other = ((BufferedImageEqualsWrapper) obj).image;
    
            boolean equal = other.getWidth() == image.getWidth()
                && other.getHeight() == other.getHeight()
                && other.getType() == image.getType();
    
            if (equal)
            {
                for (int y = 0; y < image.getHeight(); y++)
                {
                    for (int x = 0; x < image.getWidth(); x++)
                    {
                        if (ignoreFullTransparency(image.getRGB(x, y)) != ignoreFullTransparency(other
                            .getRGB(x, y)))
                        {
                            return false;
                        }
                    }
                }
            }
    
            return equal;
        }
    
        @Override
        public int hashCode()
        {
            if (image == null)
            {
                return 0;
            }
    
            int hash = image.getWidth() ^ (image.getHeight() << 16);
    
            // Computes the hashCode based on an 4 x 4 to 7 x 7 grid of image's pixels
            final int xIncrement = image.getWidth() > 7 ? image.getWidth() >> 2 : 1;
            final int yIncrement = image.getHeight() > 7 ? image.getHeight() >> 2 : 1;
    
            for (int y = 0; y < image.getHeight(); y += yIncrement)
            {
                for (int x = 0; x < image.getWidth(); x += xIncrement)
                {
                    hash ^= ignoreFullTransparency(image.getRGB(x, y));
                }
            }
    
            return hash;
        }
    
        /**
         * If the pixel is fully transparent, returns 0. Otherwise, returns the pixel.
         * This is useful in {@link #equals(Object)} and {@link #hashCode()} to ignore
         * pixels that have different colors but are invisible anyway because of full
         * transparency.
         */
        private static int ignoreFullTransparency(int pixel)
        {
            if ((pixel & 0xff000000) == 0x00000000)
            {
                return 0;
            }
            else
            {
                return pixel;
            }
        }
    }

    public abstract SpriteAlignment correctAlignment(SpriteAlignment alignment, MessageLog messageCollector);
    public abstract SpriteAlignment getDefaultAlignment();
    public abstract SpriteReferenceReplacement buildReplacement(SpriteReferenceOccurrence spriteReferenceOccurrence, SpriteReferenceDirective spriteReferenceDirective, int offset);
    public abstract BufferedImage render(BufferedImage image, SpriteReferenceOccurrence spriteReferenceOccurrence, SpriteReferenceDirective spriteReferenceDirective, int dimension);
    public abstract int getRequiredHeight(BufferedImage image, SpriteReferenceDirective spriteReferenceDirective); 
    public abstract int getRequiredWidth(BufferedImage image, SpriteReferenceDirective spriteReferenceDirective);
    public abstract SpriteImage buildSpriteImage(SpriteImageOccurrence spriteImageOccurrence, Map<SpriteReferenceOccurrence, BufferedImage> images, MessageLog messageLog);

    protected abstract List<Integer> getDimensions(Map<SpriteReferenceOccurrence, BufferedImage> images);

    public int calculateRepeatAlignmentDimension(Map<SpriteReferenceOccurrence, BufferedImage> images) {
        List<Integer> multiples = this.getDimensions(images);

        int leastCommonMultiple = 1;
        for (Integer multiple : multiples) {
            if (null == multiple) {
                continue;
            }
            leastCommonMultiple = ArithmeticUtils.lcm(leastCommonMultiple, multiple);
        }
        return leastCommonMultiple;
    }
}
