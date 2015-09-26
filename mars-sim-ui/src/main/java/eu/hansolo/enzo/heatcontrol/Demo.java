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

package eu.hansolo.enzo.heatcontrol;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


/**
 * User: hansolo
 * Date: 08.11.13
 * Time: 16:37
 */
public class Demo extends Application {
    private HeatControl    control;
    private long           lastTimerCall;
    private AnimationTimer timer;

    @Override public void init() {
        control       = new HeatControl();
        control.setTargetEnabled(true);
        lastTimerCall = System.nanoTime();
        timer         = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTimerCall + 1_000_000_000l) {
                    if (Double.compare(control.getTarget(), control.getValue()) > 0) {
                        control.setValue(control.getValue() + 0.1);
                    } else if (Double.compare(control.getTarget(), control.getValue()) < 0) {
                        control.setValue(control.getValue() - 0.1);
                    }
                    
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane();
        pane.getChildren().add(control);

        Scene scene = new Scene(pane);

        stage.setTitle("Demo HeatControl");
        stage.setScene(scene);
        stage.show();
        
        timer.start();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
