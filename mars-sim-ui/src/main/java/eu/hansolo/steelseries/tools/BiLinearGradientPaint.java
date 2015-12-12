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
package eu.hansolo.steelseries.tools;

import java.awt.Color;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.Transparency;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;


/**
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public final class BiLinearGradientPaint implements Paint {

    private static final Util UTIL = Util.INSTANCE;
    private final java.awt.Rectangle BOUNDS;
    private final Color COLOR_00;
    private final Color COLOR_10;
    private final Color COLOR_01;
    private final Color COLOR_11;
    private final float FRACTION_X_STEPSIZE;
    private final float FRACTION_Y_STEPSIZE;
    private int titleBarHeight;

    /**
     * Enhanced constructor which takes bounds of the objects SHAPE to fill and the four
     * colors we need to create the bilinear interpolated gradient
     * @param SHAPE
     * @param COLOR_00
     * @param COLOR_10
     * @param COLOR_01
     * @param COLOR_11
     * @throws IllegalArgumentException
     */
    public BiLinearGradientPaint(final Shape SHAPE, final Color COLOR_00,
                                 final Color COLOR_10, final Color COLOR_01,
                                 final Color COLOR_11) throws IllegalArgumentException {
        // Set the values
        this.BOUNDS = SHAPE.getBounds();
        this.COLOR_00 = COLOR_00;
        this.COLOR_10 = COLOR_10;
        this.COLOR_01 = COLOR_01;
        this.COLOR_11 = COLOR_11;
        this.FRACTION_X_STEPSIZE = 1.0f / (BOUNDS.getBounds().width);
        this.FRACTION_Y_STEPSIZE = 1.0f / (BOUNDS.getBounds().height);
        this.titleBarHeight = -1;
    }

    @Override
    public PaintContext createContext(final ColorModel COLOR_MODEL,
                                               final Rectangle DEVICE_BOUNDS,
                                               final Rectangle2D USER_BOUNDS,
                                               final AffineTransform TRANSFORM,
                                               final RenderingHints HINTS) {
        return new BiLinearGradientPaintContext();
    }

    @Override
    public int getTransparency() {
        return Transparency.TRANSLUCENT;
    }

    private final class BiLinearGradientPaintContext implements PaintContext {

        public BiLinearGradientPaintContext() {
        }

        @Override
        public void dispose() {
        }

        @Override
        public ColorModel getColorModel() {
            return ColorModel.getRGBdefault();
        }

        @Override
        public Raster getRaster(final int X, final int Y, final int TILE_WIDTH, final int TILE_HEIGHT) {
            // Get the offset given by the height of the titlebar
            if (titleBarHeight == -1) {
                titleBarHeight = Y;
            }

            // Create raster for given colormodel
            final WritableRaster RASTER = getColorModel().createCompatibleWritableRaster(TILE_WIDTH, TILE_HEIGHT);

            // Create data array with place for red, green, blue and alpha values
            final int[] DATA = new int[(TILE_WIDTH * TILE_HEIGHT * 4)];
            Color currentColor;

            float fraction_x = (X - BOUNDS.x) * FRACTION_X_STEPSIZE;
            float fraction_y = (Y - BOUNDS.y - titleBarHeight) * FRACTION_Y_STEPSIZE;

            fraction_x = fraction_x > 1f ? 1f : fraction_x;
            fraction_y = fraction_y > 1f ? 1f : fraction_y;

            for (int tileY = 0; tileY < TILE_HEIGHT; tileY++) {
                for (int tileX = 0; tileX < TILE_WIDTH; tileX++) {
                    currentColor = UTIL.bilinearInterpolateColor(COLOR_00, COLOR_10, COLOR_01, COLOR_11, fraction_x, fraction_y);

                    fraction_x += FRACTION_X_STEPSIZE;
                    fraction_x = fraction_x > 1f ? 1f : fraction_x;

                    // Fill data array with calculated color values
                    final int BASE = (tileY * TILE_WIDTH + tileX) * 4;
                    DATA[BASE + 0] = currentColor.getRed();
                    DATA[BASE + 1] = currentColor.getGreen();
                    DATA[BASE + 2] = currentColor.getBlue();
                    DATA[BASE + 3] = currentColor.getAlpha();
                }
                fraction_x = (X - BOUNDS.x) * FRACTION_X_STEPSIZE;
                fraction_y += FRACTION_Y_STEPSIZE;
                fraction_y = fraction_y > 1f ? 1f : fraction_y;
            }

            // Fill the raster with the data
            RASTER.setPixels(0, 0, TILE_WIDTH, TILE_HEIGHT, DATA);

            return RASTER;
        }
    }

    @Override
    public String toString() {
        return "BiLinearGradientPaint";
    }
}
