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
import eu.hansolo.enzo.charts.skin.SimplePieChartSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * User: hansolo
 * Date: 17.12.13
 * Time: 07:42
 */
public class SimplePieChart extends Control {
    public static enum DataFormat {
        TEXT,
        VALUE,
        PERCENTAGE,
        TEXT_AND_VALUE,
        TEXT_AND_PERCENTAGE
    }

    // Default section colors
    private static final Color DEFAULT_INFO_COLOR         = Color.web("#494949");
    private static final Color DEFAULT_INFO_TEXT_COLOR    = Color.web("#ffffff");
    private static final Color DEFAULT_TITLE_TEXT_COLOR   = Color.web("#ffffff");
    private static final Color DEFAULT_SECTION_TEXT_COLOR = Color.web("#ffffff");
    private static final Color DEFAULT_SECTION_FILL_0     = Color.web("#f3622d");
    private static final Color DEFAULT_SECTION_FILL_1     = Color.web("#fba71b");
    private static final Color DEFAULT_SECTION_FILL_2     = Color.web("#57b757");
    private static final Color DEFAULT_SECTION_FILL_3     = Color.web("#41a9c9");
    private static final Color DEFAULT_SECTION_FILL_4     = Color.web("#4258c9");
    private static final Color DEFAULT_SECTION_FILL_5     = Color.web("#9a42c8");
    private static final Color DEFAULT_SECTION_FILL_6     = Color.web("#c84164");
    private static final Color DEFAULT_SECTION_FILL_7     = Color.web("#1aa085");
    private static final Color DEFAULT_SECTION_FILL_8     = Color.web("#27ae60");
    private static final Color DEFAULT_SECTION_FILL_9     = Color.web("#5399c6");
    private static final Color DEFAULT_SECTION_FILL_10    = Color.web("#8e44ad");
    private static final Color DEFAULT_SECTION_FILL_11    = Color.web("#f5af41");
    private static final Color DEFAULT_SECTION_FILL_12    = Color.web("#d35519");
    private static final Color DEFAULT_SECTION_FILL_13    = Color.web("#cc6055");
    private static final Color DEFAULT_SECTION_FILL_14    = Color.web("#2c3e50");

    private DoubleProperty                sum;
    private int                           _decimals;
    private IntegerProperty               decimals;
    private double                        _startAngle;
    private DoubleProperty                startAngle;
    private boolean                       _sectionTextVisible;
    private BooleanProperty               sectionTextVisible;
    private DataFormat                    _dataFormat;
    private ObjectProperty<DataFormat>    dataFormat;

    private ObservableList<PieChart.Data> data;
    private String                        _title;
    private StringProperty                title;

    // CSS styleable properties
    private ObjectProperty<Paint>         infoColor;
    private ObjectProperty<Paint>         infoTextColor;
    private ObjectProperty<Paint>         titleTextColor;
    private ObjectProperty<Paint>         sectionTextColor;
    private ObjectProperty<Paint>         sectionFill0;
    private ObjectProperty<Paint>         sectionFill1;
    private ObjectProperty<Paint>         sectionFill2;
    private ObjectProperty<Paint>         sectionFill3;
    private ObjectProperty<Paint>         sectionFill4;
    private ObjectProperty<Paint>         sectionFill5;
    private ObjectProperty<Paint>         sectionFill6;
    private ObjectProperty<Paint>         sectionFill7;
    private ObjectProperty<Paint>         sectionFill8;
    private ObjectProperty<Paint>         sectionFill9;
    private ObjectProperty<Paint>         sectionFill10;
    private ObjectProperty<Paint>         sectionFill11;
    private ObjectProperty<Paint>         sectionFill12;
    private ObjectProperty<Paint>         sectionFill13;
    private ObjectProperty<Paint>         sectionFill14;


    // ******************** Constructors **************************************
    public SimplePieChart() {
        getStyleClass().add("simple-pie-chart");
        sum                 = new SimpleDoubleProperty(this, "sum", 0);
        _decimals           = 0;
        _startAngle         = -90;
        _sectionTextVisible = true;
        _dataFormat         = DataFormat.TEXT_AND_VALUE;
        _title              = "";
        data                = FXCollections.observableArrayList();
        data.addListener((ListChangeListener<PieChart.Data>) change -> setSum(data.stream().mapToDouble(d -> d.getPieValue()).sum()));
    }


    // ******************** Methods *******************************************
    public final double getSum() {
        return sum.get();
    }
    public final void setSum(final double SUM) {
        sum.set(SUM);
    }
    public final DoubleProperty sumProperty() {
        return sum;
    }

    public final int getDecimals() {
        return null == decimals ? _decimals : decimals.get();
    }
    public final void setDecimals(final int DECIMALS) {
        if (null == decimals) {
            _decimals = clamp(0, 3, DECIMALS);
        } else {
            decimals.set(DECIMALS);
        }
    }
    public final IntegerProperty decimalsProperty() {
        if (null == decimals) {
            decimals = new IntegerPropertyBase(_decimals) {
                @Override protected void invalidated() { set(clamp(0, 3, get())); }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "decimals"; }
            };
        }
        return decimals;
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
                @Override protected void invalidated() {
                    set(clamp(0d, 360d, get()));
                }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "startAngle"; }
            };
        }
        return startAngle;
    }

    // Properties related to visualization
    public final ObservableList<PieChart.Data> getData() { return data; }
    public final void setData(final List<PieChart.Data> DATA) {
        data.setAll(DATA);
    }
    public final void setData(final PieChart.Data... DATA) {
        setData(Arrays.asList(DATA));
    }
    public final void addData(final PieChart.Data DATA_POINT) {
        data.add(DATA_POINT);
    }
    public final void removeData(final PieChart.Data DATA_POINT) {
        if (data.contains(DATA_POINT)) data.remove(DATA_POINT);
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

    public final boolean isSectionTextVisible() {
        return null == sectionTextVisible ? _sectionTextVisible : sectionTextVisible.get();
    }
    public final void setSectionTextVisible(final boolean SECTION_TEXT_VISIBLE) {
        if (null == sectionTextVisible) {
            _sectionTextVisible = SECTION_TEXT_VISIBLE;
        } else {
            sectionTextVisible.set(SECTION_TEXT_VISIBLE);
        }
    }
    public final BooleanProperty sectionTextVisibleProperty() {
        if (null == sectionTextVisible) {
            sectionTextVisible = new SimpleBooleanProperty(this, "sectionTextVisible", _sectionTextVisible);
        }
        return sectionTextVisible;
    }

    public final DataFormat getDataFormat() {
        return null == dataFormat ? _dataFormat : dataFormat.get();
    }
    public final void setDataFormat(final DataFormat DATA_FORMAT) {
        if (null == dataFormat) {
            _dataFormat = DATA_FORMAT;
        } else {
            dataFormat.set(DATA_FORMAT);
        }
    }
    public final ObjectProperty<DataFormat> dataFormatProperty() {
        if (null == dataFormat) {
            dataFormat = new SimpleObjectProperty<>(this, "format", _dataFormat);
        }
        return dataFormat;
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


    // ******************** CSS Stylable Properties ***************************
    public final Paint getInfoColor() {
        return null == infoColor ? DEFAULT_INFO_COLOR : infoColor.get();
    }
    public final void setInfoColor(final Paint INFO_COLOR) {
        infoColorProperty().set(INFO_COLOR);
    }
    public final ObjectProperty<Paint> infoColorProperty() {
        if (null == infoColor) {
            infoColor = new StyleableObjectProperty<Paint>() {
                @Override public CssMetaData<? extends Styleable, Paint> getCssMetaData() { return StyleableProperties.INFO_COLOR; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "infoColor"; }
            };
        }
        return infoColor;
    }

    public final Paint getInfoTextColor() {
        return null == infoTextColor ? DEFAULT_INFO_TEXT_COLOR : infoTextColor.get();
    }
    public final void setInfoTextColor(Paint value) {
        infoTextColorProperty().set(value);
    }
    public final ObjectProperty<Paint> infoTextColorProperty() {
        if (null == infoTextColor) {
            infoTextColor = new StyleableObjectProperty<Paint>(DEFAULT_INFO_TEXT_COLOR) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.INFO_TEXT_COLOR; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "infoTextColor"; }
            };
        }
        return infoTextColor;
    }

    public final Paint getTitleTextColor() {
        return null == titleTextColor ? DEFAULT_TITLE_TEXT_COLOR : titleTextColor.get();
    }
    public final void setTitleTextColor(Paint value) {
        titleTextColorProperty().set(value);
    }
    public final ObjectProperty<Paint> titleTextColorProperty() {
        if (null == titleTextColor) {
            titleTextColor = new StyleableObjectProperty<Paint>(DEFAULT_TITLE_TEXT_COLOR) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.TITLE_TEXT_COLOR; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "titleTextColor"; }
            };
        }
        return titleTextColor;
    }

    public final Paint getSectionTextColor() {
        return null == sectionTextColor ? DEFAULT_SECTION_TEXT_COLOR : sectionTextColor.get();
    }
    public final void setSectionTextColor(Paint value) {
        sectionTextColorProperty().set(value);
    }
    public final ObjectProperty<Paint> sectionTextColorProperty() {
        if (null == sectionTextColor) {
            sectionTextColor = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_TEXT_COLOR) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_TEXT_COLOR; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "sectionTextColor"; }
            };
        }
        return sectionTextColor;
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

    public final Paint getSectionFill10() {
        return null == sectionFill10 ? DEFAULT_SECTION_FILL_10 : sectionFill10.get();
    }
    public final void setSectionFill10(Paint value) {
        sectionFill10Property().set(value);
    }
    public final ObjectProperty<Paint> sectionFill10Property() {
        if (null == sectionFill10) {
            sectionFill10 = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_FILL_10) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_FILL_10; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "sectionFill10"; }
            };
        }
        return sectionFill10;
    }

    public final Paint getSectionFill11() {
        return null == sectionFill11 ? DEFAULT_SECTION_FILL_11 : sectionFill11.get();
    }
    public final void setSectionFill11(Paint value) {
        sectionFill11Property().set(value);
    }
    public final ObjectProperty<Paint> sectionFill11Property() {
        if (null == sectionFill11) {
            sectionFill11 = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_FILL_11) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_FILL_11; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "sectionFill11"; }
            };
        }
        return sectionFill11;
    }

    public final Paint getSectionFill12() {
        return null == sectionFill12 ? DEFAULT_SECTION_FILL_12 : sectionFill12.get();
    }
    public final void setSectionFill12(Paint value) {
        sectionFill12Property().set(value);
    }
    public final ObjectProperty<Paint> sectionFill12Property() {
        if (null == sectionFill12) {
            sectionFill12 = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_FILL_12) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_FILL_12; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "sectionFill12"; }
            };
        }
        return sectionFill12;
    }

    public final Paint getSectionFill13() {
        return null == sectionFill13 ? DEFAULT_SECTION_FILL_13 : sectionFill13.get();
    }
    public final void setSectionFill13(Paint value) {
        sectionFill13Property().set(value);
    }
    public final ObjectProperty<Paint> sectionFill13Property() {
        if (null == sectionFill13) {
            sectionFill13 = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_FILL_13) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_FILL_13; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "sectionFill13"; }
            };
        }
        return sectionFill13;
    }

    public final Paint getSectionFill14() {
        return null == sectionFill14 ? DEFAULT_SECTION_FILL_14 : sectionFill14.get();
    }
    public final void setSectionFill14(Paint value) {
        sectionFill14Property().set(value);
    }
    public final ObjectProperty<Paint> sectionFill14Property() {
        if (null == sectionFill14) {
            sectionFill14 = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_FILL_14) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_FILL_14; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "sectionFill14"; }
            };
        }
        return sectionFill14;
    }


    // ******************** Style related *************************************
    @Override protected Skin createDefaultSkin() {
        return new SimplePieChartSkin(this);
    }

    @Override public String getUserAgentStylesheet() {
        return getClass().getResource("/eu/hansolo/enzo/charts/simplepiechart.css").toExternalForm();
    }

    private static class StyleableProperties {
        private static final CssMetaData<SimplePieChart, Paint> INFO_COLOR =
            new CssMetaData<SimplePieChart, Paint>("-info-color", PaintConverter.getInstance(), DEFAULT_INFO_COLOR) {

                @Override public boolean isSettable(SimplePieChart chart) {
                    return null == chart.infoColor || !chart.infoColor.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimplePieChart chart) {
                    return (StyleableProperty) chart.infoColorProperty();
                }

                @Override public Paint getInitialValue(SimplePieChart chart) {
                    return chart.getInfoColor();
                }
            };

        private static final CssMetaData<SimplePieChart, Paint> INFO_TEXT_COLOR =
            new CssMetaData<SimplePieChart, Paint>("-info-text-color", PaintConverter.getInstance(), DEFAULT_INFO_TEXT_COLOR) {

                @Override public boolean isSettable(SimplePieChart chart) {
                    return null == chart.infoTextColor || !chart.infoTextColor.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimplePieChart chart) {
                    return (StyleableProperty) chart.infoTextColorProperty();
                }

                @Override public Paint getInitialValue(SimplePieChart chart) {
                    return chart.getInfoTextColor();
                }
            };

        private static final CssMetaData<SimplePieChart, Paint> TITLE_TEXT_COLOR =
            new CssMetaData<SimplePieChart, Paint>("-title-text", PaintConverter.getInstance(), DEFAULT_TITLE_TEXT_COLOR) {

                @Override public boolean isSettable(SimplePieChart chart) {
                    return null == chart.titleTextColor || !chart.titleTextColor.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimplePieChart chart) {
                    return (StyleableProperty) chart.titleTextColorProperty();
                }

                @Override public Paint getInitialValue(SimplePieChart chart) {
                    return chart.getTitleTextColor();
                }
            };

        private static final CssMetaData<SimplePieChart, Paint> SECTION_TEXT_COLOR =
            new CssMetaData<SimplePieChart, Paint>("-section-text", PaintConverter.getInstance(), DEFAULT_SECTION_TEXT_COLOR) {

                @Override public boolean isSettable(SimplePieChart chart) {
                    return null == chart.sectionTextColor || !chart.sectionTextColor.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimplePieChart chart) {
                    return (StyleableProperty) chart.sectionTextColorProperty();
                }

                @Override public Paint getInitialValue(SimplePieChart chart) {
                    return chart.getSectionTextColor();
                }
            };

        private static final CssMetaData<SimplePieChart, Paint> SECTION_FILL_0 =
            new CssMetaData<SimplePieChart, Paint>("-section-fill-0", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_0) {

                @Override public boolean isSettable(SimplePieChart chart) {
                    return null == chart.sectionFill0 || !chart.sectionFill0.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimplePieChart chart) {
                    return (StyleableProperty) chart.sectionFill0Property();
                }

                @Override public Paint getInitialValue(SimplePieChart chart) {
                    return chart.getSectionFill0();
                }
            };

        private static final CssMetaData<SimplePieChart, Paint> SECTION_FILL_1 =
            new CssMetaData<SimplePieChart, Paint>("-section-fill-1", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_1) {

                @Override public boolean isSettable(SimplePieChart chart) {
                    return null == chart.sectionFill1 || !chart.sectionFill1.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimplePieChart chart) {
                    return (StyleableProperty) chart.sectionFill1Property();
                }

                @Override public Paint getInitialValue(SimplePieChart chart) {
                    return chart.getSectionFill1();
                }
            };

        private static final CssMetaData<SimplePieChart, Paint> SECTION_FILL_2 =
            new CssMetaData<SimplePieChart, Paint>("-section-fill-2", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_2) {

                @Override public boolean isSettable(SimplePieChart chart) {
                    return null == chart.sectionFill2 || !chart.sectionFill2.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimplePieChart chart) {
                    return (StyleableProperty) chart.sectionFill2Property();
                }

                @Override public Paint getInitialValue(SimplePieChart chart) {
                    return chart.getSectionFill2();
                }
            };

        private static final CssMetaData<SimplePieChart, Paint> SECTION_FILL_3 =
            new CssMetaData<SimplePieChart, Paint>("-section-fill-3", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_3) {

                @Override public boolean isSettable(SimplePieChart chart) {
                    return null == chart.sectionFill3 || !chart.sectionFill3.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimplePieChart chart) {
                    return (StyleableProperty) chart.sectionFill3Property();
                }

                @Override public Paint getInitialValue(SimplePieChart chart) {
                    return chart.getSectionFill3();
                }
            };

        private static final CssMetaData<SimplePieChart, Paint> SECTION_FILL_4 =
            new CssMetaData<SimplePieChart, Paint>("-section-fill-4", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_4) {

                @Override public boolean isSettable(SimplePieChart chart) {
                    return null == chart.sectionFill4 || !chart.sectionFill4.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimplePieChart chart) {
                    return (StyleableProperty) chart.sectionFill4Property();
                }

                @Override public Paint getInitialValue(SimplePieChart chart) {
                    return chart.getSectionFill4();
                }
            };

        private static final CssMetaData<SimplePieChart, Paint> SECTION_FILL_5 =
            new CssMetaData<SimplePieChart, Paint>("-section-fill-5", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_5) {

                @Override public boolean isSettable(SimplePieChart chart) {
                    return null == chart.sectionFill5 || !chart.sectionFill5.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimplePieChart chart) {
                    return (StyleableProperty) chart.sectionFill5Property();
                }

                @Override public Paint getInitialValue(SimplePieChart chart) {
                    return chart.getSectionFill5();
                }
            };

        private static final CssMetaData<SimplePieChart, Paint> SECTION_FILL_6 =
            new CssMetaData<SimplePieChart, Paint>("-section-fill-6", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_6) {

                @Override public boolean isSettable(SimplePieChart chart) {
                    return null == chart.sectionFill6 || !chart.sectionFill6.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimplePieChart chart) {
                    return (StyleableProperty) chart.sectionFill6Property();
                }

                @Override public Paint getInitialValue(SimplePieChart chart) {
                    return chart.getSectionFill6();
                }
            };

        private static final CssMetaData<SimplePieChart, Paint> SECTION_FILL_7 =
            new CssMetaData<SimplePieChart, Paint>("-section-fill-7", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_7) {

                @Override public boolean isSettable(SimplePieChart chart) {
                    return null == chart.sectionFill7 || !chart.sectionFill7.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimplePieChart chart) {
                    return (StyleableProperty) chart.sectionFill7Property();
                }

                @Override public Paint getInitialValue(SimplePieChart chart) {
                    return chart.getSectionFill7();
                }
            };

        private static final CssMetaData<SimplePieChart, Paint> SECTION_FILL_8 =
            new CssMetaData<SimplePieChart, Paint>("-section-fill-8", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_8) {

                @Override public boolean isSettable(SimplePieChart chart) {
                    return null == chart.sectionFill8 || !chart.sectionFill8.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimplePieChart chart) {
                    return (StyleableProperty) chart.sectionFill8Property();
                }

                @Override public Paint getInitialValue(SimplePieChart chart) {
                    return chart.getSectionFill8();
                }
            };

        private static final CssMetaData<SimplePieChart, Paint> SECTION_FILL_9 =
            new CssMetaData<SimplePieChart, Paint>("-section-fill-9", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_9) {

                @Override public boolean isSettable(SimplePieChart chart) {
                    return null == chart.sectionFill9 || !chart.sectionFill9.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimplePieChart chart) {
                    return (StyleableProperty) chart.sectionFill9Property();
                }

                @Override public Paint getInitialValue(SimplePieChart chart) {
                    return chart.getSectionFill9();
                }
            };

        private static final CssMetaData<SimplePieChart, Paint> SECTION_FILL_10 =
            new CssMetaData<SimplePieChart, Paint>("-section-fill-10", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_10) {

                @Override public boolean isSettable(SimplePieChart chart) {
                    return null == chart.sectionFill10 || !chart.sectionFill10.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimplePieChart chart) {
                    return (StyleableProperty) chart.sectionFill10Property();
                }

                @Override public Paint getInitialValue(SimplePieChart chart) {
                    return chart.getSectionFill10();
                }
            };

        private static final CssMetaData<SimplePieChart, Paint> SECTION_FILL_11 =
            new CssMetaData<SimplePieChart, Paint>("-section-fill-11", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_11) {

                @Override public boolean isSettable(SimplePieChart chart) {
                    return null == chart.sectionFill11 || !chart.sectionFill11.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimplePieChart chart) {
                    return (StyleableProperty) chart.sectionFill11Property();
                }

                @Override public Paint getInitialValue(SimplePieChart chart) {
                    return chart.getSectionFill11();
                }
            };

        private static final CssMetaData<SimplePieChart, Paint> SECTION_FILL_12 =
            new CssMetaData<SimplePieChart, Paint>("-section-fill-12", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_12) {

                @Override public boolean isSettable(SimplePieChart chart) {
                    return null == chart.sectionFill12 || !chart.sectionFill12.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimplePieChart chart) {
                    return (StyleableProperty) chart.sectionFill12Property();
                }

                @Override public Paint getInitialValue(SimplePieChart chart) {
                    return chart.getSectionFill12();
                }
            };

        private static final CssMetaData<SimplePieChart, Paint> SECTION_FILL_13 =
            new CssMetaData<SimplePieChart, Paint>("-section-fill-13", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_13) {

                @Override public boolean isSettable(SimplePieChart chart) {
                    return null == chart.sectionFill13 || !chart.sectionFill13.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimplePieChart chart) {
                    return (StyleableProperty) chart.sectionFill13Property();
                }

                @Override public Paint getInitialValue(SimplePieChart chart) {
                    return chart.getSectionFill13();
                }
            };

        private static final CssMetaData<SimplePieChart, Paint> SECTION_FILL_14 =
            new CssMetaData<SimplePieChart, Paint>("-section-fill-14", PaintConverter.getInstance(), DEFAULT_SECTION_FILL_14) {

                @Override public boolean isSettable(SimplePieChart chart) {
                    return null == chart.sectionFill14 || !chart.sectionFill14.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimplePieChart chart) {
                    return (StyleableProperty) chart.sectionFill14Property();
                }

                @Override public Paint getInitialValue(SimplePieChart chart) {
                    return chart.getSectionFill14();
                }
            };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables,
                               INFO_COLOR,
                               INFO_TEXT_COLOR,
                               TITLE_TEXT_COLOR,
                               SECTION_TEXT_COLOR,
                               SECTION_FILL_0,
                               SECTION_FILL_1,
                               SECTION_FILL_2,
                               SECTION_FILL_3,
                               SECTION_FILL_4,
                               SECTION_FILL_5,
                               SECTION_FILL_6,
                               SECTION_FILL_7,
                               SECTION_FILL_8,
                               SECTION_FILL_9,
                               SECTION_FILL_10,
                               SECTION_FILL_11,
                               SECTION_FILL_12,
                               SECTION_FILL_13,
                               SECTION_FILL_14);
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
