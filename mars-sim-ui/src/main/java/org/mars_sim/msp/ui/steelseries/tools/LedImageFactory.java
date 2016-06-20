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
package org.mars_sim.msp.ui.steelseries.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;


/**
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public enum LedImageFactory {

    INSTANCE;
    private final Util UTIL = Util.INSTANCE;
    private int sizeBuffer = 0;
    private LedColor ledColorBuffer = LedColor.RED_LED;
    private CustomLedColor customLedColorBuffer = new CustomLedColor(Color.RED);
    private BufferedImage ledOnBuffer = UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
    private BufferedImage ledOffBuffer = UTIL.createImage(1, 1, Transparency.TRANSLUCENT);

    /**
     * Returns a image of a led with the given size, state and color.
     * If the LED_COLOR parameter equals CUSTOM the customLedColor will be used
     * to calculate the custom led colors
     * @param SIZE
     * @param STATE
     * @param LED_COLOR
     * @param CUSTOM_LED_COLOR
     * @return the led image
     */
    public final BufferedImage create_LED_Image(final int SIZE, final int STATE, final LedColor LED_COLOR, final CustomLedColor CUSTOM_LED_COLOR) {
        if (SIZE <= 11) // 11 is needed because otherwise the image size would be smaller than 1 in the worst case
        {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        if (SIZE == sizeBuffer && LED_COLOR == ledColorBuffer && CUSTOM_LED_COLOR == customLedColorBuffer) {
            switch (STATE) {
                case 0:
                    return ledOffBuffer;
                case 1:
                    return ledOnBuffer;
            }
        }

        ledOnBuffer.flush();
        ledOffBuffer.flush();

        ledOnBuffer = UTIL.createImage((int) (SIZE * 0.0934579439), (int) (SIZE * 0.0934579439), Transparency.TRANSLUCENT);
        ledOffBuffer = UTIL.createImage((int) (SIZE * 0.0934579439), (int) (SIZE * 0.0934579439), Transparency.TRANSLUCENT);

        final Graphics2D G2_ON = ledOnBuffer.createGraphics();
        final Graphics2D G2_OFF = ledOffBuffer.createGraphics();

        G2_ON.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2_ON.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2_ON.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        G2_OFF.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2_OFF.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2_OFF.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = ledOnBuffer.getWidth();
        final int IMAGE_HEIGHT = ledOnBuffer.getHeight();

        // Define led data
        final Ellipse2D LED = new Ellipse2D.Double(0.25 * IMAGE_WIDTH, 0.25 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT);
        final Ellipse2D LED_CORONA = new Ellipse2D.Double(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        final Point2D LED_CENTER = new Point2D.Double(LED.getCenterX(), LED.getCenterY());

        final float[] LED_FRACTIONS = {
            0.0f,
            0.2f,
            1.0f
        };

        final float[] LED_INNER_SHADOW_FRACTIONS = {
            0.0f,
            0.8f,
            1.0f
        };

        final Color[] LED_INNER_SHADOW_COLORS = {
            new Color(0.0f, 0.0f, 0.0f, 0.0f),
            new Color(0.0f, 0.0f, 0.0f, 0.0f),
            new Color(0.0f, 0.0f, 0.0f, 0.4f),};

        final float[] LED_ON_CORONA_FRACTIONS = {
            0.0f,
            0.6f,
            0.7f,
            0.8f,
            0.85f,
            1.0f
        };

        final Color[] LED_OFF_COLORS;
        final Color[] LED_ON_COLORS;
        final Color[] LED_ON_CORONA_COLORS;

        if (LED_COLOR == LedColor.CUSTOM) {
            LED_OFF_COLORS = new Color[]{
                CUSTOM_LED_COLOR.INNER_COLOR1_OFF,
                CUSTOM_LED_COLOR.INNER_COLOR2_OFF,
                CUSTOM_LED_COLOR.OUTER_COLOR_OFF
            };

            LED_ON_COLORS = new Color[]{
                CUSTOM_LED_COLOR.INNER_COLOR1_ON,
                CUSTOM_LED_COLOR.INNER_COLOR2_ON,
                CUSTOM_LED_COLOR.OUTER_COLOR_ON
            };

            LED_ON_CORONA_COLORS = new Color[]{
                UTIL.setAlpha(CUSTOM_LED_COLOR.CORONA_COLOR, 0.0f),
                UTIL.setAlpha(CUSTOM_LED_COLOR.CORONA_COLOR, 0.4f),
                UTIL.setAlpha(CUSTOM_LED_COLOR.CORONA_COLOR, 0.25f),
                UTIL.setAlpha(CUSTOM_LED_COLOR.CORONA_COLOR, 0.15f),
                UTIL.setAlpha(CUSTOM_LED_COLOR.CORONA_COLOR, 0.05f),
                UTIL.setAlpha(CUSTOM_LED_COLOR.CORONA_COLOR, 0.0f)
            };
        } else {
            LED_OFF_COLORS = new Color[]{
                LED_COLOR.INNER_COLOR1_OFF,
                LED_COLOR.INNER_COLOR2_OFF,
                LED_COLOR.OUTER_COLOR_OFF
            };

            LED_ON_COLORS = new Color[]{
                LED_COLOR.INNER_COLOR1_ON,
                LED_COLOR.INNER_COLOR2_ON,
                LED_COLOR.OUTER_COLOR_ON
            };

            LED_ON_CORONA_COLORS = new Color[]{
                UTIL.setAlpha(LED_COLOR.CORONA_COLOR, 0.0f),
                UTIL.setAlpha(LED_COLOR.CORONA_COLOR, 0.4f),
                UTIL.setAlpha(LED_COLOR.CORONA_COLOR, 0.25f),
                UTIL.setAlpha(LED_COLOR.CORONA_COLOR, 0.15f),
                UTIL.setAlpha(LED_COLOR.CORONA_COLOR, 0.05f),
                UTIL.setAlpha(LED_COLOR.CORONA_COLOR, 0.0f)
            };
        }

        // Define gradients for the lower led
        final RadialGradientPaint LED_OFF_GRADIENT = new RadialGradientPaint(LED_CENTER, 0.25f * IMAGE_WIDTH, LED_FRACTIONS, LED_OFF_COLORS);
        final RadialGradientPaint LED_ON_GRADIENT = new RadialGradientPaint(LED_CENTER, 0.25f * IMAGE_WIDTH, LED_FRACTIONS, LED_ON_COLORS);
        final RadialGradientPaint LED_INNER_SHADOW_GRADIENT = new RadialGradientPaint(LED_CENTER, 0.25f * IMAGE_WIDTH, LED_INNER_SHADOW_FRACTIONS, LED_INNER_SHADOW_COLORS);
        final RadialGradientPaint LED_ON_CORONA_GRADIENT = new RadialGradientPaint(LED_CENTER, 0.5f * IMAGE_WIDTH, LED_ON_CORONA_FRACTIONS, LED_ON_CORONA_COLORS);


        // Define light reflex data
        final Ellipse2D LED_LIGHTREFLEX = new Ellipse2D.Double(0.4 * IMAGE_WIDTH, 0.35 * IMAGE_WIDTH, 0.2 * IMAGE_WIDTH, 0.15 * IMAGE_WIDTH);
        final Point2D LED_LIGHTREFLEX_START = new Point2D.Double(0, LED_LIGHTREFLEX.getMinY());
        final Point2D LED_LIGHTREFLEX_STOP = new Point2D.Double(0, LED_LIGHTREFLEX.getMaxY());

        final float[] LIGHT_REFLEX_FRACTIONS = {
            0.0f,
            1.0f
        };

        final Color[] LIGHTREFLEX_COLORS = {
            new Color(1.0f, 1.0f, 1.0f, 0.4f),
            new Color(1.0f, 1.0f, 1.0f, 0.0f)
        };

        // Define light reflex gradients
        final LinearGradientPaint LED_LIGHTREFLEX_GRADIENT = new LinearGradientPaint(LED_LIGHTREFLEX_START, LED_LIGHTREFLEX_STOP, LIGHT_REFLEX_FRACTIONS, LIGHTREFLEX_COLORS);

        // Draw the led in on state
        // LED ON
        G2_ON.setPaint(LED_ON_CORONA_GRADIENT);
        G2_ON.fill(LED_CORONA);
        G2_ON.setPaint(LED_ON_GRADIENT);
        G2_ON.fill(LED);
        G2_ON.setPaint(LED_INNER_SHADOW_GRADIENT);
        G2_ON.fill(LED);
        G2_ON.setPaint(LED_LIGHTREFLEX_GRADIENT);
        G2_ON.fill(LED_LIGHTREFLEX);

        // Draw the led in off state
        // LED OFF
        G2_OFF.setPaint(LED_OFF_GRADIENT);
        G2_OFF.fill(LED);
        G2_OFF.setPaint(LED_INNER_SHADOW_GRADIENT);
        G2_OFF.fill(LED);
        G2_OFF.setPaint(LED_LIGHTREFLEX_GRADIENT);
        G2_OFF.fill(LED_LIGHTREFLEX);

        G2_ON.dispose();
        G2_OFF.dispose();

        // Buffer current values
        sizeBuffer = SIZE;
        ledColorBuffer = LED_COLOR;
        customLedColorBuffer = CUSTOM_LED_COLOR;

        switch (STATE) {
            case 1:
                // Return LED ON
                return ledOnBuffer;
            case 0:
                // Return LED OFF
            default:
                return ledOffBuffer;
        }
    }
}
