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

package eu.hansolo.enzo.charts;

import com.sun.javafx.css.converters.PaintConverter;
import eu.hansolo.enzo.charts.skin.SimpleLineChartSkin;
import eu.hansolo.enzo.common.Section;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Created by
 * User: hansolo
 * Date: 19.08.13
 * Time: 13:26
 */
public class SimpleLineChart extends Control {
    public static final String                STYLE_CLASS_BLUE_TO_RED_5        = "blue-to-red-5";
    public static final String                STYLE_CLASS_GREEN_TO_DARKGREEN_6 = "green-to-darkgreen-6";
    public static final String                STYLE_CLASS_GREEN_TO_RED_6       = "green-to-red-6";
    public static final String                STYLE_CLASS_RED_TO_GREEN_6       = "red-to-green-6";
    public static final String                STYLE_CLASS_BLUE_TO_RED_6        = "blue-to-red-6";
    public static final String                STYLE_CLASS_PURPLE_TO_RED_6      = "purple-to-red-6";
    public static final String                STYLE_CLASS_GREEN_TO_RED_7       = "green-to-red-7";
    public static final String                STYLE_CLASS_RED_TO_GREEN_7       = "red-to-green-7";
    public static final String                STYLE_CLASS_GREEN_TO_RED_10      = "green-to-red-10";
    public static final String                STYLE_CLASS_RED_TO_GREEN_10      = "red-to-green-10";
    public static final String                STYLE_CLASS_PURPLE_TO_CYAN_10    = "purple-to-cyan-10";

    private static final double               PREFERRED_WIDTH        = 200;
    private static final double               PREFERRED_HEIGHT       = 100;
    private static final double               MINIMUM_WIDTH          = 25;
    private static final double               MINIMUM_HEIGHT         = 25;
    private static final double               MAXIMUM_WIDTH          = 1024;
    private static final double               MAXIMUM_HEIGHT         = 1024;

    // CSS styleable properties
    private static final Color                DEFAULT_BULLET_FILL    = Color.web("#5a615f");
    private static final Color                DEFAULT_SERIES_STROKE  = Color.web("#ffffff");
    private static final Color                DEFAULT_SECTION_FILL_0 = Color.web("#f3622d");
    private static final Color                DEFAULT_SECTION_FILL_1 = Color.web("#fba71b");
    private static final Color                DEFAULT_SECTION_FILL_2 = Color.web("#57b757");
    private static final Color                DEFAULT_SECTION_FILL_3 = Color.web("#f5982b");
    private static final Color                DEFAULT_SECTION_FILL_4 = Color.web("#41a9c9");
    private static final Color                DEFAULT_SECTION_FILL_5 = Color.web("#4258c9");
    private static final Color                DEFAULT_SECTION_FILL_6 = Color.web("#9a42c8");
    private static final Color                DEFAULT_SECTION_FILL_7 = Color.web("#c84164");
    private static final Color                DEFAULT_SECTION_FILL_8 = Color.web("#888888");
    private static final Color                DEFAULT_SECTION_FILL_9 = Color.web("#aaaaaa");
    private ObjectProperty<Paint>             bulletFill;
    private ObjectProperty<Paint>             seriesStroke;
    private ObjectProperty<Paint>             sectionFill0;
    private ObjectProperty<Paint>             sectionFill1;
    private ObjectProperty<Paint>             sectionFill2;
    private ObjectProperty<Paint>             sectionFill3;
    private ObjectProperty<Paint>             sectionFill4;
    private ObjectProperty<Paint>             sectionFill5;
    private ObjectProperty<Paint>             sectionFill6;
    private ObjectProperty<Paint>             sectionFill7;
    private ObjectProperty<Paint>             sectionFill8;
    private ObjectProperty<Paint>             sectionFill9;

    private ObservableList<Section>           sections;
    private XYChart.Series<?,?>               series;
    private BooleanProperty                   sectionRangeVisible;
    private StringProperty                    unit;
    private StringProperty                    from;
    private StringProperty                    to;
    private BooleanProperty                   titleVisible;


    // ******************** Constructors **************************************
    public SimpleLineChart() {
        sections    = FXCollections.observableArrayList();
        series      = new XYChart.Series();
        getStyleClass().setAll("canvas-chart");
    }


    // ******************** Methods *******************************************
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

    public final XYChart.Series<?, ?> getSeries() {
        return series;
    }
    public final void setSeries(final XYChart.Series<?, ?> SERIES) {
        series = SERIES;
    }

    public final boolean isSectionRangeVisible() {
        return null == sectionRangeVisible ? false : sectionRangeVisible.get();
    }
    public final void setSectionRangeVisible(final boolean SECTION_RANGE_VISIBLE) {
        sectionRangeVisibleProperty().set(SECTION_RANGE_VISIBLE);
    }
    public final BooleanProperty sectionRangeVisibleProperty() {
        if (null == sectionRangeVisible) {
            sectionRangeVisible = new SimpleBooleanProperty(this, "sectionRangeVisible", false);
        }
        return sectionRangeVisible;
    }

    public final String getUnit() {
        return null == unit ? "" : unit.get();
    }
    public void setUnit(final String UNIT) {
        unitProperty().set(UNIT);
    }
    public final StringProperty unitProperty() {
        if (null == unit) {
            unit = new SimpleStringProperty(this, "unit", "");
        }
        return unit;
    }

    public final String getFrom() {
        return null == from ? "" : from.get();
    }
    public final void setFrom(final String FROM) {
        fromProperty().set(FROM);
    }
    public final StringProperty fromProperty() {
        if (null == from) {
            from = new SimpleStringProperty(this, "from", "");
        }
        return from;
    }

    public final String getTo() {
        return null == to ? "" : to.get();
    }
    public final void setTo(final String TO) {
        toProperty().set(TO);
    }
    public final StringProperty toProperty() {
        if (null == to) {
            to = new SimpleStringProperty(this, "to", "");
        }
        return to;
    }

    public final boolean isTitleVisible() {
        return null == titleVisible ? true : titleVisible.get();
    }
    public final void setTitleVisible(final boolean TITLE_VISIBLE) {
        titleVisibleProperty().set(TITLE_VISIBLE);
    }
    public final BooleanProperty titleVisibleProperty() {
        if (null == titleVisible) {
            titleVisible = new SimpleBooleanProperty(this, "titleVisible", true);
        }
        return titleVisible;
    }


    // ******************** CSS Stylable Properties ***************************
    public final Paint getBulletFill() {
        return null == bulletFill ? DEFAULT_BULLET_FILL : bulletFill.get();
    }
    public final void setBulletFill(Paint value) {
        bulletFillProperty().set(value);
    }
    public final ObjectProperty<Paint> bulletFillProperty() {
        if (null == bulletFill) {
            bulletFill = new StyleableObjectProperty<Paint>(DEFAULT_BULLET_FILL) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.BULLET_FILL; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "bulletFill"; }
            };
        }
        return bulletFill;
    }

    public final Paint getSeriesStroke() {
        return null == seriesStroke ? DEFAULT_SERIES_STROKE : seriesStroke.get();
    }
    public final void setSeriesStroke(Paint value) {
        seriesStrokeProperty().set(value);
    }
    public final ObjectProperty<Paint> seriesStrokeProperty() {
        if (null == seriesStroke) {
            seriesStroke = new StyleableObjectProperty<Paint>(DEFAULT_SERIES_STROKE) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SERIES_STROKE; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "seriesStroke"; }
            };
        }
        return seriesStroke;
    }

    public final Paint getSectionFill0() {
        return null == sectionFill0 ? DEFAULT_SECTION_FILL_0 : sectionFill0.get();
    }
    public final void setSectionFill0(Paint value) {
        sectionFill0Property().set(value);
    }
    public final ObjectProperty<Paint> sectionFill0Property() {
        if (null == sectionFill0) {
            sectionFill0 = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_FILL_0) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_FILL_0; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "sectionFill0"; }
            };
        }
        return sectionFill0;
    }

    public final Paint getSectionFill1() {
        return null == sectionFill1 ? DEFAULT_SECTION_FILL_1 : sectionFill1.get();
    }
    public final void setSectionFill1(Paint value) {
        sectionFill1Property().set(value);
    }
    public final ObjectProperty<Paint> sectionFill1Property() {
        if (null == sectionFill1) {
            sectionFill1 = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_FILL_1) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_FILL_1; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "sectionFill1"; }
            };
        }
        return sectionFill1;
    }

    public final Paint getSectionFill2() {
        return null == sectionFill2 ? DEFAULT_SECTION_FILL_2 : sectionFill2.get();
    }
    public final void setSectionFill2(Paint value) {
        sectionFill2Property().set(value);
    }
    public final ObjectProperty<Paint> sectionFill2Property() {
        if (null == sectionFill2) {
            sectionFill2 = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_FILL_2) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_FILL_2; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "sectionFill2"; }
            };
        }
        return sectionFill2;
    }

    public final Paint getSectionFill3() {
        return null == sectionFill3 ? DEFAULT_SECTION_FILL_3 : sectionFill3.get();
    }
    public final void setSectionFill3(Paint value) {
        sectionFill3Property().set(value);
    }
    public final ObjectProperty<Paint> sectionFill3Property() {
        if (null == sectionFill3) {
            sectionFill3 = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_FILL_3) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_FILL_3; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "sectionFill3"; }
            };
        }
        return sectionFill3;
    }

    public final Paint getSectionFill4() {
        return null == sectionFill4 ? DEFAULT_SECTION_FILL_4 : sectionFill4.get();
    }
    public final void setSectionFill4(Paint value) {
        sectionFill4Property().set(value);
    }
    public final ObjectProperty<Paint> sectionFill4Property() {
        if (null == sectionFill4) {
            sectionFill4 = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_FILL_4) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_FILL_4; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "sectionFill4"; }
            };
        }
        return sectionFill4;
    }

    public final Paint getSectionFill5() {
        return null == sectionFill5 ? DEFAULT_SECTION_FILL_5 : sectionFill5.get();
    }
    public final void setSectionFill5(Paint value) {
        sectionFill5Property().set(value);
    }
    public final ObjectProperty<Paint> sectionFill5Property() {
        if (null == sectionFill5) {
            sectionFill5 = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_FILL_5) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_FILL_5; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "sectionFill5"; }
            };
        }
        return sectionFill5;
    }

    public final Paint getSectionFill6() {
        return null == sectionFill6 ? DEFAULT_SECTION_FILL_6 : sectionFill6.get();
    }
    public final void setSectionFill6(Paint value) {
        sectionFill6Property().set(value);
    }
    public final ObjectProperty<Paint> sectionFill6Property() {
        if (null == sectionFill6) {
            sectionFill6 = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_FILL_6) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_FILL_6; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "sectionFill6"; }
            };
        }
        return sectionFill6;
    }

    public final Paint getSectionFill7() {
        return null == sectionFill7 ? DEFAULT_SECTION_FILL_7 : sectionFill7.get();
    }
    public final void setSectionFill7(Paint value) {
        sectionFill7Property().set(value);
    }
    public final ObjectProperty<Paint> sectionFill7Property() {
        if (null == sectionFill7) {
            sectionFill7 = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_FILL_7) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_FILL_7; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "sectionFill7"; }
            };
        }
        return sectionFill7;
    }

    public final Paint getSectionFill8() {
        return null == sectionFill8 ? DEFAULT_SECTION_FILL_8 : sectionFill8.get();
    }
    public final void setSectionFill8(Paint value) {
        sectionFill8Property().set(value);
    }
    public final ObjectProperty<Paint> sectionFill8Property() {
        if (null == sectionFill8) {
            sectionFill8 = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_FILL_8) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_FILL_8; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "sectionFill8"; }
            };
        }
        return sectionFill8;
    }

    public final Paint getSectionFill9() {
        return null == sectionFill9 ? DEFAULT_SECTION_FILL_9 : sectionFill9.get();
    }
    public final void setSectionFill9(Paint value) {
        sectionFill9Property().set(value);
    }
    public final ObjectProperty<Paint> sectionFill9Property() {
        if (null == sectionFill9) {
            sectionFill9 = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_FILL_9) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_FILL_9; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "sectionFill9"; }
            };
        }
        return sectionFill9;
    }


    // ******************** Style related *************************************
    @Override protected Skin createDefaultSkin() {
        return new SimpleLineChartSkin(this);
    }

    @Override public String getUserAgentStylesheet() {
        return getClass().getResource("/eu/hansolo/enzo/charts/simplelinechart.css").toExternalForm();
    }

    private static class StyleableProperties {
        private static final CssMetaData<SimpleLineChart, Paint> BULLET_FILL =
            new CssMetaData<SimpleLineChart, Paint>("-bullet-fill", PaintConverter.getInstance(), DEFAULT_BULLET_FILL) {

                @Override public boolean isSettable(SimpleLineChart chart) {
                    return null == chart.bulletFill || !chart.bulletFill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimpleLineChart chart) {
                    return (StyleableProperty) chart.bulletFillProperty();
                }

                @Override public Paint getInitialValue(SimpleLineChart chart) {
                    return chart.getBulletFill();
                }
            };

        private static final CssMetaData<SimpleLineChart, Paint> SERIES_STROKE =
            new CssMetaData<SimpleLineChart, Paint>("-series-stroke", PaintConverter.getInstance(), DEFAULT_SERIES_STROKE) {

                @Override public boolean isSettable(SimpleLineChart chart) {
                    return null == chart.seriesStroke || !chart.seriesStroke.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimpleLineChart chart) {
                    return (StyleableProperty) chart.seriesStrokeProperty();
                }

                @Override public Paint getInitialValue(SimpleLineChart chart) {
                    return chart.getSeriesStroke();
                }
            };

        private static final CssMetaData<SimpleLineChart, Paint> SECTION_FILL_0 =
            new CssMetaData<SimpleLineChart, Paint>("-section-fill-0", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_0) {

                @Override public boolean isSettable(SimpleLineChart chart) {
                    return null == chart.sectionFill0 || !chart.sectionFill0.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimpleLineChart chart) {
                    return (StyleableProperty) chart.sectionFill0Property();
                }

                @Override public Paint getInitialValue(SimpleLineChart chart) {
                    return chart.getSectionFill0();
                }
            };

        private static final CssMetaData<SimpleLineChart, Paint> SECTION_FILL_1 =
            new CssMetaData<SimpleLineChart, Paint>("-section-fill-1", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_1) {

                @Override public boolean isSettable(SimpleLineChart chart) {
                    return null == chart.sectionFill1 || !chart.sectionFill1.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimpleLineChart chart) {
                    return (StyleableProperty) chart.sectionFill1Property();
                }

                @Override public Paint getInitialValue(SimpleLineChart chart) {
                    return chart.getSectionFill1();
                }
            };

        private static final CssMetaData<SimpleLineChart, Paint> SECTION_FILL_2 =
            new CssMetaData<SimpleLineChart, Paint>("-section-fill-2", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_2) {

                @Override public boolean isSettable(SimpleLineChart chart) {
                    return null == chart.sectionFill2 || !chart.sectionFill2.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimpleLineChart chart) {
                    return (StyleableProperty) chart.sectionFill2Property();
                }

                @Override public Paint getInitialValue(SimpleLineChart chart) {
                    return chart.getSectionFill2();
                }
            };

        private static final CssMetaData<SimpleLineChart, Paint> SECTION_FILL_3 =
            new CssMetaData<SimpleLineChart, Paint>("-section-fill-3", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_3) {

                @Override public boolean isSettable(SimpleLineChart chart) {
                    return null == chart.sectionFill3 || !chart.sectionFill3.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimpleLineChart chart) {
                    return (StyleableProperty) chart.sectionFill3Property();
                }

                @Override public Paint getInitialValue(SimpleLineChart chart) {
                    return chart.getSectionFill3();
                }
            };

        private static final CssMetaData<SimpleLineChart, Paint> SECTION_FILL_4 =
            new CssMetaData<SimpleLineChart, Paint>("-section-fill-4", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_4) {

                @Override public boolean isSettable(SimpleLineChart chart) {
                    return null == chart.sectionFill4 || !chart.sectionFill4.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimpleLineChart chart) {
                    return (StyleableProperty) chart.sectionFill4Property();
                }

                @Override public Paint getInitialValue(SimpleLineChart chart) {
                    return chart.getSectionFill4();
                }
            };

        private static final CssMetaData<SimpleLineChart, Paint> SECTION_FILL_5 =
            new CssMetaData<SimpleLineChart, Paint>("-section-fill-5", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_5) {

                @Override public boolean isSettable(SimpleLineChart chart) {
                    return null == chart.sectionFill5 || !chart.sectionFill5.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimpleLineChart chart) {
                    return (StyleableProperty) chart.sectionFill5Property();
                }

                @Override public Paint getInitialValue(SimpleLineChart chart) {
                    return chart.getSectionFill5();
                }
            };

        private static final CssMetaData<SimpleLineChart, Paint> SECTION_FILL_6 =
            new CssMetaData<SimpleLineChart, Paint>("-section-fill-6", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_6) {

                @Override public boolean isSettable(SimpleLineChart chart) {
                    return null == chart.sectionFill6 || !chart.sectionFill6.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimpleLineChart chart) {
                    return (StyleableProperty) chart.sectionFill6Property();
                }

                @Override public Paint getInitialValue(SimpleLineChart chart) {
                    return chart.getSectionFill6();
                }
            };

        private static final CssMetaData<SimpleLineChart, Paint> SECTION_FILL_7 =
            new CssMetaData<SimpleLineChart, Paint>("-section-fill-7", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_7) {

                @Override public boolean isSettable(SimpleLineChart chart) {
                    return null == chart.sectionFill7 || !chart.sectionFill7.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimpleLineChart chart) {
                    return (StyleableProperty) chart.sectionFill7Property();
                }

                @Override public Paint getInitialValue(SimpleLineChart chart) {
                    return chart.getSectionFill7();
                }
            };

        private static final CssMetaData<SimpleLineChart, Paint> SECTION_FILL_8 =
            new CssMetaData<SimpleLineChart, Paint>("-section-fill-8", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_8) {

                @Override public boolean isSettable(SimpleLineChart chart) {
                    return null == chart.sectionFill8 || !chart.sectionFill8.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimpleLineChart chart) {
                    return (StyleableProperty) chart.sectionFill8Property();
                }

                @Override public Paint getInitialValue(SimpleLineChart chart) {
                    return chart.getSectionFill8();
                }
            };

        private static final CssMetaData<SimpleLineChart, Paint> SECTION_FILL_9 =
            new CssMetaData<SimpleLineChart, Paint>("-section-fill-9", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_9) {

                @Override public boolean isSettable(SimpleLineChart chart) {
                    return null == chart.sectionFill9 || !chart.sectionFill9.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimpleLineChart chart) {
                    return (StyleableProperty) chart.sectionFill9Property();
                }

                @Override public Paint getInitialValue(SimpleLineChart chart) {
                    return chart.getSectionFill9();
                }
            };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables,
                               BULLET_FILL,
                               SERIES_STROKE,
                               SECTION_FILL_0,
                               SECTION_FILL_1,
                               SECTION_FILL_2,
                               SECTION_FILL_3,
                               SECTION_FILL_4,
                               SECTION_FILL_5,
                               SECTION_FILL_6,
                               SECTION_FILL_7,
                               SECTION_FILL_8,
                               SECTION_FILL_9);
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
