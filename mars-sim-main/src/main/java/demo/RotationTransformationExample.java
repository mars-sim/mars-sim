package demo;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

/** Example of how Rotation Transforms work in JavaFX */
public class RotationTransformationExample extends Application {
  @Override public void start(final Stage stage) throws Exception {
    stage.setTitle("Rotation Transform Example");

    // create some controls to manipulate the x and y pivot points of the rotation.
    final Slider xPivotSlider = createSlider(50, "Best values are 0, 50 or 100");
    final Slider yPivotSlider = createSlider(50, "Best values are 0, 50 or 100");
    final Slider zPivotSlider = createSlider(0,  "Won't do anything until you use an X or Y axis of rotation");
    final ToggleGroup  axisToggleGroup = new PersistentButtonToggleGroup();
    final ToggleButton xAxisToggleButton = new ToggleButton("X Axis");
    final ToggleButton yAxisToggleButton = new ToggleButton("Y Axis");
    final ToggleButton zAxisToggleButton = new ToggleButton("Z Axis");
    xAxisToggleButton.setToggleGroup(axisToggleGroup);
    yAxisToggleButton.setToggleGroup(axisToggleGroup);
    zAxisToggleButton.setToggleGroup(axisToggleGroup);

    // create a node to animate.
    Node square;
    try { // grab a smurf from the path if it is there otherwise just use a green square.
      square = new ImageView(new Image("http://bluebuddies.com/gallery/title/jpg/Smurf_Fun_100x100.jpg"));
    } catch (Exception e) {
      square = new Rectangle(100, 100, Color.FORESTGREEN);
    }
    square.setTranslateZ(150);           
    square.setOpacity(0.7);
    square.setMouseTransparent(true);

    // create a rotation transform starting at 0 degrees, rotating about pivot point 50, 50.
    final Rotate rotationTransform = new Rotate(0, 50, 50);
    square.getTransforms().add(rotationTransform);

    // rotate a square using timeline attached to the rotation transform's angle property.
    final Timeline rotationAnimation = new Timeline();
    rotationAnimation.getKeyFrames()
      .add(
        new KeyFrame(
          Duration.seconds(5),
          new KeyValue(
            rotationTransform.angleProperty(),
            360
          )
        )
      );
    rotationAnimation.setCycleCount(Animation.INDEFINITE);
    rotationAnimation.play();

    // bind the transforms pivot points to our slider controls.
    rotationTransform.pivotXProperty().bind(xPivotSlider.valueProperty());
    rotationTransform.pivotYProperty().bind(yPivotSlider.valueProperty());
    rotationTransform.pivotZProperty().bind(zPivotSlider.valueProperty());

    // allow our toggle controls to choose the axis of rotation..
    xAxisToggleButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override public void handle(ActionEvent actionEvent) {
        rotationTransform.setAxis(Rotate.X_AXIS);
      }
    });
    yAxisToggleButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override public void handle(ActionEvent actionEvent) {
        rotationTransform.setAxis(Rotate.Y_AXIS);
      }
    });
    zAxisToggleButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override public void handle(ActionEvent actionEvent) {
        rotationTransform.setAxis(Rotate.Z_AXIS);
      }
    });
    zAxisToggleButton.fire();

    // display a crosshair to mark the current pivot point.
    final Line verticalLine   = new Line(0, -10, 0, 10); verticalLine.setStroke(Color.FIREBRICK);   verticalLine.setStrokeWidth(3);   verticalLine.setStrokeLineCap(StrokeLineCap.ROUND);
    final Line horizontalLine = new Line(-10, 0, 10, 0); horizontalLine.setStroke(Color.FIREBRICK); horizontalLine.setStrokeWidth(3); verticalLine.setStrokeLineCap(StrokeLineCap.ROUND);
    Group pivotMarker = new Group(verticalLine, horizontalLine);
    pivotMarker.translateXProperty().bind(xPivotSlider.valueProperty().subtract(50));
    pivotMarker.translateYProperty().bind(yPivotSlider.valueProperty().subtract(50));

    // display a dashed square border outline to mark the original location of the square.
    final Rectangle squareOutline = new Rectangle(100, 100);
    squareOutline.setFill(Color.TRANSPARENT);
    squareOutline.setOpacity(0.7);
    squareOutline.setMouseTransparent(true);
    squareOutline.setStrokeType(StrokeType.INSIDE);
    squareOutline.setStrokeWidth(1);
    squareOutline.setStrokeLineCap(StrokeLineCap.BUTT);
    squareOutline.setStroke(Color.DARKGRAY);
    squareOutline.setStrokeDashOffset(5);
    squareOutline.getStrokeDashArray().add(10.0);

    // layout the scene.
    final HBox xPivotControl = new HBox(5);
    xPivotControl.getChildren().addAll(new Label("X Pivot Point"), xPivotSlider);
    HBox.setHgrow(xPivotSlider, Priority.ALWAYS);

    final HBox yPivotControl = new HBox(5);
    yPivotControl.getChildren().addAll(new Label("Y Pivot Point"), yPivotSlider);
    HBox.setHgrow(yPivotSlider, Priority.ALWAYS);

    final HBox zPivotControl = new HBox(5);
    zPivotControl.getChildren().addAll(new Label("Z Pivot Point"), zPivotSlider);
    HBox.setHgrow(zPivotSlider, Priority.ALWAYS);

    final HBox axisControl = new HBox(20);
    axisControl.getChildren().addAll(new Label("Axis of Rotation"), xAxisToggleButton, yAxisToggleButton, zAxisToggleButton);
    axisControl.setAlignment(Pos.BASELINE_LEFT);

    final StackPane displayPane = new StackPane();
    displayPane.getChildren().addAll(square, pivotMarker, squareOutline);
    displayPane.setTranslateY(80);
    displayPane.setMouseTransparent(true);

    final StackPane layout = new StackPane();
    layout.getChildren().addAll(
      VBoxBuilder.create().spacing(10).alignment(Pos.TOP_CENTER).children(xPivotControl, yPivotControl, zPivotControl, axisControl).build(),
      displayPane
    );
    layout.setStyle("-fx-background-color: linear-gradient(to bottom, cornsilk, midnightblue); -fx-padding:10; -fx-font-size: 16");
    final Scene scene = new Scene(layout, 480, 550);
    stage.setScene(scene);
    stage.show();
  }

  /**
   * Generate a new slider control initialized to the given value.
   * @param value the initial value of the slider.
   * @param helpText the tool tip text to use for the slider.
   * @return the new slider.
   */
  private Slider createSlider(final double value, final String helpText) {
    final Slider slider = new Slider(-50, 151, value);
    slider.setMajorTickUnit(50);
    slider.setMinorTickCount(0);
    slider.setShowTickMarks(true);
    slider.setShowTickLabels(true);
    slider.setStyle("-fx-text-fill: white");
    slider.setTooltip(new Tooltip(helpText));
    return slider;
  }

  /**
   * Create a toggle group of buttons where one toggle will always remain switched on.
   */
  class PersistentButtonToggleGroup extends ToggleGroup {
    PersistentButtonToggleGroup() {
      super();
      getToggles().addListener(new ListChangeListener<Toggle>() {
        @Override public void onChanged(Change<? extends Toggle> c) {
          while (c.next()) {
            for (final Toggle addedToggle : c.getAddedSubList()) {
              ((ToggleButton) addedToggle).addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent mouseEvent) {
                  if (addedToggle.equals(getSelectedToggle())) mouseEvent.consume();
                }
              });
            }
          }
        }
      });
    }
  }

  public static void main(String[] args) throws Exception { launch(args); }
}