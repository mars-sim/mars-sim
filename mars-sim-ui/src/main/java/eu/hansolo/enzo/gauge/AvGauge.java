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
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.StyleableObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.util.Duration;
import eu.hansolo.enzo.fonts.Fonts;
import eu.hansolo.enzo.common.GradientLookup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


/**
 * User: hansolo
 * Date: 21.11.14
 * Time: 07:44
 */
public class AvGauge extends Region {
    private static final double   PREFERRED_WIDTH  = 300;
    private static final double   PREFERRED_HEIGHT = 300;
    private static final double   MINIMUM_WIDTH    = 50;
    private static final double   MINIMUM_HEIGHT   = 50;
    private static final double   MAXIMUM_WIDTH    = 1024;
    private static final double   MAXIMUM_HEIGHT   = 1024;

    private static final Color    DEFAULT_OUTER_BAR_COLOR        = Color.web("#a71037");
    private static final Color    DEFAULT_INNER_BAR_COLOR        = Color.web("#f06185");
    private static final Color    DEFAULT_BACKGROUND_COLOR       = Color.web("#414141");
    private static final Color    DEFAULT_OUTER_VALUE_TEXT_COLOR = Color.web("#ffffff");
    private static final Color    DEFAULT_INNER_VALUE_TEXT_COLOR = Color.web("#ffffff");
    private static final Color    DEFAULT_TITLE_TEXT_COLOR       = Color.web("#ffffff");
    private static final Color    DEFAULT_BORDER_COLOR           = Color.web("#414141");
    private static final Color    DEFAULT_SEPARATOR_COLOR        = Color.web("#ffffff");

    private double                size;
    private double                width;
    private double                height;
    private Circle                border;
    private Arc                   outerBar;
    private Arc                   innerBar;
    private Line                  separator;
    private Circle                background;
    private Text                  outerValueText;
    private Text                  innerValueText;
    private Text                  titleText;
    private Pane                  pane;

    private double                range;
    private double                angleStep;
    private int                   decimals;
    private int                   animationDurationInMs;
    private boolean               multiColor;
    private DoubleProperty        minValue;
    private DoubleProperty        maxValue;
    private DoubleProperty        outerValue;
    private DoubleProperty        outerCurrentValue;
    private DoubleProperty        innerValue;
    private DoubleProperty        innerCurrentValue;
    private StringProperty        title;
    private ObjectProperty<Paint> outerBarColor;
    private ObjectProperty<Paint> innerBarColor;
    private ObjectProperty<Paint> backgroundColor;
    private ObjectProperty<Paint> outerValueTextColor;
    private ObjectProperty<Paint> innerValueTextColor;
    private ObjectProperty<Paint> titleTextColor;
    private ObjectProperty<Paint> borderColor;
    private ObjectProperty<Paint> separatorColor;
    private Timeline              outerBarTimeline;
    private Timeline              innerBarTimeline;
    private GradientLookup        outerGradientLookup;
    private GradientLookup        innerGradientLookup;


    // ******************** Constructors **************************************
    public AvGauge() {
        getStylesheets().add(AvGauge.class.getResource("/eu/hansolo/enzo/gauge/avgauge.css").toExternalForm());
        getStyleClass().add("av-gauge");

        range                   = 100d;
        angleStep               = 360d / range;
        decimals                = 0;
        animationDurationInMs   = 500;
        minValue                = new DoublePropertyBase(0) {
            @Override public void set(final double VALUE) {
                if (VALUE > maxValue.get()) maxValue.set(VALUE + 0.1);
                double v  = clamp(-Double.MAX_VALUE, maxValue.get() - 0.1, VALUE);
                super.set(v);
            }
            @Override public Object getBean() { return AvGauge.this;}
            @Override public String getName() { return "minValue"; }
        };
        maxValue                = new DoublePropertyBase(100) {
            @Override public void set(final double VALUE) {
                if (VALUE < minValue.get()) minValue.set(VALUE - 0.1);
                double v  = clamp(minValue.get() + 0.1, Double.MAX_VALUE, VALUE);
                //range     = v - minValue.get();
                //angleStep = 360d / (range + minValue.get());
                super.set(v);
            }
            @Override public Object getBean() { return AvGauge.this;}
            @Override public String getName() { return "maxBarValue"; }
        };
        outerValue              = new DoublePropertyBase(0) {
            @Override public void set(final double VALUE) { super.set(clamp(minValue.get(), maxValue.get(), VALUE));}
            @Override public Object getBean() { return AvGauge.this;}
            @Override public String getName() { return "outerValue"; }
        };
        outerCurrentValue       = new SimpleDoubleProperty(this, "outerCurrentValue", 0d);
        innerValue              = new DoublePropertyBase(0) {
            @Override public void set(final double VALUE) { super.set(clamp(minValue.get(), maxValue.get(), VALUE)); }
            @Override public Object getBean() { return AvGauge.this;}
            @Override public String getName() { return "innerValue"; }
        };
        innerCurrentValue       = new SimpleDoubleProperty(this, "innerCurrentValue", 0d);
        title                   = new SimpleStringProperty(this, "title", "");
        outerBarColor           = new StyleableObjectProperty<Paint>(DEFAULT_OUTER_BAR_COLOR) {
            @Override public CssMetaData getCssMetaData() { return StyleableProperties.OUTER_BAR_COLOR; }
            @Override public Object getBean() { return AvGauge.this; }
            @Override public String getName() { return "outerBarColor"; }
        };
        innerBarColor           = new StyleableObjectProperty<Paint>(DEFAULT_INNER_BAR_COLOR) {
            @Override public CssMetaData getCssMetaData() { return StyleableProperties.INNER_BAR_COLOR; }
            @Override public Object getBean() { return AvGauge.this; }
            @Override public String getName() { return "innerBarColor"; }
        };
        backgroundColor         = new StyleableObjectProperty<Paint>(DEFAULT_BACKGROUND_COLOR) {
            @Override public CssMetaData getCssMetaData() { return StyleableProperties.BACKGROUND_COLOR; }
            @Override public Object getBean() { return AvGauge.this; }
            @Override public String getName() { return "backgroundColor"; }
        };
        outerValueTextColor     = new StyleableObjectProperty<Paint>(DEFAULT_OUTER_VALUE_TEXT_COLOR) {
            @Override public CssMetaData getCssMetaData() { return StyleableProperties.OUTER_VALUE_TEXT_COLOR; }
            @Override public Object getBean() { return AvGauge.this; }
            @Override public String getName() { return "outerValueTextColor"; }
        };
        innerValueTextColor     = new StyleableObjectProperty<Paint>(DEFAULT_INNER_VALUE_TEXT_COLOR) {
            @Override public CssMetaData getCssMetaData() { return StyleableProperties.INNER_VALUE_TEXT_COLOR; }
            @Override public Object getBean() { return AvGauge.this; }
            @Override public String getName() { return "innerValueTextColor"; }
        };
        titleTextColor          = new StyleableObjectProperty<Paint>(DEFAULT_TITLE_TEXT_COLOR) {
            @Override public CssMetaData getCssMetaData() { return StyleableProperties.TITLE_TEXT_COLOR; }
            @Override public Object getBean() { return AvGauge.this; }
            @Override public String getName() { return "titleTextColor"; }
        };
        borderColor             = new StyleableObjectProperty<Paint>(DEFAULT_BORDER_COLOR) {
            @Override public CssMetaData getCssMetaData() { return StyleableProperties.BORDER_COLOR; }
            @Override public Object getBean() { return AvGauge.this; }
            @Override public String getName() { return "borderColor"; }
        };
        separatorColor          = new StyleableObjectProperty<Paint>(DEFAULT_SEPARATOR_COLOR) {
            @Override public CssMetaData getCssMetaData() { return StyleableProperties.SEPARATOR_COLOR; }
            @Override public Object getBean() { return AvGauge.this; }
            @Override public String getName() { return "separatorColor"; }
        };
        outerBarTimeline        = new Timeline();
        innerBarTimeline        = new Timeline();
        multiColor              = false;
        outerGradientLookup     = new GradientLookup(new Stop(0.00, Color.BLUE),
                                                     new Stop(0.20, Color.CYAN),
                                                     new Stop(0.40, Color.LIME),
                                                     new Stop(0.60, Color.YELLOW),
                                                     new Stop(0.80, Color.ORANGE),
                                                     new Stop(1.00, Color.RED));
        innerGradientLookup     = new GradientLookup(new Stop(0.00, Color.BLUE),
                                                     new Stop(0.20, Color.CYAN),
                                                     new Stop(0.40, Color.LIME),
                                                     new Stop(0.60, Color.YELLOW),
                                                     new Stop(0.80, Color.ORANGE),
                                                     new Stop(1.00, Color.RED));
        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getWidth(), 0.0) <= 0 || Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        if (Double.compare(getMinWidth(), 0.0) <= 0 || Double.compare(getMinHeight(), 0.0) <= 0) {
            setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }

        if (Double.compare(getMaxWidth(), 0.0) <= 0 || Double.compare(getMaxHeight(), 0.0) <= 0) {
            setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }
    }

    private void initGraphics() {
        border = new Circle(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.5);
        border.setFill(Color.TRANSPARENT);
        border.setStroke(DEFAULT_BORDER_COLOR);

        outerBar = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5,
                           PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5,
                           90, 0);
        outerBar.setType(ArcType.ROUND);
        outerBar.setFill(DEFAULT_OUTER_BAR_COLOR);

        innerBar = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.83333 * 0.5,
                           PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.83333 * 0.5,
                           90, 0);
        innerBar.setType(ArcType.ROUND);
        innerBar.setFill(DEFAULT_INNER_BAR_COLOR);

        separator = new Line(PREFERRED_WIDTH * 0.5, 1, PREFERRED_WIDTH * 0.5, 0.16667 * PREFERRED_HEIGHT);
        separator.setStroke(DEFAULT_SEPARATOR_COLOR);
        separator.setFill(Color.TRANSPARENT);

        background = new Circle(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.66667 * 0.5);
        background.setFill(DEFAULT_BACKGROUND_COLOR);

        outerValueText = new Text(String.format(Locale.US, "%.1f", getOuterValue()));
        outerValueText.setFont(Fonts.latoLight(PREFERRED_WIDTH * 0.27333));
        outerValueText.setFill(DEFAULT_OUTER_VALUE_TEXT_COLOR);

        innerValueText = new Text(String.format(Locale.US, "%.1f", getInnerValue()));
        innerValueText.setFont(Fonts.latoLight(PREFERRED_WIDTH * 0.08));
        innerValueText.setFill(DEFAULT_INNER_VALUE_TEXT_COLOR);

        titleText = new Text("");
        titleText.setFont(Fonts.latoLight(PREFERRED_WIDTH * 0.08));
        titleText.setFill(DEFAULT_TITLE_TEXT_COLOR);

        pane = new Pane();
        pane.getChildren().setAll(border, outerBar, innerBar, separator, background, outerValueText, innerValueText, titleText);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        outerValue.addListener(o -> handleControlPropertyChanged("OUTER_VALUE"));
        outerCurrentValue.addListener(o -> handleControlPropertyChanged("OUTER_CURRENT_VALUE"));
        innerValue.addListener(o -> handleControlPropertyChanged("INNER_VALUE"));
        innerCurrentValue.addListener(o -> handleControlPropertyChanged("INNER_CURRENT_VALUE"));
        title.addListener(o -> handleControlPropertyChanged("TITLE"));
        outerBarColor.addListener(o -> handleControlPropertyChanged("OUTER_BAR_COLOR"));
        innerBarColor.addListener(o -> handleControlPropertyChanged("INNER_BAR_COLOR"));
        backgroundColor.addListener(o -> handleControlPropertyChanged("BACKGROUND_COLOR"));
        outerValueTextColor.addListener(o -> handleControlPropertyChanged("OUTER_VALUE_TEXT_COLOR"));
        innerValueTextColor.addListener(o -> handleControlPropertyChanged("INNER_VALUE_TEXT_COLOR"));
        titleTextColor.addListener(o -> handleControlPropertyChanged("TEXT_COLOR"));
        borderColor.addListener(o -> handleControlPropertyChanged("BORDER_COLOR"));
        separatorColor.addListener(o -> handleControlPropertyChanged("SEPARATOR_COLOR"));
    }


    // ******************** Methods *******************************************
    private void handleControlPropertyChanged(final String PROPERTY) {
        if ("OUTER_VALUE".equals(PROPERTY)) {
            outerBarTimeline.stop();
            range     = maxValue.get() - minValue.get();
            angleStep = 360d / range;
            outerCurrentValue.set(clamp(minValue.get(), maxValue.get(), outerCurrentValue.get()));
            final KeyValue KV_START = new KeyValue(outerCurrentValue, outerCurrentValue.get(), Interpolator.EASE_BOTH);
            final KeyValue KV_STOP  = new KeyValue(outerCurrentValue, outerValue.get(), Interpolator.EASE_BOTH);
            final KeyFrame KF_START = new KeyFrame(Duration.ZERO, KV_START);
            final KeyFrame KF_STOP  = new KeyFrame(Duration.millis(animationDurationInMs), KV_STOP);
            outerBarTimeline.getKeyFrames().setAll(KF_START, KF_STOP);
            outerBarTimeline.play();
        } else if ("INNER_VALUE".equals(PROPERTY)) {
            innerBarTimeline.stop();
            range     = maxValue.get() - minValue.get();
            angleStep = 360d / range;
            innerCurrentValue.set(clamp(minValue.get(), maxValue.get(), innerCurrentValue.get()));
            final KeyValue KV_START = new KeyValue(innerCurrentValue, innerCurrentValue.get(), Interpolator.EASE_BOTH);
            final KeyValue KV_STOP  = new KeyValue(innerCurrentValue, innerValue.get(), Interpolator.EASE_BOTH);
            final KeyFrame KF_START = new KeyFrame(Duration.ZERO, KV_START);
            final KeyFrame KF_STOP  = new KeyFrame(Duration.millis(animationDurationInMs), KV_STOP);
            innerBarTimeline.getKeyFrames().setAll(KF_START, KF_STOP);
            innerBarTimeline.play();
        } else if ("OUTER_CURRENT_VALUE".equals(PROPERTY)) {
            setOuterBar(outerCurrentValue.get());
        } else if ("INNER_CURRENT_VALUE".equals(PROPERTY)) {
            setInnerBar(innerCurrentValue.get());
        } else if ("TITLE".equals(PROPERTY)) {
            titleText.setText(title.get());
            adjustTitleTextSize();
        } else if ("OUTER_BAR_COLOR".equals(PROPERTY)) {
            outerBar.setFill(outerBarColor.get());
        } else if ("INNER_BAR_COLOR".equals(PROPERTY)) {
            innerBar.setFill(innerBarColor.get());
        } else if ("BACKGROUND_COLOR".equals(PROPERTY)) {
            background.setFill(backgroundColor.get());
        } else if ("OUTER_VALUE_TEXT_COLOR".equals(PROPERTY)) {
            outerValueText.setFill(outerValueTextColor.get());
        } else if ("INNER_VALUE_TEXT_COLOR".equals(PROPERTY)) {
            innerValueText.setFill(innerValueTextColor.get());
        } else if ("TEXT_COLOR".equals(PROPERTY)) {
            titleText.setFill(titleTextColor.get());
        } else if ("BORDER_COLOR".equals(PROPERTY)) {
            border.setStroke(borderColor.get());
        } else if ("SEPARATOR_COLOR".equals(PROPERTY)) {
            separator.setStroke(separatorColor.get());
        }
    }

    public double getMinValue() { return minValue.get(); }
    public void setMinValue(final double MIN_VALUE) { minValue.set(MIN_VALUE); }
    public DoubleProperty minValueProperty() { return minValue; }

    public double getMaxValue() { return maxValue.get(); }
    public void setMaxValue(final double MAX_VALUE) { maxValue.set(MAX_VALUE); }
    public DoubleProperty maxValueProperty() { return maxValue; }

    public double getOuterValue() { return outerValue.get(); }
    public void setOuterValue(final double OUTER_VALUE) { outerValue.set(OUTER_VALUE); }
    public DoubleProperty outerValueProperty() { return outerValue; }

    public ReadOnlyDoubleProperty outerCurrentValueProperty() { return outerCurrentValue; }

    public double getInnerValue() { return innerValue.get(); }
    public void setInnerValue(final double INNER_VALUE) { innerValue.set(INNER_VALUE); }
    public DoubleProperty innerValueProperty() { return innerValue; }

    public ReadOnlyDoubleProperty innerCurrentValueProperty() { return innerCurrentValue; }

    public String getTitle() { return title.get(); }
    public void setTitle(final String TITLE) { title.set(TITLE); }
    public StringProperty titleProperty() { return title; }

    public Paint getOuterBarColor() { return outerBarColor.get(); }
    public void setOuterBarColor(final Color OUTER_BAR_COLOR) {
        if (multiColor) return;
        outerBarColor.set(OUTER_BAR_COLOR);
    }
    public ObjectProperty<Paint> outerBarColorProperty() { return outerBarColor; }

    public Paint getInnerBarColor() { return innerBarColor.get(); }
    public void setInnerBarColor(final Color INNER_BAR_COLOR) {
        if (multiColor) return;
        innerBarColor.set(INNER_BAR_COLOR); }
    public ObjectProperty<Paint> innerBarColorProperty() { return innerBarColor; }

    public Paint getBackgroundColor() { return backgroundColor.get(); }
    public void setBackgroundColor(final Color BACKGROUND_COLOR) { backgroundColor.set(BACKGROUND_COLOR); }
    public ObjectProperty<Paint> backgroundColorProperty() { return backgroundColor; }

    public Paint getOuterValueTextColor() { return outerValueTextColor.get(); }
    public void setOuterValueTextColor(final Color OUTER_VALUE_TEXT_COLOR) { outerValueTextColor.set(OUTER_VALUE_TEXT_COLOR); }
    public ObjectProperty<Paint> outerValueTextColorProperty() { return outerValueTextColor; }

    public Paint getInnerValueTextColor() { return innerValueTextColor.get(); }
    public void setInnerValueTextColor(final Color INNER_VALUE_TEXT_COLOR) { innerValueTextColor.set(INNER_VALUE_TEXT_COLOR); }
    public ObjectProperty<Paint> innerValueTextColorProperty() { return innerValueTextColor; }

    public Paint getTitleTextColor() { return titleTextColor.get(); }
    public void setTitleTextColor(final Color TITLE_TEXT_COLOR) { titleTextColor.set(TITLE_TEXT_COLOR); }
    public ObjectProperty<Paint> titleTextColorProperty() { return titleTextColor; }

    public Paint getBorderColor() { return borderColor.get(); }
    public void setBorderColor(final Color BORDER_COLOR) { borderColor.set(BORDER_COLOR); }
    public ObjectProperty<Paint> borderColorProperty() { return borderColor; }

    public Paint getSeparatorColor() { return separatorColor.get(); }
    public void setSeparatorColor(final Color SEPARATOR_COLOR) { separatorColor.set(SEPARATOR_COLOR); }
    public ObjectProperty<Paint> separatorColorProperty() { return separatorColor; }

    public void setOuterGradient(final Stop... STOPS) { outerGradientLookup.setStops(STOPS); }

    public void setInnerGradient(final Stop... STOPS) { innerGradientLookup.setStops(STOPS); }

    public void setDecimals(final int DECIMALS) {
        decimals = clamp(0, 3, DECIMALS);
        adjustOuterValueTextSize();
        adjustInnerValueTextSize();
    }

    public int getAnimationDurationInMs() { return animationDurationInMs; }
    public void setAnimationDurationInMs(final int ANIMATION_DURATION_IN_MS) { animationDurationInMs = clamp(1, 10000, ANIMATION_DURATION_IN_MS); }

    public boolean isMultiColor()  { return multiColor; }
    public void setMultiColor(final boolean MULTI_COLOR) {
        multiColor = MULTI_COLOR;
        if (multiColor) return;

        outerBar.setFill(outerBarColor.get());
        innerBar.setFill(innerBarColor.get());
    }

    private void setOuterBar(final double VALUE) {
        if (multiColor) { outerBar.setFill(outerGradientLookup.getColorAt((VALUE - minValue.get()) / range)); }
        if (minValue.get() > 0) {
            outerBar.setLength(((VALUE - minValue.get()) * (-1)) * angleStep);
        } else {
            if (VALUE < 0) {
                outerBar.setLength((-VALUE + minValue.get()) * angleStep);
            } else {
                outerBar.setLength(((minValue.get() - VALUE) * angleStep));
            }
        }
        outerValueText.setText(String.format(Locale.US, "%." + decimals + "f", VALUE));
        adjustOuterValueTextSize();
    }

    private void setInnerBar(final double VALUE) {
        if (multiColor) { innerBar.setFill(innerGradientLookup.getColorAt((VALUE - minValue.get()) / range)); }
        if (minValue.get() > 0) {
            innerBar.setLength(((VALUE - minValue.get()) * (-1)) * angleStep);
        } else {
            if (VALUE < 0) {
                innerBar.setLength((-VALUE + minValue.get()) * angleStep);
            } else {
                innerBar.setLength(((minValue.get() - VALUE) * angleStep));
            }
        }
        innerValueText.setText(String.format(Locale.US, "%." + decimals + "f", VALUE));
        adjustInnerValueTextSize();
    }

    private void adjustOuterValueTextSize() {
        outerValueText.setFont(Fonts.latoLight(size * 0.23677));
        double decrement = 0d;
        while (outerValueText.getLayoutBounds().getWidth() > 0.5 * size && outerValueText.getFont().getSize() > 0) {
            outerValueText.setFont(Fonts.latoLight(size * (0.23677 - decrement)));
            decrement += 0.01;
        }
        outerValueText.relocate((size - outerValueText.getLayoutBounds().getWidth()) * 0.5, size * 0.23667);
    }
    private void adjustInnerValueTextSize() {
        innerValueText.setFont(Fonts.latoLight(size * 0.08));
        double decrement = 0d;
        while (innerValueText.getLayoutBounds().getWidth() > 0.41333 * size && innerValueText.getFont().getSize() > 0) {
            innerValueText.setFont(Fonts.latoLight(size * (0.08 - decrement)));
            decrement += 0.01;
        }
        innerValueText.relocate((size - innerValueText.getLayoutBounds().getWidth()) * 0.5, size * 0.66667);
    }
    private void adjustTitleTextSize() {
        titleText.setFont(Fonts.latoLight(size * 0.08));
        double decrement = 0d;
        while (titleText.getLayoutBounds().getWidth() > 0.56667 * size && titleText.getFont().getSize() > 0) {
            titleText.setFont(Fonts.latoLight(size * (0.08 - decrement)));
            decrement += 0.01;
        }
        titleText.relocate((size - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.55333);
    }

    // ******************** Utility methods ***********************************
    private String colorToCss(final Color COLOR) { return COLOR.toString().replace("0x", "#"); }

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


    // ******************** CSS Meta Data *************************************
    private static class StyleableProperties {
        private static final CssMetaData<AvGauge, Paint> OUTER_BAR_COLOR =
            new CssMetaData<AvGauge, Paint>("-outer-bar-color", PaintConverter.getInstance(), DEFAULT_OUTER_BAR_COLOR) {
                @Override public boolean isSettable(AvGauge node) {
                    return null == node.outerBarColor || !node.outerBarColor.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(AvGauge node) {
                    return (StyleableProperty) node.outerBarColorProperty();
                }

                @Override public Paint getInitialValue(AvGauge node) { return node.getOuterBarColor();}
            };

        private static final CssMetaData<AvGauge, Paint> INNER_BAR_COLOR =
            new CssMetaData<AvGauge, Paint>("-inner-bar-color", PaintConverter.getInstance(), DEFAULT_INNER_BAR_COLOR) {
                @Override public boolean isSettable(AvGauge node) {
                    return null == node.innerBarColor || !node.innerBarColor.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(AvGauge node) {
                    return (StyleableProperty) node.innerBarColorProperty();
                }

                @Override public Paint getInitialValue(AvGauge node) { return node.getInnerBarColor();}
            };

        private static final CssMetaData<AvGauge, Paint> BACKGROUND_COLOR =
            new CssMetaData<AvGauge, Paint>("-background-color", PaintConverter.getInstance(),
                                            DEFAULT_BACKGROUND_COLOR) {
                @Override public boolean isSettable(AvGauge node) {
                    return null == node.backgroundColor || !node.backgroundColor.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(AvGauge node) {
                    return (StyleableProperty) node.backgroundColorProperty();
                }

                @Override public Paint getInitialValue(AvGauge node) { return node.getBackgroundColor();}
            };

        private static final CssMetaData<AvGauge, Paint> OUTER_VALUE_TEXT_COLOR =
            new CssMetaData<AvGauge, Paint>("-outer-value-text-color", PaintConverter.getInstance(),
                                            DEFAULT_OUTER_VALUE_TEXT_COLOR) {
                @Override public boolean isSettable(AvGauge node) {
                    return null == node.outerValueTextColor || !node.outerValueTextColor.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(AvGauge node) {
                    return (StyleableProperty) node.outerValueTextColorProperty();
                }

                @Override public Paint getInitialValue(AvGauge node) { return node.getOuterValueTextColor();}
            };

        private static final CssMetaData<AvGauge, Paint> INNER_VALUE_TEXT_COLOR =
            new CssMetaData<AvGauge, Paint>("-inner-value-text-color", PaintConverter.getInstance(),
                                            DEFAULT_INNER_VALUE_TEXT_COLOR) {
                @Override public boolean isSettable(AvGauge node) {
                    return null == node.innerValueTextColor || !node.innerValueTextColor.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(AvGauge node) {
                    return (StyleableProperty) node.innerValueTextColorProperty();
                }

                @Override public Paint getInitialValue(AvGauge node) { return node.getInnerValueTextColor();}
            };

        private static final CssMetaData<AvGauge, Paint> TITLE_TEXT_COLOR =
            new CssMetaData<AvGauge, Paint>("-title-text-color", PaintConverter.getInstance(),
                                            DEFAULT_TITLE_TEXT_COLOR) {
                @Override public boolean isSettable(AvGauge node) {
                    return null == node.titleTextColor || !node.titleTextColor.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(AvGauge node) {
                    return (StyleableProperty) node.titleTextColorProperty();
                }

                @Override public Paint getInitialValue(AvGauge node) { return node.getTitleTextColor();}
            };

        private static final CssMetaData<AvGauge, Paint> BORDER_COLOR =
            new CssMetaData<AvGauge, Paint>("-border-color", PaintConverter.getInstance(),
                                            DEFAULT_BORDER_COLOR) {
                @Override public boolean isSettable(AvGauge node) {
                    return null == node.borderColor || !node.borderColor.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(AvGauge node) {
                    return (StyleableProperty) node.borderColorProperty();
                }

                @Override public Paint getInitialValue(AvGauge node) { return node.getBorderColor();}
            };

        private static final CssMetaData<AvGauge, Paint> SEPARATOR_COLOR =
            new CssMetaData<AvGauge, Paint>("-separator-color", PaintConverter.getInstance(), DEFAULT_SEPARATOR_COLOR) {
                @Override public boolean isSettable(AvGauge node) {
                    return null == node.separatorColor || !node.separatorColor.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(AvGauge node) {
                    return (StyleableProperty) node.separatorColorProperty();
                }

                @Override public Paint getInitialValue(AvGauge node) { return node.getSeparatorColor();}
            };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables,
                               OUTER_BAR_COLOR,
                               INNER_BAR_COLOR,
                               BACKGROUND_COLOR,
                               OUTER_VALUE_TEXT_COLOR,
                               INNER_VALUE_TEXT_COLOR,
                               TITLE_TEXT_COLOR,
                               BORDER_COLOR,
                               SEPARATOR_COLOR);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }


    // ******************** Resizing ******************************************
    private void resize() {
        width = getWidth();
        height = getHeight();
        size = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((width - size) * 0.5, (height - size) * 0.5);

            border.setCenterX(size * 0.5);
            border.setCenterY(size * 0.5);
            border.setRadius(size * 0.5);

            outerBar.setCenterX(size * 0.5);
            outerBar.setCenterY(size * 0.5);
            outerBar.setRadiusX(size * 0.5);
            outerBar.setRadiusY(size * 0.5);

            innerBar.setCenterX(size * 0.5);
            innerBar.setCenterY(size * 0.5);
            innerBar.setRadiusX(size * 0.83333 * 0.5);
            innerBar.setRadiusY(size * 0.83333 * 0.5);

            separator.setStartX(size * 0.5);
            separator.setStartY(1);
            separator.setEndX(size * 0.5);
            separator.setEndY(size * 0.16667);

            background.setCenterX(size * 0.5);
            background.setCenterY(size * 0.5);
            background.setRadius(size * 0.66667 * 0.5);

            adjustOuterValueTextSize();
            adjustInnerValueTextSize();
            adjustTitleTextSize();
        }
    }
}
