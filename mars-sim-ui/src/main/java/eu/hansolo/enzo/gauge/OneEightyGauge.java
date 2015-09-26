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
import eu.hansolo.enzo.common.GradientLookup;
import eu.hansolo.enzo.gauge.skin.OneEightyGaugeSkin;
import javafx.beans.property.*;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * User: hansolo
 * Date: 29.12.13
 * Time: 07:49
 */
public class OneEightyGauge extends Control {
    private static final Paint DEFAULT_BAR_BACKGROUND_COLOR = Color.web("#eff3f3");
    private static final Paint DEFAULT_BAR_COLOR            = Color.web("#3221c9");
    private static final Paint DEFAULT_TITLE_COLOR          = Color.BLACK;
    private static final Paint DEFAULT_VALUE_COLOR          = Color.BLACK;
    private static final Paint DEFAULT_UNIT_COLOR           = Color.BLACK;
    private static final Paint DEFAULT_MIN_TEXT_COLOR       = Color.BLACK;
    private static final Paint DEFAULT_MAX_TEXT_COLOR       = Color.BLACK;

    private DoubleProperty        value;
    private DoubleProperty        minValue;
    private DoubleProperty        maxValue;
    private double                minMeasuredValue;
    private double                maxMeasuredValue;
    private IntegerProperty       decimals;
    private StringProperty        title;
    private StringProperty        unit;
    private BooleanProperty       shadowsEnabled;
    private BooleanProperty       animated;
    private double                animationDuration;
    private BooleanProperty       dynamicBarColor;
    private GradientLookup        gradientLookup;
    private ObjectProperty<Paint> barBackgroundColor;
    private ObjectProperty<Paint> barColor;
    private ObjectProperty<Paint> titleColor;
    private ObjectProperty<Paint> valueColor;
    private ObjectProperty<Paint> unitColor;
    private ObjectProperty<Paint> minTextColor;
    private ObjectProperty<Paint> maxTextColor;


    // ******************** Constructors **************************************
    public OneEightyGauge() {
        getStyleClass().add("one-eighty-gauge");

        value             = new DoublePropertyBase(0) {
            @Override public void set(final double VALUE) {
                super.set(clamp(getMinValue(), getMaxValue(), VALUE));
            }
            @Override public Object getBean() { return this; }
            @Override public String getName() { return "value"; }
        };
        minValue          = new SimpleDoubleProperty(this, "minValue", 0);
        maxValue          = new SimpleDoubleProperty(this, "maxValue", 100);
        decimals          = new SimpleIntegerProperty(this, "decimals", 0);
        title             = new SimpleStringProperty(this, "title", "");
        unit              = new SimpleStringProperty(this, "unit", "");
        shadowsEnabled    = new SimpleBooleanProperty(this, "shadowsEnabled", true);
        animated          = new SimpleBooleanProperty(this, "animated", false);
        animationDuration = 800;
        gradientLookup    = new GradientLookup();
    }


    // ******************** Methods *******************************************
    public final double getValue() { return value.get(); }
    public final void setValue(final double VALUE) { value.set(VALUE); }
    public final DoubleProperty valueProperty() { return value; }

    public final double getMinValue() { return minValue.get(); }
    public final void setMinValue(final double MIN_VALUE) { minValue.set(MIN_VALUE); }
    public final DoubleProperty minValueProperty() { return minValue; }

    public final double getMaxValue() { return maxValue.get(); }
    public final void setMaxValue(final double MAX_VALUE) { maxValue.set(MAX_VALUE); }
    public final DoubleProperty maxValueProperty() { return maxValue; }

    public final double getMinMeasuredValue() { return minMeasuredValue; }
    public final void setMinMeasuredValue(final double MIN_MEASURED_VALUE) { minMeasuredValue = clamp(getMinValue(), getMaxValue(), MIN_MEASURED_VALUE); }

    public final double getMaxMeasuredValue() { return maxMeasuredValue; }
    public final void setMaxMeasuredValue(final double MAX_MEASURED_VALUE) { maxMeasuredValue = clamp(getMinValue(), getMaxValue(), MAX_MEASURED_VALUE); }

    public final void resetMinMaxMeasuredValues() {
        minMeasuredValue = getValue();
        maxMeasuredValue = getValue();
    }

    public final int getDecimals() { return decimals.get(); }
    public final void setDecimals(final int DECIMALS) { decimals.set(DECIMALS); }
    public final IntegerProperty decimalsProperty() { return decimals; }

    public final String getTitle() { return title.get(); }
    public final void setTitle(final String TITLE) { title.set(TITLE); }
    public final StringProperty titleProperty() { return title; }

    public final String getUnit() { return unit.get(); }
    public final void setUnit(final String UNIT) { unit.set(UNIT); }
    public final StringProperty unitProperty() { return unit; }

    public final boolean isShadowsEnabled() { return shadowsEnabled.get(); }
    public final void setShadowsEnabled(final boolean SHADOWS_ENABLED) { shadowsEnabled.set(SHADOWS_ENABLED); }
    public final BooleanProperty shadowsEnabledProperty() { return shadowsEnabled; }

    public final boolean isAnimated() { return animated.get(); }
    public final void setAnimated(final boolean ANIMATED) { animated.set(ANIMATED); }
    public final BooleanProperty animatedProperty() { return animated; }

    public final double getAnimationDuration() { return animationDuration; }
    public final void setAnimationDuration(final double ANIMATION_DURATION) { animationDuration = clamp(20, 5000, ANIMATION_DURATION); }

    public final boolean isDynamicBarColor() { return null == dynamicBarColor ? false : dynamicBarColor.get(); }
    public final void setDynamicBarColor(final boolean DYNAMIC_BAR_COLOR) { dynamicBarColorProperty().set(DYNAMIC_BAR_COLOR); }
    public final BooleanProperty dynamicBarColorProperty() {
        if (null == dynamicBarColor) {
            dynamicBarColor = new SimpleBooleanProperty(this, "dynamicBarColor", false);
        }
        return dynamicBarColor;
    }

    public final GradientLookup getGradientLookup() { return gradientLookup; }
    public final void setGradientLookup(final GradientLookup GRADIENT_LOOKUP) {
        gradientLookup.setStops(GRADIENT_LOOKUP.getStops());
        gradientLookup = GRADIENT_LOOKUP;
    }

    public final List<Stop> getStops() { return gradientLookup.getStops(); }
    public final void setStops(final Stop... STOPS) { gradientLookup.setStops(STOPS);}
    public final void setStops(final List<Stop> STOPS) { gradientLookup.setStops(STOPS);}


    // ******************** CSS Stylable Properties ***************************
    public final Paint getBarBackgroundColor() {
        return null == barBackgroundColor ? DEFAULT_BAR_BACKGROUND_COLOR : barBackgroundColor.get();
    }
    public final void setBarBackgroundColor(Paint value) {
        barBackgroundColorProperty().set(value);
    }
    public final ObjectProperty<Paint> barBackgroundColorProperty() {
        if (null == barBackgroundColor) {
            barBackgroundColor = new StyleableObjectProperty<Paint>(DEFAULT_BAR_BACKGROUND_COLOR) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.BAR_BACKGROUND_COLOR; }
                @Override public Object getBean() { return OneEightyGauge.this; }
                @Override public String getName() { return "barBackgroundColor"; }
            };
        }
        return barBackgroundColor;
    }

    public final Paint getBarColor() {
        return null == barColor ? DEFAULT_BAR_COLOR : barColor.get();
    }
    public final void setBarColor(Paint value) {
        barColorProperty().set(value);
    }
    public final ObjectProperty<Paint> barColorProperty() {
        if (null == barColor) {
            barColor = new StyleableObjectProperty<Paint>(DEFAULT_BAR_COLOR) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.BAR_COLOR; }
                @Override public Object getBean() { return OneEightyGauge.this; }
                @Override public String getName() { return "barColor"; }
            };
        }
        return barColor;
    }

    public final Paint getTitleColor() {
        return null == titleColor ? DEFAULT_TITLE_COLOR : titleColor.get();
    }
    public final void setTitleColor(Paint value) {
        titleColorProperty().set(value);
    }
    public final ObjectProperty<Paint> titleColorProperty() {
        if (null == titleColor) {
            titleColor = new StyleableObjectProperty<Paint>(DEFAULT_TITLE_COLOR) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.TITLE_COLOR; }
                @Override public Object getBean() { return OneEightyGauge.this; }
                @Override public String getName() { return "titleColor"; }
            };
        }
        return titleColor;
    }

    public final Paint getValueColor() {
        return null == valueColor ? DEFAULT_VALUE_COLOR : valueColor.get();
    }
    public final void setValueColor(Paint value) {
        valueColorProperty().set(value);
    }
    public final ObjectProperty<Paint> valueColorProperty() {
        if (null == valueColor) {
            valueColor = new StyleableObjectProperty<Paint>(DEFAULT_VALUE_COLOR) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.VALUE_COLOR; }
                @Override public Object getBean() { return OneEightyGauge.this; }
                @Override public String getName() { return "valueColor"; }
            };
        }
        return valueColor;
    }

    public final Paint getUnitColor() {
        return null == unitColor ? DEFAULT_UNIT_COLOR : unitColor.get();
    }
    public final void setUnitColor(Paint value) {
        unitColorProperty().set(value);
    }
    public final ObjectProperty<Paint> unitColorProperty() {
        if (null == unitColor) {
            unitColor = new StyleableObjectProperty<Paint>(DEFAULT_UNIT_COLOR) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.UNIT_COLOR; }
                @Override public Object getBean() { return OneEightyGauge.this; }
                @Override public String getName() { return "unitColor"; }
            };
        }
        return unitColor;
    }

    public final Paint getMinTextColor() {
        return null == minTextColor ? DEFAULT_MIN_TEXT_COLOR : minTextColor.get();
    }
    public final void setMinTextColor(Paint value) {
        minTextColorProperty().set(value);
    }
    public final ObjectProperty<Paint> minTextColorProperty() {
        if (null == minTextColor) {
            minTextColor = new StyleableObjectProperty<Paint>(DEFAULT_MIN_TEXT_COLOR) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.MIN_TEXT_COLOR; }
                @Override public Object getBean() { return OneEightyGauge.this; }
                @Override public String getName() { return "minTextColor"; }
            };
        }
        return minTextColor;
    }

    public final Paint getMaxTextColor() {
        return null == maxTextColor ? DEFAULT_MAX_TEXT_COLOR : maxTextColor.get();
    }
    public final void setMaxTextColor(Paint value) {
        maxTextColorProperty().set(value);
    }
    public final ObjectProperty<Paint> maxTextColorProperty() {
        if (null == maxTextColor) {
            maxTextColor = new StyleableObjectProperty<Paint>(DEFAULT_MAX_TEXT_COLOR) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.MAX_TEXT_COLOR; }
                @Override public Object getBean() { return OneEightyGauge.this; }
                @Override public String getName() { return "maxTextColor"; }
            };
        }
        return maxTextColor;
    }


    // ******************** Utility methods ***********************************
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


    // ******************** Style related *************************************
    @Override protected Skin createDefaultSkin() {
        return new OneEightyGaugeSkin(this);
    }

    @Override public String getUserAgentStylesheet() {
        return getClass().getResource("/eu/hansolo/enzo/gauge/oneeightygauge.css").toExternalForm();
    }

    private static class StyleableProperties {
        private static final CssMetaData<OneEightyGauge, Paint> BAR_BACKGROUND_COLOR =
            new CssMetaData<OneEightyGauge, Paint>("-bar-background-color",
                                                   PaintConverter.getInstance(),
                                                   DEFAULT_BAR_BACKGROUND_COLOR) {

                @Override public boolean isSettable(OneEightyGauge node) {
                    return null == node.barBackgroundColor || !node.barBackgroundColor.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(OneEightyGauge node) {
                    return (StyleableProperty) node.barBackgroundColorProperty();
                }

                @Override public Color getInitialValue(OneEightyGauge node) {
                    return (Color) node.getBarBackgroundColor();
                }
            };

        private static final CssMetaData<OneEightyGauge, Paint> BAR_COLOR =
            new CssMetaData<OneEightyGauge, Paint>("-bar-color",
                                                   PaintConverter.getInstance(),
                                                   DEFAULT_BAR_COLOR) {

                @Override public boolean isSettable(OneEightyGauge node) {
                    return null == node.barColor || !node.barColor.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(OneEightyGauge node) {
                    return (StyleableProperty) node.barColorProperty();
                }

                @Override public Color getInitialValue(OneEightyGauge node) {
                    return (Color) node.getBarColor();
                }
            };

        private static final CssMetaData<OneEightyGauge, Paint> TITLE_COLOR =
            new CssMetaData<OneEightyGauge, Paint>("-title-color",
                                                   PaintConverter.getInstance(),
                                                   DEFAULT_TITLE_COLOR) {

                @Override public boolean isSettable(OneEightyGauge node) {
                    return null == node.titleColor || !node.titleColor.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(OneEightyGauge node) {
                    return (StyleableProperty) node.titleColorProperty();
                }

                @Override public Color getInitialValue(OneEightyGauge node) {
                    return (Color) node.getTitleColor();
                }
            };

        private static final CssMetaData<OneEightyGauge, Paint> VALUE_COLOR =
            new CssMetaData<OneEightyGauge, Paint>("-value-color",
                                                   PaintConverter.getInstance(),
                                                   DEFAULT_VALUE_COLOR) {

                @Override public boolean isSettable(OneEightyGauge node) {
                    return null == node.valueColor || !node.valueColor.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(OneEightyGauge node) {
                    return (StyleableProperty) node.valueColorProperty();
                }

                @Override public Color getInitialValue(OneEightyGauge node) {
                    return (Color) node.getValueColor();
                }
            };

        private static final CssMetaData<OneEightyGauge, Paint> UNIT_COLOR =
            new CssMetaData<OneEightyGauge, Paint>("-unit-color",
                                                   PaintConverter.getInstance(),
                                                   DEFAULT_UNIT_COLOR) {

                @Override public boolean isSettable(OneEightyGauge node) {
                    return null == node.unitColor || !node.unitColor.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(OneEightyGauge node) {
                    return (StyleableProperty) node.unitColorProperty();
                }

                @Override public Color getInitialValue(OneEightyGauge node) {
                    return (Color) node.getUnitColor();
                }
            };

        private static final CssMetaData<OneEightyGauge, Paint> MIN_TEXT_COLOR =
            new CssMetaData<OneEightyGauge, Paint>("-min-text-color",
                                                   PaintConverter.getInstance(),
                                                   DEFAULT_MIN_TEXT_COLOR) {

                @Override public boolean isSettable(OneEightyGauge node) {
                    return null == node.minTextColor || !node.minTextColor.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(OneEightyGauge node) {
                    return (StyleableProperty) node.minTextColorProperty();
                }

                @Override public Color getInitialValue(OneEightyGauge node) {
                    return (Color) node.getMinTextColor();
                }
            };

        private static final CssMetaData<OneEightyGauge, Paint> MAX_TEXT_COLOR =
            new CssMetaData<OneEightyGauge, Paint>("-max-text-color",
                                                   PaintConverter.getInstance(),
                                                   DEFAULT_MAX_TEXT_COLOR) {

                @Override public boolean isSettable(OneEightyGauge node) {
                    return null == node.maxTextColor || !node.maxTextColor.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(OneEightyGauge node) {
                    return (StyleableProperty) node.maxTextColorProperty();
                }

                @Override public Color getInitialValue(OneEightyGauge node) {
                    return (Color) node.getMaxTextColor();
                }
            };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables,
                               BAR_BACKGROUND_COLOR,
                               BAR_COLOR,
                               TITLE_COLOR,
                               VALUE_COLOR,
                               UNIT_COLOR,
                               MIN_TEXT_COLOR,
                               MAX_TEXT_COLOR
            );
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
