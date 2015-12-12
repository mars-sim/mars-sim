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

import eu.hansolo.steelseries.gauges.AbstractGauge;
import eu.hansolo.steelseries.gauges.AbstractRadial;
import eu.hansolo.steelseries.tools.Section;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.ease.Spline;

/**
 *
 * @author hansolo
 */
public final class Level extends AbstractRadial {
    // <editor-fold defaultstate="collapsed" desc="Variable declaration">
    private double visibleValue = 90;
    private int stepValue = 0;
    private boolean textOrientationFixed = false;
    private boolean decimalVisible = true;
    private final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");
    private double angleStep;
    private Font font = new Font("Verdana", 0, 30);
    private final Point2D CENTER = new Point2D.Double();
    // Images used to combine layers for background and foreground
    private BufferedImage bImage;
    private BufferedImage fImage;
    private BufferedImage pointerImage;
    private BufferedImage stepPointerImage;
    private BufferedImage disabledImage;
    private Timeline timeline = new Timeline(this);
    private final Spline EASE = new Spline(0.5f);
    private long easingDuration = 250;
    private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
    private TextLayout textLayout;
    private final Rectangle2D TEXT_BOUNDARY = new Rectangle2D.Double();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public Level() {
        super();
        setMinValue(-360);
        setMaxValue(360);
        calcAngleStep();
        init(getInnerBounds().width, getInnerBounds().height);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    @Override
    public AbstractGauge init(final int WIDTH, final int HEIGHT) {
        final int GAUGE_WIDTH = isFrameVisible() ? WIDTH : getGaugeBounds().width;
        final int GAUGE_HEIGHT = isFrameVisible() ? HEIGHT : getGaugeBounds().height;

        if (isFrameVisible()) {
            setFramelessOffset(0, 0);
        } else {
            setFramelessOffset(getGaugeBounds().width * 0.0841121495, getGaugeBounds().width * 0.0841121495);
        }

        if (GAUGE_WIDTH <= 1 || GAUGE_HEIGHT <= 1) {
            return this;
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

        create_TICKMARKS_Image(GAUGE_WIDTH, 0, 0, 0, 0, 0, 0, 0, true, true, null, bImage);

        if (pointerImage != null) {
            pointerImage.flush();
        }
        pointerImage = create_POINTER_Image(GAUGE_WIDTH);

        if (stepPointerImage != null) {
            stepPointerImage.flush();
        }
        stepPointerImage = create_STEPPOINTER_Image(GAUGE_WIDTH);

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

        font = new java.awt.Font("Verdana", 0, (int) (0.15 * getWidth()));

        return this;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Visualization">
    @Override
    protected void paintComponent(java.awt.Graphics g) {
        if (!isInitialized()) {
            return;
        }

        final java.awt.Graphics2D G2 = (java.awt.Graphics2D) g.create();

        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Translate the coordinate system related to insets
        G2.translate(-getFramelessOffset().getX(), -getFramelessOffset().getY());

        CENTER.setLocation(getGaugeBounds().getCenterX(), getGaugeBounds().getCenterX());

        final AffineTransform OLD_TRANSFORM = G2.getTransform();

        // Draw combined background image
        G2.drawImage(bImage, 0, 0, null);

        // Draw text if textorientation is fixed
        if (textOrientationFixed) {
            G2.translate(getFramelessOffset().getX(), getFramelessOffset().getY());
            G2.setColor(super.getBackgroundColor().LABEL_COLOR);
            if (decimalVisible) {
                G2.setFont(font.deriveFont(0.10f * getInnerBounds().width));
            } else {
                G2.setFont(font.deriveFont(0.15f * getInnerBounds().width));
            }

            textLayout = new TextLayout(DECIMAL_FORMAT.format(visibleValue) + "\u00B0", G2.getFont(), RENDER_CONTEXT);
            TEXT_BOUNDARY.setFrame(textLayout.getBounds());
            G2.drawString(DECIMAL_FORMAT.format(visibleValue) + "\u00B0", (int) ((getInnerBounds().width - TEXT_BOUNDARY.getWidth()) / 2.0), (int) ((getInnerBounds().width - TEXT_BOUNDARY.getHeight()) / 2.0) + textLayout.getAscent() - textLayout.getDescent());
            G2.translate(-getFramelessOffset().getX(), -getFramelessOffset().getY());
        }

        // Draw the pointer
        //final double ANGLE = getValue() * angleStep;
        G2.rotate(getValue() * angleStep, CENTER.getX(), CENTER.getY());
        G2.drawImage(pointerImage, 0, 0, null);

        // Draw text incl. rotation
        if (!textOrientationFixed) {
            if (!isFrameVisible()) {
                G2.translate(getFramelessOffset().getX(), getFramelessOffset().getY());
            }
            G2.setColor(super.getBackgroundColor().LABEL_COLOR);
            if (decimalVisible) {
                G2.setFont(font.deriveFont(0.15f * getInnerBounds().width));
            } else {
                G2.setFont(font.deriveFont(0.2f * getInnerBounds().width));
            }

            textLayout = new TextLayout(DECIMAL_FORMAT.format(visibleValue) + "\u00B0", G2.getFont(), RENDER_CONTEXT);
            TEXT_BOUNDARY.setFrame(textLayout.getBounds());
            G2.drawString(DECIMAL_FORMAT.format(visibleValue) + "\u00B0", (int) ((getInnerBounds().width - TEXT_BOUNDARY.getWidth()) / 2.0), (int) ((getInnerBounds().width - TEXT_BOUNDARY.getHeight()) / 2.0) + textLayout.getAscent() - textLayout.getDescent());
            G2.translate(-getFramelessOffset().getX(), -getFramelessOffset().getY());
        }

        // Draw StepPointer
        G2.rotate(Math.toRadians(stepValue), CENTER.getX(), CENTER.getY());
        G2.drawImage(stepPointerImage, 0, 0, null);

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
     * Sets the current level value in degrees (0 - 360¬¨¬®‚Äö√†√ª)
     * @param VALUE
     */
    @Override
    public void setValue(final double VALUE) {
        if (isEnabled()) {
            super.setValue(VALUE);

            this.stepValue = 2 * ((int) (Math.abs(VALUE) * 10) % 10);
            if (stepValue > 10) {
                stepValue -= 20;
            }

            if (VALUE == 0) {
                this.visibleValue = 90;
            }

            if (VALUE > 0 && VALUE <= 90) {
                this.visibleValue = (90 - VALUE % 360);
            }

            if (VALUE > 90 && VALUE <= 180) {
                this.visibleValue = (VALUE - 90);
            }

            if (VALUE > 180 && VALUE <= 270) {
                this.visibleValue = (270 - VALUE);
            }

            if (VALUE > 270 && VALUE <= 360) {
                this.visibleValue = (VALUE - 270);
            }

            if (VALUE < 0 && VALUE >= -90) {
                this.visibleValue = (90 - Math.abs(VALUE));
            }

            if (VALUE < -90 && VALUE >= -180) {
                this.visibleValue = Math.abs(VALUE) - 90;
            }

            if (VALUE < -180 && VALUE >= -270) {
                this.visibleValue = 270 - Math.abs(VALUE);
            }

            if (VALUE < -270 && VALUE >= -360) {
                this.visibleValue = Math.abs(VALUE) - 270;
            }

            fireStateChanged();
            repaint();
        }
    }

    @Override
    public void setValueAnimated(double value) {
        if (isEnabled()) {
            // Needle should always take the shortest way to it's new position
            if (360 - value + getValue() < value - getValue()) {
                value = 360 - value;
            }

            if (timeline.getState() == Timeline.TimelineState.PLAYING_FORWARD || timeline.getState() == Timeline.TimelineState.PLAYING_REVERSE) {
                timeline.abort();
            }
            timeline = new Timeline(this);
            timeline.addPropertyToInterpolate("value", getValue(), value);
            timeline.setEase(EASE);

            timeline.setDuration(easingDuration);
            timeline.play();
        }
    }

    @Override
    public double getMinValue() {
        return -360.0;
    }

    @Override
    public double getMaxValue() {
        return 360.0;
    }

    /**
     * Returns true if the text in the center of the component will not be rotated
     * @return true if the text in the center of the component will not be rotated
     */
    public boolean isTextOrientationFixed() {
        return this.textOrientationFixed;
    }

    /**
     * Enables / disables the rotation of the text in the center of the component
     * @param TEXT_ORIENTATION_FIXED
     */
    public void setTextOrientationFixed(final boolean TEXT_ORIENTATION_FIXED) {
        this.textOrientationFixed = TEXT_ORIENTATION_FIXED;
        repaint();
    }

    /**
     * Returns true if decimals will be shown on the degree value
     * @return true if decimals will be shown on the degree value
     */
    public boolean isDecimalVisible() {
        return this.decimalVisible;
    }

    /**
     * Enables / disables the visibility of the decimals on the degree value
     * @param DECIMAL_VISIBLE
     */
    public void setDecimalVisible(final boolean DECIMAL_VISIBLE) {
        if (DECIMAL_VISIBLE) {
            DECIMAL_FORMAT.applyPattern("0.0");
        } else {
            DECIMAL_FORMAT.applyPattern("0");
        }
        this.decimalVisible = DECIMAL_VISIBLE;
        repaint();
    }

    public long getEasingDuration() {
        return this.easingDuration;
    }

    public void setEasingDuration(final long EASING_DURATION) {
        this.easingDuration = EASING_DURATION;
    }

    private void calcAngleStep() {
        angleStep = (4.0 * Math.PI) / (getMaxValue() - getMinValue());
    }

    @Override
    public Point2D getCenter() {
        return new java.awt.geom.Point2D.Double(bImage.getWidth() / 2.0 + getInnerBounds().x, bImage.getHeight() / 2.0 + getInnerBounds().y);
    }

    @Override
    public Rectangle2D getBounds2D() {
        return new java.awt.geom.Rectangle2D.Double(bImage.getMinX(), bImage.getMinY(), bImage.getWidth(), bImage.getHeight());
    }

    @Override
    public Rectangle getLcdBounds() {
        return new Rectangle();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image related">
    private BufferedImage create_TICKMARKS_Image(final int WIDTH, final double FREE_AREA_ANGLE,
                                                                final double OFFSET, final double MIN_VALUE,
                                                                final double MAX_VALUE, final double ANGLE_STEP,
                                                                final int TICK_LABEL_PERIOD,
                                                                final int SCALE_DIVIDER_POWER, final boolean DRAW_TICKS,
                                                                final boolean DRAW_TICK_LABELS,
                                                                ArrayList<Section> tickmarkSections,
                                                                BufferedImage image) {
        if (WIDTH <= 0) {
            return null;
        }
        if (image == null) {
            image = UTIL.createImage(WIDTH, (int) (1.0 * WIDTH), Transparency.TRANSLUCENT);
        }
        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        final Font STD_FONT = new Font("Verdana", 0, (int) (0.04 * WIDTH));
        final Font PERCENTAGE_FONT = new Font("Verdana", 0, (int) (0.03 * WIDTH));
        final BasicStroke MEDIUM_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        final BasicStroke THIN_STROKE = new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        final BasicStroke VERY_THIN_STROKE = new BasicStroke(0.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        final int TEXT_DISTANCE = (int) (0.08 * WIDTH);
        final int MIN_LENGTH = (int) (0.0133333333 * WIDTH);
        final int MED_LENGTH = (int) (0.02 * WIDTH);
        final int MAX_LENGTH = (int) (0.04 * WIDTH);

        // Create the ticks itself
        final float RADIUS = IMAGE_WIDTH * 0.38f;
        final Point2D GAUGE_CENTER = new Point2D.Double(IMAGE_WIDTH / 2.0f, IMAGE_HEIGHT / 2.0f);

        // Draw ticks
        Point2D innerPoint;
        Point2D outerPoint;
        Point2D textPoint = null;
        Line2D tick;
        int counter = 0;
        int tickCounter = 0;
        float valueCounter = 90;
        boolean countUp = false;
        float valueStep = 1;

        G2.setFont(STD_FONT);

        boolean togglePercentage = false;
        double sinValue = 0;
        double cosValue = 0;

        final double STEP = (2.0 * Math.PI) / (360.0);

        for (double alpha = (2.0 * Math.PI); alpha >= STEP; alpha -= STEP) {
            G2.setStroke(THIN_STROKE);
            sinValue = Math.sin(alpha);
            cosValue = Math.cos(alpha);
            textPoint = new Point2D.Double(GAUGE_CENTER.getX() + (RADIUS - TEXT_DISTANCE) * sinValue, GAUGE_CENTER.getY() + (RADIUS - TEXT_DISTANCE) * cosValue);
            innerPoint = new Point2D.Double(GAUGE_CENTER.getX() + (RADIUS - MIN_LENGTH) * sinValue, GAUGE_CENTER.getY() + (RADIUS - MIN_LENGTH) * cosValue);
            outerPoint = new Point2D.Double(GAUGE_CENTER.getX() + RADIUS * sinValue, GAUGE_CENTER.getY() + RADIUS * cosValue);
            G2.setColor(super.getBackgroundColor().LABEL_COLOR);

            // Different tickmark every 5 units
            if (counter % 5 == 0) {
                G2.setColor(super.getBackgroundColor().LABEL_COLOR);
                G2.setStroke(THIN_STROKE);
                innerPoint = new Point2D.Double(GAUGE_CENTER.getX() + (RADIUS - MED_LENGTH) * sinValue, GAUGE_CENTER.getY() + (RADIUS - MED_LENGTH) * cosValue);
                outerPoint = new Point2D.Double(GAUGE_CENTER.getX() + RADIUS * sinValue, GAUGE_CENTER.getY() + RADIUS * cosValue);

                // Draw ticks
                tick = new Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);
            }

            // Different tickmark every 45 units plus text
            if (counter == 45 || counter == 0) {
                G2.setColor(super.getBackgroundColor().LABEL_COLOR);
                G2.setStroke(MEDIUM_STROKE);
                innerPoint = new Point2D.Double(GAUGE_CENTER.getX() + (RADIUS - MAX_LENGTH) * sinValue, GAUGE_CENTER.getY() + (RADIUS - MAX_LENGTH) * cosValue);
                outerPoint = new Point2D.Double(GAUGE_CENTER.getX() + RADIUS * sinValue, GAUGE_CENTER.getY() + RADIUS * cosValue);

                // Draw outer text
                textPoint = new Point2D.Double(GAUGE_CENTER.getX() + (RADIUS - TEXT_DISTANCE) * sinValue, GAUGE_CENTER.getY() + (RADIUS - TEXT_DISTANCE) * cosValue);
                G2.setFont(STD_FONT);

                G2.fill(UTIL.rotateTextAroundCenter(G2, String.valueOf((int) valueCounter) + "\u00B0", (int) textPoint.getX(), (int) textPoint.getY(), (Math.PI - alpha)));

                if (togglePercentage) {
                    textPoint = new Point2D.Double(GAUGE_CENTER.getX() + (RADIUS - TEXT_DISTANCE * 2) * sinValue, GAUGE_CENTER.getY() + (RADIUS - TEXT_DISTANCE * 2) * cosValue);
                    G2.setFont(PERCENTAGE_FONT);

                    G2.fill(UTIL.rotateTextAroundCenter(G2, "100%", (int) textPoint.getX(), (int) textPoint.getY(), (Math.PI - alpha)));
                } else if (valueCounter == 0) {
                    textPoint = new java.awt.geom.Point2D.Double(GAUGE_CENTER.getX() + (RADIUS - TEXT_DISTANCE * 2) * sinValue, GAUGE_CENTER.getY() + (RADIUS - TEXT_DISTANCE * 2) * cosValue);
                    G2.setFont(PERCENTAGE_FONT);

                    G2.fill(UTIL.rotateTextAroundCenter(G2, "0%", (int) textPoint.getX(), (int) textPoint.getY(), (Math.PI - alpha)));
                } else {
                    textPoint = new Point2D.Double(GAUGE_CENTER.getX() + (RADIUS - TEXT_DISTANCE * 2) * sinValue, GAUGE_CENTER.getY() + (RADIUS - TEXT_DISTANCE * 2) * cosValue);
                    G2.setFont(PERCENTAGE_FONT);

                    G2.fill(UTIL.rotateTextAroundCenter(G2, "\u221E", (int) textPoint.getX(), (int) textPoint.getY(), (Math.PI - alpha)));
                }

                togglePercentage ^= true;

                counter = 0;
                tickCounter++;

                // Draw ticks
                tick = new Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);
            }

            // Draw ticks
            G2.setStroke(VERY_THIN_STROKE);
            tick = new Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
            G2.draw(tick);

            counter++;
            if (valueCounter == 0) {
                countUp = true;
            }
            if (valueCounter == 90) {
                countUp = false;
            }
            if (countUp) {
                valueCounter += valueStep;
            } else {
                valueCounter -= valueStep;
            }
        }

        G2.dispose();

        return image;
    }

    @Override
    protected BufferedImage create_POINTER_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final GeneralPath POINTERREDSMALL = new GeneralPath();
        POINTERREDSMALL.setWindingRule(Path2D.WIND_EVEN_ODD);
        POINTERREDSMALL.moveTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.35046728971962615);
        POINTERREDSMALL.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271);
        POINTERREDSMALL.lineTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.35046728971962615);
        POINTERREDSMALL.curveTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.35046728971962615, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.34579439252336447, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.34579439252336447);
        POINTERREDSMALL.curveTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.34579439252336447, IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.35046728971962615, IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.35046728971962615);
        POINTERREDSMALL.closePath();
        final Point2D POINTERREDSMALL_START = new Point2D.Double(0, POINTERREDSMALL.getBounds2D().getMinY());
        final Point2D POINTERREDSMALL_STOP = new Point2D.Double(0, POINTERREDSMALL.getBounds2D().getMaxY());
        final float[] POINTERREDSMALL_FRACTIONS = {
            0.0f,
            0.3f,
            0.59f,
            1.0f
        };
        final Color[] POINTERREDSMALL_COLORS = {
            UTIL.setAlpha(getPointerColor().DARK, 180),
            UTIL.setAlpha(getPointerColor().LIGHT, 180),
            UTIL.setAlpha(getPointerColor().LIGHT, 180),
            UTIL.setAlpha(getPointerColor().DARK, 180)
        };

        final java.awt.LinearGradientPaint POINTERREDSMALL_GRADIENT = new java.awt.LinearGradientPaint(POINTERREDSMALL_START, POINTERREDSMALL_STOP, POINTERREDSMALL_FRACTIONS, POINTERREDSMALL_COLORS);
        G2.setPaint(POINTERREDSMALL_GRADIENT);
        G2.fill(POINTERREDSMALL);
        final Color STROKE_COLOR_POINTERREDSMALL = getPointerColor().LIGHT;
        G2.setColor(STROKE_COLOR_POINTERREDSMALL);
        G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        G2.draw(POINTERREDSMALL);

        final GeneralPath FRAMELEFT = new GeneralPath();
        FRAMELEFT.setWindingRule(Path2D.WIND_EVEN_ODD);
        FRAMELEFT.moveTo(IMAGE_WIDTH * 0.20093457943925233, IMAGE_HEIGHT * 0.43457943925233644);
        FRAMELEFT.lineTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.43457943925233644);
        FRAMELEFT.lineTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.5607476635514018);
        FRAMELEFT.lineTo(IMAGE_WIDTH * 0.20093457943925233, IMAGE_HEIGHT * 0.5607476635514018);
        G2.setColor(super.getBackgroundColor().LABEL_COLOR);
        G2.draw(FRAMELEFT);

        final GeneralPath TRIANGLELEFT = new GeneralPath();
        TRIANGLELEFT.setWindingRule(Path2D.WIND_EVEN_ODD);
        TRIANGLELEFT.moveTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.4719626168224299);
        TRIANGLELEFT.lineTo(IMAGE_WIDTH * 0.205607476635514, IMAGE_HEIGHT * 0.5);
        TRIANGLELEFT.lineTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.5233644859813084);
        TRIANGLELEFT.lineTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.4719626168224299);
        TRIANGLELEFT.closePath();
        G2.setColor(super.getBackgroundColor().LABEL_COLOR);
        G2.fill(TRIANGLELEFT);

        final GeneralPath FRAMERIGHT = new GeneralPath();
        FRAMERIGHT.setWindingRule(Path2D.WIND_EVEN_ODD);
        FRAMERIGHT.moveTo(IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.43457943925233644);
        FRAMERIGHT.lineTo(IMAGE_WIDTH * 0.8364485981308412, IMAGE_HEIGHT * 0.43457943925233644);
        FRAMERIGHT.lineTo(IMAGE_WIDTH * 0.8364485981308412, IMAGE_HEIGHT * 0.5607476635514018);
        FRAMERIGHT.lineTo(IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.5607476635514018);
        G2.setColor(super.getBackgroundColor().LABEL_COLOR);
        G2.draw(FRAMERIGHT);

        final GeneralPath TRIANGLERIGHT = new GeneralPath();
        TRIANGLERIGHT.setWindingRule(Path2D.WIND_EVEN_ODD);
        TRIANGLERIGHT.moveTo(IMAGE_WIDTH * 0.8364485981308412, IMAGE_HEIGHT * 0.4719626168224299);
        TRIANGLERIGHT.lineTo(IMAGE_WIDTH * 0.794392523364486, IMAGE_HEIGHT * 0.5);
        TRIANGLERIGHT.lineTo(IMAGE_WIDTH * 0.8364485981308412, IMAGE_HEIGHT * 0.5233644859813084);
        TRIANGLERIGHT.lineTo(IMAGE_WIDTH * 0.8364485981308412, IMAGE_HEIGHT * 0.4719626168224299);
        TRIANGLERIGHT.closePath();
        G2.setColor(super.getBackgroundColor().LABEL_COLOR);
        G2.fill(TRIANGLERIGHT);

        G2.dispose();

        return IMAGE;
    }

    private BufferedImage create_STEPPOINTER_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
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

        final GeneralPath POINTER_SMALL_LEFT = new GeneralPath();
        POINTER_SMALL_LEFT.setWindingRule(Path2D.WIND_EVEN_ODD);
        POINTER_SMALL_LEFT.moveTo(IMAGE_WIDTH * 0.2850467289719626, IMAGE_HEIGHT * 0.514018691588785);
        POINTER_SMALL_LEFT.lineTo(IMAGE_WIDTH * 0.2102803738317757, IMAGE_HEIGHT * 0.5);
        POINTER_SMALL_LEFT.lineTo(IMAGE_WIDTH * 0.2850467289719626, IMAGE_HEIGHT * 0.48130841121495327);
        POINTER_SMALL_LEFT.curveTo(IMAGE_WIDTH * 0.2850467289719626, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.2803738317757009, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.2803738317757009, IMAGE_HEIGHT * 0.4953271028037383);
        POINTER_SMALL_LEFT.curveTo(IMAGE_WIDTH * 0.2803738317757009, IMAGE_HEIGHT * 0.5046728971962616, IMAGE_WIDTH * 0.2850467289719626, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.2850467289719626, IMAGE_HEIGHT * 0.514018691588785);
        POINTER_SMALL_LEFT.closePath();
        final Point2D POINTER_SMALL_LEFT_START = new Point2D.Double(POINTER_SMALL_LEFT.getBounds2D().getMinX(), 0);
        final Point2D POINTER_SMALL_LEFT_STOP = new Point2D.Double(POINTER_SMALL_LEFT.getBounds2D().getMaxX(), 0);
        final float[] POINTER_SMALL_FRACTIONS = {
            0.0f,
            0.3f,
            0.59f,
            1.0f
        };
        final Color[] POINTER_SMALL_COLORS = {
            UTIL.setAlpha(getPointerColor().DARK, 180),
            UTIL.setAlpha(getPointerColor().LIGHT, 180),
            UTIL.setAlpha(getPointerColor().LIGHT, 180),
            UTIL.setAlpha(getPointerColor().DARK, 180)
        };
        final LinearGradientPaint POINTER_SMALL_LEFT_GRADIENT = new LinearGradientPaint(POINTER_SMALL_LEFT_START, POINTER_SMALL_LEFT_STOP, POINTER_SMALL_FRACTIONS, POINTER_SMALL_COLORS);
        G2.setPaint(POINTER_SMALL_LEFT_GRADIENT);
        G2.fill(POINTER_SMALL_LEFT);
        final Color STROKE_COLOR_POINTER_SMALL = UTIL.setAlpha(getPointerColor().LIGHT, 128);
        G2.setColor(STROKE_COLOR_POINTER_SMALL);
        G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        G2.draw(POINTER_SMALL_LEFT);

        final GeneralPath POINTER_SMALL_RIGHT = new GeneralPath();
        POINTER_SMALL_RIGHT.setWindingRule(Path2D.WIND_EVEN_ODD);
        POINTER_SMALL_RIGHT.moveTo(IMAGE_WIDTH * 0.7149532710280374, IMAGE_HEIGHT * 0.514018691588785);
        POINTER_SMALL_RIGHT.lineTo(IMAGE_WIDTH * 0.7897196261682243, IMAGE_HEIGHT * 0.5);
        POINTER_SMALL_RIGHT.lineTo(IMAGE_WIDTH * 0.7149532710280374, IMAGE_HEIGHT * 0.48130841121495327);
        POINTER_SMALL_RIGHT.curveTo(IMAGE_WIDTH * 0.7149532710280374, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.719626168224299, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.719626168224299, IMAGE_HEIGHT * 0.4953271028037383);
        POINTER_SMALL_RIGHT.curveTo(IMAGE_WIDTH * 0.719626168224299, IMAGE_HEIGHT * 0.5046728971962616, IMAGE_WIDTH * 0.7149532710280374, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.7149532710280374, IMAGE_HEIGHT * 0.514018691588785);
        POINTER_SMALL_RIGHT.closePath();
        final Point2D POINTER_SMALL_RIGHT_START = new Point2D.Double(POINTER_SMALL_RIGHT.getBounds2D().getMaxX(), 0);
        final Point2D POINTER_SMALL_RIGHT_STOP = new Point2D.Double(POINTER_SMALL_RIGHT.getBounds2D().getMinX(), 0);

        final LinearGradientPaint POINTER_SMALL_RIGHT_GRADIENT = new LinearGradientPaint(POINTER_SMALL_RIGHT_START, POINTER_SMALL_RIGHT_STOP, POINTER_SMALL_FRACTIONS, POINTER_SMALL_COLORS);
        G2.setPaint(POINTER_SMALL_RIGHT_GRADIENT);
        G2.fill(POINTER_SMALL_RIGHT);
        G2.setColor(STROKE_COLOR_POINTER_SMALL);
        G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        G2.draw(POINTER_SMALL_RIGHT);

        G2.dispose();

        return IMAGE;
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "Level";
    }
}
