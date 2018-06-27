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
package org.mars_sim.msp.ui.steelseries.gauges;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.border.Border;

import org.mars_sim.msp.ui.steelseries.tools.GlowImageFactory;
import org.mars_sim.msp.ui.steelseries.tools.GradientWrapper;
import org.mars_sim.msp.ui.steelseries.tools.LcdColor;
import org.mars_sim.msp.ui.steelseries.tools.NumberSystem;
import org.mars_sim.msp.ui.steelseries.tools.Section;
import org.mars_sim.msp.ui.steelseries.tools.Util;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.ease.TimelineEase;


/**
 *
 * @author hansolo
 */
public final class DisplaySingle extends JComponent implements Lcd, ActionListener
{
    // <editor-fold defaultstate="collapsed" desc="Variable declaration">

    private final Util UTIL = Util.INSTANCE;
    private LcdColor lcdColor = LcdColor.WHITE_LCD;
    private Paint customLcdBackground = Color.BLACK;
    private Color customLcdForeground = Color.WHITE;
    private final Rectangle INNER_BOUNDS = new Rectangle(0, 0, 128, 48);
    private double lcdValue;
    private double lcdMinValue;
    private double lcdMaxValue;
    private double lcdThreshold;
    private boolean lcdThresholdVisible;
    private boolean lcdThresholdBehaviourInverted;
    private boolean lcdBackgroundVisible;
    private boolean lcdTextVisible;
    private boolean lcdBlinking;
    private final Timer LCD_BLINKING_TIMER;
    private BufferedImage lcdThresholdImage;
    private static final String LCD_VALUE_PROPERTY = "lcdValue";
    private static final String LCD_TEXT_PROPERTY = "lcdText";
    private int lcdDecimals;
    private String lcdUnitString;
    private boolean lcdUnitStringVisible;
    private boolean lcdScientificFormat;
    private boolean digitalFont;
    private boolean useCustomLcdUnitFont;
    private Font customLcdUnitFont;
    private Font lcdValueFont;
    private Font lcdUnitFont;
    private String lcdInfoString;
    private final Font LCD_STANDARD_FONT;
    private final Font LCD_DIGITAL_FONT;
    private BufferedImage bgImage;
    private Color fgColor;
    private BufferedImage lcdImage;
    private NumberSystem numberSystem;
    private Shape disabledShape;
    private final Color DISABLED_COLOR;
    private Timeline timeline;
    private final transient TimelineEase EASING;
    private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
    private TextLayout unitLayout;
    private final Rectangle2D UNIT_BOUNDARY = new Rectangle2D.Double();
    private TextLayout valueLayout;
    private final Rectangle2D VALUE_BOUNDARY = new Rectangle2D.Double();
    private Font lcdInfoFont;
    private TextLayout infoLayout;
    private final Rectangle2D INFO_BOUNDARY = new Rectangle2D.Double();
    private boolean sectionsVisible;
    private ArrayList<Section> sections;
    private ArrayList<BufferedImage> sectionsBackground;
    private ArrayList<Color> sectionsForeground;
    private boolean qualityOverlayVisible;
    private double overlayCornerRadius;
    private float overlayFactor;
    private Insets overlayInsets;
    private float factor;
    private Color[] overlayColors;
    private float[] qualityOverlayFractions;
    private Color[] qualityOverlayColors;
    private GradientWrapper qualityOverlayLookup;
    private LinearGradientPaint qualityOverlayGradient;
    private RoundRectangle2D qualityOverlay;
    private boolean glowVisible;
    private Color glowColor;
    private boolean glowing;
    private BufferedImage glowImageOn;
    private boolean lcdNnumericValues;
    private boolean bargraphVisible;
    private List<Shape> bargraph;
    private double bargraphSegmentFactor;
    private boolean plainBargraphSegments;
    private String lcdText;
    private float lcdTextX;
    private Timer TEXT_SCROLLER;
    private final transient ComponentListener COMPONENT_LISTENER = new ComponentAdapter() {

        @Override
        public void componentResized(ComponentEvent event) {
            java.awt.Container parent = getParent();

            if (getWidth() < getMinimumSize().width && getHeight() < getMinimumSize().height) {
                if (parent != null && getParent().getLayout() == null) {
                    setSize(getMinimumSize());
                } else {
                    setPreferredSize(getMinimumSize());
                }
            }

            if (parent != null && getParent().getLayout() == null) {
                setSize(getWidth(), getHeight());
            } else {
                setPreferredSize(new Dimension(getWidth(), getHeight()));
            }

            calcInnerBounds();
            init(getInnerBounds().width, getInnerBounds().height);
            //revalidate();
            //repaint();
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public DisplaySingle() {
        super();
        lcdValue = 0.0;
        lcdMinValue = 0.0;
        lcdMaxValue = 100.0;
        lcdThreshold = 0.0;
        lcdThresholdVisible = false;
        lcdThresholdBehaviourInverted = false;
        lcdBackgroundVisible = true;
        lcdTextVisible = true;
        lcdBlinking = false;
        LCD_BLINKING_TIMER = new Timer(500, this);
        lcdDecimals = 1;
        lcdUnitString = "unit";
        lcdUnitStringVisible = true;
        lcdScientificFormat = false;
        digitalFont = false;
        useCustomLcdUnitFont = false;
        lcdInfoString = "";
        customLcdUnitFont = new Font("Verdana", 0, 24);
        LCD_STANDARD_FONT = new Font("Verdana", 0, 24);
        LCD_DIGITAL_FONT = Util.INSTANCE.getDigitalFont().deriveFont(24).deriveFont(Font.PLAIN);
        lcdInfoFont = new Font("Verdana", 0, 24);
        numberSystem = NumberSystem.DEC;
        DISABLED_COLOR = new Color(102, 102, 102, 178);
        timeline = new Timeline(this);
        EASING = new org.pushingpixels.trident.ease.Linear();
        sectionsVisible = false;
        sections = new ArrayList<Section>(3);
        sectionsBackground = new ArrayList<BufferedImage>(3);
        sectionsForeground = new ArrayList<Color>(3);
        qualityOverlayVisible = false;
        overlayCornerRadius = 0;
        overlayFactor = 0;
        factor = 0;
        overlayInsets = new Insets(6, 6, 6, 6);
        overlayColors = new Color[]{ Color.RED, Color.RED.darker(), Color.RED };
        qualityOverlayFractions = new float[] { 0.0f, 0.2f, 0.5f, 0.75f, 0.9f, 1.0f };
        qualityOverlayColors = new Color[] { Color.RED, Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.GREEN };
        qualityOverlayLookup = new GradientWrapper(new Point2D.Double(INNER_BOUNDS.getBounds2D().getMinX(), 0), new Point2D.Double(INNER_BOUNDS.getMaxX(), 0), qualityOverlayFractions, qualityOverlayColors);
        qualityOverlayGradient = new LinearGradientPaint(new Point2D.Double(0, 2), new Point2D.Double(0, INNER_BOUNDS.height - 2), new float[]{0.0f, 0.5f, 1.0f}, new Color[]{Color.RED, Color.RED.darker(), Color.RED});
        qualityOverlay = new RoundRectangle2D.Double();
        glowVisible = false;
        glowColor = new Color(51, 255, 255);
        glowing = false;
        bargraphVisible = false;
        bargraph = new ArrayList<Shape>(20);
        bargraphSegmentFactor = 0.2;
        plainBargraphSegments = true;
        lcdNnumericValues = true;
        lcdText = "";
        lcdTextX = 0f;
        TEXT_SCROLLER = new Timer(10, this);
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        addComponentListener(COMPONENT_LISTENER);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    public final void init(final int WIDTH, final int HEIGHT) {
        if (WIDTH <= 1 || HEIGHT <= 1) {
            return;
        }

        if (bgImage != null) {
            bgImage.flush();
        }

        if (lcdImage != null) {
            lcdImage.flush();
        }

        if (glowImageOn != null) {
            glowImageOn.flush();
        }
        glowImageOn = GlowImageFactory.INSTANCE.createLcdGlow(WIDTH, HEIGHT, glowColor, true);

        lcdImage = create_LCD_Image(WIDTH, HEIGHT, null);
        final double CORNER_RADIUS = WIDTH > HEIGHT ? (HEIGHT * 0.095) : (WIDTH * 0.095);
        disabledShape = new RoundRectangle2D.Double(0, 0, WIDTH, HEIGHT, CORNER_RADIUS, CORNER_RADIUS);
        if (isDigitalFont()) {
            lcdValueFont = LCD_DIGITAL_FONT.deriveFont(0.7f * getInnerBounds().height).deriveFont(Font.PLAIN);
            if (useCustomLcdUnitFont) {
                lcdUnitFont = customLcdUnitFont.deriveFont(0.2f * getInnerBounds().height);
            } else {
                lcdUnitFont = LCD_STANDARD_FONT.deriveFont(0.2f * getInnerBounds().height);
            }
        } else {
            lcdValueFont = LCD_STANDARD_FONT.deriveFont(0.625f * getInnerBounds().height);
            if (useCustomLcdUnitFont) {
                lcdUnitFont = customLcdUnitFont.deriveFont(0.2f * getInnerBounds().height);
            } else {
                lcdUnitFont = LCD_STANDARD_FONT.deriveFont(0.2f * getInnerBounds().height);
            }
        }
        lcdInfoFont = LCD_STANDARD_FONT.deriveFont(Font.BOLD, 0.18f * getInnerBounds().height);

        if (lcdThresholdImage != null) {
            lcdThresholdImage.flush();
        }
        lcdThresholdImage = create_LCD_THRESHOLD_Image((int) (HEIGHT * 0.2045454545), (int) (HEIGHT * 0.2045454545), lcdColor.TEXT_COLOR);

        if (!sections.isEmpty()) {
            sectionsBackground.clear();
            sectionsForeground.clear();

            final float[] HSB_START = (Color.RGBtoHSB(lcdColor.GRADIENT_START_COLOR.getRed(), lcdColor.GRADIENT_START_COLOR.getGreen(), lcdColor.GRADIENT_START_COLOR.getBlue(), null));
            final float[] HSB_FRACTION1 = (Color.RGBtoHSB(lcdColor.GRADIENT_FRACTION1_COLOR.getRed(), lcdColor.GRADIENT_FRACTION1_COLOR.getGreen(), lcdColor.GRADIENT_FRACTION1_COLOR.getBlue(), null));
            final float[] HSB_FRACTION2 = (Color.RGBtoHSB(lcdColor.GRADIENT_FRACTION2_COLOR.getRed(), lcdColor.GRADIENT_FRACTION2_COLOR.getGreen(), lcdColor.GRADIENT_FRACTION2_COLOR.getBlue(), null));
            final float[] HSB_FRACTION3 = (Color.RGBtoHSB(lcdColor.GRADIENT_FRACTION3_COLOR.getRed(), lcdColor.GRADIENT_FRACTION3_COLOR.getGreen(), lcdColor.GRADIENT_FRACTION3_COLOR.getBlue(), null));
            final float[] HSB_STOP = (Color.RGBtoHSB(lcdColor.GRADIENT_STOP_COLOR.getRed(), lcdColor.GRADIENT_STOP_COLOR.getGreen(), lcdColor.GRADIENT_STOP_COLOR.getBlue(), null));

            // Hue values of the gradient colors
            final float HUE_START = HSB_START[0];
            final float HUE_FRACTION1 = HSB_FRACTION1[0];
            final float HUE_FRACTION2 = HSB_FRACTION2[0];
            final float HUE_FRACTION3 = HSB_FRACTION3[0];
            final float HUE_STOP = HSB_STOP[0];

            // Brightness values of the gradient colors
            final float BRIGHTNESS_START = HSB_START[2];
            final float BRIGHTNESS_FRACTION1 = HSB_FRACTION1[2];
            final float BRIGHTNESS_FRACTION2 = HSB_FRACTION2[2];
            final float BRIGHTNESS_FRACTION3 = HSB_FRACTION3[2];
            final float BRIGHTNESS_STOP = HSB_STOP[2];

            for (Section section : sections) {
                final Color[] BACKGROUND_COLORS;
                final Color FOREGROUND_COLOR;
                final float[] HSB_SECTION = Color.RGBtoHSB(section.getColor().getRed(), section.getColor().getGreen(), section.getColor().getBlue(), null);
                final float HUE_SECTION = HSB_SECTION[0];
                final float SATURATION_SECTION = HSB_SECTION[1];
                final float BRIGHTNESS_SECTION = HSB_SECTION[2];
                if (!UTIL.isMonochrome(section.getColor())) {
                    // Section color is not monochrome
                    if (lcdColor == LcdColor.SECTIONS_LCD) {
                        BACKGROUND_COLORS = new Color[]{
                            new Color(Color.HSBtoRGB(HUE_SECTION, SATURATION_SECTION, BRIGHTNESS_START - 0.31f)),
                            new Color(Color.HSBtoRGB(HUE_SECTION, SATURATION_SECTION, BRIGHTNESS_FRACTION1 - 0.31f)),
                            new Color(Color.HSBtoRGB(HUE_SECTION, SATURATION_SECTION, BRIGHTNESS_FRACTION2 - 0.31f)),
                            new Color(Color.HSBtoRGB(HUE_SECTION, SATURATION_SECTION, BRIGHTNESS_FRACTION3 - 0.31f)),
                            new Color(Color.HSBtoRGB(HUE_SECTION, SATURATION_SECTION, BRIGHTNESS_STOP - 0.31f))
                        };
                    } else {
                        final float HUE_DIFF = HUE_SECTION - HUE_FRACTION3;
                        BACKGROUND_COLORS = new Color[]{
                            UTIL.setHue(lcdColor.GRADIENT_START_COLOR, (HUE_START + HUE_DIFF) % 360),
                            UTIL.setHue(lcdColor.GRADIENT_FRACTION1_COLOR, (HUE_FRACTION1 + HUE_DIFF) % 360),
                            UTIL.setHue(lcdColor.GRADIENT_FRACTION2_COLOR, (HUE_FRACTION2 + HUE_DIFF) % 360),
                            UTIL.setHue(lcdColor.GRADIENT_FRACTION3_COLOR, (HUE_FRACTION3 + HUE_DIFF) % 360),
                            UTIL.setHue(lcdColor.GRADIENT_STOP_COLOR, (HUE_STOP + HUE_DIFF) % 360)
                        };
                    }
                    FOREGROUND_COLOR = UTIL.setSaturationBrightness(section.getColor(), 0.57f, 0.83f);
                } else {
                    // Section color is monochrome
                    final float BRIGHTNESS_DIFF = BRIGHTNESS_SECTION - BRIGHTNESS_FRACTION1;

                    BACKGROUND_COLORS = new Color[]{
                        UTIL.setSaturationBrightness(lcdColor.GRADIENT_START_COLOR, 0, BRIGHTNESS_START + BRIGHTNESS_DIFF),
                        UTIL.setSaturationBrightness(lcdColor.GRADIENT_FRACTION1_COLOR, 0, BRIGHTNESS_FRACTION1 + BRIGHTNESS_DIFF),
                        UTIL.setSaturationBrightness(lcdColor.GRADIENT_FRACTION2_COLOR, 0, BRIGHTNESS_FRACTION2 + BRIGHTNESS_DIFF),
                        UTIL.setSaturationBrightness(lcdColor.GRADIENT_FRACTION3_COLOR, 0, BRIGHTNESS_FRACTION3 + BRIGHTNESS_DIFF),
                        UTIL.setSaturationBrightness(lcdColor.GRADIENT_STOP_COLOR, 0, BRIGHTNESS_STOP + BRIGHTNESS_DIFF)
                    };
                    if (UTIL.isDark(section.getColor())) {
                        FOREGROUND_COLOR = Color.WHITE;
                    } else {
                        FOREGROUND_COLOR = Color.BLACK;
                    }
                }
                sectionsBackground.add(create_LCD_Image(WIDTH, HEIGHT, BACKGROUND_COLORS));
                sectionsForeground.add(FOREGROUND_COLOR);
            }
        }

        // Quality overlay related parameters
        overlayCornerRadius = WIDTH > HEIGHT ? (HEIGHT * 0.095) - 1 : (WIDTH * 0.095) - 1;
        overlayFactor = (float) (lcdValue / (lcdMaxValue - lcdMinValue));
        if (Double.compare(overlayFactor, 1.0) > 0) {
            factor = 1.0f;
        } else if (Double.compare(overlayFactor, 0) < 0) {
            factor = 0.0f;
        } else {
            factor = overlayFactor;
        }
        overlayColors = new Color[] {
            UTIL.setAlpha(qualityOverlayLookup.getColorAt(factor), 0.5f),
            UTIL.setAlpha(qualityOverlayLookup.getColorAt(factor).darker(), 0.5f),
            UTIL.setAlpha(qualityOverlayLookup.getColorAt(factor), 0.5f)
        };
        final int INSET = (int) (qualityOverlay.getHeight() * 0.0909090909);
        overlayInsets.set(INSET, INSET, INSET, INSET);
        qualityOverlayLookup = new GradientWrapper(new Point2D.Double(overlayInsets.left, 0), new Point2D.Double(lcdImage.getMinX() + lcdImage.getWidth() - overlayInsets.right, 0), qualityOverlayFractions, qualityOverlayColors);
        qualityOverlayGradient = new LinearGradientPaint(new Point2D.Double(0, overlayInsets.top), new Point2D.Double(0, HEIGHT - overlayInsets.bottom), new float[]{0.0f, 0.5f, 1.0f}, overlayColors);
        qualityOverlay.setRoundRect(overlayInsets.left, overlayInsets.top, (INNER_BOUNDS.width * overlayFactor) - overlayInsets.left - overlayInsets.right, INNER_BOUNDS.height - overlayInsets.top - overlayInsets.bottom, overlayCornerRadius, overlayCornerRadius);

        // Prepare bargraph
        bargraphSegmentFactor = 20 / (lcdMaxValue - lcdMinValue);
        prepareBargraph(WIDTH, HEIGHT);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Visualization">
    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D G2 = (Graphics2D) g.create();

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        G2.translate(getInnerBounds().x, getInnerBounds().y);

        // Draw background
        if (sectionsVisible && !sections.isEmpty()) {
            for (int i = 0; i < sections.size(); i++) {
                if (Double.compare(lcdValue, sections.get(i).getStart()) >= 0 && Double.compare(lcdValue, sections.get(i).getStop()) <= 0) {
                    bgImage = sectionsBackground.get(i);
                    fgColor = sectionsForeground.get(i);
                    break;
                } else {
                    bgImage = lcdImage;
                    fgColor = lcdColor.TEXT_COLOR;
                }
            }

            if (bgImage == null) {
                bgImage = lcdImage;
                fgColor = null;
            }

        } else {
            bgImage = lcdImage;
            fgColor = null;
        }
        if (lcdBackgroundVisible) {
        G2.drawImage(bgImage, 0, 0, null);
        }

        // Draw bargraph
        if (bargraphVisible) {
            int activeSegments = (int) (lcdValue * bargraphSegmentFactor);
            for (int i = 0 ; i < 20 ; i++) {
                if (i < activeSegments) {
                    if (!sections.isEmpty()) {
                        for (int j = 0; j < sections.size(); j++) {
                            if (Double.compare(lcdValue, sections.get(j).getStart()) >= 0 && Double.compare(lcdValue, sections.get(j).getStop()) <= 0) {
                                Paint fill;
                                if (plainBargraphSegments) {
                                    fill = sections.get(j).getColor();
                                } else {
                                    fill = new RadialGradientPaint((float)bargraph.get(i).getBounds2D().getCenterX(), (float)bargraph.get(i).getBounds2D().getCenterY(), (float)bargraph.get(i).getBounds2D().getWidth() / 2, new float[]{0.0f, 1.0f}, new Color[]{sections.get(j).getColor().brighter(), sections.get(j).getColor().darker()});
                                }
                                G2.setPaint(fill);
                                break;
                            } else {
                                G2.setPaint(lcdColor.TEXT_COLOR);
                            }
                        }
                    } else {
                        G2.setPaint(lcdColor.TEXT_COLOR);
                    }
                    G2.fill(bargraph.get(i));
                }
            }
        }

        // Draw quality overlay
        if (qualityOverlayVisible && lcdValue > lcdMinValue) {
            G2.setPaint(qualityOverlayGradient);
            G2.fill(qualityOverlay);
        }

        // Draw lcd text
        if (fgColor == null) {
            if (lcdColor == LcdColor.CUSTOM) {
                G2.setColor(customLcdForeground);
            } else {
                G2.setColor(lcdColor.TEXT_COLOR);
            }
        } else {
            G2.setColor(fgColor);
        }

        if (lcdNnumericValues) {
            G2.setFont(lcdUnitFont);
            final double UNIT_STRING_WIDTH;
            final double digitalFontOffset = digitalFont ? lcdImage.getWidth() * 0.0625 : 0;
            // Draw unit string
            if (lcdUnitStringVisible && !lcdUnitString.isEmpty()) {
                unitLayout = new TextLayout(lcdUnitString, G2.getFont(), RENDER_CONTEXT);
                UNIT_BOUNDARY.setFrame(unitLayout.getBounds());
                if (lcdTextVisible) {
                G2.drawString(lcdUnitString, (int) ((lcdImage.getWidth() - UNIT_BOUNDARY.getWidth()) - lcdImage.getHeight() * 0.15f), (int) (lcdImage.getHeight() * 0.76f));
                }
                UNIT_STRING_WIDTH = UNIT_BOUNDARY.getWidth();
            } else {
                UNIT_STRING_WIDTH = 0;
            }

            // Draw value
            G2.setFont(lcdValueFont);
            switch (numberSystem) {
                case HEX:
                    valueLayout = new TextLayout(Integer.toHexString((int) lcdValue).toUpperCase(), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    if (lcdTextVisible) {
                    G2.drawString(Integer.toHexString((int) lcdValue).toUpperCase(), (float) ((lcdImage.getMinX() + (lcdImage.getWidth() - UNIT_STRING_WIDTH - VALUE_BOUNDARY.getWidth() - digitalFontOffset) - lcdImage.getHeight() * 0.3)), (lcdImage.getHeight() * 0.76f));
                    }
                    break;

                case OCT:
                    valueLayout = new TextLayout(Integer.toOctalString((int) lcdValue), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    if (lcdTextVisible) {
                    G2.drawString(Integer.toOctalString((int) lcdValue), (float) ((lcdImage.getMinX() + (lcdImage.getWidth() - UNIT_STRING_WIDTH - VALUE_BOUNDARY.getWidth() - digitalFontOffset) - lcdImage.getHeight() * 0.3)), (lcdImage.getHeight() * 0.76f));
                    }
                    break;

                case DEC:

                default:
                    valueLayout = new TextLayout(formatLcdValue(lcdValue), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    if (lcdTextVisible) {
                    G2.drawString(formatLcdValue(lcdValue), (float)((lcdImage.getMinX() + (lcdImage.getWidth() - UNIT_STRING_WIDTH - VALUE_BOUNDARY.getWidth() - digitalFontOffset) - lcdImage.getHeight() * 0.3)), (lcdImage.getHeight() * 0.76f));
                    }
                    break;
            }

            // Draw lcd info string
            if (!lcdInfoString.isEmpty()) {
                G2.setFont(lcdInfoFont);
                infoLayout = new TextLayout(lcdInfoString, G2.getFont(), RENDER_CONTEXT);
                INFO_BOUNDARY.setFrame(infoLayout.getBounds());
                G2.drawString(lcdInfoString, 5f, (float) INFO_BOUNDARY.getHeight() + 2f);
            }
        } else {
            // Draw text instead of numbers
            G2.setFont(lcdValueFont);
            if (lcdText != null) // 09-21-2016 Added this to avoid NullPointerException
	            if (!lcdText.isEmpty()) {
	                valueLayout = new TextLayout(lcdText, G2.getFont(), RENDER_CONTEXT);
	                VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
	                if (!TEXT_SCROLLER.isRunning()) {
	                    lcdTextX = (float) VALUE_BOUNDARY.getWidth();
	                }
	                G2.drawString(lcdText, lcdImage.getWidth() - lcdTextX  - lcdImage.getHeight() * 0.15f, (lcdImage.getHeight() * 0.76f));
	            }
        }

        // Draw lcd threshold indicator
        if (numberSystem == NumberSystem.DEC && lcdThresholdVisible) {
            if (!lcdThresholdBehaviourInverted) {
                if (lcdValue >= lcdThreshold) {
                    G2.drawImage(lcdThresholdImage, 5, getHeight() - lcdThresholdImage.getHeight() - 5, null);
                }
            } else {
                if (lcdValue <= lcdThreshold) {
                    G2.drawImage(lcdThresholdImage, 5, getHeight() - lcdThresholdImage.getHeight() - 5, null);
                }
            }
        }

        if (glowVisible && glowing) {
            G2.drawImage(glowImageOn, 0, 0, null);
        }

        if (!isEnabled()) {
            G2.setColor(DISABLED_COLOR);
            G2.fill(disabledShape);
        }

        G2.dispose();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">
    /**
     * Returns the current component as buffered image.
     * To save this buffered image as png you could use for example:
     * File file = new File("image.png");
     * ImageIO.write(Image, "png", file);
     * @return the current component as buffered image
     */
    public BufferedImage getAsImage() {
        final BufferedImage IMAGE = UTIL.createImage(getWidth(), getHeight(), Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        paintAll(G2);
        G2.dispose();
        return IMAGE;
    }

    @Override
    public double getLcdValue() {
        return this.lcdValue;
    }

    @Override
    public void setLcdValue(final double LCD_VALUE) {
        final double OLD_VALUE = lcdValue;
        lcdValue = LCD_VALUE;
        if (numberSystem != NumberSystem.DEC) {
            if (lcdValue < 0) {
                setLcdNumberSystem(NumberSystem.DEC);
            }
        }

        if (qualityOverlayVisible) {
            overlayCornerRadius = INNER_BOUNDS.width > INNER_BOUNDS.height ? (INNER_BOUNDS.height * 0.095) - 1 : (INNER_BOUNDS.width * 0.095) - 1;
            overlayFactor = (float) (lcdValue / (lcdMaxValue - lcdMinValue));
            if (Double.compare(overlayFactor, 1.0) > 0) {
                factor = 1.0f;
            } else if (Double.compare(overlayFactor, 0) < 0) {
                factor = 0.0f;
            } else {
                factor = overlayFactor;
            }
            overlayColors = new Color[] {
                UTIL.setAlpha(qualityOverlayLookup.getColorAt(factor), 0.5f),
                UTIL.setAlpha(qualityOverlayLookup.getColorAt(factor).darker(), 0.5f),
                UTIL.setAlpha(qualityOverlayLookup.getColorAt(factor), 0.5f)
            };

            qualityOverlayLookup = new GradientWrapper(new Point2D.Double(lcdImage.getMinX() + overlayInsets.left, 0), new Point2D.Double(lcdImage.getMinX() + lcdImage.getWidth() - overlayInsets.right, 0), qualityOverlayFractions, qualityOverlayColors);
            qualityOverlayGradient = new LinearGradientPaint(new Point2D.Double(0, overlayInsets.top), new Point2D.Double(0, INNER_BOUNDS.height - overlayInsets.bottom), new float[]{0.0f, 0.5f, 1.0f}, overlayColors);
            qualityOverlay.setRoundRect(overlayInsets.left, overlayInsets.top, (INNER_BOUNDS.width * overlayFactor) - overlayInsets.left - overlayInsets.right, INNER_BOUNDS.height - overlayInsets.top - overlayInsets.bottom, overlayCornerRadius, overlayCornerRadius);
        }
        firePropertyChange(LCD_VALUE_PROPERTY, OLD_VALUE, LCD_VALUE);
        repaint(getInnerBounds());
    }

    public double getLcdMinValue() {
        return lcdMinValue;
    }

    public void setLcdMinValue(final double LCD_MIN_VALUE) {
        // check min-max values
        if (Double.compare(LCD_MIN_VALUE, lcdMaxValue) == 0) {
            throw new IllegalArgumentException("Min value cannot be equal to max value");
        }

        if (Double.compare(LCD_MIN_VALUE, lcdMaxValue) > 0) {
            lcdMinValue = lcdMaxValue;
            lcdMaxValue = LCD_MIN_VALUE;
        } else {
            lcdMinValue = LCD_MIN_VALUE;
        }

        init(getWidth(), getHeight());
        repaint(getInnerBounds());
    }

    public double getLcdMaxValue() {
        return lcdMaxValue;
    }

    public void setLcdMaxValue(final double LCD_MAX_VALUE) {
        // check min-max values
        if (Double.compare(LCD_MAX_VALUE, lcdMinValue) == 0) {
            throw new IllegalArgumentException("Max value cannot be equal to min value");
        }

        if (Double.compare(LCD_MAX_VALUE, lcdMinValue) < 0) {
            lcdMaxValue = lcdMinValue;
            lcdMinValue = LCD_MAX_VALUE;
        } else {
            lcdMaxValue = LCD_MAX_VALUE;
        }
        init(getWidth(), getHeight());
        repaint(getInnerBounds());
    }

    public boolean isLcdNumericValues() {
        return lcdNnumericValues;
    }

    public void setLcdNumericValues(final boolean LCD_NUMERIC_VALUES) {
        if (LCD_NUMERIC_VALUES) {
            TEXT_SCROLLER.stop();
        }
        lcdNnumericValues = LCD_NUMERIC_VALUES;
        repaint(getInnerBounds());
    }

    public String getLcdText() {
        return lcdText;
    }

    public void setLcdText(final String LCD_TEXT) {
        final String OLD_TEXT = lcdText;
        lcdText = LCD_TEXT;
        firePropertyChange(LCD_TEXT_PROPERTY, OLD_TEXT, LCD_TEXT);
        repaint(getInnerBounds());
    }

    public boolean isLcdTextScrolling() {
        return TEXT_SCROLLER.isRunning();
    }

    public void setLcdTextScrolling(final boolean ANIMATE) {
        if (!isLcdNumericValues()) {
            if (ANIMATE) {
                TEXT_SCROLLER.start();
            } else {
                TEXT_SCROLLER.stop();
            }
        }
    }

    @Override
    public void setLcdValueAnimated(final double LCD_VALUE) {
        if (isEnabled()) {
            if (timeline.getState() != Timeline.TimelineState.IDLE) {
                timeline.abort();
            }
            timeline = new Timeline(this);
            timeline.addPropertyToInterpolate("lcdValue", this.lcdValue, LCD_VALUE);
            timeline.setEase(EASING);
            timeline.setDuration((long) (2000));
            timeline.play();
        }
    }

    @Override
    public double getLcdThreshold() {
        return lcdThreshold;
    }

    @Override
    public void setLcdThreshold(final double LCD_THRESHOLD) {
        lcdThreshold = LCD_THRESHOLD;
        if (lcdThresholdVisible) {
            repaint(getInnerBounds());
        }
    }

    @Override
    public boolean isLcdThresholdVisible() {
        return lcdThresholdVisible;
    }

    @Override
    public void setLcdThresholdVisible(final boolean LCD_THRESHOLD_VISIBLE) {
        lcdThresholdVisible = LCD_THRESHOLD_VISIBLE;
        repaint(getInnerBounds());
    }

    @Override
    public boolean isLcdThresholdBehaviourInverted() {
        return lcdThresholdBehaviourInverted;
    }

    @Override
    public void setLcdThresholdBehaviourInverted(final boolean LCD_THRESHOLD_BEHAVIOUR_INVERTED) {
        lcdThresholdBehaviourInverted = LCD_THRESHOLD_BEHAVIOUR_INVERTED;
    }

    @Override
    public boolean isLcdBlinking() {
        return lcdBlinking;
    }

    @Override
    public void setLcdBlinking(final boolean LCD_BLINKING) {
        lcdBlinking = LCD_BLINKING;
        if (LCD_BLINKING) {
            LCD_BLINKING_TIMER.start();
        } else {
            LCD_BLINKING_TIMER.stop();
        }
        repaint(getInnerBounds());
    }

    @Override
    public int getLcdDecimals() {
        return this.lcdDecimals;
    }

    @Override
    public void setLcdDecimals(final int DECIMALS) {
        this.lcdDecimals = DECIMALS;
        repaint(getInnerBounds());
    }

    @Override
    public String getLcdUnitString() {
        return this.lcdUnitString;
    }

    @Override
    public void setLcdUnitString(final String LCD_UNIT_STRING) {
        this.lcdUnitString = LCD_UNIT_STRING;
        repaint(getInnerBounds());
    }

    @Override
    public boolean isLcdUnitStringVisible() {
        return this.lcdUnitStringVisible;
    }

    @Override
    public void setLcdUnitStringVisible(final boolean LCD_UNIT_STRING_VISIBLE) {
        this.lcdUnitStringVisible = LCD_UNIT_STRING_VISIBLE;
        repaint(getInnerBounds());
    }

    @Override
    public boolean isCustomLcdUnitFontEnabled() {
        return this.useCustomLcdUnitFont;
    }

    @Override
    public void setCustomLcdUnitFontEnabled(final boolean USE_CUSTOM_LCD_UNIT_FONT) {
        this.useCustomLcdUnitFont = USE_CUSTOM_LCD_UNIT_FONT;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    @Override
    public Font getCustomLcdUnitFont() {
        return this.customLcdUnitFont;
    }

    @Override
    public void setCustomLcdUnitFont(final Font CUSTOM_LCD_UNIT_FONT) {
        this.customLcdUnitFont = CUSTOM_LCD_UNIT_FONT;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    @Override
    public void setLcdScientificFormat(final boolean LCD_SCIENTIFIC_FORMAT) {
        this.lcdScientificFormat = LCD_SCIENTIFIC_FORMAT;
    }

    @Override
    public boolean isLcdScientificFormat() {
        return lcdScientificFormat;
    }

    @Override
    public boolean isDigitalFont() {
        return this.digitalFont;
    }

    @Override
    public void setDigitalFont(final boolean DIGITAL_FONT) {
        this.digitalFont = DIGITAL_FONT;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    @Override
    public LcdColor getLcdColor() {
        return this.lcdColor;
    }

    @Override
    public void setLcdColor(final LcdColor COLOR) {
        this.lcdColor = COLOR;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    @Override
    public Paint getCustomLcdBackground() {
        return this.customLcdBackground;
    }

    @Override
    public void setCustomLcdBackground(final Paint CUSTOM_LCD_BACKGROUND) {
        this.customLcdBackground = CUSTOM_LCD_BACKGROUND;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    @Override
    public Paint createCustomLcdBackgroundPaint(final Color[] LCD_COLORS) {
        final Point2D FOREGROUND_START = new Point2D.Double(0.0, 1.0);
        final Point2D FOREGROUND_STOP = new Point2D.Double(0.0, getHeight() - 1);
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
            LCD_COLORS[0],
            LCD_COLORS[1],
            LCD_COLORS[2],
            LCD_COLORS[3],
            LCD_COLORS[4]
        };

        return new LinearGradientPaint(FOREGROUND_START, FOREGROUND_STOP, FOREGROUND_FRACTIONS, FOREGROUND_COLORS);
    }

    @Override
    public boolean isLcdBackgroundVisible() {
        return lcdBackgroundVisible;
    }

    @Override
    public void setLcdBackgroundVisible(final boolean LCD_BACKGROUND_VISIBLE) {
        lcdBackgroundVisible = LCD_BACKGROUND_VISIBLE;
        repaint(getInnerBounds());
    }

    @Override
    public Color getCustomLcdForeground() {
        return this.customLcdForeground;
    }

    @Override
    public void setCustomLcdForeground(final Color CUSTOM_LCD_FOREGROUND) {
        this.customLcdForeground = CUSTOM_LCD_FOREGROUND;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    @Override
    public String formatLcdValue(final double VALUE) {
        final StringBuilder DEC_BUFFER = new StringBuilder(16);
        DEC_BUFFER.append("0");

        if (this.lcdDecimals > 0) {
            DEC_BUFFER.append(".");
        }

        for (int i = 0; i < this.lcdDecimals; i++) {
            DEC_BUFFER.append("0");
        }

        if (lcdScientificFormat) {
            DEC_BUFFER.append("E0");
        }

        final java.text.DecimalFormat DEC_FORMAT = new java.text.DecimalFormat(DEC_BUFFER.toString(), new java.text.DecimalFormatSymbols(java.util.Locale.US));

        return DEC_FORMAT.format(VALUE);
    }

    @Override
    public boolean isValueCoupled() {
        return false;
    }

    @Override
    public void setValueCoupled(boolean VALUE_COUPLED) {
    }

    @Override
    public Font getLcdValueFont() {
        return this.lcdValueFont;
    }

    @Override
    public void setLcdValueFont(Font LCD_VALUE_FONT) {
        this.lcdValueFont = LCD_VALUE_FONT;
        repaint(getInnerBounds());
    }

    @Override
    public Font getLcdUnitFont() {
        return this.lcdUnitFont;
    }

    @Override
    public void setLcdUnitFont(Font LCD_UNIT_FONT) {
        this.lcdUnitFont = LCD_UNIT_FONT;
        repaint(getInnerBounds());
    }

    @Override
    public String getLcdInfoString() {
        return lcdInfoString;
    }

    @Override
    public void setLcdInfoString(final String LCD_INFO_STRING) {
        lcdInfoString = LCD_INFO_STRING;
        repaint(getInnerBounds());
    }

    @Override
    public Font getLcdInfoFont() {
        return lcdInfoFont;
    }

    @Override
    public void setLcdInfoFont(final Font LCD_INFO_FONT) {
        lcdInfoFont = LCD_INFO_FONT;
        repaint(getInnerBounds());
    }

    @Override
    public NumberSystem getLcdNumberSystem() {
        return numberSystem;
    }

    @Override
    public void setLcdNumberSystem(final NumberSystem NUMBER_SYSTEM) {
        numberSystem = NUMBER_SYSTEM;
        switch (NUMBER_SYSTEM) {
            case HEX:
                lcdInfoString = "hex";
                break;
            case OCT:
                lcdInfoString = "oct";
                break;
            case DEC:

            default:
                lcdInfoString = "";
                break;
        }
        repaint(getInnerBounds());
    }

    @Override
    public Rectangle getLcdBounds() {
        return getInnerBounds();
    }

    public boolean isQualityOverlayVisible() {
        return qualityOverlayVisible;
    }

    public void setQualityOverlayVisible(final boolean QUALITIY_OVERLAY_VISIBLE) {
        qualityOverlayVisible = QUALITIY_OVERLAY_VISIBLE;
        repaint(getInnerBounds());
    }

    public float[] getQualityOverlayFractions() {
        return qualityOverlayFractions.clone();
    }

    public Color[] getQualityOverlayColors() {
        return qualityOverlayColors.clone();
    }

    public void setQualityOverlayFractionsAndColors(final float[] QUALITY_OVERLAY_FRACTIONS, final Color[] QUALITY_OVERLAY_COLORS) {
        if (QUALITY_OVERLAY_FRACTIONS.length != QUALITY_OVERLAY_COLORS.length) {
            return;
        }
        qualityOverlayFractions = QUALITY_OVERLAY_FRACTIONS.clone();
        qualityOverlayColors = QUALITY_OVERLAY_COLORS.clone();
        init(getWidth(), getHeight());
        repaint(getInnerBounds());
    }

    /**
    * Returns true if the glow indicator is visible
    * @return true if the glow indicator is visible
    */
    public boolean isGlowVisible() {
        return glowVisible;
    }

    /**
    * Enables / disables the glow indicator
    * @param GLOW_VISIBLE
    */
    public void setGlowVisible(final boolean GLOW_VISIBLE) {
        glowVisible = GLOW_VISIBLE;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint();
    }

    /**
    * Returns the color that will be used for the glow indicator
    * @return the color that will be used for the glow indicator
    */
    public  Color getGlowColor() {
        return glowColor;
    }

    /**
    * Sets the color that will be used for the glow indicator
    * @param GLOW_COLOR
    */
    public void setGlowColor(final Color GLOW_COLOR) {
        glowColor = GLOW_COLOR;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint();
    }

    /**
    * Returns true if the glow indicator is glowing
    * @return true if the glow indicator is glowing
    */
    public boolean isGlowing() {
        return glowing;
    }

    /**
    * Enables / disables the glowing of the glow indicator
    * @param GLOWING
    */
    public void setGlowing(final boolean GLOWING) {
        glowing = GLOWING;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint();
    }

    public boolean isBargraphVisible() {
        return bargraphVisible;
    }

    public void setBargraphVisible(final boolean BARGRAPH_VISIBLE) {
        bargraphVisible = BARGRAPH_VISIBLE;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint();
    }

    public boolean isPlainBargraphSegments() {
        return plainBargraphSegments;
    }

    public void setPlainBargraphSegments(final boolean PLAIN_BARGRAPH_SEGMENTS) {
        plainBargraphSegments = PLAIN_BARGRAPH_SEGMENTS;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Sections related">
    /**
     * Returns the visibility of the sections.
     * The sections could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class Section.
     * The sections are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you could not visualize with the
     * track feature.
     * @return true if the sections are visible
     */
    public boolean isSectionsVisible() {
        return sectionsVisible;
    }

    /**
     * Sets the visibility of the sections.
     * The sections could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class Section.
     * The sections are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you could not visualize with the
     * track feature.
     * @param SECTIONS_VISIBLE
     */
    public void setSectionsVisible(final boolean SECTIONS_VISIBLE) {
        sectionsVisible = SECTIONS_VISIBLE;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns a copy of the ArrayList that stores the sections.
     * The sections could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class Section.
     * The sections are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you could not visualize with the
     * track feature.
     * @return a list of sections
     */
    public List<Section> getSections() {
        List<Section> sectionsCopy = new ArrayList<Section>(sections.size());
        sectionsCopy.addAll(sections);
        return sectionsCopy;
    }

    /**
     * Sets the sections given in a array of sections (Section[])
     * The sections could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class Section.
     * The sections are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you could not visualize with the
     * track feature.
     * @param SECTIONS_ARRAY
     */
    public void setSections(final Section... SECTIONS_ARRAY) {
        sections.clear();
        for (Section section : SECTIONS_ARRAY) {
            sections.add(new Section(section.getStart(), section.getStop(), section.getColor()));
        }
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Adds a given section to the list of sections
     * The sections could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class Section.
     * The sections are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you could not visualize with the
     * track feature.
     * @param SECTION
     */
    public void addSection(final Section SECTION) {
        sections.add(SECTION);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Clear the SECTIONS arraylist
     */
    public void resetSections() {
        sections.clear();
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image related">
    private BufferedImage create_LCD_Image(final int WIDTH, final int HEIGHT, Color[] lcdMainColors) {
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        // Background rectangle
        final Point2D LCD_FRAME_START = new Point2D.Double(0.0, 0.0);
        final Point2D LCD_FRAME_STOP = new Point2D.Double(0.0, IMAGE_HEIGHT);
        if (LCD_FRAME_START.equals(LCD_FRAME_STOP)) {
            LCD_FRAME_STOP.setLocation(0.0, LCD_FRAME_START.getY() + 1);
        }

        final float[] LCD_FRAME_FRACTIONS = {
            0.0f,
            0.08f,
            0.92f,
            1.0f
        };

        final Color[] LCD_FRAME_COLORS = {
            new Color(0.4f, 0.4f, 0.4f, 1.0f),
            new Color(0.5f, 0.5f, 0.5f, 1.0f),
            new Color(0.5f, 0.5f, 0.5f, 1.0f),
            new Color(0.9f, 0.9f, 0.9f, 1.0f)
        };

        final LinearGradientPaint LCD_FRAME_GRADIENT = new LinearGradientPaint(LCD_FRAME_START, LCD_FRAME_STOP, LCD_FRAME_FRACTIONS, LCD_FRAME_COLORS);
        //final double BACKGROUND_CORNER_RADIUS = WIDTH * 0.09375;
        final double LCD_FRAME_CORNER_RADIUS = WIDTH > HEIGHT ? (HEIGHT * 0.095) : (WIDTH * 0.095);
        final RoundRectangle2D LCD_FRAME = new RoundRectangle2D.Double(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, LCD_FRAME_CORNER_RADIUS, LCD_FRAME_CORNER_RADIUS);
        G2.setPaint(LCD_FRAME_GRADIENT);
        G2.fill(LCD_FRAME);

        // Foreground rectangle
        final Point2D LCD_MAIN_START = new Point2D.Double(0.0, 1.0);
        final Point2D LCD_MAIN_STOP = new Point2D.Double(0.0, IMAGE_HEIGHT - 1);
        if (LCD_MAIN_START.equals(LCD_MAIN_STOP)) {
            LCD_MAIN_STOP.setLocation(0.0, LCD_MAIN_START.getY() + 1);
        }

        final float[] LCD_MAIN_FRACTIONS = {
            0.0f,
            0.03f,
            0.49f,
            0.5f,
            1.0f
        };

        if (lcdMainColors == null) {
            lcdMainColors = new Color[]{
                lcdColor.GRADIENT_START_COLOR,
                lcdColor.GRADIENT_FRACTION1_COLOR,
                lcdColor.GRADIENT_FRACTION2_COLOR,
                lcdColor.GRADIENT_FRACTION3_COLOR,
                lcdColor.GRADIENT_STOP_COLOR
            };
        }
        if (lcdColor == LcdColor.CUSTOM) {
            G2.setPaint(customLcdBackground);
        } else {
            final LinearGradientPaint LCD_MAIN_GRADIENT = new LinearGradientPaint(LCD_MAIN_START, LCD_MAIN_STOP, LCD_MAIN_FRACTIONS, lcdMainColors);
            G2.setPaint(LCD_MAIN_GRADIENT);
        }
        final double LCD_MAIN_CORNER_RADIUS = LCD_FRAME.getArcWidth() - 1;
        final RoundRectangle2D LCD_MAIN = new RoundRectangle2D.Double(1, 1, IMAGE_WIDTH - 2, IMAGE_HEIGHT - 2, LCD_MAIN_CORNER_RADIUS, LCD_MAIN_CORNER_RADIUS);
        G2.fill(LCD_MAIN);

        if (bargraphVisible) {
            final GeneralPath BARGRAPH_OFF = new GeneralPath();
            BARGRAPH_OFF.setWindingRule(Path2D.WIND_EVEN_ODD);
            BARGRAPH_OFF.moveTo(0.859375 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.859375 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.8828125 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.8828125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.859375 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.closePath();
            BARGRAPH_OFF.moveTo(0.8203125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.8203125 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.84375 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.84375 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.8203125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.closePath();
            BARGRAPH_OFF.moveTo(0.78125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.78125 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.8046875 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.8046875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.78125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.closePath();
            BARGRAPH_OFF.moveTo(0.7421875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.7421875 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.765625 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.765625 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.7421875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.closePath();
            BARGRAPH_OFF.moveTo(0.703125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.703125 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.7265625 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.7265625 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.703125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.closePath();
            BARGRAPH_OFF.moveTo(0.6640625 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.6640625 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.6875 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.6875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.6640625 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.closePath();
            BARGRAPH_OFF.moveTo(0.625 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.625 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.6484375 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.6484375 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.625 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.closePath();
            BARGRAPH_OFF.moveTo(0.5859375 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.5859375 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.609375 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.609375 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.5859375 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.closePath();
            BARGRAPH_OFF.moveTo(0.546875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.546875 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.5703125 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.5703125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.546875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.closePath();
            BARGRAPH_OFF.moveTo(0.5078125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.5078125 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.53125 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.53125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.5078125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.closePath();
            BARGRAPH_OFF.moveTo(0.46875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.46875 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.4921875 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.4921875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.46875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.closePath();
            BARGRAPH_OFF.moveTo(0.4296875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.4296875 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.453125 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.453125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.4296875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.closePath();
            BARGRAPH_OFF.moveTo(0.390625 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.390625 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.4140625 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.4140625 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.390625 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.closePath();
            BARGRAPH_OFF.moveTo(0.3515625 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.3515625 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.375 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.375 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.3515625 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.closePath();
            BARGRAPH_OFF.moveTo(0.3125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.3125 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.3359375 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.3359375 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.3125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.closePath();
            BARGRAPH_OFF.moveTo(0.2734375 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.2734375 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.296875 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.296875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.2734375 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.closePath();
            BARGRAPH_OFF.moveTo(0.234375 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.234375 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.2578125 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.2578125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.234375 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.closePath();
            BARGRAPH_OFF.moveTo(0.1953125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.1953125 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.21875 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.21875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.1953125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.closePath();
            BARGRAPH_OFF.moveTo(0.15625 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.15625 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.1796875 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.1796875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.15625 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.closePath();
            BARGRAPH_OFF.moveTo(0.1171875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.1171875 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.140625 * IMAGE_WIDTH, 0.9375 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.140625 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.lineTo(0.1171875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT);
            BARGRAPH_OFF.closePath();
            G2.setPaint(new Color(lcdColor.TEXT_COLOR.getRed(), lcdColor.TEXT_COLOR.getGreen(), lcdColor.TEXT_COLOR.getBlue(), 25));
            G2.fill(BARGRAPH_OFF);
        }

        G2.dispose();

        return IMAGE;
    }

    private BufferedImage create_LCD_THRESHOLD_Image(final int WIDTH, final int HEIGHT, final Color COLOR) {
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final GeneralPath LCD_THRESHOLD = new GeneralPath();
        LCD_THRESHOLD.setWindingRule(Path2D.WIND_EVEN_ODD);
        LCD_THRESHOLD.moveTo(IMAGE_WIDTH * 0.4444444444444444, IMAGE_HEIGHT * 0.7777777777777778);
        LCD_THRESHOLD.lineTo(IMAGE_WIDTH * 0.5555555555555556, IMAGE_HEIGHT * 0.7777777777777778);
        LCD_THRESHOLD.lineTo(IMAGE_WIDTH * 0.5555555555555556, IMAGE_HEIGHT * 0.8888888888888888);
        LCD_THRESHOLD.lineTo(IMAGE_WIDTH * 0.4444444444444444, IMAGE_HEIGHT * 0.8888888888888888);
        LCD_THRESHOLD.lineTo(IMAGE_WIDTH * 0.4444444444444444, IMAGE_HEIGHT * 0.7777777777777778);
        LCD_THRESHOLD.closePath();
        LCD_THRESHOLD.moveTo(IMAGE_WIDTH * 0.4444444444444444, IMAGE_HEIGHT * 0.3333333333333333);
        LCD_THRESHOLD.lineTo(IMAGE_WIDTH * 0.5555555555555556, IMAGE_HEIGHT * 0.3333333333333333);
        LCD_THRESHOLD.lineTo(IMAGE_WIDTH * 0.5555555555555556, IMAGE_HEIGHT * 0.7222222222222222);
        LCD_THRESHOLD.lineTo(IMAGE_WIDTH * 0.4444444444444444, IMAGE_HEIGHT * 0.7222222222222222);
        LCD_THRESHOLD.lineTo(IMAGE_WIDTH * 0.4444444444444444, IMAGE_HEIGHT * 0.3333333333333333);
        LCD_THRESHOLD.closePath();
        LCD_THRESHOLD.moveTo(0.0, IMAGE_HEIGHT);
        LCD_THRESHOLD.lineTo(IMAGE_WIDTH, IMAGE_HEIGHT);
        LCD_THRESHOLD.lineTo(IMAGE_WIDTH * 0.5, 0.0);
        LCD_THRESHOLD.lineTo(0.0, IMAGE_HEIGHT);
        LCD_THRESHOLD.closePath();
        G2.setColor(COLOR);
        G2.fill(LCD_THRESHOLD);

        G2.dispose();

        return IMAGE;
    }

    private void prepareBargraph(final int WIDTH, final int HEIGHT) {
        final int IMAGE_WIDTH = WIDTH;
        final int IMAGE_HEIGHT = HEIGHT;
        final Rectangle2D SEG1 = new Rectangle2D.Double(0.1171875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT,
                                                        0.0234375 * IMAGE_WIDTH, 0.125 * IMAGE_HEIGHT);

        final Rectangle2D SEG2 = new Rectangle2D.Double(0.15625 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT,
                                                        0.0234375 * IMAGE_WIDTH, 0.125 * IMAGE_HEIGHT);

        final Rectangle2D SEG3 = new Rectangle2D.Double(0.1953125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT,
                                                        0.0234375 * IMAGE_WIDTH, 0.125 * IMAGE_HEIGHT);

        final Rectangle2D SEG4 = new Rectangle2D.Double(0.234375 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT,
                                                        0.0234375 * IMAGE_WIDTH, 0.125 * IMAGE_HEIGHT);

        final Rectangle2D SEG5 = new Rectangle2D.Double(0.2734375 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT,
                                                        0.0234375 * IMAGE_WIDTH, 0.125 * IMAGE_HEIGHT);

        final Rectangle2D SEG6 = new Rectangle2D.Double(0.3125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT,
                                                        0.0234375 * IMAGE_WIDTH, 0.125 * IMAGE_HEIGHT);

        final Rectangle2D SEG7 = new Rectangle2D.Double(0.3515625 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT,
                                                        0.0234375 * IMAGE_WIDTH, 0.125 * IMAGE_HEIGHT);

        final Rectangle2D SEG8 = new Rectangle2D.Double(0.390625 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT,
                                                        0.0234375 * IMAGE_WIDTH, 0.125 * IMAGE_HEIGHT);

        final Rectangle2D SEG9 = new Rectangle2D.Double(0.4296875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT,
                                                        0.0234375 * IMAGE_WIDTH, 0.125 * IMAGE_HEIGHT);

        final Rectangle2D SEG10 = new Rectangle2D.Double(0.46875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT,
                                                         0.0234375 * IMAGE_WIDTH, 0.125 * IMAGE_HEIGHT);

        final Rectangle2D SEG11 = new Rectangle2D.Double(0.5078125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT,
                                                         0.0234375 * IMAGE_WIDTH, 0.125 * IMAGE_HEIGHT);

        final Rectangle2D SEG12 = new Rectangle2D.Double(0.546875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT,
                                                         0.0234375 * IMAGE_WIDTH, 0.125 * IMAGE_HEIGHT);

        final Rectangle2D SEG13 = new Rectangle2D.Double(0.5859375 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT,
                                                         0.0234375 * IMAGE_WIDTH, 0.125 * IMAGE_HEIGHT);

        final Rectangle2D SEG14 = new Rectangle2D.Double(0.625 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT,
                                                         0.0234375 * IMAGE_WIDTH, 0.125 * IMAGE_HEIGHT);

        final Rectangle2D SEG15 = new Rectangle2D.Double(0.6640625 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT,
                                                         0.0234375 * IMAGE_WIDTH, 0.125 * IMAGE_HEIGHT);

        final Rectangle2D SEG16 = new Rectangle2D.Double(0.703125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT,
                                                         0.0234375 * IMAGE_WIDTH, 0.125 * IMAGE_HEIGHT);

        final Rectangle2D SEG17 = new Rectangle2D.Double(0.7421875 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT,
                                                         0.0234375 * IMAGE_WIDTH, 0.125 * IMAGE_HEIGHT);

        final Rectangle2D SEG18 = new Rectangle2D.Double(0.78125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT,
                                                         0.0234375 * IMAGE_WIDTH, 0.125 * IMAGE_HEIGHT);

        final Rectangle2D SEG19 = new Rectangle2D.Double(0.8203125 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT,
                                                         0.0234375 * IMAGE_WIDTH, 0.125 * IMAGE_HEIGHT);

        final Rectangle2D SEG20 = new Rectangle2D.Double(0.859375 * IMAGE_WIDTH, 0.8125 * IMAGE_HEIGHT,
                                                         0.0234375 * IMAGE_WIDTH, 0.125 * IMAGE_HEIGHT);

        bargraph.clear();
        bargraph.add(SEG1);
        bargraph.add(SEG2);
        bargraph.add(SEG3);
        bargraph.add(SEG4);
        bargraph.add(SEG5);
        bargraph.add(SEG6);
        bargraph.add(SEG7);
        bargraph.add(SEG8);
        bargraph.add(SEG9);
        bargraph.add(SEG10);
        bargraph.add(SEG11);
        bargraph.add(SEG12);
        bargraph.add(SEG13);
        bargraph.add(SEG14);
        bargraph.add(SEG15);
        bargraph.add(SEG16);
        bargraph.add(SEG17);
        bargraph.add(SEG18);
        bargraph.add(SEG19);
        bargraph.add(SEG20);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Size related">
    /**
     * Calculates the area that is available for painting the display
     */
    private void calcInnerBounds() {
        final java.awt.Insets INSETS = getInsets();
        INNER_BOUNDS.setBounds(INSETS.left, INSETS.top, getWidth() - INSETS.left - INSETS.right, getHeight() - INSETS.top - INSETS.bottom);
    }

    /**
     * Returns a rectangle2d representing the available space for drawing the
     * component taking the insets into account (e.g. given through borders etc.)
     * @return rectangle2d that represents the area available for rendering the component
     */
    private Rectangle getInnerBounds() {
        return INNER_BOUNDS;
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension dim = super.getMinimumSize();
        if (dim.width < 64 || dim.height < 24) {
            dim = new Dimension(64, 24);
        }
        return dim;
    }

    @Override
    public void setMinimumSize(final Dimension DIM) {
        int width = DIM.width < 64 ? 64 : DIM.width;
        int height = DIM.height < 24 ? 24 : DIM.height;
        super.setMinimumSize(new Dimension(width, height));
        calcInnerBounds();
        init(getInnerBounds().width, getInnerBounds().height);
        invalidate();
        repaint();
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension dim = super.getMaximumSize();
        if (dim.width > 1920 || dim.height > 720) {
            dim = new Dimension(1920, 720);
        }
        return dim;
    }

    @Override
    public void setMaximumSize(final Dimension DIM) {
        int width = DIM.width > 1920 ? 1920 : DIM.width;
        int height = DIM.height > 720 ? 720 : DIM.height;
        super.setMaximumSize(new Dimension(width, height));
        calcInnerBounds();
        init(getInnerBounds().width, getInnerBounds().height);
        invalidate();
        repaint();
    }

    @Override
    public void setPreferredSize(final Dimension DIM) {
        super.setPreferredSize(DIM);
        calcInnerBounds();
        init(getInnerBounds().width, getInnerBounds().height);
        invalidate();
        repaint();
    }

    @Override
    public void setSize(final int WIDTH, final int HEIGHT) {
        super.setSize(WIDTH, HEIGHT);
        calcInnerBounds();
        init(getInnerBounds().width, getInnerBounds().height);
    }

    @Override
    public void setSize(final Dimension DIM) {
        super.setSize(DIM);
        calcInnerBounds();
        init(getInnerBounds().width, getInnerBounds().height);
    }

    @Override
    public void setBounds(final Rectangle BOUNDS) {
        super.setBounds(BOUNDS);
        calcInnerBounds();
        init(getInnerBounds().width, getInnerBounds().height);
    }

    @Override
    public void setBounds(final int X, final int Y, final int WIDTH, final int HEIGHT) {
        super.setBounds(X, Y, WIDTH, HEIGHT);
        calcInnerBounds();
        init(getInnerBounds().width, getInnerBounds().height);
    }

    @Override
    public void setBorder(Border BORDER) {
        super.setBorder(BORDER);
        calcInnerBounds();
        init(getInnerBounds().width, getInnerBounds().height);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ActionListener">
    @Override
    public void actionPerformed(final ActionEvent EVENT) {
        if (lcdTextX > (lcdImage.getWidth() + VALUE_BOUNDARY.getWidth())) {
            lcdTextX = -VALUE_BOUNDARY.getBounds().width;
        }
        lcdTextX += 1;
        if (EVENT.getSource().equals(LCD_BLINKING_TIMER)) {
            lcdTextVisible ^= true;
        }
        repaint(getInnerBounds());
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "DisplaySingle";
    }
}
