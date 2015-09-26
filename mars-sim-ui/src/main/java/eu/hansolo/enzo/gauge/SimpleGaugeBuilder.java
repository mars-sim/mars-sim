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

import eu.hansolo.enzo.common.Section;
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
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.List;


/**
 * Created by
 * User: hansolo
 * Date: 22.07.13
 * Time: 07:47
 */
public class SimpleGaugeBuilder<B extends SimpleGaugeBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    protected SimpleGaugeBuilder() {}


    // ******************** Methods *******************************************
    public static final SimpleGaugeBuilder create() {
        return new SimpleGaugeBuilder();
    }

    public final SimpleGaugeBuilder value(final double VALUE) {
        properties.put("value", new SimpleDoubleProperty(VALUE));
        return this;
    }

    public final SimpleGaugeBuilder minValue(final double MIN_VALUE) {
        properties.put("minValue", new SimpleDoubleProperty(MIN_VALUE));
        return this;
    }

    public final SimpleGaugeBuilder maxValue(final double MAX_VALUE) {
        properties.put("maxValue", new SimpleDoubleProperty(MAX_VALUE));
        return this;
    }

    public final SimpleGaugeBuilder threshold(final double THRESHOLD) {
        properties.put("threshold", new SimpleDoubleProperty(THRESHOLD));
        return this;
    }

    public final SimpleGaugeBuilder sections(final Section... SECTIONS) {
        properties.put("sectionsArray", new SimpleObjectProperty<>(SECTIONS));
        return this;
    }

    public final SimpleGaugeBuilder sections(final List<Section> SECTIONS) {
        properties.put("sectionsList", new SimpleObjectProperty<>(SECTIONS));
        return this;
    }

    public final SimpleGaugeBuilder decimals(final int DECIMALS) {
        properties.put("decimals", new SimpleIntegerProperty(DECIMALS));
        return this;
    }

    public final SimpleGaugeBuilder title(final String TITLE) {
        properties.put("title", new SimpleStringProperty(TITLE));
        return this;
    }

    public final SimpleGaugeBuilder unit(final String UNIT) {
        properties.put("unit", new SimpleStringProperty(UNIT));
        return this;
    }

    public final SimpleGaugeBuilder valueVisible(final boolean VALUE_VISIBLE) {
        properties.put("valueVisible", new SimpleBooleanProperty(VALUE_VISIBLE));
        return this;
    }
    
    public final SimpleGaugeBuilder titleVisible(final boolean TITLE_VISIBLE) {
        properties.put("titleVisible", new SimpleBooleanProperty(TITLE_VISIBLE));
        return this;
    }
    
    public final SimpleGaugeBuilder customFontEnabled(final boolean CUSTOM_FONT_ENABLED) {
        properties.put("customFontEnabled", new SimpleBooleanProperty(CUSTOM_FONT_ENABLED));
        return this;
    }
    
    public final SimpleGaugeBuilder customFont(final Font CUSTOM_FONT) {
        properties.put("customFont", new SimpleObjectProperty<Font>(CUSTOM_FONT));
        return this;
    }
    
    public final SimpleGaugeBuilder sectionTextVisible(final boolean SECTION_TEXT_VISIBLE) {
        properties.put("sectionTextVisible", new SimpleBooleanProperty(SECTION_TEXT_VISIBLE));
        return this;
    }

    public final SimpleGaugeBuilder sectionIconVisible(final boolean SECTION_ICON_VISIBLE) {
        properties.put("sectionIconVisible", new SimpleBooleanProperty(SECTION_ICON_VISIBLE));
        return this;
    }

    public final SimpleGaugeBuilder measuredRangeVisible(final boolean MEASURED_RANGE_VISIBLE) {
        properties.put("measuredRangeVisible", new SimpleBooleanProperty(MEASURED_RANGE_VISIBLE));
        return this;
    }
    
    public final SimpleGaugeBuilder valueTextColor(final Color VALUE_TEXT_COLOR) {
        properties.put("valueTextColor", new SimpleObjectProperty<Color>(VALUE_TEXT_COLOR));
        return this;
    }

    public final SimpleGaugeBuilder titleTextColor(final Color TITLE_TEXT_COLOR) {
        properties.put("titleTextColor", new SimpleObjectProperty<Color>(TITLE_TEXT_COLOR));
        return this;
    }

    public final SimpleGaugeBuilder sectionTextColor(final Color SECTION_TEXT_COLOR) {
        properties.put("sectionTextColor", new SimpleObjectProperty<Color>(SECTION_TEXT_COLOR));
        return this;
    }

    public final SimpleGaugeBuilder animated(final boolean ANIMATED) {
        properties.put("animated", new SimpleBooleanProperty(ANIMATED));
        return this;
    }

    public final SimpleGaugeBuilder animationDuration(final double ANIMATION_DURATION) {
        properties.put("animationDuration", new SimpleDoubleProperty(ANIMATION_DURATION));
        return this;
    }

    public final SimpleGaugeBuilder startAngle(final double START_ANGLE) {
        properties.put("startAngle", new SimpleDoubleProperty(START_ANGLE));
        return this;
    }

    public final SimpleGaugeBuilder angleRange(final double ANGLE_RANGE) {
        properties.put("angleRange", new SimpleDoubleProperty(ANGLE_RANGE));
        return this;
    }

    public final SimpleGaugeBuilder autoScale(final boolean AUTO_SCALE) {
        properties.put("autoScale", new SimpleBooleanProperty(AUTO_SCALE));
        return this;
    }

    public final SimpleGaugeBuilder needleColor(final Color NEEDLE_COLOR) {
        properties.put("needleColor", new SimpleObjectProperty<>(NEEDLE_COLOR));
        return this;
    }

    public final SimpleGaugeBuilder borderColor(final Color BORDER_COLOR) {
        properties.put("borderColor", new SimpleObjectProperty<>(BORDER_COLOR));
        return this;
    }

    public final SimpleGaugeBuilder sectionFill0(final Color SECTION_0_FILL) {
        properties.put("sectionFill0", new SimpleObjectProperty<>(SECTION_0_FILL));
        return this;
    }

    public final SimpleGaugeBuilder sectionFill1(final Color SECTION_1_FILL) {
        properties.put("sectionFill1", new SimpleObjectProperty<>(SECTION_1_FILL));
        return this;
    }

    public final SimpleGaugeBuilder sectionFill2(final Color SECTION_2_FILL) {
        properties.put("sectionFill2", new SimpleObjectProperty<>(SECTION_2_FILL));
        return this;
    }

    public final SimpleGaugeBuilder sectionFill3(final Color SECTION_3_FILL) {
        properties.put("sectionFill3", new SimpleObjectProperty<>(SECTION_3_FILL));
        return this;
    }

    public final SimpleGaugeBuilder sectionFill4(final Color SECTION_4_FILL) {
        properties.put("sectionFill4", new SimpleObjectProperty<>(SECTION_4_FILL));
        return this;
    }

    public final SimpleGaugeBuilder sectionFill5(final Color SECTION_5_FILL) {
        properties.put("sectionFill5", new SimpleObjectProperty<>(SECTION_5_FILL));
        return this;
    }

    public final SimpleGaugeBuilder sectionFill6(final Color SECTION_6_FILL) {
        properties.put("sectionFill6", new SimpleObjectProperty<>(SECTION_6_FILL));
        return this;
    }

    public final SimpleGaugeBuilder sectionFill7(final Color SECTION_7_FILL) {
        properties.put("sectionFill7", new SimpleObjectProperty<>(SECTION_7_FILL));
        return this;
    }

    public final SimpleGaugeBuilder sectionFill8(final Color SECTION_8_FILL) {
        properties.put("sectionFill8", new SimpleObjectProperty<>(SECTION_8_FILL));
        return this;
    }

    public final SimpleGaugeBuilder sectionFill9(final Color SECTION_9_FILL) {
        properties.put("sectionFill9", new SimpleObjectProperty<>(SECTION_9_FILL));
        return this;
    }

    public final SimpleGaugeBuilder rangeFill(final Color RANGE_FILL) {
        properties.put("rangeFill", new SimpleObjectProperty<Color>(RANGE_FILL));
        return this;
    }
    
    public final SimpleGaugeBuilder styleClass(final String... STYLES) {
        properties.put("styleClass", new SimpleObjectProperty<>(STYLES));
        return this;
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

    public final SimpleGauge build() {
        final SimpleGauge CONTROL = new SimpleGauge();
        
        // Make sure that sections will be added first
        if (properties.keySet().contains("sectionsArray")) {
            CONTROL.setSections(((ObjectProperty<Section[]>) properties.get("sectionsArray")).get());
        } else if(properties.keySet().contains("sectionsList")) {
            CONTROL.setSections(((ObjectProperty<List<Section>>) properties.get("sectionsList")).get());
        }
        
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
                CONTROL.getStyleClass().setAll("simple-gauge");
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
            } else if("sectionTextVisible".equals(key)) {
                CONTROL.setSectionTextVisible(((BooleanProperty) properties.get(key)).get());
            } else if("sectionIconVisible".equals(key)) {
                CONTROL.setSectionIconVisible(((BooleanProperty) properties.get(key)).get());
            } else if("animated".equals(key)) {
                CONTROL.setAnimated(((BooleanProperty) properties.get(key)).get());
            } else if("animationDuration".equals(key)) {
                CONTROL.setAnimationDuration(((DoubleProperty) properties.get(key)).get());
            } else if("startAngle".equals(key)) {
                CONTROL.setStartAngle(((DoubleProperty) properties.get(key)).get());
            } else if("angleRange".equals(key)) {
                CONTROL.setAngleRange(((DoubleProperty) properties.get(key)).get());
            } else if ("autoScale".equals(key)) {
                CONTROL.setAutoScale(((BooleanProperty) properties.get(key)).get());
            } else if("needleColor".equals(key)) {
                CONTROL.setNeedleColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("borderColor".equals(key)) {
                CONTROL.setBorderColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("majorTickSpace".equals(key)) {
                CONTROL.setMajorTickSpace(((DoubleProperty) properties.get(key)).get());
            } else if("minorTickSpace".equals(key)) {
                CONTROL.setMinorTickSpace(((DoubleProperty) properties.get(key)).get());
            } else if("sectionFill0".equals(key)) {
                CONTROL.setSectionFill0(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("sectionFill1".equals(key)) {
                CONTROL.setSectionFill1(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("sectionFill2".equals(key)) {
                CONTROL.setSectionFill2(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("sectionFill3".equals(key)) {
                CONTROL.setSectionFill3(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("sectionFill4".equals(key)) {
                CONTROL.setSectionFill4(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("sectionFill5".equals(key)) {
                CONTROL.setSectionFill5(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("sectionFill6".equals(key)) {
                CONTROL.setSectionFill6(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("sectionFill7".equals(key)) {
                CONTROL.setSectionFill7(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("sectionFill8".equals(key)) {
                CONTROL.setSectionFill8(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("sectionFill9".equals(key)) {
                CONTROL.setSectionFill9(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("valueTextColor".equals(key)) {
                CONTROL.setValueTextColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("titleTextColor".equals(key)) {
                CONTROL.setTitleTextColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("sectionTextColor".equals(key)) {
                CONTROL.setSectionTextColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("measuredRangeVisible".equals(key)) {
                CONTROL.setMeasuredRangeVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("rangeFill".equals(key)) {
                CONTROL.setRangeFill(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("valueVisible".equals(key)) {
                CONTROL.setValueVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("titleVisible".equals(key)) {
                CONTROL.setTitleVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("customFontEnabled".equals(key)) {
                CONTROL.setCustomFontEnabled(((BooleanProperty) properties.get(key)).get());
            } else if ("customFont".equals(key)) {
                CONTROL.setCustomFont(((ObjectProperty<Font>) properties.get(key)).get());
            }
        }
        return CONTROL;
    }
}
