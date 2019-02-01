package org.mars_sim.javafx.tools;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class WebViewDemo extends Application {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        StackPane root = new StackPane();
        WebView x = new WebView();
        WebEngine ex = x.getEngine();
        ex.load("https://github.com/travis-ci/travis-ci");

        root.getChildren().add(x);
        java.net.CookieHandler.setDefault(null);
        Scene scene = new Scene(root, 1024, 768);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}