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

import eu.hansolo.enzo.common.GradientLookup;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Dimension2D;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;

import java.util.HashMap;
import java.util.List;


/**
 * User: hansolo
 * Date: 29.12.13
 * Time: 07:58
 */
public class OneEightyGaugeBuilder<B extends OneEightyGaugeBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    protected OneEightyGaugeBuilder() {}


    // ******************** Methods *******************************************
    public static final OneEightyGaugeBuilder create() {
        return new OneEightyGaugeBuilder();
    }

    public final OneEightyGaugeBuilder value(final double VALUE) {
        properties.put("value", new SimpleDoubleProperty(VALUE));
        return this;
    }

    public final OneEightyGaugeBuilder minValue(final double MIN_VALUE) {
        properties.put("minValue", new SimpleDoubleProperty(MIN_VALUE));
        return this;
    }

    public final OneEightyGaugeBuilder maxValue(final double MAX_VALUE) {
        properties.put("maxValue", new SimpleDoubleProperty(MAX_VALUE));
        return this;
    }

    public final OneEightyGaugeBuilder decimals(final int DECIMALS) {
        properties.put("decimals", new SimpleIntegerProperty(DECIMALS));
        return this;
    }

    public final OneEightyGaugeBuilder title(final String TITLE) {
        properties.put("title", new SimpleStringProperty(TITLE));
        return this;
    }

    public final OneEightyGaugeBuilder unit(final String UNIT) {
        properties.put("unit", new SimpleStringProperty(UNIT));
        return this;
    }

    public final OneEightyGaugeBuilder shadowsEnabled(final boolean SHADOWS_ENABLED) {
        properties.put("shadowsEnabled", new SimpleBooleanProperty(SHADOWS_ENABLED));
        return this;
    }
    
    public final OneEightyGaugeBuilder animated(final boolean ANIMATED) {
        properties.put("animated", new SimpleBooleanProperty(ANIMATED));
        return this;
    }

    public final OneEightyGaugeBuilder animationDuration(final double ANIMATION_DURATION) {
        properties.put("animationDuration", new SimpleDoubleProperty(ANIMATION_DURATION));
        return this;
    }

    public final OneEightyGaugeBuilder barBackgroundColor(final Color BAR_BACKGROUND_COLOR) {
        properties.put("barBackgroundColor", new SimpleObjectProperty<>(BAR_BACKGROUND_COLOR));
        return this;    
    }

    public final OneEightyGaugeBuilder barColor(final Color BAR_COLOR) {
        properties.put("barColor", new SimpleObjectProperty<>(BAR_COLOR));
        return this;
    }
    
    public final OneEightyGaugeBuilder titleColor(final Color TITLE_COLOR) {
        properties.put("titleColor", new SimpleObjectProperty<>(TITLE_COLOR));
        return this;
    }

    public final OneEightyGaugeBuilder valueColor(final Color VALUE_COLOR) {
        properties.put("valueColor", new SimpleObjectProperty<>(VALUE_COLOR));
        return this;
    }

    public final OneEightyGaugeBuilder unitColor(final Color UNIT_COLOR) {
        properties.put("unitColor", new SimpleObjectProperty<>(UNIT_COLOR));
        return this;
    }

    public final OneEightyGaugeBuilder minTextColor(final Color MIN_TEXT_COLOR) {
        properties.put("minTextColor", new SimpleObjectProperty<>(MIN_TEXT_COLOR));
        return this;
    }

    public final OneEightyGaugeBuilder maxTextColor(final Color MAX_TEXT_COLOR) {
        properties.put("maxTextColor", new SimpleObjectProperty<>(MAX_TEXT_COLOR));
        return this;
    }

    public final B dynamicBarColor(final boolean DYNAMIC_BAR_COLOR) {
        properties.put("dynamicBarColor", new SimpleBooleanProperty(DYNAMIC_BAR_COLOR));
        return (B) this;
    }

    public final B stops(final Stop... STOPS) {
        properties.put("stopsArray", new SimpleObjectProperty<>(STOPS));
        return (B) this;
    }

    public final B stops(final List<Stop> STOPS) {
        properties.put("stopsList", new SimpleObjectProperty<>(STOPS));
        return (B) this;
    }

    public final B gradientLookup(final GradientLookup GRADIENT_LOOKUP) {
        properties.put("gradientLookup", new SimpleObjectProperty<>(GRADIENT_LOOKUP));
        return (B) this;
    }

    public final B prefSize(final double WIDTH, final double HEIGHT) {
        properties.put("prefSize", new SimpleObjectProperty<>(new Dimension2D(WIDTH, HEIGHT)));
        return (B)this;
    }
    public final B minSize(final double WIDTH, final double HEIGHT) {
        properties.put("minSize", new SimpleObjectProperty<>(new Dimension2D(WIDTH, HEIGHT)));
        return (B)this;
    }
    public final B maxSize(final double WIDTH, final double HEIGHT) {
        properties.put("maxSize", new SimpleObjectProperty<>(new Dimension2D(WIDTH, HEIGHT)));
        return (B)this;
    }

    public final B prefWidth(final double PREF_WIDTH) {
        properties.put("prefWidth", new SimpleDoubleProperty(PREF_WIDTH));
        return (B)this;
    }
    public final B prefHeight(final double PREF_HEIGHT) {
        properties.put("prefHeight", new SimpleDoubleProperty(PREF_HEIGHT));
        return (B)this;
    }

    public final B minWidth(final double MIN_WIDTH) {
        properties.put("minWidth", new SimpleDoubleProperty(MIN_WIDTH));
        return (B)this;
    }
    public final B minHeight(final double MIN_HEIGHT) {
        properties.put("minHeight", new SimpleDoubleProperty(MIN_HEIGHT));
        return (B)this;
    }

    public final B maxWidth(final double MAX_WIDTH) {
        properties.put("maxWidth", new SimpleDoubleProperty(MAX_WIDTH));
        return (B)this;
    }
    public final B maxHeight(final double MAX_HEIGHT) {
        properties.put("maxHeight", new SimpleDoubleProperty(MAX_HEIGHT));
        return (B)this;
    }

    public final B scaleX(final double SCALE_X) {
        properties.put("scaleX", new SimpleDoubleProperty(SCALE_X));
        return (B)this;
    }
    public final B scaleY(final double SCALE_Y) {
        properties.put("scaleY", new SimpleDoubleProperty(SCALE_Y));
        return (B)this;
    }

    public final B layoutX(final double LAYOUT_X) {
        properties.put("layoutX", new SimpleDoubleProperty(LAYOUT_X));
        return (B)this;
    }
    public final B layoutY(final double LAYOUT_Y) {
        properties.put("layoutY", new SimpleDoubleProperty(LAYOUT_Y));
        return (B)this;
    }

    public final B translateX(final double TRANSLATE_X) {
        properties.put("translateX", new SimpleDoubleProperty(TRANSLATE_X));
        return (B)this;
    }
    public final B translateY(final double TRANSLATE_Y) {
        properties.put("translateY", new SimpleDoubleProperty(TRANSLATE_Y));
        return (B)this;
    }

    public final OneEightyGauge build() {
        final OneEightyGauge CONTROL = new OneEightyGauge();
        
        for (String key : properties.keySet()) {
            if ("prefSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                CONTROL.setPrefSize(dim.getWidth(), dim.getHeight());
            } else if("minSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                CONTROL.setPrefSize(dim.getWidth(), dim.getHeight());
            } else if("maxSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                CONTROL.setPrefSize(dim.getWidth(), dim.getHeight());
            } else if("prefWidth".equals(key)) {
                CONTROL.setPrefWidth(((DoubleProperty) properties.get(key)).get());
            } else if("prefHeight".equals(key)) {
                CONTROL.setPrefHeight(((DoubleProperty) properties.get(key)).get());
            } else if("minWidth".equals(key)) {
                CONTROL.setMinWidth(((DoubleProperty) properties.get(key)).get());
            } else if("minHeight".equals(key)) {
                CONTROL.setMinHeight(((DoubleProperty) properties.get(key)).get());
            } else if("maxWidth".equals(key)) {
                CONTROL.setMaxWidth(((DoubleProperty) properties.get(key)).get());
            } else if("maxHeight".equals(key)) {
                CONTROL.setMaxHeight(((DoubleProperty) properties.get(key)).get());
            } else if("scaleX".equals(key)) {
                CONTROL.setScaleX(((DoubleProperty) properties.get(key)).get());
            } else if("scaleY".equals(key)) {
                CONTROL.setScaleY(((DoubleProperty) properties.get(key)).get());
            } else if ("layoutX".equals(key)) {
                CONTROL.setLayoutX(((DoubleProperty) properties.get(key)).get());
            } else if ("layoutY".equals(key)) {
                CONTROL.setLayoutY(((DoubleProperty) properties.get(key)).get());
            } else if ("translateX".equals(key)) {
                CONTROL.setTranslateX(((DoubleProperty) properties.get(key)).get());
            } else if ("translateY".equals(key)) {
                CONTROL.setTranslateY(((DoubleProperty) properties.get(key)).get());
            } else if("styleClass".equals(key)) {
                CONTROL.getStyleClass().setAll("one-eighty-gauge");
                CONTROL.getStyleClass().addAll(((ObjectProperty<String[]>) properties.get(key)).get());
            } else if("maxValue".equals(key)) {
                CONTROL.setMaxValue(((DoubleProperty) properties.get(key)).get());
            } else if("minValue".equals(key)) {
                CONTROL.setMinValue(((DoubleProperty) properties.get(key)).get());
            } else if("value".equals(key)) {
                CONTROL.setValue(((DoubleProperty) properties.get(key)).get());
            } else if("decimals".equals(key)) {
                CONTROL.setDecimals(((IntegerProperty) properties.get(key)).get());
            } else if ("title".equals(key)) {
                CONTROL.setTitle(((StringProperty) properties.get(key)).get());
            } else if("unit".equals(key)) {
                CONTROL.setUnit(((StringProperty) properties.get(key)).get());
            } else if ("shadowsEnabled".equals(key)) {
                CONTROL.setShadowsEnabled(((BooleanProperty) properties.get(key)).get());
            } else if("animated".equals(key)) {
                CONTROL.setAnimated(((BooleanProperty) properties.get(key)).get());
            } else if("animationDuration".equals(key)) {
                CONTROL.setAnimationDuration(((DoubleProperty) properties.get(key)).get());
            } else if("barBackgroundColor".equals(key)) {
                CONTROL.setBarBackgroundColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("barColor".equals(key)) {
                CONTROL.setBarColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("titleColor".equals(key)) {
                CONTROL.setTitleColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("valueColor".equals(key)) {
                CONTROL.setValueColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("unitColor".equals(key)) {
                CONTROL.setUnitColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("minTextColor".equals(key)) {
                CONTROL.setMinTextColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("maxTextColor".equals(key)) {
                CONTROL.setMaxTextColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("dynamicBarColor".equals(key)) {
                CONTROL.setDynamicBarColor(((BooleanProperty) properties.get(key)).get());
            } else if ("stopsArray".equals(key)) {
                CONTROL.setStops(((ObjectProperty<Stop[]>) properties.get(key)).get());
            } else if ("stopsList".equals(key)) {
                CONTROL.setStops(((ObjectProperty<List<Stop>>) properties.get(key)).get());
            } else if ("gradientLookup".equals(key)) {
                CONTROL.setGradientLookup(((ObjectProperty<GradientLookup>) properties.get(key)).get());
            }
        }
        return CONTROL;
    }
}
