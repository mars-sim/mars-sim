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
import de.matthiasmann.twl.utils.StateSelect;

/**
 *
 * @author Matthias Mann
 */
public class StateSelectImage implements Image, HasBorder {

    final Image[] images;
    final StateSelect select;
    final Border border;

    public StateSelectImage(StateSelect select, Border border, Image ... images) {
        assert images.length >= select.getNumExpressions();
        assert images.length <= select.getNumExpressions() + 1;
        
        this.images = images;
        this.select = select;
        this.border = border;
    }

    public int getWidth() {
        return images[0].getWidth();
    }

    public int getHeight() {
        return images[0].getHeight();
    }

    public void draw(AnimationState as, int x, int y) {
        draw(as, x, y, getWidth(), getHeight());
    }

    public void draw(AnimationState as, int x, int y, int width, int height) {
        int idx = select.evaluate(as);
        if(idx < images.length) {
            images[idx].draw(as, x, y, width, height);
        }
    }

    public Border getBorder() {
        return border;
    }

    public Image createTintedVersion(Color color) {
        Image[] newImages = new Image[images.length];
        for(int i=0 ; i<newImages.length ; i++) {
            newImages[i] = images[i].createTintedVersion(color);
        }
        return new StateSelectImage(select, border, newImages);
    }

}
