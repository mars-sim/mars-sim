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

import eu.hansolo.enzo.common.Section;
import eu.hansolo.enzo.fonts.Fonts;
import eu.hansolo.enzo.gauge.Radial;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.Group;
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
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.time.Instant;
import java.util.Locale;
import java.util.stream.IntStream;


public class RadialSkin extends SkinBase<Radial> implements Skin<Radial> {
    private static final double MINIMUM_WIDTH    = 5;
    private static final double MINIMUM_HEIGHT   = 5;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private static final double PREFERRED_WIDTH  = 200;
    private static final double PREFERRED_HEIGHT = 200;
    private double size;
    private double width;
    private double height;
    private Pane   pane;

    private Region          background;
    private Canvas          ticksAndSectionsCanvas;
    private GraphicsContext ticksAndSections;
    private Text            unitText;
    private Text            titleText;
    private Region          ledFrame;
    private Region          ledMain;
    private Region          ledHl;
    private Label           lcdText;
    private Region          needle;
    private Region          needleColorBlock;
    private Region          minPost;
    private Region          maxPost;
    private Region          knob;
    private Group           shadowGroup;
    private Region          foreground;
    private DropShadow      dropShadow;
    private InnerShadow     innerShadow;
    private DropShadow      glow;
    private double          oldValue;
    private double          angleStep;
    private Timeline        timeline;
    private String          limitString;
    private Instant         lastCall;
    private boolean         withinSpeedLimit;
    private Rotate          needleRotate;
    private Rotate          needleColorBlockRotate;


    // ******************** Constructors **************************************
    public RadialSkin(final Radial CONTROL) {
        super(CONTROL);
        angleStep        = CONTROL.getAngleRange() / (CONTROL.getMaxValue() - CONTROL.getMinValue());
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

        titleText = new Text(getSkinnable().getTitle());
        titleText.getStyleClass().setAll("title");

        unitText = new Text(getSkinnable().getUnit());
        unitText.getStyleClass().setAll("unit");

        lcdText = new Label(getSkinnable().getNumberFormat().format(getSkinnable().getValue()));
        lcdText.getStyleClass().setAll("lcd-text");

        angleStep          = getSkinnable().getAngleRange() / (getSkinnable().getMaxValue() - getSkinnable().getMinValue());
        double targetAngle = 180 - getSkinnable().getStartAngle() + (getSkinnable().getValue() - getSkinnable().getMinValue()) * angleStep;
        targetAngle        = getSkinnable().clamp(180 - getSkinnable().getStartAngle(), 180 - getSkinnable().getStartAngle() + getSkinnable().getAngleRange(), targetAngle);

        needle = new Region();
        needle.getStyleClass().setAll("needle");
        needleRotate = new Rotate(180 - getSkinnable().getStartAngle());
        needleRotate.setAngle(targetAngle);
        needle.getTransforms().setAll(needleRotate);

        needleColorBlock = new Region();
        needleColorBlock.getStyleClass().setAll("needle-color-block");
        needleColorBlock.setStyle("-needle-color: " + (colorToCss(getSkinnable().getNeedleColor())) + ";");
        needleColorBlockRotate = new Rotate(180 - getSkinnable().getStartAngle());
        needleColorBlock.getTransforms().setAll(needleColorBlockRotate);

        minPost = new Region();
        minPost.getStyleClass().setAll("post");

        maxPost = new Region();
        maxPost.getStyleClass().setAll("post");

        knob = new Region();
        knob.getStyleClass().setAll("knob");

        shadowGroup = new Group(needle, minPost, maxPost, knob);
        dropShadow  = new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 2.5, 0, 0, 2.5);
        shadowGroup.setEffect(dropShadow);

        foreground = new Region();
        foreground.getStyleClass().setAll("foreground");

        pane = new Pane();
        pane.getChildren().setAll(background,
                                  ticksAndSectionsCanvas,
                                  unitText,
                                  titleText,
                                  ledFrame,
                                  ledMain,
                                  ledHl,
                                  lcdText,
                                  shadowGroup,
                                  needleColorBlock,
                                  foreground);

        getChildren().setAll(pane);
        resize();
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(observable -> handleControlPropertyChanged("RESIZE") );
        getSkinnable().heightProperty().addListener(observable -> handleControlPropertyChanged("RESIZE") );
        getSkinnable().ledOnProperty().addListener(observable -> ledMain.setEffect(getSkinnable().isLedOn() ? glow : innerShadow));
        getSkinnable().ledColorProperty().addListener(observable -> handleControlPropertyChanged("LED_COLOR"));
        getSkinnable().ledVisibleProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().lcdVisibleProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().tickLabelOrientationProperty().addListener(observable -> handleControlPropertyChanged("CANVAS_REFRESH"));
        getSkinnable().tickLabelFillProperty().addListener(observable -> handleControlPropertyChanged("CANVAS_REFRESH"));
        getSkinnable().tickMarkFillProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().needleColorProperty().addListener(observable -> handleControlPropertyChanged("NEEDLE_COLOR"));
        getSkinnable().animatedProperty().addListener(observable -> handleControlPropertyChanged("ANIMATED"));
        //getSkinnable().startAngleProperty().addListener(observable -> handleControlPropertyChanged("START_ANGLE"));
        //getSkinnable().angleRangeProperty().addListener(observable -> handleControlPropertyChanged("ANGLE_RANGE"));
        getSkinnable().numberFormatProperty().addListener(observable -> handleControlPropertyChanged("RECALC"));
        getSkinnable().unitProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().titleProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().getSections().addListener((ListChangeListener<Section>) change -> handleControlPropertyChanged("CANVAS_REFRESH"));
        getSkinnable().getAreas().addListener((ListChangeListener<Section>) change -> handleControlPropertyChanged("CANVAS_REFRESH"));

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
        needleColorBlockRotate.angleProperty().bind(needleRotate.angleProperty());
    }


    // ******************** Methods *******************************************
    protected void handleControlPropertyChanged(final String PROPERTY) {
        if ("RESIZE".equals(PROPERTY)) {
            resize();
        } else if ("ANGLE".equals(PROPERTY)) {
            double currentValue = (needleRotate.getAngle() + getSkinnable().getStartAngle() - 180) / angleStep + getSkinnable().getMinValue();
            lcdText.setText(limitString + String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", currentValue));
        } else if ("CANVAS_REFRESH".equals(PROPERTY)) {
            ticksAndSections.clearRect(0, 0, size, size);
            ticksAndSections.clearRect(0, 0, size, size);
            drawSections(ticksAndSections);
            drawAreas(ticksAndSections);
            drawTickMarks(ticksAndSections);
        } else if ("NEEDLE_COLOR".equals(PROPERTY)) {
            needle.setStyle("-needle-color: " + (colorToCss(getSkinnable().getNeedleColor())) + ";");
        } else if ("LED_COLOR".equals(PROPERTY)) {
            ledMain.setStyle("-led-color: " + (colorToCss(getSkinnable().getLedColor())));
            resize();
        }
    }

    private void drawTickMarks(final GraphicsContext CTX) {
        CTX.setFont(Fonts.robotoLight(0.045 * size));
        CTX.setStroke(getSkinnable().getTickMarkFill());
        CTX.setFill(getSkinnable().getTickLabelFill());

        double  sinValue;
        double  cosValue;
        double  startAngle = getSkinnable().getStartAngle();
        double  orthText   = Radial.TickLabelOrientation.ORTHOGONAL == getSkinnable().getTickLabelOrientation() ? 0.33 : 0.31;
        Point2D center     = new Point2D(size * 0.5, size * 0.5);
        for (double angle = 0, counter = getSkinnable().getMinValue() ; Double.compare(counter, getSkinnable().getMaxValue()) <= 0 ; angle -= angleStep, counter++) {
            sinValue = Math.sin(Math.toRadians(angle + startAngle));
            cosValue = Math.cos(Math.toRadians(angle + startAngle));

            Point2D innerMainPoint   = new Point2D(center.getX() + size * 0.36 * sinValue, center.getY() + size * 0.36 * cosValue);
            Point2D innerMediumPoint = new Point2D(center.getX() + size * 0.37 * sinValue, center.getY() + size * 0.37 * cosValue);
            Point2D innerMinorPoint  = new Point2D(center.getX() + size * 0.38 * sinValue, center.getY() + size * 0.38 * cosValue);
            Point2D outerPoint       = new Point2D(center.getX() + size * 0.39 * sinValue, center.getY() + size * 0.39 * cosValue);
            Point2D textPoint        = new Point2D(center.getX() + size * orthText * sinValue, center.getY() + size * orthText * cosValue);

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
                CTX.setTextAlign(TextAlignment.CENTER);
                CTX.setTextBaseline(VPos.CENTER);
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
        final double xy        = (size - 0.75 * size) / 2;
        final double wh        = size * 0.75;
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
        final double xy        = (size - 0.7925 * size) / 2;
        final double wh        = size * 0.7925;
        final double MIN_VALUE = getSkinnable().getMinValue();
        final double MAX_VALUE = getSkinnable().getMaxValue();
        final double OFFSET    = 90 - getSkinnable().getStartAngle();

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


    private void rotateNeedle() {
        timeline.stop();

        angleStep          = getSkinnable().getAngleRange() / (getSkinnable().getMaxValue() - getSkinnable().getMinValue());
        double targetAngle = 180 - getSkinnable().getStartAngle() + (getSkinnable().getValue() - getSkinnable().getMinValue()) * angleStep;
        targetAngle        = getSkinnable().clamp(180 - getSkinnable().getStartAngle(), 180 - getSkinnable().getStartAngle() + getSkinnable().getAngleRange(), targetAngle);
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


    // ******************** Utility methods ***********************************
    private String colorToCss(final Color COLOR) {
        return COLOR.toString().replace("0x", "#");
    }


    // ******************** Resizing ******************************************
    private void resize() {
        size   = getSkinnable().getWidth() < getSkinnable().getHeight() ? getSkinnable().getWidth() : getSkinnable().getHeight();
        width  = size;
        height = size;

        if (size > 0) {
            dropShadow.setRadius(0.0125 * size);
            dropShadow.setOffsetY(0.0125 * size);

            pane.setMaxSize(size, size);
            pane.relocate((getSkinnable().getWidth() - size) * 0.5, (getSkinnable().getHeight() - size) * 0.5);

            background.setStyle("-fx-background-insets: 0, " + (0.005 * size) + ", " + (getSkinnable().getFrameSizeFactor() * size) + ";");
            background.setPrefSize(width, height);

            ticksAndSectionsCanvas.setWidth(size);
            ticksAndSectionsCanvas.setHeight(size);
            ticksAndSections.clearRect(0, 0, size, size);
            if (getSkinnable().isSectionsVisible()) drawSections(ticksAndSections);
            if (getSkinnable().isAreasVisible()) drawAreas(ticksAndSections);
            drawTickMarks(ticksAndSections);
            ticksAndSectionsCanvas.setCache(true);
            ticksAndSectionsCanvas.setCacheHint(CacheHint.QUALITY);

            innerShadow.setRadius(0.01 * size);
            glow.setRadius(0.05 * size);
            glow.setColor(getSkinnable().getLedColor());

            ledFrame.setPrefSize(0.06 * width, 0.06 * height);
            ledFrame.relocate(0.61 * width, 0.415 * height);

            ledMain.setPrefSize(0.045 * width, 0.045 * height);
            ledMain.relocate(0.62 * width, 0.425 * height);

            ledHl.setPrefSize(0.035 * width, 0.035 * height);
            ledHl.relocate(0.625 * width, 0.43 * height);

            boolean ledVisible = getSkinnable().isLedVisible();
            ledFrame.setManaged(ledVisible);
            ledFrame.setVisible(ledVisible);
            ledMain.setManaged(ledVisible);
            ledMain.setVisible(ledVisible);
            ledHl.setManaged(ledVisible);
            ledHl.setVisible(ledVisible);

            needle.setPrefSize(0.055 * width, 0.48 * height);
            needle.relocate((size - needle.getPrefWidth()) * 0.5, 0.125 * size);
            needleRotate.setPivotX(0.5 * needle.getPrefWidth());
            needleRotate.setPivotY(0.77083 * needle.getPrefHeight());

            needleColorBlock.setPrefSize(0.012 * size, 0.095 * size);
            needleColorBlock.relocate((size - needleColorBlock.getPrefWidth()) * 0.5, 0.13 * size);
            needleColorBlockRotate.setPivotX(0.5 * needleColorBlock.getPrefWidth());
            needleColorBlockRotate.setPivotY(0.765 * needle.getPrefHeight());

            minPost.setStyle("-fx-background-insets: 0, " + (0.0025 * size) + ", " + (0.0025 * size) + ", " + (0.0025 * size) + ";" +
                             "-fx-padding: 0, 0, -" + (0.0008 * size) + "0 0 0, 0 0 " + (0.0008 * size) + "0;");
            minPost.setPrefSize(0.04 * width, 0.04 * height);
            minPost.relocate(0.335 * width, 0.805 * height);

            maxPost.setStyle("-fx-background-insets: 0, " + (0.0025 * size) + ", " + (0.0025 * size) + ", " + (0.0025 * size) + ";" +
                             "-fx-padding: 0, 0, -" + (0.0008 * size) + "0 0 0, 0 0 " + (0.0008 * size) + "0;");
            maxPost.setPrefSize(0.04 * width, 0.04 * height);
            maxPost.relocate(0.625 * width, 0.805 * height);

            knob.setStyle("-fx-background-insets: 0, " + (0.0075 * size) + ", " + (0.0075 * size) + ", " + (0.0075 * size) + ";" +
                          "-fx-padding: 0, 0, -" + (0.0016 * size) + "0 0 0, 0 0 " + (0.0016 * size) + "0;");
            knob.setPrefSize(0.08 * width, 0.08 * height);
            knob.relocate(0.455 * width, 0.46 * height);

            foreground.setPrefSize(0.595 * width, 0.28 * height);
            foreground.setTranslateX(0.2025 * width);
            foreground.setTranslateY(0.11 * height);

            resizeText();
        }
    }

    private void resizeText() {
        titleText.setFont(Fonts.robotoRegular(0.06 * size));
        titleText.relocate((size - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.33);

        unitText.setFont(Fonts.robotoRegular(0.05 * size));
        unitText.relocate((size - unitText.getLayoutBounds().getWidth()) * 0.5, size * 0.405);

        lcdText.setStyle("-fx-background-radius: " + (0.0125 * size) + ";");
        lcdText.setFont(Fonts.robotoRegular(size * 0.08));
        lcdText.setPrefSize(0.375 * size, 0.015 * size);
        lcdText.relocate((size - lcdText.getPrefWidth()) * 0.5, 0.58 * size);

        boolean lcdVisible = getSkinnable().isLcdVisible();
        lcdText.setManaged(lcdVisible);
        lcdText.setVisible(lcdVisible);
    }
}
