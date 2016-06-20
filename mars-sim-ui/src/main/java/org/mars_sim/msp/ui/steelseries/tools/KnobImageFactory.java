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
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.Transparency;
import java.awt.geom.Path2D;


/**
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public enum KnobImageFactory {

    INSTANCE;
    private final Util UTIL = Util.INSTANCE;
    private int sizeBuffer = 0;
    private KnobType knobTypeBuffer = KnobType.SMALL_STD_KNOB;
    private KnobStyle knobStyleBuffer = KnobStyle.SILVER;
    private BufferedImage knobImageBuffer = UTIL.createImage(1, 1, Transparency.TRANSLUCENT);

    /**
     * Creates a single alignment post image that could be placed on all the positions where it is needed
     * @param SIZE
     * @param KNOB_TYPE
     * @param KNOB_STYLE
     * @return a buffered image that contains a single alignment post of the given type
     */
    public BufferedImage create_KNOB_Image(final int SIZE, final KnobType KNOB_TYPE, final KnobStyle KNOB_STYLE) {
        if (SIZE <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        if (sizeBuffer == SIZE && knobTypeBuffer == KNOB_TYPE && knobStyleBuffer == KNOB_STYLE) {
            return knobImageBuffer;
        }

        knobImageBuffer.flush();
        knobImageBuffer = UTIL.createImage(SIZE, SIZE, Transparency.TRANSLUCENT);
        final Graphics2D G2 = knobImageBuffer.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = knobImageBuffer.getWidth();
        final int IMAGE_HEIGHT = knobImageBuffer.getHeight();

        switch (KNOB_TYPE) {
            case SMALL_STD_KNOB:
                final Ellipse2D POST_FRAME = new Ellipse2D.Double(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
                final Point2D POST_FRAME_START = new Point2D.Double(0, POST_FRAME.getBounds2D().getMinY());
                final Point2D POST_FRAME_STOP = new Point2D.Double(0, POST_FRAME.getBounds2D().getMaxY());
                final float[] POST_FRAME_FRACTIONS = {
                    0.0f,
                    0.46f,
                    1.0f
                };
                final Color[] POST_FRAME_COLORS = {
                    new Color(180, 180, 180, 255),
                    new Color(63, 63, 63, 255),
                    new Color(40, 40, 40, 255)
                };

                final LinearGradientPaint POST_FRAME_GRADIENT = new LinearGradientPaint(POST_FRAME_START, POST_FRAME_STOP, POST_FRAME_FRACTIONS, POST_FRAME_COLORS);
                G2.setPaint(POST_FRAME_GRADIENT);
                G2.fill(POST_FRAME);

                final Ellipse2D POST_MAIN = new Ellipse2D.Double((IMAGE_WIDTH - IMAGE_WIDTH * 0.77) / 2.0, (IMAGE_WIDTH - IMAGE_WIDTH * 0.77) / 2.0, IMAGE_WIDTH * 0.77, IMAGE_WIDTH * 0.77);
                final Point2D POST_MAIN_START = new Point2D.Double(0, POST_MAIN.getBounds2D().getMinY());
                final Point2D POST_MAIN_STOP = new Point2D.Double(0, POST_MAIN.getBounds2D().getMaxY());
                final float[] POST_MAIN_FRACTIONS = {
                    0.0f,
                    0.5f,
                    1.0f
                };

                final Color[] POST_MAIN_COLORS;
                switch (KNOB_STYLE) {
                    case BLACK:
                        POST_MAIN_COLORS = new Color[]{
                            new Color(0xBFBFBF),
                            new Color(0x2B2A2F),
                            new Color(0x7D7E80)
                        };
                        break;

                    case BRASS:
                        POST_MAIN_COLORS = new Color[]{
                            new Color(0xDFD0AE),
                            new Color(0x7A5E3E),
                            new Color(0xCFBE9D)
                        };
                        break;

                    case SILVER:

                    default:
                        POST_MAIN_COLORS = new Color[]{
                            new Color(0xD7D7D7),
                            new Color(0x747474),
                            new Color(0xD7D7D7)
                        };
                        break;
                }
                final LinearGradientPaint POST_MAIN_GRADIENT = new LinearGradientPaint(POST_MAIN_START, POST_MAIN_STOP, POST_MAIN_FRACTIONS, POST_MAIN_COLORS);
                G2.setPaint(POST_MAIN_GRADIENT);
                G2.fill(POST_MAIN);

                final Ellipse2D POST_INNERSHADOW = new Ellipse2D.Double((IMAGE_WIDTH - IMAGE_WIDTH * 0.77) / 2.0, (IMAGE_WIDTH - IMAGE_WIDTH * 0.77) / 2.0, IMAGE_WIDTH * 0.77, IMAGE_WIDTH * 0.77);
                final Point2D POST_INNERSHADOW_CENTER = new Point2D.Double(POST_INNERSHADOW.getCenterX(), POST_INNERSHADOW.getCenterY());
                final float[] POST_INNERSHADOW_FRACTIONS = {
                    0.0f,
                    0.75f,
                    0.76f,
                    1.0f
                };
                final Color[] POST_INNERSHADOW_COLORS = {
                    new Color(0, 0, 0, 0),
                    new Color(0, 0, 0, 0),
                    new Color(0, 0, 0, 1),
                    new Color(0, 0, 0, 51)
                };
                final RadialGradientPaint POST_INNERSHADOW_GRADIENT = new RadialGradientPaint(POST_INNERSHADOW_CENTER, (float) (POST_INNERSHADOW.getWidth() / 2.0), POST_INNERSHADOW_FRACTIONS, POST_INNERSHADOW_COLORS);
                G2.setPaint(POST_INNERSHADOW_GRADIENT);
                G2.fill(POST_INNERSHADOW);
                break;

            case BIG_STD_KNOB:
                final Ellipse2D BIGCENTER_BACKGROUNDFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4392523467540741, IMAGE_HEIGHT * 0.4392523467540741, IMAGE_WIDTH * 0.1214953362941742, IMAGE_HEIGHT * 0.1214953362941742);
                final Point2D BIGCENTER_BACKGROUNDFRAME_START = new Point2D.Double(0, BIGCENTER_BACKGROUNDFRAME.getBounds2D().getMinY());
                final Point2D BIGCENTER_BACKGROUNDFRAME_STOP = new Point2D.Double(0, BIGCENTER_BACKGROUNDFRAME.getBounds2D().getMaxY());
                final float[] BIGCENTER_BACKGROUNDFRAME_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] BIGCENTER_BACKGROUNDFRAME_COLORS;
                switch (KNOB_STYLE) {
                    case BLACK:
                        BIGCENTER_BACKGROUNDFRAME_COLORS = new Color[]{
                            new Color(129, 133, 136, 255),
                            new Color(61, 61, 73, 255)
                        };
                        break;

                    case BRASS:
                        BIGCENTER_BACKGROUNDFRAME_COLORS = new Color[]{
                            new Color(143, 117, 80, 255),
                            new Color(100, 76, 49, 255)
                        };
                        break;

                    case SILVER:

                    default:
                        BIGCENTER_BACKGROUNDFRAME_COLORS = new Color[]{
                            new Color(152, 152, 152, 255),
                            new Color(118, 121, 126, 255)
                        };
                        break;
                }
                final LinearGradientPaint BIGCENTER_BACKGROUNDFRAME_GRADIENT = new LinearGradientPaint(BIGCENTER_BACKGROUNDFRAME_START, BIGCENTER_BACKGROUNDFRAME_STOP, BIGCENTER_BACKGROUNDFRAME_FRACTIONS, BIGCENTER_BACKGROUNDFRAME_COLORS);
                G2.setPaint(BIGCENTER_BACKGROUNDFRAME_GRADIENT);
                G2.fill(BIGCENTER_BACKGROUNDFRAME);

                final Ellipse2D BIGCENTER_BACKGROUND = new Ellipse2D.Double(IMAGE_WIDTH * 0.44392523169517517, IMAGE_HEIGHT * 0.44392523169517517, IMAGE_WIDTH * 0.11214950680732727, IMAGE_HEIGHT * 0.11214950680732727);
                final Point2D BIGCENTER_BACKGROUND_START = new Point2D.Double(0, BIGCENTER_BACKGROUND.getBounds2D().getMinY());
                final Point2D BIGCENTER_BACKGROUND_STOP = new Point2D.Double(0, BIGCENTER_BACKGROUND.getBounds2D().getMaxY());
                final float[] BIGCENTER_BACKGROUND_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] BIGCENTER_BACKGROUND_COLORS;
                switch (KNOB_STYLE) {
                    case BLACK:
                        BIGCENTER_BACKGROUND_COLORS = new Color[]{
                            new Color(26, 27, 32, 255),
                            new Color(96, 97, 102, 255)
                        };
                        break;

                    case BRASS:
                        BIGCENTER_BACKGROUND_COLORS = new Color[]{
                            new Color(98, 75, 49, 255),
                            new Color(149, 109, 54, 255)
                        };
                        break;

                    case SILVER:

                    default:
                        BIGCENTER_BACKGROUND_COLORS = new Color[]{
                            new Color(118, 121, 126, 255),
                            new Color(191, 191, 191, 255)
                        };
                        break;
                }
                final LinearGradientPaint BIGCENTER_BACKGROUND_GRADIENT = new LinearGradientPaint(BIGCENTER_BACKGROUND_START, BIGCENTER_BACKGROUND_STOP, BIGCENTER_BACKGROUND_FRACTIONS, BIGCENTER_BACKGROUND_COLORS);
                G2.setPaint(BIGCENTER_BACKGROUND_GRADIENT);
                G2.fill(BIGCENTER_BACKGROUND);

                final Ellipse2D BIGCENTER_FOREGROUNDFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4532710313796997, IMAGE_HEIGHT * 0.4532710313796997, IMAGE_WIDTH * 0.09345793724060059, IMAGE_HEIGHT * 0.09345793724060059);
                final Point2D BIGCENTER_FOREGROUNDFRAME_START = new Point2D.Double(0, BIGCENTER_FOREGROUNDFRAME.getBounds2D().getMinY());
                final Point2D BIGCENTER_FOREGROUNDFRAME_STOP = new Point2D.Double(0, BIGCENTER_FOREGROUNDFRAME.getBounds2D().getMaxY());
                final float[] BIGCENTER_FOREGROUNDFRAME_FRACTIONS = {
                    0.0f,
                    0.47f,
                    1.0f
                };
                final Color[] BIGCENTER_FOREGROUNDFRAME_COLORS;
                switch (KNOB_STYLE) {
                    case BLACK:
                        BIGCENTER_FOREGROUNDFRAME_COLORS = new Color[]{
                            new Color(191, 191, 191, 255),
                            new Color(56, 57, 61, 255),
                            new Color(143, 144, 146, 255)
                        };
                        break;

                    case BRASS:
                        BIGCENTER_FOREGROUNDFRAME_COLORS = new Color[]{
                            new Color(147, 108, 54, 255),
                            new Color(82, 66, 50, 255),
                            new Color(147, 108, 54, 255)
                        };
                        break;

                    case SILVER:

                    default:
                        BIGCENTER_FOREGROUNDFRAME_COLORS = new Color[]{
                            new Color(191, 191, 191, 255),
                            new Color(116, 116, 116, 255),
                            new Color(143, 144, 146, 255)
                        };
                        break;
                }
                final LinearGradientPaint BIGCENTER_FOREGROUNDFRAME_GRADIENT = new LinearGradientPaint(BIGCENTER_FOREGROUNDFRAME_START, BIGCENTER_FOREGROUNDFRAME_STOP, BIGCENTER_FOREGROUNDFRAME_FRACTIONS, BIGCENTER_FOREGROUNDFRAME_COLORS);
                G2.setPaint(BIGCENTER_FOREGROUNDFRAME_GRADIENT);
                G2.fill(BIGCENTER_FOREGROUNDFRAME);

                final Ellipse2D BIGCENTER_FOREGROUND = new Ellipse2D.Double(IMAGE_WIDTH * 0.4579439163208008, IMAGE_HEIGHT * 0.4579439163208008, IMAGE_WIDTH * 0.08411216735839844, IMAGE_HEIGHT * 0.08411216735839844);
                final Point2D BIGCENTER_FOREGROUND_START = new Point2D.Double(0, BIGCENTER_FOREGROUND.getBounds2D().getMinY());
                final Point2D BIGCENTER_FOREGROUND_STOP = new Point2D.Double(0, BIGCENTER_FOREGROUND.getBounds2D().getMaxY());
                final float[] BIGCENTER_FOREGROUND_FRACTIONS = {
                    0.0f,
                    0.21f,
                    0.5f,
                    0.78f,
                    1.0f
                };
                final Color[] BIGCENTER_FOREGROUND_COLORS;
                switch (KNOB_STYLE) {
                    case BLACK:
                        BIGCENTER_FOREGROUND_COLORS = new Color[]{
                            new Color(191, 191, 191, 255),
                            new Color(94, 93, 99, 255),
                            new Color(43, 42, 47, 255),
                            new Color(78, 79, 81, 255),
                            new Color(143, 144, 146, 255)
                        };
                        break;

                    case BRASS:
                        BIGCENTER_FOREGROUND_COLORS = new Color[]{
                            new Color(223, 208, 174, 255),
                            new Color(159, 136, 104, 255),
                            new Color(122, 94, 62, 255),
                            new Color(159, 136, 104, 255),
                            new Color(223, 208, 174, 255)
                        };
                        break;

                    case SILVER:

                    default:
                        BIGCENTER_FOREGROUND_COLORS = new Color[]{
                            new Color(215, 215, 215, 255),
                            new Color(139, 142, 145, 255),
                            new Color(100, 100, 100, 255),
                            new Color(139, 142, 145, 255),
                            new Color(215, 215, 215, 255)
                        };
                        break;
                }
                final LinearGradientPaint BIGCENTER_FOREGROUND_GRADIENT = new LinearGradientPaint(BIGCENTER_FOREGROUND_START, BIGCENTER_FOREGROUND_STOP, BIGCENTER_FOREGROUND_FRACTIONS, BIGCENTER_FOREGROUND_COLORS);
                G2.setPaint(BIGCENTER_FOREGROUND_GRADIENT);
                G2.fill(BIGCENTER_FOREGROUND);
                break;

            case BIG_CHROME_KNOB:
                final Ellipse2D CHROMEKNOB_BACKFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.42990654706954956, IMAGE_HEIGHT * 0.42990654706954956, IMAGE_WIDTH * 0.14018690586090088, IMAGE_HEIGHT * 0.14018690586090088);
                final Point2D CHROMEKNOB_BACKFRAME_START = new Point2D.Double((0.46261682242990654 * IMAGE_WIDTH), (0.4392523364485981 * IMAGE_HEIGHT));
                final Point2D CHROMEKNOB_BACKFRAME_STOP = new Point2D.Double(((0.46261682242990654 + 0.0718114890783315) * IMAGE_WIDTH), ((0.4392523364485981 + 0.1149224055539082) * IMAGE_HEIGHT));
                final float[] CHROMEKNOB_BACKFRAME_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] CHROMEKNOB_BACKFRAME_COLORS = {
                    new Color(129, 139, 140, 255),
                    new Color(166, 171, 175, 255)
                };
                final LinearGradientPaint CHROMEKNOB_BACKFRAME_GRADIENT = new LinearGradientPaint(CHROMEKNOB_BACKFRAME_START, CHROMEKNOB_BACKFRAME_STOP, CHROMEKNOB_BACKFRAME_FRACTIONS, CHROMEKNOB_BACKFRAME_COLORS);
                G2.setPaint(CHROMEKNOB_BACKFRAME_GRADIENT);
                G2.fill(CHROMEKNOB_BACKFRAME);

                final Ellipse2D CHROMEKNOB_BACK = new Ellipse2D.Double(IMAGE_WIDTH * 0.43457943201065063, IMAGE_HEIGHT * 0.43457943201065063, IMAGE_WIDTH * 0.13084113597869873, IMAGE_HEIGHT * 0.13084113597869873);
                final Point2D CHROMEKNOB_BACK_CENTER = new Point2D.Double(CHROMEKNOB_BACK.getCenterX(), CHROMEKNOB_BACK.getCenterY());
                final float[] CHROMEKNOB_BACK_FRACTIONS = {
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
                    0.65f,
                    0.69f,
                    0.8f,
                    0.83f,
                    0.87f,
                    0.97f,
                    1.0f
                };
                final Color[] CHROMEKNOB_BACK_COLORS = {
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
                final ConicalGradientPaint CHROMEKNOB_BACK_GRADIENT = new ConicalGradientPaint(false, CHROMEKNOB_BACK_CENTER, 0, CHROMEKNOB_BACK_FRACTIONS, CHROMEKNOB_BACK_COLORS);
                G2.setPaint(CHROMEKNOB_BACK_GRADIENT);
                G2.fill(CHROMEKNOB_BACK);

                final Ellipse2D CHROMEKNOB_FOREFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4672897160053253, IMAGE_HEIGHT * 0.4672897160053253, IMAGE_WIDTH * 0.06542053818702698, IMAGE_HEIGHT * 0.06542053818702698);
                final Point2D CHROMEKNOB_FOREFRAME_START = new Point2D.Double((0.48130841121495327 * IMAGE_WIDTH), (0.4719626168224299 * IMAGE_HEIGHT));
                final Point2D CHROMEKNOB_FOREFRAME_STOP = new Point2D.Double(((0.48130841121495327 + 0.033969662360372466) * IMAGE_WIDTH), ((0.4719626168224299 + 0.05036209552904459) * IMAGE_HEIGHT));
                final float[] CHROMEKNOB_FOREFRAME_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] CHROMEKNOB_FOREFRAME_COLORS = {
                    new Color(225, 235, 232, 255),
                    new Color(196, 207, 207, 255)
                };
                final LinearGradientPaint CHROMEKNOB_FOREFRAME_GRADIENT = new LinearGradientPaint(CHROMEKNOB_FOREFRAME_START, CHROMEKNOB_FOREFRAME_STOP, CHROMEKNOB_FOREFRAME_FRACTIONS, CHROMEKNOB_FOREFRAME_COLORS);
                G2.setPaint(CHROMEKNOB_FOREFRAME_GRADIENT);
                G2.fill(CHROMEKNOB_FOREFRAME);

                final Ellipse2D CHROMEKNOB_FORE = new Ellipse2D.Double(IMAGE_WIDTH * 0.4719626307487488, IMAGE_HEIGHT * 0.4719626307487488, IMAGE_WIDTH * 0.05607473850250244, IMAGE_HEIGHT * 0.05607473850250244);
                final Point2D CHROMEKNOB_FORE_START = new Point2D.Double((0.48130841121495327 * IMAGE_WIDTH), (0.4766355140186916 * IMAGE_HEIGHT));
                final Point2D CHROMEKNOB_FORE_STOP = new Point2D.Double(((0.48130841121495327 + 0.03135661140957459) * IMAGE_WIDTH), ((0.4766355140186916 + 0.04648808818065655) * IMAGE_HEIGHT));
                final float[] CHROMEKNOB_FORE_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] CHROMEKNOB_FORE_COLORS = {
                    new Color(237, 239, 237, 255),
                    new Color(148, 161, 161, 255)
                };
                final LinearGradientPaint CHROMEKNOB_FORE_GRADIENT = new LinearGradientPaint(CHROMEKNOB_FORE_START, CHROMEKNOB_FORE_STOP, CHROMEKNOB_FORE_FRACTIONS, CHROMEKNOB_FORE_COLORS);
                G2.setPaint(CHROMEKNOB_FORE_GRADIENT);
                G2.fill(CHROMEKNOB_FORE);
                break;

            case METAL_KNOB:
                final Ellipse2D METALKNOB_FRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4579439163208008, IMAGE_HEIGHT * 0.4579439163208008, IMAGE_WIDTH * 0.08411216735839844, IMAGE_HEIGHT * 0.08411216735839844);
                final Point2D METALKNOB_FRAME_START = new Point2D.Double(0, METALKNOB_FRAME.getBounds2D().getMinY());
                final Point2D METALKNOB_FRAME_STOP = new Point2D.Double(0, METALKNOB_FRAME.getBounds2D().getMaxY());
                final float[] METALKNOB_FRAME_FRACTIONS = {
                    0.0f,
                    0.47f,
                    1.0f
                };
                final Color[] METALKNOB_FRAME_COLORS = {
                    new Color(92, 95, 101, 255),
                    new Color(46, 49, 53, 255),
                    new Color(22, 23, 26, 255)
                };
                final LinearGradientPaint METALKNOB_FRAME_GRADIENT = new LinearGradientPaint(METALKNOB_FRAME_START, METALKNOB_FRAME_STOP, METALKNOB_FRAME_FRACTIONS, METALKNOB_FRAME_COLORS);
                G2.setPaint(METALKNOB_FRAME_GRADIENT);
                G2.fill(METALKNOB_FRAME);

                final Ellipse2D METALKNOB_MAIN = new Ellipse2D.Double(IMAGE_WIDTH * 0.46261683106422424, IMAGE_HEIGHT * 0.46261683106422424, IMAGE_WIDTH * 0.0747663676738739, IMAGE_HEIGHT * 0.0747663676738739);
                final Point2D METALKNOB_MAIN_START = new Point2D.Double(0, METALKNOB_MAIN.getBounds2D().getMinY());
                final Point2D METALKNOB_MAIN_STOP = new Point2D.Double(0, METALKNOB_MAIN.getBounds2D().getMaxY());
                final float[] METALKNOB_MAIN_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] METALKNOB_MAIN_COLORS;
                switch (KNOB_STYLE) {
                    case BLACK:
                        METALKNOB_MAIN_COLORS = new Color[]{
                            new Color(0x2B2A2F),
                            new Color(0x1A1B20)
                        };
                        break;

                    case BRASS:
                        METALKNOB_MAIN_COLORS = new Color[]{
                            new Color(0x966E36),
                            new Color(0x7C5F3D)
                        };
                        break;

                    case SILVER:

                    default:
                        METALKNOB_MAIN_COLORS = new Color[]{
                            new Color(204, 204, 204, 255),
                            new Color(87, 92, 98, 255)
                        };
                        break;
                }

                final LinearGradientPaint METALKNOB_MAIN_GRADIENT = new LinearGradientPaint(METALKNOB_MAIN_START, METALKNOB_MAIN_STOP, METALKNOB_MAIN_FRACTIONS, METALKNOB_MAIN_COLORS);
                G2.setPaint(METALKNOB_MAIN_GRADIENT);
                G2.fill(METALKNOB_MAIN);

                final GeneralPath METALKNOB_LOWERHL = new GeneralPath();
                METALKNOB_LOWERHL.setWindingRule(Path2D.WIND_EVEN_ODD);
                METALKNOB_LOWERHL.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5280373831775701);
                METALKNOB_LOWERHL.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.514018691588785);
                METALKNOB_LOWERHL.curveTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5280373831775701);
                METALKNOB_LOWERHL.curveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5373831775700935, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5373831775700935);
                METALKNOB_LOWERHL.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.5373831775700935, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5280373831775701);
                METALKNOB_LOWERHL.closePath();
                final Point2D METALKNOB_LOWERHL_CENTER = new Point2D.Double((0.5 * IMAGE_WIDTH), (0.5373831775700935 * IMAGE_HEIGHT));
                final float[] METALKNOB_LOWERHL_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] METALKNOB_LOWERHL_COLORS = {
                    new Color(255, 255, 255, 153),
                    new Color(255, 255, 255, 0)
                };
                final RadialGradientPaint METALKNOB_LOWERHL_GRADIENT = new RadialGradientPaint(METALKNOB_LOWERHL_CENTER, (float) (0.03271028037383177 * IMAGE_WIDTH), METALKNOB_LOWERHL_FRACTIONS, METALKNOB_LOWERHL_COLORS);
                G2.setPaint(METALKNOB_LOWERHL_GRADIENT);
                G2.fill(METALKNOB_LOWERHL);

                final GeneralPath METALKNOB_UPPERHL = new GeneralPath();
                METALKNOB_UPPERHL.setWindingRule(Path2D.WIND_EVEN_ODD);
                METALKNOB_UPPERHL.moveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.48130841121495327);
                METALKNOB_UPPERHL.curveTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.45794392523364486);
                METALKNOB_UPPERHL.curveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.46261682242990654, IMAGE_HEIGHT * 0.48130841121495327);
                METALKNOB_UPPERHL.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.48598130841121495, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.49065420560747663);
                METALKNOB_UPPERHL.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48598130841121495, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.48130841121495327);
                METALKNOB_UPPERHL.closePath();
                final Point2D METALKNOB_UPPERHL_CENTER = new Point2D.Double((0.4953271028037383 * IMAGE_WIDTH), (0.45794392523364486 * IMAGE_HEIGHT));
                final float[] METALKNOB_UPPERHL_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] METALKNOB_UPPERHL_COLORS = {
                    new Color(255, 255, 255, 191),
                    new Color(255, 255, 255, 0)
                };
                final RadialGradientPaint METALKNOB_UPPERHL_GRADIENT = new RadialGradientPaint(METALKNOB_UPPERHL_CENTER, (float) (0.04906542056074766 * IMAGE_WIDTH), METALKNOB_UPPERHL_FRACTIONS, METALKNOB_UPPERHL_COLORS);
                G2.setPaint(METALKNOB_UPPERHL_GRADIENT);
                G2.fill(METALKNOB_UPPERHL);

                final Ellipse2D METALKNOB_INNERFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.47663551568984985, IMAGE_HEIGHT * 0.4813084006309509, IMAGE_WIDTH * 0.04205608367919922, IMAGE_HEIGHT * 0.04205608367919922);
                final Point2D METALKNOB_INNERFRAME_START = new Point2D.Double(0, METALKNOB_INNERFRAME.getBounds2D().getMinY());
                final Point2D METALKNOB_INNERFRAME_STOP = new Point2D.Double(0, METALKNOB_INNERFRAME.getBounds2D().getMaxY());
                final float[] METALKNOB_INNERFRAME_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] METALKNOB_INNERFRAME_COLORS = {
                    new Color(0, 0, 0, 255),
                    new Color(204, 204, 204, 255)
                };
                final LinearGradientPaint METALKNOB_INNERFRAME_GRADIENT = new LinearGradientPaint(METALKNOB_INNERFRAME_START, METALKNOB_INNERFRAME_STOP, METALKNOB_INNERFRAME_FRACTIONS, METALKNOB_INNERFRAME_COLORS);
                G2.setPaint(METALKNOB_INNERFRAME_GRADIENT);
                G2.fill(METALKNOB_INNERFRAME);

                final Ellipse2D METALKNOB_INNERBACKGROUND = new Ellipse2D.Double(IMAGE_WIDTH * 0.4813084006309509, IMAGE_HEIGHT * 0.4859813153743744, IMAGE_WIDTH * 0.03271031379699707, IMAGE_HEIGHT * 0.03271028399467468);
                final Point2D METALKNOB_INNERBACKGROUND_START = new Point2D.Double(0, METALKNOB_INNERBACKGROUND.getBounds2D().getMinY());
                final Point2D METALKNOB_INNERBACKGROUND_STOP = new Point2D.Double(0, METALKNOB_INNERBACKGROUND.getBounds2D().getMaxY());
                final float[] METALKNOB_INNERBACKGROUND_FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] METALKNOB_INNERBACKGROUND_COLORS = {
                    new Color(1, 6, 11, 255),
                    new Color(50, 52, 56, 255)
                };
                final LinearGradientPaint METALKNOB_INNERBACKGROUND_GRADIENT = new LinearGradientPaint(METALKNOB_INNERBACKGROUND_START, METALKNOB_INNERBACKGROUND_STOP, METALKNOB_INNERBACKGROUND_FRACTIONS, METALKNOB_INNERBACKGROUND_COLORS);
                G2.setPaint(METALKNOB_INNERBACKGROUND_GRADIENT);
                G2.fill(METALKNOB_INNERBACKGROUND);
                break;
        }
        G2.dispose();

        // Buffer current values
        sizeBuffer = SIZE;
        knobTypeBuffer = KNOB_TYPE;
        knobStyleBuffer = KNOB_STYLE;

        return knobImageBuffer;
    }
}
