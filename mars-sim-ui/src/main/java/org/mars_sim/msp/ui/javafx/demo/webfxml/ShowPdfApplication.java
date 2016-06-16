package org.mars_sim.msp.ui.javafx.demo.webfxml;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by rene on 05/04/16.
 */
public class ShowPdfApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent group = new FXMLLoader().load(getClass().getResource("/webfxml/pdf.fxml"));
        Scene scene = new Scene(group, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
