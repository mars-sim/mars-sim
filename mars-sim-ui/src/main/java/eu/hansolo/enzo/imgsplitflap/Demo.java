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
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Random;


/**
 * Created by
 * User: hansolo
 * Date: 18.04.13
 * Time: 08:20
 */
public class Demo extends Application {
    private static final Random RND = new Random();
    private SplitFlap           control;
    private long                lastTimerCall;
    private AnimationTimer      timer;

    @Override public void init() {                
        control = new SplitFlap();
        control.setSelection(SplitFlap.ALPHANUMERIC);
        control.setFlipTime(100);
        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTimerCall + 1_000_000_000l) {
                    //control.setText(Integer.toString(RND.nextInt(9)));
                    control.textProperty().set(Integer.toString(RND.nextInt(9)));
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {                  
        StackPane pane = new StackPane();
        pane.getChildren().addAll(control);        

        Scene scene = new Scene(pane);
        scene.setCamera(new PerspectiveCamera(false));

        stage.setScene(scene);
        stage.show();

        timer.start();                
    }

    public static void main(String[] args) {
        launch(args);
    }
}
