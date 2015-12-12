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
import eu.hansolo.steelseries.tools.FrameType;
import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.ease.Spline;

/**
 *
 * @author hansolo
 */
public class Horizon extends AbstractRadial {
    // <editor-fold defaultstate="collapsed" desc="Variable declaration">
    private double roll;
    private double oldRoll;
    private double pitch;
    private double oldPitch;
    private double pitchPixel;
    private boolean upsidedown = false;
    private final Point2D CENTER = new Point2D.Double();
    // Images used to combine layers for background and foreground
    private BufferedImage bImage;
    private BufferedImage fImage;
    private BufferedImage horizonImage;
    private BufferedImage horizonForegroundImage;
    private boolean customColors;
    private Color customSkyColor;
    private Color customGroundColor;
    private BufferedImage disabledImage;
    private final Ellipse2D CLIP = new Ellipse2D.Double();
    private Timeline timelineRoll = new Timeline(this);
    private Timeline timelinePitch = new Timeline(this);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public Horizon() {
        super();
        customColors = false;
        customSkyColor = new Color(127, 213, 240, 255);
        customGroundColor = new Color(60, 68, 57, 255);
        init(getInnerBounds().width, getInnerBounds().height);
        pitch = 0;
        roll = 0;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    @Override
    public final AbstractGauge init(final int WIDTH, final int HEIGHT) {
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

        // Calculate clip area
        CLIP.setFrame(GAUGE_WIDTH * 0.08411215245723724, GAUGE_WIDTH * 0.08411215245723724, GAUGE_WIDTH * 0.8317756652832031, GAUGE_WIDTH * 0.8317756652832031);

        pitchPixel = (int) (Math.PI * GAUGE_WIDTH) / 360.0;

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

        if (horizonImage != null) {
            horizonImage.flush();
        }
        horizonImage = create_HORIZON_Image(GAUGE_WIDTH);

        create_INDICATOR_Image(GAUGE_WIDTH, fImage);

        if (horizonForegroundImage != null) {
            horizonForegroundImage.flush();
        }
        horizonForegroundImage = create_HORIZON_FOREGROUND_Image(GAUGE_WIDTH);

        if (isForegroundVisible()) {
            switch (getFrameType()) {
                case SQUARE:
                    FOREGROUND_FACTORY.createLinearForeground(GAUGE_WIDTH, GAUGE_WIDTH, false, fImage);
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

        final java.awt.Graphics2D G2 = (java.awt.Graphics2D) g.create();

        CENTER.setLocation(getGaugeBounds().getCenterX(), getGaugeBounds().getCenterX());

        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Translate the coordinate system related to the insets
        G2.translate(getFramelessOffset().getX(), getFramelessOffset().getY());

        final java.awt.geom.AffineTransform OLD_TRANSFORM = G2.getTransform();
        final java.awt.Shape OLD_CLIP = G2.getClip();

        // Draw the horizon
        G2.setClip(CLIP);

        // Rotate around roll
        G2.rotate(-Math.toRadians(roll), CENTER.getX(), CENTER.getY());

        // Translate about dive
        G2.translate(0, -(pitch * pitchPixel) - getFramelessOffset().getY());

        // Draw horizon
        G2.drawImage(horizonImage, 0, (int) ((getHeight() - horizonImage.getHeight()) / 2.0), null);

        // Draw the scale and angle indicator
        G2.translate(-getFramelessOffset().getX(), (pitch * pitchPixel) + getFramelessOffset().getY());
        G2.drawImage(horizonForegroundImage, (int) (getWidth() * 0.5 - horizonForegroundImage.getWidth() / 2.0), (int) (getWidth() * 0.10747663551401869), null);

        G2.setTransform(OLD_TRANSFORM);
        G2.setClip(OLD_CLIP);

        // Draw combined background image
        G2.drawImage(bImage, 0, 0, null);

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
     * Returns the value of the roll axis (0 - 360¬∞)
     * @return the value of the roll axis (0 - 360¬∞)
     */
    public double getRoll() {
        return this.roll;
    }

    /**
     * Sets the value of the roll axis (0 - 360¬∞)
     * @param ROLL
     */
    public void setRoll(final double ROLL) {
        this.roll = ROLL % 360;
        this.oldRoll = roll;
        fireStateChanged();
        repaint();
    }

    public void setRollAnimated(final double ROLL) {
        if (isEnabled()) {
            if (timelineRoll.getState() == Timeline.TimelineState.PLAYING_FORWARD || timelineRoll.getState() == Timeline.TimelineState.PLAYING_REVERSE) {
                timelineRoll.abort();
            }
            timelineRoll = new Timeline(this);
            timelineRoll.addPropertyToInterpolate("roll", this.oldRoll, ROLL);
            timelineRoll.setEase(new Spline(0.5f));
            timelineRoll.setDuration(800);
            timelineRoll.play();
        }
    }

    /**
     * Returns the value of the current pitch
     * @return the value of the current pitch
     */
    public double getPitch() {
        return this.pitch;
    }

    /**
     * Sets the value of the current pitch
     * @param PITCH
     */
    public void setPitch(final double PITCH) {
        this.pitch = PITCH % 180;

        if (pitch > 90) {
            pitch = 90 - (pitch - 90);
            if (!upsidedown) {
                setRoll(roll - 180);
            }
            upsidedown = true;
        } else if (pitch < -90) {
            pitch = -90 + (-90 - pitch);
            if (!upsidedown) {
                setRoll(roll + 180);
            }
            upsidedown = true;
        } else {
            upsidedown = false;
            this.oldPitch = pitch;
        }
        fireStateChanged();
        repaint();
    }

    public void setPitchAnimated(final double PITCH) {
        if (isEnabled()) {
            if (timelinePitch.getState() == Timeline.TimelineState.PLAYING_FORWARD || timelinePitch.getState() == Timeline.TimelineState.PLAYING_REVERSE) {
                timelinePitch.abort();
            }
            timelinePitch = new Timeline(this);
            timelinePitch.addPropertyToInterpolate("pitch", this.oldPitch, PITCH);
            timelinePitch.setEase(new Spline(0.5f));
            timelinePitch.setDuration(800);
            timelinePitch.play();
        }
    }

    /**
     * Returns true if customized colors will be used for visualization
     * @return true if customized colors will be used for visualization
     */
    public boolean isCustomColors() {
        return customColors;
    }

    /**
     * Enables / disables the usage of custom colors for visualization
     * @param CUSTOM_COLORS
     */
    public void setCustomColors(final boolean CUSTOM_COLORS) {
        customColors = CUSTOM_COLORS;
        if (customColors) {
            init(getInnerBounds().width, getInnerBounds().height);
            repaint(getInnerBounds());
        }
    }

    /**
     * Returns the custom color that will be used for visualization of the sky
     * @return the custom color that will be used for visualization of the sky
     */
    public Color getCustomSkyColor() {
        return customSkyColor;
    }

    /**
     * Sets the custom color that will be used for visualization of the sky
     * @param CUSTOM_SKY_COLOR
     */
    public void setCustomSkyColor(final Color CUSTOM_SKY_COLOR) {
        customSkyColor = CUSTOM_SKY_COLOR;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns the custom color that will be used for visualization of the ground
     * @return the custom color that will be used for visualization of the ground
     */
    public Color getCustomGroundColor() {
        return customGroundColor;
    }

    /**
     * Sets the custom color that will be used for visualization of the ground
     * @param CUSTOM_GROUND_COLOR
     */
    public void setCustomGroundColor(final Color CUSTOM_GROUND_COLOR) {
        customGroundColor = CUSTOM_GROUND_COLOR;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    @Override
    public void setFrameType(final FrameType FRAME_TYPE) {
        super.setFrameType(FrameType.ROUND);
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
    private BufferedImage create_HORIZON_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        final int HORIZON_HEIGHT = (int) (Math.PI * WIDTH);

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, HORIZON_HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final Rectangle2D HORIZON = new Rectangle2D.Double(0, 0, IMAGE_WIDTH, HORIZON_HEIGHT);
        final Point2D HORIZON_START = new Point2D.Double(0, HORIZON.getBounds2D().getMinY());
        final Point2D HORIZON_STOP = new Point2D.Double(0, HORIZON.getBounds2D().getMaxY());
        final float[] HORIZON_FRACTIONS = {
            0.0f,
            0.49999f,
            0.5f,
            1.0f
        };
        final Color[] HORIZON_COLORS;
        if (customColors) {
            HORIZON_COLORS = new Color[]{
                customSkyColor,
                customSkyColor,
                customGroundColor,
                customGroundColor
            };
        } else {
            HORIZON_COLORS = new Color[]{
                new Color(127, 213, 240, 255),
                new Color(127, 213, 240, 255),
                new Color(60, 68, 57, 255),
                new Color(60, 68, 57, 255)
            };
        }

        final LinearGradientPaint HORIZON_GRADIENT = new LinearGradientPaint(HORIZON_START, HORIZON_STOP, HORIZON_FRACTIONS, HORIZON_COLORS);
        G2.setPaint(HORIZON_GRADIENT);
        G2.fill(HORIZON);

        // Draw horizontal lines
        G2.setColor(UTIL.setBrightness(HORIZON_COLORS[0], 0.5f));
        final Line2D LINE = new Line2D.Double();
        final double STEPSIZE_Y = HORIZON_HEIGHT / 360.0 * 5.0;
        boolean stepTen = false;
        int step = 0;
        G2.setFont(new Font("Verdana", Font.PLAIN, (int) (WIDTH * 0.04)));
        final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
        TextLayout valueLayout;
        final Rectangle2D VALUE_BOUNDARY = new Rectangle2D.Double();
        for (double y = IMAGE_HEIGHT / 2.0 - STEPSIZE_Y; y > 0; y -= STEPSIZE_Y) {
            if (step <= 80) {
                if (stepTen) {
                    LINE.setLine((IMAGE_WIDTH - (IMAGE_WIDTH * 0.2)) / 2, y, IMAGE_WIDTH - (IMAGE_WIDTH - (IMAGE_WIDTH * 0.2)) / 2, y);
                    step += 10;
                    valueLayout = new TextLayout(Integer.toString(step), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(Integer.toString(step), (float) (LINE.getX1() - VALUE_BOUNDARY.getWidth() - 5), (float) (y + VALUE_BOUNDARY.getHeight() / 2));
                    G2.drawString(Integer.toString(step), (float) (LINE.getX2() + 5), (float) (y + VALUE_BOUNDARY.getHeight() / 2));
                } else {
                    LINE.setLine((IMAGE_WIDTH - (IMAGE_WIDTH * 0.1)) / 2, y, IMAGE_WIDTH - (IMAGE_WIDTH - (IMAGE_WIDTH * 0.1)) / 2, y);
                }
                G2.draw(LINE);
            }
            stepTen ^= true;
        }
        stepTen = false;
        step = 0;
        G2.setColor(Color.WHITE);
        final Stroke FORMER_STROKE = G2.getStroke();
        G2.setStroke(new BasicStroke(1.5f));
        LINE.setLine(0, IMAGE_HEIGHT / 2.0, IMAGE_WIDTH, IMAGE_HEIGHT / 2.0);
        G2.draw(LINE);
        G2.setStroke(FORMER_STROKE);
        for (double y = IMAGE_HEIGHT / 2.0 + STEPSIZE_Y; y <= IMAGE_HEIGHT; y += STEPSIZE_Y) {
            if (step >= -80) {
                if (stepTen) {
                    LINE.setLine((IMAGE_WIDTH - (IMAGE_WIDTH * 0.2)) / 2, y, IMAGE_WIDTH - (IMAGE_WIDTH - (IMAGE_WIDTH * 0.2)) / 2, y);
                    step -= 10;
                    valueLayout = new TextLayout(Integer.toString(step), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(Integer.toString(step), (float) (LINE.getX1() - VALUE_BOUNDARY.getWidth() - 5), (float) (y + VALUE_BOUNDARY.getHeight() / 2));
                    G2.drawString(Integer.toString(step), (float) (LINE.getX2() + 5), (float) (y + VALUE_BOUNDARY.getHeight() / 2));
                } else {
                    LINE.setLine((IMAGE_WIDTH - (IMAGE_WIDTH * 0.1)) / 2, y, IMAGE_WIDTH - (IMAGE_WIDTH - (IMAGE_WIDTH * 0.1)) / 2, y);
                }
                G2.draw(LINE);
            }
            stepTen ^= true;
        }

        G2.dispose();

        return IMAGE;
    }

    private BufferedImage create_INDICATOR_Image(final int WIDTH, BufferedImage image) {
        if (WIDTH <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        if (image == null) {
            image = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        }
        final java.awt.Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();
        final Point2D LOCAL_CENTER = new Point2D.Double(IMAGE_WIDTH / 2.0, IMAGE_HEIGHT / 2.0);

        G2.setFont(new Font("Verdana", Font.PLAIN, (int) (IMAGE_WIDTH * 0.035)));
        //final java.awt.geom.Point2D TEXT_POS = new java.awt.geom.Point2D.Double(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.08878504672897196 + IMAGE_WIDTH * 0.035 / 3);

        final Line2D SCALE_MARK_SMALL = new Line2D.Double(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.08878504672897196, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.0937850467);
        final Line2D SCALE_MARK = new Line2D.Double(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.08878504672897196, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1037850467);
        final Line2D SCALE_MARK_BIG = new Line2D.Double(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.08878504672897196, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.113);
        final Stroke SMALL_STROKE = new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        final Stroke STROKE = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        final Stroke BIG_STROKE = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        final int STEP = 5;
        final AffineTransform OLD_TRANSFORM = G2.getTransform();
        G2.rotate(-Math.PI / 2, LOCAL_CENTER.getX(), LOCAL_CENTER.getY());
        for (int angle = -90; angle <= 90; angle += STEP) {
            if (angle % 45 == 0 || angle == 0) {
                //G2.fill(UTIL.rotateTextAroundCenter(G2, Integer.toString(angle), (int) TEXT_POS.getX(), (int) TEXT_POS.getY(), 0));
                G2.setColor(getPointerColor().MEDIUM);
                G2.setStroke(BIG_STROKE);
                G2.draw(SCALE_MARK_BIG);
            } else if (angle % 15 == 0) {
                G2.setColor(Color.WHITE);
                G2.setStroke(STROKE);
                G2.draw(SCALE_MARK);
            } else {
                G2.setColor(Color.WHITE);
                G2.setStroke(SMALL_STROKE);
                G2.draw(SCALE_MARK_SMALL);
            }

            G2.rotate(Math.toRadians(STEP), LOCAL_CENTER.getX(), LOCAL_CENTER.getY());
        }

        G2.setTransform(OLD_TRANSFORM);
        final GeneralPath CENTER_PLANE = new GeneralPath();
        CENTER_PLANE.setWindingRule(Path2D.WIND_EVEN_ODD);
        CENTER_PLANE.moveTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5);
        CENTER_PLANE.curveTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.5233644859813084, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5233644859813084);
        CENTER_PLANE.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.5233644859813084, IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.5);
        CENTER_PLANE.curveTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.48598130841121495, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.4766355140186916, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.4766355140186916);
        CENTER_PLANE.curveTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.4766355140186916, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.48598130841121495, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5);
        CENTER_PLANE.closePath();
        CENTER_PLANE.moveTo(IMAGE_WIDTH * 0.4158878504672897, IMAGE_HEIGHT * 0.5046728971962616);
        CENTER_PLANE.lineTo(IMAGE_WIDTH * 0.4158878504672897, IMAGE_HEIGHT * 0.4953271028037383);
        CENTER_PLANE.curveTo(IMAGE_WIDTH * 0.4158878504672897, IMAGE_HEIGHT * 0.4953271028037383, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.4953271028037383, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.4953271028037383);
        CENTER_PLANE.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.4672897196261682);
        CENTER_PLANE.curveTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.4158878504672897);
        CENTER_PLANE.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.4158878504672897);
        CENTER_PLANE.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.4672897196261682);
        CENTER_PLANE.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.4953271028037383);
        CENTER_PLANE.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.4953271028037383, IMAGE_WIDTH * 0.5841121495327103, IMAGE_HEIGHT * 0.4953271028037383, IMAGE_WIDTH * 0.5841121495327103, IMAGE_HEIGHT * 0.4953271028037383);
        CENTER_PLANE.lineTo(IMAGE_WIDTH * 0.5841121495327103, IMAGE_HEIGHT * 0.5046728971962616);
        CENTER_PLANE.curveTo(IMAGE_WIDTH * 0.5841121495327103, IMAGE_HEIGHT * 0.5046728971962616, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5046728971962616, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5046728971962616);
        CENTER_PLANE.curveTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5327102803738317);
        CENTER_PLANE.curveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5046728971962616);
        CENTER_PLANE.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5046728971962616, IMAGE_WIDTH * 0.4158878504672897, IMAGE_HEIGHT * 0.5046728971962616, IMAGE_WIDTH * 0.4158878504672897, IMAGE_HEIGHT * 0.5046728971962616);
        CENTER_PLANE.closePath();
        G2.setPaint(getPointerColor().LIGHT);
        G2.fill(CENTER_PLANE);

        G2.dispose();

        return image;
    }

    private BufferedImage create_HORIZON_FOREGROUND_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        final java.awt.image.BufferedImage IMAGE = UTIL.createImage((int) (WIDTH * 0.0373831776), (int) (WIDTH * 0.0560747664), Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        // Draw angle indicator
        final GeneralPath TRIANGLE = new GeneralPath();
        TRIANGLE.setWindingRule(Path2D.WIND_EVEN_ODD);
        TRIANGLE.moveTo(IMAGE_WIDTH * 0.5, 0);
        TRIANGLE.lineTo(0, IMAGE_HEIGHT);
        TRIANGLE.lineTo(IMAGE_WIDTH, IMAGE_HEIGHT);
        TRIANGLE.closePath();
        G2.setColor(getPointerColor().LIGHT);
        G2.fill(TRIANGLE);
        G2.setColor(getPointerColor().MEDIUM);
        G2.draw(TRIANGLE);

        G2.dispose();

        return IMAGE;
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "Horizon";
    }
}
