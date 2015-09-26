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

package eu.hansolo.enzo.gauge.skin;

import eu.hansolo.enzo.fonts.Fonts;
import eu.hansolo.enzo.gauge.OneEightyGauge;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.VPos;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.time.Instant;
import java.util.Locale;


/**
 * User: hansolo
 * Date: 07.01.14
 * Time: 06:51
 */
public class OneEightyGaugeSkin extends SkinBase<OneEightyGauge> implements Skin<OneEightyGauge> {
    private static final double PREFERRED_WIDTH  = 540;
    private static final double PREFERRED_HEIGHT = 400;
    private static final double MINIMUM_WIDTH    = 5;
    private static final double MINIMUM_HEIGHT   = 5;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private static final double ASPECT_RATIO     = PREFERRED_HEIGHT / PREFERRED_WIDTH;
    private double              size;
    private double              width;
    private double              height;
    private double              centerX;
    private DoubleProperty      currentValue;
    private double              range;
    private double              angleStep;
    private double              currentValueAngle;
    private Pane                pane;
    private Text                unitText;
    private Text                titleText;
    private Text                valueText;
    private Text                minText;
    private Text                maxText;
    private Path                barBackground;
    private MoveTo              barBackgroundStart;
    private ArcTo               barBackgroundOuterArc;
    private LineTo              barBackgroundLineToInnerArc;
    private ArcTo               barBackgroundInnerArc;
    private Path                dataBar;
    private MoveTo              dataBarStart;
    private ArcTo               dataBarOuterArc;
    private LineTo              dataBarLineToInnerArc;
    private ArcTo               dataBarInnerArc;
    private InnerShadow         innerShadow;
    private Font                smallFont;
    private Font                bigFont;
    private Timeline            timeline;
    private Instant             lastCall;
    private boolean             withinSpeedLimit;


    // ******************** Constructors **************************************
    public OneEightyGaugeSkin(OneEightyGauge gauge) {
        super(gauge);
        currentValue      = new SimpleDoubleProperty(this, "currentValue", getSkinnable().getValue());
        range             = getSkinnable().getMaxValue() - getSkinnable().getMinValue();
        angleStep         = 180d / range;
        currentValueAngle = 0;
        lastCall          = Instant.now();
        withinSpeedLimit  = true;
        timeline          = new Timeline();

        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getSkinnable().getPrefWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getPrefHeight(),
                                                                                      0.0) <= 0 ||
            Double.compare(getSkinnable().getWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getHeight(),
                                                                                  0.0) <= 0) {
            if (getSkinnable().getPrefWidth() > 0 && getSkinnable().getPrefHeight() > 0) {
                getSkinnable().setPrefSize(getSkinnable().getPrefWidth(), getSkinnable().getPrefHeight());
            } else {
                getSkinnable().setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        if (Double.compare(getSkinnable().getMinWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMinHeight(),
                                                                                     0.0) <= 0) {
            getSkinnable().setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }

        if (Double.compare(getSkinnable().getMaxWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMaxHeight(),
                                                                                     0.0) <= 0) {
            getSkinnable().setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }
    }

    private void initGraphics() {
        unitText = new Text(getSkinnable().getUnit());
        unitText.setTextOrigin(VPos.CENTER);
        unitText.getStyleClass().setAll("unit");
        unitText.setFill(getSkinnable().getUnitColor());

        titleText = new Text(getSkinnable().getTitle());
        titleText.setTextOrigin(VPos.CENTER);
        titleText.getStyleClass().setAll("title");
        titleText.setFill(getSkinnable().getTitleColor());

        valueText = new Text(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getValue()));
        valueText.setTextOrigin(VPos.CENTER);
        valueText.getStyleClass().setAll("value");
        valueText.setFill(getSkinnable().getValueColor());

        minText = new Text(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getMinValue()));
        minText.setTextOrigin(VPos.CENTER);
        minText.getStyleClass().setAll("min-text");
        minText.setFill(getSkinnable().getMinTextColor());

        maxText = new Text(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getMaxValue()));
        maxText.setTextOrigin(VPos.CENTER);
        maxText.getStyleClass().setAll("max-text");
        maxText.setFill(getSkinnable().getMaxTextColor());

        innerShadow = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.3), 30d, 0d, 0d, 10d);

        barBackgroundStart          = new MoveTo(0, 0.675 * PREFERRED_HEIGHT);
        barBackgroundOuterArc       = new ArcTo(0.675 * PREFERRED_HEIGHT, 0.675 * PREFERRED_HEIGHT, 0, PREFERRED_WIDTH, 0.675 * PREFERRED_HEIGHT, true, true);
        barBackgroundLineToInnerArc = new LineTo(0.72222 * PREFERRED_WIDTH, 0.675 * PREFERRED_HEIGHT);
        barBackgroundInnerArc       = new ArcTo(0.3 * PREFERRED_HEIGHT, 0.3 * PREFERRED_HEIGHT, 0, 0.27778 * PREFERRED_WIDTH, 0.675 * PREFERRED_HEIGHT, false, false);

        barBackground = new Path();
        barBackground.setFillRule(FillRule.EVEN_ODD);
        barBackground.getElements().add(barBackgroundStart);
        barBackground.getElements().add(barBackgroundOuterArc);
        barBackground.getElements().add(barBackgroundLineToInnerArc);
        barBackground.getElements().add(barBackgroundInnerArc);
        barBackground.getElements().add(new ClosePath());
        barBackground.getStyleClass().setAll("bar-background");
        barBackground.setFill(getSkinnable().getBarBackgroundColor());
        barBackground.setEffect(getSkinnable().isShadowsEnabled() ? innerShadow : null);        

        dataBarStart          = new MoveTo(0, 0.675 * PREFERRED_HEIGHT);
        dataBarOuterArc       = new ArcTo(0.675 * PREFERRED_HEIGHT, 0.675 * PREFERRED_HEIGHT, 0, 0, 0, false, true);
        dataBarLineToInnerArc = new LineTo(0.27778 * PREFERRED_WIDTH, 0.675 * PREFERRED_HEIGHT);
        dataBarInnerArc       = new ArcTo(0.3 * PREFERRED_HEIGHT, 0.3 * PREFERRED_HEIGHT, 0, 0, 0, false, false);

        dataBar = new Path();
        dataBar.setFillRule(FillRule.EVEN_ODD);
        dataBar.getElements().add(dataBarStart);
        dataBar.getElements().add(dataBarOuterArc);
        dataBar.getElements().add(dataBarLineToInnerArc);
        dataBar.getElements().add(dataBarInnerArc);
        dataBar.getElements().add(new ClosePath());
        dataBar.getStyleClass().setAll("data-bar");
        dataBar.setFill(getSkinnable().getBarColor());
        dataBar.setEffect(getSkinnable().isShadowsEnabled() ? innerShadow : null);

        pane = new Pane();
        pane.getChildren().setAll(unitText,
                                  titleText,
                                  valueText,
                                  minText,
                                  maxText,
                                  barBackground,
                                  dataBar);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(observable -> resize());
        getSkinnable().heightProperty().addListener(observable -> resize());       
        getSkinnable().minValueProperty().addListener(observable -> handleControlPropertyChanged("RECALC"));
        getSkinnable().maxValueProperty().addListener(observable -> handleControlPropertyChanged("RECALC"));
        getSkinnable().titleProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().titleProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().unitProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().barBackgroundColorProperty().addListener(observable -> handleControlPropertyChanged("BAR_BACKGROUND_COLOR"));
        getSkinnable().barColorProperty().addListener(observable -> handleControlPropertyChanged("BAR_COLOR"));
        getSkinnable().dynamicBarColorProperty().addListener(observable1 -> handleControlPropertyChanged("BAR_COLOR"));
        getSkinnable().titleColorProperty().addListener(observable -> handleControlPropertyChanged("TITLE_COLOR"));
        getSkinnable().valueColorProperty().addListener(observable -> handleControlPropertyChanged("VALUE_COLOR"));
        getSkinnable().unitColorProperty().addListener(observable -> handleControlPropertyChanged("UNIT_COLOR"));
        getSkinnable().minTextColorProperty().addListener(observable -> handleControlPropertyChanged("MIN_TEXT_COLOR"));
        getSkinnable().maxTextColorProperty().addListener(observable -> handleControlPropertyChanged("MAX_TEXT_COLOR"));
        getSkinnable().shadowsEnabledProperty().addListener(observable -> handleControlPropertyChanged("SHADOWS_ENABLED"));
        currentValue.addListener(observable -> handleControlPropertyChanged("CURRENT_VALUE"));
        getSkinnable().valueProperty().addListener((OV, OLD_VALUE, NEW_VALUE) -> {
            withinSpeedLimit = !(Instant.now().minusMillis((long) getSkinnable().getAnimationDuration()).isBefore(lastCall));
            lastCall         = Instant.now();
            if (withinSpeedLimit && getSkinnable().isAnimated()) {
                timeline.stop();
                final KeyValue KEY_VALUE = new KeyValue(currentValue, getSkinnable().getValue(), Interpolator.SPLINE(0.5, 0.4, 0.4, 1.0));
                final KeyFrame KEY_FRAME = new KeyFrame(Duration.millis(getSkinnable().getAnimationDuration()), KEY_VALUE);
                timeline.getKeyFrames().setAll(KEY_FRAME);
                timeline.play();
            } else {
                currentValue.set(getSkinnable().getValue());
            }
        });
    }


    // ******************** Methods *******************************************
    protected void handleControlPropertyChanged(final String PROPERTY) {
        if ("RESIZE".equals(PROPERTY)) {
            resize();
        } else if ("RECALC".equals(PROPERTY)) {
            range     = getSkinnable().getMaxValue() - getSkinnable().getMinValue();
            angleStep = 180d / range;
            currentValue.set(getSkinnable().getMinValue());
            resize();
        } else if ("CURRENT_VALUE".equals(PROPERTY)) {
            updateBar();
        } else if ("ANGLE".equals(PROPERTY)) {
            double currentValue = dataBarOuterArc.getXAxisRotation() / angleStep + getSkinnable().getMinValue();
            valueText.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", currentValue));
            valueText.setTranslateX((size - valueText.getLayoutBounds().getWidth()) * 0.5);
        } else if ("BAR_BACKGROUND_COLOR".equals(PROPERTY)) {            
            barBackground.setFill(getSkinnable().getBarBackgroundColor());
        } else if ("BAR_COLOR".equals(PROPERTY)) {
            if (getSkinnable().isDynamicBarColor() && !getSkinnable().getGradientLookup().getStops().isEmpty()) {
                dataBar.setFill(getSkinnable().getGradientLookup().getColorAt(currentValue.get() / range));
            } else {
                dataBar.setFill(getSkinnable().getBarColor());
            }
        } else if ("TITLE_COLOR".equals(PROPERTY)) {
            titleText.setFill(getSkinnable().getTitleColor());
        } else if ("VALUE_COLOR".equals(PROPERTY)) {
            valueText.setFill(getSkinnable().getValueColor());
        } else if ("UNIT_COLOR".equals(PROPERTY)) {
            unitText.setFill(getSkinnable().getUnitColor());
        } else if ("MIN_TEXT_COLOR".equals(PROPERTY)) {
            minText.setFill(getSkinnable().getMinTextColor());
        } else if ("MAX_TEXT_COLOR".equals(PROPERTY)) {
            maxText.setFill(getSkinnable().getMaxTextColor());
        } else if ("SHADOWS_ENABLED".equals(PROPERTY)) {
            barBackground.setEffect(getSkinnable().isShadowsEnabled() ? innerShadow : null);
            dataBar.setEffect(getSkinnable().isShadowsEnabled() ? innerShadow : null);
        }
    }
   

    // ******************** Private Methods ***********************************
    private void updateBar() {
        currentValueAngle = (currentValue.get() + Math.abs(getSkinnable().getMinValue())) * angleStep + 90;
        dataBarOuterArc.setX(centerX + (0.675 * height) * Math.sin(-Math.toRadians(currentValueAngle)));
        dataBarOuterArc.setY(centerX + (0.675 * height) * Math.cos(-Math.toRadians(currentValueAngle)));
        dataBarLineToInnerArc.setX(centerX + (0.3 * height) * Math.sin(-Math.toRadians(currentValueAngle)));
        dataBarLineToInnerArc.setY(centerX + (0.3 * height) * Math.cos(-Math.toRadians(currentValueAngle)));
        if (getSkinnable().isDynamicBarColor() && getSkinnable().getGradientLookup().getStops().size() > 1) dataBar.setFill(getSkinnable().getGradientLookup().getColorAt(currentValue.get() / range));
        valueText.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", currentValue.get()));
        valueText.relocate((width - valueText.getLayoutBounds().getWidth()) * 0.5, 0.62 * height);
    }

    private void resize() {
        size   = getSkinnable().getWidth() < getSkinnable().getHeight() ? getSkinnable().getWidth() : getSkinnable().getHeight();
        width  = getSkinnable().getWidth();
        height = getSkinnable().getHeight();

        if (ASPECT_RATIO * width > height) {
            width  = 1 / (ASPECT_RATIO / height);
        } else if (1 / (ASPECT_RATIO / height) > width) {
            height = ASPECT_RATIO * width;
        }

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.relocate((getSkinnable().getWidth() - width) * 0.5, (getSkinnable().getHeight() - height) * 0.5);
            
            centerX   = width * 0.5;
            smallFont = Fonts.robotoThin(0.12 * height);
            bigFont   = Fonts.robotoRegular(0.24 * height);

            unitText.setFont(smallFont);
            unitText.relocate((width - unitText.getLayoutBounds().getWidth()) * 0.5, 0.5 * height);

            titleText.setFont(smallFont);
            titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, 0.87 * height);

            valueText.setFont(bigFont);
            valueText.relocate((width - valueText.getLayoutBounds().getWidth()) * 0.5, 0.62 * height);

            minText.setFont(smallFont);
            minText.relocate(((0.27778 * width) - minText.getLayoutBounds().getWidth()) * 0.5, 0.7 * height);

            maxText.setFont(smallFont);
            maxText.relocate(((0.27778 * width) - maxText.getLayoutBounds().getWidth()) * 0.5 + 0.72222 * width, 0.7 * height);

            if (getSkinnable().isShadowsEnabled()) {
                innerShadow.setRadius(0.075 * height);
                innerShadow.setOffsetY(0.025 * height);
            }

            barBackgroundStart.setX(0);
            barBackgroundStart.setY(0.675 * height);
            barBackgroundOuterArc.setRadiusX(0.675 * height);
            barBackgroundOuterArc.setRadiusY(0.675 * height);
            barBackgroundOuterArc.setX(width);
            barBackgroundOuterArc.setY(0.675 * height);
            barBackgroundLineToInnerArc.setX(0.72222 * width);
            barBackgroundLineToInnerArc.setY(0.675 * height);
            barBackgroundInnerArc.setRadiusX(0.3 * height);
            barBackgroundInnerArc.setRadiusY(0.3 * height);
            barBackgroundInnerArc.setX(0.27778 * width);
            barBackgroundInnerArc.setY(0.675 * height);

            currentValueAngle = (currentValue.get() + Math.abs(getSkinnable().getMinValue())) * angleStep + 90;
            dataBarStart.setX(0);
            dataBarStart.setY(0.675 * height);
            dataBarOuterArc.setRadiusX(0.675 * height);
            dataBarOuterArc.setRadiusY(0.675 * height);
            dataBarOuterArc.setX(centerX + (0.675 * height) * Math.sin(-Math.toRadians(currentValueAngle)));
            dataBarOuterArc.setY(centerX + (0.675 * height) * Math.cos(-Math.toRadians(currentValueAngle)));
            dataBarLineToInnerArc.setX(centerX + (0.3 * height) * Math.sin(-Math.toRadians(currentValueAngle)));
            dataBarLineToInnerArc.setY(centerX + (0.3 * height) * Math.cos(-Math.toRadians(currentValueAngle)));
            dataBarInnerArc.setRadiusX(0.3 * height);
            dataBarInnerArc.setRadiusY(0.3 * height);
            dataBarInnerArc.setX(0.27778 * width);
            dataBarInnerArc.setY(0.675 * height);
        }
    }
}
