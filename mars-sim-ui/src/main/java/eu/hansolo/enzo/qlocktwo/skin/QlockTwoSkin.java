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

package eu.hansolo.enzo.qlocktwo.skin;

import eu.hansolo.enzo.common.BrushedMetalPaint;
import eu.hansolo.enzo.fonts.Fonts;
import eu.hansolo.enzo.qlocktwo.QlockTwo;
import eu.hansolo.enzo.qlocktwo.QlockWord;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.time.LocalTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;


public class QlockTwoSkin extends SkinBase<QlockTwo> implements Skin<QlockTwo> {
    private static final double PREFERRED_WIDTH  = 200;
    private static final double PREFERRED_HEIGHT = 200;
    private static final double MINIMUM_WIDTH    = 50;
    private static final double MINIMUM_HEIGHT   = 50;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private          double                   size;
    private          int                      hour;
    private          int                      minute;
    private          int                      second;
    private          QlockTwo.SecondsLeft     secondLeft;
    private          QlockTwo.SecondsRight    secondRight;    
    private          BrushedMetalPaint        texture;
    private          Pane                     pane;
    private          Region                   background;
    private          ImageView                stainlessBackground;
    private          Region                   p1;
    private          Region                   p2;
    private          Region                   p3;
    private          Region                   p4;
    private          Label[][]                matrix;
    private          Region                   highlight;
    private          Font                     font;
    private          double                   startX;
    private          double                   startY;
    private          double                   stepX;
    private          double                   stepY;
    private volatile ScheduledFuture<?>       periodicClockTask;
    private static   ScheduledExecutorService periodicClockExecutorService;    


    // ******************** Constructors **************************************
    public QlockTwoSkin(final QlockTwo CONTROL) {
        super(CONTROL);
        hour                 = 0;
        minute               = 0;
        second               = 0;
        secondLeft           = QlockTwo.SecondsLeft.ZERO;
        secondRight          = QlockTwo.SecondsRight.ZERO;        
        texture              = new BrushedMetalPaint(Color.web("#888888"));
        stainlessBackground  = new ImageView();
        pane                 = new Pane();        
        init();
        initGraphics();
        registerListeners();
        scheduleClockTask();
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
        startX     = PREFERRED_WIDTH * 0.114;
        startY     = PREFERRED_WIDTH * 0.112;
        stepX      = PREFERRED_WIDTH * 0.072;
        stepY      = PREFERRED_WIDTH * 0.08;                
        background = new Region();
        background.getStyleClass().setAll("background", getSkinnable().getColor().STYLE_CLASS);

        stainlessBackground.setImage(texture.getImage(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        stainlessBackground.setVisible(getSkinnable().getColor() == QlockTwo.QlockColor.STAINLESS_STEEL);
        stainlessBackground.setManaged(getSkinnable().getColor() == QlockTwo.QlockColor.STAINLESS_STEEL);

        p1 = new Region();
        p1.getStyleClass().add("dot-off");
        p2 = new Region();
        p2.getStyleClass().add("dot-off");
        p3 = new Region();
        p3.getStyleClass().add("dot-off");
        p4 = new Region();
        p4.getStyleClass().add("dot-off");

        highlight = new Region();
        highlight.getStyleClass().add("highlight");

        matrix = new Label[11][10];
        IntStream.range(0, 10).parallel().forEachOrdered(
            y -> {
                IntStream.range(0, 11).parallel().forEachOrdered(
                    x -> {
                        matrix[x][y] = new Label();
                        matrix[x][y].setAlignment(Pos.CENTER);
                        matrix[x][y].setPrefWidth(PREFERRED_WIDTH * 0.048);
                        matrix[x][y].setPrefHeight(PREFERRED_HEIGHT * 0.048);
                        matrix[x][y].setText(getSkinnable().getQlock().getMatrix()[y][x]);
                        matrix[x][y].getStyleClass().add("text-off");    
                    }
                );
            }
        );               
        pane.getChildren().setAll(background,
                                  stainlessBackground,
                                  p4,
                                  p3,
                                  p2,
                                  p1);        
        IntStream.range(0, 10).parallel().forEachOrdered(
            y -> {
                IntStream.range(0, 11).parallel().forEachOrdered(
                    x -> {
                        pane.getChildren().add(matrix[x][y]);
                    }
                );
            }
        );
        pane.getChildren().add(highlight);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(observable -> handleControlPropertyChanged("RESIZE") );
        getSkinnable().heightProperty().addListener(observable -> handleControlPropertyChanged("RESIZE") );
        getSkinnable().prefWidthProperty().addListener(observable -> handleControlPropertyChanged("PREF_SIZE") );
        getSkinnable().prefHeightProperty().addListener(observable -> handleControlPropertyChanged("PREF_SIZE") );
        getSkinnable().colorProperty().addListener(observable -> handleControlPropertyChanged("COLOR") );
        getSkinnable().languageProperty().addListener(observable -> handleControlPropertyChanged("LANGUAGE") );
        getSkinnable().highlightVisibleProperty().addListener(observable -> handleControlPropertyChanged( "HIGHLIGHT") );

        getSkinnable().getStyleClass().addListener(new ListChangeListener<String>() {
            @Override public void onChanged(Change<? extends String> change) {
                resize();
            }
        });
    }


    // ******************** Methods *******************************************
    protected void handleControlPropertyChanged(final String PROPERTY) {
        if ("RESIZE".equals(PROPERTY)) {
            resize();
        } else if ("COLOR".equals(PROPERTY)) {            
            background.getStyleClass().setAll("background", getSkinnable().getColor().STYLE_CLASS);
            stainlessBackground.setVisible(getSkinnable().getColor() == QlockTwo.QlockColor.STAINLESS_STEEL);
            stainlessBackground.setManaged(getSkinnable().getColor() == QlockTwo.QlockColor.STAINLESS_STEEL);
            IntStream.range(0, 10).parallel().forEachOrdered(
                y -> IntStream.range(0, 11).parallel().forEachOrdered(
                    x -> {
                        if (matrix[x][y].getStyleClass().contains("text-off")) {
                            matrix[x][y].getStyleClass().setAll("text-off", getSkinnable().getColor().STYLE_CLASS);
                        }
                    }
                )
            );
            if (p1.getStyleClass().contains("dot-off")) {
                p1.getStyleClass().setAll("dot-off", getSkinnable().getColor().STYLE_CLASS);
            }
            if (p2.getStyleClass().contains("dot-off")) {
                p2.getStyleClass().setAll("dot-off", getSkinnable().getColor().STYLE_CLASS);
            }
            if (p3.getStyleClass().contains("dot-off")) {
                p3.getStyleClass().setAll("dot-off", getSkinnable().getColor().STYLE_CLASS);
            }
            if (p4.getStyleClass().contains("dot-off")) {
                p4.getStyleClass().setAll("dot-off", getSkinnable().getColor().STYLE_CLASS);
            }
        } else if ("LANGUAGE".equals(PROPERTY)) {            
            IntStream.range(0, 10).parallel().forEachOrdered(
                y -> IntStream.range(0, 11).parallel().forEachOrdered(
                    x -> matrix[x][y].setText(getSkinnable().getQlock().getMatrix()[y][x])
                )
            );
        } else if ("HIGHLIGHT".equals(PROPERTY)) {
            highlight.setOpacity(getSkinnable().isHighlightVisible() ? 1 : 0);
        }
    }

    
    // ******************** Scheduled tasks ***********************************
    private synchronized static void enableClockExecutorService() {
        if (null == periodicClockExecutorService) {
            periodicClockExecutorService = new ScheduledThreadPoolExecutor(1, getThreadFactory("QlockTwo", true));
        }
    }
    private synchronized void scheduleClockTask() {
        enableClockExecutorService();
        stopTask(periodicClockTask);
        periodicClockTask = periodicClockExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override public void run() {
                if (getSkinnable().isVisible()) {
                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                            updateClock();
                        }
                    });
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }
    private static ThreadFactory getThreadFactory(final String THREAD_NAME, final boolean IS_DAEMON) {
        return new ThreadFactory() {
            @Override public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, THREAD_NAME);
                thread.setDaemon(IS_DAEMON);
                return thread;
            }
        };
    }
    private void stopTask(ScheduledFuture<?> task) {
        if (null == task) return;

        task.cancel(true);
        task = null;
    }
    
    
    // ******************** Update ********************************************
    private void updateClock() {
        hour   = LocalTime.now().getHour();
        minute = LocalTime.now().getMinute();
        second = LocalTime.now().getSecond();

        // SecondsRight        
        if (getSkinnable().isSecondsMode()) {
            if (second < 10) {
                secondLeft  = QlockTwo.SecondsLeft.ZERO;
                secondRight = QlockTwo.SecondsRight.values()[second];
            } else {
                secondLeft  = QlockTwo.SecondsLeft.values()[Integer.parseInt(Integer.toString(second).substring(0, 1))];
                secondRight = QlockTwo.SecondsRight.values()[Integer.parseInt(Integer.toString(second).substring(1, 2))];
            }
            IntStream.range(0, 10).forEachOrdered(
                y -> {
                    IntStream.range(0, 11).forEachOrdered(
                        x -> {
                            if (secondLeft.dots.containsKey(y) || secondRight.dots.containsKey(y)) {
                                if (secondLeft.dots.containsKey(y) && secondLeft.dots.get(y).contains(x)) {
                                    matrix[x][y].getStyleClass().setAll("text-on", getSkinnable().getColor().STYLE_CLASS);
                                } else if (secondRight.dots.containsKey(y) && secondRight.dots.get(y).contains(x)) {
                                    matrix[x][y].getStyleClass().setAll("text-on", getSkinnable().getColor().STYLE_CLASS);
                                } else {
                                    matrix[x][y].getStyleClass().setAll("text-off", getSkinnable().getColor().STYLE_CLASS);
                                }
                            } else {
                                matrix[x][y].getStyleClass().setAll("text-off", getSkinnable().getColor().STYLE_CLASS);
                            }    
                        }
                    );
                }
            );                        
        } else {            
            IntStream.range(0, 10).forEachOrdered(
                y -> {
                    IntStream.range(0, 11).forEachOrdered(
                        x -> {
                            matrix[x][y].getStyleClass().setAll("text-off", getSkinnable().getColor().STYLE_CLASS);
                        }
                    );
                }
            );

            for (QlockWord word : getSkinnable().getQlock().getTime(minute, hour)) {
                for (int col = word.getStart() ; col <= word.getStop() ; col++) {
                    matrix[col][word.getRow()].getStyleClass().setAll("text-on", getSkinnable().getColor().STYLE_CLASS);
                }
            }
        }
        int min = minute > 60 ? minute - 60 : (minute < 0 ? minute + 60 : minute);

        if (min %5 == 0) {
            p1.getStyleClass().setAll("dot-off", getSkinnable().getColor().STYLE_CLASS);
            p2.getStyleClass().setAll("dot-off", getSkinnable().getColor().STYLE_CLASS);
            p3.getStyleClass().setAll("dot-off", getSkinnable().getColor().STYLE_CLASS);
            p4.getStyleClass().setAll("dot-off", getSkinnable().getColor().STYLE_CLASS);
        } else if (min %10 == 1 || min %10 == 6) {
            p1.getStyleClass().setAll("dot-on", getSkinnable().getColor().STYLE_CLASS);
        } else if (min %10 == 2 || min %10 == 7) {
            p1.getStyleClass().setAll("dot-on", getSkinnable().getColor().STYLE_CLASS);
            p2.getStyleClass().setAll("dot-on", getSkinnable().getColor().STYLE_CLASS);
        } else if (min %10 == 3 || min %10 == 8) {
            p1.getStyleClass().setAll("dot-on", getSkinnable().getColor().STYLE_CLASS);
            p2.getStyleClass().setAll("dot-on", getSkinnable().getColor().STYLE_CLASS);
            p3.getStyleClass().setAll("dot-on", getSkinnable().getColor().STYLE_CLASS);
        } else if (min %10 == 4 || min %10 == 9) {
            p1.getStyleClass().setAll("dot-on", getSkinnable().getColor().STYLE_CLASS);
            p2.getStyleClass().setAll("dot-on", getSkinnable().getColor().STYLE_CLASS);
            p3.getStyleClass().setAll("dot-on", getSkinnable().getColor().STYLE_CLASS);
            p4.getStyleClass().setAll("dot-on", getSkinnable().getColor().STYLE_CLASS);
        }
    }


    // ******************** Resizing ******************************************
    private void resize() {
        size = getSkinnable().getWidth() < getSkinnable().getHeight() ? getSkinnable().getWidth() : getSkinnable().getHeight();               

        if (size > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((getSkinnable().getWidth() - size) * 0.5, (getSkinnable().getHeight() - size) * 0.5);
            background.setPrefSize(size, size);
            
            if (getSkinnable().getColor() == QlockTwo.QlockColor.STAINLESS_STEEL) {
                stainlessBackground.setImage(texture.getImage(size, size));
            }            

            p4.setPrefSize(0.012 * size, 0.012 * size);
            p4.setTranslateX(0.044 * size);
            p4.setTranslateY(0.944 * size);

            p3.setPrefSize(0.012 * size, 0.012 * size);
            p3.setTranslateX(0.944 * size);
            p3.setTranslateY(0.944 * size);

            p2.setPrefSize(0.012 * size, 0.012 * size);
            p2.setTranslateX(0.944 * size);
            p2.setTranslateY(0.044 * size);

            p1.setPrefSize(0.012 * size, 0.012 * size);
            p1.setTranslateX(0.044 * size);
            p1.setTranslateY(0.044 * size);

            startX = size * 0.114;
            startY = size * 0.112;
            stepX  = size * 0.072;
            stepY  = size * 0.08;            
            font = Fonts.dinFun(size * 0.048);

            IntStream.range(0, 10).parallel().forEachOrdered(
                y -> IntStream.range(0, 11).parallel().forEachOrdered(
                    x -> {
                        matrix[x][y].setFont(font);
                        matrix[x][y].setPrefSize(size * 0.048, size * 0.048);
                        matrix[x][y].setTranslateY(startY + y * stepY);
                        matrix[x][y].setTranslateX(startX + x * stepX);
                        matrix[x][y].setTranslateY(startY + y * stepY);
                    }
                )
            );
                        
            highlight.setPrefSize(0.8572706909179687 * size, 0.7135147094726563 * size);
            highlight.setTranslateX(0.14224906921386718 * size);
            highlight.setTranslateY(0.28614569091796876 * size);
        }
    }
}
