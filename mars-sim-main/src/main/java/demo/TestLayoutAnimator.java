package demo;

import java.util.Random;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Creates a FlowPane and adds some rectangles inside.
 * A LayoutAnimator is set to observe the contents of the FlowPane for layout
 * changes.
 */
public class TestLayoutAnimator extends Application {

  @Override
  public void start(Stage primaryStage) {
    final Pane root = new FlowPane();

    // Clicking on button adds more rectangles
    Button btn = new Button();
    btn.setText("Add Rectangles");
    final TestLayoutAnimator self = this;
    btn.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        self.addRectangle(root);
      }
    });
    root.getChildren().add(btn);

    // add 5 rectangles to start with
    for (int i = 0; i < 5; i++) {
      addRectangle(root);
    }
    root.layout();
    LayoutAnimator ly = new LayoutAnimator();
    ly.observe(root.getChildren());

    Scene scene = new Scene(root, 300, 250);

    primaryStage.setTitle("Flow Layout Test");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  protected void addRectangle(Pane root) {
    Random rnd = new Random();
    Rectangle nodeNew = new Rectangle(50 + rnd.nextInt(20), 40 + rnd.nextInt(20));

    nodeNew.setStyle("-fx-margin: 10;");
    String rndColor = String.format("%02X", rnd.nextInt(), rnd.nextInt(), rnd.nextInt());
    try {
      Paint rndPaint = Paint.valueOf(rndColor);
      nodeNew.setFill(rndPaint);
    } catch (Exception e) {
      nodeNew.setFill(Paint.valueOf("#336699"));
    }

    nodeNew.setStroke(Paint.valueOf("black"));
    root.getChildren().add(0, nodeNew);
  }
}