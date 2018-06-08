/*
 * Copyright (c) 2016 by Gerrit Grunwald
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

package org.mars_sim.msp.ui.javafx.tools;

/* need to enable medusa artifact in pom.xml


import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;

import java.util.Random;



 // User: hansolo
 // Date: 26.04.16
 // Time: 18:46
 //
public class WhiteGaugeDemo extends Application {
    private static final Random RND = new Random();
    private Color[]        colors   = { Color.rgb(65,170,227), Color.rgb(242,72,45), Color.rgb(185,216,64), Color.rgb(232,75,159) };
    private WhiteGauge     gauge;
    private long           lastTimerCall;
    private AnimationTimer timer;
    private Pane           pane;
    private int            counter;


    @Override public void init() {
        gauge = new WhiteGauge(0, 0, 100, "Unit");
        counter       = 0;
        lastTimerCall = System.nanoTime();
        timer         = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTimerCall + 2_000_000_000l) {
                    gauge.getModel().setValue(RND.nextDouble() * gauge.getModel().getRange() + gauge.getModel().getMinValue());
                    pane.setBackground(new Background(new BackgroundFill(colors[counter], CornerRadii.EMPTY, Insets.EMPTY)));
                    counter++;
                    if (counter == 4) counter = 0;
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        pane = new StackPane(gauge);
        pane.setPadding(new Insets(20));
        pane.setBackground(new Background(new BackgroundFill(Color.rgb(65, 170, 227), CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(pane);

        stage.setTitle("White Gauge");
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
}
*/