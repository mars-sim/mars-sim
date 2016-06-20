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
import org.mars_sim.msp.ui.steelseries.tools.ConicalGradientPaint;
import org.mars_sim.msp.ui.steelseries.tools.FrameDesign;
import org.mars_sim.msp.ui.steelseries.tools.FrameType;
import org.mars_sim.msp.ui.steelseries.tools.GaugeType;
import org.mars_sim.msp.ui.steelseries.tools.GradientWrapper;
import org.mars_sim.msp.ui.steelseries.tools.Model;
import org.mars_sim.msp.ui.steelseries.tools.Orientation;
import org.mars_sim.msp.ui.steelseries.tools.Scaler;
import org.mars_sim.msp.ui.steelseries.tools.Section;
import org.mars_sim.msp.ui.steelseries.tools.Shadow;


/**
 *
 * @author hansolo
 */
public final class Radial2Top extends AbstractRadial {
    // <editor-fold defaultstate="collapsed" desc="Variable declarations">
    private static final int BASE = 10;
    private final double FREE_AREA_ANGLE = Math.toRadians(0); // area where no tickmarks will be painted
    private final double ROTATION_OFFSET = (1.5 * Math.PI) + (FREE_AREA_ANGLE / 2.0); // Offset for the pointer
    private final Point2D CENTER;
    private final Point2D TRACK_OFFSET;
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
    private boolean section3DEffectVisible;
    private RadialGradientPaint section3DEffect;
    private boolean area3DEffectVisible;
    private RadialGradientPaint area3DEffect;
    private double angle = 0;
    private final Color DARK_NOISE;
    private final Color BRIGHT_NOISE;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public Radial2Top() {
        super();
        CENTER = new Point2D.Double();
        TRACK_OFFSET = new Point2D.Double();
        DARK_NOISE = new Color(0.2f, 0.2f, 0.2f);
        BRIGHT_NOISE = new Color(0.8f, 0.8f, 0.8f);
        section3DEffectVisible = false;
        area3DEffectVisible = false;
        setGaugeType(GaugeType.TYPE2);
        init(getInnerBounds().width, getInnerBounds().height);
    }

    public Radial2Top(final Model MODEL) {
        super();
        setModel(MODEL);
        CENTER = new Point2D.Double();
        TRACK_OFFSET = new Point2D.Double();
        DARK_NOISE = new Color(0.2f, 0.2f, 0.2f);
        BRIGHT_NOISE = new Color(0.8f, 0.8f, 0.8f);
        section3DEffectVisible = false;
        area3DEffectVisible = false;
        setGaugeType(GaugeType.TYPE2);
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
            create_BACKGROUND_Image(GAUGE_WIDTH, "", "", bImage);
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

        create_POSTS_Image(GAUGE_WIDTH, fImage);

        TRACK_OFFSET.setLocation(0, 0);
        CENTER.setLocation(getGaugeBounds().getCenterX() - getInsets().left, getGaugeBounds().getCenterX() - getInsets().top);
        if (isTrackVisible()) {
            create_TRACK_Image(GAUGE_WIDTH, getFreeAreaAngle(), getTickmarkOffset(), getMinValue(), getMaxValue(), getAngleStep(), getTrackStart(), getTrackSection(), getTrackStop(), getTrackStartColor(), getTrackSectionColor(), getTrackStopColor(), 0.38f, CENTER, getTickmarkDirection(), TRACK_OFFSET, bImage);
        }

        if (!getAreas().isEmpty()){
            // Create the sections 3d effect gradient overlay
            if (area3DEffectVisible) {
                // Create the sections 3d effect gradient overlay
                area3DEffect = createArea3DEffectGradient(GAUGE_WIDTH, 0.38f);
            }
            createAreas(bImage);
        }

        if (!getSections().isEmpty()) {
            // Create the sections 3d effect gradient overlay
            if (section3DEffectVisible) {
                // Create the sections 3d effect gradient overlay
                section3DEffect = createSection3DEffectGradient(GAUGE_WIDTH, 0.38f);
            }
            createSections(bImage);
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
                                                       0.38f,
                                                       0.09f,
                                                       CENTER,
                                                       new Point2D.Double(0, 0),
                                                       Orientation.NORTH,
                                                       getModel().getTicklabelOrientation(),
                                                       getModel().isNiceScale(),
                                                       getModel().isLogScale(),
                                                       bImage);

        create_TITLE_Image(GAUGE_WIDTH, getTitle(), getUnitString(), bImage);

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

        if (isForegroundVisible()) {
            FOREGROUND_FACTORY.createRadialForeground(GAUGE_WIDTH, false, getForegroundType(), fImage);
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

        if (disabledImage != null) {
            disabledImage.flush();
        }
        disabledImage = create_DISABLED_Image(GAUGE_WIDTH);

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

        // Draw threshold indicator
        if (isThresholdVisible()) {
            if (!isLogScale()) {
                G2.rotate(ROTATION_OFFSET + (getThreshold() - getMinValue()) * getAngleStep(), CENTER.getX(), CENTER.getY());
            } else {
                G2.rotate(ROTATION_OFFSET + UTIL.logOfBase(BASE, getThreshold() - getMinValue()) * getLogAngleStep(), CENTER.getX(), CENTER.getY());
            }
            G2.drawImage(thresholdImage, (int) (getGaugeBounds().width * 0.480369999), (int) (getGaugeBounds().height * 0.13), null);
            G2.setTransform(OLD_TRANSFORM);
        }

        // Draw min measured value indicator
        if (isMinMeasuredValueVisible()) {
            if (!isLogScale()) {
                G2.rotate(ROTATION_OFFSET + (getMinMeasuredValue() - getMinValue()) * getAngleStep(), CENTER.getX(), CENTER.getY());
            } else {
                G2.rotate(ROTATION_OFFSET + UTIL.logOfBase(BASE, getMinMeasuredValue() - getMinValue()) * getLogAngleStep(), CENTER.getX(), CENTER.getY());
            }
            G2.drawImage(minMeasuredImage, (int) (getGaugeBounds().width * 0.4865), (int) (getGaugeBounds().height * 0.105), null);
            G2.setTransform(OLD_TRANSFORM);
        }

        // Draw max measured value indicator
        if (isMaxMeasuredValueVisible()) {
            if (!isLogScale()) {
                G2.rotate(ROTATION_OFFSET + (getMaxMeasuredValue() - getMinValue()) * getAngleStep(), CENTER.getX(), CENTER.getY());
            } else {
                G2.rotate(ROTATION_OFFSET + UTIL.logOfBase(BASE, getMaxMeasuredValue() - getMinValue()) * getLogAngleStep(), CENTER.getX(), CENTER.getY());
            }
            G2.drawImage(maxMeasuredImage, (int) (getGaugeBounds().width * 0.4865), (int) (getGaugeBounds().height * 0.105), null);
            G2.setTransform(OLD_TRANSFORM);
        }

        // Draw LED if enabled
        if (isLedVisible()) {
            G2.drawImage(getCurrentLedImage(), (int) (getGaugeBounds().width * getLedPosition().getX()), (int) (getGaugeBounds().height * getLedPosition().getY()), null);
        }

        // Draw user LED if enabled
        if (isUserLedVisible()) {
            G2.drawImage(getCurrentUserLedImage(), (int) (getGaugeBounds().width * getUserLedPosition().getX()), (int) (getGaugeBounds().height * getUserLedPosition().getY()), null);
        }

        // Draw the pointer
        if (!isLogScale()) {
            angle = ROTATION_OFFSET + (getValue() - getMinValue()) * getAngleStep();
        } else {
            angle = ROTATION_OFFSET + UTIL.logOfBase(BASE, (getValue() - getMinValue())) * getLogAngleStep();
        }
        G2.rotate(angle, CENTER.getX(), CENTER.getY() + 2);
        G2.drawImage(pointerShadowImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);
        G2.rotate(angle, CENTER.getX(), CENTER.getY());
        G2.drawImage(pointerImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw combined foreground image
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
    @Override
    public FrameType getFrameType() {
        return FrameType.ROUND;
    }

    @Override
    public GaugeType getGaugeType() {
        return GaugeType.TYPE2;
    }

    @Override
    public void setGaugeType(final GaugeType GAUGE_TYPE) {
        super.setGaugeType(GaugeType.TYPE2);
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

    /**
     * Returns true if the 3d effect gradient overlay for the sections is visible
     * @return true if the 3d effect gradient overlay for the sections is visible
     */
    public boolean isSection3DEffectVisible() {
        return this.section3DEffectVisible;
    }

    /**
     * Defines the visibility of the 3d effect gradient overlay for the sections
     * @param SECTION_3D_EFFECT_VISIBLE
     */
    public void setSection3DEffectVisible(final boolean SECTION_3D_EFFECT_VISIBLE) {
        this.section3DEffectVisible = SECTION_3D_EFFECT_VISIBLE;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns true if the 3d effect gradient overlay for the areas is visible
     * @return true if the 3d effect gradient overlay for the areas is visible
     */
    public boolean isArea3DEffectVisible() {
        return area3DEffectVisible;
    }

    /**
     * Enables / disables the visibility of the 3d effect gradient overlay for the areas
     * @param AREA_3DEFFECT_VISIBLE
     */
    public void setArea3DEffectVisible(final boolean AREA_3DEFFECT_VISIBLE) {
        area3DEffectVisible = AREA_3DEFFECT_VISIBLE;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Areas related">
    private void createAreas(final BufferedImage IMAGE) {
        if (!getAreas().isEmpty() && bImage != null) {
            final double ORIGIN_CORRECTION = 180.0;
            final double OUTER_RADIUS = bImage.getWidth() * 0.38f;
            final double RADIUS;
            if (isSectionsVisible()) {
                RADIUS = isExpandedSectionsEnabled() ? OUTER_RADIUS - bImage.getWidth() * 0.12f : OUTER_RADIUS - bImage.getWidth() * 0.04f;
            } else {
                RADIUS = OUTER_RADIUS;
            }
            final double FREE_AREA = bImage.getWidth() / 2.0 - RADIUS;

            for (Section area : getAreas()) {
                final double ANGLE_START;
                final double ANGLE_EXTEND;

                if (!isLogScale()) {
                    ANGLE_START = ORIGIN_CORRECTION - (area.getStart() * Math.toDegrees(getAngleStep()));
                    ANGLE_EXTEND = -(area.getStop() - area.getStart()) * Math.toDegrees(getAngleStep());
                } else {
                    ANGLE_START = ORIGIN_CORRECTION - (UTIL.logOfBase(BASE, area.getStart()) * Math.toDegrees(getLogAngleStep()));
                    ANGLE_EXTEND = -UTIL.logOfBase(BASE, area.getStop() - area.getStart()) * Math.toDegrees(getLogAngleStep());
                }

                final Arc2D AREA = new Arc2D.Double(Arc2D.PIE);
                AREA.setFrame(bImage.getMinX() + FREE_AREA, bImage.getMinY() + FREE_AREA, 2 * RADIUS, 2 * RADIUS);
                AREA.setAngleStart(ANGLE_START);
                AREA.setAngleExtent(ANGLE_EXTEND);

                area.setFilledArea(AREA);
            }

            // Draw the area
            if (isAreasVisible() && IMAGE != null) {
                final Graphics2D G2 = IMAGE.createGraphics();
                G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (Section area : getAreas()) {
                    G2.setColor(isTransparentAreasEnabled() ? area.getTransparentColor() : area.getColor());
                    G2.fill(area.getFilledArea());
                    if (area3DEffectVisible) {
                        G2.setPaint(area3DEffect);
                        G2.fill(area.getFilledArea());
                    }
                }
                G2.dispose();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Sections related">
    private void createSections(final BufferedImage IMAGE) {
        if (!getSections().isEmpty() && bImage != null) {
            final double ORIGIN_CORRECTION = 180.0;
            final double OUTER_RADIUS = bImage.getWidth() * 0.38f;
            final double INNER_RADIUS = isExpandedSectionsEnabled() ? OUTER_RADIUS - bImage.getWidth() * 0.12f : OUTER_RADIUS - bImage.getWidth() * 0.04f;
            final double FREE_AREA_OUTER_RADIUS = bImage.getWidth() / 2.0 - OUTER_RADIUS;
            final double FREE_AREA_INNER_RADIUS = bImage.getWidth() / 2.0 - INNER_RADIUS;
            final Ellipse2D INNER = new Ellipse2D.Double(bImage.getMinX() + FREE_AREA_INNER_RADIUS, bImage.getMinY() + FREE_AREA_INNER_RADIUS, 2 * INNER_RADIUS, 2 * INNER_RADIUS);

            for (Section section : getSections()) {
                final double ANGLE_START;
                final double ANGLE_EXTEND;

                if (!isLogScale()) {
                    ANGLE_START = ORIGIN_CORRECTION - (section.getStart() * Math.toDegrees(getAngleStep()));
                    ANGLE_EXTEND = -(section.getStop() - section.getStart()) * Math.toDegrees(getAngleStep());
                } else {
                    ANGLE_START = ORIGIN_CORRECTION - (UTIL.logOfBase(BASE, section.getStart()) * Math.toDegrees(getLogAngleStep()));
                    ANGLE_EXTEND = -UTIL.logOfBase(BASE, section.getStop() - section.getStart()) * Math.toDegrees(getLogAngleStep());
                }

                final Arc2D OUTER_ARC = new Arc2D.Double(Arc2D.PIE);
                OUTER_ARC.setFrame(bImage.getMinX() + FREE_AREA_OUTER_RADIUS, bImage.getMinY() + FREE_AREA_OUTER_RADIUS, 2 * OUTER_RADIUS, 2 * OUTER_RADIUS);
                OUTER_ARC.setAngleStart(ANGLE_START);
                OUTER_ARC.setAngleExtent(ANGLE_EXTEND);
                final java.awt.geom.Area SECTION = new Area(OUTER_ARC);

                SECTION.subtract(new Area(INNER));

                section.setSectionArea(SECTION);
            }

            // Draw the sections
            if (isSectionsVisible() && IMAGE != null) {
                final Graphics2D G2 = IMAGE.createGraphics();
                G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (Section section : getSections()) {
                    G2.setColor(isTransparentSectionsEnabled() ? section.getTransparentColor() : section.getColor());
                    G2.fill(section.getSectionArea());
                    if (section3DEffectVisible) {
                        G2.setPaint(section3DEffect);
                        G2.fill(section.getSectionArea());
                    }
                }
                G2.dispose();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image related">
    private BufferedImage create_FRAME_Image(final int WIDTH, BufferedImage image) {
        if (WIDTH <= 0) {
            return null;
        }
        final double VERTICAL_SCALE;

        if (image == null) {
            image = UTIL.createImage(WIDTH, (int) (0.641860465116279 * WIDTH), Transparency.TRANSLUCENT);
            VERTICAL_SCALE = 1.0;
        } else {
            VERTICAL_SCALE = 0.641860465116279;
        }
        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        // Define shape that will be subtracted from frame shapes
        final GeneralPath SUBTRACT_PATH = new GeneralPath();
        SUBTRACT_PATH.setWindingRule(Path2D.WIND_EVEN_ODD);
        SUBTRACT_PATH.moveTo(IMAGE_WIDTH * 0.08372093023255814, IMAGE_HEIGHT * 0.7753623188405797 * VERTICAL_SCALE);
        SUBTRACT_PATH.curveTo(IMAGE_WIDTH * 0.08372093023255814, IMAGE_HEIGHT * 0.42028985507246375 * VERTICAL_SCALE, IMAGE_WIDTH * 0.26976744186046514, IMAGE_HEIGHT * 0.13043478260869565 * VERTICAL_SCALE, IMAGE_WIDTH * 0.49767441860465117, IMAGE_HEIGHT * 0.13043478260869565 * VERTICAL_SCALE);
        SUBTRACT_PATH.curveTo(IMAGE_WIDTH * 0.7255813953488373, IMAGE_HEIGHT * 0.13043478260869565 * VERTICAL_SCALE, IMAGE_WIDTH * 0.9116279069767442, IMAGE_HEIGHT * 0.42028985507246375 * VERTICAL_SCALE, IMAGE_WIDTH * 0.9116279069767442, IMAGE_HEIGHT * 0.7753623188405797 * VERTICAL_SCALE);
        SUBTRACT_PATH.curveTo(IMAGE_WIDTH * 0.9116279069767442, IMAGE_HEIGHT * 0.8188405797101449 * VERTICAL_SCALE, IMAGE_WIDTH * 0.9069767441860465, IMAGE_HEIGHT * 0.8695652173913043 * VERTICAL_SCALE, IMAGE_WIDTH * 0.9069767441860465, IMAGE_HEIGHT * 0.8695652173913043 * VERTICAL_SCALE);
        SUBTRACT_PATH.lineTo(IMAGE_WIDTH * 0.08837209302325581, IMAGE_HEIGHT * 0.8695652173913043 * VERTICAL_SCALE);
        SUBTRACT_PATH.curveTo(IMAGE_WIDTH * 0.08837209302325581, IMAGE_HEIGHT * 0.8695652173913043 * VERTICAL_SCALE, IMAGE_WIDTH * 0.08372093023255814, IMAGE_HEIGHT * 0.8115942028985508 * VERTICAL_SCALE, IMAGE_WIDTH * 0.08372093023255814, IMAGE_HEIGHT * 0.7753623188405797 * VERTICAL_SCALE);
        SUBTRACT_PATH.closePath();
        final Area SUBTRACT = new Area(SUBTRACT_PATH);

        final GeneralPath FRAME_OUTERFRAME = new GeneralPath();
        FRAME_OUTERFRAME.setWindingRule(Path2D.WIND_EVEN_ODD);
        FRAME_OUTERFRAME.moveTo(0.0, IMAGE_HEIGHT * 0.7753623188405797 * VERTICAL_SCALE);
        FRAME_OUTERFRAME.curveTo(0.0, IMAGE_HEIGHT * 0.34782608695652173 * VERTICAL_SCALE, IMAGE_WIDTH * 0.22325581395348837, 0.0, IMAGE_WIDTH * 0.49767441860465117, 0.0);
        FRAME_OUTERFRAME.curveTo(IMAGE_WIDTH * 0.772093023255814, 0.0, IMAGE_WIDTH, IMAGE_HEIGHT * 0.34782608695652173 * VERTICAL_SCALE, IMAGE_WIDTH, IMAGE_HEIGHT * 0.7753623188405797 * VERTICAL_SCALE);
        FRAME_OUTERFRAME.curveTo(IMAGE_WIDTH, IMAGE_HEIGHT * 0.9057971014492754 * VERTICAL_SCALE, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT * VERTICAL_SCALE);
        FRAME_OUTERFRAME.lineTo(0.0, IMAGE_HEIGHT * VERTICAL_SCALE);
        FRAME_OUTERFRAME.curveTo(0.0, IMAGE_HEIGHT * VERTICAL_SCALE, 0.0, IMAGE_HEIGHT * 0.8985507246376812 * VERTICAL_SCALE, 0.0, IMAGE_HEIGHT * 0.7753623188405797 * VERTICAL_SCALE);
        FRAME_OUTERFRAME.closePath();
        G2.setPaint(getOuterFrameColor());
        final Area FRAME_OUTERFRAME_AREA = new Area(FRAME_OUTERFRAME);
        FRAME_OUTERFRAME_AREA.subtract(SUBTRACT);
        G2.fill(FRAME_OUTERFRAME_AREA);

        final GeneralPath FRAME_MAIN = new GeneralPath();
        FRAME_MAIN.setWindingRule(Path2D.WIND_EVEN_ODD);
        FRAME_MAIN.moveTo(IMAGE_WIDTH * 0.004651162790697674, IMAGE_HEIGHT * 0.7753623188405797 * VERTICAL_SCALE);
        FRAME_MAIN.curveTo(IMAGE_WIDTH * 0.004651162790697674, IMAGE_HEIGHT * 0.34782608695652173 * VERTICAL_SCALE, IMAGE_WIDTH * 0.22325581395348837, IMAGE_HEIGHT * 0.007246376811594203 * VERTICAL_SCALE, IMAGE_WIDTH * 0.49767441860465117, IMAGE_HEIGHT * 0.007246376811594203 * VERTICAL_SCALE);
        FRAME_MAIN.curveTo(IMAGE_WIDTH * 0.772093023255814, IMAGE_HEIGHT * 0.007246376811594203 * VERTICAL_SCALE, IMAGE_WIDTH * 0.9953488372093023, IMAGE_HEIGHT * 0.35507246376811596 * VERTICAL_SCALE, IMAGE_WIDTH * 0.9953488372093023, IMAGE_HEIGHT * 0.7753623188405797 * VERTICAL_SCALE);
        FRAME_MAIN.curveTo(IMAGE_WIDTH * 0.9953488372093023, IMAGE_HEIGHT * 0.8840579710144928 * VERTICAL_SCALE, IMAGE_WIDTH * 0.9953488372093023, IMAGE_HEIGHT * 0.9927536231884058 * VERTICAL_SCALE, IMAGE_WIDTH * 0.9953488372093023, IMAGE_HEIGHT * 0.9927536231884058 * VERTICAL_SCALE);
        FRAME_MAIN.lineTo(IMAGE_WIDTH * 0.004651162790697674, IMAGE_HEIGHT * 0.9927536231884058 * VERTICAL_SCALE);
        FRAME_MAIN.curveTo(IMAGE_WIDTH * 0.004651162790697674, IMAGE_HEIGHT * 0.9927536231884058 * VERTICAL_SCALE, IMAGE_WIDTH * 0.004651162790697674, IMAGE_HEIGHT * 0.8840579710144928 * VERTICAL_SCALE, IMAGE_WIDTH * 0.004651162790697674, IMAGE_HEIGHT * 0.7753623188405797 * VERTICAL_SCALE);
        FRAME_MAIN.closePath();

        final Point2D FRAME_MAIN_START = new Point2D.Double(0, FRAME_MAIN.getBounds2D().getMinY());
        final Point2D FRAME_MAIN_STOP = new Point2D.Double(0, FRAME_MAIN.getBounds2D().getMaxY());
        final Point2D FRAME_MAIN_CENTER = new Point2D.Double(FRAME_MAIN.getBounds2D().getCenterX(), FRAME_MAIN.getBounds2D().getHeight() * 0.7753623188 * VERTICAL_SCALE);

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
                        45.0f,
                        85.0f,
                        180.0f,
                        275.0f,
                        315.0f,
                        360.0f
                    };

                    Color[] frameMainColors1 = {
                        new Color(254, 254, 254, 255),
                        new Color(0, 0, 0, 255),
                        new Color(0, 0, 0, 255),
                        new Color(0, 0, 0, 255),
                        new Color(0, 0, 0, 255),
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
                    float[] frameMainFractions3;
                    Color[] frameMainColors3;
                    if (isFrameBaseColorEnabled()) {
                        frameMainFractions3 = new float[]{
                            0.0f,
                            45.0f,
                            90.0f,
                            135.0f,
                            180.0f,
                            225.0f,
                            270.0f,
                            315.0f,
                            360.0f
                        };

                        frameMainColors3 = new Color[]{
                            new Color(254, 254, 254, 255),
                            new Color(getFrameBaseColor().getRed(), getFrameBaseColor().getGreen(), getFrameBaseColor().getBlue(), 255),
                            new Color(getFrameBaseColor().getRed(), getFrameBaseColor().getGreen(), getFrameBaseColor().getBlue(), 255),
                            new Color(getFrameBaseColor().brighter().brighter().getRed(), getFrameBaseColor().brighter().brighter().getGreen(), getFrameBaseColor().brighter().brighter().getBlue(), 255),
                            new Color(getFrameBaseColor().getRed(), getFrameBaseColor().getGreen(), getFrameBaseColor().getBlue(), 255),
                            new Color(getFrameBaseColor().brighter().brighter().getRed(), getFrameBaseColor().brighter().brighter().getGreen(), getFrameBaseColor().brighter().brighter().getBlue(), 255),
                            new Color(getFrameBaseColor().getRed(), getFrameBaseColor().getGreen(), getFrameBaseColor().getBlue(), 255),
                            new Color(getFrameBaseColor().getRed(), getFrameBaseColor().getGreen(), getFrameBaseColor().getBlue(), 255),
                            new Color(254, 254, 254, 255)
                        };
                    } else {
                        frameMainFractions3 = new float[]{
                            0.0f,
                            45.0f,
                            90.0f,
                            95.0f,
                            180.0f,
                            265.0f,
                            270.0f,
                            315.0f,
                            360.0f
                        };

                        frameMainColors3 = new Color[]{
                            new Color(254, 254, 254, 255),
                            new Color(210, 210, 210, 255),
                            new Color(179, 179, 179, 255),
                            new Color(160, 160, 160, 255),
                            new Color(160, 160, 160, 255),
                            new Color(160, 160, 160, 255),
                            new Color(179, 179, 179, 255),
                            new Color(210, 210, 210, 255),
                            new Color(254, 254, 254, 255)
                        };
                    }

                    Paint frameMainGradient3 = new ConicalGradientPaint(true, FRAME_MAIN_CENTER, 0, frameMainFractions3, frameMainColors3);
                    G2.setPaint(frameMainGradient3);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                case GLOSSY_METAL:
                    final GeneralPath FRAME_MAIN_GLOSSY1 = new GeneralPath();
                    FRAME_MAIN_GLOSSY1.setWindingRule(Path2D.WIND_EVEN_ODD);
                    FRAME_MAIN_GLOSSY1.moveTo(0.004672897196261682 * IMAGE_WIDTH, 0.7737226277372263 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY1.curveTo(0.004672897196261682 * IMAGE_WIDTH, 0.35036496350364965 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.22429906542056074 * IMAGE_WIDTH, 0.0072992700729927005 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.5 * IMAGE_WIDTH, 0.0072992700729927005 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY1.curveTo(0.7710280373831776 * IMAGE_WIDTH, 0.0072992700729927005 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.9953271028037384 * IMAGE_WIDTH, 0.35036496350364965 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.9953271028037384 * IMAGE_WIDTH, 0.7737226277372263 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY1.curveTo(0.9953271028037384 * IMAGE_WIDTH, 0.8832116788321168 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.9953271028037384 * IMAGE_WIDTH, 0.9927007299270073 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.9953271028037384 * IMAGE_WIDTH, 0.9927007299270073 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY1.lineTo(0.004672897196261682 * IMAGE_WIDTH, 0.9927007299270073 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY1.curveTo(0.004672897196261682 * IMAGE_WIDTH, 0.9927007299270073 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.004672897196261682 * IMAGE_WIDTH, 0.8832116788321168 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.004672897196261682 * IMAGE_WIDTH, 0.7737226277372263 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY1.closePath();
                    final Area FRAME_MAIN_GLOSSY_1 = new Area(FRAME_MAIN_GLOSSY1);
                    FRAME_MAIN_GLOSSY_1.subtract(SUBTRACT);
                    G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.9927007299270073 * IMAGE_HEIGHT * VERTICAL_SCALE), (float)(0.4953271028037383 * IMAGE_WIDTH), new float[]{0.0f, 0.01f, 0.95f, 1.0f}, new Color[]{new Color(0.8235294118f, 0.8235294118f, 0.8235294118f, 1f), new Color(0.8235294118f, 0.8235294118f, 0.8235294118f, 1f), new Color(0.8235294118f, 0.8235294118f, 0.8235294118f, 1f), new Color(0.9960784314f, 0.9960784314f, 0.9960784314f, 1f)}));
                    G2.fill(FRAME_MAIN_GLOSSY_1);

                    final GeneralPath FRAME_MAIN_GLOSSY2 = new GeneralPath();
                    FRAME_MAIN_GLOSSY2.setWindingRule(Path2D.WIND_EVEN_ODD);
                    FRAME_MAIN_GLOSSY2.moveTo(0.014018691588785047 * IMAGE_WIDTH, 0.7737226277372263 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY2.curveTo(0.014018691588785047 * IMAGE_WIDTH, 0.36496350364963503 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.22429906542056074 * IMAGE_WIDTH, 0.021897810218978103 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.5 * IMAGE_WIDTH, 0.021897810218978103 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY2.curveTo(0.7710280373831776 * IMAGE_WIDTH, 0.021897810218978103 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.985981308411215 * IMAGE_WIDTH, 0.36496350364963503 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.985981308411215 * IMAGE_WIDTH, 0.7737226277372263 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY2.curveTo(0.985981308411215 * IMAGE_WIDTH, 0.8832116788321168 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.985981308411215 * IMAGE_WIDTH, 0.9781021897810219 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.985981308411215 * IMAGE_WIDTH, 0.9781021897810219 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY2.lineTo(0.014018691588785047 * IMAGE_WIDTH, 0.9781021897810219 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY2.curveTo(0.014018691588785047 * IMAGE_WIDTH, 0.9781021897810219 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.014018691588785047 * IMAGE_WIDTH, 0.8832116788321168 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.014018691588785047 * IMAGE_WIDTH, 0.7737226277372263 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY2.closePath();
                    final Area FRAME_MAIN_GLOSSY_2 = new Area(FRAME_MAIN_GLOSSY2);
                    FRAME_MAIN_GLOSSY_2.subtract(SUBTRACT);
                    G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.0072992700729927005 * IMAGE_HEIGHT * VERTICAL_SCALE), new Point2D.Double(0.5000000000000001 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT * VERTICAL_SCALE), new float[]{0.0f, 0.24f, 0.34f, 0.65f, 0.85f, 1.0f}, new Color[]{new Color(0.9764705882f, 0.9764705882f, 0.9764705882f, 1f), new Color(0.7843137255f, 0.7647058824f, 0.7490196078f, 1f), new Color(0.9882352941f, 0.9882352941f, 0.9882352941f, 1f), new Color(0.1215686275f, 0.1215686275f, 0.1215686275f, 1f), new Color(0.7843137255f, 0.7607843137f, 0.7529411765f, 1f), new Color(0.8156862745f, 0.8156862745f, 0.8156862745f, 1f)}));
                    G2.fill(FRAME_MAIN_GLOSSY_2);

                    final GeneralPath FRAME_MAIN_GLOSSY3 = new GeneralPath();
                    FRAME_MAIN_GLOSSY3.setWindingRule(Path2D.WIND_EVEN_ODD);
                    FRAME_MAIN_GLOSSY3.moveTo(0.07009345794392523 * IMAGE_WIDTH, 0.7737226277372263 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY3.curveTo(0.07009345794392523 * IMAGE_WIDTH, 0.39416058394160586 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.26635514018691586 * IMAGE_WIDTH, 0.10948905109489052 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.5 * IMAGE_WIDTH, 0.10948905109489052 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY3.curveTo(0.7289719626168224 * IMAGE_WIDTH, 0.10948905109489052 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.9252336448598131 * IMAGE_WIDTH, 0.38686131386861317 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.9252336448598131 * IMAGE_WIDTH, 0.7737226277372263 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY3.curveTo(0.9252336448598131 * IMAGE_WIDTH, 0.8102189781021898 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.9158878504672897 * IMAGE_WIDTH, 0.8905109489051095 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.9158878504672897 * IMAGE_WIDTH, 0.8905109489051095 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY3.lineTo(0.07476635514018691 * IMAGE_WIDTH, 0.8905109489051095 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY3.curveTo(0.07476635514018691 * IMAGE_WIDTH, 0.8905109489051095 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.07009345794392523 * IMAGE_WIDTH, 0.8102189781021898 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.07009345794392523 * IMAGE_WIDTH, 0.7737226277372263 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY3.closePath();
                    final Area FRAME_MAIN_GLOSSY_3 = new Area(FRAME_MAIN_GLOSSY3);
                    FRAME_MAIN_GLOSSY_3.subtract(SUBTRACT);
                    G2.setPaint(new Color(0.9647058824f, 0.9647058824f, 0.9647058824f, 1f));
                    G2.fill(FRAME_MAIN_GLOSSY_3);

                    final GeneralPath FRAME_MAIN_GLOSSY4 = new GeneralPath();
                    FRAME_MAIN_GLOSSY4.setWindingRule(Path2D.WIND_EVEN_ODD);
                    FRAME_MAIN_GLOSSY4.moveTo(0.07476635514018691 * IMAGE_WIDTH, 0.7737226277372263 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY4.curveTo(0.07476635514018691 * IMAGE_WIDTH, 0.41605839416058393 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.26635514018691586 * IMAGE_WIDTH, 0.11678832116788321 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.5 * IMAGE_WIDTH, 0.11678832116788321 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY4.curveTo(0.7289719626168224 * IMAGE_WIDTH, 0.11678832116788321 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.9205607476635514 * IMAGE_WIDTH, 0.41605839416058393 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.9205607476635514 * IMAGE_WIDTH, 0.7737226277372263 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY4.curveTo(0.9205607476635514 * IMAGE_WIDTH, 0.8102189781021898 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.9112149532710281 * IMAGE_WIDTH, 0.8832116788321168 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.9112149532710281 * IMAGE_WIDTH, 0.8832116788321168 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY4.lineTo(0.0794392523364486 * IMAGE_WIDTH, 0.8832116788321168 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY4.curveTo(0.0794392523364486 * IMAGE_WIDTH, 0.8832116788321168 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.07476635514018691 * IMAGE_WIDTH, 0.8102189781021898 * IMAGE_HEIGHT * VERTICAL_SCALE, 0.07476635514018691 * IMAGE_WIDTH, 0.7737226277372263 * IMAGE_HEIGHT * VERTICAL_SCALE);
                    FRAME_MAIN_GLOSSY4.closePath();
                    final Area FRAME_MAIN_GLOSSY_4 = new Area(FRAME_MAIN_GLOSSY4);
                    FRAME_MAIN_GLOSSY_4.subtract(SUBTRACT);
                    G2.setPaint(new Color(0.2f, 0.2f, 0.2f, 1f));
                    G2.fill(FRAME_MAIN_GLOSSY_4);
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

                    Paint frameMainGradient7 = new ConicalGradientPaint(false, FRAME_MAIN_CENTER, 0, frameMainFractions7, frameMainColors7);
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

        final GeneralPath FRAME_INNERFRAME = new GeneralPath();
        FRAME_INNERFRAME.setWindingRule(Path2D.WIND_EVEN_ODD);
        FRAME_INNERFRAME.moveTo(IMAGE_WIDTH * 0.07906976744186046, IMAGE_HEIGHT * 0.7753623188405797 * VERTICAL_SCALE);
        FRAME_INNERFRAME.curveTo(IMAGE_WIDTH * 0.07906976744186046, IMAGE_HEIGHT * 0.41304347826086957 * VERTICAL_SCALE, IMAGE_WIDTH * 0.2651162790697674, IMAGE_HEIGHT * 0.12318840579710146 * VERTICAL_SCALE, IMAGE_WIDTH * 0.49767441860465117, IMAGE_HEIGHT * 0.12318840579710146 * VERTICAL_SCALE);
        FRAME_INNERFRAME.curveTo(IMAGE_WIDTH * 0.7302325581395349, IMAGE_HEIGHT * 0.12318840579710146 * VERTICAL_SCALE, IMAGE_WIDTH * 0.9162790697674419, IMAGE_HEIGHT * 0.41304347826086957 * VERTICAL_SCALE, IMAGE_WIDTH * 0.9162790697674419, IMAGE_HEIGHT * 0.7753623188405797 * VERTICAL_SCALE);
        FRAME_INNERFRAME.curveTo(IMAGE_WIDTH * 0.9162790697674419, IMAGE_HEIGHT * 0.8115942028985508 * VERTICAL_SCALE, IMAGE_WIDTH * 0.9069767441860465, IMAGE_HEIGHT * 0.8768115942028986 * VERTICAL_SCALE, IMAGE_WIDTH * 0.9069767441860465, IMAGE_HEIGHT * 0.8768115942028986 * VERTICAL_SCALE);
        FRAME_INNERFRAME.lineTo(IMAGE_WIDTH * 0.08837209302325581, IMAGE_HEIGHT * 0.8768115942028986 * VERTICAL_SCALE);
        FRAME_INNERFRAME.curveTo(IMAGE_WIDTH * 0.08837209302325581, IMAGE_HEIGHT * 0.8768115942028986 * VERTICAL_SCALE, IMAGE_WIDTH * 0.07906976744186046, IMAGE_HEIGHT * 0.8115942028985508 * VERTICAL_SCALE, IMAGE_WIDTH * 0.07906976744186046, IMAGE_HEIGHT * 0.7753623188405797 * VERTICAL_SCALE);
        FRAME_INNERFRAME.closePath();
        G2.setPaint(getInnerFrameColor());
        final Area FRAME_INNERFRAME_AREA = new Area(FRAME_INNERFRAME);
        FRAME_INNERFRAME_AREA.subtract(SUBTRACT);
        G2.fill(FRAME_INNERFRAME_AREA);

        // Apply frame effects
        final float[] EFFECT_FRACTIONS;
        final Color[] EFFECT_COLORS;
        final GradientWrapper EFFECT_GRADIENT;
        float scale = 1.0f;
        final java.awt.Shape[] EFFECT = new java.awt.Shape[100];
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
                final Shape EFFECT_BIGINNERFRAME = Scaler.INSTANCE.scale(FRAME_MAIN_AREA, 0.8785046339035034);
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

        G2.dispose();

        return image;
    }

    @Override
    protected BufferedImage create_BACKGROUND_Image(final int WIDTH) {
        return create_BACKGROUND_Image(WIDTH, "", "", null);
    }

    @Override
    protected BufferedImage create_BACKGROUND_Image(final int WIDTH, final String TITLE, final String UNIT_STRING, BufferedImage image) {
        if (WIDTH <= 0) {
            return null;
        }
        final double VERTICAL_SCALE;

        if (image == null) {
            image = UTIL.createImage(WIDTH, (int) (0.641860465116279 * WIDTH), Transparency.TRANSLUCENT);
            VERTICAL_SCALE = 1.0;
        } else {
            VERTICAL_SCALE = 0.641860465116279;
        }
        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        boolean fadeInOut = false;

        final GeneralPath GAUGE_BACKGROUND = new GeneralPath();
        GAUGE_BACKGROUND.setWindingRule(Path2D.WIND_EVEN_ODD);
        GAUGE_BACKGROUND.moveTo(IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.7737226277372263 * VERTICAL_SCALE);
        GAUGE_BACKGROUND.curveTo(IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.41605839416058393 * VERTICAL_SCALE, IMAGE_WIDTH * 0.27102803738317754, IMAGE_HEIGHT * 0.13138686131386862 * VERTICAL_SCALE, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.13138686131386862 * VERTICAL_SCALE);
        GAUGE_BACKGROUND.curveTo(IMAGE_WIDTH * 0.7242990654205608, IMAGE_HEIGHT * 0.13138686131386862 * VERTICAL_SCALE, IMAGE_WIDTH * 0.9112149532710281, IMAGE_HEIGHT * 0.41605839416058393 * VERTICAL_SCALE, IMAGE_WIDTH * 0.9112149532710281, IMAGE_HEIGHT * 0.7737226277372263 * VERTICAL_SCALE);
        GAUGE_BACKGROUND.curveTo(IMAGE_WIDTH * 0.9112149532710281, IMAGE_HEIGHT * 0.8175182481751825 * VERTICAL_SCALE, IMAGE_WIDTH * 0.9065420560747663, IMAGE_HEIGHT * 0.8686131386861314 * VERTICAL_SCALE, IMAGE_WIDTH * 0.9065420560747663, IMAGE_HEIGHT * 0.8686131386861314 * VERTICAL_SCALE);
        GAUGE_BACKGROUND.lineTo(IMAGE_WIDTH * 0.08878504672897196, IMAGE_HEIGHT * 0.8686131386861314 * VERTICAL_SCALE);
        GAUGE_BACKGROUND.curveTo(IMAGE_WIDTH * 0.08878504672897196, IMAGE_HEIGHT * 0.8686131386861314 * VERTICAL_SCALE, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.8175182481751825 * VERTICAL_SCALE, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.7737226277372263 * VERTICAL_SCALE);
        GAUGE_BACKGROUND.closePath();

        final Point2D GAUGE_BACKGROUND_START = new Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMinY());
        final Point2D GAUGE_BACKGROUND_STOP = new Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMaxY());
        final float[] GAUGE_BACKGROUND_FRACTIONS = {
            0.0f,
            0.4f,
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
                backgroundPaint = new ConicalGradientPaint(false, getCenter(), -0.45f, STAINLESS_FRACTIONS, STAINLESS_COLORS);
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
            G2.drawImage(UTIL.getScaledInstance(getCustomLayer(), IMAGE_WIDTH, (int) (IMAGE_HEIGHT * VERTICAL_SCALE), RenderingHints.VALUE_INTERPOLATION_BICUBIC), 0, 0, null);
        }

        final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);

        if (!TITLE.isEmpty()) {
            if (isLabelColorFromThemeEnabled()) {
                G2.setColor(getBackgroundColor().LABEL_COLOR);
            } else {
                G2.setColor(getLabelColor());
            }
            G2.setFont(new Font("Verdana", 0, (int) (0.04672897196261682 * IMAGE_WIDTH)));
            final TextLayout TITLE_LAYOUT = new TextLayout(TITLE, G2.getFont(), RENDER_CONTEXT);
            final Rectangle2D TITLE_BOUNDARY = TITLE_LAYOUT.getBounds();
            G2.drawString(TITLE, (float) ((IMAGE_WIDTH - TITLE_BOUNDARY.getWidth()) / 2.0), (float) (0.44f * IMAGE_HEIGHT * VERTICAL_SCALE) + TITLE_LAYOUT.getAscent() - TITLE_LAYOUT.getDescent());
        }

        if (!UNIT_STRING.isEmpty()) {
            if (isLabelColorFromThemeEnabled()) {
                G2.setColor(getBackgroundColor().LABEL_COLOR);
            } else {
                G2.setColor(getLabelColor());
            }
            G2.setFont(new Font("Verdana", 0, (int) (0.04672897196261682 * IMAGE_WIDTH)));
            final TextLayout UNIT_LAYOUT = new TextLayout(UNIT_STRING, G2.getFont(), RENDER_CONTEXT);
            final Rectangle2D UNIT_BOUNDARY = UNIT_LAYOUT.getBounds();
            G2.drawString(UNIT_STRING, (float) ((IMAGE_WIDTH - UNIT_BOUNDARY.getWidth()) / 2.0), 0.52f * IMAGE_HEIGHT + UNIT_LAYOUT.getAscent() - UNIT_LAYOUT.getDescent());
        }

        G2.dispose();

        return image;
    }

    private BufferedImage create_GLOW_Image(final int WIDTH, final Color GLOW_COLOR, final boolean ON) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, (int) (0.641860465116279 * WIDTH), Transparency.TRANSLUCENT);

        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final GeneralPath GLOWRING = new GeneralPath();
        GLOWRING.setWindingRule(Path2D.WIND_EVEN_ODD);
        GLOWRING.moveTo(IMAGE_WIDTH * 0.11214953271028037, IMAGE_HEIGHT * 0.7737226277372263);
        GLOWRING.curveTo(IMAGE_WIDTH * 0.11214953271028037, IMAGE_HEIGHT * 0.4233576642335766, IMAGE_WIDTH * 0.29906542056074764, IMAGE_HEIGHT * 0.1678832116788321, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1678832116788321);
        GLOWRING.curveTo(IMAGE_WIDTH * 0.705607476635514, IMAGE_HEIGHT * 0.1678832116788321, IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.41605839416058393, IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.7737226277372263);
        GLOWRING.curveTo(IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.8175182481751825, IMAGE_WIDTH * 0.883177570093458, IMAGE_HEIGHT * 0.8613138686131386, IMAGE_WIDTH * 0.883177570093458, IMAGE_HEIGHT * 0.8613138686131386);
        GLOWRING.lineTo(IMAGE_WIDTH * 0.11682242990654206, IMAGE_HEIGHT * 0.8613138686131386);
        GLOWRING.curveTo(IMAGE_WIDTH * 0.11682242990654206, IMAGE_HEIGHT * 0.8613138686131386, IMAGE_WIDTH * 0.11214953271028037, IMAGE_HEIGHT * 0.8175182481751825, IMAGE_WIDTH * 0.11214953271028037, IMAGE_HEIGHT * 0.7737226277372263);
        GLOWRING.closePath();
        GLOWRING.moveTo(IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.7737226277372263);
        GLOWRING.curveTo(IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.8175182481751825, IMAGE_WIDTH * 0.08878504672897196, IMAGE_HEIGHT * 0.8686131386861314, IMAGE_WIDTH * 0.08878504672897196, IMAGE_HEIGHT * 0.8686131386861314);
        GLOWRING.lineTo(IMAGE_WIDTH * 0.9065420560747663, IMAGE_HEIGHT * 0.8686131386861314);
        GLOWRING.curveTo(IMAGE_WIDTH * 0.9065420560747663, IMAGE_HEIGHT * 0.8686131386861314, IMAGE_WIDTH * 0.9112149532710281, IMAGE_HEIGHT * 0.8175182481751825, IMAGE_WIDTH * 0.9112149532710281, IMAGE_HEIGHT * 0.7737226277372263);
        GLOWRING.curveTo(IMAGE_WIDTH * 0.9112149532710281, IMAGE_HEIGHT * 0.41605839416058393, IMAGE_WIDTH * 0.7242990654205608, IMAGE_HEIGHT * 0.13138686131386862, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.13138686131386862);
        GLOWRING.curveTo(IMAGE_WIDTH * 0.27102803738317754, IMAGE_HEIGHT * 0.13138686131386862, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.41605839416058393, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.7737226277372263);
        GLOWRING.closePath();

        if (!ON) {
            final Point2D GLOWRING_OFF_START = new Point2D.Double(0, GLOWRING.getBounds2D().getMinY());
            final Point2D GLOWRING_OFF_STOP = new Point2D.Double(0, GLOWRING.getBounds2D().getMaxY());

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
            G2.drawImage(Shadow.INSTANCE.createDropShadow(GLOWRING, UTIL.setAlpha(GLOW_COLOR, 0.8f), GLOW_COLOR, true, null, null, 0, 1.0f, 10, 315, GLOW_COLOR), GLOWRING.getBounds().x, GLOWRING.getBounds().y, null);
            G2.translate(10, 10);

            final Point2D GLOWRING_HL_START = new Point2D.Double(0, GLOWRING.getBounds2D().getMinY());
            final Point2D GLOWRING_HL_STOP = new Point2D.Double(0, GLOWRING.getBounds2D().getMaxY());
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

        // Memoize parameters


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
        int IMAGE_HEIGHT = (int) (image.getHeight() * 0.6418604651);

        switch (getKnobType()) {
            case SMALL_STD_KNOB:
                final Ellipse2D CENTER_KNOB_FRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.46261683106422424, IMAGE_HEIGHT * 0.7153284549713135, IMAGE_WIDTH * 0.07943925261497498, IMAGE_HEIGHT * 0.1313868761062622);
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

                final Ellipse2D CENTER_KNOB_MAIN = new Ellipse2D.Double(IMAGE_WIDTH * 0.4719626307487488, IMAGE_HEIGHT * 0.7299270033836365, IMAGE_WIDTH * 0.060747623443603516, IMAGE_HEIGHT * 0.10218977928161621);
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

                final Ellipse2D CENTER_KNOB_INNERSHADOW = new Ellipse2D.Double(IMAGE_WIDTH * 0.4719626307487488, IMAGE_HEIGHT * 0.7299270033836365, IMAGE_WIDTH * 0.060747623443603516, IMAGE_HEIGHT * 0.10218977928161621);
                final Point2D CENTER_KNOB_INNERSHADOW_CENTER = new Point2D.Double((0.4930232558139535 * IMAGE_WIDTH), (0.7608695652173914 * IMAGE_HEIGHT));
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
                final RadialGradientPaint CENTER_KNOB_INNERSHADOW_GRADIENT = new RadialGradientPaint(CENTER_KNOB_INNERSHADOW_CENTER, (float) (0.03255813953488372 * IMAGE_WIDTH), CENTER_KNOB_INNERSHADOW_FRACTIONS, CENTER_KNOB_INNERSHADOW_COLORS);
                G2.setPaint(CENTER_KNOB_INNERSHADOW_GRADIENT);
                G2.fill(CENTER_KNOB_INNERSHADOW);
                break;

            case BIG_STD_KNOB:
                final Ellipse2D BIGCENTER_BACKGROUNDFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.44859811663627625, IMAGE_HEIGHT * 0.6934306621551514, IMAGE_WIDTH * 0.1074766218662262, IMAGE_HEIGHT * 0.17518246173858643);
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

                final Ellipse2D BIGCENTER_BACKGROUND = new Ellipse2D.Double(IMAGE_WIDTH * 0.44859811663627625, IMAGE_HEIGHT * 0.7007299065589905, IMAGE_WIDTH * 0.1074766218662262, IMAGE_HEIGHT * 0.1605839729309082);
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

                final Ellipse2D BIGCENTER_FOREGROUNDFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.46261683106422424, IMAGE_HEIGHT * 0.7153284549713135, IMAGE_WIDTH * 0.07943925261497498, IMAGE_HEIGHT * 0.1313868761062622);
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

                final Ellipse2D BIGCENTER_FOREGROUND = new Ellipse2D.Double(IMAGE_WIDTH * 0.46261683106422424, IMAGE_HEIGHT * 0.7226277589797974, IMAGE_WIDTH * 0.07943925261497498, IMAGE_HEIGHT * 0.11678832769393921);
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
                final Ellipse2D CHROMEKNOB_BACKFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.44859811663627625, IMAGE_HEIGHT * 0.6934306621551514, IMAGE_WIDTH * 0.1074766218662262, IMAGE_HEIGHT * 0.17518246173858643);
                final Point2D CHROMEKNOB_BACKFRAME_START = new Point2D.Double((0.4697674418604651 * IMAGE_WIDTH), (0.7028985507246377 * IMAGE_HEIGHT));
                final Point2D CHROMEKNOB_BACKFRAME_STOP = new Point2D.Double(((0.4697674418604651 + 0.056689037569133544) * IMAGE_WIDTH), ((0.7028985507246377 + 0.14134134935940434) * IMAGE_HEIGHT));
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

                final Ellipse2D CHROMEKNOB_BACK = new Ellipse2D.Double(IMAGE_WIDTH * 0.44859811663627625, IMAGE_HEIGHT * 0.7007299065589905, IMAGE_WIDTH * 0.1074766218662262, IMAGE_HEIGHT * 0.16788321733474731);
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

                final Ellipse2D CHROMEKNOB_FOREFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.47663551568984985, IMAGE_HEIGHT * 0.7445255517959595, IMAGE_WIDTH * 0.05140185356140137, IMAGE_HEIGHT * 0.0802919864654541);
                final Point2D CHROMEKNOB_FOREFRAME_START = new Point2D.Double((0.48372093023255813 * IMAGE_WIDTH), (0.7463768115942029 * IMAGE_HEIGHT));
                final Point2D CHROMEKNOB_FOREFRAME_STOP = new Point2D.Double(((0.48372093023255813 + 0.028609869479898676) * IMAGE_WIDTH), ((0.7463768115942029 + 0.0660827050587352) * IMAGE_HEIGHT));
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

                final Ellipse2D CHROMEKNOB_FORE = new Ellipse2D.Double(IMAGE_WIDTH * 0.47663551568984985, IMAGE_HEIGHT * 0.7445255517959595, IMAGE_WIDTH * 0.05140185356140137, IMAGE_HEIGHT * 0.07299268245697021);
                final Point2D CHROMEKNOB_FORE_START = new Point2D.Double((0.48372093023255813 * IMAGE_WIDTH), (0.7463768115942029 * IMAGE_HEIGHT));
                final Point2D CHROMEKNOB_FORE_STOP = new Point2D.Double(((0.48372093023255813 + 0.023408075029008005) * IMAGE_WIDTH), ((0.7463768115942029 + 0.05406766777532881) * IMAGE_HEIGHT));
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
                final Ellipse2D METALKNOB_FRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4579439163208008, IMAGE_HEIGHT * 0.7153284549713135, IMAGE_WIDTH * 0.08411216735839844, IMAGE_HEIGHT * 0.1313868761062622);
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

                final Ellipse2D METALKNOB_MAIN = new Ellipse2D.Double(IMAGE_WIDTH * 0.46261683106422424, IMAGE_HEIGHT * 0.7226277589797974, IMAGE_WIDTH * 0.0747663676738739, IMAGE_HEIGHT * 0.11678832769393921);
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
                METALKNOB_LOWERHL.moveTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.8321167883211679);
                METALKNOB_LOWERHL.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.8102189781021898, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.8029197080291971, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.8029197080291971);
                METALKNOB_LOWERHL.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.8029197080291971, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.8102189781021898, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.8321167883211679);
                METALKNOB_LOWERHL.curveTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.8394160583941606, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.8394160583941606, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.8394160583941606);
                METALKNOB_LOWERHL.curveTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.8394160583941606, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.8394160583941606, IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.8321167883211679);
                METALKNOB_LOWERHL.closePath();
                final Point2D METALKNOB_LOWERHL_CENTER = new Point2D.Double((0.49767441860465117 * IMAGE_WIDTH), (0.8333333333333334 * IMAGE_HEIGHT));
                final float[] METALKNOB_LOWERHL_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] METALKNOB_LOWERHL_COLORS = {
                    new Color(255, 255, 255, 153),
                    new Color(255, 255, 255, 0)
                };
                final RadialGradientPaint METALKNOB_LOWERHL_GRADIENT = new RadialGradientPaint(METALKNOB_LOWERHL_CENTER, (float) (0.03255813953488372 * IMAGE_WIDTH), METALKNOB_LOWERHL_FRACTIONS, METALKNOB_LOWERHL_COLORS);
                G2.setPaint(METALKNOB_LOWERHL_GRADIENT);
                G2.fill(METALKNOB_LOWERHL);

                final GeneralPath METALKNOB_UPPERHL = new GeneralPath();
                METALKNOB_UPPERHL.setWindingRule(Path2D.WIND_EVEN_ODD);
                METALKNOB_UPPERHL.moveTo(IMAGE_WIDTH * 0.5373831775700935, IMAGE_HEIGHT * 0.7518248175182481);
                METALKNOB_UPPERHL.curveTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.7299270072992701, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.7153284671532847, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.7153284671532847);
                METALKNOB_UPPERHL.curveTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.7153284671532847, IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.7299270072992701, IMAGE_WIDTH * 0.46261682242990654, IMAGE_HEIGHT * 0.7518248175182481);
                METALKNOB_UPPERHL.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.7591240875912408, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.7664233576642335, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.7664233576642335);
                METALKNOB_UPPERHL.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.7664233576642335, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.7591240875912408, IMAGE_WIDTH * 0.5373831775700935, IMAGE_HEIGHT * 0.7518248175182481);
                METALKNOB_UPPERHL.closePath();
                final Point2D METALKNOB_UPPERHL_CENTER = new Point2D.Double((0.4930232558139535 * IMAGE_WIDTH), (0.7101449275362319 * IMAGE_HEIGHT));
                final float[] METALKNOB_UPPERHL_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] METALKNOB_UPPERHL_COLORS = {
                    new Color(255, 255, 255, 191),
                    new Color(255, 255, 255, 0)
                };
                final RadialGradientPaint METALKNOB_UPPERHL_GRADIENT = new RadialGradientPaint(METALKNOB_UPPERHL_CENTER, (float) (0.04883720930232558 * IMAGE_WIDTH), METALKNOB_UPPERHL_FRACTIONS, METALKNOB_UPPERHL_COLORS);
                G2.setPaint(METALKNOB_UPPERHL_GRADIENT);
                G2.fill(METALKNOB_UPPERHL);

                final Ellipse2D METALKNOB_INNERFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4813084006309509, IMAGE_HEIGHT * 0.7518247961997986, IMAGE_WIDTH * 0.037383198738098145, IMAGE_HEIGHT * 0.0656934380531311);
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

                final Ellipse2D METALKNOB_INNERBACKGROUND = new Ellipse2D.Double(IMAGE_WIDTH * 0.4859813153743744, IMAGE_HEIGHT * 0.7591241002082825, IMAGE_WIDTH * 0.02803739905357361, IMAGE_HEIGHT * 0.051094889640808105);
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

        IMAGE_HEIGHT = image.getHeight();

        if (getPostsVisible()){
            final Ellipse2D MAX_POST_FRAME_RIGHT = new Ellipse2D.Double(IMAGE_WIDTH * 0.8317757248878479, IMAGE_HEIGHT * 0.514018714427948, IMAGE_WIDTH * 0.03738313913345337, IMAGE_HEIGHT * 0.03738313913345337);
            final Point2D MAX_POST_FRAME_RIGHT_START = new Point2D.Double(0, MAX_POST_FRAME_RIGHT.getBounds2D().getMinY());
            final Point2D MAX_POST_FRAME_RIGHT_STOP = new Point2D.Double(0, MAX_POST_FRAME_RIGHT.getBounds2D().getMaxY());
            final float[] E_MAX_POST_FRAME_RIGHT_FRACTIONS = {
                0.0f,
                0.46f,
                1.0f
            };
            final Color[] MAX_POST_FRAME_RIGHT_COLORS = {
                new Color(180, 180, 180, 255),
                new Color(63, 63, 63, 255),
                new Color(40, 40, 40, 255)
            };
            final LinearGradientPaint MAX_POST_FRAME_RIGHT_GRADIENT = new LinearGradientPaint(MAX_POST_FRAME_RIGHT_START, MAX_POST_FRAME_RIGHT_STOP, E_MAX_POST_FRAME_RIGHT_FRACTIONS, MAX_POST_FRAME_RIGHT_COLORS);
            G2.setPaint(MAX_POST_FRAME_RIGHT_GRADIENT);
            G2.fill(MAX_POST_FRAME_RIGHT);

            final Ellipse2D MAX_POST_MAIN_RIGHT = new Ellipse2D.Double(IMAGE_WIDTH * 0.836448609828949, IMAGE_HEIGHT * 0.5186915993690491, IMAGE_WIDTH * 0.02803736925125122, IMAGE_HEIGHT * 0.02803736925125122);
            final Point2D MAX_POST_MAIN_RIGHT_START = new Point2D.Double(0, MAX_POST_MAIN_RIGHT.getBounds2D().getMinY());
            final Point2D MAX_POST_MAIN_RIGHT_STOP = new Point2D.Double(0, MAX_POST_MAIN_RIGHT.getBounds2D().getMaxY());
            final float[] MAX_POST_MAIN_RIGHT_FRACTIONS = {
                0.0f,
                0.5f,
                1.0f
            };

            final Color[] MAX_POST_MAIN_RIGHT_COLORS;
            switch (getModel().getKnobStyle()) {
                case BLACK:
                    MAX_POST_MAIN_RIGHT_COLORS = new Color[]{
                        new Color(0xBFBFBF),
                        new Color(0x2B2A2F),
                        new Color(0x7D7E80)
                    };
                    break;

                case BRASS:
                    MAX_POST_MAIN_RIGHT_COLORS = new Color[]{
                        new Color(0xDFD0AE),
                        new Color(0x7A5E3E),
                        new Color(0xCFBE9D)
                    };
                    break;

                case SILVER:

                default:
                    MAX_POST_MAIN_RIGHT_COLORS = new Color[]{
                        new Color(0xD7D7D7),
                        new Color(0x747474),
                        new Color(0xD7D7D7)
                    };
                    break;
            }
            final LinearGradientPaint MAX_POST_MAIN_RIGHT_GRADIENT = new LinearGradientPaint(MAX_POST_MAIN_RIGHT_START, MAX_POST_MAIN_RIGHT_STOP, MAX_POST_MAIN_RIGHT_FRACTIONS, MAX_POST_MAIN_RIGHT_COLORS);
            G2.setPaint(MAX_POST_MAIN_RIGHT_GRADIENT);
            G2.fill(MAX_POST_MAIN_RIGHT);

            final Ellipse2D MAX_POST_INNERSHADOW_RIGHT = new Ellipse2D.Double(IMAGE_WIDTH * 0.836448609828949, IMAGE_HEIGHT * 0.5186915993690491, IMAGE_WIDTH * 0.02803736925125122, IMAGE_HEIGHT * 0.02803736925125122);
            final Point2D MAX_POST_INNERSHADOW_RIGHT_CENTER = new Point2D.Double((0.8504672897196262 * IMAGE_WIDTH), (0.5280373831775701 * IMAGE_HEIGHT));
            final float[] MAX_POST_INNERSHADOW_RIGHT_FRACTIONS = {
                0.0f,
                0.75f,
                0.76f,
                1.0f
            };
            final Color[] MAX_POST_INNERSHADOW_RIGHT_COLORS = {
                new Color(0, 0, 0, 0),
                new Color(0, 0, 0, 0),
                new Color(0, 0, 0, 1),
                new Color(0, 0, 0, 51)
            };
            final RadialGradientPaint MAX_POST_INNERSHADOW_RIGHT_GRADIENT = new RadialGradientPaint(MAX_POST_INNERSHADOW_RIGHT_CENTER, (float) (0.014018691588785047 * IMAGE_WIDTH), MAX_POST_INNERSHADOW_RIGHT_FRACTIONS, MAX_POST_INNERSHADOW_RIGHT_COLORS);
            G2.setPaint(MAX_POST_INNERSHADOW_RIGHT_GRADIENT);
            G2.fill(MAX_POST_INNERSHADOW_RIGHT);

            final Ellipse2D MIN_POST_FRAME_LEFT = new Ellipse2D.Double(IMAGE_WIDTH * 0.13084112107753754, IMAGE_HEIGHT * 0.514018714427948, IMAGE_WIDTH * 0.03738318383693695, IMAGE_HEIGHT * 0.03738313913345337);
            final Point2D MIN_POST_FRAME_LEFT_START = new Point2D.Double(0, MIN_POST_FRAME_LEFT.getBounds2D().getMinY());
            final Point2D MIN_POST_FRAME_LEFT_STOP = new Point2D.Double(0, MIN_POST_FRAME_LEFT.getBounds2D().getMaxY());
            final float[] E_MIN_POST_FRAME_LEFT_FRACTIONS = {
                0.0f,
                0.46f,
                1.0f
            };
            final Color[] MIN_POST_FRAME_LEFT_COLORS = {
                new Color(180, 180, 180, 255),
                new Color(63, 63, 63, 255),
                new Color(40, 40, 40, 255)
            };
            final LinearGradientPaint MIN_POST_FRAME_LEFT_GRADIENT = new LinearGradientPaint(MIN_POST_FRAME_LEFT_START, MIN_POST_FRAME_LEFT_STOP, E_MIN_POST_FRAME_LEFT_FRACTIONS, MIN_POST_FRAME_LEFT_COLORS);
            G2.setPaint(MIN_POST_FRAME_LEFT_GRADIENT);
            G2.fill(MIN_POST_FRAME_LEFT);

            final Ellipse2D MIN_POST_MAIN_LEFT = new Ellipse2D.Double(IMAGE_WIDTH * 0.1355140209197998, IMAGE_HEIGHT * 0.5186915993690491, IMAGE_WIDTH * 0.028037384152412415, IMAGE_HEIGHT * 0.02803736925125122);
            final Point2D MIN_POST_MAIN_LEFT_START = new Point2D.Double(0, MIN_POST_MAIN_LEFT.getBounds2D().getMinY());
            final Point2D MIN_POST_MAIN_LEFT_STOP = new Point2D.Double(0, MIN_POST_MAIN_LEFT.getBounds2D().getMaxY());
            final float[] MIN_POST_MAIN_LEFT_FRACTIONS = {
                0.0f,
                0.5f,
                1.0f
            };

            final Color[] MIN_POST_MAIN_LEFT_COLORS;
            switch (getModel().getKnobStyle()) {
                case BLACK:
                    MIN_POST_MAIN_LEFT_COLORS = new Color[]{
                        new Color(0xBFBFBF),
                        new Color(0x2B2A2F),
                        new Color(0x7D7E80)
                    };
                    break;

                case BRASS:
                    MIN_POST_MAIN_LEFT_COLORS = new Color[]{
                        new Color(0xDFD0AE),
                        new Color(0x7A5E3E),
                        new Color(0xCFBE9D)
                    };
                    break;

                case SILVER:

                default:
                    MIN_POST_MAIN_LEFT_COLORS = new Color[]{
                        new Color(0xD7D7D7),
                        new Color(0x747474),
                        new Color(0xD7D7D7)
                    };
                    break;
            }
            final LinearGradientPaint MIN_POST_MAIN_LEFT_GRADIENT = new LinearGradientPaint(MIN_POST_MAIN_LEFT_START, MIN_POST_MAIN_LEFT_STOP, MIN_POST_MAIN_LEFT_FRACTIONS, MIN_POST_MAIN_LEFT_COLORS);
            G2.setPaint(MIN_POST_MAIN_LEFT_GRADIENT);
            G2.fill(MIN_POST_MAIN_LEFT);

            final Ellipse2D MIN_POST_INNERSHADOW_LEFT = new Ellipse2D.Double(IMAGE_WIDTH * 0.1355140209197998, IMAGE_HEIGHT * 0.5186915993690491, IMAGE_WIDTH * 0.028037384152412415, IMAGE_HEIGHT * 0.02803736925125122);
            final Point2D MIN_POST_INNERSHADOW_LEFT_CENTER = new Point2D.Double((0.14953271028037382 * IMAGE_WIDTH), (0.5280373831775701 * IMAGE_HEIGHT));
            final float[] MIN_POST_INNERSHADOW_LEFT_FRACTIONS = {
                0.0f,
                0.75f,
                0.76f,
                1.0f
            };
            final Color[] MIN_POST_INNERSHADOW_LEFT_COLORS = {
                new Color(0, 0, 0, 0),
                new Color(0, 0, 0, 0),
                new Color(0, 0, 0, 1),
                new Color(0, 0, 0, 51)
            };
            final RadialGradientPaint MIN_POST_INNERSHADOW_LEFT_GRADIENT = new RadialGradientPaint(MIN_POST_INNERSHADOW_LEFT_CENTER, (float) (0.014018691588785047 * IMAGE_WIDTH), MIN_POST_INNERSHADOW_LEFT_FRACTIONS, MIN_POST_INNERSHADOW_LEFT_COLORS);
            G2.setPaint(MIN_POST_INNERSHADOW_LEFT_GRADIENT);
            G2.fill(MIN_POST_INNERSHADOW_LEFT);
        }

        G2.dispose();

        return image;
    }

    @Override
    protected BufferedImage create_DISABLED_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, (int) (0.641860465116279 * WIDTH), Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final GeneralPath GAUGE_BACKGROUND = new GeneralPath();
        GAUGE_BACKGROUND.setWindingRule(Path2D.WIND_EVEN_ODD);
        GAUGE_BACKGROUND.moveTo(IMAGE_WIDTH * 0.08372093023255814, IMAGE_HEIGHT * 0.7753623188405797);
        GAUGE_BACKGROUND.curveTo(IMAGE_WIDTH * 0.08372093023255814, IMAGE_HEIGHT * 0.42028985507246375, IMAGE_WIDTH * 0.26976744186046514, IMAGE_HEIGHT * 0.13043478260869565, IMAGE_WIDTH * 0.49767441860465117, IMAGE_HEIGHT * 0.13043478260869565);
        GAUGE_BACKGROUND.curveTo(IMAGE_WIDTH * 0.7255813953488373, IMAGE_HEIGHT * 0.13043478260869565, IMAGE_WIDTH * 0.9116279069767442, IMAGE_HEIGHT * 0.42028985507246375, IMAGE_WIDTH * 0.9116279069767442, IMAGE_HEIGHT * 0.7753623188405797);
        GAUGE_BACKGROUND.curveTo(IMAGE_WIDTH * 0.9116279069767442, IMAGE_HEIGHT * 0.8188405797101449, IMAGE_WIDTH * 0.9069767441860465, IMAGE_HEIGHT * 0.8695652173913043, IMAGE_WIDTH * 0.9069767441860465, IMAGE_HEIGHT * 0.8695652173913043);
        GAUGE_BACKGROUND.lineTo(IMAGE_WIDTH * 0.08837209302325581, IMAGE_HEIGHT * 0.8695652173913043);
        GAUGE_BACKGROUND.curveTo(IMAGE_WIDTH * 0.08837209302325581, IMAGE_HEIGHT * 0.8695652173913043, IMAGE_WIDTH * 0.08372093023255814, IMAGE_HEIGHT * 0.8115942028985508, IMAGE_WIDTH * 0.08372093023255814, IMAGE_HEIGHT * 0.7753623188405797);
        GAUGE_BACKGROUND.closePath();

        G2.setColor(new Color(102, 102, 102, 178));
        G2.fill(GAUGE_BACKGROUND);

        G2.dispose();

        return IMAGE;
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "Radial2Top";
    }
}
