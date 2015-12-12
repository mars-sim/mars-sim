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
package eu.hansolo.steelseries.tools;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;


/**
 *
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public class Model implements Cloneable {
    // <editor-fold defaultstate="collapsed" desc="Variable definitions">

    private static final int        BASE = 10;
    private final EventListenerList LISTENER_LIST = new EventListenerList();
    private final Font              STANDARD_BASE_FONT = new Font("Verdana", 0, 24);
    private final Font              STANDARD_INFO_FONT = new Font("Verdana", 0, 24);
    private final Font              DIGITAL_BASE_FONT = Util.INSTANCE.getDigitalFont().deriveFont(24);
    private ChangeEvent changeEvent;
    private Rectangle bounds;
    private double minValue;
    private double maxValue;
    private double range;
    private double value;
    private double oldValue;
    private double                  redrawTolerance;
    private double                  redrawFactor;
    private double peakValue;
    private boolean peakValueVisible;
    private double lcdValue;
    private double lcdThreshold;
    private boolean lcdThresholdVisible;
    private boolean lcdThresholdBehaviourInverted;
    private boolean                 lcdBlinking;
    private boolean singleLedBargraphEnabled;
    private boolean autoResetToZero;
    private boolean frameVisible;
    private FrameEffect frameEffect;
    private Color frameBaseColor;
    private boolean frameBaseColorEnabled;
    private boolean backgroundVisible;
    private boolean titleVisible;
    private boolean unitVisible;
    private boolean customTitleAndUnitFontEnabled;
    private boolean customLayerVisible;
    private boolean ledVisible;
    private boolean userLedVisible;
    private boolean lcdVisible;
    private boolean lcdUnitStringVisible;
    private boolean lcdScientificFormatEnabled;
    private boolean valueCoupled;
    private boolean digitalFontEnabled;
    private boolean customLcdUnitFontEnabled;
    private NumberSystem numberSystem;
    private boolean foregroundVisible;
    private boolean tickmarksVisible;
    private boolean ticklabelsVisible;
    private boolean minorTickmarksVisible;
    private boolean majorTickmarksVisible;
    private boolean sectionTickmarksOnly;
    private boolean tickmarkColorFromThemeEnabled;
    private boolean labelColorFromThemeEnabled;
    private TicklabelOrientation ticklabelOrientation;
    private double threshold;
    private boolean thresholdVisible;
    private ColorDef thresholdColor;
    private CustomColorDef customThresholdColor;
    private ThresholdType thresholdType;
    private boolean thresholdBehaviourInverted;
    private double minMeasuredValue;
    private boolean minMeasuredValueVisible;
    private double maxMeasuredValue;
    private boolean maxMeasuredValueVisible;
    private boolean rangeOfMeasuredValuesVisible;
    private Shape radialShapeOfMeasuredValues;
    private boolean collectingData;
    private double trackStart;
    private double trackSection;
    private double trackStop;
    private boolean trackVisible;
    private ArrayList<Section> sections;
    private boolean sectionsVisible;
    private boolean highlightSection;
    private ArrayList<Section> areas;
    private boolean areasVisible;
    private boolean highlightArea;
    private ArrayList<Section> tickmarkSections;
    private boolean tickmarkSectionsVisible;
    private GaugeType gaugeType;
    private double angleStep;
    private double logAngleStep;
    private boolean niceScale;
    private boolean logScale;
    private double niceRange;
    private int maxNoOfMajorTicks;
    private int maxNoOfMinorTicks;
    private double majorTickSpacing;
    private double minorTickSpacing;
    private double niceMinValue;
    private double niceMaxValue;
    private BackgroundColor backgroundColor;
    private Paint customBackground;
    private Color textureColor;
    private BufferedImage customLayer;
    private boolean glowVisible;
    private Color glowColor;
    private boolean glowing;
    private FrameType frameType;
    private FrameDesign frameDesign;
    private Paint customFrameDesign;
    private Paint outerFrameColor;
    private Paint innerFrameColor;
    private LedColor ledColor;
    private CustomLedColor customLedColor;
    private LedColor userLedColor;
    private CustomLedColor customUserLedColor;
    private LcdColor lcdColor;
    private Paint customLcdColor;
    private Color customLcdForegroundColor;
    private boolean                 lcdBackgroundVisible;
    private TickmarkType minorTickmarkType;
    private TickmarkType majorTickmarkType;
    private NumberFormat labelNumberFormat;
    private Color tickmarkColor;
    private Color customTickmarkColor;
    private Color labelColor;
    private Color trackStartColor;
    private Color trackSectionColor;
    private Color trackStopColor;
    private PointerType pointerType;
    private ColorDef pointerColor;
    private boolean pointerShadowVisible;
    private CustomColorDef customPointerColorObject;
    private ColorDef valueColor;
    private CustomColorDef customValueColorObject;
    private KnobType knobType;
    private KnobStyle knobStyle;
    private boolean postsVisible;
    private ForegroundType foregroundType;
    private Font lcdValueFont;
    private Font lcdUnitFont;
    private Font customLcdUnitFont;
    private Font lcdInfoFont;
    private int lcdDecimals;
    private Paint rangeOfMeasuredValuesPaint;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    /**
     * Creates a new Model with it's default parameters
     */
    public Model() {
        sections = new ArrayList<Section>(10);
        areas = new ArrayList<Section>(10);
        tickmarkSections = new ArrayList<Section>(10);
        init();
    }

    private void init() {
        bounds = new Rectangle();
        minValue = 0;
        maxValue = 100;
        range = maxValue - minValue;
        value = minValue;
        oldValue = minValue;
        redrawTolerance               = 0;
        redrawFactor                  = 0;
        peakValue = minValue;
        peakValueVisible = false;
        lcdValue = 0;
        lcdThreshold = 0;
        lcdThresholdVisible = false;
        lcdThresholdBehaviourInverted = false;
        lcdBlinking                   = false;
        autoResetToZero = false;
        frameVisible = true;
        frameEffect = FrameEffect.NONE;
        frameBaseColor = new Color(179, 179, 179, 255);
        frameBaseColorEnabled = false;
        backgroundVisible = true;
        titleVisible = true;
        unitVisible = true;
        customTitleAndUnitFontEnabled = false;
        customLayerVisible = false;
        ledVisible = true;
        lcdVisible = true;
        lcdUnitStringVisible = false;
        lcdScientificFormatEnabled = false;
        valueCoupled = true;
        digitalFontEnabled = false;
        customLcdUnitFontEnabled = false;
        numberSystem = NumberSystem.DEC;
        foregroundVisible = true;
        tickmarksVisible = true;
        ticklabelsVisible = true;
        minorTickmarksVisible = true;
        majorTickmarksVisible = true;
        sectionTickmarksOnly = false;
        tickmarkColorFromThemeEnabled = true;
        labelColorFromThemeEnabled = true;
        ticklabelOrientation = TicklabelOrientation.TANGENT;
        threshold = range / 2.0;
        thresholdVisible = false;
        thresholdColor = ColorDef.RED;
        customThresholdColor = new CustomColorDef(Color.RED);
        thresholdType = ThresholdType.TRIANGLE;
        thresholdBehaviourInverted = false;
        minMeasuredValue = maxValue;
        minMeasuredValueVisible = false;
        maxMeasuredValue = minValue;
        maxMeasuredValueVisible = false;
        rangeOfMeasuredValuesVisible = false;
        radialShapeOfMeasuredValues = new Arc2D.Double();
        collectingData = false;
        trackStart = minValue;
        trackSection = range / 2.0;
        trackStop = maxValue;
        trackVisible = false;
        sections.clear();
        sectionsVisible = false;
        highlightSection = false;
        areas.clear();
        areasVisible = false;
        highlightArea = false;
        tickmarkSections.clear();
        tickmarkSectionsVisible = false;
        gaugeType = GaugeType.TYPE4;
        angleStep = gaugeType.ANGLE_RANGE / range;
        logAngleStep = gaugeType.ANGLE_RANGE / (Util.INSTANCE.logOfBase(BASE, range) - 1) * (BASE - 1) + BASE;
        niceScale = true;
        logScale = false;
        maxNoOfMajorTicks = 10;
        maxNoOfMinorTicks = 10;
        backgroundColor = BackgroundColor.DARK_GRAY;
        customBackground = Color.RED;
        textureColor = new Color(0x686868);
        customLayer = null;
        glowVisible = false;
        glowColor = new Color(51, 255, 255);
        glowing = false;
        frameType = FrameType.ROUND;
        frameDesign = FrameDesign.METAL;
        outerFrameColor = new Color(0x848484);
        innerFrameColor = new Color(0.6f, 0.6f, 0.6f, 0.8f);
        customFrameDesign = Color.RED;
        ledColor = LedColor.RED_LED;
        customLedColor = new CustomLedColor(Color.RED);
        userLedColor = LedColor.RED_LED;
        customUserLedColor = new CustomLedColor(Color.RED);
        lcdColor = LcdColor.WHITE_LCD;
        customLcdColor = Color.RED;
        lcdBackgroundVisible          = true;
        customLcdForegroundColor = Color.BLACK;
        minorTickmarkType = TickmarkType.LINE;
        majorTickmarkType = TickmarkType.LINE;
        labelNumberFormat = NumberFormat.AUTO;
        tickmarkColor = backgroundColor.LABEL_COLOR;
        customTickmarkColor = Color.BLACK;
        labelColor = backgroundColor.LABEL_COLOR;
        trackStartColor = new Color(0.0f, 1.0f, 0.0f, 0.35f);
        trackSectionColor = new Color(1.0f, 1.0f, 0.0f, 0.35f);
        trackStopColor = new Color(1.0f, 0.0f, 0.0f, 0.35f);
        pointerType = PointerType.TYPE1;
        pointerColor = ColorDef.RED;
        pointerShadowVisible = true;
        customPointerColorObject = new CustomColorDef(Color.RED);
        valueColor = ColorDef.RED;
        customValueColorObject = new CustomColorDef(Color.RED);
        knobType = KnobType.SMALL_STD_KNOB;
        knobStyle = KnobStyle.SILVER;
        postsVisible = true;
        foregroundType = ForegroundType.FG_TYPE1;
        customLcdUnitFont = new Font("Verdana", 1, 24);
        lcdInfoFont = new Font("Verdana", 0, 24);
        rangeOfMeasuredValuesPaint = new Color(1.0f, 0.0f, 0.0f, 0.1f);

        calculate();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">
    /**
     * Returns the size of the gauge as a rectangle
     * @return the size of the gauge as a rectangle
     */
    public Rectangle getSize() {
        return bounds;
    }

    /**
     * Sets the width and height of the gauge
     * @param X
     * @param Y
     * @param WIDTH
     * @param HEIGHT
     */
    public void setSize(final int X, final int Y, final int WIDTH, final int HEIGHT) {
        bounds.setBounds(X, Y, WIDTH, HEIGHT);
        fireStateChanged();
    }

    /**
     * Returns the minimum value that will be displayed by the gauge
     * @return the minium value that will be displayed by the gauge
     */
    public double getMinValue() {
        return minValue;
    }

    /**
     * Sets the minium value that will be used for the calculation
     * of the nice minimum value for the scale.
     * @param MIN_VALUE
     */
    public void setMinValue(final double MIN_VALUE) {
        // check min-max values
        if (Double.compare(MIN_VALUE, maxValue) == 0) {
            throw new IllegalArgumentException("Min value cannot be equal to max value");
        }

        if (Double.compare(MIN_VALUE, maxValue) > 0) {
            minValue = maxValue;
            maxValue = MIN_VALUE;
        } else {
            minValue = MIN_VALUE;
        }
        calculate();
        validate();
        calcAngleStep();
        fireStateChanged();
    }

    /**
     * Returns the maximum value that will be displayed by the gauge
     * @return the maximum value that will be displayed by the gauge
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * Sets the maximum value that will be used for the calculation
     * of the nice maximum vlaue for the scale.
     * @param MAX_VALUE
     */
    public void setMaxValue(final double MAX_VALUE) {
        // check min-max values
        if (Double.compare(MAX_VALUE, minValue) == 0) {
            throw new IllegalArgumentException("Max value cannot be equal to min value");
        }

        if (Double.compare(MAX_VALUE, minValue) < 0) {
            maxValue = minValue;
            minValue = MAX_VALUE;
        } else {
            maxValue = MAX_VALUE;
        }
        calculate();
        validate();
        calcAngleStep();
        fireStateChanged();
    }

    /**
     * Returns the difference between the maximum and minimum value
     * @return the difference between the maximum and minimum value
     */
    public double getRange() {
        return (maxValue - minValue);
    }

    /**
     * Sets the minimum and maximum value for the calculation of the
     * nice minimum and nice maximum values.
     * @param MIN_VALUE
     * @param MAX_VALUE
     */
    public void setRange(final double MIN_VALUE, final double MAX_VALUE) {
        maxValue = MAX_VALUE;
        minValue = MIN_VALUE;
        calculate();
        validate();
        calcAngleStep();
        fireStateChanged();
    }

    /**
     * Returns the current value of the gauge
     * @return the current value of the gauge
     */
    public double getValue() {
        return value;
    }

    /**
     * Sets the current value of the gauge
     * @param VALUE
     */
    public void setValue(final double VALUE) {
        oldValue = value;

        value = VALUE < niceMinValue ? niceMinValue : (VALUE > niceMaxValue ? niceMaxValue : VALUE);

        fireStateChanged();
    }

    /**
     * Returns the old value of the gauge
     * @return the old value of the gauge
     */
    public double getOldValue() {
        return oldValue;
    }

    /**
     * Returns a double that represents the tolerance of the current value compared to the old value where no
     * repaint will happen. e.g.
     *     oldValue        = 1.0
     *     redrawTolerance = 0.5
     *     value           = 1.1     => no repaint
     * @return a double that represents the tolerance of the current value compared to the old value (0.0 - 1.0)
     */
    public double getRedrawTolerance() {
        return redrawTolerance;
    }

    /**
     * Sets the redraw tolerance
     * @param REDRAW_TOLERANCE
     */
    public void setRedrawTolerance(final double REDRAW_TOLERANCE) {
        redrawTolerance = REDRAW_TOLERANCE < 0 ? 0 : (REDRAW_TOLERANCE > 1 ? 1.0 : REDRAW_TOLERANCE);
        redrawFactor    = redrawTolerance * getRange();
        fireStateChanged();
    }

    public double getRedrawFactor() {
        return redrawFactor;
    }

    /**
     * Returns the peak value of the gauge
     * @return the peak value of the gauge
     */
    public double getPeakValue() {
        return peakValue;
    }

    /**
     * Sets the peak value of the gauge
     * @param PEAK_VALUE
     */
    public void setPeakValue(final double PEAK_VALUE) {
        peakValue = PEAK_VALUE;
        fireStateChanged();
    }

    /**
     * Returns true if the peak value is visible
     * @return true if the peak value is visible
     */
    public boolean isPeakValueVisible() {
        return peakValueVisible;
    }

    /**
     * Enables / disables the visibility of the peak value
     * @param PEAK_VALUE_VISIBLE
     */
    public void setPeakValueVisible(final boolean PEAK_VALUE_VISIBLE) {
        peakValueVisible = PEAK_VALUE_VISIBLE;
        fireStateChanged();
    }

    public double getLcdValue() {
        return lcdValue;
    }

    public void setLcdValue(final double LCD_VALUE) {
        lcdValue = LCD_VALUE;
        fireStateChanged();
    }

    /**
     * Returns the value of the lcd threshold
     * @return the value of the lcd threshold
     */
    public double getLcdThreshold() {
        return lcdThreshold;
    }

    /**
     * Sets the lcd threshold to the given value
     * @param LCD_THRESHOLD
     */
    public void setLcdThreshold(final double LCD_THRESHOLD) {
        lcdThreshold = LCD_THRESHOLD;
        fireStateChanged();
    }

    /**
     * Returns true if the lcd threshold indicator is visible
     * @return true if the lcd threshold indicator is visible
     */
    public boolean isLcdThresholdVisible() {
        return lcdThresholdVisible;
    }

    /**
     * Enables/disables the visibility of the lcd threshold indicator
     * @param LCD_THRESHOLD_VISIBLE
     */
    public void setLcdThresholdVisible(final boolean LCD_THRESHOLD_VISIBLE) {
        lcdThresholdVisible = LCD_THRESHOLD_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns true if the lcd threshold behaviour is inverted which means the threshold
     * led will be switched on if the value is below the threshold instead of higher
     * than the threshold.
     * @return true if the threshold behaviour is inverted
     */
    public boolean isLcdThresholdBehaviourInverted() {
        return lcdThresholdBehaviourInverted;
    }

    /**
     * Enables / disables the inversion of the lcd threshold behaviour
     * @param LCD_THRESHOLD_BEHAVIOUR_INVERTED
     */
    public void setLcdThresholdBehaviourInverted(final boolean LCD_THRESHOLD_BEHAVIOUR_INVERTED) {
        lcdThresholdBehaviourInverted = LCD_THRESHOLD_BEHAVIOUR_INVERTED;
        fireStateChanged();
    }

    /**
     * Returns true if the lcd text is blinking
     * @return true if the lcd text is blinking
     */
    public boolean isLcdBlinking() {
        return lcdBlinking;
    }

    /**
     * Enables/disables the blinking of the lcd text
     * @param LCD_BLINKING
     */
    public void setLcdBlinking(final boolean LCD_BLINKING) {
        lcdBlinking = LCD_BLINKING;
        fireStateChanged();
    }

    /**
     * Returns true if the single led bargraph feature is enabled
     * @return true if the single led bargraph feature is enabled
     */
    public boolean isSingleLedBargraphEnabled() {
        return singleLedBargraphEnabled;
    }

    /**
     * Enables / disables the single led bargraph feature of the gauge
     * @param SINGLE_LED_BARGRAPH_ENABLED
     */
    public void setSingleLedBargraphEnabled(final boolean SINGLE_LED_BARGRAPH_ENABLED) {
        singleLedBargraphEnabled = SINGLE_LED_BARGRAPH_ENABLED;
        fireStateChanged();
    }

    /**
     * Returns true if the auto reset to zero feature is enabled.
     * The auto reset to zero feature will automaticaly reset the value
     * to zero after it reached the given value.
     * @return true if the auto reset to zero feature is enabled
     */
    public boolean isAutoResetToZero() {
        return autoResetToZero;
    }

    /**
     * Enables / disables the auto reset to zero feature
     * @param AUTO_RESET_TO_ZERO
     */
    public void setAutoResetToZero(final boolean AUTO_RESET_TO_ZERO) {
        if (niceMinValue > 0 || niceMaxValue < 0) {
            autoResetToZero = false;
        } else {
            autoResetToZero = AUTO_RESET_TO_ZERO;
        }
        fireStateChanged();
    }

    /**
     * Returns true if the frame of the gauge is visible
     * @return true if the frame of the gauge is visible
     */
    public boolean isFrameVisible() {
        return frameVisible;
    }

    /**
     * Enables / disables the visibility of the frame
     * @param FRAME_VISIBLE
     */
    public void setFrameVisible(final boolean FRAME_VISIBLE) {
        frameVisible = FRAME_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns the frame effect
     * @return the frame effect
     */
    public FrameEffect getFrameEffect() {
        return frameEffect;
    }

    /**
     * Sets the frame effect
     * @param FRAME_EFFECT
     */
    public void setFrameEffect(final FrameEffect FRAME_EFFECT) {
        frameEffect = FRAME_EFFECT;
        fireStateChanged();
    }

    /**
     * Returns the color that is used by the SHINY_METAL FrameDesign
     * @return the color that is used by the SHINY_METAL FrameDesign
     */
    public Color getFrameBaseColor() {
        return frameBaseColor;
    }

    /**
     * Sets the color that is used the SHINY_METAL FrameDesign
     * @param FRAME_BASECOLOR
     */
    public void setFrameBaseColor(final Color FRAME_BASECOLOR) {
        frameBaseColor = FRAME_BASECOLOR;
        fireStateChanged();
    }

    /**
     * Returns true if the frameBaseColor will be used to colorize the SHINY_METAL FrameDesign
     * @return true if the frameBaseColor will be used to colorize the SHINY_METAL FrameDesign
     */
    public boolean isFrameBaseColorEnabled() {
        return frameBaseColorEnabled;
    }

    /**
     * Enables / disables the usage of the frameBaseColor to colorize the SHINY_METAL FrameDesign
     * @param FRAME_BASECOLOR_ENABLED
     */
    public void setFrameBaseColorEnabled(final boolean FRAME_BASECOLOR_ENABLED) {
        frameBaseColorEnabled = FRAME_BASECOLOR_ENABLED;
        fireStateChanged();
    }

    /**
     * Returns true if the background of the gauge is visible
     * @return true if the background of the gauge is visible
     */
    public boolean isBackgroundVisible() {
        return backgroundVisible;
    }

    /**
     * Enables / disables the visibility of the gauge background
     * @param BACKGROUND_VISIBLE
     */
    public void setBackgroundVisible(final boolean BACKGROUND_VISIBLE) {
        backgroundVisible = BACKGROUND_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns true if the title of the gauge is visible
     * @return true if the title of the gauge is visible
     */
    public boolean isTitleVisible() {
        return titleVisible;
    }

    /**
     * Enables / disables the visibility of the gauge title
     * @param TITLE_VISIBLE
     */
    public void setTitleVisible(final boolean TITLE_VISIBLE) {
        titleVisible = TITLE_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns true if the unit of the gauge is visible
     * @return true if the unit of the gauge is visible
     */
    public boolean isUnitVisible() {
        return unitVisible;
    }

    /**
     * Enables / disables the visibility of the gauge unit
     * @param UNIT_VISIBLE
     */
    public void setUnitVisible(final boolean UNIT_VISIBLE) {
        unitVisible = UNIT_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns true if the custom font for the title and unit
     * will be used
     * @return true if the custom font for the title and unit will be used
     */
    public boolean isCustomTitleAndUnitFontEnabled() {
        return customTitleAndUnitFontEnabled;
    }

    /**
     * Enables / disables the usage of the custom font for the gauge title and unit
     * @param CUSTOM_TITLE_AND_UNIT_FONT_ENABLED
     */
    public void setCustomTitleAndUnitFontEnabled(final boolean CUSTOM_TITLE_AND_UNIT_FONT_ENABLED) {
        customTitleAndUnitFontEnabled = CUSTOM_TITLE_AND_UNIT_FONT_ENABLED;
        fireStateChanged();
    }

    /**
     * Returns true if the custom layer of the gauge is visible
     * @return true if the custom layer of the gauge is visible
     */
    public boolean isCustomLayerVisible() {
        return customLayerVisible;
    }

    /**
     * Enables / disables the visibility of the gauge custom layer
     * @param CUSTOM_LAYER_VISIBLE
     */
    public void setCustomLayerVisible(final boolean CUSTOM_LAYER_VISIBLE) {
        customLayerVisible = CUSTOM_LAYER_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns true if the threshold led of the gauge is visible (if led available)
     * @return true if the threshold led of the gauge is visible (if led available)
     */
    public boolean isLedVisible() {
        return ledVisible;
    }

    /**
     * Enables / disables the visibility of the gauge threshold led (if led available)
     * @param LED_VISIBLE
     */
    public void setLedVisible(final boolean LED_VISIBLE) {
        ledVisible = LED_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns true if the user led of the gauge is visible (if led available)
     * @return true if the user led of the gauge is visible (if led available)
     */
    public boolean isUserLedVisible() {
        return userLedVisible;
    }

    /**
     * Enables / disables the visibility of the gauge user led (if led available)
     * @param USER_LED_VISIBLE
     */
    public void setUserLedVisible(final boolean USER_LED_VISIBLE) {
        userLedVisible = USER_LED_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns true if the lcd display of the gauge is visible (if lcd available)
     * @return true if the lcd display of the gauge is visible (if lcd available)
     */
    public boolean isLcdVisible() {
        return lcdVisible;
    }

    /**
     * Enables / disables the visibility of the gauge lcd display (if lcd available)
     * @param LCD_VISIBLE
     */
    public void setLcdVisible(final boolean LCD_VISIBLE) {
        lcdVisible = LCD_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns true if the unit in the lcd display of the gauge is visible (if lcd available)
     * @return true if the unit in the lcd display of the gauge is visible (if lcd available)
     */
    public boolean isLcdUnitStringVisible() {
        return lcdUnitStringVisible;
    }

    /**
     * Enables / disables the visibility of the unit string in the lcd display of the gauge (if lcd available)
     * @param LCD_UNIT_STRING_VISIBLE
     */
    public void setLcdUnitStringVisible(final boolean LCD_UNIT_STRING_VISIBLE) {
        lcdUnitStringVisible = LCD_UNIT_STRING_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns true if the lcd value will be displayed in a scientific format (if lcd available)
     * @return true if the lcd value will be displayed in a scientific format (if lcd available)
     */
    public boolean isLcdScientificFormatEnabled() {
        return lcdScientificFormatEnabled;
    }

    /**
     * Enables / disables the scientific format of the lcd value (if lcd available)
     * @param LCD_SCIENTIFIC_FORMAT_ENABLED
     */
    public void setLcdScientificFormatEnabled(final boolean LCD_SCIENTIFIC_FORMAT_ENABLED) {
        lcdScientificFormatEnabled = LCD_SCIENTIFIC_FORMAT_ENABLED;
        fireStateChanged();
    }

    /**
     * Returns true if the value of the lcd display is coupled to the value of the gauge (if lcd available)
     * @return true if the value of the lcd display is coupled to the value of the gauge (if lcd available)
     */
    public boolean isValueCoupled() {
        return valueCoupled;
    }

    /**
     * Enables / disables the coupling of the lcd value and the gauge value (if lcd available)
     * @param VALUE_COUPLED
     */
    public void setValueCoupled(final boolean VALUE_COUPLED) {
        valueCoupled = VALUE_COUPLED;
        fireStateChanged();
    }

    /**
     * Returns true if the lcd display will use the digital font to display the values (if lcd available)
     * @return true if the lcd display will use the digital font to display the values (if lcd available)
     */
    public boolean isDigitalFontEnabled() {
        return digitalFontEnabled;
    }

    /**
     * Enables / disables the usage of the digital font in the lcd display of the gauge (if lcd available)
     * @param DIGITAL_FONT_ENABLED
     */
    public void setDigitalFontEnabled(final boolean DIGITAL_FONT_ENABLED) {
        digitalFontEnabled = DIGITAL_FONT_ENABLED;
        fireStateChanged();
    }

    /**
     * Returns true if the custom font for the unit in the lcd display of the gauge is enabled (if lcd available)
     * @return true if the custom font for the unit in the lcd display of the gauge is enabled (if lcd available)
     */
    public boolean isCustomLcdUnitFontEnabled() {
        return customLcdUnitFontEnabled;
    }

    /**
     * Enables / disables the usage of the custom unit font in the lcd display of the gauge (if lcd available)
     * @param CUSTOM_LCD_UNIT_FONT_ENABLED
     */
    public void setCustomLcdUnitFontEnabled(final boolean CUSTOM_LCD_UNIT_FONT_ENABLED) {
        customLcdUnitFontEnabled = CUSTOM_LCD_UNIT_FONT_ENABLED;
        fireStateChanged();
    }

    /**
     * Returns the number system that will be used to display the current lcd value
     * @return the number system that will be used to display the current lcd value
     */
    public NumberSystem getNumberSystem() {
        return numberSystem;
    }

    /**
     * Sets the number system that will be used to display the current lcd value
     * @param NUMBER_SYSTEM
     */
    public void setNumberSystem(final NumberSystem NUMBER_SYSTEM) {
        numberSystem = NUMBER_SYSTEM;
        fireStateChanged();
    }

    /**
     * Returns true if the foreground (highlight) of the gauge is visible
     * @return true if the foreground (highlight) of the gauge is visible
     */
    public boolean isForegroundVisible() {
        return foregroundVisible;
    }

    /**
     * Enables /disables the visibility of the foreground (highlight) of the gauge
     * @param FOREGROUND_VISIBLE
     */
    public void setForegroundVisible(final boolean FOREGROUND_VISIBLE) {
        foregroundVisible = FOREGROUND_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns true if the minor tickmarks of the gauge dial are visible
     * @return true if the minor tickmarks of the gauge dial are visible
     */
    public boolean isMinorTickmarksVisible() {
        return minorTickmarksVisible;
    }

    /**
     * Enables / disables the visibility of the minor tickmarks of the gauge dial
     * @param MINOR_TICKMARKS_VISIBLE
     */
    public void setMinorTickmarksVisible(final boolean MINOR_TICKMARKS_VISIBLE) {
        minorTickmarksVisible = MINOR_TICKMARKS_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns true if the major tickmarks of the gauge dial are visible
     * @return true if the major tickmarks of the gauge dial are visible
     */
    public boolean isMajorTickmarksVisible() {
        return majorTickmarksVisible;
    }

    /**
     * Enables / disables the visibility of the major tickmarks of the gauge dial
     * @param MAJOR_TICKMARKS_VISIBLE
     */
    public void setMajorTickmarksVisible(final boolean MAJOR_TICKMARKS_VISIBLE) {
        majorTickmarksVisible = MAJOR_TICKMARKS_VISIBLE;
        fireStateChanged();
    }

    public boolean isSectionTickmarksOnly() {
        return sectionTickmarksOnly;
    }

    public void setSectionTickmarksOnly(final boolean SECTION_TICKMARKS_ONLY) {
        sectionTickmarksOnly = SECTION_TICKMARKS_ONLY;
        fireStateChanged();
    }


    /**
     * Returns true if the color for the tickmarks will be taken from the current gauge background color
     * @return true if the color for the tickmarks will be taken from the current gauge background color
     */
    public boolean isTickmarkColorFromThemeEnabled() {
        return tickmarkColorFromThemeEnabled;
    }

    /**
     * Enables / disables the usage of the color from the current gauge background color for the tickmarks
     * @param TICKMARK_COLOR_FROM_THEME_ENABLED
     */
    public void setTickmarkColorFromThemeEnabled(final boolean TICKMARK_COLOR_FROM_THEME_ENABLED) {
        tickmarkColorFromThemeEnabled = TICKMARK_COLOR_FROM_THEME_ENABLED;
        fireStateChanged();
    }

    /**
     * Returns true if the color for the ticklabels will be taken from the current gauge background color
     * @return true if the color for the ticklabels will be taken from the current gauge background color
     */
    public boolean isLabelColorFromThemeEnabled() {
        return labelColorFromThemeEnabled;
    }

    /**
     * Enables / disables the usage of the color from the current gauge background color for the tickmarks
     * @param LABEL_COLOR_FROM_THEME_ENABLED
     */
    public void setLabelColorFromThemeEnabled(final boolean LABEL_COLOR_FROM_THEME_ENABLED) {
        labelColorFromThemeEnabled = LABEL_COLOR_FROM_THEME_ENABLED;
        fireStateChanged();
    }

    /**
     * Returns the orientation of the tickmark labels
     * @return the orientation of the tickmark labels
     */
    public TicklabelOrientation getTicklabelOrientation() {
        return ticklabelOrientation;
    }

    /**
     * Sets the orientation of the tickmark labels
     * @param TICKLABEL_ORIENTATION
     */
    public void setTicklabelOrienatation(final TicklabelOrientation TICKLABEL_ORIENTATION) {
        ticklabelOrientation = TICKLABEL_ORIENTATION;
        fireStateChanged();
    }

    /**
     * Returns true if the tickmarks of the gauge dial are visible
     * @return true if the tickmarks of the gauge dial are visible
     */
    public boolean isTickmarksVisible() {
        return tickmarksVisible;
    }

    /**
     * Enables / disables the visibility of the tickmarks in the gauge dial
     * @param TICKMARKS_VISIBLE
     */
    public void setTickmarksVisible(final boolean TICKMARKS_VISIBLE) {
        tickmarksVisible = TICKMARKS_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns true if the tick labels of the gauge dial are visible
     * @return true if the tick labels of the gauge dial are visible
     */
    public boolean isTicklabelsVisible() {
        return ticklabelsVisible;
    }

    /**
     * Enables / disables the visibility of the ticklabels in the gauge dial
     * @param TICKLABELS_VISIBLE
     */
    public void setTicklabelsVisible(final boolean TICKLABELS_VISIBLE) {
        ticklabelsVisible = TICKLABELS_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns the value of the current threshold of the gauge
     * @return the value of the current threshold of the gauge
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * Sets the value for the threshold of the gauge
     * @param THRESHOLD
     */
    public void setThreshold(final double THRESHOLD) {
        if (Double.compare(THRESHOLD, minValue) >= 0 && Double.compare(THRESHOLD, maxValue) <= 0) {
            threshold = THRESHOLD;
        } else {
            if (THRESHOLD < niceMinValue) {
                threshold = niceMinValue;
            }

            if (THRESHOLD > niceMaxValue) {
                threshold = niceMaxValue;
            }
        }
        fireStateChanged();
    }

    /**
     * Returns true if the threshold indicator of the gauge is visible
     * @return true if the threshold indicator of the gauge is visible
     */
    public boolean isThresholdVisible() {
        return thresholdVisible;
    }

    /**
     * Enables / disables the visibility of the threshold indicator of the gauge
     * @param THRESHOLD_VISIBLE
     */
    public void setThresholdVisible(final boolean THRESHOLD_VISIBLE) {
        thresholdVisible = THRESHOLD_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns true if the threshold behaviour is inverted which means the threshold
     * led will be switched on if the value is below the threshold instead of higher
     * than the threshold.
     * @return true if the threshold behaviour is inverted
     */
    public boolean isThresholdBehaviourInverted() {
        return thresholdBehaviourInverted;
    }

    /**
     * Enables / disables the inversion of the threshold behaviour
     * @param THRESHOLD_BEHAVIOUR_INVERTED
     */
    public void setThresholdBehaviourInverted(final boolean THRESHOLD_BEHAVIOUR_INVERTED) {
        thresholdBehaviourInverted = THRESHOLD_BEHAVIOUR_INVERTED;
        fireStateChanged();
    }

    /**
     * Returns the color definition of the threshold indicator
     * @return the color definition of the threshold indicator
     */
    public ColorDef getThresholdColor() {
        return thresholdColor;
    }

    /**
     * Sets the color definition of the threshold indicator
     * @param THRESHOLD_COLOR
     */
    public void setThresholdColor(final ColorDef THRESHOLD_COLOR) {
        thresholdColor = THRESHOLD_COLOR;
        fireStateChanged();
    }

    /**
     * Returns the custom color definition of the threshold indicator
     * @return the custom color definition of the threshold indicator
     */
    public CustomColorDef getCustomThresholdColor() {
        return customThresholdColor;
    }

    /**
     * Sets the custom color definition of the threshold indicator
     * @param CUSTOM_THRESHOLD_COLOR
     */
    public void setCustomThresholdColor(final CustomColorDef CUSTOM_THRESHOLD_COLOR) {
        customThresholdColor = CUSTOM_THRESHOLD_COLOR;
        fireStateChanged();
    }

    /**
     * Returns the type of the threshold indicator
     * @return the type of the threshold indicator
     */
    public ThresholdType getThresholdType() {
        return thresholdType;
    }

    /**
     * Sets the type of the threshold indicator
     * @param THRESHOLD_TYPE
     */
    public void setThresholdType(final ThresholdType THRESHOLD_TYPE) {
        thresholdType = THRESHOLD_TYPE;
        fireStateChanged();
    }

    /**
     * Returns the minimum measured value of the gauge
     * @return the minimum measured value of the gauge
     */
    public double getMinMeasuredValue() {
        return minMeasuredValue;
    }

    /**
     * Sets the minimum measured value of the gauge to the given value
     * @param MIN_MEASURED_VALUE
     */
    public void setMinMeasuredValue(final double MIN_MEASURED_VALUE) {
        if (Double.compare(MIN_MEASURED_VALUE, niceMinValue) >= 0 && Double.compare(MIN_MEASURED_VALUE, niceMaxValue) <= 0) {
            minMeasuredValue = MIN_MEASURED_VALUE;
        } else {
            if (MIN_MEASURED_VALUE < niceMinValue) {
                minMeasuredValue = niceMinValue;
            }

            if (MIN_MEASURED_VALUE > niceMaxValue) {
                minMeasuredValue = niceMaxValue;
            }
        }
        createRadialShapeOfMeasureValuesArea();
        fireStateChanged();
    }

    /**
     * Resets the minimum measured value to the current value of the gauge
     */
    public void resetMinMeasuredValue() {
        minMeasuredValue = value;
        createRadialShapeOfMeasureValuesArea();
        fireStateChanged();
    }

    /**
     * Resets the minimum measured value of the gauge to the given value
     * @param MIN_MEASURED_VALUE
     */
    public void resetMinMeasuredValue(final double MIN_MEASURED_VALUE) {
        minMeasuredValue = MIN_MEASURED_VALUE < niceMinValue ? niceMinValue : (MIN_MEASURED_VALUE > niceMaxValue ? niceMaxValue : MIN_MEASURED_VALUE);
        createRadialShapeOfMeasureValuesArea();
        fireStateChanged();
    }

    /**
     * Returns true if the minimum measured value indicator of the gauge is visible
     * @return true if the minimum measured value indicator of the gauge is visible
     */
    public boolean isMinMeasuredValueVisible() {
        return minMeasuredValueVisible;
    }

    /**
     * Enables / disables the visibility of the minimum measured value indicator of the gauge
     * @param MIN_MEASURED_VALUE_VISIBLE
     */
    public void setMinMeasuredValueVisible(final boolean MIN_MEASURED_VALUE_VISIBLE) {
        minMeasuredValueVisible = MIN_MEASURED_VALUE_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns the maximum measured value of the gauge
     * @return the maximum measured value of the gauge
     */
    public double getMaxMeasuredValue() {
        return maxMeasuredValue;
    }

    /**
     * Sets the maximum measured value of the gauge to the given value
     * @param MAX_MEASURED_VALUE
     */
    public void setMaxMeasuredValue(final double MAX_MEASURED_VALUE) {
        if (Double.compare(MAX_MEASURED_VALUE, niceMinValue) >= 0 && Double.compare(MAX_MEASURED_VALUE, niceMaxValue) <= 0) {
            maxMeasuredValue = MAX_MEASURED_VALUE;
        } else {
            if (MAX_MEASURED_VALUE < niceMinValue) {
                maxMeasuredValue = niceMinValue;
            }

            if (MAX_MEASURED_VALUE > niceMaxValue) {
                maxMeasuredValue = niceMaxValue;
            }
        }
        createRadialShapeOfMeasureValuesArea();
        fireStateChanged();
    }

    /**
     * Resets the maximum measured value to the current value of the gauge
     */
    public void resetMaxMeasuredValue() {
        maxMeasuredValue = value;
        createRadialShapeOfMeasureValuesArea();
        fireStateChanged();
    }

    /**
     * Resets the maximum measured value of the gauge to the given value
     * @param MAX_MEASURED_VALUE
     */
    public void resetMaxMeasuredValue(final double MAX_MEASURED_VALUE) {
        maxMeasuredValue = MAX_MEASURED_VALUE < niceMinValue ? niceMinValue : (MAX_MEASURED_VALUE > niceMaxValue ? niceMaxValue : MAX_MEASURED_VALUE);
        createRadialShapeOfMeasureValuesArea();
        fireStateChanged();
    }

    /**
     * Returns true if the maximum measured value indicator of the gauge is visible
     * @return true if the maximum measured value indicator of the gauge is visible
     */
    public boolean isMaxMeasuredValueVisible() {
        return maxMeasuredValueVisible;
    }

    /**
     * Enables / disables the visibility of the maximum measured value indicator of the gauge
     * @param MAX_MEASURED_VALUE_VISIBLE
     */
    public void setMaxMeasuredValueVisible(final boolean MAX_MEASURED_VALUE_VISIBLE) {
        maxMeasuredValueVisible = MAX_MEASURED_VALUE_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns true if the range of measured values is visible. The range will be visualized
     * by an area which will be filled with a gradient of colors.
     * @return true if the range of measured values is visible;
     */
    public boolean isRangeOfMeasuredValuesVisible() {
        return rangeOfMeasuredValuesVisible;
    }

    /**
     * Enables / disables the visibility of the range of measured values.
     * @param RANGE_OF_MEASURED_VALUES_VISIBLE
     */
    public void setRangeOfMeasuredValuesVisible(final boolean RANGE_OF_MEASURED_VALUES_VISIBLE) {
        rangeOfMeasuredValuesVisible = RANGE_OF_MEASURED_VALUES_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns the paint object that will be used to fill the area of measured values.
     * @return the paint object that will be used to fill the area of measured values.
     */
    public Paint getRangeOfMeasuredValuesPaint() {
        return rangeOfMeasuredValuesPaint;
    }

    /**
     * Returns the shape that represents the range of measured values
     * @return the shape that represents the range of measured values
     */
    public Shape getRadialShapeOfMeasuredValues() {
        return radialShapeOfMeasuredValues;
    }

    /**
     * Returns the area that represents the range of measured values
     * @return the area that represents the range of measured values
     */
    public Area getRadialAreaOfMeasuredValues() {
        return new Area(radialShapeOfMeasuredValues);
    }

    /**
     * Sets the paint object that will be used to fill the area of measured values to the given paint object.
     * @param RANGE_OF_MEASURED_VALUES_PAINT
     */
    public void setRangeOfMeasuredValuesPaint(final Paint RANGE_OF_MEASURED_VALUES_PAINT) {
        rangeOfMeasuredValuesPaint = RANGE_OF_MEASURED_VALUES_PAINT;
        fireStateChanged();
    }

    /**
     * Returns true if the gauge is collecting all measured values to calculate a histogram
     * @return true if the gauge is collecting all measured values to calculate a histogram
     */
    public boolean isCollectingData() {
        return collectingData;
    }

    /**
     * Enables / disables the collection of measured values
     * @param COLLECTING_DATA
     */
    public void setCollectingData(final boolean COLLECTING_DATA) {
        collectingData = COLLECTING_DATA;
    }

    /**
     * Returns the value where the track of the gauge starts
     * @return the value where the track of the gauge starts
     */
    public double getTrackStart() {
        return trackStart;
    }

    /**
     * Sets the track start value of the gauge to the given value
     * @param TRACK_START
     */
    public void setTrackStart(final double TRACK_START) {
        // check values
        if (Double.compare(TRACK_START, trackStop) == 0) {
            throw new IllegalArgumentException("Track start value cannot equal track stop value");
        }
        trackStart = TRACK_START;
        validate();
        fireStateChanged();
    }

    /**
     * Returns the value of the track section of the gauge
     * @return the value of the track section of the gauge
     */
    public double getTrackSection() {
        return trackSection;
    }

    /**
     * Sets the track section of the gauge to the given value
     * @param TRACK_SECTION
     */
    public void setTrackSection(final double TRACK_SECTION) {
        trackSection = TRACK_SECTION;
        validate();
        fireStateChanged();
    }

    /**
     * Returns the value where the track of the gauge stops
     * @return the value where the track of the gauge stops
     */
    public double getTrackStop() {
        return trackStop;
    }

    /**
     * Sets the track stop value of the gauge to the given value
     * @param TRACK_STOP
     */
    public void setTrackStop(final double TRACK_STOP) {
        // check values
        if (Double.compare(trackStart, TRACK_STOP) == 0) {
            throw new IllegalArgumentException("Track stop value cannot equal track start value");
        }
        trackStop = TRACK_STOP;
        validate();
        fireStateChanged();
    }

    /**
     * Returns true if the track of the gauge is visible
     * @return true if the track of the gauge is visible
     */
    public boolean isTrackVisible() {
        return trackVisible;
    }

    /**
     * Enables / disables the visibility of the three-colored gauge track
     * @param TRACK_VISIBLE
     */
    public void setTrackVisible(final boolean TRACK_VISIBLE) {
        trackVisible = TRACK_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns a list of section objects that will be used to display the sections
     * of a gauge with their different colors
     * @return a list of section objects that represent the sections of the gauge
     */
    public List<Section> getSections() {
        List<Section> sectionsCopy = new ArrayList<Section>(10);
        sectionsCopy.addAll(sections);
        return sectionsCopy;
    }

    /**
     * Sets the sections of the gauge to the given array of section objects
     * @param SECTIONS_ARRAY
     */
    public void setSections(Section... SECTIONS_ARRAY) {
        sections.clear();
        for (Section section : SECTIONS_ARRAY) {
            sections.add(new Section(section.getStart(), section.getStop(), section.getColor()));
        }
        validate();
        fireStateChanged();
    }

    /**
     * Adds the given section object to the list of section objects that will be
     * used to display the sections of a gauge with their different colors
     * @param SECTION
     */
    public void addSection(Section SECTION) {
        sections.add(SECTION);
        fireStateChanged();
    }

    /**
     * Clear the list of sections of the gauge
     */
    public void resetSections() {
        sections.clear();
        fireStateChanged();
    }

    /**
     * Returns true if the sections of the gauge are visible
     * @return true if the sections of the gauge are visible
     */
    public boolean isSectionsVisible() {
        return sectionsVisible;
    }

    /**
     * Enables / disables the sections of the gauge
     * @param SECTIONS_VISIBLE
     */
    public void setSectionsVisible(final boolean SECTIONS_VISIBLE) {
        sectionsVisible = SECTIONS_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns true if the section that contains the current value will be highlighted
     * @return true if the section that contains the current value will be highlighted
     */
    public boolean isHighlightSection() {
        return highlightSection;
    }

    /**
     * Enables / disables the highlighting of the section that contains the current value
     * @param HIGHLIGHT_SECTION
     */
    public void setHighlightSection(final boolean HIGHLIGHT_SECTION) {
        highlightSection = HIGHLIGHT_SECTION;
        fireStateChanged();
    }

    /**
     * Returns a list of section objects that will used to display the areas of
     * a gauge with their colors.
     * @return a list of section objects that will represent the areas of the gauge
     */
    public List<Section> getAreas() {
        List<Section> areasCopy = new ArrayList<Section>(10);
        areasCopy.addAll(areas);
        return areasCopy;
    }

    /**
     * Sets the areas of the gauge to the given array of section objects
     * @param AREAS_ARRAY
     */
    public void setAreas(Section... AREAS_ARRAY) {
        areas.clear();
        for (Section area : AREAS_ARRAY) {
            areas.add(new Section(area.getStart(), area.getStop(), area.getColor()));
        }
        validate();
        fireStateChanged();
    }

    /**
     * Adds the given section object to the list of section objects that will be
     * used to display the areas of a gauge with their different colors.
     * @param AREA
     */
    public void addArea(Section AREA) {
        areas.add(AREA);
    }

    /**
     * Clear the list of areas of the gauge
     */
    public void resetAreas() {
        areas.clear();
    }

    /**
     * Returns true if the areas of the gauge are visible
     * @return true if the areas of the gauges are visible
     */
    public boolean isAreasVisible() {
        return areasVisible;
    }

    /**
     * Enables / disables the visibility of the gauge areas
     * @param AREAS_VISIBLE
     */
    public void setAreasVisible(final boolean AREAS_VISIBLE) {
        areasVisible = AREAS_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns true if the area that contains the current value will be highlighted
     * @return true if the area that contains the current value will be highlighted
     */
    public boolean isHighlightArea() {
        return highlightArea;
    }

    /**
     * Enables / disables the highlighting of the area that contains the current value
     * @param HIGHLIGHT_AREA
     */
    public void setHighlightArea(final boolean HIGHLIGHT_AREA) {
        highlightArea = HIGHLIGHT_AREA;
        fireStateChanged();
    }

    /**
     * Returns a list of section objects that will be used to display
     * to display the tickmark sections of a gauge with their different colors.
     * @return a list of section objects that represents the tickmark sections of the gauge
     */
    public List<Section> getTickmarkSections() {
        List<Section> tickmarkSectionsCopy = new ArrayList<Section>(10);
        tickmarkSectionsCopy.addAll(tickmarkSections);
        return tickmarkSectionsCopy;
    }

    /**
     * Sets the tickmark sections of the gauge to the given array of section objects
     * @param TICKMARK_SECTIONS_ARRAY
     */
    public void setTickmarkSections(final Section... TICKMARK_SECTIONS_ARRAY) {
        tickmarkSections.clear();
        for (Section tickmarkSection : TICKMARK_SECTIONS_ARRAY) {
            tickmarkSections.add(new Section(tickmarkSection.getStart(), tickmarkSection.getStop(), tickmarkSection.getColor()));
        }
        validate();
        fireStateChanged();
    }

    /**
     * Adds the given section object to the list of section objects that will be
     * used to display the tickmark sections of a gauge with their different colors
     * @param TICKMARK_SECTION
     */
    public void addTickmarkSection(final Section TICKMARK_SECTION) {
        tickmarkSections.add(TICKMARK_SECTION);
        fireStateChanged();
    }

    /**
     * Clear the list of tickmark sections of the gauge
     */
    public void resetTickmarkSections() {
        tickmarkSections.clear();
        fireStateChanged();
    }

    /**
     * Returns true if the tickmark sections of the gauge are visible
     * @return true if the tickmark sections of the gauge are visible
     */
    public boolean isTickmarkSectionsVisible() {
        return tickmarkSectionsVisible;
    }

    /**
     * Enables / disables the visibility of the tickmark sections of the gauge
     * @param TICKMARK_SECTIONS_VISIBLE
     */
    public void setTickmarkSectionsVisible(final boolean TICKMARK_SECTIONS_VISIBLE) {
        tickmarkSectionsVisible = TICKMARK_SECTIONS_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns the type of the radial gauge
     * TYPE1 : a quarter gauge (range 90°)
     * TYPE2 : a two quarter gauge (range 180°)
     * TYPE3 : a three quarter gauge (range 270°)
     * TYPE4 : a 300° gauge
     * TYPE5 : a quarter gauge that is rotated by 90°
     * @return the type of the radial gauge
     */
    public GaugeType getGaugeType() {
        return gaugeType;
    }

    /**
     * Sets the radial type of the gauge
     * @param GAUGE_TYPE
     */
    public void setGaugeType(final GaugeType GAUGE_TYPE) {
        gaugeType = GAUGE_TYPE;
        calcAngleStep();
        fireStateChanged();
    }

    /**
     * Returns the range in rad where no tickmarks will be placed in a dial of a radial gauge
     * @return the range in rad where no tickmarks will be placed in a dial of a radial gauge
     */
    public double getFreeAreaAngle() {
        return gaugeType.FREE_AREA_ANGLE;
    }

    /**
     * Returns the stepsize in rad of the gauge dial
     * @return the stepsize in rad of the gauge dial
     */
    public double getAngleStep() {
        return angleStep;
    }

    /**
     * Returns the stepsize in rad of the gauge dial for log scaling
     * @return the stepsize in rad of the gauge dial for log scaling
     */
    public double getLogAngleStep() {
        return logAngleStep;
    }

    /**
     * Returns the angle in rad that will be used to define the start position of the gauge pointer
     * @return the angle in rad that will be used to define the start position of the gauge pointer
     */
    public double getRotationOffset() {
        return gaugeType.ROTATION_OFFSET;
    }

    /**
     * Returns the angle in degree that will be used to define the start position of the gauge dial
     * @return the angle in degree that will be used to define the start position of the gauge dial
     */
    public double getTickmarkOffset() {
        return gaugeType.TICKMARK_OFFSET;
    }

    /**
     * Sets the minimum and maximum value of the gauge dial
     * @param MIN_VALUE
     * @param MAX_VALUE
     */
    public void setMinMaxValues(final double MIN_VALUE, final double MAX_VALUE) {
        this.minValue = MIN_VALUE;
        this.maxValue = MAX_VALUE;
        calculate();
    }

    /**
     * Sets the minimum and maximum values and the number of minor and major tickmarks of the gauge dial
     * @param MIN_VALUE
     * @param MAX_VALUE
     * @param NO_OF_MINOR_TICKS
     * @param NO_OF_MAJOR_TICKS
     */
    public void setMinMaxAndNoOfTicks(final double MIN_VALUE, final double MAX_VALUE, final int NO_OF_MINOR_TICKS,
                                      final int NO_OF_MAJOR_TICKS) {
        this.maxNoOfMinorTicks = NO_OF_MINOR_TICKS;
        this.maxNoOfMajorTicks = NO_OF_MAJOR_TICKS;
        this.minValue = MIN_VALUE;
        this.maxValue = MAX_VALUE;
        calculate();
    }

    /**
     * Returns the maximum number of major tickmarks we're comfortable with
     * @return the maximum number of major tickmarks we're comfortable with
     */
    public int getMaxNoOfMajorTicks() {
        return this.maxNoOfMajorTicks;
    }

    /**
     * Sets the maximum number of major tickmarks we're comfortable with
     * @param MAX_NO_OF_MAJOR_TICKS the maximum number of major tickmarks for the axis
     */
    public void setMaxNoOfMajorTicks(final int MAX_NO_OF_MAJOR_TICKS) {
        if (MAX_NO_OF_MAJOR_TICKS > 20) {
            this.maxNoOfMajorTicks = 20;
        } else if (MAX_NO_OF_MAJOR_TICKS < 2) {
            this.maxNoOfMajorTicks = 2;
        } else {
            this.maxNoOfMajorTicks = MAX_NO_OF_MAJOR_TICKS;
        }
        calculate();
        fireStateChanged();
    }

    /**
     * Returns the maximum number of minor tickmarks we're comfortable with
     * @return the maximum number of minor tickmarks we're comfortable with
     */
    public int getMaxNoOfMinorTicks() {
        return this.maxNoOfMinorTicks;
    }

    /**
     * Sets the maximum number of minor tickmarks we're comfortable with
     * @param MAX_NO_OF_MINOR_TICKS the maxmium number of minor tickmarks for the axis
     */
    public void setMaxNoOfMinorTicks(final int MAX_NO_OF_MINOR_TICKS) {
        if (MAX_NO_OF_MINOR_TICKS > 10) {
            this.maxNoOfMinorTicks = 10;
        } else if (MAX_NO_OF_MINOR_TICKS < 1) {
            this.maxNoOfMinorTicks = 1;
        } else {
            this.maxNoOfMinorTicks = MAX_NO_OF_MINOR_TICKS;
        }
        calculate();
        fireStateChanged();
    }

    /**
     * Returns the tick spacing between the major tickmarks
     * @return the tick spacing between the major tickmarks
     */
    public double getMajorTickSpacing() {
        return majorTickSpacing;
    }

    /**
     * Sets the major tickspacing if niceScale property is disabled
     * @param MAJOR_TICKSPACING
     */
    public void setMajorTickSpacing(final double MAJOR_TICKSPACING) {
        if (!niceScale) {
            majorTickSpacing = MAJOR_TICKSPACING;
            calculate();
            validate();
            fireStateChanged();
        }
    }

    /**
     * Returns the tick spacing between the minor tickmarks
     * @return the tick spacing between the minor tickmarks
     */
    public double getMinorTickSpacing() {
        return minorTickSpacing;
    }

    /**
     * Sets the minor tickspacing if niceScale property is disabled
     * @param MINOR_TICKSPACING
     */
    public void setMinorTickSpacing(final double MINOR_TICKSPACING) {
        if (!niceScale) {
            minorTickSpacing = MINOR_TICKSPACING;
            calculate();
            validate();
            fireStateChanged();
        }
    }

    /**
     * Returns the new minimum value for the gauge dial
     * @return the new minimum value for the gauge dial
     */
    public double getNiceMinValue() {
        return niceMinValue;
    }

    /**
     * Returns the new maximum value for the gauge dial
     * @return the new maximum value for the gauge dial
     */
    public double getNiceMaxValue() {
        return niceMaxValue;
    }

    /**
     * Returns the calculated range of the gauge dial
     * @return the calculated range of the gauge dial
     */
    public double getNiceRange() {
        return this.niceRange;
    }

    /**
     * Returns the background color of the gauge
     * @return the background color of the gauge
     */
    public BackgroundColor getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color of the gauge
     * @param BACKGROUND_COLOR
     */
    public void setBackgroundColor(final BackgroundColor BACKGROUND_COLOR) {
        backgroundColor = BACKGROUND_COLOR;
        fireStateChanged();
    }

    /**
     * Returns the custom background paint of the gauge
     * @return the custom background paint of the gauge
     */
    public Paint getCustomBackground() {
        return customBackground;
    }

    /**
     * Sets the custom background paint of the gauge
     * @param CUSTOM_BACKGROUND
     */
    public void setCustomBackground(final Paint CUSTOM_BACKGROUND) {
        customBackground = CUSTOM_BACKGROUND;
        fireStateChanged();
    }

    /**
     * Returns the color that will be used to render textures like Carbon, Punched_Sheet, Linen etc.
     * @return the color that will be used to render textures liek Carbon, Punched_Sheet, Linen etc.
     */
    public Color getTextureColor() {
        return textureColor;
    }

    /**
     * Sets the color that will be used to render textures like Carbon, Punched_Sheet, Linen etc.
     * @param TEXTURE_COLOR
     */
    public void setTextureColor(final Color TEXTURE_COLOR) {
        textureColor = TEXTURE_COLOR;
        fireStateChanged();
    }

    /**
     * Returns the buffered image that is used as a custom layer of the gauge
     * @return the buffered image that is used as a custom layer of the gauge
     */
    public BufferedImage getCustomLayer() {
        return customLayer;
    }

    /**
     * Sets the given buffered image as the custom layer of the gauge
     * @param CUSTOM_LAYER
     */
    public void setCustomLayer(final BufferedImage CUSTOM_LAYER) {
        if (customLayer != null) {
            customLayer.flush();
        }
        customLayer = CUSTOM_LAYER;
        fireStateChanged();
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
        fireStateChanged();
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
        fireStateChanged();
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
        fireStateChanged();
    }

    /**
     * Returns the frame type of the gauge
     * @return the frame type of the gauge
     */
    public FrameType getFrameType() {
        return frameType;
    }

    /**
     * Sets the given frame type object as frame type of the gauge
     * ROUND
     * SQUARE
     * @param FRAME_TYPE
     */
    public void setFrameType(final FrameType FRAME_TYPE) {
        frameType = FRAME_TYPE;
        fireStateChanged();
    }

    /**
     * Returns the current frame design of the gauge
     * @return the current frame design of the gauge
     */
    public FrameDesign getFrameDesign() {
        return frameDesign;
    }

    /**
     * Sets the given frame design as the custom frame design of the gauge
     * @param FRAME_DESIGN
     */
    public void setFrameDesign(final FrameDesign FRAME_DESIGN) {
        frameDesign = FRAME_DESIGN;
        fireStateChanged();
    }

    /**
     * Returns the custom frame design paint object of the gauge
     * @return the cusotm frame design paint object of the gauge
     */
    public Paint getCustomFrameDesign() {
        return customFrameDesign;
    }

    /**
     * Sets the given paint object as the current custom frame design of the gauge
     * @param CUSTOM_FRAME_DESIGN
     */
    public void setCustomFrameDesign(final Paint CUSTOM_FRAME_DESIGN) {
        customFrameDesign = CUSTOM_FRAME_DESIGN;
        fireStateChanged();
    }

    /**
     * Returns the paint object that will be used to visualize the outer frame of the gauges
     * @return the paint object that will be used to visualize the outer frame of the gauges
     */
    public Paint getOuterFrameColor() {
        return outerFrameColor;
    }

    /**
     * Sets the paint object that will be used to visualize the outer frame of the gauges
     * @param OUTER_FRAME_COLOR
     */
    public void setOuterFrameColor(final Paint OUTER_FRAME_COLOR) {
        outerFrameColor = OUTER_FRAME_COLOR;
        fireStateChanged();
    }

    /**
     * Returns the paint object that will be used to visualize the inner frame of the gauges
     * @return the paint object that will be used to visualize the inner frame of the gauges
     */
    public Paint getInnerFrameColor() {
        return innerFrameColor;
    }

    /**
     * Sets the paint object that will be sued to visualize the inner frame of the gauges
     * @param INNER_FRAME_COLOR
     */
    public void setInnerFrameColor(final Paint INNER_FRAME_COLOR) {
        innerFrameColor = INNER_FRAME_COLOR;
        fireStateChanged();
    }

    /**
     * Returns the led color of the gauge threshold led
     * @return the led color of the gauge threshold led
     */
    public LedColor getLedColor() {
        return ledColor;
    }

    /**
     * Sets the given led color as the color of the gauge threshold led
     * @param LED_COLOR
     */
    public void setLedColor(final LedColor LED_COLOR) {
        ledColor = LED_COLOR;
        fireStateChanged();
    }

    /**
     * Returns the custom led color of the gauge threshold led
     * @return the custom led color of the gauge threshold led
     */
    public CustomLedColor getCustomLedColor() {
        return customLedColor;
    }

    /**
     * Sets the custom color of the gauge threshold led to the given led color
     * @param CUSTOM_LED_COLOR
     */
    public void setCustomLedColor(final CustomLedColor CUSTOM_LED_COLOR) {
        customLedColor = CUSTOM_LED_COLOR;
        fireStateChanged();
    }

    /**
     * Returns the led color of the gauge user led
     * @return the led color of the gauge user led
     */
    public LedColor getUserLedColor() {
        return userLedColor;
    }

    /**
     * Sets the given led color as the color of the gauge user led
     * @param LED_COLOR
     */
    public void setUserLedColor(final LedColor LED_COLOR) {
        userLedColor = LED_COLOR;
        fireStateChanged();
    }

    /**
     * Returns the custom led color of the gauge user led
     * @return the custom led color of the gauge user led
     */
    public CustomLedColor getCustomUserLedColor() {
        return customUserLedColor;
    }

    /**
     * Sets the custom color of the gauge user led to the given led color
     * @param CUSTOM_LED_COLOR
     */
    public void setCustomUserLedColor(final CustomLedColor CUSTOM_LED_COLOR) {
        customUserLedColor = CUSTOM_LED_COLOR;
        fireStateChanged();
    }

    /**
     * Returns the lcd background color of the gauge (if lcd available)
     * @return the lcd background color of the gauge (if lcd available)
     */
    public LcdColor getLcdColor() {
        return lcdColor;
    }

    /**
     * Sets the lcd background color of the gauge to the given lcd color
     * @param LCD_COLOR
     */
    public void setLcdColor(final LcdColor LCD_COLOR) {
        lcdColor = LCD_COLOR;
        fireStateChanged();
    }

    /**
     * Returns the custom lcd background color of the gauge
     * @return the custom lcd background color of the gauge
     */
    public Paint getCustomLcdBackground() {
        return customLcdColor;
    }

    /**
     * Sets the custom lcd background color of the gauge to the given lcd color
     * @param CUSTOM_LCD_COLOR
     */
    public void setCustomLcdBackground(final Paint CUSTOM_LCD_COLOR) {
        customLcdColor = CUSTOM_LCD_COLOR;
        fireStateChanged();
    }

    /**
     * Returns true if the background of the lcd is visible
     * @return true if the background of the lcd is visible
     */
    public boolean isLcdBackgroundVisible() {
        return lcdBackgroundVisible;
    }

    /**
     * Enables/disables the visibility of the lcd background
     * @param LCD_BACKGROUND_VISIBLE
     */
    public void setLcdBackgroundVisible(final boolean LCD_BACKGROUND_VISIBLE) {
        lcdBackgroundVisible = LCD_BACKGROUND_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns the custom lcd foreground color of the gauge
     * @return the custom lcd foreground color of the gauge
     */
    public Color getCustomLcdForeground() {
        return customLcdForegroundColor;
    }

    /**
     * Sets the custom lcd foreground color of the gauge to the given lcd foreground color
     * @param CUSTOM_LCD_FOREGROUND_COLOR
     */
    public void setCustomLcdForeground(final Color CUSTOM_LCD_FOREGROUND_COLOR) {
        customLcdForegroundColor = CUSTOM_LCD_FOREGROUND_COLOR;
        fireStateChanged();
    }

    /**
     * Returns the type of the tickmark that will be used for the minor tickmarks in the gauge dial
     * @return the type of the tickmark that will be used for the minor tickmarks int he gauge dial
     */
    public TickmarkType getMinorTickmarkType() {
        return minorTickmarkType;
    }

    /**
     * Sets the type of tickmark that will be used for the minor tickmarks in the gauge dial to the given tickmarktype
     * @param MINOR_TICKMARK_TYPE
     */
    public void setMinorTickmarkType(final TickmarkType MINOR_TICKMARK_TYPE) {
        minorTickmarkType = MINOR_TICKMARK_TYPE;
        fireStateChanged();
    }

    /**
     * Returns the type of tickmark that will be used for the major tickmarks in the gauge dial
     * @return the type of tickmark that will be used for the major tickmarks in the gauge dial
     */
    public TickmarkType getMajorTickmarkType() {
        return majorTickmarkType;
    }

    /**
     * Sets the type of tickmark that will be used for the major tickmarks in the gauge dial to the given tickmarktype
     * @param MAJOR_TICKMARK_TYPE
     */
    public void setMajorTickmarkType(final TickmarkType MAJOR_TICKMARK_TYPE) {
        majorTickmarkType = MAJOR_TICKMARK_TYPE;
        fireStateChanged();
    }

    /**
     * Returns the numberformat that will be used to format the labels of the gauge dial
     * @return the numberformat that will be used to format the labels of the gauge dial
     */
    public NumberFormat getLabelNumberFormat() {
        return labelNumberFormat;
    }

    /**
     * Sets the number format that will be used to format the labels of the gauge dial
     * @param LABEL_NUMBERFORMAT
     */
    public void setLabelNumberFormat(final NumberFormat LABEL_NUMBERFORMAT) {
        labelNumberFormat = LABEL_NUMBERFORMAT;
        fireStateChanged();
    }

    /**
     * Returns the color that will be used to draw the tickmarks in the gauge dial
     * @return the color that will be used to draw the tickmarks in the gauge dial
     */
    public Color getTickmarkColor() {
        return tickmarkColor;
    }

    /**
     * Sets the color of the tickmarks in the gauge dial to the given color
     * @param TICKMARK_COLOR
     */
    public void setTickmarkColor(final Color TICKMARK_COLOR) {
        tickmarkColor = TICKMARK_COLOR;
        fireStateChanged();
    }

    /**
     * Returns the color that will be used as custom tickmark color in the gauge dial
     * @return the color that will be used as custom tickmark color in the gauge dial
     */
    public Color getCustomTickmarkColor() {
        return customTickmarkColor;
    }

    /**
     * Sets the custom tickmark color of the gauge to the given value
     * @param CUSTOM_TICKMARK_COLOR
     */
    public void setCustomTickmarkColor(final Color CUSTOM_TICKMARK_COLOR) {
        customTickmarkColor = CUSTOM_TICKMARK_COLOR;
        fireStateChanged();
    }

    /**
     * Returns the color that will be used to draw the labels of the gauge dial
     * @return the color that will be used to draw the labels of the gauge dial
     */
    public Color getLabelColor() {
        return labelColor;
    }

    /**
     * Sets the color that will be used to draw the labels of the gauge dial to the given color
     * @param LABEL_COLOR
     */
    public void setLabelColor(final Color LABEL_COLOR) {
        labelColor = LABEL_COLOR;
        fireStateChanged();
    }

    /**
     * Returns the color that will be used as start color of the track gradient
     * @return the color that will be used as start color of the track gradient
     */
    public Color getTrackStartColor() {
        return trackStartColor;
    }

    /**
     * Sets the color that will be used as start color of the track gradient to the given color
     * @param TRACK_START_COLOR
     */
    public void setTrackStartColor(final Color TRACK_START_COLOR) {
        trackStartColor = TRACK_START_COLOR;
        fireStateChanged();
    }

    /**
     * Returns the color that will be used as intermediate color of the track gradient
     * @return the color that will be used as intermediate color of the track gradient
     */
    public Color getTrackSectionColor() {
        return trackSectionColor;
    }

    /**
     * Sets the color that will be used as intermediate color of the track gradient to the given color
     * @param TRACK_SECTION_COLOR
     */
    public void setTrackSectionColor(final Color TRACK_SECTION_COLOR) {
        trackSectionColor = TRACK_SECTION_COLOR;
        fireStateChanged();
    }

    /**
     * Returns the color that will be used as stop color of the track gradient
     * @return the color that will be used as stop color of the track gradient
     */
    public Color getTrackStopColor() {
        return trackStopColor;
    }

    /**
     * Sets the color that will be used as stop color of the track gradient to the given color
     * @param TRACK_STOP_COLOR
     */
    public void setTrackStopColor(final Color TRACK_STOP_COLOR) {
        trackStopColor = TRACK_STOP_COLOR;
        fireStateChanged();
    }

    /**
     * Returns the type of pointer that will be used by the radial gauge
     * @return the type of pointer that will be used by the radial gauge
     */
    public PointerType getPointerType() {
        return pointerType;
    }

    /**
     * Sets the type of pointer that will be used by the radial gauge to the given type
     * @param POINTER_TYPE
     */
    public void setPointerType(final PointerType POINTER_TYPE) {
        pointerType = POINTER_TYPE;
        fireStateChanged();
    }

    /**
     * Returns the color of the pointer of the radial gauge
     * @return the color of the pointer of the radial gauge
     */
    public ColorDef getPointerColor() {
        return pointerColor;
    }

    /**
     * Sets the color of the pointer of the radial gauge to the given color
     * @param POINTER_COLOR
     */
    public void setPointerColor(final ColorDef POINTER_COLOR) {
        pointerColor = POINTER_COLOR;
        fireStateChanged();
    }

    public boolean isPointerShadowVisible() {
        return pointerShadowVisible;
    }

    public void setPointerShadowVisible(final boolean POINTER_SHADOW_VISIBLE) {
        pointerShadowVisible = POINTER_SHADOW_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns the custom color object of the pointer of the radial gauge
     * @return the custom color object of the pointer of the radial gauge
     */
    public CustomColorDef getCustomPointerColorObject() {
        return customPointerColorObject;
    }

    /**
     * Sets the custom color object of the pointer of the radial gauge to the given color object
     * @param CUSTOM_POINTER_COLOR_OBJECT
     */
    public void setCustomPointerColorObject(final CustomColorDef CUSTOM_POINTER_COLOR_OBJECT) {
        customPointerColorObject = CUSTOM_POINTER_COLOR_OBJECT;
        fireStateChanged();
    }

    /**
     * Returns the custom color of the pointer of the radial gauge
     * @return the custom color of the pointer of the radial gauge
     */
    public Color getCustomPointerColor() {
        return customPointerColorObject.COLOR;
    }

    /**
     * Returns the color definition that will be used to visualize the color of the value
     * @return the color definition that will be used to visualize the color of the value
     */
    public ColorDef getValueColor() {
        return valueColor;
    }

    /**
     * Sets the color definition that will be used to visualize the color of the value
     * @param VALUE_COLOR
     */
    public void setValueColor(final ColorDef VALUE_COLOR) {
        valueColor = VALUE_COLOR;
        fireStateChanged();
    }

    /**
     * Returns the color definition that will be used to visualize the custom color of the value
     * @return the color definition that will be used to visualize the custom color of the value
     */
    public CustomColorDef getCustomValueColorObject() {
        return customValueColorObject;
    }

    /**
     * Sets the color definition that will be used to visualize the custom color of the value
     * @param CUSTOM_VALUE_COLOR_OBJECT
     */
    public void setCustomValueColorObject(final CustomColorDef CUSTOM_VALUE_COLOR_OBJECT) {
        customValueColorObject = CUSTOM_VALUE_COLOR_OBJECT;
        fireStateChanged();
    }

    /**
     * Returns the color that will be used to visualize the value
     * @return the color that will be used to visualize the value
     */
    public Color getCustomValueColor() {
        return customValueColorObject.COLOR;
    }

    /**
     * Returns the type of knob that will be used as center knob in a radial gauge
     * @return the type of knob that will be used as center knob in a radial gauge
     */
    public KnobType getKnobType() {
        return knobType;
    }

    /**
     * Sets the type of knob that will be used as center knob in a radial gauge
     * @param KNOB_TYPE
     */
    public void setKnobType(final KnobType KNOB_TYPE) {
        knobType = KNOB_TYPE;
        fireStateChanged();
    }

    /**
     * Returns the style of the center knob of a radial gauge
     * @return the style of the center knob of a radial gauge
     */
    public KnobStyle getKnobStyle() {
        return knobStyle;
    }

    /**
     * Sets the style of the center knob of a radial gauge
     * @param KNOB_STYLE
     */
    public void setKnobStyle(final KnobStyle KNOB_STYLE) {
        knobStyle = KNOB_STYLE;
        fireStateChanged();
    }

    /**
     * Returns true if the posts of the radial gauges are visible
     * @return true if the posts of the radial gauges are visible
     */
    public boolean getPostsVisible() {
        return postsVisible;
    }

    /**
     * Enables/disables the visibility of the posts of the radial gauges
     * @param POSTS_VISIBLE
     */
    public void setPostsVisible(final boolean POSTS_VISIBLE) {
        postsVisible = POSTS_VISIBLE;
        fireStateChanged();
    }

    /**
     * Returns the type of foreground that will be used to visualize the highlight effect in a radial gauge
     * @return the type of foreground that will be used to visualize the highlight effect in a radial gauge
     */
    public ForegroundType getForegroundType() {
        return foregroundType;
    }

    /**
     * Sets the type of foreground that will be used to visualize the highlight effect in a radial gauge to the given type
     * @param FOREGROUND_TYPE
     */
    public void setForegroundType(final ForegroundType FOREGROUND_TYPE) {
        foregroundType = FOREGROUND_TYPE;
        fireStateChanged();
    }

    /**
     * Returns the font that will be used to visualize values in the lcd display
     * @return the font that will be used to visualize values in the lcd display
     */
    public Font getStandardBaseFont() {
        return STANDARD_BASE_FONT;
    }

    /**
     * Returns the font that will be used to visualize infos in the lcd display
     * @return the font that will be used to visualize infos in the lcd display
     */
    public Font getStandardInfoFont() {
        return STANDARD_INFO_FONT;
    }

    /**
     * Returns the font that will be used as digital font in the lcd display
     * @return the font that will be used as digital font in the lcd display
     */
    public Font getDigitalBaseFont() {
        return DIGITAL_BASE_FONT;
    }

    /**
     * Returns the font that will be used to visualize the value on the lcd display
     * @return the font that will be used to visualize the value on the lcd display
     */
    public Font getLcdValueFont() {
        return lcdValueFont;
    }

    /**
     * Sets the font that will be used to visualize the value on the lcd display to the given font
     * @param LCD_VALUE_FONT
     */
    public void setLcdValueFont(final Font LCD_VALUE_FONT) {
        lcdValueFont = LCD_VALUE_FONT;
        fireStateChanged();
    }

    /**
     * Returns the font that will be used to visualize the unit on the lcd display
     * @return the font that will be used to visualize the unit on the lcd display
     */
    public Font getLcdUnitFont() {
        return lcdUnitFont;
    }

    /**
     * Sets the font that will be used to visualize the unit on the lcd display to the given font
     * @param LCD_UNIT_FONT
     */
    public void setLcdUnitFont(final Font LCD_UNIT_FONT) {
        lcdUnitFont = LCD_UNIT_FONT;
        fireStateChanged();
    }

    /**
     * Returns the custom font that will be used to visualize the unit in the lcd display
     * @return the custom font that will be used to visualize the unit in the lcd display
     */
    public Font getCustomLcdUnitFont() {
        return customLcdUnitFont;
    }

    /**
     * Sets the custom font that will be used to visualize the unit in the lcd display to the given font
     * @param CUSTOM_LCD_UNIT_FONT
     */
    public void setCustomLcdUnitFont(final Font CUSTOM_LCD_UNIT_FONT) {
        customLcdUnitFont = CUSTOM_LCD_UNIT_FONT;
        fireStateChanged();
    }

    /**
     * Returns the font that will be used to visualize the info on the lcd display
     * @return the font that will be used to visualize the info on the lcd display
     */
    public Font getLcdInfoFont() {
        return lcdInfoFont;
    }

    /**
     * Sets the font that will be used to visualize the info on the lcd display
     * @param LCD_INFO_FONT
     */
    public void setLcdInfoFont(final Font LCD_INFO_FONT) {
        lcdInfoFont = LCD_INFO_FONT;
        fireStateChanged();
    }

    /**
     * Returns the number of decimals that will be used to visualize values in the lcd display
     * @return the number of decimals that will be used to visualize values in the lcd display
     */
    public int getLcdDecimals() {
        return lcdDecimals;
    }

    /**
     * Sets the number of decimals that will be used to visualize values in the lcd display to the given value
     * @param LCD_DECIMALS
     */
    public void setLcdDecimals(final int LCD_DECIMALS) {
        lcdDecimals = LCD_DECIMALS;
        fireStateChanged();
    }

    /**
     * Returns true if the calculation method will try to calculate
     * nice values for min and max values of the scale.
     * @return true if the calculation method will try to calculate nice values for min and max of the scale
     */
    public boolean isNiceScale() {
        return niceScale;
    }

    /**
     * Enables / disables the calculation of nice values for min and max of the scale
     * @param NICE_SCALE
     */
    public void setNiceScale(final boolean NICE_SCALE) {
        niceScale = NICE_SCALE;
        if (!niceScale) {
            minorTickSpacing = 1;
            majorTickSpacing = 10;
        }
        calculate();
        validate();
        calcAngleStep();
        fireStateChanged();
    }

    /**
     * Returns true if the scaling of the axis is logarithmic
     * @return true if the scaling of the axis is logarithmic
     */
    public boolean isLogScale() {
        return logScale;
    }

    /**
     * Enables / disables logarithmic scaling of the axis
     * @param LOG_SCALE
     */
    public void setLogScale(final boolean LOG_SCALE) {
        logScale = LOG_SCALE;
        fireStateChanged();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Model related">
    /**
     * Validates many important values and adjust them if they do not fit.
     * e.g. If the threshold is higher than the maximum visible value it
     * will set the threshold to the maximum value.
     */
    public void validate() {
        // AutoResetToZero
        if (niceMinValue > 0 || niceMaxValue < 0) {
            autoResetToZero = false;
        }

        // Threshold
        if (threshold < niceMinValue || threshold > niceMaxValue) {
            threshold = niceMaxValue;
        }

        // MinMeasuredValue
        if (minMeasuredValue < niceMinValue || minMeasuredValue > niceMaxValue) {
            minMeasuredValue = value;
        }

        // MaxMeasuredValue
        if (maxMeasuredValue > niceMaxValue || maxMeasuredValue < niceMinValue) {
            maxMeasuredValue = value;
        }

        // PeakValue
        if (peakValue < niceMinValue || peakValue > niceMaxValue) {
            peakValue = value;
        }

        // TrackStart
        if (Double.compare(trackStart, niceMinValue) <= 0 || Double.compare(trackStart, niceMaxValue) >= 0 || Double.compare(trackStart, trackStop) >= 0) {
            trackStart = niceMinValue;
        }

        // TrackStop
        if ((Double.compare(trackStop, niceMinValue)) <= 0 || Double.compare(trackStop, niceMaxValue) >= 0 || Double.compare(trackStop, trackStart) <= 0) {
            trackStop = niceMaxValue;
        }

        // TrackSection
        if (Double.compare(trackSection, niceMinValue) <= 0 || Double.compare(trackSection, niceMaxValue) >= 0 || Double.compare(trackSection, trackStart) <= 0 || Double.compare(trackSection, trackStop) >= 0) {
            trackSection = ((trackStart + (trackStop - trackStart) / 2.0));
        }

        // Areas
        for (Section area : areas) {
            if ((area.getStart() < niceMinValue) || Double.compare(area.getStart(), niceMaxValue) >= 0 || Double.compare(area.getStart(), area.getStop()) >= 0) {
                area.setStart(niceMinValue);
            }

            if (area.getStop() < niceMinValue || area.getStop() > niceMaxValue || Double.compare(area.getStop(), area.getStart()) <= 0) {
                area.setStop(niceMaxValue);
            }

            if (Double.compare(area.getStart(), minValue) == 0) {
                area.setStart(niceMinValue);
            }

            if (Double.compare(area.getStop(), maxValue) == 0) {
                area.setStop(niceMaxValue);
            }
        }

        // Sections
        for (Section section : sections) {
            if ((section.getStart() < niceMinValue) || Double.compare(section.getStart(), niceMaxValue) >= 0 || Double.compare(section.getStart(), section.getStop()) >= 0) {
                section.setStart(niceMinValue);
            }

            if (section.getStop() < niceMinValue || section.getStop() > niceMaxValue || Double.compare(section.getStop(), section.getStart()) <= 0) {
                section.setStop(niceMaxValue);
            }

            if (Double.compare(section.getStart(), minValue) == 0) {
                section.setStart(niceMinValue);
            }

            if (Double.compare(section.getStop(), maxValue) == 0) {
                section.setStop(niceMaxValue);
            }
        }

        // TickmarkSections
        for (Section tickmarkSection : tickmarkSections) {
            if ((tickmarkSection.getStart() < niceMinValue) || Double.compare(tickmarkSection.getStart(), niceMaxValue) >= 0 || Double.compare(tickmarkSection.getStart(), tickmarkSection.getStop()) >= 0) {
                tickmarkSection.setStart(niceMinValue);
            }

            if (tickmarkSection.getStop() < niceMinValue || tickmarkSection.getStop() > niceMaxValue || Double.compare(tickmarkSection.getStop(), tickmarkSection.getStart()) <= 0) {
                tickmarkSection.setStop(niceMaxValue);
            }

            if (Double.compare(tickmarkSection.getStart(), minValue) == 0) {
                tickmarkSection.setStart(niceMinValue);
            }

            if (Double.compare(tickmarkSection.getStop(), maxValue) == 0) {
                tickmarkSection.setStop(niceMaxValue);
            }
        }

        // Recalculate the redrawTolerance
        redrawFactor = redrawTolerance * getRange();

        // Correct the value (needed for init with negative minValue)
        value = value < niceMinValue ? niceMinValue : (value > niceMaxValue ? niceMaxValue : value);
    }

    /**
     * Calculates the stepsize in rad for the given gaugetype and range
     */
    private void calcAngleStep() {
        angleStep = gaugeType.ANGLE_RANGE / range;
        logAngleStep = gaugeType.ANGLE_RANGE / (Util.INSTANCE.logOfBase(BASE, range));
    }

    /**
     * Calculate and update values for majro and minor tick spacing and nice
     * minimum and maximum values on the axis.
     */
    private void calculate() {
        if (niceScale) {
            this.niceRange = calcNiceNumber(maxValue - minValue, false);
            this.majorTickSpacing = calcNiceNumber(niceRange / (maxNoOfMajorTicks - 1), true);
            this.niceMinValue = Math.floor(minValue / majorTickSpacing) * majorTickSpacing;
            this.niceMaxValue = Math.ceil(maxValue / majorTickSpacing) * majorTickSpacing;
            this.minorTickSpacing = calcNiceNumber(majorTickSpacing / (maxNoOfMinorTicks - 1), true);
            this.range = niceMaxValue - niceMinValue;
        } else {
            this.niceRange = (maxValue - minValue);
            this.niceMinValue = minValue;
            this.niceMaxValue = maxValue;
            this.range = this.niceRange;
        }
    }

    /**
     * Returns a "nice" number approximately equal to the range.
     * Rounds the number if ROUND == true.
     * Takes the ceiling if ROUND = false.
     * @param RANGE the value range (maxValue - minValue)
     * @param ROUND whether to round the result or ceil
     * @return a "nice" number to be used for the value range
     */
    private double calcNiceNumber(final double RANGE, final boolean ROUND) {
        final double EXPONENT = Math.floor(Math.log10(RANGE));   // exponent of range
        final double FRACTION = RANGE / Math.pow(10, EXPONENT);  // fractional part of range

        // nice, rounded fraction
        final double NICE_FRACTION;

        if (ROUND) {
            if (FRACTION < 1.5) {
                NICE_FRACTION = 1;
            } else if (FRACTION < 3) {
                NICE_FRACTION = 2;
            } else if (FRACTION < 7) {
                NICE_FRACTION = 5;
            } else {
                NICE_FRACTION = 10;
            }
        } else {
            if (FRACTION <= 1) {
                NICE_FRACTION = 1;
            } else if (FRACTION <= 2) {
                NICE_FRACTION = 2;
            } else if (FRACTION <= 5) {
                NICE_FRACTION = 5;
            } else {
                NICE_FRACTION = 10;
            }
        }

        return NICE_FRACTION * Math.pow(10, EXPONENT);
    }

    private void createRadialShapeOfMeasureValuesArea() {
        if (bounds.width > 1 && bounds.height > 1 && Double.compare(getMinMeasuredValue(), getMaxMeasuredValue()) != 0) {
            final double ANGLE_STEP = Math.toDegrees(getGaugeType().ANGLE_RANGE) / (getMaxValue() - getMinValue());
            final double RADIUS = bounds.width * 0.35f - bounds.height * 0.04f;
            final double FREE_AREA = bounds.width / 2.0 - RADIUS;
            ((Arc2D) radialShapeOfMeasuredValues).setFrame(new Rectangle2D.Double(bounds.x + FREE_AREA, bounds.y + FREE_AREA, 2 * RADIUS, 2 * RADIUS));
            ((Arc2D) radialShapeOfMeasuredValues).setAngleStart(getGaugeType().ORIGIN_CORRECTION - (getMinMeasuredValue() * ANGLE_STEP) + (getMinValue() * ANGLE_STEP));
            ((Arc2D) radialShapeOfMeasuredValues).setAngleExtent(-(getMaxMeasuredValue() - getMinMeasuredValue()) * ANGLE_STEP);
            ((Arc2D) radialShapeOfMeasuredValues).setArcType(Arc2D.PIE);
        }
    }

    /**
     * Resets the model by calling the init() method
     */
    public void reset() {
        init();
    }

    /**
     * Returns a shallow copy of the gauge model
     * @return a shallow copy of the gauge model
     */
    @Override
    public Model clone() {
        try {
            return (Model) super.clone();
        } catch (java.lang.CloneNotSupportedException exception) {
        }
        return new Model();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Listener related">
    /**
     * Adds the given listener to the listener list
     * @param LISTENER
     */
    public void addChangeListener(javax.swing.event.ChangeListener LISTENER) {
        LISTENER_LIST.add(javax.swing.event.ChangeListener.class, LISTENER);
    }

    /**
     * Removes all listeners from the listener list
     * @param LISTENER
     */
    public void removeChangeListener(javax.swing.event.ChangeListener LISTENER) {
        LISTENER_LIST.remove(javax.swing.event.ChangeListener.class, LISTENER);
    }

    /**
     * Fires an state change event every time the data model changes
     */
    protected void fireStateChanged() {
        Object[] listeners = LISTENER_LIST.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == javax.swing.event.ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new javax.swing.event.ChangeEvent(this);
                }
                ((javax.swing.event.ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
            }
        }
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "Model";
    }
}
