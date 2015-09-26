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

package eu.hansolo.enzo.heatcontrol.skin;

import eu.hansolo.enzo.fonts.Fonts;
import eu.hansolo.enzo.common.ValueEvent;
import eu.hansolo.enzo.heatcontrol.GradientLookup;
import eu.hansolo.enzo.heatcontrol.HeatControl;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.Locale;


/**
 * User: hansolo
 * Date: 08.11.13
 * Time: 16:35
 */
public class HeatControlSkin extends SkinBase<HeatControl> implements Skin<HeatControl> {
    private static final double      PREFERRED_WIDTH  = 200;
    private static final double      PREFERRED_HEIGHT = 200;
    private static final double      MINIMUM_WIDTH    = 50;
    private static final double      MINIMUM_HEIGHT   = 50;
    private static final double      MAXIMUM_WIDTH    = 1024;
    private static final double      MAXIMUM_HEIGHT   = 1024; 
    private static boolean           userAction;
    private double                   size;
    private double                   centerX;
    private double                   centerY;
    private Pane                     pane;
    private Circle                   background;
    private Canvas                   ticksCanvas;
    private GraphicsContext          ticks;
    private Region                   targetIndicator;
    private Rotate                   targetIndicatorRotate;
    private boolean                  targetExceeded;
    private Region                   valueIndicator;
    private Rotate                   valueIndicatorRotate;
    private Text                     infoText;
    private Text                     value;
    private String                   newTarget;
    private GradientLookup           gradientLookup;   
    private InnerShadow              innerShadow;
    private double                   angleStep;
    private double                   interactiveAngle;
    private EventHandler<MouseEvent> mouseEventHandler;
    private EventHandler<TouchEvent> touchEventHandler;    


    // ******************** Constructors **************************************
    public HeatControlSkin(HeatControl heatControl) {
        super(heatControl);
        userAction        = false;
        newTarget         = "";
        gradientLookup    = new GradientLookup(new Stop(0.10, Color.web("#3221c9")),                                               
                                               new Stop(0.20, Color.web("#216ec9")),
                                               new Stop(0.30, Color.web("#21bac9")),
                                               new Stop(0.40, Color.web("#30cb22")),
                                               new Stop(0.50, Color.web("#2fcb22")),
                                               new Stop(0.60, Color.web("#f1ec28")),
                                               new Stop(0.70, Color.web("#f1c428")),
                                               new Stop(0.80, Color.web("#f19c28")),
                                               new Stop(0.90, Color.web("#f16f28")),
                                               new Stop(1.00, Color.web("#ec272f")));        
        angleStep         = heatControl.getAngleRange() / (heatControl.getMaxValue() - heatControl.getMinValue());
        mouseEventHandler = mouseEvent -> handleMouseEvent(mouseEvent);
        touchEventHandler = touchEvent -> handleTouchEvent(touchEvent);

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
        innerShadow = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), PREFERRED_HEIGHT * 0.1, 0, 0, 0);
        Color color = gradientLookup.getColorAt(getSkinnable().getValue() / (getSkinnable().getMaxValue() - getSkinnable().getMinValue())); 
        background = new Circle(0.5 * PREFERRED_WIDTH, 0.5 * PREFERRED_HEIGHT, 0.5 * PREFERRED_WIDTH);
        background.setFill(new LinearGradient(0, 0, 0, PREFERRED_HEIGHT,
                                              false, CycleMethod.NO_CYCLE,
                                              new Stop(0, color.deriveColor(0, 1, 0.8, 1)),
                                              new Stop(1, color.deriveColor(0, 1, 0.6, 1))));
        background.setEffect(innerShadow);

        ticksCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        ticksCanvas.setMouseTransparent(true);
        ticks = ticksCanvas.getGraphicsContext2D();

        targetIndicator = new Region();
        targetIndicator.getStyleClass().setAll("target-indicator");
        targetIndicatorRotate = new Rotate(180 - getSkinnable().getStartAngle() - getSkinnable().getMinValue() * angleStep);
        targetIndicator.getTransforms().setAll(targetIndicatorRotate);       
        targetExceeded = false;
        targetIndicator.setVisible(getSkinnable().isTargetEnabled());

        valueIndicator = new Region();
        valueIndicator.getStyleClass().setAll("value-indicator");
        valueIndicatorRotate = new Rotate(180 - getSkinnable().getStartAngle());
        valueIndicatorRotate.setAngle(valueIndicatorRotate.getAngle() + (getSkinnable().getValue() - getSkinnable().getOldValue() - getSkinnable().getMinValue()) * angleStep);
        valueIndicator.getTransforms().setAll(valueIndicatorRotate);

        infoText = new Text(getSkinnable().getInfoText().toUpperCase());
        infoText.setTextOrigin(VPos.CENTER);
        infoText.setFont(Fonts.robotoMedium(0.06 * PREFERRED_HEIGHT));
        infoText.setMouseTransparent(true);
        infoText.getStyleClass().setAll("info-text");        

        value = new Text(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getValue()));
        value.setMouseTransparent(true);
        value.setTextOrigin(VPos.CENTER);
        value.setFont(Fonts.robotoBold(0.32 * PREFERRED_HEIGHT));
        value.setMouseTransparent(true);
        value.getStyleClass().setAll("value");

        // Add all nodes
        pane = new Pane();
        pane.getChildren().setAll(background,                                  
                                  ticksCanvas,
                                  valueIndicator,
                                  targetIndicator,
                                  infoText,
                                  value);
        
        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().heightProperty().addListener(observable -> handleControlPropertyChanged("RESIZE"));
        getSkinnable().infoTextProperty().addListener(observable -> handleControlPropertyChanged("INFO_TEXT"));
        getSkinnable().targetEnabledProperty().addListener(observable -> handleControlPropertyChanged("TARGET_ENABLED"));
        getSkinnable().valueProperty().addListener(observable -> handleControlPropertyChanged("VALUE"));
        getSkinnable().minValueProperty().addListener(observable -> handleControlPropertyChanged("RECALC"));
        getSkinnable().maxValueProperty().addListener(observable -> handleControlPropertyChanged("RECALC"));
        getSkinnable().minMeasuredValueProperty().addListener(observable -> handleControlPropertyChanged("MIN_MEASURED_VALUE"));        
        getSkinnable().maxMeasuredValueProperty().addListener(observable -> handleControlPropertyChanged("MAX_MEASURED_VALUE"));                             
        getSkinnable().thresholdProperty().addListener(observable -> handleControlPropertyChanged("TARGET"));        
        getSkinnable().angleRangeProperty().addListener(observable -> handleControlPropertyChanged("ANGLE_RANGE"));        
        
        valueIndicatorRotate.angleProperty().addListener(observable -> handleControlPropertyChanged("ANGLE"));        

        targetIndicator.setOnMousePressed(mouseEventHandler);
        targetIndicator.setOnMouseDragged(mouseEventHandler);
        targetIndicator.setOnMouseReleased(mouseEventHandler);

        targetIndicator.setOnTouchPressed(touchEventHandler);
        targetIndicator.setOnTouchMoved(touchEventHandler);
        targetIndicator.setOnTouchReleased(touchEventHandler);
    }


    // ******************** Methods *******************************************
    protected void handleControlPropertyChanged(final String PROPERTY) {
        if ("RESIZE".equals(PROPERTY)) {
            resize();
        } else if ("INFO_TEXT".equals(PROPERTY)) {
            infoText.setText(getSkinnable().getInfoText().toUpperCase());
            resize();
        } else if ("VALUE".equals(PROPERTY)) {
            rotateNeedle();      
            adjustBackgroundColor();
        } else if ("RECALC".equals(PROPERTY)) {
            if (getSkinnable().getMinValue() < 0) {
                angleStep = getSkinnable().getAngleRange() / (getSkinnable().getMaxValue() - getSkinnable().getMinValue());
                valueIndicatorRotate.setAngle(180 - getSkinnable().getStartAngle() - (getSkinnable().getMinValue()) * angleStep);
            } else {
                angleStep = getSkinnable().getAngleRange() / (getSkinnable().getMaxValue() + getSkinnable().getMinValue());
                valueIndicatorRotate.setAngle(180 - getSkinnable().getStartAngle() * angleStep);
            }
            resize();
        } else if ("ANGLE".equals(PROPERTY)) {            
            double currentValue = (valueIndicatorRotate.getAngle() + getSkinnable().getStartAngle() - 180) / angleStep + getSkinnable().getMinValue();
            if (!userAction) {
                value.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", currentValue));
                value.setTranslateX((size - value.getLayoutBounds().getWidth()) * 0.5);
            }
            // Check targetIndicator
            if (targetExceeded) {
                if (currentValue < getSkinnable().getTarget()) {
                    getSkinnable().fireEvent(new ValueEvent(this, null, ValueEvent.VALUE_UNDERRUN));
                    targetExceeded = false;
                }
            } else {
                if (currentValue > getSkinnable().getTarget()) {
                    getSkinnable().fireEvent(new ValueEvent(this, null, ValueEvent.VALUE_EXCEEDED));
                    targetExceeded = true;
                }
            }                                    
        } else if ("TARGET".equals(PROPERTY)) {
            targetIndicatorRotate.setAngle(getSkinnable().getTarget() * angleStep - 180 - getSkinnable().getStartAngle());
        } else if ("TARGET_ENABLED".equals(PROPERTY)) {
            targetIndicator.setVisible(getSkinnable().isTargetEnabled());
        }
    }
    

    // ******************** Private Methods ***********************************    
    private void handleMouseEvent(final MouseEvent MOUSE_EVENT) {
        final Object    SRC  = MOUSE_EVENT.getSource();
        final EventType TYPE = MOUSE_EVENT.getEventType();
        if (SRC.equals(targetIndicator)) {
            if (MouseEvent.MOUSE_PRESSED == TYPE) {    
                userAction = true;
                value.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getTarget()));
                resizeText();                
            } else if (MouseEvent.MOUSE_DRAGGED == TYPE) {
                touchRotate(MOUSE_EVENT.getSceneX() - getSkinnable().getLayoutX(), MOUSE_EVENT.getSceneY() - getSkinnable().getLayoutY(), targetIndicatorRotate);                
            } else if (MouseEvent.MOUSE_RELEASED == TYPE) {                
                getSkinnable().setTarget(Double.parseDouble(newTarget));
                fadeBack();
            }
        }
    }

    private void handleTouchEvent(final TouchEvent TOUCH_EVENT) {
        final Object    SRC  = TOUCH_EVENT.getSource();
        final EventType TYPE = TOUCH_EVENT.getEventType();
        if (SRC.equals(targetIndicator)) {
            if (TouchEvent.TOUCH_PRESSED == TYPE) {
                value.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getTarget()));
                resizeText();
            } else if (TouchEvent.TOUCH_MOVED == TYPE) {
                touchRotate(TOUCH_EVENT.getTouchPoint().getSceneX() - getSkinnable().getLayoutX(), TOUCH_EVENT.getTouchPoint().getSceneY() - getSkinnable().getLayoutY(),
                            targetIndicatorRotate);
            } else if (TouchEvent.TOUCH_RELEASED == TYPE) {
                getSkinnable().setTarget(Double.parseDouble(value.getText()));
                fadeBack();
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
            value.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", newValue));
            newTarget = value.getText();
            resizeText();
        }

    }

    private void fadeBack() {
        FadeTransition fadeInfoTextOut = new FadeTransition(Duration.millis(425), infoText);
        fadeInfoTextOut.setFromValue(1.0);
        fadeInfoTextOut.setToValue(0.0);
        
        FadeTransition fadeValueOut = new FadeTransition(Duration.millis(425), value);
        fadeValueOut.setFromValue(1.0);
        fadeValueOut.setToValue(0.0);

        PauseTransition pause = new PauseTransition(Duration.millis(50));

        FadeTransition fadeInfoTextIn = new FadeTransition(Duration.millis(425), infoText);
        fadeInfoTextIn.setFromValue(0.0);
        fadeInfoTextIn.setToValue(1.0);
        
        FadeTransition fadeValueIn = new FadeTransition(Duration.millis(425), value);
        fadeValueIn.setFromValue(0.0);
        fadeValueIn.setToValue(1.0);                
        
        ParallelTransition parallelIn = new ParallelTransition(fadeInfoTextIn, fadeValueIn);

        ParallelTransition parallelOut = new ParallelTransition(fadeInfoTextOut, fadeValueOut);
        parallelOut.setOnFinished(event -> {
            double currentValue = (valueIndicatorRotate.getAngle() + getSkinnable().getStartAngle() - 180) / angleStep + getSkinnable().getMinValue();
            value.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", currentValue));
            value.setTranslateX((size - value.getLayoutBounds().getWidth()) * 0.5);
            if (getSkinnable().getTarget() < getSkinnable().getValue()) {
                getSkinnable().setInfoText("COOLING");
            } else if (getSkinnable().getTarget() > getSkinnable().getValue()) {
                getSkinnable().setInfoText("HEATING");
            }
            
            resizeText();
            drawTickMarks(ticks);
            userAction = false;
        });

        SequentialTransition sequence = new SequentialTransition(parallelOut, pause, parallelIn);
        sequence.play();
    }

    private void rotateNeedle() {
        double range       = (getSkinnable().getMaxValue() - getSkinnable().getMinValue());
        double angleRange  = getSkinnable().getAngleRange();
        angleStep          = angleRange / range;
        double targetAngle = valueIndicatorRotate.getAngle() + (getSkinnable().getValue() - getSkinnable().getOldValue()) * angleStep;
        
        valueIndicatorRotate.setAngle(targetAngle);
        drawTickMarks(ticks);
    }
   
    private void adjustBackgroundColor() {
        Color color = gradientLookup.getColorAt(getSkinnable().getValue() / (getSkinnable().getMaxValue() - getSkinnable().getMinValue()));
        background.setFill(new LinearGradient(0, 0, 0, size,
                                              false, CycleMethod.NO_CYCLE,
                                              new Stop(0, color.deriveColor(0, 1, 0.8, 1)),
                                              new Stop(1, color.deriveColor(0, 1, 0.6, 1))));                        
    }
    
    private void drawTickMarks(final GraphicsContext CTX) {        
        CTX.clearRect(0, 0, size, size);
        double  sinValue;        
        double  cosValue;
        double  startAngle     = getSkinnable().getStartAngle();                
        Point2D center         = new Point2D(size * 0.5, size * 0.5);
        double  stdLineWidth   = size * 0.003;
        double  rangeLineWidth = size * 0.007;
        for (double angle = 0, counter = getSkinnable().getMinValue() ; Double.compare(counter, getSkinnable().getMaxValue()) <= 0 ; angle -= angleStep / 3, counter+= 0.33333) {
            sinValue = Math.sin(Math.toRadians(angle + startAngle));
            cosValue = Math.cos(Math.toRadians(angle + startAngle));

            Point2D innerPoint = new Point2D(center.getX() + size * 0.368 * sinValue, center.getY() + size * 0.368 * cosValue);            
            Point2D outerPoint = new Point2D(center.getX() + size * 0.457 * sinValue, center.getY() + size * 0.457 * cosValue);            

            CTX.setStroke(getSkinnable().getTickMarkFill());            
            if (getSkinnable().isTargetEnabled() && 
                counter > getSkinnable().getValue() && counter < getSkinnable().getTarget() ||
                counter > getSkinnable().getTarget() && counter < getSkinnable().getValue()) {
                CTX.setLineWidth(rangeLineWidth);
            } else {
                CTX.setLineWidth(stdLineWidth);    
            }            
            CTX.setLineCap(StrokeLineCap.ROUND);
            CTX.strokeLine(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());                            
        }
    }
    
    private void resizeText() {        
        infoText.setFont(Fonts.robotoLight(size * 0.07));
        infoText.setTranslateX((size - infoText.getLayoutBounds().getWidth()) * 0.5);
        infoText.setTranslateY(size * 0.34);

        value.setFont(Fonts.robotoBold(size * 0.32));
        value.setTranslateX((size - value.getLayoutBounds().getWidth()) * 0.5);
        value.setTranslateY(size * 0.5);
    }

    private void resize() {
        size = getSkinnable().getWidth() < getSkinnable().getHeight() ? getSkinnable().getWidth() : getSkinnable().getHeight();
        
        if (size > 0) {
            pane.setMaxSize(size, size);
            centerX = size * 0.5;
            centerY = size * 0.5;
            
            innerShadow.setRadius(size * 0.1);
            
            background.setCenterX(centerX);
            background.setCenterY(centerY);
            background.setRadius(size * 0.5);
    
            ticksCanvas.setWidth(size);
            ticksCanvas.setHeight(size);
            ticks.clearRect(0, 0, size, size);        
            drawTickMarks(ticks);
            ticksCanvas.setCache(true);
            ticksCanvas.setCacheHint(CacheHint.QUALITY);
    
            valueIndicator.setPrefSize(size * 0.025, size * 0.096);
            valueIndicator.relocate((size - valueIndicator.getPrefWidth()) * 0.5, size * 0.039);
            valueIndicatorRotate.setPivotX(valueIndicator.getPrefWidth() * 0.5);
            valueIndicatorRotate.setPivotY(size * 0.461);
            
            targetIndicator.setPrefSize(0.025 * size, 0.13 * size);
            targetIndicator.relocate((size - targetIndicator.getPrefWidth()) * 0.5, size * 0.039);
            targetIndicatorRotate.setPivotX(targetIndicator.getPrefWidth() * 0.5);
            targetIndicatorRotate.setPivotY(size * 0.461);
            targetIndicatorRotate.setAngle(getSkinnable().getTarget() * angleStep - 180 - getSkinnable().getStartAngle() - getSkinnable().getMinValue() * angleStep);
    
            infoText.setText(getSkinnable().getInfoText().toUpperCase());
            value.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", (valueIndicatorRotate.getAngle() + getSkinnable().getStartAngle() - 180) / angleStep));
                    
            resizeText();
        }
    }
}
