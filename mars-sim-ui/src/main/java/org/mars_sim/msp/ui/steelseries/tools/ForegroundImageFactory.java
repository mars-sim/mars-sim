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
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.Transparency;
import java.awt.geom.Path2D;


/**
 *
 * @author hansolo
 */
public enum ForegroundImageFactory {

    INSTANCE;
    private final Util UTIL = Util.INSTANCE;
    private int radWidth = 0;
    private boolean radWithCenterKnob = true;
    private ForegroundType radType = ForegroundType.FG_TYPE1;
    private BufferedImage radForegroundImage = UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
    private int linWidth = 0;
    private int linHeight = 0;
    private boolean linWithCenterKnob = false;
    private BufferedImage linForegroundImage = UTIL.createImage(1, 1, Transparency.TRANSLUCENT);

    /**
     * Creates the foreground image for a radial gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * @param WIDTH
     * @return a buffered image that contains the foreground image of a radial gauge
     */
    public BufferedImage createRadialForeground(final int WIDTH) {
        return createRadialForeground(WIDTH, true, ForegroundType.FG_TYPE1);
    }

    /**
     * Creates the foreground image for a radial gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * @param WIDTH
     * @param WITH_CENTER_KNOB
     * @return a buffered image that contains the foreground image of a radial gauge
     */
    public BufferedImage createRadialForeground(final int WIDTH, final boolean WITH_CENTER_KNOB) {

        return createRadialForeground(WIDTH, WITH_CENTER_KNOB, ForegroundType.FG_TYPE1);
    }

    /**
     * Creates the foreground image for a radial gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * @param WIDTH
     * @param WITH_CENTER_KNOB
     * @param TYPE
     * @return a buffered image that contains the foreground image of a radial gauge
     */
    public BufferedImage createRadialForeground(final int WIDTH, final boolean WITH_CENTER_KNOB, final ForegroundType TYPE) {
        return createRadialForeground(WIDTH, WITH_CENTER_KNOB, TYPE, null);
    }

    /**
     * Creates the foreground image for a radial gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * @param WIDTH
     * @param WITH_CENTER_KNOB
     * @param TYPE
     * @param FOREGROUND_IMAGE
     * @return a buffered image that contains the foreground image of a radial gauge
     */
    public BufferedImage createRadialForeground(final int WIDTH, final boolean WITH_CENTER_KNOB, final ForegroundType TYPE, final BufferedImage FOREGROUND_IMAGE) {
        if (WIDTH <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        // Take image from cache instead of creating a new one if parameters are the same as last time
        if (radWidth == WIDTH && radWithCenterKnob == WITH_CENTER_KNOB && radType == TYPE) {
            if (FOREGROUND_IMAGE != null) {
                final Graphics2D G2 = FOREGROUND_IMAGE.createGraphics();
                G2.drawImage(radForegroundImage, 0, 0, null);
                G2.dispose();
            }
            return radForegroundImage;
        }

        radForegroundImage.flush();
        radForegroundImage = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);

        final Graphics2D G2 = radForegroundImage.createGraphics();

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = WIDTH;
        final int IMAGE_HEIGHT = WIDTH;

        if (WITH_CENTER_KNOB) {
            final Ellipse2D E_CENTER_KNOB_FRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4579439163208008, IMAGE_HEIGHT * 0.4579439163208008, IMAGE_WIDTH * 0.08411216735839844, IMAGE_HEIGHT * 0.08411216735839844);
            final Point2D E_CENTER_KNOB_FRAME_START = new Point2D.Double(0, E_CENTER_KNOB_FRAME.getBounds2D().getMinY());
            final Point2D E_CENTER_KNOB_FRAME_STOP = new Point2D.Double(0, E_CENTER_KNOB_FRAME.getBounds2D().getMaxY());
            final float[] E_CENTER_KNOB_FRAME_FRACTIONS = {
                0.0f,
                0.46f,
                1.0f
            };
            final Color[] E_CENTER_KNOB_FRAME_COLORS = {
                new Color(180, 180, 180, 255),
                new Color(63, 63, 63, 255),
                new Color(40, 40, 40, 255)
            };
            final LinearGradientPaint E_CENTER_KNOB_FRAME_GRADIENT = new LinearGradientPaint(E_CENTER_KNOB_FRAME_START, E_CENTER_KNOB_FRAME_STOP, E_CENTER_KNOB_FRAME_FRACTIONS, E_CENTER_KNOB_FRAME_COLORS);
            G2.setPaint(E_CENTER_KNOB_FRAME_GRADIENT);
            G2.fill(E_CENTER_KNOB_FRAME);

            final Ellipse2D E_CENTER_KNOB_MAIN = new Ellipse2D.Double(IMAGE_WIDTH * 0.4672897160053253, IMAGE_HEIGHT * 0.4672897160053253, IMAGE_WIDTH * 0.06542053818702698, IMAGE_HEIGHT * 0.06542053818702698);
            final Point2D E_CENTER_KNOB_MAIN_START = new Point2D.Double(0, E_CENTER_KNOB_MAIN.getBounds2D().getMinY());
            final Point2D E_CENTER_KNOB_MAIN_STOP = new Point2D.Double(0, E_CENTER_KNOB_MAIN.getBounds2D().getMaxY());
            final float[] E_CENTER_KNOB_MAIN_FRACTIONS = {
                0.0f,
                1.0f
            };
            final Color[] E_CENTER_KNOB_MAIN_COLORS = {
                new Color(217, 217, 217, 255),
                new Color(191, 191, 191, 255)
            };
            final LinearGradientPaint E_CENTER_KNOB_MAIN_GRADIENT = new LinearGradientPaint(E_CENTER_KNOB_MAIN_START, E_CENTER_KNOB_MAIN_STOP, E_CENTER_KNOB_MAIN_FRACTIONS, E_CENTER_KNOB_MAIN_COLORS);
            G2.setPaint(E_CENTER_KNOB_MAIN_GRADIENT);
            G2.fill(E_CENTER_KNOB_MAIN);

            final Ellipse2D E_CENTER_KNOB_INNERSHADOW = new Ellipse2D.Double(IMAGE_WIDTH * 0.4672897160053253, IMAGE_HEIGHT * 0.4672897160053253, IMAGE_WIDTH * 0.06542053818702698, IMAGE_HEIGHT * 0.06542053818702698);
            final Point2D E_CENTER_KNOB_INNERSHADOW_CENTER = new Point2D.Double((0.4953271028037383 * IMAGE_WIDTH), (0.49065420560747663 * IMAGE_HEIGHT));
            final float[] E_CENTER_KNOB_INNERSHADOW_FRACTIONS = {
                0.0f,
                0.75f,
                0.76f,
                1.0f
            };
            final Color[] E_CENTER_KNOB_INNERSHADOW_COLORS = {
                new Color(0, 0, 0, 0),
                new Color(0, 0, 0, 0),
                new Color(0, 0, 0, 1),
                new Color(0, 0, 0, 51)
            };
            final RadialGradientPaint E_CENTER_KNOB_INNERSHADOW_GRADIENT = new RadialGradientPaint(E_CENTER_KNOB_INNERSHADOW_CENTER, (float) (0.03271028037383177 * IMAGE_WIDTH), E_CENTER_KNOB_INNERSHADOW_FRACTIONS, E_CENTER_KNOB_INNERSHADOW_COLORS);
            G2.setPaint(E_CENTER_KNOB_INNERSHADOW_GRADIENT);
            G2.fill(E_CENTER_KNOB_INNERSHADOW);
        }

        final GeneralPath HIGHLIGHT = new GeneralPath();
        final Point2D HIGHLIGHT_START = new Point2D.Double();
        final Point2D HIGHLIGHT_STOP = new Point2D.Double();
        final float[] HIGHLIGHT_FRACTIONS = {
            0.0f,
            1.0f
        };
        final Color[] HIGHLIGHT_COLORS = {
            new Color(1.0f, 1.0f, 1.0f, 0.2f),
            new Color(1.0f, 1.0f, 1.0f, 0.05f)
        };
        final Paint HIGHLIGHT_GRADIENT;

        switch (TYPE) {
            case FG_TYPE1:
                HIGHLIGHT.setWindingRule(Path2D.WIND_EVEN_ODD);
                HIGHLIGHT.moveTo(IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.49065420560747663);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.5046728971962616, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.5093457943925234);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.20093457943925233, IMAGE_HEIGHT * 0.4532710280373832, IMAGE_WIDTH * 0.32710280373831774, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.4158878504672897);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.6588785046728972, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.794392523364486, IMAGE_HEIGHT * 0.4439252336448598, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.514018691588785);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.5046728971962616, IMAGE_WIDTH * 0.9205607476635514, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.49065420560747663);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.2757009345794392, IMAGE_WIDTH * 0.7476635514018691, IMAGE_HEIGHT * 0.08411214953271028, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.08411214953271028);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.2523364485981308, IMAGE_HEIGHT * 0.08411214953271028, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.2803738317757009, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.49065420560747663);
                HIGHLIGHT.closePath();
                HIGHLIGHT_START.setLocation(0, HIGHLIGHT.getBounds2D().getMinY());
                HIGHLIGHT_STOP.setLocation(0, HIGHLIGHT.getBounds2D().getMaxY());
                HIGHLIGHT_GRADIENT = new LinearGradientPaint(HIGHLIGHT_START, HIGHLIGHT_STOP, HIGHLIGHT_FRACTIONS, HIGHLIGHT_COLORS);
                G2.setPaint(HIGHLIGHT_GRADIENT);
                G2.fill(HIGHLIGHT);
                break;

            case FG_TYPE2:
                HIGHLIGHT.setWindingRule(Path2D.WIND_EVEN_ODD);
                HIGHLIGHT.moveTo(IMAGE_WIDTH * 0.13551401869158877, IMAGE_HEIGHT * 0.6962616822429907);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.21495327102803738, IMAGE_HEIGHT * 0.5887850467289719, IMAGE_WIDTH * 0.3177570093457944, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.46261682242990654, IMAGE_HEIGHT * 0.4252336448598131);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.6121495327102804, IMAGE_HEIGHT * 0.34579439252336447, IMAGE_WIDTH * 0.7336448598130841, IMAGE_HEIGHT * 0.3177570093457944, IMAGE_WIDTH * 0.8738317757009346, IMAGE_HEIGHT * 0.32242990654205606);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.7663551401869159, IMAGE_HEIGHT * 0.11214953271028037, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.02336448598130841, IMAGE_WIDTH * 0.3130841121495327, IMAGE_HEIGHT * 0.1308411214953271);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.09813084112149532, IMAGE_HEIGHT * 0.2383177570093458, IMAGE_WIDTH * 0.028037383177570093, IMAGE_HEIGHT * 0.48598130841121495, IMAGE_WIDTH * 0.13551401869158877, IMAGE_HEIGHT * 0.6962616822429907);
                HIGHLIGHT.closePath();
                HIGHLIGHT_START.setLocation((0.3130841121495327 * IMAGE_WIDTH), (0.13551401869158877 * IMAGE_HEIGHT));
                HIGHLIGHT_STOP.setLocation(((0.3130841121495327 + 0.1824447802691637) * IMAGE_WIDTH), ((0.13551401869158877 + 0.3580680424308394) * IMAGE_HEIGHT));
                HIGHLIGHT_GRADIENT = new LinearGradientPaint(HIGHLIGHT_START, HIGHLIGHT_STOP, HIGHLIGHT_FRACTIONS, HIGHLIGHT_COLORS);
                G2.setPaint(HIGHLIGHT_GRADIENT);
                G2.fill(HIGHLIGHT);
                break;

            case FG_TYPE3:
                HIGHLIGHT.setWindingRule(Path2D.WIND_EVEN_ODD);
                HIGHLIGHT.moveTo(IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.5093457943925234);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.2102803738317757, IMAGE_HEIGHT * 0.5560747663551402, IMAGE_WIDTH * 0.46261682242990654, IMAGE_HEIGHT * 0.5607476635514018, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5607476635514018);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.5373831775700935, IMAGE_HEIGHT * 0.5607476635514018, IMAGE_WIDTH * 0.794392523364486, IMAGE_HEIGHT * 0.5607476635514018, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.5093457943925234);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.2757009345794392, IMAGE_WIDTH * 0.7383177570093458, IMAGE_HEIGHT * 0.08411214953271028, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.08411214953271028);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.2616822429906542, IMAGE_HEIGHT * 0.08411214953271028, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.2757009345794392, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.5093457943925234);
                HIGHLIGHT.closePath();
                HIGHLIGHT_START.setLocation(0, HIGHLIGHT.getBounds2D().getMinY());
                HIGHLIGHT_STOP.setLocation(0, HIGHLIGHT.getBounds2D().getMaxY());
                HIGHLIGHT_GRADIENT = new LinearGradientPaint(HIGHLIGHT_START, HIGHLIGHT_STOP, HIGHLIGHT_FRACTIONS, HIGHLIGHT_COLORS);
                G2.setPaint(HIGHLIGHT_GRADIENT);
                G2.fill(HIGHLIGHT);
                break;

            case FG_TYPE4:
                HIGHLIGHT.setWindingRule(Path2D.WIND_EVEN_ODD);
                HIGHLIGHT.moveTo(IMAGE_WIDTH * 0.677570093457944, IMAGE_HEIGHT * 0.24299065420560748);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.7710280373831776, IMAGE_HEIGHT * 0.308411214953271, IMAGE_WIDTH * 0.822429906542056, IMAGE_HEIGHT * 0.411214953271028, IMAGE_WIDTH * 0.8130841121495327, IMAGE_HEIGHT * 0.5280373831775701);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.6542056074766355, IMAGE_WIDTH * 0.719626168224299, IMAGE_HEIGHT * 0.7570093457943925, IMAGE_WIDTH * 0.5934579439252337, IMAGE_HEIGHT * 0.7990654205607477);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.8317757009345794, IMAGE_WIDTH * 0.3691588785046729, IMAGE_HEIGHT * 0.8084112149532711, IMAGE_WIDTH * 0.2850467289719626, IMAGE_HEIGHT * 0.7289719626168224);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.2757009345794392, IMAGE_HEIGHT * 0.719626168224299, IMAGE_WIDTH * 0.2523364485981308, IMAGE_HEIGHT * 0.7149532710280374, IMAGE_WIDTH * 0.2336448598130841, IMAGE_HEIGHT * 0.7289719626168224);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.21495327102803738, IMAGE_HEIGHT * 0.7476635514018691, IMAGE_WIDTH * 0.21962616822429906, IMAGE_HEIGHT * 0.7710280373831776, IMAGE_WIDTH * 0.22897196261682243, IMAGE_HEIGHT * 0.7757009345794392);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.3317757009345794, IMAGE_HEIGHT * 0.8785046728971962, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.9158878504672897, IMAGE_WIDTH * 0.616822429906542, IMAGE_HEIGHT * 0.8691588785046729);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.7710280373831776, IMAGE_HEIGHT * 0.822429906542056, IMAGE_WIDTH * 0.8738317757009346, IMAGE_HEIGHT * 0.6915887850467289, IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.5327102803738317);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.897196261682243, IMAGE_HEIGHT * 0.3878504672897196, IMAGE_WIDTH * 0.8364485981308412, IMAGE_HEIGHT * 0.2570093457943925, IMAGE_WIDTH * 0.719626168224299, IMAGE_HEIGHT * 0.1822429906542056);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.705607476635514, IMAGE_HEIGHT * 0.17289719626168223, IMAGE_WIDTH * 0.6822429906542056, IMAGE_HEIGHT * 0.16355140186915887, IMAGE_WIDTH * 0.6635514018691588, IMAGE_HEIGHT * 0.18691588785046728);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.6542056074766355, IMAGE_HEIGHT * 0.205607476635514, IMAGE_WIDTH * 0.6682242990654206, IMAGE_HEIGHT * 0.2383177570093458, IMAGE_WIDTH * 0.677570093457944, IMAGE_HEIGHT * 0.24299065420560748);
                HIGHLIGHT.closePath();
                final Point2D HIGHLIGHT_CENTER = new Point2D.Double((0.5 * IMAGE_WIDTH), (0.5 * IMAGE_HEIGHT));
                final float[] HIGHLIGHT_FRACTIONS_4_1 = {
                    0.0f,
                    0.82f,
                    0.83f,
                    1.0f
                };
                final Color[] HIGHLIGHT_COLORS_4_1 = {
                    new Color(255, 255, 255, 0),
                    new Color(255, 255, 255, 0),
                    new Color(255, 255, 255, 0),
                    new Color(255, 255, 255, 25)
                };
                HIGHLIGHT_GRADIENT = new RadialGradientPaint(HIGHLIGHT_CENTER, (float) (0.3878504672897196 * IMAGE_WIDTH), HIGHLIGHT_FRACTIONS_4_1, HIGHLIGHT_COLORS_4_1);
                G2.setPaint(HIGHLIGHT_GRADIENT);
                G2.fill(HIGHLIGHT);

                final GeneralPath HIGHLIGHT_4 = new GeneralPath();
                HIGHLIGHT_4.setWindingRule(Path2D.WIND_EVEN_ODD);
                HIGHLIGHT_4.moveTo(IMAGE_WIDTH * 0.2616822429906542, IMAGE_HEIGHT * 0.22429906542056074);
                HIGHLIGHT_4.curveTo(IMAGE_WIDTH * 0.2850467289719626, IMAGE_HEIGHT * 0.2383177570093458, IMAGE_WIDTH * 0.2523364485981308, IMAGE_HEIGHT * 0.2850467289719626, IMAGE_WIDTH * 0.24299065420560748, IMAGE_HEIGHT * 0.3177570093457944);
                HIGHLIGHT_4.curveTo(IMAGE_WIDTH * 0.24299065420560748, IMAGE_HEIGHT * 0.35046728971962615, IMAGE_WIDTH * 0.27102803738317754, IMAGE_HEIGHT * 0.38317757009345793, IMAGE_WIDTH * 0.27102803738317754, IMAGE_HEIGHT * 0.397196261682243);
                HIGHLIGHT_4.curveTo(IMAGE_WIDTH * 0.2757009345794392, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.2616822429906542, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.2383177570093458, IMAGE_HEIGHT * 0.5093457943925234);
                HIGHLIGHT_4.curveTo(IMAGE_WIDTH * 0.22429906542056074, IMAGE_HEIGHT * 0.5420560747663551, IMAGE_WIDTH * 0.17757009345794392, IMAGE_HEIGHT * 0.6121495327102804, IMAGE_WIDTH * 0.1588785046728972, IMAGE_HEIGHT * 0.6121495327102804);
                HIGHLIGHT_4.curveTo(IMAGE_WIDTH * 0.14485981308411214, IMAGE_HEIGHT * 0.6121495327102804, IMAGE_WIDTH * 0.08878504672897196, IMAGE_HEIGHT * 0.5467289719626168, IMAGE_WIDTH * 0.1308411214953271, IMAGE_HEIGHT * 0.3691588785046729);
                HIGHLIGHT_4.curveTo(IMAGE_WIDTH * 0.14018691588785046, IMAGE_HEIGHT * 0.3364485981308411, IMAGE_WIDTH * 0.21495327102803738, IMAGE_HEIGHT * 0.20093457943925233, IMAGE_WIDTH * 0.2616822429906542, IMAGE_HEIGHT * 0.22429906542056074);
                HIGHLIGHT_4.closePath();
                final Point2D HIGHLIGHT_4_START = new Point2D.Double((0.1308411214953271 * IMAGE_WIDTH), (0.3691588785046729 * IMAGE_HEIGHT));
                final Point2D HIGHLIGHT_4_STOP = new Point2D.Double(((0.1308411214953271 + 0.1429988420131642) * IMAGE_WIDTH), ((0.3691588785046729 + 0.04371913341648399) * IMAGE_HEIGHT));
                final float[] HIGHLIGHT_FRACTIONS4_2 = {
                    0.0f,
                    1.0f
                };
                final Color[] HIGHLIGHT_FRACTIONS_4_2 = {
                    new Color(255, 255, 255, 51),
                    new Color(255, 255, 255, 0)
                };
                final LinearGradientPaint HIGHLIGHT_GRADIENT_4_2 = new LinearGradientPaint(HIGHLIGHT_4_START, HIGHLIGHT_4_STOP, HIGHLIGHT_FRACTIONS4_2, HIGHLIGHT_FRACTIONS_4_2);
                G2.setPaint(HIGHLIGHT_GRADIENT_4_2);
                G2.fill(HIGHLIGHT_4);
                break;

            case FG_TYPE5:
                HIGHLIGHT.setWindingRule(Path2D.WIND_EVEN_ODD);
                HIGHLIGHT.moveTo(IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.5);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.27102803738317754, IMAGE_WIDTH * 0.27102803738317754, IMAGE_HEIGHT * 0.08411214953271028, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.08411214953271028);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.7009345794392523, IMAGE_HEIGHT * 0.08411214953271028, IMAGE_WIDTH * 0.8644859813084113, IMAGE_HEIGHT * 0.22429906542056074, IMAGE_WIDTH * 0.9065420560747663, IMAGE_HEIGHT * 0.411214953271028);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.9112149532710281, IMAGE_HEIGHT * 0.4392523364485981, IMAGE_WIDTH * 0.9112149532710281, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.8457943925233645, IMAGE_HEIGHT * 0.5373831775700935);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.794392523364486, IMAGE_HEIGHT * 0.5467289719626168, IMAGE_WIDTH * 0.5514018691588785, IMAGE_HEIGHT * 0.411214953271028, IMAGE_WIDTH * 0.3925233644859813, IMAGE_HEIGHT * 0.45794392523364486);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.16822429906542055, IMAGE_HEIGHT * 0.5093457943925234, IMAGE_WIDTH * 0.13551401869158877, IMAGE_HEIGHT * 0.7757009345794392, IMAGE_WIDTH * 0.09345794392523364, IMAGE_HEIGHT * 0.5934579439252337);
                HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.08878504672897196, IMAGE_HEIGHT * 0.5607476635514018, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.5);
                HIGHLIGHT.closePath();
                HIGHLIGHT_START.setLocation(0, HIGHLIGHT.getBounds2D().getMinY());
                HIGHLIGHT_STOP.setLocation(0, HIGHLIGHT.getBounds2D().getMaxY());
                HIGHLIGHT_GRADIENT = new LinearGradientPaint(HIGHLIGHT_START, HIGHLIGHT_STOP, HIGHLIGHT_FRACTIONS, HIGHLIGHT_COLORS);
                G2.setPaint(HIGHLIGHT_GRADIENT);
                G2.fill(HIGHLIGHT);
                break;
        }

        G2.dispose();

        if (FOREGROUND_IMAGE != null) {
            final Graphics2D G = FOREGROUND_IMAGE.createGraphics();
            G.drawImage(radForegroundImage, 0, 0, null);
            G.dispose();
        }
        // Cache current values
        radWidth = WIDTH;
        radWithCenterKnob = WITH_CENTER_KNOB;
        radType = TYPE;

        return radForegroundImage;
    }

    /**
     * Creates the foreground image for a linear gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * @param WIDTH
     * @param HEIGHT
     * @return a buffered image that contains the foreground image of a linear gauge
     */
    public BufferedImage createLinearForeground(final int WIDTH, final int HEIGHT) {
        return createLinearForeground(WIDTH, HEIGHT, false);
    }

    /**
     * Creates the foreground image for a linear gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * @param WIDTH
     * @param HEIGHT
     * @param WITH_CENTER_KNOB
     * @return a buffered image that contains the foreground image of a linear gauge
     */
    public BufferedImage createLinearForeground(final int WIDTH, final int HEIGHT, final boolean WITH_CENTER_KNOB) {
        return createLinearForeground(WIDTH, HEIGHT, WITH_CENTER_KNOB, null);
    }

    /**
     * Creates the foreground image for a linear gauge.
     * The image parameters and the image will be cached. If the
     * current request has the same parameters as the last request
     * it will return the already created image instead of creating
     * a new image.
     * @param WIDTH
     * @param HEIGHT
     * @param WITH_CENTER_KNOB
     * @param FOREGROUND_IMAGE
     * @return a buffered image that contains the foreground image of a linear gauge
     */
    public BufferedImage createLinearForeground(final int WIDTH, final int HEIGHT, final boolean WITH_CENTER_KNOB, final BufferedImage FOREGROUND_IMAGE) {
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        // Take image from cache instead of creating a new one if parameters are the same as last time
        if (linWidth == WIDTH && linHeight == HEIGHT && linWithCenterKnob == WITH_CENTER_KNOB) {
            if (FOREGROUND_IMAGE != null) {
                final Graphics2D G2 = FOREGROUND_IMAGE.createGraphics();
                G2.drawImage(linForegroundImage, 0, 0, null);
                G2.dispose();
            }
            return linForegroundImage;
        }

        linForegroundImage.flush();
        linForegroundImage = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = linForegroundImage.createGraphics();

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = WIDTH;
        final int IMAGE_HEIGHT = HEIGHT;

        if (WITH_CENTER_KNOB) {
            final Ellipse2D E_CENTER_KNOB_FRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4579439163208008, IMAGE_HEIGHT * 0.4579439163208008, IMAGE_WIDTH * 0.08411216735839844, IMAGE_HEIGHT * 0.08411216735839844);
            final Point2D E_CENTER_KNOB_FRAME_START = new Point2D.Double(0, E_CENTER_KNOB_FRAME.getBounds2D().getMinY());
            final Point2D E_CENTER_KNOB_FRAME_STOP = new Point2D.Double(0, E_CENTER_KNOB_FRAME.getBounds2D().getMaxY());
            final float[] E_CENTER_KNOB_FRAME_FRACTIONS = {
                0.0f,
                0.46f,
                1.0f
            };
            final Color[] E_CENTER_KNOB_FRAME_COLORS = {
                new Color(180, 180, 180, 255),
                new Color(63, 63, 63, 255),
                new Color(40, 40, 40, 255)
            };
            final LinearGradientPaint E_CENTER_KNOB_FRAME_GRADIENT = new LinearGradientPaint(E_CENTER_KNOB_FRAME_START, E_CENTER_KNOB_FRAME_STOP, E_CENTER_KNOB_FRAME_FRACTIONS, E_CENTER_KNOB_FRAME_COLORS);
            G2.setPaint(E_CENTER_KNOB_FRAME_GRADIENT);
            G2.fill(E_CENTER_KNOB_FRAME);

            final Ellipse2D E_CENTER_KNOB_MAIN = new Ellipse2D.Double(IMAGE_WIDTH * 0.4672897160053253, IMAGE_HEIGHT * 0.4672897160053253, IMAGE_WIDTH * 0.06542053818702698, IMAGE_HEIGHT * 0.06542053818702698);
            final Point2D E_CENTER_KNOB_MAIN_START = new Point2D.Double(0, E_CENTER_KNOB_MAIN.getBounds2D().getMinY());
            final Point2D E_CENTER_KNOB_MAIN_STOP = new Point2D.Double(0, E_CENTER_KNOB_MAIN.getBounds2D().getMaxY());
            final float[] E_CENTER_KNOB_MAIN_FRACTIONS = {
                0.0f,
                1.0f
            };
            final Color[] E_CENTER_KNOB_MAIN_COLORS = {
                new Color(217, 217, 217, 255),
                new Color(191, 191, 191, 255)
            };
            final LinearGradientPaint E_CENTER_KNOB_MAIN_GRADIENT = new LinearGradientPaint(E_CENTER_KNOB_MAIN_START, E_CENTER_KNOB_MAIN_STOP, E_CENTER_KNOB_MAIN_FRACTIONS, E_CENTER_KNOB_MAIN_COLORS);
            G2.setPaint(E_CENTER_KNOB_MAIN_GRADIENT);
            G2.fill(E_CENTER_KNOB_MAIN);

            final Ellipse2D E_CENTER_KNOB_INNERSHADOW = new Ellipse2D.Double(IMAGE_WIDTH * 0.4672897160053253, IMAGE_HEIGHT * 0.4672897160053253, IMAGE_WIDTH * 0.06542053818702698, IMAGE_HEIGHT * 0.06542053818702698);
            final Point2D E_CENTER_KNOB_INNERSHADOW_CENTER = new Point2D.Double((0.4953271028037383 * IMAGE_WIDTH), (0.49065420560747663 * IMAGE_HEIGHT));
            final float[] E_CENTER_KNOB_INNERSHADOW_FRACTIONS = {
                0.0f,
                0.75f,
                0.76f,
                1.0f
            };
            final Color[] E_CENTER_KNOB_INNERSHADOW_COLORS = {
                new Color(0, 0, 0, 0),
                new Color(0, 0, 0, 0),
                new Color(0, 0, 0, 1),
                new Color(0, 0, 0, 51)
            };
            final RadialGradientPaint E_CENTER_KNOB_INNERSHADOW_GRADIENT = new RadialGradientPaint(E_CENTER_KNOB_INNERSHADOW_CENTER, (float) (0.03271028037383177 * IMAGE_WIDTH), E_CENTER_KNOB_INNERSHADOW_FRACTIONS, E_CENTER_KNOB_INNERSHADOW_COLORS);
            G2.setPaint(E_CENTER_KNOB_INNERSHADOW_GRADIENT);
            G2.fill(E_CENTER_KNOB_INNERSHADOW);
        }

        // Highlight
        final GeneralPath GLASSEFFECT = new GeneralPath();
        GLASSEFFECT.setWindingRule(Path2D.WIND_EVEN_ODD);
        final Point2D GLASSEFFECT_START;
        final Point2D GLASSEFFECT_STOP;
        final LinearGradientPaint GLASSEFFECT_GRADIENT;

        if (WIDTH >= HEIGHT) {
            // Horizontal glass effect
            GLASSEFFECT.moveTo(18, IMAGE_HEIGHT - 18);
            GLASSEFFECT.lineTo(IMAGE_WIDTH - 18, IMAGE_HEIGHT - 18);
            GLASSEFFECT.curveTo(IMAGE_WIDTH - 18, IMAGE_HEIGHT - 18, IMAGE_WIDTH - 27, IMAGE_HEIGHT * 0.7, IMAGE_WIDTH - 27, IMAGE_HEIGHT * 0.5);
            GLASSEFFECT.curveTo(IMAGE_WIDTH - 27, 27, IMAGE_WIDTH - 18, 18, IMAGE_WIDTH - 18, 18);
            GLASSEFFECT.lineTo(18, 18);
            GLASSEFFECT.curveTo(18, 18, 27, IMAGE_HEIGHT * 0.2857142857142857, 27, IMAGE_HEIGHT * 0.5);
            GLASSEFFECT.curveTo(27, IMAGE_HEIGHT * 0.7, 18, IMAGE_HEIGHT - 18, 18, IMAGE_HEIGHT - 18);
            GLASSEFFECT.closePath();
            GLASSEFFECT_START = new Point2D.Double(0, GLASSEFFECT.getBounds2D().getMaxY());
            GLASSEFFECT_STOP = new Point2D.Double(0, GLASSEFFECT.getBounds2D().getMinY());
        } else {
            // Vertical glass effect
            GLASSEFFECT.setWindingRule(Path2D.WIND_EVEN_ODD);
            GLASSEFFECT.moveTo(18, 18);
            GLASSEFFECT.lineTo(18, IMAGE_HEIGHT - 18);
            GLASSEFFECT.curveTo(18, IMAGE_HEIGHT - 18, 27, IMAGE_HEIGHT - 27, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT - 27);
            GLASSEFFECT.curveTo(IMAGE_WIDTH - 27, IMAGE_HEIGHT - 27, IMAGE_WIDTH - 18, IMAGE_HEIGHT - 18, IMAGE_WIDTH - 18, IMAGE_HEIGHT - 18);
            GLASSEFFECT.lineTo(IMAGE_WIDTH - 18, 18);
            GLASSEFFECT.curveTo(IMAGE_WIDTH - 18, 18, IMAGE_WIDTH - 27, 27, IMAGE_WIDTH * 0.5, 27);
            GLASSEFFECT.curveTo(27, 27, 18, 18, 18, 18);
            GLASSEFFECT.closePath();
            GLASSEFFECT_START = new Point2D.Double(GLASSEFFECT.getBounds2D().getMinX(), 0);
            GLASSEFFECT_STOP = new Point2D.Double(GLASSEFFECT.getBounds2D().getMaxX(), 0);
        }

        final float[] GLASSEFFECT_FRACTIONS = {
            0.0f,
            0.06f,
            0.07f,
            0.12f,
            0.17f,
            0.1701f,
            0.79f,
            0.8f,
            0.84f,
            0.93f,
            0.94f,
            0.96f,
            0.97f,
            1.0f
        };
        final Color[] GLASSEFFECT_COLORS = {
            new Color(255, 255, 255, 0),
            new Color(255, 255, 255, 0),
            new Color(255, 255, 255, 0),
            new Color(255, 255, 255, 0),
            new Color(255, 255, 255, 3),
            new Color(255, 255, 255, 5),
            new Color(255, 255, 255, 5),
            new Color(255, 255, 255, 5),
            new Color(255, 255, 255, 20),
            new Color(255, 255, 255, 73),
            new Color(255, 255, 255, 76),
            new Color(255, 255, 255, 30),
            new Color(255, 255, 255, 10),
            new Color(255, 255, 255, 5)
        };
        GLASSEFFECT_GRADIENT = new LinearGradientPaint(GLASSEFFECT_START, GLASSEFFECT_STOP, GLASSEFFECT_FRACTIONS, GLASSEFFECT_COLORS);
        G2.setPaint(GLASSEFFECT_GRADIENT);
        G2.fill(GLASSEFFECT);

        G2.dispose();

        if (FOREGROUND_IMAGE != null) {
            final Graphics2D G = FOREGROUND_IMAGE.createGraphics();
            G.drawImage(linForegroundImage, 0, 0, null);
            G.dispose();
        }
        // Cache current values
        linWidth = WIDTH;
        linHeight = HEIGHT;
        linWithCenterKnob = WITH_CENTER_KNOB;

        return linForegroundImage;
    }

    @Override
    public String toString() {
        return "ForegroundImageFactory";
    }
}
