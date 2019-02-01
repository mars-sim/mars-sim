package org.mars_sim.javafx.tools;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ProgressIndicatorTest2 extends Application {

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        ProgressIndicator pi = new ProgressIndicator();
        Task<Void> counter = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                for (int i = 1; i <= 100; i++) {
                    Thread.sleep(50);
                    updateProgress(i, 100);
                }
                return null;
            }
        };
        pi.progressProperty().bind(counter.progressProperty());
        pi.progressProperty().addListener((obs, oldProgress, newProgress) -> {
            PseudoClass warning = PseudoClass.getPseudoClass("warning");
            PseudoClass critical = PseudoClass.getPseudoClass("critical");
            if (newProgress.doubleValue() < 0.3) {
                pi.pseudoClassStateChanged(warning, false);
                pi.pseudoClassStateChanged(critical, true);
            } else if (newProgress.doubleValue() < 0.65) {
                pi.pseudoClassStateChanged(warning, true);
                pi.pseudoClassStateChanged(critical, false);
            } else {
                pi.pseudoClassStateChanged(warning, false);
                pi.pseudoClassStateChanged(critical, false);
            }
        });
        pi.setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
        root.setStyle("-fx-background-color: antiqueWhite;");
        root.getChildren().add(pi);
        Scene scene = new Scene(root, 400, 400);
        scene.getStylesheets().add("/css/progress.css");
        primaryStage.setScene(scene);
        primaryStage.show();
        new Thread(counter).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}