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
import eu.hansolo.enzo.gauge.RadialSteelGauge;
import javafx.animation.*;
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
import javafx.scene.effect.*;
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
 * Created by hansolo on 21.07.14.
 */
public class RadialSteelGaugeSkin extends SkinBase<RadialSteelGauge> implements Skin<RadialSteelGauge> {
    private static final double      PREFERRED_WIDTH  = 250;
    private static final double      PREFERRED_HEIGHT = 250;
    private static final double      MINIMUM_WIDTH    = 50;
    private static final double      MINIMUM_HEIGHT   = 50;
    private static final double      MAXIMUM_WIDTH    = 1024;
    private static final double      MAXIMUM_HEIGHT   = 1024;
    private double                   oldValue;
    private double                   size;
    private Pane                     pane;
    private Region                   background;
    private Canvas                   ticksAndSectionsCanvas;
    private GraphicsContext          ticksAndSections;
    private Region                   needle;
    private Region                   needleHighlight;
    private Rotate                   needleRotate;
    private Region                   knob;
    private Group                    shadowGroup;
    private DropShadow               dropShadow;
    private Text                     titleText;
    private Text                     unitText;
    private Text                     valueText;
    private double                   angleStep;
    private Timeline                 timeline;
    private String                   limitString;
    private Instant                  lastCall;
    private boolean                  withinSpeedLimit;
    private double                   interactiveAngle;


    // ******************** Constructors **************************************
    public RadialSteelGaugeSkin(RadialSteelGauge gauge) {
        super(gauge);
        angleStep        = gauge.getAngleRange() / (gauge.getMaxValue() - gauge.getMinValue());
        timeline         = new Timeline();
        oldValue         = getSkinnable().getValue();
        limitString      = "";
        lastCall         = Instant.now();
        withinSpeedLimit = true;

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
        background = new Region();
        background.getStyleClass().setAll("background");

        ticksAndSectionsCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        ticksAndSections = ticksAndSectionsCanvas.getGraphicsContext2D();

        needle = new Region();
        needle.getStyleClass().setAll(Gauge.STYLE_CLASS_NEEDLE_STANDARD);
        needleRotate = new Rotate(180 - getSkinnable().getStartAngle());
        needleRotate.setAngle(needleRotate.getAngle() + (getSkinnable().getValue() - oldValue - getSkinnable().getMinValue()) * angleStep);
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

        shadowGroup = new Group(needle, needleHighlight, knob);
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

        // Set initial value
        angleStep          = getSkinnable().getAngleRange() / (getSkinnable().getMaxValue() - getSkinnable().getMinValue());
        double targetAngle = 180 - getSkinnable().getStartAngle() + (getSkinnable().getValue() - getSkinnable().getMinValue()) * angleStep;
        targetAngle        = clamp(180 - getSkinnable().getStartAngle(), 180 - getSkinnable().getStartAngle() + getSkinnable().getAngleRange(), targetAngle);
        needleRotate.setAngle(targetAngle);

        // Add all nodes
        pane = new Pane();
        pane.getChildren().setAll(background,
                                  ticksAndSectionsCanvas,
                                  titleText,
                                  shadowGroup,
                                  unitText,
                                  valueText);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().heightProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().tickLabelOrientationProperty().addListener(observable -> handleControlPropertyChanged("CANVAS_REFRESH"));
        getSkinnable().tickLabelFillProperty().addListener(observable -> handleControlPropertyChanged("CANVAS_REFRESH"));
        getSkinnable().tickMarkFillProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().needleColorProperty().addListener(observable -> handleControlPropertyChanged("NEEDLE_COLOR"));
        getSkinnable().animatedProperty().addListener(observable -> handleControlPropertyChanged("ANIMATED"));
        getSkinnable().angleRangeProperty().addListener(observable -> handleControlPropertyChanged("ANGLE_RANGE"));
        getSkinnable().numberFormatProperty().addListener(observable -> handleControlPropertyChanged("RECALC"));
        getSkinnable().dropShadowEnabledProperty().addListener(observable -> handleControlPropertyChanged("DROP_SHADOW"));

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

        needleRotate.angleProperty().addListener(observable -> handleControlPropertyChanged("ANGLE"));
    }


    // ******************** Methods *******************************************
    protected void handleControlPropertyChanged(final String PROPERTY) {
        if ("RESIZE".equals(PROPERTY)) {
            resize();
        } else if ("ANGLE".equals(PROPERTY)) {
            double currentValue = (needleRotate.getAngle() + getSkinnable().getStartAngle() - 180) / angleStep + getSkinnable().getMinValue();
            valueText.setText(limitString + String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", currentValue));
            valueText.setTranslateX((size - valueText.getLayoutBounds().getWidth()) * 0.5);
        } else if ("DROP_SHADOW".equals(PROPERTY)) {
            shadowGroup.setEffect(getSkinnable().isDropShadowEnabled() ? dropShadow : null);
        } else if ("CANVAS_REFRESH".equals(PROPERTY)) {
            ticksAndSections.clearRect(0, 0, size, size);
            drawTickMarks(ticksAndSections);
        }
    }


    // ******************** Private Methods ***********************************
    private void rotateNeedle() {
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

    private void drawTickMarks(final GraphicsContext CTX) {
        double  sinValue;
        double  cosValue;
        double  startAngle = getSkinnable().getStartAngle();
        double  orthText   = RadialSteelGauge.TickLabelOrientation.ORTHOGONAL == getSkinnable().getTickLabelOrientation() ? 0.33 : 0.31;
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

    private double clamp(final double MIN_VALUE, final double MAX_VALUE, final double VALUE) {
        if (VALUE < MIN_VALUE) return MIN_VALUE;
        if (VALUE > MAX_VALUE) return MAX_VALUE;
        return VALUE;
    }

    private void resizeText() {
        titleText.setFont(Fonts.robotoMedium(size * 0.06));
        titleText.setTranslateX((size - titleText.getLayoutBounds().getWidth()) * 0.5);
        titleText.setTranslateY(size * 0.35);

        unitText.setFont(Fonts.robotoMedium(size * 0.06));
        unitText.setTranslateX((size - unitText.getLayoutBounds().getWidth()) * 0.5);
        unitText.setTranslateY(size * 0.65);

        valueText.setFont(Fonts.robotoBold(size * 0.1));
        valueText.setTranslateX((size - valueText.getLayoutBounds().getWidth()) * 0.5);
        valueText.setTranslateY(size * 0.85);
    }

    private void resize() {
        size = getSkinnable().getWidth() < getSkinnable().getHeight() ? getSkinnable().getWidth() : getSkinnable().getHeight();

        if (size > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((getSkinnable().getWidth() - size) * 0.5, (getSkinnable().getHeight() - size) * 0.5);

            dropShadow.setRadius(0.01 * size);
            dropShadow.setOffsetY(0.01 * size);

            background.setPrefSize(size, size);
            // TODO: Remove this CSS inlining workaround by using % in -fx-background-insets
            background.setStyle("-fx-background-insets: 0, " + 0.02 * size + ", " + 0.022 * size + ", " + 0.038 * size + ";");

            ticksAndSectionsCanvas.setWidth(size);
            ticksAndSectionsCanvas.setHeight(size);
            ticksAndSections.clearRect(0, 0, size, size);
            drawTickMarks(ticksAndSections);
            ticksAndSectionsCanvas.setCache(true);
            ticksAndSectionsCanvas.setCacheHint(CacheHint.QUALITY);

            needle.setPrefSize(size * 0.04, size * 0.425);
            needle.relocate((size - needle.getPrefWidth()) * 0.5, size * 0.5 - needle.getPrefHeight());
            needleRotate.setPivotX(needle.getPrefWidth() * 0.5);
            needleRotate.setPivotY(needle.getPrefHeight());

            needleHighlight.setPrefSize(size * 0.04, size * 0.425);
            needleHighlight.setTranslateX((size - needle.getPrefWidth()) * 0.5);
            needleHighlight.setTranslateY(size * 0.5 - needle.getPrefHeight());

            knob.setPrefSize(size * 0.15, size * 0.15);
            knob.setTranslateX((size - knob.getPrefWidth()) * 0.5);
            knob.setTranslateY((size - knob.getPrefHeight()) * 0.5);

            resizeText();
        }
    }
}
