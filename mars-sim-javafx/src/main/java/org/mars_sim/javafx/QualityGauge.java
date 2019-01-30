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

package org.mars_sim.javafx;

import java.util.ArrayList;
import java.util.List;

import eu.hansolo.medusa.Fonts;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.Section;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
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
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
//import jfxtras.scene.layout.VBox;


/**
 * User: hansolo
 * Date: 20.07.16
 * Time: 06:52
 */
public class QualityGauge extends Region {
    private static final double    PREFERRED_WIDTH  = 616;
    private static final double    PREFERRED_HEIGHT = 323;
    private static final double    MINIMUM_WIDTH    = 61;
    private static final double    MINIMUM_HEIGHT   = 32;
    private static final double    MAXIMUM_WIDTH    = 1024;
    private static final double    MAXIMUM_HEIGHT   = 1024;
    private static final Section[] NORMAL_ORDER  = {
        new Section(0, 1, "1", Color.web("#87ec7a")),
        new Section(1, 2, "2", Color.web("#a0e17a")),
        new Section(2, 3, "3", Color.web("#b9d87a")),
        new Section(3, 4, "4", Color.web("#d2cf7a")),
        new Section(4, 5, "5", Color.web("#edc57a")),
        new Section(5, 6, "6", Color.web("#ffbb7a")),
        new Section(6, 7, "7", Color.web("#ffac7b")),
        new Section(7, 8, "8", Color.web("#ff9f7b")),
        new Section(8, 9, "9", Color.web("#ff917c")),
        new Section(9, 10, "10", Color.web("#ff837c"))
    };
    private static final Section[] REVERSE_ORDER = {
        new Section(0, 1, "1", Color.web("#ff837c")),
        new Section(1, 2, "2", Color.web("#ff917c")),
        new Section(2, 3, "3", Color.web("#ff9f7b")),
        new Section(3, 4, "4", Color.web("#ffac7b")),
        new Section(4, 5, "5", Color.web("#ffbb7a")),
        new Section(5, 6, "6", Color.web("#edc57a")),
        new Section(6, 7, "7", Color.web("#d2cf7a")),
        new Section(7, 8, "8", Color.web("#b9d87a")),
        new Section(8, 9, "9", Color.web("#a0e17a")),
        new Section(9, 10, "10", Color.web("#87ec7a"))
    };
    private static double          aspectRatio;

    private        double          width;
    private        double          height;
    private        double          centerX;
    private        double          centerY;
    private        double          sectionAngle;
    private        List<Path>      sections;
    private        MoveTo          moveTo;
    private        LineTo          lineTo1;
    private        CubicCurveTo    cubicCurveTo1;
    private        CubicCurveTo    cubicCurveTo2;
    private        CubicCurveTo    cubicCurveTo3;
    private        CubicCurveTo    cubicCurveTo4;
    private        LineTo          lineTo2;
    private        ClosePath       closePath;
    private        Path            currentQuality;
    private        Text            currentQualityText;
    private        Rotate          currentQualityRotate;
    private        Circle          knob;
    private        Pane            pane;
    private        Paint           backgroundPaint;
    private        Paint           borderPaint;
    private        double          borderWidth;
    private        Gauge           model;
    private        BooleanProperty reverseOrder;

    private			String			_title = "";
    private			StringProperty	title;    

    // ******************** Constructors **************************************
    public QualityGauge() {
        aspectRatio     = PREFERRED_HEIGHT / PREFERRED_WIDTH;
        backgroundPaint = Color.TRANSPARENT;
        borderPaint     = Color.TRANSPARENT;
        borderWidth     = 0d;
        model           = GaugeBuilder.create()
                                      .minValue(0)
                                      .maxValue(10)
                                      .startAngle(0)
                                      .angleRange(180)
                                      .sectionsVisible(true)
                                      .sections(NORMAL_ORDER)
                                      .build();
        reverseOrder = new BooleanPropertyBase(false) {
            @Override protected void invalidated() { model.setSections(get() ? REVERSE_ORDER : NORMAL_ORDER); }
            @Override public Object getBean() { return QualityGauge.this; }
            @Override public String getName() { return "reverseOrder"; }
        };
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
        int noOfSections = model.getSections().size();

        centerX      = PREFERRED_WIDTH * 0.5;
        centerY      = PREFERRED_HEIGHT * 0.94736842;
        sectionAngle = model.getAngleRange() / noOfSections;
        sections     = new ArrayList<>(noOfSections);

        double radius = PREFERRED_WIDTH * 0.43831169;
        double sinValue;
        double cosValue;
        for (int i = 0 ; i < noOfSections ; i++) {
            MoveTo moveTo = new MoveTo(centerX, centerY);

            sinValue = Math.sin(Math.toRadians(-sectionAngle * i + model.getStartAngle() + 270));
            cosValue = Math.cos(Math.toRadians(-sectionAngle * i + model.getStartAngle() + 270));
            LineTo lineTo1 = new LineTo(centerX + radius * sinValue, centerY + radius * cosValue);

            sinValue = Math.sin(Math.toRadians(-sectionAngle * (i + 1) + model.getStartAngle() + 270));
            cosValue = Math.cos(Math.toRadians(-sectionAngle * (i + 1) + model.getStartAngle() + 270));
            LineTo lineTo2 = new LineTo(centerX + radius * sinValue, centerY + radius * cosValue);

            ClosePath closePath = new ClosePath();

            sinValue = Math.sin(Math.toRadians(-sectionAngle * (i + 0.5) + model.getStartAngle() + 270));
            cosValue = Math.cos(Math.toRadians(-sectionAngle * (i + 0.5) + model.getStartAngle() + 270));
            LinearGradient secFill = new LinearGradient(centerX + radius * sinValue, centerY + radius * cosValue, centerX, centerY, false, CycleMethod.NO_CYCLE,
                                                        new Stop(0.0, model.getSections().get(i).getColor()),
                                                        new Stop(0.2, model.getSections().get(i).getColor()),
                                                        new Stop(0.2, model.getSections().get(i).getColor().deriveColor(0, 0.8, 1.1, 1)),
                                                        new Stop(1.0, model.getSections().get(i).getColor().deriveColor(0, 0.8, 1.1, 1)));

            Path sec = new Path(moveTo, lineTo1, lineTo2, closePath);
            sec.setFill(secFill);
            sec.setStroke(Color.WHITE);
            sec.setStrokeWidth(PREFERRED_HEIGHT * 0.01);

            sections.add(i, sec);
        }

        moveTo        = new MoveTo(42.5, 313);
        lineTo1       = new LineTo(84.7373, 46.3241);
        cubicCurveTo1 = new CubicCurveTo(84.7373, 46.3241, 85, 43.8221, 85, 42.5);
        cubicCurveTo2 = new CubicCurveTo(85, 19.0276, 65.9724, 0, 42.5, 0);
        cubicCurveTo3 = new CubicCurveTo(19.0276, 0, 0, 19.0276, 0, 42.5);
        cubicCurveTo4 = new CubicCurveTo(0, 43.8221, 0.2627, 46.3241, 0.2627, 46.3241);
        lineTo2       = new LineTo(42.5, 313);
        closePath     = new ClosePath();

        currentQualityRotate = new Rotate(279);
        currentQualityRotate.setPivotX(centerX);
        currentQualityRotate.setPivotY(centerY);

        currentQuality = new Path(moveTo, lineTo1, cubicCurveTo1, cubicCurveTo2, cubicCurveTo3, cubicCurveTo4, lineTo2, closePath);
        currentQuality.setStrokeWidth(PREFERRED_HEIGHT * 0.01);
        currentQuality.setStroke(Color.TRANSPARENT);
        currentQuality.setFill(Color.TRANSPARENT);
        currentQuality.setStrokeLineJoin(StrokeLineJoin.ROUND);
        currentQuality.getTransforms().add(currentQualityRotate);

        currentQualityText = new Text("");
        currentQualityText.setTextOrigin(VPos.CENTER);
        currentQualityText.setFont(Fonts.latoBold(PREFERRED_HEIGHT * 0.14860681));
        currentQualityText.setFill(Color.TRANSPARENT);

        knob = new Circle(centerX, centerY, PREFERRED_HEIGHT * 0.05572755);
        knob.setFill(Color.web("#cccccc"));
        knob.setStroke(Color.WHITE);
        knob.setStrokeWidth(PREFERRED_HEIGHT * 0.01);

        pane = new Pane();
        pane.getChildren().addAll(sections);
        pane.getChildren().addAll(currentQuality, currentQualityText, knob);
        pane.setBackground(new Background(new BackgroundFill(backgroundPaint, new CornerRadii(1024), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(borderPaint, BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(borderWidth))));

        //VBox box = new VBox();
        //Label label = new Label(_title);
        //box.getChildren().add(pane);
        
        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        model.valueProperty().addListener(o -> updateValue());
    }


    // ******************** Methods *******************************************
    private void updateValue() {
        double value = model.getValue();
        if (value == 0) {
            currentQuality.setFill(Color.TRANSPARENT);
            currentQuality.setStroke(Color.TRANSPARENT);
            currentQualityText.setFill(Color.TRANSPARENT);
            currentQualityText.setText("");
            knob.setFill(Color.web("#cccccc"));
        } else {
            currentQualityRotate.setAngle(279 + ((int) value) * sectionAngle - sectionAngle);
            double radius      = width * 0.45;
            double sinValue    = Math.sin(Math.toRadians(currentQualityRotate.getAngle() + (-sectionAngle * ((int) value + 0.5) + model.getStartAngle() + 270 + sectionAngle)));
            double cosValue    = Math.cos(Math.toRadians(currentQualityRotate.getAngle() + (-sectionAngle * ((int) value + 0.5) + model.getStartAngle() + 270 + sectionAngle)));
            Color sectionColor = model.getSections().get((int) value - 1).getColor().deriveColor(0, 2.5, 1, 1);
            LinearGradient currentQualityFill = new LinearGradient(centerX + radius * sinValue,
                                                                   centerY + radius * cosValue,
                                                                   centerX, centerY,
                                                                   false, CycleMethod.NO_CYCLE,
                                                                   new Stop(0.0, sectionColor),
                                                                   new Stop(0.22, sectionColor),
                                                                   new Stop(0.22, sectionColor.deriveColor(0, 0.7, 1.1, 1)),
                                                                   new Stop(1.0, sectionColor.deriveColor(0, 0.7, 1.1, 1)));

            currentQuality.setFill(currentQualityFill);
            currentQuality.setStroke(Color.WHITE);

            sinValue = Math.sin(Math.toRadians(-sectionAngle * ((int) value + 0.5) + model.getStartAngle() + 270 + sectionAngle));
            cosValue = Math.cos(Math.toRadians(-sectionAngle * ((int) value + 0.5) + model.getStartAngle() + 270 + sectionAngle));
            currentQualityText.setFont(Fonts.latoBold(height * 0.14860681));
            currentQualityText.setText(model.getSections().get((int) value - 1).getText());
            currentQualityText.setFill(Color.WHITE);
            currentQualityText.setX(centerX - (currentQualityText.getLayoutBounds().getWidth() * 0.55) + radius * sinValue);
            currentQualityText.setY(centerY + radius * cosValue);

            knob.setFill(sectionColor);
        }
    }

    public int getValue() { return model.valueProperty().intValue(); }
    public void setValue(final int VALUE) { model.setValue(VALUE); }
    public DoubleProperty valueProperty() { return model.valueProperty(); }

    public boolean isReverseOrder() { return reverseOrder.get(); }
    public void setReverseOrder(final boolean ORDER) { reverseOrder.set(ORDER); }
    public BooleanProperty reverseOrderProperty() { return reverseOrder; }


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();

        if (aspectRatio * width > height) {
            width = 1 / (aspectRatio / height);
        } else if (1 / (aspectRatio / height) > width) {
            height = aspectRatio * width;
        }

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);
            pane.relocate((getWidth() - width) * 0.5, (getHeight() - height) * 0.5);

            centerX = width * 0.5;
            centerY = height * 0.94736842;

            int    noOfSections = sections.size();
            double radius       = width * 0.43831169;
            double sinValue;
            double cosValue;
            Color  sectionColor;
            for (int i = 0 ; i < noOfSections ; i++) {
                sinValue     = Math.sin(Math.toRadians(-sectionAngle * (i + 0.5) + model.getStartAngle() + 270));
                cosValue     = Math.cos(Math.toRadians(-sectionAngle * (i + 0.5) + model.getStartAngle() + 270));
                sectionColor = model.getSections().get(i).getColor();
                LinearGradient secFill = new LinearGradient(centerX + radius * sinValue, centerY + radius * cosValue,
                                                            centerX, centerY,
                                                            false, CycleMethod.NO_CYCLE,
                                                            new Stop(0.0, sectionColor),
                                                            new Stop(0.2, sectionColor),
                                                            new Stop(0.2, sectionColor.deriveColor(0, 0.8, 1.1, 1)),
                                                            new Stop(1.0, sectionColor.deriveColor(0, 0.8, 1.1, 1)));

                Path sec = sections.get(i);
                sec.setFill(secFill);
                sec.setStrokeWidth(height * 0.01);

                MoveTo moveTo = (MoveTo) sec.getElements().get(0);
                moveTo.setX(centerX); moveTo.setY(centerY);

                sinValue = Math.sin(Math.toRadians(-sectionAngle * i + model.getStartAngle() + 270));
                cosValue = Math.cos(Math.toRadians(-sectionAngle * i + model.getStartAngle() + 270));
                LineTo lineTo1 = (LineTo) sec.getElements().get(1);
                lineTo1.setX(centerX + radius * sinValue); lineTo1.setY(centerY + radius * cosValue);

                sinValue = Math.sin(Math.toRadians(-sectionAngle * (i + 1) + model.getStartAngle() + 270));
                cosValue = Math.cos(Math.toRadians(-sectionAngle * (i + 1) + model.getStartAngle() + 270));
                LineTo lineTo2 = (LineTo) sec.getElements().get(2);
                lineTo2.setX(centerX + radius * sinValue); lineTo2.setY(centerY + radius * cosValue);
            }

            currentQualityRotate.setPivotX(centerX);
            currentQualityRotate.setPivotY(centerY);

            moveTo.setX(centerX); moveTo.setY(centerY);
            lineTo1.setX(centerX + width * 0.06856705); lineTo1.setY(height * 0.12174644);
            cubicCurveTo1.setControlX1(centerX + width * 0.06856705); cubicCurveTo1.setControlY1(height * 0.12174644); cubicCurveTo1.setControlX2(centerX + width * 0.06899351); cubicCurveTo1.setControlY2(height * 0.11400031); cubicCurveTo1.setX(centerX + width * 0.06899351); cubicCurveTo1.setY(height * 0.10990712);
            cubicCurveTo2.setControlX1(centerX + width * 0.06899351); cubicCurveTo2.setControlY1(height * 0.03723715);  cubicCurveTo2.setControlX2(centerX + width * 0.03810455); cubicCurveTo2.setControlY2(-height * 0.02167183); cubicCurveTo2.setX(centerX); cubicCurveTo2.setY(-height * 0.02167183);
            cubicCurveTo3.setControlX1(centerX + -width * 0.03810455); cubicCurveTo3.setControlY1(-height * 0.02167183); cubicCurveTo3.setControlX2(centerX - width * 0.06899351); cubicCurveTo3.setControlY2(height * 0.03723715); cubicCurveTo3.setX(centerX - width * 0.06899351); cubicCurveTo3.setY(height * 0.10990712);
            cubicCurveTo4.setControlX1(centerX - width * 0.06899351); cubicCurveTo4.setControlY1(height * 0.11400031); cubicCurveTo4.setControlX2(centerX - width * 0.06856705); cubicCurveTo4.setControlY2(height * 0.12174644); cubicCurveTo4.setX(centerX - width * 0.06856705); cubicCurveTo4.setY(height * 0.12174644);
            lineTo2.setX(centerX); lineTo2.setY(centerY);
            currentQuality.setStrokeWidth(height * 0.01);

            updateValue();

            knob.setCenterX(width * 0.5); knob.setCenterY(height * 0.94736842); knob.setRadius(height * 0.05572755);
            knob.setStrokeWidth(height * 0.01);

            redraw();
        }
    }

    private void redraw() {
        pane.setBackground(new Background(new BackgroundFill(backgroundPaint, new CornerRadii(1024), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(borderPaint, BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(borderWidth / PREFERRED_WIDTH * height))));

    }
    
    /**
     * Returns the title of the gauge. This title will usually
     * only be visible if it is not empty.
     *
     * @return the title of the gauge
     */
    public String getTitle() { return null == title ? _title : title.get(); }
    /**
     * Sets the title of the gauge. This title will only be visible
     * if it is not empty.
     *
     * @param TITLE
     */
    public void setTitle(final String TITLE) {
        if (null == title) {
            _title = TITLE;
            //fireUpdateEvent(VISIBILITY_EVENT);
        } else {
            title.set(TITLE);
        }
    }
    public StringProperty titleProperty() {
        if (null == title) {
            title  = new StringPropertyBase(_title) {
                //@Override protected void invalidated() { fireUpdateEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return QualityGauge.this; }
                @Override public String getName() { return "title"; }
            };
            _title = null;
        }
        return title;
    }
    
    public Pane getPane() {
    	return pane;
    }
}
