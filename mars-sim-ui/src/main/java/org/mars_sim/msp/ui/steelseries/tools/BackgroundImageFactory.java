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
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;


/**
 *
 * @author hansolo
 */
public enum BackgroundImageFactory {

    INSTANCE;
    private final Util UTIL = Util.INSTANCE;
    // Variables for caching
    private int radWidth = 0;
    private BackgroundColor radBackgroundColor = BackgroundColor.DARK_GRAY;
    private Paint radCustomBackground = null;
    private BufferedImage radBackgroundImage = UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
    private Color radTextureColor = new Color(0x686868);
    private int linWidth = 0;
    private int linHeight = 0;
    private BackgroundColor linBackgroundColor = BackgroundColor.DARK_GRAY;
    private Paint linCustomBackground = null;
    private BufferedImage linBackgroundImage = UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
    private Color linTextureColor = new Color(0x686868);
    private final Color DARK_NOISE = new Color(0.2f, 0.2f, 0.2f);
    private final Color BRIGHT_NOISE = new Color(0.8f, 0.8f, 0.8f);
    public final BufferedImage STAINLESS_GRINDED_TEXTURE = UTIL.create_STAINLESS_STEEL_PLATE_Texture(100);
    public final BufferedImage CARBON_FIBRE_TEXTURE = UTIL.create_CARBON_Texture(12);
    private BufferedImage punchedSheetTexture = UTIL.create_PUNCHED_SHEET_Image(12, new Color(0x1D2123));

    /**
     * Creates the background image for a radial gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * @param WIDTH
     * @param BACKGROUND_COLOR
     * @return a buffered image that contains the background image of a radial gauge
     */
    public BufferedImage createRadialBackground(final int WIDTH, final BackgroundColor BACKGROUND_COLOR) {
        return createRadialBackground(WIDTH, BACKGROUND_COLOR, null);
    }

    /**
     * Creates the background image for a radial gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * @param WIDTH
     * @param BACKGROUND_COLOR
     * @param CUSTOM_BACKGROUND
     * @return a buffered image that contains the background image of a radial gauge
     */
    public BufferedImage createRadialBackground(final int WIDTH, final BackgroundColor BACKGROUND_COLOR, final Paint CUSTOM_BACKGROUND) {
        return createRadialBackground(WIDTH, BACKGROUND_COLOR, CUSTOM_BACKGROUND, null, null);
    }

    /**
     * Creates the background image for a radial gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating.
     * a new image.
     * If an image is passed to the method, it will paint to the image and
     * return this image. This will reduce the memory consumption.
     * @param WIDTH
     * @param BACKGROUND_COLOR
     * @param CUSTOM_BACKGROUND
     * @param TEXTURE_COLOR
     * @param BACKGROUND_IMAGE
     * @return a buffered image that contains the background image of a radial gauge
     */
    public BufferedImage createRadialBackground(final int WIDTH, final BackgroundColor BACKGROUND_COLOR, final Paint CUSTOM_BACKGROUND, final Color TEXTURE_COLOR, final BufferedImage BACKGROUND_IMAGE) {
        if (WIDTH <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        // Take image from cache instead of creating a new one if parameters are the same as last time
        if (radWidth == WIDTH && radBackgroundColor == BACKGROUND_COLOR && radCustomBackground.equals(CUSTOM_BACKGROUND) && radTextureColor.equals(TEXTURE_COLOR)) {
            if (BACKGROUND_IMAGE != null) {
                final Graphics2D G2 = BACKGROUND_IMAGE.createGraphics();
                G2.drawImage(radBackgroundImage, 0, 0, null);
                G2.dispose();
            }
            return radBackgroundImage;
        }

        radBackgroundImage.flush();
        radBackgroundImage = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);

        final Graphics2D G2 = radBackgroundImage.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = WIDTH;
        final int IMAGE_HEIGHT = WIDTH;

        // Boolean that defines if a overlay gradient will be painted
        boolean fadeInOut = false;

        // Background of gauge
        final Ellipse2D GAUGE_BACKGROUND = new Ellipse2D.Double(IMAGE_WIDTH * 0.08411215245723724, IMAGE_HEIGHT * 0.08411215245723724, IMAGE_WIDTH * 0.8317756652832031, IMAGE_HEIGHT * 0.8317756652832031);
        final Point2D GAUGE_BACKGROUND_START = new Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMinY());
        final Point2D GAUGE_BACKGROUND_STOP = new Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMaxY());
        if (GAUGE_BACKGROUND_START.equals(GAUGE_BACKGROUND_STOP)) {
            GAUGE_BACKGROUND_STOP.setLocation(0.0, GAUGE_BACKGROUND_START.getY() + 1);
        }

        final float[] GAUGE_BACKGROUND_FRACTIONS = {
            0.0f,
            0.40f,
            1.0f
        };

        // Set custom background paint if selected
        if (CUSTOM_BACKGROUND != null && BACKGROUND_COLOR == BackgroundColor.CUSTOM) {
            G2.setPaint(CUSTOM_BACKGROUND);
        } else {
            final Color[] GAUGE_BACKGROUND_COLORS = {
                BACKGROUND_COLOR.GRADIENT_START_COLOR,
                BACKGROUND_COLOR.GRADIENT_FRACTION_COLOR,
                BACKGROUND_COLOR.GRADIENT_STOP_COLOR
            };

            final Paint GAUGE_BACKGROUND_GRADIENT;
            if (BACKGROUND_COLOR == BackgroundColor.BRUSHED_METAL) {
                GAUGE_BACKGROUND_GRADIENT = new TexturePaint(UTIL.createBrushMetalTexture(TEXTURE_COLOR, GAUGE_BACKGROUND.getBounds().width, GAUGE_BACKGROUND.getBounds().height), GAUGE_BACKGROUND.getBounds());
            } else if (BACKGROUND_COLOR == BackgroundColor.STAINLESS) {
                final Point2D CENTER = new Point2D.Double(GAUGE_BACKGROUND.getCenterX(), GAUGE_BACKGROUND.getCenterY());
                final float[] STAINLESS_FRACTIONS = {
                    0f,
                    0.03f,
                    0.10f,
                    0.14f,
                    0.24f,
                    0.33f,
                    0.38f,
                    0.5f,
                    0.62f,
                    0.67f,
                    0.76f,
                    0.81f,
                    0.85f,
                    0.97f,
                    1.0f
                };

                // Define the colors of the conical gradient paint
                final Color[] STAINLESS_COLORS = {
                    new Color(0xFDFDFD),
                    new Color(0xFDFDFD),
                    new Color(0xB2B2B4),
                    new Color(0xACACAE),
                    new Color(0xFDFDFD),
                    new Color(0x6E6E70),
                    new Color(0x6E6E70),
                    new Color(0xFDFDFD),
                    new Color(0x6E6E70),
                    new Color(0x6E6E70),
                    new Color(0xFDFDFD),
                    new Color(0xACACAE),
                    new Color(0xB2B2B4),
                    new Color(0xFDFDFD),
                    new Color(0xFDFDFD)
                };

                // Define the conical gradient paint
                GAUGE_BACKGROUND_GRADIENT = new ConicalGradientPaint(false, CENTER, -0.45f, STAINLESS_FRACTIONS, STAINLESS_COLORS);
            } else if (BACKGROUND_COLOR == BackgroundColor.STAINLESS_GRINDED) {
                GAUGE_BACKGROUND_GRADIENT = new TexturePaint(STAINLESS_GRINDED_TEXTURE, new java.awt.Rectangle(0, 0, 100, 100));
            } else if (BACKGROUND_COLOR == BackgroundColor.CARBON) {
                GAUGE_BACKGROUND_GRADIENT = new TexturePaint(CARBON_FIBRE_TEXTURE, new java.awt.Rectangle(0, 0, 12, 12));
                fadeInOut = true;
            } else if (BACKGROUND_COLOR == BackgroundColor.PUNCHED_SHEET) {
                GAUGE_BACKGROUND_GRADIENT = new TexturePaint(punchedSheetTexture, new java.awt.Rectangle(0, 0, 12, 12));
                fadeInOut = true;
            } else if (BACKGROUND_COLOR == BackgroundColor.LINEN) {
                GAUGE_BACKGROUND_GRADIENT = new TexturePaint(UTIL.createLinenTexture(TEXTURE_COLOR, GAUGE_BACKGROUND.getBounds().width, GAUGE_BACKGROUND.getBounds().height), GAUGE_BACKGROUND.getBounds());
            } else if (BACKGROUND_COLOR == BackgroundColor.NOISY_PLASTIC) {
                GAUGE_BACKGROUND_START.setLocation(0.0, GAUGE_BACKGROUND.getMinY());
                GAUGE_BACKGROUND_STOP.setLocation(0.0, GAUGE_BACKGROUND.getMaxY());
                if (GAUGE_BACKGROUND_START.equals(GAUGE_BACKGROUND_STOP)) {
                    GAUGE_BACKGROUND_STOP.setLocation(0.0, GAUGE_BACKGROUND_START.getY() + 1);
                }
                final float[] FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] COLORS = {
                    UTIL.lighter(TEXTURE_COLOR, 0.15f),
                    UTIL.darker(TEXTURE_COLOR, 0.15f)
                };
                GAUGE_BACKGROUND_GRADIENT = new LinearGradientPaint(GAUGE_BACKGROUND_START, GAUGE_BACKGROUND_STOP, FRACTIONS, COLORS);
            } else {
                GAUGE_BACKGROUND_GRADIENT = new LinearGradientPaint(GAUGE_BACKGROUND_START, GAUGE_BACKGROUND_STOP, GAUGE_BACKGROUND_FRACTIONS, GAUGE_BACKGROUND_COLORS);
            }
            G2.setPaint(GAUGE_BACKGROUND_GRADIENT);
        }
        G2.fill(GAUGE_BACKGROUND);

        // add noise if NOISY_PLASTIC
        if (BACKGROUND_COLOR == BackgroundColor.NOISY_PLASTIC) {
            final Random BW_RND = new Random();
            final Random ALPHA_RND = new Random();
            final Shape OLD_CLIP = G2.getClip();
            G2.setClip(GAUGE_BACKGROUND);
            Color noiseColor;
            int noiseAlpha;
            for (int y = 0 ; y < GAUGE_BACKGROUND.getHeight() ; y ++) {
                for (int x = 0 ; x < GAUGE_BACKGROUND.getWidth() ; x ++) {
                    if (BW_RND.nextBoolean()) {
                        noiseColor = BRIGHT_NOISE;
                    } else {
                        noiseColor = DARK_NOISE;
                    }
                    noiseAlpha = 10 + ALPHA_RND.nextInt(10) - 5;
                    G2.setColor(new Color(noiseColor.getRed(), noiseColor.getGreen(), noiseColor.getBlue(), noiseAlpha));
                    G2.drawLine((int) (x + GAUGE_BACKGROUND.getMinX()), (int) (y + GAUGE_BACKGROUND.getMinY()), (int) (x + GAUGE_BACKGROUND.getMinX()), (int) (y + GAUGE_BACKGROUND.getMinY()));
                }
            }
            G2.setClip(OLD_CLIP);
        }

        // Draw an overlay gradient that gives the carbon fibre a more realistic look
        if (fadeInOut) {
            final float[] SHADOW_OVERLAY_FRACTIONS = {
                0.0f,
                0.4f,
                0.6f,
                1.0f
            };
            final Color[] SHADOW_OVERLAY_COLORS = {
                new Color(0f, 0f, 0f, 0.6f),
                new Color(0f, 0f, 0f, 0.0f),
                new Color(0f, 0f, 0f, 0.0f),
                new Color(0f, 0f, 0f, 0.6f)
            };
            final LinearGradientPaint SHADOW_OVERLAY_GRADIENT = new LinearGradientPaint(new Point2D.Double(GAUGE_BACKGROUND.getMinX(), 0), new Point2D.Double(GAUGE_BACKGROUND.getMaxX(), 0), SHADOW_OVERLAY_FRACTIONS, SHADOW_OVERLAY_COLORS);
            G2.setPaint(SHADOW_OVERLAY_GRADIENT);
            G2.fill(GAUGE_BACKGROUND);
        }

        final Ellipse2D GAUGE_INNERSHADOW = new Ellipse2D.Double(IMAGE_WIDTH * 0.08411215245723724, IMAGE_HEIGHT * 0.08411215245723724, IMAGE_WIDTH * 0.8317756652832031, IMAGE_HEIGHT * 0.8317756652832031);
        final Point2D GAUGE_INNERSHADOW_CENTER = new Point2D.Double((0.5 * IMAGE_WIDTH), (0.5 * IMAGE_HEIGHT));
        final float[] GAUGE_INNERSHADOW_FRACTIONS = {
            0.0f,
            0.7f,
            0.71f,
            0.86f,
            0.92f,
            0.97f,
            1.0f
        };
        final Color[] GAUGE_INNERSHADOW_COLORS = {
            new Color(0f, 0f, 0f, 0f),
            new Color(0f, 0f, 0f, 0f),
            new Color(0f, 0f, 0f, 0f),
            new Color(0f, 0f, 0f, 0.03f),
            new Color(0f, 0f, 0f, 0.07f),
            new Color(0f, 0f, 0f, 0.15f),
            new Color(0f, 0f, 0f, 0.3f)
        };
        final RadialGradientPaint GAUGE_INNERSHADOW_GRADIENT = new RadialGradientPaint(GAUGE_INNERSHADOW_CENTER, (float) (0.4158878504672897 * IMAGE_WIDTH), GAUGE_INNERSHADOW_FRACTIONS, GAUGE_INNERSHADOW_COLORS);
        G2.setPaint(GAUGE_INNERSHADOW_GRADIENT);
        G2.fill(GAUGE_INNERSHADOW);
        if (BACKGROUND_COLOR != BackgroundColor.TRANSPARENT) {
            G2.fill(GAUGE_INNERSHADOW);
        }

        G2.dispose();

        if (BACKGROUND_IMAGE != null) {
            final Graphics2D G = BACKGROUND_IMAGE.createGraphics();
            G.drawImage(radBackgroundImage, 0, 0, null);
            G.dispose();
        }

        // Cache current values
        radWidth = WIDTH;
        radBackgroundColor = BACKGROUND_COLOR;
        radCustomBackground = CUSTOM_BACKGROUND;
        radTextureColor = TEXTURE_COLOR;

        return radBackgroundImage;
    }

    /**
     * Creates the background image for a linear gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * @param WIDTH
     * @param HEIGHT
     * @param BACKGROUND_COLOR
     * @return a buffered image that contains the background image of a linear gauge
     */
    public BufferedImage createLinearBackground(final int WIDTH, final int HEIGHT, final BackgroundColor BACKGROUND_COLOR) {
        return createLinearBackground(WIDTH, HEIGHT, BACKGROUND_COLOR, null);
    }

    /**
     * Creates the background image for a linear gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * @param WIDTH
     * @param HEIGHT
     * @param BACKGROUND_COLOR
     * @param CUSTOM_BACKGROUND
     * @return a buffered image that contains the background image of a linear gauge
     */
    public BufferedImage createLinearBackground(final int WIDTH, final int HEIGHT, final BackgroundColor BACKGROUND_COLOR, final Paint CUSTOM_BACKGROUND) {
        return createLinearBackground(WIDTH, HEIGHT, BACKGROUND_COLOR, CUSTOM_BACKGROUND, null, null);
    }

    /**
     * Creates the background image for a linear gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * If an image is passed to the method, it will paint to the image and
     * return this image. This will reduce the memory consumption.
     * @param WIDTH
     * @param HEIGHT
     * @param BACKGROUND_COLOR
     * @param CUSTOM_BACKGROUND
     * @param TEXTURE_COLOR
     * @param BACKGROUND_IMAGE
     * @return a buffered image that contains the background image of a linear gauge
     */
    public BufferedImage createLinearBackground(final int WIDTH, final int HEIGHT, final BackgroundColor BACKGROUND_COLOR, final Paint CUSTOM_BACKGROUND, final Color TEXTURE_COLOR, final BufferedImage BACKGROUND_IMAGE) {
        if (WIDTH <= 32 || HEIGHT <= 32) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        // Take image from cache instead of creating a new one if parameters are the same as last time
        if (linWidth == WIDTH && linHeight == HEIGHT && linBackgroundColor == BACKGROUND_COLOR && linCustomBackground.equals(CUSTOM_BACKGROUND) && linTextureColor.equals(TEXTURE_COLOR)) {
            if (BACKGROUND_IMAGE != null) {
                final Graphics2D G2 = BACKGROUND_IMAGE.createGraphics();
                G2.drawImage(linBackgroundImage, 0, 0, null);
                G2.dispose();
            }
            return linBackgroundImage;
        }

        linBackgroundImage.flush();
        linBackgroundImage = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);

        final Graphics2D G2 = linBackgroundImage.createGraphics();

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = WIDTH;
        final int IMAGE_HEIGHT = HEIGHT;

        // Boolean that defines if a gradient overlay will be painted
        boolean fadeInOut = false;

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

        final RoundRectangle2D GAUGE_BACKGROUND = new RoundRectangle2D.Double(INNER_FRAME.getX() + 1, INNER_FRAME.getY() + 1, INNER_FRAME.getWidth() - 2, INNER_FRAME.getHeight() - 2, BACKGROUND_CORNER_RADIUS, BACKGROUND_CORNER_RADIUS);
        final Point2D BACKGROUND_START = new Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMinY());
        final Point2D BACKGROUND_STOP = new Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMaxY());
        if (BACKGROUND_START.equals(BACKGROUND_STOP)) {
            BACKGROUND_STOP.setLocation(0.0, BACKGROUND_START.getY() + 1);
        }

        final float[] BACKGROUND_FRACTIONS = {
            0.0f,
            0.4f,
            1.0f
        };

        Paint gaugeBackgroundGradient = null;

        // Set custom background paint if selected
        if (CUSTOM_BACKGROUND != null && BACKGROUND_COLOR == BackgroundColor.CUSTOM) {
            G2.setPaint(CUSTOM_BACKGROUND);
        } else {
            final Color[] BACKGROUND_COLORS = {
                BACKGROUND_COLOR.GRADIENT_START_COLOR,
                BACKGROUND_COLOR.GRADIENT_FRACTION_COLOR,
                BACKGROUND_COLOR.GRADIENT_STOP_COLOR
            };

            if (BACKGROUND_COLOR == BackgroundColor.BRUSHED_METAL) {
                gaugeBackgroundGradient = new TexturePaint(UTIL.createBrushMetalTexture(TEXTURE_COLOR, GAUGE_BACKGROUND.getBounds().width, GAUGE_BACKGROUND.getBounds().height), GAUGE_BACKGROUND.getBounds());
            } else if (BACKGROUND_COLOR == BackgroundColor.STAINLESS) {
                gaugeBackgroundGradient = new TexturePaint(UTIL.createBrushMetalTexture(new Color(0x6E6E70), GAUGE_BACKGROUND.getBounds().width, GAUGE_BACKGROUND.getBounds().height, 5, 0.03f, true, 0.5f), GAUGE_BACKGROUND.getBounds());
            } else if (BACKGROUND_COLOR == BackgroundColor.STAINLESS_GRINDED) {
                gaugeBackgroundGradient = new TexturePaint(STAINLESS_GRINDED_TEXTURE, new java.awt.Rectangle(0, 0, 100, 100));
            } else if (BACKGROUND_COLOR == BackgroundColor.CARBON) {
                gaugeBackgroundGradient = new TexturePaint(CARBON_FIBRE_TEXTURE, new java.awt.Rectangle(0, 0, 12, 12));
                fadeInOut = true;
            } else if (BACKGROUND_COLOR == BackgroundColor.PUNCHED_SHEET) {
                gaugeBackgroundGradient = new TexturePaint(punchedSheetTexture, new java.awt.Rectangle(0, 0, 12, 12));
                fadeInOut = true;
            } else if (BACKGROUND_COLOR == BackgroundColor.LINEN) {
                gaugeBackgroundGradient = new TexturePaint(UTIL.createLinenTexture(TEXTURE_COLOR, GAUGE_BACKGROUND.getBounds().width, GAUGE_BACKGROUND.getBounds().height), GAUGE_BACKGROUND.getBounds());
            } else if (BACKGROUND_COLOR == BackgroundColor.NOISY_PLASTIC) {
                BACKGROUND_START.setLocation(0.0, GAUGE_BACKGROUND.getMinY());
                BACKGROUND_STOP.setLocation(0.0, GAUGE_BACKGROUND.getMaxY());
                if (BACKGROUND_START.equals(BACKGROUND_STOP)) {
                    BACKGROUND_STOP.setLocation(0.0, BACKGROUND_START.getY() + 1);
                }
                final float[] FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] COLORS = {
                    UTIL.lighter(TEXTURE_COLOR, 0.15f),
                    UTIL.darker(TEXTURE_COLOR, 0.15f)
                };
                gaugeBackgroundGradient = new LinearGradientPaint(BACKGROUND_START, BACKGROUND_STOP, FRACTIONS, COLORS);
            } else {
                gaugeBackgroundGradient = new LinearGradientPaint(BACKGROUND_START, BACKGROUND_STOP, BACKGROUND_FRACTIONS, BACKGROUND_COLORS);
            }
            G2.setPaint(gaugeBackgroundGradient);
        }
        G2.fill(GAUGE_BACKGROUND);

        // Create inner shadow on background shape
        final BufferedImage CLP;
        if (CUSTOM_BACKGROUND != null && BACKGROUND_COLOR == BackgroundColor.CUSTOM) {
            CLP = Shadow.INSTANCE.createInnerShadow((java.awt.Shape) GAUGE_BACKGROUND, CUSTOM_BACKGROUND, 0, 0.65f, Color.BLACK, 20, 315);
        } else {
            CLP = Shadow.INSTANCE.createInnerShadow((java.awt.Shape) GAUGE_BACKGROUND, gaugeBackgroundGradient, 0, 0.65f, Color.BLACK, 20, 315);
        }
        G2.drawImage(CLP, GAUGE_BACKGROUND.getBounds().x, GAUGE_BACKGROUND.getBounds().y, null);

        // add noise if NOISY_PLASTIC
        if (BACKGROUND_COLOR == BackgroundColor.NOISY_PLASTIC) {
            final Random BW_RND = new Random();
            final Random ALPHA_RND = new Random();
            final Shape OLD_CLIP = G2.getClip();
            G2.setClip(GAUGE_BACKGROUND);
            Color noiseColor;
            int noiseAlpha;
            for (int y = 0 ; y < GAUGE_BACKGROUND.getHeight() ; y ++) {
                for (int x = 0 ; x < GAUGE_BACKGROUND.getWidth() ; x ++) {
                    if (BW_RND.nextBoolean()) {
                        noiseColor = BRIGHT_NOISE;
                    } else {
                        noiseColor = DARK_NOISE;
                    }
                    noiseAlpha = 10 + ALPHA_RND.nextInt(10) - 5;
                    G2.setColor(new Color(noiseColor.getRed(), noiseColor.getGreen(), noiseColor.getBlue(), noiseAlpha));
                    G2.drawLine((int) (x + GAUGE_BACKGROUND.getMinX()), (int) (y + GAUGE_BACKGROUND.getMinY()), (int) (x + GAUGE_BACKGROUND.getMinX()), (int) (y + GAUGE_BACKGROUND.getMinY()));
                }
            }
            G2.setClip(OLD_CLIP);
        }

        // Draw an overlay gradient that gives the carbon fibre a more realistic look
        if (fadeInOut) {
            final float[] SHADOW_OVERLAY_FRACTIONS = {
                0.0f,
                0.4f,
                0.6f,
                1.0f
            };
            final Color[] SHADOW_OVERLAY_COLORS = {
                new Color(0f, 0f, 0f, 0.5f),
                new Color(0f, 0f, 0f, 0.0f),
                new Color(0f, 0f, 0f, 0.0f),
                new Color(0f, 0f, 0f, 0.5f)
            };
            final LinearGradientPaint SHADOW_OVERLAY_GRADIENT = new LinearGradientPaint(new Point2D.Double(GAUGE_BACKGROUND.getMinX(), 0), new Point2D.Double(GAUGE_BACKGROUND.getMaxX(), 0), SHADOW_OVERLAY_FRACTIONS, SHADOW_OVERLAY_COLORS);
            G2.setPaint(SHADOW_OVERLAY_GRADIENT);
            G2.fill(GAUGE_BACKGROUND);
        }

        G2.dispose();

        if (BACKGROUND_IMAGE != null) {
            final Graphics2D G = BACKGROUND_IMAGE.createGraphics();
            G.drawImage(linBackgroundImage, 0, 0, null);
            G.dispose();
        }
        // Cache current values
        linWidth = WIDTH;
        linHeight = HEIGHT;
        linBackgroundColor = BACKGROUND_COLOR;
        linCustomBackground = CUSTOM_BACKGROUND;
        linTextureColor = TEXTURE_COLOR;

        return linBackgroundImage;
    }

    /**
     * Returns the buffered image with the punched sheet texture with the given color
     * @return the buffered image with the punched sheet texture with the given color
     */
    public BufferedImage getPunchedSheetTexture() {
        return punchedSheetTexture;
    }

    /**
     * Recreates the punched sheet texture with the given color
     * @param TEXTURE_COLOR
     */
    public void recreatePunchedSheetTexture(final Color TEXTURE_COLOR) {
        if (punchedSheetTexture != null) {
            punchedSheetTexture.flush();
        }
        punchedSheetTexture = UTIL.create_PUNCHED_SHEET_Image(12, TEXTURE_COLOR);
    }

    @Override
    public String toString() {
        return "BackgroundImageFactory";
    }
}
