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
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Path2D;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.ease.Spline;

/**
 *
 * @author hansolo
 */
public class AirCompass extends AbstractRadial {
    // <editor-fold defaultstate="collapsed" desc="Variable declaration">
    private boolean rotateTickmarks;
    private double value;
    private double rotationAngle;
    private final Point2D CENTER;
    private BufferedImage frameImage;
    private BufferedImage backgroundImage;
    private BufferedImage tickmarksImage;
    private BufferedImage planeImage;
    private BufferedImage foregroundImage;
    private BufferedImage disabledImage;
    private Timeline timeline;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public AirCompass() {
        super();
        rotateTickmarks = true;
        value = 0;
        rotationAngle = 0;
        CENTER = new Point2D.Double();
        timeline = new Timeline(this);
        init(getInnerBounds().width, getInnerBounds().height);
        setLcdVisible(true);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    @Override
    public final AbstractGauge init(int WIDTH, int HEIGHT) {
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

        if (frameImage != null) {
            frameImage.flush();
        }
        frameImage = create_FRAME_Image(GAUGE_WIDTH);

        if (backgroundImage != null) {
            backgroundImage.flush();
        }
        backgroundImage = create_BACKGROUND_Image(GAUGE_WIDTH);

        if (tickmarksImage != null) {
            tickmarksImage.flush();
        }
        tickmarksImage = create_TICKMARKS_Image(GAUGE_WIDTH);

        if (planeImage != null) {
            planeImage.flush();
        }
        planeImage = create_AIRPLANE_Image(GAUGE_WIDTH);

        if (foregroundImage != null) {
            foregroundImage.flush();
        }
        switch (getFrameType()) {
            case SQUARE:
                foregroundImage = FOREGROUND_FACTORY.createLinearForeground(GAUGE_WIDTH, GAUGE_WIDTH, false);
                break;

            case ROUND:

            default:
                foregroundImage = FOREGROUND_FACTORY.createRadialForeground(GAUGE_WIDTH, false, getForegroundType());
                break;
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

        CENTER.setLocation(getGaugeBounds().getCenterX(), getGaugeBounds().getCenterY());

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Translate the coordinate system related to insets
        G2.translate(getFramelessOffset().getX(), getFramelessOffset().getY());

        final AffineTransform OLD_TRANSFORM = G2.getTransform();

        // Draw the frame
        if (isFrameVisible()) {
            G2.drawImage(frameImage, 0, 0, null);
        }

        // Draw the background
        if (isBackgroundVisible()) {
            G2.drawImage(backgroundImage, 0, 0, null);
        }

        // Draw the tickmarks
        if (rotateTickmarks) {
            G2.rotate(-rotationAngle, CENTER.getX(), CENTER.getY());
            G2.drawImage(tickmarksImage, 0, 0, null);
            G2.setTransform(OLD_TRANSFORM);
        } else {
            G2.drawImage(tickmarksImage, 0, 0, null);
        }

        // Draw plane
        if (!rotateTickmarks) {
            G2.rotate(rotationAngle, CENTER.getX(), CENTER.getY());
            G2.drawImage(planeImage, 0, 0, null);
            G2.setTransform(OLD_TRANSFORM);
        } else {
            G2.drawImage(planeImage, 0, 0, null);
        }

        // Draw foreground
        if (isForegroundVisible()) {
            G2.drawImage(foregroundImage, 0, 0, null);
        }

        if (!isEnabled()) {
            G2.drawImage(disabledImage, 0, 0, null);
        }

        // Translate coordinate system back to original
        G2.translate(-getInnerBounds().x, -getInnerBounds().y);

        G2.dispose();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    @Override
    public double getValue() {
        return value;
    }

    @Override
    public void setValue(final double VALUE) {
        rotationAngle = (2.0 * Math.PI / 360.0) * (VALUE % 360);
        double oldValue = value;
        if (isValueCoupled()) {
            setLcdValue(VALUE);
        }
        fireStateChanged();
        firePropertyChange(VALUE_PROPERTY, oldValue, value);
        repaint(getInnerBounds());
    }

    @Override
    public void setValueAnimated(final double VALUE) {
        if (timeline.getState() == Timeline.TimelineState.PLAYING_FORWARD || timeline.getState() == Timeline.TimelineState.PLAYING_REVERSE) {
            timeline.abort();
        }
        timeline = new Timeline(this);
        timeline.addPropertyToInterpolate("value", value, VALUE);
        timeline.setEase(new Spline(0.5f));

        timeline.setDuration(800);
        timeline.play();
    }

    @Override
    public double getMinValue() {
        return 0;
    }

    @Override
    public double getMaxValue() {
        return 360;
    }

    public boolean isRotateTickmarks() {
        return this.rotateTickmarks;
    }

    public void setRotateTickmarks(final boolean ROTATE_TICKMARKS) {
        this.rotateTickmarks = ROTATE_TICKMARKS;
        setValue(0);
        repaint(getInnerBounds());
    }

    @Override
    public Point2D getCenter() {
        return new Point2D.Double(getInnerBounds().getCenterX() + getInnerBounds().x, getInnerBounds().getCenterX() + getInnerBounds().y);
    }

    @Override
    public Rectangle2D getBounds2D() {
        return getInnerBounds();
    }

    @Override
    public Rectangle getLcdBounds() {
        return new Rectangle();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image related">
    private BufferedImage create_TICKMARKS_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        final int IMAGE_WIDTH = IMAGE.getWidth();

        final AffineTransform OLD_TRANSFORM = G2.getTransform();

        final BasicStroke THIN_STROKE = new BasicStroke(0.01f * IMAGE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        final Font NUMBER_FONT = new Font("Arial", Font.PLAIN, (int) (0.073f * IMAGE_WIDTH));
        final Font CHAR_FONT = new Font("Arial", Font.PLAIN, (int) (0.075f * IMAGE_WIDTH));
        final float TEXT_DISTANCE = 0.1f * IMAGE_WIDTH;
        final float MIN_LENGTH = 0.039f * IMAGE_WIDTH;
        final float MED_LENGTH = 0.04f * IMAGE_WIDTH;

        // Create the watch itself
        final float RADIUS = IMAGE_WIDTH * 0.39f;
        CENTER.setLocation(IMAGE_WIDTH / 2.0f, IMAGE_WIDTH / 2.0f);

        // Draw ticks
        Point2D innerPoint = new Point2D.Double();
        Point2D outerPoint = new Point2D.Double();
        Point2D textPoint = new Point2D.Double();
        Line2D tick;
        int tickCounterFull = 0;
        int tickCounterHalf = 0;
        int counter = 0;

        double sinValue = 0;
        double cosValue = 0;

        final double STEP = (2.0 * Math.PI) / (72.0);

        if (isTickmarkColorFromThemeEnabled()) {
            G2.setColor(getBackgroundColor().LABEL_COLOR);
        } else {
            G2.setColor(getTickmarkColor());
        }

        for (double alpha = 2 * Math.PI; alpha >= 0; alpha -= STEP) {
            G2.setStroke(THIN_STROKE);
            sinValue = Math.sin(alpha - Math.PI);
            cosValue = Math.cos(alpha - Math.PI);

            if (tickCounterHalf == 1) {
                G2.setStroke(THIN_STROKE);
                innerPoint.setLocation(CENTER.getX() + (RADIUS - MED_LENGTH) * sinValue, CENTER.getY() + (RADIUS - MED_LENGTH) * cosValue);
                outerPoint.setLocation(CENTER.getX() + (RADIUS - MIN_LENGTH) * sinValue, CENTER.getY() + (RADIUS - MIN_LENGTH) * cosValue);
                // Draw ticks
                tick = new Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);

                tickCounterHalf = 0;
            }

            // Different tickmark every 15 units
            if (tickCounterFull == 2) {
                G2.setStroke(THIN_STROKE);
                innerPoint.setLocation(CENTER.getX() + (RADIUS - MED_LENGTH) * sinValue, CENTER.getY() + (RADIUS - MED_LENGTH) * cosValue);
                outerPoint.setLocation(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);

                // Draw ticks
                tick = new Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);

                tickCounterFull = 0;
            }

            // Draw text
            textPoint.setLocation(CENTER.getX() + (RADIUS - TEXT_DISTANCE) * sinValue, CENTER.getY() + (RADIUS - TEXT_DISTANCE) * cosValue);
            if (counter != 72 && counter % 6 == 0) {
                if (counter / 2 == 0) {
                    G2.setFont(CHAR_FONT);
                    final Color FORMER_COLOR = G2.getColor();
                    G2.setColor(getPointerColor().LIGHT);
                    G2.rotate(Math.toRadians(0), CENTER.getX(), CENTER.getY());
                    G2.fill(UTIL.rotateTextAroundCenter(G2, "N", (int) textPoint.getX(), (int) textPoint.getY(), (2 * Math.PI - alpha)));
                    G2.setColor(FORMER_COLOR);
                } else if (counter / 2 == 9) {
                    G2.setFont(CHAR_FONT);
                    final Color FORMER_COLOR = G2.getColor();
                    G2.setColor(getPointerColor().LIGHT);
                    G2.rotate(Math.toRadians(0), CENTER.getX(), CENTER.getY());
                    G2.fill(UTIL.rotateTextAroundCenter(G2, "E", (int) textPoint.getX(), (int) textPoint.getY(), (2 * Math.PI - alpha)));
                    G2.setColor(FORMER_COLOR);
                } else if (counter / 2 == 18) {
                    G2.setFont(CHAR_FONT);
                    final Color FORMER_COLOR = G2.getColor();
                    G2.setColor(getPointerColor().LIGHT);
                    G2.rotate(Math.toRadians(0), CENTER.getX(), CENTER.getY());
                    G2.fill(UTIL.rotateTextAroundCenter(G2, "S", (int) textPoint.getX(), (int) textPoint.getY(), (2 * Math.PI - alpha)));
                    G2.setColor(FORMER_COLOR);
                } else if (counter / 2 == 27) {
                    G2.setFont(CHAR_FONT);
                    final Color FORMER_COLOR = G2.getColor();
                    G2.setColor(getPointerColor().LIGHT);
                    G2.rotate(Math.toRadians(0), CENTER.getX(), CENTER.getY());
                    G2.fill(UTIL.rotateTextAroundCenter(G2, "W", (int) textPoint.getX(), (int) textPoint.getY(), (2 * Math.PI - alpha)));
                    G2.setColor(FORMER_COLOR);
                } else {
                    G2.setFont(NUMBER_FONT);
                    G2.rotate(Math.toRadians(0), CENTER.getX(), CENTER.getY());
                    G2.fill(UTIL.rotateTextAroundCenter(G2, String.valueOf(counter / 2), (int) textPoint.getX(), (int) textPoint.getY(), (2 * Math.PI - alpha)));
                }
            }

            G2.setTransform(OLD_TRANSFORM);

            tickCounterHalf++;
            tickCounterFull++;

            counter++;
        }

        G2.dispose();

        return IMAGE;
    }

    private BufferedImage create_AIRPLANE_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        // Effect
        final Ellipse2D OVERLAY_EFFECT = new Ellipse2D.Double(IMAGE_WIDTH * 0.08411215245723724, IMAGE_HEIGHT * 0.08411215245723724, IMAGE_WIDTH * 0.8317756652832031, IMAGE_HEIGHT * 0.8317756652832031);
        final Point2D OVERLAY_EFFECT_CENTER = new Point2D.Double((0.5 * IMAGE_WIDTH), (0.5 * IMAGE_HEIGHT));
        final float[] OVERLAY_EFFECT_FRACTIONS = {
            0.0f,
            0.41f,
            0.705f,
            1.0f
        };
        final Color[] OVERLAY_EFFECT_COLORS = {
            UTIL.setAlpha(getBackgroundColor().LABEL_COLOR, 0),
            UTIL.setAlpha(getBackgroundColor().LABEL_COLOR, 0),
            UTIL.setAlpha(getBackgroundColor().LABEL_COLOR, 30),
            UTIL.setAlpha(getBackgroundColor().LABEL_COLOR, 0)
        };
        final RadialGradientPaint OVERLAY_EFFECT_GRADIENT = new RadialGradientPaint(OVERLAY_EFFECT_CENTER, (float) (0.4158878326 * IMAGE_WIDTH), OVERLAY_EFFECT_FRACTIONS, OVERLAY_EFFECT_COLORS);
        G2.setPaint(OVERLAY_EFFECT_GRADIENT);
        G2.fill(OVERLAY_EFFECT);

        // Plane holder
        final Ellipse2D PLANEHOLDER_FRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.44392523169517517, IMAGE_HEIGHT * 0.44392523169517517, IMAGE_WIDTH * 0.11214950680732727, IMAGE_HEIGHT * 0.11214950680732727);
        final Point2D PLANEHOLDER_FRAME_START = new Point2D.Double(0, PLANEHOLDER_FRAME.getBounds2D().getMinY());
        final Point2D PLANEHOLDER_FRAME_STOP = new Point2D.Double(0, PLANEHOLDER_FRAME.getBounds2D().getMaxY());
        final float[] PLANEHOLDER_FRAME_FRACTIONS = {
            0.0f,
            0.46f,
            1.0f
        };
        final Color[] PLANEHOLDER_FRAME_COLORS = {
            new Color(180, 180, 180, 255),
            new Color(63, 63, 63, 255),
            new Color(40, 40, 40, 255)
        };
        final LinearGradientPaint PLANEHOLDER_FRAME_GRADIENT = new LinearGradientPaint(PLANEHOLDER_FRAME_START, PLANEHOLDER_FRAME_STOP, PLANEHOLDER_FRAME_FRACTIONS, PLANEHOLDER_FRAME_COLORS);
        G2.setPaint(PLANEHOLDER_FRAME_GRADIENT);
        G2.fill(PLANEHOLDER_FRAME);

        final Ellipse2D GAUGE_BACKGROUND = new Ellipse2D.Double(IMAGE_WIDTH * 0.08411215245723724, IMAGE_HEIGHT * 0.08411215245723724, IMAGE_WIDTH * 0.8317756652832031, IMAGE_HEIGHT * 0.8317756652832031);
        final Ellipse2D PLANEHOLDER_MAIN = new Ellipse2D.Double(IMAGE_WIDTH * 0.44859811663627625, IMAGE_HEIGHT * 0.44859811663627625, IMAGE_WIDTH * 0.10280373692512512, IMAGE_HEIGHT * 0.10280373692512512);
        final Point2D PLANEHOLDER_MAIN_START = new Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMinY());
        final Point2D PLANEHOLDER_MAIN_STOP = new Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMaxY());
        final float[] PLANEHOLDER_MAIN_FRACTIONS = {
            0.0f,
            0.35f,
            1.0f
        };
        final Color[] PLANEHOLDER_MAIN_COLORS = {
            getBackgroundColor().GRADIENT_START_COLOR,
            getBackgroundColor().GRADIENT_FRACTION_COLOR,
            getBackgroundColor().GRADIENT_STOP_COLOR
        };
        final LinearGradientPaint PLANEHOLDER_MAIN_GRADIENT = new LinearGradientPaint(PLANEHOLDER_MAIN_START, PLANEHOLDER_MAIN_STOP, PLANEHOLDER_MAIN_FRACTIONS, PLANEHOLDER_MAIN_COLORS);
        G2.setPaint(PLANEHOLDER_MAIN_GRADIENT);
        G2.fill(PLANEHOLDER_MAIN);

        // Airplane
        final GeneralPath PLANE = new GeneralPath();
        PLANE.setWindingRule(Path2D.WIND_EVEN_ODD);
        PLANE.moveTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.2523364485981308);
        PLANE.curveTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.2523364485981308, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.2850467289719626, IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.3130841121495327);
        PLANE.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.32710280373831774, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.38317757009345793, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.38317757009345793);
        PLANE.lineTo(IMAGE_WIDTH * 0.32710280373831774, IMAGE_HEIGHT * 0.5186915887850467);
        PLANE.lineTo(IMAGE_WIDTH * 0.32710280373831774, IMAGE_HEIGHT * 0.5700934579439252);
        PLANE.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.48598130841121495);
        PLANE.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.6121495327102804);
        PLANE.lineTo(IMAGE_WIDTH * 0.4252336448598131, IMAGE_HEIGHT * 0.6635514018691588);
        PLANE.lineTo(IMAGE_WIDTH * 0.4252336448598131, IMAGE_HEIGHT * 0.7149532710280374);
        PLANE.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.6822429906542056);
        PLANE.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.6962616822429907);
        PLANE.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.6962616822429907);
        PLANE.lineTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.6822429906542056);
        PLANE.lineTo(IMAGE_WIDTH * 0.5747663551401869, IMAGE_HEIGHT * 0.7149532710280374);
        PLANE.lineTo(IMAGE_WIDTH * 0.5747663551401869, IMAGE_HEIGHT * 0.6635514018691588);
        PLANE.lineTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.6121495327102804);
        PLANE.lineTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48598130841121495);
        PLANE.lineTo(IMAGE_WIDTH * 0.6728971962616822, IMAGE_HEIGHT * 0.5700934579439252);
        PLANE.lineTo(IMAGE_WIDTH * 0.6728971962616822, IMAGE_HEIGHT * 0.5186915887850467);
        PLANE.lineTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.38317757009345793);
        PLANE.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.38317757009345793, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.32710280373831774, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.3130841121495327);
        PLANE.curveTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.2897196261682243, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.2570093457943925, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.2523364485981308);
        PLANE.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.2523364485981308, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.2336448598130841, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.2336448598130841);
        PLANE.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.16822429906542055);
        PLANE.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.2336448598130841);
        PLANE.curveTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.2336448598130841, IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.2523364485981308, IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.2523364485981308);
        PLANE.closePath();
        G2.setColor(getPointerColor().MEDIUM);
        G2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        G2.draw(PLANE);
        G2.translate(1, 2);
        G2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        G2.setColor(new Color(0.0f, 0.0f, 0.0f, 0.25f));
        G2.draw(PLANE);

        G2.dispose();

        return IMAGE;
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "AirCompass";
    }
}
