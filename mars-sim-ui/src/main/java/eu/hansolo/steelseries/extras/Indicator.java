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
package eu.hansolo.steelseries.extras;

import eu.hansolo.steelseries.gauges.AbstractGauge;
import eu.hansolo.steelseries.gauges.AbstractRadial;
import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.CustomColorDef;
import eu.hansolo.steelseries.tools.SymbolImageFactory;
import eu.hansolo.steelseries.tools.SymbolType;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public class Indicator extends AbstractRadial {
    // <editor-fold defaultstate="collapsed" desc="Variable declarations">

    private static final SymbolImageFactory SYMBOL_FACTORY = SymbolImageFactory.INSTANCE;
    // Images used to combine layers for background and foreground
    private BufferedImage bImage;
    private BufferedImage symbolOnImage;
    private BufferedImage symbolOffImage;
    private BufferedImage fImage;
    private BufferedImage disabledImage;
    private SymbolType symbolType = SymbolType.HORN;
    private ColorDef onColor = ColorDef.RED;
    private CustomColorDef customOnColor = new CustomColorDef(Color.RED);
    private ColorDef offColor = ColorDef.GRAY;
    private CustomColorDef customOffColor = new CustomColorDef(Color.DARK_GRAY);
    private boolean on = false;
    private boolean glow = true;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public Indicator() {
        super();
        init(getInnerBounds().width, getInnerBounds().height);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    @Override
    public final AbstractGauge init(int WIDTH, int HEIGHT) {
        final int GAUGE_WIDTH = isFrameVisible() ? WIDTH : getGaugeBounds().width;
        final int GAUGE_HEIGHT = isFrameVisible() ? HEIGHT : getGaugeBounds().height;
        if (GAUGE_WIDTH <= 1 || GAUGE_HEIGHT <= 1) {
            return this;
        }

        if (!isFrameVisible()) {
            setFramelessOffset(-getGaugeBounds().width * 0.0841121495, -getGaugeBounds().width * 0.0841121495);
        } else {
            setFramelessOffset(getGaugeBounds().x, getGaugeBounds().y);
        }

        // Create background image
        if (bImage != null) {
            bImage.flush();
        }
        bImage = UTIL.createImage(GAUGE_WIDTH, GAUGE_WIDTH, Transparency.TRANSLUCENT);

        // Create the symbol image
        if (symbolOnImage != null) {
            symbolOnImage.flush();
        }
        symbolOnImage = SYMBOL_FACTORY.createSymbol(GAUGE_WIDTH, symbolType, onColor, customOnColor, glow);

        // Create the symbol image
        if (symbolOffImage != null) {
            symbolOffImage.flush();
        }
        symbolOffImage = SYMBOL_FACTORY.createSymbol(GAUGE_WIDTH, symbolType, offColor, customOffColor, false);

        // Create foreground image
        if (fImage != null) {
            fImage.flush();
        }
        fImage = UTIL.createImage(GAUGE_WIDTH, GAUGE_WIDTH, Transparency.TRANSLUCENT);

        if (isFrameVisible()) {
            switch (getFrameType()) {
                case ROUND:
                    FRAME_FACTORY.createRadialFrame(GAUGE_WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameEffect(), bImage);
                    break;
                case SQUARE:
                    FRAME_FACTORY.createLinearFrame(GAUGE_WIDTH, GAUGE_WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameEffect(), bImage);
                    break;
                default:
                    FRAME_FACTORY.createRadialFrame(GAUGE_WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameEffect(), bImage);
                    break;
            }
        }

        if (isBackgroundVisible()) {
            create_BACKGROUND_Image(GAUGE_WIDTH, "", "", bImage);
        }

        //create_TITLE_Image(WIDTH, getTitle(), getUnitString(), bImage);

        if (isForegroundVisible()) {
            switch (getFrameType()) {
                case SQUARE:
                    FOREGROUND_FACTORY.createLinearForeground(GAUGE_WIDTH, GAUGE_WIDTH, false, bImage);
                    break;

                case ROUND:

                default:
                    FOREGROUND_FACTORY.createRadialForeground(GAUGE_WIDTH, false, getForegroundType(), fImage);
                    break;
            }
        }

        if (disabledImage != null) {
            disabledImage.flush();
        }
        disabledImage = create_DISABLED_Image(GAUGE_WIDTH);

        return this;
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

        // Translate the coordinate system related to insets
        G2.translate(getFramelessOffset().getX(), getFramelessOffset().getY());

        // Draw combined background image
        G2.drawImage(bImage, 0, 0, null);

        // Draw the symbol image
        if (on) {
            G2.drawImage(symbolOnImage, 0, 0, null);
        } else {
            G2.drawImage(symbolOffImage, 0, 0, null);
        }

        // Draw combined foreground image
        G2.drawImage(fImage, 0, 0, null);

        if (!isEnabled()) {
            G2.drawImage(disabledImage, 0, 0, null);
        }

        // Translate the coordinate system back to original
        G2.translate(-getInnerBounds().x, -getInnerBounds().y);

        G2.dispose();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    /**
     * Returns the type of symbol that will be drawn on the indicator
     * @return the type of symbol that will be drawn on the indicator
     */
    public SymbolType getSymbolType() {
        return symbolType;
    }

    /**
     * Sets the type of symbol that will be drawn on the indicator
     * @param SYMBOL_TYPE
     */
    public void setSymbolType(final SymbolType SYMBOL_TYPE) {
        symbolType = SYMBOL_TYPE;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns true if the symbol is visualized with it's on color
     * @return true if the symbol is visualized with it's on color
     */
    public boolean isOn() {
        return on;
    }

    /**
     * Sets the symbol to on or off
     * @param ON
     */
    public void setOn(final boolean ON) {
        on = ON;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns the color definition that is used to visualize the on state of the symbol
     * @return the color definition that is used to visualize the on state of the symbol
     */
    public ColorDef getOnColor() {
        return onColor;
    }

    /**
     * Sets the color definition that is used to visualize the on state of the symbol
     * @param ON_COLOR
     */
    public void setOnColor(final ColorDef ON_COLOR) {
        onColor = ON_COLOR;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns the custom color definition that is used to visualize the on state of the symbol
     * @return the custom color definition that is used to visualize the on state of the symbol
     */
    public CustomColorDef getCustomOnColor() {
        return customOnColor;
    }

    /**
     * Sets the custom color definition that will be used to visualize the on state of the symbol
     * @param CUSTOM_ON_COLOR
     */
    public void setCustomOnColor(final CustomColorDef CUSTOM_ON_COLOR) {
        customOnColor = CUSTOM_ON_COLOR;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns the color definition that is used to visualize the off state of the symbol
     * @return the color definition that is used to visualize the off state of the symbol
     */
    public ColorDef getOffColor() {
        return offColor;
    }

    /**
     * Sets the color definition that will be used to visualize the off state of the symbol
     * @param OFF_COLOR
     */
    public void setOffColor(final ColorDef OFF_COLOR) {
        offColor = OFF_COLOR;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns the custom color definition that is used to visualize the off state of the symbol
     * @return the custom color definition that is used to visualize the off state of the symbol
     */
    public CustomColorDef getCustomOffColor() {
        return customOffColor;
    }

    /**
     * Sets the custom color definition that is used to visualize the off state of the symbol
     * @param CUSTOM_OFF_COLOR
     */
    public void setCustomOffColor(final CustomColorDef CUSTOM_OFF_COLOR) {
        customOffColor = CUSTOM_OFF_COLOR;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns true if a glow effect will be applied to the on state of the symbol
     * @return true if a glow effect will be applied to the on state of the symbol
     */
    public boolean isGlow() {
        return glow;
    }

    /**
     * Enables / disables the glow effect to the on state of the symbol
     * @param GLOW
     */
    public void setGlow(final boolean GLOW) {
        glow = GLOW;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    @Override
    public Point2D getCenter() {
        return new Point2D.Double(bImage.getWidth() / 2.0 + getInnerBounds().x, bImage.getHeight() / 2.0 + getInnerBounds().y);
    }

    @Override
    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Double(bImage.getMinX(), bImage.getMinY(), bImage.getWidth(), bImage.getHeight());
    }

    @Override
    public Rectangle getLcdBounds() {
        return new Rectangle();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Size related methods">
    @Override
    public java.awt.Dimension getMinimumSize() {
        return new java.awt.Dimension(50, 50);
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "Indicator";
    }
}
