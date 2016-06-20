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
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;


/**
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public class GradientWrapper implements Paint {

    private final LinearGradientPaint GRADIENT;
    private float[] fractions;
    private Color[] colors;

    public GradientWrapper(float startX, float startY, float endX, float endY, float[] fractions, Color[] colors) {
        GRADIENT = new LinearGradientPaint(new Point2D.Float(startX, startY), new Point2D.Float(endX, endY), fractions, colors, java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE);
        copyArrays(fractions, colors);
    }

    public GradientWrapper(float startX, float startY, float endX, float endY, float[] fractions, Color[] colors, MultipleGradientPaint.CycleMethod cycleMethod) {
        GRADIENT = new LinearGradientPaint(new Point2D.Float(startX, startY), new Point2D.Float(endX, endY), fractions, colors, cycleMethod);
        copyArrays(fractions, colors);
    }

    public GradientWrapper(Point2D start, Point2D end, float[] fractions, Color[] colors) {
        GRADIENT = new LinearGradientPaint(start, end, fractions, colors, java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE);
        copyArrays(fractions, colors);
    }

    public GradientWrapper(Point2D start, Point2D end, float[] fractions, Color[] colors, MultipleGradientPaint.CycleMethod cycleMethod) {
        GRADIENT = new LinearGradientPaint(start, end, fractions, colors, cycleMethod, java.awt.MultipleGradientPaint.ColorSpaceType.SRGB, new AffineTransform());
        copyArrays(fractions, colors);
    }

    public GradientWrapper(Point2D start, Point2D end, float[] fractions, Color[] colors, MultipleGradientPaint.CycleMethod cycleMethod, MultipleGradientPaint.ColorSpaceType colorSpace, AffineTransform gradientTransform) {
        GRADIENT = new LinearGradientPaint(start, end, fractions, colors, cycleMethod, colorSpace, gradientTransform);
        copyArrays(fractions, colors);
    }

    /**
     * Returns the point where the wrapped LinearGradientPaint will start
     * @return the point where the wrapped LinearGradientPaint will start
     */
    public Point2D getStartPoint() {
        return GRADIENT.getStartPoint();
    }

    /**
     * Returns the point where the wrapped LinearGradientPaint will stop
     * @return the point where the wrapped LinearGradientPaint will stop
     */
    public Point2D getEndPoint() {
        return GRADIENT.getEndPoint();
    }

    /**
     * Returns the color that is defined by the given fraction in the linear gradient paint
     * @param FRACTION
     * @return the color that is defined by the given fraction in the linear gradient paint
     */
    public Color getColorAt(final float FRACTION) {
        float fraction = FRACTION < 0f ? 0f : (FRACTION > 1f ? 1f : FRACTION);
        float lowerLimit = 0f;
        int lowerIndex = 0;
        float upperLimit = 1f;
        int upperIndex = 1;
        int index = 0;

        for (float currentFraction : fractions) {
            if (Float.compare(currentFraction, fraction) < 0) {
                lowerLimit = currentFraction;
                lowerIndex = index;
            }
            if (Float.compare(currentFraction, fraction) == 0) {
                return colors[index];
            }
            if (Float.compare(currentFraction, fraction) > 0) {
                upperLimit = currentFraction;
                upperIndex = index;
                break;
            }
            index++;
        }

        float interpolationFraction = (fraction - lowerLimit) / (upperLimit - lowerLimit);

        return interpolateColor(colors[lowerIndex], colors[upperIndex], interpolationFraction);
    }

    /**
     * Returns the wrapped LinearGradientPaint
     * @return the wrapped LinearGradientPaint
     */
    public LinearGradientPaint getGradient() {
        return GRADIENT;
    }

    @Override
    public PaintContext createContext(final ColorModel COLOR_MODEL, final Rectangle DEVICE_BOUNDS, Rectangle2D USER_BOUNDS, AffineTransform X_FORM, RenderingHints HINTS) {
        return GRADIENT.createContext(COLOR_MODEL, DEVICE_BOUNDS, USER_BOUNDS, X_FORM, HINTS);
    }

    @Override
    public int getTransparency() {
        return GRADIENT.getTransparency();
    }

    /**
     * Just create a local copy of the fractions and colors array
     * @param FRACTIONS
     * @param colors
     */
    private void copyArrays(final float[] FRACTIONS, final Color[] colors) {
        fractions = new float[FRACTIONS.length];
        System.arraycopy(FRACTIONS, 0, fractions, 0, FRACTIONS.length);
        this.colors = colors.clone();
    }

    /**
     * Returns the interpolated color that you get if you multiply the delta between
     * color2 and color1 with the given fraction (for each channel) and interpolation. The fraction should
     * be a value between 0 and 1.
     * @param COLOR1 The first color as integer in the hex format 0xALPHA RED GREEN BLUE, e.g. 0xFF00FF00 for a pure green
     * @param COLOR2 The second color as integer in the hex format 0xALPHA RED GREEN BLUE e.g. 0xFFFF0000 for a pure red
     * @param FRACTION The fraction between those two colors that we would like to get e.g. 0.5f will result in the color 0xFF808000
     * @return the interpolated color between color1 and color2 calculated by the given fraction and interpolation
     */
    private Color interpolateColor(final Color COLOR1, final Color COLOR2, final float FRACTION) {
        assert (Float.compare(FRACTION, 0f) >= 0 && Float.compare(FRACTION, 1f) <= 0);

        final float INT_TO_FLOAT_CONST = 1f / 255f;

        final float RED1 = COLOR1.getRed() * INT_TO_FLOAT_CONST;
        final float GREEN1 = COLOR1.getGreen() * INT_TO_FLOAT_CONST;
        final float BLUE1 = COLOR1.getBlue() * INT_TO_FLOAT_CONST;
        final float ALPHA1 = COLOR1.getAlpha() * INT_TO_FLOAT_CONST;

        final float RED2 = COLOR2.getRed() * INT_TO_FLOAT_CONST;
        final float GREEN2 = COLOR2.getGreen() * INT_TO_FLOAT_CONST;
        final float BLUE2 = COLOR2.getBlue() * INT_TO_FLOAT_CONST;
        final float ALPHA2 = COLOR2.getAlpha() * INT_TO_FLOAT_CONST;

        final float DELTA_RED = RED2 - RED1;
        final float DELTA_GREEN = GREEN2 - GREEN1;
        final float DELTA_BLUE = BLUE2 - BLUE1;
        final float DELTA_ALPHA = ALPHA2 - ALPHA1;

        float red = RED1 + (DELTA_RED * FRACTION);
        float green = GREEN1 + (DELTA_GREEN * FRACTION);
        float blue = BLUE1 + (DELTA_BLUE * FRACTION);
        float alpha = ALPHA1 + (DELTA_ALPHA * FRACTION);

        red = red < 0f ? 0f : (red > 1f ? 1f : red);
        green = green < 0f ? 0f : (green > 1f ? 1f : green);
        blue = blue < 0f ? 0f : (blue > 1f ? 1f : blue);
        alpha = alpha < 0f ? 0f : (alpha > 1f ? 1f : alpha);

        return new Color(red, green, blue, alpha);
    }
}
