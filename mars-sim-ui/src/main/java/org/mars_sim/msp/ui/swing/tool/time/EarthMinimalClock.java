/* Mars Simulation Project
 * EarthMinimalClock.java
 * @version 3.1.0 2016-06-21
 * @author Manny Kung
 */

/*
 * Copyright (c) 2014 by Gerrit Grunwald
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

package org.mars_sim.msp.ui.swing.tool.time;

//import com.sun.javafx.css.converters.PaintConverter;
//import javafx.application.Platform;
//import javafx.beans.property.BooleanProperty;
//import javafx.beans.property.ObjectProperty;
//import javafx.beans.property.SimpleBooleanProperty;
//import javafx.css.CssMetaData;
//import javafx.css.Styleable;
//import javafx.css.StyleableObjectProperty;
//import javafx.css.StyleableProperty;
//import javafx.scene.control.Control;
//import javafx.scene.control.Label;
//import javafx.scene.layout.Pane;
//import javafx.scene.layout.Region;
//import javafx.scene.paint.Color;
//import javafx.scene.paint.Paint;
//import javafx.scene.shape.Arc;
//
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.*;
//
//import org.mars_sim.msp.core.Simulation;
//import org.mars_sim.msp.core.time.EarthClock;
//import org.mars_sim.msp.ui.javafx.Fonts;

/**
 * User: hansolo
 * Date: 11.09.14
 * Time: 23:58

public class EarthMinimalClock extends Region {
    private static final double               PREFERRED_WIDTH    = 320;
    private static final double               PREFERRED_HEIGHT   = 320;
    private static final double               MINIMUM_WIDTH      = 50;
    private static final double               MINIMUM_HEIGHT     = 50;
    private static final double               MAXIMUM_WIDTH      = 1024;
    private static final double               MAXIMUM_HEIGHT     = 1024;

    //private static final DateTimeFormatter    HOURS   = DateTimeFormatter.ofPattern("H");
    //private static final DateTimeFormatter    MINUTES = DateTimeFormatter.ofPattern("m");
    //private static final DateTimeFormatter    DATE    = DateTimeFormatter.ofPattern("E, dd MMM");

    public static final  Color                DEFAULT_CLOCK_BACKGROUND_COLOR   = Color.rgb(255, 255, 255, 0.3);
    public static final  Color                DEFAULT_CLOCK_BORDER_COLOR       = Color.rgb(255, 255, 255, 0.6);
    public static final  Color                DEFAULT_MINUTES_BACKGROUND_COLOR = Color.rgb(59, 209, 255, 1.0);
    public static final  Color                DEFAULT_HOUR_TEXT_COLOR          = Color.WHITE;
    public static final  Color                DEFAULT_DATE_TEXT_COLOR          = Color.WHITE;
    public static final  Color                DEFAULT_MINUTES_TEXT_COLOR       = Color.WHITE;
    public static final  Color                DEFAULT_SECONDS_COLOR            = Color.WHITE;

    private double                            size;
    private double                            width;
    private double                            height;
    private double                            centerX;
    private double                            centerY;
    private Label                             hourLabel;
    private Label                             weekDayLabel;
    private Label                             dateLabel;

    //private Label                             minutesLabel;
    private Arc                               seconds;
    private Pane                              pane;

    private BooleanProperty                   weekDayVisible;
    private BooleanProperty                   dateVisible;
    private BooleanProperty                   secondsVisible;

    private ObjectProperty<Paint>             clockBackgroundColor;
    private ObjectProperty<Paint>             clockBorderColor;
    private ObjectProperty<Paint>             minutesBackgroundColor;
    private ObjectProperty<Paint>             hourTextColor;
    private ObjectProperty<Paint>             dateTextColor;
    private ObjectProperty<Paint>             minutesTextColor;
    private ObjectProperty<Paint>             secondsColor;

    private          EarthClock               earthClock = Simulation.instance().getMasterClock().getEarthClock();

    //private          Clock                    clock;
    //private          LocalDateTime            ldt;
    
    private volatile ScheduledFuture<?>       secondTask;
    private static   ScheduledExecutorService periodicSecondExecutorService;


    // ******************** Constructors **************************************
    public EarthMinimalClock() { this(true, true); }
    public EarthMinimalClock(final boolean DATE_VISIBLE) { this(DATE_VISIBLE, true); }
    //public EarthMinimalClock(final boolean DATE_VISIBLE, true) { this(DATE_VISIBLE, true, true); }
    //public EarthMinimalClock(final boolean WEEKDAY_VISIBLE) { this(true, WEEKDAY_VISIBLE, true); }
    public EarthMinimalClock(final boolean DATE_VISIBLE, final boolean START) {
        getStylesheets().add(EarthMinimalClock.class.getResource("/css/minimalclock.css").toExternalForm());
        getStyleClass().add("minimal-clock");

        //clock                  = Clock.systemDefaultZone();
        //ldt                    = LocalDateTime.ofInstant(clock.instant(), ZoneId.systemDefault());

        dateVisible            = new SimpleBooleanProperty(this, "dateVisible", DATE_VISIBLE);
        secondsVisible         = new SimpleBooleanProperty(this, "secondsVisible", true);
        //weekDayVisible         = new SimpleBooleanProperty(this, "weekDayVisible", WEEKDAY_VISIBLE);

        clockBackgroundColor   = new StyleableObjectProperty<Paint>(DEFAULT_CLOCK_BACKGROUND_COLOR) {
            @Override public void set(final Paint CLOCK_BACKGROUND_COLOR) {
                super.set(CLOCK_BACKGROUND_COLOR);
                applyStyles();
            }
            @Override public CssMetaData getCssMetaData() { return StyleableProperties.CLOCK_BACKGROUND_COLOR; }
            @Override public Object getBean() { return EarthMinimalClock.this; }
            @Override public String getName() { return "clockBackgroundColor"; }
        };
        clockBorderColor       = new StyleableObjectProperty<Paint>(DEFAULT_CLOCK_BORDER_COLOR) {
            @Override public void set(final Paint CLOCK_BORDER_COLOR) {
                super.set(CLOCK_BORDER_COLOR);
                applyStyles();
            }
            @Override public CssMetaData getCssMetaData() { return StyleableProperties.CLOCK_BORDER_COLOR; }
            @Override public Object getBean() { return EarthMinimalClock.this; }
            @Override public String getName() { return "clockBorderColor"; }
        };
        minutesBackgroundColor = new StyleableObjectProperty<Paint>(DEFAULT_MINUTES_BACKGROUND_COLOR) {
            @Override public void set(final Paint MINUTES_BACKGROUND_COLOR) {
                super.set(MINUTES_BACKGROUND_COLOR);
                applyStyles();
            }
            @Override public CssMetaData getCssMetaData() { return StyleableProperties.MINUTES_BACKGROUND_COLOR; }
            @Override public Object getBean() { return EarthMinimalClock.this; }
            @Override public String getName() { return "minutesBackgroundColor"; }
        };

        hourTextColor          = new StyleableObjectProperty<Paint>(DEFAULT_HOUR_TEXT_COLOR) {
            @Override public void set(final Paint HOUR_TEXT_COLOR) {
                super.set(HOUR_TEXT_COLOR);
                applyStyles();
            }
            @Override public CssMetaData getCssMetaData() { return StyleableProperties.HOUR_TEXT_COLOR; }
            @Override public Object getBean() { return EarthMinimalClock.this; }
            @Override public String getName() { return "hourTextColor"; }
        };
        dateTextColor          = new StyleableObjectProperty<Paint>(DEFAULT_DATE_TEXT_COLOR) {
            @Override public void set(final Paint DATE_TEXT_COLOR) {
                super.set(DATE_TEXT_COLOR);
                applyStyles();
            }
            @Override public CssMetaData getCssMetaData() { return StyleableProperties.DATE_TEXT_COLOR; }
            @Override public Object getBean() { return EarthMinimalClock.this; }
            @Override public String getName() { return "dateTextColor"; }
        };
        minutesTextColor       = new StyleableObjectProperty<Paint>(DEFAULT_MINUTES_TEXT_COLOR) {
            @Override public void set(final Paint MINUTES_TEXT_COLOR) {
                super.set(MINUTES_TEXT_COLOR);
                applyStyles();
            }
            @Override public CssMetaData getCssMetaData() { return StyleableProperties.MINUTES_TEXT_COLOR; }
            @Override public Object getBean() { return EarthMinimalClock.this; }
            @Override public String getName() { return "minutesTextColor"; }
        };
        secondsColor           = new StyleableObjectProperty<Paint>(DEFAULT_SECONDS_COLOR) {
            @Override public void set(final Paint SECONDS_COLOR) {
                super.set(SECONDS_COLOR);
                applyStyles();
            }
            @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECONDS_COLOR; }
            @Override public Object getBean() { return EarthMinimalClock.this; }
            @Override public String getName() { return "secondsColor"; }
        };

        init();
        initGraphics();
        registerListeners();
        if (START) { scheduleSecondTask(); }
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

        weekDayLabel = new Label();
        weekDayLabel.getStyleClass().add("weekDay-label");
        //weekDayLabel.setVisible(isWeekDayVisible());

        hourLabel = new Label();
        hourLabel.getStyleClass().add("hour-label");

        dateLabel = new Label();
        dateLabel.getStyleClass().add("date-label");
        dateLabel.setVisible(isDateVisible());

        //minutesLabel = new Label();
        //minutesLabel.getStyleClass().add("minutes-label");

        seconds = new Arc();
        seconds.getStyleClass().add("seconds");
        seconds.setVisible(isSecondsVisible());

        pane = new Pane();
        pane.getChildren().setAll(hourLabel, dateLabel, weekDayLabel, seconds);//, minutesLabel);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(observable -> resize());
        heightProperty().addListener(observable -> resize());
        dateVisible.addListener(observable -> handleControlPropertyChanged("DATE_VISIBLE"));
        secondsVisible.addListener(observable -> handleControlPropertyChanged("SECONDS_VISIBLE"));
        //weekDayVisible.addListener(observable -> handleControlPropertyChanged("WEEKDAY_VISIBLE"));
    }


    // ******************** Public Methods ************************************
    private void handleControlPropertyChanged(final String PROPERTY) {
        if ("DATE_VISIBLE".equals(PROPERTY)) {
            dateLabel.setVisible(isDateVisible());
            resize();
        } else if ("SECONDS_VISIBLE".equals(PROPERTY)) {
            seconds.setVisible(isSecondsVisible());
            resize();
        //} else if ("WEEKDAY_VISIBLE".equals(PROPERTY)) {
        //    weekDayLabel.setVisible(isWeekDayVisible());
        //    resize();
        }
    }

    public final void start() { scheduleSecondTask(); }
    public final void stop() { stopTask(secondTask); }

    //public final boolean isWeekDayVisible() { return weekDayVisible.get(); }
    //public final void setWeekDayVisible(final boolean WEEKDAY_VISIBLE) { weekDayVisible.set(WEEKDAY_VISIBLE); }
    //public final BooleanProperty weekDayVisibleProperty() { return weekDayVisible; }

    public final boolean isDateVisible() { return dateVisible.get(); }
    public final void setDateVisible(final boolean DATE_VISIBLE) { dateVisible.set(DATE_VISIBLE); }
    public final BooleanProperty dateVisibleProperty() { return dateVisible; }

    public final boolean isSecondsVisible() { return secondsVisible.get(); }
    public final void setSecondsVisible(final boolean SECONDS_VISIBLE) { secondsVisible.set(SECONDS_VISIBLE); }
    public final BooleanProperty secondsVisibleProperty() { return secondsVisible; }

    public final Paint getClockBackgroundColor() { return clockBackgroundColor.get(); }
    public final void setClockBackgroundColor(final Paint CLOCK_BACKGROUND_COLOR) { clockBackgroundColor.set(CLOCK_BACKGROUND_COLOR); }
    public final ObjectProperty<Paint> clockBackgroundColorProperty() { return clockBackgroundColor; }

    public final Paint getClockBorderColor() { return clockBorderColor.get(); }
    public final void setClockBorderColor(final Paint CLOCK_BORDER_COLOR) { clockBorderColor.set(CLOCK_BORDER_COLOR); }
    public final ObjectProperty<Paint> clockBorderColorProperty() { return clockBorderColor; }

    public final Paint getMinutesBackgroundColor() { return minutesBackgroundColor.get(); }
    public final void setMinutesBackgroundColor(final Paint MINUTES_BACKGROUND_COLOR) { minutesBackgroundColor.set(MINUTES_BACKGROUND_COLOR); }
    public final ObjectProperty<Paint> minutesBackgroundColorProperty() { return minutesBackgroundColor; }

    public final Paint getHourTextColor() { return hourTextColor.get(); }
    public final void setHourTextColor(final Paint HOUR_TEXT_COLOR) { hourTextColor.set(HOUR_TEXT_COLOR); }
    public final ObjectProperty<Paint> hourTextColorProperty() { return hourTextColor; }

    public final Paint getDateTextColor() { return dateTextColor.get(); }
    public final void setDateTextColor(final Paint DATE_TEXT_COLOR) { dateTextColor.set(DATE_TEXT_COLOR); }
    public final ObjectProperty<Paint> dateTextColorProperty() { return dateTextColor; }

    public final Paint getMinutesTextColor() { return minutesTextColor.get(); }
    public final void setMinutesTextColor(final Paint MINUTES_TEXT_COLOR) { minutesTextColor.set(MINUTES_TEXT_COLOR); }
    public final ObjectProperty<Paint> minutesTextColorProperty() { return minutesTextColor; }

    public final Paint getSecondsColor() { return secondsColor.get(); }
    public final void setSecondsColor(final Paint SECONDS_COLOR) { secondsColor.set(SECONDS_COLOR); }
    public final ObjectProperty<Paint> secondsColorProperty() { return secondsColor; }


    // ******************** Private methods ***********************************
    private void applyStyles() {
        setStyle("-clock-background-color:" + getClockBackgroundColor().toString().replace("0x", "#") + ";" +
                "-clock-border-color:" + getClockBorderColor().toString().replace("0x", "#") + ";" +
                "-minutes-background-color:" + getMinutesBackgroundColor().toString().replace("0x", "#") + ";" +
                "-hour-text-color:" + getHourTextColor().toString().replace("0x", "#") + ";" +
                "-date-text-color:" + getDateTextColor().toString().replace("0x", "#") + ";" +
                "-minutes-text-color:" + getMinutesTextColor().toString().replace("0x", "#") + ";" +
                "-seconds-color:" + getSecondsColor().toString().replace("0x", "#") + ";");
    }

    private synchronized static void enableSecondExecutorService() {
        if (null == periodicSecondExecutorService) {
            periodicSecondExecutorService = new ScheduledThreadPoolExecutor(1, getThreadFactory("SecondTask", false));
        }
    }
    private synchronized void scheduleSecondTask() {
        enableSecondExecutorService();
        stopTask(secondTask);
        secondTask = periodicSecondExecutorService.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                //ldt = LocalDateTime.ofInstant(clock.instant(), ZoneId.systemDefault());
                //hourLabel.setText(HOURS.format(ldt));
                //dateLabel.setText(DATE.format(ldt));

                weekDayLabel.setText(earthClock.getDayOfWeekString());
                hourLabel.setText(earthClock.getHourString() + ":" + earthClock.getMinuteString());
                dateLabel.setText(earthClock.getDayOfMonth()+ " "
                        + earthClock.getMonthString() + " "
                        + earthClock.getYear());
                
                //minutesLabel.setText(earthClock.getMinuteString()); //MINUTES.format(ldt));
                //double angle    = 6 * earthClock.getMinute();// + 0.1 * earthClock.getSecond();
                //double sinValue = Math.sin(Math.toRadians(-angle + 180));
                //double cosValue = Math.cos(Math.toRadians(-angle + 180));
                //minutesLabel.relocate(centerX + size * 0.31 * sinValue, centerY + size * 0.31 * cosValue);

                seconds.setLength(-6 * earthClock.getSecond());
            });
        }, 1, 1, TimeUnit.SECONDS);
    }

    private static ThreadFactory getThreadFactory(final String THREAD_NAME, final boolean IS_DAEMON) {
        return runnable -> {
            Thread thread = new Thread(runnable, THREAD_NAME);
            thread.setDaemon(IS_DAEMON);
            return thread;
        };
    }

    private void stopTask(ScheduledFuture<?> task) {
        if (null == task) return;
        task.cancel(true);
        task = null;
    }


    // ******************** CSS Meta Data *************************************
    private static class StyleableProperties {
        private static final CssMetaData<EarthMinimalClock, Paint> CLOCK_BACKGROUND_COLOR =
                new CssMetaData<EarthMinimalClock, Paint>("-clock-background-color", PaintConverter.getInstance(), DEFAULT_CLOCK_BACKGROUND_COLOR) {
                    @Override public boolean isSettable(EarthMinimalClock node) {
                        return null == node.clockBackgroundColor || !node.clockBackgroundColor.isBound();
                    }
                    @Override public StyleableProperty<Paint> getStyleableProperty(EarthMinimalClock node) {
                        return (StyleableProperty) node.clockBackgroundColorProperty();
                    }
                    @Override public Paint getInitialValue(EarthMinimalClock node) {
                        return node.getClockBackgroundColor();
                    }
                };

        private static final CssMetaData<EarthMinimalClock, Paint> CLOCK_BORDER_COLOR =
                new CssMetaData<EarthMinimalClock, Paint>("-clock-border-color", PaintConverter.getInstance(), DEFAULT_CLOCK_BORDER_COLOR) {
                    @Override public boolean isSettable(EarthMinimalClock node) {
                        return null == node.clockBorderColor || !node.clockBorderColor.isBound();
                    }
                    @Override public StyleableProperty<Paint> getStyleableProperty(EarthMinimalClock node) {
                        return (StyleableProperty) node.clockBorderColorProperty();
                    }
                    @Override public Paint getInitialValue(EarthMinimalClock node) {
                        return node.getClockBorderColor();
                    }
                };

        private static final CssMetaData<EarthMinimalClock, Paint> MINUTES_BACKGROUND_COLOR =
                new CssMetaData<EarthMinimalClock, Paint>("-minutes-background-color", PaintConverter.getInstance(), DEFAULT_MINUTES_BACKGROUND_COLOR) {
                    @Override public boolean isSettable(EarthMinimalClock node) {
                        return null == node.minutesBackgroundColor || !node.minutesBackgroundColor.isBound();
                    }
                    @Override public StyleableProperty<Paint> getStyleableProperty(EarthMinimalClock node) {
                        return (StyleableProperty) node.minutesBackgroundColorProperty();
                    }
                    @Override public Paint getInitialValue(EarthMinimalClock node) {
                        return node.getMinutesBackgroundColor();
                    }
                };

        private static final CssMetaData<EarthMinimalClock, Paint> HOUR_TEXT_COLOR =
                new CssMetaData<EarthMinimalClock, Paint>("-hour-text-color", PaintConverter.getInstance(), DEFAULT_HOUR_TEXT_COLOR) {
                    @Override public boolean isSettable(EarthMinimalClock node) {
                        return null == node.hourTextColor || !node.hourTextColor.isBound();
                    }
                    @Override public StyleableProperty<Paint> getStyleableProperty(EarthMinimalClock node) {
                        return (StyleableProperty) node.hourTextColorProperty();
                    }
                    @Override public Paint getInitialValue(EarthMinimalClock node) {
                        return node.getHourTextColor();
                    }
                };

        private static final CssMetaData<EarthMinimalClock, Paint> DATE_TEXT_COLOR =
                new CssMetaData<EarthMinimalClock, Paint>("-date-text-color", PaintConverter.getInstance(), DEFAULT_DATE_TEXT_COLOR) {
                    @Override public boolean isSettable(EarthMinimalClock node) {
                        return null == node.dateTextColor || !node.dateTextColor.isBound();
                    }
                    @Override public StyleableProperty<Paint> getStyleableProperty(EarthMinimalClock node) {
                        return (StyleableProperty) node.dateTextColorProperty();
                    }
                    @Override public Paint getInitialValue(EarthMinimalClock node) {
                        return node.getDateTextColor();
                    }
                };

        private static final CssMetaData<EarthMinimalClock, Paint> MINUTES_TEXT_COLOR =
                new CssMetaData<EarthMinimalClock, Paint>("-minutes-text-color", PaintConverter.getInstance(), DEFAULT_MINUTES_TEXT_COLOR) {
                    @Override public boolean isSettable(EarthMinimalClock node) {
                        return null == node.minutesTextColor || !node.minutesTextColor.isBound();
                    }
                    @Override public StyleableProperty<Paint> getStyleableProperty(EarthMinimalClock node) {
                        return (StyleableProperty) node.minutesTextColorProperty();
                    }
                    @Override public Paint getInitialValue(EarthMinimalClock node) {
                        return node.getMinutesTextColor();
                    }
                };

        private static final CssMetaData<EarthMinimalClock, Paint> SECONDS_COLOR =
                new CssMetaData<EarthMinimalClock, Paint>("-seconds-color", PaintConverter.getInstance(), DEFAULT_SECONDS_COLOR) {
                    @Override public boolean isSettable(EarthMinimalClock node) {
                        return null == node.secondsColor || !node.secondsColor.isBound();
                    }
                    @Override public StyleableProperty<Paint> getStyleableProperty(EarthMinimalClock node) {
                        return (StyleableProperty) node.secondsColorProperty();
                    }
                    @Override public Paint getInitialValue(EarthMinimalClock node) {
                        return node.getSecondsColor();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables,
                               CLOCK_BACKGROUND_COLOR,
                               CLOCK_BORDER_COLOR,
                               MINUTES_BACKGROUND_COLOR,
                               HOUR_TEXT_COLOR,
                               DATE_TEXT_COLOR,
                               MINUTES_TEXT_COLOR,
                               SECONDS_COLOR);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }


    // ******************** Resizing ******************************************
    private void resize() {
        width = getWidth();
        height = getHeight();
        size = width < height ? width : height;
        centerX = size * 0.5;
        centerY = size * 0.5;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((width - size) * 0.5, (height - size) * 0.5);

            hourLabel.setPrefSize(size * 0.83333, size * 0.83333);
            //hourLabel.setFont(Fonts.robotoThin(size * 0.41667));
            hourLabel.setFont(Fonts.robotoThin(size * 0.2));
            hourLabel.relocate(size * 0.08333, size * 0.08333);
            if (dateLabel.isVisible()) {
                hourLabel.setStyle("-fx-border-width: " + (size * 0.02083) + "; -fx-label-padding: " + (size * 0.11) + " 0 0 0 ;");
            } else {
                hourLabel.setStyle("-fx-border-width: " + (size * 0.02083) + "; -fx-label-padding: 0;");
            }

            dateLabel.setPrefWidth(size * 0.41667);
            dateLabel.setFont(Fonts.robotoLight(size * 0.07));
            dateLabel.relocate((size - dateLabel.getPrefWidth()) * 0.5, (size - dateLabel.getPrefHeight()) * 0.27917 + 15);

            weekDayLabel.setPrefWidth(size * 0.41667);
            weekDayLabel.setFont(Fonts.robotoMedium(size * 0.06));
            weekDayLabel.relocate((size - weekDayLabel.getPrefWidth()) * 0.5, (size - weekDayLabel.getPrefHeight()) * 0.27917 - 20);

            //double angle = 6 * earthClock.getMinute();// + 0.1 * ldt.getSecond();
            //double sinValue = Math.sin(Math.toRadians(-angle + 180));
            //double cosValue = Math.cos(Math.toRadians(-angle + 180));
            //minutesLabel.setPrefSize(0.14583 * size, 0.14583 * size);
            //minutesLabel.setFont(Fonts.robotoLight(size * 0.06667));
            //minutesLabel.relocate(centerX + size * 0.31 * sinValue, centerY + size * 0.31 * cosValue);

            seconds.setCenterX(centerX);
            seconds.setCenterY(centerY);
            seconds.setRadiusX(size * 0.406);
            seconds.setRadiusY(size * 0.406);
            seconds.setStartAngle(90);
            seconds.setLength(-6 * earthClock.getSecond());
            seconds.setStyle("-fx-stroke-width: " + (size * 0.02083) + ";");
        }
    }
}
 */