package org.carrot2.labs.smartsprites.layout;

import java.awt.image.BufferedImage;
import java.util.Map;

import org.carrot2.labs.smartsprites.SpriteLayoutProperties.SpriteAlignment;
import org.carrot2.labs.smartsprites.SpriteReferenceDirective;
import org.carrot2.labs.smartsprites.SpriteReferenceOccurrence;
import org.carrot2.labs.smartsprites.SpriteReferenceReplacement;
import org.carrot2.labs.smartsprites.message.MessageLog;

public interface SpriteImageLayout {
    public SpriteAlignment correctAlignment(SpriteAlignment alignment, MessageLog messageCollector);
    public SpriteAlignment getDefaultAlignment();
    public SpriteReferenceReplacement buildReplacement(SpriteReferenceOccurrence spriteReferenceOccurrence, SpriteReferenceDirective spriteReferenceDirective, int offset);
    public BufferedImage render(BufferedImage image, SpriteReferenceOccurrence spriteReferenceOccurrence, SpriteReferenceDirective spriteReferenceDirective, int dimension);
    public int getRequiredHeight(BufferedImage image, SpriteReferenceDirective spriteReferenceDirective);
    public int getRequiredWidth(BufferedImage image, SpriteReferenceDirective spriteReferenceDirective);
    public int calculateRepeatAlignmentDimension(Map<SpriteReferenceOccurrence, BufferedImage> images);
}