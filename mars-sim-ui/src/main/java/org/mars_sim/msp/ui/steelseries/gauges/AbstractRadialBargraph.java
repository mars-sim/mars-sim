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
package org.mars_sim.msp.ui.steelseries.gauges;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.mars_sim.msp.ui.steelseries.tools.ColorDef;
import org.mars_sim.msp.ui.steelseries.tools.CustomColorDef;


/**
 *
 * @author hansolo
 */
public abstract class AbstractRadialBargraph extends AbstractRadial {
    // <editor-fold defaultstate="collapsed" desc="Variable declarations">

    private final float[] LED_FRACTIONS = {
        0.0f,
        1.0f
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public AbstractRadialBargraph() {
        super();
        setLedPosition(0.453271028, 0.65);
        setUserLedPosition(0.453271028, 0.59);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getter/Setter">
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
        init(getInnerBounds().width, getInnerBounds().width);
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
        init(getInnerBounds().width, getInnerBounds().width);
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

    // <editor-fold defaultstate="collapsed" desc="Image creation methods">
    /**
     * Returns the bargraph track image
     * with the given with and height.
     * @param WIDTH
     * @param START_ANGLE
     * @param ANGLE_EXTEND
     * @param APEX_ANGLE
     * @param BARGRAPH_OFFSET
     * @return buffered image containing the bargraph track image
     */
    protected BufferedImage create_BARGRAPH_TRACK_Image(final int WIDTH, final double START_ANGLE,
                                                                       final double ANGLE_EXTEND,
                                                                       final double APEX_ANGLE,
                                                                       final double BARGRAPH_OFFSET) {
        return create_BARGRAPH_TRACK_Image(WIDTH, START_ANGLE, ANGLE_EXTEND, APEX_ANGLE, BARGRAPH_OFFSET, null);
    }

    /**
     * Returns the bargraph track image
     * with the given with and height.
     * @param WIDTH
     * @param START_ANGLE
     * @param ANGLE_EXTEND
     * @param APEX_ANGLE
     * @param BARGRAPH_OFFSET
     * @param image
     * @return buffered image containing the bargraph track image
     */
    protected BufferedImage create_BARGRAPH_TRACK_Image(final int WIDTH, final double START_ANGLE,
                                                                       final double ANGLE_EXTEND,
                                                                       final double APEX_ANGLE,
                                                                       final double BARGRAPH_OFFSET,
                                                                       BufferedImage image) {
        if (WIDTH <= 0) {
            return null;
        }

        if (image == null) {
            image = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        }
        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//        G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
//        G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
//        G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
//        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        // Create led track
        final java.awt.geom.Arc2D BACK = new java.awt.geom.Arc2D.Double(java.awt.geom.Arc2D.PIE);
        BACK.setFrame(IMAGE_WIDTH * 0.1074766355, IMAGE_HEIGHT * 0.1074766355, IMAGE_WIDTH * 0.785046729, IMAGE_HEIGHT * 0.785046729);
        BACK.setAngleStart(START_ANGLE + 2);
        BACK.setAngleExtent(ANGLE_EXTEND - 5);

        final Ellipse2D BACK_SUB = new Ellipse2D.Double(IMAGE_WIDTH * 0.1822429907, IMAGE_HEIGHT * 0.1822429907, IMAGE_WIDTH * 0.6355140187, IMAGE_HEIGHT * 0.6355140187);

        final java.awt.geom.Area LED_TRACK_FRAME = new java.awt.geom.Area(BACK);
        LED_TRACK_FRAME.subtract(new java.awt.geom.Area(BACK_SUB));

        final Point2D LED_TRACK_FRAME_START = new Point2D.Double(0, LED_TRACK_FRAME.getBounds2D().getMinY());
        final Point2D LED_TRACK_FRAME_STOP = new Point2D.Double(0, LED_TRACK_FRAME.getBounds2D().getMaxY());
        final float[] LED_TRACK_FRAME_FRACTIONS = {
            0.0f,
            0.22f,
            0.76f,
            1.0f
        };
        final Color[] LED_TRACK_FRAME_COLORS = {
            new Color(0, 0, 0, 255),
            new Color(51, 51, 51, 255),
            new Color(51, 51, 51, 255),
            new Color(100, 100, 100, 255)
        };
        final LinearGradientPaint LED_TRACK_FRAME_GRADIENT = new LinearGradientPaint(LED_TRACK_FRAME_START, LED_TRACK_FRAME_STOP, LED_TRACK_FRAME_FRACTIONS, LED_TRACK_FRAME_COLORS);
        G2.setPaint(LED_TRACK_FRAME_GRADIENT);
        G2.fill(LED_TRACK_FRAME);

        final java.awt.geom.Arc2D FRONT = new java.awt.geom.Arc2D.Double(java.awt.geom.Arc2D.PIE);
        FRONT.setFrame(IMAGE_WIDTH * 0.1121495327, IMAGE_HEIGHT * 0.1121495327, IMAGE_WIDTH * 0.7803738318, IMAGE_HEIGHT * 0.7803738318);
        FRONT.setAngleStart(START_ANGLE);
        FRONT.setAngleExtent(ANGLE_EXTEND);

        final Ellipse2D FRONT_SUB = new Ellipse2D.Double(IMAGE_WIDTH * 0.1822429907, IMAGE_HEIGHT * 0.1822429907, IMAGE_WIDTH * 0.6448598131, IMAGE_HEIGHT * 0.6448598131);

        final java.awt.geom.Area LED_TRACK_MAIN = new java.awt.geom.Area(BACK);
        LED_TRACK_MAIN.subtract(new java.awt.geom.Area(FRONT_SUB));

        final Point2D LED_TRACK_MAIN_START = new Point2D.Double(0, LED_TRACK_MAIN.getBounds2D().getMinY());
        final Point2D LED_TRACK_MAIN_STOP = new Point2D.Double(0, LED_TRACK_MAIN.getBounds2D().getMaxY());
        final float[] LED_TRACK_MAIN_FRACTIONS = {
            0.0f,
            1.0f
        };
        final Color[] LED_TRACK_MAIN_COLORS = {
            new Color(17, 17, 17, 255),
            new Color(51, 51, 51, 255)
        };
        final LinearGradientPaint LED_TRACK_MAIN_GRADIENT = new LinearGradientPaint(LED_TRACK_MAIN_START, LED_TRACK_MAIN_STOP, LED_TRACK_MAIN_FRACTIONS, LED_TRACK_MAIN_COLORS);
        G2.setPaint(LED_TRACK_MAIN_GRADIENT);
        G2.fill(LED_TRACK_MAIN);

        // Draw the inactive leds
        final Point2D CENTER = new Point2D.Double(WIDTH / 2.0, WIDTH / 2.0);
        final Rectangle2D LED = new Rectangle2D.Double(WIDTH * 0.1168224299, WIDTH * 0.4859813084, WIDTH * 0.06074766355140187, WIDTH * 0.023364486);
        final Point2D LED_CENTER = new Point2D.Double(LED.getCenterX(), LED.getCenterY());

        final Color[] LED_COLORS = new Color[]{
            new Color(60, 60, 60, 255),
            new Color(50, 50, 50, 255)
        };
        final RadialGradientPaint LED_GRADIENT = new RadialGradientPaint(LED_CENTER, (float) (0.030373831775700934 * IMAGE_WIDTH), LED_FRACTIONS, LED_COLORS);
        G2.setPaint(LED_GRADIENT);

        final AffineTransform OLD_TRANSFORM = G2.getTransform();

        for (double angle = 0; angle <= APEX_ANGLE; angle += 5.0) {
            G2.rotate(Math.toRadians(angle + BARGRAPH_OFFSET), CENTER.getX(), CENTER.getY());
            G2.fill(LED);
            G2.setTransform(OLD_TRANSFORM);
        }

        G2.dispose();

        return image;
    }
    // </editor-fold>
}
