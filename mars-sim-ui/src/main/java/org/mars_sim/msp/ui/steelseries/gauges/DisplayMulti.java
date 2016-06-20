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
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.border.Border;

import org.mars_sim.msp.ui.steelseries.tools.GlowImageFactory;
import org.mars_sim.msp.ui.steelseries.tools.LcdColor;
import org.mars_sim.msp.ui.steelseries.tools.NumberSystem;
import org.mars_sim.msp.ui.steelseries.tools.Util;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.ease.Linear;
import org.pushingpixels.trident.ease.TimelineEase;


/**
 *
 * @author hansolo
 */
public final class DisplayMulti extends JComponent implements Lcd, ActionListener {
    // <editor-fold defaultstate="collapsed" desc="Variable declaration">

    private final Util UTIL = Util.INSTANCE;
    private LcdColor lcdColor = LcdColor.WHITE_LCD;
    private Paint customLcdBackground = Color.BLACK;
    private Color customLcdForeground = Color.WHITE;
    private final Rectangle INNER_BOUNDS = new Rectangle(0, 0, 128, 64);
    private double lcdValue;
    private double oldValue;
    private static final String LCD_VALUE_PROPERTY = "lcdValue";
    private double lcdThreshold;
    private boolean lcdThresholdVisible;
    private boolean lcdThresholdBehaviourInverted;
    private boolean lcdBackgroundVisible;
    private boolean lcdTextVisible;
    private boolean lcdBlinking;
    private final Timer LCD_BLINKING_TIMER;
    private BufferedImage lcdThresholdImage;
    private int lcdDecimals;
    private String lcdUnitString;
    private boolean lcdUnitStringVisible;
    private boolean lcdScientificFormat;
    private boolean digitalFont;
    private boolean useCustomLcdUnitFont;
    private Font customLcdUnitFont;
    private Font lcdValueFont;
    private Font lcdFormerValueFont;
    private Font lcdUnitFont;
    private Font lcdInfoFont;
    private String lcdInfoString;
    private final Font LCD_STANDARD_FONT;
    private final Font LCD_DIGITAL_FONT;
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
    private TextLayout oldValueLayout;
    private final Rectangle2D OLD_VALUE_BOUNDARY = new Rectangle2D.Double();
    private TextLayout infoLayout;
    private final Rectangle2D INFO_BOUNDARY = new Rectangle2D.Double();
    private boolean glowVisible;
    private Color glowColor;
    private boolean glowing;
    private BufferedImage glowImageOn;
    private final transient ComponentListener COMPONENT_LISTENER = new ComponentAdapter() {

        @Override
        public void componentResized(java.awt.event.ComponentEvent event) {
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
                setPreferredSize(new java.awt.Dimension(getWidth(), getHeight()));
            }

            calcInnerBounds();
            init(getInnerBounds().width, getInnerBounds().height);
            //revalidate();
            //repaint();
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public DisplayMulti() {
        super();
        lcdValue = 0.0;
        oldValue = 0.0;
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
        customLcdUnitFont = new Font("Verdana", 0, 24);
        LCD_STANDARD_FONT = new Font("Verdana", 0, 30);
        LCD_DIGITAL_FONT = Util.INSTANCE.getDigitalFont().deriveFont(24).deriveFont(Font.PLAIN);
        lcdInfoFont = new Font("Verdana", 0, 24);
        lcdInfoString = "";
        numberSystem = NumberSystem.DEC;
        DISABLED_COLOR = new Color(102, 102, 102, 178);
        timeline = new Timeline(this);
        EASING = new Linear();
        glowVisible = false;
        glowColor = new Color(51, 255, 255);
        glowing = false;
        init(128, 64);
        addComponentListener(COMPONENT_LISTENER);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    public final void init(final int WIDTH, final int HEIGHT) {
        if (WIDTH <= 1 || HEIGHT <= 1) {
            return;
        }

        if (lcdImage != null) {
            lcdImage.flush();
        }
        lcdImage = create_LCD_Image(WIDTH, HEIGHT);

        if (glowImageOn != null) {
            glowImageOn.flush();
        }
        glowImageOn = GlowImageFactory.INSTANCE.createLcdGlow(WIDTH, HEIGHT, glowColor, true);

        final double CORNER_RADIUS = WIDTH > HEIGHT ? (HEIGHT * 0.095) : (WIDTH * 0.095);
        disabledShape = new RoundRectangle2D.Double(0, 0, WIDTH, HEIGHT, CORNER_RADIUS, CORNER_RADIUS);
        if (isDigitalFont()) {
            lcdValueFont = LCD_DIGITAL_FONT.deriveFont(0.5f * getInnerBounds().height);
            lcdFormerValueFont = LCD_DIGITAL_FONT.deriveFont(0.2f * getInnerBounds().height);
            if (useCustomLcdUnitFont) {
                lcdUnitFont = customLcdUnitFont.deriveFont(0.1875f * getInnerBounds().height);
            } else {
                lcdUnitFont = LCD_STANDARD_FONT.deriveFont(0.1875f * getInnerBounds().height);
            }
        } else {
            lcdValueFont = LCD_STANDARD_FONT.deriveFont(0.46875f * getInnerBounds().height);
            lcdFormerValueFont = LCD_STANDARD_FONT.deriveFont(0.1875f * getInnerBounds().height);
            if (useCustomLcdUnitFont) {
                lcdUnitFont = customLcdUnitFont.deriveFont(0.1875f * getInnerBounds().height);
            } else {
                lcdUnitFont = LCD_STANDARD_FONT.deriveFont(0.1875f * getInnerBounds().height);
            }
        }
        lcdInfoFont = LCD_STANDARD_FONT.deriveFont(0.15f * getInnerBounds().height);

        if (lcdThresholdImage != null) {
            lcdThresholdImage.flush();
        }
        lcdThresholdImage = create_LCD_THRESHOLD_Image((int) (HEIGHT * 0.2045454545), (int) (HEIGHT * 0.2045454545), lcdColor.TEXT_COLOR);
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

        if (lcdBackgroundVisible) {
        G2.drawImage(lcdImage, 0, 0, null);
        }

        // Draw lcd text
        if (lcdColor == LcdColor.CUSTOM) {
            G2.setColor(customLcdForeground);
        } else {
            G2.setColor(lcdColor.TEXT_COLOR);
        }
        G2.setFont(lcdUnitFont);
        final double UNIT_STRING_WIDTH;
        if (lcdUnitStringVisible && !lcdUnitString.isEmpty()) {
            unitLayout = new TextLayout(lcdUnitString, G2.getFont(), RENDER_CONTEXT);
            UNIT_BOUNDARY.setFrame(unitLayout.getBounds());
            if (lcdTextVisible) {
            G2.drawString(lcdUnitString, (float) ((lcdImage.getWidth() - UNIT_BOUNDARY.getWidth()) - lcdImage.getHeight() * 0.15f), (lcdImage.getHeight() * 0.6f));
            }
            UNIT_STRING_WIDTH = UNIT_BOUNDARY.getWidth();
        } else {
            UNIT_STRING_WIDTH = 0;
        }

        // Draw value and oldValue
        switch (numberSystem) {
            case DEC:

            default:
                G2.setFont(lcdValueFont);
                valueLayout = new TextLayout(formatLcdValue(lcdValue), G2.getFont(), RENDER_CONTEXT);
                VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                if (lcdTextVisible) {
                G2.drawString(formatLcdValue(lcdValue), (float)((lcdImage.getMinX() + (lcdImage.getWidth() - UNIT_STRING_WIDTH - VALUE_BOUNDARY.getWidth()) - lcdImage.getHeight() * 0.3)), (lcdImage.getHeight() * 0.6f));
                }

                G2.setFont(lcdFormerValueFont);
                oldValueLayout = new TextLayout(formatLcdValue(oldValue), G2.getFont(), RENDER_CONTEXT);
                OLD_VALUE_BOUNDARY.setFrame(oldValueLayout.getBounds());
                if (lcdTextVisible) {
                G2.drawString(formatLcdValue(oldValue), (float) ((lcdImage.getWidth() - OLD_VALUE_BOUNDARY.getWidth()) / 2f), (lcdImage.getHeight() * 0.9f));
                }
                break;

            case HEX:
                G2.setFont(lcdValueFont);
                valueLayout = new TextLayout(Integer.toHexString((int) lcdValue).toUpperCase(), G2.getFont(), RENDER_CONTEXT);
                VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                if (lcdTextVisible) {
                G2.drawString(Integer.toHexString((int) lcdValue).toUpperCase(), (float)((lcdImage.getMinX() + (lcdImage.getWidth() - UNIT_STRING_WIDTH - VALUE_BOUNDARY.getWidth()) - lcdImage.getHeight() * 0.3)), (lcdImage.getHeight() * 0.6f));
                }

                G2.setFont(lcdFormerValueFont);
                oldValueLayout = new TextLayout(Integer.toHexString((int) oldValue).toUpperCase(), G2.getFont(), RENDER_CONTEXT);
                OLD_VALUE_BOUNDARY.setFrame(oldValueLayout.getBounds());
                if (lcdTextVisible) {
                G2.drawString(Integer.toHexString((int) oldValue).toUpperCase(), (float) ((lcdImage.getWidth() - OLD_VALUE_BOUNDARY.getWidth()) / 2f), (lcdImage.getHeight() * 0.9f));
                }
                break;

            case OCT:
                G2.setFont(lcdValueFont);
                valueLayout = new TextLayout(Integer.toOctalString((int) lcdValue), G2.getFont(), RENDER_CONTEXT);
                VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                if (lcdTextVisible) {
                G2.drawString(Integer.toOctalString((int) lcdValue), (float)((lcdImage.getMinX() + (lcdImage.getWidth() - UNIT_STRING_WIDTH - VALUE_BOUNDARY.getWidth()) - lcdImage.getHeight() * 0.3)), (lcdImage.getHeight() * 0.6f));
                }

                G2.setFont(lcdFormerValueFont);
                oldValueLayout = new TextLayout(Integer.toOctalString((int) oldValue), G2.getFont(), RENDER_CONTEXT);
                OLD_VALUE_BOUNDARY.setFrame(oldValueLayout.getBounds());
                if (lcdTextVisible) {
                G2.drawString(Integer.toOctalString((int) oldValue), (float) ((lcdImage.getWidth() - OLD_VALUE_BOUNDARY.getWidth()) / 2f), (lcdImage.getHeight() * 0.9f));
                }
                break;
        }

        // Draw lcd info string
        if (!lcdInfoString.isEmpty()) {
            G2.setFont(lcdInfoFont);
            infoLayout = new TextLayout(lcdInfoString, G2.getFont(), RENDER_CONTEXT);
            INFO_BOUNDARY.setFrame(infoLayout.getBounds());
            G2.drawString(lcdInfoString, 5f, (float) INFO_BOUNDARY.getHeight() + 5f);
        }

        // Draw lcd threshold indicator
        if (numberSystem == NumberSystem.DEC && lcdThresholdVisible && lcdValue >= lcdThreshold) {
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

        G2.translate(-getInnerBounds().x, -getInnerBounds().y);

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
    public void setLcdValue(final double VALUE) {
        oldValue = this.lcdValue;
        this.lcdValue = VALUE;
        if (numberSystem != NumberSystem.DEC) {
            if (lcdValue < 0) {
                setLcdNumberSystem(NumberSystem.DEC);
            }
        }
        firePropertyChange(LCD_VALUE_PROPERTY, oldValue, VALUE);
        repaint(getInnerBounds());
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
        repaint(getInnerBounds());
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
    public Color getCustomLcdForeground() {
        return this.customLcdForeground;
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
        DEC_BUFFER.trimToSize();

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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image related">
    private BufferedImage create_LCD_Image(final int WIDTH, final int HEIGHT) {
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
        final Point2D BACKGROUND_START = new Point2D.Double(0.0, 0.0);
        final Point2D BACKGROUND_STOP = new Point2D.Double(0.0, IMAGE_HEIGHT);
        if (BACKGROUND_START.equals(BACKGROUND_STOP)) {
            BACKGROUND_STOP.setLocation(0.0, BACKGROUND_START.getY() + 1);
        }

        final float[] BACKGROUND_FRACTIONS = {
            0.0f,
            0.08f,
            0.92f,
            1.0f
        };

        final Color[] BACKGROUND_COLORS = {
            new Color(0.4f, 0.4f, 0.4f, 1.0f),
            new Color(0.5f, 0.5f, 0.5f, 1.0f),
            new Color(0.5f, 0.5f, 0.5f, 1.0f),
            new Color(0.9f, 0.9f, 0.9f, 1.0f)
        };

        final LinearGradientPaint BACKGROUND_GRADIENT = new LinearGradientPaint(BACKGROUND_START, BACKGROUND_STOP, BACKGROUND_FRACTIONS, BACKGROUND_COLORS);
        //final double BACKGROUND_CORNER_RADIUS = WIDTH * 0.09375;
        final double BACKGROUND_CORNER_RADIUS = WIDTH > HEIGHT ? (HEIGHT * 0.095) : (WIDTH * 0.095);
        final java.awt.geom.RoundRectangle2D BACKGROUND = new java.awt.geom.RoundRectangle2D.Double(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, BACKGROUND_CORNER_RADIUS, BACKGROUND_CORNER_RADIUS);
        G2.setPaint(BACKGROUND_GRADIENT);
        G2.fill(BACKGROUND);

        // Foreground rectangle
        final Point2D FOREGROUND_START = new Point2D.Double(0.0, 1.0);
        final Point2D FOREGROUND_STOP = new Point2D.Double(0.0, IMAGE_HEIGHT - 1);
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
            lcdColor.GRADIENT_START_COLOR,
            lcdColor.GRADIENT_FRACTION1_COLOR,
            lcdColor.GRADIENT_FRACTION2_COLOR,
            lcdColor.GRADIENT_FRACTION3_COLOR,
            lcdColor.GRADIENT_STOP_COLOR
        };

        if (lcdColor == LcdColor.CUSTOM) {
            G2.setPaint(customLcdBackground);
        } else {
            final LinearGradientPaint FOREGROUND_GRADIENT = new LinearGradientPaint(FOREGROUND_START, FOREGROUND_STOP, FOREGROUND_FRACTIONS, FOREGROUND_COLORS);
            G2.setPaint(FOREGROUND_GRADIENT);
        }
        final double FOREGROUND_CORNER_RADIUS = BACKGROUND.getArcWidth() - 1;
        final java.awt.geom.RoundRectangle2D FOREGROUND = new java.awt.geom.RoundRectangle2D.Double(1, 1, IMAGE_WIDTH - 2, IMAGE_HEIGHT - 2, FOREGROUND_CORNER_RADIUS, FOREGROUND_CORNER_RADIUS);
        G2.fill(FOREGROUND);

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
        if (dim.width < 64 || dim.height < 32) {
            dim = new Dimension(64, 32);
        }
        return dim;
    }

    @Override
    public void setMinimumSize(final Dimension DIM) {
        int width = DIM.width < 64 ? 64 : DIM.width;
        int height = DIM.height < 32 ? 32 : DIM.height;
        super.setMinimumSize(new Dimension(width, height));
        calcInnerBounds();
        init(getInnerBounds().width, getInnerBounds().height);
        invalidate();
        repaint();
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension dim = super.getMaximumSize();
        if (dim.width > 1920 || dim.height > 960) {
            dim = new Dimension(1920, 960);
        }
        return dim;
    }

    @Override
    public void setMaximumSize(final Dimension DIM) {
        int width = DIM.width > 1920 ? 1920 : DIM.width;
        int height = DIM.height > 960 ? 960 : DIM.height;
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
        if (EVENT.getSource().equals(LCD_BLINKING_TIMER)) {
            lcdTextVisible ^= true;
        }
        repaint(getInnerBounds());
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "DisplayMulti";
    }
}
