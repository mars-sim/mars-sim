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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.mars_sim.msp.ui.steelseries.tools.ColorDef;
import org.mars_sim.msp.ui.steelseries.tools.CustomColorDef;
import org.mars_sim.msp.ui.steelseries.tools.GaugeType;
import org.mars_sim.msp.ui.steelseries.tools.Model;
import org.mars_sim.msp.ui.steelseries.tools.Orientation;
import org.mars_sim.msp.ui.steelseries.tools.PointerType;
import org.mars_sim.msp.ui.steelseries.tools.PostPosition;
import org.mars_sim.msp.ui.steelseries.tools.Section;


/**
 *
 * @author hansolo
 */
public final class Radial1Vertical extends AbstractRadial {
    // <editor-fold defaultstate="collapsed" desc="Variable declarations">
    private static final int BASE = 10;
    private final Point2D CENTER;
    private final Point2D TRACK_OFFSET;
    private BufferedImage bImage;
    private BufferedImage fImage;
    private BufferedImage glowImageOff;
    private BufferedImage glowImageOn;
    private BufferedImage postsImage;
    private BufferedImage trackImage;
    private BufferedImage tickmarksImage;
    private BufferedImage pointerImage;
    private BufferedImage pointerShadowImage;
    private BufferedImage thresholdImage;
    private BufferedImage minMeasuredImage;
    private BufferedImage maxMeasuredImage;
    private BufferedImage disabledImage;
    private double angle = 0;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public Radial1Vertical() {
        super();
        CENTER = new Point2D.Double();
        TRACK_OFFSET = new Point2D.Double();
        setLedPosition(0.455, 0.51);
        setUserLedPosition(0.455, 0.58);
        setOrientation(Orientation.NORTH);
        setGaugeType(GaugeType.TYPE5);
        init(getInnerBounds().width, getInnerBounds().height);
    }

    public Radial1Vertical(final Model MODEL) {
        super();
        CENTER = new Point2D.Double();
        TRACK_OFFSET = new Point2D.Double();
        setModel(MODEL);
        setGaugeType(GaugeType.TYPE5);
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

        CENTER.setLocation(getGaugeBounds().getCenterX() - getInsets().left, getGaugeBounds().height * 0.73 - getInsets().top);

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

        // Create Foreground Image
        if (fImage != null) {
            fImage.flush();
        }
        fImage = UTIL.createImage(GAUGE_WIDTH, GAUGE_WIDTH, Transparency.TRANSLUCENT);

        if (isFrameVisible()) {
            switch (getFrameType()) {
                case ROUND:
                    FRAME_FACTORY.createRadialFrame(GAUGE_WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameBaseColor(), isFrameBaseColorEnabled(), getFrameEffect(), bImage);
                    break;
                case SQUARE:
                    FRAME_FACTORY.createLinearFrame(GAUGE_WIDTH, GAUGE_WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameBaseColor(), isFrameBaseColorEnabled(), getFrameEffect(), bImage);
                    break;
                default:
                    FRAME_FACTORY.createRadialFrame(GAUGE_WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameBaseColor(), isFrameBaseColorEnabled(), getFrameEffect(), bImage);
                    break;
            }
        }

        if (isBackgroundVisible()) {
            create_BACKGROUND_Image(GAUGE_WIDTH, "", "", bImage);
        }

        if (isGlowVisible()) {
            if (glowImageOff != null) {
                glowImageOff.flush();
            }
            glowImageOff = create_GLOW_Image(GAUGE_WIDTH, getGlowColor(), false, getGaugeType(), true, getOrientation());
            if (glowImageOn != null) {
                glowImageOn.flush();
            }
            glowImageOn = create_GLOW_Image(GAUGE_WIDTH, getGlowColor(), true, getGaugeType(), true, getOrientation());
        } else {
            setGlowPulsating(false);
        }

        if (postsImage != null) {
            postsImage.flush();
        }
        if (getPostsVisible()) {
            postsImage = create_POSTS_Image(GAUGE_WIDTH, PostPosition.LOWER_CENTER, PostPosition.SMALL_GAUGE_MIN_LEFT, PostPosition.SMALL_GAUGE_MAX_RIGHT);
        } else {
            postsImage = create_POSTS_Image(GAUGE_WIDTH, PostPosition.LOWER_CENTER);
        }

        if (trackImage != null) {
            trackImage.flush();
        }
        TRACK_OFFSET.setLocation(0, 0);
        trackImage = create_TRACK_Image(GAUGE_WIDTH, Math.PI / 2, getTickmarkOffset() - Math.PI / 4, getMinValue(), getMaxValue(), getAngleStep(), getTrackStart(), getTrackSection(), getTrackStop(), getTrackStartColor(), getTrackSectionColor(), getTrackStopColor(), 0.44f, CENTER, getTickmarkDirection(), TRACK_OFFSET);

        if (tickmarksImage != null) {
            tickmarksImage.flush();
        }

        tickmarksImage = TICKMARK_FACTORY.create_RADIAL_TICKMARKS_Image(GAUGE_WIDTH,
                                                                        getModel().getNiceMinValue(),
                                                                        getModel().getNiceMaxValue(),
                                                                        getModel().getMaxNoOfMinorTicks(),
                                                                        getModel().getMaxNoOfMajorTicks(),
                                                                        getModel().getMinorTickSpacing(),
                                                                        getModel().getMajorTickSpacing(),
                                                                        getGaugeType(),
                                                                        getMinorTickmarkType(),
                                                                        getMajorTickmarkType(),
                                                                        isTickmarksVisible(),
                                                                        isTicklabelsVisible(),
                                                                        getModel().isMinorTickmarksVisible(),
                                                                        getModel().isMajorTickmarksVisible(),
                                                                        getLabelNumberFormat(),
                                                                        isTickmarkSectionsVisible(),
                                                                        getBackgroundColor(),
                                                                        getTickmarkColor(),
                                                                        isTickmarkColorFromThemeEnabled(),
                                                                        getTickmarkSections(),
                                                                        isSectionTickmarksOnly(),
                                                                        getSections(),
                                                                        0.44f,
                                                                        -0.04f,
                                                                        CENTER,
                                                                        new Point2D.Double(0, 0),
                                                                        getOrientation(),
                                                                        getModel().getTicklabelOrientation(),
                                                                        getModel().isNiceScale(),
                                                                        getModel().isLogScale(),
                                                                        null);

        if (pointerImage != null) {
            pointerImage.flush();
        }
        pointerImage = create_POINTER_Image(GAUGE_WIDTH, getPointerType());

        if (pointerShadowImage != null) {
            pointerShadowImage.flush();
        }
        if (getModel().isPointerShadowVisible()) {
            pointerShadowImage = create_POINTER_SHADOW_Image(GAUGE_WIDTH, getPointerType());
        } else {
            pointerShadowImage = null;
        }

        if (thresholdImage != null) {
            thresholdImage.flush();
        }
        thresholdImage = create_THRESHOLD_Image(GAUGE_WIDTH);

        if (minMeasuredImage != null) {
            minMeasuredImage.flush();
        }
        minMeasuredImage = create_MEASURED_VALUE_Image(GAUGE_WIDTH, new Color(0, 23, 252, 255));

        if (maxMeasuredImage != null) {
            maxMeasuredImage.flush();
        }
        maxMeasuredImage = create_MEASURED_VALUE_Image(GAUGE_WIDTH, new Color(252, 29, 0, 255));

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

        CENTER.setLocation(getGaugeBounds().getCenterX(), getGaugeBounds().getCenterY());

        if (!getAreas().isEmpty()) {
            createAreas(bImage);
        }

        if (!getSections().isEmpty()) {
            createSections(bImage);
        }

        setCurrentLedImage(getLedImageOff());

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

        // Translate coordinate system related to insets
        G2.translate(getFramelessOffset().getX(), getFramelessOffset().getY());

        final AffineTransform OLD_TRANSFORM = G2.getTransform();

        G2.drawImage(bImage, 0, 0, null);

        // Highlight active area
        if (isHighlightArea()) {
            switch (getOrientation()) {
                case EAST:
                    break;
                case SOUTH:
                    break;
                case WEST:
                    G2.translate(CENTER.getX() / 2.2, 0);
                    break;
                case NORTH:

                default:
                    G2.translate(0, CENTER.getY() / 2.2);
                    break;
            }

            for(Section area : getAreas()) {
                if (area.contains(getValue())) {
                    G2.setColor(area.getHighlightColor());
                    G2.fill(area.getFilledArea());
                    break;
                }
            }
            G2.setTransform(OLD_TRANSFORM);
        }

        // Highlight active section
        if (isHighlightSection()) {

            switch (getOrientation()) {
                case EAST:
                    break;
                case SOUTH:
                    break;
                case WEST:
                    G2.translate(CENTER.getX() / 2.2, 0);
                    break;
                case NORTH:

                default:
                    G2.translate(0, CENTER.getY() / 2.2);
                    break;
            }

            for(Section section : getSections()) {
                if (section.contains(getValue())) {
                    G2.setColor(section.getHighlightColor());
                    G2.fill(section.getSectionArea());
                    break;
                }
            }
            G2.setTransform(OLD_TRANSFORM);
        }

        // Draw title
        final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
        if (!getTitle().isEmpty()) {
            if (isLabelColorFromThemeEnabled()) {
                G2.setColor(getBackgroundColor().LABEL_COLOR);
            } else {
                G2.setColor(getLabelColor());
            }
            G2.setFont(new Font("Verdana", 0, (int) (0.04672897196261682 * getGaugeBounds().width)));
            final TextLayout TITLE_LAYOUT = new TextLayout(getTitle(), G2.getFont(), RENDER_CONTEXT);
            final Rectangle2D TITLE_BOUNDARY = TITLE_LAYOUT.getBounds();
            G2.drawString(getTitle(), (float) ((getGaugeBounds().width - TITLE_BOUNDARY.getWidth()) / 2.0), 0.4f * getGaugeBounds().width + TITLE_LAYOUT.getAscent() - TITLE_LAYOUT.getDescent());
        }

        // Draw unit string
        if (!getUnitString().isEmpty()) {
            if (isLabelColorFromThemeEnabled()) {
                G2.setColor(getBackgroundColor().LABEL_COLOR);
            } else {
                G2.setColor(getLabelColor());
            }
            G2.setFont(new Font("Verdana", 0, (int) (0.04672897196261682 * getGaugeBounds().width)));
            final TextLayout UNIT_LAYOUT = new TextLayout(getUnitString(), G2.getFont(), RENDER_CONTEXT);
            final Rectangle2D UNIT_BOUNDARY = UNIT_LAYOUT.getBounds();
            G2.drawString(getUnitString(), (float) ((getGaugeBounds().width - UNIT_BOUNDARY.getWidth()) / 2.0), 0.47f * getGaugeBounds().width + UNIT_LAYOUT.getAscent() - UNIT_LAYOUT.getDescent());
        }

        switch (getOrientation()) {
            case NORTH:
                break;
            case EAST:
                break;
            case SOUTH:
                break;
            case WEST:
                G2.rotate(-Math.PI / 2.0, CENTER.getX(), CENTER.getY());
                break;
            default:
                break;
        }

        final AffineTransform FORMER_TRANSFORM = G2.getTransform();

        // Draw the track
        if (isTrackVisible()) {
            G2.drawImage(trackImage, 0, 0, null);
        }

        // Draw the tickmarks
        G2.drawImage(tickmarksImage, 0, 0, null);

        // Draw threshold indicator
        if (isThresholdVisible()) {
            if (!isLogScale()) {
                G2.rotate(getGaugeType().ROTATION_OFFSET + (getThreshold() - getMinValue()) * getAngleStep(), CENTER.getX(), getGaugeBounds().width * 0.7336448598);
            } else {
                G2.rotate(getGaugeType().ROTATION_OFFSET + UTIL.logOfBase(BASE, getThreshold() - getMinValue()) * getLogAngleStep(), CENTER.getX(), getGaugeBounds().width * 0.7336448598);
            }
            G2.drawImage(thresholdImage, 0, 0, null);
            G2.setTransform(FORMER_TRANSFORM);
        }

        // Draw min measured value indicator
        if (isMinMeasuredValueVisible()) {
            if (!isLogScale()) {
                G2.rotate(getGaugeType().ROTATION_OFFSET + (getMinMeasuredValue() - getMinValue()) * getAngleStep(), CENTER.getX(), getGaugeBounds().width * 0.7336448598);
            } else {
                G2.rotate(getGaugeType().ROTATION_OFFSET + UTIL.logOfBase(BASE, getMinMeasuredValue() - getMinValue()) * getLogAngleStep(), CENTER.getX(), getGaugeBounds().width * 0.7336448598);
            }
            G2.drawImage(minMeasuredImage, 0, 0, null);
            G2.setTransform(FORMER_TRANSFORM);
        }

        // Draw max measured value indicator
        if (isMaxMeasuredValueVisible()) {
            if (!isLogScale()) {
                G2.rotate(getGaugeType().ROTATION_OFFSET + (getMaxMeasuredValue() - getMinValue()) * getAngleStep(), CENTER.getX(), getGaugeBounds().width * 0.7336448598);
            } else {
                G2.rotate(getGaugeType().ROTATION_OFFSET + UTIL.logOfBase(BASE, getMaxMeasuredValue() - getMinValue()) * getLogAngleStep(), CENTER.getX(), getGaugeBounds().width * 0.7336448598);
            }
            G2.drawImage(maxMeasuredImage, 0, 0, null);
            G2.setTransform(FORMER_TRANSFORM);
        }

        // Draw LED if enabled
        if (isLedVisible()) {
            G2.setTransform(OLD_TRANSFORM);
            G2.drawImage(getCurrentLedImage(), (int) (getGaugeBounds().width * getLedPosition().getX()), (int) (getGaugeBounds().width * getLedPosition().getY()), null);
            G2.setTransform(FORMER_TRANSFORM);
        }

        // Draw user LED if enabled
        if (isUserLedVisible()) {
            G2.setTransform(OLD_TRANSFORM);
            G2.drawImage(getCurrentUserLedImage(), (int) (getGaugeBounds().width * getUserLedPosition().getX()), (int) (getGaugeBounds().width * getUserLedPosition().getY()), null);
            G2.setTransform(FORMER_TRANSFORM);
        }

        // Draw the pointer
        if (!isLogScale()) {
            angle = getGaugeType().ROTATION_OFFSET + (getValue() - getMinValue()) * getAngleStep();
        } else {
            angle = getGaugeType().ROTATION_OFFSET + UTIL.logOfBase(BASE, getValue() - getMinValue()) * getLogAngleStep();
        }
        //G2.rotate(ANGLE + (Math.cos(Math.toRadians(ANGLE - ROTATION_OFFSET - 91.5))), CENTER.getX(), backgroundImage.getHeight() * 0.7336448598);
        G2.rotate(angle, CENTER.getX(), getGaugeBounds().height * 0.7336448598 + 2);
        G2.drawImage(pointerShadowImage, 0, 0, null);
        G2.setTransform(FORMER_TRANSFORM);
        G2.rotate(angle, CENTER.getX(), getGaugeBounds().height * 0.7336448598);
        G2.drawImage(pointerImage, 0, 0, null);
        G2.setTransform(FORMER_TRANSFORM);

        // Draw posts
        G2.drawImage(postsImage, 0, 0, null);

        switch (getOrientation()) {
            case NORTH:
                break;
            case EAST:
                break;
            case SOUTH:
                break;
            case WEST:
                G2.setTransform(OLD_TRANSFORM);
                break;
            default:
                break;
        }

        G2.drawImage(fImage, 0, 0, null);

        // Draw glow indicator
        if (isGlowVisible()) {
            if (isGlowing()) {
                G2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getGlowAlpha()));
                G2.drawImage(glowImageOn, 0, 0, null);
                G2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            } else
            {
                G2.drawImage(glowImageOff, 0, 0, null);
            }
        }

        if (!isEnabled()) {
            G2.drawImage(disabledImage, 0, 0, null);
        }

        G2.dispose();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    /**
     * Sets the orientation of the gauge as an int value that is defined in javax.swing.SwingUtilities
     * 1 => NORTH => Pointer rotation center placed on the bottom of the gauge
     * 3 => EAST  => Pointer rotation center placed on the left side of the gauge
     * 5 => SOUTH => Pointer rotation center placed on the top of the gauge
     * 7 => WEST  => Pointer rotation center placed on the left side of the gauge
     * @param ORIENTATION
     */
    @Override
    public void setOrientation(final Orientation ORIENTATION) {
        super.setOrientation(ORIENTATION);
        switch (ORIENTATION) {
            case NORTH:
                setLedPosition(0.455, 0.51);
                setUserLedPosition(0.455, 0.58);
                break;
            case EAST:
                break;
            case SOUTH:
                break;
            case WEST:
                setLedPosition(0.455, 0.51);
                setUserLedPosition(0.455, 0.58);
                break;
            default:
                setLedPosition(0.455, 0.51);
                setUserLedPosition(0.455, 0.58);
                break;
        }
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    @Override
    public Point2D getCenter() {
        return new Point2D.Double(bImage.getWidth() / 2.0, bImage.getHeight() / 2.0);
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

    // <editor-fold defaultstate="collapsed" desc="Areas related">
    private void createAreas(final BufferedImage IMAGE) {
        final double ORIGIN_CORRECTION;
        final double ANGLE_STEP;

        switch (getOrientation()) {
            case NORTH:
                ORIGIN_CORRECTION = 135;
                break;
            case EAST:
                ORIGIN_CORRECTION = 135;
                break;
            case SOUTH:
                ORIGIN_CORRECTION = 135;
                break;
            case WEST:
                ORIGIN_CORRECTION = 225;
                break;
            default:
                ORIGIN_CORRECTION = 135;
                break;
        }

        if (bImage != null) {
            if (!isLogScale()) {
                ANGLE_STEP = Math.toDegrees(getGaugeType().ANGLE_RANGE) / (getMaxValue() - getMinValue());
            } else {
                ANGLE_STEP = Math.toDegrees(getGaugeType().ANGLE_RANGE) / UTIL.logOfBase(BASE, getMaxValue() - getMinValue());
            }

            if (bImage != null && !getAreas().isEmpty()) {
                final double OUTER_RADIUS = bImage.getWidth() * 0.44f;
                final double RADIUS;
                if (isSectionsVisible()) {
                    RADIUS = isExpandedSectionsEnabled() ? OUTER_RADIUS - bImage.getWidth() * 0.12f : OUTER_RADIUS - bImage.getWidth() * 0.04f;
                } else {
                    RADIUS = OUTER_RADIUS;
                }
                final double FREE_AREA = bImage.getWidth() / 2.0 - RADIUS;
                final Rectangle2D AREA_FRAME = new Rectangle2D.Double(bImage.getMinX() + FREE_AREA, bImage.getMinY() + FREE_AREA, 2 * RADIUS, 2 * RADIUS);

                for (Section area : getAreas()) {
                    if (!isLogScale()) {
                        area.setFilledArea(new Arc2D.Double(AREA_FRAME, ORIGIN_CORRECTION - (area.getStart() * ANGLE_STEP) + (getMinValue() * ANGLE_STEP), -(area.getStop() - area.getStart()) * ANGLE_STEP, Arc2D.PIE));
                    } else {
                        area.setFilledArea(new Arc2D.Double(AREA_FRAME, ORIGIN_CORRECTION - (UTIL.logOfBase(BASE, area.getStart()) * ANGLE_STEP) + (UTIL.logOfBase(BASE, getMinValue()) * ANGLE_STEP), -UTIL.logOfBase(BASE, area.getStop() - area.getStart()) * ANGLE_STEP, Arc2D.PIE));
                    }
                }
            }
            // Draw the areas
            if (isAreasVisible() && IMAGE != null) {
                final Graphics2D G2 = IMAGE.createGraphics();
                G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                switch (getOrientation()) {
                    case EAST:
                        break;
                    case SOUTH:
                        break;
                    case WEST:
                        G2.translate(CENTER.getX() / 2.2, 0);
                        break;
                    case NORTH:

                    default:
                        G2.translate(0, CENTER.getY() / 2.2);
                        break;
                }

                for (Section area : getAreas()) {
                    G2.setColor(isTransparentAreasEnabled() ? area.getTransparentColor() : area.getColor());
                    G2.fill(area.getFilledArea());
                }
                G2.dispose();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Sections related">
    private void createSections(final BufferedImage IMAGE) {
        if (bImage != null) {
            final double ORIGIN_CORRECTION;
            switch (getOrientation()) {
                case NORTH:
                    ORIGIN_CORRECTION = 135;
                    break;
                case EAST:
                    ORIGIN_CORRECTION = 135;
                    break;
                case SOUTH:
                    ORIGIN_CORRECTION = 135;
                    break;
                case WEST:
                    ORIGIN_CORRECTION = 225;
                    break;
                default:
                    ORIGIN_CORRECTION = 135;
                    break;
            }

            final double ANGLE_STEP;
            if (!isLogScale()) {
                ANGLE_STEP = getGaugeType().APEX_ANGLE / (getMaxValue() - getMinValue());
            } else {
                ANGLE_STEP = getGaugeType().APEX_ANGLE / UTIL.logOfBase(BASE, getMaxValue() - getMinValue());
            }

            final double OUTER_RADIUS = bImage.getWidth() * 0.44f;
            final double INNER_RADIUS = isExpandedSectionsEnabled() ? OUTER_RADIUS - bImage.getWidth() * 0.12f : OUTER_RADIUS - bImage.getWidth() * 0.04f;

            final double FREE_AREA_OUTER_RADIUS = bImage.getWidth() / 2.0 - OUTER_RADIUS;
            final double FREE_AREA_INNER_RADIUS = bImage.getWidth() / 2.0 - INNER_RADIUS;
            final Ellipse2D INNER = new Ellipse2D.Double(bImage.getMinX() + FREE_AREA_INNER_RADIUS, bImage.getMinY() + FREE_AREA_INNER_RADIUS, 2 * INNER_RADIUS, 2 * INNER_RADIUS);


            for (Section section : getSections()) {
                final double ANGLE_START;
                final double ANGLE_EXTEND;

                if (!isLogScale()) {
                    ANGLE_START = ORIGIN_CORRECTION - (section.getStart() * ANGLE_STEP) + (getMinValue() * ANGLE_STEP);
                    ANGLE_EXTEND = -(section.getStop() - section.getStart()) * ANGLE_STEP;
                } else {
                    ANGLE_START = ORIGIN_CORRECTION - UTIL.logOfBase(BASE, section.getStart()) * ANGLE_STEP + (UTIL.logOfBase(BASE, getMinValue()) * ANGLE_STEP);
                    ANGLE_EXTEND = -(section.getStop() - section.getStart()) * ANGLE_STEP;
                }

                final java.awt.geom.Arc2D OUTER_ARC = new java.awt.geom.Arc2D.Double(java.awt.geom.Arc2D.PIE);
                OUTER_ARC.setFrame(bImage.getMinX() + FREE_AREA_OUTER_RADIUS, bImage.getMinY() + FREE_AREA_OUTER_RADIUS, 2 * OUTER_RADIUS, 2 * OUTER_RADIUS);
                OUTER_ARC.setAngleStart(ANGLE_START);
                OUTER_ARC.setAngleExtent(ANGLE_EXTEND);
                final java.awt.geom.Area SECTION = new java.awt.geom.Area(OUTER_ARC);

                SECTION.subtract(new java.awt.geom.Area(INNER));

                section.setSectionArea(SECTION);
            }

            // Draw the sections
            if (isSectionsVisible() && IMAGE != null) {
                final Graphics2D G2 = IMAGE.createGraphics();
                G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                switch (getOrientation()) {
                    case EAST:
                        break;
                    case SOUTH:
                        break;
                    case WEST:
                        G2.translate(CENTER.getX() / 2.2, 0);
                        break;
                    case NORTH:

                    default:
                        G2.translate(0, CENTER.getY() / 2.2);
                        break;
                }

                for (Section section : getSections()) {
                    G2.setColor(isTransparentAreasEnabled() ? section.getTransparentColor() : section.getColor());
                    G2.fill(section.getSectionArea());
//                    if (section3DEffectVisible)
//                    {
//                        G2.setPaint(section3DEffect);
//                        G2.fill(section.getSectionArea());
//                    }
                }
                G2.dispose();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image related">
    @Override
    protected BufferedImage create_THRESHOLD_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        //G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final GeneralPath THRESHOLD = new GeneralPath();
        THRESHOLD.setWindingRule(Path2D.WIND_EVEN_ODD);
        THRESHOLD.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.3333333333333333);
        THRESHOLD.lineTo(IMAGE_WIDTH * 0.4866666666666667, IMAGE_HEIGHT * 0.37333333333333335);
        THRESHOLD.lineTo(IMAGE_WIDTH * 0.52, IMAGE_HEIGHT * 0.37333333333333335);
        THRESHOLD.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.3333333333333333);
        THRESHOLD.closePath();
        final Point2D THRESHOLD_START = new Point2D.Double(0, THRESHOLD.getBounds2D().getMinY());
        final Point2D THRESHOLD_STOP = new Point2D.Double(0, THRESHOLD.getBounds2D().getMaxY());
        final float[] THRESHOLD_FRACTIONS = {
            0.0f,
            0.3f,
            0.59f,
            1.0f
        };
        final Color[] THRESHOLD_COLORS = {
            new Color(82, 0, 0, 255),
            new Color(252, 29, 0, 255),
            new Color(252, 29, 0, 255),
            new Color(82, 0, 0, 255)
        };
        final LinearGradientPaint THRESHOLD_GRADIENT = new LinearGradientPaint(THRESHOLD_START, THRESHOLD_STOP, THRESHOLD_FRACTIONS, THRESHOLD_COLORS);
        G2.setPaint(THRESHOLD_GRADIENT);
        G2.fill(THRESHOLD);

        G2.setColor(Color.WHITE);
        G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        G2.draw(THRESHOLD);

        G2.dispose();

        return IMAGE;
    }

    @Override
    protected BufferedImage create_MEASURED_VALUE_Image(final int WIDTH, final Color COLOR) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//        G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
//        G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
//        G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
//        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        //G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final GeneralPath MAXMEASURED = new GeneralPath();
        MAXMEASURED.setWindingRule(Path2D.WIND_EVEN_ODD);
        MAXMEASURED.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.3037383177570093);
        MAXMEASURED.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.2803738317757009);
        MAXMEASURED.lineTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.2803738317757009);
        MAXMEASURED.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.3037383177570093);
        MAXMEASURED.closePath();
        G2.setColor(COLOR);
        G2.fill(MAXMEASURED);

        G2.dispose();

        return IMAGE;
    }

    @Override
    protected BufferedImage create_POINTER_Image(final int WIDTH, final PointerType POINTER_TYPE) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
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

        final GeneralPath POINTER;
        final Point2D POINTER_START;
        final Point2D POINTER_STOP;
        final float[] POINTER_FRACTIONS;
        final Color[] POINTER_COLORS;
        final java.awt.Paint POINTER_GRADIENT;
        final CustomColorDef CUSTOM_POINTER_COLOR;
        if (getPointerColor() == ColorDef.CUSTOM) {
            CUSTOM_POINTER_COLOR = new CustomColorDef(getCustomPointerColor());
        } else {
            CUSTOM_POINTER_COLOR = null;
        }

        switch (POINTER_TYPE) {
            case TYPE2:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.705607476635514);
                POINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.6962616822429907);
                POINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5747663551401869);
                POINTER.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.2897196261682243);
                POINTER.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.2897196261682243);
                POINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5747663551401869);
                POINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.6962616822429907);
                POINTER.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.705607476635514);
                POINTER.curveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.705607476635514, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.7242990654205608, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.7336448598130841);
                POINTER.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.7523364485981309, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.7663551401869159, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.7663551401869159);
                POINTER.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.7663551401869159, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.7523364485981309, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.7336448598130841);
                POINTER.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.7242990654205608, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.705607476635514, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.705607476635514);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(0, POINTER.getBounds2D().getMaxY());
                POINTER_STOP = new Point2D.Double(0, POINTER.getBounds2D().getMinY());
                POINTER_FRACTIONS = new float[]{
                    0.0f,
                    0.36f,
                    0.3601f,
                    1.0f
                };
                if (getPointerColor() != ColorDef.CUSTOM) {
                    POINTER_COLORS = new Color[]{
                        getBackgroundColor().LABEL_COLOR,
                        getBackgroundColor().LABEL_COLOR,
                        getPointerColor().LIGHT,
                        getPointerColor().LIGHT
                    };
                } else {
                    POINTER_COLORS = new Color[]{
                        getBackgroundColor().LABEL_COLOR,
                        getBackgroundColor().LABEL_COLOR,
                        CUSTOM_POINTER_COLOR.LIGHT,
                        CUSTOM_POINTER_COLOR.LIGHT
                    };
                }
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                break;

            case TYPE3:
                POINTER = new GeneralPath(new Rectangle2D.Double(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.2897196261682243, IMAGE_WIDTH * 0.009345794392523364, IMAGE_HEIGHT * 0.4485981308));
                if (getPointerColor() != ColorDef.CUSTOM) {
                    G2.setColor(getPointerColor().LIGHT);
                } else {
                    G2.setColor(CUSTOM_POINTER_COLOR.LIGHT);
                }
                G2.fill(POINTER);
                break;

            case TYPE4:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.29439252336448596);
                POINTER.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.3037383177570093);
                POINTER.lineTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.7336448598130841);
                POINTER.lineTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.8364485981308412);
                POINTER.lineTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.8364485981308412);
                POINTER.lineTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.7336448598130841);
                POINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.3037383177570093);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.29439252336448596);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(POINTER.getBounds2D().getMinX(), 0);
                POINTER_STOP = new Point2D.Double(POINTER.getBounds2D().getMaxX(), 0);
                POINTER_FRACTIONS = new float[]{
                    0.0f,
                    0.51f,
                    0.52f,
                    1.0f
                };
                if (getPointerColor() != ColorDef.CUSTOM) {
                    POINTER_COLORS = new Color[]{
                        getPointerColor().DARK,
                        getPointerColor().DARK,
                        getPointerColor().LIGHT,
                        getPointerColor().LIGHT
                    };
                } else {
                    POINTER_COLORS = new Color[]{
                        CUSTOM_POINTER_COLOR.DARK,
                        CUSTOM_POINTER_COLOR.DARK,
                        CUSTOM_POINTER_COLOR.LIGHT,
                        CUSTOM_POINTER_COLOR.LIGHT
                    };
                }
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                break;

            case TYPE5:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.74);
                POINTER.lineTo(IMAGE_WIDTH * 0.5266666666666666, IMAGE_HEIGHT * 0.74);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.3);
                POINTER.lineTo(IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.74);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.74);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(POINTER.getBounds2D().getMinX(), 0);
                POINTER_STOP = new Point2D.Double(POINTER.getBounds2D().getMaxX(), 0);
                POINTER_FRACTIONS = new float[]{
                    0.0f,
                    0.4999f,
                    0.5f,
                    1.0f
                };
                if (getPointerColor() != ColorDef.CUSTOM) {
                    POINTER_COLORS = new Color[]{
                        getPointerColor().LIGHT,
                        getPointerColor().LIGHT,
                        getPointerColor().MEDIUM,
                        getPointerColor().MEDIUM
                    };
                } else {
                    POINTER_COLORS = new Color[]{
                        getCustomPointerColorObject().LIGHT,
                        getCustomPointerColorObject().LIGHT,
                        getCustomPointerColorObject().MEDIUM,
                        getCustomPointerColorObject().MEDIUM
                    };
                }
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                if (getPointerColor() != ColorDef.CUSTOM) {
                    G2.setColor(getPointerColor().DARK);
                } else {
                    G2.setColor(getCustomPointerColorObject().DARK);
                }
                G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                G2.draw(POINTER);
                break;

            case TYPE6:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.7333333333333333);
                POINTER.lineTo(IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.6);
                POINTER.lineTo(IMAGE_WIDTH * 0.48, IMAGE_HEIGHT * 0.49333333333333335);
                POINTER.lineTo(IMAGE_WIDTH * 0.49333333333333335, IMAGE_HEIGHT * 0.30666666666666664);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.30666666666666664);
                POINTER.lineTo(IMAGE_WIDTH * 0.5066666666666667, IMAGE_HEIGHT * 0.30666666666666664);
                POINTER.lineTo(IMAGE_WIDTH * 0.52, IMAGE_HEIGHT * 0.49333333333333335);
                POINTER.lineTo(IMAGE_WIDTH * 0.5266666666666666, IMAGE_HEIGHT * 0.5933333333333334);
                POINTER.lineTo(IMAGE_WIDTH * 0.5266666666666666, IMAGE_HEIGHT * 0.7333333333333333);
                POINTER.lineTo(IMAGE_WIDTH * 0.5066666666666667, IMAGE_HEIGHT * 0.7333333333333333);
                POINTER.lineTo(IMAGE_WIDTH * 0.5066666666666667, IMAGE_HEIGHT * 0.5933333333333334);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.49333333333333335);
                POINTER.lineTo(IMAGE_WIDTH * 0.49333333333333335, IMAGE_HEIGHT * 0.6);
                POINTER.lineTo(IMAGE_WIDTH * 0.49333333333333335, IMAGE_HEIGHT * 0.7333333333333333);
                POINTER.lineTo(IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.7333333333333333);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(POINTER.getBounds2D().getMaxY(), 0);
                POINTER_STOP = new Point2D.Double(POINTER.getBounds2D().getMinY(), 0);
                POINTER_FRACTIONS = new float[]{
                    0.0f,
                    0.25f,
                    0.75f,
                    1.0f
                };
                if (getPointerColor() != ColorDef.CUSTOM) {
                    POINTER_COLORS = new Color[]{
                        getPointerColor().LIGHT,
                        getPointerColor().MEDIUM,
                        getPointerColor().MEDIUM,
                        getPointerColor().LIGHT
                    };
                } else {
                    POINTER_COLORS = new Color[]{
                        getCustomPointerColorObject().LIGHT,
                        getCustomPointerColorObject().MEDIUM,
                        getCustomPointerColorObject().MEDIUM,
                        getCustomPointerColorObject().LIGHT
                    };
                }
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                if (getPointerColor() != ColorDef.CUSTOM) {
                    G2.setColor(getPointerColor().DARK);
                } else {
                    G2.setColor(getCustomPointerColorObject().DARK);
                }
                G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                G2.draw(POINTER);
                break;

            case TYPE7:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.4866666666666667, IMAGE_HEIGHT * 0.30666666666666664);
                POINTER.lineTo(IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.7333333333333333);
                POINTER.lineTo(IMAGE_WIDTH * 0.5266666666666666, IMAGE_HEIGHT * 0.7333333333333333);
                POINTER.lineTo(IMAGE_WIDTH * 0.5066666666666667, IMAGE_HEIGHT * 0.30666666666666664);
                POINTER.lineTo(IMAGE_WIDTH * 0.4866666666666667, IMAGE_HEIGHT * 0.30666666666666664);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(POINTER.getBounds2D().getMinX(), 0);
                POINTER_STOP = new Point2D.Double(POINTER.getBounds2D().getMaxX(), 0);
                POINTER_FRACTIONS = new float[]{
                    0.0f,
                    1.0f
                };
                if (getPointerColor() != ColorDef.CUSTOM) {
                    POINTER_COLORS = new Color[]{
                        getPointerColor().DARK,
                        getPointerColor().MEDIUM
                    };
                } else {
                    POINTER_COLORS = new Color[]{
                        getCustomPointerColorObject().DARK,
                        getCustomPointerColorObject().MEDIUM
                    };
                }
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                break;

            case TYPE8:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.7666666666666667);
                POINTER.lineTo(IMAGE_WIDTH * 0.5333333333333333, IMAGE_HEIGHT * 0.7333333333333333);
                POINTER.curveTo(IMAGE_WIDTH * 0.5333333333333333, IMAGE_HEIGHT * 0.7333333333333333, IMAGE_WIDTH * 0.5066666666666667, IMAGE_HEIGHT * 0.7066666666666667, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.31333333333333335);
                POINTER.curveTo(IMAGE_WIDTH * 0.49333333333333335, IMAGE_HEIGHT * 0.7066666666666667, IMAGE_WIDTH * 0.4666666666666667, IMAGE_HEIGHT * 0.7333333333333333, IMAGE_WIDTH * 0.4666666666666667, IMAGE_HEIGHT * 0.7333333333333333);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.7666666666666667);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(POINTER.getBounds2D().getMinX(), 0);
                POINTER_STOP = new Point2D.Double(POINTER.getBounds2D().getMaxX(), 0);
                POINTER_FRACTIONS = new float[]{
                    0.0f,
                    0.46f,
                    0.47f,
                    1.0f
                };
                if (getPointerColor() != ColorDef.CUSTOM) {
                    POINTER_COLORS = new Color[]{
                        getPointerColor().LIGHT,
                        getPointerColor().LIGHT,
                        getPointerColor().MEDIUM,
                        getPointerColor().MEDIUM
                    };
                } else {
                    POINTER_COLORS = new Color[]{
                        getCustomPointerColorObject().LIGHT,
                        getCustomPointerColorObject().LIGHT,
                        getCustomPointerColorObject().MEDIUM,
                        getCustomPointerColorObject().MEDIUM
                    };
                }
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                if (getPointerColor() != ColorDef.CUSTOM) {
                    G2.setColor(getPointerColor().DARK);
                } else {
                    G2.setColor(getCustomPointerColorObject().DARK);
                }
                G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                G2.draw(POINTER);
                break;

            case TYPE9:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.49333333333333335, IMAGE_HEIGHT * 0.42);
                POINTER.lineTo(IMAGE_WIDTH * 0.5066666666666667, IMAGE_HEIGHT * 0.42);
                POINTER.lineTo(IMAGE_WIDTH * 0.5133333333333333, IMAGE_HEIGHT * 0.66);
                POINTER.lineTo(IMAGE_WIDTH * 0.4866666666666667, IMAGE_HEIGHT * 0.66);
                POINTER.lineTo(IMAGE_WIDTH * 0.49333333333333335, IMAGE_HEIGHT * 0.42);
                POINTER.closePath();
                POINTER.moveTo(IMAGE_WIDTH * 0.49333333333333335, IMAGE_HEIGHT * 0.3);
                POINTER.lineTo(IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.7);
                POINTER.lineTo(IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.7666666666666667);
                POINTER.curveTo(IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.7666666666666667, IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.8533333333333334, IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.8533333333333334);
                POINTER.curveTo(IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.86, IMAGE_WIDTH * 0.48, IMAGE_HEIGHT * 0.86, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.86);
                POINTER.curveTo(IMAGE_WIDTH * 0.52, IMAGE_HEIGHT * 0.86, IMAGE_WIDTH * 0.5266666666666666, IMAGE_HEIGHT * 0.86, IMAGE_WIDTH * 0.5266666666666666, IMAGE_HEIGHT * 0.8533333333333334);
                POINTER.curveTo(IMAGE_WIDTH * 0.5266666666666666, IMAGE_HEIGHT * 0.8533333333333334, IMAGE_WIDTH * 0.5266666666666666, IMAGE_HEIGHT * 0.7666666666666667, IMAGE_WIDTH * 0.5266666666666666, IMAGE_HEIGHT * 0.7666666666666667);
                POINTER.lineTo(IMAGE_WIDTH * 0.5266666666666666, IMAGE_HEIGHT * 0.7);
                POINTER.lineTo(IMAGE_WIDTH * 0.5066666666666667, IMAGE_HEIGHT * 0.3);
                POINTER.lineTo(IMAGE_WIDTH * 0.49333333333333335, IMAGE_HEIGHT * 0.3);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(POINTER.getBounds2D().getMinX(), 0);
                POINTER_STOP = new Point2D.Double(POINTER.getBounds2D().getMaxX(), 0);
                POINTER_FRACTIONS = new float[]{
                    0.0f,
                    0.48f,
                    1.0f
                };
                POINTER_COLORS = new Color[]{
                    new Color(50, 50, 50, 255),
                    new Color(102, 102, 102, 255),
                    new Color(50, 50, 50, 255)
                };
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                G2.setColor(new Color(0x2E2E2E));
                G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                G2.draw(POINTER);

                final GeneralPath COLOR_BOX = new GeneralPath();
                COLOR_BOX.setWindingRule(Path2D.WIND_EVEN_ODD);
                COLOR_BOX.moveTo(IMAGE_WIDTH * 0.49333333333333335, IMAGE_HEIGHT * 0.4066666666666667);
                COLOR_BOX.lineTo(IMAGE_WIDTH * 0.5066666666666667, IMAGE_HEIGHT * 0.4066666666666667);
                COLOR_BOX.lineTo(IMAGE_WIDTH * 0.5066666666666667, IMAGE_HEIGHT * 0.30666666666666664);
                COLOR_BOX.lineTo(IMAGE_WIDTH * 0.49333333333333335, IMAGE_HEIGHT * 0.30666666666666664);
                COLOR_BOX.lineTo(IMAGE_WIDTH * 0.49333333333333335, IMAGE_HEIGHT * 0.4066666666666667);
                COLOR_BOX.closePath();
                G2.setColor(getPointerColor().MEDIUM);
                G2.fill(COLOR_BOX);
                break;

            case TYPE10:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.3);
                POINTER.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.3, IMAGE_WIDTH * 0.43333333333333335, IMAGE_HEIGHT * 0.7133333333333334, IMAGE_WIDTH * 0.43333333333333335, IMAGE_HEIGHT * 0.7266666666666667);
                POINTER.curveTo(IMAGE_WIDTH * 0.43333333333333335, IMAGE_HEIGHT * 0.76, IMAGE_WIDTH * 0.46, IMAGE_HEIGHT * 0.7933333333333333, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.7933333333333333);
                POINTER.curveTo(IMAGE_WIDTH * 0.54, IMAGE_HEIGHT * 0.7933333333333333, IMAGE_WIDTH * 0.5666666666666667, IMAGE_HEIGHT * 0.76, IMAGE_WIDTH * 0.5666666666666667, IMAGE_HEIGHT * 0.7266666666666667);
                POINTER.curveTo(IMAGE_WIDTH * 0.5666666666666667, IMAGE_HEIGHT * 0.7133333333333334, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.3, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.3);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(POINTER.getBounds2D().getMinX(), 0);
                POINTER_STOP = new Point2D.Double(POINTER.getBounds2D().getMaxX(), 0);
                POINTER_FRACTIONS = new float[]{
                    0.0f,
                    0.4999f,
                    0.5f,
                    1.0f
                };
                POINTER_COLORS = new Color[]{
                    getPointerColor().LIGHT,
                    getPointerColor().LIGHT,
                    getPointerColor().MEDIUM,
                    getPointerColor().MEDIUM
                };
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                G2.setColor(getPointerColor().MEDIUM);
                G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                G2.draw(POINTER);
                break;

            case TYPE11:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.5 * IMAGE_WIDTH, 0.3 * IMAGE_HEIGHT);
                POINTER.lineTo(0.4866666666666667 * IMAGE_WIDTH, 0.7333333333333333 * IMAGE_HEIGHT);
                POINTER.curveTo(0.4866666666666667 * IMAGE_WIDTH, 0.7333333333333333 * IMAGE_HEIGHT, 0.4866666666666667 * IMAGE_WIDTH, 0.8066666666666666 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.8066666666666666 * IMAGE_HEIGHT);
                POINTER.curveTo(0.5133333333333333 * IMAGE_WIDTH, 0.8066666666666666 * IMAGE_HEIGHT, 0.5133333333333333 * IMAGE_WIDTH, 0.7333333333333333 * IMAGE_HEIGHT, 0.5133333333333333 * IMAGE_WIDTH, 0.7333333333333333 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5 * IMAGE_WIDTH, 0.3 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5066666666666667 * IMAGE_WIDTH, 0.22666666666666666 * IMAGE_HEIGHT), new Point2D.Double(0.5066666666666667 * IMAGE_WIDTH, 0.8666666666666667 * IMAGE_HEIGHT), new float[]{0.0f, 1.0f}, new Color[]{getPointerColor().LIGHT, getPointerColor().MEDIUM}));
                G2.fill(POINTER);
                G2.setColor(getPointerColor().DARK);
                G2.setStroke(new BasicStroke((0.006666666666666667f * IMAGE_WIDTH), 0, 1));
                G2.draw(POINTER);
                break;

            case TYPE12:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.5 * IMAGE_WIDTH, 0.3 * IMAGE_HEIGHT);
                POINTER.lineTo(0.4866666666666667 * IMAGE_WIDTH, 0.7333333333333333 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5 * IMAGE_WIDTH, 0.7466666666666667 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5133333333333333 * IMAGE_WIDTH, 0.7333333333333333 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5 * IMAGE_WIDTH, 0.3 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5066666666666667 * IMAGE_WIDTH, 0.22666666666666666 * IMAGE_HEIGHT), new Point2D.Double(0.5066666666666667 * IMAGE_WIDTH, 0.7466666666666667 * IMAGE_HEIGHT), new float[]{0.0f, 1.0f}, new Color[]{getPointerColor().LIGHT, getPointerColor().MEDIUM}));
                G2.fill(POINTER);
                G2.setPaint(getPointerColor().DARK);
                G2.setStroke(new BasicStroke((0.006666666666666667f * IMAGE_WIDTH), 0, 1));
                G2.draw(POINTER);
                break;

            case TYPE13:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.4866666666666667 * IMAGE_WIDTH, 0.32666666666666666 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5 * IMAGE_WIDTH, 0.29333333333333333 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5133333333333333 * IMAGE_WIDTH, 0.32666666666666666 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5133333333333333 * IMAGE_WIDTH, 0.7466666666666667 * IMAGE_HEIGHT);
                POINTER.lineTo(0.4866666666666667 * IMAGE_WIDTH, 0.7466666666666667 * IMAGE_HEIGHT);
                POINTER.lineTo(0.4866666666666667 * IMAGE_WIDTH, 0.32666666666666666 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.7333333333333333 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 0.29333333333333333 * IMAGE_HEIGHT), new float[]{0.0f, 0.849999f, 0.85f, 1.0f}, new Color[]{getPointerColor().MEDIUM, getPointerColor().MEDIUM, getBackgroundColor().LABEL_COLOR, getBackgroundColor().LABEL_COLOR}));
                G2.fill(POINTER);
                break;

            case TYPE14:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.4866666666666667 * IMAGE_WIDTH, 0.32666666666666666 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5 * IMAGE_WIDTH, 0.29333333333333333 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5133333333333333 * IMAGE_WIDTH, 0.32666666666666666 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5133333333333333 * IMAGE_WIDTH, 0.7466666666666667 * IMAGE_HEIGHT);
                POINTER.lineTo(0.4866666666666667 * IMAGE_WIDTH, 0.7466666666666667 * IMAGE_HEIGHT);
                POINTER.lineTo(0.4866666666666667 * IMAGE_WIDTH, 0.32666666666666666 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.4866666666666667 * IMAGE_WIDTH, 0.64 * IMAGE_HEIGHT), new Point2D.Double(0.5066666666666667 * IMAGE_WIDTH, 0.64 * IMAGE_HEIGHT), new float[]{0.0f, 0.5f, 1.0f}, new Color[]{getPointerColor().VERY_DARK, getPointerColor().LIGHT, getPointerColor().VERY_DARK}));
                G2.fill(POINTER);
                break;

            case TYPE1:

            default:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.705607476635514);
                POINTER.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.6915887850467289, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.6261682242990654, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.6121495327102804);
                POINTER.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.5981308411214953, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.29906542056074764, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.29906542056074764);
                POINTER.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.29906542056074764, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5981308411214953, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.6121495327102804);
                POINTER.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.6308411214953271, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.6915887850467289, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.705607476635514);
                POINTER.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.7149532710280374, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.7242990654205608, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.7336448598130841);
                POINTER.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.7523364485981309, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.7663551401869159, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.7663551401869159);
                POINTER.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.7663551401869159, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.7523364485981309, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.7336448598130841);
                POINTER.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.7242990654205608, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.7149532710280374, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.705607476635514);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(0, POINTER.getBounds2D().getMinY());
                POINTER_STOP = new Point2D.Double(0, POINTER.getBounds2D().getMaxY());
                POINTER_FRACTIONS = new float[]{
                    0.0f,
                    0.3f,
                    0.59f,
                    1.0f
                };
                if (getPointerColor() != ColorDef.CUSTOM) {
                    POINTER_COLORS = new Color[]{
                        getPointerColor().DARK,
                        getPointerColor().LIGHT,
                        getPointerColor().LIGHT,
                        getPointerColor().DARK
                    };
                } else {
                    POINTER_COLORS = new Color[]{
                        CUSTOM_POINTER_COLOR.DARK,
                        CUSTOM_POINTER_COLOR.LIGHT,
                        CUSTOM_POINTER_COLOR.LIGHT,
                        CUSTOM_POINTER_COLOR.DARK
                    };
                }
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);

                final Color STROKE_COLOR_POINTER = getPointerColor().LIGHT;
                G2.setColor(STROKE_COLOR_POINTER);
                G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                G2.draw(POINTER);
                break;
        }

        G2.dispose();

        return IMAGE;
    }

    @Override
    protected BufferedImage create_POINTER_SHADOW_Image(final int WIDTH, final PointerType POINTER_TYPE) {
        if (WIDTH <= 0) {
            return null;
        }
        final Color SHADOW_COLOR = new Color(0.0f, 0.0f, 0.0f, 0.65f);

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
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

        final GeneralPath POINTER;

        switch (POINTER_TYPE) {
            case TYPE1:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.705607476635514);
                POINTER.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.6915887850467289, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.6261682242990654, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.6121495327102804);
                POINTER.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.5981308411214953, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.29906542056074764, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.29906542056074764);
                POINTER.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.29906542056074764, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5981308411214953, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.6121495327102804);
                POINTER.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.6308411214953271, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.6915887850467289, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.705607476635514);
                POINTER.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.7149532710280374, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.7242990654205608, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.7336448598130841);
                POINTER.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.7523364485981309, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.7663551401869159, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.7663551401869159);
                POINTER.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.7663551401869159, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.7523364485981309, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.7336448598130841);
                POINTER.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.7242990654205608, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.7149532710280374, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.705607476635514);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE2:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.705607476635514);
                POINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.6962616822429907);
                POINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5747663551401869);
                POINTER.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.2897196261682243);
                POINTER.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.2897196261682243);
                POINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5747663551401869);
                POINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.6962616822429907);
                POINTER.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.705607476635514);
                POINTER.curveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.705607476635514, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.7242990654205608, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.7336448598130841);
                POINTER.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.7523364485981309, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.7663551401869159, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.7663551401869159);
                POINTER.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.7663551401869159, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.7523364485981309, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.7336448598130841);
                POINTER.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.7242990654205608, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.705607476635514, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.705607476635514);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE3:
                break;

            case TYPE4:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.29439252336448596);
                POINTER.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.3037383177570093);
                POINTER.lineTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.7336448598130841);
                POINTER.lineTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.8364485981308412);
                POINTER.lineTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.8364485981308412);
                POINTER.lineTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.7336448598130841);
                POINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.3037383177570093);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.29439252336448596);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE5:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.74);
                POINTER.lineTo(IMAGE_WIDTH * 0.5266666666666666, IMAGE_HEIGHT * 0.74);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.3);
                POINTER.lineTo(IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.74);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.74);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE6:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.7333333333333333);
                POINTER.lineTo(IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.6);
                POINTER.lineTo(IMAGE_WIDTH * 0.48, IMAGE_HEIGHT * 0.49333333333333335);
                POINTER.lineTo(IMAGE_WIDTH * 0.49333333333333335, IMAGE_HEIGHT * 0.30666666666666664);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.30666666666666664);
                POINTER.lineTo(IMAGE_WIDTH * 0.5066666666666667, IMAGE_HEIGHT * 0.30666666666666664);
                POINTER.lineTo(IMAGE_WIDTH * 0.52, IMAGE_HEIGHT * 0.49333333333333335);
                POINTER.lineTo(IMAGE_WIDTH * 0.5266666666666666, IMAGE_HEIGHT * 0.5933333333333334);
                POINTER.lineTo(IMAGE_WIDTH * 0.5266666666666666, IMAGE_HEIGHT * 0.7333333333333333);
                POINTER.lineTo(IMAGE_WIDTH * 0.5066666666666667, IMAGE_HEIGHT * 0.7333333333333333);
                POINTER.lineTo(IMAGE_WIDTH * 0.5066666666666667, IMAGE_HEIGHT * 0.5933333333333334);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.49333333333333335);
                POINTER.lineTo(IMAGE_WIDTH * 0.49333333333333335, IMAGE_HEIGHT * 0.6);
                POINTER.lineTo(IMAGE_WIDTH * 0.49333333333333335, IMAGE_HEIGHT * 0.7333333333333333);
                POINTER.lineTo(IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.7333333333333333);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE7:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.4866666666666667, IMAGE_HEIGHT * 0.30666666666666664);
                POINTER.lineTo(IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.7333333333333333);
                POINTER.lineTo(IMAGE_WIDTH * 0.5266666666666666, IMAGE_HEIGHT * 0.7333333333333333);
                POINTER.lineTo(IMAGE_WIDTH * 0.5066666666666667, IMAGE_HEIGHT * 0.30666666666666664);
                POINTER.lineTo(IMAGE_WIDTH * 0.4866666666666667, IMAGE_HEIGHT * 0.30666666666666664);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE8:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.7666666666666667);
                POINTER.lineTo(IMAGE_WIDTH * 0.5333333333333333, IMAGE_HEIGHT * 0.7333333333333333);
                POINTER.curveTo(IMAGE_WIDTH * 0.5333333333333333, IMAGE_HEIGHT * 0.7333333333333333, IMAGE_WIDTH * 0.5066666666666667, IMAGE_HEIGHT * 0.7066666666666667, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.31333333333333335);
                POINTER.curveTo(IMAGE_WIDTH * 0.49333333333333335, IMAGE_HEIGHT * 0.7066666666666667, IMAGE_WIDTH * 0.4666666666666667, IMAGE_HEIGHT * 0.7333333333333333, IMAGE_WIDTH * 0.4666666666666667, IMAGE_HEIGHT * 0.7333333333333333);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.7666666666666667);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE9:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.49333333333333335, IMAGE_HEIGHT * 0.42);
                POINTER.lineTo(IMAGE_WIDTH * 0.5066666666666667, IMAGE_HEIGHT * 0.42);
                POINTER.lineTo(IMAGE_WIDTH * 0.5133333333333333, IMAGE_HEIGHT * 0.66);
                POINTER.lineTo(IMAGE_WIDTH * 0.4866666666666667, IMAGE_HEIGHT * 0.66);
                POINTER.lineTo(IMAGE_WIDTH * 0.49333333333333335, IMAGE_HEIGHT * 0.42);
                POINTER.closePath();
                POINTER.moveTo(IMAGE_WIDTH * 0.49333333333333335, IMAGE_HEIGHT * 0.3);
                POINTER.lineTo(IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.7);
                POINTER.lineTo(IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.7666666666666667);
                POINTER.curveTo(IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.7666666666666667, IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.8533333333333334, IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.8533333333333334);
                POINTER.curveTo(IMAGE_WIDTH * 0.47333333333333333, IMAGE_HEIGHT * 0.86, IMAGE_WIDTH * 0.48, IMAGE_HEIGHT * 0.86, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.86);
                POINTER.curveTo(IMAGE_WIDTH * 0.52, IMAGE_HEIGHT * 0.86, IMAGE_WIDTH * 0.5266666666666666, IMAGE_HEIGHT * 0.86, IMAGE_WIDTH * 0.5266666666666666, IMAGE_HEIGHT * 0.8533333333333334);
                POINTER.curveTo(IMAGE_WIDTH * 0.5266666666666666, IMAGE_HEIGHT * 0.8533333333333334, IMAGE_WIDTH * 0.5266666666666666, IMAGE_HEIGHT * 0.7666666666666667, IMAGE_WIDTH * 0.5266666666666666, IMAGE_HEIGHT * 0.7666666666666667);
                POINTER.lineTo(IMAGE_WIDTH * 0.5266666666666666, IMAGE_HEIGHT * 0.7);
                POINTER.lineTo(IMAGE_WIDTH * 0.5066666666666667, IMAGE_HEIGHT * 0.3);
                POINTER.lineTo(IMAGE_WIDTH * 0.49333333333333335, IMAGE_HEIGHT * 0.3);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE10:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.3);
                POINTER.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.3, IMAGE_WIDTH * 0.43333333333333335, IMAGE_HEIGHT * 0.7133333333333334, IMAGE_WIDTH * 0.43333333333333335, IMAGE_HEIGHT * 0.7266666666666667);
                POINTER.curveTo(IMAGE_WIDTH * 0.43333333333333335, IMAGE_HEIGHT * 0.76, IMAGE_WIDTH * 0.46, IMAGE_HEIGHT * 0.7933333333333333, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.7933333333333333);
                POINTER.curveTo(IMAGE_WIDTH * 0.54, IMAGE_HEIGHT * 0.7933333333333333, IMAGE_WIDTH * 0.5666666666666667, IMAGE_HEIGHT * 0.76, IMAGE_WIDTH * 0.5666666666666667, IMAGE_HEIGHT * 0.7266666666666667);
                POINTER.curveTo(IMAGE_WIDTH * 0.5666666666666667, IMAGE_HEIGHT * 0.7133333333333334, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.3, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.3);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE11:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.5 * IMAGE_WIDTH, 0.3 * IMAGE_HEIGHT);
                POINTER.lineTo(0.4866666666666667 * IMAGE_WIDTH, 0.7333333333333333 * IMAGE_HEIGHT);
                POINTER.curveTo(0.4866666666666667 * IMAGE_WIDTH, 0.7333333333333333 * IMAGE_HEIGHT, 0.4866666666666667 * IMAGE_WIDTH, 0.8066666666666666 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.8066666666666666 * IMAGE_HEIGHT);
                POINTER.curveTo(0.5133333333333333 * IMAGE_WIDTH, 0.8066666666666666 * IMAGE_HEIGHT, 0.5133333333333333 * IMAGE_WIDTH, 0.7333333333333333 * IMAGE_HEIGHT, 0.5133333333333333 * IMAGE_WIDTH, 0.7333333333333333 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5 * IMAGE_WIDTH, 0.3 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE12:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.5 * IMAGE_WIDTH, 0.3 * IMAGE_HEIGHT);
                POINTER.lineTo(0.4866666666666667 * IMAGE_WIDTH, 0.7333333333333333 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5 * IMAGE_WIDTH, 0.7466666666666667 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5133333333333333 * IMAGE_WIDTH, 0.7333333333333333 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5 * IMAGE_WIDTH, 0.3 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE13:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.4866666666666667 * IMAGE_WIDTH, 0.32666666666666666 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5 * IMAGE_WIDTH, 0.29333333333333333 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5133333333333333 * IMAGE_WIDTH, 0.32666666666666666 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5133333333333333 * IMAGE_WIDTH, 0.7466666666666667 * IMAGE_HEIGHT);
                POINTER.lineTo(0.4866666666666667 * IMAGE_WIDTH, 0.7466666666666667 * IMAGE_HEIGHT);
                POINTER.lineTo(0.4866666666666667 * IMAGE_WIDTH, 0.32666666666666666 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE14:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.4866666666666667 * IMAGE_WIDTH, 0.32666666666666666 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5 * IMAGE_WIDTH, 0.29333333333333333 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5133333333333333 * IMAGE_WIDTH, 0.32666666666666666 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5133333333333333 * IMAGE_WIDTH, 0.7466666666666667 * IMAGE_HEIGHT);
                POINTER.lineTo(0.4866666666666667 * IMAGE_WIDTH, 0.7466666666666667 * IMAGE_HEIGHT);
                POINTER.lineTo(0.4866666666666667 * IMAGE_WIDTH, 0.32666666666666666 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;
        }

        G2.dispose();

        return IMAGE;
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "Radial1Vertical";
    }
}
