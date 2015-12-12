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
import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.PointerType;
import eu.hansolo.steelseries.tools.PostPosition;
import eu.hansolo.steelseries.tools.Section;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.Transparency;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.ease.Spline;


/**
 *
 * @author hansolo
 */
public class WindDirection extends AbstractRadial {
    // <editor-fold defaultstate="collapsed" desc="Variable declaration">
    private double visibleValue = 0;
    private double angleStep;
    //private Font font = new Font("Verdana", 0, 30);
    private final Point2D CENTER = new Point2D.Double();
    // Images used to combine layers for background and foreground
    private BufferedImage bImage;
    private BufferedImage fImage;
    private BufferedImage pointerImage;
    private BufferedImage pointer2Image;
    private BufferedImage disabledImage;
    private ColorDef pointer2Color = ColorDef.BLUE;
    private CustomColorDef customPointer2Color = new CustomColorDef(Color.BLUE);
    private PointerType pointer2Type = PointerType.TYPE3;
    private boolean pointer2Visible;
    private double value2;
    private Timeline timeline = new Timeline(this);
    private final Spline EASE = new Spline(0.5f);
    private long easingDuration = 250;
    private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
    private TextLayout unitLayout;
    private final Rectangle2D UNIT_BOUNDARY = new Rectangle2D.Double();
    private TextLayout valueLayout;
    private final Rectangle2D VALUE_BOUNDARY = new Rectangle2D.Double();
    private final Rectangle2D LCD = new Rectangle2D.Double();
    private TextLayout infoLayout;
    private final Rectangle2D INFO_BOUNDARY = new Rectangle2D.Double();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public WindDirection() {
        super();
        setMinValue(-360);
        setMaxValue(360);
        setPointerType(PointerType.TYPE5);
        setLcdColor(LcdColor.BLACK_LCD);
        setValueCoupled(false);
        setLcdDecimals(1);
        setLcdVisible(true);
        calcAngleStep();
        value2 = 0;
        pointer2Visible = true;
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

        if (isDigitalFont()) {
            setLcdValueFont(getModel().getDigitalBaseFont().deriveFont(0.7f * GAUGE_WIDTH * 0.15f));
        } else {
            setLcdValueFont(getModel().getStandardBaseFont().deriveFont(0.625f * GAUGE_WIDTH * 0.15f));
        }

        if (isCustomLcdUnitFontEnabled()) {
            setLcdUnitFont(getCustomLcdUnitFont().deriveFont(0.25f * GAUGE_WIDTH * 0.15f));
        } else {
            setLcdUnitFont(getModel().getStandardBaseFont().deriveFont(0.25f * GAUGE_WIDTH * 0.15f));
        }

        setLcdInfoFont(getModel().getStandardInfoFont().deriveFont(0.15f * GAUGE_WIDTH * 0.15f));

        // Create Background Image
        if (bImage != null) {
            bImage.flush();
        }
        bImage = UTIL.createImage(GAUGE_WIDTH, GAUGE_WIDTH, Transparency.TRANSLUCENT);

        // Create Foreground Image
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

        create_SHIP_Image(GAUGE_WIDTH, bImage);

        create_TITLE_Image(GAUGE_WIDTH, getTitle(), getUnitString(), bImage);

        // Create sections if not empty
        if (!getSections().isEmpty()) {
            createSections(bImage);
        }

        create_TICKMARKS_Image(GAUGE_WIDTH, 0, 0, 0, 0, 0, 0, 0, true, true, null, bImage);

        createLcdImage(new Rectangle2D.Double(((getGaugeBounds().width - GAUGE_WIDTH * 0.4) / 2.0), (getGaugeBounds().height * 0.55), (GAUGE_WIDTH * 0.4), (GAUGE_WIDTH * 0.15)), getLcdColor(), getCustomLcdBackground(), bImage);
        LCD.setRect(((getGaugeBounds().width - GAUGE_WIDTH * 0.4) / 2.0), (getGaugeBounds().height * 0.55), GAUGE_WIDTH * 0.4, GAUGE_WIDTH * 0.15);

        if (pointerImage != null) {
            pointerImage.flush();
        }
        pointerImage = create_POINTER_Image(GAUGE_WIDTH, getPointerType());

        if (pointer2Image != null) {
            pointer2Image.flush();
        }
        pointer2Image = create_POINTER_Image(GAUGE_WIDTH, pointer2Type, pointer2Color, customPointer2Color);

        createPostsImage(GAUGE_WIDTH, fImage, PostPosition.CENTER);

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
        if (!isInitialized()) {
            return;
        }

        final Graphics2D G2 = (Graphics2D) g.create();

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Translate the coordinate system related to insets
        G2.translate(getFramelessOffset().getX(), getFramelessOffset().getY());

        CENTER.setLocation(getGaugeBounds().getCenterX(), getGaugeBounds().getCenterX());

        // Draw combined background image
        G2.drawImage(bImage, 0, 0, null);

        // Draw LCD display
        if (isLcdVisible()) {
            if (getLcdColor() == LcdColor.CUSTOM) {
                G2.setColor(getCustomLcdForeground());
            } else {
                G2.setColor(getLcdColor().TEXT_COLOR);
            }
            G2.setFont(getLcdUnitFont());
            final double UNIT_STRING_WIDTH;
            if (isLcdUnitStringVisible()) {
                unitLayout = new TextLayout(getLcdUnitString(), G2.getFont(), RENDER_CONTEXT);
                UNIT_BOUNDARY.setFrame(unitLayout.getBounds());
                G2.drawString(getLcdUnitString(), (int) (LCD.getX() + (LCD.getWidth() - UNIT_BOUNDARY.getWidth()) - LCD.getWidth() * 0.03), (int) (LCD.getY() + LCD.getHeight() * 0.76f));
                UNIT_STRING_WIDTH = UNIT_BOUNDARY.getWidth();
            } else {
                UNIT_STRING_WIDTH = 0;
            }
            G2.setFont(getLcdValueFont());
            switch (getModel().getNumberSystem()) {
                case HEX:
                    valueLayout = new TextLayout(Integer.toHexString((int) getLcdValue()).toUpperCase(), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(Integer.toHexString((int) getLcdValue()).toUpperCase(), (int) (LCD.getX() + (LCD.getWidth() - UNIT_STRING_WIDTH - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09), (int) (LCD.getY() + LCD.getHeight() * 0.76f));
                    break;

                case OCT:
                    valueLayout = new TextLayout(Integer.toOctalString((int) getLcdValue()), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(Integer.toOctalString((int) getLcdValue()), (int) (LCD.getX() + (LCD.getWidth() - UNIT_STRING_WIDTH - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09), (int) (LCD.getY() + LCD.getHeight() * 0.76f));
                    break;

                case DEC:

                default:
                    valueLayout = new TextLayout(formatLcdValue(getLcdValue()), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(formatLcdValue(getLcdValue()), (int) (LCD.getX() + (LCD.getWidth() - UNIT_STRING_WIDTH - VALUE_BOUNDARY.getWidth()) - LCD.getWidth() * 0.09), (int) (LCD.getY() + LCD.getHeight() * 0.76f));
                    break;
            }
            // Draw lcd info string
            if (!getLcdInfoString().isEmpty()) {
                G2.setFont(getLcdInfoFont());
                infoLayout = new TextLayout(getLcdInfoString(), G2.getFont(), RENDER_CONTEXT);
                INFO_BOUNDARY.setFrame(infoLayout.getBounds());
                G2.drawString(getLcdInfoString(), LCD.getBounds().x + 5, LCD.getBounds().y + (int) INFO_BOUNDARY.getHeight() + 5);
            }
        }

        // Draw the pointer2
        final AffineTransform OLD_TRANSFORM = G2.getTransform();
        G2.rotate(value2 * angleStep, CENTER.getX(), CENTER.getY());
        G2.drawImage(pointer2Image, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw the pointer
        G2.rotate(getValue() * angleStep, CENTER.getX(), CENTER.getY());
        G2.drawImage(pointerImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

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
    @Override
    public void setValue(final double VALUE) {
        if (isEnabled()) {
            super.setValue((VALUE % 360));

            if ((VALUE % 360) == 0) {
                this.visibleValue = 90;
            }

            if ((VALUE % 360) > 0 && (VALUE % 360) <= 180) {
                this.visibleValue = ((VALUE % 360));
            }

            if ((VALUE % 360) > 180 && (VALUE % 360) <= 360) {
                this.visibleValue = (360 - (VALUE % 360));
            }

            if (isValueCoupled()) {
                setLcdValue(visibleValue);
            }

            fireStateChanged();
            repaint(getInnerBounds());
        }
    }

    @Override
    public void setValueAnimated(double value) {
        if (isEnabled()) {
            if (timeline.getState() == Timeline.TimelineState.PLAYING_FORWARD || timeline.getState() == Timeline.TimelineState.PLAYING_REVERSE) {
                timeline.abort();
            }
            timeline = new Timeline(this);
            timeline.addPropertyToInterpolate("value", getValue(), value);
            timeline.setEase(EASE);

            timeline.setDuration(easingDuration);
            timeline.play();
        }
    }

    @Override
    public double getMinValue() {
        return -360.0;
    }

    @Override
    public double getMaxValue() {
        return 360.0;
    }

    public long getEasingDuration() {
        return this.easingDuration;
    }

    public void setEasingDuration(final long EASING_DURATION) {
        this.easingDuration = EASING_DURATION;
    }

    /**
     * Returns the colordefinition of the second pointer
     * @return the colordefinition of the second pointer
     */
    public ColorDef getPointer2Color() {
        return pointer2Color;
    }

    /**
     * Sets the colordefinition of the second pointer
     * @param POINTER2_COLOR
     */
    public void setPointer2Color(final ColorDef POINTER2_COLOR) {
        pointer2Color = POINTER2_COLOR;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns the value of the second pointer
     * @return the value of the second pointer
     */
    public double getValue2() {
        return value2;
    }

    /**
     * Sets the value of the second pointer
     * @param VALUE2
     */
    public void setValue2(final double VALUE2) {
        if (isEnabled()) {
            value2 = (VALUE2 % 360);
            fireStateChanged();
            repaint(getInnerBounds());
        }
    }

    /**
     * Returns true if the second pointer is visible
     * @return true if the second pointer is visible
     */
    public boolean isPointer2Visible() {
        return pointer2Visible;
    }

    /**
     * Enables / disables the visibility of the second pointer
     * @param POINTER2_VISIBLE
     */
    public void setPointer2Visible(final boolean POINTER2_VISIBLE) {
        pointer2Visible = POINTER2_VISIBLE;
        repaint(getInnerBounds());
    }

    /**
     * Returns the pointertype of the second pointer
     * @return the pointertype of the second pointer
     */
    public PointerType getPointer2Type() {
        return pointer2Type;
    }

    /**
     * Sets the pointertype of the second pointer
     * @param POINTER2_TYPE
     */
    public void setPointer2Type(final PointerType POINTER2_TYPE) {
        pointer2Type = POINTER2_TYPE;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns the color from which the custom pointer2 color will be calculated
     * @return the color from which the custom pointer2 color will be calculated
     */
    public Color getCustomPointer2Color() {
        return this.customPointer2Color.COLOR;
    }

    /**
     * Sets the color from which the custom pointer2 color is calculated
     * @param COLOR
     */
    public void setCustomPointer2Color(final Color COLOR) {
        this.customPointer2Color = new CustomColorDef(COLOR);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns the object that represents the custom pointer2 color
     * @return the object that represents the custom pointer2 color
     */
    public CustomColorDef getCustomPointer2ColorObject() {
        return this.customPointer2Color;
    }

    private void calcAngleStep() {
        angleStep = (2.0 * Math.PI) / 360.0;
    }

    @Override
    public Paint createCustomLcdBackgroundPaint(final Color[] LCD_COLORS) {
        final Point2D FOREGROUND_START = new Point2D.Double(0.0, LCD.getMinY() + 1.0);
        final Point2D FOREGROUND_STOP = new Point2D.Double(0.0, LCD.getMaxY() - 1);
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
    public Point2D getCenter() {
        return new Point2D.Double(bImage.getWidth() / 2.0 + getInnerBounds().x, bImage.getHeight() / 2.0 + getInnerBounds().y);
    }

    @Override
    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Double(bImage.getMinX(), bImage.getMinY(), bImage.getWidth(), bImage.getHeight());
    }

    @Override
    public Rectangle getLcdBounds() {
        return LCD.getBounds();
    }

    private void createSections(final BufferedImage IMAGE) {
        final double ORIGIN_CORRECTION;
        final double ANGLE_STEP;
        final double OUTER_RADIUS;
        final double INNER_RADIUS;
        final double FREE_AREA_OUTER_RADIUS;
        final double FREE_AREA_INNER_RADIUS;
        final Ellipse2D INNER;

        if (bImage != null) {
            ORIGIN_CORRECTION = 90.0;
            ANGLE_STEP = 1.0;
            OUTER_RADIUS = bImage.getWidth() * 0.38f;
            INNER_RADIUS = bImage.getWidth() * 0.38f - bImage.getWidth() * 0.04f;
            FREE_AREA_OUTER_RADIUS = bImage.getWidth() / 2.0 - OUTER_RADIUS;
            FREE_AREA_INNER_RADIUS = bImage.getWidth() / 2.0 - INNER_RADIUS;
            INNER = new Ellipse2D.Double(bImage.getMinX() + FREE_AREA_INNER_RADIUS, bImage.getMinY() + FREE_AREA_INNER_RADIUS, 2 * INNER_RADIUS, 2 * INNER_RADIUS);

            for (Section section : getSections()) {
                final double ANGLE_START = ORIGIN_CORRECTION - (section.getStart() * ANGLE_STEP) + (getMinValue() * ANGLE_STEP);
                final double ANGLE_EXTEND = -(section.getStop() - section.getStart()) * ANGLE_STEP;

                final java.awt.geom.Arc2D OUTER_ARC = new java.awt.geom.Arc2D.Double(java.awt.geom.Arc2D.PIE);
                OUTER_ARC.setFrame(bImage.getMinX() + FREE_AREA_OUTER_RADIUS, bImage.getMinY() + FREE_AREA_OUTER_RADIUS, 2 * OUTER_RADIUS, 2 * OUTER_RADIUS);
                OUTER_ARC.setAngleStart(ANGLE_START);
                OUTER_ARC.setAngleExtent(ANGLE_EXTEND);
                final java.awt.geom.Area SECTION = new java.awt.geom.Area(OUTER_ARC);

                SECTION.subtract(new java.awt.geom.Area(INNER));

                section.setSectionArea(SECTION);
            }

            if (isSectionsVisible() && IMAGE != null) {
                final Graphics2D G2 = IMAGE.createGraphics();
                G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (Section section : getSections()) {
                    G2.setColor(section.getColor());
                    G2.fill(section.getSectionArea());
                }
                G2.dispose();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image related">
    protected BufferedImage create_TICKMARKS_Image(final int WIDTH, final double FREE_AREA_ANGLE,
                                                                  final double OFFSET, final double MIN_VALUE,
                                                                  final double MAX_VALUE, final double ANGLE_STEP,
                                                                  final int TICK_LABEL_PERIOD,
                                                                  final int SCALE_DIVIDER_POWER,
                                                                  final boolean DRAW_TICKS,
                                                                  final boolean DRAW_TICK_LABELS,
                                                                  ArrayList<Section> tickmarkSections,
                                                                  BufferedImage image) {
        if (WIDTH <= 0) {
            return null;
        }
        if (image == null) {
            image = UTIL.createImage(WIDTH, (int) (1.0 * WIDTH), Transparency.TRANSLUCENT);
        }
        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        final Font STD_FONT = new Font("Verdana", 0, (int) (0.04 * WIDTH));
        final BasicStroke MEDIUM_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        final BasicStroke THIN_STROKE = new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        final int TEXT_DISTANCE = (int) (0.08 * WIDTH);
        //final int MIN_LENGTH = (int) (0.0133333333 * WIDTH);
        final int MED_LENGTH = (int) (0.02 * WIDTH);
        final int MAX_LENGTH = (int) (0.04 * WIDTH);
        //final int MIN_DIAMETER = (int) (0.0093457944 * WIDTH);
        //final int MED_DIAMETER = (int) (0.0186915888 * WIDTH);
        //final int MAX_DIAMETER = (int) (0.0280373832 * WIDTH);

        // Create the ticks itself
        final float RADIUS = IMAGE_WIDTH * 0.38f;
        final Point2D GAUGE_CENTER = new Point2D.Double(IMAGE_WIDTH / 2.0f, IMAGE_HEIGHT / 2.0f);

        // Draw ticks
        final Point2D INNER_POINT = new Point2D.Double(0, 0);
        final Point2D OUTER_POINT = new Point2D.Double(0, 0);
        final Point2D TEXT_POINT = new Point2D.Double(0, 0);
        final Line2D TICK_LINE = new Line2D.Double(0, 0, 1, 1);
        final Ellipse2D TICK_CIRCLE = new Ellipse2D.Double(0, 0, 1, 1);
        //final Rectangle2D TICK_RECT = new Rectangle2D.Double(0, 0, 1, 1);

        //final int MINI_DIAMETER = (int) (0.0093457944 * WIDTH);
        final int MINOR_DIAMETER = (int) (0.0186915888 * WIDTH);
        final int MAJOR_DIAMETER = (int) (0.03 * WIDTH);

        int counter = 0;
        int tickCounter = 0;
        float valueCounter = 90;
        boolean countUp = false;
        float valueStep = 1;

        G2.setFont(STD_FONT);

        double sinValue = 0;
        double cosValue = 0;

        final double STEP = (2.0 * Math.PI) / (360.0);

        for (double alpha = (2.0 * Math.PI); alpha >= STEP; alpha -= STEP) {
            G2.setStroke(THIN_STROKE);
            sinValue = Math.sin(alpha - Math.PI / 2);
            cosValue = Math.cos(alpha - Math.PI / 2);

            // Different tickmark every 5 units
            if (counter % 5 == 0) {
                G2.setColor(super.getBackgroundColor().LABEL_COLOR);
                G2.setStroke(THIN_STROKE);
                INNER_POINT.setLocation(GAUGE_CENTER.getX() + (RADIUS - MED_LENGTH) * sinValue, GAUGE_CENTER.getY() + (RADIUS - MED_LENGTH) * cosValue);
                OUTER_POINT.setLocation(GAUGE_CENTER.getX() + RADIUS * sinValue, GAUGE_CENTER.getY() + RADIUS * cosValue);

                // Draw ticks
                switch (getMinorTickmarkType()) {
                    case LINE:
                        TICK_LINE.setLine(INNER_POINT, OUTER_POINT);
                        G2.draw(TICK_LINE);
                        break;
                    case CIRCLE:
                        TICK_CIRCLE.setFrame(OUTER_POINT.getX() - MINOR_DIAMETER / 2.0, OUTER_POINT.getY() - MINOR_DIAMETER / 2.0, MINOR_DIAMETER, MINOR_DIAMETER);
                        G2.fill(TICK_CIRCLE);
                        break;
                    default:
                        TICK_LINE.setLine(INNER_POINT, OUTER_POINT);
                        G2.draw(TICK_LINE);
                        break;
                }
            }

            // Different tickmark every 45 units plus text
            if (counter == 30 || counter == 0) {
                G2.setColor(super.getBackgroundColor().LABEL_COLOR);
                G2.setStroke(MEDIUM_STROKE);
                INNER_POINT.setLocation(GAUGE_CENTER.getX() + (RADIUS - MAX_LENGTH) * sinValue, GAUGE_CENTER.getY() + (RADIUS - MAX_LENGTH) * cosValue);
                OUTER_POINT.setLocation(GAUGE_CENTER.getX() + RADIUS * sinValue, GAUGE_CENTER.getY() + RADIUS * cosValue);

                // Draw outer text
                TEXT_POINT.setLocation(GAUGE_CENTER.getX() + (RADIUS - TEXT_DISTANCE) * sinValue, GAUGE_CENTER.getY() + (RADIUS - TEXT_DISTANCE) * cosValue);
                G2.setFont(STD_FONT);

                G2.fill(UTIL.rotateTextAroundCenter(G2, String.valueOf((int) valueCounter), (int) TEXT_POINT.getX(), (int) TEXT_POINT.getY(), 0));

                counter = 0;
                tickCounter++;

                // Draw ticks
                switch (getMajorTickmarkType()) {
                    case LINE:
                        TICK_LINE.setLine(INNER_POINT, OUTER_POINT);
                        G2.draw(TICK_LINE);
                        break;
                    case CIRCLE:
                        TICK_CIRCLE.setFrame(OUTER_POINT.getX() - MAJOR_DIAMETER / 2.0, OUTER_POINT.getY() - MAJOR_DIAMETER / 2.0, MAJOR_DIAMETER, MAJOR_DIAMETER);
                        G2.fill(TICK_CIRCLE);
                        break;
                    default:
                        TICK_LINE.setLine(INNER_POINT, OUTER_POINT);
                        G2.draw(TICK_LINE);
                        break;
                }
            }

            counter++;
            if (valueCounter == 0) {
                countUp = true;
            }
            if (valueCounter == 180) {
                countUp = false;
            }
            if (countUp) {
                valueCounter += valueStep;
            } else {
                valueCounter -= valueStep;
            }
        }

        G2.dispose();

        return image;
    }

    private BufferedImage create_SHIP_Image(final int WIDTH, BufferedImage image) {
        if (WIDTH <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }
        if (image == null) {
            image = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        }
        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        final GeneralPath SHIP = new GeneralPath();
        SHIP.setWindingRule(Path2D.WIND_EVEN_ODD);
        SHIP.moveTo(IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.7242990654205608);
        SHIP.curveTo(IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.7242990654205608, IMAGE_WIDTH * 0.45794392523364486, IMAGE_HEIGHT * 0.7383177570093458, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.7383177570093458);
        SHIP.curveTo(IMAGE_WIDTH * 0.5420560747663551, IMAGE_HEIGHT * 0.7383177570093458, IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.7242990654205608, IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.7242990654205608);
        SHIP.curveTo(IMAGE_WIDTH * 0.5560747663551402, IMAGE_HEIGHT * 0.7242990654205608, IMAGE_WIDTH * 0.5654205607476636, IMAGE_HEIGHT * 0.6962616822429907, IMAGE_WIDTH * 0.5654205607476636, IMAGE_HEIGHT * 0.48598130841121495);
        SHIP.curveTo(IMAGE_WIDTH * 0.5654205607476636, IMAGE_HEIGHT * 0.2897196261682243, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.2336448598130841, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.2336448598130841);
        SHIP.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.2336448598130841, IMAGE_WIDTH * 0.42990654205607476, IMAGE_HEIGHT * 0.2897196261682243, IMAGE_WIDTH * 0.42990654205607476, IMAGE_HEIGHT * 0.48598130841121495);
        SHIP.curveTo(IMAGE_WIDTH * 0.42990654205607476, IMAGE_HEIGHT * 0.719626168224299, IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.7242990654205608, IMAGE_WIDTH * 0.4439252336448598, IMAGE_HEIGHT * 0.7242990654205608);
        SHIP.closePath();
        G2.setColor(getBackgroundColor().LABEL_COLOR);
        G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        G2.draw(SHIP);

        G2.dispose();

        return image;
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "WindDirection";
    }
}
