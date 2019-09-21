/**
 * Mars Simulation Project
 * EffectUtilities.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 * see the original codes based on https://gist.github.com/jewelsea/3388637
 */

package org.mars_sim.javafx;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

/** Various utilities for applying different effects to nodes. */
public class EffectUtilities {
  /** configures the node to fade when it is clicked on performed the onFinished handler when the fade is complete */
  public static void fadeOnClick(final Node node, final EventHandler<ActionEvent> onFinished) {
    node.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        node.setMouseTransparent(true);
        FadeTransition fade = new FadeTransition(Duration.seconds(1.2), node);
        fade.setOnFinished(onFinished);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.play();
      }
    });
  }

  /* adds a glow effect to a node when the mouse is hovered over the node */
  public static void addGlowOnHover(final Node node) {
    final Glow glow = new Glow();
    node.setOnMouseEntered(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
        node.setEffect(glow);
      }
    });
    node.setOnMouseExited(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
        node.setEffect(null);
      }
    });
  }


  /** makes a node draggable using a given node 
  public static void makeDraggable(final Node node, final Node byNode) {
    final Delta dragDelta = new Delta();
    byNode.setOnMousePressed(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
        // record a delta distance for the drag and drop operation.
        dragDelta.x = node.getX() - mouseEvent.getScreenX();
        dragDelta.y = node.getY() - mouseEvent.getScreenY();
        byNode.setCursor(Cursor.MOVE);
      }
    });
    byNode.setOnMouseReleased(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
        byNode.setCursor(Cursor.HAND);
      }
    });
    byNode.setOnMouseDragged(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
        node.setX(mouseEvent.getScreenX() + dragDelta.x);
        node.setY(mouseEvent.getScreenY() + dragDelta.y);
      }
    });
    byNode.setOnMouseEntered(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
        if (!mouseEvent.isPrimaryButtonDown()) {
          byNode.setCursor(Cursor.HAND);
        }
      }
    });
    byNode.setOnMouseExited(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
        if (!mouseEvent.isPrimaryButtonDown()) {
          byNode.setCursor(Cursor.DEFAULT);
        }
      }
    });
  }
  */
  
  /** makes a stage draggable using a given node */
  public static void makeDraggable(final Stage stage, final Node byNode) {
    final Delta dragDelta = new Delta();
    byNode.setOnMousePressed(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
        // record a delta distance for the drag and drop operation.
        dragDelta.x = stage.getX() - mouseEvent.getScreenX();
        dragDelta.y = stage.getY() - mouseEvent.getScreenY();
        byNode.setCursor(Cursor.MOVE);
      }
    });
    byNode.setOnMouseReleased(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
        byNode.setCursor(Cursor.HAND);
      }
    });
    byNode.setOnMouseDragged(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
        stage.setX(mouseEvent.getScreenX() + dragDelta.x);
        stage.setY(mouseEvent.getScreenY() + dragDelta.y);
      }
    });
    byNode.setOnMouseEntered(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
        if (!mouseEvent.isPrimaryButtonDown()) {
          byNode.setCursor(Cursor.HAND);
        }
      }
    });
    byNode.setOnMouseExited(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
        if (!mouseEvent.isPrimaryButtonDown()) {
          byNode.setCursor(Cursor.DEFAULT);
        }
      }
    });
  }

  /** records relative x and y co-ordinates. */
  private static class Delta {
    double x, y;
  }
}
