package org.mars_sim.javafx.tools;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ButtonDemo extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxui/fxml/buttondemo.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/css/buttondemo.css");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Button of choice - DEMO");
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}