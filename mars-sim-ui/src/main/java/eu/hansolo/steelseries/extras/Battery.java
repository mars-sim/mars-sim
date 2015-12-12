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
package eu.hansolo.steelseries.extras;

import eu.hansolo.steelseries.tools.GradientWrapper;
import eu.hansolo.steelseries.tools.Orientation;
import eu.hansolo.steelseries.tools.Util;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.border.Border;

/**
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public class Battery extends JComponent {
    // <editor-fold defaultstate="collapsed" desc="Variable declaration">
    private static final Util UTIL = Util.INSTANCE;
    private final java.awt.Rectangle INNER_BOUNDS;
    private int value = 0;
    private boolean initialized;
    private BufferedImage batteryImage = UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
    private final Color FULL_BORDER = new Color(0x79A24B);
    private final Color HALF_BORDER = new Color(0xDBA715);
    private final Color EMPTY_BORDER = new Color(0xB11902);
    private final Color FULL_DARK = new Color(0xA3D866);
    private final Color FULL_LIGHT = new Color(0xDFE956);
    private final Color HALF_DARK = new Color(0xE4BD20);
    private final Color HALF_LIGHT = new Color(0xF6F49D);
    private final Color EMPTY_DARK = new Color(0xC62705);
    private final Color EMPTY_LIGHT = new Color(0xF67930);
    private GradientWrapper borderGradient;
    private GradientWrapper liquidGradientDark;
    private GradientWrapper liquidGradientLight;
    private Orientation lightPosition;
    private final transient ComponentListener COMPONENT_LISTENER = new ComponentAdapter() {

        @Override
        public void componentResized(java.awt.event.ComponentEvent event) {
            //init(getWidth(), getHeight());

            //repaint(INNER_BOUNDS);

            //****************//
            java.awt.Container parent = getParent();
            if ((parent != null) && (parent.getLayout() == null)) {
                setSize(getWidth(), getHeight());
            } else {
                setPreferredSize(new java.awt.Dimension(getWidth(), getHeight()));
            }

            calcInnerBounds();

            init(INNER_BOUNDS.width, INNER_BOUNDS.height);
            //revalidate();
            //repaint(INNER_BOUNDS);

        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public Battery() {
        super();
        lightPosition = Orientation.NORTH;
        INNER_BOUNDS = new Rectangle(40, 18);
        initialized = false;
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        addComponentListener(COMPONENT_LISTENER);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    private void init(final int WIDTH, final int HEIGHT) {
        if (WIDTH <= 1) {
            return;
        }

        if (batteryImage != null) {
            batteryImage.flush();
        }
        batteryImage = create_BATTERY_Image(WIDTH, HEIGHT - getInsets().bottom, value);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Visualization">
    @Override
    protected void paintComponent(Graphics g) {
        if (!initialized) {
            return;
        }

        final Graphics2D G2 = (Graphics2D) g.create();

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        G2.translate(INNER_BOUNDS.x, INNER_BOUNDS.y);

        G2.drawImage(batteryImage, 0, 0, null);

        G2.translate(-INNER_BOUNDS.x, -INNER_BOUNDS.y);

        G2.dispose();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">
    /**
     * Returns the value of the battery as integer (0 - 100)
     * @return the value of the battery as integer (0 - 100)
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets the current charge of the battery as integer from 0 - 100
     * @param VALUE
     */
    public void setValue(final int VALUE) {
        value = VALUE < 0 ? 0 : (VALUE > 100 ? 100 : VALUE);
        init(getWidth(), getHeight());
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns the position of the light that affects the gradient of the battery frame
     * @return the position of the light that affects the gradient of the battery frame
     */
    public Orientation getLightPosition() {
        return lightPosition;
    }

    /**
     * Set the position of the light that affects the gradient of the battery frame
     * @param LIGHT_POSITION
     */
    public void setLightPosition(final Orientation LIGHT_POSITION) {
        lightPosition = LIGHT_POSITION;
        init(getWidth(), getHeight());
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns the current component as buffered image.
     * To save this buffered image as png you could use for example:
     * File file = new File("image.png");
     * ImageIO.write(Image, "png", file);
     * @return the current component as buffered image
     */
    public BufferedImage getAsImage() {
        final BufferedImage IMAGE = UTIL.createImage(getWidth(), getHeight(), Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        paintAll(G2);
        G2.dispose();
        return IMAGE;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image related">
    /**
     * Returns a buffered image that represents a battery
     * @param WIDTH
     * @param HEIGHT
     * @param VALUE
     * @return a buffered image that represents a battery
     */
    public BufferedImage create_BATTERY_Image(final int WIDTH, final int HEIGHT, final int VALUE) {
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        // Background
        final GeneralPath BATTERY = new GeneralPath();
        BATTERY.setWindingRule(Path2D.WIND_EVEN_ODD);
        BATTERY.moveTo(IMAGE_WIDTH * 0.025, IMAGE_HEIGHT * 0.05555555555555555);
        BATTERY.lineTo(IMAGE_WIDTH * 0.9, IMAGE_HEIGHT * 0.05555555555555555);
        BATTERY.lineTo(IMAGE_WIDTH * 0.9, IMAGE_HEIGHT * 0.9444444444444444);
        BATTERY.lineTo(IMAGE_WIDTH * 0.025, IMAGE_HEIGHT * 0.9444444444444444);
        BATTERY.lineTo(IMAGE_WIDTH * 0.025, IMAGE_HEIGHT * 0.05555555555555555);
        BATTERY.closePath();
        BATTERY.moveTo(IMAGE_WIDTH * 0.925, IMAGE_HEIGHT * 0.0);
        BATTERY.lineTo(IMAGE_WIDTH * 0.0, IMAGE_HEIGHT * 0.0);
        BATTERY.lineTo(IMAGE_WIDTH * 0.0, IMAGE_HEIGHT * 1.0);
        BATTERY.lineTo(IMAGE_WIDTH * 0.925, IMAGE_HEIGHT * 1.0);
        BATTERY.lineTo(IMAGE_WIDTH * 0.925, IMAGE_HEIGHT * 0.7222222222222222);
        BATTERY.curveTo(IMAGE_WIDTH * 0.925, IMAGE_HEIGHT * 0.7222222222222222, IMAGE_WIDTH * 0.975, IMAGE_HEIGHT * 0.7222222222222222, IMAGE_WIDTH * 0.975, IMAGE_HEIGHT * 0.7222222222222222);
        BATTERY.curveTo(IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 0.7222222222222222, IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 0.6666666666666666, IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 0.6666666666666666);
        BATTERY.curveTo(IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 0.6666666666666666, IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 0.3333333333333333, IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 0.3333333333333333);
        BATTERY.curveTo(IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 0.3333333333333333, IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 0.2777777777777778, IMAGE_WIDTH * 0.975, IMAGE_HEIGHT * 0.2777777777777778);
        BATTERY.curveTo(IMAGE_WIDTH * 0.975, IMAGE_HEIGHT * 0.2777777777777778, IMAGE_WIDTH * 0.925, IMAGE_HEIGHT * 0.2777777777777778, IMAGE_WIDTH * 0.925, IMAGE_HEIGHT * 0.2777777777777778);
        BATTERY.lineTo(IMAGE_WIDTH * 0.925, IMAGE_HEIGHT * 0.0);
        BATTERY.closePath();
        final Point2D BATTERY_START = new Point2D.Double();
        final Point2D BATTERY_STOP = new Point2D.Double();

        switch(lightPosition)
        {
            case NORTH_EAST:
                BATTERY_START.setLocation(BATTERY.getBounds2D().getMaxX(), BATTERY.getBounds2D().getMinY());
                BATTERY_STOP.setLocation(BATTERY.getBounds2D().getMinX(), BATTERY.getBounds2D().getMaxY());
                break;
            case EAST:
                BATTERY_START.setLocation(BATTERY.getBounds2D().getMaxX(), 0);
                BATTERY_STOP.setLocation(BATTERY.getBounds2D().getMinX(), 0);
                break;
            case SOUTH_EAST:
                BATTERY_START.setLocation(BATTERY.getBounds2D().getMaxX(), BATTERY.getBounds2D().getMaxY());
                BATTERY_STOP.setLocation(BATTERY.getBounds2D().getMinX(), BATTERY.getBounds2D().getMinY());
                break;
            case SOUTH:
                BATTERY_START.setLocation(0, BATTERY.getBounds2D().getMaxY());
                BATTERY_STOP.setLocation(0, BATTERY.getBounds2D().getMinY());
                break;
            case SOUTH_WEST:
                BATTERY_START.setLocation(BATTERY.getBounds2D().getMinX(), BATTERY.getBounds2D().getMaxY());
                BATTERY_STOP.setLocation(BATTERY.getBounds2D().getMaxX(), BATTERY.getBounds2D().getMinY());
                break;
            case WEST:
                BATTERY_START.setLocation(BATTERY.getBounds2D().getMinX(), 0);
                BATTERY_STOP.setLocation(BATTERY.getBounds2D().getMaxX(), 0);
                break;
            case NORTH_WEST:
                BATTERY_START.setLocation(BATTERY.getBounds2D().getMinX(), BATTERY.getBounds2D().getMinY());
                BATTERY_STOP.setLocation(BATTERY.getBounds2D().getMaxX(), BATTERY.getBounds2D().getMaxY());
                break;
            case NORTH:
            default:
                BATTERY_START.setLocation(0, BATTERY.getBounds2D().getMinY());
                BATTERY_STOP.setLocation(0, BATTERY.getBounds2D().getMaxY());
                break;
        }

        final float[] BATTERY_FRACTIONS = {
            0.0f,
            1.0f
        };
        final Color[] BATTERY_COLORS = {
            new Color(255, 255, 255, 255),
            new Color(126, 126, 126, 255)
        };
        final LinearGradientPaint BATTERY_GRADIENT = new LinearGradientPaint(BATTERY_START, BATTERY_STOP, BATTERY_FRACTIONS, BATTERY_COLORS);
        G2.setPaint(BATTERY_GRADIENT);
        G2.fill(BATTERY);

        // Main
        final Rectangle2D BORDER = new Rectangle2D.Double(IMAGE_WIDTH * 0.025, IMAGE_WIDTH * 0.025, IMAGE_WIDTH * 0.875 * (VALUE / 100.0), IMAGE_HEIGHT * 0.88888888888888);
        final float[] BORDER_FRACTIONS = {
            0.0f,
            0.40f,
            1.0f
        };
        final Color[] BORDER_COLORS = {
            EMPTY_BORDER,
            HALF_BORDER,
            FULL_BORDER
        };
        borderGradient = new GradientWrapper(new Point2D.Double(0, 0), new Point2D.Double(100, 0), BORDER_FRACTIONS, BORDER_COLORS);
        G2.setPaint(borderGradient.getColorAt(VALUE / 100f));
        G2.fill(BORDER);

        final Rectangle2D LIQUID = new Rectangle2D.Double(IMAGE_WIDTH * 0.05, IMAGE_WIDTH * 0.05, IMAGE_WIDTH * 0.85 * (VALUE / 100.0), IMAGE_HEIGHT * 0.77777777777777);
        final Point2D LIQUID_START = new Point2D.Double(IMAGE_WIDTH * 0.05, 0);
        final Point2D LIQUID_STOP = new Point2D.Double(IMAGE_WIDTH * 0.875, 0);
        final float[] LIQUID_FRACTIONS = {
            0.0f,
            0.5f,
            1.0f
        };
        final Color[] LIQUID_COLORS_DARK = {
            EMPTY_DARK,
            HALF_DARK,
            FULL_DARK
        };
        final Color[] LIQUID_COLORS_LIGHT = {
            EMPTY_LIGHT,
            HALF_LIGHT,
            FULL_LIGHT
        };
        final float[] LIQUID_GRADIENT_FRACTIONS = {
            0.0f,
            0.4f,
            1.0f
        };
        liquidGradientDark = new GradientWrapper(new Point2D.Double(0, 0), new Point2D.Double(100, 0), LIQUID_GRADIENT_FRACTIONS, LIQUID_COLORS_DARK);
        liquidGradientLight = new GradientWrapper(new Point2D.Double(0, 0), new Point2D.Double(100, 0), LIQUID_GRADIENT_FRACTIONS, LIQUID_COLORS_LIGHT);
        final Color[] LIQUID_COLORS = {
            liquidGradientDark.getColorAt(VALUE / 100f),
            liquidGradientLight.getColorAt(VALUE / 100f),
            liquidGradientDark.getColorAt(VALUE / 100f)
        };
        final LinearGradientPaint LIQUID_GRADIENT = new LinearGradientPaint(LIQUID_START, LIQUID_STOP, LIQUID_FRACTIONS, LIQUID_COLORS);
        G2.setPaint(LIQUID_GRADIENT);
        G2.fill(LIQUID);

        // Foreground
        final Rectangle2D HIGHLIGHT = new Rectangle2D.Double(IMAGE_WIDTH * 0.025, IMAGE_WIDTH * 0.025, IMAGE_WIDTH * 0.875, IMAGE_HEIGHT * 0.44444444444444);
        final Point2D HIGHLIGHT_START = new Point2D.Double(0, HIGHLIGHT.getBounds2D().getMinY());
        final Point2D HIGHLIGHT_STOP = new Point2D.Double(0, HIGHLIGHT.getBounds2D().getMaxY());
        final float[] HIGHLIGHT_FRACTIONS = {
            0.0f,
            1.0f
        };
        final Color[] HIGHLIGHT_COLORS = {
            new Color(1.0f, 1.0f, 1.0f, 0.0f),
            new Color(1.0f, 1.0f, 1.0f, 0.8f)
        };

        final LinearGradientPaint HIGHLIGHT_GRADIENT = new LinearGradientPaint(HIGHLIGHT_START, HIGHLIGHT_STOP, HIGHLIGHT_FRACTIONS, HIGHLIGHT_COLORS);
        G2.setPaint(HIGHLIGHT_GRADIENT);
        G2.fill(HIGHLIGHT);

        G2.dispose();

        return IMAGE;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Size related">
    /**
     * Calculates the rectangle that specifies the area that is available
     * for painting the gauge. This means that if the component has insets
     * that are larger than 0, these will be taken into account.
     */
    private void calcInnerBounds() {
        final java.awt.Insets INSETS = getInsets();
        INNER_BOUNDS.setBounds(INSETS.left, INSETS.top, (getWidth() - INSETS.left - INSETS.right), (getHeight() - INSETS.top - INSETS.bottom));
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension dim = super.getMinimumSize();
        if (dim.width < 40 || dim.height < 18) {
            dim = new Dimension(40, 18);
        }
        return dim;
    }

    @Override
    public void setMinimumSize(final Dimension DIM) {
        int width = DIM.width < 40 ? 40 : DIM.width;
        int height = DIM.height < 18 ? 18 : DIM.height;
        super.setMinimumSize(new Dimension(width, height));
        calcInnerBounds();
        init(INNER_BOUNDS.width, (int) (0.45 * INNER_BOUNDS.width));
        invalidate();
        repaint();
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension dim = super.getMaximumSize();
        if (dim.width > 1920 || dim.height > 864) {
            dim = new Dimension(1920, 864);
        }
        return dim;
    }

    @Override
    public void setMaximumSize(final Dimension DIM) {
        int width = DIM.width > 1920 ? 1920 : DIM.width;
        int height = DIM.height > 864 ? 864 : DIM.height;
        super.setMaximumSize(new Dimension(width, height));
        calcInnerBounds();
        init(INNER_BOUNDS.width, (int) (0.45 * INNER_BOUNDS.width));
        invalidate();
        repaint();
    }

    @Override
    public void setPreferredSize(final Dimension DIM) {
        super.setPreferredSize(new Dimension(DIM.width, (int) (0.45 * DIM.width)));
        calcInnerBounds();
        init(INNER_BOUNDS.width, (int) (0.45 * INNER_BOUNDS.width));
        initialized = true;
    }

    @Override
    public void setSize(final int WIDTH, final int HEIGHT) {
        super.setSize(WIDTH, (int) (0.45 * WIDTH));
        calcInnerBounds();
        init(INNER_BOUNDS.width, (int) (0.45 * INNER_BOUNDS.width));
        initialized = true;
    }

    @Override
    public void setSize(final Dimension DIM) {
        super.setPreferredSize(new java.awt.Dimension(DIM.width, (int) (0.45 * DIM.width)));
        calcInnerBounds();
        init(INNER_BOUNDS.width, (int) (0.45 * INNER_BOUNDS.width));
        initialized = true;
    }

    @Override
    public void setBounds(final Rectangle BOUNDS) {
        super.setBounds(new java.awt.Rectangle(BOUNDS.x, BOUNDS.y, BOUNDS.width, (int) (0.45 * BOUNDS.width)));
        calcInnerBounds();
        init(INNER_BOUNDS.width, (int) (0.45 * INNER_BOUNDS.width));
        initialized = true;
    }

    @Override
    public void setBounds(final int X, final int Y, final int WIDTH, final int HEIGHT) {
        super.setBounds(X, Y, WIDTH, (int) (0.45 * WIDTH));
        calcInnerBounds();
        init(INNER_BOUNDS.width, (int) (0.45 * INNER_BOUNDS.width));
        initialized = true;
    }

    @Override
    public void setBorder(final Border BORDER) {
        super.setBorder(BORDER);
        calcInnerBounds();
        init(INNER_BOUNDS.width, (int) (0.45 * INNER_BOUNDS.width));
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "Battery";
    }
}
