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

package org.mars_sim.javafx.tools;
/*
import org.mars_sim.msp.ui.javafx.SubmitButton;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;


**
 * User: hansolo
 * Date: 22.08.16
 * Time: 08:23
 *
public class SubmitButtonDemo extends Application {
    private SubmitButton   button;
    private double         progress;
    private long           lastTimerCall;
    private boolean        toggle, running = false;
    private AnimationTimer timer;

    @SuppressWarnings("restriction")
	@Override public void init() {
        button = new SubmitButton();
        button.setColor(Color.web("#34495e"));
        button.setOnMousePressed(e -> {
        	
        	if (button.getStatus() != SubmitButton.Status.IN_PROGRESS && !running) {
                running = true;
                button.setDisable(true);
        		timer.start();	
        	}
        	else if (button.getStatus() == SubmitButton.Status.IN_PROGRESS) {
                running = true;
                button.setDisable(true);
        	}
        	else if (button.getStatus() == SubmitButton.Status.FINISHED
        			|| button.getStatus() == SubmitButton.Status.FAIL) {
                running = false;
                button.setDisable(false);
        	}
        	else
        		e.consume();
        });
        button.statusProperty().addListener(o -> System.out.println(button.getStatus()));

        progress      = 0;
        lastTimerCall = System.nanoTime();
        timer         = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTimerCall + 10_000_000l) {
                    progress += 0.005;
                    button.setProgress(progress);
                    lastTimerCall = now;
                    if (toggle) {
                   	
                        if (progress > 0.75) {
                            progress = 0;
                            button.setFailed();
                            timer.stop();
                            running = false;
                            toggle ^= true;
                            button.setDisable(false);
                        }
                        
                    } else {
                        if (progress > 1) {
                            progress = 0;
                            button.setSuccess();
                            timer.stop();
                            running = false;
                            toggle ^= true;
                            button.setDisable(false);
                        }
                    }
                }
                else {
                    running = false;
                    button.setDisable(false);
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane(button);
        pane.setPadding(new Insets(20));

        Scene scene = new Scene(pane);

        stage.setTitle("Submit Button");
        stage.setScene(scene);
        stage.show();
    }

    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
*/