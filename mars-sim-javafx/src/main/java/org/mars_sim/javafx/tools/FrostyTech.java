package org.mars_sim.javafx.tools;

import javafx.animation.*;
import javafx.application.*;
import javafx.beans.property.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.effect.*;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class FrostyTech extends Application {

    private static final double BLUR_AMOUNT = 10;

    private static final Effect frostEffect =
        new BoxBlur(BLUR_AMOUNT, BLUR_AMOUNT, 3);

    private static final ImageView background = new ImageView();
    private static final StackPane layout = new StackPane();

    @Override public void start(Stage stage) {
        layout.getChildren().setAll(background, createContent());
        layout.setStyle("-fx-background-color: null");

        Scene scene = new Scene(
                layout,
                200, 300,
                Color.TRANSPARENT
        );

        Platform.setImplicitExit(false);

        scene.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) Platform.exit();
        });
        makeSmoke(stage);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.show();

        background.setImage(copyBackground(stage));
        background.setEffect(frostEffect);

        makeDraggable(stage, layout);
    }

    // copy a background node to be frozen over.
    private Image copyBackground(Stage stage) {
        final int X = (int) stage.getX();
        final int Y = (int) stage.getY();
        final int W = (int) stage.getWidth();
        final int H = (int) stage.getHeight();

        try {
            java.awt.Robot robot = new java.awt.Robot();
            java.awt.image.BufferedImage image = robot.createScreenCapture(new java.awt.Rectangle(X, Y, W, H));

            return SwingFXUtils.toFXImage(image, null);
        } catch (java.awt.AWTException e) {
            System.out.println("The robot of doom strikes!");
            e.printStackTrace();

            return null;
        }
    }

    // create some content to be displayed on top of the frozen glass panel.
    private Label createContent() {
        Label label = new Label("PAUSE");
        label.setPadding(new Insets(10));

        label.setStyle("-fx-font-size: 20px; -fx-text-fill: cyan;");
        label.setMaxWidth(250);
        label.setWrapText(true);

        return label;
    }

    // makes a stage draggable using a given node.
    public void makeDraggable(final Stage stage, final Node byNode) {
        final Delta dragDelta = new Delta();
        byNode.setOnMousePressed(mouseEvent -> {
            // record a delta distance for the drag and drop operation.
            dragDelta.x = stage.getX() - mouseEvent.getScreenX();
            dragDelta.y = stage.getY() - mouseEvent.getScreenY();
            byNode.setCursor(Cursor.MOVE);
        });
        final BooleanProperty inDrag = new SimpleBooleanProperty(false);

        byNode.setOnMouseReleased(mouseEvent -> {
            byNode.setCursor(Cursor.HAND);

            if (inDrag.get()) {
                stage.hide();

                Timeline pause = new Timeline(new KeyFrame(Duration.millis(50), event -> {
                    background.setImage(copyBackground(stage));
                    layout.getChildren().set(
                            0,
                            background
                    );
                    stage.show();
                }));
                pause.play();
            }

            inDrag.set(false);
        });
        byNode.setOnMouseDragged(mouseEvent -> {
            stage.setX(mouseEvent.getScreenX() + dragDelta.x);
            stage.setY(mouseEvent.getScreenY() + dragDelta.y);

            layout.getChildren().set(
                    0,
                    makeSmoke(stage)
            );

            inDrag.set(true);
        });
        byNode.setOnMouseEntered(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                byNode.setCursor(Cursor.HAND);
            }
        });
        byNode.setOnMouseExited(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                byNode.setCursor(Cursor.DEFAULT);
            }
        });
    }

    private javafx.scene.shape.Rectangle makeSmoke(Stage stage) {
        return new javafx.scene.shape.Rectangle(
                stage.getWidth(),
                stage.getHeight(),
                Color.WHITESMOKE.deriveColor(
                        0, 1, 1, 0.08
                )
        );
    }

    /** records relative x and y co-ordinates. */
    private static class Delta {
        double x, y;
    }

    public static void main(String[] args) {
        launch(args);
    }
}