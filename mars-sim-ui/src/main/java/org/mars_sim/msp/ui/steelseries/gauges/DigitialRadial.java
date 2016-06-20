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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

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
public class DigitialRadial extends AbstractLinear {
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
    // Value background
    private final Rectangle2D VALUE_BACKGROUND_TRACK;
    private final Point2D VALUE_BACKGROUND_START;
    private final Point2D VALUE_BACKGROUND_STOP;
    private final float[] VALUE_BACKGROUND_TRACK_FRACTIONS;
    // Value track border
    private final Rectangle2D VALUE_LEFT_BORDER;
    private final Rectangle2D VALUE_RIGHT_BORDER;
    private final Point2D VALUE_BORDER_START;
    private final Point2D VALUE_BORDER_STOP;
    private final float[] VALUE_BORDER_FRACTIONS;
    // The value itself
    private final Rectangle2D VALUE_BACKGROUND;
    private final Point2D VALUE_START;
    private final Point2D VALUE_STOP;
    private static final int BASE = 10;
    // The lighteffect on the value
    private final Rectangle2D VALUE_FOREGROUND;
    private final Point2D VALUE_FOREGROUND_START;
    private final Point2D VALUE_FOREGROUND_STOP;
    private final float[] VALUE_FOREGROUND_FRACTIONS;
    private final Color[] VALUE_FOREGROUND_COLORS;
    // Lcd related variables
    private final Rectangle2D LCD = new Rectangle2D.Double();
    private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
    private double unitStringWidth;
    private TextLayout unitLayout;
    private final Rectangle2D UNIT_BOUNDARY = new Rectangle2D.Double();
    private TextLayout valueLayout;
    private final Rectangle2D VALUE_BOUNDARY = new Rectangle2D.Double();
    private TextLayout infoLayout;
    private final Rectangle2D INFO_BOUNDARY = new Rectangle2D.Double();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public DigitialRadial() {
        super();
        VALUE_BACKGROUND_TRACK = new Rectangle2D.Double(0, 0, 10, 10);
        VALUE_BACKGROUND_START = new Point2D.Double(0, 0);
        VALUE_BACKGROUND_STOP = new Point2D.Double(0, 0);
        VALUE_BACKGROUND_TRACK_FRACTIONS = new float[]{
            0.0f,
            0.48f,
            0.49f,
            1.0f
        };
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
        VALUE_BACKGROUND = new Rectangle2D.Double(0, 0, 10, 10);
        VALUE_START = new Point2D.Double(0, 0);
        VALUE_STOP = new Point2D.Double(0, 0);
        VALUE_FOREGROUND = new Rectangle2D.Double(0, 0, 10, 10);
        VALUE_FOREGROUND_START = new Point2D.Double(0, 0);
        VALUE_FOREGROUND_STOP = new Point2D.Double(0, 0);
        VALUE_FOREGROUND_FRACTIONS = new float[]{
            0.0f,
            1.0f
        };
        VALUE_FOREGROUND_COLORS = new Color[]{
            new Color(1.0f, 1.0f, 1.0f, 0.7f),
            new Color(1.0f, 1.0f, 1.0f, 0.05f),
        };
        init(getInnerBounds().width, getInnerBounds().height);
    }

    public DigitialRadial(final Model MODEL) {
        super();
        setModel(MODEL);
        VALUE_BACKGROUND_TRACK = new Rectangle2D.Double(0, 0, 10, 10);
        VALUE_BACKGROUND_START = new Point2D.Double(0, 0);
        VALUE_BACKGROUND_STOP = new Point2D.Double(0, 0);
        VALUE_BACKGROUND_TRACK_FRACTIONS = new float[]{
            0.0f,
            0.48f,
            0.49f,
            1.0f
        };
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
        VALUE_BACKGROUND = new Rectangle2D.Double(0, 0, 10, 10);
        VALUE_START = new Point2D.Double(0, 0);
        VALUE_STOP = new Point2D.Double(0, 0);
        VALUE_FOREGROUND = new Rectangle2D.Double(0, 0, 10, 10);
        VALUE_FOREGROUND_START = new Point2D.Double(0, 0);
        VALUE_FOREGROUND_STOP = new Point2D.Double(0, 0);
        VALUE_FOREGROUND_FRACTIONS = new float[]{
            0.0f,
            1.0f
        };
        VALUE_FOREGROUND_COLORS = new Color[]{
            new Color(1.0f, 1.0f, 1.0f, 0.7f),
            new Color(1.0f, 1.0f, 1.0f, 0.05f)
        };
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
        final double PIXEL_RANGE;    // pixels between maxValue and minValue
        final double RANGE;          // maxValue - minValue
        final double PIXEL_SCALE;    // PIXEL_RANGE / RANGE

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
            // Show text only if isLcdTextVisible in Abstract gauge. This is needed for lcd blinking
            if (isLcdTextVisible()) {
            if (isLcdUnitStringVisible()) {
                unitLayout = new TextLayout(getLcdUnitString(), G2.getFont(), RENDER_CONTEXT);
                UNIT_BOUNDARY.setFrame(unitLayout.getBounds());
                G2.drawString(getLcdUnitString(), (float) (LCD.getX() + (LCD.getWidth() - UNIT_BOUNDARY.getWidth()) - LCD.getWidth() * 0.03), (float) (LCD.getY() + LCD.getHeight() * 0.76));
                unitStringWidth = UNIT_BOUNDARY.getWidth();
            } else {
                unitStringWidth = 0;
            }
            G2.setFont(getLcdValueFont());
            switch (getModel().getNumberSystem()) {
                case HEX:
                    valueLayout = new TextLayout(Integer.toHexString((int) getLcdValue()).toUpperCase(), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(Integer.toHexString((int) getLcdValue()).toUpperCase(), (float) (LCD.getX() + (LCD.getWidth() - unitStringWidth - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09), (float) (LCD.getY() + LCD.getHeight() * 0.76));
                    break;

                case OCT:
                    valueLayout = new TextLayout(Integer.toOctalString((int) getLcdValue()), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(Integer.toOctalString((int) getLcdValue()), (float) (LCD.getX() + (LCD.getWidth() - unitStringWidth - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09), (float) (LCD.getY() + LCD.getHeight() * 0.76));
                    break;

                case DEC:

                default:
                    valueLayout = new TextLayout(formatLcdValue(getLcdValue()), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(formatLcdValue(getLcdValue()), (float) (LCD.getX() + (LCD.getWidth() - unitStringWidth - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09), (float) (LCD.getY() + LCD.getHeight() * 0.76));
                    break;
            }
            }
            if (!getLcdInfoString().isEmpty()) {
                G2.setFont(getLcdInfoFont());
                infoLayout = new TextLayout(getLcdInfoString(), G2.getFont(), RENDER_CONTEXT);
                INFO_BOUNDARY.setFrame(infoLayout.getBounds());
                G2.drawString(getLcdInfoString(), LCD.getBounds().x + 5f, LCD.getBounds().y + (float) INFO_BOUNDARY.getHeight() + 5f);
            }

            // Draw lcd threshold indicator
            if (getLcdNumberSystem() == NumberSystem.DEC && isLcdThresholdVisible() && getLcdValue() >= getLcdThreshold()) {
                G2.drawImage(lcdThresholdImage, (int) (LCD.getX() + LCD.getHeight() * 0.0568181818), (int) (LCD.getY() + LCD.getHeight() - lcdThresholdImage.getHeight() - LCD.getHeight() * 0.0568181818), null);
            }
        }

        // Draw value
        drawValue(G2, getInnerBounds().width, getInnerBounds().height, PIXEL_SCALE);

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

    private void drawValue(final Graphics2D G2, final int WIDTH, final int HEIGHT, final double PIXEL_SCALE) {
        final boolean VERTICAL = WIDTH < HEIGHT ? true : false;
        final double MAX_POS_OF_BAR; // position of max value
        final double MIN_POS_OF_BAR; // position of min value
        final double FULL_SIZE;      // full size of bar
        final double VALUE_SIZE;     // size of the bar of the current value
        final double START_OF_VALUE; // position of the start value of the bar
        final double SIZE_FACTOR;

        // Orientation dependend definitions
        if (VERTICAL) {
            // Vertical orientation
            MAX_POS_OF_BAR = HEIGHT * 0.12864077669902912; // position of max value
            MIN_POS_OF_BAR = HEIGHT * 0.8567961165048543; // position of min value
            FULL_SIZE = MIN_POS_OF_BAR - MAX_POS_OF_BAR;
            if (isStartingFromZero()) {
                VALUE_SIZE = FULL_SIZE * Math.abs(getValue()) / (getMaxValue() - getMinValue());
                if (getValue() < 0) {
                    START_OF_VALUE = -getMinValue() * PIXEL_SCALE + MIN_POS_OF_BAR + getValue() * PIXEL_SCALE;
                } else {
                    START_OF_VALUE = -getMinValue() * PIXEL_SCALE + MIN_POS_OF_BAR;
                }

                //if (getValue() < 0) {
                //    START_OF_VALUE = MAX_POS_OF_BAR + FULL_SIZE - (FULL_SIZE * Math.abs(getMinValue()) / (getMaxValue() - getMinValue()));
                //} else {
                //    START_OF_VALUE = MAX_POS_OF_BAR + FULL_SIZE - VALUE_SIZE - (FULL_SIZE * Math.abs(getMinValue()) / (getMaxValue() - getMinValue()));
                //}
            } else if (isLogScale()) {
                SIZE_FACTOR = Math.abs(MAX_POS_OF_BAR - MIN_POS_OF_BAR) / (UTIL.logOfBase(BASE, getMaxValue()));
                VALUE_SIZE = (SIZE_FACTOR * UTIL.logOfBase(BASE, getValue()));
                START_OF_VALUE = MAX_POS_OF_BAR + FULL_SIZE - VALUE_SIZE;
            } else {
                VALUE_SIZE = FULL_SIZE * (getValue() - getMinValue()) / (getMaxValue() - getMinValue());
                START_OF_VALUE = MAX_POS_OF_BAR + FULL_SIZE - VALUE_SIZE;
            }

            VALUE_BACKGROUND_TRACK.setRect(WIDTH * 0.4357142857142857, MAX_POS_OF_BAR, WIDTH * 0.14285714285714285, FULL_SIZE);
            VALUE_BACKGROUND_START.setLocation(0, VALUE_BACKGROUND_TRACK.getBounds2D().getMinY());
            VALUE_BACKGROUND_STOP.setLocation(0, VALUE_BACKGROUND_TRACK.getBounds2D().getMaxY());
        } else {
            // Horizontal orientation
            MAX_POS_OF_BAR = WIDTH * 0.8567961165048543; // position of max value
            MIN_POS_OF_BAR = WIDTH * 0.14285714285714285; // position of min value
            FULL_SIZE = MAX_POS_OF_BAR - WIDTH * 0.12864077669902912;
            if (isStartingFromZero()) {
                VALUE_SIZE = FULL_SIZE * Math.abs(getValue()) / (getMaxValue() - getMinValue());
                if (getValue() < 0 ) {
                    START_OF_VALUE = -getMinValue() * PIXEL_SCALE + MIN_POS_OF_BAR + getValue() * PIXEL_SCALE;
                } else {
                    START_OF_VALUE = -getMinValue() * PIXEL_SCALE + MIN_POS_OF_BAR;
                }
                //if (getValue() < 0) {
                //    START_OF_VALUE = MIN_POS_OF_BAR + (FULL_SIZE - VALUE_SIZE) - (FULL_SIZE * Math.abs(getMinValue()) / (getMaxValue() - getMinValue()));
                //} else {
                //    START_OF_VALUE = MIN_POS_OF_BAR + FULL_SIZE - (FULL_SIZE * Math.abs(getMinValue()) / (getMaxValue() - getMinValue()));
                //}
            } else if (isLogScale()) {
                SIZE_FACTOR = FULL_SIZE / (UTIL.logOfBase(BASE, getMaxValue()));
                VALUE_SIZE = SIZE_FACTOR * UTIL.logOfBase(BASE, getValue());
                START_OF_VALUE = MIN_POS_OF_BAR;
            } else {
                VALUE_SIZE = FULL_SIZE * (getValue() - getMinValue()) / (getMaxValue() - getMinValue());
                START_OF_VALUE = MIN_POS_OF_BAR;
            }

            VALUE_BACKGROUND_TRACK.setRect(WIDTH * 0.14285714285714285, HEIGHT * 0.4357142857142857, FULL_SIZE, HEIGHT * 0.14285714285714285);
            VALUE_BACKGROUND_START.setLocation(MAX_POS_OF_BAR, 0);
            VALUE_BACKGROUND_STOP.setLocation(MIN_POS_OF_BAR, 0);
        }

        final Color[] VALUE_BACKGROUND_TRACK_COLORS = {
            UTIL.setAlpha(getBackgroundColor().LABEL_COLOR, 0.0470588235f),
            UTIL.setAlpha(getBackgroundColor().LABEL_COLOR, 0.1450980392f),
            UTIL.setAlpha(getBackgroundColor().LABEL_COLOR, 0.1490196078f),
            UTIL.setAlpha(getBackgroundColor().LABEL_COLOR, 0.0470588235f)
        };
        final LinearGradientPaint VALUE_BACKGROUND_TRACK_GRADIENT = new LinearGradientPaint(VALUE_BACKGROUND_START, VALUE_BACKGROUND_STOP, VALUE_BACKGROUND_TRACK_FRACTIONS, VALUE_BACKGROUND_TRACK_COLORS);
        G2.setPaint(VALUE_BACKGROUND_TRACK_GRADIENT);
        G2.fill(VALUE_BACKGROUND_TRACK);

        if (VERTICAL) {
            // Vertical orientation
            VALUE_LEFT_BORDER.setRect(WIDTH * 0.4357142857142857, MAX_POS_OF_BAR, WIDTH * 0.007142857142857143, FULL_SIZE);
            VALUE_RIGHT_BORDER.setRect(WIDTH * 0.5714285714285714, MAX_POS_OF_BAR, WIDTH * 0.007142857142857143, FULL_SIZE);
            VALUE_BORDER_START.setLocation(0, VALUE_LEFT_BORDER.getBounds2D().getMinY());
            VALUE_BORDER_STOP.setLocation(0, VALUE_LEFT_BORDER.getBounds2D().getMaxY());
        } else {
            // Horizontal orientation
            VALUE_LEFT_BORDER.setRect(WIDTH * 0.14285714285714285, HEIGHT * 0.4357142857, FULL_SIZE, HEIGHT * 0.007142857142857143);
            VALUE_RIGHT_BORDER.setRect(WIDTH * 0.14285714285714285, HEIGHT * 0.5714285714, FULL_SIZE, HEIGHT * 0.007142857142857143);
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
            VALUE_BACKGROUND.setRect(WIDTH * 0.45, START_OF_VALUE, WIDTH * 0.1142857143, VALUE_SIZE);
            VALUE_START.setLocation(VALUE_BACKGROUND.getBounds2D().getMinX(), 0);
            VALUE_STOP.setLocation(VALUE_BACKGROUND.getBounds2D().getMaxX(), 0);
        } else {
            // Horizontal orientation
            VALUE_BACKGROUND.setRect(START_OF_VALUE, HEIGHT * 0.45, VALUE_SIZE, HEIGHT * 0.1142857143);
            VALUE_START.setLocation(0, VALUE_BACKGROUND.getBounds2D().getMinY());
            VALUE_STOP.setLocation(0, VALUE_BACKGROUND.getBounds2D().getMaxY());
        }

        final float[] VALUE_BACKGROUND_FRACTIONS = {
            0.0f,
            0.99f,
            1.0f
        };
        Color[] valueBackgroundColors;

        if (getValueColor() != ColorDef.CUSTOM) {
            valueBackgroundColors = new Color[]{
                getValueColor().MEDIUM,
                getValueColor().LIGHT,
                getValueColor().LIGHT
            };
        } else {
            valueBackgroundColors = new Color[]{
                getCustomValueColorObject().MEDIUM,
                getCustomValueColorObject().LIGHT,
                getCustomValueColorObject().LIGHT
            };
        }

        if (isSectionsVisible()) {
            for (Section section : getSections()) {
                if (Double.compare(getValue(), section.getStart()) >= 0 && Double.compare(getValue(), section.getStop()) <= 0) {
                    valueBackgroundColors = new Color[]{
                        isTransparentSectionsEnabled() ? section.getTransparentColor() : section.getColor(),
                        isTransparentSectionsEnabled() ? section.getTransparentColor() : section.getColor(),
                        isTransparentSectionsEnabled() ? section.getTransparentColor() : section.getColor()
                    };
                    G2.setColor(section.getColor());
                    break;
                }
            }
        }
        final LinearGradientPaint VALUE_BACKGROUND_GRADIENT = new LinearGradientPaint(VALUE_START, VALUE_STOP, VALUE_BACKGROUND_FRACTIONS, valueBackgroundColors);
        G2.setPaint(VALUE_BACKGROUND_GRADIENT);
        G2.fill(VALUE_BACKGROUND);

        // The lighteffect on the value
        if (VERTICAL) {
            // Vertical orientation
            VALUE_FOREGROUND.setRect(WIDTH * 0.45, START_OF_VALUE, WIDTH * 0.05, VALUE_SIZE);
            VALUE_FOREGROUND_START.setLocation(VALUE_FOREGROUND.getBounds2D().getMinX(), 0);
            VALUE_FOREGROUND_STOP.setLocation(VALUE_FOREGROUND.getBounds2D().getMaxX(), 0);
        } else {
            // Horizontal orientation
            VALUE_FOREGROUND.setRect(START_OF_VALUE, HEIGHT * 0.45, VALUE_SIZE, HEIGHT * 0.05);
            VALUE_FOREGROUND_START.setLocation(0, VALUE_FOREGROUND.getBounds2D().getMinY());
            VALUE_FOREGROUND_STOP.setLocation(0, VALUE_FOREGROUND.getBounds2D().getMaxY());
        }
        final LinearGradientPaint VALUE_FOREGROUND_GRADIENT = new LinearGradientPaint(VALUE_FOREGROUND_START, VALUE_FOREGROUND_STOP, VALUE_FOREGROUND_FRACTIONS, VALUE_FOREGROUND_COLORS);
        G2.setPaint(VALUE_FOREGROUND_GRADIENT);
        if (VALUE_FOREGROUND.getWidth() > 0 && VALUE_FOREGROUND.getHeight() > 0) {
            G2.fill(VALUE_FOREGROUND);
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

    public boolean isTitleVisible() {
        return getModel().isTitleVisible();
    }

    public void setTitleVisible(final boolean TITLE_VISIBLE) {
        getModel().setTitleVisible(TITLE_VISIBLE);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    @Override
    public boolean isTickmarksVisible() {
        return getModel().isTickmarksVisible();
    }

    @Override
    public void setTickmarksVisible(final boolean TICKMARKS_VISIBLE) {
        getModel().setTickmarksVisible(TICKMARKS_VISIBLE);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    public boolean isUnitStringVisible() {
        return getModel().isUnitVisible();
    }

    public void setUnitStringVisible(final boolean UNIT_STRING_VISIBLE) {
        getModel().setUnitVisible(UNIT_STRING_VISIBLE);
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
        return "Linear";
    }
}
