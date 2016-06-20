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
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.mars_sim.msp.ui.steelseries.tools.ColorDef;
import org.mars_sim.msp.ui.steelseries.tools.GaugeType;
import org.mars_sim.msp.ui.steelseries.tools.LcdColor;
import org.mars_sim.msp.ui.steelseries.tools.Model;
import org.mars_sim.msp.ui.steelseries.tools.NumberSystem;
import org.mars_sim.msp.ui.steelseries.tools.Orientation;
import org.mars_sim.msp.ui.steelseries.tools.Section;


/**
 *
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public class RadialBargraph extends AbstractRadialBargraph {
    // <editor-fold defaultstate="collapsed" desc="Variable declarations">
    private static final int BASE = 10;
    private double ledTrackStartAngle;
    private double ledTrackAngleExtend;
    private BufferedImage bImage;
    private BufferedImage fImage;
    private BufferedImage glowImageOff;
    private BufferedImage glowImageOn;
    private BufferedImage lcdThresholdImage;
    private BufferedImage disabledImage;
    private ColorDef barGraphColor;
    private final Point2D CENTER;
    private Rectangle2D led;
    private final Point2D LED_CENTER;
    private Color[] ledColors;
    private final float[] LED_FRACTIONS;
    private RadialGradientPaint ledGradient;
    private java.util.HashMap<Section, RadialGradientPaint> sectionGradients;
    private java.util.HashMap<Section, Point2D> sectionAngles;
    private final Rectangle2D LCD = new Rectangle2D.Double();
    private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
    private TextLayout unitLayout;
    private final Rectangle2D UNIT_BOUNDARY = new Rectangle2D.Double();
    private TextLayout valueLayout;
    private final Rectangle2D VALUE_BOUNDARY = new Rectangle2D.Double();
    private TextLayout infoLayout;
    private final Rectangle2D INFO_BOUNDARY = new Rectangle2D.Double();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public RadialBargraph() {
        super();
        barGraphColor = ColorDef.RED;
        CENTER = new Point2D.Double();
        LED_CENTER = new Point2D.Double();
        LED_FRACTIONS = new float[]{
            0.0f,
            1.0f
        };
        sectionGradients = new java.util.HashMap<Section, RadialGradientPaint>(4);
        sectionAngles = new java.util.HashMap<Section, Point2D>(4);

        ledTrackStartAngle = getGaugeType().ORIGIN_CORRECTION - (0 * (getGaugeType().APEX_ANGLE / (getMaxValue() - getMinValue())));
        ledTrackAngleExtend = -(getMaxValue() - getMinValue()) * (getGaugeType().APEX_ANGLE / (getMaxValue() - getMinValue()));
        calcBargraphTrack();
        prepareBargraph(getInnerBounds().width);
        setLcdVisible(true);
    }

    public RadialBargraph(final Model MODEL) {
        super();
        setModel(MODEL);

        barGraphColor = ColorDef.RED;
        CENTER = new Point2D.Double();
        LED_CENTER = new Point2D.Double();
        LED_FRACTIONS = new float[]{
            0.0f,
            1.0f
        };
        sectionGradients = new java.util.HashMap<Section, RadialGradientPaint>(4);
        sectionAngles = new java.util.HashMap<Section, Point2D>(4);

        ledTrackStartAngle = getGaugeType().ORIGIN_CORRECTION - (0 * (getGaugeType().APEX_ANGLE / (getMaxValue() - getMinValue())));
        ledTrackAngleExtend = -(getMaxValue() - getMinValue()) * (getGaugeType().APEX_ANGLE / (getMaxValue() - getMinValue()));
        calcBargraphTrack();
        prepareBargraph(getGaugeBounds().width);
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
        ledTrackStartAngle = getGaugeType().ORIGIN_CORRECTION - (0 * (getGaugeType().APEX_ANGLE / (getMaxValue() - getMinValue())));
        ledTrackAngleExtend = -(getMaxValue() - getMinValue()) * (getGaugeType().APEX_ANGLE / (getMaxValue() - getMinValue()));

        calcBargraphTrack();
        prepareBargraph(getGaugeBounds().width);

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
            glowImageOff = create_GLOW_Image(GAUGE_WIDTH, getGlowColor(), false, getGaugeType(), false, getOrientation());
            if (glowImageOn != null) {
                glowImageOn.flush();
            }
            glowImageOn = create_GLOW_Image(GAUGE_WIDTH, getGlowColor(), true, getGaugeType(), false, getOrientation());
        } else {
            setGlowPulsating(false);
        }

        create_BARGRAPH_TRACK_Image(GAUGE_WIDTH, ledTrackStartAngle, ledTrackAngleExtend, getGaugeType().APEX_ANGLE, getGaugeType().BARGRAPH_OFFSET, bImage);

        create_TITLE_Image(GAUGE_WIDTH, getTitle(), getUnitString(), bImage);

        if (isLcdVisible()) {
            if (isLcdBackgroundVisible()) {
            createLcdImage(new Rectangle2D.Double(((getGaugeBounds().width - GAUGE_WIDTH * 0.48) / 2.0), (getGaugeBounds().height * 0.425), (GAUGE_WIDTH * 0.48), (GAUGE_WIDTH * 0.15)), getLcdColor(), getCustomLcdBackground(), bImage);
            }
            LCD.setRect(((getGaugeBounds().width - GAUGE_WIDTH * 0.48) / 2.0), (getGaugeBounds().height * 0.425), GAUGE_WIDTH * 0.48, GAUGE_WIDTH * 0.15);

            // Create the lcd threshold indicator image
            if (lcdThresholdImage != null) {
                lcdThresholdImage.flush();
            }
            lcdThresholdImage = create_LCD_THRESHOLD_Image((int) (LCD.getHeight() * 0.2045454545), (int) (LCD.getHeight() * 0.2045454545), getLcdColor().TEXT_COLOR);
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
                                                       false,
                                                       isTicklabelsVisible(),
                                                       false,
                                                       false,
                                                       getLabelNumberFormat(),
                                                       isTickmarkSectionsVisible(),
                                                       getBackgroundColor(),
                                                       getTickmarkColor(),
                                                       isTickmarkColorFromThemeEnabled(),
                                                       getTickmarkSections(),
                                                       isSectionTickmarksOnly(),
                                                       getSections(),
                                                       0.38f,
                                                       0.105f,
                                                       new Point2D.Double(getGaugeBounds().getCenterX() - getInsets().left, getGaugeBounds().getCenterX() - getInsets().top),
                                                       new Point2D.Double(0, 0),
                                                       Orientation.NORTH,
                                                       getModel().getTicklabelOrientation(),
                                                       getModel().isNiceScale(),
                                                       getModel().isLogScale(),
                                                       bImage);

        if (isForegroundVisible()) {
            FOREGROUND_FACTORY.createRadialForeground(GAUGE_WIDTH, false, getForegroundType(), fImage);
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

        // Translate coordinate system related to insets
        G2.translate(getFramelessOffset().getX(), getFramelessOffset().getY());

        // Draw combined background image
        G2.drawImage(bImage, 0, 0, null);

        // Draw an Arc2d object that will visualize the range of measured values
        if (isRangeOfMeasuredValuesVisible()) {
            G2.setPaint(getModel().getRangeOfMeasuredValuesPaint());
            if (getGaugeType() == GaugeType.TYPE3 || getGaugeType() == GaugeType.TYPE4) {
                final Area area = new Area(getModel().getRadialShapeOfMeasuredValues());
                area.subtract(new Area(LCD));
                G2.fill(area);
            } else {
                G2.fill(getModel().getRadialShapeOfMeasuredValues());
            }
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
            G2.setFont(getLcdUnitFont());
            if (isLcdTextVisible()) {
            final double UNIT_STRING_WIDTH;
            if (isLcdUnitStringVisible()) {
                unitLayout = new TextLayout(getLcdUnitString(), G2.getFont(), RENDER_CONTEXT);
                UNIT_BOUNDARY.setFrame(unitLayout.getBounds());
                G2.drawString(getLcdUnitString(), (int) (LCD.getX() + (LCD.getWidth() - UNIT_BOUNDARY.getWidth()) - LCD.getWidth() * 0.03), (int) (LCD.getY() + LCD.getHeight() * 0.76));
                UNIT_STRING_WIDTH = UNIT_BOUNDARY.getWidth();
            } else {
                UNIT_STRING_WIDTH = 0;
            }
            G2.setFont(getLcdValueFont());
            switch (getModel().getNumberSystem()) {
                case HEX:
                    valueLayout = new TextLayout(Integer.toHexString((int) getLcdValue()).toUpperCase(), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(Integer.toHexString((int) getLcdValue()).toUpperCase(), (int) (LCD.getX() + (LCD.getWidth() - UNIT_STRING_WIDTH - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09), (int) (LCD.getY() + LCD.getHeight() * 0.76));
                    break;

                case OCT:
                    valueLayout = new TextLayout(Integer.toOctalString((int) getLcdValue()), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(Integer.toOctalString((int) getLcdValue()), (int) (LCD.getX() + (LCD.getWidth() - UNIT_STRING_WIDTH - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09), (int) (LCD.getY() + LCD.getHeight() * 0.76));
                    break;

                case DEC:

                default:
                    valueLayout = new TextLayout(formatLcdValue(getLcdValue()), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(formatLcdValue(getLcdValue()), (int) (LCD.getX() + (LCD.getWidth() - UNIT_STRING_WIDTH - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09), (int) (LCD.getY() + LCD.getHeight() * 0.76));
                    break;
            }
            }
            // Draw lcd info string
            if (!getLcdInfoString().isEmpty()) {
                G2.setFont(getLcdInfoFont());
                infoLayout = new TextLayout(getLcdInfoString(), G2.getFont(), RENDER_CONTEXT);
                INFO_BOUNDARY.setFrame(infoLayout.getBounds());
                G2.drawString(getLcdInfoString(), LCD.getBounds().x + 5, LCD.getBounds().y + (int) INFO_BOUNDARY.getHeight() + 5);
            }

            // Draw lcd threshold indicator
            if (getLcdNumberSystem() == NumberSystem.DEC && isLcdThresholdVisible() && getLcdValue() >= getLcdThreshold()) {
                G2.drawImage(lcdThresholdImage, (int) (LCD.getX() + LCD.getHeight() * 0.0568181818), (int) (LCD.getY() + LCD.getHeight() - lcdThresholdImage.getHeight() - LCD.getHeight() * 0.0568181818), null);
            }
        }

        // Draw the active leds in dependence on the current value
        final AffineTransform OLD_TRANSFORM = G2.getTransform();

        final double ACTIVE_LED_ANGLE;
        if (!isLogScale()) {
            ACTIVE_LED_ANGLE = getAngleForValue(getValue());
        } else {
            ACTIVE_LED_ANGLE = Math.toDegrees(UTIL.logOfBase(BASE, getValue() - getMinValue()) * getLogAngleStep());
        }

        if (!getModel().isSingleLedBargraphEnabled()) {
            for (double angle = 0; Double.compare(angle, ACTIVE_LED_ANGLE) <= 0; angle += 5.0) {
                G2.rotate(Math.toRadians(angle + getGaugeType().BARGRAPH_OFFSET), CENTER.getX(), CENTER.getY());
                // If sections visible, color the bargraph with the given section colors
                G2.setPaint(ledGradient);
                if (isSectionsVisible()) {
                    // Use defined bargraphColor in areas where no section is defined
                    for (Section section : getSections()) {
                        if (Double.compare(angle, sectionAngles.get(section).getX()) >= 0 && angle < sectionAngles.get(section).getY()) {
                            G2.setPaint(sectionGradients.get(section));
                            break;
                        }
                    }
                } else {
                    G2.setPaint(ledGradient);
                }
                G2.fill(led);
                G2.setTransform(OLD_TRANSFORM);
            }
        } else {   // Draw only one led instead of all active leds
            final double ANGLE = Math.toRadians(((getValue() - getMinValue()) / (getMaxValue() - getMinValue())) * getGaugeType().APEX_ANGLE);
            G2.rotate(ANGLE, CENTER.getX(), CENTER.getY());
            if (isSectionsVisible()) {
                for (Section section : getSections()) {
                    if (Double.compare(ANGLE, sectionAngles.get(section).getX()) >= 0 && ANGLE < sectionAngles.get(section).getY()) {
                        G2.setPaint(sectionGradients.get(section));
                        break;
                    }
                }
            } else {
                G2.setPaint(ledGradient);
            }
            G2.fill(led);
            G2.setTransform(OLD_TRANSFORM);
        }

        // Draw peak value if enabled
        if (isPeakValueEnabled() && isPeakValueVisible()) {
            G2.rotate(Math.toRadians(((getPeakValue() - getMinValue()) / (getMaxValue() - getMinValue())) * getGaugeType().APEX_ANGLE + getGaugeType().BARGRAPH_OFFSET), CENTER.getX(), CENTER.getY());
            G2.fill(led);
            G2.setTransform(OLD_TRANSFORM);
        }

        // Draw the foreground
        if (isForegroundVisible()) {
            G2.drawImage(fImage, 0, 0, null);
        }

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
    public ColorDef getBarGraphColor() {
        return this.barGraphColor;
    }

    @Override
    public void setBarGraphColor(final ColorDef BARGRAPH_COLOR) {
        this.barGraphColor = BARGRAPH_COLOR;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    private void prepareBargraph(final int WIDTH) {
        led = new Rectangle2D.Double(WIDTH * 0.1168224299, WIDTH * 0.4859813084, WIDTH * 0.06074766355140187, WIDTH * 0.023364486);
        LED_CENTER.setLocation(led.getCenterX(), led.getCenterY());

        if (!getSections().isEmpty()) {
            sectionGradients.clear();
            sectionAngles.clear();
            for (Section section : getSections()) {
                sectionGradients.put(section, new RadialGradientPaint(LED_CENTER, (float) (0.030373831775700934 * WIDTH), LED_FRACTIONS, new Color[]{
                        section.getColor().brighter(),
                        section.getColor().darker()}));
                sectionAngles.put(section,
                		new Point2D.Double(getAngleForValue(section.getStart()),
                                           getAngleForValue(section.getStop())));
            }
        }

        if (barGraphColor != ColorDef.CUSTOM) {
            ledColors = new Color[]{
                barGraphColor.LIGHT,
                barGraphColor.DARK
            };
        } else {
            ledColors = new Color[]{
                getCustomBarGraphColorObject().LIGHT,
                getCustomBarGraphColorObject().DARK
            };
        }

        ledGradient = new RadialGradientPaint(LED_CENTER, (float) (0.030373831775700934 * WIDTH), LED_FRACTIONS, ledColors);
    }

    private void calcBargraphTrack() {
        ledTrackStartAngle = getGaugeType().ORIGIN_CORRECTION;
        ledTrackAngleExtend = -(getMaxValue() - getMinValue()) * (getGaugeType().APEX_ANGLE / (getMaxValue() - getMinValue()));
    }

    private double getAngleForValue(double value) {
        return ((value - getMinValue()) / (getMaxValue() - getMinValue())) * getGaugeType().APEX_ANGLE;
    }

    @Override
    public void setValue(double value) {
        if (isValueCoupled()) {
            setLcdValue(value);
        }
        super.setValue(value);
    }

    @Override
    public java.awt.Paint createCustomLcdBackgroundPaint(final Color[] LCD_COLORS) {
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
        return new Point2D.Double(getInnerBounds().getCenterX() + getInnerBounds().x, getInnerBounds().getCenterX() + getInnerBounds().y);
    }

    @Override
    public Rectangle2D getBounds2D() {
        return getInnerBounds();
    }

    @Override
    public Rectangle getLcdBounds() {
        return LCD.getBounds();
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "RadialBargraph " + getGaugeType();
    }
}
