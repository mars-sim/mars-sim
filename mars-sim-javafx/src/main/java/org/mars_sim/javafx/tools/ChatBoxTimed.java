package org.mars_sim.javafx.tools;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

//from https://gist.github.com/james-d/9686094
// requires Clock.java

public class ChatBoxTimed extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        final Clock clock = new Clock(Duration.seconds(1), true);
        
        final BorderPane root = new BorderPane();
        final HBox controls = new HBox(5);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(10));
        
        final TextField itemField = new TextField();
        itemField.setTooltip(new Tooltip("Type an item and press Enter"));
        controls.getChildren().add(itemField);
        
        final VBox items = new VBox(5);
        
        itemField.setOnAction(event -> {
            String text = itemField.getText();
            Label label = new Label();
            label.textProperty().bind(Bindings.format("%s (%s)", text, clock.getElapsedStringBinding()));
            items.getChildren().add(label);
            itemField.setText("");
        });
        
        final ScrollPane scroller = new ScrollPane();
        scroller.setContent(items);
        
        final Label currentTimeLabel = new Label();
        currentTimeLabel.textProperty().bind(Bindings.format("Current time: %s", clock.getTimeStringBinding()));
        
        root.setTop(controls);
        root.setCenter(scroller);
        root.setBottom(currentTimeLabel);
        
        Scene scene = new Scene(root, 600, 400);
        
        primaryStage.setTitle("Timed item display");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}