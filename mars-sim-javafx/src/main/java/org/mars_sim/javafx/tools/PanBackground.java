package org.mars_sim.javafx.tools;

import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

class PanTest extends StackPane {
  private Rectangle rect;
  private double pressedX, pressedY;
  private LongProperty frame = new SimpleLongProperty();

  public PanTest() {
    setMinSize(600, 600);
    setStyle("-fx-border-color: blue;");
    Label count = new Label();
    count.textProperty().bind(Bindings.convert(frame));
    getChildren().add(count);
    count.setMouseTransparent(true);

    setOnMousePressed(new EventHandler<MouseEvent>() {
      public void handle(MouseEvent event) {
        pressedX = event.getX();
        pressedY = event.getY();
      }
    });

    setOnMouseDragged(new EventHandler<MouseEvent>() {
      public void handle(MouseEvent event) {
        setTranslateX(getTranslateX() + event.getX() - pressedX);
        setTranslateY(getTranslateY() + event.getY() - pressedY);

        event.consume();
      }
    });

    Timeline t = new Timeline(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
      @Override public void handle(ActionEvent event) {
        frame.set(frame.get() + 1);

        if (rect != null) {
          getChildren().remove(rect);
        }

        rect = new Rectangle(10, 10, 200, 200);
        rect.setFill(Color.RED);
        rect.setMouseTransparent(true);
        getChildren().add(0, rect);
      }
    }));
    t.setCycleCount(Timeline.INDEFINITE);
    t.play();
  }
}

public class PanBackground extends Application {
  public static void main(String[] args) { launch(args); }
  @Override public void start(Stage stage) {
    stage.setScene(new Scene(new PanTest()));
    stage.show();
  }
}