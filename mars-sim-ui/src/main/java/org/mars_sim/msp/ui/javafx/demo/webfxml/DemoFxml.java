package org.mars_sim.msp.ui.javafx.demo.webfxml;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Created by rene on 28/03/16.
 */
public class DemoFxml extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load components from the xml file (from the resource folder with the full name specified)
        Parent group = new FXMLLoader().load(getClass().getResource("/webfxml/DemoFxml.fxml"));

        Scene scene = new Scene(group, 500, 250, Color.LIGHTBLUE);
        primaryStage.setTitle("Hello JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
