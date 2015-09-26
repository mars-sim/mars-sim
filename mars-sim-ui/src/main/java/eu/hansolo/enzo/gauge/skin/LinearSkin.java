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
import eu.hansolo.enzo.gauge.Linear;
import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.time.Instant;
import java.util.Locale;


public class LinearSkin extends SkinBase<Linear> implements Skin<Linear> {
    private static final double MINIMUM_WIDTH    = 140;
    private static final double MINIMUM_HEIGHT   = 140;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private static final double PREFERRED_WIDTH  = 140;
    private static final double PREFERRED_HEIGHT = 350;
    private static final double ASPECT_RATIO     = PREFERRED_HEIGHT / PREFERRED_WIDTH;
    private double         size;
    private double         width;
    private double         height;
    private double         stepSize;
    private DoubleProperty currentValue;
    private Pane           pane;

    private Region          background;
    private Canvas          ticksAndSectionsCanvas;
    private GraphicsContext ticksAndSections;
    private Text            unitText;
    private Text            titleText;
    private Region          ledFrame;
    private Region          ledMain;
    private Region          ledHl;
    private Label           lcdText;
    private Region          barBackground;
    private Path            barBackgroundBorder;
    private MoveTo          barBackgroundBorderStart1;
    private LineTo          barBackgroundBorderStop1;
    private MoveTo          barBackgroundBorderStart2;
    private LineTo          barBackgroundBorderStop2;
    private Region          bar;
    private Region          foreground;
    private InnerShadow     innerShadow;
    private DropShadow      glow;
    private Timeline        timeline;
    private Instant         lastCall;
    private boolean         withinSpeedLimit;


    // ******************** Constructors **************************************
    public LinearSkin(final Linear CONTROL) {
        super(CONTROL);
        width            = PREFERRED_WIDTH;
        height           = PREFERRED_HEIGHT;
        size             = PREFERRED_WIDTH;
        timeline         = new Timeline();
        lastCall         = Instant.now();
        withinSpeedLimit = true;
        currentValue     = new DoublePropertyBase(getSkinnable().getValue()) {
            @Override public Object getBean() { return LinearSkin.this; }
            @Override public String getName() { return "currentValue"; }
        };

        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getSkinnable().getPrefWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getSkinnable().getWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getHeight(), 0.0) <= 0) {
            if (getSkinnable().getPrefWidth() > 0 && getSkinnable().getPrefHeight() > 0) {
                getSkinnable().setPrefSize(getSkinnable().getPrefWidth(), getSkinnable().getPrefHeight());
            } else {
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

        barBackground = new Region();
        barBackground.getStyleClass().setAll("bar-background");

        barBackgroundBorderStart1 = new MoveTo();
        barBackgroundBorderStop1  = new LineTo();
        barBackgroundBorderStart2 = new MoveTo();
        barBackgroundBorderStop2  = new LineTo();
        barBackgroundBorder       = new Path(barBackgroundBorderStart1, barBackgroundBorderStop1, barBackgroundBorderStart2, barBackgroundBorderStop2);
        barBackgroundBorder.getStyleClass().setAll("bar-background-border");

        ticksAndSectionsCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        ticksAndSections = ticksAndSectionsCanvas.getGraphicsContext2D();

        innerShadow = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 8, 0d, 0d, 0d);
        glow = new DropShadow(BlurType.TWO_PASS_BOX, getSkinnable().getLedColor(), 20, 0d, 0d, 0d);
        glow.setInput(innerShadow);

        ledFrame = new Region();
        ledFrame.getStyleClass().setAll("led-frame");

        ledMain = new Region();
        ledMain.getStyleClass().setAll("led-main");
        ledMain.setStyle("-led-color: " + (colorToCss(getSkinnable().getLedColor())) + ";");

        ledHl = new Region();
        ledHl.getStyleClass().setAll("led-hl");

        bar = new Region();
        bar.getStyleClass().setAll("bar");

        foreground = new Region();
        foreground.getStyleClass().setAll("foreground");

        titleText = new Text(getSkinnable().getTitle());
        titleText.getStyleClass().setAll("title");

        unitText = new Text(getSkinnable().getUnit());
        unitText.getStyleClass().setAll("unit");

        lcdText = new Label(getSkinnable().getNumberFormat().format(getSkinnable().getValue()));
        lcdText.getStyleClass().setAll("lcd-text");

        pane = new Pane();
        pane.getChildren().setAll(background,
                                  barBackground,
                                  barBackgroundBorder,
                                  ticksAndSectionsCanvas,
                                  titleText,
                                  unitText,
                                  lcdText,
                                  ledFrame,
                                  ledMain,
                                  ledHl,
                                  bar,
                                  foreground);

        getChildren().setAll(pane);
        resize();
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(observable -> handleControlPropertyChanged("RESIZE") );
        getSkinnable().heightProperty().addListener(observable -> handleControlPropertyChanged("RESIZE") );
        getSkinnable().ledOnProperty().addListener(observable -> ledMain.setEffect(getSkinnable().isLedOn() ? glow : innerShadow));
        getSkinnable().ledColorProperty().addListener(observable -> handleControlPropertyChanged("LED_COLOR"));
        getSkinnable().tickLabelFillProperty().addListener(observable -> handleControlPropertyChanged("CANVAS_REFRESH"));
        getSkinnable().tickMarkFillProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().barColorProperty().addListener(observable -> handleControlPropertyChanged("BAR_COLOR"));
        getSkinnable().animatedProperty().addListener(observable -> handleControlPropertyChanged("ANIMATED"));
        getSkinnable().numberFormatProperty().addListener(observable -> handleControlPropertyChanged("RECALC"));
        getSkinnable().unitProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().titleProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().orientationProperty().addListener(observable -> handleControlPropertyChanged("ORIENTATION"));
        getSkinnable().minValueProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().maxValueProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));

        getSkinnable().valueProperty().addListener((OV, OLD_VALUE, NEW_VALUE) -> {
            withinSpeedLimit = !(Instant.now().minusMillis((long) getSkinnable().getAnimationDuration()).isBefore(lastCall));
            lastCall         = Instant.now();
            updateBar();
        });
        currentValue.addListener(observable -> {
            if (Orientation.VERTICAL == getSkinnable().getOrientation()) {
                bar.setPrefSize(0.14286 * width, Math.abs(currentValue.get()) * stepSize);
                if (Double.compare(currentValue.get(), 0) >= 0) {
                    bar.setLayoutY(-currentValue.get() * stepSize);
                }
            } else {
                bar.setPrefSize(Math.abs(currentValue.get()) * stepSize, 0.14286 * height);
                if (Double.compare(currentValue.get(), 0) <= 0) {
                    bar.setLayoutX(-Math.abs(currentValue.get()) * stepSize);
                }
            }

            getSkinnable().setBlinking((currentValue.get() + getSkinnable().getMinValue()) > getSkinnable().getThreshold());

            lcdText.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", (currentValue.get() + getSkinnable().getMinValue())));
        });
    }


    // ******************** Methods *******************************************
    protected void handleControlPropertyChanged(final String PROPERTY) {
        if ("RESIZE".equals(PROPERTY)) {
            resize();
        } else if ("CANVAS_REFRESH".equals(PROPERTY)) {
            ticksAndSections.clearRect(0, 0, ticksAndSectionsCanvas.getWidth(), ticksAndSectionsCanvas.getHeight());
            drawTickMarks(ticksAndSections);
        } else if ("NEEDLE_COLOR".equals(PROPERTY)) {
            bar.setStyle("-bar-color: " + (colorToCss(getSkinnable().getBarColor())) + ";");
        } else if ("LED_COLOR".equals(PROPERTY)) {
            ledMain.setStyle("-led-color: " + (colorToCss(getSkinnable().getLedColor())));
            resize();
        } else if ("ORIENTATION".equals(PROPERTY)) {
            resize();
        }
    }

    private void drawTickMarks(final GraphicsContext CTX) {
        CTX.setFont(Fonts.robotoLight(0.06 * size));
        CTX.setStroke(getSkinnable().getTickMarkFill());
        CTX.setFill(getSkinnable().getTickLabelFill());

        double range = getSkinnable().getMaxValue() - getSkinnable().getMinValue();
        double minPosition;
        double maxPosition;
        double stepSize;
        Orientation orientation = getSkinnable().getOrientation();
        if (Orientation.VERTICAL == orientation) {
            minPosition = (1 - 0.67143) * height * 0.5;
            maxPosition = (1 - 0.67143) * height * 0.5 + 0.67143 * height;
            stepSize    = Math.abs(0.67143 * height / range);
        } else {
            minPosition = (1 - 0.75) * width * 0.5;
            maxPosition = (1 - 0.75) * width * 0.5 + 0.75 * width;
            stepSize    = Math.abs(0.75 * width / range);
        }

        double tickStart = minPosition;
        double tickStop  = maxPosition;

        if (stepSize == 0) return;

        Point2D innerMainPoint;
        Point2D innerMediumPoint;
        Point2D innerMinorPoint;
        Point2D outerPoint;
        Point2D textPoint;

        int tickCounter = 0;

        for (double counter = tickStart ; Double.compare(counter, tickStop + 1) <= 0 ; counter += stepSize) {
            if (Orientation.VERTICAL == orientation) {
                innerMainPoint   = new Point2D(0.36 * width, counter);
                innerMediumPoint = new Point2D(0.375 * width, counter);
                innerMinorPoint  = new Point2D(0.39 * width, counter);
                outerPoint       = new Point2D(0.41 * width, counter);
                textPoint        = new Point2D(0.34 * width, counter);
            } else {
                innerMainPoint   = new Point2D(counter, 0.64 * height);
                innerMediumPoint = new Point2D(counter, 0.625 * height);
                innerMinorPoint  = new Point2D(counter, 0.61 * height);
                outerPoint       = new Point2D(counter, 0.59 * height);
                textPoint        = new Point2D(counter, 0.7 * height);
            }

            if (tickCounter % getSkinnable().getMajorTickSpace() == 0) {
                // Draw major tickmark
                CTX.setLineWidth(size * 0.007);
                CTX.strokeLine(innerMainPoint.getX(), innerMainPoint.getY(), outerPoint.getX(), outerPoint.getY());

                // Draw text
                CTX.setTextBaseline(VPos.CENTER);
                if (Orientation.VERTICAL == orientation) {
                    CTX.setTextAlign(TextAlignment.RIGHT);
                    CTX.fillText(Integer.toString((int) (getSkinnable().getMaxValue() - tickCounter * getSkinnable().getMinorTickSpace())), textPoint.getX(), textPoint.getY());
                } else {
                    CTX.setTextAlign(TextAlignment.CENTER);
                    CTX.fillText(Integer.toString((int) (getSkinnable().getMinValue() + tickCounter * getSkinnable().getMinorTickSpace())), textPoint.getX(), textPoint.getY());
                }
            } else if (getSkinnable().getMinorTickSpace() % 2 != 0 && tickCounter % 5 == 0) {
                CTX.setLineWidth(size * 0.006);
                CTX.strokeLine(innerMediumPoint.getX(), innerMediumPoint.getY(), outerPoint.getX(), outerPoint.getY());
            } else if (tickCounter % getSkinnable().getMinorTickSpace() == 0) {
                CTX.setLineWidth(size * 0.005);
                CTX.strokeLine(innerMinorPoint.getX(), innerMinorPoint.getY(), outerPoint.getX(), outerPoint.getY());
            }
            tickCounter++;
        }
    }

    private void updateBar() {
        timeline.stop();

        if (withinSpeedLimit && getSkinnable().isAnimated()) {
            //double animationDuration = (getSkinnable().getAnimationDuration() / (getSkinnable().getMaxValue() - getSkinnable().getMinValue())) * Math.abs(getSkinnable().getValue() - getSkinnable().getOldValue());
            final KeyValue KEY_VALUE = new KeyValue(currentValue, getSkinnable().getValue() - getSkinnable().getMinValue() , Interpolator.SPLINE(0.5, 0.4, 0.4, 1.0));
            final KeyFrame KEY_FRAME = new KeyFrame(Duration.millis(getSkinnable().getAnimationDuration()), KEY_VALUE);
            timeline.getKeyFrames().setAll(KEY_FRAME);
            timeline.play();
        } else {
            currentValue.set(getSkinnable().getValue() - getSkinnable().getMinValue());
        }
    }


    // ******************** Utility methods ***********************************
    private String colorToCss(final Color COLOR) {
        return COLOR.toString().replace("0x", "#");
    }


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getSkinnable().getWidth();
        height = getSkinnable().getHeight();

        if (Orientation.VERTICAL == getSkinnable().getOrientation()) {
            width = height / ASPECT_RATIO;
        } else {
            height = width / ASPECT_RATIO;
        }

        size = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);

            if (Orientation.VERTICAL == getSkinnable().getOrientation()) {
                stepSize = Math.abs(0.67143 * height / (getSkinnable().getMaxValue() - getSkinnable().getMinValue()));

                background.setStyle("-fx-background-insets: 0, " + (0.005 * width) + ", " + (0.12143 * width) + ";");
                background.setPrefSize(height / ASPECT_RATIO, height);

                barBackground.setPrefSize(0.14286 * width, 0.67143 * height);
                barBackground.relocate((width - barBackground.getPrefWidth()) * 0.5, (height - barBackground.getPrefHeight()) * 0.5);

                barBackgroundBorderStart1.setX(barBackground.getLayoutX() - 1);
                barBackgroundBorderStart1.setY(barBackground.getLayoutY());
                barBackgroundBorderStop1.setX(barBackground.getLayoutX() - 1);
                barBackgroundBorderStop1.setY(barBackground.getLayoutY() + barBackground.getPrefHeight());
                barBackgroundBorderStart2.setX(barBackground.getLayoutX() + barBackground.getPrefWidth() + 1);
                barBackgroundBorderStart2.setY(barBackground.getLayoutY());
                barBackgroundBorderStop2.setX(barBackground.getLayoutX() + barBackground.getPrefWidth() + 1);
                barBackgroundBorderStop2.setY(barBackground.getLayoutY() + barBackground.getPrefHeight());

                ticksAndSectionsCanvas.setWidth(height / ASPECT_RATIO);
                ticksAndSectionsCanvas.setHeight(height);
                ticksAndSections.clearRect(0, 0, ticksAndSectionsCanvas.getWidth(), ticksAndSectionsCanvas.getHeight());
                drawTickMarks(ticksAndSections);
                ticksAndSectionsCanvas.setCache(true);
                ticksAndSectionsCanvas.setCacheHint(CacheHint.QUALITY);

                innerShadow.setRadius(0.01 * width);
                glow.setRadius(0.05 * width);
                glow.setColor(getSkinnable().getLedColor());

                ledFrame.setPrefSize(0.08571 * width, 0.08571 * width);
                ledFrame.relocate(0.69286 * width, 0.14857 * height);

                ledMain.setPrefSize(0.06429 * width, 0.06429 * width);
                ledMain.relocate(0.70714 * width, 0.15429 * height);

                ledHl.setPrefSize(0.05 * width, 0.05 * width);
                ledHl.relocate(0.71429 * width, 0.15714 * height);

                bar.setPrefSize(0.14286 * width, Math.abs(currentValue.get()) * stepSize);
                bar.setTranslateX((width - bar.getPrefWidth()) * 0.5);
                bar.setTranslateY(barBackground.getLayoutY() + barBackground.getPrefHeight());

                foreground.setPrefSize(0.8 * width, 0.9 * height);
                foreground.relocate(0.1 * width, 0.05 * height);
            } else {
                stepSize = Math.abs(0.75 * width / (getSkinnable().getMaxValue() - getSkinnable().getMinValue()));

                background.setStyle("-fx-background-insets: 0, " + (0.005 * height) + ", " + (0.12143 * height) + ";");
                background.setPrefSize(width, width / ASPECT_RATIO);

                barBackground.setPrefSize(0.75 * width, 0.14286 * height);
                barBackground.relocate((width - barBackground.getPrefWidth()) * 0.5, (height - barBackground.getPrefHeight()) * 0.5);

                barBackgroundBorderStart1.setX(barBackground.getLayoutX());
                barBackgroundBorderStart1.setY(barBackground.getLayoutY() - 1);
                barBackgroundBorderStop1.setX(barBackground.getLayoutX() + barBackground.getPrefWidth());
                barBackgroundBorderStop1.setY(barBackground.getLayoutY() - 1);
                barBackgroundBorderStart2.setX(barBackground.getLayoutX());
                barBackgroundBorderStart2.setY(barBackground.getLayoutY() + barBackground.getPrefHeight() + 1);
                barBackgroundBorderStop2.setX(barBackground.getLayoutX() + barBackground.getPrefWidth());
                barBackgroundBorderStop2.setY(barBackground.getLayoutY() + barBackground.getPrefHeight() + 1);

                ticksAndSectionsCanvas.setWidth(width);
                ticksAndSectionsCanvas.setHeight(width / ASPECT_RATIO);
                ticksAndSections.clearRect(0, 0, ticksAndSectionsCanvas.getWidth(), ticksAndSectionsCanvas.getHeight());
                drawTickMarks(ticksAndSections);
                ticksAndSectionsCanvas.setCache(true);
                ticksAndSectionsCanvas.setCacheHint(CacheHint.QUALITY);

                innerShadow.setRadius(0.01 * height);
                glow.setRadius(0.05 * height);
                glow.setColor(getSkinnable().getLedColor());

                ledFrame.setPrefSize(0.08571 * height, 0.08571 * height);
                ledFrame.relocate(0.89748 * width, 0.455 * height);

                ledMain.setPrefSize(0.06429 * height, 0.06429 * height);
                ledMain.relocate(0.90233 * width, 0.46714 * height);

                ledHl.setPrefSize(0.05 * height, 0.05 * height);
                ledHl.relocate(0.90476 * width, 0.47429 * height);

                bar.setPrefSize(Math.abs(currentValue.get()) * stepSize, 0.14286 * height);
                bar.setTranslateX(barBackground.getLayoutX());
                bar.setTranslateY(barBackground.getLayoutY());

                foreground.setPrefSize(0.9 * width, 0.8 * height);
                foreground.relocate((width - foreground.getPrefWidth()) * 0.5, 0.1 * height);
            }

            resizeText();

            updateBar();
        }
    }

    private void resizeText() {
        if (Orientation.VERTICAL == getSkinnable().getOrientation()) {
            titleText.setFont(Fonts.robotoRegular(0.08571 * width));
            titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, 0.22 * width);

            unitText.setFont(Fonts.robotoRegular(0.07143 * width));
            unitText.relocate((width - unitText.getLayoutBounds().getWidth()) * 0.76, 0.88 * height);

            lcdText.setStyle("-fx-background-radius: " + (0.0125 * width) + ";");
            lcdText.setFont(Fonts.robotoRegular(0.08571 * width));
            lcdText.setPrefSize(0.375 * width, 0.015 * width);
            lcdText.relocate((width - lcdText.getPrefWidth()) * 0.5, 0.87 * height);
        } else {
            titleText.setFont(Fonts.robotoRegular(0.08571 * height));
            titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.1, 0.22 * height);

            unitText.setFont(Fonts.robotoRegular(0.07143 * height));
            unitText.relocate((width - unitText.getLayoutBounds().getWidth()) * 0.5, height * 0.76);

            lcdText.setStyle("-fx-background-radius: " + (0.0125 * height) + ";");
            lcdText.setFont(Fonts.robotoRegular(height * 0.09));
            lcdText.setPrefSize(0.5 * height, 0.025 * height);
            lcdText.relocate((width - lcdText.getPrefWidth()) * 0.9, 0.21 * height);
        }
    }
}

