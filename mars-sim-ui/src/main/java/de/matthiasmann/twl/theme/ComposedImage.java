/*
 * Copyright (c) 2008, Matthias Mann
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

/**
 * A image composed by layering several images. Layer 0 is the base and drawn first.
 *
 * @author Matthias Mann
 */
class ComposedImage implements Image, HasBorder {

    private final Image[] layers;
    private final Border border;

    public ComposedImage(Image[] layers, Border border) {
        super();
        this.layers = layers;
        this.border = border;
    }

    public void draw(AnimationState as, int x, int y) {
        draw(as, x, y, getWidth(), getHeight());
    }

    public void draw(AnimationState as, int x, int y, int width, int height) {
        for(Image layer : layers) {
            layer.draw(as, x, y, width, height);
        }
    }

    public int getHeight() {
        return layers[0].getHeight();
    }

    public int getWidth() {
        return layers[0].getWidth();
    }

    public Border getBorder() {
        return border;
    }

    public Image createTintedVersion(Color color) {
        Image[] newLayers = new Image[layers.length];
        for(int i=0 ; i<newLayers.length ; i++) {
            newLayers[i] = layers[i].createTintedVersion(color);
        }
        return new ComposedImage(newLayers, border);
    }

}
