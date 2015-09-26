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

package eu.hansolo.enzo.charts;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Random;


/**
 * User: hansolo
 * Date: 04.03.14
 * Time: 08:35
 */
public class DemoSimpleRadarChart extends Application {
    private static final Random RND       = new Random();
    private static       int    noOfNodes = 0;
    private SimpleRadarChart    chart;

    @Override public void init() {
        chart = new SimpleRadarChart();
        chart.setTitle("Temperature\n" + LocalDate.now());
        chart.setUnit("°C");
        chart.setScaleVisible(true);
        chart.setMinValue(-15);
        chart.setMaxValue(40);
        chart.setZeroLineVisible(true);
        chart.setFilled(true);
        chart.setNoOfSectors(24);
        for (int i = 0; i <= 24; i++) {
            chart.addData(i, new XYChart.Data<>(i < 10 ? "0" + i + ":00" : i + ":00", RND.nextDouble() * 55 - 15));
        }
        chart.setGradientStops(new Stop(0.00000, Color.web("#3552a0")),
                               new Stop(0.09090, Color.web("#456acf")),
                               new Stop(0.27272, Color.web("#45a1cf")),
                               new Stop(0.36363, Color.web("#30c8c9")),
                               new Stop(0.45454, Color.web("#30c9af")),
                               new Stop(0.50909, Color.web("#56d483")),
                               new Stop(0.72727, Color.web("#9adb49")),
                               new Stop(0.81818, Color.web("#efd750")),
                               new Stop(0.90909, Color.web("#ef9850")),
                               new Stop(1.00000, Color.web("#ef6050")));
        chart.setPolygonMode(true);
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane();
        pane.setPadding(new Insets(5, 5, 5, 5));
        pane.getChildren().addAll(chart);

        Scene scene = new Scene(pane);

        stage.setTitle("Demo SimpleRadarChart");
        stage.setScene(scene);
        stage.show();

        calcNoOfNodes(scene.getRoot());
        System.out.println(noOfNodes + " Nodes in SceneGraph");
    }

    @Override public void stop() {

    }

    public static void main(String[] args) {
        launch(args);
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
