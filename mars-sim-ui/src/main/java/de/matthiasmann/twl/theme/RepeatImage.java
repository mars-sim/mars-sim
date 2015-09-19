/*
 * Copyright (c) 2008-2009, Matthias Mann
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
import de.matthiasmann.twl.renderer.SupportsDrawRepeat;

/**
 *
 * @author Matthias Mann
 */
class RepeatImage implements Image, HasBorder, SupportsDrawRepeat {

    private final Image base;
    private final Border border;
    private final boolean repeatX;
    private final boolean repeatY;
    private final SupportsDrawRepeat sdr;

    RepeatImage(Image base, Border border, boolean repeatX, boolean repeatY) {
        assert repeatX || repeatY;
        this.base = base;
        this.border = border;
        this.repeatX = repeatX;
        this.repeatY = repeatY;

        if(base instanceof SupportsDrawRepeat) {
            sdr = (SupportsDrawRepeat)base;
        } else {
            sdr = this;
        }
    }

    public int getWidth() {
        return base.getWidth();
    }

    public int getHeight() {
        return base.getHeight();
    }

    public void draw(AnimationState as, int x, int y) {
        base.draw(as, x, y);
    }

    public void draw(AnimationState as, int x, int y, int width, int height) {
        int countX = repeatX ? Math.max(1, width / base.getWidth()) : 1;
        int countY = repeatY ? Math.max(1, height / base.getHeight()) : 1;
        sdr.draw(as, x, y, width, height, countX, countY);
    }

    public void draw(AnimationState as, int x, int y, int width, int height, int repeatCountX, int repeatCountY) {
        while(repeatCountY > 0) {
            int rowHeight = height / repeatCountY;

            int cx = 0;
            for(int xi=0 ; xi<repeatCountX ;) {
                int nx = ++xi * width / repeatCountX;
                base.draw(as, x+cx, y, nx-cx, rowHeight);
                cx = nx;
            }

            y += rowHeight;
            height -= rowHeight;
            repeatCountY--;
        }
    }


    public Border getBorder() {
        return border;
    }

    public Image createTintedVersion(Color color) {
        return new RepeatImage(base.createTintedVersion(color), border, repeatX, repeatY);
    }

}
