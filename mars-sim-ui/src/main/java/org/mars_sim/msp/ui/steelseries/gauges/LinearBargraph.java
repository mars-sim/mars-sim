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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import org.mars_sim.msp.ui.steelseries.tools.ColorDef;
import org.mars_sim.msp.ui.steelseries.tools.LcdColor;
import org.mars_sim.msp.ui.steelseries.tools.Model;
import org.mars_sim.msp.ui.steelseries.tools.NumberSystem;
import org.mars_sim.msp.ui.steelseries.tools.Orientation;
import org.mars_sim.msp.ui.steelseries.tools.Section;


/**
 *
 * @author hansolo
 */
public class LinearBargraph extends AbstractLinearBargraph {
    // <editor-fold defaultstate="collapsed" desc="Variable declarations">
    private BufferedImage bImage;
    private BufferedImage fImage;
    private BufferedImage glowImageOff;
    private BufferedImage glowImageOn;
    private BufferedImage thresholdImage;
    private BufferedImage minMeasuredImage;
    private BufferedImage maxMeasuredImage;
    private BufferedImage lcdThresholdImage;
    private BufferedImage disabledImage;
    private ColorDef barGraphColor;
    private Rectangle2D led;
    private Point2D ledCenter;
    private Color[] ledColors;
    private final float[] LED_FRACTIONS;
    private RadialGradientPaint ledGradient;
    private RadialGradientPaint ledInactiveGradient;
    private final Color[] LED_INACTIVE_COLORS;
    private HashMap<Section, RadialGradientPaint> sectionGradients;
    private HashMap<Section, Point2D> sectionOffsets;
    private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
    private TextLayout unitLayout;
    private final Rectangle2D UNIT_BOUNDARY = new Rectangle2D.Double();
    private TextLayout valueLayout;
    private final Rectangle2D VALUE_BOUNDARY = new Rectangle2D.Double();
    private final Rectangle2D LCD = new Rectangle2D.Double();
    private TextLayout infoLayout;
    private final Rectangle2D INFO_BOUNDARY = new Rectangle2D.Double();
    private static final int BASE = 10;
    // The value background
    private final Rectangle2D VALUE_BACKGROUND_TRACK;
    private final Point2D VALUE_BACKGROUND_VERTICAL_START;
    private final Point2D VALUE_BACKGROUND_VERTICAL_STOP;
    private final float[] VALUE_BACKGROUND_TRACK_FRACTIONS;
    // Value track border
    private final Rectangle2D VALUE_LEFT_BORDER;
    private final Rectangle2D VALUE_RIGHT_BORDER;
    private final Point2D VALUE_BORDER_START;
    private final Point2D VALUE_BORDER_STOP;
    private final float[] VALUE_BORDER_FRACTIONS;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public LinearBargraph() {
        super();
        barGraphColor = getModel().getValueColor();
        LED_FRACTIONS = new float[]{
            0.0f,
            1.0f
        };
        LED_INACTIVE_COLORS = new Color[]{
            new Color(60, 60, 60, 255),
            new Color(50, 50, 50, 255)
        };
        sectionGradients = new java.util.HashMap<Section, RadialGradientPaint>(4);
        sectionOffsets = new java.util.HashMap<Section, Point2D>(4);

        // The value background
        VALUE_BACKGROUND_TRACK = new Rectangle2D.Double(0, 0, 10, 10);
        VALUE_BACKGROUND_VERTICAL_START = new Point2D.Double(0, 0);
        VALUE_BACKGROUND_VERTICAL_STOP = new Point2D.Double(0, 0);
        VALUE_BACKGROUND_TRACK_FRACTIONS = new float[]{
            0.0f,
            0.48f,
            0.49f,
            1.0f
        };
        // Value track border
        VALUE_LEFT_BORDER = new Rectangle2D.Double(0, 0, 10, 10);
        VALUE_RIGHT_BORDER = new Rectangle2D.Double(0, 0, 10, 10);
        VALUE_BORDER_START = new Point2D.Double(0, 0);
        VALUE_BORDER_STOP = new Point2D.Double(0, 0);
        VALUE_BORDER_FRACTIONS = new float[]{
            0.0f,
            0.48f,
            0.49f,
            1.0f
        };
        prepareBargraph(getInnerBounds().width, getInnerBounds().height);
        init(getInnerBounds().width, getInnerBounds().height);
    }

    public LinearBargraph(final Model MODEL) {
        super();
        setModel(MODEL);
        barGraphColor = MODEL.getValueColor();
        LED_FRACTIONS = new float[]{
            0.0f,
            1.0f
        };
        LED_INACTIVE_COLORS = new Color[]{
            new Color(60, 60, 60, 255),
            new Color(50, 50, 50, 255)
        };
        sectionGradients = new HashMap<Section, RadialGradientPaint>(4);
        sectionOffsets = new HashMap<Section, Point2D>(4);

        // The value background
        VALUE_BACKGROUND_TRACK = new Rectangle2D.Double(0, 0, 10, 10);
        VALUE_BACKGROUND_VERTICAL_START = new Point2D.Double(0, 0);
        VALUE_BACKGROUND_VERTICAL_STOP = new Point2D.Double(0, 0);
        VALUE_BACKGROUND_TRACK_FRACTIONS = new float[]{
            0.0f,
            0.48f,
            0.49f,
            1.0f
        };
        // Value track border
        VALUE_LEFT_BORDER = new Rectangle2D.Double(0, 0, 10, 10);
        VALUE_RIGHT_BORDER = new Rectangle2D.Double(0, 0, 10, 10);
        VALUE_BORDER_START = new Point2D.Double(0, 0);
        VALUE_BORDER_STOP = new Point2D.Double(0, 0);
        VALUE_BORDER_FRACTIONS = new float[]{
            0.0f,
            0.48f,
            0.49f,
            1.0f
        };
        prepareBargraph(getInnerBounds().width, getInnerBounds().height);
        init(getInnerBounds().width, getInnerBounds().height);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    @Override
    public final AbstractGauge init(final int WIDTH, final int HEIGHT) {
        if (WIDTH <= 1 || HEIGHT <= 1) {
            return this;
        }

        // Create Background Image
        if (bImage != null) {
            bImage.flush();
        }
        bImage = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);

        // Create Foreground Image
        if (fImage != null) {
            fImage.flush();
        }
        fImage = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);

//        if (inactiveLedImage != null)
//        {
//            inactiveLedImage.flush();
//        }
//        inactiveLedImage = create_BARGRAPH_LED_Image(WIDTH, HEIGHT, barGraphColor, LED_INACTIVE_COLORS);
//
//        if (activeLedImage != null)
//        {
//            activeLedImage.flush();
//        }
//        activeLedImage = create_BARGRAPH_LED_Image(WIDTH, HEIGHT, barGraphColor, null);

        prepareBargraph(WIDTH, HEIGHT);

        if (isLcdVisible()) {
            final float LCD_TEXT_HEIGHT_BASE;
            if (getOrientation() == Orientation.HORIZONTAL) {
                LCD_TEXT_HEIGHT_BASE = HEIGHT * 0.15f;
            } else {
                LCD_TEXT_HEIGHT_BASE = HEIGHT * 0.055f;
            }
            if (isDigitalFont()) {
                setLcdValueFont(LCD_DIGITAL_FONT.deriveFont(0.7f * LCD_TEXT_HEIGHT_BASE));
            } else {
                setLcdValueFont(LCD_STANDARD_FONT.deriveFont(0.625f * LCD_TEXT_HEIGHT_BASE));
            }

            if (isCustomLcdUnitFontEnabled()) {
                setLcdUnitFont(getCustomLcdUnitFont().deriveFont(0.25f * LCD_TEXT_HEIGHT_BASE));
            } else {
                setLcdUnitFont(LCD_STANDARD_FONT.deriveFont(0.25f * LCD_TEXT_HEIGHT_BASE));
            }

            setLcdInfoFont(getModel().getStandardInfoFont().deriveFont(0.15f * LCD_TEXT_HEIGHT_BASE));
        }
        if (isFrameVisible()) {
            create_FRAME_Image(WIDTH, HEIGHT, bImage);
        }

        if (isBackgroundVisible()) {
            create_BACKGROUND_Image(WIDTH, HEIGHT, bImage);
        }

        if (isGlowVisible()) {
            if (glowImageOff != null) {
                glowImageOff.flush();
            }
            glowImageOff = create_GLOW_Image(WIDTH, HEIGHT, getGlowColor(), false);
            if (glowImageOn != null) {
                glowImageOn.flush();
            }
            glowImageOn = create_GLOW_Image(WIDTH, HEIGHT, getGlowColor(), true);
        } else {
            setGlowPulsating(false);
        }

        if (isTrackVisible()) {
            create_TRACK_Image(WIDTH, HEIGHT, getMinValue(), getMaxValue(), getTrackStart(), getTrackSection(), getTrackStop(), getTrackStartColor(), getTrackSectionColor(), getTrackStopColor(), bImage);
        }

        TICKMARK_FACTORY.create_LINEAR_TICKMARKS_Image(WIDTH,
                                                       HEIGHT,
                                                       getModel().getNiceMinValue(),
                                                       getModel().getNiceMaxValue(),
                                                       getModel().getMaxNoOfMinorTicks(),
                                                       getModel().getMaxNoOfMajorTicks(),
                                                       getModel().getMinorTickSpacing(),
                                                       getModel().getMajorTickSpacing(),
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
                                                       new Point2D.Double(0, 0),
                                                       getOrientation(),
                                                       getModel().isNiceScale(),
                                                       getModel().isLogScale(),
                                                       bImage);

        if (isTitleVisible()) {
            create_TITLE_Image(WIDTH, HEIGHT, getModel().isUnitVisible(), bImage);
        }

        if (isLcdVisible()) {
            switch (getOrientation()) {
                case HORIZONTAL:
                    // Horizontal
                    if (isLcdBackgroundVisible()) {
                    create_LCD_Image(new Rectangle2D.Double((WIDTH * 0.695), (HEIGHT * 0.22), (WIDTH * 0.18), (HEIGHT * 0.15)), getLcdColor(), getCustomLcdBackground(), bImage);
                    }
                    LCD.setRect((WIDTH * 0.695), (HEIGHT * 0.22), WIDTH * 0.18, HEIGHT * 0.15);
                    break;

                case VERTICAL:
                    if (isLcdBackgroundVisible()) {
                    create_LCD_Image(new Rectangle2D.Double(((WIDTH - (WIDTH * 0.5714285714)) / 2.0), (HEIGHT * 0.875), (WIDTH * 0.5714285714), (HEIGHT * 0.055)), getLcdColor(), getCustomLcdBackground(), bImage);
                    }
                    LCD.setRect(((WIDTH - (WIDTH * 0.5714285714)) / 2.0), (HEIGHT * 0.875), WIDTH * 0.5714285714, HEIGHT * 0.055);
                    break;
            }
            // Create the lcd threshold indicator image
            if (lcdThresholdImage != null) {
                lcdThresholdImage.flush();
            }
            lcdThresholdImage = create_LCD_THRESHOLD_Image((int) (LCD.getHeight() * 0.2045454545), (int) (LCD.getHeight() * 0.2045454545), getLcdColor().TEXT_COLOR);
        }

        if (thresholdImage != null) {
            thresholdImage.flush();
        }
        thresholdImage = create_THRESHOLD_Image(WIDTH, HEIGHT);

        if (minMeasuredImage != null) {
            minMeasuredImage.flush();
        }
        minMeasuredImage = create_MEASURED_VALUE_Image(WIDTH, HEIGHT, new Color(0, 23, 252, 255));

        if (maxMeasuredImage != null) {
            maxMeasuredImage.flush();
        }
        maxMeasuredImage = create_MEASURED_VALUE_Image(WIDTH, HEIGHT, new Color(252, 29, 0, 255));

        if (isForegroundVisible()) {
            FOREGROUND_FACTORY.createLinearForeground(WIDTH, HEIGHT, false, fImage);
        }

        if (disabledImage != null) {
            disabledImage.flush();
        }
        disabledImage = create_DISABLED_Image(WIDTH, HEIGHT);

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

        if (!isFrameVisible()) {
            G2.translate(getInnerBounds().x - 17, getInnerBounds().y - 17);
        } else {
            G2.translate(getInnerBounds().x, getInnerBounds().y);
        }

        // Draw combined background image
        G2.drawImage(bImage, 0, 0, null);

        // Parameters neede for setting the indicators to the right location
        final double PIXEL_RANGE;
        final double RANGE;
        final double PIXEL_SCALE;

        if (getInnerBounds().width < getInnerBounds().height) {
            // Vertical
            PIXEL_RANGE = getInnerBounds().height * 0.8567961165048543 - getInnerBounds().height * 0.12864077669902912;
            RANGE = (getMaxValue() - getMinValue());
            PIXEL_SCALE = PIXEL_RANGE / RANGE;
        } else {
            // Horizontal
            PIXEL_RANGE = getInnerBounds().width * 0.8567961165048543 - getInnerBounds().width * 0.12864077669902912;
            RANGE = (getMaxValue() - getMinValue());
            PIXEL_SCALE = PIXEL_RANGE / RANGE;
        }

        // Draw threshold indicator
        if (isThresholdVisible()) {
            final double VALUE_POS;
            final AffineTransform OLD_TRANSFORM = G2.getTransform();
            if (getInnerBounds().width < getInnerBounds().height) {
                // Vertical orientation
                if (getMinValue() < 0) {
                    VALUE_POS = getInnerBounds().height * 0.8567961165048543 + getMinValue() * PIXEL_SCALE - getThreshold() * PIXEL_SCALE ;
                } else {
                    VALUE_POS = getInnerBounds().height * 0.8567961165048543 - getMinValue() * PIXEL_SCALE - getThreshold() * PIXEL_SCALE ;
                }
                G2.translate(getInnerBounds().width * 0.4357142857142857 - thresholdImage.getWidth() - 2 + getInnerBounds().x, VALUE_POS - thresholdImage.getHeight() / 2.0 + getInnerBounds().y);
            } else {
                // Horizontal orientation
                VALUE_POS = getThreshold() * PIXEL_SCALE - getMinValue() * PIXEL_SCALE;
                G2.translate(getInnerBounds().width * 0.14285714285714285 - thresholdImage.getWidth() / 2.0 + VALUE_POS + getInnerBounds().x, getInnerBounds().height * 0.5714285714 + 2 + getInnerBounds().y);
            }
            G2.drawImage(thresholdImage, 0, 0, null);

            G2.setTransform(OLD_TRANSFORM);
        }

        // Draw min measured value indicator
        if (isMinMeasuredValueVisible()) {
            final double VALUE_POS;
            final AffineTransform OLD_TRANSFORM = G2.getTransform();
            if (getInnerBounds().width < getInnerBounds().height) {
                // Vertical orientation
                if (getMinValue() < 0) {
                    VALUE_POS = getInnerBounds().height * 0.8567961165048543 + getMinValue() * PIXEL_SCALE - getMinMeasuredValue() * PIXEL_SCALE ;
                } else {
                    VALUE_POS = getInnerBounds().height * 0.8567961165048543 - getMinValue() * PIXEL_SCALE - getMinMeasuredValue() * PIXEL_SCALE ;
                }
                G2.translate(getInnerBounds().width * 0.37 - minMeasuredImage.getWidth() - 2 + getInnerBounds().x, VALUE_POS - minMeasuredImage.getHeight() / 2.0 + getInnerBounds().y);
            } else {
                // Horizontal orientation
                VALUE_POS = getMinMeasuredValue() * PIXEL_SCALE - getMinValue() * PIXEL_SCALE;
                G2.translate(getInnerBounds().width * 0.14285714285714285 - minMeasuredImage.getWidth() / 2.0 + VALUE_POS + getInnerBounds().x, getInnerBounds().height * 0.63 + 2 + getInnerBounds().y);
            }
            G2.drawImage(minMeasuredImage, 0, 0, null);

            G2.setTransform(OLD_TRANSFORM);
        }

        // Draw max measured value indicator
        if (isMaxMeasuredValueVisible()) {
            final double VALUE_POS;
            final AffineTransform OLD_TRANSFORM = G2.getTransform();
            if (getInnerBounds().width < getInnerBounds().height) {
                // Vertical orientation
                if (getMinValue() < 0) {
                    VALUE_POS = getInnerBounds().height * 0.8567961165048543 + getMinValue() * PIXEL_SCALE - getMaxMeasuredValue() * PIXEL_SCALE ;
                } else {
                    VALUE_POS = getInnerBounds().height * 0.8567961165048543 - getMinValue() * PIXEL_SCALE - getMaxMeasuredValue() * PIXEL_SCALE ;
                }
                G2.translate(getInnerBounds().width * 0.37 - maxMeasuredImage.getWidth() - 2 + getInnerBounds().x, VALUE_POS - maxMeasuredImage.getHeight() / 2.0 + getInnerBounds().y);
            } else {
                // Horizontal orientation
                VALUE_POS = getMaxMeasuredValue() * PIXEL_SCALE - getMinValue() * PIXEL_SCALE;
                G2.translate(getInnerBounds().width * 0.14285714285714285 - maxMeasuredImage.getWidth() / 2.0 + VALUE_POS + getInnerBounds().x, getInnerBounds().height * 0.63 + 2 + getInnerBounds().y);
            }
            G2.drawImage(maxMeasuredImage, 0, 0, null);

            G2.setTransform(OLD_TRANSFORM);
        }

        // Draw LED if enabled
        if (isLedVisible()) {
            G2.drawImage(getCurrentLedImage(), (int) (getInnerBounds().width * getLedPosition().getX()), (int) (getInnerBounds().height * getLedPosition().getY()), null);
        }

        // Draw user LED if enabled
        if (isUserLedVisible()) {
            G2.drawImage(getCurrentUserLedImage(), (int) (getInnerBounds().width * getUserLedPosition().getX()), (int) (getInnerBounds().height * getUserLedPosition().getY()), null);
        }

        // Draw LCD display
        if (isLcdVisible()) {
            if (getLcdColor() == LcdColor.CUSTOM) {
                G2.setColor(getCustomLcdForeground());
            } else {
                G2.setColor(getLcdColor().TEXT_COLOR);
            }
            G2.setFont(getLcdUnitFont());
            final double UNIT_STRING_WIDTH;
            if (isLcdTextVisible()) {
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

        // Draw the active leds in dependence on the current value and the peak value
        // drawValue(G2, getWidth(), getHeight());
        drawValue(G2, getInnerBounds().width, getInnerBounds().height);

        // Draw foreground
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

    private void drawValue(final Graphics2D G2, final int WIDTH, final int HEIGHT) {
        final boolean VERTICAL = WIDTH < HEIGHT ? true : false;
        final double TOP; // position of max value
        final double BOTTOM; // position of min value
        final double FULL_SIZE;

        // The value background
        // Orientation dependend definitions
        if (VERTICAL) {
            // Vertical orientation
            TOP = HEIGHT * 0.12864077669902912; // position of max value
            BOTTOM = HEIGHT * 0.8567961165048543; // position of min value
            FULL_SIZE = BOTTOM - TOP;

            VALUE_BACKGROUND_TRACK.setRect(WIDTH * 0.4357142857142857, TOP, WIDTH * 0.14285714285714285, FULL_SIZE);
            VALUE_BACKGROUND_VERTICAL_START.setLocation(0, VALUE_BACKGROUND_TRACK.getBounds2D().getMinY());
            VALUE_BACKGROUND_VERTICAL_STOP.setLocation(0, VALUE_BACKGROUND_TRACK.getBounds2D().getMaxY());
        } else {
            // Horizontal orientation
            TOP = WIDTH * 0.8567961165048543; // position of max value
            BOTTOM = WIDTH * 0.12864077669902912; // position of min value
            FULL_SIZE = TOP - BOTTOM;

            VALUE_BACKGROUND_TRACK.setRect(WIDTH * 0.14285714285714285, HEIGHT * 0.4357142857142857, FULL_SIZE, HEIGHT * 0.14285714285714285);
            VALUE_BACKGROUND_VERTICAL_START.setLocation(TOP, 0);
            VALUE_BACKGROUND_VERTICAL_STOP.setLocation(BOTTOM, 0);
        }

        final Color[] VALUE_BACKGROUND_TRACK_COLORS = {
            UTIL.setAlpha(getBackgroundColor().LABEL_COLOR, 0.0470588235f),
            UTIL.setAlpha(getBackgroundColor().LABEL_COLOR, 0.1450980392f),
            UTIL.setAlpha(getBackgroundColor().LABEL_COLOR, 0.1490196078f),
            UTIL.setAlpha(getBackgroundColor().LABEL_COLOR, 0.0470588235f)
        };
        final LinearGradientPaint VALUE_BACKGROUND_TRACK_GRADIENT = new LinearGradientPaint(VALUE_BACKGROUND_VERTICAL_START, VALUE_BACKGROUND_VERTICAL_STOP, VALUE_BACKGROUND_TRACK_FRACTIONS, VALUE_BACKGROUND_TRACK_COLORS);
        G2.setPaint(VALUE_BACKGROUND_TRACK_GRADIENT);
        G2.fill(VALUE_BACKGROUND_TRACK);

        // Value track border
        if (VERTICAL) {
            // Vertical orientation
            VALUE_LEFT_BORDER.setRect(WIDTH * 0.4357142857142857, TOP - (led.getHeight() / 2), WIDTH * 0.007142857142857143, FULL_SIZE + led.getHeight());
            VALUE_RIGHT_BORDER.setRect(WIDTH * 0.5714285714285714, TOP - (led.getHeight() / 2), WIDTH * 0.007142857142857143, FULL_SIZE + led.getHeight());
            VALUE_BORDER_START.setLocation(0, VALUE_LEFT_BORDER.getBounds2D().getMinY());
            VALUE_BORDER_STOP.setLocation(0, VALUE_LEFT_BORDER.getBounds2D().getMaxY());
        } else {
            // Horizontal orientation
            VALUE_LEFT_BORDER.setRect(WIDTH * 0.14285714285714285 - (led.getWidth() / 2), HEIGHT * 0.4357142857, FULL_SIZE + led.getWidth(), HEIGHT * 0.007142857142857143);
            VALUE_RIGHT_BORDER.setRect(WIDTH * 0.14285714285714285 - (led.getWidth() / 2), HEIGHT * 0.5714285714, FULL_SIZE + led.getWidth(), HEIGHT * 0.007142857142857143);
            VALUE_BORDER_START.setLocation(VALUE_LEFT_BORDER.getBounds2D().getMaxX(), 0);
            VALUE_BORDER_STOP.setLocation(VALUE_LEFT_BORDER.getBounds2D().getMinX(), 0);
        }
        final Color[] VALUE_BORDER_COLORS = {
            UTIL.setAlpha(getBackgroundColor().LABEL_COLOR, 0.2980392157f),
            UTIL.setAlpha(getBackgroundColor().LABEL_COLOR, 0.6862745098f),
            UTIL.setAlpha(getBackgroundColor().LABEL_COLOR, 0.6980392157f),
            UTIL.setAlpha(getBackgroundColor().LABEL_COLOR, 0.4f)
        };
        final LinearGradientPaint VALUE_BORDER_GRADIENT = new LinearGradientPaint(VALUE_BORDER_START, VALUE_BORDER_STOP, VALUE_BORDER_FRACTIONS, VALUE_BORDER_COLORS);
        G2.setPaint(VALUE_BORDER_GRADIENT);
        G2.fill(VALUE_LEFT_BORDER);
        G2.fill(VALUE_RIGHT_BORDER);

        if (VERTICAL) {
            // Vertical orientation

            // Draw the inactive leds
            final AffineTransform OLD_TRANSFORM = G2.getTransform();
            G2.setPaint(ledInactiveGradient);
            final double INACTIVE_LEDS = getCoordForValue(getMaxValue(), FULL_SIZE);
            for (double translateY = 0; Double.compare(translateY, INACTIVE_LEDS) <= 0; translateY += led.getHeight() + 1) {
                G2.translate(0, -translateY);
                G2.fill(led);
                G2.setTransform(OLD_TRANSFORM);
            }

            // Draw the active leds in dependence on the current value
            if (!isSectionsVisible()) {
                G2.setPaint(ledGradient);
            }

            final double ACTIVE_LEDS;
            if (!isLogScale()){
                ACTIVE_LEDS = getCoordForValue(getValue(), FULL_SIZE);
            } else {
                double SIZE_FACTOR = Math.abs(TOP - BOTTOM) / (UTIL.logOfBase(BASE, getMaxValue()));
                ACTIVE_LEDS = (SIZE_FACTOR * UTIL.logOfBase(BASE, getValue()));
            }

            if (!getModel().isSingleLedBargraphEnabled()) {
                for (double translateY = 0; Double.compare(translateY, ACTIVE_LEDS) <= 0; translateY += led.getHeight() + 1) {
                    // If sections visible, color the bargraph with the given section colors
                    G2.setPaint(ledGradient);
                    if (isSectionsVisible()) {
                        for (Section section : getSections()) {
                            if (translateY >= sectionOffsets.get(section).getX() && translateY < sectionOffsets.get(section).getY()) {
                                G2.setPaint(sectionGradients.get(section));
                                break;
                            }
                        }
                    }
                    G2.translate(0, -translateY);
                    G2.fill(led);
                    G2.setTransform(OLD_TRANSFORM);
                }
            } else { // Draw only single led in bargraph
                final double TRANSLATE_Y = -getCoordForValue(getValue(), FULL_SIZE);
                if (isSectionsVisible()) {
                    for (Section section : getSections()) {
                        if (TRANSLATE_Y >= sectionOffsets.get(section).getX() && TRANSLATE_Y < sectionOffsets.get(section).getY()) {
                            G2.setPaint(sectionGradients.get(section));
                        }
                    }
                }
                G2.translate(0, -TRANSLATE_Y);
                G2.fill(led);
                G2.setTransform(OLD_TRANSFORM);
            }

            // Draw peak value if enabled
            if (isPeakValueEnabled() && isPeakValueVisible()) {
                G2.translate(0, -getCoordForValue(getPeakValue(), FULL_SIZE));
                G2.fill(led);
                G2.setTransform(OLD_TRANSFORM);
            }
        } else {
            // Horizontal orientation

            // Draw the inactive leds
            final AffineTransform OLD_TRANSFORM = G2.getTransform();
            G2.setPaint(ledInactiveGradient);
            final double INACTIVE_LEDS = getCoordForValue(getMaxValue(), FULL_SIZE);
            for (double translateX = -(led.getWidth() / 2); Double.compare(translateX, INACTIVE_LEDS) <= 0; translateX += led.getWidth() + 1) {
                G2.translate(translateX, 0);
                G2.fill(led);
                G2.setTransform(OLD_TRANSFORM);
            }

            // Draw the active leds in dependence on the current value
            if (!isSectionsVisible()) {
                G2.setPaint(ledGradient);
            }

            final double ACTIVE_LEDS;
            if (!isLogScale()){
                ACTIVE_LEDS = getCoordForValue(getValue(), FULL_SIZE);
            } else {
                final double SIZE_FACTOR = FULL_SIZE / (UTIL.logOfBase(BASE, getMaxValue()));
                ACTIVE_LEDS = SIZE_FACTOR * UTIL.logOfBase(BASE, getValue());
            }

            if (!getModel().isSingleLedBargraphEnabled()) {
                for (double translateX = -(led.getWidth() / 2); Double.compare(translateX, ACTIVE_LEDS) <= 0; translateX += led.getWidth() + 1) {
                    // If sections visible, color the bargraph with the given section colors
                    G2.setPaint(ledGradient);
                    if (isSectionsVisible()) {
                        for (Section section : getSections()) {
                            if (translateX + (led.getWidth() / 2) >= sectionOffsets.get(section).getX() && translateX + (led.getWidth() / 2) < sectionOffsets.get(section).getY()) {
                                G2.setPaint(sectionGradients.get(section));
                            }
                        }
                    }

                    G2.translate(translateX, 0);
                    G2.fill(led);
                    G2.setTransform(OLD_TRANSFORM);
                }
            } else {   // Draw only one led
                final double TRANSLATE_X = getCoordForValue(getValue(), FULL_SIZE);
                if (isSectionsVisible()) {
                    for (Section section : getSections()) {
                        if (TRANSLATE_X + (led.getWidth() / 2) >= sectionOffsets.get(section).getX() && TRANSLATE_X + (led.getWidth() / 2) < sectionOffsets.get(section).getY()) {
                            G2.setPaint(sectionGradients.get(section));
                        }
                    }
                }
                G2.translate(TRANSLATE_X, 0);
                G2.fill(led);
                G2.setTransform(OLD_TRANSFORM);
            }

            // Draw peak value if enabled
            if (isPeakValueEnabled() && isPeakValueVisible()) {
                G2.translate(getCoordForValue(getPeakValue(), FULL_SIZE), 0);
                G2.fill(led);
                G2.setTransform(OLD_TRANSFORM);
            }
        }
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

    private void prepareBargraph(final int WIDTH, final int HEIGHT) {
        // Define colors of aktive leds
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

        if (getOrientation() == Orientation.VERTICAL) {
            // VERTICAL
            led = new Rectangle2D.Double(WIDTH * 0.45, HEIGHT * 0.8519417476, WIDTH * 0.1214285714, HEIGHT * 0.0121359223);
            ledCenter = new Point2D.Double(led.getCenterX(), led.getCenterY());

            if (!getSections().isEmpty()) {
                sectionGradients.clear();
                sectionOffsets.clear();

                // Vertical orientation
                double TOP = HEIGHT * 0.12864077669902912; // position of max value
                double BOTTOM = HEIGHT * 0.8567961165048543; // position of min value
                double FULL_SIZE = BOTTOM - TOP;

                for (Section section : getSections()) {
                    sectionGradients.put(section, new RadialGradientPaint(ledCenter, (float) (0.030373831775700934 * WIDTH), LED_FRACTIONS, new Color[]{
                            section.getColor().brighter(),
                            section.getColor().darker()}));
                    sectionOffsets.put(section,
                    		new Point2D.Double(
                    				getCoordForValue(section.getStart(), FULL_SIZE),
                    				getCoordForValue(section.getStop(), FULL_SIZE)));
                }
            }
            ledGradient = new RadialGradientPaint(ledCenter, (float) (0.030373831775700934 * WIDTH), LED_FRACTIONS, ledColors);
            ledInactiveGradient = new RadialGradientPaint(ledCenter, (float) (0.030373831775700934 * WIDTH), LED_FRACTIONS, LED_INACTIVE_COLORS);
        } else {
            // HORIZONTAL
            led = new Rectangle2D.Double(WIDTH * 0.14285714285714285, HEIGHT * 0.45, WIDTH * 0.0121359223, HEIGHT * 0.1214285714);
            ledCenter = new Point2D.Double(led.getCenterX(), led.getCenterY());

            if (!getSections().isEmpty()) {
                sectionGradients.clear();
                sectionOffsets.clear();

                // Horizontal orientation
                double TOP = WIDTH * 0.8567961165048543; // position of max value
                double BOTTOM = WIDTH * 0.12864077669902912; // position of min value
                double FULL_SIZE = TOP - BOTTOM;

                for (Section section : getSections()) {
                    sectionGradients.put(section, new RadialGradientPaint(ledCenter, (float) (0.030373831775700934 * WIDTH), LED_FRACTIONS, new Color[]{
                            section.getColor().brighter(),
                            section.getColor().darker()}));
                    sectionOffsets.put(section,
                    		new Point2D.Double(
                    				getCoordForValue(section.getStart(), FULL_SIZE),
                    				getCoordForValue(section.getStop(), FULL_SIZE)));
                }
            }
            ledGradient = new RadialGradientPaint(ledCenter, (float) (0.030373831775700934 * HEIGHT), LED_FRACTIONS, ledColors);
            ledInactiveGradient = new RadialGradientPaint(ledCenter, (float) (0.030373831775700934 * HEIGHT), LED_FRACTIONS, LED_INACTIVE_COLORS);
        }
    }

    public boolean isTitleVisible() {
        return getModel().isTitleVisible();
    }

    public void setTitleVisible(final boolean TITLE_VISIBLE) {
        getModel().setTitleVisible(TITLE_VISIBLE);
        repaint(getInnerBounds());
    }

    @Override
    public boolean isTickmarksVisible() {
        return getModel().isTickmarksVisible();
    }

    @Override
    public void setTickmarksVisible(final boolean TICKMARKS_VISIBLE) {
        getModel().setTickmarksVisible(TICKMARKS_VISIBLE);
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

    private double getCoordForValue(double value, final double FULL_SIZE)
    {
    	return ((value - getMinValue()) / (getMaxValue() - getMinValue())) * FULL_SIZE;
    }

    @Override
    protected Point2D getCenter() {
        return new Point2D.Double(bImage.getWidth() / 2.0 + getInnerBounds().x, bImage.getHeight() / 2.0 + getInnerBounds().y);
    }

    @Override
    protected Rectangle2D getBounds2D() {
        return new Rectangle2D.Double(bImage.getMinX(), bImage.getMinY(), bImage.getWidth(), bImage.getHeight());
    }

    @Override
    public Rectangle getLcdBounds() {
        return LCD.getBounds();
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "LinearBargraph";
    }
}
