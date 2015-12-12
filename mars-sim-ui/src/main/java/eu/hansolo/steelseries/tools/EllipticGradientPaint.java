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
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Transparency;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public class EllipticGradientPaint implements Paint {

    private final Point2D CENTER;
    private final Point2D RADIUS_X_Y;
    private final float[] FRACTIONS;
    private final Color[] COLORS;
    private final GradientWrapper COLOR_LOOKUP;

    public EllipticGradientPaint(final Point2D GIVEN_CENTER, Point2D GIVEN_RADIUS_X_Y, final float[] GIVEN_FRACTIONS, final Color[] GIVEN_COLORS) {
        if (GIVEN_RADIUS_X_Y.distance(0, 0) <= 0) {
            throw new IllegalArgumentException("Radius must be greater than 0.");
        }

        // Check that fractions and colors are of the same size
        if (GIVEN_FRACTIONS.length != GIVEN_COLORS.length) {
            throw new IllegalArgumentException("Fractions and colors must be equal in size");
        }

        CENTER = GIVEN_CENTER;
        RADIUS_X_Y = GIVEN_RADIUS_X_Y;
        FRACTIONS = GIVEN_FRACTIONS.clone();
        COLORS = GIVEN_COLORS.clone();

        COLOR_LOOKUP = new GradientWrapper(new Point2D.Double(0, 0), new Point2D.Double(100, 0), FRACTIONS, COLORS);
    }

    @Override
    public java.awt.PaintContext createContext(final ColorModel COLOR_MODEL,
                                               final Rectangle DEVICE_BOUNDS,
                                               final Rectangle2D USER_BOUNDS,
                                               final AffineTransform TRANSFORM,
                                               final RenderingHints RENDERING_HINTS) {
        final Point2D TRANSFORMED_CENTER = TRANSFORM.transform(CENTER, null);
        final Point2D TRANSFORMED_RADIUS_XY = TRANSFORM.deltaTransform(RADIUS_X_Y, null);
        return new OvalGradientContext(TRANSFORMED_CENTER, TRANSFORMED_RADIUS_XY, FRACTIONS, COLORS);
    }

    @Override
    public int getTransparency() {
        return Transparency.TRANSLUCENT;
    }

    private final class OvalGradientContext implements PaintContext {

        private final Point2D CENTER;
        private final Ellipse2D.Double ELLIPSE;
        private final Line2D.Double LINE;
        private Map<Double, Double> lookup;
        private double R;

        public OvalGradientContext(final Point2D CENTER, final Point2D RADIUS_X_Y, final float[] FRACTIONS, final Color[] COLORS) {
            this.CENTER = CENTER;
            final double X = CENTER.getX() - RADIUS_X_Y.getX();
            final double Y = CENTER.getY() - RADIUS_X_Y.getY();
            final double WIDTH = 2 * RADIUS_X_Y.getX();
            final double HEIGHT = 2 * RADIUS_X_Y.getY();
            ELLIPSE = new Ellipse2D.Double(X, Y, WIDTH, HEIGHT);
            LINE = new Line2D.Double();
            R = Point2D.distance(0, 0, RADIUS_X_Y.getX(), RADIUS_X_Y.getY());
            initLookup();
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
            final WritableRaster RASTER = getColorModel().createCompatibleWritableRaster(TILE_WIDTH, TILE_HEIGHT);
            int[] data = new int[TILE_WIDTH * TILE_HEIGHT * 4];
            double distance;
            double dx;
            double dy;
            double alpha;
            double roundDegrees;
            double radius;
            float ratio;

            for (int tileY = 0; tileY < TILE_HEIGHT; tileY++) {
                for (int tileX = 0; tileX < TILE_WIDTH; tileX++) {
                    distance = CENTER.distance(X + tileX, Y + tileY);
                    dy = Y + tileY - CENTER.getY();
                    dx = X + tileX - CENTER.getX();
                    alpha = Math.atan2(dy, dx);
                    roundDegrees = Math.round(Math.toDegrees(alpha));
                    radius = lookup.get(roundDegrees);
                    ratio = (float) (distance / radius);
                    if (Float.compare(ratio, 1.0f) > 0) {
                        ratio = 1.0f;
                    }

                    final int BASE = (tileY * TILE_WIDTH + tileX) * 4;
                    data[BASE + 0] = (COLOR_LOOKUP.getColorAt(ratio).getRed());
                    data[BASE + 1] = (COLOR_LOOKUP.getColorAt(ratio).getGreen());
                    data[BASE + 2] = (COLOR_LOOKUP.getColorAt(ratio).getBlue());
                    data[BASE + 3] = (COLOR_LOOKUP.getColorAt(ratio).getAlpha());
                }
            }
            RASTER.setPixels(0, 0, TILE_WIDTH, TILE_HEIGHT, data);
            return RASTER;
        }

        private void initLookup() {
            lookup = new HashMap<Double, Double>(360);
            double alpha;
            double xp;
            double yp;
            for (int angle = -180; angle <= 180; angle++) {
                Double key = Double.valueOf(angle);
                alpha = Math.toRadians(angle);
                xp = CENTER.getX() + R * Math.cos(alpha);
                yp = CENTER.getY() + R * Math.sin(alpha);
                LINE.setLine(CENTER.getX(), CENTER.getY(), xp, yp);
                Double value = Double.valueOf(getRadius());
                lookup.put(key, value);
            }
            lookup.put(0.0, getRadius());
        }

        private double getRadius() {
            final double[] COORDINATES = new double[6];
            final Point2D.Double P = new Point2D.Double();
            double minDistance = Double.MAX_VALUE;
            final double FLATNESS = 0.005;
            final java.awt.geom.PathIterator PATH_ITERATOR = ELLIPSE.getPathIterator(null, FLATNESS);
            while (!PATH_ITERATOR.isDone()) {
                int segment = PATH_ITERATOR.currentSegment(COORDINATES);
                switch (segment) {
                    case java.awt.geom.PathIterator.SEG_CLOSE:

                    case java.awt.geom.PathIterator.SEG_MOVETO:

                    case java.awt.geom.PathIterator.SEG_LINETO:
                        break;

                    default:
                        break;
                }
                final double DISTANCE = LINE.ptSegDist(COORDINATES[0], COORDINATES[1]);

                if (DISTANCE < minDistance) {
                    minDistance = DISTANCE;
                    P.x = COORDINATES[0];
                    P.y = COORDINATES[1];
                }

                PATH_ITERATOR.next();
            }
            return CENTER.distance(P);
        }
    }

}
