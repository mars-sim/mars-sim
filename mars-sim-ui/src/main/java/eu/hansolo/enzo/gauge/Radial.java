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
import eu.hansolo.enzo.common.Section;
import eu.hansolo.enzo.gauge.skin.RadialSkin;
import javafx.animation.AnimationTimer;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.*;
import javafx.scene.paint.Color;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Paint;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.util.*;


public class Radial extends Control {
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

    private static final PseudoClass LED_ON_PSEUDO_CLASS = PseudoClass.getPseudoClass("led-on");
    private static final long        LED_BLINK_INTERVAL  = 500_000_000l;

    // Default section colors
    private static final Color       DEFAULT_SECTION_FILL_0      = Color.rgb(0, 0, 178, 0.5);
    private static final Color       DEFAULT_SECTION_FILL_1      = Color.rgb(0, 128, 255, 0.5);
    private static final Color       DEFAULT_SECTION_FILL_2      = Color.rgb(  0, 255, 255, 0.5);
    private static final Color       DEFAULT_SECTION_FILL_3      = Color.rgb(  0, 255,  64, 0.5);
    private static final Color       DEFAULT_SECTION_FILL_4      = Color.rgb(128, 255,   0, 0.5);
    private static final Color       DEFAULT_SECTION_FILL_5      = Color.rgb(255, 255,   0, 0.5);
    private static final Color       DEFAULT_SECTION_FILL_6      = Color.rgb(255, 191,   0, 0.5);
    private static final Color       DEFAULT_SECTION_FILL_7      = Color.rgb(255, 128,   0, 0.5);
    private static final Color       DEFAULT_SECTION_FILL_8      = Color.rgb(255,  64,   0, 0.5);
    private static final Color       DEFAULT_SECTION_FILL_9      = Color.rgb(255,   0,   0, 0.5);
    private static final Color       DEFAULT_HISTOGRAM_FILL      = Color.rgb(  0, 200,   0, 0.3);

    // Default area colors
    private static final Color       DEFAULT_AREA_FILL_0         = Color.rgb(0, 0, 178, 0.5);
    private static final Color       DEFAULT_AREA_FILL_1         = Color.rgb(0, 128, 255, 0.5);
    private static final Color       DEFAULT_AREA_FILL_2         = Color.rgb(  0, 255, 255, 0.5);
    private static final Color       DEFAULT_AREA_FILL_3         = Color.rgb(  0, 255,  64, 0.5);
    private static final Color       DEFAULT_AREA_FILL_4         = Color.rgb(128, 255,   0, 0.5);
    private static final Color       DEFAULT_AREA_FILL_5         = Color.rgb(255, 255,   0, 0.5);
    private static final Color       DEFAULT_AREA_FILL_6         = Color.rgb(255, 191,   0, 0.5);
    private static final Color       DEFAULT_AREA_FILL_7         = Color.rgb(255, 128,   0, 0.5);
    private static final Color       DEFAULT_AREA_FILL_8         = Color.rgb(255,  64,   0, 0.5);
    private static final Color       DEFAULT_AREA_FILL_9         = Color.rgb(255,   0,   0, 0.5);

    private DoubleProperty                       value;
    private DoubleProperty                       minValue;
    private double                               exactMinValue;
    private DoubleProperty                       maxValue;
    private double                               exactMaxValue;
    private IntegerProperty                      decimals;
    private StringProperty                       title;
    private StringProperty                       unit;
    private BooleanProperty                      animated;
    private double                               animationDuration;
    private DoubleProperty                       startAngle;
    private DoubleProperty                       angleRange;
    private BooleanProperty                      autoScale;
    private ObjectProperty<Color>                needleColor;
    private BooleanProperty                      ledOn;
    private BooleanProperty                      blinking;
    private ObjectProperty<Color>                ledColor;
    private BooleanProperty                      ledVisible;
    private BooleanProperty                      lcdVisible;
    private ObjectProperty<TickLabelOrientation> tickLabelOrientation;
    private ObjectProperty<NumberFormat>         numberFormat;
    private DoubleProperty                       majorTickSpace;
    private DoubleProperty                       minorTickSpace;
    private DoubleProperty                       frameSizeFactor;
    private ObservableList<Section>              sections;
    private ObservableList<Section>              areas;
    private BooleanProperty                      sectionsVisible;
    private BooleanProperty                      areasVisible;
    private ObjectProperty<Color>                sectionFill0;
    private ObjectProperty<Color>                sectionFill1;
    private ObjectProperty<Color>                sectionFill2;
    private ObjectProperty<Color>                sectionFill3;
    private ObjectProperty<Color>                sectionFill4;
    private ObjectProperty<Color>                sectionFill5;
    private ObjectProperty<Color>                sectionFill6;
    private ObjectProperty<Color>                sectionFill7;
    private ObjectProperty<Color>                sectionFill8;
    private ObjectProperty<Color>                sectionFill9;
    private ObjectProperty<Color>                areaFill0;
    private ObjectProperty<Color>                areaFill1;
    private ObjectProperty<Color>                areaFill2;
    private ObjectProperty<Color>                areaFill3;
    private ObjectProperty<Color>                areaFill4;
    private ObjectProperty<Color>                areaFill5;
    private ObjectProperty<Color>                areaFill6;
    private ObjectProperty<Color>                areaFill7;
    private ObjectProperty<Color>                areaFill8;
    private ObjectProperty<Color>                areaFill9;

    // CSS styleable properties
    private ObjectProperty<Paint> tickMarkFill;
    private ObjectProperty<Paint> tickLabelFill;

    private long           lastTimerCall;
    private AnimationTimer timer;


    // ******************** Constructors **************************************
    public Radial() {
        getStyleClass().add("radial");
        value             = new DoublePropertyBase(0) {
            @Override public void set(final double VALUE) { super.set(clamp(minValue.get(), maxValue.get(), VALUE)); }
            @Override public Object getBean() { return this; }
            @Override public String getName() { return "value"; }
        };
        minValue                 = new DoublePropertyBase(0) {
            @Override public void set(final double VALUE) {
                super.set(clamp(-Double.MAX_VALUE, maxValue.get(), VALUE));
                if (value.get() < VALUE) value.set(VALUE);
            }
            @Override public Object getBean() { return Radial.this; }
            @Override public String getName() { return "minValue"; }
        };
        maxValue                 = new DoublePropertyBase(100) {
            @Override public void set(final double VALUE) {
                super.set(clamp(minValue.get(), Double.MAX_VALUE, VALUE));
                if (value.get() > VALUE) value.set(VALUE);
            }
            @Override public Object getBean() { return Radial.this; }
            @Override public String getName() { return "maxValue"; }
        };
        sections          = FXCollections.observableArrayList();
        areas             = FXCollections.observableArrayList();
        lastTimerCall     = System.nanoTime();
        animationDuration = 800;
        timer = new AnimationTimer() {
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

    public double getStartAngle() { return null == startAngle ? 330 : startAngle.get(); }
    public final void setStartAngle(final double START_ANGLE) { startAngleProperty().set(START_ANGLE); }
    public final DoubleProperty startAngleProperty() {
        if (null == startAngle) {
            startAngle = new DoublePropertyBase(320) {
                @Override public void set(final double START_ANGLE) { super.set(clamp(0d, 360d, get())); }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "startAngle"; }
            };
        }
        return startAngle;
    }

    public final double getAnimationDuration() { return animationDuration; }
    public final void setAnimationDuration(final double ANIMATION_DURATION) { animationDuration = clamp(20, 5000, ANIMATION_DURATION); }

    public final double getAngleRange() { return null == angleRange ? 300 : angleRange.get(); }
    public final void setAngleRange(final double ANGLE_RANGE) { angleRangeProperty().set(ANGLE_RANGE); }
    public final DoubleProperty angleRangeProperty() {
        if (null == angleRange) {
            angleRange = new DoublePropertyBase(300) {
                @Override public void set(final double ANGLE_RANGE) { super.set(clamp(0d, 360d, get())); }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "angleRange"; }
            };
        }
        return angleRange;
    }

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

    public final Color getNeedleColor() {
        return null == needleColor ? Color.RED : needleColor.get();
    }
    public final void setNeedleColor(final Color NEEDLE_COLOR) { needleColorProperty().set(NEEDLE_COLOR); }
    public final ObjectProperty<Color> needleColorProperty() {
        if (null == needleColor) { needleColor = new SimpleObjectProperty<>(this, "needleColor", Color.RED); }
        return needleColor;
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
                    return Radial.this;
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

    public final boolean isLedVisible() { return null == ledVisible ? true : ledVisible.get(); }
    public final void setLedVisible(final boolean LED_VISIBLE) { ledVisibleProperty().set(LED_VISIBLE); }
    public final BooleanProperty ledVisibleProperty() {
        if (null == ledVisible) {
            ledVisible = new SimpleBooleanProperty(this, "ledVisible", true);
        }
        return ledVisible;
    }

    public final boolean isLcdVisible() { return null == lcdVisible ? true : lcdVisible.get(); }
    public final void setLcdVisible(final boolean LCD_VISIBLE) { lcdVisibleProperty().set(LCD_VISIBLE); }
    public final BooleanProperty lcdVisibleProperty() {
        if (null == lcdVisible) {
            lcdVisible = new SimpleBooleanProperty(this, "lcdVisible", true);
        }
        return lcdVisible;
    }

    public final TickLabelOrientation getTickLabelOrientation() {
        return null == tickLabelOrientation ? TickLabelOrientation.HORIZONTAL : tickLabelOrientation.get();
    }
    public final void setTickLabelOrientation(final TickLabelOrientation TICK_LABEL_ORIENTATION) {
        tickLabelOrientationProperty().set(TICK_LABEL_ORIENTATION);
    }
    public final ObjectProperty<TickLabelOrientation> tickLabelOrientationProperty() {
        if (null == tickLabelOrientation) {
            tickLabelOrientation = new SimpleObjectProperty<>(this, "tickLabelOrientation", TickLabelOrientation.HORIZONTAL);
        }
        return tickLabelOrientation;
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

    public double getFrameSizeFactor() { return null == frameSizeFactor ? 0.085 : frameSizeFactor.get(); }
    public void setFrameSizeFactor(final double FRAME_SIZE_FACTOR) { frameSizeFactorProperty().set(FRAME_SIZE_FACTOR); }
    public DoubleProperty frameSizeFactorProperty() {
        if (null == frameSizeFactor) {
            frameSizeFactor = new SimpleDoubleProperty(this, "frameSizeFactor", 0.085);
        }
        return frameSizeFactor;
    }

    public final ObservableList<Section> getSections() {
        return sections;
    }
    public final void setSections(final List<Section> SECTIONS) {
        sections.setAll(SECTIONS);
    }
    public final void setSections(final Section... SECTIONS) {
        setSections(Arrays.asList(SECTIONS));
    }
    public final void addSection(final Section SECTION) {
        if (!sections.contains(SECTION)) sections.add(SECTION);
    }
    public final void removeSection(final Section SECTION) {
        if (sections.contains(SECTION)) sections.remove(SECTION);
    }

    public final ObservableList<Section> getAreas() {
        return areas;
    }
    public final void setAreas(final List<Section> AREAS) {
        areas.setAll(AREAS);
    }
    public final void setAreas(final Section... AREAS) {
        setAreas(Arrays.asList(AREAS));
    }
    public final void addArea(final Section AREA) {
        if (!areas.contains(AREA)) areas.add(AREA);
    }
    public final void removeArea(final Section AREA) {
        if (areas.contains(AREA)) areas.remove(AREA);
    }

    public final boolean isSectionsVisible() { return null == sectionsVisible ? true : sectionsVisible.get(); }
    public final void setSectionsVisible(final boolean SECTIONS_VISIBLE) { sectionsVisibleProperty().set(SECTIONS_VISIBLE); }
    public final BooleanProperty sectionsVisibleProperty() {
        if (null == sectionsVisible) {
            sectionsVisible = new SimpleBooleanProperty(this, "sectionsVisible", true);
        }
        return sectionsVisible;
    }

    public final boolean isAreasVisible() { return null == areasVisible ? true : areasVisible.get(); }
    public final void setAreasVisible(final boolean AREAS_VISIBLE) { areasVisibleProperty().set(AREAS_VISIBLE); }
    public final BooleanProperty areasVisibleProperty() {
        if (null == areasVisible) {
            areasVisible = new SimpleBooleanProperty(this, "areasVisible", true);
        }
        return areasVisible;
    }


    public final Color getSectionFill0() {
        return null == sectionFill0 ? DEFAULT_SECTION_FILL_0 : sectionFill0.get();
    }
    public final void setSectionFill0(Color value) {
        sectionFill0Property().set(value);
    }
    public final ObjectProperty<Color> sectionFill0Property() {
        if (null == sectionFill0) {
            sectionFill0 = new SimpleObjectProperty<>(this, "sectionFill0", DEFAULT_SECTION_FILL_0);
        }
        return sectionFill0;
    }

    public final Color getSectionFill1() {
        return null == sectionFill1 ? DEFAULT_SECTION_FILL_1 : sectionFill1.get();
    }
    public final void setSectionFill1(Color value) {
        sectionFill1Property().set(value);
    }
    public final ObjectProperty<Color> sectionFill1Property() {
        if (null == sectionFill1) {
            sectionFill1 = new SimpleObjectProperty<>(this, "sectionFill1", DEFAULT_SECTION_FILL_1);
        }
        return sectionFill1;
    }

    public final Color getSectionFill2() {
        return null == sectionFill2 ? DEFAULT_SECTION_FILL_2 : sectionFill2.get();
    }
    public final void setSectionFill2(Color value) {
        sectionFill2Property().set(value);
    }
    public final ObjectProperty<Color> sectionFill2Property() {
        if (null == sectionFill2) {
            sectionFill2 = new SimpleObjectProperty<>(this, "sectionFill2", DEFAULT_SECTION_FILL_2);
        }
        return sectionFill2;
    }

    public final Color getSectionFill3() {
        return null == sectionFill3 ? DEFAULT_SECTION_FILL_3 : sectionFill3.get();
    }
    public final void setSectionFill3(Color value) {
        sectionFill3Property().set(value);
    }
    public final ObjectProperty<Color> sectionFill3Property() {
        if (null == sectionFill3) {
            sectionFill3 = new SimpleObjectProperty<>(this, "sectionFill3", DEFAULT_SECTION_FILL_3);
        }
        return sectionFill3;
    }

    public final Color getSectionFill4() {
        return null == sectionFill4 ? DEFAULT_SECTION_FILL_4 : sectionFill4.get();
    }
    public final void setSectionFill4(Color value) {
        sectionFill4Property().set(value);
    }
    public final ObjectProperty<Color> sectionFill4Property() {
        if (null == sectionFill4) {
            sectionFill4 = new SimpleObjectProperty<>(this, "sectionFill4", DEFAULT_SECTION_FILL_4);
        }
        return sectionFill4;
    }

    public final Color getSectionFill5() {
        return null == sectionFill5 ? DEFAULT_SECTION_FILL_5 : sectionFill5.get();
    }
    public final void setSectionFill5(Color value) {
        sectionFill5Property().set(value);
    }
    public final ObjectProperty<Color> sectionFill5Property() {
        if (null == sectionFill5) {
            sectionFill5 = new SimpleObjectProperty<>(this, "sectionFill5", DEFAULT_SECTION_FILL_5);
        }
        return sectionFill5;
    }

    public final Color getSectionFill6() {
        return null == sectionFill6 ? DEFAULT_SECTION_FILL_6 : sectionFill6.get();
    }
    public final void setSectionFill6(Color value) {
        sectionFill6Property().set(value);
    }
    public final ObjectProperty<Color> sectionFill6Property() {
        if (null == sectionFill6) {
            sectionFill6 = new SimpleObjectProperty<>(this, "sectionFill6", DEFAULT_SECTION_FILL_6);
        }
        return sectionFill6;
    }

    public final Color getSectionFill7() {
        return null == sectionFill7 ? DEFAULT_SECTION_FILL_7 : sectionFill7.get();
    }
    public final void setSectionFill7(Color value) {
        sectionFill7Property().set(value);
    }
    public final ObjectProperty<Color> sectionFill7Property() {
        if (null == sectionFill7) {
            sectionFill7 = new SimpleObjectProperty<>(this, "sectionFill7", DEFAULT_SECTION_FILL_7);
        }
        return sectionFill7;
    }

    public final Color getSectionFill8() {
        return null == sectionFill8 ? DEFAULT_SECTION_FILL_8 : sectionFill8.get();
    }
    public final void setSectionFill8(Color value) {
        sectionFill8Property().set(value);
    }
    public final ObjectProperty<Color> sectionFill8Property() {
        if (null == sectionFill8) {
            sectionFill8 = new SimpleObjectProperty<>(this, "sectionFill8", DEFAULT_SECTION_FILL_8);
        }
        return sectionFill8;
    }

    public final Color getSectionFill9() {
        return null == sectionFill9 ? DEFAULT_SECTION_FILL_9 : sectionFill9.get();
    }
    public final void setSectionFill9(Color value) {
        sectionFill9Property().set(value);
    }
    public final ObjectProperty<Color> sectionFill9Property() {
        if (null == sectionFill9) {
            sectionFill9 = new SimpleObjectProperty<>(this, "sectionFill9", DEFAULT_SECTION_FILL_9);
        }
        return sectionFill9;
    }
    
    public final Color getAreaFill0() {
        return null == areaFill0 ? DEFAULT_AREA_FILL_0 : areaFill0.get();
    }
    public final void setAreaFill0(Color value) {
        areaFill0Property().set(value);
    }
    public final ObjectProperty<Color> areaFill0Property() {
        if (null == areaFill0) {
            areaFill0 = new SimpleObjectProperty<>(this, "areaFill0", DEFAULT_SECTION_FILL_0);
        }
        return areaFill0;
    }

    public final Color getAreaFill1() {
        return null == areaFill1 ? DEFAULT_AREA_FILL_1 : areaFill1.get();
    }
    public final void setAreaFill1(Color value) {
        areaFill1Property().set(value);
    }
    public final ObjectProperty<Color> areaFill1Property() {
        if (null == areaFill1) {
            areaFill1 = new SimpleObjectProperty<>(this, "areaFill1", DEFAULT_SECTION_FILL_1);
        }
        return areaFill1;
    }

    public final Color getAreaFill2() {
        return null == areaFill2 ? DEFAULT_AREA_FILL_2 : areaFill2.get();
    }
    public final void setAreaFill2(Color value) {
        areaFill2Property().set(value);
    }
    public final ObjectProperty<Color> areaFill2Property() {
        if (null == areaFill2) {
            areaFill2 = new SimpleObjectProperty<>(this, "areaFill2", DEFAULT_SECTION_FILL_2);
        }
        return areaFill2;
    }

    public final Color getAreaFill3() {
        return null == areaFill3 ? DEFAULT_AREA_FILL_3 : areaFill3.get();
    }
    public final void setAreaFill3(Color value) {
        areaFill3Property().set(value);
    }
    public final ObjectProperty<Color> areaFill3Property() {
        if (null == areaFill3) {
            areaFill3 = new SimpleObjectProperty<>(this, "areaFill3", DEFAULT_SECTION_FILL_3);
        }
        return areaFill3;
    }

    public final Color getAreaFill4() {
        return null == areaFill4 ? DEFAULT_AREA_FILL_4 : areaFill4.get();
    }
    public final void setAreaFill4(Color value) {
        areaFill4Property().set(value);
    }
    public final ObjectProperty<Color> areaFill4Property() {
        if (null == areaFill4) {
            areaFill4 = new SimpleObjectProperty<>(this, "areaFill4", DEFAULT_SECTION_FILL_4);
        }
        return areaFill4;
    }

    public final Color getAreaFill5() {
        return null == areaFill5 ? DEFAULT_AREA_FILL_5 : areaFill5.get();
    }
    public final void setAreaFill5(Color value) {
        areaFill5Property().set(value);
    }
    public final ObjectProperty<Color> areaFill5Property() {
        if (null == areaFill5) {
            areaFill5 = new SimpleObjectProperty<>(this, "areaFill5", DEFAULT_SECTION_FILL_5);
        }
        return areaFill5;
    }

    public final Color getAreaFill6() {
        return null == areaFill6 ? DEFAULT_AREA_FILL_6 : areaFill6.get();
    }
    public final void setAreaFill6(Color value) {
        areaFill6Property().set(value);
    }
    public final ObjectProperty<Color> areaFill6Property() {
        if (null == areaFill6) {
            areaFill6 = new SimpleObjectProperty<>(this, "areaFill6", DEFAULT_SECTION_FILL_6);
        }
        return areaFill6;
    }

    public final Color getAreaFill7() {
        return null == areaFill7 ? DEFAULT_AREA_FILL_7 : areaFill7.get();
    }
    public final void setAreaFill7(Color value) {
        areaFill7Property().set(value);
    }
    public final ObjectProperty<Color> areaFill7Property() {
        if (null == areaFill7) {
            areaFill7 = new SimpleObjectProperty<>(this, "areaFill7", DEFAULT_SECTION_FILL_7);
        }
        return areaFill7;
    }

    public final Color getAreaFill8() {
        return null == areaFill8 ? DEFAULT_AREA_FILL_8 : areaFill8.get();
    }
    public final void setAreaFill8(Color value) {
        areaFill8Property().set(value);
    }
    public final ObjectProperty<Color> areaFill8Property() {
        if (null == areaFill8) {
            areaFill8 = new SimpleObjectProperty<>(this, "areaFill8", DEFAULT_SECTION_FILL_8);
        }
        return areaFill8;
    }

    public final Color getAreaFill9() {
        return null == areaFill9 ? DEFAULT_AREA_FILL_9 : areaFill9.get();
    }
    public final void setAreaFill9(Color value) {
        areaFill9Property().set(value);
    }
    public final ObjectProperty<Color> areaFill9Property() {
        if (null == areaFill9) {
            areaFill9 = new SimpleObjectProperty<>(this, "areaFill9", DEFAULT_SECTION_FILL_9);
        }
        return areaFill9;
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

                @Override public Object getBean() { return Radial.this; }

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
                @Override public Object getBean() { return Radial.this; }
                @Override public String getName() { return "tickLabelFill"; }
            };
        }
        return tickLabelFill;
    }
    

    // ******************** Style related *************************************
    @Override protected Skin createDefaultSkin() {
        return new RadialSkin(this);
    }

    @Override public String getUserAgentStylesheet() {
        return getClass().getResource("/eu/hansolo/enzo/gauge/radial.css").toExternalForm();
    }

    private static class StyleableProperties {
        private static final CssMetaData<Radial, Paint> TICK_MARK_FILL =
                new CssMetaData<Radial, Paint>("-tick-mark-fill", PaintConverter.getInstance(), Color.BLACK) {

                    @Override public boolean isSettable(Radial gauge) {
                        return null == gauge.tickMarkFill || !gauge.tickMarkFill.isBound();
                    }

                    @Override public StyleableProperty<Paint> getStyleableProperty(Radial gauge) {
                        return (StyleableProperty) gauge.tickMarkFillProperty();
                    }
                };

        private static final CssMetaData<Radial, Paint> TICK_LABEL_FILL =
                new CssMetaData<Radial, Paint>("-tick-label-fill", PaintConverter.getInstance(), Color.BLACK) {

                    @Override public boolean isSettable(Radial gauge) {
                        return null == gauge.tickLabelFill || !gauge.tickLabelFill.isBound();
                    }

                    @Override public StyleableProperty<Paint> getStyleableProperty(Radial gauge) {
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

