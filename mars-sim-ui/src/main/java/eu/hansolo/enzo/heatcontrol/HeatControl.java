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

package eu.hansolo.enzo.heatcontrol;

import com.sun.javafx.css.converters.PaintConverter;
import eu.hansolo.enzo.heatcontrol.skin.HeatControlSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * User: hansolo
 * Date: 08.11.13
 * Time: 16:35
 */
public class HeatControl extends Control {
    private DoubleProperty  value;
    private double          oldValue;
    private DoubleProperty  minValue;
    private DoubleProperty  maxValue;
    private DoubleProperty  target;
    private double          _minMeasuredValue;
    private DoubleProperty  minMeasuredValue;
    private double          _maxMeasuredValue;
    private DoubleProperty  maxMeasuredValue;
    private int             _decimals;
    private IntegerProperty decimals;
    private String          _infoText;
    private StringProperty  infoText;
    private BooleanProperty targetEnabled;
    private double          _startAngle;
    private DoubleProperty  startAngle;
    private double          _angleRange;
    private DoubleProperty  angleRange;

    // CSS styleable properties
    private ObjectProperty<Paint> tickMarkFill;


    // ******************** Constructors **************************************
    public HeatControl() {
        getStyleClass().add("heat-control");
        value             = new DoublePropertyBase(0) {
            @Override protected void invalidated() {
                set(clamp(getMinValue(), getMaxValue(), get()));
            }
            @Override public Object getBean() { return this; }
            @Override public String getName() { return "value"; }
        };
        minValue          = new DoublePropertyBase(0) {
            @Override protected void invalidated() {
                if (getValue() < get()) setValue(get());
            }
            @Override public Object getBean() { return this; }
            @Override public String getName() { return "minValue"; }
        };
        maxValue          = new DoublePropertyBase(40) {
            @Override protected void invalidated() {
                if (getValue() > get()) setValue(get());
            }
            @Override public Object getBean() { return this; }
            @Override public String getName() { return "maxValue"; }
        };
        oldValue          = 0;
        target            = new DoublePropertyBase(20) {
            @Override protected void invalidated() {
                set(clamp(getMinValue(), getMaxValue(), get()));
            }
            @Override public Object getBean() { return this; }
            @Override public String getName() { return "target"; }
        };
        _minMeasuredValue = maxValue.getValue();
        _maxMeasuredValue = 0;
        _decimals         = 0;
        _infoText         = "";
        targetEnabled     = new SimpleBooleanProperty(this, "targetEnabled", false);
        _startAngle       = 325;
        _angleRange       = 290;
    }


    // ******************** Methods *******************************************
    public final double getValue() {
        return value.get();
    }
    public final void setValue(final double VALUE) {
        oldValue = value.get();
        value.set(clamp(getMinValue(), getMaxValue(), VALUE));
        if (Math.abs(value.get() - target.get()) < 0.5) {
            setInfoText("");
        }
    }
    public final DoubleProperty valueProperty() {
        return value;
    }

    public final double getOldValue() {
        return oldValue;
    }

    public final double getMinValue() {
        return minValue.get();
    }
    public final void setMinValue(final double MIN_VALUE) {
        minValue.set(clamp(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, MIN_VALUE));
        validate();
    }
    public final DoubleProperty minValueProperty() {
        return minValue;
    }

    public final double getMaxValue() {
        return maxValue.get();
    }
    public final void setMaxValue(final double MAX_VALUE) {
        maxValue.set(clamp(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, MAX_VALUE));
        validate();
    }
    public final DoubleProperty maxValueProperty() {
        return maxValue;
    }

    public final double getTarget() {
        return target.get();
    }
    public final void setTarget(final double TARGET) {
        target.set(clamp(getMinValue(), getMaxValue(), TARGET));
    }
    public final DoubleProperty thresholdProperty() {
        return target;
    }

    public final double getMinMeasuredValue() {
        return null == minMeasuredValue ? _minMeasuredValue : minMeasuredValue.get();
    }
    public final void setMinMeasuredValue(final double MIN_MEASURED_VALUE) {
        if (null == minMeasuredValue) {
            _minMeasuredValue = MIN_MEASURED_VALUE;
        } else {
            minMeasuredValue.set(MIN_MEASURED_VALUE);
        }
    }
    public final ReadOnlyDoubleProperty minMeasuredValueProperty() {
        if (null == minMeasuredValue) {
            minMeasuredValue = new SimpleDoubleProperty(this, "minMeasuredValue", _minMeasuredValue);
        }
        return minMeasuredValue;
    }

    public final double getMaxMeasuredValue() {
        return null == maxMeasuredValue ? _maxMeasuredValue : maxMeasuredValue.get();
    }
    public final void setMaxMeasuredValue(final double MAX_MEASURED_VALUE) {
        if (null == maxMeasuredValue) {
            _maxMeasuredValue = MAX_MEASURED_VALUE;
        } else {
            maxMeasuredValue.set(MAX_MEASURED_VALUE);
        }
    }
    public final ReadOnlyDoubleProperty maxMeasuredValueProperty() {
        if (null == maxMeasuredValue) {
            maxMeasuredValue = new SimpleDoubleProperty(this, "maxMeasuredValue", _maxMeasuredValue);
        }
        return maxMeasuredValue;
    }

    public void resetMinMeasuredValue() {
        setMinMeasuredValue(getValue());
    }
    public void resetMaxMeasuredValue() {
        setMaxMeasuredValue(getValue());
    }
    public void resetMinAndMaxMeasuredValue() {
        setMinMeasuredValue(getValue());
        setMaxMeasuredValue(getValue());
    }

    public final int getDecimals() {
        return null == decimals ? _decimals : decimals.get();
    }
    public final void setDecimals(final int DECIMALS) {
        if (null == decimals) {
            _decimals = clamp(0, 3, DECIMALS);
        } else {
            decimals.set(clamp(0, 3, DECIMALS));
        }
    }
    public final IntegerProperty decimalsProperty() {
        if (null == decimals) {
            decimals = new SimpleIntegerProperty(this, "decimals", _decimals);
        }
        return decimals;
    }

    public final String getInfoText() {
        return null == infoText ? _infoText : infoText.get();
    }
    public final void setInfoText(final String INFO_TEXT) {
        if (null == infoText) {
            _infoText = INFO_TEXT;
        } else {
            infoText.set(INFO_TEXT);
        }
    }
    public final StringProperty infoTextProperty() {
        if (null == infoText) {
            infoText = new SimpleStringProperty(this, "infoText", _infoText);
        }
        return infoText;
    }

    public final boolean isTargetEnabled() {
        return targetEnabled.get();
    }
    public final void setTargetEnabled(final boolean TARGET_ENABLED) {
        targetEnabled.set(TARGET_ENABLED);
    }
    public final BooleanProperty targetEnabledProperty() {
        return targetEnabled;
    }

    public double getStartAngle() {
        return null == startAngle ? _startAngle : startAngle.get();
    }
    public final void setStartAngle(final double START_ANGLE) {
        if (null == startAngle) {
            _startAngle = clamp(0, 360, START_ANGLE);
        } else {
            startAngle.set(clamp(0, 360, START_ANGLE));
        }
    }
    public final DoubleProperty startAngleProperty() {
        if (null == startAngle) {
            startAngle = new SimpleDoubleProperty(this, "startAngle", _startAngle);
        }
        return startAngle;
    }

    public final double getAngleRange() {
        return null == angleRange ? _angleRange : angleRange.get();
    }
    public final void setAngleRange(final double ANGLE_RANGE) {
        if (null == angleRange) {
            _angleRange = clamp(0.0, 360.0, ANGLE_RANGE);
        } else {
            angleRange.set(clamp(0.0, 360.0, ANGLE_RANGE));
        }
    }
    public final DoubleProperty angleRangeProperty() {
        if (null == angleRange) {
            angleRange = new SimpleDoubleProperty(this, "angleRange", _angleRange);
        }
        return angleRange;
    }

    private double clamp(final double MIN_VALUE, final double MAX_VALUE, final double VALUE) {
        if (VALUE < MIN_VALUE) return MIN_VALUE;
        if (VALUE > MAX_VALUE) return MAX_VALUE;
        return VALUE;
    }
    private int clamp(final int MIN_VALUE, final int MAX_VALUE, final int VALUE) {
        if (VALUE < MIN_VALUE) return MIN_VALUE;
        if (VALUE > MAX_VALUE) return MAX_VALUE;
        return VALUE;
    }
    private Duration clamp(final Duration MIN_VALUE, final Duration MAX_VALUE, final Duration VALUE) {
        if (VALUE.lessThan(MIN_VALUE)) return MIN_VALUE;
        if (VALUE.greaterThan(MAX_VALUE)) return MAX_VALUE;
        return VALUE;
    }

    private void validate() {
        if (getTarget() < getMinValue()) setTarget(getMinValue());
        if (getTarget() > getMaxValue()) setTarget(getMaxValue());
        if (getValue() < getMinValue()) setValue(getMinValue());
        if (getValue() > getMaxValue()) setValue(getMaxValue());
    }


    // ******************** CSS Stylable Properties ***************************
    public final Paint getTickMarkFill() {
        return null == tickMarkFill ? Color.BLACK : tickMarkFill.get();
    }
    public final void setTickMarkFill(Paint value) {
        tickMarkFillProperty().set(value);
    }
    public final ObjectProperty<Paint> tickMarkFillProperty() {
        if (null == tickMarkFill) {
            tickMarkFill = new StyleableObjectProperty<Paint>(Color.BLACK) {

                @Override public CssMetaData getCssMetaData() { return StyleableProperties.TICK_MARK_FILL; }

                @Override public Object getBean() { return HeatControl.this; }

                @Override public String getName() { return "tickMarkFill"; }
            };
        }
        return tickMarkFill;
    }


    // ******************** Style related *************************************
    @Override protected Skin createDefaultSkin() {
        return new HeatControlSkin(this);
    }

    @Override public String getUserAgentStylesheet() {
        return getClass().getResource("/eu/hansolo/enzo/heatcontrol/heatcontrol.css").toExternalForm();
    }

    private static class StyleableProperties {
        private static final CssMetaData<HeatControl, Paint> TICK_MARK_FILL =
            new CssMetaData<HeatControl, Paint>("-tick-mark-fill", PaintConverter.getInstance(), Color.WHITE) {

                @Override public boolean isSettable(HeatControl heatControl) {
                    return null == heatControl.tickMarkFill || !heatControl.tickMarkFill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(HeatControl heatControl) {
                    return (StyleableProperty) heatControl.tickMarkFillProperty();
                }
            };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables,
                               TICK_MARK_FILL);
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
