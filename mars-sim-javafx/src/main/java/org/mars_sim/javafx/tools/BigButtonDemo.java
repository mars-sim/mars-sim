package org.mars_sim.javafx.tools;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class BigButtonDemo extends Application {

    @Override
    public void start(Stage primaryStage) {
        Button btn = new Button();
        btn.setText("Alpha Base" + System.lineSeparator() + "Population : 8");
        btn.setGraphic(new Rectangle(30,30, Color.RED));
        btn.setMinHeight(200);
        btn.setMinWidth(250);
        //btn.setStyle("-fx-alignment: LEFT;");
        btn.setAlignment(Pos.BASELINE_LEFT);

        StackPane root = new StackPane();
        root.getChildren().add(btn);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }

    public static void main(String[] args) { launch(); }
}