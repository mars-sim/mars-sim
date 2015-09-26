/*
 * Copyright (c) 2015 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.enzo.gauge;

import com.sun.javafx.css.converters.PaintConverter;
import eu.hansolo.enzo.gauge.skin.LinearSkin;
import javafx.animation.AnimationTimer;
import javafx.beans.property.*;
import javafx.css.*;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class Linear extends Control {
    public static enum NumberFormat {
        AUTO("0"),
        STANDARD("0"),
        FRACTIONAL("0.0#"),
        SCIENTIFIC("0.##E0"),
        PERCENTAGE("##0.0%");

        private final DecimalFormat DF;

        private NumberFormat(final String FORMAT_STRING) {
            Locale.setDefault(new Locale("en", "US"));

            DF = new DecimalFormat(FORMAT_STRING);
        }

        public String format(final Number NUMBER) {
            return DF.format(NUMBER);
        }
    }

    private static final PseudoClass HORIZONTAL_PSEUDO_CLASS = PseudoClass.getPseudoClass("horizontal");
    private static final PseudoClass LED_ON_PSEUDO_CLASS     = PseudoClass.getPseudoClass("led-on");
    private static final long        LED_BLINK_INTERVAL      = 500_000_000l;

    private DoubleProperty                       value;
    private DoubleProperty                       minValue;
    private double                               exactMinValue;
    private DoubleProperty                       maxValue;
    private double                               exactMaxValue;
    private DoubleProperty                       threshold;
    private IntegerProperty                      decimals;
    private StringProperty                       title;
    private StringProperty                       unit;
    private BooleanProperty                      animated;
    private double                               animationDuration;
    private BooleanProperty                      autoScale;
    private ObjectProperty<Color>                barColor;
    private BooleanProperty                      ledOn;
    private BooleanProperty                      blinking;
    private ObjectProperty<Color>                ledColor;
    private ObjectProperty<NumberFormat>         numberFormat;
    private DoubleProperty                       majorTickSpace;
    private DoubleProperty                       minorTickSpace;
    private ObjectProperty<Orientation>          orientation;

    // CSS styleable properties
    private ObjectProperty<Paint>                tickMarkFill;
    private ObjectProperty<Paint>                tickLabelFill;

    private long                                 lastTimerCall;
    private AnimationTimer                       timer;


    // ******************** Constructors **************************************
    public Linear() {
        getStyleClass().add("linear");
        value             = new DoublePropertyBase(0) {
            @Override public void set(final double VALUE) { super.set(clamp(getMinValue(), getMaxValue(), VALUE)); }
            @Override public Object getBean() { return Linear.this; }
            @Override public String getName() { return "value"; }
        };
        minValue          = new DoublePropertyBase(0) {
            @Override public void set(final double MIN_VALUE) {
                super.set(clamp(-Double.MAX_VALUE, getMaxValue(), MIN_VALUE));
                setThreshold(clamp(getMinValue(), getMaxValue(), getThreshold()));
            }
            @Override public Object getBean() { return Linear.this; }
            @Override public String getName() { return "minValue"; }
        };
        maxValue          = new DoublePropertyBase(100) {
            @Override public void set(final double MAX_VALUE) {
                super.set(clamp(getMinValue(), Double.MAX_VALUE, MAX_VALUE));
                setThreshold(clamp(getMinValue(), getMaxValue(), getThreshold()));
            }
            @Override public Object getBean() { return Linear.this; }
            @Override public String getName() { return "maxValue"; }
        };
        threshold         = new DoublePropertyBase(50) {
            @Override public void set(final double THRESHOLD) {
                super.set(clamp(getMinValue(), getMaxValue(), THRESHOLD));
            }
            @Override public Object getBean() { return Linear.this; }
            @Override public String getName() { return "threshold"; }
        };
        lastTimerCall     = System.nanoTime();
        animationDuration = 800;
        timer             = new AnimationTimer() {
            @Override public void handle(final long NOW) {
                if (NOW > lastTimerCall + LED_BLINK_INTERVAL) {
                    setLedOn(!isLedOn());
                    lastTimerCall = NOW;
                }
            }
        };
    }


    // ******************** Methods *******************************************
    public final double getValue() { return value.get(); }
    public final void setValue(final double VALUE) { value.set(VALUE); }
    public final DoubleProperty valueProperty() { return value; }

    public final double getMinValue() {return minValue.get(); }
    public final void setMinValue(final double MIN_VALUE) { minValue.set(MIN_VALUE); }
    public final DoubleProperty minValueProperty() { return minValue; }

    public final double getMaxValue() { return maxValue.get(); }
    public final void setMaxValue(final double MAX_VALUE) { maxValue.set(MAX_VALUE); }
    public final DoubleProperty maxValueProperty() { return maxValue; }

    public final double getThreshold() { return threshold.get(); }
    public final void setThreshold(final double THRESHOLD) { threshold.set(THRESHOLD); }
    public final DoubleProperty thresholdProperty() { return threshold; }

    public final int getDecimals() { return null == decimals ? 1 : decimals.get(); }
    public final void setDecimals(final int DECIMALS) { decimalsProperty().set(clamp(0, 3, DECIMALS)); }
    public final IntegerProperty decimalsProperty() {
        if (null == decimals) { decimals = new SimpleIntegerProperty(this, "decimals", 1); }
        return decimals;
    }

    public final String getTitle() { return null == title ? "" : title.get(); }
    public final void setTitle(final String TITLE) { titleProperty().set(TITLE); }
    public final StringProperty titleProperty() {
        if (null == title) { title = new SimpleStringProperty(this, "title", ""); }
        return title;
    }

    public final String getUnit() { return null == unit ? "" : unit.get(); }
    public final void setUnit(final String UNIT) { unitProperty().set(UNIT); }
    public final StringProperty unitProperty() {
        if (null == unit) { unit = new SimpleStringProperty(this, "unit", ""); }
        return unit;
    }

    public final boolean isAnimated() { return null == animated ? true : animated.get(); }
    public final void setAnimated(final boolean ANIMATED) { animatedProperty().set(ANIMATED); }
    public final BooleanProperty animatedProperty() {
        if (null == animated) { animated = new SimpleBooleanProperty(this, "animated", true); }
        return animated;
    }

    public final double getAnimationDuration() { return animationDuration; }
    public final void setAnimationDuration(final double ANIMATION_DURATION) { animationDuration = clamp(20, 5000, ANIMATION_DURATION); }

    public final boolean isAutoScale() { return null == autoScale ? false : autoScale.get(); }
    public final void setAutoScale(final boolean AUTO_SCALE) { autoScaleProperty().set(AUTO_SCALE); }
    public final BooleanProperty autoScaleProperty() {
        if (null == autoScale) {
            autoScale = new BooleanPropertyBase(false) {
                @Override public void set(final boolean AUTO_SCALE) {
                    if (get()) {
                        exactMinValue = getMinValue();
                        exactMaxValue = getMaxValue();
                    } else {
                        setMinValue(exactMinValue);
                        setMaxValue(exactMaxValue);
                    }
                    super.set(AUTO_SCALE);
                }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "autoScale"; }
            };
        }
        return autoScale;
    }

    public final Color getBarColor() {
        return null == barColor ? Color.RED : barColor.get();
    }
    public final void setBarColor(final Color BAR_COLOR) { barColorProperty().set(BAR_COLOR); }
    public final ObjectProperty<Color> barColorProperty() {
        if (null == barColor) { barColor = new SimpleObjectProperty<>(this, "barColor", Color.RED); }
        return barColor;
    }

    public final boolean isLedOn() { return null == ledOn ? false : ledOn.get(); }
    public final void setLedOn(final boolean LED_ON) { ledOnProperty().set(LED_ON); }
    public final BooleanProperty ledOnProperty() {
        if (null == ledOn) {
            ledOn = new BooleanPropertyBase(false) {
                @Override protected void invalidated() { pseudoClassStateChanged(LED_ON_PSEUDO_CLASS, get()); }

                @Override public Object getBean() { return this; }

                @Override public String getName() { return "on"; }
            };
        }
        return ledOn;
    }

    public final boolean isBlinking() { return null == blinking ? false : blinking.get(); }
    public final void setBlinking(final boolean BLINKING) { blinkingProperty().set(BLINKING); }
    public final BooleanProperty blinkingProperty() {
        if (null == blinking) {
            blinking = new BooleanPropertyBase() {
                @Override public void set(final boolean BLINKING) {
                    super.set(BLINKING);
                    if (BLINKING) {
                        timer.start();
                    } else {
                        timer.stop();
                        setLedOn(false);
                    }
                }
                @Override public Object getBean() {
                    return Linear.this;
                }
                @Override public String getName() {
                    return "blinking";
                }
            };
        }
        return blinking;
    }

    public final Color getLedColor() { return null == ledColor ? Color.RED : ledColor.get(); }
    public final void setLedColor(final Color LED_COLOR) { ledColorProperty().set(LED_COLOR); }
    public final ObjectProperty<Color> ledColorProperty() {
        if (null == ledColor) {
            ledColor = new SimpleObjectProperty<>(this, "ledColor", Color.RED);
        }
        return ledColor;
    }

    public final NumberFormat getNumberFormat() {
        return null == numberFormat ? NumberFormat.STANDARD : numberFormat.get();
    }
    public final void setNumberFormat(final NumberFormat NUMBER_FORMAT) {
        numberFormatProperty().set(NUMBER_FORMAT);
    }
    public final ObjectProperty<NumberFormat> numberFormatProperty() {
        if (null == numberFormat) {
            numberFormat = new SimpleObjectProperty<>(this, "numberFormat", NumberFormat.STANDARD);
        }
        return numberFormat;
    }

    public final double getMajorTickSpace() {
        return null == majorTickSpace ? 10 : majorTickSpace.get();
    }
    public final void setMajorTickSpace(final double MAJOR_TICK_SPACE) {
        majorTickSpaceProperty().set(MAJOR_TICK_SPACE);
    }
    public final DoubleProperty majorTickSpaceProperty() {
        if (null == majorTickSpace) {
            majorTickSpace = new SimpleDoubleProperty(this, "majorTickSpace", 10);
        }
        return majorTickSpace;
    }

    public final double getMinorTickSpace() {
        return null == minorTickSpace ? 1 : minorTickSpace.get();
    }
    public final void setMinorTickSpace(final double MINOR_TICK_SPACE) {
        minorTickSpaceProperty().set(MINOR_TICK_SPACE);
    }
    public final DoubleProperty minorTickSpaceProperty() {
        if (null == minorTickSpace) {
            minorTickSpace = new SimpleDoubleProperty(this, "minorTickSpace", 1);
        }
        return minorTickSpace;
    }

    public final Orientation getOrientation() { return null == orientation ? Orientation.VERTICAL : orientation.get(); }
    public final void setOrientation(final Orientation ORIENTATION) { orientationProperty().set(ORIENTATION); }
    public final ObjectProperty<Orientation> orientationProperty() {
        if (null == orientation) {
            orientation = new ObjectPropertyBase<Orientation>(Orientation.VERTICAL) {
                @Override public void invalidated() { pseudoClassStateChanged(HORIZONTAL_PSEUDO_CLASS, Orientation.HORIZONTAL == get()); }
                @Override public Object getBean() { return Linear.this; }
                @Override public String getName() { return "orientation"; }
            };
        }
        return orientation;
    }

    public double clamp(final double MIN_VALUE, final double MAX_VALUE, final double VALUE) {
        if (VALUE < MIN_VALUE) return MIN_VALUE;
        if (VALUE > MAX_VALUE) return MAX_VALUE;
        return VALUE;
    }
    public int clamp(final int MIN_VALUE, final int MAX_VALUE, final int VALUE) {
        if (VALUE < MIN_VALUE) return MIN_VALUE;
        if (VALUE > MAX_VALUE) return MAX_VALUE;
        return VALUE;
    }
    public Duration clamp(final Duration MIN_VALUE, final Duration MAX_VALUE, final Duration VALUE) {
        if (VALUE.lessThan(MIN_VALUE)) return MIN_VALUE;
        if (VALUE.greaterThan(MAX_VALUE)) return MAX_VALUE;
        return VALUE;
    }


    // ******************** CSS Stylable Properties ***************************
    public final Paint getTickMarkFill() {
        return null == tickMarkFill ? Color.WHITE : tickMarkFill.get();
    }
    public final void setTickMarkFill(Paint value) {
        tickMarkFillProperty().set(value);
    }
    public final ObjectProperty<Paint> tickMarkFillProperty() {
        if (null == tickMarkFill) {
            tickMarkFill = new StyleableObjectProperty<Paint>(Color.BLACK) {

                @Override public CssMetaData getCssMetaData() { return StyleableProperties.TICK_MARK_FILL; }

                @Override public Object getBean() { return Linear.this; }

                @Override public String getName() { return "tickMarkFill"; }
            };
        }
        return tickMarkFill;
    }

    public final Paint getTickLabelFill() {
        return null == tickLabelFill ? Color.WHITE : tickLabelFill.get();
    }
    public final void setTickLabelFill(Paint value) {
        tickLabelFillProperty().set(value);
    }
    public final ObjectProperty<Paint> tickLabelFillProperty() {
        if (null == tickLabelFill) {
            tickLabelFill = new StyleableObjectProperty<Paint>(Color.BLACK) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.TICK_LABEL_FILL; }
                @Override public Object getBean() { return Linear.this; }
                @Override public String getName() { return "tickLabelFill"; }
            };
        }
        return tickLabelFill;
    }


    // ******************** Style related *************************************
    @Override protected Skin createDefaultSkin() {
        return new LinearSkin(this);
    }

    @Override public String getUserAgentStylesheet() {
        return getClass().getResource("/eu/hansolo/enzo/gauge/linear.css").toExternalForm();
    }

    private static class StyleableProperties {
        private static final CssMetaData<Linear, Paint> TICK_MARK_FILL =
                new CssMetaData<Linear, Paint>("-tick-mark-fill", PaintConverter.getInstance(), Color.BLACK) {

                    @Override public boolean isSettable(Linear gauge) {
                        return null == gauge.tickMarkFill || !gauge.tickMarkFill.isBound();
                    }

                    @Override public StyleableProperty<Paint> getStyleableProperty(Linear gauge) {
                        return (StyleableProperty) gauge.tickMarkFillProperty();
                    }
                };

        private static final CssMetaData<Linear, Paint> TICK_LABEL_FILL =
                new CssMetaData<Linear, Paint>("-tick-label-fill", PaintConverter.getInstance(), Color.BLACK) {

                    @Override public boolean isSettable(Linear gauge) {
                        return null == gauge.tickLabelFill || !gauge.tickLabelFill.isBound();
                    }

                    @Override public StyleableProperty<Paint> getStyleableProperty(Linear gauge) {
                        return (StyleableProperty) gauge.tickLabelFillProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables,
                    TICK_MARK_FILL,
                    TICK_LABEL_FILL);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }
}

