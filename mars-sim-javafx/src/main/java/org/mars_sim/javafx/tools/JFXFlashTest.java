package org.mars_sim.javafx.tools;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class JFXFlashTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final StackPane pane = new StackPane();
        pane.setBackground(new Background(new BackgroundFill(Color.rgb(54, 54, 54), CornerRadii.EMPTY, Insets.EMPTY)));
        final Scene scene = new Scene(pane, 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}