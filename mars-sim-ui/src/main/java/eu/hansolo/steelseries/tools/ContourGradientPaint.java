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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Transparency;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;


/**
 * A paint class that creates a gradient that is a combination of four linear
 * gradient paints. Each of the gradients start at one of the four sides of
 * the given bounds rectangle and fill stop in the center of the boundary rectangle.
 * You could use it like the other gradient paints in the jdk (LinearGradientPaint and RadialGradientPaint).
 * You simply have to pass the boundary box of your element, a array of floats for the fractions and
 * a array of colors to the ContourGradientPaint. If you would like to create a diamond like gradient
 * you have to pass a boundary rectangle that is square (width == height).
 * Even if the name implies that it uses the contour of a shape it will always use the rectangular
 * boundary box of your shape!
 * @version 1.0
 * @author hansolo
 */
public final class ContourGradientPaint implements Paint {

    private static final Util UTIL = Util.INSTANCE;
    private final Rectangle2D BOUNDS;
    private final Float[] FRACTIONS;
    private final Color[] COLORS;
    private List<Color> colorLookup = new ArrayList<Color>(256);

    /**
     * Enhanced constructor which takes the FRACTIONS in degress from 0.0f to 360.0f and
     * also an GIVEN_OFFSET in degrees around the rotation CENTER
     * @param GIVEN_BOUNDS
     * @param GIVEN_FRACTIONS
     * @param GIVEN_COLORS
     * @throws IllegalArgumentException
     */
    public ContourGradientPaint(final Rectangle2D GIVEN_BOUNDS, final float[] GIVEN_FRACTIONS, final Color[] GIVEN_COLORS) throws IllegalArgumentException {
        // Set the values
        this.BOUNDS = GIVEN_BOUNDS;

        // Check that fractions and colors are of the same size
        if (GIVEN_FRACTIONS.length != GIVEN_COLORS.length) {
            throw new IllegalArgumentException("Fractions and colors must be equal in size");
        }

        List<Float> fractionList = new ArrayList<Float>(GIVEN_FRACTIONS.length);

        for (float fraction : GIVEN_FRACTIONS) {
            fractionList.add(fraction);
        }

        // Adjust fractions and colors array in the case where startvalue != 0.0f and/or endvalue != 1.0f
        List<Color> colorList = new ArrayList<Color>(GIVEN_COLORS.length);
        colorList.addAll(java.util.Arrays.asList(GIVEN_COLORS));

        // Assure that fractions start with 0.0f
        if (fractionList.get(0) != 0.0f) {
            fractionList.add(0, 0.0f);
            final Color TMP_COLOR = colorList.get(0);
            colorList.add(0, TMP_COLOR);
        }

        // Assure that fractions end with 1.0f
        if (fractionList.get(fractionList.size() - 1) != 1.0f) {
            fractionList.add(1.0f);
            colorList.add(GIVEN_COLORS[0]);
        }

        COLORS = new Color[colorList.size()];
        FRACTIONS = new Float[fractionList.size()];

        colorList.toArray(COLORS);
        fractionList.toArray(FRACTIONS);

        // Prepare color lookup table
        colorLookup.clear();
        colorLookup.addAll(prepareColorLookup());
    }

    private List<Color> prepareColorLookup() {
        final int SIZE = BOUNDS.getWidth() <= BOUNDS.getHeight() ? ((int) (BOUNDS.getWidth() / 2.0)) : ((int) (BOUNDS.getHeight() / 2.0));

        List<Color> tmpColorLookup = new ArrayList<Color>(SIZE);
        int relativeValue;
        for (int colorIndex = 0; colorIndex < COLORS.length - 1; colorIndex++) {
            relativeValue = 0;
            for (int value = (int) (FRACTIONS[colorIndex] * SIZE); value < (int) (FRACTIONS[colorIndex + 1] * SIZE); value++) {
                tmpColorLookup.add(UTIL.getColorFromFraction(COLORS[colorIndex], COLORS[colorIndex + 1], (int) ((FRACTIONS[colorIndex + 1] - FRACTIONS[colorIndex]) * SIZE), relativeValue));
                relativeValue++;

            }

        }

        return (ArrayList<Color>) tmpColorLookup;
    }

    @Override
    public java.awt.PaintContext createContext(final ColorModel COLOR_MODEL,
                                               final Rectangle DEVICE_BOUNDS,
                                               final Rectangle2D USER_BOUNDS,
                                               final AffineTransform TRANSFORM,
                                               final RenderingHints HINTS) {
        return new ContourGradientPaintContext();
    }

    @Override
    public int getTransparency() {
        return Transparency.TRANSLUCENT;
    }

    private final class ContourGradientPaintContext implements PaintContext {

        private final Point2D P1;
        private final Point2D P2;
        final GeneralPath SECTOR_A = new GeneralPath();
        final GeneralPath SECTOR_B = new GeneralPath();
        final GeneralPath SECTOR_C = new GeneralPath();
        final GeneralPath SECTOR_D = new GeneralPath();

        public ContourGradientPaintContext() {
            if (BOUNDS.getWidth() > BOUNDS.getHeight()) {
                this.P1 = new Point2D.Double(BOUNDS.getX() + BOUNDS.getHeight() / 2.0, BOUNDS.getY() + BOUNDS.getHeight() / 2.0);
                this.P2 = new Point2D.Double(BOUNDS.getMaxX() - BOUNDS.getHeight() / 2.0, BOUNDS.getMinY() + BOUNDS.getHeight() / 2.0);
            } else if (BOUNDS.getWidth() < BOUNDS.getHeight()) {
                this.P1 = new Point2D.Double(BOUNDS.getX() + BOUNDS.getWidth() / 2.0, BOUNDS.getY() + BOUNDS.getWidth() / 2.0);
                this.P2 = new Point2D.Double(BOUNDS.getX() + BOUNDS.getWidth() / 2.0, BOUNDS.getMaxY() - BOUNDS.getWidth() / 2.0);
            } else {
                this.P1 = new Point2D.Double(BOUNDS.getX() + BOUNDS.getWidth() / 2.0, BOUNDS.getY() + BOUNDS.getHeight() / 2.0);
                this.P2 = P1;
            }

            // Definition of the 4 sectors
            SECTOR_A.moveTo(BOUNDS.getMinX(), BOUNDS.getMaxY());
            SECTOR_A.lineTo(P1.getX(), P2.getY());
            SECTOR_A.lineTo(P1.getX(), P1.getY());
            SECTOR_A.lineTo(BOUNDS.getMinX(), BOUNDS.getMinY());
            SECTOR_A.closePath();

            SECTOR_B.moveTo(BOUNDS.getMinX(), BOUNDS.getMinY());
            SECTOR_B.lineTo(P1.getX(), P1.getY());
            SECTOR_B.lineTo(P2.getX(), P1.getY());
            SECTOR_B.lineTo(BOUNDS.getMaxX(), BOUNDS.getMinY());
            SECTOR_B.closePath();

            SECTOR_C.moveTo(BOUNDS.getMaxX(), BOUNDS.getMinY());
            SECTOR_C.lineTo(P2.getX(), P1.getY());
            SECTOR_C.lineTo(P2.getX(), P2.getY());
            SECTOR_C.lineTo(BOUNDS.getMaxX(), BOUNDS.getMaxY());
            SECTOR_C.closePath();

            SECTOR_D.moveTo(BOUNDS.getMaxX(), BOUNDS.getMaxY());
            SECTOR_D.lineTo(P2.getX(), P2.getY());
            SECTOR_D.lineTo(P1.getX(), P2.getY());
            SECTOR_D.lineTo(BOUNDS.getMinX(), BOUNDS.getMaxY());
            SECTOR_D.closePath();
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
            // The moving point
            final Point P = new Point(0, 0);

            // Create raster for given colormodel
            final WritableRaster RASTER = getColorModel().createCompatibleWritableRaster(TILE_WIDTH, TILE_HEIGHT);

            // Create data array with place for red, green, blue and alpha values
            int[] data = new int[(TILE_WIDTH * TILE_HEIGHT * 4)];

            Color currentColor = new Color(0.0f, 0.0f, 0.0f, 0.0f);
            int currentRed = 0;
            int currentGreen = 0;
            int currentBlue = 0;
            int currentAlpha = 0;

            for (int tileY = 0; tileY < TILE_HEIGHT; tileY++) {
                for (int tileX = 0; tileX < TILE_WIDTH; tileX++) {
                    P.setLocation(X + tileX, Y + tileY);
                    if (SECTOR_A.contains(P)) {
                        if (X + tileX - BOUNDS.getBounds().x < colorLookup.size()) {
                            currentColor = colorLookup.get(X + tileX - BOUNDS.getBounds().x);
                        }
                    }

                    if (SECTOR_B.contains(P)) {
                        if (Y + tileY - BOUNDS.getBounds().y < colorLookup.size()) {
                            currentColor = colorLookup.get(Y + tileY - BOUNDS.getBounds().y);
                        }
                    }

                    if (SECTOR_C.contains(P)) {
                        if (colorLookup.size() - (X + tileX - SECTOR_A.getBounds().width - BOUNDS.getBounds().x - ((int) P2.getX() - (int) P1.getX())) < colorLookup.size()) {
                            currentColor = colorLookup.get(colorLookup.size() - (X + tileX - SECTOR_A.getBounds().width - BOUNDS.getBounds().x - ((int) P2.getX() - (int) P1.getX())));
                        }
                    }

                    if (SECTOR_D.contains(P)) {
                        if ((colorLookup.size() - (Y + tileY - SECTOR_B.getBounds().height - BOUNDS.getBounds().y - ((int) P2.getY() - (int) P1.getY()))) < colorLookup.size()) {
                            currentColor = colorLookup.get(colorLookup.size() - (Y + tileY - SECTOR_B.getBounds().height - BOUNDS.getBounds().y - ((int) P2.getY() - (int) P1.getY())));
                        }
                    }

                    // Split the current color in it's parts
                    currentRed = currentColor.getRed();
                    currentGreen = currentColor.getGreen();
                    currentBlue = currentColor.getBlue();
                    currentAlpha = currentColor.getAlpha();

                    // Fill data array with calculated color values
                    final int BASE = (tileY * TILE_WIDTH + tileX) * 4;
                    data[BASE + 0] = currentRed;
                    data[BASE + 1] = currentGreen;
                    data[BASE + 2] = currentBlue;
                    data[BASE + 3] = currentAlpha;
                }
            }

            // Fill the raster with the data
            RASTER.setPixels(0, 0, TILE_WIDTH, TILE_HEIGHT, data);

            return RASTER;
        }
    }

    @Override
    public String toString() {
        return "ContourGradientPaint";
    }
}
