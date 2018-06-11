package org.mars_sim.msp.ui.javafx.tabboard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TabboardMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/fxui/fxml/tabboard/main_view.fxml"));
    	//FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxui/fxml/dashboard/oneSettler.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 1366, 768));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
