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

package eu.hansolo.enzo.gauge;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;

import java.util.Random;


/**
 * User: hansolo
 * Date: 29.12.13
 * Time: 07:56
 */
public class DemoOneEightyGauge extends Application {
    private static final Random         RND = new Random();
    private              OneEightyGauge gauge;
    private              long           lastTimerCall;
    private              AnimationTimer timer;


    @Override public void init() {
        gauge = OneEightyGaugeBuilder.create()
                                     .animated(true)
                                     .title("Temperature")
                                     .unit("°C")
                                     .maxValue(40)
                                     //.barBackgroundColor(Color.BLACK)
                                     //.barColor(Color.web("#5399c6"))
                                     //.valueColor(Color.DARKVIOLET)
                                     //.unitColor(Color.CRIMSON)
                                     //.minTextColor(Color.BLUE)
                                     //.maxTextColor(Color.RED)
                                     //.titleColor(Color.ORANGE)
                                     .dynamicBarColor(true)
                                     .stops(new Stop(0.00, Color.BLUE),
                                            new Stop(0.25, Color.CYAN),
                                            new Stop(0.50, Color.LIME),
                                            new Stop(0.75, Color.YELLOW),
                                            new Stop(1.00, Color.RED))
                                     //.value(25)
                                     .build();

        lastTimerCall = System.nanoTime();
        timer         = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTimerCall + 2_000_000_000l) {
                    gauge.setValue(RND.nextDouble() * gauge.getMaxValue() + gauge.getMinValue());
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane();
        pane.setPadding(new Insets(10, 10, 10, 10));
        pane.getChildren().addAll(gauge);

        Scene scene = new Scene(pane);

        stage.setScene(scene);
        stage.show();

        timer.start();
    }

    @Override public void stop() {

    }

    public static void main(String[] args) {
        launch(args);
    }
}
