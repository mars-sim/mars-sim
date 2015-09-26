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

import eu.hansolo.enzo.common.Marker;
import eu.hansolo.enzo.common.Section;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;

import java.util.Random;


/**
 * User: hansolo
 * Date: 22.07.14
 * Time: 15:37
 */
public class RadialSteelDemo extends Application {
    private static final Random RND       = new Random();
    private static       int    noOfNodes = 0;
    private RadialSteelGauge    leftControl;
    private RadialSteelGauge    rightControl;
    private long                lastTimerCall;
    private AnimationTimer      timer;


    @Override public void init() {
        leftControl = RadialSteelGaugeBuilder.create()
                                             //.value(50)
                                             .title("Bright")
                                             .unit("Bright")
                                             .build();

        rightControl = RadialSteelGaugeBuilder.create()
                                              .title("Dark")
                                              .unit("Dark")
                                              .style("-body: black; -tick-mark-fill: white; -tick-label-fill: white;")
                                              .build();

        lastTimerCall = System.nanoTime() + 50_000_000l;
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTimerCall + 2_000_000_000l) {
                    leftControl.setValue(RND.nextDouble() * 100);
                    rightControl.setValue(RND.nextDouble() * 100);
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) throws Exception {
        HBox controlBox = new HBox(leftControl, rightControl);
        controlBox.setSpacing(20);

        StackPane pane = new StackPane();
        pane.setPadding(new Insets(5, 5, 5, 5));
        pane.getChildren().add(controlBox);

        final Scene scene = new Scene(pane, Color.BLACK);
        //scene.getStylesheets().add(getClass().getResource("demo.css").toExternalForm());
        //scene.setFullScreen(true);

        stage.setTitle("RadialSteelGauge");
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
