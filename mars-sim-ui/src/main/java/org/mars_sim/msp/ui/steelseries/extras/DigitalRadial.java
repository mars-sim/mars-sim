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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.mars_sim.msp.ui.steelseries.gauges.AbstractGauge;
import org.mars_sim.msp.ui.steelseries.gauges.AbstractRadial;
import org.mars_sim.msp.ui.steelseries.tools.LcdColor;
import org.mars_sim.msp.ui.steelseries.tools.PostPosition;
import org.mars_sim.msp.ui.steelseries.tools.Section;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.ease.Spline;

/**
 *
 * @author hansolo
 */
public final class DigitalRadial extends AbstractRadial {
    // <editor-fold defaultstate="collapsed" desc="Variable declaration">
    private double value100 = 0;
    private double oldValue = 0;
    private double value1000 = 0;
    private double value10000 = 0;
    private double angleStep100ft;
    private double angleStep1000ft;
    private double angleStep10000ft;
    private final static double TICKMARK_OFFSET = Math.PI;
    private float tickLabelPeriod = 1f; // Draw value at every 10th tickmark
    private final Point2D CENTER = new Point2D.Double();
    // Images used to combine layers for background and foreground
    private BufferedImage bImage;
    private BufferedImage fImage;
    private BufferedImage pointer10000FtImage;
    private BufferedImage pointer1000FtImage;
    private BufferedImage pointer100FtImage;
    private BufferedImage disabledImage;
    private final Rectangle2D LCD = new Rectangle2D.Double();
    private Timeline timeline = new Timeline(this);
    private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
    private TextLayout unitLayout;
    private final Rectangle2D UNIT_BOUNDARY = new Rectangle2D.Double();
    private double unitStringWidth;
    private TextLayout valueLayout;
    private final Rectangle2D VALUE_BOUNDARY = new Rectangle2D.Double();
    private TextLayout infoLayout;
    private final Rectangle2D INFO_BOUNDARY = new Rectangle2D.Double();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public DigitalRadial() {
        super();
        init(getInnerBounds().width, getInnerBounds().height);
        setMinValue(0);
        setMaxValue(10);
        calcAngleStep();
        setLcdColor(LcdColor.BLACK_LCD);
        setLcdVisible(true);
        setTitle("Elevation");
        setUnitString("km");
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

        if (isDigitalFont()) {
            setLcdValueFont(getModel().getDigitalBaseFont().deriveFont(0.7f * GAUGE_WIDTH * 0.10f));
        } else {
            setLcdValueFont(getModel().getStandardBaseFont().deriveFont(0.625f * GAUGE_WIDTH * 0.10f));
        }

        if (isCustomLcdUnitFontEnabled()) {
            setLcdUnitFont(getCustomLcdUnitFont().deriveFont(0.25f * GAUGE_WIDTH * 0.10f));
        } else {
            setLcdUnitFont(getModel().getStandardBaseFont().deriveFont(0.25f * GAUGE_WIDTH * 0.10f));
        }

        setLcdInfoFont(getModel().getStandardInfoFont().deriveFont(0.15f * GAUGE_WIDTH * 0.10f));

        // Create Background Image
        if (bImage != null) {
            bImage.flush();
        }
        bImage = UTIL.createImage(GAUGE_WIDTH, GAUGE_WIDTH, Transparency.TRANSLUCENT);

        // Create Foreground Image
        if (fImage != null) {
            fImage.flush();
        }
        fImage = UTIL.createImage(GAUGE_WIDTH, GAUGE_WIDTH, Transparency.TRANSLUCENT);

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

        create_TICKMARKS_Image(GAUGE_WIDTH, 0, TICKMARK_OFFSET, 0, 10, angleStep100ft, (int) tickLabelPeriod, 0, true, true, null, bImage);

        if (isLcdVisible()) {
            createLcdImage(new Rectangle2D.Double(((getGaugeBounds().width - GAUGE_WIDTH * 0.4) / 2.0), (getGaugeBounds().height * 0.55), (GAUGE_WIDTH * 0.4), (GAUGE_WIDTH * 0.1)), getLcdColor(), getCustomLcdBackground(), bImage);
            LCD.setRect(((getGaugeBounds().width - GAUGE_WIDTH * 0.4) / 2.0), (getGaugeBounds().height * 0.55), GAUGE_WIDTH * 0.4, GAUGE_WIDTH * 0.1);
        }

        if (pointer100FtImage != null) {
            pointer100FtImage.flush();
        }
        pointer100FtImage = create_100FT_POINTER_Image(GAUGE_WIDTH);

        if (pointer1000FtImage != null) {
            pointer1000FtImage.flush();
        }
        pointer1000FtImage = create_1000FT_POINTER_Image(GAUGE_WIDTH);

        if (pointer10000FtImage != null) {
            pointer10000FtImage.flush();
        }
        pointer10000FtImage = create_10000FT_POINTER_Image(GAUGE_WIDTH);


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
    protected void paintComponent(Graphics g) {
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

        // Draw LCD display
        if (isLcdVisible()) {
            if (getLcdColor() == LcdColor.CUSTOM) {
                G2.setColor(getCustomLcdForeground());
            } else {
                G2.setColor(getLcdColor().TEXT_COLOR);
            }
            G2.setFont(getLcdUnitFont());
            if (isLcdUnitStringVisible()) {
                unitLayout = new TextLayout(getLcdUnitString(), G2.getFont(), RENDER_CONTEXT);
                UNIT_BOUNDARY.setFrame(unitLayout.getBounds());
                G2.drawString(getLcdUnitString(), (float) (LCD.getX() + (LCD.getWidth() - UNIT_BOUNDARY.getWidth()) - LCD.getWidth() * 0.03), (float) (LCD.getY() + LCD.getHeight() * 0.76));
                unitStringWidth = UNIT_BOUNDARY.getWidth();
            } else {
                unitStringWidth = 0;
            }
            G2.setFont(getLcdValueFont());
            switch (getModel().getNumberSystem()) {
                case HEX:
                    valueLayout = new TextLayout(Integer.toHexString((int) getLcdValue()).toUpperCase(), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(Integer.toHexString((int) getLcdValue()).toUpperCase(), (float) (LCD.getX() + (LCD.getWidth() - unitStringWidth - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09), (float) (LCD.getY() + LCD.getHeight() * 0.76));
                    break;

                case OCT:
                    valueLayout = new TextLayout(Integer.toOctalString((int) getLcdValue()), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(Integer.toOctalString((int) getLcdValue()), (float) (LCD.getX() + (LCD.getWidth() - unitStringWidth - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09), (float) (LCD.getY() + LCD.getHeight() * 0.76));
                    break;

                case DEC:

                default:
                    valueLayout = new TextLayout(formatLcdValue(getLcdValue()), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(formatLcdValue(getLcdValue()), (float) (LCD.getX() + (LCD.getWidth() - unitStringWidth - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09), (float) (LCD.getY() + LCD.getHeight() * 0.76));
                    break;
            }
            // Draw lcd info string
            if (!getLcdInfoString().isEmpty()) {
                G2.setFont(getLcdInfoFont());
                infoLayout = new TextLayout(getLcdInfoString(), G2.getFont(), RENDER_CONTEXT);
                INFO_BOUNDARY.setFrame(infoLayout.getBounds());
                G2.drawString(getLcdInfoString(), LCD.getBounds().x + 5f, LCD.getBounds().y + (float) INFO_BOUNDARY.getHeight() + 5f);
            }
        }

        // Draw the 10000ft pointer
        //final double ANGLE10000FT = ((value10000 - getMinValue()) * angleStep10000ft);
        G2.rotate(((value10000 - getMinValue()) * angleStep10000ft), CENTER.getX(), CENTER.getY());
        G2.drawImage(pointer10000FtImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw the 1000ft pointer
        //final double ANGLE1000FT = ((value1000 - getMinValue()) * angleStep1000ft);
        G2.rotate(((value1000 - getMinValue()) * angleStep1000ft), CENTER.getX(), CENTER.getY());
        G2.drawImage(pointer1000FtImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw the 100ft pointer
        //final double ANGLE100FT = ((value100 - getMinValue()) * angleStep100ft);
        G2.rotate(((value100 - getMinValue()) * angleStep100ft), CENTER.getX(), CENTER.getY());
        G2.drawImage(pointer100FtImage, 0, 0, null);
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
    /**
     * Sets the current height in feet
     * @param VALUE
     */
    @Override
    public void setValue(final double VALUE) {
        if (isEnabled()) {
            this.value100 = (VALUE % 1000) / 100;
            this.value1000 = (VALUE % 10000) / 100;
            this.value10000 = (VALUE % 100000) / 100;

            if (isValueCoupled()) {
                setLcdValue(VALUE);
            }

            fireStateChanged();
            this.oldValue = VALUE;
            repaint();
        }
    }

    @Override
    public void setValueAnimated(final double VALUE) {
        if (isEnabled()) {
            if (timeline.getState() == Timeline.TimelineState.PLAYING_FORWARD || timeline.getState() == Timeline.TimelineState.PLAYING_REVERSE) {
                timeline.abort();
            }
            timeline = new Timeline(this);
            timeline.addPropertyToInterpolate("value", this.oldValue, VALUE);
            timeline.setEase(new Spline(0.5f));
            double range = Math.abs(this.value100 - VALUE);
            double fraction = range / 1000;

            timeline.setDuration((long) (1000 * fraction));
            timeline.play();
        }
    }

    @Override
    public Paint createCustomLcdBackgroundPaint(final Color[] LCD_COLORS) {
        final Point2D FOREGROUND_START = new Point2D.Double(0.0, LCD.getMinY() + 1.0);
        final Point2D FOREGROUND_STOP = new Point2D.Double(0.0, LCD.getMaxY() - 1);
        if (FOREGROUND_START.equals(FOREGROUND_STOP)) {
            FOREGROUND_STOP.setLocation(0.0, FOREGROUND_START.getY() + 1);
        }

        final float[] FOREGROUND_FRACTIONS = {
            0.0f,
            0.03f,
            0.49f,
            0.5f,
            1.0f
        };

        final Color[] FOREGROUND_COLORS = {
            LCD_COLORS[0],
            LCD_COLORS[1],
            LCD_COLORS[2],
            LCD_COLORS[3],
            LCD_COLORS[4]
        };

        return new LinearGradientPaint(FOREGROUND_START, FOREGROUND_STOP, FOREGROUND_FRACTIONS, FOREGROUND_COLORS);
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
        return LCD.getBounds();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Misc">
    private void calcAngleStep() {
        this.angleStep100ft = (2 * Math.PI) / (getMaxValue() - getMinValue());
        this.angleStep1000ft = angleStep100ft / 10.0;
        this.angleStep10000ft = angleStep1000ft / 10.0;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image related">
    private BufferedImage create_TICKMARKS_Image(final int WIDTH, final double FREE_AREA_ANGLE,
                                                                final double OFFSET, final double MIN_VALUE,
                                                                final double MAX_VALUE, final double ANGLE_STEP,
                                                                final int TICK_LABEL_PERIOD,
                                                                final int SCALE_DIVIDER_POWER, final boolean DRAW_TICKS,
                                                                final boolean DRAW_TICK_LABELS,
                                                                java.util.ArrayList<Section> tickmarkSections,
                                                                BufferedImage image) {
        if (WIDTH <= 0) {
            return null;
        }
        if (image == null) {
            image = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        }
        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        //G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        final Font STD_FONT = new Font("Verdana", 0, (int) (0.09 * WIDTH));
        final BasicStroke MEDIUM_STROKE = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        final BasicStroke THIN_STROKE = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        final int TEXT_DISTANCE = (int) (0.17 * WIDTH);
        final int MED_LENGTH = (int) (0.05 * WIDTH);
        final int MAX_LENGTH = (int) (0.07 * WIDTH);

        final Color TEXT_COLOR = super.getBackgroundColor().LABEL_COLOR;
        final Color TICK_COLOR = super.getBackgroundColor().LABEL_COLOR;

        // Create the ticks itself
        final float RADIUS = IMAGE_WIDTH * 0.4f;
        final Point2D ALTIMETER_CENTER = new Point2D.Double(IMAGE_WIDTH / 2.0f, IMAGE_HEIGHT / 2.0f);

        // Draw ticks
        Point2D innerPoint;
        Point2D outerPoint;
        Point2D textPoint = null;
        Line2D tick;
        int counter = 0;
        int tickCounter = 0;

        G2.setFont(STD_FONT);

        double sinValue = 0;
        double cosValue = 0;

        double alpha; // angle for the tickmarks
        final double ALPHA_START = -OFFSET - (FREE_AREA_ANGLE / 2.0);
        float valueCounter; // value for the tickmarks

        for (alpha = ALPHA_START, valueCounter = 0; valueCounter <= 10; alpha -= ANGLE_STEP * 0.1, valueCounter += 0.1) {
            G2.setStroke(THIN_STROKE);
            sinValue = Math.sin(alpha);
            cosValue = Math.cos(alpha);

            // tickmark every 2 units
            if (counter % 2 == 0) {
                G2.setStroke(THIN_STROKE);
                innerPoint = new Point2D.Double(ALTIMETER_CENTER.getX() + (RADIUS - MED_LENGTH) * sinValue, ALTIMETER_CENTER.getY() + (RADIUS - MED_LENGTH) * cosValue);
                outerPoint = new Point2D.Double(ALTIMETER_CENTER.getX() + RADIUS * sinValue, ALTIMETER_CENTER.getY() + RADIUS * cosValue);
                // Draw ticks
                G2.setColor(TICK_COLOR);
                tick = new Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);
            }

            // Different tickmark every 10 units
            if (counter == 10 || counter == 0) {
                G2.setColor(TEXT_COLOR);
                G2.setStroke(MEDIUM_STROKE);
                innerPoint = new Point2D.Double(ALTIMETER_CENTER.getX() + (RADIUS - MAX_LENGTH) * sinValue, ALTIMETER_CENTER.getY() + (RADIUS - MAX_LENGTH) * cosValue);
                outerPoint = new Point2D.Double(ALTIMETER_CENTER.getX() + RADIUS * sinValue, ALTIMETER_CENTER.getY() + RADIUS * cosValue);
                textPoint = new Point2D.Double(ALTIMETER_CENTER.getX() + (RADIUS - TEXT_DISTANCE + STD_FONT.getSize() / 2f) * sinValue, ALTIMETER_CENTER.getY() + (RADIUS - TEXT_DISTANCE + STD_FONT.getSize() / 2f) * cosValue + TEXT_DISTANCE / 2.5f);

                // Draw text
                final TextLayout TEXT_LAYOUT = new TextLayout(String.valueOf(Math.round(valueCounter)), G2.getFont(), RENDER_CONTEXT);

                final Rectangle2D TEXT_BOUNDARY = TEXT_LAYOUT.getBounds();

                // if gauge is full circle, avoid painting maxValue over minValue
                if (FREE_AREA_ANGLE == 0) {
                    if (Float.compare(valueCounter, 10) != 0) {
                        G2.drawString(String.valueOf(Math.round(valueCounter)), (int) (textPoint.getX() - TEXT_BOUNDARY.getWidth() / 2.0), (int) ((textPoint.getY() - TEXT_BOUNDARY.getHeight() / 2.0)));
                    }
                } else {
                    G2.drawString(String.valueOf(Math.round(valueCounter)), (int) (textPoint.getX() - TEXT_BOUNDARY.getWidth() / 2.0), (int) ((textPoint.getY() - TEXT_BOUNDARY.getHeight() / 2.0)));
                }
                counter = 0;
                tickCounter++;

                // Draw ticks
                G2.setColor(TICK_COLOR);
                tick = new Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);
            }

            counter++;
        }

        G2.dispose();

        return image;
    }

    private BufferedImage create_100FT_POINTER_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, (int) (1.0 * WIDTH), Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        //G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final GeneralPath POINTER100FT = new GeneralPath();
        POINTER100FT.setWindingRule(Path2D.WIND_EVEN_ODD);
        POINTER100FT.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.4672897196261682);
        POINTER100FT.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.20093457943925233);
        POINTER100FT.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.16822429906542055);
        POINTER100FT.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.20093457943925233);
        POINTER100FT.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.4672897196261682);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5280373831775701, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5327102803738317);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5794392523364486, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5887850467289719);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.5934579439252337, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5981308411214953, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.6074766355140186);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.616822429906542, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.6261682242990654, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.6261682242990654);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.6261682242990654, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.616822429906542, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.6074766355140186);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5981308411214953, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.5934579439252337, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.5887850467289719);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.5794392523364486, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5327102803738317);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.5280373831775701, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER100FT.closePath();
        final Point2D POINTER100FT_START = new Point2D.Double(0, POINTER100FT.getBounds2D().getMinY());
        final Point2D POINTER100FT_STOP = new Point2D.Double(0, POINTER100FT.getBounds2D().getMaxY());
        final float[] POINTER100FT_FRACTIONS = {
            0.0f,
            0.31f,
            0.3101f,
            0.32f,
            1.0f
        };
        final Color[] POINTER100FT_COLORS = {
            new Color(255, 255, 255, 255),
            new Color(255, 255, 255, 255),
            new Color(255, 255, 255, 255),
            new Color(32, 32, 32, 255),
            new Color(32, 32, 32, 255)
        };
        final LinearGradientPaint POINTER100FT_GRADIENT = new LinearGradientPaint(POINTER100FT_START, POINTER100FT_STOP, POINTER100FT_FRACTIONS, POINTER100FT_COLORS);
        G2.setPaint(POINTER100FT_GRADIENT);
        G2.fill(POINTER100FT);

        G2.dispose();

        return IMAGE;
    }

    private BufferedImage create_1000FT_POINTER_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, (int) (1.0 * WIDTH), Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        //G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final GeneralPath POINTER1000FT = new GeneralPath();
        POINTER1000FT.setWindingRule(Path2D.WIND_EVEN_ODD);
        POINTER1000FT.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.46261682242990654, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.40186915887850466, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.40186915887850466);
        POINTER1000FT.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.3317757009345794);
        POINTER1000FT.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.40186915887850466);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.40186915887850466, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.46261682242990654, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5280373831775701, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5327102803738317);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.46261682242990654, IMAGE_HEIGHT * 0.5747663551401869, IMAGE_WIDTH * 0.46261682242990654, IMAGE_HEIGHT * 0.5934579439252337);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.616822429906542, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.6121495327102804, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.6121495327102804);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.6121495327102804, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.616822429906542, IMAGE_WIDTH * 0.5373831775700935, IMAGE_HEIGHT * 0.5934579439252337);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.5373831775700935, IMAGE_HEIGHT * 0.5747663551401869, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5327102803738317);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.5280373831775701, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER1000FT.closePath();
        final Point2D POINTER1000FT_START = new Point2D.Double(0, POINTER1000FT.getBounds2D().getMinY());
        final Point2D POINTER1000FT_STOP = new Point2D.Double(0, POINTER1000FT.getBounds2D().getMaxY());
        final float[] POINTER1000FT_FRACTIONS = {
            0.0f,
            0.51f,
            0.52f,
            0.5201f,
            0.53f,
            1.0f
        };
        final Color[] POINTER1000FT_COLORS = {
            new Color(255, 255, 255, 255),
            new Color(255, 255, 255, 255),
            new Color(255, 255, 255, 255),
            new Color(32, 32, 32, 255),
            new Color(32, 32, 32, 255),
            new Color(32, 32, 32, 255)
        };
        final LinearGradientPaint POINTER1000FT_GRADIENT = new LinearGradientPaint(POINTER1000FT_START, POINTER1000FT_STOP, POINTER1000FT_FRACTIONS, POINTER1000FT_COLORS);
        G2.setPaint(POINTER1000FT_GRADIENT);
        G2.fill(POINTER1000FT);

        G2.dispose();

        return IMAGE;
    }

    private BufferedImage create_10000FT_POINTER_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, (int) (1.0 * WIDTH), Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        //G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final GeneralPath POINTER10000FT = new GeneralPath();
        POINTER10000FT.setWindingRule(Path2D.WIND_EVEN_ODD);
        POINTER10000FT.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER10000FT.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.4672897196261682);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.3177570093457944);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.3037383177570093);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.1822429906542056);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.11682242990654206);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.46261682242990654, IMAGE_HEIGHT * 0.11682242990654206);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.1822429906542056);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.29906542056074764);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.3177570093457944);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.4672897196261682);
        POINTER10000FT.curveTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER10000FT.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
        POINTER10000FT.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5327102803738317);
        POINTER10000FT.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
        POINTER10000FT.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER10000FT.closePath();
        G2.setColor(Color.WHITE);
        G2.fill(POINTER10000FT);

        G2.dispose();

        return IMAGE;
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "Altimeter";
    }
}
