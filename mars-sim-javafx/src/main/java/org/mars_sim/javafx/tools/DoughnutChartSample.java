package org.mars_sim.javafx.tools;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class DoughnutChartSample extends Application {
 
    @Override public void start(Stage stage) {
        stage.setTitle("Imported Fruits");
        stage.setWidth(500);
        stage.setHeight(500);

        ObservableList<PieChart.Data> pieChartData = createData();

        final DoughnutChart chart = new DoughnutChart(pieChartData);
        chart.setTitle("Imported Fruits");

        Scene scene = new Scene(new StackPane(chart));
        stage.setScene(scene);
        stage.show();
    }

    private ObservableList<PieChart.Data> createData() {
        return FXCollections.observableArrayList(
                new PieChart.Data("Grapefruit", 13),
                new PieChart.Data("Oranges", 25),
                new PieChart.Data("Plums", 10),
                new PieChart.Data("Pears", 22),
                new PieChart.Data("Apples", 30));
    }

    public static void main(String[] args) {
        launch(args);
    }

}