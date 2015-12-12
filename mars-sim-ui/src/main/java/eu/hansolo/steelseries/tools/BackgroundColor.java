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
 * Definition of color combinations for gradients that will be used
 * as backgrounds for the components. So one could choose DARK_GRAY
 * in the ui editor and it will set the gradient with the colors
 * that are defined here for DARK_GRAY.
 * @author hansolo
 */
public enum BackgroundColor {

    DARK_GRAY(new Color(0, 0, 0, 255), new Color(51, 51, 51, 255), new Color(153, 153, 153, 255), Color.WHITE, new Color(180, 180, 180)),
    SATIN_GRAY(new Color(45, 57, 57, 255), new Color(45, 57, 57, 255), new Color(45, 57, 57, 255), new Color(167, 184, 180, 255), new Color(137, 154, 150)),
    LIGHT_GRAY(new Color(130, 130, 130, 255), new Color(181, 181, 181, 255), new Color(253, 253, 253, 255), Color.BLACK, new Color(80, 80, 80)),
    WHITE(Color.WHITE, Color.WHITE, Color.WHITE, Color.BLACK, new Color(80, 80, 80)),
    BLACK(Color.BLACK, Color.BLACK, Color.BLACK, Color.WHITE, new Color(180, 180, 180)),
    BEIGE(new Color(178, 172, 150, 255), new Color(204, 205, 184, 255), new Color(231, 231, 214, 255), Color.BLACK, new Color(80, 80, 80)),
    BROWN(new Color(245, 225, 193, 255), new Color(245, 225, 193, 255), new Color(255, 250, 240, 255), new Color(109, 73, 47, 255), new Color(89, 53, 27)),
    RED(new Color(198, 93, 95, 255), new Color(212, 132, 134, 255), new Color(242, 218, 218, 255), Color.BLACK, new Color(90, 0, 0)),
    GREEN(new Color(65, 120, 40, 255), new Color(129, 171, 95, 255), new Color(218, 237, 202, 255), Color.BLACK, new Color(0, 90, 0)),
    BLUE(new Color(45, 83, 122, 255), new Color(115, 144, 170, 255), new Color(227, 234, 238, 255), Color.BLACK, new Color(0, 0, 90)),
    ANTHRACITE(new Color(50, 50, 54, 255), new Color(47, 47, 51, 255), new Color(69, 69, 74, 255), new Color(250, 250, 250, 255), new Color(180, 180, 180)),
    MUD(new Color(80, 86, 82, 255), new Color(70, 76, 72, 255), new Color(57, 62, 58, 255), new Color(255, 255, 240, 255), new Color(225, 225, 210)),
    CARBON(new Color(50, 50, 54, 255), new Color(47, 47, 51, 255), new Color(69, 69, 74, 255), Color.WHITE, new Color(180, 180, 180)),
    STAINLESS(Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, new Color(80, 80, 80)),
    STAINLESS_GRINDED(new Color(50, 50, 54, 255), new Color(47, 47, 51, 255), new Color(69, 69, 74, 255), Color.BLACK, new Color(80, 80, 80)),
    BRUSHED_METAL(new Color(50, 50, 54, 255), new Color(47, 47, 51, 255), new Color(69, 69, 74, 255), Color.BLACK, new Color(80, 80, 80)),
    PUNCHED_SHEET(new Color(50, 50, 54, 255), new Color(47, 47, 51, 255), new Color(69, 69, 74, 255), Color.WHITE, new Color(180, 180, 180)),
    LINEN(new Color(50, 50, 54, 255), new Color(47, 47, 51, 255), new Color(69, 69, 74, 255), Color.BLACK, new Color(80, 80, 80)),
    NOISY_PLASTIC(new Color(50, 50, 54, 255), new Color(47, 47, 51, 255), new Color(69, 69, 74, 255), Color.WHITE, new Color(180, 180, 180)),
    TRANSPARENT(new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), Color.BLACK, new Color(80, 80, 80)),
    CUSTOM(null, null, null, Color.BLACK, new Color(80, 80, 80));
    public final Color GRADIENT_START_COLOR;
    public final Color GRADIENT_FRACTION_COLOR;
    public final Color GRADIENT_STOP_COLOR;
    public final Color LABEL_COLOR;
    public final Color SYMBOL_COLOR;

    private BackgroundColor(final Color GRADIENT_START_COLOR, final Color GRADIENT_FRACTION_COLOR,
                            final Color GRADIENT_STOP_COLOR, final Color LABEL_COLOR,
                            final Color SYMBOL_COLOR) {
        this.GRADIENT_START_COLOR = GRADIENT_START_COLOR;
        this.GRADIENT_FRACTION_COLOR = GRADIENT_FRACTION_COLOR;
        this.GRADIENT_STOP_COLOR = GRADIENT_STOP_COLOR;
        this.LABEL_COLOR = LABEL_COLOR;
        this.SYMBOL_COLOR = SYMBOL_COLOR;
    }
}
