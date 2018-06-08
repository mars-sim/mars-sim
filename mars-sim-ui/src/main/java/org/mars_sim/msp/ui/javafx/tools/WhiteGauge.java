/*
 * Copyright (c) 2016 by Gerrit Grunwald
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

package org.mars_sim.msp.ui.javafx.tools;


/* need to enable medusa artifact in pom.xml
 
 
import eu.hansolo.medusa.Fonts;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;

import java.util.Locale;

// User: hansolo
// Date: 26.04.16
// Time: 18:44

public class WhiteGauge extends Region {
    private static final double PREFERRED_WIDTH  = 320;
    private static final double PREFERRED_HEIGHT = 320;
    private static final double MINIMUM_WIDTH    = 50;
    private static final double MINIMUM_HEIGHT   = 50;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private Gauge      model;
    private double     size;
    private double     center;
    private Arc        backgroundRing;
    private Arc        barBackground;
    private Arc        bar;
    private Text       valueText;
    private Text       unitText;
    private Pane       pane;
    private Paint      backgroundPaint;
    private Paint      borderPaint;
    private double     borderWidth;
    private DropShadow shadow;
    private DropShadow textShadow;


    // ******************** Constructors **************************************
    public WhiteGauge() {
        this(0, 0, 100, "");
    }
    public WhiteGauge(final String UNIT) {
        this(0, 0, 100, UNIT);
    }
    public WhiteGauge(final double VALUE, final double MIN_VALUE, final double MAX_VALUE, final String UNIT) {
        model = GaugeBuilder.create()
                            .animated(true)
                            .animationDuration(1000)
                            .angleRange(360)
                            .minValue(MIN_VALUE)
                            .maxValue(MAX_VALUE)
                            .barColor(Color.WHITE)
                            .value(VALUE)
                            .unit(UNIT)
                            .build();

        backgroundPaint = Color.TRANSPARENT;
        borderPaint     = Color.TRANSPARENT;
        borderWidth     = 0d;

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
        shadow     = new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 12, 0, 3, 3);
        textShadow = new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 4, 0, 2, 2);

        valueText = new Text(String.format(Locale.US, "%.0f", model.getValue()));
        valueText.setFill(Color.WHITE);
        valueText.setFont(Fonts.robotoBold(PREFERRED_WIDTH * 0.20625));
        valueText.setTextOrigin(VPos.CENTER);
        valueText.relocate(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.46875);
        valueText.setEffect(textShadow);

        unitText  = new Text(model.getUnit());
        unitText.setFill(Color.WHITE);
        unitText.setFont(Fonts.robotoBold(PREFERRED_WIDTH * 0.0875));
        unitText.setTextOrigin(VPos.CENTER);
        unitText.relocate(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.65625);
        unitText.setEffect(textShadow);

        backgroundRing = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5,
                                 PREFERRED_WIDTH * 0.43125, PREFERRED_HEIGHT * 0.43125,
                                 0, 360);
        backgroundRing.setFill(null);
        backgroundRing.setStroke(Color.rgb(255, 255, 255, 0.9));
        backgroundRing.setStrokeLineCap(StrokeLineCap.BUTT);
        backgroundRing.setStrokeWidth(PREFERRED_WIDTH * 0.1375);
        backgroundRing.setEffect(shadow);

        barBackground = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5,
                                PREFERRED_WIDTH * 0.43125, PREFERRED_HEIGHT * 0.43125,
                                0, 360);
        barBackground.setFill(null);
        barBackground.setStroke(Color.rgb(255, 255, 255, 0.4));
        barBackground.setStrokeLineCap(StrokeLineCap.BUTT);
        barBackground.setStrokeWidth(PREFERRED_WIDTH * 0.1375);

        bar = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5,
                      PREFERRED_WIDTH * 0.43125, PREFERRED_HEIGHT * 0.43125,
                      90, -model.getAngleStep() * model.getValue());
        bar.setFill(null);
        bar.setStroke(Color.WHITE);
        bar.setStrokeWidth(PREFERRED_WIDTH * 0.1375);
        bar.setStrokeLineCap(StrokeLineCap.BUTT);

        pane = new Pane(valueText, unitText, backgroundRing, barBackground, bar);
        pane.setBackground(new Background(new BackgroundFill(backgroundPaint, new CornerRadii(1024), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(borderPaint, BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(borderWidth))));

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        model.currentValueProperty().addListener(o -> handleControlPropertyChanged("VALUE"));
    }


    // ******************** Methods *******************************************
    private void handleControlPropertyChanged(final String PROPERTY) {
        if ("VALUE".equals(PROPERTY)) {
            bar.setLength(-model.getAngleStep() * model.getCurrentValue());
            valueText.setText(String.format(Locale.US, "%.0f", model.getCurrentValue()));
            valueText.relocate((size - valueText.getLayoutBounds().getWidth()) * 0.5, (size - valueText.getLayoutBounds().getHeight()) * 0.42);
        }
    }

    public Gauge getModel() { return model; }


    // ******************** Resizing ******************************************
    private void resize() {
        double width = getWidth() - getInsets().getLeft() - getInsets().getRight();
        double height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.setPrefSize(size, size);
            pane.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);

            shadow.setRadius(size * 0.06);
            shadow.setOffsetX(size * 0.02);
            shadow.setOffsetY(size * 0.02);

            textShadow.setRadius(size * 0.0125);
            textShadow.setOffsetX(size * 0.00625);
            textShadow.setOffsetY(size * 0.00625);

            center = size * 0.5;

            valueText.setFont(Fonts.robotoBold(size * 0.20625));

            unitText.setFont(Fonts.robotoBold(size * 0.0875));

            Arc outerRing = new Arc(size * 0.5, size * 0.5,
                                     size * 0.43125, size * 0.43125,
                                     0, 360);
            outerRing.setFill(null);
            outerRing.setStroke(Color.WHITE);
            outerRing.setStrokeLineCap(StrokeLineCap.BUTT);
            outerRing.setStrokeWidth(size * 0.3);

            Arc innerRing = new Arc(size * 0.5, size * 0.5,
                                    size * 0.43125, size * 0.43125,
                                    0, 360);
            innerRing.setFill(null);
            innerRing.setStroke(Color.WHITE);
            innerRing.setStrokeLineCap(StrokeLineCap.BUTT);
            innerRing.setStrokeWidth(size * 0.1375);

            Shape shape = Shape.subtract(outerRing, innerRing);

            backgroundRing.setCenterX(center);
            backgroundRing.setCenterY(center);
            backgroundRing.setRadiusX(size * 0.43125);
            backgroundRing.setRadiusY(size * 0.43125);
            backgroundRing.setStrokeWidth(size * 0.1375);
            backgroundRing.setClip(shape);

            barBackground.setCenterX(center);
            barBackground.setCenterY(center);
            barBackground.setRadiusX(size * 0.43125);
            barBackground.setRadiusY(size * 0.43125);
            barBackground.setStrokeWidth(size * 0.1375);

            bar.setCenterX(center);
            bar.setCenterY(center);
            bar.setRadiusX(size * 0.43125);
            bar.setRadiusY(size * 0.43125);
            bar.setStrokeWidth(size * 0.1375);

            redraw();
        }
    }

    private void redraw() {
        pane.setBackground(new Background(new BackgroundFill(backgroundPaint, new CornerRadii(1024), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(borderPaint, BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(borderWidth / PREFERRED_WIDTH * size))));

        valueText.setText(String.format(Locale.US, "%.0f", model.getCurrentValue()));
        valueText.relocate((size - valueText.getLayoutBounds().getWidth()) * 0.5, (size - valueText.getLayoutBounds().getHeight()) * 0.42);

        unitText.relocate((size - unitText.getLayoutBounds().getWidth()) * 0.5, (size - unitText.getLayoutBounds().getHeight()) * 0.66);

        bar.setLength(-model.getAngleStep() * model.getCurrentValue());
    }
}
*/