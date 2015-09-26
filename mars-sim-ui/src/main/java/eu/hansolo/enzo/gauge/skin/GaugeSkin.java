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

import eu.hansolo.enzo.common.Marker;
import eu.hansolo.enzo.common.Section;
import eu.hansolo.enzo.common.ValueEvent;
import eu.hansolo.enzo.fonts.Fonts;
import eu.hansolo.enzo.gauge.Gauge;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;


/**
 * Created by
 * User: hansolo
 * Date: 01.04.13
 * Time: 17:18
 */
public class GaugeSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double      PREFERRED_WIDTH  = 200;
    private static final double      PREFERRED_HEIGHT = 200;
    private static final double      MINIMUM_WIDTH    = 50;
    private static final double      MINIMUM_HEIGHT   = 50;
    private static final double      MAXIMUM_WIDTH    = 1024;
    private static final double      MAXIMUM_HEIGHT   = 1024;
    private double                   oldValue;
    private double                   size;
    private double                   centerX;
    private double                   centerY;
    private Pane                     pane;
    private Region                   background;
    private Canvas                   ticksAndSectionsCanvas;
    private GraphicsContext          ticksAndSections;
    private Region                   threshold;
    private Rotate                   thresholdRotate;
    private boolean                  thresholdExceeded;
    private Region                   minMeasuredValue;
    private Rotate                   minMeasuredValueRotate;
    private Region                   maxMeasuredValue;
    private Rotate                   maxMeasuredValueRotate;
    private Region                   needle;
    private Region                   needleHighlight;
    private Rotate                   needleRotate;
    private Region                   knob;
    private Group                    shadowGroup;
    private DropShadow               dropShadow;
    private Text                     titleText;
    private Text                     unitText;
    private Text                     valueText;
    private DropShadow               valueBlendBottomShadow;
    private InnerShadow              valueBlendTopShadow;
    private Blend                    valueBlend;
    private Path                     histogram;
    private double                   angleStep;
    private Timeline                 timeline;
    private double                   interactiveAngle;
    private EventHandler<MouseEvent> mouseEventHandler;
    private List<Node>               markersToRemove;
    private String                   limitString;
    private Instant                  lastCall;
    private boolean                  withinSpeedLimit;


    // ******************** Constructors **************************************
    public GaugeSkin(Gauge gauge) {
        super(gauge);
        angleStep         = gauge.getAngleRange() / (gauge.getMaxValue() - gauge.getMinValue());
        timeline          = new Timeline();
        mouseEventHandler = mouseEvent -> handleMouseEvent(mouseEvent);
        markersToRemove   = new ArrayList<>();
        oldValue          = getSkinnable().getValue();
        limitString       = "";
        lastCall          = Instant.now();
        withinSpeedLimit  = true;
        
        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getSkinnable().getPrefWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getSkinnable().getWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getHeight(), 0.0) <= 0) {
            if (getSkinnable().getPrefWidth() < 0 && getSkinnable().getPrefHeight() < 0) {                
                getSkinnable().setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        if (Double.compare(getSkinnable().getMinWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMinHeight(), 0.0) <= 0) {
            getSkinnable().setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }

        if (Double.compare(getSkinnable().getMaxWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMaxHeight(), 0.0) <= 0) {
            getSkinnable().setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }
    }

    private void initGraphics() {        
        valueBlendBottomShadow = new DropShadow();
        valueBlendBottomShadow.setBlurType(BlurType.TWO_PASS_BOX);
        valueBlendBottomShadow.setColor(Color.rgb(255, 255, 255, 0.5));
        valueBlendBottomShadow.setOffsetX(0);
        valueBlendBottomShadow.setOffsetY(0.005 * PREFERRED_WIDTH);
        valueBlendBottomShadow.setRadius(0);

        valueBlendTopShadow = new InnerShadow();
        valueBlendTopShadow.setBlurType(BlurType.TWO_PASS_BOX);
        valueBlendTopShadow.setColor(Color.rgb(0, 0, 0, 0.7));
        valueBlendTopShadow.setOffsetX(0);
        valueBlendTopShadow.setOffsetY(0.005 * PREFERRED_WIDTH);
        valueBlendTopShadow.setRadius(0.005 * PREFERRED_WIDTH);

        valueBlend = new Blend();
        valueBlend.setMode(BlendMode.MULTIPLY);
        valueBlend.setBottomInput(valueBlendBottomShadow);
        valueBlend.setTopInput(valueBlendTopShadow);

        background = new Region();
        background.getStyleClass().setAll("background");

        ticksAndSectionsCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        ticksAndSections = ticksAndSectionsCanvas.getGraphicsContext2D();        

        histogram = new Path();
        histogram.setFillRule(FillRule.NON_ZERO);
        histogram.getStyleClass().add("histogram-fill");

        minMeasuredValue = new Region();
        minMeasuredValue.getStyleClass().setAll("min-measured-value");
        minMeasuredValueRotate = new Rotate(180 - getSkinnable().getStartAngle() + (getSkinnable().getValue() - getSkinnable().getMinValue()) * angleStep);
        minMeasuredValue.getTransforms().setAll(minMeasuredValueRotate);
        minMeasuredValue.setOpacity(getSkinnable().isMinMeasuredValueVisible() ? 1 : 0);
        minMeasuredValue.setManaged(getSkinnable().isMinMeasuredValueVisible());

        maxMeasuredValue = new Region();
        maxMeasuredValue.getStyleClass().setAll("max-measured-value");
        maxMeasuredValueRotate = new Rotate(180 - getSkinnable().getStartAngle() + (getSkinnable().getValue() - getSkinnable().getMinValue()) * angleStep);
        maxMeasuredValue.getTransforms().setAll(maxMeasuredValueRotate);
        maxMeasuredValue.setOpacity(getSkinnable().isMaxMeasuredValueVisible() ? 1 : 0);
        maxMeasuredValue.setManaged(getSkinnable().isMaxMeasuredValueVisible());

        threshold = new Region();
        threshold.getStyleClass().setAll("threshold");
        thresholdRotate = new Rotate(180 - getSkinnable().getStartAngle() - getSkinnable().getMinValue() * angleStep);
        threshold.getTransforms().setAll(thresholdRotate);
        threshold.setOpacity(getSkinnable().isThresholdVisible() ? 1 : 0);
        threshold.setManaged(getSkinnable().isThresholdVisible());
        thresholdExceeded = false;

        // Set initial value
        angleStep          = getSkinnable().getAngleRange() / (getSkinnable().getMaxValue() - getSkinnable().getMinValue());
        double targetAngle = 180 - getSkinnable().getStartAngle() + (getSkinnable().getValue() - getSkinnable().getMinValue()) * angleStep;
        targetAngle        = clamp(180 - getSkinnable().getStartAngle(), 180 - getSkinnable().getStartAngle() + getSkinnable().getAngleRange(), targetAngle);

        needle = new Region();
        needle.getStyleClass().setAll(Gauge.STYLE_CLASS_NEEDLE_STANDARD);
        needleRotate = new Rotate(180 - getSkinnable().getStartAngle());
        needleRotate.setAngle(targetAngle);
        needle.getTransforms().setAll(needleRotate);

        needleHighlight = new Region();
        needleHighlight.setMouseTransparent(true);
        needleHighlight.getStyleClass().setAll("needle-highlight");
        needleHighlight.getTransforms().setAll(needleRotate);

        knob = new Region();
        knob.setPickOnBounds(false);
        knob.getStyleClass().setAll("knob");

        dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.25));
        dropShadow.setBlurType(BlurType.TWO_PASS_BOX);
        dropShadow.setRadius(0.015 * PREFERRED_WIDTH);
        dropShadow.setOffsetY(0.015 * PREFERRED_WIDTH);

        shadowGroup = new Group(needle, needleHighlight);//, knob);
        shadowGroup.setEffect(getSkinnable().isDropShadowEnabled() ? dropShadow : null);

        titleText = new Text(getSkinnable().getTitle());
        titleText.setTextOrigin(VPos.CENTER);
        titleText.getStyleClass().setAll("title");

        unitText = new Text(getSkinnable().getUnit());
        unitText.setMouseTransparent(true);
        unitText.setTextOrigin(VPos.CENTER);
        unitText.getStyleClass().setAll("unit");

        valueText = new Text(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getValue()));
        valueText.setMouseTransparent(true);
        valueText.setTextOrigin(VPos.CENTER);
        valueText.getStyleClass().setAll("value");
        valueText.setEffect(getSkinnable().isPlainValue() ? null : valueBlend);

        // Add all nodes
        pane = new Pane();
        pane.getChildren().setAll(background,
                                  histogram,
                                  ticksAndSectionsCanvas,
                                  minMeasuredValue,
                                  maxMeasuredValue,
                                  threshold,
                                  titleText,
                                  shadowGroup,
                                  knob,
                                  unitText,
                                  valueText);

        pane.getChildren().addAll(getSkinnable().getMarkers().keySet());

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().heightProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));                
        getSkinnable().minMeasuredValueProperty().addListener(observable -> handleControlPropertyChanged("MIN_MEASURED_VALUE"));
        getSkinnable().minMeasuredValueVisibleProperty().addListener(observable -> handleControlPropertyChanged("MIN_MEASURED_VALUE_VISIBLE"));
        getSkinnable().maxMeasuredValueProperty().addListener(observable -> handleControlPropertyChanged("MAX_MEASURED_VALUE"));
        getSkinnable().maxMeasuredValueVisibleProperty().addListener(observable -> handleControlPropertyChanged("MAX_MEASURED_VALUE_VISIBLE"));
        getSkinnable().tickLabelOrientationProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().needleTypeProperty().addListener(observable -> handleControlPropertyChanged("NEEDLE_TYPE"));
        getSkinnable().needleColorProperty().addListener(observable -> handleControlPropertyChanged("NEEDLE_COLOR"));
        getSkinnable().animatedProperty().addListener(observable -> handleControlPropertyChanged("ANIMATED"));
        getSkinnable().thresholdProperty().addListener(observable -> handleControlPropertyChanged("THRESHOLD"));
        getSkinnable().thresholdVisibleProperty().addListener(observable -> handleControlPropertyChanged("THRESHOLD_VISIBLE"));
        getSkinnable().angleRangeProperty().addListener(observable -> handleControlPropertyChanged("ANGLE_RANGE"));
        getSkinnable().numberFormatProperty().addListener(observable -> handleControlPropertyChanged("RECALC"));
        getSkinnable().plainValueProperty().addListener(observable -> handleControlPropertyChanged("PLAIN_VALUE"));
        getSkinnable().histogramEnabledProperty().addListener(observable -> handleControlPropertyChanged("HISTOGRAM"));
        getSkinnable().dropShadowEnabledProperty().addListener(observable -> handleControlPropertyChanged("DROP_SHADOW"));
        getSkinnable().interactiveProperty().addListener(observable -> handleControlPropertyChanged("INTERACTIVE"));
        getSkinnable().getSections().addListener((ListChangeListener<Section>) change -> handleControlPropertyChanged("CANVAS_REFRESH"));
        getSkinnable().getAreas().addListener((ListChangeListener<Section>) change -> handleControlPropertyChanged("CANVAS_REFRESH"));
        getSkinnable().getMarkers().addListener((MapChangeListener<Marker, Rotate>) change -> handleControlPropertyChanged("MARKER"));

        getSkinnable().markersVisibleProperty().addListener(observable -> handleControlPropertyChanged("MARKER"));
        getSkinnable().sectionsVisibleProperty().addListener(observable -> handleControlPropertyChanged("CANVAS_REFRESH"));
        getSkinnable().areasVisibleProperty().addListener(observable -> handleControlPropertyChanged("CANVAS_REFRESH"));

        getSkinnable().customKnobClickHandlerProperty().addListener(observable -> handleControlPropertyChanged("CUSTOM_KNOB_CLICK_HANDLER"));

        getSkinnable().valueProperty().addListener((OV, OLD_VALUE, NEW_VALUE) -> {                         
            withinSpeedLimit = !(Instant.now().minusMillis((long) getSkinnable().getAnimationDuration()).isBefore(lastCall));
            lastCall         = Instant.now();
            oldValue         = OLD_VALUE.doubleValue();            
            rotateNeedle();
        });

        getSkinnable().minValueProperty().addListener((OV, OLD_VALUE, NEW_VALUE) -> {
            angleStep = getSkinnable().getAngleRange() / (getSkinnable().getMaxValue() - NEW_VALUE.doubleValue());
            needleRotate.setAngle((180 - getSkinnable().getStartAngle()) + (getSkinnable().getValue() - NEW_VALUE.doubleValue()) * angleStep);
            if (getSkinnable().getValue() < NEW_VALUE.doubleValue()) {
                getSkinnable().setValue(NEW_VALUE.doubleValue());
                oldValue = NEW_VALUE.doubleValue();
            }
        });
        getSkinnable().maxValueProperty().addListener((OV, OLD_VALUE, NEW_VALUE) -> {
            angleStep = getSkinnable().getAngleRange() / (NEW_VALUE.doubleValue() - getSkinnable().getMinValue());
            needleRotate.setAngle((180 - getSkinnable().getStartAngle()) + (getSkinnable().getValue() - getSkinnable().getMinValue()) * angleStep);
            if (getSkinnable().getValue() > NEW_VALUE.doubleValue()) {
                getSkinnable().setValue(NEW_VALUE.doubleValue());
                oldValue = NEW_VALUE.doubleValue();
            }
        });
        
        getSkinnable().limitProperty().addListener(observable -> handleControlPropertyChanged("LIMIT")) ;
        
        needleRotate.angleProperty().addListener(observable -> handleControlPropertyChanged("ANGLE"));

        if (null == getSkinnable().getCustomKnobClickHandler()) {
            knob.setOnMousePressed(event -> {
                timeline.stop();
                needleRotate.setAngle(180 - getSkinnable().getStartAngle() + (getSkinnable().getValue() - getSkinnable().getMinValue()) * angleStep);
                getSkinnable().setInteractive(!getSkinnable().isInteractive());
            });
        } else {
            knob.setOnMousePressed(getSkinnable().getCustomKnobClickHandler());
        }

        minMeasuredValue.setOnMousePressed(mouseEventHandler);
        minMeasuredValue.setOnMouseReleased(mouseEventHandler);

        maxMeasuredValue.setOnMousePressed(mouseEventHandler);
        maxMeasuredValue.setOnMouseReleased(mouseEventHandler);

        threshold.setOnMousePressed(mouseEventHandler);
        threshold.setOnMouseDragged(mouseEventHandler);
        threshold.setOnMouseReleased(mouseEventHandler);
                
        for (Marker marker : getSkinnable().getMarkers().keySet()) {
            marker.setOnMousePressed(mouseEventHandler);
            marker.setOnMouseDragged(mouseEventHandler);
            marker.setOnMouseReleased(mouseEventHandler);
        }
    }


    // ******************** Methods *******************************************
    protected void handleControlPropertyChanged(final String PROPERTY) {
        if ("RESIZE".equals(PROPERTY)) {
            resize();
        } else if ("ANGLE".equals(PROPERTY)) {
            if (getSkinnable().isInteractive()) return;

            double currentValue = (needleRotate.getAngle() + getSkinnable().getStartAngle() - 180) / angleStep + getSkinnable().getMinValue();                        
            valueText.setText(limitString + String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", currentValue));
            valueText.setTranslateX((size - valueText.getLayoutBounds().getWidth()) * 0.5);
            // Check threshold
            if (thresholdExceeded) {
                if (currentValue < getSkinnable().getThreshold()) {
                    getSkinnable().fireEvent(new ValueEvent(this, null, ValueEvent.VALUE_UNDERRUN));
                    thresholdExceeded = false;
                }
            } else {
                if (currentValue > getSkinnable().getThreshold()) {
                    getSkinnable().fireEvent(new ValueEvent(this, null, ValueEvent.VALUE_EXCEEDED));
                    thresholdExceeded = true;
                }
            }
            // Check each marker
            for (Marker marker : getSkinnable().getMarkers().keySet()) {
                if (marker.isExceeded()) {
                    if (currentValue < marker.getValue()) {
                        marker.fireMarkerEvent(new Marker.MarkerEvent(this, null, Marker.MarkerEvent.MARKER_UNDERRUN));
                        marker.setExceeded(false);
                    }
                } else {
                    if (currentValue > marker.getValue()) {
                        marker.fireMarkerEvent(new Marker.MarkerEvent(this, null, Marker.MarkerEvent.MARKER_EXCEEDED));
                        marker.setExceeded(true);
                    }
                }
            }
            // Check min- and maxMeasuredValue
            if (currentValue < getSkinnable().getMinMeasuredValue()) {
                getSkinnable().setMinMeasuredValue(currentValue);
                minMeasuredValueRotate.setAngle(currentValue * angleStep - 180 - getSkinnable().getStartAngle() - getSkinnable().getMinValue() * angleStep);
            }
            if (currentValue > getSkinnable().getMaxMeasuredValue()) {
                getSkinnable().setMaxMeasuredValue(currentValue);
                maxMeasuredValueRotate.setAngle(currentValue * angleStep - 180 - getSkinnable().getStartAngle() - getSkinnable().getMinValue() * angleStep);
            }
            // Check sections
            for (Section section : getSkinnable().getSections()) {
                if (section.contains(currentValue)) {
                    section.fireSectionEvent(new Section.SectionEvent(section, null, Section.SectionEvent.ENTERING_SECTION));
                    break;
                }
            }
            // Check areas
            for (Section area : getSkinnable().getAreas()) {
                if (area.contains(currentValue)) {
                    area.fireSectionEvent(new Section.SectionEvent(area, null, Section.SectionEvent.ENTERING_SECTION));
                    break;
                }
            }
        } else if ("PLAIN_VALUE".equals(PROPERTY)) {
            valueText.setEffect(getSkinnable().isPlainValue() ? null : valueBlend);
        } else if ("HISTOGRAM".equals(PROPERTY)) {
            histogram.setVisible(getSkinnable().isHistogramEnabled());
            histogram.setManaged(getSkinnable().isHistogramEnabled());
        } else if ("DROP_SHADOW".equals(PROPERTY)) {
            shadowGroup.setEffect(getSkinnable().isDropShadowEnabled() ? dropShadow : null);
        } else if ("INTERACTIVE".equals(PROPERTY)) {                        
            needle.setMouseTransparent(getSkinnable().isInteractive());

            if (getSkinnable().isInteractive()) {
                unitText.setText("Interactive");
                valueText.setText("");
                resizeText();
                shadowGroup.setEffect(null);
            } else {
                unitText.setText(getSkinnable().getUnit());
                valueText.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", (needleRotate.getAngle() + getSkinnable().getStartAngle() - 180) / angleStep));
                resizeText();
                shadowGroup.setEffect(dropShadow);
            }
        } else if ("CANVAS_REFRESH".equals(PROPERTY)) {
            ticksAndSections.clearRect(0, 0, size, size);
            drawSections(ticksAndSections);
            drawAreas(ticksAndSections);
            drawTickMarks(ticksAndSections);
        } else if ("THRESHOLD".equals(PROPERTY)) {
            thresholdRotate.setAngle(getSkinnable().getThreshold() * angleStep - 180 - getSkinnable().getStartAngle());
        } else if ("THRESHOLD_VISIBLE".equals(PROPERTY)) {
            threshold.setOpacity(getSkinnable().isThresholdVisible() ? 1 : 0);
            threshold.setManaged(getSkinnable().isThresholdVisible());
        } else if ("MIN_MEASURED_VALUE_VISIBLE".equals(PROPERTY)) {
            minMeasuredValue.setOpacity(getSkinnable().isMinMeasuredValueVisible() ? 1 : 0);
            minMeasuredValue.setManaged(getSkinnable().isMinMeasuredValueVisible());
        } else if ("MAX_MEASURED_VALUE_VISIBLE".equals(PROPERTY)) {
            maxMeasuredValue.setOpacity(getSkinnable().isMaxMeasuredValueVisible() ? 1 : 0);
            maxMeasuredValue.setManaged(getSkinnable().isMaxMeasuredValueVisible());
        } else if ("MARKER".equals(PROPERTY)) {
            checkForRemovedMarkers();
            for (Marker marker : getSkinnable().getMarkers().keySet()) {
                if (pane.getChildren().contains(marker)) continue;
                pane.getChildren().add(marker);
                // Add MouseEvent handler
                marker.setOnMousePressed(mouseEventHandler);
                marker.setOnMouseDragged(mouseEventHandler);
                marker.setOnMouseReleased(mouseEventHandler);                
            }
            drawMarkers();
        } else if ("LIMIT".equals(PROPERTY)) {
            switch(getSkinnable().getLimit()) {
                case EXCEEDED:
                    limitString = ">";
                    break;
                case IN_RANGE:
                    limitString = "";
                    break;
                case UNDERRUN:
                    limitString = "<";
                    break;
            }
        } else if ("CUSTOM_KNOB_CLICK_HANDLER".equals(PROPERTY)) {
            if (null == getSkinnable().getCustomKnobClickHandler()) {
                knob.setOnMousePressed(event -> {
                    timeline.stop();
                    needleRotate.setAngle(180 - getSkinnable().getStartAngle() + (getSkinnable().getValue() - getSkinnable().getMinValue()) * angleStep);
                    getSkinnable().setInteractive(!getSkinnable().isInteractive());
                });
            } else {
                knob.setOnMousePressed(getSkinnable().getCustomKnobClickHandler());
            }
        }
    }
    

    // ******************** Private Methods ***********************************
    private void checkForRemovedMarkers() {
        markersToRemove.clear();
        for (Node node : pane.getChildren()) {
            if (node instanceof Marker) {
                if (getSkinnable().getMarkers().keySet().contains(node)) continue;
                node.setManaged(false);
                node.removeEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
                node.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEventHandler);
                node.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseEventHandler);
                markersToRemove.add(node);
            }
        }
        for (Node node : markersToRemove) pane.getChildren().remove(node);
    }

    private void handleMouseEvent(final MouseEvent MOUSE_EVENT) {
        final Object    SRC  = MOUSE_EVENT.getSource();
        final EventType TYPE = MOUSE_EVENT.getEventType();
        if (getSkinnable().isInteractive() && SRC.equals(threshold)) {
            if (MouseEvent.MOUSE_PRESSED == TYPE) {
                unitText.setText("Threshold");
                valueText.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getThreshold()));
                resizeText();
            } else if (MouseEvent.MOUSE_DRAGGED == TYPE) {
                touchRotate(MOUSE_EVENT.getSceneX() - getSkinnable().getLayoutX(), MOUSE_EVENT.getSceneY() - getSkinnable().getLayoutY(), thresholdRotate);
            } else if (MouseEvent.MOUSE_RELEASED == TYPE) {
                getSkinnable().setThreshold(Double.parseDouble(valueText.getText()));
                fadeBackToInteractive();
            }
        } else if (getSkinnable().isInteractive() && SRC instanceof Marker) {
            if (MouseEvent.MOUSE_PRESSED == TYPE) {
                unitText.setText(((Marker) SRC).getText());
                valueText.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", ((Marker) SRC).getValue()));
                resizeText();
            } else if (MouseEvent.MOUSE_DRAGGED == TYPE) {
                touchRotate(MOUSE_EVENT.getSceneX() - getSkinnable().getLayoutX(), MOUSE_EVENT.getSceneY() - getSkinnable().getLayoutY(), getSkinnable().getMarkers().get(SRC));
            } else if (MouseEvent.MOUSE_RELEASED == TYPE) {
                ((Marker) SRC).setValue(Double.parseDouble(valueText.getText()));
                fadeBackToInteractive();
            }
        } else if (getSkinnable().isInteractive() && SRC.equals(minMeasuredValue)) {
            if (MouseEvent.MOUSE_PRESSED == TYPE) {
                unitText.setText("Min");
                valueText.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getMinMeasuredValue()));
                resizeText();
            } else if (MouseEvent.MOUSE_RELEASED == TYPE) {
                fadeBackToInteractive();
            }
        } else if (getSkinnable().isInteractive() && SRC.equals(maxMeasuredValue)) {
            if (MouseEvent.MOUSE_PRESSED == TYPE) {
                unitText.setText("Max");
                valueText.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getMaxMeasuredValue()));
                resizeText();
            } else if (MouseEvent.MOUSE_RELEASED == TYPE) {
                fadeBackToInteractive();
            }
        }
    }

    private void handleTouchEvent(final TouchEvent TOUCH_EVENT) {
        final Object    SRC  = TOUCH_EVENT.getSource();
        final EventType TYPE = TOUCH_EVENT.getEventType();
        if (SRC.equals(threshold)) {
            if (TouchEvent.TOUCH_PRESSED == TYPE) {
                unitText.setText("Threshold");
                valueText.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getThreshold()));
                resizeText();
            } else if (TouchEvent.TOUCH_MOVED == TYPE) {
                touchRotate(TOUCH_EVENT.getTouchPoint().getSceneX() - getSkinnable().getLayoutX(), TOUCH_EVENT.getTouchPoint().getSceneY() - getSkinnable().getLayoutY(), thresholdRotate);
            } else if (TouchEvent.TOUCH_RELEASED == TYPE) {
                getSkinnable().setThreshold(Double.parseDouble(valueText.getText()));
                fadeBackToInteractive();
            }
        } else if (SRC instanceof Marker) {
            if (TouchEvent.TOUCH_PRESSED == TYPE) {
                unitText.setText(((Marker) SRC).getText());
                valueText.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", ((Marker) SRC).getValue()));
                resizeText();
            } else if (TouchEvent.TOUCH_MOVED == TYPE) {
                touchRotate(TOUCH_EVENT.getTouchPoint().getSceneX() - getSkinnable().getLayoutX(), TOUCH_EVENT.getTouchPoint().getSceneY() - getSkinnable().getLayoutY(), getSkinnable().getMarkers().get(SRC));
            } else if (TouchEvent.TOUCH_RELEASED == TYPE) {
                ((Marker) SRC).setValue(Double.parseDouble(valueText.getText()));
                fadeBackToInteractive();
            }
        } else if (SRC.equals(minMeasuredValue)) {
            if (TouchEvent.TOUCH_PRESSED == TYPE) {
                unitText.setText("Min");
                valueText.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getMinMeasuredValue()));
                resizeText();
            } else if (TouchEvent.TOUCH_RELEASED == TYPE) {
                fadeBackToInteractive();
            }
        } else if (SRC.equals(maxMeasuredValue)) {
            if (TouchEvent.TOUCH_PRESSED == TYPE) {
                unitText.setText("Max");
                valueText.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getMaxMeasuredValue()));
                resizeText();
            } else if (TouchEvent.TOUCH_RELEASED == TYPE) {
                fadeBackToInteractive();
            }
        }
    }

    private double getTheta(double x, double y) {
        double deltaX = x - centerX;
        double deltaY = y - centerY;
        double radius = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
        double nx     = deltaX / radius;
        double ny     = deltaY / radius;
        double theta  = Math.atan2(ny, nx);
        return Double.compare(theta, 0.0) >= 0 ? Math.toDegrees(theta) : Math.toDegrees((theta)) + 360.0;
    }

    private void touchRotate(final double X, final double Y, final Rotate ROTATE) {
        double theta     = getTheta(X, Y);
        interactiveAngle = (theta + 90) % 360;
        double newValue  = Double.compare(interactiveAngle, 180) <= 0 ?
                           (interactiveAngle + 180.0 + getSkinnable().getStartAngle() - 360) / angleStep + getSkinnable().getMinValue():
                           (interactiveAngle - 180.0 + getSkinnable().getStartAngle() - 360) / angleStep + getSkinnable().getMinValue();
        if (Double.compare(newValue, getSkinnable().getMinValue()) >= 0 && Double.compare(newValue, getSkinnable().getMaxValue()) <= 0) {
            ROTATE.setAngle(interactiveAngle);
            valueText.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", newValue));
            resizeText();
        }

    }

    private void fadeBackToInteractive() {
        FadeTransition fadeUnitOut = new FadeTransition(Duration.millis(425), unitText);
        fadeUnitOut.setFromValue(1.0);
        fadeUnitOut.setToValue(0.0);
        FadeTransition fadeValueOut = new FadeTransition(Duration.millis(425), valueText);
        fadeValueOut.setFromValue(1.0);
        fadeValueOut.setToValue(0.0);

        PauseTransition pause = new PauseTransition(Duration.millis(50));

        FadeTransition fadeUnitIn = new FadeTransition(Duration.millis(425), unitText);
        fadeUnitIn.setFromValue(0.0);
        fadeUnitIn.setToValue(1.0);
        FadeTransition fadeValueIn = new FadeTransition(Duration.millis(425), valueText);
        fadeValueIn.setFromValue(0.0);
        fadeValueIn.setToValue(1.0);
        ParallelTransition parallelIn = new ParallelTransition(fadeUnitIn, fadeValueIn);

        ParallelTransition parallelOut = new ParallelTransition(fadeUnitOut, fadeValueOut);
        parallelOut.setOnFinished(event -> {
            unitText.setText("Interactive");
            valueText.setText("");
            resizeText();
        });

        SequentialTransition sequence = new SequentialTransition(parallelOut, pause, parallelIn);
        sequence.play();
    }

    private void rotateNeedle() {
        if (getSkinnable().isInteractive()) return;
        timeline.stop();
        angleStep          = getSkinnable().getAngleRange() / (getSkinnable().getMaxValue() - getSkinnable().getMinValue());
        double targetAngle = 180 - getSkinnable().getStartAngle() + (getSkinnable().getValue() - getSkinnable().getMinValue()) * angleStep;
        targetAngle        = clamp(180 - getSkinnable().getStartAngle(), 180 - getSkinnable().getStartAngle() + getSkinnable().getAngleRange(), targetAngle);
        if (withinSpeedLimit && getSkinnable().isAnimated()) {            
            //double animationDuration = (getSkinnable().getAnimationDuration() / (getSkinnable().getMaxValue() - getSkinnable().getMinValue())) * Math.abs(getSkinnable().getValue() - getSkinnable().getOldValue());
            final KeyValue KEY_VALUE = new KeyValue(needleRotate.angleProperty(), targetAngle, Interpolator.SPLINE(0.5, 0.4, 0.4, 1.0));
            final KeyFrame KEY_FRAME = new KeyFrame(Duration.millis(getSkinnable().getAnimationDuration()), KEY_VALUE);            
            timeline.getKeyFrames().setAll(KEY_FRAME);
            timeline.play();
        } else {
            needleRotate.setAngle(targetAngle);
        }
    }

    private void changeNeedle() {
        switch(getSkinnable().getNeedleType()) {
            default:
                needle.getStyleClass().setAll(Gauge.STYLE_CLASS_NEEDLE_STANDARD);
        }
    }

    private void drawTickMarks(final GraphicsContext CTX) {
        if (getSkinnable().isHistogramEnabled()) {
            double xy;
            double wh;
            double step         = 0;
            double OFFSET       = 90 - getSkinnable().getStartAngle();
            double ANGLE_EXTEND = (getSkinnable().getMaxValue()) * angleStep;
            CTX.setStroke(Color.rgb(200, 200, 200));
            CTX.setLineWidth(size * 0.001);
            CTX.setLineCap(StrokeLineCap.BUTT);
            for (int i = 0 ; i < 5 ; i++) {
                xy = (size - (0.435 + step) * size) / 2;
                wh = size * (0.435 + step);
                CTX.strokeArc(xy, xy, wh, wh, -OFFSET, -ANGLE_EXTEND, ArcType.OPEN);
                step += 0.075;
            }
        }

        double  sinValue;
        double  cosValue;
        double  startAngle = getSkinnable().getStartAngle();
        double  orthText   = Gauge.TickLabelOrientation.ORTHOGONAL == getSkinnable().getTickLabelOrientation() ? 0.33 : 0.31;
        Point2D center     = new Point2D(size * 0.5, size * 0.5);
        for (double angle = 0, counter = getSkinnable().getMinValue() ; Double.compare(counter, getSkinnable().getMaxValue()) <= 0 ; angle -= angleStep, counter++) {
            sinValue = Math.sin(Math.toRadians(angle + startAngle));
            cosValue = Math.cos(Math.toRadians(angle + startAngle));

            Point2D innerMainPoint   = new Point2D(center.getX() + size * 0.368 * sinValue, center.getY() + size * 0.368 * cosValue);
            Point2D innerMediumPoint = new Point2D(center.getX() + size * 0.388 * sinValue, center.getY() + size * 0.388 * cosValue);
            Point2D innerMinorPoint  = new Point2D(center.getX() + size * 0.3975 * sinValue, center.getY() + size * 0.3975 * cosValue);
            Point2D outerPoint       = new Point2D(center.getX() + size * 0.432 * sinValue, center.getY() + size * 0.432 * cosValue);
            Point2D textPoint        = new Point2D(center.getX() + size * orthText * sinValue, center.getY() + size * orthText * cosValue);

            CTX.setStroke(getSkinnable().getTickMarkFill());
            if (counter % getSkinnable().getMajorTickSpace() == 0) {
                // Draw major tickmark
                CTX.setLineWidth(size * 0.0055);
                CTX.strokeLine(innerMainPoint.getX(), innerMainPoint.getY(), outerPoint.getX(), outerPoint.getY());

                // Draw text
                CTX.save();
                CTX.translate(textPoint.getX(), textPoint.getY());
                switch(getSkinnable().getTickLabelOrientation()) {
                    case ORTHOGONAL:
                        if ((360 - startAngle - angle) % 360 > 90 && (360 - startAngle - angle) % 360 < 270) {
                            CTX.rotate((180 - startAngle - angle) % 360);
                        } else {
                            CTX.rotate((360 - startAngle - angle) % 360);
                        }
                        break;
                    case TANGENT:
                        if ((360 - startAngle - angle - 90) % 360 > 90 && (360 - startAngle - angle - 90) % 360 < 270) {
                            CTX.rotate((90 - startAngle - angle) % 360);
                        } else {
                            CTX.rotate((270 - startAngle - angle) % 360);
                        }
                        break;
                    case HORIZONTAL:
                    default:
                        break;
                }
                CTX.setFont(Font.font("Verdana", FontWeight.NORMAL, 0.045 * size));
                CTX.setTextAlign(TextAlignment.CENTER);
                CTX.setTextBaseline(VPos.CENTER);
                CTX.setFill(getSkinnable().getTickLabelFill());
                CTX.fillText(Integer.toString((int) counter), 0, 0);
                CTX.restore();
            } else if (getSkinnable().getMinorTickSpace() % 2 != 0 && counter % 5 == 0) {
                CTX.setLineWidth(size * 0.0035);
                CTX.strokeLine(innerMediumPoint.getX(), innerMediumPoint.getY(), outerPoint.getX(), outerPoint.getY());
            } else if (counter % getSkinnable().getMinorTickSpace() == 0) {
                CTX.setLineWidth(size * 0.00225);
                CTX.strokeLine(innerMinorPoint.getX(), innerMinorPoint.getY(), outerPoint.getX(), outerPoint.getY());
            }
        }
    }

    private final void drawSections(final GraphicsContext CTX) {
        final double xy        = (size - 0.83 * size) / 2;
        final double wh        = size * 0.83;
        final double MIN_VALUE = getSkinnable().getMinValue();
        final double MAX_VALUE = getSkinnable().getMaxValue();
        final double OFFSET = 90 - getSkinnable().getStartAngle();

        IntStream.range(0, getSkinnable().getSections().size()).parallel().forEachOrdered(
            i -> {
                final Section SECTION = getSkinnable().getSections().get(i);                
                final double SECTION_START_ANGLE;
                if (Double.compare(SECTION.getStart(), MAX_VALUE) <= 0 && Double.compare(SECTION.getStop(), MIN_VALUE) >= 0) {
                    if (SECTION.getStart() < MIN_VALUE && SECTION.getStop() < MAX_VALUE) {
                        SECTION_START_ANGLE = MIN_VALUE * angleStep;
                    } else {
                        SECTION_START_ANGLE = (SECTION.getStart() - MIN_VALUE) * angleStep;
                    }
                    final double SECTION_ANGLE_EXTEND;
                    if (SECTION.getStop() > MAX_VALUE) {
                        SECTION_ANGLE_EXTEND = MAX_VALUE * angleStep;
                    } else {
                        SECTION_ANGLE_EXTEND = (SECTION.getStop() - SECTION.getStart()) * angleStep;
                    }

                    CTX.save();
                    switch(i) {
                        case 0: CTX.setStroke(getSkinnable().getSectionFill0()); break;
                        case 1: CTX.setStroke(getSkinnable().getSectionFill1()); break;
                        case 2: CTX.setStroke(getSkinnable().getSectionFill2()); break;
                        case 3: CTX.setStroke(getSkinnable().getSectionFill3()); break;
                        case 4: CTX.setStroke(getSkinnable().getSectionFill4()); break;
                        case 5: CTX.setStroke(getSkinnable().getSectionFill5()); break;
                        case 6: CTX.setStroke(getSkinnable().getSectionFill6()); break;
                        case 7: CTX.setStroke(getSkinnable().getSectionFill7()); break;
                        case 8: CTX.setStroke(getSkinnable().getSectionFill8()); break;
                        case 9: CTX.setStroke(getSkinnable().getSectionFill9()); break;
                    }
                    CTX.setLineWidth(size * 0.037);
                    CTX.setLineCap(StrokeLineCap.BUTT);
                    CTX.strokeArc(xy, xy, wh, wh, -(OFFSET + SECTION_START_ANGLE), -SECTION_ANGLE_EXTEND, ArcType.OPEN);
                    CTX.restore();   
                }                    
            }
        );        
    }

    private final void drawAreas(final GraphicsContext CTX) {
        final double xy        = (size - 0.79 * size) / 2;
        final double wh        = size * 0.79;
        final double MIN_VALUE = getSkinnable().getMinValue();
        final double MAX_VALUE = getSkinnable().getMaxValue();
        final double OFFSET = 90 - getSkinnable().getStartAngle();
        
        IntStream.range(0, getSkinnable().getAreas().size()).parallel().forEachOrdered(
            i -> {
                final Section AREA = getSkinnable().getAreas().get(i);
                final double AREA_START_ANGLE;
                if (Double.compare(AREA.getStart(), MAX_VALUE) <= 0 && Double.compare(AREA.getStop(), MIN_VALUE) >= 0) {
                    if (AREA.getStart() < MIN_VALUE && AREA.getStop() < MAX_VALUE) {
                        AREA_START_ANGLE = MIN_VALUE * angleStep;
                    } else {
                        AREA_START_ANGLE = (AREA.getStart() - MIN_VALUE) * angleStep;
                    }
                    final double AREA_ANGLE_EXTEND;
                    if (AREA.getStop() > MAX_VALUE) {
                        AREA_ANGLE_EXTEND = MAX_VALUE * angleStep;
                    } else {
                        AREA_ANGLE_EXTEND = (AREA.getStop() - AREA.getStart()) * angleStep;
                    }

                    CTX.save();
                    switch(i) {
                        case 0: CTX.setFill(getSkinnable().getAreaFill0()); break;
                        case 1: CTX.setFill(getSkinnable().getAreaFill1()); break;
                        case 2: CTX.setFill(getSkinnable().getAreaFill2()); break;
                        case 3: CTX.setFill(getSkinnable().getAreaFill3()); break;
                        case 4: CTX.setFill(getSkinnable().getAreaFill4()); break;
                        case 5: CTX.setFill(getSkinnable().getAreaFill5()); break;
                        case 6: CTX.setFill(getSkinnable().getAreaFill6()); break;
                        case 7: CTX.setFill(getSkinnable().getAreaFill7()); break;
                        case 8: CTX.setFill(getSkinnable().getAreaFill8()); break;
                        case 9: CTX.setFill(getSkinnable().getAreaFill9()); break;
                    }  
                    CTX.fillArc(xy, xy, wh, wh, -(OFFSET + AREA_START_ANGLE), - AREA_ANGLE_EXTEND, ArcType.ROUND);
                    CTX.restore();
                }
            }
        );
    }
    
    private final void drawMarkers() {
        for (Marker marker : getSkinnable().getMarkers().keySet()) {
            marker.setPrefSize(0.0325 * size, 0.0325 * size);
            marker.relocate((size - marker.getPrefWidth()) * 0.5, size * 0.04);
            getSkinnable().getMarkers().get(marker).setPivotX(marker.getPrefWidth() * 0.5);
            getSkinnable().getMarkers().get(marker).setPivotY(size * 0.46);
            getSkinnable().getMarkers().get(marker).setAngle(marker.getValue() * angleStep - 180 - getSkinnable().getStartAngle() - getSkinnable().getMinValue() * angleStep);
        }
    }

    private double clamp(final double MIN_VALUE, final double MAX_VALUE, final double VALUE) {
        if (VALUE < MIN_VALUE) return MIN_VALUE;
        if (VALUE > MAX_VALUE) return MAX_VALUE;
        return VALUE;
    }
    
    private void resizeText() {        
        titleText.setFont(Fonts.robotoMedium(size * 0.06));
        titleText.setTranslateX((size - titleText.getLayoutBounds().getWidth()) * 0.5);
        titleText.setTranslateY(size * 0.84);

        unitText.setFont(Fonts.robotoMedium(size * 0.045));
        unitText.setTranslateX((size - unitText.getLayoutBounds().getWidth()) * 0.5);
        unitText.setTranslateY(size * 0.4);

        valueText.setFont(Fonts.robotoBold(size * 0.1));
        valueText.setTranslateX((size - valueText.getLayoutBounds().getWidth()) * 0.5);
        valueText.setTranslateY(size * 0.5);
    }

    private void resize() {
        size = getSkinnable().getWidth() < getSkinnable().getHeight() ? getSkinnable().getWidth() : getSkinnable().getHeight();
        centerX = size * 0.5;
        centerY = size * 0.5;

        if (size > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((getSkinnable().getWidth() - size) * 0.5, (getSkinnable().getHeight() - size) * 0.5);
            
            valueBlendBottomShadow.setOffsetY(0.005 * size);
    
            valueBlendTopShadow.setOffsetY(0.005 * size);
            valueBlendTopShadow.setRadius(0.005 * size);
    
            dropShadow.setRadius(0.015 * size);
            dropShadow.setOffsetY(0.015 * size);
    
            background.setPrefSize(size, size);            
            // TODO: Remove this CSS inlining workaround by using % in -fx-background-insets 
            background.setStyle("-fx-background-insets: 0, " + 0.018 * size + ", " + 0.02 * size + ", " + 0.036 * size + ";");
    
            ticksAndSectionsCanvas.setWidth(size);
            ticksAndSectionsCanvas.setHeight(size);
            ticksAndSections.clearRect(0, 0, size, size);
            if (getSkinnable().isSectionsVisible()) drawSections(ticksAndSections);
            if (getSkinnable().isAreasVisible()) drawAreas(ticksAndSections);
            drawTickMarks(ticksAndSections);
            ticksAndSectionsCanvas.setCache(true);
            ticksAndSectionsCanvas.setCacheHint(CacheHint.QUALITY);
    
            if (getSkinnable().isMarkersVisible()) drawMarkers();
    
            minMeasuredValue.setPrefSize(0.03 * size, 0.03 * size);
            minMeasuredValue.relocate((size - minMeasuredValue.getPrefWidth()) * 0.5, size * 0.11);
            minMeasuredValueRotate.setPivotX(minMeasuredValue.getPrefWidth() * 0.5);
            minMeasuredValueRotate.setPivotY(size * 0.39);
            minMeasuredValueRotate.setAngle(getSkinnable().getMinMeasuredValue() * angleStep - 180 - getSkinnable().getStartAngle() - getSkinnable().getMinValue() * angleStep);
    
            maxMeasuredValue.setPrefSize(0.03 * size, 0.03 * size);
            maxMeasuredValue.relocate((size - maxMeasuredValue.getPrefWidth()) * 0.5, size * 0.11);
            maxMeasuredValueRotate.setPivotX(maxMeasuredValue.getPrefWidth() * 0.5);
            maxMeasuredValueRotate.setPivotY(size * 0.39);
            maxMeasuredValueRotate.setAngle(getSkinnable().getMaxMeasuredValue() * angleStep - 180 - getSkinnable().getStartAngle() - getSkinnable().getMinValue() * angleStep);
    
            threshold.setPrefSize(0.03 * size, 0.0275 * size);
            threshold.relocate((size - threshold.getPrefWidth()) * 0.5, size * 0.11);
            thresholdRotate.setPivotX(threshold.getPrefWidth() * 0.5);
            thresholdRotate.setPivotY(size * 0.39);
            thresholdRotate.setAngle(getSkinnable().getThreshold() * angleStep - 180 - getSkinnable().getStartAngle() - getSkinnable().getMinValue() * angleStep);

            switch (getSkinnable().getNeedleType()) {
                default:
                    needle.setPrefSize(size * 0.04, size * 0.425);
            }
            needle.relocate((size - needle.getPrefWidth()) * 0.5, size * 0.5 - needle.getPrefHeight());
            needleRotate.setPivotX(needle.getPrefWidth() * 0.5);
            needleRotate.setPivotY(needle.getPrefHeight());
    
            needleHighlight.setPrefSize(size * 0.04, size * 0.425);
            needleHighlight.setTranslateX((size - needle.getPrefWidth()) * 0.5);
            needleHighlight.setTranslateY(size * 0.5 - needle.getPrefHeight());
    
            knob.setPrefSize(size * 0.35, size * 0.35);
            knob.setTranslateX((size - knob.getPrefWidth()) * 0.5);
            knob.setTranslateY((size - knob.getPrefHeight()) * 0.5);
    
            resizeText();
        }
    }
}
