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
package eu.hansolo.steelseries.gauges;

import eu.hansolo.steelseries.tools.BackgroundColor;
import eu.hansolo.steelseries.tools.BackgroundImageFactory;
import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.CustomColorDef;
import eu.hansolo.steelseries.tools.CustomLedColor;
import eu.hansolo.steelseries.tools.DesignSet;
import eu.hansolo.steelseries.tools.DisabledImageFactory;
import eu.hansolo.steelseries.tools.ForegroundImageFactory;
import eu.hansolo.steelseries.tools.FrameDesign;
import eu.hansolo.steelseries.tools.FrameEffect;
import eu.hansolo.steelseries.tools.FrameImageFactory;
import eu.hansolo.steelseries.tools.GlowImageFactory;
import eu.hansolo.steelseries.tools.KnobImageFactory;
import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.LcdImageFactory;
import eu.hansolo.steelseries.tools.LedColor;
import eu.hansolo.steelseries.tools.LedImageFactory;
import eu.hansolo.steelseries.tools.Model;
import eu.hansolo.steelseries.tools.NumberFormat;
import eu.hansolo.steelseries.tools.Orientation;
import eu.hansolo.steelseries.tools.PointerImageFactory;
import eu.hansolo.steelseries.tools.Section;
import eu.hansolo.steelseries.tools.ThresholdType;
import eu.hansolo.steelseries.tools.TickmarkImageFactory;
import eu.hansolo.steelseries.tools.TickmarkType;
import eu.hansolo.steelseries.tools.Util;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;


/**
 * The "mother" of most of the gauges in the steelseries library.
 * It contains common methods that are used in most of the gauges, no
 * matter if they are radial or linear.
 * @author hansolo
 */
public abstract class AbstractGauge extends JComponent implements ComponentListener, ActionListener, Serializable {
    // <editor-fold defaultstate="collapsed" desc="Variable declarations">
    private static final long serialVersionUID = 31269L;
    public static final String VALUE_PROPERTY = "value";
    private PropertyChangeSupport propertyChangeSupport;
    protected static final Util UTIL = Util.INSTANCE;
    protected static final FrameImageFactory FRAME_FACTORY = FrameImageFactory.INSTANCE;
    protected static final BackgroundImageFactory BACKGROUND_FACTORY = BackgroundImageFactory.INSTANCE;
    protected static final GlowImageFactory GLOW_FACTORY = GlowImageFactory.INSTANCE;
    protected static final TickmarkImageFactory TICKMARK_FACTORY = TickmarkImageFactory.INSTANCE;
    protected static final LcdImageFactory LCD_FACTORY = LcdImageFactory.INSTANCE;
    protected static final LedImageFactory LED_FACTORY = LedImageFactory.INSTANCE;
    protected static final KnobImageFactory KNOB_FACTORY = KnobImageFactory.INSTANCE;
    protected static final PointerImageFactory POINTER_FACTORY = PointerImageFactory.INSTANCE;
    protected static final ForegroundImageFactory FOREGROUND_FACTORY = ForegroundImageFactory.INSTANCE;
    protected static final DisabledImageFactory DISABLED_FACTORY = DisabledImageFactory.INSTANCE;
    // Initialization
    private boolean initialized;
    // Models
    private volatile Model model;
    private DesignSet design1;
    private DesignSet design2;
    private DesignSet activeDesign;
    // Value related
    private ChangeEvent changeEvent;
    private final EventListenerList LISTENER_LIST = new EventListenerList();
    protected static final String THRESHOLD_PROPERTY = "threshold";
    // Glow related
    private final Timer GLOW_PULSE;
    private float glowAlpha;
    private double glowAlphaIncrement;
    private double glowAlphaCounter;
    // Threshold LED related
    private BufferedImage ledImageOff;
    private BufferedImage ledImageOn;
    private BufferedImage currentLedImage;
    private final Timer LED_BLINKING_TIMER;
    private boolean ledBlinking;
    private boolean ledOn;
    // Custom LED related
    private BufferedImage userLedImageOff;
    private BufferedImage userLedImageOn;
    private BufferedImage currentUserLedImage;
    private final Timer USER_LED_BLINKING_TIMER;
    private boolean userLedBlinking;
    private boolean userLedOn;
    // Peak value related
    private final Timer PEAK_TIMER;
    // Tickmark related
    private boolean customTickmarkLabelsEnabled;
    private ArrayList<Double> customTickmarkLabels;
    // Title and unit related
    private String title;
    private String unitString;
    private Font titleAndUnitFont;
    // Timeline
    private long stdTimeToValue;
    private long rtzTimeToValue;
    private long rtzTimeBackToZero;
    // Orientation
    private Orientation orientation;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public AbstractGauge() {
        super();
        propertyChangeSupport = new PropertyChangeSupport(this);
        initialized = false;
        model = new Model();
        design1 = new DesignSet.Builder().frameDesign(FrameDesign.METAL).frameEffect(FrameEffect.NONE).backgroundColor(BackgroundColor.DARK_GRAY).textureColor(new Color(0x686868)).color(ColorDef.RED).ledColor(LedColor.RED_LED).userLedColor(LedColor.RED_LED).lcdColor(LcdColor.STANDARD_LCD).glowColor(new Color(51, 255, 255)).build();
        design2 = new DesignSet.Builder().frameDesign(FrameDesign.METAL).frameEffect(FrameEffect.NONE).backgroundColor(BackgroundColor.WHITE).textureColor(new Color(0xc3c3c3)).color(ColorDef.RED).ledColor(LedColor.RED_LED).userLedColor(LedColor.RED_LED).lcdColor(LcdColor.STANDARD_LCD).glowColor(new Color(51, 255, 255)).build();
        activeDesign = design1;

        GLOW_PULSE = new Timer(50, this);
        glowAlpha = 1.0f;
        glowAlphaCounter = 1.0;
        glowAlphaIncrement = -0.1;

        ledImageOff = create_LED_Image(200, 0, model.getLedColor());
        ledImageOn = create_LED_Image(200, 1, model.getLedColor());
        currentLedImage = ledImageOff;
        LED_BLINKING_TIMER = new Timer(500, this);
        ledOn = false;
        ledBlinking = false;

        userLedImageOff = create_LED_Image(200, 0, model.getLedColor());
        userLedImageOn = create_LED_Image(200, 1, model.getLedColor());
        currentUserLedImage = userLedImageOff;
        USER_LED_BLINKING_TIMER = new Timer(500, this);
        userLedOn = false;
        userLedBlinking = false;

        PEAK_TIMER = new Timer(1000, this);
        customTickmarkLabelsEnabled = false;
        customTickmarkLabels = new ArrayList<Double>(10);
        title = "Title";
        unitString = "unit";
        titleAndUnitFont = new Font("Verdana", 0, 10);
        stdTimeToValue = 800;
        rtzTimeToValue = 800;
        rtzTimeBackToZero = 1200;
        orientation = Orientation.NORTH;
        addComponentListener(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    /**
     * In the init method all the images will be created.
     * @param WIDTH
     * @param HEIGHT
     * @return a instance of the current gauge
     */
    abstract public AbstractGauge init(final int WIDTH, final int HEIGHT);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getter/Setter">
    /**
     * Returns true if the component is initialized and ready to display
     * @return a boolean that represents the initialzation state
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Sets the state of initialization of the component
     * @param INITIALIZED
     */
    public void setInitialized(final boolean INITIALIZED) {
        initialized = INITIALIZED;
    }

    /**
     * Returns the state model of the gauge
     * @return the state model of the gauge
     */
    public Model getModel() {
        return model;
    }

    /**
     * Sets the state model of the gauge
     * @param MODEL
     */
    public void setModel(final Model MODEL) {
        model = MODEL;
        reInitialize();
    }

    /**
     * Returns the value of the gauge as a double
     * @return the value of the gauge
     */
    public double getValue() {
        return model.getValue();
    }

    /**
     * Sets the value of the gauge. This method is primarly used for
     * static gauges or if you really have measurement results that
     * are occuring within the range of a second. If you have slow
     * changing values you should better use the method setValueAnimated.
     * @param VALUE
     */
    public void setValue(final double VALUE) {
        if (isEnabled()) {
            if (!isLogScale()) {
                model.setValue(VALUE);
            } else {
                if (VALUE > 0) {
                    model.setValue(VALUE);
                } else {
                    model.setValue(1);
                }
            }

            // LED blinking makes only sense when autoResetToZero == OFF
            if (!isAutoResetToZero()) {
                // Check if current value exceeds threshold and activate led as indicator
                if (!model.isThresholdBehaviourInverted()) {
                    if (Double.compare(model.getValue(), model.getThreshold()) >= 0) {
                        if (!LED_BLINKING_TIMER.isRunning()) {
                            LED_BLINKING_TIMER.start();
                            propertyChangeSupport.firePropertyChange(THRESHOLD_PROPERTY, false, true);
                        }
                    } else {
                        LED_BLINKING_TIMER.stop();
                        setCurrentLedImage(getLedImageOff());
                    }
                } else {
                    if (Double.compare(model.getValue(), model.getThreshold()) <= 0) {
                        if (!LED_BLINKING_TIMER.isRunning()) {
                            LED_BLINKING_TIMER.start();
                            propertyChangeSupport.firePropertyChange(THRESHOLD_PROPERTY, false, true);
                        }
                    } else {
                        LED_BLINKING_TIMER.stop();
                        setCurrentLedImage(getLedImageOff());
                    }
                }
            }

            if (model.getValue() > (model.getOldValue() + model.getRange() * model.getRedrawFactor()) ||
                model.getValue() < (model.getOldValue() - model.getRange() * model.getRedrawFactor())) {
            repaint(getInnerBounds());
            }

            fireStateChanged();
            propertyChangeSupport.firePropertyChange(VALUE_PROPERTY, model.getOldValue(), model.getValue());
        }
    }

    /**
     * Returns the minimum value of the measurement
     * range of this gauge.
     * @return a dobule representing the min value the gauge could visualize
     */
    public double getMinValue() {
        return model.getNiceMinValue();
    }

    /**
     * Sets the minimum value of the measurement
     * range of this gauge. This value defines the
     * minimum value the gauge could display.
     * @param MIN_VALUE
     */
    public void setMinValue(final double MIN_VALUE) {
        model.setMinValue(MIN_VALUE);
        reInitialize();
    }

    /**
     * Returns the maximum value of the measurement
     * range of this gauge. This means the gauge could
     * not display values larger than this value.
     * @return Double that represents the maximum value the gauge could display
     */
    public double getMaxValue() {
        return model.getNiceMaxValue();
    }

    /**
     * Sets the maximum value of the measurement
     * range of this gauge. This value defines the
     * maximum value the gauge could display.
     * It has nothing to do with MaxMeasuredValue,
     * which represents the max. value that was
     * measured since the last reset of MaxMeasuredValue
     * @param MAX_VALUE
     */
    public void setMaxValue(final double MAX_VALUE) {
        model.setMaxValue(MAX_VALUE);
        reInitialize();
    }

    /**
     * Returns the peak value which is the last
     * value that was set before the current one
     * This feature is used in the bargraphs
     * @return a double that represents the peak value
     */
    public double getPeakValue() {
        return model.getPeakValue();
    }

    /**
     * Sets the peak value of the gauge. This method will
     * be used in the bargraph gauges to visualize the last
     * displayed value
     * @param PEAK_VALUE
     */
    public void setPeakValue(final double PEAK_VALUE) {
        model.setPeakValue(PEAK_VALUE);
    }

    /**
     * Returns true if the last measured value (peak value)
     * is visible and will be painted.
     * @return the visibility of the peak value
     */
    public boolean isPeakValueVisible() {
        return model.isPeakValueVisible();
    }

    /**
     * Sets the visbility of the peak value which
     * is the last measured value.
     * @param PEAK_VALUE_VISIBLE
     */
    public void setPeakValueVisible(final boolean PEAK_VALUE_VISIBLE) {
        model.setPeakValueVisible(PEAK_VALUE_VISIBLE);
    }

    /**
     * Returns true if the gauge will be reseted to
     * zero after each value.
     * Means if you set a value the pointer will
     * move to this value and after it reached the
     * given value it will return back to zero.
     * @return true if the value will be set to
     * zero automaticaly
     */
    public boolean isAutoResetToZero() {
        return model.isAutoResetToZero();
    }

    /**
     * Enables/disables the mode where the gauge
     * will return to zero after a value was set.
     * Means if you set a value the pointer will
     * move to this value and after it reached the
     * given value it will return back to zero.
     * @param AUTO_RESET_TO_ZERO
     */
    public void setAutoResetToZero(final boolean AUTO_RESET_TO_ZERO) {
        model.setAutoResetToZero(AUTO_RESET_TO_ZERO);

        if (model.isAutoResetToZero()) {
            setThresholdVisible(false);
            setLedVisible(false);
        }
    }

    /**
     * Returns the value that is defined as a threshold.
     * If the current value of the gauge exceeds this
     * threshold, a event will be fired and the led will
     * start blinking (if the led is visible).
     * @return the threshold value where the led starts blinking
     */
    public double getThreshold() {
        return model.getThreshold();
    }

    /**
     * Sets the given value as the threshold.
     * If the current value of the gauge exceeds this
     * threshold, a event will be fired and the led will
     * start blinking (if the led is visible).
     * @param THRESHOLD
     */
    public void setThreshold(final double THRESHOLD) {
        model.setThreshold(THRESHOLD);
        repaint(getInnerBounds());
    }

    /**
     * Returns the visibility of the threshold indicator.
     * The value of the threshold will be visualized by
     * a small red triangle that points on the threshold
     * value.
     * @return true if the threshold indicator is visible
     */
    public boolean isThresholdVisible() {
        return model.isThresholdVisible();
    }

    /**
     * Sets the visibility of the threshold indicator.
     * The value of the threshold will be visualized by
     * a small red triangle that points on the threshold
     * value.
     * @param THRESHOLD_VISIBLE
     */
    public void setThresholdVisible(final boolean THRESHOLD_VISIBLE) {
        model.setThresholdVisible(THRESHOLD_VISIBLE);
        repaint(getInnerBounds());
    }

    /**
     * Returns true if the threshold behaviour is inverted which means the threshold
     * led will be switched on if the value is below the threshold instead of higher
     * than the threshold.
     * @return true if the threshold behaviour is inverted
     */
    public boolean isThresholdBehaviourInverted() {
        return model.isThresholdBehaviourInverted();
    }

    /**
     * Enables / disables the inversion of the threshold behaviour
     * @param THRESHOLD_BEHAVIOUR_INVERTED
     */
    public void setThresholdBehaviourInverted(final boolean THRESHOLD_BEHAVIOUR_INVERTED) {
        model.setThresholdBehaviourInverted(THRESHOLD_BEHAVIOUR_INVERTED);
        repaint(getInnerBounds());
    }

    /**
     * Returns the visiblity of the threshold led.
     * @return a boolean that indicates if the led is visible
     */
    public boolean isLedVisible() {
        return model.isLedVisible();
    }

    /**
     * Sets the visibility of the threshold led.
     * @param LED_VISIBLE
     */
    public void setLedVisible(final boolean LED_VISIBLE) {
        model.setLedVisible(LED_VISIBLE);
        repaint(getInnerBounds());
    }

    /**
     * Returns the visiblity of the user led.
     * @return a boolean that indicates if the user led is visible
     */
    public boolean isUserLedVisible() {
        return model.isUserLedVisible();
    }

    /**
     * Sets the visibility of the user led.
     * @param USER_LED_VISIBLE
     */
    public void setUserLedVisible(final boolean USER_LED_VISIBLE) {
        model.setUserLedVisible(USER_LED_VISIBLE);
        repaint(getInnerBounds());
    }

    public DesignSet getDesign1() {
        return design1;
    }

    public void setDesign1(final DesignSet DESIGN1) {
        design1 = DESIGN1;
    }

    public DesignSet getDesign2() {
        return design2;
    }

    public void setDesign2(final DesignSet DESIGN2) {
        design2 = DESIGN2;
    }

    public DesignSet getActiveDesign() {
        return activeDesign;
    }

    public void setActiveDesign(final DesignSet DESIGN_SET) {
        activeDesign = DESIGN_SET;

        if (DESIGN_SET.getOuterFrameColor() != null) {
            model.setOuterFrameColor(DESIGN_SET.getOuterFrameColor());
        }

        if (DESIGN_SET.getInnerFrameColor() != null) {
            model.setInnerFrameColor(DESIGN_SET.getInnerFrameColor());
        }

        if (DESIGN_SET.getFrameDesign() != null) {
            model.setFrameDesign(DESIGN_SET.getFrameDesign());
        }

        if (DESIGN_SET.getFrameEffect() != null) {
            model.setFrameEffect(DESIGN_SET.getFrameEffect());
        }

        if (DESIGN_SET.getTextureColor() != null) {
            model.setTextureColor(DESIGN_SET.getTextureColor());
        }

        if (DESIGN_SET.getBackgroundColor() != null) {
            model.setBackgroundColor(DESIGN_SET.getBackgroundColor());
        }

        if (DESIGN_SET.getColor() != null) {
            model.setPointerColor(DESIGN_SET.getColor());
            model.setValueColor(DESIGN_SET.getColor());
        }

        if (DESIGN_SET.getLedColor() != null) {
            model.setLedColor(DESIGN_SET.getLedColor());
            final boolean LED_WAS_ON = currentLedImage.equals(ledImageOn) ? true : false;

            switch (getOrientation()) {
                case HORIZONTAL:
                    recreateLedImages(getHeight());
                    break;
                case VERTICAL:
                    recreateLedImages(getWidth());
                    break;
                default:
                    recreateLedImages();
                    break;
            }
            if (currentLedImage != null) {
                currentLedImage.flush();
            }
            currentLedImage = LED_WAS_ON == true ? ledImageOn : ledImageOff;
        }

        if (DESIGN_SET.getUserLedColor() != null) {
            model.setUserLedColor(DESIGN_SET.getUserLedColor());
            final boolean USER_LED_WAS_ON = currentUserLedImage.equals(userLedImageOn) ? true : false;

            switch (getOrientation()) {
                case HORIZONTAL:
                    recreateUserLedImages(getHeight());
                    break;
                case VERTICAL:
                    recreateUserLedImages(getWidth());
                    break;
                default:
                    recreateUserLedImages();
                    break;
            }
            if (currentUserLedImage != null) {
                currentUserLedImage.flush();
            }
            currentUserLedImage = USER_LED_WAS_ON == true ? userLedImageOn : userLedImageOff;
        }

        if (DESIGN_SET.getLcdColor() != null) {
            model.setLcdColor(DESIGN_SET.getLcdColor());
        }

        if (DESIGN_SET.getGlowColor() != null) {
            model.setGlowColor(DESIGN_SET.getGlowColor());
        }


        if (DESIGN_SET.getColor() != null) {
            model.setPointerColor(DESIGN_SET.getColor());
        }

        if (DESIGN_SET.getKnobStyle() != null) {
            model.setKnobStyle(DESIGN_SET.getKnobStyle());
        }

        if (DESIGN_SET.getColor() != null) {
            model.setValueColor(DESIGN_SET.getColor());
        }

        reInitialize();
    }

    abstract void toggleDesign();

    abstract Point2D getLedPosition();

    abstract void setLedPosition(final double X, final double Y);

    abstract Point2D getUserLedPosition();

    abstract void setUserLedPosition(final double X, final double Y);

    /**
     * Returns the color of the threshold led.
     * The LedColor is not a standard color but defines a
     * color scheme for the led. The default ledcolor is RED
     * @return the selected the color for the led
     */
    public LedColor getLedColor() {
        return model.getLedColor();
    }

    /**
     * Sets the color of the threshold led dependend on the orientation of
     * a component. This is only important for the linear gauges where the width
     * and the height are different.
     * The LedColor is not a standard color but defines a
     * color scheme for the led. The default ledcolor is RED
     * @param LED_COLOR
     */
    public void setLedColor(final LedColor LED_COLOR) {
        model.setLedColor(LED_COLOR);
        final boolean LED_WAS_ON = currentLedImage.equals(ledImageOn) ? true : false;

        switch (getOrientation()) {
            case HORIZONTAL:
                recreateLedImages(getHeight());
                break;
            case VERTICAL:
                recreateLedImages(getWidth());
                break;
            default:
                recreateLedImages();
                break;
        }
        if (currentLedImage != null) {
            currentLedImage.flush();
        }
        currentLedImage = LED_WAS_ON == true ? ledImageOn : ledImageOff;

        repaint(getInnerBounds());
    }

    /**
     * Returns the color from which the custom ledcolor will be calculated
     * @return the color from which the custom ledcolor will be calculated
     */
    public Color getCustomLedColor() {
        return model.getCustomLedColor().COLOR;
    }

    /**
     * Sets the color from which the custom ledcolor will be calculated
     * @param COLOR
     */
    public void setCustomLedColor(final Color COLOR) {
        model.setCustomLedColor(new CustomLedColor(COLOR));
        final boolean LED_WAS_ON = currentLedImage.equals(ledImageOn) ? true : false;

        switch (getOrientation()) {
            case HORIZONTAL:
                recreateLedImages(getHeight());
                break;
            case VERTICAL:
                recreateLedImages(getWidth());
                break;
            default:
                recreateLedImages();
                break;
        }

        if (currentLedImage != null) {
            currentLedImage.flush();
        }
        currentLedImage = LED_WAS_ON == true ? ledImageOn : ledImageOff;

        repaint(getInnerBounds());
    }

    /**
     * Returns the state of the threshold led.
     * The led could blink which will be triggered by a javax.swing.Timer
     * that triggers every 500 ms. The blinking will be done by switching
     * between two images.
     * @return true if the led is blinking
     */
    public boolean isLedBlinking() {
        return ledBlinking;
    }

    /**
     * Sets the state of the threshold led.
     * The led could blink which will be triggered by a javax.swing.Timer
     * that triggers every 500 ms. The blinking will be done by switching
     * between two images.
     * @param LED_BLINKING
     */
    public void setLedBlinking(final boolean LED_BLINKING) {
        ledBlinking = LED_BLINKING;
        if (LED_BLINKING) {
            LED_BLINKING_TIMER.start();
        } else {
            setCurrentLedImage(getLedImageOff());
            LED_BLINKING_TIMER.stop();
        }
    }

    /**
     * Returns the image of the switched on threshold led
     * with the currently active ledcolor.
     * @return the image of the led with the state active
     * and the selected led color
     */
    protected BufferedImage getLedImageOn() {
        return this.ledImageOn;
    }

    /**
     * Returns the image of the switched off threshold led
     * with the currently active ledcolor.
     * @return the image of the led with the state inactive
     * and the selected led color
     */
    protected BufferedImage getLedImageOff() {
        return this.ledImageOff;
    }

    /**
     * Recreates the current threshold led images due to the size of the component
     */
    protected void recreateLedImages() {
        recreateLedImages(getInnerBounds().width);
    }

    /**
     * Recreates the current threshold led images due to the given width
     * @param SIZE
     */
    protected void recreateLedImages(final int SIZE) {
        if (ledImageOff != null) {
            ledImageOff.flush();
        }
        ledImageOff = create_LED_Image(SIZE, 0, model.getLedColor());

        if (ledImageOn != null) {
            ledImageOn.flush();
        }
        ledImageOn = create_LED_Image(SIZE, 1, model.getLedColor());
    }

    /**
     * Returns the image of the currently used led image.
     * @return the led image at the moment (depends on blinking)
     */
    protected BufferedImage getCurrentLedImage() {
        return this.currentLedImage;
    }

    /**
     * Sets the image of the currently used led image.
     * @param CURRENT_LED_IMAGE
     */
    protected void setCurrentLedImage(final BufferedImage CURRENT_LED_IMAGE) {
        if (currentLedImage != null) {
            currentLedImage.flush();
        }
        currentLedImage = CURRENT_LED_IMAGE;
        repaint(getInnerBounds());
    }

    /**
     * Returns the current state of the threshold led
     * @return a boolean that represents the state of the threshold led
     */
    protected boolean isLedOn() {
        return this.ledOn;
    }

    /**
     * Returns the color of the user led.
     * The LedColor is not a standard color but defines a
     * color scheme for the led. The default ledcolor is RED
     * @return the selected the color for the led
     */
    public LedColor getUserLedColor() {
        return model.getUserLedColor();
    }

    /**
     * Sets the color of the user led dependend on the orientation of
     * a component. This is only important for the linear gauges where the width
     * and the height are different.
     * The LedColor is not a standard color but defines a
     * color scheme for the led. The default ledcolor is RED
     * @param LED_COLOR
     */
    public void setUserLedColor(final LedColor LED_COLOR) {
        model.setUserLedColor(LED_COLOR);
        final boolean LED_WAS_ON = currentUserLedImage.equals(userLedImageOn) ? true : false;

        switch (getOrientation()) {
            case HORIZONTAL:
                recreateUserLedImages(getHeight());
                break;
            case VERTICAL:
                recreateUserLedImages(getWidth());
                break;
            default:
                recreateUserLedImages();
                break;
        }
        if (currentUserLedImage != null) {
            currentUserLedImage.flush();
        }
        currentUserLedImage = LED_WAS_ON == true ? userLedImageOn : userLedImageOff;

        repaint(getInnerBounds());
    }

    /**
     * Returns the color from which the custom user ledcolor will be calculated
     * @return the color from which the custom user ledcolor will be calculated
     */
    public Color getCustomUserLedColor() {
        return model.getCustomUserLedColor().COLOR;
    }

    /**
     * Sets the color from which the custom user ledcolor will be calculated
     * @param COLOR
     */
    public void setCustomUserLedColor(final Color COLOR) {
        model.setCustomUserLedColor(new CustomLedColor(COLOR));
        final boolean LED_WAS_ON = currentUserLedImage.equals(ledImageOn) ? true : false;

        switch (getOrientation()) {
            case HORIZONTAL:
                recreateUserLedImages(getHeight());
                break;
            case VERTICAL:
                recreateUserLedImages(getWidth());
                break;
            default:
                recreateUserLedImages();
                break;
        }

        if (currentUserLedImage != null) {
            currentUserLedImage.flush();
        }
        currentUserLedImage = LED_WAS_ON == true ? userLedImageOn : userLedImageOff;

        repaint(getInnerBounds());
    }

    /**
     * Returns the state of the user led.
     * The led could blink which will be triggered by a javax.swing.Timer
     * that triggers every 500 ms. The blinking will be done by switching
     * between two images.
     * @return true if the led is blinking
     */
    public boolean isUserLedBlinking() {
        return this.userLedBlinking;
    }

    /**
     * Sets the state of the user led.
     * The led could blink which will be triggered by a javax.swing.Timer
     * that triggers every 500 ms. The blinking will be done by switching
     * between two images.
     * @param USER_LED_BLINKING
     */
    public void setUserLedBlinking(final boolean USER_LED_BLINKING) {
        this.userLedBlinking = USER_LED_BLINKING;
        if (USER_LED_BLINKING) {
            USER_LED_BLINKING_TIMER.start();
        } else {
            setCurrentUserLedImage(getUserLedImageOff());
            USER_LED_BLINKING_TIMER.stop();
        }
    }

    /**
     * Returns the image of the switched on user led
     * with the currently active ledcolor.
     * @return the image of the led with the state active
     * and the selected led color
     */
    protected BufferedImage getUserLedImageOn() {
        return this.userLedImageOn;
    }

    /**
     * Returns the image of the switched off user led
     * with the currently active ledcolor.
     * @return the image of the led with the state inactive
     * and the selected led color
     */
    protected BufferedImage getUserLedImageOff() {
        return this.userLedImageOff;
    }

    /**
     * Recreates the current user led images due to the size of the component
     */
    protected void recreateUserLedImages() {
        recreateUserLedImages(getInnerBounds().width);
    }

    /**
     * Recreates the current user led images due to the given width
     * @param SIZE
     */
    protected void recreateUserLedImages(final int SIZE) {
        if (userLedImageOff != null) {
            userLedImageOff.flush();
        }
        userLedImageOff = create_LED_Image(SIZE, 0, model.getUserLedColor());

        if (userLedImageOn != null) {
            userLedImageOn.flush();
        }
        userLedImageOn = create_LED_Image(SIZE, 1, model.getUserLedColor());
    }

    /**
     * Returns the image of the currently used user led image.
     * @return the led image at the moment (depends on blinking)
     */
    protected BufferedImage getCurrentUserLedImage() {
        return this.currentUserLedImage;
    }

    /**
     * Sets the image of the currently used user led image.
     * @param CURRENT_USER_LED_IMAGE
     */
    protected void setCurrentUserLedImage(final BufferedImage CURRENT_USER_LED_IMAGE) {
        if (currentUserLedImage != null) {
            currentUserLedImage.flush();
        }
        currentUserLedImage = CURRENT_USER_LED_IMAGE;
        repaint(getInnerBounds());
    }

    /**
     * Returns the current state of the user led
     * @return a boolean that represents the state of the threshold led
     */
    public boolean isUserLedOn() {
        return this.userLedOn;
    }

    /**
     * Switches the user led on or off
     * @param USER_LED_ON
     */
    public void setUserLedOn(final boolean USER_LED_ON) {
        this.userLedOn = USER_LED_ON;
        if (userLedOn) {
            setCurrentUserLedImage(userLedImageOn);
        } else {
            setCurrentUserLedImage(userLedImageOff);
        }
    }

    /**
     * Returns the lowest measured value.
     * On every move of the bar/pointer the lowest value
     * will be stored in the minMeasuredValue variable.
     * @return a double representing the min measure value
     */
    public double getMinMeasuredValue() {
        return model.getMinMeasuredValue();
    }

    /**
     * Sets the lowest value that was measured
     * On every move of the bar/pointer the lowest value
     * will be stored in the minMeasuredValue variable.
     * @param MIN_MEASURED_VALUE
     */
    protected void setMinMeasuredValue(final double MIN_MEASURED_VALUE) {
        model.setMinMeasuredValue(MIN_MEASURED_VALUE);
        repaint(getInnerBounds());
    }

    /**
     * Returns the visibility of the minMeasuredValue indicator.
     * The lowest value that was measured by the gauge will
     * be visualized by a little blue triangle.
     * @return a boolean that indicates if the min measured value image is visible
     */
    public boolean isMinMeasuredValueVisible() {
        return model.isMinMeasuredValueVisible();
    }

    /**
     * Sets the visibility of the minMeasuredValue indicator.
     * The lowest value that was measured by the gauge will
     * be visualized by a little blue triangle.
     * @param MIN_MEASURED_VALUE_VISIBLE
     */
    public void setMinMeasuredValueVisible(final boolean MIN_MEASURED_VALUE_VISIBLE) {
        model.setMinMeasuredValueVisible(MIN_MEASURED_VALUE_VISIBLE);
        repaint(getInnerBounds());
    }

    /**
     * Resets the minMeasureValue variable to the maximum value
     * that the gauge could display. So on the next move of the
     * pointer/bar the indicator will be set to the pointer/bar
     * position again.
     */
    public void resetMinMeasuredValue() {
        model.resetMinMeasuredValue();
        repaint(getInnerBounds());
    }

    /**
     * Resets the minMeasuredValue variable to the given value.
     * So on the next move of the pointer/bar the indicator will
     * be set to the pointer/bar position again.
     * @param VALUE
     */
    public void resetMinMeasuredValue(final double VALUE) {
        model.resetMinMeasuredValue(VALUE);
        repaint(getInnerBounds());
    }

    /**
     * Returns the biggest measured value.
     * On every move of the bar/pointer the biggest value
     * will be stored in the maxMeasuredValue variable.
     * @return a double representing the max measured value
     */
    public double getMaxMeasuredValue() {
        return model.getMaxMeasuredValue();
    }

    /**
     * Sets the highest value that was measured
     * On every move of the bar/pointer the highest value
     * will be stored in the maxMeasuredValue variable.
     * @param MAX_MEASURED_VALUE
     */
    protected void setMaxMeasuredValue(final double MAX_MEASURED_VALUE) {
        model.setMaxMeasuredValue(MAX_MEASURED_VALUE);
        repaint(getInnerBounds());
    }

    /**
     * Returns the visibility of the maxMeasuredValue indicator.
     * The biggest value that was measured by the gauge will
     * be visualized by a little red triangle.
     * @return a boolean that indicates if the max measured value image is visible
     */
    public boolean isMaxMeasuredValueVisible() {
        return model.isMaxMeasuredValueVisible();
    }

    /**
     * Sets the visibility of the maxMeasuredValue indicator.
     * The biggest value that was measured by the gauge will
     * be visualized by a little red triangle.
     * @param MAX_MEASURED_VALUE_VISIBLE
     */
    public void setMaxMeasuredValueVisible(final boolean MAX_MEASURED_VALUE_VISIBLE) {
        model.setMaxMeasuredValueVisible(MAX_MEASURED_VALUE_VISIBLE);
        repaint(getInnerBounds());
    }

    /**
     * Resets the maxMeasureValue variable to the minimum value
     * that the gauge could display. So on the next move of the
     * pointer/bar the indicator will be set to the pointer/bar
     * position again.
     */
    public void resetMaxMeasuredValue() {
        model.resetMaxMeasuredValue();
        repaint(getInnerBounds());
    }

    /**
     * Resets the maxMeasuredValue variable to the given value.
     * So on the next move of the pointer/bar the indicator will
     * be set to the pointer/bar position again.
     * @param VALUE
     */
    public void resetMaxMeasuredValue(final double VALUE) {
        model.resetMaxMeasuredValue(VALUE);
        repaint(getInnerBounds());
    }

    /**
     * Returns the time in milliseconds that the pointer/bar/led needs to move from
     * the minimum value of the gauge to the maximum of the gauge in standard mode.
     * The minimum time is 250 ms and the maximum time is 5000 ms.
     * @return time in ms that the pointer/bar/led needs to move from minValue to maxValue in standard mode
     */
    public long getStdTimeToValue() {
        return this.stdTimeToValue;
    }

    /**
     * Sets the time in milliseconds that the pointer/bar/led needs to move from
     * the minimum value of the gauge to the maximum of the gauge in standard mode.
     * The minimum time is 250 ms and the maximum time is 5000 ms.
     * @param STD_TIME_TO_VALUE
     */
    public void setStdTimeToValue(final long STD_TIME_TO_VALUE) {
        stdTimeToValue = STD_TIME_TO_VALUE < 250 ? 250 : (STD_TIME_TO_VALUE > 5000 ? 5000 : STD_TIME_TO_VALUE);
    }

    /**
     * Returns the time in milliseconds that the pointer/bar/led needs to move from
     * the minimum value of the gauge to the maximum of the gauge in autoreturn to zero mode.
     * The minimum time is 250 ms and the maximum time is 5000 ms.
     * @return time in ms that the pointer/bar/led needs to move from minValue to maxValue in autoreturn to zero mode
     */
    public long getRtzTimeToValue() {
        return this.rtzTimeToValue;
    }

    /**
     * Sets the time in milliseconds that the pointer/bar/led needs to move from
     * the minimum value of the gauge to the maximum of the gauge in autoreturn to zero mode.
     * The minimum time is 250 ms and the maximum time is 5000 ms.
     * @param RTZ_TIME_TO_VALUE
     */
    public void setRtzTimeToValue(final long RTZ_TIME_TO_VALUE) {
        rtzTimeToValue = RTZ_TIME_TO_VALUE < 250 ? 250 : (RTZ_TIME_TO_VALUE > 5000 ? 5000 : RTZ_TIME_TO_VALUE);
    }

    /**
     * Returns the time in milliseconds that the pointer/bar/led needs back from the value to zero
     * in autoreturn to zero mode. The minimum time is 250 ms and the maximum time is 5000 ms.
     * @return the time in ms that the pointer/bar/led needs to move back from the value to zero
     */
    public long getRtzTimeBackToZero() {
        return this.rtzTimeBackToZero;
    }

    /**
     * Sets the time in milliseconds that the pointer/bar/led needs to move back from the value
     * to zero in autoreturn to zero mode. The minimum time is 250 ms and the maximum time is 5000 ms.
     * @param RTZ_TIME_BACK_TO_ZERO
     */
    public void setRtzTimeBackToZero(final long RTZ_TIME_BACK_TO_ZERO) {
        rtzTimeBackToZero = RTZ_TIME_BACK_TO_ZERO < 250 ? 250 : (RTZ_TIME_BACK_TO_ZERO > 5000 ? 5000 : RTZ_TIME_BACK_TO_ZERO);
    }

    /**
     * Returns the timer that is used to timeout the peak value in
     * the bargraph gauges.
     * @return a javax.swing.Timer object
     */
    public javax.swing.Timer getPeakTimer() {
        return this.PEAK_TIMER;
    }

    /**
     * Start the peak timer
     */
    public void startPeakTimer() {
        if (!PEAK_TIMER.isRunning()) {
            PEAK_TIMER.start();
        }
    }

    /**
     * Stop the peak timer
     */
    public void stopPeakTimer() {
        if (PEAK_TIMER.isRunning()) {
            PEAK_TIMER.stop();
        }
    }

    /**
     * Returns an int that represents the orientation of the gauge.
     * The values are taken from Orientation
     * NORTH         => Used in Radial1Vertical
     * NORTH_EAST    => Used in Radial1Square
     * EAST          => Used in Radial1Vertical
     * SOUTH_EAST    => Used in Radial1Square
     * SOUTH         => Used in Radial1Vertical
     * SOUTH_WEST    => Used in Radial1Square
     * WEST          => Used in Radial1Vertical
     * NORTH_WEST    => Used in Radial1Square
     * HORIZONTAL    => Used in Linear
     * VERTICAL      => Used in Linear
     * @return a enum that represents the orientation
     */
    public Orientation getOrientation() {
        return this.orientation;
    }

    /**
     * Sets the orientation of the gauge.
     * The values are taken from Orientation
     * NORTH         => Used in Radial1Vertical
     * NORTH_EAST    => Used in Radial1Square
     * EAST          => Used in Radial1Vertical
     * SOUTH_EAST    => Used in Radial1Square
     * SOUTH         => Used in Radial1Vertical
     * SOUTH_WEST    => Used in Radial1Square
     * WEST          => Used in Radial1Vertical
     * NORTH_WEST    => Used in Radial1Square
     * HORIZONTAL    => Used in Linear
     * VERTICAL      => Used in Linear
     * @param ORIENTATION
     */
    public void setOrientation(final Orientation ORIENTATION) {
        this.orientation = ORIENTATION;
        reInitialize();
    }

    /**
     * Returns the color of the threshold indicator
     * @return the color of the threshold indicator
     */
    public ColorDef getThresholdColor() {
        return model.getThresholdColor();
    }

    /**
     * Sets the color of the threshold indicator
     * @param THRESHOLD_COLOR
     */
    public void setThresholdColor(final ColorDef THRESHOLD_COLOR) {
        model.setThresholdColor(THRESHOLD_COLOR);
        reInitialize();
    }

    /**
     * Returns the custom color definition of the threshold indicator
     * @return the custom color definition of the threshold indicator
     */
    public CustomColorDef getCustomThresholdColor() {
        return model.getCustomThresholdColor();
    }

    /**
     * Sets the custom color definition of the threshold indicator
     * @param CUSTOM_THRESHOLD_COLOR
     */
    public void setCustomThresholdColor(final CustomColorDef CUSTOM_THRESHOLD_COLOR) {
        model.setCustomThresholdColor(CUSTOM_THRESHOLD_COLOR);
        reInitialize();
    }

    /**
     * Returns the type of the threshold indicator
     * @return the type of the threshold indicator
     */
    public ThresholdType getThresholdType() {
        return model.getThresholdType();
    }

    /**
     * Sets the type of the threshold indicator
     * @param THRESHOLD_TYPE
     */
    public void setThresholdType(final ThresholdType THRESHOLD_TYPE) {
        model.setThresholdType(THRESHOLD_TYPE);
        reInitialize();
    }

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
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Tickmarks related">
    /**
     * Returns true if the color of the tickmarks will be
     * used from the defined background color.
     * @return true if tickmarks will use the color defined in the current
     * background color
     */
    public boolean isTickmarkColorFromThemeEnabled() {
        return model.isTickmarkColorFromThemeEnabled();
    }

    /**
     * Enables/disables the usage of a separate color for the
     * tickmarks.
     * @param TICKMARK_COLOR_FROM_THEME_ENABLED
     */
    public void setTickmarkColorFromThemeEnabled(final boolean TICKMARK_COLOR_FROM_THEME_ENABLED) {
        model.setTickmarkColorFromThemeEnabled(TICKMARK_COLOR_FROM_THEME_ENABLED);
        reInitialize();
    }

    /**
     * Returns the color of the tickmarks and their labels
     * @return the custom defined color for the tickmarks and labels
     */
    public Color getTickmarkColor() {
        return model.getTickmarkColor();
    }

    /**
     * Sets the color of the tickmarks and their labels
     * @param TICKMARK_COLOR
     */
    public void setTickmarkColor(final Color TICKMARK_COLOR) {
        model.setTickmarkColor(TICKMARK_COLOR);
        reInitialize();
    }

    /**
     * Returns true if the tickmarks are visible
     * @return true if the tickmarks are visible
     */
    public boolean isTickmarksVisible() {
        return model.isTickmarksVisible();
    }

    /**
     * Enables or disables the visibility of the tickmarks
     * @param TICKMARKS_VISIBLE
     */
    public void setTickmarksVisible(final boolean TICKMARKS_VISIBLE) {
        model.setTickmarksVisible(TICKMARKS_VISIBLE);
        reInitialize();
    }

    /**
     * Returns true if the tickmark labels are visible
     * @return true if the tickmark labels are visible
     */
    public boolean isTicklabelsVisible() {
        return model.isTicklabelsVisible();
    }

    /**
     * Enables or disables the visibility of the tickmark labels
     * @param TICKLABELS_VISIBLE
     */
    public void setTicklabelsVisible(final boolean TICKLABELS_VISIBLE) {
        model.setTicklabelsVisible(TICKLABELS_VISIBLE);
        reInitialize();
    }

    /**
     * Returns the number format that is used to display the labels of the tickmarks
     * @return the number format that is used to display the labels of the tickmarks
     */
    public NumberFormat getLabelNumberFormat() {
        return model.getLabelNumberFormat();
    }

    /**
     * Sets the number format that will be used to display the labels of the tickmarks
     * @param NUMBER_FORMAT Possible values are AUTO, STANDARD, FRACTIONAL and SCIENTIFIC
     */
    public void setLabelNumberFormat(final NumberFormat NUMBER_FORMAT) {
        model.setLabelNumberFormat(NUMBER_FORMAT);
        reInitialize();
    }

    /**
     * Returns true if customer defined tickmark labels will be
     * used for the scaling.
     * e.g. you only want to show "0, 10, 50, 100" in your
     * gauge scale so you could set the custom tickmarklabels
     * to these values.
     * @return a boolean that indicates if custom tickmark labels will be used
     */
    public boolean isCustomTickmarkLabelsEnabled() {
        return this.customTickmarkLabelsEnabled;
    }

    /**
     * Enables/Disables the usage of custom tickmark labels.
     * e.g. you only want to show "0, 10, 50, 100" in your
     * gauge scale so you could set the custom tickmarklabels
     * to these values.
     * @param CUSTOM_TICKMARK_LABELS_ENABLED
     */
    public void setCustomTickmarkLabelsEnabled(final boolean CUSTOM_TICKMARK_LABELS_ENABLED) {
        this.customTickmarkLabelsEnabled = CUSTOM_TICKMARK_LABELS_ENABLED;
        reInitialize();
    }

    /**
     * Returns a list of the defined custom tickmark labels
     * e.g. you only want to show "0, 10, 50, 100" in your
     * gauge scale so you could set the custom tickmarklabels
     * to these values.
     * @return the arraylist containing custom tickmark labels
     */
    public List<Double> getCustomTickmarkLabels() {
        java.util.List<Double> customTickmarkLabelsCopy = new java.util.ArrayList<Double>(10);
        customTickmarkLabelsCopy.addAll(customTickmarkLabels);
        //return (java.util.ArrayList<Double>) this.CUSTOM_TICKMARK_LABELS.clone();
        return customTickmarkLabelsCopy;
    }

    /**
     * Takes a array of doubles that will be used as custom tickmark labels
     * e.g. you only want to show "0, 10, 50, 100" in your
     * gauge scale so you could set the custom tickmarklabels
     * to these values.
     * @param CUSTOM_TICKMARK_LABELS_ARRAY
     */
    public void setCustomTickmarkLabels(final double... CUSTOM_TICKMARK_LABELS_ARRAY) {
        customTickmarkLabels.clear();
        for (Double label : CUSTOM_TICKMARK_LABELS_ARRAY) {
            customTickmarkLabels.add(label);
        }
        reInitialize();
    }

    /**
     * Adds the given double to the list of custom tickmark labels
     * e.g. you only want to show "0, 10, 50, 100" in your
     * gauge scale so you could set the custom tickmarklabels
     * to these values.
     * @param CUSTOM_TICKMARK_LABEL
     */
    public void addCustomTickmarkLabel(final double CUSTOM_TICKMARK_LABEL) {
        customTickmarkLabels.add(CUSTOM_TICKMARK_LABEL);
        reInitialize();
    }

    /**
     * Reset the list of custom tickmark labels, which means clear the list
     */
    public void resetCustomTickmarkLabels() {
        customTickmarkLabels.clear();
        reInitialize();
    }

    /**
     * Returns a copy of the ArrayList that stores the sections.
     * The sections could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class Section.
     * The sections are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you would like to visualize by
     * colored tickmarks.
     * @return a arraylist representing the sections for the tickmarks
     */
    public java.util.List<Section> getTickmarkSections() {
        return model.getTickmarkSections();
    }

    /**
     * Sets the sections given in a array of sections (Section[])
     * The sections could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class Section.
     * The sections are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you would like to visualize by
     * by colored tickmarks.
     * @param TICKMARK_SECTIONS_ARRAY
     */
    public void setTickmarkSections(final Section... TICKMARK_SECTIONS_ARRAY) {
        model.setTickmarkSections(TICKMARK_SECTIONS_ARRAY);
        reInitialize();
    }

    /**
     * Adds a given section to the list of sections
     * The sections could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class Section.
     * The sections are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you would like to visualize by
     * by colored tickmarks.
     * @param TICKMARK_SECTION
     */
    public void addTickmarkSection(final Section TICKMARK_SECTION) {
        model.addTickmarkSection(TICKMARK_SECTION);
        reInitialize();
    }

    /**
     * Clear the TICKMARK_SECTIONS arraylist
     */
    public void resetTickmarkSections() {
        model.resetTickmarkSections();
        reInitialize();
    }

    /**
     * Returns the visibility of the tickmark sections.
     * The sections could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class Section.
     * The sections are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * the tickmark labels colored for specific areas.
     * @return true if the tickmark sections are visible
     */
    public boolean isTickmarkSectionsVisible() {
        return model.isTickmarkSectionsVisible();
    }

    /**
     * Sets the visibility of the tickmark sections.
     * The sections could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class Section.
     * The sections are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * the tickmark labels colored for specific areas.
     * @param TICKMARK_SECTIONS_VISIBLE
     */
    public void setTickmarkSectionsVisible(final boolean TICKMARK_SECTIONS_VISIBLE) {
        model.setTickmarkSectionsVisible(TICKMARK_SECTIONS_VISIBLE);
        reInitialize();
    }

    /**
     * Returns the current type of tickmark that is used for minor tickmarks. Could be LINE (default), CIRCLE, TRIANGLE or SQUARE
     * @return the current type of tickmark that is used for minor tickmarks. Could be LINE (default), CIRCLE, TRIANGLE or SQUARE
     */
    public TickmarkType getMinorTickmarkType() {
        return model.getMinorTickmarkType();
    }

    /**
     * Sets the current type of tickmark that is used for minor tickmarks. Value could be LINE (default), CIRCLE, TRIANGLE or SQUARE
     * @param TICKMARK_TYPE
     */
    public void setMinorTickmarkType(final TickmarkType TICKMARK_TYPE) {
        model.setMinorTickmarkType(TICKMARK_TYPE);
        reInitialize();
    }

    /**
     * Returns the current type of tickmark that is used for major tickmarks. Could be LINE (default), CIRCLE, TRIANGLE or SQUARE
     * @return the current type of tickmark that is used for major tickmarks. Could be LINE (default), CIRCLE, TRIANGLE or SQUARE
     */
    public TickmarkType getMajorTickmarkType() {
        return model.getMajorTickmarkType();
    }

    /**
     * Sets the current type of tickmark that is used for major tickmarks. Value could be LINE (default), CIRCLE, TRIANGLE or SQUARE
     * @param TICKMARK_TYPE
     */
    public void setMajorTickmarkType(final TickmarkType TICKMARK_TYPE) {
        model.setMajorTickmarkType(TICKMARK_TYPE);
        reInitialize();
    }

    /**
     * Returns true if the minor tickmarks are visible (every 5th tickmark)
     * @return true if the minor tickmarks are visible (every 5th tickmark)
     */
    public boolean isMinorTickmarkVisible() {
        return model.isMinorTickmarksVisible();
    }

    /**
     * Enables / Disables the visibility of the minor tickmarks (every 5th tickmark)
     * @param MINOR_TICKMARK_VISIBLE
     */
    public void setMinorTickmarkVisible(final boolean MINOR_TICKMARK_VISIBLE) {
        model.setMinorTickmarksVisible(MINOR_TICKMARK_VISIBLE);
        reInitialize();
    }

    /**
     * Returns true if the major tickmarks are visible (every 10th tickmark)
     * @return true if the major tickmarks are visible (every 10th tickmark)
     */
    public boolean isMajorTickmarkVisible() {
        return model.isMajorTickmarksVisible();
    }

    /**
     * Enables / Disables the visibility of the major tickmarks (every 10th tickmark)
     * @param MAJOR_TICKMARK_VISIBLE
     */
    public void setMajorTickmarkVisible(final boolean MAJOR_TICKMARK_VISIBLE) {
        model.setMajorTickmarksVisible(MAJOR_TICKMARK_VISIBLE);
        reInitialize();
    }

    /**
     * Returns true if only the tickmarks of the sections will be visible.
     * @return true if only the tickmarks of the sections will be visible
     */
    public boolean isSectionTickmarksOnly() {
        return model.isSectionTickmarksOnly();
    }

    public void setSectionTickmarksOnly(final boolean SECTION_TICKMARKS_ONLY) {
        model.setSectionTickmarksOnly(SECTION_TICKMARKS_ONLY);
        reInitialize();
    }

    /**
     * Returns true if the calculation of nice values for the min and max values of the scale is enabled
     * @return true if the calculation of nice values for the min and max values of the scale is enabled
     */
    public boolean isNiceScale() {
        return model.isNiceScale();
    }

    /**
     * Enables / disables the calculation of nice values for the min and max values of the scale
     * @param NICE_SCALE
     */
    public void setNiceScale(final boolean NICE_SCALE) {
        model.setNiceScale(NICE_SCALE);
        reInitialize();
    }

    /**
     * Returns true if axis are using logarithmic scaling
     * @return true if axis are using logarithmic scaling
     */
    public boolean isLogScale() {
        return model.isLogScale();
    }

    /**
     * Enables / disables logarithmic scaling for the axis
     * @param LOG_SCALE
     */
    public void setLogScale(final boolean LOG_SCALE) {
        model.setLogScale(LOG_SCALE);
        model.setMinValue(0);
        model.setLabelNumberFormat(NumberFormat.SCIENTIFIC);
        reInitialize();
    }

    /**
     * Returns the spacing between the minor tickmarks
     * @return the spacing between the minor tickmarks
     */
    public double getMinorTickSpacing() {
        return model.getMinorTickSpacing();
    }

    /**
     * Sets the spacing between the minor tickmarks if the niceScale property is disabled
     * @param MINOR_TICKSPACING
     */
    public void setMinorTickSpacing(final double MINOR_TICKSPACING) {
        model.setMinorTickSpacing(MINOR_TICKSPACING);
        reInitialize();
    }

    /**
     * Returns the spacing between the major tickmarks
     * @return the spacing between the major tickmarks
     */
    public double getMajorTickSpacing() {
        return model.getMajorTickSpacing();
    }

    /**
     * Sets the spacing between the major tickmarks if the niceScale property is disabled
     * @param MAJOR_TICKSPACING
     */
    public void setMajorTickSpacing(final double MAJOR_TICKSPACING) {
        model.setMajorTickSpacing(MAJOR_TICKSPACING);
        reInitialize();

    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Track related">
    /**
     * Returns the visibility of the track.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @return true if the track is visible
     */
    public boolean isTrackVisible() {
        return model.isTrackVisible();
    }

    /**
     * Sets the visibility of the track.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @param TRACK_VISIBLE
     */
    public void setTrackVisible(final boolean TRACK_VISIBLE) {
        model.setTrackVisible(TRACK_VISIBLE);
        reInitialize();
    }

    /**
     * Returns the value where the track will start.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @return represents the value where the track starts
     */
    public double getTrackStart() {
        return model.getTrackStart();
    }

    /**
     * Sets the value where the track will start.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @param TRACK_START
     */
    public void setTrackStart(final double TRACK_START) {
        model.setTrackStart(TRACK_START);
        reInitialize();
    }

    /**
     * Returns the value of the point between trackStart and trackStop.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @return represents the value where the intermediate position
     * of the track is defined.
     */
    public double getTrackSection() {
        return model.getTrackSection();
    }

    /**
     * Sets the value of the point between trackStart and trackStop.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @param TRACK_SECTION
     */
    public void setTrackSection(final double TRACK_SECTION) {
        model.setTrackSection(TRACK_SECTION);
        reInitialize();
    }

    /**
     * Returns the value of the point where the track will stop
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @return represents the position where the track stops
     */
    public double getTrackStop() {
        return model.getTrackStop();
    }

    /**
     * Sets the value of the end of the track.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @param TRACK_STOP
     */
    public void setTrackStop(final double TRACK_STOP) {
        model.setTrackStop(TRACK_STOP);
        reInitialize();
    }

    /**
     * Returns the color of the point where the track will start.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @return represents the color at the point where the track starts
     */
    public Color getTrackStartColor() {
        return model.getTrackStartColor();
    }

    /**
     * Sets the color of the point where the track will start.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @param TRACK_START_COLOR
     */
    public void setTrackStartColor(final Color TRACK_START_COLOR) {
        model.setTrackStartColor(TRACK_START_COLOR);
        reInitialize();
    }

    /**
     * Returns the color of the value between trackStart and trackStop
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @return represents the color of the intermediate position on the track
     */
    public Color getTrackSectionColor() {
        return model.getTrackSectionColor();
    }

    /**
     * Sets the color of the value between trackStart and trackStop
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @param TRACK_SECTION_COLOR
     */
    public void setTrackSectionColor(final Color TRACK_SECTION_COLOR) {
        model.setTrackSectionColor(TRACK_SECTION_COLOR);
        reInitialize();
    }

    /**
     * Returns the color of the point where the track will stop.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @return represents the color of the point where the track stops
     */
    public Color getTrackStopColor() {
        return model.getTrackStopColor();
    }

    /**
     * Sets the color of the point where the track will stop.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @param TRACK_STOP_COLOR
     */
    public void setTrackStopColor(final Color TRACK_STOP_COLOR) {
        model.setTrackStopColor(TRACK_STOP_COLOR);
        reInitialize();
    }
    //</editor-fold>

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
        return model.isSectionsVisible();
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
        model.setSectionsVisible(SECTIONS_VISIBLE);
        reInitialize();
    }

    /**
     * Returns true if the section that contains the current value will be highlighted
     * @return true if the section that contains the current value will be highlighted
     */
    public boolean isHighlightSection() {
        return getModel().isHighlightSection();
    }

    /**
     * Enables / disables the highlighting of the section that contains the current value
     * @param HIGHLIGHT_SECTION
     */
    public void setHighlightSection(final boolean HIGHLIGHT_SECTION) {
        getModel().setHighlightSection(HIGHLIGHT_SECTION);
        reInitialize();
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
        return model.getSections();
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
        model.setSections(SECTIONS_ARRAY);
        reInitialize();
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
        model.addSection(SECTION);
        reInitialize();
    }

    /**
     * Clear the SECTIONS arraylist
     */
    public void resetSections() {
        model.resetSections();
        reInitialize();
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Area related">
    /**
     * Returns the visibility of the areas.
     * The areas could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class Section.
     * The areas are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you could not visualize with the
     * track feature.
     * @return true if the areas are visible
     */
    public boolean isAreasVisible() {
        return model.isAreasVisible();
    }

    /**
     * Sets the visibility of the areas.
     * The areas could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class Section.
     * The areas are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you could not visualize with the
     * track feature.
     * @param AREAS_VISIBLE
     */
    public void setAreasVisible(final boolean AREAS_VISIBLE) {
        model.setAreasVisible(AREAS_VISIBLE);
        reInitialize();
    }

    /**
     * Returns true if the area that contains the current value will be highlighted
     * @return true if the area that contains the current value will be highlighted
     */
    public boolean isHighlightArea() {
        return getModel().isHighlightArea();
    }

    /**
     * Enables / disables the highlighting of the area that contains the current value
     * @param HIGHLIGHT_AREA
     */
    public void setHighlightArea(final boolean HIGHLIGHT_AREA) {
        getModel().setHighlightArea(HIGHLIGHT_AREA);
        reInitialize();
    }

    /**
     * Returns a copy of the ArrayList that stores the areas.
     * The areas could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class Section.
     * The areas are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you could not visualize with the
     * track feature.
     * @return a clone of the list of areas
     */
    public List<Section> getAreas() {
        return model.getAreas();
    }

    /**
     * Sets the areas given in a array of areas (Section[])
     * A local copy of the Section object will created and will
     * be stored in the component.
     * The areas could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class Section.
     * The areas are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you could not visualize with the
     * track feature.
     * @param AREAS_ARRAY
     */
    public void setAreas(final Section... AREAS_ARRAY) {
        model.setAreas(AREAS_ARRAY);
        reInitialize();
    }

    /**
     * Adds a given area to the list of areas
     * The areas could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class Section.
     * The areas are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you could not visualize with the
     * track feature.
     * @param AREA
     */
    public void addArea(final Section AREA) {
        model.addArea(AREA);
        reInitialize();
    }

    /**
     * Clear the AREAS arraylist
     */
    public void resetAreas() {
        model.resetAreas();
        reInitialize();
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Title/Unit related">
    /**
     * Returns the title of the gauge.
     * A title could be for example "Temperature".
     * @return the title of the gauge
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Sets the title of the gauge.
     * A title could be for example "Temperature".
     * @param TITLE
     */
    public void setTitle(final String TITLE) {
        this.title = TITLE;
        reInitialize();
    }

    /**
     * Returns the unit string of the gauge.
     * A unit string could be for example "[cm]".
     * @return the unit string of the gauge
     */
    public String getUnitString() {
        return this.unitString;
    }

    /**
     * Sets the unit string of the gauge.
     * A unit string could be for example "[cm]"
     * @param UNIT_STRING
     */
    public void setUnitString(final String UNIT_STRING) {
        this.unitString = UNIT_STRING;
        reInitialize();
    }

    /**
     * Returns true if the color of the tickmarks will be
     * used from the defined background color.
     * @return true if the color for the tickmarks and labels
     * will be used from the selected backgroundcolor
     */
    public boolean isLabelColorFromThemeEnabled() {
        return model.isLabelColorFromThemeEnabled();
    }

    /**
     * Enables/disables the usage of a separate color for the
     * title and unit string.
     * @param LABEL_COLOR_FROM_THEME_ENABLED
     */
    public void setLabelColorFromThemeEnabled(final boolean LABEL_COLOR_FROM_THEME_ENABLED) {
        model.setLabelColorFromThemeEnabled(LABEL_COLOR_FROM_THEME_ENABLED);
        reInitialize();
    }

    /**
     * Returns the color of the Title and the Unit string.
     * @return the color of the title and unit string
     */
    public Color getLabelColor() {
        return model.getLabelColor();
    }

    /**
     * Sets the color of the Title and the Unit string.
     * @param LABEL_COLOR
     */
    public void setLabelColor(final Color LABEL_COLOR) {
        model.setLabelColor(LABEL_COLOR);
        reInitialize();
    }

    /**
     * Returns true if a custom font will be used for the title and unit string
     * @return true if a custom font will be used for the title and unit string
     */
    public boolean isTitleAndUnitFontEnabled() {
        return model.isCustomTitleAndUnitFontEnabled();
    }

    /**
     * Enables and disables the usage of a custom title and unit string font
     * @param TITLE_AND_UNIT_FONT_ENABLED
     */
    public void setTitleAndUnitFontEnabled(final boolean TITLE_AND_UNIT_FONT_ENABLED) {
        model.setCustomTitleAndUnitFontEnabled(TITLE_AND_UNIT_FONT_ENABLED);
        reInitialize();
    }

    /**
     * Sets the given font for the title and unit string.
     * @return the custom defined font for the title and unit string
     */
    public Font getTitleAndUnitFont() {
        return this.titleAndUnitFont;
    }

    /**
     * Returns the font that will be used for the title and unit string
     * @param TITLE_UNIT_FONT
     */
    public void setTitleAndUnitFont(final Font TITLE_UNIT_FONT) {
        this.titleAndUnitFont = TITLE_UNIT_FONT;
        reInitialize();
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Frame related">
    /**
     * Returns the framedesign of the component.
     * The framedesign is some kind of a color scheme for the
     * frame of the component.
     * The typical framedesign is METAL
     * @return the selected framedesign
     */
    public FrameDesign getFrameDesign() {
        return model.getFrameDesign();
    }

    /**
     * Sets the framedesign of the component.
     * The framedesign is some kind of a color scheme for the
     * frame of the component.
     * The typical framedesign is METAL
     * @param FRAME_DESIGN
     */
    public void setFrameDesign(final FrameDesign FRAME_DESIGN) {
        model.setFrameDesign(FRAME_DESIGN);
        reInitialize();
    }

    /**
     * Returns the java.awt.Paint that will be used to visualize the frame
     * @return the java.awt.Paint that will be used to visualize the frame
     */
    public Paint getCustomFrameDesign() {
        return model.getCustomFrameDesign();
    }

    /**
     * Seths the custom framedesign of the type java.awt.Paint
     * This will be used if the frameDesign property is set to CUSTOM
     * @param CUSTOM_FRAME_DESIGN
     */
    public void setCustomFrameDesign(final Paint CUSTOM_FRAME_DESIGN) {
        model.setCustomFrameDesign(CUSTOM_FRAME_DESIGN);
        reInitialize();
    }

    /**
     * Returns true if the frameImage is visible and will be painted
     * @return a boolean that represents the visibility of the frameImage
     */
    public boolean isFrameVisible() {
        return model.isFrameVisible();
    }

    /**
     * Enables/Disables the visibility of the frame.
     * If enabled the frame will be painted in the paintComponent() method.
     * Setting the frameDesign to NO_FRAME will only make the frame transparent.
     * @param FRAME_VISIBLE
     */
    public void setFrameVisible(final boolean FRAME_VISIBLE) {
        model.setFrameVisible(FRAME_VISIBLE);
        calcInnerBounds();
        reInitialize();
    }

    /**
     * Returns the frame effect
     * @return the frame effect
     */
    public FrameEffect getFrameEffect() {
        return model.getFrameEffect();
    }

    /**
     * Sets the pseudo 3d effect of the frame
     * @param FRAME_EFFECT
     */
    public void setFrameEffect(final FrameEffect FRAME_EFFECT) {
        model.setFrameEffect(FRAME_EFFECT);
        reInitialize();
    }

    /**
     * Returns the color that will be used to colorize the SHINY_METAL FrameDesign
     * @return the color that will be used to colorize the SHINY_METAL FrameDesign
     */
    public Color getFrameBaseColor() {
        return model.getFrameBaseColor();
    }

    /**
     * Sets the color that will be used to colorize the SHINY_METAL FrameDesign
     * @param FRAME_BASECOLOR
     */
    public void setFrameBaseColor(final Color FRAME_BASECOLOR) {
        model.setFrameBaseColor(FRAME_BASECOLOR);
        reInitialize();
    }

    /**
     * Returns true if the frameBaseColor will be used to colorize the SHINY_METAL FrameDesign
     * @return true if the frameBaseColor will be used to colorize the SHINY_METAL FrameDesign
     */
    public boolean isFrameBaseColorEnabled() {
        return model.isFrameBaseColorEnabled();
    }

    /**
     * Enables / disables the usage of the frameBaseColor to colorize the SHINY_METAL FrameDesign
     * @param FRAME_BASECOLOR_ENABLED
     */
    public void setFrameBaseColorEnabled(final boolean FRAME_BASECOLOR_ENABLED) {
        model.setFrameBaseColorEnabled(FRAME_BASECOLOR_ENABLED);
        reInitialize();
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Background related">
    /**
     * Returns the backgroundcolor of the gauge.
     * The backgroundcolor is not a standard color but more a
     * color scheme with colors and a gradient.
     * The typical backgroundcolor is DARK_GRAY.
     * @return the selected backgroundcolor
     */
    public BackgroundColor getBackgroundColor() {
        return model.getBackgroundColor();
    }

    /**
     * Sets the backgroundcolor of the gauge.
     * The backgroundcolor is not a standard color but more a
     * color scheme with colors and a gradient.
     * The typical backgroundcolor is DARK_GRAY.
     * @param BACKGROUND_COLOR
     */
    public void setBackgroundColor(final BackgroundColor BACKGROUND_COLOR) {
        model.setBackgroundColor(BACKGROUND_COLOR);
        reInitialize();
    }

    /**
     * Returns true if the backgroundImage is visible and will be painted
     * @return a boolean that represents the visibility of the backgroundImage
     */
    public boolean isBackgroundVisible() {
        return model.isBackgroundVisible();
    }

    /**
     * Enables/Disables the visibility of the backgroundImage.
     * If enabled the backgroundImage will be painted in the
     * paintComponent() method. The backgroundColor TRANSPARENT
     * only makes the background transparent but the custom
     * background will still be visible.
     * @param BACKGROUND_VISIBLE
     */
    public void setBackgroundVisible(final boolean BACKGROUND_VISIBLE) {
        model.setBackgroundVisible(BACKGROUND_VISIBLE);
        reInitialize();
    }

    /**
     * Returns the color that will be used to render textures like Carbon, PunchedSheet, Linen etc.
     * @return the color that will be used to render textures like Carbon, PunchedSheet, Linen etc.
     */
    public Color getTextureColor() {
        return model.getTextureColor();
    }

    /**
     * Sets the color that will be used to render textures like Carbon, PunchedSheet, Linen etc.
     * @param TEXTURE_COLOR
     */
    public void setTextureColor(final Color TEXTURE_COLOR) {
        model.setTextureColor(TEXTURE_COLOR);
        BACKGROUND_FACTORY.recreatePunchedSheetTexture(TEXTURE_COLOR);
        reInitialize();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Custom background related">
    /**
     * Returns the custom background paint that will be used instead of
     * the predefined backgroundcolors like DARK_GRAY, BEIGE etc.
     * @return the custom paint that will be used for the background of the gauge
     */
    public Paint getCustomBackground() {
        return model.getCustomBackground();
    }

    /**
     * Sets the custom background paint that will be used instead of
     * the predefined backgroundcolors like DARK_GRAY, BEIGE etc.
     * @param CUSTOM_BACKGROUND
     */
    public void setCustomBackground(final Paint CUSTOM_BACKGROUND) {
        model.setCustomBackground(CUSTOM_BACKGROUND);
        if (model.getBackgroundColor() == BackgroundColor.CUSTOM) {
            reInitialize();
        }
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Custom layer related">
    /**
     * Returns true if the custom layer is visible.
     * The custom layer (which is a buffered image) will be
     * drawn on the background of the gauge and could be used
     * to display logos or icons.
     * @return true if custom layer is visible
     */
    public boolean isCustomLayerVisible() {
        return model.isCustomLayerVisible();
    }

    /**
     * Enables/disables the usage of the custom layer.
     * The custom layer (which is a buffered image) will be
     * drawn on the background of the gauge and could be used
     * to display logos or icons.
     * @param CUSTOM_LAYER_VISIBLE
     */
    public void setCustomLayerVisible(final boolean CUSTOM_LAYER_VISIBLE) {
        if (model.getCustomLayer() != null) {
            model.setCustomLayerVisible(CUSTOM_LAYER_VISIBLE);
        }
        reInitialize();
    }

    /**
     * Returns the buffered image that represents the custom layer.
     * The custom layer (which is a buffered image) will be
     * drawn on the background of the gauge and could be used
     * to display logos or icons.
     * @return the buffered image that represents the custom layer
     */
    public BufferedImage getCustomLayer() {
        return model.getCustomLayer();
    }

    /**
     * Sets the buffered image that represents the custom layer.
     * It will automaticaly scale the given image to the bounds.
     * The custom layer (which is a buffered image) will be
     * drawn on the background of the gauge and could be used
     * to display logos or icons.
     * @param CUSTOM_LAYER
     */
    public void setCustomLayer(final BufferedImage CUSTOM_LAYER) {
        if (CUSTOM_LAYER == null) {
            model.setCustomLayerVisible(false);
            return;
        }

        model.setCustomLayer(CUSTOM_LAYER);

        if (model.isCustomLayerVisible()) {
            reInitialize();
        }
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Glow related">
    protected float getGlowAlpha() {
        return glowAlpha;
    }

    /**
     * Returns true if the glow effect is visible
     * @return true if the glow effect is visible
     */
    abstract boolean isGlowVisible();

    /**
     * Returns true if the glow effect is pulsating
     * @return true if the glow effect is pulsating
     */
    public boolean isGlowPulsating() {
        return GLOW_PULSE.isRunning();
    }

    /**
     * Enables / disables the pulsating of the glow effect
     * @param PULSATING
     */
    public void setGlowPulsating(final boolean PULSATING) {
        if (isGlowVisible() && PULSATING && !GLOW_PULSE.isRunning()) {
            GLOW_PULSE.start();
        } else {
            if (GLOW_PULSE.isRunning()) {
                GLOW_PULSE.stop();
                glowAlpha = 1.0f;
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Foreground related">
    /**
     * Returns true if the foreground image is visible
     * The foreground image will only be painted if
     * it is set to true.
     * @return visibility of the foreground image
     */
    public boolean isForegroundVisible() {
        return model.isForegroundVisible();
    }

    /**
     * Enables/Disables the visibility of the glass effect foreground image.
     * If enabled the foregroundImage will be painted.
     * @param FOREGROUND_VISIBLE
     */
    public void setForegroundVisible(final boolean FOREGROUND_VISIBLE) {
        model.setForegroundVisible(FOREGROUND_VISIBLE);
        reInitialize();
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Miscellaneous">
    /**
     * Returns an image of a led with the given size, state and color.
     * If the LED_COLOR parameter equals CUSTOM the userLedColor will be used
     * to calculate the custom led colors
     * @param SIZE
     * @param STATE
     * @param LED_COLOR
     * @return the led image
     */
    protected final BufferedImage create_LED_Image(final int SIZE, final int STATE, final LedColor LED_COLOR) {
        return LED_FACTORY.create_LED_Image(SIZE, STATE, LED_COLOR, model.getCustomLedColor());
    }

    /**
     * Returns an image of the lcd threshold indicator with the given size and color
     * @param WIDTH
     * @param HEIGHT
     * @param COLOR
     * @return an image of the lcd threshold indicator
     */
    public BufferedImage create_LCD_THRESHOLD_Image(final int WIDTH, final int HEIGHT, final Color COLOR) {
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

    /**
     * Calculates the rectangle that is defined by the dimension of the component
     * and it's insets given by e.g. a border.
     */
    abstract public void calcInnerBounds();

    /**
     * Returns the rectangle that is defined by the dimension of the component and
     * it's insets given by e.g. a border.
     * @return rectangle that defines the inner area available for painting
     */
    abstract public java.awt.Rectangle getInnerBounds();

    /**
     * Returns a point2d object that defines the center of the gauge.
     * This method will take the insets and the real position of the
     * gauge into account.
     * @return a point2d object that represents the center of the gauge
     */
    abstract protected Point2D getCenter();

    /**
     * Returns the boundary of the gauge itself as a rectangle2d.
     * @return a rectangle2d that represents the boundary of the gauge itself
     */
    abstract protected Rectangle2D getBounds2D();

    /**
     * Reinitialize and repaint the component
     */
    public void reInitialize() {
        if (isInitialized()) {
            init(getInnerBounds().width, getInnerBounds().height);
            repaint(getInnerBounds());
        }
    }

    @Override
    public void setBorder(final Border BORDER) {
        super.setBorder(BORDER);
        calcInnerBounds();
        reInitialize();
    }

    @Override
    public JComponent clone() {
        try {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream objOut = new java.io.ObjectOutputStream(out);
            objOut.writeObject(this);
            objOut.flush();
            java.io.ObjectInputStream objIn = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(out.toByteArray()));
            JComponent compClone = (JComponent) objIn.readObject();
            objOut.close();
            objIn.close();
            return compClone;
        } catch (java.io.IOException exception) {
        } catch (java.lang.ClassNotFoundException exception) {
        }
        return null;
    }

    /**
     * Calling this method will remove all active listeners from the timers
     */
    public void dispose() {
        LED_BLINKING_TIMER.removeActionListener(this);
        USER_LED_BLINKING_TIMER.removeActionListener(this);
        PEAK_TIMER.removeActionListener(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Change listener methods">
    /**
     * Add a given ChangeListener to the list of listeners
     * @param LISTENER
     */
    public void addChangeListener(final ChangeListener LISTENER) {
        LISTENER_LIST.add(ChangeListener.class, LISTENER);
    }

    /**
     * Remove the given ChangeListener from the list of listeners
     * @param LISTENER
     */
    public void removeChangeListener(final ChangeListener LISTENER) {
        LISTENER_LIST.remove(ChangeListener.class, LISTENER);
    }

    /**
     * Notify all registered listeners about a state change
     */
    protected void fireStateChanged() {
        Object[] listeners = LISTENER_LIST.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="PropertyChange listener methods">
    @Override
    public void addPropertyChangeListener(final PropertyChangeListener LISTENER) {
        propertyChangeSupport.addPropertyChangeListener(LISTENER);
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener LISTENER) {
        propertyChangeSupport.removePropertyChangeListener(LISTENER);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Component listener methods">
    @Override
    public void componentResized(ComponentEvent event) {
        // Radial gauge
        if (event.getComponent() instanceof AbstractRadial) {
            final int SIZE = getWidth() < getHeight() ? getWidth() : getHeight();
            setSize(SIZE, SIZE);
            setPreferredSize(getSize());

            if (SIZE < getMinimumSize().width || SIZE < getMinimumSize().height) {
                setSize(getMinimumSize());
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

            reInitialize();
        }

        // Linear gauge
        if (event.getComponent() instanceof AbstractLinear) {
            setSize(getWidth(), getHeight());
            setPreferredSize(getSize());
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
                setLedPosition((getInnerBounds().width - 18.0 - 16.0) / getInnerBounds().width, 0.453271028);

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
                setLedPosition(0.453271028, (18.0 / getInnerBounds().height));

                if (isUserLedOn()) {
                    setCurrentUserLedImage(getUserLedImageOn());
                } else {
                    setCurrentUserLedImage(getUserLedImageOff());
                }
                setUserLedPosition((getInnerBounds().width - 18.0 - 16.0) / getInnerBounds().width, 0.453271028);
            }

            reInitialize();
        }
        //revalidate();
        //repaint();
    }

    @Override
    public void componentMoved(ComponentEvent event) {
    }

    @Override
    public void componentShown(ComponentEvent event) {
    }

    @Override
    public void componentHidden(ComponentEvent event) {
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ActionListener methods">
    @Override
    public void actionPerformed(final ActionEvent EVENT) {
        if (EVENT.getSource().equals(LED_BLINKING_TIMER)) {
            currentLedImage.flush();
            currentLedImage = ledOn == true ? getLedImageOn() : getLedImageOff();
            ledOn ^= true;
            repaint((int) (getInnerBounds().width * getLedPosition().getX() + getInnerBounds().x), (int) (getInnerBounds().height * getLedPosition().getY() + getInnerBounds().y), currentLedImage.getWidth(), currentLedImage.getHeight());
        } else if (EVENT.getSource().equals(USER_LED_BLINKING_TIMER)) {
            currentUserLedImage.flush();
            currentUserLedImage = userLedOn == true ? getUserLedImageOn() : getUserLedImageOff();
            userLedOn ^= true;
            repaint((int) (getInnerBounds().width * getUserLedPosition().getX() + getInnerBounds().x), (int) (getInnerBounds().height * getUserLedPosition().getY() + getInnerBounds().y), currentUserLedImage.getWidth(), currentUserLedImage.getHeight());
        } else if (EVENT.getSource().equals(PEAK_TIMER)) {
            setPeakValueVisible(false);
            PEAK_TIMER.stop();
        } else if (EVENT.getSource().equals(GLOW_PULSE)) {
            glowAlphaCounter += glowAlphaIncrement;
            glowAlpha = (float) Math.cos(glowAlphaCounter);
            if (Float.compare(glowAlpha, 1.0f) >= 0) {
                glowAlpha = 1.0f;
                glowAlphaIncrement = -0.0785398164;
            }
            if (Float.compare(glowAlpha, 0.2f) <= 0) {
                glowAlpha = 0f;
                glowAlphaIncrement = 0.0785398164;
            }
            repaint(getInnerBounds());
        }
    }
    // </editor-fold>
}
