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
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.Border;

import org.mars_sim.msp.ui.steelseries.tools.ColorDef;
import org.mars_sim.msp.ui.steelseries.tools.ConicalGradientPaint;
import org.mars_sim.msp.ui.steelseries.tools.CustomColorDef;
import org.mars_sim.msp.ui.steelseries.tools.Direction;
import org.mars_sim.msp.ui.steelseries.tools.ForegroundType;
import org.mars_sim.msp.ui.steelseries.tools.FrameType;
import org.mars_sim.msp.ui.steelseries.tools.GaugeType;
import org.mars_sim.msp.ui.steelseries.tools.KnobStyle;
import org.mars_sim.msp.ui.steelseries.tools.KnobType;
import org.mars_sim.msp.ui.steelseries.tools.LcdColor;
import org.mars_sim.msp.ui.steelseries.tools.NumberSystem;
import org.mars_sim.msp.ui.steelseries.tools.Orientation;
import org.mars_sim.msp.ui.steelseries.tools.PointerType;
import org.mars_sim.msp.ui.steelseries.tools.PostPosition;
import org.mars_sim.msp.ui.steelseries.tools.TicklabelOrientation;
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
public abstract class AbstractRadial extends AbstractGauge implements Lcd {
    // <editor-fold defaultstate="collapsed" desc="Variable declarations">

    protected static final float ANGLE_CONST = 1f / 360f;
    private final Rectangle INNER_BOUNDS;
    private final Rectangle GAUGE_BOUNDS;
    private final Rectangle FRAMELESS_BOUNDS;
    private final Point2D FRAMELESS_OFFSET;
    // Sections related
    private boolean transparentSectionsEnabled;
    private boolean transparentAreasEnabled;
    private boolean expandedSectionsEnabled;
    // Frame type related
    private Direction tickmarkDirection;
    // LED related variables
    private Point2D ledPosition;
    // User LED related variables
    private Point2D userLedPosition;
    // LCD related variables
    private String lcdUnitString;
    private double lcdValue;
    private String lcdInfoString = "";
    private Timeline lcdTimeline;
    private boolean lcdTextVisible;
    private Timer LCD_BLINKING_TIMER;
    // Animation related variables
    private Timeline timeline;
    private final TimelineEase STANDARD_EASING;
    private final TimelineEase RETURN_TO_ZERO_EASING;
    private TimelineCallback timelineCallback;
    // Alignment related
    private int horizontalAlignment;
    private int verticalAlignment;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public AbstractRadial() {
        super();
        lcdTimeline = new Timeline(this);
        lcdValue = 0;
        lcdUnitString = getUnitString();
        ledPosition = new Point2D.Double(0.6, 0.4);
        userLedPosition = new Point2D.Double(0.3, 0.4);
        INNER_BOUNDS = new Rectangle(200, 200);
        GAUGE_BOUNDS = new Rectangle(200, 200);
        FRAMELESS_BOUNDS = new Rectangle(200, 200);
        FRAMELESS_OFFSET = new Point2D.Double(0, 0);
        transparentSectionsEnabled = false;
        transparentAreasEnabled = false;
        expandedSectionsEnabled = false;
        tickmarkDirection = Direction.CLOCKWISE;
        timeline = new Timeline(this);
        STANDARD_EASING = new Spline(0.5f);
        RETURN_TO_ZERO_EASING = new Sine();
		horizontalAlignment = SwingConstants.CENTER;
		verticalAlignment = SwingConstants.CENTER;
        lcdTextVisible = true;
        LCD_BLINKING_TIMER = new Timer(500, this);
        addComponentListener(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    /**
     * Returns the enum that defines the type of the gauge
     * FG_TYPE1    a quarter gauge (90 deg)
     * FG_TYPE2    a two quarter gauge (180 deg)
     * FG_TYPE3    a three quarter gauge (270 deg)
     * TYPE4    a four quarter gauge (300 deg)
     * @return the type of the gauge (90, 180, 270 or 300 deg)
     */
    public GaugeType getGaugeType() {
        return getModel().getGaugeType();
    }

    /**
     * Sets the type of the gauge
     * FG_TYPE1    a quarter gauge (90 deg)
     * FG_TYPE2    a two quarter gauge (180 deg)
     * FG_TYPE3    a three quarter gauge (270 deg)
     * TYPE4    a four quarter gauge (300 deg)
     * @param GAUGE_TYPE
     */
    public void setGaugeType(final GaugeType GAUGE_TYPE) {
        getModel().setGaugeType(GAUGE_TYPE);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns the type of frame that is used for the radial gauge.
     * It could be round our square.
     * @return the type of frame that will be used for the radial gauge.
     */
    public FrameType getFrameType() {
        return getModel().getFrameType();
    }

    /**
     * Defines the type of frame that will be used for the radial gauge.
     * It could be round our square.
     * @param FRAME_TYPE
     */
    public void setFrameType(final FrameType FRAME_TYPE) {
        getModel().setFrameType(FRAME_TYPE);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns the type of foreground that is used for the radial gauge.
     * There are three types available.
     * @return the type of foreground that is will be used for the radial gauge
     */
    public ForegroundType getForegroundType() {
        return getModel().getForegroundType();
    }

    /**
     * Defines the type of foreground that will be used for the radial gauge.
     * There area three types available.
     * @param FOREGROUND_TYPE
     */
    public void setForegroundType(final ForegroundType FOREGROUND_TYPE) {
        getModel().setForegroundType(FOREGROUND_TYPE);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

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
//                    }
//                });

                AUTOZERO_SCENARIO.play();
            }
        }
    }

    /**
     * Returns the step between the tickmarks
     * @return returns double value that represents the stepsize between the tickmarks
     */
    public double getAngleStep() {
        return getModel().getAngleStep();
    }

    /**
     * Returns the step between the tickmarks for log scaling
     * @return returns double value that represents the stepsize between the tickmarks for log scaling
     */
    public double getLogAngleStep() {
        return getModel().getLogAngleStep();
    }

    /**
     * Returns the angle area where no tickmarks will be drawn
     * @return the angle area where no tickmarks will be drawn
     */
    public double getFreeAreaAngle() {
        return getModel().getFreeAreaAngle();
    }

    public double getRotationOffset() {
        return getModel().getRotationOffset();
    }

    public double getTickmarkOffset() {
        return getModel().getTickmarkOffset();
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

    /**
     * Returns the direction of the tickmark labels.
     * CLOCKWISE is the standard and counts the labels like on a analog clock
     * COUNTER_CLOCKWISE could be useful for gauges like Radial1Square in SOUTH_EAST orientation
     * @return the direction of the tickmark counting
     */
    public Direction getTickmarkDirection() {
        return tickmarkDirection;
    }

    /**
     * Sets the direction of the tickmark label counting.
     * CLOCKWISE will count in clockwise direction
     * COUNTER_CLOCKWISE will count the opposite way
     * @param DIRECTION
     */
    public void setTickmarkDirection(final Direction DIRECTION) {
        this.tickmarkDirection = DIRECTION;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns the type of the pointer
     * FG_TYPE1 (standard version) or FG_TYPE2
     * @return the type of the pointer
     */
    public PointerType getPointerType() {
        return getModel().getPointerType();
    }

    /**
     * Sets the type of the pointer
     * @param POINTER_TYPE type of the pointer
     *     PointerType.TYPE1 (default)
     *     PointerType.TYPE2
     *     PointerType.TYPE3
     *     PointerType.TYPE4
     */
    public void setPointerType(final PointerType POINTER_TYPE) {
        getModel().setPointerType(POINTER_TYPE);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns the color of the pointer
     * @return the selected color of the pointer
     */
    public ColorDef getPointerColor() {
        return getModel().getPointerColor();
    }

    /**
     * Sets the color of the pointer
     * @param POINTER_COLOR
     */
    public void setPointerColor(final ColorDef POINTER_COLOR) {
        getModel().setPointerColor(POINTER_COLOR);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns true if the pointer shadow is visible
     * @return true if the pointer shadow is visible
     */
    public boolean isPointerShadowVisible() {
        return getModel().isPointerShadowVisible();
    }

    /**
     * Enables/disables the pointer shadow
     * @param POINTER_SHADOW_VISIBLE
     */
    public void setPointerShadowVisible(final boolean POINTER_SHADOW_VISIBLE) {
        getModel().setPointerShadowVisible(POINTER_SHADOW_VISIBLE);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns the color from which the custom pointer color will be calculated
     * @return the color from which the custom pointer color will be calculated
     */
    public Color getCustomPointerColor() {
        return getModel().getCustomPointerColor();
    }

    /**
     * Sets the color from which the custom pointer color is calculated
     * @param COLOR
     */
    public void setCustomPointerColor(final Color COLOR) {
        getModel().setCustomPointerColorObject(new CustomColorDef(COLOR));
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns the object that represents the custom pointer color
     * @return the object that represents the custom pointer color
     */
    public CustomColorDef getCustomPointerColorObject() {
        return getModel().getCustomPointerColorObject();
    }

    /**
     * Returns the type of the knob
     * @return the type of the knob
     */
    public KnobType getKnobType() {
        return getModel().getKnobType();
    }

    /**
     * Sets the type of the knob
     * @param KNOB_TYPE
     */
    public void setKnobType(final KnobType KNOB_TYPE) {
        getModel().setKnobType(KNOB_TYPE);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns the style of the center knob of a radial gauge
     * @return the style of the center knob of a radial gauge
     */
    public KnobStyle getKnobStyle() {
        return getModel().getKnobStyle();
    }

    /**
     * Sets the the style of the center knob of a radial gauge
     * @param KNOB_STYLE
     */
    public void setKnobStyle(final KnobStyle KNOB_STYLE) {
        getModel().setKnobStyle(KNOB_STYLE);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
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

    /**
     * Returns true if the arc that represents the range of measured values is visible
     * @return true if the arc that represents the range of measured values is visible
     */
    public boolean isRangeOfMeasuredValuesVisible() {
        return getModel().isRangeOfMeasuredValuesVisible();
    }

    /**
     * Enables / disables the visibility of the arc that represents the range of measured values
     * @param RANGE_OF_MEASURED_VALUES_VISIBLE
     */
    public void setRangeOfMeasuredValuesVisible(final boolean RANGE_OF_MEASURED_VALUES_VISIBLE) {
        getModel().setRangeOfMeasuredValuesVisible(RANGE_OF_MEASURED_VALUES_VISIBLE);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
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
        return getModel().getLcdValue();
    }

    @Override
    public void setLcdValue(final double LCD_VALUE) {
        if (getLcdNumberSystem() != NumberSystem.DEC) {
            if (LCD_VALUE < 0) {
                setLcdNumberSystem(NumberSystem.DEC);
            }
        }

        if (!isLogScale()) {
            lcdValue = LCD_VALUE;
        } else {
            if (LCD_VALUE > 1) {
                lcdValue = LCD_VALUE;
            } else {
                lcdValue = 1;
            }
        }
        getModel().setLcdValue(lcdValue);
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
    public void setLcdColor(final LcdColor LCD_COLOR) {
        getModel().setLcdColor(LCD_COLOR);
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
    public Paint createCustomLcdBackgroundPaint(final Color[] LCD_COLORS) {
        return null;
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
     * Returns true if the posts of the radial gauges are visible
     * @return true if the posts of the radial gauges are visible
     */
    public boolean getPostsVisible() {
        return getModel().getPostsVisible();
    }

    /**
     * Enables/disables the visibility of the posts of the radial gauges
     * @param POSTS_VISIBLE
     */
    public void setPostsVisible(final boolean POSTS_VISIBLE) {
        getModel().setPostsVisible(POSTS_VISIBLE);
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns the orientation of the tickmark labels
     * @return the orientation of the tickmark labels
     */
    public TicklabelOrientation getTicklabelOrientation() {
        return getModel().getTicklabelOrientation();
    }

    /**
     * Sets the orientation of the tickmark labels
     * @param TICKLABEL_ORIENTATION
     */
    public void setTicklabelOrientation(final TicklabelOrientation TICKLABEL_ORIENTATION) {
        getModel().setTicklabelOrienatation(TICKLABEL_ORIENTATION);
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

    /**
     * Returns true if the sections are wider
     * @return true if the sections are wider
     */
    public boolean isExpandedSectionsEnabled() {
        return expandedSectionsEnabled;
    }

    /**
     * Enables / disables the use of wider sections
     * @param EXPANDED_SECTIONS_ENABLED
     */
    public void setExpandedSectionsEnabled(final boolean EXPANDED_SECTIONS_ENABLED) {
        expandedSectionsEnabled = EXPANDED_SECTIONS_ENABLED;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image related">
    /**
     * Returns a radial gradient paint that will be used as overlay for the track or section image
     * to achieve some kind of a 3d effect.
     * @param WIDTH
     * @param RADIUS_FACTOR : 0.38f for the standard radial gauge
     * @return a radial gradient paint that will be used as overlay for the track or section image
     */
    protected RadialGradientPaint createSection3DEffectGradient(final int WIDTH, final float RADIUS_FACTOR) {
        final float[] FRACTIONS;
        final Color[] COLORS;
        if (isExpandedSectionsEnabled()) {
           FRACTIONS = new float[]{
                0.0f,
                0.7f,
                0.75f,
                0.96f,
                1.0f
            };

            COLORS = new Color[]{
                new Color(0.0f, 0.0f, 0.0f, 1.0f),
                new Color(0.9f, 0.9f, 0.9f, 0.2f),
                new Color(1.0f, 1.0f, 1.0f, 0.5f),
                new Color(0.1843137255f, 0.1843137255f, 0.1843137255f, 0.3f),
                new Color(0.0f, 0.0f, 0.0f, 0.2f)
            };
        } else {
           FRACTIONS = new float[]{
                0.0f,
                0.89f,
                0.955f,
                1.0f
            };

            COLORS = new Color[]{
                new Color(0.0f, 0.0f, 0.0f, 0.0f),
                new Color(0.0f, 0.0f, 0.0f, 0.3f),
                new Color(1.0f, 1.0f, 1.0f, 0.6f),
                new Color(0.0f, 0.0f, 0.0f, 0.4f)
            };
        }



        final Point2D GRADIENT_CENTER = new Point2D.Double(WIDTH / 2.0, WIDTH / 2.0);

        return new RadialGradientPaint(GRADIENT_CENTER, WIDTH * RADIUS_FACTOR, FRACTIONS, COLORS);
    }

    /**
     * Returns a radial gradient paint that will be used as overlay for the track or area image
     * to achieve some kind of a 3d effect.
     * @param WIDTH
     * @param RADIUS_FACTOR
     * @return a radial gradient paint that will be used as overlay for the track or area image
     */
    protected RadialGradientPaint createArea3DEffectGradient(final int WIDTH, final float RADIUS_FACTOR) {
        final float[] FRACTIONS;
        final Color[] COLORS;

        FRACTIONS = new float[]{
            0.0f,
            0.6f,
            1.0f
        };
        COLORS = new Color[]{
            new Color(1.0f, 1.0f, 1.0f, 0.75f),
            new Color(1.0f, 1.0f, 1.0f, 0.0f),
            new Color(0.0f, 0.0f, 0.0f, 0.3f)
        };
        final Point2D GRADIENT_CENTER = new Point2D.Double(WIDTH / 2.0, WIDTH / 2.0);

        return new RadialGradientPaint(GRADIENT_CENTER, WIDTH * RADIUS_FACTOR, FRACTIONS, COLORS);
    }

    /**
     * Returns the frame image with the currently active framedesign
     * with the given width and the current frame type.
     * @param WIDTH
     * @return buffered image containing the frame in the active frame design
     */
    protected BufferedImage create_FRAME_Image(final int WIDTH) {
        switch (getFrameType()) {
            case ROUND:
                return FRAME_FACTORY.createRadialFrame(WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameBaseColor(), isFrameBaseColorEnabled(), getFrameEffect());
            case SQUARE:
                return FRAME_FACTORY.createLinearFrame(WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameBaseColor(), isFrameBaseColorEnabled(), getFrameEffect());
            default:
                return FRAME_FACTORY.createRadialFrame(WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameBaseColor(), isFrameBaseColorEnabled(), getFrameEffect());
        }
    }

    /**
     * Returns the background image with the currently active backgroundcolor
     * with the given width without a title and a unit string.
     * @param WIDTH
     * @return the background image that is used
     */
    protected BufferedImage create_BACKGROUND_Image(final int WIDTH) {
        return create_BACKGROUND_Image(WIDTH, "", "");
    }

    /**
     * Returns the background image with the currently active backgroundcolor
     * with the given width, title and unitstring.
     * @param WIDTH
     * @param TITLE
     * @param UNIT_STRING
     * @return buffered image containing the background with the selected background design
     */
    protected BufferedImage create_BACKGROUND_Image(final int WIDTH, final String TITLE, final String UNIT_STRING) {
        return create_BACKGROUND_Image(WIDTH, TITLE, UNIT_STRING, null);
    }

    /**
     * Returns the background image with the currently active backgroundcolor
     * with the given width, title and unitstring.
     * @param WIDTH
     * @param TITLE
     * @param UNIT_STRING
     * @param image
     * @return buffered image containing the background with the selected background design
     */
    protected BufferedImage create_BACKGROUND_Image(final int WIDTH, final String TITLE, final String UNIT_STRING, BufferedImage image) {
        if (WIDTH <= 0) {
            return null;
        }

        if (image == null) {
            image = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        }

        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        switch (getFrameType()) {
            case SQUARE:
                BACKGROUND_FACTORY.createLinearBackground(WIDTH, WIDTH, getBackgroundColor(), getModel().getCustomBackground(), getModel().getTextureColor(), image);
                break;
            case ROUND:

            default:
                BACKGROUND_FACTORY.createRadialBackground(WIDTH, getBackgroundColor(), getModel().getCustomBackground(), getModel().getTextureColor(), image);
                break;
        }

        // Draw the custom layer if selected
        if (isCustomLayerVisible()) {
            G2.drawImage(UTIL.getScaledInstance(getCustomLayer(), IMAGE_WIDTH, IMAGE_HEIGHT, RenderingHints.VALUE_INTERPOLATION_BICUBIC), 0, 0, null);
        }

        final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);

        if (!TITLE.isEmpty()) {
            // Use custom label color if selected
            if (isLabelColorFromThemeEnabled()) {
                G2.setColor(getModel().getBackgroundColor().LABEL_COLOR);
            } else {
                G2.setColor(getModel().getLabelColor());
            }

            // Use custom font if selected
            if (isTitleAndUnitFontEnabled()) {
                G2.setFont(new Font(getTitleAndUnitFont().getFamily(), 0, (int) (0.04672897196261682 * IMAGE_WIDTH)));
            } else {
                G2.setFont(new Font("Verdana", 0, (int) (0.04672897196261682 * IMAGE_WIDTH)));
            }
            final TextLayout TITLE_LAYOUT = new TextLayout(TITLE, G2.getFont(), RENDER_CONTEXT);
            final Rectangle2D TITLE_BOUNDARY = TITLE_LAYOUT.getBounds();
            G2.drawString(TITLE, (float) ((IMAGE_WIDTH - TITLE_BOUNDARY.getWidth()) / 2.0), 0.3f * IMAGE_HEIGHT + TITLE_LAYOUT.getAscent() - TITLE_LAYOUT.getDescent());
        }

        if (!UNIT_STRING.isEmpty()) {
            // Use custom label color if selected
            if (isLabelColorFromThemeEnabled()) {
                G2.setColor(getModel().getBackgroundColor().LABEL_COLOR);
            } else {
                G2.setColor(getModel().getLabelColor());
            }

            // Use custom font if selected
            if (isTitleAndUnitFontEnabled()) {
                G2.setFont(new Font(getTitleAndUnitFont().getFamily(), 0, (int) (0.04672897196261682 * IMAGE_WIDTH)));
            } else {
                G2.setFont(new Font("Verdana", 0, (int) (0.04672897196261682 * IMAGE_WIDTH)));
            }
            final TextLayout UNIT_LAYOUT = new TextLayout(UNIT_STRING, G2.getFont(), RENDER_CONTEXT);
            final Rectangle2D UNIT_BOUNDARY = UNIT_LAYOUT.getBounds();
            G2.drawString(UNIT_STRING, (float) ((IMAGE_WIDTH - UNIT_BOUNDARY.getWidth()) / 2.0), 0.38f * IMAGE_HEIGHT + UNIT_LAYOUT.getAscent() - UNIT_LAYOUT.getDescent());
        }

        G2.dispose();

        return image;
    }

    /**
     * Returns an image that simulates a glowing ring which could be used to visualize
     * a state of the gauge by a color. The LED might be too small if you are not in front
     * of the screen and so one could see the current state more easy.
     * @param WIDTH
     * @param GLOW_COLOR
     * @param ON
     * @param GAUGE_TYPE
     * @param KNOBS
     * @param ORIENTATION
     * @return an image that simulates a glowing ring
     */
    protected BufferedImage create_GLOW_Image(final int WIDTH, final Color GLOW_COLOR, final boolean ON, final GaugeType GAUGE_TYPE, final boolean KNOBS, final Orientation ORIENTATION) {
        switch (getFrameType()) {
            case ROUND:
                return GLOW_FACTORY.createRadialGlow(WIDTH, GLOW_COLOR, ON, GAUGE_TYPE, KNOBS, ORIENTATION);
            case SQUARE:
                return GLOW_FACTORY.createLinearGlow(WIDTH, WIDTH, GLOW_COLOR, ON);
            default:
                return GLOW_FACTORY.createRadialGlow(WIDTH, GLOW_COLOR, ON, GAUGE_TYPE, KNOBS, ORIENTATION);
        }
    }

    /**
     * Returns the image with the given title and unitstring.
     * @param WIDTH
     * @param TITLE
     * @param UNIT_STRING
     * @return the image with the given title and unitstring.
     */
    protected BufferedImage create_TITLE_Image(final int WIDTH, final String TITLE, final String UNIT_STRING) {
        return create_TITLE_Image(WIDTH, TITLE, UNIT_STRING, null);
    }

    /**
     * Returns the image with the given title and unitstring.
     * @param WIDTH
     * @param TITLE
     * @param UNIT_STRING
     * @param image
     * @return the image with the given title and unitstring.
     */
    protected BufferedImage create_TITLE_Image(final int WIDTH, final String TITLE, final String UNIT_STRING, BufferedImage image) {
        if (WIDTH <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        if (image == null) {
            image = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        }

        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);

        if (!TITLE.isEmpty()) {
            // Use custom label color if selected
            if (isLabelColorFromThemeEnabled()) {
                G2.setColor(getBackgroundColor().LABEL_COLOR);
            } else {
                G2.setColor(getLabelColor());
            }

            // Use custom font if selected
            if (isTitleAndUnitFontEnabled()) {
                G2.setFont(new Font(getTitleAndUnitFont().getFamily(), getTitleAndUnitFont().getStyle(), getTitleAndUnitFont().getSize()));
            } else {
                G2.setFont(new Font("Verdana", 0, (int) (0.04672897196261682 * IMAGE_WIDTH)));
            }
            final TextLayout TITLE_LAYOUT = new TextLayout(TITLE, G2.getFont(), RENDER_CONTEXT);
            final Rectangle2D TITLE_BOUNDARY = TITLE_LAYOUT.getBounds();
            G2.drawString(TITLE, (float) ((IMAGE_WIDTH - TITLE_BOUNDARY.getWidth()) / 2.0), 0.3f * IMAGE_HEIGHT + TITLE_LAYOUT.getAscent() - TITLE_LAYOUT.getDescent());
        }

        if (!UNIT_STRING.isEmpty()) {
            // Use custom label color if selected
            if (isLabelColorFromThemeEnabled()) {
                G2.setColor(getBackgroundColor().LABEL_COLOR);
            } else {
                G2.setColor(getLabelColor());
            }

            // Use custom font if selected
            if (isTitleAndUnitFontEnabled()) {
                G2.setFont(new Font(getTitleAndUnitFont().getFamily(), getTitleAndUnitFont().getStyle(), getTitleAndUnitFont().getSize()));
            } else {
                G2.setFont(new Font("Verdana", 0, (int) (0.04672897196261682 * IMAGE_WIDTH)));
            }
            final TextLayout UNIT_LAYOUT = new TextLayout(UNIT_STRING, G2.getFont(), RENDER_CONTEXT);
            final Rectangle2D UNIT_BOUNDARY = UNIT_LAYOUT.getBounds();
            G2.drawString(UNIT_STRING, (float) ((IMAGE_WIDTH - UNIT_BOUNDARY.getWidth()) / 2.0), 0.37f * IMAGE_HEIGHT + UNIT_LAYOUT.getAscent() - UNIT_LAYOUT.getDescent());
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
        return createLcdImage(new Rectangle2D.Double(0, 0, WIDTH, HEIGHT), LCD_COLOR, CUSTOM_LCD_BACKGROUND, null);
    }

    /**
     * Returns the image with the given lcd color.
     * @param BOUNDS
     * @param LCD_COLOR
     * @param CUSTOM_LCD_BACKGROUND
     * @param IMAGE
     * @return buffered image containing the lcd with the selected lcd color
     */
    protected BufferedImage createLcdImage(final Rectangle2D BOUNDS, final LcdColor LCD_COLOR, final Paint CUSTOM_LCD_BACKGROUND, final BufferedImage IMAGE) {
        return LCD_FACTORY.create_LCD_Image(BOUNDS, LCD_COLOR, CUSTOM_LCD_BACKGROUND, IMAGE);
    }

    /**
     * Returns the track image with a given values. The gradient will be created by drawing
     * a series of lines and varies the color of the lines. The linewidth will be calculated
     * from the size of the component and from the gaugeType.
     * @param WIDTH
     * @param FREE_AREA_ANGLE
     * @param ROTATION_OFFSET
     * @param MIN_VALUE
     * @param MAX_VALUE
     * @param ANGLE_STEP
     * @param TRACK_START
     * @param TRACK_SECTION
     * @param TRACK_STOP
     * @param TRACK_START_COLOR
     * @param TRACK_SECTION_COLOR
     * @param TRACK_STOP_COLOR
     * @param RADIUS_FACTOR
     * @param CENTER
     * @param DIRECTION
     * @param OFFSET
     * @return a buffered image that represents the image of the track
     */
    protected BufferedImage create_TRACK_Image(final int WIDTH, final double FREE_AREA_ANGLE,
                                                              final double ROTATION_OFFSET, final double MIN_VALUE,
                                                              final double MAX_VALUE, final double ANGLE_STEP,
                                                              final double TRACK_START, final double TRACK_SECTION,
                                                              final double TRACK_STOP,
                                                              final Color TRACK_START_COLOR,
                                                              final Color TRACK_SECTION_COLOR,
                                                              final Color TRACK_STOP_COLOR,
                                                              final float RADIUS_FACTOR,
                                                              final Point2D CENTER,
                                                              final Direction DIRECTION,
                                                              final Point2D OFFSET) {
        return create_TRACK_Image(WIDTH, FREE_AREA_ANGLE, ROTATION_OFFSET, MIN_VALUE, MAX_VALUE, ANGLE_STEP, TRACK_START, TRACK_SECTION, TRACK_STOP, TRACK_START_COLOR, TRACK_SECTION_COLOR, TRACK_STOP_COLOR, RADIUS_FACTOR, CENTER, DIRECTION, OFFSET, null);
    }

    /**
     * Returns the track image with a given values. The gradient will be created by drawing
     * a series of lines and varies the color of the lines. The linewidth will be calculated
     * from the size of the component and from the gaugeType.
     * @param WIDTH
     * @param FREE_AREA_ANGLE
     * @param ROTATION_OFFSET
     * @param MIN_VALUE
     * @param MAX_VALUE
     * @param ANGLE_STEP
     * @param TRACK_START
     * @param TRACK_SECTION
     * @param TRACK_STOP
     * @param TRACK_START_COLOR
     * @param TRACK_SECTION_COLOR
     * @param TRACK_STOP_COLOR
     * @param RADIUS_FACTOR
     * @param CENTER
     * @param DIRECTION
     * @param OFFSET
     * @param image
     * @return a buffered image that represents the image of the track
     */
    protected BufferedImage create_TRACK_Image(final int WIDTH, final double FREE_AREA_ANGLE,
                                                              final double ROTATION_OFFSET, final double MIN_VALUE,
                                                              final double MAX_VALUE, final double ANGLE_STEP,
                                                              final double TRACK_START, final double TRACK_SECTION,
                                                              final double TRACK_STOP,
                                                              final Color TRACK_START_COLOR,
                                                              final Color TRACK_SECTION_COLOR,
                                                              final Color TRACK_STOP_COLOR,
                                                              final float RADIUS_FACTOR,
                                                              final Point2D CENTER,
                                                              final Direction DIRECTION,
                                                              final Point2D OFFSET,
                                                              BufferedImage image) {
        if (WIDTH <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        if (image == null) {
            image = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        }
        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        final int IMAGE_WIDTH = image.getWidth();
        //final int IMAGE_HEIGHT = IMAGE.getHeight();

        if (OFFSET != null) {
            G2.translate(OFFSET.getX(), OFFSET.getY());
        }

        // Definitions
        float lineWidth;
        switch (getGaugeType()) {
            case TYPE1:
                lineWidth = (float) Math.toDegrees(Math.PI / 2.0 - FREE_AREA_ANGLE) * 0.00167f * WIDTH * 0.0067f;
                break;

            case TYPE2:
                lineWidth = (float) Math.toDegrees(Math.PI - FREE_AREA_ANGLE) * 0.00167f * WIDTH * 0.0067f;
                break;

            case TYPE3:
                lineWidth = (float) Math.toDegrees(1.5 * Math.PI - FREE_AREA_ANGLE) * 0.00167f * WIDTH * 0.0067f;
                break;

            case TYPE4:
                lineWidth = (float) Math.toDegrees(2.0 * Math.PI - FREE_AREA_ANGLE) * 0.00167f * WIDTH * 0.0067f;
                break;

            default:
                lineWidth = (float) Math.toDegrees(2.0 * Math.PI - FREE_AREA_ANGLE) * 0.00167f * WIDTH * 0.0067f;
                break;

        }
        if (lineWidth < 0.25f) {
            lineWidth = 0.25f;
        }
        final BasicStroke STD_STROKE = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);

        final int TRACK_WIDTH = (int) (0.035 * WIDTH);

        final float RADIUS = IMAGE_WIDTH * RADIUS_FACTOR;

        // Define the lines
        final Point2D INNER_POINT = new Point2D.Double();
        final Point2D OUTER_POINT = new Point2D.Double();
        final Line2D TICK = new Line2D.Double();

        double sinValue;
        double cosValue;

        // Calculate the start, section and stop values dependend of the ticklabel direction
        final double TRACK_START_ANGLE;
        final double TRACK_SECTION_ANGLE;
        final double TRACK_STOP_ANGLE;

        final double ALPHA_START;
        final double ALPHA_SECTION;
        final double ALPHA_STOP;

        Color currentColor;

        switch (DIRECTION) {
            case COUNTER_CLOCKWISE:
                TRACK_START_ANGLE = ((MAX_VALUE - TRACK_STOP) * ANGLE_STEP);
                TRACK_SECTION_ANGLE = ((MAX_VALUE - TRACK_SECTION) * ANGLE_STEP);
                TRACK_STOP_ANGLE = ((MAX_VALUE - TRACK_START) * ANGLE_STEP);

                ALPHA_START = -ROTATION_OFFSET - (FREE_AREA_ANGLE / 2.0) - TRACK_START_ANGLE;
                ALPHA_SECTION = -ROTATION_OFFSET - (FREE_AREA_ANGLE / 2.0) - TRACK_SECTION_ANGLE;
                ALPHA_STOP = -ROTATION_OFFSET - (FREE_AREA_ANGLE / 2.0) - TRACK_STOP_ANGLE;
                currentColor = TRACK_STOP_COLOR;
                break;

            case CLOCKWISE:

            default:
                TRACK_START_ANGLE = (TRACK_START * ANGLE_STEP);
                TRACK_SECTION_ANGLE = (TRACK_SECTION * ANGLE_STEP);
                TRACK_STOP_ANGLE = (TRACK_STOP * ANGLE_STEP);

                ALPHA_START = -ROTATION_OFFSET - (FREE_AREA_ANGLE / 2.0) - TRACK_START_ANGLE + (MIN_VALUE * ANGLE_STEP);
                ALPHA_SECTION = -ROTATION_OFFSET - (FREE_AREA_ANGLE / 2.0) - TRACK_SECTION_ANGLE + (MIN_VALUE * ANGLE_STEP);
                ALPHA_STOP = -ROTATION_OFFSET - (FREE_AREA_ANGLE / 2.0) - TRACK_STOP_ANGLE + (MIN_VALUE * ANGLE_STEP);
                currentColor = TRACK_START_COLOR;
                break;
        }

        // Define the stepsize between each line of the track so that it will work on small ranges like on large ranges
        final double RANGE_FACTOR = 1000 / (MAX_VALUE - MIN_VALUE) < 10 ? 10 : 1000 / (MAX_VALUE - MIN_VALUE);
        final double FRACTION_STEP = 1 / RANGE_FACTOR;

        G2.setStroke(STD_STROKE);
        float fraction = 0;
        // Draw track from TRACK_START to TRACK_SECTION
        for (double alpha = ALPHA_START; Double.compare(alpha, ALPHA_SECTION) >= 0; alpha -= (ANGLE_STEP / RANGE_FACTOR), fraction += FRACTION_STEP) {
            sinValue = Math.sin(alpha);
            cosValue = Math.cos(alpha);
            switch (DIRECTION) {
                case CLOCKWISE:
                    currentColor = UTIL.getColorFromFraction(TRACK_START_COLOR, TRACK_SECTION_COLOR, (int) (TRACK_SECTION - TRACK_START), (int) (fraction));
                    break;

                case COUNTER_CLOCKWISE:
                    currentColor = UTIL.getColorFromFraction(TRACK_STOP_COLOR, TRACK_SECTION_COLOR, (int) (TRACK_STOP - TRACK_SECTION), (int) (fraction));
                    break;
            }

            G2.setColor(currentColor);
            INNER_POINT.setLocation(CENTER.getX() + (RADIUS - TRACK_WIDTH) * sinValue, CENTER.getY() + (RADIUS - TRACK_WIDTH) * cosValue);
            OUTER_POINT.setLocation(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);

            TICK.setLine(INNER_POINT, OUTER_POINT);

            G2.draw(TICK);
        }

        // Draw track from TRACK_SECTION to TRACK_STOP
        fraction = 0;
        for (double alpha = ALPHA_SECTION; Double.compare(alpha, ALPHA_STOP) >= 0; alpha -= (ANGLE_STEP / RANGE_FACTOR), fraction += FRACTION_STEP) {
            sinValue = Math.sin(alpha);
            cosValue = Math.cos(alpha);
            switch (DIRECTION) {
                case CLOCKWISE:
                    currentColor = UTIL.getColorFromFraction(TRACK_SECTION_COLOR, TRACK_STOP_COLOR, (int) (TRACK_STOP - TRACK_SECTION), (int) (fraction));
                    break;
                case COUNTER_CLOCKWISE:
                    currentColor = UTIL.getColorFromFraction(TRACK_SECTION_COLOR, TRACK_START_COLOR, (int) (TRACK_SECTION - TRACK_START), (int) (fraction));
                    break;
            }

            G2.setColor(currentColor);
            INNER_POINT.setLocation(CENTER.getX() + (RADIUS - TRACK_WIDTH) * sinValue, CENTER.getY() + (RADIUS - TRACK_WIDTH) * cosValue);
            OUTER_POINT.setLocation(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);

            TICK.setLine(INNER_POINT, OUTER_POINT);

            G2.draw(TICK);
        }

        G2.dispose();

        return image;
    }

    /**
     * Returns the image of the posts for the pointer
     * @param WIDTH
     * @param POSITIONS
     * @return the post image that is used
     */
    protected BufferedImage create_POSTS_Image(final int WIDTH, final PostPosition... POSITIONS) {
        return createPostsImage(WIDTH, null, POSITIONS);
    }

    /**
     * Returns the image of the posts for the pointer
     * @param WIDTH
     * @param POSITIONS
     * @param image
     * @return the post image that is used
     */
    protected BufferedImage createPostsImage(final int WIDTH, BufferedImage image, final PostPosition... POSITIONS) {
        if (WIDTH <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        if (image == null) {
            image = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        }
        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        //final BufferedImage CENTER_KNOB = create_KNOB_Image((int) (WIDTH * 0.09));
        final BufferedImage SINGLE_POST = create_KNOB_Image((int) Math.ceil(WIDTH * 0.03738316893577576), KnobType.SMALL_STD_KNOB, getModel().getKnobStyle());

        List<PostPosition> postPositionList = Arrays.asList(POSITIONS);

        // Draw center knob
        if (postPositionList.contains(PostPosition.CENTER)) {
            switch (getKnobType()) {
                case SMALL_STD_KNOB:
                    //G2.drawImage(KNOB_FACTORY.create_KNOB_Image((int) Math.ceil(IMAGE_WIDTH * 0.08411216735839844), KnobType.SMALL_STD_KNOB), (int) Math.ceil(IMAGE_WIDTH * 0.4579439163208008), (int) Math.ceil(IMAGE_WIDTH * 0.4579439163208008), null);

                    final Ellipse2D CENTER_KNOB_FRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4579439163208008, IMAGE_HEIGHT * 0.4579439163208008, IMAGE_WIDTH * 0.08411216735839844, IMAGE_HEIGHT * 0.08411216735839844);
                    final Point2D CENTER_KNOB_FRAME_START = new Point2D.Double(0, CENTER_KNOB_FRAME.getBounds2D().getMinY());
                    final Point2D CENTER_KNOB_FRAME_STOP = new Point2D.Double(0, CENTER_KNOB_FRAME.getBounds2D().getMaxY());
                    final float[] CENTER_KNOB_FRAME_FRACTIONS = {
                        0.0f,
                        0.46f,
                        1.0f
                    };
                    final Color[] CENTER_KNOB_FRAME_COLORS = {
                        new Color(180, 180, 180, 255),
                        new Color(63, 63, 63, 255),
                        new Color(40, 40, 40, 255)
                    };

                    final LinearGradientPaint CENTER_KNOB_FRAME_GRADIENT = new LinearGradientPaint(CENTER_KNOB_FRAME_START, CENTER_KNOB_FRAME_STOP, CENTER_KNOB_FRAME_FRACTIONS, CENTER_KNOB_FRAME_COLORS);
                    G2.setPaint(CENTER_KNOB_FRAME_GRADIENT);
                    G2.fill(CENTER_KNOB_FRAME);

                    final Ellipse2D CENTER_KNOB_MAIN = new Ellipse2D.Double(IMAGE_WIDTH * 0.4672897160053253, IMAGE_HEIGHT * 0.4672897160053253, IMAGE_WIDTH * 0.06542053818702698, IMAGE_HEIGHT * 0.06542053818702698);
                    final Point2D CENTER_KNOB_MAIN_START = new Point2D.Double(0, CENTER_KNOB_MAIN.getBounds2D().getMinY());
                    final Point2D CENTER_KNOB_MAIN_STOP = new Point2D.Double(0, CENTER_KNOB_MAIN.getBounds2D().getMaxY());
                    final float[] CENTER_KNOB_MAIN_FRACTIONS = {
                        0.0f,
                        0.5f,
                        1.0f
                    };

                    final Color[] CENTER_KNOB_MAIN_COLORS;
                    switch (getModel().getKnobStyle()) {
                        case BLACK:
                            CENTER_KNOB_MAIN_COLORS = new Color[]{
                                new Color(0xBFBFBF),
                                new Color(0x2B2A2F),
                                new Color(0x7D7E80)
                            };
                            break;

                        case BRASS:
                            CENTER_KNOB_MAIN_COLORS = new Color[]{
                                new Color(0xDFD0AE),
                                new Color(0x7A5E3E),
                                new Color(0xCFBE9D)
                            };
                            break;

                        case SILVER:

                        default:
                            CENTER_KNOB_MAIN_COLORS = new Color[]{
                                new Color(0xD7D7D7),
                                new Color(0x747474),
                                new Color(0xD7D7D7)
                            };
                            break;
                    }

                    final LinearGradientPaint CENTER_KNOB_MAIN_GRADIENT = new LinearGradientPaint(CENTER_KNOB_MAIN_START, CENTER_KNOB_MAIN_STOP, CENTER_KNOB_MAIN_FRACTIONS, CENTER_KNOB_MAIN_COLORS);
                    G2.setPaint(CENTER_KNOB_MAIN_GRADIENT);
                    G2.fill(CENTER_KNOB_MAIN);

                    final Ellipse2D CENTER_KNOB_INNERSHADOW = new Ellipse2D.Double(IMAGE_WIDTH * 0.4672897160053253, IMAGE_HEIGHT * 0.4672897160053253, IMAGE_WIDTH * 0.06542053818702698, IMAGE_HEIGHT * 0.06542053818702698);
                    final Point2D CENTER_KNOB_INNERSHADOW_CENTER = new Point2D.Double((0.4953271028037383 * IMAGE_WIDTH), (0.49065420560747663 * IMAGE_HEIGHT));
                    final float[] CENTER_KNOB_INNERSHADOW_FRACTIONS = {
                        0.0f,
                        0.75f,
                        0.76f,
                        1.0f
                    };
                    final Color[] CENTER_KNOB_INNERSHADOW_COLORS = {
                        new Color(0, 0, 0, 0),
                        new Color(0, 0, 0, 0),
                        new Color(0, 0, 0, 1),
                        new Color(0, 0, 0, 51)
                    };
                    final RadialGradientPaint CENTER_KNOB_INNERSHADOW_GRADIENT = new RadialGradientPaint(CENTER_KNOB_INNERSHADOW_CENTER, (float) (0.03271028037383177 * IMAGE_WIDTH), CENTER_KNOB_INNERSHADOW_FRACTIONS, CENTER_KNOB_INNERSHADOW_COLORS);
                    G2.setPaint(CENTER_KNOB_INNERSHADOW_GRADIENT);
                    G2.fill(CENTER_KNOB_INNERSHADOW);
                    break;

                case BIG_STD_KNOB:
                    //G2.drawImage(KNOB_FACTORY.create_KNOB_Image((int) Math.ceil(IMAGE_WIDTH * 0.1214953362941742), KnobType.BIG_STD_KNOB), (int) Math.ceil(IMAGE_WIDTH * 0.4392523467540741), (int) Math.ceil(IMAGE_WIDTH * 0.4392523467540741), null);
                    final Ellipse2D BIGCENTER_BACKGROUNDFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4392523467540741, IMAGE_HEIGHT * 0.4392523467540741, IMAGE_WIDTH * 0.1214953362941742, IMAGE_HEIGHT * 0.1214953362941742);
                    final Point2D BIGCENTER_BACKGROUNDFRAME_START = new Point2D.Double(0, BIGCENTER_BACKGROUNDFRAME.getBounds2D().getMinY());
                    final Point2D BIGCENTER_BACKGROUNDFRAME_STOP = new Point2D.Double(0, BIGCENTER_BACKGROUNDFRAME.getBounds2D().getMaxY());
                    final float[] BIGCENTER_BACKGROUNDFRAME_FRACTIONS = {
                        0.0f,
                        1.0f
                    };

                    final Color[] BIGCENTER_BACKGROUNDFRAME_COLORS;
                    switch (getModel().getKnobStyle()) {
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
                    switch (getModel().getKnobStyle()) {
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
                    switch (getModel().getKnobStyle()) {
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
                    switch (getModel().getKnobStyle()) {
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
                    //G2.drawImage(KNOB_FACTORY.create_KNOB_Image((int) Math.ceil(IMAGE_WIDTH * 0.14018690586090088), KnobType.BIG_CHROME_KNOB), (int) Math.ceil(IMAGE_WIDTH * 0.42990654706954956), (int) Math.ceil(IMAGE_WIDTH * 0.42990654706954956), null);
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
                    switch (getModel().getKnobStyle()) {
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
        }

        // Draw min bottom
        if (postPositionList.contains(PostPosition.MIN_BOTTOM)) {
            G2.drawImage(SINGLE_POST, (int) (IMAGE_WIDTH * 0.336448609828949), (int) (IMAGE_HEIGHT * 0.8037382960319519), null);
        }

        // Draw max bottom post
        if (postPositionList.contains(PostPosition.MAX_BOTTOM)) {
            G2.drawImage(SINGLE_POST, (int) (IMAGE_WIDTH * 0.6261682510375977), (int) (IMAGE_HEIGHT * 0.8037382960319519), null);
        }

        // Draw min center bottom post
        if (postPositionList.contains(PostPosition.MAX_CENTER_BOTTOM)) {
            G2.drawImage(SINGLE_POST, (int) (IMAGE_WIDTH * 0.5233644843101501), (int) (IMAGE_HEIGHT * 0.8317757248878479), null);
        }

        // Draw max center top post
        if (postPositionList.contains(PostPosition.MAX_CENTER_TOP)) {
            G2.drawImage(SINGLE_POST, (int) (IMAGE_WIDTH * 0.5233644843101501), (int) (IMAGE_HEIGHT * 0.13084112107753754), null);
        }

        // Draw max right post
        if (postPositionList.contains(PostPosition.MAX_RIGHT)) {
            G2.drawImage(SINGLE_POST, (int) (IMAGE_WIDTH * 0.8317757248878479), (int) (IMAGE_HEIGHT * 0.514018714427948), null);
        }

        // Draw min left post
        if (postPositionList.contains(PostPosition.MIN_LEFT)) {
            G2.drawImage(SINGLE_POST, (int) (IMAGE_WIDTH * 0.13084112107753754), (int) (IMAGE_HEIGHT * 0.514018714427948), null);
        }

        // Draw lower center post
        final AffineTransform OLD_TRANSFORM = G2.getTransform();
        final Point2D KNOB_CENTER = new Point2D.Double();
        if (postPositionList.contains(PostPosition.LOWER_CENTER)) {
            switch (getKnobType()) {
                case SMALL_STD_KNOB:
                    final Ellipse2D LOWERCENTER_KNOB_FRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4579439163208008, IMAGE_HEIGHT * 0.6915887594223022, IMAGE_WIDTH * 0.08411216735839844, IMAGE_HEIGHT * 0.08411216735839844);
                    switch (getOrientation()) {
                        case WEST:
                            KNOB_CENTER.setLocation(LOWERCENTER_KNOB_FRAME.getCenterX(), LOWERCENTER_KNOB_FRAME.getCenterY());
                            G2.rotate(Math.PI / 2, KNOB_CENTER.getX(), KNOB_CENTER.getY());
                            break;
                    }
                    final Point2D LOWERCENTER_KNOB_FRAME_START = new Point2D.Double(0, LOWERCENTER_KNOB_FRAME.getBounds2D().getMinY());
                    final Point2D LOWERCENTER_KNOB_FRAME_STOP = new Point2D.Double(0, LOWERCENTER_KNOB_FRAME.getBounds2D().getMaxY());
                    final float[] LOWERCENTER_KNOB_FRAME_FRACTIONS = {
                        0.0f,
                        0.46f,
                        1.0f
                    };
                    final Color[] LOWERCENTER_KNOB_FRAME_COLORS = {
                        new Color(180, 180, 180, 255),
                        new Color(63, 63, 63, 255),
                        new Color(40, 40, 40, 255)
                    };

                    final LinearGradientPaint LOWERCENTER_KNOB_FRAME_GRADIENT = new LinearGradientPaint(LOWERCENTER_KNOB_FRAME_START, LOWERCENTER_KNOB_FRAME_STOP, LOWERCENTER_KNOB_FRAME_FRACTIONS, LOWERCENTER_KNOB_FRAME_COLORS);
                    G2.setPaint(LOWERCENTER_KNOB_FRAME_GRADIENT);
                    G2.fill(LOWERCENTER_KNOB_FRAME);

                    final Ellipse2D LOWERCENTER_KNOB_MAIN = new Ellipse2D.Double(IMAGE_WIDTH * 0.4672897160053253, IMAGE_HEIGHT * 0.7009345889091492, IMAGE_WIDTH * 0.06542053818702698, IMAGE_HEIGHT * 0.06542056798934937);
                    final Point2D LOWERCENTER_KNOB_MAIN_START = new Point2D.Double(0, LOWERCENTER_KNOB_MAIN.getBounds2D().getMinY());
                    final Point2D LOWERCENTER_KNOB_MAIN_STOP = new Point2D.Double(0, LOWERCENTER_KNOB_MAIN.getBounds2D().getMaxY());
                    final float[] LOWERCENTER_KNOB_MAIN_FRACTIONS = {
                        0.0f,
                        0.5f,
                        1.0f
                    };

                    final Color[] LOWERCENTER_KNOB_MAIN_COLORS;
                    switch (getModel().getKnobStyle()) {
                        case BLACK:
                            LOWERCENTER_KNOB_MAIN_COLORS = new Color[]{
                                new Color(0xBFBFBF),
                                new Color(0x2B2A2F),
                                new Color(0x7D7E80)
                            };
                            break;

                        case BRASS:
                            LOWERCENTER_KNOB_MAIN_COLORS = new Color[]{
                                new Color(0xDFD0AE),
                                new Color(0x7A5E3E),
                                new Color(0xCFBE9D)
                            };
                            break;

                        case SILVER:

                        default:
                            LOWERCENTER_KNOB_MAIN_COLORS = new Color[]{
                                new Color(0xD7D7D7),
                                new Color(0x747474),
                                new Color(0xD7D7D7)
                            };
                            break;
                    }

                    final LinearGradientPaint LOWERCENTER_KNOB_MAIN_GRADIENT = new LinearGradientPaint(LOWERCENTER_KNOB_MAIN_START, LOWERCENTER_KNOB_MAIN_STOP, LOWERCENTER_KNOB_MAIN_FRACTIONS, LOWERCENTER_KNOB_MAIN_COLORS);
                    G2.setPaint(LOWERCENTER_KNOB_MAIN_GRADIENT);
                    G2.fill(LOWERCENTER_KNOB_MAIN);

                    final Ellipse2D LOWERCENTER_KNOB_INNERSHADOW = new Ellipse2D.Double(IMAGE_WIDTH * 0.4672897160053253, IMAGE_HEIGHT * 0.7009345889091492, IMAGE_WIDTH * 0.06542053818702698, IMAGE_HEIGHT * 0.06542056798934937);
                    final Point2D LOWERCENTER_KNOB_INNERSHADOW_CENTER = new Point2D.Double((0.4953271028037383 * IMAGE_WIDTH), (0.7242990654205608 * IMAGE_HEIGHT));
                    final float[] LOWERCENTER_KNOB_INNERSHADOW_FRACTIONS = {
                        0.0f,
                        0.75f,
                        0.76f,
                        1.0f
                    };
                    final Color[] LOWERCENTER_KNOB_INNERSHADOW_COLORS = {
                        new Color(0, 0, 0, 0),
                        new Color(0, 0, 0, 0),
                        new Color(0, 0, 0, 1),
                        new Color(0, 0, 0, 51)
                    };
                    final RadialGradientPaint LOWERCENTER_KNOB_INNERSHADOW_GRADIENT = new RadialGradientPaint(LOWERCENTER_KNOB_INNERSHADOW_CENTER, (float) (0.03271028037383177 * IMAGE_WIDTH), LOWERCENTER_KNOB_INNERSHADOW_FRACTIONS, LOWERCENTER_KNOB_INNERSHADOW_COLORS);
                    G2.setPaint(LOWERCENTER_KNOB_INNERSHADOW_GRADIENT);
                    G2.fill(LOWERCENTER_KNOB_INNERSHADOW);
                    break;

                case BIG_STD_KNOB:
                    final Ellipse2D BIGLOWERCENTER_BACKGROUNDFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4392523467540741, IMAGE_HEIGHT * 0.672897219657898, IMAGE_WIDTH * 0.1214953362941742, IMAGE_HEIGHT * 0.1214953064918518);
                    switch (getOrientation()) {
                        case WEST:
                            KNOB_CENTER.setLocation(BIGLOWERCENTER_BACKGROUNDFRAME.getCenterX(), BIGLOWERCENTER_BACKGROUNDFRAME.getCenterY());
                            G2.rotate(Math.PI / 2, KNOB_CENTER.getX(), KNOB_CENTER.getY());
                            break;
                    }
                    final Point2D BIGLOWERCENTER_BACKGROUNDFRAME_START = new Point2D.Double(0, BIGLOWERCENTER_BACKGROUNDFRAME.getBounds2D().getMinY());
                    final Point2D BIGLOWERCENTER_BACKGROUNDFRAME_STOP = new Point2D.Double(0, BIGLOWERCENTER_BACKGROUNDFRAME.getBounds2D().getMaxY());
                    final float[] BIGLOWERCENTER_BACKGROUNDFRAME_FRACTIONS = {
                        0.0f,
                        1.0f
                    };
                    final Color[] BIGLOWERCENTER_BACKGROUNDFRAME_COLORS;
                    switch (getModel().getKnobStyle()) {
                        case BLACK:
                            BIGLOWERCENTER_BACKGROUNDFRAME_COLORS = new Color[]{
                                new Color(129, 133, 136, 255),
                                new Color(61, 61, 73, 255)
                            };
                            break;

                        case BRASS:
                            BIGLOWERCENTER_BACKGROUNDFRAME_COLORS = new Color[]{
                                new Color(143, 117, 80, 255),
                                new Color(100, 76, 49, 255)
                            };
                            break;

                        case SILVER:

                        default:
                            BIGLOWERCENTER_BACKGROUNDFRAME_COLORS = new Color[]{
                                new Color(152, 152, 152, 255),
                                new Color(118, 121, 126, 255)
                            };
                            break;
                    }
                    final LinearGradientPaint BIGLOWERCENTER_BACKGROUNDFRAME_GRADIENT = new LinearGradientPaint(BIGLOWERCENTER_BACKGROUNDFRAME_START, BIGLOWERCENTER_BACKGROUNDFRAME_STOP, BIGLOWERCENTER_BACKGROUNDFRAME_FRACTIONS, BIGLOWERCENTER_BACKGROUNDFRAME_COLORS);
                    G2.setPaint(BIGLOWERCENTER_BACKGROUNDFRAME_GRADIENT);
                    G2.fill(BIGLOWERCENTER_BACKGROUNDFRAME);

                    final Ellipse2D BIGLOWERCENTER_BACKGROUND = new Ellipse2D.Double(IMAGE_WIDTH * 0.44392523169517517, IMAGE_HEIGHT * 0.677570104598999, IMAGE_WIDTH * 0.11214950680732727, IMAGE_HEIGHT * 0.11214953660964966);
                    final Point2D BIGLOWERCENTER_BACKGROUND_START = new Point2D.Double(0, BIGLOWERCENTER_BACKGROUND.getBounds2D().getMinY());
                    final Point2D BIGLOWERCENTER_BACKGROUND_STOP = new Point2D.Double(0, BIGLOWERCENTER_BACKGROUND.getBounds2D().getMaxY());
                    final float[] BIGLOWERCENTER_BACKGROUND_FRACTIONS = {
                        0.0f,
                        1.0f
                    };
                    final Color[] BIGLOWERCENTER_BACKGROUND_COLORS;
                    switch (getModel().getKnobStyle()) {
                        case BLACK:
                            BIGLOWERCENTER_BACKGROUND_COLORS = new Color[]{
                                new Color(26, 27, 32, 255),
                                new Color(96, 97, 102, 255)
                            };
                            break;

                        case BRASS:
                            BIGLOWERCENTER_BACKGROUND_COLORS = new Color[]{
                                new Color(98, 75, 49, 255),
                                new Color(149, 109, 54, 255)
                            };
                            break;

                        case SILVER:

                        default:
                            BIGLOWERCENTER_BACKGROUND_COLORS = new Color[]{
                                new Color(118, 121, 126, 255),
                                new Color(191, 191, 191, 255)
                            };
                            break;
                    }
                    final LinearGradientPaint BIGLOWERCENTER_BACKGROUND_GRADIENT = new LinearGradientPaint(BIGLOWERCENTER_BACKGROUND_START, BIGLOWERCENTER_BACKGROUND_STOP, BIGLOWERCENTER_BACKGROUND_FRACTIONS, BIGLOWERCENTER_BACKGROUND_COLORS);
                    G2.setPaint(BIGLOWERCENTER_BACKGROUND_GRADIENT);
                    G2.fill(BIGLOWERCENTER_BACKGROUND);

                    final Ellipse2D BIGLOWERCENTER_FOREGROUNDFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4532710313796997, IMAGE_HEIGHT * 0.6869158744812012, IMAGE_WIDTH * 0.09345793724060059, IMAGE_HEIGHT * 0.09345793724060059);
                    final Point2D BIGLOWERCENTER_FOREGROUNDFRAME_START = new Point2D.Double(0, BIGLOWERCENTER_FOREGROUNDFRAME.getBounds2D().getMinY());
                    final Point2D BIGLOWERCENTER_FOREGROUNDFRAME_STOP = new Point2D.Double(0, BIGLOWERCENTER_FOREGROUNDFRAME.getBounds2D().getMaxY());
                    final float[] BIGLOWERCENTER_FOREGROUNDFRAME_FRACTIONS = {
                        0.0f,
                        0.47f,
                        1.0f
                    };
                    final Color[] BIGLOWERCENTER_FOREGROUNDFRAME_COLORS;
                    switch (getModel().getKnobStyle()) {
                        case BLACK:
                            BIGLOWERCENTER_FOREGROUNDFRAME_COLORS = new Color[]{
                                new Color(191, 191, 191, 255),
                                new Color(56, 57, 61, 255),
                                new Color(143, 144, 146, 255)
                            };
                            break;

                        case BRASS:
                            BIGLOWERCENTER_FOREGROUNDFRAME_COLORS = new Color[]{
                                new Color(147, 108, 54, 255),
                                new Color(82, 66, 50, 255),
                                new Color(147, 108, 54, 255)
                            };
                            break;

                        case SILVER:

                        default:
                            BIGLOWERCENTER_FOREGROUNDFRAME_COLORS = new Color[]{
                                new Color(191, 191, 191, 255),
                                new Color(116, 116, 116, 255),
                                new Color(143, 144, 146, 255)
                            };
                            break;
                    }
                    final LinearGradientPaint BIGLOWERCENTER_FOREGROUNDFRAME_GRADIENT = new LinearGradientPaint(BIGLOWERCENTER_FOREGROUNDFRAME_START, BIGLOWERCENTER_FOREGROUNDFRAME_STOP, BIGLOWERCENTER_FOREGROUNDFRAME_FRACTIONS, BIGLOWERCENTER_FOREGROUNDFRAME_COLORS);
                    G2.setPaint(BIGLOWERCENTER_FOREGROUNDFRAME_GRADIENT);
                    G2.fill(BIGLOWERCENTER_FOREGROUNDFRAME);

                    final Ellipse2D BIGLOWERCENTER_FOREGROUND = new Ellipse2D.Double(IMAGE_WIDTH * 0.4579439163208008, IMAGE_HEIGHT * 0.6915887594223022, IMAGE_WIDTH * 0.08411216735839844, IMAGE_HEIGHT * 0.08411216735839844);
                    final Point2D BIGLOWERCENTER_FOREGROUND_START = new Point2D.Double(0, BIGLOWERCENTER_FOREGROUND.getBounds2D().getMinY());
                    final Point2D BIGLOWERCENTER_FOREGROUND_STOP = new Point2D.Double(0, BIGLOWERCENTER_FOREGROUND.getBounds2D().getMaxY());
                    final float[] BIGLOWERCENTER_FOREGROUND_FRACTIONS = {
                        0.0f,
                        0.21f,
                        0.5f,
                        0.78f,
                        1.0f
                    };
                    final Color[] BIGLOWERCENTER_FOREGROUND_COLORS;
                    switch (getModel().getKnobStyle()) {
                        case BLACK:
                            BIGLOWERCENTER_FOREGROUND_COLORS = new Color[]{
                                new Color(191, 191, 191, 255),
                                new Color(94, 93, 99, 255),
                                new Color(43, 42, 47, 255),
                                new Color(78, 79, 81, 255),
                                new Color(143, 144, 146, 255)
                            };
                            break;

                        case BRASS:
                            BIGLOWERCENTER_FOREGROUND_COLORS = new Color[]{
                                new Color(223, 208, 174, 255),
                                new Color(159, 136, 104, 255),
                                new Color(122, 94, 62, 255),
                                new Color(159, 136, 104, 255),
                                new Color(223, 208, 174, 255)
                            };
                            break;

                        case SILVER:

                        default:
                            BIGLOWERCENTER_FOREGROUND_COLORS = new Color[]{
                                new Color(215, 215, 215, 255),
                                new Color(139, 142, 145, 255),
                                new Color(100, 100, 100, 255),
                                new Color(139, 142, 145, 255),
                                new Color(215, 215, 215, 255)
                            };
                            break;
                    }
                    final LinearGradientPaint BIGLOWERCENTER_FOREGROUND_GRADIENT = new LinearGradientPaint(BIGLOWERCENTER_FOREGROUND_START, BIGLOWERCENTER_FOREGROUND_STOP, BIGLOWERCENTER_FOREGROUND_FRACTIONS, BIGLOWERCENTER_FOREGROUND_COLORS);
                    G2.setPaint(BIGLOWERCENTER_FOREGROUND_GRADIENT);
                    G2.fill(BIGLOWERCENTER_FOREGROUND);
                    break;

                case BIG_CHROME_KNOB:
                    final Ellipse2D CHROMEKNOB_BACKFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4000000059604645, IMAGE_HEIGHT * 0.6333333253860474, IMAGE_WIDTH * 0.20000001788139343, IMAGE_HEIGHT * 0.19999998807907104);
                    switch (getOrientation()) {
                        case WEST:
                            KNOB_CENTER.setLocation(CHROMEKNOB_BACKFRAME.getCenterX(), CHROMEKNOB_BACKFRAME.getCenterY());
                            G2.rotate(Math.PI / 2, KNOB_CENTER.getX(), KNOB_CENTER.getY());
                            break;
                    }
                    final Point2D CHROMEKNOB_BACKFRAME_START = new Point2D.Double((0.44666666666666666 * IMAGE_WIDTH), (0.6466666666666666 * IMAGE_HEIGHT));
                    final Point2D CHROMEKNOB_BACKFRAME_STOP = new Point2D.Double(((0.44666666666666666 + 0.10245105775175295) * IMAGE_WIDTH), ((0.6466666666666666 + 0.16395596525690903) * IMAGE_HEIGHT));
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

                    final Ellipse2D CHROMEKNOB_BACK = new Ellipse2D.Double(IMAGE_WIDTH * 0.40666666626930237, IMAGE_HEIGHT * 0.6399999856948853, IMAGE_WIDTH * 0.18666663765907288, IMAGE_HEIGHT * 0.18666666746139526);
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

                    final Ellipse2D CHROMEKNOB_FOREFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4533333480358124, IMAGE_HEIGHT * 0.6866666674613953, IMAGE_WIDTH * 0.09333333373069763, IMAGE_HEIGHT * 0.09333330392837524);
                    final Point2D CHROMEKNOB_FOREFRAME_START = new Point2D.Double((0.47333333333333333 * IMAGE_WIDTH), (0.6933333333333334 * IMAGE_HEIGHT));
                    final Point2D CHROMEKNOB_FOREFRAME_STOP = new Point2D.Double(((0.47333333333333333 + 0.04846338496746472) * IMAGE_WIDTH), ((0.6933333333333334 + 0.07184992295477029) * IMAGE_HEIGHT));
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

                    final Ellipse2D CHROMEKNOB_FORE = new Ellipse2D.Double(IMAGE_WIDTH * 0.46000000834465027, IMAGE_HEIGHT * 0.6933333277702332, IMAGE_WIDTH * 0.08000001311302185, IMAGE_HEIGHT * 0.07999998331069946);
                    final Point2D CHROMEKNOB_FORE_START = new Point2D.Double((0.47333333333333333 * IMAGE_WIDTH), (0.7 * IMAGE_HEIGHT));
                    final Point2D CHROMEKNOB_FORE_STOP = new Point2D.Double(((0.47333333333333333 + 0.04473543227765974) * IMAGE_WIDTH), ((0.7 + 0.06632300580440334) * IMAGE_HEIGHT));
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
                    final Ellipse2D METALKNOBLC_FRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4579439163208008, IMAGE_HEIGHT * 0.6915887594223022, IMAGE_WIDTH * 0.08411216735839844, IMAGE_HEIGHT * 0.08411216735839844);
                    switch (getOrientation()) {
                        case WEST:
                            KNOB_CENTER.setLocation(METALKNOBLC_FRAME.getCenterX(), METALKNOBLC_FRAME.getCenterY());
                            G2.rotate(Math.PI / 2, KNOB_CENTER.getX(), KNOB_CENTER.getY());
                            break;
                    }
                    final Point2D METALKNOBLC_FRAME_START = new Point2D.Double(0, METALKNOBLC_FRAME.getBounds2D().getMinY());
                    final Point2D METALKNOBLC_FRAME_STOP = new Point2D.Double(0, METALKNOBLC_FRAME.getBounds2D().getMaxY());
                    final float[] METALKNOBLC_FRAME_FRACTIONS = {
                        0.0f,
                        0.47f,
                        1.0f
                    };
                    final Color[] METALKNOBLC_FRAME_COLORS = {
                        new Color(92, 95, 101, 255),
                        new Color(46, 49, 53, 255),
                        new Color(22, 23, 26, 255)
                    };
                    final LinearGradientPaint METALKNOBLC_FRAME_GRADIENT = new LinearGradientPaint(METALKNOBLC_FRAME_START, METALKNOBLC_FRAME_STOP, METALKNOBLC_FRAME_FRACTIONS, METALKNOBLC_FRAME_COLORS);
                    G2.setPaint(METALKNOBLC_FRAME_GRADIENT);
                    G2.fill(METALKNOBLC_FRAME);

                    final Ellipse2D METALKNOBLC_MAIN = new Ellipse2D.Double(IMAGE_WIDTH * 0.46261683106422424, IMAGE_HEIGHT * 0.6962617039680481, IMAGE_WIDTH * 0.0747663676738739, IMAGE_HEIGHT * 0.07476633787155151);
                    final Point2D METALKNOBLC_MAIN_START = new Point2D.Double(0, METALKNOBLC_MAIN.getBounds2D().getMinY());
                    final Point2D METALKNOBLC_MAIN_STOP = new Point2D.Double(0, METALKNOBLC_MAIN.getBounds2D().getMaxY());
                    final float[] METALKNOBLC_MAIN_FRACTIONS = {
                        0.0f,
                        1.0f
                    };
                    final Color[] METALKNOBLC_MAIN_COLORS;
                    switch (getModel().getKnobStyle()) {
                        case BLACK:
                            METALKNOBLC_MAIN_COLORS = new Color[]{
                                new Color(0x2B2A2F),
                                new Color(0x1A1B20)
                            };
                            break;

                        case BRASS:
                            METALKNOBLC_MAIN_COLORS = new Color[]{
                                new Color(0x966E36),
                                new Color(0x7C5F3D)
                            };
                            break;

                        case SILVER:

                        default:
                            METALKNOBLC_MAIN_COLORS = new Color[]{
                                new Color(204, 204, 204, 255),
                                new Color(87, 92, 98, 255)
                            };
                            break;
                    }
                    final LinearGradientPaint METALKNOBLC_MAIN_GRADIENT = new LinearGradientPaint(METALKNOBLC_MAIN_START, METALKNOBLC_MAIN_STOP, METALKNOBLC_MAIN_FRACTIONS, METALKNOBLC_MAIN_COLORS);
                    G2.setPaint(METALKNOBLC_MAIN_GRADIENT);
                    G2.fill(METALKNOBLC_MAIN);

                    final GeneralPath METALKNOBLC_LOWERHL = new GeneralPath();
                    METALKNOBLC_LOWERHL.setWindingRule(Path2D.WIND_EVEN_ODD);
                    METALKNOBLC_LOWERHL.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.7616822429906542);
                    METALKNOBLC_LOWERHL.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.7523364485981309, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.7476635514018691, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.7476635514018691);
                    METALKNOBLC_LOWERHL.curveTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.7476635514018691, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.7523364485981309, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.7616822429906542);
                    METALKNOBLC_LOWERHL.curveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.7663551401869159, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.7710280373831776, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.7710280373831776);
                    METALKNOBLC_LOWERHL.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.7710280373831776, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.7663551401869159, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.7616822429906542);
                    METALKNOBLC_LOWERHL.closePath();
                    final Point2D METALKNOBLC_LOWERHL_CENTER = new Point2D.Double((0.5 * IMAGE_WIDTH), (0.7710280373831776 * IMAGE_HEIGHT));
                    final float[] METALKNOBLC_LOWERHL_FRACTIONS = {
                        0.0f,
                        1.0f
                    };
                    final Color[] METALKNOBLC_LOWERHL_COLORS = {
                        new Color(255, 255, 255, 153),
                        new Color(255, 255, 255, 0)
                    };
                    final RadialGradientPaint METALKNOBLC_LOWERHL_GRADIENT = new RadialGradientPaint(METALKNOBLC_LOWERHL_CENTER, (float) (0.03271028037383177 * IMAGE_WIDTH), METALKNOBLC_LOWERHL_FRACTIONS, METALKNOBLC_LOWERHL_COLORS);
                    G2.setPaint(METALKNOBLC_LOWERHL_GRADIENT);
                    G2.fill(METALKNOBLC_LOWERHL);

                    final GeneralPath METALKNOBLC_UPPERHL = new GeneralPath();
                    METALKNOBLC_UPPERHL.setWindingRule(Path2D.WIND_EVEN_ODD);
                    METALKNOBLC_UPPERHL.moveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.7149532710280374);
                    METALKNOBLC_UPPERHL.curveTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.7009345794392523, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.6915887850467289, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.6915887850467289);
                    METALKNOBLC_UPPERHL.curveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.6915887850467289, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.7009345794392523, IMAGE_WIDTH * 0.46261682242990654, IMAGE_HEIGHT * 0.7149532710280374);
                    METALKNOBLC_UPPERHL.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.719626168224299, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.7242990654205608, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.7242990654205608);
                    METALKNOBLC_UPPERHL.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.7242990654205608, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.719626168224299, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.7149532710280374);
                    METALKNOBLC_UPPERHL.closePath();
                    final Point2D METALKNOBLC_UPPERHL_CENTER = new Point2D.Double((0.4953271028037383 * IMAGE_WIDTH), (0.6915887850467289 * IMAGE_HEIGHT));
                    final float[] METALKNOBLC_UPPERHL_FRACTIONS = {
                        0.0f,
                        1.0f
                    };
                    final Color[] METALKNOBLC_UPPERHL_COLORS = {
                        new Color(255, 255, 255, 191),
                        new Color(255, 255, 255, 0)
                    };
                    final RadialGradientPaint METALKNOBLC_UPPERHL_GRADIENT = new RadialGradientPaint(METALKNOBLC_UPPERHL_CENTER, (float) (0.04906542056074766 * IMAGE_WIDTH), METALKNOBLC_UPPERHL_FRACTIONS, METALKNOBLC_UPPERHL_COLORS);
                    G2.setPaint(METALKNOBLC_UPPERHL_GRADIENT);
                    G2.fill(METALKNOBLC_UPPERHL);

                    final Ellipse2D METALKNOBLC_INNERFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.47663551568984985, IMAGE_HEIGHT * 0.7149532437324524, IMAGE_WIDTH * 0.04205608367919922, IMAGE_HEIGHT * 0.04205608367919922);
                    final Point2D METALKNOBLC_INNERFRAME_START = new Point2D.Double(0, METALKNOBLC_INNERFRAME.getBounds2D().getMinY());
                    final Point2D METALKNOBLC_INNERFRAME_STOP = new Point2D.Double(0, METALKNOBLC_INNERFRAME.getBounds2D().getMaxY());
                    final float[] METALKNOBLC_INNERFRAME_FRACTIONS = {
                        0.0f,
                        1.0f
                    };
                    final Color[] METALKNOBLC_INNERFRAME_COLORS = {
                        new Color(0, 0, 0, 255),
                        new Color(204, 204, 204, 255)
                    };
                    final LinearGradientPaint METALKNOBLC_INNERFRAME_GRADIENT = new LinearGradientPaint(METALKNOBLC_INNERFRAME_START, METALKNOBLC_INNERFRAME_STOP, METALKNOBLC_INNERFRAME_FRACTIONS, METALKNOBLC_INNERFRAME_COLORS);
                    G2.setPaint(METALKNOBLC_INNERFRAME_GRADIENT);
                    G2.fill(METALKNOBLC_INNERFRAME);

                    final Ellipse2D METALKNOBLC_INNERBACKGROUND = new Ellipse2D.Double(IMAGE_WIDTH * 0.4813084006309509, IMAGE_HEIGHT * 0.7196261882781982, IMAGE_WIDTH * 0.03271031379699707, IMAGE_HEIGHT * 0.032710254192352295);
                    final Point2D METALKNOBLC_INNERBACKGROUND_START = new Point2D.Double(0, METALKNOBLC_INNERBACKGROUND.getBounds2D().getMinY());
                    final Point2D METALKNOBLC_INNERBACKGROUND_STOP = new Point2D.Double(0, METALKNOBLC_INNERBACKGROUND.getBounds2D().getMaxY());
                    final float[] METALKNOBLC_INNERBACKGROUND_FRACTIONS = {
                        0.0f,
                        1.0f
                    };
                    final Color[] METALKNOBLC_INNERBACKGROUND_COLORS = {
                        new Color(1, 6, 11, 255),
                        new Color(50, 52, 56, 255)
                    };
                    final LinearGradientPaint METALKNOBLC_INNERBACKGROUND_GRADIENT = new LinearGradientPaint(METALKNOBLC_INNERBACKGROUND_START, METALKNOBLC_INNERBACKGROUND_STOP, METALKNOBLC_INNERBACKGROUND_FRACTIONS, METALKNOBLC_INNERBACKGROUND_COLORS);
                    G2.setPaint(METALKNOBLC_INNERBACKGROUND_GRADIENT);
                    G2.fill(METALKNOBLC_INNERBACKGROUND);
                    break;
            }
        }

        // Reset orientation
        G2.setTransform(OLD_TRANSFORM);

        // Draw radialvertical gauge right post
        if (postPositionList.contains(PostPosition.SMALL_GAUGE_MAX_RIGHT)) {
            switch (getOrientation()) {
                case WEST:
                    KNOB_CENTER.setLocation(IMAGE_WIDTH * 0.7803738117218018 + SINGLE_POST.getWidth() / 2.0, IMAGE_HEIGHT * 0.44859811663627625 + SINGLE_POST.getHeight() / 2.0);
                    G2.rotate(Math.PI / 2, KNOB_CENTER.getX(), KNOB_CENTER.getY());
                    break;
            }
            G2.drawImage(SINGLE_POST, (int) (IMAGE_WIDTH * 0.7803738117218018), (int) (IMAGE_HEIGHT * 0.44859811663627625), null);
            G2.setTransform(OLD_TRANSFORM);
        }

        // Draw radialvertical gauge left post
        if (postPositionList.contains(PostPosition.SMALL_GAUGE_MIN_LEFT)) {
            switch (getOrientation()) {
                case WEST:
                    KNOB_CENTER.setLocation(IMAGE_WIDTH * 0.1822429895401001 + SINGLE_POST.getWidth() / 2.0, IMAGE_HEIGHT * 0.44859811663627625 + SINGLE_POST.getHeight() / 2.0);
                    G2.rotate(Math.PI / 2, KNOB_CENTER.getX(), KNOB_CENTER.getY());
                    break;
            }
            G2.drawImage(SINGLE_POST, (int) (IMAGE_WIDTH * 0.1822429895401001), (int) (IMAGE_HEIGHT * 0.44859811663627625), null);
            G2.setTransform(OLD_TRANSFORM);
        }

        G2.dispose();

        return image;
    }

    /**
     * Creates a single alignment post image that could be placed on all the positions where it is needed
     * @param WIDTH
     * @param KNOB_TYPE
     * @return a buffered image that contains a single alignment post of the given type
     */
    private BufferedImage create_KNOB_Image(final int WIDTH, final KnobType KNOB_TYPE, final KnobStyle KNOB_STYLE) {
        return KNOB_FACTORY.create_KNOB_Image(WIDTH, KNOB_TYPE, KNOB_STYLE);
    }

    /**
     * Returns the image of the threshold indicator.
     * @param WIDTH
     * @return the threshold image that is used
     */
    protected BufferedImage create_THRESHOLD_Image(final int WIDTH) {
        return create_THRESHOLD_Image(WIDTH, 0);
    }

    protected BufferedImage create_THRESHOLD_Image(final int WIDTH, final double ROTATION_OFFSET) {
        if (WIDTH <= 22) // 22 is needed because otherwise the image size could be smaller than 1
        {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        final int IMAGE_WIDTH = (int) (WIDTH * 0.0420560748);
        final int IMAGE_HEIGHT = (int) (WIDTH * 0.0981308411);

        final BufferedImage IMAGE = UTIL.createImage(IMAGE_WIDTH, IMAGE_HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        G2.rotate(ROTATION_OFFSET, IMAGE_WIDTH / 2.0, IMAGE_HEIGHT / 2.0);

        final Point2D THRESHOLD_START = new Point2D.Double();
        final Point2D THRESHOLD_STOP = new Point2D.Double();

        final GeneralPath THRESHOLD = new GeneralPath();
        switch (getThresholdType()) {
            case ARROW:
                THRESHOLD.setWindingRule(Path2D.WIND_EVEN_ODD);
                THRESHOLD.moveTo(IMAGE_WIDTH * 0.1111111111111111, IMAGE_HEIGHT * 0.047619047619047616);
                THRESHOLD.lineTo(IMAGE_WIDTH * 0.8888888888888888, IMAGE_HEIGHT * 0.047619047619047616);
                THRESHOLD.lineTo(IMAGE_WIDTH * 0.8888888888888888, IMAGE_HEIGHT * 0.3333333333333333);
                THRESHOLD.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5714285714285714);
                THRESHOLD.lineTo(IMAGE_WIDTH * 0.1111111111111111, IMAGE_HEIGHT * 0.3333333333333333);
                THRESHOLD.closePath();
                THRESHOLD_START.setLocation(0, THRESHOLD.getBounds2D().getMinY());
                THRESHOLD_STOP.setLocation(0, THRESHOLD.getBounds2D().getMaxY());
                break;

            case TRIANGLE:

            default:
                THRESHOLD.setWindingRule(Path2D.WIND_EVEN_ODD);
                THRESHOLD.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.6190476190476191);
                THRESHOLD.lineTo(IMAGE_WIDTH * 0.1111111111111111, IMAGE_HEIGHT * 0.9523809523809523);
                THRESHOLD.lineTo(IMAGE_WIDTH * 0.8888888888888888, IMAGE_HEIGHT * 0.9523809523809523);
                THRESHOLD.closePath();
                THRESHOLD_START.setLocation(0, THRESHOLD.getBounds2D().getMaxY());
                THRESHOLD_STOP.setLocation(0, THRESHOLD.getBounds2D().getMinY());
                break;
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
     * @param WIDTH
     * @param COLOR
     * @return the image of the min or max measured value
     */
    protected BufferedImage create_MEASURED_VALUE_Image(final int WIDTH, final Color COLOR) {
        return create_MEASURED_VALUE_Image(WIDTH, COLOR, 0);
    }

    /**
     * Returns the image of the MinMeasuredValue and MaxMeasuredValue dependend
     * @param WIDTH
     * @param COLOR
     * @param ROTATION_OFFSET
     * @return the image of the min or max measured value
     */
    protected BufferedImage create_MEASURED_VALUE_Image(final int WIDTH, final Color COLOR, final double ROTATION_OFFSET) {
        if (WIDTH <= 36) // 36 is needed otherwise the image size could be smaller than 1
        {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        final int IMAGE_HEIGHT = (int) (WIDTH * 0.0280373832);
        final int IMAGE_WIDTH = IMAGE_HEIGHT;

        final BufferedImage IMAGE = UTIL.createImage(IMAGE_WIDTH, IMAGE_HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        G2.rotate(ROTATION_OFFSET, IMAGE_WIDTH / 2.0, IMAGE_HEIGHT / 2.0);

        final GeneralPath INDICATOR = new GeneralPath();
        INDICATOR.setWindingRule(Path2D.WIND_EVEN_ODD);
        INDICATOR.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT);
        INDICATOR.lineTo(0.0, 0.0);
        INDICATOR.lineTo(IMAGE_WIDTH, 0.0);
        INDICATOR.closePath();

        G2.setColor(COLOR);
        G2.fill(INDICATOR);

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns the image of the pointer. This pointer is centered in the gauge and of PointerType FG_TYPE1.
     * @param WIDTH
     * @return the pointer image that is used in all gauges that have a centered pointer
     */
    protected BufferedImage create_POINTER_Image(final int WIDTH) {
        return create_POINTER_Image(WIDTH, PointerType.TYPE1);
    }

    /**
     * Returns the image of the pointer. This pointer is centered in the gauge.
     * @param WIDTH
     * @param POINTER_TYPE
     * @return the pointer image that is used in all gauges that have a centered pointer
     */
    protected BufferedImage create_POINTER_Image(final int WIDTH, final PointerType POINTER_TYPE) {
        if (getPointerColor() != ColorDef.CUSTOM) {
            return POINTER_FACTORY.createStandardPointer(WIDTH, POINTER_TYPE, getPointerColor(), getBackgroundColor());
        } else {
            return POINTER_FACTORY.createStandardPointer(WIDTH, POINTER_TYPE, getPointerColor(), getModel().getCustomPointerColorObject(), getBackgroundColor());
        }
    }

    /**
     * Returns the image of the pointer. This pointer is centered in the gauge.
     * @param WIDTH
     * @param POINTER_TYPE
     * @param POINTER_COLOR
     * @return the pointer image that is used in all gauges that have a centered pointer
     */
    protected BufferedImage create_POINTER_Image(final int WIDTH, final PointerType POINTER_TYPE, final ColorDef POINTER_COLOR) {
        return POINTER_FACTORY.createStandardPointer(WIDTH, POINTER_TYPE, POINTER_COLOR, getBackgroundColor());
    }

    /**
     * Returns the image of the pointer. This pointer is centered in the gauge.
     * @param WIDTH
     * @param POINTER_TYPE
     * @param POINTER_COLOR
     * @param CUSTOM_POINTER_COLOR
     * @return the pointer image that is used in all gauges that have a centered pointer
     */
    protected BufferedImage create_POINTER_Image(final int WIDTH, final PointerType POINTER_TYPE, final ColorDef POINTER_COLOR, final CustomColorDef CUSTOM_POINTER_COLOR) {
        return POINTER_FACTORY.createStandardPointer(WIDTH, POINTER_TYPE, POINTER_COLOR, CUSTOM_POINTER_COLOR, getBackgroundColor());
    }

    /**
     * Returns the image of the pointer shadow. This shadow is centered in the gauge
     * @param WIDTH
     * @return the pointer shadow image that is used in all gauges that have a centered pointer
     */
    protected BufferedImage create_POINTER_SHADOW_Image(final int WIDTH) {
        return create_POINTER_SHADOW_Image(WIDTH, PointerType.TYPE1);
    }

    /**
     * Returns the image of the pointer shadow. This shadow is centered in the gauge
     * @param WIDTH
     * @param POINTER_TYPE
     * @return the pointer shadow image that is used in all gauges that have a centered pointer
     */
    protected BufferedImage create_POINTER_SHADOW_Image(final int WIDTH, final PointerType POINTER_TYPE) {
        return POINTER_FACTORY.createStandardPointerShadow(WIDTH, POINTER_TYPE);
    }

    /**
     * Returns the image of the glasseffect with a centered knob
     * @param WIDTH
     * @return the foreground image that will be used (in principle only the glass effect)
     */
    protected BufferedImage create_FOREGROUND_Image(final int WIDTH) {
        switch (getFrameType()) {
            case ROUND:
                return FOREGROUND_FACTORY.createRadialForeground(WIDTH);
            case SQUARE:
                return FOREGROUND_FACTORY.createLinearForeground(WIDTH, WIDTH);
            default:
                return FOREGROUND_FACTORY.createRadialForeground(WIDTH);
        }
    }

    /**
     * Returns the image of the glasseffect and a centered knob if wanted
     * @param WIDTH
     * @param WITH_CENTER_KNOB
     * @return the foreground image that will be used (in principle only the glass effect)
     */
    protected BufferedImage create_FOREGROUND_Image(final int WIDTH, final boolean WITH_CENTER_KNOB) {
        switch (getFrameType()) {
            case ROUND:
                return FOREGROUND_FACTORY.createRadialForeground(WIDTH, WITH_CENTER_KNOB);
            case SQUARE:
                return FOREGROUND_FACTORY.createLinearForeground(WIDTH, WIDTH, WITH_CENTER_KNOB);
            default:
                return FOREGROUND_FACTORY.createRadialForeground(WIDTH, WITH_CENTER_KNOB);
        }
    }

    /**
     * Returns the image of the selected (FG_TYPE1, FG_TYPE2, FG_TYPE3) glasseffect, the centered knob (if wanted)
     * @param WIDTH
     * @param WITH_CENTER_KNOB
     * @param TYPE
     * @return the foreground image that will be used
     */
    protected BufferedImage create_FOREGROUND_Image(final int WIDTH, final boolean WITH_CENTER_KNOB, final ForegroundType TYPE) {
        return create_FOREGROUND_Image(WIDTH, WITH_CENTER_KNOB, TYPE, null);
    }

    /**
     * Returns the image of the selected (FG_TYPE1, FG_TYPE2, FG_TYPE3) glasseffect, the centered knob (if wanted)
     * @param WIDTH
     * @param WITH_CENTER_KNOB
     * @param TYPE
     * @param IMAGE
     * @return the foreground image that will be used
     */
    protected BufferedImage create_FOREGROUND_Image(final int WIDTH, final boolean WITH_CENTER_KNOB, final ForegroundType TYPE, final BufferedImage IMAGE) {
        switch (getFrameType()) {
            case ROUND:
                return FOREGROUND_FACTORY.createRadialForeground(WIDTH, WITH_CENTER_KNOB, TYPE, IMAGE);
            case SQUARE:
                return FOREGROUND_FACTORY.createLinearForeground(WIDTH, WIDTH, WITH_CENTER_KNOB, IMAGE);
            default:
                return FOREGROUND_FACTORY.createRadialForeground(WIDTH, WITH_CENTER_KNOB, ForegroundType.FG_TYPE1, IMAGE);
        }
    }

    /**
     * Returns the image that will be displayed if the gauge is disabled
     * @param WIDTH
     * @return the disabled image that will be displayed if the gauge is disabled
     */
    protected BufferedImage create_DISABLED_Image(final int WIDTH) {
        return DISABLED_FACTORY.createRadialDisabled(WIDTH);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Size related methods">
    /**
     * Calculates the rectangle that specifies the area that is available
     * for painting the gauge. This means that if the component has insets
     * that are larger than 0, these will be taken into account.
     */
    @Override
    public void calcInnerBounds() {
        calcInnerBounds(getWidth(), getHeight());
    }

    public void calcInnerBounds(final int WIDTH, final int HEIGHT) {
        final Insets INSETS = getInsets();
        final int SIZE = (WIDTH - INSETS.left - INSETS.right) <= (HEIGHT - INSETS.top - INSETS.bottom) ? (WIDTH - INSETS.left - INSETS.right) : (HEIGHT - INSETS.top - INSETS.bottom);
        INNER_BOUNDS.setBounds(INSETS.left, INSETS.top, WIDTH - INSETS.left - INSETS.right, HEIGHT - INSETS.top - INSETS.bottom);
        if (!isFrameVisible()) {
            GAUGE_BOUNDS.setBounds(INSETS.left, INSETS.top, (int)(SIZE * 1.202247191), (int)(SIZE * 1.202247191));
        } else {
            GAUGE_BOUNDS.setBounds(INSETS.left, INSETS.top, SIZE, SIZE);
        }
        FRAMELESS_BOUNDS.setBounds(INSETS.left + (int)(SIZE * 0.08411215245723724), INSETS.top + (int)(SIZE * 0.08411215245723724), (int)(SIZE * 0.8317756652832031), (int)(SIZE * 0.8317756652832031));
    }

    /**
     * Returns the rectangle that specifies the area that is available
     * for painting the gauge. This means that if the component has insets
     * that are larger than 0, these will be taken into account.
     * If you add a border to the component the gauge will be drawn smaller
     * but within the border.
     * @return rectangle which describes the area that is available for painting
     */
    @Override
    public Rectangle getInnerBounds() {
        return INNER_BOUNDS;
    }

    public Rectangle getGaugeBounds() {
        return GAUGE_BOUNDS;
    }

    public Rectangle getFramelessBounds() {
        return FRAMELESS_BOUNDS;
    }

    public Point2D getFramelessOffset() {
        return FRAMELESS_OFFSET;
    }

    public void setFramelessOffset(final double X, final double Y) {
        FRAMELESS_OFFSET.setLocation(X, Y);
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension dim = super.getMinimumSize();
        if (dim.width < 50 || dim.height < 50) {
            dim = new Dimension(50, 50);
        }
        return dim;
    }

    @Override
    public void setMinimumSize(final Dimension DIM) {
        int  width = DIM.width < 50 ? 50 : DIM.width;
        int height = DIM.height < 50 ? 50 : DIM.height;
        final int SIZE = width <= height ? width : height;
        super.setMinimumSize(new Dimension(SIZE, SIZE));
        calcInnerBounds();
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
        invalidate();
        repaint();
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension dim = super.getMaximumSize();
        if (dim.width > 1080 || dim.height > 1080) {
            dim = new Dimension(1080, 1080);
        }
        return dim;
    }

    @Override
    public void setMaximumSize(final Dimension DIM) {
        int  width = DIM.width > 1080 ? 1080 : DIM.width;
        int height = DIM.height > 1080 ? 1080 : DIM.height;
        final int SIZE = width <= height ? width : height;
        super.setMaximumSize(new Dimension(SIZE, SIZE));
        calcInnerBounds();
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
        invalidate();
        repaint();
    }

    @Override
    public void setPreferredSize(final Dimension DIM) {
        final int SIZE = DIM.width <= DIM.height ? DIM.width : DIM.height;
        super.setPreferredSize(new Dimension(SIZE, SIZE));
        calcInnerBounds();
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
        invalidate();
        repaint();
    }

    @Override
    public void setSize(final int WIDTH, final int HEIGHT) {
        final int SIZE = WIDTH <= HEIGHT ? WIDTH : HEIGHT;
        super.setSize(SIZE, SIZE);
        calcInnerBounds();
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
    }

    @Override
    public void setSize(final Dimension DIM) {
        final int SIZE = DIM.width <= DIM.height ? DIM.width : DIM.height;
        super.setSize(new Dimension(SIZE, SIZE));
        calcInnerBounds();
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
    }

    @Override
    public void setBounds(final Rectangle BOUNDS) {
        if (BOUNDS.width <= BOUNDS.height) {
            // vertical
            int yNew;
            switch(verticalAlignment) {
                case SwingConstants.TOP:
                    yNew = BOUNDS.y;
                    break;
                case SwingConstants.BOTTOM:
                    yNew = BOUNDS.y + (BOUNDS.height - BOUNDS.width);
                    break;
                case SwingConstants.CENTER:
                default:
                    yNew = BOUNDS.y + ((BOUNDS.height - BOUNDS.width) / 2);
                    break;
            }
            super.setBounds(BOUNDS.x, yNew, BOUNDS.width, BOUNDS.width);
        } else {
            // horizontal
            int xNew;
            switch(horizontalAlignment) {
                case SwingConstants.LEFT:
                    xNew = BOUNDS.x;
                    break;
                case SwingConstants.RIGHT:
                    xNew = BOUNDS.x + (BOUNDS.width - BOUNDS.height);
                    break;
                case SwingConstants.CENTER:
                default:
                    xNew = BOUNDS.x + ((BOUNDS.width - BOUNDS.height) / 2);
                    break;
            }
            super.setBounds(xNew, BOUNDS.y, BOUNDS.height, BOUNDS.height);
        }
        calcInnerBounds();
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
    }

    @Override
    public void setBounds(final int X, final int Y, final int WIDTH, final int HEIGHT) {
        if (WIDTH <= HEIGHT) {
            // vertical
            int yNew;
            switch(verticalAlignment) {
                case SwingConstants.TOP:
                    yNew = Y;
                    break;
                case SwingConstants.BOTTOM:
                    yNew = Y + (HEIGHT - WIDTH);
                    break;
                case SwingConstants.CENTER:
                default:
                    yNew = Y + ((HEIGHT - WIDTH) / 2);
                    break;
            }
            super.setBounds(X, yNew, WIDTH, WIDTH);
        } else {
            // horizontal
            int xNew;
            switch(horizontalAlignment) {
                case SwingConstants.LEFT:
                    xNew = X;
                    break;
                case SwingConstants.RIGHT:
                    xNew = X + (WIDTH - HEIGHT);
                    break;
                case SwingConstants.CENTER:
                default:
                    xNew = X + ((WIDTH - HEIGHT) / 2);
                    break;
            }
            super.setBounds(xNew, Y, HEIGHT, HEIGHT);
        }
        calcInnerBounds();
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
    }

    @Override
    public void setBorder(Border BORDER) {
        super.setBorder(BORDER);
        calcInnerBounds();
        init(getGaugeBounds().width, getGaugeBounds().height);
    }

    /**
     * Returns the alignment of the radial gauge along the X axis.
     * @return the alignment of the radial gauge along the X axis.
     */
    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * Sets the alignment of the radial gauge along the X axis.
     * @param HORIZONTAL_ALIGNMENT (SwingConstants.CENTER is default)
     */
    public void setHorizontalAlignment(final int HORIZONTAL_ALIGNMENT) {
        horizontalAlignment = HORIZONTAL_ALIGNMENT;
    }

    /**
     * Returns the alignment of the radial gauge along the Y axis.
     * @return the alignment of the radial gauge along the Y axis.
     */
    public int getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * Sets the alignment of the radial gauge along the Y axis.
     * @param VERTICAL_ALIGNMENT (SwingConstants.CENTER is default)
     */
    public void setVerticalAlignment(final int VERTICAL_ALIGNMENT) {
        verticalAlignment = VERTICAL_ALIGNMENT;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ComponentListener methods">
    @Override
    public void componentResized(final ComponentEvent EVENT) {
        final int SIZE = getWidth() <= getHeight() ? getWidth() : getHeight();
        final Container PARENT = getParent();
        if ((PARENT != null) && (PARENT.getLayout() == null)) {
            if (SIZE < getMinimumSize().width || SIZE < getMinimumSize().height) {
                setSize(getMinimumSize());
            } else {
                setSize(SIZE, SIZE);
            }
        } else {
            if (SIZE < getMinimumSize().width || SIZE < getMinimumSize().height) {
                //setSize(getMinimumSize());
                setPreferredSize(getMinimumSize());
            } else {
                //setSize(new Dimension(SIZE, SIZE));
                setPreferredSize(new Dimension(SIZE, SIZE));
            }
        }

        calcInnerBounds();

        recreateLedImages();
        if (isLedOn()) {
            setCurrentLedImage(getLedImageOn());
        } else {
            setCurrentLedImage(getLedImageOff());
        }

        recreateUserLedImages();
        if (isUserLedOn()) {
            setCurrentUserLedImage(getUserLedImageOn());
        } else {
            setCurrentUserLedImage(getUserLedImageOff());
        }

        getModel().setSize(getLocation().x, getLocation().y, SIZE, SIZE);
        init(getInnerBounds().width, getInnerBounds().height);
        //revalidate();
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
        return "AbstractRadial";
    }
}
