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
import eu.hansolo.enzo.common.Marker;
import eu.hansolo.enzo.common.Section;
import eu.hansolo.enzo.gauge.skin.GaugeSkin;
import eu.hansolo.enzo.gauge.skin.RadialSteelGaugeSkin;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.css.*;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.util.*;


/**
 * Created by hansolo on 21.07.14.
 */
public class RadialSteelGauge extends Control {
    public static enum TickLabelOrientation {
        ORTHOGONAL,
        HORIZONTAL,
        TANGENT
    }
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

    private DoubleProperty                        value;
    private DoubleProperty                        minValue;
    private double                                exactMinValue;
    private DoubleProperty                        maxValue;
    private double                                exactMaxValue;
    private int                                   _decimals;
    private IntegerProperty                       decimals;
    private String                                _title;
    private StringProperty                        title;
    private String                                _unit;
    private StringProperty                        unit;
    private boolean                               _animated;
    private BooleanProperty                       animated;
    private double                                animationDuration;
    private double                                _startAngle;
    private DoubleProperty                        startAngle;
    private double                                _angleRange;
    private DoubleProperty                        angleRange;
    private boolean                               _autoScale;
    private BooleanProperty                       autoScale;
    private Color                                 _needleColor;
    private ObjectProperty<Color>                 needleColor;
    private RadialSteelGauge.TickLabelOrientation _tickLabelOrientation;
    private ObjectProperty<TickLabelOrientation>  tickLabelOrientation;
    private RadialSteelGauge.NumberFormat         _numberFormat;
    private ObjectProperty<NumberFormat>          numberFormat;
    private double                                _majorTickSpace;
    private DoubleProperty                        majorTickSpace;
    private double                                _minorTickSpace;
    private DoubleProperty                        minorTickSpace;
    private boolean                               _dropShadowEnabled;
    private BooleanProperty                       dropShadowEnabled;

    // CSS styleable properties
    private ObjectProperty<Paint>                 tickMarkFill;
    private ObjectProperty<Paint>                 tickLabelFill;


    // ******************** Constructors **************************************
    public RadialSteelGauge() {
        getStyleClass().add("gauge");
        value                 = new DoublePropertyBase(0) {
            @Override public void set(final double VALUE) {
                super.set(clamp(getMinValue(), getMaxValue(), VALUE));
            }

            @Override public Object getBean() { return this; }

            @Override public String getName() { return "value"; }
        };
        minValue              = new SimpleDoubleProperty(this, "minValue", 0);
        maxValue              = new SimpleDoubleProperty(this, "maxValue", 100);
        _decimals             = 1;
        _title                = "";
        _unit                 = "";
        _animated             = true;
        _startAngle           = 320;
        _angleRange           = 280;
        _autoScale            = false;
        _needleColor          = Color.RED;
        _tickLabelOrientation = TickLabelOrientation.HORIZONTAL;
        _numberFormat         = NumberFormat.STANDARD;
        _majorTickSpace       = 10;
        _minorTickSpace       = 1;
        animationDuration     = 800;
        _dropShadowEnabled    = true;
    }


    // ******************** Methods *******************************************
    public final double getValue() {
        return value.get();
    }
    public final void setValue(final double VALUE) {
        value.set(VALUE);
    }
    public final DoubleProperty valueProperty() {
        return value;
    }

    public final double getMinValue() {
        return minValue.get();
    }
    public final void setMinValue(final double MIN_VALUE) {
        minValue.set(MIN_VALUE);
    }
    public final DoubleProperty minValueProperty() {
        return minValue;
    }

    public final double getMaxValue() {
        return maxValue.get();
    }
    public final void setMaxValue(final double MAX_VALUE) {
        maxValue.set(MAX_VALUE);
    }
    public final DoubleProperty maxValueProperty() {
        return maxValue;
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

    public final String getTitle() {
        return null == title ? _title : title.get();
    }
    public final void setTitle(final String TITLE) {
        if (null == title) {
            _title = TITLE;
        } else {
            title.set(TITLE);
        }
    }
    public final StringProperty titleProperty() {
        if (null == title) {
            title = new SimpleStringProperty(this, "title", _title);
        }
        return title;
    }

    public final String getUnit() {
        return null == unit ? _unit : unit.get();
    }
    public final void setUnit(final String UNIT) {
        if (null == unit) {
            _unit = UNIT;
        } else {
            unit.set(UNIT);
        }
    }
    public final StringProperty unitProperty() {
        if (null == unit) {
            unit = new SimpleStringProperty(this, "unit", _unit);
        }
        return unit;
    }

    public final boolean isAnimated() {
        return null == animated ? _animated : animated.get();
    }
    public final void setAnimated(final boolean ANIMATED) {
        if (null == animated) {
            _animated = ANIMATED;
        } else {
            animated.set(ANIMATED);
        }
    }
    public final BooleanProperty animatedProperty() {
        if (null == animated) {
            animated = new SimpleBooleanProperty(this, "animated", _animated);
        }
        return animated;
    }

    public double getStartAngle() {
        return null == startAngle ? _startAngle : startAngle.get();
    }
    public final void setStartAngle(final double START_ANGLE) {
        if (null == startAngle) {
            _startAngle = clamp(0, 360, START_ANGLE);
        } else {
            startAngle.set(START_ANGLE);
        }
    }
    public final DoubleProperty startAngleProperty() {
        if (null == startAngle) {
            startAngle = new DoublePropertyBase(_startAngle) {
                @Override public void set(final double START_ANGLE) {
                    super.set(clamp(0d, 360d, get()));
                }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "startAngle"; }
            };
        }
        return startAngle;
    }

    public final double getAnimationDuration() {
        return animationDuration;
    }
    public final void setAnimationDuration(final double ANIMATION_DURATION) {
        animationDuration = clamp(20, 5000, ANIMATION_DURATION);
    }

    public final double getAngleRange() {
        return null == angleRange ? _angleRange : angleRange.get();
    }
    public final void setAngleRange(final double ANGLE_RANGE) {
        if (null == angleRange) {
            _angleRange = clamp(0.0, 360.0, ANGLE_RANGE);
        } else {
            angleRange.set(ANGLE_RANGE);
        }
    }
    public final DoubleProperty angleRangeProperty() {
        if (null == angleRange) {
            angleRange = new DoublePropertyBase(_angleRange) {
                @Override public void set(final double ANGLE_RANGE) {
                    super.set(clamp(0d, 360d, get()));
                }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "angleRange"; }
            };
        }
        return angleRange;
    }

    public final boolean isAutoScale() {
        return null == autoScale ? _autoScale : autoScale.get();
    }
    public final void setAutoScale(final boolean AUTO_SCALE) {
        if (null == autoScale) {
            _autoScale = AUTO_SCALE;
        } else {
            autoScale.set(AUTO_SCALE);
        }
    }
    public final BooleanProperty autoScaleProperty() {
        if (null == autoScale) {
            autoScale = new BooleanPropertyBase(_autoScale) {
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

    public final Color getNeedleColor() {
        return null == needleColor ? _needleColor : needleColor.get();
    }
    public final void setNeedleColor(final Color NEEDLE_COLOR) {
        if (null == needleColor) {
            _needleColor = NEEDLE_COLOR;
        } else {
            needleColor.set(NEEDLE_COLOR);
        }
    }
    public final ObjectProperty<Color> needleColorProperty() {
        if (null == needleColor) {
            needleColor = new SimpleObjectProperty<>(this, "needleColor", _needleColor);
        }
        return needleColor;
    }

    public final RadialSteelGauge.TickLabelOrientation getTickLabelOrientation() {
        return null == tickLabelOrientation ? _tickLabelOrientation : tickLabelOrientation.get();
    }
    public final void setTickLabelOrientation(final RadialSteelGauge.TickLabelOrientation TICK_LABEL_ORIENTATION) {
        if (null == tickLabelOrientation) {
            _tickLabelOrientation = TICK_LABEL_ORIENTATION;
        } else {
            tickLabelOrientation.set(TICK_LABEL_ORIENTATION);
        }
    }
    public final ObjectProperty<TickLabelOrientation> tickLabelOrientationProperty() {
        if (null == tickLabelOrientation) {
            tickLabelOrientation = new SimpleObjectProperty<>(this, "tickLabelOrientation", _tickLabelOrientation);
        }
        return tickLabelOrientation;
    }

    public final RadialSteelGauge.NumberFormat getNumberFormat() {
        return null == numberFormat ? _numberFormat : numberFormat.get();
    }
    public final void setNumberFormat(final RadialSteelGauge.NumberFormat NUMBER_FORMAT) {
        if (null == numberFormat) {
            _numberFormat = NUMBER_FORMAT;
        } else {
            numberFormat.set(NUMBER_FORMAT);
        }
    }
    public final ObjectProperty<NumberFormat> numberFormatProperty() {
        if (null == numberFormat) {
            numberFormat = new SimpleObjectProperty<>(this, "numberFormat", _numberFormat);
        }
        return numberFormat;
    }

    public final double getMajorTickSpace() {
        return null == majorTickSpace ? _majorTickSpace : majorTickSpace.get();
    }
    public final void setMajorTickSpace(final double MAJOR_TICK_SPACE) {
        if (null == majorTickSpace) {
            _majorTickSpace = MAJOR_TICK_SPACE;
        } else {
            majorTickSpace.set(MAJOR_TICK_SPACE);
        }
    }
    public final DoubleProperty majorTickSpaceProperty() {
        if (null == majorTickSpace) {
            majorTickSpace = new SimpleDoubleProperty(this, "majorTickSpace", _majorTickSpace);
        }
        return majorTickSpace;
    }

    public final double getMinorTickSpace() {
        return null == minorTickSpace ? _minorTickSpace : minorTickSpace.get();
    }
    public final void setMinorTickSpace(final double MINOR_TICK_SPACE) {
        if (null == minorTickSpace) {
            _minorTickSpace = MINOR_TICK_SPACE;
        } else {
            minorTickSpace.set(MINOR_TICK_SPACE);
        }
    }
    public final DoubleProperty minorTickSpaceProperty() {
        if (null == minorTickSpace) {
            minorTickSpace = new SimpleDoubleProperty(this, "minorTickSpace", _minorTickSpace);
        }
        return minorTickSpace;
    }

    public final boolean isDropShadowEnabled() {
        return null == dropShadowEnabled ? _dropShadowEnabled : dropShadowEnabled.get();
    }
    public final void setDropShadowEnabled(final boolean DROP_SHADOW_ENABLED) {
        if (null == dropShadowEnabled) {
            _dropShadowEnabled = DROP_SHADOW_ENABLED;
        } else {
            dropShadowEnabled.set(DROP_SHADOW_ENABLED);
        }
    }
    public final BooleanProperty dropShadowEnabledProperty() {
        if (null == dropShadowEnabled) {
            dropShadowEnabled = new SimpleBooleanProperty(this, "dropShadowEnabled", _dropShadowEnabled);
        }
        return dropShadowEnabled;
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

    public void calcAutoScale() {
        if (isAutoScale()) {
            double maxNoOfMajorTicks = 10;
            double maxNoOfMinorTicks = 10;
            double niceMinValue;
            double niceMaxValue;
            double niceRange;
            niceRange = (calcNiceNumber((getMaxValue() - getMinValue()), false));
            majorTickSpace.set(calcNiceNumber(niceRange / (maxNoOfMajorTicks - 1), true));
            niceMinValue = (Math.floor(getMinValue() / majorTickSpace.doubleValue()) * majorTickSpace.doubleValue());
            niceMaxValue = (Math.ceil(getMaxValue() / majorTickSpace.doubleValue()) * majorTickSpace.doubleValue());
            minorTickSpace.set(calcNiceNumber(majorTickSpace.doubleValue() / (maxNoOfMinorTicks - 1), true));
            setMinValue(niceMinValue);
            setMaxValue(niceMaxValue);
        }
    }

    /**
     * Returns a "niceScaling" number approximately equal to the range.
     * Rounds the number if ROUND == true.
     * Takes the ceiling if ROUND = false.
     *
     * @param RANGE the value range (maxValue - minValue)
     * @param ROUND whether to round the result or ceil
     * @return a "niceScaling" number to be used for the value range
     */
    private double calcNiceNumber(final double RANGE, final boolean ROUND) {
        final double EXPONENT = Math.floor(Math.log10(RANGE));   // exponent of range
        final double FRACTION = RANGE / Math.pow(10, EXPONENT);  // fractional part of range
        //final double MOD      = FRACTION % 0.5;                  // needed for large number scale
        double niceFraction;

        // niceScaling
        /*
        if (isLargeNumberScale()) {
            if (MOD != 0) {
                niceFraction = FRACTION - MOD;
                niceFraction += 0.5;
            } else {
                niceFraction = FRACTION;
            }
        } else {
        */

        if (ROUND) {
            if (FRACTION < 1.5) {
                niceFraction = 1;
            } else if (FRACTION < 3) {
                niceFraction = 2;
            } else if (FRACTION < 7) {
                niceFraction = 5;
            } else {
                niceFraction = 10;
            }
        } else {
            if (Double.compare(FRACTION, 1) <= 0) {
                niceFraction = 1;
            } else if (Double.compare(FRACTION, 2) <= 0) {
                niceFraction = 2;
            } else if (Double.compare(FRACTION, 5) <= 0) {
                niceFraction = 5;
            } else {
                niceFraction = 10;
            }
        }
        //}
        return niceFraction * Math.pow(10, EXPONENT);
    }

    public void validate() {
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

                @Override public Object getBean() { return RadialSteelGauge.this; }

                @Override public String getName() { return "tickMarkFill"; }
            };
        }
        return tickMarkFill;
    }

    public final Paint getTickLabelFill() {
        return null == tickLabelFill ? Color.BLACK : tickLabelFill.get();
    }
    public final void setTickLabelFill(Paint value) {
        tickLabelFillProperty().set(value);
    }
    public final ObjectProperty<Paint> tickLabelFillProperty() {
        if (null == tickLabelFill) {
            tickLabelFill = new StyleableObjectProperty<Paint>(Color.BLACK) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.TICK_LABEL_FILL; }
                @Override public Object getBean() { return RadialSteelGauge.this; }
                @Override public String getName() { return "tickLabelFill"; }
            };
        }
        return tickLabelFill;
    }


    // ******************** Style related *************************************
    @Override protected Skin createDefaultSkin() {
        return new RadialSteelGaugeSkin(this);
    }

    @Override public String getUserAgentStylesheet() {
        return getClass().getResource("/eu/hansolo/enzo/gauge/steel-gauge.css").toExternalForm();
    }

    private static class StyleableProperties {
        private static final CssMetaData<RadialSteelGauge, Paint> TICK_MARK_FILL =
                new CssMetaData<RadialSteelGauge, Paint>("-tick-mark-fill", PaintConverter.getInstance(), Color.BLACK) {

                    @Override public boolean isSettable(RadialSteelGauge gauge) {
                        return null == gauge.tickMarkFill || !gauge.tickMarkFill.isBound();
                    }

                    @Override public StyleableProperty<Paint> getStyleableProperty(RadialSteelGauge gauge) {
                        return (StyleableProperty) gauge.tickMarkFillProperty();
                    }
                };

        private static final CssMetaData<RadialSteelGauge, Paint> TICK_LABEL_FILL =
                new CssMetaData<RadialSteelGauge, Paint>("-tick-label-fill", PaintConverter.getInstance(), Color.BLACK) {

                    @Override public boolean isSettable(RadialSteelGauge gauge) {
                        return null == gauge.tickLabelFill || !gauge.tickLabelFill.isBound();
                    }

                    @Override public StyleableProperty<Paint> getStyleableProperty(RadialSteelGauge gauge) {
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
