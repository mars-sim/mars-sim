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

package eu.hansolo.enzo.radialmenu;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.util.Builder;

import java.util.HashMap;


/**
 * Created with IntelliJ IDEA.
 * User: hansolo
 * Date: 24.09.12
 * Time: 15:56
 * To change this template use File | Settings | File Templates.
 */
public class RadialMenuOptionsBuilder implements Builder<RadialMenuOptions> {
    private HashMap<String, Property> properties = new HashMap<String, Property>();


    // ******************** Constructors **************************************
    protected RadialMenuOptionsBuilder() {}


    // ******************** Methods *******************************************
    public static final RadialMenuOptionsBuilder create() {
        return new RadialMenuOptionsBuilder();
    }

    public final RadialMenuOptionsBuilder degrees(final double DEGREES) {
        properties.put("degrees", new SimpleDoubleProperty(DEGREES));
        return this;
    }

    public final RadialMenuOptionsBuilder offset(final double OFFSET) {
        properties.put("offset", new SimpleDoubleProperty(OFFSET));
        return this;
    }

    public final RadialMenuOptionsBuilder radius(final double RADIUS) {
        properties.put("radius", new SimpleDoubleProperty(RADIUS));
        return this;
    }

    public final RadialMenuOptionsBuilder buttonSize(final double BUTTON_SIZE) {
        properties.put("buttonSize", new SimpleDoubleProperty(BUTTON_SIZE));
        return this;
    }

    public final RadialMenuOptionsBuilder buttonFillColor(final Color BUTTON_INNER_COLOR) {
        properties.put("buttonFillColor", new SimpleObjectProperty<Color>(BUTTON_INNER_COLOR));
        return this;
    }

    public final RadialMenuOptionsBuilder buttonStrokeColor(final Color BUTTON_FRAME_COLOR) {
        properties.put("buttonStrokeColor", new SimpleObjectProperty<Color>(BUTTON_FRAME_COLOR));
        return this;
    }

    public final RadialMenuOptionsBuilder buttonForegroundColor(final Color BUTTON_FOREGROUND_COLOR) {
        properties.put("buttonForegroundColor", new SimpleObjectProperty<Color>(BUTTON_FOREGROUND_COLOR));
        return this;
    }

    public final RadialMenuOptionsBuilder buttonAlpha(final double BUTTON_ALPHA) {
        properties.put("buttonAlpha", new SimpleDoubleProperty(BUTTON_ALPHA));
        return this;
    }

    public final RadialMenuOptionsBuilder buttonHideOnSelect(final boolean BUTTON_HIDE_ON_SELECT) {
        properties.put("buttonHideOnSelect", new SimpleBooleanProperty(BUTTON_HIDE_ON_SELECT));
        return this;
    }

    public final RadialMenuOptionsBuilder buttonHideOnClose(final boolean BUTTON_HIDE_ON_CLOSE) {
        properties.put("buttonHideOnClose", new SimpleBooleanProperty(BUTTON_HIDE_ON_CLOSE));
        return this;
    }

    public final RadialMenuOptionsBuilder tooltipsEnabled(final boolean TOOLTIPS_ENABLED) {
        properties.put("tooltipsEnabled", new SimpleBooleanProperty(TOOLTIPS_ENABLED));
        return this;
    }

    public final RadialMenuOptionsBuilder buttonVisible(final boolean BUTTON_VISIBLE) {
        properties.put("buttonVisible", new SimpleBooleanProperty(BUTTON_VISIBLE));
        return this;
    }

    public final RadialMenuOptionsBuilder simpleMode(final boolean SIMPLE_MODE) {
        properties.put("simpleMode", new SimpleBooleanProperty(SIMPLE_MODE));
        return this;
    }

    public final RadialMenuOptionsBuilder strokeVisible(final boolean STROKE_VISIBLE) {
        properties.put("strokeVisible", new SimpleBooleanProperty(STROKE_VISIBLE));
        return this;
    }

    @Override public final RadialMenuOptions build() {
        final RadialMenuOptions CONTROL = new RadialMenuOptions();

        properties.forEach((key, property) -> {
            if ("degrees".equals(key)) {
                CONTROL.setDegrees(((DoubleProperty) property).get());
            } else if("offset".equals(key)) {
                CONTROL.setOffset(((DoubleProperty) property).get());
            } else if ("radius".equals(key)) {
                CONTROL.setRadius(((DoubleProperty) property).get());
            } else if ("buttonSize".equals(key)) {
                CONTROL.setButtonSize(((DoubleProperty) property).get());
            } else if ("buttonFillColor".equals(key)) {
                CONTROL.setButtonFillColor(((ObjectProperty<Color>) property).get());
            } else if ("buttonStrokeColor".equals(key)) {
                CONTROL.setButtonStrokeColor(((ObjectProperty<Color>) property).get());
            } else if ("buttonForegroundColor".equals(key)) {
                CONTROL.setButtonForegroundColor(((ObjectProperty<Color>) property).get());
            } else if ("buttonAlpha".equals(key)) {
                CONTROL.setButtonAlpha(((DoubleProperty) property).get());
            } else if ("buttonHideOnSelect".equals(key)) {
                CONTROL.setButtonHideOnSelect(((BooleanProperty) property).get());
            } else if ("buttonHideOnClose".equals(key)) {
                CONTROL.setButtonHideOnClose(((BooleanProperty) property).get());
            } else if ("tooltipsEnabled".equals(key)) {
                CONTROL.setTooltipsEnabled(((BooleanProperty) property).get());
            } else if ("buttonVisible".equals(key)) {
                CONTROL.setButtonVisible(((BooleanProperty) property).get());
            } else if ("simpleMode".equals(key)) {
                CONTROL.setSimpleMode(((BooleanProperty) property).get());
            } else if ("strokeVisible".equals(key)) {
                CONTROL.setStrokeVisible(((BooleanProperty) property).get());
            }
        });

        return CONTROL;
    }
}

