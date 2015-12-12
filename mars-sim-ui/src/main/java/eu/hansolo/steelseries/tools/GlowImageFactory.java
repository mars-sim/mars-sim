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
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;


/**
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public enum GlowImageFactory {
    INSTANCE;

    private final Util UTIL = Util.INSTANCE;
    private int radWidth = 0;
    private Color radGlowColor = Color.RED;
    private boolean radOn = false;
    private GaugeType radGaugeType = GaugeType.TYPE4;
    private boolean radKnobs;
    private Orientation radOrientation;
    private BufferedImage radGlowImage = UTIL.createImage(1, 1, Transparency.TRANSLUCENT);

    private int linWidth = 0;
    private int linHeight = 0;
    private Color linGlowColor = Color.RED;
    private boolean linOn = false;
    private BufferedImage linGlowImage = UTIL.createImage(1, 1, Transparency.TRANSLUCENT);

    private int lcdWidth = 0;
    private int lcdHeight = 0;
    private Color lcdGlowColor = Color.RED;
    private boolean lcdOn = false;
    private BufferedImage lcdGlowImage = UTIL.createImage(1, 1, Transparency.TRANSLUCENT);

    /**
     * Returns an image that simulates a glowing ring which could be used to visualize
     * a state of the gauge by a color. The LED might be too small if you are not in front
     * of the screen and so one could see the current state more easy.
     * @param WIDTH
     * @param GLOW_COLOR
     * @param ON
     * @param GAUGE_TYPE
     * @param KNOBS
     * @param ORIENTATION
     * @return an image that simulates a glowing ring
     */
    public BufferedImage createRadialGlow(final int WIDTH, final Color GLOW_COLOR, final boolean ON, final GaugeType GAUGE_TYPE, final boolean KNOBS, final Orientation ORIENTATION) {
        if (WIDTH <= 0) {
            return null;
        }

        // Take image from cache instead of creating a new one if parameters are the same as last time
        if (radWidth == WIDTH && radGlowColor.equals(GLOW_COLOR) && radOn == ON && radGaugeType == GAUGE_TYPE && radKnobs == KNOBS && radOrientation == ORIENTATION) {
            return radGlowImage;
        }

        radGlowImage.flush();
        radGlowImage = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);

        final Graphics2D G2 = radGlowImage.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = radGlowImage.getWidth();
        final int IMAGE_HEIGHT = radGlowImage.getHeight();

        final Area GLOWRING = new Area(new Ellipse2D.Double(IMAGE_WIDTH * 0.0841121495, IMAGE_WIDTH * 0.0841121495, IMAGE_WIDTH * 0.8317757009, IMAGE_WIDTH * 0.8317757009));
        final Area TMP_RING = new Area(new Ellipse2D.Double(IMAGE_WIDTH * 0.1074766355, IMAGE_WIDTH * 0.1074766355, IMAGE_WIDTH * 0.785046729, IMAGE_WIDTH * 0.785046729));
        GLOWRING.subtract(TMP_RING);

        if (!ON) {
            final Point2D GLOWRING_OFF_START = new Point2D.Double(0, GLOWRING.getBounds2D().getMinY() );
            final Point2D GLOWRING_OFF_STOP = new Point2D.Double(0, GLOWRING.getBounds2D().getMaxY() );
            final float[] GLOWRING_OFF_FRACTIONS = {
                0.0f,
                0.17f,
                0.33f,
                0.34f,
                0.63f,
                0.64f,
                0.83f,
                1.0f
            };
            final Color[] GLOWRING_OFF_COLORS = {
                new Color(204, 204, 204, 102),
                new Color(153, 153, 153, 102),
                new Color(252, 252, 252, 102),
                new Color(255, 255, 255, 102),
                new Color(204, 204, 204, 102),
                new Color(203, 203, 203, 102),
                new Color(153, 153, 153, 102),
                new Color(255, 255, 255, 102)
            };
            final LinearGradientPaint GLOWRING_OFF_GRADIENT = new LinearGradientPaint(GLOWRING_OFF_START, GLOWRING_OFF_STOP, GLOWRING_OFF_FRACTIONS, GLOWRING_OFF_COLORS);
            G2.setPaint(GLOWRING_OFF_GRADIENT);
            G2.fill(GLOWRING);
        } else {
            final Point2D GLOWRING_ON_CENTER = new Point2D.Double( (0.5 * IMAGE_WIDTH), (0.5 * IMAGE_HEIGHT) );
            final float[] GLOWRING_ON_FRACTIONS = {
                0.0f,
                0.8999999f,
                0.9f,
                0.95f,
                1.0f
            };
            final Color[] GLOWRING_ON_COLORS = {
                UTIL.setAlpha(GLOW_COLOR, 0.6f),
                UTIL.setAlpha(GLOW_COLOR, 0.6f),
                UTIL.setSaturation(GLOW_COLOR, 0.6f),
                GLOW_COLOR,
                UTIL.setSaturation(GLOW_COLOR, 0.6f),
            };
            final Paint GLOWRING_ON_GRADIENT = new RadialGradientPaint(GLOWRING_ON_CENTER, (float)(0.4158878504672897 * IMAGE_WIDTH), GLOWRING_ON_FRACTIONS, GLOWRING_ON_COLORS);
            G2.setPaint(GLOWRING_ON_GRADIENT);
            final BufferedImage CLIP_IMAGE_GLOWRING_ON;
            CLIP_IMAGE_GLOWRING_ON = Shadow.INSTANCE.createSoftClipImage((Shape) GLOWRING, GLOWRING_ON_GRADIENT);
            G2.translate(-16, -16);
            G2.drawImage(Shadow.INSTANCE.createDropShadow(CLIP_IMAGE_GLOWRING_ON, 0, 1.0f, 15, 315, GLOW_COLOR), GLOWRING.getBounds().x + 1, GLOWRING.getBounds().y + 1, null);
            G2.translate(16, 16);

            // Create some reflections on the knobs
            if (KNOBS) {
                final Ellipse2D POST_GLOW = new Ellipse2D.Double();
                final Point2D POST_GLOW_START = new Point2D.Double();
                final Point2D POST_GLOW_STOP = new Point2D.Double();
                final float[] POST_GLOW_FRACTIONS = {
                    0.0f,
                    0.5f,
                    1.0f
                };
                final Color[] POST_GLOW_COLORS = {
                    new Color(0, 0, 0, 0),
                    UTIL.setAlpha(GLOW_COLOR, 0.0f),
                    UTIL.setAlpha(GLOW_COLOR, 0.3f)
                };
                Paint postGlowGradient;

                final Ellipse2D CENTER_GLOW = new Ellipse2D.Double();
                final Point2D CENTER_GLOW_CENTER = new Point2D.Double();
                final float[] CENTER_GLOW_FRACTIONS = {
                    0.0f,
                    0.50f,
                    0.98f,
                    1.0f
                };
                final Color[] CENTER_GLOW_COLORS = {
                    UTIL.setAlpha(GLOW_COLOR, 0.0f),
                    UTIL.setAlpha(GLOW_COLOR, 0.1f),
                    UTIL.setAlpha(GLOW_COLOR, 0.2f),
                    UTIL.setAlpha(GLOW_COLOR, 0.1f)
                };
                Paint centerGlowGradient;

                switch (GAUGE_TYPE) {
                    case TYPE1:
                        // min knob
                        POST_GLOW.setFrame(IMAGE_WIDTH * 0.13084112107753754, IMAGE_HEIGHT * 0.514018714427948, IMAGE_WIDTH * 0.0373831776, IMAGE_WIDTH * 0.0373831776);
                        POST_GLOW_START.setLocation(POST_GLOW.getMaxX(), POST_GLOW.getCenterY());
                        POST_GLOW_STOP.setLocation(POST_GLOW.getMinX(), POST_GLOW.getCenterY());
                        postGlowGradient = new LinearGradientPaint(POST_GLOW_START, POST_GLOW_STOP, POST_GLOW_FRACTIONS, POST_GLOW_COLORS);
                        G2.setPaint(postGlowGradient);
                        G2.fill(POST_GLOW);

                        // max knob
                        POST_GLOW.setFrame(IMAGE_WIDTH * 0.5233644843101501, IMAGE_HEIGHT * 0.13084112107753754, IMAGE_WIDTH * 0.0373831776, IMAGE_WIDTH * 0.0373831776);
                        POST_GLOW_START.setLocation(POST_GLOW.getCenterX(), POST_GLOW.getMaxY());
                        POST_GLOW_STOP.setLocation(POST_GLOW.getCenterX(), POST_GLOW.getMinY());
                        postGlowGradient = new LinearGradientPaint(POST_GLOW_START, POST_GLOW_STOP, POST_GLOW_FRACTIONS, POST_GLOW_COLORS);
                        G2.setPaint(postGlowGradient);
                        G2.fill(POST_GLOW);

                        // center knob
                        CENTER_GLOW.setFrame(IMAGE_WIDTH * 0.4579439252, IMAGE_WIDTH * 0.4579439252, IMAGE_WIDTH * 0.0841121495, IMAGE_WIDTH * 0.0841121495);
                        CENTER_GLOW_CENTER.setLocation(CENTER_GLOW.getCenterX(), CENTER_GLOW.getCenterY());
                        centerGlowGradient = new RadialGradientPaint(CENTER_GLOW_CENTER, (float)(CENTER_GLOW.getWidth() / 2.0), CENTER_GLOW_FRACTIONS, CENTER_GLOW_COLORS);
                        G2.setPaint(centerGlowGradient);
                        G2.fill(CENTER_GLOW);
                        break;
                    case TYPE2:
                        // min knob
                        POST_GLOW.setFrame(IMAGE_WIDTH * 0.13084112107753754, IMAGE_HEIGHT * 0.514018714427948, IMAGE_WIDTH * 0.0373831776, IMAGE_WIDTH * 0.0373831776);
                        POST_GLOW_START.setLocation(POST_GLOW.getMaxX(), POST_GLOW.getCenterY());
                        POST_GLOW_STOP.setLocation(POST_GLOW.getMinX(), POST_GLOW.getCenterY());
                        postGlowGradient = new LinearGradientPaint(POST_GLOW_START, POST_GLOW_STOP, POST_GLOW_FRACTIONS, POST_GLOW_COLORS);
                        G2.setPaint(postGlowGradient);
                        G2.fill(POST_GLOW);

                        // max knob
                        POST_GLOW.setFrame(IMAGE_WIDTH * 0.8317757248878479, IMAGE_HEIGHT * 0.514018714427948, IMAGE_WIDTH * 0.0373831776, IMAGE_WIDTH * 0.0373831776);
                        POST_GLOW_START.setLocation(POST_GLOW.getMinX(), POST_GLOW.getCenterY());
                        POST_GLOW_STOP.setLocation(POST_GLOW.getMaxX(), POST_GLOW.getCenterY());
                        postGlowGradient = new LinearGradientPaint(POST_GLOW_START, POST_GLOW_STOP, POST_GLOW_FRACTIONS, POST_GLOW_COLORS);
                        G2.setPaint(postGlowGradient);
                        G2.fill(POST_GLOW);

                        // center knob
                        CENTER_GLOW.setFrame(IMAGE_WIDTH * 0.4579439252, IMAGE_WIDTH * 0.4579439252, IMAGE_WIDTH * 0.0841121495, IMAGE_WIDTH * 0.0841121495);
                        CENTER_GLOW_CENTER.setLocation(CENTER_GLOW.getCenterX(), CENTER_GLOW.getCenterY());
                        centerGlowGradient = new RadialGradientPaint(CENTER_GLOW_CENTER, (float)(CENTER_GLOW.getWidth() / 2.0), CENTER_GLOW_FRACTIONS, CENTER_GLOW_COLORS);
                        G2.setPaint(centerGlowGradient);
                        G2.fill(CENTER_GLOW);
                        break;
                    case TYPE3:
                        // min knob
                        POST_GLOW.setFrame(IMAGE_WIDTH * 0.5233644843101501, IMAGE_HEIGHT * 0.8317757248878479, IMAGE_WIDTH * 0.0373831776, IMAGE_WIDTH * 0.0373831776);
                        POST_GLOW_START.setLocation(POST_GLOW.getCenterX(), POST_GLOW.getMinY());
                        POST_GLOW_STOP.setLocation(POST_GLOW.getCenterX(), POST_GLOW.getMaxY());
                        postGlowGradient = new LinearGradientPaint(POST_GLOW_START, POST_GLOW_STOP, POST_GLOW_FRACTIONS, POST_GLOW_COLORS);
                        G2.setPaint(postGlowGradient);
                        G2.fill(POST_GLOW);

                        // max knob
                        POST_GLOW.setFrame(IMAGE_WIDTH * 0.8317757248878479, IMAGE_HEIGHT * 0.514018714427948, IMAGE_WIDTH * 0.0373831776, IMAGE_WIDTH * 0.0373831776);
                        POST_GLOW_START.setLocation(POST_GLOW.getMinX(), POST_GLOW.getCenterY());
                        POST_GLOW_STOP.setLocation(POST_GLOW.getMaxX(), POST_GLOW.getCenterY());
                        postGlowGradient = new LinearGradientPaint(POST_GLOW_START, POST_GLOW_STOP, POST_GLOW_FRACTIONS, POST_GLOW_COLORS);
                        G2.setPaint(postGlowGradient);
                        G2.fill(POST_GLOW);

                        // center knob
                        CENTER_GLOW.setFrame(IMAGE_WIDTH * 0.4579439252, IMAGE_WIDTH * 0.4579439252, IMAGE_WIDTH * 0.0841121495, IMAGE_WIDTH * 0.0841121495);
                        CENTER_GLOW_CENTER.setLocation(CENTER_GLOW.getCenterX(), CENTER_GLOW.getCenterY());
                        centerGlowGradient = new RadialGradientPaint(CENTER_GLOW_CENTER, (float)(CENTER_GLOW.getWidth() / 2.0), CENTER_GLOW_FRACTIONS, CENTER_GLOW_COLORS);
                        G2.setPaint(centerGlowGradient);
                        G2.fill(CENTER_GLOW);
                        break;
                    case TYPE5:
                        switch(ORIENTATION) {
                            case WEST:
                                // min knob
                                POST_GLOW.setFrame(IMAGE_WIDTH * 0.4485981308, IMAGE_HEIGHT * 0.7803738318, IMAGE_WIDTH * 0.0373831776, IMAGE_WIDTH * 0.0373831776);
                                POST_GLOW_START.setLocation(POST_GLOW.getCenterX(), POST_GLOW.getMinY());
                                POST_GLOW_STOP.setLocation(POST_GLOW.getCenterX(), POST_GLOW.getMaxY());
                                postGlowGradient = new LinearGradientPaint(POST_GLOW_START, POST_GLOW_STOP, POST_GLOW_FRACTIONS, POST_GLOW_COLORS);
                                G2.setPaint(postGlowGradient);
                                G2.fill(POST_GLOW);

                                // max knob
                                POST_GLOW.setFrame(IMAGE_WIDTH * 0.4485981308, IMAGE_HEIGHT * 0.1822429907, IMAGE_WIDTH * 0.0373831776, IMAGE_WIDTH * 0.0373831776);
                                POST_GLOW_START.setLocation(POST_GLOW.getCenterX(), POST_GLOW.getMaxY());
                                POST_GLOW_STOP.setLocation(POST_GLOW.getCenterX(), POST_GLOW.getMinY());
                                postGlowGradient = new LinearGradientPaint(POST_GLOW_START, POST_GLOW_STOP, POST_GLOW_FRACTIONS, POST_GLOW_COLORS);
                                G2.setPaint(postGlowGradient);
                                G2.fill(POST_GLOW);

                                // center knob
                                CENTER_GLOW.setFrame(IMAGE_WIDTH * 0.691588785, IMAGE_WIDTH * 0.4579439252, IMAGE_WIDTH * 0.0841121495, IMAGE_WIDTH * 0.0841121495);
                                //CENTER_GLOW_CENTER.setLocation(CENTER_GLOW.getCenterX(), CENTER_GLOW.getCenterY());
                                //centerGlowGradient = new RadialGradientPaint(CENTER_GLOW_CENTER, (float)(CENTER_GLOW.getWidth() / 2.0), CENTER_GLOW_FRACTIONS, CENTER_GLOW_COLORS);
                                centerGlowGradient = new LinearGradientPaint(new Point2D.Double(CENTER_GLOW.getMinX(), CENTER_GLOW.getCenterY()), new Point2D.Double(CENTER_GLOW.getMaxX(), CENTER_GLOW.getCenterY()), CENTER_GLOW_FRACTIONS, CENTER_GLOW_COLORS);
                                G2.setPaint(centerGlowGradient);
                                G2.fill(CENTER_GLOW);
                                break;

                            default:
                                // min knob
                                POST_GLOW.setFrame(IMAGE_WIDTH * 0.1822429907, IMAGE_HEIGHT * 0.4485981308, IMAGE_WIDTH * 0.0373831776, IMAGE_WIDTH * 0.0373831776);
                                POST_GLOW_START.setLocation(POST_GLOW.getMaxX(), POST_GLOW.getCenterY());
                                POST_GLOW_STOP.setLocation(POST_GLOW.getMinX(), POST_GLOW.getCenterY());
                                postGlowGradient = new LinearGradientPaint(POST_GLOW_START, POST_GLOW_STOP, POST_GLOW_FRACTIONS, POST_GLOW_COLORS);
                                G2.setPaint(postGlowGradient);
                                G2.fill(POST_GLOW);

                                // max knob
                                POST_GLOW.setFrame(IMAGE_WIDTH * 0.7803738318, IMAGE_HEIGHT * 0.4485981308, IMAGE_WIDTH * 0.0373831776, IMAGE_WIDTH * 0.0373831776);
                                POST_GLOW_START.setLocation(POST_GLOW.getMinX(), POST_GLOW.getCenterY());
                                POST_GLOW_STOP.setLocation(POST_GLOW.getMaxX(), POST_GLOW.getCenterY());
                                postGlowGradient = new LinearGradientPaint(POST_GLOW_START, POST_GLOW_STOP, POST_GLOW_FRACTIONS, POST_GLOW_COLORS);
                                G2.setPaint(postGlowGradient);
                                G2.fill(POST_GLOW);

                                // center knob
                                CENTER_GLOW.setFrame(IMAGE_WIDTH * 0.4579439252, IMAGE_WIDTH * 0.691588785, IMAGE_WIDTH * 0.0841121495, IMAGE_WIDTH * 0.0841121495);
                                //CENTER_GLOW_CENTER.setLocation(CENTER_GLOW.getCenterX(), CENTER_GLOW.getCenterY());
                                //centerGlowGradient = new RadialGradientPaint(CENTER_GLOW_CENTER, (float)(CENTER_GLOW.getWidth() / 2.0), CENTER_GLOW_FRACTIONS, CENTER_GLOW_COLORS);
                                centerGlowGradient = new LinearGradientPaint(new Point2D.Double(CENTER_GLOW.getCenterX(), CENTER_GLOW.getMinY()), new Point2D.Double(CENTER_GLOW.getCenterX(), CENTER_GLOW.getMaxY()), CENTER_GLOW_FRACTIONS, CENTER_GLOW_COLORS);
                                G2.setPaint(centerGlowGradient);
                                G2.fill(CENTER_GLOW);
                                break;
                        }
                        break;
                    case TYPE4:

                    default:
                        // min knob
                        POST_GLOW.setFrame(IMAGE_WIDTH * 0.336448609828949, IMAGE_HEIGHT * 0.8037382960319519, IMAGE_WIDTH * 0.0373831776, IMAGE_WIDTH * 0.0373831776);
                        POST_GLOW_START.setLocation(POST_GLOW.getMaxX(), POST_GLOW.getMinY());
                        POST_GLOW_STOP.setLocation(POST_GLOW.getMinX(), POST_GLOW.getMaxY());
                        postGlowGradient = new LinearGradientPaint(POST_GLOW_START, POST_GLOW_STOP, POST_GLOW_FRACTIONS, POST_GLOW_COLORS);
                        G2.setPaint(postGlowGradient);
                        G2.fill(POST_GLOW);

                        // max knob
                        POST_GLOW.setFrame(IMAGE_WIDTH * 0.6261682510375977, IMAGE_HEIGHT * 0.8037382960319519, IMAGE_WIDTH * 0.0373831776, IMAGE_WIDTH * 0.0373831776);
                        POST_GLOW_START.setLocation(POST_GLOW.getMinX(), POST_GLOW.getMinY());
                        POST_GLOW_STOP.setLocation(POST_GLOW.getMaxX(), POST_GLOW.getMaxY());
                        postGlowGradient = new LinearGradientPaint(POST_GLOW_START, POST_GLOW_STOP, POST_GLOW_FRACTIONS, POST_GLOW_COLORS);
                        G2.setPaint(postGlowGradient);
                        G2.fill(POST_GLOW);

                        // center knob
                        CENTER_GLOW.setFrame(IMAGE_WIDTH * 0.4579439252, IMAGE_WIDTH * 0.4579439252, IMAGE_WIDTH * 0.0841121495, IMAGE_WIDTH * 0.0841121495);
                        CENTER_GLOW_CENTER.setLocation(CENTER_GLOW.getCenterX(), CENTER_GLOW.getCenterY());
                        centerGlowGradient = new RadialGradientPaint(CENTER_GLOW_CENTER, (float)(CENTER_GLOW.getWidth() / 2.0), CENTER_GLOW_FRACTIONS, CENTER_GLOW_COLORS);
                        G2.setPaint(centerGlowGradient);
                        G2.fill(CENTER_GLOW);
                        break;
                }
            }
        }

        // add a little highlight lower right
        final Area GLOWRING_HL = new Area(new Arc2D.Double(IMAGE_WIDTH * 0.0841121495, IMAGE_WIDTH * 0.0841121495, IMAGE_WIDTH * 0.8317757009, IMAGE_WIDTH * 0.8317757009, 270, 114, Arc2D.PIE));
        GLOWRING_HL.subtract(TMP_RING);

        final Point2D GLOWRING_HL_LOWERRIGHT_CENTER = new Point2D.Double( (0.7336448598130841 * IMAGE_WIDTH), (0.8364485981308412 * IMAGE_HEIGHT) );
        final float[] GLOWRING_HL_LOWERRIGHT_FRACTIONS = {
            0.0f,
            1.0f
        };
        final Color[] GLOWRING_HL_LOWERRIGHT_COLORS = {
            new Color(255, 255, 255, 140),
            new Color(255, 255, 255, 0)
        };
        final Paint GLOWRING_HL_LOWERRIGHT_GRADIENT = new RadialGradientPaint(GLOWRING_HL_LOWERRIGHT_CENTER, (float)(0.23598130841121495 * IMAGE_WIDTH), GLOWRING_HL_LOWERRIGHT_FRACTIONS, GLOWRING_HL_LOWERRIGHT_COLORS);
        G2.setPaint(GLOWRING_HL_LOWERRIGHT_GRADIENT);
        G2.fill(GLOWRING_HL);

        G2.dispose();

        // Memoize parameters
        radWidth = WIDTH;
        radGlowColor = GLOW_COLOR;
        radOn = ON;
        radGaugeType = GAUGE_TYPE;
        radKnobs = KNOBS;
        radOrientation = ORIENTATION;

        return radGlowImage;
    }

    /**
     * Returns an image that simulates a glowing ring which could be used to visualize
     * a state of the gauge by a color. The LED might be too small if you are not in front
     * of the screen and so one could see the current state more easy.
     * @param WIDTH
     * @param HEIGHT
     * @param GLOW_COLOR
     * @param ON
     * @return an image that simulates a glowing ring
     */
    public BufferedImage createLinearGlow(final int WIDTH, final int HEIGHT, final Color GLOW_COLOR, final boolean ON) {
        if (WIDTH <= 32 || HEIGHT <= 32) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        // Take image from cache instead of creating a new one if parameters are the same as last time
        if (linWidth == WIDTH && linHeight == HEIGHT && linGlowColor.equals(GLOW_COLOR) && linOn == ON) {
            return linGlowImage;
        }

        linGlowImage.flush();
        linGlowImage = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);

        final Graphics2D G2 = linGlowImage.createGraphics();

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = WIDTH;
        final int IMAGE_HEIGHT = HEIGHT;


        final double OUTER_FRAME_CORNER_RADIUS;
        if (IMAGE_WIDTH >= IMAGE_HEIGHT) {
            OUTER_FRAME_CORNER_RADIUS = IMAGE_HEIGHT * 0.05;
        } else {
            OUTER_FRAME_CORNER_RADIUS = IMAGE_WIDTH * 0.05;
        }
        final RoundRectangle2D OUTER_FRAME = new RoundRectangle2D.Double(0.0, 0.0, IMAGE_WIDTH, IMAGE_HEIGHT, OUTER_FRAME_CORNER_RADIUS, OUTER_FRAME_CORNER_RADIUS);
        final double FRAME_MAIN_CORNER_RADIUS;
        if (IMAGE_WIDTH >= IMAGE_HEIGHT) {
            FRAME_MAIN_CORNER_RADIUS = OUTER_FRAME_CORNER_RADIUS - ((OUTER_FRAME.getHeight() - IMAGE_HEIGHT - 2) / 2.0);
        } else {
            FRAME_MAIN_CORNER_RADIUS = OUTER_FRAME_CORNER_RADIUS - ((OUTER_FRAME.getWidth() - IMAGE_WIDTH - 2) / 2.0);
        }
        final RoundRectangle2D FRAME_MAIN = new RoundRectangle2D.Double(1.0, 1.0, IMAGE_WIDTH - 2, IMAGE_HEIGHT - 2, FRAME_MAIN_CORNER_RADIUS, FRAME_MAIN_CORNER_RADIUS);

        final double INNER_FRAME_CORNER_RADIUS;
        if (IMAGE_WIDTH >= IMAGE_HEIGHT) {
            INNER_FRAME_CORNER_RADIUS = IMAGE_HEIGHT * 0.02857143;
        } else {
            INNER_FRAME_CORNER_RADIUS = IMAGE_WIDTH * 0.02857143;
        }

        final RoundRectangle2D INNER_FRAME = new RoundRectangle2D.Double(FRAME_MAIN.getX() + 16, FRAME_MAIN.getY() + 16, FRAME_MAIN.getWidth() - 32, FRAME_MAIN.getHeight() - 32, INNER_FRAME_CORNER_RADIUS, INNER_FRAME_CORNER_RADIUS);

        final double BACKGROUND_CORNER_RADIUS = INNER_FRAME_CORNER_RADIUS - 1;

        final Area GLOWRING = new Area(new RoundRectangle2D.Double(INNER_FRAME.getX() + 1, INNER_FRAME.getY() + 1, INNER_FRAME.getWidth() - 2, INNER_FRAME.getHeight() - 2, BACKGROUND_CORNER_RADIUS, BACKGROUND_CORNER_RADIUS));
        final Area TMP_RING = new Area(new RoundRectangle2D.Double(INNER_FRAME.getX() + 6, INNER_FRAME.getY() + 6, INNER_FRAME.getWidth() - 12, INNER_FRAME.getHeight() - 12, BACKGROUND_CORNER_RADIUS, BACKGROUND_CORNER_RADIUS));
        GLOWRING.subtract(TMP_RING);

        if (!ON) {
            final Point2D GLOWRING_OFF_START = new Point2D.Double(0, GLOWRING.getBounds2D().getMinY() );
            final Point2D GLOWRING_OFF_STOP = new Point2D.Double(0, GLOWRING.getBounds2D().getMaxY() );
            final float[] GLOWRING_OFF_FRACTIONS = {
                0.0f,
                0.17f,
                0.33f,
                0.34f,
                0.63f,
                0.64f,
                0.83f,
                1.0f
            };
            final Color[] GLOWRING_OFF_COLORS = {
                new Color(204, 204, 204, 102),
                new Color(153, 153, 153, 102),
                new Color(252, 252, 252, 102),
                new Color(255, 255, 255, 102),
                new Color(204, 204, 204, 102),
                new Color(203, 203, 203, 102),
                new Color(153, 153, 153, 102),
                new Color(255, 255, 255, 102)
            };
            final Paint GLOWRING_OFF_GRADIENT = new LinearGradientPaint(GLOWRING_OFF_START, GLOWRING_OFF_STOP, GLOWRING_OFF_FRACTIONS, GLOWRING_OFF_COLORS);
            G2.setPaint(GLOWRING_OFF_GRADIENT);
            G2.fill(GLOWRING);
        } else {
            final float relFrameSize;
            if (WIDTH >= HEIGHT) {
                relFrameSize = (10f / GLOWRING.getBounds().height);
            } else {
                relFrameSize = (10f / GLOWRING.getBounds().width);
            }
            final float[] GLOWRING_ON_FRACTIONS = {
                0.0f,
                relFrameSize * 0.1f,
                relFrameSize * 0.5f,
                relFrameSize,
                1.0f
            };
            final Color[] GLOWRING_ON_COLORS = {
                UTIL.setAlpha(GLOW_COLOR, 0.0f),
                UTIL.setSaturation(GLOW_COLOR, 0.6f),
                GLOW_COLOR,
                UTIL.setSaturation(GLOW_COLOR, 0.6f),
                UTIL.setAlpha(GLOW_COLOR, 0.0f)
            };
            final Paint GLOWRING_ON_GRADIENT = new ContourGradientPaint(GLOWRING.getBounds2D(), GLOWRING_ON_FRACTIONS, GLOWRING_ON_COLORS);
            G2.setPaint(GLOWRING_ON_GRADIENT);
            G2.translate(-10, -10);
            G2.drawImage(Shadow.INSTANCE.createDropShadow(GLOWRING, GLOWRING_ON_GRADIENT, GLOW_COLOR, true, null, null, 0, 1.0f, 10, 315, GLOW_COLOR), GLOWRING.getBounds().x, GLOWRING.getBounds().y, null);
            G2.translate(10, 10);

            // add a little highlight
            final Point2D GLOWRING_HL_START = new Point2D.Double(GLOWRING.getBounds2D().getCenterX(), GLOWRING.getBounds2D().getMinY());
            final Point2D GLOWRING_HL_STOP = new Point2D.Double(GLOWRING.getBounds2D().getCenterX(), GLOWRING.getBounds2D().getMaxY());
            final float[] GLOWRING_HL_FRACTIONS = {
                0.0f,
                0.1f,
                0.2f,
                0.2001f,
                0.27f,
                0.41f,
                0.42f,
                0.48f,
                0.48009998f,
                0.55f,
                0.5501f,
                0.92f,
                0.93f,
                0.97f,
                0.99f,
                1.0f
            };
            final Color[] GLOWRING_HL_COLORS = {
                new Color(255, 255, 255, 140),
                new Color(255, 255, 255, 0),
                new Color(255, 255, 255, 50),
                new Color(255, 255, 255, 60),
                new Color(255, 255, 255, 45),
                new Color(255, 255, 255, 0),
                new Color(255, 255, 255, 0),
                new Color(255, 255, 255, 80),
                new Color(255, 255, 255, 95),
                new Color(255, 255, 255, 24),
                new Color(255, 255, 255, 0),
                new Color(255, 255, 255, 0),
                new Color(255, 255, 255, 0),
                new Color(255, 255, 255, 0),
                new Color(255, 255, 255, 124),
                new Color(255, 255, 255, 164)
            };
            final LinearGradientPaint GLOWRING_HL_GRADIENT = new LinearGradientPaint(GLOWRING_HL_START, GLOWRING_HL_STOP, GLOWRING_HL_FRACTIONS, GLOWRING_HL_COLORS);
            G2.setPaint(GLOWRING_HL_GRADIENT);
            G2.fill(GLOWRING);
        }

        G2.dispose();

        // memoize parameters
        linWidth = WIDTH;
        linHeight = HEIGHT;
        linGlowColor = GLOW_COLOR;
        linOn = ON;

        return linGlowImage;
    }

    /**
     * Returns an image that simulates a glowing ring which could be used to visualize
     * a state of the gauge by a color. The LED might be too small if you are not in front
     * of the screen and so one could see the current state more easy.
     * @param WIDTH
     * @param HEIGHT
     * @param GLOW_COLOR
     * @param ON
     * @return an image that simulates a glowing ring
     */
    public BufferedImage createLcdGlow(final int WIDTH, final int HEIGHT, final Color GLOW_COLOR, final boolean ON) {
        if (WIDTH <= 1 || HEIGHT <= 1) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        // Take image from cache instead of creating a new one if parameters are the same as last time
        if (lcdWidth == WIDTH && lcdHeight == HEIGHT && lcdGlowColor.equals(GLOW_COLOR) && lcdOn == ON) {
            return lcdGlowImage;
        }

        lcdGlowImage.flush();
        lcdGlowImage = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);

        final Graphics2D G2 = lcdGlowImage.createGraphics();

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final double CORNER_RADIUS = WIDTH > HEIGHT ? (HEIGHT * 0.095) - 1 : (WIDTH * 0.095) - 1;
        final RoundRectangle2D GLOWRING = new RoundRectangle2D.Double(1, 1, WIDTH - 2, HEIGHT - 2 - 1, CORNER_RADIUS, CORNER_RADIUS);

        final Color[] GLOW_COLORS = {
            UTIL.setAlpha(GLOW_COLOR, 0.65f),
            UTIL.setAlpha(GLOW_COLOR, 0.32f),
            UTIL.setAlpha(GLOW_COLOR, 0.18f),
            UTIL.setAlpha(GLOW_COLOR, 0.07f),
            UTIL.setAlpha(GLOW_COLOR, 0.03f),
            UTIL.setAlpha(GLOW_COLOR, 0.01f)
        };

        for (int i = 0 ; i < 6 ; i++) {
            G2.setColor(GLOW_COLORS[i]);
            GLOWRING.setRoundRect(i + 1, i + 1, WIDTH - 2 - i * 2, HEIGHT - 2 - i * 2, CORNER_RADIUS, CORNER_RADIUS);
            G2.draw(GLOWRING);
        }

        G2.dispose();

        // memoize parameters
        lcdWidth = WIDTH;
        lcdHeight = HEIGHT;
        lcdGlowColor = GLOW_COLOR;
        lcdOn = ON;

        return lcdGlowImage;
    }
}
