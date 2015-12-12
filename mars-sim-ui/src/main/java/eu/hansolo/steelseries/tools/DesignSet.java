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
import java.awt.Paint;


/**
 * <p/>
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public class DesignSet {

    private final FrameDesign FRAME_DESIGN;
    private final FrameEffect FRAME_EFFECT;
    private final Paint OUTER_FRAME_COLOR;
    private final Paint INNER_FRAME_COLOR;
    private final BackgroundColor BACKGROUND_COLOR;
    private final Color TEXTURE_COLOR;
    private final ColorDef COLOR;
    private final LedColor LED_COLOR;
    private final LedColor USER_LED_COLOR;
    private final LcdColor LCD_COLOR;
    private final Color GLOW_COLOR;
    private final KnobStyle KNOB_STYLE;

    private DesignSet(Builder builder) {
        // private Constructor can only be called from Builder
        FRAME_DESIGN = builder.frameDesign;
        FRAME_EFFECT = builder.frameEffect;
        OUTER_FRAME_COLOR = builder.outerFrameColor;
        INNER_FRAME_COLOR = builder.innerFrameColor;
        BACKGROUND_COLOR = builder.backgroundColor;
        TEXTURE_COLOR = builder.textureColor;
        COLOR = builder.color;
        LED_COLOR = builder.ledColor;
        USER_LED_COLOR = builder.userLedColor;
        LCD_COLOR = builder.lcdColor;
        GLOW_COLOR = builder.glowColor;
        KNOB_STYLE = builder.knobStyle;
    }

    public FrameDesign getFrameDesign() {
        return FRAME_DESIGN;
    }

    public FrameEffect getFrameEffect() {
        return FRAME_EFFECT;
    }

    public Paint getOuterFrameColor() {
        return OUTER_FRAME_COLOR;
    }

    public Paint getInnerFrameColor() {
        return INNER_FRAME_COLOR;
    }

    public BackgroundColor getBackgroundColor() {
        return BACKGROUND_COLOR;
    }

    public Color getTextureColor() {
        return TEXTURE_COLOR;
    }

    public ColorDef getColor() {
        return COLOR;
    }

    public LedColor getLedColor() {
        return LED_COLOR;
    }

    public LedColor getUserLedColor() {
        return USER_LED_COLOR;
    }

    public LcdColor getLcdColor() {
        return LCD_COLOR;
    }

    public Color getGlowColor() {
        return GLOW_COLOR;
    }

    public KnobStyle getKnobStyle() {
        return KNOB_STYLE;
    }

    public static class Builder {
        // mandatory parameter
        private FrameDesign frameDesign = null;
        private FrameEffect frameEffect = null;
        private Paint outerFrameColor = null;
        private Paint innerFrameColor = null;
        private BackgroundColor backgroundColor = null;
        private Color textureColor = null;
        private ColorDef color = null;
        private LedColor ledColor = null;
        private LedColor userLedColor = null;
        private LcdColor lcdColor = null;
        private Color glowColor = null;
        private KnobStyle knobStyle = null;

        public Builder() {
        }

        public Builder frameDesign(FrameDesign frameDesign) {
            this.frameDesign = frameDesign;
            return this;
        }

        public Builder frameEffect(FrameEffect frameEffect) {
            this.frameEffect = frameEffect;
            return this;
        }

        public Builder outerFrameColor(Paint outerFrameColor) {
            this.outerFrameColor = outerFrameColor;
            return this;
        }

        public Builder innerFrameColor(Paint innerFrameColor) {
            this.innerFrameColor = innerFrameColor;
            return this;
        }

        public Builder backgroundColor(BackgroundColor backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder textureColor(Color textureColor) {
            this.textureColor = textureColor;
            return this;
        }

        public Builder color(ColorDef color) {
            this.color = color;
            return this;
        }

        public Builder ledColor(LedColor ledColor) {
            this.ledColor = ledColor;
            return this;
        }

        public Builder userLedColor(LedColor userLedColor) {
            this.userLedColor = userLedColor;
            return this;
        }

        public Builder lcdColor(LcdColor lcdColor) {
            this.lcdColor = lcdColor;
            return this;
        }

        public Builder glowColor(Color glowColor) {
            this.glowColor = glowColor;
            return this;
        }

        public Builder knobStyle(KnobStyle knobStyle) {
            this.knobStyle = knobStyle;
            return this;
        }

        public DesignSet build() {
            return new DesignSet(this);
        }
    }

}
