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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.Timer;
import javax.swing.border.Border;

import org.mars_sim.msp.ui.steelseries.tools.ColorDef;
import org.mars_sim.msp.ui.steelseries.tools.CustomColorDef;
import org.mars_sim.msp.ui.steelseries.tools.LcdColor;
import org.mars_sim.msp.ui.steelseries.tools.NumberSystem;
import org.mars_sim.msp.ui.steelseries.tools.Orientation;
import org.mars_sim.msp.ui.steelseries.tools.Util;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.TimelineScenario;
import org.pushingpixels.trident.callback.TimelineCallback;
import org.pushingpixels.trident.ease.Sine;
import org.pushingpixels.trident.ease.Spline;
import org.pushingpixels.trident.ease.TimelineEase;


/**
 *
 * @author hansolo
 */
public abstract class AbstractLinear extends AbstractGauge implements Lcd, ActionListener {
    // <editor-fold defaultstate="collapsed" desc="Variable declarations">
    private final Rectangle INNER_BOUNDS;
    // Bar related
    private boolean startingFromZero;
    private boolean transparentSectionsEnabled;
    private boolean transparentAreasEnabled;
    // LED related variables
    private Point2D ledPosition;
    // User LED related variables
    private Point2D userLedPosition;
    // LCD related variables
    private String lcdUnitString;
    private double lcdValue;
    private String lcdInfoString;
    protected static final Font LCD_STANDARD_FONT = new Font("Verdana", 0, 24);
    protected static final Font LCD_DIGITAL_FONT = Util.INSTANCE.getDigitalFont().deriveFont(24);
    private Timeline lcdTimeline;
    private boolean lcdTextVisible;
    private Timer LCD_BLINKING_TIMER;
    // Animation related variables
    private Timeline timeline;
    private final TimelineEase STANDARD_EASING;
    private final TimelineEase RETURN_TO_ZERO_EASING;
    private TimelineCallback timelineCallback;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public AbstractLinear() {
        super();
        INNER_BOUNDS = new Rectangle(120, 300);
        startingFromZero = false;
        transparentSectionsEnabled = false;
        transparentAreasEnabled = false;
        ledPosition = new Point2D.Double((getInnerBounds().width - 18.0 - 16.0) / getInnerBounds().width, 0.453271028);
        userLedPosition = new Point2D.Double(18.0 / getInnerBounds().width, 0.453271028);
        lcdValue = 0;
        lcdTimeline = new Timeline(this);
        lcdUnitString = getUnitString();
        lcdInfoString = "";
        timeline = new Timeline(this);
        STANDARD_EASING = new Spline(0.5f);
        RETURN_TO_ZERO_EASING = new Sine();
        lcdTextVisible = true;
        LCD_BLINKING_TIMER = new Timer(500, this);
        addComponentListener(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    /**
     * Uses trident animation library to animate
     * the setting of the value.
     * The method plays a defined trident timeline
     * that calls the setValue(double value) method
     * with a given easing behaviour and duration.
     * You should always use this method to set the
     * gauge to a given value.
     * @param VALUE
     */
    public void setValueAnimated(final double VALUE) {
        if (isEnabled()) {
            if (timeline.getState() != Timeline.TimelineState.IDLE) {
                timeline.abort();
            }

            final double TARGET_VALUE = VALUE < getMinValue() ? getMinValue() : (VALUE > getMaxValue() ? getMaxValue() : VALUE);

            if (!isAutoResetToZero()) {
                timeline.removeCallback(timelineCallback);
                timeline = new Timeline(this);
                timeline.addPropertyToInterpolate("value", getValue(), TARGET_VALUE);
                timeline.setEase(STANDARD_EASING);
                //TIMELINE.setDuration((long) (getStdTimeToValue() * fraction));
                timeline.setDuration(getStdTimeToValue());
                timelineCallback = new TimelineCallback() {

                    @Override
                    public void onTimelineStateChanged(final Timeline.TimelineState OLD_STATE,
                                                       final Timeline.TimelineState NEW_STATE,
                                                       final float OLD_VALUE, final float NEW_VALUE) {
                        if (NEW_STATE == Timeline.TimelineState.IDLE) {
                            repaint(getInnerBounds());
                        }

                        // Check if current value exceeds maxMeasuredValue
                        if (getValue() > getMaxMeasuredValue()) {
                            setMaxMeasuredValue(getValue());
                        }
                    }

                    @Override
                    public void onTimelinePulse(final float OLD_VALUE, final float NEW_VALUE) {
                        // Check if current value exceeds maxMeasuredValue
                        if (getValue() > getMaxMeasuredValue()) {
                            setMaxMeasuredValue(getValue());
                        }

                        // Check if current value exceeds minMeasuredValue
                        if (getValue() < getMinMeasuredValue()) {
                            setMinMeasuredValue(getValue());
                        }
                    }
                };

                timeline.addCallback(timelineCallback);
                timeline.play();
            } else {
                final TimelineScenario AUTOZERO_SCENARIO = new TimelineScenario.Sequence();

                final Timeline TIMELINE_TO_VALUE = new Timeline(this);
                TIMELINE_TO_VALUE.addPropertyToInterpolate("value", getValue(), TARGET_VALUE);
                TIMELINE_TO_VALUE.setEase(RETURN_TO_ZERO_EASING);
                //TIMELINE_TO_VALUE.setDuration((long) (getRtzTimeToValue() * fraction));
                TIMELINE_TO_VALUE.setDuration(getRtzTimeToValue());
                TIMELINE_TO_VALUE.addCallback(new TimelineCallback() {

                    @Override
                    public void onTimelineStateChanged(Timeline.TimelineState oldState,
                                                       Timeline.TimelineState newState,
                                                       float oldValue, float newValue) {
                        if (oldState == Timeline.TimelineState.PLAYING_FORWARD && newState == Timeline.TimelineState.DONE) {
                            // Set the peak value and start the timer
                            getModel().setPeakValue(getValue());
                            getModel().setPeakValueVisible(true);
                            if (getPeakTimer().isRunning()) {
                                stopPeakTimer();
                            }
                            startPeakTimer();

                            // Check if current value exceeds maxMeasuredValue
                            if (getValue() > getMaxMeasuredValue()) {
                                setMaxMeasuredValue(getValue());
                            }
                        }
                    }

                    @Override
                    public void onTimelinePulse(float oldValue, float newValue) {
                        // Check if current value exceeds maxMeasuredValue
                        if (getValue() > getMaxMeasuredValue()) {
                            setMaxMeasuredValue(getValue());
                        }

                        // Check if current value exceeds minMeasuredValue
                        if (getValue() < getMinMeasuredValue()) {
                            setMinMeasuredValue(getValue());
                        }
                    }
                });

                final Timeline TIMELINE_TO_ZERO = new Timeline(this);
                TIMELINE_TO_ZERO.addPropertyToInterpolate("value", TARGET_VALUE, 0.0);
                TIMELINE_TO_ZERO.setEase(RETURN_TO_ZERO_EASING);
                //TIMELINE_TO_ZERO.setDuration((long) (getRtzTimeBackToZero() * fraction));
                TIMELINE_TO_ZERO.setDuration(getRtzTimeBackToZero());

                AUTOZERO_SCENARIO.addScenarioActor(TIMELINE_TO_VALUE);
                AUTOZERO_SCENARIO.addScenarioActor(TIMELINE_TO_ZERO);

//                AUTOZERO_SCENARIO.addCallback(new org.pushingpixels.trident.callback.TimelineScenarioCallback()
//                {
//                    @Override
//                    public void onTimelineScenarioDone()
//                    {
//
//                    }
//                });

                AUTOZERO_SCENARIO.play();
            }
        }
    }

    /**
     * Returns the color of the bar
     * @return the selected color of the bar
     */
    public ColorDef getValueColor() {
        return getModel().getValueColor();
    }

    /**
     * Sets the color of the bar
     * @param VALUE_COLOR
     */
    public void setValueColor(final ColorDef VALUE_COLOR) {
        getModel().setValueColor(VALUE_COLOR);
        repaint(getInnerBounds());
    }

    /**
     * Returns the object that represents holds the custom value color
     * @return the object that represents the custom value color
     */
    public CustomColorDef getCustomValueColorObject() {
        return getModel().getCustomValueColorObject();
    }

    /**
     * Returns the color of the bar from which the custom bar color will calculated
     * @return the color of the bar from which the custom bar color will be calculated
     */
    public Color getCustomValueColor() {
        return getModel().getCustomValueColor();
    }

    /**
     * Sets the color of the bar from which the custom bar color will be calculated
     * @param COLOR
     */
    public void setCustomValueColor(final Color COLOR) {
        getModel().setCustomValueColorObject(new CustomColorDef(COLOR));
        repaint(getInnerBounds());
    }

    /**
     * Returns true if the bar/bargraph will always start from zero instead from
     * the minValue. This could be useful if you would like to create something
     * like a g-force meter, where 0 is in the center of the range and the bar
     * could move in negative and positive direction. In combination with
     * AutoReturnToZero this feature might be useful.
     * @return true if the bar/bargraph will always start to in-/decrease from zero
     */
    public boolean isStartingFromZero() {
        return this.startingFromZero;
    }

    /**
     * Enables/Disables the feature that the bar/bargraph will always start from zero
     * instead from the minValue. This could be useful if you would like to create
     * something like a g-force meter, where 0 is in the center of the range and
     * the bar could move in negative and positive direction. In combination with
     * AutoReturnToZero this feature might be useful.
     * @param STARTING_FROM_ZERO
     */
    public void setStartingFromZero(final boolean STARTING_FROM_ZERO) {
        this.startingFromZero = STARTING_FROM_ZERO;
    }

    public int getMaxNoOfMinorTicks() {
        return getModel().getMaxNoOfMinorTicks();
    }

    public void setMaxNoOfMinorTicks(final int MAX_NO_OF_MINOR_TICKS) {
        getModel().setMaxNoOfMinorTicks(MAX_NO_OF_MINOR_TICKS);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    public int getMaxNoOfMajorTicks() {
        return getModel().getMaxNoOfMajorTicks();
    }

    public void setMaxNoOfMajorTicks(final int MAX_NO_OF_MAJOR_TICKS) {
        getModel().setMaxNoOfMajorTicks(MAX_NO_OF_MAJOR_TICKS);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns the current position of the gauge threshold led
     * @return the current position of the gauge threshold led
     */
    @Override
    public Point2D getLedPosition() {
        return ledPosition;
    }

    /**
     * Sets the position of the gauge threshold led to the given values
     * @param X
     * @param Y
     */
    @Override
    public void setLedPosition(final double X, final double Y) {
        ledPosition.setLocation(X, Y);
        repaint(getInnerBounds());
    }

    /**
     * Sets the position of the gauge threshold led to the given values
     * @param LED_POSITION
     */
    public void setLedPosition(final Point2D LED_POSITION) {
        ledPosition.setLocation(LED_POSITION);
        repaint(getInnerBounds());
    }

    /**
     * Returns the current position of the gauge user led
     * @return the current position of the gauge user led
     */
    @Override
    public Point2D getUserLedPosition() {
        return userLedPosition;
    }

    /**
     * Sets the position of the gauge user led to the given values
     * @param X
     * @param Y
     */
    @Override
    public void setUserLedPosition(final double X, final double Y) {
        userLedPosition.setLocation(X, Y);
        repaint(getInnerBounds());
    }

    /**
     * Sets the position of the gauge threshold led to the given values
     * @param USER_LED_POSITION
     */
    public void setUserLedPosition(final Point2D USER_LED_POSITION) {
        userLedPosition.setLocation(USER_LED_POSITION);
        repaint(getInnerBounds());
    }

    protected void createLedImages() {
        if (getOrientation() == Orientation.VERTICAL) {
            recreateLedImages(getWidth());
        } else {
            recreateLedImages(getHeight());
        }
    }

    /**
     * Returns the visibility of the lcd display
     * @return true if the lcd display is visible
     */
    public boolean isLcdVisible() {
        return getModel().isLcdVisible();
    }

    /**
     * Enables or disables the visibility of the lcd display
     * @param LCD_VISIBLE
     */
    public void setLcdVisible(final boolean LCD_VISIBLE) {
        getModel().setLcdVisible(LCD_VISIBLE);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns true if the lcd text is visible which is needed for lcd blinking
     * @return true if the lcd text is visible which is needed for lcd blinking
     */
    public boolean isLcdTextVisible() {
        return lcdTextVisible;
    }

    @Override
    public boolean isValueCoupled() {
        return getModel().isValueCoupled();
    }

    @Override
    public void setValueCoupled(final boolean VALUE_COUPLED) {
        getModel().setValueCoupled(VALUE_COUPLED);
        repaint(getInnerBounds());
    }

    @Override
    public double getLcdValue() {
        return this.lcdValue;
    }

    @Override
    public void setLcdValue(final double LCD_VALUE) {
        this.lcdValue = LCD_VALUE;
        if (getLcdNumberSystem() != NumberSystem.DEC) {
            if (LCD_VALUE < 0) {
                setLcdNumberSystem(NumberSystem.DEC);
            }
        }
        repaint(getLcdBounds());
    }

    @Override
    public void setLcdValueAnimated(final double LCD_VALUE) {
        if (lcdTimeline.getState() == Timeline.TimelineState.PLAYING_FORWARD || lcdTimeline.getState() == Timeline.TimelineState.PLAYING_REVERSE) {
            lcdTimeline.abort();
        }
        lcdTimeline = new Timeline(this);
        lcdTimeline.addPropertyToInterpolate("lcdValue", this.lcdValue, LCD_VALUE);
        lcdTimeline.setEase(new Spline(0.5f));

        lcdTimeline.play();
    }

    @Override
    public double getLcdThreshold() {
        return getModel().getLcdThreshold();
    }

    @Override
    public void setLcdThreshold(final double LCD_THRESHOLD) {
        getModel().setLcdThreshold(LCD_THRESHOLD);
        if (getModel().isLcdThresholdVisible()) {
            repaint(getInnerBounds());
        }
    }

    @Override
    public boolean isLcdThresholdVisible() {
        return getModel().isLcdThresholdVisible();
    }

    @Override
    public void setLcdThresholdVisible(final boolean LCD_THRESHOLD_VISIBLE) {
        getModel().setLcdThresholdVisible(LCD_THRESHOLD_VISIBLE);
        repaint(getInnerBounds());
    }

    @Override
    public boolean isLcdThresholdBehaviourInverted() {
        return getModel().isLcdThresholdBehaviourInverted();
    }

    @Override
    public void setLcdThresholdBehaviourInverted(final boolean LCD_THRESHOLD_BEHAVIOUR_INVERTED) {
        getModel().setLcdThresholdBehaviourInverted(LCD_THRESHOLD_BEHAVIOUR_INVERTED);
        repaint(getInnerBounds());
    }

    @Override
    public boolean isLcdBlinking() {
        return getModel().isLcdBlinking();
    }

    @Override
    public void setLcdBlinking(final boolean LCD_BLINKING) {
        if (LCD_BLINKING) {
            LCD_BLINKING_TIMER.start();
        } else {
            LCD_BLINKING_TIMER.stop();
            lcdTextVisible = true;
        }
        getModel().setLcdBlinking(LCD_BLINKING);
    }

    @Override
    public String getLcdUnitString() {
        return lcdUnitString;
    }

    @Override
    public void setLcdUnitString(final String UNIT_STRING) {
        this.lcdUnitString = UNIT_STRING;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    @Override
    public boolean isLcdUnitStringVisible() {
        return getModel().isLcdUnitStringVisible();
    }

    @Override
    public void setLcdUnitStringVisible(final boolean UNIT_STRING_VISIBLE) {
        getModel().setLcdUnitStringVisible(UNIT_STRING_VISIBLE);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    @Override
    public boolean isDigitalFont() {
        return getModel().isDigitalFontEnabled();
    }

    @Override
    public void setDigitalFont(final boolean DIGITAL_FONT) {
        getModel().setDigitalFontEnabled(DIGITAL_FONT);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    @Override
    public boolean isCustomLcdUnitFontEnabled() {
        return getModel().isCustomLcdUnitFontEnabled();
    }

    @Override
    public void setCustomLcdUnitFontEnabled(final boolean USE_CUSTOM_LCD_UNIT_FONT) {
        getModel().setCustomLcdUnitFontEnabled(USE_CUSTOM_LCD_UNIT_FONT);
        repaint(getInnerBounds());
    }

    @Override
    public Font getCustomLcdUnitFont() {
        return getModel().getCustomLcdUnitFont();
    }

    @Override
    public void setCustomLcdUnitFont(final Font CUSTOM_LCD_UNIT_FONT) {
        getModel().setCustomLcdUnitFont(CUSTOM_LCD_UNIT_FONT);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    @Override
    public int getLcdDecimals() {
        return getModel().getLcdDecimals();
    }

    @Override
    public void setLcdDecimals(final int DECIMALS) {
        getModel().setLcdDecimals(DECIMALS);
        repaint(getInnerBounds());
    }

    @Override
    public LcdColor getLcdColor() {
        return getModel().getLcdColor();
    }

    @Override
    public void setLcdColor(final LcdColor COLOR) {
        getModel().setLcdColor(COLOR);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    @Override
    public Paint getCustomLcdBackground() {
        return getModel().getCustomLcdBackground();
    }

    @Override
    public void setCustomLcdBackground(final Paint CUSTOM_LCD_BACKGROUND) {
        getModel().setCustomLcdBackground(CUSTOM_LCD_BACKGROUND);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    @Override
    public boolean isLcdBackgroundVisible() {
        return getModel().isLcdBackgroundVisible();
    }

    @Override
    public void setLcdBackgroundVisible(final boolean LCD_BACKGROUND_VISIBLE) {
        getModel().setLcdBackgroundVisible(LCD_BACKGROUND_VISIBLE);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    @Override
    public Color getCustomLcdForeground() {
        return getModel().getCustomLcdForeground();
    }

    @Override
    public void setCustomLcdForeground(final Color CUSTOM_LCD_FOREGROUND) {
        getModel().setCustomLcdForeground(CUSTOM_LCD_FOREGROUND);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    @Override
    public String formatLcdValue(final double VALUE) {
        final StringBuilder DEC_BUFFER = new StringBuilder(16);
        DEC_BUFFER.append("0");

        if (getModel().getLcdDecimals() > 0) {
            DEC_BUFFER.append(".");
        }

        for (int i = 0; i < getModel().getLcdDecimals(); i++) {
            DEC_BUFFER.append("0");
        }

        if (getModel().isLcdScientificFormatEnabled()) {
            DEC_BUFFER.append("E0");
        }
        DEC_BUFFER.trimToSize();
        final java.text.DecimalFormat DEC_FORMAT = new java.text.DecimalFormat(DEC_BUFFER.toString(), new java.text.DecimalFormatSymbols(java.util.Locale.US));

        return DEC_FORMAT.format(VALUE);
    }

    @Override
    public boolean isLcdScientificFormat() {
        return getModel().isLcdScientificFormatEnabled();
    }

    @Override
    public void setLcdScientificFormat(boolean LCD_SCIENTIFIC_FORMAT) {
        getModel().setLcdScientificFormatEnabled(LCD_SCIENTIFIC_FORMAT);
        repaint(getInnerBounds());
    }

    @Override
    public Font getLcdValueFont() {
        return getModel().getLcdValueFont();
    }

    @Override
    public void setLcdValueFont(final Font LCD_VALUE_FONT) {
        getModel().setLcdValueFont(LCD_VALUE_FONT);
        repaint(getInnerBounds());
    }

    @Override
    public Font getLcdUnitFont() {
        return getModel().getLcdUnitFont();
    }

    @Override
    public void setLcdUnitFont(final Font LCD_UNIT_FONT) {
        getModel().setLcdUnitFont(LCD_UNIT_FONT);
        repaint(getInnerBounds());
    }

    @Override
    public Font getLcdInfoFont() {
        return getModel().getLcdInfoFont();
    }

    @Override
    public void setLcdInfoFont(final Font LCD_INFO_FONT) {
        getModel().setLcdInfoFont(LCD_INFO_FONT);
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
    public NumberSystem getLcdNumberSystem() {
        return getModel().getNumberSystem();
    }

    @Override
    public void setLcdNumberSystem(final NumberSystem NUMBER_SYSTEM) {
        getModel().setNumberSystem(NUMBER_SYSTEM);
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
    public abstract Rectangle getLcdBounds();

    @Override
    public void toggleDesign() {
        if (getActiveDesign().equals(getDesign1())) {
            setActiveDesign(getDesign2());
        } else {
            setActiveDesign(getDesign1());
        }
    }

    /**
    * Returns true if the glow indicator is visible
    * @return true if the glow indicator is visible
    */
    @Override
    public boolean isGlowVisible() {
        return getModel().isGlowVisible();
    }

    /**
    * Enables / disables the glow indicator
    * @param GLOW_VISIBLE
    */
    public void setGlowVisible(final boolean GLOW_VISIBLE) {
        getModel().setGlowVisible(GLOW_VISIBLE);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
    * Returns the color that will be used for the glow indicator
    * @return the color that will be used for the glow indicator
    */
    public  Color getGlowColor() {
        return getModel().getGlowColor();
    }

    /**
    * Sets the color that will be used for the glow indicator
    * @param GLOW_COLOR
    */
    public void setGlowColor(final Color GLOW_COLOR) {
        getModel().setGlowColor(GLOW_COLOR);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
    * Returns true if the glow indicator is glowing
    * @return true if the glow indicator is glowing
    */
    public boolean isGlowing() {
        return getModel().isGlowing();
    }

    /**
    * Enables / disables the glowing of the glow indicator
    * @param GLOWING
    */
    public void setGlowing(final boolean GLOWING) {
        getModel().setGlowing(GLOWING);
        repaint(getInnerBounds());
    }

    /**
     * Returns the color of the small outer frame of the gauge
     * @return the color of the small outer frame of the gauge
     */
    public Paint getOuterFrameColor() {
        return FRAME_FACTORY.getOuterFrameColor();
    }

    /**
     * Sets the color of the small outer frame of the gauge
     * @param OUTER_FRAME_COLOR
     */
    public void setOuterFrameColor(final Paint OUTER_FRAME_COLOR) {
        FRAME_FACTORY.setOuterFrameColor(OUTER_FRAME_COLOR);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns the color of the small inner frame of the gauge
     * @return the color of the small inner frame of the gauge
     */
    public Paint getInnerFrameColor() {
        return FRAME_FACTORY.getInnerFrameColor();
    }

    /**
     * Sets the color of the small inner frame of the gauge
     * @param INNER_FRAME_COLOR
     */
    public void setInnerFrameColor(final Paint INNER_FRAME_COLOR) {
        FRAME_FACTORY.setInnerFrameColor(INNER_FRAME_COLOR);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns true if the sections will be filled with a transparent color
     * @return true if the sections will be filled with a transparent color
     */
    public boolean isTransparentSectionsEnabled() {
        return transparentSectionsEnabled;
    }

    /**
     * Enables / disables the usage of a transparent color for filling sections
     * @param TRANSPARENT_SECTIONS_ENABLED
     */
    public void setTransparentSectionsEnabled(final boolean TRANSPARENT_SECTIONS_ENABLED) {
        transparentSectionsEnabled = TRANSPARENT_SECTIONS_ENABLED;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

     /**
     * Returns true if the areas will be filled with a transparent color
     * @return true if the areas will be filled with a transparent color
     */
    public boolean isTransparentAreasEnabled() {
        return transparentAreasEnabled;
    }

    /**
     * Enables / disables the usage of a transparent color for filling areas
     * @param TRANSPARENT_AREAS_ENABLED
     */
    public void setTransparentAreasEnabled(final boolean TRANSPARENT_AREAS_ENABLED) {
        transparentAreasEnabled = TRANSPARENT_AREAS_ENABLED;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image creation methods">
    /**
     * Returns the frame image with the currently active framedesign
     * with the given with and height.
     * @param WIDTH
     * @param HEIGHT
     * @return buffered image containing the frame in the active frame design
     */
    protected BufferedImage create_FRAME_Image(final int WIDTH, final int HEIGHT) {
        return FRAME_FACTORY.createLinearFrame(WIDTH, HEIGHT, getFrameDesign(), getCustomFrameDesign(), getFrameEffect());
    }

    /**
     * Returns the frame image with the currently active framedesign
     * with the given with and height.
     * @param WIDTH
     * @param HEIGHT
     * @param IMAGE
     * @return buffered image containing the frame in the active frame design
     */
    protected BufferedImage create_FRAME_Image(final int WIDTH, final int HEIGHT, final BufferedImage IMAGE) {
        return FRAME_FACTORY.createLinearFrame(WIDTH, HEIGHT, getFrameDesign(), getCustomFrameDesign(), getFrameBaseColor(), isFrameBaseColorEnabled(), getFrameEffect(), IMAGE);
    }

    /**
     * Returns the background image with the currently active backgroundcolor
     * with the given width and height.
     * @param WIDTH
     * @param HEIGHT
     * @return buffered image containing the background with the selected background design
     */
    protected BufferedImage create_BACKGROUND_Image(final int WIDTH, final int HEIGHT) {
        return create_BACKGROUND_Image(WIDTH, HEIGHT, null);
    }

    /**
     * Returns the background image with the currently active backgroundcolor
     * with the given width and height.
     * @param WIDTH
     * @param HEIGHT
     * @param image
     * @return buffered image containing the background with the selected background design
     */
    protected BufferedImage create_BACKGROUND_Image(final int WIDTH, final int HEIGHT, BufferedImage image) {
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        if (image == null) {
            image = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        }
        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        // Draw the background image
        BACKGROUND_FACTORY.createLinearBackground(WIDTH, HEIGHT, getBackgroundColor(), getModel().getCustomBackground(), getModel().getTextureColor(), image);

        // Draw the custom layer if selected
        if (isCustomLayerVisible()) {
            G2.drawImage(UTIL.getScaledInstance(getCustomLayer(), IMAGE_WIDTH, IMAGE_HEIGHT, RenderingHints.VALUE_INTERPOLATION_BICUBIC), 0, 0, null);
        }

        G2.dispose();

        return image;
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
    protected BufferedImage create_GLOW_Image(final int WIDTH, final int HEIGHT, final Color GLOW_COLOR, final boolean ON) {
        return GLOW_FACTORY.createLinearGlow(WIDTH, HEIGHT, GLOW_COLOR, ON);
    }

    /**
     * Returns the track image with the given values.
     * @param WIDTH
     * @param HEIGHT
     * @param MIN_VALUE
     * @param MAX_VALUE
     * @param TRACK_START
     * @param TRACK_SECTION
     * @param TRACK_STOP
     * @param TRACK_START_COLOR
     * @param TRACK_SECTION_COLOR
     * @param TRACK_STOP_COLOR
     * @return a buffered image of the track colored with the given values
     */
    protected BufferedImage create_TRACK_Image(final int WIDTH, final int HEIGHT, final double MIN_VALUE,
                                                              final double MAX_VALUE, final double TRACK_START,
                                                              final double TRACK_SECTION, final double TRACK_STOP,
                                                              final Color TRACK_START_COLOR,
                                                              final Color TRACK_SECTION_COLOR,
                                                              final Color TRACK_STOP_COLOR) {
        return create_TRACK_Image(WIDTH, HEIGHT, MIN_VALUE, MAX_VALUE, TRACK_START, TRACK_SECTION, TRACK_STOP, TRACK_START_COLOR, TRACK_SECTION_COLOR, TRACK_STOP_COLOR, null);
    }

    /**
     * Returns the track image with the given values.
     * @param WIDTH
     * @param HEIGHT
     * @param MIN_VALUE
     * @param MAX_VALUE
     * @param TRACK_START
     * @param TRACK_SECTION
     * @param TRACK_STOP
     * @param TRACK_START_COLOR
     * @param TRACK_SECTION_COLOR
     * @param TRACK_STOP_COLOR
     * @param image
     * @return a buffered image of the track colored with the given values
     */
    protected BufferedImage create_TRACK_Image(final int WIDTH, final int HEIGHT, final double MIN_VALUE,
                                                              final double MAX_VALUE, final double TRACK_START,
                                                              final double TRACK_SECTION, final double TRACK_STOP,
                                                              final Color TRACK_START_COLOR,
                                                              final Color TRACK_SECTION_COLOR,
                                                              final Color TRACK_STOP_COLOR,
                                                              BufferedImage image) {
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        if (TRACK_STOP > MAX_VALUE) {
            throw new IllegalArgumentException("Please adjust track start and/or track stop values");
        }

        if (image == null) {
            image = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        }
        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        final Rectangle2D TRACK;
        final Point2D TRACK_START_POINT;
        final Point2D TRACK_STOP_POINT;

        if (getOrientation() == Orientation.VERTICAL) {
            // Vertical orientation
            TRACK = new Rectangle2D.Double(IMAGE_WIDTH * 0.315, IMAGE_HEIGHT * 0.1276, IMAGE_WIDTH * 0.05, IMAGE_HEIGHT * 0.7280);
            TRACK_START_POINT = new Point2D.Double(0, TRACK.getMaxY());
            TRACK_STOP_POINT = new Point2D.Double(0, TRACK.getMinY());
        } else {
            // Horizontal orientation
            TRACK = new Rectangle2D.Double(IMAGE_WIDTH * 0.139, IMAGE_HEIGHT * 0.6285714285714286, IMAGE_WIDTH * 0.735, IMAGE_HEIGHT * 0.05);
            TRACK_START_POINT = new Point2D.Double(TRACK.getMinX(), 0);
            TRACK_STOP_POINT = new Point2D.Double(TRACK.getMaxX(), 0);
        }

        // Calculate the track start and stop position for the gradient
        final float TRACK_START_POSITION = (float) ((TRACK_START - MIN_VALUE) / (MAX_VALUE - MIN_VALUE));
        final float TRACK_STOP_POSITION = (float) ((TRACK_STOP - MIN_VALUE) / (MAX_VALUE - MIN_VALUE));

        final Color FULLY_TRANSPARENT = new Color(0.0f, 0.0f, 0.0f, 0.0f);

        final float[] TRACK_FRACTIONS;
        final Color[] TRACK_COLORS;

        // Three color gradient from trackStart over trackSection to trackStop
        final float TRACK_SECTION_POSITION = (float) ((TRACK_SECTION - MIN_VALUE) / (MAX_VALUE - MIN_VALUE));

        TRACK_FRACTIONS = new float[]{
            0.0f,
            TRACK_START_POSITION + 0.001f,
            TRACK_START_POSITION + 0.002f,
            TRACK_SECTION_POSITION,
            TRACK_STOP_POSITION - 0.002f,
            TRACK_STOP_POSITION - 0.001f,
            1.0f
        };

        TRACK_COLORS = new Color[]{
            FULLY_TRANSPARENT,
            FULLY_TRANSPARENT,
            TRACK_START_COLOR,
            TRACK_SECTION_COLOR,
            TRACK_STOP_COLOR,
            FULLY_TRANSPARENT,
            FULLY_TRANSPARENT
        };

        final LinearGradientPaint TRACK_GRADIENT = new LinearGradientPaint(TRACK_START_POINT, TRACK_STOP_POINT, TRACK_FRACTIONS, TRACK_COLORS);
        G2.setPaint(TRACK_GRADIENT);
        G2.fill(TRACK);

        G2.dispose();

        return image;
    }

    /**
     * Returns the image of the title.
     * @param WIDTH
     * @param HEIGHT
     * @param UNIT_STRING_VISIBLE
     * @return a buffered image of the title and unit string
     */
    protected BufferedImage create_TITLE_Image(final int WIDTH, final int HEIGHT,
                                                              final boolean UNIT_STRING_VISIBLE) {
        return create_TITLE_Image(WIDTH, HEIGHT, UNIT_STRING_VISIBLE, null);
    }

    /**
     * Returns the image of the title.
     * @param WIDTH
     * @param HEIGHT
     * @param UNIT_STRING_VISIBLE
     * @param image
     * @return a buffered image of the title and unit string
     */
    protected BufferedImage create_TITLE_Image(final int WIDTH, final int HEIGHT,
                                                              final boolean UNIT_STRING_VISIBLE,
                                                              BufferedImage image) {
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        if (image == null) {
            image = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        }
        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);

        if (isLabelColorFromThemeEnabled()) {
            G2.setColor(getBackgroundColor().LABEL_COLOR);
        } else {
            G2.setColor(getLabelColor());
        }

        final TextLayout LAYOUT_TITLE;

        if (getOrientation() == Orientation.VERTICAL) {
            // Vertical orientation
            // Draw title
            // Use custom font if selected
            if (isTitleAndUnitFontEnabled()) {
                G2.setFont(new Font(getTitleAndUnitFont().getFamily(), getTitleAndUnitFont().getStyle(), getTitleAndUnitFont().getSize()));
            } else {
                G2.setFont(new Font("Verdana", 0, (int) (0.1 * IMAGE_WIDTH)));
            }
            if (!getTitle().isEmpty()) {
                LAYOUT_TITLE = new TextLayout(getTitle(), G2.getFont(), RENDER_CONTEXT);
                final AffineTransform OLD_TRANSFORM = G2.getTransform();
                G2.translate(0.0, -0.05 * IMAGE_HEIGHT);
                G2.rotate(1.5707963267948966, 0.6714285714285714f * IMAGE_WIDTH, 0.1375f * IMAGE_HEIGHT + LAYOUT_TITLE.getAscent() - LAYOUT_TITLE.getDescent());
                G2.drawString(getTitle(), 0.6714285714285714f * IMAGE_WIDTH, 0.1375f * IMAGE_HEIGHT + LAYOUT_TITLE.getAscent() - LAYOUT_TITLE.getDescent());
                G2.setTransform(OLD_TRANSFORM);
            }
            // Draw unit string
            if (UNIT_STRING_VISIBLE && !getUnitString().isEmpty()) {
                if (isTitleAndUnitFontEnabled()) {
                    G2.setFont(new Font(getTitleAndUnitFont().getFamily(), 0, (int) (0.07142857142857142 * IMAGE_WIDTH)));
                } else {
                    G2.setFont(new Font("Verdana", 0, (int) (0.07142857142857142 * IMAGE_WIDTH)));
                }
                final TextLayout LAYOUT_UNIT = new TextLayout(getUnitString(), G2.getFont(), RENDER_CONTEXT);
                final Rectangle2D UNIT_BOUNDARY = LAYOUT_UNIT.getBounds();
                G2.drawString(getUnitString(), (float) (IMAGE_WIDTH - UNIT_BOUNDARY.getWidth()) / 2f, 0.8875f * IMAGE_HEIGHT + LAYOUT_UNIT.getAscent() - LAYOUT_UNIT.getDescent());
            }
        } else {
            // Horizontal orientation
            // Draw title
            if (isTitleAndUnitFontEnabled()) {
                G2.setFont(new Font(getTitleAndUnitFont().getFamily(), 0, (int) (0.1 * IMAGE_HEIGHT)));
            } else {
                G2.setFont(new Font("Verdana", 0, (int) (0.1 * IMAGE_HEIGHT)));
            }
            if (!getTitle().isEmpty()) {
                LAYOUT_TITLE = new TextLayout(getTitle(), G2.getFont(), RENDER_CONTEXT);
                G2.drawString(getTitle(), 0.15f * IMAGE_WIDTH, 0.25f * IMAGE_HEIGHT + LAYOUT_TITLE.getAscent() - LAYOUT_TITLE.getDescent());
            }
            // Draw unit string
            if (UNIT_STRING_VISIBLE && !getUnitString().isEmpty()) {
                if (isTitleAndUnitFontEnabled()) {
                    G2.setFont(new Font(getTitleAndUnitFont().getFamily(), 0, (int) (0.025 * IMAGE_WIDTH)));
                } else {
                    G2.setFont(new Font("Verdana", 0, (int) (0.025 * IMAGE_WIDTH)));
                }
                final TextLayout LAYOUT_UNIT = new TextLayout(getUnitString(), G2.getFont(), RENDER_CONTEXT);
                G2.drawString(getUnitString(), 0.0625f * IMAGE_WIDTH, 0.7f * IMAGE_HEIGHT + LAYOUT_UNIT.getAscent() - LAYOUT_UNIT.getDescent());
            }
        }

        G2.dispose();

        return image;
    }

    /**
     * Returns the image with the given lcd color.
     * @param WIDTH
     * @param HEIGHT
     * @param LCD_COLOR
     * @param CUSTOM_LCD_BACKGROUND
     * @return buffered image containing the lcd with the selected lcd color
     */
    protected BufferedImage create_LCD_Image(final int WIDTH, final int HEIGHT, final LcdColor LCD_COLOR, final Paint CUSTOM_LCD_BACKGROUND) {
        return create_LCD_Image(new Rectangle2D.Double(0, 0, WIDTH, HEIGHT), LCD_COLOR, CUSTOM_LCD_BACKGROUND, null);
    }

    /**
     * Returns the image with the given lcd color.
     * @param BOUNDS
     * @param LCD_COLOR
     * @param CUSTOM_LCD_BACKGROUND
     * @param IMAGE
     * @return buffered image containing the lcd with the selected lcd color
     */
    protected BufferedImage create_LCD_Image(final Rectangle2D BOUNDS, final LcdColor LCD_COLOR, final Paint CUSTOM_LCD_BACKGROUND, final BufferedImage IMAGE) {
        return LCD_FACTORY.create_LCD_Image(BOUNDS, LCD_COLOR, CUSTOM_LCD_BACKGROUND, IMAGE);
    }

    /**
     * Returns the image of the threshold indicator
     * @param WIDTH
     * @param HEIGHT
     * @return a buffered image of the threshold indicator
     */
    protected BufferedImage create_THRESHOLD_Image(final int WIDTH, final int HEIGHT) {
        if (WIDTH <= 14 || HEIGHT <= 14) // 14 is needed otherwise the image size could be smaller than 1
        {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        final int IMAGE_WIDTH;
        final int IMAGE_HEIGHT;
        if (getOrientation() == Orientation.VERTICAL) {
            // Vertical orientation
            IMAGE_WIDTH = (int) (WIDTH * 0.0714285714);
            IMAGE_HEIGHT = (int) (IMAGE_WIDTH * 0.8);
        } else {
            // Horizontal orientation
            IMAGE_HEIGHT = (int) (HEIGHT * 0.0714285714);
            IMAGE_WIDTH = (int) (IMAGE_HEIGHT * 0.8);
        }
        final BufferedImage IMAGE = UTIL.createImage(IMAGE_WIDTH, IMAGE_HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        G2.translate(0, IMAGE_WIDTH * 0.005);
        final GeneralPath THRESHOLD = new GeneralPath();
        THRESHOLD.setWindingRule(Path2D.WIND_EVEN_ODD);
        final Point2D THRESHOLD_START;
        final Point2D THRESHOLD_STOP;

        if (getOrientation() == Orientation.VERTICAL) {
            // Vertical orientation
            THRESHOLD.moveTo(IMAGE_WIDTH * 0.1, IMAGE_HEIGHT * 0.5);
            THRESHOLD.lineTo(IMAGE_WIDTH * 0.9, IMAGE_HEIGHT * 0.1);
            THRESHOLD.lineTo(IMAGE_WIDTH * 0.9, IMAGE_HEIGHT * 0.9);
            THRESHOLD.lineTo(IMAGE_WIDTH * 0.1, IMAGE_HEIGHT * 0.5);
            THRESHOLD.closePath();
            THRESHOLD_START = new Point2D.Double(THRESHOLD.getBounds2D().getMinX(), 0);
            THRESHOLD_STOP = new Point2D.Double(THRESHOLD.getBounds2D().getMaxX(), 0);
        } else {
            // Horizontal orientation
            THRESHOLD.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.9);
            THRESHOLD.lineTo(IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 0.1);
            THRESHOLD.lineTo(IMAGE_WIDTH * 0.1, IMAGE_HEIGHT * 0.1);
            THRESHOLD.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.9);
            THRESHOLD.closePath();
            THRESHOLD_START = new Point2D.Double(0, THRESHOLD.getBounds2D().getMaxY());
            THRESHOLD_STOP = new Point2D.Double(0, THRESHOLD.getBounds2D().getMinY());
        }
        final float[] THRESHOLD_FRACTIONS = {
            0.0f,
            0.3f,
            0.59f,
            1.0f
        };
        final Color[] THRESHOLD_COLORS = {
            getThresholdColor().DARK,
            getThresholdColor().MEDIUM,
            getThresholdColor().MEDIUM,
            getThresholdColor().DARK
        };
        final LinearGradientPaint THRESHOLD_GRADIENT = new LinearGradientPaint(THRESHOLD_START, THRESHOLD_STOP, THRESHOLD_FRACTIONS, THRESHOLD_COLORS);
        G2.setPaint(THRESHOLD_GRADIENT);
        G2.fill(THRESHOLD);

        G2.setColor(Color.WHITE);
        G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        G2.draw(THRESHOLD);

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns the image of the MinMeasuredValue and MaxMeasuredValue dependend
     * on the given color
     * @param WIDTH
     * @param HEIGHT
     * @param COLOR
     * @return a buffered image of either the min measured value or the max measured value indicator
     */
    protected BufferedImage create_MEASURED_VALUE_Image(final int WIDTH, final int HEIGHT, final Color COLOR) {
        if (WIDTH <= 20 || HEIGHT <= 20) // 20 is needed otherwise the image size could be smaller than 1
        {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        final int IMAGE_WIDTH;
        final int IMAGE_HEIGHT;
        if (getOrientation() == Orientation.VERTICAL) {
            // Vertical orientation
            IMAGE_WIDTH = (int) (WIDTH * 0.05);
            IMAGE_HEIGHT = IMAGE_WIDTH;
        } else {
            // Horizontal orientation
            IMAGE_HEIGHT = (int) (HEIGHT * 0.05);
            IMAGE_WIDTH = IMAGE_HEIGHT;
        }

        final BufferedImage IMAGE = UTIL.createImage(IMAGE_WIDTH, IMAGE_HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final GeneralPath INDICATOR = new GeneralPath();
        INDICATOR.setWindingRule(Path2D.WIND_EVEN_ODD);
        if (getOrientation() == Orientation.VERTICAL) {
            INDICATOR.moveTo(IMAGE_WIDTH, IMAGE_HEIGHT * 0.5);
            INDICATOR.lineTo(0.0, 0.0);
            INDICATOR.lineTo(0.0, IMAGE_HEIGHT);
            INDICATOR.closePath();
        } else {
            INDICATOR.moveTo(IMAGE_WIDTH * 0.5, 0.0);
            INDICATOR.lineTo(IMAGE_WIDTH, IMAGE_HEIGHT);
            INDICATOR.lineTo(0.0, IMAGE_HEIGHT);
            INDICATOR.closePath();
        }
        G2.setColor(COLOR);
        G2.fill(INDICATOR);

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns the image of the glasseffect image
     * @param WIDTH
     * @param HEIGHT
     * @return a buffered image of the foreground glass effect
     */
    protected BufferedImage create_FOREGROUND_Image(final int WIDTH, final int HEIGHT) {
        return FOREGROUND_FACTORY.createLinearForeground(WIDTH, HEIGHT);
    }

    /**
     * Returns the image that will be displayed if the gauge is disabled
     * @param WIDTH
     * @param HEIGHT
     * @return the disabled image that will be displayed if the gauge is disabled
     */
    protected BufferedImage create_DISABLED_Image(final int WIDTH, final int HEIGHT) {
        return DISABLED_FACTORY.createLinearDisabled(WIDTH, HEIGHT);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Size related methods">
    @Override
    public void calcInnerBounds() {
        calcInnerBounds(getWidth(), getHeight());
    }

    public void calcInnerBounds(final int WIDTH, final int HEIGHT) {
        final Insets INSETS = getInsets();
        if (!isFrameVisible()) {
            INNER_BOUNDS.setBounds(INSETS.left, INSETS.top, WIDTH - INSETS.left - INSETS.right + 34, HEIGHT - INSETS.top - INSETS.bottom + 34);
        } else {
            INNER_BOUNDS.setBounds(INSETS.left, INSETS.top, WIDTH - INSETS.left - INSETS.right, HEIGHT - INSETS.top - INSETS.bottom);
        }
    }

    /**
     * Returns the rectangle that is defined by the dimension of the component and
     * it's insets given by e.g. a border.
     * @return rectangle that defines the inner area available for painting
     */
    @Override
    public final Rectangle getInnerBounds() {
        return INNER_BOUNDS;
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension dim = super.getMinimumSize();
        if (dim.width <= dim.height) {
            if (dim.width < 40 || dim.height < 60) {
                dim = new Dimension(40, 60);
            }
        } else if(dim.width < 60 || dim.height < 60) {
            dim = new Dimension(60, 40);
        }
        return dim;
    }

    @Override
    public void setMinimumSize(final Dimension DIM) {
        int width;
        int height;
        if (getInnerBounds().width <= getInnerBounds().height) {
            // vertical
            width = DIM.width < 40 ? 40 : DIM.width;
            height = DIM.height < 60 ? 60 : DIM.height;
        } else {
            // horizontal
            width = DIM.width < 60 ? 60 : DIM.width;
            height = DIM.height < 40 ? 40 : DIM.height;
        }
        super.setMinimumSize(new Dimension(width, height));
        calcInnerBounds(DIM.width, DIM.height);
        init(getInnerBounds().width, getInnerBounds().height);
        setInitialized(true);
        invalidate();
        repaint();
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension dim = super.getMaximumSize();
        if (dim.width <= dim.height) {
            if (dim.width > 1080 || dim.height > 1920) {
                dim = new Dimension(1080, 1920);
            }
        } else if(dim.width > 1920 || dim.height > 1080) {
            dim = new Dimension(1920, 1080);
        }
        return dim;
    }

    @Override
    public void setMaximumSize(final Dimension DIM) {
        int width;
        int height;
        if (getInnerBounds().width <= getInnerBounds().height) {
            // vertical
            width = DIM.width > 1080 ? 1080 : DIM.width;
            height = DIM.height > 1920 ? 1920 : DIM.height;
        } else {
            // horizontal
            width = DIM.width > 1920 ? 1920 : DIM.width;
            height = DIM.height > 1080 ? 1080 : DIM.height;
        }
        super.setMaximumSize(new Dimension(width, height));
        calcInnerBounds(DIM.width, DIM.height);
        init(getInnerBounds().width, getInnerBounds().height);
        setInitialized(true);
        invalidate();
        repaint();
    }

    @Override
    public void setPreferredSize(final Dimension DIM) {
        super.setPreferredSize(DIM);
        calcInnerBounds(DIM.width, DIM.height);
        init(getInnerBounds().width, getInnerBounds().height);
        setInitialized(true);
        invalidate();
        repaint();
    }

    @Override
    public void setSize(final int WIDTH, final int HEIGHT) {
        super.setSize(WIDTH, HEIGHT);
        calcInnerBounds(WIDTH, HEIGHT);
        init(getInnerBounds().width, getInnerBounds().height);
        setInitialized(true);
    }

    @Override
    public void setSize(final Dimension DIM) {
        super.setSize(DIM);
        calcInnerBounds(DIM.width, DIM.height);
        init(getInnerBounds().width, getInnerBounds().height);
        setInitialized(true);
    }

    @Override
    public void setBounds(final Rectangle BOUNDS) {
        super.setBounds(BOUNDS);
        calcInnerBounds(BOUNDS.width, BOUNDS.height);
        init(getInnerBounds().width, getInnerBounds().height);
        setInitialized(true);
    }

    @Override
    public void setBounds(final int X, final int Y, final int WIDTH, final int HEIGHT) {
        super.setBounds(X, Y, WIDTH, HEIGHT);
        calcInnerBounds(WIDTH, HEIGHT);
        init(getInnerBounds().width, getInnerBounds().height);
        setInitialized(true);
    }

    @Override
    public void setBorder(Border BORDER) {
        super.setBorder(BORDER);
        calcInnerBounds();
        init(getInnerBounds().width, getInnerBounds().height);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ComponentListener methods">
    // ComponentListener methods
    @Override
    public void componentResized(final ComponentEvent EVENT) {
        final Container PARENT = getParent();
        if ((PARENT != null) && (PARENT.getLayout() == null)) {
            setSize(getWidth(), getHeight());
        } else {
            //setSize(new Dimension(getWidth(), getHeight()));
            setPreferredSize(new Dimension(getWidth(), getHeight()));
        }

        calcInnerBounds();

        if (getWidth() >= getHeight()) {
            // Horizontal
            setOrientation(Orientation.HORIZONTAL);
            recreateLedImages(getInnerBounds().height);
            recreateUserLedImages(getInnerBounds().height);

            if (isLedOn()) {
                setCurrentLedImage(getLedImageOn());
            } else {
                setCurrentLedImage(getLedImageOff());
            }
            setLedPosition((getInnerBounds().width - 20.0 - 16.0) / getInnerBounds().width, 0.453271028);

            if (isUserLedOn()) {
                setCurrentUserLedImage(getUserLedImageOn());
            } else {
                setCurrentUserLedImage(getUserLedImageOff());
            }
            setUserLedPosition(18.0 / getInnerBounds().width, 0.453271028);
        } else {
            // Vertical
            setOrientation(Orientation.VERTICAL);
            recreateLedImages(getInnerBounds().width);
            recreateUserLedImages(getInnerBounds().width);

            if (isLedOn()) {
                setCurrentLedImage(getLedImageOn());
            } else {
                setCurrentLedImage(getLedImageOff());
            }
            setLedPosition(0.453271028, (20.0 / getInnerBounds().height));

            if (isUserLedOn()) {
                setCurrentUserLedImage(getUserLedImageOn());
            } else {
                setCurrentUserLedImage(getUserLedImageOff());
            }
            setUserLedPosition((getInnerBounds().width - 18.0 - 16.0) / getInnerBounds().width, 0.453271028);
        }
        getModel().setSize(getLocation().x, getLocation().y, getWidth(), getHeight());
        init(getInnerBounds().width, getInnerBounds().height);
        revalidate();
        //repaint();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ActionListener method">
    @Override
    public void actionPerformed(final ActionEvent EVENT) {
        super.actionPerformed(EVENT);

        if (EVENT.getSource().equals(LCD_BLINKING_TIMER)) {
            lcdTextVisible ^= true;
            repaint(getInnerBounds());
        }
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "AbstractLinear";
    }
}
