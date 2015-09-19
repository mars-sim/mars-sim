/*
 * Copyright (c) 2008-2012, Matthias Mann
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Matthias Mann nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.matthiasmann.twl.theme;

import de.matthiasmann.twl.Border;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.utils.StateExpression;

/**
 *
 * @author Matthias Mann
 */
class ImageAdjustments implements Image, HasBorder {

    final Image image;
    final Border border;
    final Border inset;
    final int sizeOverwriteH;
    final int sizeOverwriteV;
    final boolean center;
    final StateExpression condition;

    ImageAdjustments(Image image, Border border, Border inset,
            int sizeOverwriteH, int sizeOverwriteV,
            boolean center, StateExpression condition) {
        this.image = image;
        this.border = border;
        this.inset = inset;
        this.sizeOverwriteH = sizeOverwriteH;
        this.sizeOverwriteV = sizeOverwriteV;
        this.center = center;
        this.condition = condition;
    }

    public int getWidth() {
        if(sizeOverwriteH >= 0) {
            return sizeOverwriteH;
        } else if(inset != null) {
            return image.getWidth() + inset.getBorderLeft() + inset.getBorderRight();
        } else {
            return image.getWidth();
        }
    }

    public int getHeight() {
        if(sizeOverwriteV >= 0) {
            return sizeOverwriteV;
        } else if(inset != null) {
            return image.getHeight() + inset.getBorderTop() + inset.getBorderBottom();
        } else {
            return image.getHeight();
        }
    }

    public void draw(AnimationState as, int x, int y, int width, int height) {
        if(condition == null || condition.evaluate(as)) {
            if(inset != null) {
                x += inset.getBorderLeft();
                y += inset.getBorderTop();
                width = Math.max(0, width - inset.getBorderLeft() - inset.getBorderRight());
                height = Math.max(0, height - inset.getBorderTop() - inset.getBorderBottom());
            }
            if(center) {
                final int w = Math.min(width, image.getWidth());
                final int h = Math.min(height, image.getHeight());
                x += (width - w) / 2;
                y += (height - h) / 2;
                width = w;
                height = h;
            }
            image.draw(as, x, y, width, height);
        }
    }

    public void draw(AnimationState as, int x, int y) {
        draw(as, x, y, image.getWidth(), image.getHeight());
    }

    public Border getBorder() {
        return border;
    }

    public Image createTintedVersion(Color color) {
        return new ImageAdjustments(image.createTintedVersion(color), border,
                inset, sizeOverwriteH, sizeOverwriteV, center, condition);
    }
    
    boolean isSimple() {
        // used for ImageManager.parseStateSelect
        // only check parameters affecting rendering (except condition)
        return !center && inset == null && sizeOverwriteH < 0 && sizeOverwriteV < 0;
    }
}
