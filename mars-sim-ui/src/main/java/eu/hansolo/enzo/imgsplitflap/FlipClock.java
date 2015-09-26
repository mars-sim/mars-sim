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

package eu.hansolo.enzo.imgsplitflap;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;

import java.time.LocalDateTime;


/**
 * Created by
 * User: hansolo
 * Date: 18.04.13
 * Time: 14:59
 */
public class FlipClock extends Application {
    private static final String[] WEEK_DAYS = { "SUN","MON","TUE","WED","THU","FRI","SAT" };
    private static final String[] MONTHS = { "JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC" };
    private SplitFlap      dayLeft;
    private SplitFlap      dayMid;
    private SplitFlap      dayRight;

    private SplitFlap      dateLeft;
    private SplitFlap      dateRight;

    private SplitFlap      monthLeft;
    private SplitFlap      monthMid;
    private SplitFlap      monthRight;

    private SplitFlap      hourLeft;
    private SplitFlap      hourRight;
    private SplitFlap      minLeft;
    private SplitFlap      minRight;
    private SplitFlap      secLeft;
    private SplitFlap      secRight;

    private int            date;
    private int            hours;
    private int            minutes;
    private int            seconds;
    private long           lastTimerCall;
    private AnimationTimer timer;

    @Override public void init() {
        // 234 : 402 => 0.5820895522
        // 402 : 234 => 1.7179487179                        
        dayLeft    = SplitFlapBuilder.create().prefWidth(146).prefHeight(250).flipTime(300).selection(SplitFlap.ALPHA).textColor(Color.WHITESMOKE).build();
        dayMid     = SplitFlapBuilder.create().prefWidth(146).prefHeight(250).flipTime(300).selection(SplitFlap.ALPHA).textColor(Color.WHITESMOKE).build();
        dayRight   = SplitFlapBuilder.create().prefWidth(146).prefHeight(250).flipTime(300).selection(SplitFlap.ALPHA).textColor(Color.WHITESMOKE).build();

        dateLeft   = SplitFlapBuilder.create().prefWidth(146).prefHeight(250).flipTime(300).selection(SplitFlap.NUMERIC).textColor(Color.WHITESMOKE).build();
        dateRight  = SplitFlapBuilder.create().prefWidth(146).prefHeight(250).flipTime(300).selection(SplitFlap.NUMERIC).textColor(Color.WHITESMOKE).build();

        monthLeft  = SplitFlapBuilder.create().prefWidth(146).prefHeight(250).flipTime(300).selection(SplitFlap.ALPHA).textColor(Color.WHITESMOKE).build();
        monthMid   = SplitFlapBuilder.create().prefWidth(146).prefHeight(250).flipTime(300).selection(SplitFlap.ALPHA).textColor(Color.WHITESMOKE).build();
        monthRight = SplitFlapBuilder.create().prefWidth(146).prefHeight(250).flipTime(300).selection(SplitFlap.ALPHA).textColor(Color.WHITESMOKE).build();

        hourLeft   = SplitFlapBuilder.create().prefWidth(200).prefHeight(343).flipTime(300).selection(SplitFlap.TIME_0_TO_5).textColor(Color.WHITESMOKE).build();
        hourRight  = SplitFlapBuilder.create().prefWidth(200).prefHeight(343).flipTime(300).selection(SplitFlap.TIME_0_TO_9).textColor(Color.WHITESMOKE).build();
        minLeft    = SplitFlapBuilder.create().prefWidth(200).prefHeight(343).flipTime(300).selection(SplitFlap.TIME_0_TO_5).textColor(Color.WHITESMOKE).build();
        minRight   = SplitFlapBuilder.create().prefWidth(200).prefHeight(343).flipTime(300).selection(SplitFlap.TIME_0_TO_9).textColor(Color.WHITESMOKE).build();
        secLeft    = SplitFlapBuilder.create().prefWidth(200).prefHeight(343).flipTime(300).selection(SplitFlap.TIME_0_TO_5).textColor(Color.WHITESMOKE).build();
        secRight   = SplitFlapBuilder.create().prefWidth(200).prefHeight(343).flipTime(300).selection(SplitFlap.TIME_0_TO_9).textColor(Color.WHITESMOKE).build();
        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTimerCall + 500_000_000l) {
                    String day = WEEK_DAYS[LocalDateTime.now().getDayOfWeek().getValue()];
                    if (day.equals("SAT") || day.equals("SUN")) {
                        if (!dayLeft.getTextColor().equals(Color.CRIMSON)) {
                            dayLeft.setTextColor(Color.CRIMSON);
                            dayMid.setTextColor(Color.CRIMSON);
                            dayRight.setTextColor(Color.CRIMSON);
                        }
                    } else {
                        if (!dayLeft.getText().equals(Color.WHITESMOKE)) {
                            dayLeft.setTextColor(Color.WHITESMOKE);
                            dayMid.setTextColor(Color.WHITESMOKE);
                            dayRight.setTextColor(Color.WHITESMOKE);
                        }
                    }
                    dayLeft.setText(day.substring(0, 1));
                    dayMid.setText(day.substring(1, 2));
                    dayRight.setText(day.substring(2));

                    date = LocalDateTime.now().getDayOfMonth();
                    String dateString = Integer.toString(date);
                    if (date < 10) {
                        dateLeft.setText("0");
                        dateRight.setText(dateString.substring(0, 1));
                    } else {
                        dateLeft.setText(dateString.substring(0, 1));
                        dateRight.setText(dateString.substring(1));
                    }

                    String month = MONTHS[LocalDateTime.now().getMonthValue() - 1];
                    monthLeft.setText(month.substring(0, 1));
                    monthMid.setText(month.substring(1, 2));
                    monthRight.setText(month.substring(2));

                    hours = LocalDateTime.now().getHour();
                    String hourString = Integer.toString(hours);
                    if (hours < 10) {
                        hourLeft.setText("0");
                        hourRight.setText(hourString.substring(0, 1));
                    } else {
                        hourLeft.setText(hourString.substring(0, 1));
                        hourRight.setText(hourString.substring(1));
                    }

                    minutes = LocalDateTime.now().getMinute();
                    String minutesString = Integer.toString(minutes);
                    if (minutes < 10) {
                        minLeft.setText("0");
                        minRight.setText(minutesString.substring(0, 1));
                    } else {
                        minLeft.setText(minutesString.substring(0, 1));
                        minRight.setText(minutesString.substring(1));
                    }

                    seconds = LocalDateTime.now().getSecond();
                    String secondsString = Integer.toString(seconds);
                    if (seconds < 10) {
                        secLeft.setText("0");
                        secRight.setText(secondsString.substring(0, 1));
                    } else {
                        secLeft.setText(secondsString.substring(0, 1));
                        secRight.setText(secondsString.substring(1));
                    }
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        // Create the red flaps for the seconds
        SplitFlapImgFactory.INSTANCE.setFlapColor(Color.web("#FF1200"));        
        secLeft.setBackgroundImage(SplitFlapImgFactory.INSTANCE.createBackgroundImage(234, 402));
        secLeft.setFlapImage(SplitFlapImgFactory.INSTANCE.createFlapImage(234, 402));
        secRight.setBackgroundImage(SplitFlapImgFactory.INSTANCE.createBackgroundImage(234, 402));
        secRight.setFlapImage(SplitFlapImgFactory.INSTANCE.createFlapImage(234, 402));
        
        HBox dayBox = new HBox();
        dayBox.setSpacing(0);
        dayBox.getChildren().addAll(dayLeft, dayMid, dayRight);
        dayBox.setLayoutX(12);
        dayBox.setLayoutY(76);

        HBox dateBox = new HBox();
        dateBox.setSpacing(0);
        dateBox.getChildren().addAll(dateLeft, dateRight);
        dateBox.setLayoutX(495);
        dateBox.setLayoutY(76);

        HBox monthBox = new HBox();
        monthBox.setSpacing(0);
        monthBox.getChildren().addAll(monthLeft, monthMid, monthRight);
        monthBox.setLayoutX(833);
        monthBox.setLayoutY(76);

        HBox clockBox = new HBox();
        clockBox.setSpacing(0);
        HBox.setMargin(hourRight, new Insets(0, 40, 0, 0));
        HBox.setMargin(minRight, new Insets(0, 40, 0, 0));
        clockBox.getChildren().addAll(hourLeft, hourRight, minLeft, minRight, secLeft, secRight);
        clockBox.setLayoutY(375);

        Pane pane = new Pane(dayBox, dateBox, monthBox, clockBox);
        pane.setPadding(new Insets(10, 10, 10, 10));

        Scene scene = new Scene(pane, 1280, 800, new LinearGradient(0, 0, 0, 800, false, CycleMethod.NO_CYCLE,
                                                                    new Stop(0.0, Color.rgb(28, 27, 22)),
                                                                    new Stop(0.25, Color.rgb(38, 37, 32)),
                                                                    new Stop(1.0, Color.rgb(28, 27, 22))));
        scene.setCamera(new PerspectiveCamera());
        
        stage.setScene(scene);        
        stage.show();

        timer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
