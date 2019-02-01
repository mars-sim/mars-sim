package org.mars_sim.javafx.tools;

import javafx.util.Duration;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;


import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.tools.Helper;
import eu.hansolo.tilesfx.TileBuilder;

public class TilesfxClock extends Application {
    private static final int            SECONDS_PER_DAY    = 86_400;
    private static final int            SECONDS_PER_HOUR   = 3600;
    private static final int            SECONDS_PER_MINUTE = 60;
    private              Tile           days;
    private              Tile           hours;
    private              Tile           minutes;
    private              Tile           seconds;
    private              Duration       duration;
    private              long           lastTimerCall;
    private              AnimationTimer timer;


    @SuppressWarnings("restriction")
	@Override public void init() {
        days     = createTile("DAYS", "0");
        hours    = createTile("HOURS", "0");
        minutes  = createTile("MINUTES", "0");
        seconds  = createTile("SECONDS", "0");

        duration = Duration.hours(72);

        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall + 1_000_000_000l) {
                    duration = duration.subtract(Duration.seconds(1));

                    int remainingSeconds = (int) duration.toSeconds();
                    int d = remainingSeconds / SECONDS_PER_DAY;
                    int h = (remainingSeconds % SECONDS_PER_DAY) / SECONDS_PER_HOUR;
                    int m = ((remainingSeconds % SECONDS_PER_DAY) % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE;
                    int s = (((remainingSeconds % SECONDS_PER_DAY) % SECONDS_PER_HOUR) % SECONDS_PER_MINUTE);

                    if (d == 0 && h == 0 && m == 0 && s == 0) { timer.stop(); }
/*
                    days.setDescription(Integer.toString(d));
                    hours.setDescription(Integer.toString(h));
                    minutes.setDescription(String.format("%02d", m));
                    seconds.setDescription(String.format("%02d", s));
*/
                    days.setFlipText(Integer.toString(d));
                    hours.setFlipText(Integer.toString(h));
                    minutes.setFlipText(String.format("%02d", m));
                    seconds.setFlipText(String.format("%02d", s));

                    
                    lastTimerCall = now;
                }
            }
        };
    }

    @SuppressWarnings("restriction")
	@Override public void start(Stage stage) {

        HBox pane = new HBox(20, days, hours, minutes, seconds);
        pane.setPadding(new Insets(10));
        pane.setBackground(new Background(new BackgroundFill(Color.web("#606060"), CornerRadii.EMPTY, Insets.EMPTY)));

        //Scene scene = new Scene(pane);

        PerspectiveCamera camera = new PerspectiveCamera();
        camera.setFieldOfView(7);

        Scene scene = new Scene(pane);
        scene.setCamera(camera);
        
        stage.setTitle("Countdown");
        stage.setScene(scene);
        stage.show();

        timer.start();
    }

    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Tile createTile(final String TITLE, final String TEXT) {
        return TileBuilder.create()
 /*
        		.skinType(SkinType.CHARACTER)
                          .prefSize(200, 200)
                          .title(TITLE)
                          .titleAlignment(TextAlignment.CENTER)
                          .description(TEXT)
                          .build();
*/
        .skinType(SkinType.FLIP)
        .characters(Helper.ALPHANUMERIC)
        .flipTimeInMS(500)
        .prefSize(200, 200)
        .title(TITLE)
        .titleAlignment(TextAlignment.CENTER)
        .flipText(TEXT)
        .build();
        
    }
}