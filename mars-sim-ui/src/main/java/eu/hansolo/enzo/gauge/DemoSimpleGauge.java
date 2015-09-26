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

import eu.hansolo.enzo.common.Section;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;


/**
 * Created by
 * User: hansolo
 * Date: 22.07.13
 * Time: 08:41
 */

public class DemoSimpleGauge extends Application {
    private static final   Random RND = new Random();
    private static int     noOfNodes  = 0;
    private SimpleGauge    thermoMeter;
    private SimpleGauge    wattMeter;
    private SimpleGauge    energyMeter;
    private long           lastTimerCall;
    private AnimationTimer timer;

    @Override public void init() {
        thermoMeter = SimpleGaugeBuilder.create()
                                        .prefSize(400, 400)
                                        .sections(new Section(0, 16.66666, "0"),
                                                  new Section(16.66666, 33.33333, "1"),
                                                  new Section(33.33333, 50.0, "2"),
                                                  new Section(50.0, 66.66666, "3"),
                                                  new Section(66.66666, 83.33333, "4"),
                                                  new Section(83.33333, 100.0, "5"))
                                        .title("Temperature")
                                        .unit("C")                                                                              
                                        .value(20)
                                        .styleClass(SimpleGauge.STYLE_CLASS_GREEN_TO_RED_6)
                                        .build();
        wattMeter = SimpleGaugeBuilder.create()
                                      .prefSize(400, 400)
                                      .sections(new Section(0, 16.66666, "0"),
                                                new Section(16.66666, 33.33333, "1"),
                                                new Section(33.33333, 50.0, "2"),
                                                new Section(50.0, 66.66666, "3"),
                                                new Section(66.66666, 83.33333, "4"),
                                                new Section(83.33333, 100.0, "5"))
                                      .unit("W")
                                      .measuredRangeVisible(true)
                                      .rangeFill(Color.rgb(0, 0, 200, 0.5))                                      
                                      .styleClass(SimpleGauge.STYLE_CLASS_GREEN_TO_DARKGREEN_6)
                                      .build();
        energyMeter = SimpleGaugeBuilder.create()
                                        .prefSize(400, 400)                                        
                                        .sections(new Section(0, 10, "A++"),
                                                  new Section(10, 20, "A+"),
                                                  new Section(20, 30, "A"),
                                                  new Section(30, 40, "B"),
                                                  new Section(40, 50, "C"),
                                                  new Section(50, 60, "D"),
                                                  new Section(60, 70, "E"),
                                                  new Section(70, 80, "F"),
                                                  new Section(80, 90, "G"),
                                                  new Section(90, 100, "H"))                                                  
                                        .sectionTextVisible(true)
                                        .unit("W")
                                        .styleClass(SimpleGauge.STYLE_CLASS_GREEN_TO_RED_10)
                                        .build();


        lastTimerCall = System.nanoTime() + 2_000_000_000l;
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTimerCall + 5_000_000_000l) {
                    thermoMeter.setValue(RND.nextDouble() * 100);
                    wattMeter.setValue(RND.nextDouble() * 100);
                    energyMeter.setValue(RND.nextDouble() * 100);
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) throws Exception {
        HBox pane = new HBox();
        pane.setPadding(new Insets(10, 10, 10, 10));
        pane.setSpacing(10);
        pane.getChildren().addAll(thermoMeter, wattMeter, energyMeter);

        final Scene scene = new Scene(pane, Color.BLACK);
        //scene.setFullScreen(true);

        stage.setTitle("SimpleGauge");
        stage.setScene(scene);
        stage.show();

        wattMeter.setValue(50);
                
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
