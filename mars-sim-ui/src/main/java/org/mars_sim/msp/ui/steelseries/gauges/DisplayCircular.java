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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.mars_sim.msp.ui.steelseries.tools.LcdColor;
import org.mars_sim.msp.ui.steelseries.tools.NumberSystem;

import java.awt.Transparency;


/**
 *
 * @author hansolo
 */
public final class DisplayCircular extends AbstractRadial {
    // <editor-fold defaultstate="collapsed" desc="Variable declarations">
    private BufferedImage bImage;
    private BufferedImage fImage;
    private BufferedImage lcdThresholdImage;
    private BufferedImage disabledImage;
    private Font lcdFormerValueFont;
    private double oldValue;
    private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
    private TextLayout unitLayout;
    private final Rectangle2D UNIT_BOUNDARY = new Rectangle2D.Double();
    private TextLayout valueLayout;
    private final Rectangle2D VALUE_BOUNDARY = new Rectangle2D.Double();
    private TextLayout oldValueLayout;
    private final Rectangle2D OLD_VALUE_BOUNDARY = new Rectangle2D.Double();
    private TextLayout infoLayout;
    private final Rectangle2D INFO_BOUNDARY = new Rectangle2D.Double();
    private final Rectangle2D LCD = new Rectangle2D.Double();
    private float lcdTextYPositionFactor = 0.6f;
    private boolean displayMulti = true;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public DisplayCircular() {
        super();
        setLcdVisible(true);
        setUserLedPosition(0.453271028, 0.75);
        oldValue = 0;
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

        create_TITLE_Image(WIDTH, getTitle(), getUnitString(), bImage);

        if (displayMulti) {
            lcdTextYPositionFactor = 0.6f;
            createLcdImage(new Rectangle2D.Double(((getGaugeBounds().width - WIDTH * 0.6542056075) / 2.0), (getGaugeBounds().height * 0.425), (WIDTH * 0.6542056075), (WIDTH * 0.2990654206)), getLcdColor(), getCustomLcdBackground(), bImage);
            LCD.setRect(((getGaugeBounds().width - WIDTH * 0.6542056075) / 2.0), (getGaugeBounds().height * 0.425), WIDTH * 0.6542056075, WIDTH * 0.2990654206);
            // Create the lcd threshold indicator image
            if (lcdThresholdImage != null) {
                lcdThresholdImage.flush();
            }
            lcdThresholdImage = create_LCD_THRESHOLD_Image((int) (LCD.getHeight() * 0.2045454545), (int) (LCD.getHeight() * 0.2045454545), getLcdColor().TEXT_COLOR);
        } else {
            lcdTextYPositionFactor = 0.76f;
            createLcdImage(new Rectangle2D.Double(((getGaugeBounds().width - WIDTH * 0.6542056075) / 2.0), (getGaugeBounds().height * 0.425), (WIDTH * 0.6542056075), (WIDTH * 0.2242990654)), getLcdColor(), getCustomLcdBackground(), bImage);
            LCD.setRect(((getGaugeBounds().width - WIDTH * 0.6542056075) / 2.0), (getGaugeBounds().height * 0.425), WIDTH * 0.6542056075, WIDTH * 0.2242990654);
            // Create the lcd threshold indicator image
            if (lcdThresholdImage != null) {
                lcdThresholdImage.flush();
            }
            lcdThresholdImage = create_LCD_THRESHOLD_Image((int) (LCD.getHeight() * 0.2045454545), (int) (LCD.getHeight() * 0.2045454545), getLcdColor().TEXT_COLOR);
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

        if (disabledImage != null) {
            disabledImage.flush();
        }
        disabledImage = create_DISABLED_Image(WIDTH);

        if (bImage != null) {
            if (displayMulti) {
                if (isDigitalFont()) {
                    setLcdValueFont(getModel().getDigitalBaseFont().deriveFont(0.5f * (WIDTH * 0.2990654206f)));
                    lcdFormerValueFont = getModel().getDigitalBaseFont().deriveFont(0.2f * (WIDTH * 0.2990654206f));
                } else {
                    setLcdValueFont(getModel().getStandardBaseFont().deriveFont(0.46875f * (WIDTH * 0.2990654206f)));
                    lcdFormerValueFont = getModel().getStandardBaseFont().deriveFont(0.1875f * (WIDTH * 0.2990654206f));
                }

                if (isCustomLcdUnitFontEnabled()) {
                    setLcdUnitFont(getCustomLcdUnitFont().deriveFont(0.1875f * (WIDTH * 0.2990654206f)));
                } else {
                    setLcdUnitFont(getModel().getStandardBaseFont().deriveFont(0.1875f * (WIDTH * 0.2990654206f)));
                }
            } else {
                if (isDigitalFont()) {
                    setLcdValueFont(getModel().getDigitalBaseFont().deriveFont(0.7f * (WIDTH * 0.2242990654f)));
                } else {
                    setLcdValueFont(getModel().getStandardBaseFont().deriveFont(0.625f * (WIDTH * 0.2242990654f)));
                }

                if (isCustomLcdUnitFontEnabled()) {
                    setLcdUnitFont(getCustomLcdUnitFont().deriveFont(0.25f * (WIDTH * 0.2242990654f)));
                } else {
                    setLcdUnitFont(getModel().getStandardBaseFont().deriveFont(0.25f * (WIDTH * 0.2242990654f)));
                }
                setLcdInfoFont(getModel().getStandardBaseFont().deriveFont(0.15f * (WIDTH * 0.2242990654f)));
            }
        }

        return this;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Visualization">
    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D G2 = (Graphics2D) g.create();

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Translate the coordinate system related to insets
        G2.translate(getInnerBounds().x, getInnerBounds().y);

        // Draw combined background image
        G2.drawImage(bImage, 0, 0, null);

        // Draw LCD display
        if (isLcdVisible() && bImage != null) {
            // Draw lcd text
            if (getLcdColor() == LcdColor.CUSTOM) {
                G2.setColor(getCustomLcdForeground());
            } else {
                G2.setColor(getLcdColor().TEXT_COLOR);
            }
            G2.setFont(getLcdUnitFont());
            final double UNIT_STRING_WIDTH;
            if (isLcdUnitStringVisible() && !getLcdUnitString().isEmpty()) {
                unitLayout = new TextLayout(getLcdUnitString(), G2.getFont(), RENDER_CONTEXT);
                UNIT_BOUNDARY.setFrame(unitLayout.getBounds());
                //G2.drawString(getLcdUnitString(), (int) ((LCD.getWidth() - UNIT_BOUNDARY.getWidth()) - LCD.getWidth() * 0.03 + (getGaugeBounds().width - LCD.getWidth()) / 2.0), (int) (LCD.getHeight() * 0.6 + (getGaugeBounds().height - LCD.getHeight()) / 2.0));
                G2.drawString(getLcdUnitString(), (int) ((LCD.getWidth() - UNIT_BOUNDARY.getWidth()) - LCD.getWidth() * 0.03 + (getGaugeBounds().width - LCD.getWidth()) / 2.0), (int) (LCD.getHeight() * lcdTextYPositionFactor + getGaugeBounds().height * 0.425));
                UNIT_STRING_WIDTH = UNIT_BOUNDARY.getWidth();
            } else {
                UNIT_STRING_WIDTH = 0;
            }

            // Draw value
            G2.setFont(getLcdValueFont());
            switch (getLcdNumberSystem()) {
                case HEX:
                    valueLayout = new TextLayout(Integer.toHexString((int) getLcdValue()).toUpperCase(), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(Integer.toHexString((int) getLcdValue()).toUpperCase(), (int) ((LCD.getWidth() - UNIT_STRING_WIDTH - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09 + ((getGaugeBounds().width - LCD.getWidth()) / 2.0)), (int) (LCD.getHeight() * lcdTextYPositionFactor + getGaugeBounds().height * 0.425));
                    break;

                case OCT:
                    valueLayout = new TextLayout(Integer.toOctalString((int) getLcdValue()), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(Integer.toOctalString((int) getLcdValue()), (int) ((LCD.getWidth() - UNIT_STRING_WIDTH - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09 + ((getGaugeBounds().width - LCD.getWidth()) / 2.0)), (int) (LCD.getHeight() * lcdTextYPositionFactor + getGaugeBounds().height * 0.425));
                    break;

                case DEC:

                default:
                    valueLayout = new TextLayout(formatLcdValue(getLcdValue()), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(formatLcdValue(getLcdValue()), (int) ((LCD.getWidth() - UNIT_STRING_WIDTH - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09 + ((getGaugeBounds().width - LCD.getWidth()) / 2.0)), (int) (LCD.getHeight() * lcdTextYPositionFactor + getGaugeBounds().height * 0.425));
                    break;
            }

            // Draw lcd info string
            if (!getLcdInfoString().isEmpty() && !displayMulti) {
                G2.setFont(getLcdInfoFont());
                infoLayout = new TextLayout(getLcdInfoString(), G2.getFont(), RENDER_CONTEXT);
                INFO_BOUNDARY.setFrame(infoLayout.getBounds());
                G2.drawString(getLcdInfoString(), LCD.getBounds().x + 5, LCD.getBounds().y + (float) INFO_BOUNDARY.getHeight() + 5f);
            }

            if (displayMulti) {
                // Draw oldValue
                G2.setFont(lcdFormerValueFont);
                oldValueLayout = new TextLayout(formatLcdValue(oldValue), G2.getFont(), RENDER_CONTEXT);
                OLD_VALUE_BOUNDARY.setFrame(oldValueLayout.getBounds());
                //G2.drawString(formatLcdValue(oldValue), (int) ((LCD.getWidth() - OLD_VALUE_BOUNDARY.getWidth()) / 2.0 + (getGaugeBounds().width - LCD.getWidth()) / 2.0), (int) (LCD.getHeight() * 0.9 + (getGaugeBounds().height - LCD.getHeight()) / 2.0));
                G2.drawString(formatLcdValue(oldValue), (int) ((LCD.getWidth() - OLD_VALUE_BOUNDARY.getWidth()) / 2.0 + (getGaugeBounds().width - LCD.getWidth()) / 2.0), (int) (LCD.getHeight() * 0.9 + getGaugeBounds().height * 0.425));
            }

            // Draw lcd threshold indicator
            if (getLcdNumberSystem() == NumberSystem.DEC && isLcdThresholdVisible() && getLcdValue() >= getLcdThreshold()) {
                G2.drawImage(lcdThresholdImage, (int) (LCD.getX() + LCD.getHeight() * 0.0568181818), (int) (LCD.getY() + LCD.getHeight() - lcdThresholdImage.getHeight() - LCD.getHeight() * 0.0568181818), null);
            }
        }

        // Draw user LED if enabled
        if (isUserLedVisible()) {
            G2.drawImage(getCurrentUserLedImage(), (int) (getGaugeBounds().width * getUserLedPosition().getX()), (int) (getGaugeBounds().height * getUserLedPosition().getY()), null);
        }

        // Draw combined foreground image
        G2.drawImage(fImage, 0, 0, null);

        if (!isEnabled()) {
            G2.drawImage(disabledImage, 0, 0, null);
        }

        // Translate the coordinate system back to original
        G2.translate(-getInnerBounds().x, -getInnerBounds().y);

        G2.dispose();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public boolean isDisplayMulti() {
        return displayMulti;
    }

    public void setDisplayMulti(final boolean DISPLAY_MULTI) {
        displayMulti = DISPLAY_MULTI;
        init(getWidth(), getWidth());
        repaint(getInnerBounds());
    }

    @Override
    public void setLcdValue(final double VALUE) {
        oldValue = getLcdValue();
        super.setLcdValue(VALUE);
    }

    @Override
    public void setLcdVisible(final boolean LCD_VISIBLE) {
        super.setLcdVisible(true);
    }

    @Override
    public boolean isLcdVisible() {
        return true;
    }

    @Override
    public NumberSystem getLcdNumberSystem() {
        if (displayMulti) {
            return NumberSystem.DEC;
        } else {
            return super.getLcdNumberSystem();
        }
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

    @Override
    public String toString() {
        return "DisplayCircular";
    }
}
