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
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import org.mars_sim.msp.ui.steelseries.tools.BackgroundColor;
import org.mars_sim.msp.ui.steelseries.tools.ColorDef;
import org.mars_sim.msp.ui.steelseries.tools.ConicalGradientPaint;
import org.mars_sim.msp.ui.steelseries.tools.Direction;
import org.mars_sim.msp.ui.steelseries.tools.FrameDesign;
import org.mars_sim.msp.ui.steelseries.tools.GaugeType;
import org.mars_sim.msp.ui.steelseries.tools.GradientWrapper;
import org.mars_sim.msp.ui.steelseries.tools.Model;
import org.mars_sim.msp.ui.steelseries.tools.Orientation;
import org.mars_sim.msp.ui.steelseries.tools.PointerType;
import org.mars_sim.msp.ui.steelseries.tools.Scaler;
import org.mars_sim.msp.ui.steelseries.tools.Section;
import org.mars_sim.msp.ui.steelseries.tools.Shadow;


/**
 *
 * @author hansolo
 */
public final class RadialQuarterN extends AbstractRadial {
    // <editor-fold defaultstate="collapsed" desc="Variable declarations">
    private static final int BASE = 10;
    private static final double TICKMARK_ROTATION_OFFSET = 0.5 * Math.PI;
    private final double ROTATION_OFFSET; // Offset for the pointer
    private BufferedImage bImage;
    private BufferedImage fImage;
    private BufferedImage glowImageOff;
    private BufferedImage glowImageOn;
    private BufferedImage pointerImage;
    private BufferedImage pointerShadowImage;
    private BufferedImage thresholdImage;
    private BufferedImage minMeasuredImage;
    private BufferedImage maxMeasuredImage;
    private BufferedImage disabledImage;
    private final Point2D ROTATION_CENTER;
    private final Point2D TRACK_OFFSET;
    private final Point2D TICKMARKS_OFFSET;
    private final Point2D THRESHOLD_OFFSET;
    private final Point2D MEASURED_OFFSET;
    private double thresholdRotationOffset;
    private double measuredRotationOffset;
    private float titleOffsetYFactor;
    private float unitOffsetYFactor;
    private double angle;
    private final Color DARK_NOISE;
    private final Color BRIGHT_NOISE;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public RadialQuarterN() {
        super();
        getModel().setGaugeType(GaugeType.TYPE1);
        ROTATION_OFFSET = (1.5 * Math.PI) + (getModel().getFreeAreaAngle() / 2.0);
        ROTATION_CENTER = new Point2D.Double(0, 0);
        TRACK_OFFSET = new Point2D.Double(0, 0);
        TICKMARKS_OFFSET = new Point2D.Double(0, 0);
        THRESHOLD_OFFSET = new Point2D.Double(0, 0);
        MEASURED_OFFSET = new Point2D.Double(0, 0);
        measuredRotationOffset = 0;
        thresholdRotationOffset = 0;
        titleOffsetYFactor = 0.6f;
        unitOffsetYFactor = 0.67f;
        angle = 0;
        DARK_NOISE = new Color(0.2f, 0.2f, 0.2f);
        BRIGHT_NOISE = new Color(0.8f, 0.8f, 0.8f);
        setLedPosition(0.45, 0.45);
        setUserLedPosition(0.6, 0.45);
        setOrientation(Orientation.NORTH_WEST);
        init(getInnerBounds().width, getInnerBounds().height);
    }

    public RadialQuarterN(final Model MODEL) {
        super();
        setModel(MODEL);
        ROTATION_OFFSET = (1.5 * Math.PI) + (getModel().getFreeAreaAngle() / 2.0);
        ROTATION_CENTER = new Point2D.Double(0, 0);
        TRACK_OFFSET = new Point2D.Double(0, 0);
        TICKMARKS_OFFSET = new Point2D.Double(0, 0);
        THRESHOLD_OFFSET = new Point2D.Double(0, 0);
        MEASURED_OFFSET = new Point2D.Double(0, 0);
        measuredRotationOffset = 0;
        thresholdRotationOffset = 0;
        titleOffsetYFactor = 0.6f;
        unitOffsetYFactor = 0.67f;
        angle = 0;
        DARK_NOISE = new Color(0.2f, 0.2f, 0.2f);
        BRIGHT_NOISE = new Color(0.8f, 0.8f, 0.8f);
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
            create_FRAME_Image(GAUGE_WIDTH, bImage);
        }

        if (isBackgroundVisible()) {
            create_BACKGROUND_Image(GAUGE_WIDTH, bImage);
        }

        if (isGlowVisible()) {
            if (glowImageOff != null) {
                glowImageOff.flush();
            }
            glowImageOff = create_GLOW_Image(GAUGE_WIDTH, getGlowColor(), false);
            if (glowImageOn != null) {
                glowImageOn.flush();
            }
            glowImageOn = create_GLOW_Image(GAUGE_WIDTH, getGlowColor(), true);
        } else {
            setGlowPulsating(false);
        }

        final double TRACK_ORIENTATION_OFFSET;
        switch (getOrientation()) {
            case NORTH_EAST:
                TRACK_ORIENTATION_OFFSET = Math.PI / 2;
                TRACK_OFFSET.setLocation(-(GAUGE_WIDTH / 1.55), 0);
                break;
            case SOUTH_EAST:
                TRACK_ORIENTATION_OFFSET = Math.PI;
                TRACK_OFFSET.setLocation(-(GAUGE_WIDTH / 1.55), -(GAUGE_WIDTH / 1.55));
                break;
            case SOUTH_WEST:
                TRACK_ORIENTATION_OFFSET = 1.5 * Math.PI;
                TRACK_OFFSET.setLocation(0, -(GAUGE_WIDTH / 1.55));
                break;
            case NORTH_WEST:

            default:
                TRACK_ORIENTATION_OFFSET = 0;
                TRACK_OFFSET.setLocation(0, 0);
                break;
        }

        if (isTrackVisible()) {
            create_TRACK_Image(GAUGE_WIDTH, getModel().getFreeAreaAngle(), TICKMARK_ROTATION_OFFSET + TRACK_ORIENTATION_OFFSET, getMinValue(), getMaxValue(), getAngleStep(), getTrackStart(), getTrackSection(), getTrackStop(), getTrackStartColor(), getTrackSectionColor(), getTrackStopColor(), 0.68f, new Point2D.Double(GAUGE_WIDTH * 0.8271028037, GAUGE_WIDTH * 0.8271028037), getTickmarkDirection(), TRACK_OFFSET, bImage);
        }

        if (!getAreas().isEmpty()){
            createAreas(bImage);
        }

        if (!getSections().isEmpty()) {
            createSections(bImage);
        }

        //final double TICKMARKS_ORIENTATION_OFFSET;
        switch (getOrientation()) {
            case NORTH_EAST:
                //TICKMARKS_ORIENTATION_OFFSET = Math.PI / 2;
                TICKMARKS_OFFSET.setLocation(-(GAUGE_WIDTH / 1.55), 0);
                //tickLabelRotationOffset = 0;
                break;
            case SOUTH_EAST:
                //TICKMARKS_ORIENTATION_OFFSET = Math.PI;
                TICKMARKS_OFFSET.setLocation(-(GAUGE_WIDTH / 1.55), -(GAUGE_WIDTH / 1.55));
                //tickLabelRotationOffset = Math.PI;
                break;
            case SOUTH_WEST:
                //TICKMARKS_ORIENTATION_OFFSET = 1.5 * Math.PI;
                TICKMARKS_OFFSET.setLocation(0, -(GAUGE_WIDTH / 1.55));
                //tickLabelRotationOffset = Math.PI;
                break;
            case NORTH_WEST:

            default:
                //TICKMARKS_ORIENTATION_OFFSET = 0;
                TICKMARKS_OFFSET.setLocation(0, 0);
                //tickLabelRotationOffset = 0;
                break;
        }

        TICKMARK_FACTORY.create_RADIAL_TICKMARKS_Image(GAUGE_WIDTH,
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
                                                       0.68f,
                                                       0.09f,
                                                       new Point2D.Double(GAUGE_WIDTH * 0.8271028037, GAUGE_WIDTH * 0.8271028037),
                                                       new Point2D.Double(0, 0),
                                                       Orientation.NORTH_WEST,
                                                       getModel().getTicklabelOrientation(),
                                                       getModel().isNiceScale(),
                                                       getModel().isLogScale(),
                                                       bImage);

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

        create_POSTS_Image(GAUGE_WIDTH, fImage);

        if (isForegroundVisible()) {
            create_FOREGROUND_Image(GAUGE_WIDTH, fImage);
        }

        final double THRESHOLD_ORIENTATION_OFFSET;
        switch (getOrientation()) {
            case NORTH_EAST:
                THRESHOLD_ORIENTATION_OFFSET = Math.PI / 2;
                THRESHOLD_OFFSET.setLocation(bImage.getWidth() * 0.775, bImage.getHeight() * 0.81);
                if (!isLogScale()) {
                    thresholdRotationOffset = ROTATION_OFFSET + (getMaxValue() - getThreshold() - getMinValue()) * getAngleStep();
                } else {
                    thresholdRotationOffset = ROTATION_OFFSET + UTIL.logOfBase(BASE, getMaxValue() - getThreshold() - getMinValue()) * getLogAngleStep();
                }
                break;
            case SOUTH_EAST:
                THRESHOLD_ORIENTATION_OFFSET = Math.PI / 2;
                THRESHOLD_OFFSET.setLocation((bImage.getWidth() * 0.79), (bImage.getHeight() * 0.16));
                if (!isLogScale()) {
                    thresholdRotationOffset = Math.PI / 2 + ROTATION_OFFSET + (getMaxValue() - getThreshold() - getMinValue()) * getAngleStep();
                } else {
                    thresholdRotationOffset = Math.PI / 2 + ROTATION_OFFSET + UTIL.logOfBase(BASE, getMaxValue() - getThreshold() - getMinValue()) * getLogAngleStep();
                }
                break;
            case SOUTH_WEST:
                THRESHOLD_ORIENTATION_OFFSET = -Math.PI / 2;
                THRESHOLD_OFFSET.setLocation((bImage.getWidth() * 0.19), (bImage.getHeight() * 0.16));
                if (!isLogScale()) {
                    thresholdRotationOffset = ROTATION_OFFSET + (getThreshold() - getMinValue()) * getAngleStep();
                } else {
                    thresholdRotationOffset = ROTATION_OFFSET + UTIL.logOfBase(BASE, getThreshold() - getMinValue()) * getLogAngleStep();
                }
                break;
            case NORTH_WEST:

            default:
                THRESHOLD_ORIENTATION_OFFSET = 0;
                THRESHOLD_OFFSET.setLocation((bImage.getWidth() * 0.805), (bImage.getHeight() * 0.19));
                if (!isLogScale()) {
                    thresholdRotationOffset = ROTATION_OFFSET + (getThreshold() - getMinValue()) * getAngleStep();
                } else {
                    thresholdRotationOffset = ROTATION_OFFSET + UTIL.logOfBase(BASE, getThreshold() - getMinValue()) * getLogAngleStep();
                }
                break;
        }

        if (thresholdImage != null) {
            thresholdImage.flush();
        }
        thresholdImage = create_THRESHOLD_Image(GAUGE_WIDTH, THRESHOLD_ORIENTATION_OFFSET);

        final double MIN_MEASURED_ORIENTATION_OFFSET;
        switch (getOrientation()) {
            case NORTH_EAST:
                MIN_MEASURED_ORIENTATION_OFFSET = Math.PI / 2;
                measuredRotationOffset = ROTATION_OFFSET;
                MEASURED_OFFSET.setLocation(bImage.getWidth() * 0.87, bImage.getHeight() * 0.815);
                break;
            case SOUTH_EAST:
                MIN_MEASURED_ORIENTATION_OFFSET = Math.PI / 2;
                MEASURED_OFFSET.setLocation((bImage.getWidth() * 0.87), (bImage.getHeight() * 0.15));
                measuredRotationOffset = Math.PI / 2 + ROTATION_OFFSET;
                break;
            case SOUTH_WEST:
                MIN_MEASURED_ORIENTATION_OFFSET = -Math.PI / 2;
                MEASURED_OFFSET.setLocation((bImage.getWidth() * 0.10), (bImage.getHeight() * 0.16));
                measuredRotationOffset = ROTATION_OFFSET;
                break;
            case NORTH_WEST:

            default:
                MIN_MEASURED_ORIENTATION_OFFSET = 0;
                MEASURED_OFFSET.setLocation((bImage.getWidth() * 0.811), (bImage.getHeight() * 0.11));
                measuredRotationOffset = ROTATION_OFFSET;
                break;
        }
        if (minMeasuredImage != null) {
            minMeasuredImage.flush();
        }
        minMeasuredImage = create_MEASURED_VALUE_Image(GAUGE_WIDTH, new Color(0, 23, 252, 255), MIN_MEASURED_ORIENTATION_OFFSET);

        final double MAX_MEASURED_ORIENTATION_OFFSET;
        switch (getOrientation()) {
            case NORTH_EAST:
                MAX_MEASURED_ORIENTATION_OFFSET = Math.PI / 2;
                measuredRotationOffset = ROTATION_OFFSET;
                MEASURED_OFFSET.setLocation(bImage.getWidth() * 0.87, bImage.getHeight() * 0.815);
                break;
            case SOUTH_EAST:
                MAX_MEASURED_ORIENTATION_OFFSET = Math.PI / 2;
                MEASURED_OFFSET.setLocation((bImage.getWidth() * 0.87), (bImage.getHeight() * 0.15));
                measuredRotationOffset = Math.PI / 2 + ROTATION_OFFSET;
                break;
            case SOUTH_WEST:
                MAX_MEASURED_ORIENTATION_OFFSET = -Math.PI / 2;
                MEASURED_OFFSET.setLocation((bImage.getWidth() * 0.10), (bImage.getHeight() * 0.16));
                measuredRotationOffset = ROTATION_OFFSET;
                break;
            case NORTH_WEST:

            default:
                MAX_MEASURED_ORIENTATION_OFFSET = 0;
                MEASURED_OFFSET.setLocation((bImage.getWidth() * 0.811), (bImage.getHeight() * 0.11));
                measuredRotationOffset = ROTATION_OFFSET;
                break;
        }
        if (maxMeasuredImage != null) {
            maxMeasuredImage.flush();
        }
        maxMeasuredImage = create_MEASURED_VALUE_Image(GAUGE_WIDTH, new Color(252, 29, 0, 255), MAX_MEASURED_ORIENTATION_OFFSET);

        if (disabledImage != null) {
            disabledImage.flush();
        }
        disabledImage = create_DISABLED_Image(GAUGE_WIDTH);

        // Adjust the rotation center of the pointer
        switch (getOrientation()) {
            case NORTH_EAST:
                ROTATION_CENTER.setLocation(GAUGE_WIDTH - GAUGE_WIDTH * 0.8271028037, GAUGE_WIDTH * 0.8271028037);
                titleOffsetYFactor = 0.6f;
                unitOffsetYFactor = 0.67f;
                break;
            case SOUTH_EAST:
                ROTATION_CENTER.setLocation(GAUGE_WIDTH - GAUGE_WIDTH * 0.8271028037, GAUGE_WIDTH - GAUGE_WIDTH * 0.8271028037);
                titleOffsetYFactor = 0.3f;
                unitOffsetYFactor = 0.37f;
                break;
            case SOUTH_WEST:
                ROTATION_CENTER.setLocation(GAUGE_WIDTH * 0.8271028037, GAUGE_WIDTH - GAUGE_WIDTH * 0.8271028037);
                titleOffsetYFactor = 0.3f;
                unitOffsetYFactor = 0.37f;
                break;
            case NORTH_WEST:

            default:
                ROTATION_CENTER.setLocation(GAUGE_WIDTH * 0.8271028037, GAUGE_WIDTH * 0.8271028037);
                titleOffsetYFactor = 0.6f;
                unitOffsetYFactor = 0.67f;
                break;
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

        G2.translate(getFramelessOffset().getX(), getFramelessOffset().getY());

        final AffineTransform OLD_TRANSFORM = G2.getTransform();

        // Draw combined background image
        G2.drawImage(bImage, 0, 0, null);

        // Highlight active area
        if (isHighlightArea()) {
            for(Section area : getAreas()) {
                if (area.contains(getValue())) {
                    G2.setColor(area.getHighlightColor());
                    G2.fill(area.getFilledArea());
                    break;
                }
            }
        }

        // Highlight active section
        if (isHighlightSection()) {
            for(Section section : getSections()) {
                if (section.contains(getValue())) {
                    G2.setColor(section.getHighlightColor());
                    G2.fill(section.getSectionArea());
                    break;
                }
            }
        }

        // Draw title
        final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
        if (!getTitle().isEmpty()) {
            if (isLabelColorFromThemeEnabled()) {
                G2.setColor(getBackgroundColor().LABEL_COLOR);
            } else {
                G2.setColor(getLabelColor());
            }

            // Use custom font if selected
            if (isTitleAndUnitFontEnabled()) {
                G2.setFont(new Font(getTitleAndUnitFont().getFamily(), 0, (int) (0.04672897196261682 * getGaugeBounds().width)));
            } else {
                G2.setFont(new Font("Verdana", 0, (int) (0.04672897196261682 * getGaugeBounds().width)));
            }
            final TextLayout TITLE_LAYOUT = new TextLayout(getTitle(), G2.getFont(), RENDER_CONTEXT);
            final Rectangle2D TITLE_BOUNDARY = TITLE_LAYOUT.getBounds();
            G2.drawString(getTitle(), (float) ((getGaugeBounds().width - TITLE_BOUNDARY.getWidth()) / 2), titleOffsetYFactor * getGaugeBounds().height + TITLE_LAYOUT.getAscent() - TITLE_LAYOUT.getDescent());
        }

        // Draw unit string
        if (!getUnitString().isEmpty()) {
            if (isLabelColorFromThemeEnabled()) {
                G2.setColor(getBackgroundColor().LABEL_COLOR);
            } else {
                G2.setColor(getLabelColor());
            }

            // Use custom font if selected
            if (isTitleAndUnitFontEnabled()) {
                G2.setFont(new Font(getTitleAndUnitFont().getFamily(), 0, (int) (0.04672897196261682 * getGaugeBounds().width)));
            } else {
                G2.setFont(new Font("Verdana", 0, (int) (0.04672897196261682 * bImage.getWidth())));
            }

            final TextLayout UNIT_LAYOUT = new TextLayout(getUnitString(), G2.getFont(), RENDER_CONTEXT);
            final Rectangle2D UNIT_BOUNDARY = UNIT_LAYOUT.getBounds();
            G2.drawString(getUnitString(), (float) ((getGaugeBounds().width - UNIT_BOUNDARY.getWidth()) / 2), unitOffsetYFactor * getGaugeBounds().width + UNIT_LAYOUT.getAscent() - UNIT_LAYOUT.getDescent());
        }

        // Draw threshold indicator
        if (isThresholdVisible()) {
            G2.rotate(thresholdRotationOffset, ROTATION_CENTER.getX(), ROTATION_CENTER.getY());
            G2.drawImage(thresholdImage, (int) THRESHOLD_OFFSET.getX(), (int) THRESHOLD_OFFSET.getY(), null);
            G2.setTransform(OLD_TRANSFORM);
        }

        // Draw min measured value indicator
        if (isMinMeasuredValueVisible()) {
            switch (getOrientation()) {
                case NORTH_EAST:
                    if (!isLogScale()) {
                        G2.rotate(measuredRotationOffset + (getMaxValue() - getMinMeasuredValue() - getMinValue()) * getAngleStep(), ROTATION_CENTER.getX(), ROTATION_CENTER.getY());
                    } else {
                        G2.rotate(measuredRotationOffset + UTIL.logOfBase(BASE, getMaxValue() - getMinMeasuredValue() - getMinValue()) * getLogAngleStep(), ROTATION_CENTER.getX(), ROTATION_CENTER.getY());
                    }
                    break;
                case SOUTH_EAST:
                    if (!isLogScale()) {
                        G2.rotate(measuredRotationOffset + (getMaxValue() - getMinMeasuredValue() - getMinValue()) * getAngleStep(), ROTATION_CENTER.getX(), ROTATION_CENTER.getY());
                    } else {
                        G2.rotate(measuredRotationOffset + UTIL.logOfBase(BASE, getMaxValue() - getMinMeasuredValue() - getMinValue()) * getLogAngleStep(), ROTATION_CENTER.getX(), ROTATION_CENTER.getY());
                    }
                    break;
                case SOUTH_WEST:
                    if (!isLogScale()) {
                        G2.rotate(measuredRotationOffset + (getMinMeasuredValue() - getMinValue()) * getAngleStep(), ROTATION_CENTER.getX(), ROTATION_CENTER.getY());
                    } else {
                        G2.rotate(measuredRotationOffset + UTIL.logOfBase(BASE, getMinMeasuredValue() - getMinValue()) * getLogAngleStep(), ROTATION_CENTER.getX(), ROTATION_CENTER.getY());
                    }
                    break;
                case NORTH_WEST:
                    if (!isLogScale()) {
                        G2.rotate(measuredRotationOffset + (getMinMeasuredValue() - getMinValue()) * getAngleStep(), ROTATION_CENTER.getX(), ROTATION_CENTER.getY());
                    } else {
                        G2.rotate(measuredRotationOffset + UTIL.logOfBase(BASE, getMinMeasuredValue() - getMinValue()) * getLogAngleStep(), ROTATION_CENTER.getX(), ROTATION_CENTER.getY());
                    }
                    break;
            }
            G2.drawImage(minMeasuredImage, (int) MEASURED_OFFSET.getX(), (int) MEASURED_OFFSET.getY(), null);
            G2.setTransform(OLD_TRANSFORM);
        }

        // Draw max measured value indicator
        if (isMaxMeasuredValueVisible()) {
            switch (getOrientation()) {
                case NORTH_EAST:
                    if (!isLogScale()) {
                        G2.rotate(measuredRotationOffset + (getMaxValue() - getMaxMeasuredValue() - getMinValue()) * getAngleStep(), ROTATION_CENTER.getX(), ROTATION_CENTER.getY());
                    } else {
                        G2.rotate(measuredRotationOffset + UTIL.logOfBase(BASE, getMaxValue() - getMaxMeasuredValue() - getMinValue()) * getLogAngleStep(), ROTATION_CENTER.getX(), ROTATION_CENTER.getY());
                    }
                    break;
                case SOUTH_EAST:
                    if (!isLogScale()) {
                        G2.rotate(measuredRotationOffset + (getMaxValue() - getMaxMeasuredValue() - getMinValue()) * getAngleStep(), ROTATION_CENTER.getX(), ROTATION_CENTER.getY());
                    } else {
                        G2.rotate(measuredRotationOffset + UTIL.logOfBase(BASE, getMaxValue() - getMaxMeasuredValue() - getMinValue()) * getLogAngleStep(), ROTATION_CENTER.getX(), ROTATION_CENTER.getY());
                    }
                    break;
                case SOUTH_WEST:
                    if (!isLogScale()) {
                        G2.rotate(measuredRotationOffset + (getMaxMeasuredValue() - getMinValue()) * getAngleStep(), ROTATION_CENTER.getX(), ROTATION_CENTER.getY());
                    } else {
                        G2.rotate(measuredRotationOffset + UTIL.logOfBase(BASE, getMaxMeasuredValue() - getMinValue()) * getLogAngleStep(), ROTATION_CENTER.getX(), ROTATION_CENTER.getY());
                    }
                    break;
                case NORTH_WEST:
                    if (!isLogScale()) {
                        G2.rotate(measuredRotationOffset + (getMaxMeasuredValue() - getMinValue()) * getAngleStep(), ROTATION_CENTER.getX(), ROTATION_CENTER.getY());
                    } else {
                        G2.rotate(measuredRotationOffset + UTIL.logOfBase(BASE, getMaxMeasuredValue() - getMinValue()) * getLogAngleStep(), ROTATION_CENTER.getX(), ROTATION_CENTER.getY());
                    }
                    break;
            }
            G2.drawImage(maxMeasuredImage, (int) MEASURED_OFFSET.getX(), (int) MEASURED_OFFSET.getY(), null);
            G2.setTransform(OLD_TRANSFORM);
        }

        // Draw LED if enabled
        if (isLedVisible()) {
            G2.drawImage(getCurrentLedImage(), (int) (getGaugeBounds().width * getLedPosition().getX()), (int) (getGaugeBounds().width * getLedPosition().getY()), null);
        }

        // Draw user LED if enabled
        if (isUserLedVisible()) {
            G2.drawImage(getCurrentUserLedImage(), (int) (getGaugeBounds().width * getUserLedPosition().getX()), (int) (getGaugeBounds().width * getUserLedPosition().getY()), null);
        }

        // Draw the pointer
        switch (getOrientation()) {
            case SOUTH_EAST:
                if (!isLogScale()) {
                    angle = (getValue() - getMinValue() - getMaxValue()) * (-getAngleStep());
                } else {
                    angle = UTIL.logOfBase(BASE, getValue() - getMinValue() - getMaxValue()) * (-getLogAngleStep());
                }
                break;
            case SOUTH_WEST:
                if (!isLogScale()) {
                    angle = (getValue() - getMinValue() - getMaxValue()) * getAngleStep();
                } else {
                    angle = UTIL.logOfBase(BASE, getValue() - getMinValue() - getMaxValue()) * getLogAngleStep();
                }
                break;
            case NORTH_EAST:
                if (!isLogScale()) {
                    angle = (getValue() - getMinValue()) * (-getAngleStep());
                } else {
                    angle = UTIL.logOfBase(BASE, getValue() - getMinValue()) * (-getLogAngleStep());
                }
                break;
            case NORTH_WEST:

            default:
                if (!isLogScale()) {
                    angle = (getValue() - getMinValue()) * getAngleStep();
                } else {
                    angle = UTIL.logOfBase(BASE, getValue() - getMinValue()) * getLogAngleStep();
                }
                break;
        }

        //G2.rotate(ANGLE + (Math.cos(Math.toRadians(ANGLE - 91.0))), CENTER.getX(), CENTER.getY());
        G2.rotate(angle, ROTATION_CENTER.getX(), ROTATION_CENTER.getY() + 2);
        G2.drawImage(pointerShadowImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);
        G2.rotate(angle, ROTATION_CENTER.getX(), ROTATION_CENTER.getY());
        G2.drawImage(pointerImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw the foreground
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

        G2.translate(-getInnerBounds().x, -getInnerBounds().y);

        G2.dispose();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    @Override
    public GaugeType getGaugeType() {
        return org.mars_sim.msp.ui.steelseries.tools.GaugeType.TYPE1;
    }

    // BECAUSE THERE ARE PROBLEMS WITH NEGATIVE VALUES I TEMPORARLY
    // DEACTIVATED THE ORIENTATION RELATED SETTINGS
    @Override
    public Orientation getOrientation() {
        return org.mars_sim.msp.ui.steelseries.tools.Orientation.NORTH_WEST;
    }

    /**
     * Sets the orientation of the gauge.
     * Possible values are:
     * 8 => NORTH_WEST => the upper left area of a circle (default)
     * 2 => NORTH_EAST => upper right area of a circle
     * 4 => SOUTH_EAST => lower right area of a circle
     * 6 => SOUTH_WEST => lower left area of a circle
     * the related int values are defined in javax.swing.SwingUtilities
     * @param ORIENTATION
     */
    @Override
    public void setOrientation(final Orientation ORIENTATION) {
        super.setOrientation(ORIENTATION);

        switch (getOrientation()) {
            case NORTH_WEST:
                setTickmarkDirection(Direction.CLOCKWISE);
                break;
            case NORTH_EAST:
                setTickmarkDirection(Direction.COUNTER_CLOCKWISE);
                break;
            case SOUTH_EAST:
                setTickmarkDirection(Direction.COUNTER_CLOCKWISE);
                break;
            case SOUTH_WEST:
                setTickmarkDirection(Direction.CLOCKWISE);
                break;
        }

        init(getGaugeBounds().width, getGaugeBounds().height);
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
        final Point2D AREA_OFFSET = new Point2D.Double(0, 0);

        if (bImage != null && !getAreas().isEmpty()) {
            double stopAngle = 0;
            double startAngle = 0;

            final double OUTER_RADIUS = bImage.getWidth() *  0.3411214953f;
            final double RADIUS;
            if (isSectionsVisible()) {
                RADIUS = isExpandedSectionsEnabled() ? OUTER_RADIUS - bImage.getWidth() * 0.12f : OUTER_RADIUS - bImage.getWidth() * 0.023364486f;
            } else {
                RADIUS = OUTER_RADIUS;
            }

            final double FREE_AREA = bImage.getWidth() / 2.0 - RADIUS;
            final Rectangle2D AREA_FRAME = new Rectangle2D.Double(bImage.getMinX() + FREE_AREA * 0.9 + AREA_OFFSET.getX(), bImage.getMinY() + FREE_AREA * 0.9 + AREA_OFFSET.getY(), 4 * RADIUS, 4 * RADIUS);

            for (Section tmpArea : getAreas()) {
                switch (getOrientation()) {
                    case NORTH_EAST:
                        if (!isLogScale()) {
                            stopAngle = 90 - Math.toDegrees((getMaxValue() - tmpArea.getStop() - getMinValue()) * getAngleStep());
                            startAngle = 90 - Math.toDegrees((getMaxValue() - tmpArea.getStart() - getMinValue()) * getAngleStep());
                        } else {
                            stopAngle = 90 - Math.toDegrees(UTIL.logOfBase(BASE, getMaxValue() - tmpArea.getStop() - getMinValue()) * getLogAngleStep());
                            startAngle = 90 - Math.toDegrees(UTIL.logOfBase(BASE, getMaxValue() - tmpArea.getStart() - getMinValue()) * getLogAngleStep());
                        }
                        AREA_OFFSET.setLocation(-bImage.getWidth() * 0.45, bImage.getWidth() * 0.195);
                        break;

                    case SOUTH_EAST:
                        if (!isLogScale()) {
                            stopAngle = 0 - Math.toDegrees((getMaxValue() - tmpArea.getStop() - getMinValue()) * getAngleStep());
                            startAngle = 0 - Math.toDegrees((getMaxValue() - tmpArea.getStart() - getMinValue()) * getAngleStep());
                        } else {
                            stopAngle = 0 - Math.toDegrees(UTIL.logOfBase(BASE, getMaxValue() - tmpArea.getStop() - getMinValue()) * getLogAngleStep());
                            startAngle = 0 - Math.toDegrees(UTIL.logOfBase(BASE, getMaxValue() - tmpArea.getStart() - getMinValue()) * getLogAngleStep());
                        }
                        AREA_OFFSET.setLocation(-bImage.getWidth() * 0.45, -bImage.getWidth() * 0.45);
                        break;

                    case SOUTH_WEST:
                        if (!isLogScale()) {
                            stopAngle = 270 - Math.toDegrees((tmpArea.getStop() - getMinValue()) * getAngleStep());
                            startAngle = 270 - Math.toDegrees((tmpArea.getStart() - getMinValue()) * getAngleStep());
                        } else {
                            stopAngle = 270 - Math.toDegrees(UTIL.logOfBase(BASE, tmpArea.getStop() - getMinValue()) * getLogAngleStep());
                            startAngle = 270 - Math.toDegrees(UTIL.logOfBase(BASE, tmpArea.getStart() - getMinValue()) * getLogAngleStep());
                        }
                        AREA_OFFSET.setLocation(bImage.getWidth() * 0.195, -bImage.getWidth() * 0.45);
                        break;

                    case NORTH_WEST:

                    default:
                        if (!isLogScale()) {
                            stopAngle = 180 - Math.toDegrees((tmpArea.getStop() - getMinValue()) * getAngleStep());
                            startAngle = 180 - Math.toDegrees((tmpArea.getStart() - getMinValue()) * getAngleStep());
                        } else {
                            if (!isLogScale()) {
                            stopAngle = 180 - Math.toDegrees(UTIL.logOfBase(BASE, tmpArea.getStop() - getMinValue()) * getLogAngleStep());
                            startAngle = 180 - Math.toDegrees(UTIL.logOfBase(BASE, tmpArea.getStart() - getMinValue()) * getLogAngleStep());
                        }
                        }
                        if (isSectionsVisible()) {
                            AREA_OFFSET.setLocation(bImage.getWidth() * 0.195, bImage.getWidth() * 0.195);
                        } else {
                            AREA_OFFSET.setLocation(bImage.getWidth() * 0.148271028, bImage.getWidth() * 0.148271028);
                        }

                        break;
                }
                final java.awt.geom.Arc2D ARC = new java.awt.geom.Arc2D.Double(AREA_FRAME, 0 - (tmpArea.getStart() * getAngleStep()), -(tmpArea.getStop() - tmpArea.getStart()) * getAngleStep(), java.awt.geom.Arc2D.PIE);
                ARC.setFrame(AREA_OFFSET.getX(), AREA_OFFSET.getY(), 4 * RADIUS, 4 * RADIUS);
                ARC.setAngleStart(startAngle);
                ARC.setAngleExtent(stopAngle - startAngle);
                tmpArea.setFilledArea(ARC);
            }

            // Draw the area
            if (isAreasVisible() && IMAGE != null) {
                final Graphics2D G2 = IMAGE.createGraphics();
                G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
        if (!getSections().isEmpty() && bImage != null) {
            final double OUTER_RADIUS = bImage.getWidth() * 0.3411214953f;
            final double INNER_RADIUS = isExpandedSectionsEnabled() ? OUTER_RADIUS - bImage.getWidth() * 0.12f : OUTER_RADIUS - bImage.getWidth() * 0.023364486f;
            final Ellipse2D INNER = new Ellipse2D.Double(0, 0, 1, 1);

            switch (getOrientation()) {
                case NORTH_EAST:
                    INNER.setFrame(-bImage.getWidth() * 0.45, bImage.getWidth() * 0.195, 4 * INNER_RADIUS, 4 * INNER_RADIUS);
                    break;

                case SOUTH_EAST:
                    INNER.setFrame(-bImage.getWidth() * 0.45, -bImage.getWidth() * 0.45, 4 * INNER_RADIUS, 4 * INNER_RADIUS);
                    break;

                case SOUTH_WEST:
                    INNER.setFrame(bImage.getWidth() * 0.195, -bImage.getWidth() * 0.45, 4 * INNER_RADIUS, 4 * INNER_RADIUS);
                    break;

                case NORTH_WEST:

                default:
                    final double INNER_OFFSET = isExpandedSectionsEnabled() ? bImage.getWidth() * 0.38 : bImage.getWidth() * 0.195;
                    INNER.setFrame(INNER_OFFSET, INNER_OFFSET, 4 * INNER_RADIUS, 4 * INNER_RADIUS);
                    break;
            }

            double stopAngle;
            double startAngle;
            final Point2D SECTION_OFFSET = new Point2D.Double(0, 0);

            for (Section section : getSections()) {
                switch (getOrientation()) {
                    case NORTH_EAST:
                        if (!isLogScale()) {
                            stopAngle = 90 - Math.toDegrees((getMaxValue() - section.getStop() - getMinValue()) * getAngleStep());
                            startAngle = 90 - Math.toDegrees((getMaxValue() - section.getStart() - getMinValue()) * getAngleStep());
                        } else {
                            stopAngle = 90 - Math.toDegrees(UTIL.logOfBase(BASE, getMaxValue() - section.getStop() - getMinValue()) * getLogAngleStep());
                            startAngle = 90 - Math.toDegrees(UTIL.logOfBase(BASE, getMaxValue() - section.getStart() - getMinValue()) * getLogAngleStep());
                        }
                        SECTION_OFFSET.setLocation(-bImage.getWidth() * 0.403271028, bImage.getWidth() * 0.148271028);
                        break;

                    case SOUTH_EAST:
                        if (!isLogScale()) {
                            stopAngle = 0 - Math.toDegrees((getMaxValue() - section.getStop() - getMinValue()) * getAngleStep());
                            startAngle = 0 - Math.toDegrees((getMaxValue() - section.getStart() - getMinValue()) * getAngleStep());
                        } else {
                            stopAngle = 0 - Math.toDegrees(UTIL.logOfBase(BASE, getMaxValue() - section.getStop() - getMinValue()) * getLogAngleStep());
                            startAngle = 0 - Math.toDegrees(UTIL.logOfBase(BASE, getMaxValue() - section.getStart() - getMinValue()) * getLogAngleStep());
                        }
                        SECTION_OFFSET.setLocation(-bImage.getWidth() * 0.403271028, -bImage.getWidth() * 0.403271028);
                        break;

                    case SOUTH_WEST:
                        if (!isLogScale()) {
                            stopAngle = 270 - Math.toDegrees((section.getStop() - getMinValue()) * getAngleStep());
                            startAngle = 270 - Math.toDegrees((section.getStart() - getMinValue()) * getAngleStep());
                        } else {
                            stopAngle = 270 - Math.toDegrees(UTIL.logOfBase(BASE, section.getStop() - getMinValue()) * getLogAngleStep());
                            startAngle = 270 - Math.toDegrees(UTIL.logOfBase(BASE, section.getStart() - getMinValue()) * getLogAngleStep());
                        }
                        SECTION_OFFSET.setLocation(bImage.getWidth() * 0.148271028, -bImage.getWidth() * 0.403271028);
                        break;

                    case NORTH_WEST:

                    default:
                        if (!isLogScale()) {
                            stopAngle = 180 - Math.toDegrees((section.getStop() - getMinValue()) * getAngleStep());
                            startAngle = 180 - Math.toDegrees((section.getStart() - getMinValue()) * getAngleStep());
                        } else {
                            stopAngle = 180 - Math.toDegrees(UTIL.logOfBase(BASE, section.getStop() - getMinValue()) * getLogAngleStep());
                            startAngle = 180 - Math.toDegrees(UTIL.logOfBase(BASE, section.getStart() - getMinValue()) * getLogAngleStep());
                        }
                        SECTION_OFFSET.setLocation(bImage.getWidth() * 0.148271028, bImage.getWidth() * 0.148271028);
                        break;
                }

                final Arc2D OUTER_ARC = new Arc2D.Double(java.awt.geom.Arc2D.PIE);
                OUTER_ARC.setFrame(SECTION_OFFSET.getX(), SECTION_OFFSET.getY(), 4 * OUTER_RADIUS, 4 * OUTER_RADIUS);
                OUTER_ARC.setAngleStart(startAngle);
                OUTER_ARC.setAngleExtent(stopAngle - startAngle);
                final Area SECTION = new Area(OUTER_ARC);

                SECTION.subtract(new java.awt.geom.Area(INNER));

                section.setSectionArea(SECTION);
            }

            // Draw the sections
            if (isSectionsVisible() && IMAGE != null) {
                final Graphics2D G2 = IMAGE.createGraphics();
                G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (Section section : getSections()) {
                    G2.setColor(isTransparentAreasEnabled() ? section.getTransparentColor() : section.getColor());
                    G2.fill(section.getSectionArea());
                }
                G2.dispose();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Misc">
    private void transformGraphics(final int IMAGE_WIDTH, final int IMAGE_HEIGHT, final Graphics2D G2) {
        switch (getOrientation()) {
            // UpperRight
            case NORTH_EAST:
                G2.scale(-1, 1);
                G2.translate(-IMAGE_WIDTH, 0);
                break;
            // LowerRight
            case SOUTH_EAST:
                G2.scale(-1, -1);
                G2.translate(-IMAGE_WIDTH, -IMAGE_HEIGHT);
                break;
            // LowerLeft
            case SOUTH_WEST:
                G2.scale(1, -1);
                G2.translate(0, -IMAGE_HEIGHT);
                break;
            // UpperLeft
            case NORTH_WEST:

            default:
                G2.scale(1, 1);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image related">
    private BufferedImage create_FRAME_Image(final int WIDTH, BufferedImage image) {
        if (WIDTH <= 0) {
            return null;
        }

        if (image == null) {
            image = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
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

        transformGraphics(IMAGE_WIDTH, IMAGE_HEIGHT, G2);

        // Define shape that will be subtracted from the frame shapes and will be filled by the background later on
        final GeneralPath BACKGROUND = new GeneralPath();
        BACKGROUND.setWindingRule(Path2D.WIND_EVEN_ODD);
        BACKGROUND.moveTo(IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.9158878504672897);
        BACKGROUND.curveTo(IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.9158878504672897, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.08411214953271028, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.08411214953271028);
        BACKGROUND.curveTo(IMAGE_WIDTH * 0.6401869158878505, IMAGE_HEIGHT * 0.08411214953271028, IMAGE_WIDTH * 0.46261682242990654, IMAGE_HEIGHT * 0.1588785046728972, IMAGE_WIDTH * 0.29439252336448596, IMAGE_HEIGHT * 0.32242990654205606);
        BACKGROUND.curveTo(IMAGE_WIDTH * 0.17289719626168223, IMAGE_HEIGHT * 0.4439252336448598, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.6635514018691588, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.9158878504672897);
        BACKGROUND.curveTo(IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.9158878504672897, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.9158878504672897, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.9158878504672897);
        BACKGROUND.closePath();
        final Area SUBTRACT = new Area(BACKGROUND);

        final GeneralPath FRAME_OUTERFRAME = new GeneralPath();
        FRAME_OUTERFRAME.setWindingRule(Path2D.WIND_EVEN_ODD);
        FRAME_OUTERFRAME.moveTo(IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 1.0);
        FRAME_OUTERFRAME.curveTo(IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 1.0, IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 0.0, IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 0.0);
        FRAME_OUTERFRAME.curveTo(IMAGE_WIDTH * 0.3644859813084112, IMAGE_HEIGHT * 0.0, IMAGE_WIDTH * 0.0, IMAGE_HEIGHT * 0.308411214953271, IMAGE_WIDTH * 0.0, IMAGE_HEIGHT * 1.0);
        FRAME_OUTERFRAME.curveTo(IMAGE_WIDTH * 0.0, IMAGE_HEIGHT * 1.0, IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 1.0, IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 1.0);
        FRAME_OUTERFRAME.closePath();
        G2.setPaint(getOuterFrameColor());
        final Area FRAME_OUTERFRAME_AREA = new Area(FRAME_OUTERFRAME);
        FRAME_OUTERFRAME_AREA.subtract(SUBTRACT);
        G2.fill(FRAME_OUTERFRAME_AREA);

        final GeneralPath FRAME_MAIN = new GeneralPath();
        FRAME_MAIN.setWindingRule(Path2D.WIND_EVEN_ODD);
        FRAME_MAIN.moveTo(IMAGE_WIDTH * 0.9953271028037384, IMAGE_HEIGHT * 0.9953271028037384);
        FRAME_MAIN.curveTo(IMAGE_WIDTH * 0.9953271028037384, IMAGE_HEIGHT * 0.9953271028037384, IMAGE_WIDTH * 0.9953271028037384, IMAGE_HEIGHT * 0.004672897196261682, IMAGE_WIDTH * 0.9953271028037384, IMAGE_HEIGHT * 0.004672897196261682);
        FRAME_MAIN.curveTo(IMAGE_WIDTH * 0.3364485981308411, IMAGE_HEIGHT * 0.004672897196261682, IMAGE_WIDTH * 0.004672897196261682, IMAGE_HEIGHT * 0.35514018691588783, IMAGE_WIDTH * 0.004672897196261682, IMAGE_HEIGHT * 0.9953271028037384);
        FRAME_MAIN.curveTo(IMAGE_WIDTH * 0.004672897196261682, IMAGE_HEIGHT * 0.9953271028037384, IMAGE_WIDTH * 0.9953271028037384, IMAGE_HEIGHT * 0.9953271028037384, IMAGE_WIDTH * 0.9953271028037384, IMAGE_HEIGHT * 0.9953271028037384);
        FRAME_MAIN.closePath();

        final Point2D FRAME_MAIN_START;
        final Point2D FRAME_MAIN_STOP;
        final Point2D FRAME_MAIN_CENTER = new Point2D.Double(FRAME_MAIN.getBounds2D().getCenterX(), FRAME_MAIN.getBounds2D().getCenterY());

        switch (getOrientation()) {
            case NORTH_WEST:
                FRAME_MAIN_START = new Point2D.Double(0, FRAME_MAIN.getBounds2D().getMinY());
                FRAME_MAIN_STOP = new Point2D.Double(0, FRAME_MAIN.getBounds2D().getMaxY());
                break;
            case NORTH_EAST:
                FRAME_MAIN_START = new Point2D.Double(0, FRAME_MAIN.getBounds2D().getMinY());
                FRAME_MAIN_STOP = new Point2D.Double(0, FRAME_MAIN.getBounds2D().getMaxY());
                break;
            case SOUTH_EAST:
                FRAME_MAIN_START = new Point2D.Double(0, FRAME_MAIN.getBounds2D().getMaxY());
                FRAME_MAIN_STOP = new Point2D.Double(0, FRAME_MAIN.getBounds2D().getMinY());
                break;
            case SOUTH_WEST:
                FRAME_MAIN_START = new Point2D.Double(0, FRAME_MAIN.getBounds2D().getMaxY());
                FRAME_MAIN_STOP = new Point2D.Double(0, FRAME_MAIN.getBounds2D().getMinY());
                break;
            default:
                FRAME_MAIN_START = new Point2D.Double(0, FRAME_MAIN.getBounds2D().getMinY());
                FRAME_MAIN_STOP = new Point2D.Double(0, FRAME_MAIN.getBounds2D().getMaxY());
        }

        final float ANGLE_OFFSET = (float) Math.toDegrees(Math.atan((IMAGE_HEIGHT / 8.0f) / (IMAGE_WIDTH / 2.0f)));
        final Area FRAME_MAIN_AREA = new Area(FRAME_MAIN);

        if (getFrameDesign() == FrameDesign.CUSTOM) {
            G2.setPaint(getCustomFrameDesign());
            FRAME_MAIN_AREA.subtract(SUBTRACT);
            G2.fill(FRAME_MAIN_AREA);
        } else {
            switch (getFrameDesign()) {
                case BLACK_METAL:
                    float[] frameMainFractions1 = {
                        0.0f,
                        90.0f - 2 * ANGLE_OFFSET,
                        90.0f,
                        90.0f + 3 * ANGLE_OFFSET,
                        180.0f,
                        270.0f - 3 * ANGLE_OFFSET,
                        270.0f,
                        270.0f + 2 * ANGLE_OFFSET,
                        1.0f
                    };

                    Color[] frameMainColors1 = {
                        new Color(254, 254, 254, 255),
                        new Color(0, 0, 0, 255),
                        new Color(153, 153, 153, 255),
                        new Color(0, 0, 0, 255),
                        new Color(0, 0, 0, 255),
                        new Color(0, 0, 0, 255),
                        new Color(153, 153, 153, 255),
                        new Color(0, 0, 0, 255),
                        new Color(254, 254, 254, 255)
                    };

                    Paint frameMainGradient1 = new ConicalGradientPaint(true, FRAME_MAIN_CENTER, 0, frameMainFractions1, frameMainColors1);
                    G2.setPaint(frameMainGradient1);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                case METAL:
                    float[] frameMainFractions2 = {
                        0.0f,
                        0.07f,
                        0.12f,
                        1.0f
                    };

                    Color[] frameMainColors2 = {
                        new Color(254, 254, 254, 255),
                        new Color(210, 210, 210, 255),
                        new Color(179, 179, 179, 255),
                        new Color(213, 213, 213, 255)
                    };

                    Paint frameMainGradient2 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions2, frameMainColors2);
                    G2.setPaint(frameMainGradient2);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                case SHINY_METAL:
                    float[] frameMainFractions3 = {
                        0.0f,
                        90.0f - 2 * ANGLE_OFFSET,
                        90.0f,
                        90.0f + 4 * ANGLE_OFFSET,
                        180.0f,
                        270.0f - 4 * ANGLE_OFFSET,
                        270.0f,
                        270.0f + 2 * ANGLE_OFFSET,
                        1.0f
                    };

                    Color[] frameMainColors3;
                    if (isFrameBaseColorEnabled()) {
                        frameMainColors3 = new Color[]{
                            new Color(254, 254, 254, 255),
                            new Color(getFrameBaseColor().getRed(), getFrameBaseColor().getGreen(), getFrameBaseColor().getBlue(), 255),
                            new Color(getFrameBaseColor().brighter().brighter().getRed(), getFrameBaseColor().brighter().brighter().getGreen(), getFrameBaseColor().brighter().brighter().getBlue(), 255),
                            new Color(getFrameBaseColor().getRed(), getFrameBaseColor().getGreen(), getFrameBaseColor().getBlue(), 255),
                            new Color(getFrameBaseColor().getRed(), getFrameBaseColor().getGreen(), getFrameBaseColor().getBlue(), 255),
                            new Color(getFrameBaseColor().getRed(), getFrameBaseColor().getGreen(), getFrameBaseColor().getBlue(), 255),
                            new Color(getFrameBaseColor().brighter().brighter().getRed(), getFrameBaseColor().brighter().brighter().getGreen(), getFrameBaseColor().brighter().brighter().getBlue(), 255),
                            new Color(getFrameBaseColor().getRed(), getFrameBaseColor().getGreen(), getFrameBaseColor().getBlue(), 255),
                            new Color(254, 254, 254, 255)
                        };
                    } else {
                        frameMainColors3 = new Color[]{
                            new Color(254, 254, 254, 255),
                            new Color(179, 179, 179, 255),
                            new Color(238, 238, 238, 255),
                            new Color(179, 179, 179, 255),
                            new Color(179, 179, 179, 255),
                            new Color(179, 179, 179, 255),
                            new Color(238, 238, 238, 255),
                            new Color(179, 179, 179, 255),
                            new Color(254, 254, 254, 255)
                        };
                    }

                    Paint frameMainGradient3 = new org.mars_sim.msp.ui.steelseries.tools.ConicalGradientPaint(true, FRAME_MAIN_CENTER, 0, frameMainFractions3, frameMainColors3);
                    G2.setPaint(frameMainGradient3);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                case GLOSSY_METAL:
                    final GeneralPath FRAME_GLOSSY1 = new GeneralPath();
                    FRAME_GLOSSY1.setWindingRule(Path2D.WIND_EVEN_ODD);
                    FRAME_GLOSSY1.moveTo(0.9953271028037384 * IMAGE_WIDTH, 0.9953271028037384 * IMAGE_HEIGHT);
                    FRAME_GLOSSY1.curveTo(0.9953271028037384 * IMAGE_WIDTH, 0.9953271028037384 * IMAGE_HEIGHT, 0.9953271028037384 * IMAGE_WIDTH, 0.004672897196261682 * IMAGE_HEIGHT, 0.9953271028037384 * IMAGE_WIDTH, 0.004672897196261682 * IMAGE_HEIGHT);
                    FRAME_GLOSSY1.curveTo(0.3364485981308411 * IMAGE_WIDTH, 0.004672897196261682 * IMAGE_HEIGHT, 0.004672897196261682 * IMAGE_WIDTH, 0.35514018691588783 * IMAGE_HEIGHT, 0.004672897196261682 * IMAGE_WIDTH, 0.9953271028037384 * IMAGE_HEIGHT);
                    FRAME_GLOSSY1.curveTo(0.004672897196261682 * IMAGE_WIDTH, 0.9953271028037384 * IMAGE_HEIGHT, 0.9953271028037384 * IMAGE_WIDTH, 0.9953271028037384 * IMAGE_HEIGHT, 0.9953271028037384 * IMAGE_WIDTH, 0.9953271028037384 * IMAGE_HEIGHT);
                    FRAME_GLOSSY1.closePath();
                    final Area FRAME_GLOSSY_1 = new Area(FRAME_GLOSSY1);
                    FRAME_GLOSSY_1.subtract(SUBTRACT);
                    G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.9906542056074766 * IMAGE_WIDTH, 0.9813084112149533 * IMAGE_HEIGHT), (float)(0.9789719626168224 * IMAGE_WIDTH), new float[]{0.0f, 0.94f, 1.0f}, new Color[]{new Color(0.8235294118f, 0.8235294118f, 0.8235294118f, 1f), new Color(0.8235294118f, 0.8235294118f, 0.8235294118f, 1f), new Color(1f, 1f, 1f, 1f)}));
                    G2.fill(FRAME_GLOSSY_1);

                    final GeneralPath FRAME_GLOSSY2 = new GeneralPath();
                    FRAME_GLOSSY2.setWindingRule(Path2D.WIND_EVEN_ODD);
                    FRAME_GLOSSY2.moveTo(0.9906542056074766 * IMAGE_WIDTH, 0.9906542056074766 * IMAGE_HEIGHT);
                    FRAME_GLOSSY2.curveTo(0.9906542056074766 * IMAGE_WIDTH, 0.9906542056074766 * IMAGE_HEIGHT, 0.9906542056074766 * IMAGE_WIDTH, 0.009345794392523364 * IMAGE_HEIGHT, 0.9906542056074766 * IMAGE_WIDTH, 0.009345794392523364 * IMAGE_HEIGHT);
                    FRAME_GLOSSY2.curveTo(0.3364485981308411 * IMAGE_WIDTH, 0.009345794392523364 * IMAGE_HEIGHT, 0.009345794392523364 * IMAGE_WIDTH, 0.3598130841121495 * IMAGE_HEIGHT, 0.009345794392523364 * IMAGE_WIDTH, 0.9906542056074766 * IMAGE_HEIGHT);
                    FRAME_GLOSSY2.curveTo(0.009345794392523364 * IMAGE_WIDTH, 0.9906542056074766 * IMAGE_HEIGHT, 0.9906542056074766 * IMAGE_WIDTH, 0.9906542056074766 * IMAGE_HEIGHT, 0.9906542056074766 * IMAGE_WIDTH, 0.9906542056074766 * IMAGE_HEIGHT);
                    FRAME_GLOSSY2.closePath();
                    final Area FRAME_GLOSSY_2 = new Area(FRAME_GLOSSY2);
                    FRAME_GLOSSY_1.subtract(SUBTRACT);
                    G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.9953271028037384 * IMAGE_WIDTH, 0.004672897196261682 * IMAGE_HEIGHT), new Point2D.Double(0.9953271028037384 * IMAGE_WIDTH, 0.9906542056074766 * IMAGE_HEIGHT), new float[]{0.0f, 0.18f, 0.32f, 0.66f, 0.89f, 1.0f}, new Color[]{new Color(0.9764705882f, 0.9764705882f, 0.9764705882f, 1f), new Color(0.7843137255f, 0.7647058824f, 0.7490196078f, 1f), new Color(0.9960784314f, 0.9960784314f, 0.9921568627f, 1f), new Color(0.1137254902f, 0.1137254902f, 0.1137254902f, 1f), new Color(0.7843137255f, 0.7647058824f, 0.7490196078f, 1f), new Color(0.8196078431f, 0.8196078431f, 0.8196078431f, 1f)}));
                    G2.fill(FRAME_GLOSSY_2);

                    final GeneralPath FRAME_GLOSSY3 = new GeneralPath();
                    FRAME_GLOSSY3.setWindingRule(Path2D.WIND_EVEN_ODD);
                    FRAME_GLOSSY3.moveTo(0.9299065420560748 * IMAGE_WIDTH, 0.9299065420560748 * IMAGE_HEIGHT);
                    FRAME_GLOSSY3.curveTo(0.9299065420560748 * IMAGE_WIDTH, 0.9299065420560748 * IMAGE_HEIGHT, 0.9299065420560748 * IMAGE_WIDTH, 0.06542056074766354 * IMAGE_HEIGHT, 0.9299065420560748 * IMAGE_WIDTH, 0.06542056074766354 * IMAGE_HEIGHT);
                    FRAME_GLOSSY3.curveTo(0.40654205607476634 * IMAGE_WIDTH, 0.06542056074766354 * IMAGE_HEIGHT, 0.07009345794392523 * IMAGE_WIDTH, 0.37383177570093457 * IMAGE_HEIGHT, 0.07009345794392523 * IMAGE_WIDTH, 0.9299065420560748 * IMAGE_HEIGHT);
                    FRAME_GLOSSY3.curveTo(0.07009345794392523 * IMAGE_WIDTH, 0.9299065420560748 * IMAGE_HEIGHT, 0.9299065420560748 * IMAGE_WIDTH, 0.9299065420560748 * IMAGE_HEIGHT, 0.9299065420560748 * IMAGE_WIDTH, 0.9299065420560748 * IMAGE_HEIGHT);
                    FRAME_GLOSSY3.closePath();
                    final Area FRAME_GLOSSY_3 = new Area(FRAME_GLOSSY3);
                    FRAME_GLOSSY_3.subtract(SUBTRACT);
                    G2.setPaint(new Color(0.9647058824f, 0.9647058824f, 0.9647058824f, 1f));
                    G2.fill(FRAME_GLOSSY_3);

                    final GeneralPath FRAME_GLOSSY4 = new GeneralPath();
                    FRAME_GLOSSY4.setWindingRule(Path2D.WIND_EVEN_ODD);
                    FRAME_GLOSSY4.moveTo(0.9252336448598131 * IMAGE_WIDTH, 0.9252336448598131 * IMAGE_HEIGHT);
                    FRAME_GLOSSY4.curveTo(0.9252336448598131 * IMAGE_WIDTH, 0.9252336448598131 * IMAGE_HEIGHT, 0.9252336448598131 * IMAGE_WIDTH, 0.07009345794392523 * IMAGE_HEIGHT, 0.9252336448598131 * IMAGE_WIDTH, 0.07009345794392523 * IMAGE_HEIGHT);
                    FRAME_GLOSSY4.curveTo(0.3878504672897196 * IMAGE_WIDTH, 0.07009345794392523 * IMAGE_HEIGHT, 0.07476635514018691 * IMAGE_WIDTH, 0.4158878504672897 * IMAGE_HEIGHT, 0.07476635514018691 * IMAGE_WIDTH, 0.9252336448598131 * IMAGE_HEIGHT);
                    FRAME_GLOSSY4.curveTo(0.07476635514018691 * IMAGE_WIDTH, 0.9252336448598131 * IMAGE_HEIGHT, 0.9252336448598131 * IMAGE_WIDTH, 0.9252336448598131 * IMAGE_HEIGHT, 0.9252336448598131 * IMAGE_WIDTH, 0.9252336448598131 * IMAGE_HEIGHT);
                    FRAME_GLOSSY4.closePath();
                    final Area FRAME_GLOSSY_4 = new Area(FRAME_GLOSSY4);
                    FRAME_GLOSSY_4.subtract(SUBTRACT);
                    G2.setPaint(new Color(0.2f, 0.2f, 0.2f, 1f));
                    G2.fill(FRAME_GLOSSY_4);
                    break;

                case BRASS:
                    float[] frameMainFractions5 = {
                        0.0f,
                        0.05f,
                        0.10f,
                        0.50f,
                        0.90f,
                        0.95f,
                        1.0f
                    };

                    Color[] frameMainColors5 = {
                        new Color(249, 243, 155, 255),
                        new Color(246, 226, 101, 255),
                        new Color(240, 225, 132, 255),
                        new Color(90, 57, 22, 255),
                        new Color(249, 237, 139, 255),
                        new Color(243, 226, 108, 255),
                        new Color(202, 182, 113, 255)
                    };
                    Paint frameMainGradient5 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions5, frameMainColors5);
                    G2.setPaint(frameMainGradient5);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                case STEEL:
                    float[] frameMainFractions6 = {
                        0.0f,
                        0.05f,
                        0.10f,
                        0.50f,
                        0.90f,
                        0.95f,
                        1.0f
                    };

                    Color[] frameMainColors6 = {
                        new Color(231, 237, 237, 255),
                        new Color(189, 199, 198, 255),
                        new Color(192, 201, 200, 255),
                        new Color(23, 31, 33, 255),
                        new Color(196, 205, 204, 255),
                        new Color(194, 204, 203, 255),
                        new Color(189, 201, 199, 255)
                    };
                    Paint frameMainGradient6 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions6, frameMainColors6);
                    G2.setPaint(frameMainGradient6);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                case CHROME:
                    float[] frameMainFractions7 = {
                        0.0f,
                        0.09f,
                        0.12f,
                        0.16f,
                        0.25f,
                        0.29f,
                        0.33f,
                        0.38f,
                        0.48f,
                        0.52f,
                        0.63f,
                        0.68f,
                        0.8f,
                        0.83f,
                        0.87f,
                        0.97f,
                        1.0f
                    };

                    Color[] frameMainColors7 = {
                        new Color(255, 255, 255, 255),
                        new Color(255, 255, 255, 255),
                        new Color(136, 136, 138, 255),
                        new Color(164, 185, 190, 255),
                        new Color(158, 179, 182, 255),
                        new Color(112, 112, 112, 255),
                        new Color(221, 227, 227, 255),
                        new Color(155, 176, 179, 255),
                        new Color(156, 176, 177, 255),
                        new Color(254, 255, 255, 255),
                        new Color(255, 255, 255, 255),
                        new Color(156, 180, 180, 255),
                        new Color(198, 209, 211, 255),
                        new Color(246, 248, 247, 255),
                        new Color(204, 216, 216, 255),
                        new Color(164, 188, 190, 255),
                        new Color(255, 255, 255, 255)
                    };

                    Paint frameMainGradient7 = new org.mars_sim.msp.ui.steelseries.tools.ConicalGradientPaint(false, FRAME_MAIN_CENTER, 0, frameMainFractions7, frameMainColors7);
                    G2.setPaint(frameMainGradient7);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                case GOLD:
                    float[] frameMainFractions8 = {
                        0.0f,
                        0.15f,
                        0.22f,
                        0.3f,
                        0.38f,
                        0.44f,
                        0.51f,
                        0.6f,
                        0.68f,
                        0.75f,
                        1.0f
                    };

                    Color[] frameMainColors8 = {
                        new Color(255, 255, 207, 255),
                        new Color(255, 237, 96, 255),
                        new Color(254, 199, 57, 255),
                        new Color(255, 249, 203, 255),
                        new Color(255, 199, 64, 255),
                        new Color(252, 194, 60, 255),
                        new Color(255, 204, 59, 255),
                        new Color(213, 134, 29, 255),
                        new Color(255, 201, 56, 255),
                        new Color(212, 135, 29, 255),
                        new Color(247, 238, 101, 255)
                    };
                    Paint frameMainGradient8 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions8, frameMainColors8);
                    G2.setPaint(frameMainGradient8);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                case ANTHRACITE:
                    float[] frameMainFractions9 = {
                        0.0f,
                        0.06f,
                        0.12f,
                        1.0f
                    };
                    Color[] frameMainColors9 = {
                        new Color(118, 117, 135, 255),
                        new Color(74, 74, 82, 255),
                        new Color(50, 50, 54, 255),
                        new Color(97, 97, 108, 255)
                    };
                    Paint frameMainGradient9 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions9, frameMainColors9);
                    G2.setPaint(frameMainGradient9);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                case TILTED_GRAY:
                    FRAME_MAIN_START.setLocation((0.2336448598130841 * IMAGE_WIDTH), (0.08411214953271028 * IMAGE_HEIGHT));
                    FRAME_MAIN_STOP.setLocation(((0.2336448598130841 + 0.5789369637935792) * IMAGE_WIDTH), ((0.08411214953271028 + 0.8268076708711319) * IMAGE_HEIGHT));
                    float[] frameMainFractions10 = {
                        0.0f,
                        0.07f,
                        0.16f,
                        0.33f,
                        0.55f,
                        0.79f,
                        1.0f
                    };
                    Color[] frameMainColors10 = {
                        new Color(255, 255, 255, 255),
                        new Color(210, 210, 210, 255),
                        new Color(179, 179, 179, 255),
                        new Color(255, 255, 255, 255),
                        new Color(197, 197, 197, 255),
                        new Color(255, 255, 255, 255),
                        new Color(102, 102, 102, 255)
                    };
                    Paint frameMainGradient10 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions10, frameMainColors10);
                    G2.setPaint(frameMainGradient10);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                case TILTED_BLACK:
                    FRAME_MAIN_START.setLocation((0.22897196261682243 * IMAGE_WIDTH), (0.0794392523364486 * IMAGE_HEIGHT));
                    FRAME_MAIN_STOP.setLocation(((0.22897196261682243 + 0.573576436351046) * IMAGE_WIDTH), ((0.0794392523364486 + 0.8191520442889918) * IMAGE_HEIGHT));
                    float[] frameMainFractions11 = {
                        0.0f,
                        0.21f,
                        0.47f,
                        0.99f,
                        1.0f
                    };
                    Color[] frameMainColors11 = {
                        new Color(102, 102, 102, 255),
                        new Color(0, 0, 0, 255),
                        new Color(102, 102, 102, 255),
                        new Color(0, 0, 0, 255),
                        new Color(0, 0, 0, 255)
                    };
                    Paint frameMainGradient11 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions11, frameMainColors11);
                    G2.setPaint(frameMainGradient11);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                default:
                    float[] frameMainFractions = {
                        0.0f,
                        0.07f,
                        0.12f,
                        1.0f
                    };

                    Color[] frameMainColors = {
                        new Color(254, 254, 254, 255),
                        new Color(210, 210, 210, 255),
                        new Color(179, 179, 179, 255),
                        new Color(213, 213, 213, 255)
                    };

                    Paint frameMainGradient = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions, frameMainColors);
                    G2.setPaint(frameMainGradient);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;
            }
        }

        // Apply frame effects
        final float[] EFFECT_FRACTIONS;
        final Color[] EFFECT_COLORS;
        final GradientWrapper EFFECT_GRADIENT;
        float scale = 1.0f;
        final Shape[] EFFECT = new Shape[100];
        switch (getFrameEffect()) {
            case EFFECT_BULGE:
                EFFECT_FRACTIONS = new float[]{
                    0.0f,
                    0.13f,
                    0.14f,
                    0.17f,
                    0.18f,
                    1.0f
                };
                EFFECT_COLORS = new Color[]{
                    new Color(0, 0, 0, 102),            // Outside
                    new Color(255, 255, 255, 151),
                    new Color(219, 219, 219, 153),
                    new Color(0, 0, 0, 95),
                    new Color(0, 0, 0, 76),       // Inside
                    new Color(0, 0, 0, 0)
                };
                EFFECT_GRADIENT = new GradientWrapper(new Point2D.Double(100, 0), new Point2D.Double(0, 0), EFFECT_FRACTIONS, EFFECT_COLORS);
                for (int i = 0; i < 100; i++) {
                    EFFECT[i] = Scaler.INSTANCE.scale(FRAME_MAIN_AREA, scale);
                    scale -= 0.01f;
                }
                G2.setStroke(new BasicStroke(1.5f));
                for (int i = 0; i < EFFECT.length; i++) {
                    G2.setPaint(EFFECT_GRADIENT.getColorAt(i / 100f));
                    G2.draw(EFFECT[i]);
                }
                break;

            case EFFECT_CONE:
                EFFECT_FRACTIONS = new float[]{
                    0.0f,
                    0.0399f,
                    0.04f,
                    0.1799f,
                    0.18f,
                    1.0f
                };
                EFFECT_COLORS = new Color[]{
                    new Color(0, 0, 0, 76),
                    new Color(223, 223, 223, 127),
                    new Color(255, 255, 255, 124),
                    new Color(9, 9, 9, 51),
                    new Color(0, 0, 0, 50),
                    new Color(0, 0, 0, 0)
                };
                EFFECT_GRADIENT = new GradientWrapper(new Point2D.Double(100, 0), new Point2D.Double(0, 0), EFFECT_FRACTIONS, EFFECT_COLORS);
                for (int i = 0; i < 100; i++) {
                    EFFECT[i] = Scaler.INSTANCE.scale(FRAME_MAIN_AREA, scale);
                    scale -= 0.01f;
                }
                G2.setStroke(new BasicStroke(1.5f));
                for (int i = 0; i < EFFECT.length; i++) {
                    G2.setPaint(EFFECT_GRADIENT.getColorAt(i / 100f));
                    G2.draw(EFFECT[i]);
                }
                break;

            case EFFECT_TORUS:
                EFFECT_FRACTIONS = new float[]{
                    0.0f,
                    0.08f,
                    0.1799f,
                    0.18f,
                    1.0f
                };
                EFFECT_COLORS = new Color[]{
                    new Color(0, 0, 0, 76),
                    new Color(255, 255, 255, 64),
                    new Color(13, 13, 13, 51),
                    new Color(0, 0, 0, 50),
                    new Color(0, 0, 0, 0)
                };
                EFFECT_GRADIENT = new GradientWrapper(new Point2D.Double(100, 0), new Point2D.Double(0, 0), EFFECT_FRACTIONS, EFFECT_COLORS);
                for (int i = 0; i < 100; i++) {
                    EFFECT[i] = Scaler.INSTANCE.scale(FRAME_MAIN_AREA, scale);
                    scale -= 0.01f;
                }
                G2.setStroke(new BasicStroke(1.5f));
                for (int i = 0; i < EFFECT.length; i++) {
                    G2.setPaint(EFFECT_GRADIENT.getColorAt(i / 100f));
                    G2.draw(EFFECT[i]);
                }
                break;

            case EFFECT_INNER_FRAME:
                final java.awt.Shape EFFECT_BIGINNERFRAME = Scaler.INSTANCE.scale(FRAME_MAIN_AREA, 0.8785046339035034);
                final Point2D EFFECT_BIGINNERFRAME_START = new Point2D.Double(0, EFFECT_BIGINNERFRAME.getBounds2D().getMinY());
                final Point2D EFFECT_BIGINNERFRAME_STOP = new Point2D.Double(0, EFFECT_BIGINNERFRAME.getBounds2D().getMaxY());
                EFFECT_FRACTIONS = new float[]{
                    0.0f,
                    0.3f,
                    0.5f,
                    0.71f,
                    1.0f
                };
                EFFECT_COLORS = new Color[]{
                    new Color(0, 0, 0, 183),
                    new Color(148, 148, 148, 25),
                    new Color(0, 0, 0, 159),
                    new Color(0, 0, 0, 81),
                    new Color(255, 255, 255, 158)
                };
                final LinearGradientPaint EFFECT_BIGINNERFRAME_GRADIENT = new LinearGradientPaint(EFFECT_BIGINNERFRAME_START, EFFECT_BIGINNERFRAME_STOP, EFFECT_FRACTIONS, EFFECT_COLORS);
                G2.setPaint(EFFECT_BIGINNERFRAME_GRADIENT);
                G2.fill(EFFECT_BIGINNERFRAME);
                break;
        }

        final GeneralPath GAUGE_BACKGROUND_MAIN = new GeneralPath();
        GAUGE_BACKGROUND_MAIN.setWindingRule(Path2D.WIND_EVEN_ODD);
        GAUGE_BACKGROUND_MAIN.moveTo(IMAGE_WIDTH * 0.9205607476635514, IMAGE_HEIGHT * 0.9205607476635514);
        GAUGE_BACKGROUND_MAIN.curveTo(IMAGE_WIDTH * 0.9205607476635514, IMAGE_HEIGHT * 0.9205607476635514, IMAGE_WIDTH * 0.9205607476635514, IMAGE_HEIGHT * 0.0794392523364486, IMAGE_WIDTH * 0.9205607476635514, IMAGE_HEIGHT * 0.0794392523364486);
        GAUGE_BACKGROUND_MAIN.curveTo(IMAGE_WIDTH * 0.6822429906542056, IMAGE_HEIGHT * 0.0794392523364486, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.13551401869158877, IMAGE_WIDTH * 0.3037383177570093, IMAGE_HEIGHT * 0.308411214953271);
        GAUGE_BACKGROUND_MAIN.curveTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.4439252336448598, IMAGE_WIDTH * 0.0794392523364486, IMAGE_HEIGHT * 0.6822429906542056, IMAGE_WIDTH * 0.0794392523364486, IMAGE_HEIGHT * 0.9205607476635514);
        GAUGE_BACKGROUND_MAIN.curveTo(IMAGE_WIDTH * 0.0794392523364486, IMAGE_HEIGHT * 0.9205607476635514, IMAGE_WIDTH * 0.9205607476635514, IMAGE_HEIGHT * 0.9205607476635514, IMAGE_WIDTH * 0.9205607476635514, IMAGE_HEIGHT * 0.9205607476635514);
        GAUGE_BACKGROUND_MAIN.closePath();
        G2.setColor(Color.WHITE);
        final java.awt.geom.Area GAUGE_BACKGROUND_MAIN_AREA = new java.awt.geom.Area(GAUGE_BACKGROUND_MAIN);
        GAUGE_BACKGROUND_MAIN_AREA.subtract(SUBTRACT);
        G2.fill(GAUGE_BACKGROUND_MAIN_AREA);

        G2.dispose();

        return image;
    }

    private BufferedImage create_BACKGROUND_Image(final int WIDTH, BufferedImage image) {
        if (WIDTH <= 0) {
            return null;
        }
        if (image == null) {
            image = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        }

        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        boolean fadeInOut = false;

        final AffineTransform OLD_TRANSFORM = G2.getTransform();
        transformGraphics(IMAGE_WIDTH, IMAGE_HEIGHT, G2);
        final AffineTransform NEW_TRANSFORM = G2.getTransform();

        final GeneralPath GAUGE_BACKGROUND = new GeneralPath();
        GAUGE_BACKGROUND.setWindingRule(Path2D.WIND_EVEN_ODD);
        GAUGE_BACKGROUND.moveTo(IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.9158878504672897);
        GAUGE_BACKGROUND.curveTo(IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.9158878504672897, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.08411214953271028, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.08411214953271028);
        GAUGE_BACKGROUND.curveTo(IMAGE_WIDTH * 0.6401869158878505, IMAGE_HEIGHT * 0.08411214953271028, IMAGE_WIDTH * 0.46261682242990654, IMAGE_HEIGHT * 0.1588785046728972, IMAGE_WIDTH * 0.29439252336448596, IMAGE_HEIGHT * 0.32242990654205606);
        GAUGE_BACKGROUND.curveTo(IMAGE_WIDTH * 0.17289719626168223, IMAGE_HEIGHT * 0.4439252336448598, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.6635514018691588, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.9158878504672897);
        GAUGE_BACKGROUND.curveTo(IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.9158878504672897, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.9158878504672897, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.9158878504672897);
        GAUGE_BACKGROUND.closePath();
        final Point2D GAUGE_BACKGROUND_START;
        final Point2D GAUGE_BACKGROUND_STOP;
        switch (getOrientation()) {
            case NORTH_WEST:
                GAUGE_BACKGROUND_START = new Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMinY());
                GAUGE_BACKGROUND_STOP = new Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMaxY());
                break;
            case NORTH_EAST:
                GAUGE_BACKGROUND_START = new Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMinY());
                GAUGE_BACKGROUND_STOP = new Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMaxY());
                break;
            case SOUTH_EAST:
                GAUGE_BACKGROUND_START = new Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMaxY());
                GAUGE_BACKGROUND_STOP = new Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMinY());
                break;
            case SOUTH_WEST:
                GAUGE_BACKGROUND_START = new Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMaxY());
                GAUGE_BACKGROUND_STOP = new Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMinY());
                break;
            default:
                GAUGE_BACKGROUND_START = new Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMinY());
                GAUGE_BACKGROUND_STOP = new Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMaxY());
        }

        final float[] GAUGE_BACKGROUND_FRACTIONS = {
            0.0f,
            0.39f,
            1.0f
        };

        Paint backgroundPaint = null;

        // Set custom background paint if selected
        if (getCustomBackground() != null && getBackgroundColor() == BackgroundColor.CUSTOM) {
            G2.setPaint(getCustomBackground());
        } else {
            final Color[] GAUGE_BACKGROUND_COLORS = {
                getBackgroundColor().GRADIENT_START_COLOR,
                getBackgroundColor().GRADIENT_FRACTION_COLOR,
                getBackgroundColor().GRADIENT_STOP_COLOR
            };

            if (getBackgroundColor() == BackgroundColor.BRUSHED_METAL) {
                backgroundPaint = new TexturePaint(UTIL.createBrushMetalTexture(getModel().getTextureColor(), GAUGE_BACKGROUND.getBounds().width, GAUGE_BACKGROUND.getBounds().height), GAUGE_BACKGROUND.getBounds());
            } else if (getBackgroundColor() == BackgroundColor.STAINLESS) {
                final float[] STAINLESS_FRACTIONS = {
                    0f,
                    0.03f,
                    0.10f,
                    0.14f,
                    0.24f,
                    0.33f,
                    0.38f,
                    0.5f,
                    0.62f,
                    0.67f,
                    0.76f,
                    0.81f,
                    0.85f,
                    0.97f,
                    1.0f
                };

                // Define the colors of the conical gradient paint
                final Color[] STAINLESS_COLORS = {
                    new Color(0xFDFDFD),
                    new Color(0xFDFDFD),
                    new Color(0xB2B2B4),
                    new Color(0xACACAE),
                    new Color(0xFDFDFD),
                    new Color(0x6E6E70),
                    new Color(0x6E6E70),
                    new Color(0xFDFDFD),
                    new Color(0x6E6E70),
                    new Color(0x6E6E70),
                    new Color(0xFDFDFD),
                    new Color(0xACACAE),
                    new Color(0xB2B2B4),
                    new Color(0xFDFDFD),
                    new Color(0xFDFDFD)
                };

                // Define the conical gradient paint
                backgroundPaint = new ConicalGradientPaint(false, ROTATION_CENTER, -0.45f, STAINLESS_FRACTIONS, STAINLESS_COLORS);
            } else if (getBackgroundColor() == BackgroundColor.STAINLESS_GRINDED) {
                backgroundPaint = new TexturePaint(BACKGROUND_FACTORY.STAINLESS_GRINDED_TEXTURE, new java.awt.Rectangle(0, 0, 100, 100));
            } else if (getBackgroundColor() == BackgroundColor.CARBON) {
                backgroundPaint = new TexturePaint(BACKGROUND_FACTORY.CARBON_FIBRE_TEXTURE, new java.awt.Rectangle(0, 0, 12, 12));
                fadeInOut = true;
            } else if (getBackgroundColor() == BackgroundColor.PUNCHED_SHEET) {
                backgroundPaint = new TexturePaint(BACKGROUND_FACTORY.getPunchedSheetTexture(), new java.awt.Rectangle(0, 0, 12, 12));
                fadeInOut = true;
            } else if (getBackgroundColor() == BackgroundColor.LINEN) {
                backgroundPaint = new TexturePaint(UTIL.createLinenTexture(getModel().getTextureColor(), GAUGE_BACKGROUND.getBounds().width, GAUGE_BACKGROUND.getBounds().height), GAUGE_BACKGROUND.getBounds());
            } else if (getBackgroundColor() == BackgroundColor.NOISY_PLASTIC) {
                GAUGE_BACKGROUND_START.setLocation(0.0, GAUGE_BACKGROUND.getBounds2D().getMinY());
                GAUGE_BACKGROUND_STOP.setLocation(0.0, GAUGE_BACKGROUND.getBounds2D().getMaxY());
                if (GAUGE_BACKGROUND_START.equals(GAUGE_BACKGROUND_STOP)) {
                    GAUGE_BACKGROUND_STOP.setLocation(0.0, GAUGE_BACKGROUND_START.getY() + 1);
                }
                final float[] FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] COLORS = {
                    UTIL.lighter(getTextureColor(), 0.15f),
                    UTIL.darker(getTextureColor(), 0.15f)
                };
                backgroundPaint = new LinearGradientPaint(GAUGE_BACKGROUND_START, GAUGE_BACKGROUND_STOP, FRACTIONS, COLORS);
            } else {
                backgroundPaint = new LinearGradientPaint(GAUGE_BACKGROUND_START, GAUGE_BACKGROUND_STOP, GAUGE_BACKGROUND_FRACTIONS, GAUGE_BACKGROUND_COLORS);
            }
            G2.setPaint(backgroundPaint);
        }
        G2.fill(GAUGE_BACKGROUND);

        // Create inner shadow on background shape
        final BufferedImage CLP;
        if (getCustomBackground() != null && getBackgroundColor() == BackgroundColor.CUSTOM) {
            CLP = Shadow.INSTANCE.createInnerShadow((Shape) GAUGE_BACKGROUND, getCustomBackground(), 0, 0.65f, Color.BLACK, 20, 315);
        } else {
            CLP = Shadow.INSTANCE.createInnerShadow((Shape) GAUGE_BACKGROUND, backgroundPaint, 0, 0.65f, Color.BLACK, 20, 315);
        }
        G2.drawImage(CLP, GAUGE_BACKGROUND.getBounds().x, GAUGE_BACKGROUND.getBounds().y, null);

        // add noise if NOISY_PLASTIC
        if (getBackgroundColor() == BackgroundColor.NOISY_PLASTIC) {
            final Random BW_RND = new Random();
            final Random ALPHA_RND = new Random();
            final Shape OLD_CLIP = G2.getClip();
            G2.setClip(GAUGE_BACKGROUND);
            Color noiseColor;
            int noiseAlpha;
            for (int y = 0 ; y < GAUGE_BACKGROUND.getBounds().getHeight() ; y ++) {
                for (int x = 0 ; x < GAUGE_BACKGROUND.getBounds().getWidth() ; x ++) {
                    if (BW_RND.nextBoolean()) {
                        noiseColor = BRIGHT_NOISE;
                    } else {
                        noiseColor = DARK_NOISE;
                    }
                    noiseAlpha = 10 + ALPHA_RND.nextInt(10) - 5;
                    G2.setColor(new Color(noiseColor.getRed(), noiseColor.getGreen(), noiseColor.getBlue(), noiseAlpha));
                    G2.drawLine((int) (x + GAUGE_BACKGROUND.getBounds2D().getMinX()), (int) (y + GAUGE_BACKGROUND.getBounds2D().getMinY()), (int) (x + GAUGE_BACKGROUND.getBounds2D().getMinX()), (int) (y + GAUGE_BACKGROUND.getBounds2D().getMinY()));
                }
            }
            G2.setClip(OLD_CLIP);
        }

        // Draw an overlay gradient that gives the carbon fibre a more realistic look
        if (fadeInOut) {
            final float[] SHADOW_OVERLAY_FRACTIONS = {
                0.0f,
                0.4f,
                0.6f,
                1.0f
            };
            final Color[] SHADOW_OVERLAY_COLORS = {
                new Color(0f, 0f, 0f, 0.6f),
                new Color(0f, 0f, 0f, 0.0f),
                new Color(0f, 0f, 0f, 0.0f),
                new Color(0f, 0f, 0f, 0.6f)
            };
            final LinearGradientPaint SHADOW_OVERLAY_GRADIENT = new LinearGradientPaint(new Point2D.Double(GAUGE_BACKGROUND.getBounds().getMinX(), 0), new Point2D.Double(GAUGE_BACKGROUND.getBounds().getMaxX(), 0), SHADOW_OVERLAY_FRACTIONS, SHADOW_OVERLAY_COLORS);
            G2.setPaint(SHADOW_OVERLAY_GRADIENT);
            G2.fill(GAUGE_BACKGROUND);
        }

        // Draw the custom layer if selected
        if (isCustomLayerVisible()) {
            G2.setTransform(OLD_TRANSFORM);
            G2.drawImage(UTIL.getScaledInstance(getCustomLayer(), IMAGE_WIDTH, IMAGE_HEIGHT, RenderingHints.VALUE_INTERPOLATION_BICUBIC), 0, 0, null);
            G2.setTransform(NEW_TRANSFORM);
        }

        G2.dispose();

        return image;
    }

    @Override
    protected BufferedImage create_POINTER_Image(final int WIDTH, PointerType POINTER_TYPE) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        transformGraphics(IMAGE_WIDTH, IMAGE_HEIGHT, G2);

        final GeneralPath POINTER;
        final Point2D POINTER_START;
        final Point2D POINTER_STOP;
        final float[] POINTER_FRACTIONS;
        final Color[] POINTER_COLORS;
        final java.awt.Paint POINTER_GRADIENT;

        switch (POINTER_TYPE) {
            case TYPE2:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.8130841121495327);
                POINTER.lineTo(IMAGE_WIDTH * 0.7897196261682243, IMAGE_HEIGHT * 0.822429906542056);
                POINTER.lineTo(IMAGE_WIDTH * 0.6635514018691588, IMAGE_HEIGHT * 0.822429906542056);
                POINTER.lineTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.lineTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8317757009345794);
                POINTER.lineTo(IMAGE_WIDTH * 0.6682242990654206, IMAGE_HEIGHT * 0.8364485981308412);
                POINTER.lineTo(IMAGE_WIDTH * 0.7897196261682243, IMAGE_HEIGHT * 0.8364485981308412);
                POINTER.lineTo(IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.8457943925233645);
                POINTER.curveTo(IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.8457943925233645, IMAGE_WIDTH * 0.8177570093457944, IMAGE_HEIGHT * 0.8598130841121495, IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8598130841121495);
                POINTER.curveTo(IMAGE_WIDTH * 0.8457943925233645, IMAGE_HEIGHT * 0.8598130841121495, IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.8457943925233645, IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.curveTo(IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.8084112149532711, IMAGE_WIDTH * 0.8457943925233645, IMAGE_HEIGHT * 0.794392523364486, IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.794392523364486);
                POINTER.curveTo(IMAGE_WIDTH * 0.8177570093457944, IMAGE_HEIGHT * 0.794392523364486, IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.8130841121495327, IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.8130841121495327);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(POINTER.getBounds2D().getMaxX(), 0);
                POINTER_STOP = new Point2D.Double(POINTER.getBounds2D().getMinX(), 0);
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
                        getCustomPointerColorObject().LIGHT,
                        getCustomPointerColorObject().LIGHT
                    };
                }
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                break;

            case TYPE3:
                POINTER = new GeneralPath(new Rectangle2D.Double(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.822429906542056, IMAGE_WIDTH * 0.6775700935, IMAGE_HEIGHT * 0.009345794392523364));
                if (getPointerColor() != ColorDef.CUSTOM) {
                    G2.setColor(getPointerColor().LIGHT);
                } else {
                    G2.setColor(getCustomPointerColorObject().LIGHT);
                }
                G2.fill(POINTER);
                break;

            case TYPE4:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.lineTo(IMAGE_WIDTH * 0.17757009345794392, IMAGE_HEIGHT * 0.8084112149532711);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.794392523364486);
                POINTER.lineTo(IMAGE_WIDTH * 0.897196261682243, IMAGE_HEIGHT * 0.8037383177570093);
                POINTER.lineTo(IMAGE_WIDTH * 0.897196261682243, IMAGE_HEIGHT * 0.8504672897196262);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8598130841121495);
                POINTER.lineTo(IMAGE_WIDTH * 0.17757009345794392, IMAGE_HEIGHT * 0.8411214953271028);
                POINTER.lineTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(0, POINTER.getBounds2D().getMaxY());
                POINTER_STOP = new Point2D.Double(0, POINTER.getBounds2D().getMinY());
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
                        getCustomPointerColorObject().DARK,
                        getCustomPointerColorObject().DARK,
                        getCustomPointerColorObject().LIGHT,
                        getCustomPointerColorObject().LIGHT
                    };
                }
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                break;

            case TYPE5:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.7990654205607477);
                POINTER.lineTo(IMAGE_WIDTH * 0.16822429906542055, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8551401869158879);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(0, POINTER.getBounds2D().getMaxY());
                POINTER_STOP = new Point2D.Double(0, POINTER.getBounds2D().getMinY());
                POINTER_FRACTIONS = new float[]{
                    0.0f,
                    0.45f,
                    0.46f,
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
                POINTER.moveTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8457943925233645);
                POINTER.lineTo(IMAGE_WIDTH * 0.6495327102803738, IMAGE_HEIGHT * 0.8457943925233645);
                POINTER.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.8411214953271028);
                POINTER.lineTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8317757009345794);
                POINTER.lineTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.822429906542056);
                POINTER.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.8130841121495327);
                POINTER.lineTo(IMAGE_WIDTH * 0.6448598130841121, IMAGE_HEIGHT * 0.8084112149532711);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8084112149532711);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.822429906542056);
                POINTER.lineTo(IMAGE_WIDTH * 0.6495327102803738, IMAGE_HEIGHT * 0.822429906542056);
                POINTER.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.lineTo(IMAGE_WIDTH * 0.6495327102803738, IMAGE_HEIGHT * 0.8317757009345794);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8317757009345794);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8457943925233645);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(0, POINTER.getBounds2D().getMaxX());
                POINTER_STOP = new Point2D.Double(0, POINTER.getBounds2D().getMinX());
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
                POINTER.moveTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8364485981308412);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8504672897196262);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8084112149532711);
                POINTER.lineTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.822429906542056);
                POINTER.lineTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8364485981308412);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(0, POINTER.getBounds2D().getMaxY());
                POINTER_STOP = new Point2D.Double(0, POINTER.getBounds2D().getMinY());
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
                POINTER.moveTo(IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.794392523364486);
                POINTER.curveTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.794392523364486, IMAGE_WIDTH * 0.7850467289719626, IMAGE_HEIGHT * 0.8177570093457944, IMAGE_WIDTH * 0.16822429906542055, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.curveTo(IMAGE_WIDTH * 0.7850467289719626, IMAGE_HEIGHT * 0.8411214953271028, IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8598130841121495, IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8598130841121495);
                POINTER.lineTo(IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(0, POINTER.getBounds2D().getMaxY());
                POINTER_STOP = new Point2D.Double(0, POINTER.getBounds2D().getMinY());
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
                POINTER.moveTo(IMAGE_WIDTH * 0.29439252336448596, IMAGE_HEIGHT * 0.8317757009345794);
                POINTER.lineTo(IMAGE_WIDTH * 0.29439252336448596, IMAGE_HEIGHT * 0.822429906542056);
                POINTER.lineTo(IMAGE_WIDTH * 0.7663551401869159, IMAGE_HEIGHT * 0.8130841121495327);
                POINTER.lineTo(IMAGE_WIDTH * 0.7663551401869159, IMAGE_HEIGHT * 0.8411214953271028);
                POINTER.lineTo(IMAGE_WIDTH * 0.29439252336448596, IMAGE_HEIGHT * 0.8317757009345794);
                POINTER.closePath();
                POINTER.moveTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8364485981308412);
                POINTER.lineTo(IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.8551401869158879);
                POINTER.lineTo(IMAGE_WIDTH * 0.8551401869158879, IMAGE_HEIGHT * 0.8551401869158879);
                POINTER.curveTo(IMAGE_WIDTH * 0.8551401869158879, IMAGE_HEIGHT * 0.8551401869158879, IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.8504672897196262, IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.8504672897196262);
                POINTER.curveTo(IMAGE_WIDTH * 0.8925233644859814, IMAGE_HEIGHT * 0.8504672897196262, IMAGE_WIDTH * 0.8925233644859814, IMAGE_HEIGHT * 0.8457943925233645, IMAGE_WIDTH * 0.8925233644859814, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.curveTo(IMAGE_WIDTH * 0.8925233644859814, IMAGE_HEIGHT * 0.8084112149532711, IMAGE_WIDTH * 0.8925233644859814, IMAGE_HEIGHT * 0.8037383177570093, IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.8037383177570093);
                POINTER.curveTo(IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.8037383177570093, IMAGE_WIDTH * 0.8551401869158879, IMAGE_HEIGHT * 0.7990654205607477, IMAGE_WIDTH * 0.8551401869158879, IMAGE_HEIGHT * 0.7990654205607477);
                POINTER.lineTo(IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.7990654205607477);
                POINTER.lineTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8177570093457944);
                POINTER.lineTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8364485981308412);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(0, POINTER.getBounds2D().getMaxY());
                POINTER_STOP = new Point2D.Double(0, POINTER.getBounds2D().getMinY());
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
                COLOR_BOX.moveTo(IMAGE_WIDTH * 0.2523364485981308, IMAGE_HEIGHT * 0.8317757009345794);
                COLOR_BOX.lineTo(IMAGE_WIDTH * 0.2523364485981308, IMAGE_HEIGHT * 0.822429906542056);
                COLOR_BOX.lineTo(IMAGE_WIDTH * 0.16822429906542055, IMAGE_HEIGHT * 0.822429906542056);
                COLOR_BOX.lineTo(IMAGE_WIDTH * 0.16822429906542055, IMAGE_HEIGHT * 0.8317757009345794);
                COLOR_BOX.lineTo(IMAGE_WIDTH * 0.2523364485981308, IMAGE_HEIGHT * 0.8317757009345794);
                COLOR_BOX.closePath();
                G2.setColor(getPointerColor().MEDIUM);
                G2.fill(COLOR_BOX);
                break;

            case TYPE10:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.curveTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8271028037383178, IMAGE_WIDTH * 0.822429906542056, IMAGE_HEIGHT * 0.883177570093458, IMAGE_WIDTH * 0.8317757009345794, IMAGE_HEIGHT * 0.883177570093458);
                POINTER.curveTo(IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.883177570093458, IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.8598130841121495, IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.curveTo(IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.7990654205607477, IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.7710280373831776, IMAGE_WIDTH * 0.8317757009345794, IMAGE_HEIGHT * 0.7710280373831776);
                POINTER.curveTo(IMAGE_WIDTH * 0.822429906542056, IMAGE_HEIGHT * 0.7710280373831776, IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8271028037383178, IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(0, POINTER.getBounds2D().getMaxY());
                POINTER_STOP = new Point2D.Double(0, POINTER.getBounds2D().getMinY());
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
                POINTER.moveTo(0.16822429906542055 * IMAGE_WIDTH, 0.8271028037383178 * IMAGE_HEIGHT);
                POINTER.lineTo(0.8317757009345794 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT);
                POINTER.curveTo(0.8317757009345794 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT, 0.9018691588785047 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT, 0.9018691588785047 * IMAGE_WIDTH, 0.8271028037383178 * IMAGE_HEIGHT);
                POINTER.curveTo(0.9018691588785047 * IMAGE_WIDTH, 0.8130841121495327 * IMAGE_HEIGHT, 0.8317757009345794 * IMAGE_WIDTH, 0.8130841121495327 * IMAGE_HEIGHT, 0.8317757009345794 * IMAGE_WIDTH, 0.8130841121495327 * IMAGE_HEIGHT);
                POINTER.lineTo(0.16822429906542055 * IMAGE_WIDTH, 0.8271028037383178 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.16822429906542055 * IMAGE_WIDTH, 0.822429906542056 * IMAGE_HEIGHT), new Point2D.Double(0.897196261682243 * IMAGE_WIDTH, 0.822429906542056 * IMAGE_HEIGHT), new float[]{0.0f, 1.0f}, new Color[]{getPointerColor().LIGHT, getPointerColor().MEDIUM}));
                G2.fill(POINTER);
                G2.setPaint(getPointerColor().DARK);
                G2.setStroke(new BasicStroke((0.004672897196261682f * IMAGE_WIDTH), 0, 1));
                G2.draw(POINTER);
                break;

            case TYPE12:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.16822429906542055 * IMAGE_WIDTH, 0.8271028037383178 * IMAGE_HEIGHT);
                POINTER.lineTo(0.8317757009345794 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT);
                POINTER.lineTo(0.8411214953271028 * IMAGE_WIDTH, 0.8271028037383178 * IMAGE_HEIGHT);
                POINTER.lineTo(0.8317757009345794 * IMAGE_WIDTH, 0.8130841121495327 * IMAGE_HEIGHT);
                POINTER.lineTo(0.16822429906542055 * IMAGE_WIDTH, 0.8271028037383178 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.16822429906542055 * IMAGE_WIDTH, 0.822429906542056 * IMAGE_HEIGHT), new Point2D.Double(0.8364485981308412 * IMAGE_WIDTH, 0.822429906542056 * IMAGE_HEIGHT), new float[]{0.0f, 1.0f}, new Color[]{getPointerColor().LIGHT, getPointerColor().MEDIUM}));
                G2.fill(POINTER);
                G2.setPaint(getPointerColor().DARK);
                G2.setStroke(new BasicStroke((0.004672897196261682f * IMAGE_WIDTH), 0, 1));
                G2.draw(POINTER);
                break;

            case TYPE13:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.20093457943925233 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT);
                POINTER.lineTo(0.16355140186915887 * IMAGE_WIDTH, 0.8271028037383178 * IMAGE_HEIGHT);
                POINTER.lineTo(0.20093457943925233 * IMAGE_WIDTH, 0.8130841121495327 * IMAGE_HEIGHT);
                POINTER.lineTo(0.8317757009345794 * IMAGE_WIDTH, 0.8130841121495327 * IMAGE_HEIGHT);
                POINTER.lineTo(0.8317757009345794 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT);
                POINTER.lineTo(0.20093457943925233 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.822429906542056 * IMAGE_WIDTH, 0.8271028037383178 * IMAGE_HEIGHT), new Point2D.Double(0.16822429906542055 * IMAGE_WIDTH, 0.8271028037383179 * IMAGE_HEIGHT), new float[]{0.0f, 0.899999f, 0.9f, 1.0f}, new Color[]{getPointerColor().MEDIUM, getPointerColor().MEDIUM, getBackgroundColor().LABEL_COLOR, getBackgroundColor().LABEL_COLOR}));
                G2.fill(POINTER);
                break;

            case TYPE14:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.20093457943925233 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT);
                POINTER.lineTo(0.16355140186915887 * IMAGE_WIDTH, 0.8271028037383178 * IMAGE_HEIGHT);
                POINTER.lineTo(0.20093457943925233 * IMAGE_WIDTH, 0.8130841121495327 * IMAGE_HEIGHT);
                POINTER.lineTo(0.8317757009345794 * IMAGE_WIDTH, 0.8130841121495327 * IMAGE_HEIGHT);
                POINTER.lineTo(0.8317757009345794 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT);
                POINTER.lineTo(0.20093457943925233 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.7476635514018691 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT), new Point2D.Double(0.7476635514018691 * IMAGE_WIDTH, 0.8084112149532711 * IMAGE_HEIGHT), new float[]{0.0f, 0.5f, 1.0f}, new Color[]{getPointerColor().VERY_DARK, getPointerColor().LIGHT, getPointerColor().VERY_DARK}));
                G2.fill(POINTER);
                break;

            case TYPE1:

            default:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.8130841121495327);
                POINTER.curveTo(IMAGE_WIDTH * 0.7850467289719626, IMAGE_HEIGHT * 0.8130841121495327, IMAGE_WIDTH * 0.7429906542056075, IMAGE_HEIGHT * 0.8130841121495327, IMAGE_WIDTH * 0.7289719626168224, IMAGE_HEIGHT * 0.8130841121495327);
                POINTER.curveTo(IMAGE_WIDTH * 0.7102803738317757, IMAGE_HEIGHT * 0.822429906542056, IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8271028037383178, IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.curveTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8271028037383178, IMAGE_WIDTH * 0.7149532710280374, IMAGE_HEIGHT * 0.8364485981308412, IMAGE_WIDTH * 0.7242990654205608, IMAGE_HEIGHT * 0.8411214953271028);
                POINTER.curveTo(IMAGE_WIDTH * 0.7429906542056075, IMAGE_HEIGHT * 0.8411214953271028, IMAGE_WIDTH * 0.7850467289719626, IMAGE_HEIGHT * 0.8411214953271028, IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.8457943925233645);
                POINTER.curveTo(IMAGE_WIDTH * 0.8084112149532711, IMAGE_HEIGHT * 0.8551401869158879, IMAGE_WIDTH * 0.8177570093457944, IMAGE_HEIGHT * 0.8598130841121495, IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8598130841121495);
                POINTER.curveTo(IMAGE_WIDTH * 0.8457943925233645, IMAGE_HEIGHT * 0.8598130841121495, IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.8457943925233645, IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.curveTo(IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.8084112149532711, IMAGE_WIDTH * 0.8457943925233645, IMAGE_HEIGHT * 0.794392523364486, IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.794392523364486);
                POINTER.curveTo(IMAGE_WIDTH * 0.8177570093457944, IMAGE_HEIGHT * 0.794392523364486, IMAGE_WIDTH * 0.8084112149532711, IMAGE_HEIGHT * 0.7990654205607477, IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.8130841121495327);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(POINTER.getBounds2D().getMinX(), 0);
                POINTER_STOP = new Point2D.Double(POINTER.getBounds2D().getMaxX(), 0);
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
                        getCustomPointerColorObject().DARK,
                        getCustomPointerColorObject().LIGHT,
                        getCustomPointerColorObject().LIGHT,
                        getCustomPointerColorObject().DARK
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
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        transformGraphics(IMAGE_WIDTH, IMAGE_HEIGHT, G2);

        final GeneralPath POINTER;

        switch (POINTER_TYPE) {
            case TYPE1:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.8130841121495327);
                POINTER.curveTo(IMAGE_WIDTH * 0.7850467289719626, IMAGE_HEIGHT * 0.8130841121495327, IMAGE_WIDTH * 0.7429906542056075, IMAGE_HEIGHT * 0.8130841121495327, IMAGE_WIDTH * 0.7289719626168224, IMAGE_HEIGHT * 0.8130841121495327);
                POINTER.curveTo(IMAGE_WIDTH * 0.7102803738317757, IMAGE_HEIGHT * 0.822429906542056, IMAGE_WIDTH * 0.2102803738317757, IMAGE_HEIGHT * 0.8271028037383178, IMAGE_WIDTH * 0.2102803738317757, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.curveTo(IMAGE_WIDTH * 0.2102803738317757, IMAGE_HEIGHT * 0.8271028037383178, IMAGE_WIDTH * 0.7149532710280374, IMAGE_HEIGHT * 0.8364485981308412, IMAGE_WIDTH * 0.7242990654205608, IMAGE_HEIGHT * 0.8411214953271028);
                POINTER.curveTo(IMAGE_WIDTH * 0.7429906542056075, IMAGE_HEIGHT * 0.8411214953271028, IMAGE_WIDTH * 0.7850467289719626, IMAGE_HEIGHT * 0.8411214953271028, IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.8457943925233645);
                POINTER.curveTo(IMAGE_WIDTH * 0.8084112149532711, IMAGE_HEIGHT * 0.8551401869158879, IMAGE_WIDTH * 0.8177570093457944, IMAGE_HEIGHT * 0.8598130841121495, IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8598130841121495);
                POINTER.curveTo(IMAGE_WIDTH * 0.8457943925233645, IMAGE_HEIGHT * 0.8598130841121495, IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.8457943925233645, IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.curveTo(IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.8084112149532711, IMAGE_WIDTH * 0.8457943925233645, IMAGE_HEIGHT * 0.794392523364486, IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.794392523364486);
                POINTER.curveTo(IMAGE_WIDTH * 0.8177570093457944, IMAGE_HEIGHT * 0.794392523364486, IMAGE_WIDTH * 0.8084112149532711, IMAGE_HEIGHT * 0.7990654205607477, IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.8130841121495327);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE2:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.8130841121495327);
                POINTER.lineTo(IMAGE_WIDTH * 0.7897196261682243, IMAGE_HEIGHT * 0.822429906542056);
                POINTER.lineTo(IMAGE_WIDTH * 0.6635514018691588, IMAGE_HEIGHT * 0.822429906542056);
                POINTER.lineTo(IMAGE_WIDTH * 0.2102803738317757, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.lineTo(IMAGE_WIDTH * 0.2102803738317757, IMAGE_HEIGHT * 0.8317757009345794);
                POINTER.lineTo(IMAGE_WIDTH * 0.6682242990654206, IMAGE_HEIGHT * 0.8364485981308412);
                POINTER.lineTo(IMAGE_WIDTH * 0.7897196261682243, IMAGE_HEIGHT * 0.8364485981308412);
                POINTER.lineTo(IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.8457943925233645);
                POINTER.curveTo(IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.8457943925233645, IMAGE_WIDTH * 0.8177570093457944, IMAGE_HEIGHT * 0.8598130841121495, IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8598130841121495);
                POINTER.curveTo(IMAGE_WIDTH * 0.8457943925233645, IMAGE_HEIGHT * 0.8598130841121495, IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.8457943925233645, IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.curveTo(IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.8084112149532711, IMAGE_WIDTH * 0.8457943925233645, IMAGE_HEIGHT * 0.794392523364486, IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.794392523364486);
                POINTER.curveTo(IMAGE_WIDTH * 0.8177570093457944, IMAGE_HEIGHT * 0.794392523364486, IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.8130841121495327, IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.8130841121495327);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE3:
                break;

            case TYPE4:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.lineTo(IMAGE_WIDTH * 0.17757009345794392, IMAGE_HEIGHT * 0.8084112149532711);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.794392523364486);
                POINTER.lineTo(IMAGE_WIDTH * 0.897196261682243, IMAGE_HEIGHT * 0.8037383177570093);
                POINTER.lineTo(IMAGE_WIDTH * 0.897196261682243, IMAGE_HEIGHT * 0.8504672897196262);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8598130841121495);
                POINTER.lineTo(IMAGE_WIDTH * 0.17757009345794392, IMAGE_HEIGHT * 0.8411214953271028);
                POINTER.lineTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE5:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.7990654205607477);
                POINTER.lineTo(IMAGE_WIDTH * 0.16822429906542055, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8551401869158879);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE6:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8457943925233645);
                POINTER.lineTo(IMAGE_WIDTH * 0.6495327102803738, IMAGE_HEIGHT * 0.8457943925233645);
                POINTER.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.8411214953271028);
                POINTER.lineTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8317757009345794);
                POINTER.lineTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.822429906542056);
                POINTER.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.8130841121495327);
                POINTER.lineTo(IMAGE_WIDTH * 0.6448598130841121, IMAGE_HEIGHT * 0.8084112149532711);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8084112149532711);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.822429906542056);
                POINTER.lineTo(IMAGE_WIDTH * 0.6495327102803738, IMAGE_HEIGHT * 0.822429906542056);
                POINTER.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.lineTo(IMAGE_WIDTH * 0.6495327102803738, IMAGE_HEIGHT * 0.8317757009345794);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8317757009345794);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8457943925233645);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE7:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8364485981308412);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8504672897196262);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8084112149532711);
                POINTER.lineTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.822429906542056);
                POINTER.lineTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8364485981308412);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE8:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.lineTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.794392523364486);
                POINTER.curveTo(IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.794392523364486, IMAGE_WIDTH * 0.7850467289719626, IMAGE_HEIGHT * 0.8177570093457944, IMAGE_WIDTH * 0.16822429906542055, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.curveTo(IMAGE_WIDTH * 0.7850467289719626, IMAGE_HEIGHT * 0.8411214953271028, IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8598130841121495, IMAGE_WIDTH * 0.8271028037383178, IMAGE_HEIGHT * 0.8598130841121495);
                POINTER.lineTo(IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE9:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.29439252336448596, IMAGE_HEIGHT * 0.8317757009345794);
                POINTER.lineTo(IMAGE_WIDTH * 0.29439252336448596, IMAGE_HEIGHT * 0.822429906542056);
                POINTER.lineTo(IMAGE_WIDTH * 0.7663551401869159, IMAGE_HEIGHT * 0.8130841121495327);
                POINTER.lineTo(IMAGE_WIDTH * 0.7663551401869159, IMAGE_HEIGHT * 0.8411214953271028);
                POINTER.lineTo(IMAGE_WIDTH * 0.29439252336448596, IMAGE_HEIGHT * 0.8317757009345794);
                POINTER.closePath();
                POINTER.moveTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8364485981308412);
                POINTER.lineTo(IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.8551401869158879);
                POINTER.lineTo(IMAGE_WIDTH * 0.8551401869158879, IMAGE_HEIGHT * 0.8551401869158879);
                POINTER.curveTo(IMAGE_WIDTH * 0.8551401869158879, IMAGE_HEIGHT * 0.8551401869158879, IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.8504672897196262, IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.8504672897196262);
                POINTER.curveTo(IMAGE_WIDTH * 0.8925233644859814, IMAGE_HEIGHT * 0.8504672897196262, IMAGE_WIDTH * 0.8925233644859814, IMAGE_HEIGHT * 0.8457943925233645, IMAGE_WIDTH * 0.8925233644859814, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.curveTo(IMAGE_WIDTH * 0.8925233644859814, IMAGE_HEIGHT * 0.8084112149532711, IMAGE_WIDTH * 0.8925233644859814, IMAGE_HEIGHT * 0.8037383177570093, IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.8037383177570093);
                POINTER.curveTo(IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.8037383177570093, IMAGE_WIDTH * 0.8551401869158879, IMAGE_HEIGHT * 0.7990654205607477, IMAGE_WIDTH * 0.8551401869158879, IMAGE_HEIGHT * 0.7990654205607477);
                POINTER.lineTo(IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.7990654205607477);
                POINTER.lineTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8177570093457944);
                POINTER.lineTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8364485981308412);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE10:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.curveTo(IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8271028037383178, IMAGE_WIDTH * 0.822429906542056, IMAGE_HEIGHT * 0.883177570093458, IMAGE_WIDTH * 0.8317757009345794, IMAGE_HEIGHT * 0.883177570093458);
                POINTER.curveTo(IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.883177570093458, IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.8598130841121495, IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.curveTo(IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.7990654205607477, IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.7710280373831776, IMAGE_WIDTH * 0.8317757009345794, IMAGE_HEIGHT * 0.7710280373831776);
                POINTER.curveTo(IMAGE_WIDTH * 0.822429906542056, IMAGE_HEIGHT * 0.7710280373831776, IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8271028037383178, IMAGE_WIDTH * 0.16355140186915887, IMAGE_HEIGHT * 0.8271028037383178);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE11:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.16822429906542055 * IMAGE_WIDTH, 0.8271028037383178 * IMAGE_HEIGHT);
                POINTER.lineTo(0.8317757009345794 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT);
                POINTER.curveTo(0.8317757009345794 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT, 0.9018691588785047 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT, 0.9018691588785047 * IMAGE_WIDTH, 0.8271028037383178 * IMAGE_HEIGHT);
                POINTER.curveTo(0.9018691588785047 * IMAGE_WIDTH, 0.8130841121495327 * IMAGE_HEIGHT, 0.8317757009345794 * IMAGE_WIDTH, 0.8130841121495327 * IMAGE_HEIGHT, 0.8317757009345794 * IMAGE_WIDTH, 0.8130841121495327 * IMAGE_HEIGHT);
                POINTER.lineTo(0.16822429906542055 * IMAGE_WIDTH, 0.8271028037383178 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE12:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.16822429906542055 * IMAGE_WIDTH, 0.8271028037383178 * IMAGE_HEIGHT);
                POINTER.lineTo(0.8317757009345794 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT);
                POINTER.lineTo(0.8411214953271028 * IMAGE_WIDTH, 0.8271028037383178 * IMAGE_HEIGHT);
                POINTER.lineTo(0.8317757009345794 * IMAGE_WIDTH, 0.8130841121495327 * IMAGE_HEIGHT);
                POINTER.lineTo(0.16822429906542055 * IMAGE_WIDTH, 0.8271028037383178 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE13:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.20093457943925233 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT);
                POINTER.lineTo(0.16355140186915887 * IMAGE_WIDTH, 0.8271028037383178 * IMAGE_HEIGHT);
                POINTER.lineTo(0.20093457943925233 * IMAGE_WIDTH, 0.8130841121495327 * IMAGE_HEIGHT);
                POINTER.lineTo(0.8317757009345794 * IMAGE_WIDTH, 0.8130841121495327 * IMAGE_HEIGHT);
                POINTER.lineTo(0.8317757009345794 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT);
                POINTER.lineTo(0.20093457943925233 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE14:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.20093457943925233 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT);
                POINTER.lineTo(0.16355140186915887 * IMAGE_WIDTH, 0.8271028037383178 * IMAGE_HEIGHT);
                POINTER.lineTo(0.20093457943925233 * IMAGE_WIDTH, 0.8130841121495327 * IMAGE_HEIGHT);
                POINTER.lineTo(0.8317757009345794 * IMAGE_WIDTH, 0.8130841121495327 * IMAGE_HEIGHT);
                POINTER.lineTo(0.8317757009345794 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT);
                POINTER.lineTo(0.20093457943925233 * IMAGE_WIDTH, 0.8411214953271028 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;
        }

        G2.dispose();

        return IMAGE;
    }

    private BufferedImage create_POSTS_Image(final int WIDTH, BufferedImage image) {
        if (WIDTH <= 0) {
            return null;
        }

        if (image == null) {
            image = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        }

        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        transformGraphics(IMAGE_WIDTH, IMAGE_HEIGHT, G2);

        // Min post
        if (getPostsVisible()) {
            if (getOrientation() == Orientation.NORTH_EAST || getOrientation() == Orientation.NORTH_WEST) {
                final Ellipse2D MIN_POST_FRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.15887850522994995, IMAGE_HEIGHT * 0.836448609828949, IMAGE_WIDTH * 0.03738318383693695, IMAGE_HEIGHT * 0.03738313913345337);
                final Point2D MIN_POST_FRAME_START = new Point2D.Double(0, MIN_POST_FRAME.getBounds2D().getMinY());
                final Point2D MIN_POST_FRAME_STOP = new Point2D.Double(0, MIN_POST_FRAME.getBounds2D().getMaxY());
                final float[] MIN_POST_FRAME_FRACTIONS = {
                    0.0f,
                    0.46f,
                    1.0f
                };
                final Color[] MIN_POST_FRAME_COLORS = {
                    new Color(180, 180, 180, 255),
                    new Color(63, 63, 63, 255),
                    new Color(40, 40, 40, 255)
                };
                final LinearGradientPaint MIN_POST_FRAME_GRADIENT = new LinearGradientPaint(MIN_POST_FRAME_START, MIN_POST_FRAME_STOP, MIN_POST_FRAME_FRACTIONS, MIN_POST_FRAME_COLORS);
                G2.setPaint(MIN_POST_FRAME_GRADIENT);
                G2.fill(MIN_POST_FRAME);

                final Ellipse2D MIN_POST_MAIN = new Ellipse2D.Double(IMAGE_WIDTH * 0.16355140507221222, IMAGE_HEIGHT * 0.84112149477005, IMAGE_WIDTH * 0.028037384152412415, IMAGE_HEIGHT * 0.02803736925125122);
                final Point2D MIN_POST_MAIN_START = new Point2D.Double(0, MIN_POST_MAIN.getBounds2D().getMinY());
                final Point2D MIN_POST_MAIN_STOP = new Point2D.Double(0, MIN_POST_MAIN.getBounds2D().getMaxY());
                final float[] MIN_POST_MAIN_FRACTIONS = {
                    0.0f,
                    0.5f,
                    1.0f
                };

                final Color[] MIN_POST_MAIN_COLORS;
                switch (getModel().getKnobStyle()) {
                    case BLACK:
                        MIN_POST_MAIN_COLORS = new Color[]{
                            new Color(0xBFBFBF),
                            new Color(0x2B2A2F),
                            new Color(0x7D7E80)
                        };
                        break;

                    case BRASS:
                        MIN_POST_MAIN_COLORS = new Color[]{
                            new Color(0xDFD0AE),
                            new Color(0x7A5E3E),
                            new Color(0xCFBE9D)
                        };
                        break;

                    case SILVER:

                    default:
                        MIN_POST_MAIN_COLORS = new Color[]{
                            new Color(0xD7D7D7),
                            new Color(0x747474),
                            new Color(0xD7D7D7)
                        };
                        break;
                }
                final LinearGradientPaint MIN_POST_MAIN_GRADIENT = new LinearGradientPaint(MIN_POST_MAIN_START, MIN_POST_MAIN_STOP, MIN_POST_MAIN_FRACTIONS, MIN_POST_MAIN_COLORS);
                G2.setPaint(MIN_POST_MAIN_GRADIENT);
                G2.fill(MIN_POST_MAIN);
            }

            if (getOrientation() == Orientation.SOUTH_EAST || getOrientation() == Orientation.SOUTH_WEST) {
                final Ellipse2D MIN_POST1_FRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.8317757248878479, IMAGE_HEIGHT * 0.1682243049144745, IMAGE_WIDTH * 0.03738313913345337, IMAGE_HEIGHT * 0.03738316893577576);
                final Point2D MIN_POST1_FRAME_START = new Point2D.Double(MIN_POST1_FRAME.getBounds2D().getMaxX(), 0);
                final Point2D MIN_POST1_FRAME_STOP = new Point2D.Double(MIN_POST1_FRAME.getBounds2D().getMinX(), 0);
                final float[] MIN_POST1_FRAME_FRACTIONS = {
                    0.0f,
                    0.46f,
                    1.0f
                };
                final Color[] MIN_POST1_FRAME_COLORS = {
                    new Color(180, 180, 180, 255),
                    new Color(63, 63, 63, 255),
                    new Color(40, 40, 40, 255)
                };
                final LinearGradientPaint MIN_POST1_FRAME_GRADIENT = new LinearGradientPaint(MIN_POST1_FRAME_START, MIN_POST1_FRAME_STOP, MIN_POST1_FRAME_FRACTIONS, MIN_POST1_FRAME_COLORS);
                G2.setPaint(MIN_POST1_FRAME_GRADIENT);
                G2.fill(MIN_POST1_FRAME);

                final Ellipse2D MIN_POST1_MAIN = new Ellipse2D.Double(IMAGE_WIDTH * 0.836448609828949, IMAGE_HEIGHT * 0.17289718985557556, IMAGE_WIDTH * 0.02803736925125122, IMAGE_HEIGHT * 0.028037384152412415);
                final Point2D MIN_POST1_MAIN_START = new Point2D.Double(MIN_POST1_MAIN.getBounds2D().getMaxX(), 0);
                final Point2D MIN_POST1_MAIN_STOP = new Point2D.Double(MIN_POST1_MAIN.getBounds2D().getMinX(), 0);
                final float[] MIN_POST1_MAIN_FRACTIONS = {
                    0.0f,
                    0.5f,
                    1.0f
                };

                final Color[] MIN_POST1_MAIN_COLORS;
                switch (getModel().getKnobStyle()) {
                    case BLACK:
                        MIN_POST1_MAIN_COLORS = new Color[]{
                            new Color(0xBFBFBF),
                            new Color(0x2B2A2F),
                            new Color(0x7D7E80)
                        };
                        break;

                    case BRASS:
                        MIN_POST1_MAIN_COLORS = new Color[]{
                            new Color(0xDFD0AE),
                            new Color(0x7A5E3E),
                            new Color(0xCFBE9D)
                        };
                        break;

                    case SILVER:

                    default:
                        MIN_POST1_MAIN_COLORS = new Color[]{
                            new Color(0xD7D7D7),
                            new Color(0x747474),
                            new Color(0xD7D7D7)
                        };
                        break;
                }
                final LinearGradientPaint MIN_POST1_MAIN_GRADIENT = new LinearGradientPaint(MIN_POST1_MAIN_START, MIN_POST1_MAIN_STOP, MIN_POST1_MAIN_FRACTIONS, MIN_POST1_MAIN_COLORS);
                G2.setPaint(MIN_POST1_MAIN_GRADIENT);
                G2.fill(MIN_POST1_MAIN);
            }
        }

        // Center post
        switch (getKnobType()) {
            case SMALL_STD_KNOB:
                final Ellipse2D CENTER_KNOB_FRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.7850467562675476, IMAGE_HEIGHT * 0.7850467562675476, IMAGE_WIDTH * 0.08411210775375366, IMAGE_HEIGHT * 0.08411210775375366);
                final Point2D CENTER_KNOB_FRAME_START = new Point2D.Double(0, CENTER_KNOB_FRAME.getBounds2D().getMinY());
                final Point2D CENTER_KNOB_FRAME_STOP = new Point2D.Double(0, CENTER_KNOB_FRAME.getBounds2D().getMaxY());
                final float[] CENTER_KNOB_FRAME_FRACTIONS = {
                    0.0f,
                    0.46f,
                    1.0f
                };
                final Color[] CENTER_KNOB_FRAME_COLORS = {
                    new Color(180, 180, 180, 255),
                    new Color(63, 63, 63, 255),
                    new Color(40, 40, 40, 255)
                };
                final LinearGradientPaint CENTER_KNOB_FRAME_GRADIENT = new LinearGradientPaint(CENTER_KNOB_FRAME_START, CENTER_KNOB_FRAME_STOP, CENTER_KNOB_FRAME_FRACTIONS, CENTER_KNOB_FRAME_COLORS);
                G2.setPaint(CENTER_KNOB_FRAME_GRADIENT);
                G2.fill(CENTER_KNOB_FRAME);

                final Ellipse2D CENTER_KNOB_MAIN = new Ellipse2D.Double(IMAGE_WIDTH * 0.7943925261497498, IMAGE_HEIGHT * 0.7943925261497498, IMAGE_WIDTH * 0.06542056798934937, IMAGE_HEIGHT * 0.06542056798934937);
                final Point2D CENTER_KNOB_MAIN_START = new Point2D.Double(0, CENTER_KNOB_MAIN.getBounds2D().getMinY());
                final Point2D CENTER_KNOB_MAIN_STOP = new Point2D.Double(0, CENTER_KNOB_MAIN.getBounds2D().getMaxY());
                final float[] CENTER_KNOB_MAIN_FRACTIONS = {
                    0.0f,
                    0.5f,
                    1.0f
                };

                final Color[] CENTER_KNOB_MAIN_COLORS;
                switch (getModel().getKnobStyle()) {
                    case BLACK:
                        CENTER_KNOB_MAIN_COLORS = new Color[]{
                            new Color(0xBFBFBF),
                            new Color(0x2B2A2F),
                            new Color(0x7D7E80)
                        };
                        break;

                    case BRASS:
                        CENTER_KNOB_MAIN_COLORS = new Color[]{
                            new Color(0xDFD0AE),
                            new Color(0x7A5E3E),
                            new Color(0xCFBE9D)
                        };
                        break;

                    case SILVER:

                    default:
                        CENTER_KNOB_MAIN_COLORS = new Color[]{
                            new Color(0xD7D7D7),
                            new Color(0x747474),
                            new Color(0xD7D7D7)
                        };
                        break;
                }
                final LinearGradientPaint CENTER_KNOB_MAIN_GRADIENT = new LinearGradientPaint(CENTER_KNOB_MAIN_START, CENTER_KNOB_MAIN_STOP, CENTER_KNOB_MAIN_FRACTIONS, CENTER_KNOB_MAIN_COLORS);
                G2.setPaint(CENTER_KNOB_MAIN_GRADIENT);
                G2.fill(CENTER_KNOB_MAIN);

                final Ellipse2D CENTER_KNOB_INNERSHADOW = new Ellipse2D.Double(IMAGE_WIDTH * 0.7943925261497498, IMAGE_HEIGHT * 0.7943925261497498, IMAGE_WIDTH * 0.06542056798934937, IMAGE_HEIGHT * 0.06542056798934937);
                final Point2D CENTER_KNOB_INNERSHADOW_CENTER = new Point2D.Double((0.822429906542056 * IMAGE_WIDTH), (0.8177570093457944 * IMAGE_HEIGHT));
                final float[] CENTER_KNOB_INNERSHADOW_FRACTIONS = {
                    0.0f,
                    0.75f,
                    0.76f,
                    1.0f
                };
                final Color[] CENTER_KNOB_INNERSHADOW_COLORS = {
                    new Color(0, 0, 0, 0),
                    new Color(0, 0, 0, 0),
                    new Color(0, 0, 0, 1),
                    new Color(0, 0, 0, 51)
                };
                final RadialGradientPaint CENTER_KNOB_INNERSHADOW_GRADIENT = new RadialGradientPaint(CENTER_KNOB_INNERSHADOW_CENTER, (float) (0.03271028037383177 * IMAGE_WIDTH), CENTER_KNOB_INNERSHADOW_FRACTIONS, CENTER_KNOB_INNERSHADOW_COLORS);
                G2.setPaint(CENTER_KNOB_INNERSHADOW_GRADIENT);
                G2.fill(CENTER_KNOB_INNERSHADOW);
                break;

            case BIG_STD_KNOB:
                final Ellipse2D BIGCENTER_BACKGROUNDFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.7663551568984985, IMAGE_HEIGHT * 0.7663551568984985, IMAGE_WIDTH * 0.1214953064918518, IMAGE_HEIGHT * 0.1214953064918518);
                final Point2D BIGCENTER_BACKGROUNDFRAME_START = new Point2D.Double(0, BIGCENTER_BACKGROUNDFRAME.getBounds2D().getMinY());
                final Point2D BIGCENTER_BACKGROUNDFRAME_STOP = new Point2D.Double(0, BIGCENTER_BACKGROUNDFRAME.getBounds2D().getMaxY());
                final float[] BIGCENTER_BACKGROUNDFRAME_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] BIGCENTER_BACKGROUNDFRAME_COLORS;
                switch (getModel().getKnobStyle()) {
                    case BLACK:
                        BIGCENTER_BACKGROUNDFRAME_COLORS = new Color[]{
                            new Color(129, 133, 136, 255),
                            new Color(61, 61, 73, 255)
                        };
                        break;

                    case BRASS:
                        BIGCENTER_BACKGROUNDFRAME_COLORS = new Color[]{
                            new Color(143, 117, 80, 255),
                            new Color(100, 76, 49, 255)
                        };
                        break;

                    case SILVER:

                    default:
                        BIGCENTER_BACKGROUNDFRAME_COLORS = new Color[]{
                            new Color(152, 152, 152, 255),
                            new Color(118, 121, 126, 255)
                        };
                        break;
                }
                final LinearGradientPaint BIGCENTER_BACKGROUNDFRAME_GRADIENT = new LinearGradientPaint(BIGCENTER_BACKGROUNDFRAME_START, BIGCENTER_BACKGROUNDFRAME_STOP, BIGCENTER_BACKGROUNDFRAME_FRACTIONS, BIGCENTER_BACKGROUNDFRAME_COLORS);
                G2.setPaint(BIGCENTER_BACKGROUNDFRAME_GRADIENT);
                G2.fill(BIGCENTER_BACKGROUNDFRAME);

                final Ellipse2D BIGCENTER_BACKGROUND = new Ellipse2D.Double(IMAGE_WIDTH * 0.7710280418395996, IMAGE_HEIGHT * 0.7710280418395996, IMAGE_WIDTH * 0.11214953660964966, IMAGE_HEIGHT * 0.11214953660964966);
                final Point2D BIGCENTER_BACKGROUND_START = new Point2D.Double(0, BIGCENTER_BACKGROUND.getBounds2D().getMinY());
                final Point2D BIGCENTER_BACKGROUND_STOP = new Point2D.Double(0, BIGCENTER_BACKGROUND.getBounds2D().getMaxY());
                final float[] BIGCENTER_BACKGROUND_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] BIGCENTER_BACKGROUND_COLORS;
                switch (getModel().getKnobStyle()) {
                    case BLACK:
                        BIGCENTER_BACKGROUND_COLORS = new Color[]{
                            new Color(26, 27, 32, 255),
                            new Color(96, 97, 102, 255)
                        };
                        break;

                    case BRASS:
                        BIGCENTER_BACKGROUND_COLORS = new Color[]{
                            new Color(98, 75, 49, 255),
                            new Color(149, 109, 54, 255)
                        };
                        break;

                    case SILVER:

                    default:
                        BIGCENTER_BACKGROUND_COLORS = new Color[]{
                            new Color(118, 121, 126, 255),
                            new Color(191, 191, 191, 255)
                        };
                        break;
                }
                final LinearGradientPaint BIGCENTER_BACKGROUND_GRADIENT = new LinearGradientPaint(BIGCENTER_BACKGROUND_START, BIGCENTER_BACKGROUND_STOP, BIGCENTER_BACKGROUND_FRACTIONS, BIGCENTER_BACKGROUND_COLORS);
                G2.setPaint(BIGCENTER_BACKGROUND_GRADIENT);
                G2.fill(BIGCENTER_BACKGROUND);

                final Ellipse2D BIGCENTER_FOREGROUNDFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.7803738117218018, IMAGE_HEIGHT * 0.7803738117218018, IMAGE_WIDTH * 0.09345793724060059, IMAGE_HEIGHT * 0.09345793724060059);
                final Point2D BIGCENTER_FOREGROUNDFRAME_START = new Point2D.Double(0, BIGCENTER_FOREGROUNDFRAME.getBounds2D().getMinY());
                final Point2D BIGCENTER_FOREGROUNDFRAME_STOP = new Point2D.Double(0, BIGCENTER_FOREGROUNDFRAME.getBounds2D().getMaxY());
                final float[] BIGCENTER_FOREGROUNDFRAME_FRACTIONS = {
                    0.0f,
                    0.47f,
                    1.0f
                };
                final Color[] BIGCENTER_FOREGROUNDFRAME_COLORS;
                switch (getModel().getKnobStyle()) {
                    case BLACK:
                        BIGCENTER_FOREGROUNDFRAME_COLORS = new Color[]{
                            new Color(191, 191, 191, 255),
                            new Color(56, 57, 61, 255),
                            new Color(143, 144, 146, 255)
                        };
                        break;

                    case BRASS:
                        BIGCENTER_FOREGROUNDFRAME_COLORS = new Color[]{
                            new Color(147, 108, 54, 255),
                            new Color(82, 66, 50, 255),
                            new Color(147, 108, 54, 255)
                        };
                        break;

                    case SILVER:

                    default:
                        BIGCENTER_FOREGROUNDFRAME_COLORS = new Color[]{
                            new Color(191, 191, 191, 255),
                            new Color(116, 116, 116, 255),
                            new Color(143, 144, 146, 255)
                        };
                        break;
                }
                final LinearGradientPaint BIGCENTER_FOREGROUNDFRAME_GRADIENT = new LinearGradientPaint(BIGCENTER_FOREGROUNDFRAME_START, BIGCENTER_FOREGROUNDFRAME_STOP, BIGCENTER_FOREGROUNDFRAME_FRACTIONS, BIGCENTER_FOREGROUNDFRAME_COLORS);
                G2.setPaint(BIGCENTER_FOREGROUNDFRAME_GRADIENT);
                G2.fill(BIGCENTER_FOREGROUNDFRAME);

                final Ellipse2D BIGCENTER_FOREGROUND = new Ellipse2D.Double(IMAGE_WIDTH * 0.7850467562675476, IMAGE_HEIGHT * 0.7850467562675476, IMAGE_WIDTH * 0.08411210775375366, IMAGE_HEIGHT * 0.08411210775375366);
                final Point2D BIGCENTER_FOREGROUND_START = new Point2D.Double(0, BIGCENTER_FOREGROUND.getBounds2D().getMinY());
                final Point2D BIGCENTER_FOREGROUND_STOP = new Point2D.Double(0, BIGCENTER_FOREGROUND.getBounds2D().getMaxY());
                final float[] BIGCENTER_FOREGROUND_FRACTIONS = {
                    0.0f,
                    0.21f,
                    0.5f,
                    0.78f,
                    1.0f
                };
                final Color[] BIGCENTER_FOREGROUND_COLORS;
                switch (getModel().getKnobStyle()) {
                    case BLACK:
                        BIGCENTER_FOREGROUND_COLORS = new Color[]{
                            new Color(191, 191, 191, 255),
                            new Color(94, 93, 99, 255),
                            new Color(43, 42, 47, 255),
                            new Color(78, 79, 81, 255),
                            new Color(143, 144, 146, 255)
                        };
                        break;

                    case BRASS:
                        BIGCENTER_FOREGROUND_COLORS = new Color[]{
                            new Color(223, 208, 174, 255),
                            new Color(159, 136, 104, 255),
                            new Color(122, 94, 62, 255),
                            new Color(159, 136, 104, 255),
                            new Color(223, 208, 174, 255)
                        };
                        break;

                    case SILVER:

                    default:
                        BIGCENTER_FOREGROUND_COLORS = new Color[]{
                            new Color(215, 215, 215, 255),
                            new Color(139, 142, 145, 255),
                            new Color(100, 100, 100, 255),
                            new Color(139, 142, 145, 255),
                            new Color(215, 215, 215, 255)
                        };
                        break;
                }
                final LinearGradientPaint BIGCENTER_FOREGROUND_GRADIENT = new LinearGradientPaint(BIGCENTER_FOREGROUND_START, BIGCENTER_FOREGROUND_STOP, BIGCENTER_FOREGROUND_FRACTIONS, BIGCENTER_FOREGROUND_COLORS);
                G2.setPaint(BIGCENTER_FOREGROUND_GRADIENT);
                G2.fill(BIGCENTER_FOREGROUND);
                break;

            case BIG_CHROME_KNOB:
                final Ellipse2D CHROMEKNOB_BACKFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.7570093274116516, IMAGE_HEIGHT * 0.7570093274116516, IMAGE_WIDTH * 0.14018690586090088, IMAGE_HEIGHT * 0.14018690586090088);
                final Point2D CHROMEKNOB_BACKFRAME_START = new Point2D.Double((0.7897196261682243 * IMAGE_WIDTH), (0.7663551401869159 * IMAGE_HEIGHT));
                final Point2D CHROMEKNOB_BACKFRAME_STOP = new Point2D.Double(((0.7897196261682243 + 0.0718114890783315) * IMAGE_WIDTH), ((0.7663551401869159 + 0.1149224055539082) * IMAGE_HEIGHT));
                final float[] CHROMEKNOB_BACKFRAME_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] CHROMEKNOB_BACKFRAME_COLORS = {
                    new Color(129, 139, 140, 255),
                    new Color(166, 171, 175, 255)
                };
                final LinearGradientPaint CHROMEKNOB_BACKFRAME_GRADIENT = new LinearGradientPaint(CHROMEKNOB_BACKFRAME_START, CHROMEKNOB_BACKFRAME_STOP, CHROMEKNOB_BACKFRAME_FRACTIONS, CHROMEKNOB_BACKFRAME_COLORS);
                G2.setPaint(CHROMEKNOB_BACKFRAME_GRADIENT);
                G2.fill(CHROMEKNOB_BACKFRAME);

                final Ellipse2D CHROMEKNOB_BACK = new Ellipse2D.Double(IMAGE_WIDTH * 0.7616822719573975, IMAGE_HEIGHT * 0.7616822719573975, IMAGE_WIDTH * 0.13084107637405396, IMAGE_HEIGHT * 0.13084107637405396);
                final Point2D CHROMEKNOB_BACK_CENTER = new Point2D.Double(CHROMEKNOB_BACK.getCenterX(), CHROMEKNOB_BACK.getCenterY());
                final float[] CHROMEKNOB_BACK_FRACTIONS = {
                    0.0f,
                    0.09f,
                    0.12f,
                    0.16f,
                    0.25f,
                    0.29f,
                    0.33f,
                    0.38f,
                    0.48f,
                    0.52f,
                    0.65f,
                    0.69f,
                    0.8f,
                    0.83f,
                    0.87f,
                    0.97f,
                    1.0f
                };
                final Color[] CHROMEKNOB_BACK_COLORS = {
                    new Color(255, 255, 255, 255),
                    new Color(255, 255, 255, 255),
                    new Color(136, 136, 138, 255),
                    new Color(164, 185, 190, 255),
                    new Color(158, 179, 182, 255),
                    new Color(112, 112, 112, 255),
                    new Color(221, 227, 227, 255),
                    new Color(155, 176, 179, 255),
                    new Color(156, 176, 177, 255),
                    new Color(254, 255, 255, 255),
                    new Color(255, 255, 255, 255),
                    new Color(156, 180, 180, 255),
                    new Color(198, 209, 211, 255),
                    new Color(246, 248, 247, 255),
                    new Color(204, 216, 216, 255),
                    new Color(164, 188, 190, 255),
                    new Color(255, 255, 255, 255)
                };
                final ConicalGradientPaint CHROMEKNOB_BACK_GRADIENT = new ConicalGradientPaint(false, CHROMEKNOB_BACK_CENTER, 0, CHROMEKNOB_BACK_FRACTIONS, CHROMEKNOB_BACK_COLORS);
                G2.setPaint(CHROMEKNOB_BACK_GRADIENT);
                G2.fill(CHROMEKNOB_BACK);

                final Ellipse2D CHROMEKNOB_FOREFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.7943925261497498, IMAGE_HEIGHT * 0.7943925261497498, IMAGE_WIDTH * 0.06542056798934937, IMAGE_HEIGHT * 0.06542056798934937);
                final Point2D CHROMEKNOB_FOREFRAME_START = new Point2D.Double((0.8084112149532711 * IMAGE_WIDTH), (0.7990654205607477 * IMAGE_HEIGHT));
                final Point2D CHROMEKNOB_FOREFRAME_STOP = new Point2D.Double(((0.8084112149532711 + 0.033969662360372466) * IMAGE_WIDTH), ((0.7990654205607477 + 0.05036209552904459) * IMAGE_HEIGHT));
                final float[] CHROMEKNOB_FOREFRAME_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] CHROMEKNOB_FOREFRAME_COLORS = {
                    new Color(225, 235, 232, 255),
                    new Color(196, 207, 207, 255)
                };
                final LinearGradientPaint CHROMEKNOB_FOREFRAME_GRADIENT = new LinearGradientPaint(CHROMEKNOB_FOREFRAME_START, CHROMEKNOB_FOREFRAME_STOP, CHROMEKNOB_FOREFRAME_FRACTIONS, CHROMEKNOB_FOREFRAME_COLORS);
                G2.setPaint(CHROMEKNOB_FOREFRAME_GRADIENT);
                G2.fill(CHROMEKNOB_FOREFRAME);

                final Ellipse2D CHROMEKNOB_FORE = new Ellipse2D.Double(IMAGE_WIDTH * 0.7990654110908508, IMAGE_HEIGHT * 0.7990654110908508, IMAGE_WIDTH * 0.05607479810714722, IMAGE_HEIGHT * 0.05607479810714722);
                final Point2D CHROMEKNOB_FORE_START = new Point2D.Double((0.8084112149532711 * IMAGE_WIDTH), (0.8037383177570093 * IMAGE_HEIGHT));
                final Point2D CHROMEKNOB_FORE_STOP = new Point2D.Double(((0.8084112149532711 + 0.03135661140957459) * IMAGE_WIDTH), ((0.8037383177570093 + 0.04648808818065655) * IMAGE_HEIGHT));
                final float[] CHROMEKNOB_FORE_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] CHROMEKNOB_FORE_COLORS = {
                    new Color(237, 239, 237, 255),
                    new Color(148, 161, 161, 255)
                };
                final LinearGradientPaint CHROMEKNOB_FORE_GRADIENT = new LinearGradientPaint(CHROMEKNOB_FORE_START, CHROMEKNOB_FORE_STOP, CHROMEKNOB_FORE_FRACTIONS, CHROMEKNOB_FORE_COLORS);
                G2.setPaint(CHROMEKNOB_FORE_GRADIENT);
                G2.fill(CHROMEKNOB_FORE);
                break;

            case METAL_KNOB:
                final Ellipse2D METALKNOB_FRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.7897196412086487, IMAGE_HEIGHT * 0.7850467562675476, IMAGE_WIDTH * 0.08411210775375366, IMAGE_HEIGHT * 0.08411210775375366);
                final Point2D METALKNOB_FRAME_START = new Point2D.Double(0, METALKNOB_FRAME.getBounds2D().getMinY());
                final Point2D METALKNOB_FRAME_STOP = new Point2D.Double(0, METALKNOB_FRAME.getBounds2D().getMaxY());
                final float[] METALKNOB_FRAME_FRACTIONS = {
                    0.0f,
                    0.47f,
                    1.0f
                };
                final Color[] METALKNOB_FRAME_COLORS = {
                    new Color(92, 95, 101, 255),
                    new Color(46, 49, 53, 255),
                    new Color(22, 23, 26, 255)
                };
                final LinearGradientPaint METALKNOB_FRAME_GRADIENT = new LinearGradientPaint(METALKNOB_FRAME_START, METALKNOB_FRAME_STOP, METALKNOB_FRAME_FRACTIONS, METALKNOB_FRAME_COLORS);
                G2.setPaint(METALKNOB_FRAME_GRADIENT);
                G2.fill(METALKNOB_FRAME);

                final Ellipse2D METALKNOB_MAIN = new Ellipse2D.Double(IMAGE_WIDTH * 0.7943925261497498, IMAGE_HEIGHT * 0.7897196412086487, IMAGE_WIDTH * 0.07476633787155151, IMAGE_HEIGHT * 0.07476633787155151);
                final Point2D METALKNOB_MAIN_START = new Point2D.Double(0, METALKNOB_MAIN.getBounds2D().getMinY());
                final Point2D METALKNOB_MAIN_STOP = new Point2D.Double(0, METALKNOB_MAIN.getBounds2D().getMaxY());
                final float[] METALKNOB_MAIN_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] METALKNOB_MAIN_COLORS;
                switch (getModel().getKnobStyle()) {
                    case BLACK:
                        METALKNOB_MAIN_COLORS = new Color[]{
                            new Color(0x2B2A2F),
                            new Color(0x1A1B20)
                        };
                        break;

                    case BRASS:
                        METALKNOB_MAIN_COLORS = new Color[]{
                            new Color(0x966E36),
                            new Color(0x7C5F3D)
                        };
                        break;

                    case SILVER:

                    default:
                        METALKNOB_MAIN_COLORS = new Color[]{
                            new Color(204, 204, 204, 255),
                            new Color(87, 92, 98, 255)
                        };
                        break;
                }
                final LinearGradientPaint METALKNOB_MAIN_GRADIENT = new LinearGradientPaint(METALKNOB_MAIN_START, METALKNOB_MAIN_STOP, METALKNOB_MAIN_FRACTIONS, METALKNOB_MAIN_COLORS);
                G2.setPaint(METALKNOB_MAIN_GRADIENT);
                G2.fill(METALKNOB_MAIN);

                final GeneralPath METALKNOB_LOWERHL = new GeneralPath();
                METALKNOB_LOWERHL.setWindingRule(Path2D.WIND_EVEN_ODD);
                METALKNOB_LOWERHL.moveTo(IMAGE_WIDTH * 0.8504672897196262, IMAGE_HEIGHT * 0.8551401869158879);
                METALKNOB_LOWERHL.curveTo(IMAGE_WIDTH * 0.8504672897196262, IMAGE_HEIGHT * 0.8457943925233645, IMAGE_WIDTH * 0.8411214953271028, IMAGE_HEIGHT * 0.8411214953271028, IMAGE_WIDTH * 0.8317757009345794, IMAGE_HEIGHT * 0.8411214953271028);
                METALKNOB_LOWERHL.curveTo(IMAGE_WIDTH * 0.8177570093457944, IMAGE_HEIGHT * 0.8411214953271028, IMAGE_WIDTH * 0.8084112149532711, IMAGE_HEIGHT * 0.8457943925233645, IMAGE_WIDTH * 0.8084112149532711, IMAGE_HEIGHT * 0.8551401869158879);
                METALKNOB_LOWERHL.curveTo(IMAGE_WIDTH * 0.8130841121495327, IMAGE_HEIGHT * 0.8598130841121495, IMAGE_WIDTH * 0.822429906542056, IMAGE_HEIGHT * 0.8644859813084113, IMAGE_WIDTH * 0.8317757009345794, IMAGE_HEIGHT * 0.8644859813084113);
                METALKNOB_LOWERHL.curveTo(IMAGE_WIDTH * 0.8364485981308412, IMAGE_HEIGHT * 0.8644859813084113, IMAGE_WIDTH * 0.8457943925233645, IMAGE_HEIGHT * 0.8598130841121495, IMAGE_WIDTH * 0.8504672897196262, IMAGE_HEIGHT * 0.8551401869158879);
                METALKNOB_LOWERHL.closePath();
                final Point2D METALKNOB_LOWERHL_CENTER = new Point2D.Double((0.8317757009345794 * IMAGE_WIDTH), (0.8644859813084113 * IMAGE_HEIGHT));
                final float[] METALKNOB_LOWERHL_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] METALKNOB_LOWERHL_COLORS = {
                    new Color(255, 255, 255, 153),
                    new Color(255, 255, 255, 0)
                };
                final RadialGradientPaint METALKNOB_LOWERHL_GRADIENT = new RadialGradientPaint(METALKNOB_LOWERHL_CENTER, (float) (0.03271028037383177 * IMAGE_WIDTH), METALKNOB_LOWERHL_FRACTIONS, METALKNOB_LOWERHL_COLORS);
                G2.setPaint(METALKNOB_LOWERHL_GRADIENT);
                G2.fill(METALKNOB_LOWERHL);

                final GeneralPath METALKNOB_UPPERHL = new GeneralPath();
                METALKNOB_UPPERHL.setWindingRule(Path2D.WIND_EVEN_ODD);
                METALKNOB_UPPERHL.moveTo(IMAGE_WIDTH * 0.8644859813084113, IMAGE_HEIGHT * 0.8084112149532711);
                METALKNOB_UPPERHL.curveTo(IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.794392523364486, IMAGE_WIDTH * 0.8457943925233645, IMAGE_HEIGHT * 0.7850467289719626, IMAGE_WIDTH * 0.8317757009345794, IMAGE_HEIGHT * 0.7850467289719626);
                METALKNOB_UPPERHL.curveTo(IMAGE_WIDTH * 0.8130841121495327, IMAGE_HEIGHT * 0.7850467289719626, IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.794392523364486, IMAGE_WIDTH * 0.794392523364486, IMAGE_HEIGHT * 0.8084112149532711);
                METALKNOB_UPPERHL.curveTo(IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.8130841121495327, IMAGE_WIDTH * 0.8130841121495327, IMAGE_HEIGHT * 0.8177570093457944, IMAGE_WIDTH * 0.8317757009345794, IMAGE_HEIGHT * 0.8177570093457944);
                METALKNOB_UPPERHL.curveTo(IMAGE_WIDTH * 0.8457943925233645, IMAGE_HEIGHT * 0.8177570093457944, IMAGE_WIDTH * 0.8598130841121495, IMAGE_HEIGHT * 0.8130841121495327, IMAGE_WIDTH * 0.8644859813084113, IMAGE_HEIGHT * 0.8084112149532711);
                METALKNOB_UPPERHL.closePath();
                final Point2D METALKNOB_UPPERHL_CENTER = new Point2D.Double((0.8271028037383178 * IMAGE_WIDTH), (0.7850467289719626 * IMAGE_HEIGHT));
                final float[] METALKNOB_UPPERHL_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] METALKNOB_UPPERHL_COLORS = {
                    new Color(255, 255, 255, 191),
                    new Color(255, 255, 255, 0)
                };
                final RadialGradientPaint METALKNOB_UPPERHL_GRADIENT = new RadialGradientPaint(METALKNOB_UPPERHL_CENTER, (float) (0.04906542056074766 * IMAGE_WIDTH), METALKNOB_UPPERHL_FRACTIONS, METALKNOB_UPPERHL_COLORS);
                G2.setPaint(METALKNOB_UPPERHL_GRADIENT);
                G2.fill(METALKNOB_UPPERHL);

                final Ellipse2D METALKNOB_INNERFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.8084112405776978, IMAGE_HEIGHT * 0.8084112405776978, IMAGE_WIDTH * 0.04205602407455444, IMAGE_HEIGHT * 0.04205602407455444);
                final Point2D METALKNOB_INNERFRAME_START = new Point2D.Double(0, METALKNOB_INNERFRAME.getBounds2D().getMinY());
                final Point2D METALKNOB_INNERFRAME_STOP = new Point2D.Double(0, METALKNOB_INNERFRAME.getBounds2D().getMaxY());
                final float[] METALKNOB_INNERFRAME_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] METALKNOB_INNERFRAME_COLORS = {
                    new Color(0, 0, 0, 255),
                    new Color(204, 204, 204, 255)
                };
                final LinearGradientPaint METALKNOB_INNERFRAME_GRADIENT = new LinearGradientPaint(METALKNOB_INNERFRAME_START, METALKNOB_INNERFRAME_STOP, METALKNOB_INNERFRAME_FRACTIONS, METALKNOB_INNERFRAME_COLORS);
                G2.setPaint(METALKNOB_INNERFRAME_GRADIENT);
                G2.fill(METALKNOB_INNERFRAME);

                final Ellipse2D METALKNOB_INNERBACKGROUND = new Ellipse2D.Double(IMAGE_WIDTH * 0.8130841255187988, IMAGE_HEIGHT * 0.8130841255187988, IMAGE_WIDTH * 0.032710254192352295, IMAGE_HEIGHT * 0.032710254192352295);
                final Point2D METALKNOB_INNERBACKGROUND_START = new Point2D.Double(0, METALKNOB_INNERBACKGROUND.getBounds2D().getMinY());
                final Point2D METALKNOB_INNERBACKGROUND_STOP = new Point2D.Double(0, METALKNOB_INNERBACKGROUND.getBounds2D().getMaxY());
                final float[] METALKNOB_INNERBACKGROUND_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] METALKNOB_INNERBACKGROUND_COLORS = {
                    new Color(1, 6, 11, 255),
                    new Color(50, 52, 56, 255)
                };
                final LinearGradientPaint METALKNOB_INNERBACKGROUND_GRADIENT = new LinearGradientPaint(METALKNOB_INNERBACKGROUND_START, METALKNOB_INNERBACKGROUND_STOP, METALKNOB_INNERBACKGROUND_FRACTIONS, METALKNOB_INNERBACKGROUND_COLORS);
                G2.setPaint(METALKNOB_INNERBACKGROUND_GRADIENT);
                G2.fill(METALKNOB_INNERBACKGROUND);
                break;
        }
        G2.dispose();

        return image;
    }

    private BufferedImage create_FOREGROUND_Image(final int WIDTH, BufferedImage image) {
        if (WIDTH <= 0) {
            return null;
        }

        if (image == null) {
            image = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        }

        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        transformGraphics(IMAGE_WIDTH, IMAGE_HEIGHT, G2);

        if (getOrientation() == Orientation.NORTH_EAST || getOrientation() == Orientation.NORTH_WEST) {
            final GeneralPath HIGHLIGHT = new GeneralPath();
            HIGHLIGHT.setWindingRule(Path2D.WIND_EVEN_ODD);
            HIGHLIGHT.moveTo(IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.3925233644859813);
            HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.35514018691588783, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.08411214953271028, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.08411214953271028);
            HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.7710280373831776, IMAGE_HEIGHT * 0.08411214953271028, IMAGE_WIDTH * 0.5887850467289719, IMAGE_HEIGHT * 0.102803738317757, IMAGE_WIDTH * 0.4392523364485981, IMAGE_HEIGHT * 0.205607476635514);
            HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.3037383177570093, IMAGE_HEIGHT * 0.29439252336448596, IMAGE_WIDTH * 0.22429906542056074, IMAGE_HEIGHT * 0.37850467289719625, IMAGE_WIDTH * 0.1542056074766355, IMAGE_HEIGHT * 0.5420560747663551);
            HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.3691588785046729, IMAGE_WIDTH * 0.794392523364486, IMAGE_HEIGHT * 0.38317757009345793, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.3925233644859813);
            HIGHLIGHT.closePath();
            final Point2D HIGHLIGHT_START = new Point2D.Double(0, HIGHLIGHT.getBounds2D().getMinY());
            final Point2D HIGHLIGHT_STOP = new Point2D.Double(0, HIGHLIGHT.getBounds2D().getMaxY());
            final float[] HIGHLIGHT_FRACTIONS = {
                0.0f,
                1.0f
            };
            final Color[] HIGHLIGHT_COLORS = {
                new Color(255, 255, 255, 63),
                new Color(255, 255, 255, 12)
            };
            final LinearGradientPaint HIGHLIGHT_GRADIENT = new LinearGradientPaint(HIGHLIGHT_START, HIGHLIGHT_STOP, HIGHLIGHT_FRACTIONS, HIGHLIGHT_COLORS);
            G2.setPaint(HIGHLIGHT_GRADIENT);
            G2.fill(HIGHLIGHT);
        } else {
            final GeneralPath HIGHLIGHT_FLIPPED = new GeneralPath();
            HIGHLIGHT_FLIPPED.setWindingRule(Path2D.WIND_EVEN_ODD);
            HIGHLIGHT_FLIPPED.moveTo(IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.9158878504672897);
            HIGHLIGHT_FLIPPED.curveTo(IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.9158878504672897, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.5560747663551402, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.5560747663551402);
            HIGHLIGHT_FLIPPED.curveTo(IMAGE_WIDTH * 0.5841121495327103, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.22897196261682243, IMAGE_HEIGHT * 0.6308411214953271, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.9158878504672897);
            HIGHLIGHT_FLIPPED.curveTo(IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.9158878504672897, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.9158878504672897, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.9158878504672897);
            HIGHLIGHT_FLIPPED.closePath();
            final Point2D HIGHLIGHT_FLIPPED_START = new Point2D.Double(0, HIGHLIGHT_FLIPPED.getBounds2D().getMaxY());
            final Point2D HIGHLIGHT_FLIPPED_STOP = new Point2D.Double(0, HIGHLIGHT_FLIPPED.getBounds2D().getMinY());
            final float[] HIGHLIGHT_FLIPPED_FRACTIONS = {
                0.0f,
                1.0f
            };
            final Color[] HIGHLIGHT_FLIPPED_COLORS = {
                new Color(255, 255, 255, 63),
                new Color(255, 255, 255, 12)
            };
            final LinearGradientPaint HIGHLIGHT_FLIPPED_GRADIENT = new LinearGradientPaint(HIGHLIGHT_FLIPPED_START, HIGHLIGHT_FLIPPED_STOP, HIGHLIGHT_FLIPPED_FRACTIONS, HIGHLIGHT_FLIPPED_COLORS);
            G2.setPaint(HIGHLIGHT_FLIPPED_GRADIENT);
            G2.fill(HIGHLIGHT_FLIPPED);
        }
        G2.dispose();

        return image;
    }

    private BufferedImage create_GLOW_Image(final int WIDTH, final Color GLOW_COLOR, final boolean ON) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);

        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final GeneralPath GLOWRING = new GeneralPath();
        GLOWRING.setWindingRule(Path2D.WIND_EVEN_ODD);
        GLOWRING.moveTo(IMAGE_WIDTH * 0.8925233644859814, IMAGE_HEIGHT * 0.8925233644859814);
        GLOWRING.curveTo(IMAGE_WIDTH * 0.8925233644859814, IMAGE_HEIGHT * 0.8925233644859814, IMAGE_WIDTH * 0.10747663551401869, IMAGE_HEIGHT * 0.8925233644859814, IMAGE_WIDTH * 0.10747663551401869, IMAGE_HEIGHT * 0.8925233644859814);
        GLOWRING.curveTo(IMAGE_WIDTH * 0.10747663551401869, IMAGE_HEIGHT * 0.6588785046728972, IMAGE_WIDTH * 0.19626168224299065, IMAGE_HEIGHT * 0.4532710280373832, IMAGE_WIDTH * 0.308411214953271, IMAGE_HEIGHT * 0.3411214953271028);
        GLOWRING.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.17757009345794392, IMAGE_WIDTH * 0.6308411214953271, IMAGE_HEIGHT * 0.10747663551401869, IMAGE_WIDTH * 0.8925233644859814, IMAGE_HEIGHT * 0.10747663551401869);
        GLOWRING.curveTo(IMAGE_WIDTH * 0.8925233644859814, IMAGE_HEIGHT * 0.10747663551401869, IMAGE_WIDTH * 0.8925233644859814, IMAGE_HEIGHT * 0.8925233644859814, IMAGE_WIDTH * 0.8925233644859814, IMAGE_HEIGHT * 0.8925233644859814);
        GLOWRING.closePath();
        GLOWRING.moveTo(IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.9158878504672897);
        GLOWRING.curveTo(IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.9158878504672897, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.08411214953271028, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.08411214953271028);
        GLOWRING.curveTo(IMAGE_WIDTH * 0.6401869158878505, IMAGE_HEIGHT * 0.08411214953271028, IMAGE_WIDTH * 0.46261682242990654, IMAGE_HEIGHT * 0.1588785046728972, IMAGE_WIDTH * 0.29439252336448596, IMAGE_HEIGHT * 0.32242990654205606);
        GLOWRING.curveTo(IMAGE_WIDTH * 0.17289719626168223, IMAGE_HEIGHT * 0.4439252336448598, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.6635514018691588, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.9158878504672897);
        GLOWRING.curveTo(IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.9158878504672897, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.9158878504672897, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.9158878504672897);
        GLOWRING.closePath();

        if (!ON) {
            final Point2D GLOWRING_OFF_START = new Point2D.Double( (0.3037383177570093 * IMAGE_WIDTH), (0.3037383177570093 * IMAGE_HEIGHT) );
            final Point2D GLOWRING_OFF_STOP = new Point2D.Double( ((0.3037383177570093 + 0.5980669504428276) * IMAGE_WIDTH), ((0.3037383177570093 + 0.5980669504428275) * IMAGE_HEIGHT) );

            final float[] GLOWRING_OFF_FRACTIONS = {
                0.0f,
                0.19f,
                0.2f,
                0.39f,
                0.4f,
                0.64f,
                0.65f,
                0.82f,
                1.0f
            };
            final Color[] GLOWRING_OFF_COLORS = {
                new Color(204, 204, 204, 102),
                new Color(255, 255, 255, 102),
                new Color(250, 250, 250, 102),
                new Color(158, 158, 158, 102),
                new Color(153, 153, 153, 102),
                new Color(202, 202, 202, 102),
                new Color(204, 204, 204, 102),
                new Color(255, 255, 255, 102),
                new Color(153, 153, 153, 102)
            };
            final LinearGradientPaint GLOWRING_OFF_GRADIENT = new LinearGradientPaint(GLOWRING_OFF_START, GLOWRING_OFF_STOP, GLOWRING_OFF_FRACTIONS, GLOWRING_OFF_COLORS);
            G2.setPaint(GLOWRING_OFF_GRADIENT);
            G2.fill(GLOWRING);
        } else {
            G2.translate(-10, -10);
            G2.drawImage(Shadow.INSTANCE.createDropShadow(GLOWRING, GLOW_COLOR, GLOW_COLOR, true, null, null, 0, 1.0f, 10, 315, GLOW_COLOR), GLOWRING.getBounds().x, GLOWRING.getBounds().y, null);
            G2.translate(10, 10);

            final Point2D GLOWRING_HL_START = new Point2D.Double( (0.3037383177570093 * IMAGE_WIDTH), (0.3037383177570093 * IMAGE_HEIGHT) );
            final Point2D GLOWRING_HL_STOP = new Point2D.Double( ((0.3037383177570093 + 0.5980669504428276) * IMAGE_WIDTH), ((0.3037383177570093 + 0.5980669504428275) * IMAGE_HEIGHT) );
            final float[] GLOWRING_HL_FRACTIONS = {
                0.0f,
                0.26f,
                0.42f,
                0.42009997f,
                0.56f,
                0.5601f,
                0.96f,
                0.9601f,
                1.0f
            };
            final Color[] GLOWRING_HL_COLORS = {
                new Color(255, 255, 255, 0),
                new Color(255, 255, 255, 63),
                new Color(255, 255, 255, 102),
                new Color(255, 255, 255, 98),
                new Color(255, 255, 255, 3),
                new Color(255, 255, 255, 0),
                new Color(255, 255, 255, 0),
                new Color(255, 255, 255, 0),
                new Color(255, 255, 255, 102)
            };
            final LinearGradientPaint GLOWRING_HL_GRADIENT = new LinearGradientPaint(GLOWRING_HL_START, GLOWRING_HL_STOP, GLOWRING_HL_FRACTIONS, GLOWRING_HL_COLORS);
            G2.setPaint(GLOWRING_HL_GRADIENT);
            G2.fill(GLOWRING);
        }
        G2.dispose();

        return IMAGE;
    }

    @Override
    protected BufferedImage create_DISABLED_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        transformGraphics(IMAGE_WIDTH, IMAGE_HEIGHT, G2);

        final GeneralPath BACKGROUND = new GeneralPath();
        BACKGROUND.setWindingRule(Path2D.WIND_EVEN_ODD);
        BACKGROUND.moveTo(IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.9158878504672897);
        BACKGROUND.curveTo(IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.9158878504672897, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.08411214953271028, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.08411214953271028);
        BACKGROUND.curveTo(IMAGE_WIDTH * 0.6401869158878505, IMAGE_HEIGHT * 0.08411214953271028, IMAGE_WIDTH * 0.46261682242990654, IMAGE_HEIGHT * 0.1588785046728972, IMAGE_WIDTH * 0.29439252336448596, IMAGE_HEIGHT * 0.32242990654205606);
        BACKGROUND.curveTo(IMAGE_WIDTH * 0.17289719626168223, IMAGE_HEIGHT * 0.4439252336448598, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.6635514018691588, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.9158878504672897);
        BACKGROUND.curveTo(IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.9158878504672897, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.9158878504672897, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.9158878504672897);
        BACKGROUND.closePath();

        G2.setColor(new Color(102, 102, 102, 178));
        G2.fill(BACKGROUND);

        G2.dispose();

        return IMAGE;
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "Radial1Square";
    }
}
