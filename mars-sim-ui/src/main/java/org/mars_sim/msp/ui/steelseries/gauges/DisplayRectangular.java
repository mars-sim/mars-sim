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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.mars_sim.msp.ui.steelseries.tools.LcdColor;


/**
 *
 * @author hansolo
 */
public final class DisplayRectangular extends AbstractLinear {
    // <editor-fold defaultstate="collapsed" desc="Variable declarations">
    private BufferedImage frameImage;
    private BufferedImage backgroundImage;
    private BufferedImage titleImage;
    private BufferedImage lcdImage;
    private BufferedImage foregroundImage;
    private BufferedImage disabledImage;
    private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
    private TextLayout unitLayout;
    private final Rectangle2D UNIT_BOUNDARY = new Rectangle2D.Double();
    private TextLayout valueLayout;
    private final Rectangle2D VALUE_BOUNDARY = new Rectangle2D.Double();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public DisplayRectangular() {
        super();
        init(getInnerBounds().width, getInnerBounds().height);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    @Override
    public final AbstractGauge init(final int WIDTH, final int HEIGHT) {
        if (WIDTH <= 1 || HEIGHT <= 1) {
            return this;
        }

        int offset = 0;
        if (isFrameVisible()) {
            offset = 18;
        }

        if (isDigitalFont()) {
            setLcdValueFont(LCD_DIGITAL_FONT.deriveFont(0.7f * (getHeight() - (2 * offset))));
        } else {
            setLcdValueFont(LCD_STANDARD_FONT.deriveFont(0.625f * (getHeight() - (2 * offset))));
        }

        if (isCustomLcdUnitFontEnabled()) {
            setLcdUnitFont(getCustomLcdUnitFont().deriveFont(0.25f * (getHeight() - (2 * offset))));
        } else {
            setLcdUnitFont(LCD_STANDARD_FONT.deriveFont(0.25f * (getHeight() - (2 * offset))));
        }

        if (frameImage != null) {
            frameImage.flush();
        }
        frameImage = create_FRAME_Image(WIDTH, HEIGHT);

        if (backgroundImage != null) {
            backgroundImage.flush();
        }
        backgroundImage = create_BACKGROUND_Image(WIDTH, HEIGHT);

        if (lcdImage != null) {
            lcdImage.flush();
        }
        if (isFrameVisible()) {
            lcdImage = create_LCD_Image(WIDTH - (2 * offset), HEIGHT - (2 * offset), getLcdColor(), getCustomLcdBackground());
        } else {
            lcdImage = create_LCD_Image(getWidth(), getHeight(), getLcdColor(), getCustomLcdBackground());
        }
        if (foregroundImage != null) {
            foregroundImage.flush();
        }
        foregroundImage = create_FOREGROUND_Image(WIDTH, HEIGHT);

        if (disabledImage != null) {
            disabledImage.flush();
        }
        disabledImage = create_DISABLED_Image(getWidth(), getHeight());

        if (backgroundImage != null) {
            backgroundImage.flush();
        }
        backgroundImage = create_BACKGROUND_Image(WIDTH, HEIGHT);
        if (foregroundImage != null) {
            foregroundImage.flush();
        }
        foregroundImage = create_FOREGROUND_Image(WIDTH, HEIGHT);
        if (disabledImage != null) {
            disabledImage.flush();
        }
        disabledImage = create_DISABLED_Image(WIDTH, HEIGHT);

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
        //G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        //G2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        G2.translate(getInnerBounds().x, getInnerBounds().y);

        // Draw the frame
        if (isFrameVisible()) {
            G2.drawImage(frameImage, 0, 0, null);
        }

        // Draw the background
        if (isBackgroundVisible()) {
            G2.drawImage(backgroundImage, 0, 0, null);
        }

        // Draw LCD display
        if (isLcdVisible()) {
            int offset = 0;
            if (isFrameVisible()) {
                offset = 18;
            }

            G2.drawImage(lcdImage, offset, offset, null);

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
                G2.drawString(getLcdUnitString(), (int) ((lcdImage.getWidth() - UNIT_BOUNDARY.getWidth()) - lcdImage.getWidth() * 0.03f) + offset, (int) (lcdImage.getHeight() * 0.76f) + offset);
                UNIT_STRING_WIDTH = UNIT_BOUNDARY.getWidth();
            } else {
                UNIT_STRING_WIDTH = 0;
            }
            G2.setFont(getLcdValueFont());
            valueLayout = new TextLayout(formatLcdValue(getLcdValue()), G2.getFont(), RENDER_CONTEXT);
            VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
            G2.drawString(formatLcdValue(getLcdValue()), (int) ((lcdImage.getWidth() - UNIT_STRING_WIDTH - VALUE_BOUNDARY.getWidth()) - lcdImage.getWidth() * 0.09) + offset, (int) (lcdImage.getHeight() * 0.76f) + offset);
        }

        // Draw the foreground
        if (isForegroundVisible()) {
            G2.drawImage(foregroundImage, 0, 0, null);
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
    public boolean isLcdVisible() {
        return true;
    }

    @Override
    public void setValue(final double VALUE) {
        setLcdValue(VALUE);
    }

    @Override
    public double getValue() {
        return getLcdValue();
    }

    @Override
    public boolean isValueCoupled() {
        return false;
    }

    @Override
    public Paint createCustomLcdBackgroundPaint(final Color[] LCD_COLORS) {
        int offset = 1;
        if (isFrameVisible()) {
            offset = 19;
        }
        final Point2D FOREGROUND_START = new Point2D.Double(0.0, offset);
        final Point2D FOREGROUND_STOP = new Point2D.Double(0.0, getHeight() - offset);
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
        return new Point2D.Double(backgroundImage.getWidth() / 2.0 + getInnerBounds().x, backgroundImage.getHeight() / 2.0 + getInnerBounds().y);
    }

    @Override
    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Double(0, 0, getWidth(), getHeight());
    }

    @Override
    public Rectangle getLcdBounds() {
        int offset = 0;
        if (isFrameVisible()) {
            offset = 17;
        }
        return new Rectangle(offset, offset, lcdImage.getWidth(), lcdImage.getHeight());
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "DisplayRectangular";
    }
}
