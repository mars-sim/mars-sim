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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.mars_sim.msp.ui.steelseries.tools.GaugeType;
import org.mars_sim.msp.ui.steelseries.tools.LcdColor;
import org.mars_sim.msp.ui.steelseries.tools.Model;
import org.mars_sim.msp.ui.steelseries.tools.NumberSystem;
import org.mars_sim.msp.ui.steelseries.tools.Orientation;
import org.mars_sim.msp.ui.steelseries.tools.PostPosition;
import org.mars_sim.msp.ui.steelseries.tools.Section;


/**
 *
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public class Radial extends AbstractRadial {
    // <editor-fold defaultstate="collapsed" desc="Variable declarations">
    private static final int BASE = 10;
    private BufferedImage bImage;
    private BufferedImage fImage;
    private BufferedImage glowImageOff;
    private BufferedImage glowImageOn;
    private BufferedImage pointerImage;
    private BufferedImage pointerShadowImage;
    private BufferedImage thresholdImage;
    private BufferedImage minMeasuredImage;
    private BufferedImage maxMeasuredImage;
    private BufferedImage lcdThresholdImage;
    private BufferedImage disabledImage;
    private double angle;
    private final Point2D CENTER = new Point2D.Double();
    private final Rectangle2D LCD = new Rectangle2D.Double();
    private boolean section3DEffectVisible;
    private RadialGradientPaint section3DEffect;
    private boolean area3DEffectVisible;
    private RadialGradientPaint area3DEffect;
    private final Point2D TRACK_OFFSET = new Point2D.Double();
    private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
    private TextLayout unitLayout;
    private final Rectangle2D UNIT_BOUNDARY = new Rectangle2D.Double();
    private double unitStringWidth;
    private TextLayout valueLayout;
    private final Rectangle2D VALUE_BOUNDARY = new Rectangle2D.Double();
    private TextLayout infoLayout;
    private final Rectangle2D INFO_BOUNDARY = new Rectangle2D.Double();
    private Area areaOfMeasuredValues;
    private Area lcdArea;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public Radial() {
        super();
        angle = 0;
        section3DEffectVisible = false;
        area3DEffectVisible = false;
        init(getInnerBounds().width, getInnerBounds().height);
    }

    public Radial(final Model MODEL) {
        super();
        setModel(MODEL);
        angle = 0;
        section3DEffectVisible = false;
        area3DEffectVisible = false;
        areaOfMeasuredValues = new Area();
        lcdArea = new Area();
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

        CENTER.setLocation(getGaugeBounds().getCenterX() - getInsets().left, getGaugeBounds().getCenterX() - getInsets().top);

        if (isLcdVisible()) {
            if (isDigitalFont()) {
                setLcdValueFont(getModel().getDigitalBaseFont().deriveFont(0.7f * GAUGE_WIDTH * 0.15f));
            } else {
                setLcdValueFont(getModel().getStandardBaseFont().deriveFont(0.625f * GAUGE_WIDTH * 0.15f));
            }

            if (isCustomLcdUnitFontEnabled()) {
                setLcdUnitFont(getCustomLcdUnitFont().deriveFont(0.25f * GAUGE_WIDTH * 0.15f));
            } else {
                setLcdUnitFont(getModel().getStandardBaseFont().deriveFont(0.25f * GAUGE_WIDTH * 0.15f));
            }

            setLcdInfoFont(getModel().getStandardInfoFont().deriveFont(0.15f * GAUGE_WIDTH * 0.15f));
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

        if (getPostsVisible()) {
            createPostsImage(GAUGE_WIDTH, fImage, getGaugeType().POST_POSITIONS);
        } else {
            createPostsImage(GAUGE_WIDTH, fImage, new PostPosition[]{PostPosition.CENTER});
        }

        TRACK_OFFSET.setLocation(0, 0);

        if (isTrackVisible()) {
            create_TRACK_Image(GAUGE_WIDTH, getFreeAreaAngle(), getTickmarkOffset(), getMinValue(), getMaxValue(), getAngleStep(), getTrackStart(), getTrackSection(), getTrackStop(), getTrackStartColor(), getTrackSectionColor(), getTrackStopColor(), 0.38f, CENTER, getTickmarkDirection(), TRACK_OFFSET, bImage);
        }

        // Create areas if not empty
        if (!getAreas().isEmpty()) {
            // Create the sections 3d effect gradient overlay
            if (area3DEffectVisible) {
                area3DEffect = createArea3DEffectGradient(GAUGE_WIDTH, 0.38f);
            }
            createAreas(bImage);
        }

        // Create sections if not empty
        if (!getSections().isEmpty()) {
            // Create the sections 3d effect gradient overlay
            if (section3DEffectVisible) {
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

        if (isLcdVisible()) {
            if (isLcdBackgroundVisible()) {
            createLcdImage(new Rectangle2D.Double(((getGaugeBounds().width - GAUGE_WIDTH * getGaugeType().LCD_FACTORS.getX()) / 2.0),
                             (getGaugeBounds().height * getGaugeType().LCD_FACTORS.getY()),
                             (GAUGE_WIDTH * getGaugeType().LCD_FACTORS.getWidth()),
                             (GAUGE_WIDTH * getGaugeType().LCD_FACTORS.getHeight())),
                             getLcdColor(),
                             getCustomLcdBackground(),
                             bImage);
            }
            LCD.setRect(((getGaugeBounds().width - GAUGE_WIDTH * getGaugeType().LCD_FACTORS.getX()) / 2.0), (getGaugeBounds().height * getGaugeType().LCD_FACTORS.getY()), GAUGE_WIDTH * getGaugeType().LCD_FACTORS.getWidth(), GAUGE_WIDTH * getGaugeType().LCD_FACTORS.getHeight());
            lcdArea = new Area(LCD);

            // Create the lcd threshold indicator image
            if (lcdThresholdImage != null) {
                lcdThresholdImage.flush();
            }
            lcdThresholdImage = create_LCD_THRESHOLD_Image((int) (LCD.getHeight() * 0.2045454545), (int) (LCD.getHeight() * 0.2045454545), getLcdColor().TEXT_COLOR);
        }

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

        // Calc area of measured values
        if ((getGaugeType() == GaugeType.TYPE3 || getGaugeType() == GaugeType.TYPE4) && isLcdVisible()) {
            areaOfMeasuredValues = new Area(getModel().getRadialShapeOfMeasuredValues());
            areaOfMeasuredValues.subtract(lcdArea);
        } else {
            areaOfMeasuredValues = new Area(getModel().getRadialShapeOfMeasuredValues());
        }

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

        // Draw an Arc2d object that will visualize the range of measured values
        if (isRangeOfMeasuredValuesVisible()) {
            G2.setPaint(getModel().getRangeOfMeasuredValuesPaint());
            if ((getGaugeType() == GaugeType.TYPE3 || getGaugeType() == GaugeType.TYPE4) && isLcdVisible()) {
                final Area area = getModel().getRadialAreaOfMeasuredValues();
                area.subtract(lcdArea);
                G2.fill(area);
            } else {
                G2.fill(getModel().getRadialShapeOfMeasuredValues());
            }
        }

        // Highlight active area
        if (isHighlightArea()) {
            for(Section area : getAreas()) {
                if (area.contains(getValue())) {
                    G2.setColor(area.getHighlightColor());
                    if ((getGaugeType() == GaugeType.TYPE3 || getGaugeType() == GaugeType.TYPE4) && isLcdVisible()) {
                        final Area currentArea = new Area(area.getFilledArea());
                        currentArea.subtract(lcdArea);
                        G2.fill(currentArea);
                    } else {
                        G2.fill(area.getFilledArea());
                    }
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
                G2.rotate(getRotationOffset() + (getThreshold() - getMinValue()) * getAngleStep(), CENTER.getX(), CENTER.getY());
            } else {
                G2.rotate(getRotationOffset() + UTIL.logOfBase(BASE, getThreshold() - getMinValue()) * getLogAngleStep(), CENTER.getX(), CENTER.getY());
            }
            G2.drawImage(thresholdImage, (int) (getGaugeBounds().width * 0.4813084112), (int) (getGaugeBounds().height * 0.0841121495), null);
            G2.setTransform(OLD_TRANSFORM);
        }

        // Draw min measured value indicator
        if (isMinMeasuredValueVisible()) {
            if (!isLogScale()) {
                G2.rotate(getRotationOffset() + (getMinMeasuredValue() - getMinValue()) * getAngleStep(), CENTER.getX(), CENTER.getY());
            } else {
                G2.rotate(getRotationOffset() + UTIL.logOfBase(BASE, getMinMeasuredValue() - getMinValue()) * getLogAngleStep(), CENTER.getX(), CENTER.getY());
            }
            G2.drawImage(minMeasuredImage, (int) (getGaugeBounds().width * 0.4865), (int) (getGaugeBounds().height * 0.105), null);
            G2.setTransform(OLD_TRANSFORM);
        }

        // Draw max measured value indicator
        if (isMaxMeasuredValueVisible()) {
            if (!isLogScale()) {
                G2.rotate(getRotationOffset() + (getMaxMeasuredValue() - getMinValue()) * getAngleStep(), CENTER.getX(), CENTER.getY());
            } else {
                G2.rotate(getRotationOffset() + UTIL.logOfBase(BASE, getMaxMeasuredValue() - getMinValue()) * getLogAngleStep(), CENTER.getX(), CENTER.getY());
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

        // Draw LCD display
        if (isLcdVisible()) {
            if (getLcdColor() == LcdColor.CUSTOM) {
                G2.setColor(getCustomLcdForeground());
            } else {
                G2.setColor(getLcdColor().TEXT_COLOR);
            }
            // Draw LCD text only if isVisible() in AbstractGauge (this is needed for lcd blinking)
            if (isLcdTextVisible()) {
            G2.setFont(getLcdUnitFont());
            if (isLcdUnitStringVisible()) {
                unitLayout = new TextLayout(getLcdUnitString(), G2.getFont(), RENDER_CONTEXT);
                UNIT_BOUNDARY.setFrame(unitLayout.getBounds());
                G2.drawString(getLcdUnitString(), (int) (LCD.getX() + (LCD.getWidth() - UNIT_BOUNDARY.getWidth()) - LCD.getWidth() * 0.03), (int) (LCD.getY() + LCD.getHeight() * 0.76));
                unitStringWidth = UNIT_BOUNDARY.getWidth();
            } else {
                unitStringWidth = 0;
            }
            G2.setFont(getLcdValueFont());
            switch (getModel().getNumberSystem()) {
                case HEX:
                    valueLayout = new TextLayout(Integer.toHexString((int) getLcdValue()).toUpperCase(), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(Integer.toHexString((int) getLcdValue()).toUpperCase(), (float) (LCD.getX() + (LCD.getWidth() - unitStringWidth - VALUE_BOUNDARY.getWidth()) - LCD.getHeight() * 0.333333333), (float) (LCD.getY() + LCD.getHeight() * 0.76));
                    break;

                case OCT:
                    valueLayout = new TextLayout(Integer.toOctalString((int) getLcdValue()), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(Integer.toOctalString((int) getLcdValue()), (float) (LCD.getX() + (LCD.getWidth() - unitStringWidth - VALUE_BOUNDARY.getWidth()) - LCD.getHeight() * 0.333333333), (float) (LCD.getY() + LCD.getHeight() * 0.76));
                    break;

                case DEC:

                default:
                    int digitalFontNo_1Offset = 0;
                    if (isDigitalFont() && Double.toString(getLcdValue()).startsWith("1")) {
                        digitalFontNo_1Offset = (int) (LCD.getHeight() * 0.2166666667);
                    }
                    valueLayout = new TextLayout(formatLcdValue(getLcdValue()), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(formatLcdValue(getLcdValue()), (float) (LCD.getX() + (LCD.getWidth() - unitStringWidth - VALUE_BOUNDARY.getWidth()) - LCD.getHeight() * 0.333333333) - digitalFontNo_1Offset, (float) (LCD.getY() + LCD.getHeight() * 0.76));
                    break;
            }
            }
            // Draw lcd info string
            if (!getLcdInfoString().isEmpty()) {
                G2.setFont(getLcdInfoFont());
                infoLayout = new TextLayout(getLcdInfoString(), G2.getFont(), RENDER_CONTEXT);
                INFO_BOUNDARY.setFrame(infoLayout.getBounds());
                G2.drawString(getLcdInfoString(), (float) LCD.getBounds().x + 5f, LCD.getBounds().y + (float) INFO_BOUNDARY.getHeight() + 5f);
            }
            // Draw lcd threshold indicator
            if (getLcdNumberSystem() == NumberSystem.DEC && isLcdThresholdVisible() && getLcdValue() >= getLcdThreshold()) {
                G2.drawImage(lcdThresholdImage, (int) (LCD.getX() + LCD.getHeight() * 0.0568181818), (int) (LCD.getY() + LCD.getHeight() - lcdThresholdImage.getHeight() - LCD.getHeight() * 0.0568181818), null);
            }
        }

        // Draw the pointer
        if (!isLogScale()) {
            angle = getRotationOffset() + (getValue() - getMinValue()) * getAngleStep();
            G2.rotate(angle + (Math.cos(Math.toRadians(angle - getRotationOffset() - 91.5))), CENTER.getX(), CENTER.getY());
        } else {
            angle = getRotationOffset() + UTIL.logOfBase(BASE, getValue() - getMinValue()) * getLogAngleStep();
            G2.rotate(angle, CENTER.getX(), CENTER.getY() + 2);
        }
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
                G2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            } else {
                G2.drawImage(glowImageOff, 0, 0, null);
            }
        }

        // Draw disabled image if component isEnabled() == false
        if (!isEnabled()) {
            G2.drawImage(disabledImage, 0, 0, null);
        }

        G2.dispose();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    @Override
    public void setValue(double value) {
        if (isValueCoupled()) {
            setLcdValue(value);
        }
        super.setValue(value);
    }

    /**
     * Returns true if the 3d effect gradient overlay for the sections is visible
     * @return true if the 3d effect gradient overlay for the sections is visible
     */
    public boolean isSection3DEffectVisible() {
        return this.section3DEffectVisible;
    }

    /**
     * Enables / disables the visibility of the 3d effect gradient overlay for the sections
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

    // <editor-fold defaultstate="collapsed" desc="Areas related">
    private void createAreas(final BufferedImage IMAGE) {
        if (bImage != null) {
            final double ANGLE_STEP;
            if (!isLogScale()) {
                ANGLE_STEP = Math.toDegrees(getGaugeType().ANGLE_RANGE) / (getMaxValue() - getMinValue());
            } else {
                ANGLE_STEP = Math.toDegrees(getGaugeType().ANGLE_RANGE) / UTIL.logOfBase(BASE, (getMaxValue() - getMinValue()));
            }

            if (bImage != null && !getAreas().isEmpty()) {
                final double OUTER_RADIUS = bImage.getWidth() * 0.38f;
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
                        area.setFilledArea(new Arc2D.Double(AREA_FRAME, getGaugeType().ORIGIN_CORRECTION - (area.getStart() * ANGLE_STEP) + (getMinValue() * ANGLE_STEP), -(area.getStop() - area.getStart()) * ANGLE_STEP, Arc2D.PIE));
                    } else {
                        area.setFilledArea(new Arc2D.Double(AREA_FRAME, getGaugeType().ORIGIN_CORRECTION - (UTIL.logOfBase(BASE, area.getStart()) * ANGLE_STEP) + (UTIL.logOfBase(BASE, getMinValue()) * ANGLE_STEP), -UTIL.logOfBase(BASE, area.getStop() - area.getStart()) * ANGLE_STEP, Arc2D.PIE));
                    }
                }
            }

            // Draw the areas
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
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Sections related">
    private void createSections(final BufferedImage IMAGE) {
        if (bImage != null) {
            final double ANGLE_STEP;
            if (!isLogScale()) {
                ANGLE_STEP = getGaugeType().APEX_ANGLE / (getMaxValue() - getMinValue());
            } else {
                ANGLE_STEP = getGaugeType().APEX_ANGLE / UTIL.logOfBase(BASE, getMaxValue() - getMinValue());
            }

            final double OUTER_RADIUS = bImage.getWidth() * 0.38f;
            final double INNER_RADIUS = isExpandedSectionsEnabled() ? OUTER_RADIUS - bImage.getWidth() * 0.12f : OUTER_RADIUS - bImage.getWidth() * 0.04f;
            final double FREE_AREA_OUTER_RADIUS = bImage.getWidth() / 2.0 - OUTER_RADIUS;
            final double FREE_AREA_INNER_RADIUS = bImage.getWidth() / 2.0 - INNER_RADIUS;
            final Area INNER = new Area(new Ellipse2D.Double(bImage.getMinX() + FREE_AREA_INNER_RADIUS, bImage.getMinY() + FREE_AREA_INNER_RADIUS, 2 * INNER_RADIUS, 2 * INNER_RADIUS));

            for (Section section : getSections()) {
                final double ANGLE_START;
                final double ANGLE_EXTEND;

                if (!isLogScale()) {
                    ANGLE_START = getGaugeType().ORIGIN_CORRECTION - (section.getStart() * ANGLE_STEP) + (getMinValue() * ANGLE_STEP);
                    ANGLE_EXTEND = -(section.getStop() - section.getStart()) * ANGLE_STEP;
                } else {
                    ANGLE_START = getGaugeType().ORIGIN_CORRECTION - (UTIL.logOfBase(BASE, section.getStart())) * ANGLE_STEP + (UTIL.logOfBase(BASE, getMinValue())) * ANGLE_STEP;
                    ANGLE_EXTEND = -UTIL.logOfBase(BASE, section.getStop() - section.getStart()) * ANGLE_STEP;
                }

                final Arc2D OUTER_ARC = new Arc2D.Double(Arc2D.PIE);
                OUTER_ARC.setFrame(bImage.getMinX() + FREE_AREA_OUTER_RADIUS, bImage.getMinY() + FREE_AREA_OUTER_RADIUS, 2 * OUTER_RADIUS, 2 * OUTER_RADIUS);
                OUTER_ARC.setAngleStart(ANGLE_START);
                OUTER_ARC.setAngleExtent(ANGLE_EXTEND);
                final Area SECTION = new Area(OUTER_ARC);

                SECTION.subtract(INNER);

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

    @Override
    public String toString() {
        return "Radial " + getGaugeType();
    }
}
