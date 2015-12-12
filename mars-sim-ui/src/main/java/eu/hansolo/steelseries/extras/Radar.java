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
import eu.hansolo.steelseries.tools.ConicalGradientPaint;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ConcurrentHashMap;
import org.pushingpixels.trident.Timeline;


/**
 *
 * @author hansolo
 */
public final class Radar extends AbstractRadial implements ActionListener {
    // <editor-fold defaultstate="collapsed" desc="Variable declaration">
    private final Poi MY_LOCATION = new Poi("Home", 0, 0);
    private double range = 100000;
    private static final int INITIAL_WIDTH = 200;
    private double pixelScaleX = this.range / (0.4 * INITIAL_WIDTH) / 620;
    private double pixelScaleY = this.range / (0.4 * INITIAL_WIDTH) / 1000;
    private Point2D CENTER_XY = MY_LOCATION.getLocationXY();
    private ConcurrentHashMap<String, Poi> pois = new ConcurrentHashMap<String, eu.hansolo.steelseries.extras.Poi>(64);
    private ConcurrentHashMap<String, Poi> blips = new ConcurrentHashMap<String, eu.hansolo.steelseries.extras.Poi>(64);
    private final Color BLIP_TEXT_COLOR = new Color(0x619E65);
    private final Font BLIP_FONT = new Font("Verdana", 0, 6);
    private final Line2D BEAM = new Line2D.Double(INITIAL_WIDTH / 2.0, INITIAL_WIDTH / 2.0, INITIAL_WIDTH * 0.79, INITIAL_WIDTH * 0.79);
    private final Point2D CENTER = new Point2D.Double();
    // Images used to combine layers for background and foreground
    private BufferedImage bImage;
    private BufferedImage beamImage;
    private BufferedImage disabledImage;
    private final Color BEAM_COLOR = new Color(130, 230, 150, 180);
    private double rotationAngle = 0;
    private Timeline timeline = new Timeline(this);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public Radar() {
        super();
        init(getInnerBounds().width, getInnerBounds().height);
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

        pixelScaleX = this.range / (0.4 * GAUGE_WIDTH) / 620;
        pixelScaleY = this.range / (0.4 * GAUGE_WIDTH) / 1000;

        CENTER.setLocation(getGaugeBounds().getCenterX(), getGaugeBounds().getCenterY());

        if (!isFrameVisible()) {
            setFramelessOffset(-getGaugeBounds().width * 0.0841121495, -getGaugeBounds().width * 0.0841121495);
        } else {
            setFramelessOffset(getGaugeBounds().x, getGaugeBounds().y);
        }

        // Create Background Image
        if (bImage != null) {
            bImage.flush();
        }
        bImage = UTIL.createImage(GAUGE_WIDTH, GAUGE_WIDTH, Transparency.TRANSLUCENT);

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
            create_BACKGROUND_Image(GAUGE_WIDTH, bImage);
        }

        create_TICKMARKS_Image(GAUGE_WIDTH, bImage);

        if (beamImage != null) {
            beamImage.flush();
        }
        beamImage = create_BEAM_Image(GAUGE_WIDTH);

        if (disabledImage != null) {
            disabledImage.flush();
        }
        disabledImage = create_DISABLED_Image(GAUGE_WIDTH);

        BEAM.setLine(CENTER.getX(), CENTER.getY(), GAUGE_WIDTH * 0.79, GAUGE_HEIGHT * 0.79);

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

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Translate the coordinate system related to insets
        G2.translate(getFramelessOffset().getX(), getFramelessOffset().getY());

        final AffineTransform OLD_TRANSFORM = G2.getTransform();

        // Draw combined background image
        G2.drawImage(bImage, 0, 0, null);

        // Draw blips
        G2.setColor(BLIP_TEXT_COLOR);
        G2.setFont(BLIP_FONT);
        for (Poi poi : blips.values()) {
            if (poi.distanceTo(MY_LOCATION) < this.range) {
                G2.drawImage(poi.getPoiImage(), (int) (CENTER.getX() - poi.getPoiImage().getWidth() / 2.0 + (poi.getLocationXY().getX() - CENTER_XY.getX()) / pixelScaleX), (int) (CENTER.getY() - poi.getPoiImage().getWidth() / 2.0 + (poi.getLocationXY().getY() - CENTER_XY.getY()) / pixelScaleY), null);
                G2.drawString(poi.getName(), (int) (CENTER.getX() - poi.getPoiImage().getWidth() + (poi.getLocationXY().getX() - CENTER_XY.getX()) / pixelScaleX), (int) (CENTER.getY() - poi.getPoiImage().getWidth() + (poi.getLocationXY().getY() - CENTER_XY.getY()) / pixelScaleY));
            }
        }

        // Draw the beam
        G2.rotate(rotationAngle, CENTER.getX(), CENTER.getY());
        G2.drawImage(beamImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        G2.rotate(Math.toRadians(-135) + rotationAngle, CENTER.getX(), CENTER.getY());
        G2.setColor(BEAM_COLOR);
        G2.draw(BEAM);
        G2.setTransform(OLD_TRANSFORM);

        if (!isEnabled()) {
            G2.drawImage(disabledImage, 0, 0, null);
        }

        G2.dispose();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    /**
     * Returns the rotation angle of the radar beam
     * @return the rotation angle of the radar beam
     */
    public double getRotationAngle() {
        return this.rotationAngle;
    }

    /**
     * Sets the rotation angle of the radar beam
     * @param ROTATION_ANGLE
     */
    public void setRotationAngle(final double ROTATION_ANGLE) {
        this.rotationAngle = ROTATION_ANGLE;

        repaint();
    }

    /**
     * Returns the range of the radar in meters which means
     * the distance from the center of the radar to it's
     * outer circle
     * @return the range of the radar in meters
     */
    public double getRange() {
        return this.range;
    }

    /**
     * Sets the range of the radar in meters which means
     * the distance from the center of the radar to it's
     * outer circle
     * @param RANGE
     */
    public void setRange(final double RANGE) {
        this.range = RANGE;
        checkForBlips();
        init(getInnerBounds().width, getInnerBounds().height);
        repaint();
    }

    /**
     * Returns the position of the center of the radar as
     * point of interest (poi) object with it's coordinates
     * as latitude and longitude
     * @return poi that contains the latitude and longitude of
     * the center of the radar
     */
    public Poi getMyLocation() {
        return this.MY_LOCATION;
    }

    /**
     * Defines the position of the center of the radar by the
     * coordinates of the given point of interest (poi) object
     * @param NEW_LOCATION
     */
    public void setMyLocation(final Poi NEW_LOCATION) {
        this.MY_LOCATION.setLocation(NEW_LOCATION.getLocation());
        checkForBlips();
        init(getInnerBounds().width, getInnerBounds().height);
        repaint();
    }

    /**
     * Defines the position of the center of the radar by the
     * given coordinates as latitude and longitude
     * @param LON
     * @param LAT
     */
    public void setMyLocation(final double LON, final double LAT) {
        this.MY_LOCATION.setLocation(LON, LAT);
        checkForBlips();
        init(getInnerBounds().width, getInnerBounds().height);
        repaint();
    }

    /**
     * Adds a new point of interest to the list of poi's of the radar
     * Keep in mind that only the poi's are visible as blips that are
     * in the range of the radar.
     * @param BLIP
     */
    public void addPoi(final Poi BLIP) {
        if (pois.keySet().contains(BLIP.getName())) {
            updatePoi(BLIP.getName(), BLIP.getLocation());
        } else {
            pois.put(BLIP.getName(), BLIP);
        }
        checkForBlips();
    }

    /**
     * Updates the position of the given poi by it's name (BLIP_NAME) on
     * the radar screen. This could be useful to visualize moving points.
     * Keep in mind that only the poi's are visible as blips that are
     * in the range of the radar.
     * @param BLIP_NAME
     * @param LOCATION
     */
    public void updatePoi(final String BLIP_NAME, final Point2D LOCATION) {
        if (pois.keySet().contains(BLIP_NAME)) {
            pois.get(BLIP_NAME).setLocation(LOCATION);
            checkForBlips();
        }
    }

    /**
     * Removes a point of interest from the radar
     * Keep in mind that only the poi's are visible as blips that are
     * in the range of the radar.
     * @param BLIP
     */
    public void removePoi(Poi BLIP) {
        if (pois.keySet().contains(BLIP.getName())) {
            pois.remove(BLIP.getName());
            checkForBlips();
        }
    }

    /**
     * Returns the point of interest given by it's name
     * Keep in mind that only the poi's are visible as blips that are
     * in the range of the radar.
     * @param NAME
     * @return the point of interest given by it's name
     */
    public Poi getPoi(final String NAME) {
        final Poi POINT_OF_INTEREST;
        if (pois.keySet().contains(NAME)) {
            POINT_OF_INTEREST = pois.get(NAME);
        } else {
            POINT_OF_INTEREST = null;
        }

        return POINT_OF_INTEREST;
    }

    /**
     * Animates the radar beam of the component. This has no effect
     * on the functionality but is only eye candy.
     * @param RUN enables/disables the animation of the beam
     */
    public void animate(final boolean RUN) {
        if (isEnabled()) {
            if (RUN) {
                if (timeline.getState() != Timeline.TimelineState.PLAYING_FORWARD && timeline.getState() != Timeline.TimelineState.SUSPENDED) {
                    timeline = new Timeline(this);
                    timeline.addPropertyToInterpolate("rotationAngle", this.rotationAngle, 2 * Math.PI);
                    timeline.setEase(new org.pushingpixels.trident.ease.Linear());
                    timeline.setDuration((long) (5000));
                    timeline.playLoop(Timeline.RepeatBehavior.LOOP);
                } else if (timeline.getState() == Timeline.TimelineState.SUSPENDED) {
                    timeline.resume();
                }
            } else {
                timeline.suspend();
            }
        }
    }

    /**
     * Checks for poi's in the range of the radar
     */
    private void checkForBlips() {
        blips.clear();
        for (Poi poi : pois.values()) {
            if (poi.distanceTo(MY_LOCATION) < this.range) {
                if (!blips.keySet().contains(poi.getName())) {
                    blips.put(poi.getName(), poi);
                }
            }
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
        //G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        //G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        //G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        //G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        final Ellipse2D E_GAUGE_BACKGROUND = new Ellipse2D.Double(IMAGE_WIDTH * 0.08411215245723724, IMAGE_HEIGHT * 0.08411215245723724, IMAGE_WIDTH * 0.8317756652832031, IMAGE_HEIGHT * 0.8317756652832031);
        final Point2D E_GAUGE_BACKGROUND_START = new Point2D.Double(0, E_GAUGE_BACKGROUND.getBounds2D().getMinY());
        final Point2D E_GAUGE_BACKGROUND_STOP = new Point2D.Double(0, E_GAUGE_BACKGROUND.getBounds2D().getMaxY());
        final float[] E_GAUGE_BACKGROUND_FRACTIONS = {
            0.0f,
            1.0f
        };
        final Color[] E_GAUGE_BACKGROUND_COLORS = {
            new Color(0x001F04),
            new Color(0x013505)
        };
        final LinearGradientPaint E_GAUGE_BACKGROUND_GRADIENT = new LinearGradientPaint(E_GAUGE_BACKGROUND_START, E_GAUGE_BACKGROUND_STOP, E_GAUGE_BACKGROUND_FRACTIONS, E_GAUGE_BACKGROUND_COLORS);
        G2.setPaint(E_GAUGE_BACKGROUND_GRADIENT);
        G2.fill(E_GAUGE_BACKGROUND);

        final Ellipse2D E_GAUGE_INNERSHADOW = new Ellipse2D.Double(IMAGE_WIDTH * 0.08411215245723724, IMAGE_HEIGHT * 0.08411215245723724, IMAGE_WIDTH * 0.8317756652832031, IMAGE_HEIGHT * 0.8317756652832031);
        final Point2D E_GAUGE_INNERSHADOW_CENTER = new Point2D.Double((0.5 * IMAGE_WIDTH), (0.5 * IMAGE_HEIGHT));
        final float[] E_GAUGE_INNERSHADOW_FRACTIONS = {
            0.0f,
            0.7f,
            0.71f,
            1.0f
        };
        final Color[] E_GAUGE_INNERSHADOW_COLORS = {
            new Color(0, 90, 40, 0),
            new Color(0, 90, 40, 0),
            new Color(0, 90, 40, 0),
            new Color(0, 90, 40, 76)
        };
        final RadialGradientPaint E_GAUGE_INNERSHADOW_GRADIENT = new RadialGradientPaint(E_GAUGE_INNERSHADOW_CENTER, (float) (0.4158878504672897 * IMAGE_WIDTH), E_GAUGE_INNERSHADOW_FRACTIONS, E_GAUGE_INNERSHADOW_COLORS);
        G2.setPaint(E_GAUGE_INNERSHADOW_GRADIENT);
        G2.fill(E_GAUGE_INNERSHADOW);

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
        G2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        final int IMAGE_WIDTH = image.getWidth();

        final AffineTransform OLD_TRANSFORM = G2.getTransform();

        final BasicStroke THIN_STROKE = new BasicStroke(0.00390625f * IMAGE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        final Font SMALL_FONT = new Font("Verdana", 0, (int) (0.02f * IMAGE_WIDTH));
        final float TEXT_DISTANCE = 0.040f * IMAGE_WIDTH;
        final float MIN_LENGTH = 0.015625f * IMAGE_WIDTH;
        final float MED_LENGTH = 0.0234375f * IMAGE_WIDTH;
        final float MAX_LENGTH = 0.03125f * IMAGE_WIDTH;

        final Color TEXT_COLOR = new Color(0x619E65);
        final Color TICK_COLOR = new Color(0x619E65);
        final Color TICK_30_COLOR = new Color(86, 119, 92, 100);

        // Create the watch itself
        final float RADIUS = IMAGE_WIDTH * 0.4f;
        final Point2D TICKMARKS_CENTER = new Point2D.Double(IMAGE_WIDTH / 2.0f, IMAGE_WIDTH / 2.0f);

        // Draw ticks
        Point2D innerPoint;
        Point2D outerPoint;
        Point2D textPoint = null;
        Line2D tick;
        int tickCounter90 = 0;
        int tickCounter30 = 0;
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
                innerPoint = new Point2D.Double(TICKMARKS_CENTER.getX() + (RADIUS - MIN_LENGTH) * sinValue, TICKMARKS_CENTER.getY() + (RADIUS - MIN_LENGTH) * cosValue);
                outerPoint = new Point2D.Double(TICKMARKS_CENTER.getX() + RADIUS * sinValue, TICKMARKS_CENTER.getY() + RADIUS * cosValue);
                // Draw ticks
                tick = new Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);

                tickCounter5 = 0;
            }

            // Different tickmark every 15 units
            if (tickCounter15 == 15) {
                G2.setStroke(THIN_STROKE);
                G2.setColor(TICK_COLOR);
                innerPoint = new Point2D.Double(TICKMARKS_CENTER.getX() + (RADIUS - MED_LENGTH) * sinValue, TICKMARKS_CENTER.getY() + (RADIUS - MED_LENGTH) * cosValue);
                outerPoint = new Point2D.Double(TICKMARKS_CENTER.getX() + RADIUS * sinValue, TICKMARKS_CENTER.getY() + RADIUS * cosValue);

                // Draw ticks
                tick = new Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);

                tickCounter15 = 0;
                tickCounter90 += 15;
            }

            // Different tickmark every 30 units
            if (tickCounter30 == 30) {
                G2.setStroke(THIN_STROKE);
                G2.setColor(TICK_30_COLOR);
                innerPoint = new Point2D.Double(TICKMARKS_CENTER.getX(), TICKMARKS_CENTER.getY());
                outerPoint = new Point2D.Double(TICKMARKS_CENTER.getX() + (RADIUS - TEXT_DISTANCE * 1.5f) * sinValue, TICKMARKS_CENTER.getY() + (RADIUS - TEXT_DISTANCE * 1.5f) * cosValue);

                // Draw ticks
                tick = new Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);

                tickCounter30 = 0;
                tickCounter90 += 30;
            }

            // Different tickmark every 90 units plus text
            if (tickCounter90 == 90) {
                G2.setStroke(THIN_STROKE);
                innerPoint = new Point2D.Double(TICKMARKS_CENTER.getX() + (RADIUS - MAX_LENGTH) * sinValue, TICKMARKS_CENTER.getY() + (RADIUS - MAX_LENGTH) * cosValue);
                outerPoint = new Point2D.Double(TICKMARKS_CENTER.getX() + RADIUS * sinValue, TICKMARKS_CENTER.getY() + RADIUS * cosValue);

                // Draw ticks
                G2.setColor(TICK_COLOR);
                tick = new Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);

                tickCounter90 = 0;
            }

            // Draw text
            G2.setFont(SMALL_FONT);
            G2.setColor(TEXT_COLOR);
            textPoint = new Point2D.Double(TICKMARKS_CENTER.getX() + (RADIUS - TEXT_DISTANCE) * sinValue, TICKMARKS_CENTER.getY() + (RADIUS - TEXT_DISTANCE) * cosValue);
            if (counter != 360 && counter % 30 == 0) {
                G2.rotate(Math.toRadians(180), TICKMARKS_CENTER.getX(), TICKMARKS_CENTER.getY());
                G2.fill(UTIL.rotateTextAroundCenter(G2, String.valueOf(counter), (int) textPoint.getX(), (int) textPoint.getY(), (Math.PI - alpha)));
            }

            G2.setTransform(OLD_TRANSFORM);

            tickCounter5++;
            tickCounter15++;
            tickCounter30++;

            counter++;
        }

        // Draw distance rings
        final double RADIUS_STEP = RADIUS / 5.0;
        for (int i = 1; i < 6; i++) {
            G2.setColor(TICK_30_COLOR);
            G2.draw(new Ellipse2D.Double(TICKMARKS_CENTER.getX() - (i * RADIUS_STEP), TICKMARKS_CENTER.getY() - (i * RADIUS_STEP), i * 2 * RADIUS_STEP, i * 2 * RADIUS_STEP));
            if (i < 5) {
                G2.setColor(TICK_COLOR);
                G2.drawString(String.valueOf((int) (this.range / 5000 * i)), (int) TICKMARKS_CENTER.getX() + 2, (int) (TICKMARKS_CENTER.getY() - RADIUS_STEP * i - 1));
            }
        }

        G2.dispose();

        return image;
    }

    private BufferedImage create_BEAM_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final Point2D BEAM_CENTER = new Point2D.Double(IMAGE_WIDTH / 2.0, IMAGE_HEIGHT / 2.0);

        final float[] BEAMAREA_FRACTIONS = {
            0.0f,
            0.001f,
            180.0f,
            360.0f
        };
        final Color[] BEAMAREA_COLORS = {
            new Color(55, 178, 72, 100),
            new Color(0.0f, 0.5f, 0.0f, 0.0f),
            new Color(0.0f, 0.5f, 0.0f, 0.0f),
            new Color(55, 178, 72, 100)
        };

        final Ellipse2D BEAM_AREA = new Ellipse2D.Double(IMAGE_WIDTH * 0.08411215245723724, IMAGE_HEIGHT * 0.08411215245723724, IMAGE_WIDTH * 0.8317756652832031, IMAGE_HEIGHT * 0.8317756652832031);

        final ConicalGradientPaint BEAM_GRADIENT = new ConicalGradientPaint(true, BEAM_CENTER, 0, BEAMAREA_FRACTIONS, BEAMAREA_COLORS);
        G2.setPaint(BEAM_GRADIENT);
        G2.fill(BEAM_AREA);

        G2.dispose();

        return IMAGE;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ActionListener">
    @Override
    public void actionPerformed(ActionEvent event) {
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "Radar";
    }
}
