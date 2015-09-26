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
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;


/**
 * Created by hansolo on 21.08.14.
 */
public class DemoLinear extends Application {
    private static final Random RND       = new Random();
    private static       int    noOfNodes = 0;
    private Linear         linear;
    private long           lastTimerCall;
    private AnimationTimer timer;

    @Override public void init() {
        linear = LinearBuilder.create()
                              .prefSize(600, 600)
                              .title("Title")
                              .unit("°C")
                              .value(50)
                              .orientation(Orientation.HORIZONTAL)
                              //.ledColor(Color.CYAN)
                              //.barColor(Color.PURPLE)
                              .build();

        lastTimerCall = System.nanoTime() + 2_000_000_000l;
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTimerCall + 5_000_000_000l) {
                    linear.setValue(RND.nextDouble() * 100);
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) throws Exception {
        StackPane pane = new StackPane();
        pane.setPadding(new Insets(10, 10, 10, 10));
        pane.getChildren().addAll(linear);

        final Scene scene = new Scene(pane, Color.BLACK);
        //scene.setFullScreen(true);

        stage.setTitle("Radial");
        stage.setScene(scene);
        stage.show();

        timer.start();

        calcNoOfNodes(scene.getRoot());
        System.out.println(noOfNodes + " Nodes in SceneGraph");
    }

    @Override public void stop() {

    }

    public static void main(final String[] args) {
        Application.launch(args);
    }

    // ******************** Misc **********************************************
    private static void calcNoOfNodes(Node node) {
        if (node instanceof Parent) {
            if (((Parent) node).getChildrenUnmodifiable().size() != 0) {
                ObservableList<Node> tempChildren = ((Parent) node).getChildrenUnmodifiable();
                noOfNodes += tempChildren.size();
                for (Node n : tempChildren) {
                    calcNoOfNodes(n);
                    //System.out.println(n.getStyleClass().toString());
                }
            }
        }
    }
}

