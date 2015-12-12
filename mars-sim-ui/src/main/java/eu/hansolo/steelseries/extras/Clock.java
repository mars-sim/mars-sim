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
import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.PointerType;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.Border;

/**
 *
 * @author hansolo
 */
public final class Clock extends AbstractRadial implements java.awt.event.ActionListener {
    // <editor-fold defaultstate="collapsed" desc="Variable declaration">
    private static final double ANGLE_STEP = 6;
    private final javax.swing.Timer CLOCK_TIMER;
    private boolean automatic = false;
    private double minutePointerAngle = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE) * ANGLE_STEP;
    private double hourPointerAngle = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR) * ANGLE_STEP * 5 + 0.5 * java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE);
    private double secondPointerAngle = java.util.Calendar.getInstance().get(java.util.Calendar.SECOND) * ANGLE_STEP;
    private final java.awt.Rectangle INNER_BOUNDS;
    private boolean secondMovesContinuous = false;
    // Background
    private final Point2D CENTER = new Point2D.Double();
    // Images used to combine layers for background and foreground
    private BufferedImage bImage;
    private BufferedImage fImage;
    private BufferedImage hourImage;
    private BufferedImage hourShadowImage;
    private BufferedImage minuteImage;
    private BufferedImage minuteShadowImage;
    private BufferedImage knobImage;
    private BufferedImage secondImage;
    private BufferedImage secondShadowImage;
    private BufferedImage topKnobImage;
    private BufferedImage disabledImage;
    private int hour = 11;
    private int minute = 55;
    private int second = 0;
    private int timeZoneOffsetHour = 0;
    private int timeZoneOffsetMinute = 0;
    private boolean secondPointerVisible = true;
    private final Color SHADOW_COLOR = new Color(0.0f, 0.0f, 0.0f, 0.65f);
    // Alignment related
    private int horizontalAlignment;
    private int verticalAlignment;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public Clock() {
        super();
        CLOCK_TIMER = new Timer(1000, this);
        INNER_BOUNDS = new Rectangle(200, 200);
        init(getInnerBounds().width, getInnerBounds().height);
        setPointerColor(ColorDef.BLACK);
        horizontalAlignment = SwingConstants.CENTER;
		verticalAlignment = SwingConstants.CENTER;
        //CLOCK_TIMER.start();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    @Override
    public AbstractGauge init(final int WIDTH, final int HEIGHT) {
        if (WIDTH <= 1 || HEIGHT <= 1) {
            return this;
        }

        CENTER.setLocation(INNER_BOUNDS.getCenterX() - getInsets().top, INNER_BOUNDS.getCenterX() - getInsets().top);

        if (!isFrameVisible()) {
            setFramelessOffset(-getInnerBounds().width * 0.0841121495, -getInnerBounds().width * 0.0841121495);
        } else {
            setFramelessOffset(getInnerBounds().x, getInnerBounds().y);
        }

        // Create Background Image
        if (bImage != null) {
            bImage.flush();
        }
        bImage = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);

        // Create Foreground Image
        if (fImage != null) {
            fImage.flush();
        }
        fImage = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);

        if (isFrameVisible()) {
            switch (getFrameType()) {
                case ROUND:
                    FRAME_FACTORY.createRadialFrame(WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameEffect(), bImage);
                    break;
                case SQUARE:
                    FRAME_FACTORY.createLinearFrame(WIDTH, WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameEffect(), bImage);
                    break;
                default:
                    FRAME_FACTORY.createRadialFrame(WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameEffect(), bImage);
                    break;
            }
        }

        if (isBackgroundVisible()) {
            create_BACKGROUND_Image(WIDTH, bImage);
        }

        create_TICKMARKS_Image(WIDTH, bImage);

        if (hourImage != null) {
            hourImage.flush();
        }
        hourImage = create_HOUR_Image(WIDTH);

        if (hourShadowImage != null) {
            hourShadowImage.flush();
        }
        hourShadowImage = create_HOUR_SHADOW_Image(WIDTH);

        if (minuteImage != null) {
            minuteImage.flush();
        }
        minuteImage = create_MINUTE_Image(WIDTH);

        if (minuteShadowImage != null) {
            minuteShadowImage.flush();
        }
        minuteShadowImage = create_MINUTE_SHADOW_Image(WIDTH);

        if (knobImage != null) {
            knobImage.flush();
        }
        knobImage = create_KNOB_Image(WIDTH);

        if (secondImage != null) {
            secondImage.flush();
        }
        secondImage = create_SECOND_Image(WIDTH);

        if (secondShadowImage != null) {
            secondShadowImage.flush();
        }
        secondShadowImage = create_SECOND_SHADOW_Image(WIDTH);

        if (topKnobImage != null) {
            topKnobImage.flush();
        }
        topKnobImage = create_TOP_KNOB_Image(WIDTH);

        if (isForegroundVisible()) {
            switch (getFrameType()) {
                case SQUARE:
                    FOREGROUND_FACTORY.createLinearForeground(WIDTH, WIDTH, false, bImage);
                    break;

                case ROUND:

                default:
                    FOREGROUND_FACTORY.createRadialForeground(WIDTH, false, getForegroundType(), fImage);
                    break;
            }
        }

        if (disabledImage != null) {
            disabledImage.flush();
        }
        disabledImage = DISABLED_FACTORY.createRadialDisabled(WIDTH);

        return this;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Visualization">
    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D G2 = (Graphics2D) g.create();

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Translate the coordinate system related to the insets
        G2.translate(getFramelessOffset().getX(), getFramelessOffset().getY());

        final AffineTransform OLD_TRANSFORM = G2.getTransform();

        // Draw combined background image
        G2.drawImage(bImage, 0, 0, null);

        // Draw the hour pointer
        G2.rotate(Math.toRadians(hourPointerAngle + (2 * Math.sin(Math.toRadians(hourPointerAngle)))), CENTER.getX(), CENTER.getY());
        G2.drawImage(hourShadowImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);
        G2.rotate(Math.toRadians(hourPointerAngle), CENTER.getX(), CENTER.getY());
        G2.drawImage(hourImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw the minute pointer
        G2.rotate(Math.toRadians(minutePointerAngle + (2 * Math.sin(Math.toRadians(minutePointerAngle)))), CENTER.getX(), CENTER.getY());
        G2.drawImage(minuteShadowImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);
        G2.rotate(Math.toRadians(minutePointerAngle), CENTER.getX(), CENTER.getY());
        G2.drawImage(minuteImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw knob image
        if (getPointerType() == PointerType.TYPE1) {
            G2.drawImage(knobImage, 0, 0, null);
        }

        // Draw the second pointer
        if (secondPointerVisible) {
            G2.rotate(Math.toRadians(secondPointerAngle + (2 * Math.sin(Math.toRadians(secondPointerAngle)))), CENTER.getX(), CENTER.getY());
            G2.drawImage(secondShadowImage, 0, 0, null);
            G2.setTransform(OLD_TRANSFORM);
            G2.rotate(Math.toRadians(secondPointerAngle), CENTER.getX(), CENTER.getY());
            G2.drawImage(secondImage, 0, 0, null);
            G2.setTransform(OLD_TRANSFORM);
        }

        // Draw the top knob
        G2.drawImage(topKnobImage, 0, 0, null);

        // Draw combined foreground image
        G2.drawImage(fImage, 0, 0, null);

        if (!isEnabled()) {
            G2.drawImage(disabledImage, 0, 0, null);
        }

        G2.dispose();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    /**
     * Returns true if the clock will be visualized by the current time
     * @return true if the clock will be visualized by the current time
     */
    public boolean isAutomatic() {
        return automatic;
    }

    /**
     * Enables / disables the visualization of the clock by using the current time
     * @param AUTOMATIC
     */
    public void setAutomatic(final boolean AUTOMATIC) {
        automatic = AUTOMATIC;
        if (AUTOMATIC) {
            if (!CLOCK_TIMER.isRunning()) {
                CLOCK_TIMER.start();
            }
        } else {
            if (CLOCK_TIMER.isRunning()) {
                CLOCK_TIMER.stop();
            }
        }
        repaint(getInnerBounds());
    }

    /**
     * Returns the current hour of the clock
     * @return the current hour of the clock
     */
    public int getHour() {
        return hour;
    }

    /**
     * Sets the current hour of the clock
     * @param HOUR
     */
    public void setHour(final int HOUR) {
        hour = HOUR % 12;
        calculateAngles(hour, minute, second);
        repaint(getInnerBounds());
    }

    /**
     * Returns the current minute of the clock
     * @return the current minute of the clock
     */
    public int getMinute() {
        return minute;
    }

    /**
     * Sets the current minute of the clock
     * @param MINUTE
     */
    public void setMinute(final int MINUTE) {
        minute = MINUTE % 60;
        calculateAngles(hour, minute, second);
        repaint(getInnerBounds());
    }

    /**
     * Returns the current second of the clock
     * @return the current second of the clock
     */
    public int getSecond() {
        return second;
    }

    /**
     * Sets the current second of the clock
     * @param SECOND
     */
    public void setSecond(final int SECOND) {
        second = SECOND % 60;
        calculateAngles(hour, minute, second);
        repaint(getInnerBounds());
    }

    /**
     * Returns the current timezone offset in hours
     * @return the current timezone offset in hours
     */
    public int getTimeZoneOffsetHour() {
        return this.timeZoneOffsetHour;
    }

    /**
     * Sets the current timezone offset in hours
     * @param TIMEZONE_OFFSET_HOUR
     */
    public void setTimeZoneOffsetHour(final int TIMEZONE_OFFSET_HOUR) {
        this.timeZoneOffsetHour = TIMEZONE_OFFSET_HOUR;
    }

    /**
     * Returns the additional timezone offset in minutes
     * @return the additional timezone offset in minutes
     */
    public int getTimeZoneOffsetMinute() {
        return this.timeZoneOffsetMinute;
    }

    /**
     * Sets the additional timezone offset in minutes
     * @param TIMEZONE_OFFSET_MINUTE
     */
    public void setTimeZoneOffsetMinute(final int TIMEZONE_OFFSET_MINUTE) {
        this.timeZoneOffsetMinute = TIMEZONE_OFFSET_MINUTE;
    }

    /**
     * Returns true if the second pointer of the clock is visible
     * @return true if the second pointer of the clock is visible
     */
    public boolean isSecondPointerVisible() {
        return secondPointerVisible;
    }

    /**
     * Enables / disables the visibility of the second pointer of the clock
     * @param SECOND_POINTER_VISIBLE
     */
    public void setSecondPointerVisible(final boolean SECOND_POINTER_VISIBLE) {
        secondPointerVisible = SECOND_POINTER_VISIBLE;
        repaint(getInnerBounds());
    }

    /**
     * Returns true if the second pointer moves continuously as you might know it from
     * an automatic clock. Otherwise the second pointer will move only once each second.
     * @return true if the second pointer moves continuously
     */
    public boolean isSecondMovesContinuous() {
        return secondMovesContinuous;
    }

    /**
     * Enables / disables the continuous movement of the second pointer
     * @param SECOND_MOVES_CONTINUOUS
     */
    public void setSecondMovesContinuous(final boolean SECOND_MOVES_CONTINUOUS) {
        if (SECOND_MOVES_CONTINUOUS) {
            CLOCK_TIMER.setDelay(100);
        } else {
            CLOCK_TIMER.setDelay(1000);
        }
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
    private BufferedImage create_BACKGROUND_Image(final int WIDTH, BufferedImage image) {
        if (WIDTH <= 0) {
            return null;
        }
        if (image == null) {
            image = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        }
        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (getFrameType()) {
            case SQUARE:
                BACKGROUND_FACTORY.createLinearBackground(WIDTH, WIDTH, getBackgroundColor(), getCustomBackground(), getModel().getTextureColor(), image);
                break;
            case ROUND:

            default:
                BACKGROUND_FACTORY.createRadialBackground(WIDTH, getBackgroundColor(), getCustomBackground(), getModel().getTextureColor(), image);
                break;
        }
        G2.dispose();

        return image;
    }

    private BufferedImage create_TICKMARKS_Image(final int WIDTH, BufferedImage image) {
        if (WIDTH <= 0) {
            return null;
        }
        if (image == null) {
            image = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        }

        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        final int IMAGE_WIDTH = image.getWidth();
        //final int IMAGE_HEIGHT = image.getHeight();

        final Color TICKMARK_COLOR = getBackgroundColor().LABEL_COLOR;
        final double SMALL_TICK_WIDTH;
        final double SMALL_TICK_HEIGHT;
        final double BIG_TICK_WIDTH;
        final double BIG_TICK_HEIGHT;
        final Rectangle2D SMALL_TICK = new Rectangle2D.Double();
        final Rectangle2D BIG_TICK = new Rectangle2D.Double();
        final AffineTransform OLD_TRANSFORM = G2.getTransform();

        switch (getPointerType()) {
            case TYPE2:

                // Draw minutes tickmarks
                SMALL_TICK_WIDTH = IMAGE_WIDTH * 0.0140186916;
                SMALL_TICK_HEIGHT = IMAGE_WIDTH * 0.0373831776;
                SMALL_TICK.setFrame(CENTER.getX() - (SMALL_TICK_WIDTH / 2), IMAGE_WIDTH * 0.0981308411, SMALL_TICK_WIDTH, SMALL_TICK_HEIGHT);
                G2.setColor(TICKMARK_COLOR);
                for (int tickAngle = 0; tickAngle < 360; tickAngle += 6) {
                    G2.setTransform(OLD_TRANSFORM);
                    G2.rotate(Math.toRadians(tickAngle), CENTER.getX(), CENTER.getY());
                    G2.fill(SMALL_TICK);
                }

                // Draw hours tickmarks
                BIG_TICK_WIDTH = IMAGE_WIDTH * 0.0327102804;
                BIG_TICK_HEIGHT = IMAGE_WIDTH * 0.1261682243;
                BIG_TICK.setFrame(CENTER.getX() - (BIG_TICK_WIDTH / 2), IMAGE_WIDTH * 0.0981308411, BIG_TICK_WIDTH, BIG_TICK_HEIGHT);
                for (int tickAngle = 0; tickAngle < 360; tickAngle += 30) {
                    G2.setTransform(OLD_TRANSFORM);
                    G2.rotate(Math.toRadians(tickAngle), CENTER.getX(), CENTER.getY());
                    G2.fill(BIG_TICK);
                }
                break;

            case TYPE1:

            default:
                SMALL_TICK_WIDTH = IMAGE_WIDTH * 0.0093457944;
                SMALL_TICK_HEIGHT = IMAGE_WIDTH * 0.0747663551;
                SMALL_TICK.setFrame(CENTER.getX() - (SMALL_TICK_WIDTH / 2), IMAGE_WIDTH * 0.0981308411, SMALL_TICK_WIDTH, SMALL_TICK_HEIGHT);
                for (int tickAngle = 0; tickAngle < 360; tickAngle += 30) {
                    G2.setTransform(OLD_TRANSFORM);
                    G2.rotate(Math.toRadians(tickAngle), CENTER.getX(), CENTER.getY());
                    G2.setColor(TICKMARK_COLOR);
                    G2.fill(SMALL_TICK);
                    G2.setColor(TICKMARK_COLOR.darker());
                    G2.draw(SMALL_TICK);
                }

                BIG_TICK_WIDTH = IMAGE_WIDTH * 0.0280373832;
                BIG_TICK_HEIGHT = IMAGE_WIDTH * 0.0841121495;
                BIG_TICK.setFrame(CENTER.getX() - (BIG_TICK_WIDTH / 2), IMAGE_WIDTH * 0.0981308411, BIG_TICK_WIDTH, BIG_TICK_HEIGHT);
                for (int tickAngle = 0; tickAngle < 360; tickAngle += 90) {
                    G2.setTransform(OLD_TRANSFORM);
                    G2.rotate(Math.toRadians(tickAngle), CENTER.getX(), CENTER.getY());
                    G2.setColor(TICKMARK_COLOR);
                    G2.fill(BIG_TICK);
                    G2.setColor(TICKMARK_COLOR.darker());
                    G2.draw(BIG_TICK);
                }
                break;
        }

        G2.dispose();

        return image;
    }

    private BufferedImage create_HOUR_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        switch (getPointerType()) {
            case TYPE2:
                final double HOUR_POINTER_WIDTH = IMAGE_WIDTH * 0.046728972;
                final double HOUR_POINTER_HEIGHT = IMAGE_WIDTH * 0.2242990654;
                final Rectangle2D HOUR_POINTER = new Rectangle2D.Double(CENTER.getX() - (HOUR_POINTER_WIDTH / 2), (IMAGE_WIDTH * 0.2897196262), HOUR_POINTER_WIDTH, HOUR_POINTER_HEIGHT);
                G2.setPaint(getPointerColor().MEDIUM);
                G2.fill(HOUR_POINTER);
                break;

            case TYPE1:

            default:
                final GeneralPath HOURPOINTER = new GeneralPath();
                HOURPOINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                HOURPOINTER.moveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5607476635514018);
                HOURPOINTER.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.21495327102803738);
                HOURPOINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1822429906542056);
                HOURPOINTER.lineTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.21495327102803738);
                HOURPOINTER.lineTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.5607476635514018);
                HOURPOINTER.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5607476635514018);
                HOURPOINTER.closePath();
                final Point2D HOURPOINTER_START = new Point2D.Double(0, HOURPOINTER.getBounds2D().getMaxY());
                final Point2D HOURPOINTER_STOP = new Point2D.Double(0, HOURPOINTER.getBounds2D().getMinY());
                final float[] HOURPOINTER_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] HOURPOINTER_COLORS = {
                    new Color(245, 246, 248, 255),
                    new Color(176, 181, 188, 255)
                };
                final LinearGradientPaint HOURPOINTER_GRADIENT = new LinearGradientPaint(HOURPOINTER_START, HOURPOINTER_STOP, HOURPOINTER_FRACTIONS, HOURPOINTER_COLORS);
                G2.setPaint(HOURPOINTER_GRADIENT);
                G2.fill(HOURPOINTER);
                final Color STROKE_COLOR_HOURPOINTER = new Color(0xDADDE1);
                G2.setColor(STROKE_COLOR_HOURPOINTER);
                G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                G2.draw(HOURPOINTER);
                break;
        }

        G2.dispose();

        return IMAGE;
    }

    private BufferedImage create_HOUR_SHADOW_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();
        switch (getPointerType()) {
            case TYPE2:

                break;

            case TYPE1:

            default:
                final GeneralPath HOURPOINTER = new GeneralPath();
                HOURPOINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                HOURPOINTER.moveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5607476635514018);
                HOURPOINTER.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.21495327102803738);
                HOURPOINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1822429906542056);
                HOURPOINTER.lineTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.21495327102803738);
                HOURPOINTER.lineTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.5607476635514018);
                HOURPOINTER.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5607476635514018);
                HOURPOINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(HOURPOINTER);
                break;
        }

        G2.dispose();

        return IMAGE;
    }

    private BufferedImage create_MINUTE_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        switch (getPointerType()) {
            case TYPE2:
                final double MINUTE_POINTER_WIDTH = IMAGE_WIDTH * 0.0327102804;
                final double MINUTE_POINTER_HEIGHT = IMAGE_WIDTH * 0.3878504673;
                final Rectangle2D MINUTE_POINTER = new Rectangle2D.Double(CENTER.getX() - (MINUTE_POINTER_WIDTH / 2), (IMAGE_WIDTH * 0.1168224299), MINUTE_POINTER_WIDTH, MINUTE_POINTER_HEIGHT);
                G2.setPaint(getPointerColor().MEDIUM);
                G2.fill(MINUTE_POINTER);
                break;

            case TYPE1:

            default:
                final GeneralPath MINUTEPOINTER = new GeneralPath();
                MINUTEPOINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                MINUTEPOINTER.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5747663551401869);
                MINUTEPOINTER.lineTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.13551401869158877);
                MINUTEPOINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.10747663551401869);
                MINUTEPOINTER.lineTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.14018691588785046);
                MINUTEPOINTER.lineTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5747663551401869);
                MINUTEPOINTER.lineTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5747663551401869);
                MINUTEPOINTER.closePath();
                final Point2D MINUTEPOINTER_START = new Point2D.Double(0, MINUTEPOINTER.getBounds2D().getMinY());
                final Point2D MINUTEPOINTER_STOP = new Point2D.Double(0, MINUTEPOINTER.getBounds2D().getMaxY());
                final float[] MINUTEPOINTER_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] MINUTEPOINTER_COLORS = {
                    new Color(245, 246, 248, 255),
                    new Color(176, 181, 188, 255)
                };
                final LinearGradientPaint MINUTEPOINTER_GRADIENT = new LinearGradientPaint(MINUTEPOINTER_START, MINUTEPOINTER_STOP, MINUTEPOINTER_FRACTIONS, MINUTEPOINTER_COLORS);
                G2.setPaint(MINUTEPOINTER_GRADIENT);
                G2.fill(MINUTEPOINTER);
                final Color STROKE_COLOR_MINUTEPOINTER = new Color(0xDADDE1);
                G2.setColor(STROKE_COLOR_MINUTEPOINTER);
                G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                G2.draw(MINUTEPOINTER);
                break;
        }

        G2.dispose();

        return IMAGE;
    }

    private BufferedImage create_MINUTE_SHADOW_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, (int) (1.0 * WIDTH), Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        switch (getPointerType()) {
            case TYPE2:

                break;

            case TYPE1:

            default:
                final GeneralPath MINUTEPOINTER = new GeneralPath();
                MINUTEPOINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                MINUTEPOINTER.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5747663551401869);
                MINUTEPOINTER.lineTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.13551401869158877);
                MINUTEPOINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.10747663551401869);
                MINUTEPOINTER.lineTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.14018691588785046);
                MINUTEPOINTER.lineTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5747663551401869);
                MINUTEPOINTER.lineTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5747663551401869);
                MINUTEPOINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(MINUTEPOINTER);
                break;
        }

        G2.dispose();

        return IMAGE;
    }

    private BufferedImage create_KNOB_Image(final int WIDTH) {
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

        final GeneralPath KNOBSHADOW = new GeneralPath();
        KNOBSHADOW.setWindingRule(Path2D.WIND_EVEN_ODD);
        KNOBSHADOW.moveTo(IMAGE_WIDTH * 0.4532710280373832, IMAGE_HEIGHT * 0.5046728971962616);
        KNOBSHADOW.curveTo(IMAGE_WIDTH * 0.4532710280373832, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.45794392523364486);
        KNOBSHADOW.curveTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5467289719626168, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5467289719626168, IMAGE_HEIGHT * 0.5046728971962616);
        KNOBSHADOW.curveTo(IMAGE_WIDTH * 0.5467289719626168, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.5560747663551402, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5560747663551402);
        KNOBSHADOW.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5560747663551402, IMAGE_WIDTH * 0.4532710280373832, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.4532710280373832, IMAGE_HEIGHT * 0.5046728971962616);
        KNOBSHADOW.closePath();
        final Point2D KNOBSHADOW_START = new Point2D.Double(0, KNOBSHADOW.getBounds2D().getMinY());
        final Point2D KNOBSHADOW_STOP = new Point2D.Double(0, KNOBSHADOW.getBounds2D().getMaxY());
        final float[] KNOBSHADOW_FRACTIONS = {
            0.0f,
            1.0f
        };
        final Color[] KNOBSHADOW_COLORS = {
            new Color(40, 40, 41, 255),
            new Color(13, 13, 13, 255)
        };
        final LinearGradientPaint KNOBSHADOW_GRADIENT = new LinearGradientPaint(KNOBSHADOW_START, KNOBSHADOW_STOP, KNOBSHADOW_FRACTIONS, KNOBSHADOW_COLORS);
        G2.setPaint(KNOBSHADOW_GRADIENT);
        G2.fill(KNOBSHADOW);

        final GeneralPath KNOB = new GeneralPath();
        KNOB.setWindingRule(Path2D.WIND_EVEN_ODD);
        KNOB.moveTo(IMAGE_WIDTH * 0.45794392523364486, IMAGE_HEIGHT * 0.5);
        KNOB.curveTo(IMAGE_WIDTH * 0.45794392523364486, IMAGE_HEIGHT * 0.4766355140186916, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.45794392523364486);
        KNOB.curveTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5420560747663551, IMAGE_HEIGHT * 0.4766355140186916, IMAGE_WIDTH * 0.5420560747663551, IMAGE_HEIGHT * 0.5);
        KNOB.curveTo(IMAGE_WIDTH * 0.5420560747663551, IMAGE_HEIGHT * 0.5233644859813084, IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.5420560747663551, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5420560747663551);
        KNOB.curveTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5420560747663551, IMAGE_WIDTH * 0.45794392523364486, IMAGE_HEIGHT * 0.5233644859813084, IMAGE_WIDTH * 0.45794392523364486, IMAGE_HEIGHT * 0.5);
        KNOB.closePath();
        final Point2D KNOB_START = new Point2D.Double(0, KNOB.getBounds2D().getMinY());
        final Point2D KNOB_STOP = new Point2D.Double(0, KNOB.getBounds2D().getMaxY());
        final float[] KNOB_FRACTIONS = {
            0.0f,
            1.0f
        };
        final Color[] KNOB_COLORS = {
            new Color(238, 240, 242, 255),
            new Color(101, 105, 109, 255)
        };
        final LinearGradientPaint KNOB_GRADIENT = new LinearGradientPaint(KNOB_START, KNOB_STOP, KNOB_FRACTIONS, KNOB_COLORS);
        G2.setPaint(KNOB_GRADIENT);
        G2.fill(KNOB);

        G2.dispose();

        return IMAGE;
    }

    private BufferedImage create_SECOND_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        switch (getPointerType()) {
            case TYPE2:
                final Rectangle2D TOP = new Rectangle2D.Double(IMAGE_WIDTH * 0.4953271028, IMAGE_WIDTH * 0.0981308411, IMAGE_WIDTH * 0.0093457944, IMAGE_WIDTH * 0.1261682243);
                final Rectangle2D BOTTOM = new Rectangle2D.Double(IMAGE_WIDTH * 0.4906542056, IMAGE_WIDTH * 0.308411215, IMAGE_WIDTH * 0.0186915888, IMAGE_WIDTH * 0.191588785);
                final java.awt.geom.Area SECOND = new java.awt.geom.Area(TOP);
                SECOND.add(new java.awt.geom.Area(new Ellipse2D.Double(IMAGE_WIDTH * 0.453271028, IMAGE_WIDTH * 0.2196261682, IMAGE_WIDTH * 0.0934579439, IMAGE_WIDTH * 0.0934579439)));
                SECOND.subtract(new java.awt.geom.Area(new Ellipse2D.Double(IMAGE_WIDTH * 0.4672897196, IMAGE_WIDTH * 0.2336448598, IMAGE_WIDTH * 0.0654205607, IMAGE_WIDTH * 0.0654205607)));
                SECOND.add(new java.awt.geom.Area(BOTTOM));
                final GeneralPath SECOND_POINTER = new GeneralPath(SECOND);
                G2.setPaint(ColorDef.RED.MEDIUM);
                G2.fill(SECOND_POINTER);
                break;

            case TYPE1:

            default:
                final GeneralPath SECONDPOINTER = new GeneralPath();
                SECONDPOINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                SECONDPOINTER.moveTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.11682242990654206);
                SECONDPOINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5747663551401869);
                SECONDPOINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5747663551401869);
                SECONDPOINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.11682242990654206);
                SECONDPOINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.11682242990654206);
                SECONDPOINTER.closePath();
                final Point2D SECONDPOINTER_START = new Point2D.Double(SECONDPOINTER.getBounds2D().getMaxX(), 0);
                final Point2D SECONDPOINTER_STOP = new Point2D.Double(SECONDPOINTER.getBounds2D().getMinX(), 0);
                final float[] SECONDPOINTER_FRACTIONS = {
                    0.0f,
                    0.47f,
                    1.0f
                };
                final Color[] SECONDPOINTER_COLORS = {
                    new Color(236, 123, 125, 255),
                    new Color(231, 27, 33, 255),
                    new Color(166, 40, 46, 255)
                };
                final LinearGradientPaint SECONDPOINTER_GRADIENT = new LinearGradientPaint(SECONDPOINTER_START, SECONDPOINTER_STOP, SECONDPOINTER_FRACTIONS, SECONDPOINTER_COLORS);
                G2.setPaint(SECONDPOINTER_GRADIENT);
                G2.fill(SECONDPOINTER);
                break;
        }

        G2.dispose();

        return IMAGE;
    }

    private BufferedImage create_SECOND_SHADOW_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        switch (getPointerType()) {
            case TYPE2:

                final Rectangle2D TOP = new Rectangle2D.Double(IMAGE_WIDTH * 0.4953271028, IMAGE_WIDTH * 0.0981308411, IMAGE_WIDTH * 0.0093457944, IMAGE_WIDTH * 0.1261682243);
                final Rectangle2D BOTTOM = new Rectangle2D.Double(IMAGE_WIDTH * 0.4906542056, IMAGE_WIDTH * 0.308411215, IMAGE_WIDTH * 0.0186915888, IMAGE_WIDTH * 0.191588785);
                final java.awt.geom.Area SECOND = new java.awt.geom.Area(TOP);
                SECOND.add(new java.awt.geom.Area(new Ellipse2D.Double(IMAGE_WIDTH * 0.453271028, IMAGE_WIDTH * 0.2196261682, IMAGE_WIDTH * 0.0934579439, IMAGE_WIDTH * 0.0934579439)));
                SECOND.subtract(new java.awt.geom.Area(new Ellipse2D.Double(IMAGE_WIDTH * 0.4672897196, IMAGE_WIDTH * 0.2336448598, IMAGE_WIDTH * 0.0654205607, IMAGE_WIDTH * 0.0654205607)));
                SECOND.add(new java.awt.geom.Area(BOTTOM));
                final GeneralPath SECOND_POINTER = new GeneralPath(SECOND);
                ;
                G2.setPaint(SHADOW_COLOR);
                G2.fill(SECOND_POINTER);
                break;

            case TYPE1:

            default:
                final GeneralPath SECONDPOINTER = new GeneralPath();
                SECONDPOINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                SECONDPOINTER.moveTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.11682242990654206);
                SECONDPOINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5747663551401869);
                SECONDPOINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5747663551401869);
                SECONDPOINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.11682242990654206);
                SECONDPOINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.11682242990654206);
                SECONDPOINTER.closePath();
                G2.setPaint(SHADOW_COLOR);
                G2.fill(SECONDPOINTER);
                break;
        }

        G2.dispose();

        return IMAGE;
    }

    private BufferedImage create_TOP_KNOB_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        switch (getPointerType()) {
            case TYPE2:
                final double CENTER_KNOB_DIAMETER = WIDTH * 0.0887850467;
                final Ellipse2D CENTER_KNOB = new Ellipse2D.Double(CENTER.getX() - CENTER_KNOB_DIAMETER / 2, CENTER.getY() - CENTER_KNOB_DIAMETER / 2, CENTER_KNOB_DIAMETER, CENTER_KNOB_DIAMETER);
                G2.setPaint(getPointerColor().MEDIUM);
                G2.fill(CENTER_KNOB);
                break;

            case TYPE1:

            default:
                final GeneralPath TOPKNOBSHADOW = new GeneralPath();
                TOPKNOBSHADOW.setWindingRule(Path2D.WIND_EVEN_ODD);
                TOPKNOBSHADOW.moveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5);
                TOPKNOBSHADOW.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.48598130841121495, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.4719626168224299);
                TOPKNOBSHADOW.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48598130841121495, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.5);
                TOPKNOBSHADOW.curveTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.5280373831775701, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5280373831775701);
                TOPKNOBSHADOW.curveTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.5280373831775701, IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5);
                TOPKNOBSHADOW.closePath();
                final Point2D TOPKNOBSHADOW_START = new Point2D.Double(0, TOPKNOBSHADOW.getBounds2D().getMinY());
                final Point2D TOPKNOBSHADOW_STOP = new Point2D.Double(0, TOPKNOBSHADOW.getBounds2D().getMaxY());
                final float[] TOPKNOBSHADOW_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] TOPKNOBSHADOW_COLORS = {
                    new Color(221, 223, 223, 255),
                    new Color(38, 40, 41, 255)
                };
                final LinearGradientPaint TOPKNOBSHADOW_GRADIENT = new LinearGradientPaint(TOPKNOBSHADOW_START, TOPKNOBSHADOW_STOP, TOPKNOBSHADOW_FRACTIONS, TOPKNOBSHADOW_COLORS);
                G2.setPaint(TOPKNOBSHADOW_GRADIENT);
                G2.fill(TOPKNOBSHADOW);

                final GeneralPath TOPKNOB = new GeneralPath();
                TOPKNOB.setWindingRule(Path2D.WIND_EVEN_ODD);
                TOPKNOB.moveTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5);
                TOPKNOB.curveTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.48598130841121495, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.4766355140186916, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.4766355140186916);
                TOPKNOB.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.4766355140186916, IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.48598130841121495, IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.5);
                TOPKNOB.curveTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.5233644859813084, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5233644859813084);
                TOPKNOB.curveTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.5233644859813084, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5);
                TOPKNOB.closePath();
                final Point2D TOPKNOB_START = new Point2D.Double(0, TOPKNOB.getBounds2D().getMinY());
                final Point2D TOPKNOB_STOP = new Point2D.Double(0, TOPKNOB.getBounds2D().getMaxY());
                final float[] TOPKNOB_FRACTIONS = {
                    0.0f,
                    0.11f,
                    0.12f,
                    0.2f,
                    0.2001f,
                    1.0f
                };
                final Color[] TOPKNOB_COLORS = {
                    new Color(234, 235, 238, 255),
                    new Color(234, 236, 238, 255),
                    new Color(232, 234, 236, 255),
                    new Color(192, 197, 203, 255),
                    new Color(190, 195, 201, 255),
                    new Color(169, 174, 181, 255)
                };
                final LinearGradientPaint TOPKNOB_GRADIENT = new LinearGradientPaint(TOPKNOB_START, TOPKNOB_STOP, TOPKNOB_FRACTIONS, TOPKNOB_COLORS);
                G2.setPaint(TOPKNOB_GRADIENT);
                G2.fill(TOPKNOB);
                break;
        }

        G2.dispose();

        return IMAGE;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Misc">
    private void calculateAngles(final int HOUR, final int MINUTE, final int SECOND) {
        secondPointerAngle = SECOND * ANGLE_STEP;
        minutePointerAngle = MINUTE * ANGLE_STEP;
        hourPointerAngle = HOUR * ANGLE_STEP * 5 + (0.5) * minute;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Size related">
    @Override
    public void calcInnerBounds() {
        final Insets INSETS = getInsets();
        final int SIZE = (getWidth() - INSETS.left - INSETS.right) <= (getHeight() - INSETS.top - INSETS.bottom) ? (getWidth() - INSETS.left - INSETS.right) : (getHeight() - INSETS.top - INSETS.bottom);
        if (!isFrameVisible()) {
            INNER_BOUNDS.setBounds(INSETS.left, INSETS.top, (int)(SIZE * 1.202247191), (int)(SIZE * 1.202247191));
        } else {
            INNER_BOUNDS.setBounds(INSETS.left, INSETS.top, SIZE, SIZE);
        }
    }

    @Override
    public Rectangle getInnerBounds() {
        return INNER_BOUNDS;
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension dim = super.getMinimumSize();
        if (dim.width < 50 || dim.height < 50) {
            dim = new Dimension(50, 50);
        }
        return dim;
    }

    @Override
    public void setMinimumSize(final Dimension DIM) {
        int  width = DIM.width < 50 ? 50 : DIM.width;
        int height = DIM.height < 50 ? 50 : DIM.height;
        final int SIZE = width <= height ? width : height;
        super.setMinimumSize(new Dimension(SIZE, SIZE));
        calcInnerBounds();
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
        invalidate();
        repaint();
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension dim = super.getMaximumSize();
        if (dim.width > 1080 || dim.height > 1080) {
            dim = new Dimension(1080, 1080);
        }
        return dim;
    }

    @Override
    public void setMaximumSize(final Dimension DIM) {
        int  width = DIM.width > 1080 ? 1080 : DIM.width;
        int height = DIM.height > 1080 ? 1080 : DIM.height;
        final int SIZE = width <= height ? width : height;
        super.setMaximumSize(new Dimension(SIZE, SIZE));
        calcInnerBounds();
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
        invalidate();
        repaint();
    }

    @Override
    public void setPreferredSize(final Dimension DIM) {
        final int SIZE = DIM.width <= DIM.height ? DIM.width : DIM.height;
        super.setSize(new Dimension(SIZE, SIZE));
        calcInnerBounds();
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
    }

    @Override
    public void setSize(final int WIDTH, final int HEIGHT) {
        final int SIZE = WIDTH <= HEIGHT ? WIDTH : HEIGHT;
        super.setSize(SIZE, SIZE);
        calcInnerBounds();
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
    }

    @Override
    public void setSize(final Dimension DIM) {
        final int SIZE = DIM.width <= DIM.height ? DIM.width : DIM.height;
        super.setSize(new Dimension(SIZE, SIZE));
        calcInnerBounds();
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
    }

    @Override
    public void setBounds(final Rectangle BOUNDS) {
        if (BOUNDS.width <= BOUNDS.height) {
            // vertical
            int yNew;
            switch(verticalAlignment) {
                case SwingConstants.TOP:
                    yNew = BOUNDS.y;
                    break;
                case SwingConstants.BOTTOM:
                    yNew = BOUNDS.y + (BOUNDS.height - BOUNDS.width);
                    break;
                case SwingConstants.CENTER:
                default:
                    yNew = BOUNDS.y + ((BOUNDS.height - BOUNDS.width) / 2);
                    break;
            }
            super.setBounds(BOUNDS.x, yNew, BOUNDS.width, BOUNDS.width);
        } else {
            // horizontal
            int xNew;
            switch(horizontalAlignment) {
                case SwingConstants.LEFT:
                    xNew = BOUNDS.x;
                    break;
                case SwingConstants.RIGHT:
                    xNew = BOUNDS.x + (BOUNDS.width - BOUNDS.height);
                    break;
                case SwingConstants.CENTER:
                default:
                    xNew = BOUNDS.x + ((BOUNDS.width - BOUNDS.height) / 2);
                    break;
            }
            super.setBounds(xNew, BOUNDS.y, BOUNDS.height, BOUNDS.height);
        }
        calcInnerBounds();
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
    }

    @Override
    public void setBounds(final int X, final int Y, final int WIDTH, final int HEIGHT) {
        if (WIDTH <= HEIGHT) {
            // vertical
            int yNew;
            switch(verticalAlignment) {
                case SwingConstants.TOP:
                    yNew = Y;
                    break;
                case SwingConstants.BOTTOM:
                    yNew = Y + (HEIGHT - WIDTH);
                    break;
                case SwingConstants.CENTER:
                default:
                    yNew = Y + ((HEIGHT - WIDTH) / 2);
                    break;
            }
            super.setBounds(X, yNew, WIDTH, WIDTH);
        } else {
            // horizontal
            int xNew;
            switch(horizontalAlignment) {
                case SwingConstants.LEFT:
                    xNew = X;
                    break;
                case SwingConstants.RIGHT:
                    xNew = X + (WIDTH - HEIGHT);
                    break;
                case SwingConstants.CENTER:
                default:
                    xNew = X + ((WIDTH - HEIGHT) / 2);
                    break;
            }
            super.setBounds(xNew, Y, HEIGHT, HEIGHT);
        }
        calcInnerBounds();
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
    }

    @Override
    public void setBorder(Border BORDER) {
        super.setBorder(BORDER);
        calcInnerBounds();
        init(getInnerBounds().width, getInnerBounds().height);
    }

    /**
     * Returns the alignment of the radial gauge along the X axis.
     * @return the alignment of the radial gauge along the X axis.
     */
    @Override
    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * Sets the alignment of the radial gauge along the X axis.
     * @param HORIZONTAL_ALIGNMENT (SwingConstants.CENTER is default)
     */
    @Override
    public void setHorizontalAlignment(final int HORIZONTAL_ALIGNMENT) {
        horizontalAlignment = HORIZONTAL_ALIGNMENT;
    }

    /**
     * Returns the alignment of the radial gauge along the Y axis.
     * @return the alignment of the radial gauge along the Y axis.
     */
    @Override
    public int getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * Sets the alignment of the radial gauge along the Y axis.
     * @param VERTICAL_ALIGNMENT (SwingConstants.CENTER is default)
     */
    @Override
    public void setVerticalAlignment(final int VERTICAL_ALIGNMENT) {
        verticalAlignment = VERTICAL_ALIGNMENT;
    }
    // </editor-fold>

    @Override
    public void dispose() {
        CLOCK_TIMER.removeActionListener(this);
        super.dispose();
    }

    // <editor-fold defaultstate="collapsed" desc="ActionListener">
    @Override
    public void actionPerformed(java.awt.event.ActionEvent event) {
        if (event.getSource().equals(CLOCK_TIMER)) {
            // Seconds
            secondPointerAngle = java.util.Calendar.getInstance().get(java.util.Calendar.SECOND) * ANGLE_STEP + java.util.Calendar.getInstance().get(java.util.Calendar.MILLISECOND) * ANGLE_STEP / 1000;

            // Hours
            hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR) - this.timeZoneOffsetHour;
            if (hour > 12) {
                hour -= 12;
            }
            if (hour < 0) {
                hour += 12;
            }

            // Minutes
            minute = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE) + this.timeZoneOffsetMinute;
            if (minute > 60) {
                minute -= 60;
                hour++;
            }
            if (minute < 0) {
                minute += 60;
                hour--;
            }

            // Calculate angles from current hour and minute values
            hourPointerAngle = hour * ANGLE_STEP * 5 + (0.5) * minute;
            minutePointerAngle = minute * ANGLE_STEP;

            repaint(getInnerBounds());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ComponentListener">
    @Override
    public void componentResized(ComponentEvent event) {
        final int SIZE = getWidth() <= getHeight() ? getWidth() : getHeight();
        final Container PARENT = getParent();
        if ((PARENT != null) && (PARENT.getLayout() == null)) {
            if (SIZE < getMinimumSize().width || SIZE < getMinimumSize().height) {
                setSize(getMinimumSize());
            } else {
                setSize(SIZE, SIZE);
            }
        } else {
            if (SIZE < getMinimumSize().width || SIZE < getMinimumSize().height) {
                setSize(getMinimumSize());
                setPreferredSize(getMinimumSize());
            } else {
                setSize(new Dimension(SIZE, SIZE));
                setPreferredSize(new Dimension(SIZE, SIZE));
            }
        }

        calcInnerBounds();

        recreateLedImages();
        if (isLedOn()) {
            setCurrentLedImage(getLedImageOn());
        } else {
            setCurrentLedImage(getLedImageOff());
        }

        recreateUserLedImages();
        if (isUserLedOn()) {
            setCurrentUserLedImage(getUserLedImageOn());
        } else {
            setCurrentUserLedImage(getUserLedImageOff());
        }

        getModel().setSize(getLocation().x, getLocation().y, SIZE, SIZE);
        init(getInnerBounds().width, getInnerBounds().height);
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "Clock";
    }
}
