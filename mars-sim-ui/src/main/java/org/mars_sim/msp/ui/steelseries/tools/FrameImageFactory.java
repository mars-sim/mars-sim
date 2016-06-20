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
import java.awt.Transparency;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;


/**
 *
 * @author hansolo
 */
public enum FrameImageFactory {
    // <editor-fold defaultstate="collapsed" desc="Variable declarations">
    INSTANCE;
    private final Util UTIL = Util.INSTANCE;
    private Paint outerFrameColor = new Color(0x848484);
    private Paint innerFrameColor = new Color(0.6f, 0.6f, 0.6f, 0.8f);
    // Variables for caching
    private int radWidth = 0;
    private FrameDesign radFrameDesign = FrameDesign.METAL;
    private BufferedImage radFrameImage = UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
    private FrameEffect radFrameEffect = FrameEffect.NONE;
    private Paint radCustomFrame = Color.BLACK;
    private Color radFrameBaseColor = new Color(179, 179, 179, 255);
    private boolean radFrameBaseColorEnabled = false;
    private int linWidth = 0;
    private int linHeight = 0;
    private FrameDesign linFrameDesign = FrameDesign.METAL;
    private BufferedImage linFrameImage = UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
    private FrameEffect linFrameEffect = FrameEffect.NONE;
    private Paint linCustomFrame = Color.BLACK;
    private Color linFrameBaseColor = new Color(179, 179, 179, 255);
    private boolean linFrameBaseColorEnabled = false;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public Paint getOuterFrameColor() {
        return outerFrameColor;
    }

    public void setOuterFrameColor(final Paint OUTER_FRAME_COLOR) {
        outerFrameColor = OUTER_FRAME_COLOR;
    }

    public Paint getInnerFrameColor() {
        return innerFrameColor;
    }

    public void setInnerFrameColor(final Paint INNER_FRAME_COLOR) {
        innerFrameColor = INNER_FRAME_COLOR;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image related (Radial)">
    /**
     * Creates the frame image for a radial gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * @param WIDTH
     * @param FRAME_DESIGN
     * @param FRAME_EFFECT
     * @param CUSTOM_FRAME_DESIGN
     * @return a buffered image that contains the frame image for a radial gauge
     */
    public BufferedImage createRadialFrame(final int WIDTH, final FrameDesign FRAME_DESIGN, final Paint CUSTOM_FRAME_DESIGN, final FrameEffect FRAME_EFFECT) {
        return createRadialFrame(WIDTH, FRAME_DESIGN, CUSTOM_FRAME_DESIGN, new Color(179, 179, 179, 255), false, FRAME_EFFECT, null);
    }

    public BufferedImage createRadialFrame(final int WIDTH, final FrameDesign FRAME_DESIGN, final Paint CUSTOM_FRAME_DESIGN, final Color FRAME_BASECOLOR, final boolean FRAME_BASECOLOR_ENABLED, final FrameEffect FRAME_EFFECT) {
        return createRadialFrame(WIDTH, FRAME_DESIGN, CUSTOM_FRAME_DESIGN, FRAME_BASECOLOR, FRAME_BASECOLOR_ENABLED, FRAME_EFFECT, null);
    }

    public BufferedImage createRadialFrame(final int WIDTH, final FrameDesign FRAME_DESIGN, final Paint CUSTOM_FRAME_DESIGN, final FrameEffect FRAME_EFFECT, final BufferedImage BACKGROUND_IMAGE) {
        return createRadialFrame(WIDTH, FRAME_DESIGN, CUSTOM_FRAME_DESIGN, new Color(179, 179, 179, 255), false, FRAME_EFFECT, BACKGROUND_IMAGE);
    }

    /**
     * Creates the frame image for a radial gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * If an image is passed to the method, it will paint to the image and
     * return this image. This will reduce the memory consumption.
     * @param WIDTH
     * @param FRAME_DESIGN
     * @param CUSTOM_FRAME_DESIGN
     * @param FRAME_BASECOLOR
     * @param FRAME_BASECOLOR_ENABLED
     * @param FRAME_EFFECT
     * @param BACKGROUND_IMAGE
     * @return a buffered image that contains the frame image for a radial gauge
     */
    public BufferedImage createRadialFrame(final int WIDTH, final FrameDesign FRAME_DESIGN, final Paint CUSTOM_FRAME_DESIGN, final Color FRAME_BASECOLOR, final boolean FRAME_BASECOLOR_ENABLED, final FrameEffect FRAME_EFFECT, final BufferedImage BACKGROUND_IMAGE) {
        if (WIDTH <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        // Take image from cache instead of creating a new one if parameters are the same as last time
        if (radWidth == WIDTH && radFrameDesign == FRAME_DESIGN && radFrameEffect == FRAME_EFFECT && radCustomFrame.equals(CUSTOM_FRAME_DESIGN) && radFrameBaseColor.equals(FRAME_BASECOLOR) && radFrameBaseColorEnabled == FRAME_BASECOLOR_ENABLED) {
            if (BACKGROUND_IMAGE != null) {
                final Graphics2D G2 = BACKGROUND_IMAGE.createGraphics();
                G2.drawImage(radFrameImage, 0, 0, null);
                G2.dispose();
            }
            return radFrameImage;
        }

        radFrameImage.flush();
        radFrameImage = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = radFrameImage.createGraphics();

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = WIDTH;
        final int IMAGE_HEIGHT = WIDTH;

        // Shape that will be subtracted from the ellipse and will be filled by the background image later
        final Area SUBTRACT = new Area(new Ellipse2D.Double(IMAGE_WIDTH * 0.08411215245723724, IMAGE_HEIGHT * 0.08411215245723724, IMAGE_WIDTH * 0.8317756652832031, IMAGE_HEIGHT * 0.8317756652832031));

        final Area FRAME_OUTERFRAME = new Area(new Ellipse2D.Double(0.0, 0.0, IMAGE_WIDTH, IMAGE_HEIGHT));
        FRAME_OUTERFRAME.subtract(SUBTRACT);
        G2.setPaint(outerFrameColor);
        G2.fill(FRAME_OUTERFRAME);

        final Area FRAME_MAIN = new Area(new Ellipse2D.Double(IMAGE_WIDTH * 0.004672897048294544, IMAGE_HEIGHT * 0.004672897048294544, IMAGE_WIDTH * 0.9906542301177979, IMAGE_HEIGHT * 0.9906542301177979));
        FRAME_MAIN.subtract(SUBTRACT);
        final Point2D FRAME_MAIN_START = new Point2D.Double(0, FRAME_MAIN.getBounds2D().getMinY());
        final Point2D FRAME_MAIN_STOP = new Point2D.Double(0, FRAME_MAIN.getBounds2D().getMaxY());
        final Point2D FRAME_MAIN_CENTER = new Point2D.Double(FRAME_MAIN.getBounds2D().getCenterX(), FRAME_MAIN.getBounds2D().getCenterY());

        if (CUSTOM_FRAME_DESIGN != null && FRAME_DESIGN == org.mars_sim.msp.ui.steelseries.tools.FrameDesign.CUSTOM) {
            G2.setPaint(CUSTOM_FRAME_DESIGN);
            G2.fill(FRAME_MAIN);
        } else {
            switch (FRAME_DESIGN) {
                case BLACK_METAL:
                    float[] frameMainFractions1 = {
                        0.0f,
                        45.0f,
                        125.0f,
                        180.0f,
                        245.0f,
                        315.0f,
                        360.0f
                    };

                    Color[] frameMainColors1 = {
                        new Color(254, 254, 254, 255),
                        new Color(0, 0, 0, 255),
                        new Color(153, 153, 153, 255),
                        new Color(0, 0, 0, 255),
                        new Color(153, 153, 153, 255),
                        new Color(0, 0, 0, 255),
                        new Color(254, 254, 254, 255)
                    };

                    Paint frameMainPaint1 = new ConicalGradientPaint(true, FRAME_MAIN_CENTER, 0, frameMainFractions1, frameMainColors1);
                    G2.setPaint(frameMainPaint1);
                    G2.fill(FRAME_MAIN);
                    break;

                case METAL:
                    float[] frameMainFractions2 = {
                        0.0f,
                        0.07f,
                        0.12f,
                        1.0f
                    };

                    Color[] frameMainColors2 = {
                        new Color(254, 254, 254, 255),
                        new Color(210, 210, 210, 255),
                        new Color(179, 179, 179, 255),
                        new Color(213, 213, 213, 255)
                    };

                    Paint frameMainPaint2 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions2, frameMainColors2);
                    G2.setPaint(frameMainPaint2);
                    G2.fill(FRAME_MAIN);
                    break;

                case SHINY_METAL:
                    float[] frameMainFractions3 = {
                        0.0f,
                        45.0f,
                        90.0f,
                        125.0f,
                        180.0f,
                        235.0f,
                        270.0f,
                        315.0f,
                        360.0f
                    };

                    Color[] frameMainColors3;
                    if (FRAME_BASECOLOR_ENABLED) {
                        frameMainColors3 = new Color[]{
                            new Color(254, 254, 254, 255),
                            new Color(FRAME_BASECOLOR.getRed(), FRAME_BASECOLOR.getGreen(), FRAME_BASECOLOR.getBlue(), 255),
                            new Color(FRAME_BASECOLOR.getRed(), FRAME_BASECOLOR.getGreen(), FRAME_BASECOLOR.getBlue(), 255),
                            new Color(FRAME_BASECOLOR.brighter().brighter().getRed(), FRAME_BASECOLOR.brighter().brighter().getGreen(), FRAME_BASECOLOR.brighter().brighter().getBlue(), 255),
                            new Color(FRAME_BASECOLOR.getRed(), FRAME_BASECOLOR.getGreen(), FRAME_BASECOLOR.getBlue(), 255),
                            new Color(FRAME_BASECOLOR.brighter().brighter().getRed(), FRAME_BASECOLOR.brighter().brighter().getGreen(), FRAME_BASECOLOR.brighter().brighter().getBlue(), 255),
                            new Color(FRAME_BASECOLOR.getRed(), FRAME_BASECOLOR.getGreen(), FRAME_BASECOLOR.getBlue(), 255),
                            new Color(FRAME_BASECOLOR.getRed(), FRAME_BASECOLOR.getGreen(), FRAME_BASECOLOR.getBlue(), 255),
                            new Color(254, 254, 254, 255)
                        };
                    } else {
                        frameMainColors3 = new Color[]{
                            new Color(254, 254, 254, 255),
                            new Color(210, 210, 210, 255),
                            new Color(179, 179, 179, 255),
                            new Color(238, 238, 238, 255),
                            new Color(160, 160, 160, 255),
                            new Color(238, 238, 238, 255),
                            new Color(179, 179, 179, 255),
                            new Color(210, 210, 210, 255),
                            new Color(254, 254, 254, 255)
                        };
                    }

                    Paint frameMainPaint3 = new ConicalGradientPaint(true, FRAME_MAIN_CENTER, 0, frameMainFractions3, frameMainColors3);
                    G2.setPaint(frameMainPaint3);
                    G2.fill(FRAME_MAIN);
                    break;

                case GLOSSY_METAL:
                    G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT), IMAGE_WIDTH * 0.5f, new float[]{0.0f, 0.96f, 1.0f}, new Color[]{new Color(0.8117647059f, 0.8117647059f, 0.8117647059f, 1f), new Color(0.8039215686f, 0.8f, 0.8039215686f, 1f), new Color(0.9568627451f, 0.9568627451f, 0.9568627451f, 1f)}));
                    G2.fill(FRAME_MAIN);

                    //final Area FRAME_MAIN_GLOSSY2 = new Area(new Ellipse2D.Double(0.018691588785046728 * IMAGE_WIDTH, 0.018691588785046728 * IMAGE_HEIGHT, 0.9626168224299065 * IMAGE_WIDTH, 0.9626168224299065 * IMAGE_HEIGHT));
                    final Area FRAME_MAIN_GLOSSY2 = new Area(new Ellipse2D.Double(0.0140186916 * IMAGE_WIDTH, 0.0140186916 * IMAGE_HEIGHT, 0.9719626168 * IMAGE_WIDTH, 0.9719626168 * IMAGE_HEIGHT));
                    FRAME_MAIN_GLOSSY2.subtract(SUBTRACT);
                    G2.setPaint(new LinearGradientPaint(new Point2D.Double(0, FRAME_MAIN_GLOSSY2.getBounds2D().getMinY()), new Point2D.Double(0, FRAME_MAIN_GLOSSY2.getBounds2D().getMaxY()), new float[]{0.0f, 0.23f, 0.36f, 0.59f, 0.76f, 1.0f}, new Color[]{new Color(0.9764705882f, 0.9764705882f, 0.9764705882f, 1f), new Color(0.7843137255f, 0.7647058824f, 0.7490196078f, 1f), Color.WHITE, new Color(0.1137254902f, 0.1137254902f, 0.1137254902f, 1f), new Color(0.7843137255f, 0.7607843137f, 0.7529411765f, 1f), new Color(0.8196078431f, 0.8196078431f, 0.8196078431f, 1f)}));
                    G2.fill(FRAME_MAIN_GLOSSY2);

                    final Area FRAME_MAIN_GLOSSY3 = new Area(new Ellipse2D.Double(0.06542056074766354 * IMAGE_WIDTH, 0.06542056074766354 * IMAGE_HEIGHT, 0.8691588785046729 * IMAGE_WIDTH, 0.8691588785046729 * IMAGE_HEIGHT));
                    FRAME_MAIN_GLOSSY3.subtract(SUBTRACT);
                    G2.setColor(new Color(0xf6f6f6));
                    G2.fill(FRAME_MAIN_GLOSSY3);

                    final Area FRAME_MAIN_GLOSSY4 = new Area(new Ellipse2D.Double(FRAME_MAIN_GLOSSY3.getBounds2D().getMinX() + 2, FRAME_MAIN_GLOSSY3.getBounds2D().getMinY() + 2, FRAME_MAIN_GLOSSY3.getBounds2D().getWidth() - 4, FRAME_MAIN_GLOSSY3.getBounds2D().getHeight() - 4));
                    FRAME_MAIN_GLOSSY4.subtract(SUBTRACT);
                    G2.setColor(new Color(0x333333));
                    G2.fill(FRAME_MAIN_GLOSSY4);
                    break;

                case BRASS:
                    float[] frameMainFractions4 = {
                        0.0f,
                        0.05f,
                        0.10f,
                        0.50f,
                        0.90f,
                        0.95f,
                        1.0f
                    };

                    Color[] frameMainColors4 = {
                        new Color(249, 243, 155, 255),
                        new Color(246, 226, 101, 255),
                        new Color(240, 225, 132, 255),
                        new Color(90, 57, 22, 255),
                        new Color(249, 237, 139, 255),
                        new Color(243, 226, 108, 255),
                        new Color(202, 182, 113, 255)
                    };
                    Paint frameMainPaint4 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions4, frameMainColors4);
                    G2.setPaint(frameMainPaint4);
                    G2.fill(FRAME_MAIN);
                    break;

                case STEEL:
                    float[] frameMainFractions5 = {
                        0.0f,
                        0.05f,
                        0.10f,
                        0.50f,
                        0.90f,
                        0.95f,
                        1.0f
                    };

                    Color[] frameMainColors5 = {
                        new Color(231, 237, 237, 255),
                        new Color(189, 199, 198, 255),
                        new Color(192, 201, 200, 255),
                        new Color(23, 31, 33, 255),
                        new Color(196, 205, 204, 255),
                        new Color(194, 204, 203, 255),
                        new Color(189, 201, 199, 255)
                    };
                    Paint frameMainPaint5 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions5, frameMainColors5);
                    G2.setPaint(frameMainPaint5);
                    G2.fill(FRAME_MAIN);
                    break;

                case CHROME:
                    float[] frameMainFractions6 = {
                        0.0f,
                        0.09f,
                        0.12f,
                        0.16f,
                        0.25f,
                        0.29f,
                        0.33f,
                        0.38f,
                        0.48f,
                        0.52f,
                        0.63f,
                        0.68f,
                        0.8f,
                        0.83f,
                        0.87f,
                        0.97f,
                        1.0f
                    };

                    Color[] frameMainColors6 = {
                        new Color(255, 255, 255, 255),
                        new Color(255, 255, 255, 255),
                        new Color(136, 136, 138, 255),
                        new Color(164, 185, 190, 255),
                        new Color(158, 179, 182, 255),
                        new Color(112, 112, 112, 255),
                        new Color(221, 227, 227, 255),
                        new Color(155, 176, 179, 255),
                        new Color(156, 176, 177, 255),
                        new Color(254, 255, 255, 255),
                        new Color(255, 255, 255, 255),
                        new Color(156, 180, 180, 255),
                        new Color(198, 209, 211, 255),
                        new Color(246, 248, 247, 255),
                        new Color(204, 216, 216, 255),
                        new Color(164, 188, 190, 255),
                        new Color(255, 255, 255, 255)
                    };

                    Paint frameMainPaint6 = new ConicalGradientPaint(false, FRAME_MAIN_CENTER, 0, frameMainFractions6, frameMainColors6);
                    G2.setPaint(frameMainPaint6);
                    G2.fill(FRAME_MAIN);
                    break;

                case GOLD:
                    float[] frameMainFractions7 = {
                        0.0f,
                        0.15f,
                        0.22f,
                        0.3f,
                        0.38f,
                        0.44f,
                        0.51f,
                        0.6f,
                        0.68f,
                        0.75f,
                        1.0f
                    };

                    Color[] frameMainColors7 = {
                        new Color(255, 255, 207, 255),
                        new Color(255, 237, 96, 255),
                        new Color(254, 199, 57, 255),
                        new Color(255, 249, 203, 255),
                        new Color(255, 199, 64, 255),
                        new Color(252, 194, 60, 255),
                        new Color(255, 204, 59, 255),
                        new Color(213, 134, 29, 255),
                        new Color(255, 201, 56, 255),
                        new Color(212, 135, 29, 255),
                        new Color(247, 238, 101, 255)
                    };
                    Paint frameMainPaint7 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions7, frameMainColors7);
                    G2.setPaint(frameMainPaint7);
                    G2.fill(FRAME_MAIN);
                    break;

                case ANTHRACITE:
                    float[] frameMainFractions8 = {
                        0.0f,
                        0.06f,
                        0.12f,
                        1.0f
                    };
                    Color[] frameMainColors8 = {
                        new Color(118, 117, 135, 255),
                        new Color(74, 74, 82, 255),
                        new Color(50, 50, 54, 255),
                        new Color(97, 97, 108, 255)
                    };
                    Paint frameMainPaint8 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions8, frameMainColors8);
                    G2.setPaint(frameMainPaint8);
                    G2.fill(FRAME_MAIN);
                    break;

                case TILTED_GRAY:
                    FRAME_MAIN_START.setLocation((0.2336448598130841 * IMAGE_WIDTH), (0.08411214953271028 * IMAGE_HEIGHT));
                    FRAME_MAIN_STOP.setLocation(((0.2336448598130841 + 0.5789369637935792) * IMAGE_WIDTH), ((0.08411214953271028 + 0.8268076708711319) * IMAGE_HEIGHT));
                    float[] frameMainFractions9 = {
                        0.0f,
                        0.07f,
                        0.16f,
                        0.33f,
                        0.55f,
                        0.79f,
                        1.0f
                    };
                    Color[] frameMainColors9 = {
                        new Color(255, 255, 255, 255),
                        new Color(210, 210, 210, 255),
                        new Color(179, 179, 179, 255),
                        new Color(255, 255, 255, 255),
                        new Color(197, 197, 197, 255),
                        new Color(255, 255, 255, 255),
                        new Color(102, 102, 102, 255)
                    };
                    Paint frameMainPaint9 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions9, frameMainColors9);
                    G2.setPaint(frameMainPaint9);
                    G2.fill(FRAME_MAIN);
                    break;

                case TILTED_BLACK:
                    FRAME_MAIN_START.setLocation((0.22897196261682243 * IMAGE_WIDTH), (0.0794392523364486 * IMAGE_HEIGHT));
                    FRAME_MAIN_STOP.setLocation(((0.22897196261682243 + 0.573576436351046) * IMAGE_WIDTH), ((0.0794392523364486 + 0.8191520442889918) * IMAGE_HEIGHT));
                    float[] frameMainFractions10 = {
                        0.0f,
                        0.21f,
                        0.47f,
                        0.99f,
                        1.0f
                    };
                    Color[] frameMainColors10 = {
                        new Color(102, 102, 102, 255),
                        new Color(0, 0, 0, 255),
                        new Color(102, 102, 102, 255),
                        new Color(0, 0, 0, 255),
                        new Color(0, 0, 0, 255)
                    };
                    Paint frameMainPaint10 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions10, frameMainColors10);
                    G2.setPaint(frameMainPaint10);
                    G2.fill(FRAME_MAIN);
                    break;

                default:
                    float[] frameMainFractions = {
                        0.0f,
                        0.07f,
                        0.12f,
                        1.0f
                    };

                    Color[] frameMainColors = {
                        new Color(254, 254, 254, 255),
                        new Color(210, 210, 210, 255),
                        new Color(179, 179, 179, 255),
                        new Color(213, 213, 213, 255)
                    };

                    Paint frameMainPaint = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions, frameMainColors);
                    G2.setPaint(frameMainPaint);
                    G2.fill(FRAME_MAIN);
                    break;
            }
        }

        //final Ellipse2D FRAME_INNERFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.07943925261497498, IMAGE_HEIGHT * 0.07943925261497498, IMAGE_WIDTH * 0.8411215543746948, IMAGE_HEIGHT * 0.8411215543746948);
        final Area FRAME_INNERFRAME = new Area(new Ellipse2D.Double(IMAGE_WIDTH * 0.07943925261497498, IMAGE_HEIGHT * 0.07943925261497498, IMAGE_WIDTH * 0.8411215543746948, IMAGE_HEIGHT * 0.8411215543746948));
        FRAME_INNERFRAME.subtract(SUBTRACT);

        // Former white ring
        G2.setPaint(innerFrameColor);
        G2.fill(FRAME_INNERFRAME);

        // Frame effect overlay
        final Point2D EFFECT_CENTER = new Point2D.Double((0.5 * IMAGE_WIDTH), (0.5 * IMAGE_HEIGHT));
        final float[] EFFECT_FRACTIONS;
        final Color[] EFFECT_COLORS;
        final java.awt.Paint EFFECT_GRADIENT;
        switch (FRAME_EFFECT) {
            case NONE:

            default:

                break;

            case EFFECT_BULGE:
                EFFECT_FRACTIONS = new float[]{
                    0.0f,
                    0.82f,
                    0.83f,
                    0.86f,
                    0.87f,
                    1.0f
                };
                EFFECT_COLORS = new Color[]{
                    new Color(0, 0, 0, 0),
                    new Color(0, 0, 0, 76),
                    new Color(0, 0, 0, 95),
                    new Color(219, 219, 219, 153),
                    new Color(255, 255, 255, 151),
                    new Color(0, 0, 0, 102)
                };
                EFFECT_GRADIENT = new RadialGradientPaint(EFFECT_CENTER, (0.5f * IMAGE_WIDTH), EFFECT_FRACTIONS, EFFECT_COLORS);
                G2.setPaint(EFFECT_GRADIENT);
                G2.fill(FRAME_OUTERFRAME);
                break;

            case EFFECT_CONE:
                EFFECT_FRACTIONS = new float[]{
                    0.0f,
                    0.82f,
                    0.8201f,
                    0.96f,
                    0.9601f,
                    1.0f
                };
                EFFECT_COLORS = new Color[]{
                    new Color(0, 0, 0, 0),
                    new Color(0, 0, 0, 50),
                    new Color(9, 9, 9, 51),
                    new Color(255, 255, 255, 124),
                    new Color(223, 223, 223, 127),
                    new Color(0, 0, 0, 76)
                };
                EFFECT_GRADIENT = new RadialGradientPaint(EFFECT_CENTER, (float) (0.5 * IMAGE_WIDTH), EFFECT_FRACTIONS, EFFECT_COLORS);
                G2.setPaint(EFFECT_GRADIENT);
                G2.fill(FRAME_OUTERFRAME);
                break;

            case EFFECT_TORUS:
                EFFECT_FRACTIONS = new float[]{
                    0.0f,
                    0.82f,
                    0.8201f,
                    0.92f,
                    1.0f
                };
                EFFECT_COLORS = new Color[]{
                    new Color(0, 0, 0, 0),
                    new Color(0, 0, 0, 50),
                    new Color(13, 13, 13, 51),
                    new Color(255, 255, 255, 64),
                    new Color(0, 0, 0, 76)
                };
                EFFECT_GRADIENT = new RadialGradientPaint(EFFECT_CENTER, (float) (0.5 * IMAGE_WIDTH), EFFECT_FRACTIONS, EFFECT_COLORS);
                G2.setPaint(EFFECT_GRADIENT);
                G2.fill(FRAME_OUTERFRAME);
                break;

            case EFFECT_INNER_FRAME:
                final Ellipse2D EFFECT_BIGINNERFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.0607476644217968, IMAGE_HEIGHT * 0.0607476644217968, IMAGE_WIDTH * 0.8785046339035034, IMAGE_HEIGHT * 0.8785046339035034);
                final Point2D EFFECT_BIGINNERFRAME_START = new Point2D.Double(0, EFFECT_BIGINNERFRAME.getBounds2D().getMinY());
                final Point2D EFFECT_BIGINNERFRAME_STOP = new Point2D.Double(0, EFFECT_BIGINNERFRAME.getBounds2D().getMaxY());
                EFFECT_FRACTIONS = new float[]{
                    0.0f,
                    0.3f,
                    0.5f,
                    0.71f,
                    1.0f
                };
                EFFECT_COLORS = new Color[]{
                    new Color(0, 0, 0, 183),
                    new Color(148, 148, 148, 25),
                    new Color(0, 0, 0, 159),
                    new Color(0, 0, 0, 81),
                    new Color(255, 255, 255, 158)
                };
                final LinearGradientPaint EFFECT_BIGINNERFRAME_GRADIENT = new LinearGradientPaint(EFFECT_BIGINNERFRAME_START, EFFECT_BIGINNERFRAME_STOP, EFFECT_FRACTIONS, EFFECT_COLORS);
                G2.setPaint(EFFECT_BIGINNERFRAME_GRADIENT);
                G2.fill(EFFECT_BIGINNERFRAME);
                break;
        }

        G2.dispose();

        if (BACKGROUND_IMAGE != null) {
            final Graphics2D G = BACKGROUND_IMAGE.createGraphics();
            G.drawImage(radFrameImage, 0, 0, null);
            G.dispose();
        }
        // Cache current parameters
        radWidth = WIDTH;
        radFrameDesign = FRAME_DESIGN;
        radFrameEffect = FRAME_EFFECT;
        radCustomFrame = CUSTOM_FRAME_DESIGN;
        radFrameBaseColor = FRAME_BASECOLOR;
        radFrameBaseColorEnabled = FRAME_BASECOLOR_ENABLED;
        return radFrameImage;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image related (Linear)">
    /**
     * Creates the frame image for a linear gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * @param WIDTH
     * @param FRAME_DESIGN
     * @param FRAME_EFFECT
     * @param CUSTOM_FRAME_DESIGN
     * @return a buffered image that contains the frame image for a linear gauge
     */
    public BufferedImage createLinearFrame(final int WIDTH, final FrameDesign FRAME_DESIGN, final Paint CUSTOM_FRAME_DESIGN, final FrameEffect FRAME_EFFECT) {
        return createLinearFrame(WIDTH, WIDTH, FRAME_DESIGN, CUSTOM_FRAME_DESIGN, FRAME_EFFECT);
    }

    public BufferedImage createLinearFrame(final int WIDTH, final FrameDesign FRAME_DESIGN, final Paint CUSTOM_FRAME_DESIGN, final Color FRAME_BASECOLOR, final boolean FRAME_BASECOLOR_ENABLED, final FrameEffect FRAME_EFFECT) {
        return createLinearFrame(WIDTH, WIDTH, FRAME_DESIGN, CUSTOM_FRAME_DESIGN, FRAME_BASECOLOR, FRAME_BASECOLOR_ENABLED, FRAME_EFFECT, null);
    }

    /**
     * Creates the frame image for a linear gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * @param WIDTH
     * @param HEIGHT
     * @param FRAME_DESIGN
     * @param FRAME_EFFECT
     * @param CUSTOM_FRAME_DESIGN
     * @return a buffered image that contains the frame image for a linear gauge
     */
    public BufferedImage createLinearFrame(final int WIDTH, final int HEIGHT, final FrameDesign FRAME_DESIGN, final Paint CUSTOM_FRAME_DESIGN, final FrameEffect FRAME_EFFECT) {
        return createLinearFrame(WIDTH, HEIGHT, FRAME_DESIGN, CUSTOM_FRAME_DESIGN, FRAME_EFFECT, null);
    }

    public BufferedImage createLinearFrame(final int WIDTH, final int HEIGHT, final FrameDesign FRAME_DESIGN, final Paint CUSTOM_FRAME_DESIGN, final FrameEffect FRAME_EFFECT, final BufferedImage BACKGROUND_IMAGE) {
        return createLinearFrame(WIDTH, HEIGHT, FRAME_DESIGN, CUSTOM_FRAME_DESIGN, new Color(179, 179, 179, 255), false, FRAME_EFFECT, BACKGROUND_IMAGE);
    }

    /**
     * Creates the frame image for a linear gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * If an image is passed to the method, it will paint to the image and
     * return this image. This will reduce the memory consumption.
     * @param WIDTH
     * @param HEIGHT
     * @param FRAME_DESIGN
     * @param FRAME_EFFECT
     * @param FRAME_BASECOLOR
     * @param FRAME_BASECOLOR_ENABLED
     * @param CUSTOM_FRAME_DESIGN
     * @param BACKGROUND_IMAGE
     * @return a buffered image that contains the frame image for a linear gauge
     */
    public BufferedImage createLinearFrame(final int WIDTH, final int HEIGHT, final FrameDesign FRAME_DESIGN, final Paint CUSTOM_FRAME_DESIGN, final Color FRAME_BASECOLOR, final boolean FRAME_BASECOLOR_ENABLED, final FrameEffect FRAME_EFFECT, final BufferedImage BACKGROUND_IMAGE) {
        if (WIDTH <= 2 || HEIGHT <= 2) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        // Take image from cache instead of creating a new one if parameters are the same as last time
        if (linWidth == WIDTH && linHeight == HEIGHT && linFrameDesign == FRAME_DESIGN && linFrameEffect == FRAME_EFFECT && linCustomFrame.equals(CUSTOM_FRAME_DESIGN) && linFrameBaseColor.equals(FRAME_BASECOLOR) && linFrameBaseColorEnabled == FRAME_BASECOLOR_ENABLED) {
            if (BACKGROUND_IMAGE != null) {
                final Graphics2D G2 = BACKGROUND_IMAGE.createGraphics();
                G2.drawImage(linFrameImage, 0, 0, null);
                G2.dispose();
            }
            return linFrameImage;
        }

        linFrameImage.flush();
        linFrameImage = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = linFrameImage.createGraphics();

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

        final Area OUTER_FRAME = new Area(new RoundRectangle2D.Double(0.0, 0.0, IMAGE_WIDTH, IMAGE_HEIGHT, OUTER_FRAME_CORNER_RADIUS, OUTER_FRAME_CORNER_RADIUS));
        G2.setPaint(outerFrameColor);
        // The outer frame will be painted later because first we have to subtract the inner background

        final double FRAME_MAIN_CORNER_RADIUS;
        if (IMAGE_WIDTH >= IMAGE_HEIGHT) {
            FRAME_MAIN_CORNER_RADIUS = OUTER_FRAME_CORNER_RADIUS - ((OUTER_FRAME.getBounds2D().getHeight() - IMAGE_HEIGHT - 2) / 2.0);
        } else {
            FRAME_MAIN_CORNER_RADIUS = OUTER_FRAME_CORNER_RADIUS - ((OUTER_FRAME.getBounds2D().getWidth() - IMAGE_WIDTH - 2) / 2.0);
        }
        final Area FRAME_MAIN = new Area(new RoundRectangle2D.Double(1.0, 1.0, IMAGE_WIDTH - 2, IMAGE_HEIGHT - 2, FRAME_MAIN_CORNER_RADIUS, FRAME_MAIN_CORNER_RADIUS));
        final Point2D FRAME_MAIN_START = new Point2D.Double(0, FRAME_MAIN.getBounds2D().getMinY());
        final Point2D FRAME_MAIN_STOP = new Point2D.Double(0, FRAME_MAIN.getBounds2D().getMaxY());
        final Point2D FRAME_MAIN_CENTER = new Point2D.Double(FRAME_MAIN.getBounds2D().getCenterX(), FRAME_MAIN.getBounds2D().getCenterY());

        // Create shape that needs to be subtracted from rectangles
        final double SUBTRACT_CORNER_RADIUS;
        if (IMAGE_WIDTH >= IMAGE_HEIGHT) {
            SUBTRACT_CORNER_RADIUS = IMAGE_HEIGHT * 0.02857143;
        } else {
            SUBTRACT_CORNER_RADIUS = IMAGE_WIDTH * 0.02857143;
        }
        final Area SUBTRACT = new Area(new RoundRectangle2D.Double(FRAME_MAIN.getBounds2D().getX() + 16, FRAME_MAIN.getBounds2D().getY() + 16, FRAME_MAIN.getBounds2D().getWidth() - 32, FRAME_MAIN.getBounds2D().getHeight() - 32, SUBTRACT_CORNER_RADIUS, SUBTRACT_CORNER_RADIUS));

        // Paint outer frame after we subtracted the inner background shape
        OUTER_FRAME.subtract(SUBTRACT);
        G2.fill(OUTER_FRAME);

        final float ANGLE_OFFSET = (float) Math.toDegrees(Math.atan((IMAGE_HEIGHT / 8.0f) / (IMAGE_WIDTH / 2.0f)));
        if (CUSTOM_FRAME_DESIGN != null && FRAME_DESIGN == org.mars_sim.msp.ui.steelseries.tools.FrameDesign.CUSTOM) {
            G2.setPaint(CUSTOM_FRAME_DESIGN);
            FRAME_MAIN.subtract(SUBTRACT);
            G2.fill(FRAME_MAIN);
        } else {
            switch (FRAME_DESIGN) {
                case BLACK_METAL:
                    float[] frameMainFractions1 = {
                        0.0f,
                        90.0f - 2 * ANGLE_OFFSET,
                        90.0f,
                        90.0f + 3 * ANGLE_OFFSET,
                        180.0f,
                        270.0f - 3 * ANGLE_OFFSET,
                        270.0f,
                        270.0f + 2 * ANGLE_OFFSET,
                        1.0f
                    };

                    Color[] frameMainColors1 = {
                        new Color(254, 254, 254, 255),
                        new Color(0, 0, 0, 255),
                        new Color(153, 153, 153, 255),
                        new Color(0, 0, 0, 255),
                        new Color(0, 0, 0, 255),
                        new Color(0, 0, 0, 255),
                        new Color(153, 153, 153, 255),
                        new Color(0, 0, 0, 255),
                        new Color(254, 254, 254, 255)
                    };

                    Paint frameMainGradient1 = new ConicalGradientPaint(true, FRAME_MAIN_CENTER, 0, frameMainFractions1, frameMainColors1);
                    G2.setPaint(frameMainGradient1);
                    FRAME_MAIN.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN);
                    break;

                case METAL:
                    float[] frameMainFractions2 = {
                        0.0f,
                        0.07f,
                        0.12f,
                        1.0f
                    };

                    Color[] frameMainColors2 = {
                        new Color(254, 254, 254, 255),
                        new Color(210, 210, 210, 255),
                        new Color(179, 179, 179, 255),
                        new Color(213, 213, 213, 255)
                    };

                    Paint frameMainGradient2 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions2, frameMainColors2);
                    G2.setPaint(frameMainGradient2);
                    FRAME_MAIN.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN);
                    break;

                case SHINY_METAL:
                    float[] frameMainFractions3 = {
                        0.0f,
                        90.0f - 2 * ANGLE_OFFSET,
                        90.0f,
                        90.0f + 3 * ANGLE_OFFSET,
                        180.0f,
                        270.0f - 3 * ANGLE_OFFSET,
                        270.0f,
                        270.0f + 2 * ANGLE_OFFSET,
                        1.0f
                    };
                    Color[] frameMainColors3;
                    if (FRAME_BASECOLOR_ENABLED) {
                        frameMainColors3 = new Color[]{
                            new Color(254, 254, 254, 255),
                            new Color(FRAME_BASECOLOR.getRed(), FRAME_BASECOLOR.getGreen(), FRAME_BASECOLOR.getBlue(), 255),
                            new Color(FRAME_BASECOLOR.brighter().brighter().getRed(), FRAME_BASECOLOR.brighter().brighter().getGreen(), FRAME_BASECOLOR.brighter().brighter().getBlue(), 255),
                            new Color(FRAME_BASECOLOR.getRed(), FRAME_BASECOLOR.getGreen(), FRAME_BASECOLOR.getBlue(), 255),
                            new Color(FRAME_BASECOLOR.getRed(), FRAME_BASECOLOR.getGreen(), FRAME_BASECOLOR.getBlue(), 255),
                            new Color(FRAME_BASECOLOR.getRed(), FRAME_BASECOLOR.getGreen(), FRAME_BASECOLOR.getBlue(), 255),
                            new Color(FRAME_BASECOLOR.brighter().brighter().getRed(), FRAME_BASECOLOR.brighter().brighter().getGreen(), FRAME_BASECOLOR.brighter().brighter().getBlue(), 255),
                            new Color(FRAME_BASECOLOR.getRed(), FRAME_BASECOLOR.getGreen(), FRAME_BASECOLOR.getBlue(), 255),
                            new Color(254, 254, 254, 255)
                        };
                    } else {
                        frameMainColors3 = new Color[]{
                            new Color(254, 254, 254, 255),
                            new Color(179, 179, 179, 255),
                            new Color(238, 238, 238, 255),
                            new Color(179, 179, 179, 255),
                            new Color(179, 179, 179, 255),
                            new Color(179, 179, 179, 255),
                            new Color(238, 238, 238, 255),
                            new Color(179, 179, 179, 255),
                            new Color(254, 254, 254, 255)
                        };
                    }
                    Paint frameMainGradient3 = new ConicalGradientPaint(true, FRAME_MAIN_CENTER, 0, frameMainFractions3, frameMainColors3);
                    G2.setPaint(frameMainGradient3);
                    FRAME_MAIN.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN);
                    break;

                case GLOSSY_METAL:
                    // The smaller side is important for the contour gradient
                    float relFrameSize;
                    if (WIDTH >= HEIGHT) {
                        relFrameSize = (32f / HEIGHT);
                    } else {
                        relFrameSize = (32f / WIDTH);
                    }
                    float [] frameMainFractions4 = {
                        0.0f,
                        relFrameSize * 0.04f,
                        1.0f
                    };
                    Color[] frameMainColors4 = {
                        new Color(0.9568627451f, 0.9568627451f, 0.9568627451f, 1f),
                        new Color(0.8117647059f, 0.8117647059f, 0.8117647059f, 1f),
                        new Color(0.8117647059f, 0.8117647059f, 0.8117647059f, 1f)
                    };
                    Paint frameMainGradient4 = new ContourGradientPaint(OUTER_FRAME.getBounds2D(), frameMainFractions4, frameMainColors4);
                    G2.setPaint(frameMainGradient4);
                    G2.fill(FRAME_MAIN);

                    final Area FRAME_MAIN_GLOSSY2 = new Area(new RoundRectangle2D.Double(2, 2, IMAGE_WIDTH - 4, IMAGE_HEIGHT - 4, FRAME_MAIN_CORNER_RADIUS, FRAME_MAIN_CORNER_RADIUS));
                    FRAME_MAIN_GLOSSY2.subtract(SUBTRACT);
                    G2.setPaint(new LinearGradientPaint(new Point2D.Double(0, FRAME_MAIN_GLOSSY2.getBounds2D().getMinY()), new Point2D.Double(0, FRAME_MAIN_GLOSSY2.getBounds2D().getMaxY()), new float[]{0.0f, 0.1f, 0.26f, 0.73f, 1.0f}, new Color[]{new Color(0.9764705882f, 0.9764705882f, 0.9764705882f, 1f), new Color(0.7843137255f, 0.7647058824f, 0.7490196078f, 1f), new Color(1f, 1f, 1f, 1f), new Color(0.1137254902f, 0.1137254902f, 0.1137254902f, 1f), new Color(0.8196078431f, 0.8196078431f, 0.8196078431f, 1f)}));
                    G2.fill(FRAME_MAIN_GLOSSY2);

                    final Area FRAME_MAIN_GLOSSY3 = new Area(new RoundRectangle2D.Double(15, 15, IMAGE_WIDTH - 30, IMAGE_HEIGHT - 30, SUBTRACT_CORNER_RADIUS, SUBTRACT_CORNER_RADIUS));
                    FRAME_MAIN_GLOSSY3.subtract(SUBTRACT);
                    G2.setPaint(new Color(0xf6f6f6));
                    G2.fill(FRAME_MAIN_GLOSSY3);

                    final Area FRAME_MAIN_GLOSSY4 = new Area(new RoundRectangle2D.Double(16, 16, IMAGE_WIDTH - 32, IMAGE_HEIGHT - 32, SUBTRACT_CORNER_RADIUS, SUBTRACT_CORNER_RADIUS));
                    FRAME_MAIN_GLOSSY4.subtract(SUBTRACT);
                    G2.setPaint(new Color(0x333333));
                    G2.fill(FRAME_MAIN_GLOSSY4);
                    break;

                case BRASS:
                    float[] frameMainFractions5 = {
                        0.0f,
                        0.05f,
                        0.10f,
                        0.50f,
                        0.90f,
                        0.95f,
                        1.0f
                    };

                    Color[] frameMainColors5 = {
                        new Color(249, 243, 155, 255),
                        new Color(246, 226, 101, 255),
                        new Color(240, 225, 132, 255),
                        new Color(90, 57, 22, 255),
                        new Color(249, 237, 139, 255),
                        new Color(243, 226, 108, 255),
                        new Color(202, 182, 113, 255)
                    };
                    Paint frameMainGradient5 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions5, frameMainColors5);
                    G2.setPaint(frameMainGradient5);
                    FRAME_MAIN.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN);
                    break;

                case STEEL:
                    float[] frameMainFractions6 = {
                        0.0f,
                        0.05f,
                        0.10f,
                        0.50f,
                        0.90f,
                        0.95f,
                        1.0f
                    };

                    Color[] frameMainColors6 = {
                        new Color(231, 237, 237, 255),
                        new Color(189, 199, 198, 255),
                        new Color(192, 201, 200, 255),
                        new Color(23, 31, 33, 255),
                        new Color(196, 205, 204, 255),
                        new Color(194, 204, 203, 255),
                        new Color(189, 201, 199, 255)
                    };
                    Paint frameMainGradient6 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions6, frameMainColors6);
                    G2.setPaint(frameMainGradient6);
                    FRAME_MAIN.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN);
                    break;

                case CHROME:
                    float[] frameMainFractions7 = {
                        0.0f,
                        0.09f,
                        0.12f,
                        0.16f,
                        0.25f,
                        0.29f,
                        0.33f,
                        0.38f,
                        0.48f,
                        0.52f,
                        0.63f,
                        0.68f,
                        0.8f,
                        0.83f,
                        0.87f,
                        0.97f,
                        1.0f
                    };

                    Color[] frameMainColors7 = {
                        new Color(255, 255, 255, 255),
                        new Color(255, 255, 255, 255),
                        new Color(136, 136, 138, 255),
                        new Color(164, 185, 190, 255),
                        new Color(158, 179, 182, 255),
                        new Color(112, 112, 112, 255),
                        new Color(221, 227, 227, 255),
                        new Color(155, 176, 179, 255),
                        new Color(156, 176, 177, 255),
                        new Color(254, 255, 255, 255),
                        new Color(255, 255, 255, 255),
                        new Color(156, 180, 180, 255),
                        new Color(198, 209, 211, 255),
                        new Color(246, 248, 247, 255),
                        new Color(204, 216, 216, 255),
                        new Color(164, 188, 190, 255),
                        new Color(255, 255, 255, 255)
                    };

                    Paint frameMainGradient7 = new ConicalGradientPaint(false, FRAME_MAIN_CENTER, 0, frameMainFractions7, frameMainColors7);
                    G2.setPaint(frameMainGradient7);
                    FRAME_MAIN.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN);
                    break;

                case GOLD:
                    float[] frameMainFractions8 = {
                        0.0f,
                        0.15f,
                        0.22f,
                        0.3f,
                        0.38f,
                        0.44f,
                        0.51f,
                        0.6f,
                        0.68f,
                        0.75f,
                        1.0f
                    };

                    Color[] frameMainColors8 = {
                        new Color(255, 255, 207, 255),
                        new Color(255, 237, 96, 255),
                        new Color(254, 199, 57, 255),
                        new Color(255, 249, 203, 255),
                        new Color(255, 199, 64, 255),
                        new Color(252, 194, 60, 255),
                        new Color(255, 204, 59, 255),
                        new Color(213, 134, 29, 255),
                        new Color(255, 201, 56, 255),
                        new Color(212, 135, 29, 255),
                        new Color(247, 238, 101, 255)
                    };
                    Paint frameMainGradient8 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions8, frameMainColors8);
                    G2.setPaint(frameMainGradient8);
                    FRAME_MAIN.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN);
                    break;

                case ANTHRACITE:
                    float[] frameMainFractions9 = {
                        0.0f,
                        0.06f,
                        0.12f,
                        1.0f
                    };

                    Color[] frameMainColors9 = {
                        new Color(118, 117, 135, 255),
                        new Color(74, 74, 82, 255),
                        new Color(50, 50, 54, 255),
                        new Color(97, 97, 108, 255)
                    };

                    Paint frameMainGradient9 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions9, frameMainColors9);
                    G2.setPaint(frameMainGradient9);
                    FRAME_MAIN.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN);
                    break;

                case TILTED_GRAY:
                    FRAME_MAIN_START.setLocation((0.08571428571428572 * IMAGE_WIDTH), (0.0 * IMAGE_HEIGHT));
                    FRAME_MAIN_STOP.setLocation(((0.08571428571428572 + 0.7436714214664596) * IMAGE_WIDTH), ((0.0 + 0.9868853088441548) * IMAGE_HEIGHT));
                    float[] frameMainFractions10 = {
                        0.0f,
                        0.07f,
                        0.16f,
                        0.33f,
                        0.55f,
                        0.79f,
                        1.0f
                    };
                    Color[] frameMainColors10 = {
                        new Color(255, 255, 255, 255),
                        new Color(210, 210, 210, 255),
                        new Color(179, 179, 179, 255),
                        new Color(255, 255, 255, 255),
                        new Color(197, 197, 197, 255),
                        new Color(255, 255, 255, 255),
                        new Color(102, 102, 102, 255)
                    };
                    Paint frameMainGradient10 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions10, frameMainColors10);
                    G2.setPaint(frameMainGradient10);
                    FRAME_MAIN.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN);
                    break;

                case TILTED_BLACK:
                    FRAME_MAIN_START.setLocation((0.08571428571428572 * IMAGE_WIDTH), (0.014285714285714285 * IMAGE_HEIGHT));
                    FRAME_MAIN_STOP.setLocation(((0.08571428571428572 + 0.6496765631964967) * IMAGE_WIDTH), ((0.014285714285714285 + 1.0004141774777557) * IMAGE_HEIGHT));
                    float[] frameMainFractions11 = {
                        0.0f,
                        0.21f,
                        0.47f,
                        1.0f
                    };
                    Color[] frameMainColors11 = {
                        new Color(102, 102, 102, 255),
                        new Color(0, 0, 0, 255),
                        new Color(102, 102, 102, 255),
                        new Color(0, 0, 0, 255)
                    };
                    Paint frameMainGradient11 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions11, frameMainColors11);
                    G2.setPaint(frameMainGradient11);
                    FRAME_MAIN.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN);
                    break;

                default:
                    float[] frameMainFractions = {
                        0.0f,
                        0.07f,
                        0.12f,
                        1.0f
                    };

                    Color[] frameMainColors = {
                        new Color(254, 254, 254, 255),
                        new Color(210, 210, 210, 255),
                        new Color(179, 179, 179, 255),
                        new Color(213, 213, 213, 255)
                    };

                    Paint frameMainGradient = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions, frameMainColors);
                    G2.setPaint(frameMainGradient);
                    FRAME_MAIN.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN);
                    break;
            }
        }


        final float[] EFFECT_FRACTIONS;
        final Color[] EFFECT_COLORS;
        final java.awt.Paint EFFECT_GRADIENT;
        switch (FRAME_EFFECT) {
            case NONE:

            default:
                break;

            case EFFECT_BULGE:
                final float relFrameSize;
                // The smaller side is important for the contour gradient
                if (WIDTH >= HEIGHT) {
                    relFrameSize = (32f / HEIGHT);
                } else {
                    relFrameSize = (32f / WIDTH);
                }
                EFFECT_FRACTIONS = new float[]{
                    0.0f,
                    relFrameSize * 0.87f,
                    relFrameSize * 0.86f,
                    relFrameSize * 0.83f,
                    relFrameSize,
                    relFrameSize * 1.01f,
                    1.0f
                };
                EFFECT_COLORS = new Color[]{
                    new Color(0, 0, 0, 102),
                    new Color(255, 255, 255, 151),
                    new Color(219, 219, 219, 153),
                    new Color(219, 219, 219, 153),
                    new Color(36, 36, 36, 76),
                    new Color(0, 0, 0, 95),
                    new Color(0, 0, 0, 0)
                };
                EFFECT_GRADIENT = new ContourGradientPaint(OUTER_FRAME.getBounds2D(), EFFECT_FRACTIONS, EFFECT_COLORS);
                G2.setPaint(EFFECT_GRADIENT);
                G2.fill(OUTER_FRAME);
                break;

            case EFFECT_CONE:
                // The smaller side is important for the contour gradient

                if (WIDTH >= HEIGHT) {
                    relFrameSize = (32f / HEIGHT);
                } else {
                    relFrameSize = (32f / WIDTH);
                }
                EFFECT_FRACTIONS = new float[]{
                    0.0f,
                    relFrameSize * 0.1f,
                    //relFrameSize * 0.2f,
                    relFrameSize * 0.3f,
                    //relFrameSize * 0.4f,
                    //relFrameSize * 0.5f,
                    //relFrameSize * 0.6f,
                    //relFrameSize * 0.7f,
                    //relFrameSize * 0.8f,
                    //relFrameSize * 0.9f,
                    relFrameSize,
                    1.0f
                };
                EFFECT_COLORS = new Color[]{
                    //Color.BLUE,     // 0.0f                   Outer border of frame
                    //Color.RED,      // 0.1f * relFrameSize
                    //Color.WHITE,    // 0.2f * relFrameSize
                    //Color.BLACK,    // 0.3f * relFrameSize
                    //Color.YELLOW,   // 0.4f * relFrameSize
                    //Color.MAGENTA,  // 0.5f * relFrameSize
                    //Color.CYAN,     // 0.6f * relFrameSize
                    //Color.GREEN,    // 0.7f * relFrameSize
                    //Color.GRAY,     // 0.8f * relFrameSize
                    //Color.BLUE,     // 0.9f * relFrameSize
                    //Color.RED,      // 1.0f * relFrameSize    Inner border of frame
                    //Color.WHITE,    // 1.0f

                    new Color(0f, 0f, 0f, 0.3f),
                    new Color(0f, 0f, 0f, 0.3f),
                    new Color(1f, 1f, 1f, 0.5f),
                    new Color(0f, 0f, 0f, 0.2f),
                    new Color(0f, 0f, 0f, 0f)
                };
                EFFECT_GRADIENT = new ContourGradientPaint(OUTER_FRAME.getBounds2D(), EFFECT_FRACTIONS, EFFECT_COLORS);
                G2.setPaint(EFFECT_GRADIENT);
                G2.fill(OUTER_FRAME);
                break;

            case EFFECT_TORUS:
                // The smaller side is important for the contour gradient
                if (WIDTH >= HEIGHT) {
                    relFrameSize = (32f / HEIGHT);
                } else {
                    relFrameSize = (32f / WIDTH);
                }
                EFFECT_FRACTIONS = new float[]{
                    0.0f,
                    relFrameSize * 0.1f,
                    relFrameSize * 0.5f,
                    relFrameSize,
                    1.0f
                };
                EFFECT_COLORS = new Color[]{
                    new Color(0f, 0f, 0f, 0.3f),
                    new Color(0f, 0f, 0f, 0.3f),
                    new Color(1f, 1f, 1f, 0.5f),
                    new Color(0f, 0f, 0f, 0.2f),
                    new Color(0f, 0f, 0f, 0f)
                };
                EFFECT_GRADIENT = new ContourGradientPaint(OUTER_FRAME.getBounds2D(), EFFECT_FRACTIONS, EFFECT_COLORS);
                G2.setPaint(EFFECT_GRADIENT);
                G2.fill(OUTER_FRAME);
                break;

            case EFFECT_INNER_FRAME:
                final RoundRectangle2D EFFECT_BIGINNERFRAME = new RoundRectangle2D.Double(10, 10, IMAGE_WIDTH - 20, IMAGE_HEIGHT - 20, 10.0, 10.0);
                final Point2D EFFECT_BIGINNERFRAME_START = new Point2D.Double(0, EFFECT_BIGINNERFRAME.getBounds2D().getMinY());
                final Point2D EFFECT_BIGINNERFRAME_STOP = new Point2D.Double(0, EFFECT_BIGINNERFRAME.getBounds2D().getMaxY());
                final float[] EFFECT_BIGINNERFRAME_FRACTIONS = {
                    0.0f,
                    0.13f,
                    0.45f,
                    0.92f,
                    1.0f
                };
                final Color[] EFFECT_BIGINNERFRAME_COLORS = {
                    new Color(0, 0, 0, 183),
                    new Color(0, 0, 0, 25),
                    new Color(0, 0, 0, 160),
                    new Color(0, 0, 0, 80),
                    new Color(255, 255, 255, 158)
                };
                final LinearGradientPaint EFFECT_BIGINNERFRAME_GRADIENT = new LinearGradientPaint(EFFECT_BIGINNERFRAME_START, EFFECT_BIGINNERFRAME_STOP, EFFECT_BIGINNERFRAME_FRACTIONS, EFFECT_BIGINNERFRAME_COLORS);
                G2.setPaint(EFFECT_BIGINNERFRAME_GRADIENT);
                G2.fill(EFFECT_BIGINNERFRAME);
                break;
        }

        final double INNER_FRAME_CORNER_RADIUS;
        if (IMAGE_WIDTH >= IMAGE_HEIGHT) {
            INNER_FRAME_CORNER_RADIUS = IMAGE_HEIGHT * 0.02857143;
        } else {
            INNER_FRAME_CORNER_RADIUS = IMAGE_WIDTH * 0.02857143;
        }

        final Area INNER_FRAME = new Area(new java.awt.geom.RoundRectangle2D.Double(FRAME_MAIN.getBounds2D().getX() + 16, FRAME_MAIN.getBounds2D().getY() + 16, FRAME_MAIN.getBounds2D().getWidth() - 32, FRAME_MAIN.getBounds2D().getHeight() - 32, INNER_FRAME_CORNER_RADIUS, INNER_FRAME_CORNER_RADIUS));
        G2.setPaint(innerFrameColor);

        INNER_FRAME.subtract(SUBTRACT);
        G2.fill(INNER_FRAME);

        G2.dispose();

        if (BACKGROUND_IMAGE != null) {
            final Graphics2D G = BACKGROUND_IMAGE.createGraphics();
            G.drawImage(linFrameImage, 0, 0, null);
            G.dispose();
        }

        // Cache current parameters
        linWidth = WIDTH;
        linHeight = HEIGHT;
        linFrameDesign = FRAME_DESIGN;
        linFrameEffect = FRAME_EFFECT;
        linCustomFrame = CUSTOM_FRAME_DESIGN;
        linFrameBaseColor = FRAME_BASECOLOR;
        linFrameBaseColorEnabled = FRAME_BASECOLOR_ENABLED;

        return linFrameImage;
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "FrameImageFactory";
    }
}
