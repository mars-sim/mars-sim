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
package eu.hansolo.steelseries.gauges;

import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.CustomColorDef;
import eu.hansolo.steelseries.tools.Orientation;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;


/**
 *
 * @author hansolo
 */
public abstract class AbstractLinearBargraph extends AbstractLinear {
    // <editor-fold defaultstate="collapsed" desc="Constructor">

    public AbstractLinearBargraph() {
        super();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    /**
     * Returns the enum colordef that is defined for the current bargraph
     * @return enum colordef that represents the current bargraph color
     */
    public ColorDef getBarGraphColor() {
        return getModel().getValueColor();
    }

    /**
     * Sets the current bargraph color to the given enum colordef
     * @param BARGRAPH_COLOR
     */
    public void setBarGraphColor(final ColorDef BARGRAPH_COLOR) {
        getModel().setValueColor(BARGRAPH_COLOR);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns the color that will be used to calculate the custom bargraph color
     * @return the color that will be used to calculate the custom bargraph color
     */
    public Color getCustomBargraphColor() {
        return getModel().getCustomValueColor();
    }

    /**
     * Sets the color that will be used to calculate the custom bargraph color
     * @param COLOR
     */
    public void setCustomBarGraphColor(final Color COLOR) {
        getModel().setCustomValueColorObject(new CustomColorDef(COLOR));
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns the object that represents holds the custom bargraph color
     * @return the object that represents the custom bargraph color
     */
    public CustomColorDef getCustomBarGraphColorObject() {
        return getModel().getCustomValueColorObject();
    }

    /**
     * Returns true if the peak value is visible
     * @return true if the park value is visible
     */
    public boolean isPeakValueEnabled() {
        return getModel().isPeakValueVisible();
    }

    /**
     * Enables/Disables the visibility of the peak value
     * @param PEAK_VALUE_ENABLED
     */
    public void setPeakValueEnabled(final boolean PEAK_VALUE_ENABLED) {
        getModel().setPeakValueVisible(PEAK_VALUE_ENABLED);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image related">
    /**
     * Returns a buffered image of a bargraph led with the given color
     * @param WIDTH
     * @param HEIGHT
     * @param COLOR
     * @param CUSTOM_COLORS
     * @return a buffered image of a bargraph led with the given color
     */
    protected BufferedImage create_BARGRAPH_LED_Image(final int WIDTH, final int HEIGHT,
                                                                     final ColorDef COLOR,
                                                                     final Color[] CUSTOM_COLORS) {
        if (WIDTH <= 20 || HEIGHT <= 20) // 20 is needed otherwise the image size could be smaller than 1
        {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        final int IMAGE_WIDTH;
        final int IMAGE_HEIGHT;
        if (getOrientation() == Orientation.VERTICAL) {
            // Vertical orientation
            IMAGE_WIDTH = (int) (WIDTH * 0.1214285714);
            IMAGE_HEIGHT = (int) (HEIGHT * 0.0121359223);
        } else {
            // Horizontal orientation
            IMAGE_HEIGHT = (int) (WIDTH * 0.0121359223);
            IMAGE_WIDTH = (int) (HEIGHT * 0.1214285714);
        }

        final BufferedImage IMAGE = UTIL.createImage(IMAGE_WIDTH, IMAGE_HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        final Rectangle2D LED = new Rectangle2D.Double(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        final Point2D CENTER = new Point2D.Double(LED.getCenterX(), LED.getCenterY());
        final float[] FRACTIONS = {
            0.0f,
            1.0f
        };
        final Color[] COLORS;
        if (CUSTOM_COLORS != null && CUSTOM_COLORS.length == 2) {
            COLORS = CUSTOM_COLORS;
        } else {
            COLORS = new Color[]{
                COLOR.LIGHT,
                COLOR.DARK
            };
        }
        final float RADIUS;
        switch (getOrientation()) {
            case VERTICAL:

            default:
                RADIUS = (float) (LED.getWidth() / 2f);
                break;

            case HORIZONTAL:
                RADIUS = (float) (LED.getHeight() / 2f);
                break;
        }
        final RadialGradientPaint GRADIENT = new RadialGradientPaint(CENTER, RADIUS, FRACTIONS, COLORS);
        G2.setPaint(GRADIENT);
        G2.fill(LED);

        G2.dispose();

        return IMAGE;
    }
    // </editor-fold>
}
