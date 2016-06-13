// http://stackoverflow.com/questions/24472170/how-can-we-make-text-auto-scroll-continuious-loop

package org.mars_sim.msp.demo;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Scroll extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox vbox = new VBox();
        for (int i = 0; i < 30; i++)
            vbox.getChildren().add(new Text("line " + i));
        //add a copy of the first 12 lines that will be showing as wrapped
        for (int i = 0; i < 12; i++)
            vbox.getChildren().add(new Text("line " + i));

        ScrollPane sp = new ScrollPane(vbox);
        Scene scene = new Scene(sp, 300, 10*12);//guess height

        primaryStage.setScene(scene);
        primaryStage.show();
        //resize to exactly 12 lines
        double textHeight = vbox.getHeight() / vbox.getChildren().size();
        primaryStage.setHeight(textHeight*12+primaryStage.getHeight()-scene.getHeight());

        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        KeyValue kv = new KeyValue(sp.vvalueProperty(), sp.getVmax());
        KeyFrame kf = new KeyFrame(Duration.millis(5000), kv);
        timeline.getKeyFrames().addAll(kf);
        timeline.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}