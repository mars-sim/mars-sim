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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;


/**
 *
 * @author hansolo
 */
public enum PointerImageFactory {
    INSTANCE;
    private final Util UTIL = Util.INSTANCE;
    private int radWidth = 0;
    private PointerType radPointerType = PointerType.TYPE1;
    private ColorDef radPointerColor = null;
    private CustomColorDef radCustomPointerColor = new CustomColorDef(Color.RED);
    private BackgroundColor backgroundColor = BackgroundColor.DARK_GRAY;
    private BufferedImage radPointerImage = UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
    private int radWidthShadow = 0;
    private PointerType radPointerTypeShadow = PointerType.TYPE1;
    private BufferedImage radPointerShadowImage = UTIL.createImage(1, 1, Transparency.TRANSLUCENT);

    /**
     * Creates the pointer image for a centered radial gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * @param WIDTH
     * @param POINTER_TYPE
     * @param POINTER_COLOR
     * @return a buffered image that contains the pointer image for a centered radial gauge
     */
    public BufferedImage createStandardPointer(final int WIDTH, final PointerType POINTER_TYPE, final ColorDef POINTER_COLOR, final BackgroundColor BACKGROUND_COLOR) {
        return createStandardPointer(WIDTH, POINTER_TYPE, POINTER_COLOR, null, BACKGROUND_COLOR);
    }

    /**
     * Creates the pointer image for a centered radial gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * @param WIDTH
     * @param POINTER_TYPE
     * @param POINTER_COLOR
     * @param CUSTOM_POINTER_COLOR
     * @return a buffered image that contains the pointer image for a centered radial gauge
     */
    public BufferedImage createStandardPointer(final int WIDTH, final PointerType POINTER_TYPE, final ColorDef POINTER_COLOR, final CustomColorDef CUSTOM_POINTER_COLOR, final BackgroundColor BACKGROUND_COLOR) {
        if (WIDTH <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        if (radWidth == WIDTH && radPointerType == POINTER_TYPE && radPointerColor == POINTER_COLOR && radCustomPointerColor == CUSTOM_POINTER_COLOR && backgroundColor == BACKGROUND_COLOR) {
            return radPointerImage;
        }

        radPointerImage.flush();
        radPointerImage = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = radPointerImage.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        //G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = radPointerImage.getWidth();
        final int IMAGE_HEIGHT = radPointerImage.getHeight();

        final GeneralPath POINTER;
        final Point2D POINTER_START;
        final Point2D POINTER_STOP;
        final float[] POINTER_FRACTIONS;
        final Color[] POINTER_COLORS;
        final java.awt.Paint POINTER_GRADIENT;

        switch (POINTER_TYPE) {
            case TYPE2:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.46261682242990654);
                POINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.3411214953271028);
                POINTER.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.3411214953271028);
                POINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.46261682242990654);
                POINTER.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.curveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5327102803738317);
                POINTER.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(0, POINTER.getBounds2D().getMaxY());
                POINTER_STOP = new Point2D.Double(0, POINTER.getBounds2D().getMinY());
                POINTER_FRACTIONS = new float[]{
                    0.0f,
                    0.36f,
                    0.3601f,
                    1.0f
                };
                if (POINTER_COLOR != ColorDef.CUSTOM) {
                    POINTER_COLORS = new Color[]{
                        BACKGROUND_COLOR.LABEL_COLOR,
                        BACKGROUND_COLOR.LABEL_COLOR,
                        POINTER_COLOR.LIGHT,
                        POINTER_COLOR.LIGHT
                    };
                } else {
                    POINTER_COLORS = new Color[]{
                        BACKGROUND_COLOR.LABEL_COLOR,
                        BACKGROUND_COLOR.LABEL_COLOR,
                        CUSTOM_POINTER_COLOR.LIGHT,
                        CUSTOM_POINTER_COLOR.LIGHT
                    };
                }
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                break;

            case TYPE3:
                POINTER = new GeneralPath(new Rectangle2D.Double(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.1308411214953271, IMAGE_WIDTH * 0.009345794392523364, IMAGE_HEIGHT * 0.37383177570093457));
                if (POINTER_COLOR != ColorDef.CUSTOM) {
                    G2.setColor(POINTER_COLOR.LIGHT);
                } else {
                    G2.setColor(CUSTOM_POINTER_COLOR.LIGHT);
                }
                G2.fill(POINTER);
                break;

            case TYPE4:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1261682242990654);
                POINTER.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.13551401869158877);
                POINTER.lineTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
                POINTER.lineTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.602803738317757);
                POINTER.lineTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.602803738317757);
                POINTER.lineTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
                POINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.13551401869158877);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1261682242990654);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(POINTER.getBounds2D().getMinX(), 0);
                POINTER_STOP = new Point2D.Double(POINTER.getBounds2D().getMaxX(), 0);
                POINTER_FRACTIONS = new float[]{
                    0.0f,
                    0.51f,
                    0.52f,
                    1.0f
                };
                if (POINTER_COLOR != ColorDef.CUSTOM) {
                    POINTER_COLORS = new Color[]{
                        POINTER_COLOR.DARK,
                        POINTER_COLOR.DARK,
                        POINTER_COLOR.LIGHT,
                        POINTER_COLOR.LIGHT
                    };
                } else {
                    POINTER_COLORS = new Color[]{
                        CUSTOM_POINTER_COLOR.DARK,
                        CUSTOM_POINTER_COLOR.DARK,
                        CUSTOM_POINTER_COLOR.LIGHT,
                        CUSTOM_POINTER_COLOR.LIGHT
                    };
                }
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                break;

            case TYPE5:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.4953271028037383);
                POINTER.lineTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.4953271028037383);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382);
                POINTER.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.4953271028037383);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.4953271028037383);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(POINTER.getBounds2D().getMinX(), 0);
                POINTER_STOP = new Point2D.Double(POINTER.getBounds2D().getMaxX(), 0);
                POINTER_FRACTIONS = new float[]{
                    0.0f,
                    0.4999f,
                    0.5f,
                    1.0f
                };
                if (POINTER_COLOR != ColorDef.CUSTOM) {
                    POINTER_COLORS = new Color[]{
                        POINTER_COLOR.LIGHT,
                        POINTER_COLOR.LIGHT,
                        POINTER_COLOR.MEDIUM,
                        POINTER_COLOR.MEDIUM
                    };
                } else {
                    POINTER_COLORS = new Color[]{
                        CUSTOM_POINTER_COLOR.LIGHT,
                        CUSTOM_POINTER_COLOR.LIGHT,
                        CUSTOM_POINTER_COLOR.MEDIUM,
                        CUSTOM_POINTER_COLOR.MEDIUM
                    };
                }
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                if (POINTER_COLOR != ColorDef.CUSTOM) {
                    G2.setColor(POINTER_COLOR.DARK);
                } else {
                    G2.setColor(CUSTOM_POINTER_COLOR.DARK);
                }
                G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                G2.draw(POINTER);
                break;

            case TYPE6:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.48598130841121495);
                POINTER.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.3925233644859813);
                POINTER.lineTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.3177570093457944);
                POINTER.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.3177570093457944);
                POINTER.lineTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.3878504672897196);
                POINTER.lineTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.48598130841121495);
                POINTER.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.48598130841121495);
                POINTER.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.3878504672897196);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.3177570093457944);
                POINTER.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.3925233644859813);
                POINTER.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.48598130841121495);
                POINTER.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.48598130841121495);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(POINTER.getBounds2D().getMaxY(), 0);
                POINTER_STOP = new Point2D.Double(POINTER.getBounds2D().getMinY(), 0);
                POINTER_FRACTIONS = new float[]{
                    0.0f,
                    0.25f,
                    0.75f,
                    1.0f
                };
                if (POINTER_COLOR != ColorDef.CUSTOM) {
                    POINTER_COLORS = new Color[]{
                        POINTER_COLOR.LIGHT,
                        POINTER_COLOR.MEDIUM,
                        POINTER_COLOR.MEDIUM,
                        POINTER_COLOR.LIGHT
                    };
                } else {
                    POINTER_COLORS = new Color[]{
                        CUSTOM_POINTER_COLOR.LIGHT,
                        CUSTOM_POINTER_COLOR.MEDIUM,
                        CUSTOM_POINTER_COLOR.MEDIUM,
                        CUSTOM_POINTER_COLOR.LIGHT
                    };
                }
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                if (POINTER_COLOR != ColorDef.CUSTOM) {
                    G2.setColor(POINTER_COLOR.DARK);
                } else {
                    G2.setColor(CUSTOM_POINTER_COLOR.DARK);
                }
                G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                G2.draw(POINTER);
                break;

            case TYPE7:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5);
                POINTER.lineTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5);
                POINTER.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(POINTER.getBounds2D().getMinX(), 0);
                POINTER_STOP = new Point2D.Double(POINTER.getBounds2D().getMaxX(), 0);
                POINTER_FRACTIONS = new float[]{
                    0.0f,
                    1.0f
                };
                if (POINTER_COLOR != ColorDef.CUSTOM) {
                    POINTER_COLORS = new Color[]{
                        POINTER_COLOR.DARK,
                        POINTER_COLOR.MEDIUM
                    };
                } else {
                    POINTER_COLORS = new Color[]{
                        CUSTOM_POINTER_COLOR.DARK,
                        CUSTOM_POINTER_COLOR.MEDIUM
                    };
                }
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                break;

            case TYPE8:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5327102803738317);
                POINTER.lineTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382);
                POINTER.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5327102803738317);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(POINTER.getBounds2D().getMinX(), 0);
                POINTER_STOP = new Point2D.Double(POINTER.getBounds2D().getMaxX(), 0);
                POINTER_FRACTIONS = new float[]{
                    0.0f,
                    0.46f,
                    0.47f,
                    1.0f
                };
                if (POINTER_COLOR != ColorDef.CUSTOM) {
                    POINTER_COLORS = new Color[]{
                        POINTER_COLOR.LIGHT,
                        POINTER_COLOR.LIGHT,
                        POINTER_COLOR.MEDIUM,
                        POINTER_COLOR.MEDIUM
                    };
                } else {
                    POINTER_COLORS = new Color[]{
                        CUSTOM_POINTER_COLOR.LIGHT,
                        CUSTOM_POINTER_COLOR.LIGHT,
                        CUSTOM_POINTER_COLOR.MEDIUM,
                        CUSTOM_POINTER_COLOR.MEDIUM
                    };
                }
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                if (POINTER_COLOR != ColorDef.CUSTOM) {
                    G2.setColor(POINTER_COLOR.DARK);
                } else {
                    G2.setColor(CUSTOM_POINTER_COLOR.DARK);
                }
                G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                G2.draw(POINTER);
                break;

            case TYPE9:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.2336448598130841);
                POINTER.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.2336448598130841);
                POINTER.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.4392523364485981);
                POINTER.lineTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.4392523364485981);
                POINTER.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.2336448598130841);
                POINTER.closePath();
                POINTER.moveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5280373831775701);
                POINTER.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5280373831775701, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.602803738317757, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.602803738317757);
                POINTER.curveTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.6074766355140186, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.6074766355140186, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.6074766355140186);
                POINTER.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.6074766355140186, IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.6074766355140186, IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.602803738317757);
                POINTER.curveTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.602803738317757, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.5280373831775701, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.5280373831775701);
                POINTER.lineTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(POINTER.getBounds2D().getMinX(), 0);
                POINTER_STOP = new Point2D.Double(POINTER.getBounds2D().getMaxX(), 0);
                POINTER_FRACTIONS = new float[]{
                    0.0f,
                    0.48f,
                    1.0f
                };
                POINTER_COLORS = new Color[]{
                    new Color(50, 50, 50, 255),
                    new Color(102, 102, 102, 255),
                    new Color(50, 50, 50, 255)
                };
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                G2.setColor(new Color(0x2E2E2E));
                G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                G2.draw(POINTER);

                final GeneralPath COLORED_BOX = new GeneralPath();
                COLORED_BOX.setWindingRule(Path2D.WIND_EVEN_ODD);
                COLORED_BOX.moveTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.21962616822429906);
                COLORED_BOX.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.21962616822429906);
                COLORED_BOX.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.13551401869158877);
                COLORED_BOX.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.13551401869158877);
                COLORED_BOX.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.21962616822429906);
                COLORED_BOX.closePath();
                G2.setColor(POINTER_COLOR.MEDIUM);
                G2.fill(COLORED_BOX);
                break;

            case TYPE10:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382);
                POINTER.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382, IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5560747663551402, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5560747663551402);
                POINTER.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5560747663551402, IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(POINTER.getBounds2D().getMinX(), 0);
                POINTER_STOP = new Point2D.Double(POINTER.getBounds2D().getMaxX(), 0);
                POINTER_FRACTIONS = new float[]{
                    0.0f,
                    0.4999f,
                    0.5f,
                    1.0f
                };
                if (POINTER_COLOR != ColorDef.CUSTOM) {
                    POINTER_COLORS = new Color[]{
                        POINTER_COLOR.LIGHT,
                        POINTER_COLOR.LIGHT,
                        POINTER_COLOR.MEDIUM,
                        POINTER_COLOR.MEDIUM
                    };
                } else {
                    POINTER_COLORS = new Color[]{
                        CUSTOM_POINTER_COLOR.LIGHT,
                        CUSTOM_POINTER_COLOR.LIGHT,
                        CUSTOM_POINTER_COLOR.MEDIUM,
                        CUSTOM_POINTER_COLOR.MEDIUM
                    };
                }
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                G2.setColor(POINTER_COLOR.MEDIUM);
                G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                G2.draw(POINTER);
                break;

            case TYPE11:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.5 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT);
                POINTER.lineTo(0.48598130841121495 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT);
                POINTER.curveTo(0.48598130841121495 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT, 0.48130841121495327 * IMAGE_WIDTH, 0.5841121495327103 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.5841121495327103 * IMAGE_HEIGHT);
                POINTER.curveTo(0.514018691588785 * IMAGE_WIDTH, 0.5841121495327103 * IMAGE_HEIGHT, 0.5093457943925234 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT, 0.5093457943925234 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT);
                POINTER.closePath();
                if (POINTER_COLOR != ColorDef.CUSTOM) {
                    POINTER_GRADIENT = new LinearGradientPaint(new Point2D.Double(0, POINTER.getBounds2D().getMinY()), new Point2D.Double(0, POINTER.getBounds2D().getMaxY()), new float[]{0.0f, 1.0f}, new Color[]{POINTER_COLOR.MEDIUM, POINTER_COLOR.DARK});
                    G2.setPaint(POINTER_GRADIENT);
                    G2.fill(POINTER);
                    G2.setColor(POINTER_COLOR.VERY_DARK);
                } else {
                    POINTER_GRADIENT = new LinearGradientPaint(new Point2D.Double(0, POINTER.getBounds2D().getMinY()), new Point2D.Double(0, POINTER.getBounds2D().getMaxY()), new float[]{0.0f, 1.0f}, new Color[]{CUSTOM_POINTER_COLOR.MEDIUM, CUSTOM_POINTER_COLOR.DARK});
                    G2.setPaint(POINTER_GRADIENT);
                    G2.fill(POINTER);
                    G2.setColor(CUSTOM_POINTER_COLOR.VERY_DARK);
                }
                G2.setStroke(new BasicStroke(0.004672897196261682f * IMAGE_WIDTH, 0, 1));
                G2.draw(POINTER);
                break;

            case TYPE12:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.5 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT);
                POINTER.lineTo(0.48598130841121495 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5 * IMAGE_WIDTH, 0.5046728971962616 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5093457943925234 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT);
                POINTER.closePath();
                if (POINTER_COLOR != ColorDef.CUSTOM) {
                    G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 0.5046728971962616 * IMAGE_HEIGHT), new float[]{0.0f, 1.0f}, new Color[]{POINTER_COLOR.MEDIUM, POINTER_COLOR.DARK}));
                    G2.fill(POINTER);
                    G2.setColor(POINTER_COLOR.VERY_DARK);
                } else {
                    G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 0.5046728971962616 * IMAGE_HEIGHT), new float[]{0.0f, 1.0f}, new Color[]{CUSTOM_POINTER_COLOR.MEDIUM, CUSTOM_POINTER_COLOR.DARK}));
                    G2.fill(POINTER);
                    G2.setColor(CUSTOM_POINTER_COLOR.VERY_DARK);
                }
                G2.setStroke(new BasicStroke((0.004672897196261682f * IMAGE_WIDTH), 0, 1));
                G2.draw(POINTER);
                break;

            case TYPE13:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.48598130841121495 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5 * IMAGE_WIDTH, 0.1308411214953271 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5093457943925234 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5093457943925234 * IMAGE_WIDTH, 0.5093457943925234 * IMAGE_HEIGHT);
                POINTER.lineTo(0.48598130841121495 * IMAGE_WIDTH, 0.5093457943925234 * IMAGE_HEIGHT);
                POINTER.lineTo(0.48598130841121495 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT);
                POINTER.closePath();
                if (POINTER_COLOR != ColorDef.CUSTOM) {
                    G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 0.1308411214953271 * IMAGE_HEIGHT), new float[]{0.0f, 0.849999f, 0.85f, 1.0f}, new Color[]{BACKGROUND_COLOR.LABEL_COLOR, BACKGROUND_COLOR.LABEL_COLOR, POINTER_COLOR.MEDIUM, POINTER_COLOR.MEDIUM}));
                } else {
                    G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 0.1308411214953271 * IMAGE_HEIGHT), new float[]{0.0f, 0.849999f, 0.85f, 1.0f}, new Color[]{BACKGROUND_COLOR.LABEL_COLOR, BACKGROUND_COLOR.LABEL_COLOR, CUSTOM_POINTER_COLOR.MEDIUM, CUSTOM_POINTER_COLOR.MEDIUM}));
                }
                G2.fill(POINTER);
                break;

            case TYPE14:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.48598130841121495 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5 * IMAGE_WIDTH, 0.1308411214953271 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5093457943925234 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5093457943925234 * IMAGE_WIDTH, 0.5093457943925234 * IMAGE_HEIGHT);
                POINTER.lineTo(0.48598130841121495 * IMAGE_WIDTH, 0.5093457943925234 * IMAGE_HEIGHT);
                POINTER.lineTo(0.48598130841121495 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT);
                POINTER.closePath();
                if (POINTER_COLOR != ColorDef.CUSTOM) {
                    G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.48598130841121495 * IMAGE_WIDTH, 0), new Point2D.Double(0.5093457943925234 * IMAGE_HEIGHT, 0), new float[]{0f, 0.5f, 1f}, new Color[]{POINTER_COLOR.VERY_DARK, POINTER_COLOR.LIGHT, POINTER_COLOR.VERY_DARK}));
                } else {
                    G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.48598130841121495 * IMAGE_WIDTH, 0), new Point2D.Double(0.5093457943925234 * IMAGE_HEIGHT, 0), new float[]{0f, 0.5f, 1f}, new Color[]{CUSTOM_POINTER_COLOR.VERY_DARK, CUSTOM_POINTER_COLOR.LIGHT, CUSTOM_POINTER_COLOR.VERY_DARK}));
                }

                G2.fill(POINTER);

                break;
            case TYPE15:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.48 * IMAGE_WIDTH, 0.505 * IMAGE_HEIGHT);
                POINTER.lineTo(0.48 * IMAGE_WIDTH, 0.275 * IMAGE_HEIGHT);
                POINTER.lineTo(0.46 * IMAGE_WIDTH, 0.275 * IMAGE_HEIGHT);
                POINTER.lineTo(0.495 * IMAGE_WIDTH, 0.15 * IMAGE_HEIGHT);
                POINTER.lineTo(0.53 * IMAGE_WIDTH, 0.275 * IMAGE_HEIGHT);
                POINTER.lineTo(0.515 * IMAGE_WIDTH, 0.275 * IMAGE_HEIGHT);
                POINTER.lineTo(0.515 * IMAGE_WIDTH, 0.505 * IMAGE_HEIGHT);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(0.46 * IMAGE_WIDTH, 0.26 * IMAGE_HEIGHT);
                POINTER_STOP  = new Point2D.Double(0.53 * IMAGE_WIDTH, 0.26 * IMAGE_HEIGHT);
                POINTER_FRACTIONS = new float[]{
                    0.0f,
                    0.5f,
                    1.0f
                };
                if (POINTER_COLOR != ColorDef.CUSTOM) {
                    POINTER_COLORS = new Color[]{
                        POINTER_COLOR.VERY_DARK,
                        POINTER_COLOR.MEDIUM,
                        POINTER_COLOR.VERY_DARK
                    };
                } else {
                    POINTER_COLORS = new Color[]{
                        CUSTOM_POINTER_COLOR.VERY_DARK,
                        CUSTOM_POINTER_COLOR.MEDIUM,
                        CUSTOM_POINTER_COLOR.VERY_DARK
                    };
                }
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                //G2.setColor(POINTER_COLOR.LIGHT);
                //G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                //G2.draw(POINTER);
                break;
            case TYPE1:

            default:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.40186915887850466);
                POINTER.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.38317757009345793, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.38317757009345793, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.397196261682243);
                POINTER.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5327102803738317);
                POINTER.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.closePath();
                POINTER_START = new Point2D.Double(0, POINTER.getBounds2D().getMinY());
                POINTER_STOP = new Point2D.Double(0, POINTER.getBounds2D().getMaxY());
                POINTER_FRACTIONS = new float[]{
                    0.0f,
                    0.3f,
                    0.6f,
                    1.0f
                };
                if (POINTER_COLOR != ColorDef.CUSTOM) {
                    POINTER_COLORS = new Color[]{
                        POINTER_COLOR.VERY_DARK,
                        POINTER_COLOR.MEDIUM,
                        POINTER_COLOR.MEDIUM,
                        POINTER_COLOR.VERY_DARK
                    };
                } else {
                    POINTER_COLORS = new Color[]{
                        CUSTOM_POINTER_COLOR.VERY_DARK,
                        CUSTOM_POINTER_COLOR.MEDIUM,
                        CUSTOM_POINTER_COLOR.MEDIUM,
                        CUSTOM_POINTER_COLOR.VERY_DARK
                    };
                }
                POINTER_GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                G2.setColor(POINTER_COLOR.LIGHT);
                G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                G2.draw(POINTER);
                break;
        }

        G2.dispose();

        // Cache current parameters
        radWidth = WIDTH;
        radPointerType = POINTER_TYPE;
        radPointerColor = POINTER_COLOR;
        backgroundColor = BACKGROUND_COLOR;
        radCustomPointerColor = CUSTOM_POINTER_COLOR;

        return radPointerImage;
    }

    /**
     * Creates the pointer shadow image for a centered radial gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * @param WIDTH
     * @param POINTER_TYPE
     * @return a buffered image that contains the pointer shadow image for a centered radial gauge
     */
    public BufferedImage createStandardPointerShadow(final int WIDTH, final PointerType POINTER_TYPE) {
        if (WIDTH <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        if (radWidthShadow == WIDTH && radPointerTypeShadow == POINTER_TYPE) {
            return radPointerShadowImage;
        }

        final Color SHADOW_COLOR = new Color(0.0f, 0.0f, 0.0f, 0.65f);

        radPointerShadowImage.flush();
        radPointerShadowImage = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = radPointerShadowImage.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        //G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = radPointerShadowImage.getWidth();
        final int IMAGE_HEIGHT = radPointerShadowImage.getHeight();

        final GeneralPath POINTER;

        switch (POINTER_TYPE) {
            case TYPE1:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.40186915887850466);
                POINTER.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.38317757009345793, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.38317757009345793, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.397196261682243);
                POINTER.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5327102803738317);
                POINTER.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE2:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.46261682242990654);
                POINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.3411214953271028);
                POINTER.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.3411214953271028);
                POINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.46261682242990654);
                POINTER.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.curveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5327102803738317);
                POINTER.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE3:

                break;

            case TYPE4:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1261682242990654);
                POINTER.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.13551401869158877);
                POINTER.lineTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
                POINTER.lineTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.602803738317757);
                POINTER.lineTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.602803738317757);
                POINTER.lineTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
                POINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.13551401869158877);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1261682242990654);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE5:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.4953271028037383);
                POINTER.lineTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.4953271028037383);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382);
                POINTER.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.4953271028037383);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.4953271028037383);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE6:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.48598130841121495);
                POINTER.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.3925233644859813);
                POINTER.lineTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.3177570093457944);
                POINTER.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.3177570093457944);
                POINTER.lineTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.3878504672897196);
                POINTER.lineTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.48598130841121495);
                POINTER.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.48598130841121495);
                POINTER.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.3878504672897196);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.3177570093457944);
                POINTER.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.3925233644859813);
                POINTER.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.48598130841121495);
                POINTER.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.48598130841121495);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE7:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5);
                POINTER.lineTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5);
                POINTER.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE8:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5327102803738317);
                POINTER.lineTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382);
                POINTER.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
                POINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5327102803738317);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE9:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.2336448598130841);
                POINTER.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.2336448598130841);
                POINTER.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.4392523364485981);
                POINTER.lineTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.4392523364485981);
                POINTER.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.2336448598130841);
                POINTER.closePath();
                POINTER.moveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5280373831775701);
                POINTER.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5280373831775701, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.602803738317757, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.602803738317757);
                POINTER.curveTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.6074766355140186, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.6074766355140186, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.6074766355140186);
                POINTER.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.6074766355140186, IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.6074766355140186, IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.602803738317757);
                POINTER.curveTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.602803738317757, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.5280373831775701, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.5280373831775701);
                POINTER.lineTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE10:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382);
                POINTER.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382, IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5560747663551402, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5560747663551402);
                POINTER.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5560747663551402, IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.14953271028037382);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE11:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.5 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT);
                POINTER.lineTo(0.48598130841121495 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT);
                POINTER.curveTo(0.48598130841121495 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT, 0.48130841121495327 * IMAGE_WIDTH, 0.5841121495327103 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.5841121495327103 * IMAGE_HEIGHT);
                POINTER.curveTo(0.514018691588785 * IMAGE_WIDTH, 0.5841121495327103 * IMAGE_HEIGHT, 0.5093457943925234 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT, 0.5093457943925234 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE12:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.5 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT);
                POINTER.lineTo(0.48598130841121495 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5 * IMAGE_WIDTH, 0.5046728971962616 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5093457943925234 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE13:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.48598130841121495 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5 * IMAGE_WIDTH, 0.1308411214953271 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5093457943925234 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5093457943925234 * IMAGE_WIDTH, 0.5093457943925234 * IMAGE_HEIGHT);
                POINTER.lineTo(0.48598130841121495 * IMAGE_WIDTH, 0.5093457943925234 * IMAGE_HEIGHT);
                POINTER.lineTo(0.48598130841121495 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;
            case TYPE14:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.48598130841121495 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5 * IMAGE_WIDTH, 0.1308411214953271 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5093457943925234 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT);
                POINTER.lineTo(0.5093457943925234 * IMAGE_WIDTH, 0.5093457943925234 * IMAGE_HEIGHT);
                POINTER.lineTo(0.48598130841121495 * IMAGE_WIDTH, 0.5093457943925234 * IMAGE_HEIGHT);
                POINTER.lineTo(0.48598130841121495 * IMAGE_WIDTH, 0.16822429906542055 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;
            case TYPE15:
                POINTER = new GeneralPath();
                POINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
                POINTER.moveTo(0.48 * IMAGE_WIDTH, 0.505 * IMAGE_HEIGHT);
                POINTER.lineTo(0.48 * IMAGE_WIDTH, 0.275 * IMAGE_HEIGHT);
                POINTER.lineTo(0.46 * IMAGE_WIDTH, 0.275 * IMAGE_HEIGHT);
                POINTER.lineTo(0.495 * IMAGE_WIDTH, 0.15 * IMAGE_HEIGHT);
                POINTER.lineTo(0.53 * IMAGE_WIDTH, 0.275 * IMAGE_HEIGHT);
                POINTER.lineTo(0.515 * IMAGE_WIDTH, 0.275 * IMAGE_HEIGHT);
                POINTER.lineTo(0.515 * IMAGE_WIDTH, 0.505 * IMAGE_HEIGHT);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
            default:

                break;
        }

        G2.dispose();

        // Cache current parameters
        radWidthShadow = WIDTH;
        radPointerTypeShadow = POINTER_TYPE;

        return radPointerShadowImage;
    }

    @Override
    public String toString() {
        return "PointerImageFactory";
    }
}
