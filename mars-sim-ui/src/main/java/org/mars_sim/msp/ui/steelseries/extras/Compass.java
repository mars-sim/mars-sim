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
package org.mars_sim.msp.ui.steelseries.extras;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.mars_sim.msp.ui.steelseries.gauges.AbstractGauge;
import org.mars_sim.msp.ui.steelseries.gauges.AbstractRadial;
import org.mars_sim.msp.ui.steelseries.tools.ColorDef;
import org.mars_sim.msp.ui.steelseries.tools.PostPosition;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.ease.Spline;


/**
 *
 * @author hansolo
 */
public final class Compass extends AbstractRadial {
    // <editor-fold defaultstate="collapsed" desc="Variable declaration">
    private static final double MIN_VALUE = 0;
    private static final double MAX_VALUE = 360;
    private double value = 0;
    private double angleStep = (2 * Math.PI) / (MAX_VALUE - MIN_VALUE);
    private final Point2D CENTER = new Point2D.Double();
    // Images used to combine layers for background and foreground
    private BufferedImage bImage;
    private BufferedImage fImage;
    private BufferedImage compassRoseImage;
    private BufferedImage pointerShadowImage;
    private BufferedImage pointerImage;
    private BufferedImage disabledImage;
    private Timeline timeline = new Timeline(this);
    private final Spline EASE = new Spline(0.5f);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public Compass() {
        super();
        setPointerColor(ColorDef.RED);
        init(getInnerBounds().width, getInnerBounds().height);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    @Override
    public AbstractGauge init(final int WIDTH, final int HEIGHT) {
        final int GAUGE_WIDTH = isFrameVisible() ? WIDTH : getGaugeBounds().width;
        final int GAUGE_HEIGHT = isFrameVisible() ? HEIGHT : getGaugeBounds().height;

        if (GAUGE_WIDTH <= 1 || GAUGE_HEIGHT <= 1) {
            return this;
        }

        if (!isFrameVisible()) {
            setFramelessOffset(-getGaugeBounds().width * 0.0841121495, -getGaugeBounds().width * 0.0841121495);
        } else {
            setFramelessOffset(getGaugeBounds().x, getGaugeBounds().y);
        }

        // Create Background Image
        if (bImage != null) {
            bImage.flush();
        }
        bImage = UTIL.createImage(GAUGE_WIDTH, GAUGE_WIDTH, java.awt.Transparency.TRANSLUCENT);

        // Create Foreground Image
        if (fImage != null) {
            fImage.flush();
        }
        fImage = UTIL.createImage(GAUGE_WIDTH, GAUGE_WIDTH, java.awt.Transparency.TRANSLUCENT);

        if (isFrameVisible()) {
            switch (getFrameType()) {
                case ROUND:
                    FRAME_FACTORY.createRadialFrame(GAUGE_WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameEffect(), bImage);
                    break;
                case SQUARE:
                    FRAME_FACTORY.createLinearFrame(GAUGE_WIDTH, GAUGE_WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameEffect(), bImage);
                    break;
                default:
                    FRAME_FACTORY.createRadialFrame(GAUGE_WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameEffect(), bImage);
                    break;
            }
        }

        if (isBackgroundVisible()) {
            create_BACKGROUND_Image(GAUGE_WIDTH, "", "", bImage);
        }

        if (compassRoseImage != null) {
            compassRoseImage.flush();
        }
        compassRoseImage = create_COMPASS_ROSE_Image(GAUGE_WIDTH);

        if (pointerShadowImage != null) {
            pointerShadowImage.flush();
        }
        pointerShadowImage = create_POINTER_SHADOW_Image(GAUGE_WIDTH);

        if (pointerImage != null) {
            pointerImage.flush();
        }
        pointerImage = create_POINTER_Image(GAUGE_WIDTH);

        createPostsImage(GAUGE_WIDTH, fImage, PostPosition.CENTER);

        if (isForegroundVisible()) {
            switch (getFrameType()) {
                case SQUARE:
                    FOREGROUND_FACTORY.createLinearForeground(GAUGE_WIDTH, GAUGE_WIDTH, false, bImage);
                    break;

                case ROUND:

                default:
                    FOREGROUND_FACTORY.createRadialForeground(GAUGE_WIDTH, false, getForegroundType(), fImage);
                    break;
            }
        }

        if (disabledImage != null) {
            disabledImage.flush();
        }
        disabledImage = create_DISABLED_Image(GAUGE_WIDTH);

        return this;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Visualization">
    @Override
    protected void paintComponent(java.awt.Graphics g) {
        if (!isInitialized()) {
            return;
        }

        final Graphics2D G2 = (Graphics2D) g.create();

        CENTER.setLocation(getGaugeBounds().getCenterX(), getGaugeBounds().getCenterX());

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Translate the coordinate system related to the insets
        G2.translate(getFramelessOffset().getX(), getFramelessOffset().getY());

        final AffineTransform OLD_TRANSFORM = G2.getTransform();

        // Draw combined background image
        G2.drawImage(bImage, 0, 0, null);

        // Draw compass rose
        G2.drawImage(compassRoseImage, 0, 0, null);

        // Draw the pointer
        G2.rotate((value - MIN_VALUE) * angleStep, CENTER.getX(), CENTER.getY() + 2);
        G2.drawImage(pointerShadowImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);
        G2.rotate((value - MIN_VALUE) * angleStep, CENTER.getX(), CENTER.getY());
        G2.drawImage(pointerImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw combined foreground image
        G2.drawImage(fImage, 0, 0, null);

        if (!isEnabled()) {
            G2.drawImage(disabledImage, 0, 0, null);
        }

        // Translate the coordinate system back to original
        G2.translate(-getInnerBounds().x, -getInnerBounds().y);

        G2.dispose();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    @Override
    public double getValue() {
        return value;
    }

    /**
     * Sets the direction of the needle in degrees (0 - 360°)
     * @param VALUE
     */
    @Override
    public void setValue(final double VALUE) {
        if (isEnabled()) {
            double oldValue = value;
            value = VALUE % 360;

            fireStateChanged();
            firePropertyChange(VALUE_PROPERTY, oldValue, value);
            repaint();
        }
    }

    @Override
    public void setValueAnimated(double newValue) {
        if (isEnabled()) {
            // Needle should always take the shortest way to it's new position
            if (360 - newValue + value < newValue - value) {
                newValue = 360 - newValue;
            }

            if (timeline.getState() == Timeline.TimelineState.PLAYING_FORWARD || timeline.getState() == Timeline.TimelineState.PLAYING_REVERSE) {
                timeline.abort();
            }
            timeline = new Timeline(this);
            timeline.addPropertyToInterpolate("value", value, newValue);
            timeline.setEase(EASE);

            timeline.setDuration((long) 250);
            timeline.play();
        }
    }

    @Override
    public double getMinValue() {
        return MIN_VALUE;
    }

    @Override
    public double getMaxValue() {
        return MAX_VALUE;
    }

    @Override
    public Point2D getCenter() {
        return new Point2D.Double(bImage.getWidth() / 2.0 + getInnerBounds().x, bImage.getHeight() / 2.0 + getInnerBounds().y);
    }

    @Override
    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Double(bImage.getMinX(), bImage.getMinY(), bImage.getWidth(), bImage.getHeight());
    }

    @Override
    public Rectangle getLcdBounds() {
        return new Rectangle();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image related">
    private BufferedImage create_BIG_ROSE_POINTER_Image(final int WIDTH) {
        final BufferedImage IMAGE = UTIL.createImage((int) (WIDTH * 0.0546875f), (int) (WIDTH * 0.2f), java.awt.Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        G2.setStroke(new BasicStroke(0.75f));

        // Define arrow shape of pointer
        final GeneralPath POINTER_WHITE_LEFT = new GeneralPath();
        final GeneralPath POINTER_WHITE_RIGHT = new GeneralPath();

        POINTER_WHITE_LEFT.moveTo(IMAGE_WIDTH - IMAGE_WIDTH * 0.95f, IMAGE_HEIGHT);
        POINTER_WHITE_LEFT.lineTo(IMAGE_WIDTH / 2.0f, 0);
        POINTER_WHITE_LEFT.lineTo(IMAGE_WIDTH / 2.0f, IMAGE_HEIGHT);
        POINTER_WHITE_LEFT.closePath();

        POINTER_WHITE_RIGHT.moveTo(IMAGE_WIDTH * 0.95f, IMAGE_HEIGHT);
        POINTER_WHITE_RIGHT.lineTo(IMAGE_WIDTH / 2.0f, 0);
        POINTER_WHITE_RIGHT.lineTo(IMAGE_WIDTH / 2.0f, IMAGE_HEIGHT);
        POINTER_WHITE_RIGHT.closePath();

        final Area POINTER_FRAME_WHITE = new Area(POINTER_WHITE_LEFT);
        POINTER_FRAME_WHITE.add(new Area(POINTER_WHITE_RIGHT));

        final Color STROKE_COLOR = getBackgroundColor().SYMBOL_COLOR.darker();
        final Color FILL_COLOR = getBackgroundColor().SYMBOL_COLOR;

        G2.setColor(STROKE_COLOR);
        G2.fill(POINTER_WHITE_RIGHT);
        G2.setColor(FILL_COLOR);
        G2.fill(POINTER_WHITE_LEFT);
        G2.setColor(STROKE_COLOR);
        G2.draw(POINTER_FRAME_WHITE);

        G2.dispose();

        return IMAGE;
    }

    private BufferedImage create_SMALL_ROSE_POINTER_Image(final int WIDTH) {
        final BufferedImage IMAGE = UTIL.createImage((int) (WIDTH * 0.0546875f), (int) (WIDTH * 0.2f), java.awt.Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();


        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        G2.setStroke(new BasicStroke(0.75f));

        // Define arrow shape of pointer
        final GeneralPath POINTER_WHITE_LEFT = new GeneralPath();
        final GeneralPath POINTER_WHITE_RIGHT = new GeneralPath();

        POINTER_WHITE_LEFT.moveTo(IMAGE_WIDTH - IMAGE_WIDTH * 0.75f, IMAGE_HEIGHT);
        POINTER_WHITE_LEFT.lineTo(IMAGE_WIDTH / 2.0f, IMAGE_HEIGHT / 2.0f);
        POINTER_WHITE_LEFT.lineTo(IMAGE_WIDTH / 2.0f, IMAGE_HEIGHT);
        POINTER_WHITE_LEFT.closePath();

        POINTER_WHITE_RIGHT.moveTo(IMAGE_WIDTH * 0.75f, IMAGE_HEIGHT);
        POINTER_WHITE_RIGHT.lineTo(IMAGE_WIDTH / 2.0f, IMAGE_HEIGHT / 2.0f);
        POINTER_WHITE_RIGHT.lineTo(IMAGE_WIDTH / 2.0f, IMAGE_HEIGHT);
        POINTER_WHITE_RIGHT.closePath();

        final Area POINTER_FRAME_WHITE = new Area(POINTER_WHITE_LEFT);
        POINTER_FRAME_WHITE.add(new Area(POINTER_WHITE_RIGHT));

        final Color STROKE_COLOR = getBackgroundColor().SYMBOL_COLOR.darker();
        final Color FILL_COLOR = getBackgroundColor().SYMBOL_COLOR;

        G2.setColor(FILL_COLOR);
        G2.fill(POINTER_FRAME_WHITE);
        G2.setColor(STROKE_COLOR);
        G2.draw(POINTER_FRAME_WHITE);

        G2.dispose();

        return IMAGE;
    }

    private BufferedImage create_COMPASS_ROSE_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, java.awt.Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        //G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        //final int IMAGE_HEIGHT = IMAGE.getHeight();

        // ******************* COMPASS ROSE *************************************************
        final Point2D COMPASS_CENTER = new Point2D.Double(IMAGE_WIDTH / 2.0f, IMAGE_WIDTH / 2.0f);
        AffineTransform transform = G2.getTransform();
        G2.setStroke(new BasicStroke(IMAGE_WIDTH * 0.01953125f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

        G2.setColor(getBackgroundColor().SYMBOL_COLOR);

        for (int i = 0; i <= 360; i += 30) {
            G2.draw(new Arc2D.Double(COMPASS_CENTER.getX() - IMAGE_WIDTH * 0.263671875f, COMPASS_CENTER.getY() - IMAGE_WIDTH * 0.263671875f, IMAGE_WIDTH * 0.52734375f, IMAGE_WIDTH * 0.52734375f, i, 15, Arc2D.OPEN));
        }

        G2.setColor(getBackgroundColor().SYMBOL_COLOR);
        G2.setStroke(new BasicStroke(0.5f));
        java.awt.Shape outerCircle = new Ellipse2D.Double(COMPASS_CENTER.getX() - IMAGE_WIDTH * 0.2734375f, COMPASS_CENTER.getY() - IMAGE_WIDTH * 0.2734375f, IMAGE_WIDTH * 0.546875f, IMAGE_WIDTH * 0.546875f);
        G2.draw(outerCircle);
        java.awt.Shape innerCircle = new Ellipse2D.Double(COMPASS_CENTER.getX() - IMAGE_WIDTH * 0.25390625f, COMPASS_CENTER.getY() - IMAGE_WIDTH * 0.25390625f, IMAGE_WIDTH * 0.5078125f, IMAGE_WIDTH * 0.5078125f);
        G2.draw(innerCircle);

        final java.awt.geom.Line2D LINE = new java.awt.geom.Line2D.Double(COMPASS_CENTER.getX(), IMAGE_WIDTH * 0.4018691589, COMPASS_CENTER.getX(), IMAGE_WIDTH * 0.1495327103);
        G2.setColor(getBackgroundColor().SYMBOL_COLOR);

        G2.setStroke(new BasicStroke(1f));
        G2.draw(LINE);
        G2.rotate(Math.PI / 12, COMPASS_CENTER.getX(), COMPASS_CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 6, COMPASS_CENTER.getX(), COMPASS_CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 6, COMPASS_CENTER.getX(), COMPASS_CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 12, COMPASS_CENTER.getX(), COMPASS_CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 12, COMPASS_CENTER.getX(), COMPASS_CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 6, COMPASS_CENTER.getX(), COMPASS_CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 6, COMPASS_CENTER.getX(), COMPASS_CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 12, COMPASS_CENTER.getX(), COMPASS_CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 12, COMPASS_CENTER.getX(), COMPASS_CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 6, COMPASS_CENTER.getX(), COMPASS_CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 6, COMPASS_CENTER.getX(), COMPASS_CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 12, COMPASS_CENTER.getX(), COMPASS_CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 12, COMPASS_CENTER.getX(), COMPASS_CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 6, COMPASS_CENTER.getX(), COMPASS_CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 6, COMPASS_CENTER.getX(), COMPASS_CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 12, COMPASS_CENTER.getX(), COMPASS_CENTER.getY());
        G2.draw(LINE);

        G2.setTransform(transform);
        final BufferedImage BIG_ROSE_POINTER = create_BIG_ROSE_POINTER_Image(IMAGE_WIDTH);
        final BufferedImage SMALL_ROSE_POINTER = create_SMALL_ROSE_POINTER_Image(IMAGE_WIDTH);
        final Point2D OFFSET = new Point2D.Double(IMAGE_WIDTH * 0.475f, IMAGE_WIDTH * 0.20f);

        G2.translate(OFFSET.getX(), OFFSET.getY());

        // N
        G2.drawImage(BIG_ROSE_POINTER, 0, 0, this);

        // NE
        G2.rotate(Math.PI / 4f, COMPASS_CENTER.getX() - OFFSET.getX(), COMPASS_CENTER.getY() - OFFSET.getY());
        G2.drawImage(SMALL_ROSE_POINTER, 0, 0, this);

        // E
        G2.rotate(Math.PI / 4f, COMPASS_CENTER.getX() - OFFSET.getX(), COMPASS_CENTER.getY() - OFFSET.getY());
        G2.drawImage(BIG_ROSE_POINTER, 0, 0, this);

        // SE
        G2.rotate(Math.PI / 4f, COMPASS_CENTER.getX() - OFFSET.getX(), COMPASS_CENTER.getY() - OFFSET.getY());
        G2.drawImage(SMALL_ROSE_POINTER, 0, 0, this);

        // S
        G2.rotate(Math.PI / 4f, COMPASS_CENTER.getX() - OFFSET.getX(), COMPASS_CENTER.getY() - OFFSET.getY());
        G2.drawImage(BIG_ROSE_POINTER, 0, 0, this);

        // SW
        G2.rotate(Math.PI / 4f, COMPASS_CENTER.getX() - OFFSET.getX(), COMPASS_CENTER.getY() - OFFSET.getY());
        G2.drawImage(SMALL_ROSE_POINTER, 0, 0, this);


        // W
        G2.rotate(Math.PI / 4f, COMPASS_CENTER.getX() - OFFSET.getX(), COMPASS_CENTER.getY() - OFFSET.getY());
        G2.drawImage(BIG_ROSE_POINTER, 0, 0, this);

        // NW
        G2.rotate(Math.PI / 4f, COMPASS_CENTER.getX() - OFFSET.getX(), COMPASS_CENTER.getY() - OFFSET.getY());
        G2.drawImage(SMALL_ROSE_POINTER, 0, 0, this);

        G2.setTransform(transform);

        G2.setColor(getBackgroundColor().SYMBOL_COLOR);
        G2.setStroke(new BasicStroke(IMAGE_WIDTH * 0.00953125f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        G2.draw(new Ellipse2D.Double(COMPASS_CENTER.getX() - (IMAGE_WIDTH * 0.1025f), COMPASS_CENTER.getY() - (IMAGE_WIDTH * 0.1025f), IMAGE_WIDTH * 0.205f, IMAGE_WIDTH * 0.205f));

        G2.setStroke(new BasicStroke(0.5f));
        G2.setColor(getBackgroundColor().SYMBOL_COLOR.darker());
        final java.awt.Shape OUTER_ROSE_ELLIPSE = new Ellipse2D.Double(COMPASS_CENTER.getX() - (IMAGE_WIDTH * 0.11f), COMPASS_CENTER.getY() - (IMAGE_WIDTH * 0.11f), IMAGE_WIDTH * 0.22f, IMAGE_WIDTH * 0.22f);
        G2.draw(OUTER_ROSE_ELLIPSE);
        final java.awt.Shape INNER_ROSE_ELLIPSE = new Ellipse2D.Double(COMPASS_CENTER.getX() - (IMAGE_WIDTH * 0.095f), COMPASS_CENTER.getY() - (IMAGE_WIDTH * 0.095f), IMAGE_WIDTH * 0.19f, IMAGE_WIDTH * 0.19f);
        G2.draw(INNER_ROSE_ELLIPSE);


        // ******************* TICKMARKS ****************************************************
        create_TICKMARKS(G2, IMAGE_WIDTH);

        G2.dispose();

        return IMAGE;
    }

    private void create_TICKMARKS(final Graphics2D G2, final int IMAGE_WIDTH) {
        // Store former transformation
        final AffineTransform FORMER_TRANSFORM = G2.getTransform();

        final BasicStroke MEDIUM_STROKE = new BasicStroke(0.005859375f * IMAGE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        final BasicStroke THIN_STROKE = new BasicStroke(0.00390625f * IMAGE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        final java.awt.Font BIG_FONT = new java.awt.Font("Serif", java.awt.Font.PLAIN, (int) (0.12f * IMAGE_WIDTH));
        final java.awt.Font SMALL_FONT = new java.awt.Font("Serif", java.awt.Font.PLAIN, (int) (0.06f * IMAGE_WIDTH));
        final float TEXT_DISTANCE = 0.0750f * IMAGE_WIDTH;
        final float MIN_LENGTH = 0.015625f * IMAGE_WIDTH;
        final float MED_LENGTH = 0.0234375f * IMAGE_WIDTH;
        final float MAX_LENGTH = 0.03125f * IMAGE_WIDTH;

        final Color TEXT_COLOR = getBackgroundColor().LABEL_COLOR;
        final Color TICK_COLOR = getBackgroundColor().LABEL_COLOR;

        // Create the watch itself
        final float RADIUS = IMAGE_WIDTH * 0.38f;
        final Point2D COMPASS_CENTER = new Point2D.Double(IMAGE_WIDTH / 2.0f, IMAGE_WIDTH / 2.0f);

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // Draw ticks
        Point2D innerPoint;
        Point2D outerPoint;
        Point2D textPoint = null;
        java.awt.geom.Line2D tick;
        int tickCounter90 = 0;
        int tickCounter15 = 0;
        int tickCounter5 = 0;
        int counter = 0;

        double sinValue = 0;
        double cosValue = 0;

        final double STEP = (2.0d * Math.PI) / (360.0d);

        for (double alpha = 2 * Math.PI; alpha >= 0; alpha -= STEP) {
            G2.setStroke(THIN_STROKE);
            sinValue = Math.sin(alpha);
            cosValue = Math.cos(alpha);

            G2.setColor(TICK_COLOR);

            if (tickCounter5 == 5) {
                G2.setStroke(THIN_STROKE);
                innerPoint = new Point2D.Double(COMPASS_CENTER.getX() + (RADIUS - MIN_LENGTH) * sinValue, COMPASS_CENTER.getY() + (RADIUS - MIN_LENGTH) * cosValue);
                outerPoint = new Point2D.Double(COMPASS_CENTER.getX() + RADIUS * sinValue, COMPASS_CENTER.getY() + RADIUS * cosValue);
                // Draw ticks
                tick = new java.awt.geom.Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);

                tickCounter5 = 0;
            }

            // Different tickmark every 15 units
            if (tickCounter15 == 15) {
                G2.setStroke(THIN_STROKE);
                innerPoint = new Point2D.Double(COMPASS_CENTER.getX() + (RADIUS - MED_LENGTH) * sinValue, COMPASS_CENTER.getY() + (RADIUS - MED_LENGTH) * cosValue);
                outerPoint = new Point2D.Double(COMPASS_CENTER.getX() + RADIUS * sinValue, COMPASS_CENTER.getY() + RADIUS * cosValue);

                // Draw ticks
                tick = new java.awt.geom.Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);

                tickCounter15 = 0;
                tickCounter90 += 15;
            }

            // Different tickmark every 90 units plus text
            if (tickCounter90 == 90) {
                G2.setStroke(MEDIUM_STROKE);
                innerPoint = new Point2D.Double(COMPASS_CENTER.getX() + (RADIUS - MAX_LENGTH) * sinValue, COMPASS_CENTER.getY() + (RADIUS - MAX_LENGTH) * cosValue);
                outerPoint = new Point2D.Double(COMPASS_CENTER.getX() + RADIUS * sinValue, COMPASS_CENTER.getY() + RADIUS * cosValue);

                // Draw ticks
                tick = new java.awt.geom.Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);

                tickCounter90 = 0;
            }

            // Draw text
            G2.setFont(BIG_FONT);
            G2.setColor(TEXT_COLOR);

            textPoint = new Point2D.Double(COMPASS_CENTER.getX() + (RADIUS - TEXT_DISTANCE) * sinValue, COMPASS_CENTER.getY() + (RADIUS - TEXT_DISTANCE) * cosValue);
            switch (counter) {
                case 360:
                    G2.setFont(BIG_FONT);
                    G2.fill(UTIL.rotateTextAroundCenter(G2, "S", (int) textPoint.getX(), (int) textPoint.getY(), (Math.PI - alpha)));
                    break;
                case 45:
                    G2.setFont(SMALL_FONT);
                    G2.fill(UTIL.rotateTextAroundCenter(G2, "SW", (int) textPoint.getX(), (int) textPoint.getY(), (Math.PI - alpha)));
                    break;
                case 90:
                    G2.setFont(BIG_FONT);
                    G2.fill(UTIL.rotateTextAroundCenter(G2, "W", (int) textPoint.getX(), (int) textPoint.getY(), (Math.PI - alpha)));
                    break;
                case 135:
                    G2.setFont(SMALL_FONT);
                    G2.fill(UTIL.rotateTextAroundCenter(G2, "NW", (int) textPoint.getX(), (int) textPoint.getY(), (Math.PI - alpha)));
                    break;
                case 180:
                    G2.setFont(BIG_FONT);
                    G2.fill(UTIL.rotateTextAroundCenter(G2, "N", (int) textPoint.getX(), (int) textPoint.getY(), (Math.PI - alpha)));
                    break;
                case 225:
                    G2.setFont(SMALL_FONT);
                    G2.fill(UTIL.rotateTextAroundCenter(G2, "NE", (int) textPoint.getX(), (int) textPoint.getY(), (Math.PI - alpha)));
                    break;
                case 270:
                    G2.setFont(BIG_FONT);
                    G2.fill(UTIL.rotateTextAroundCenter(G2, "E", (int) textPoint.getX(), (int) textPoint.getY(), (Math.PI - alpha)));
                    break;
                case 315:
                    G2.setFont(SMALL_FONT);
                    G2.fill(UTIL.rotateTextAroundCenter(G2, "SE", (int) textPoint.getX(), (int) textPoint.getY(), (Math.PI - alpha)));
                    break;
            }
            G2.setTransform(FORMER_TRANSFORM);

            tickCounter5++;
            tickCounter15++;

            counter++;
        }

        // Restore former transformation
        G2.setTransform(FORMER_TRANSFORM);
    }

    @Override
    protected BufferedImage create_POINTER_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, java.awt.Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        switch (getPointerType()) {
            case TYPE2:
                final GeneralPath NORTHPOINTER2 = new GeneralPath();
                NORTHPOINTER2.setWindingRule(Path2D.WIND_EVEN_ODD);
                NORTHPOINTER2.moveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.4532710280373832);
                NORTHPOINTER2.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.4532710280373832, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382);
                NORTHPOINTER2.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.4532710280373832, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.4532710280373832);
                NORTHPOINTER2.curveTo(IMAGE_WIDTH * 0.4532710280373832, IMAGE_HEIGHT * 0.46261682242990654, IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.5);
                NORTHPOINTER2.curveTo(IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.5);
                NORTHPOINTER2.curveTo(IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5467289719626168, IMAGE_HEIGHT * 0.46261682242990654, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.4532710280373832);
                NORTHPOINTER2.closePath();
                final Point2D NORTHPOINTER2_START = new Point2D.Double(NORTHPOINTER2.getBounds2D().getMinX(), 0);
                final Point2D NORTHPOINTER2_STOP = new Point2D.Double(NORTHPOINTER2.getBounds2D().getMaxX(), 0);
                final float[] NORTHPOINTER2_FRACTIONS = {
                    0.0f,
                    0.4999f,
                    0.5f,
                    1.0f
                };
                final Color[] NORTHPOINTER2_COLORS = {
                    getPointerColor().LIGHT,
                    getPointerColor().LIGHT,
                    getPointerColor().MEDIUM,
                    getPointerColor().MEDIUM
                };
                final java.awt.LinearGradientPaint NORTHPOINTER2_GRADIENT = new java.awt.LinearGradientPaint(NORTHPOINTER2_START, NORTHPOINTER2_STOP, NORTHPOINTER2_FRACTIONS, NORTHPOINTER2_COLORS);
                G2.setPaint(NORTHPOINTER2_GRADIENT);
                G2.fill(NORTHPOINTER2);
                G2.setColor(getPointerColor().DARK);
                G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                G2.draw(NORTHPOINTER2);

                final GeneralPath SOUTHPOINTER2 = new GeneralPath();
                SOUTHPOINTER2.setWindingRule(Path2D.WIND_EVEN_ODD);
                SOUTHPOINTER2.moveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5467289719626168);
                SOUTHPOINTER2.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5467289719626168, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.8504672897196262, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.8504672897196262);
                SOUTHPOINTER2.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.8504672897196262, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5467289719626168, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5467289719626168);
                SOUTHPOINTER2.curveTo(IMAGE_WIDTH * 0.5467289719626168, IMAGE_HEIGHT * 0.5373831775700935, IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.5);
                SOUTHPOINTER2.curveTo(IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.5);
                SOUTHPOINTER2.curveTo(IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.4532710280373832, IMAGE_HEIGHT * 0.5373831775700935, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5467289719626168);
                SOUTHPOINTER2.closePath();
                final Point2D SOUTHPOINTER2_START = new Point2D.Double(SOUTHPOINTER2.getBounds2D().getMinX(), 0);
                final Point2D SOUTHPOINTER2_STOP = new Point2D.Double(SOUTHPOINTER2.getBounds2D().getMaxX(), 0);
                final float[] SOUTHPOINTER2_FRACTIONS = {
                    0.0f,
                    0.48f,
                    0.48009998f,
                    1.0f
                };
                final Color[] SOUTHPOINTER2_COLORS = {
                    new Color(227, 229, 232, 255),
                    new Color(227, 229, 232, 255),
                    new Color(171, 177, 184, 255),
                    new Color(171, 177, 184, 255)
                };
                final java.awt.LinearGradientPaint SOUTHPOINTER2_GRADIENT = new java.awt.LinearGradientPaint(SOUTHPOINTER2_START, SOUTHPOINTER2_STOP, SOUTHPOINTER2_FRACTIONS, SOUTHPOINTER2_COLORS);
                G2.setPaint(SOUTHPOINTER2_GRADIENT);
                G2.fill(SOUTHPOINTER2);
                G2.setColor(new Color(0xABB1B8));
                G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                G2.draw(SOUTHPOINTER2);
                break;

            case TYPE3:
                final GeneralPath NORTHPOINTER3 = new GeneralPath();
                NORTHPOINTER3.setWindingRule(Path2D.WIND_EVEN_ODD);
                NORTHPOINTER3.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382);
                NORTHPOINTER3.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382, IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.5);
                NORTHPOINTER3.curveTo(IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5560747663551402, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5560747663551402);
                NORTHPOINTER3.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5560747663551402, IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.5);
                NORTHPOINTER3.curveTo(IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382);
                NORTHPOINTER3.closePath();
                final Point2D NORTHPOINTER3_START = new Point2D.Double(NORTHPOINTER3.getBounds2D().getMinX(), 0);
                final Point2D NORTHPOINTER3_STOP = new Point2D.Double(NORTHPOINTER3.getBounds2D().getMaxX(), 0);
                final float[] NORTHPOINTER3_FRACTIONS = {
                    0.0f,
                    0.4999f,
                    0.5f,
                    1.0f
                };
                final Color[] NORTHPOINTER3_COLORS = {
                    getPointerColor().LIGHT,
                    getPointerColor().LIGHT,
                    getPointerColor().MEDIUM,
                    getPointerColor().MEDIUM
                };
                final java.awt.LinearGradientPaint NORTHPOINTER3_GRADIENT = new java.awt.LinearGradientPaint(NORTHPOINTER3_START, NORTHPOINTER3_STOP, NORTHPOINTER3_FRACTIONS, NORTHPOINTER3_COLORS);
                G2.setPaint(NORTHPOINTER3_GRADIENT);
                G2.fill(NORTHPOINTER3);
                break;

            case TYPE1:

            default:
                final GeneralPath NORTHPOINTER1 = new GeneralPath();
                NORTHPOINTER1.setWindingRule(Path2D.WIND_EVEN_ODD);
                NORTHPOINTER1.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.4953271028037383);
                NORTHPOINTER1.lineTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.4953271028037383);
                NORTHPOINTER1.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382);
                NORTHPOINTER1.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.4953271028037383);
                NORTHPOINTER1.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.4953271028037383);
                NORTHPOINTER1.closePath();

                final Point2D NORTHPOINTER1_START = new Point2D.Double(NORTHPOINTER1.getBounds2D().getMinX(), 0);
                final Point2D NORTHPOINTER1_STOP = new Point2D.Double(NORTHPOINTER1.getBounds2D().getMaxX(), 0);
                final float[] NORTHPOINTER1_FRACTIONS = {
                    0.0f,
                    0.4999f,
                    0.5f,
                    1.0f
                };
                final Color[] NORTHPOINTER1_COLORS = {
                    getPointerColor().LIGHT,
                    getPointerColor().LIGHT,
                    getPointerColor().MEDIUM,
                    getPointerColor().MEDIUM
                };
                final java.awt.LinearGradientPaint NORTHPOINTER1_GRADIENT = new java.awt.LinearGradientPaint(NORTHPOINTER1_START, NORTHPOINTER1_STOP, NORTHPOINTER1_FRACTIONS, NORTHPOINTER1_COLORS);
                G2.setPaint(NORTHPOINTER1_GRADIENT);
                G2.fill(NORTHPOINTER1);
                G2.setColor(getPointerColor().DARK);
                G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                G2.draw(NORTHPOINTER1);

                final GeneralPath SOUTHPOINTER1 = new GeneralPath();
                SOUTHPOINTER1.setWindingRule(Path2D.WIND_EVEN_ODD);
                SOUTHPOINTER1.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5046728971962616);
                SOUTHPOINTER1.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5046728971962616);
                SOUTHPOINTER1.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.8504672897196262);
                SOUTHPOINTER1.lineTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.5046728971962616);
                SOUTHPOINTER1.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5046728971962616);
                SOUTHPOINTER1.closePath();

                final Point2D SOUTHPOINTER1_START = new Point2D.Double(SOUTHPOINTER1.getBounds2D().getMinX(), 0);
                final Point2D SOUTHPOINTER1_STOP = new Point2D.Double(SOUTHPOINTER1.getBounds2D().getMaxX(), 0);
                final float[] SOUTHPOINTER1_FRACTIONS = {
                    0.0f,
                    0.4999f,
                    0.5f,
                    1.0f
                };
                final Color[] SOUTHPOINTER1_COLORS = {
                    new Color(227, 229, 232, 255),
                    new Color(227, 229, 232, 255),
                    new Color(171, 177, 184, 255),
                    new Color(171, 177, 184, 255)
                };
                final java.awt.LinearGradientPaint SOUTHPOINTER1_GRADIENT = new java.awt.LinearGradientPaint(SOUTHPOINTER1_START, SOUTHPOINTER1_STOP, SOUTHPOINTER1_FRACTIONS, SOUTHPOINTER1_COLORS);
                G2.setPaint(SOUTHPOINTER1_GRADIENT);
                G2.fill(SOUTHPOINTER1);
                final Color STROKE_COLOR_SOUTHPOINTER1 = new Color(0xABB1B8);
                G2.setColor(STROKE_COLOR_SOUTHPOINTER1);
                G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                G2.draw(SOUTHPOINTER1);
                break;
        }

        G2.dispose();

        return IMAGE;
    }

    @Override
    protected BufferedImage create_POINTER_SHADOW_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, (int) (1.0 * WIDTH), java.awt.Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final Color SHADOW_COLOR = new Color(0.0f, 0.0f, 0.0f, 0.65f);
        switch (getPointerType()) {
            case TYPE2:
                final GeneralPath NORTHPOINTER2 = new GeneralPath();
                NORTHPOINTER2.setWindingRule(Path2D.WIND_EVEN_ODD);
                NORTHPOINTER2.moveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.4532710280373832);
                NORTHPOINTER2.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.4532710280373832, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382);
                NORTHPOINTER2.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.4532710280373832, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.4532710280373832);
                NORTHPOINTER2.curveTo(IMAGE_WIDTH * 0.4532710280373832, IMAGE_HEIGHT * 0.46261682242990654, IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.5);
                NORTHPOINTER2.curveTo(IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.5);
                NORTHPOINTER2.curveTo(IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5467289719626168, IMAGE_HEIGHT * 0.46261682242990654, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.4532710280373832);
                NORTHPOINTER2.closePath();

                final GeneralPath SOUTHPOINTER2 = new GeneralPath();
                SOUTHPOINTER2.setWindingRule(Path2D.WIND_EVEN_ODD);
                SOUTHPOINTER2.moveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5467289719626168);
                SOUTHPOINTER2.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5467289719626168, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.8504672897196262, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.8504672897196262);
                SOUTHPOINTER2.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.8504672897196262, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5467289719626168, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5467289719626168);
                SOUTHPOINTER2.curveTo(IMAGE_WIDTH * 0.5467289719626168, IMAGE_HEIGHT * 0.5373831775700935, IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.5);
                SOUTHPOINTER2.curveTo(IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.5);
                SOUTHPOINTER2.curveTo(IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.4532710280373832, IMAGE_HEIGHT * 0.5373831775700935, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5467289719626168);
                SOUTHPOINTER2.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(NORTHPOINTER2);
                G2.fill(SOUTHPOINTER2);
                break;

            case TYPE3:
                final GeneralPath NORTHPOINTER3 = new GeneralPath();
                NORTHPOINTER3.setWindingRule(Path2D.WIND_EVEN_ODD);
                NORTHPOINTER3.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382);
                NORTHPOINTER3.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382, IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.5);
                NORTHPOINTER3.curveTo(IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5560747663551402, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5560747663551402);
                NORTHPOINTER3.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5560747663551402, IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.5);
                NORTHPOINTER3.curveTo(IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382);
                NORTHPOINTER3.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(NORTHPOINTER3);
                break;

            case TYPE1:

            default:
                final GeneralPath NORTHPOINTER1 = new GeneralPath();
                NORTHPOINTER1.setWindingRule(Path2D.WIND_EVEN_ODD);
                NORTHPOINTER1.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
                NORTHPOINTER1.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.40186915887850466);
                NORTHPOINTER1.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.38317757009345793, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271);
                NORTHPOINTER1.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.38317757009345793, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.397196261682243);
                NORTHPOINTER1.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299);
                NORTHPOINTER1.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
                NORTHPOINTER1.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5327102803738317);
                NORTHPOINTER1.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
                NORTHPOINTER1.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
                NORTHPOINTER1.closePath();

                final GeneralPath SOUTHPOINTER1 = new GeneralPath();
                SOUTHPOINTER1.setWindingRule(Path2D.WIND_EVEN_ODD);
                SOUTHPOINTER1.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5280373831775701);
                SOUTHPOINTER1.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.5420560747663551, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5841121495327103, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.602803738317757);
                SOUTHPOINTER1.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.616822429906542, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.8691588785046729, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.8691588785046729);
                SOUTHPOINTER1.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.8691588785046729, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.616822429906542, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.602803738317757);
                SOUTHPOINTER1.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5841121495327103, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.5420560747663551, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5280373831775701);
                SOUTHPOINTER1.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5093457943925234, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
                SOUTHPOINTER1.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.4672897196261682);
                SOUTHPOINTER1.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
                SOUTHPOINTER1.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5093457943925234, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5280373831775701);
                SOUTHPOINTER1.closePath();

                G2.setColor(SHADOW_COLOR);
                G2.fill(NORTHPOINTER1);
                G2.fill(SOUTHPOINTER1);
                break;
        }

        G2.dispose();

        return IMAGE;
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "Compass";
    }
}
