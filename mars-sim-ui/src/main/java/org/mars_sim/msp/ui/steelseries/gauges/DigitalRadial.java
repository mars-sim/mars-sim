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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.mars_sim.msp.ui.steelseries.tools.LcdColor;
import org.mars_sim.msp.ui.steelseries.tools.LedColor;
import org.mars_sim.msp.ui.steelseries.tools.NumberSystem;

import java.awt.Transparency;


public class DigitalRadial extends AbstractRadial {
    // <editor-fold defaultstate="collapsed" desc="Variable declarations">
    private int noOfActiveLeds = 0;
    // One image to reduce memory consumption
    private BufferedImage bImage;
    private BufferedImage fImage;
    private BufferedImage lcdThresholdImage;
    private BufferedImage disabledImage;
    private BufferedImage ledGreenOff = UTIL.createImage(24, 24, Transparency.TRANSLUCENT);
    private BufferedImage ledYellowOff = UTIL.createImage(24, 24, Transparency.TRANSLUCENT);
    private BufferedImage ledRedOff = UTIL.createImage(24, 24, Transparency.TRANSLUCENT);
    private BufferedImage ledGreenOn = UTIL.createImage(24, 24, Transparency.TRANSLUCENT);
    private BufferedImage ledYellowOn = UTIL.createImage(24, 24, Transparency.TRANSLUCENT);
    private BufferedImage ledRedOn = UTIL.createImage(24, 24, Transparency.TRANSLUCENT);
    private Color valueColor = new Color(255, 0, 0, 255);
    private final Rectangle2D LCD = new Rectangle2D.Double();
    private java.awt.Point[] ledPosition;
    private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
    private TextLayout unitLayout;
    private final Rectangle2D UNIT_BOUNDARY = new Rectangle2D.Double();
    private TextLayout valueLayout;
    private final Rectangle2D VALUE_BOUNDARY = new Rectangle2D.Double();
    private TextLayout infoLayout;
    private final Rectangle2D INFO_BOUNDARY = new Rectangle2D.Double();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public DigitalRadial() {
        super();
        setUnitString("");
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    @Override
    public final AbstractGauge init(final int WIDTH, final int HEIGHT) {
        if (WIDTH <= 1 || HEIGHT <= 1) {
            return this;
        }
        if (isLcdVisible()) {
            if (isDigitalFont()) {
                setLcdValueFont(getModel().getDigitalBaseFont().deriveFont(0.7f * WIDTH * 0.15f));
            } else {
                setLcdValueFont(getModel().getStandardBaseFont().deriveFont(0.625f * WIDTH * 0.15f));
            }

            if (isCustomLcdUnitFontEnabled()) {
                setLcdUnitFont(getCustomLcdUnitFont().deriveFont(0.25f * WIDTH * 0.15f));
            } else {
                setLcdUnitFont(getModel().getStandardBaseFont().deriveFont(0.25f * WIDTH * 0.15f));
            }

            setLcdInfoFont(getModel().getStandardInfoFont().deriveFont(0.15f * WIDTH * 0.15f));
        }
        // Create Background Image
        if (bImage != null) {
            bImage.flush();
        }
        bImage = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);

        // Create Foreground Image
        if (fImage != null) {
            fImage.flush();
        }
        fImage = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);

        if (isFrameVisible()) {
            switch (getFrameType()) {
                case ROUND:
                    FRAME_FACTORY.createRadialFrame(WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameEffect(), bImage);
                    break;
                case SQUARE:
                    FRAME_FACTORY.createLinearFrame(WIDTH, WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameEffect(), bImage);
                    break;
                default:
                    FRAME_FACTORY.createRadialFrame(WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameEffect(), bImage);
                    break;
            }
        }

        if (isBackgroundVisible()) {
            create_BACKGROUND_Image(WIDTH, "", "", bImage);
        }

        if (isForegroundVisible()) {
            switch (getFrameType()) {
                case SQUARE:
                    FOREGROUND_FACTORY.createLinearForeground(WIDTH, WIDTH, false, bImage);
                    break;

                case ROUND:

                default:
                    FOREGROUND_FACTORY.createRadialForeground(WIDTH, false, getForegroundType(), fImage);
                    break;
            }
        }

        if (isLcdVisible()) {
            createLcdImage(new Rectangle2D.Double(((getGaugeBounds().width - WIDTH * 0.48) / 2.0), (getGaugeBounds().height * 0.425), (WIDTH * 0.48), (WIDTH * 0.15)), getLcdColor(), getCustomLcdBackground(), bImage);
            LCD.setRect(((getGaugeBounds().width - WIDTH * 0.4) / 2.0), (getGaugeBounds().height * 0.55), WIDTH * 0.48, WIDTH * 0.15);

            // Create the lcd threshold indicator image
            if (lcdThresholdImage != null) {
                lcdThresholdImage.flush();
            }
            lcdThresholdImage = create_LCD_THRESHOLD_Image((int) (LCD.getHeight() * 0.2045454545), (int) (LCD.getHeight() * 0.2045454545), getLcdColor().TEXT_COLOR);
        }

        if (disabledImage != null) {
            disabledImage.flush();
        }
        disabledImage = create_DISABLED_Image(WIDTH);

        ledPosition = new java.awt.Point[]{
            // LED 1
            new java.awt.Point((int) (WIDTH * 0.186915887850467), (int) (WIDTH * 0.649532710280374)),
            // LED 2
            new java.awt.Point((int) (WIDTH * 0.116822429906542), (int) (WIDTH * 0.546728971962617)),
            // LED 3
            new java.awt.Point((int) (WIDTH * 0.088785046728972), (int) (WIDTH * 0.41588785046729)),
            // LED 4
            new java.awt.Point((int) (WIDTH * 0.116822429906542), (int) (WIDTH * 0.285046728971963)),
            // LED 5
            new java.awt.Point((int) (WIDTH * 0.177570093457944), (int) (WIDTH * 0.182242990654206)),
            // LED 6
            new java.awt.Point((int) (WIDTH * 0.280373831775701), (int) (WIDTH * 0.117222429906542)),
            // LED 7
            new java.awt.Point((int) (WIDTH * 0.411214953271028), (int) (WIDTH * 0.0794392523364486)),
            // LED 8
            new java.awt.Point((int) (WIDTH * 0.542056074766355), (int) (WIDTH * 0.117222429906542)),
            // LED 9
            new java.awt.Point((int) (WIDTH * 0.649532710280374), (int) (WIDTH * 0.182242990654206)),
            // LED 10
            new java.awt.Point((int) (WIDTH * 0.719626168224299), (int) (WIDTH * 0.285046728971963)),
            // LED 11
            new java.awt.Point((int) (WIDTH * 0.738317757009346), (int) (WIDTH * 0.41588785046729)),
            // LED 12
            new java.awt.Point((int) (WIDTH * 0.710280373831776), (int) (WIDTH * 0.546728971962617)),
            // LED 13
            new java.awt.Point((int) (WIDTH * 0.64018691588785), (int) (WIDTH * 0.649532710280374))
        };
        ledGreenOff.flush();
        ledGreenOff = create_LED_OFF_Image(WIDTH, LedColor.GREEN);
        ledYellowOff.flush();
        ledYellowOff = create_LED_OFF_Image(WIDTH, LedColor.YELLOW);
        ledRedOff.flush();
        ledRedOff = create_LED_OFF_Image(WIDTH, LedColor.RED);
        ledGreenOn.flush();
        ledGreenOn = create_LED_ON_Image(WIDTH, LedColor.GREEN);
        ledYellowOn.flush();
        ledYellowOn = create_LED_ON_Image(WIDTH, LedColor.YELLOW);
        ledRedOn.flush();
        ledRedOn = create_LED_ON_Image(WIDTH, LedColor.RED);

        return this;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Visualization">
    @Override
    protected void paintComponent(Graphics g) {
        if (!isInitialized()) {
            return;
        }

        super.paintComponent(g);
        final Graphics2D G2 = (Graphics2D) g.create();

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        G2.translate(getInnerBounds().x, getInnerBounds().y);

        // Draw combined background image
        G2.drawImage(bImage, 0, 0, null);

        for (int i = 0; i < 13; i++) {
            if (i < 7) {
                if (i < noOfActiveLeds) {
                    G2.drawImage(ledGreenOn, ledPosition[i].x, ledPosition[i].y, null);
                } else {
                    G2.drawImage(ledGreenOff, ledPosition[i].x, ledPosition[i].y, null);
                }
            }

            if (i >= 7 && i < 12) {
                if (i < noOfActiveLeds) {
                    G2.drawImage(ledYellowOn, ledPosition[i].x, ledPosition[i].y, null);
                } else {
                    G2.drawImage(ledYellowOff, ledPosition[i].x, ledPosition[i].y, null);
                }
            }

            if (i == 12) {
                if (i < noOfActiveLeds) {
                    G2.drawImage(ledRedOn, ledPosition[i].x, ledPosition[i].y, null);
                } else {
                    G2.drawImage(ledRedOff, ledPosition[i].x, ledPosition[i].y, null);
                }
            }
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
            if (isLcdUnitStringVisible()) {
                unitLayout = new TextLayout(getLcdUnitString(), G2.getFont(), RENDER_CONTEXT);
                UNIT_BOUNDARY.setFrame(unitLayout.getBounds());
                G2.drawString(getLcdUnitString(), (int) (((getGaugeBounds().width - LCD.getWidth()) / 2.0) + (LCD.getWidth() - UNIT_BOUNDARY.getWidth()) - LCD.getWidth() * 0.03), (int) ((getGaugeBounds().height * 0.425) + LCD.getHeight() * 0.76));
                UNIT_STRING_WIDTH = UNIT_BOUNDARY.getWidth();
            } else {
                UNIT_STRING_WIDTH = 0;
            }
            G2.setFont(getLcdValueFont());
            switch (getModel().getNumberSystem()) {
                case HEX:
                    valueLayout = new TextLayout(Integer.toHexString((int) getLcdValue()).toUpperCase(), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(Integer.toHexString((int) getLcdValue()).toUpperCase(), (int) (((getGaugeBounds().width - LCD.getWidth()) / 2.0) + (LCD.getWidth() - UNIT_STRING_WIDTH - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09), (int) ((getGaugeBounds().height * 0.425) + LCD.getHeight() * 0.76));
                    break;

                case OCT:
                    valueLayout = new TextLayout(Integer.toOctalString((int) getLcdValue()), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(Integer.toOctalString((int) getLcdValue()), (int) (((getGaugeBounds().width - LCD.getWidth()) / 2.0) + (LCD.getWidth() - UNIT_STRING_WIDTH - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09), (int) ((getGaugeBounds().height * 0.425) + LCD.getHeight() * 0.76));
                    break;

                case DEC:

                default:
                    valueLayout = new TextLayout(formatLcdValue(getLcdValue()), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(formatLcdValue(getLcdValue()), (int) (((getGaugeBounds().width - LCD.getWidth()) / 2.0) + (LCD.getWidth() - UNIT_STRING_WIDTH - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09), (int) ((getGaugeBounds().height * 0.425) + LCD.getHeight() * 0.76));
                    break;
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

        // Draw combined foreground image
        G2.drawImage(fImage, 0, 0, null);

        if (!isEnabled()) {
            G2.drawImage(disabledImage, 0, 0, null);
        }

        G2.translate(-getInnerBounds().x, -getInnerBounds().y);

        G2.dispose();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    @Override
    public void setValue(double value) {
        super.setValue(value);

        // Set active leds relating to the new value
        calcNoOfActiveLed();

        if (isValueCoupled()) {
            setLcdValue(value);
        }
        repaint(getInnerBounds());
    }

    @Override
    public void setMinValue(final double MIN_VALUE) {
        super.setMinValue(MIN_VALUE);
        calcNoOfActiveLed();
        repaint(getInnerBounds());
    }

    @Override
    public void setMaxValue(final double MAX_VALUE) {
        super.setMaxValue(MAX_VALUE);
        calcNoOfActiveLed();
        repaint(getInnerBounds());
    }

    private void calcNoOfActiveLed() {
        noOfActiveLeds = (int) (13 / (getMaxValue() - getMinValue()) * getValue());
    }

    public Color getValueColor() {
        return this.valueColor;
    }

    public void setValueColor(final Color VALUE_COLOR) {
        this.valueColor = VALUE_COLOR;
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

    // <editor-fold defaultstate="collapsed" desc="Image related">
    private BufferedImage create_LED_OFF_Image(final int WIDTH, final LedColor LED_COLOR) {
        final BufferedImage IMAGE = UTIL.createImage((int) (WIDTH * 0.1775700935), (int) (WIDTH * 0.1775700935), Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        // Led background
        final Ellipse2D E_LED1_BG = new Ellipse2D.Double(IMAGE_WIDTH * 0.21052631735801697, IMAGE_HEIGHT * 0.21052631735801697, IMAGE_WIDTH * 0.5526316165924072, IMAGE_HEIGHT * 0.5526316165924072);
        final Point2D E_LED1_BG_START = new Point2D.Double(0, E_LED1_BG.getBounds2D().getMinY());
        final Point2D E_LED1_BG_STOP = new Point2D.Double(0, E_LED1_BG.getBounds2D().getMaxY());
        final float[] E_LED1_BG_FRACTIONS = {
            0.0f,
            1.0f
        };
        final Color[] E_LED1_BG_COLORS = {
            new Color(0, 0, 0, 229),
            new Color(153, 153, 153, 255)
        };
        final LinearGradientPaint E_LED1_BG_GRADIENT = new LinearGradientPaint(E_LED1_BG_START, E_LED1_BG_STOP, E_LED1_BG_FRACTIONS, E_LED1_BG_COLORS);
        G2.setPaint(E_LED1_BG_GRADIENT);
        G2.fill(E_LED1_BG);

        // Led foreground
        final Ellipse2D LED_FG = new Ellipse2D.Double(IMAGE_WIDTH * 0.2368421107530594, IMAGE_HEIGHT * 0.2368421107530594, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5);
        final Point2D LED_FG_CENTER = new Point2D.Double(LED_FG.getCenterX(), LED_FG.getCenterY());
        final float[] LED_FG_FRACTIONS = {
            0.0f,
            0.14f,
            0.15f,
            1.0f
        };
        Color[] ledFgColors;

        switch (LED_COLOR) {
            case GREEN:
                ledFgColors = new Color[]{
                    new Color(28, 126, 0, 255),
                    new Color(28, 126, 0, 255),
                    new Color(28, 126, 0, 255),
                    new Color(27, 100, 0, 255)
                };
                break;

            case YELLOW:
                ledFgColors = new Color[]{
                    new Color(164, 128, 8, 255),
                    new Color(158, 125, 10, 255),
                    new Color(158, 125, 10, 255),
                    new Color(130, 96, 25, 255)
                };
                break;

            case RED:
                ledFgColors = new Color[]{
                    new Color(248, 0, 0, 255),
                    new Color(248, 0, 0, 255),
                    new Color(248, 0, 0, 255),
                    new Color(63, 0, 0, 255)
                };
                break;

            default:
                ledFgColors = new Color[]{
                    new Color(28, 126, 0, 255),
                    new Color(28, 126, 0, 255),
                    new Color(28, 126, 0, 255),
                    new Color(27, 100, 0, 255)
                };
                break;
        }

        final RadialGradientPaint LED_FG_GRADIENT = new RadialGradientPaint(LED_FG_CENTER, 0.25f * IMAGE_WIDTH, LED_FG_FRACTIONS, ledFgColors);
        G2.setPaint(LED_FG_GRADIENT);
        G2.fill(LED_FG);

        // Led inner shadow
        final Ellipse2D E_LED1_INNERSHADOW = new Ellipse2D.Double(IMAGE_WIDTH * 0.2368421107530594, IMAGE_HEIGHT * 0.2368421107530594, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5);
        final Point2D E_LED1_INNERSHADOW_CENTER = new Point2D.Double((0.47368421052631576 * IMAGE_WIDTH), (0.47368421052631576 * IMAGE_HEIGHT));
        final float[] E_LED1_INNERSHADOW_FRACTIONS = {
            0.0f,
            0.86f,
            1.0f
        };
        final Color[] E_LED1_INNERSHADOW_COLORS = {
            new Color(0, 0, 0, 0),
            new Color(0, 0, 0, 88),
            new Color(0, 0, 0, 102)
        };
        final RadialGradientPaint E_LED1_INNERSHADOW_GRADIENT = new RadialGradientPaint(E_LED1_INNERSHADOW_CENTER, (float) (0.25 * IMAGE_WIDTH), E_LED1_INNERSHADOW_FRACTIONS, E_LED1_INNERSHADOW_COLORS);
        G2.setPaint(E_LED1_INNERSHADOW_GRADIENT);
        G2.fill(E_LED1_INNERSHADOW);

        // Led highlight
        final Ellipse2D E_LED1_HL = new Ellipse2D.Double(IMAGE_WIDTH * 0.3947368562221527, IMAGE_HEIGHT * 0.31578946113586426, IMAGE_WIDTH * 0.21052631735801697, IMAGE_HEIGHT * 0.1315789520740509);
        final Point2D E_LED1_HL_START = new Point2D.Double(0, E_LED1_HL.getBounds2D().getMinY());
        final Point2D E_LED1_HL_STOP = new Point2D.Double(0, E_LED1_HL.getBounds2D().getMaxY());
        final float[] E_LED1_HL_FRACTIONS = {
            0.0f,
            1.0f
        };
        final Color[] E_LED1_HL_COLORS = {
            new Color(255, 255, 255, 102),
            new Color(255, 255, 255, 0)
        };
        final LinearGradientPaint E_LED1_HL_GRADIENT = new LinearGradientPaint(E_LED1_HL_START, E_LED1_HL_STOP, E_LED1_HL_FRACTIONS, E_LED1_HL_COLORS);
        G2.setPaint(E_LED1_HL_GRADIENT);
        G2.fill(E_LED1_HL);

        G2.dispose();

        return IMAGE;
    }

    private BufferedImage create_LED_ON_Image(final int WIDTH, final LedColor LED_COLOR) {
        final BufferedImage IMAGE = UTIL.createImage((int) (WIDTH * 0.1775700935), (int) (WIDTH * 0.1775700935), Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        // Led background
        final Ellipse2D E_LED1_BG = new Ellipse2D.Double(IMAGE_WIDTH * 0.21052631735801697, IMAGE_HEIGHT * 0.21052631735801697, IMAGE_WIDTH * 0.5526316165924072, IMAGE_HEIGHT * 0.5526316165924072);
        final Point2D E_LED1_BG_START = new Point2D.Double(0, E_LED1_BG.getBounds2D().getMinY());
        final Point2D E_LED1_BG_STOP = new Point2D.Double(0, E_LED1_BG.getBounds2D().getMaxY());
        final float[] E_LED1_BG_FRACTIONS = {
            0.0f,
            1.0f
        };
        final Color[] E_LED1_BG_COLORS = {
            new Color(0, 0, 0, 229),
            new Color(153, 153, 153, 255)
        };
        final LinearGradientPaint E_LED1_BG_GRADIENT = new LinearGradientPaint(E_LED1_BG_START, E_LED1_BG_STOP, E_LED1_BG_FRACTIONS, E_LED1_BG_COLORS);
        G2.setPaint(E_LED1_BG_GRADIENT);
        G2.fill(E_LED1_BG);

        // Led glow
        final Ellipse2D LED_GLOW = new Ellipse2D.Double(0.0, 0.0, IMAGE_WIDTH, IMAGE_HEIGHT);
        final Point2D LED_GLOW_CENTER = new Point2D.Double(LED_GLOW.getCenterX(), LED_GLOW.getCenterY());
        final float[] LED_GLOW_FRACTIONS = {
            0.0f,
            0.57f,
            0.71f,
            0.72f,
            0.85f,
            0.93f,
            0.9301f,
            0.99f
        };
        Color[] ledGlowColors;

        switch (LED_COLOR) {
            case GREEN:
                ledGlowColors = new Color[]{
                    new Color(165, 255, 0, 255),
                    new Color(165, 255, 0, 101),
                    new Color(165, 255, 0, 63),
                    new Color(165, 255, 0, 62),
                    new Color(165, 255, 0, 31),
                    new Color(165, 255, 0, 13),
                    new Color(165, 255, 0, 12),
                    new Color(165, 255, 0, 0)
                };
                break;

            case YELLOW:
                ledGlowColors = new Color[]{
                    new Color(255, 102, 0, 255),
                    new Color(255, 102, 0, 101),
                    new Color(255, 102, 0, 63),
                    new Color(255, 102, 0, 62),
                    new Color(255, 102, 0, 31),
                    new Color(255, 102, 0, 13),
                    new Color(255, 102, 0, 12),
                    new Color(255, 102, 0, 0)
                };
                break;

            case RED:
                ledGlowColors = new Color[]{
                    new Color(255, 0, 0, 255),
                    new Color(255, 0, 0, 101),
                    new Color(255, 0, 0, 63),
                    new Color(255, 0, 0, 62),
                    new Color(255, 0, 0, 31),
                    new Color(255, 0, 0, 13),
                    new Color(255, 0, 0, 12),
                    new Color(255, 0, 0, 0)
                };
                break;

            default:
                ledGlowColors = new Color[]{
                    new Color(165, 255, 0, 255),
                    new Color(165, 255, 0, 101),
                    new Color(165, 255, 0, 63),
                    new Color(165, 255, 0, 62),
                    new Color(165, 255, 0, 31),
                    new Color(165, 255, 0, 13),
                    new Color(165, 255, 0, 12),
                    new Color(165, 255, 0, 0)
                };
                break;
        }
        final RadialGradientPaint LED_GLOW_GRADIENT = new RadialGradientPaint(LED_GLOW_CENTER, 0.5f * IMAGE_WIDTH, LED_GLOW_FRACTIONS, ledGlowColors);
        G2.setPaint(LED_GLOW_GRADIENT);
        G2.fill(LED_GLOW);

        // Led foreground
        final Ellipse2D LED_FG = new Ellipse2D.Double(IMAGE_WIDTH * 0.2368421107530594, IMAGE_HEIGHT * 0.2368421107530594, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5);
        final Point2D LED_FG_CENTER = new Point2D.Double(LED_FG.getCenterX(), LED_FG.getCenterY());
        final float[] LED_FG_FRACTIONS = {
            0.0f,
            0.14f,
            0.15f,
            1.0f
        };
        Color[] ledFgColors;

        switch (LED_COLOR) {
            case GREEN:
                ledFgColors = new Color[]{
                    new Color(154, 255, 137, 255),
                    new Color(154, 255, 137, 255),
                    new Color(154, 255, 137, 255),
                    new Color(89, 255, 42, 255)
                };
                break;

            case YELLOW:
                ledFgColors = new Color[]{
                    new Color(251, 255, 140, 255),
                    new Color(251, 255, 140, 255),
                    new Color(251, 255, 140, 255),
                    new Color(250, 249, 60, 255)
                };
                break;

            case RED:
                ledFgColors = new Color[]{
                    new Color(252, 53, 55, 255),
                    new Color(252, 53, 55, 255),
                    new Color(252, 53, 55, 255),
                    new Color(255, 0, 0, 255)
                };
                break;

            default:
                ledFgColors = new Color[]{
                    new Color(154, 255, 137, 255),
                    new Color(154, 255, 137, 255),
                    new Color(154, 255, 137, 255),
                    new Color(89, 255, 42, 255)
                };
                break;
        }

        final RadialGradientPaint LED_FG_GRADIENT = new RadialGradientPaint(LED_FG_CENTER, 0.25f * IMAGE_WIDTH, LED_FG_FRACTIONS, ledFgColors);
        G2.setPaint(LED_FG_GRADIENT);
        G2.fill(LED_FG);

        // Led inner shadow
        final Ellipse2D E_LED1_INNERSHADOW = new Ellipse2D.Double(IMAGE_WIDTH * 0.2368421107530594, IMAGE_HEIGHT * 0.2368421107530594, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5);
        final Point2D E_LED1_INNERSHADOW_CENTER = new Point2D.Double((0.47368421052631576 * IMAGE_WIDTH), (0.47368421052631576 * IMAGE_HEIGHT));
        final float[] E_LED1_INNERSHADOW_FRACTIONS = {
            0.0f,
            0.86f,
            1.0f
        };
        final Color[] E_LED1_INNERSHADOW_COLORS = {
            new Color(0, 0, 0, 0),
            new Color(0, 0, 0, 88),
            new Color(0, 0, 0, 102)
        };
        final RadialGradientPaint E_LED1_INNERSHADOW_GRADIENT = new RadialGradientPaint(E_LED1_INNERSHADOW_CENTER, (float) (0.25 * IMAGE_WIDTH), E_LED1_INNERSHADOW_FRACTIONS, E_LED1_INNERSHADOW_COLORS);
        G2.setPaint(E_LED1_INNERSHADOW_GRADIENT);
        G2.fill(E_LED1_INNERSHADOW);

        // Led highlight
        final Ellipse2D E_LED1_HL = new Ellipse2D.Double(IMAGE_WIDTH * 0.3947368562221527, IMAGE_HEIGHT * 0.31578946113586426, IMAGE_WIDTH * 0.21052631735801697, IMAGE_HEIGHT * 0.1315789520740509);
        final Point2D E_LED1_HL_START = new Point2D.Double(0, E_LED1_HL.getBounds2D().getMinY());
        final Point2D E_LED1_HL_STOP = new Point2D.Double(0, E_LED1_HL.getBounds2D().getMaxY());
        final float[] E_LED1_HL_FRACTIONS = {
            0.0f,
            1.0f
        };
        final Color[] E_LED1_HL_COLORS = {
            new Color(255, 255, 255, 102),
            new Color(255, 255, 255, 0)
        };
        final LinearGradientPaint E_LED1_HL_GRADIENT = new LinearGradientPaint(E_LED1_HL_START, E_LED1_HL_STOP, E_LED1_HL_FRACTIONS, E_LED1_HL_COLORS);
        G2.setPaint(E_LED1_HL_GRADIENT);
        G2.fill(E_LED1_HL);

        G2.dispose();

        return IMAGE;
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "DigitalRadial";
    }
}
