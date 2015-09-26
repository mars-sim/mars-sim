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

import eu.hansolo.enzo.common.ConicalGradient;
import eu.hansolo.enzo.common.Marker;
import eu.hansolo.enzo.common.Section;
import eu.hansolo.enzo.common.ValueEvent;
import eu.hansolo.enzo.fonts.Fonts;
import eu.hansolo.enzo.gauge.RadialBargraph;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
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
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;


/**
 * Created by
 * User: hansolo
 * Date: 17.07.13
 * Time: 08:02
 */
public class RadialBargraphSkin extends SkinBase<RadialBargraph> implements Skin<RadialBargraph> {
    private static final double      PREFERRED_WIDTH  = 200;
    private static final double      PREFERRED_HEIGHT = 200;
    private static final double      MINIMUM_WIDTH    = 50;
    private static final double      MINIMUM_HEIGHT   = 50;
    private static final double      MAXIMUM_WIDTH    = 1024;
    private static final double      MAXIMUM_HEIGHT   = 1024;
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
    private DoubleProperty           angle;
    private Arc                      bar;
    private Region                   knob;
    private DropShadow               dropShadow;
    private Text                     title;
    private Text                     unit;
    private Text                     value;
    private DropShadow               valueBlendBottomShadow;
    private InnerShadow              valueBlendTopShadow;
    private Blend                    blend;
    private double                   angleStep;
    private Timeline                 timeline;
    private double                   interactiveAngle;
    private EventHandler<MouseEvent> mouseEventHandler;
    private EventHandler<TouchEvent> touchEventHandler;
    private List<Node>               markersToRemove;
    private Color                    barColor;
    private ConicalGradient          barGradient;
    private Instant                  lastCall;
    private boolean                  withinSpeedLimit;


    // ******************** Constructors **************************************
    public RadialBargraphSkin(RadialBargraph radialBargraph) {
        super(radialBargraph);
        angleStep         = radialBargraph.getAngleRange() / (radialBargraph.getMaxValue() - radialBargraph.getMinValue());
        angle             = new SimpleDoubleProperty(this, "angle", getSkinnable().getValue() * angleStep);
        lastCall          = Instant.now();
        withinSpeedLimit  = true;
        timeline          = new Timeline();
        mouseEventHandler = mouseEvent -> handleMouseEvent(mouseEvent);
        touchEventHandler = touchEvent -> handleTouchEvent(touchEvent);
        markersToRemove   = new ArrayList<>();

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
        Font.loadFont(getClass().getResourceAsStream("/eu/hansolo/enzo/fonts/opensans-semibold.ttf"), (0.06 * PREFERRED_HEIGHT)); // "OpenSans"

        barColor    = getSkinnable().getBarColor();
        barGradient = new ConicalGradient(new Stop(0.0, Color.TRANSPARENT),
                                          new Stop(1.0, Color.TRANSPARENT));

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

        blend = new Blend();
        blend.setMode(BlendMode.MULTIPLY);
        blend.setBottomInput(valueBlendBottomShadow);
        blend.setTopInput(valueBlendTopShadow);

        background = new Region();
        background.getStyleClass().setAll("background");

        ticksAndSectionsCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        ticksAndSections = ticksAndSectionsCanvas.getGraphicsContext2D();

        minMeasuredValue = new Region();
        minMeasuredValue.getStyleClass().setAll("min-measured-value");
        minMeasuredValueRotate = new Rotate(180 - getSkinnable().getStartAngle());
        minMeasuredValue.getTransforms().setAll(minMeasuredValueRotate);
        minMeasuredValue.setOpacity(getSkinnable().isMinMeasuredValueVisible() ? 1 : 0);
        minMeasuredValue.setManaged(getSkinnable().isMinMeasuredValueVisible());

        maxMeasuredValue = new Region();
        maxMeasuredValue.getStyleClass().setAll("max-measured-value");
        maxMeasuredValueRotate = new Rotate(180 - getSkinnable().getStartAngle());
        maxMeasuredValue.getTransforms().setAll(maxMeasuredValueRotate);
        maxMeasuredValue.setOpacity(getSkinnable().isMaxMeasuredValueVisible() ? 1 : 0);
        maxMeasuredValue.setManaged(getSkinnable().isMaxMeasuredValueVisible());

        threshold = new Region();
        threshold.getStyleClass().setAll("threshold");
        thresholdRotate = new Rotate(180 - getSkinnable().getStartAngle());
        threshold.getTransforms().setAll(thresholdRotate);
        threshold.setOpacity(getSkinnable().isThresholdVisible() ? 1 : 0);
        threshold.setManaged(getSkinnable().isThresholdVisible());
        thresholdExceeded = false;

        bar = new Arc();
        bar.setType(ArcType.ROUND);
        bar.setCenterX(PREFERRED_WIDTH * 0.5);
        bar.setCenterY(PREFERRED_HEIGHT * 0.5);
        bar.setRadiusX(PREFERRED_WIDTH * 0.5 - 4);
        bar.setRadiusY(PREFERRED_HEIGHT * 0.5 - 4);
        bar.setStartAngle(getSkinnable().getStartAngle() - 90);
        bar.setLength(0);
        bar.setStrokeType(StrokeType.CENTERED);
        bar.setStroke(null);
        bar.setFill(new RadialGradient(0, 0,
                                       PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5,
                                       PREFERRED_WIDTH * 0.45, false, CycleMethod.NO_CYCLE,
                                       new Stop(0.0, barColor),
                                       new Stop(0.76, barColor.deriveColor(-5, 1, 1, 1)), // -5 for on the barColorHue)
                                       new Stop(0.79, barColor),
                                       new Stop(0.97, barColor),
                                       new Stop(1.0, barColor.deriveColor(-5, 1, 1, 1)))); // -5 for on the barColorHue)

        knob = new Region();
        knob.setPickOnBounds(false);
        knob.getStyleClass().setAll("knob");

        dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.25));
        dropShadow.setBlurType(BlurType.TWO_PASS_BOX);
        dropShadow.setRadius(0.015 * PREFERRED_WIDTH);
        dropShadow.setOffsetY(0.015 * PREFERRED_WIDTH);

        title = new Text(getSkinnable().getTitle());
        title.setMouseTransparent(true);
        title.setTextOrigin(VPos.CENTER);
        title.getStyleClass().setAll("title");
        title.setEffect(getSkinnable().isPlainValue() ? null : blend);

        unit = new Text(getSkinnable().getUnit());
        unit.setMouseTransparent(true);
        unit.setTextOrigin(VPos.CENTER);
        unit.getStyleClass().setAll("unit");
        unit.setEffect(getSkinnable().isPlainValue() ? null : blend);

        value = new Text();
        value.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getValue()));
        value.setMouseTransparent(true);
        value.setTextOrigin(VPos.CENTER);
        value.getStyleClass().setAll("value");
        value.setEffect(getSkinnable().isPlainValue() ? null : blend);

        // Set initial value
        double range       = (getSkinnable().getMaxValue() - getSkinnable().getMinValue());
        double angleRange  = getSkinnable().getAngleRange();
        angleStep          = angleRange / range;
        double targetAngle = getSkinnable().getValue() * angleStep;
        angle.set(targetAngle);
        double currentValue = angle.get() / angleStep;

        value.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", currentValue));
        value.setTranslateX((size - value.getLayoutBounds().getWidth()) * 0.5);
        bar.setLength(-currentValue * angleStep);

        // Add all nodes
        pane = new Pane();
        pane.getChildren().setAll(background,
                                  bar,
                                  ticksAndSectionsCanvas,
                                  minMeasuredValue,
                                  maxMeasuredValue,
                                  threshold,
                                  knob,
                                  title,
                                  unit,
                                  value);

        pane.getChildren().addAll(getSkinnable().getMarkers().keySet());

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().heightProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().valueProperty().addListener(observable -> handleControlPropertyChanged("VALUE"));
        getSkinnable().minValueProperty().addListener(observable -> handleControlPropertyChanged("RECALC"));
        getSkinnable().maxValueProperty().addListener(observable -> handleControlPropertyChanged("RECALC"));
        getSkinnable().minMeasuredValueProperty().addListener(observable -> handleControlPropertyChanged("MIN_MEASURED_VALUE"));
        getSkinnable().minMeasuredValueVisibleProperty().addListener(observable -> handleControlPropertyChanged("MIN_MEASURED_VALUE_VISIBLE"));
        getSkinnable().maxMeasuredValueProperty().addListener(observable -> handleControlPropertyChanged("MAX_MEASURED_VALUE"));
        getSkinnable().maxMeasuredValueVisibleProperty().addListener(observable -> handleControlPropertyChanged("MAX_MEASURED_VALUE_VISIBLE"));
        getSkinnable().barColorProperty().addListener(observable -> handleControlPropertyChanged("BAR_COLOR"));
        getSkinnable().animatedProperty().addListener(observable -> handleControlPropertyChanged("ANIMATED"));
        getSkinnable().thresholdProperty().addListener(observable -> handleControlPropertyChanged("THRESHOLD"));
        getSkinnable().thresholdVisibleProperty().addListener(observable -> handleControlPropertyChanged("THRESHOLD_VISIBLE"));
        getSkinnable().angleRangeProperty().addListener(observable -> handleControlPropertyChanged("ANGLE_RANGE"));
        getSkinnable().numberFormatProperty().addListener(observable -> handleControlPropertyChanged("RECALC"));
        getSkinnable().plainValueProperty().addListener(observable -> handleControlPropertyChanged("PLAIN_VALUE"));
        getSkinnable().interactiveProperty().addListener(observable -> handleControlPropertyChanged("INTERACTIVE"));
        getSkinnable().getSections().addListener((ListChangeListener<Section>) change -> handleControlPropertyChanged("CANVAS_REFRESH"));
        getSkinnable().getMarkers().addListener((MapChangeListener<Marker, Rotate>) change -> handleControlPropertyChanged("MARKER"));
        getSkinnable().barGradientProperty().addListener((ListChangeListener<Stop>) change -> handleControlPropertyChanged("BAR_GRADIENT"));
        getSkinnable().barGradientEnabledProperty().addListener(observable -> handleControlPropertyChanged("BAR_COLOR"));

        angle.addListener(observable -> handleControlPropertyChanged("ANGLE"));

        knob.setOnMousePressed(event -> getSkinnable().setInteractive(!getSkinnable().isInteractive()));

        minMeasuredValue.setOnMousePressed(mouseEventHandler);
        minMeasuredValue.setOnMouseReleased(mouseEventHandler);

        minMeasuredValue.setOnTouchPressed(touchEventHandler);
        minMeasuredValue.setOnTouchReleased(touchEventHandler);

        maxMeasuredValue.setOnMousePressed(mouseEventHandler);
        maxMeasuredValue.setOnMouseReleased(mouseEventHandler);

        maxMeasuredValue.setOnTouchPressed(touchEventHandler);
        maxMeasuredValue.setOnTouchReleased(touchEventHandler);

        threshold.setOnMousePressed(mouseEventHandler);
        threshold.setOnMouseDragged(mouseEventHandler);
        threshold.setOnMouseReleased(mouseEventHandler);

        threshold.setOnTouchPressed(touchEventHandler);
        threshold.setOnTouchMoved(touchEventHandler);
        threshold.setOnTouchReleased(touchEventHandler);

        for (Marker marker : getSkinnable().getMarkers().keySet()) {
            marker.setOnMousePressed(mouseEventHandler);
            marker.setOnMouseDragged(mouseEventHandler);
            marker.setOnMouseReleased(mouseEventHandler);

            marker.setOnTouchPressed(touchEventHandler);
            marker.setOnTouchMoved(touchEventHandler);
            marker.setOnTouchReleased(touchEventHandler);
        }
    }


    // ******************** Methods *******************************************
    protected void handleControlPropertyChanged(final String PROPERTY) {
        if ("RESIZE".equals(PROPERTY)) {
            resize();
        } else if ("VALUE".equals(PROPERTY)) {
            withinSpeedLimit = !(Instant.now().minusMillis((long) getSkinnable().getAnimationDuration()).isBefore(lastCall));
            lastCall         = Instant.now();
            setBar();
        } else if ("RECALC".equals(PROPERTY)) {
            angleStep = getSkinnable().getAngleRange() / (getSkinnable().getMaxValue() - getSkinnable().getMinValue());
            resize();
        } else if ("ANGLE".equals(PROPERTY)) {
            if (getSkinnable().isInteractive()) return;

            double currentValue = angle.get() / angleStep;

            value.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", currentValue));
            value.setTranslateX((size - value.getLayoutBounds().getWidth()) * 0.5);
            bar.setLength(-currentValue * angleStep);

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
                minMeasuredValueRotate.setAngle(currentValue * angleStep - 180 - getSkinnable().getStartAngle());
            }
            if (currentValue > getSkinnable().getMaxMeasuredValue()) {
                getSkinnable().setMaxMeasuredValue(currentValue);
                maxMeasuredValueRotate.setAngle(currentValue * angleStep - 180 - getSkinnable().getStartAngle());
            }
        } else if ("PLAIN_VALUE".equals(PROPERTY)) {
            value.setEffect(getSkinnable().isPlainValue() ? null : blend);
        } else if ("INTERACTIVE".equals(PROPERTY)) {
            if (getSkinnable().isInteractive()) {
                unit.setText("Interactive");
                value.setText("");
                resizeText();
            } else {
                unit.setText(getSkinnable().getUnit());
                value.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", (angle.get() / angleStep)));
                resizeText();
            }
        } else if ("CANVAS_REFRESH".equals(PROPERTY)) {
            ticksAndSections.clearRect(0, 0, size, size);
            drawSections(ticksAndSections);
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
                // Add TouchEvent handler
                marker.setOnTouchPressed(touchEventHandler);
                marker.setOnTouchMoved(touchEventHandler);
                marker.setOnTouchReleased(touchEventHandler);
            }
            drawMarkers();
        } else if ("BAR_COLOR".equals(PROPERTY)) {
            barColor = getSkinnable().getBarColor();
            resize();
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
                node.removeEventHandler(TouchEvent.TOUCH_PRESSED, touchEventHandler);
                node.removeEventHandler(TouchEvent.TOUCH_MOVED, touchEventHandler);
                node.removeEventHandler(TouchEvent.TOUCH_RELEASED, touchEventHandler);
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
                unit.setText("Threshold");
                value.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getThreshold()));
                resizeText();
            } else if (MouseEvent.MOUSE_DRAGGED == TYPE) {
				touchRotate(MOUSE_EVENT.getSceneX() - getSkinnable().getParent().getLayoutX() - getSkinnable().getLayoutX(), MOUSE_EVENT.getSceneY() - getSkinnable().getParent().getLayoutY() - getSkinnable().getLayoutY(), thresholdRotate);
            } else if (MouseEvent.MOUSE_RELEASED == TYPE) {
                getSkinnable().setThreshold(Double.parseDouble(value.getText()));
                fadeBackToInteractive();
            }
        } else if (getSkinnable().isInteractive() && SRC instanceof Marker) {
            if (MouseEvent.MOUSE_PRESSED == TYPE) {
                unit.setText(((Marker) SRC).getText());
                value.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", ((Marker) SRC).getValue()));
                resizeText();
            } else if (MouseEvent.MOUSE_DRAGGED == TYPE) {
				touchRotate(MOUSE_EVENT.getSceneX() - getSkinnable().getParent().getLayoutX() - getSkinnable().getLayoutX(), MOUSE_EVENT.getSceneY() - getSkinnable().getParent().getLayoutY() - getSkinnable().getLayoutY(), getSkinnable().getMarkers().get(SRC));
            } else if (MouseEvent.MOUSE_RELEASED == TYPE) {
                ((Marker) SRC).setValue(Double.parseDouble(value.getText()));
                fadeBackToInteractive();
            }
        } else if (getSkinnable().isInteractive() && SRC.equals(minMeasuredValue)) {
            if (MouseEvent.MOUSE_PRESSED == TYPE) {
                unit.setText("Min");
                value.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getMinMeasuredValue()));
                resizeText();
            } else if (MouseEvent.MOUSE_RELEASED == TYPE) {
                fadeBackToInteractive();
            }
        } else if (getSkinnable().isInteractive() && SRC.equals(maxMeasuredValue)) {
            if (MouseEvent.MOUSE_PRESSED == TYPE) {
                unit.setText("Max");
                value.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getMaxMeasuredValue()));
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
                unit.setText("Threshold");
                value.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getThreshold()));
                resizeText();
            } else if (TouchEvent.TOUCH_MOVED == TYPE) {
				touchRotate(TOUCH_EVENT.getTouchPoint().getSceneX() - getSkinnable().getParent().getLayoutX() - getSkinnable().getLayoutX(), TOUCH_EVENT.getTouchPoint().getSceneY() - getSkinnable().getParent().getLayoutY() - getSkinnable().getLayoutY(), thresholdRotate);
            } else if (TouchEvent.TOUCH_RELEASED == TYPE) {
                getSkinnable().setThreshold(Double.parseDouble(value.getText()));
                fadeBackToInteractive();
            }
        } else if (SRC instanceof Marker) {
            if (TouchEvent.TOUCH_PRESSED == TYPE) {
                unit.setText(((Marker) SRC).getText());
                value.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", ((Marker) SRC).getValue()));
                resizeText();
            } else if (TouchEvent.TOUCH_MOVED == TYPE) {
				touchRotate(TOUCH_EVENT.getTouchPoint().getSceneX() - getSkinnable().getParent().getLayoutX() - getSkinnable().getLayoutX(), TOUCH_EVENT.getTouchPoint().getSceneY() - getSkinnable().getParent().getLayoutY() - getSkinnable().getLayoutY(), getSkinnable().getMarkers().get(SRC));
            } else if (TouchEvent.TOUCH_RELEASED == TYPE) {
                ((Marker) SRC).setValue(Double.parseDouble(value.getText()));
                fadeBackToInteractive();
            }
        } else if (SRC.equals(minMeasuredValue)) {
            if (TouchEvent.TOUCH_PRESSED == TYPE) {
                unit.setText("Min");
                value.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getMinMeasuredValue()));
                resizeText();
            } else if (TouchEvent.TOUCH_RELEASED == TYPE) {
                fadeBackToInteractive();
            }
        } else if (SRC.equals(maxMeasuredValue)) {
            if (TouchEvent.TOUCH_PRESSED == TYPE) {
                unit.setText("Max");
                value.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getMaxMeasuredValue()));
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
            (interactiveAngle + 180.0 + getSkinnable().getStartAngle() - 360) / angleStep :
            (interactiveAngle - 180.0 + getSkinnable().getStartAngle() - 360) / angleStep;
        if (Double.compare(newValue, getSkinnable().getMinValue()) >= 0 && Double.compare(newValue, getSkinnable().getMaxValue()) <= 0) {
            ROTATE.setAngle(interactiveAngle);
            value.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", newValue));
            resizeText();
        }

    }

    private void fadeBackToInteractive() {
        FadeTransition fadeUnitOut = new FadeTransition(Duration.millis(425), unit);
        fadeUnitOut.setFromValue(1.0);
        fadeUnitOut.setToValue(0.0);
        FadeTransition fadeValueOut = new FadeTransition(Duration.millis(425), value);
        fadeValueOut.setFromValue(1.0);
        fadeValueOut.setToValue(0.0);

        PauseTransition pause = new PauseTransition(Duration.millis(50));

        FadeTransition fadeUnitIn = new FadeTransition(Duration.millis(425), unit);
        fadeUnitIn.setFromValue(0.0);
        fadeUnitIn.setToValue(1.0);
        FadeTransition fadeValueIn = new FadeTransition(Duration.millis(425), value);
        fadeValueIn.setFromValue(0.0);
        fadeValueIn.setToValue(1.0);
        ParallelTransition parallelIn = new ParallelTransition(fadeUnitIn, fadeValueIn);

        ParallelTransition parallelOut = new ParallelTransition(fadeUnitOut, fadeValueOut);
        parallelOut.setOnFinished(event -> {
            unit.setText("Interactive");
            value.setText("");
            resizeText();
        });

        SequentialTransition sequence = new SequentialTransition(parallelOut, pause, parallelIn);
        sequence.play();
    }

    private void setBar() {
        double range       = (getSkinnable().getMaxValue() - getSkinnable().getMinValue());
        double angleRange  = getSkinnable().getAngleRange();
        angleStep          = angleRange / range;
        double targetAngle = getSkinnable().getValue() * angleStep;

        if (withinSpeedLimit && getSkinnable().isAnimated()) {
            timeline.stop();
            final KeyValue KEY_VALUE = new KeyValue(angle, targetAngle, Interpolator.SPLINE(0.5, 0.4, 0.4, 1.0));
            final KeyFrame KEY_FRAME = new KeyFrame(Duration.millis(getSkinnable().getAnimationDuration()), KEY_VALUE);
            timeline.getKeyFrames().setAll(KEY_FRAME);
            timeline.play();
        } else {
            angle.set(targetAngle);
        }
    }

    private void drawTickMarks(final GraphicsContext CTX) {
        double  sinValue;
        double  cosValue;
        double  startAngle = getSkinnable().getStartAngle();
        Point2D center     = new Point2D(size * 0.5, size * 0.5);
        for (double angle = 0, counter = getSkinnable().getMinValue() ; Double.compare(counter, getSkinnable().getMaxValue()) <= 0 ; angle -= angleStep, counter++) {
            sinValue = Math.sin(Math.toRadians(angle + startAngle));
            cosValue = Math.cos(Math.toRadians(angle + startAngle));

            Point2D innerPoint = new Point2D(center.getX() + size * 0.388 * sinValue, center.getY() + size * 0.388 * cosValue);
            Point2D outerPoint = new Point2D(center.getX() + size * 0.485 * sinValue, center.getY() + size * 0.485 * cosValue);

            CTX.setStroke(getSkinnable().getTickMarkFill());
            if (counter % getSkinnable().getMinorTickSpace() == 0) {
                CTX.setLineWidth(size * 0.0035);
                CTX.strokeLine(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
            }
        }
    }
    
    private final void drawSections(final GraphicsContext CTX) {
        final double xy        = (size - 0.87 * size) * 0.5;
        final double wh        = size * 0.87;
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
                    CTX.setLineWidth(size * 0.1);
                    CTX.setLineCap(StrokeLineCap.BUTT);
                    CTX.strokeArc(xy, xy, wh, wh, -(OFFSET + SECTION_START_ANGLE), -SECTION_ANGLE_EXTEND, ArcType.OPEN);
                    CTX.restore();
                }
            }
        );
    }
         
    private final void drawMarkers() {
        for (Marker marker : getSkinnable().getMarkers().keySet()) {
            marker.setPrefSize(0.05 * size, 0.05 * size);
            marker.relocate((size - marker.getPrefWidth()) * 0.5, size * 0.04);
            getSkinnable().getMarkers().get(marker).setPivotX(marker.getPrefWidth() * 0.5);
            getSkinnable().getMarkers().get(marker).setPivotY(size * 0.46);
            getSkinnable().getMarkers().get(marker).setAngle(marker.getValue() * angleStep - 180 - getSkinnable().getStartAngle());
        }
    }

    private void recalculateBarGradient() {
        double angleFactor = 1d / 360d;
        double emptyRange  = 360d - getSkinnable().getAngleRange();
        double offset      = angleFactor * ((360 - getSkinnable().getStartAngle() + 180 - emptyRange * 0.5) % 360);
        List<Stop> stops   = new LinkedList<>();

        double emptyOffset = (emptyRange * 0.5) * angleFactor;

        double minFraction = 1.0;
        double maxFraction = 0.0;
        Color  minFractionColor = Color.TRANSPARENT;
        Color  maxFractionColor = Color.TRANSPARENT;

        for (Stop stop : getSkinnable().getBarGradient()) {
            double fraction = stop.getOffset();
            if (fraction < minFraction) {
                minFraction      = fraction;
                minFractionColor = stop.getColor();
            }
            if (fraction > maxFraction) {
                maxFraction      = fraction;
                maxFractionColor = stop.getColor();
            }
        }
        stops.add(new Stop(0d, minFractionColor));
        stops.add(new Stop(0d + emptyOffset, minFractionColor));
        stops.add(new Stop(1d - emptyOffset, maxFractionColor));
        stops.add(new Stop(1d, maxFractionColor));
        if (getSkinnable().getBarGradient().size() == 2) {
            stops.add(new Stop((maxFraction - minFraction) * 0.5, (Color) Interpolator.LINEAR.interpolate(minFractionColor, maxFractionColor, 0.5)));
        }
        for (Stop stop : getSkinnable().getBarGradient()) {
            if (Double.compare(stop.getOffset(), minFraction) == 0 || Double.compare(stop.getOffset(), maxFraction) == 0) continue;
            stops.add(stop);
        }
        barGradient = new ConicalGradient(new Point2D(size * 0.5, size * 0.5), offset, stops);
    }

    private void resizeText() {
        title.setFont(Fonts.robotoMedium(size * 0.1));
        title.setTranslateX((size - title.getLayoutBounds().getWidth()) * 0.5);
        title.setTranslateY(size * 0.3);

        unit.setFont(Fonts.robotoMedium(size * 0.1));
        unit.setTranslateX((size - unit.getLayoutBounds().getWidth()) * 0.5);
        unit.setTranslateY(size * 0.7);

        value.setFont(Fonts.robotoBold(size * 0.25));
        value.setTranslateX((size - value.getLayoutBounds().getWidth()) * 0.5);
        value.setTranslateY(size * 0.5);
    }

    private void resize() {
        size = getSkinnable().getWidth() < getSkinnable().getHeight() ? getSkinnable().getWidth() : getSkinnable().getHeight();
        centerX = size * 0.5;
        centerY = size * 0.5;

        if (size > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((getSkinnable().getWidth() - size) * 0.5, (getSkinnable().getHeight() - size) * 0.5);
            
            final double RADIUS = size * 0.5 - 2;
    
            valueBlendBottomShadow.setOffsetY(0.005 * size);
    
            valueBlendTopShadow.setOffsetY(0.005 * size);
            valueBlendTopShadow.setRadius(0.005 * size);
    
            dropShadow.setRadius(0.015 * size);
            dropShadow.setOffsetY(0.015 * size);
    
            background.setPrefSize(size, size);
            
            ticksAndSectionsCanvas.setWidth(size);
            ticksAndSectionsCanvas.setHeight(size);
            ticksAndSections.clearRect(0, 0, size, size);
            drawSections(ticksAndSections);
            drawTickMarks(ticksAndSections);
            ticksAndSectionsCanvas.setCache(true);
            ticksAndSectionsCanvas.setCacheHint(CacheHint.QUALITY);
    
            drawMarkers();
    
            minMeasuredValue.setPrefSize(0.03 * size, 0.03 * size);
            minMeasuredValue.relocate((size - minMeasuredValue.getPrefWidth()) * 0.5, size * 0.11);
            minMeasuredValueRotate.setPivotX(minMeasuredValue.getPrefWidth() * 0.5);
            minMeasuredValueRotate.setPivotY(size * 0.39);
            minMeasuredValueRotate.setAngle(getSkinnable().getMinMeasuredValue() * angleStep - 180 - getSkinnable().getStartAngle());
    
            maxMeasuredValue.setPrefSize(0.03 * size, 0.03 * size);
            maxMeasuredValue.relocate((size - maxMeasuredValue.getPrefWidth()) * 0.5, size * 0.11);
            maxMeasuredValueRotate.setPivotX(maxMeasuredValue.getPrefWidth() * 0.5);
            maxMeasuredValueRotate.setPivotY(size * 0.39);
            maxMeasuredValueRotate.setAngle(getSkinnable().getMaxMeasuredValue() * angleStep - 180 - getSkinnable().getStartAngle());
    
            threshold.setPrefSize(0.06 * size, 0.055 * size);
            threshold.relocate((size - threshold.getPrefWidth()) * 0.5, size * 0.08);
            thresholdRotate.setPivotX(threshold.getPrefWidth() * 0.5);
            thresholdRotate.setPivotY(size * 0.42);
            thresholdRotate.setAngle(getSkinnable().getThreshold() * angleStep - 180 - getSkinnable().getStartAngle());

            bar.setCenterX(centerX);
            bar.setCenterY(centerY);
            bar.setRadiusX(RADIUS);
            bar.setRadiusY(RADIUS);
    
            if (getSkinnable().isBarGradientEnabled()) {
                recalculateBarGradient();
                Image image = barGradient.getImage(size, size);
                bar.setFill(new ImagePattern(image, 0, 0, size, size, false));
            } else {
                bar.setFill(new RadialGradient(0, 0,
                                               centerX, centerY,
                                               RADIUS, false, CycleMethod.NO_CYCLE,
                                               new Stop(0.0, barColor),
                                               new Stop(0.76, barColor.deriveColor(-5, 1, 1, 1)), // -5 for on the barColorHue)
                                               new Stop(0.79, barColor),
                                               new Stop(0.97, barColor),
                                               new Stop(1.0, barColor.deriveColor(-5, 1, 1, 1)))); // -5 for on the barColorHue)
            }
            knob.setPrefSize(size * 0.75, size * 0.75);
            knob.setTranslateX((size - knob.getPrefWidth()) * 0.5);
            knob.setTranslateY((size - knob.getPrefHeight()) * 0.5);
    
            resizeText();
        }
    }
}
