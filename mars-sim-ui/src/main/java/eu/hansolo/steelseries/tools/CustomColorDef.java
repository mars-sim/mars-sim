/*
 * Copyright (c) 2012, Gerrit Grunwald
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * The names of its contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package eu.hansolo.steelseries.tools;

import java.awt.Color;


/**
 *
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public class CustomColorDef {

    public final Color COLOR;
    public final Color VERY_DARK;
    public final Color DARK;
    public final Color MEDIUM;
    public final Color NORMAL;
    public final Color LIGHT;
    public final Color LIGHTER;
    public final Color VERY_LIGHT;

    public CustomColorDef(final Color COLOR) {
        this.COLOR = COLOR;
        final float HUE = Color.RGBtoHSB(COLOR.getRed(), COLOR.getGreen(), COLOR.getBlue(), null)[0];
        if (COLOR.getRed() == COLOR.getGreen() && COLOR.getRed() == COLOR.getBlue()) {
            VERY_DARK = Color.getHSBColor(HUE, 0.0f, 0.32f);
            DARK = Color.getHSBColor(HUE, 0.0f, 0.62f);
            MEDIUM = Color.getHSBColor(HUE, 0.0f, 0.74f);
            NORMAL = COLOR;
            LIGHT = Color.getHSBColor(HUE, 0.0f, 0.84f);
            LIGHTER = Color.getHSBColor(HUE, 0.0f, 0.94f);
            VERY_LIGHT = Color.getHSBColor(HUE, 0.0f, 1.0f);
        } else {
            VERY_DARK = Color.getHSBColor(HUE, 1.0f, 0.32f);
            DARK = Color.getHSBColor(HUE, 1.0f, 0.62f);
            MEDIUM = Color.getHSBColor(HUE, 1.0f, 0.74f);
            NORMAL = COLOR;
            LIGHT = Color.getHSBColor(HUE, 0.65f, 0.84f);
            LIGHTER = Color.getHSBColor(HUE, 0.33f, 0.94f);
            VERY_LIGHT = Color.getHSBColor(HUE, 0.15f, 1.0f);
        }
    }
}
