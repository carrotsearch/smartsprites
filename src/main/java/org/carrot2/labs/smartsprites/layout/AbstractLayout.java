package org.carrot2.labs.smartsprites.layout;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.ArithmeticUtils;
import org.carrot2.labs.smartsprites.SpriteLayoutProperties.SpriteAlignment;
import org.carrot2.labs.smartsprites.SpriteReferenceDirective;
import org.carrot2.labs.smartsprites.SpriteReferenceOccurrence;
import org.carrot2.labs.smartsprites.SpriteReferenceReplacement;
import org.carrot2.labs.smartsprites.message.MessageLog;

public abstract class AbstractLayout implements SpriteImageLayout {

    public abstract SpriteAlignment correctAlignment(SpriteAlignment alignment, MessageLog messageCollector);
    public abstract SpriteAlignment getDefaultAlignment();
    public abstract SpriteReferenceReplacement buildReplacement(SpriteReferenceOccurrence spriteReferenceOccurrence, SpriteReferenceDirective spriteReferenceDirective, int offset);
    public abstract BufferedImage render(BufferedImage image, SpriteReferenceOccurrence spriteReferenceOccurrence, SpriteReferenceDirective spriteReferenceDirective, int dimension);
    public abstract int getRequiredHeight(BufferedImage image, SpriteReferenceDirective spriteReferenceDirective); 
    public abstract int getRequiredWidth(BufferedImage image, SpriteReferenceDirective spriteReferenceDirective);

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
