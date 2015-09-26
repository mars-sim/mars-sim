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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;


/**
 * Created by
 * User: hansolo
 * Date: 02.05.13
 * Time: 10:11
 */
public class DemoMulti extends Application {
    private Random         rnd;
    private long           lastTimerCall;
    private AnimationTimer timer;
    private SimpleGauge[]  gauges;
    private static int     noOfNodes;

    @Override public void init() {
        rnd           = new Random();
        lastTimerCall = 0l;
        timer         = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now  > lastTimerCall + 100_000_000l) {
                    for (SimpleGauge gauge : gauges) {
                        gauge.setValue(rnd.nextDouble() * 100.0);
                    }
                    lastTimerCall = now;
                }
            }
        };
        gauges = new SimpleGauge[100];
        for (int i = 0 ; i < 100 ; i++) {
            SimpleGauge gauge = SimpleGaugeBuilder.create()
                                                  .prefSize(50, 50)
                                                  .animationDuration(80)
                                                  .animated(false)
                                                  .sections(new Section(0, 100))
                                                  .styleClass(SimpleGauge.STYLE_CLASS_BLUE_TO_RED_6)
                                                  .build();
            gauges[i] = gauge;
        }

    }

    @Override public void start(Stage stage) {
        FlowPane pane = new FlowPane();
        pane.getChildren().addAll(gauges);

        Scene scene = new Scene(pane, 500, 500, Color.WHITE);

        stage.setTitle("Gauge");
        stage.setScene(scene);
        stage.show();

        calcNoOfNodes(scene.getRoot());
        System.out.println("No. of nodes in scene: " + noOfNodes);

        timer.start();
    }

    private static void calcNoOfNodes(Node node) {
        if (node instanceof Parent) {
            if (((Parent) node).getChildrenUnmodifiable().size() != 0) {
                ObservableList<Node> tempChildren = ((Parent) node).getChildrenUnmodifiable();
                noOfNodes += tempChildren.size();
                for (Node n : tempChildren) {
                    calcNoOfNodes(n);
                }
            }
        }
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
