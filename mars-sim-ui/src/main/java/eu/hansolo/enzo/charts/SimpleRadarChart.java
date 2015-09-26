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

import com.sun.javafx.css.converters.ColorConverter;
import com.sun.javafx.css.converters.PaintConverter;
import eu.hansolo.enzo.charts.skin.SimpleRadarChartSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * User: hansolo
 * Date: 03.03.14
 * Time: 17:27
 */
public class SimpleRadarChart extends Control {
    private static final int MAX_NO_OF_SECTORS = 24;

    // Default section colors
    private static final Color DEFAULT_CHART_BACKGROUND = Color.web("#ffffff");
    private static final Color DEFAULT_CHART_FOREGROUND = Color.web("#dddddd");
    private static final Color DEFAULT_CHART_TEXT       = Color.web("#5c7490");
    private static final Paint DEFAULT_CHART_FILL       = Color.web("#8b8a8f");
    private static final Color DEFAULT_CHART_STROKE     = Color.web("#8b8a8f");
    private static final Color DEFAULT_ZERO_LINE_COLOR  = Color.web("#f00000");

    private IntegerProperty                                      noOfSectors;
    private ObservableMap<Integer, XYChart.Data<String, Double>> data;
    private DoubleProperty                                       minValue;
    private DoubleProperty                                       maxValue;
    private StringProperty                                       title;
    private StringProperty                                       unit;
    private BooleanProperty                                      scaleVisible;
    private BooleanProperty                                      polygonMode;
    private ObservableList<Stop>                                 gradientStops;
    private BooleanProperty                                      zeroLineVisible;
    private BooleanProperty                                      filled;

    // CSS styleable properties
    private ObjectProperty<Color>                                chartBackground;
    private ObjectProperty<Color>                                chartForeground;
    private ObjectProperty<Color>                                chartText;
    private ObjectProperty<Paint>                                chartFill;
    private ObjectProperty<Color>                                chartStroke;
    private ObjectProperty<Color>                                zeroLineColor;


    // ******************** Constructors **************************************
    public SimpleRadarChart() {
        getStyleClass().add("simple-pie-chart");
        minValue = new DoublePropertyBase(0) {
            @Override public void set(final double MIN_VALUE) {
                super.set(clamp(-Double.MAX_VALUE, getMaxValue(), MIN_VALUE));
            }
            @Override public Object getBean() { return SimpleRadarChart.this; }
            @Override public String getName() { return "minValue"; }
        };
        maxValue = new DoublePropertyBase(100) {
            @Override public void set(final double MAX_VALUE) {
                super.set(clamp(getMinValue(), Double.MAX_VALUE, MAX_VALUE));
            }
            @Override public Object getBean() { return SimpleRadarChart.this; }
            @Override public String getName() { return "maxValue"; }
        };
        gradientStops = FXCollections.observableArrayList();
        initData();
    }

    private void initData() {
        data = FXCollections.observableHashMap();
        for (int i = 0; i < (getNoOfSectors() + 1); i++) {
            data.put(i, new XYChart.Data<>(i < 10 ? "0" + i + ":00" : i + ":00", 0d));
        }
    }


    // ******************** Methods *******************************************
    public final double getMinValue() {
        return minValue.get();
    }
    public final void setMinValue(final double MIN_VALUE) {
        minValue.set(MIN_VALUE);
    }
    public final ReadOnlyDoubleProperty minValueProperty() {
        return minValue;
    }

    public final double getMaxValue() {
        return maxValue.get();
    }
    public final void setMaxValue(final double MAX_VALUE) {
        maxValue.set(MAX_VALUE);
    }
    public final ReadOnlyDoubleProperty maxValueProperty() {
        return maxValue;
    }

    public final ObservableList<Stop> getGradientStops() {
        return gradientStops;
    }
    public final void setGradientStops(final List<Stop> GRADIENT_STOPS) {
        gradientStops.setAll(GRADIENT_STOPS);
    }
    public final void setGradientStops(final Stop... GRADIENT_STOPS) {
        gradientStops.setAll(GRADIENT_STOPS);
    }
    public final void addGradientStop(final Stop GRADIENT_STOP) {
        gradientStops.add(GRADIENT_STOP);
    }

    public final String getTitle() {
        return null == title ? "" : title.get();
    }
    public final void setTitle(final String TITLE) {
        titleProperty().set(TITLE);
    }
    public final StringProperty titleProperty() {
        if (null == title) {
            title = new SimpleStringProperty(this, "title", "");
        }
        return title;
    }

    public final String getUnit() {
        return null == unit ? "" : unit.get();
    }
    public final void setUnit(final String UNIT) {
        unitProperty().set(UNIT);
    }
    public final StringProperty unitProperty() {
        if (null == unit) {
            unit = new SimpleStringProperty(this, "unit", "");
        }
        return unit;
    }

    public final boolean isScaleVisible() {
        return null == scaleVisible ? false : scaleVisible.get();
    }
    public final void setScaleVisible(final boolean SCALE_VISIBLE) {
        scaleVisibleProperty().set(SCALE_VISIBLE);
    }
    public final BooleanProperty scaleVisibleProperty() {
        if (null == scaleVisible) {
            scaleVisible = new SimpleBooleanProperty(this, "scaleVisible", false);
        }
        return scaleVisible;
    }

    public final boolean isPolygonMode() {
        return null == polygonMode ? true : polygonMode.get();
    }
    public final void setPolygonMode(final boolean POLYGON_MODE) {
        polygonModeProperty().set(POLYGON_MODE);
    }
    public final BooleanProperty polygonModeProperty() {
        if (null == polygonMode) {
            polygonMode = new SimpleBooleanProperty(this, "polygonMode", true);
        }
        return polygonMode;
    }

    public int getNoOfSectors() {
        return null == noOfSectors ? MAX_NO_OF_SECTORS : noOfSectors.get();
    }
    public void setNoOfSectors(final int NO_OF_SECTORS) {
        noOfSectorsProperty().set(NO_OF_SECTORS);
    }
    public IntegerProperty noOfSectorsProperty() {
        if (null == noOfSectors) {
            noOfSectors = new IntegerPropertyBase(MAX_NO_OF_SECTORS) {
                @Override public void set(final int NO_OF_SECTORS) {
                    super.set(clamp(1, MAX_NO_OF_SECTORS, NO_OF_SECTORS));
                }
                @Override public Object getBean() { return SimpleRadarChart.this; }
                @Override public String getName() { return "noOfSectors"; }
            };
        }
        return noOfSectors;
    }

    public final ObservableMap<Integer, XYChart.Data<String, Double>> getData() { return data; }
    public final void setData(final Map<Integer, XYChart.Data<String, Double>> DATA) {
        if (DATA.size() > MAX_NO_OF_SECTORS) throw new IllegalArgumentException("Too many sectors (max. " + getNoOfSectors() + " sectors allowed)");
        for (Integer key : DATA.keySet()) {
            addData(key, DATA.get(key));
        }
    }
    public final void addData(final int KEY, final XYChart.Data<String, Double> DATA) {
        if (KEY < 0 || KEY > (getNoOfSectors() + 1)) throw new IllegalArgumentException("Too many sectors (max. " + getNoOfSectors() + " sectors allowed)");
        DATA.setYValue(clamp(getMinValue(), getMaxValue(), DATA.getYValue()));
        data.put(KEY, DATA);
    }
    public final XYChart.Data<String, Double> getData(final int KEY) {
        return data.containsKey(KEY) ? data.get(KEY) : new XYChart.Data<>("", getMinValue());
    }

    public final void reset() {
        for (int i = 0 ; i < 25 ; i++) {
            data.get(i).setYValue(0d);
        }
    }

    public final boolean isZeroLineVisible() {
        return null == zeroLineVisible ? false : zeroLineVisible.get();
    }
    public final void setZeroLineVisible(final boolean ZERO_LINE_VISIBLE) {
        zeroLineVisibleProperty().set(ZERO_LINE_VISIBLE);
    }
    public final BooleanProperty zeroLineVisibleProperty() {
        if (null == zeroLineVisible) {
            zeroLineVisible = new SimpleBooleanProperty(this, "zeroLineVisible", false);
        }
        return zeroLineVisible;
    }

    public final boolean isFilled() {
        return null == filled ? true : filled.get();
    }
    public final void setFilled(final boolean FILLED) {
        filledProperty().set(FILLED);
    }
    public final BooleanProperty filledProperty() {
        if (null == filled) {
            filled = new SimpleBooleanProperty(this, "filled", true);
        }
        return filled;
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
    public final Color getChartBackground() {
        return null == chartBackground ? DEFAULT_CHART_BACKGROUND : chartBackground.get();
    }
    public final void setChartBackground(final Color CHART_BACKGROUND) {
        chartBackgroundProperty().set(CHART_BACKGROUND);
    }
    public final ObjectProperty<Color> chartBackgroundProperty() {
        if (null == chartBackground) {
            chartBackground = new StyleableObjectProperty<Color>(DEFAULT_CHART_BACKGROUND) {
                @Override public CssMetaData<? extends Styleable, Color> getCssMetaData() { return StyleableProperties.CHART_BACKGROUND; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "chartBackground"; }
            };
        }
        return chartBackground;
    }

    public final Color getChartForeground() {
        return null == chartForeground ? DEFAULT_CHART_FOREGROUND : chartForeground.get();
    }
    public final void setChartForeground(final Color CHART_FOREGROUND) {
        chartForegroundProperty().set(CHART_FOREGROUND);
    }
    public final ObjectProperty<Color> chartForegroundProperty() {
        if (null == chartForeground) {
            chartForeground = new StyleableObjectProperty<Color>(DEFAULT_CHART_FOREGROUND) {
                @Override public CssMetaData<? extends Styleable, Color> getCssMetaData() { return StyleableProperties.CHART_FOREGROUND; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "chartForeground"; }
            };
        }
        return chartBackground;
    }

    public final Color getChartText() {
        return null == chartText ? DEFAULT_CHART_TEXT : chartText.get();
    }
    public final void setChartText(Color value) {
        chartTextProperty().set(value);
    }
    public final ObjectProperty<Color> chartTextProperty() {
        if (null == chartText) {
            chartText = new StyleableObjectProperty<Color>(DEFAULT_CHART_TEXT) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.CHART_TEXT; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "chartText"; }
            };
        }
        return chartText;
    }

    public final Paint getChartFill() {
        return null == chartFill ? DEFAULT_CHART_FILL : chartFill.get();
    }
    public final void setChartFill(final Paint CHART_FILL) {
        chartFillProperty().set(CHART_FILL);
    }
    public final ObjectProperty<Paint> chartFillProperty() {
        if (null == chartFill) {
            chartFill = new StyleableObjectProperty<Paint>(DEFAULT_CHART_FILL) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.CHART_FILL; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "chartFill"; }
            };
        }
        return chartFill;
    }

    public final Color getChartStroke() {
        return null == chartStroke ? DEFAULT_CHART_STROKE : chartStroke.get();
    }
    public final void setChartStroke(final Color CHART_STROKE) {
        chartStrokeProperty().set(CHART_STROKE);
    }
    public final ObjectProperty<Color> chartStrokeProperty() {
        if (null == chartStroke) {
            chartStroke = new StyleableObjectProperty<Color>(DEFAULT_CHART_STROKE) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.CHART_STROKE; }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "chartStroke"; }
            };
        }
        return chartStroke;
    }

    public final Color getZeroLineColor() {
        return null == zeroLineColor ? DEFAULT_ZERO_LINE_COLOR : zeroLineColor.get();
    }
    public final void setZeroLineColor(final Color ZERO_LINE_COLOR) {
        zeroLineColorProperty().set(ZERO_LINE_COLOR);
    }
    public final ObjectProperty<Color> zeroLineColorProperty() {
        if (null == zeroLineColor) {
            zeroLineColor = new StyleableObjectProperty<Color>(DEFAULT_ZERO_LINE_COLOR) {
                @Override public CssMetaData<? extends Styleable, Color> getCssMetaData() { return StyleableProperties.ZERO_LINE_COLOR; }
                @Override public Object getBean() { return SimpleRadarChart.this; }
                @Override public String getName() { return "zeroLineColor";}
            };
        }
        return zeroLineColor;
    }


    // ******************** Style related *************************************
    @Override protected Skin createDefaultSkin() {
        return new SimpleRadarChartSkin(this);
    }

    @Override public String getUserAgentStylesheet() {
        return getClass().getResource("/eu/hansolo/enzo/charts/simpleradarchart.css").toExternalForm();
    }

    private static class StyleableProperties {
        private static final CssMetaData<SimpleRadarChart, Color> CHART_BACKGROUND =
            new CssMetaData<SimpleRadarChart, Color>("-chart-background", ColorConverter.getInstance(), DEFAULT_CHART_BACKGROUND) {

                @Override public boolean isSettable(SimpleRadarChart chart) {
                    return null == chart.chartBackground || !chart.chartBackground.isBound();
                }

                @Override public StyleableProperty<Color> getStyleableProperty(SimpleRadarChart chart) {
                    return (StyleableProperty) chart.chartBackgroundProperty();
                }

                @Override public Color getInitialValue(SimpleRadarChart chart) {
                    return chart.getChartBackground();
                }
            };

        private static final CssMetaData<SimpleRadarChart, Color> CHART_FOREGROUND =
            new CssMetaData<SimpleRadarChart, Color>("-chart-foreground", ColorConverter.getInstance(), DEFAULT_CHART_FOREGROUND) {

                @Override public boolean isSettable(SimpleRadarChart chart) {
                    return null == chart.chartForeground || !chart.chartForeground.isBound();
                }

                @Override public StyleableProperty<Color> getStyleableProperty(SimpleRadarChart chart) {
                    return (StyleableProperty) chart.chartForegroundProperty();
                }

                @Override public Color getInitialValue(SimpleRadarChart chart) {
                    return chart.getChartForeground();
                }
            };

        private static final CssMetaData<SimpleRadarChart, Color> CHART_TEXT =
            new CssMetaData<SimpleRadarChart, Color>("-chart-text", ColorConverter.getInstance(), DEFAULT_CHART_TEXT) {

                @Override public boolean isSettable(SimpleRadarChart chart) {
                    return null == chart.chartText || !chart.chartText.isBound();
                }

                @Override public StyleableProperty<Color> getStyleableProperty(SimpleRadarChart chart) {
                    return (StyleableProperty) chart.chartTextProperty();
                }

                @Override public Color getInitialValue(SimpleRadarChart chart) {
                    return chart.getChartText();
                }
            };

        private static final CssMetaData<SimpleRadarChart, Paint> CHART_FILL =
            new CssMetaData<SimpleRadarChart, Paint>("-chart-fill", PaintConverter.getInstance(), DEFAULT_CHART_FILL) {

                @Override public boolean isSettable(SimpleRadarChart chart) {
                    return null == chart.chartFill || !chart.chartFill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(SimpleRadarChart chart) {
                    return (StyleableProperty<Paint>) chart.chartFillProperty();
                }

                @Override public Paint getInitialValue(SimpleRadarChart chart) {
                    return chart.getChartFill();
                }
            };

        private static final CssMetaData<SimpleRadarChart, Color> CHART_STROKE = new CssMetaData<SimpleRadarChart, Color>("-chart-stroke", ColorConverter.getInstance(), DEFAULT_CHART_STROKE) {
            @Override public boolean isSettable(SimpleRadarChart chart) {
                return null == chart.chartStroke || chart.chartStroke.isBound();
            }

            @Override public StyleableProperty<Color> getStyleableProperty(SimpleRadarChart simpleRadarChart) {
                return (StyleableProperty) simpleRadarChart.chartStrokeProperty();
            }

            @Override public Color getInitialValue(SimpleRadarChart chart) {
                return chart.getChartStroke();
            }
        };

        private static final CssMetaData<SimpleRadarChart, Color> ZERO_LINE_COLOR =
            new CssMetaData<SimpleRadarChart, Color>("-zero-line-color", ColorConverter.getInstance(), DEFAULT_ZERO_LINE_COLOR) {

                @Override public boolean isSettable(SimpleRadarChart chart) {
                    return null == chart.zeroLineColor || !chart.zeroLineColor.isBound();
                }

                @Override public StyleableProperty<Color> getStyleableProperty(SimpleRadarChart chart) {
                    return (StyleableProperty) chart.zeroLineColorProperty();
                }

                @Override public Color getInitialValue(SimpleRadarChart chart) {
                    return chart.getZeroLineColor();
                }
            };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables,
                               CHART_BACKGROUND,
                               CHART_FOREGROUND,
                               CHART_TEXT,
                               CHART_FILL,
                               CHART_STROKE,
                               ZERO_LINE_COLOR);
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
