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
package org.mars_sim.msp.ui.steelseries.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;


/**
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public enum LcdImageFactory {

    INSTANCE;
    private final Util UTIL = Util.INSTANCE;
    private Rectangle2D boundsBuffer = new Rectangle2D.Double();
    private LcdColor lcdColorBuffer = LcdColor.WHITE_LCD;
    private Paint customLcdBackgroundBuffer = Color.RED;
    private BufferedImage lcdImageBuffer = UTIL.createImage(1, 1, Transparency.TRANSLUCENT);

    /**
     * Returns the image with the given lcd color.
     * @param BOUNDS
     * @param LCD_COLOR
     * @param CUSTOM_LCD_BACKGROUND
     * @param BACKGROUND_IMAGE
     * @return buffered image containing the lcd with the selected lcd color
     */
    public BufferedImage create_LCD_Image(final Rectangle2D BOUNDS, final LcdColor LCD_COLOR, final Paint CUSTOM_LCD_BACKGROUND, final BufferedImage BACKGROUND_IMAGE) {
        if (BOUNDS.getWidth() <= 0 || BOUNDS.getHeight() <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        if (BOUNDS.equals(boundsBuffer) && LCD_COLOR == lcdColorBuffer && CUSTOM_LCD_BACKGROUND == customLcdBackgroundBuffer) {
            if (BACKGROUND_IMAGE != null && lcdImageBuffer.getWidth() == BACKGROUND_IMAGE.getWidth()) {
                final Graphics2D G = BACKGROUND_IMAGE.createGraphics();
                G.drawImage(lcdImageBuffer, 0, 0, null);
                G.dispose();
            }
            return lcdImageBuffer;
        }

        lcdImageBuffer.flush();
        if (BACKGROUND_IMAGE != null) {
            lcdImageBuffer = UTIL.createImage(BACKGROUND_IMAGE.getWidth(), BACKGROUND_IMAGE.getHeight(), Transparency.TRANSLUCENT);
        } else {
            lcdImageBuffer = UTIL.createImage((int) BOUNDS.getWidth(), (int) BOUNDS.getHeight(), Transparency.TRANSLUCENT);
        }

        final Graphics2D G2 = lcdImageBuffer.createGraphics();

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        // Background rectangle
        final Point2D BACKGROUND_START = new Point2D.Double(0.0, BOUNDS.getMinY());
        final Point2D BACKGROUND_STOP = new Point2D.Double(0.0, BOUNDS.getMaxY());
        if (BACKGROUND_START.equals(BACKGROUND_STOP)) {
            BACKGROUND_STOP.setLocation(0.0, BACKGROUND_START.getY() + 1);
        }

        final float[] BACKGROUND_FRACTIONS = {
            0.0f,
            0.08f,
            0.92f,
            1.0f
        };

        final Color[] BACKGROUND_COLORS = {
            new Color(0.3f, 0.3f, 0.3f, 1.0f),
            new Color(0.4f, 0.4f, 0.4f, 1.0f),
            new Color(0.4f, 0.4f, 0.4f, 1.0f),
            new Color(0.9f, 0.9f, 0.9f, 1.0f)
        };

        final LinearGradientPaint BACKGROUND_GRADIENT = new LinearGradientPaint(BACKGROUND_START, BACKGROUND_STOP, BACKGROUND_FRACTIONS, BACKGROUND_COLORS);
        final double BACKGROUND_CORNER_RADIUS = BOUNDS.getWidth() > BOUNDS.getHeight() ? (BOUNDS.getHeight() * 0.12) : (BOUNDS.getWidth() * 0.12);
        final RoundRectangle2D BACKGROUND = new RoundRectangle2D.Double(BOUNDS.getMinX(), BOUNDS.getMinY(), BOUNDS.getWidth(), BOUNDS.getHeight(), BACKGROUND_CORNER_RADIUS, BACKGROUND_CORNER_RADIUS);
        G2.setPaint(BACKGROUND_GRADIENT);
        G2.fill(BACKGROUND);

        // Foreground rectangle
        final Point2D FOREGROUND_START = new Point2D.Double(0.0, BOUNDS.getMinY() + 1.0);
        final Point2D FOREGROUND_STOP = new Point2D.Double(0.0, BOUNDS.getMaxY() - 1);
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
            LCD_COLOR.GRADIENT_START_COLOR,
            LCD_COLOR.GRADIENT_FRACTION1_COLOR,
            LCD_COLOR.GRADIENT_FRACTION2_COLOR,
            LCD_COLOR.GRADIENT_FRACTION3_COLOR,
            LCD_COLOR.GRADIENT_STOP_COLOR
        };

        if (LCD_COLOR == LcdColor.CUSTOM) {
            G2.setPaint(CUSTOM_LCD_BACKGROUND);
        } else {
            final LinearGradientPaint FOREGROUND_GRADIENT = new LinearGradientPaint(FOREGROUND_START, FOREGROUND_STOP, FOREGROUND_FRACTIONS, FOREGROUND_COLORS);
            G2.setPaint(FOREGROUND_GRADIENT);
        }

        final double FOREGROUND_CORNER_RADIUS = BACKGROUND.getArcWidth() - 1;
        final RoundRectangle2D FOREGROUND = new RoundRectangle2D.Double(BOUNDS.getMinX() + 1, BOUNDS.getMinY() + 1, BOUNDS.getWidth() - 2, BOUNDS.getHeight() - 2, FOREGROUND_CORNER_RADIUS, FOREGROUND_CORNER_RADIUS);
        G2.fill(FOREGROUND);

        G2.dispose();


        if (BACKGROUND_IMAGE != null) {
            final Graphics2D G = BACKGROUND_IMAGE.createGraphics();
            G.drawImage(lcdImageBuffer, 0, 0, null);
            G.dispose();
        }

        // Buffer current values
        boundsBuffer.setRect(BOUNDS);
        lcdColorBuffer = LCD_COLOR;
        customLcdBackgroundBuffer = CUSTOM_LCD_BACKGROUND;

        return lcdImageBuffer;
    }
}
